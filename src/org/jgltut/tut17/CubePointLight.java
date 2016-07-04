package org.jgltut.tut17;

import org.jglsdk.glimg.DdsLoader;
import org.jglsdk.glimg.ImageSet;
import org.jglsdk.glimg.ImageSet.Dimensions;
import org.jglsdk.glimg.ImageSet.SingleImage;
import org.jglsdk.glimg.TextureGenerator;
import org.jglsdk.glutil.MousePoles.*;
import org.jgltut.Tutorial;
import org.jgltut.commons.LightBlock;
import org.jgltut.commons.PerLight;
import org.jgltut.commons.ProjectionBlock;
import org.jgltut.framework.*;
import org.jgltut.framework.Scene.SceneNode;
import org.jgltut.framework.SceneBinders.UniformIntBinder;
import org.jgltut.framework.SceneBinders.UniformMat4Binder;
import org.jgltut.framework.SceneBinders.UniformVec3Binder;
import org.joml.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part IV. Texturing
 * Chapter 17. Spotlight on Textures
 * <p>
 * W,A,S,D  - move the camera forward/backwards and left/right, relative to the camera's current orientation. Holding
 * SHIFT with these keys will move in smaller increments.
 * Q,E      - raise and lower the camera, relative to its current orientation. Holding SHIFT with these keys will move
 * in smaller increments.
 * SPACE    - reset the projected flashlight direction.
 * T        - toggle viewing of the current target point.
 * G        - toggle all of the regular lighting on and off.
 * P        - toggle pausing.
 * 1,2      - toggle between different light textures.
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING         - rotate the orientation of the light horizontally and vertically, relative to
 * the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL  - rotate the orientation of the light horizontally or vertically only, relative
 * to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT   - spin the orientation of the light.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class CubePointLight extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/org/jgltut/tut17/data/";
        new CubePointLight().start(displayWidth, displayHeight);
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
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE_IN_BYTES, GL_STREAM_DRAW);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE_IN_BYTES);

        createSamplers();
        loadTextures();

        try {
            loadAndSetupScene();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        lightUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE_IN_BYTES, GL_STREAM_DRAW);

        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE_IN_BYTES);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_SPACE:
                            lightPole.reset();
                            break;

                        case GLFW_KEY_T:
                            drawCameraPos = !drawCameraPos;
                            break;

                        case GLFW_KEY_G:
                            showOtherLights = !showOtherLights;
                            break;

                        case GLFW_KEY_P:
                            timer.togglePause();
                            break;

                        case GLFW_KEY_1:
                        case GLFW_KEY_2:
                            currTextureIndex = key - GLFW_KEY_1;
                            System.out.printf("%s\n", texDefs[currTextureIndex].name);
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
                MousePole.forwardMouseButton(window, lightPole, button, pressed, x, y);
            }
        });
        glfwSetCursorPosCallback(window, mousePosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                    MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
                    MousePole.forwardMouseMotion(lightPole, (int) xpos, (int) ypos);
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
                MousePole.forwardMouseWheel(window, lightPole, (int) yoffset, x, y);
            }
        });
    }

    @Override
    protected void display() {
        timer.update(elapsedTime);

        glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final Matrix4f cameraMatrix = viewPole.calcMatrix();
        final Matrix4f lightView = lightPole.calcMatrix();

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
        glBindTexture(GL_TEXTURE_CUBE_MAP, lightTextures[currTextureIndex]);
        glBindSampler(lightProjTexUnit, samplers[currSampler]);

        {
            MatrixStackf lightProjStack = new MatrixStackf();
            lightProjStack.mul(new Matrix4f(lightView).invert());
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

            modelMatrix.mul(lightView);
            modelMatrix.scale(15.0f);

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
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
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
    }

    ////////////////////////////////
    private int coloredModelToCameraMatrixUnif;
    private int coloredProg;

    private int unlitProg;
    private int unlitModelToCameraMatrixUnif;
    private int unlitObjectColorUnif;


    private void loadAndSetupScene() {
        scene = new Scene("projCube_scene.xml");

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


    private ObjectData initLightData = new ObjectData(
            new Vector3f(0.0f, 0.0f, 10.0f),
            new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
    );


    private ViewPole viewPole = new ViewPole(initialView, initialViewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole lightPole = new ObjectPole(initLightData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);

    ////////////////////////////////
    private final TexDef[] texDefs = {
            new TexDef("IrregularPoint.dds", "Irregular Point Light"),
            new TexDef("Planetarium.dds", "Planetarium")
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
                lightTextures[textureIndex] = glGenTextures();

                String filePath = Framework.findFileOrThrow(texDefs[textureIndex].filename);
                ImageSet imageSet = DdsLoader.loadFromFile(filePath);

                glBindTexture(GL_TEXTURE_CUBE_MAP, lightTextures[textureIndex]);
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BASE_LEVEL, 0);
                glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LEVEL, 0);

                Dimensions imageDimensions = imageSet.getDimensions();
                int imageFormat = TextureGenerator.getInternalFormat(imageSet.getFormat(), 0);

                for (int face = 0; face < 6; ++face) {
                    SingleImage singleImage = imageSet.getImage(0, 0, face);

                    glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, imageFormat,
                            imageDimensions.width, imageDimensions.height, 0, singleImage.getImageData());
                }

                glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////
    private final int NUM_SAMPLERS = 1;

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
        glSamplerParameteri(samplers[0], GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
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
