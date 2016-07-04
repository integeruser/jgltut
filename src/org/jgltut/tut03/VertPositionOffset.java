package org.jgltut.tut03;

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
 * Chapter 3. OpenGL's Moving Triangle
 */
public class VertPositionOffset extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/org/jgltut/tut03/data/";
        new VertPositionOffset().start(500, 500);
    }


    @Override
    protected void init() {
        initializeProgram();
        initializeVertexBuffer();

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
    }

    @Override
    protected void display() {
        xOffset = 0.0f;
        yOffset = 0.0f;
        computePositionOffsets();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(theProgram);

        glUniform2f(offsetLocation, xOffset, yOffset);

        glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, 3);

        glDisableVertexAttribArray(0);
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

    private int offsetLocation;


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "PositionOffset.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "Standard.frag"));
        theProgram = Framework.createProgram(shaderList);

        offsetLocation = glGetUniformLocation(theProgram, "offset");
    }

    ////////////////////////////////
    private final float[] vertexPositions = {
            0.25f, 0.25f, 0.0f, 1.0f,
            0.25f, -0.25f, 0.0f, 1.0f,
            -0.25f, -0.25f, 0.0f, 1.0f
    };

    private int positionBufferObject;


    private void initializeVertexBuffer() {
        FloatBuffer vertexPositionsBuffer = BufferUtils.createFloatBuffer(vertexPositions.length);
        vertexPositionsBuffer.put(vertexPositions);
        vertexPositionsBuffer.flip();

        positionBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexPositionsBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    ////////////////////////////////
    private float xOffset, yOffset;


    private void computePositionOffsets() {
        final float loopDuration = 5.0f;
        final float scale = 3.14159f * 2.0f / loopDuration;
        final float currTimeThroughLoop = elapsedTime % loopDuration;
        xOffset = (float) (Math.cos(currTimeThroughLoop * scale) * 0.5f);
        yOffset = (float) (Math.sin(currTimeThroughLoop * scale) * 0.5f);
    }
}