package org.jgltut.tut12;

import org.jgltut.LWJGLWindow;
import org.jgltut.commons.ProjectionBlock;
import org.jgltut.framework.Framework;
import org.jgltut.framework.MousePole;
import org.jgltut.framework.Timer;
import org.jgltut.Bufferable;
import org.jglsdk.glutil.MousePoles.MouseButtons;
import org.jglsdk.glutil.MousePoles.ViewData;
import org.jglsdk.glutil.MousePoles.ViewPole;
import org.jglsdk.glutil.MousePoles.ViewScale;
import org.jgltut.tut12.LightManager.LightBlock;
import org.jgltut.tut12.LightManager.SunlightValue;
import org.jgltut.tut12.LightManager.TimerTypes;
import org.jgltut.tut12.Scene.LightingProgramTypes;
import org.jgltut.tut12.Scene.ProgramData;
import org.joml.*;
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
 * <p>
 * W,A,S,D  - move the cameras forward/backwards and left/right, relative to the camera's current orientation.
 * Holding SHIFT with these
 * keys will move in smaller increments.
 * Q,E      - raise and lower the camera, relative to its current orientation. Holding SHIFT with these keys will move
 * in smaller increments.
 * P        - toggle pausing.
 * -,=      - rewind/jump forward time by one second (of real-time).
 * T        - toggle viewing of the current target point.
 * 1,2,3    - timer commands affect both the sun and the other lights/only the sun/only the other lights.
 * L        - switch to day-optimized lighting. Pressing SHIFT+L will switch to a night-time optimized version.
 * SPACE    - print out the current sun-based time, in 24-hour notation.
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class SceneLighting extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/org/jgltut/tut12/data/";
        new SceneLighting().start(700, 700);
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

        setupDaytimeLighting();

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
        glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
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
                                setupNighttimeLighting();
                            } else {
                                setupDaytimeLighting();
                            }
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
        lights.updateTime(elapsedTime);

        Vector4f bkg = lights.getBackgroundColor();
        glClearColor(bkg.x, bkg.y, bkg.z, bkg.w);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());

        final Matrix4f worldToCamMat = modelMatrix;
        LightBlock lightData = lights.getLightInformation(worldToCamMat);

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.getAndFlip(lightBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        {
            modelMatrix.pushMatrix();

            scene.draw(modelMatrix, materialBlockIndex, lights.getTimerValue("tetra"));

            modelMatrix.popMatrix();
        }

        {
            modelMatrix.pushMatrix();

            // Render the sun
            {
                modelMatrix.pushMatrix();

                Vector4f temp = lights.getSunlightDirection();
                Vector3f sunlightDir = new Vector3f(temp.x, temp.y, temp.z);
                modelMatrix.translate(sunlightDir.mul(500.0f));
                modelMatrix.scale(30.0f, 30.0f, 30.0f);

                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

                Vector4f lightColor = lights.getSunlightIntensity();
                glUniform4fv(unlit.objectColorUnif, lightColor.get(vec4Buffer));
                scene.getSphereMesh().render("flat");

                modelMatrix.popMatrix();
            }

            // Render the lights
            {
                for (int light = 0; light < lights.getNumberOfPointLights(); light++) {
                    modelMatrix.pushMatrix();

                    modelMatrix.translate(lights.getWorldLightPosition(light));

                    glUseProgram(unlit.theProgram);
                    glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

                    Vector4f lightColor = lights.getPointLightIntensity(light);
                    glUniform4fv(unlit.objectColorUnif, lightColor.get(vec4Buffer));
                    scene.getCubeMesh().render("flat");

                    modelMatrix.popMatrix();
                }
            }

            if (drawCameraPos) {
                modelMatrix.pushMatrix();

                modelMatrix.identity();
                modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);

                glDisable(GL_DEPTH_TEST);
                glDepthMask(false);
                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
                scene.getCubeMesh().render("flat");
                glDepthMask(true);
                glEnable(GL_DEPTH_TEST);
                glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
                scene.getCubeMesh().render("flat");

                modelMatrix.popMatrix();
            }

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
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        final float scale = 20;

        if (isKeyPressed(GLFW_KEY_W)) {
            viewPole.charPress(GLFW_KEY_W, isKeyPressed(GLFW_KEY_LEFT_SHIFT)
                    || isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_S)) {
            viewPole.charPress(GLFW_KEY_S, isKeyPressed(GLFW_KEY_LEFT_SHIFT)
                    || isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_D)) {
            viewPole.charPress(GLFW_KEY_D, isKeyPressed(GLFW_KEY_LEFT_SHIFT)
                    || isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_A)) {
            viewPole.charPress(GLFW_KEY_A, isKeyPressed(GLFW_KEY_LEFT_SHIFT)
                    || isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_E)) {
            viewPole.charPress(GLFW_KEY_E, isKeyPressed(GLFW_KEY_LEFT_SHIFT)
                    || isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_Q)) {
            viewPole.charPress(GLFW_KEY_Q, isKeyPressed(GLFW_KEY_LEFT_SHIFT)
                    || isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
    }

    ////////////////////////////////
    private final int materialBlockIndex = 0;
    private final int lightBlockIndex = 1;

    private int lightUniformBuffer;

    private ProgramData[] programs = new ProgramData[LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal()];
    private Shaders[] shaderFileNames = new Shaders[]{
            new Shaders("PCN.vert", "DiffuseSpecular.frag"),
            new Shaders("PCN.vert", "DiffuseOnly.frag"),

            new Shaders("PN.vert", "DiffuseSpecularMtl.frag"),
            new Shaders("PN.vert", "DiffuseOnlyMtl.frag")
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


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    private FloatBuffer projBuffer = BufferUtils.createFloatBuffer(ProjectionBlock.SIZE);
    private FloatBuffer lightBlockBuffer = BufferUtils.createFloatBuffer(LightBlock.SIZE);


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
    private final Vector4f skyDaylightColor = new Vector4f(0.65f, 0.65f, 1.0f, 1.0f);

    private TimerTypes timerMode = TimerTypes.ALL;

    private boolean drawCameraPos;


    private void setupDaytimeLighting() {
        SunlightValue values[] = {
                new SunlightValue(
                        0.0f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new SunlightValue(
                        4.5f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new SunlightValue(
                        6.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.10f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new SunlightValue(
                        8.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new SunlightValue(
                        18.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new SunlightValue(
                        19.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.1f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new SunlightValue(
                        20.5f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor))
        };

        lights.setSunlightValues(values, 7);

        lights.setPointLightIntensity(0, new Vector4f(0.2f, 0.2f, 0.2f, 1.0f));
        lights.setPointLightIntensity(1, new Vector4f(0.0f, 0.0f, 0.3f, 1.0f));
        lights.setPointLightIntensity(2, new Vector4f(0.3f, 0.0f, 0.0f, 1.0f));
    }

    private void setupNighttimeLighting() {
        SunlightValue values[] = {
                new SunlightValue(
                        0.0f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new SunlightValue(
                        4.5f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new SunlightValue(
                        6.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.10f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new SunlightValue(
                        8.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new SunlightValue(
                        18.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new SunlightValue(
                        19.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.1f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new SunlightValue(
                        20.5f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor))
        };

        lights.setSunlightValues(values, 7);

        lights.setPointLightIntensity(0, new Vector4f(0.6f, 0.6f, 0.6f, 1.0f));
        lights.setPointLightIntensity(1, new Vector4f(0.0f, 0.0f, 0.7f, 1.0f));
        lights.setPointLightIntensity(2, new Vector4f(0.7f, 0.0f, 0.0f, 1.0f));
    }

    ////////////////////////////////
    // View setup.
    private ViewData initialViewData = new ViewData(
            new Vector3f(-59.5f, 44.0f, 95.0f),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
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
}
