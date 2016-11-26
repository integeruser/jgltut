package integeruser.jgltut.tut12;

import integeruser.jglsdk.glutil.MousePoles.MouseButtons;
import integeruser.jglsdk.glutil.MousePoles.ViewData;
import integeruser.jglsdk.glutil.MousePoles.ViewPole;
import integeruser.jglsdk.glutil.MousePoles.ViewScale;
import integeruser.jgltut.Tutorial;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.MousePole;
import integeruser.jgltut.framework.Timer;
import org.joml.*;
import org.lwjgl.opengl.GL15;

import java.lang.Math;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2012%20Dynamic%20Range/HDR%20Lighting.cpp
 * <p>
 * Part III. Illumination
 * Chapter 12. Dynamic Range
 * <p>
 * W,A,S,D  - move the cameras forward/backwards and left/right, relative to the camera's current orientation. Holding
 * SHIFT with these  keys will move in smaller increments.
 * Q,E      - raise and lower the camera, relative to its current orientation. Holding SHIFT with these keys will move
 * in smaller increments.
 * P        - toggle pausing.
 * -,=      - rewind/jump forward time by one second (of real-time).
 * T        - toggle viewing of the current target point.
 * 1,2,3    - timer commands affect both the sun and the other lights/only the sun/only the other lights.
 * L        - switch to day-optimized lighting. Pressing SHIFT+L will switch to a night-time optimized version.
 * K        - switch to HDR lighting.
 * SPACE    - print out the current sun-based time, in 24-hour notation.
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class HDRLighting extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut12/data/";
        new HDRLighting().start(700, 700);
    }


    @Override
    protected void init() {
        initializePrograms();

        scene = new Scene() {
            @Override
            ProgramData getProgram(LightingProgramTypes lightingProgramType) {
                return programs[lightingProgramType.ordinal()];
            }
        };

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
        GL15.glBufferData(GL_UNIFORM_BUFFER, LightManager.LightBlockHDR.BYTES, GL_DYNAMIC_DRAW);

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        GL15.glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.BYTES, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightManager.LightBlockHDR.BYTES);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.BYTES);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
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
                        timerMode = LightManager.TimerTypes.ALL;
                        System.out.printf("All\n");
                        break;

                    case GLFW_KEY_2:
                        timerMode = LightManager.TimerTypes.SUN;
                        System.out.printf("Sun\n");
                        break;

                    case GLFW_KEY_3:
                        timerMode = LightManager.TimerTypes.LIGHTS;
                        System.out.printf("Lights\n");
                        break;

                    case GLFW_KEY_L:
                        if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                            setupNighttimeLighting();
                        } else {
                            setupDaytimeLighting();
                        }
                        break;

                    case GLFW_KEY_K:
                        setupHDRLighting();
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
                        glfwSetWindowShouldClose(window, true);
                        break;
                }
            }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            boolean pressed = action == GLFW_PRESS;
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
            MousePole.forwardMouseButton(window, viewPole, button, pressed, x, y);
        });
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
            }
        });
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
            MousePole.forwardMouseWheel(window, viewPole, (int) yoffset, x, y);
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
        LightManager.LightBlockHDR lightData = lights.getLightInformationHDR(worldToCamMat);

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        GL15.glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.getAndFlip(lightBlockBuffer));
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
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
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

    private Scene.ProgramData[] programs = new Scene.ProgramData[Scene.LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal()];
    private Shaders[] shaderFileNames = new Shaders[]{
            new Shaders("PCN.vert", "DiffuseSpecularHDR.frag"),
            new Shaders("PCN.vert", "DiffuseOnlyHDR.frag"),

            new Shaders("PN.vert", "DiffuseSpecularMtlHDR.frag"),
            new Shaders("PN.vert", "DiffuseOnlyMtlHDR.frag")
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


    private void initializePrograms() {
        for (int progIndex = 0; progIndex < Scene.LightingProgramTypes.MAX_LIGHTING_PROGRAM_TYPES.ordinal(); progIndex++) {
            programs[progIndex] = new Scene.ProgramData();
            programs[progIndex] = loadLitProgram(shaderFileNames[progIndex].vertexShaderFileName,
                    shaderFileNames[progIndex].fragmentShaderFileName);
        }

        unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
    }

    private Scene.ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        Scene.ProgramData data = new Scene.ProgramData();
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

    private LightManager.TimerTypes timerMode = LightManager.TimerTypes.ALL;

    private boolean drawCameraPos;


    private void setupDaytimeLighting() {
        LightManager.SunlightValue values[] = {
                new LightManager.SunlightValue(
                        0.0f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new LightManager.SunlightValue(
                        4.5f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new LightManager.SunlightValue(
                        6.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.10f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new LightManager.SunlightValue(
                        8.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new LightManager.SunlightValue(
                        18.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new LightManager.SunlightValue(
                        19.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.1f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new LightManager.SunlightValue(
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
        LightManager.SunlightValue values[] = {
                new LightManager.SunlightValue(
                        0.0f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new LightManager.SunlightValue(
                        4.5f / 24.0f,
                        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(skyDaylightColor)),
                new LightManager.SunlightValue(
                        6.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.10f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new LightManager.SunlightValue(
                        8.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new LightManager.SunlightValue(
                        18.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f)),
                new LightManager.SunlightValue(
                        19.5f / 24.0f,
                        new Vector4f(0.15f, 0.05f, 0.05f, 1.0f),
                        new Vector4f(0.3f, 0.1f, 0.1f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f)),
                new LightManager.SunlightValue(
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

    private void setupHDRLighting() {
        LightManager.SunlightValueHDR values[] = {
                new LightManager.SunlightValueHDR(
                        0.0f / 24.0f,
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vector4f(skyDaylightColor),
                        3.0f),
                new LightManager.SunlightValueHDR(
                        4.5f / 24.0f,
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vector4f(skyDaylightColor),
                        3.0f),
                new LightManager.SunlightValueHDR(
                        6.5f / 24.0f,
                        new Vector4f(0.225f, 0.075f, 0.075f, 1.0f),
                        new Vector4f(0.45f, 0.15f, 0.15f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f),
                        1.5f),
                new LightManager.SunlightValueHDR(
                        8.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        1.0f),
                new LightManager.SunlightValueHDR(
                        18.0f / 24.0f,
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
                        1.0f),
                new LightManager.SunlightValueHDR(
                        19.5f / 24.0f,
                        new Vector4f(0.225f, 0.075f, 0.075f, 1.0f),
                        new Vector4f(0.45f, 0.15f, 0.15f, 1.0f),
                        new Vector4f(0.5f, 0.1f, 0.1f, 1.0f),
                        1.5f),
                new LightManager.SunlightValueHDR(
                        20.5f / 24.0f,
                        new Vector4f(0.6f, 0.6f, 0.6f, 1.0f),
                        new Vector4f(1.8f, 1.8f, 1.8f, 1.0f),
                        new Vector4f(skyDaylightColor),
                        3.0f)
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
