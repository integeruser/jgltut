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
		float x = (vector[X] * quat.vector[W]) + (vector[W] * quat.vector[X]) + (vector[Y] * quat.vector[Z]) - (vector[Z] * quat.vector[Y]);
		float y = (vector[Y] * quat.vector[W]) + (vector[W] * quat.vector[Y]) + (vector[Z] * quat.vector[X]) - (vector[X] * quat.vector[Z]);
		float z = (vector[Z] * quat.vector[W]) + (vector[W] * quat.vector[Z]) + (vector[X] * quat.vector[Y]) - (vector[Y] * quat.vector[X]);
		float w = (vector[W] * quat.vector[W]) - (vector[X] * quat.vector[X]) - (vector[Y] * quat.vector[Y]) - (vector[Z] * quat.vector[Z]);
		
		vector[X] = x;
		vector[Y] = y;
		vector[Z] = z;
		vector[W] = w;

		return this;
	}
	
	
	public Quaternion scale(float scalar) {
		vector[X] *= scalar;
		vector[Y] *= scalar;
		vector[Z] *= scalar;
		vector[W] *= scalar;

		return this;
	}
	
	
	public Quaternion negate() {
		vector[X] = -vector[X];
		vector[Y] = -vector[Y];
		vector[Z] = -vector[Z];
		vector[W] = -vector[W];

		return this;
	}
	
	public Quaternion conjugate() {
		vector[X] = -vector[X];
		vector[Y] = -vector[Y];
		vector[Z] = -vector[Z];

		return this;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static Quaternion add(Quaternion a, Quaternion b) {
		Quaternion ris = new Quaternion(a);
		
		return ris.add(b);
	}
	
	public static Quaternion mul(Quaternion a, Quaternion b) {
		Quaternion ris = new Quaternion(a);
		
		return ris.mul(b);
	}
	
	
	public static Quaternion scale(Quaternion quat, float scalar) {
		Quaternion ris = new Quaternion(quat);
		
		return ris.scale(scalar);
	}
	
	
	public static Quaternion negate(Quaternion quat) {
		Quaternion ris = new Quaternion(quat);
		
		return ris.negate();
	}

	public static Quaternion conjugate(Quaternion quat) {
		Quaternion ris = new Quaternion(quat);
		
		return ris.conjugate();
	}
}