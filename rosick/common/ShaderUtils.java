package rosick.common;

import static org.lwjgl.opengl.GL20.*;

import java.util.ArrayList;


public class ShaderUtils {

	/**
	 * Create a program and attach shaders to it.
	 * @param shaderList the list of shaders to attach
	 * @return the created program
	 */
	public static int createProgram(ArrayList<Integer> shaderList) {		
		// Create the program
	    int program = glCreateProgram();
	    
	    if (program != 0) {
		    // If created, attach each shader to the program
		    for (Integer shader : shaderList) {
		    	glAttachShader(program, shader);
			}
		    	    
		    // Link the program
		    glLinkProgram(program);
	        glValidateProgram(program);
	    }
	    
	    return program;
	}
	  
    
	/**
	 * Create and compile a vertex / fragment shader.
	 * @param shaderType can assume two possible values: GL_VERTEX_SHADER to create a vertex shader, GL_FRAGMENT_SHADER to create a fragment shader
	 * @param shaderCode the shader code
	 * @return the created shader
	 */
    public static int loadShader(int shaderType, String shaderCode){
        int shader = glCreateShader(shaderType);
       
        if (shader != 0) {            
            // Associate the code String with the created shader and compile
            glShaderSource(shader, shaderCode);
            glCompileShader(shader);
        }
        
        return shader;
    }
    
	/**
	 * Create and compile a vertex / fragment shader.
	 * @param path the path of the shader file
	 * @param shaderType can assume two possible values: GL_VERTEX_SHADER to create a vertex shader, GL_FRAGMENT_SHADER to create a fragment shader
	 * @return the created shader
	 */
    public static int loadShaderFromFile(int shaderType, String path){
        int shader = glCreateShader(shaderType);
       
        if (shader != 0) {
            // If created, convert the vertex / fragment shader code to a String
            String shaderCode = IOUtils.loadFileAsString(path);
            
            // Associate the code String with the created shader and compile
            glShaderSource(shader, shaderCode);
            glCompileShader(shader);
        }
        
        return shader;
    }
}