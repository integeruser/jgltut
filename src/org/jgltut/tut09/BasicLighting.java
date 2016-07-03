package org.jgltut.tut09;

import org.jgltut.LWJGLWindow;
import org.jgltut.framework.Framework;
import org.jgltut.framework.Mesh;
import org.jgltut.framework.MousePole;
import org.jgltut.jglsdk.BufferableData;
import org.jgltut.jglsdk.glm.Glm;
import org.jgltut.jglsdk.glutil.MousePoles.*;
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
 * Chapter 9. Lights On
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2009.html
 * <p>
 * SPACE    - toggle between drawing the uncolored cylinder and the colored one.
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
public class BasicLighting extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut09/data/";
        new BasicLighting().start(500, 500);
    }


    @Override
    protected void init() {
        initializeProgram();

        try {
            cylinderMesh = new Mesh("UnitCylinder.xml");
            planeMesh = new Mesh("LargePlane.xml");
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        glEnable(GL_DEPTH_CLAMP);

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_SPACE:
                            drawColoredCyl = !drawColoredCyl;
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
                MousePole.forwardMouseButton(window, objtPole, button, pressed, x, y);
            }
        });
        glfwSetCursorPosCallback(window, mousePosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT) || isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                    MousePole.forwardMouseMotion(viewPole, (int) xpos, (int) ypos);
                    MousePole.forwardMouseMotion(objtPole, (int) xpos, (int) ypos);
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
                MousePole.forwardMouseWheel(window, objtPole, (int) yoffset, x, y);
            }
        });
    }

    @Override
    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());

        Vector4f lightDirCameraSpace = modelMatrix.transform(new Vector4f(lightDirection));

        glUseProgram(whiteDiffuseColor.theProgram);
        glUniform3fv(whiteDiffuseColor.dirToLightUnif, lightDirCameraSpace.get(vec4Buffer));
        glUseProgram(vertexDiffuseColor.theProgram);
        glUniform3fv(vertexDiffuseColor.dirToLightUnif, lightDirCameraSpace.get(vec4Buffer));
        glUseProgram(0);

        {
            modelMatrix.pushMatrix();

            // Render the ground plane.
            {
                modelMatrix.pushMatrix();

                glUseProgram(whiteDiffuseColor.theProgram);
                glUniformMatrix4fv(whiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                glUniformMatrix3fv(whiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
                glUniform4f(whiteDiffuseColor.lightIntensityUnif, 1.0f, 1.0f, 1.0f, 1.0f);
                planeMesh.render();
                glUseProgram(0);

                modelMatrix.popMatrix();
            }

            // Render the Cylinder
            {
                modelMatrix.pushMatrix();

                modelMatrix.mul(objtPole.calcMatrix());

                if (drawColoredCyl) {
                    glUseProgram(vertexDiffuseColor.theProgram);
                    glUniformMatrix4fv(vertexDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                    Matrix3f normMatrix = new Matrix3f(modelMatrix);
                    glUniformMatrix3fv(vertexDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
                    glUniform4f(vertexDiffuseColor.lightIntensityUnif, 1.0f, 1.0f, 1.0f, 1.0f);
                    cylinderMesh.render("lit-color");
                } else {
                    glUseProgram(whiteDiffuseColor.theProgram);
                    glUniformMatrix4fv(whiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                    Matrix3f normMatrix = new Matrix3f(modelMatrix);
                    glUniformMatrix3fv(whiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
                    glUniform4f(whiteDiffuseColor.lightIntensityUnif, 1.0f, 1.0f, 1.0f, 1.0f);
                    cylinderMesh.render("lit");
                }

                glUseProgram(0);

                modelMatrix.popMatrix();
            }

            modelMatrix.popMatrix();
        }
    }

    @Override
    protected void reshape(int w, int h) {
        float zNear = 1.0f;
        float zFar = 1000.0f;
        MatrixStackf persMatrix = new MatrixStackf();
        persMatrix.perspective(Framework.degToRad(45.0f), (w / (float) h), zNear, zFar);

        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix;

        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillBuffer(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
    }

    ////////////////////////////////
    private ProgramData whiteDiffuseColor;
    private ProgramData vertexDiffuseColor;

    private class ProgramData {
        int theProgram;

        int dirToLightUnif;
        int lightIntensityUnif;

        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);


    private void initializeProgram() {
        whiteDiffuseColor = loadProgram("DirVertexLighting_PN.vert", "ColorPassthrough.frag");
        vertexDiffuseColor = loadProgram("DirVertexLighting_PCN.vert", "ColorPassthrough.frag");
    }

    private ProgramData loadProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
        data.dirToLightUnif = glGetUniformLocation(data.theProgram, "dirToLight");
        data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");

        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

        return data;
    }

    ////////////////////////////////
    private Mesh cylinderMesh;
    private Mesh planeMesh;

    private Vector4f lightDirection = new Vector4f(0.866f, 0.5f, 0.0f, 0.0f);

    private boolean drawColoredCyl = true;

    ////////////////////////////////
    // View / Object setup.
    private ViewData initialViewData = new ViewData(
            new Vector3f(0.0f, 0.5f, 0.0f),
            new Quaternionf(0.3826834f, 0.0f, 0.0f, 0.92387953f),
            5.0f,
            0.0f
    );

    private ViewScale viewScale = new ViewScale(
            3.0f, 20.0f,
            1.5f, 0.5f,
            0.0f, 0.0f,  // No camera movement.
            90.0f / 250.0f
    );


    private ObjectData initialObjectData = new ObjectData(
            new Vector3f(0.0f, 0.5f, 0.0f),
            new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
    );


    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole objtPole = new ObjectPole(initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);

    ////////////////////////////////
    private final int projectionBlockIndex = 2;

    private int projectionUniformBuffer;

    private class ProjectionBlock extends BufferableData<FloatBuffer> {
        Matrix4f cameraToClipMatrix;

        static final int SIZE = 16*4;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            return cameraToClipMatrix.get(buffer);
        }
    }
}