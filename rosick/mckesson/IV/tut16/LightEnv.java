package rosick.mckesson.IV.tut16;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import rosick.jglsdk.BufferableData;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.mckesson.framework.Timer;
import rosick.mckesson.framework.Interpolators.ConstVelLinearInterpolatorVec3;
import rosick.mckesson.framework.Interpolators.WeightedLinearInterpolatorFloat;
import rosick.mckesson.framework.Interpolators.WeightedLinearInterpolatorVec4;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public class LightEnv {

	class PerLight extends BufferableData<FloatBuffer> {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;
		
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
		float maxIntensity;
		float padding[] = new float[2];
		PerLight lights[] = new PerLight[MAX_NUMBER_OF_LIGHTS];

		static final int SIZE = (4 + 1 + 1 + 2 + (8 * 4)) * (Float.SIZE / 8);

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {			
			ambientIntensity.fillBuffer(buffer);
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(padding);
			
			for (PerLight light : lights) {
				if (light == null)
					break;
				
				light.fillBuffer(buffer);
			}
			
			return buffer;
		}
	}
	

	private class LightInterpolatorVec3 extends ConstVelLinearInterpolatorVec3 {}
	
	
	private final int MAX_NUMBER_OF_LIGHTS = 4;
	
	private final float g_fHalfLightDistance = 70.0f;
	private final float g_fLightAttenuation = 1.0f / (g_fHalfLightDistance * g_fHalfLightDistance);

	private Timer m_sunTimer;

	private float m_fLightAttenuation;
	
	private TimedLinearInterpolatorVec4 m_ambientInterpolator = new TimedLinearInterpolatorVec4();
	private TimedLinearInterpolatorVec4 m_backgroundInterpolator = new TimedLinearInterpolatorVec4();
	private TimedLinearInterpolatorVec4 m_sunlightInterpolator = new TimedLinearInterpolatorVec4();
	private TimedLinearInterpolatorFloat m_maxIntensityInterpolator = new TimedLinearInterpolatorFloat();
	
	private ArrayList<LightInterpolatorVec3> m_lightPos = new ArrayList<>();
	private ArrayList<Vec4> m_lightIntensity = new ArrayList<>();
	private ArrayList<Timer> m_lightTimers = new ArrayList<>();
		
		
	LightEnv(String envFileName) {
		m_fLightAttenuation = 40.0f;
		
		// crea il parser e ci associa il file di input
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(ClassLoader.class.getResourceAsStream(envFileName));
		} catch (SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace();
			System.exit(0);
		}	
		
		Element lightenvElement = doc.getDocumentElement();
		
		m_fLightAttenuation = Float.parseFloat(lightenvElement.getAttribute("atten"));
		m_fLightAttenuation = 1.0f / (m_fLightAttenuation * m_fLightAttenuation);
		
		Element sunNode = (Element) lightenvElement.getElementsByTagName("sun").item(0);
		if (sunNode == null) {
			throw new RuntimeException("There must be a 'lightenv' element that has a 'sun' element as a child.");
		}
		
		float timerTime = Float.parseFloat(sunNode.getAttribute("time"));
		
		m_sunTimer = new Timer(Timer.Type.TT_LOOP, timerTime);
		
		LightVector ambient = new LightVector();
		LightVector light = new LightVector();
		LightVector background = new LightVector();
		MaxIntensityVector maxIntensity = new MaxIntensityVector();
		
		{
			NodeList keys = sunNode.getElementsByTagName("key");
			Element key;
			int countKeys = 0;
			while ((key = (Element) keys.item(countKeys)) != null) {
				float keyTime = Float.parseFloat(key.getAttribute("time"));
				//Convert from hours to normalized time.
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
		
		m_ambientInterpolator.setValues(ambient);
		m_sunlightInterpolator.setValues(light);
		m_backgroundInterpolator.setValues(background);
		m_maxIntensityInterpolator.setValues(maxIntensity);
		
		{
			NodeList lights = lightenvElement.getElementsByTagName("light");
			Element elemlight;
			int countLights = 0;
			while ((elemlight = (Element) lights.item(countLights)) != null) {
				
				if(m_lightPos.size() + 1 == MAX_NUMBER_OF_LIGHTS)
					throw new RuntimeException("Too many lights specified.");
				
				float lightTime = Float.parseFloat(elemlight.getAttribute("time"));
				m_lightTimers.add(new Timer(Timer.Type.TT_LOOP, lightTime));
				
				String strVec4 = elemlight.getAttribute("intensity");
				m_lightIntensity.add(parseVec4(strVec4));
				
				ArrayList<Vec3> posValues = new ArrayList<>();
				NodeList keys = elemlight.getElementsByTagName("key");
				Element key;
				int countKeys = 0;
				while ((key = (Element) keys.item(countKeys)) != null) {
					String text = key.getChildNodes().item(0).getNodeValue();
					posValues.add(parseVec3(text));
					countKeys++;
				}
				
				if(posValues.isEmpty())
					throw new RuntimeException("'light' elements must have at least one 'key' element child.");

				LightInterpolatorVec3 lightInterpolatorVec3 = new LightInterpolatorVec3();
				lightInterpolatorVec3.setValues(posValues);
				m_lightPos.add(lightInterpolatorVec3);
				countLights++;
			}
		}
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	public class TimedLinearInterpolatorFloat extends WeightedLinearInterpolatorFloat {

		public void setValues(ArrayList<MaxIntensityData> data) {
			setValues(data, true);
		}
		
		public void setValues(ArrayList<MaxIntensityData> data, boolean isLooping) {
			m_values.clear();

			for (MaxIntensityData curr : data) {				
				Data temp = new Data();
				temp.data = getValue(curr);
				temp.weight = getTime(curr);

				m_values.add(temp);
			}
			
			if (isLooping && !m_values.isEmpty()) {
				Data temp = new Data();
				temp.data = m_values.get(0).data;
				temp.weight = m_values.get(0).weight;

				m_values.add(temp);
			}
				
			// Ensure first is weight 0, and last is weight 1.
			if (!m_values.isEmpty()) {
				m_values.get(0).weight = 0.0f;
				m_values.get(m_values.size() - 1).weight = 1.0f;
			}
		}
	}
		
	public class TimedLinearInterpolatorVec4 extends WeightedLinearInterpolatorVec4 {

		public void setValues(ArrayList<LightData> data) {
			setValues(data, true);
		}
		
		public void setValues(ArrayList<LightData> data, boolean isLooping) {
			m_values.clear();

			for (LightData curr : data) {				
				Data temp = new Data();
				temp.data = new Vec4(getValue(curr));
				temp.weight = getTime(curr);

				m_values.add(temp);
			}
			
			if (isLooping && !m_values.isEmpty()) {
				Data temp = new Data();
				temp.data = new Vec4(m_values.get(0).data);
				temp.weight = m_values.get(0).weight;

				m_values.add(temp);
			}
				
			// Ensure first is weight 0, and last is weight 1.
			if (!m_values.isEmpty()) {
				m_values.get(0).weight = 0.0f;
				m_values.get(m_values.size() - 1).weight = 1.0f;
			}
		}
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
	
	public class LightData extends Pair<Vec4, Float> {
		public LightData(Vec4 first, Float second) {
			this.first = first;
			this.second = second;
		}
	}
	
	
	public class MaxIntensityVector extends ArrayList<MaxIntensityData> {
		private static final long serialVersionUID = -3083128917290208757L;			// Autogenerated by Eclipse Ide
	}

	public class LightVector extends ArrayList<LightData> {
		private static final long serialVersionUID = -7238863853470466667L;			// Autogenerated by Eclipse Ide
	}

	
	public static Vec4 getValue(LightData data) {
		return data.first;
	}
	
	public static float getTime(LightData data) {
		return data.second;
	}
	
	
	public float getValue(MaxIntensityData data) {
		return data.first;
	}

	public static float getTime(MaxIntensityData data) {
		return data.second;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
		
	void updateTime(double fElapsedTime) {
		m_sunTimer.update((float) fElapsedTime);
		
		for (Timer timer : m_lightTimers) {
			timer.update((float) fElapsedTime);
		}
	}

	
	void togglePause() {
		boolean isPaused = m_sunTimer.togglePause();
		setPause(isPaused);
	}
	
	void setPause(boolean pause) {
		m_sunTimer.setPause(pause);

		for (Timer timer : m_lightTimers) {
			timer.setPause(pause);
		}
	}

	
	void rewindTime(float secRewind) {
		m_sunTimer.rewind(secRewind);
		
		for (Timer timer : m_lightTimers) {
			timer.rewind(secRewind);
		}
	}

	void fastForwardTime(float secFF) {
		m_sunTimer.fastForward(secFF);
		
		for (Timer timer : m_lightTimers) {
			timer.fastForward(secFF);
		}
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Vec4 parseVec4(String s) {
		Scanner snr = new Scanner(s);
		Vec4 res = new Vec4();
		
		res.x = Float.parseFloat(snr.next());
		res.y = Float.parseFloat(snr.next());
		res.z = Float.parseFloat(snr.next());
		res.w = Float.parseFloat(snr.next());

		return res;
	}
	
	public Vec3 parseVec3(String s) {
		Scanner snr = new Scanner(s);
		Vec3 res = new Vec3();
		
		res.x = Float.parseFloat(snr.next());
		res.y = Float.parseFloat(snr.next());
		res.z = Float.parseFloat(snr.next());

		return res;
	}
		
	
	LightBlock getLightBlock(Mat4 worldToCameraMat) {
		LightBlock lightData = new LightBlock();

		lightData.ambientIntensity = m_ambientInterpolator.interpolate(m_sunTimer.getAlpha());
		lightData.lightAttenuation = g_fLightAttenuation;
		lightData.maxIntensity = m_maxIntensityInterpolator.interpolate(m_sunTimer.getAlpha());

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCameraMat, getSunlightDirection());
		lightData.lights[0].lightIntensity = m_sunlightInterpolator.interpolate(m_sunTimer.getAlpha());

		for (int light = 0; light < m_lightPos.size(); light++) {
			Vec4 worldLightPos = new Vec4(m_lightPos.get(light).interpolate(m_lightTimers.get(light).getAlpha()), 1.0f);
			Vec4 lightPosCameraSpace = Mat4.mul(worldToCameraMat, worldLightPos);

			lightData.lights[light + 1] = new PerLight();
			lightData.lights[light + 1].cameraSpaceLightPos = lightPosCameraSpace;
			lightData.lights[light + 1].lightIntensity = new Vec4(m_lightIntensity.get(light));
		}

		return lightData;
	}
	
	
	Vec4 getSunlightDirection() {
		float angle = 2.0f * 3.14159f * m_sunTimer.getAlpha();
		Vec4 sunDirection = new Vec4(0.0f);
		sunDirection.x = (float) Math.sin(angle);
		sunDirection.y = (float) Math.cos(angle);

		// Keep the sun from being perfectly centered overhead.
		sunDirection = Mat4.mul(Glm.rotate(new Mat4(1.0f), 5.0f, new Vec3(0.0f, 1.0f, 0.0f)), sunDirection);

		return sunDirection;
	}
	
	Vec4 getSunlightScaledIntensity() {
		return Vec4.scale(m_sunlightInterpolator.interpolate(m_sunTimer.getAlpha()),
				1.0f / m_maxIntensityInterpolator.interpolate(m_sunTimer.getAlpha()));
	}
	
	
	int getNumLights() {
		return 1 + m_lightPos.size();
	}

	int getNumPointLights() {
		return m_lightPos.size();
	}

	
	Vec4 getPointLightIntensity(int pointLightIx) {
		return m_lightIntensity.get(pointLightIx);
	}
	
	Vec4 getPointLightScaledIntensity(int pointLightIx) {
		return Vec4.scale(m_lightIntensity.get(pointLightIx), 
				1.0f / m_maxIntensityInterpolator.interpolate(m_sunTimer.getAlpha()));
	}	
	
	Vec3 getPointLightWorldPos(int pointLightIx) {
		return m_lightPos.get(pointLightIx).interpolate(m_lightTimers.get(pointLightIx).getAlpha());
	}
	
	
	Vec4 getBackgroundColor() {
		return m_backgroundInterpolator.interpolate(m_sunTimer.getAlpha());
	}
}