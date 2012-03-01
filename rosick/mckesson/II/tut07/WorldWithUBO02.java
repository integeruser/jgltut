package rosick.mckesson.II.tut07;

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

import rosick.LWJGLWindow;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 7. World in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2007.html
 * @author integeruser
 * 
 * Function										Increase/Left	Decrease/Right
 * Move camera target up/down					E				Q
 * Move camera target horizontally				A				D
 * Move camera target vertically				W				S
 * Rotate camera horizontally around target		L				J
 * Rotate camera vertically around target		I				K
 * Move camera towards/away from target			U				O
 * 
 * In addition, if you hold down the SHIFT key while pressing any of the last six keys, then 
 * 		the affected control will be much slower.
 */
public class WorldWithUBO02 extends LWJGLWindow {

	public static void main(String[] args) {		
		new WorldWithUBO02().start(800, 800);
	}

	
	private final int FLOAT_SIZE = Float.SIZE / 8;
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/II/tut07/data/";

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private class ProgramData {
		int theProgram;
		int globalUniformBlockIndex;
		int modelToWorldMatrixUnif;
		int baseColorUnif;
	}
	
	
	private final int MAT_SIZE = 16 * FLOAT_SIZE;
	private final int g_iGlobalMatricesBindingIndex = 0;
	
	private ProgramData uniformColor;
	private ProgramData objectColor;
	private ProgramData uniformColorTint;
	
	private int g_GlobalMatricesUBO;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack camMatrix = new MatrixStack(); 
	private MatrixStack	modelMatrix = new MatrixStack();
	
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private ProgramData loadProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToWorldMatrixUnif = glGetUniformLocation(data.theProgram, "modelToWorldMatrix");
		data.globalUniformBlockIndex = glGetUniformBlockIndex(data.theProgram, "GlobalMatrices");
		data.baseColorUnif = glGetUniformLocation(data.theProgram, "baseColor");

		glUniformBlockBinding(data.theProgram, data.globalUniformBlockIndex, g_iGlobalMatricesBindingIndex);

		return data;
	}

	private void initializeProgram() {
		uniformColor = 		loadProgram(TUTORIAL_DATAPATH + "PosOnlyWorldTransformUBO.vert",		TUTORIAL_DATAPATH + "ColorUniform.frag");
		objectColor = 		loadProgram(TUTORIAL_DATAPATH + "PosColorWorldTransformUBO.vert", 	TUTORIAL_DATAPATH + "ColorPassthrough.frag");
		uniformColorTint = 	loadProgram(TUTORIAL_DATAPATH + "PosColorWorldTransformUBO.vert", 	TUTORIAL_DATAPATH + "ColorMultUniform.frag");
		
		g_GlobalMatricesUBO = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_GlobalMatricesUBO);
		glBufferData(GL_UNIFORM_BUFFER, MAT_SIZE * 2, GL_STREAM_DRAW);	
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		glBindBufferRange(GL_UNIFORM_BUFFER, g_iGlobalMatricesBindingIndex, g_GlobalMatricesUBO, 0, MAT_SIZE * 2);
	}

	
	@Override
	protected void init() {
		initializeProgram();

		try {
			g_pConeMesh 		= new Mesh(TUTORIAL_DATAPATH + "UnitConeTint.xml");
			g_pCylinderMesh 	= new Mesh(TUTORIAL_DATAPATH + "UnitCylinderTint.xml");
			g_pCubeTintMesh 	= new Mesh(TUTORIAL_DATAPATH + "UnitCubeTint.xml");
			g_pCubeColorMesh 	= new Mesh(TUTORIAL_DATAPATH + "UnitCubeColor.xml");
			g_pPlaneMesh 		= new Mesh(TUTORIAL_DATAPATH + "UnitPlane.xml");
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
	}
	

	@Override
	protected void update() {
		float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			g_camTarget.z = (float) (g_camTarget.z - 4.0f * lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			g_camTarget.z = (float) (g_camTarget.z + 4.0f * lastFrameDuration);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			g_camTarget.x = (float) (g_camTarget.x + 4.0f * lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			g_camTarget.x = (float) (g_camTarget.x - 4.0f * lastFrameDuration);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			g_camTarget.y = (float) (g_camTarget.y - 4.0f * lastFrameDuration);
		} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			g_camTarget.y = (float) (g_camTarget.y + 4.0f * lastFrameDuration);
		}


		if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_sphereCamRelPos.y = (float) (g_sphereCamRelPos.y - 1.125f * lastFrameDuration);
			} else {
				g_sphereCamRelPos.y = (float) (g_sphereCamRelPos.y - 11.25f * lastFrameDuration);
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_sphereCamRelPos.y = (float) (g_sphereCamRelPos.y + 1.125f * lastFrameDuration);
			} else {
				g_sphereCamRelPos.y = (float) (g_sphereCamRelPos.y + 11.25f * lastFrameDuration);
			}
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_sphereCamRelPos.x = (float) (g_sphereCamRelPos.x - 1.125f * lastFrameDuration);
			} else {
				g_sphereCamRelPos.x = (float) (g_sphereCamRelPos.x - 11.25f * lastFrameDuration);
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_sphereCamRelPos.x = (float) (g_sphereCamRelPos.x + 1.125f * lastFrameDuration);
			} else {
				g_sphereCamRelPos.x = (float) (g_sphereCamRelPos.x + 11.25f * lastFrameDuration);
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_sphereCamRelPos.z = (float) (g_sphereCamRelPos.z - 0.5f * lastFrameDuration);
			} else {
				g_sphereCamRelPos.z = (float) (g_sphereCamRelPos.z - 5.0f * lastFrameDuration);
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				g_sphereCamRelPos.z = (float) (g_sphereCamRelPos.z + 0.5f * lastFrameDuration);
			} else {
				g_sphereCamRelPos.z = (float) (g_sphereCamRelPos.z + 5.0f * lastFrameDuration);
			}
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_SPACE:
					g_bDrawLookatPoint = !g_bDrawLookatPoint;
					break;
					
				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}
			}
		}
		
		
		g_sphereCamRelPos.y = Glm.clamp(g_sphereCamRelPos.y, -78.75f, -1.0f);
		g_camTarget.y = g_camTarget.y > 0.0f ? g_camTarget.y : 0.0f;
		g_sphereCamRelPos.z = g_sphereCamRelPos.z > 5.0f ? g_sphereCamRelPos.z : 5.0f;
	}


	@Override
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		{
			final Vec3 camPos = resolveCamPosition();
	
			camMatrix.clear();
			camMatrix.setMatrix(calcLookAtMatrix(camPos, g_camTarget, new Vec3(0.0f, 1.0f, 0.0f)));
			
			glBindBuffer(GL_UNIFORM_BUFFER, g_GlobalMatricesUBO);
			glBufferSubData(GL_UNIFORM_BUFFER, MAT_SIZE, camMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
	
			modelMatrix.clear();

			// Render the ground plane.
			{
				modelMatrix.push();

				modelMatrix.scale(100.0f, 1.0f, 100.0f);
			
				glUseProgram(uniformColor.theProgram);
				glUniformMatrix4(uniformColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
				glUniform4f(uniformColor.baseColorUnif, 0.302f, 0.416f, 0.0589f, 1.0f);
				g_pPlaneMesh.render();
				glUseProgram(0);
				
				modelMatrix.pop();
			}

			// Draw the trees
			drawForest(modelMatrix);

			// Draw the building.
			{
				modelMatrix.push();
				
				modelMatrix.translate(20.0f, 0.0f, -10.0f);

				drawParthenon(modelMatrix);
				
				modelMatrix.pop();
			}
					
			if (g_bDrawLookatPoint) {
				glDisable(GL_DEPTH_TEST);
				
				{
					modelMatrix.push();
	
					modelMatrix.translate(g_camTarget);
					modelMatrix.scale(1.0f, 1.0f, 1.0f);

					glUseProgram(objectColor.theProgram);
					glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
					g_pCubeColorMesh.render();
					glUseProgram(0);

					modelMatrix.pop();
				}
				
				glEnable(GL_DEPTH_TEST);
			}
		}
	}


	@Override
	protected void reshape(int width, int height) {
		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(45.0f, (width / (float) height), g_fzNear, g_fzFar);
		
		glBindBuffer(GL_UNIFORM_BUFFER, g_GlobalMatricesUBO);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, persMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		

	// Trees are 3x3 in X/Z, and fTrunkHeight + fConeHeight in the Y.
	private void drawTree(MatrixStack modelMatrix, float fTrunkHeight, float fConeHeight) {
		// Draw trunk.
		{
			modelMatrix.push();
			
			modelMatrix.scale(1.0f, fTrunkHeight, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(uniformColorTint.baseColorUnif, 0.694f, 0.4f, 0.106f, 1.0f);
			g_pCylinderMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw the treetop
		{
			modelMatrix.push();
			
			modelMatrix.translate(0.0f, fTrunkHeight, 0.0f);
			modelMatrix.scale(3.0f, fConeHeight, 3.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(uniformColorTint.baseColorUnif, 0.0f, 1.0f, 0.0f, 1.0f);
			g_pConeMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	private final float g_fColumnBaseHeight = 0.25f;


	// Columns are 1x1 in the X/Z, and fHieght units in the Y.
	private void drawColumn(MatrixStack modelMatrix, float fHeight) {
		// Draw the bottom of the column.
		{
			modelMatrix.push();
			
			modelMatrix.scale(1.0f, g_fColumnBaseHeight, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(uniformColorTint.baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw the top of the column.
		{
			modelMatrix.push();
			
			modelMatrix.translate(0.0f, fHeight - g_fColumnBaseHeight, 0.0f);
			modelMatrix.scale(1.0f, g_fColumnBaseHeight, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw the main column.
		{
			modelMatrix.push();
			
			modelMatrix.translate(0.0f, g_fColumnBaseHeight, 0.0f);
			modelMatrix.scale(0.8f, fHeight - (g_fColumnBaseHeight * 2.0f), 0.8f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCylinderMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final float g_fParthenonWidth = 14.0f;
	private final float g_fParthenonLength = 20.0f;
	private final float g_fParthenonColumnHeight = 5.0f;
	private final float g_fParthenonBaseHeight = 1.0f;
	private final float g_fParthenonTopHeight = 2.0f;

	private final float fFrontZVal = (g_fParthenonLength / 2.0f) - 1.0f;
	private final float fRightXVal = (g_fParthenonWidth / 2.0f) - 1.0f;
	private final int max1 = (int) (g_fParthenonWidth / 2.0f);
	private final int max2 = (int) ((g_fParthenonLength - 2.0f) / 2.0f);
	

	private void drawParthenon(MatrixStack modelMatrix) {
		// Draw base.
		{
			modelMatrix.push();

			modelMatrix.scale(g_fParthenonWidth, g_fParthenonBaseHeight, g_fParthenonLength);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw top.
		{
			modelMatrix.push();

			modelMatrix.translate(0.0f, g_fParthenonColumnHeight + g_fParthenonBaseHeight, 0.0f);
			modelMatrix.scale(g_fParthenonWidth, g_fParthenonTopHeight, g_fParthenonLength);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw columns.
		for (int iColumnNum = 0; iColumnNum < max1; iColumnNum++) {
			{
				modelMatrix.push();
				
				modelMatrix.translate((2.0f * iColumnNum) - (g_fParthenonWidth / 2.0f) + 1.0f, g_fParthenonBaseHeight, fFrontZVal);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
			{
				modelMatrix.push();
				
				modelMatrix.translate((2.0f * iColumnNum) - (g_fParthenonWidth / 2.0f) + 1.0f, g_fParthenonBaseHeight, -fFrontZVal);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
		}

		// Don't draw the first or last columns, since they've been drawn already.
		for (int iColumnNum = 1; iColumnNum < max2; iColumnNum++) {
			{
				modelMatrix.push();
				
				modelMatrix.translate(fRightXVal, g_fParthenonBaseHeight, (2.0f * iColumnNum) - (g_fParthenonLength / 2.0f) + 1.0f);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
			{
				modelMatrix.push();
				
				modelMatrix.translate(-fRightXVal, g_fParthenonBaseHeight, (2.0f * iColumnNum) - (g_fParthenonLength / 2.0f) + 1.0f);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
		}

		// Draw interior.
		{
			modelMatrix.push();

			modelMatrix.translate(0.0f, 1.0f, 0.0f);
			modelMatrix.scale(g_fParthenonWidth - 6.0f, g_fParthenonColumnHeight, g_fParthenonLength - 6.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(objectColor.theProgram);
			glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			g_pCubeColorMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw headpiece.
		{
			modelMatrix.push();

			modelMatrix.translate(0.0f, g_fParthenonColumnHeight + g_fParthenonBaseHeight + (g_fParthenonTopHeight / 2.0f),	g_fParthenonLength / 2.0f);
			modelMatrix.rotateX(-135.0f);
			modelMatrix.rotateY(45.0f);

			glUseProgram(objectColor.theProgram);
			glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			g_pCubeColorMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	private class TreeData {
		float fXPos;
		float fZPos;
		float fTrunkHeight;
		float fConeHeight;

		TreeData(float fXPos, float fZPos, float fTrunkHeight, float fConeHeight) {
			this.fXPos = fXPos;
			this.fZPos = fZPos;
			this.fTrunkHeight = fTrunkHeight;
			this.fConeHeight = fConeHeight;
		}
	}


	private final TreeData g_forest[] = {
			new TreeData(-45.0f, -40.0f, 2.0f, 3.0f),
			new TreeData(-42.0f, -35.0f, 2.0f, 3.0f),
			new TreeData(-39.0f, -29.0f, 2.0f, 4.0f),
			new TreeData(-44.0f, -26.0f, 3.0f, 3.0f),
			new TreeData(-40.0f, -22.0f, 2.0f, 4.0f),
			new TreeData(-36.0f, -15.0f, 3.0f, 3.0f),
			new TreeData(-41.0f, -11.0f, 2.0f, 3.0f),
			new TreeData(-37.0f, -6.0f, 3.0f, 3.0f),
			new TreeData(-45.0f, 0.0f, 2.0f, 3.0f),
			new TreeData(-39.0f, 4.0f, 3.0f, 4.0f),
			new TreeData(-36.0f, 8.0f, 2.0f, 3.0f),
			new TreeData(-44.0f, 13.0f, 3.0f, 3.0f),
			new TreeData(-42.0f, 17.0f, 2.0f, 3.0f),
			new TreeData(-38.0f, 23.0f, 3.0f, 4.0f),
			new TreeData(-41.0f, 27.0f, 2.0f, 3.0f),
			new TreeData(-39.0f, 32.0f, 3.0f, 3.0f),
			new TreeData(-44.0f, 37.0f, 3.0f, 4.0f),
			new TreeData(-36.0f, 42.0f, 2.0f, 3.0f),

			new TreeData(-32.0f, -45.0f, 2.0f, 3.0f),
			new TreeData(-30.0f, -42.0f, 2.0f, 4.0f),
			new TreeData(-34.0f, -38.0f, 3.0f, 5.0f),
			new TreeData(-33.0f, -35.0f, 3.0f, 4.0f),
			new TreeData(-29.0f, -28.0f, 2.0f, 3.0f),
			new TreeData(-26.0f, -25.0f, 3.0f, 5.0f),
			new TreeData(-35.0f, -21.0f, 3.0f, 4.0f),
			new TreeData(-31.0f, -17.0f, 3.0f, 3.0f),
			new TreeData(-28.0f, -12.0f, 2.0f, 4.0f),
			new TreeData(-29.0f, -7.0f, 3.0f, 3.0f),
			new TreeData(-26.0f, -1.0f, 2.0f, 4.0f),
			new TreeData(-32.0f, 6.0f, 2.0f, 3.0f),
			new TreeData(-30.0f, 10.0f, 3.0f, 5.0f),
			new TreeData(-33.0f, 14.0f, 2.0f, 4.0f),
			new TreeData(-35.0f, 19.0f, 3.0f, 4.0f),
			new TreeData(-28.0f, 22.0f, 2.0f, 3.0f),
			new TreeData(-33.0f, 26.0f, 3.0f, 3.0f),
			new TreeData(-29.0f, 31.0f, 3.0f, 4.0f),
			new TreeData(-32.0f, 38.0f, 2.0f, 3.0f),
			new TreeData(-27.0f, 41.0f, 3.0f, 4.0f),
			new TreeData(-31.0f, 45.0f, 2.0f, 4.0f),
			new TreeData(-28.0f, 48.0f, 3.0f, 5.0f),

			new TreeData(-25.0f, -48.0f, 2.0f, 3.0f),
			new TreeData(-20.0f, -42.0f, 3.0f, 4.0f),
			new TreeData(-22.0f, -39.0f, 2.0f, 3.0f),
			new TreeData(-19.0f, -34.0f, 2.0f, 3.0f),
			new TreeData(-23.0f, -30.0f, 3.0f, 4.0f),
			new TreeData(-24.0f, -24.0f, 2.0f, 3.0f),
			new TreeData(-16.0f, -21.0f, 2.0f, 3.0f),
			new TreeData(-17.0f, -17.0f, 3.0f, 3.0f),
			new TreeData(-25.0f, -13.0f, 2.0f, 4.0f),
			new TreeData(-23.0f, -8.0f, 2.0f, 3.0f),
			new TreeData(-17.0f, -2.0f, 3.0f, 3.0f),
			new TreeData(-16.0f, 1.0f, 2.0f, 3.0f),
			new TreeData(-19.0f, 4.0f, 3.0f, 3.0f),
			new TreeData(-22.0f, 8.0f, 2.0f, 4.0f),
			new TreeData(-21.0f, 14.0f, 2.0f, 3.0f),
			new TreeData(-16.0f, 19.0f, 2.0f, 3.0f),
			new TreeData(-23.0f, 24.0f, 3.0f, 3.0f),
			new TreeData(-18.0f, 28.0f, 2.0f, 4.0f),
			new TreeData(-24.0f, 31.0f, 2.0f, 3.0f),
			new TreeData(-20.0f, 36.0f, 2.0f, 3.0f),
			new TreeData(-22.0f, 41.0f, 3.0f, 3.0f),
			new TreeData(-21.0f, 45.0f, 2.0f, 3.0f),

			new TreeData(-12.0f, -40.0f, 2.0f, 4.0f),
			new TreeData(-11.0f, -35.0f, 3.0f, 3.0f),
			new TreeData(-10.0f, -29.0f, 1.0f, 3.0f),
			new TreeData(-9.0f, -26.0f, 2.0f, 2.0f),
			new TreeData(-6.0f, -22.0f, 2.0f, 3.0f),
			new TreeData(-15.0f, -15.0f, 1.0f, 3.0f),
			new TreeData(-8.0f, -11.0f, 2.0f, 3.0f),
			new TreeData(-14.0f, -6.0f, 2.0f, 4.0f),
			new TreeData(-12.0f, 0.0f, 2.0f, 3.0f),
			new TreeData(-7.0f, 4.0f, 2.0f, 2.0f),
			new TreeData(-13.0f, 8.0f, 2.0f, 2.0f),
			new TreeData(-9.0f, 13.0f, 1.0f, 3.0f),
			new TreeData(-13.0f, 17.0f, 3.0f, 4.0f),
			new TreeData(-6.0f, 23.0f, 2.0f, 3.0f),
			new TreeData(-12.0f, 27.0f, 1.0f, 2.0f),
			new TreeData(-8.0f, 32.0f, 2.0f, 3.0f),
			new TreeData(-10.0f, 37.0f, 3.0f, 3.0f),
			new TreeData(-11.0f, 42.0f, 2.0f, 2.0f),

			new TreeData(15.0f, 5.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 10.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 15.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 20.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 25.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 30.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 35.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 40.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 45.0f, 2.0f, 3.0f),

			new TreeData(25.0f, 5.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 10.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 15.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 20.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 25.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 30.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 35.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 40.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 45.0f, 2.0f, 3.0f), 
	};


	private void drawForest(MatrixStack modelMatrix) {
		for (TreeData currTree : g_forest) {
			modelMatrix.push();

			modelMatrix.translate(currTree.fXPos, 0.0f, currTree.fZPos);
			drawTree(modelMatrix, currTree.fTrunkHeight, currTree.fConeHeight);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	private Mesh g_pConeMesh;
	private Mesh g_pCylinderMesh;
	private Mesh g_pCubeTintMesh;
	private Mesh g_pCubeColorMesh;
	private Mesh g_pPlaneMesh;
	
	private boolean g_bDrawLookatPoint = false;
	private Vec3 g_camTarget = new Vec3(0.0f, 0.4f, 0.0f);
	
	// In spherical coordinates.
	private Vec3 g_sphereCamRelPos = new Vec3(67.5f, -46.0f, 150.0f);
	
	
	private Vec3 resolveCamPosition() {
		float phi = Framework.degToRad(g_sphereCamRelPos.x);
		float theta = Framework.degToRad(g_sphereCamRelPos.y + 90.0f);

		float fSinTheta = (float) Math.sin(theta);
		float fCosTheta = (float) Math.cos(theta);
		float fCosPhi = (float) Math.cos(phi);
		float fSinPhi = (float) Math.sin(phi);

		Vec3 dirToCamera = new Vec3(fSinTheta * fCosPhi, fCosTheta, fSinTheta * fSinPhi);
		
		return (dirToCamera.scale(g_sphereCamRelPos.z)).add(g_camTarget);
	}
	
	
	private Mat4 calcLookAtMatrix(Vec3 cameraPt, Vec3 lookPt, Vec3 upPt) {
		Vec3 lookDir = Glm.normalize(Vec3.sub(lookPt, cameraPt));
		Vec3 upDir = Glm.normalize(upPt);

		Vec3 rightDir = Glm.normalize(Glm.cross(lookDir, upDir));
		Vec3 perpUpDir = Glm.cross(rightDir, lookDir);

		Mat4 rotMat = new Mat4(1.0f);
		rotMat.setColumn(0, new Vec4(rightDir, 0.0f));
		rotMat.setColumn(1, new Vec4(perpUpDir, 0.0f));
		rotMat.setColumn(2, new Vec4(Vec3.negate(lookDir), 0.0f));

		rotMat = Glm.transpose(rotMat);

		Mat4 transMat = new Mat4(1.0f);
		transMat.setColumn(3, new Vec4(Vec3.negate(cameraPt), 1.0f));

		return rotMat.mul(transMat);
	}
}