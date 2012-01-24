package rosick.glm;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public class Quaternion extends Vec {
	
	public Quaternion() {
		vector = new float[4];
	}
	
	public Quaternion(float w, float x, float y, float z) {
		vector = new float[4];
		vector[X] = x;
		vector[Y] = y;
		vector[Z] = z;
		vector[W] = w;
	}
	
	public Quaternion(Quaternion quat) {
		vector = new float[4];
		vector[X] = quat.vector[X];
		vector[Y] = quat.vector[Y];
		vector[Z] = quat.vector[Z];
		vector[W] = quat.vector[W];
	}
	
	public Quaternion(float w, Vec3 vec) {
		vector = new float[4];
		vector[X] = vec.vector[X];
		vector[Y] = vec.vector[Y];
		vector[Z] = vec.vector[Z];
		vector[W] = w;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Quaternion add(Quaternion quat) {
		vector[X] += quat.vector[X];
		vector[Y] += quat.vector[Y];
		vector[Z] += quat.vector[Z];
		vector[W] += quat.vector[W];

		return this;
	}
	
	public Quaternion sub(Quaternion quat) {
		vector[X] -= quat.vector[X];
		vector[Y] -= quat.vector[Y];
		vector[Z] -= quat.vector[Z];
		vector[W] -= quat.vector[W];

		return this;
	}
	
	public Quaternion mul(Quaternion quat) {
		Quaternion copy = new Quaternion(this);

		copy.vector[W] = (vector[W] * quat.vector[W]) - (vector[X] * quat.vector[X]) - (vector[Y] * quat.vector[Y]) - (vector[Z] * quat.vector[Z]);
		copy.vector[X] = (vector[W] * quat.vector[X]) + (vector[X] * quat.vector[W]) - (vector[Y] * quat.vector[Z]) + (vector[Z] * quat.vector[Y]);
		copy.vector[Y] = (vector[W] * quat.vector[Y]) + (vector[X] * quat.vector[Z]) + (vector[Y] * quat.vector[W]) - (vector[Z] * quat.vector[X]);
		copy.vector[Z] = (vector[W] * quat.vector[Z]) - (vector[X] * quat.vector[Y]) + (vector[Y] * quat.vector[X]) + (vector[Z] * quat.vector[W]);
		
		vector[W] = copy.vector[W];
		vector[X] = copy.vector[X];
		vector[Y] = copy.vector[Y];
		vector[Z] = copy.vector[Z];
		
		return this;
	}
	
	
	public Quaternion scale(float scalar) {
		vector[W] *= scalar;
		vector[X] *= scalar;
		vector[Y] *= scalar;
		vector[Z] *= scalar;
		
		return this;
	}
	
	
	public Quaternion negate() {
		vector[W] = -vector[W];
		vector[X] = -vector[X];
		vector[Y] = -vector[Y];
		vector[Z] = -vector[Z];
		
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