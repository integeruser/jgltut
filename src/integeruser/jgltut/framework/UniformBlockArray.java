package integeruser.jgltut.framework;

import integeruser.jgltut.commons.Bufferable;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 * Original: https://bitbucket.org/alfonse/gltut/src/default/framework/UniformBlockArray.h
 */
public class UniformBlockArray<T extends Bufferable> {
    public UniformBlockArray(int blockSize, int arrayCount) {
        this.blockSize = blockSize;

        int uniformBufferAlignSize = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

        blockOffset = blockSize;
        blockOffset += uniformBufferAlignSize - (blockOffset % uniformBufferAlignSize);

        storage = new byte[arrayCount * blockOffset];
    }

    ////////////////////////////////
    public int createBufferObject() {
        int bufferObject = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, bufferObject);

        ByteBuffer storageBuffer = BufferUtils.createByteBuffer(storage.length);
        storageBuffer.put(storage);
        storageBuffer.flip();

        glBufferData(GL_UNIFORM_BUFFER, storageBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return bufferObject;
    }


    /**
     * copy data in storage[index]
     */
    public void set(int index, T data) {
        ByteBuffer tempByteBuffer = BufferUtils.createByteBuffer(blockSize);
        data.getAndFlip(tempByteBuffer);

        byte temp[] = new byte[blockSize];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = tempByteBuffer.get(i);
        }

        System.arraycopy(temp, 0, storage, index * blockOffset, blockSize);
    }


    public int getArrayOffset() {
        return blockOffset;
    }

    ////////////////////////////////
    private byte[] storage;
    private int blockOffset;
    private int blockSize;
}
