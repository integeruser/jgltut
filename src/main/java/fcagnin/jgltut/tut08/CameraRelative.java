package fcagnin.jgltut.tut08;

import fcagnin.jglsdk.glm.*;
import fcagnin.jglsdk.glutil.MatrixStack;
import fcagnin.jgltut.LWJGLWindow;
import fcagnin.jgltut.framework.Framework;
import fcagnin.jgltut.framework.Mesh;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


/**
 * Visit https://github.com/integeruser/gltut-lwjgl for project info, updates and license terms. info, updates and license terms.
 * <p/>
 * Part II. Positioning
 * Chapter 8. Getting Oriented
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2008.html
 * <p/>
 * SPACE    - toggles between three transforms: model-relative (yaw/pitch/roll-style), world-relative, and camera-relative.
 * W,S      - control the outer gimbal.
 * A,D      - control the middle gimbal.
 * Q,E      - control the inner gimbal.
 * I,K      - move the camera up and down, relative to a center point. Holding SHIFT with these keys will move the camera in smaller
 * increments.
 * J,L      - move the camera left and right around the center point. Holding SHIFT with these keys will move the camera in smaller
 * increments.
 *
 * @author integeruser
 */
public class CameraRelative extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/fcagnin/jgltut/tut08/data/";

        new CameraRelative().start();
    }


    @Override
    protected void init() {
        initializeProgram();

        try {
            ship = new Mesh( "Ship.xml" );
            plane = new Mesh( "UnitPlane.xml" );
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
    }

    @Override
    protected void display() {
        glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glClearDepth( 1.0f );
        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        MatrixStack currMatrix = new MatrixStack();
        final Vec3 camPos = resolveCamPosition();
        currMatrix.setMatrix( calcLookAtMatrix( camPos, camTarget, new Vec3( 0.0f, 1.0f, 0.0f ) ) );

        glUseProgram( theProgram );

        {
            currMatrix.push();
            currMatrix.scale( 100.0f, 1.0f, 100.0f );

            glUniform4f( baseColorUnif, 0.2f, 0.5f, 0.2f, 1.0f );
            glUniformMatrix4( modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

            plane.render();

            currMatrix.pop();
        }

        {
            currMatrix.push();
            currMatrix.translate( camTarget );
            currMatrix.applyMatrix( Glm.matCast( orientation ) );
            currMatrix.rotateX( -90.0f );

            // Set the base color for this object.
            glUniform4f( baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f );
            glUniformMatrix4( modelToCameraMatrixUnif, false, currMatrix.top().fillAndFlipBuffer( mat4Buffer ) );

            ship.render( "tint" );

            currMatrix.pop();
        }

        glUseProgram( 0 );
    }

    @Override
    protected void reshape(int width, int height) {
        cameraToClipMatrix.set( 0, 0, frustumScale * (height / (float) width) );
        cameraToClipMatrix.set( 1, 1, frustumScale );

        glUseProgram( theProgram );
        glUniformMatrix4( cameraToClipMatrixUnif, false, cameraToClipMatrix.fillAndFlipBuffer( mat4Buffer ) );
        glUseProgram( 0 );

        glViewport( 0, 0, width, height );
    }

    @Override
    protected void update() {
        final float SMALL_ANGLE_INCREMENT = 9.0f;
        float lastFrameDuration = getLastFrameDuration() * 10 / 1000.0f;

        if ( Keyboard.isKeyDown( Keyboard.KEY_W ) ) {
            offsetOrientation( new Vec3( 1.0f, 0.0f, 0.0f ), SMALL_ANGLE_INCREMENT * lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_S ) ) {
            offsetOrientation( new Vec3( 1.0f, 0.0f, 0.0f ), -SMALL_ANGLE_INCREMENT * lastFrameDuration );
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_A ) ) {
            offsetOrientation( new Vec3( 0.0f, 0.0f, 1.0f ), SMALL_ANGLE_INCREMENT * lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_D ) ) {
            offsetOrientation( new Vec3( 0.0f, 0.0f, 1.0f ), -SMALL_ANGLE_INCREMENT * lastFrameDuration );
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_Q ) ) {
            offsetOrientation( new Vec3( 0.0f, 1.0f, 0.0f ), SMALL_ANGLE_INCREMENT * lastFrameDuration );
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_E ) ) {
            offsetOrientation( new Vec3( 0.0f, 1.0f, 0.0f ), -SMALL_ANGLE_INCREMENT * lastFrameDuration );
        }


        if ( Keyboard.isKeyDown( Keyboard.KEY_I ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.y = sphereCamRelPos.y - 1.125f * lastFrameDuration;
            } else {
                sphereCamRelPos.y = sphereCamRelPos.y - 11.25f * lastFrameDuration;
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_K ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.y = sphereCamRelPos.y + 1.125f * lastFrameDuration;
            } else {
                sphereCamRelPos.y = sphereCamRelPos.y + 11.25f * lastFrameDuration;
            }
        }

        if ( Keyboard.isKeyDown( Keyboard.KEY_J ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.x = sphereCamRelPos.x - 1.125f * lastFrameDuration;
            } else {
                sphereCamRelPos.x = sphereCamRelPos.x - 11.25f * lastFrameDuration;
            }
        } else if ( Keyboard.isKeyDown( Keyboard.KEY_L ) ) {
            if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) {
                sphereCamRelPos.x = sphereCamRelPos.x + 1.125f * lastFrameDuration;
            } else {
                sphereCamRelPos.x = sphereCamRelPos.x + 11.25f * lastFrameDuration;
            }
        }


        while ( Keyboard.next() ) {
            if ( Keyboard.getEventKeyState() ) {
                switch ( Keyboard.getEventKey() ) {
                    case Keyboard.KEY_SPACE:
                        int ordinal = (offsetRelative.ordinal() + 1) % OffsetRelative.NUM_RELATIVES.ordinal();
                        offsetRelative = OffsetRelative.values()[ordinal];
                        switch ( offsetRelative ) {
                            case MODEL_RELATIVE:
                                System.out.printf( "Model Relative\n" );
                                break;

                            case WORLD_RELATIVE:
                                System.out.printf( "World Relative\n" );
                                break;

                            case CAMERA_RELATIVE:
                                System.out.printf( "Camera Relative\n" );
                                break;
                        }

                        break;

                    case Keyboard.KEY_ESCAPE:
                        leaveMainLoop();
                        break;
                }
            }
        }


        sphereCamRelPos.y = Glm.clamp( sphereCamRelPos.y, -78.75f, 10.0f );
    }


    ////////////////////////////////
    private int theProgram;

    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;
    private int baseColorUnif;

    private Mat4 cameraToClipMatrix = new Mat4( 0.0f );
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer( Mat4.SIZE );

    private final float frustumScale = calcFrustumScale( 20.0f );


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add( Framework.loadShader( GL_VERTEX_SHADER, "PosColorLocalTransform.vert" ) );
        shaderList.add( Framework.loadShader( GL_FRAGMENT_SHADER, "ColorMultUniform.frag" ) );

        theProgram = Framework.createProgram( shaderList );

        modelToCameraMatrixUnif = glGetUniformLocation( theProgram, "modelToCameraMatrix" );
        cameraToClipMatrixUnif = glGetUniformLocation( theProgram, "cameraToClipMatrix" );
        baseColorUnif = glGetUniformLocation( theProgram, "baseColor" );

        float zNear = 1.0f;
        float zFar = 600.0f;

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
        float fovRad = fovDeg * degToRad;
        return (float) (1.0f / Math.tan( fovRad / 2.0f ));
    }


    ////////////////////////////////
    private Mesh ship;
    private Mesh plane;

    private Vec3 camTarget = new Vec3( 0.0f, 10.0f, 0.0f );
    private Quaternion orientation = new Quaternion( 1.0f, 0.0f, 0.0f, 0.0f );
    private OffsetRelative offsetRelative = OffsetRelative.MODEL_RELATIVE;

    // In spherical coordinates.
    private Vec3 sphereCamRelPos = new Vec3( 90.0f, 0.0f, 66.0f );

    private enum OffsetRelative {
        MODEL_RELATIVE,
        WORLD_RELATIVE,
        CAMERA_RELATIVE,

        NUM_RELATIVES
    }


    private void offsetOrientation(Vec3 axis, float angDeg) {
        float angRad = Framework.degToRad( angDeg );

        axis = Glm.normalize( axis );
        axis.scale( (float) Math.sin( angRad / 2.0f ) );

        float scalar = (float) Math.cos( angRad / 2.0f );

        Quaternion offset = new Quaternion( scalar, axis.x, axis.y, axis.z );

        switch ( offsetRelative ) {
            case MODEL_RELATIVE:
                orientation = Quaternion.mul( orientation, offset );
                break;

            case WORLD_RELATIVE:
                orientation = Quaternion.mul( offset, orientation );
                break;

            case CAMERA_RELATIVE:
                final Vec3 camPos = resolveCamPosition();
                final Mat4 camMat = calcLookAtMatrix( camPos, camTarget, new Vec3( 0.0f, 1.0f, 0.0f ) );

                Quaternion viewQuat = Glm.quatCast( camMat );
                Quaternion invViewQuat = Glm.conjugate( viewQuat );

                final Quaternion worldQuat = invViewQuat.mul( offset.mul( viewQuat ) );
                orientation = Quaternion.mul( worldQuat, orientation );
                break;
        }

        orientation = Glm.normalize( orientation );
    }

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
}