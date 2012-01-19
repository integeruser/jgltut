package rosick.glutil;

import rosick.glm.Mat4;
import rosick.glm.Vec3;


/**
 * @author integeruser, xire-
 */
public class MatrixStack {	
	
	private Mat4 currentMatrix;
	private float matrices[];
	private int firstIndexUsable;
	
	private Mat4 tempMat;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public MatrixStack() {
		matrices = new float[1600];													// 100 matrices		
		currentMatrix = new Mat4(1);
		
		tempMat = new Mat4();
		
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
		System.arraycopy(currentMatrix.matrix, 0, matrices, firstIndexUsable, 16);		
		firstIndexUsable += 16;
	}

	
	public void pop() {
		// Pop the last matrix pushed in the buffer and set it as currentMatrix
		firstIndexUsable -= 16;
		System.arraycopy(matrices, firstIndexUsable, currentMatrix.matrix, 0, 16);		
	}

	public Mat4 top() {		
		return currentMatrix;
	}
	
	
	public void clear() {
		currentMatrix.clear(1);

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
		tempMat.clear(0);
		tempMat.put(0, x);
		tempMat.put(5, y);
		tempMat.put(10, z);
		tempMat.put(15, 1);

		currentMatrix.mul(tempMat);
	}

	public void scale(Vec3 scale) {
		scale(scale.x, scale.y, scale.z);
	}

	
	public void translate(float x, float y, float z) {
		tempMat.clear(1);
		tempMat.put(12, x);
		tempMat.put(13, y);
		tempMat.put(14, z);

		currentMatrix.mul(tempMat);
	}

	public void translate(Vec3 offset) {
		translate(offset.x, offset.y, offset.z);
	}
}