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
	
	
	public static float length(Vec3 a) {	
		return (float) Math.sqrt(dot(a, a));
	}
	
	public static float length(Vec4 a) {
		return (float) Math.sqrt(dot(a, a));
	}
	
	public static float length(Quaternion a) {
		return (float) Math.sqrt(dot(a, a));
	}
	
	
	public static Vec3 normalize(Vec3 a) {
		Vec3 res = new Vec3();
		
		float invLength = 1.0f / length(a);
		res.vector[X] = a.vector[X] * invLength;
		res.vector[Y] = a.vector[Y] * invLength;
		res.vector[Z] = a.vector[Z] * invLength;
		
		return res;
	}
	
	public static Vec4 normalize(Vec4 a) {
		Vec4 res = new Vec4();
		
		float invLength = 1.0f / length(a);
		res.vector[X] = a.vector[X] * invLength;
		res.vector[Y] = a.vector[Y] * invLength;
		res.vector[Z] = a.vector[Z] * invLength;
		res.vector[W] = a.vector[W] * invLength;

		return res;
	}
	
	public static Quaternion normalize(Quaternion a) {		
		Quaternion res = new Quaternion();
		
		float invLength = 1.0f / length(a);
		res.vector[X] = a.vector[X] * invLength;
		res.vector[Y] = a.vector[Y] * invLength;
		res.vector[Z] = a.vector[Z] * invLength;
		res.vector[W] = a.vector[W] * invLength;

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
		
	
	public static Vec4 mix(Vec4 x, Vec4 y, float a) {
		return Vec4.add(x, Vec4.sub(y, x).scale(a));
	}
	
	
	public static Quaternion angleAxis(float angle, Vec3 v) {
		Quaternion res = new Quaternion();

		float a = (float) Math.toRadians(angle);
        float s = (float) Math.sin(a * 0.5);

        res.vector[X] = v.vector[X] * s;
        res.vector[Y] = v.vector[Y] * s;
        res.vector[Z] = v.vector[Z] * s;
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
	
	
	public static Quaternion conjugate(Quaternion quat) {
        return Quaternion.negate(quat);
	}
	
	
	public static Mat4 translate(Mat4 mat, Vec3 vec) {
		Mat4 res = new Mat4(mat);

		res.matrix[12] = mat.matrix[0] * vec.get(X) + mat.matrix[4] * vec.get(Y) + mat.matrix[8] * vec.get(Z);
		res.matrix[13] = mat.matrix[1] * vec.get(X) + mat.matrix[5] * vec.get(Y) + mat.matrix[9] * vec.get(Z);
		res.matrix[14] = mat.matrix[2] * vec.get(X) + mat.matrix[6] * vec.get(Y) + mat.matrix[10] * vec.get(Z);
		res.matrix[15] = mat.matrix[3] * vec.get(X) + mat.matrix[7] * vec.get(Y) + mat.matrix[11] * vec.get(Z);
		
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
