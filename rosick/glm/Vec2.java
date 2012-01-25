package rosick.glm;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public class Vec2 extends Vec {

	public Vec2() {
		vector = new float[2];
	}
	
	public Vec2(float x, float y) {
		vector = new float[2];
		vector[X] = x;
		vector[Y] = y;
	}
	
	public Vec2(Vec2 vec) {
		vector = new float[2];
		vector[X] = vec.vector[X];
		vector[Y] = vec.vector[Y];
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public Vec2 add(Vec2 vec) {
		vector[X] += vec.vector[X];
		vector[Y] += vec.vector[Y];
		
		return this;
	}
	
	public Vec2 sub(Vec2 vec) {
		vector[X] -= vec.vector[X];
		vector[Y] -= vec.vector[Y];
		
		return this;
	}
	
	public Vec2 mul(Vec2 vec) {
		vector[X] *= vec.vector[X];
		vector[Y] *= vec.vector[Y];
		
		return this;
	}
	
	
	public Vec2 scale(float scalar) {
		vector[X] *= scalar;
		vector[Y] *= scalar;
		
		return this;
	}
	

	public Vec2 negate() {
		vector[X] = -vector[X];
		vector[Y] = -vector[Y];

		return this;
	}
	
	
	@Override
	public String toString() {
		return "X: " + vector[X] + ", Y: " + vector[Y];
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public static Vec2 add(Vec2 a, Vec2 b) {
		Vec2 vec = new Vec2(a);
		
		return vec.add(b);
	}
	
	public static Vec2 sub(Vec2 a, Vec2 b) {
		Vec2 vec = new Vec2(a);

		return vec.sub(b);
	}	
	
	public static Vec2 mul(Vec2 a, Vec2 b) {	
		Vec2 vec = new Vec2(a);
		
		return vec.mul(b);
	}
	
	
	public static Vec2 scale(Vec2 a, float scalar) {
		Vec2 vec = new Vec2(a);
		
		return vec.scale(scalar);
	}
	

	public static Vec2 negate(Vec2 a) {
		Vec2 vec = new Vec2(a);
		
		return vec.negate();
	}
}