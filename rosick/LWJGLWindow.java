package rosick;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Util;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class LWJGLWindow {
		
	// Measured in milliseconds
	private double elapsedTime; 
	private double lastFrameDuration;
	
	private double lastFrameTimestamp, now;
	private boolean continueMainLoop;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public final void start() {		
		start(500, 500);
	}
	
	public final void start(int width, int height) {		
		try {
			Display.setTitle("GLWindow by rosickteam");
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setResizable(true);
			Display.setVSyncEnabled(true);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		long startTime = System.nanoTime();
		continueMainLoop = true;
		
		init();						checkForGlErrors("init()"); 
		reshape(width, height); 	checkForGlErrors("reshape()");	
		
		while (continueMainLoop && !Display.isCloseRequested()) {
			elapsedTime = (System.nanoTime() - startTime) / 1000000.0;
					
			now = System.nanoTime();
		    lastFrameDuration = (now - lastFrameTimestamp) / 1000000.0;
		    lastFrameTimestamp = now;
			
			update();
			display();

			Display.update();
			
			if (Display.wasResized()) {
				reshape(Display.getWidth(), Display.getHeight());
			}
		}
		
		checkForGlErrors("display() or update()");
	}
	
	
	public final void leaveMainLoop() {
		continueMainLoop = false;
	}
	
	
	public final double getElapsedTime() {
		return elapsedTime;
	}
	
	public final double getLastFrameDuration() {
		return lastFrameDuration;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	protected void init() {
	};
	
	
	protected void update() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					leaveMainLoop();
				}
			}
		}	
	};
	
	
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
	};
	
	
	protected void reshape(int width, int height) {
		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final void checkForGlErrors(String method) {
		int error_code = glGetError();
		
		if (error_code != 0) {
			System.err.println("While executing:\t" + method + "\nglGetError():\t\t" + Util.translateGLErrorString(error_code));
		}
	}
}
