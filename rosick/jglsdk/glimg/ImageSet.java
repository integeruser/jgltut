package rosick.jglsdk.glimg;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;



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


		int numLines() {
			// Computes the number of rows of pixel data in the image.
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
		private ImageSet m_pImpl;
		private int m_arrayIx, m_faceIx, m_mipmapLevel;
		
		
		public SingleImage(ImageSet pImpl, int mipmapLevel, int arrayIx, int faceIx) {
			m_pImpl = pImpl;
			m_arrayIx = arrayIx;
			m_faceIx = faceIx;
			m_mipmapLevel = mipmapLevel;
		}


		public Dimensions getDimensions() {
			return m_pImpl.getDimensions(m_mipmapLevel);
		}

		public ByteBuffer getImageData() {
			return m_pImpl.getImageData(m_mipmapLevel, m_arrayIx, m_faceIx);
		}
	}

	
	private ImageFormat m_format;
	private Dimensions m_dimensions;
	private ArrayList<Integer> m_imageSizes;
	private ArrayList<ArrayList<Integer>> m_imageData;
	private int m_mipmapCount;
	private int m_faceCount;

	
	
	ImageSet(ImageFormat format, Dimensions dimensions,
			int mipmapCount, int arrayCount, int faceCount,
			ArrayList<ArrayList<Integer>> imageData,
			ArrayList<Integer> imageSizes) {
		m_format = format;
		m_dimensions = dimensions;
		m_imageData = imageData;
		m_imageSizes = imageSizes;
		m_mipmapCount = mipmapCount;
		m_faceCount = faceCount;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	public int getMipmapCount() {
		return m_mipmapCount;
	}

	public ImageFormat getFormat() {
		return m_format;
	}
	
	public Dimensions getDimensions(int mipmapLevel) {
		return Util.modifySizeForMipmap(m_dimensions, mipmapLevel);
	}

	public SingleImage getImage(int mipmapLevel, int arrayIx, int faceIx) {
		return new SingleImage(this, mipmapLevel, arrayIx, faceIx);		
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private ByteBuffer getImageData(int mipmapLevel, int arrayIx, int faceIx) {		
		int imageOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes.get(mipmapLevel);
		ArrayList<Integer> image = m_imageData.get(mipmapLevel);
		List<Integer> offsettedImage = image.subList(imageOffset, image.size());

		ByteBuffer tempBuffer = BufferUtils.createByteBuffer(offsettedImage.size());
		
		for (Integer integer : offsettedImage) {
			tempBuffer.put(integer.byteValue());
		}
		
		tempBuffer.flip();
		
		return tempBuffer;
	}
}
