package rosick.glm;


/**
 * @author integeruser
 */
public class Vec4 {
	
	public float x, y, z, w;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public Vec4(Vec3 vec3, float w) {
		x = vec3.x;
		y = vec3.y;
		z = vec3.z;
		this.w = w;
	}
}