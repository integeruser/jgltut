package integeruser.jgltut.tut08;

import integeruser.jgltut.Tutorial;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part II. Positioning
 * Chapter 8. Getting Oriented
 * <p>
 * SPACE    - toggle drawing the gimbal rings.
 * W,S      - control the outer gimbal.
 * A,D      - control the middle gimbal.
 * Q,E      - control the inner gimbal.
 */
public class GimbalLock extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut08/data/";
        new GimbalLock().start(500, 500);
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
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_SPACE:
                            drawGimbals = !drawGimbals;
                            break;

                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, true);
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

        MatrixStackf currMatrix = new MatrixStackf(10);
        currMatrix.translate(0.0f, 0.0f, -200.0f);
        currMatrix.rotateX((float) Math.toRadians(gimbalAngles.angleX));
        drawGimbal(currMatrix, GimbalAxis.X_AXIS, new Vector4f(0.4f, 0.4f, 1.0f, 1.0f));
        currMatrix.rotateY((float) Math.toRadians(gimbalAngles.angleY));
        drawGimbal(currMatrix, GimbalAxis.Y_AXIS, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        currMatrix.rotateZ((float) Math.toRadians(gimbalAngles.angleZ));
        drawGimbal(currMatrix, GimbalAxis.Z_AXIS, new Vector4f(1.0f, 0.3f, 0.3f, 1.0f));

        glUseProgram(theProgram);

        currMatrix.scale(3.0f, 3.0f, 3.0f);
        currMatrix.rotateX((float) Math.toRadians(-90.0f));

        // Set the base color for this object.
        glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
        glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.get(mat4Buffer));

        object.render("tint");

        glUseProgram(0);
    }

    @Override
    protected void reshape(int w, int h) {
        cameraToClipMatrix.m00(frustumScale * (h / (float) w));
        cameraToClipMatrix.m11(frustumScale);

        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        final float SMALL_ANGLE_INCREMENT = 9.0f;
        final float scale = 10;

        if (isKeyPressed(GLFW_KEY_W)) {
            gimbalAngles.angleX += SMALL_ANGLE_INCREMENT * lastFrameDuration * scale;
        } else if (isKeyPressed(GLFW_KEY_S)) {
            gimbalAngles.angleX -= SMALL_ANGLE_INCREMENT * lastFrameDuration * scale;
        }

        if (isKeyPressed(GLFW_KEY_A)) {
            gimbalAngles.angleY += SMALL_ANGLE_INCREMENT * lastFrameDuration * scale;
        } else if (isKeyPressed(GLFW_KEY_D)) {
            gimbalAngles.angleY -= SMALL_ANGLE_INCREMENT * lastFrameDuration * scale;
        }

        if (isKeyPressed(GLFW_KEY_Q)) {
            gimbalAngles.angleZ += SMALL_ANGLE_INCREMENT * lastFrameDuration * scale;
        } else if (isKeyPressed(GLFW_KEY_E)) {
            gimbalAngles.angleZ -= SMALL_ANGLE_INCREMENT * lastFrameDuration * scale;
        }
    }

    ////////////////////////////////
    private int theProgram;

    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;
    private int baseColorUnif;

    private Matrix4f cameraToClipMatrix = new Matrix4f();

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
        cameraToClipMatrix.m00(frustumScale);
        cameraToClipMatrix.m11(frustumScale);
        cameraToClipMatrix.m22((zFar + zNear) / (zNear - zFar));
        cameraToClipMatrix.m23(-1.0f);
        cameraToClipMatrix.m32((2 * zFar * zNear) / (zNear - zFar));

        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);
    }


    private float calcFrustumScale(float fovDeg) {
        float fovRad = (float) Math.toRadians(fovDeg);
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


    private void drawGimbal(MatrixStackf currMatrix, GimbalAxis axis, Vector4f baseColor) {
        if (!drawGimbals) return;

        currMatrix.pushMatrix();

        switch (axis) {
            case X_AXIS:
                break;

            case Y_AXIS:
                currMatrix.rotateZ((float) Math.toRadians(90.0f));
                currMatrix.rotateX((float) Math.toRadians(90.0f));
                break;

            case Z_AXIS:
                currMatrix.rotateY((float) Math.toRadians(90.0f));
                currMatrix.rotateX((float) Math.toRadians(90.0f));
                break;
        }

        glUseProgram(theProgram);

        // Set the base color for this object.
        glUniform4f(baseColorUnif, baseColor.x, baseColor.y, baseColor.z, baseColor.w);
        glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.get(mat4Buffer));

        gimbals[axis.ordinal()].render();

        glUseProgram(0);

        currMatrix.popMatrix();
    }
}
