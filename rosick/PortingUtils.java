package rosick;

import java.nio.Buffer;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class PortingUtils {
		
	public static abstract class BufferableData<T extends Buffer> {		
		
		public abstract T fillBuffer(T buffer);
		
		public T fillAndFlipBuffer(T buffer) {
			buffer.clear();
			fillBuffer(buffer);
			buffer.flip();
			
			return buffer;
		}
	}
}