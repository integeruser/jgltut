package org.jgltut.tut11;

import org.jgltut.LWJGLWindow;
import org.jgltut.framework.Framework;
import org.jgltut.framework.Mesh;
import org.jgltut.framework.MousePole;
import org.jgltut.framework.Timer;
import org.jgltut.Bufferable;
import org.jglsdk.glutil.MousePoles.*;
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
 * Chapter 11. Shinies
 * <p>
 * I,J,K,L  - control the light's position. Holding SHIFT with these keys will move in smaller increments.
 * SPACE    - toggle between drawing the uncolored cylinder and the colored one.
 * U,O      - control the specular value. They raise and low the specular exponent. Using SHIFT in combination with
 * them will raise/lower the exponent by smaller amounts.
 * Y        - toggle the drawing of the light source.
 * T        - toggle between the scaled and unscaled cylinder.
 * B        - toggle the light's rotation on/off.
 * G        - toggle between a diffuse color of (1, 1, 1) and a darker diffuse color of (0.2, 0.2, 0.2).
 * H        - switch between Blinn, Phong and Gaussian specular. Pressing SHIFT+H will switch between diffuse+specular
 * and specular only.
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
 */
public class GaussianSpecularLighting extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/org/jgltut/tut11/data/";
        new GaussianSpecularLighting().start(500, 500);
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

                        case GLFW_KEY_O:
                            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                                matParams.increment(false);
                            } else {
                                matParams.increment(true);
                            }

                            System.out.printf("Shiny: %f\n", (float) matParams.getSpecularValue());
                            break;

                        case GLFW_KEY_U:
                            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                                matParams.decrement(false);
                            } else {
                                matParams.decrement(true);
                            }

                            System.out.printf("Shiny: %f\n", (float) matParams.getSpecularValue());
                            break;

                        case GLFW_KEY_Y:
                            drawLightSource = !drawLightSource;
                            break;

                        case GLFW_KEY_T:
                            scaleCyl = !scaleCyl;
                            break;

                        case GLFW_KEY_B:
                            lightTimer.togglePause();
                            break;

                        case GLFW_KEY_G:
                            drawDark = !drawDark;
                            break;

                        case GLFW_KEY_H:
                            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                                int index;
                                if (lightModel.ordinal() % 2 != 0) {
                                    index = lightModel.ordinal() - 1;
                                } else {
                                    index = lightModel.ordinal() + 1;
                                }
                                index %= LightingModel.MAX_LIGHTING_MODEL.ordinal();
                                lightModel = LightingModel.values()[index];
                            } else {
                                int index = lightModel.ordinal() + 2;
                                index %= LightingModel.MAX_LIGHTING_MODEL.ordinal();
                                lightModel = LightingModel.values()[index];
                            }

                            System.out.printf("%s\n", lightModelNames[lightModel.ordinal()]);
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
        lightTimer.update(elapsedTime);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(viewPole.calcMatrix());

        final Vector4f worldLightPos = calcLightPosition();
        final Vector4f lightPosCameraSpace = modelMatrix.transform(new Vector4f(worldLightPos));

        ProgramData whiteProg = programs[lightModel.ordinal()].whiteProg;
        ProgramData colorProg = programs[lightModel.ordinal()].colorProg;

        glUseProgram(whiteProg.theProgram);
        glUniform4f(whiteProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(whiteProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(whiteProg.cameraSpaceLightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        float lightAttenuation = 1.2f;
        glUniform1f(whiteProg.lightAttenuationUnif, lightAttenuation);
        glUniform1f(whiteProg.shininessFactorUnif, matParams.getSpecularValue());
        glUniform4fv(whiteProg.baseDiffuseColorUnif, drawDark ? darkColor.get(vec4Buffer) : lightColor.get(vec4Buffer));

        glUseProgram(colorProg.theProgram);
        glUniform4f(colorProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(colorProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3fv(colorProg.cameraSpaceLightPosUnif, lightPosCameraSpace.get(vec4Buffer));
        glUniform1f(colorProg.lightAttenuationUnif, lightAttenuation);
        glUniform1f(colorProg.shininessFactorUnif, matParams.getSpecularValue());
        glUseProgram(0);

        {
            modelMatrix.pushMatrix();

            // Render the ground plane.
            {
                modelMatrix.pushMatrix();

                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                normMatrix.invert().transpose();

                glUseProgram(whiteProg.theProgram);
                glUniformMatrix4fv(whiteProg.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

                glUniformMatrix3fv(whiteProg.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));
                planeMesh.render();
                glUseProgram(0);

                modelMatrix.popMatrix();
            }

            // Render the Cylinder
            {
                modelMatrix.pushMatrix();

                modelMatrix.mul(objtPole.calcMatrix());

                if (scaleCyl) {
                    modelMatrix.scale(1.0f, 1.0f, 0.2f);
                }

                Matrix3f normMatrix = new Matrix3f(modelMatrix);
                normMatrix.invert().transpose();

                ProgramData prog = drawColoredCyl ? colorProg : whiteProg;
                glUseProgram(prog.theProgram);
                glUniformMatrix4fv(prog.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

                glUniformMatrix3fv(prog.normalModelToCameraMatrixUnif, false, normMatrix.get(mat3Buffer));

                if (drawColoredCyl) {
                    cylinderMesh.render("lit-color");
                } else {
                    cylinderMesh.render("lit");
                }

                glUseProgram(0);

                modelMatrix.popMatrix();
            }

            // Render the light
            if (drawLightSource) {
                modelMatrix.pushMatrix();

                modelMatrix.translate(worldLightPos.x, worldLightPos.y, worldLightPos.z);
                modelMatrix.scale(0.1f, 0.1f, 0.1f);

                glUseProgram(unlit.theProgram);
                glUniformMatrix4fv(unlit.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));
                glUniform4f(unlit.objectColorUnif, 0.8078f, 0.8706f, 0.9922f, 1.0f);
                cubeMesh.render("flat");

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
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.get(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        final float scale = 5;

        if (isKeyPressed(GLFW_KEY_J)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightRadius -= 0.05f * lastFrameDuration * scale;
            } else {
                lightRadius -= 0.2f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_L)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightRadius += 0.05f * lastFrameDuration * scale;
            } else {
                lightRadius += 0.2f * lastFrameDuration * scale;
            }
        }

        if (isKeyPressed(GLFW_KEY_I)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightHeight += 0.05f * lastFrameDuration * scale;
            } else {
                lightHeight += 0.2f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_K)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                lightHeight -= 0.05f * lastFrameDuration * scale;
            } else {
                lightHeight -= 0.2f * lastFrameDuration * scale;
            }
        }


        if (lightRadius < 0.2f) {
            lightRadius = 0.2f;
        }
    }

    ////////////////////////////////
    private ProgramPairs[] programs = new ProgramPairs[LightingModel.MAX_LIGHTING_MODEL.ordinal()];
    private ShaderPairs[] shaderFileNames = new ShaderPairs[]{
            new ShaderPairs("PN.vert", "PCN.vert", "PhongLighting.frag"),
            new ShaderPairs("PN.vert", "PCN.vert", "PhongOnly.frag"),
            new ShaderPairs("PN.vert", "PCN.vert", "BlinnLighting.frag"),
            new ShaderPairs("PN.vert", "PCN.vert", "BlinnOnly.frag"),
            new ShaderPairs("PN.vert", "PCN.vert", "GaussianLighting.frag"),
            new ShaderPairs("PN.vert", "PCN.vert", "GaussianOnly.frag")
    };
    private UnlitProgData unlit;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;

        int lightIntensityUnif;
        int ambientIntensityUnif;

        int normalModelToCameraMatrixUnif;
        int cameraSpaceLightPosUnif;
        int lightAttenuationUnif;
        int shininessFactorUnif;
        int baseDiffuseColorUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }

    private class ProgramPairs {
        ProgramData whiteProg;
        ProgramData colorProg;
    }

    private class ShaderPairs {
        String whiteVertShaderFileName;
        String colorVertShaderFileName;
        String fragmentShaderFileName;

        ShaderPairs(String whiteVertShaderFileName, String colorVertShaderFileName, String fragmentShaderFileName) {
            this.whiteVertShaderFileName = whiteVertShaderFileName;
            this.colorVertShaderFileName = colorVertShaderFileName;
            this.fragmentShaderFileName = fragmentShaderFileName;
        }
    }


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);


    private void initializePrograms() {
        for (int progIndex = 0; progIndex < LightingModel.MAX_LIGHTING_MODEL.ordinal(); progIndex++) {
            programs[progIndex] = new ProgramPairs();
            programs[progIndex].whiteProg = loadLitProgram(shaderFileNames[progIndex].whiteVertShaderFileName,
                    shaderFileNames[progIndex].fragmentShaderFileName);
            programs[progIndex].colorProg = loadLitProgram(shaderFileNames[progIndex].colorVertShaderFileName,
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
        data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "ambientIntensity");

        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
        data.cameraSpaceLightPosUnif = glGetUniformLocation(data.theProgram, "cameraSpaceLightPos");
        data.lightAttenuationUnif = glGetUniformLocation(data.theProgram, "lightAttenuation");
        data.shininessFactorUnif = glGetUniformLocation(data.theProgram, "shininessFactor");
        data.baseDiffuseColorUnif = glGetUniformLocation(data.theProgram, "baseDiffuseColor");

        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
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
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;

    private float lightHeight = 1.5f;
    private float lightRadius = 1.0f;
    private Timer lightTimer = new Timer(Timer.Type.LOOP, 5.0f);

    private boolean drawColoredCyl;
    private boolean drawLightSource;
    private boolean scaleCyl;
    private boolean drawDark;

    private final Vector4f darkColor = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f);
    private final Vector4f lightColor = new Vector4f(1.0f);

    private final String[] lightModelNames = {
            "Phong Specular.",
            "Phong Only.",
            "Blinn Specular.",
            "Blinn Only.",
            "Gaussian Specular.",
            "Gaussian Only."
    };


    private Vector4f calcLightPosition() {
        float currTimeThroughLoop = lightTimer.getAlpha();

        Vector4f lightPos = new Vector4f(0.0f, lightHeight, 0.0f, 1.0f);
        lightPos.x = (float) (Math.cos(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        lightPos.z = (float) (Math.sin(currTimeThroughLoop * (3.14159f * 2.0f)) * lightRadius);
        return lightPos;
    }

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

    private class ProjectionBlock implements Bufferable<FloatBuffer> {
        Matrix4f cameraToClipMatrix;

        static final int SIZE = 16*4;

        @Override
        public FloatBuffer get(FloatBuffer buffer) {
            return cameraToClipMatrix.get(buffer);
        }
    }

    ////////////////////////////////
    private LightingModel lightModel = LightingModel.GAUSSIAN_SPECULAR;

    private enum LightingModel {
        PHONG_SPECULAR,
        PHONG_ONLY,
        BLINN_SPECULAR,
        BLINN_ONLY,
        GAUSSIAN_SPECULAR,
        GAUSSIAN_ONLY,

        MAX_LIGHTING_MODEL
    }

    ////////////////////////////////
    private MaterialParams matParams = new MaterialParams();

    private class MaterialParams {
        float phongExponent;
        float blinnExponent;
        float gaussianRoughness;


        MaterialParams() {
            phongExponent = 4.0f;
            blinnExponent = 4.0f;
            gaussianRoughness = 0.5f;
        }


        void increment(boolean isLarge) {
            switch (lightModel) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    if (isLarge) {
                        phongExponent += 0.5f;
                    } else {
                        phongExponent += 0.1f;
                    }
                    break;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    if (isLarge) {
                        blinnExponent += 0.5f;
                    } else {
                        blinnExponent += 0.1f;
                    }
                    break;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    if (isLarge) {
                        gaussianRoughness += 0.1f;
                    } else {
                        gaussianRoughness += 0.01f;
                    }
                    break;

                default:
                    break;
            }

            clampParam();
        }

        void decrement(boolean isLarge) {
            switch (lightModel) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    if (isLarge) {
                        phongExponent += 0.5f;
                    } else {
                        phongExponent += 0.1f;
                    }
                    break;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    if (isLarge) {
                        blinnExponent += 0.5f;
                    } else {
                        blinnExponent += 0.1f;
                    }
                    break;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    if (isLarge) {
                        gaussianRoughness -= 0.1f;
                    } else {
                        gaussianRoughness -= 0.01f;
                    }
                    break;

                default:
                    break;
            }

            clampParam();
        }


        float getSpecularValue() {
            switch (lightModel) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    return phongExponent;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    return blinnExponent;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    return gaussianRoughness;

                default:
                    return 0.0f;
            }
        }


        void clampParam() {
            switch (lightModel) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    if (phongExponent <= 0.0f) {
                        phongExponent = 0.0001f;
                    }
                    break;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    if (blinnExponent <= 0.0f) {
                        blinnExponent = 0.0001f;
                    }
                    break;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    gaussianRoughness = Math.max(0.0001f, gaussianRoughness);
                    gaussianRoughness = Math.min(1.0f, gaussianRoughness);
                    break;

                default:
                    break;
            }
        }
    }
}
