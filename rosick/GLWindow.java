package rosick;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;


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
		
		while (continueMainLoop) {
			elapsedTime = (System.nanoTime() - startTime) / 1000000.0;
					
			now = System.nanoTime();
		    lastFrameDuration = (now - lastFrameTimestamp) / 1000000.0;
		    lastFrameTimestamp = now;
			
			update();
			display();

			Display.update();
			
			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
			
			if (Display.wasResized()) {
				reshape(Display.getWidth(), Display.getHeight());
			}
		}
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
}
