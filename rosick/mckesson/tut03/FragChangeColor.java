package rosick.mckesson.tut03;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import rosick.common.GLWindow;
import rosick.common.IOUtils;
import rosick.common.ShaderUtils;


/**
 * Part II. Positioning
 * Chapter 3. OpenGL's Moving Triangle
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2003.html
 */
public class FragChangeColor extends GLWindow {
	
	public static void main(String[] args) {		
		new	FragChangeColor().start();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	float vertexPositions[] = {
		 0.25f,  0.25f, 0.0f, 1.0f,
		 0.25f, -0.25f, 0.0f, 1.0f,
		-0.25f, -0.25f, 0.0f, 1.0f,
	};
	
	int theProgram;
	int elapsedTimeUniform;
	int positionBufferObject;
	int vao;

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);
	}
	
	private void initializeProgram() {			
		int vertexShader =		ShaderUtils.loadShaderFromFile(GL_VERTEX_SHADER, 	"/rosick/mckesson/shaders/tut03/calcOffset.vert");
		int fragmentShader = 	ShaderUtils.loadShaderFromFile(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/shaders/tut03/calcColor.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
		
	    
	    int uniformLoopDuration = glGetUniformLocation(theProgram, "loopDuration");
		int fragLoopDurUnf = glGetUniformLocation(theProgram, "fragLoopDuration");
		elapsedTimeUniform = glGetUniformLocation(theProgram, "time");
	    
	    glUseProgram(theProgram);
	    glUniform1f(uniformLoopDuration, 5.0f);
	    glUniform1f(fragLoopDurUnf, 10.0f);
	    glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer positionBuffer = IOUtils.allocFloats(vertexPositions);
        
        positionBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STREAM_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
		
	@Override
	protected void render(float elapsedTime) {	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);
		
		glUniform1f(elapsedTimeUniform, elapsedTime);
		
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

		glDrawArrays(GL_TRIANGLES, 0, 3);

		glDisableVertexAttribArray(0);
		glUseProgram(0);
	}
}