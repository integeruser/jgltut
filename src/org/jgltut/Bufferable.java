package org.jgltut;

import java.nio.Buffer;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
public interface Bufferable<T extends Buffer> {
    T get(T buffer);

    default T getAndFlip(T buffer) {
        buffer.clear();
        get(buffer);
        buffer.flip();
        return buffer;
    }
}
