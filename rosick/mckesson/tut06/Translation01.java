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
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author xire-
 */
public class Translation01 extends GLWindow {
	
	public static void main(String[] args) {		
		new Translation01().start(800, 800);
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
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		BASEPATH + "posColorLocalTransform.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	BASEPATH + "colorPassthrough.frag");
        
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
		
		modelToCameraMatrixBuffer.put(0, 	1.0f);
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
		
		stationaryOffset();
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrixBuffer);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		ovalOffset((float) elapsedTime);
		glUniformMatrix4(modelToCameraMatrixUnif, false, modelToCameraMatrixBuffer);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		
		bottomCircleOffset((float) elapsedTime);
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
	
	
	private void stationaryOffset() {
		modelToCameraMatrixBuffer.put(12, 0);  															// x
		modelToCameraMatrixBuffer.put(13, 0); 															// y
		modelToCameraMatrixBuffer.put(14, -20); 														// z
	}

	
	private void ovalOffset(float fElapsedTime) {
		final float fLoopDuration = 3.0f;
		final float fScale = 3.14159f * 2.0f / fLoopDuration;
		float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;
		
		modelToCameraMatrixBuffer.put(12, (float) (Math.cos(fCurrTimeThroughLoop * fScale) * 4.f)); 			
		modelToCameraMatrixBuffer.put(13, (float) (Math.sin(fCurrTimeThroughLoop * fScale) * 6.f)); 			
		modelToCameraMatrixBuffer.put(14, -20); 																
	}

	
	private void bottomCircleOffset(float fElapsedTime) {
		final float fLoopDuration = 12.0f;
		final float fScale = 3.14159f * 2.0f / fLoopDuration;
		float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;

		modelToCameraMatrixBuffer.put(12, (float) (Math.cos(fCurrTimeThroughLoop * fScale) * 5.f)); 			
		modelToCameraMatrixBuffer.put(13, -3.5f); 															
		modelToCameraMatrixBuffer.put(14, (float) (Math.sin(fCurrTimeThroughLoop * fScale) * 5.f -20.f)); 	
	}
}