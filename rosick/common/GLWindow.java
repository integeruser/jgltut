package rosick.common;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;


public class GLWindow {
	
	public final void start() {
		long startTime = System.currentTimeMillis();
		float elapsedTime;
		int width = 600, height = 600;
		
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
			elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
			
			render(elapsedTime);
			
			Display.update();
			Display.sync(100);
			
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
	
	
	protected void render(float elapsedTime) {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
	};
	
	
	protected void reshape(int width, int height) {
		glViewport(0, 0, width, height);
	}
}
