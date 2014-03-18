package fcagnin.gltut.tut15;

import fcagnin.gltut.LWJGLWindow;
import fcagnin.gltut.framework.Framework;
import fcagnin.gltut.framework.Mesh;
import fcagnin.gltut.framework.Timer;
import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glimg.DdsLoader;
import fcagnin.jglsdk.glimg.ImageSet;
import fcagnin.jglsdk.glimg.ImageSet.Dimensions;
import fcagnin.jglsdk.glimg.ImageSet.SingleImage;
import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glutil.MatrixStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part IV. Texturing
 * Chapter 15. Many Images
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2015.html
 * <p/>
 * SPACE        - toggle between loaded/constructed texture.
 * Y            - toggle between plane/corridor mesh.
 * P            - toggle pausing.
 * 1,2,3,4,5,6  - switch filtering technique.
 *
 * @author integeruser
 */
public class ManyImages extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/gltut/tut15/data/";

        new ManyImages().start();
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            corridor = new Mesh( "Corridor.xml" );
            plane = new Mesh( "BigPlane.xml" );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            System.exit( -1 );
        }

        glEnable( GL_CULL_FACE );
        glCullFace( GL_BACK );
        glFrontFace( GL_CW );

        final float depthZNear = 0.0f;
        final float depthZFar = 1.0f;

        glEnable( GL_DEPTH_TEST );
        glDepthMask( true );
        glDepthFunc( GL_LEQUAL );
        glDepthRange( depthZNear, depthZFar );
        glEnable( GL_DEPTH_CLAMP );

        // Setup our Uniform Buffers
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, projectionUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW );

        glBindBufferRange( GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer,
                0, ProjectionBlock.SIZE );

        glBindBuffer( GL_UNIFORM_BUFFER, 0 );

        loadCheckerTexture();
        loadMipmapTexture();
        createSamplers();
    }

    @Override
    protected void display() {
        glClearColor( 0.75f, 0.75f, 1.0f, 1.0f );
        glClearDepth( 1.0f );
        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        camTimer.update( getElapsedTime() );

        float cyclicAngle = camTimer.getAlpha() * 6.28f;
        float hOffset = (float) (Math.cos( cyclicAngle ) * 0.25f);
        float vOffset = (float) (Math.sin( cyclicAngle ) * 0.25f);

        MatrixStack modelMatrix = new MatrixStack();

        final Mat4 worldToCamMat = Glm.lookAt(
                new Vec3( hOffset, 1.0f, -64.0f ),
                new Vec3( hOffset, -5.0f + vOffset, -44.0f ),
                new Vec3( 0.0f, 1.0f, 0.0f ) );

        modelMatrix.applyMatrix( worldToCamMat );

        glUseProgram( program.theProgram );
        glUniformMatrix4( program.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

        glActiveTexture( GL_TEXTURE0 + colorTexUnit );
        glBindTexture( GL_TEXTURE_2D, useMipmapTexture ? mipmapTestTexture : checkerTexture );
        glBindSampler( colorTexUnit, samplers[currSampler] );

        if ( drawCorridor ) {
            corridor.render( "tex" );
        } else {
            plane.render( "tex" );
        }

        glBindSampler( colorTexUnit, 0 );
        glBindTexture( GL_TEXTURE_2D, 0 );

        glUseProgram( 0 );
    }

    @Override
    protected void reshape(int width, int height) {
        MatrixStack persMatrix = new MatrixStack();
        persMatrix.perspective( 90.0f, (width / (float) height), zNear, zFar );

        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix.top();

        glBindBuffer( GL_UNIFORM_BUFFER, projectionUniformBuffer );
        glBufferSubData( GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer( mat4Buffer ) );
        glBindBuffer( GL_UNIFORM_BUFFER, 0 );

        glViewport( 0, 0, width, height );
    }

    @Override
    protected void update() {
        while ( Keyboard.next() ) {
            if ( Keyboard.getEventKeyState() ) {
                switch ( Keyboard.getEventKey() ) {
                    case Keyboard.KEY_SPACE:
                        useMipmapTexture = !useMipmapTexture;
                        break;

                    case Keyboard.KEY_Y:
                        drawCorridor = !drawCorridor;
                        break;

                    case Keyboard.KEY_P:
                        camTimer.togglePause();
                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }


                if ( Keyboard.KEY_1 <= Keyboard.getEventKey() && Keyboard.getEventKey() <= Keyboard.KEY_9 ) {
                    int number = Keyboard.getEventKey() - Keyboard.KEY_1;
                    if ( number < NUM_SAMPLERS ) {
                        System.out.printf( "Sampler: %s\n", samplerNames[number] );
                        currSampler = number;
                    }
                }
            }
        }
    }


    ////////////////////////////////
    private float zNear = 1.0f;
    private float zFar = 1000.0f;

    private ProgramData program;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;
    }


    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );


    private void initializePrograms() {
        program = loadProgram( "PT.vert", "Tex.frag" );
    }

    private ProgramData loadProgram(String vertexShaderFilename, String fragmentShaderFilename) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, vertexShaderFilename ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, fragmentShaderFilename ) );

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram( shaderList );
        data.modelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "modelToCameraMatrix" );

        int projectionBlock = glGetUniformBlockIndex( data.theProgram, "Projection" );
        glUniformBlockBinding( data.theProgram, projectionBlock, projectionBlockIndex );

        int colorTextureUnif = glGetUniformLocation( data.theProgram, "colorTexture" );
        glUseProgram( data.theProgram );
        glUniform1i( colorTextureUnif, colorTexUnit );
        glUseProgram( 0 );

        return data;
    }


    ////////////////////////////////
    private final int NUM_SAMPLERS = 6;
    private int[] samplers = new int[NUM_SAMPLERS];

    private final int colorTexUnit = 0;

    private int checkerTexture;
    private int mipmapTestTexture;

    private final byte[] mipmapColors = {
            (byte) 0xFF, (byte) 0xFF, 0x00,
            (byte) 0xFF, 0x00, (byte) 0xFF,
            0x00, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, 0x00, 0x00,
            0x00, (byte) 0xFF, 0x00,
            0x00, 0x00, (byte) 0xFF,
            0x00, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    private final String[] samplerNames = {
            "Nearest",
            "Linear",
            "Linear with nearest mipmaps",
            "Linear with linear mipmaps",
            "Low anisotropic",
            "Max anisotropic"
    };

    private Mesh plane;
    private Mesh corridor;

    private Timer camTimer = new Timer( Timer.Type.LOOP, 5.0f );

    private boolean useMipmapTexture;
    private boolean drawCorridor;
    private int currSampler;


    private void loadMipmapTexture() {
        mipmapTestTexture = glGenTextures();
        glBindTexture( GL_TEXTURE_2D, mipmapTestTexture );

        int oldAlign = glGetInteger( GL_UNPACK_ALIGNMENT );
        glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        for ( int mipmapLevel = 0; mipmapLevel < 8; mipmapLevel++ ) {
            int width = 128 >> mipmapLevel;
            int height = 128 >> mipmapLevel;
            ArrayList<Byte> texture = new ArrayList<>();

            final int currColor = mipmapLevel * 3;
            fillWithColor( texture, mipmapColors[currColor], mipmapColors[currColor + 1], mipmapColors[currColor + 2], width, height );

            ByteBuffer textureBuffer = BufferUtils.createByteBuffer( texture.size() );
            for ( Byte b : texture ) {
                textureBuffer.put( (byte) b );
            }
            textureBuffer.flip();

            glTexImage2D( GL_TEXTURE_2D, mipmapLevel, GL_RGB8, width, height, 0,
                    GL_RGB, GL_UNSIGNED_BYTE, textureBuffer );
        }

        glPixelStorei( GL_UNPACK_ALIGNMENT, oldAlign );

        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0 );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 7 );
        glBindTexture( GL_TEXTURE_2D, 0 );
    }

    private void fillWithColor(ArrayList<Byte> buffer, byte red, byte green, byte blue, int width, int height) {
        int numTexels = width * height;

        for ( int i = 0; i < numTexels * 3; i++ ) {
            buffer.add( red );
            buffer.add( green );
            buffer.add( blue );
        }
    }


    private void loadCheckerTexture() {
        try {
            String filepath = Framework.findFileOrThrow( "checker.dds" );
            ImageSet imageSet = DdsLoader.loadFromFile( filepath );

            checkerTexture = glGenTextures();
            glBindTexture( GL_TEXTURE_2D, checkerTexture );

            for ( int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++ ) {
                SingleImage image = imageSet.getImage( mipmapLevel, 0, 0 );
                Dimensions imageDimensions = image.getDimensions();

                glTexImage2D( GL_TEXTURE_2D, mipmapLevel, GL_RGB8, imageDimensions.width, imageDimensions.height, 0,
                        GL12.GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData() );
            }

            glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0 );
            glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1 );
            glBindTexture( GL_TEXTURE_2D, 0 );
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }


    private void createSamplers() {
        for ( int samplerIndex = 0; samplerIndex < NUM_SAMPLERS; samplerIndex++ ) {
            samplers[samplerIndex] = glGenSamplers();
            glSamplerParameteri( samplers[samplerIndex], GL_TEXTURE_WRAP_S, GL_REPEAT );
            glSamplerParameteri( samplers[samplerIndex], GL_TEXTURE_WRAP_T, GL_REPEAT );
        }

        // Nearest
        glSamplerParameteri( samplers[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        glSamplerParameteri( samplers[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST );

        // Linear
        glSamplerParameteri( samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glSamplerParameteri( samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR );

        // Linear mipmap Nearest
        glSamplerParameteri( samplers[2], GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glSamplerParameteri( samplers[2], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST );

        // Linear mipmap linear
        glSamplerParameteri( samplers[3], GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glSamplerParameteri( samplers[3], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );

        // Low anisotropic
        glSamplerParameteri( samplers[4], GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glSamplerParameteri( samplers[4], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
        glSamplerParameterf( samplers[4], GL_TEXTURE_MAX_ANISOTROPY_EXT, 4.0f );

        // Max anisotropic
        float maxAniso = glGetFloat( GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT );
        System.out.printf( "Maximum anisotropy: %f\n", maxAniso );

        glSamplerParameteri( samplers[5], GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        glSamplerParameteri( samplers[5], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
        glSamplerParameterf( samplers[5], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso );
    }


    ////////////////////////////////
    private final int projectionBlockIndex = 0;

    private int projectionUniformBuffer;

    private class ProjectionBlock extends BufferableData<FloatBuffer> {
        Mat4 cameraToClipMatrix;

        static final int SIZE = Mat4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            return cameraToClipMatrix.fillBuffer( buffer );
        }
    }
}