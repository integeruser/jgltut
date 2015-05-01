package fcagnin.jgltut.tut03;

import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.Framework;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

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
public class CpuPositionOffset extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut03/data/";
        new CpuPositionOffset().start();
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
        adjustVertexData();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(theProgram);

        glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, 3);

        glDisableVertexAttribArray(0);
        glUseProgram(0);
    }

    ////////////////////////////////
    private int theProgram;


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "Standard.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "Standard.frag"));
        theProgram = Framework.createProgram(shaderList);
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

    ////////////////////////////////
    private float xOffset, yOffset;


    private void computePositionOffsets() {
        final float loopDuration = 5.0f;
        final float scale = 3.14159f * 2.0f / loopDuration;
        final float elapsedTime = getElapsedTime() / 1000.0f;
        final float currTimeThroughLoop = elapsedTime % loopDuration;
        xOffset = (float) (Math.cos(currTimeThroughLoop * scale) * 0.5f);
        yOffset = (float) (Math.sin(currTimeThroughLoop * scale) * 0.5f);
    }

    private void adjustVertexData() {
        float newData[] = Arrays.copyOf(vertexPositions, vertexPositions.length);
        for (int vertex = 0; vertex < vertexPositions.length; vertex += 4) {
            newData[vertex] += xOffset;
            newData[vertex + 1] += yOffset;
        }

        FloatBuffer newDataBuffer = BufferUtils.createFloatBuffer(newData.length);
        newDataBuffer.put(newData);
        newDataBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
        glBufferSubData(GL_ARRAY_BUFFER, 0, newDataBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}