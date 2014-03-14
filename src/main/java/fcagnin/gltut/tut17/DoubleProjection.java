package fcagnin.gltut.tut17;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat3;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Quaternion;
import fcagnin.jglsdk.glm.Vec2;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.*;
import fcagnin.gltut.framework.Framework;
import fcagnin.gltut.framework.Mesh;
import fcagnin.gltut.framework.MousePole;
import fcagnin.gltut.framework.Scene;
import fcagnin.gltut.framework.Scene.SceneNode;
import fcagnin.gltut.framework.SceneBinders;
import fcagnin.gltut.framework.Timer;
import fcagnin.gltut.framework.SceneBinders.UniformIntBinder;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 *
 * Part IV. Texturing
 * Chapter 17. Spotlight on Textures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2017.html
 * @author integeruser
 *
 * W,A,S,D	- move the camera forward/backwards and left/right, relative to the camera's current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.
 * Q,E		- raise and lower the camera, relative to its current orientation.
 * 				Holding SHIFT with these keys will move in smaller increments.
 * SPACE	- reset the right camera back to a neutral view.
 * T		- toggle viewing of the current target point.
 * Y		- toggle depth clamping in the right camera.
 * P		- toggle pausing.
 *
 * LEFT	  CLICKING and DRAGGING			- rotate the left camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the left camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the left camera's up direction.
 * RIGHT  CLICKING and DRAGGING			- rotate the right camera horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL	- rotate the right camera flashlight horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT	- change the right camera's up direction.
 * WHEEL  SCROLLING						- move the left camera and the right camera  closer to it's target point or farther away.
 */
public class DoubleProjection extends LWJGLWindow {

	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut17/data/";

		new DoubleProjection().start(displayWidth, displayHeight);
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
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_STREAM_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer,
				0, ProjectionBlock.SIZE);

		try {
			loadAndSetupScene();
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
		}

		lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_STREAM_DRAW);

		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer,
				0, LightBlock.SIZE);

		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}


	@Override
	protected void update() {
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();

			if (eventButton != -1) {
				boolean pressed = Mouse.getEventButtonState();
				MousePole.forwardMouseButton(viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
				MousePole.forwardMouseButton(persViewPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();

				if (dWheel != 0) {
					MousePole.forwardMouseWheel(viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}

				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(viewPole, Mouse.getX(), Mouse.getY());
					MousePole.forwardMouseMotion(persViewPole, Mouse.getX(), Mouse.getY());
				}
			}
		}


		float lastFrameDuration = getLastFrameDuration() * 10 / 1000.0f;

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
					persViewPole.reset();
					break;

				case Keyboard.KEY_T:
					dDrawCameraPos = !dDrawCameraPos;
					break;

				case Keyboard.KEY_Y:
					depthClampProj = !depthClampProj;
					break;

				case Keyboard.KEY_P:
					timer.togglePause();
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
		timer.update(getElapsedTime());

		glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.applyMatrix(viewPole.calcMatrix());

		buildLights(modelMatrix.top());

		nodes.get(0).nodeSetOrient(Glm.rotate(new Quaternion(1.0f),
				360.0f * timer.getAlpha(), new Vec3(0.0f, 1.0f, 0.0f)));

		nodes.get(3).nodeSetOrient(Quaternion.mul(spinBarOrient, Glm.rotate(new Quaternion(1.0f),
				360.0f * timer.getAlpha(), new Vec3(0.0f, 0.0f, 1.0f))));

		Vec2 displaySize = new Vec2(displayWidth / 2, displayHeight);

		{
			MatrixStack persMatrix = new MatrixStack();
			persMatrix.perspective(60.0f, displaySize.x / displaySize.y, zNear, zFar);

			ProjectionBlock projData = new ProjectionBlock();
			projData.cameraToClipMatrix = persMatrix.top();

			glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
			glBufferData(GL_UNIFORM_BUFFER, projData.fillAndFlipBuffer(mat4Buffer), GL_STREAM_DRAW);
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
		}

		glViewport(0, 0, (int) displaySize.x, (int) displaySize.y);
		scene.render(modelMatrix.top());

		if (dDrawCameraPos) {
			modelMatrix.push();

			// Draw lookat point.
			modelMatrix.setIdentity();
			modelMatrix.translate(0.0f, 0.0f, -viewPole.getView().radius);
			modelMatrix.scale(0.5f);

			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			glUseProgram(unlitProg);
			glUniformMatrix4(unlitModelToCameraMatrixUnif, false,
					modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
			glUniform4f(unlitObjectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
			sphereMesh.render("flat");
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(unlitObjectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			sphereMesh.render("flat");

			modelMatrix.pop();
		}

		{
			MatrixStack persMatrix = new MatrixStack();
			persMatrix.applyMatrix(new Mat4(new Mat3(persViewPole.calcMatrix())));
			persMatrix.perspective(60.0f, displaySize.x / displaySize.y, zNear, zFar);

			ProjectionBlock projData = new ProjectionBlock();
			projData.cameraToClipMatrix = persMatrix.top();

			glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
			glBufferData(GL_UNIFORM_BUFFER, projData.fillAndFlipBuffer(mat4Buffer), GL_STREAM_DRAW);
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
		}

		if (!depthClampProj) {
			glDisable(GL_DEPTH_CLAMP);
		}

		glViewport((int) displaySize.x + (displayWidth % 2), 0, (int) displaySize.x, (int) displaySize.y);
		scene.render(modelMatrix.top());
		glEnable(GL_DEPTH_CLAMP);
	}


	@Override
	protected void reshape(int width, int height) {
		displayWidth = width;
		displayHeight = height;
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private static int displayWidth = 700;
	private static int displayHeight = 350;

	private final float zNear = 1.0f;
	private final float zFar = 1000.0f;

	private final FloatBuffer mat4Buffer		= BufferUtils.createFloatBuffer(Mat4.SIZE);
	private final FloatBuffer lightBlockBuffer	= BufferUtils.createFloatBuffer(LightBlock.SIZE);



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private int unlitProg;
	private int unlitModelToCameraMatrixUnif;
	private int unlitObjectColorUnif;


	private void loadAndSetupScene() {
		scene = new Scene("dp_scene.xml");

		nodes = new ArrayList<>();
		nodes.add(scene.findNode("cube"));
		nodes.add(scene.findNode("rightBar"));
		nodes.add(scene.findNode("leaningBar"));
		nodes.add(scene.findNode("spinBar"));

		lightNumBinder = new UniformIntBinder();
		SceneBinders.associateUniformWithNodes(nodes, lightNumBinder, "numberOfLights");
		SceneBinders.setStateBinderWithNodes(nodes, lightNumBinder);

		int unlit = scene.findProgram("p_unlit");
		sphereMesh = scene.findMesh("m_sphere");

		spinBarOrient = nodes.get(3).nodeGetOrient();
		unlitProg = unlit;
		unlitModelToCameraMatrixUnif = glGetUniformLocation(unlit, "modelToCameraMatrix");
		unlitObjectColorUnif = glGetUniformLocation(unlit, "objectColor");
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private Scene scene;
	private ArrayList<SceneNode> nodes;
	private Timer timer = new Timer(Timer.Type.LOOP, 10.0f);

	private Quaternion spinBarOrient;

	private boolean depthClampProj = true;

	private Mesh sphereMesh;
	private boolean dDrawCameraPos;


	////////////////////////////////
	// View setup.
	private ViewData initialView = new ViewData(
			new Vec3(0.0f, 0.0f, 0.0f),
			new Quaternion(0.909845f, 0.16043f, -0.376867f, -0.0664516f),
			25.0f,
			0.0f);

	private ViewScale initialViewScale = new ViewScale(
			5.0f, 70.0f,
			2.0f, 0.5f,
			2.0f, 0.5f,
			90.0f / 250.0f);


	private ViewData initPersView = new ViewData(
			new Vec3(0.0f, 0.0f, 0.0f),
			new Quaternion(1.0f, 0.0f, 0.0f, 0.0f),
			5.0f,
			0.0f);

	private ViewScale initPersViewScale = new ViewScale(
			0.05f, 10.0f,
			0.1f, 0.05f,
			4.0f, 1.0f,
			90.0f / 250.0f);


	private ViewPole viewPole 		= new ViewPole(initialView, initialViewScale, MouseButtons.MB_LEFT_BTN);
	private ViewPole persViewPole 	= new ViewPole(initPersView, initPersViewScale, MouseButtons.MB_RIGHT_BTN);



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



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private static final int MAX_NUMBER_OF_LIGHTS = 4;

	private final int lightBlockIndex = 1;

	private int lightUniformBuffer;
	private UniformIntBinder lightNumBinder;


	private class PerLight extends BufferableData<FloatBuffer> {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;

		static final int SIZE = Vec4.SIZE + Vec4.SIZE;

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

		static final int SIZE = Vec4.SIZE + ((1 + 1 + 2) * FLOAT_SIZE) + PerLight.SIZE * MAX_NUMBER_OF_LIGHTS;

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
		lightData.ambientIntensity 	= new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
		lightData.lightAttenuation 	= 1.0f / (5.0f * 5.0f);
		lightData.maxIntensity 		= 3.0f;

		lightData.lights[0] = new PerLight();
		lightData.lights[0].lightIntensity = new Vec4(2.0f, 2.0f, 2.5f, 1.0f);
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(camMatrix,
				Glm.normalize(new Vec4(-0.2f, 0.5f, 0.5f, 0.0f)));

		lightData.lights[1] = new PerLight();
		lightData.lights[1].lightIntensity = new Vec4(3.5f, 6.5f, 3.0f, 1.0f).scale(1.2f);
		lightData.lights[1].cameraSpaceLightPos = Mat4.mul(camMatrix,
				new Vec4(5.0f, 6.0f, 0.5f, 1.0f));

		lightNumBinder.setValue(2);

		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, lightData.fillAndFlipBuffer(lightBlockBuffer), GL_STREAM_DRAW);
	}
}