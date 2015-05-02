package jgltut;

import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class LWJGLWindow {
    protected static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

    // Measured in milliseconds
    private float elapsedTime;
    private float lastFrameDuration;

    private double lastFrameTimestamp;
    private boolean continueMainLoop;

    ////////////////////////////////
    public final void start() {
        start(500, 500);
    }

    public final void start(int width, int height) {
        try {
            Display.setTitle("LWJGLWindow");
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setResizable(true);
            Display.setVSyncEnabled(true);

            if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_MACOSX) {
                Display.create(new PixelFormat(), new ContextAttribs(3, 2).withProfileCore(true));
            } else {
                Display.create();
            }

            printInfo();
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

            double now = System.nanoTime();
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


    private void printInfo() {
        System.out.println();
        System.out.println("-----------------------------------------------------------");

        System.out.format("%-18s%s\n", "Running:", getClass().getName());
        System.out.format("%-18s%s\n", "OpenGL version:", glGetString(GL_VERSION));

        if (!GLContext.getCapabilities().OpenGL33) {
            System.out.println("You must have at least OpenGL 3.3 to run this tutorial.");
        }
    }

    ////////////////////////////////
    protected void init() {
    }

    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    protected void reshape(int w, int h) {
        glViewport(0, 0, w, h);
    }

    protected void update() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                    leaveMainLoop();
                }
            }
        }
    }

    ////////////////////////////////
    protected final float getElapsedTime() {
        return elapsedTime;
    }

    protected final float getLastFrameDuration() {
        return lastFrameDuration;
    }


    protected final void leaveMainLoop() {
        continueMainLoop = false;
    }
}
