package integeruser.jgltut;

import integeruser.jgltut.commons.LightBlock;
import integeruser.jgltut.commons.MaterialBlock;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.commons.UnprojectionBlock;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public abstract class Tutorial {
    protected long window;
    protected GLFWKeyCallback keyCallback;
    protected GLFWMouseButtonCallback mouseCallback;
    protected GLFWCursorPosCallback mousePosCallback;
    protected GLFWScrollCallback mouseScrollCallback;
    protected DoubleBuffer mouseBuffer1 = BufferUtils.createDoubleBuffer(1);
    protected DoubleBuffer mouseBuffer2 = BufferUtils.createDoubleBuffer(1);

    private GLFWErrorCallback errorCallback;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;

    // Measured in seconds
    protected float elapsedTime;
    protected float lastFrameDuration;

    private double lastFrameTimestamp;

    protected FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);
    protected FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer(9);
    protected FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
    protected ByteBuffer projectionBlockBuffer = BufferUtils.createByteBuffer(ProjectionBlock.SIZE_IN_BYTES);
    protected ByteBuffer unprojectionBlockBuffer = BufferUtils.createByteBuffer(UnprojectionBlock.SIZE_IN_BYTES);
    protected ByteBuffer lightBlockBuffer = BufferUtils.createByteBuffer(LightBlock.SIZE_IN_BYTES);
    protected ByteBuffer materialBlockBuffer = BufferUtils.createByteBuffer(MaterialBlock.SIZE_IN_BYTES);

    ////////////////////////////////

    public final void start(int width, int height) {
        try {
            initLWJGL(width, height);
            printInfo();

            init();

            // From [http://www.glfw.org/faq.html#why-is-my-output-in-the-lower-left-corner-of-the-window]:
            // On OS X with a Retina display, and possibly on other platforms in the future, screen coordinates and
            // pixels do not map 1:1. Use the framebuffer size, which is in pixels, instead of the window size
            IntBuffer w = BufferUtils.createIntBuffer(1);
            IntBuffer h = BufferUtils.createIntBuffer(1);
            glfwGetFramebufferSize(window, w, h);
            int currWidth = w.get(0);
            int currHeight = h.get(0);
            reshape(currWidth, currHeight);

            // Setup a framebuffer size callback. It will be called every time the window is resized
            glfwSetCallback(window, framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
                @Override
                public void invoke(long window, int width, int height) {
                    reshape(width, height);
                }
            });

            // Start main loop
            long startTime = System.nanoTime();
            while (glfwWindowShouldClose(window) == GL_FALSE) {
                elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000000.0);

                double now = System.nanoTime();
                lastFrameDuration = (float) ((now - lastFrameTimestamp) / 1000000000.0);
                lastFrameTimestamp = now;

                update();
                display();

                glfwSwapBuffers(window);
                glfwPollEvents();
            }

            glfwDestroyWindow(window);
            keyCallback.release();
            if (mouseCallback != null) mouseCallback.release();
            if (mousePosCallback != null) mousePosCallback.release();
            if (mouseScrollCallback != null) mouseScrollCallback.release();
            framebufferSizeCallback.release();
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
        if (LWJGLUtil.getPlatform() == LWJGLUtil.Platform.MACOSX) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        }

        // Create the window
        window = glfwCreateWindow(width, height, "Tutorial", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Center our window
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - width) / 2, (GLFWvidmode.height(vidmode) - height) / 2);

        // Setup a key callback, it will be called every time a key is pressed, repeated or released
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    glfwSetWindowShouldClose(window, GL_TRUE);
                }
            }
        });

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
