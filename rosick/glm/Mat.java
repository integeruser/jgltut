package rosick.glm;

import java.nio.FloatBuffer;

import rosick.PortingUtils.Bufferable;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public abstract class Mat implements Bufferable<FloatBuffer> {
	
	float matrix[];

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	public FloatBuffer fillBuffer(FloatBuffer buffer) {
		buffer.clear();
		buffer.put(matrix);
		buffer.flip();
		
		return buffer;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public final float[] get() {
		return matrix;
	}
	
	public final float get(int index) {
		return matrix[index];
	}
	
	
	public final void set(int index, float value) {
		matrix[index] = value;
	}
}