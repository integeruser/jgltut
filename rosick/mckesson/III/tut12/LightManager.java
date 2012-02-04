package rosick.mckesson.III.tut12;

import static rosick.glm.Vec.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rosick.PortingUtils.Bufferable;
import rosick.framework.Interpolators.LightInterpolatorVec3;
import rosick.framework.Interpolators.TimedLinearInterpolatorFloat;
import rosick.framework.Interpolators.TimedLinearInterpolatorVec4;
import rosick.framework.Timer;
import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Vec3;
import rosick.glm.Vec4;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class LightManager {
	
	class PerLight {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;
	}
	
	
	class LightBlock implements Bufferable<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float padding[] = new float[3];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = (4 + 1 + 3 + (8 * 4)) * (Float.SIZE / 8);


		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			float data[] = new float[40];
			System.arraycopy(ambientIntensity.get(), 0, data, 0, 4);
			data[4] = lightAttenuation;
			System.arraycopy(padding, 0, data, 5, padding.length);

			for (int i = 0; i < lights.length; i++) {
				float light[] = new float[8];
				System.arraycopy(lights[i].cameraSpaceLightPos.get(), 0, light, 0, 4);
				System.arraycopy(lights[i].lightIntensity.get(), 0, light, 4, 4);
				
				System.arraycopy(light, 0, data, 8 + i * 8, 8);
			}
			
			buffer.clear();
			buffer.put(data);
			buffer.flip();
			
			return buffer;
		}
	}

	class LightBlockHDR implements Bufferable<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float maxIntensity;
		float padding[] = new float[2];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			float data[] = new float[40];
			System.arraycopy(ambientIntensity.get(), 0, data, 0, 4);
			data[4] = lightAttenuation;
			data[5] = maxIntensity;
			System.arraycopy(padding, 0, data, 6, padding.length);

			for (int i = 0; i < lights.length; i++) {
				float temp[] = new float[8];
				System.arraycopy(lights[i].cameraSpaceLightPos.get(), 0, temp, 0, 4);
				System.arraycopy(lights[i].lightIntensity.get(), 0, temp, 4, 4);
				
				System.arraycopy(temp, 0, data, 8 + i * 8, 8);
			}
			
			buffer.put(data);
			buffer.flip();
			
			return buffer;
		}
	}
		
	class LightBlockGamma implements Bufferable<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float maxIntensity;
		float gamma;
		float padding;
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];
		
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			float data[] = new float[40];
			System.arraycopy(ambientIntensity.get(), 0, data, 0, 4);
			data[4] = lightAttenuation;
			data[5] = maxIntensity;
			data[6] = gamma;
			data[7] = padding;

			for (int i = 0; i < lights.length; i++) {
				float temp[] = new float[8];
				System.arraycopy(lights[i].cameraSpaceLightPos.get(), 0, temp, 0, 4);
				System.arraycopy(lights[i].lightIntensity.get(), 0, temp, 4, 4);
				
				System.arraycopy(temp, 0, data, 8 + i * 8, 8);
			}
			
			buffer.put(data);
			buffer.flip();
			
			return buffer;
		}
	};
	
	
	static class SunlightValue {
		float normTime;
		Vec4 ambient;
		Vec4 sunlightIntensity;
		Vec4 backgroundColor;
		
		SunlightValue(float normTime, Vec4 ambient, Vec4 sunlightIntensity, Vec4 backgroundColor) {
			this.normTime = normTime;
			this.ambient = ambient;
			this.sunlightIntensity = sunlightIntensity;
			this.backgroundColor = backgroundColor;
		}
	};

	static class SunlightValueHDR {
		float normTime;
		Vec4 ambient;
		Vec4 sunlightIntensity;
		Vec4 backgroundColor;
		float maxIntensity;
	
		SunlightValueHDR(float normTime, Vec4 ambient, Vec4 sunlightIntensity,
				Vec4 backgroundColor, float maxIntensity) {
			this.normTime = normTime;
			this.ambient = ambient;
			this.sunlightIntensity = sunlightIntensity;
			this.backgroundColor = backgroundColor;
			this.maxIntensity = maxIntensity;
		}
	};
	
	
	enum TimerTypes {
		TIMER_SUN,
		TIMER_LIGHTS,
		TIMER_ALL,
		
		NUM_TIMER_TYPES,
	};
	
		
	private final int NUMBER_OF_LIGHTS = 4;
	private final int NUMBER_OF_POINT_LIGHTS = NUMBER_OF_LIGHTS - 1;
	
	private final float g_fHalfLightDistance = 70.0f;
	private final float g_fLightAttenuation = 1.0f / (g_fHalfLightDistance * g_fHalfLightDistance);

	private Timer m_sunTimer;
	
	private TimedLinearInterpolatorVec4 m_ambientInterpolator;
	private TimedLinearInterpolatorVec4 m_backgroundInterpolator;
	private TimedLinearInterpolatorVec4 m_sunlightInterpolator;
	private TimedLinearInterpolatorFloat m_maxIntensityInterpolator;
	
	private ArrayList<LightInterpolatorVec3> m_lightPos;
	private ArrayList<Vec4> m_lightIntensity;
	private ArrayList<Timer> m_lightTimers;
	
	private Map<String, Timer> m_extraTimers;
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	LightManager() {
		m_sunTimer = new Timer(Timer.Type.TT_LOOP, 30.0f);
		
		m_ambientInterpolator = new TimedLinearInterpolatorVec4();
		m_backgroundInterpolator = new TimedLinearInterpolatorVec4();
		m_sunlightInterpolator = new TimedLinearInterpolatorVec4();
		m_maxIntensityInterpolator = new TimedLinearInterpolatorFloat();
		
		m_lightPos = new ArrayList<>();
		m_lightIntensity = new ArrayList<>();
		m_lightTimers = new ArrayList<>();
		
		m_extraTimers = new HashMap<>();
				 
		m_lightPos.add(new LightInterpolatorVec3());
		m_lightPos.add(new LightInterpolatorVec3());
		m_lightPos.add(new LightInterpolatorVec3());

		for (int i = 0; i < NUMBER_OF_POINT_LIGHTS; i++) {
			m_lightIntensity.add(new Vec4(0.2f, 0.2f, 0.2f, 1.0f));
		}	
		
		ArrayList<Vec3> posValues = new ArrayList<>();
	
		posValues.add(new Vec3(-50.0f, 30.0f, 70.0f));
		posValues.add(new Vec3(-70.0f, 30.0f, 50.0f));
		posValues.add(new Vec3(-70.0f, 30.0f, -50.0f));
		posValues.add(new Vec3(-50.0f, 30.0f, -70.0f));
		posValues.add(new Vec3(50.0f, 30.0f, -70.0f));
		posValues.add(new Vec3(70.0f, 30.0f, -50.0f));
		posValues.add(new Vec3(70.0f, 30.0f, 50.0f));
		posValues.add(new Vec3(50.0f, 30.0f, 70.0f));
		m_lightPos.get(0).setValues(posValues);
		m_lightTimers.add(new Timer(Timer.Type.TT_LOOP, 15.0f));
		
		// Right-side light.
		posValues = new ArrayList<>();
		posValues.add(new Vec3(100.0f, 6.0f, 75.0f));
		posValues.add(new Vec3(90.0f, 8.0f, 90.0f));
		posValues.add(new Vec3(75.0f, 10.0f, 100.0f));
		posValues.add(new Vec3(60.0f, 12.0f, 90.0f));
		posValues.add(new Vec3(50.0f, 14.0f, 75.0f));
		posValues.add(new Vec3(60.0f, 16.0f, 60.0f));
		posValues.add(new Vec3(75.0f, 18.0f, 50.0f));
		posValues.add(new Vec3(90.0f, 20.0f, 60.0f));
		posValues.add(new Vec3(100.0f, 22.0f, 75.0f));
		posValues.add(new Vec3(90.0f, 24.0f, 90.0f));
		posValues.add(new Vec3(75.0f, 26.0f, 100.0f));
		posValues.add(new Vec3(60.0f, 28.0f, 90.0f));
		posValues.add(new Vec3(50.0f, 30.0f, 75.0f));

		posValues.add(new Vec3(105.0f, 9.0f, -70.0f));
		posValues.add(new Vec3(105.0f, 10.0f, -90.0f));
		posValues.add(new Vec3(72.0f, 20.0f, -90.0f));
		posValues.add(new Vec3(72.0f, 22.0f, -70.0f));
		posValues.add(new Vec3(105.0f, 32.0f, -70.0f));
		posValues.add(new Vec3(105.0f, 34.0f, -90.0f));
		posValues.add(new Vec3(72.0f, 44.0f, -90.0f));

		m_lightPos.get(1).setValues(posValues);
		m_lightTimers.add(new Timer(Timer.Type.TT_LOOP, 25.0f));

		// Left-side light.
		posValues = new ArrayList<>();
		posValues.add(new Vec3(-7.0f, 35.0f, 1.0f));
		posValues.add(new Vec3(8.0f, 40.0f, -14.0f));
		posValues.add(new Vec3(-7.0f, 45.0f, -29.0f));
		posValues.add(new Vec3(-22.0f, 50.0f, -14.0f));
		posValues.add(new Vec3(-7.0f, 55.0f, 1.0f));
		posValues.add(new Vec3(8.0f, 60.0f, -14.0f));
		posValues.add(new Vec3(-7.0f, 65.0f, -29.0f));

		posValues.add(new Vec3(-83.0f, 30.0f, -92.0f));
		posValues.add(new Vec3(-98.0f, 27.0f, -77.0f));
		posValues.add(new Vec3(-83.0f, 24.0f, -62.0f));
		posValues.add(new Vec3(-68.0f, 21.0f, -77.0f));
		posValues.add(new Vec3(-83.0f, 18.0f, -92.0f));
		posValues.add(new Vec3(-98.0f, 15.0f, -77.0f));

		posValues.add(new Vec3(-50.0f, 8.0f, 25.0f));
		posValues.add(new Vec3(-59.5f, 4.0f, 65.0f));
		posValues.add(new Vec3(-59.5f, 4.0f, 78.0f));
		posValues.add(new Vec3(-45.0f, 4.0f, 82.0f));
		posValues.add(new Vec3(-40.0f, 4.0f, 50.0f));
		posValues.add(new Vec3(-70.0f, 20.0f, 40.0f));
		posValues.add(new Vec3(-60.0f, 20.0f, 90.0f));
		posValues.add(new Vec3(-40.0f, 25.0f, 90.0f));

		m_lightPos.get(2).setValues(posValues);
		m_lightTimers.add(new Timer(Timer.Type.TT_LOOP, 15.0f));
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	class Pair<K, V> {
		K first;
		V second;
	}
	
	
	public class MaxIntensityData extends Pair<Float, Float> {
		public MaxIntensityData(Float first, Float second) {
			this.first = first;
			this.second = second;
		}
	}
	
	public class LightVectorData extends Pair<Vec4, Float> {
		public LightVectorData(Vec4 first, Float second) {
			this.first = first;
			this.second = second;
		}
	}
	
	
	public class MaxIntensityVector extends ArrayList<MaxIntensityData> {
		private static final long serialVersionUID = -3083128917290208757L;			// Autogenerated by Eclipse Ide
	}

	public class LightVector extends ArrayList<LightVectorData> {
		private static final long serialVersionUID = -7238863853470466667L;			// Autogenerated by Eclipse Ide
	}

	
	public static Vec4 getValue(LightVectorData data) {
		return data.first;
	}
	
	public static float getTime(LightVectorData data) {
		return data.second;
	}
	
	
	public static float getValue(MaxIntensityData data) {
		return data.first;
	}

	public static float getTime(MaxIntensityData data) {
		return data.second;
	}
	
	
	void setSunlightValues(SunlightValue pValues[], int iSize) {
		LightVector ambient = new LightVector();
		LightVector light = new LightVector();
		LightVector background = new LightVector();

		for (int valIx = 0; valIx < iSize; valIx++) {
			ambient.add		(new LightVectorData(new Vec4(pValues[valIx].ambient), 				pValues[valIx].normTime));
			light.add		(new LightVectorData(new Vec4(pValues[valIx].sunlightIntensity), 	pValues[valIx].normTime));
			background.add	(new LightVectorData(new Vec4(pValues[valIx].backgroundColor), 		pValues[valIx].normTime));
		}

		m_ambientInterpolator.setValues(ambient);
		m_sunlightInterpolator.setValues(light);
		m_backgroundInterpolator.setValues(background);

		MaxIntensityVector maxIntensity = new MaxIntensityVector();
		maxIntensity.add(new MaxIntensityData(1.0f, 0.0f));
		m_maxIntensityInterpolator.setValues(maxIntensity, false);
	}
	
	void setSunlightValues(SunlightValueHDR pValues[], int iSize) {
		LightVector ambient = new LightVector();
		LightVector light = new LightVector();
		LightVector background = new LightVector();
		MaxIntensityVector maxIntensity = new MaxIntensityVector();

		for (int valIx = 0; valIx < iSize; valIx++) {
			ambient.add			(new LightVectorData(new Vec4(pValues[valIx].ambient), 				pValues[valIx].normTime));
			light.add			(new LightVectorData(new Vec4(pValues[valIx].sunlightIntensity), 	pValues[valIx].normTime));
			background.add		(new LightVectorData(new Vec4(pValues[valIx].backgroundColor), 		pValues[valIx].normTime));
			maxIntensity.add	(new MaxIntensityData(pValues[valIx].maxIntensity, 					pValues[valIx].normTime));
		}

		m_ambientInterpolator.setValues(ambient);
		m_sunlightInterpolator.setValues(light);
		m_backgroundInterpolator.setValues(background);
		m_maxIntensityInterpolator.setValues(maxIntensity);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	void updateTime(double fElapsedTime) {
		m_sunTimer.update(fElapsedTime);
		
		for (Timer timer : m_lightTimers) {
			timer.update(fElapsedTime);
		}
		
		for (Timer timer : m_extraTimers.values()) {
			timer.update(fElapsedTime);
		}
	}

	
	void setPause(TimerTypes eTimer, boolean pause) {
		if (eTimer == TimerTypes.TIMER_ALL || eTimer == TimerTypes.TIMER_LIGHTS) {
			for (Timer timer : m_lightTimers) {
				timer.setPause(pause);
			}
			for (Timer timer : m_extraTimers.values()) {
				timer.setPause(pause);
			}
		}

		if (eTimer == TimerTypes.TIMER_ALL || eTimer == TimerTypes.TIMER_SUN) {
			m_sunTimer.togglePause();
		}
	}

	void togglePause(TimerTypes eTimer) {
		setPause(eTimer, !isPaused(eTimer));
	}

	boolean isPaused(TimerTypes eTimer) {
		if (eTimer == TimerTypes.TIMER_ALL || eTimer == TimerTypes.TIMER_SUN) {
			return m_sunTimer.isPaused();
		}

		return m_lightTimers.get(0).isPaused();
	}

	
	void rewindTime(TimerTypes eTimer, float secRewind) {
		if (eTimer == TimerTypes.TIMER_ALL || eTimer == TimerTypes.TIMER_SUN) {
			m_sunTimer.rewind(secRewind);
		}

		if (eTimer == TimerTypes.TIMER_ALL || eTimer == TimerTypes.TIMER_LIGHTS) {
			for (Timer timer : m_lightTimers) {
				timer.rewind(secRewind);
			}
			for (Timer timer : m_extraTimers.values()) {
				timer.rewind(secRewind);
			}
		}
	}

	
	void fastForwardTime(TimerTypes eTimer, float secFF) {
		if (eTimer == TimerTypes.TIMER_ALL || eTimer == TimerTypes.TIMER_SUN) {
			m_sunTimer.fastForward(secFF);
		}

		if (eTimer == TimerTypes.TIMER_ALL || eTimer == TimerTypes.TIMER_LIGHTS) {
			for (Timer timer : m_lightTimers) {
				timer.fastForward(secFF);
			}
			for (Timer timer : m_extraTimers.values()) {
				timer.fastForward(secFF);
			}
		}
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	LightBlock getLightInformation(Mat4 worldToCameraMat) {
		LightBlock lightData = new LightBlock();

		lightData.ambientIntensity = m_ambientInterpolator.interpolate(m_sunTimer.getAlpha());
		lightData.lightAttenuation = g_fLightAttenuation;

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCameraMat, getSunlightDirection());
		lightData.lights[0].lightIntensity = m_sunlightInterpolator.interpolate(m_sunTimer.getAlpha());

		for (int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
			Vec4 worldLightPos = new Vec4(m_lightPos.get(light).interpolate(m_lightTimers.get(light).getAlpha()), 1.0f);
			Vec4 lightPosCameraSpace = Mat4.mul(worldToCameraMat, worldLightPos);

			lightData.lights[light + 1] = new PerLight();
			lightData.lights[light + 1].cameraSpaceLightPos = lightPosCameraSpace;
			lightData.lights[light + 1].lightIntensity = new Vec4(m_lightIntensity.get(light));
		}

		return lightData;
	}

	LightBlockHDR getLightInformationHDR(Mat4 worldToCameraMat) {
		LightBlockHDR lightData = new LightBlockHDR();

		lightData.ambientIntensity = m_ambientInterpolator.interpolate(m_sunTimer.getAlpha());
		lightData.lightAttenuation = g_fLightAttenuation;
		lightData.maxIntensity = m_maxIntensityInterpolator.interpolate(m_sunTimer.getAlpha());

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCameraMat, getSunlightDirection());
		lightData.lights[0].lightIntensity = m_sunlightInterpolator.interpolate(m_sunTimer.getAlpha());

		for (int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
			Vec4 worldLightPos = new Vec4(m_lightPos.get(light).interpolate(m_lightTimers.get(light).getAlpha()), 1.0f);
			Vec4 lightPosCameraSpace = Mat4.mul(worldToCameraMat, worldLightPos);

			lightData.lights[light + 1] = new PerLight();
			lightData.lights[light + 1].cameraSpaceLightPos = lightPosCameraSpace;
			lightData.lights[light + 1].lightIntensity = new Vec4(m_lightIntensity.get(light));
		}

		return lightData;
	}

	LightBlockGamma getLightInformationGamma(Mat4 worldToCameraMat) {
		LightBlockHDR lightDataHdr = getLightInformationHDR(worldToCameraMat);
		LightBlockGamma lightData = new LightBlockGamma();

		lightData.ambientIntensity = lightDataHdr.ambientIntensity;
		lightData.lightAttenuation = lightDataHdr.lightAttenuation;
		lightData.maxIntensity = lightDataHdr.maxIntensity;
		lightData.lights = lightDataHdr.lights;

		return lightData;
	}

	
	Vec4 getSunlightDirection() {
		float angle = 2.0f * 3.14159f * m_sunTimer.getAlpha();
		Vec4 sunDirection = new Vec4(0.0f);
		sunDirection.set(X, (float) Math.sin(angle));
		sunDirection.set(Y, (float) Math.cos(angle));

		// Keep the sun from being perfectly centered overhead.
		sunDirection = Mat4.mul(Glm.rotate(new Mat4(1.0f), 5.0f, new Vec3(0.0f, 1.0f, 0.0f)), sunDirection);

		return sunDirection;
	}
		
	Vec4 getSunlightIntensity() {
		return m_sunlightInterpolator.interpolate(m_sunTimer.getAlpha());
	}

	
	int getNumberOfPointLights() {
		return m_lightPos.size();
	}
	

	Vec3 getWorldLightPosition(int lightIx) {
		return m_lightPos.get(lightIx).interpolate(m_lightTimers.get(lightIx).getAlpha());
	}

	
	void setPointLightIntensity(int iLightIx, Vec4 intensity) {
		m_lightIntensity.set(iLightIx, intensity);
	}
	
	Vec4 getPointLightIntensity(int iLightIx) {
		return m_lightIntensity.get(iLightIx);
	}
	

	void createTimer(String timerName, Timer.Type eType, float fDuration) {
		m_extraTimers.put(timerName, new Timer(eType, fDuration));
	}
	
	float getTimerValue(String timerName) {
		if (!m_extraTimers.containsKey(timerName)) {
			return -1.0f;
		}

		return m_extraTimers.get(timerName).getAlpha();
	}
	
	
	Vec4 getBackgroundColor() {
		return m_backgroundInterpolator.interpolate(m_sunTimer.getAlpha());
	}

	float getSunTime() {
		return m_sunTimer.getAlpha();
	}
}