package rosick.glutil;

import java.util.ArrayDeque;
import java.util.Deque;

import rosick.glm.Mat4;
import rosick.glm.Vec3;


/**
 * @author xire-
 */
public class MatrixStack {
	
	private Mat4 m_currMat;
	private Deque<Mat4> m_matrices;

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public MatrixStack() {
		m_matrices = new ArrayDeque<>();
		m_currMat = new Mat4(1);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public void push() {
		m_matrices.push(m_currMat);
		m_currMat = new Mat4(m_currMat);
	}

	public void pop() {
		m_currMat = m_matrices.pop();
	}

	public Mat4 top() {
		return m_currMat;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	public void rotateX(float fAngDeg) {
		m_currMat.mul(Mat4.getRotateX(fAngDeg));
	}

	public void rotateY(float fAngDeg) {
		m_currMat.mul(Mat4.getRotateY(fAngDeg));
	}

	public void rotateZ(float fAngDeg) {
		m_currMat.mul(Mat4.getRotateZ(fAngDeg));
	}

	
	public void scale(float x, float y, float z) {
		Mat4 m = new Mat4();
		m.put(0, x);
		m.put(5, y);
		m.put(10, z);
		m.put(15, 1);

		m_currMat.mul(m);
	}

	public void scale(Vec3 scale) {
		Mat4 m = new Mat4();
		m.put(0, scale.x);
		m.put(5, scale.y);
		m.put(10, scale.z);
		m.put(15, 1);

		m_currMat.mul(m);
	}

	
	public void translate(float x, float y, float z) {
		Mat4 m = new Mat4(1.0f);
		m.put(12, x);
		m.put(13, y);
		m.put(14, z);

		m_currMat.mul(m);
	}

	public void translate(Vec3 offset) {
		Mat4 m = new Mat4(1.0f);
		m.put(12, offset.x);
		m.put(13, offset.y);
		m.put(14, offset.z);

		m_currMat.mul(m);
	}
	
	
	public void setMatrix(Mat4 mat) {
		for (int i = 0; i < 16; i++) {
			m_currMat.m[i] = mat.m[i];
		}
	}
}