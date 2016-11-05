package integeruser.jgltut.tut16;

import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.PerLight;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Interpolators.ConstVelLinearInterpolatorVec3;
import integeruser.jgltut.framework.Interpolators.WeightedLinearInterpolatorFloat;
import integeruser.jgltut.framework.Interpolators.WeightedLinearInterpolatorVec4;
import integeruser.jgltut.framework.Timer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2016%20Gamma%20and%20Textures/LightEnv.cpp
 */
class LightEnv {
    LightEnv(String envFileName) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            String filePath = Framework.findFileOrThrow(envFileName);

            doc = dBuilder.parse(LightEnv.class.getResourceAsStream(filePath));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Element lightEnvElement = doc.getDocumentElement();

        Element sunNode = (Element) lightEnvElement.getElementsByTagName("sun").item(0);
        if (sunNode == null) {
            throw new RuntimeException("There must be a 'lightenv' element that has a 'sun' element as a child.");
        }

        float timerTime = Float.parseFloat(sunNode.getAttribute("time"));

        sunTimer = new Timer(Timer.Type.LOOP, timerTime);

        ArrayList<LightData> ambient = new ArrayList<>();
        ArrayList<LightData> light = new ArrayList<>();
        ArrayList<LightData> background = new ArrayList<>();
        ArrayList<MaxIntensityData> maxIntensity = new ArrayList<>();

        {
            NodeList keys = sunNode.getElementsByTagName("key");
            Element key;
            int countKeys = 0;
            while ((key = (Element) keys.item(countKeys)) != null) {
                float keyTime = Float.parseFloat(key.getAttribute("time"));
                // Convert from hours to normalized time.
                keyTime = keyTime / 24.0f;

                String strVec4 = key.getAttribute("ambient");
                ambient.add(new LightData(parseVec4(strVec4), keyTime));

                strVec4 = key.getAttribute("intensity");
                light.add(new LightData(parseVec4(strVec4), keyTime));

                strVec4 = key.getAttribute("background");
                background.add(new LightData(parseVec4(strVec4), keyTime));

                maxIntensity.add(new MaxIntensityData(Float.parseFloat(key.getAttribute("max-intensity")), keyTime));

                countKeys++;
            }
        }

        ambientInterpolator.setValues(ambient);
        sunlightInterpolator.setValues(light);
        backgroundInterpolator.setValues(background);
        maxIntensityInterpolator.setValues(maxIntensity);

        {
            NodeList lights = lightEnvElement.getElementsByTagName("light");
            Element elemLight;
            int countLights = 0;
            while ((elemLight = (Element) lights.item(countLights)) != null) {

                if (lightPos.size() + 1 == MAX_NUMBER_OF_LIGHTS)
                    throw new RuntimeException("Too many lights specified.");

                float lightTime = Float.parseFloat(elemLight.getAttribute("time"));
                lightTimers.add(new Timer(Timer.Type.LOOP, lightTime));

                String strVec4 = elemLight.getAttribute("intensity");
                lightIntensity.add(parseVec4(strVec4));

                ArrayList<Vector3f> posValues = new ArrayList<>();
                NodeList keys = elemLight.getElementsByTagName("key");
                Element key;
                int countKeys = 0;
                while ((key = (Element) keys.item(countKeys)) != null) {
                    String text = key.getChildNodes().item(0).getNodeValue();
                    posValues.add(parseVec3(text));
                    countKeys++;
                }

                if (posValues.isEmpty())
                    throw new RuntimeException("'light' elements must have at least one 'key' element child.");

                ConstVelLinearInterpolatorVec3 lightInterpolatorVec3 = new ConstVelLinearInterpolatorVec3();
                lightInterpolatorVec3.setValues(posValues);
                lightPos.add(lightInterpolatorVec3);
                countLights++;
            }
        }
    }

    ////////////////////////////////
    private final static int MAX_NUMBER_OF_LIGHTS = 4;

    private Timer sunTimer;

    private TimedLinearInterpolatorVec4 ambientInterpolator = new TimedLinearInterpolatorVec4();
    private TimedLinearInterpolatorVec4 backgroundInterpolator = new TimedLinearInterpolatorVec4();
    private TimedLinearInterpolatorVec4 sunlightInterpolator = new TimedLinearInterpolatorVec4();
    private TimedLinearInterpolatorFloat maxIntensityInterpolator = new TimedLinearInterpolatorFloat();

    private ArrayList<ConstVelLinearInterpolatorVec3> lightPos = new ArrayList<>();
    private ArrayList<Vector4f> lightIntensity = new ArrayList<>();
    private ArrayList<Timer> lightTimers = new ArrayList<>();

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
                temp.weight = getTime(curr);

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
                temp.data = new Vector4f(getValue(curr));
                temp.weight = getTime(curr);

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

    ////////////////////////////////
    void updateTime(double elapsedTime) {
        sunTimer.update((float) elapsedTime);

        for (Timer timer : lightTimers) {
            timer.update((float) elapsedTime);
        }
    }


    void togglePause() {
        boolean isPaused = sunTimer.togglePause();
        setPause(isPaused);
    }

    void setPause(boolean pause) {
        sunTimer.setPause(pause);

        for (Timer timer : lightTimers) {
            timer.setPause(pause);
        }
    }


    void rewindTime(float secRewind) {
        sunTimer.rewind(secRewind);

        for (Timer timer : lightTimers) {
            timer.rewind(secRewind);
        }
    }

    void fastForwardTime(float secFF) {
        sunTimer.fastForward(secFF);

        for (Timer timer : lightTimers) {
            timer.fastForward(secFF);
        }
    }

    ////////////////////////////////
    LightBlock getLightBlock(Matrix4f worldToCameraMat) {
        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = ambientInterpolator.interpolate(sunTimer.getAlpha());
        float halfLightDistance = 70.0f;
        lightData.lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);
        lightData.maxIntensity = maxIntensityInterpolator.interpolate(sunTimer.getAlpha());

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = worldToCameraMat.transform(getSunlightDirection());
        lightData.lights[0].lightIntensity = sunlightInterpolator.interpolate(sunTimer.getAlpha());

        for (int lightIndex = 0; lightIndex < lightPos.size(); lightIndex++) {
            Vector4f worldLightPos = new Vector4f(lightPos.get(lightIndex).interpolate(lightTimers.get(lightIndex).getAlpha()), 1.0f);
            Vector4f lightPosCameraSpace = worldToCameraMat.transform(worldLightPos);

            lightData.lights[lightIndex + 1] = new PerLight();
            lightData.lights[lightIndex + 1].cameraSpaceLightPos = lightPosCameraSpace;
            lightData.lights[lightIndex + 1].lightIntensity = new Vector4f(lightIntensity.get(lightIndex));
        }

        return lightData;
    }


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

    Vector4f getSunlightScaledIntensity() {
        return new Vector4f(sunlightInterpolator.interpolate(sunTimer.getAlpha())).mul(
                1.0f / maxIntensityInterpolator.interpolate(sunTimer.getAlpha()));
    }


    int getNumLights() {
        return 1 + lightPos.size();
    }

    int getNumPointLights() {
        return lightPos.size();
    }


    Vector4f getPointLightScaledIntensity(int pointLightIndex) {
        return new Vector4f(lightIntensity.get(pointLightIndex)).mul(
                1.0f / maxIntensityInterpolator.interpolate(sunTimer.getAlpha()));
    }

    Vector3f getPointLightWorldPos(int pointLightIndex) {
        return lightPos.get(pointLightIndex).interpolate(lightTimers.get(pointLightIndex).getAlpha());
    }


    Vector4f getBackgroundColor() {
        return backgroundInterpolator.interpolate(sunTimer.getAlpha());
    }

    ////////////////////////////////
    private Vector4f parseVec4(String s) {
        Scanner snr = new Scanner(s);
        Vector4f res = new Vector4f();
        res.x = Float.parseFloat(snr.next());
        res.y = Float.parseFloat(snr.next());
        res.z = Float.parseFloat(snr.next());
        res.w = Float.parseFloat(snr.next());
        snr.close();
        return res;
    }

    private Vector3f parseVec3(String s) {
        Scanner snr = new Scanner(s);
        Vector3f res = new Vector3f();
        res.x = Float.parseFloat(snr.next());
        res.y = Float.parseFloat(snr.next());
        res.z = Float.parseFloat(snr.next());
        snr.close();
        return res;
    }
}
