package integeruser.jgltut.tut14;

import integeruser.jgltut.Tutorial;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import org.joml.MatrixStackf;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part IV. Texturing
 * Chapter 14. Textures are not Pictures
 * <p>
 * S        - switch meshes.
 * P        - toggle between perspective-correct interpolation/window-space linear interpolation.
 */
public class PerspectiveInterpolation extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut14/data/";
        new PerspectiveInterpolation().start(500, 500);
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            realHallway = new Mesh("RealHallway.xml");
            fauxHallway = new Mesh("FauxHallway.xml");
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_S:
                            useFakeHallway = !useFakeHallway;
                            if (useFakeHallway) {
                                System.out.printf("Fake Hallway.\n");
                            } else {
                                System.out.printf("Real Hallway.\n");
                            }
                            break;

                        case GLFW_KEY_P:
                            useSmoothInterpolation = !useSmoothInterpolation;
                            if (useSmoothInterpolation) {
                                System.out.printf("Perspective correct interpolation.\n");
                            } else {
                                System.out.printf("Just linear interpolation.\n");
                            }
                            break;

                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, GL_TRUE);
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (useSmoothInterpolation) {
            glUseProgram(smoothInterp.theProgram);
        } else {
            glUseProgram(linearInterp.theProgram);
        }

        if (useFakeHallway) {
            fauxHallway.render();
        } else {
            realHallway.render();
        }

        glUseProgram(0);
    }

    @Override
    protected void reshape(int w, int h) {
        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
    }

    ////////////////////////////////
    private ProgramData smoothInterp;
    private ProgramData linearInterp;

    private class ProgramData {
        int theProgram;

        int cameraToClipMatrixUnif;
    }


    private void initializePrograms() {
        smoothInterp = loadProgram("SmoothVertexColors.vert", "SmoothVertexColors.frag");
        linearInterp = loadProgram("NoCorrectVertexColors.vert", "NoCorrectVertexColors.frag");

        float zNear = 1.0f;
        float zFar = 1000.0f;
        MatrixStackf persMatrix = new MatrixStackf();
        persMatrix.perspective((float) Math.toRadians(60.0f), 1.0f, zNear, zFar);

        glUseProgram(smoothInterp.theProgram);
        glUniformMatrix4fv(smoothInterp.cameraToClipMatrixUnif, false, persMatrix.get(mat4Buffer));
        glUseProgram(linearInterp.theProgram);
        glUniformMatrix4fv(linearInterp.cameraToClipMatrixUnif, false, persMatrix.get(mat4Buffer));
        glUseProgram(0);
    }

    private ProgramData loadProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.cameraToClipMatrixUnif = glGetUniformLocation(data.theProgram, "cameraToClipMatrix");
        return data;
    }

    ////////////////////////////////
    private Mesh realHallway;
    private Mesh fauxHallway;

    private boolean useSmoothInterpolation = true;
    private boolean useFakeHallway;
}
