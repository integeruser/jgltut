package rosick.mckesson.framework;

import rosick.jglsdk.glm.Glm;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Timer {
	
	public enum Type {
		TT_LOOP,
		TT_SINGLE,
		TT_INFINITE
	};
	
	
	private Type m_eType;
	private float m_secDuration;

	private boolean m_hasUpdated;
	private boolean m_isPaused;

	private float m_absPrevTime;
	private float m_secAccumTime;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * 	Creates a timer with the given type.

		LOOP and SINGLE timers need an explicit duration. This represents the time in seconds
		through a loop, or the time in seconds until the timer expires.

		INFINITE timers ignore the duration.

		It is legal to create these statically.
	 */	
	public Timer(Type eType, float fDuration) {
		m_eType = eType;
		m_secDuration = fDuration;
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
		
		if (!m_hasUpdated) {
			m_absPrevTime = absCurrTime;
			m_hasUpdated = true;
		}

		if (m_isPaused) {
			m_absPrevTime = absCurrTime;
			return false;
		}

		float fDeltaTime = absCurrTime - m_absPrevTime;
		m_secAccumTime += fDeltaTime;

		m_absPrevTime = absCurrTime;
		if (m_eType == Type.TT_SINGLE) {
			return m_secAccumTime > m_secDuration;
		}
		
		return false;
	}
	
	
	/**
	 * Resets the timer, as though the user just created the object with the original parameters.
	 */
	public void reset() {
		m_hasUpdated = false;
		m_secAccumTime = 0.0f;
	}

	/**
	 * Subtracts secRewind from the current time and continues from there.
	 */
	public void rewind(float secRewind) {
		m_secAccumTime -= secRewind;
		
		if (m_secAccumTime < 0.0f) {
			m_secAccumTime = 0.0f;
		}
	}
	
	/**
	 * Adds secRewind to the current time and continues from there.
	 */
	public void fastForward(float secFF) {
		m_secAccumTime += secFF;
	}
	
	
	/**
	 * Returns true if the timer is paused.
	 */
	public boolean isPaused() {
		return m_isPaused;
	}
	
	/**
	 * Pauses/unpauses. Returns true if the timer is paused after the toggling.
	 */
	public boolean togglePause() {
		m_isPaused = !m_isPaused;
		
		return m_isPaused;
	}
		
	/**
	 * Sets the pause state to the given value.
	 */
	public void setPause(boolean pause) {
		m_isPaused = pause;
	}
	
	
	/**	
	 * Returns a number [0, 1], representing progress through the duration. 
	 * Only used for SINGLE and LOOP timers.
	*/
	public float getAlpha() {
		switch (m_eType) {
			case TT_LOOP:
				return (m_secAccumTime % m_secDuration) / m_secDuration;
			case TT_SINGLE:
				return Glm.clamp(m_secAccumTime / m_secDuration, 0.0f, 1.0f);
		}

		return -1.0f;																// Garbage.
	}

	/**
	 * Returns a number [0, duration], representing the progress through the timer in 
	 * seconds. Only for SINGLE and LOOP timers.
	 */
	public float getProgression() {
		switch (m_eType) {
			case TT_LOOP:
				return m_secAccumTime % m_secDuration;
			case TT_SINGLE:
				return Glm.clamp(m_secAccumTime, 0.0f, m_secDuration);
		}

		return -1.0f;																// Garbage.
	}

	/**
	 * Returns the time in seconds since the timer was started, excluding
	 * time for pausing.
	 */
	public float getTimeSinceStart() {
		return m_secAccumTime;
	}
	
	/**
	 * Returns the timer's duration that was passed in.
	 */
	public float getDuration() {
		return m_secDuration;
	}
}