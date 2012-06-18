package rosick;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLContext;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class LWJGLWindow {
			
	public final void start() {		
		start(500, 500);
	}
	
	public final void start(int width, int height) {		
		try {
			Display.setTitle("LWJGLWindow");
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setResizable(true);
			Display.create();
			
			if (!GLContext.getCapabilities().OpenGL33) {
				System.err.printf("You must have at least OpenGL 3.3 to run this tutorial.\n");
			}
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		long startTime = System.nanoTime();
		continueMainLoop = true;
		
		init();								
		reshape(width, height); 	
		
		while (continueMainLoop && !Display.isCloseRequested()) {
			elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000.0);
					
			now = System.nanoTime();
		    lastFrameDuration = (float) ((now - lastFrameTimestamp) / 1000000.0);
		    lastFrameTimestamp = now;
			
			update();
			display();

			Display.update();
			
			if (Display.wasResized()) {
				reshape(Display.getWidth(), Display.getHeight());
			}
		}
		
		Display.destroy();
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	protected static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

	
	
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
	
	protected final float getElapsedTime() {
		return elapsedTime;
	}
	
	protected final float getLastFrameDuration() {
		return lastFrameDuration;
	}
	
	
	protected final void leaveMainLoop() {
		continueMainLoop = false;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	// Measured in milliseconds
	private float elapsedTime; 
	private float lastFrameDuration;
	
	private double lastFrameTimestamp, now;
	private boolean continueMainLoop;
}
