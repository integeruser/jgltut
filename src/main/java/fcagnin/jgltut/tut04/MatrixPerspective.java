package fcagnin.jgltut.tut04;

import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.Framework;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part II. Positioning
 * Chapter 4. Objects at Rest
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2004.html
 *
 * @author integeruser
 */
public class MatrixPerspective extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut04/data/";

        new MatrixPerspective().start();
    }


    @Override
    protected void init() {
        initializeProgram();
        initializeVertexBuffer();

        vao = glGenVertexArrays();
        glBindVertexArray( vao );

        glEnable( GL_CULL_FACE );
        glCullFace( GL_BACK );
        glFrontFace( GL_CW );
    }

    @Override
    protected void display() {
        glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glClear( GL_COLOR_BUFFER_BIT );

        glUseProgram( theProgram );

        glUniform2f( offsetUniform, 0.5f, 0.5f );

        int colorData = (vertexData.length * FLOAT_SIZE) / 2;
        glBindBuffer( GL_ARRAY_BUFFER, vertexBufferObject );
        glEnableVertexAttribArray( 0 );
        glEnableVertexAttribArray( 1 );
        glVertexAttribPointer( 0, 4, GL_FLOAT, false, 0, 0 );
        glVertexAttribPointer( 1, 4, GL_FLOAT, false, 0, colorData );

        glDrawArrays( GL_TRIANGLES, 0, 36 );

        glDisableVertexAttribArray( 0 );
        glDisableVertexAttribArray( 1 );
        glUseProgram( 0 );
    }


    ////////////////////////////////
    private int theProgram;

    private int offsetUniform;


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, "MatrixPerspective.vert" ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, "StandardColors.frag" ) );

        theProgram = Framework.createProgram( shaderList );

        offsetUniform = glGetUniformLocation( theProgram, "offset" );

        int perspectiveMatrixUnif = glGetUniformLocation( theProgram, "perspectiveMatrix" );

        float frustumScale = 1.0f;
        float zNear = 0.5f;
        float zFar = 3.0f;

        float theMatrix[] = new float[16];
        theMatrix[0] = frustumScale;
        theMatrix[5] = frustumScale;
        theMatrix[10] = (zFar + zNear) / (zNear - zFar);
        theMatrix[14] = (2 * zFar * zNear) / (zNear - zFar);
        theMatrix[11] = -1.0f;

        FloatBuffer theMatrixBuffer = BufferUtils.createFloatBuffer( theMatrix.length );
        theMatrixBuffer.put( theMatrix );
        theMatrixBuffer.flip();

        glUseProgram( theProgram );
        glUniformMatrix4( perspectiveMatrixUnif, false, theMatrixBuffer );
        glUseProgram( 0 );
    }


    ////////////////////////////////
    private final float vertexData[] = {
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

    private int vao;


    private void initializeVertexBuffer() {
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer( vertexData.length );
        vertexDataBuffer.put( vertexData );
        vertexDataBuffer.flip();

        vertexBufferObject = glGenBuffers();
        glBindBuffer( GL_ARRAY_BUFFER, vertexBufferObject );
        glBufferData( GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW );
        glBindBuffer( GL_ARRAY_BUFFER, 0 );
    }
}