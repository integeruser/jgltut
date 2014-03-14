package fcagnin.gltut.tut12;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.gltut.framework.Timer;
import fcagnin.gltut.framework.Interpolators.ConstVelLinearInterpolatorVec3;
import fcagnin.gltut.framework.Interpolators.WeightedLinearInterpolatorFloat;
import fcagnin.gltut.framework.Interpolators.WeightedLinearInterpolatorVec4;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 *
 * @author integeruser
 */
class LightManager {

	LightManager() {
		sunTimer = new Timer(Timer.Type.LOOP, 30.0f);

		ambientInterpolator = new TimedLinearInterpolatorVec4();
		backgroundInterpolator = new TimedLinearInterpolatorVec4();
		sunlightInterpolator = new TimedLinearInterpolatorVec4();
		maxIntensityInterpolator = new TimedLinearInterpolatorFloat();

		lightPos = new ArrayList<>();
		lightIntensity = new ArrayList<>();
		lightTimers = new ArrayList<>();

		extraTimers = new HashMap<String, Timer>();

		lightPos.add(new ConstVelLinearInterpolatorVec3());
		lightPos.add(new ConstVelLinearInterpolatorVec3());
		lightPos.add(new ConstVelLinearInterpolatorVec3());

		for (int i = 0; i < NUMBER_OF_POINT_LIGHTS; i++) {
			lightIntensity.add(new Vec4(0.2f, 0.2f, 0.2f, 1.0f));
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
		lightPos.get(0).setValues(posValues);
		lightTimers.add(new Timer(Timer.Type.LOOP, 15.0f));

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

		lightPos.get(1).setValues(posValues);
		lightTimers.add(new Timer(Timer.Type.LOOP, 25.0f));

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

		lightPos.get(2).setValues(posValues);
		lightTimers.add(new Timer(Timer.Type.LOOP, 15.0f));
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	class PerLight extends BufferableData<FloatBuffer> {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;

		static final int SIZE = Vec4.SIZE + Vec4.SIZE;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			cameraSpaceLightPos.fillBuffer(buffer);
			lightIntensity.fillBuffer(buffer);

			return buffer;
		}
	}


	class LightBlock extends BufferableData<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float padding[] = new float[3];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = Vec4.SIZE + ((1 + 3) * (Float.SIZE / Byte.SIZE)) + PerLight.SIZE * NUMBER_OF_LIGHTS;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			ambientIntensity.fillBuffer(buffer);
			buffer.put(lightAttenuation);
			buffer.put(padding);

			for (PerLight light : lights) {
				light.fillBuffer(buffer);
			}

			return buffer;
		}
	}

	class LightBlockHDR extends BufferableData<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float maxIntensity;
		float padding[] = new float[2];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = Vec4.SIZE + ((1 + 1 + 2) * (Float.SIZE / Byte.SIZE)) + PerLight.SIZE * NUMBER_OF_LIGHTS;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			ambientIntensity.fillBuffer(buffer);
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(padding);

			for (PerLight light : lights) {
				light.fillBuffer(buffer);
			}

			return buffer;
		}
	}

	class LightBlockGamma extends BufferableData<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float maxIntensity;
		float gamma;
		float padding;
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = Vec4.SIZE + ((1 + 1 + 1 + 1) * (Float.SIZE / Byte.SIZE)) + PerLight.SIZE * NUMBER_OF_LIGHTS;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			ambientIntensity.fillBuffer(buffer);
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(gamma);
			buffer.put(padding);

			for (PerLight light : lights) {
				light.fillBuffer(buffer);
			}

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
		SUN,
		LIGHTS,
		ALL,

		NUM_TIMER_TYPES
	};



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	class TimedLinearInterpolatorFloat extends WeightedLinearInterpolatorFloat {

		void setValues(ArrayList<MaxIntensityData> data) {
			setValues(data, true);
		}

		void setValues(ArrayList<MaxIntensityData> data, boolean isLooping) {
			values.clear();

			for (MaxIntensityData curr : data) {
				Data temp = new Data();
				temp.data = getValue(curr);
				temp.weight = LightManager.getTime(curr);

				values.add(temp);
			}

			if (isLooping && !values.isEmpty()) {
				Data temp = new Data();
				temp.data = values.get(0).data;
				temp.weight = values.get(0).weight;

				values.add(temp);
			}

			// Ensure first is weight 0, and last is weight 1.
			if (!values.isEmpty()) {
				values.get(0).weight = 0.0f;
				values.get(values.size() - 1).weight = 1.0f;
			}
		}
	}

	class TimedLinearInterpolatorVec4 extends WeightedLinearInterpolatorVec4 {

		void setValues(ArrayList<LightData> data) {
			setValues(data, true);
		}

		void setValues(ArrayList<LightData> data, boolean isLooping) {
			values.clear();

			for (LightData curr : data) {
				Data temp = new Data();
				temp.data = new Vec4(LightManager.getValue(curr));
				temp.weight = LightManager.getTime(curr);

				values.add(temp);
			}

			if (isLooping && !values.isEmpty()) {
				Data temp = new Data();
				temp.data = new Vec4(values.get(0).data);
				temp.weight = values.get(0).weight;

				values.add(temp);
			}

			// Ensure first is weight 0, and last is weight 1.
			if (!values.isEmpty()) {
				values.get(0).weight = 0.0f;
				values.get(values.size() - 1).weight = 1.0f;
			}
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	class Pair<K, V> {
		K first;
		V second;
	}


	class MaxIntensityData extends Pair<Float, Float> {
		MaxIntensityData(Float first, Float second) {
			this.first = first;
			this.second = second;
		}
	}

	class LightData extends Pair<Vec4, Float> {
		LightData(Vec4 first, Float second) {
			this.first = first;
			this.second = second;
		}
	}


	static Vec4 getValue(LightData data) {
		return data.first;
	}

	static float getTime(LightData data) {
		return data.second;
	}


	float getValue(MaxIntensityData data) {
		return data.first;
	}

	static float getTime(MaxIntensityData data) {
		return data.second;
	}


	void setSunlightValues(SunlightValue values[], int size) {
		ArrayList<LightData> ambient = new ArrayList<>();
		ArrayList<LightData> light = new ArrayList<>();
		ArrayList<LightData> background = new ArrayList<>();

		for (int valIndex = 0; valIndex < size; valIndex++) {
			ambient.add		(new LightData(new Vec4(values[valIndex].ambient), 			values[valIndex].normTime));
			light.add		(new LightData(new Vec4(values[valIndex].sunlightIntensity), 	values[valIndex].normTime));
			background.add	(new LightData(new Vec4(values[valIndex].backgroundColor), 	values[valIndex].normTime));
		}

		ambientInterpolator.setValues(ambient);
		sunlightInterpolator.setValues(light);
		backgroundInterpolator.setValues(background);

		ArrayList<MaxIntensityData> maxIntensity = new ArrayList<>();
		maxIntensity.add(new MaxIntensityData(1.0f, 0.0f));
		maxIntensityInterpolator.setValues(maxIntensity, false);
	}

	void setSunlightValues(SunlightValueHDR values[], int size) {
		ArrayList<LightData> ambient = new ArrayList<>();
		ArrayList<LightData> light = new ArrayList<>();
		ArrayList<LightData> background = new ArrayList<>();
		ArrayList<MaxIntensityData> maxIntensity = new ArrayList<>();

		for (int valIndex = 0; valIndex < size; valIndex++) {
			ambient.add			(new LightData(new Vec4(values[valIndex].ambient), 			values[valIndex].normTime));
			light.add			(new LightData(new Vec4(values[valIndex].sunlightIntensity),	values[valIndex].normTime));
			background.add		(new LightData(new Vec4(values[valIndex].backgroundColor), 	values[valIndex].normTime));
			maxIntensity.add	(new MaxIntensityData(values[valIndex].maxIntensity, 				values[valIndex].normTime));
		}

		ambientInterpolator.setValues(ambient);
		sunlightInterpolator.setValues(light);
		backgroundInterpolator.setValues(background);
		maxIntensityInterpolator.setValues(maxIntensity);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	void createTimer(String timerName, Timer.Type timerType, float duration) {
		extraTimers.put(timerName, new Timer(timerType, duration));
	}

	float getTimerValue(String timerName) {
		if (!extraTimers.containsKey(timerName)) {
			return -1.0f;
		}

		return extraTimers.get(timerName).getAlpha();
	}


	void updateTime(float elapsedTime) {
		sunTimer.update(elapsedTime);

		for (Timer timer : lightTimers) {
			timer.update(elapsedTime);
		}

		for (Timer timer : extraTimers.values()) {
			timer.update(elapsedTime);
		}
	}


	void setPause(TimerTypes timerType, boolean pause) {
		if (timerType == TimerTypes.ALL || timerType == TimerTypes.LIGHTS) {
			for (Timer timer : lightTimers) {
				timer.setPause(pause);
			}
			for (Timer timer : extraTimers.values()) {
				timer.setPause(pause);
			}
		}

		if (timerType == TimerTypes.ALL || timerType == TimerTypes.SUN) {
			sunTimer.togglePause();
		}
	}

	void togglePause(TimerTypes timerType) {
		setPause(timerType, !isPaused(timerType));
	}

	boolean isPaused(TimerTypes timerType) {
		if (timerType == TimerTypes.ALL || timerType == TimerTypes.SUN) {
			return sunTimer.isPaused();
		}

		return lightTimers.get(0).isPaused();
	}


	void rewindTime(TimerTypes timerType, float secRewind) {
		if (timerType == TimerTypes.ALL || timerType == TimerTypes.SUN) {
			sunTimer.rewind(secRewind);
		}

		if (timerType == TimerTypes.ALL || timerType == TimerTypes.LIGHTS) {
			for (Timer timer : lightTimers) {
				timer.rewind(secRewind);
			}
			for (Timer timer : extraTimers.values()) {
				timer.rewind(secRewind);
			}
		}
	}


	void fastForwardTime(TimerTypes timerType, float secFF) {
		if (timerType == TimerTypes.ALL || timerType == TimerTypes.SUN) {
			sunTimer.fastForward(secFF);
		}

		if (timerType == TimerTypes.ALL || timerType == TimerTypes.LIGHTS) {
			for (Timer timer : lightTimers) {
				timer.fastForward(secFF);
			}
			for (Timer timer : extraTimers.values()) {
				timer.fastForward(secFF);
			}
		}
	}


	float getSunTime() {
		return sunTimer.getAlpha();
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	LightBlock getLightInformation(Mat4 worldToCameraMat) {
		LightBlock lightData = new LightBlock();

		lightData.ambientIntensity = ambientInterpolator.interpolate(sunTimer.getAlpha());
		lightData.lightAttenuation = lightAttenuation;

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCameraMat, getSunlightDirection());
		lightData.lights[0].lightIntensity = sunlightInterpolator.interpolate(sunTimer.getAlpha());

		for (int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
			Vec4 worldLightPos = new Vec4(lightPos.get(light).interpolate(lightTimers.get(light).getAlpha()), 1.0f);
			Vec4 lightPosCameraSpace = Mat4.mul(worldToCameraMat, worldLightPos);

			lightData.lights[light + 1] = new PerLight();
			lightData.lights[light + 1].cameraSpaceLightPos = lightPosCameraSpace;
			lightData.lights[light + 1].lightIntensity = new Vec4(lightIntensity.get(light));
		}

		return lightData;
	}

	LightBlockHDR getLightInformationHDR(Mat4 worldToCameraMat) {
		LightBlockHDR lightData = new LightBlockHDR();

		lightData.ambientIntensity = ambientInterpolator.interpolate(sunTimer.getAlpha());
		lightData.lightAttenuation = lightAttenuation;
		lightData.maxIntensity = maxIntensityInterpolator.interpolate(sunTimer.getAlpha());

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCameraMat, getSunlightDirection());
		lightData.lights[0].lightIntensity = sunlightInterpolator.interpolate(sunTimer.getAlpha());

		for (int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
			Vec4 worldLightPos = new Vec4(lightPos.get(light).interpolate(lightTimers.get(light).getAlpha()), 1.0f);
			Vec4 lightPosCameraSpace = Mat4.mul(worldToCameraMat, worldLightPos);

			lightData.lights[light + 1] = new PerLight();
			lightData.lights[light + 1].cameraSpaceLightPos = lightPosCameraSpace;
			lightData.lights[light + 1].lightIntensity = new Vec4(lightIntensity.get(light));
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
		float angle = 2.0f * 3.14159f * sunTimer.getAlpha();
		Vec4 sunDirection = new Vec4(0.0f);
		sunDirection.x = (float) Math.sin(angle);
		sunDirection.y = (float) Math.cos(angle);

		// Keep the sun from being perfectly centered overhead.
		sunDirection = Mat4.mul(Glm.rotate(new Mat4(1.0f), 5.0f, new Vec3(0.0f, 1.0f, 0.0f)), sunDirection);

		return sunDirection;
	}

	Vec4 getSunlightIntensity() {
		return sunlightInterpolator.interpolate(sunTimer.getAlpha());
	}


	int getNumberOfPointLights() {
		return lightPos.size();
	}


	Vec3 getWorldLightPosition(int lightIndex) {
		return lightPos.get(lightIndex).interpolate(lightTimers.get(lightIndex).getAlpha());
	}


	void setPointLightIntensity(int lightIndex, Vec4 intensity) {
		lightIntensity.set(lightIndex, intensity);
	}

	Vec4 getPointLightIntensity(int lightIndex) {
		return lightIntensity.get(lightIndex);
	}


	Vec4 getBackgroundColor() {
		return backgroundInterpolator.interpolate(sunTimer.getAlpha());
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private static final int NUMBER_OF_LIGHTS = 4;
	private static final int NUMBER_OF_POINT_LIGHTS = NUMBER_OF_LIGHTS - 1;

	private final float halfLightDistance = 70.0f;
	private final float lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

	private Timer sunTimer;

	private TimedLinearInterpolatorVec4 ambientInterpolator;
	private TimedLinearInterpolatorVec4 backgroundInterpolator;
	private TimedLinearInterpolatorVec4 sunlightInterpolator;
	private TimedLinearInterpolatorFloat maxIntensityInterpolator;

	private ArrayList<ConstVelLinearInterpolatorVec3> lightPos;
	private ArrayList<Vec4> lightIntensity;
	private ArrayList<Timer> lightTimers;

	private HashMap<String, Timer> extraTimers;
}