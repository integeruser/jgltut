package rosick.mckesson.tut05;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glDrawElementsBaseVertex;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import rosick.common.GLWindow;
import rosick.common.IOUtils;
import rosick.common.ShaderUtils;


/**
 * Jason L. McKesson
 * II. Positioning
 * 5. Objects in Depth
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2005.html
 */
public class DepthClamping05 extends GLWindow {
	
	public static void main(String[] args) {		
		new DepthClamping05().start(600, 600);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	final float RIGHT_EXTENT 	=	0.8f;
	final float LEFT_EXTENT  	=  -RIGHT_EXTENT;
	final float TOP_EXTENT   	=	0.20f;
	final float MIDDLE_EXTENT	= 	0.0f;
	final float BOTTOM_EXTENT	=  -TOP_EXTENT;
	final float FRONT_EXTENT	=  -1.25f;
	final float REAR_EXTENT		=  -1.75f;
		
	final float vertexData[] = {												
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
	
	final short indexData[] = {	
		0, 2, 1,
		3, 2, 0,

		4, 5, 6,
		6, 7, 4,

		8, 9, 10,
		11, 13, 12,

		14, 16, 15,
		17, 16, 14,
	};
	
	final int numberOfVertices = 36;
	final float fFrustumScale = 1.0f;
	
	int theProgram;
	int offsetUniform, perspectiveMatrixUnif;
	FloatBuffer perspectiveMatrixBuffer;
	int vertexBufferObject, indexBufferObject;
	int vao;
	
	

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
		glDepthFunc(GL_LESS);
		glDepthRange(0.0f, 1.0f);
	}
	
	private void initializeProgram() {			
		int vertexShader =		ShaderUtils.loadShaderFromFile(GL_VERTEX_SHADER, 	"/rosick/mckesson/shaders/tut05/standard.vert");
		int fragmentShader = 	ShaderUtils.loadShaderFromFile(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/shaders/tut05/standard.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
		
	    
		offsetUniform = glGetUniformLocation(theProgram, "offset");
		
		perspectiveMatrixUnif = glGetUniformLocation(theProgram, "perspectiveMatrix");

		float fzNear = 1.0f; float fzFar = 3.0f;
		
		float perspectiveMatrix[] = new float[16];
		perspectiveMatrix[0] = fFrustumScale;
		perspectiveMatrix[5] = fFrustumScale;
		perspectiveMatrix[10] = (fzFar + fzNear) / (fzNear - fzFar);
		perspectiveMatrix[14] = (2 * fzFar * fzNear) / (fzNear - fzFar);
		perspectiveMatrix[11] = -1.0f;
		
		perspectiveMatrixBuffer = IOUtils.allocFloats(perspectiveMatrix);
		
		glUseProgram(theProgram);
		glUniformMatrix4(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
		glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer vertexBuffer = IOUtils.allocFloats(vertexData);
        
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		
		ShortBuffer indexBuffer = IOUtils.allocShorts(indexData);
        
        indexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
		
	@Override
	protected void update(float fElapsedTime) {
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			glEnable(GL_DEPTH_CLAMP);
		} else {
			glDisable(GL_DEPTH_CLAMP);
		}
	}
	
	
	@Override
	protected void render(float fElapsedTime) {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glUseProgram(theProgram);
		glBindVertexArray(vao);
		
		glUniform3f(offsetUniform, 0.0f, 0.0f, 0.5f);
		glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);

		glUniform3f(offsetUniform, 0.0f, 0.0f, -1.00f);
		glDrawElementsBaseVertex(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0, numberOfVertices / 2);

		glBindVertexArray(0);
		glUseProgram(0);
	}
	
	
	@Override
	protected void reshape(int width, int height) {
		perspectiveMatrixBuffer.put(0, fFrustumScale / (width / (float) height));
		perspectiveMatrixBuffer.put(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
}