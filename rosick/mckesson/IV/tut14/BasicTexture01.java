package rosick.mckesson.IV.tut14;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

import static rosick.glm.Vec.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import rosick.GLWindow;
import rosick.PortingUtils.Bufferable;
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
 * IV. Texturing
 * 14. Textures are not Pictures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2014.html
 * @author integeruser
 * 
 * P		- toggle pausing on/off.
 * -,=		- rewind/jump forward time by 0.5 second (of real-time).
 * T		- toggle a display showing the look-at point.
 * G		- toggles the drawing of the light source.
 * SPACEBAR	- toggle between shader-based Gaussian specular and texture-based specular.
 * 1,2,3,4	- switch to progressively larger textures.
 * 
 * LEFT	  CLICKING and DRAGGING				- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + LEFT_ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING				- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + LEFT_ALT	- spin the object.
 * WHEEL  SCROLLING							- move the camera closer to it's target point or farther away. 
 */
public class BasicTexture01 extends GLWindow {
	
	public static void main(String[] args) {		
		new BasicTexture01().start();
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/IV/tut14/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class ProgramData {
		int theProgram;

		int modelToCameraMatrixUnif;
		int normalModelToCameraMatrixUnif;
	}
	
	private class UnlitProgData {
		int theProgram;

		int objectColorUnif;
		int modelToCameraMatrixUnif;
	}
		
		
	private final int g_materialBlockIndex = 0;
	private final int g_lightBlockIndex = 1;
	private final int g_projectionBlockIndex = 2;
	
	private ProgramData g_litShaderProg;
	private ProgramData g_litTextureProg;	
	private UnlitProgData g_Unlit;
	
	private int g_lightUniformBuffer;
	private int g_projectionUniformBuffer;
	private int g_materialUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempSharedFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempSharedFloatBuffer9 	= BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempSharedFloatBuffer16 = BufferUtils.createFloatBuffer(16);
	private FloatBuffer tempSharedFloatBuffer24 = BufferUtils.createFloatBuffer(24);

	
	
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
	
	private ProgramData loadStandardProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");
		data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "normalModelToCameraMatrix");

		int materialBlock = glGetUniformBlockIndex(data.theProgram, "Material");
		int lightBlock = glGetUniformBlockIndex(data.theProgram, "Light");
		int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");

		glUniformBlockBinding(data.theProgram, materialBlock, g_materialBlockIndex);
		glUniformBlockBinding(data.theProgram, lightBlock, g_lightBlockIndex);
		glUniformBlockBinding(data.theProgram, projectionBlock, g_projectionBlockIndex);

		int gaussianTextureUnif = glGetUniformLocation(data.theProgram, "gaussianTexture");
		glUseProgram(data.theProgram);
		glUniform1i(gaussianTextureUnif, g_gaussTexUnit);
		glUseProgram(0);
		
		return data;
	}
	
	private void initializePrograms() {	
		g_litShaderProg = loadStandardProgram(BASEPATH + "PN.vert", BASEPATH + "ShaderGaussian.frag");
		g_litTextureProg = loadStandardProgram(BASEPATH + "PN.vert", BASEPATH + "TextureGaussian.frag");

		g_Unlit = loadUnlitProgram(BASEPATH + "Unlit.vert", BASEPATH + "Unlit.frag");
	}
	
	
	@Override
	protected void init() {
		initializePrograms();
		
		try {
			g_pObjectMesh = new Mesh(BASEPATH + "Infinity.xml");
			g_pCubeMesh = 	new Mesh(BASEPATH + "UnitCube.xml");
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

		// Setup our Uniform Buffers
		MaterialBlock mtl = new MaterialBlock();
		mtl.diffuseColor = new Vec4(1.0f, 0.673f, 0.043f, 1.0f);
		mtl.specularColor = new Vec4(1.0f, 0.673f, 0.043f, 1.0f).scale(0.4f);
		mtl.specularShininess = g_specularShininess;
		
		g_materialUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, g_materialUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, mtl.fillBuffer(BufferUtils.createFloatBuffer(12)), GL_STATIC_DRAW);
		
		g_lightUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		g_projectionUniformBuffer = glGenBuffers();	       
		glBindBuffer(GL_UNIFORM_BUFFER, g_projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW);	
		
		// Bind the static buffers.
		glBindBufferRange(GL_UNIFORM_BUFFER, g_lightBlockIndex, g_lightUniformBuffer, 0, LightBlock.SIZE);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer, 0, ProjectionBlock.SIZE);

		glBindBufferRange(GL_UNIFORM_BUFFER, g_materialBlockIndex, g_materialUniformBuffer, 0, MaterialBlock.SIZE);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		createGaussianTextures();
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
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					g_lightTimer.togglePause();
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_MINUS) {
					g_lightTimer.rewind(0.5f);
										
				} else if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS) {
					g_lightTimer.fastForward(0.5f);
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
					g_bDrawCameraPos = !g_bDrawCameraPos;
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_G) {
					g_bDrawLights = !g_bDrawLights;
				
				} else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					g_bUseTexture = !g_bUseTexture;
					if (g_bUseTexture) {
						System.out.println("Texture");
					} else {
						System.out.println("Shader");
					}
					
				} else if (Keyboard.KEY_1 <= Keyboard.getEventKey() && Keyboard.getEventKey() <= Keyboard.KEY_9  ) {
					int number = Keyboard.getEventKey() - Keyboard.KEY_1;
					if (number < NUM_GAUSS_TEXTURES) {
						System.out.println("Angle Resolution: "+ calcCosAngResolution(number));
						g_currTexture = number;
					}
					
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					leaveMainLoop();
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		g_lightTimer.update(getElapsedTime());

		glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		modelMatrix.clear();
		modelMatrix.setMatrix(g_viewPole.calcMatrix());
		final Mat4 worldToCamMat = modelMatrix.top();

		LightBlock lightData = new LightBlock();

		lightData.ambientIntensity = new Vec4(0.2f, 0.2f, 0.2f, 1.0f);
		lightData.lightAttenuation = g_fLightAttenuation;

		Vec3 globalLightDirection = new Vec3(0.707f, 0.707f, 0.0f);

		lightData.lights[0] = new PerLight();
		lightData.lights[0].cameraSpaceLightPos = Mat4.mul(worldToCamMat, new Vec4(globalLightDirection, 0.0f));
		lightData.lights[0].lightIntensity = new Vec4(0.6f, 0.6f, 0.6f, 1.0f);

		lightData.lights[1] = new PerLight();
		lightData.lights[1].cameraSpaceLightPos = Mat4.mul(worldToCamMat, calcLightPosition());
		lightData.lights[1].lightIntensity = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);

		glBindBuffer(GL_UNIFORM_BUFFER, g_lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillBuffer(tempSharedFloatBuffer24));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		{
			glBindBufferRange(GL_UNIFORM_BUFFER, g_materialBlockIndex, g_materialUniformBuffer, 0, MaterialBlock.SIZE);

			modelMatrix.push();
			
			modelMatrix.applyMatrix(g_objtPole.calcMatrix());
			modelMatrix.scale(2.0f);
			
			Mat3 normMatrix = new Mat3(modelMatrix.top());
			normMatrix = Glm.transpose(Glm.inverse(normMatrix));

			ProgramData prog = g_bUseTexture ? g_litTextureProg : g_litShaderProg;

			glUseProgram(prog.theProgram);
			glUniformMatrix4(prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));
			glUniformMatrix3(prog.normalModelToCameraMatrixUnif, false, normMatrix.fillBuffer(tempSharedFloatBuffer9));

			glActiveTexture(GL_TEXTURE0 + g_gaussTexUnit);
			glBindTexture(GL_TEXTURE_1D, g_gaussTextures[g_currTexture]);
			glBindSampler(g_gaussTexUnit, g_gaussSampler);

			g_pObjectMesh.render("lit");

			glBindSampler(g_gaussTexUnit, 0);
			glBindTexture(GL_TEXTURE_1D, 0);

			glUseProgram(0);
			glBindBufferBase(GL_UNIFORM_BUFFER, g_materialBlockIndex, 0);
			
			modelMatrix.pop();
		}

		if (g_bDrawLights) {
			modelMatrix.push();
			
			modelMatrix.translate(new Vec3(calcLightPosition()));
			modelMatrix.scale(0.25f);

			glUseProgram(g_Unlit.theProgram);
			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));

			Vec4 lightColor = new Vec4(1.0f);
			glUniform4(g_Unlit.objectColorUnif, lightColor.fillBuffer(tempSharedFloatBuffer4));
			g_pCubeMesh.render("flat");
			
			modelMatrix.pop();

			modelMatrix.translate(globalLightDirection.scale(100.0f));
			modelMatrix.scale(5.0f);

			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));
			g_pCubeMesh.render("flat");

			glUseProgram(0);
		}

		if (g_bDrawCameraPos) {
			modelMatrix.push();

			modelMatrix.setIdentity();
			modelMatrix.translate(new Vec3(0.0f, 0.0f, - g_viewPole.getView().radius));
			modelMatrix.scale(0.25f);

			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			glUseProgram(g_Unlit.theProgram);
			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedFloatBuffer16));
			glUniform4f(g_Unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f);
			g_pCubeMesh.render("flat");
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(g_Unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			g_pCubeMesh.render("flat");
			
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
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillBuffer(tempSharedFloatBuffer16));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private class MaterialBlock implements Bufferable<FloatBuffer> {
		Vec4 diffuseColor;
		Vec4 specularColor;
		float specularShininess;
		float padding[] = new float[3];

		static final int SIZE = (4 + 4 + 1 + 3) * (Float.SIZE / 8);

		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			float data[] = new float[12];
			System.arraycopy(diffuseColor.get(), 0, data, 0, 4);
			System.arraycopy(specularColor.get(), 0, data, 4, 4);
			data[8] = specularShininess;
			System.arraycopy(padding, 0, data, 9, padding.length);

			buffer.clear();
			buffer.put(data);
			buffer.flip();
			
			return buffer;
		}
	}
	
	
	private class PerLight {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;
	};
	
	private class LightBlock implements Bufferable<FloatBuffer> {
		Vec4 ambientIntensity;
		float lightAttenuation;
		float padding[] = new float[3];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = (4 + 1 + 3 + (8 * 2)) * (Float.SIZE / 8);


		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			float data[] = new float[24];
			System.arraycopy(ambientIntensity.get(), 0, data, 0, 4);
			data[4] = lightAttenuation;
			System.arraycopy(padding, 0, data, 5, padding.length);

			for (int i = 0; i < lights.length; i++) {
				float light[] = new float[8];
				System.arraycopy(lights[i].cameraSpaceLightPos.get(), 0, light, 0, 4);
				System.arraycopy(lights[i].lightIntensity.get(), 0, light, 4, 4);
				
				System.arraycopy(light, 0, data, 8 + i * 8, 8);
			}
			
			buffer.clear();
			buffer.put(data);
			buffer.flip();
			
			return buffer;
		}
	}


	private class ProjectionBlock implements Bufferable<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = 16 * (Float.SIZE / 8);

		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
	
	
	private final int NUMBER_OF_LIGHTS = 2;
	private final int NUM_GAUSS_TEXTURES = 4;
	private final int g_gaussTexUnit = 0;
	private final float g_fHalfLightDistance = 25.0f;
	private final float g_fLightAttenuation = 1.0f / (g_fHalfLightDistance * g_fHalfLightDistance);

	private Mesh g_pObjectMesh;
	private Mesh g_pCubeMesh;
	
	private Timer g_lightTimer = new Timer(Timer.Type.TT_LOOP, 6.0f);
	
	private boolean g_bDrawLights = true;
	private boolean g_bDrawCameraPos;
	private boolean g_bUseTexture;
	private int g_gaussTextures[] = new int[NUM_GAUSS_TEXTURES];
	private int g_gaussSampler;
	private int g_currTexture;	
	private float g_specularShininess = 0.2f;
	private float g_lightHeight = 1.0f;
	private float g_lightRadius = 3.0f;

	
	// View/Object Setup
	
	private ObjectData g_initialObjectData = new ObjectData(
		new Vec3(0.0f, 0.5f, 0.0f),
		new Quaternion(1.0f, 0.0f, 0.0f, 0.0f)
	);

	private ViewData g_initialViewData = new ViewData(
		new Vec3(g_initialObjectData.position),
		new Quaternion(0.92387953f, 0.3826834f, 0.0f, 0.0f),
		10.0f,
		0.0f
	);

	private ViewScale g_viewScale = new ViewScale(	
		1.5f, 70.0f,
		1.5f, 0.5f,
		0.0f, 0.0f,																	// No camera movement.
		90.0f / 250.0f
	);

	private ViewPole g_viewPole = new ViewPole(g_initialViewData, g_viewScale, MouseButtons.MB_LEFT_BTN);
	private ObjectPole g_objtPole = new ObjectPole(g_initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, g_viewPole);


	private void buildGaussianData(byte textureData[], int cosAngleResolution) {
		for (int iCosAng = 0; iCosAng < cosAngleResolution; iCosAng++) {
			float cosAng = iCosAng / (float) (cosAngleResolution - 1);
			float angle = (float) Math.acos(cosAng);
			float exponent = angle / g_specularShininess;
			exponent = - (exponent * exponent);
			float gaussianTerm = (float) Math.exp(exponent);
			
			textureData[iCosAng] = (byte) (gaussianTerm * 255.0f);
		}
	}
	
	
	private void createGaussianTextures() {
		for (int loop = 0; loop < NUM_GAUSS_TEXTURES; loop++) {
			int cosAngleResolution = calcCosAngResolution(loop);
			g_gaussTextures[loop] = createGaussianTexture(cosAngleResolution);
		}

		g_gaussSampler = glGenSamplers();
		glSamplerParameteri(g_gaussSampler, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(g_gaussSampler, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glSamplerParameteri(g_gaussSampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	}
	
	private int createGaussianTexture(int cosAngleResolution) {	
		byte[] textureData = new byte[cosAngleResolution];
				
		buildGaussianData(textureData, cosAngleResolution);
		
		ByteBuffer tempByteBuffer = BufferUtils.createByteBuffer(textureData.length);
		tempByteBuffer.put(textureData);
		tempByteBuffer.flip();
		
		int gaussTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_1D, gaussTexture);
		glTexImage1D(GL_TEXTURE_1D, 0, GL_R8, cosAngleResolution, 0, GL11.GL_RED, GL_UNSIGNED_BYTE, tempByteBuffer);
		glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAX_LEVEL, 0);
		glBindTexture(GL_TEXTURE_1D, 0);

		return gaussTexture;
	}
	
	
	private int calcCosAngResolution(int level) {
		final int cosAngleStart = 64;
		
		return cosAngleStart * (int) (Math.pow(2.0f, level));
	}
	
	
	private Vec4 calcLightPosition() {
		final float fScale = 3.14159f * 2.0f;

		float timeThroughLoop = g_lightTimer.getAlpha();

		Vec4 ret = new Vec4(0.0f, g_lightHeight, 0.0f, 1.0f);

		ret.set(X, (float) (Math.cos(timeThroughLoop * fScale) * g_lightRadius));
		ret.set(Z, (float) (Math.sin(timeThroughLoop * fScale) * g_lightRadius));

		return ret;
	}
}