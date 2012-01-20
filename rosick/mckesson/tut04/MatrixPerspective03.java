package rosick.mckesson.tut04;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.framework.IOUtils;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 4. Objects at Rest 
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2004.html
 * @author integeruser
 */
public class MatrixPerspective03 extends GLWindow {
	
	public static void main(String[] args) {		
		new MatrixPerspective03().start(600, 600);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float vertexData[] = {
		 0.25f,  0.25f, -1.25f, 1.0f,
		 0.25f, -0.25f, -1.25f, 1.0f,
		-0.25f,  0.25f, -1.25f, 1.0f,

		 0.25f, -0.25f, -1.25f, 1.0f,
		-0.25f, -0.25f, -1.25f, 1.0f,
		-0.25f,  0.25f, -1.25f, 1.0f,

		 0.25f,  0.25f, -2.75f, 1.0f,
		-0.25f,  0.25f, -2.75f, 1.0f,
		 0.25f, -0.25f, -2.75f, 1.0f,

		 0.25f, -0.25f, -2.75f, 1.0f,
		-0.25f,  0.25f, -2.75f, 1.0f,
		-0.25f, -0.25f, -2.75f, 1.0f,

		-0.25f,  0.25f, -1.25f, 1.0f,
		-0.25f, -0.25f, -1.25f, 1.0f,
		-0.25f, -0.25f, -2.75f, 1.0f,

		-0.25f,  0.25f, -1.25f, 1.0f,
		-0.25f, -0.25f, -2.75f, 1.0f,
		-0.25f,  0.25f, -2.75f, 1.0f,

		 0.25f,  0.25f, -1.25f, 1.0f,
		 0.25f, -0.25f, -2.75f, 1.0f,
		 0.25f, -0.25f, -1.25f, 1.0f,

		 0.25f,  0.25f, -1.25f, 1.0f,
		 0.25f,  0.25f, -2.75f, 1.0f,
		 0.25f, -0.25f, -2.75f, 1.0f,

		 0.25f,  0.25f, -2.75f, 1.0f,
		 0.25f,  0.25f, -1.25f, 1.0f,
		-0.25f,  0.25f, -1.25f, 1.0f,

		 0.25f,  0.25f, -2.75f, 1.0f,
		-0.25f,  0.25f, -1.25f, 1.0f,
		-0.25f,  0.25f, -2.75f, 1.0f,

		 0.25f, -0.25f, -2.75f, 1.0f,
		-0.25f, -0.25f, -1.25f, 1.0f,
		 0.25f, -0.25f, -1.25f, 1.0f,

		 0.25f, -0.25f, -2.75f, 1.0f,
		-0.25f, -0.25f, -2.75f, 1.0f,
		-0.25f, -0.25f, -1.25f, 1.0f,




		0.0f, 0.0f, 1.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,

		0.0f, 0.0f, 1.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,

		0.8f, 0.8f, 0.8f, 1.0f,
		0.8f, 0.8f, 0.8f, 1.0f,
		0.8f, 0.8f, 0.8f, 1.0f,

		0.8f, 0.8f, 0.8f, 1.0f,
		0.8f, 0.8f, 0.8f, 1.0f,
		0.8f, 0.8f, 0.8f, 1.0f,

		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,

		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,

		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,

		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,

		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,

		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,

		0.0f, 1.0f, 1.0f, 1.0f,
		0.0f, 1.0f, 1.0f, 1.0f,
		0.0f, 1.0f, 1.0f, 1.0f,

		0.0f, 1.0f, 1.0f, 1.0f,
		0.0f, 1.0f, 1.0f, 1.0f,
		0.0f, 1.0f, 1.0f, 1.0f,
	};
	
	private int theProgram;
	private int offsetUniform;
	private int vertexBufferObject;
	private int vao;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);
	}
	
	private void initializeProgram() {			
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		"/rosick/mckesson/tut04/data/matrixPerspective.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/tut04/data/standardColors.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = Framework.createProgram(shaderList);
		
	    
		offsetUniform = glGetUniformLocation(theProgram, "offset");
		
		int perspectiveMatrixUnif = glGetUniformLocation(theProgram, "perspectiveMatrix");
		
		float fFrustumScale = 1.0f; float fzNear = 0.5f; float fzFar = 3.0f;
		
		float theMatrix[] = new float[16];
		theMatrix[0] = fFrustumScale;
		theMatrix[5] = fFrustumScale;
		theMatrix[10] = (fzFar + fzNear) / (fzNear - fzFar);
		theMatrix[14] = (2 * fzFar * fzNear) / (fzNear - fzFar);
		theMatrix[11] = -1.0f;
		
		FloatBuffer matrixBuffer = IOUtils.allocFloats(theMatrix);

		glUseProgram(theProgram);
		glUniformMatrix4(perspectiveMatrixUnif, false, matrixBuffer);
		glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer vertexBuffer = IOUtils.allocFloats(vertexData);
        
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	

	@Override
	protected void display() {		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		
		glUseProgram(theProgram);
		
		glUniform2f(offsetUniform, 0.5f, 0.5f);

		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, vertexData.length * 2);

		glDrawArrays(GL_TRIANGLES, 0, 36);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glUseProgram(0);
	}
}