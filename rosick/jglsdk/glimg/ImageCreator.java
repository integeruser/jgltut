package rosick.jglsdk.glimg;

import java.util.ArrayList;
import java.util.List;

import rosick.PortingUtils;
import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ImageCreator {
	
	public ImageCreator(ImageFormat imageFormat, Dimensions dimensions, int mipmapCount, int arrayCount, int faceCount) {
		this.imageFormat = imageFormat;
		this.dimensions = new Dimensions(dimensions);
		this.mipmapCount = mipmapCount;
		this.arrayCount = arrayCount;
		this.faceCount = faceCount;
		imageData = new ArrayList<>(mipmapCount);
		imageSizes = new ArrayList<>(mipmapCount);

		if (faceCount != 6 && faceCount != 1) {
			throw new BadFaceCountException();
		}

		if (faceCount == 6 && dimensions.numDimensions != 2) {
			throw new CubemapsMustBe2DException();
		}

		if (dimensions.numDimensions == 3 && arrayCount != 1) {
			throw new No3DTextureArrayException();
		}

		if (mipmapCount <= 0 || arrayCount <= 0) {
			throw new NoImagesSpecifiedException();
		}

		// Allocate the memory for our data.
		for (int level = 0; level < mipmapCount; level++) {
			Dimensions mipmapDimensions = Util.modifyDimensionsForMipmap(dimensions, level);
			int imageSize = Util.getImageByteSize(imageFormat, mipmapDimensions);

			ArrayList<Integer> mipmap = new ArrayList<>();
			for (int i = 0; i < imageSize * faceCount * arrayCount; i++) {
				mipmap.add(0);
			}
			
			imageData.add(mipmap);
			imageSizes.add(imageSize);
		}
	}	
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public void setImageData(List<Character> pixelData,
			boolean isTopLeft, int mipmapLevel, int arrayIx, int faceIx) {

 		if (imageData.isEmpty()) {
			throw new ImageSetAlreadyCreatedException();
 		}
			
		// Check inputs.
		if ((arrayIx < 0) || (arrayCount <= arrayIx)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if ((mipmapLevel < 0) || (mipmapCount <= mipmapLevel)) {
			throw new MipmapLayerOutOfBoundsException();
		}

		if ((faceIx < 0) || (faceCount <= faceIx)) {
			throw new FaceIndexOutOfBoundsException();
		}

		int imageOffset = ((arrayIx * faceCount) + faceIx) * imageSizes.get(mipmapLevel);

		ArrayList<Integer> pMipmapData = imageData.get(mipmapLevel);
		List<Integer> pMipmapDataList = pMipmapData.subList(imageOffset, pMipmapData.size());
		
		if (!isTopLeft) {
			//memcpy(pMipmapData, pixelData, m_imageSizes[mipmapLevel]);
			throw new RuntimeException("Not yet implemented");
		}
		else {
			copyImageFlipped(pixelData, pMipmapDataList, mipmapLevel);
		}
	}


	public ImageSet createImage() {
		if (imageData.isEmpty()) {
			throw new ImageSetAlreadyCreatedException();
		}
		
		return new ImageSet(imageFormat, dimensions,
				mipmapCount, arrayCount, faceCount, imageData, imageSizes);
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
	
		
	private ImageFormat imageFormat;
	private Dimensions dimensions;
	private int mipmapCount;
	private int arrayCount;
	private int faceCount;
	private ArrayList<ArrayList<Integer>> imageData;
	private ArrayList<Integer> imageSizes;

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void copyImageFlipped(List<Character> pixelData,
			List<Integer> pDstData, int mipmapLevel) {
		Dimensions dims = Util.modifyDimensionsForMipmap(new Dimensions(dimensions), mipmapLevel);
		
		if (imageFormat.getType().ordinal() < PixelDataType.DT_NUM_UNCOMPRESSED_TYPES.ordinal()) {
			copyPixelsFlipped(pDstData, dims, imageFormat, mipmapLevel, pixelData,
				imageSizes.get(mipmapLevel));
		} 
		else {
			throw new RuntimeException("Not yet implemented");
		}
	}
	
	
	private void copyPixelsFlipped(List<Integer> pMipmapData, Dimensions dims,
			ImageFormat format, int mipmapLevel, List<Character> pixelData,
			int imageSize) {
		// Flip the data. Copy line by line.
		int numLines = dims.numLines();
		int lineByteSize = format.alignByteCount(Util.getBytesPerPixel(format) * dims.width);
		
		// Move the pixel data to the last row.
		int pInputRow = imageSize;
		pInputRow -= lineByteSize;
		for (int line = 0; line < numLines; line++) {
			int lineOffset = line * lineByteSize;
			
			for (int i = 0; i < lineByteSize; i++) {
				int pixel = PortingUtils.toUnsignedInt(pixelData.get(pInputRow - lineOffset + i));
				pMipmapData.set(lineOffset + i, pixel);
			}
		}
	}
}
