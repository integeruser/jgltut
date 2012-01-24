package rosick.glm;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public class Vec4 extends Vec implements Bufferable {
	
	public Vec4() {
		vector = new float[4];
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
	
	@Override
	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(vector);
		buffer.flip();
		
		return buffer;
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
		Vec4 ris = new Vec4(a);
		
		return ris.add(b);
	}
	
	public static Vec4 sub(Vec4 a, Vec4 b) {
		Vec4 ris = new Vec4(a);

		return ris.sub(b);
	}
	
	public static Vec4 mul(Vec4 a, Vec4 b) {
		Vec4 ris = new Vec4(a);

		return ris.mul(b);
	}
}