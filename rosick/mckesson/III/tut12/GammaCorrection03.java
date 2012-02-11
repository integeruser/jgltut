package rosick.mckesson.III.tut12;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import static rosick.jglsdk.glm.Vec.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import rosick.GLWindow;
import rosick.PortingUtils.Bufferable;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Timer;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.pole.MousePole.*;
import rosick.jglsdk.glutil.pole.ViewPole;
import rosick.mckesson.III.tut12.LightManager.LightBlock;
import rosick.mckesson.III.tut12.LightManager.LightBlockGamma;
import rosick.mckesson.III.tut12.LightManager.SunlightValueHDR;
import rosick.mckesson.III.tut12.LightManager.TimerTypes;
import rosick.mckesson.III.tut12.Scene.LightingProgramTypes;
import rosick.mckesson.III.tut12.Scene.ProgramData;


// Some graphics card (like my Radeon HD3870) generates an OpenGL error. Don't worry, the tutorial works fine anyway.


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 12. Dynamic Range
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2012.html
 * @author integeruser
 * 
 * W,A,S,D	- move the cameras forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding LEFT_SHIFT with these keys will move in smaller increments.  
 * Q,E		- raise and lower the camera, relative to its current orientation. 
 * 				Holding LEFT_SHIFT with these keys will move in smaller increments.  
 * P		- toggle pausing on/off.
 * -,=		- rewind/jump forward time by one second (of real-time).
 * T		- toggle viewing of the current target point.
 * 1,2,3	- timer commands affect both the sun and the other lights/only the sun/only the other lights.
 * L		- switch to hdr lighting. Pressing LEFT_SHIFT+L will switch to gamma version.
 * K 		- toggles gamma correction.
 * Y,H 		- raise and lower the gamma value (default 2.2).
 * SPACEBAR - print out the current sun-based time, in 24-hour notation.
 * 
 * LEFT	  CLICKING and DRAGGING				- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_ALT	- change the camera's up direction.
 * WHEEL  SCROLLING							- move the camera closer to it's target point or farther away. 
 */
public class GammaCorrection03 extends GLWindow {
	
	public static void main(String[] args) {		
		new GammaCorrection03().start(800, 800);
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/III/tut12/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class UnlitProgData {
		int theProgram;

		int objectColorUnif;
		int modelToCameraMatrixUnif;
	}
	
	
	private class Shaders {
		String fileVertexShader;
		String fileFragmentShader;
		
		Shaders(String fileVertexShader, String fileFragmentShader) {
			this.fileVertexShader = fileVertexShader;
			this.fileFragmentShader = fileFragmentShader;
		}
	}
	
		
	private final int g_materialBlockIndex 		= 0;
	private final int g_lightBlockIndex 		= 1;
	private final int g_projectionBlockIndex 	= 2;

	private ProgramData g_Programs[] = new ProgramData[LightingProgramTypes.LP_MAX_LIGHTING_PROGRAM_TYPES.ordinal()];
	private Shaders g_ShaderFiles[] = new Shaders[] {
		new Shaders(BASEPATH + "PCN.vert", BASEPATH + "DiffuseSpecularGamma.frag"),
		new Shaders(BASEPATH + "PCN.vert", BASEPATH + "DiffuseOnlyGamma.frag"),
		
		new Shaders(BASEPATH + "PN.vert", BASEPATH + "DiffuseSpecularMtlGamma.frag"),
		new Shaders(BASEPATH + "PN.vert", BASEPATH + "DiffuseOnlyMtlGamma.frag"),
	};
	
	private UnlitProgData g_Unlit;
	
	private int g_lightUniformBuffer;
	private int g_projectionUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);
	private FloatBuffer tempFloatBuffer40 = BufferUtils.createFloatBuffer(40);

	
	
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

		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");

		int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

		glUniformBlockBinding(data.theProgram, materialBlock, g_materialBlockIndex);
		glUniformBlockBinding(data.theProgram, lightBlock, g_lightBlockIndex);
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		return data;
	}
	
	private void initializePrograms() {	
		for (int iProg = 0; iProg < LightingProgramTypes.LP_MAX_LIGHTING_PROGRAM_TYPES.ordinal(); iProg++) {
			g_Programs[iProg] = new ProgramData();
			g_Programs[iProg] = loadLitProgram(g_ShaderFiles[iProg].fileVertexShader, g_ShaderFiles[iProg].fileFragmentShader);
		}

		g_Unlit = loadUnlitProgram(BASEPATH + "PosTransform.vert", BASEPATH + "UniformColor.frag");
	}
	
	
	@Override
	protected void init() {
		initializePrograms();

		try {
			g_pScene = new Scene(BASEPATH) {

				@Override
				ProgramData getProgram(LightingProgramTypes eType) {
					return g_Programs[eType.ordinal()];
				}
			};
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}	
		
		setupHDRLighting();

		g_lights.createTimer("tetra", Timer.Type.TT_LOOP, 2.5f);
		
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
					g_lights.togglePause(g_eTimerMode);
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_MINUS) {
					g_lights.rewindTime(g_eTimerMode, 1.0f);
										
				} else if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS) {
					g_lights.fastForwardTime(g_eTimerMode, 1.0f);
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
					g_bDrawCameraPos = !g_bDrawCameraPos;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_1) {
					g_eTimerMode = TimerTypes.TIMER_ALL;
					System.out.println("All.");
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_2) {
					g_eTimerMode = TimerTypes.TIMER_SUN;
					System.out.println("Sun.");
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_3) {
					g_eTimerMode = TimerTypes.TIMER_LIGHTS;
					System.out.println("Lights.");
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_L) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						setupGammaLighting();
					} else {
						setupHDRLighting();
					}
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_K) {
					g_isGammaCorrect = !g_isGammaCorrect;
					if (g_isGammaCorrect) {
						System.out.println("Gamma on!");
					} else {
						System.out.println("Gamma off!");
					}
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_Y) {
					g_gammaValue += 0.1f;
					System.out.println("Gamma: " + g_gammaValue);
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_H) {
					g_gammaValue -= 0.1f;
					if (g_gammaValue < 1.0f) {
						g_gammaValue = 1.0f;	
					}
					System.out.println("Gamma: " + g_gammaValue);
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					float sunAlpha = g_lights.getSunTime();
					float sunTimeHours = sunAlpha * 24.0f + 12.0f;
					sunTimeHours = sunTimeHours > 24.0f ? sunTimeHours - 24.0f : sunTimeHours;
					int sunHours = (int) sunTimeHours;
					float sunTimeMinutes = (sunTimeHours - sunHours) * 60.0f;
					int sunMinutes = (int) sunTimeMinutes;
					System.out.format("%02d:%02d\n", sunHours, sunMinutes);
					
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					leaveMainLoop();
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		g_lights.updateTime(getElapsedTime());
		
		float gamma = g_isGammaCorrect ? g_gammaValue : 1.0f;

		Vec4 bkg = gammaCorrect(g_lights.getBackgroundColor(), gamma);

		glClearColor(bkg.get(X), bkg.get(Y), bkg.get(Z), bkg.get(W));
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		modelMatrix.clear();
		modelMatrix.setMatrix(g_viewPole.calcMatrix());

		final Mat4 worldToCamMat = modelMatrix.top();
		LightBlockGamma lightData = g_lights.getLightInformationGamma(worldToCamMat);
		lightData.gamma = gamma;

		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer(tempFloatBuffer40));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		{
			modelMatrix.push();

			g_pScene.draw(modelMatrix, g_materialBlockIndex, g_lights.getTimerValue("tetra"));
			
			modelMatrix.pop();
		}
		
		{
			modelMatrix.push();

			// Render the sun
			{
				modelMatrix.push();

				Vec3 sunlightDir = new Vec3(g_lights.getSunlightDirection());
				modelMatrix.translate(sunlightDir.scale(500.0f));
				modelMatrix.scale(30.0f, 30.0f, 30.0f);

				glUseProgram(g_Unlit.theProgram);
				glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

				Vec4 lightColor = g_lights.getSunlightIntensity();
				glUniform4(g_Unlit.objectColorUnif, lightColor.fillAndFlipBuffer(tempFloatBuffer4));
				g_pScene.getSphereMesh().render("flat");
				
				modelMatrix.pop();
			}

			// Render the lights
			if (g_bDrawLights) {
				for (int light = 0; light < g_lights.getNumberOfPointLights(); light++) {
					modelMatrix.push();

					modelMatrix.translate(g_lights.getWorldLightPosition(light));

					glUseProgram(g_Unlit.theProgram);
					glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

					Vec4 lightColor = g_lights.getPointLightIntensity(light);
					glUniform4(g_Unlit.objectColorUnif, lightColor.fillAndFlipBuffer(tempFloatBuffer4));
					g_pScene.getCubeMesh().render("flat");

					modelMatrix.pop();
				}
			}
			
			if (g_bDrawCameraPos) {
				modelMatrix.push();

				modelMatrix.setIdentity();
				modelMatrix.translate(0.0f, 0.0f, - g_viewPole.getView().radius);

				glDisable(GL_DEPTH_TEST);
				glDepthMask(false);
				glUseProgram(g_Unlit.theProgram);
				glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
				glUniform4f(g_Unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
				g_pScene.getCubeMesh().render("flat");
				glDepthMask(true);
				glEnable(GL_DEPTH_TEST);
				glUniform4f(g_Unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
				g_pScene.getCubeMesh().render("flat");
				
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
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.cameraToClipMatrix.fillAndFlipBuffer(tempFloatBuffer16));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class ProjectionBlock implements Bufferable<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = 16 * (Float.SIZE / 8);

		@Override
		public FloatBuffer fillAndFlipBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillAndFlipBuffer(buffer);
		}
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
		
	
	private final Vec4 g_skyDaylightColor = new Vec4(0.65f, 0.65f, 1.0f, 1.0f);

	private Scene g_pScene;
	private LightManager g_lights = new LightManager();
	
	private TimerTypes g_eTimerMode = TimerTypes.TIMER_ALL;

	private boolean g_bDrawLights = true;
	private boolean g_bDrawCameraPos;
	private boolean g_isGammaCorrect;
	private float g_gammaValue = 2.2f;
	
	
	// View/Object Setup
	
	private ViewData g_initialViewData = new ViewData(
		new Vec3(-59.5f, 44.0f, 95.0f),
		new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
		50.0f,
		0.0f
	);

	private ViewScale g_viewScale = new ViewScale(	
		3.0f, 80.0f,
		4.0f, 1.0f,
		5.0f, 1.0f,
		90.0f / 250.0f
	);

	private ViewPole g_viewPole = new ViewPole(g_initialViewData, g_viewScale, MouseButtons.MB_LEFT_BTN);
		
	
	private void setupHDRLighting() {
		SunlightValueHDR values[] = {
			new SunlightValueHDR(0.0f/24.0f, new Vec4(0.6f, 0.6f, 0.6f, 1.0f), new Vec4(1.8f, 1.8f, 1.8f, 1.0f), new Vec4(g_skyDaylightColor), 3.0f),
			new SunlightValueHDR(4.5f/24.0f, new Vec4(0.6f, 0.6f, 0.6f, 1.0f), new Vec4(1.8f, 1.8f, 1.8f, 1.0f), new Vec4(g_skyDaylightColor), 3.0f),
			new SunlightValueHDR(6.5f/24.0f, new Vec4(0.225f, 0.075f, 0.075f, 1.0f), new Vec4(0.45f, 0.15f, 0.15f, 1.0f), new Vec4(0.5f, 0.1f, 0.1f, 1.0f), 1.5f),
			new SunlightValueHDR(8.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 1.0f),
			new SunlightValueHDR(18.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 1.0f),
			new SunlightValueHDR(19.5f/24.0f, new Vec4(0.225f, 0.075f, 0.075f, 1.0f), new Vec4(0.45f, 0.15f, 0.15f, 1.0f), new Vec4(0.5f, 0.1f, 0.1f, 1.0f), 1.5f),
			new SunlightValueHDR(20.5f/24.0f, new Vec4(0.6f, 0.6f, 0.6f, 1.0f), new Vec4(1.8f, 1.8f, 1.8f, 1.0f), new Vec4(g_skyDaylightColor), 3.0f),
		};

		g_lights.setSunlightValues(values, 7);

		g_lights.setPointLightIntensity(0, new Vec4(0.6f, 0.6f, 0.6f, 1.0f));
		g_lights.setPointLightIntensity(1, new Vec4(0.0f, 0.0f, 0.7f, 1.0f));
		g_lights.setPointLightIntensity(2, new Vec4(0.7f, 0.0f, 0.0f, 1.0f));
	}
	
	private void setupGammaLighting() {
		Vec4 sunlight = new Vec4(6.5f, 6.5f, 6.5f, 1.0f);
		Vec4 brightAmbient = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);
		
		SunlightValueHDR values[] = {
				new SunlightValueHDR( 0.0f/24.0f, brightAmbient, sunlight, new Vec4(0.65f, 0.65f, 1.0f, 1.0f), 10.0f),
				new SunlightValueHDR( 4.5f/24.0f, brightAmbient, sunlight, new Vec4(g_skyDaylightColor), 10.0f),
				new SunlightValueHDR( 6.5f/24.0f, new Vec4(0.01f, 0.025f, 0.025f, 1.0f), new Vec4(2.5f, 0.2f, 0.2f, 1.0f), new Vec4(0.5f, 0.1f, 0.1f, 1.0f), 5.0f),
				new SunlightValueHDR( 8.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 3.0f),
				new SunlightValueHDR(18.0f/24.0f, new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), new Vec4(0.0f, 0.0f, 0.0f, 1.0f), 3.0f),
				new SunlightValueHDR(19.5f/24.0f, new Vec4(0.01f, 0.025f, 0.025f, 1.0f), new Vec4(2.5f, 0.2f, 0.2f, 1.0f), new Vec4(0.5f, 0.1f, 0.1f, 1.0f), 5.0f),
				new SunlightValueHDR(20.5f/24.0f, brightAmbient, sunlight, new Vec4(g_skyDaylightColor), 10.0f)
		};
				
		g_lights.setSunlightValues(values, 7);

		g_lights.setPointLightIntensity(0, new Vec4(0.6f, 0.6f, 0.6f, 1.0f));
		g_lights.setPointLightIntensity(1, new Vec4(0.0f, 0.0f, 0.7f, 1.0f));
		g_lights.setPointLightIntensity(2, new Vec4(0.7f, 0.0f, 0.0f, 1.0f));
	}
	
	
	private Vec4 gammaCorrect(Vec4 input, float gamma) {
		Vec4 ret = new Vec4();
		ret.set(X, (float) Math.pow(input.get(X), 1.0f / gamma));
		ret.set(Y, (float) Math.pow(input.get(Y), 1.0f / gamma));
		ret.set(Z, (float) Math.pow(input.get(Z), 1.0f / gamma));
		ret.set(W, input.get(W));

		return ret;
	}
}