package rosick.glutil.pole;

import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Quaternion;
import rosick.glm.Vec3;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ViewPole extends ViewProvider {

	private ViewData m_currView;
	private ViewScale m_viewScale;
	private ViewData m_initialView;
	private MouseButtons m_actionButton;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public ViewPole(ViewData initialView, ViewScale viewScale, MouseButtons actionButton) {
		m_currView = initialView;
		m_viewScale = viewScale;
		m_initialView = initialView;
		m_actionButton = actionButton;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Mat4 calcMatrix() {
		Mat4 mat = new Mat4(1.0f);
		
		//Remember: these transforms are in reverse order.

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
}
