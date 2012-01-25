package rosick.glm;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public interface Bufferable {
	
	public FloatBuffer fillBuffer(FloatBuffer buffer);
}