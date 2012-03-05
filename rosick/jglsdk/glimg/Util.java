package rosick.jglsdk.glimg;

import rosick.jglsdk.glimg.ImageFormat.Bitdepth;
import rosick.jglsdk.glimg.ImageFormat.PixelComponents;
import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Util {
	
	private static PixelComponents[] g_twoCompFormats 	= {PixelComponents.FMT_COLOR_RG, PixelComponents.FMT_DEPTH_X};
	private static PixelComponents[] g_threeCompFormats = {PixelComponents.FMT_COLOR_RGB, PixelComponents.FMT_COLOR_RGB_sRGB};
	private static PixelComponents[] g_fourCompFormats 	= {PixelComponents.FMT_COLOR_RGBX, PixelComponents.FMT_COLOR_RGBA,
			PixelComponents.FMT_COLOR_RGBX_sRGB, PixelComponents.FMT_COLOR_RGBA_sRGB};
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	static boolean isOneOfThese(PixelComponents testValue, PixelComponents testArray[]) {
		for (PixelComponents pixelComp : testArray) {
			if (testValue == pixelComp) {
				return true;
			}
		}
		
		return false;
	}
	
	
	static int componentCount(PixelComponents eFormat) {
		if (isOneOfThese(eFormat, g_twoCompFormats)) {
			return 2;
		}

		if (isOneOfThese(eFormat, g_threeCompFormats)) {
			return 3;
		}

		if (isOneOfThese(eFormat, g_fourCompFormats)) {
			return 4;
		}

		return 1;
	}
	

	public static Dimensions modifySizeForMipmap(Dimensions origDim, int mipmapLevel) {
		Dimensions res = new Dimensions(origDim);
		
		for (int iLoop = 0; iLoop < mipmapLevel; iLoop++) {
			res.width /= 2;
			res.height /= 2;
			res.depth /= 2;
		}

		return res;
	}
	
	
	public static int calcBytesPerPixel(ImageFormat fmt) {
		int bytesPerPixel = 0;
		
		switch(fmt.depth()) {
		case BD_COMPRESSED:											return 0;
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

		if (fmt.depth().ordinal() < Bitdepth.BD_NUM_PER_COMPONENT.ordinal()) {
			bytesPerPixel *= componentCount(fmt.components());
		}

		return bytesPerPixel;
	}
	
	
	static int calcImageByteSize(ImageFormat fmt, Dimensions dims) {
		if (fmt.type().ordinal() >= PixelDataType.DT_NUM_UNCOMPRESSED_TYPES.ordinal()) {
			// Compressed texture.
			throw new RuntimeException("Compressed texture non yet implemented.");
			
			/*
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
			*/
		}
		else {
			int bpp = Util.calcBytesPerPixel(fmt);
			int lineByteSize = fmt.alignByteCount(bpp * dims.width);

			if(dims.numDimensions > 1)
				lineByteSize *= dims.height;
			if(dims.numDimensions == 3)
				lineByteSize *= dims.depth;

			return lineByteSize;
		}
	}
}