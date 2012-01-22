package rosick.glm;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Quaternion {
	
	public float w, x, y, z;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Quaternion() {
	}
	
	public Quaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Quaternion(Quaternion quat) {
		w = quat.w;
		x = quat.x;
		y = quat.y;
		z = quat.z;
	}
	
	public Quaternion(float w, Vec3 vec) {
		this.w = w;
		x = vec.x;
		y = vec.y;
		z = vec.z;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Quaternion add(Quaternion quat) {
		w += quat.w;
		x += quat.x;
		y += quat.y;
		z += quat.z;

		return this;
	}
	
	public Quaternion mul(Quaternion quat) {
		Quaternion copy = new Quaternion(this);

		copy.w = (w * quat.w) - (x * quat.x) - (y * quat.y) - (z * quat.z);
		copy.x = (w * quat.x) + (x * quat.w) - (y * quat.z) + (z * quat.y);
		copy.y = (w * quat.y) + (x * quat.z) + (y * quat.w) - (z * quat.x);
		copy.z = (w * quat.z) - (x * quat.y) + (y * quat.x) + (z * quat.w);
		
		w = copy.w;
		x = copy.x;
		y = copy.y;
		z = copy.z;
		
		return this;
	}
	
	
	public Quaternion scale(float scalar) {
		w *= scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
		
		return this;
	}
	
	
	public Quaternion negate() {
		w = -w;
		x = -x;
		y = -y;
		z = -z;
		
		return this;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static Quaternion add(Quaternion a, Quaternion b) {
		Quaternion quat = new Quaternion(a);
		
		return quat.add(b);
	}
	
	public static Quaternion mul(Quaternion a, Quaternion b) {
		Quaternion ris = new Quaternion(a);
		
		return ris.mul(b);
	}
	
	
	public static Quaternion scale(Quaternion a, float scalar) {
		Quaternion quat = new Quaternion(a);
		
		return quat.scale(scalar);
	}
	
	
	public static Quaternion negate(Quaternion a) {
		Quaternion quat = new Quaternion(a);
		
		return quat.negate();
	}
}