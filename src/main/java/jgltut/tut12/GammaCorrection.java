package jgltut.tut12;

import jgltut.LWJGLWindow;
import jgltut.framework.Framework;
import jgltut.framework.MousePole;
import jgltut.framework.Timer;
import jgltut.jglsdk.BufferableData;
import jgltut.jglsdk.glm.Mat4;
import jgltut.jglsdk.glm.Quaternion;
import jgltut.jglsdk.glm.Vec3;
import jgltut.jglsdk.glm.Vec4;
import jgltut.jglsdk.glutil.MatrixStack;
import jgltut.jglsdk.glutil.MousePoles.MouseButtons;
import jgltut.jglsdk.glutil.MousePoles.ViewData;
import jgltut.jglsdk.glutil.MousePoles.ViewPole;
import jgltut.jglsdk.glutil.MousePoles.ViewScale;
import jgltut.tut12.LightManager.LightBlockGamma;
import jgltut.tut12.LightManager.SunlightValueHDR;
import jgltut.tut12.LightManager.TimerTypes;
import jgltut.tut12.Scene.LightingProgramTypes;
import jgltut.tut12.Scene.ProgramData;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part III. Illumination
 * Chapter 12. Dynamic Range
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2012.html
 * <p>
 * W,A,S,D  - move the cameras forward/backwards and left/right, relative to the camera's current orientation. Holding
 * SHIFT with these keys will move in smaller increments.
 * Q,E      - raise and lower the camera, relative to its current orientation. Holding SHIFT with these keys will move
 * in smaller increments.
 * P        - toggle pausing.
 * -,=      - rewind/jump forward time by one second (of real-time).
 * T        - toggle viewing of the current target point.
 * 1,2,3    - timer commands affect both the sun and the other lights/only the sun/only the other lights.
 * L        - switch to hdr lighting. Pressing SHIFT+L will switch to gamma version.
 * K        - toggle gamma correction.
 * Y,H      - raise and lower the gamma value (default 2.2).
 * SPACE    - print out the current sun-based time, in 24-hour notation.
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 *
 * @author integeruser
 */
public class GammaCorrection extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut12/data/";
        new GammaCorrection().start(700, 700);
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            scene = new Scene() {
                @Override
                ProgramData getProgram(LightingProgramTypes lightingProgramType) {
                    return programs[lightingProgramType.ordinal()];
                }
            };
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        setupHDRLighting();

        lights.createTimer("tetra", Timer.Type.LOOP, 2.5f);

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
        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, LightBlockGamma.SIZE, GL_DYNAMIC_DRAW);

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlockGamma.SIZE);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_RELEASE) {
                    switch (key) {
                        case GLFW_KEY_P:
                            lights.togglePause(timerMode);
                            break;

                        case GLFW_KEY_MINUS:
                            lights.rewindTime(timerMode, 1.0f);
                            break;

                        case GLFW_KEY_EQUAL:
                            lights.fastForwardTime(timerMode, 1.0f);
                            break;

                        case GLFW_KEY_T:
                            drawCameraPos = !drawCameraPos;
                            break;

                        case GLFW_KEY_1:
                            timerMode = TimerTypes.ALL;
                            System.out.printf("All\n");
                            break;

                        case GLFW_KEY_2:
                            timerMode = TimerTypes.SUN;
                            System.out.printf("Sun\n");
                            break;

                        case GLFW_KEY_3:
                            timerMode = TimerTypes.LIGHTS;
                            System.out.printf("Lights\n");
                            break;

                        case GLFW_KEY_L:
                            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                                setupGammaLighting();
                            } else {
                                setupHDRLighting();
                            }
                            break;

                        case GLFW_KEY_K:
                            isGammaCorrect = !isGammaCorrect;
                            if (isGammaCorrect) {
                                System.out.printf("Gamma on!\n");
                            } else {
                                System.out.printf("Gamma off!\n");
                            }
                            break;

                        case GLFW_KEY_Y:
                            gammaValue += 0.1f;
                            System.out.printf("Gamma: %f\n", gammaValue);
                            break;

                        case GLFW_KEY_H:
                            gammaValue -= 0.1f;
                            if (gammaValue < 1.0f) {
                                gammaValue = 1.0f;
                            }
                            System.out.printf("Gamma: %f\n", gammaValue);
                            break;

                        case GLFW_KEY_SPACE:
                            float sunAlpha = lights.getSunTime();
                            float sunTimeHours = sunAlpha * 24.0f + 12.0f;
                            sunTimeHours = sunTimeHours > 24.0f ? sunTimeHours - 24.0f : sunTimeHours;
                            int sunHours = (int) sunTimeHours;
                            float sunTimeMinutes = (sunTimeHours - sunHours) * 60.0f;
                            int sunMinutes = (int) sunTimeMinutes;
                            System.out.printf("%02d:%02d\n", sunHours, sunMinutes);
                            break;

                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, GL_TRUE);
                            break;
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
        lights.updateTime(getElapsedTime());

        float gamma = isGammaCorrect ? gammaValue : 1.0f;

        Vec4 bkg = gammaCorrect(lights.getBackgroundColor(), gamma);

        glClearColor(bkg.x, bkg.y, bkg.z, bkg.w);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStack modelMatrix = new MatrixStack();
        modelMatrix.setMatrix(viewPole.calcMatrix());

        final Mat4 worldToCamMat = modelMatrix.top();
        LightBlockGamma lightData = lights.getLightInformationGamma(worldToCamMat);
        lightData.gamma = gamma;

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer(lightBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        {
            modelMatrix.push();

            scene.draw(modelMatrix, materialBlockIndex, lights.getTimerValue("tetra"));

            modelMatrix.pop();
        }

        {
            modelMatrix.push();

            // Render the sun
            {
                modelMatrix.push();

                Vec3 sunlightDir = new Vec3(lights.getSunlightDirection());
                modelMatrix.translate(sunlightDir.scale(500.0f));
                modelMatrix.scale(30.0f, 30.0f, 30.0f);

                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

                Vec4 lightColor = lights.getSunlightIntensity();
                glUniform4fv(unlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
                scene.getSphereMesh().render("flat");

                modelMatrix.pop();
            }

            // Render the lights
            {
                for (int light = 0; light < lights.getNumberOfPointLights(); light++) {
                    modelMatrix.push();

                    modelMatrix.translate(lights.getWorldLightPosition(light));

                    glUseProgram(unlit.theProgram);
                    glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

                    Vec4 lightColor = lights.getPointLightIntensity(light);
                    glUniform4fv(unlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
                    scene.getCubeMesh().render("flat");

                    modelMatrix.pop();
                }
            }

            if (drawCameraPos) {
                modelMatrix.push();

                modelMatrix.setIdentity();
                modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);

                glDisable(GL_DEPTH_TEST);
                glDepthMask(false);
                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
                glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
                scene.getCubeMesh().render("flat");
                glDepthMask(true);
                glEnable(GL_DEPTH_TEST);
                glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
                scene.getCubeMesh().render("flat");

                modelMatrix.pop();
            }

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
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        float lastFrameDuration = getLastFrameDuration() * 20 / 1000.0f;

        if (isKeyPressed(GLFW_KEY_W)) {
            viewPole.charPress(GLFW_KEY_W, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration);
        } else if (isKeyPressed(GLFW_KEY_S)) {
            viewPole.charPress(GLFW_KEY_S, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration);
        }

        if (isKeyPressed(GLFW_KEY_D)) {
            viewPole.charPress(GLFW_KEY_D, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration);
        } else if (isKeyPressed(GLFW_KEY_A)) {
            viewPole.charPress(GLFW_KEY_A, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration);
        }

        if (isKeyPressed(GLFW_KEY_E)) {
            viewPole.charPress(GLFW_KEY_E, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration);
        } else if (isKeyPressed(GLFW_KEY_Q)) {
            viewPole.charPress(GLFW_KEY_Q, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration);
        }
    }

    ////////////////////////////////
    private final int materialBlockIndex = 0;
    private final int lightBlockIndex = 1;

    private int lightUniformBuffer;

    private ProgramData[] programs = new ProgramData[LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal()];
    private Shaders[] shaderFileNames = new Shaders[]{
            new Shaders("PCN.vert", "DiffuseSpecularGamma.frag"),
            new Shaders("PCN.vert", "DiffuseOnlyGamma.frag"),

            new Shaders("PN.vert", "DiffuseSpecularMtlGamma.frag"),
            new Shaders("PN.vert", "DiffuseOnlyMtlGamma.frag")
    };
    private UnlitProgData unlit;

    private class Shaders {
        String vertexShaderFileName;
        String fragmentShaderFileName;

        Shaders(String vertexShaderFileName, String fragmentShaderFileName) {
            this.vertexShaderFileName = vertexShaderFileName;
            this.fragmentShaderFileName = fragmentShaderFileName;
        }
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(Vec4.SIZE);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);
    private FloatBuffer lightBlockBuffer = BufferUtils.createFloatBuffer(LightBlockGamma.SIZE);


    private void initializePrograms() {
        for (int progIndex = 0; progIndex < LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal(); progIndex++) {
            programs[progIndex] = new ProgramData();
            programs[progIndex] = loadLitProgram(shaderFileNames[progIndex].vertexShaderFileName,
                    shaderFileNames[progIndex].fragmentShaderFileName);
        }

        unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
    }

    private ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
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

        if (materialBlock != GL_INVALID_INDEX) {  // Can be optimized out.
            glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
        }
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

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
    private Scene scene;

    private LightManager lights = new LightManager();
    private final Vec4 skyDaylightColor = new Vec4(0.65f, 0.65f, 1.0f, 1.0f);

    private TimerTypes timerMode = TimerTypes.ALL;

    private boolean drawCameraPos;
    private boolean isGammaCorrect;

    private float gammaValue = 2.2f;


    private void setupHDRLighting() {
        SunlightValueHDR values[] = {
                new SunlightValueHDR(
                        0.0f / 24.0f,
                        new Vec4(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vec4(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vec4(skyDaylightColor),
                        3.0f),
                new SunlightValueHDR(
                        4.5f / 24.0f,
                        new Vec4(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vec4(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vec4(skyDaylightColor),
                        3.0f),
                new SunlightValueHDR(
                        6.5f / 24.0f,
                        new Vec4(0.225f, 0.075f, 0.075f, 1.0f),
                        new Vec4(0.45f, 0.15f, 0.15f, 1.0f),
                        new Vec4(0.5f, 0.1f, 0.1f, 1.0f),
                        1.5f),
                new SunlightValueHDR(
                        8.0f / 24.0f,
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        1.0f),
                new SunlightValueHDR(
                        18.0f / 24.0f,
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        1.0f),
                new SunlightValueHDR(
                        19.5f / 24.0f,
                        new Vec4(0.225f, 0.075f, 0.075f, 1.0f),
                        new Vec4(0.45f, 0.15f, 0.15f, 1.0f),
                        new Vec4(0.5f, 0.1f, 0.1f, 1.0f),
                        1.5f),
                new SunlightValueHDR(
                        20.5f / 24.0f,
                        new Vec4(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vec4(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vec4(skyDaylightColor),
                        3.0f),
        };

        lights.setSunlightValues(values, 7);

        lights.setPointLightIntensity(0, new Vec4(0.6f, 0.6f, 0.6f, 1.0f));
        lights.setPointLightIntensity(1, new Vec4(0.0f, 0.0f, 0.7f, 1.0f));
        lights.setPointLightIntensity(2, new Vec4(0.7f, 0.0f, 0.0f, 1.0f));
    }

    private void setupGammaLighting() {
        Vec4 sunlight = new Vec4(6.5f, 6.5f, 6.5f, 1.0f);
        Vec4 brightAmbient = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);

        SunlightValueHDR values[] = {
                new SunlightValueHDR(
                        0.0f / 24.0f,
                        brightAmbient,
                        sunlight,
                        new Vec4(0.65f, 0.65f, 1.0f, 1.0f),
                        10.0f),
                new SunlightValueHDR(
                        4.5f / 24.0f,
                        brightAmbient,
                        sunlight,
                        new Vec4(skyDaylightColor),
                        10.0f),
                new SunlightValueHDR(
                        6.5f / 24.0f,
                        new Vec4(0.01f, 0.025f, 0.025f, 1.0f),
                        new Vec4(2.5f, 0.2f, 0.2f, 1.0f),
                        new Vec4(0.5f, 0.1f, 0.1f, 1.0f),
                        5.0f),
                new SunlightValueHDR(
                        8.0f / 24.0f,
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        3.0f),
                new SunlightValueHDR(
                        18.0f / 24.0f,
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vec4(0.0f, 0.0f, 0.0f, 1.0f),
                        3.0f),
                new SunlightValueHDR(
                        19.5f / 24.0f,
                        new Vec4(0.01f, 0.025f, 0.025f, 1.0f),
                        new Vec4(2.5f, 0.2f, 0.2f, 1.0f),
                        new Vec4(0.5f, 0.1f, 0.1f, 1.0f),
                        5.0f),
                new SunlightValueHDR(
                        20.5f / 24.0f,
                        brightAmbient,
                        sunlight,
                        new Vec4(skyDaylightColor),
                        10.0f)
        };

        lights.setSunlightValues(values, 7);

        lights.setPointLightIntensity(0, new Vec4(0.6f, 0.6f, 0.6f, 1.0f));
        lights.setPointLightIntensity(1, new Vec4(0.0f, 0.0f, 0.7f, 1.0f));
        lights.setPointLightIntensity(2, new Vec4(0.7f, 0.0f, 0.0f, 1.0f));
    }


    private Vec4 gammaCorrect(Vec4 input, float gamma) {
        Vec4 inputCorrected = new Vec4();
        inputCorrected.x = (float) Math.pow(input.x, 1.0f / gamma);
        inputCorrected.y = (float) Math.pow(input.y, 1.0f / gamma);
        inputCorrected.z = (float) Math.pow(input.z, 1.0f / gamma);
        inputCorrected.w = input.w;
        return inputCorrected;
    }

    ////////////////////////////////
    // View setup.
    private ViewData initialViewData = new ViewData(
            new Vec3(-59.5f, 44.0f, 95.0f),
            new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
            50.0f,
            0.0f
    );

    private ViewScale viewScale = new ViewScale(
            3.0f, 80.0f,
            4.0f, 1.0f,
            5.0f, 1.0f,
            90.0f / 250.0f
    );


    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);

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
}