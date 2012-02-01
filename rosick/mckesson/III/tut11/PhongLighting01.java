package rosick.mckesson.III.tut11;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import static rosick.glm.Vec.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.framework.Mesh;
import rosick.framework.Timer;
import rosick.glm.Glm;
import rosick.glm.Mat3;
import rosick.glm.Mat4;
import rosick.glm.Quaternion;
import rosick.glm.Vec3;
import rosick.glm.Vec4;
import rosick.glutil.MatrixStack;
import rosick.glutil.pole.MousePole.*;
import rosick.glutil.pole.ObjectPole;
import rosick.glutil.pole.ViewPole;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 11. Shinies
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2011.html
 * @author integeruser
 * 
 * SPACEBAR - toggles between drawing the uncolored cylinder and the colored one.
 * I,J,K,L  - control the light's position. Holding LEFT_SHIFT with these keys will move in smaller increments.
 * U,O      - control the specular value. They raise and low the specular exponent. Using LEFT_SHIFT in combination 
 * 				with them will raise/lower the exponent by smaller amounts.
 * Y 		- toggles the drawing of the light source.
 * T 		- toggles between the scaled and unscaled cylinder.
 * B 		- toggles the light's rotation on/off.
 * G 		- toggles between a diffuse color of (1, 1, 1) and a darker diffuse color of (0.2, 0.2, 0.2).
 * H 		- selects between specular and diffuse, just specular and just diffuse. Pressing LEFT_SHIFT+H will toggle 
 * 				between diffuse only and diffuse+specular.
 * 
 * LEFT	  CLICKING and DRAGGING				- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING				- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_ALT	- spin the object.
 * WHEEL  SCROLLING							- move the camera closer to it's target point or farther away. 
 */
public class PhongLighting01 extends GLWindow {
	
	public static void main(String[] args) {		
		new PhongLighting01().start();
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/III/tut11/data/";
	
	
	
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
		int shininessFactorUnif;
		int baseDiffuseColorUnif;
	}
	
	private class UnlitProgData {
		int theProgram;

		int objectColorUnif;
		int modelToCameraMatrixUnif;
	}
	
	
	private class ProjectionBlock {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = 16 * (Float.SIZE / 8);
	}
	
		
	private final int g_projectionBlockIndex = 2;
	
	private ProgramData g_WhiteNoPhong;
	private ProgramData g_ColorNoPhong;
	
	private ProgramData g_WhitePhong;
	private ProgramData g_ColorPhong;
	
	private ProgramData g_WhitePhongOnly;
	private ProgramData g_ColorPhongOnly;
	
	private UnlitProgData g_Unlit;
	
	private int g_projectionUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempSharedFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempSharedFloatBuffer9 	= BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempSharedFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
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
		data.shininessFactorUnif = glGetUniformLocation(data.theProgram, "shininessFactor");
		data.baseDiffuseColorUnif = glGetUniformLocation(data.theProgram, "baseDiffuseColor");

		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		return data;
	}
	
	private void initializePrograms() {	
		g_WhiteNoPhong = 	loadLitProgram(BASEPATH + "PN.vert", 	BASEPATH + "NoPhong.frag");
		g_ColorNoPhong = 	loadLitProgram(BASEPATH + "PCN.vert", 	BASEPATH + "NoPhong.frag");

		g_WhitePhong = 		loadLitProgram(BASEPATH + "PN.vert", 	BASEPATH + "PhongLighting.frag");
		g_ColorPhong = 		loadLitProgram(BASEPATH + "PCN.vert", 	BASEPATH + "PhongLighting.frag");

		g_WhitePhongOnly = 	loadLitProgram(BASEPATH + "PN.vert", 	BASEPATH + "PhongOnly.frag");
		g_ColorPhongOnly = 	loadLitProgram(BASEPATH + "PCN.vert", 	BASEPATH + "PhongOnly.frag");

		g_Unlit = loadUnlitProgram(BASEPATH + "PosTransform.vert", BASEPATH + "UniformColor.frag");
	}
	
	
	@Override
	protected void init() {
		initializePrograms();
		
		try {
			g_pCylinderMesh = new Mesh(BASEPATH + "UnitCylinder.xml");
			g_pPlaneMesh 	= new Mesh(BASEPATH + "LargePlane.xml");
			g_pCubeMesh 	= new Mesh(BASEPATH + "UnitCube.xml");
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
					Framework.forwardMouseButton(g_viewPole, eventButton, true, Mouse.getX(), Mouse.getY());			
					Framework.forwardMouseButton(g_objtPole, eventButton, true, Mouse.getX(), Mouse.getY());	
				} else {
					// Mouse up
					Framework.forwardMouseButton(g_viewPole, eventButton, false, Mouse.getX(), Mouse.getY());			
					Framework.forwardMouseButton(g_objtPole, eventButton, false, Mouse.getX(), Mouse.getY());
				}
			} else {
				// Mouse moving or mouse scrolling
				int dWheel = Mouse.getDWheel();
				
				if (dWheel != 0) {
					Framework.forwardMouseWheel(g_viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
					Framework.forwardMouseWheel(g_objtPole, dWheel, dWheel, Mouse.getX(), Mouse.getY());
				}
				
				if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) {
					Framework.forwardMouseMotion(g_viewPole, Mouse.getX(), Mouse.getY());			
					Framework.forwardMouseMotion(g_objtPole, Mouse.getX(), Mouse.getY());
				}
			}
		}
		
		
		boolean bChangedShininess = false;
		boolean bChangedLightModel = false;
		
		float lastFrameDuration = (float) (getLastFrameDuration() * 5 / 1000.0);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				g_fLightRadius -= 0.05f * lastFrameDuration;
			} else {
				g_fLightRadius -= 0.2f * lastFrameDuration;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				g_fLightRadius += 0.05f * lastFrameDuration;
			} else {
				g_fLightRadius += 0.2f * lastFrameDuration;
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				g_fLightHeight += 0.05f * lastFrameDuration;
			} else {
				g_fLightHeight += 0.2f * lastFrameDuration;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				g_fLightHeight -= 0.05f * lastFrameDuration;
			} else {
				g_fLightHeight -= 0.2f * lastFrameDuration;
			}
		}

		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					g_bDrawColoredCyl = !g_bDrawColoredCyl;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_O) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						g_fShininessFactor += 0.1f;
					} else {
						g_fShininessFactor += 0.5f;
					}
					
					bChangedShininess = true;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_U) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						g_fShininessFactor -= 0.1f;
					} else {
						g_fShininessFactor -= 0.5f;
					}
					
					bChangedShininess = true;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_Y) {
					g_bDrawLightSource = !g_bDrawLightSource;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
					g_bScaleCyl = !g_bScaleCyl;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_B) {
					g_LightTimer.togglePause();
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_G) {
					g_bDrawDark = !g_bDrawDark;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_H) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						switch(g_eLightModel) {
							case LM_DIFFUSE_AND_SPECULAR:
								g_eLightModel = LightingModel.LM_PURE_DIFFUSE;
								break;
							case LM_PURE_DIFFUSE:
								g_eLightModel = LightingModel.LM_DIFFUSE_AND_SPECULAR;
								break;
							case LM_SPECULAR_ONLY:
								g_eLightModel = LightingModel.LM_PURE_DIFFUSE;
								break;
						}
					} else {
						int index = g_eLightModel.ordinal() + 1;
						index %= LightingModel.LM_MAX_LIGHTING_MODEL.ordinal();
						g_eLightModel = LightingModel.values()[index];
					}
				
					bChangedLightModel = true;
				
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					leaveMainLoop();
				}
			}
		}
		
		
		if (g_fLightRadius < 0.2f) {
			g_fLightRadius = 0.2f;
		}
		
		if (g_fShininessFactor <= 0.0f) {
			g_fShininessFactor = 0.0001f;
		}
		
		if (bChangedShininess) {
			System.out.println("Shiny: " + g_fShininessFactor);
		}
		
		if (bChangedLightModel) {
			System.out.println(strLightModelNames[g_eLightModel.ordinal()]);
		}
	}
	

	@Override
	protected void display() {			
		g_LightTimer.update(getElapsedTime());
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		modelMatrix.clear();
		modelMatrix.setMatrix(g_viewPole.calcMatrix());

		final Vec4 worldLightPos = calcLightPosition();
		final Vec4 lightPosCameraSpace = Mat4.mul(modelMatrix.top(), worldLightPos);
		
		ProgramData pWhiteProg = null;
		ProgramData pColorProg = null;		
		
		switch (g_eLightModel) {
			case LM_PURE_DIFFUSE:
				pWhiteProg = g_WhiteNoPhong;
				pColorProg = g_ColorNoPhong;
				break;
			case LM_DIFFUSE_AND_SPECULAR:
				pWhiteProg = g_WhitePhong;
				pColorProg = g_ColorPhong;
				break;
			case LM_SPECULAR_ONLY:
				pWhiteProg = g_WhitePhongOnly;
				pColorProg = g_ColorPhongOnly;
				break;
		}
			
		glUseProgram(pWhiteProg.theProgram);
		glUniform4f(pWhiteProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(pWhiteProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUniform3(pWhiteProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillBuffer(tempSharedFloatBuffer4));
		glUniform1f(pWhiteProg.lightAttenuationUnif, g_fLightAttenuation);
		glUniform1f(pWhiteProg.shininessFactorUnif, g_fShininessFactor);
		glUniform4(pWhiteProg.baseDiffuseColorUnif, g_bDrawDark ? g_darkColor.fillBuffer(tempSharedFloatBuffer4) : g_lightColor.fillBuffer(tempSharedFloatBuffer4));
		
		glUseProgram(pColorProg.theProgram);
		glUniform4f(pColorProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(pColorProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUniform3(pColorProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillBuffer(tempSharedFloatBuffer4));
		glUniform1f(pColorProg.lightAttenuationUnif, g_fLightAttenuation);
		glUniform1f(pColorProg.shininessFactorUnif, g_fShininessFactor);
		glUseProgram(0);
		
		{
			modelMatrix.push();

			// Render the ground plane.
			{
				modelMatrix.push();

				Mat3 normMatrix = new Mat3(modelMatrix.top());
				normMatrix = Glm.transpose(Glm.inverse(normMatrix));
				
				glUseProgram(pWhiteProg.theProgram);
				glUniformMatrix4(pWhiteProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));

				glUniformMatrix3(pWhiteProg.normalModelToCameraMatrixUnif, false, normMatrix.fillBuffer(tempSharedFloatBuffer9));
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
				
				ProgramData pProg = g_bDrawColoredCyl ? pColorProg : pWhiteProg;
				glUseProgram(pProg.theProgram);
				glUniformMatrix4(pProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));

				glUniformMatrix3(pProg.normalModelToCameraMatrixUnif, false, normMatrix.fillBuffer(tempSharedFloatBuffer9));

				if (g_bDrawColoredCyl) {
					g_pCylinderMesh.render("lit-color");
				} else {
					g_pCylinderMesh.render("lit");
				}
				
				glUseProgram(0);
				
				modelMatrix.pop();
			}
			
			// Render the light
			if (g_bDrawLightSource) {
				modelMatrix.push();

				modelMatrix.translate(worldLightPos.get(X), worldLightPos.get(Y), worldLightPos.get(Z));
				modelMatrix.scale(0.1f, 0.1f, 0.1f);

				glUseProgram(g_Unlit.theProgram);
				glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));
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

		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.cameraToClipMatrix.fillBuffer(tempSharedFloatBuffer16));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static LightingModel g_eLightModel = LightingModel.LM_DIFFUSE_AND_SPECULAR;
	
	private enum LightingModel {
		LM_PURE_DIFFUSE,
		LM_DIFFUSE_AND_SPECULAR,
		LM_SPECULAR_ONLY,

		LM_MAX_LIGHTING_MODEL;
	};
	
	
	private static final String strLightModelNames[] = {
		"Diffuse only.",
		"Specular + diffuse.",
		"Specular only.",
	};
	
	private static boolean g_bDrawColoredCyl;
	private static boolean g_bDrawLightSource;
	private static boolean g_bScaleCyl;
	private static boolean g_bDrawDark;

	private static float g_fLightHeight = 1.5f;
	private static float g_fLightRadius = 1.0f;
	
	private static float g_fShininessFactor = 4.0f;
	private final float g_fLightAttenuation = 1.2f;
	
	private final Vec4 g_darkColor = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
	private final Vec4 g_lightColor = new Vec4(1.0f);

	private Timer g_LightTimer = new Timer(Timer.Type.TT_LOOP, 5.0f);
	
	private Mesh g_pCylinderMesh;
	private Mesh g_pPlaneMesh;
	private Mesh g_pCubeMesh;
	
	
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
		0.0f, 0.0f,																	// No camera movement.
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

		ret.set(X, (float) (Math.cos(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius));
		ret.set(Z, (float) (Math.sin(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius));

		return ret;
	}
}