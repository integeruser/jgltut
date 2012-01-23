package rosick.mckesson.tut08;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.framework.Mesh;
import rosick.glm.Vec4;
import rosick.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 8. Getting Oriented 
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2008.html
 * @author integeruser
 */
public class GimbalLock01 extends GLWindow {
	
	public static void main(String[] args) {		
		new GimbalLock01().start(800, 800);
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/tut08/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif, baseColorUnif;
	private FloatBuffer cameraToClipMatrixBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer tempSharedBuffer = BufferUtils.createFloatBuffer(16);

	private MatrixStack currMatrix = new MatrixStack(); 
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();
		
		try {
			for (int i = 0; i < 3; i++) {
				g_Gimbals[i] = new Mesh(g_strGimbalNames[i]);
			}
			
			g_pObject = new Mesh(BASEPATH + "Ship.xml");
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
	
	private void initializeProgram() {			
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		BASEPATH + "PosColorLocalTransform.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	BASEPATH + "ColorMultUniform.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = Framework.createProgram(shaderList);
		

		modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		baseColorUnif = glGetUniformLocation(theProgram, "baseColor");

		float fzNear = 1.0f; float fzFar = 600.0f;
		
		cameraToClipMatrixBuffer.put(0,		fFrustumScale);
		cameraToClipMatrixBuffer.put(5, 	fFrustumScale);
		cameraToClipMatrixBuffer.put(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrixBuffer.put(11, 	-1.0f);
		cameraToClipMatrixBuffer.put(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrixBuffer);
		glUseProgram(0);
	}
	
	
	@Override
	protected void update() {
		lastFrameDuration *= 5;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			g_angles.fAngleY += SMALL_ANGLE_INCREMENT * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			g_angles.fAngleY -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			g_angles.fAngleX += SMALL_ANGLE_INCREMENT * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			g_angles.fAngleX -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			g_angles.fAngleZ += SMALL_ANGLE_INCREMENT * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			g_angles.fAngleZ -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
		}

		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			leaveMainLoop();
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					g_bDrawGimbals = !g_bDrawGimbals;
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
		
		currMatrix.rotateX(g_angles.fAngleX);
		drawGimbal(currMatrix, GimbalAxis.GIMBAL_X_AXIS, new Vec4(0.4f, 0.4f, 1.0f, 1.0f));
		
		currMatrix.rotateY(g_angles.fAngleY);
		drawGimbal(currMatrix, GimbalAxis.GIMBAL_Y_AXIS, new Vec4(0.0f, 1.0f, 0.0f, 1.0f));
		
		currMatrix.rotateZ(g_angles.fAngleZ);
		drawGimbal(currMatrix, GimbalAxis.GIMBAL_Z_AXIS, new Vec4(1.0f, 0.3f, 0.3f, 1.0f));

		glUseProgram(theProgram);
		currMatrix.scale(3.0f, 3.0f, 3.0f);
		currMatrix.rotateX(-90);
		// Set the base color for this object.
		glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
		glUniformMatrix4(modelToCameraMatrixUnif, false, currMatrix.top().fillBuffer(tempSharedBuffer));

		g_pObject.render("tint");

		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		cameraToClipMatrixBuffer.put(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrixBuffer.put(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrixBuffer);
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float SMALL_ANGLE_INCREMENT = 9.0f;
	private final String g_strGimbalNames[] = {
		"/rosick/mckesson/tut08/data/LargeGimbal.xml",
		"/rosick/mckesson/tut08/data/MediumGimbal.xml",
		"/rosick/mckesson/tut08/data/SmallGimbal.xml",
	};
	
	private Mesh g_Gimbals[] = new Mesh[3];
	private Mesh g_pObject;
	private boolean g_bDrawGimbals = true;
	

	private enum GimbalAxis {
		GIMBAL_X_AXIS,
		GIMBAL_Y_AXIS,
		GIMBAL_Z_AXIS;
	};
	
	
	private void drawGimbal(MatrixStack currMatrix, GimbalAxis eAxis, Vec4 baseColor) {
		if(!g_bDrawGimbals)
			return;

		currMatrix.push();

		switch (eAxis) {
			case GIMBAL_X_AXIS:
				break;
			case GIMBAL_Y_AXIS:
				currMatrix.rotateZ(90.0f);
				currMatrix.rotateX(90.0f);
				break;
			case GIMBAL_Z_AXIS:
				currMatrix.rotateY(90.0f);
				currMatrix.rotateX(90.0f);
				break;
		}

		glUseProgram(theProgram);
		// Set the base color for this object.
		glUniform4f(baseColorUnif, baseColor.x, baseColor.y, baseColor.z, baseColor.w);
		glUniformMatrix4(modelToCameraMatrixUnif, false, currMatrix.top().fillBuffer(tempSharedBuffer));
		
		g_Gimbals[eAxis.ordinal()].render();

		glUseProgram(0);
		
		currMatrix.pop();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private GimbalAngles g_angles = new GimbalAngles();

	
	private class GimbalAngles {
		float fAngleX;
		float fAngleY;
		float fAngleZ;
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