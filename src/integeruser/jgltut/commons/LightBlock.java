package integeruser.jgltut.commons;

import org.joml.Vector4f;

import java.nio.ByteBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public class LightBlock implements Bufferable {
    public static final int MAX_NUMBER_OF_LIGHTS = 5;
    public static final int SIZE_IN_BYTES = Float.BYTES * (4 + 1 + 1 + 2) + PerLight.SIZE_IN_BYTES * MAX_NUMBER_OF_LIGHTS;

    public Vector4f ambientIntensity;
    public float lightAttenuation;
    public float maxIntensity;
    public float padding[] = new float[2];
    public PerLight lights[] = new PerLight[MAX_NUMBER_OF_LIGHTS];

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        ambientIntensity.get(buffer);
        buffer.position(buffer.position() + Float.BYTES * 4);

        buffer.putFloat(lightAttenuation);

        buffer.putFloat(maxIntensity);

        buffer.putFloat(padding[0]);
        buffer.putFloat(padding[1]);

        for (PerLight light : lights) {
            if (light == null) break;
            light.get(buffer);
        }
        return buffer;
    }
}
