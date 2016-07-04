package org.jgltut.commons;

import org.joml.Vector4f;

import java.nio.FloatBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class LightBlock implements Bufferable<FloatBuffer> {
    public static final int MAX_NUMBER_OF_LIGHTS = 5;
    public static final int SIZE_IN_BYTES = Float.BYTES * (4 + 1 + 1 + 2) + PerLight.SIZE_IN_BYTES * MAX_NUMBER_OF_LIGHTS;

    public Vector4f ambientIntensity;
    public float lightAttenuation;
    public float maxIntensity;
    public float padding[] = new float[2];
    public PerLight lights[] = new PerLight[MAX_NUMBER_OF_LIGHTS];

    @Override
    public FloatBuffer get(FloatBuffer buffer) {
        buffer.put(ambientIntensity.x);
        buffer.put(ambientIntensity.y);
        buffer.put(ambientIntensity.z);
        buffer.put(ambientIntensity.w);
        buffer.put(lightAttenuation);
        buffer.put(maxIntensity);
        buffer.put(padding);
        for (PerLight light : lights) {
            if (light == null) break;
            light.get(buffer);
        }
        return buffer;
    }
}
