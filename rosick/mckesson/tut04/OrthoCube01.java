package rosick.mckesson.tut04;

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
 * 4. Objects at Rest 
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2004.html
 */
public class OrthoCube01 extends GLWindow {
	
	public static void main(String[] args) {		
		new OrthoCube01().start(600, 600);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	final float vertexData[] = {
		 0.25f,  0.25f, 0.75f, 1.0f,
		 0.25f, -0.25f, 0.75f, 1.0f,
		-0.25f,  0.25f, 0.75f, 1.0f,

		 0.25f, -0.25f, 0.75f, 1.0f,
		-0.25f, -0.25f, 0.75f, 1.0f,
		-0.25f,  0.25f, 0.75f, 1.0f,

		 0.25f,  0.25f, -0.75f, 1.0f,
		-0.25f,  0.25f, -0.75f, 1.0f,
		 0.25f, -0.25f, -0.75f, 1.0f,

		 0.25f, -0.25f, -0.75f, 1.0f,
		-0.25f,  0.25f, -0.75f, 1.0f,
		-0.25f, -0.25f, -0.75f, 1.0f,

		-0.25f,  0.25f,  0.75f, 1.0f,
		-0.25f, -0.25f,  0.75f, 1.0f,
		-0.25f, -0.25f, -0.75f, 1.0f,

		-0.25f,  0.25f,  0.75f, 1.0f,
		-0.25f, -0.25f, -0.75f, 1.0f,
		-0.25f,  0.25f, -0.75f, 1.0f,

		 0.25f,  0.25f,  0.75f, 1.0f,
		 0.25f, -0.25f, -0.75f, 1.0f,
		 0.25f, -0.25f,  0.75f, 1.0f,

		 0.25f,  0.25f,  0.75f, 1.0f,
		 0.25f,  0.25f, -0.75f, 1.0f,
		 0.25f, -0.25f, -0.75f, 1.0f,

		 0.25f,  0.25f, -0.75f, 1.0f,
		 0.25f,  0.25f,  0.75f, 1.0f,
		-0.25f,  0.25f,  0.75f, 1.0f,

		 0.25f,  0.25f, -0.75f, 1.0f,
		-0.25f,  0.25f,  0.75f, 1.0f,
		-0.25f,  0.25f, -0.75f, 1.0f,

		 0.25f, -0.25f, -0.75f, 1.0f,
		-0.25f, -0.25f,  0.75f, 1.0f,
		 0.25f, -0.25f,  0.75f, 1.0f,

		 0.25f, -0.25f, -0.75f, 1.0f,
		-0.25f, -0.25f, -0.75f, 1.0f,
		-0.25f, -0.25f,  0.75f, 1.0f,




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
	
	int theProgram;
	int offsetUniform;
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
		
		glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);
	}
	
	private void initializeProgram() {			
		int vertexShader =		ShaderUtils.loadShaderFromFile(GL_VERTEX_SHADER, 	"/rosick/mckesson/shaders/tut04/orthoWithOffset.vert");
		int fragmentShader = 	ShaderUtils.loadShaderFromFile(GL_FRAGMENT_SHADER, 	"/rosick/mckesson/shaders/tut04/standardColors.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
		
	    
		offsetUniform = glGetUniformLocation(theProgram, "offset");
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
		
		glUniform2f(offsetUniform, 0.5f, 0.25f);
		
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, vertexData.length * 2);
		glDrawArrays(GL_TRIANGLES, 0, 3*12);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glUseProgram(0);
	}
}