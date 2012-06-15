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
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;
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
 * P		- toggle pausing.
 * 1,2		- toggle between different light textures.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING			- rotate the orientation of the light horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL	- rotate the orientation of the light horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT	- spin the orientation of the light.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 * 
 * W,A,S,D	- move the camera forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.  
 * Q,E		- raise and lower the camera, relative to its current orientation. 
 * 				Holding SHIFT with these keys will move in smaller increments.
 */
public class CubePointLight03 extends LWJGLWindow {
	
	public static void main(String[] args) {	
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/IV/tut17/data/";
		
		new CubePointLight03().start(g_displayWidth, g_displayHeight);
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
					MousePole.forwardMouseButton(g_lightPole, eventButton, true, Mouse.getX(), Mouse.getY());			
				} else {
					// Mouse up
					MousePole.forwardMouseButton(g_viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseButton(g_lightPole, eventButton, false, Mouse.getX(), Mouse.getY());			
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(g_viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
					MousePole.forwardMouseWheel(g_lightPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(g_viewPole, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseMotion(g_lightPole, Mouse.getX(), Mouse.getY());			
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
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					g_lightPole.reset();
					break;
					
				case Keyboard.KEY_T:
					g_bDrawCameraPos = !g_bDrawCameraPos;
					break;

				case Keyboard.KEY_G:
					g_bShowOtherLights = !g_bShowOtherLights;
					break;
						
				case Keyboard.KEY_P:
					g_timer.togglePause();
					break;
					
				case Keyboard.KEY_1:
				case Keyboard.KEY_2:
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
		final Mat4 lightView = g_lightPole.calcMatrix();
		
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
		glBindTexture(GL_TEXTURE_CUBE_MAP, g_lightTextures[g_currTextureIndex]);
		glBindSampler(g_lightProjTexUnit, g_samplers[g_currSampler]);	
		
		{
			MatrixStack lightProjStack = new MatrixStack();		
			lightProjStack.applyMatrix(Glm.inverse(lightView));
			lightProjStack.applyMatrix(Glm.inverse(cameraMatrix));

			g_lightProjMatBinder.setValue(lightProjStack.top());

			Vec4 worldLightPos = lightView.getColumn(3);
			Vec3 lightPos = new Vec3(Mat4.mul(cameraMatrix, worldLightPos));

			g_camLightPosBinder.setValue(lightPos);
		}
				
		glViewport(0, 0, g_displayWidth, g_displayHeight);
		g_pScene.render(modelMatrix.top());

		{
			// Draw axes
			modelMatrix.push();
			
			modelMatrix.applyMatrix(lightView);
			modelMatrix.scale(15.0f);

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
		glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
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
	
	
	private ObjectData g_initLightData = new ObjectData(
			new Vec3(0.0f, 0.0f, 10.0f),
			new Quaternion(1.0f, 0.0f, 0.0f, 0.0f)
	);
	
	
	private ViewPole g_viewPole 	= new ViewPole(g_initialView, g_initialViewScale, MouseButtons.MB_LEFT_BTN);
	private ObjectPole g_lightPole 	= new ObjectPole(g_initLightData, 90.0f/250.0f, MouseButtons.MB_RIGHT_BTN, g_viewPole);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private void loadAndSetupScene() {
		g_pScene = new Scene("projCube_scene.xml");

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
			new TexDef("IrregularPoint.dds", 	"Irregular Point Light"),
			new TexDef("Planetarium.dds", 		"Planetarium")};
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
			for (int textureIndex = 0; textureIndex < NUM_LIGHT_TEXTURES; textureIndex++) {
				g_lightTextures[textureIndex] = glGenTextures();
				
				String filepath = Framework.findFileOrThrow(g_texDefs[textureIndex].filename);
				ImageSet imageSet = DdsLoader.loadFromFile(filepath);
				
				glBindTexture(GL_TEXTURE_CUBE_MAP, g_lightTextures[textureIndex]);
				glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BASE_LEVEL, 0);
				glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LEVEL, 0);

				Dimensions imageDimensions = imageSet.getDimensions();
				int imageFormat = TextureGenerator.getInternalFormat(imageSet.getFormat(), 0);
				
				for (int face = 0; face < 6; ++face) {
					SingleImage singleImage = imageSet.getImage(0, 0, face);

					glCompressedTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 
							0, imageFormat, imageDimensions.width, imageDimensions.height, 
							0, singleImage.getImageData());
				}
				
				glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
		
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final int NUM_SAMPLERS = 1;

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
		glSamplerParameteri(g_samplers[0], GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
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
		}
		else {
			g_lightNumBinder.setValue(0);
		}

		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, lightData.fillAndFlipBuffer(lightBlockBuffer), GL_STREAM_DRAW);
	}
}