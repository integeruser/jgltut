package jgltut.tut03;

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
 * Chapter 3. OpenGL's Moving Triangle
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2003.html
 *
 * @author integeruser
 */
public class FragChangeColor extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/jgltut/tut03/data/";
        new FragChangeColor().start(500, 500);
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
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(theProgram);

        glUniform1f(elapsedTimeUniform, getElapsedTime() / 1000.0f);

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

    private int elapsedTimeUniform;


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "CalcOffset.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "CalcColor.frag"));
        theProgram = Framework.createProgram(shaderList);

        elapsedTimeUniform = glGetUniformLocation(theProgram, "time");

        glUseProgram(theProgram);
        int uniformLoopDuration = glGetUniformLocation(theProgram, "loopDuration");
        glUniform1f(uniformLoopDuration, 5.0f);
        int fragLoopDurUnf = glGetUniformLocation(theProgram, "fragLoopDuration");
        glUniform1f(fragLoopDurUnf, 10.0f);
        glUseProgram(0);
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
        glBufferData(GL_ARRAY_BUFFER, vertexPositionsBuffer, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}