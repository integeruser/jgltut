package jgltut.jglsdk.glutil;

import jgltut.jglsdk.glm.Glm;
import jgltut.jglsdk.glm.Mat4;
import jgltut.jglsdk.glm.Vec3;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class MatrixStack {
    private Mat4 currentMatrix;
    private float matrices[];
    private int firstIndexUsable;


    public MatrixStack() {
        matrices = new float[160];  // 10 matrices
        currentMatrix = new Mat4(1.0f);
        firstIndexUsable = 0;
    }

    ////////////////////////////////
    public void push() {
        if (firstIndexUsable == matrices.length) {
            // Double the size of matrices[]
            float temp[] = new float[matrices.length * 2];
            System.arraycopy(matrices, 0, temp, 0, matrices.length);
            matrices = temp;
        }

        // Store the currentMatrix in the buffer
        System.arraycopy(currentMatrix.get(), 0, matrices, firstIndexUsable, 16);
        firstIndexUsable += 16;
    }


    public void pop() {
        // Pop the last matrix pushed in the buffer and set it as currentMatrix
        firstIndexUsable -= 16;
        System.arraycopy(matrices, firstIndexUsable, currentMatrix.get(), 0, 16);
    }

    public Mat4 top() {
        return currentMatrix;
    }


    public void applyMatrix(Mat4 mat) {
        currentMatrix.mul(mat);
    }


    public void setIdentity() {
        currentMatrix = new Mat4(1.0f);
    }

    public void setMatrix(Mat4 mat) {
        currentMatrix = new Mat4(mat);
    }


    public void clear() {
        currentMatrix.clear(1.0f);
        firstIndexUsable = 0;
    }

    ////////////////////////////////
    public void rotate(Vec3 axis, float angDegCCW) {
        currentMatrix = Glm.rotate(currentMatrix, angDegCCW, axis);
    }

    public void rotateX(float fAngDeg) {
        currentMatrix.mul(Mat4.getRotateX(fAngDeg));
    }

    public void rotateY(float fAngDeg) {
        currentMatrix.mul(Mat4.getRotateY(fAngDeg));
    }

    public void rotateZ(float fAngDeg) {
        currentMatrix.mul(Mat4.getRotateZ(fAngDeg));
    }


    public void scale(float f) {
        scale(f, f, f);
    }

    public void scale(float x, float y, float z) {
        Mat4 mat = new Mat4();
        mat.clear(0);
        mat.set(0, x);
        mat.set(5, y);
        mat.set(10, z);
        mat.set(15, 1);
        currentMatrix.mul(mat);
    }

    public void scale(Vec3 vec) {
        scale(vec.x, vec.y, vec.z);
    }


    public void translate(float x, float y, float z) {
        Mat4 mat = new Mat4();
        mat.clear(1);
        mat.set(12, x);
        mat.set(13, y);
        mat.set(14, z);
        currentMatrix.mul(mat);
    }

    public void translate(Vec3 vec) {
        translate(vec.x, vec.y, vec.z);
    }


    public void perspective(float fovy, float aspect, float zNear, float zFar) {
        currentMatrix.mul(Glm.perspective(fovy, aspect, zNear, zFar));
    }
}