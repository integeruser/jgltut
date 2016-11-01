package integeruser.jgltut.tut12;

import integeruser.jgltut.commons.Bufferable;
import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.PerLight;
import integeruser.jgltut.framework.Interpolators.ConstVelLinearInterpolatorVec3;
import integeruser.jgltut.framework.Interpolators.WeightedLinearInterpolatorFloat;
import integeruser.jgltut.framework.Interpolators.WeightedLinearInterpolatorVec4;
import integeruser.jgltut.framework.Timer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
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

        extraTimers = new HashMap<>();

        lightPos.add(new ConstVelLinearInterpolatorVec3());
        lightPos.add(new ConstVelLinearInterpolatorVec3());
        lightPos.add(new ConstVelLinearInterpolatorVec3());

        for (int i = 0; i < NUMBER_OF_POINT_LIGHTS; i++) {
            lightIntensity.add(new Vector4f(0.2f, 0.2f, 0.2f, 1.0f));
        }

        ArrayList<Vector3f> posValues = new ArrayList<>();

        posValues.add(new Vector3f(-50.0f, 30.0f, 70.0f));
        posValues.add(new Vector3f(-70.0f, 30.0f, 50.0f));
        posValues.add(new Vector3f(-70.0f, 30.0f, -50.0f));
        posValues.add(new Vector3f(-50.0f, 30.0f, -70.0f));
        posValues.add(new Vector3f(50.0f, 30.0f, -70.0f));
        posValues.add(new Vector3f(70.0f, 30.0f, -50.0f));
        posValues.add(new Vector3f(70.0f, 30.0f, 50.0f));
        posValues.add(new Vector3f(50.0f, 30.0f, 70.0f));
        lightPos.get(0).setValues(posValues);
        lightTimers.add(new Timer(Timer.Type.LOOP, 15.0f));

        // Right-side light.
        posValues = new ArrayList<>();
        posValues.add(new Vector3f(100.0f, 6.0f, 75.0f));
        posValues.add(new Vector3f(90.0f, 8.0f, 90.0f));
        posValues.add(new Vector3f(75.0f, 10.0f, 100.0f));
        posValues.add(new Vector3f(60.0f, 12.0f, 90.0f));
        posValues.add(new Vector3f(50.0f, 14.0f, 75.0f));
        posValues.add(new Vector3f(60.0f, 16.0f, 60.0f));
        posValues.add(new Vector3f(75.0f, 18.0f, 50.0f));
        posValues.add(new Vector3f(90.0f, 20.0f, 60.0f));
        posValues.add(new Vector3f(100.0f, 22.0f, 75.0f));
        posValues.add(new Vector3f(90.0f, 24.0f, 90.0f));
        posValues.add(new Vector3f(75.0f, 26.0f, 100.0f));
        posValues.add(new Vector3f(60.0f, 28.0f, 90.0f));
        posValues.add(new Vector3f(50.0f, 30.0f, 75.0f));

        posValues.add(new Vector3f(105.0f, 9.0f, -70.0f));
        posValues.add(new Vector3f(105.0f, 10.0f, -90.0f));
        posValues.add(new Vector3f(72.0f, 20.0f, -90.0f));
        posValues.add(new Vector3f(72.0f, 22.0f, -70.0f));
        posValues.add(new Vector3f(105.0f, 32.0f, -70.0f));
        posValues.add(new Vector3f(105.0f, 34.0f, -90.0f));
        posValues.add(new Vector3f(72.0f, 44.0f, -90.0f));

        lightPos.get(1).setValues(posValues);
        lightTimers.add(new Timer(Timer.Type.LOOP, 25.0f));

        // Left-side light.
        posValues = new ArrayList<>();
        posValues.add(new Vector3f(-7.0f, 35.0f, 1.0f));
        posValues.add(new Vector3f(8.0f, 40.0f, -14.0f));
        posValues.add(new Vector3f(-7.0f, 45.0f, -29.0f));
        posValues.add(new Vector3f(-22.0f, 50.0f, -14.0f));
        posValues.add(new Vector3f(-7.0f, 55.0f, 1.0f));
        posValues.add(new Vector3f(8.0f, 60.0f, -14.0f));
        posValues.add(new Vector3f(-7.0f, 65.0f, -29.0f));

        posValues.add(new Vector3f(-83.0f, 30.0f, -92.0f));
        posValues.add(new Vector3f(-98.0f, 27.0f, -77.0f));
        posValues.add(new Vector3f(-83.0f, 24.0f, -62.0f));
        posValues.add(new Vector3f(-68.0f, 21.0f, -77.0f));
        posValues.add(new Vector3f(-83.0f, 18.0f, -92.0f));
        posValues.add(new Vector3f(-98.0f, 15.0f, -77.0f));

        posValues.add(new Vector3f(-50.0f, 8.0f, 25.0f));
        posValues.add(new Vector3f(-59.5f, 4.0f, 65.0f));
        posValues.add(new Vector3f(-59.5f, 4.0f, 78.0f));
        posValues.add(new Vector3f(-45.0f, 4.0f, 82.0f));
        posValues.add(new Vector3f(-40.0f, 4.0f, 50.0f));
        posValues.add(new Vector3f(-70.0f, 20.0f, 40.0f));
        posValues.add(new Vector3f(-60.0f, 20.0f, 90.0f));
        posValues.add(new Vector3f(-40.0f, 25.0f, 90.0f));

        lightPos.get(2).setValues(posValues);
        lightTimers.add(new Timer(Timer.Type.LOOP, 15.0f));
    }

    ////////////////////////////////
    private final float halfLightDistance = 70.0f;
    private final float lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

    private Timer sunTimer;

    private TimedLinearInterpolatorVec4 ambientInterpolator;
    private TimedLinearInterpolatorVec4 backgroundInterpolator;
    private TimedLinearInterpolatorVec4 sunlightInterpolator;
    private TimedLinearInterpolatorFloat maxIntensityInterpolator;

    private ArrayList<ConstVelLinearInterpolatorVec3> lightPos;
    private ArrayList<Vector4f> lightIntensity;
    private ArrayList<Timer> lightTimers;

    private HashMap<String, Timer> extraTimers;


    static class SunlightValue {
        float normTime;
        Vector4f ambient;
        Vector4f sunlightIntensity;
        Vector4f backgroundColor;

        SunlightValue(float normTime, Vector4f ambient, Vector4f sunlightIntensity, Vector4f backgroundColor) {
            this.normTime = normTime;
            this.ambient = ambient;
            this.sunlightIntensity = sunlightIntensity;
            this.backgroundColor = backgroundColor;
        }
    }

    static class SunlightValueHDR {
        float normTime;
        Vector4f ambient;
        Vector4f sunlightIntensity;
        Vector4f backgroundColor;
        float maxIntensity;

        SunlightValueHDR(float normTime, Vector4f ambient, Vector4f sunlightIntensity, Vector4f backgroundColor,
                         float maxIntensity) {
            this.normTime = normTime;
            this.ambient = ambient;
            this.sunlightIntensity = sunlightIntensity;
            this.backgroundColor = backgroundColor;
            this.maxIntensity = maxIntensity;
        }
    }

    ////////////////////////////////
    Vector4f getSunlightDirection() {
        float angle = 2.0f * 3.14159f * sunTimer.getAlpha();
        Vector4f sunDirection = new Vector4f(0.0f);
        sunDirection.x = (float) Math.sin(angle);
        sunDirection.y = (float) Math.cos(angle);
        // Keep the sun from being perfectly centered overhead.
        Matrix4f rotationMat = new Matrix4f().rotate((float) Math.toRadians(5.0f), 0.0f, 1.0f, 0.0f);
        rotationMat.transform(sunDirection);
        return sunDirection;
    }

    Vector4f getSunlightIntensity() {
        return sunlightInterpolator.interpolate(sunTimer.getAlpha());
    }


    int getNumberOfPointLights() {
        return lightPos.size();
    }


    Vector3f getWorldLightPosition(int lightIndex) {
        return lightPos.get(lightIndex).interpolate(lightTimers.get(lightIndex).getAlpha());
    }


    void setPointLightIntensity(int lightIndex, Vector4f intensity) {
        lightIntensity.set(lightIndex, intensity);
    }

    Vector4f getPointLightIntensity(int lightIndex) {
        return lightIntensity.get(lightIndex);
    }


    Vector4f getBackgroundColor() {
        return backgroundInterpolator.interpolate(sunTimer.getAlpha());
    }

    ////////////////////////////////
    private static final int NUMBER_OF_LIGHTS = 4;
    private static final int NUMBER_OF_POINT_LIGHTS = NUMBER_OF_LIGHTS - 1;

    class LightBlockHDR implements Bufferable {
        static final int SIZE = 4 * (4 + 1 + 1 + 2) + PerLight.SIZE_IN_BYTES * NUMBER_OF_LIGHTS;

        Vector4f ambientIntensity;
        float lightAttenuation;
        float maxIntensity;
        float padding[] = new float[2];
        PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

        @Override
        public ByteBuffer get(ByteBuffer buffer) {
            buffer.putFloat(ambientIntensity.x);
            buffer.putFloat(ambientIntensity.y);
            buffer.putFloat(ambientIntensity.z);
            buffer.putFloat(ambientIntensity.w);
            buffer.putFloat(lightAttenuation);
            buffer.putFloat(maxIntensity);
            buffer.putFloat(padding[0]);
            buffer.putFloat(padding[1]);
            for (PerLight light : lights) {
                if (light == null) break;
                light.get(buffer);
            }
            return buffer;
        }
    }

    class LightBlockGamma implements Bufferable {
        static final int SIZE = 4 * (4 + 1 + 1 + 2) + PerLight.SIZE_IN_BYTES * NUMBER_OF_LIGHTS;

        Vector4f ambientIntensity;
        float lightAttenuation;
        float maxIntensity;
        float gamma;
        float padding;
        PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

        @Override
        public ByteBuffer get(ByteBuffer buffer) {
            buffer.putFloat(ambientIntensity.x);
            buffer.putFloat(ambientIntensity.y);
            buffer.putFloat(ambientIntensity.z);
            buffer.putFloat(ambientIntensity.w);
            buffer.putFloat(lightAttenuation);
            buffer.putFloat(maxIntensity);
            buffer.putFloat(gamma);
            buffer.putFloat(padding);
            for (PerLight light : lights) {
                if (light == null) break;
                light.get(buffer);
            }
            return buffer;
        }
    }


    LightBlock getLightInformation(Matrix4f worldToCameraMat) {
        LightBlock lightData = new LightBlock();

        lightData.ambientIntensity = ambientInterpolator.interpolate(sunTimer.getAlpha());
        lightData.lightAttenuation = lightAttenuation;

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = worldToCameraMat.transform(new Vector4f(getSunlightDirection()));
        lightData.lights[0].lightIntensity = sunlightInterpolator.interpolate(sunTimer.getAlpha());

        for (int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
            Vector4f worldLightPos = new Vector4f(lightPos.get(light).interpolate(lightTimers.get(light).getAlpha()), 1.0f);
            Vector4f lightPosCameraSpace = worldToCameraMat.transform(new Vector4f(worldLightPos));

            lightData.lights[light + 1] = new PerLight();
            lightData.lights[light + 1].cameraSpaceLightPos = lightPosCameraSpace;
            lightData.lights[light + 1].lightIntensity = new Vector4f(lightIntensity.get(light));
        }

        return lightData;
    }

    LightBlockHDR getLightInformationHDR(Matrix4f worldToCameraMat) {
        LightBlockHDR lightData = new LightBlockHDR();

        lightData.ambientIntensity = ambientInterpolator.interpolate(sunTimer.getAlpha());
        lightData.lightAttenuation = lightAttenuation;
        lightData.maxIntensity = maxIntensityInterpolator.interpolate(sunTimer.getAlpha());

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = worldToCameraMat.transform(new Vector4f(getSunlightDirection()));
        lightData.lights[0].lightIntensity = sunlightInterpolator.interpolate(sunTimer.getAlpha());

        for (int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
            Vector4f worldLightPos = new Vector4f(lightPos.get(light).interpolate(lightTimers.get(light).getAlpha()), 1.0f);
            Vector4f lightPosCameraSpace = worldToCameraMat.transform(new Vector4f(worldLightPos));

            lightData.lights[light + 1] = new PerLight();
            lightData.lights[light + 1].cameraSpaceLightPos = lightPosCameraSpace;
            lightData.lights[light + 1].lightIntensity = new Vector4f(lightIntensity.get(light));
        }

        return lightData;
    }

    LightBlockGamma getLightInformationGamma(Matrix4f worldToCameraMat) {
        LightBlockHDR lightDataHdr = getLightInformationHDR(worldToCameraMat);
        LightBlockGamma lightData = new LightBlockGamma();

        lightData.ambientIntensity = lightDataHdr.ambientIntensity;
        lightData.lightAttenuation = lightDataHdr.lightAttenuation;
        lightData.maxIntensity = lightDataHdr.maxIntensity;
        lightData.lights = lightDataHdr.lights;

        return lightData;
    }

    ////////////////////////////////
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
                temp.data = new Vector4f(LightManager.getValue(curr));
                temp.weight = LightManager.getTime(curr);

                values.add(temp);
            }

            if (isLooping && !values.isEmpty()) {
                Data temp = new Data();
                temp.data = new Vector4f(values.get(0).data);
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

    ////////////////////////////////
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

    class LightData extends Pair<Vector4f, Float> {
        LightData(Vector4f first, Float second) {
            this.first = first;
            this.second = second;
        }
    }


    private static Vector4f getValue(LightData data) {
        return data.first;
    }

    private static float getTime(LightData data) {
        return data.second;
    }


    float getValue(MaxIntensityData data) {
        return data.first;
    }

    private static float getTime(MaxIntensityData data) {
        return data.second;
    }


    void setSunlightValues(SunlightValue values[], int size) {
        ArrayList<LightData> ambient = new ArrayList<>();
        ArrayList<LightData> light = new ArrayList<>();
        ArrayList<LightData> background = new ArrayList<>();

        for (int valIndex = 0; valIndex < size; valIndex++) {
            ambient.add(new LightData(new Vector4f(values[valIndex].ambient), values[valIndex].normTime));
            light.add(new LightData(new Vector4f(values[valIndex].sunlightIntensity), values[valIndex].normTime));
            background.add(new LightData(new Vector4f(values[valIndex].backgroundColor), values[valIndex].normTime));
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
            ambient.add(new LightData(new Vector4f(values[valIndex].ambient), values[valIndex].normTime));
            light.add(new LightData(new Vector4f(values[valIndex].sunlightIntensity), values[valIndex].normTime));
            background.add(new LightData(new Vector4f(values[valIndex].backgroundColor), values[valIndex].normTime));
            maxIntensity.add(new MaxIntensityData(values[valIndex].maxIntensity, values[valIndex].normTime));
        }

        ambientInterpolator.setValues(ambient);
        sunlightInterpolator.setValues(light);
        backgroundInterpolator.setValues(background);
        maxIntensityInterpolator.setValues(maxIntensity);
    }

    ////////////////////////////////
    enum TimerTypes {
        SUN,
        LIGHTS,
        ALL,

        NUM_TIMER_TYPES
    }


    void createTimer(String timerName, Timer.Type timerType, float duration) {
        extraTimers.put(timerName, new Timer(timerType, duration));
    }

    float getTimerValue(String timerName) {
        if (!extraTimers.containsKey(timerName)) return -1.0f;
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
}
