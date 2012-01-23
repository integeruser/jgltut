package rosick.glutil.pole;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ViewScale {
	
	public float minRadius;        
	public float maxRadius;        
	public float largeRadiusDelta; 
	public float smallRadiusDelta; 
	public float largePosOffset;   
	public float smallPosOffset;   
	public float rotationScale;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public ViewScale(float minRadius, float maxRadius, float largeRadiusDelta,
			float smallRadiusDelta, float largePosOffset, float smallPosOffset,
			float rotationScale) {
		this.minRadius = minRadius;
		this.maxRadius = maxRadius;
		this.largeRadiusDelta = largeRadiusDelta;
		this.smallRadiusDelta = smallRadiusDelta;
		this.largePosOffset = largePosOffset;
		this.smallPosOffset = smallPosOffset;
		this.rotationScale = rotationScale;
	}
}