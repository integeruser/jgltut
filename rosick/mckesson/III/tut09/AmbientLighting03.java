package rosick.mckesson.III.tut09;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

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
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 9. Lights On 
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2009.html
 * @author integeruser
 * 
 * SPACE	- toggles between drawing the uncolored cylinder and the colored one.
 * T		- toggles ambient lighting on and off.
 * 
 * LEFT	  CLICKING and DRAGGING				- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING				- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_ALT	- spin the object.
 * WHEEL  SCROLLING							- move the camera closer to it's target point or farther away. 
 */
public class AmbientLighting03 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		new AmbientLighting03().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/III/tut09/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class ProgramData {
		int theProgram;
		
		int dirToLightUnif;
		int lightIntensityUnif;
		int ambientIntensityUnif;

		int modelToCameraMatrixUnif;
		int normalModelToCameraMatrixUnif;
	}
	
		
	private final int g_projectionBlockIndex = 2;
	
	private ProgramData g_WhiteDiffuseColor;
	private ProgramData g_VertexDiffuseColor;
	private ProgramData g_WhiteAmbDiffuseColor;
	private ProgramData g_VertexAmbDiffuseColor;
	
	private int g_projectionUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;

	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer9 	= BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempFloatBuffer16 	= BufferUtils.createFloatBuffer(16);
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private ProgramData loadProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");
		data.dirToLightUnif = glGetUniformLocation(data.theProgram, "dirToLight");
		data.lightIntensityUnif = glGetUniformLocation(data.theProgram, "lightIntensity");
		data.ambientIntensityUnif = glGetUniformLocation(data.theProgram, "ambientIntensity");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		return data;
	}
	
	private void initializeProgram() {	
		g_WhiteDiffuseColor =		loadProgram(TUTORIAL_DATAPATH + "DirVertexLighting_PN.vert",		TUTORIAL_DATAPATH + "ColorPassthrough.frag");		
		g_VertexDiffuseColor = 		loadProgram(TUTORIAL_DATAPATH + "DirVertexLighting_PCN.vert", 		TUTORIAL_DATAPATH + "ColorPassthrough.frag");
		g_WhiteAmbDiffuseColor = 	loadProgram(TUTORIAL_DATAPATH + "DirAmbVertexLighting_PN.vert", 	TUTORIAL_DATAPATH + "ColorPassthrough.frag");
		g_VertexAmbDiffuseColor = 	loadProgram(TUTORIAL_DATAPATH + "DirAmbVertexLighting_PCN.vert",	TUTORIAL_DATAPATH + "ColorPassthrough.frag");
	}
	

	@Override
	protected void init() {
		initializeProgram();
		
		try {
			g_pCylinderMesh = new Mesh(TUTORIAL_DATAPATH + "UnitCylinder.xml");
			g_pPlaneMesh 	= new Mesh(TUTORIAL_DATAPATH + "LargePlane.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}		
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
		glEnable(GL_DEPTH_CLAMP);
		
		g_projectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer, 0, ProjectionBlock.SIZE);
		
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
		

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					g_bDrawColoredCyl = !g_bDrawColoredCyl;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
					g_bShowAmbient = !g_bShowAmbient;
					
					if (g_bShowAmbient) {
						System.out.printf("Ambient Lighting On.\n");
					} else {
						System.out.printf("Ambient Lighting Off.\n");
					}
				}
				
				
				else if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
					leaveMainLoop();
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		modelMatrix.clear();
		modelMatrix.setMatrix(g_viewPole.calcMatrix());

		Vec4 lightDirCameraSpace = Mat4.mul(modelMatrix.top(), g_lightDirection);
		
		ProgramData whiteDiffuse = g_bShowAmbient ? g_WhiteAmbDiffuseColor : g_WhiteDiffuseColor;
		ProgramData vertexDiffuse = g_bShowAmbient ? g_VertexAmbDiffuseColor : g_VertexDiffuseColor;

		if (g_bShowAmbient) {
			glUseProgram(whiteDiffuse.theProgram);
			glUniform4f(whiteDiffuse.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
			glUniform4f(whiteDiffuse.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
			glUseProgram(vertexDiffuse.theProgram);
			glUniform4f(vertexDiffuse.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
			glUniform4f(vertexDiffuse.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		} else {
			glUseProgram(whiteDiffuse.theProgram);
			glUniform4f(whiteDiffuse.lightIntensityUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			glUseProgram(vertexDiffuse.theProgram);
			glUniform4f(vertexDiffuse.lightIntensityUnif, 1.0f, 1.0f, 1.0f, 1.0f);
		}
		
		glUseProgram(whiteDiffuse.theProgram);
		glUniform3(whiteDiffuse.dirToLightUnif, lightDirCameraSpace.fillAndFlipBuffer(tempFloatBuffer4));
		glUseProgram(vertexDiffuse.theProgram);
		glUniform3(vertexDiffuse.dirToLightUnif, lightDirCameraSpace.fillAndFlipBuffer(tempFloatBuffer4));
		glUseProgram(0);

		{
			modelMatrix.push();

			// Render the ground plane.
			{
				modelMatrix.push();

				glUseProgram(whiteDiffuse.theProgram);
				glUniformMatrix4(whiteDiffuse.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
				Mat3 normMatrix = new Mat3(modelMatrix.top());
				glUniformMatrix3(whiteDiffuse.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));
				g_pPlaneMesh.render();
				glUseProgram(0);
				
				modelMatrix.pop();
			}

			// Render the Cylinder
			{
				modelMatrix.push();
	
				modelMatrix.applyMatrix(g_objtPole.calcMatrix());

				if (g_bDrawColoredCyl) {
					glUseProgram(vertexDiffuse.theProgram);
					glUniformMatrix4(vertexDiffuse.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
					Mat3 normMatrix = new Mat3(modelMatrix.top());
					glUniformMatrix3(vertexDiffuse.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));
					g_pCylinderMesh.render("lit-color");
				} else {
					glUseProgram(whiteDiffuse.theProgram);
					glUniformMatrix4(whiteDiffuse.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
					Mat3 normMatrix = new Mat3(modelMatrix.top());
					glUniformMatrix3(whiteDiffuse.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));
					g_pCylinderMesh.render("lit");
				}
				glUseProgram(0);
				
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
	
	
	private Mesh g_pCylinderMesh;
	private Mesh g_pPlaneMesh;
	
	private Vec4 g_lightDirection = new Vec4(0.866f, 0.5f, 0.0f, 0.0f);

	private boolean g_bDrawColoredCyl = true;
	private boolean g_bShowAmbient;
	
	
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
}