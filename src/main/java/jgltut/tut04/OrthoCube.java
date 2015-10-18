package jgltut.tut04;

import jgltut.LWJGLWindow;
import jgltut.framework.Framework;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part II. Positioning
 * Chapter 4. Objects at Rest
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2004.html
 *
 * @author integeruser
 */
public class OrthoCube extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut04/data/";
        new OrthoCube().start(500, 500);
    }


    @Override
    protected void init() {
        initializeProgram();
        initializeVertexBuffer();

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
    }

    @Override
    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(theProgram);

        glUniform2f(offsetUniform, 0.5f, 0.25f);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
        int colorData = (FLOAT_SIZE * vertexData.length) / 2;
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorData);

        glDrawArrays(GL_TRIANGLES, 0, 36);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glUseProgram(0);
    }

    @Override
    protected void reshape(int w, int h) {
        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
    }

    ////////////////////////////////
    private int theProgram;

    private int offsetUniform;


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "OrthoWithOffset.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "StandardColors.frag"));
        theProgram = Framework.createProgram(shaderList);

        offsetUniform = glGetUniformLocation(theProgram, "offset");
    }

    ////////////////////////////////
    private final float[] vertexData = {
            0.25f, 0.25f, 0.75f, 1.0f,
            0.25f, -0.25f, 0.75f, 1.0f,
            -0.25f, 0.25f, 0.75f, 1.0f,

            0.25f, -0.25f, 0.75f, 1.0f,
            -0.25f, -0.25f, 0.75f, 1.0f,
            -0.25f, 0.25f, 0.75f, 1.0f,

            0.25f, 0.25f, -0.75f, 1.0f,
            -0.25f, 0.25f, -0.75f, 1.0f,
            0.25f, -0.25f, -0.75f, 1.0f,

            0.25f, -0.25f, -0.75f, 1.0f,
            -0.25f, 0.25f, -0.75f, 1.0f,
            -0.25f, -0.25f, -0.75f, 1.0f,

            -0.25f, 0.25f, 0.75f, 1.0f,
            -0.25f, -0.25f, 0.75f, 1.0f,
            -0.25f, -0.25f, -0.75f, 1.0f,

            -0.25f, 0.25f, 0.75f, 1.0f,
            -0.25f, -0.25f, -0.75f, 1.0f,
            -0.25f, 0.25f, -0.75f, 1.0f,

            0.25f, 0.25f, 0.75f, 1.0f,
            0.25f, -0.25f, -0.75f, 1.0f,
            0.25f, -0.25f, 0.75f, 1.0f,

            0.25f, 0.25f, 0.75f, 1.0f,
            0.25f, 0.25f, -0.75f, 1.0f,
            0.25f, -0.25f, -0.75f, 1.0f,

            0.25f, 0.25f, -0.75f, 1.0f,
            0.25f, 0.25f, 0.75f, 1.0f,
            -0.25f, 0.25f, 0.75f, 1.0f,

            0.25f, 0.25f, -0.75f, 1.0f,
            -0.25f, 0.25f, 0.75f, 1.0f,
            -0.25f, 0.25f, -0.75f, 1.0f,

            0.25f, -0.25f, -0.75f, 1.0f,
            -0.25f, -0.25f, 0.75f, 1.0f,
            0.25f, -0.25f, 0.75f, 1.0f,

            0.25f, -0.25f, -0.75f, 1.0f,
            -0.25f, -0.25f, -0.75f, 1.0f,
            -0.25f, -0.25f, 0.75f, 1.0f,


            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,

            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,
            0.8f, 0.8f, 0.8f, 1.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,

            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,

            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f
    };

    private int vertexBufferObject;


    private void initializeVertexBuffer() {
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
        vertexDataBuffer.put(vertexData);
        vertexDataBuffer.flip();

        vertexBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}