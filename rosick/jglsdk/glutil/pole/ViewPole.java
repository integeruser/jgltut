package rosick.jglsdk.glutil.pole;

import static rosick.jglsdk.glm.Vec.*;

import org.lwjgl.input.Keyboard;

import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec2;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glutil.pole.MousePole.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ViewPole extends ViewProvider {
	
	private enum RotateMode {
		RM_DUAL_AXIS_ROTATE,
		RM_BIAXIAL_ROTATE,
		RM_XZ_AXIS_ROTATE,
		RM_Y_AXIS_ROTATE,
		RM_SPIN_VIEW_AXIS
	};
	
	
	private enum TargetOffsetDir {
		DIR_UP,
		DIR_DOWN,
		DIR_FORWARD,
		DIR_BACKWARD,
		DIR_RIGHT,
		DIR_LEFT
	};
	
	
	private ViewData m_currView;
	private ViewScale m_viewScale;
	
	private ViewData m_initialView;
	private MouseButtons m_actionButton;
	private boolean m_bRightKeyboardCtrls;
	
	// Used when rotating.
	private boolean m_bIsDragging;
	private RotateMode m_RotateMode;
	
	private float m_degStarDragSpin;
	private Vec2 m_startDragMouseLoc;
	private Quaternion m_startDragOrient;

	private Vec3 g_offsets[] = {
		new Vec3( 0.0f,  1.0f,  0.0f),
		new Vec3( 0.0f, -1.0f,  0.0f),
		new Vec3( 0.0f,  0.0f, -1.0f),
		new Vec3( 0.0f,  0.0f,  1.0f),
		new Vec3( 1.0f,  0.0f,  0.0f),
		new Vec3(-1.0f,  0.0f,  0.0f)
	};
	


	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public ViewPole(ViewData initialView, ViewScale viewScale) {
		this(initialView, viewScale, MouseButtons.MB_LEFT_BTN, false);
	}
	
	public ViewPole(ViewData initialView, ViewScale viewScale, MouseButtons actionButton) {
		this(initialView, viewScale, actionButton, false);
	}
	
	public ViewPole(ViewData initialView, ViewScale viewScale, MouseButtons actionButton, boolean bRightKeyboardCtrls) {
		m_currView = initialView;
		m_viewScale = viewScale;
		m_initialView = initialView;
		m_actionButton = actionButton;
		
		m_bRightKeyboardCtrls = bRightKeyboardCtrls;
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	public void mouseMove(Vec2 position) {
		if (m_bIsDragging) {
			onDragRotate(position);
		}
	}
	
	@Override
	public void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vec2 position) {
		if (isPressed) {
			// Ignore all other button presses when dragging.
			if (!m_bIsDragging) {
				if (button == m_actionButton) {
					if (modifiers == MouseModifiers.MM_KEY_CTRL)
						beginDragRotate(position, RotateMode.RM_BIAXIAL_ROTATE);
					else if (modifiers == MouseModifiers.MM_KEY_ALT)
						beginDragRotate(position, RotateMode.RM_SPIN_VIEW_AXIS);
					else
						beginDragRotate(position, RotateMode.RM_DUAL_AXIS_ROTATE);
				}
			}
		} else {
			// Ignore all other button releases when not dragging
			if (m_bIsDragging) {
				if (button == m_actionButton) {
					if (m_RotateMode == RotateMode.RM_DUAL_AXIS_ROTATE ||
							m_RotateMode == RotateMode.RM_SPIN_VIEW_AXIS || 
							m_RotateMode == RotateMode.RM_BIAXIAL_ROTATE) {
						endDragRotate(position);
					}
				}
			}
		}
	}
	
	@Override
	public void mouseWheel(int direction, MouseModifiers modifiers, Vec2 position) {	
		if (direction > 0) {
			moveCloser(modifiers != MouseModifiers.MM_KEY_SHIFT);
		} else {
			moveAway(modifiers != MouseModifiers.MM_KEY_SHIFT);
		}
	}
	
	
	@Override
	public void charPress(int key, boolean isShiftPressed, float lastFrameDuration) {
		if (m_bRightKeyboardCtrls) {
			switch (key) {
				case Keyboard.KEY_I: offsetTargetPos(TargetOffsetDir.DIR_FORWARD, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_K: offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_L: offsetTargetPos(TargetOffsetDir.DIR_RIGHT, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_J: offsetTargetPos(TargetOffsetDir.DIR_LEFT, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_O: offsetTargetPos(TargetOffsetDir.DIR_UP, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_U: offsetTargetPos(TargetOffsetDir.DIR_DOWN, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
			}
		} else {
			switch (key) {
				case Keyboard.KEY_W: offsetTargetPos(TargetOffsetDir.DIR_FORWARD, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_S: offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_D: offsetTargetPos(TargetOffsetDir.DIR_RIGHT, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_A: offsetTargetPos(TargetOffsetDir.DIR_LEFT, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_E: offsetTargetPos(TargetOffsetDir.DIR_UP, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
				
				case Keyboard.KEY_Q: offsetTargetPos(TargetOffsetDir.DIR_DOWN, 
						isShiftPressed ? m_viewScale.smallPosOffset : m_viewScale.largePosOffset, 
						lastFrameDuration); 
				break;
			}
		}
	};

	
	@Override
	public Mat4 calcMatrix() {
		Mat4 mat = new Mat4(1.0f);

		// Remember: these transforms are in reverse order.

		// In this space, we are facing in the correct direction. Which means that the camera point
		// is directly behind us by the radius number of units.
		mat = Glm.translate(mat, new Vec3(0.0f, 0.0f, -m_currView.radius));

		// Rotate the world to look in the right direction..
		Quaternion fullRotation = Glm.angleAxis(m_currView.degSpinRotation, new Vec3(0.0f, 0.0f, 1.0f)).mul(m_currView.orient);

		mat.mul(Glm.matCast(fullRotation));

		// Translate the world by the negation of the lookat point, placing the origin at the lookat point.
		mat = Glm.translate(mat, Vec3.negate(m_currView.targetPos));

		return mat;
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public void reset() {
		if (!m_bIsDragging) {
			m_currView = m_initialView;
		}
	}
	
	
	public ViewData getView() {
		return m_currView;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void beginDragRotate(Vec2 ptStart, RotateMode rotMode) {
		m_RotateMode = rotMode;
		m_startDragMouseLoc = ptStart;
		m_degStarDragSpin = m_currView.degSpinRotation;
		m_startDragOrient = m_currView.orient;
		m_bIsDragging = true;
	}

	
	private void onDragRotate(Vec2 ptCurr) {
		Vec2 iDiff = Vec2.sub(ptCurr, m_startDragMouseLoc);
		int iXDiff = (int) iDiff.get(X);
		int iYDiff = (int) - iDiff.get(Y);											// Y axis is different in LWJGL
		
		switch (m_RotateMode) {
			case RM_DUAL_AXIS_ROTATE:
				processXYChange(iXDiff, iYDiff);
				break;
			case RM_BIAXIAL_ROTATE:
				if (Math.abs(iXDiff) > Math.abs(iYDiff)) {
					processXChange(iXDiff, true);
				} else {
					processYChange(iYDiff, true);
				}
				break;
			case RM_XZ_AXIS_ROTATE:
				processXChange(iXDiff);
				break;
			case RM_Y_AXIS_ROTATE:
				processYChange(iYDiff);
				break;
			case RM_SPIN_VIEW_AXIS:
				processSpinAxis(iXDiff, iYDiff);
		}
	}


	private void endDragRotate(Vec2 ptEnd) {
		endDragRotate(ptEnd, true);
	}

	private void endDragRotate(Vec2 ptEnd, boolean bKeepResults) {
		if (bKeepResults) {
			onDragRotate(ptEnd);
		} else {
			m_currView.orient = m_startDragOrient;
		}

		m_bIsDragging = false;
	}


	private void processXChange(int iXDiff) {
		processXChange(iXDiff, false);
	}

	private void processXChange(int iXDiff, boolean bClearY) {
		float degAngleDiff = iXDiff * m_viewScale.rotationScale;

		// Rotate about the world-space Y axis.
		m_currView.orient = Quaternion.mul(m_startDragOrient, Glm.angleAxis(degAngleDiff, new Vec3(0.0f, 1.0f, 0.0f)));
	}

	
	private void processYChange(int iYDiff) {
		processYChange(iYDiff, false);
	}

	private void processYChange(int iYDiff, boolean bClearXZ) {
		float degAngleDiff = iYDiff * m_viewScale.rotationScale;

		// Rotate about the local-space X axis.
		m_currView.orient = Glm.angleAxis(degAngleDiff, new Vec3(1.0f, 0.0f, 0.0f)).mul(m_startDragOrient);
	}

	
	private void processXYChange(int iXDiff, int iYDiff) {
		float degXAngleDiff = (iXDiff * m_viewScale.rotationScale);
		float degYAngleDiff = (iYDiff * m_viewScale.rotationScale);

		// Rotate about the world-space Y axis.
		m_currView.orient = Quaternion.mul(m_startDragOrient, Glm.angleAxis(degXAngleDiff, new Vec3(0.0f, 1.0f, 0.0f)));
		// Rotate about the local-space X axis.
		m_currView.orient = Glm.angleAxis(degYAngleDiff, new Vec3(1.0f, 0.0f, 0.0f)).mul(m_currView.orient);
	}

	
	private void processSpinAxis(int iXDiff, int iYDiff) {
		float degSpinDiff = iXDiff * m_viewScale.rotationScale;
		
		m_currView.degSpinRotation = degSpinDiff + m_degStarDragSpin;
	}
	
	
	private void moveCloser(boolean bLargeStep) {
		if (bLargeStep) {
			m_currView.radius -= m_viewScale.largeRadiusDelta;

		} else {
			m_currView.radius -= m_viewScale.smallRadiusDelta;
		}
		
		if (m_currView.radius < m_viewScale.minRadius) {
			m_currView.radius = m_viewScale.minRadius;
		}
	}

	
	private void moveAway(boolean bLargeStep) {
		if (bLargeStep) {
			m_currView.radius += m_viewScale.largeRadiusDelta;
		} else {
			m_currView.radius += m_viewScale.smallRadiusDelta;
		}
		
		if (m_currView.radius > m_viewScale.maxRadius) {
			m_currView.radius = m_viewScale.maxRadius;
		}
	}
	
	
	private void offsetTargetPos(TargetOffsetDir eDir, float worldDistance, float lastFrameDuration) {
		Vec3 offsetDir = new Vec3(g_offsets[eDir.ordinal()]);
		offsetTargetPos(offsetDir.scale(worldDistance).scale(lastFrameDuration), lastFrameDuration);
	}

	private void offsetTargetPos(Vec3 cameraOffset, float lastFrameDuration) {
		Mat4 currMat = calcMatrix();
		Quaternion orientation = Glm.quatCast(currMat);

		Quaternion invOrient = Glm.conjugate(orientation);
		Vec3 worldOffset = invOrient.mul(cameraOffset);

		m_currView.targetPos.add(worldOffset);
	}
}