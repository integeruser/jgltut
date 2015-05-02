package jglsdk.glm;

import jglsdk.BufferableData;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class Quaternion extends BufferableData<FloatBuffer> {
    public float x, y, z, w;


    public Quaternion() {
    }

    public Quaternion(float w) {
        this.w = w;
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

    ////////////////////////////////
    @Override
    public FloatBuffer fillBuffer(FloatBuffer buffer) {
        buffer.put(w);
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);
        return buffer;
    }

    ////////////////////////////////
    public Quaternion add(Quaternion rhs) {
        x += rhs.x;
        y += rhs.y;
        z += rhs.z;
        w += rhs.w;
        return this;
    }

    public Quaternion sub(Quaternion rhs) {
        x -= rhs.x;
        y -= rhs.y;
        z -= rhs.z;
        w -= rhs.w;
        return this;
    }

    public Quaternion mul(Quaternion rhs) {
        float newX = (x * rhs.w) + (w * rhs.x) + (y * rhs.z) - (z * rhs.y);
        float newY = (y * rhs.w) + (w * rhs.y) + (z * rhs.x) - (x * rhs.z);
        float newZ = (z * rhs.w) + (w * rhs.z) + (x * rhs.y) - (y * rhs.x);
        float newW = (w * rhs.w) - (x * rhs.x) - (y * rhs.y) - (z * rhs.z);
        x = newX;
        y = newY;
        z = newZ;
        w = newW;
        return this;
    }

    public Vec3 mul(Vec3 rhs) {
        float two = 2.0f;
        Vec3 uv, uuv;
        Vec3 quatVector = new Vec3(x, y, z);

        uv = Glm.cross(quatVector, rhs);
        uuv = Glm.cross(quatVector, uv);

        uv.scale(two * w);
        uuv.scale(two);

        return rhs.add(uv).add(uuv);
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

    ////////////////////////////////
    public static Quaternion add(Quaternion lhs, Quaternion rhs) {
        Quaternion res = new Quaternion(lhs);
        return res.add(rhs);
    }

    public static Quaternion mul(Quaternion lhs, Quaternion rhs) {
        Quaternion res = new Quaternion(lhs);
        return res.mul(rhs);
    }

    public static Vec3 mul(Quaternion lhs, Vec3 rhs) {
        Quaternion res = new Quaternion(lhs);
        return res.mul(rhs);
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