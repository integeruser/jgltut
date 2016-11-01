package integeruser.jgltut.commons;

import java.nio.ByteBuffer;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 */
public interface Bufferable {
    ByteBuffer get(ByteBuffer buffer);

    default ByteBuffer getAndFlip(ByteBuffer buffer) {
        buffer.clear();
        get(buffer);
        buffer.flip();
        return buffer;
    }
}
