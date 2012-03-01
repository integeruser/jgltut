package rosick;

import java.nio.Buffer;
import java.util.ArrayList;


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
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	public static int toInt(ArrayList<Character> data, int startIndex, int length) {
		StringBuilder temp = new StringBuilder();
		
		for (int i = startIndex; i < startIndex + length; i++) {
			temp.append(data.get(i));
		}
		
		byte bytes[] = temp.toString().getBytes();
		long value = 0;
		
		for (int i = 0; i < bytes.length; i++) {
		   value += (bytes[i] & 0xff) << (8 * i);
		}
		
		return (int) value;
	}
	
	
	public static byte[] toByteArray(float data[]) {
		byte[] bytes = new byte[data.length * 4];

		for (int i = 0; i < data.length; i++) {
			int intBits = Float.floatToIntBits(data[i]);
			byte[] current = new byte[4];

			current[0] = (byte) ((intBits & 0x000000ff) >> 0);
			current[1] = (byte) ((intBits & 0x0000ff00) >> 8);
			current[2] = (byte) ((intBits & 0x00ff0000) >> 16);
			current[3] = (byte) ((intBits & 0xff000000) >> 24);

			System.arraycopy(current, 0, bytes, i * 4, 4);
		}

		return bytes;
	}
}