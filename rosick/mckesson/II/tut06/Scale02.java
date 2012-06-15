package rosick.mckesson.II.tut06;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.BufferUtils;

import rosick.LWJGLWindow;
import rosick.jglsdk.glm.Glm;
import rosick.jglsdk.glm.Mat4;
import rosick.jglsdk.glm.Vec3;
import rosick.jglsdk.glm.Vec4;
import rosick.mckesson.framework.Framework;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author integeruser
 */
public class Scale02 extends LWJGLWindow {
	
	public static void main(String[] args) {		
		Framework.CURRENT_TUTORIAL_DATAPATH = "/rosick/mckesson/II/tut06/data/";

		new Scale02().start();
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
	
	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);

			
	private void initializeProgram() {	
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER,	"PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	"ColorPassthrough.frag"));

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
			new NullScale				(new Vec3(0.0f, 0.0f, -45.0f)),
			new StaticUniformScale		(new Vec3(-10.0f, -10.0f, -45.0f)),
			new StaticNonUniformScale	(new Vec3(-10.0f, 10.0f, -45.0f)),
			new DynamicUniformScale		(new Vec3(10.0f, 10.0f, -45.0f)),
			new DynamicNonUniformScale	(new Vec3(10.0f, -10.0f, -45.0f))};
	
	
	private abstract class Instance {
		Vec3 offset;
		
		
		abstract Vec3 calcScale(float elapsedTime);
		
		Mat4 constructMatrix(float elapsedTime) {
			Vec3 theScale = calcScale(elapsedTime);

			Mat4 theMat = new Mat4(1.0f);
			theMat.set(0, 0,	theScale.x);
			theMat.set(1, 1,	theScale.y);
			theMat.set(2, 2,	theScale.z);
			theMat.setColumn(3, new Vec4(offset, 1.0f));
			
			return theMat;
		}
	}
	
	
	private class NullScale extends Instance {

		NullScale(Vec3 vec) {
			offset = new Vec3(vec);
		}
		
		@Override
		Vec3 calcScale(float elapsedTime) {
			return new Vec3(1.0f, 1.0f, 1.0f);
		}
	}
	
	private class StaticUniformScale extends Instance {

		StaticUniformScale(Vec3 vec) {
			offset = new Vec3(vec);
		}
		
		@Override
		Vec3 calcScale(float elapsedTime) {
			return new Vec3(4.0f, 4.0f, 4.0f);
		}
	}

	private class StaticNonUniformScale extends Instance {

		StaticNonUniformScale(Vec3 vec) {
			offset = new Vec3(vec);
		}
		
		@Override
		Vec3 calcScale(float elapsedTime) {
			return new Vec3(0.5f, 1.0f, 10.0f);
		}
	}
	
	private class DynamicUniformScale extends Instance {
		final float loopDuration = 3.0f;
		
		
		DynamicUniformScale(Vec3 vec) {
			offset = new Vec3(vec);
		}
		
		@Override
		Vec3 calcScale(float elapsedTime) {
			return new Vec3(Glm.mix(1.0f, 4.0f, calcLerpFactor(elapsedTime, loopDuration)));
		}
	}
	
	private class DynamicNonUniformScale extends Instance {
		final float xLoopDuration = 3.0f;
		final float zLoopDuration = 5.0f;

		
		DynamicNonUniformScale(Vec3 vec) {
			offset = new Vec3(vec);
		}
		
		@Override
		Vec3 calcScale(float elapsedTime) {
			return new Vec3(
					Glm.mix(1.0f, 0.5f, calcLerpFactor(elapsedTime, xLoopDuration)),
					1.0f,
					Glm.mix(1.0f, 10.0f, calcLerpFactor(elapsedTime, zLoopDuration)));
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
	
	
	private float calcLerpFactor(float elapsedTime, float loopDuration) {
		float value = (elapsedTime % loopDuration) / loopDuration;
		if (value > 0.5f) {
			value = 1.0f - value;
		}
		
		return value * 2.0f;
	}
}