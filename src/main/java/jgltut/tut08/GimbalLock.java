package jgltut.tut08;

import jgltut.LWJGLWindow;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.jglsdk.glm.Mat4;
import jgltut.jglsdk.glm.Vec4;
import jgltut.jglsdk.glutil.MatrixStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part II. Positioning
 * Chapter 8. Getting Oriented
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2008.html
 * <p>
 * SPACE    - toggle drawing the gimbal rings.
 * W,S      - control the outer gimbal.
 * A,D      - control the middle gimbal.
 * Q,E      - control the inner gimbal.
 *
 * @author integeruser
 */
public class GimbalLock extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut08/data/";
        new GimbalLock().start();
    }


    @Override
    protected void init() {
        initializeProgram();

        try {
            for (int gimbalIndex = 0; gimbalIndex < 3; gimbalIndex++) {
                gimbals[gimbalIndex] = new Mesh(gimbalNames[gimbalIndex]);
            }

            object = new Mesh("Ship.xml");
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


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_RELEASE) {
                    switch (key) {
                        case GLFW_KEY_SPACE:
                            drawGimbals = !drawGimbals;
                            break;

                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, GL_TRUE);
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        MatrixStack currMatrix = new MatrixStack();
        currMatrix.translate(0.0f, 0.0f, -200.0f);
        currMatrix.rotateX(gimbalAngles.angleX);
        drawGimbal(currMatrix, GimbalAxis.X_AXIS, new Vec4(0.4f, 0.4f, 1.0f, 1.0f));
        currMatrix.rotateY(gimbalAngles.angleY);
        drawGimbal(currMatrix, GimbalAxis.Y_AXIS, new Vec4(0.0f, 1.0f, 0.0f, 1.0f));
        currMatrix.rotateZ(gimbalAngles.angleZ);
        drawGimbal(currMatrix, GimbalAxis.Z_AXIS, new Vec4(1.0f, 0.3f, 0.3f, 1.0f));

        glUseProgram(theProgram);

        currMatrix.scale(3.0f, 3.0f, 3.0f);
        currMatrix.rotateX(-90.0f);

        // Set the base color for this object.
        glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
        glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer(mat4Buffer));

        object.render("tint");

        glUseProgram(0);
    }

    @Override
    protected void reshape(int w, int h) {
        cameraToClipMatrix.set(0, 0, frustumScale * (h / (float) w));
        cameraToClipMatrix.set(1, 1, frustumScale);

        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
        glUseProgram(0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        final float SMALL_ANGLE_INCREMENT = 9.0f;
        float lastFrameDuration = getLastFrameDuration() * 10 / 1000.0f;

        if (isKeyPressed(GLFW_KEY_W)) {
            gimbalAngles.angleX += SMALL_ANGLE_INCREMENT * lastFrameDuration;
        } else if (isKeyPressed(GLFW_KEY_S)) {
            gimbalAngles.angleX -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
        }

        if (isKeyPressed(GLFW_KEY_A)) {
            gimbalAngles.angleY += SMALL_ANGLE_INCREMENT * lastFrameDuration;
        } else if (isKeyPressed(GLFW_KEY_D)) {
            gimbalAngles.angleY -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
        }

        if (isKeyPressed(GLFW_KEY_Q)) {
            gimbalAngles.angleZ += SMALL_ANGLE_INCREMENT * lastFrameDuration;
        } else if (isKeyPressed(GLFW_KEY_E)) {
            gimbalAngles.angleZ -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
        }
    }

    ////////////////////////////////
    private int theProgram;

    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;
    private int baseColorUnif;

    private Mat4 cameraToClipMatrix = new Mat4(0.0f);
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);

    private final float frustumScale = calcFrustumScale(20.0f);


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "PosColorLocalTransform.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorMultUniform.frag"));
        theProgram = Framework.createProgram(shaderList);

        modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
        baseColorUnif = glGetUniformLocation(theProgram, "baseColor");

        float zNear = 1.0f;
        float zFar = 600.0f;
        cameraToClipMatrix.set(0, 0, frustumScale);
        cameraToClipMatrix.set(1, 1, frustumScale);
        cameraToClipMatrix.set(2, 2, (zFar + zNear) / (zNear - zFar));
        cameraToClipMatrix.set(2, 3, -1.0f);
        cameraToClipMatrix.set(3, 2, (2 * zFar * zNear) / (zNear - zFar));

        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
        glUseProgram(0);
    }


    private float calcFrustumScale(float fovDeg) {
        final float degToRad = 3.14159f * 2.0f / 360.0f;
        float fovRad = fovDeg * degToRad;
        return (float) (1.0f / Math.tan(fovRad / 2.0f));
    }

    ////////////////////////////////
    private Mesh object;

    private Mesh gimbals[] = new Mesh[3];
    private final String gimbalNames[] = {
            "LargeGimbal.xml",
            "MediumGimbal.xml",
            "SmallGimbal.xml"
    };
    private GimbalAngles gimbalAngles = new GimbalAngles();

    private boolean drawGimbals = true;

    private enum GimbalAxis {
        X_AXIS,
        Y_AXIS,
        Z_AXIS
    }

    private class GimbalAngles {
        float angleX;
        float angleY;
        float angleZ;
    }


    private void drawGimbal(MatrixStack currMatrix, GimbalAxis axis, Vec4 baseColor) {
        if (!drawGimbals) return;

        currMatrix.push();

        switch (axis) {
            case X_AXIS:
                break;

            case Y_AXIS:
                currMatrix.rotateZ(90.0f);
                currMatrix.rotateX(90.0f);
                break;

            case Z_AXIS:
                currMatrix.rotateY(90.0f);
                currMatrix.rotateX(90.0f);
                break;
        }

        glUseProgram(theProgram);

        // Set the base color for this object.
        glUniform4f(baseColorUnif, baseColor.x, baseColor.y, baseColor.z, baseColor.w);
        glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer(mat4Buffer));

        gimbals[axis.ordinal()].render();

        glUseProgram(0);

        currMatrix.pop();
    }
}