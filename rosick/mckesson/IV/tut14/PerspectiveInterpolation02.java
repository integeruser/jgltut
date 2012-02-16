package rosick.mckesson.IV.tut14;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.GLWindow;
import rosick.jglsdk.framework.Framework;
import rosick.jglsdk.framework.Mesh;
import rosick.jglsdk.glutil.MatrixStack;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * IV. Texturing
 * 14. Textures are not Pictures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2014.html
 * @author integeruser
 *  
 * S		- switchs meshes. 
 * P		- toggles between perspective-correct interpolation/window-space linear interpolation.
 * SPACE	- reloads meshes.
 */
public class PerspectiveInterpolation02 extends GLWindow {
	
	public static void main(String[] args) {		
		new PerspectiveInterpolation02().start();
	}
	
	
	private final String BASEPATH = "/rosick/mckesson/IV/tut14/data/";
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private class ProgramData {
		int theProgram;

		int cameraToClipMatrixUnif;
	}
	
	
	private ProgramData g_SmoothInterp;
	private ProgramData g_LinearInterp;	
	
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;
	
	private FloatBuffer tempFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ProgramData loadProgram(String strVertexShader, String strFragmentShader) {		
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	strVertexShader));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.cameraToClipMatrixUnif = glGetUniformLocation(data.theProgram, "cameraToClipMatrix");

		return data;
	}
	
	private void initializePrograms() {	
		g_SmoothInterp = loadProgram(BASEPATH + "SmoothVertexColors.vert", BASEPATH + "SmoothVertexColors.frag");
		g_LinearInterp = loadProgram(BASEPATH + "NoCorrectVertexColors.vert", BASEPATH + "NoCorrectVertexColors.frag");

		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(60.0f, 1.0f, g_fzNear, g_fzFar);

		glUseProgram(g_SmoothInterp.theProgram);
		glUniformMatrix4(g_SmoothInterp.cameraToClipMatrixUnif, false, persMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(g_LinearInterp.theProgram);
		glUniformMatrix4(g_LinearInterp.cameraToClipMatrixUnif, false, persMatrix.top().fillAndFlipBuffer(tempFloatBuffer16));
		glUseProgram(0);	
	}
	
	
	@Override
	protected void init() {
		initializePrograms();

		try {
			g_pRealHallway = new Mesh(BASEPATH + "RealHallway.xml");
			g_pFauxHallway = new Mesh(BASEPATH + "FauxHallway.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(0);
		}
	}
	

	@Override
	protected void update() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_S) {
					g_bUseFakeHallway = !g_bUseFakeHallway;
					if (g_bUseFakeHallway) {
						System.out.println("Fake Hallway.");
					} else {
						System.out.println("Real Hallway.");
					}
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					g_bUseSmoothInterpolation = !g_bUseSmoothInterpolation;
					if (g_bUseSmoothInterpolation) {
						System.out.println("Perspective correct interpolation.");
					} else {
						System.out.println("Just linear interpolation.");
					}
											
				} else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					// Reload
					g_pRealHallway = new Mesh(BASEPATH + "RealHallway.xml");
					g_pFauxHallway = new Mesh(BASEPATH + "FauxHallway.xml");
					
					
				} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
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
		
		if (g_bUseSmoothInterpolation) {
			glUseProgram(g_SmoothInterp.theProgram);
		} else {
			glUseProgram(g_LinearInterp.theProgram);
		}
		
		if (g_bUseFakeHallway) {
			g_pFauxHallway.render();
		} else {
			g_pRealHallway.render();
		}
			
		glUseProgram(0);
	}	
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private Mesh g_pRealHallway;
	private Mesh g_pFauxHallway;

	private boolean g_bUseSmoothInterpolation = true;
	private boolean g_bUseFakeHallway;
}