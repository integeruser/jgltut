package rosick.mckesson.tut01;

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
 * Chapter 1. Hello, Triangle! 
 * http://www.arcsynthesis.org/gltut/Basics/Tutorial%2001.html
 */
public class HelloTriangle extends GLWindow {
	
	public static void main(String[] args) {
		new HelloTriangle().start(600, 600);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	final float vertexPositions[] = {
		 0.75f,  0.75f, 0.0f, 1.0f,
		 0.75f, -0.75f, 0.0f, 1.0f,
		-0.75f, -0.75f, 0.0f, 1.0f,
	};
	
	final String strVertexShader = 
		"#version 330 \n" +
		"\n" +
		"layout(location = 0) in vec4 position;\n" +
		"void main()\n" +
		"{\n" +
		"    gl_Position = position;\n" +
		"}";
		
	final String strFragmentShader = 
		"#version 330\n" +
		"\n" +
		"out vec4 outputColor;\n" +
		"void main()\n" +
		"{\n" +
		"   outputColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n" +
		"}";
	
	int theProgram;
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
		int vertexShader =		ShaderUtils.loadShader(GL_VERTEX_SHADER, 	strVertexShader);
		int fragmentShader = 	ShaderUtils.loadShader(GL_FRAGMENT_SHADER, 	strFragmentShader);
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer positionBuffer = IOUtils.allocFloats(vertexPositions);
        
		positionBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

		
	@Override
	public void render(float fElapsedTime) {	
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(theProgram);

		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

		glDrawArrays(GL_TRIANGLES, 0, 3);

		glDisableVertexAttribArray(0);
		glUseProgram(0);
	}
}