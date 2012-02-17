package rosick.jglsdk.glm;

import java.nio.FloatBuffer;

import rosick.PortingUtils.BufferableData;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Quaternion extends BufferableData<FloatBuffer> {
	
	public float w, x, y, z;

	
	public Quaternion() {
	}
	
	public Quaternion(float w, float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Quaternion(Quaternion quat) {
		x = quat.x;
		y = quat.y;
		z = quat.z;
		w = quat.w;
	}
	
	public Quaternion(float w, Vec3 vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
		this.w = w;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	

	@Override
	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(w);
		buffer.put(x);
		buffer.put(y);
		buffer.put(z);

		return buffer;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Quaternion add(Quaternion quat) {
		x += quat.x;
		y += quat.y;
		z += quat.z;
		w += quat.w;

		return this;
	}
	
	public Quaternion sub(Quaternion quat) {
		x -= quat.x;
		y -= quat.y;
		z -= quat.z;
		w -= quat.w;

		return this;
	}
	
	public Quaternion mul(Quaternion quat) {	
		float newX = (x * quat.w) + (w * quat.x) + (y * quat.z) - (z * quat.y);
		float newY = (y * quat.w) + (w * quat.y) + (z * quat.x) - (x * quat.z);
		float newZ = (z * quat.w) + (w * quat.z) + (x * quat.y) - (y * quat.x);
		float newW = (w * quat.w) - (x * quat.x) - (y * quat.y) - (z * quat.z);
		
		x = newX;
		y = newY;
		z = newZ;
		w = newW;

		return this;
	}
	
	public Vec3 mul(Vec3 v) {	
		float two = 2.0f;
		Vec3 uv, uuv;
		Vec3 quatVector = new Vec3(x, y, z);

		uv = Glm.cross(quatVector, v);
		uuv = Glm.cross(quatVector, uv);
			
		uv.scale(two * w); 
		uuv.scale(two); 

		return v.add(uv).add(uuv);
	}
	
	
	public Quaternion scale(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		w *= scalar;

		return this;
	}
	
	
	public Quaternion negate() {
		x = -x;
		y = -y;
		z = -z;
		w = -w;

		return this;
	}
	
	public Quaternion conjugate() {
		x = -x;
		y = -y;
		z = -z;

		return this;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static Quaternion add(Quaternion a, Quaternion b) {
		Quaternion res = new Quaternion(a);
		
		return res.add(b);
	}
	
	public static Quaternion mul(Quaternion a, Quaternion b) {
		Quaternion res = new Quaternion(a);
		
		return res.mul(b);
	}
	
	public static Vec3 mul(Quaternion a, Vec3 b) {
		Quaternion res = new Quaternion(a);
		
		return res.mul(b);
	}
	
	
	public static Quaternion scale(Quaternion quat, float scalar) {
		Quaternion res = new Quaternion(quat);
		
		return res.scale(scalar);
	}
	
	
	public static Quaternion negate(Quaternion quat) {
		Quaternion res = new Quaternion(quat);
		
		return res.negate();
	}

	public static Quaternion conjugate(Quaternion quat) {
		Quaternion res = new Quaternion(quat);
		
		return res.conjugate();
	}
}