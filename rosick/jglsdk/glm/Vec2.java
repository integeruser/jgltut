package rosick.jglsdk.glm;

import java.nio.FloatBuffer;

import rosick.PortingUtils.BufferableData;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Vec2 extends BufferableData<FloatBuffer> {
	
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
	
	public Vec2 add(Vec2 vec) {
		x += vec.x;
		y += vec.y;
		
		return this;
	}
	
	public Vec2 sub(Vec2 vec) {
		x -= vec.x;
		y -= vec.y;
		
		return this;
	}
	
	public Vec2 mul(Vec2 vec) {
		x *= vec.x;
		y *= vec.y;
		
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
	
	
	@Override
	public String toString() {
		return "X: " + x + ", Y: " + y;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public static Vec2 add(Vec2 a, Vec2 b) {
		Vec2 res = new Vec2(a);
		
		return res.add(b);
	}
	
	public static Vec2 sub(Vec2 a, Vec2 b) {
		Vec2 res = new Vec2(a);

		return res.sub(b);
	}	
	
	public static Vec2 mul(Vec2 a, Vec2 b) {	
		Vec2 res = new Vec2(a);
		
		return res.mul(b);
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