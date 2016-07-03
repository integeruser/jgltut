package jgltut.jglsdk.glm;


import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class Glm {
    public static float clamp(float x, float min, float max) {
        return Math.min(Math.max(x, min), max);
    }


    public static Vec3 cross(Vec3 lhs, Vec3 rhs) {
        Vec3 cross = new Vec3();

        cross.x = lhs.y * rhs.z - lhs.z * rhs.y;
        cross.y = lhs.z * rhs.x - lhs.x * rhs.z;
        cross.z = lhs.x * rhs.y - lhs.y * rhs.x;

        return cross;
    }


    public static float mix(float x, float y, float a) {
        return x + a * (y - x);
    }

    public static Vector4f mix(Vector4f x, Vector4f y, float a) {
        return new Vector4f(x).add((new Vector4f(y).sub(x)).mul(a));
    }
}
