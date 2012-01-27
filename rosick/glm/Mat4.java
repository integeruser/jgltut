package rosick.glm;

import static rosick.glm.Vec4.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public class Mat4 extends Mat implements Bufferable {
	
	public Mat4() {
		matrix = new float[16];
		
		matrix[0] 	= 1.0f;
		matrix[5] 	= 1.0f;
		matrix[10] 	= 1.0f;
		matrix[15] 	= 1.0f;
	}

	public Mat4(float diagonal) {
		matrix = new float[16];
		
		matrix[0] 	= diagonal;
		matrix[5] 	= diagonal;
		matrix[10] 	= diagonal;
		matrix[15] 	= diagonal;
	}

	public Mat4(float mat[]) {
		matrix = new float[16];
		
		System.arraycopy(mat, 0, matrix, 0, 16);
	}
	
	public Mat4(Mat3 rotMatrix) {
		matrix = new float[16];

		matrix[0] = rotMatrix.matrix[0];
		matrix[1] = rotMatrix.matrix[1];
		matrix[2] = rotMatrix.matrix[2];
		matrix[3] = 0.0f;
		
		matrix[4] = rotMatrix.matrix[3];
		matrix[5] = rotMatrix.matrix[4];
		matrix[6] = rotMatrix.matrix[5];
		matrix[7] = 0.0f;
		
		matrix[8] = rotMatrix.matrix[6];
		matrix[9] = rotMatrix.matrix[7];
		matrix[10] = rotMatrix.matrix[8];
		matrix[11] = 0.0f;
		
		matrix[12] = 0.0f;
		matrix[13] = 0.0f;
		matrix[14] = 0.0f;
		matrix[15] = 1.0f;
	}
	
	public Mat4(Mat4 mat) {
		matrix = new float[16];
		
		System.arraycopy(mat.matrix, 0, matrix, 0, 16);
	}
	
	public Mat4(Vec4 column0, Vec4 column1, Vec4 column2, Vec4 column3) {
		matrix = new float[16];

		matrix[0] = column0.vector[X];
		matrix[1] = column0.vector[Y];
		matrix[2] = column0.vector[Z];
		matrix[3] = column0.vector[W];
		
		matrix[4] = column1.vector[X];
		matrix[5] = column1.vector[Y];
		matrix[6] = column1.vector[Z];
		matrix[7] = column1.vector[W];
		
		matrix[8] = column2.vector[X];
		matrix[9] = column2.vector[Y];
		matrix[10] = column2.vector[Z];
		matrix[11] = column2.vector[W];
		
		matrix[12] = column3.vector[X];
		matrix[13] = column3.vector[Y];
		matrix[14] = column3.vector[Z];
		matrix[15] = column3.vector[W];
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public Vec4 getColumn(int columnIndex) {		
		int offset = (columnIndex * 4);
		
		Vec4 res = new Vec4();
		res.vector[X] = matrix[offset];
		res.vector[Y] = matrix[offset + 1];
		res.vector[Z] = matrix[offset + 2];
		res.vector[W] = matrix[offset + 3];
		
		return res;
	}
	
	
	public void setColumn(int columnIndex, Vec4 vec) {
		int offset = (columnIndex * 4);
		
		matrix[offset]     = vec.vector[X];
		matrix[offset + 1] = vec.vector[Y];
		matrix[offset + 2] = vec.vector[Z];
		matrix[offset + 3] = vec.vector[W];
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Mat4 mul(Mat4 mat) {
		float[] res = new float[16];
		float[] m1 = matrix;
		float[] m2 = mat.matrix;

		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) { 
				float sum = 0;
				
				for (int k = 0; k < 4; k++) {
					float a = m1[r + 4*k];
					float b = m2[4*c + k];
					sum += a * b;
				}
				
				res[r + 4*c] = sum;
			}
		}

		System.arraycopy(res, 0, matrix, 0, 16);
		
		return this;
	}
	
	
	public Mat4 scale(float scalar) {
		for (int i = 0; i < 16; i++) {
			matrix[i] = matrix[i] * scalar;
		}
		
		return this;
	}
	

	public void clear(float diagonal) {
		for (int i = 0; i < 16; i++) {
			matrix[i] = 0.0f;
		}
		
		matrix[0] = diagonal;
		matrix[5] = diagonal;
		matrix[10] = diagonal;
		matrix[15] = diagonal;
	}
	
	public void clear(float[] array) {
		System.arraycopy(array, 0, matrix, 0, 16);		
	}
	
	public void clear(Mat4 mat) {
		clear(mat.matrix);
	}


	@Override
	public String toString() {
		String res = "";

		for (int i = 0; i < 4; i++) {
			res += matrix[i * 4] + " " + matrix[i * 4 + 1] + " " + matrix[i * 4 + 2] + " " + matrix[i * 4 + 3] + "\n";
		}

		return res;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public static Vec4 mul(Mat4 mat, Vec4 vec) {
		Vec4 res = new Vec4();
		
		for (int i = 0; i < 4; i++) {
			res.vector[X] += mat.matrix[4*i + 0] * vec.get(i);
			res.vector[Y] += mat.matrix[4*i + 1] * vec.get(i);
			res.vector[Z] += mat.matrix[4*i + 2] * vec.get(i);
			res.vector[W] += mat.matrix[4*i + 3] * vec.get(i);
		}

		return res;
	}
	
	
	public static Mat4 getRotateX(float angDeg) {
		float fAngRad = (float) Math.toRadians(angDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 res = new Mat4();

		// X column
		res.matrix[0] 	= 1; 														// x
		res.matrix[1] 	= 0; 														// y
		res.matrix[2] 	= 0; 														// z

		// Y column
		res.matrix[4] 	= 0; 														// x
		res.matrix[5] 	= fCos; 													// y
		res.matrix[6] 	= fSin; 													// z

		// Z column
		res.matrix[8] 	= 0; 														// x
		res.matrix[9] 	= -fSin; 													// y
		res.matrix[10] 	= fCos; 													// z

		// Last
		res.matrix[15] 	= 1; 

		return res;
	}

	public static Mat4 getRotateY(float angDeg) {
		float fAngRad = (float) Math.toRadians(angDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 res = new Mat4();

		// X column
		res.matrix[0] 	= fCos; 
		res.matrix[1] 	= 0; 
		res.matrix[2] 	= -fSin;

		// Y column
		res.matrix[4] 	= 0; 
		res.matrix[5] 	= 1; 
		res.matrix[6] 	= 0; 

		// Z column
		res.matrix[8] 	= fSin; 
		res.matrix[9] 	= 0;
		res.matrix[10] 	= fCos; 

		// Last
		res.matrix[15] 	= 1; 

		return res;
	}

	public static Mat4 getRotateZ(float angDeg) {
		float fAngRad = (float) Math.toRadians(angDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 res = new Mat4();

		// X column
		res.matrix[0] 	= fCos; 
		res.matrix[1] 	= fSin; 
		res.matrix[2] 	= 0;

		// Y column
		res.matrix[4] 	= -fSin;
		res.matrix[5] 	= fCos; 
		res.matrix[6] 	= 0; 

		// Z column
		res.matrix[8] 	= 0; 
		res.matrix[9] 	= 0; 
		res.matrix[10] 	= 1; 

		// Last
		res.matrix[15] 	= 1; 

		return res;
	}
}