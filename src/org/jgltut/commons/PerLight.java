package org.jgltut.commons;

import org.joml.Vector4f;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class PerLight implements Bufferable<FloatBuffer> {
    public static final int SIZE = 4 * (4 + 4);

    public Vector4f cameraSpaceLightPos;
    public Vector4f lightIntensity;

    @Override
    public FloatBuffer get(FloatBuffer buffer) {
        buffer.put(cameraSpaceLightPos.x);
        buffer.put(cameraSpaceLightPos.y);
        buffer.put(cameraSpaceLightPos.z);
        buffer.put(cameraSpaceLightPos.w);
        buffer.put(lightIntensity.x);
        buffer.put(lightIntensity.y);
        buffer.put(lightIntensity.z);
        buffer.put(lightIntensity.w);
        return buffer;
    }
}
