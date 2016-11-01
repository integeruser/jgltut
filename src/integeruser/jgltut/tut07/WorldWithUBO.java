package integeruser.jgltut.tut07;

import integeruser.jgltut.Tutorial;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2007%20World%20in%20Motion/World%20With%20UBO.cpp
 * <p>
 * Part II. Positioning
 * Chapter 7. World in Motion
 * <p>
 * Function                                     Increase/Left   Decrease/Right
 * Move camera target up/down                          E               Q
 * Move camera target horizontally                     A               D
 * Move camera target vertically                       W               S
 * Rotate camera horizontally around target            L               J
 * Rotate camera vertically around target              I               K
 * Move camera towards/away from target                U               O
 * In addition, if you hold down the SHIFT key while pressing any of the last six keys, then the affected control will
 * be much slower.
 * <p>
 * SPACE    - toggle the appearance of an object indicating the position of the camera point.
 */
public class WorldWithUBO extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut07/data/";
        new WorldWithUBO().start(700, 700);
    }


    @Override
    protected void init() {
        initializeProgram();

        try {
            coneMesh = new Mesh("UnitConeTint.xml");
            cylinderMesh = new Mesh("UnitCylinderTint.xml");
            cubeTintMesh = new Mesh("UnitCubeTint.xml");
            cubeColorMesh = new Mesh("UnitCubeColor.xml");
            planeMesh = new Mesh("UnitPlane.xml");
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
        glEnable(GL_DEPTH_CLAMP);


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_SPACE:
                        drawLookatPoint = !drawLookatPoint;
                        System.out.printf("Target: %f, %f, %f\n", camTarget.x, camTarget.y, camTarget.z);
                        System.out.printf("Position: %f, %f, %f\n", sphereCamRelPos.x, sphereCamRelPos.y, sphereCamRelPos.z);
                        break;

                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(window, true);
                        break;
                }
            }
        });
    }

    @Override
    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final Vector3f camPos = resolveCamPosition();

        MatrixStackf camMatrix = new MatrixStackf();
        camMatrix.mul(calcLookAtMatrix(camPos, camTarget, new Vector3f(0.0f, 1.0f, 0.0f)));

        glBindBuffer(GL_UNIFORM_BUFFER, globalMatricesUBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 16 * 4, camMatrix.get(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        MatrixStackf modelMatrix = new MatrixStackf(4);

        // Render the ground plane.
        {
            modelMatrix.pushMatrix();
            modelMatrix.scale(100.0f, 1.0f, 100.0f);

            glUseProgram(uniformColor.theProgram);
            glUniformMatrix4fv(uniformColor.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColor.baseColorUnif, 0.302f, 0.416f, 0.0589f, 1.0f);
            planeMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        // Draw the trees.
        drawForest(modelMatrix);

        // Draw the building.
        {
            modelMatrix.pushMatrix();
            modelMatrix.translate(20.0f, 0.0f, -10.0f);

            drawParthenon(modelMatrix);

            modelMatrix.popMatrix();
        }

        if (drawLookatPoint) {
            glDisable(GL_DEPTH_TEST);

            modelMatrix.pushMatrix();
            modelMatrix.translate(camTarget);
            modelMatrix.scale(1.0f, 1.0f, 1.0f);

            glUseProgram(objectColor.theProgram);
            glUniformMatrix4fv(objectColor.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            cubeColorMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();

            glEnable(GL_DEPTH_TEST);
        }
    }

    @Override
    protected void reshape(int w, int h) {
        float zNear = 1.0f;
        float zFar = 1000.0f;
        Matrix4f persMatrix = new Matrix4f();
        persMatrix.perspective((float) Math.toRadians(45.0f), (w / (float) h), zNear, zFar);

        glBindBuffer(GL_UNIFORM_BUFFER, globalMatricesUBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, persMatrix.get(mat4Buffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
        final float scale = 5;

        if (isKeyPressed(GLFW_KEY_W)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                camTarget.z = camTarget.z - 0.4f * lastFrameDuration * scale;
            } else {
                camTarget.z = camTarget.z - 4.0f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_S)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                camTarget.z = camTarget.z + 0.4f * lastFrameDuration * scale;
            } else {
                camTarget.z = camTarget.z + 4.0f * lastFrameDuration * scale;
            }
        }

        if (isKeyPressed(GLFW_KEY_D)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                camTarget.x = camTarget.x + 0.4f * lastFrameDuration * scale;
            } else {
                camTarget.x = camTarget.x + 4.0f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_A)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                camTarget.x = camTarget.x - 0.4f * lastFrameDuration * scale;
            } else {
                camTarget.x = camTarget.x - 4.0f * lastFrameDuration * scale;
            }
        }

        if (isKeyPressed(GLFW_KEY_E)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                camTarget.y = camTarget.y - 0.4f * lastFrameDuration * scale;
            } else {
                camTarget.y = camTarget.y - 4.0f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_Q)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                camTarget.y = camTarget.y + 0.4f * lastFrameDuration * scale;
            } else {
                camTarget.y = camTarget.y + 4.0f * lastFrameDuration * scale;
            }
        }


        if (isKeyPressed(GLFW_KEY_I)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.y = sphereCamRelPos.y - 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.y = sphereCamRelPos.y - 11.25f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_K)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.y = sphereCamRelPos.y + 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.y = sphereCamRelPos.y + 11.25f * lastFrameDuration * scale;
            }
        }

        if (isKeyPressed(GLFW_KEY_J)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.x = sphereCamRelPos.x - 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.x = sphereCamRelPos.x - 11.25f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_L)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.x = sphereCamRelPos.x + 1.125f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.x = sphereCamRelPos.x + 11.25f * lastFrameDuration * scale;
            }
        }

        if (isKeyPressed(GLFW_KEY_O)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.z = sphereCamRelPos.z - 0.5f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.z = sphereCamRelPos.z - 5.0f * lastFrameDuration * scale;
            }
        } else if (isKeyPressed(GLFW_KEY_U)) {
            if (isKeyPressed(GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                sphereCamRelPos.z = sphereCamRelPos.z + 0.5f * lastFrameDuration * scale;
            } else {
                sphereCamRelPos.z = sphereCamRelPos.z + 5.0f * lastFrameDuration * scale;
            }
        }


        sphereCamRelPos.y = Math.min(Math.max(sphereCamRelPos.y, -78.75f), -1.0f);
        camTarget.y = camTarget.y > 0.0f ? camTarget.y : 0.0f;
        sphereCamRelPos.z = sphereCamRelPos.z > 5.0f ? sphereCamRelPos.z : 5.0f;
    }

    ////////////////////////////////
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


    private void initializeProgram() {
        uniformColor = loadProgram("PosOnlyWorldTransformUBO.vert", "ColorUniform.frag");
        objectColor = loadProgram("PosColorWorldTransformUBO.vert", "ColorPassthrough.frag");
        uniformColorTint = loadProgram("PosColorWorldTransformUBO.vert", "ColorMultUniform.frag");

        globalMatricesUBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, globalMatricesUBO);
        glBufferData(GL_UNIFORM_BUFFER, 16 * 4 * 2, GL_STREAM_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        glBindBufferRange(GL_UNIFORM_BUFFER, globalMatricesBindingIndex, globalMatricesUBO, 0, 16 * 4 * 2);
    }

    private ProgramData loadProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToWorldMatrixUnif = glGetUniformLocation(data.theProgram, "modelToWorldMatrix");
        data.globalUniformBlockIndex = glGetUniformBlockIndex(data.theProgram, "GlobalMatrices");
        data.baseColorUnif = glGetUniformLocation(data.theProgram, "baseColor");

        glUniformBlockBinding(data.theProgram, data.globalUniformBlockIndex, globalMatricesBindingIndex);

        return data;
    }

    ////////////////////////////////
    private Mesh coneMesh;
    private Mesh cylinderMesh;
    private Mesh cubeTintMesh;
    private Mesh cubeColorMesh;
    private Mesh planeMesh;

    private boolean drawLookatPoint = false;
    private Vector3f camTarget = new Vector3f(0.0f, 0.4f, 0.0f);

    // In spherical coordinates.
    private Vector3f sphereCamRelPos = new Vector3f(67.5f, -46.0f, 150.0f);


    private Vector3f resolveCamPosition() {
        float phi = (float) Math.toRadians(sphereCamRelPos.x);
        float theta = (float) Math.toRadians(sphereCamRelPos.y + 90.0f);

        float sinTheta = (float) Math.sin(theta);
        float cosTheta = (float) Math.cos(theta);
        float cosPhi = (float) Math.cos(phi);
        float sinPhi = (float) Math.sin(phi);

        Vector3f dirToCamera = new Vector3f(sinTheta * cosPhi, cosTheta, sinTheta * sinPhi);
        return (dirToCamera.mul(sphereCamRelPos.z)).add(camTarget);
    }


    private Matrix4f calcLookAtMatrix(Vector3f cameraPt, Vector3f lookPt, Vector3f upPt) {
        Vector3f lookDir = new Vector3f(lookPt).sub(cameraPt).normalize();
        Vector3f upDir = new Vector3f(upPt).normalize();

        Vector3f rightDir = new Vector3f(lookDir).cross(upDir).normalize();
        Vector3f perpUpDir = new Vector3f(rightDir).cross(lookDir);

        Matrix4f rotMat = new Matrix4f();
        rotMat.m00(rightDir.x);
        rotMat.m01(rightDir.y);
        rotMat.m02(rightDir.z);
        rotMat.m10(perpUpDir.x);
        rotMat.m11(perpUpDir.y);
        rotMat.m12(perpUpDir.z);
        rotMat.m20(-lookDir.x);
        rotMat.m21(-lookDir.y);
        rotMat.m22(-lookDir.z);
        rotMat.transpose();

        Matrix4f transMat = new Matrix4f().setTranslation(-cameraPt.x, -cameraPt.y, -cameraPt.z);
        return rotMat.mul(transMat);
    }

    ////////////////////////////////
    private final TreeData forest[] = {
            new TreeData(-45.0f, -40.0f, 2.0f, 3.0f),
            new TreeData(-42.0f, -35.0f, 2.0f, 3.0f),
            new TreeData(-39.0f, -29.0f, 2.0f, 4.0f),
            new TreeData(-44.0f, -26.0f, 3.0f, 3.0f),
            new TreeData(-40.0f, -22.0f, 2.0f, 4.0f),
            new TreeData(-36.0f, -15.0f, 3.0f, 3.0f),
            new TreeData(-41.0f, -11.0f, 2.0f, 3.0f),
            new TreeData(-37.0f, -6.0f, 3.0f, 3.0f),
            new TreeData(-45.0f, 0.0f, 2.0f, 3.0f),
            new TreeData(-39.0f, 4.0f, 3.0f, 4.0f),
            new TreeData(-36.0f, 8.0f, 2.0f, 3.0f),
            new TreeData(-44.0f, 13.0f, 3.0f, 3.0f),
            new TreeData(-42.0f, 17.0f, 2.0f, 3.0f),
            new TreeData(-38.0f, 23.0f, 3.0f, 4.0f),
            new TreeData(-41.0f, 27.0f, 2.0f, 3.0f),
            new TreeData(-39.0f, 32.0f, 3.0f, 3.0f),
            new TreeData(-44.0f, 37.0f, 3.0f, 4.0f),
            new TreeData(-36.0f, 42.0f, 2.0f, 3.0f),

            new TreeData(-32.0f, -45.0f, 2.0f, 3.0f),
            new TreeData(-30.0f, -42.0f, 2.0f, 4.0f),
            new TreeData(-34.0f, -38.0f, 3.0f, 5.0f),
            new TreeData(-33.0f, -35.0f, 3.0f, 4.0f),
            new TreeData(-29.0f, -28.0f, 2.0f, 3.0f),
            new TreeData(-26.0f, -25.0f, 3.0f, 5.0f),
            new TreeData(-35.0f, -21.0f, 3.0f, 4.0f),
            new TreeData(-31.0f, -17.0f, 3.0f, 3.0f),
            new TreeData(-28.0f, -12.0f, 2.0f, 4.0f),
            new TreeData(-29.0f, -7.0f, 3.0f, 3.0f),
            new TreeData(-26.0f, -1.0f, 2.0f, 4.0f),
            new TreeData(-32.0f, 6.0f, 2.0f, 3.0f),
            new TreeData(-30.0f, 10.0f, 3.0f, 5.0f),
            new TreeData(-33.0f, 14.0f, 2.0f, 4.0f),
            new TreeData(-35.0f, 19.0f, 3.0f, 4.0f),
            new TreeData(-28.0f, 22.0f, 2.0f, 3.0f),
            new TreeData(-33.0f, 26.0f, 3.0f, 3.0f),
            new TreeData(-29.0f, 31.0f, 3.0f, 4.0f),
            new TreeData(-32.0f, 38.0f, 2.0f, 3.0f),
            new TreeData(-27.0f, 41.0f, 3.0f, 4.0f),
            new TreeData(-31.0f, 45.0f, 2.0f, 4.0f),
            new TreeData(-28.0f, 48.0f, 3.0f, 5.0f),

            new TreeData(-25.0f, -48.0f, 2.0f, 3.0f),
            new TreeData(-20.0f, -42.0f, 3.0f, 4.0f),
            new TreeData(-22.0f, -39.0f, 2.0f, 3.0f),
            new TreeData(-19.0f, -34.0f, 2.0f, 3.0f),
            new TreeData(-23.0f, -30.0f, 3.0f, 4.0f),
            new TreeData(-24.0f, -24.0f, 2.0f, 3.0f),
            new TreeData(-16.0f, -21.0f, 2.0f, 3.0f),
            new TreeData(-17.0f, -17.0f, 3.0f, 3.0f),
            new TreeData(-25.0f, -13.0f, 2.0f, 4.0f),
            new TreeData(-23.0f, -8.0f, 2.0f, 3.0f),
            new TreeData(-17.0f, -2.0f, 3.0f, 3.0f),
            new TreeData(-16.0f, 1.0f, 2.0f, 3.0f),
            new TreeData(-19.0f, 4.0f, 3.0f, 3.0f),
            new TreeData(-22.0f, 8.0f, 2.0f, 4.0f),
            new TreeData(-21.0f, 14.0f, 2.0f, 3.0f),
            new TreeData(-16.0f, 19.0f, 2.0f, 3.0f),
            new TreeData(-23.0f, 24.0f, 3.0f, 3.0f),
            new TreeData(-18.0f, 28.0f, 2.0f, 4.0f),
            new TreeData(-24.0f, 31.0f, 2.0f, 3.0f),
            new TreeData(-20.0f, 36.0f, 2.0f, 3.0f),
            new TreeData(-22.0f, 41.0f, 3.0f, 3.0f),
            new TreeData(-21.0f, 45.0f, 2.0f, 3.0f),

            new TreeData(-12.0f, -40.0f, 2.0f, 4.0f),
            new TreeData(-11.0f, -35.0f, 3.0f, 3.0f),
            new TreeData(-10.0f, -29.0f, 1.0f, 3.0f),
            new TreeData(-9.0f, -26.0f, 2.0f, 2.0f),
            new TreeData(-6.0f, -22.0f, 2.0f, 3.0f),
            new TreeData(-15.0f, -15.0f, 1.0f, 3.0f),
            new TreeData(-8.0f, -11.0f, 2.0f, 3.0f),
            new TreeData(-14.0f, -6.0f, 2.0f, 4.0f),
            new TreeData(-12.0f, 0.0f, 2.0f, 3.0f),
            new TreeData(-7.0f, 4.0f, 2.0f, 2.0f),
            new TreeData(-13.0f, 8.0f, 2.0f, 2.0f),
            new TreeData(-9.0f, 13.0f, 1.0f, 3.0f),
            new TreeData(-13.0f, 17.0f, 3.0f, 4.0f),
            new TreeData(-6.0f, 23.0f, 2.0f, 3.0f),
            new TreeData(-12.0f, 27.0f, 1.0f, 2.0f),
            new TreeData(-8.0f, 32.0f, 2.0f, 3.0f),
            new TreeData(-10.0f, 37.0f, 3.0f, 3.0f),
            new TreeData(-11.0f, 42.0f, 2.0f, 2.0f),

            new TreeData(15.0f, 5.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 10.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 15.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 20.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 25.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 30.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 35.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 40.0f, 2.0f, 3.0f),
            new TreeData(15.0f, 45.0f, 2.0f, 3.0f),

            new TreeData(25.0f, 5.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 10.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 15.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 20.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 25.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 30.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 35.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 40.0f, 2.0f, 3.0f),
            new TreeData(25.0f, 45.0f, 2.0f, 3.0f)
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


    private void drawForest(MatrixStackf modelMatrix) {
        for (TreeData currTree : forest) {
            modelMatrix.pushMatrix();
            modelMatrix.translate(currTree.xPos, 0.0f, currTree.zPos);

            drawTree(modelMatrix, currTree.trunkHeight, currTree.coneHeight);

            modelMatrix.popMatrix();
        }
    }

    // Trees are 3x3 in X/Z, and trunkHeight + coneHeight in the Y.
    private void drawTree(MatrixStackf modelMatrix, float trunkHeight, float coneHeight) {
        // Draw trunk.
        {
            modelMatrix.pushMatrix();
            modelMatrix.scale(1.0f, trunkHeight, 1.0f);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);

            glUseProgram(uniformColorTint.theProgram);
            glUniformMatrix4fv(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColorTint.baseColorUnif, 0.694f, 0.4f, 0.106f, 1.0f);
            cylinderMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        // Draw the treetop.
        {
            modelMatrix.pushMatrix();
            modelMatrix.translate(0.0f, trunkHeight, 0.0f);
            modelMatrix.scale(3.0f, coneHeight, 3.0f);

            glUseProgram(uniformColorTint.theProgram);
            glUniformMatrix4fv(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColorTint.baseColorUnif, 0.0f, 1.0f, 0.0f, 1.0f);
            coneMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }
    }

    ////////////////////////////////
    // Columns are 1x1 in the X/Z, and height units in the Y.
    private void drawColumn(MatrixStackf modelMatrix, float height) {
        final float columnBaseHeight = 0.25f;

        // Draw the bottom of the column.
        {
            modelMatrix.pushMatrix();
            modelMatrix.scale(1.0f, columnBaseHeight, 1.0f);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);

            glUseProgram(uniformColorTint.theProgram);
            glUniformMatrix4fv(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColorTint.baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
            cubeTintMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        // Draw the top of the column.
        {
            modelMatrix.pushMatrix();
            modelMatrix.translate(0.0f, height - columnBaseHeight, 0.0f);
            modelMatrix.scale(1.0f, columnBaseHeight, 1.0f);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);

            glUseProgram(uniformColorTint.theProgram);
            glUniformMatrix4fv(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            cubeTintMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        // Draw the main column.
        {
            modelMatrix.pushMatrix();
            modelMatrix.translate(0.0f, columnBaseHeight, 0.0f);
            modelMatrix.scale(0.8f, height - (columnBaseHeight * 2.0f), 0.8f);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);

            glUseProgram(uniformColorTint.theProgram);
            glUniformMatrix4fv(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            cylinderMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }
    }

    ////////////////////////////////
    private void drawParthenon(MatrixStackf modelMatrix) {
        final float parthenonWidth = 14.0f;
        final float parthenonLength = 20.0f;
        final float parthenonColumnHeight = 5.0f;
        final float parthenonBaseHeight = 1.0f;
        final float parthenonTopHeight = 2.0f;

        // Draw base.
        {
            modelMatrix.pushMatrix();
            modelMatrix.scale(parthenonWidth, parthenonBaseHeight, parthenonLength);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);

            glUseProgram(uniformColorTint.theProgram);
            glUniformMatrix4fv(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            cubeTintMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        // Draw top.
        {
            modelMatrix.pushMatrix();
            modelMatrix.translate(0.0f, parthenonColumnHeight + parthenonBaseHeight, 0.0f);
            modelMatrix.scale(parthenonWidth, parthenonTopHeight, parthenonLength);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);

            glUseProgram(uniformColorTint.theProgram);
            glUniformMatrix4fv(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
            cubeTintMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        // Draw columns.
        final float frontZVal = (parthenonLength / 2.0f) - 1.0f;
        final float rightXVal = (parthenonWidth / 2.0f) - 1.0f;
        for (int columnNum = 0; columnNum < (int) (parthenonWidth / 2.0f); columnNum++) {
            {
                modelMatrix.pushMatrix();
                modelMatrix.translate((2.0f * columnNum) - (parthenonWidth / 2.0f) + 1.0f, parthenonBaseHeight, frontZVal);

                drawColumn(modelMatrix, parthenonColumnHeight);

                modelMatrix.popMatrix();
            }
            {
                modelMatrix.pushMatrix();
                modelMatrix.translate((2.0f * columnNum) - (parthenonWidth / 2.0f) + 1.0f, parthenonBaseHeight, -frontZVal);

                drawColumn(modelMatrix, parthenonColumnHeight);

                modelMatrix.popMatrix();
            }
        }

        // Don't draw the first or last columns, since they've been drawn already.
        for (int columnNum = 1; columnNum < (int) ((parthenonLength - 2.0f) / 2.0f); columnNum++) {
            {
                modelMatrix.pushMatrix();
                modelMatrix.translate(rightXVal, parthenonBaseHeight, (2.0f * columnNum) - (parthenonLength / 2.0f) + 1.0f);

                drawColumn(modelMatrix, parthenonColumnHeight);

                modelMatrix.popMatrix();
            }
            {
                modelMatrix.pushMatrix();
                modelMatrix.translate(-rightXVal, parthenonBaseHeight, (2.0f * columnNum) - (parthenonLength / 2.0f) + 1.0f);

                drawColumn(modelMatrix, parthenonColumnHeight);

                modelMatrix.popMatrix();
            }
        }

        // Draw interior.
        {
            modelMatrix.pushMatrix();
            modelMatrix.translate(0.0f, 1.0f, 0.0f);
            modelMatrix.scale(parthenonWidth - 6.0f, parthenonColumnHeight, parthenonLength - 6.0f);
            modelMatrix.translate(0.0f, 0.5f, 0.0f);

            glUseProgram(objectColor.theProgram);
            glUniformMatrix4fv(objectColor.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            cubeColorMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }

        // Draw headpiece.
        {
            modelMatrix.pushMatrix();
            modelMatrix.translate(0.0f, parthenonColumnHeight + parthenonBaseHeight + (parthenonTopHeight / 2.0f), parthenonLength / 2.0f);
            modelMatrix.rotateX(-135.0f);
            modelMatrix.rotateY(45.0f);

            glUseProgram(objectColor.theProgram);
            glUniformMatrix4fv(objectColor.modelToWorldMatrixUnif, false, modelMatrix.get(mat4Buffer));
            cubeColorMesh.render();
            glUseProgram(0);

            modelMatrix.popMatrix();
        }
    }
}
