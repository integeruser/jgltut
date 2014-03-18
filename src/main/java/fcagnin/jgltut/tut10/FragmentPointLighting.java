package fcagnin.jgltut.tut10;

import fcagnin.jglsdk.BufferableData;
import fcagnin.jglsdk.glm.*;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jglsdk.glutil.MousePoles.*;
import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.Framework;
import fcagnin.jgltut.framework.Mesh;
import fcagnin.jgltut.framework.MousePole;
import fcagnin.jgltut.framework.Timer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part III. Illumination
 * Chapter 10. Plane Lights
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2010.html
 * <p/>
 * I,J,K,L  - control the light's position. Holding LEFT_SHIFT with these keys will move in smaller increments.
 * SPACE    - toggle between drawing the uncolored cylinder and the colored one.
 * Y        - toggle the drawing of the light source.
 * T        - toggle between the scaled and unscaled cylinder.
 * H        - toggle between per-fragment lighting and per-vertex lighting.
 * B        - toggle the light's rotation on/off.
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
public class FragmentPointLighting extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut10/data/";

        new FragmentPointLighting().start();
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            cylinderMesh = new Mesh( "UnitCylinder.xml" );
            planeMesh = new Mesh( "LargePlane.xml" );
            cubeMesh = new Mesh( "UnitCube.xml" );
        } catch ( Exception exception ) {
            exception.printStackTrace();
            System.exit( -1 );
        }

        glEnable( GL_CULL_FACE );
        glCullFace( GL_BACK );
        glFrontFace( GL_CW );

        glEnable( GL_DEPTH_TEST );
        glDepthMask( true );
        glDepthFunc( GL_LEQUAL );
        glDepthRange( 0.0f, 1.0f );
        glEnable( GL_DEPTH_CLAMP );

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, projectionUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW );

        // Bind the static buffers.
        glBindBufferRange( GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer,
                0, ProjectionBlock.SIZE );

        glBindBuffer( GL_UNIFORM_BUFFER, 0 );
    }

    @Override
    protected void display() {
        lightTimer.update( getElapsedTime() );

        glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glClearDepth( 1.0f );
        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        MatrixStack modelMatrix = new MatrixStack();
        modelMatrix.setMatrix( viewPole.calcMatrix() );

        final Vec4 worldLightPos = calcLightPosition();

        Vec4 lightPosCameraSpace = Mat4.mul( modelMatrix.top(), worldLightPos );

        ProgramData whiteProgram;
        ProgramData vertColorProgram;

        if ( useFragmentLighting ) {
            whiteProgram = fragWhiteDiffuseColor;
            vertColorProgram = fragVertexDiffuseColor;
        } else {
            whiteProgram = whiteDiffuseColor;
            vertColorProgram = vertexDiffuseColor;
        }

        glUseProgram( whiteProgram.theProgram );
        glUniform4f( whiteProgram.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f );
        glUniform4f( whiteProgram.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f );
        glUseProgram( vertColorProgram.theProgram );
        glUniform4f( vertColorProgram.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f );
        glUniform4f( vertColorProgram.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f );
        glUseProgram( 0 );

        {
            modelMatrix.push();

            // Render the ground plane.
            {
                modelMatrix.push();

                glUseProgram( whiteProgram.theProgram );
                glUniformMatrix4( whiteProgram.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

                Mat4 invTransform = Glm.inverse( modelMatrix.top() );
                Vec4 lightPosModelSpace = Mat4.mul( invTransform, lightPosCameraSpace );
                glUniform3( whiteProgram.modelSpaceLightPosUnif, lightPosModelSpace.fillAndFlipBuffer( vec4Buffer ) );

                planeMesh.render();
                glUseProgram( 0 );

                modelMatrix.pop();
            }

            // Render the Cylinder
            {
                modelMatrix.push();

                modelMatrix.applyMatrix( objtPole.calcMatrix() );

                if ( scaleCyl ) {
                    modelMatrix.scale( 1.0f, 1.0f, 0.2f );
                }

                Mat4 invTransform = Glm.inverse( modelMatrix.top() );
                Vec4 lightPosModelSpace = Mat4.mul( invTransform, lightPosCameraSpace );

                if ( drawColoredCyl ) {
                    glUseProgram( vertColorProgram.theProgram );
                    glUniformMatrix4( vertColorProgram.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

                    glUniform3( vertColorProgram.modelSpaceLightPosUnif, lightPosModelSpace.fillAndFlipBuffer( vec4Buffer ) );

                    cylinderMesh.render( "lit-color" );
                } else {
                    glUseProgram( whiteProgram.theProgram );
                    glUniformMatrix4( whiteProgram.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

                    glUniform3( whiteProgram.modelSpaceLightPosUnif, lightPosModelSpace.fillAndFlipBuffer( vec4Buffer ) );

                    cylinderMesh.render( "lit" );
                }
                glUseProgram( 0 );

                modelMatrix.pop();
            }

            // Render the light
            if ( drawLight ) {
                modelMatrix.push();

                modelMatrix.translate( worldLightPos.x, worldLightPos.y, worldLightPos.z );
                modelMatrix.scale( 0.1f, 0.1f, 0.1f );

                glUseProgram( unlit.theProgram );
                glUniformMatrix4( unlit.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
                glUniform4f( unlit.objectColorUnif, 0.8078f, 0.8706f, 0.9922f, 1.0f );
                cubeMesh.render( "flat" );

                modelMatrix.pop();
            }

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


        float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

        if ( Keyboard.isKeyDown( Keyboard.KEY_J ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                lightRadius -= 0.05f * lastFrameDuration;
            } else {
                lightRadius -= 0.2f * lastFrameDuration;
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_L ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                lightRadius += 0.05f * lastFrameDuration;
            } else {
                lightRadius += 0.2f * lastFrameDuration;
            }
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_I ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                lightHeight += 0.05f * lastFrameDuration;
            } else {
                lightHeight += 0.2f * lastFrameDuration;
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_K ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                lightHeight -= 0.05f * lastFrameDuration;
            } else {
                lightHeight -= 0.2f * lastFrameDuration;
            }
        }


        if ( lightRadius < 0.2f ) {
            lightRadius = 0.2f;
        }


        while ( Keyboard.next() ) {
            if ( Keyboard.getEventKeyState() ) {
                switch ( Keyboard.getEventKey() ) {
                    case Keyboard.KEY_SPACE:
                        drawColoredCyl = !drawColoredCyl;
                        break;

                    case Keyboard.KEY_Y:
                        drawLight = !drawLight;
                        break;

                    case Keyboard.KEY_T:
                        scaleCyl = !scaleCyl;
                        break;

                    case Keyboard.KEY_H:
                        useFragmentLighting = !useFragmentLighting;
                        break;

                    case Keyboard.KEY_B:
                        lightTimer.togglePause();
                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }
            }
        }
    }


    ////////////////////////////////
    private float zNear = 1.0f;
    private float zFar = 1000.0f;

    private ProgramData whiteDiffuseColor;
    private ProgramData vertexDiffuseColor;
    private ProgramData fragWhiteDiffuseColor;
    private ProgramData fragVertexDiffuseColor;
    private UnlitProgData unlit;

    private class ProgramData {
        int theProgram;

        int modelSpaceLightPosUnif;
        int lightIntensityUnif;
        int ambientIntensityUnif;

        int modelToCameraMatrixUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer( Vec4.SIZE );
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );


    private void initializePrograms() {
        whiteDiffuseColor = loadLitProgram( "ModelPosVertexLighting_PN.vert", "ColorPassthrough.frag" );
        vertexDiffuseColor = loadLitProgram( "ModelPosVertexLighting_PCN.vert", "ColorPassthrough.frag" );
        fragWhiteDiffuseColor = loadLitProgram( "FragmentLighting_PN.vert", "FragmentLighting.frag" );
        fragVertexDiffuseColor = loadLitProgram( "FragmentLighting_PCN.vert", "FragmentLighting.frag" );
        unlit = loadUnlitProgram( "PosTransform.vert", "UniformColor.frag" );
    }

    private ProgramData loadLitProgram(String vertexShaderFilename, String fragmentShaderFilename) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, vertexShaderFilename ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, fragmentShaderFilename ) );

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram( shaderList );
        data.modelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "modelToCameraMatrix" );
        data.modelSpaceLightPosUnif = glGetUniformLocation( data.theProgram, "modelSpaceLightPos" );
        data.lightIntensityUnif = glGetUniformLocation( data.theProgram, "lightIntensity" );
        data.ambientIntensityUnif = glGetUniformLocation( data.theProgram, "ambientIntensity" );

        int projectionBlock = glGetUniformBlockIndex( data.theProgram, "Projection" );
        glUniformBlockBinding( data.theProgram, projectionBlock, projectionBlockIndex );

        return data;
    }

    private UnlitProgData loadUnlitProgram(String vertexShaderFilename, String fragmentShaderFilename) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, vertexShaderFilename ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, fragmentShaderFilename ) );

        UnlitProgData data = new UnlitProgData();
        data.theProgram = Framework.createProgram( shaderList );
        data.modelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "modelToCameraMatrix" );
        data.objectColorUnif = glGetUniformLocation( data.theProgram, "objectColor" );

        int projectionBlock = glGetUniformBlockIndex( data.theProgram, "Projection" );
        glUniformBlockBinding( data.theProgram, projectionBlock, projectionBlockIndex );

        return data;
    }


    ////////////////////////////////
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;

    private Timer lightTimer = new Timer( Timer.Type.LOOP, 5.0f );

    private boolean useFragmentLighting = true;
    private boolean drawColoredCyl;
    private boolean drawLight;
    private boolean scaleCyl;
    private float lightHeight = 1.5f;
    private float lightRadius = 1.0f;


    private Vec4 calcLightPosition() {
        float currTimeThroughLoop = lightTimer.getAlpha();

        Vec4 lightPos = new Vec4( 0.0f, lightHeight, 0.0f, 1.0f );

        lightPos.x = (float) (Math.cos( currTimeThroughLoop * (3.14159f * 2.0f) ) * lightRadius);
        lightPos.z = (float) (Math.sin( currTimeThroughLoop * (3.14159f * 2.0f) ) * lightRadius);

        return lightPos;
    }


    ////////////////////////////////
    // View / Object setup.
    private ViewData initialViewData = new ViewData(
            new Vec3( 0.0f, 0.5f, 0.0f ),
            new Quaternion( 0.92387953f, 0.3826834f, 0.0f, 0.0f ),
            5.0f,
            0.0f
    );

    private ViewScale viewScale = new ViewScale(
            3.0f, 20.0f,
            1.5f, 0.5f,
            0.0f, 0.0f,     // No camera movement.
            90.0f / 250.0f
    );


    private ObjectData initialObjectData = new ObjectData(
            new Vec3( 0.0f, 0.5f, 0.0f ),
            new Quaternion( 1.0f, 0.0f, 0.0f, 0.0f )
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
}