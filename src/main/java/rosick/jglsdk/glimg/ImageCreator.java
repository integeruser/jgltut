package rosick.jglsdk.glimg;

import java.util.ArrayList;
import java.util.Arrays;

import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.Util.CompressedBlockData;


/**
 * Visit https://github.com/integeruser/jglsdk for project info, updates and license terms.
 * 
 * @author integeruser
 */
class ImageCreator {
	
	ImageCreator(ImageFormat ddsFormat, Dimensions ddsDimensions, int mipmapCount, int arrayCount, int faceCount) {
		m_imageFormat = ddsFormat;
		m_imageDimensions = new Dimensions(ddsDimensions);
		
		m_mipmapCount = mipmapCount;
		m_arrayCount = arrayCount;
		m_faceCount = faceCount;

		if (faceCount != 6 && faceCount != 1) {
			throw new BadFaceCountException();
		}

		if (faceCount == 6 && ddsDimensions.numDimensions != 2) {
			throw new CubemapsMustBe2DException();
		}

		if (ddsDimensions.numDimensions == 3 && arrayCount != 1) {
			throw new No3DTextureArrayException();
		}

		if (mipmapCount <= 0 || arrayCount <= 0) {
			throw new NoImagesSpecifiedException();
		}
		
		m_imageData = new ArrayList<>(mipmapCount);
		m_imageSizes = new int[mipmapCount];
		
		// Allocate the memory for our data.
		for (int mipmapLevel = 0; mipmapLevel < mipmapCount; mipmapLevel++) {
			Dimensions mipmapLevelDimensions = Util.calcMipmapLevelDimensions(ddsDimensions, mipmapLevel);
			
			int mipmapLevelSize = Util.calcMipmapLevelSize(ddsFormat, mipmapLevelDimensions);
			m_imageSizes[mipmapLevel] = mipmapLevelSize;

			byte[] mipmapLevelData = new byte[mipmapLevelSize * faceCount * arrayCount];		
			m_imageData.add(mipmapLevelData);			
		}
	}	
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	void setImageData(byte sourceData[], boolean isTopLeft, int mipmapLevel, int arrayIx, int faceIx) {
 		if (m_imageData.isEmpty()) {
			throw new ImageSetAlreadyCreatedException();
 		}
			
		// Check inputs.
		if ((arrayIx < 0) || (m_arrayCount <= arrayIx)) {
			throw new ArrayIndexOutOfBoundsException();
		}
	
		if ((faceIx < 0) || (m_faceCount <= faceIx)) {
			throw new FaceIndexOutOfBoundsException();
		}

		if ((mipmapLevel < 0) || (m_mipmapCount <= mipmapLevel)) {
			throw new MipmapLayerOutOfBoundsException();
		}
		
		// Get the image relative to mipmapLevel
		byte[] imageData = m_imageData.get(mipmapLevel);
				
		if (!isTopLeft) {
			Util.throwNotYetPortedException();
		} else {	
			int imageDataOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes[mipmapLevel];
			copyImageFlipped(sourceData, imageData, imageDataOffset, mipmapLevel);
		}
	}


	ImageSet createImage() {
		if (m_imageData.isEmpty()) {
			throw new ImageSetAlreadyCreatedException();
		}
		
		return new ImageSet(m_imageFormat, m_imageDimensions,
				m_mipmapCount, m_arrayCount, m_faceCount, 
				m_imageData, m_imageSizes);
	}	
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	private ImageFormat m_imageFormat;
	private Dimensions m_imageDimensions;
	
	private int m_mipmapCount;
	private int m_arrayCount;
	private int m_faceCount;
	
	private ArrayList<byte[]> m_imageData;
	private int[] m_imageSizes;
	
	
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

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private void copyImageFlipped(byte[] sourceData, byte[] imageData, int imageDataOffset, int mipmapLevel) {		
		assert (sourceData.length * m_faceCount * m_arrayCount) == imageData.length;
		
		Dimensions mipmapImageDimensions = Util.calcMipmapLevelDimensions(new Dimensions(m_imageDimensions), mipmapLevel);
		
		if (m_imageFormat.getPixelDataType().ordinal() < PixelDataType.NUM_UNCOMPRESSED_TYPES.ordinal()) {
			copyPixelsFlipped(m_imageFormat, 
					sourceData, imageData, imageDataOffset, 
					m_imageSizes[mipmapLevel], mipmapImageDimensions);
		} else {
			// Have to decode the pixel data and flip it manually.
			switch (m_imageFormat.getPixelDataType()) {
			case COMPRESSED_BC1:				
			case COMPRESSED_BC2:
			case COMPRESSED_BC3:
			case COMPRESSED_UNSIGNED_BC4:
			case COMPRESSED_SIGNED_BC4:
			case COMPRESSED_UNSIGNED_BC5:
			case COMPRESSED_SIGNED_BC5:
				copyBCFlipped(m_imageFormat,
						sourceData, imageData,imageDataOffset,
						m_imageSizes[mipmapLevel], mipmapImageDimensions, mipmapLevel);
				break;
				
			default:
				Util.throwNotYetPortedException();
			}
		}
	}
	
	
	private void copyPixelsFlipped(ImageFormat imageFormat, 
			byte[] sourceData, byte[] imageData, int imageDataOffset, 
			int imageSize, Dimensions imageDimensions) {
		// Flip the data. Copy line by line.
		final int numLines = imageDimensions.calcNumLines();
		final int lineSize = imageFormat.alignByteCount(Util.calcBytesPerPixel(imageFormat) * imageDimensions.width);

		// Flipped: start from last line of source, going backward
		int sourceLineOffset = imageSize - lineSize;			// start from last line
		int imageDataLineOffset = imageDataOffset;				// start from imageDataOffset
		
		for (int line = 0; line < numLines; line++) {		
			byte[] sourceLine = Arrays.copyOfRange(sourceData, sourceLineOffset, sourceLineOffset + lineSize);

			// Copy the source line into imageData
			System.arraycopy(sourceLine, 0, imageData, imageDataLineOffset, lineSize);
			
			// Update indices
			sourceLineOffset -= lineSize;
			imageDataLineOffset += lineSize;
		}
	}
	
	
	private void copyBCFlipped(ImageFormat imageFormat,
			byte[] sourceData, byte[] imageData, int imageDataOffset, 
			int imageSize, Dimensions imageDimensions, int mipmapLevel) {
		// No support for 3D compressed formats.
		assert imageDimensions.numDimensions != 3 : "No support for 3D compressed formats.";

		CompressedBlockData blockData = Util.getBlockCompressionData(imageFormat.getPixelDataType());
		final int blocksPerLine = (imageDimensions.width + (blockData.dimensions.width - 1)) / blockData.dimensions.width;

		final int blockLineSize = blocksPerLine * blockData.byteCount;
		final int numTotalBlocks = imageSize / blockData.byteCount;
		final int numLines = numTotalBlocks / blocksPerLine;
		
		// Copy each block.
		int sourceBlockOffset = imageSize - blockLineSize;		// start from last block
		int imageDataBlockOffset = imageDataOffset;				// start from imageDataOffset
		
		for (int line = 0; line < numLines; ++line) {		
			for (int block = 0; block < blocksPerLine; ++block) {
				byte[] sourceBlock = Arrays.copyOfRange(sourceData, sourceBlockOffset, sourceBlockOffset + blockData.byteCount);
				
				flippingFunc(imageFormat, sourceBlock, imageData, imageDataBlockOffset);
				
				sourceBlockOffset += blockData.byteCount;
				imageDataBlockOffset += blockData.byteCount;
			}
						
			// First goes back to beginning, second goes back one row.
			sourceBlockOffset -= blockLineSize;
			sourceBlockOffset -= blockLineSize;
		}	
	}

	private void flippingFunc(ImageFormat imageFormat, byte[] sourceData, byte[] imageData, int imageDataOffset) {
		switch (m_imageFormat.getPixelDataType()) {
		case COMPRESSED_BC1:
			copyBlockBC1Flipped(sourceData, imageData, imageDataOffset);
			break;

		default:
			Util.throwNotYetPortedException();
		}	
	}

	private void copyBlockBC1Flipped(byte[] sourceData, byte[] imageData, int imageDataOffset) {	
		assert sourceData.length == 8;
		
		// First 4 bytes are 2 16-bit colors. Keep them the same.
		for (int i = 0; i < 4; i++) {
			imageData[imageDataOffset + i] = sourceData[i];
		}

		// Next four bytes are 16 2-bit values, in row-major, top-to-bottom order,
		// representing the 4x4 pixel data for the block. So copy the bytes in reverse order.
		imageData[imageDataOffset + 4] = sourceData[7];
		imageData[imageDataOffset + 5] = sourceData[6];
		imageData[imageDataOffset + 6] = sourceData[5];
		imageData[imageDataOffset + 7] = sourceData[4];
	}	
}