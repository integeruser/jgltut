package rosick.jglsdk.glimg;

import java.util.ArrayList;
import java.util.Arrays;

import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
class ImageCreator {
	
	ImageCreator(ImageFormat format, Dimensions dimensions, int mipmapCount, int arrayCount, int faceCount) {
		m_imageFormat = format;
		m_imageDimensions = new Dimensions(dimensions);
		m_mipmapCount = mipmapCount;
		m_arrayCount = arrayCount;
		m_faceCount = faceCount;
		m_imageData = new ArrayList<>(mipmapCount);
		m_imageByteSizes = new int[mipmapCount];

		if (faceCount != 6 && faceCount != 1) {
			throw new BadFaceCountException();
		}

		if (faceCount == 6 && dimensions.m_numDimensions != 2) {
			throw new CubemapsMustBe2DException();
		}

		if (dimensions.m_numDimensions == 3 && arrayCount != 1) {
			throw new No3DTextureArrayException();
		}

		if (mipmapCount <= 0 || arrayCount <= 0) {
			throw new NoImagesSpecifiedException();
		}

		// Allocate the memory for our data.
		for (int mipmapLevel = 0; mipmapLevel < mipmapCount; mipmapLevel++) {
			Dimensions mipmapImageDimensions = Util.getImageDimensionsForMipmapLevel(dimensions, mipmapLevel);
			
			int mipmapImageByteSize = getImageByteSize(format, mipmapImageDimensions);
			m_imageByteSizes[mipmapLevel] = mipmapImageByteSize;

			// An image for each mipmapLevel
			byte[] mipmapImage = new byte[mipmapImageByteSize * faceCount * arrayCount];		
			m_imageData.add(mipmapImage);			
		}
	}	
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	void setImageData(byte image[], boolean isTopLeft, int mipmapLevel, int arrayIx, int faceIx) {
 		if (m_imageData.isEmpty()) {
			throw new ImageSetAlreadyCreatedException();
 		}
			
		// Check inputs.
		if ((arrayIx < 0) || (m_arrayCount <= arrayIx)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if ((mipmapLevel < 0) || (m_mipmapCount <= mipmapLevel)) {
			throw new MipmapLayerOutOfBoundsException();
		}

		if ((faceIx < 0) || (m_faceCount <= faceIx)) {
			throw new FaceIndexOutOfBoundsException();
		}

		// Get the image relative to mipmapLevel
		byte[] mipmapImage = m_imageData.get(mipmapLevel);
		int mipmapImageOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageByteSizes[mipmapLevel];
				
		if (!isTopLeft) {
			throw new RuntimeException("Not yet implemented");
		}
		else {
			copyImageFlipped(mipmapLevel, image, mipmapImage, mipmapImageOffset);
		}
	}


	ImageSet createImage() {
		if (m_imageData.isEmpty()) {
			throw new ImageSetAlreadyCreatedException();
		}
		
		return new ImageSet(m_imageFormat, m_imageDimensions,
				m_mipmapCount, m_faceCount, m_imageData, m_imageByteSizes);
	}	
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	private static class BadFaceCountException extends RuntimeException {
		private static final long serialVersionUID = -4120563516856357626L;
	}

	private static class CubemapsMustBe2DException extends RuntimeException {
		private static final long serialVersionUID = 4034128602806705190L;
	}

	private static class No3DTextureArrayException extends RuntimeException {
		private static final long serialVersionUID = -9001483779340494905L;
	}

	private static class NoImagesSpecifiedException extends RuntimeException {
		private static final long serialVersionUID = 3390815114957601365L;
	}

	private static class ImageSetAlreadyCreatedException extends RuntimeException {
		private static final long serialVersionUID = -7707381545866051896L;
	}

	private static class MipmapLayerOutOfBoundsException extends RuntimeException {
		private static final long serialVersionUID = 5160364349781356398L;
	}

	private static class FaceIndexOutOfBoundsException extends RuntimeException {
		private static final long serialVersionUID = 5885166000035279615L;
	}
	
		
	private ImageFormat m_imageFormat;
	private Dimensions m_imageDimensions;
	private int m_mipmapCount;
	private int m_arrayCount;
	private int m_faceCount;
	private ArrayList<byte[]> m_imageData;
	private int[] m_imageByteSizes;

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static int getImageByteSize(ImageFormat imageFormat, Dimensions dimensions) {
		if (imageFormat.getType().ordinal() >= PixelDataType.DT_NUM_UNCOMPRESSED_TYPES.ordinal()) {
			throw new RuntimeException("Compressed texture non yet implemented.");
		}
		else {
			int bytesPerPixel = Util.getBytesPerPixel(imageFormat);
			int lineByteSize = imageFormat.alignByteCount(bytesPerPixel * dimensions.m_width);

			if (dimensions.m_numDimensions > 1) {
				lineByteSize *= dimensions.m_height;
			}
			
			if (dimensions.m_numDimensions == 3) {
				lineByteSize *= dimensions.m_depth;
			}

			return lineByteSize;
		}
	}
	

	private void copyImageFlipped(int mipmapLevel, byte[] image, byte[] mipmapImage, int mipmapImageOffset) {	
		Dimensions mipmapImageDimensions = Util.getImageDimensionsForMipmapLevel(new Dimensions(m_imageDimensions), mipmapLevel);
		
		if (m_imageFormat.getType().ordinal() < PixelDataType.DT_NUM_UNCOMPRESSED_TYPES.ordinal()) {
			copyPixelsFlipped(m_imageFormat, image, 
					mipmapImage, mipmapImageOffset, m_imageByteSizes[mipmapLevel], mipmapImageDimensions);
		} 
		else {
			throw new RuntimeException("Not yet implemented");
		}
	}
	
	
	private void copyPixelsFlipped(ImageFormat imageFormat, byte[] image, 
			byte[] mipmapImage, int mipmapImageOffset, int mipmapImageByteSize, Dimensions mipmapImageDimensions) 
	{
		int lineCount = mipmapImageDimensions.getLineCount();
		int lineByteSize = imageFormat.alignByteCount(Util.getBytesPerPixel(imageFormat) * mipmapImageDimensions.m_width);

		// Flipped: start from last line of image, going backwards
		int imageLineOffset = mipmapImageByteSize - lineByteSize; 
		int mipmapImageLineOffset = 0;
		
		for (int line = 0; line < lineCount; line++) {		
			// Get a line from image
			byte[] imageLine = Arrays.copyOfRange(image, imageLineOffset, imageLineOffset + lineByteSize);

			// Copy the line into mipmapImage
			System.arraycopy(imageLine, 0, mipmapImage, mipmapImageOffset + mipmapImageLineOffset, lineByteSize);
			
			// Update indices
			imageLineOffset -= lineByteSize;
			mipmapImageLineOffset += lineByteSize;
		}
	}
}
