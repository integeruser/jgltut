package rosick.framework;

import java.nio.Buffer;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public interface Bufferable<T extends Buffer> {
	
	public T fillBuffer(T buffer);
}