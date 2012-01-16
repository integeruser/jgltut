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
 * Jason L. McKesson
 * II. Positioning
 * Chapter 3. OpenGL's Moving Triangle
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2003.html
 */
public class VertCalcOffset03 extends GLWindow {
	
	public static void main(String[] args) {		
		new	VertCalcOffset03().start(600, 600);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	final float vertexPositions[] = {
		0.25f, 0.25f, 0.0f, 1.0f,
		0.25f, -0.25f, 0.0f, 1.0f,
		-0.25f, -0.25f, 0.0f, 1.0f,
	};
	
	int theProgram;
	int uniformLoopDuration, uniformTime;
	int positionBufferObject;
	int vao;
	float fXOffset, fYOffset;

	
	
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
		int fragmentShader = 	ShaderUtils.loadShaderFromFile(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/shaders/tut03/standard.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
		
		
	    uniformLoopDuration = glGetUniformLocation(theProgram, "loopDuration");
	    uniformTime = glGetUniformLocation(theProgram, "time");
	    
	    glUseProgram(theProgram);
	    glUniform1f(uniformLoopDuration, 5.0f);
	    glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer positionBuffer = IOUtils.allocFloats(vertexPositions);
        
        positionBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
		
	@Override
	protected void render(float fElapsedTime) {			
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);

		glUniform1f(uniformTime, fElapsedTime);
		
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
		
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

		glDrawArrays(GL_TRIANGLES, 0, 3);

		glDisableVertexAttribArray(0);
		glUseProgram(0);
	}
}