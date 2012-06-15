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
import org.lwjgl.opengl.GL12;

import rosick.LWJGLWindow;
import rosick.jglsdk.BufferableData;
import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.mckesson.framework.Framework;
import rosick.mckesson.framework.Mesh;
import rosick.mckesson.framework.Timer;


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
public class GammaCheckers02 extends LWJGLWindow {
	
	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/IV/tut16/data/";

		new GammaCheckers02().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class ProgramData {
		int theProgram;

		int modelToCameraMatrixUnif;
	}
		
	
	private final int g_projectionBlockIndex = 0;
	private final int g_colorTexUnit = 0;

	private ProgramData g_progNoGamma;
	private ProgramData g_progGamma;

	private int g_projectionUniformBuffer;
	private int g_linearTexture;
	private int g_gammaTexture;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ProgramData loadProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		int colorTextureUnif = glGetUniformLocation(data.theProgram, "colorTexture");
		glUseProgram(data.theProgram);
		glUniform1i(colorTextureUnif, g_colorTexUnit);
		glUseProgram(0);
		
		return data;
	}
		
	private void initializePrograms() {	
		g_progNoGamma = loadProgram("PT.vert", "textureNoGamma.frag");
		g_progGamma = loadProgram("PT.vert", "textureGamma.frag");
	}
	
	
	@Override
	protected void init() {	
		initializePrograms();

		try {
			g_pCorridor = new Mesh("Corridor.xml");
			g_pPlane = 	new Mesh("BigPlane.xml");
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

		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		loadCheckerTexture();
		createSamplers();
	}
	

	@Override
	protected void update() {		
		while (Keyboard.next()) {
			boolean particularKeyPressed = false;
			
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_A:
					g_drawGammaProgram = !g_drawGammaProgram;
					particularKeyPressed = true;
					break;
					
				case Keyboard.KEY_G:
					g_drawGammaTexture = !g_drawGammaTexture;
					particularKeyPressed = true;
					break;
					
				case Keyboard.KEY_SPACE:
					g_drawGammaProgram = !g_drawGammaProgram;
					g_drawGammaTexture = !g_drawGammaTexture;
					particularKeyPressed = true;
					break;

				case Keyboard.KEY_Y:
					g_drawCorridor = !g_drawCorridor;
					break;
					
				case Keyboard.KEY_P:
					g_camTimer.togglePause();
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
			
			
			if (particularKeyPressed) {
				System.out.printf("----\n");
				System.out.printf("Rendering:\t\t%s\n", g_drawGammaProgram ? "Gamma" : "Linear");
				System.out.printf("Mipmap Generation:\t%s\n", g_drawGammaTexture ? "Gamma" : "Linear");
			}
		}
	}
	

	@Override
	protected void display() {			
		glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		g_camTimer.update((float) getElapsedTime());

		float cyclicAngle = g_camTimer.getAlpha() * 6.28f;
		float hOffset = (float) (Math.cos(cyclicAngle) * 0.25f);
		float vOffset = (float) (Math.sin(cyclicAngle) * 0.25f);

		modelMatrix.clear();

		final Mat4 worldToCamMat = Glm.lookAt(
				new Vec3(hOffset, 1.0f, -64.0f),
				new Vec3(hOffset, -5.0f + vOffset, -44.0f),
				new Vec3(0.0f, 1.0f, 0.0f));

		modelMatrix.applyMatrix(worldToCamMat);	

		final ProgramData prog = g_drawGammaProgram ? g_progGamma : g_progNoGamma;

		glUseProgram(prog.theProgram);
		glUniformMatrix4(prog.modelToCameraMatrixUnif, false,
				modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

		glActiveTexture(GL_TEXTURE0 + g_colorTexUnit);
		glBindTexture(GL_TEXTURE_2D, g_drawGammaTexture ? g_gammaTexture : g_linearTexture);		
		glBindSampler(g_colorTexUnit, g_samplers[g_currSampler]);

		if (g_drawCorridor) {
			g_pCorridor.render("tex");
		} else {
			g_pPlane.render("tex");
		}
		
		glBindSampler(g_colorTexUnit, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(90.0f, (width / (float) height), g_fzNear, g_fzFar);
		
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
		
	private Mesh g_pPlane;
	private Mesh g_pCorridor;

	private Timer g_camTimer = new Timer(Timer.Type.TT_LOOP, 5.0f);

	private boolean g_drawCorridor;
	private boolean g_drawGammaTexture;
	private boolean g_drawGammaProgram;
	private int g_samplers[] = new int[NUM_SAMPLERS];
	private int g_currSampler;


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
	
		
	private void loadCheckerTexture() {
		try	{
			String filepath = Framework.findFileOrThrow("checker_linear.dds");
			ImageSet pImageSet = DdsLoader.loadFromFile(filepath);

			g_linearTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, g_linearTexture);

			for (int mipmapLevel = 0; mipmapLevel < pImageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = pImageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();

				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8, dims.width, dims.height, 0,
						GL12.GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData());
			}

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, pImageSet.getMipmapCount() - 1);

			
			filepath = Framework.findFileOrThrow("checker_gamma.dds");
			pImageSet = DdsLoader.loadFromFile(filepath);

			g_gammaTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, g_gammaTexture);

			for (int mipmapLevel = 0; mipmapLevel < pImageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = pImageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();

				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8, dims.width, dims.height, 0, 
						GL12.GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData());
			}

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, pImageSet.getMipmapCount() - 1);

			glBindTexture(GL_TEXTURE_2D, 0);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}