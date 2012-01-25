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
public class GLWindow {
	
	// Measured in milliseconds
	protected double elapsedTime; 
	protected double lastFrameDuration;
	
	private double lastFrameTimestamp, now;
	private boolean continueMainLoop;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public final void start(int width, int height) {		
		try {
			Display.setTitle("Tutorials by rosickteam");
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
		
		init();	
		reshape(width, height);
		
		checkForGlErrors();
		
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
		
		checkForGlErrors();
	}
	
	
	public final void leaveMainLoop() {
		continueMainLoop = false;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	protected void init() {
	};
	
	
	protected void update() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			leaveMainLoop();
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
	
	private final void checkForGlErrors() {
		int error_code = glGetError();
		
		if (error_code != 0) {
			System.err.println("glGetError() says: " + Util.translateGLErrorString(error_code));
		}
	}
}
