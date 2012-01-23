package rosick.glutil.pole;

import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Vec4;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ObjectPole {

	private ObjectData m_po;
	private ObjectData m_initialPo;
	private float m_rotateScale;
	private MouseButtons m_actionButton;
	private ViewProvider m_pView;

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public ObjectPole(ObjectData initialData, float rotateScale,
			MouseButtons actionButton, ViewProvider pLookatProvider) {
		m_initialPo = initialData;
		m_po = initialData;
		m_rotateScale = rotateScale;
		m_actionButton = actionButton;
		m_pView = pLookatProvider;
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Mat4 calcMatrix() {
		Mat4 translateMat = new Mat4(); 
		translateMat.putColumn(3, new Vec4(m_po.position, 1.0f));

		return translateMat.mul(Glm.matCast(m_po.orientation));
	}
}