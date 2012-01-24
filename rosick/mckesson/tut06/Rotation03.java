package rosick.mckesson.tut06;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import static rosick.glm.Vec.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.framework.IOUtils;
import rosick.glm.Glm;
import rosick.glm.Vec3;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author xire-
 */
public class Rotation03 extends GLWindow {
	
	public static void main(String[] args) {		
		new Rotation03().start(800, 800);
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/tut06/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	private FloatBuffer cameraToClipMatrixBuffer = IOUtils.allocFloats(new float[16]);
	private FloatBuffer modelToCameraMatrixBuffer = IOUtils.allocFloats(new float[16]); 
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
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		BASEPATH + "PosColorLocalTransform.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	BASEPATH + "ColorPassthrough.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = Framework.createProgram(shaderList);
		
	    
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float fzNear = 1.0f; float fzFar = 45.0f;

		cameraToClipMatrixBuffer.put(0, 	fFrustumScale);
		cameraToClipMatrixBuffer.put(5, 	fFrustumScale);
		cameraToClipMatrixBuffer.put(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrixBuffer.put(11, 	-1.0f);
		cameraToClipMatrixBuffer.put(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));
		
		modelToCameraMatrixBuffer.put(0,	1.0f);
		modelToCameraMatrixBuffer.put(5, 	1.0f);
		modelToCameraMatrixBuffer.put(10, 	1.0f);
		modelToCameraMatrixBuffer.put(15, 	1.0f);
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrixBuffer);
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
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glUseProgram(theProgram);
		glBindVertexArray(vao);
		
		nullRotation();
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrixBuffer);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		rotateX((float) (elapsedTime / 1000.0));
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrixBuffer);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		rotateY((float) (elapsedTime / 1000.0));
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrixBuffer);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		rotateZ((float) (elapsedTime / 1000.0));
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrixBuffer);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		rotateAxis((float) (elapsedTime / 1000.0));
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrixBuffer);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		glBindVertexArray(0);
		glUseProgram(0);
	}

	
	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrixBuffer.put(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrixBuffer.put(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrixBuffer);
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float fFrustumScale = calcFrustumScale(45.0f);

	
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
		modelToCameraMatrixBuffer.put(0, 1); 												// x
		modelToCameraMatrixBuffer.put(1, 0); 												// y
		modelToCameraMatrixBuffer.put(2, 0); 												// z
		
		// Y coloumn
		modelToCameraMatrixBuffer.put(4, 0); 												// x
		modelToCameraMatrixBuffer.put(5, 1);												// y
		modelToCameraMatrixBuffer.put(6, 0); 												// z
		
		// Z coloumn
		modelToCameraMatrixBuffer.put(8, 0); 												// x
		modelToCameraMatrixBuffer.put(9, 0); 												// y
		modelToCameraMatrixBuffer.put(10, 1); 											// z
		
		
		// Offset
		modelToCameraMatrixBuffer.put(12, 0); 											// x
		modelToCameraMatrixBuffer.put(13, 0); 											// y
		modelToCameraMatrixBuffer.put(14, -25.0f); 										// z
	}

	
	private void rotateX(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 3.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);
		
		// X coloumn
		modelToCameraMatrixBuffer.put(0, 1); 
		modelToCameraMatrixBuffer.put(1, 0); 
		modelToCameraMatrixBuffer.put(2, 0); 
		
		// Y coloumn
		modelToCameraMatrixBuffer.put(4, 0); 
		modelToCameraMatrixBuffer.put(5, fCos); 
		modelToCameraMatrixBuffer.put(6, fSin); 
		
		// Z coloumn
		modelToCameraMatrixBuffer.put(8, 0); 
		modelToCameraMatrixBuffer.put(9, -fSin); 
		modelToCameraMatrixBuffer.put(10, fCos); 
		
		
		// Offset
		modelToCameraMatrixBuffer.put(12, -5.0f); 
		modelToCameraMatrixBuffer.put(13, -5.0f); 
		modelToCameraMatrixBuffer.put(14, -25.0f); 
	}

	
	private void rotateY(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);
		
		// X column
		modelToCameraMatrixBuffer.put(0, fCos); 
		modelToCameraMatrixBuffer.put(1, 0); 
		modelToCameraMatrixBuffer.put(2, -fSin); 
		
		// Y column
		modelToCameraMatrixBuffer.put(4, 0);
		modelToCameraMatrixBuffer.put(5, 1); 
		modelToCameraMatrixBuffer.put(6, 0);
		
		// Z column
		modelToCameraMatrixBuffer.put(8, fSin); 
		modelToCameraMatrixBuffer.put(9, 0); 
		modelToCameraMatrixBuffer.put(10, fCos); 
		
		
		// Offset
		modelToCameraMatrixBuffer.put(12, -5.0f);
		modelToCameraMatrixBuffer.put(13, 5.0f); 
		modelToCameraMatrixBuffer.put(14, -25.0f); 
	}
	

	private void rotateZ(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fSin = (float) Math.sin(fAngRad);
		
		// X column
		modelToCameraMatrixBuffer.put(0, fCos); 																
		modelToCameraMatrixBuffer.put(1, fSin); 																
		modelToCameraMatrixBuffer.put(2, 0); 																	
		
		// Y column
		modelToCameraMatrixBuffer.put(4, -fSin); 																
		modelToCameraMatrixBuffer.put(5, fCos); 																
		modelToCameraMatrixBuffer.put(6, 0); 																	
		
		// Z column
		modelToCameraMatrixBuffer.put(8, 0);																	
		modelToCameraMatrixBuffer.put(9, 0);																	
		modelToCameraMatrixBuffer.put(10, 1); 																
		
		
		// Offset
		modelToCameraMatrixBuffer.put(12, 5.0f); 																
		modelToCameraMatrixBuffer.put(13, 5.0f); 																
		modelToCameraMatrixBuffer.put(14, -25.0f); 															
	}

	
	private void rotateAxis(float fElapsedTime) {
		float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
		float fCos = (float) Math.cos(fAngRad);
		float fInvCos = 1.0f - fCos;
		float fSin = (float) Math.sin(fAngRad);
		Vec3 axis = Glm.normalize(new Vec3(1, 1, 1));
		
		// X column
		modelToCameraMatrixBuffer.put(0, (axis.get(X) * axis.get(X)) + ((1 - axis.get(X) * axis.get(X)) * fCos)); 				
		modelToCameraMatrixBuffer.put(1, axis.get(X) * axis.get(Y) * (fInvCos) + (axis.get(Z) * fSin)); 						
		modelToCameraMatrixBuffer.put(2, axis.get(X) * axis.get(Z) * (fInvCos) - (axis.get(Y) * fSin)); 						
		
		// Y column
		modelToCameraMatrixBuffer.put(4, axis.get(X) * axis.get(Y) * (fInvCos) - (axis.get(Z) * fSin)); 						
		modelToCameraMatrixBuffer.put(5, (axis.get(Y) * axis.get(Y)) + ((1 - axis.get(Y) * axis.get(Y)) * fCos));					
		modelToCameraMatrixBuffer.put(6, axis.get(Y) * axis.get(Z) * (fInvCos) + (axis.get(X) * fSin)); 						
		
		// Z column
		modelToCameraMatrixBuffer.put(8, axis.get(X) * axis.get(Z) * (fInvCos) + (axis.get(Y) * fSin)); 						
		modelToCameraMatrixBuffer.put(9, axis.get(Y) * axis.get(Z) * (fInvCos) - (axis.get(X) * fSin)); 						
		modelToCameraMatrixBuffer.put(10, (axis.get(Z) * axis.get(Z)) + ((1 - axis.get(Z) * axis.get(Z)) * fCos)); 				
		
		
		// Offset
		modelToCameraMatrixBuffer.put(12, 5.0f); 																
		modelToCameraMatrixBuffer.put(13, -5.0f); 															
		modelToCameraMatrixBuffer.put(14, -25.0f); 															
	}
}