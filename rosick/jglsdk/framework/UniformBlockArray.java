package rosick.jglsdk.framework;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL31.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import rosick.PortingUtils.BufferableData;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class UniformBlockArray<T extends BufferableData<ByteBuffer>> {

	private byte[] m_storage;
	private int m_blockOffset;
	private int arrayCount;
	private int blockSize;
	
	
	public UniformBlockArray(int blockSize, int arrayCount) {
		this.arrayCount = arrayCount;
		this.blockSize = blockSize;
		
		int uniformBufferAlignSize = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

		m_blockOffset = blockSize;
		m_blockOffset += uniformBufferAlignSize - (m_blockOffset % uniformBufferAlignSize);

		m_storage = new byte[arrayCount * m_blockOffset];
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public int createBufferObject() {		
		int bufferObject = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, bufferObject);
		
		ByteBuffer tempByteBuffer = BufferUtils.createByteBuffer(m_storage.length);
		tempByteBuffer.put(m_storage);
		tempByteBuffer.flip();
		
		glBufferData(GL_UNIFORM_BUFFER, tempByteBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		return bufferObject;
	}
	
		
	public void set(int index, T data) {
		ByteBuffer tempByteBuffer = BufferUtils.createByteBuffer(blockSize);
		data.fillAndFlipBuffer(tempByteBuffer);
		
		byte temp[] = new byte[blockSize];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = tempByteBuffer.get(i);
		}
		
		System.arraycopy(temp, 0, m_storage, index * m_blockOffset, blockSize);
	}
	
	
	public int size() {
		return arrayCount;
	}
	
	
	public int getArrayOffset() {
		return m_blockOffset;
	}
}