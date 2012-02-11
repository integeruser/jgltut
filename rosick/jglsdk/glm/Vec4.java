package rosick.jglsdk.glm;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Vec4 extends Vec {
	
	public Vec4() {
		vector = new float[4];
	}
	
	public Vec4(float f) {
		vector = new float[4];
		vector[X] = f;
		vector[Y] = f;
		vector[Z] = f;
		vector[W] = f;
	}
	
	public Vec4(float x, float y, float z, float w) {
		vector = new float[4];
		vector[X] = x;
		vector[Y] = y;
		vector[Z] = z;
		vector[W] = w;
	}
	
	public Vec4(Vec3 vec, float w) {
		vector = new float[4];
		vector[X] = vec.vector[X];
		vector[Y] = vec.vector[Y];
		vector[Z] = vec.vector[Z];
		vector[W] = w;
	}
	
	public Vec4(Vec4 vec) {
		vector = new float[4];
		vector[X] = vec.vector[X];
		vector[Y] = vec.vector[Y];
		vector[Z] = vec.vector[Z];
		vector[W] = vec.vector[W];
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Vec4 add(Vec4 vec) {
		vector[X] += vec.vector[X];
		vector[Y] += vec.vector[Y];
		vector[Z] += vec.vector[Z];
		vector[W] += vec.vector[W];

		return this;
	}
	
	public Vec4 sub(Vec4 vec) {
		vector[X] -= vec.vector[X];
		vector[Y] -= vec.vector[Y];
		vector[Z] -= vec.vector[Z];
		vector[W] -= vec.vector[W];

		return this;
	}
	
	public Vec4 mul(Vec4 vec) {	   
		vector[X] *= vec.vector[X];
		vector[Y] *= vec.vector[Y];
		vector[Z] *= vec.vector[Z];
		vector[W] *= vec.vector[W];
		
		return this;
	}
	
	
	public Vec4 scale(float scalar) {
		vector[X] *= scalar;
		vector[Y] *= scalar;
		vector[Z] *= scalar;
		vector[W] *= scalar;

		return this;
	}
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static Vec4 add(Vec4 a, Vec4 b) {
		Vec4 res = new Vec4(a);
		
		return res.add(b);
	}
	
	public static Vec4 sub(Vec4 a, Vec4 b) {
		Vec4 res = new Vec4(a);

		return res.sub(b);
	}
	
	public static Vec4 mul(Vec4 a, Vec4 b) {
		Vec4 res = new Vec4(a);

		return res.mul(b);
	}
	
	
	public static Vec4 scale(Vec4 vec, float scalar) {
		Vec4 res = new Vec4(vec);

		return res.scale(scalar);
	}
}