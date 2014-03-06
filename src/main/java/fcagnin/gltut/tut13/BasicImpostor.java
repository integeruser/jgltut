package fcagnin.gltut.tut13;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import fcagnin.LWJGLWindow;
import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat3;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Quaternion;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.*;
import fcagnin.gltut.framework.Framework;
import fcagnin.gltut.framework.Mesh;
import fcagnin.gltut.framework.MousePole;
import fcagnin.gltut.framework.Timer;
import fcagnin.gltut.framework.UniformBlockArray;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 13. Lies and Impostors
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2013.html
 * @author integeruser
 * 
 * W,A,S,D	- move the cameras forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.  
 * Q,E		- raise and lower the camera, relative to its current orientation. 
 * 				Holding SHIFT with these keys will move in smaller increments.  
 * P		- toggl pausing on/off.
 * -,=		- rewind/jump forward time by 0.5 second (of real-time).
 * T		- toggle viewing the look-at point.
 * G		- toggl the drawing of the light source.
 * 1		- switch back and forth between actual meshes and impostor spheres (the central blue sphere).
 * 2		- switch back and forth between actual meshes and impostor spheres (the orbiting grey sphere).
 * 3		- switch back and forth between actual meshes and impostor spheres (the black marble on the left).
 * 4		- switch back and forth between actual meshes and impostor spheres (the gold sphere on the right).
 * L,J,H	- switch impostor.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 */
public class BasicImpostor extends LWJGLWindow {
	
	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut13/data/";

		new BasicImpostor().start();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	protected void init() {
		initializePrograms();

		try {
			planeMesh =		new Mesh("LargePlane.xml");
			sphereMesh = 	new Mesh("UnitSphere.xml");
			cubeMesh = 		new Mesh("UnitCube.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
		}	
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
		
		final float depthZNear = 0.0f;
		final float depthZFar = 1.0f;

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(depthZNear, depthZFar);
		glEnable(GL_DEPTH_CLAMP);
		
		// Setup our Uniform Buffers
		lightUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		projectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 
				0, LightBlock.SIZE);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 
				0, ProjectionBlock.SIZE);

		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		// Empty Vertex Array Object.	
		imposterVAO = glGenVertexArrays();
		glBindVertexArray(imposterVAO);

		createMaterials();
	}
	

	@Override
	protected void update() {
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();
									
			if (eventButton != -1) {
				boolean pressed = Mouse.getEventButtonState();
				MousePole.forwardMouseButton(viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(viewPole, Mouse.getX(), Mouse.getY());			
				}
			}
		}
		
		
		float lastFrameDuration = getLastFrameDuration() * 10 / 1000.f;

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			viewPole.charPress(Keyboard.KEY_W, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			viewPole.charPress(Keyboard.KEY_S, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			viewPole.charPress(Keyboard.KEY_D, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			viewPole.charPress(Keyboard.KEY_A, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			viewPole.charPress(Keyboard.KEY_E, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			viewPole.charPress(Keyboard.KEY_Q, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_P:
					sphereTimer.togglePause();
					break;
					
				case Keyboard.KEY_MINUS:
					sphereTimer.rewind(0.5f);
					break;

				case Keyboard.KEY_EQUALS:
					sphereTimer.fastForward(0.5f);
					break;
					
				case Keyboard.KEY_T:
					drawCameraPos = !drawCameraPos;
					break;
					
				case Keyboard.KEY_G:
					drawLights = !drawLights;
					break;
					
				case Keyboard.KEY_1:
					drawImposter[0] = !drawImposter[0];
					break;
					
				case Keyboard.KEY_2:
					drawImposter[1] = !drawImposter[1];
					break;

				case Keyboard.KEY_3:
					drawImposter[2] = !drawImposter[2];
					break;
					
				case Keyboard.KEY_4:
					drawImposter[3] = !drawImposter[3];
					break;
					
				case Keyboard.KEY_L:
					currImpostor = Impostors.BASIC;
					break;
				
				case Keyboard.KEY_J:
					currImpostor = Impostors.PERSPECTIVE;
					break;
				
				case Keyboard.KEY_H:
					currImpostor = Impostors.DEPTH;
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
		sphereTimer.update(getElapsedTime());

		glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setMatrix(viewPole.calcMatrix());
		final Mat4 worldToCamMat = modelMatrix.top();

		LightBlock lightData = new LightBlock();
		lightData.ambientIntensity = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
		lightData.lightAttenuation = lightAttenuation;

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCamMat, new Vec4(0.707f, 0.707f, 0.0f, 0.0f));
		lightData.lights[0].lightIntensity = new Vec4(0.6f, 0.6f, 0.6f, 1.0f);

		lightData.lights[1] = new PerLight();
		lightData.lights[1].cameraSpaceLightPos = Mat4.mul(worldToCamMat, calcLightPosition());
		lightData.lights[1].lightIntensity = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);

		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer(lightBlockBuffer));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		{
			glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
				MaterialNames.TERRAIN.ordinal() * materialBlockOffset, MaterialBlock.SIZE);

			Mat3 normMatrix = new Mat3(modelMatrix.top());
			normMatrix = Glm.transpose(Glm.inverse(normMatrix));

			glUseProgram(litMeshProg.theProgram);
			glUniformMatrix4(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			glUniformMatrix3(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));

			planeMesh.render();

			glUseProgram(0);
			glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
		}

		drawSphere(modelMatrix, new Vec3(0.0f, 10.0f, 0.0f), 4.0f, MaterialNames.BLUE_SHINY, drawImposter[0]);
		drawSphereOrbit(modelMatrix, new Vec3(0.0f, 10.0f, 0.0f), new Vec3(0.6f, 0.8f, 0.0f), 20.0f, sphereTimer.getAlpha(), 2.0f, MaterialNames.DULL_GREY, drawImposter[1]);
		drawSphereOrbit(modelMatrix, new Vec3(-10.0f, 1.0f, 0.0f), new Vec3(0.0f, 1.0f, 0.0f), 10.0f, sphereTimer.getAlpha(), 1.0f, MaterialNames.BLACK_SHINY, drawImposter[2]);
		drawSphereOrbit(modelMatrix, new Vec3(10.0f, 1.0f, 0.0f), new Vec3(0.0f, 1.0f, 0.0f), 10.0f, sphereTimer.getAlpha() * 2.0f, 1.0f, MaterialNames.GOLD_METAL, drawImposter[3]);

		if (drawLights) {
			modelMatrix.push();
			
			modelMatrix.translate(new Vec3(calcLightPosition()));
			modelMatrix.scale(0.5f);

			glUseProgram(unlit.theProgram);
			glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

			Vec4 lightColor = new Vec4(1.0f);
			glUniform4(unlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
			cubeMesh.render("flat");
			
			modelMatrix.pop();
		}

		if (drawCameraPos) {
			modelMatrix.push();

			modelMatrix.setIdentity();
			modelMatrix.translate(new Vec3(0.0f, 0.0f, - viewPole.getView().radius));

			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			glUseProgram(unlit.theProgram);
			glUniformMatrix4(unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			glUniform4f(unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
			cubeMesh.render("flat");
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			cubeMesh.render("flat");
			
			modelMatrix.pop();
		}
	}
	
	
	@Override
	protected void reshape(int width, int height) {
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(45.0f, (width / (float) height), zNear, zFar);
		
		ProjectionBlock projData = new ProjectionBlock();
		projData.cameraToClipMatrix = persMatrix.top();

		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(mat4Buffer));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int imposterVAO;
	private float zNear = 1.0f;
	private float zFar = 1000.0f;
	
	private FloatBuffer vec4Buffer 			= BufferUtils.createFloatBuffer(Vec4.SIZE);
	private FloatBuffer mat3Buffer 			= BufferUtils.createFloatBuffer(Mat3.SIZE);
	private FloatBuffer mat4Buffer 			= BufferUtils.createFloatBuffer(Mat4.SIZE);
	private FloatBuffer lightBlockBuffer	= BufferUtils.createFloatBuffer(LightBlock.SIZE);
	
	
	private void initializePrograms() {
		litMeshProg = loadLitMeshProgram("PN.vert", "Lighting.frag");

		for (int progIndex = 0; progIndex < Impostors.NUM_IMPOSTORS.ordinal(); progIndex++) {
			litImpProgs[progIndex] = new ProgramImposData();
			litImpProgs[progIndex] = loadLitImposProgram(impShaderFilenames[progIndex * 2], impShaderFilenames[progIndex * 2 + 1]);
		}

		unlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private ProgramMeshData litMeshProg;
	private ProgramImposData[] litImpProgs = new ProgramImposData[Impostors.NUM_IMPOSTORS.ordinal()];	
	private UnlitProgData unlit;
	
	private String[] impShaderFilenames = new String[] {
		"BasicImpostor.vert", "BasicImpostor.frag",
		"PerspImpostor.vert", "PerspImpostor.frag",
		"DepthImpostor.vert", "DepthImpostor.frag"};
	
	
	private class ProgramMeshData {
		int theProgram;

		int modelToCameraMatrixUnif;
		int normalModelToCameraMatrixUnif;
	}
	
	private class ProgramImposData {
		int theProgram;

		int sphereRadiusUnif;
		int cameraSpherePosUnif;
	}
		
	private class UnlitProgData {
		int theProgram;

		int objectColorUnif;
		int modelToCameraMatrixUnif;
	}
			

	private ProgramMeshData loadLitMeshProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		ProgramMeshData data = new ProgramMeshData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");

		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");

		int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

		glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
		glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		return data;
	}
		
	private ProgramImposData loadLitImposProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		ProgramImposData data = new ProgramImposData();
		data.theProgram = Framework.createProgram(shaderList);
		data.sphereRadiusUnif = glGetUniformLocation(data.theProgram, "sphereRadius");
		data.cameraSpherePosUnif = glGetUniformLocation(data.theProgram, "cameraSpherePos");

		int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

		glUniformBlockBinding(data.theProgram, materialBlock, materialBlockIndex);
		glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		return data;
	}
	
	private UnlitProgData loadUnlitProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		UnlitProgData data = new UnlitProgData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.objectColorUnif = glGetUniformLocation(data.theProgram, "objectColor");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		return data;
	}
	


	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float halfLightDistance = 25.0f;
	private final float lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

	private Mesh planeMesh;
	private Mesh sphereMesh;
	private Mesh cubeMesh;
	
	private Timer sphereTimer = new Timer(Timer.Type.LOOP, 6.0f);
	
	private boolean[] drawImposter = {false, false, false, false};
	private boolean drawLights = true;
	private boolean drawCameraPos;
	private float lightHeight = 20.0f;

	
	////////////////////////////////
	// View setup.
	private ViewData initialViewData = new ViewData(
			new Vec3(0.0f, 30.0f, 25.0f),
			new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
			10.0f,
			0.0f);

	private ViewScale viewScale = new ViewScale(
			3.0f, 70.0f,
			3.5f, 1.5f,
			5.0f, 1.0f,
			90.0f / 250.0f);

	
	private ViewPole viewPole = new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Impostors currImpostor = Impostors.BASIC;

	
	private enum Impostors {
		BASIC,
	 	PERSPECTIVE,
	 	DEPTH,

		NUM_IMPOSTORS
	};
	
		
	private void drawSphere(MatrixStack modelMatrix, Vec3 position, float radius, MaterialNames material, boolean drawImposter) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, 
				material.ordinal() * materialBlockOffset, MaterialBlock.SIZE);

		if (drawImposter) {
			Vec4 cameraSpherePos = Mat4.mul(modelMatrix.top(), new Vec4(position, 1.0f));
			glUseProgram(litImpProgs[currImpostor.ordinal()].theProgram);
			glUniform3(litImpProgs[currImpostor.ordinal()].cameraSpherePosUnif, cameraSpherePos.fillAndFlipBuffer(vec4Buffer));
			glUniform1f(litImpProgs[currImpostor.ordinal()].sphereRadiusUnif, radius);
		
			glBindVertexArray(imposterVAO);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 1, GL_FLOAT, false, 0, 0);
			
			glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		
			glBindVertexArray(0);
			glUseProgram(0);
		} else {
			modelMatrix.push();
			
			modelMatrix.translate(position);
			modelMatrix.scale(radius * 2.0f);					// The unit sphere has a radius 0.5f
		
			Mat3 normMatrix = new Mat3(modelMatrix.top());
			normMatrix = Glm.transpose(Glm.inverse(normMatrix));
		
			glUseProgram(litMeshProg.theProgram);
			glUniformMatrix4(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			glUniformMatrix3(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
		
			sphereMesh.render("lit");
		
			glUseProgram(0);
			
			modelMatrix.pop();
		}
		
		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}

	private void drawSphereOrbit(MatrixStack modelMatrix, Vec3 orbitCenter, Vec3 orbitAxis, 
			float orbitRadius, float orbitAlpha, float sphereRadius, MaterialNames material, boolean drawImposter) {
		modelMatrix.push();
		
		modelMatrix.translate(orbitCenter);
		modelMatrix.rotate(orbitAxis, 360.0f * orbitAlpha);
		
		Vec3 offsetDir = Glm.cross(orbitAxis, new Vec3(0.0f, 1.0f, 0.0f));
		if (Glm.length(offsetDir) < 0.001f) {
			offsetDir = Glm.cross(orbitAxis, new Vec3(1.0f, 0.0f, 0.0f));
		}
		
		offsetDir = Glm.normalize(offsetDir);
		
		modelMatrix.translate(offsetDir.scale(orbitRadius));
		
		drawSphere(modelMatrix, new Vec3(0.0f), sphereRadius, material, drawImposter);
		
		modelMatrix.pop();
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int projectionBlockIndex = 2;

	private int projectionUniformBuffer;

		
	private class ProjectionBlock extends BufferableData<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = Mat4.SIZE;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
		
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static final int NUMBER_OF_LIGHTS = 2;

	private final int lightBlockIndex = 1;

	private int lightUniformBuffer;

	
	class PerLight extends BufferableData<FloatBuffer> {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;
		
		static final int SIZE = Vec4.SIZE + Vec4.SIZE;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			cameraSpaceLightPos.fillBuffer(buffer);
			lightIntensity.fillBuffer(buffer);

			return buffer;
		}
	}
	
	
	class LightBlock extends BufferableData<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float padding[] = new float[3];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = Vec4.SIZE + ((1 + 3) * (Float.SIZE / Byte.SIZE)) + PerLight.SIZE * NUMBER_OF_LIGHTS;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {			
			ambientIntensity.fillBuffer(buffer);
			buffer.put(lightAttenuation);
			buffer.put(padding);
			
			for (PerLight light : lights) {
				light.fillBuffer(buffer);
			}
			
			return buffer;
		}
	}
	
	
	private Vec4 calcLightPosition() {
		final float scale = 3.14159f * 2.0f;
		float timeThroughLoop = sphereTimer.getAlpha();

		Vec4 lightPos = new Vec4(0.0f, lightHeight, 0.0f, 1.0f);

		lightPos.x = (float) (Math.cos(timeThroughLoop * scale) * 20.0f);
		lightPos.z = (float) (Math.sin(timeThroughLoop * scale) * 20.0f);

		return lightPos;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int materialBlockIndex = 0;
	
	private int materialUniformBuffer;
	private int materialBlockOffset;

	
	private class MaterialBlock extends BufferableData<ByteBuffer> {
		Vec4 diffuseColor;
		Vec4 specularColor;
		float specularShininess;
		float padding[] = new float[3];

		static final int SIZE = Vec4.SIZE + Vec4.SIZE + ((1 + 3) * (Float.SIZE / Byte.SIZE));

		@Override
		public ByteBuffer fillBuffer(ByteBuffer buffer) {
			buffer.putFloat(diffuseColor.x);
			buffer.putFloat(diffuseColor.y);
			buffer.putFloat(diffuseColor.z);
			buffer.putFloat(diffuseColor.w);

			buffer.putFloat(specularColor.x);
			buffer.putFloat(specularColor.y);
			buffer.putFloat(specularColor.z);
			buffer.putFloat(specularColor.w);

			buffer.putFloat(specularShininess);
			
			for (int i = 0; i < 3; i++) {
				buffer.putFloat(padding[i]);
			}
			
			return buffer;
		}
	}


	private enum MaterialNames {
		TERRAIN,
		BLUE_SHINY,
		GOLD_METAL,
		DULL_GREY,
		BLACK_SHINY,

		NUM_MATERIALS
	};

	
	private void createMaterials() {
		UniformBlockArray<MaterialBlock> ubArray = new UniformBlockArray<>(MaterialBlock.SIZE, MaterialNames.NUM_MATERIALS.ordinal());
		materialBlockOffset = ubArray.getArrayOffset();

		MaterialBlock matBlock = new MaterialBlock();
		matBlock.diffuseColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
		matBlock.specularColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
		matBlock.specularShininess = 0.6f;
		ubArray.set(MaterialNames.TERRAIN.ordinal(), matBlock);

		matBlock.diffuseColor = new Vec4(0.1f, 0.1f, 0.8f, 1.0f);
		matBlock.specularColor = new Vec4(0.8f, 0.8f, 0.8f, 1.0f);
		matBlock.specularShininess = 0.1f;
		ubArray.set(MaterialNames.BLUE_SHINY.ordinal(), matBlock);

		matBlock.diffuseColor = new Vec4(0.803f, 0.709f, 0.15f, 1.0f);
		matBlock.specularColor = new Vec4(0.803f, 0.709f, 0.15f, 1.0f).scale(0.75f);
		matBlock.specularShininess = 0.18f;
		ubArray.set(MaterialNames.GOLD_METAL.ordinal(), matBlock);

		matBlock.diffuseColor = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);
		matBlock.specularColor = new Vec4(0.1f, 0.1f, 0.1f, 1.0f);
		matBlock.specularShininess = 0.8f;
		ubArray.set(MaterialNames.DULL_GREY.ordinal(), matBlock);

		matBlock.diffuseColor = new Vec4(0.05f, 0.05f, 0.05f, 1.0f);
		matBlock.specularColor = new Vec4(0.95f, 0.95f, 0.95f, 1.0f);
		matBlock.specularShininess = 0.3f;
		ubArray.set(MaterialNames.BLACK_SHINY.ordinal(), matBlock);

		materialUniformBuffer = ubArray.createBufferObject();
	}
}