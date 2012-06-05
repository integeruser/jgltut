package rosick.mckesson.IV.tut15;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL12;

import rosick.LWJGLWindow;
import rosick.PortingUtils.BufferableData;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.framework.Timer;
import rosick.jglsdk.glimg.Dds;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * IV. Texturing
 * 15. Many Images
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2015.html
 * @author integeruser
 * 
 * SPACE		- toggles between loaded/constructed texture.
 * Y			- toggles between plane/corridor mesh.
 * P			- toggles pausing on/off.
 * 1,2,3,4,5,6	- switch filtering technique.
 */
public class ManyImages01 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		new ManyImages01().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/IV/tut15/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class ProgramData {
		int theProgram;

		int modelToCameraMatrixUnif;
	}
		
	
	private final int g_projectionBlockIndex = 0;
	private final int g_colorTexUnit = 0;

	private ProgramData g_program;
		
	private int g_projectionUniformBuffer;
	private int g_checkerTexture;
	private int g_mipmapTestTexture;
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
		g_program = loadProgram(TUTORIAL_DATAPATH + "PT.vert", TUTORIAL_DATAPATH + "Tex.frag");
	}
	
	
	@Override
	protected void init() {	
		initializePrograms();

		try {
			g_pCorridor = 	new Mesh(TUTORIAL_DATAPATH + "Corridor.xml");
			g_pPlane = 		new Mesh(TUTORIAL_DATAPATH + "BigPlane.xml");
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
		loadMipmapTexture();
		createSamplers();
	}
	

	@Override
	protected void update() {		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					g_useMipmapTexture = !g_useMipmapTexture;
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
						System.out.printf("Sampler: %s\n", g_samplerNames[number]);
						g_currSampler = number;
					}
				}
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

		glUseProgram(g_program.theProgram);
		glUniformMatrix4(g_program.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

		glActiveTexture(GL_TEXTURE0 + g_colorTexUnit);
		glBindTexture(GL_TEXTURE_2D, 
				g_useMipmapTexture ? g_mipmapTestTexture : g_checkerTexture);		
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
			
	
	private final int NUM_SAMPLERS = 6;

	private final byte mipmapColors[] = {
			(byte) 0xFF, (byte) 0xFF, 		 0x00,
			(byte) 0xFF, 		0x00, (byte) 0xFF,
				   0x00, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, 		0x00, 		 0x00,
				   0x00, (byte) 0xFF, 		 0x00,
				   0x00, 		0x00, (byte) 0xFF,
				   0x00, 		0x00, 		 0x00,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF
	};
	
	private final String g_samplerNames[] = {
			"Nearest",
			"Linear",
			"Linear with nearest mipmaps",
			"Linear with linear mipmaps",
			"Low anisotropic",
			"Max anisotropic"
	};
	
	private Mesh g_pPlane;
	private Mesh g_pCorridor;

	private Timer g_camTimer = new Timer(Timer.Type.TT_LOOP, 5.0f);

	private boolean g_useMipmapTexture;
	private boolean g_drawCorridor;
	private int g_samplers[] = new int[NUM_SAMPLERS];
	private int g_currSampler;


	private void createSamplers() {		
		for (int samplerIx = 0; samplerIx < NUM_SAMPLERS; samplerIx++) {
			g_samplers[samplerIx] = glGenSamplers();
			glSamplerParameteri(g_samplers[samplerIx], GL_TEXTURE_WRAP_S, GL_REPEAT);
			glSamplerParameteri(g_samplers[samplerIx], GL_TEXTURE_WRAP_T, GL_REPEAT);
		}

		// Nearest
		glSamplerParameteri(g_samplers[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(g_samplers[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		// Linear
		glSamplerParameteri(g_samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(g_samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		
		// Linear mipmap Nearest
		glSamplerParameteri(g_samplers[2], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(g_samplers[2], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);

		// Linear mipmap linear
		glSamplerParameteri(g_samplers[3], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(g_samplers[3], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

		// Low anisotropic
		glSamplerParameteri(g_samplers[4], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(g_samplers[4], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(g_samplers[4], GL_TEXTURE_MAX_ANISOTROPY_EXT, 4.0f);

		// Max anisotropic
		float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);

		System.out.printf("Maximum anisotropy: %f\n", maxAniso);

		glSamplerParameteri(g_samplers[5], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(g_samplers[5], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(g_samplers[5], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
	}
	
	
	private void fillWithColor(ArrayList<Byte> buffer, byte red, byte green, byte blue, int width, int height) {
		int numTexels = width * height;

		for (int i = 0; i < numTexels * 3; i++) {
			buffer.add(red);
			buffer.add(green);
			buffer.add(blue);
		}
	}
	
	
	private void loadMipmapTexture() {
		g_mipmapTestTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, g_mipmapTestTexture);

		int oldAlign = glGetInteger(GL_UNPACK_ALIGNMENT);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		for (int mipmapLevel = 0; mipmapLevel < 8; mipmapLevel++) {
			int width = 128 >> mipmapLevel;
			int height = 128 >> mipmapLevel;
			ArrayList<Byte> buffer = new ArrayList<>();

			final int pCurrColor = mipmapLevel * 3;
			fillWithColor(buffer, mipmapColors[pCurrColor], mipmapColors[pCurrColor + 1], mipmapColors[pCurrColor + 2], width, height);

			ByteBuffer tempByteBuffer = BufferUtils.createByteBuffer(buffer.size());
			for (Byte b : buffer) {
				tempByteBuffer.put((byte) b);
			}
			tempByteBuffer.flip();
			
			glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_RGB8, width, height, 0, 
					GL_RGB, GL_UNSIGNED_BYTE, tempByteBuffer);
		}

		glPixelStorei(GL_UNPACK_ALIGNMENT, oldAlign);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 7);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	private void loadCheckerTexture() {
		try	{
			ImageSet pImageSet = Dds.loadFromFile(TUTORIAL_DATAPATH + "checker.dds");

			g_checkerTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, g_checkerTexture);
			
			for (int mipmapLevel = 0; mipmapLevel < pImageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = pImageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();

				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_RGB8, dims.m_width, dims.m_height, 0,
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