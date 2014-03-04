package rosick.jglsdk.glm;

import java.nio.FloatBuffer;

import rosick.jglsdk.BufferableData;


/**
 * Visit https://github.com/integeruser/jglsdk for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Vec2 extends BufferableData<FloatBuffer> {
	public static final int SIZE = (2 * Float.SIZE) / Byte.SIZE;

	
	public float x, y;
	
	
	public Vec2() {
	}
	
	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2(Vec2 vec) {
		this.x = vec.x;
		this.y = vec.y;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	@Override
	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		
		return buffer;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public Vec2 add(Vec2 rhs) {
		x += rhs.x;
		y += rhs.y;
		
		return this;
	}
	
	public Vec2 sub(Vec2 rhs) {
		x -= rhs.x;
		y -= rhs.y;
		
		return this;
	}
	
	public Vec2 mul(Vec2 rhs) {
		x *= rhs.x;
		y *= rhs.y;
		
		return this;
	}
	
	
	public Vec2 scale(float scalar) {
		x *= scalar;
		y *= scalar;
		
		return this;
	}
	

	public Vec2 negate() {
		x = -x;
		y = -y;

		return this;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public static Vec2 add(Vec2 lhs, Vec2 rhs) {
		Vec2 res = new Vec2(lhs);
		
		return res.add(rhs);
	}
	
	public static Vec2 sub(Vec2 lhs, Vec2 rhs) {
		Vec2 res = new Vec2(lhs);

		return res.sub(rhs);
	}	
	
	public static Vec2 mul(Vec2 lhs, Vec2 rhs) {	
		Vec2 res = new Vec2(lhs);
		
		return res.mul(rhs);
	}
	
	
	public static Vec2 scale(Vec2 vec, float scalar) {
		Vec2 res = new Vec2(vec);
		
		return res.scale(scalar);
	}
	

	public static Vec2 negate(Vec2 vec) {
		Vec2 res = new Vec2(vec);
		
		return res.negate();
	}
}