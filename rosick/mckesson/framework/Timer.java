package rosick.mckesson.framework;

import rosick.jglsdk.glm.Glm;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Timer {
	
	public enum Type {
		LOOP,
		SINGLE,
		INFINITE
	};
	
		
	public Timer(Type type, float duration) {
		this.type = type;
		secDuration = duration;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Updates the time for the timer. Returns true if the timer has reached the end.
	 * Will only return true for SINGLE timers that have reached their duration.
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
	 * Resets the timer, as though the user just created the object with the original parameters.
	 */
	public void reset() {
		hasUpdated = false;
		secAccumTime = 0.0f;
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
		}

		return -1.0f;						// Garbage.
	}

	/**
	 * Returns a number [0, duration], representing the progress through the timer in 
	 * seconds. Only for SINGLE and LOOP timers.
	 */
	public float getProgression() {
		switch (type) {
			case LOOP:
				return secAccumTime % secDuration;
				
			case SINGLE:
				return Glm.clamp(secAccumTime, 0.0f, secDuration);
		}

		return -1.0f;						// Garbage.
	}

	/**
	 * Returns the time in seconds since the timer was started, excluding
	 * time for pausing.
	 */
	public float getTimeSinceStart() {
		return secAccumTime;
	}
	
	/**
	 * Returns the timer's duration that was passed in.
	 */
	public float getDuration() {
		return secDuration;
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Type type;
	private float secDuration;

	private boolean hasUpdated;
	private boolean isPaused;

	private float absPrevTime;
	private float secAccumTime;
}