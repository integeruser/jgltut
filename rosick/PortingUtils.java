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
		public T fillAndFlipBuffer(T buffer);
		public T fillBuffer(T buffer);
	}
		
	public static abstract class BufferableData<T extends Buffer> implements Bufferable<T> {		
		
		@Override
		public T fillAndFlipBuffer(T buffer) {
			buffer.clear();
			fillBuffer(buffer);
			buffer.flip();
			
			return buffer;
		}
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	public static long toLong(byte data[]) {
		long value = 0;
		
		for (int i = 0; i < data.length; i++) {
		   value += (data[i] & 0xff) << (8 * i);
		}
		
		return value;
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
	
	public static byte[] toByteArray(ArrayList<Character> data, int start, int length) {
		String temp = "";
		
		for (int i = start; i < start + length; i++) {
			temp += data.get(i);
		}
		
		return temp.getBytes();
	}
}