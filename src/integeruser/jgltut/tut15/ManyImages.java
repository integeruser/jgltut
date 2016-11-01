package integeruser.jgltut.tut15;

import integeruser.jglsdk.glimg.DdsLoader;
import integeruser.jglsdk.glimg.ImageSet;
import integeruser.jgltut.Tutorial;
import integeruser.jgltut.commons.ProjectionBlock;
import integeruser.jgltut.framework.Framework;
import integeruser.jgltut.framework.Mesh;
import integeruser.jgltut.framework.Timer;
import org.joml.Matrix4f;
import org.joml.MatrixStackf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
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
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.*;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/Tut%2015%20Many%20Images/Many%20Images.cpp
 * <p>
 * Part IV. Texturing
 * Chapter 15. Many Images
 * <p>
 * SPACE        - toggle between loaded/constructed texture.
 * Y            - toggle between plane/corridor mesh.
 * P            - toggle pausing.
 * 1,2,3,4,5,6  - switch filtering technique.
 */
public class ManyImages extends Tutorial {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/integeruser/jgltut/tut15/data/";
        new ManyImages().start(500, 500);
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
        loadMipmapTexture();
        createSamplers();


        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_SPACE:
                        useMipmapTexture = !useMipmapTexture;
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

                if (GLFW_KEY_1 <= key && key <= GLFW_KEY_9) {
                    int number = key - GLFW_KEY_1;
                    if (number < NUM_SAMPLERS) {
                        System.out.printf("Sampler: %s\n", samplerNames[number]);
                        currSampler = number;
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

        MatrixStackf modelMatrix = new MatrixStackf(10);

        Vector3f eye = new Vector3f(hOffset, 1.0f, -64.0f);
        Vector3f center = new Vector3f(hOffset, -5.0f + vOffset, -44.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        final Matrix4f worldToCamMat = new Matrix4f().lookAt(eye, center, up);

        modelMatrix.mul(worldToCamMat);

        glUseProgram(program.theProgram);
        glUniformMatrix4fv(program.modelToCameraMatrixUnif, false, modelMatrix.get(mat4Buffer));

        glActiveTexture(GL_TEXTURE0 + colorTexUnit);
        glBindTexture(GL_TEXTURE_2D, useMipmapTexture ? mipmapTestTexture : checkerTexture);
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
    private ProgramData program;

    private class ProgramData {
        int theProgram;

        int modelToCameraMatrixUnif;
    }


    private void initializePrograms() {
        program = loadProgram("PT.vert", "Tex.frag");
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

    private final int NUM_SAMPLERS = 6;
    private int[] samplers = new int[NUM_SAMPLERS];
    private int currSampler;

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

    private Timer camTimer = new Timer(Timer.Type.LOOP, 5.0f);

    private boolean useMipmapTexture;
    private boolean drawCorridor;


    private void loadMipmapTexture() {
        mipmapTestTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, mipmapTestTexture);

        int oldAlign = glGetInteger(GL_UNPACK_ALIGNMENT);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        for (int mipmapLevel = 0; mipmapLevel < 8; mipmapLevel++) {
            int width = 128 >> mipmapLevel;
            int height = 128 >> mipmapLevel;
            ArrayList<Byte> texture = new ArrayList<>();

            final int currColor = mipmapLevel * 3;
            fillWithColor(texture, mipmapColors[currColor], mipmapColors[currColor + 1], mipmapColors[currColor + 2], width, height);

            ByteBuffer textureBuffer = BufferUtils.createByteBuffer(texture.size());
            for (Byte b : texture) {
                textureBuffer.put(b);
            }
            textureBuffer.flip();

            glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_RGB8, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, textureBuffer);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, oldAlign);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 7);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void fillWithColor(ArrayList<Byte> buffer, byte red, byte green, byte blue, int width, int height) {
        int numTexels = width * height;
        for (int i = 0; i < numTexels * 3; i++) {
            buffer.add(red);
            buffer.add(green);
            buffer.add(blue);
        }
    }


    private void loadCheckerTexture() {
        try {
            String filePath = Framework.findFileOrThrow("checker.dds");
            ImageSet imageSet = DdsLoader.loadFromFile(filePath);

            checkerTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, checkerTexture);

            for (int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
                ImageSet.SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
                ImageSet.Dimensions imageDimensions = image.getDimensions();

                glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_RGB8, imageDimensions.width, imageDimensions.height, 0,
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

        // Nearest
        glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        // Linear
        glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        // Linear mipmap Nearest
        glSamplerParameteri(samplers[2], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[2], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);

        // Linear mipmap linear
        glSamplerParameteri(samplers[3], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[3], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        // Low anisotropic
        glSamplerParameteri(samplers[4], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[4], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameterf(samplers[4], GL_TEXTURE_MAX_ANISOTROPY_EXT, 4.0f);

        // Max anisotropic
        float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
        System.out.printf("Maximum anisotropy: %f\n", maxAniso);

        glSamplerParameteri(samplers[5], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(samplers[5], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameterf(samplers[5], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
    }

    ////////////////////////////////
    private final int projectionBlockIndex = 0;

    private int projectionUniformBuffer;
}
