package rosick.mckesson.I.tut01;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.jglsdk.GLWindow;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * I. The Basics
 * Chapter 1. Hello, Triangle! 
 * http://www.arcsynthesis.org/gltut/Basics/Tutorial%2001.html
 * @author integeruser
 */
public class HelloTriangle extends GLWindow {
	
	public static void main(String[] args) {
		new HelloTriangle().start();
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int theProgram;
	private int positionBufferObject;
	private int vao;
	
	private final float vertexPositions[] = {
		 0.75f,  0.75f, 0.0f, 1.0f,
		 0.75f, -0.75f, 0.0f, 1.0f,
		-0.75f, -0.75f, 0.0f, 1.0f,
	};
	
	private final String strVertexShader = 
		"#version 330 \n" +
		"\n" +
		"layout(location = 0) in vec4 position;\n" +
		"void main()\n" +
		"{\n" +
		"    gl_Position = position;\n" +
		"}";
		
	private final String strFragmentShader = 
		"#version 330\n" +
		"\n" +
		"out vec4 outputColor;\n" +
		"void main()\n" +
		"{\n" +
		"   outputColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n" +
		"}";

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void initializeProgram() {	
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(createShader(GL_VERTEX_SHADER,	strVertexShader));
		shaderList.add(createShader(GL_FRAGMENT_SHADER,	strFragmentShader));

		theProgram = createProgram(shaderList);
		
	    for (Integer shader : shaderList) {
	    	glDeleteShader(shader);
		}
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer tempBuffer = BufferUtils.createFloatBuffer(vertexPositions.length);
		tempBuffer.put(vertexPositions);
		tempBuffer.flip();
        
		positionBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, tempBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);
	}
	
		
	@Override
	protected void display() {	
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
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private int createShader(int shaderType, String strShaderFile) {
        int shader = glCreateShader(shaderType);
        
        if (shader != 0) {            
            glShaderSource(shader, strShaderFile);
            glCompileShader(shader);
        }
        
		return shader;
	}
	
	
	private int createProgram(ArrayList<Integer> shaderList) {		
	    int program = glCreateProgram();
	    
	    if (program != 0) {
		    for (Integer shader : shaderList) {
		    	glAttachShader(program, shader);
			}
		    	    
		    glLinkProgram(program);
	        glValidateProgram(program);
	        
		    for (Integer shader : shaderList) {
		    	glDetachShader(program, shader);
			}
	    }
	    
	    return program;
	}
}