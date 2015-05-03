package jgltut.framework;

import jgltut.jglsdk.glm.Glm;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class Timer {
    public enum Type {
        LOOP,
        SINGLE
    }

    ////////////////////////////////
    public Timer(Type type, float duration) {
        this.type = type;
        secDuration = duration;
    }

    ////////////////////////////////

    /**
     * Updates the time for the timer. Returns true if the timer has reached the end.
     * Will only return true for SINGLE timers that have reached their duration.
     *
     * @param elapsedTime the time passed since the application start (in milliseconds)
     */
    public boolean update(float elapsedTime) {
        float absCurrTime = elapsedTime / 1000.0f;

        if (!hasUpdated) {
            absPrevTime = absCurrTime;
            hasUpdated = true;
        }

        if (isPaused) {
            absPrevTime = absCurrTime;
            return false;
        }

        float deltaTime = absCurrTime - absPrevTime;
        secAccumTime += deltaTime;

        absPrevTime = absCurrTime;
        if (type == Type.SINGLE) {
            return secAccumTime > secDuration;
        }

        return false;
    }


    /**
     * Subtracts secRewind from the current time and continues from there.
     */
    public void rewind(float secRewind) {
        secAccumTime -= secRewind;

        if (secAccumTime < 0.0f) {
            secAccumTime = 0.0f;
        }
    }

    /**
     * Adds secRewind to the current time and continues from there.
     */
    public void fastForward(float secFF) {
        secAccumTime += secFF;
    }


    /**
     * Returns true if the timer is paused.
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Pauses/unpauses. Returns true if the timer is paused after the toggling.
     */
    public boolean togglePause() {
        isPaused = !isPaused;
        return isPaused;
    }

    /**
     * Sets the pause state to the given value.
     */
    public void setPause(boolean pause) {
        isPaused = pause;
    }


    /**
     * Returns a number [0, 1], representing progress through the duration.
     * Only used for SINGLE and LOOP timers.
     */
    public float getAlpha() {
        switch (type) {
            case LOOP:
                return (secAccumTime % secDuration) / secDuration;

            case SINGLE:
                return Glm.clamp(secAccumTime / secDuration, 0.0f, 1.0f);

            default:
                break;
        }

        return -1.0f;  // Garbage.
    }

    ////////////////////////////////

    private Type type;
    private float secDuration;

    private boolean hasUpdated;
    private boolean isPaused;

    private float absPrevTime;
    private float secAccumTime;
}