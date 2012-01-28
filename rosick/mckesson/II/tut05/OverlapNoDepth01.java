package rosick.mckesson.II.tut05;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.GLWindow;
import rosick.framework.Framework;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 5. Objects in Depth
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2005.html
 * @author integeruser
 */
public class OverlapNoDepth01 extends GLWindow {
	
	public static void main(String[] args) {		
		new OverlapNoDepth01().start();
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/II/tut05/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int offsetUniform, perspectiveMatrixUnif;
	private int vertexBufferObject, indexBufferObject;
	private int vaoObject1, vaoObject2;
	
	private final float RIGHT_EXTENT 	=	0.8f;
	private final float LEFT_EXTENT  	=  -RIGHT_EXTENT;
	private final float TOP_EXTENT   	=	0.20f;
	private final float MIDDLE_EXTENT	= 	0.0f;
	private final float BOTTOM_EXTENT	=  -TOP_EXTENT;
	private final float FRONT_EXTENT	=  -1.25f;
	private final float REAR_EXTENT		=  -1.75f;
	
	private final float vertexData[] = {												
		// Object 1 positions
		LEFT_EXTENT,	TOP_EXTENT,		REAR_EXTENT,
		LEFT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
		RIGHT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
		RIGHT_EXTENT,	TOP_EXTENT,		REAR_EXTENT,

		LEFT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,
		LEFT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
		RIGHT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
		RIGHT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

		LEFT_EXTENT,	TOP_EXTENT,		REAR_EXTENT,
		LEFT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
		LEFT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

		RIGHT_EXTENT,	TOP_EXTENT,		REAR_EXTENT,
		RIGHT_EXTENT,	MIDDLE_EXTENT,	FRONT_EXTENT,
		RIGHT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

		LEFT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,
		LEFT_EXTENT,	TOP_EXTENT,		REAR_EXTENT,
		RIGHT_EXTENT,	TOP_EXTENT,		REAR_EXTENT,
		RIGHT_EXTENT,	BOTTOM_EXTENT,	REAR_EXTENT,

		// Object 2 positions
		TOP_EXTENT,		RIGHT_EXTENT,	REAR_EXTENT,
		MIDDLE_EXTENT,	RIGHT_EXTENT,	FRONT_EXTENT,
		MIDDLE_EXTENT,	LEFT_EXTENT,	FRONT_EXTENT,
		TOP_EXTENT,		LEFT_EXTENT,	REAR_EXTENT,

		BOTTOM_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
		MIDDLE_EXTENT,	RIGHT_EXTENT,	FRONT_EXTENT,
		MIDDLE_EXTENT,	LEFT_EXTENT,	FRONT_EXTENT,
		BOTTOM_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,

		TOP_EXTENT,		RIGHT_EXTENT,	REAR_EXTENT,
		MIDDLE_EXTENT,	RIGHT_EXTENT,	FRONT_EXTENT,
		BOTTOM_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
						
		TOP_EXTENT,		LEFT_EXTENT,	REAR_EXTENT,
		MIDDLE_EXTENT,	LEFT_EXTENT,	FRONT_EXTENT,
		BOTTOM_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,
						
		BOTTOM_EXTENT,	RIGHT_EXTENT,	REAR_EXTENT,
		TOP_EXTENT,		RIGHT_EXTENT,	REAR_EXTENT,
		TOP_EXTENT,		LEFT_EXTENT,	REAR_EXTENT,
		BOTTOM_EXTENT,	LEFT_EXTENT,	REAR_EXTENT,

		// Object 1 colors
		0.75f, 0.75f, 1.0f, 1.0f, 													// GREEN
		0.75f, 0.75f, 1.0f, 1.0f,
		0.75f, 0.75f, 1.0f, 1.0f,
		0.75f, 0.75f, 1.0f, 1.0f,

		0.0f, 0.5f, 0.0f, 1.0f, 													// BLUE
		0.0f, 0.5f, 0.0f, 1.0f,
		0.0f, 0.5f, 0.0f, 1.0f,
		0.0f, 0.5f, 0.0f, 1.0f,

		1.0f, 0.0f, 0.0f, 1.0f, 													// RED
		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,

		0.8f, 0.8f, 0.8f, 1.0f, 													// GREY
		0.8f, 0.8f, 0.8f, 1.0f,
		0.8f, 0.8f, 0.8f, 1.0f,

		0.5f, 0.5f, 0.0f, 1.0f, 													// BROWN
		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,

		// Object 2 colors
		1.0f, 0.0f, 0.0f, 1.0f,														// RED
		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,

		0.5f, 0.5f, 0.0f, 1.0f, 													// BROWN
		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,

		0.0f, 0.5f, 0.0f, 1.0f,														// BLUE
		0.0f, 0.5f, 0.0f, 1.0f,
		0.0f, 0.5f, 0.0f, 1.0f,

		0.75f, 0.75f, 1.0f, 1.0f, 													// GREEN
		0.75f, 0.75f, 1.0f, 1.0f,
		0.75f, 0.75f, 1.0f, 1.0f,

		0.8f, 0.8f, 0.8f, 1.0f, 													// GREY
		0.8f, 0.8f, 0.8f, 1.0f,
		0.8f, 0.8f, 0.8f, 1.0f,
		0.8f, 0.8f, 0.8f, 1.0f,
	};
	
	private final short indexData[] = {	
		0, 2, 1,
		3, 2, 0,

		4, 5, 6,
		6, 7, 4,

		8, 9, 10,
		11, 13, 12,

		14, 16, 15,
		17, 16, 14,
	};
	
	private final int numberOfVertices = 36;
	private final float fFrustumScale = 1.0f;

	private float perspectiveMatrix[];

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	private void initializeProgram() {			
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		BASEPATH + "Standard.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	BASEPATH + "Standard.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = Framework.createProgram(shaderList);
		
		offsetUniform = glGetUniformLocation(theProgram, "offset");
		
		perspectiveMatrixUnif = glGetUniformLocation(theProgram, "perspectiveMatrix");

		float fzNear = 1.0f; float fzFar = 3.0f;
		
		perspectiveMatrix = new float[16];
		
		perspectiveMatrix[0] 	= fFrustumScale;
		perspectiveMatrix[5] 	= fFrustumScale;
		perspectiveMatrix[10] 	= (fzFar + fzNear) / (fzNear - fzFar);
		perspectiveMatrix[11] 	= -1.0f;
		perspectiveMatrix[14] 	= (2 * fzFar * fzNear) / (fzNear - fzFar);

		FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer(perspectiveMatrix.length);
		perspectiveMatrixBuffer.put(perspectiveMatrix);
		perspectiveMatrixBuffer.flip();
				
		glUseProgram(theProgram);
		glUniformMatrix4(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
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
	
	private void initializeVertexArrayObjects() {
		vaoObject1 = glGenVertexArrays();
		glBindVertexArray(vaoObject1);

		int colorDataOffset = (Float.SIZE / 8) * 3 * numberOfVertices;
		
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

		glBindVertexArray(0);
		
		vaoObject2 = glGenVertexArrays();
		glBindVertexArray(vaoObject2);

		int posDataOffset = 4 * 3 * (numberOfVertices / 2);
		colorDataOffset += 4 * 4 * (numberOfVertices / 2);

		// Use the same buffer object previously bound to GL_ARRAY_BUFFER.
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, posDataOffset);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

		glBindVertexArray(0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 
		initializeVertexArrayObjects();
	
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
	}
		
	
	@Override
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);

		glBindVertexArray(vaoObject1);
		glUniform3f(offsetUniform, 0.0f, 0.0f, 0.0f);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		glBindVertexArray(vaoObject2);
		glUniform3f(offsetUniform, 0.0f, 0.0f, -1.0f);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		glBindVertexArray(0);
		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {
		perspectiveMatrix[0] = fFrustumScale / (width / (float) height);
		perspectiveMatrix[5] = fFrustumScale;

		FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer(perspectiveMatrix.length);
		perspectiveMatrixBuffer.put(perspectiveMatrix);
		perspectiveMatrixBuffer.flip();

		glUseProgram(theProgram);
		glUniformMatrix4(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
}