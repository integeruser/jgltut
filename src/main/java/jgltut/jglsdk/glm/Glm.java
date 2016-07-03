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


    public static Quaternionf quatCast(Matrix4f matNew) {
        Mat4 mat = toMat(matNew);

        float fourXSquaredMinus1 = mat.matrix[0] - mat.matrix[5] - mat.matrix[10];
        float fourYSquaredMinus1 = mat.matrix[5] - mat.matrix[0] - mat.matrix[10];
        float fourZSquaredMinus1 = mat.matrix[10] - mat.matrix[0] - mat.matrix[5];
        float fourWSquaredMinus1 = mat.matrix[0] + mat.matrix[5] + mat.matrix[10];

        int biggestIndex = 0;
        float fourBiggestSquaredMinus1 = fourWSquaredMinus1;

        if (fourXSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourXSquaredMinus1;
            biggestIndex = 1;
        }

        if (fourYSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourYSquaredMinus1;
            biggestIndex = 2;
        }

        if (fourZSquaredMinus1 > fourBiggestSquaredMinus1) {
            fourBiggestSquaredMinus1 = fourZSquaredMinus1;
            biggestIndex = 3;
        }

        float biggestVal = (float) (Math.sqrt(fourBiggestSquaredMinus1 + 1) * 0.5f);
        float mult = 0.25f / biggestVal;

        Quaternion res = new Quaternion();

        switch (biggestIndex) {
            case 0:
                res.w = biggestVal;
                res.x = (mat.matrix[6] - mat.matrix[9]) * mult;
                res.y = (mat.matrix[8] - mat.matrix[2]) * mult;
                res.z = (mat.matrix[1] - mat.matrix[4]) * mult;
                break;

            case 1:
                res.w = (mat.matrix[6] - mat.matrix[9]) * mult;
                res.x = biggestVal;
                res.y = (mat.matrix[1] + mat.matrix[4]) * mult;
                res.z = (mat.matrix[8] + mat.matrix[2]) * mult;
                break;

            case 2:
                res.w = (mat.matrix[8] - mat.matrix[2]) * mult;
                res.x = (mat.matrix[1] + mat.matrix[4]) * mult;
                res.y = biggestVal;
                res.z = (mat.matrix[6] + mat.matrix[9]) * mult;
                break;

            case 3:
                res.w = (mat.matrix[1] - mat.matrix[4]) * mult;
                res.x = (mat.matrix[8] + mat.matrix[2]) * mult;
                res.y = (mat.matrix[6] + mat.matrix[9]) * mult;
                res.z = biggestVal;
                break;
        }

        return toQuatNew(res);
    }


    public static Mat4 translate(Mat4 m, Vec3 v) {
        return toMat(translate(toMatNew(m), toVec3new(v)));
    }
    public static Matrix4f translate(Matrix4f m, Vector3f v) {
        Vec3 vec = toVec3(v);
        Mat4 mat = toMat(m);

        Vec4 temp0 = mat.getColumn(0).scale(vec.x);
        Vec4 temp1 = mat.getColumn(1).scale(vec.y);
        Vec4 temp2 = mat.getColumn(2).scale(vec.z);
        Vec4 temp3 = mat.getColumn(3);

        Vec4 temp = temp0.add(temp1).add(temp2).add(temp3);

        Mat4 res = new Mat4(mat);
        res.setColumn(3, temp);

        return toMatNew(res);
    }

    public static Matrix4f scale(Matrix4f mat, Vector3f vec) {
        return toMatNew(scale(toMat(mat), toVec3(vec)));
    }
    public static Mat4 scale(Mat4 mat, Vec3 vec) {
        Mat4 result = new Mat4();
        result.setColumn(0, Vec4.scale(mat.getColumn(0), vec.x));
        result.setColumn(1, Vec4.scale(mat.getColumn(1), vec.y));
        result.setColumn(2, Vec4.scale(mat.getColumn(2), vec.z));
        result.setColumn(3, mat.getColumn(3));

        return result;
    }


    public static Matrix4f mat4Cast(Quaternionf quatNew) {
        Quaternion quat = toQuat(quatNew);
        //  Converts this quaternion to a rotation matrix.
        //  | 1 - 2(y^2 + z^2)	2(xy + wz)			2(xz - wy)			0  |
        //  | 2(xy - wz)		1 - 2(x^2 + z^2)	2(yz + wx)			0  |
        //  | 2(xz + wy)		2(yz - wx)			1 - 2(x^2 + y^2)	0  |
        //  | 0					0					0					1  |

        Mat4 res = new Mat4(0.0f);

        res.matrix[0] = 1 - 2 * quat.y * quat.y - 2 * quat.z * quat.z;
        res.matrix[1] = 2 * quat.x * quat.y + 2 * quat.w * quat.z;
        res.matrix[2] = 2 * quat.x * quat.z - 2 * quat.w * quat.y;

        res.matrix[4] = 2 * quat.x * quat.y - 2 * quat.w * quat.z;
        res.matrix[5] = 1 - 2 * quat.x * quat.x - 2 * quat.z * quat.z;
        res.matrix[6] = 2 * quat.y * quat.z + 2 * quat.w * quat.x;

        res.matrix[8] = 2 * quat.x * quat.z + 2 * quat.w * quat.y;
        res.matrix[9] = 2 * quat.y * quat.z - 2 * quat.w * quat.x;
        res.matrix[10] = 1 - 2 * quat.x * quat.x - 2 * quat.y * quat.y;

        res.matrix[12] = 0.0f;
        res.matrix[13] = 0.0f;
        res.matrix[14] = 0.0f;
        res.matrix[15] = 1.0f;

        return toMatNew(res);
    }


    public static Matrix4f rotate(Matrix4f m, float angle, Vector3f v) {
        Mat4 mat = toMat(m);
        Vec3 vec = toVec3(v);

        float a = (float) Math.toRadians(angle);
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);

        Vec3 axis = toVec3(toVec3new(vec).normalize());

        Vec3 temp = Vec3.scale(axis, 1.0f - c);

        Mat4 rotate = new Mat4();
        rotate.set(0, c + temp.x * axis.x);
        rotate.set(1, 0 + temp.x * axis.y + s * axis.z);
        rotate.set(2, 0 + 0 + temp.x * axis.z - s * axis.y);

        rotate.set(4, 0 + temp.y * axis.x - s * axis.z);
        rotate.set(5, c + temp.y * axis.y);
        rotate.set(6, 0 + temp.y * axis.z + s * axis.x);

        rotate.set(8, 0 + temp.z * axis.x + s * axis.y);
        rotate.set(9, 0 + temp.z * axis.y - s * axis.x);
        rotate.set(10, c + temp.z * axis.z);

        Mat4 result = new Mat4();
        result.setColumn(0, Vec4.scale(mat.getColumn(0), rotate.matrix[0]).add(Vec4.scale(mat.getColumn(1), rotate.matrix[1])).add(Vec4.scale(mat.getColumn(2), rotate.matrix[2])));
        result.setColumn(1, Vec4.scale(mat.getColumn(0), rotate.matrix[4]).add(Vec4.scale(mat.getColumn(1), rotate.matrix[5])).add(Vec4.scale(mat.getColumn(2), rotate.matrix[6])));
        result.setColumn(2, Vec4.scale(mat.getColumn(0), rotate.matrix[8]).add(Vec4.scale(mat.getColumn(1), rotate.matrix[9])).add(Vec4.scale(mat.getColumn(2), rotate.matrix[10])));
        result.setColumn(3, mat.getColumn(3));
        return toMatNew(result);
    }

    public static Quaternionf rotate(Quaternionf q, float angle, Vector3f v) {
        Quaternion quat = toQuat(q);
        Vec3 vec = toVec3(v);
        Vec3 tmp = new Vec3(vec);

        // Axis of rotation must be normalised
        float len = toVec3new(tmp).length();
        if (Math.abs(len - 1.0f) > 0.001f) {
            float oneOverLen = 1.0f / len;
            tmp.x *= oneOverLen;
            tmp.y *= oneOverLen;
            tmp.z *= oneOverLen;
        }

        float angleRad = (float) Math.toRadians(angle);
        float fSin = (float) Math.sin(angleRad * 0.5f);

        Quaternion res = new Quaternion((float) Math.cos(angleRad * 0.5f), tmp.x * fSin, tmp.y * fSin, tmp.z * fSin);

        return toQuatNew(Quaternion.mul(quat, res));
    }


    private static Vec3 toVec3(Vector3f vec) {
        return new Vec3(vec.x, vec.y, vec.z);
    }

    private static Vector3f toVec3new(Vec3 vec) {
        return new Vector3f(vec.x, vec.y, vec.z);
    }

    private static Quaternion toQuat(Quaternionf q) {
        return new Quaternion(q.w, q.x, q.y, q.z);
    }

    private static Quaternionf toQuatNew(Quaternion q) {
        return new Quaternionf(q.x, q.y, q.z, q.w);
    }

    private static Mat4 toMat(Matrix4f mat) {
        Mat4 res = new Mat4();
        mat.get(res.get());
        return res;
    }

    private static Matrix4f toMatNew(Mat4 mat) {
        Matrix4f res = new Matrix4f();
        res.set(mat.get());
        return res;
    }
}
