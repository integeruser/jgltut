package rosick.common.math;


public class Vector3 {
	
	public static final Vector3 RIGHT	= new Vector3(1.0f, 0.0f, 0.0f);		
	public static final Vector3 UP 		= new Vector3(0.0f, 1.0f, 0.0f);		
	public static final Vector3 FORWARD	= new Vector3(0.0f, 0.0f, 1.0f);	
	
	public float x, y, z;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public Vector3() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3(Vector3 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public Vector3 add(Vector3 vec) {
		x += vec.x;
		y += vec.y;
		z += vec.z;
		
		return this;
	}
	
	public Vector3 sub(Vector3 vec) {
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		
		return this;
	}
	
	
	public Vector3 scale(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		
		return this;
	}
		
	public Vector3 addScaledVector(Vector3 vec, float scale) {
		this.x = x + vec.x * scale;
		this.y = y + vec.y * scale;
		this.z = z + vec.z * scale;
		
		return this;
	}
	

	public Vector3 negate() {
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

	public void set(Vector3 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}
	
	
	public Vector3 normalize() {
		if (x == 0 && y == 0 && z == 0) return this;
		
		float magnitude = magnitude();
		x = x / magnitude;
		y = y / magnitude;
		z = z / magnitude;
		
		return this;
	}
	
	
	public float magnitude() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public float sqrMagnitude() {
		return x * x + y * y + z * z;
	}
	

	public float dot(Vector3 vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}
	

	public Vector3 cross(Vector3 vec) {
		Vector3 res = cross(this, vec);

		x = res.x;
		y = res.y;
		z = res.z;
		
		return this;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public static Vector3 add(Vector3 a, Vector3 b) {
		return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
	
	public static Vector3 sub(Vector3 a, Vector3 b) {
		return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
	}	
	
	public static Vector3 scale(Vector3 vec, float scalar) {
		return new Vector3(scalar * vec.x, scalar * vec.y, scalar * vec.z);
	}
	

	public static Vector3 negate(Vector3 vec) {
		return new Vector3(-vec.x, -vec.y, -vec.z);
	}
	

	public static float dot(Vector3 a, Vector3 b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}
	
	
	public static Vector3 cross(Vector3 a, Vector3 b) {
		Vector3 res = new Vector3();
		res.x = a.y * b.z - a.z * b.y;
		res.y = a.z * b.x - a.x * b.z;
		res.z = a.x * b.y - a.y * b.x;

		return res;
	}

	public static Vector3 cross(Vector3 a, Vector3 b, Vector3 c) {
		return cross(sub(b, a), sub(c, a));
	}
	
	
	
	public static Vector3 getNormal(Vector3 a, Vector3 b) {
		return cross(a,b).normalize();
	}
	
	public static Vector3 getNormal(Vector3 a, Vector3 b, Vector3 c) {
		return cross(a,b).normalize();
	}
	
	
	public static float getAngle(Vector3 a, Vector3 b) {
		a.normalize();
		b.normalize();
		
		return (a.x * b.x + a.y * b.y + a.z * b.z);
	}
}