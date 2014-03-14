package fcagnin.gltut.tut14;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.gltut.framework.Framework;
import fcagnin.gltut.framework.Mesh;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 *
 * Part IV. Texturing
 * Chapter 14. Textures are not Pictures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2014.html
 * @author integeruser
 *
 * S		- switch meshes.
 * P		- toggle between perspective-correct interpolation/window-space linear interpolation.
 */
public class PerspectiveInterpolation extends LWJGLWindow {

	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut14/data/";

		new PerspectiveInterpolation().start();
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializePrograms();

		try {
			realHallway = new Mesh("RealHallway.xml");
			fauxHallway = new Mesh("FauxHallway.xml");
		} catch (Exception exception) {
			exception.printStackTrace();
			System.exit(-1);
		}
	}


	@Override
	protected void update() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_S:
					useFakeHallway = !useFakeHallway;
					if (useFakeHallway) {
						System.out.printf("Fake Hallway.\n");
					} else {
						System.out.printf("Real Hallway.\n");
					}
					break;

				case Keyboard.KEY_P:
					useSmoothInterpolation = !useSmoothInterpolation;
					if (useSmoothInterpolation) {
						System.out.printf("Perspective correct interpolation.\n");
					} else {
						System.out.printf("Just linear interpolation.\n");
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

		if (useSmoothInterpolation) {
			glUseProgram(smoothInterp.theProgram);
		} else {
			glUseProgram(linearInterp.theProgram);
		}

		if (useFakeHallway) {
			fauxHallway.render();
		} else {
			realHallway.render();
		}

		glUseProgram(0);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private float zNear = 1.0f;
	private float zFar = 1000.0f;

	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);


	private void initializePrograms() {
		smoothInterp = loadProgram("SmoothVertexColors.vert", 		"SmoothVertexColors.frag");
		linearInterp = loadProgram("NoCorrectVertexColors.vert", 	"NoCorrectVertexColors.frag");

		MatrixStack persMatrix = new MatrixStack();
		persMatrix.perspective(60.0f, 1.0f, zNear, zFar);

		glUseProgram(smoothInterp.theProgram);
		glUniformMatrix4(smoothInterp.cameraToClipMatrixUnif, false, persMatrix.top().fillAndFlipBuffer(mat4Buffer));
		glUseProgram(linearInterp.theProgram);
		glUniformMatrix4(linearInterp.cameraToClipMatrixUnif, false, persMatrix.top().fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private ProgramData smoothInterp;
	private ProgramData linearInterp;


	private class ProgramData {
		int theProgram;

		int cameraToClipMatrixUnif;
	}


	private ProgramData loadProgram(String vertexShaderFilename, String fragmentShaderFilename) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	vertexShaderFilename));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	fragmentShaderFilename));

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.cameraToClipMatrixUnif = glGetUniformLocation(data.theProgram, "cameraToClipMatrix");

		return data;
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private Mesh realHallway;
	private Mesh fauxHallway;

	private boolean useSmoothInterpolation = true;
	private boolean useFakeHallway;
}