package rosick.common;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;


public class GLWindow {
	
	public final void start(int width, int height) {
		long startTime = System.currentTimeMillis();
		float fElapsedTime;
		
		try {
			Display.setTitle("Test");
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.create();
			Display.setVSyncEnabled(true);
			Display.setResizable(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		init();

		while (true) {
			fElapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
			
			update(fElapsedTime);
			render(fElapsedTime);
			
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
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	protected void init() {};
	
	
	protected void update(float fElapsedTime) {};
	
	
	protected void render(float fElapsedTime) {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
	};
	
	
	protected void reshape(int width, int height) {
		glViewport(0, 0, width, height);
	}
}
