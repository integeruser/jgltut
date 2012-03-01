package rosick.mckesson.IV.tut16;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.LWJGLWindow;
import rosick.PortingUtils.BufferableData;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * IV. Texturing
 * 16. Gamma and Textures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2016.html
 * @author integeruser
 * 
 * 1 		- switches the top texture between lRGB and sRGB.
 * 2		- switches the bottom texture between lRGB and sRGB.
 */
public class GammaRamp01 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		new GammaRamp01().start(500, 195);
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/IV/tut16/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final int g_projectionBlockIndex = 0;
	private final int g_gammaRampTextureUnit = 0;
	
	private final short vertexData[] = {
			 90, 80,				0,				0,
			 90, 16,				0,	(short) 65535,
			410, 80,	(short) 65535,				0,
			410, 16,	(short) 65535,	(short) 65535,
			
			 90, 176,				0,				0,
			 90, 112,				0,	(short) 65535,
			410, 176,	(short) 65535,				0,
			410, 112,	(short) 65535,	(short) 65535,
	};
	
	private int g_noGammaProgram;
	private int g_gammaProgram;
	private int g_dataBufferObject;
	private int g_projectionUniformBuffer;
	private int g_vao;
	
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void initializeProgram() {
		int vertexShader = Framework.loadShader(GL_VERTEX_SHADER, TUTORIAL_DATAPATH + "screenCoords.vert");
		ArrayList<Integer> shaderList = new ArrayList<>();
		
		shaderList.add(vertexShader);
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	TUTORIAL_DATAPATH + "textureNoGamma.frag"));

		g_noGammaProgram = Framework.createProgram(shaderList);
		glDeleteShader(shaderList.get(1));

		shaderList.remove(1);
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	TUTORIAL_DATAPATH + "textureGamma.frag"));

		g_gammaProgram = Framework.createProgram(shaderList);
		glDeleteShader(shaderList.get(1));
		glDeleteShader(vertexShader);
		
		int projectionBlock = glGetUniformBlockIndex(g_noGammaProgram, "Projection");
		glUniformBlockBinding(g_noGammaProgram, projectionBlock, g_projectionBlockIndex);

		int colorTextureUnif = glGetUniformLocation(g_noGammaProgram, "colorTexture");
		glUseProgram(g_noGammaProgram);
		glUniform1i(colorTextureUnif, g_gammaRampTextureUnit);
		glUseProgram(0);

		projectionBlock = glGetUniformBlockIndex(g_gammaProgram, "Projection");
		glUniformBlockBinding(g_gammaProgram, projectionBlock, g_projectionBlockIndex);

		colorTextureUnif = glGetUniformLocation(g_gammaProgram, "colorTexture");
		glUseProgram(g_gammaProgram);
		glUniform1i(colorTextureUnif, g_gammaRampTextureUnit);
		glUseProgram(0);
	}
	
	private void initializeVertexData() {
		g_dataBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, g_dataBufferObject);
		
		ShortBuffer vertexDataBuffer = BufferUtils.createShortBuffer(vertexData.length);
		vertexDataBuffer.put(vertexData);
		vertexDataBuffer.flip();
		
		glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);

		g_vao = glGenVertexArrays();

		glBindVertexArray(g_vao);
		glBindBuffer(GL_ARRAY_BUFFER, g_dataBufferObject);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_UNSIGNED_SHORT, false, 8, 0);
		glEnableVertexAttribArray(5);
		glVertexAttribPointer(5, 2, GL_UNSIGNED_SHORT, true, 8, 4);

		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexData();
		loadTextures();

		//Setup our Uniform Buffers
		g_projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer,
			0, ProjectionBlock.SIZE);

		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	

	@Override
	protected void update() {		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_1:
					g_useGammaCorrect[0] = !g_useGammaCorrect[0];
					
					if (g_useGammaCorrect[0]) {
						System.out.printf("Top:\tsRGB texture.\n");
					} else {
						System.out.printf("Top:\tlinear texture.\n");
					}
					break;
					
				case Keyboard.KEY_2:
					g_useGammaCorrect[1] = !g_useGammaCorrect[1];
					
					if (g_useGammaCorrect[1]) {
						System.out.printf("Bottom:\tsRGB texture.\n");
					} else {
						System.out.printf("Bottom:\tlinear texture.\n");
					}
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
		glClearColor(0.0f, 0.5f, 0.3f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glActiveTexture(GL_TEXTURE0 + g_gammaRampTextureUnit);
		glBindTexture(GL_TEXTURE_2D, g_textures[g_useGammaCorrect[0] ? 1 : 0]);
		glBindSampler(g_gammaRampTextureUnit, g_samplerObj);

		glBindVertexArray(g_vao);

		glUseProgram(g_noGammaProgram);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glBindTexture(GL_TEXTURE_2D, g_textures[g_useGammaCorrect[1] ? 1 : 0]);

		glUseProgram(g_gammaProgram);
		glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);

		glBindVertexArray(0);
		glUseProgram(0);

		glActiveTexture(GL_TEXTURE0 + g_gammaRampTextureUnit);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindSampler(g_gammaRampTextureUnit, 0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.translate(-1.0f, 1.0f, 0.0f);
		persMatrix.scale(2.0f / width, -2.0f / height, 1.0f);
		
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
			
	
	private boolean g_useGammaCorrect[] = {false, false};
	private int g_textures[] = new int[2];
	private int g_samplerObj;

	
	private void loadTextures() {
		g_textures[0] = glGenTextures();
		g_textures[1] = glGenTextures();
		
		try {
			/* Not in the original tutorial, needed for png loading */
			BufferedImage bufferedImage = ImageIO.read(ClassLoader.class.getResourceAsStream(TUTORIAL_DATAPATH + "gamma_ramp.png"));			
			ByteBuffer textureBuffer = byteBufferFromBufferedImage(bufferedImage);
			/*                                                      */

			glBindTexture(GL_TEXTURE_2D, g_textures[0]);
		    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, bufferedImage.getWidth(), bufferedImage.getHeight(), 0, 
		    		GL_RGB, GL_UNSIGNED_BYTE, textureBuffer);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);

			glBindTexture(GL_TEXTURE_2D, g_textures[1]);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, bufferedImage.getWidth(), bufferedImage.getHeight(), 0,
					GL_RGB, GL_UNSIGNED_BYTE, textureBuffer);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
		
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		g_samplerObj = glGenSamplers();
		glSamplerParameteri(g_samplerObj, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(g_samplerObj, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(g_samplerObj, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(g_samplerObj, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	}
	
		
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	protected ByteBuffer byteBufferFromBufferedImage(BufferedImage image) {
		int numComponents = image.getColorModel().getNumComponents();
		
		// Define an image type for the following BufferedImage appropriate for the number of colour components.
		int type = 0;

		if (numComponents == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		} else {
			type = BufferedImage.TYPE_4BYTE_ABGR;
		}
		
		// Create a BufferedImage of the appropriate type.
		BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), type);
		Graphics2D g = bi.createGraphics();
		g.scale(1, -1);
		g.drawImage(image, 0, -image.getHeight(), null);
		
		// Create an array with a length that is dependent on the number of colour components.
		byte[] data = new byte[numComponents * image.getWidth() * image.getHeight()];
		bi.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), data); 				// Copy image to array data
		
		ByteBuffer pixels = BufferUtils.createByteBuffer(data.length); 
		pixels.put(data); 
		pixels.rewind();

		return pixels;
	}
}