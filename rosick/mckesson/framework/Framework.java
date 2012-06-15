package rosick.mckesson.framework;

import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Framework {

	public static String COMMON_DATAPATH = "/rosick/mckesson/data/";
	public static String CURRENT_TUTORIAL_DATAPATH = null;
	
	
	public static String findFileOrThrow(String filename) {		
		InputStream fileStream = ClassLoader.class.getResourceAsStream(CURRENT_TUTORIAL_DATAPATH + filename);
		if (fileStream != null) {
			return CURRENT_TUTORIAL_DATAPATH + filename;
		}
		
		fileStream = ClassLoader.class.getResourceAsStream(COMMON_DATAPATH + filename);
		if (fileStream != null) {
			return COMMON_DATAPATH + filename;
		}
		
		throw new RuntimeException("Could not find the file " + filename);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
    public static int loadShader(int shaderType, String filename){
    	String filepath = Framework.findFileOrThrow(filename);
    	
        int shader = glCreateShader(shaderType);
       
        if (shader != 0) {
        	String shaderCode;
        	
        	{
        		// Load file as a string
                StringBuilder text = new StringBuilder();
                
                try {
                	BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.class.getResourceAsStream(filepath)));
                	String line;
                	
                	while ((line = reader.readLine()) != null) {
                		text.append(line).append("\n");
                	}
                	
                	reader.close();
                } catch (Exception e){
                	throw new RuntimeException("Unexpected error");
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
	        
		    for (Integer shader : shaderList) {
		    	glDetachShader(program, shader);
			}
	    }
	    
	    return program;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static float degToRad(float fAngDeg) {
		final float fDegToRad = 3.14159f * 2.0f / 360.0f;
		
		return fAngDeg * fDegToRad;
	}
}
