package rosick.glm;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author xire-
 */
public class Mat3 {
	
	private static final float fDegToRad = 3.14159f * 2.0f / 360.0f;
	
	public float[] matrix;
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Mat3() {
		matrix = new float[9];
		matrix[0] = 1.0f;
		matrix[4] = 1.0f;
		matrix[8] = 1.0f;
	}

	public Mat3(float diagonal) {
		matrix = new float[9];
		matrix[0] = diagonal;
		matrix[4] = diagonal;
		matrix[8] = diagonal;
	}

	public Mat3(float mat[]) {
		matrix = new float[9];
		System.arraycopy(mat, 0, matrix, 0, 9);
	}
	
	public Mat3(Mat3 mat) {
		matrix = new float[9];
		System.arraycopy(mat.matrix, 0, matrix, 0, 9);
	}
	
	public Mat3(Mat4 mat) {
		matrix = new float[9];
		matrix[0] = mat.matrix[0];
		matrix[1] = mat.matrix[1];
		matrix[2] = mat.matrix[2];
		matrix[3] = mat.matrix[4];
		matrix[4] = mat.matrix[5];
		matrix[5] = mat.matrix[6];
		matrix[6] = mat.matrix[8];
		matrix[7] = mat.matrix[9];
		matrix[8] = mat.matrix[10];
	}


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public Mat3 mul(Mat3 mat) {
		float[] res = new float[9];
		float[] m1 = matrix;
		float[] m2 = mat.matrix;

		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) { 
				float sum = 0;
				for (int k = 0; k < 3; k++) {
					float a = m1[r + 3*k];
					float b = m2[3*c + k];
					sum += a * b;
				}
				res[r + 3*c] = sum;
			}
		}

		System.arraycopy(res, 0, matrix, 0, 9);
		
		return this;
	}
	
	
	public void put(int index, float val) {
		matrix[index] = val;
	}
	
	public void putColumn(int columnIndex, Vec3 vec) {
		int offset = (columnIndex * 3);
		
		matrix[offset]     = vec.x;
		matrix[offset + 1] = vec.y;
		matrix[offset + 2] = vec.z;
	}

	
	public void clear(float diagonal) {
		for (int i = 0; i < 9; i++) {
			matrix[i] = 0.0f;
		}
		
		matrix[0] = diagonal;
		matrix[4] = diagonal;
		matrix[8] = diagonal;
	}
	
	public void clear(float[] array) {
		System.arraycopy(array, 0, matrix, 0, 9);		
	}
	
	public void clear(Mat3 mat) {
		clear(mat.matrix);
	}
	

	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(matrix);
		buffer.flip();
		
		return buffer;
	}


	@Override
	public String toString() {
		String ris = "";

		for (int i = 0; i < 3; i++) {
			ris += matrix[i * 3] + " " + matrix[i * 3 + 1] + " " + matrix[i * 3 + 2] + "\n";
		}

		return ris;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public static Vec4 mul(Mat3 mat, Vec4 vec) {
		Vec4 firstColumn = new Vec4(
				mat.matrix[0],
				mat.matrix[1],
				mat.matrix[2],
				mat.matrix[3]);
		
		return firstColumn.mul(vec);
	}
	
	
	public static Mat3 getRotateX(float angDeg) {
		float fAngRad = degToRad(angDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat3 mat = new Mat3();

		// X column
		mat.put(0, 1); 															// x
		mat.put(1, 0); 															// y
		mat.put(2, 0); 															// z

		// Y column
		mat.put(4, 0); 															// x
		mat.put(5, fCos); 														// y
		mat.put(6, fSin); 														// z

		// Z column
		mat.put(8, 0); 															// x
		mat.put(9, -fSin); 														// y
		mat.put(10, fCos); 														// z

		// Last
		mat.put(15, 1); 

		return mat;
	}

	public static Mat3 getRotateY(float angDeg) {
		float fAngRad = degToRad(angDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat3 mat = new Mat3();

		// X column
		mat.put(0, fCos); 
		mat.put(1, 0); 
		mat.put(2, -fSin);

		// Y column
		mat.put(4, 0); 
		mat.put(5, 1); 
		mat.put(6, 0); 

		// Z column
		mat.put(8, fSin); 
		mat.put(9, 0);
		mat.put(10, fCos); 

		// Last
		mat.put(15, 1); 

		return mat;
	}

	public static Mat3 getRotateZ(float angDeg) {
		float fAngRad = degToRad(angDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat3 mat = new Mat3();

		// X column
		mat.put(0, fCos); 
		mat.put(1, fSin); 
		mat.put(2, 0);

		// Y column
		mat.put(4, -fSin);
		mat.put(5, fCos); 
		mat.put(6, 0); 

		// Z column
		mat.put(8, 0); 
		mat.put(9, 0); 
		mat.put(10, 1); 

		// Last
		mat.put(15, 1); 

		return mat;
	}


	private static float degToRad(float fAngDeg) {
		return fAngDeg * fDegToRad;
	}
}