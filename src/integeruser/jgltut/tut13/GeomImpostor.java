package integeruser.jgltut.tut13;

import integeruser.jglsdk.glutil.MousePoles.MouseButtons;
import integeruser.jglsdk.glutil.MousePoles.ViewData;
import integeruser.jglsdk.glutil.MousePoles.ViewPole;
import integeruser.jglsdk.glutil.MousePoles.ViewScale;
import integeruser.jgltut.Tutorial;
import integeruser.jgltut.commons.*;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import integeruser.jgltut.framework.MousePole;
import integeruser.jgltut.framework.Timer;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL15;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2013%20Impostors/GeomImpostor.cpp
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
 * <p>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 */
public class GeomImpostor extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut13/data/";
        new GeomImpostor().start(500, 500);
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            planeMesh = new Mesh("LargePlane.xml");
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
        GL15.glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE_IN_BYTES, GL_DYNAMIC_DRAW);

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE_IN_BYTES, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE_IN_BYTES);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE_IN_BYTES);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        imposterVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, imposterVBO);
        glBufferData(GL_ARRAY_BUFFER, NUMBER_OF_SPHERES * 4 * Float.BYTES, GL_STREAM_DRAW);

        imposterVAO = glGenVertexArrays();
        glBindVertexArray(imposterVAO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 16, 12);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnable(GL_PROGRAM_POINT_SIZE);

        createMaterials();


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
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

                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
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
        lightData.lights[0].cameraSpaceLightPos = worldToCamMat.transform(new Vector4f(0.707f, 0.707f, 0.0f, 0.0f));
        lightData.lights[0].lightIntensity = new Vector4f(0.6f, 0.6f, 0.6f, 1.0f);

        lightData.lights[1] = new PerLight();
        lightData.lights[1].cameraSpaceLightPos = worldToCamMat.transform(new Vector4f(calcLightPosition()));
        lightData.lights[1].lightIntensity = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);

        glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.getAndFlip(lightBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        {
            glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialTerrainUniformBuffer, 0, MaterialBlock.SIZE_IN_BYTES);

            Matrix3f normMatrix = new Matrix3f(modelMatrix);
            normMatrix.invert().transpose();

            glUseProgram(litMeshProg.theProgram);
            glUniformMatrix4fv(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniformMatrix3fv(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));

            planeMesh.render();

            glUseProgram(0);
            glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
        }

        {
            VertexData[] posSizeArray = new VertexData[NUMBER_OF_SPHERES];

            posSizeArray[0] = new VertexData();
            Vector4f tmp = worldToCamMat.transform(new Vector4f(0.0f, 10.0f, 0.0f, 1.0f));
            posSizeArray[0].cameraPosition = new Vector3f(tmp.x, tmp.y, tmp.z);
            posSizeArray[0].sphereRadius = 4.0f;

            posSizeArray[1] = new VertexData();
            posSizeArray[1].cameraPosition = getSphereOrbitPos(modelMatrix, new Vector3f(0.0f, 10.0f, 0.0f),
                    new Vector3f(0.6f, 0.8f, 0.0f), 20.0f, sphereTimer.getAlpha());
            posSizeArray[1].sphereRadius = 2.0f;

            posSizeArray[2] = new VertexData();
            posSizeArray[2].cameraPosition = getSphereOrbitPos(modelMatrix, new Vector3f(-10.0f, 1.0f, 0.0f),
                    new Vector3f(0.0f, 1.0f, 0.0f), 10.0f, sphereTimer.getAlpha());
            posSizeArray[2].sphereRadius = 1.0f;

            posSizeArray[3] = new VertexData();
            posSizeArray[3].cameraPosition = getSphereOrbitPos(modelMatrix, new Vector3f(10.0f, 1.0f, 0.0f),
                    new Vector3f(0.0f, 1.0f, 0.0f), 10.0f, sphereTimer.getAlpha() * 2.0f);
            posSizeArray[3].sphereRadius = 1.0f;

            glBindBuffer(GL_ARRAY_BUFFER, imposterVBO);

            {
                ByteBuffer vertexDataBuffer = BufferUtils.createByteBuffer(NUMBER_OF_SPHERES * VertexData.SIZE);

                for (VertexData vertexData : posSizeArray) {
                    vertexData.get(vertexDataBuffer);
                }

                vertexDataBuffer.flip();

                glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STREAM_DRAW);
            }

            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        {
            glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialArrayUniformBuffer, 0, MaterialBlock.SIZE_IN_BYTES * NUMBER_OF_SPHERES);

            glUseProgram(litImpProg.theProgram);
            glBindVertexArray(imposterVAO);
            glDrawArrays(GL_POINTS, 0, NUMBER_OF_SPHERES);
            glBindVertexArray(0);
            glUseProgram(0);

            glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
        }

        if (drawLights) {
            modelMatrix.pushMatrix();

            Vector4f tmp = calcLightPosition();
            modelMatrix.translate(new Vector3f(tmp.x, tmp.y, tmp.z));
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
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
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
    private ProgramImposData litImpProg;
    private UnlitProgData unlit;

    private class ProgramMeshData {
        int theProgram;

        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }

    private class ProgramImposData {
        int theProgram;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }


    private void initializePrograms() {
        litMeshProg = loadLitMeshProgram("PN.vert", "Lighting.frag");
        litImpProg = loadLitImposProgram("GeomImpostor.vert", "GeomImpostor.geom", "GeomImpostor.frag");
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

    private ProgramImposData loadLitImposProgram(String vertexShaderFileName, String geometryShaderFileName,
                                                 String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_GEOMETRY_SHADER, geometryShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramImposData data = new ProgramImposData();
        data.theProgram = Framework.createProgram(shaderList);

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
    private Mesh cubeMesh;

    private int imposterVAO;
    private int imposterVBO;

    private final int NUMBER_OF_SPHERES = 4;

    private Timer sphereTimer = new Timer(Timer.Type.LOOP, 6.0f);

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


    private Vector3f getSphereOrbitPos(MatrixStackf modelMatrix, Vector3f orbitCenter, Vector3f orbitAxis, float orbitRadius, float orbitAlpha) {
        modelMatrix.pushMatrix();

        modelMatrix.translate(orbitCenter);
        modelMatrix.rotate((float) Math.toRadians(360.0f * orbitAlpha), orbitAxis);

        Vector3f offsetDir = new Vector3f(orbitAxis).cross(new Vector3f(0.0f, 1.0f, 0.0f));
        if (offsetDir.length() < 0.001f) {
            offsetDir = new Vector3f(orbitAxis).cross(new Vector3f(1.0f, 0.0f, 0.0f));
        }

        offsetDir.normalize();

        modelMatrix.translate(offsetDir.mul(orbitRadius));

        Vector4f tmp = modelMatrix.transform(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        Vector3f res = new Vector3f(tmp.x, tmp.y, tmp.z);
        modelMatrix.popMatrix();
        return res;
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
    private class VertexData implements Bufferable {
        Vector3f cameraPosition;
        float sphereRadius;

        static final int SIZE = 3 * 4 + (1 * Float.BYTES);

        @Override
        public ByteBuffer get(ByteBuffer buffer) {
            buffer.putFloat(cameraPosition.x);
            buffer.putFloat(cameraPosition.y);
            buffer.putFloat(cameraPosition.z);
            buffer.putFloat(sphereRadius);
            return buffer;
        }
    }

    ////////////////////////////////
    private final int projectionBlockIndex = 2;

    private int projectionUniformBuffer;

    ////////////////////////////////
    private final int lightBlockIndex = 1;

    private int lightUniformBuffer;

    ////////////////////////////////
    private final int materialBlockIndex = 0;

    private int materialArrayUniformBuffer;
    private int materialTerrainUniformBuffer;


    private void createMaterials() {
        ArrayList<MaterialBlock> ubArray = new ArrayList<>();

        MaterialBlock matEntry = new MaterialBlock();
        matEntry.diffuseColor = new Vector4f(0.1f, 0.1f, 0.8f, 1.0f);
        matEntry.specularColor = new Vector4f(0.8f, 0.8f, 0.8f, 1.0f);
        matEntry.specularShininess = 0.1f;
        ubArray.add(matEntry);

        matEntry = new MaterialBlock();
        matEntry.diffuseColor = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
        matEntry.specularColor = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);
        matEntry.specularShininess = 0.8f;
        ubArray.add(matEntry);

        matEntry = new MaterialBlock();
        matEntry.diffuseColor = new Vector4f(0.05f, 0.05f, 0.05f, 1.0f);
        matEntry.specularColor = new Vector4f(0.95f, 0.95f, 0.95f, 1.0f);
        matEntry.specularShininess = 0.3f;
        ubArray.add(matEntry);

        matEntry = new MaterialBlock();
        matEntry.diffuseColor = new Vector4f(0.803f, 0.709f, 0.15f, 1.0f);
        matEntry.specularColor = new Vector4f(0.803f, 0.709f, 0.15f, 1.0f).mul(0.75f);
        matEntry.specularShininess = 0.18f;
        ubArray.add(matEntry);

        materialArrayUniformBuffer = glGenBuffers();
        materialTerrainUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, materialArrayUniformBuffer);

        {
            ByteBuffer ubArrayBuffer = BufferUtils.createByteBuffer(ubArray.size() * MaterialBlock.SIZE_IN_BYTES);

            for (MaterialBlock anUbArray : ubArray) {
                anUbArray.get(ubArrayBuffer);
            }

            ubArrayBuffer.flip();

            glBufferData(GL_UNIFORM_BUFFER, ubArrayBuffer, GL_STATIC_DRAW);
        }

        glBindBuffer(GL_UNIFORM_BUFFER, materialTerrainUniformBuffer);

        {
            matEntry = new MaterialBlock();
            matEntry.diffuseColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
            matEntry.specularColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
            matEntry.specularShininess = 0.6f;

            glBufferData(GL_UNIFORM_BUFFER, matEntry.getAndFlip(BufferUtils.createByteBuffer(MaterialBlock.SIZE_IN_BYTES)),
                    GL_STATIC_DRAW);
        }

        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }
}
