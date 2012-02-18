package rosick.mckesson.III.tut10;

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

import rosick.LWJGLWindow;
import rosick.PortingUtils.BufferableData;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.framework.MousePole;
import rosick.jglsdk.framework.Timer;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 10. Plane Lights
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2010.html
 * @author integeruser
 * 
 * I,J,K,L  - control the light's position. Holding LEFT_SHIFT with these keys will move in smaller increments.
 * SPACE	- toggles between drawing the uncolored cylinder and the colored one.
 * Y 		- toggles the drawing of the light source.
 * T 		- toggles between the scaled and unscaled cylinder.
 * H 		- toggles between per-fragment lighting and per-vertex lighting.
 * B 		- toggles the light's rotation on/off.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING			- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT	- spin the object.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 */
public class FragmentPointLighting02 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		new FragmentPointLighting02().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/III/tut10/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class ProgramData {
		int theProgram;
		
		int modelSpaceLightPosUnif;
		int lightIntensityUnif;
		int ambientIntensityUnif;

		int modelToCameraMatrixUnif;
	}
	
	private class UnlitProgData {
		int theProgram;
		
		int objectColorUnif;
		int modelToCameraMatrixUnif;
	}
	
	
	private final int g_projectionBlockIndex = 2;

	private ProgramData g_WhiteDiffuseColor;
	private ProgramData g_VertexDiffuseColor;
	private ProgramData g_FragWhiteDiffuseColor;
	private ProgramData g_FragVertexDiffuseColor;
	
	private UnlitProgData g_Unlit;
	
	private int g_projectionUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer16 	= BufferUtils.createFloatBuffer(16);
	
	

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
	
	private ProgramData loadLitProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.modelSpaceLightPosUnif = glGetUniformLocation(data.theProgram, "modelSpaceLightPos");
		data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
		data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "ambientIntensity");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		return data;
	}
	
	private void initializePrograms() {	
		g_WhiteDiffuseColor =		loadLitProgram(TUTORIAL_DATAPATH + "ModelPosVertexLighting_PN.vert",	TUTORIAL_DATAPATH + "ColorPassthrough.frag");		
		g_VertexDiffuseColor = 		loadLitProgram(TUTORIAL_DATAPATH + "ModelPosVertexLighting_PCN.vert",	TUTORIAL_DATAPATH + "ColorPassthrough.frag");
		g_FragWhiteDiffuseColor =	loadLitProgram(TUTORIAL_DATAPATH + "FragmentLighting_PN.vert",			TUTORIAL_DATAPATH + "FragmentLighting.frag");		
		g_FragVertexDiffuseColor = 	loadLitProgram(TUTORIAL_DATAPATH + "FragmentLighting_PCN.vert", 		TUTORIAL_DATAPATH + "FragmentLighting.frag");
		
		g_Unlit = loadUnlitProgram(TUTORIAL_DATAPATH + "PosTransform.vert", TUTORIAL_DATAPATH + "UniformColor.frag");
	}
	
	
	@Override
	protected void init() {
		initializePrograms();
		
		try {
			g_pCylinderMesh = new Mesh(TUTORIAL_DATAPATH + "UnitCylinder.xml");
			g_pPlaneMesh 	= new Mesh(TUTORIAL_DATAPATH + "LargePlane.xml");
			g_pCubeMesh 	= new Mesh(TUTORIAL_DATAPATH + "UnitCube.xml");
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
		glEnable(GL_DEPTH_CLAMP);
		
		g_projectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer, 0, ProjectionBlock.SIZE);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	
	@Override
	protected void update() {
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();
									
			if (eventButton != -1) {
				if (Mouse.getEventButtonState()) {
					// Mouse down
					MousePole.forwardMouseButton(g_viewPole, eventButton, true, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseButton(g_objtPole, eventButton, true, Mouse.getX(), Mouse.getY());	
				} else {
					// Mouse up
					MousePole.forwardMouseButton(g_viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseButton(g_objtPole, eventButton, false, Mouse.getX(), Mouse.getY());
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(g_viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
					MousePole.forwardMouseWheel(g_objtPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(g_viewPole, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseMotion(g_objtPole, Mouse.getX(), Mouse.getY());
				}
			}
		}
		
		
		float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightRadius -= 0.05f * lastFrameDuration;
			} else {
				g_fLightRadius -= 0.2f * lastFrameDuration;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightRadius += 0.05f * lastFrameDuration;
			} else {
				g_fLightRadius += 0.2f * lastFrameDuration;
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightHeight += 0.05f * lastFrameDuration;
			} else {
				g_fLightHeight += 0.2f * lastFrameDuration;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightHeight -= 0.05f * lastFrameDuration;
			} else {
				g_fLightHeight -= 0.2f * lastFrameDuration;
			}
		}
		
		
		if (g_fLightRadius < 0.2f) {
			g_fLightRadius = 0.2f;
		}
		

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					g_bDrawColoredCyl = !g_bDrawColoredCyl;
					break;
					
				case Keyboard.KEY_Y:
					g_bDrawLight = !g_bDrawLight;
					break;

				case Keyboard.KEY_T:
					g_bScaleCyl = !g_bScaleCyl;
					break;
					
				case Keyboard.KEY_H:
					g_bUseFragmentLighting = !g_bUseFragmentLighting;
					break;
					
				case Keyboard.KEY_B:
					g_LightTimer.togglePause();
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
		g_LightTimer.update((float) getElapsedTime());
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		modelMatrix.clear();
		modelMatrix.setMatrix(g_viewPole.calcMatrix());

		final Vec4 worldLightPos = calcLightPosition();
		
		Vec4 lightPosCameraSpace = Mat4.mul(modelMatrix.top(), worldLightPos);
		
		ProgramData pWhiteProgram;
		ProgramData pVertColorProgram;
		
		if (g_bUseFragmentLighting) {
			pWhiteProgram = 	g_FragWhiteDiffuseColor;
			pVertColorProgram = g_FragVertexDiffuseColor;
		} else {
			pWhiteProgram = 	g_WhiteDiffuseColor;
			pVertColorProgram = g_VertexDiffuseColor;
		}
			
		glUseProgram(pWhiteProgram.theProgram);
		glUniform4f(pWhiteProgram.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(pWhiteProgram.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUseProgram(pVertColorProgram.theProgram);
		glUniform4f(pVertColorProgram.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(pVertColorProgram.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUseProgram(0);
		
		{
			modelMatrix.push();

			// Render the ground plane.
			{
				modelMatrix.push();

				glUseProgram(pWhiteProgram.theProgram);
				glUniformMatrix4(pWhiteProgram.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

				Mat4 invTransform = Glm.inverse(modelMatrix.top());
				Vec4 lightPosModelSpace = Mat4.mul(invTransform, lightPosCameraSpace);
				glUniform3(pWhiteProgram.modelSpaceLightPosUnif, lightPosModelSpace.fillAndFlipBuffer(tempFloatBuffer4));

				g_pPlaneMesh.render();
				glUseProgram(0);
				
				modelMatrix.pop();
			}

			// Render the Cylinder
			{
				modelMatrix.push();
	
				modelMatrix.applyMatrix(g_objtPole.calcMatrix());

				if (g_bScaleCyl) {
					modelMatrix.scale(1.0f, 1.0f, 0.2f);
				}
				
				Mat4 invTransform = Glm.inverse(modelMatrix.top());
				Vec4 lightPosModelSpace = Mat4.mul(invTransform, lightPosCameraSpace);
				
				if (g_bDrawColoredCyl) {
					glUseProgram(pVertColorProgram.theProgram);
					glUniformMatrix4(pVertColorProgram.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
					
					glUniform3(pVertColorProgram.modelSpaceLightPosUnif, lightPosModelSpace.fillAndFlipBuffer(tempFloatBuffer4));

					g_pCylinderMesh.render("lit-color");
				} else {
					glUseProgram(pWhiteProgram.theProgram);
					glUniformMatrix4(pWhiteProgram.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

					glUniform3(pWhiteProgram.modelSpaceLightPosUnif, lightPosModelSpace.fillAndFlipBuffer(tempFloatBuffer4));

					g_pCylinderMesh.render("lit");
				}
				glUseProgram(0);
				
				modelMatrix.pop();
			}
			
			// Render the light
			if (g_bDrawLight) {
				modelMatrix.push();

				modelMatrix.translate(worldLightPos.x, worldLightPos.y, worldLightPos.z);
				modelMatrix.scale(0.1f, 0.1f, 0.1f);

				glUseProgram(g_Unlit.theProgram);
				glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
				glUniform4f(g_Unlit.objectColorUnif, 0.8078f, 0.8706f, 0.9922f, 1.0f);
				g_pCubeMesh.render("flat");
				
				modelMatrix.pop();
			}
			
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
	
	private class ProjectionBlock extends BufferableData<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = 16 * FLOAT_SIZE;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
		
	
	private Mesh g_pCylinderMesh;
	private Mesh g_pPlaneMesh;
	private Mesh g_pCubeMesh;
	
	private Timer g_LightTimer = new Timer(Timer.Type.TT_LOOP, 5.0f);

	private boolean g_bUseFragmentLighting = true;
	private boolean g_bDrawColoredCyl;
	private boolean g_bDrawLight;
	private boolean g_bScaleCyl;
	private float g_fLightHeight = 1.5f;
	private float g_fLightRadius = 1.0f;
	
	
	// View/Object Setup
	
	private ViewData g_initialViewData = new ViewData(
			new Vec3(0.0f, 0.5f, 0.0f),
			new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
			5.0f,
			0.0f
	);

	private ViewScale g_viewScale = new ViewScale(	
			3.0f, 20.0f,
			1.5f, 0.5f,
			0.0f, 0.0f,																// No camera movement.
			90.0f / 250.0f
	);
	
	private ObjectData g_initialObjectData = new ObjectData(
			new Vec3(0.0f, 0.5f, 0.0f),
			new Quaternion(1.0f, 0.0f, 0.0f, 0.0f)
	);

	private ViewPole g_viewPole = new ViewPole(g_initialViewData, g_viewScale, MouseButtons.MB_LEFT_BTN);
	private ObjectPole g_objtPole = new ObjectPole(g_initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, g_viewPole);
	

	private Vec4 calcLightPosition() {
		float fCurrTimeThroughLoop = g_LightTimer.getAlpha();

		Vec4 ret = new Vec4(0.0f, g_fLightHeight, 0.0f, 1.0f);

		ret.x = (float) (Math.cos(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius);
		ret.z = (float) (Math.sin(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius);

		return ret;
	}
}