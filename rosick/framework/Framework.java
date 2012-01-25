package rosick.framework;

import static org.lwjgl.opengl.GL20.*;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import rosick.glm.Vec2;
import rosick.glutil.pole.MousePole.*;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Framework {

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

	public static void forwardMouseMotion(Pole forward, int x, int y) {
		forward.mouseMove(new Vec2(x, y));		
	}
	
	
	public static void forwardMouseButton(Pole forward, int button, boolean state, int x, int y) {
		int modifiers = calc_glut_modifiers();
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
	
	
	private static int calc_glut_modifiers() {		
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			return 0;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			return 1;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
			return 2;
		}
		
		return -1;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static final float fDegToRad = 3.14159f * 2.0f / 360.0f;

	
	public static float degToRad(float fAngDeg) {
		return fAngDeg * fDegToRad;
	}
}
