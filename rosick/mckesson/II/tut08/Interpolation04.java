package rosick.mckesson.II.tut08;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.GLWindow;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.framework.Timer;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Quaternion;
import rosick.jglsdk.glm.Vec4;
import rosick.jglsdk.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 8. Getting Oriented 
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2008.html
 * @author integeruser
 * 
 * SPACE			- toggles between regular linear interpolation and slerp. 
 * Q,W,E,R,T,Y,U 	- cause the ship to interpolate to a new orientation.
 */
public class Interpolation04 extends GLWindow {
	
	public static void main(String[] args) {		
		new Interpolation04().start();
	}
	
	
	private final String TUTORIAL_DATAPATH = "/rosick/mckesson/II/tut08/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif, baseColorUnif;
	
	private MatrixStack currMatrix = new MatrixStack(); 

	private Mat4 cameraToClipMatrix = new Mat4();
	
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void initializeProgram() {			
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	TUTORIAL_DATAPATH + "PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, TUTORIAL_DATAPATH + "ColorMultUniform.frag"));

		theProgram = Framework.createProgram(shaderList);
		
		modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		baseColorUnif = glGetUniformLocation(theProgram, "baseColor");

		float fzNear = 1.0f; float fzFar = 600.0f;
		
		cameraToClipMatrix.set(0,	fFrustumScale);
		cameraToClipMatrix.set(5, 	fFrustumScale);
		cameraToClipMatrix.set(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.set(11, 	-1.0f);
		cameraToClipMatrix.set(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		
		try {		
			g_pShip = new Mesh(TUTORIAL_DATAPATH + "Ship.xml");
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
	}
	
	
	@Override
	protected void update() {	
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
					leaveMainLoop();
				}
				
				
				else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					boolean bSlerp = g_orient.toggleSlerp();
					System.out.printf(bSlerp ? "Slerp\n" : "Lerp\n");
					
				} else {
					for (int iOrient = 0; iOrient < g_OrientKeys.length; iOrient++) {
						if (Keyboard.getEventKey() == g_OrientKeys[iOrient]) {
							applyOrientation(iOrient);
							break;
						}
					}
				}
			}
		}
	}
	

	@Override
	protected void display() {			
		g_orient.updateTime();

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		currMatrix.clear();
		currMatrix.translate(0.0f, 0.0f, -200.0f);
		currMatrix.applyMatrix(Glm.matCast(g_orient.getOrient()));

		glUseProgram(theProgram);
		currMatrix.scale(3.0f, 3.0f, 3.0f);
		currMatrix.rotateX(-90.0f);
		//Set the base color for this object.
		glUniform4f(baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
		glUniformMatrix4(modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));

		g_pShip.render("tint");

		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {	
		cameraToClipMatrix.set(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.set(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Mesh g_pShip;
	
	private Quaternion g_Orients[] = {
			new Quaternion(0.7071f, 0.7071f, 0.0f, 0.0f),
			new Quaternion(0.5f, 0.5f, -0.5f, 0.5f),
			new Quaternion(-0.4895f, -0.7892f, -0.3700f, -0.02514f),
			new Quaternion(0.4895f, 0.7892f, 0.3700f, 0.02514f),
	
			new Quaternion(0.3840f, -0.1591f, -0.7991f, -0.4344f),
			new Quaternion(0.5537f, 0.5208f, 0.6483f, 0.0410f),
			new Quaternion(0.0f, 0.0f, 1.0f, 0.0f)
	};

	private int g_OrientKeys[] = {
			Keyboard.KEY_Q,
			Keyboard.KEY_W,
			Keyboard.KEY_E,
			Keyboard.KEY_R,
	
			Keyboard.KEY_T,
			Keyboard.KEY_Y,
			Keyboard.KEY_U
	};
		
	
	private Vec4 vectorize(Quaternion theQuat) {
		Vec4 ret = new Vec4();

		ret.x = theQuat.x;
		ret.y = theQuat.y;
		ret.z = theQuat.z;
		ret.w = theQuat.w;

		return ret;
	}
	
	
	private Quaternion lerp(Quaternion v0, Quaternion v1, float alpha) {
		Vec4 start = vectorize(v0);
		Vec4 end = vectorize(v1);
		Vec4 interp = Glm.mix(start, end, alpha);

		interp = Glm.normalize(interp);
		
		return new Quaternion(interp.w, interp.x, interp.y, interp.z);
	}
	
	
	private Quaternion slerp(Quaternion v0, Quaternion v1, float alpha) {
		float dot = Glm.dot(v0, v1);

		final float DOT_THRESHOLD = 0.9995f;
		if (dot > DOT_THRESHOLD) {
			return lerp(v0, v1, alpha);
		}
		
		Glm.clamp(dot, -1.0f, 1.0f);
		float theta_0 = (float) Math.acos(dot);
		float theta = theta_0*alpha;

		Quaternion v2 = Quaternion.add(v1, Quaternion.negate(Quaternion.scale(v0, dot)));
		v2 = Glm.normalize(v2);

		return Quaternion.add(Quaternion.scale(v0, (float) Math.cos(theta)), Quaternion.scale(v2, (float) Math.sin(theta)));
	}
	
	
	private void applyOrientation(int iIndex) {
		if (!g_orient.isAnimating()) {
			g_orient.animateToOrient(iIndex);
		}
	}
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Orientation g_orient = new Orientation();
	
	
	private class Orientation {
		
		private boolean m_bIsAnimating;
		private boolean m_bSlerp;
		private int m_ixCurrOrient;
	
		private Animation m_anim = new Animation();
		
		
		void updateTime() {
			if (m_bIsAnimating) {
				boolean bIsFinished = m_anim.updateTime();
				if (bIsFinished) {
					m_bIsAnimating = false;
					m_ixCurrOrient = m_anim.getFinalIx();
				}
			}
		}
		
		
		void animateToOrient(int ixDestination) {
			if (m_ixCurrOrient == ixDestination) {
				return;
			}

			m_anim.startAnimation(ixDestination, 1.0f);
			m_bIsAnimating = true;
		}
		
		
		boolean toggleSlerp() {
			m_bSlerp = !m_bSlerp;
			
			return m_bSlerp;
		}
		
		
		boolean isAnimating() {
			return m_bIsAnimating;
		}
		
		
		Quaternion getOrient() {
			if (m_bIsAnimating) {
				return m_anim.getOrient(g_Orients[m_ixCurrOrient], m_bSlerp);
			} else {
				return g_Orients[m_ixCurrOrient];
			}
		}
		
		
		
		private class Animation {		
			private int m_ixFinalOrient;
			private Timer m_currTimer;
			
			
			/**
			 * @return true if the animation is over
			 */
			boolean updateTime() {
				return m_currTimer.update((float) getElapsedTime());
			}
			
				
			void startAnimation(int ixDestination, float fDuration) {
				m_ixFinalOrient = ixDestination;
				m_currTimer = new Timer(Timer.Type.TT_SINGLE, fDuration);
			}
			
			
			Quaternion getOrient(Quaternion initial, boolean bSlerp) {
				if (bSlerp) {
					return slerp(initial, g_Orients[m_ixFinalOrient], m_currTimer.getAlpha());
				} else {
					return lerp(initial, g_Orients[m_ixFinalOrient], m_currTimer.getAlpha());
				}
			}

			int getFinalIx() {
				return m_ixFinalOrient;
			}
		}
	}	
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private final float fFrustumScale = calcFrustumScale(20.0f);

	
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fFovRad = fFovDeg * degToRad;
		
		return (float) (1.0f / Math.tan(fFovRad / 2.0f));
	}
}