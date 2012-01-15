package mckesson.chapter01;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import utils.GLWindow;
import utils.IOUtils;
import utils.ShaderUtils;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;


/**
 * I. The Basics
 * Chapter 1. Hello, Triangle! 
 * http://www.arcsynthesis.org/gltut/Basics/Tutorial%2001.html
 */
public class HelloTriangle extends GLWindow {
	
	public static void main(String[] args) {
		new HelloTriangle().start();
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	final float vertexPositions[] = {
		 0.75f,  0.75f, 0.0f, 1.0f,
		 0.75f, -0.75f, 0.0f, 1.0f,
		-0.75f, -0.75f, 0.0f, 1.0f,
	};
	
	int theProgram, positionBufferObject;
	
	private String strVertexShader = 
		"#version 330 \n" +
		"\n" +
		"layout(location = 0) in vec4 position;\n" +
		"void main()\n" +
		"{\n" +
		"    gl_Position = position;\n" +
		"}";
	
	private String strFragmentShader = 
		"#version 330\n" +
		"\n" +
		"out vec4 outputColor;\n" +
		"void main()\n" +
		"{\n" +
		"   outputColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n" +
		"}";
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	@Override
	protected void initGL() {
		initializeProgram();
		initializeVertexBuffer(); 
		initializeVertexArrayObjects();
	}
	
	private void initializeProgram() {			
		int vertexShader =		ShaderUtils.loadShader(GL_VERTEX_SHADER, 	strVertexShader);
		int fragmentShader = 	ShaderUtils.loadShader(GL_FRAGMENT_SHADER, 	strFragmentShader);
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = ShaderUtils.createProgram(shaderList);
		
	    for (Integer integer : shaderList) {
			glDeleteShader(integer);
		}
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer positionBuffer = IOUtils.allocFloats(vertexPositions);
        
		positionBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private void initializeVertexArrayObjects() {
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
	}

		
	@Override
	public void render(float elapsedTime) {	
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