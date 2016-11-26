package integeruser.jgltut;

import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.MaterialBlock;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.commons.UnprojectionBlock;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 */
public abstract class Tutorial {
    protected long window;

    protected DoubleBuffer mouseBuffer1 = BufferUtils.createDoubleBuffer(1);
    protected DoubleBuffer mouseBuffer2 = BufferUtils.createDoubleBuffer(1);

    // Measured in seconds
    protected float elapsedTime;
    protected float lastFrameDuration;

    private double lastFrameTimestamp;

    protected FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);
    protected FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
    protected FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    protected ByteBuffer projectionBlockBuffer = BufferUtils.createByteBuffer(ProjectionBlock.BYTES);
    protected ByteBuffer unprojectionBlockBuffer = BufferUtils.createByteBuffer(UnprojectionBlock.BYTES);
    protected ByteBuffer lightBlockBuffer = BufferUtils.createByteBuffer(LightBlock.BYTES);
    protected ByteBuffer materialBlockBuffer = BufferUtils.createByteBuffer(MaterialBlock.BYTES);

    ////////////////////////////////

    public final void start(int width, int height) {
        try {
            GLFWErrorCallback.createPrint(System.err).set();

            initGLFW();
            initWindow(width, height);

            glfwSwapInterval(1);
            GL.createCapabilities();

            initCallbacks();
            init();
            initViewport();

            printInfo();

            loop();

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }


    private void initGLFW() {
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        }
    }

    private void initWindow(int width, int height) {
        window = glfwCreateWindow(width, height, "jgltut", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
    }

    private void initViewport() {
        // From http://www.glfw.org/faq.html#why-is-my-output-in-the-lower-left-corner-of-the-window:
        // "On OS X with a Retina display, and possibly on other platforms in the future, screen coordinates and
        // pixels do not map 1:1. Use the framebuffer size, which is in pixels, instead of the window size."
        int[] w = new int[1], h = new int[1];
        glfwGetFramebufferSize(window, w, h);
        reshape(w[0], h[0]);
    }

    private void initCallbacks() {
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            reshape(width, height);
        });

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true);
        });
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

    private void loop() {
        long startTime = System.nanoTime();
        while (!glfwWindowShouldClose(window)) {
            elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000000.0);

            double now = System.nanoTime();
            lastFrameDuration = (float) ((now - lastFrameTimestamp) / 1000000000.0);
            lastFrameTimestamp = now;

            update();
            display();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    ////////////////////////////////

    protected abstract void init();

    protected abstract void display();

    protected abstract void reshape(int w, int h);

    protected abstract void update();

    ////////////////////////////////

    protected final boolean isKeyPressed(int key) {
        return glfwGetKey(window, key) == 1;
    }

    protected final boolean isMouseButtonPressed(int key) {
        return glfwGetMouseButton(window, key) == 1;
    }
}
