package rosick.glutil.pole;

import rosick.glm.Quaternion;
import rosick.glm.Vec3;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ObjectData {

	public Vec3 position;
	public Quaternion orientation;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public ObjectData(Vec3 position, Quaternion orientation) {
		this.position = position;
		this.orientation = orientation;
	}
}