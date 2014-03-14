package fcagnin.gltut.tut16;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glimg.ImageSet;
import fcagnin.jglsdk.glimg.ImageSet.Dimensions;
import fcagnin.jglsdk.glimg.ImageSet.SingleImage;
import fcagnin.jglsdk.glimg.StbLoader;
import fcagnin.jglsdk.glimg.TextureGenerator;
import fcagnin.jglsdk.glimg.TextureGenerator.OpenGLPixelTransferParams;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.gltut.framework.Framework;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part IV. Texturing
 * Chapter 16. Gamma and Textures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2016.html
 * <p/>
 * 1    - switch the top texture between lRGB and sRGB.
 * 2    - switch the bottom texture between lRGB and sRGB.
 *
 * @author integeruser
 */
public class GammaRamp extends LWJGLWindow {

	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut16/data/";

		new GammaRamp().start(500, 195);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();
		initializeVertexData();
		loadTextures();

		// Setup our Uniform Buffers
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer,
			0, ProjectionBlock.SIZE);

		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}


	@Override
	protected void update() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_1:
					useGammaCorrect[0] = !useGammaCorrect[0];
					if (useGammaCorrect[0]) {
						System.out.printf("Top:\tsRGB texture.\n");
					} else {
						System.out.printf("Top:\tlinear texture.\n");
					}
					break;

				case Keyboard.KEY_2:
					useGammaCorrect[1] = !useGammaCorrect[1];
					if (useGammaCorrect[1]) {
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

		glActiveTexture(GL_TEXTURE0 + gammaRampTextureUnit);
		glBindTexture(GL_TEXTURE_2D, textures[useGammaCorrect[0] ? 1 : 0]);
		glBindSampler(gammaRampTextureUnit, samplerObj);

		glBindVertexArray(vao);

		glUseProgram(noGammaProgram);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glBindTexture(GL_TEXTURE_2D, textures[useGammaCorrect[1] ? 1 : 0]);

		glUseProgram(gammaProgram);
		glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);

		glBindVertexArray(0);
		glUseProgram(0);

		glActiveTexture(GL_TEXTURE0 + gammaRampTextureUnit);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindSampler(gammaRampTextureUnit, 0);
	}


	@Override
	protected void reshape(int width, int height) {
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.translate(-1.0f, 1.0f, 0.0f);
		persMatrix.scale(2.0f / width, -2.0f / height, 1.0f);

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

	private final int gammaRampTextureUnit = 0;

	private int gammaProgram;
	private int noGammaProgram;
	private int vao;

	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);

	private void initializeProgram() {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "screenCoords.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	"textureNoGamma.frag"));
		noGammaProgram = Framework.createProgram(shaderList);

		shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "screenCoords.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	"textureGamma.frag"));
		gammaProgram = Framework.createProgram(shaderList);

		int projectionBlock = glGetUniformBlockIndex(noGammaProgram, "Projection");
		glUniformBlockBinding(noGammaProgram, projectionBlock, projectionBlockIndex);

		int colorTextureUnif = glGetUniformLocation(noGammaProgram, "colorTexture");
		glUseProgram(noGammaProgram);
		glUniform1i(colorTextureUnif, gammaRampTextureUnit);
		glUseProgram(0);

		projectionBlock = glGetUniformBlockIndex(gammaProgram, "Projection");
		glUniformBlockBinding(gammaProgram, projectionBlock, projectionBlockIndex);

		colorTextureUnif = glGetUniformLocation(gammaProgram, "colorTexture");
		glUseProgram(gammaProgram);
		glUniform1i(colorTextureUnif, gammaRampTextureUnit);
		glUseProgram(0);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final short[] vertexData = {
			 90, 80,				0,				0,
			 90, 16,				0,	(short) 65535,
			410, 80,	(short) 65535,				0,
			410, 16,	(short) 65535,	(short) 65535,

			 90, 176,				0,				0,
			 90, 112,				0,	(short) 65535,
			410, 176,	(short) 65535,				0,
			410, 112,	(short) 65535,	(short) 65535};

	private int dataBufferObject;


	private void initializeVertexData() {
		dataBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, dataBufferObject);

		ShortBuffer vertexDataBuffer = BufferUtils.createShortBuffer(vertexData.length);
		vertexDataBuffer.put(vertexData);
		vertexDataBuffer.flip();

		glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);

		vao = glGenVertexArrays();

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, dataBufferObject);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_UNSIGNED_SHORT, false, 8, 0);
		glEnableVertexAttribArray(5);
		glVertexAttribPointer(5, 2, GL_UNSIGNED_SHORT, true, 8, 4);

		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private boolean[] useGammaCorrect = {false, false};
	private int[] textures = new int[2];
	private int samplerObj;


	private void loadTextures() {
		textures[0] = glGenTextures();
		textures[1] = glGenTextures();

		try {
			String filepath = Framework.findFileOrThrow("gamma_ramp.png");
			ImageSet imageSet = StbLoader.loadFromFile(filepath);

			SingleImage image = imageSet.getImage(0, 0, 0);
			Dimensions imageDimensions = image.getDimensions();

			OpenGLPixelTransferParams pxTrans = TextureGenerator.getUploadFormatType(imageSet.getFormat(), 0);

			glBindTexture(GL_TEXTURE_2D, textures[0]);

			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, imageDimensions.width, imageDimensions.height, 0,
					pxTrans.format, pxTrans.type, image.getImageData());
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);

			glBindTexture(GL_TEXTURE_2D, textures[1]);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, imageDimensions.width, imageDimensions.height, 0,
				pxTrans.format, pxTrans.type, image.getImageData());
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);

			glBindTexture(GL_TEXTURE_2D, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		samplerObj = glGenSamplers();
		glSamplerParameteri(samplerObj, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(samplerObj, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(samplerObj, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(samplerObj, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final int projectionBlockIndex = 0;

	private int projectionUniformBuffer;


	private class ProjectionBlock extends BufferableData<FloatBuffer> {
		Mat4 cameraToClipMatrix;

		static final int SIZE = Mat4.SIZE;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
}