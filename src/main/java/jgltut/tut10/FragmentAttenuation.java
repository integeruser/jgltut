package jgltut.tut10;

import jgltut.LWJGLWindow;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.framework.MousePole;
import jgltut.framework.Timer;
import jgltut.jglsdk.BufferableData;
import jgltut.jglsdk.glm.*;
import jgltut.jglsdk.glutil.MatrixStack;
import jgltut.jglsdk.glutil.MousePoles.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.nio.ByteBuffer;
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
 * Chapter 10. Plane Lights
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2010.html
 * <p>
 * I,J,K,L  - control the light's position. Holding LEFT_SHIFT with these keys will move in smaller increments.
 * SPACE    - toggle between drawing the uncolored cylinder and the colored one.
 * O,U      - increase/decrease the attenuation constant.
 * Y        - toggle the drawing of the light source.
 * T        - toggle between the scaled and unscaled cylinder.
 * B        - toggle the light's rotation on/off.
 * H        - swap between the linear and quadratic interpolation functions.
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
public class FragmentAttenuation extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut10/data/";
        new FragmentAttenuation().start(500, 500);
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            cylinderMesh = new Mesh("UnitCylinder.xml");
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

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

        unprojectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, unprojectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, UnProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE);

        // Bind the static buffers.
        glBindBufferRange(GL_UNIFORM_BUFFER, unprojectionBlockIndex, unprojectionUniformBuffer, 0, UnProjectionBlock.SIZE);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_SPACE:
                            drawColoredCyl = !drawColoredCyl;
                            break;

                        case GLFW_KEY_O:
                            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                                lightAttenuation *= 1.1f;
                            } else {
                                lightAttenuation *= 1.5f;
                            }

                            System.out.printf("Atten: %f\n", lightAttenuation);
                            break;

                        case GLFW_KEY_U:
                            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                                lightAttenuation /= 1.1f;
                            } else {
                                lightAttenuation /= 1.5f;
                            }

                            System.out.printf("Atten: %f\n", lightAttenuation);
                            break;

                        case GLFW_KEY_Y:
                            drawLight = !drawLight;
                            break;

                        case GLFW_KEY_T:
                            scaleCyl = !scaleCyl;
                            break;

                        case GLFW_KEY_B:
                            lightTimer.togglePause();
                            break;

                        case GLFW_KEY_H:
                            useRSquare = !useRSquare;
                            if (useRSquare) {
                                System.out.printf("Inverse Squared Attenuation\n");
                            } else {
                                System.out.printf("Plain Inverse Attenuation\n");
                            }
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
        lightTimer.update(getElapsedTime());

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStack modelMatrix = new MatrixStack();
        modelMatrix.setMatrix(viewPole.calcMatrix());

        final Vec4 worldLightPos = calcLightPosition();
        final Vec4 lightPosCameraSpace = Mat4.mul(modelMatrix.top(), worldLightPos);

        glUseProgram(fragWhiteDiffuseColor.theProgram);
        glUniform4f(fragWhiteDiffuseColor.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(fragWhiteDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(fragWhiteDiffuseColor.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(vec4Buffer));
        glUniform1f(fragWhiteDiffuseColor.lightAttenuationUnif, lightAttenuation);
        glUniform1i(fragWhiteDiffuseColor.useRSquareUnif, useRSquare ? 1 : 0);

        glUseProgram(fragVertexDiffuseColor.theProgram);
        glUniform4f(fragVertexDiffuseColor.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(fragVertexDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(fragVertexDiffuseColor.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(vec4Buffer));
        glUniform1f(fragVertexDiffuseColor.lightAttenuationUnif, lightAttenuation);
        glUniform1i(fragVertexDiffuseColor.useRSquareUnif, useRSquare ? 1 : 0);
        glUseProgram(0);

        {
            modelMatrix.push();

            // Render the ground plane.
            {
                modelMatrix.push();

                Mat3 normMatrix = new Mat3(modelMatrix.top());
                normMatrix = Glm.transpose(Glm.inverse(normMatrix));

                glUseProgram(fragWhiteDiffuseColor.theProgram);
                glUniformMatrix4fv(fragWhiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

                glUniformMatrix3fv(fragWhiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
                planeMesh.render();
                glUseProgram(0);

                modelMatrix.pop();
            }

            // Render the Cylinder
            {
                modelMatrix.push();

                modelMatrix.applyMatrix(objtPole.calcMatrix());

                if (scaleCyl) {
                    modelMatrix.scale(1.0f, 1.0f, 0.2f);
                }

                Mat3 normMatrix = new Mat3(modelMatrix.top());
                normMatrix = Glm.transpose(Glm.inverse(normMatrix));

                if (drawColoredCyl) {
                    glUseProgram(fragVertexDiffuseColor.theProgram);
                    glUniformMatrix4fv(fragVertexDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

                    glUniformMatrix3fv(fragVertexDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
                    cylinderMesh.render("lit-color");
                } else {
                    glUseProgram(fragWhiteDiffuseColor.theProgram);
                    glUniformMatrix4fv(fragWhiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

                    glUniformMatrix3fv(fragWhiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
                    cylinderMesh.render("lit");
                }
                glUseProgram(0);

                modelMatrix.pop();
            }

            // Render the light
            if (drawLight) {
                modelMatrix.push();

                modelMatrix.translate(worldLightPos.x, worldLightPos.y, worldLightPos.z);
                modelMatrix.scale(0.1f, 0.1f, 0.1f);

                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
                glUniform4f(unlit.objectColorUnif, 0.8078f, 0.8706f, 0.9922f, 1.0f);
                cubeMesh.render("flat");

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

        UnProjectionBlock unprojData = new UnProjectionBlock();
        unprojData.clipToCameraMatrix = Glm.inverse(persMatrix.top());
        unprojData.windowSize = new Vec2(w, h);

        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, unprojectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, unprojData.fillAndFlipBuffer(BufferUtils.createByteBuffer(18 * FLOAT_SIZE)));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        float lastFrameDuration = getLastFrameDuration() * 5;

        if (isKeyPressed(GLFW_KEY_J)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightRadius -= 0.05f * lastFrameDuration;
            } else {
                lightRadius -= 0.2f * lastFrameDuration;
            }
        } else if (isKeyPressed(GLFW_KEY_L)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightRadius += 0.05f * lastFrameDuration;
            } else {
                lightRadius += 0.2f * lastFrameDuration;
            }
        }

        if (isKeyPressed(GLFW_KEY_I)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightHeight += 0.05f * lastFrameDuration;
            } else {
                lightHeight += 0.2f * lastFrameDuration;
            }
        } else if (isKeyPressed(GLFW_KEY_K)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightHeight -= 0.05f * lastFrameDuration;
            } else {
                lightHeight -= 0.2f * lastFrameDuration;
            }
        }


        if (lightRadius < 0.2f) {
            lightRadius = 0.2f;
        }

        if (lightAttenuation < 0.1f) {
            lightAttenuation = 0.1f;
        }
    }

    ////////////////////////////////
    private ProgramData fragWhiteDiffuseColor;
    private ProgramData fragVertexDiffuseColor;
    private UnlitProgData unlit;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;

        int lightIntensityUnif;
        int ambientIntensityUnif;

        int normalModelToCameraMatrixUnif;
        int cameraSpaceLightPosUnif;
        int lightAttenuationUnif;
        int useRSquareUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(Vec4.SIZE);
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(Mat3.SIZE);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);


    private void initializePrograms() {
        fragWhiteDiffuseColor = loadLitProgram("FragLightAtten_PN.vert", "FragLightAtten.frag");
        fragVertexDiffuseColor = loadLitProgram("FragLightAtten_PCN.vert", "FragLightAtten.frag");
        unlit = loadUnlitProgram("PosTransform.vert", "UniformColor.frag");
    }

    private ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
        data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "ambientIntensity");

        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
        data.cameraSpaceLightPosUnif = glGetUniformLocation(data.theProgram, "cameraSpaceLightPos");
        data.lightAttenuationUnif = glGetUniformLocation(data.theProgram, "lightAttenuation");
        data.useRSquareUnif = glGetUniformLocation(data.theProgram, "bUseRSquare");

        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

        int unprojectionBlock = glGetUniformBlockIndex(data.theProgram, "UnProjection");
        glUniformBlockBinding(data.theProgram, unprojectionBlock, unprojectionBlockIndex);

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
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;

    private float lightHeight = 1.5f;
    private float lightRadius = 1.0f;
    private float lightAttenuation = 1.0f;
    private Timer lightTimer = new Timer(Timer.Type.LOOP, 5.0f);

    private boolean drawColoredCyl;
    private boolean drawLight;
    private boolean scaleCyl;
    private boolean useRSquare;


    private Vec4 calcLightPosition() {
        float currTimeThroughLoop = lightTimer.getAlpha();

        Vec4 lightPos = new Vec4(0.0f, lightHeight, 0.0f, 1.0f);
        lightPos.x = (float) (Math.cos(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        lightPos.z = (float) (Math.sin(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        return lightPos;
    }

    ////////////////////////////////
    // View / Object setup.
    private ViewData initialViewData = new ViewData(
            new Vec3(0.0f, 0.5f, 0.0f),
            new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
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
            new Vec3(0.0f, 0.5f, 0.0f),
            new Quaternion(1.0f, 0.0f, 0.0f, 0.0f)
    );


    private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
    private ObjectPole objtPole = new ObjectPole(initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);

    ////////////////////////////////
    private final int projectionBlockIndex = 2;
    private final int unprojectionBlockIndex = 1;

    private int projectionUniformBuffer;
    private int unprojectionUniformBuffer;

    private class ProjectionBlock extends BufferableData<FloatBuffer> {
        Mat4 cameraToClipMatrix;

        static final int SIZE = Mat4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            return cameraToClipMatrix.fillBuffer(buffer);
        }
    }

    private class UnProjectionBlock extends BufferableData<ByteBuffer> {
        Mat4 clipToCameraMatrix;
        Vec2 windowSize;

        static final int SIZE = Mat4.SIZE + Vec2.SIZE;

        @Override
        public ByteBuffer fillBuffer(ByteBuffer buffer) {
            for (float f : clipToCameraMatrix.get()) {
                buffer.putFloat(f);
            }
            // The shader uses an int vector
            buffer.putInt((int) windowSize.x);
            buffer.putInt((int) windowSize.y);
            return buffer;
        }
    }
}