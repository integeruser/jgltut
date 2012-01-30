package rosick.framework;

import java.util.ArrayList;

import rosick.glm.Glm;
import rosick.glm.Vec3;
import rosick.glm.Vec4;
import rosick.mckesson.III.tut12.LightManager;
import rosick.mckesson.III.tut12.LightManager.LightVectorData;
import rosick.mckesson.III.tut12.LightManager.MaxIntensityData;


public class Interpolators {
	
	static class WeightedLinearInterpolatorFloat {
		
		class Data {
			float data;
			float weight;
		};
	
		ArrayList<Data> m_values = new ArrayList<>();
	
		
		public float interpolate(float fAlpha) {
			if (m_values.isEmpty()) {
				return 0.0f;
			}
			
			if (m_values.size() == 1) {
				return m_values.get(0).data;
			}
	
			// Find which segment we are within.
			int segment = 1;
			for(; segment < m_values.size(); segment++) {
				if (fAlpha < m_values.get(segment).weight) {
					break;
				}
			}
	
			if (segment == m_values.size()) {
				return m_values.get(segment - 1).data;
			}
	
			float sectionAlpha = fAlpha - m_values.get(segment - 1).weight;
			sectionAlpha /= m_values.get(segment).weight - m_values.get(segment - 1).weight;
	
			float invSecAlpha = 1.0f - sectionAlpha;
	
			return m_values.get(segment - 1).data * invSecAlpha + m_values.get(segment).data * sectionAlpha;
		}
	
		
		int numSegments() {
			return m_values.isEmpty() ? 0 : m_values.size() - 1;
		}
	}
	
	static class WeightedLinearInterpolatorVec3 {
		
		class Data {
			Vec3 data;
			float weight;
		};
	
		ArrayList<Data> m_values = new ArrayList<>();
	
		
		public Vec3 interpolate(float fAlpha) {
			if (m_values.isEmpty()) {
				new Vec3();
			}
			
			if (m_values.size() == 1) {
				return new Vec3(m_values.get(0).data);
			}
	
			// Find which segment we are within.
			int segment = 1;
			for(; segment < m_values.size(); segment++) {
				if (fAlpha < m_values.get(segment).weight) {
					break;
				}
			}
	
			if (segment == m_values.size()) {
				return new Vec3(m_values.get(segment - 1).data);
			}
	
			float sectionAlpha = fAlpha - m_values.get(segment - 1).weight;
			sectionAlpha /= m_values.get(segment).weight - m_values.get(segment - 1).weight;
	
			float invSecAlpha = 1.0f - sectionAlpha;
	
			return Vec3.scale(m_values.get(segment - 1).data, invSecAlpha).add(Vec3.scale(m_values.get(segment).data, sectionAlpha));
		}
	
		
		int numSegments() {
			return m_values.isEmpty() ? 0 : m_values.size() - 1;
		}
	}
	
	static class WeightedLinearInterpolatorVec4 {
		
		class Data {
			Vec4 data;
			float weight;
		};
	
		ArrayList<Data> m_values = new ArrayList<>();
	
		
		public Vec4 interpolate(float fAlpha) {
			if (m_values.isEmpty()) {
				new Vec4();
			}
			
			if (m_values.size() == 1) {
				return new Vec4(m_values.get(0).data);
			}
	
			// Find which segment we are within.
			int segment = 1;
			for(; segment < m_values.size(); segment++) {
				if (fAlpha < m_values.get(segment).weight) {
					break;
				}
			}
	
			if (segment == m_values.size()) {
				return new Vec4(m_values.get(segment - 1).data);
			}
			
			float sectionAlpha = fAlpha - m_values.get(segment - 1).weight;
			sectionAlpha /= m_values.get(segment).weight - m_values.get(segment - 1).weight;
	
			float invSecAlpha = 1.0f - sectionAlpha;
	
			return Vec4.scale(m_values.get(segment - 1).data, invSecAlpha).add(Vec4.scale(m_values.get(segment).data, sectionAlpha));
		}
	
		
		int numSegments() {
			return m_values.isEmpty() ? 0 : m_values.size() - 1;
		}
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static class ConstVelLinearInterpolatorVec3 extends WeightedLinearInterpolatorVec3 {

		float m_totalDist;
		

		public void setValues(ArrayList<Vec3> data) {
			setValues(data, true);
		}
		
		public void setValues(ArrayList<Vec3> data, boolean isLoop) {			
			m_values.clear();

			for (Vec3 curr : data) {
				Data currData = new Data();
				currData.data = new Vec3(curr);
				currData.weight = 0.0f;
				m_values.add(currData);
			}

			if (isLoop) {
				Data currData = new Data();
				currData.data = new Vec3(data.get(0));
				currData.weight = 0.0f;
				m_values.add(currData);
			}

			// Compute the distances of each segment.
			m_totalDist = 0.0f;			
			
			for (int iLoop = 1; iLoop < m_values.size(); iLoop++) {
				m_totalDist += distance((Vec3) m_values.get(iLoop - 1).data, (Vec3) m_values.get(iLoop).data);
				m_values.get(iLoop).weight = m_totalDist;
			}
			
			// Compute the alpha value that represents when to use this segment.
			for (int iLoop = 1; iLoop < m_values.size(); iLoop++) {
				m_values.get(iLoop).weight /= m_totalDist;
			}
		}


		float distance() {
			return m_totalDist;
		}	
		
		float distance(Vec3 lhs, Vec3 rhs) {
			return Glm.length(Vec3.sub(rhs, lhs));
		}
	}
	
	public static class LightInterpolatorVec3 extends ConstVelLinearInterpolatorVec3 {}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static class TimedLinearInterpolatorFloat extends WeightedLinearInterpolatorFloat {

		public void setValues(ArrayList<MaxIntensityData> data) {
			setValues(data, true);
		}
		
		public void setValues(ArrayList<MaxIntensityData> data, boolean isLooping) {
			m_values.clear();

			for (MaxIntensityData curr : data) {				
				Data temp = new Data();
				temp.data = LightManager.getValue(curr);
				temp.weight = LightManager.getTime(curr);

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
		
	public static class TimedLinearInterpolatorVec4 extends WeightedLinearInterpolatorVec4 {

		public void setValues(ArrayList<LightVectorData> data) {
			setValues(data, true);
		}
		
		public void setValues(ArrayList<LightVectorData> data, boolean isLooping) {
			m_values.clear();

			for (LightVectorData curr : data) {				
				Data temp = new Data();
				temp.data = new Vec4(LightManager.getValue(curr));
				temp.weight = LightManager.getTime(curr);

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
}