package fcagnin.gltut.tut06;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.BufferUtils;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat3;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.gltut.framework.Framework;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 *
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author integeruser
 */
public class Rotation extends LWJGLWindow {

	public static void main(String[] args) {
		Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut06/data/";

		new Rotation().start();
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer();

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		int colorDataOffset = FLOAT_SIZE * 3 * numberOfVertices;
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

		glBindVertexArray(0);

	    glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);

	    glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
	}


	@Override
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glUseProgram(theProgram);

		glBindVertexArray(vao);

		float elapsedTime = getElapsedTime() / 1000.0f;
		for (Instance currInst : instanceList) {
			final Mat4 transformMatrix = currInst.constructMatrix(elapsedTime);

			glUniformMatrix4(modelToCameraMatrixUnif, false, transformMatrix.fillAndFlipBuffer(mat4Buffer));
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		}

		glBindVertexArray(0);
		glUseProgram(0);
	}


	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrix.set(0, 0, frustumScale / (width / (float) height));
		cameraToClipMatrix.set(1, 1, frustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	private int vao;

	private Mat4 cameraToClipMatrix = new Mat4(0.0f);

	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(Mat4.SIZE);


	private void initializeProgram() {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER,	"PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorPassthrough.frag"));

		theProgram = Framework.createProgram(shaderList);

	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");

		float zNear = 1.0f; float zFar = 61.0f;

		cameraToClipMatrix.set(0, 0, 	frustumScale);
		cameraToClipMatrix.set(1, 1, 	frustumScale);
		cameraToClipMatrix.set(2, 2,	(zFar + zNear) / (zNear - zFar));
		cameraToClipMatrix.set(2, 3,	-1.0f);
		cameraToClipMatrix.set(3, 2,	(2 * zFar * zNear) / (zNear - zFar));

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final int numberOfVertices = 8;

	private final float vertexData[] = {
			+1.0f, +1.0f, +1.0f,
			-1.0f, -1.0f, +1.0f,
			-1.0f, +1.0f, -1.0f,
			+1.0f, -1.0f, -1.0f,

			-1.0f, -1.0f, -1.0f,
			+1.0f, +1.0f, -1.0f,
			+1.0f, -1.0f, +1.0f,
			-1.0f, +1.0f, +1.0f,

			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f,

			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f};
	private final short indexData[] = {
			0, 1, 2,
			1, 0, 3,
			2, 3, 0,
			3, 2, 1,

			5, 4, 6,
			4, 5, 7,
			7, 6, 4,
			6, 7, 5};

	private int vertexBufferObject, indexBufferObject;


	private void initializeVertexBuffer() {
		FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
		vertexDataBuffer.put(vertexData);
		vertexDataBuffer.flip();

        vertexBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		ShortBuffer indexDataBuffer = BufferUtils.createShortBuffer(indexData.length);
		indexDataBuffer.put(indexData);
		indexDataBuffer.flip();

        indexBufferObject = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private Instance instanceList[] = {
			new NullRotation	(new Vec3(0.0f, 0.0f, -25.0f)),
			new RotateX			(new Vec3(-5.0f, -5.0f, -25.0f)),
			new RotateY			(new Vec3(-5.0f, 5.0f, -25.0f)),
			new RotateZ			(new Vec3(5.0f, 5.0f, -25.0f)),
			new RotateAxis		(new Vec3(5.0f, -5.0f, -25.0f))};


	private abstract class Instance {
		Vec3 offset;


		abstract Mat3 calcRotation(float elapsedTime);

		Mat4 constructMatrix(float elapsedTime) {
			final Mat3 rotMatrix = calcRotation(elapsedTime);

			Mat4 theMat = new Mat4(rotMatrix);
			theMat.setColumn(3, new Vec4(offset, 1.0f));

			return theMat;
		}
	}


	private class NullRotation extends Instance {

		NullRotation(Vec3 vec) {
			offset = new Vec3(vec);
		}

		@Override
		Mat3 calcRotation(float elapsedTime) {
			return new Mat3(1.0f);
		}
	}

	private class RotateX extends Instance {

		RotateX(Vec3 vec) {
			offset = new Vec3(vec);
		}

		@Override
		Mat3 calcRotation(float elapsedTime) {
			float angRad = computeAngleRad(elapsedTime, 3.0f);
			float cos = (float) Math.cos(angRad);
			float sin = (float) Math.sin(angRad);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(1, 1, cos); theMat.set(2, 1, -sin);
			theMat.set(1, 2, sin); theMat.set(2, 2, cos);
			return theMat;
		}
	}

	private class RotateY extends Instance {

		RotateY(Vec3 vec) {
			offset = new Vec3(vec);
		}

		@Override
		Mat3 calcRotation(float elapsedTime) {
			float angRad = computeAngleRad(elapsedTime, 2.0f);
			float cos = (float) Math.cos(angRad);
			float sin = (float) Math.sin(angRad);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(0, 0, cos); 	theMat.set(2, 0, sin);
			theMat.set(0, 2, -sin); theMat.set(2, 2, cos);
			return theMat;
		}
	}

	private class RotateZ extends Instance {

		RotateZ(Vec3 vec) {
			offset = new Vec3(vec);
		}

		@Override
		Mat3 calcRotation(float elapsedTime) {
			float angRad = computeAngleRad(elapsedTime, 2.0f);
			float cos = (float) Math.cos(angRad);
			float sin = (float) Math.sin(angRad);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(0, 0, cos); theMat.set(1, 0, -sin);
			theMat.set(0, 1, sin); theMat.set(1, 1, cos);
			return theMat;
		}
	}

	private class RotateAxis extends Instance {

		RotateAxis(Vec3 vec) {
			offset = new Vec3(vec);
		}

		@Override
		Mat3 calcRotation(float elapsedTime) {
			float angRad = computeAngleRad(elapsedTime, 2.0f);
			float cos = (float) Math.cos(angRad);
			float invCos = 1.0f - cos;
			float sin = (float) Math.sin(angRad);

			Vec3 axis = new Vec3(1.0f, 1.0f, 1.0f);
			axis = Glm.normalize(axis);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(0, 0, (axis.x * axis.x) + ((1 - axis.x * axis.x) * cos));
			theMat.set(1, 0, axis.x * axis.y * (invCos) - (axis.z * sin));
			theMat.set(2, 0, axis.x * axis.z * (invCos) + (axis.y * sin));

			theMat.set(0, 1, axis.x * axis.y * (invCos) + (axis.z * sin));
			theMat.set(1, 1, (axis.y * axis.y) + ((1 - axis.y * axis.y) * cos));
			theMat.set(2, 1, axis.y * axis.z * (invCos) - (axis.x * sin));

			theMat.set(0, 2, axis.x * axis.z * (invCos) - (axis.y * sin));
			theMat.set(1, 2, axis.y * axis.z * (invCos) + (axis.x * sin));
			theMat.set(2, 2, (axis.z * axis.z) + ((1 - axis.z * axis.z) * cos));
			return theMat;
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final float frustumScale = calcFrustumScale(45.0f);


	private float calcFrustumScale(float fovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fovRad = fovDeg * degToRad;

		return 1.0f / (float) (Math.tan(fovRad / 2.0f));
	}


	private float computeAngleRad(float elapsedTime, float loopDuration) {
		final float scale = 3.14159f * 2.0f / loopDuration;
		float currTimeThroughLoop = elapsedTime % loopDuration;

		return currTimeThroughLoop * scale;
	}
}