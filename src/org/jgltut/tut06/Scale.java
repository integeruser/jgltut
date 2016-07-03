package org.jgltut.tut06;

import org.jgltut.LWJGLWindow;
import org.jgltut.framework.Framework;
import org.jglsdk.glm.Glm;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 * <p>
 * Part II. Positioning
 * Chapter 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 *
 * @author integeruser
 */
public class Scale extends LWJGLWindow {
    public static void main(String[] args) {
        Framework.CURRENT_TUTORIAL_DATAPATH = "/org/jgltut/tut06/data/";
        new Scale().start(500, 500);
    }


    @Override
    protected void init() {
        initializeProgram();
        initializeVertexBuffer();

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        final int numberOfVertices = 8;
        int colorDataOffset = FLOAT_SIZE * 3 * numberOfVertices;
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

        glBindVertexArray(0);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRange(0.0f, 1.0f);
    }

    @Override
    protected void display() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(theProgram);

        glBindVertexArray(vao);

        for (Instance currInst : instanceList) {
            final Matrix4f transformMatrix = currInst.constructMatrix(elapsedTime);
            glUniformMatrix4fv(modelToCameraMatrixUnif, false, transformMatrix.get(mat4Buffer));
            glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
        }

        glBindVertexArray(0);
        glUseProgram(0);
    }

    @Override
    protected void reshape(int w, int h) {
        cameraToClipMatrix.m00(frustumScale * (h / (float) w));
        cameraToClipMatrix.m11(frustumScale);

        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);

        glViewport(0, 0, w, h);
    }

    @Override
    protected void update() {
    }

    ////////////////////////////////
    private int theProgram;

    private int modelToCameraMatrixUnif;
    private int cameraToClipMatrixUnif;

    private Matrix4f cameraToClipMatrix = new Matrix4f();
    private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);

    private final float frustumScale = calcFrustumScale(45.0f);


    private void initializeProgram() {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, "PosColorLocalTransform.vert"));
        shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "ColorPassthrough.frag"));
        theProgram = Framework.createProgram(shaderList);

        modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
        cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");

        float zNear = 1.0f;
        float zFar = 61.0f;
        cameraToClipMatrix.m00(frustumScale);
        cameraToClipMatrix.m11(frustumScale);
        cameraToClipMatrix.m22((zFar + zNear) / (zNear - zFar));
        cameraToClipMatrix.m23(-1.0f);
        cameraToClipMatrix.m32((2 * zFar * zNear) / (zNear - zFar));

        glUseProgram(theProgram);
        glUniformMatrix4fv(cameraToClipMatrixUnif, false, cameraToClipMatrix.get(mat4Buffer));
        glUseProgram(0);
    }


    private float calcFrustumScale(float fovDeg) {
        float fovRad = (float) Math.toRadians(fovDeg);
        return (float) (1.0f / Math.tan(fovRad / 2.0f));
    }

    ////////////////////////////////
    private final float vertexData[] = {
            +1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,

            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR
            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR
            0.5f, 0.5f, 0.0f, 1.0f,  // BROWN_COLOR

            0.0f, 1.0f, 0.0f, 1.0f,  // GREEN_COLOR
            0.0f, 0.0f, 1.0f, 1.0f,  // BLUE_COLOR
            1.0f, 0.0f, 0.0f, 1.0f,  // RED_COLOR
            0.5f, 0.5f, 0.0f, 1.0f   // BROWN_COLOR
    };

    private final short indexData[] = {
            0, 1, 2,
            1, 0, 3,
            2, 3, 0,
            3, 2, 1,

            5, 4, 6,
            4, 5, 7,
            7, 6, 4,
            6, 7, 5
    };

    private int vertexBufferObject;
    private int indexBufferObject;

    private int vao;


    private void initializeVertexBuffer() {
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(vertexData.length);
        vertexDataBuffer.put(vertexData);
        vertexDataBuffer.flip();

        vertexBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        ShortBuffer indexDataBuffer = BufferUtils.createShortBuffer(indexData.length);
        indexDataBuffer.put(indexData);
        indexDataBuffer.flip();

        indexBufferObject = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    ////////////////////////////////
    private Instance instanceList[] = {
            new NullScale(new Vector3f(0.0f, 0.0f, -45.0f)),
            new StaticUniformScale(new Vector3f(-10.0f, -10.0f, -45.0f)),
            new StaticNonUniformScale(new Vector3f(-10.0f, 10.0f, -45.0f)),
            new DynamicUniformScale(new Vector3f(10.0f, 10.0f, -45.0f)),
            new DynamicNonUniformScale(new Vector3f(10.0f, -10.0f, -45.0f))
    };

    private abstract class Instance {
        private Vector3f offset;

        Instance(Vector3f offset) {
            this.offset = offset;
        }

        abstract Vector3f calcScale(float elapsedTime);

        Matrix4f constructMatrix(float elapsedTime) {
            Vector3f theScale = calcScale(elapsedTime);

            Matrix4f theMat = new Matrix4f();
            theMat.m00(theScale.x);
            theMat.m11(theScale.y);
            theMat.m22(theScale.z);
            theMat.setTranslation(offset);
            return theMat;
        }
    }

    private class NullScale extends Instance {
        NullScale(Vector3f offset) {
            super(offset);
        }

        @Override
        Vector3f calcScale(float elapsedTime) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
    }

    private class StaticUniformScale extends Instance {
        StaticUniformScale(Vector3f offset) {
            super(offset);
        }

        @Override
        Vector3f calcScale(float elapsedTime) {
            return new Vector3f(4.0f, 4.0f, 4.0f);
        }
    }

    private class StaticNonUniformScale extends Instance {
        StaticNonUniformScale(Vector3f offset) {
            super(offset);
        }

        @Override
        Vector3f calcScale(float elapsedTime) {
            return new Vector3f(0.5f, 1.0f, 10.0f);
        }
    }

    private class DynamicUniformScale extends Instance {
        DynamicUniformScale(Vector3f offset) {
            super(offset);
        }

        @Override
        Vector3f calcScale(float elapsedTime) {
            final float loopDuration = 3.0f;
            return new Vector3f(Glm.mix(1.0f, 4.0f, calcLerpFactor(elapsedTime, loopDuration)));
        }
    }

    private class DynamicNonUniformScale extends Instance {
        DynamicNonUniformScale(Vector3f offset) {
            super(offset);
        }

        @Override
        Vector3f calcScale(float elapsedTime) {
            final float xLoopDuration = 3.0f;
            final float zLoopDuration = 5.0f;
            return new Vector3f(
                    Glm.mix(1.0f, 0.5f, calcLerpFactor(elapsedTime, xLoopDuration)),
                    1.0f,
                    Glm.mix(1.0f, 10.0f, calcLerpFactor(elapsedTime, zLoopDuration))
            );
        }
    }


    private float calcLerpFactor(float elapsedTime, float loopDuration) {
        float value = (elapsedTime % loopDuration) / loopDuration;
        if (value > 0.5f) {
            value = 1.0f - value;
        }
        return value * 2.0f;
    }
}