package org.jgltut.commons;

import org.joml.Matrix4f;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class ProjectionBlock implements Bufferable<FloatBuffer> {
    public static final int SIZE = 4 * (16);

    public Matrix4f cameraToClipMatrix;

    @Override
    public FloatBuffer get(FloatBuffer buffer) {
        cameraToClipMatrix.get(buffer);
        buffer.position(buffer.position() + 16);
        return buffer;
    }
}
