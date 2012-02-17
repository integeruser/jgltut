package rosick.jglsdk.framework;

import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Framework {

    public static int loadShader(int shaderType, String path){
        int shader = glCreateShader(shaderType);
       
        if (shader != 0) {
        	String shaderCode;
        	
        	{
        		// Load file as a string
                StringBuilder text = new StringBuilder();
                
                try {
                	BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.class.getResourceAsStream(path)));
                	String line;
                	
                	while ((line = reader.readLine()) != null) {
                		text.append(line).append("\n");
                	}
                	
                	reader.close();
                } catch (Exception e){
                	System.err.println("Fail reading " + path + ": " + e.getMessage());
                }
                
                shaderCode = text.toString();
        	}
            
            glShaderSource(shader, shaderCode);
            glCompileShader(shader);
        }
        
        return shader;
    }
    
    
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
	
	private static final float fDegToRad = 3.14159f * 2.0f / 360.0f;

	public static float degToRad(float fAngDeg) {
		return fAngDeg * fDegToRad;
	}
}
