package rosick;

import java.nio.Buffer;

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
}
