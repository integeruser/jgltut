package rosick.jglsdk.glimg;

import java.nio.ByteBuffer;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
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
		private ImageSetImpl m_pImpl;
		private int m_arrayIx, m_faceIx, m_mipmapLevel;
		
		
		public SingleImage(ImageSetImpl pImpl, int mipmapLevel, int arrayIx, int faceIx) {
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

			
	private ImageSetImpl m_pImpl;
	
	
	ImageSet(ImageSetImpl pImpl) {
		m_pImpl = pImpl;
	}


	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public int getMipmapCount() {
		return m_pImpl.getMipmapCount();
	}
	
	public ImageFormat getFormat() {
		return m_pImpl.getFormat();
	}
	
	public SingleImage getImage(int mipmapLevel, int arrayIx, int faceIx) {
		return new SingleImage(m_pImpl, mipmapLevel, arrayIx, faceIx);		
	}
}
