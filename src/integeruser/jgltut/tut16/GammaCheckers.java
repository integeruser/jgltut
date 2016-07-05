package integeruser.jgltut.tut16;

import integeruser.jglsdk.glimg.DdsLoader;
import integeruser.jglsdk.glimg.ImageSet;
import integeruser.jglsdk.glimg.ImageSet.Dimensions;
import integeruser.jglsdk.glimg.ImageSet.SingleImage;
import integeruser.jgltut.Tutorial;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import integeruser.jgltut.framework.Timer;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.GL_SRGB8;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part IV. Texturing
 * Chapter 16. Gamma and Textures
 * <p>
 * A        - toggle gamma correction.
 * G        - switch to a texture who's mipmaps were properly generated.
 * SPACE    - press A and G keys.
 * Y        - toggle between plane/corridor mesh.
 * P        - toggle pausing.
 * 1,2      - select linear mipmap filtering and anisotropic filtering (using the maximum possible anisotropy).
 */
public class GammaCheckers extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut16/data/";
        new GammaCheckers().start(500, 500);
    }


    @Override
    protected void init() {
        initializePrograms();

        try {
            corridor = new Mesh("Corridor.xml");
            plane = new Mesh("BigPlane.xml");
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        final float depthZNear = 0.0f;
        final float depthZFar = 1.0f;

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(depthZNear, depthZFar);
        glEnable(GL_DEPTH_CLAMP);

        // Setup our Uniform Buffers
        projectionUniformBuffer = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferData(GL_UNIFORM_BUFFER, ProjectionBlock.SIZE_IN_BYTES, GL_DYNAMIC_DRAW);

        glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, ProjectionBlock.SIZE_IN_BYTES);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        loadCheckerTexture();
        createSamplers();


        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    boolean particularKeyPressed = false;
                    switch (key) {
                        case GLFW_KEY_A:
                            drawGammaProgram = !drawGammaProgram;
                            particularKeyPressed = true;
                            break;

                        case GLFW_KEY_G:
                            drawGammaTexture = !drawGammaTexture;
                            particularKeyPressed = true;
                            break;

                        case GLFW_KEY_SPACE:
                            drawGammaProgram = !drawGammaProgram;
                            drawGammaTexture = !drawGammaTexture;
                            particularKeyPressed = true;
                            break;

                        case GLFW_KEY_Y:
                            drawCorridor = !drawCorridor;
                            break;

                        case GLFW_KEY_P:
                            camTimer.togglePause();
                            break;

                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, true);
                            break;
                    }

                    if (particularKeyPressed) {
                        System.out.printf("----\n");
                        System.out.printf("Rendering:\t\t%s\n", drawGammaProgram ? "Gamma" : "Linear");
                        System.out.printf("Mipmap Generation:\t%s\n", drawGammaTexture ? "Gamma" : "Linear");
                    }

                    if (GLFW_KEY_1 <= key && key <= GLFW_KEY_9) {
                        int number = key - GLFW_KEY_1;
                        if (number < NUM_SAMPLERS) {
                            currSampler = number;
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void display() {
        glClearColor(0.75f, 0.75f, 1.0f, 1.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        camTimer.update(elapsedTime);

        float cyclicAngle = camTimer.getAlpha() * 6.28f;
        float hOffset = (float) (Math.cos(cyclicAngle) * 0.25f);
        float vOffset = (float) (Math.sin(cyclicAngle) * 0.25f);

        Vector3f eye = new Vector3f(hOffset, 1.0f, -64.0f);
        Vector3f center = new Vector3f(hOffset, -5.0f + vOffset, -44.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        final Matrix4f worldToCamMat = new Matrix4f().lookAt(eye, center, up);

        MatrixStackf modelMatrix = new MatrixStackf(10);
        modelMatrix.mul(worldToCamMat);

        final ProgramData prog = drawGammaProgram ? progGamma : progNoGamma;

        glUseProgram(prog.theProgram);
        glUniformMatrix4fv(prog.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

        glActiveTexture(GL_TEXTURE0 + colorTexUnit);
        glBindTexture(GL_TEXTURE_2D, drawGammaTexture ? gammaTexture : linearTexture);
        glBindSampler(colorTexUnit, samplers[currSampler]);

        if (drawCorridor) {
            corridor.render("tex");
        } else {
            plane.render("tex");
        }

        glBindSampler(colorTexUnit, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glUseProgram(0);
    }

    @Override
    protected void reshape(int w, int h) {
        float zNear = 1.0f;
        float zFar = 1000.0f;
        Matrix4f persMatrix = new Matrix4f();
        persMatrix.perspective((float) Math.toRadians(90.0f), (w / (float) h), zNear, zFar);

        ProjectionBlock projData = new ProjectionBlock();
        projData.cameraToClipMatrix = persMatrix;

        glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projData.getAndFlip(projectionBlockBuffer));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
    }

    ////////////////////////////////
    private ProgramData progNoGamma;
    private ProgramData progGamma;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;
    }


    private void initializePrograms() {
        progNoGamma = loadProgram("PT.vert", "textureNoGamma.frag");
        progGamma = loadProgram("PT.vert", "textureGamma.frag");
    }

    private ProgramData loadProgram(String vertexShaderFileName, String fragmentShaderFileName) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, vertexShaderFileName));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName));

        ProgramData data = new ProgramData();
        data.theProgram = Framework.createProgram(shaderList);
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "modelToCameraMatrix");

        int projectionBlock = glGetUniformBlockIndex(data.theProgram, "Projection");
        glUniformBlockBinding(data.theProgram, projectionBlock, projectionBlockIndex);

        int colorTextureUnif = glGetUniformLocation(data.theProgram, "colorTexture");
        glUseProgram(data.theProgram);
        glUniform1i(colorTextureUnif, colorTexUnit);
        glUseProgram(0);

        return data;
    }

    ////////////////////////////////
    private Mesh plane;
    private Mesh corridor;

    private final int colorTexUnit = 0;

    private final int NUM_SAMPLERS = 2;
    private int[] samplers = new int[NUM_SAMPLERS];
    private int currSampler;

    private int linearTexture;
    private int gammaTexture;

    private Timer camTimer = new Timer(Timer.Type.LOOP, 5.0f);

    private boolean drawCorridor;
    private boolean drawGammaTexture;
    private boolean drawGammaProgram;


    private void loadCheckerTexture() {
        try {
            String filePath = Framework.findFileOrThrow("checker_linear.dds");
            ImageSet imageSet = DdsLoader.loadFromFile(filePath);

            linearTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, linearTexture);

            for (int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
                SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
                Dimensions imageDimensions = image.getDimensions();

                glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8, imageDimensions.width, imageDimensions.height, 0,
                        GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData());
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);


            filePath = Framework.findFileOrThrow("checker_gamma.dds");
            imageSet = DdsLoader.loadFromFile(filePath);

            gammaTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, gammaTexture);

            for (int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
                SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
                Dimensions imageDimensions = image.getDimensions();

                glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8, imageDimensions.width, imageDimensions.height, 0,
                        GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData());
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);

            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private void createSamplers() {
        for (int samplerIndex = 0; samplerIndex < NUM_SAMPLERS; samplerIndex++) {
            samplers[samplerIndex] = glGenSamplers();
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_S, GL_REPEAT);
            glSamplerParameteri(samplers[samplerIndex], GL_TEXTURE_WRAP_T, GL_REPEAT);
        }

        // Linear mipmap linear
        glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        // Max anisotropic
        float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);

        glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameterf(samplers[1], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
    }

    ////////////////////////////////
    private final int projectionBlockIndex = 0;

    private int projectionUniformBuffer;
}
