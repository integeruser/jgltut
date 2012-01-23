package rosick.glm;


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
		vec.x = a.x / length;
		vec.y = a.y / length;
		vec.z = a.z / length;
		
		return vec;
	}
	
	public static Vec4 normalize(Vec4 a) {
		Vec4 vec = new Vec4();
		
		float length = length(a);
		vec.x = a.x / length;
		vec.y = a.y / length;
		vec.z = a.z / length;
		vec.w = a.w / length;

		return vec;
	}
	
	public static Quaternion normalize(Quaternion a) {		
		Quaternion quat = new Quaternion();
		
		float length = length(a);
		quat.w = a.w / length;
		quat.x = a.x / length;
		quat.y = a.y / length;
		quat.z = a.z / length;
		
		return quat;
	}
	
	
	public static float dot(Vec3 a, Vec3 b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}
	
	public static float dot(Vec4 a, Vec4 b) {
		return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
	}
	public static float dot(Quaternion a, Quaternion b) {
		return a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
	}
	
	
	public static Vec3 cross(Vec3 a, Vec3 b) {
		Vec3 vec = new Vec3();

		vec.x = a.y * b.z - a.z * b.y;
		vec.y = a.z * b.x - a.x * b.z;
		vec.z = a.x * b.y - a.y * b.x;

		return vec;
	}
		
	
	public static Vec4 mix(Vec4 x, Vec4 y, float a) {
		return Vec4.add(x, Vec4.sub(y, x).scale(a));
	}
	
	
	public static Quaternion angleAxis(float angle, Vec3 v) {
		Quaternion result = new Quaternion();

		float a = (float) Math.toRadians(angle);
        float s = (float) Math.sin(a * 0.5);

        result.w = (float) Math.cos(a * 0.5);
        result.x = v.x * s;
        result.y = v.y * s;
        result.z = v.z * s;
        
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
				quat.w = biggestVal; 
				quat.x = (mat.matrix[6] - mat.matrix[9]) * mult;
				quat.y = (mat.matrix[8] - mat.matrix[2]) * mult;
				quat.z = (mat.matrix[1] - mat.matrix[4]) * mult;
				break;
			case 1:
				quat.w = (mat.matrix[6] - mat.matrix[9]) * mult;
				quat.x = biggestVal;
				quat.y = (mat.matrix[1] + mat.matrix[4]) * mult;
				quat.z = (mat.matrix[8] + mat.matrix[2]) * mult;
				break;
			case 2:
				quat.w = (mat.matrix[8] - mat.matrix[2]) * mult;
				quat.x = (mat.matrix[1] + mat.matrix[4]) * mult;
				quat.y = biggestVal;
				quat.z = (mat.matrix[6] + mat.matrix[9]) * mult;
				break;
			case 3:
				quat.w = (mat.matrix[1] - mat.matrix[4]) * mult;
				quat.x = (mat.matrix[8] + mat.matrix[2]) * mult;
				quat.y = (mat.matrix[6] + mat.matrix[9]) * mult;
				quat.z = biggestVal;
				break;
		}   
		
		return quat;
	}
	
	
	public static Quaternion conjugate(Quaternion quat) {
        return new Quaternion(quat.w, -quat.x, -quat.y, -quat.z);
	}
	
	
	public static Mat4 translate(Mat4 mat, Vec3 vec) {
		Mat4 result = new Mat4(mat);

		mat.matrix[3] = mat.matrix[3] * vec.x;
		mat.matrix[7] = mat.matrix[7] * vec.y;
		mat.matrix[11] = mat.matrix[11] * vec.z;

		return result;
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

		mat.matrix[0] = 1 - 2 * quat.y * quat.y - 2 * quat.z * quat.z;
		mat.matrix[1] = 2 * quat.x * quat.y + 2 * quat.w * quat.z;
		mat.matrix[2] = 2 * quat.x * quat.z - 2 * quat.w * quat.y;

		mat.matrix[4] = 2 * quat.x * quat.y - 2 * quat.w * quat.z;
		mat.matrix[5] = 1 - 2 * quat.x * quat.x - 2 * quat.z * quat.z;
		mat.matrix[6] = 2 * quat.y * quat.z + 2 * quat.w * quat.x;

		mat.matrix[8] = 2 * quat.x * quat.z + 2 * quat.w * quat.y;
		mat.matrix[9] = 2 * quat.y * quat.z - 2 * quat.w * quat.x;
		mat.matrix[10] = 1 - 2 * quat.x * quat.x - 2 * quat.y * quat.y;
		
	    mat.matrix[12] = 0.0f;
	    mat.matrix[13] = 0.0f;
	    mat.matrix[14] = 0.0f;
	    mat.matrix[15] = 1.0f;
	    
		return mat;
	}
}
