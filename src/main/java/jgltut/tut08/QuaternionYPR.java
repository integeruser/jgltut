package jgltut.tut08;

import jgltut.LWJGLWindow;
import jgltut.framework.Framework;
import jgltut.framework.Mesh;
import jgltut.jglsdk.glm.Glm;
import jgltut.jglsdk.glm.Mat4;
import jgltut.jglsdk.glm.Quaternion;
import jgltut.jglsdk.glm.Vec3;
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
 * SPACE    - switches between right-multiplying the YPR values to the current orientation and left-multiplying them.
 * W,S      - control the outer gimbal.
 * A,D      - control the middle gimbal.
 * Q,E      - control the inner gimbal.
 *
 * @author integeruser
 */
public class QuaternionYPR extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut08/data/";
        new QuaternionYPR().start(500, 500);
    }


    @Override
    protected void init() {
        initializeProgram();

        try {
            ship = new Mesh("Ship.xml");
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
                            rightMultiply = !rightMultiply;
                            System.out.printf(rightMultiply ? "Right-multiply\n" : "Left-multiply\n");
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
        currMatrix.applyMatrix(Glm.mat4Cast(orientation));

        glUseProgram(theProgram);

        currMatrix.scale(3.0f, 3.0f, 3.0f);
        currMatrix.rotateX(-90.0f);

        // Set the base color for this object.
        glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
        glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer(mat4Buffer));

        ship.render("tint");

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
        float lastFrameDuration = getLastFrameDuration() * 10;

        if (isKeyPressed(GLFW_KEY_W)) {
            offsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), SMALL_ANGLE_INCREMENT * lastFrameDuration);
        } else if (isKeyPressed(GLFW_KEY_S)) {
            offsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), -SMALL_ANGLE_INCREMENT * lastFrameDuration);
        }

        if (isKeyPressed(GLFW_KEY_A)) {
            offsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), SMALL_ANGLE_INCREMENT * lastFrameDuration);
        } else if (isKeyPressed(GLFW_KEY_D)) {
            offsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), -SMALL_ANGLE_INCREMENT * lastFrameDuration);
        }

        if (isKeyPressed(GLFW_KEY_Q)) {
            offsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), SMALL_ANGLE_INCREMENT * lastFrameDuration);
        } else if (isKeyPressed(GLFW_KEY_E)) {
            offsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), -SMALL_ANGLE_INCREMENT * lastFrameDuration);
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
    private Mesh ship;

    private Quaternion orientation = new Quaternion(1.0f, 0.0f, 0.0f, 0.0f);

    private boolean rightMultiply = true;


    private void offsetOrientation(Vec3 axis, float angDeg) {
        float angRad = Framework.degToRad(angDeg);

        axis = Glm.normalize(axis);
        axis = Vec3.scale(axis, (float) Math.sin(angRad / 2.0f));

        float scalar = (float) Math.cos(angRad / 2.0f);
        Quaternion offset = new Quaternion(scalar, axis.x, axis.y, axis.z);

        if (rightMultiply) {
            orientation = Quaternion.mul(orientation, offset);
        } else {
            orientation = Quaternion.mul(offset, orientation);
        }

        orientation = Glm.normalize(orientation);
    }
}