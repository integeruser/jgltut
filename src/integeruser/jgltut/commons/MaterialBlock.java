package integeruser.jgltut.commons;

import org.joml.Vector4f;

import java.nio.ByteBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class MaterialBlock implements Bufferable {
    public static final int SIZE_IN_BYTES = Float.BYTES * (4 + 4 + 1 + 3);

    public Vector4f diffuseColor;
    public Vector4f specularColor;
    public float specularShininess;
    public float padding[] = new float[3];

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        diffuseColor.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 4);

        specularColor.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 4);

        buffer.putFloat(specularShininess);

        buffer.putFloat(padding[0]);
        buffer.putFloat(padding[1]);
        buffer.putFloat(padding[2]);
        return buffer;
    }
}
