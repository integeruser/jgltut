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
import rosick.glm.Vec3;


/**
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author xire-
 */
public class Rotation03 extends GLWindow {
	
	public static void main(String[] args) {		
		new Rotation03().start(600, 600);
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
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		"/rosick/mckesson/data/tut06/posColorLocalTransform.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/data/tut06/colorPassthrough.frag");
        
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
		
		nullRotation();
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		rotateX((float) elapsedTimeSeconds);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		rotateY((float) elapsedTimeSeconds);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		rotateZ((float) elapsedTimeSeconds);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrix);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		rotateAxis((float) elapsedTimeSeconds);
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
	
	
	private float computeAngleRad(float fElapsedTime, float fLoopDuration) {
		final float fScale = 3.14159f * 2.0f / fLoopDuration;
		float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;
		
		return fCurrTimeThroughLoop * fScale;
	}
	
	
	private void nullRotation() {
		// X coloumn
		modelToCameraMatrix.put(0, 1); 												// x
		modelToCameraMatrix.put(1, 0); 												// y
		modelToCameraMatrix.put(2, 0); 												// z
		
		// Y coloumn
		modelToCameraMatrix.put(4, 0); 												// x
		modelToCameraMatrix.put(5, 1);												// y
		modelToCameraMatrix.put(6, 0); 												// z
		
		// Z coloumn
		modelToCameraMatrix.put(8, 0); 												// x
		modelToCameraMatrix.put(9, 0); 												// y
		modelToCameraMatrix.put(10, 1); 											// z
		
		
		// Offset
		modelToCameraMatrix.put(12, 0); 											// x
		modelToCameraMatrix.put(13, 0); 											// y
		modelToCameraMatrix.put(14, -25.0f); 										// z
	}

	
	private void rotateX(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 3.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);
		
		// X coloumn
		modelToCameraMatrix.put(0, 1); 
		modelToCameraMatrix.put(1, 0); 
		modelToCameraMatrix.put(2, 0); 
		
		// Y coloumn
		modelToCameraMatrix.put(4, 0); 
		modelToCameraMatrix.put(5, fCos); 
		modelToCameraMatrix.put(6, fSin); 
		
		// Z coloumn
		modelToCameraMatrix.put(8, 0); 
		modelToCameraMatrix.put(9, -fSin); 
		modelToCameraMatrix.put(10, fCos); 
		
		
		// Offset
		modelToCameraMatrix.put(12, -5.0f); 
		modelToCameraMatrix.put(13, -5.0f); 
		modelToCameraMatrix.put(14, -25.0f); 
	}

	
	private void rotateY(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);
		
		// X column
		modelToCameraMatrix.put(0, fCos); 
		modelToCameraMatrix.put(1, 0); 
		modelToCameraMatrix.put(2, -fSin); 
		
		// Y column
		modelToCameraMatrix.put(4, 0);
		modelToCameraMatrix.put(5, 1); 
		modelToCameraMatrix.put(6, 0);
		
		// Z column
		modelToCameraMatrix.put(8, fSin); 
		modelToCameraMatrix.put(9, 0); 
		modelToCameraMatrix.put(10, fCos); 
		
		
		// Offset
		modelToCameraMatrix.put(12, -5.0f);
		modelToCameraMatrix.put(13, 5.0f); 
		modelToCameraMatrix.put(14, -25.0f); 
	}
	

	private void rotateZ(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);
		
		// X column
		modelToCameraMatrix.put(0, fCos); 																
		modelToCameraMatrix.put(1, fSin); 																
		modelToCameraMatrix.put(2, 0); 																	
		
		// Y column
		modelToCameraMatrix.put(4, -fSin); 																
		modelToCameraMatrix.put(5, fCos); 																
		modelToCameraMatrix.put(6, 0); 																	
		
		// Z column
		modelToCameraMatrix.put(8, 0);																	
		modelToCameraMatrix.put(9, 0);																	
		modelToCameraMatrix.put(10, 1); 																
		
		
		// Offset
		modelToCameraMatrix.put(12, 5.0f); 																
		modelToCameraMatrix.put(13, 5.0f); 																
		modelToCameraMatrix.put(14, -25.0f); 															
	}

	
	private void rotateAxis(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fInvCos = 1.0f - fCos;
		float fSin = (float) Math.sin(fAngRad);
		Vec3 axis = new Vec3(1, 1, 1).normalize();
		
		// X column
		modelToCameraMatrix.put(0, (axis.x * axis.x) + ((1 - axis.x * axis.x) * fCos)); 				
		modelToCameraMatrix.put(1, axis.x * axis.y * (fInvCos) + (axis.z * fSin)); 						
		modelToCameraMatrix.put(2, axis.x * axis.z * (fInvCos) - (axis.y * fSin)); 						
		
		// Y column
		modelToCameraMatrix.put(4, axis.x * axis.y * (fInvCos) - (axis.z * fSin)); 						
		modelToCameraMatrix.put(5, (axis.y * axis.y) + ((1 - axis.y * axis.y) * fCos));					
		modelToCameraMatrix.put(6, axis.y * axis.z * (fInvCos) + (axis.x * fSin)); 						
		
		// Z column
		modelToCameraMatrix.put(8, axis.x * axis.z * (fInvCos) + (axis.y * fSin)); 						
		modelToCameraMatrix.put(9, axis.y * axis.z * (fInvCos) - (axis.x * fSin)); 						
		modelToCameraMatrix.put(10, (axis.z * axis.z) + ((1 - axis.z * axis.z) * fCos)); 				
		
		
		// Offset
		modelToCameraMatrix.put(12, 5.0f); 																
		modelToCameraMatrix.put(13, -5.0f); 															
		modelToCameraMatrix.put(14, -25.0f); 															
	}
}