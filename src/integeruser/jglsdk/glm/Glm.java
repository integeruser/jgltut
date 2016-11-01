package integeruser.jglsdk.glm;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 */
public class Glm {
    public static float clamp(float x, float min, float max) {
        return Math.min(Math.max(x, min), max);
    }
}
