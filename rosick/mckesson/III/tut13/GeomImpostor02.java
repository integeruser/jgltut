package rosick.mckesson.III.tut13;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import rosick.GLWindow;
import rosick.PortingUtils.BufferableData;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.framework.Timer;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.pole.MousePole.*;
import rosick.jglsdk.glutil.pole.ViewPole;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 13. Lies and Impostors
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2013.html
 * @author integeruser
 * 
 * W,A,S,D	- move the cameras forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding LEFT_SHIFT with these keys will move in smaller increments.  
 * Q,E		- raise and lower the camera, relative to its current orientation. 
 * 				Holding LEFT_SHIFT with these keys will move in smaller increments.  
 * P		- toggles pausing on/off.
 * -,=		- rewind/jump forward time by 0.5 second (of real-time).
 * T		- toggles a display showing the look-at point.
 * G		- toggles the drawing of the light source.
 * 
 * LEFT	  CLICKING and DRAGGING				- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_ALT	- change the camera's up direction.
 * WHEEL  SCROLLING							- move the camera closer to it's target point or farther away. 
 */
public class GeomImpostor02 extends GLWindow {
	
	public static void main(String[] args) {		
		new GeomImpostor02().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String COMMON_DATAPATH = "/rosick/mckesson/data/";
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/III/tut13/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
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
	
			
	private final int g_materialBlockIndex = 0;
	private final int g_lightBlockIndex = 1;
	private final int g_projectionBlockIndex = 2;

	private ProgramMeshData g_litMeshProg;
	private ProgramImposData g_litImpProg;
	private UnlitProgData g_Unlit;

	private int g_lightUniformBuffer;
	private int g_projectionUniformBuffer;
	private int g_materialArrayUniformBuffer;
	private int g_materialTerrainUniformBuffer;
	private int g_imposterVAO;
	private int g_imposterVBO;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer9 	= BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempFloatBuffer16 	= BufferUtils.createFloatBuffer(16);
	private FloatBuffer tempFloatBuffer24 	= BufferUtils.createFloatBuffer(24);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private UnlitProgData loadUnlitProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		UnlitProgData data = new UnlitProgData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.objectColorUnif = glGetUniformLocation(data.theProgram, "objectColor");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		return data;
	}
	
	private ProgramMeshData loadLitMeshProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramMeshData data = new ProgramMeshData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");

		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");

		int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

		glUniformBlockBinding(data.theProgram, materialBlock, g_materialBlockIndex);
		glUniformBlockBinding(data.theProgram, lightBlock, g_lightBlockIndex);
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		return data;
	}
	
	private ProgramImposData loadLitImposProgram(String strVertexShader, String strGeometryShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_GEOMETRY_SHADER, strGeometryShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramImposData data = new ProgramImposData();
		data.theProgram = Framework.createProgram(shaderList);

		int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

		glUniformBlockBinding(data.theProgram, materialBlock, g_materialBlockIndex);
		glUniformBlockBinding(data.theProgram, lightBlock, g_lightBlockIndex);
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		return data;
	}
	
	private void initializePrograms() {	
		g_litMeshProg = loadLitMeshProgram(TUTORIAL_DATAPATH + "PN.vert", TUTORIAL_DATAPATH + "Lighting.frag");

		g_litImpProg = loadLitImposProgram(TUTORIAL_DATAPATH + "GeomImpostor.vert", TUTORIAL_DATAPATH + "GeomImpostor.geom", TUTORIAL_DATAPATH + "GeomImpostor.frag");

		g_Unlit = loadUnlitProgram(COMMON_DATAPATH + "Unlit.vert", COMMON_DATAPATH + "Unlit.frag");
	}
	
	
	@Override
	protected void init() {
		initializePrograms();

		try {
			g_pPlaneMesh = 	new Mesh(TUTORIAL_DATAPATH + "LargePlane.xml");
			g_pCubeMesh = 	new Mesh(TUTORIAL_DATAPATH + "UnitCube.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
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
		g_lightUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		g_projectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, g_lightBlockIndex, g_lightUniformBuffer, 0, LightBlock.SIZE);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer, 0, ProjectionBlock.SIZE);

		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		g_imposterVBO = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, g_imposterVBO);
		glBufferData(GL_ARRAY_BUFFER, NUMBER_OF_SPHERES * 4 * FLOAT_SIZE, GL_STREAM_DRAW);	
		
		g_imposterVAO = glGenVertexArrays();
		glBindVertexArray(g_imposterVAO);
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
				if (Mouse.getEventButtonState()) {
					// Mouse down
					Framework.forwardMouseButton(g_viewPole, eventButton, true, Mouse.getX(), Mouse.getY());			
				} else {
					// Mouse up
					Framework.forwardMouseButton(g_viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					Framework.forwardMouseWheel(g_viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					Framework.forwardMouseMotion(g_viewPole, Mouse.getX(), Mouse.getY());			
				}
			}
		}
		
		
		float lastFrameDuration = (float) (getLastFrameDuration() / 100.0);

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			g_viewPole.charPress(Keyboard.KEY_W, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			g_viewPole.charPress(Keyboard.KEY_S, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), lastFrameDuration);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			g_viewPole.charPress(Keyboard.KEY_D, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			g_viewPole.charPress(Keyboard.KEY_A, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			g_viewPole.charPress(Keyboard.KEY_E, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			g_viewPole.charPress(Keyboard.KEY_Q, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), lastFrameDuration);
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					g_sphereTimer.togglePause();
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_MINUS) {
					g_sphereTimer.rewind(0.5f);
										
				} else if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS) {
					g_sphereTimer.fastForward(0.5f);
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
					g_bDrawCameraPos = !g_bDrawCameraPos;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_G) {
					g_bDrawLights = !g_bDrawLights;
					
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					leaveMainLoop();
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		g_sphereTimer.update((float) getElapsedTime());

		glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		modelMatrix.clear();
		modelMatrix.setMatrix(g_viewPole.calcMatrix());
		final Mat4 worldToCamMat = modelMatrix.top();

		LightBlock lightData = new LightBlock();

		lightData.ambientIntensity = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
		lightData.lightAttenuation = g_fLightAttenuation;

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCamMat, new Vec4(0.707f, 0.707f, 0.0f, 0.0f));
		lightData.lights[0].lightIntensity = new Vec4(0.6f, 0.6f, 0.6f, 1.0f);

		lightData.lights[1] = new PerLight();
		lightData.lights[1].cameraSpaceLightPos = Mat4.mul(worldToCamMat, calcLightPosition());
		lightData.lights[1].lightIntensity = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);

		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer(tempFloatBuffer24));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		{
			glBindBufferRange(GL_UNIFORM_BUFFER, g_materialBlockIndex, g_materialTerrainUniformBuffer, 0, MaterialEntry.SIZE);

			Mat3 normMatrix = new Mat3(modelMatrix.top());
			normMatrix = Glm.transpose(Glm.inverse(normMatrix));

			glUseProgram(g_litMeshProg.theProgram);
			glUniformMatrix4(g_litMeshProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniformMatrix3(g_litMeshProg.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));

			g_pPlaneMesh.render();

			glUseProgram(0);
			glBindBufferBase(GL_UNIFORM_BUFFER, g_materialBlockIndex, 0);
		}
		
		{
			VertexData posSizeArray[] = new VertexData[NUMBER_OF_SPHERES];

			posSizeArray[0] = new VertexData();
			posSizeArray[0].cameraPosition = new Vec3(Mat4.mul(worldToCamMat, new Vec4(0.0f, 10.0f, 0.0f, 1.0f)));
			posSizeArray[0].sphereRadius = 4.0f;

			posSizeArray[1] = new VertexData();
			posSizeArray[1].cameraPosition = getSphereOrbitPos(modelMatrix,
				new Vec3(0.0f, 10.0f, 0.0f), new Vec3(0.6f, 0.8f, 0.0f), 20.0f,
				g_sphereTimer.getAlpha());
			posSizeArray[1].sphereRadius = 2.0f;

			posSizeArray[2] = new VertexData();
			posSizeArray[2].cameraPosition = getSphereOrbitPos(modelMatrix,
				new Vec3(-10.0f, 1.0f, 0.0f), new Vec3(0.0f, 1.0f, 0.0f),
				10.0f, g_sphereTimer.getAlpha());
			posSizeArray[2].sphereRadius = 1.0f;

			posSizeArray[3] = new VertexData();
			posSizeArray[3].cameraPosition = getSphereOrbitPos(modelMatrix,
				new Vec3(10.0f, 1.0f, 0.0f), new Vec3(0.0f, 1.0f, 0.0f),
				10.0f, g_sphereTimer.getAlpha() * 2.0f);
			posSizeArray[3].sphereRadius = 1.0f;

			glBindBuffer(GL_ARRAY_BUFFER, g_imposterVBO);
			
			{				
				FloatBuffer tempFloatBuffer = BufferUtils.createFloatBuffer(NUMBER_OF_SPHERES * VertexData.SIZE / FLOAT_SIZE);
				
				for (VertexData vertexData : posSizeArray) {
					vertexData.fillBuffer(tempFloatBuffer);
				}
				
				tempFloatBuffer.flip();
				
				glBufferData(GL_ARRAY_BUFFER, tempFloatBuffer, GL_STREAM_DRAW);
			}
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
		
		{
			glBindBufferRange(GL_UNIFORM_BUFFER, g_materialBlockIndex, g_materialArrayUniformBuffer, 0, MaterialEntry.SIZE * NUMBER_OF_SPHERES);

			glUseProgram(g_litImpProg.theProgram);
			glBindVertexArray(g_imposterVAO);
			glDrawArrays(GL_POINTS, 0, NUMBER_OF_SPHERES);
			glBindVertexArray(0);
			glUseProgram(0);

			glBindBufferBase(GL_UNIFORM_BUFFER, g_materialBlockIndex, 0);
		}
		
		if (g_bDrawLights) {
			modelMatrix.push();
			
			modelMatrix.translate(new Vec3(calcLightPosition()));
			modelMatrix.scale(0.5f);

			glUseProgram(g_Unlit.theProgram);
			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

			Vec4 lightColor = new Vec4(1.0f);
			glUniform4(g_Unlit.objectColorUnif, lightColor.fillAndFlipBuffer(tempFloatBuffer4));
			g_pCubeMesh.render("flat");
			
			modelMatrix.pop();
		}

		if (g_bDrawCameraPos) {
			modelMatrix.push();

			modelMatrix.setIdentity();
			modelMatrix.translate(new Vec3(0.0f, 0.0f, - g_viewPole.getView().radius));

			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			glUseProgram(g_Unlit.theProgram);
			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(g_Unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
			g_pCubeMesh.render("flat");
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(g_Unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			g_pCubeMesh.render("flat");
			
			modelMatrix.pop();
		}
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(45.0f, (width / (float) height), g_fzNear, g_fzFar);
		
		ProjectionBlock projData = new ProjectionBlock();
		projData.cameraToClipMatrix = persMatrix.top();

		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(tempFloatBuffer16));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private class PerLight extends BufferableData<FloatBuffer> {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			cameraSpaceLightPos.fillBuffer(buffer);
			lightIntensity.fillBuffer(buffer);

			return buffer;
		}
	}
	
	private class LightBlock extends BufferableData<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float padding[] = new float[3];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = (4 + 1 + 3 + (8 * 4)) * FLOAT_SIZE;

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
	
	
	private class ProjectionBlock extends BufferableData<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = 16 * FLOAT_SIZE;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
			
	
	private final int NUMBER_OF_LIGHTS = 2;
	private final int NUMBER_OF_SPHERES = 4;
	private final float g_fHalfLightDistance = 25.0f;
	private final float g_fLightAttenuation = 1.0f / (g_fHalfLightDistance * g_fHalfLightDistance);

	private Mesh g_pPlaneMesh;
	private Mesh g_pCubeMesh;
	
	private Timer g_sphereTimer = new Timer(Timer.Type.TT_LOOP, 6.0f);

	private boolean g_bDrawLights = true;
	private boolean g_bDrawCameraPos;
	private float g_lightHeight = 20.0f;
	
	
	// View/Object Setup
	
	private ViewData g_initialViewData = new ViewData(
			new Vec3(0.0f, 30.0f, 25.0f),
			new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
			10.0f,
			0.0f
	);

	private ViewScale g_viewScale = new ViewScale(	
			3.0f, 70.0f,
			3.5f, 1.5f,
			5.0f, 1.0f,
			90.0f / 250.0f
	);

	private ViewPole g_viewPole = new ViewPole(g_initialViewData, g_viewScale, MouseButtons.MB_LEFT_BTN);
		
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class VertexData extends BufferableData<FloatBuffer> {
		Vec3 cameraPosition;
		float sphereRadius;
		
		static final int SIZE = 4 * FLOAT_SIZE;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			cameraPosition.fillBuffer(buffer);
			buffer.put(sphereRadius);
			
			return buffer;
		}
	}
	
	
	private class MaterialEntry extends BufferableData<FloatBuffer> {
		Vec4 diffuseColor;
		Vec4 specularColor;
		float specularShininess;
		float padding[] = new float[3];

		static final int SIZE = (4 + 4 + 1 + 3) * FLOAT_SIZE;

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
		
		MaterialEntry mtl = new MaterialEntry();
		mtl.diffuseColor = new Vec4(0.1f, 0.1f, 0.8f, 1.0f);
		mtl.specularColor = new Vec4(0.8f, 0.8f, 0.8f, 1.0f);
		mtl.specularShininess = 0.1f;
		ubArray.add(mtl);

		mtl = new MaterialEntry();
		mtl.diffuseColor = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);
		mtl.specularColor = new Vec4(0.1f, 0.1f, 0.1f, 1.0f);
		mtl.specularShininess = 0.8f;
		ubArray.add(mtl);
		
		mtl = new MaterialEntry();
		mtl.diffuseColor = new Vec4(0.05f, 0.05f, 0.05f, 1.0f);
		mtl.specularColor = new Vec4(0.95f, 0.95f, 0.95f, 1.0f);
		mtl.specularShininess = 0.3f;
		ubArray.add(mtl);

		mtl = new MaterialEntry();
		mtl.diffuseColor = new Vec4(0.803f, 0.709f, 0.15f, 1.0f);
		mtl.specularColor = new Vec4(0.803f, 0.709f, 0.15f, 1.0f).scale(0.75f);
		mtl.specularShininess = 0.18f;
		ubArray.add(mtl);

		g_materialArrayUniformBuffer = glGenBuffers();
		g_materialTerrainUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, g_materialArrayUniformBuffer);
		
		{			
			FloatBuffer ubArrayBuffer = BufferUtils.createFloatBuffer(ubArray.size() * MaterialEntry.SIZE / FLOAT_SIZE);
			
			for (int i = 0; i < ubArray.size(); i++) {
				ubArray.get(i).fillBuffer(ubArrayBuffer);
			}
			
			ubArrayBuffer.flip();
			
			glBufferData(GL_UNIFORM_BUFFER, ubArrayBuffer, GL_STATIC_DRAW);
		}
		
		glBindBuffer(GL_UNIFORM_BUFFER, g_materialTerrainUniformBuffer);
		
		{
			mtl = new MaterialEntry();
			mtl.diffuseColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
			mtl.specularColor = new Vec4(0.5f, 0.5f, 0.5f, 1.0f);
			mtl.specularShininess = 0.6f;
			
			glBufferData(GL_UNIFORM_BUFFER, mtl.fillAndFlipBuffer(BufferUtils.createFloatBuffer(MaterialEntry.SIZE / FLOAT_SIZE)), GL_STATIC_DRAW);
		}
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
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
			
	
	private Vec4 calcLightPosition() {
		final float fScale = 3.14159f * 2.0f;

		float timeThroughLoop = g_sphereTimer.getAlpha();

		Vec4 ret = new Vec4(0.0f, g_lightHeight, 0.0f, 1.0f);

		ret.x = (float) (Math.cos(timeThroughLoop * fScale) * 20.0f);
		ret.z = (float) (Math.sin(timeThroughLoop * fScale) * 20.0f);

		return ret;
	}
}