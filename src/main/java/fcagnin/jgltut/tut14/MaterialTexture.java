package fcagnin.jgltut.tut14;

import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glimg.DdsLoader;
import fcagnin.jglsdk.glimg.ImageSet;
import fcagnin.jglsdk.glimg.ImageSet.Dimensions;
import fcagnin.jglsdk.glimg.ImageSet.SingleImage;
import fcagnin.jglsdk.glm.*;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.*;
import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part IV. Texturing
 * Chapter 14. Textures are not Pictures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2014.html
 * <p/>
 * P        - toggle pausing.
 * -,=      - rewind/jump forward time by 0.5 second (of real-time).
 * T        - toggle viewing the look-at point.
 * G        - toggle the drawing of the light source.
 * Y        - switch between the infinity symbol and a flat plane.
 * SPACE    - switch between one of three rendering modes: fixed shininess with a Gaussian lookup-table,
 * a texture-based shininess with a Gaussian lookup-table, and a texture-based shininess with a shader-computed Gaussian term.
 * 1,2,3,4  - switch to progressively larger textures.
 * 8,9      - switch to the gold material/a material with a dark diffuse color and bright specular color.
 * <p/>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING         - rotate the object horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL  - rotate the object horizontally or vertically only, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT   - spin the object.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 *
 * @author integeruser
 */
public class MaterialTexture extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut14/data/";

        new MaterialTexture().start();
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            objectMesh = new Mesh( "Infinity.xml" );
            cubeMesh = new Mesh( "UnitCube.xml" );
            planeMesh = new Mesh( "UnitPlane.xml" );
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
        setupMaterials();

        lightUniformBuffer = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, lightUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW );

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, projectionUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW );

        // Bind the static buffers.
        glBindBufferRange( GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer,
                0, LightBlock.SIZE );

        glBindBufferRange( GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer,
                0, ProjectionBlock.SIZE );

        glBindBufferRange( GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer,
                0, MaterialBlock.SIZE );

        glBindBuffer( GL_UNIFORM_BUFFER, 0 );

        createGaussianTextures();
        createShininessTexture();
    }

    @Override
    protected void display() {
        lightTimer.update( getElapsedTime() );

        glClearColor( 0.75f, 0.75f, 1.0f, 1.0f );
        glClearDepth( 1.0f );
        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        MatrixStack modelMatrix = new MatrixStack();
        modelMatrix.setMatrix( viewPole.calcMatrix() );
        final Mat4 worldToCamMat = modelMatrix.top();

        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = new Vec4( 0.2f, 0.2f, 0.2f, 1.0f );
        lightData.lightAttenuation = lightAttenuation;

        Vec3 globalLightDirection = new Vec3( 0.707f, 0.707f, 0.0f );

        lightData.lights[0] = new PerLight();
        lightData.lights[0].cameraSpaceLightPos = Mat4.mul( worldToCamMat, new Vec4( globalLightDirection, 0.0f ) );
        lightData.lights[0].lightIntensity = new Vec4( 0.6f, 0.6f, 0.6f, 1.0f );

        lightData.lights[1] = new PerLight();
        lightData.lights[1].cameraSpaceLightPos = Mat4.mul( worldToCamMat, calcLightPosition() );
        lightData.lights[1].lightIntensity = new Vec4( 0.4f, 0.4f, 0.4f, 1.0f );

        glBindBuffer( GL_UNIFORM_BUFFER, lightUniformBuffer );
        glBufferSubData( GL_UNIFORM_BUFFER, 0, lightData.fillAndFlipBuffer( lightBlockBuffer ) );
        glBindBuffer( GL_UNIFORM_BUFFER, 0 );

        {
            Mesh pMesh = g_bUseInfinity ? objectMesh : planeMesh;

            glBindBufferRange( GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, g_currMaterial * materialOffset,
                    MaterialBlock.SIZE );

            modelMatrix.push();

            modelMatrix.applyMatrix( objtPole.calcMatrix() );
            modelMatrix.scale( g_bUseInfinity ? 2.0f : 4.0f );

            Mat3 normMatrix = new Mat3( modelMatrix.top() );
            normMatrix = Glm.transpose( Glm.inverse( normMatrix ) );

            ProgramData prog = programs[shaderMode.ordinal()];

            glUseProgram( prog.theProgram );
            glUniformMatrix4( prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniformMatrix3( prog.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer( mat3Buffer ) );

            glActiveTexture( GL_TEXTURE0 + gaussTexUnit );
            glBindTexture( GL_TEXTURE_2D, g_gaussTextures[g_currTexture] );
            glBindSampler( gaussTexUnit, g_textureSampler );

            glActiveTexture( GL_TEXTURE0 + shineTexUnit );
            glBindTexture( GL_TEXTURE_2D, g_shineTexture );
            glBindSampler( shineTexUnit, g_textureSampler );

            if ( shaderMode != ShaderMode.FIXED ) {
                pMesh.render( "lit-tex" );
            } else {
                pMesh.render( "lit" );
            }

            glBindSampler( gaussTexUnit, 0 );
            glBindTexture( GL_TEXTURE_2D, 0 );

            glUseProgram( 0 );
            glBindBufferBase( GL_UNIFORM_BUFFER, materialBlockIndex, 0 );

            modelMatrix.pop();
        }

        if ( g_bDrawLights ) {
            modelMatrix.push();

            modelMatrix.translate( new Vec3( calcLightPosition() ) );
            modelMatrix.scale( 0.25f );

            glUseProgram( unlit.theProgram );
            glUniformMatrix4( unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

            Vec4 lightColor = new Vec4( 1.0f );
            glUniform4( unlit.objectColorUnif, lightColor.fillAndFlipBuffer( vec4Buffer ) );
            cubeMesh.render( "flat" );

            modelMatrix.pop();

            modelMatrix.translate( globalLightDirection.scale( 100.0f ) );
            modelMatrix.scale( 5.0f );

            glUniformMatrix4( unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            cubeMesh.render( "flat" );

            glUseProgram( 0 );
        }

        if ( g_bDrawCameraPos ) {
            modelMatrix.push();

            modelMatrix.setIdentity();
            modelMatrix.translate( new Vec3( 0.0f, 0.0f, -viewPole.getView().radius ) );
            modelMatrix.scale( 0.25f );

            glDisable( GL_DEPTH_TEST );
            glDepthMask( false );
            glUseProgram( unlit.theProgram );
            glUniformMatrix4( unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( unlit.objectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f );
            cubeMesh.render( "flat" );
            glDepthMask( true );
            glEnable( GL_DEPTH_TEST );
            glUniform4f( unlit.objectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f );
            cubeMesh.render( "flat" );

            modelMatrix.pop();
        }
    }

    @Override
    protected void reshape(int width, int height) {
        MatrixStack persMatrix = new MatrixStack();
        persMatrix.perspective( 45.0f, (width / (float) height), zNear, zFar );

        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix.top();

        glBindBuffer( GL_UNIFORM_BUFFER, projectionUniformBuffer );
        glBufferSubData( GL_UNIFORM_BUFFER, 0, projData.fillAndFlipBuffer( mat4Buffer ) );
        glBindBuffer( GL_UNIFORM_BUFFER, 0 );

        glViewport( 0, 0, width, height );
    }

    @Override
    protected void update() {
        while ( Mouse.next() ) {
            int eventButton = Mouse.getEventButton();

            if ( eventButton != -1 ) {
                boolean pressed = Mouse.getEventButtonState();
                MousePole.forwardMouseButton( viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY() );
                MousePole.forwardMouseButton( objtPole, eventButton, pressed, Mouse.getX(), Mouse.getY() );
            } else {
                // Mouse moving or mouse scrolling
                int dWheel = Mouse.getDWheel();

                if ( dWheel != 0 ) {
                    MousePole.forwardMouseWheel( viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY() );
                    MousePole.forwardMouseWheel( objtPole, dWheel, dWheel, Mouse.getX(), Mouse.getY() );
                }

                if ( Mouse.isButtonDown( 0 ) || Mouse.isButtonDown( 1 ) || Mouse.isButtonDown( 2 ) ) {
                    MousePole.forwardMouseMotion( viewPole, Mouse.getX(), Mouse.getY() );
                    MousePole.forwardMouseMotion( objtPole, Mouse.getX(), Mouse.getY() );
                }
            }
        }


        while ( Keyboard.next() ) {
            if ( Keyboard.getEventKeyState() ) {
                switch ( Keyboard.getEventKey() ) {
                    case Keyboard.KEY_P:
                        lightTimer.togglePause();
                        break;

                    case Keyboard.KEY_MINUS:
                        lightTimer.rewind( 0.5f );
                        break;

                    case Keyboard.KEY_EQUALS:
                        lightTimer.fastForward( 0.5f );
                        break;

                    case Keyboard.KEY_T:
                        g_bDrawCameraPos = !g_bDrawCameraPos;
                        break;

                    case Keyboard.KEY_G:
                        g_bDrawLights = !g_bDrawLights;
                        break;

                    case Keyboard.KEY_Y:
                        g_bUseInfinity = !g_bUseInfinity;
                        break;

                    case Keyboard.KEY_SPACE:
                        int index = (shaderMode.ordinal() + 1) % ShaderMode.NUM_SHADER_MODES.ordinal();
                        shaderMode = ShaderMode.values()[index];
                        System.out.printf( "%s\n", g_shaderModeNames[shaderMode.ordinal()] );
                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }


                if ( Keyboard.KEY_1 <= Keyboard.getEventKey() && Keyboard.getEventKey() <= Keyboard.KEY_9 ) {
                    int number = Keyboard.getEventKey() - Keyboard.KEY_1;
                    if ( number < NUM_GAUSS_TEXTURES ) {
                        System.out.printf( "Angle Resolution: %d\n", calcCosAngResolution( number ) );
                        g_currTexture = number;
                    }

                    if ( number >= (9 - NUM_MATERIALS) ) {
                        number = number - (9 - NUM_MATERIALS);
                        System.out.printf( "Material number %d\n", number );
                        g_currMaterial = number;
                    }
                }
            }
        }
    }


    ////////////////////////////////
    private float zNear = 1.0f;
    private float zFar = 1000.0f;

    private ProgramData[] programs = new ProgramData[ShaderMode.NUM_SHADER_MODES.ordinal()];
    private UnlitProgData unlit;
    private ShaderPairs[] shaderPairs = new ShaderPairs[]{
            new ShaderPairs( "PN.vert", "FixedShininess.frag" ),
            new ShaderPairs( "PNT.vert", "TextureShininess.frag" ),
            new ShaderPairs( "PNT.vert", "TextureCompute.frag" )
    };

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;
        int normalModelToCameraMatrixUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }

    private class ShaderPairs {
        String vertShaderFileName;
        String fragShaderFileName;

        ShaderPairs(String vertShader, String fragShader) {
            this.vertShaderFileName = vertShader;
            this.fragShaderFileName = fragShader;
        }
    }


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer( Vec4.SIZE );
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer( Mat3.SIZE );
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );
    private FloatBuffer lightBlockBuffer = BufferUtils.createFloatBuffer( LightBlock.SIZE );


    private void initializePrograms() {
        for ( int progIndex = 0; progIndex < ShaderMode.NUM_SHADER_MODES.ordinal(); progIndex++ ) {
            programs[progIndex] = loadStandardProgram( shaderPairs[progIndex].vertShaderFileName,
                    shaderPairs[progIndex].fragShaderFileName );
        }

        unlit = loadUnlitProgram( "Unlit.vert", "Unlit.frag" );
    }

    private ProgramData loadStandardProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, vertexShaderFileName ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, fragmentShaderFileName ) );

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram( shaderList );
        data.modelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "modelToCameraMatrix" );
        data.normalModelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "normalModelToCameraMatrix" );

        int materialBlock = glGetUniformBlockIndex( data.theProgram, "Material" );
        int lightBlock = glGetUniformBlockIndex( data.theProgram, "Light" );
        int projectionBlock = glGetUniformBlockIndex( data.theProgram, "Projection" );

        glUniformBlockBinding( data.theProgram, materialBlock, materialBlockIndex );
        glUniformBlockBinding( data.theProgram, lightBlock, lightBlockIndex );
        glUniformBlockBinding( data.theProgram, projectionBlock, projectionBlockIndex );

        int gaussianTextureUnif = glGetUniformLocation( data.theProgram, "gaussianTexture" );
        int shininessTextureUnif = glGetUniformLocation( data.theProgram, "shininessTexture" );
        glUseProgram( data.theProgram );
        glUniform1i( gaussianTextureUnif, gaussTexUnit );
        glUniform1i( shininessTextureUnif, shineTexUnit );
        glUseProgram( 0 );

        return data;
    }

    private UnlitProgData loadUnlitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, vertexShaderFileName ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, fragmentShaderFileName ) );

        UnlitProgData data = new UnlitProgData();
        data.theProgram = Framework.createProgram( shaderList );
        data.modelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "modelToCameraMatrix" );
        data.objectColorUnif = glGetUniformLocation( data.theProgram, "objectColor" );

        int projectionBlock = glGetUniformBlockIndex( data.theProgram, "Projection" );
        glUniformBlockBinding( data.theProgram, projectionBlock, projectionBlockIndex );

        return data;
    }


    ////////////////////////////////
    private final int NUM_GAUSS_TEXTURES = 4;
    private final int NUM_MATERIALS = 2;
    private final int gaussTexUnit = 0;
    private final int shineTexUnit = 1;
    private final float halfLightDistance = 25.0f;
    private final float lightAttenuation = 1.0f / (halfLightDistance * halfLightDistance);

    private final String[] g_shaderModeNames = {
            "Fixed Shininess with Gaussian Texture",
            "Texture Shininess with Gaussian Texture",
            "Texture Shininess with computed Gaussian"
    };

    private Mesh objectMesh;
    private Mesh cubeMesh;
    private Mesh planeMesh;

    private Timer lightTimer = new Timer( Timer.Type.LOOP, 6.0f );

    private enum ShaderMode {
        FIXED,
        TEXTURED,
        TEXTURED_COMPUTE,

        NUM_SHADER_MODES
    }

    private ShaderMode shaderMode = ShaderMode.FIXED;

    private boolean g_bDrawLights = true;
    private boolean g_bUseInfinity = true;
    private boolean g_bDrawCameraPos;
    private int g_gaussTextures[] = new int[NUM_GAUSS_TEXTURES];
    private int materialOffset;
    private int g_textureSampler;
    private int g_shineTexture;
    private int g_currTexture = NUM_GAUSS_TEXTURES - 1;
    private int g_currMaterial;
    private float g_lightHeight = 1.0f;
    private float g_lightRadius = 3.0f;


    private void createGaussianTextures() {
        for ( int textureIndex = 0; textureIndex < NUM_GAUSS_TEXTURES; textureIndex++ ) {
            int cosAngleResolution = calcCosAngResolution( textureIndex );
            g_gaussTextures[textureIndex] = createGaussianTexture( cosAngleResolution, 128 );
        }

        g_textureSampler = glGenSamplers();
        glSamplerParameteri( g_textureSampler, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        glSamplerParameteri( g_textureSampler, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        glSamplerParameteri( g_textureSampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glSamplerParameteri( g_textureSampler, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
    }

    private int createGaussianTexture(int cosAngleResolution, int shininessResolution) {
        byte[] textureData = new byte[shininessResolution * cosAngleResolution];

        buildGaussianData( textureData, cosAngleResolution, shininessResolution );

        ByteBuffer textureDataBuffer = BufferUtils.createByteBuffer( textureData.length );
        textureDataBuffer.put( textureData );
        textureDataBuffer.flip();

        int gaussTexture = glGenTextures();
        glBindTexture( GL_TEXTURE_2D, gaussTexture );
        glTexImage2D( GL_TEXTURE_2D, 0, GL_R8, cosAngleResolution, shininessResolution,
                0, GL11.GL_RED, GL_UNSIGNED_BYTE, textureDataBuffer );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0 );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0 );
        glBindTexture( GL_TEXTURE_2D, 0 );

        return gaussTexture;
    }

    private void buildGaussianData(byte[] textureData, int cosAngleResolution, int shininessResolution) {
        int offset = 0;

        for ( int shinIndex = 1; shinIndex <= shininessResolution; shinIndex++ ) {
            float shininess = shinIndex / (float) (shininessResolution);

            for ( int cosAngIndex = 0; cosAngIndex < cosAngleResolution; cosAngIndex++ ) {
                float cosAng = cosAngIndex / (float) (cosAngleResolution - 1);
                float angle = (float) Math.acos( cosAng );
                float exponent = angle / shininess;
                exponent = -(exponent * exponent);
                float gaussianTerm = (float) Math.exp( exponent );

                textureData[offset] = (byte) (char) (gaussianTerm * 255.0f);
                offset++;
            }
        }
    }

    private int calcCosAngResolution(int level) {
        final int cosAngleStart = 64;

        return cosAngleStart * (int) (Math.pow( 2.0f, level ));
    }

    private void createShininessTexture() {
        try {
            String filepath = Framework.findFileOrThrow( "main.dds" );
            ImageSet imageSet = DdsLoader.loadFromFile( filepath );

            SingleImage image = imageSet.getImage( 0, 0, 0 );
            Dimensions dims = image.getDimensions();

            g_shineTexture = glGenTextures();
            glBindTexture( GL_TEXTURE_2D, g_shineTexture );
            glTexImage2D( GL_TEXTURE_2D, 0, GL_R8, dims.width, dims.height, 0,
                    GL11.GL_RED, GL_UNSIGNED_BYTE, image.getImageData() );
            glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0 );
            glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0 );
            glBindTexture( GL_TEXTURE_2D, 0 );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    private Vec4 calcLightPosition() {
        final float scale = 3.14159f * 2.0f;

        float timeThroughLoop = lightTimer.getAlpha();

        Vec4 ret = new Vec4( 0.0f, g_lightHeight, 0.0f, 1.0f );

        ret.x = (float) (Math.cos( timeThroughLoop * scale ) * g_lightRadius);
        ret.z = (float) (Math.sin( timeThroughLoop * scale ) * g_lightRadius);

        return ret;
    }


    ////////////////////////////////
    // View / Object setup.
    private ObjectData initialObjectData = new ObjectData(
            new Vec3( 0.0f, 0.5f, 0.0f ),
            new Quaternion( 1.0f, 0.0f, 0.0f, 0.0f )
    );


    private ViewData initialViewData = new ViewData(
            new Vec3( initialObjectData.position ),
            new Quaternion( 0.92387953f, 0.3826834f, 0.0f, 0.0f ),
            10.0f,
            0.0f
    );

    private ViewScale viewScale = new ViewScale(
            1.5f, 70.0f,
            1.5f, 0.5f,
            0.0f, 0.0f,     // No camera movement.
            90.0f / 250.0f
    );


    private ViewPole viewPole = new ViewPole( initialViewData, viewScale, MouseButtons.MB_LEFT_BTN );
    private ObjectPole objtPole = new ObjectPole( initialObjectData, 90.0f / 250.0f, MouseButtons.MB_RIGHT_BTN, viewPole );


    ////////////////////////////////
    private final int projectionBlockIndex = 2;

    private int projectionUniformBuffer;

    private class ProjectionBlock extends BufferableData<FloatBuffer> {
        Mat4 cameraToClipMatrix;

        static final int SIZE = Mat4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            return cameraToClipMatrix.fillBuffer( buffer );
        }
    }


    ////////////////////////////////
    private static final int NUMBER_OF_LIGHTS = 2;

    private final int lightBlockIndex = 1;

    private int lightUniformBuffer;

    class PerLight extends BufferableData<FloatBuffer> {
        Vec4 cameraSpaceLightPos;
        Vec4 lightIntensity;

        static final int SIZE = Vec4.SIZE + Vec4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            cameraSpaceLightPos.fillBuffer( buffer );
            lightIntensity.fillBuffer( buffer );

            return buffer;
        }
    }

    class LightBlock extends BufferableData<FloatBuffer> {
        Vec4 ambientIntensity;
        float lightAttenuation;
        float padding[] = new float[3];
        PerLight lights[] = new PerLight[NUMBER_OF_LIGHTS];

        static final int SIZE = Vec4.SIZE + ((1 + 3) * FLOAT_SIZE) + PerLight.SIZE * NUMBER_OF_LIGHTS;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            ambientIntensity.fillBuffer( buffer );
            buffer.put( lightAttenuation );
            buffer.put( padding );

            for ( PerLight light : lights ) {
                light.fillBuffer( buffer );
            }

            return buffer;
        }
    }


    ////////////////////////////////
    private final int materialBlockIndex = 0;

    private int materialUniformBuffer;

    private class MaterialBlock extends BufferableData<ByteBuffer> {
        Vec4 diffuseColor;
        Vec4 specularColor;
        float specularShininess;
        float padding[] = new float[3];

        static final int SIZE = Vec4.SIZE + Vec4.SIZE + ((1 + 3) * FLOAT_SIZE);

        @Override
        public ByteBuffer fillBuffer(ByteBuffer buffer) {
            buffer.putFloat( diffuseColor.x );
            buffer.putFloat( diffuseColor.y );
            buffer.putFloat( diffuseColor.z );
            buffer.putFloat( diffuseColor.w );

            buffer.putFloat( specularColor.x );
            buffer.putFloat( specularColor.y );
            buffer.putFloat( specularColor.z );
            buffer.putFloat( specularColor.w );

            buffer.putFloat( specularShininess );

            for ( int i = 0; i < 3; i++ ) {
                buffer.putFloat( padding[i] );
            }

            return buffer;
        }
    }


    private void setupMaterials() {
        UniformBlockArray<MaterialBlock> ubArray = new UniformBlockArray<>( MaterialBlock.SIZE, NUM_MATERIALS );
        MaterialBlock matBlock;

        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vec4( 1.0f, 0.673f, 0.043f, 1.0f );
        matBlock.specularColor = new Vec4( 1.0f, 0.673f, 0.043f, 1.0f ).scale( 0.4f );
        matBlock.specularShininess = 0.125f;
        ubArray.set( 0, matBlock );

        matBlock = new MaterialBlock();
        matBlock.diffuseColor = new Vec4( 0.01f, 0.01f, 0.01f, 1.0f );
        matBlock.specularColor = new Vec4( 0.99f, 0.99f, 0.99f, 1.0f );
        matBlock.specularShininess = 0.125f;
        ubArray.set( 1, matBlock );

        materialUniformBuffer = ubArray.createBufferObject();
        materialOffset = ubArray.getArrayOffset();
    }
}