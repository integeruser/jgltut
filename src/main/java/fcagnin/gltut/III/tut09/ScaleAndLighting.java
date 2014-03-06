package fcagnin.gltut.III.tut09;

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

import fcagnin.LWJGLWindow;
import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat3;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Quaternion;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.*;
import fcagnin.gltut.framework.Framework;
import fcagnin.gltut.framework.Mesh;
import fcagnin.gltut.framework.MousePole;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 9. Lights On 
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2009.html
 * @author integeruser
 * 
 * SPACE	- toggle between drawing the uncolored cylinder and the colored one.
 * T		- toggle between properly using the inverse-transpose and not using the inverse transpose.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING			- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT	- spin the object.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 */
public class ScaleAndLighting extends LWJGLWindow {
	
	public static void main(String[] args) {		
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/III/tut09/data/";

		new ScaleAndLighting().start();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	protected void init() {
		initializeProgram();
		
		try {
			cylinderMesh = new Mesh("UnitCylinder.xml");
			planeMesh 	= new Mesh("LargePlane.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
		}		
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
		glEnable(GL_DEPTH_CLAMP);
		
		projectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 
				0, ProjectionBlock.SIZE);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	

	@Override
	protected void update() {
		while (Mouse.next()) {
			int eventButton = Mouse.getEventButton();
									
			if (eventButton != -1) {
				boolean pressed = Mouse.getEventButtonState();
				MousePole.forwardMouseButton(viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY());			
				MousePole.forwardMouseButton(objtPole, eventButton, pressed, Mouse.getX(), Mouse.getY());
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					MousePole.forwardMouseWheel(viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
					MousePole.forwardMouseWheel(objtPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					MousePole.forwardMouseMotion(viewPole, Mouse.getX(), Mouse.getY());			
					MousePole.forwardMouseMotion(objtPole, Mouse.getX(), Mouse.getY());
				}
			}
		}


		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					scaleCyl = !scaleCyl;
					break;
					
				case Keyboard.KEY_T:
					doInvTranspose = !doInvTranspose;
					
					if (doInvTranspose) {
						System.out.printf("Doing Inverse Transpose.\n");
					} else {
						System.out.printf("Bad lighting.\n");
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
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setMatrix(viewPole.calcMatrix());

		Vec4 lightDirCameraSpace = Mat4.mul(modelMatrix.top(), lightDirection);	
		
		glUseProgram(whiteDiffuseColor.theProgram);
		glUniform3(whiteDiffuseColor.dirToLightUnif, lightDirCameraSpace.fillAndFlipBuffer(vec4Buffer));
		glUseProgram(vertexDiffuseColor.theProgram);
		glUniform3(vertexDiffuseColor.dirToLightUnif, lightDirCameraSpace.fillAndFlipBuffer(vec4Buffer));
		glUseProgram(0);
		
		{
			modelMatrix.push();

			// Render the ground plane.
			{
				modelMatrix.push();

				glUseProgram(whiteDiffuseColor.theProgram);
				glUniformMatrix4(whiteDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));				
				Mat3 normMatrix = new Mat3(modelMatrix.top());
				glUniformMatrix3(whiteDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
				glUniform4f(whiteDiffuseColor.lightIntensityUnif, 1.0f, 1.0f, 1.0f, 1.0f);
				planeMesh.render();
				glUseProgram(0);
				
				modelMatrix.pop();
			}

			// Render the Cylinder
			{
				modelMatrix.push();
	
				modelMatrix.applyMatrix(objtPole.calcMatrix());

				if (scaleCyl) {
					modelMatrix.scale(1.0f, 1.0f, 0.2f);
				}
				
				glUseProgram(vertexDiffuseColor.theProgram);
				glUniformMatrix4(vertexDiffuseColor.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(mat4Buffer));
				Mat3 normMatrix = new Mat3(modelMatrix.top());
				if (doInvTranspose) {
					normMatrix = Glm.transpose(Glm.inverse(normMatrix));
				}
				glUniformMatrix3(vertexDiffuseColor.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(mat3Buffer));
				glUniform4f(vertexDiffuseColor.lightIntensityUnif, 1.0f, 1.0f, 1.0f, 1.0f);
				cylinderMesh.render("lit-color");				
				glUseProgram(0);
				
				modelMatrix.pop();
			}
			
			modelMatrix.pop();
		}
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(45.0f, (width / (float) height), zNear, zFar);
		
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
	
	private float zNear = 1.0f;
	private float zFar = 1000.0f;

	private FloatBuffer vec4Buffer 	= BufferUtils.createFloatBuffer(Vec4.SIZE);
	private FloatBuffer mat3Buffer 	= BufferUtils.createFloatBuffer(Mat3.SIZE);
	private FloatBuffer mat4Buffer 	= BufferUtils.createFloatBuffer(Mat4.SIZE);
	
	
	private void initializeProgram() {	
		whiteDiffuseColor =		loadProgram("DirVertexLighting_PN.vert",	"ColorPassthrough.frag");
		vertexDiffuseColor = 	loadProgram("DirVertexLighting_PCN.vert", 	"ColorPassthrough.frag");
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ProgramData whiteDiffuseColor;
	private ProgramData vertexDiffuseColor;
	
	
	private class ProgramData {
		int theProgram;
		
		int dirToLightUnif;
		int lightIntensityUnif;
		
		int modelToCameraMatrixUnif;
		int normalModelToCameraMatrixUnif;
	}
	
	
	private ProgramData loadProgram(String vertexShaderFilename, String fragmentShaderFilename) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
		data.dirToLightUnif = glGetUniformLocation(data.theProgram, "dirToLight");
		data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

		return data;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Mesh cylinderMesh;
	private Mesh planeMesh;
	
	private Vec4 lightDirection = new Vec4(0.866f, 0.5f, 0.0f, 0.0f);
	
	private boolean doInvTranspose = true;
	private boolean scaleCyl;
	
	
	////////////////////////////////
	// View / Object setup.
	private ViewData initialViewData = new ViewData(
			new Vec3(0.0f, 0.5f, 0.0f),
			new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
			5.0f,
			0.0f);

	private ViewScale viewScale = new ViewScale(	
			3.0f, 20.0f,
			1.5f, 0.5f,
			0.0f, 0.0f,						// No camera movement.
			90.0f / 250.0f);
	
	
	private ObjectData initialObjectData = new ObjectData(
			new Vec3(0.0f, 0.5f, 0.0f),
			new Quaternion(1.0f, 0.0f, 0.0f, 0.0f));

	
	private ViewPole viewPole 	= new ViewPole(initialViewData, viewScale, MouseButtons.MB_LEFT_BTN);
	private ObjectPole objtPole = new ObjectPole(initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final int projectionBlockIndex = 2;

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