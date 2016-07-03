package jgltut.tut06;

import jgltut.LWJGLWindow;
import jgltut.framework.Framework;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part II. Positioning
 * Chapter 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * <p>
 * Node Angle      Increase/Left   Decrease/Right
 * Base Spin              D               A
 * Arm Raise              S               W
 * Elbow Raise            F               R
 * Wrist Raise            G               T
 * Wrist Spin             C               Z
 * Finger                 Q               E
 * <p>
 * SPACE   - print current armature position.
 *
 * @author integeruser
 */
public class Hierarchy extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut06/data/";
        new Hierarchy().start(700, 700);
    }


    @Override
    protected void init() {
        initializeProgram();
        initializeVAO();

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
                            armature.writePose();
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

        armature.draw();
    }

    @Override
    protected void reshape(int w, int h) {
        cameraToClipMatrix.m00(frustumScale / (w / (float) h));
        cameraToClipMatrix.m11(frustumScale);

        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        if (isKeyPressed(GLFW_KEY_A)) {
            armature.adjBase(false);
        } else if (isKeyPressed(GLFW_KEY_D)) {
            armature.adjBase(true);
        }

        if (isKeyPressed(GLFW_KEY_W)) {
            armature.adjUpperArm(false);
        } else if (isKeyPressed(GLFW_KEY_S)) {
            armature.adjUpperArm(true);
        }

        if (isKeyPressed(GLFW_KEY_R)) {
            armature.adjLowerArm(false);
        } else if (isKeyPressed(GLFW_KEY_F)) {
            armature.adjLowerArm(true);
        }

        if (isKeyPressed(GLFW_KEY_T)) {
            armature.adjWristPitch(false);
        } else if (isKeyPressed(GLFW_KEY_G)) {
            armature.adjWristPitch(true);
        }

        if (isKeyPressed(GLFW_KEY_Z)) {
            armature.adjWristRoll(false);
        } else if (isKeyPressed(GLFW_KEY_C)) {
            armature.adjWristRoll(true);
        }

        if (isKeyPressed(GLFW_KEY_Q)) {
            armature.adjFingerOpen(true);
        } else if (isKeyPressed(GLFW_KEY_E)) {
            armature.adjFingerOpen(false);
        }
    }

    ////////////////////////////////
    private int theProgram;
    private int positionAttrib;
    private int colorAttrib;

    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;

    private Matrix4f cameraToClipMatrix = new Matrix4f();
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);

    private final float frustumScale = calcFrustumScale(45.0f);


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "PosColorLocalTransform.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorPassthrough.frag"));
        theProgram = Framework.createProgram(shaderList);

        positionAttrib = glGetAttribLocation(theProgram, "position");
        colorAttrib = glGetAttribLocation(theProgram, "color");

        modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");

        float zNear = 1.0f;
        float zFar = 100.0f;
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
        final float degToRad = 3.14159f * 2.0f / 360.0f;
        float fovRad = fovDeg * degToRad;
        return 1.0f / (float) (Math.tan(fovRad / 2.0f));
    }

    ////////////////////////////////
    private final float vertexData[] = {
            // Front
            +1.0f, +1.0f, +1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,

            // Top
            +1.0f, +1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,

            // Left
            +1.0f, +1.0f, +1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,

            // Back
            +1.0f, +1.0f, -1.0f,
            -1.0f, +1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,

            // Bottom
            +1.0f, -1.0f, +1.0f,
            +1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, +1.0f,

            // Right
            -1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, +1.0f, -1.0f,


            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR
            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR
            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR
            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR

            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR
            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR
            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR
            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR

            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR

            1.0f, 1.0f, 0.0f, 1.0f,  // YELLOW_COLOR
            1.0f, 1.0f, 0.0f, 1.0f,  // YELLOW_COLOR
            1.0f, 1.0f, 0.0f, 1.0f,  // YELLOW_COLOR
            1.0f, 1.0f, 0.0f, 1.0f,  // YELLOW_COLOR

            0.0f, 1.0f, 1.0f, 1.0f,  // CYAN_COLOR
            0.0f, 1.0f, 1.0f, 1.0f,  // CYAN_COLOR
            0.0f, 1.0f, 1.0f, 1.0f,  // CYAN_COLOR
            0.0f, 1.0f, 1.0f, 1.0f,  // CYAN_COLOR

            1.0f, 0.0f, 1.0f, 1.0f,  // MAGENTA_COLOR
            1.0f, 0.0f, 1.0f, 1.0f,  // MAGENTA_COLOR
            1.0f, 0.0f, 1.0f, 1.0f,  // MAGENTA_COLOR
            1.0f, 0.0f, 1.0f, 1.0f   // MAGENTA_COLOR
    };

    private final short indexData[] = {
            0, 1, 2,
            2, 3, 0,

            4, 5, 6,
            6, 7, 4,

            8, 9, 10,
            10, 11, 8,

            12, 13, 14,
            14, 15, 12,

            16, 17, 18,
            18, 19, 16,

            20, 21, 22,
            22, 23, 20
    };

    private int vao;


    private void initializeVAO() {
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
        vertexDataBuffer.put(vertexData);
        vertexDataBuffer.flip();

        int vertexBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        ShortBuffer indexDataBuffer = BufferUtils.createShortBuffer(indexData.length);
        indexDataBuffer.put(indexData);
        indexDataBuffer.flip();

        int indexBufferObject = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glEnableVertexAttribArray(positionAttrib);
        glEnableVertexAttribArray(colorAttrib);
        glVertexAttribPointer(positionAttrib, 3, GL_FLOAT, false, 0, 0);
        final int numberOfVertices = 24;
        int colorDataOffset = FLOAT_SIZE * 3 * numberOfVertices;
        glVertexAttribPointer(colorAttrib, 4, GL_FLOAT, false, 0, colorDataOffset);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

        glBindVertexArray(0);
    }

    ////////////////////////////////
    private final float STANDARD_ANGLE_INCREMENT = 11.25f;
    private final float SMALL_ANGLE_INCREMENT = 9.0f;

    private Armature armature = new Armature();

    private class Armature {
        Vector3f posBase;
        float angBase;

        Vector3f posBaseLeft, posBaseRight;
        float scaleBaseZ;

        float angUpperArm;
        float sizeUpperArm;

        Vector3f posLowerArm;
        float angLowerArm;
        float lenLowerArm;
        float widthLowerArm;

        Vector3f posWrist;
        float angWristRoll;
        float angWristPitch;
        float lenWrist;
        float widthWrist;

        Vector3f posLeftFinger, posRightFinger;
        float angFingerOpen;
        float lenFinger;
        float widthFinger;
        float angLowerFinger;


        Armature() {
            posBase = new Vector3f(3.0f, -5.0f, -40.0f);
            angBase = -45.0f;

            posBaseLeft = new Vector3f(2.0f, 0.0f, 0.0f);
            posBaseRight = new Vector3f(-2.0f, 0.0f, 0.0f);
            scaleBaseZ = 3.0f;

            angUpperArm = -70.75f;
            sizeUpperArm = 9.0f;

            posLowerArm = new Vector3f(0.0f, 0.0f, 8.0f);
            angLowerArm = 60.25f;
            lenLowerArm = 5.0f;
            widthLowerArm = 1.5f;

            posWrist = new Vector3f(0.0f, 0.0f, 5.0f);
            angWristRoll = 0.0f;
            angWristPitch = 67.5f;
            lenWrist = 2.0f;
            widthWrist = 2.0f;

            posLeftFinger = new Vector3f(1.0f, 0.0f, 1.0f);
            posRightFinger = new Vector3f(-1.0f, 0.0f, 1.0f);
            angFingerOpen = 45.0f;
            lenFinger = 2.0f;
            widthFinger = 0.5f;
            angLowerFinger = 45.0f;
        }


        void draw() {
            MatrixStackf modelToCameraStack = new MatrixStackf(10);

            glUseProgram(theProgram);
            glBindVertexArray(vao);

            modelToCameraStack.translate(posBase);
            modelToCameraStack.rotateY(degToRad(angBase));

            // Draw left base.
            {
                modelToCameraStack.pushMatrix();
                modelToCameraStack.translate(posBaseLeft);
                modelToCameraStack.scale(new Vector3f(1.0f, 1.0f, scaleBaseZ));
                glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
                glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.popMatrix();
            }

            // Draw right base.
            {
                modelToCameraStack.pushMatrix();
                modelToCameraStack.translate(posBaseRight);
                modelToCameraStack.scale(new Vector3f(1.0f, 1.0f, scaleBaseZ));
                glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
                glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.popMatrix();
            }

            // Draw main arm.
            drawUpperArm(modelToCameraStack);

            glBindVertexArray(0);
            glUseProgram(0);
        }

        void drawUpperArm(MatrixStackf modelToCameraStack) {
            modelToCameraStack.pushMatrix();
            modelToCameraStack.rotateX(degToRad(angUpperArm));

            {
                modelToCameraStack.pushMatrix();
                modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, (sizeUpperArm / 2.0f) - 1.0f));
                modelToCameraStack.scale(new Vector3f(1.0f, 1.0f, sizeUpperArm / 2.0f));
                glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
                glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.popMatrix();
            }

            drawLowerArm(modelToCameraStack);

            modelToCameraStack.popMatrix();
        }

        void drawLowerArm(MatrixStackf modelToCameraStack) {
            modelToCameraStack.pushMatrix();
            modelToCameraStack.translate(posLowerArm);
            modelToCameraStack.rotateX(degToRad(angLowerArm));

            modelToCameraStack.pushMatrix();
            modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, lenLowerArm / 2.0f));
            modelToCameraStack.scale(new Vector3f(widthLowerArm / 2.0f, widthLowerArm / 2.0f, lenLowerArm / 2.0f));
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
            glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.popMatrix();

            drawWrist(modelToCameraStack);

            modelToCameraStack.popMatrix();
        }

        void drawWrist(MatrixStackf modelToCameraStack) {
            modelToCameraStack.pushMatrix();
            modelToCameraStack.translate(posWrist);
            modelToCameraStack.rotateZ(degToRad(angWristRoll));
            modelToCameraStack.rotateX(degToRad(angWristPitch));

            modelToCameraStack.pushMatrix();
            modelToCameraStack.scale(new Vector3f(widthWrist / 2.0f, widthWrist / 2.0f, lenWrist / 2.0f));
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
            glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.popMatrix();

            drawFingers(modelToCameraStack);

            modelToCameraStack.popMatrix();
        }

        void drawFingers(MatrixStackf modelToCameraStack) {
            // Draw left finger
            modelToCameraStack.pushMatrix();
            modelToCameraStack.translate(posLeftFinger);
            modelToCameraStack.rotateY(degToRad(angFingerOpen));

            modelToCameraStack.pushMatrix();
            modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, lenFinger / 2.0f));
            modelToCameraStack.scale(new Vector3f(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
            glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.popMatrix();

            {
                // Draw left lower finger
                modelToCameraStack.pushMatrix();
                modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, lenFinger));
                modelToCameraStack.rotateY(degToRad(-angLowerFinger));

                modelToCameraStack.pushMatrix();
                modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, lenFinger / 2.0f));
                modelToCameraStack.scale(new Vector3f(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
                glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
                glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.popMatrix();

                modelToCameraStack.popMatrix();
            }

            modelToCameraStack.popMatrix();

            // Draw right finger
            modelToCameraStack.pushMatrix();
            modelToCameraStack.translate(posRightFinger);
            modelToCameraStack.rotateY(degToRad(-angFingerOpen));

            modelToCameraStack.pushMatrix();
            modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, lenFinger / 2.0f));
            modelToCameraStack.scale(new Vector3f(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
            glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
            modelToCameraStack.popMatrix();

            {
                // Draw right lower finger
                modelToCameraStack.pushMatrix();
                modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, lenFinger));
                modelToCameraStack.rotateY(degToRad(angLowerFinger));

                modelToCameraStack.pushMatrix();
                modelToCameraStack.translate(new Vector3f(0.0f, 0.0f, lenFinger / 2.0f));
                modelToCameraStack.scale(new Vector3f(widthFinger / 2.0f, widthFinger / 2.0f, lenFinger / 2.0f));
                glUniformMatrix4fv(modelToCameraMatrixUnif, false, modelToCameraStack.get(mat4Buffer));
                glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
                modelToCameraStack.popMatrix();

                modelToCameraStack.popMatrix();
            }

            modelToCameraStack.popMatrix();
        }


        void adjBase(boolean increment) {
            final float scale = 5;
            angBase += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale : -STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale;
            angBase = angBase % 360.0f;
        }

        void adjUpperArm(boolean increment) {
            final float scale = 5;
            angUpperArm += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale : -STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale;
            angUpperArm = clamp(angUpperArm, -90.0f, 0.0f);
        }

        void adjLowerArm(boolean increment) {
            final float scale = 5;
            angLowerArm += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale : -STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale;
            angLowerArm = clamp(angLowerArm, 0.0f, 146.25f);
        }

        void adjWristPitch(boolean increment) {
            final float scale = 5;
            angWristPitch += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale : -STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale;
            angWristPitch = clamp(angWristPitch, 0.0f, 90.0f);
        }

        void adjWristRoll(boolean increment) {
            final float scale = 5;
            angWristRoll += increment ? STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale : -STANDARD_ANGLE_INCREMENT * lastFrameDuration * scale;
            angWristRoll = angWristRoll % 360.0f;
        }

        void adjFingerOpen(boolean increment) {
            final float scale = 5;
            angFingerOpen += increment ? SMALL_ANGLE_INCREMENT * lastFrameDuration * scale : -SMALL_ANGLE_INCREMENT * lastFrameDuration * scale;
            angFingerOpen = clamp(angFingerOpen, 9.0f, 90.0f);
        }


        void writePose() {
            System.out.printf("angBase:\t%f\n", angBase);
            System.out.printf("angUpperArm:\t%f\n", angUpperArm);
            System.out.printf("angLowerArm:\t%f\n", angLowerArm);
            System.out.printf("angWristPitch:\t%f\n", angWristPitch);
            System.out.printf("angWristRoll:\t%f\n", angWristRoll);
            System.out.printf("angFingerOpen:\t%f\n", angFingerOpen);
            System.out.printf("\n");
        }
    }


    private float degToRad(float angDeg) {
        final float degToRad = 3.14159f * 2.0f / 360.0f;
        return angDeg * degToRad;
    }


    private float clamp(float value, float minValue, float maxValue) {
        if (value < minValue) return minValue;
        if (value > maxValue) return maxValue;
        return value;
    }
}