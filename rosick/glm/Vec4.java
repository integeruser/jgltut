package rosick.glm;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Vec4 {
	
	public float x, y, z, w;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Vec4() {
	}
	
	public Vec4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vec4(Vec3 vec3, float w) {
		x = vec3.x;
		y = vec3.y;
		z = vec3.z;
		this.w = w;
	}
	
	public Vec4(Vec4 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
		w = vec.w;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public Vec4 add(Vec4 vec) {
		x += vec.x;
		y += vec.y;
		z += vec.z;
		w += vec.w;

		return this;
	}
	
	public Vec4 sub(Vec4 vec) {
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		w -= vec.w;

		return this;
	}
	
	public Vec4 mul(Vec4 vec) {
		x *= vec.x;
		y *= vec.y;
		z *= vec.z;
		w *= vec.w;

		return this;
	}
	
	
	public Vec4 scale(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		w *= scalar;

		return this;
	}
	
	
	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(new float[] {x, y, z, w});
		buffer.flip();
		
		return buffer;
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static Vec4 add(Vec4 a, Vec4 b) {
		Vec4 vec = new Vec4(a);
		
		return vec.add(b);
	}
	
	public static Vec4 sub(Vec4 a, Vec4 b) {
		Vec4 vec = new Vec4(a);

		return vec.sub(b);
	}
	
	public static Vec4 mul(Vec4 a, Vec4 b) {
		Vec4 vec = new Vec4(a);

		return vec.mul(b);
	}
}