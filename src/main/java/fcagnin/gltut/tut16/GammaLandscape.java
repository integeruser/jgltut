package fcagnin.gltut.tut16;

import java.nio.FloatBuffer;
import java.util.ArrayList;

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

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glimg.DdsLoader;
import fcagnin.jglsdk.glimg.ImageSet;
import fcagnin.jglsdk.glimg.ImageSet.Dimensions;
import fcagnin.jglsdk.glimg.ImageSet.SingleImage;
import fcagnin.jglsdk.glimg.TextureGenerator;
import fcagnin.jglsdk.glimg.TextureGenerator.OpenGLPixelTransferParams;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Quaternion;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.*;
import fcagnin.gltut.tut16.LightEnv.LightBlock;
import fcagnin.gltut.framework.Framework;
import fcagnin.gltut.framework.Mesh;
import fcagnin.gltut.framework.MousePole;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 *
 * IV. Texturing
 * 16. Gamma and Textures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2016.html
 * @author integeruser
 *
 * W,A,S,D	- move the cameras forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.
 * Q,E		- raise and lower the camera, relative to its current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.
 * SPACE	- toggle non-shader-based gamma correction.
 * -,=		- rewind/jump forward time by 0.5 second (of real-time).
 * T		- toggle viewing the look-at point.
 * P		- toggle pausing.
 * 1,2		- select linear mipmap filtering and anisotropic filtering (using the maximum possible anisotropy).
 *
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away.
 */
public class GammaLandscape extends LWJGLWindow {

	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut16/data/";

		new GammaLandscape().start(700, 700);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		try {
			lightEnv = new LightEnv("LightEnv.xml");

			initializePrograms();

			terrain = 	new Mesh("terrain.xml");
			sphere = 	new Mesh("UnitSphere.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
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
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer,
			0, ProjectionBlock.SIZE);

		lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_STREAM_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer,
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
				boolean pressed = Mouse.getEventButtonState();
				MousePole.forwardMouseButton(viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();

				if (dWheel != 0) {
					MousePole.forwardMouseWheel(viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}

				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(viewPole, Mouse.getX(), Mouse.getY());
				}
			}
		}


		float lastFrameDuration = getLastFrameDuration() * 20 / 1000.0f;

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			viewPole.charPress(Keyboard.KEY_W, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			viewPole.charPress(Keyboard.KEY_S, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			viewPole.charPress(Keyboard.KEY_D, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			viewPole.charPress(Keyboard.KEY_A, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			viewPole.charPress(Keyboard.KEY_E, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			viewPole.charPress(Keyboard.KEY_Q, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), lastFrameDuration);
		}


		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					useGammaDisplay = !useGammaDisplay;
					break;

				case Keyboard.KEY_MINUS:
					lightEnv.rewindTime(1.0f);
					break;

				case Keyboard.KEY_EQUALS:
					lightEnv.fastForwardTime(1.0f);
					break;

				case Keyboard.KEY_T:
					drawCameraPos = !drawCameraPos;
					break;

				case Keyboard.KEY_P:
					lightEnv.togglePause();
					break;

				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}


				if (Keyboard.KEY_1 <= Keyboard.getEventKey() && Keyboard.getEventKey() <= Keyboard.KEY_9) {
					int number = Keyboard.getEventKey() - Keyboard.KEY_1;
					if (number < NUM_SAMPLERS) {
						currSampler = number;
					}
				}
			}
		}
	}


	@Override
	protected void display() {
		if (useGammaDisplay) {
			glEnable(GL_FRAMEBUFFER_SRGB);
		} else {
			glDisable(GL_FRAMEBUFFER_SRGB);
		}

		lightEnv.updateTime(getElapsedTime());

		Vec4 bgColor = lightEnv.getBackgroundColor();
		glClearColor(bgColor.x, bgColor.y, bgColor.z, bgColor.w);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.applyMatrix(viewPole.calcMatrix());

		LightBlock lightData = lightEnv.getLightBlock(viewPole.calcMatrix());

		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, lightData.fillAndFlipBuffer(lightBlockBuffer), GL_STREAM_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		modelMatrix.push();
		modelMatrix.rotateX(-90.0f);

		glUseProgram(progStandard.theProgram);
		glUniformMatrix4(progStandard.modelToCameraMatrixUnif, false,
			modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
		glUniform1i(progStandard.numberOfLightsUnif, lightEnv.getNumLights());

		glActiveTexture(GL_TEXTURE0 + colorTexUnit);
		glBindTexture(GL_TEXTURE_2D, linearTexture);
		glBindSampler(colorTexUnit, samplers[currSampler]);

		terrain.render("lit-tex");

		glBindSampler(colorTexUnit, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		glUseProgram(0);

		modelMatrix.pop();

		// Render the sun
		{
			modelMatrix.push();

			Vec3 sunlightDir = new Vec3(lightEnv.getSunlightDirection());
			modelMatrix.translate(sunlightDir.scale(500.0f));
			modelMatrix.scale(30.0f, 30.0f, 30.0f);

			glUseProgram(progUnlit.theProgram);
			glUniformMatrix4(progUnlit.modelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

			Vec4 lightColor = lightEnv.getSunlightScaledIntensity();
			glUniform4(progUnlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
			sphere.render("flat");

			modelMatrix.pop();
		}

		// Draw lights
		for (int light = 0; light < lightEnv.getNumPointLights(); light++) {
			modelMatrix.push();

			modelMatrix.translate(lightEnv.getPointLightWorldPos(light));

			glUseProgram(progUnlit.theProgram);
			glUniformMatrix4(progUnlit.modelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(mat4Buffer));

			Vec4 lightColor = lightEnv.getPointLightScaledIntensity(light);
			glUniform4(progUnlit.objectColorUnif, lightColor.fillAndFlipBuffer(vec4Buffer));
			sphere.render("flat");

			modelMatrix.pop();
		}

		if (drawCameraPos) {
			modelMatrix.push();

			// Draw lookat point.
			modelMatrix.setIdentity();
			modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);

			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			glUseProgram(progUnlit.theProgram);
			glUniformMatrix4(progUnlit.modelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			glUniform4f(progUnlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
			sphere.render("flat");
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(progUnlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			sphere.render("flat");

			modelMatrix.pop();
		}
	}


	@Override
	protected void reshape(int width, int height) {
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(60.0f, (width / (float) height), zNear, zFar);

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

	private final int lightBlockIndex = 1;
	private final int colorTexUnit = 0;

	private int lightUniformBuffer;
	private int linearTexture;
	private float zNear = 1.0f;
	private float zFar = 1000.0f;

	private FloatBuffer vec4Buffer 			= BufferUtils.createFloatBuffer(Vec4.SIZE);
	private FloatBuffer mat4Buffer 			= BufferUtils.createFloatBuffer(Mat4.SIZE);
	private FloatBuffer lightBlockBuffer 	= BufferUtils.createFloatBuffer(LightBlock.SIZE);


	private void initializePrograms() {
		progStandard = loadProgram("PNT.vert", "litTexture.frag");
		progUnlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private ProgramData progStandard;
	private UnlitProgData progUnlit;


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


	private ProgramData loadProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.numberOfLightsUnif = glGetUniformLocation(data.theProgram, "numberOfLights");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		glUniformBlockBinding(data.theProgram, lightBlock, lightBlockIndex);

		int colorTextureUnif = glGetUniformLocation(data.theProgram, "diffuseColorTex");
		glUseProgram(data.theProgram);
		glUniform1i(colorTextureUnif, colorTexUnit);
		glUseProgram(0);

		return data;
	}

	private UnlitProgData loadUnlitProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		UnlitProgData data = new UnlitProgData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.objectColorUnif = glGetUniformLocation(data.theProgram, "objectColor");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		return data;
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final int NUM_SAMPLERS = 2;

	private Mesh terrain;
	private Mesh sphere;

	private LightEnv lightEnv;

	private boolean useGammaDisplay = true;
	private boolean drawCameraPos;
	private int[] samplers = new int[NUM_SAMPLERS];
	private int currSampler;


	////////////////////////////////
	// View setup.
	private ViewData initialView = new ViewData(
		new Vec3(-60.257084f, 10.947238f, 62.636356f),
		new Quaternion(-0.972817f, -0.099283f, -0.211198f, -0.020028f),
		30.0f,
		0.0f);

	private ViewScale initialViewScale = new ViewScale(
			5.0f, 90.0f,
			2.0f, 0.5f,
			4.0f, 1.0f,
			90.0f / 250.0f);


	private ViewPole viewPole = new ViewPole(initialView, initialViewScale, MouseButtons.MB_LEFT_BTN);


	private void loadTextures() {
		try	{
			String filepath = Framework.findFileOrThrow("terrain_tex.dds");
			ImageSet imageSet = DdsLoader.loadFromFile(filepath);

			linearTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, linearTexture);

			OpenGLPixelTransferParams xfer = TextureGenerator.getUploadFormatType(imageSet.getFormat(), 0);

			for (int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
				Dimensions imageDimensions = image.getDimensions();

				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8_ALPHA8, imageDimensions.width, imageDimensions.height, 0,
						xfer.format, xfer.type, image.getImageData());
			}

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);

			glBindTexture(GL_TEXTURE_2D, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void createSamplers() {
		for (int samplerIndex = 0; samplerIndex < NUM_SAMPLERS; samplerIndex++) {
			samplers[samplerIndex] = glGenSamplers();
			glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_S, GL_REPEAT);
			glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_T, GL_REPEAT);
		}

		// Linear mipmap linear
		glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

		// Max anisotropic
		float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);

		glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(samplers[1], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
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