package rosick.mckesson.III.tut11;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import static rosick.jglsdk.glm.Vec.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import rosick.GLWindow;
import rosick.PortingUtils.BufferableData;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.framework.Timer;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.pole.MousePole.*;
import rosick.jglsdk.glutil.pole.ObjectPole;
import rosick.jglsdk.glutil.pole.ViewPole;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * III. Illumination
 * 11. Shinies
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2011.html
 * @author integeruser
 * 
 * I,J,K,L  - control the light's position. Holding LEFT_SHIFT with these keys will move in smaller increments.
 * SPACE	- toggles between drawing the uncolored cylinder and the colored one.
 * U,O      - control the specular value. They raise and low the specular exponent. Using LEFT_SHIFT in combination 
 * 				with them will raise/lower the exponent by smaller amounts.
 * Y 		- toggles the drawing of the light source.
 * T 		- toggles between the scaled and unscaled cylinder.
 * B 		- toggles the light's rotation on/off.
 * G 		- toggles between a diffuse color of (1, 1, 1) and a darker diffuse color of (0.2, 0.2, 0.2).
 * H 		- switch between Blinn, Phong and Gaussian specular. Pressing LEFT_SHIFT+H will switch between 
 * 				diffuse+specular and specular only.
 * 
 * LEFT	  CLICKING and DRAGGING				- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING				- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_ALT	- spin the object.
 * WHEEL  SCROLLING							- move the camera closer to it's target point or farther away. 
 */
public class GaussianSpecularLighting03 extends GLWindow {
	
	public static void main(String[] args) {		
		new GaussianSpecularLighting03().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;
	private final String BASEPATH = "/rosick/mckesson/III/tut11/data/";

	
	
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
	
	
	private class ProgramPairs {
		ProgramData whiteProg;
		ProgramData colorProg;
	}
	
	private class ShaderPairs {
		String strWhiteVertShader;
		String strColorVertShader;
		String strFragmentShader;
		
		ShaderPairs(String strWhiteVertShader, String strColorVertShader, String strFragmentShader) {
			this.strWhiteVertShader = strWhiteVertShader;
			this.strColorVertShader = strColorVertShader;
			this.strFragmentShader = strFragmentShader;
		}
	}
	
		
	private final int g_projectionBlockIndex = 2;
			
	private ProgramPairs g_Programs[] = new ProgramPairs[LightingModel.LM_MAX_LIGHTING_MODEL.ordinal()];
	private ShaderPairs g_ShaderFiles[] = new ShaderPairs[] {
			new ShaderPairs(BASEPATH + "PN.vert", BASEPATH + "PCN.vert", BASEPATH + "PhongLighting.frag"),
			new ShaderPairs(BASEPATH + "PN.vert", BASEPATH + "PCN.vert", BASEPATH + "PhongOnly.frag"),
			new ShaderPairs(BASEPATH + "PN.vert", BASEPATH + "PCN.vert", BASEPATH + "BlinnLighting.frag"),
			new ShaderPairs(BASEPATH + "PN.vert", BASEPATH + "PCN.vert", BASEPATH + "BlinnOnly.frag"),
			new ShaderPairs(BASEPATH + "PN.vert", BASEPATH + "PCN.vert", BASEPATH + "GaussianLighting.frag"),
			new ShaderPairs(BASEPATH + "PN.vert", BASEPATH + "PCN.vert", BASEPATH + "GaussianOnly.frag")
	};
	
	private UnlitProgData g_Unlit;
	
	private int g_projectionUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer9 	= BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
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
		for (int iProg = 0; iProg < LightingModel.LM_MAX_LIGHTING_MODEL.ordinal(); iProg++) {
			g_Programs[iProg] = new ProgramPairs();
			g_Programs[iProg].whiteProg = loadLitProgram(g_ShaderFiles[iProg].strWhiteVertShader, g_ShaderFiles[iProg].strFragmentShader);
			g_Programs[iProg].colorProg = loadLitProgram(g_ShaderFiles[iProg].strColorVertShader, g_ShaderFiles[iProg].strFragmentShader);
		}
		
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
						g_matParams.increment(false);
					} else {
						g_matParams.increment(true);
					}
					
					bChangedShininess = true;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_U) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						g_matParams.decrement(false);
					} else {
						g_matParams.decrement(true);
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
						int index;
						if (g_eLightModel.ordinal() % 2 != 0) {
							index = g_eLightModel.ordinal() - 1;
						} else {
							index = g_eLightModel.ordinal() + 1;
						}
						index %= LightingModel.LM_MAX_LIGHTING_MODEL.ordinal();
						g_eLightModel = LightingModel.values()[index];
					} else {
						int index = g_eLightModel.ordinal() + 2;
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
		
		if (bChangedShininess) {
			System.out.println("Shiny: " + g_matParams.getSpecularValue());
		}
		
		if (bChangedLightModel) {
			System.out.println(strLightModelNames[g_eLightModel.ordinal()]);
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
		
		ProgramData whiteProg = g_Programs[g_eLightModel.ordinal()].whiteProg;
		ProgramData colorProg = g_Programs[g_eLightModel.ordinal()].colorProg;		
					
		glUseProgram(whiteProg.theProgram);
		glUniform4f(whiteProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(whiteProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUniform3(whiteProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(tempFloatBuffer4));
		glUniform1f(whiteProg.lightAttenuationUnif, g_fLightAttenuation);
		glUniform1f(whiteProg.shininessFactorUnif, g_matParams.getSpecularValue());
		glUniform4(whiteProg.baseDiffuseColorUnif, g_bDrawDark ? g_darkColor.fillAndFlipBuffer(tempFloatBuffer4) : g_lightColor.fillAndFlipBuffer(tempFloatBuffer4));
		
		glUseProgram(colorProg.theProgram);
		glUniform4f(colorProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f);
		glUniform4f(colorProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f);
		glUniform3(colorProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer(tempFloatBuffer4));
		glUniform1f(colorProg.lightAttenuationUnif, g_fLightAttenuation);
		glUniform1f(colorProg.shininessFactorUnif, g_matParams.getSpecularValue());
		glUseProgram(0);
		
		{
			modelMatrix.push();

			// Render the ground plane.
			{
				modelMatrix.push();

				Mat3 normMatrix = new Mat3(modelMatrix.top());
				normMatrix = Glm.transpose(Glm.inverse(normMatrix));
				
				glUseProgram(whiteProg.theProgram);
				glUniformMatrix4(whiteProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

				glUniformMatrix3(whiteProg.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));
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
				
				ProgramData prog = g_bDrawColoredCyl ? colorProg : whiteProg;
				glUseProgram(prog.theProgram);
				glUniformMatrix4(prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

				glUniformMatrix3(prog.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));

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
		
	
	private enum LightingModel {
		LM_PHONG_SPECULAR,
		LM_PHONG_ONLY,
		LM_BLINN_SPECULAR,
		LM_BLINN_ONLY,
		LM_GAUSSIAN_SPECULAR,
		LM_GAUSSIAN_ONLY,

		LM_MAX_LIGHTING_MODEL;
	};
	
	
	private final String strLightModelNames[] = {
			"Phong Specular.",
			"Phong Only.",
			"Blinn Specular.",
			"Blinn Only.",
			"Gaussian Specular.",
			"Gaussian Only."
	};
		
	private final Vec4 g_darkColor = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
	private final Vec4 g_lightColor = new Vec4(1.0f);
	private final float g_fLightAttenuation = 1.2f;
	
	private Mesh g_pCylinderMesh;
	private Mesh g_pPlaneMesh;
	private Mesh g_pCubeMesh;
	
	private Timer g_LightTimer = new Timer(Timer.Type.TT_LOOP, 5.0f);

	private LightingModel g_eLightModel = LightingModel.LM_GAUSSIAN_SPECULAR;
	
	private boolean g_bDrawColoredCyl;
	private boolean g_bDrawLightSource;
	private boolean g_bScaleCyl;
	private boolean g_bDrawDark;
	private float g_fLightHeight = 1.5f;
	private float g_fLightRadius = 1.0f;
	
	
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

		ret.set(X, (float) (Math.cos(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius));
		ret.set(Z, (float) (Math.sin(fCurrTimeThroughLoop * (3.14159f * 2.0f)) * g_fLightRadius));

		return ret;
	}
	
	
	private boolean isGaussianLightModel() {
		return (g_eLightModel == LightingModel.LM_GAUSSIAN_ONLY || g_eLightModel == LightingModel.LM_GAUSSIAN_SPECULAR);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class MaterialParams {

		private float m_fPhongExponent;
		private float m_fBlinnExponent;
		private float m_fGaussianRoughness;

		
		MaterialParams() {
			m_fPhongExponent = 4.0f;
			m_fBlinnExponent = 4.0f;
			m_fGaussianRoughness = 0.5f;
		}
		
		
		void increment(boolean bIsLarge) {
			if (isGaussianLightModel()) {
				if (bIsLarge) {
					m_fGaussianRoughness += 0.1f;
				} else {
					m_fGaussianRoughness += 0.01f;
				}
			} else {
				switch (g_eLightModel) {
					case LM_PHONG_SPECULAR:
					case LM_PHONG_ONLY:
						if (bIsLarge) {
							m_fPhongExponent += 0.5f;
						} else {
							m_fPhongExponent += 0.1f;
						}
						break;
					case LM_BLINN_SPECULAR:
					case LM_BLINN_ONLY:
						if (bIsLarge) {
							m_fBlinnExponent += 0.5f;
						} else {
							m_fBlinnExponent += 0.1f;
						}
						break;
				}
			}
					
			clampParam();
		}
		
		void decrement(boolean bIsLarge) {
			if (isGaussianLightModel()) {
				if (bIsLarge) {
					m_fGaussianRoughness -= 0.1f;
				} else {
					m_fGaussianRoughness -= 0.01f;
				}
			} else {
				switch (g_eLightModel) {
					case LM_PHONG_SPECULAR:
					case LM_PHONG_ONLY:
						if (bIsLarge) {
							m_fPhongExponent += 0.5f;
						} else {
							m_fPhongExponent += 0.1f;
						}
						break;
					case LM_BLINN_SPECULAR:
					case LM_BLINN_ONLY:
						if (bIsLarge) {
							m_fBlinnExponent += 0.5f;
						} else {
							m_fBlinnExponent += 0.1f;
						}
						break;
				}
			}
						
			clampParam();
		}
		
		
		float getSpecularValue() {
			switch (g_eLightModel) {
				case LM_PHONG_SPECULAR:
				case LM_PHONG_ONLY:
					return m_fPhongExponent;
				case LM_BLINN_SPECULAR:
				case LM_BLINN_ONLY:
					return m_fBlinnExponent;
				case LM_GAUSSIAN_SPECULAR:
				case LM_GAUSSIAN_ONLY:
					return m_fGaussianRoughness;	
			}

			float fStopComplaint = 0.0f;
			return fStopComplaint;
		}
		

		private void clampParam() {
			switch (g_eLightModel) {
				case LM_PHONG_SPECULAR:
				case LM_PHONG_ONLY:
					if (m_fPhongExponent <= 0.0f) {
						m_fPhongExponent = 0.0001f;
					}
					break;
				case LM_BLINN_SPECULAR:
				case LM_BLINN_ONLY:
					if (m_fBlinnExponent <= 0.0f) {
						m_fBlinnExponent = 0.0001f;
					}
					break;
				case LM_GAUSSIAN_SPECULAR:
				case LM_GAUSSIAN_ONLY:
					m_fGaussianRoughness = Math.max(0.0001f, m_fGaussianRoughness);
					m_fGaussianRoughness = Math.min(1.0f, m_fGaussianRoughness);
					break;
			}
		}		
	}
	
	
	private MaterialParams g_matParams = new MaterialParams();
}