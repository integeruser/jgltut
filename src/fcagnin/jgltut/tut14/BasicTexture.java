package fcagnin.jgltut.tut14;

import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glm.*;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.*;
import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.Framework;
import fcagnin.jgltut.framework.Mesh;
import fcagnin.jgltut.framework.MousePole;
import fcagnin.jgltut.framework.Timer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

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
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part IV. Texturing
 * Chapter 14. Textures are not Pictures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2014.html
 * <p>
 * P        - toggle pausing.
 * -,=      - rewind/jump forward time by 0.5 second (of real-time).
 * T        - toggle viewing the look-at point.
 * G        - toggle the drawing of the light source.
 * SPACE    - toggle between shader-based Gaussian specular and texture-based specular.
 * 1,2,3,4  - switch to progressively larger textures.
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
 *
 * @author integeruser
 */
public class BasicTexture extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut14/data/";
        new BasicTexture().start();
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            objectMesh = new Mesh("Infinity.xml");
            cubeMesh = new Mesh("UnitCube.xml");
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

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
        MaterialBlock matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vec4(1.0f, 0.673f, 0.043f, 1.0f);
        matBlock.specularColor = new Vec4(1.0f, 0.673f, 0.043f, 1.0f).scale(0.4f);
        matBlock.specularShininess = specularShininess;

        materialUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, materialUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, matBlock.fillAndFlipBuffer(BufferUtils.createFloatBuffer(12)), GL_STATIC_DRAW);

        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE);

        glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, 0, MaterialBlock.SIZE);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        createGaussianTextures();
    }

    @Override
    protected void display() {
        lightTimer.update(getElapsedTime());

        glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStack modelMatrix = new MatrixStack();
        modelMatrix.setMatrix(viewPole.calcMatrix());
        final Mat4 worldToCamMat = modelMatrix.top();

        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
        float halfLightDistance = 25.0f;
        lightData.lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

        Vec3 globalLightDirection = new Vec3(0.707f, 0.707f, 0.0f);

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCamMat, new Vec4(globalLightDirection, 0.0f));
        lightData.lights[0].lightIntensity = new Vec4(0.6f, 0.6f, 0.6f, 1.0f);

        lightData.lights[1] = new PerLight();
        lightData.lights[1].cameraSpaceLightPos = Mat4.mul(worldToCamMat, calcLightPosition());
        lightData.lights[1].lightIntensity = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer(lightBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        {
            glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, 0, MaterialBlock.SIZE);

            modelMatrix.push();

            modelMatrix.applyMatrix(objtPole.calcMatrix());
            modelMatrix.scale(2.0f);

            Mat3 normMatrix = new Mat3(modelMatrix.top());
            normMatrix = Glm.transpose(Glm.inverse(normMatrix));

            ProgramData prog = useTexture ? litTextureProg : litShaderProg;

            glUseProgram(prog.theProgram);
            glUniformMatrix4(prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
            glUniformMatrix3(prog.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));

            glActiveTexture(GL_TEXTURE0 + gaussTexUnit);
            glBindTexture(GL_TEXTURE_1D, gaussTextures[currTexture]);
            glBindSampler(gaussTexUnit, gaussSampler);

            objectMesh.render("lit");

            glBindSampler(gaussTexUnit, 0);
            glBindTexture(GL_TEXTURE_1D, 0);

            glUseProgram(0);
            glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);

            modelMatrix.pop();
        }

        if (drawLights) {
            modelMatrix.push();

            modelMatrix.translate(new Vec3(calcLightPosition()));
            modelMatrix.scale(0.25f);

            glUseProgram(unlit.theProgram);
            glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

            Vec4 lightColor = new Vec4(1.0f);
            glUniform4(unlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
            cubeMesh.render("flat");

            modelMatrix.pop();

            modelMatrix.translate(globalLightDirection.scale(100.0f));
            modelMatrix.scale(5.0f);

            glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
            cubeMesh.render("flat");

            glUseProgram(0);
        }

        if (drawCameraPos) {
            modelMatrix.push();

            modelMatrix.setIdentity();
            modelMatrix.translate(new Vec3(0.0f, 0.0f, -viewPole.getView().radius));
            modelMatrix.scale(0.25f);

            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            glUseProgram(unlit.theProgram);
            glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
            glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
            cubeMesh.render("flat");
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            cubeMesh.render("flat");

            modelMatrix.pop();
        }
    }

    @Override
    protected void reshape(int w, int h) {
        float zNear = 1.0f;
        float zFar = 1000.0f;
        MatrixStack persMatrix = new MatrixStack();
        persMatrix.perspective(45.0f, (w / (float) h), zNear, zFar);

        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix.top();

        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        while (Mouse.next()) {
            int eventButton = Mouse.getEventButton();
            if (eventButton != -1) {
                boolean pressed = Mouse.getEventButtonState();
                MousePole.forwardMouseButton(viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
                MousePole.forwardMouseButton(objtPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
            } else {
                // Mouse moving or mouse scrolling
                int dWheel = Mouse.getDWheel();
                if (dWheel != 0) {
                    MousePole.forwardMouseWheel(viewPole, dWheel, Mouse.getX(), Mouse.getY());
                    MousePole.forwardMouseWheel(objtPole, dWheel, Mouse.getX(), Mouse.getY());
                }

                if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
                    MousePole.forwardMouseMotion(viewPole, Mouse.getX(), Mouse.getY());
                    MousePole.forwardMouseMotion(objtPole, Mouse.getX(), Mouse.getY());
                }
            }
        }


        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                switch (Keyboard.getEventKey()) {
                    case Keyboard.KEY_P:
                        lightTimer.togglePause();
                        break;

                    case Keyboard.KEY_MINUS:
                        lightTimer.rewind(0.5f);
                        break;

                    case Keyboard.KEY_EQUALS:
                        lightTimer.fastForward(0.5f);
                        break;

                    case Keyboard.KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;

                    case Keyboard.KEY_G:
                        drawLights = !drawLights;
                        break;

                    case Keyboard.KEY_SPACE:
                        useTexture = !useTexture;
                        if (useTexture) {
                            System.out.printf("Texture\n");
                        } else {
                            System.out.printf("Shader\n");
                        }
                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }


                if (Keyboard.KEY_1 <= Keyboard.getEventKey() && Keyboard.getEventKey() <= Keyboard.KEY_9) {
                    int number = Keyboard.getEventKey() - Keyboard.KEY_1;
                    if (number < NUM_GAUSS_TEXTURES) {
                        System.out.printf("Angle Resolution: %d\n", calcCosAngResolution(number));
                        currTexture = number;
                    }
                }
            }
        }
    }

    ////////////////////////////////
    private ProgramData litShaderProg;
    private ProgramData litTextureProg;
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


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(Vec4.SIZE);
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(Mat3.SIZE);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);
    private FloatBuffer lightBlockBuffer = BufferUtils.createFloatBuffer(LightBlock.SIZE);


    private void initializePrograms() {
        litShaderProg = loadStandardProgram("PN.vert", "ShaderGaussian.frag");
        litTextureProg = loadStandardProgram("PN.vert", "TextureGaussian.frag");
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
        glUseProgram(data.theProgram);
        glUniform1i(gaussianTextureUnif, gaussTexUnit);
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

    private float specularShininess = 0.2f;

    private final int gaussTexUnit = 0;

    private final int NUM_GAUSS_TEXTURES = 4;
    private int[] gaussTextures = new int[NUM_GAUSS_TEXTURES];
    private int currTexture;

    private int gaussSampler;

    private Timer lightTimer = new Timer(Timer.Type.LOOP, 6.0f);

    private boolean drawLights = true;
    private boolean drawCameraPos;
    private boolean useTexture;


    private void createGaussianTextures() {
        for (int textureIndex = 0; textureIndex < NUM_GAUSS_TEXTURES; textureIndex++) {
            int cosAngleResolution = calcCosAngResolution(textureIndex);
            gaussTextures[textureIndex] = createGaussianTexture(cosAngleResolution);
        }

        gaussSampler = glGenSamplers();
        glSamplerParameteri(gaussSampler, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(gaussSampler, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glSamplerParameteri(gaussSampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    }

    private int createGaussianTexture(int cosAngleResolution) {
        byte[] textureData = new byte[cosAngleResolution];

        buildGaussianData(textureData, cosAngleResolution);

        ByteBuffer textureDataBuffer = BufferUtils.createByteBuffer(textureData.length);
        textureDataBuffer.put(textureData);
        textureDataBuffer.flip();

        int gaussTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_1D, gaussTexture);
        glTexImage1D(GL_TEXTURE_1D, 0, GL_R8, cosAngleResolution, 0, GL_RED, GL_UNSIGNED_BYTE, textureDataBuffer);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LEVEL, 0);
        glBindTexture(GL_TEXTURE_1D, 0);

        return gaussTexture;
    }

    private void buildGaussianData(byte[] textureData, int cosAngleResolution) {
        for (int cosAngIndex = 0; cosAngIndex < cosAngleResolution; cosAngIndex++) {
            float cosAng = cosAngIndex / (float) (cosAngleResolution - 1);
            float angle = (float) Math.acos(cosAng);
            float exponent = angle / specularShininess;
            exponent = -(exponent * exponent);
            float gaussianTerm = (float) Math.exp(exponent);

            textureData[cosAngIndex] = (byte) (gaussianTerm * 255.0f);
        }
    }

    private int calcCosAngResolution(int level) {
        final int cosAngleStart = 64;
        return cosAngleStart * (int) (Math.pow(2.0f, level));
    }


    private Vec4 calcLightPosition() {
        final float scale = 3.14159f * 2.0f;
        float timeThroughLoop = lightTimer.getAlpha();

        float lightHeight = 1.0f;
        Vec4 lightPos = new Vec4(0.0f, lightHeight, 0.0f, 1.0f);
        float lightRadius = 3.0f;
        lightPos.x = (float) (Math.cos(timeThroughLoop * scale) * lightRadius);
        lightPos.z = (float) (Math.sin(timeThroughLoop * scale) * lightRadius);
        return lightPos;
    }

    ////////////////////////////////
    // View / Object setup.
    private ObjectData initialObjectData = new ObjectData(
            new Vec3(0.0f, 0.5f, 0.0f),
            new Quaternion(1.0f, 0.0f, 0.0f, 0.0f)
    );


    private ViewData initialViewData = new ViewData(
            new Vec3(initialObjectData.position),
            new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
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

    private class ProjectionBlock extends BufferableData<FloatBuffer> {
        Mat4 cameraToClipMatrix;

        static final int SIZE = Mat4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            return cameraToClipMatrix.fillBuffer(buffer);
        }
    }

    ////////////////////////////////
    private static final int NUMBER_OF_LIGHTS = 2;

    private final int lightBlockIndex = 1;

    private int lightUniformBuffer;

    class PerLight extends BufferableData<FloatBuffer> {
        Vec4 cameraSpaceLightPos;
        Vec4 lightIntensity;

        static final int SIZE = Vec4.SIZE + Vec4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            cameraSpaceLightPos.fillBuffer(buffer);
            lightIntensity.fillBuffer(buffer);
            return buffer;
        }
    }

    class LightBlock extends BufferableData<FloatBuffer> {
        Vec4 ambientIntensity;
        float lightAttenuation;
        float padding[] = new float[3];
        PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

        static final int SIZE = Vec4.SIZE + ((1 + 3) * FLOAT_SIZE) + PerLight.SIZE * NUMBER_OF_LIGHTS;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            ambientIntensity.fillBuffer(buffer);
            buffer.put(lightAttenuation);
            buffer.put(padding);
            for (PerLight light : lights) {
                light.fillBuffer(buffer);
            }
            return buffer;
        }
    }

    ////////////////////////////////
    private final int materialBlockIndex = 0;

    private int materialUniformBuffer;

    private class MaterialBlock extends BufferableData<FloatBuffer> {
        Vec4 diffuseColor;
        Vec4 specularColor;
        float specularShininess;
        float padding[] = new float[3];

        static final int SIZE = Vec4.SIZE + Vec4.SIZE + ((1 + 3) * FLOAT_SIZE);

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            diffuseColor.fillBuffer(buffer);
            specularColor.fillBuffer(buffer);
            buffer.put(specularShininess);
            buffer.put(padding);
            return buffer;
        }
    }
}