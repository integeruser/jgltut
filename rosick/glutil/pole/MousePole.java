package rosick.glutil.pole;

import rosick.glm.Mat4;
import rosick.glm.Quaternion;
import rosick.glm.Vec2;
import rosick.glm.Vec3;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class MousePole {

	/**
	 * The possible buttons that Poles can use.
	 */
	public enum MouseButtons {
		MB_LEFT_BTN,    
		MB_RIGHT_BTN,
		MB_MIDDLE_BTN;
	}
 
	/**
	 * Modifiers that may be held down while mouse movements go on.
	 */
	public enum MouseModifiers {		
		MM_KEY_SHIFT,   
		MM_KEY_CTRL,   
		MM_KEY_ALT;
	}


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public static abstract class Pole {
		public abstract void mouseMove(Vec2 vec2);
		public abstract void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vec2 position);
		public abstract void mouseWheel(int direction, MouseModifiers modifiers, Vec2 position);
	}

	public static abstract class ViewProvider extends Pole {
		public abstract Mat4 calcMatrix();
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 * Utility object containing the ObjectPole's position and orientation information.
	 */
	public static class ObjectData {
		Vec3 position;
		Quaternion orientation;

		public ObjectData(Vec3 position, Quaternion orientation) {
			this.position = position;
			this.orientation = orientation;
		}
	}


	/**
	 * Utility object containing the ViewPole's view information.
	 */
	public static class ViewData {
		Vec3 targetPos;
		Quaternion orient;
		float radius;
		float degSpinRotation;

		public ViewData(Vec3 targetPos, Quaternion orient, float radius, float degSpinRotation) {
			this.targetPos = targetPos;
			this.orient = orient;
			this.radius = radius;
			this.degSpinRotation = degSpinRotation;
		}
	}

	/**
	 * Utility object describing the scale of the ViewPole.
	 */
	public static class ViewScale {
		float minRadius;        
		float maxRadius;        
		float largeRadiusDelta; 
		float smallRadiusDelta; 
		float largePosOffset;   
		float smallPosOffset;   
		float rotationScale;

		public ViewScale(float minRadius, float maxRadius, float largeRadiusDelta,
				float smallRadiusDelta, float largePosOffset, float smallPosOffset,
				float rotationScale) {
			this.minRadius = minRadius;
			this.maxRadius = maxRadius;
			this.largeRadiusDelta = largeRadiusDelta;
			this.smallRadiusDelta = smallRadiusDelta;
			this.largePosOffset = largePosOffset;
			this.smallPosOffset = smallPosOffset;
			this.rotationScale = rotationScale;
		}
	}	
}