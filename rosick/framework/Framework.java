package rosick.framework;

import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.util.ArrayList;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Framework {
	
	private static final float fDegToRad = 3.14159f * 2.0f / 360.0f;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 * Create and compile a vertex / fragment shader from a file.
	 * @param path the path of the shader file
	 * @param shaderType can assume two possible values: GL_VERTEX_SHADER to create a vertex shader, GL_FRAGMENT_SHADER to create a fragment shader
	 * @return the created shader
	 */
    public static int loadShader(int shaderType, String path){
        int shader = glCreateShader(shaderType);
       
        if (shader != 0) {
            String shaderCode = IOUtils.loadFileAsString(path);
            
            glShaderSource(shader, shaderCode);
            glCompileShader(shader);
        }
        
        return shader;
    }
    
    
	/**
	 * Create a program and attach a list of shaders to it.
	 * @param shaderList the list of shaders to attach
	 * @return the created program
	 */
	public static int createProgram(ArrayList<Integer> shaderList) {		
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


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static float degToRad(float fAngDeg) {
		return fAngDeg * fDegToRad;
	}
}
