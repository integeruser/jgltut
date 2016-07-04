package org.jgltut.tut13;

import org.jglsdk.glutil.MousePoles.MouseButtons;
import org.jglsdk.glutil.MousePoles.ViewData;
import org.jglsdk.glutil.MousePoles.ViewPole;
import org.jglsdk.glutil.MousePoles.ViewScale;
import org.jgltut.Tutorial;
import org.jgltut.commons.Bufferable;
import org.jgltut.commons.LightBlock;
import org.jgltut.commons.PerLight;
import org.jgltut.commons.ProjectionBlock;
import org.jgltut.framework.*;
import org.joml.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part III. Illumination
 * Chapter 13. Lies and Impostors
 * <p>
 * W,A,S,D  - move the cameras forward/backwards and left/right, relative to the camera's current orientation. Holding
 * SHIFT with these keys will move in smaller increments.
 * Q,E      - raise and lower the camera, relative to its current orientation. Holding SHIFT with these keys will move
 * in smaller increments.
 * P        - toggle pausing on/off.
 * -,=      - rewind/jump forward time by 0.5 second (of real-time).
 * T        - toggle viewing the look-at point.
 * G        - toggle the drawing of the light source.
 * 1        - switch back and forth between actual meshes and impostor spheres (the central blue sphere).
 * 2        - switch back and forth between actual meshes and impostor spheres (the orbiting grey sphere).
 * 3        - switch back and forth between actual meshes and impostor spheres (the black marble on the left).
 * 4        - switch back and forth between actual meshes and impostor spheres (the gold sphere on the right).
 * L,J,H    - switch impostor.
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class BasicImpostor extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/org/jgltut/tut13/data/";
        new BasicImpostor().start(500, 500);
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            planeMesh = new Mesh("LargePlane.xml");
            sphereMesh = new Mesh("UnitSphere.xml");
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

        // Empty Vertex Array Object.
        imposterVAO = glGenVertexArrays();
        glBindVertexArray(imposterVAO);

        createMaterials();


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_P:
                            sphereTimer.togglePause();
                            break;

                        case GLFW_KEY_MINUS:
                            sphereTimer.rewind(0.5f);
                            break;

                        case GLFW_KEY_EQUAL:
                            sphereTimer.fastForward(0.5f);
                            break;

                        case GLFW_KEY_T:
                            drawCameraPos = !drawCameraPos;
                            break;

                        case GLFW_KEY_G:
                            drawLights = !drawLights;
                            break;

                        case GLFW_KEY_1:
                            drawImposter[0] = !drawImposter[0];
                            break;

                        case GLFW_KEY_2:
                            drawImposter[1] = !drawImposter[1];
                            break;

                        case GLFW_KEY_3:
                            drawImposter[2] = !drawImposter[2];
                            break;

                        case GLFW_KEY_4:
                            drawImposter[3] = !drawImposter[3];
                            break;

                        case GLFW_KEY_L:
                            currImpostor = Impostors.BASIC;
                            break;

                        case GLFW_KEY_J:
                            currImpostor = Impostors.PERSPECTIVE;
                            break;

                        case GLFW_KEY_H:
                            currImpostor = Impostors.DEPTH;
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
        sphereTimer.update(elapsedTime);

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

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = new Matrix4f(worldToCamMat).transform(new Vector4f(0.707f, 0.707f, 0.0f, 0.0f));
        lightData.lights[0].lightIntensity = new Vector4f(0.6f, 0.6f, 0.6f, 1.0f);

        lightData.lights[1] = new PerLight();
        lightData.lights[1].cameraSpaceLightPos = new Matrix4f(worldToCamMat).transform(calcLightPosition());
        lightData.lights[1].lightIntensity = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.getAndFlip(lightBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        {
            glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
                    MaterialNames.TERRAIN.ordinal() * materialBlockOffset, MaterialBlock.SIZE);

            Matrix3f normMatrix = new Matrix3f(modelMatrix);
            normMatrix.invert().transpose();

            glUseProgram(litMeshProg.theProgram);
            glUniformMatrix4fv(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniformMatrix3fv(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));

            planeMesh.render();

            glUseProgram(0);
            glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
        }

        drawSphere(modelMatrix, new Vector3f(0.0f, 10.0f, 0.0f), 4.0f, MaterialNames.BLUE_SHINY, drawImposter[0]);
        drawSphereOrbit(modelMatrix, new Vector3f(0.0f, 10.0f, 0.0f), new Vector3f(0.6f, 0.8f, 0.0f), 20.0f,
                sphereTimer.getAlpha(), 2.0f, MaterialNames.DULL_GREY, drawImposter[1]);
        drawSphereOrbit(modelMatrix, new Vector3f(-10.0f, 1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), 10.0f,
                sphereTimer.getAlpha(), 1.0f, MaterialNames.BLACK_SHINY, drawImposter[2]);
        drawSphereOrbit(modelMatrix, new Vector3f(10.0f, 1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), 10.0f,
                sphereTimer.getAlpha() * 2.0f, 1.0f, MaterialNames.GOLD_METAL, drawImposter[3]);

        if (drawLights) {
            modelMatrix.pushMatrix();

            Vector4f v = calcLightPosition();
            modelMatrix.translate(new Vector3f(v.x, v.y, v.z));
            modelMatrix.scale(0.5f);

            glUseProgram(unlit.theProgram);
            glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

            Vector4f lightColor = new Vector4f(1.0f);
            glUniform4fv(unlit.objectColorUnif, lightColor.get(vec4Buffer));
            cubeMesh.render("flat");

            modelMatrix.popMatrix();
        }

        if (drawCameraPos) {
            modelMatrix.pushMatrix();

            modelMatrix.identity();
            modelMatrix.translate(new Vector3f(0.0f, 0.0f, -viewPole.getView().radius));

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
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
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
    private ProgramMeshData litMeshProg;
    private ProgramImposData[] litImpProgs = new ProgramImposData[Impostors.NUM_IMPOSTORS.ordinal()];
    private String[] impShaderFileNames = new String[]{
            "BasicImpostor.vert", "BasicImpostor.frag",
            "PerspImpostor.vert", "PerspImpostor.frag",
            "DepthImpostor.vert", "DepthImpostor.frag"
    };
    private UnlitProgData unlit;

    private class ProgramMeshData {
        int theProgram;

        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }

    private class ProgramImposData {
        int theProgram;

        int sphereRadiusUnif;
        int cameraSpherePosUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }


    private void initializePrograms() {
        litMeshProg = loadLitMeshProgram("PN.vert", "Lighting.frag");

        for (int progIndex = 0; progIndex < Impostors.NUM_IMPOSTORS.ordinal(); progIndex++) {
            litImpProgs[progIndex] = new ProgramImposData();
            litImpProgs[progIndex] = loadLitImposProgram(impShaderFileNames[progIndex * 2], impShaderFileNames[progIndex * 2 + 1]);
        }

        unlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
    }

    private ProgramMeshData loadLitMeshProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramMeshData data = new ProgramMeshData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");

        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");

        int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
        int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

        glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
        glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

        return data;
    }

    private ProgramImposData loadLitImposProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramImposData data = new ProgramImposData();
        data.theProgram = Framework.createProgram(shaderList);
        data.sphereRadiusUnif = glGetUniformLocation(data.theProgram, "sphereRadius");
        data.cameraSpherePosUnif = glGetUniformLocation(data.theProgram, "cameraSpherePos");

        int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
        int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

        glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
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
    private Mesh planeMesh;
    private Mesh sphereMesh;
    private Mesh cubeMesh;

    private int imposterVAO;

    private Timer sphereTimer = new Timer(Timer.Type.LOOP, 6.0f);

    private boolean[] drawImposter = {false, false, false, false};
    private boolean drawCameraPos;
    private boolean drawLights = true;


    private Vector4f calcLightPosition() {
        final float scale = 3.14159f * 2.0f;
        float timeThroughLoop = sphereTimer.getAlpha();

        float lightHeight = 20.0f;
        Vector4f lightPos = new Vector4f(0.0f, lightHeight, 0.0f, 1.0f);
        lightPos.x = (float) (Math.cos(timeThroughLoop * scale) * 20.0f);
        lightPos.z = (float) (Math.sin(timeThroughLoop * scale) * 20.0f);
        return lightPos;
    }

    ////////////////////////////////
    // View setup.
    private ViewData initialViewData = new ViewData(
            new Vector3f(0.0f, 30.0f, 25.0f),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
            10.0f,
            0.0f
    );

    private ViewScale viewScale = new ViewScale(
            3.0f, 70.0f,
            3.5f, 1.5f,
            5.0f, 1.0f,
            90.0f / 250.0f
    );


    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);

    ////////////////////////////////
    private Impostors currImpostor = Impostors.BASIC;

    private enum Impostors {
        BASIC,
        PERSPECTIVE,
        DEPTH,

        NUM_IMPOSTORS
    }


    private void drawSphere(MatrixStackf modelMatrix, Vector3f position, float radius, MaterialNames material, boolean drawImposter) {
        glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
                material.ordinal() * materialBlockOffset, MaterialBlock.SIZE);

        if (drawImposter) {
            Vector4f cameraSpherePos = new Matrix4f(modelMatrix).transform(new Vector4f(position, 1.0f));
            glUseProgram(litImpProgs[currImpostor.ordinal()].theProgram);
            glUniform3fv(litImpProgs[currImpostor.ordinal()].cameraSpherePosUnif, cameraSpherePos.get(vec4Buffer));
            glUniform1f(litImpProgs[currImpostor.ordinal()].sphereRadiusUnif, radius);

            glBindVertexArray(imposterVAO);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 1, GL_FLOAT, false, 0, 0);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

            glBindVertexArray(0);
            glUseProgram(0);
        } else {
            modelMatrix.pushMatrix();

            modelMatrix.translate(position);
            modelMatrix.scale(radius * 2.0f);  // The unit sphere has a radius 0.5f.

            Matrix3f normMatrix = new Matrix3f(modelMatrix);
            normMatrix.invert().transpose();

            glUseProgram(litMeshProg.theProgram);
            glUniformMatrix4fv(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniformMatrix3fv(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));

            sphereMesh.render("lit");

            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
    }

    private void drawSphereOrbit(MatrixStackf modelMatrix, Vector3f orbitCenter, Vector3f orbitAxis,
                                 float orbitRadius, float orbitAlpha, float sphereRadius, MaterialNames material,
                                 boolean drawImposter) {
        modelMatrix.pushMatrix();

        modelMatrix.translate(orbitCenter);
        modelMatrix.rotate((float) Math.toRadians(360.0f * orbitAlpha), orbitAxis);

        Vector3f offsetDir = new Vector3f(orbitAxis).cross(new Vector3f(0.0f, 1.0f, 0.0f));
        if (offsetDir.length() < 0.001f) {
            offsetDir = new Vector3f(orbitAxis).cross(new Vector3f(1.0f, 0.0f, 0.0f));
        }

        offsetDir.normalize();

        modelMatrix.translate(offsetDir.mul(orbitRadius));

        drawSphere(modelMatrix, new Vector3f(0.0f), sphereRadius, material, drawImposter);

        modelMatrix.popMatrix();
    }

    ////////////////////////////////
    private final int projectionBlockIndex = 2;

    private int projectionUniformBuffer;

    ////////////////////////////////
    private final int lightBlockIndex = 1;

    private int lightUniformBuffer;

    ////////////////////////////////
    private final int materialBlockIndex = 0;

    private int materialUniformBuffer;
    private int materialBlockOffset;

    private class MaterialBlock implements Bufferable<ByteBuffer> {
        Vector4f diffuseColor;
        Vector4f specularColor;
        float specularShininess;
        float padding[] = new float[3];

        static final int SIZE = 4 * 4 + 4 * 4 + ((1 + 3) * FLOAT_SIZE);

        @Override
        public ByteBuffer get(ByteBuffer buffer) {
            buffer.putFloat(diffuseColor.x);
            buffer.putFloat(diffuseColor.y);
            buffer.putFloat(diffuseColor.z);
            buffer.putFloat(diffuseColor.w);
            buffer.putFloat(specularColor.x);
            buffer.putFloat(specularColor.y);
            buffer.putFloat(specularColor.z);
            buffer.putFloat(specularColor.w);
            buffer.putFloat(specularShininess);
            for (int i = 0; i < 3; i++) {
                buffer.putFloat(padding[i]);
            }
            return buffer;
        }
    }

    private enum MaterialNames {
        TERRAIN,
        BLUE_SHINY,
        GOLD_METAL,
        DULL_GREY,
        BLACK_SHINY,

        NUM_MATERIALS
    }


    private void createMaterials() {
        UniformBlockArray<MaterialBlock> ubArray = new UniformBlockArray<>(MaterialBlock.SIZE, MaterialNames.NUM_MATERIALS.ordinal());
        materialBlockOffset = ubArray.getArrayOffset();

        MaterialBlock matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        matBlock.specularColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        matBlock.specularShininess = 0.6f;
        ubArray.set(MaterialNames.TERRAIN.ordinal(), matBlock);

        matBlock.diffuseColor = new Vector4f(0.1f, 0.1f, 0.8f, 1.0f);
        matBlock.specularColor = new Vector4f(0.8f, 0.8f, 0.8f, 1.0f);
        matBlock.specularShininess = 0.1f;
        ubArray.set(MaterialNames.BLUE_SHINY.ordinal(), matBlock);

        matBlock.diffuseColor = new Vector4f(0.803f, 0.709f, 0.15f, 1.0f);
        matBlock.specularColor = new Vector4f(0.803f, 0.709f, 0.15f, 1.0f).mul(0.75f);
        matBlock.specularShininess = 0.18f;
        ubArray.set(MaterialNames.GOLD_METAL.ordinal(), matBlock);

        matBlock.diffuseColor = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
        matBlock.specularColor = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);
        matBlock.specularShininess = 0.8f;
        ubArray.set(MaterialNames.DULL_GREY.ordinal(), matBlock);

        matBlock.diffuseColor = new Vector4f(0.05f, 0.05f, 0.05f, 1.0f);
        matBlock.specularColor = new Vector4f(0.95f, 0.95f, 0.95f, 1.0f);
        matBlock.specularShininess = 0.3f;
        ubArray.set(MaterialNames.BLACK_SHINY.ordinal(), matBlock);

        materialUniformBuffer = ubArray.createBufferObject();
    }
}
