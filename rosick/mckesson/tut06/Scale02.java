package rosick.mckesson.tut06;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.framework.IOUtils;


/**
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author xire-
 */
public class Scale02 extends GLWindow {
	
	public static void main(String[] args) {		
		new Scale02().start(600, 600);
	}
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
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
	private final float fFrustumScale = calcFrustumScale(45.0f);
	
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	private FloatBuffer cameraToClipMatrix, modelToCameraMatrix; 
	private int vertexBufferObject, indexBufferObject;
	private int vao;
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		int colorDataOffset = 4 * 3 * numberOfVertices;
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
	
	private void initializeProgram() {	
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		"/rosick/mckesson/tut06/data/posColorLocalTransform.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/tut06/data/colorPassthrough.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = Framework.createProgram(shaderList);
		
	    
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float fzNear = 1.0f; float fzFar = 45.0f;
		cameraToClipMatrix = IOUtils.allocFloats(new float[16]);
		cameraToClipMatrix.put(0, fFrustumScale);
		cameraToClipMatrix.put(5, fFrustumScale);
		cameraToClipMatrix.put(10, (fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.put(11, -1.0f);
		cameraToClipMatrix.put(14, (2 * fzFar * fzNear) / (fzNear - fzFar));
		
		modelToCameraMatrix = IOUtils.allocFloats(new float[16]); 
		modelToCameraMatrix.put(0, 1);
		modelToCameraMatrix.put(5, 1);
		modelToCameraMatrix.put(10, 1);
		modelToCameraMatrix.put(15, 1);
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix);
		glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer vertexDataBuffer = IOUtils.allocFloats(vertexData);
        
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		
		ShortBuffer indexDataBuffer = IOUtils.allocShorts(indexData);
        
        indexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	
	@Override
	protected void render() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glUseProgram(theProgram);
		glBindVertexArray(vao);
		
		nullScale();
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		staticUniformScale();
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		staticNonUniformScale();
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		dynamicUniformScale((float) elapsedTimeSeconds);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		dynamicNonUniformScale((float) elapsedTimeSeconds);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		glBindVertexArray(0);
		glUseProgram(0);
	}

	
	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrix.put(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.put(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix);
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
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
	
	
	private void nullScale() {
		// Scale
		modelToCameraMatrix.put(0, 1); 												// x
		modelToCameraMatrix.put(5, 1); 												// y
		modelToCameraMatrix.put(10, 1); 											// z
		
		// Offset
		modelToCameraMatrix.put(12, 0); 											// x
		modelToCameraMatrix.put(13, 0); 											// y
		modelToCameraMatrix.put(14, -45.0f); 										// z
	}

	
	private void staticUniformScale() {
		// Scale
		modelToCameraMatrix.put(0, 4); 												
		modelToCameraMatrix.put(5, 4); 												
		modelToCameraMatrix.put(10, 4); 											
		
		// Offset
		modelToCameraMatrix.put(12, -10.0f); 									
		modelToCameraMatrix.put(13, -10.0f); 										
		modelToCameraMatrix.put(14, -45.0f); 										
	}

	
	private void staticNonUniformScale() {
		// Scale
		modelToCameraMatrix.put(0, 0.5f);											
		modelToCameraMatrix.put(5, 1); 												
		modelToCameraMatrix.put(10, 10); 										
		
		// Offset
		modelToCameraMatrix.put(12, -10.0f); 										
		modelToCameraMatrix.put(13, 10.0f); 										
		modelToCameraMatrix.put(14, -45.0f); 										
	}

	
	private void dynamicUniformScale(float fElapsedTime) {
		final float fLoopDuration = 3.0f;
		float val = 1.0f + 3.0f * calcLerpFactor(fElapsedTime, fLoopDuration);
		
		// Scale
		modelToCameraMatrix.put(0, val);											
		modelToCameraMatrix.put(5, val); 											
		modelToCameraMatrix.put(10, val); 											
		
		// Offset
		modelToCameraMatrix.put(12, 10.0f);											
		modelToCameraMatrix.put(13, 10.0f); 										
		modelToCameraMatrix.put(14, -45.0f); 										
	}

	
	private void dynamicNonUniformScale(float fElapsedTime) {
		final float fXLoopDuration = 3.0f;
		final float fZLoopDuration = 5.0f;
		
		float valX = 1.0f - 0.5f * calcLerpFactor(fElapsedTime, fXLoopDuration);
		float valZ = 1.0f + 9.0f * calcLerpFactor(fElapsedTime, fZLoopDuration);
		
		// Scale
		modelToCameraMatrix.put(0, valX); 											
		modelToCameraMatrix.put(5, 1); 												
		modelToCameraMatrix.put(10, valZ); 											
		
		// Offset
		modelToCameraMatrix.put(12, 10.0f); 										
		modelToCameraMatrix.put(13, -10.0f); 										
		modelToCameraMatrix.put(14, -45.0f); 										
	}
}