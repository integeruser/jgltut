package org.jgltut.commons;

import org.joml.Vector4f;

import java.nio.ByteBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class MaterialBlock implements Bufferable<ByteBuffer> {
    public static final int SIZE_IN_BYTES = Float.BYTES * (4 + 4 + 1 + 3);

    public Vector4f diffuseColor;
    public Vector4f specularColor;
    public float specularShininess;
    public float padding[] = new float[3];

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        buffer.putFloat(diffuseColor.x);
        buffer.putFloat(diffuseColor.y);
        buffer.putFloat(diffuseColor.z);
        buffer.putFloat(diffuseColor.w);
        buffer.putFloat(specularColor.x);
        buffer.putFloat(specularColor.y);
        buffer.putFloat(specularColor.z);
        buffer.putFloat(specularColor.w);
        buffer.putFloat(specularShininess);
        for (int i = 0; i < 3; i++) {
            buffer.putFloat(padding[i]);
        }
        return buffer;
    }
}
