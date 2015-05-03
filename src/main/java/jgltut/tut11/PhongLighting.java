package jgltut.tut11;

import jgltut.jglsdk.BufferableData;
import jgltut.jglsdk.glm.*;
import jgltut.jglsdk.glutil.MatrixStack;
import jgltut.jglsdk.glutil.MousePoles.*;
import jgltut.LWJGLWindow;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.framework.MousePole;
import jgltut.framework.Timer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.nio.FloatBuffer;
import java.util.ArrayList;

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
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2011.html
 * <p>
 * I,J,K,L  - control the light's position. Holding SHIFT with these keys will move in smaller increments.
 * SPACE    - toggles between drawing the uncolored cylinder and the colored one.
 * U,O      - control the specular value. They raise and low the specular exponent. Using SHIFT in combination with
 * them will raise/lower the exponent by smaller amounts.
 * Y        - toggles the drawing of the light source.
 * T        - toggles between the scaled and unscaled cylinder.
 * B        - toggles the light's rotation on/off.
 * G        - toggles between a diffuse color of (1, 1, 1) and a darker diffuse color of (0.2, 0.2, 0.2).
 * H        - selects between specular and diffuse, just specular and just diffuse. Pressing SHIFT+H will toggle between
 * diffuse only and diffuse+specular.
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
public class PhongLighting extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut11/data/";
        new PhongLighting().start();
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

        ProgramData whiteProg = null;
        ProgramData colorProg = null;

        switch (lightModel) {
            case PURE_DIFFUSE:
                whiteProg = whiteNoPhong;
                colorProg = colorNoPhong;
                break;

            case DIFFUSE_AND_SPECULAR:
                whiteProg = whitePhong;
                colorProg = colorPhong;
                break;

            case SPECULAR_ONLY:
                whiteProg = whitePhongOnly;
                colorProg = colorPhongOnly;
                break;

            default:
                break;
        }

        glUseProgram(whiteProg.theProgram);
        glUniform4f(whiteProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(whiteProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3(whiteProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(vec4Buffer));
        float lightAttenuation = 1.2f;
        glUniform1f(whiteProg.lightAttenuationUnif, lightAttenuation);
        glUniform1f(whiteProg.shininessFactorUnif, shininessFactor);
        glUniform4(whiteProg.baseDiffuseColorUnif, drawDark ? darkColor.fillAndFlipBuffer(vec4Buffer) : lightColor.fillAndFlipBuffer(vec4Buffer));

        glUseProgram(colorProg.theProgram);
        glUniform4f(colorProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
        glUniform4f(colorProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
        glUniform3(colorProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(vec4Buffer));
        glUniform1f(colorProg.lightAttenuationUnif, lightAttenuation);
        glUniform1f(colorProg.shininessFactorUnif, shininessFactor);
        glUseProgram(0);

        {
            modelMatrix.push();

            // Render the ground plane.
            {
                modelMatrix.push();

                Mat3 normMatrix = new Mat3(modelMatrix.top());
                normMatrix = Glm.transpose(Glm.inverse(normMatrix));

                glUseProgram(whiteProg.theProgram);
                glUniformMatrix4(whiteProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

                glUniformMatrix3(whiteProg.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
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

                ProgramData prog = drawColoredCyl ? colorProg : whiteProg;
                glUseProgram(prog.theProgram);
                glUniformMatrix4(prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

                glUniformMatrix3(prog.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));

                if (drawColoredCyl) {
                    cylinderMesh.render("lit-color");
                } else {
                    cylinderMesh.render("lit");
                }

                glUseProgram(0);

                modelMatrix.pop();
            }

            // Render the light
            if (drawLightSource) {
                modelMatrix.push();

                modelMatrix.translate(worldLightPos.x, worldLightPos.y, worldLightPos.z);
                modelMatrix.scale(0.1f, 0.1f, 0.1f);

                glUseProgram(unlit.theProgram);
                glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
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

        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        while (Mouse.next()) {
            int eventButton = Mouse.getEventButton();
            if (eventButton != -1) {
                boolean pressed = Mouse.getEventButtonState();
                MousePole.forwardMouseButton(viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
                MousePole.forwardMouseButton(objtPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
            } else {
                // Mouse moving or mouse scrolling
                int dWheel = Mouse.getDWheel();
                if (dWheel != 0) {
                    MousePole.forwardMouseWheel(viewPole, dWheel, Mouse.getX(), Mouse.getY());
                    MousePole.forwardMouseWheel(objtPole, dWheel, Mouse.getX(), Mouse.getY());
                }

                if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
                    MousePole.forwardMouseMotion(viewPole, Mouse.getX(), Mouse.getY());
                    MousePole.forwardMouseMotion(objtPole, Mouse.getX(), Mouse.getY());
                }
            }
        }


        float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

        if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                lightRadius -= 0.05f * lastFrameDuration;
            } else {
                lightRadius -= 0.2f * lastFrameDuration;
            }
        } else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                lightRadius += 0.05f * lastFrameDuration;
            } else {
                lightRadius += 0.2f * lastFrameDuration;
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                lightHeight += 0.05f * lastFrameDuration;
            } else {
                lightHeight += 0.2f * lastFrameDuration;
            }
        } else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                lightHeight -= 0.05f * lastFrameDuration;
            } else {
                lightHeight -= 0.2f * lastFrameDuration;
            }
        }

        if (lightRadius < 0.2f) {
            lightRadius = 0.2f;
        }


        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                switch (Keyboard.getEventKey()) {
                    case Keyboard.KEY_SPACE:
                        drawColoredCyl = !drawColoredCyl;
                        break;

                    case Keyboard.KEY_O:
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                            shininessFactor += 0.1f;
                        } else {
                            shininessFactor += 0.5f;
                        }

                        System.out.printf("Shiny: %f\n", shininessFactor);
                        break;

                    case Keyboard.KEY_U:
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                            shininessFactor -= 0.1f;
                        } else {
                            shininessFactor -= 0.5f;
                        }

                        System.out.printf("Shiny: %f\n", shininessFactor);
                        break;

                    case Keyboard.KEY_Y:
                        drawLightSource = !drawLightSource;
                        break;

                    case Keyboard.KEY_T:
                        scaleCyl = !scaleCyl;
                        break;

                    case Keyboard.KEY_B:
                        lightTimer.togglePause();
                        break;

                    case Keyboard.KEY_G:
                        drawDark = !drawDark;
                        break;

                    case Keyboard.KEY_H:
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                            switch (lightModel) {
                                case DIFFUSE_AND_SPECULAR:
                                    lightModel = LightingModel.PURE_DIFFUSE;
                                    break;

                                case PURE_DIFFUSE:
                                    lightModel = LightingModel.DIFFUSE_AND_SPECULAR;
                                    break;

                                case SPECULAR_ONLY:
                                    lightModel = LightingModel.PURE_DIFFUSE;
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            int index = lightModel.ordinal() + 1;
                            index %= LightingModel.MAX_LIGHTING_MODEL.ordinal();
                            lightModel = LightingModel.values()[index];
                        }

                        System.out.printf("%s\n", lightModelNames[lightModel.ordinal()]);
                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }
            }
        }

        if (shininessFactor <= 0.0f) {
            shininessFactor = 0.0001f;
        }
    }

    ////////////////////////////////
    private ProgramData whiteNoPhong;
    private ProgramData colorNoPhong;
    private ProgramData whitePhong;
    private ProgramData colorPhong;
    private ProgramData whitePhongOnly;
    private ProgramData colorPhongOnly;
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


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(Vec4.SIZE);
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(Mat3.SIZE);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);


    private void initializePrograms() {
        whiteNoPhong = loadLitProgram("PN.vert", "NoPhong.frag");
        colorNoPhong = loadLitProgram("PCN.vert", "NoPhong.frag");
        whitePhong = loadLitProgram("PN.vert", "PhongLighting.frag");
        colorPhong = loadLitProgram("PCN.vert", "PhongLighting.frag");
        whitePhongOnly = loadLitProgram("PN.vert", "PhongOnly.frag");
        colorPhongOnly = loadLitProgram("PCN.vert", "PhongOnly.frag");
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
    private float shininessFactor = 4.0f;

    private boolean drawColoredCyl;
    private boolean drawLightSource;
    private boolean scaleCyl;
    private boolean drawDark;

    private final Vec4 darkColor = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
    private final Vec4 lightColor = new Vec4(1.0f);

    private final String[] lightModelNames = {
            "Diffuse only.",
            "Specular + diffuse.",
            "Specular only."
    };


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

    private int projectionUniformBuffer;

    private class ProjectionBlock extends BufferableData<FloatBuffer> {
        Mat4 cameraToClipMatrix;

        static final int SIZE = Mat4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            return cameraToClipMatrix.fillBuffer(buffer);
        }
    }

    ////////////////////////////////
    private LightingModel lightModel = LightingModel.DIFFUSE_AND_SPECULAR;

    private enum LightingModel {
        PURE_DIFFUSE,
        DIFFUSE_AND_SPECULAR,
        SPECULAR_ONLY,

        MAX_LIGHTING_MODEL
    }
}