package rosick.glutil.pole;

import static rosick.glm.Vec.*;

import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Quaternion;
import rosick.glm.Vec2;
import rosick.glm.Vec3;
import rosick.glm.Vec4;
import rosick.glutil.pole.MousePole.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ObjectPole extends Pole {

	private enum Axis {
		AXIS_X,
		AXIS_Y,
		AXIS_Z,
	}
	
	private enum RotateMode {
		RM_DUAL_AXIS,
		RM_BIAXIAL,
		RM_SPIN,
	};

	
	private final ViewProvider m_pView;	 
	
	private ObjectData m_po;
	private ObjectData m_initialPo;

	private float m_rotateScale;
	private MouseButtons m_actionButton;

	// Used when rotating.
	private RotateMode m_RotateMode;
	private boolean m_bIsDragging;

	private Vec2 m_prevMousePos;
	private Vec2 m_startDragMousePos;
	private Quaternion m_startDragOrient;

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public ObjectPole(ObjectData initialData, float rotateScale, MouseButtons actionButton, ViewProvider pLookatProvider) {
		m_pView = pLookatProvider;
		m_po = initialData;
		m_initialPo = initialData;
		m_rotateScale = rotateScale;
		m_actionButton = actionButton;
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	public void mouseMove(Vec2 position) {
		if (m_bIsDragging) {
			Vec2 iDiff = Vec2.sub(position, m_prevMousePos);
	
			switch (m_RotateMode) {
				case RM_DUAL_AXIS: {
					Quaternion rot = calcRotationQuat(Axis.AXIS_Y.ordinal(), iDiff.get(X) * m_rotateScale);
					rot = Glm.normalize(calcRotationQuat(Axis.AXIS_X.ordinal(), - iDiff.get(Y) * m_rotateScale).mul(rot));	// Y axis is different in LWJGL
					rotateViewDegrees(rot);
					break;
				}	
				
				case RM_BIAXIAL: {
					Vec2 iInitDiff = Vec2.sub(position, m_startDragMousePos);
	
					Axis eAxis;
					float degAngle;
					if (Math.abs(iInitDiff.get(X)) > Math.abs(iInitDiff.get(Y))) {
						eAxis = Axis.AXIS_Y;
						degAngle = iInitDiff.get(X) * m_rotateScale;
					} else {
						eAxis = Axis.AXIS_X;
						degAngle = iInitDiff.get(Y) * m_rotateScale;
					}
	
					Quaternion rot = calcRotationQuat(eAxis.ordinal(), degAngle);
					rotateViewDegrees(rot, true);
					break;
				}
				
				case RM_SPIN: {
					rotateViewDegrees(calcRotationQuat(Axis.AXIS_Z.ordinal(), - iDiff.get(X) * m_rotateScale));
					break;
				}
			}

			m_prevMousePos = position;
		}
	}
	
	@Override
	public void mouseClick(MouseButtons button, boolean isPressed, MouseModifiers modifiers, Vec2 position) {
		if (isPressed) {
			// Ignore button presses when dragging.
			if (!m_bIsDragging){
				if (button == m_actionButton) {
					if (modifiers == MouseModifiers.MM_KEY_ALT)
						m_RotateMode = RotateMode.RM_SPIN;
					else if(modifiers == MouseModifiers.MM_KEY_CTRL)
						m_RotateMode = RotateMode.RM_BIAXIAL;
					else
						m_RotateMode = RotateMode.RM_DUAL_AXIS;

					m_prevMousePos = position;
					m_startDragMousePos = position;
					m_startDragOrient = m_po.orientation;

					m_bIsDragging = true;
				}
			}
		} else {
			// Ignore up buttons if not dragging.
			if (m_bIsDragging) {
				if (button == m_actionButton) {
					mouseMove(position);

					m_bIsDragging = false;
				}
			}
		}
	}

	@Override
	public void mouseWheel(int direction, MouseModifiers modifiers, Vec2 position) {		
	}
	
	
	@Override
	public void charPress(int key, boolean isShiftPressed, float lastFrameDuration) {		
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public void reset() {
		if (!m_bIsDragging) {
			m_po = m_initialPo;
		}
	}
	
	
	public Mat4 calcMatrix() {
		Mat4 translateMat = new Mat4(1.0f);
		translateMat.setColumn(3, new Vec4(m_po.position, 1.0f));

		return translateMat.mul(Glm.matCast(m_po.orientation));
	}


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void rotateWorldDegrees(Quaternion rot, boolean bFromInitial) {
		if (!m_bIsDragging) {
			bFromInitial = false;
		}
		
		m_po.orientation = Glm.normalize(Quaternion.mul(rot, bFromInitial ? m_startDragOrient : m_po.orientation));
	}


	private void rotateViewDegrees(Quaternion rot) {
		rotateViewDegrees(rot, false);
	}

	private void rotateViewDegrees(Quaternion rot, boolean bFromInitial) {
		if (!m_bIsDragging) {
			bFromInitial = false;
		}
		
		if (m_pView != null) {
			Quaternion viewQuat = Glm.quatCast(m_pView.calcMatrix());
			Quaternion invViewQuat = Glm.conjugate(viewQuat);

			m_po.orientation = Glm.normalize(Quaternion.mul(Quaternion.mul(invViewQuat,rot), (viewQuat)).mul(bFromInitial ? m_startDragOrient : m_po.orientation));		
		} else {
			rotateWorldDegrees(rot, bFromInitial);
		}
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Vec3 g_axisVectors[] = new Vec3[] {
		new Vec3(1.0f, 0.0f, 0.0f),
		new Vec3(0.0f, 1.0f, 0.0f),
		new Vec3(0.0f, 0.0f, 1.0f),
	};

	
	private Quaternion calcRotationQuat(int eAxis, float degAngle) {
		return Glm.angleAxis(degAngle, g_axisVectors[eAxis]);
	}
}