package rosick.jglsdk.glimg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ImageSet {
	
	public static class Dimensions {
		public int m_numDimensions;								// The number of dimensions of an image. Can be 1, 2, or 3.
		public int m_width;										// The width of the image. Always valid.
		public int m_height;									// The height of the image. Only valid if numDimensions is 2 or 3.
		public int m_depth;										// The depth of the image. Only valid if numDimensions is 3.

		
		public Dimensions() {
		}
		
		public Dimensions(Dimensions dimensions) {
			m_numDimensions = dimensions.m_numDimensions;
			m_width = dimensions.m_width;
			m_height = dimensions.m_height;
			m_depth = dimensions.m_depth;
		}


		public int getLineCount() {
			// Computes the number of rows of pixel data in the image.
			switch (m_numDimensions) {
			case 1:
				return 1;
			case 2:
				return m_height;
			case 3:
				return m_depth * m_height;
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
		
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
		SingleImage(ImageSet imageSet, int mipmapLevel, int arrayIx, int faceIx) {
			m_imageSet = imageSet;
			m_arrayIx = arrayIx;
			m_faceIx = faceIx;
			m_mipmapLevel = mipmapLevel;
		}
		
		
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private ImageSet m_imageSet;
		private int m_arrayIx, m_faceIx, m_mipmapLevel;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public final int getMipmapCount() {
		return m_mipmapCount;
	}

	public final ImageFormat getFormat() {
		return m_format;
	}
	
	public final Dimensions getDimensions(int mipmapLevel) {
		return Util.getImageDimensionsForMipmapLevel(m_dimensions, mipmapLevel);
	}

	public final SingleImage getImage(int mipmapLevel, int arrayIx, int faceIx) {
		return new SingleImage(this, mipmapLevel, arrayIx, faceIx);		
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	ImageSet(ImageFormat format, Dimensions dimensions, int mipmapCount, int faceCount, 
			ArrayList<byte[]> imageData, int[] imageSizes) {
		m_format = format;
		m_dimensions = dimensions;
		m_imageData = imageData;
		m_imageSizes = imageSizes;
		m_mipmapCount = mipmapCount;
		m_faceCount = faceCount;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ImageFormat m_format;
	private Dimensions m_dimensions;
	private int[] m_imageSizes;
	private ArrayList<byte[]> m_imageData;
	private int m_mipmapCount;
	private int m_faceCount;
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ByteBuffer getImageData(int mipmapLevel, int arrayIx, int faceIx) {		
		byte[] mipmapImage = m_imageData.get(mipmapLevel);
		int mipmapImageOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes[mipmapLevel];
		
		byte[] mipmapImageOffsetted = Arrays.copyOf(mipmapImage, mipmapImage.length);
		
		ByteBuffer mipmapByteBuffer = ByteBuffer.allocateDirect(mipmapImage.length - mipmapImageOffset).order(ByteOrder.nativeOrder());
		for (Byte pixel : mipmapImageOffsetted) {
			mipmapByteBuffer.put(pixel);
		}
		
		mipmapByteBuffer.flip();
		
		return mipmapByteBuffer;
	}
}
