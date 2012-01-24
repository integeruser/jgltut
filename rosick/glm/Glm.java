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
		Vec3 vec = new Vec3();
		
		float length = length(a);
		vec.vector[X] = a.vector[X] / length;
		vec.vector[Y] = a.vector[Y] / length;
		vec.vector[Z] = a.vector[Z] / length;
		
		return vec;
	}
	
	public static Vec4 normalize(Vec4 a) {
		Vec4 vec = new Vec4();
		
		float length = length(a);
		vec.vector[X] = a.vector[X] / length;
		vec.vector[Y] = a.vector[Y] / length;
		vec.vector[Z] = a.vector[Z] / length;
		vec.vector[W] = a.vector[W] / length;

		return vec;
	}
	
	public static Quaternion normalize(Quaternion a) {		
		Quaternion quat = new Quaternion();
		
		float length = length(a);
		quat.vector[W] = a.vector[W] / length;
		quat.vector[X] = a.vector[X] / length;
		quat.vector[Y] = a.vector[Y] / length;
		quat.vector[Z] = a.vector[Z] / length;
		
		return quat;
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
		Vec3 vec = new Vec3();

		vec.vector[X] = a.vector[Y] * b.vector[Z] - a.vector[Z] * b.vector[Y];
		vec.vector[Y] = a.vector[Z] * b.vector[X] - a.vector[X] * b.vector[Z];
		vec.vector[Z] = a.vector[X] * b.vector[Y] - a.vector[Y] * b.vector[X];

		return vec;
	}
		
	
	public static Vec4 mix(Vec4 x, Vec4 y, float a) {
		return Vec4.add(x, Vec4.sub(y, x).scale(a));
	}
	
	
	public static Quaternion angleAxis(float angle, Vec3 v) {
		Quaternion result = new Quaternion();

		float a = (float) Math.toRadians(angle);
        float s = (float) Math.sin(a * 0.5);

        result.vector[W] = (float) Math.cos(a * 0.5);
        result.vector[X] = v.vector[X] * s;
        result.vector[Y] = v.vector[Y] * s;
        result.vector[Z] = v.vector[Z] * s;
        
        return result;
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

		Quaternion quat = new Quaternion();

		switch (biggestIndex) {
			case 0:
				quat.vector[W] = biggestVal; 
				quat.vector[X] = (mat.matrix[6] - mat.matrix[9]) * mult;
				quat.vector[Y] = (mat.matrix[8] - mat.matrix[2]) * mult;
				quat.vector[Z] = (mat.matrix[1] - mat.matrix[4]) * mult;
				break;
			case 1:
				quat.vector[W] = (mat.matrix[6] - mat.matrix[9]) * mult;
				quat.vector[X] = biggestVal;
				quat.vector[Y] = (mat.matrix[1] + mat.matrix[4]) * mult;
				quat.vector[Z] = (mat.matrix[8] + mat.matrix[2]) * mult;
				break;
			case 2:
				quat.vector[W] = (mat.matrix[8] - mat.matrix[2]) * mult;
				quat.vector[X] = (mat.matrix[1] + mat.matrix[4]) * mult;
				quat.vector[Y] = biggestVal;
				quat.vector[Z] = (mat.matrix[6] + mat.matrix[9]) * mult;
				break;
			case 3:
				quat.vector[W] = (mat.matrix[1] - mat.matrix[4]) * mult;
				quat.vector[X] = (mat.matrix[8] + mat.matrix[2]) * mult;
				quat.vector[Y] = (mat.matrix[6] + mat.matrix[9]) * mult;
				quat.vector[Z] = biggestVal;
				break;
		}   
		
		return quat;
	}
	
	
	public static Quaternion conjugate(Quaternion quat) {
        return new Quaternion(quat.vector[W], -quat.vector[X], -quat.vector[Y], -quat.vector[Z]);
	}
	
	
	public static Mat4 translate(Mat4 mat, Vec3 vec) {
		/*
		Mat4 res = new Mat4(mat);

		res.matrix[12] = mat.matrix[12] + vec.x;
		res.matrix[13] = mat.matrix[13] + vec.y;
		res.matrix[14] = mat.matrix[14] + vec.z;
		*/
		
		Mat4 res = new Mat4(mat);

		Vec4 column0 = mat.getColumn(0);
		Vec4 column1 = mat.getColumn(1);
		Vec4 column2 = mat.getColumn(2);
		Vec4 column3 = mat.getColumn(3);

		Vec4 newColumn3 = column0.scale(vec.vector[X]).add(column1.scale(vec.vector[Y])).add(column2.scale(vec.vector[Z])).add(column3);
		res.setColumn(3, newColumn3);
		
		return res;
	}
	
	
	public static Mat4 transpose(Mat4 mat) {
		float source[] = mat.matrix;
		float destination[] = new float[16];

		for( int i = 0; i < 4; i++)
			for( int j = 0; j < 4; j++)
				destination[j+i*4] = source[i+j*4];
		
		return new Mat4(destination);
	}
	
	
	public static Mat4 perspective(float fovy, float aspect, float zNear, float zFar) {	
		float range = (float) (Math.tan(Math.toRadians(fovy / 2.0f)) * zNear);	
		float left = -range * aspect;
		float right = range * aspect;
		float bottom = -range;
		float top = range;

		Mat4 mat = new Mat4(0.0f);

		mat.matrix[0] = (2.0f * zNear) / (right - left);
		mat.matrix[5] = (2.0f * zNear) / (top - bottom);
		mat.matrix[10] = - (zFar + zNear) / (zFar - zNear);
		mat.matrix[11] = - 1.0f;
		mat.matrix[14] = - (2.0f * zFar * zNear) / (zFar - zNear);

		return mat;
	}
	
	
	public static Mat4 matCast(Quaternion quat) {
		// Converts this quaternion to a rotation matrix.
	    //  | 1 - 2(y^2 + z^2)	2(xy + wz)			2(xz - wy)			0  |
	    //  | 2(xy - wz)		1 - 2(x^2 + z^2)	2(yz + wx)			0  |
	    //  | 2(xz + wy)		2(yz - wx)			1 - 2(x^2 + y^2)	0  |
	    //  | 0					0					0					1  |
		
		Mat4 mat = new Mat4(0.0f);

		mat.matrix[0] = 1 - 2 * quat.vector[Y] * quat.vector[Y] - 2 * quat.vector[Z] * quat.vector[Z];
		mat.matrix[1] = 2 * quat.vector[X] * quat.vector[Y] + 2 * quat.vector[W] * quat.vector[Z];
		mat.matrix[2] = 2 * quat.vector[X] * quat.vector[Z] - 2 * quat.vector[W] * quat.vector[Y];

		mat.matrix[4] = 2 * quat.vector[X] * quat.vector[Y] - 2 * quat.vector[W] * quat.vector[Z];
		mat.matrix[5] = 1 - 2 * quat.vector[X] * quat.vector[X] - 2 * quat.vector[Z] * quat.vector[Z];
		mat.matrix[6] = 2 * quat.vector[Y] * quat.vector[Z] + 2 * quat.vector[W] * quat.vector[X];

		mat.matrix[8] = 2 * quat.vector[X] * quat.vector[Z] + 2 * quat.vector[W] * quat.vector[Y];
		mat.matrix[9] = 2 * quat.vector[Y] * quat.vector[Z] - 2 * quat.vector[W] * quat.vector[X];
		mat.matrix[10] = 1 - 2 * quat.vector[X] * quat.vector[X] - 2 * quat.vector[Y] * quat.vector[Y];
		
	    mat.matrix[12] = 0.0f;
	    mat.matrix[13] = 0.0f;
	    mat.matrix[14] = 0.0f;
	    mat.matrix[15] = 1.0f;
	    
		return mat;
	}
}
