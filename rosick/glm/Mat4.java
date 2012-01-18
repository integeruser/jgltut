package rosick.glm;

import java.nio.FloatBuffer;


/**
 * @author xire-
 */
public class Mat4 {
	
	private static final float fDegToRad = 3.14159f * 2.0f / 360.0f;
	
	private static float fAngRad, fCos, fSin;
	private static Mat4 tempMat = new Mat4();

	public float[] matrix;
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Mat4() {
		matrix = new float[16];
	}

	public Mat4(float diagonal) {
		matrix = new float[16];
		matrix[0] = diagonal;
		matrix[5] = diagonal;
		matrix[10] = diagonal;
		matrix[15] = diagonal;
	}

	public Mat4(float m[]) {
		matrix = new float[16];
		System.arraycopy(m, 0, matrix, 0, 16);
	}
	
	public Mat4(Mat4 mat) {
		matrix = new float[16];
		System.arraycopy(mat.matrix, 0, matrix, 0, 16);
	}


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public void mul(Mat4 mat) {
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
	}
	
	
	public void put(int index, float val) {
		matrix[index] = val;
	}
	
	public void putColumn(int columnIndex, Vec4 vec4) {
		int offset = (columnIndex * 4);
		
		matrix[offset]     = vec4.x;
		matrix[offset + 1] = vec4.y;
		matrix[offset + 2] = vec4.z;
		matrix[offset + 3] = vec4.w;
	}

	
	public void clear(float diagonal) {
		matrix[1] = 0;
		matrix[2] = 0;
		matrix[3] = 0;
		matrix[4] = 0;
		matrix[6] = 0;
		matrix[7] = 0;
		matrix[8] = 0;
		matrix[9] = 0;
		matrix[11] = 0;
		matrix[12] = 0;
		matrix[13] = 0;
		matrix[14] = 0;
		
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
	

	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(matrix);
		buffer.flip();
		
		return buffer;
	}


	public String toString() {
		String ris = "";

		for (int i = 0; i < 4; i++) {
			ris += matrix[i * 4] + " " + matrix[i * 4 + 1] + " " + matrix[i * 4 + 2] + " " + matrix[i * 4 + 3] + "\n";
		}

		return ris;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static Mat4 transpose(Mat4 mat) {
		float source[] = mat.matrix;
		float destination[] = new float[16];

		for( int i = 0; i < 16; i++ )
			for( int j = 0; j < 16; j++ )
				destination[j+i*16] = source[i+j*16];
		
		return new Mat4(destination);
	}
	
	
	public static Mat4 getRotateX(float angDeg) {
		fAngRad = degToRad(angDeg);
		fCos = (float) Math.cos(fAngRad);
		fSin = (float) Math.sin(fAngRad);

		tempMat.clear(0);

		// X column
		tempMat.put(0, 1); 															// x
		tempMat.put(1, 0); 															// y
		tempMat.put(2, 0); 															// z

		// Y column
		tempMat.put(4, 0); 															// x
		tempMat.put(5, fCos); 														// y
		tempMat.put(6, fSin); 														// z

		// Z column
		tempMat.put(8, 0); 															// x
		tempMat.put(9, -fSin); 														// y
		tempMat.put(10, fCos); 														// z

		// Last
		tempMat.put(15, 1); 

		return tempMat;
	}

	public static Mat4 getRotateY(float angDeg) {
		fAngRad = degToRad(angDeg);
		fCos = (float) Math.cos(fAngRad);
		fSin = (float) Math.sin(fAngRad);

		tempMat.clear(0);

		// X column
		tempMat.put(0, fCos); 
		tempMat.put(1, 0); 
		tempMat.put(2, -fSin);

		// Y column
		tempMat.put(4, 0); 
		tempMat.put(5, 1); 
		tempMat.put(6, 0); 

		// Z column
		tempMat.put(8, fSin); 
		tempMat.put(9, 0);
		tempMat.put(10, fCos); 

		// Last
		tempMat.put(15, 1); 

		return tempMat;
	}

	public static Mat4 getRotateZ(float angDeg) {
		fAngRad = degToRad(angDeg);
		fCos = (float) Math.cos(fAngRad);
		fSin = (float) Math.sin(fAngRad);

		tempMat.clear(0);

		// X column
		tempMat.put(0, fCos); 
		tempMat.put(1, fSin); 
		tempMat.put(2, 0);

		// Y column
		tempMat.put(4, -fSin);
		tempMat.put(5, fCos); 
		tempMat.put(6, 0); 

		// Z column
		tempMat.put(8, 0); 
		tempMat.put(9, 0); 
		tempMat.put(10, 1); 

		// Last
		tempMat.put(15, 1); 

		return tempMat;
	}


	private static float degToRad(float fAngDeg) {
		return fAngDeg * fDegToRad;
	}
}