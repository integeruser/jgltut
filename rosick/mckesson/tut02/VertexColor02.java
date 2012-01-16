package rosick.mckesson.tut02;

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
 * I. The Basics
 * Chapter 2. Playing with Colors
 * http://www.arcsynthesis.org/gltut/Basics/Tutorial%2002.html
 */
public class VertexColor02 extends GLWindow {
	
	public static void main(String[] args) {		
		new VertexColor02().start(600, 600);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	final float vertexData[] = {													// 3 positions and 3 colors
		 0.0f,    0.5f, 0.0f, 1.0f,
		 0.5f, -0.366f, 0.0f, 1.0f,
		-0.5f, -0.366f, 0.0f, 1.0f,
		 1.0f,    0.0f, 0.0f, 1.0f,
		 0.0f,    1.0f, 0.0f, 1.0f,
		 0.0f,    0.0f, 1.0f, 1.0f,
	};
	
	int theProgram; 
	int vertexBufferObject;
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
		int vertexShader =		ShaderUtils.loadShaderFromFile(GL_VERTEX_SHADER, 	"/rosick/mckesson/shaders/tut02/vertexColors.vert");
		int fragmentShader = 	ShaderUtils.loadShaderFromFile(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/shaders/tut02/vertexColors.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer vertexBuffer = IOUtils.allocFloats(vertexData);
        
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
		
	@Override
	protected void render(float fElapsedTime) {	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);

		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 12 * 4);

		glDrawArrays(GL_TRIANGLES, 0, 3);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glUseProgram(0);
	}
}