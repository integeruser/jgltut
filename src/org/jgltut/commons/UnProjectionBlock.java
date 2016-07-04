package org.jgltut.commons;

import org.jgltut.Bufferable;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.nio.ByteBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class UnProjectionBlock implements Bufferable<ByteBuffer> {
    public static final int SIZE = 4 * (16 + 2);

    public Matrix4f clipToCameraMatrix;
    public Vector2i windowSize;

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        float[] matrix = new float[16];
        clipToCameraMatrix.get(matrix);
        for (float f : matrix) {
            buffer.putFloat(f);
        }

        buffer.putInt(windowSize.x);
        buffer.putInt(windowSize.y);
        return buffer;
    }
}
