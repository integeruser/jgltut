package fcagnin.jgltut.tut11;

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
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p/>
 * Part III. Illumination
 * Chapter 11. Shinies
 * http://www.arcsynthesis.org/gltut/Illumination/Tutorial%2011.html
 * <p/>
 * I,J,K,L  - control the light's position. Holding SHIFT with these keys will move in smaller increments.
 * SPACE    - toggle between drawing the uncolored cylinder and the colored one.
 * U,O      - control the specular value. They raise and low the specular exponent. Using SHIFT in combination with them will raise/lower
 * the exponent by smaller amounts.
 * Y        - toggle the drawing of the light source.
 * T        - toggle between the scaled and unscaled cylinder.
 * B        - toggle the light's rotation on/off.
 * G        - toggle between a diffuse color of (1, 1, 1) and a darker diffuse color of (0.2, 0.2, 0.2).
 * H        - switch between Blinn, Phong and Gaussian specular. Pressing SHIFT+H will switch between diffuse+specular and specular only.
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
public class GaussianSpecularLighting extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut11/data/";

        new GaussianSpecularLighting().start();
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

        final float depthZNear = 0.0f;
        final float depthZFar = 1.0f;

        glEnable( GL_DEPTH_TEST );
        glDepthMask( true );
        glDepthFunc( GL_LEQUAL );
        glDepthRange( depthZNear, depthZFar );
        glEnable( GL_DEPTH_CLAMP );

        projectionUniformBuffer = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, projectionUniformBuffer );
        glBufferData( GL_UNIFORM_BUFFER, ProjectionBlock.SIZE, GL_DYNAMIC_DRAW );

        // Bind the static buffers.
        glBindBufferRange( GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE );

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
        final Vec4 lightPosCameraSpace = Mat4.mul( modelMatrix.top(), worldLightPos );

        ProgramData whiteProg = programs[lightModel.ordinal()].whiteProg;
        ProgramData colorProg = programs[lightModel.ordinal()].colorProg;

        glUseProgram( whiteProg.theProgram );
        glUniform4f( whiteProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f );
        glUniform4f( whiteProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f );
        glUniform3( whiteProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer( vec4Buffer ) );
        float lightAttenuation = 1.2f;
        glUniform1f( whiteProg.lightAttenuationUnif, lightAttenuation );
        glUniform1f( whiteProg.shininessFactorUnif, matParams.getSpecularValue() );
        glUniform4( whiteProg.baseDiffuseColorUnif, drawDark ? darkColor.fillAndFlipBuffer( vec4Buffer ) : lightColor.fillAndFlipBuffer(
                vec4Buffer ) );

        glUseProgram( colorProg.theProgram );
        glUniform4f( colorProg.lightIntensityUnif, 0.8f, 0.8f, 0.8f, 1.0f );
        glUniform4f( colorProg.ambientIntensityUnif, 0.2f, 0.2f, 0.2f, 1.0f );
        glUniform3( colorProg.cameraSpaceLightPosUnif, lightPosCameraSpace.fillAndFlipBuffer( vec4Buffer ) );
        glUniform1f( colorProg.lightAttenuationUnif, lightAttenuation );
        glUniform1f( colorProg.shininessFactorUnif, matParams.getSpecularValue() );
        glUseProgram( 0 );

        {
            modelMatrix.push();

            // Render the ground plane.
            {
                modelMatrix.push();

                Mat3 normMatrix = new Mat3( modelMatrix.top() );
                normMatrix = Glm.transpose( Glm.inverse( normMatrix ) );

                glUseProgram( whiteProg.theProgram );
                glUniformMatrix4( whiteProg.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

                glUniformMatrix3( whiteProg.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer( mat3Buffer ) );
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

                Mat3 normMatrix = new Mat3( modelMatrix.top() );
                normMatrix = Glm.transpose( Glm.inverse( normMatrix ) );

                ProgramData prog = drawColoredCyl ? colorProg : whiteProg;
                glUseProgram( prog.theProgram );
                glUniformMatrix4( prog.modelToCameraMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

                glUniformMatrix3( prog.normalModelToCameraMatrixUnif, false, normMatrix.fillAndFlipBuffer( mat3Buffer ) );

                if ( drawColoredCyl ) {
                    cylinderMesh.render( "lit-color" );
                } else {
                    cylinderMesh.render( "lit" );
                }

                glUseProgram( 0 );

                modelMatrix.pop();
            }

            // Render the light
            if ( drawLightSource ) {
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

                    case Keyboard.KEY_O:
                        if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                            matParams.increment( false );
                        } else {
                            matParams.increment( true );
                        }

                        System.out.printf( "Shiny: %f\n", (float) matParams.getSpecularValue() );
                        break;

                    case Keyboard.KEY_U:
                        if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                            matParams.decrement( false );
                        } else {
                            matParams.decrement( true );
                        }

                        System.out.printf( "Shiny: %f\n", (float) matParams.getSpecularValue() );
                        break;

                    case Keyboard.KEY_Y:
                        drawLightSource = !drawLightSource;
                        break;

                    case Keyboard.KEY_T:
                        scaleCyl = !scaleCyl;
                        break;

                    case Keyboard.KEY_B:
                        lightTimer.togglePause();
                        break;

                    case Keyboard.KEY_G:
                        drawDark = !drawDark;
                        break;

                    case Keyboard.KEY_H:
                        if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                            int index;
                            if ( lightModel.ordinal() % 2 != 0 ) {
                                index = lightModel.ordinal() - 1;
                            } else {
                                index = lightModel.ordinal() + 1;
                            }
                            index %= LightingModel.MAX_LIGHTING_MODEL.ordinal();
                            lightModel = LightingModel.values()[index];
                        } else {
                            int index = lightModel.ordinal() + 2;
                            index %= LightingModel.MAX_LIGHTING_MODEL.ordinal();
                            lightModel = LightingModel.values()[index];
                        }

                        System.out.printf( "%s\n", lightModelNames[lightModel.ordinal()] );
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

    private ProgramPairs[] programs = new ProgramPairs[LightingModel.MAX_LIGHTING_MODEL.ordinal()];
    private ShaderPairs[] shaderFileNames = new ShaderPairs[]{
            new ShaderPairs( "PN.vert", "PCN.vert", "PhongLighting.frag" ),
            new ShaderPairs( "PN.vert", "PCN.vert", "PhongOnly.frag" ),
            new ShaderPairs( "PN.vert", "PCN.vert", "BlinnLighting.frag" ),
            new ShaderPairs( "PN.vert", "PCN.vert", "BlinnOnly.frag" ),
            new ShaderPairs( "PN.vert", "PCN.vert", "GaussianLighting.frag" ),
            new ShaderPairs( "PN.vert", "PCN.vert", "GaussianOnly.frag" )
    };
    private UnlitProgData unlit;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;

        int lightIntensityUnif;
        int ambientIntensityUnif;

        int normalModelToCameraMatrixUnif;
        int cameraSpaceLightPosUnif;
        int lightAttenuationUnif;
        int shininessFactorUnif;
        int baseDiffuseColorUnif;
    }

    private class UnlitProgData {
        int theProgram;

        int objectColorUnif;
        int modelToCameraMatrixUnif;
    }

    private class ProgramPairs {
        ProgramData whiteProg;
        ProgramData colorProg;
    }

    private class ShaderPairs {
        String whiteVertShaderFileName;
        String colorVertShaderFileName;
        String fragmentShaderFileName;

        ShaderPairs(String whiteVertShaderFileName, String colorVertShaderFileName, String fragmentShaderFileName) {
            this.whiteVertShaderFileName = whiteVertShaderFileName;
            this.colorVertShaderFileName = colorVertShaderFileName;
            this.fragmentShaderFileName = fragmentShaderFileName;
        }
    }


    private FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer( Vec4.SIZE );
    private FloatBuffer mat3Buffer = BufferUtils.createFloatBuffer( Mat3.SIZE );
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );


    private void initializePrograms() {
        for ( int progIndex = 0; progIndex < LightingModel.MAX_LIGHTING_MODEL.ordinal(); progIndex++ ) {
            programs[progIndex] = new ProgramPairs();
            programs[progIndex].whiteProg = loadLitProgram( shaderFileNames[progIndex].whiteVertShaderFileName,
                    shaderFileNames[progIndex].fragmentShaderFileName );
            programs[progIndex].colorProg = loadLitProgram( shaderFileNames[progIndex].colorVertShaderFileName,
                    shaderFileNames[progIndex].fragmentShaderFileName );
        }

        unlit = loadUnlitProgram( "PosTransform.vert", "UniformColor.frag" );
    }

    private ProgramData loadLitProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, vertexShaderFileName ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, fragmentShaderFileName ) );

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram( shaderList );
        data.modelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "modelToCameraMatrix" );
        data.lightIntensityUnif = glGetUniformLocation( data.theProgram, "lightIntensity" );
        data.ambientIntensityUnif = glGetUniformLocation( data.theProgram, "ambientIntensity" );

        data.normalModelToCameraMatrixUnif = glGetUniformLocation( data.theProgram, "normalModelToCameraMatrix" );
        data.cameraSpaceLightPosUnif = glGetUniformLocation( data.theProgram, "cameraSpaceLightPos" );
        data.lightAttenuationUnif = glGetUniformLocation( data.theProgram, "lightAttenuation" );
        data.shininessFactorUnif = glGetUniformLocation( data.theProgram, "shininessFactor" );
        data.baseDiffuseColorUnif = glGetUniformLocation( data.theProgram, "baseDiffuseColor" );

        int projectionBlock = glGetUniformBlockIndex( data.theProgram, "Projection" );
        glUniformBlockBinding( data.theProgram, projectionBlock, projectionBlockIndex );

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
    private Mesh cylinderMesh;
    private Mesh planeMesh;
    private Mesh cubeMesh;

    private float lightHeight = 1.5f;
    private float lightRadius = 1.0f;
    private Timer lightTimer = new Timer( Timer.Type.LOOP, 5.0f );

    private boolean drawColoredCyl;
    private boolean drawLightSource;
    private boolean scaleCyl;
    private boolean drawDark;

    private final Vec4 darkColor = new Vec4( 0.2f, 0.2f, 0.2f, 1.0f );
    private final Vec4 lightColor = new Vec4( 1.0f );

    private final String[] lightModelNames = {
            "Phong Specular.",
            "Phong Only.",
            "Blinn Specular.",
            "Blinn Only.",
            "Gaussian Specular.",
            "Gaussian Only."
    };


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


    ////////////////////////////////
    private LightingModel lightModel = LightingModel.GAUSSIAN_SPECULAR;

    private enum LightingModel {
        PHONG_SPECULAR,
        PHONG_ONLY,
        BLINN_SPECULAR,
        BLINN_ONLY,
        GAUSSIAN_SPECULAR,
        GAUSSIAN_ONLY,

        MAX_LIGHTING_MODEL;
    }


    ////////////////////////////////
    private MaterialParams matParams = new MaterialParams();

    private class MaterialParams {
        float phongExponent;
        float blinnExponent;
        float gaussianRoughness;


        MaterialParams() {
            phongExponent = 4.0f;
            blinnExponent = 4.0f;
            gaussianRoughness = 0.5f;
        }


        void increment(boolean isLarge) {
            switch ( lightModel ) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    if ( isLarge ) {
                        phongExponent += 0.5f;
                    } else {
                        phongExponent += 0.1f;
                    }
                    break;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    if ( isLarge ) {
                        blinnExponent += 0.5f;
                    } else {
                        blinnExponent += 0.1f;
                    }
                    break;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    if ( isLarge ) {
                        gaussianRoughness += 0.1f;
                    } else {
                        gaussianRoughness += 0.01f;
                    }
                    break;

                default:
                    break;
            }

            clampParam();
        }

        void decrement(boolean isLarge) {
            switch ( lightModel ) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    if ( isLarge ) {
                        phongExponent += 0.5f;
                    } else {
                        phongExponent += 0.1f;
                    }
                    break;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    if ( isLarge ) {
                        blinnExponent += 0.5f;
                    } else {
                        blinnExponent += 0.1f;
                    }
                    break;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    if ( isLarge ) {
                        gaussianRoughness -= 0.1f;
                    } else {
                        gaussianRoughness -= 0.01f;
                    }
                    break;

                default:
                    break;
            }

            clampParam();
        }


        float getSpecularValue() {
            switch ( lightModel ) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    return phongExponent;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    return blinnExponent;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    return gaussianRoughness;

                default:
                    float stopComplaint = 0.0f;
                    return stopComplaint;
            }
        }


        void clampParam() {
            switch ( lightModel ) {
                case PHONG_SPECULAR:
                case PHONG_ONLY:
                    if ( phongExponent <= 0.0f ) {
                        phongExponent = 0.0001f;
                    }
                    break;

                case BLINN_SPECULAR:
                case BLINN_ONLY:
                    if ( blinnExponent <= 0.0f ) {
                        blinnExponent = 0.0001f;
                    }
                    break;

                case GAUSSIAN_SPECULAR:
                case GAUSSIAN_ONLY:
                    gaussianRoughness = Math.max( 0.0001f, gaussianRoughness );
                    gaussianRoughness = Math.min( 1.0f, gaussianRoughness );
                    break;

                default:
                    break;
            }
        }
    }
}