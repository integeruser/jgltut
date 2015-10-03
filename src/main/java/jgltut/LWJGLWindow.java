package jgltut;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class LWJGLWindow {
    protected static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

    protected long window;
    protected GLFWErrorCallback errorCallback;
    protected GLFWKeyCallback keyCallback;
    protected GLFWWindowSizeCallback windowSizeCallback;

    // Measured in milliseconds
    private float elapsedTime;
    private float lastFrameDuration;

    private double lastFrameTimestamp;

    ////////////////////////////////
    public final void start() {
        start(500, 500);
    }

    public final void start(int width, int height) {
        try {
            initLWJGL(width, height);
            printInfo();

            init();

            // Setup a window size callback. It will be called every time the window is resized
            glfwSetCallback(window, windowSizeCallback = new GLFWWindowSizeCallback() {
                @Override
                public void invoke(long window, int width, int height) {
                    reshape(width, height);
                }
            });
            reshape(width, height);

            long startTime = System.nanoTime();
            while (glfwWindowShouldClose(window) == GL_FALSE) {
                elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000.0);

                double now = System.nanoTime();
                lastFrameDuration = (float) ((now - lastFrameTimestamp) / 1000000.0);
                lastFrameTimestamp = now;

                update();
                display();

                glfwSwapBuffers(window);
                glfwPollEvents();
            }

            glfwDestroyWindow(window);
            keyCallback.release();
        } finally {
            glfwTerminate();
            errorCallback.release();
        }
    }


    private void initLWJGL(int width, int height) {
        glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

        if (glfwInit() != GL11.GL_TRUE)
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        // Create the window
        window = glfwCreateWindow(width, height, "LWJGLWindow", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Center our window
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - width) / 2, (GLFWvidmode.height(vidmode) - height) / 2);

        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        GL.createCapabilities();
        glfwShowWindow(window);
    }

    private void printInfo() {
        System.out.println();
        System.out.println("-----------------------------------------------------------");

        System.out.format("%-18s%s\n", "Running:", getClass().getName());
        System.out.format("%-18s%s\n", "OpenGL version:", glGetString(GL_VERSION));

        if (!GL.getCapabilities().OpenGL33) {
            System.out.println("You must have at least OpenGL 3.3 to run this tutorial.");
        }
    }

    ////////////////////////////////
    protected void init() {
        // Setup a key callback. It will be called every time a key is pressed, repeated or released
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, GL_TRUE);
                }
            }
        });
    }

    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    protected void reshape(int w, int h) {
        glViewport(0, 0, w, h);
    }

    protected void update() {
    }

    ////////////////////////////////
    protected final float getElapsedTime() {
        return elapsedTime;
    }

    protected final float getLastFrameDuration() {
        return lastFrameDuration;
    }
}
