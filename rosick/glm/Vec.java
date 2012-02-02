package rosick.glm;

import java.nio.FloatBuffer;

import rosick.PortingUtils.Bufferable;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public abstract class Vec implements Bufferable<FloatBuffer> {
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int W = 3;
	
	float vector[];

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.put(vector);
		buffer.flip();
		
		return buffer;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public final float[] get() {
		return vector;
	}
	
	public final float get(int index) {
		return vector[index];
	}
	
	
	public final void set(int index, float value) {
		vector[index] = value;
	}
}