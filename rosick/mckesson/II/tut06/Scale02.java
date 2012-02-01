package rosick.mckesson.II.tut06;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import static rosick.glm.Vec.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Vec3;
import rosick.glm.Vec4;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author integeruser
 */
public class Scale02 extends GLWindow {
	
	public static void main(String[] args) {		
		new Scale02().start();
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/II/tut06/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	private int vertexBufferObject, indexBufferObject;
	private int vao;
	
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
		0.5f, 0.5f, 0.0f, 1.0f,
	};
	
	private final short indexData[] = {
		0, 1, 2,
		1, 0, 3,
		2, 3, 0,
		3, 2, 1,

		5, 4, 6,
		4, 5, 7,
		7, 6, 4,
		6, 7, 5,
	};
	
	private final int numberOfVertices = 8;
	
	private Mat4 cameraToClipMatrix = new Mat4();
	
	private FloatBuffer tempSharedFloatBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
		
	private void initializeProgram() {	
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER,	BASEPATH + "PosColorLocalTransform.vert"));
		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER,	BASEPATH + "ColorPassthrough.frag"));

		theProgram = Framework.createProgram(shaderList);
			    
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float fzNear = 1.0f; float fzFar = 45.0f;

		cameraToClipMatrix.set(0, 	fFrustumScale);
		cameraToClipMatrix.set(5, 	fFrustumScale);
		cameraToClipMatrix.set(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.set(11, 	-1.0f);
		cameraToClipMatrix.set(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillBuffer(tempSharedFloatBuffer16));
		glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer tempVertexBuffer = BufferUtils.createFloatBuffer(vertexData.length);
		tempVertexBuffer.put(vertexData);
		tempVertexBuffer.flip();
		
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, tempVertexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		
		ShortBuffer tempIndexBuffer = BufferUtils.createShortBuffer(indexData.length);
		tempIndexBuffer.put(indexData);
		tempIndexBuffer.flip();
		
        indexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, tempIndexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		int colorDataOffset = (Float.SIZE / 8) * 3 * numberOfVertices;
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
		
		float fElapsedTime = (float) (getElapsedTime() / 1000.0);
		for (Instance currInst : g_instanceList) {
			final Mat4 transformMatrix = currInst.constructMatrix(fElapsedTime);
			
			glUniformMatrix4(modelToCameraMatrixUnif, false, transformMatrix.fillBuffer(tempSharedFloatBuffer16));
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		}

		glBindVertexArray(0);
		glUseProgram(0);
	}

	
	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrix.set(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.set(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillBuffer(tempSharedFloatBuffer16));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Instance g_instanceList[] = {
			new NullScale(new Vec3(0.0f, 0.0f, -45.0f)),
			new StaticUniformScale(new Vec3(-10.0f, -10.0f, -45.0f)),
			new StaticNonUniformScale(new Vec3(-10.0f, 10.0f, -45.0f)),
			new DynamicUniformScale(new Vec3(10.0f, 10.0f, -45.0f)),
			new DynamicNonUniformScale(new Vec3(10.0f, -10.0f, -45.0f))
	};
	
	
	private abstract class Instance {
		
		Vec3 offset;
		
		
		abstract Vec3 calcScale(float fElapsedTime);
		
		Mat4 constructMatrix(float fElapsedTime) {
			Vec3 theScale = calcScale(fElapsedTime);
			Mat4 theMat = new Mat4(1.0f);
			
			theMat.set(0, 	theScale.get(X));
			theMat.set(5, 	theScale.get(Y));
			theMat.set(10, 	theScale.get(Z));
			theMat.setColumn(3, new Vec4(offset, 1.0f));
			
			return theMat;
		}
	}
	
	
	private class NullScale extends Instance {

		public NullScale(Vec3 vec) {
			offset = vec;
		}
		

		@Override
		Vec3 calcScale(float fElapsedTime) {
			return new Vec3(1.0f, 1.0f, 1.0f);
		}
	}
	
	private class StaticUniformScale extends Instance {

		public StaticUniformScale(Vec3 vec) {
			offset = vec;
		}
		

		@Override
		Vec3 calcScale(float fElapsedTime) {
			return new Vec3(4.0f, 4.0f, 4.0f);
		}
	}

	private class StaticNonUniformScale extends Instance {

		public StaticNonUniformScale(Vec3 vec) {
			offset = vec;
		}
		

		@Override
		Vec3 calcScale(float fElapsedTime) {
			return new Vec3(0.5f, 1.0f, 10.0f);
		}
	}
	
	private class DynamicUniformScale extends Instance {

		final float fLoopDuration = 3.0f;
		
		
		public DynamicUniformScale(Vec3 vec) {
			offset = vec;
		}
		

		@Override
		Vec3 calcScale(float fElapsedTime) {
			return new Vec3(Glm.mix(1.0f, 4.0f, calcLerpFactor(fElapsedTime, fLoopDuration)));
		}
	}
	
	private class DynamicNonUniformScale extends Instance {

		final float fXLoopDuration = 3.0f;
		final float fZLoopDuration = 5.0f;

		
		public DynamicNonUniformScale(Vec3 vec) {
			offset = vec;
		}
		

		@Override
		Vec3 calcScale(float fElapsedTime) {
			return new Vec3(Glm.mix(1.0f, 0.5f, calcLerpFactor(fElapsedTime, fXLoopDuration)),
					1.0f,
					Glm.mix(1.0f, 10.0f, calcLerpFactor(fElapsedTime, fZLoopDuration)));
		}
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float fFrustumScale = calcFrustumScale(45.0f);

	
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fFovRad = fFovDeg * degToRad;
		
		return 1.0f / (float) (Math.tan(fFovRad / 2.0f));
	}
	
	
	private float calcLerpFactor(float fElapsedTime, float fLoopDuration) {
		float fValue = (fElapsedTime % fLoopDuration) / fLoopDuration;
		if (fValue > 0.5f)
			fValue = 1.0f - fValue;

		return fValue * 2.0f;
	}
}