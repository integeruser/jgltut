package integeruser.jgltut.commons;

import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.nio.ByteBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 */
public class UnprojectionBlock implements Bufferable {
    public static final int SIZE_IN_BYTES = Float.BYTES * (16) + Integer.BYTES * (2);

    public Matrix4f clipToCameraMatrix;
    public Vector2i windowSize;

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        clipToCameraMatrix.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 16);

        windowSize.get(buffer);
        buffer.position(buffer.position() + Integer.BYTES * 2);
        return buffer;
    }
}
