package integeruser.jgltut.tut17;

import integeruser.jglsdk.glimg.DdsLoader;
import integeruser.jglsdk.glimg.ImageSet;
import integeruser.jglsdk.glimg.TextureGenerator;
import integeruser.jglsdk.glutil.MousePoles.MouseButtons;
import integeruser.jglsdk.glutil.MousePoles.ViewData;
import integeruser.jglsdk.glutil.MousePoles.ViewPole;
import integeruser.jglsdk.glutil.MousePoles.ViewScale;
import integeruser.jgltut.Tutorial;
import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.PerLight;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.framework.*;
import integeruser.jgltut.framework.Scene.SceneNode;
import integeruser.jgltut.framework.SceneBinders.UniformIntBinder;
import integeruser.jgltut.framework.SceneBinders.UniformMat4Binder;
import integeruser.jgltut.framework.SceneBinders.UniformVec3Binder;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.lang.Math;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2017%20Spotlight%20on%20Textures/Projected%20Light.cpp
 * <p>
 * Part IV. Texturing
 * Chapter 17. Spotlight on Textures
 * <p>
 * W,A,S,D  - move the camera forward/backwards and left/right, relative to the camera's current orientation. Holding
 * SHIFT with these keys will move in smaller increments.
 * I,J,K,L  - control the projected flashlight's position. Holding SHIFT with these keys will move in smaller
 * increments.
 * O,U       raise and lower the projected flashlight, relative to its current orientation. Holding SHIFT with these
 * keys will move in smaller increments.
 * SPACE    - reset the projected flashlight direction.
 * T        - toggle viewing of the current target point.
 * G        - toggle all of the regular lighting on and off.
 * H        - toggle between the edge clamping sampler and the border clamping one.
 * P        - toggle pausing.
 * Y        - increase the FOV.
 * N        - decrease the FOV.
 * 1,2,3    - toggle between different light textures.
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING         - rotate the projected flashlight horizontally and vertically, relative to the
 * current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL  - rotate the projected flashlight horizontally or vertically only, relative to
 * the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT   - spin the projected flashlight.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class ProjectedLight extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut17/data/";
        new ProjectedLight().start(displayWidth, displayHeight);
    }


    @Override
    protected void init() {
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
        glEnable(GL_FRAMEBUFFER_SRGB);

        // Setup our Uniform Buffers
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.BYTES, GL_STREAM_DRAW);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.BYTES);

        createSamplers();
        loadTextures();

        loadAndSetupScene();

        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, LightBlock.BYTES, GL_STREAM_DRAW);

        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.BYTES);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_SPACE:
                        lightViewPole.reset();
                        break;

                    case GLFW_KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;

                    case GLFW_KEY_G:
                        showOtherLights = !showOtherLights;
                        break;

                    case GLFW_KEY_H:
                        currSampler = (currSampler + 1) % NUM_SAMPLERS;
                        break;

                    case GLFW_KEY_P:
                        timer.togglePause();
                        break;

                    case GLFW_KEY_Y:
                        currFOVIndex = Math.min(currFOVIndex + 1, lightFOVs.length - 1);
                        System.out.printf("Curr FOV: %f\n", lightFOVs[currFOVIndex]);
                        break;

                    case GLFW_KEY_N:
                        currFOVIndex = Math.max(currFOVIndex - 1, 0);
                        System.out.printf("Curr FOV: %f\n", lightFOVs[currFOVIndex]);
                        break;

                    case GLFW_KEY_1:
                    case GLFW_KEY_2:
                    case GLFW_KEY_3:
                        currTextureIndex = key - GLFW_KEY_1;
                        System.out.printf("%s\n", texDefs[currTextureIndex].name);
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
            MousePole.forwardMouseButton(window, lightViewPole, button, pressed, x, y);
        });
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
                MousePole.forwardMouseMotion(lightViewPole, (int) xpos, (int) ypos);
            }
        });
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            glfwGetCursorPos(window, mouseBuffer1, mouseBuffer2);
            int x = (int) mouseBuffer1.get(0);
            int y = (int) mouseBuffer2.get(0);
            MousePole.forwardMouseWheel(window, viewPole, (int) yoffset, x, y);
            MousePole.forwardMouseWheel(window, lightViewPole, (int) yoffset, x, y);
        });
    }

    @Override
    protected void display() {
        timer.update(elapsedTime);

        glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final Matrix4f cameraMatrix = viewPole.calcMatrix();
        final Matrix4f lightView = lightViewPole.calcMatrix();

        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(cameraMatrix);

        buildLights(cameraMatrix);

        float angle0 = (float) Math.toRadians(360.0f * timer.getAlpha());
        Quaternionf rotation0 = new Quaternionf().rotateAxis(angle0, 0.0f, 1.0f, 0.0f);
        nodes.get(0).nodeSetOrient(rotation0);

        float angle3 = (float) Math.toRadians(360.0f * timer.getAlpha());
        Quaternionf rotation3 = new Quaternionf().rotateAxis(angle3, 0.0f, 0.0f, 1.0f);
        nodes.get(3).nodeSetOrient(new Quaternionf(spinBarOrient).mul(rotation3));

        {
            final float zNear = 1.0f;
            final float zFar = 1000.0f;
            MatrixStackf persMatrix = new MatrixStackf();
            persMatrix.perspective((float) Math.toRadians(60.0f), displayWidth / (float) displayHeight, zNear, zFar);

            ProjectionBlock projData = new ProjectionBlock();
            projData.cameraToClipMatrix = persMatrix;

            glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
            glBufferData(GL_UNIFORM_BUFFER, projData.getAndFlip(projectionBlockBuffer), GL_STREAM_DRAW);
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        glActiveTexture(GL_TEXTURE0 + lightProjTexUnit);
        glBindTexture(GL_TEXTURE_2D, lightTextures[currTextureIndex]);
        glBindSampler(lightProjTexUnit, samplers[currSampler]);

        {
            MatrixStackf lightProjStack = new MatrixStackf();
            // Texture-space transform
            lightProjStack.translate(0.5f, 0.5f, 0.0f);
            lightProjStack.scale(0.5f, 0.5f, 1.0f);

            // Project. Z-range is irrelevant.
            lightProjStack.perspective((float) Math.toRadians(lightFOVs[currFOVIndex]), 1.0f, 1.0f, 100.0f);

            // Transform from main camera space to light camera space.
            lightProjStack.mul(lightView);
            lightProjStack.mul(new Matrix4f(cameraMatrix).invert());
            lightProjMatBinder.setValue(lightProjStack);

            Vector4f worldLightPos = new Vector4f(lightView.m30(), lightView.m31(), lightView.m32(), lightView.m33());
            Vector4f tmp = cameraMatrix.transform(new Vector4f(worldLightPos));
            Vector3f lightPos = new Vector3f(tmp.x, tmp.y, tmp.z);
            camLightPosBinder.setValue(lightPos);
        }

        glViewport(0, 0, displayWidth, displayHeight);
        scene.render(modelMatrix);

        {
            // Draw axes
            modelMatrix.pushMatrix();

            modelMatrix.mul(new Matrix4f(lightView).invert());
            modelMatrix.scale(15.0f);
            modelMatrix.scale(1.0f, 1.0f, -1.0f);     // Invert the Z-axis so that it points in the right direction.

            glUseProgram(coloredProg);
            glUniformMatrix4fv(coloredModelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            axesMesh.render();

            modelMatrix.popMatrix();
        }

        if (drawCameraPos) {
            modelMatrix.pushMatrix();

            // Draw lookat point.
            modelMatrix.identity();
            modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);
            modelMatrix.scale(0.5f);

            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            glUseProgram(unlitProg);
            glUniformMatrix4fv(unlitModelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(unlitObjectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
            sphereMesh.render("flat");
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            glUniform4f(unlitObjectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            sphereMesh.render("flat");

            modelMatrix.popMatrix();
        }

        glActiveTexture(GL_TEXTURE0 + lightProjTexUnit);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindSampler(lightProjTexUnit, 0);
    }

    @Override
    protected void reshape(int w, int h) {
        displayWidth = w;
        displayHeight = h;
    }

    @Override
    protected void update() {
        final float scale = 10;

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


        if (isKeyPressed(GLFW_KEY_I)) {
            lightViewPole.charPress(GLFW_KEY_I, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_K)) {
            lightViewPole.charPress(GLFW_KEY_K, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_L)) {
            lightViewPole.charPress(GLFW_KEY_L, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_J)) {
            lightViewPole.charPress(GLFW_KEY_J, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_O)) {
            lightViewPole.charPress(GLFW_KEY_O, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_U)) {
            lightViewPole.charPress(GLFW_KEY_U, isKeyPressed(GLFW_KEY_LEFT_SHIFT) ||
                    isKeyPressed(GLFW_KEY_RIGHT_SHIFT), lastFrameDuration * scale);
        }
    }

    ////////////////////////////////
    private int coloredModelToCameraMatrixUnif;
    private int coloredProg;

    private int unlitProg;
    private int unlitModelToCameraMatrixUnif;
    private int unlitObjectColorUnif;


    private void loadAndSetupScene() {
        scene = new Scene("proj2d_scene.xml");

        nodes = new ArrayList<>();
        nodes.add(scene.findNode("cube"));
        nodes.add(scene.findNode("rightBar"));
        nodes.add(scene.findNode("leaningBar"));
        nodes.add(scene.findNode("spinBar"));
        nodes.add(scene.findNode("diorama"));
        nodes.add(scene.findNode("floor"));

        lightNumBinder = new UniformIntBinder();
        SceneBinders.associateUniformWithNodes(nodes, lightNumBinder, "numberOfLights");
        SceneBinders.setStateBinderWithNodes(nodes, lightNumBinder);

        lightProjMatBinder = new UniformMat4Binder();
        SceneBinders.associateUniformWithNodes(nodes, lightProjMatBinder, "cameraToLightProjMatrix");
        SceneBinders.setStateBinderWithNodes(nodes, lightProjMatBinder);

        camLightPosBinder = new UniformVec3Binder();
        SceneBinders.associateUniformWithNodes(nodes, camLightPosBinder, "cameraSpaceProjLightPos");
        SceneBinders.setStateBinderWithNodes(nodes, camLightPosBinder);

        int unlit = scene.findProgram("p_unlit");
        sphereMesh = scene.findMesh("m_sphere");

        int colored = scene.findProgram("p_colored");
        axesMesh = scene.findMesh("m_axes");

        // No more things that can throw.
        spinBarOrient = nodes.get(3).nodeGetOrient();
        unlitProg = unlit;
        unlitModelToCameraMatrixUnif = glGetUniformLocation(unlit, "modelToCameraMatrix");
        unlitObjectColorUnif = glGetUniformLocation(unlit, "objectColor");

        coloredProg = colored;
        coloredModelToCameraMatrixUnif = glGetUniformLocation(colored, "modelToCameraMatrix");
    }

    ////////////////////////////////
    private static int displayWidth = 500;
    private static int displayHeight = 500;

    private Scene scene;
    private ArrayList<SceneNode> nodes;
    private Mesh sphereMesh, axesMesh;

    private UniformMat4Binder lightProjMatBinder;
    private UniformVec3Binder camLightPosBinder;

    private float[] lightFOVs = {
            10.0f, 20.0f, 45.0f, 75.0f,
            90.0f, 120.0f, 150.0f, 170.0f
    };
    private int currFOVIndex = 3;

    private Timer timer = new Timer(Timer.Type.LOOP, 10.0f);

    private Quaternionf spinBarOrient;

    private boolean showOtherLights = true;
    private boolean drawCameraPos;

    ////////////////////////////////
    // View setup.
    private ViewData initialView = new ViewData(
            new Vector3f(0.0f, 0.0f, 10.0f),
            new Quaternionf(0.16043f, -0.376867f, -0.0664516f, 0.909845f),
            25.0f,
            0.0f
    );

    private ViewScale initialViewScale = new ViewScale(
            5.0f, 70.0f,
            2.0f, 0.5f,
            2.0f, 0.5f,
            90.0f / 250.0f
    );


    private ViewData initLightView = new ViewData(
            new Vector3f(0.0f, 0.0f, 20.0f),
            new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f),
            5.0f,
            0.0f
    );

    private ViewScale initLightViewScale = new ViewScale(
            0.05f, 10.0f,
            0.1f, 0.05f,
            4.0f, 1.0f,
            90.0f / 250.0f
    );


    private ViewPole viewPole = new ViewPole(initialView, initialViewScale, MouseButtons.MB_LEFT_BTN);
    private ViewPole lightViewPole = new ViewPole(initLightView, initLightViewScale, MouseButtons.MB_RIGHT_BTN, true);

    ////////////////////////////////
    private final TexDef[] texDefs = {
            new TexDef("Flashlight.dds", "Flashlight"),
            new TexDef("PointsOfLight.dds", "Multiple Point Lights"),
            new TexDef("Bands.dds", "Light Bands")
    };
    private final int NUM_LIGHT_TEXTURES = texDefs.length;
    private int[] lightTextures = new int[texDefs.length];
    private int currTextureIndex = 0;

    private class TexDef {
        String filename;
        String name;

        TexDef(String filename, String name) {
            this.filename = filename;
            this.name = name;
        }
    }


    private void loadTextures() {
        try {
            for (int textureIndex = 0; textureIndex < NUM_LIGHT_TEXTURES; textureIndex++) {
                String filePath = Framework.findFileOrThrow(texDefs[textureIndex].filename);
                ImageSet imageSet = DdsLoader.loadFromFile(filePath);
                lightTextures[textureIndex] = TextureGenerator.createTexture(imageSet, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////
    private final int NUM_SAMPLERS = 2;

    private int[] samplers = new int[NUM_SAMPLERS];
    private int currSampler = 0;


    private void createSamplers() {
        for (int samplerIndex = 0; samplerIndex < NUM_SAMPLERS; samplerIndex++) {
            samplers[samplerIndex] = glGenSamplers();
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        }

        glSamplerParameteri(samplers[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glSamplerParameteri(samplers[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glSamplerParameteri(samplers[1], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glSamplerParameteri(samplers[1], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);


        float[] color = {0.0f, 0.0f, 0.0f, 1.0f};
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(4);

        for (float f : color) {
            colorBuffer.put(f);
        }

        colorBuffer.flip();

        glSamplerParameterfv(samplers[1], GL_TEXTURE_BORDER_COLOR, colorBuffer);
    }

    ////////////////////////////////
    private final int projectionBlockIndex = 0;

    private int projectionUniformBuffer;

    ////////////////////////////////
    private final int lightBlockIndex = 1;
    private final int lightProjTexUnit = 3;

    private int lightUniformBuffer;
    private UniformIntBinder lightNumBinder;


    private void buildLights(Matrix4f camMatrix) {
        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
        lightData.lightAttenuation = 1.0f / (30.0f * 30.0f);
        lightData.maxIntensity = 2.0f;

        lightData.lights[0] = new PerLight();
        lightData.lights[0].lightIntensity = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
        lightData.lights[0].cameraSpaceLightPos = camMatrix.transform(new Vector4f(-0.2f, 0.5f, 0.5f, 0.0f).normalize());

        lightData.lights[1] = new PerLight();
        lightData.lights[1].lightIntensity = new Vector4f(3.5f, 6.5f, 3.0f, 1.0f).mul(0.5f);
        lightData.lights[1].cameraSpaceLightPos = camMatrix.transform(new Vector4f(5.0f, 6.0f, 0.5f, 1.0f));

        if (showOtherLights) {
            lightNumBinder.setValue(2);
        } else {
            lightNumBinder.setValue(0);
        }

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, lightData.getAndFlip(lightBlockBuffer), GL_STREAM_DRAW);
    }
}
