package jgltut.jglsdk.glm;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class Glm {
    public static float clamp(float x, float min, float max) {
        return Math.min(Math.max(x, min), max);
    }


    public static float length(Vec3 vec) {
        return (float) Math.sqrt(dot(vec, vec));
    }

    public static float length(Vec4 vec) {
        return (float) Math.sqrt(dot(vec, vec));
    }

    public static float length(Quaternion quat) {
        return (float) Math.sqrt(dot(quat, quat));
    }


    public static Vec3 normalize(Vec3 vec) {
        Vec3 vecNormalized = new Vec3();

        float invLength = 1.0f / length(vec);
        vecNormalized.x = vec.x * invLength;
        vecNormalized.y = vec.y * invLength;
        vecNormalized.z = vec.z * invLength;

        return vecNormalized;
    }

    public static Vec4 normalize(Vec4 vec) {
        Vec4 vecNormalized = new Vec4();

        float invLength = 1.0f / length(vec);
        vecNormalized.x = vec.x * invLength;
        vecNormalized.y = vec.y * invLength;
        vecNormalized.z = vec.z * invLength;
        vecNormalized.w = vec.w * invLength;

        return vecNormalized;
    }

    public static Quaternion normalize(Quaternion quat) {
        Quaternion quatNormalized = new Quaternion();

        float invLength = 1.0f / length(quat);
        quatNormalized.x = quat.x * invLength;
        quatNormalized.y = quat.y * invLength;
        quatNormalized.z = quat.z * invLength;
        quatNormalized.w = quat.w * invLength;

        return quatNormalized;
    }


    public static float dot(Vec3 lhs, Vec3 rhs) {
        return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z;
    }

    public static float dot(Vec4 lhs, Vec4 rhs) {
        return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z + lhs.w * rhs.w;
    }

    public static float dot(Quaternion lhs, Quaternion rhs) {
        return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z + lhs.w * rhs.w;
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

    public static Vec4 mix(Vec4 x, Vec4 y, float a) {
        return Vec4.add(x, Vec4.sub(y, x).scale(a));
    }


    public static Quaternion conjugate(Quaternion quat) {
        return Quaternion.conjugate(quat);
    }


    public static Quaternion angleAxis(float angle, Vec3 vec) {
        Quaternion res = new Quaternion();

        float a = (float) Math.toRadians(angle);
        float s = (float) Math.sin(a * 0.5);

        res.x = vec.x * s;
        res.y = vec.y * s;
        res.z = vec.z * s;
        res.w = (float) Math.cos(a * 0.5);

        return res;
    }


    public static Quaternion quatCast(Mat4 mat) {
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

        return res;
    }


    public static Mat3 inverse(Mat3 mat) {
        Mat3 res = new Mat3();

        res.matrix[0] = +(mat.matrix[4] * mat.matrix[8] - mat.matrix[7] * mat.matrix[5]);
        res.matrix[3] = -(mat.matrix[3] * mat.matrix[8] - mat.matrix[6] * mat.matrix[5]);
        res.matrix[6] = +(mat.matrix[3] * mat.matrix[7] - mat.matrix[6] * mat.matrix[4]);
        res.matrix[1] = -(mat.matrix[1] * mat.matrix[8] - mat.matrix[7] * mat.matrix[2]);
        res.matrix[4] = +(mat.matrix[0] * mat.matrix[8] - mat.matrix[6] * mat.matrix[2]);
        res.matrix[7] = -(mat.matrix[0] * mat.matrix[7] - mat.matrix[6] * mat.matrix[1]);
        res.matrix[2] = +(mat.matrix[1] * mat.matrix[5] - mat.matrix[4] * mat.matrix[2]);
        res.matrix[5] = -(mat.matrix[0] * mat.matrix[5] - mat.matrix[3] * mat.matrix[2]);
        res.matrix[8] = +(mat.matrix[0] * mat.matrix[4] - mat.matrix[3] * mat.matrix[1]);

        float determinant = mat.matrix[0] * (mat.matrix[4] * mat.matrix[8] - mat.matrix[5] * mat.matrix[7])
                + mat.matrix[1] * (mat.matrix[5] * mat.matrix[6] - mat.matrix[3] * mat.matrix[8])
                + mat.matrix[2] * (mat.matrix[3] * mat.matrix[7] - mat.matrix[4] * mat.matrix[6]);

        return res.scale(1.0f / determinant);
    }

    public static Mat4 inverse(Mat4 mat) {
        float coef00 = mat.matrix[10] * mat.matrix[15] - mat.matrix[14] * mat.matrix[11];
        float coef02 = mat.matrix[6] * mat.matrix[15] - mat.matrix[14] * mat.matrix[7];
        float coef03 = mat.matrix[6] * mat.matrix[11] - mat.matrix[10] * mat.matrix[7];

        float coef04 = mat.matrix[9] * mat.matrix[15] - mat.matrix[13] * mat.matrix[11];
        float coef06 = mat.matrix[5] * mat.matrix[15] - mat.matrix[13] * mat.matrix[7];
        float coef07 = mat.matrix[5] * mat.matrix[11] - mat.matrix[9] * mat.matrix[7];

        float coef08 = mat.matrix[9] * mat.matrix[14] - mat.matrix[13] * mat.matrix[10];
        float coef10 = mat.matrix[5] * mat.matrix[14] - mat.matrix[13] * mat.matrix[6];
        float coef11 = mat.matrix[5] * mat.matrix[10] - mat.matrix[9] * mat.matrix[6];

        float coef12 = mat.matrix[8] * mat.matrix[15] - mat.matrix[12] * mat.matrix[11];
        float coef14 = mat.matrix[4] * mat.matrix[15] - mat.matrix[12] * mat.matrix[7];
        float coef15 = mat.matrix[4] * mat.matrix[11] - mat.matrix[8] * mat.matrix[7];

        float coef16 = mat.matrix[8] * mat.matrix[14] - mat.matrix[12] * mat.matrix[10];
        float coef18 = mat.matrix[4] * mat.matrix[14] - mat.matrix[12] * mat.matrix[6];
        float coef19 = mat.matrix[4] * mat.matrix[10] - mat.matrix[8] * mat.matrix[6];

        float coef20 = mat.matrix[8] * mat.matrix[13] - mat.matrix[12] * mat.matrix[9];
        float coef22 = mat.matrix[4] * mat.matrix[13] - mat.matrix[12] * mat.matrix[5];
        float coef23 = mat.matrix[4] * mat.matrix[9] - mat.matrix[8] * mat.matrix[5];

        final Vec4 signA = new Vec4(+1, -1, +1, -1);
        final Vec4 signB = new Vec4(-1, +1, -1, +1);

        Vec4 fac0 = new Vec4(coef00, coef00, coef02, coef03);
        Vec4 fac1 = new Vec4(coef04, coef04, coef06, coef07);
        Vec4 fac2 = new Vec4(coef08, coef08, coef10, coef11);
        Vec4 fac3 = new Vec4(coef12, coef12, coef14, coef15);
        Vec4 fac4 = new Vec4(coef16, coef16, coef18, coef19);
        Vec4 fac5 = new Vec4(coef20, coef20, coef22, coef23);

        Vec4 vec0 = new Vec4(mat.matrix[4], mat.matrix[0], mat.matrix[0], mat.matrix[0]);
        Vec4 vec1 = new Vec4(mat.matrix[5], mat.matrix[1], mat.matrix[1], mat.matrix[1]);
        Vec4 vec2 = new Vec4(mat.matrix[6], mat.matrix[2], mat.matrix[2], mat.matrix[2]);
        Vec4 vec3 = new Vec4(mat.matrix[7], mat.matrix[3], mat.matrix[3], mat.matrix[3]);

        Vec4 inv0 = Vec4.mul(signA, Vec4.mul(vec1, fac0).sub(Vec4.mul(vec2, fac1)).add(Vec4.mul(vec3, fac2)));
        Vec4 inv1 = Vec4.mul(signB, Vec4.mul(vec0, fac0).sub(Vec4.mul(vec2, fac3)).add(Vec4.mul(vec3, fac4)));
        Vec4 inv2 = Vec4.mul(signA, Vec4.mul(vec0, fac1).sub(Vec4.mul(vec1, fac3)).add(Vec4.mul(vec3, fac5)));
        Vec4 inv3 = Vec4.mul(signB, Vec4.mul(vec0, fac2).sub(Vec4.mul(vec1, fac4)).add(Vec4.mul(vec2, fac5)));

        Mat4 res = new Mat4(inv0, inv1, inv2, inv3);

        Vec4 row0 = new Vec4(res.matrix[0], res.matrix[4], res.matrix[8], res.matrix[12]);

        float determinant = Glm.dot(mat.getColumn(0), row0);

        return res.scale(1.0f / determinant);
    }


    public static Mat4 translate(Mat4 mat, Vec3 vec) {
        Vec4 temp0 = mat.getColumn(0).scale(vec.x);
        Vec4 temp1 = mat.getColumn(1).scale(vec.y);
        Vec4 temp2 = mat.getColumn(2).scale(vec.z);
        Vec4 temp3 = mat.getColumn(3);

        Vec4 temp = temp0.add(temp1).add(temp2).add(temp3);

        Mat4 res = new Mat4(mat);
        res.setColumn(3, temp);

        return res;
    }

    public static Mat4 scale(Mat4 mat, Vec3 vec) {
        Mat4 result = new Mat4();
        result.setColumn(0, Vec4.scale(mat.getColumn(0), vec.x));
        result.setColumn(1, Vec4.scale(mat.getColumn(1), vec.y));
        result.setColumn(2, Vec4.scale(mat.getColumn(2), vec.z));
        result.setColumn(3, mat.getColumn(3));

        return result;
    }


    public static Mat3 transpose(Mat3 mat) {
        Mat3 res = new Mat3();

        res.matrix[0] = mat.matrix[0];
        res.matrix[1] = mat.matrix[3];
        res.matrix[2] = mat.matrix[6];

        res.matrix[3] = mat.matrix[1];
        res.matrix[4] = mat.matrix[4];
        res.matrix[5] = mat.matrix[7];

        res.matrix[6] = mat.matrix[2];
        res.matrix[7] = mat.matrix[5];
        res.matrix[8] = mat.matrix[8];

        return res;
    }

    public static Mat4 transpose(Mat4 mat) {
        Mat4 res = new Mat4();

        res.matrix[0] = mat.matrix[0];
        res.matrix[1] = mat.matrix[4];
        res.matrix[2] = mat.matrix[8];
        res.matrix[3] = mat.matrix[12];

        res.matrix[4] = mat.matrix[1];
        res.matrix[5] = mat.matrix[5];
        res.matrix[6] = mat.matrix[9];
        res.matrix[7] = mat.matrix[13];

        res.matrix[8] = mat.matrix[2];
        res.matrix[9] = mat.matrix[6];
        res.matrix[10] = mat.matrix[10];
        res.matrix[11] = mat.matrix[14];

        res.matrix[12] = mat.matrix[3];
        res.matrix[13] = mat.matrix[7];
        res.matrix[14] = mat.matrix[11];
        res.matrix[15] = mat.matrix[15];

        return res;
    }


    public static Mat4 perspective(float fovY, float aspect, float zNear, float zFar) {
        float range = (float) (Math.tan(Math.toRadians(fovY / 2.0f)) * zNear);
        float left = -range * aspect;
        float right = range * aspect;
        float bottom = -range;
        float top = range;

        Mat4 res = new Mat4(0.0f);

        res.matrix[0] = (2.0f * zNear) / (right - left);
        res.matrix[5] = (2.0f * zNear) / (top - bottom);
        res.matrix[10] = -(zFar + zNear) / (zFar - zNear);
        res.matrix[11] = -1.0f;
        res.matrix[14] = -(2.0f * zFar * zNear) / (zFar - zNear);

        return res;
    }


    public static Mat4 matCast(Quaternion quat) {
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

        return res;
    }


    public static Mat4 rotate(Mat4 mat, float angle, Vec3 vec) {
        float a = (float) Math.toRadians(angle);
        float c = (float) Math.cos(a);
        float s = (float) Math.sin(a);

        Vec3 axis = normalize(vec);

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

        return result;
    }

    public static Quaternion rotate(Quaternion quat, float angle, Vec3 vec) {
        Vec3 tmp = new Vec3(vec);

        // Axis of rotation must be normalised
        float len = length(tmp);
        if (Math.abs(len - 1.0f) > 0.001f) {
            float oneOverLen = 1.0f / len;
            tmp.x *= oneOverLen;
            tmp.y *= oneOverLen;
            tmp.z *= oneOverLen;
        }

        float angleRad = (float) Math.toRadians(angle);
        float fSin = (float) Math.sin(angleRad * 0.5f);

        Quaternion res = new Quaternion((float) Math.cos(angleRad * 0.5f), tmp.x * fSin, tmp.y * fSin, tmp.z * fSin);

        return Quaternion.mul(quat, res);
    }


    public static Mat4 lookAt(Vec3 eye, Vec3 center, Vec3 up) {
        Vec3 f = normalize(Vec3.sub(center, eye));
        Vec3 u = normalize(up);
        Vec3 s = normalize(cross(f, u));
        u = cross(s, f);

        Mat4 result = new Mat4(1.0f);
        result.set(0, 0, s.x);
        result.set(1, 0, s.y);
        result.set(2, 0, s.z);
        result.set(0, 1, u.x);
        result.set(1, 1, u.y);
        result.set(2, 1, u.z);
        result.set(0, 2, -f.x);
        result.set(1, 2, -f.y);
        result.set(2, 2, -f.z);

        return translate(result, Vec3.negate(eye));
    }
}
