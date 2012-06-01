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
	
	private class BadFaceCountException extends RuntimeException {
		private static final long serialVersionUID = -4120563516856357626L;
	}

	private class CubemapsMustBe2DException extends RuntimeException {
		private static final long serialVersionUID = 4034128602806705190L;
	}

	private class No3DTextureArrayException extends RuntimeException {
		private static final long serialVersionUID = -9001483779340494905L;
	}

	private class NoImagesSpecifiedException extends RuntimeException {
		private static final long serialVersionUID = 3390815114957601365L;
	}

	private class ImageSetAlreadyCreatedException extends RuntimeException {
		private static final long serialVersionUID = -7707381545866051896L;
	}

	private class MipmapLayerOutOfBoundsException extends RuntimeException {
		private static final long serialVersionUID = 5160364349781356398L;
	}

	private class FaceIndexOutOfBoundsException extends RuntimeException {
		private static final long serialVersionUID = 5885166000035279615L;
	}
	
		
	private ImageFormat m_format;
	private Dimensions m_dims;
	private int m_mipmapCount;
	private int m_arrayCount;
	private int m_faceCount;
	private ArrayList<ArrayList<Integer>> m_imageData;
	private ArrayList<Integer> m_imageSizes;

	
	public ImageCreator(ImageFormat format, Dimensions dimensions, int mipmapCount, int arrayCount, int faceCount) {
		m_format = format;
		m_dims = new Dimensions(dimensions);
		m_mipmapCount = mipmapCount;
		m_arrayCount = arrayCount;
		m_faceCount = faceCount;
		m_imageData = new ArrayList<>(mipmapCount);
		m_imageSizes = new ArrayList<>(mipmapCount);

		if (m_faceCount != 6 && m_faceCount != 1) {
			throw new BadFaceCountException();
		}

		if (m_faceCount == 6 && m_dims.numDimensions != 2) {
			throw new CubemapsMustBe2DException();
		}

		if (m_dims.numDimensions == 3 && m_arrayCount != 1) {
			throw new No3DTextureArrayException();
		}

		if (m_mipmapCount <= 0 || m_arrayCount <= 0) {
			throw new NoImagesSpecifiedException();
		}

		// Allocate the memory for our data.
		for (int level = 0; level < mipmapCount; level++) {
			Dimensions mipmapDims = Util.modifySizeForMipmap(m_dims, level);
			int imageSize = Util.calcImageByteSize(m_format, mipmapDims);

			ArrayList<Integer> mipmap = new ArrayList<>();
			for (int i = 0; i < imageSize * m_faceCount * m_arrayCount; i++) {
				mipmap.add(0);
			}
			
			m_imageData.add(mipmap);
			m_imageSizes.add(imageSize);
		}
	}	
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public void setImageData(List<Character> pixelData,
			boolean isTopLeft, int mipmapLevel, int arrayIx, int faceIx) {

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

		int imageOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes.get(mipmapLevel);

		ArrayList<Integer> pMipmapData = m_imageData.get(mipmapLevel);
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
		if (m_imageData.isEmpty()) {
			throw new ImageSetAlreadyCreatedException();
		}
		
		return new ImageSet(m_format, m_dims,
				m_mipmapCount, m_arrayCount, m_faceCount, m_imageData, m_imageSizes);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private void copyImageFlipped(List<Character> pixelData,
			List<Integer> pDstData, int mipmapLevel) {
		Dimensions dims = Util.modifySizeForMipmap(new Dimensions(m_dims), mipmapLevel);
		
		if (m_format.type().ordinal() < PixelDataType.DT_NUM_UNCOMPRESSED_TYPES.ordinal()) {
			copyPixelsFlipped(pDstData, dims, m_format, mipmapLevel, pixelData,
				m_imageSizes.get(mipmapLevel));
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
		int lineByteSize = format.alignByteCount(Util.calcBytesPerPixel(format) * dims.width);
		
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
