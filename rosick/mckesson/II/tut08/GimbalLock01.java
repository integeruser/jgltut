package rosick.mckesson.II.tut08;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.LWJGLWindow;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.mckesson.framework.Framework;
import rosick.mckesson.framework.Mesh;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 8. Getting Oriented 
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2008.html
 * @author integeruser
 * 
 * SPACE	- toggle drawing the gimbal rings.
 * W,S		- control the outer gimbal.
 * A,D 		- control the middle gimbal.
 * Q,E  	- control the inner gimbal.
 */
public class GimbalLock01 extends LWJGLWindow {
	
	public static void main(String[] args) {	
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/II/tut08/data/";

		new GimbalLock01().start();
	}
		
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	@Override
	protected void init() {
		initializeProgram();
		
		try {
			for (int gimbalIndex = 0; gimbalIndex < 3; gimbalIndex++) {
				gimbals[gimbalIndex] = new Mesh(strGimbalNames[gimbalIndex]);
			}
			
			object = new Mesh("Ship.xml");
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
		float lastFrameDuration = getLastFrameDuration() * 10 / 1000.0f;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			angles.angleX += SMALL_ANGLE_INCREMENT * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			angles.angleX -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			angles.angleY += SMALL_ANGLE_INCREMENT * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			angles.angleY -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			angles.angleZ += SMALL_ANGLE_INCREMENT * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			angles.angleZ -= SMALL_ANGLE_INCREMENT * lastFrameDuration;
		}

		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					drawGimbals = !drawGimbals;
					break;
					
				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}
			}
		}
	}
	

	@Override
	protected void display() {	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		MatrixStack currMatrix = new MatrixStack();
		currMatrix.translate(0.0f, 0.0f, -200.0f);
		currMatrix.rotateX(angles.angleX);
		drawGimbal(currMatrix, GimbalAxis.X_AXIS, new Vec4(0.4f, 0.4f, 1.0f, 1.0f));
		currMatrix.rotateY(angles.angleY);
		drawGimbal(currMatrix, GimbalAxis.Y_AXIS, new Vec4(0.0f, 1.0f, 0.0f, 1.0f));
		currMatrix.rotateZ(angles.angleZ);
		drawGimbal(currMatrix, GimbalAxis.Z_AXIS, new Vec4(1.0f, 0.3f, 0.3f, 1.0f));

		glUseProgram(theProgram);
		currMatrix.scale(3.0f, 3.0f, 3.0f);
		currMatrix.rotateX(-90.0f);
		// Set the base color for this object.
		glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
		glUniformMatrix4(modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer(mat4Buffer));

		object.render("tint");

		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		cameraToClipMatrix.set(0, 0, frustumScale / (width / (float) height));
		cameraToClipMatrix.set(1, 1, frustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif, baseColorUnif;
	
	private Mat4 cameraToClipMatrix = new Mat4(0.0f);
	
	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
	
	
	private void initializeProgram() {			
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	"PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorMultUniform.frag"));

		theProgram = Framework.createProgram(shaderList);
		
		modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		baseColorUnif = glGetUniformLocation(theProgram, "baseColor");

		float zNear = 1.0f; float zFar = 600.0f;
		
		cameraToClipMatrix.set(0, 0, 	frustumScale);
		cameraToClipMatrix.set(1, 1, 	frustumScale);
		cameraToClipMatrix.set(2, 2,	(zFar + zNear) / (zNear - zFar));
		cameraToClipMatrix.set(2, 3,	-1.0f);
		cameraToClipMatrix.set(3, 2,	(2 * zFar * zNear) / (zNear - zFar));
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float SMALL_ANGLE_INCREMENT = 9.0f;
	private final String strGimbalNames[] = {
			"LargeGimbal.xml",
			"MediumGimbal.xml",
			"SmallGimbal.xml"};
	
	private GimbalAngles angles = new GimbalAngles();

	private Mesh gimbals[] = new Mesh[3];
	private Mesh object;
	
	private boolean drawGimbals = true;
	
	
	private enum GimbalAxis {
		X_AXIS,
		Y_AXIS,
		Z_AXIS
	}
	
	
	private class GimbalAngles {
		float angleX;
		float angleY;
		float angleZ;
	}
	
		
	private void drawGimbal(MatrixStack currMatrix, GimbalAxis axis, Vec4 baseColor) {
		if (!drawGimbals) {
			return;
		}

		currMatrix.push();

		switch (axis) {
		case X_AXIS:
			break;
			
		case Y_AXIS:
			currMatrix.rotateZ(90.0f);
			currMatrix.rotateX(90.0f);
			break;
			
		case Z_AXIS:
			currMatrix.rotateY(90.0f);
			currMatrix.rotateX(90.0f);
			break;
		}

		glUseProgram(theProgram);
		// Set the base color for this object.
		glUniform4f(baseColorUnif, baseColor.x, baseColor.y, baseColor.z, baseColor.w);
		glUniformMatrix4(modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer(mat4Buffer));
		
		gimbals[axis.ordinal()].render();

		glUseProgram(0);
		
		currMatrix.pop();
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float frustumScale = calcFrustumScale(20.0f);

	
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fovRad = fFovDeg * degToRad;
		
		return (float) (1.0f / Math.tan(fovRad / 2.0f));
	}
}