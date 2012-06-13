package rosick.jglsdk.glimg;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;


/**
 * Visit https://github.com/integeruser/jglsdk for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ImageSet {
	
	public static class Dimensions {
		public int numDimensions;			// The number of dimensions of an image. Can be 1, 2, or 3.
		public int width;					// The width of the image. Always valid.
		public int height;					// The height of the image. Only valid if numDimensions is 2 or 3.
		public int depth;					// The depth of the image. Only valid if numDimensions is 3.

		
		public Dimensions() {
		}
		
		public Dimensions(Dimensions dimensions) {
			numDimensions = dimensions.numDimensions;
			width = dimensions.width;
			height = dimensions.height;
			depth = dimensions.depth;
		}


		// Computes the number of rows of pixel data in the image.
		public int calcNumLines() {
			switch (numDimensions) {
			case 1:
				return 1;
			case 2:
				return height;
			case 3:
				return depth * height;
			}

			// Should not be possible.
			return -1;
		}
	};

	
	public static class SingleImage {
	
		public Dimensions getDimensions() {
			return m_imageSet.getDimensions(m_mipmapLevel);
		}

		public ByteBuffer getImageData() {
			return m_imageSet.getImageData(m_mipmapLevel, m_arrayIx, m_faceIx);
		}
		
		
		public final int getSize() {
			return m_imageSet.getSize(m_mipmapLevel);
		}
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

		private ImageSet m_imageSet;
		private int m_arrayIx, m_faceIx, m_mipmapLevel;
		
		
		private SingleImage(ImageSet imageSet, int mipmapLevel, int arrayIx, int faceIx) {
			m_imageSet = imageSet;
			m_arrayIx = arrayIx;
			m_faceIx = faceIx;
			m_mipmapLevel = mipmapLevel;
		}
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public final int getMipmapCount() {
		return m_mipmapCount;
	}

	public final ImageFormat getFormat() {
		return m_format;
	}
	
	
	public final Dimensions getDimensions() {
		return m_dimensions;
	}
	
	public final Dimensions getDimensions(int mipmapLevel) {
		return Util.calcMipmapLevelDimensions(m_dimensions, mipmapLevel);
	}

	
	public final SingleImage getImage(int mipmapLevel, int arrayIx, int faceIx) {
		return new SingleImage(this, mipmapLevel, arrayIx, faceIx);		
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	ImageSet(ImageFormat imageFormat, Dimensions imageDimensions, int mipmapCount, int arrayCount, int faceCount, 
			ArrayList<byte[]> imageData, int[] imageSizes) {
		m_format = imageFormat;
		m_dimensions = imageDimensions;
		m_imageData = imageData;
		m_imageSizes = imageSizes;
		m_arrayCount = arrayCount;
		m_mipmapCount = mipmapCount;
		m_faceCount = faceCount;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	final int getArrayCount() {
		return m_arrayCount;
	}
	
	final int getFaceCount() {
		return m_faceCount;
	}
	
	
	final int getSize(int mipmap) {
		return m_imageSizes[mipmap];
	}
	
	
	final ByteBuffer getImageData(int mipmapLevel, int arrayIx, int faceIx) {	
		byte[] imageData = m_imageData.get(mipmapLevel);
		int imageDataOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes[mipmapLevel];
				
		ByteBuffer imageDataBuffer = BufferUtils.createByteBuffer(m_imageSizes[mipmapLevel]);
		for (int i = imageDataOffset; i < imageDataOffset + m_imageSizes[mipmapLevel]; i++) {
			imageDataBuffer.put(imageData[i]);
		}
		
		imageDataBuffer.flip();
		
		return imageDataBuffer;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ImageFormat m_format;
	private Dimensions m_dimensions;
	
	private int[] m_imageSizes;
	private ArrayList<byte[]> m_imageData;
	
	private int m_arrayCount;
	private int m_faceCount;
	private int m_mipmapCount;
}
