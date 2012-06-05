package rosick.jglsdk.glimg;

import rosick.jglsdk.glimg.ImageFormat.Bitdepth;
import rosick.jglsdk.glimg.ImageFormat.PixelComponents;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
class Util {
		
	static Dimensions getImageDimensionsForMipmapLevel(Dimensions imageDimensions, int mipmapLevel) {
		Dimensions mipmapImageDimensions = new Dimensions(imageDimensions);
		
		for (int i = 0; i < mipmapLevel; i++) {
			mipmapImageDimensions.m_width /= 2;
			mipmapImageDimensions.m_height /= 2;
			mipmapImageDimensions.m_depth /= 2;
		}

		return mipmapImageDimensions;
	}
	
	
	static int getBytesPerPixel(ImageFormat imageFormat) {
		int bytesPerPixel = 0;
		
		switch(imageFormat.getBitdepth()) {
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

		if (imageFormat.getBitdepth().ordinal() < Bitdepth.BD_NUM_PER_COMPONENT.ordinal()) {
			bytesPerPixel *= getComponentCount(imageFormat.getPixelComponents());
		}

		return bytesPerPixel;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private static PixelComponents[] twoCompFormats 	= {PixelComponents.FMT_COLOR_RG, PixelComponents.FMT_DEPTH_X};
	private static PixelComponents[] threeCompFormats 	= {PixelComponents.FMT_COLOR_RGB, PixelComponents.FMT_COLOR_RGB_sRGB};
	private static PixelComponents[] fourCompFormats 	= {PixelComponents.FMT_COLOR_RGBX, PixelComponents.FMT_COLOR_RGBA,
			PixelComponents.FMT_COLOR_RGBX_sRGB, PixelComponents.FMT_COLOR_RGBA_sRGB};
	
	
	private static boolean isOneOfThese(PixelComponents testValue, PixelComponents testArray[]) {
		for (PixelComponents pixelComp : testArray) {
			if (testValue == pixelComp) {
				return true;
			}
		}
		
		return false;
	}
	
	private static int getComponentCount(PixelComponents eFormat) {
		if (isOneOfThese(eFormat, twoCompFormats)) {
			return 2;
		}

		if (isOneOfThese(eFormat, threeCompFormats)) {
			return 3;
		}

		if (isOneOfThese(eFormat, fourCompFormats)) {
			return 4;
		}

		return 1;
	}
}