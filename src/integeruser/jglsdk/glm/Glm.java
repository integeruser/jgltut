package integeruser.jglsdk.glm;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class Glm {
    public static float clamp(float x, float min, float max) {
        return Math.min(Math.max(x, min), max);
    }


    public static float mix(float x, float y, float a) {
        return x + a * (y - x);
    }
}
