package integeruser.jgltut.tut14;

import integeruser.jglsdk.glimg.DdsLoader;
import integeruser.jglsdk.glimg.ImageSet;
import integeruser.jglsdk.glimg.ImageSet.Dimensions;
import integeruser.jglsdk.glimg.ImageSet.SingleImage;
import integeruser.jglsdk.glutil.MousePoles.*;
import integeruser.jgltut.Tutorial;
import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.MaterialBlock;
import integeruser.jgltut.commons.PerLight;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.framework.*;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2014%20Textures%20Are%20Not%20Pictures/Material%20Texture.cpp
 * <p>
 * Part IV. Texturing
 * Chapter 14. Textures are not Pictures
 * <p>
 * P        - toggle pausing.
 * -,=      - rewind/jump forward time by 0.5 second (of real-time).
 * T        - toggle viewing the look-at point.
 * G        - toggle the drawing of the light source.
 * Y        - switch between the infinity symbol and a flat plane.
 * SPACE    - switch between one of three rendering modes: fixed shininess with a Gaussian lookup-table, a texture-based
 * shininess with a Gaussian lookup-table, and a texture-based shininess with a shader-computed Gaussian term.
 * 1,2,3,4  - switch to progressively larger textures.
 * 8,9      - switch to the gold material/a material with a dark diffuse color and bright specular color.
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING         - rotate the object horizontally and vertically, relative to the current camera
 * view.
 * RIGHT  CLICKING and DRAGGING + CTRL  - rotate the object horizontally or vertically only, relative to the current
 * camera view.
 * RIGHT  CLICKING and DRAGGING + ALT   - spin the object.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class MaterialTexture extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut14/data/";
        new MaterialTexture().start(500, 500);
    }


    @Override
    protected void init() {
        initializePrograms();

        objectMesh = new Mesh("Infinity.xml");
        cubeMesh = new Mesh("UnitCube.xml");
        planeMesh = new Mesh("UnitPlane.xml");

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        final float depthZNear = 0.0f;
        final float depthZFar = 1.0f;

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(depthZNear, depthZFar);
        glEnable(GL_DEPTH_CLAMP);

        // Setup our Uniform Buffers
        setupMaterials();

        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, LightBlock.BYTES, GL_DYNAMIC_DRAW);

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.BYTES, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.BYTES);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.BYTES);

        glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, 0, MaterialBlock.BYTES);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        createGaussianTextures();
        createShininessTexture();


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_P:
                        lightTimer.togglePause();
                        break;

                    case GLFW_KEY_MINUS:
                        lightTimer.rewind(0.5f);
                        break;

                    case GLFW_KEY_EQUAL:
                        lightTimer.fastForward(0.5f);
                        break;

                    case GLFW_KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;

                    case GLFW_KEY_G:
                        drawLights = !drawLights;
                        break;

                    case GLFW_KEY_Y:
                        useInfinity = !useInfinity;
                        break;

                    case GLFW_KEY_SPACE:
                        int index = (shaderMode.ordinal() + 1) % ShaderMode.NUM_SHADER_MODES.ordinal();
                        shaderMode = ShaderMode.values()[index];
                        System.out.printf("%s\n", shaderModeNames[shaderMode.ordinal()]);
                        break;

                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
                }

                if (GLFW_KEY_1 <= key && key <= GLFW_KEY_9) {
                    int number = key - GLFW_KEY_1;
                    if (number < NUM_GAUSS_TEXTURES) {
                        System.out.printf("Angle Resolution: %d\n", calcCosAngResolution(number));
                        currTexture = number;
                    }

                    if (number >= (9 - NUM_MATERIALS)) {
                        number = number - (9 - NUM_MATERIALS);
                        System.out.printf("Material number %d\n", number);
                        currMaterial = number;
                    }
                }
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            boolean pressed = action == GLFW_PRESS;
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
            MousePole.forwardMouseButton(window, viewPole, button, pressed, x, y);
            MousePole.forwardMouseButton(window, objtPole, button, pressed, x, y);
        });
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
                MousePole.forwardMouseMotion(objtPole, (int) xpos, (int) ypos);
            }
        });
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
            MousePole.forwardMouseWheel(window, viewPole, (int) yoffset, x, y);
            MousePole.forwardMouseWheel(window, objtPole, (int) yoffset, x, y);
        });
    }

    @Override
    protected void display() {
        lightTimer.update(elapsedTime);

        glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());
        final Matrix4f worldToCamMat = modelMatrix;

        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
        float halfLightDistance = 25.0f;
        lightData.lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

        Vector3f globalLightDirection = new Vector3f(0.707f, 0.707f, 0.0f);

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = worldToCamMat.transform(new Vector4f(globalLightDirection, 0.0f));
        lightData.lights[0].lightIntensity = new Vector4f(0.6f, 0.6f, 0.6f, 1.0f);

        lightData.lights[1] = new PerLight();
        lightData.lights[1].cameraSpaceLightPos = worldToCamMat.transform(new Vector4f(calcLightPosition()));
        lightData.lights[1].lightIntensity = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.getAndFlip(lightBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        {
            Mesh mesh = useInfinity ? objectMesh : planeMesh;

            glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, currMaterial * materialOffset,
                    MaterialBlock.BYTES);

            modelMatrix.pushMatrix();

            modelMatrix.mul(objtPole.calcMatrix());
            modelMatrix.scale(useInfinity ? 2.0f : 4.0f);

            Matrix3f normMatrix = new Matrix3f(modelMatrix);
            normMatrix.invert().transpose();

            ProgramData prog = programs[shaderMode.ordinal()];

            glUseProgram(prog.theProgram);
            glUniformMatrix4fv(prog.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniformMatrix3fv(prog.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));

            glActiveTexture(GL_TEXTURE0 + gaussTexUnit);
            glBindTexture(GL_TEXTURE_2D, gaussTextures[currTexture]);
            glBindSampler(gaussTexUnit, textureSampler);

            glActiveTexture(GL_TEXTURE0 + shineTexUnit);
            glBindTexture(GL_TEXTURE_2D, shineTexture);
            glBindSampler(shineTexUnit, textureSampler);

            if (shaderMode != ShaderMode.FIXED) {
                mesh.render("lit-tex");
            } else {
                mesh.render("lit");
            }

            glBindSampler(gaussTexUnit, 0);
            glBindTexture(GL_TEXTURE_2D, 0);

            glUseProgram(0);
            glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);

            modelMatrix.popMatrix();
        }

        if (drawLights) {
            modelMatrix.pushMatrix();

            Vector4f tmp = calcLightPosition();
            modelMatrix.translate(new Vector3f(tmp.x, tmp.y, tmp.z));
            modelMatrix.scale(0.25f);

            glUseProgram(unlit.theProgram);
            glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

            Vector4f lightColor = new Vector4f(1.0f);
            glUniform4fv(unlit.objectColorUnif, lightColor.get(vec4Buffer));
            cubeMesh.render("flat");

            modelMatrix.popMatrix();

            modelMatrix.translate(globalLightDirection.mul(100.0f));
            modelMatrix.scale(5.0f);

            glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            cubeMesh.render("flat");

            glUseProgram(0);
        }

        if (drawCameraPos) {
            modelMatrix.pushMatrix();

            modelMatrix.identity();
            modelMatrix.translate(new Vector3f(0.0f, 0.0f, -viewPole.getView().radius));
            modelMatrix.scale(0.25f);

            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            glUseProgram(unlit.theProgram);
            glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
            cubeMesh.render("flat");
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            cubeMesh.render("flat");

            modelMatrix.popMatrix();
        }
    }

    @Override
    protected void reshape(int w, int h) {
        float zNear = 1.0f;
        float zFar = 1000.0f;
        Matrix4f persMatrix = new Matrix4f();
        persMatrix.perspective((float) Math.toRadians(45.0f), (w / (float) h), zNear, zFar);

        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix;

        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
    }

    ////////////////////////////////
    private ProgramData[] programs = new ProgramData[ShaderMode.NUM_SHADER_MODES.ordinal()];
    private ShaderPairs[] shaderPairs = new ShaderPairs[]{
            new ShaderPairs("PN.vert", "FixedShininess.frag"),
            new ShaderPairs("PNT.vert", "TextureShininess.frag"),
            new ShaderPairs("PNT.vert", "TextureCompute.frag")
    };
    private UnlitProgData unlit;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }

    private class ShaderPairs {
        String vertShaderFileName;
        String fragShaderFileName;

        ShaderPairs(String vertShader, String fragShader) {
            this.vertShaderFileName = vertShader;
            this.fragShaderFileName = fragShader;
        }
    }


    private void initializePrograms() {
        for (int progIndex = 0; progIndex < ShaderMode.NUM_SHADER_MODES.ordinal(); progIndex++) {
            programs[progIndex] = loadStandardProgram(shaderPairs[progIndex].vertShaderFileName,
                    shaderPairs[progIndex].fragShaderFileName);
        }

        unlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
    }

    private ProgramData loadStandardProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");

        int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
        int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

        glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

        int gaussianTextureUnif = glGetUniformLocation(data.theProgram, "gaussianTexture");
        int shininessTextureUnif = glGetUniformLocation(data.theProgram, "shininessTexture");
        glUseProgram(data.theProgram);
        glUniform1i(gaussianTextureUnif, gaussTexUnit);
        glUniform1i(shininessTextureUnif, shineTexUnit);
        glUseProgram(0);

        return data;
    }

    private UnlitProgData loadUnlitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        UnlitProgData data = new UnlitProgData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.objectColorUnif = glGetUniformLocation(data.theProgram, "objectColor");

        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

        return data;
    }

    ////////////////////////////////
    private Mesh objectMesh;
    private Mesh cubeMesh;
    private Mesh planeMesh;

    private final int gaussTexUnit = 0;
    private final int shineTexUnit = 1;

    private final int NUM_GAUSS_TEXTURES = 4;
    private int gaussTextures[] = new int[NUM_GAUSS_TEXTURES];
    private int currTexture = NUM_GAUSS_TEXTURES - 1;

    private int textureSampler;
    private int shineTexture;
    private int materialOffset;
    private int currMaterial;

    private final String[] shaderModeNames = {
            "Fixed Shininess with Gaussian Texture",
            "Texture Shininess with Gaussian Texture",
            "Texture Shininess with computed Gaussian"
    };

    private Timer lightTimer = new Timer(Timer.Type.LOOP, 6.0f);

    private boolean drawLights = true;
    private boolean useInfinity = true;
    private boolean drawCameraPos;

    private ShaderMode shaderMode = ShaderMode.FIXED;

    private enum ShaderMode {
        FIXED,
        TEXTURED,
        TEXTURED_COMPUTE,

        NUM_SHADER_MODES
    }


    private void createGaussianTextures() {
        for (int textureIndex = 0; textureIndex < NUM_GAUSS_TEXTURES; textureIndex++) {
            int cosAngleResolution = calcCosAngResolution(textureIndex);
            gaussTextures[textureIndex] = createGaussianTexture(cosAngleResolution, 128);
        }

        textureSampler = glGenSamplers();
        glSamplerParameteri(textureSampler, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(textureSampler, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glSamplerParameteri(textureSampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glSamplerParameteri(textureSampler, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    private int createGaussianTexture(int cosAngleResolution, int shininessResolution) {
        byte[] textureData = new byte[shininessResolution * cosAngleResolution];

        buildGaussianData(textureData, cosAngleResolution, shininessResolution);

        ByteBuffer textureDataBuffer = BufferUtils.createByteBuffer(textureData.length);
        textureDataBuffer.put(textureData);
        textureDataBuffer.flip();

        int gaussTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, gaussTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, cosAngleResolution, shininessResolution, 0, GL_RED, GL_UNSIGNED_BYTE, textureDataBuffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        return gaussTexture;
    }

    private void buildGaussianData(byte[] textureData, int cosAngleResolution, int shininessResolution) {
        int offset = 0;
        for (int shinIndex = 1; shinIndex <= shininessResolution; shinIndex++) {
            float shininess = shinIndex / (float) (shininessResolution);

            for (int cosAngIndex = 0; cosAngIndex < cosAngleResolution; cosAngIndex++) {
                float cosAng = cosAngIndex / (float) (cosAngleResolution - 1);
                float angle = (float) Math.acos(cosAng);
                float exponent = angle / shininess;
                exponent = -(exponent * exponent);
                float gaussianTerm = (float) Math.exp(exponent);

                textureData[offset] = (byte) (char) (gaussianTerm * 255.0f);
                offset++;
            }
        }
    }

    private int calcCosAngResolution(int level) {
        final int cosAngleStart = 64;
        return cosAngleStart * (int) (Math.pow(2.0f, level));
    }

    private void createShininessTexture() {
        try {
            String filePath = Framework.findFileOrThrow("main.dds");
            ImageSet imageSet = DdsLoader.loadFromFile(filePath);

            SingleImage image = imageSet.getImage(0, 0, 0);
            Dimensions dims = image.getDimensions();

            shineTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, shineTexture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, dims.width, dims.height, 0, GL_RED, GL_UNSIGNED_BYTE, image.getImageData());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private Vector4f calcLightPosition() {
        final float scale = 3.14159f * 2.0f;
        float timeThroughLoop = lightTimer.getAlpha();

        float lightHeight = 1.0f;
        Vector4f ret = new Vector4f(0.0f, lightHeight, 0.0f, 1.0f);
        float lightRadius = 3.0f;
        ret.x = (float) (Math.cos(timeThroughLoop * scale) * lightRadius);
        ret.z = (float) (Math.sin(timeThroughLoop * scale) * lightRadius);
        return ret;
    }

    ////////////////////////////////
    // View / Object setup.
    private ObjectData initialObjectData = new ObjectData(
            new Vector3f(0.0f, 0.5f, 0.0f),
            new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
    );


    private ViewData initialViewData = new ViewData(
            new Vector3f(initialObjectData.position),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
            10.0f,
            0.0f
    );

    private ViewScale viewScale = new ViewScale(
            1.5f, 70.0f,
            1.5f, 0.5f,
            0.0f, 0.0f,  // No camera movement.
            90.0f / 250.0f
    );


    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole objtPole = new ObjectPole(initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);

    ////////////////////////////////
    private final int projectionBlockIndex = 2;

    private int projectionUniformBuffer;

    ////////////////////////////////
    private final int lightBlockIndex = 1;

    private int lightUniformBuffer;

    ////////////////////////////////
    private final int NUM_MATERIALS = 2;

    private final int materialBlockIndex = 0;

    private int materialUniformBuffer;


    private void setupMaterials() {
        UniformBlockArray<MaterialBlock> ubArray = new UniformBlockArray<>(MaterialBlock.BYTES, NUM_MATERIALS);
        MaterialBlock matBlock;

        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(1.0f, 0.673f, 0.043f, 1.0f);
        matBlock.specularColor = new Vector4f(1.0f, 0.673f, 0.043f, 1.0f).mul(0.4f);
        matBlock.specularShininess = 0.125f;
        ubArray.set(0, matBlock);

        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.01f, 0.01f, 0.01f, 1.0f);
        matBlock.specularColor = new Vector4f(0.99f, 0.99f, 0.99f, 1.0f);
        matBlock.specularShininess = 0.125f;
        ubArray.set(1, matBlock);

        materialUniformBuffer = ubArray.createBufferObject();
        materialOffset = ubArray.getArrayOffset();
    }
}
