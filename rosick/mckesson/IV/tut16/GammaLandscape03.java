package rosick.mckesson.IV.tut16;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;

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
import rosick.jglsdk.glimg.Dds;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;
import rosick.mckesson.IV.tut16.LightEnv.LightBlock;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * IV. Texturing
 * 16. Gamma and Textures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2016.html
 * @author integeruser
 * 
 * A		- toggles gamma correction.
 * G		- switches to a texture who's mipmaps were properly generated.
 * SPACE	- presses A and G keys.
 * Y		- toggles between plane/corridor mesh.
 * P		- toggles pausing on/off.
 * 1,2		- select linear mipmap filtering and anisotropic filtering (using the maximum possible anisotropy).
 */
public class GammaLandscape03 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		new GammaLandscape03().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String COMMON_DATAPATH = "/rosick/mckesson/data/";
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/IV/tut16/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class ProgramData {
		int theProgram;

		int modelToCameraMatrixUnif;
		int numberOfLightsUnif;
	}
	
	private class UnlitProgData {
		int theProgram;

		int modelToCameraMatrixUnif;
		int objectColorUnif;
	};
		
	
	private final int g_projectionBlockIndex = 0;
	private final int g_lightBlockIndex = 1;
	private final int g_colorTexUnit = 0;

	private ProgramData g_progStandard;
	private UnlitProgData g_progUnlit;

	private int g_projectionUniformBuffer;
	private int g_lightUniformBuffer;
	private int g_linearTexture;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer16 	= BufferUtils.createFloatBuffer(16);
	private FloatBuffer tempFloatBuffer40 	= BufferUtils.createFloatBuffer(40);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ProgramData loadProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.numberOfLightsUnif = glGetUniformLocation(data.theProgram, "numberOfLights");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		int lightBlockIndex = glGetUniformBlockIndex(data.theProgram, "Light");
		glUniformBlockBinding(data.theProgram, lightBlockIndex, g_lightBlockIndex);

		int colorTextureUnif = glGetUniformLocation(data.theProgram, "diffuseColorTex");
		glUseProgram(data.theProgram);
		glUniform1i(colorTextureUnif, g_colorTexUnit);
		glUseProgram(0);
		
		return data;
	}
	
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
		
	private void initializePrograms() {	
		g_progStandard = loadProgram(TUTORIAL_DATAPATH + "PNT.vert", TUTORIAL_DATAPATH + "litTexture.frag");
		g_progUnlit = loadUnlitProgram(COMMON_DATAPATH + "Unlit.vert", COMMON_DATAPATH + "Unlit.frag");
	}
	
	
	@Override
	protected void init() {	
		try {
			g_pLightEnv = new LightEnv("/rosick/mckesson/IV/tut16/data/LightEnv.xml");
			
			initializePrograms();
			
			g_pTerrain = 	new Mesh(TUTORIAL_DATAPATH + "terrain.xml");
			g_pSphere = 	new Mesh(TUTORIAL_DATAPATH + "UnitSphere.xml");
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
		g_projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer,
			0, ProjectionBlock.SIZE);

		g_lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_STREAM_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, g_lightBlockIndex, g_lightUniformBuffer,
			0, LightBlock.SIZE);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		loadTextures();
		createSamplers();
	}
	

	@Override
	protected void update() {
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();
									
			if (eventButton != -1) {
				if (Mouse.getEventButtonState()) {
					// Mouse down
					MousePole.forwardMouseButton(g_viewPole, eventButton, true, Mouse.getX(), Mouse.getY());			
				} else {
					// Mouse up
					MousePole.forwardMouseButton(g_viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(g_viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(g_viewPole, Mouse.getX(), Mouse.getY());			
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
					g_useGammaDisplay = !g_useGammaDisplay;
					break;
					
				case Keyboard.KEY_MINUS:
					g_pLightEnv.rewindTime(1.0f);
					break;

				case Keyboard.KEY_EQUALS:
					g_pLightEnv.fastForwardTime(1.0f);
					break;
					
				case Keyboard.KEY_T:
					g_bDrawCameraPos = !g_bDrawCameraPos;
					break;
					
				case Keyboard.KEY_P:
					g_pLightEnv.togglePause();
					break;
				
				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}
				
				
				if (Keyboard.KEY_1 <= Keyboard.getEventKey() && Keyboard.getEventKey() <= Keyboard.KEY_9) {
					int number = Keyboard.getEventKey() - Keyboard.KEY_1;
					if (number < NUM_SAMPLERS) {
						g_currSampler = number;
					}
				}
			}
		}
	}
	

	@Override
	protected void display() {	
		if (g_useGammaDisplay) {
			glEnable(GL_FRAMEBUFFER_SRGB);
		} else {
			glDisable(GL_FRAMEBUFFER_SRGB);
		}
		
		g_pLightEnv.updateTime(getElapsedTime());

		Vec4 bgColor = g_pLightEnv.getBackgroundColor();
		glClearColor(bgColor.x, bgColor.y, bgColor.z, bgColor.w);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		modelMatrix.clear();
		modelMatrix.applyMatrix(g_viewPole.calcMatrix());

		LightBlock lightData = g_pLightEnv.getLightBlock(g_viewPole.calcMatrix());

		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, lightData.fillAndFlipBuffer(tempFloatBuffer40), GL_STREAM_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		modelMatrix.push();
		modelMatrix.rotateX(-90.0f);
	
		glUseProgram(g_progStandard.theProgram);
		glUniformMatrix4(g_progStandard.modelToCameraMatrixUnif, false,
			modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
		glUniform1i(g_progStandard.numberOfLightsUnif, g_pLightEnv.getNumLights());

		glActiveTexture(GL_TEXTURE0 + g_colorTexUnit);
		glBindTexture(GL_TEXTURE_2D, g_linearTexture);
		glBindSampler(g_colorTexUnit, g_samplers[g_currSampler]);

		g_pTerrain.render("lit-tex");

		glBindSampler(g_colorTexUnit, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		glUseProgram(0);

		modelMatrix.pop();

		// Render the sun
		{		
			modelMatrix.push();
			
			Vec3 sunlightDir = new Vec3(g_pLightEnv.getSunlightDirection());
			modelMatrix.translate(sunlightDir.scale(500.0f));
			modelMatrix.scale(30.0f, 30.0f, 30.0f);

			glUseProgram(g_progUnlit.theProgram);
			glUniformMatrix4(g_progUnlit.modelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

			Vec4 lightColor = g_pLightEnv.getSunlightScaledIntensity();
			glUniform4(g_progUnlit.objectColorUnif, lightColor.fillAndFlipBuffer(tempFloatBuffer4));
			g_pSphere.render("flat");
			
			modelMatrix.pop();
		}

		// Draw lights
		for (int light = 0; light < g_pLightEnv.getNumPointLights(); light++) {
			modelMatrix.push();
			
			modelMatrix.translate(g_pLightEnv.getPointLightWorldPos(light));

			glUseProgram(g_progUnlit.theProgram);
			glUniformMatrix4(g_progUnlit.modelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

			Vec4 lightColor = g_pLightEnv.getPointLightScaledIntensity(light);
			glUniform4(g_progUnlit.objectColorUnif, lightColor.fillAndFlipBuffer(tempFloatBuffer4));
			g_pSphere.render("flat");
			
			modelMatrix.pop();
		}

		if (g_bDrawCameraPos) {
			modelMatrix.push();

			// Draw lookat point.
			modelMatrix.setIdentity();
			modelMatrix.translate(0.0f, 0.0f, -g_viewPole.getView().radius);

			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			glUseProgram(g_progUnlit.theProgram);
			glUniformMatrix4(g_progUnlit.modelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(g_progUnlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
			g_pSphere.render("flat");
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(g_progUnlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			g_pSphere.render("flat");
			
			modelMatrix.pop();
		}
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(60.0f, (width / (float) height), g_fzNear, g_fzFar);
		
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
			
	
	private final int NUM_SAMPLERS = 2;
		
	private Mesh g_pTerrain;
	private Mesh g_pSphere;

	private LightEnv g_pLightEnv;
	
	private boolean g_useGammaDisplay = true;
	private boolean g_bDrawCameraPos;
	private int g_samplers[] = new int[NUM_SAMPLERS];
	private int g_currSampler;

	
	// View/Object Setup
	
	private ViewData g_initialView = new ViewData(
		new Vec3(-60.257084f, 10.947238f, 62.636356f),
		new Quaternion(-0.972817f, -0.099283f, -0.211198f, -0.020028f),
		30.0f,
		0.0f
	);

	private ViewScale g_initialViewScale = new ViewScale(	
			5.0f, 90.0f,
			2.0f, 0.5f,
			4.0f, 1.0f,
			90.0f / 250.0f
	);

	private ViewPole g_viewPole = new ViewPole(g_initialView, g_initialViewScale, MouseButtons.MB_LEFT_BTN);
		
	
	private void createSamplers() {		
		for (int samplerIx = 0; samplerIx < NUM_SAMPLERS; samplerIx++) {
			g_samplers[samplerIx] = glGenSamplers();
			glSamplerParameteri(g_samplers[samplerIx], GL_TEXTURE_WRAP_S, GL_REPEAT);
			glSamplerParameteri(g_samplers[samplerIx], GL_TEXTURE_WRAP_T, GL_REPEAT);
		}

		// Linear mipmap linear
		glSamplerParameteri(g_samplers[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(g_samplers[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

		// Max anisotropic
		float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);

		glSamplerParameteri(g_samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(g_samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(g_samplers[1], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
	}
	
		
	private void loadTextures() {
		try	{
			ImageSet pImageSet = Dds.loadFromFile(TUTORIAL_DATAPATH + "terrain_tex.dds");

			g_linearTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, g_linearTexture);
			
			for (int mipmapLevel = 0; mipmapLevel < pImageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = pImageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();

				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8_ALPHA8, dims.m_width, dims.m_height, 0, 
						GL_RGBA, GL_UNSIGNED_BYTE, image.getImageData());
			}

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, pImageSet.getMipmapCount() - 1);

			glBindTexture(GL_TEXTURE_2D, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}