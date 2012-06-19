package rosick.mckesson.III.tut13;

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

import rosick.LWJGLWindow;
import rosick.jglsdk.BufferableData;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;
import rosick.mckesson.framework.Framework;
import rosick.mckesson.framework.Mesh;
import rosick.mckesson.framework.MousePole;
import rosick.mckesson.framework.Timer;


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
 * P		- toggle pausing on/off.
 * -,=		- rewind/jump forward time by 0.5 second (of real-time).
 * T		- toggle viewing the look-at point.
 * G		- toggle the drawing of the light source.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 */
public class GeomImpostor02 extends LWJGLWindow {
	
	public static void main(String[] args) {	
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/III/tut13/data/";

		new GeomImpostor02().start();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	protected void init() {
		initializePrograms();

		try {
			planeMesh =	new Mesh("LargePlane.xml");
			cubeMesh = 	new Mesh("UnitCube.xml");
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
		
		imposterVBO = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, imposterVBO);
		glBufferData(GL_ARRAY_BUFFER, NUMBER_OF_SPHERES * 4 * FLOAT_SIZE, GL_STREAM_DRAW);	
		
		imposterVAO = glGenVertexArrays();
		glBindVertexArray(imposterVAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 16, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 1, GL_FLOAT, false, 16, 12);
		
		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glEnable(GL_PROGRAM_POINT_SIZE);

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
		
		
		float lastFrameDuration = getLastFrameDuration() * 10 / 1000.0f;

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
			glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialTerrainUniformBuffer, 0, MaterialEntry.SIZE);

			Mat3 normMatrix = new Mat3(modelMatrix.top());
			normMatrix = Glm.transpose(Glm.inverse(normMatrix));

			glUseProgram(litMeshProg.theProgram);
			glUniformMatrix4(litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			glUniformMatrix3(litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));

			planeMesh.render();

			glUseProgram(0);
			glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
		}
		
		{
			VertexData[] posSizeArray = new VertexData[NUMBER_OF_SPHERES];

			posSizeArray[0] = new VertexData();
			posSizeArray[0].cameraPosition = new Vec3(Mat4.mul(worldToCamMat, new Vec4(0.0f, 10.0f, 0.0f, 1.0f)));
			posSizeArray[0].sphereRadius = 4.0f;

			posSizeArray[1] = new VertexData();
			posSizeArray[1].cameraPosition = getSphereOrbitPos(modelMatrix,
				new Vec3(0.0f, 10.0f, 0.0f), new Vec3(0.6f, 0.8f, 0.0f), 20.0f,
				sphereTimer.getAlpha());
			posSizeArray[1].sphereRadius = 2.0f;

			posSizeArray[2] = new VertexData();
			posSizeArray[2].cameraPosition = getSphereOrbitPos(modelMatrix,
				new Vec3(-10.0f, 1.0f, 0.0f), new Vec3(0.0f, 1.0f, 0.0f),
				10.0f, sphereTimer.getAlpha());
			posSizeArray[2].sphereRadius = 1.0f;

			posSizeArray[3] = new VertexData();
			posSizeArray[3].cameraPosition = getSphereOrbitPos(modelMatrix,
				new Vec3(10.0f, 1.0f, 0.0f), new Vec3(0.0f, 1.0f, 0.0f),
				10.0f, sphereTimer.getAlpha() * 2.0f);
			posSizeArray[3].sphereRadius = 1.0f;

			glBindBuffer(GL_ARRAY_BUFFER, imposterVBO);
			
			{				
				FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(NUMBER_OF_SPHERES * VertexData.SIZE / FLOAT_SIZE);
				
				for (VertexData vertexData : posSizeArray) {
					vertexData.fillBuffer(vertexDataBuffer);
				}
				
				vertexDataBuffer.flip();
				
				glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STREAM_DRAW);
			}
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
		
		{
			glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialArrayUniformBuffer, 
					0, MaterialEntry.SIZE * NUMBER_OF_SPHERES);

			glUseProgram(litImpProg.theProgram);
			glBindVertexArray(imposterVAO);
			glDrawArrays(GL_POINTS, 0, NUMBER_OF_SPHERES);
			glBindVertexArray(0);
			glUseProgram(0);

			glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
		}
		
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
	private int imposterVBO;
	private float zNear = 1.0f;
	private float zFar = 1000.0f;
	
	private FloatBuffer vec4Buffer 			= BufferUtils.createFloatBuffer(4);
	private FloatBuffer mat3Buffer 			= BufferUtils.createFloatBuffer(9);
	private FloatBuffer mat4Buffer 			= BufferUtils.createFloatBuffer(16);
	private FloatBuffer lightBlockBuffer	= BufferUtils.createFloatBuffer(24);
	
	
	private void initializePrograms() {
		litMeshProg = loadLitMeshProgram("PN.vert", "Lighting.frag");
		litImpProg = loadLitImposProgram("GeomImpostor.vert", "GeomImpostor.geom", "GeomImpostor.frag");
		unlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ProgramMeshData litMeshProg;
	private ProgramImposData litImpProg;
	private UnlitProgData unlit;
	
	
	private class ProgramMeshData {
		int theProgram;

		int modelToCameraMatrixUnif;
		int normalModelToCameraMatrixUnif;
	}
	
	private class ProgramImposData {
		int theProgram;
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
	
	private ProgramImposData loadLitImposProgram(String vertexShaderFilename, String geometryShaderFilename, String fragmentShaderFilename) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_GEOMETRY_SHADER, geometryShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		ProgramImposData data = new ProgramImposData();
		data.theProgram = Framework.createProgram(shaderList);

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
	
	private final int NUMBER_OF_SPHERES = 4;
	private final float halfLightDistance = 25.0f;
	private final float lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

	private Mesh planeMesh;
	private Mesh cubeMesh;
	
	private Timer sphereTimer = new Timer(Timer.Type.LOOP, 6.0f);

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
		
	private class VertexData extends BufferableData<FloatBuffer> {
		Vec3 cameraPosition;
		float sphereRadius;
		
		static final int SIZE = Vec3.SIZE + (1 * FLOAT_SIZE);

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			cameraPosition.fillBuffer(buffer);
			buffer.put(sphereRadius);
			
			return buffer;
		}
	}
	
		
	private Vec3 getSphereOrbitPos(MatrixStack modelMatrix, Vec3 orbitCenter, Vec3 orbitAxis, float orbitRadius, float orbitAlpha) {
		modelMatrix.push();
		
		modelMatrix.translate(orbitCenter);
		modelMatrix.rotate(orbitAxis, 360.0f * orbitAlpha);
	
		Vec3 offsetDir = Glm.cross(orbitAxis, new Vec3(0.0f, 1.0f, 0.0f));
		if (Glm.length(offsetDir) < 0.001f) {
			offsetDir = Glm.cross(orbitAxis, new Vec3(1.0f, 0.0f, 0.0f));
		}
		
		offsetDir = Glm.normalize(offsetDir);
		
		modelMatrix.translate(offsetDir.scale(orbitRadius));
	
		Vec3 res = new Vec3(Mat4.mul(modelMatrix.top(), new Vec4(0.0f, 0.0f, 0.0f, 1.0f)));
	
		modelMatrix.pop();
		
		return res;
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

		static final int SIZE = Vec4.SIZE + ((1 + 3) * FLOAT_SIZE) + PerLight.SIZE * NUMBER_OF_LIGHTS;

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
	
	private int materialArrayUniformBuffer;
	private int materialTerrainUniformBuffer;

	
	private class MaterialEntry extends BufferableData<FloatBuffer> {
		Vec4 diffuseColor;
		Vec4 specularColor;
		float specularShininess;
		float padding[] = new float[3];

		static final int SIZE = Vec4.SIZE + Vec4.SIZE + ((1 + 3) * FLOAT_SIZE);

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			diffuseColor.fillBuffer(buffer);
			specularColor.fillBuffer(buffer);
			buffer.put(specularShininess);
			buffer.put(padding);
			
			return buffer;
		}
	}


	private void createMaterials() {
		ArrayList<MaterialEntry> ubArray = new ArrayList<>();
		
		MaterialEntry matEntry = new MaterialEntry();
		matEntry.diffuseColor = new Vec4(0.1f, 0.1f, 0.8f, 1.0f);
		matEntry.specularColor = new Vec4(0.8f, 0.8f, 0.8f, 1.0f);
		matEntry.specularShininess = 0.1f;
		ubArray.add(matEntry);

		matEntry = new MaterialEntry();
		matEntry.diffuseColor = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);
		matEntry.specularColor = new Vec4(0.1f, 0.1f, 0.1f, 1.0f);
		matEntry.specularShininess = 0.8f;
		ubArray.add(matEntry);
		
		matEntry = new MaterialEntry();
		matEntry.diffuseColor = new Vec4(0.05f, 0.05f, 0.05f, 1.0f);
		matEntry.specularColor = new Vec4(0.95f, 0.95f, 0.95f, 1.0f);
		matEntry.specularShininess = 0.3f;
		ubArray.add(matEntry);

		matEntry = new MaterialEntry();
		matEntry.diffuseColor = new Vec4(0.803f, 0.709f, 0.15f, 1.0f);
		matEntry.specularColor = new Vec4(0.803f, 0.709f, 0.15f, 1.0f).scale(0.75f);
		matEntry.specularShininess = 0.18f;
		ubArray.add(matEntry);

		materialArrayUniformBuffer = glGenBuffers();
		materialTerrainUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, materialArrayUniformBuffer);
		
		{			
			FloatBuffer ubArrayBuffer = BufferUtils.createFloatBuffer(ubArray.size() * MaterialEntry.SIZE / FLOAT_SIZE);
			
			for (int i = 0; i < ubArray.size(); i++) {
				ubArray.get(i).fillBuffer(ubArrayBuffer);
			}
			
			ubArrayBuffer.flip();
			
			glBufferData(GL_UNIFORM_BUFFER, ubArrayBuffer, GL_STATIC_DRAW);
		}
		
		glBindBuffer(GL_UNIFORM_BUFFER, materialTerrainUniformBuffer);
		
		{
			matEntry = new MaterialEntry();
			matEntry.diffuseColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
			matEntry.specularColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
			matEntry.specularShininess = 0.6f;
			
			glBufferData(GL_UNIFORM_BUFFER, matEntry.fillAndFlipBuffer(BufferUtils.createFloatBuffer(MaterialEntry.SIZE / FLOAT_SIZE)), GL_STATIC_DRAW);
		}
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
}