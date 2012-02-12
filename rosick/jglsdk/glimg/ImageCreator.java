package rosick.jglsdk.glimg;

import java.util.ArrayList;

import rosick.jglsdk.glimg.ImageFormat.Bitdepth;
import rosick.jglsdk.glimg.ImageFormat.PixelComponents;
import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.ImageSetImpl;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ImageCreator {
	
	private ImageFormat m_format;
	private Dimensions m_dims;
	private int m_mipmapCount;
	private int m_arrayCount;
	private int m_faceCount;
	private ArrayList<ArrayList<Integer>> m_imageData;
	private ArrayList<Integer> m_imageSizes;


	public ImageCreator(ImageFormat format, Dimensions dimensions, int mipmapCount, int arrayCount, int faceCount) throws CubemapsMustBe2DException, BadFaceCountException, No3DTextureArrayException, NoImagesSpecifiedException {
			m_format = format;
			m_dims = new Dimensions(dimensions);
			m_mipmapCount = mipmapCount;
			m_arrayCount = arrayCount;
			m_faceCount = faceCount;
			m_imageData = new ArrayList<>();
			m_imageSizes = new ArrayList<>();
			
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

			//Allocate the memory for our data.
			int bpp = calcBytesPerPixel(m_format);
			for(int level = 0; level < mipmapCount; ++level)
			{
				Dimensions mipmapDims = modifySizeForMipmap(new Dimensions(m_dims), level);
				int imageSize = calcImageByteSize(m_format, mipmapDims);
				
				ArrayList<Integer> temp = new ArrayList<>();
				for (int i = 0; i < imageSize * m_faceCount * m_arrayCount; i++) {
					temp.add(0);
				}
				m_imageData.add(temp);
				m_imageSizes.add(imageSize);
			}
	}
	
	


	private int calcImageByteSize(ImageFormat fmt, Dimensions dims) {
		/*
		if(fmt.type().ordinal() >= PixelDataType.DT_NUM_UNCOMPRESSED_TYPES.ordinal())
		{
			//Compressed texture.
		
			CompressedBlockData cdata = GetBlockCompressionData(fmt.Type());
			size_t width = (dims.width + (cdata.dims.width - 1)) / cdata.dims.width;
			size_t height = 0;
			if(dims.numDimensions > 1)
				height = (dims.height + (cdata.dims.height - 1)) / cdata.dims.height;
			else
			{
				assert(cdata.dims.numDimensions >= 2);
				height = cdata.dims.height;
			}

			return width * height * cdata.byteCount;
		}
		else
		{
		*/
			int bpp = calcBytesPerPixel(fmt);
			int lineByteSize = fmt.alignByteCount(bpp * dims.width);

			if(dims.numDimensions > 1)
				lineByteSize *= dims.height;
			if(dims.numDimensions == 3)
				lineByteSize *= dims.depth;

			return lineByteSize;
		//}
	}


	static Dimensions modifySizeForMipmap(Dimensions origDim, int mipmapLevel) {
		for(int iLoop = 0; iLoop < mipmapLevel; iLoop++) {
			origDim.width /= 2;
			origDim.height /= 2;
			origDim.depth /= 2;
		}

		return origDim;
	}


	static int calcBytesPerPixel(ImageFormat fmt) {
		int bytesPerPixel = 0;
		switch(fmt.depth()) {
		case BD_COMPRESSED:			return 0;
		case BD_PER_COMP_8:					bytesPerPixel = 1;		break;
		case BD_PER_COMP_16:				bytesPerPixel = 2;		break;
		case BD_PER_COMP_32:				bytesPerPixel = 4;		break;
		case BD_PACKED_16_BIT_565:			bytesPerPixel = 2;		break;
		case BD_PACKED_16_BIT_5551:			bytesPerPixel = 2;		break;
		case BD_PACKED_16_BIT_4444:			bytesPerPixel = 2;		break;
		case BD_PACKED_32_BIT_8888:			bytesPerPixel = 4;		break;
		case BD_PACKED_32_BIT_1010102:		bytesPerPixel = 4;		break;
		case BD_PACKED_32_BIT_248:			bytesPerPixel = 4;		break;
		case BD_PACKED_16_BIT_565_REV:		bytesPerPixel = 2;		break;
		case BD_PACKED_16_BIT_1555_REV:		bytesPerPixel = 2;		break;
		case BD_PACKED_16_BIT_4444_REV:		bytesPerPixel = 2;		break;
		case BD_PACKED_32_BIT_8888_REV:		bytesPerPixel = 4;		break;
		case BD_PACKED_32_BIT_2101010_REV:	bytesPerPixel = 4;		break;
		case BD_PACKED_32_BIT_101111_REV:	bytesPerPixel = 4;		break;
		case BD_PACKED_32_BIT_5999_REV:		bytesPerPixel = 4;		break;
		}

		if (fmt.depth().ordinal() < Bitdepth.BD_NUM_PER_COMPONENT.ordinal())
			bytesPerPixel *= componentCount(fmt.components());

		return bytesPerPixel;
	}


	private static int componentCount(PixelComponents eFormat) {
		if (isOneOfThese(eFormat, g_twoCompFormats))
			return 2;

		if (isOneOfThese(eFormat, g_threeCompFormats))
			return 3;

		if (isOneOfThese(eFormat, g_fourCompFormats))
			return 4;

		return 1;
	}
	
	
	static PixelComponents g_twoCompFormats[] = {PixelComponents.FMT_COLOR_RG, PixelComponents.FMT_DEPTH_X};
	static PixelComponents g_threeCompFormats[] = {PixelComponents.FMT_COLOR_RGB, PixelComponents.FMT_COLOR_RGB_sRGB};
	static PixelComponents g_fourCompFormats[] = {PixelComponents.FMT_COLOR_RGBX, PixelComponents.FMT_COLOR_RGBA,
			PixelComponents.FMT_COLOR_RGBX_sRGB, PixelComponents.FMT_COLOR_RGBA_sRGB};

	static boolean isOneOfThese(PixelComponents testValue, PixelComponents testArray[]) {
		for (PixelComponents loop : testArray) {
			if (testValue == loop) {
				return true;
			}
		}
		
		return false;
	}

	public class BadFaceCountException extends RuntimeException {}
	
	public class CubemapsMustBe2DException extends RuntimeException {}

	public class No3DTextureArrayException extends RuntimeException {}
	
	public class NoImagesSpecifiedException extends RuntimeException {}

	public class ImageSetAlreadyCreatedException extends RuntimeException {}

	public class MipmapLayerOutOfBoundsException extends RuntimeException {}

	public class FaceIndexOutOfBoundsException extends RuntimeException {}



	public void setImageData(ArrayList<Character> pixelData, int startIndex,
			boolean isTopLeft, int mipmapLevel, int arrayIx, int faceIx) {

 		if (m_imageData.isEmpty())
			throw new ImageSetAlreadyCreatedException();

		//Check inputs.
		if ((arrayIx < 0) || (m_arrayCount <= arrayIx)) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if ((mipmapLevel < 0) || (m_mipmapCount <= mipmapLevel))
			throw new MipmapLayerOutOfBoundsException();


		if((faceIx < 0) || (m_faceCount <= faceIx))
			throw new FaceIndexOutOfBoundsException();

		//int imageOffset = arrayIx * faceIx * m_imageSizes.get(mipmapLevel);
		int imageOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes.get(mipmapLevel);

		ArrayList<Integer> pMipmapData = m_imageData.get(mipmapLevel);
		//pMipmapData += imageOffset;
		//if(!isTopLeft)
		//{
		//	memcpy(pMipmapData, pixelData, m_imageSizes[mipmapLevel]);
		//}
		//else
		//{
			copyImageFlipped(pixelData, startIndex, pMipmapData, imageOffset, mipmapLevel);
		//}
	}




	private void copyImageFlipped(ArrayList<Character> pixelData,
			int startIndex, ArrayList<Integer> pDstData, int imageOffset,
			int mipmapLevel) {
		
		Dimensions dims = modifySizeForMipmap(new Dimensions(m_dims), mipmapLevel);
		if (m_format.type().ordinal() < PixelDataType.DT_NUM_UNCOMPRESSED_TYPES.ordinal())
		{
			copyPixelsFlipped(pDstData, imageOffset, dims, m_format, mipmapLevel, pixelData, startIndex,
				m_imageSizes.get(mipmapLevel));
		}		
	}


	private void copyPixelsFlipped(ArrayList<Integer> pMipmapData,
			int imageOffset, Dimensions dims, ImageFormat format,
			int mipmapLevel, ArrayList<Character> pixelData, int startIndex,
			int imageSize) {


		//Flip the data. Copy line by line.
		int numLines = dims.numLines();
		int lineByteSize = format.alignByteCount(calcBytesPerPixel(format) * dims.width);

		//Move the pixel data to the last row.
		int pInputRow = imageOffset;
		pInputRow += imageSize;
		pInputRow -= lineByteSize;
		for(int line = 0; line < numLines; ++line)
		{
			for (int i = 0; i < lineByteSize; i++) {
				pMipmapData.set(line * lineByteSize + i, (int) pixelData.get(startIndex + pInputRow - line * lineByteSize + i));

			}
			///memcpy(pMipmapData, pInputRow, lineByteSize);
			//pMipmapData += lineByteSize;
			//pInputRow -= lineByteSize;
		}

		//return static_cast<const unsigned char *>(pixelData) + imageSize;
	}




	public ImageSet createImage() {
		if (m_imageData.isEmpty())
			throw new ImageSetAlreadyCreatedException();

		ImageSetImpl pImageData = new ImageSetImpl(m_format, m_dims,
			m_mipmapCount, m_arrayCount, m_faceCount, m_imageData, m_imageSizes);

		ImageSet pImageSet = new ImageSet(pImageData);

		return pImageSet;
		
		
	}
}
