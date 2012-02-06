package rosick;

import java.nio.Buffer;
import java.util.ArrayList;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class PortingUtils {

	public static interface Bufferable<T extends Buffer> {	
		public T fillBuffer(T buffer);
	}
	
	
	public static abstract class BufferableData {		
		public abstract byte[] getAsByteArray();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
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
	
	
	public static byte[] toByteArray(ArrayList<Character> data, int start, int length) {
		byte bytes[] = new byte[length];
		
		for (int i = start; i < start + length; i++) {
			bytes[i - start] = (byte) (char) data.get(i);
		}
		
		return bytes;
	}
	
	public static long toLong(byte data[]) {
		long value = 0;
		
		for (int i = 0; i < data.length; i++) {
		   value += (data[i] & 0xff) << (8 * i);
		}
		
		return value;
	}
}
