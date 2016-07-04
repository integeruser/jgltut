package org.jgltut.tut04;

import org.jgltut.LWJGLWindow;
import org.jgltut.framework.Framework;
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
 */
public class AspectRatio extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/org/jgltut/tut04/data/";
        new AspectRatio().start(500, 500);
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

        glUniform2f(offsetUniform, 1.5f, 0.5f);

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
        perspectiveMatrix[0] = frustumScale / (w / (float) h);
        perspectiveMatrix[5] = frustumScale;

        FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer(perspectiveMatrix.length);
        perspectiveMatrixBuffer.put(perspectiveMatrix);
        perspectiveMatrixBuffer.flip();

        glUseProgram(theProgram);
        glUniformMatrix4fv(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
        glUseProgram(0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
    }

    ////////////////////////////////
    private int theProgram;

    private int offsetUniform;
    private int perspectiveMatrixUnif;

    private float perspectiveMatrix[];
    private final float frustumScale = 1.0f;


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "MatrixPerspective.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "StandardColors.frag"));
        theProgram = Framework.createProgram(shaderList);

        offsetUniform = glGetUniformLocation(theProgram, "offset");
        perspectiveMatrixUnif = glGetUniformLocation(theProgram, "perspectiveMatrix");

        float zNear = 0.5f;
        float zFar = 3.0f;
        perspectiveMatrix = new float[16];
        perspectiveMatrix[0] = frustumScale;
        perspectiveMatrix[5] = frustumScale;
        perspectiveMatrix[10] = (zFar + zNear) / (zNear - zFar);
        perspectiveMatrix[14] = (2 * zFar * zNear) / (zNear - zFar);
        perspectiveMatrix[11] = -1.0f;

        FloatBuffer perspectiveMatrixBuffer = BufferUtils.createFloatBuffer(perspectiveMatrix.length);
        perspectiveMatrixBuffer.put(perspectiveMatrix);
        perspectiveMatrixBuffer.flip();

        glUseProgram(theProgram);
        glUniformMatrix4fv(perspectiveMatrixUnif, false, perspectiveMatrixBuffer);
        glUseProgram(0);
    }

    ////////////////////////////////
    private final float[] vertexData = {
            0.25f, 0.25f, -1.25f, 1.0f,
            0.25f, -0.25f, -1.25f, 1.0f,
            -0.25f, 0.25f, -1.25f, 1.0f,

            0.25f, -0.25f, -1.25f, 1.0f,
            -0.25f, -0.25f, -1.25f, 1.0f,
            -0.25f, 0.25f, -1.25f, 1.0f,

            0.25f, 0.25f, -2.75f, 1.0f,
            -0.25f, 0.25f, -2.75f, 1.0f,
            0.25f, -0.25f, -2.75f, 1.0f,

            0.25f, -0.25f, -2.75f, 1.0f,
            -0.25f, 0.25f, -2.75f, 1.0f,
            -0.25f, -0.25f, -2.75f, 1.0f,

            -0.25f, 0.25f, -1.25f, 1.0f,
            -0.25f, -0.25f, -1.25f, 1.0f,
            -0.25f, -0.25f, -2.75f, 1.0f,

            -0.25f, 0.25f, -1.25f, 1.0f,
            -0.25f, -0.25f, -2.75f, 1.0f,
            -0.25f, 0.25f, -2.75f, 1.0f,

            0.25f, 0.25f, -1.25f, 1.0f,
            0.25f, -0.25f, -2.75f, 1.0f,
            0.25f, -0.25f, -1.25f, 1.0f,

            0.25f, 0.25f, -1.25f, 1.0f,
            0.25f, 0.25f, -2.75f, 1.0f,
            0.25f, -0.25f, -2.75f, 1.0f,

            0.25f, 0.25f, -2.75f, 1.0f,
            0.25f, 0.25f, -1.25f, 1.0f,
            -0.25f, 0.25f, -1.25f, 1.0f,

            0.25f, 0.25f, -2.75f, 1.0f,
            -0.25f, 0.25f, -1.25f, 1.0f,
            -0.25f, 0.25f, -2.75f, 1.0f,

            0.25f, -0.25f, -2.75f, 1.0f,
            -0.25f, -0.25f, -1.25f, 1.0f,
            0.25f, -0.25f, -1.25f, 1.0f,

            0.25f, -0.25f, -2.75f, 1.0f,
            -0.25f, -0.25f, -2.75f, 1.0f,
            -0.25f, -0.25f, -1.25f, 1.0f,


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