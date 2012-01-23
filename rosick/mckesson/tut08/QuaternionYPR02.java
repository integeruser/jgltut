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
import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Quaternion;
import rosick.glm.Vec3;
import rosick.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 8. Getting Oriented 
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2008.html
 * @author integeruser
 */
public class QuaternionYPR02 extends GLWindow {
	
	public static void main(String[] args) {		
		new QuaternionYPR02().start(800, 800);
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/tut08/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif, baseColorUnif;
	private Mat4 cameraToClipMatrix = new Mat4();
	private FloatBuffer tempSharedBuffer = BufferUtils.createFloatBuffer(16);

	private MatrixStack currMatrix = new MatrixStack(); 

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();
		
		try {		
			g_pShip = new Mesh(BASEPATH + "Ship.xml");
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
		
		cameraToClipMatrix.put(0,	fFrustumScale);
		cameraToClipMatrix.put(5, 	fFrustumScale);
		cameraToClipMatrix.put(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.put(11, 	-1.0f);
		cameraToClipMatrix.put(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillBuffer(tempSharedBuffer));
		glUseProgram(0);
	}
	
	
	@Override
	protected void update() {
		lastFrameDuration *= 5 / 1000.0f;
	
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			offsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), (float) (SMALL_ANGLE_INCREMENT * lastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			offsetOrientation(new Vec3(0.0f, 0.0f, 1.0f), (float) (-SMALL_ANGLE_INCREMENT * lastFrameDuration));
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			offsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), (float) (SMALL_ANGLE_INCREMENT * lastFrameDuration)); 
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			offsetOrientation(new Vec3(1.0f, 0.0f, 0.0f), (float) (-SMALL_ANGLE_INCREMENT * lastFrameDuration));
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			offsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), (float) (SMALL_ANGLE_INCREMENT * lastFrameDuration));
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			offsetOrientation(new Vec3(0.0f, 1.0f, 0.0f), (float) (-SMALL_ANGLE_INCREMENT * lastFrameDuration));
		}

		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			leaveMainLoop();
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					g_bRightMultiply = !g_bRightMultiply;
					System.out.println(g_bRightMultiply ? "Right-multiply" : "Left-multiply");
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
		glUniformMatrix4(modelToCameraMatrixUnif, false, currMatrix.top().fillBuffer(tempSharedBuffer));

		g_pShip.render(/*"tint"*/);

		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		cameraToClipMatrix.put(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.put(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillBuffer(tempSharedBuffer));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private static boolean g_bRightMultiply = true;

	private final float SMALL_ANGLE_INCREMENT = 9.0f;

	private Mesh g_pShip;
	private Quaternion g_orientation = new Quaternion(1.0f, 0.0f, 0.0f, 0.0f);
	
	
	private void offsetOrientation(Vec3 _axis, float fAngDeg) {
		float fAngRad = Framework.degToRad(fAngDeg);

		Vec3 axis = Glm.normalize(_axis);
		axis = Vec3.scale(axis, (float) Math.sin(fAngRad / 2.0f));
		
		float scalar = (float) Math.cos(fAngRad / 2.0f);

		Quaternion offset = new Quaternion(scalar, axis.x, axis.y, axis.z);

		if(g_bRightMultiply)
			g_orientation = Quaternion.mul(g_orientation, offset);
		else
			g_orientation = Quaternion.mul(offset, g_orientation);

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