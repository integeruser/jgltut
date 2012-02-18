package rosick.mckesson.III.tut10;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.ByteBuffer;
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
import rosick.jglsdk.framework.Timer;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec2;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 10. Plane Lights
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2010.html
 * @author integeruser
 * 
 * I,J,K,L  - control the light's position. Holding LEFT_SHIFT with these keys will move in smaller increments.
 * SPACE	- toggles between drawing the uncolored cylinder and the colored one.
 * O,U		- increase/decrease the attenuation constant.
 * Y 		- toggles the drawing of the light source.
 * T 		- toggles between the scaled and unscaled cylinder.
 * B 		- toggles the light's rotation on/off.
 * H 		- swaps between the linear and quadratic interpolation functions.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING			- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT	- spin the object.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 */
public class FragmentAttenuation03 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		new FragmentAttenuation03().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/III/tut10/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class ProgramData {
		int theProgram;
		
		int modelToCameraMatrixUnif;
		
		int lightIntensityUnif;
		int ambientIntensityUnif;

		int normalModelToCameraMatrixUnif;
		int cameraSpaceLightPosUnif;
		int lightAttenuationUnif;
		int bUseRSquareUnif;
	}
	
	private class UnlitProgData {
		int theProgram;
		
		int objectColorUnif;
		int modelToCameraMatrixUnif;
	}
		
	
	private final int g_projectionBlockIndex = 2;
	private final int g_unprojectionBlockIndex = 1;

	private ProgramData g_FragWhiteDiffuseColor;
	private ProgramData g_FragVertexDiffuseColor;
	
	private UnlitProgData g_Unlit;
	
	private int g_projectionUniformBuffer;
	private int g_unprojectionUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;

	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer9 	= BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempFloatBuffer16 	= BufferUtils.createFloatBuffer(16);

	
	
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
		data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
		data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "ambientIntensity");

		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
		data.cameraSpaceLightPosUnif = glGetUniformLocation(data.theProgram, "cameraSpaceLightPos");
		data.lightAttenuationUnif = glGetUniformLocation(data.theProgram, "lightAttenuation");
		data.bUseRSquareUnif = glGetUniformLocation(data.theProgram, "bUseRSquare");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);
		int unprojectionBlock = glGetUniformBlockIndex(data.theProgram, "UnProjection");
		glUniformBlockBinding(data.theProgram, unprojectionBlock, g_unprojectionBlockIndex);

		return data;
	}
	
	private void initializePrograms() {	
		g_FragWhiteDiffuseColor =	loadLitProgram(TUTORIAL_DATAPATH + "FragLightAtten_PN.vert",	TUTORIAL_DATAPATH + "FragLightAtten.frag");		
		g_FragVertexDiffuseColor = 	loadLitProgram(TUTORIAL_DATAPATH + "FragLightAtten_PCN.vert", 	TUTORIAL_DATAPATH + "FragLightAtten.frag");
		
		g_Unlit = loadUnlitProgram(TUTORIAL_DATAPATH + "PosTransform.vert", TUTORIAL_DATAPATH + "UniformColor.frag");
	}
	
	
	@Override
	protected void init() {
		initializePrograms();
		
		try {
			g_pCylinderMesh = new Mesh(TUTORIAL_DATAPATH + "UnitCylinder.xml");
			g_pPlaneMesh 	= new Mesh(TUTORIAL_DATAPATH + "LargePlane.xml");
			g_pCubeMesh 	= new Mesh(TUTORIAL_DATAPATH + "UnitCube.xml");
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
		
		g_projectionUniformBuffer 	= glGenBuffers();	  
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		g_unprojectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_unprojectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, UnProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer, 0, ProjectionBlock.SIZE);
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, g_unprojectionBlockIndex, g_unprojectionUniformBuffer, 0, UnProjectionBlock.SIZE);
				
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
					MousePole.forwardMouseButton(g_objtPole, eventButton, true, Mouse.getX(), Mouse.getY());	
				} else {
					// Mouse up
					MousePole.forwardMouseButton(g_viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseButton(g_objtPole, eventButton, false, Mouse.getX(), Mouse.getY());
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(g_viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
					MousePole.forwardMouseWheel(g_objtPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(g_viewPole, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseMotion(g_objtPole, Mouse.getX(), Mouse.getY());
				}
			}
		}
		
		
		float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightRadius -= 0.05f * lastFrameDuration;
			} else {
				g_fLightRadius -= 0.2f * lastFrameDuration;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightRadius += 0.05f * lastFrameDuration;
			} else {
				g_fLightRadius += 0.2f * lastFrameDuration;
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightHeight += 0.05f * lastFrameDuration;
			} else {
				g_fLightHeight += 0.2f * lastFrameDuration;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_fLightHeight -= 0.05f * lastFrameDuration;
			} else {
				g_fLightHeight -= 0.2f * lastFrameDuration;
			}
		}
		
		
		if (g_fLightRadius < 0.2f) {
			g_fLightRadius = 0.2f;
		}
		
		if (g_fLightAttenuation < 0.1f) {
			g_fLightAttenuation = 0.1f;
		}
		

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				boolean bChangedAtten = false;

				
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					g_bDrawColoredCyl = !g_bDrawColoredCyl;
					break;
					
				case Keyboard.KEY_O:
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						g_fLightAttenuation *= 1.1f;
					} else {
						g_fLightAttenuation *= 1.5f;
					}
					
					bChangedAtten = true;
					break;
					
				case Keyboard.KEY_U:
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						g_fLightAttenuation /= 1.1f; 
					} else {
						g_fLightAttenuation /= 1.5f; 
					}
					
					bChangedAtten = true;
					break;
				
				case Keyboard.KEY_Y:
					g_bDrawLight = !g_bDrawLight;
					break;
					
				case Keyboard.KEY_T:
					g_bScaleCyl = !g_bScaleCyl;
					break;
					
				case Keyboard.KEY_B:
					g_LightTimer.togglePause();
					break;
					
				case Keyboard.KEY_H:
					g_bUseRSquare = !g_bUseRSquare;
					
					if (g_bUseRSquare) {
						System.out.printf("Inverse Squared Attenuation\n");
					} else {
						System.out.printf("Plain Inverse Attenuation\n");
					}	
					break;
					
				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}
				
				
				if (bChangedAtten) {
					System.out.printf("Atten: %f\n", g_fLightAttenuation);
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		g_LightTimer.update((float) getElapsedTime());
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		modelMatrix.clear();
		modelMatrix.setMatrix(g_viewPole.calcMatrix());

		final Vec4 worldLightPos = calcLightPosition();
		final Vec4 lightPosCameraSpace = Mat4.mul(modelMatrix.top(), worldLightPos);
		
		glUseProgram(g_FragWhiteDiffuseColor.theProgram);
		glUniform4f(g_FragWhiteDiffuseColor.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(g_FragWhiteDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUniform3(g_FragWhiteDiffuseColor.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(tempFloatBuffer4));
		glUniform1f(g_FragWhiteDiffuseColor.lightAttenuationUnif, g_fLightAttenuation);
		glUniform1i(g_FragWhiteDiffuseColor.bUseRSquareUnif, g_bUseRSquare ? 1 : 0);

		glUseProgram(g_FragVertexDiffuseColor.theProgram);
		glUniform4f(g_FragVertexDiffuseColor.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(g_FragVertexDiffuseColor.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUniform3(g_FragVertexDiffuseColor.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(tempFloatBuffer4));
		glUniform1f(g_FragVertexDiffuseColor.lightAttenuationUnif, g_fLightAttenuation);
		glUniform1i(g_FragVertexDiffuseColor.bUseRSquareUnif, g_bUseRSquare ? 1 : 0);
		glUseProgram(0);
		
		{
			modelMatrix.push();

			// Render the ground plane.
			{
				modelMatrix.push();

				Mat3 normMatrix = new Mat3(modelMatrix.top());
				normMatrix = Glm.transpose(Glm.inverse(normMatrix));
				
				glUseProgram(g_FragWhiteDiffuseColor.theProgram);
				glUniformMatrix4(g_FragWhiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

				glUniformMatrix3(g_FragWhiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));		
				g_pPlaneMesh.render();
				glUseProgram(0);
				
				modelMatrix.pop();
			}

			// Render the Cylinder
			{
				modelMatrix.push();
	
				modelMatrix.applyMatrix(g_objtPole.calcMatrix());

				if (g_bScaleCyl) {
					modelMatrix.scale(1.0f, 1.0f, 0.2f);
				}
				
				Mat3 normMatrix = new Mat3(modelMatrix.top());
				normMatrix = Glm.transpose(Glm.inverse(normMatrix));
						
				if (g_bDrawColoredCyl) {
					glUseProgram(g_FragVertexDiffuseColor.theProgram);
					glUniformMatrix4(g_FragVertexDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
					
					glUniformMatrix3(g_FragVertexDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));		
					g_pCylinderMesh.render("lit-color");
				} else {
					glUseProgram(g_FragWhiteDiffuseColor.theProgram);
					glUniformMatrix4(g_FragWhiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
					
					glUniformMatrix3(g_FragWhiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));
					g_pCylinderMesh.render("lit");
				}
				glUseProgram(0);
				
				modelMatrix.pop();
			}
			
			// Render the light
			if (g_bDrawLight) {
				modelMatrix.push();

				modelMatrix.translate(worldLightPos.x, worldLightPos.y, worldLightPos.z);
				modelMatrix.scale(0.1f, 0.1f, 0.1f);

				glUseProgram(g_Unlit.theProgram);
				glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
				glUniform4f(g_Unlit.objectColorUnif, 0.8078f, 0.8706f, 0.9922f, 1.0f);
				g_pCubeMesh.render("flat");
				
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

		UnProjectionBlock unprojData = new UnProjectionBlock();
		unprojData.clipToCameraMatrix = Glm.inverse(persMatrix.top());
		unprojData.windowSize = new Vec2(width, height);

		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(tempFloatBuffer16));
		glBindBuffer(GL_UNIFORM_BUFFER, g_unprojectionUniformBuffer);	
		glBufferSubData(GL_UNIFORM_BUFFER, 0, unprojData.fillAndFlipBuffer(BufferUtils.createByteBuffer(18 * FLOAT_SIZE)));
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
		
	private class UnProjectionBlock extends BufferableData<ByteBuffer> {
		Mat4 clipToCameraMatrix;
		Vec2 windowSize;
		
		static final int SIZE = 18 * FLOAT_SIZE;
				
		@Override
		public ByteBuffer fillBuffer(ByteBuffer buffer) {
			for (float f : clipToCameraMatrix.get()) {
				buffer.putFloat(f);
			}
			
			// The shader uses an int vector.
			buffer.putInt((int) windowSize.x);												
			buffer.putInt((int) windowSize.y);												

			return buffer;
		}
	}
	
	
	private Mesh g_pCylinderMesh;
	private Mesh g_pPlaneMesh;
	private Mesh g_pCubeMesh;
	
	private Timer g_LightTimer = new Timer(Timer.Type.TT_LOOP, 5.0f);

	private boolean g_bDrawColoredCyl;
	private boolean g_bDrawLight;
	private boolean g_bScaleCyl;
	private boolean g_bUseRSquare;
	private float g_fLightHeight = 1.5f;
	private float g_fLightRadius = 1.0f;
	private float g_fLightAttenuation = 1.0f;
	
	
	// View/Object Setup
	
	private ViewData g_initialViewData = new ViewData(
			new Vec3(0.0f, 0.5f, 0.0f),
			new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
			5.0f,
			0.0f
	);

	private ViewScale g_viewScale = new ViewScale(	
			3.0f, 20.0f,
			1.5f, 0.5f,
			0.0f, 0.0f,																// No camera movement.
			90.0f / 250.0f
	);
	
	private ObjectData g_initialObjectData = new ObjectData(
			new Vec3(0.0f, 0.5f, 0.0f),
			new Quaternion(1.0f, 0.0f, 0.0f, 0.0f)
	);

	private ViewPole g_viewPole = new ViewPole(g_initialViewData, g_viewScale, MouseButtons.MB_LEFT_BTN);
	private ObjectPole g_objtPole = new ObjectPole(g_initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, g_viewPole);

	
	private Vec4 calcLightPosition() {
		float fCurrTimeThroughLoop = g_LightTimer.getAlpha();

		Vec4 ret = new Vec4(0.0f, g_fLightHeight, 0.0f, 1.0f);

		ret.x = (float) (Math.cos(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius);
		ret.z = (float) (Math.sin(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius);

		return ret;
	}
}