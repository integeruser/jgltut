package rosick.glutil;

import static rosick.glm.Vec.*;

import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Vec3;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public class MatrixStack {	
	
	private Mat4 currentMatrix;
	private float matrices[];
	private int firstIndexUsable;
		
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public MatrixStack() {
		matrices = new float[1600];													// 100 matrices		
		currentMatrix = new Mat4(1.0f);
				
		firstIndexUsable = 0;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
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
		
	public void setMatrix(Mat4 mat) {
		currentMatrix = new Mat4(mat);
	}
	
	
	public void clear() {
		currentMatrix.clear(1);

		firstIndexUsable = 0;
	}
	
	public void clear(Mat4 mat) {
		currentMatrix = new Mat4(mat);

		firstIndexUsable = 0;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	public void rotateX(float fAngDeg) {
		currentMatrix.mul(Mat4.getRotateX(fAngDeg));
	}

	public void rotateY(float fAngDeg) {
		currentMatrix.mul(Mat4.getRotateY(fAngDeg));
	}

	public void rotateZ(float fAngDeg) {
		currentMatrix.mul(Mat4.getRotateZ(fAngDeg));
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
		scale(vec.get(X), vec.get(Y), vec.get(Z));
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
		translate(vec.get(X), vec.get(Y), vec.get(Z));
	}
	
	
	public void perspective(float fovy, float aspect, float zNear, float zFar) {
		currentMatrix.mul(Glm.perspective(fovy, aspect, zNear, zFar));
	}
}