package rosick.glutil.pole;

import rosick.glm.Quaternion;
import rosick.glm.Vec3;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ViewData {
	
	public Vec3 targetPos;
	public Quaternion orient;
	public float radius;
	public float degSpinRotation;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public ViewData(Vec3 targetPos, Quaternion orient, float radius, float degSpinRotation) {
		this.targetPos = targetPos;
		this.orient = orient;
		this.radius = radius;
		this.degSpinRotation = degSpinRotation;
	}
}