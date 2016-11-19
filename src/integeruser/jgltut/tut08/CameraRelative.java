package integeruser.jgltut.tut08;

import integeruser.jgltut.Tutorial;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2008%20Getting%20Oriented/CameraRelative.cpp
 * <p>
 * Part II. Positioning
 * Chapter 8. Getting Oriented
 * <p>
 * SPACE    - toggles between three transforms: model-relative (yaw/pitch/roll-style), world-relative, and
 * camera-relative.
 * W,S      - control the outer gimbal.
 * A,D      - control the middle gimbal.
 * Q,E      - control the inner gimbal.
 * I,K      - move the camera up and down, relative to a center point. Holding SHIFT with these keys will move the
 * camera in smaller increments.
 * J,L      - move the camera left and right around the center point. Holding SHIFT with these keys will move the
 * camera in smaller increments.
 */
public class CameraRelative extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut08/data/";
        new CameraRelative().start(500, 500);
    }


    @Override
    protected void init() {
        initializeProgram();

        ship = new Mesh("Ship.xml");
        plane = new Mesh("UnitPlane.xml");

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_SPACE:
                        int ordinal = (offsetRelative.ordinal() + 1) % OffsetRelative.NUM_RELATIVES.ordinal();
                        offsetRelative = OffsetRelative.values()[ordinal];
                        switch (offsetRelative) {
                            case MODEL_RELATIVE:
                                System.out.printf("Model Relative\n");
                                break;

                            case WORLD_RELATIVE:
                                System.out.printf("World Relative\n");
                                break;

                            case CAMERA_RELATIVE:
                                System.out.printf("Camera Relative\n");
                                break;
                        }
                        break;

                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
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
        final Vector3f camPos = resolveCamPosition();
        currMatrix.mul(calcLookAtMatrix(camPos, camTarget, new Vector3f(0.0f, 1.0f, 0.0f)));

        glUseProgram(theProgram);

        {
            currMatrix.pushMatrix();
            currMatrix.scale(100.0f, 1.0f, 100.0f);

            glUniform4f(baseColorUnif, 0.2f, 0.5f, 0.2f, 1.0f);
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.get(mat4Buffer));

            plane.render();

            currMatrix.popMatrix();
        }

        {
            currMatrix.pushMatrix();
            currMatrix.translate(camTarget);
            currMatrix.mul(orientation.get(new Matrix4f()));
            currMatrix.rotateX((float) Math.toRadians(-90.0f));

            // Set the base color for this object.
            glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, currMatrix.get(mat4Buffer));

            ship.render("tint");

            currMatrix.popMatrix();
        }

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
            offsetOrientation(new Vector3f(1.0f, 0.0f, 0.0f), SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_S)) {
            offsetOrientation(new Vector3f(1.0f, 0.0f, 0.0f), -SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_A)) {
            offsetOrientation(new Vector3f(0.0f, 0.0f, 1.0f), SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_D)) {
            offsetOrientation(new Vector3f(0.0f, 0.0f, 1.0f), -SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }

        if (isKeyPressed(GLFW_KEY_Q)) {
            offsetOrientation(new Vector3f(0.0f, 1.0f, 0.0f), SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        } else if (isKeyPressed(GLFW_KEY_E)) {
            offsetOrientation(new Vector3f(0.0f, 1.0f, 0.0f), -SMALL_ANGLE_INCREMENT * lastFrameDuration * scale);
        }


        if (isKeyPressed(GLFW_KEY_I)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.y = sphereCamRelPos.y - 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.y = sphereCamRelPos.y - 11.25f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_K)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.y = sphereCamRelPos.y + 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.y = sphereCamRelPos.y + 11.25f * lastFrameDuration * scale;
            }
        }

        if (isKeyPressed(GLFW_KEY_J)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.x = sphereCamRelPos.x - 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.x = sphereCamRelPos.x - 11.25f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_L)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.x = sphereCamRelPos.x + 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.x = sphereCamRelPos.x + 11.25f * lastFrameDuration * scale;
            }
        }


        sphereCamRelPos.y = Math.min(Math.max(sphereCamRelPos.y, -78.75f), 10.0f);
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
    private Mesh ship;
    private Mesh plane;

    private Vector3f camTarget = new Vector3f(0.0f, 10.0f, 0.0f);
    private Quaternionf orientation = new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
    private OffsetRelative offsetRelative = OffsetRelative.MODEL_RELATIVE;

    // In spherical coordinates.
    private Vector3f sphereCamRelPos = new Vector3f(90.0f, 0.0f, 66.0f);

    private enum OffsetRelative {
        MODEL_RELATIVE,
        WORLD_RELATIVE,
        CAMERA_RELATIVE,

        NUM_RELATIVES
    }


    private void offsetOrientation(Vector3f axis, float angDeg) {
        float angRad = (float) Math.toRadians(angDeg);

        axis = new Vector3f(axis).normalize();
        axis.mul((float) Math.sin(angRad / 2.0f));

        float scalar = (float) Math.cos(angRad / 2.0f);
        Quaternionf offset = new Quaternionf(axis.x, axis.y, axis.z, scalar);

        switch (offsetRelative) {
            case MODEL_RELATIVE:
                orientation = new Quaternionf(orientation).mul(offset);
                break;

            case WORLD_RELATIVE:
                orientation = new Quaternionf(offset).mul(orientation);
                break;

            case CAMERA_RELATIVE:
                final Vector3f camPos = resolveCamPosition();
                final Matrix4f camMat = calcLookAtMatrix(camPos, camTarget, new Vector3f(0.0f, 1.0f, 0.0f));

                Quaternionf viewQuat = new Quaternionf();
                camMat.getNormalizedRotation(viewQuat);
                Quaternionf invViewQuat = new Quaternionf(viewQuat).conjugate();

                final Quaternionf worldQuat = invViewQuat.mul(offset.mul(viewQuat));
                orientation = new Quaternionf(worldQuat).mul(orientation);
                break;
        }

        orientation.normalize();
    }

    private Vector3f resolveCamPosition() {
        float phi = (float) Math.toRadians(sphereCamRelPos.x);
        float theta = (float) Math.toRadians(sphereCamRelPos.y + 90.0f);

        float sinTheta = (float) Math.sin(theta);
        float cosTheta = (float) Math.cos(theta);
        float cosPhi = (float) Math.cos(phi);
        float sinPhi = (float) Math.sin(phi);

        Vector3f dirToCamera = new Vector3f(sinTheta * cosPhi, cosTheta, sinTheta * sinPhi);
        return (dirToCamera.mul(sphereCamRelPos.z)).add(camTarget);
    }

    private Matrix4f calcLookAtMatrix(Vector3f cameraPt, Vector3f lookPt, Vector3f upPt) {
        Vector3f lookDir = new Vector3f(lookPt).sub(cameraPt).normalize();
        Vector3f upDir = new Vector3f(upPt).normalize();

        Vector3f rightDir = new Vector3f(lookDir).cross(upDir).normalize();
        Vector3f perpUpDir = new Vector3f(rightDir).cross(lookDir);

        Matrix4f rotMat = new Matrix4f();
        rotMat.m00(rightDir.x);
        rotMat.m01(rightDir.y);
        rotMat.m02(rightDir.z);
        rotMat.m10(perpUpDir.x);
        rotMat.m11(perpUpDir.y);
        rotMat.m12(perpUpDir.z);
        rotMat.m20(-lookDir.x);
        rotMat.m21(-lookDir.y);
        rotMat.m22(-lookDir.z);
        rotMat.transpose();

        Matrix4f transMat = new Matrix4f().setTranslation(-cameraPt.x, -cameraPt.y, -cameraPt.z);
        return rotMat.mul(transMat);
    }
}
