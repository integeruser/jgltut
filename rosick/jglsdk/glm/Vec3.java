package rosick.jglsdk.glm;

import java.nio.FloatBuffer;

import rosick.PortingUtils.BufferableData;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Vec3 extends BufferableData<FloatBuffer> {
	
	public float x, y, z;
	
	
	public Vec3() {
	}
	
	public Vec3(float f) {
		x = f;
		y = f;
		z = f;
	}
	
	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(Vec3 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}
	
	public Vec3(Vec4 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;	
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	

	@Override
	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		buffer.put(z);

		return buffer;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	public Vec3 add(Vec3 vec) {
		x += vec.x;
		y += vec.y;
		z += vec.z;
		
		return this;
	}
	
	public Vec3 sub(Vec3 vec) {
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		
		return this;
	}
	
	public Vec3 mul(Vec3 vec) {
		x *= vec.x;
		y *= vec.y;
		z *= vec.z;
		
		return this;
	}
	
	
	public Vec3 scale(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		
		return this;
	}
	

	public Vec3 negate() {
		x = -x;
		y = -y;
		z = -z;

		return this;
	}
	
	
	@Override
	public String toString() {
		return "X: " + x + ", Y: " + y + ", Z: " + z;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public static Vec3 add(Vec3 a, Vec3 b) {
		Vec3 res = new Vec3(a);
		
		return res.add(b);
	}
	
	public static Vec3 sub(Vec3 a, Vec3 b) {
		Vec3 res = new Vec3(a);

		return res.sub(b);
	}	
	
	public static Vec3 mul(Vec3 a, Vec3 b) {	
		Vec3 res = new Vec3(a);
		
		return res.mul(b);
	}
	
	
	public static Vec3 scale(Vec3 vec, float scalar) {
		Vec3 res = new Vec3(vec);
		
		return res.scale(scalar);
	}
	

	public static Vec3 negate(Vec3 vec) {
		Vec3 res = new Vec3(vec);
		
		return res.negate();
	}
}