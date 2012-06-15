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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import rosick.LWJGLWindow;
import rosick.jglsdk.BufferableData;
import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat3;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;
import rosick.jglsdk.glutil.MousePoles.*;
import rosick.mckesson.framework.Framework;
import rosick.mckesson.framework.Mesh;
import rosick.mckesson.framework.MousePole;
import rosick.mckesson.framework.Timer;
import rosick.mckesson.framework.UniformBlockArray;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * IV. Texturing
 * 14. Textures are not Pictures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2014.html
 * @author integeruser
 * 
 * P		- toggles pausing on/off.
 * -,=		- rewind/jump forward time by 0.5 second (of real-time).
 * T		- toggles a display showing the look-at point.
 * G		- toggles the drawing of the light source.
 * Y		- switches between the infinity symbol and a flat plane.
 * SPACE	- switches between one of three rendering modes: fixed shininess with a Gaussian lookup-table, a texture-based shininess with a 
 * 				Gaussian lookup-table, and a texture-based shininess with a shader-computed Gaussian term.
 * 1,2,3,4	- switch to progressively larger textures.
 * 8,9		- switch to the gold material/a material with a dark diffuse color and bright specular color.
 * 
 * LEFT	  CLICKING and DRAGGING			- rotate the camera around the target point, both horizontally and vertically.
 * LEFT	  CLICKING and DRAGGING + CTRL	- rotate the camera around the target point, either horizontally or vertically.
 * LEFT	  CLICKING and DRAGGING + ALT	- change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING			- rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL	- rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT	- spin the object.
 * WHEEL  SCROLLING						- move the camera closer to it's target point or farther away. 
 */
public class MaterialTexture03 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/IV/tut14/data/";

		new MaterialTexture03().start();
	}
	
	
	private final static int FLOAT_SIZE = Float.SIZE / 8;

	
	
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
		
	
	private class ShaderPairs {
		String vertShader;
		String fragShader;
		
		ShaderPairs(String vertShader, String fragShader) {
			this.vertShader = vertShader;
			this.fragShader = fragShader;
		}
	};
	
	
	private final int g_materialBlockIndex = 0;
	private final int g_lightBlockIndex = 1;
	private final int g_projectionBlockIndex = 2;
	
	private ProgramData g_Programs[] = new ProgramData[ShaderMode.NUM_SHADER_MODES.ordinal()];
	private UnlitProgData g_Unlit;
	
	private ShaderPairs g_shaderPairs[] = new ShaderPairs[] {
			new ShaderPairs("PN.vert", 	"FixedShininess.frag"),
			new ShaderPairs("PNT.vert", "TextureShininess.frag"),
			new ShaderPairs("PNT.vert", "TextureCompute.frag")
	};
	
	private int g_lightUniformBuffer;
	private int g_projectionUniformBuffer;
	private int g_materialUniformBuffer;
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private MatrixStack modelMatrix = new MatrixStack();

	private FloatBuffer tempFloatBuffer4 	= BufferUtils.createFloatBuffer(4);
	private FloatBuffer tempFloatBuffer9 	= BufferUtils.createFloatBuffer(9);
	private FloatBuffer tempFloatBuffer16 	= BufferUtils.createFloatBuffer(16);
	private FloatBuffer tempFloatBuffer24 	= BufferUtils.createFloatBuffer(24);

	
	
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
		int shininessTextureUnif = glGetUniformLocation(data.theProgram, "shininessTexture");
		glUseProgram(data.theProgram);
		glUniform1i(gaussianTextureUnif, g_gaussTexUnit);
		glUniform1i(shininessTextureUnif, g_shineTexUnit);
		glUseProgram(0);
		
		return data;
	}
	
	private void initializePrograms() {	
		for (int prog = 0; prog < ShaderMode.NUM_SHADER_MODES.ordinal(); prog++) {
			g_Programs[prog] = loadStandardProgram(g_shaderPairs[prog].vertShader, g_shaderPairs[prog].fragShader);
		}

		g_Unlit = loadUnlitProgram("Unlit.vert", "Unlit.frag");
	}
	
	
	@Override
	protected void init() {	
		initializePrograms();

		try {
			g_pObjectMesh = new Mesh("Infinity.xml");
			g_pCubeMesh = 	new Mesh("UnitCube.xml");
			g_pPlaneMesh = 	new Mesh("UnitPlane.xml");
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
		setupMaterials();
		
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
		createShininessTexture();
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
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_P:
					g_lightTimer.togglePause();
					break;
					
				case Keyboard.KEY_MINUS:
					g_lightTimer.rewind(0.5f);
					break;
					
				case Keyboard.KEY_EQUALS:
					g_lightTimer.fastForward(0.5f);
					break;

				case Keyboard.KEY_T:
					g_bDrawCameraPos = !g_bDrawCameraPos;
					break;
					
				case Keyboard.KEY_G:
					g_bDrawLights = !g_bDrawLights;
					break;
				
				case Keyboard.KEY_Y:
					g_bUseInfinity = !g_bUseInfinity;
					break;
					
				case Keyboard.KEY_SPACE:
					int index = (g_eMode.ordinal() + 1) % ShaderMode.NUM_SHADER_MODES.ordinal();
					g_eMode = ShaderMode.values()[index];

					System.out.printf("%s\n", g_shaderModeNames[g_eMode.ordinal()]);
					break;
					
				case Keyboard.KEY_ESCAPE:
					leaveMainLoop();
					break;
				}

				
				if (Keyboard.KEY_1 <= Keyboard.getEventKey() && Keyboard.getEventKey() <= Keyboard.KEY_9) {
					int number = Keyboard.getEventKey() - Keyboard.KEY_1;
					if (number < NUM_GAUSS_TEXTURES) {
						System.out.printf("Angle Resolution: %d\n", calcCosAngResolution(number));
						g_currTexture = number;
					}
					
					if (number >= (9 - NUM_MATERIALS)) {
						number = number - (9 - NUM_MATERIALS);
						System.out.printf("Material number %d\n", number);
						g_currMaterial = number;
					}
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		g_lightTimer.update((float) getElapsedTime());

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
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer(tempFloatBuffer24));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		{
			Mesh pMesh = g_bUseInfinity ? g_pObjectMesh : g_pPlaneMesh;
			
			glBindBufferRange(GL_UNIFORM_BUFFER, g_materialBlockIndex, g_materialUniformBuffer, g_currMaterial * g_materialOffset, MaterialBlock.SIZE);

			modelMatrix.push();
			
			modelMatrix.applyMatrix(g_objtPole.calcMatrix());
			modelMatrix.scale(g_bUseInfinity ? 2.0f : 4.0f);
			
			Mat3 normMatrix = new Mat3(modelMatrix.top());
			normMatrix = Glm.transpose(Glm.inverse(normMatrix));

			ProgramData prog = g_Programs[g_eMode.ordinal()];

			glUseProgram(prog.theProgram);
			glUniformMatrix4(prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
			glUniformMatrix3(prog.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer(tempFloatBuffer9));

			glActiveTexture(GL_TEXTURE0 + g_gaussTexUnit);
			glBindTexture(GL_TEXTURE_2D, g_gaussTextures[g_currTexture]);
			glBindSampler(g_gaussTexUnit, g_textureSampler);

			glActiveTexture(GL_TEXTURE0 + g_shineTexUnit);
			glBindTexture(GL_TEXTURE_2D, g_shineTexture);
			glBindSampler(g_shineTexUnit, g_textureSampler);
			
			if (g_eMode != ShaderMode.MODE_FIXED) {
				pMesh.render("lit-tex");
			} else {
				pMesh.render("lit");				
			}
			
			glBindSampler(g_gaussTexUnit, 0);
			glBindTexture(GL_TEXTURE_2D, 0);

			glUseProgram(0);
			glBindBufferBase(GL_UNIFORM_BUFFER, g_materialBlockIndex, 0);
			
			modelMatrix.pop();
		}

		if (g_bDrawLights) {
			modelMatrix.push();
			
			modelMatrix.translate(new Vec3(calcLightPosition()));
			modelMatrix.scale(0.25f);

			glUseProgram(g_Unlit.theProgram);
			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

			Vec4 lightColor = new Vec4(1.0f);
			glUniform4(g_Unlit.objectColorUnif, lightColor.fillAndFlipBuffer(tempFloatBuffer4));
			g_pCubeMesh.render("flat");
			
			modelMatrix.pop();

			modelMatrix.translate(globalLightDirection.scale(100.0f));
			modelMatrix.scale(5.0f);

			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
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
			glUniformMatrix4(g_Unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
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
		glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer(tempFloatBuffer16));
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private class MaterialBlock extends BufferableData<ByteBuffer> {
		Vec4 diffuseColor;
		Vec4 specularColor;
		float specularShininess;
		float padding[] = new float[3];

		static final int SIZE = (4 + 4 + 1 + 3) * FLOAT_SIZE;

		@Override
		public ByteBuffer fillBuffer(ByteBuffer buffer) {
			buffer.putFloat(diffuseColor.x);
			buffer.putFloat(diffuseColor.y);
			buffer.putFloat(diffuseColor.z);
			buffer.putFloat(diffuseColor.w);

			buffer.putFloat(specularColor.x);
			buffer.putFloat(specularColor.y);
			buffer.putFloat(specularColor.z);
			buffer.putFloat(specularColor.w);

			buffer.putFloat(specularShininess);
			
			for (int i = 0; i < 3; i++) {
				buffer.putFloat(padding[i]);
			}
			
			return buffer;
		}
	}
	
	
	private class PerLight extends BufferableData<FloatBuffer> {
		Vec4 cameraSpaceLightPos;
		Vec4 lightIntensity;
		
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
		float padding[] = new float[3];
		PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

		static final int SIZE = (4 + 1 + 3 + (8 * 4)) * FLOAT_SIZE;

		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {			
			ambientIntensity.fillBuffer(buffer);
			buffer.put(lightAttenuation);
			buffer.put(padding);
			
			for (PerLight light : lights) {
				light.fillBuffer(buffer);
			}
			
			return buffer;
		}
	}


	private class ProjectionBlock extends BufferableData<FloatBuffer> {
		Mat4 cameraToClipMatrix;
		
		static final int SIZE = 16 * FLOAT_SIZE;
		
		@Override
		public FloatBuffer fillBuffer(FloatBuffer buffer) {
			return cameraToClipMatrix.fillBuffer(buffer);
		}
	}
			
	
	private enum ShaderMode {
		MODE_FIXED,
		MODE_TEXTURED,
		MODE_TEXTURED_COMPUTE,

		NUM_SHADER_MODES
	};
	
	
	private final int NUMBER_OF_LIGHTS = 2;
	private final int NUM_GAUSS_TEXTURES = 4;
	private final int NUM_MATERIALS = 2;
	private final int g_gaussTexUnit = 0;
	private final int g_shineTexUnit = 1;
	private final float g_fHalfLightDistance = 25.0f;
	private final float g_fLightAttenuation = 1.0f / (g_fHalfLightDistance * g_fHalfLightDistance);

	private final String g_shaderModeNames[] = {
			"Fixed Shininess with Gaussian Texture",
			"Texture Shininess with Gaussian Texture",
			"Texture Shininess with computed Gaussian"
	};
	
	private Mesh g_pObjectMesh;
	private Mesh g_pCubeMesh;
	private Mesh g_pPlaneMesh;

	private Timer g_lightTimer = new Timer(Timer.Type.TT_LOOP, 6.0f);
	
	private ShaderMode g_eMode = ShaderMode.MODE_FIXED;
	
	private boolean g_bDrawLights = true;
	private boolean g_bUseInfinity = true;
	private boolean g_bDrawCameraPos;
	private int g_gaussTextures[] = new int[NUM_GAUSS_TEXTURES];
	private int g_materialOffset;
	private int g_textureSampler;
	private int g_shineTexture;	
	private int g_currTexture = NUM_GAUSS_TEXTURES - 1;
	private int g_currMaterial;	
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
			0.0f, 0.0f,																// No camera movement.
			90.0f / 250.0f
	);

	private ViewPole g_viewPole = new ViewPole(g_initialViewData, g_viewScale, MouseButtons.MB_LEFT_BTN);
	private ObjectPole g_objtPole = new ObjectPole(g_initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, g_viewPole);


	private void setupMaterials() {
		UniformBlockArray<MaterialBlock> mtls = new UniformBlockArray<>(MaterialBlock.SIZE, NUM_MATERIALS);

		MaterialBlock mtl = new MaterialBlock();
		mtl.diffuseColor = new Vec4(1.0f, 0.673f, 0.043f, 1.0f);
		mtl.specularColor = new Vec4(1.0f, 0.673f, 0.043f, 1.0f).scale(0.4f);
		mtl.specularShininess = 0.125f;
		mtls.set(0, mtl);

		mtl = new MaterialBlock();
		mtl.diffuseColor = new Vec4(0.01f, 0.01f, 0.01f, 1.0f);
		mtl.specularColor = new Vec4(0.99f, 0.99f, 0.99f, 1.0f);
		mtl.specularShininess = 0.125f;
		mtls.set(1, mtl);

		g_materialUniformBuffer = mtls.createBufferObject();
		g_materialOffset = mtls.getArrayOffset();
	}
	
	
	private void buildGaussianData(byte textureData[], int cosAngleResolution, int shininessResolution) {
		int currIt = 0;
		
		for (int iShin = 1; iShin <= shininessResolution; iShin++) {
			float shininess = iShin / (float)(shininessResolution);
			
			for (int iCosAng = 0; iCosAng < cosAngleResolution; iCosAng++) {
				float cosAng = iCosAng / (float)(cosAngleResolution - 1);
				float angle = (float) Math.acos(cosAng);
				float exponent = angle / shininess;
				exponent = -(exponent * exponent);
				float gaussianTerm = (float) Math.exp(exponent);
	
				textureData[currIt] = (byte) (char) (gaussianTerm * 255.0f);
				currIt++;
			}
		}
	}
	
	
	private void createGaussianTextures() {
		for (int loop = 0; loop < NUM_GAUSS_TEXTURES; loop++) {
			int cosAngleResolution = calcCosAngResolution(loop);
			g_gaussTextures[loop] = createGaussianTexture(cosAngleResolution, 128);
		}

		g_textureSampler = glGenSamplers();
		glSamplerParameteri(g_textureSampler, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(g_textureSampler, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glSamplerParameteri(g_textureSampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(g_textureSampler, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}
	
	private int createGaussianTexture(int cosAngleResolution, int shininessResolution) {	
		byte[] textureData = new byte[shininessResolution * cosAngleResolution];
				
		buildGaussianData(textureData, cosAngleResolution, shininessResolution);
		
		ByteBuffer textureDataBuffer = BufferUtils.createByteBuffer(textureData.length);
		textureDataBuffer.put(textureData);
		textureDataBuffer.flip();
		
		int gaussTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, gaussTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, cosAngleResolution, shininessResolution, 0, GL11.GL_RED, GL_UNSIGNED_BYTE, textureDataBuffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		return gaussTexture;
	}
	
	
	private void createShininessTexture() {
		ImageSet pImageSet;

		try {
			String filepath = Framework.findFileOrThrow("main.dds");
			pImageSet = DdsLoader.loadFromFile(filepath);
			
			SingleImage image = pImageSet.getImage(0, 0, 0);
			Dimensions dims = image.getDimensions();

			g_shineTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, g_shineTexture);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, dims.width, dims.height, 0, 
					GL11.GL_RED, GL_UNSIGNED_BYTE, image.getImageData());
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
			
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private int calcCosAngResolution(int level) {
		final int cosAngleStart = 64;
		
		return cosAngleStart * (int) (Math.pow(2.0f, level));
	}
	
	
	private Vec4 calcLightPosition() {
		final float fScale = 3.14159f * 2.0f;

		float timeThroughLoop = g_lightTimer.getAlpha();

		Vec4 ret = new Vec4(0.0f, g_lightHeight, 0.0f, 1.0f);

		ret.x = (float) (Math.cos(timeThroughLoop * fScale) * g_lightRadius);
		ret.z = (float) (Math.sin(timeThroughLoop * fScale) * g_lightRadius);

		return ret;
	}
}