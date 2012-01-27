package rosick.glm;

import static rosick.glm.Vec.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Glm {
	
	public static float clamp(float x, float minVal, float maxVal) {
		return Math.min(Math.max(x, minVal), maxVal);
	}
	
	
	public static float length(Vec3 vec) {	
		return (float) Math.sqrt(dot(vec, vec));
	}
	
	public static float length(Vec4 vec) {
		return (float) Math.sqrt(dot(vec, vec));
	}
	
	public static float length(Quaternion quat) {
		return (float) Math.sqrt(dot(quat, quat));
	}
	
	
	public static Vec3 normalize(Vec3 vec) {
		Vec3 res = new Vec3();
		
		float invLength = 1.0f / length(vec);
		res.vector[X] = vec.vector[X] * invLength;
		res.vector[Y] = vec.vector[Y] * invLength;
		res.vector[Z] = vec.vector[Z] * invLength;
		
		return res;
	}
	
	public static Vec4 normalize(Vec4 vec) {
		Vec4 res = new Vec4();
		
		float invLength = 1.0f / length(vec);
		res.vector[X] = vec.vector[X] * invLength;
		res.vector[Y] = vec.vector[Y] * invLength;
		res.vector[Z] = vec.vector[Z] * invLength;
		res.vector[W] = vec.vector[W] * invLength;

		return res;
	}
	
	public static Quaternion normalize(Quaternion quat) {		
		Quaternion res = new Quaternion();
		
		float invLength = 1.0f / length(quat);
		res.vector[X] = quat.vector[X] * invLength;
		res.vector[Y] = quat.vector[Y] * invLength;
		res.vector[Z] = quat.vector[Z] * invLength;
		res.vector[W] = quat.vector[W] * invLength;

		return res;
	}
	
	
	public static float dot(Vec3 a, Vec3 b) {
		return a.vector[X] * b.vector[X] + a.vector[Y] * b.vector[Y] + a.vector[Z] * b.vector[Z];
	}
	
	public static float dot(Vec4 a, Vec4 b) {
		return a.vector[X] * b.vector[X] + a.vector[Y] * b.vector[Y] + a.vector[Z] * b.vector[Z] + a.vector[W] * b.vector[W];
	}
	
	public static float dot(Quaternion a, Quaternion b) {
		return a.vector[X] * b.vector[X] + a.vector[Y] * b.vector[Y] + a.vector[Z] * b.vector[Z] + a.vector[W] * b.vector[W];
	}
	
	
	public static Vec3 cross(Vec3 a, Vec3 b) {
		Vec3 res = new Vec3();

		res.vector[X] = a.vector[Y] * b.vector[Z] - a.vector[Z] * b.vector[Y];
		res.vector[Y] = a.vector[Z] * b.vector[X] - a.vector[X] * b.vector[Z];
		res.vector[Z] = a.vector[X] * b.vector[Y] - a.vector[Y] * b.vector[X];

		return res;
	}
		
	
	public static float mix(float x, float y, float a) {
		return x + a * (y - x);
	}
	
	public static Vec4 mix(Vec4 x, Vec4 y, float a) {
		return Vec4.add(x, Vec4.sub(y, x).scale(a));
	}
	
	
	public static Quaternion conjugate(Quaternion quat) {
        return Quaternion.conjugate(quat);
	}
	
	
	public static Quaternion angleAxis(float angle, Vec3 vec) {
		Quaternion res = new Quaternion();

		float a = (float) Math.toRadians(angle);
        float s = (float) Math.sin(a * 0.5);

        res.vector[X] = vec.vector[X] * s;
        res.vector[Y] = vec.vector[Y] * s;
        res.vector[Z] = vec.vector[Z] * s;
        res.vector[W] = (float) Math.cos(a * 0.5);

        return res;
	}
	
	
	public static Quaternion quatCast(Mat4 mat) {
		float fourXSquaredMinus1 = mat.matrix[0] 	- mat.matrix[5] 	- mat.matrix[10];
		float fourYSquaredMinus1 = mat.matrix[5] 	- mat.matrix[0] 	- mat.matrix[10];
		float fourZSquaredMinus1 = mat.matrix[10] 	- mat.matrix[0] 	- mat.matrix[5];
		float fourWSquaredMinus1 = mat.matrix[0] 	+ mat.matrix[5] 	+ mat.matrix[10];

		int biggestIndex = 0;
	    float fourBiggestSquaredMinus1 = fourWSquaredMinus1;
	    
        if (fourXSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourXSquaredMinus1;
            biggestIndex = 1;
        }
        
        if (fourYSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourYSquaredMinus1;
            biggestIndex = 2;
        }
        
        if (fourZSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourZSquaredMinus1;
            biggestIndex = 3;
        }

	    float biggestVal = (float) (Math.sqrt(fourBiggestSquaredMinus1 + 1) * 0.5f);
	    float mult = 0.25f / biggestVal;

		Quaternion res = new Quaternion();

		switch (biggestIndex) {
			case 0:
				res.vector[W] = biggestVal; 
				res.vector[X] = (mat.matrix[6] - mat.matrix[9]) * mult;
				res.vector[Y] = (mat.matrix[8] - mat.matrix[2]) * mult;
				res.vector[Z] = (mat.matrix[1] - mat.matrix[4]) * mult;
				break;
			case 1:
				res.vector[W] = (mat.matrix[6] - mat.matrix[9]) * mult;
				res.vector[X] = biggestVal;
				res.vector[Y] = (mat.matrix[1] + mat.matrix[4]) * mult;
				res.vector[Z] = (mat.matrix[8] + mat.matrix[2]) * mult;
				break;
			case 2:
				res.vector[W] = (mat.matrix[8] - mat.matrix[2]) * mult;
				res.vector[X] = (mat.matrix[1] + mat.matrix[4]) * mult;
				res.vector[Y] = biggestVal;
				res.vector[Z] = (mat.matrix[6] + mat.matrix[9]) * mult;
				break;
			case 3:
				res.vector[W] = (mat.matrix[1] - mat.matrix[4]) * mult;
				res.vector[X] = (mat.matrix[8] + mat.matrix[2]) * mult;
				res.vector[Y] = (mat.matrix[6] + mat.matrix[9]) * mult;
				res.vector[Z] = biggestVal;
				break;
		}   
		
		return res;
	}
	
	
	public static Mat3 inverse(Mat3 mat) {	
		Mat3 res = new Mat3();
		
		res.matrix[0] = + (mat.matrix[4] * mat.matrix[8] - mat.matrix[7] * mat.matrix[5]);
		res.matrix[3] = - (mat.matrix[3] * mat.matrix[8] - mat.matrix[6] * mat.matrix[5]);
		res.matrix[6] = + (mat.matrix[3] * mat.matrix[7] - mat.matrix[6] * mat.matrix[4]);
		res.matrix[1] = - (mat.matrix[1] * mat.matrix[8] - mat.matrix[7] * mat.matrix[2]);
		res.matrix[4] = + (mat.matrix[0] * mat.matrix[8] - mat.matrix[6] * mat.matrix[2]);
		res.matrix[7] = - (mat.matrix[0] * mat.matrix[7] - mat.matrix[6] * mat.matrix[1]);
		res.matrix[2] = + (mat.matrix[1] * mat.matrix[5] - mat.matrix[4] * mat.matrix[2]);
		res.matrix[5] = - (mat.matrix[0] * mat.matrix[5] - mat.matrix[3] * mat.matrix[2]);
		res.matrix[8] = + (mat.matrix[0] * mat.matrix[4] - mat.matrix[3] * mat.matrix[1]);
		
		float determinant = mat.matrix[0] * (mat.matrix[4] * mat.matrix[8] - mat.matrix[5] * mat.matrix[7]) 
				+ mat.matrix[1] * (mat.matrix[5] * mat.matrix[6] - mat.matrix[3] * mat.matrix[8])
	            + mat.matrix[2] * (mat.matrix[3] * mat.matrix[7] - mat.matrix[4] * mat.matrix[6]);
		
		return res.scale(1.0f / determinant);	
	}
	
	public static Mat4 inverse(Mat4 mat) {
		float Coef00 = mat.matrix[10] * mat.matrix[15] - mat.matrix[14] * mat.matrix[11];
		float Coef02 = mat.matrix[6] * mat.matrix[15] - mat.matrix[14] * mat.matrix[7];
		float Coef03 = mat.matrix[6] * mat.matrix[11] - mat.matrix[10] * mat.matrix[7];

		float Coef04 = mat.matrix[9] * mat.matrix[15] - mat.matrix[13] * mat.matrix[11];
		float Coef06 = mat.matrix[5] * mat.matrix[15] - mat.matrix[13] * mat.matrix[7];
		float Coef07 = mat.matrix[5] * mat.matrix[11] - mat.matrix[9] * mat.matrix[7];

		float Coef08 = mat.matrix[9] * mat.matrix[14] - mat.matrix[13] * mat.matrix[10];
		float Coef10 = mat.matrix[5] * mat.matrix[14] - mat.matrix[13] * mat.matrix[6];
		float Coef11 = mat.matrix[5] * mat.matrix[10] - mat.matrix[9] * mat.matrix[6];

		float Coef12 = mat.matrix[8] * mat.matrix[15] - mat.matrix[12] * mat.matrix[11];
		float Coef14 = mat.matrix[4] * mat.matrix[15] - mat.matrix[12] * mat.matrix[7];
		float Coef15 = mat.matrix[4] * mat.matrix[11] - mat.matrix[8] * mat.matrix[7];

		float Coef16 = mat.matrix[8] * mat.matrix[14] - mat.matrix[12] * mat.matrix[10];
		float Coef18 = mat.matrix[4] * mat.matrix[14] - mat.matrix[12] * mat.matrix[6];
		float Coef19 = mat.matrix[4] * mat.matrix[10] - mat.matrix[8] * mat.matrix[6];

		float Coef20 = mat.matrix[8] * mat.matrix[13] - mat.matrix[12] * mat.matrix[9];
		float Coef22 = mat.matrix[4] * mat.matrix[13] - mat.matrix[12] * mat.matrix[5];
		float Coef23 = mat.matrix[4] * mat.matrix[9]  - mat.matrix[8] * mat.matrix[5];

		final Vec4 SignA = new Vec4(+1, -1, +1, -1);
		final Vec4 SignB = new Vec4(-1, +1, -1, +1);

		Vec4 Fac0 = new Vec4(Coef00, Coef00, Coef02, Coef03);
		Vec4 Fac1 = new Vec4(Coef04, Coef04, Coef06, Coef07);
		Vec4 Fac2 = new Vec4(Coef08, Coef08, Coef10, Coef11);
		Vec4 Fac3 = new Vec4(Coef12, Coef12, Coef14, Coef15);
		Vec4 Fac4 = new Vec4(Coef16, Coef16, Coef18, Coef19);
		Vec4 Fac5 = new Vec4(Coef20, Coef20, Coef22, Coef23);

		Vec4 Vec0 = new Vec4(mat.matrix[4], mat.matrix[0], mat.matrix[0], mat.matrix[0]);
		Vec4 Vec1 = new Vec4(mat.matrix[5], mat.matrix[1], mat.matrix[1], mat.matrix[1]);
		Vec4 Vec2 = new Vec4(mat.matrix[6], mat.matrix[2], mat.matrix[2], mat.matrix[2]);
		Vec4 Vec3 = new Vec4(mat.matrix[7], mat.matrix[3], mat.matrix[3], mat.matrix[3]);

		Vec4 Inv0 = Vec4.mul(SignA, Vec4.mul(Vec1, Fac0).sub(Vec4.mul(Vec2, Fac1)).add(Vec4.mul(Vec3, Fac2)));
		Vec4 Inv1 = Vec4.mul(SignB, Vec4.mul(Vec0, Fac0).sub(Vec4.mul(Vec2, Fac3)).add(Vec4.mul(Vec3, Fac4)));
		Vec4 Inv2 = Vec4.mul(SignA, Vec4.mul(Vec0, Fac1).sub(Vec4.mul(Vec1, Fac3)).add(Vec4.mul(Vec3, Fac5)));
		Vec4 Inv3 = Vec4.mul(SignB, Vec4.mul(Vec0, Fac2).sub(Vec4.mul(Vec1, Fac4)).add(Vec4.mul(Vec2, Fac5)));

		Mat4 res = new Mat4(Inv0, Inv1, Inv2, Inv3);

		Vec4 Row0 = new Vec4(res.matrix[0], res.matrix[4], res.matrix[8], res.matrix[12]);

		float Determinant = Glm.dot(mat.getColumn(0), Row0);

		return res.scale(1.0f / Determinant);
	}
	
		
	public static Mat4 translate(Mat4 mat, Vec3 vec) {
		Mat4 res = new Mat4(mat);	
		
		Vec4 temp = new Vec4();
		Vec4 temp0 = mat.getColumn(0).scale(vec.get(X));
		Vec4 temp1 = mat.getColumn(1).scale(vec.get(Y));
		Vec4 temp2 = mat.getColumn(2).scale(vec.get(Z));
		Vec4 temp3 = mat.getColumn(3);

		temp = temp0.add(temp1).add(temp2).add(temp3);
		
		res.setColumn(3, temp);
		
		return res;
	}
	
	
	public static Mat3 transpose(Mat3 mat) {
		Mat3 res = new Mat3();
		
		res.matrix[0] = mat.matrix[0];
		res.matrix[1] = mat.matrix[3];
		res.matrix[2] = mat.matrix[6];

		res.matrix[3] = mat.matrix[1];
		res.matrix[4] = mat.matrix[4];
		res.matrix[5] = mat.matrix[7];

        res.matrix[6] = mat.matrix[2];
        res.matrix[7] = mat.matrix[5];
        res.matrix[8] = mat.matrix[8];
        
        return res;
	}
	
	public static Mat4 transpose(Mat4 mat) {
		float source[] = mat.matrix;
		float destination[] = new float[16];

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				destination[j + i * 4] = source[i + j * 4];
		
		return new Mat4(destination);
	}
	
	
	public static Mat4 perspective(float fovy, float aspect, float zNear, float zFar) {	
		float range = (float) (Math.tan(Math.toRadians(fovy / 2.0f)) * zNear);	
		float left = -range * aspect;
		float right = range * aspect;
		float bottom = -range;
		float top = range;

		Mat4 res = new Mat4(0.0f);

		res.matrix[0] = (2.0f * zNear) / (right - left);
		res.matrix[5] = (2.0f * zNear) / (top - bottom);
		res.matrix[10] = - (zFar + zNear) / (zFar - zNear);
		res.matrix[11] = - 1.0f;
		res.matrix[14] = - (2.0f * zFar * zNear) / (zFar - zNear);

		return res;
	}
	
	
	public static Mat4 matCast(Quaternion quat) {
		// Converts this quaternion to a rotation matrix.
	    //  | 1 - 2(y^2 + z^2)	2(xy + wz)			2(xz - wy)			0  |
	    //  | 2(xy - wz)		1 - 2(x^2 + z^2)	2(yz + wx)			0  |
	    //  | 2(xz + wy)		2(yz - wx)			1 - 2(x^2 + y^2)	0  |
	    //  | 0					0					0					1  |
		
		Mat4 res = new Mat4(0.0f);

		res.matrix[0] = 1 - 2 * quat.vector[Y] * quat.vector[Y] - 2 * quat.vector[Z] * quat.vector[Z];
		res.matrix[1] = 2 * quat.vector[X] * quat.vector[Y] + 2 * quat.vector[W] * quat.vector[Z];
		res.matrix[2] = 2 * quat.vector[X] * quat.vector[Z] - 2 * quat.vector[W] * quat.vector[Y];

		res.matrix[4] = 2 * quat.vector[X] * quat.vector[Y] - 2 * quat.vector[W] * quat.vector[Z];
		res.matrix[5] = 1 - 2 * quat.vector[X] * quat.vector[X] - 2 * quat.vector[Z] * quat.vector[Z];
		res.matrix[6] = 2 * quat.vector[Y] * quat.vector[Z] + 2 * quat.vector[W] * quat.vector[X];

		res.matrix[8] = 2 * quat.vector[X] * quat.vector[Z] + 2 * quat.vector[W] * quat.vector[Y];
		res.matrix[9] = 2 * quat.vector[Y] * quat.vector[Z] - 2 * quat.vector[W] * quat.vector[X];
		res.matrix[10] = 1 - 2 * quat.vector[X] * quat.vector[X] - 2 * quat.vector[Y] * quat.vector[Y];
		
	    res.matrix[12] = 0.0f;
	    res.matrix[13] = 0.0f;
	    res.matrix[14] = 0.0f;
	    res.matrix[15] = 1.0f;
	    
		return res;
	}
}
