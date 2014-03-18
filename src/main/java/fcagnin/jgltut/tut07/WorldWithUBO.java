package fcagnin.jgltut.tut07;

import fcagnin.jglsdk.glm.Glm;
import fcagnin.jglsdk.glm.Mat4;
import fcagnin.jglsdk.glm.Vec3;
import fcagnin.jglsdk.glm.Vec4;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.Framework;
import fcagnin.jgltut.framework.Mesh;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

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
 * Part II. Positioning
 * Chapter 7. World in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2007.html
 * <p/>
 * Function                                     Increase/Left   Decrease/Right
 * Move camera target up/down                          E               Q
 * Move camera target horizontally                     A               D
 * Move camera target vertically                       W               S
 * Rotate camera horizontally around target            L               J
 * Rotate camera vertically around target              I               K
 * Move camera towards/away from target                U               O
 * In addition, if you hold down the SHIFT key while pressing any of the last six keys, then the affected control will be much slower.
 * <p/>
 * SPACE    - toggle the appearance of an object indicating the position of the camera point.
 *
 * @author integeruser
 */
public class WorldWithUBO extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut07/data/";

        new WorldWithUBO().start( 700, 700 );
    }


    @Override
    protected void init() {
        initializeProgram();

        try {
            coneMesh = new Mesh( "UnitConeTint.xml" );
            cylinderMesh = new Mesh( "UnitCylinderTint.xml" );
            cubeTintMesh = new Mesh( "UnitCubeTint.xml" );
            cubeColorMesh = new Mesh( "UnitCubeColor.xml" );
            planeMesh = new Mesh( "UnitPlane.xml" );
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
    }

    @Override
    protected void display() {
        glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glClearDepth( 1.0f );
        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        final Vec3 camPos = resolveCamPosition();

        MatrixStack camMatrix = new MatrixStack();
        camMatrix.setMatrix( calcLookAtMatrix( camPos, camTarget, new Vec3( 0.0f, 1.0f, 0.0f ) ) );

        glBindBuffer( GL_UNIFORM_BUFFER, globalMatricesUBO );
        glBufferSubData( GL_UNIFORM_BUFFER, Mat4.SIZE, camMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
        glBindBuffer( GL_UNIFORM_BUFFER, 0 );

        MatrixStack modelMatrix = new MatrixStack();

        // Render the ground plane.
        {
            modelMatrix.push();

            modelMatrix.scale( 100.0f, 1.0f, 100.0f );

            glUseProgram( uniformColor.theProgram );
            glUniformMatrix4( uniformColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColor.baseColorUnif, 0.302f, 0.416f, 0.0589f, 1.0f );
            planeMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }

        // Draw the trees
        drawForest( modelMatrix );

        // Draw the building.
        {
            modelMatrix.push();

            modelMatrix.translate( 20.0f, 0.0f, -10.0f );

            drawParthenon( modelMatrix );

            modelMatrix.pop();
        }

        if ( drawLookatPoint ) {
            glDisable( GL_DEPTH_TEST );

            modelMatrix.push();

            modelMatrix.translate( camTarget );
            modelMatrix.scale( 1.0f, 1.0f, 1.0f );

            glUseProgram( objectColor.theProgram );
            glUniformMatrix4( objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            cubeColorMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();

            glEnable( GL_DEPTH_TEST );
        }
    }

    @Override
    protected void reshape(int width, int height) {
        MatrixStack persMatrix = new MatrixStack();
        persMatrix.perspective( 45.0f, (width / (float) height), zNear, zFar );

        glBindBuffer( GL_UNIFORM_BUFFER, globalMatricesUBO );
        glBufferSubData( GL_UNIFORM_BUFFER, 0, persMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
        glBindBuffer( GL_UNIFORM_BUFFER, 0 );

        glViewport( 0, 0, width, height );
    }

    @Override
    protected void update() {
        float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;

        if ( Keyboard.isKeyDown( Keyboard.KEY_W ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                camTarget.z = camTarget.z - 0.4f * lastFrameDuration;
            } else {
                camTarget.z = camTarget.z - 4.0f * lastFrameDuration;
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_S ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                camTarget.z = camTarget.z + 0.4f * lastFrameDuration;
            } else {
                camTarget.z = camTarget.z + 4.0f * lastFrameDuration;
            }
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_D ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                camTarget.x = camTarget.x + 0.4f * lastFrameDuration;
            } else {
                camTarget.x = camTarget.x + 4.0f * lastFrameDuration;
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_A ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                camTarget.x = camTarget.x - 0.4f * lastFrameDuration;
            } else {
                camTarget.x = camTarget.x - 4.0f * lastFrameDuration;
            }
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_E ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                camTarget.y = camTarget.y - 0.4f * lastFrameDuration;
            } else {
                camTarget.y = camTarget.y - 4.0f * lastFrameDuration;
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_Q ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                camTarget.y = camTarget.y + 0.4f * lastFrameDuration;
            } else {
                camTarget.y = camTarget.y + 4.0f * lastFrameDuration;
            }
        }


        if ( Keyboard.isKeyDown( Keyboard.KEY_I ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.y = (float) (sphereCamRelPos.y - 1.125f * lastFrameDuration);
            } else {
                sphereCamRelPos.y = (float) (sphereCamRelPos.y - 11.25f * lastFrameDuration);
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_K ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.y = (float) (sphereCamRelPos.y + 1.125f * lastFrameDuration);
            } else {
                sphereCamRelPos.y = (float) (sphereCamRelPos.y + 11.25f * lastFrameDuration);
            }
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_J ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.x = (float) (sphereCamRelPos.x - 1.125f * lastFrameDuration);
            } else {
                sphereCamRelPos.x = (float) (sphereCamRelPos.x - 11.25f * lastFrameDuration);
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_L ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.x = (float) (sphereCamRelPos.x + 1.125f * lastFrameDuration);
            } else {
                sphereCamRelPos.x = (float) (sphereCamRelPos.x + 11.25f * lastFrameDuration);
            }
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_O ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.z = (float) (sphereCamRelPos.z - 0.5f * lastFrameDuration);
            } else {
                sphereCamRelPos.z = (float) (sphereCamRelPos.z - 5.0f * lastFrameDuration);
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_U ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.z = (float) (sphereCamRelPos.z + 0.5f * lastFrameDuration);
            } else {
                sphereCamRelPos.z = (float) (sphereCamRelPos.z + 5.0f * lastFrameDuration);
            }
        }


        while ( Keyboard.next() ) {
            if ( Keyboard.getEventKeyState() ) {
                switch ( Keyboard.getEventKey() ) {
                    case Keyboard.KEY_SPACE:
                        drawLookatPoint = !drawLookatPoint;
                        System.out.printf( "Target: %f, %f, %f\n", camTarget.x, camTarget.y, camTarget.z );
                        System.out.printf( "Position: %f, %f, %f\n", sphereCamRelPos.x, sphereCamRelPos.y, sphereCamRelPos.z );
                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }
            }
        }


        sphereCamRelPos.y = Glm.clamp( sphereCamRelPos.y, -78.75f, -1.0f );
        camTarget.y = camTarget.y > 0.0f ? camTarget.y : 0.0f;
        sphereCamRelPos.z = sphereCamRelPos.z > 5.0f ? sphereCamRelPos.z : 5.0f;
    }


    ////////////////////////////////
    private float zNear = 1.0f;
    private float zFar = 1000.0f;

    private ProgramData uniformColor;
    private ProgramData objectColor;
    private ProgramData uniformColorTint;

    private class ProgramData {
        int theProgram;
        int globalUniformBlockIndex;
        int modelToWorldMatrixUnif;
        int baseColorUnif;
    }

    private int globalMatricesUBO;

    private final int globalMatricesBindingIndex = 0;


    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );


    private void initializeProgram() {
        uniformColor = loadProgram( "PosOnlyWorldTransformUBO.vert", "ColorUniform.frag" );
        objectColor = loadProgram( "PosColorWorldTransformUBO.vert", "ColorPassthrough.frag" );
        uniformColorTint = loadProgram( "PosColorWorldTransformUBO.vert", "ColorMultUniform.frag" );

        globalMatricesUBO = glGenBuffers();
        glBindBuffer( GL_UNIFORM_BUFFER, globalMatricesUBO );
        glBufferData( GL_UNIFORM_BUFFER, Mat4.SIZE * 2, GL_STREAM_DRAW );
        glBindBuffer( GL_UNIFORM_BUFFER, 0 );
        glBindBufferRange( GL_UNIFORM_BUFFER, globalMatricesBindingIndex, globalMatricesUBO,
                0, Mat4.SIZE * 2 );
    }

    private ProgramData loadProgram(String vertexShaderFilename, String fragmentShaderFilename) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, vertexShaderFilename ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, fragmentShaderFilename ) );

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram( shaderList );
        data.modelToWorldMatrixUnif = glGetUniformLocation( data.theProgram, "modelToWorldMatrix" );
        data.globalUniformBlockIndex = glGetUniformBlockIndex( data.theProgram, "GlobalMatrices" );
        data.baseColorUnif = glGetUniformLocation( data.theProgram, "baseColor" );

        glUniformBlockBinding( data.theProgram, data.globalUniformBlockIndex, globalMatricesBindingIndex );

        return data;
    }


    ////////////////////////////////
    private Mesh coneMesh;
    private Mesh cylinderMesh;
    private Mesh cubeTintMesh;
    private Mesh cubeColorMesh;
    private Mesh planeMesh;

    private boolean drawLookatPoint = false;
    private Vec3 camTarget = new Vec3( 0.0f, 0.4f, 0.0f );

    // In spherical coordinates.
    private Vec3 sphereCamRelPos = new Vec3( 67.5f, -46.0f, 150.0f );


    private Vec3 resolveCamPosition() {
        float phi = Framework.degToRad( sphereCamRelPos.x );
        float theta = Framework.degToRad( sphereCamRelPos.y + 90.0f );

        float sinTheta = (float) Math.sin( theta );
        float cosTheta = (float) Math.cos( theta );
        float cosPhi = (float) Math.cos( phi );
        float sinPhi = (float) Math.sin( phi );

        Vec3 dirToCamera = new Vec3( sinTheta * cosPhi, cosTheta, sinTheta * sinPhi );

        return (dirToCamera.scale( sphereCamRelPos.z )).add( camTarget );
    }


    private Mat4 calcLookAtMatrix(Vec3 cameraPt, Vec3 lookPt, Vec3 upPt) {
        Vec3 lookDir = Glm.normalize( Vec3.sub( lookPt, cameraPt ) );
        Vec3 upDir = Glm.normalize( upPt );

        Vec3 rightDir = Glm.normalize( Glm.cross( lookDir, upDir ) );
        Vec3 perpUpDir = Glm.cross( rightDir, lookDir );

        Mat4 rotMat = new Mat4( 1.0f );
        rotMat.setColumn( 0, new Vec4( rightDir, 0.0f ) );
        rotMat.setColumn( 1, new Vec4( perpUpDir, 0.0f ) );
        rotMat.setColumn( 2, new Vec4( Vec3.negate( lookDir ), 0.0f ) );

        rotMat = Glm.transpose( rotMat );

        Mat4 transMat = new Mat4( 1.0f );
        transMat.setColumn( 3, new Vec4( Vec3.negate( cameraPt ), 1.0f ) );

        return rotMat.mul( transMat );
    }


    ////////////////////////////////
    private final TreeData forest[] = {
            new TreeData( -45.0f, -40.0f, 2.0f, 3.0f ),
            new TreeData( -42.0f, -35.0f, 2.0f, 3.0f ),
            new TreeData( -39.0f, -29.0f, 2.0f, 4.0f ),
            new TreeData( -44.0f, -26.0f, 3.0f, 3.0f ),
            new TreeData( -40.0f, -22.0f, 2.0f, 4.0f ),
            new TreeData( -36.0f, -15.0f, 3.0f, 3.0f ),
            new TreeData( -41.0f, -11.0f, 2.0f, 3.0f ),
            new TreeData( -37.0f, -6.0f, 3.0f, 3.0f ),
            new TreeData( -45.0f, 0.0f, 2.0f, 3.0f ),
            new TreeData( -39.0f, 4.0f, 3.0f, 4.0f ),
            new TreeData( -36.0f, 8.0f, 2.0f, 3.0f ),
            new TreeData( -44.0f, 13.0f, 3.0f, 3.0f ),
            new TreeData( -42.0f, 17.0f, 2.0f, 3.0f ),
            new TreeData( -38.0f, 23.0f, 3.0f, 4.0f ),
            new TreeData( -41.0f, 27.0f, 2.0f, 3.0f ),
            new TreeData( -39.0f, 32.0f, 3.0f, 3.0f ),
            new TreeData( -44.0f, 37.0f, 3.0f, 4.0f ),
            new TreeData( -36.0f, 42.0f, 2.0f, 3.0f ),

            new TreeData( -32.0f, -45.0f, 2.0f, 3.0f ),
            new TreeData( -30.0f, -42.0f, 2.0f, 4.0f ),
            new TreeData( -34.0f, -38.0f, 3.0f, 5.0f ),
            new TreeData( -33.0f, -35.0f, 3.0f, 4.0f ),
            new TreeData( -29.0f, -28.0f, 2.0f, 3.0f ),
            new TreeData( -26.0f, -25.0f, 3.0f, 5.0f ),
            new TreeData( -35.0f, -21.0f, 3.0f, 4.0f ),
            new TreeData( -31.0f, -17.0f, 3.0f, 3.0f ),
            new TreeData( -28.0f, -12.0f, 2.0f, 4.0f ),
            new TreeData( -29.0f, -7.0f, 3.0f, 3.0f ),
            new TreeData( -26.0f, -1.0f, 2.0f, 4.0f ),
            new TreeData( -32.0f, 6.0f, 2.0f, 3.0f ),
            new TreeData( -30.0f, 10.0f, 3.0f, 5.0f ),
            new TreeData( -33.0f, 14.0f, 2.0f, 4.0f ),
            new TreeData( -35.0f, 19.0f, 3.0f, 4.0f ),
            new TreeData( -28.0f, 22.0f, 2.0f, 3.0f ),
            new TreeData( -33.0f, 26.0f, 3.0f, 3.0f ),
            new TreeData( -29.0f, 31.0f, 3.0f, 4.0f ),
            new TreeData( -32.0f, 38.0f, 2.0f, 3.0f ),
            new TreeData( -27.0f, 41.0f, 3.0f, 4.0f ),
            new TreeData( -31.0f, 45.0f, 2.0f, 4.0f ),
            new TreeData( -28.0f, 48.0f, 3.0f, 5.0f ),

            new TreeData( -25.0f, -48.0f, 2.0f, 3.0f ),
            new TreeData( -20.0f, -42.0f, 3.0f, 4.0f ),
            new TreeData( -22.0f, -39.0f, 2.0f, 3.0f ),
            new TreeData( -19.0f, -34.0f, 2.0f, 3.0f ),
            new TreeData( -23.0f, -30.0f, 3.0f, 4.0f ),
            new TreeData( -24.0f, -24.0f, 2.0f, 3.0f ),
            new TreeData( -16.0f, -21.0f, 2.0f, 3.0f ),
            new TreeData( -17.0f, -17.0f, 3.0f, 3.0f ),
            new TreeData( -25.0f, -13.0f, 2.0f, 4.0f ),
            new TreeData( -23.0f, -8.0f, 2.0f, 3.0f ),
            new TreeData( -17.0f, -2.0f, 3.0f, 3.0f ),
            new TreeData( -16.0f, 1.0f, 2.0f, 3.0f ),
            new TreeData( -19.0f, 4.0f, 3.0f, 3.0f ),
            new TreeData( -22.0f, 8.0f, 2.0f, 4.0f ),
            new TreeData( -21.0f, 14.0f, 2.0f, 3.0f ),
            new TreeData( -16.0f, 19.0f, 2.0f, 3.0f ),
            new TreeData( -23.0f, 24.0f, 3.0f, 3.0f ),
            new TreeData( -18.0f, 28.0f, 2.0f, 4.0f ),
            new TreeData( -24.0f, 31.0f, 2.0f, 3.0f ),
            new TreeData( -20.0f, 36.0f, 2.0f, 3.0f ),
            new TreeData( -22.0f, 41.0f, 3.0f, 3.0f ),
            new TreeData( -21.0f, 45.0f, 2.0f, 3.0f ),

            new TreeData( -12.0f, -40.0f, 2.0f, 4.0f ),
            new TreeData( -11.0f, -35.0f, 3.0f, 3.0f ),
            new TreeData( -10.0f, -29.0f, 1.0f, 3.0f ),
            new TreeData( -9.0f, -26.0f, 2.0f, 2.0f ),
            new TreeData( -6.0f, -22.0f, 2.0f, 3.0f ),
            new TreeData( -15.0f, -15.0f, 1.0f, 3.0f ),
            new TreeData( -8.0f, -11.0f, 2.0f, 3.0f ),
            new TreeData( -14.0f, -6.0f, 2.0f, 4.0f ),
            new TreeData( -12.0f, 0.0f, 2.0f, 3.0f ),
            new TreeData( -7.0f, 4.0f, 2.0f, 2.0f ),
            new TreeData( -13.0f, 8.0f, 2.0f, 2.0f ),
            new TreeData( -9.0f, 13.0f, 1.0f, 3.0f ),
            new TreeData( -13.0f, 17.0f, 3.0f, 4.0f ),
            new TreeData( -6.0f, 23.0f, 2.0f, 3.0f ),
            new TreeData( -12.0f, 27.0f, 1.0f, 2.0f ),
            new TreeData( -8.0f, 32.0f, 2.0f, 3.0f ),
            new TreeData( -10.0f, 37.0f, 3.0f, 3.0f ),
            new TreeData( -11.0f, 42.0f, 2.0f, 2.0f ),

            new TreeData( 15.0f, 5.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 10.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 15.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 20.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 25.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 30.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 35.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 40.0f, 2.0f, 3.0f ),
            new TreeData( 15.0f, 45.0f, 2.0f, 3.0f ),

            new TreeData( 25.0f, 5.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 10.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 15.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 20.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 25.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 30.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 35.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 40.0f, 2.0f, 3.0f ),
            new TreeData( 25.0f, 45.0f, 2.0f, 3.0f )
    };

    private class TreeData {
        float xPos;
        float zPos;
        float trunkHeight;
        float coneHeight;

        TreeData(float xPos, float zPos, float trunkHeight, float coneHeight) {
            this.xPos = xPos;
            this.zPos = zPos;
            this.trunkHeight = trunkHeight;
            this.coneHeight = coneHeight;
        }
    }


    private void drawForest(MatrixStack modelMatrix) {
        for ( TreeData currTree : forest ) {
            modelMatrix.push();

            modelMatrix.translate( currTree.xPos, 0.0f, currTree.zPos );
            drawTree( modelMatrix, currTree.trunkHeight, currTree.coneHeight );

            modelMatrix.pop();
        }
    }

    // Trees are 3x3 in X/Z, and fTrunkHeight + fConeHeight in the Y.
    private void drawTree(MatrixStack modelMatrix, float trunkHeight, float coneHeight) {
        // Draw trunk.
        {
            modelMatrix.push();

            modelMatrix.scale( 1.0f, trunkHeight, 1.0f );
            modelMatrix.translate( 0.0f, 0.5f, 0.0f );

            glUseProgram( uniformColorTint.theProgram );
            glUniformMatrix4( uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColorTint.baseColorUnif, 0.694f, 0.4f, 0.106f, 1.0f );
            cylinderMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }

        // Draw the treetop
        {
            modelMatrix.push();

            modelMatrix.translate( 0.0f, trunkHeight, 0.0f );
            modelMatrix.scale( 3.0f, coneHeight, 3.0f );

            glUseProgram( uniformColorTint.theProgram );
            glUniformMatrix4( uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColorTint.baseColorUnif, 0.0f, 1.0f, 0.0f, 1.0f );
            coneMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }
    }


    ////////////////////////////////
    private final float columnBaseHeight = 0.25f;


    // Columns are 1x1 in the X/Z, and fHieght units in the Y.
    private void drawColumn(MatrixStack modelMatrix, float height) {
        // Draw the bottom of the column.
        {
            modelMatrix.push();

            modelMatrix.scale( 1.0f, columnBaseHeight, 1.0f );
            modelMatrix.translate( 0.0f, 0.5f, 0.0f );

            glUseProgram( uniformColorTint.theProgram );
            glUniformMatrix4( uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColorTint.baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f );
            cubeTintMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }

        // Draw the top of the column.
        {
            modelMatrix.push();

            modelMatrix.translate( 0.0f, height - columnBaseHeight, 0.0f );
            modelMatrix.scale( 1.0f, columnBaseHeight, 1.0f );
            modelMatrix.translate( 0.0f, 0.5f, 0.0f );

            glUseProgram( uniformColorTint.theProgram );
            glUniformMatrix4( uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f );
            cubeTintMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }

        // Draw the main column.
        {
            modelMatrix.push();

            modelMatrix.translate( 0.0f, columnBaseHeight, 0.0f );
            modelMatrix.scale( 0.8f, height - (columnBaseHeight * 2.0f), 0.8f );
            modelMatrix.translate( 0.0f, 0.5f, 0.0f );

            glUseProgram( uniformColorTint.theProgram );
            glUniformMatrix4( uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f );
            cylinderMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }
    }


    ////////////////////////////////
    private final float parthenonWidth = 14.0f;
    private final float parthenonLength = 20.0f;
    private final float parthenonColumnHeight = 5.0f;
    private final float parthenonBaseHeight = 1.0f;
    private final float parthenonTopHeight = 2.0f;

    private final float frontZVal = (parthenonLength / 2.0f) - 1.0f;
    private final float rightXVal = (parthenonWidth / 2.0f) - 1.0f;
    private final int max1 = (int) (parthenonWidth / 2.0f);
    private final int max2 = (int) ((parthenonLength - 2.0f) / 2.0f);


    private void drawParthenon(MatrixStack modelMatrix) {
        // Draw base.
        {
            modelMatrix.push();

            modelMatrix.scale( parthenonWidth, parthenonBaseHeight, parthenonLength );
            modelMatrix.translate( 0.0f, 0.5f, 0.0f );

            glUseProgram( uniformColorTint.theProgram );
            glUniformMatrix4( uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f );
            cubeTintMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }

        // Draw top.
        {
            modelMatrix.push();

            modelMatrix.translate( 0.0f, parthenonColumnHeight + parthenonBaseHeight, 0.0f );
            modelMatrix.scale( parthenonWidth, parthenonTopHeight, parthenonLength );
            modelMatrix.translate( 0.0f, 0.5f, 0.0f );

            glUseProgram( uniformColorTint.theProgram );
            glUniformMatrix4( uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            glUniform4f( uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f );
            cubeTintMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }

        // Draw columns.
        for ( int iColumnNum = 0; iColumnNum < max1; iColumnNum++ ) {
            {
                modelMatrix.push();

                modelMatrix.translate( (2.0f * iColumnNum) - (parthenonWidth / 2.0f) + 1.0f, parthenonBaseHeight, frontZVal );

                drawColumn( modelMatrix, parthenonColumnHeight );

                modelMatrix.pop();
            }
            {
                modelMatrix.push();

                modelMatrix.translate( (2.0f * iColumnNum) - (parthenonWidth / 2.0f) + 1.0f, parthenonBaseHeight, -frontZVal );

                drawColumn( modelMatrix, parthenonColumnHeight );

                modelMatrix.pop();
            }
        }

        // Don't draw the first or last columns, since they've been drawn already.
        for ( int iColumnNum = 1; iColumnNum < max2; iColumnNum++ ) {
            {
                modelMatrix.push();

                modelMatrix.translate( rightXVal, parthenonBaseHeight, (2.0f * iColumnNum) - (parthenonLength / 2.0f) + 1.0f );

                drawColumn( modelMatrix, parthenonColumnHeight );

                modelMatrix.pop();
            }
            {
                modelMatrix.push();

                modelMatrix.translate( -rightXVal, parthenonBaseHeight, (2.0f * iColumnNum) - (parthenonLength / 2.0f) + 1.0f );

                drawColumn( modelMatrix, parthenonColumnHeight );

                modelMatrix.pop();
            }
        }

        // Draw interior.
        {
            modelMatrix.push();

            modelMatrix.translate( 0.0f, 1.0f, 0.0f );
            modelMatrix.scale( parthenonWidth - 6.0f, parthenonColumnHeight, parthenonLength - 6.0f );
            modelMatrix.translate( 0.0f, 0.5f, 0.0f );

            glUseProgram( objectColor.theProgram );
            glUniformMatrix4( objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            cubeColorMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }

        // Draw headpiece.
        {
            modelMatrix.push();

            modelMatrix.translate( 0.0f, parthenonColumnHeight + parthenonBaseHeight + (parthenonTopHeight / 2.0f),
                    parthenonLength / 2.0f );
            modelMatrix.rotateX( -135.0f );
            modelMatrix.rotateY( 45.0f );

            glUseProgram( objectColor.theProgram );
            glUniformMatrix4( objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillAndFlipBuffer( mat4Buffer ) );
            cubeColorMesh.render();
            glUseProgram( 0 );

            modelMatrix.pop();
        }
    }
}