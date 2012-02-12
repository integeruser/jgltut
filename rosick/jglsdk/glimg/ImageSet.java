package rosick.jglsdk.glimg;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ImageSet {

	private ImageSetImpl m_pImpl;
	
	public ImageSet(ImageSetImpl pImpl) {
		m_pImpl = pImpl;
	}


	public static class Dimensions {
		int numDimensions;	// <The number of dimensions of an image. Can be 1, 2, or 3.
		public int width;			// <The width of the image. Always valid.
		public int height;			// <The height of the image. Only valid if numDimensions is 2 or 3.
		int depth;			// <The depth of the image. Only valid if numDimensions is 3.

		Dimensions(Dimensions dimensions) {
			numDimensions = dimensions.numDimensions;
			width = dimensions.width;
			height = dimensions.height;
			depth = dimensions.depth;
		}

		public Dimensions() {
			// TODO Auto-generated constructor stub
		}

		// Computes the number of rows of pixel data in the image.
		int numLines() {
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
	
	
	public static class ImageSetImpl {
		private Dimensions m_dimensions;
		private ArrayList<Integer> m_imageSizes;
		private ArrayList<ArrayList<Integer>> m_imageData;
		private int m_mipmapCount;
		private int m_faceCount;

		
		public ImageSetImpl(ImageFormat format, Dimensions dimensions,
				int mipmapCount, int arrayCount, int faceCount,
				ArrayList<ArrayList<Integer>> imageData,
				ArrayList<Integer> imageSizes) {

			m_dimensions = dimensions;
			m_imageData = imageData;
			m_imageSizes = imageSizes;
			m_mipmapCount = mipmapCount;
			m_faceCount = faceCount;
		}

		public Dimensions getDimensions(int mipmapLevel) {
			return modifySizeForMipmap(m_dimensions, mipmapLevel);
		}

		private Dimensions modifySizeForMipmap(Dimensions origDim, int mipmapLevel) {
			for(int iLoop = 0; iLoop < mipmapLevel; iLoop++) {
				origDim.width /= 2;
				origDim.height /= 2;
				origDim.depth /= 2;
			}

			return origDim;
		}

		public ByteBuffer getImageData(int mipmapLevel, int arrayIx, int faceIx) {

			//int imageOffset = arrayIx * faceIx * m_imageSizes.get(mipmapLevel);			
			
			int imageOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes.get(mipmapLevel);
			ArrayList<Integer> temp = m_imageData.get(imageOffset);
			
			ByteBuffer tempBuffer = BufferUtils.createByteBuffer(temp.size());
			
			for (Integer integer : temp) {
				tempBuffer.put((byte) (int) integer);
			}
			
			tempBuffer.flip();
			
			return tempBuffer;
		}

		public int getMipmapCount() {
			return m_mipmapCount;
		}
	}


	
	
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
	
	public SingleImage getImage(int mipmapLevel, int arrayIx, int faceIx) {
		return new SingleImage(m_pImpl, mipmapLevel, arrayIx, faceIx);		
	}

	
	public int getMipmapCount() {
		return m_pImpl.getMipmapCount();
	}
}
