package rosick.mckesson.II.tut08;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.GLWindow;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 8. Getting Oriented 
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2008.html
 * @author integeruser
 * 
 * SPACE	- switches between right-multiplying the YPR values to the current orientation and left-multiplying them. 
 * W,S		- control the outer gimbal.
 * A,D 		- control the middle gimbal.
 * Q,E  	- control the inner gimbal.
 */
public class QuaternionYPR02 extends GLWindow {
	
	public static void main(String[] args) {		
		new QuaternionYPR02().start();
	}
	
	
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/II/tut08/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif, baseColorUnif;
	
	private MatrixStack currMatrix = new MatrixStack(); 

	private Mat4 cameraToClipMatrix = new Mat4();
	
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);


		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void initializeProgram() {			
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	TUTORIAL_DATAPATH + "PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, TUTORIAL_DATAPATH + "ColorMultUniform.frag"));

		theProgram = Framework.createProgram(shaderList);
		
		modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		baseColorUnif = glGetUniformLocation(theProgram, "baseColor");

		float fzNear = 1.0f; float fzFar = 600.0f;
		
		cameraToClipMatrix.set(0,	fFrustumScale);
		cameraToClipMatrix.set(5, 	fFrustumScale);
		cameraToClipMatrix.set(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.set(11, 	-1.0f);
		cameraToClipMatrix.set(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		
		try {		
			g_pShip = new Mesh(TUTORIAL_DATAPATH + "Ship.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}		
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
	}
	
	
	@Override
	protected void update() {
		float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);
	
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			offsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), (float) (SMALL_ANGLE_INCREMENT * lastFrameDuration)); 
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			offsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), (float) (-SMALL_ANGLE_INCREMENT * lastFrameDuration));
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			offsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), (float) (SMALL_ANGLE_INCREMENT * lastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			offsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), (float) (-SMALL_ANGLE_INCREMENT * lastFrameDuration));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			offsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), (float) (SMALL_ANGLE_INCREMENT * lastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			offsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), (float) (-SMALL_ANGLE_INCREMENT * lastFrameDuration));
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					g_bRightMultiply = !g_bRightMultiply;
					System.out.printf(g_bRightMultiply ? "Right-multiply\n" : "Left-multiply\n");
				}
				
				
				else if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
					leaveMainLoop();
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		currMatrix.clear();
		currMatrix.translate(0.0f, 0.0f, -200.0f);
		currMatrix.applyMatrix(Glm.matCast(g_orientation));

		glUseProgram(theProgram);
		currMatrix.scale(3.0f, 3.0f, 3.0f);
		currMatrix.rotateX(-90.0f);
		// Set the base color for this object.
		glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
		glUniformMatrix4(modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

		g_pShip.render("tint");

		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		cameraToClipMatrix.set(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.set(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final float SMALL_ANGLE_INCREMENT = 9.0f;

	private Mesh g_pShip;
	private Quaternion g_orientation = new Quaternion(1.0f, 0.0f, 0.0f, 0.0f);

	private boolean g_bRightMultiply = true;

	
	private void offsetOrientation(Vec3 _axis, float fAngDeg) {
		float fAngRad = Framework.degToRad(fAngDeg);

		Vec3 axis = Glm.normalize(_axis);
		axis = Vec3.scale(axis, (float) Math.sin(fAngRad / 2.0f));
		
		float scalar = (float) Math.cos(fAngRad / 2.0f);

		Quaternion offset = new Quaternion(scalar, axis.x, axis.y, axis.z);

		if (g_bRightMultiply) {
			g_orientation = Quaternion.mul(g_orientation, offset);
		} else {
			g_orientation = Quaternion.mul(offset, g_orientation);
		}
		
		g_orientation = Glm.normalize(g_orientation);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float fFrustumScale = calcFrustumScale(20.0f);

	
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fFovRad = fFovDeg * degToRad;
		
		return (float) (1.0f / Math.tan(fFovRad / 2.0f));
	}
}