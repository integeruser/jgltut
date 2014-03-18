package fcagnin.gltut.tut06;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.gltut.framework.Framework;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
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
 * Chapter 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 *
 * @author integeruser
 */
public class Translation extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut06/data/";

        new Translation().start();
    }


    @Override
    protected void init() {
        initializeProgram();
        initializeVertexBuffer();

        vao = glGenVertexArrays();
        glBindVertexArray( vao );

        int colorDataOffset = FLOAT_SIZE * 3 * numberOfVertices;
        glBindBuffer( GL_ARRAY_BUFFER, vertexBufferObject );
        glEnableVertexAttribArray( 0 );
        glEnableVertexAttribArray( 1 );
        glVertexAttribPointer( 0, 3, GL_FLOAT, false, 0, 0 );
        glVertexAttribPointer( 1, 4, GL_FLOAT, false, 0, colorDataOffset );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, indexBufferObject );

        glBindVertexArray( 0 );

        glEnable( GL_CULL_FACE );
        glCullFace( GL_BACK );
        glFrontFace( GL_CW );

        glEnable( GL_DEPTH_TEST );
        glDepthMask( true );
        glDepthFunc( GL_LEQUAL );
        glDepthRange( 0.0f, 1.0f );
    }

    @Override
    protected void display() {
        glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glClearDepth( 1.0f );
        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        glUseProgram( theProgram );

        glBindVertexArray( vao );

        float elapsedTime = getElapsedTime() / 1000.0f;
        for ( Instance currInst : instanceList ) {
            final Mat4 transformMatrix = currInst.constructMatrix( elapsedTime );

            glUniformMatrix4( modelToCameraMatrixUnif, false, transformMatrix.fillAndFlipBuffer( mat4Buffer ) );
            glDrawElements( GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0 );
        }

        glBindVertexArray( 0 );
        glUseProgram( 0 );
    }

    @Override
    protected void reshape(int width, int height) {
        cameraToClipMatrix.set( 0, 0, frustumScale / (width / (float) height) );
        cameraToClipMatrix.set( 1, 1, frustumScale );

        glUseProgram( theProgram );
        glUniformMatrix4( cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer( mat4Buffer ) );
        glUseProgram( 0 );

        glViewport( 0, 0, width, height );
    }


    ////////////////////////////////
    private int theProgram;

    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;

    private Mat4 cameraToClipMatrix = new Mat4( 0.0f );

    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );

    private final float frustumScale = calcFrustumScale( 45.0f );


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, "PosColorLocalTransform.vert" ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, "ColorPassthrough.frag" ) );

        theProgram = Framework.createProgram( shaderList );

        modelToCameraMatrixUnif = glGetUniformLocation( theProgram, "modelToCameraMatrix" );
        cameraToClipMatrixUnif = glGetUniformLocation( theProgram, "cameraToClipMatrix" );

        float zNear = 1.0f;
        float zFar = 45.0f;

        cameraToClipMatrix.set( 0, 0, frustumScale );
        cameraToClipMatrix.set( 1, 1, frustumScale );
        cameraToClipMatrix.set( 2, 2, (zFar + zNear) / (zNear - zFar) );
        cameraToClipMatrix.set( 2, 3, -1.0f );
        cameraToClipMatrix.set( 3, 2, (2 * zFar * zNear) / (zNear - zFar) );

        glUseProgram( theProgram );
        glUniformMatrix4( cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer( mat4Buffer ) );
        glUseProgram( 0 );
    }


    private float calcFrustumScale(float fovDeg) {
        final float degToRad = 3.14159f * 2.0f / 360.0f;
        float fFovRad = fovDeg * degToRad;

        return 1.0f / (float) (Math.tan( fFovRad / 2.0f ));
    }


    ////////////////////////////////
    private final int numberOfVertices = 8;

    private final float vertexData[] = {
            +1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f,

            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.5f, 0.5f, 0.0f, 1.0f
    };

    private final short indexData[] = {
            0, 1, 2,
            1, 0, 3,
            2, 3, 0,
            3, 2, 1,

            5, 4, 6,
            4, 5, 7,
            7, 6, 4,
            6, 7, 5
    };

    private int vertexBufferObject;
    private int indexBufferObject;

    private int vao;


    private void initializeVertexBuffer() {
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer( vertexData.length );
        vertexDataBuffer.put( vertexData );
        vertexDataBuffer.flip();

        vertexBufferObject = glGenBuffers();
        glBindBuffer( GL_ARRAY_BUFFER, vertexBufferObject );
        glBufferData( GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW );
        glBindBuffer( GL_ARRAY_BUFFER, 0 );

        ShortBuffer indexDataBuffer = BufferUtils.createShortBuffer( indexData.length );
        indexDataBuffer.put( indexData );
        indexDataBuffer.flip();

        indexBufferObject = glGenBuffers();
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, indexBufferObject );
        glBufferData( GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW );
        glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
    }


    ////////////////////////////////
    private Instance instanceList[] = {
            new StationaryOffset(),
            new OvalOffset(),
            new BottomCircleOffset()
    };

    private abstract class Instance {
        abstract Vec3 calcOffset(float elapsedTime);

        Mat4 constructMatrix(float elapsedTime) {
            Mat4 theMat = new Mat4( 1.0f );
            theMat.setColumn( 3, new Vec4( calcOffset( elapsedTime ), 1.0f ) );

            return theMat;
        }
    }

    private class StationaryOffset extends Instance {

        @Override
        Vec3 calcOffset(float elapsedTime) {
            return new Vec3( 0.0f, 0.0f, -20.0f );
        }
    }

    private class OvalOffset extends Instance {

        @Override
        Vec3 calcOffset(float elapsedTime) {
            final float loopDuration = 3.0f;
            final float scale = 3.14159f * 2.0f / loopDuration;

            float currTimeThroughLoop = elapsedTime % loopDuration;

            return new Vec3(
                    (float) (Math.cos( currTimeThroughLoop * scale ) * 4.f),
                    (float) (Math.sin( currTimeThroughLoop * scale ) * 6.f),
                    -20.0f );
        }
    }

    private class BottomCircleOffset extends Instance {

        @Override
        Vec3 calcOffset(float elapsedTime) {
            final float loopDuration = 12.0f;
            final float scale = 3.14159f * 2.0f / loopDuration;

            float currTimeThroughLoop = elapsedTime % loopDuration;

            return new Vec3(
                    (float) (Math.cos( currTimeThroughLoop * scale ) * 5.f),
                    -3.5f,
                    (float) (Math.sin( currTimeThroughLoop * scale ) * 5.f - 20.f) );
        }
    }
}