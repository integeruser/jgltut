package rosick.glm;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser, xire-
 */
public abstract class Mat {
	
	float matrix[];

	
	
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