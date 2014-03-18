package fcagnin.jgltut.tut17;

import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glimg.DdsLoader;
import fcagnin.jglsdk.glimg.ImageSet;
import fcagnin.jglsdk.glimg.TextureGenerator;
import fcagnin.jglsdk.glm.*;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.MouseButtons;
import fcagnin.jglsdk.glutil.MousePoles.ViewData;
import fcagnin.jglsdk.glutil.MousePoles.ViewPole;
import fcagnin.jglsdk.glutil.MousePoles.ViewScale;
import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.*;
import fcagnin.jgltut.framework.Scene.SceneNode;
import fcagnin.jgltut.framework.SceneBinders.UniformIntBinder;
import fcagnin.jgltut.framework.SceneBinders.UniformMat4Binder;
import fcagnin.jgltut.framework.SceneBinders.UniformVec3Binder;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part IV. Texturing
 * Chapter 17. Spotlight on Textures
 * http://www.arcsynthesis.org/gltut/Texturing/Tutorial%2017.html
 * <p/>
 * W,A,S,D  - move the camera forward/backwards and left/right, relative to the camera's current orientation. Holding SHIFT with these
 * keys will move in smaller increments.
 * I,J,K,L  - control the projected flashlight's position. Holding SHIFT with these keys will move in smaller increments.
 * O,U       raise and lower the projected flashlight, relative to its current orientation. Holding SHIFT with these keys will move in
 * smaller increments.
 * SPACE    - reset the projected flashlight direction.
 * T        - toggle viewing of the current target point.
 * G        - toggle all of the regular lighting on and off.
 * H        - toggle between the edge clamping sampler and the border clamping one.
 * P        - toggle pausing.
 * Y        - increase the FOV.
 * N        - decrease the FOV.
 * 1,2,3    - toggle between different light textures.
 * <p/>
 * LEFT   CLICKING and DRAGGING         - rotate the camera around the target point, both horizontally and vertically.
 * LEFT   CLICKING and DRAGGING + CTRL  - rotate the camera around the target point, either horizontally or vertically.
 * LEFT   CLICKING and DRAGGING + ALT   - change the camera's up direction.
 * RIGHT  CLICKING and DRAGGING         - rotate the projected flashlight horizontally and vertically, relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + CTRL  - rotate the projected flashlight horizontally or vertically only,
 * relative to the current camera view.
 * RIGHT  CLICKING and DRAGGING + ALT   - spin the projected flashlight.
 * WHEEL  SCROLLING                     - move the camera closer to it's target point or farther away.
 *
 * @author integeruser
 */
public class ProjectedLight extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut17/data/";

        new ProjectedLight().start( displayWidth, displayHeight );
    }


    @Override
    protected void init() {
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
        glEnable( GL_FRAMEBUFFER_SRGB );

        // Setup our Uniform Buffers
        g_projectionUniformBuffer = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, g_projectionUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_STREAM_DRAW );

        glBindBufferRange( GL_UNIFORM_BUFFER, g_projectionBlockIndex, g_projectionUniformBuffer,
                0, ProjectionBlock.SIZE );

        createSamplers();
        loadTextures();

        try {
            loadAndSetupScene();
        } catch ( Exception exception ) {
            exception.printStackTrace();
            System.exit( -1 );
        }

        lightUniformBuffer = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, lightUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_STREAM_DRAW );

        glBindBufferRange( GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer,
                0, LightBlock.SIZE );

        glBindBuffer( GL_UNIFORM_BUFFER, 0 );
    }

    @Override
    protected void display() {
        timer.update( getElapsedTime() );

        glClearColor( 0.8f, 0.8f, 0.8f, 1.0f );
        glClearDepth( 1.0f );
        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        final Mat4 cameraMatrix = viewPole.calcMatrix();
        final Mat4 lightView = lightViewPole.calcMatrix();

        MatrixStack modelMatrix = new MatrixStack();
        modelMatrix.applyMatrix( cameraMatrix );

        buildLights( cameraMatrix );

        nodes.get( 0 ).nodeSetOrient( Glm.rotate( new Quaternion( 1.0f ),
                360.0f * timer.getAlpha(), new Vec3( 0.0f, 1.0f, 0.0f ) ) );

        nodes.get( 3 ).nodeSetOrient( Quaternion.mul( spinBarOrient, Glm.rotate( new Quaternion( 1.0f ),
                360.0f * timer.getAlpha(), new Vec3( 0.0f, 0.0f, 1.0f ) ) ) );

        {
            MatrixStack persMatrix = new MatrixStack();
            persMatrix.perspective( 60.0f, displayWidth / (float) displayHeight, zNear, zFar );

            ProjectionBlock projData = new ProjectionBlock();
            projData.cameraToClipMatrix = persMatrix.top();

            glBindBuffer( GL_UNIFORM_BUFFER, g_projectionUniformBuffer );
            glBufferData( GL_UNIFORM_BUFFER, projData.fillAndFlipBuffer( mat4Buffer ), GL_STREAM_DRAW );
            glBindBuffer( GL_UNIFORM_BUFFER, 0 );
        }

        glActiveTexture( GL_TEXTURE0 + lightProjTexUnit );
        glBindTexture( GL_TEXTURE_2D, lightTextures[currTextureIndex] );
        glBindSampler( lightProjTexUnit, samplers[currSampler] );

        {
            MatrixStack lightProjStack = new MatrixStack();
            // Texture-space transform
            lightProjStack.translate( 0.5f, 0.5f, 0.0f );
            lightProjStack.scale( 0.5f, 0.5f, 1.0f );

            // Project. Z-range is irrelevant.
            lightProjStack.perspective( lightFOVs[currFOVIndex], 1.0f, 1.0f, 100.0f );

            // Transform from main camera space to light camera space.
            lightProjStack.applyMatrix( lightView );
            lightProjStack.applyMatrix( Glm.inverse( cameraMatrix ) );

            lightProjMatBinder.setValue( lightProjStack.top() );

            Vec4 worldLightPos = Glm.inverse( lightView ).getColumn( 3 );
            Vec3 lightPos = new Vec3( Mat4.mul( cameraMatrix, worldLightPos ) );

            camLightPosBinder.setValue( lightPos );
        }

        glViewport( 0, 0, displayWidth, displayHeight );
        scene.render( modelMatrix.top() );

        {
            // Draw axes
            modelMatrix.push();

            modelMatrix.applyMatrix( Glm.inverse( lightView ) );
            modelMatrix.scale( 15.0f );
            modelMatrix.scale( 1.0f, 1.0f, -1.0f );                // Invert the Z-axis so that it points in the right direction.

            glUseProgram( coloredProg );
            glUniformMatrix4( coloredModelToCameraMatrixUnif, false,
                    modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            axesMesh.render();

            modelMatrix.pop();
        }

        if ( drawCameraPos ) {
            modelMatrix.push();

            // Draw lookat point.
            modelMatrix.setIdentity();
            modelMatrix.translate( 0.0f, 0.0f, -viewPole.getView().radius );
            modelMatrix.scale( 0.5f );

            glDisable( GL_DEPTH_TEST );
            glDepthMask( false );
            glUseProgram( unlitProg );
            glUniformMatrix4( unlitModelToCameraMatrixUnif, false,
                    modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( unlitObjectColorUnif, 0.25f, 0.25f, 0.25f, 1.0f );
            sphereMesh.render( "flat" );
            glDepthMask( true );
            glEnable( GL_DEPTH_TEST );
            glUniform4f( unlitObjectColorUnif, 1.0f, 1.0f, 1.0f, 1.0f );
            sphereMesh.render( "flat" );

            modelMatrix.pop();
        }

        glActiveTexture( GL_TEXTURE0 + lightProjTexUnit );
        glBindTexture( GL_TEXTURE_2D, 0 );
        glBindSampler( lightProjTexUnit, 0 );
    }

    @Override
    protected void reshape(int width, int height) {
        displayWidth = width;
        displayHeight = height;
    }

    @Override
    protected void update() {
        while ( Mouse.next() ) {
            int eventButton = Mouse.getEventButton();


            if ( eventButton != -1 ) {
                boolean pressed = Mouse.getEventButtonState();
                MousePole.forwardMouseButton( viewPole, eventButton, pressed, Mouse.getX(), Mouse.getY() );
                MousePole.forwardMouseButton( lightViewPole, eventButton, pressed, Mouse.getX(), Mouse.getY() );
            } else {
                // Mouse moving or mouse scrolling
                int dWheel = Mouse.getDWheel();

                if ( dWheel != 0 ) {
                    MousePole.forwardMouseWheel( viewPole, dWheel, dWheel, Mouse.getX(), Mouse.getY() );
                }

                if ( Mouse.isButtonDown( 0 ) || Mouse.isButtonDown( 1 ) || Mouse.isButtonDown( 2 ) ) {
                    MousePole.forwardMouseMotion( viewPole, Mouse.getX(), Mouse.getY() );
                    MousePole.forwardMouseMotion( lightViewPole, Mouse.getX(), Mouse.getY() );
                }
            }
        }


        float lastFrameDuration = (float) (getLastFrameDuration() / 100.0);

        if ( Keyboard.isKeyDown( Keyboard.KEY_W ) ) {
            viewPole.charPress( Keyboard.KEY_W, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ),
                    lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_S ) ) {
            viewPole.charPress( Keyboard.KEY_S, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ),
                    lastFrameDuration );
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_D ) ) {
            viewPole.charPress( Keyboard.KEY_D, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ),
                    lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_A ) ) {
            viewPole.charPress( Keyboard.KEY_A, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ),
                    lastFrameDuration );
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_E ) ) {
            viewPole.charPress( Keyboard.KEY_E, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ),
                    lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_Q ) ) {
            viewPole.charPress( Keyboard.KEY_Q, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ),
                    lastFrameDuration );
        }


        if ( Keyboard.isKeyDown( Keyboard.KEY_I ) ) {
            lightViewPole.charPress( Keyboard.KEY_I, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT
            ), lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_K ) ) {
            lightViewPole.charPress( Keyboard.KEY_K, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT
            ), lastFrameDuration );
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_L ) ) {
            lightViewPole.charPress( Keyboard.KEY_L, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT
            ), lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_J ) ) {
            lightViewPole.charPress( Keyboard.KEY_J, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT
            ), lastFrameDuration );
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_O ) ) {
            lightViewPole.charPress( Keyboard.KEY_O, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT
            ), lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_U ) ) {
            lightViewPole.charPress( Keyboard.KEY_U, Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT
            ), lastFrameDuration );
        }


        while ( Keyboard.next() ) {
            if ( Keyboard.getEventKeyState() ) {
                switch ( Keyboard.getEventKey() ) {
                    case Keyboard.KEY_SPACE:
                        lightViewPole.reset();
                        break;

                    case Keyboard.KEY_T:
                        drawCameraPos = !drawCameraPos;
                        break;

                    case Keyboard.KEY_G:
                        showOtherLights = !showOtherLights;
                        break;

                    case Keyboard.KEY_H:
                        currSampler = (currSampler + 1) % NUM_SAMPLERS;
                        break;

                    case Keyboard.KEY_P:
                        timer.togglePause();
                        break;

                    case Keyboard.KEY_Y:
                        currFOVIndex = Math.min( currFOVIndex + 1, lightFOVs.length - 1 );
                        System.out.printf( "Curr FOV: %f\n", lightFOVs[currFOVIndex] );
                        break;

                    case Keyboard.KEY_N:
                        currFOVIndex = Math.max( currFOVIndex - 1, 0 );
                        System.out.printf( "Curr FOV: %f\n", lightFOVs[currFOVIndex] );
                        break;

                    case Keyboard.KEY_1:
                    case Keyboard.KEY_2:
                    case Keyboard.KEY_3:
                        int number = Keyboard.getEventKey() - Keyboard.KEY_1;
                        currTextureIndex = number;
                        System.out.printf( "%s\n", texDefs[currTextureIndex].name );
                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }
            }
        }
    }


    ////////////////////////////////
    private final float zNear = 1.0f;
    private final float zFar = 1000.0f;

    private int coloredModelToCameraMatrixUnif;
    private int coloredProg;

    private int unlitProg;
    private int unlitModelToCameraMatrixUnif;
    private int unlitObjectColorUnif;


    private final FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );
    private final FloatBuffer lightBlockBuffer = BufferUtils.createFloatBuffer( LightBlock.SIZE );


    private void loadAndSetupScene() {
        scene = new Scene( "proj2d_scene.xml" );

        nodes = new ArrayList<>();
        nodes.add( scene.findNode( "cube" ) );
        nodes.add( scene.findNode( "rightBar" ) );
        nodes.add( scene.findNode( "leaningBar" ) );
        nodes.add( scene.findNode( "spinBar" ) );
        nodes.add( scene.findNode( "diorama" ) );
        nodes.add( scene.findNode( "floor" ) );

        lightNumBinder = new UniformIntBinder();
        SceneBinders.associateUniformWithNodes( nodes, lightNumBinder, "numberOfLights" );
        SceneBinders.setStateBinderWithNodes( nodes, lightNumBinder );

        lightProjMatBinder = new UniformMat4Binder();
        SceneBinders.associateUniformWithNodes( nodes, lightProjMatBinder, "cameraToLightProjMatrix" );
        SceneBinders.setStateBinderWithNodes( nodes, lightProjMatBinder );

        camLightPosBinder = new UniformVec3Binder();
        SceneBinders.associateUniformWithNodes( nodes, camLightPosBinder, "cameraSpaceProjLightPos" );
        SceneBinders.setStateBinderWithNodes( nodes, camLightPosBinder );

        int unlit = scene.findProgram( "p_unlit" );
        sphereMesh = scene.findMesh( "m_sphere" );

        int colored = scene.findProgram( "p_colored" );
        axesMesh = scene.findMesh( "m_axes" );

        // No more things that can throw.
        spinBarOrient = nodes.get( 3 ).nodeGetOrient();
        unlitProg = unlit;
        unlitModelToCameraMatrixUnif = glGetUniformLocation( unlit, "modelToCameraMatrix" );
        unlitObjectColorUnif = glGetUniformLocation( unlit, "objectColor" );

        coloredProg = colored;
        coloredModelToCameraMatrixUnif = glGetUniformLocation( colored, "modelToCameraMatrix" );
    }


    ////////////////////////////////
    private static int displayWidth = 500;
    private static int displayHeight = 500;

    private Scene scene;
    private ArrayList<SceneNode> nodes;
    private Timer timer = new Timer( Timer.Type.LOOP, 10.0f );

    private UniformMat4Binder lightProjMatBinder;
    private UniformVec3Binder camLightPosBinder;

    private Quaternion spinBarOrient;

    private boolean showOtherLights = true;

    private Mesh sphereMesh, axesMesh;
    private boolean drawCameraPos;

    private float[] lightFOVs = {
            10.0f, 20.0f, 45.0f, 75.0f,
            90.0f, 120.0f, 150.0f, 170.0f
    };
    private int currFOVIndex = 3;


    ////////////////////////////////
    // View setup.
    private ViewData initialView = new ViewData(
            new Vec3( 0.0f, 0.0f, 10.0f ),
            new Quaternion( 0.909845f, 0.16043f, -0.376867f, -0.0664516f ),
            25.0f,
            0.0f
    );

    private ViewScale initialViewScale = new ViewScale(
            5.0f, 70.0f,
            2.0f, 0.5f,
            2.0f, 0.5f,
            90.0f / 250.0f
    );


    private ViewData initLightView = new ViewData(
            new Vec3( 0.0f, 0.0f, 20.0f ),
            new Quaternion( 1.0f, 0.0f, 0.0f, 0.0f ),
            5.0f,
            0.0f
    );

    private ViewScale initLightViewScale = new ViewScale(
            0.05f, 10.0f,
            0.1f, 0.05f,
            4.0f, 1.0f,
            90.0f / 250.0f
    );


    private ViewPole viewPole = new ViewPole( initialView, initialViewScale, MouseButtons.MB_LEFT_BTN );
    private ViewPole lightViewPole = new ViewPole( initLightView, initLightViewScale, MouseButtons.MB_RIGHT_BTN, true );


    ////////////////////////////////
    private final TexDef[] texDefs = {
            new TexDef( "Flashlight.dds", "Flashlight" ),
            new TexDef( "PointsOfLight.dds", "Multiple Point Lights" ),
            new TexDef( "Bands.dds", "Light Bands" )
    };
    private final int NUM_LIGHT_TEXTURES = texDefs.length;

    private int[] lightTextures = new int[texDefs.length];
    private int currTextureIndex = 0;

    private class TexDef {
        String filename;
        String name;

        TexDef(String filename, String name) {
            this.filename = filename;
            this.name = name;
        }
    }


    private void loadTextures() {
        try {
            for ( int textureIndex = 0; textureIndex < NUM_LIGHT_TEXTURES; textureIndex++ ) {
                String filepath = Framework.findFileOrThrow( texDefs[textureIndex].filename );
                ImageSet imageSet = DdsLoader.loadFromFile( filepath );

                lightTextures[textureIndex] = TextureGenerator.createTexture( imageSet, 0 );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    ////////////////////////////////
    private final int NUM_SAMPLERS = 2;

    private int[] samplers = new int[NUM_SAMPLERS];
    private int currSampler = 0;


    private void createSamplers() {
        for ( int samplerIndex = 0; samplerIndex < NUM_SAMPLERS; samplerIndex++ ) {
            samplers[samplerIndex] = glGenSamplers();
            glSamplerParameteri( samplers[samplerIndex], GL_TEXTURE_MAG_FILTER, GL_LINEAR );
            glSamplerParameteri( samplers[samplerIndex], GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        }

        glSamplerParameteri( samplers[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        glSamplerParameteri( samplers[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );

        glSamplerParameteri( samplers[1], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
        glSamplerParameteri( samplers[1], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );


        float[] color = {0.0f, 0.0f, 0.0f, 1.0f};
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer( 4 );

        for ( float f : color ) {
            colorBuffer.put( f );
        }

        colorBuffer.flip();

        glSamplerParameter( samplers[1], GL_TEXTURE_BORDER_COLOR, colorBuffer );
    }


    ////////////////////////////////
    private final int g_projectionBlockIndex = 0;

    private int g_projectionUniformBuffer;

    private class ProjectionBlock extends BufferableData<FloatBuffer> {
        Mat4 cameraToClipMatrix;

        static final int SIZE = Mat4.SIZE;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            return cameraToClipMatrix.fillBuffer( buffer );
        }
    }


    ////////////////////////////////
    private static final int MAX_NUMBER_OF_LIGHTS = 4;

    private final int lightBlockIndex = 1;
    private final int lightProjTexUnit = 3;

    private int lightUniformBuffer;
    private UniformIntBinder lightNumBinder;

    private class PerLight extends BufferableData<FloatBuffer> {
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

    private class LightBlock extends BufferableData<FloatBuffer> {
        Vec4 ambientIntensity;
        float lightAttenuation;
        float maxIntensity;
        float padding[] = new float[2];
        PerLight lights[] = new PerLight[MAX_NUMBER_OF_LIGHTS];

        static final int SIZE = Vec4.SIZE + ((1 + 1 + 2) * FLOAT_SIZE) + PerLight.SIZE * MAX_NUMBER_OF_LIGHTS;

        @Override
        public FloatBuffer fillBuffer(FloatBuffer buffer) {
            ambientIntensity.fillBuffer( buffer );
            buffer.put( lightAttenuation );
            buffer.put( maxIntensity );
            buffer.put( padding );

            for ( PerLight light : lights ) {
                if ( light == null ) { break; }

                light.fillBuffer( buffer );
            }

            return buffer;
        }
    }


    private void buildLights(Mat4 camMatrix) {
        LightBlock lightData = new LightBlock();
        lightData.ambientIntensity = new Vec4( 0.2f, 0.2f, 0.2f, 1.0f );
        lightData.lightAttenuation = 1.0f / (30.0f * 30.0f);
        lightData.maxIntensity = 2.0f;

        lightData.lights[0] = new PerLight();
        lightData.lights[0].lightIntensity = new Vec4( 0.2f, 0.2f, 0.2f, 1.0f );
        lightData.lights[0].cameraSpaceLightPos = Mat4.mul( camMatrix,
                Glm.normalize( new Vec4( -0.2f, 0.5f, 0.5f, 0.0f ) ) );

        lightData.lights[1] = new PerLight();
        lightData.lights[1].lightIntensity = new Vec4( 3.5f, 6.5f, 3.0f, 1.0f ).scale( 0.5f );
        lightData.lights[1].cameraSpaceLightPos = Mat4.mul( camMatrix,
                new Vec4( 5.0f, 6.0f, 0.5f, 1.0f ) );

        if ( showOtherLights ) {
            lightNumBinder.setValue( 2 );
        } else {
            lightNumBinder.setValue( 0 );
        }

        glBindBuffer( GL_UNIFORM_BUFFER, lightUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, lightData.fillAndFlipBuffer( lightBlockBuffer ), GL_STREAM_DRAW );
    }
}