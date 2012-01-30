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
		float coef00 = mat.matrix[10] * mat.matrix[15] - mat.matrix[14] * mat.matrix[11];
		float coef02 = mat.matrix[6] * mat.matrix[15] - mat.matrix[14] * mat.matrix[7];
		float coef03 = mat.matrix[6] * mat.matrix[11] - mat.matrix[10] * mat.matrix[7];

		float coef04 = mat.matrix[9] * mat.matrix[15] - mat.matrix[13] * mat.matrix[11];
		float coef06 = mat.matrix[5] * mat.matrix[15] - mat.matrix[13] * mat.matrix[7];
		float coef07 = mat.matrix[5] * mat.matrix[11] - mat.matrix[9] * mat.matrix[7];

		float coef08 = mat.matrix[9] * mat.matrix[14] - mat.matrix[13] * mat.matrix[10];
		float coef10 = mat.matrix[5] * mat.matrix[14] - mat.matrix[13] * mat.matrix[6];
		float coef11 = mat.matrix[5] * mat.matrix[10] - mat.matrix[9] * mat.matrix[6];

		float coef12 = mat.matrix[8] * mat.matrix[15] - mat.matrix[12] * mat.matrix[11];
		float coef14 = mat.matrix[4] * mat.matrix[15] - mat.matrix[12] * mat.matrix[7];
		float coef15 = mat.matrix[4] * mat.matrix[11] - mat.matrix[8] * mat.matrix[7];

		float coef16 = mat.matrix[8] * mat.matrix[14] - mat.matrix[12] * mat.matrix[10];
		float coef18 = mat.matrix[4] * mat.matrix[14] - mat.matrix[12] * mat.matrix[6];
		float coef19 = mat.matrix[4] * mat.matrix[10] - mat.matrix[8] * mat.matrix[6];

		float coef20 = mat.matrix[8] * mat.matrix[13] - mat.matrix[12] * mat.matrix[9];
		float coef22 = mat.matrix[4] * mat.matrix[13] - mat.matrix[12] * mat.matrix[5];
		float coef23 = mat.matrix[4] * mat.matrix[9]  - mat.matrix[8] * mat.matrix[5];

		final Vec4 signA = new Vec4(+1, -1, +1, -1);
		final Vec4 signB = new Vec4(-1, +1, -1, +1);

		Vec4 fac0 = new Vec4(coef00, coef00, coef02, coef03);
		Vec4 fac1 = new Vec4(coef04, coef04, coef06, coef07);
		Vec4 fac2 = new Vec4(coef08, coef08, coef10, coef11);
		Vec4 fac3 = new Vec4(coef12, coef12, coef14, coef15);
		Vec4 fac4 = new Vec4(coef16, coef16, coef18, coef19);
		Vec4 fac5 = new Vec4(coef20, coef20, coef22, coef23);

		Vec4 vec0 = new Vec4(mat.matrix[4], mat.matrix[0], mat.matrix[0], mat.matrix[0]);
		Vec4 vec1 = new Vec4(mat.matrix[5], mat.matrix[1], mat.matrix[1], mat.matrix[1]);
		Vec4 vec2 = new Vec4(mat.matrix[6], mat.matrix[2], mat.matrix[2], mat.matrix[2]);
		Vec4 vec3 = new Vec4(mat.matrix[7], mat.matrix[3], mat.matrix[3], mat.matrix[3]);

		Vec4 inv0 = Vec4.mul(signA, Vec4.mul(vec1, fac0).sub(Vec4.mul(vec2, fac1)).add(Vec4.mul(vec3, fac2)));
		Vec4 inv1 = Vec4.mul(signB, Vec4.mul(vec0, fac0).sub(Vec4.mul(vec2, fac3)).add(Vec4.mul(vec3, fac4)));
		Vec4 inv2 = Vec4.mul(signA, Vec4.mul(vec0, fac1).sub(Vec4.mul(vec1, fac3)).add(Vec4.mul(vec3, fac5)));
		Vec4 inv3 = Vec4.mul(signB, Vec4.mul(vec0, fac2).sub(Vec4.mul(vec1, fac4)).add(Vec4.mul(vec2, fac5)));

		Mat4 res = new Mat4(inv0, inv1, inv2, inv3);

		Vec4 row0 = new Vec4(res.matrix[0], res.matrix[4], res.matrix[8], res.matrix[12]);

		float determinant = Glm.dot(mat.getColumn(0), row0);

		return res.scale(1.0f / determinant);
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
		Mat4 res = new Mat4();
		
		res.matrix[0] = mat.matrix[0];
		res.matrix[1] = mat.matrix[4];
		res.matrix[2] = mat.matrix[8];
		res.matrix[3] = mat.matrix[12];
		
		res.matrix[4] = mat.matrix[1];
		res.matrix[5] = mat.matrix[5];
        res.matrix[6] = mat.matrix[9];
        res.matrix[7] = mat.matrix[13];
        
		res.matrix[8] = mat.matrix[2];
		res.matrix[9] = mat.matrix[6];
        res.matrix[10] = mat.matrix[10];
        res.matrix[11] = mat.matrix[14];
        
		res.matrix[12] = mat.matrix[3];
		res.matrix[13] = mat.matrix[7];
        res.matrix[14] = mat.matrix[11];
        res.matrix[15] = mat.matrix[15];
        
        return res;
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
		//  Converts this quaternion to a rotation matrix.
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
	
	
	public static Mat4 rotate(Mat4 m, float angle, Vec3 v) {
        float a = (float) Math.toRadians(angle);
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);

        Vec3 axis = normalize(v);

        Vec3 temp = Vec3.scale(axis, 1.0f - c);

        Mat4 rotate = new Mat4();
		rotate.set(0, c + temp.vector[X] * axis.vector[X]);
		rotate.set(1, 0 + temp.vector[X] * axis.vector[Y] + s * axis.vector[Z]);
		rotate.set(2, 0 + 0 + temp.vector[X] * axis.vector[Z] - s * axis.vector[Y]);

		rotate.set(4, 0 + temp.vector[Y] * axis.vector[X] - s * axis.vector[Z]);
		rotate.set(5, c + temp.vector[Y] * axis.vector[Y]);
		rotate.set(6, 0 + temp.vector[Y] * axis.vector[Z] + s * axis.vector[X]);
		
		rotate.set(8, 0 + temp.vector[Z] * axis.vector[X] + s * axis.vector[Y]);
		rotate.set(9, 0 + temp.vector[Z] * axis.vector[Y] - s * axis.vector[X]);
		rotate.set(10, c + temp.vector[Z] * axis.vector[Z]);

	    Mat4 result = new Mat4();
	    result.setColumn(0, Vec4.scale(m.getColumn(0), rotate.matrix[0]).add(Vec4.scale(m.getColumn(1), rotate.matrix[1])).add(Vec4.scale(m.getColumn(2), rotate.matrix[2])));
	    result.setColumn(1, Vec4.scale(m.getColumn(0), rotate.matrix[4]).add(Vec4.scale(m.getColumn(1), rotate.matrix[5])).add(Vec4.scale(m.getColumn(2), rotate.matrix[6])));
	    result.setColumn(2, Vec4.scale(m.getColumn(0), rotate.matrix[8]).add(Vec4.scale(m.getColumn(1), rotate.matrix[9])).add(Vec4.scale(m.getColumn(2), rotate.matrix[10])));
	    result.setColumn(3, m.getColumn(3));
		
		return result;
	}
}
