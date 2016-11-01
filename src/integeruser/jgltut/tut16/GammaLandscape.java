package integeruser.jgltut.tut16;

import integeruser.jglsdk.glimg.DdsLoader;
import integeruser.jglsdk.glimg.ImageSet;
import integeruser.jglsdk.glimg.TextureGenerator;
import integeruser.jglsdk.glutil.MousePoles.MouseButtons;
import integeruser.jglsdk.glutil.MousePoles.ViewData;
import integeruser.jglsdk.glutil.MousePoles.ViewPole;
import integeruser.jglsdk.glutil.MousePoles.ViewScale;
import integeruser.jgltut.Tutorial;
import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import integeruser.jgltut.framework.MousePole;
import org.joml.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL15;

import java.lang.Math;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_BASE_LEVEL;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2016%20Gamma%20and%20Textures/Gamma%20Landscape.cpp
 * <p>
 * Part IV. Texturing
 * Chapter 16. Gamma and Textures
 * <p>
 * W,A,S,D  - move the cameras forward/backwards and left/right, relative to the camera's current orientation. Holding
 * SHIFT with these keys will move in smaller increments.
 * Q,E      - raise and lower the camera, relative to its current orientation. Holding SHIFT with these keys will move
 * in smaller increments.
 * SPACE    - toggle non-shader-based gamma correction.
 * -,=      - rewind/jump forward time by 0.5 second (of real-time).
 * T        - toggle viewing the look-at point.
 * P        - toggle pausing.
 * 1,2      - select linear mipmap filtering and anisotropic filtering (using the maximum possible anisotropy).
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class GammaLandscape extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut16/data/";
        new GammaLandscape().start(700, 700);
    }


    @Override
    protected void init() {
        try {
            lightEnv = new LightEnv("LightEnv.xml");

            initializePrograms();

            terrain = new Mesh("terrain.xml");
            sphere = new Mesh("UnitSphere.xml");
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
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE_IN_BYTES, GL_DYNAMIC_DRAW);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE_IN_BYTES);

        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        GL15.glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE_IN_BYTES, GL_STREAM_DRAW);

        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE_IN_BYTES);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        loadTextures();
        createSamplers();


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_SPACE:
                        useGammaDisplay = !useGammaDisplay;
                        break;

                    case GLFW_KEY_MINUS:
                        lightEnv.rewindTime(1.0f);
                        break;

                    case GLFW_KEY_EQUAL:
                        lightEnv.fastForwardTime(1.0f);
                        break;

                    case GLFW_KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;

                    case GLFW_KEY_P:
                        lightEnv.togglePause();
                        break;

                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
                }

                if (GLFW_KEY_1 <= key && key <= GLFW_KEY_9) {
                    int number = key - GLFW_KEY_1;
                    if (number < NUM_SAMPLERS) {
                        currSampler = number;
                    }
                }
            }
        });

        glfwSetMouseButtonCallback(window, mouseCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                boolean pressed = action == GLFW_PRESS;
                glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
                int x = (int) mouseBuffer1.get(0);
                int y = (int) mouseBuffer2.get(0);
                MousePole.forwardMouseButton(window, viewPole, button, pressed, x, y);
            }
        });
        glfwSetCursorPosCallback(window, mousePosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                    MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
                }
            }
        });
        glfwSetScrollCallback(window, mouseScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
                int x = (int) mouseBuffer1.get(0);
                int y = (int) mouseBuffer2.get(0);
                MousePole.forwardMouseWheel(window, viewPole, (int) yoffset, x, y);
            }
        });
    }

    @Override
    protected void display() {
        if (useGammaDisplay) {
            glEnable(GL_FRAMEBUFFER_SRGB);
        } else {
            glDisable(GL_FRAMEBUFFER_SRGB);
        }

        lightEnv.updateTime(elapsedTime);

        Vector4f bgColor = lightEnv.getBackgroundColor();
        glClearColor(bgColor.x, bgColor.y, bgColor.z, bgColor.w);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());

        LightBlock lightData = lightEnv.getLightBlock(viewPole.calcMatrix());

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, lightData.getAndFlip(lightBlockBuffer), GL_STREAM_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        modelMatrix.pushMatrix();
        modelMatrix.rotateX((float) Math.toRadians(-90.0f));

        glUseProgram(progStandard.theProgram);
        glUniformMatrix4fv(progStandard.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
        glUniform1i(progStandard.numberOfLightsUnif, lightEnv.getNumLights());

        glActiveTexture(GL_TEXTURE0 + colorTexUnit);
        glBindTexture(GL_TEXTURE_2D, linearTexture);
        glBindSampler(colorTexUnit, samplers[currSampler]);

        terrain.render("lit-tex");

        glBindSampler(colorTexUnit, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glUseProgram(0);

        modelMatrix.popMatrix();

        // Render the sun
        {
            modelMatrix.pushMatrix();

            Vector4f tmp = lightEnv.getSunlightDirection();
            Vector3f sunlightDir = new Vector3f(tmp.x, tmp.y, tmp.z);
            modelMatrix.translate(sunlightDir.mul(500.0f));
            modelMatrix.scale(30.0f, 30.0f, 30.0f);

            glUseProgram(progUnlit.theProgram);
            glUniformMatrix4fv(progUnlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

            Vector4f lightColor = lightEnv.getSunlightScaledIntensity();
            glUniform4fv(progUnlit.objectColorUnif, lightColor.get(vec4Buffer));
            sphere.render("flat");

            modelMatrix.popMatrix();
        }

        // Draw lights
        for (int light = 0; light < lightEnv.getNumPointLights(); light++) {
            modelMatrix.pushMatrix();

            modelMatrix.translate(lightEnv.getPointLightWorldPos(light));

            glUseProgram(progUnlit.theProgram);
            glUniformMatrix4fv(progUnlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

            Vector4f lightColor = lightEnv.getPointLightScaledIntensity(light);
            glUniform4fv(progUnlit.objectColorUnif, lightColor.get(vec4Buffer));
            sphere.render("flat");

            modelMatrix.popMatrix();
        }

        if (drawCameraPos) {
            modelMatrix.pushMatrix();

            // Draw lookat point.
            modelMatrix.identity();
            modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);

            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            glUseProgram(progUnlit.theProgram);
            glUniformMatrix4fv(progUnlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(progUnlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
            sphere.render("flat");
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            glUniform4f(progUnlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            sphere.render("flat");

            modelMatrix.popMatrix();
        }
    }

    @Override
    protected void reshape(int w, int h) {
        float zNear = 1.0f;
        float zFar = 1000.0f;
        Matrix4f persMatrix = new Matrix4f();
        persMatrix.perspective((float) Math.toRadians(60.0f), (w / (float) h), zNear, zFar);

        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix;

        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        final float scale = 20;

        if (isKeyPressed(GLFW_KEY_W)) {
            viewPole.charPress(GLFW_KEY_W, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_S)) {
            viewPole.charPress(GLFW_KEY_S, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_D)) {
            viewPole.charPress(GLFW_KEY_D, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_A)) {
            viewPole.charPress(GLFW_KEY_A, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_E)) {
            viewPole.charPress(GLFW_KEY_E, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_Q)) {
            viewPole.charPress(GLFW_KEY_Q, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
    }

    ////////////////////////////////
    private ProgramData progStandard;
    private UnlitProgData progUnlit;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;
        int numberOfLightsUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int modelToCameraMatrixUnif;
        int objectColorUnif;
    }


    private void initializePrograms() {
        progStandard = loadProgram("PNT.vert", "litTexture.frag");
        progUnlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
    }

    private ProgramData loadProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.numberOfLightsUnif = glGetUniformLocation(data.theProgram, "numberOfLights");

        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

        int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);

        int colorTextureUnif = glGetUniformLocation(data.theProgram, "diffuseColorTex");
        glUseProgram(data.theProgram);
        glUniform1i(colorTextureUnif, colorTexUnit);
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
    private Mesh terrain;
    private Mesh sphere;

    private final int lightBlockIndex = 1;
    private final int colorTexUnit = 0;

    private final int NUM_SAMPLERS = 2;
    private int[] samplers = new int[NUM_SAMPLERS];
    private int currSampler;

    private int lightUniformBuffer;
    private int linearTexture;

    private LightEnv lightEnv;

    private boolean useGammaDisplay = true;
    private boolean drawCameraPos;


    private void loadTextures() {
        try {
            String filePath = Framework.findFileOrThrow("terrain_tex.dds");
            ImageSet imageSet = DdsLoader.loadFromFile(filePath);

            linearTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, linearTexture);

            TextureGenerator.OpenGLPixelTransferParams xfer = TextureGenerator.getUploadFormatType(imageSet.getFormat(), 0);

            for (int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
                ImageSet.SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
                ImageSet.Dimensions imageDimensions = image.getDimensions();

                glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8_ALPHA8, imageDimensions.width,
                        imageDimensions.height, 0, xfer.format, xfer.type, image.getImageData());
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);

            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private void createSamplers() {
        for (int samplerIndex = 0; samplerIndex < NUM_SAMPLERS; samplerIndex++) {
            samplers[samplerIndex] = glGenSamplers();
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_S, GL_REPEAT);
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_T, GL_REPEAT);
        }

        // Linear mipmap linear
        glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        // Max anisotropic
        float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);

        glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameterf(samplers[1], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
    }

    ////////////////////////////////
    // View setup.
    private ViewData initialView = new ViewData(
            new Vector3f(-60.257084f, 10.947238f, 62.636356f),
            new Quaternionf(-0.099283f, -0.211198f, -0.020028f, -0.972817f),
            30.0f,
            0.0f
    );

    private ViewScale initialViewScale = new ViewScale(
            5.0f, 90.0f,
            2.0f, 0.5f,
            4.0f, 1.0f,
            90.0f / 250.0f
    );


    private ViewPole viewPole = new ViewPole(initialView, initialViewScale, MouseButtons.MB_LEFT_BTN);

    ////////////////////////////////
    private final int projectionBlockIndex = 0;

    private int projectionUniformBuffer;
}
