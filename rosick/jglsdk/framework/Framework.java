package rosick.jglsdk.framework;

import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import rosick.jglsdk.glm.Vec2;
import rosick.jglsdk.glutil.pole.MousePole.*;


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

	public static void forwardMouseMotion(Pole forward, int x, int y) {
		forward.mouseMove(new Vec2(x, y));		
	}
	
	
	public static void forwardMouseButton(Pole forward, int button, boolean state, int x, int y) {
		MouseModifiers modifiers = calc_glut_modifiers();
		Vec2 mouseLoc = new Vec2(x, y);
		MouseButtons eButton = null;

		switch (button) {
			case 0:
				eButton = MouseButtons.MB_LEFT_BTN;
				break;
			case 1:
				eButton = MouseButtons.MB_RIGHT_BTN;
				break;
			case 2:
				eButton = MouseButtons.MB_MIDDLE_BTN;
				break;
		}

		forward.mouseClick(eButton, state, modifiers, mouseLoc);
	}
	
	
	public static void forwardMouseWheel(Pole forward, int wheel, int direction, int x, int y) {
		forward.mouseWheel(direction, calc_glut_modifiers(), new Vec2(x, y));
	}
	
	
	private static MouseModifiers calc_glut_modifiers() {		
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			return MouseModifiers.MM_KEY_SHIFT;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			return MouseModifiers.MM_KEY_CTRL;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
			return MouseModifiers.MM_KEY_ALT;
		}
		
		return null;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static final float fDegToRad = 3.14159f * 2.0f / 360.0f;

	
	public static float degToRad(float fAngDeg) {
		return fAngDeg * fDegToRad;
	}
}
