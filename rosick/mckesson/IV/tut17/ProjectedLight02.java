package rosick.mckesson.IV.tut17;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import rosick.LWJGLWindow;
import rosick.jglsdk.BufferableData;
import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.TextureGenerator;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;
import rosick.mckesson.framework.Framework;
import rosick.mckesson.framework.Mesh;
import rosick.mckesson.framework.MousePole;
import rosick.mckesson.framework.Scene;
import rosick.mckesson.framework.Scene.SceneNode;
import rosick.mckesson.framework.SceneBinders;
import rosick.mckesson.framework.Timer;
import rosick.mckesson.framework.SceneBinders.UniformIntBinder;
import rosick.mckesson.framework.SceneBinders.UniformMat4Binder;
import rosick.mckesson.framework.SceneBinders.UniformVec3Binder;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * IV. Texturing
 * 17. Spotlight on Textures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2017.html
 * @author integeruser
 * 
 * SPACE	- reset the projected flashlight direction.
 * T		- toggle viewing of the current target point.
 * G		- toggle all of the regular lighting on and off.
 * H		- toggle between the edge clamping sampler and the border clamping one.
 * P		- toggle pausing.
 * Y		- increase the FOV.
 * N		- decrease the FOV.
 * 1,2,3	- toggle between different light textures.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING			- rotate the projected flashlight horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL	- rotate the projected flashlight horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT	- spin the projected flashlight.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 * 
 * W,A,S,D	- move the camera forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.  
 * Q,E		- raise and lower the camera, relative to its current orientation. 
 * 				Holding SHIFT with these keys will move in smaller increments.
 * 
 * I,J,K,L	- move the projected flashlight forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.  
 * U,O		- raise and lower the projected flashlight, relative to its current orientation. 
 * 				Holding SHIFT with these keys will move in smaller increments.
 */
public class ProjectedLight02 extends LWJGLWindow {
	
	public static void main(String[] args) {	
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/IV/tut17/data/";
		
		new ProjectedLight02().start(g_displayWidth, g_displayHeight);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	protected void init() {		
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
		glEnable(GL_FRAMEBUFFER_SRGB);

		// Setup our Uniform Buffers
		g_projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_STREAM_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer,
				0, ProjectionBlock.SIZE);

		createSamplers();
		loadTextures();
		
		try {
			loadAndSetupScene();
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}

		g_lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_STREAM_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, g_lightBlockIndex, g_lightUniformBuffer,
				0, LightBlock.SIZE);

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
					MousePole.forwardMouseButton(g_lightViewPole, eventButton, true, Mouse.getX(), Mouse.getY());			
				} else {
					// Mouse up
					MousePole.forwardMouseButton(g_viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseButton(g_lightViewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(g_viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(g_viewPole, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseMotion(g_lightViewPole, Mouse.getX(), Mouse.getY());			
				}
			}
		}
		
		
		float lastFrameDuration = (float) (getLastFrameDuration() / 100.0);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			g_viewPole.charPress(Keyboard.KEY_W, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			g_viewPole.charPress(Keyboard.KEY_S, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			g_viewPole.charPress(Keyboard.KEY_D, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			g_viewPole.charPress(Keyboard.KEY_A, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			g_viewPole.charPress(Keyboard.KEY_E, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			g_viewPole.charPress(Keyboard.KEY_Q, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		
		if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
			g_lightViewPole.charPress(Keyboard.KEY_I, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			g_lightViewPole.charPress(Keyboard.KEY_K, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			g_lightViewPole.charPress(Keyboard.KEY_L, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			g_lightViewPole.charPress(Keyboard.KEY_J, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			g_lightViewPole.charPress(Keyboard.KEY_O, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
			g_lightViewPole.charPress(Keyboard.KEY_U, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					g_lightViewPole.reset();
					break;
					
				case Keyboard.KEY_T:
					g_bDrawCameraPos = !g_bDrawCameraPos;
					break;

				case Keyboard.KEY_G:
					g_bShowOtherLights = !g_bShowOtherLights;
					break;
	
				case Keyboard.KEY_H:
					g_currSampler = (g_currSampler + 1) % NUM_SAMPLERS;
					break;
					
				case Keyboard.KEY_P:
					g_timer.togglePause();
					break;
					
				case Keyboard.KEY_Y:
					g_currFOVIndex = Math.min(g_currFOVIndex + 1, g_lightFOVs.length - 1);
					System.out.printf("Curr FOV: %f\n", g_lightFOVs[g_currFOVIndex]);
					break;
					
				case Keyboard.KEY_N:
					g_currFOVIndex = Math.max(g_currFOVIndex - 1, 0);
					System.out.printf("Curr FOV: %f\n", g_lightFOVs[g_currFOVIndex]);
					break;
					
				case Keyboard.KEY_1:
				case Keyboard.KEY_2:
				case Keyboard.KEY_3:
					int number = Keyboard.getEventKey() - Keyboard.KEY_1;
					g_currTextureIndex = number;
					System.out.printf("%s\n", g_texDefs[g_currTextureIndex].name);
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
		g_timer.update((float) getElapsedTime());

		glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		final Mat4 cameraMatrix = g_viewPole.calcMatrix();
		final Mat4 lightView = g_lightViewPole.calcMatrix();
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.applyMatrix(cameraMatrix);

		buildLights(cameraMatrix);
		
		g_nodes.get(0).nodeSetOrient(Glm.rotate(new Quaternion(1.0f),
				360.0f * g_timer.getAlpha(), new Vec3(0.0f, 1.0f, 0.0f)));

		g_nodes.get(3).nodeSetOrient(Quaternion.mul(g_spinBarOrient, Glm.rotate(new Quaternion(1.0f),
				360.0f * g_timer.getAlpha(), new Vec3(0.0f, 0.0f, 1.0f))));

		{		
			MatrixStack persMatrix = new MatrixStack();
			persMatrix.perspective(60.0f, g_displayWidth / (float) g_displayHeight, g_fzNear, g_fzFar);
			
			ProjectionBlock projData = new ProjectionBlock();
			projData.cameraToClipMatrix = persMatrix.top();

			glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
			glBufferData(GL_UNIFORM_BUFFER, projData.fillAndFlipBuffer(mat4Buffer), GL_STREAM_DRAW);
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
		}

		glActiveTexture(GL_TEXTURE0 + g_lightProjTexUnit);
		glBindTexture(GL_TEXTURE_2D, g_lightTextures[g_currTextureIndex]);
		glBindSampler(g_lightProjTexUnit, g_samplers[g_currSampler]);	
		
		{
			MatrixStack lightProjStack = new MatrixStack();
			// Texture-space transform
			lightProjStack.translate(0.5f, 0.5f, 0.0f);
			lightProjStack.scale(0.5f, 0.5f, 1.0f);
			
			// Project. Z-range is irrelevant.
			lightProjStack.perspective(g_lightFOVs[g_currFOVIndex], 1.0f, 1.0f, 100.0f);
			
			// Transform from main camera space to light camera space.
			lightProjStack.applyMatrix(lightView);
			lightProjStack.applyMatrix(Glm.inverse(cameraMatrix));

			g_lightProjMatBinder.setValue(lightProjStack.top());

			Vec4 worldLightPos = Glm.inverse(lightView).getColumn(3);
			Vec3 lightPos = new Vec3(Mat4.mul(cameraMatrix, worldLightPos));

			g_camLightPosBinder.setValue(lightPos);
		}
				
		glViewport(0, 0, g_displayWidth, g_displayHeight);
		g_pScene.render(modelMatrix.top());

		{
			// Draw axes
			modelMatrix.push();
			
			modelMatrix.applyMatrix(Glm.inverse(lightView));
			modelMatrix.scale(15.0f);
			modelMatrix.scale(1.0f, 1.0f, -1.0f); 				// Invert the Z-axis so that it points in the right direction.

			glUseProgram(g_coloredProg);
			glUniformMatrix4(g_coloredModelToCameraMatrixUnif, false,
				modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			g_pAxesMesh.render();
	
			modelMatrix.pop();
		}
		
		if (g_bDrawCameraPos) {
			modelMatrix.push();

			// Draw lookat point.
			modelMatrix.setIdentity();
			modelMatrix.translate(0.0f, 0.0f, -g_viewPole.getView().radius);
			modelMatrix.scale(0.5f);

			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			glUseProgram(g_unlitProg);
			glUniformMatrix4(g_unlitModelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			glUniform4f(g_unlitObjectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
			g_pSphereMesh.render("flat");
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(g_unlitObjectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			g_pSphereMesh.render("flat");
			
			modelMatrix.pop();
		}
		
		glActiveTexture(GL_TEXTURE0 + g_lightProjTexUnit);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindSampler(g_lightProjTexUnit, 0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		g_displayWidth = width;
		g_displayHeight = height;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private static int g_displayWidth = 500;
	private static int g_displayHeight = 500;

	private final float g_fzNear = 1.0f;
	private final float g_fzFar = 1000.0f;

	private final FloatBuffer mat4Buffer		= BufferUtils.createFloatBuffer(16);
	private final FloatBuffer lightBlockBuffer	= BufferUtils.createFloatBuffer(40);

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int g_unlitProg;
	private int g_unlitModelToCameraMatrixUnif;
	private int g_unlitObjectColorUnif;
	
	private int g_coloredModelToCameraMatrixUnif;
	private int g_coloredProg;
	
	private Scene g_pScene;
	private ArrayList<SceneNode> g_nodes;
	private Timer g_timer = new Timer(Timer.Type.TT_LOOP, 10.0f);

	private UniformMat4Binder g_lightProjMatBinder;
	private UniformVec3Binder g_camLightPosBinder;
	
	private Quaternion g_spinBarOrient;
	
	private boolean g_bShowOtherLights = true;

	private Mesh g_pSphereMesh, g_pAxesMesh;
	private boolean g_bDrawCameraPos;
		
	private float g_lightFOVs[] = { 
			10.0f, 20.0f, 45.0f, 75.0f, 
			90.0f, 120.0f, 150.0f, 170.0f};
	private int g_currFOVIndex = 3;
	
	
	////////////////////////////////
	// View setup.
	private ViewData g_initialView = new ViewData(
			new Vec3(0.0f, 0.0f, 10.0f),
			new Quaternion(0.909845f, 0.16043f, -0.376867f, -0.0664516f),
			25.0f, 
			0.0f);
	
	private ViewScale g_initialViewScale = new ViewScale(	
			5.0f, 70.0f,
			2.0f, 0.5f,
			2.0f, 0.5f,
			90.0f / 250.0f);
	

	private ViewData g_initLightView = new ViewData(
			new Vec3(0.0f, 0.0f, 20.0f),
			new Quaternion(1.0f, 0.0f, 0.0f, 0.0f),
			5.0f,
			0.0f);
	
	private ViewScale g_initLightViewScale = new ViewScale(	
			0.05f, 10.0f,
			0.1f, 0.05f,
			4.0f, 1.0f,
			90.0f / 250.0f);

	
	private ViewPole g_viewPole 		= new ViewPole(g_initialView, g_initialViewScale, MouseButtons.MB_LEFT_BTN);
	private ViewPole g_lightViewPole	= new ViewPole(g_initLightView, g_initLightViewScale, MouseButtons.MB_RIGHT_BTN, true);	

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private void loadAndSetupScene() {
		g_pScene = new Scene("proj2d_scene.xml");

		g_nodes = new ArrayList<>();
		g_nodes.add(g_pScene.findNode("cube"));
		g_nodes.add(g_pScene.findNode("rightBar"));
		g_nodes.add(g_pScene.findNode("leaningBar"));
		g_nodes.add(g_pScene.findNode("spinBar"));
		g_nodes.add(g_pScene.findNode("diorama"));
		g_nodes.add(g_pScene.findNode("floor"));
		
		g_lightNumBinder = new UniformIntBinder();
		SceneBinders.associateUniformWithNodes(g_nodes, g_lightNumBinder, "numberOfLights");
		SceneBinders.setStateBinderWithNodes(g_nodes, g_lightNumBinder);

		g_lightProjMatBinder = new UniformMat4Binder();
		SceneBinders.associateUniformWithNodes(g_nodes, g_lightProjMatBinder, "cameraToLightProjMatrix");
		SceneBinders.setStateBinderWithNodes(g_nodes, g_lightProjMatBinder);
		
		g_camLightPosBinder = new UniformVec3Binder();
		SceneBinders.associateUniformWithNodes(g_nodes, g_camLightPosBinder, "cameraSpaceProjLightPos");
		SceneBinders.setStateBinderWithNodes(g_nodes, g_camLightPosBinder);	
		
		int unlit = g_pScene.findProgram("p_unlit");
		g_pSphereMesh = g_pScene.findMesh("m_sphere");

		int colored = g_pScene.findProgram("p_colored");
		g_pAxesMesh = g_pScene.findMesh("m_axes");
		
		// No more things that can throw.
		g_spinBarOrient = g_nodes.get(3).nodeGetOrient();
		g_unlitProg = unlit;
		g_unlitModelToCameraMatrixUnif = glGetUniformLocation(unlit, "modelToCameraMatrix");
		g_unlitObjectColorUnif = glGetUniformLocation(unlit, "objectColor");

		g_coloredProg = colored;
		g_coloredModelToCameraMatrixUnif = glGetUniformLocation(colored, "modelToCameraMatrix");	
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final TexDef g_texDefs[] = { 
			new TexDef("Flashlight.dds", 	"Flashlight"),
			new TexDef("PointsOfLight.dds", "Multiple Point Lights"),
			new TexDef("Bands.dds", 		"Light Bands")};
	private final int NUM_LIGHT_TEXTURES = g_texDefs.length;

	private int[] g_lightTextures = new int[g_texDefs.length];
	private int g_currTextureIndex = 0;
	
	
	private class TexDef {
		String filename;
		String name;
		
		TexDef(String filename, String name) {
			this.filename = filename;
			this.name = name;
		}
	}
	
	
	private void loadTextures() {
		try {
			for (int i = 0; i < NUM_LIGHT_TEXTURES; i++) {
				String filepath = Framework.findFileOrThrow(g_texDefs[i].filename);
				ImageSet imageSet = DdsLoader.loadFromFile(filepath);
				
				g_lightTextures[i] = TextureGenerator.createTexture(imageSet, 0);	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
		
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int NUM_SAMPLERS = 2;

	private int[] g_samplers = new int[NUM_SAMPLERS];
	private int g_currSampler = 0;
	
	
	private void createSamplers() {		
		for (int samplerIx = 0; samplerIx < NUM_SAMPLERS; samplerIx++) {
			g_samplers[samplerIx] = glGenSamplers();
			glSamplerParameteri(g_samplers[samplerIx], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glSamplerParameteri(g_samplers[samplerIx], GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}

		glSamplerParameteri(g_samplers[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(g_samplers[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glSamplerParameteri(g_samplers[1], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glSamplerParameteri(g_samplers[1], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

		
		float[] color = {0.0f, 0.0f, 0.0f, 1.0f};
		FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
		
		for (float f : color) {
			buffer.put(f);
		}
		
		buffer.flip();
		
		glSamplerParameter(g_samplers[1], GL_TEXTURE_BORDER_COLOR, buffer);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int g_projectionBlockIndex = 0;

	private int g_projectionUniformBuffer;

		
	private class ProjectionBlock extends BufferableData<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = 16 * FLOAT_SIZE;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
		
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static final int MAX_NUMBER_OF_LIGHTS = 4;

	private final int g_lightBlockIndex = 1;
	private final int g_lightProjTexUnit = 3;

	private int g_lightUniformBuffer;
	private UniformIntBinder g_lightNumBinder;


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
		float maxIntensity;
		float padding[] = new float[2];
		PerLight lights[] = new PerLight[MAX_NUMBER_OF_LIGHTS];

		static final int SIZE = (4 + 1 + 1 + 2 + (8 * MAX_NUMBER_OF_LIGHTS)) * FLOAT_SIZE;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {			
			ambientIntensity.fillBuffer(buffer);
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(padding);
			
			for (PerLight light : lights) {
				if (light == null)
					break;
				
				light.fillBuffer(buffer);
			}
			
			return buffer;
		}
	}

	
	private void buildLights(Mat4 camMatrix) {
		LightBlock lightData = new LightBlock();
		lightData.ambientIntensity = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
		lightData.lightAttenuation = 1.0f / (30.0f * 30.0f);
		lightData.maxIntensity = 2.0f;
		
		lightData.lights[0] = new PerLight();
		lightData.lights[0].lightIntensity = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(camMatrix, 
				Glm.normalize(new Vec4(-0.2f, 0.5f, 0.5f, 0.0f)));
		
		lightData.lights[1] = new PerLight();
		lightData.lights[1].lightIntensity = new Vec4(3.5f, 6.5f, 3.0f, 1.0f).scale(0.5f);
		lightData.lights[1].cameraSpaceLightPos = Mat4.mul(camMatrix, 
				new Vec4(5.0f, 6.0f, 0.5f, 1.0f));

		if (g_bShowOtherLights) {
			g_lightNumBinder.setValue(2);
		} else {
			g_lightNumBinder.setValue(0);
		}
		
		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, lightData.fillAndFlipBuffer(lightBlockBuffer), GL_STREAM_DRAW);
	}
}