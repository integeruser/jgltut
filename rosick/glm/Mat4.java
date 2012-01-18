package rosick.glm;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;


/**
 * @author xire-
 */
public class Mat4 {
	
	public float[] m;
	
	private FloatBuffer buf;

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Mat4() {
		m = new float[16];
		buf = BufferUtils.createFloatBuffer(16);
	}

	public Mat4(float diagonal) {
		m = new float[16];
		buf = BufferUtils.createFloatBuffer(16);

		m[0] = diagonal;
		m[5] = diagonal;
		m[10] = diagonal;
		m[15] = diagonal;
	}

	public Mat4(float m[]) {
		System.arraycopy(m, 0, this.m, 0, 16);
	}
	
	public Mat4(Mat4 mat) {
		m = new float[16];
		System.arraycopy(mat.m, 0, m, 0, 16);
		buf = BufferUtils.createFloatBuffer(16);
	}


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public void put(int index, float val) {
		m[index] = val;
	}
	
	public void putColumn(int columnIndex, Vec4 vec4) {
		int offset = (columnIndex * 4);
		m[offset]     = vec4.x;
		m[offset + 1] = vec4.y;
		m[offset + 2] = vec4.z;
		m[offset + 3] = vec4.w;
	}


	public void mul(Mat4 mat) {
		float[] res = new float[16];
		float[] m1 = m;
		float[] m2 = mat.m;

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

		System.arraycopy(res, 0, m, 0, 16);
	}
	
	

	public FloatBuffer getBuffer() {
		buf.put(m);
		buf.flip();
		return buf;
	}


	public String toString() {
		String ris = "";

		for (int i = 0; i < 4; i++) {
			ris += m[i*4] + " " + m[i*4+1] + " " + m[i*4+2] + " " + m[i*4+3] + "\n";
		}

		return ris;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	public static Mat4 transpose(Mat4 mat) {
		float source[] = mat.m;
		float destination[] = new float[16];

		for( int i = 0; i < 16; i++ )
			for( int j = 0; j < 16; j++ )
				destination[j+i*16] = source[i+j*16];
		
		return new Mat4(destination);
	}
	
	
	
	public static Mat4 getRotateX(float angoloDeg) {
		float fAngRad = degToRad(angoloDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 mat = new Mat4();

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

	public static Mat4 getRotateY(float angoloDeg) {
		float fAngRad = degToRad(angoloDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 mat = new Mat4();

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

	public static Mat4 getRotateZ(float angoloDeg) {
		float fAngRad = degToRad(angoloDeg);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);

		Mat4 mat = new Mat4();

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
		final float fDegToRad = 3.14159f * 2.0f / 360.0f;

		return fAngDeg * fDegToRad;
	}
}