package rosick.glm;


/**
 * @author integeruser
 */
public class Vec3 {
	
	public float x, y, z;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public Vec3() {
		x = 0;
		y = 0;
		z = 0;
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
	

	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vec3 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}
	
	
	public Vec3 normalize() {
		if (x == 0 && y == 0 && z == 0) return this;
		
		float magnitude = length();
		x = x / magnitude;
		y = y / magnitude;
		z = z / magnitude;
		
		return this;
	}
	
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public float squaredLength() {
		return x * x + y * y + z * z;
	}
	

	public float dot(Vec3 vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}
	

	public Vec3 cross(Vec3 vec) {
		Vec3 res = cross(this, vec);

		x = res.x;
		y = res.y;
		z = res.z;
		
		return this;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public static Vec3 add(Vec3 a, Vec3 b) {
		return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
	
	public static Vec3 sub(Vec3 a, Vec3 b) {
		return new Vec3(a.x - b.x, a.y - b.y, a.z - b.z);
	}	
	
	
	public static Vec3 scale(Vec3 vec, float scalar) {
		return new Vec3(scalar * vec.x, scalar * vec.y, scalar * vec.z);
	}
	

	public static Vec3 negate(Vec3 vec) {
		return new Vec3(-vec.x, -vec.y, -vec.z);
	}
	

	public static float dot(Vec3 a, Vec3 b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}
	
	
	public static Vec3 cross(Vec3 a, Vec3 b) {
		Vec3 res = new Vec3();
		res.x = a.y * b.z - a.z * b.y;
		res.y = a.z * b.x - a.x * b.z;
		res.z = a.x * b.y - a.y * b.x;

		return res;
	}

	public static Vec3 cross(Vec3 a, Vec3 b, Vec3 c) {
		return cross(sub(b, a), sub(c, a));
	}
	
		
	public static Vec3 getNormal(Vec3 a, Vec3 b) {
		return cross(a,b).normalize();
	}
	
	public static Vec3 getNormal(Vec3 a, Vec3 b, Vec3 c) {
		return cross(a,b).normalize();
	}
	
	
	public static float getAngle(Vec3 a, Vec3 b) {
		a.normalize();
		b.normalize();
		
		return (a.x * b.x + a.y * b.y + a.z * b.z);
	}
}