package rosick.jglsdk.glm;

import java.nio.FloatBuffer;

import rosick.PortingUtils.Bufferable;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public abstract class Mat implements Bufferable<FloatBuffer> {
	
	float matrix[];

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	public FloatBuffer fillAndFlipBuffer(FloatBuffer buffer) {
		buffer.clear();
		buffer.put(matrix);
		buffer.flip();
				
		return buffer;
	}

	@Override
	public FloatBuffer fillBuffer(FloatBuffer buffer) {	
		buffer.put(matrix);
						
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