package rosick.jglsdk.glimg;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.ARBTextureFloat.*;
import static org.lwjgl.opengl.ARBTextureCompressionBPTC.*;
import static org.lwjgl.opengl.EXTTextureInteger.*;
import static org.lwjgl.opengl.EXTTextureSRGB.*;
import static org.lwjgl.opengl.EXTTextureSnorm.*;
import static org.lwjgl.opengl.EXTTextureCompressionLATC.*;
import static org.lwjgl.opengl.NVTextureCompressionVTC.*;

import org.lwjgl.opengl.EXTTextureSnorm;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;

import rosick.jglsdk.glimg.ImageFormat.BitDepth;
import rosick.jglsdk.glimg.ImageFormat.ComponentOrder;
import rosick.jglsdk.glimg.ImageFormat.PixelComponents;
import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/integeruser/jglsdk for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class TextureGenerator {
	
	public static class ForcedConvertFlags {
		public static int FORCE_SRGB_COLORSPACE_FMT		= 0x0001;	// When possible, will force the format to use the sRGB colorspace. Does not cause erroring for formats that can't be sRGB, unless your GL implementation doesn't support sRGB.
		public static int FORCE_BC1_ALPHA_FMT			= 0x0002;	// When used with a BC1 texture, will force the texture to have an alpha. Ignored otherwise.
		public static int FORCE_LUMINANCE_FMT			= 0x0008;	// Red and RG textures will become luminance and luminance/alpha textures in all cases. Exceptions will be thrown if the GL implementation does not support those luminance/alpha formats (ie: is core).

		public static int FORCE_INTEGRAL_FMT			= 0x0020;	// Image formats that contain normalized integers will be uploaded as non-normalized integers. Ignored for floating-point or compressed formats.
		public static int FORCE_SIGNED_FMT				= 0x0040;	// Image formats that contain unsigned integers will be uploaded as signed integers. Ignored if the format is not an integer/integral format, or if it isn't BC4 or BC5 compressed.
		public static int FORCE_COLOR_RENDERABLE_FMT	= 0x0080;	// NOT YET SUPPORTED! Will force the use of formats that are required to be valid render targets. This will add components if necessary, but it will throw if conversion would require fundamentally changing the basic format (from signed to unsigned, compressed textures, etc).

		public static int FORCE_ARRAY_TEXTURE			= 0x0004;	// NOT YET SUPPORTED! The texture will be an array texture even if the depth is not present. Ignored for formats that can't be arrays. Will throw if array textures of that type are not supported (ie: cubemap arrays, 2D arrays for lesser hardware, etc).
		public static int USE_TEXTURE_STORAGE			= 0x0100;	// If ARB_texture_storage or GL 4.2 is available, then texture storage functions will be used to create the textures. Otherwise regular glTex* functions will be used.
		public static int FORCE_TEXTURE_STORAGE			= 0x0200;	// If ARB_texture_storage or GL 4.2 is available, then texture storage functions will be used to create the textures. Otherwise, an exception will be thrown.
		public static int USE_DSA						= 0x0400;	// If EXT_direct_state_access is available, then DSA functions will be used to create the texture. Otherwise, regular ones will be used.
		public static int FORCE_DSA						= 0x0800;	// If EXT_direct_state_access is available, then DSA functions will be used to create the texture. Otherwise, an exception will be thrown.
	};
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public static int createTexture(ImageSet imageSet, int forceConvertBits) {
		int textureName = glGenTextures();
		
		try {
			createTexture(textureName, imageSet, forceConvertBits);
		} catch (Exception e) {
			e.printStackTrace();
			glDeleteTextures(textureName);
		}
		
		return textureName;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	public static class OpenGLPixelTransferParams {
		public int format;					// The GLenum format value of pixel transfer operations.
		public int type;					// The GLenum type value of pixel transfer operations.
		public int blockByteCount;			// The size in bytes for blocks in compressed formats. Necessary to compute the size of the pixel data.
	};
	
	
	public static int getInternalFormat(ImageFormat imageFormat, int forceConvertBits) {
		int internalFormat = getStandardOpenGLFormat(imageFormat, forceConvertBits);

		boolean bConvertToLA = useLAInsteadOfRG(forceConvertBits);

		// Convert any R or RG formats to L or LA formats.
		switch (internalFormat) {
		case GL_COMPRESSED_RED_RGTC1:
			return LARG_COMPRESSED_CONV(bConvertToLA, internalFormat, GL_COMPRESSED_LUMINANCE_LATC1_EXT);
		case GL_COMPRESSED_SIGNED_RED_RGTC1:
			return LARG_COMPRESSED_CONV(bConvertToLA, internalFormat, GL_COMPRESSED_SIGNED_LUMINANCE_LATC1_EXT);
		//case GL_COMPRESSED_RG_RGTC2:
		//	return LARG_COMPRESSED_CONV(bConvertToLA, internalFormat, GL_COMPRESSED_LUMINANCE_ALPHA_LATC2_EXT);
		//case GL_COMPRESSED_SIGNED_RG_RGTC2:
		//	return LARG_COMPRESSED_CONV(bConvertToLA, internalFormat, GL_COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_EXT);

		case GL_R8I:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE8I_EXT);
		case GL_R16I:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE16I_EXT);
		case GL_R32I:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE32I_EXT);
		case GL_RG8I:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA8I_EXT);
		case GL_RG16I:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA16I_EXT);
		case GL_RG32I:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA32I_EXT);
		
		case GL_R8UI:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE8UI_EXT);
		case GL_R16UI:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE16UI_EXT);
		case GL_R32UI:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE32UI_EXT);
		case GL_RG8UI:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA8UI_EXT);
		case GL_RG16UI:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA16UI_EXT);
		case GL_RG32UI:
			return LARG_INTEGRAL_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA32UI_EXT);
		
		case GL_R16F:
			return LARG_FLOAT_CONV(bConvertToLA, internalFormat, GL_LUMINANCE16F_ARB);
		case GL_R32F:
			return LARG_FLOAT_CONV(bConvertToLA, internalFormat, GL_LUMINANCE32F_ARB);
		case GL_RG16F:
			return LARG_FLOAT_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA16F_ARB);
		case GL_RG32F:
			return LARG_FLOAT_CONV(bConvertToLA, internalFormat, GL_LUMINANCE_ALPHA32F_ARB);
		
		case EXTTextureSnorm.GL_R8_SNORM:
			return LARG_SNORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE8_SNORM);
		case EXTTextureSnorm.GL_R16_SNORM:
			return LARG_SNORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE16_SNORM);
		case EXTTextureSnorm.GL_RG8_SNORM:
			return LARG_SNORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE8_ALPHA8_SNORM);
		case EXTTextureSnorm.GL_RG16_SNORM:
			return LARG_SNORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE16_ALPHA16_SNORM);
	
		case GL_R8:
			return LARG_NORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE8);
		case GL_R16:
			return LARG_NORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE16);
		case GL_RG8:
			return LARG_NORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE8_ALPHA8);
		case GL_RG16:
			return LARG_NORM_CONV(bConvertToLA, internalFormat, GL_LUMINANCE16_ALPHA16);
		}
		
		return internalFormat;
	}
	
	
	public static OpenGLPixelTransferParams getUploadFormatType(ImageFormat imageFormat, int forceConvertBits)	{
		OpenGLPixelTransferParams upload = new OpenGLPixelTransferParams();
		upload.type = 0xFFFFFFFF;
		upload.format = 0xFFFFFFFF;
		upload.blockByteCount = 0;

		PixelDataType pixelDataType = getDataType(imageFormat, forceConvertBits);
		if (pixelDataType.ordinal() >= PixelDataType.NUM_UNCOMPRESSED_TYPES.ordinal()) {
			switch (pixelDataType) {
			case COMPRESSED_BC1:
			case COMPRESSED_UNSIGNED_BC4:
			case COMPRESSED_SIGNED_BC4:
				upload.blockByteCount = 8;
				break;
				
			default:
				upload.blockByteCount = 16;
				break;
			}

			// Provide reasonable parameters.
			upload.format = GL_RGBA;
			
			if (pixelDataType != PixelDataType.COMPRESSED_UNSIGNED_BC6H 
					|| pixelDataType != PixelDataType.COMPRESSED_SIGNED_BC6H) {
				upload.type = GL_FLOAT;
			} else {
				upload.type = GL_UNSIGNED_BYTE;
			}
				
			return upload;
		}

		upload.type = getOpenGLType(imageFormat, pixelDataType, forceConvertBits);
		upload.format = getOpenGLFormat(imageFormat, pixelDataType, forceConvertBits);

		return upload;
	}

	
	public static int getTextureType(ImageSet imageSet, int forceConvertBits) {
		Dimensions dims = imageSet.getDimensions();

		switch (dims.numDimensions) {
		case 1:
			// May be 1D or 1D array.
			if (isArrayTexture(imageSet, forceConvertBits)) {
				return GL_TEXTURE_1D_ARRAY;
			} else {
				return GL_TEXTURE_1D;
			}
			
		case 2:
			// 2D, 2D array, 2D cube, or 2D array cube.
			if (isArrayTexture(imageSet, forceConvertBits)) {
				if (imageSet.getFaceCount() > 1) {
					return GL_TEXTURE_CUBE_MAP_ARRAY;
				} else {
					return GL_TEXTURE_2D_ARRAY;
				}
			} else {
				if (imageSet.getFaceCount() > 1) {
					return GL_TEXTURE_CUBE_MAP;
				} else {
					return GL_TEXTURE_2D;
				}
			}
			
		case 3:
			// 3D.
			return GL_TEXTURE_3D;
		}

		return -1;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static int[] s_packedTypes = {
			GL_UNSIGNED_SHORT_5_6_5, 							// BitDepth.PACKED_16_BIT_565
			GL_UNSIGNED_SHORT_5_5_5_1, 							// BitDepth.PACKED_16_BIT_5551
			GL_UNSIGNED_SHORT_4_4_4_4, 							// BitDepth.PACKED_16_BIT_4444
			GL_UNSIGNED_INT_8_8_8_8, 							// BitDepth.PACKED_32_BIT_8888
			GL_UNSIGNED_INT_10_10_10_2, 						// BitDepth.PACKED_32_BIT_1010102
			GL_UNSIGNED_INT_24_8, 								// BitDepth.PACKED_32_BIT_248

			GL_UNSIGNED_SHORT_5_6_5_REV,						// BitDepth.PACKED_16_BIT_565_REV
			GL_UNSIGNED_SHORT_1_5_5_5_REV, 						// BitDepth.PACKED_16_BIT_1555_REV
			GL_UNSIGNED_SHORT_4_4_4_4_REV, 						// BitDepth.PACKED_16_BIT_4444_REV

			GL_UNSIGNED_INT_8_8_8_8_REV,						// BitDepth.PACKED_32_BIT_8888_REV
			GL_UNSIGNED_INT_2_10_10_10_REV,						// BitDepth.PACKED_32_BIT_2101010_REV
			GL_UNSIGNED_INT_10F_11F_11F_REV,					// BitDepth.PACKED_32_BIT_101111_REV
			GL_UNSIGNED_INT_5_9_9_9_REV, 						// BitDepth.PACKED_32_BIT_5999_REV
	};
	
	// Starts from PixelComponents.COLOR_RGB. non-integral vs. integral.
	private static int[] s_bgraFormats = { 
			GL_BGR, GL_BGR_INTEGER, 		// PixelComponents.COLOR_RGB
			GL_BGRA, GL_BGRA_INTEGER, 		// PixelComponents.COLOR_RGBX
			GL_BGRA, GL_BGRA_INTEGER, 		// PixelComponents.COLOR_RGBA
			GL_BGR, GL_BGR_INTEGER,			// PixelComponents.COLOR_RGB_sRGB
			GL_BGRA, GL_BGRA_INTEGER, 		// PixelComponents.COLOR_RGBX_sRGB
			GL_BGRA, GL_BGRA_INTEGER, 		// PixelComponents.COLOR_RGBA_sRGB
	};
	
	// Non-integral vs. integral.
	private static int[] s_rgbaFormats = { 
			GL_RED, 		GL_RED_INTEGER, 					// PixelComponents.COLOR_RED
			GL_RG, 			GL_RG_INTEGER, 						// PixelComponents.COLOR_RG
			GL_RGB, 		GL_RGB_INTEGER, 					// PixelComponents.COLOR_RGB
			GL_RGBA, 		GL_RGBA_INTEGER, 					// PixelComponents.COLOR_RGBX
			GL_RGBA, 		GL_RGBA_INTEGER, 					// PixelComponents.COLOR_RGBA
			GL_RGB, 		GL_RGB_INTEGER,						// PixelComponents.COLOR_RGB_sRGB
			GL_RGBA, 		GL_RGBA_INTEGER, 					// PixelComponents.COLOR_RGBX_sRGB
			GL_RGBA, 		GL_RGBA_INTEGER, 					// PixelComponents.COLOR_RGBA_sRGB
	};
	
	// Non-integral vs. integral.
	private static int[] s_rgbaLuminanceFormats = {
			GL_LUMINANCE,		GL_LUMINANCE_INTEGER_EXT,		// PixelComponents.COLOR_RED
			GL_LUMINANCE_ALPHA,	GL_LUMINANCE_ALPHA_INTEGER_EXT,	// PixelComponents.COLOR_RG
			GL_RGB,				GL_RGB_INTEGER,		            // PixelComponents.COLOR_RGB
			GL_RGBA,			GL_RGBA_INTEGER,				// PixelComponents.COLOR_RGBX
			GL_RGBA,			GL_RGBA_INTEGER,				// PixelComponents.COLOR_RGBA
			GL_RGB,				GL_RGB_INTEGER,					// PixelComponents.COLOR_RGB_sRGB
			GL_RGBA,			GL_RGBA_INTEGER,				// PixelComponents.COLOR_RGBX_sRGB
			GL_RGBA,			GL_RGBA_INTEGER,				// PixelComponents.COLOR_RGBA_sRGB
	};
	
	
	private static PixelDataType getDataType(ImageFormat imageFormat, int forceConvertBits) {
		boolean bForceIntegral = (forceConvertBits & ForcedConvertFlags.FORCE_INTEGRAL_FMT) != 0;
		boolean bForceSigned = (forceConvertBits & ForcedConvertFlags.FORCE_SIGNED_FMT) != 0;
		
		if (!bForceIntegral && !bForceSigned) {
			return imageFormat.getPixelDataType();
		}
			
		switch (imageFormat.getPixelDataType()) {
		case NORM_UNSIGNED_INTEGER:
			if (bForceIntegral) {
				if (bForceSigned) {
					return PixelDataType.SIGNED_INTEGRAL;
				} else {
					return PixelDataType.UNSIGNED_INTEGRAL;
				}
			} else {
				if (bForceSigned) {
					return PixelDataType.NORM_SIGNED_INTEGER;
				}
			}
			break;

		case NORM_SIGNED_INTEGER:
			if (bForceIntegral) {
				return PixelDataType.SIGNED_INTEGRAL;
			}
			break;

		case UNSIGNED_INTEGRAL:
			if (bForceSigned) {
				return PixelDataType.SIGNED_INTEGRAL;
			}
			break;

		case COMPRESSED_UNSIGNED_BC4:
			if (bForceSigned) {
				return PixelDataType.COMPRESSED_SIGNED_BC4;
			}
		case COMPRESSED_UNSIGNED_BC5:
			if (bForceSigned) {
				return PixelDataType.COMPRESSED_SIGNED_BC5;
			}
			break;
			
		default:
			break;
		}

		return imageFormat.getPixelDataType();
	}
	
	
	private static int getOpenGLType(ImageFormat imageFormat, PixelDataType pixelDataType, int forceConvertBits) {
		switch (imageFormat.getBitDepth()) {
		case COMPRESSED:
			return 0xFFFFFFFF;

		case PER_COMP_8:
			if (isTypeSigned(pixelDataType)) {
				return GL_BYTE;
			} else {
				return GL_UNSIGNED_BYTE;
			}

		case PER_COMP_16:
			if (pixelDataType == PixelDataType.FLOAT) {
				throwIfHalfFloatNotSupported();
				return GL_HALF_FLOAT;
			} else {
				if (isTypeSigned(pixelDataType)) {
					return GL_SHORT;
				} else {
					return GL_UNSIGNED_SHORT;
				}
			}

		case PER_COMP_32:
			if (pixelDataType == PixelDataType.FLOAT) {
				throwIfFloatNotSupported();
				return GL_FLOAT;
			} else {
				if (isTypeSigned(pixelDataType)) {
					return GL_INT;
				} else {
					return GL_UNSIGNED_INT;
				}
			}

		default:
			int typeIndex = imageFormat.getBitDepth().ordinal() - BitDepth.NUM_PER_COMPONENT.ordinal();

			if (!((0 <= typeIndex) && (typeIndex < s_packedTypes.length))) {
				throw new ImageFormatUnsupportedException("Couldn't get the GL type field, due to the bitdepth being outside the packed type array.");
			}

			int testType = s_packedTypes[typeIndex];

			// Test for implemented features.
			switch (testType) {
			case GL_UNSIGNED_INT_10F_11F_11F_REV:
				throwIfPackedFloatNotSupported();
				break;

			case GL_UNSIGNED_INT_5_9_9_9_REV:
				throwIfSharedExpNotSupported();
				break;
			}

			return testType;
		}
	}
	
	private static int getOpenGLFormat(ImageFormat imageFormat, PixelDataType pixelDataType, int forceConvertBits) {
		if (imageFormat.getPixelComponents() == PixelComponents.DEPTH) {
			throwIfDepthNotSupported();
			return GL_DEPTH_COMPONENT;
		}

		if (imageFormat.getPixelComponents() == PixelComponents.DEPTH_X) {
			throwIfDepthStencilNotSupported();
			return GL_DEPTH_STENCIL;
		}

		if (isTypeIntegral(pixelDataType)) {
			throwIfIntegralNotSupported();
		}

		int arrayOffset = isTypeIntegral(pixelDataType) ? 1 : 0;

		if (imageFormat.getComponentOrder() == ComponentOrder.BGRA) {
			int formatIndex = imageFormat.getPixelComponents().ordinal() - PixelComponents.COLOR_RGB.ordinal();
			formatIndex *= 2;

			if (!((0 <= formatIndex) && (formatIndex < s_bgraFormats.length))) {
				throw new ImageFormatUnsupportedException("Couldn't get the GL format field with ORDER_BGRA, due to the order being outside the bgraFormats array.");
			}
		
			return s_bgraFormats[formatIndex];
		} else {
			int formatIndex = imageFormat.getPixelComponents().ordinal();
			formatIndex *= 2;

			if (!((0 <= formatIndex) && (formatIndex < s_rgbaFormats.length))) {
				throw new ImageFormatUnsupportedException("Couldn't get the GL format field with ORDER_RGBA, due to the order being outside the rgbaFormats array.");
			}

			boolean isRGChannel = getComponentCount(imageFormat, forceConvertBits) < 3;

			if (useLAInsteadOfRG(forceConvertBits)) {
				if (isRGChannel && arrayOffset == 1) {
					throwIfEXT_IntegralNotSupported();
				}

				if (isRGChannel) {
					throwIfLANotSupported();
				}

				return s_rgbaLuminanceFormats[formatIndex];
			} else {
				if (isRGChannel) {
					throwIfRGNotSupported();
				}

				return s_rgbaFormats[formatIndex];
			}
		}
	}

	
	private static boolean isTypeSigned(PixelDataType pixelDataType) {
		PixelDataType[] signedIntegerFormats = {PixelDataType.SIGNED_INTEGRAL, PixelDataType.NORM_SIGNED_INTEGER};
		
		return Util.isOneOfThese(pixelDataType, signedIntegerFormats);
	}
	
	private static boolean isTypeIntegral(PixelDataType pixelDataType) {
		PixelDataType[] integralIntegerFormats = {PixelDataType.SIGNED_INTEGRAL, PixelDataType.UNSIGNED_INTEGRAL};
		
		return Util.isOneOfThese(pixelDataType, integralIntegerFormats);
	}
	
	
	private static int getComponentCount(ImageFormat imageFormat, int forceConvertBits) {
		// TODO: Forceconv.
		PixelComponents[] twoCompFormats 	= {PixelComponents.COLOR_RG, PixelComponents.DEPTH_X};
		PixelComponents[] threeCompFormats 	= {PixelComponents.COLOR_RGB, PixelComponents.COLOR_RGB_SRGB};
		PixelComponents[] fourCompFormats 	= {PixelComponents.COLOR_RGBX, PixelComponents.COLOR_RGBA,
				PixelComponents.COLOR_RGBX_SRGB, PixelComponents.COLOR_RGBA_SRGB};

		if (Util.isOneOfThese(imageFormat.getPixelComponents(), twoCompFormats)) {
			return 2;
		}

		if (Util.isOneOfThese(imageFormat.getPixelComponents(), threeCompFormats)) {
			return 3;
		}
		
		if (Util.isOneOfThese(imageFormat.getPixelComponents(), fourCompFormats)) {
			return 4;
		}

		return 1;
	}
	
	private static boolean useLAInsteadOfRG(int forceConvertBits) {
		if ((forceConvertBits & ForcedConvertFlags.FORCE_LUMINANCE_FMT) != 0) {
			return true;
		}

		try {
			throwIfRGNotSupported();
		}
		catch (ImageFormatUnsupportedException e) {
			return true;
		}

		return false;
	}

	
	private static boolean isArrayTexture(ImageSet imageSet, int forceConvertBits) {
		// No such thing as 3D array textures.
		if (imageSet.getDimensions().numDimensions == 3) {
			return false;
		}

		if ((forceConvertBits & ForcedConvertFlags.FORCE_ARRAY_TEXTURE) != 0 || imageSet.getArrayCount() > 1) {
			return true;
		}
			
		return false;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static class ImageFormatUnsupportedException extends RuntimeException {
		public ImageFormatUnsupportedException() {
			super("The image format is not supported by this OpenGL implementation.");
		}
		
		public ImageFormatUnsupportedException(String message) {
			super(message);
		}

		private static final long serialVersionUID = 6252749121362296508L;		
	}
	
	
	private static void throwIfS3TCNotSupported() {
		if (!GLContext.getCapabilities().GL_EXT_texture_compression_s3tc) {
			throw new ImageFormatUnsupportedException("S3TC not supported.");
		}
	}

	private static void throwIfLATCNotSupported() {
		if (!GLContext.getCapabilities().GL_EXT_texture_compression_latc) {
			throw new ImageFormatUnsupportedException("LATC not supported.");
		}
	}

	private static void throwIfRGTCNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			if (!GLContext.getCapabilities().GL_ARB_texture_compression_rgtc || !GLContext.getCapabilities().GL_EXT_texture_compression_rgtc) {
				throw new ImageFormatUnsupportedException("RGTC, part of GL 3.0 and above, is not supported.");
			}
		}
	}

	private static void throwIfBPTCNotSupported() {
		if (!GLContext.getCapabilities().GL_ARB_texture_compression_bptc) {
			throw new ImageFormatUnsupportedException("PBTC not supported.");
		}
	}

	private static void throwIfSRGBNotSupported() {
		if (!GLContext.getCapabilities().OpenGL21) {
			if (!GLContext.getCapabilities().GL_EXT_texture_sRGB) {
				throw new ImageFormatUnsupportedException("sRGB textures not supported.");
			}
		}
	}

	private static void throwIfEXT_SRGBNotSupported() {
		if (!GLContext.getCapabilities().GL_EXT_texture_sRGB) {
			throw new ImageFormatUnsupportedException("sRGB and S3TC textures not supported.");
		}
	}

	private static void throwIfSharedExpNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			if (!GLContext.getCapabilities().GL_EXT_texture_shared_exponent) {
				throw new ImageFormatUnsupportedException("Shared exponent texture format not supported.");
			}
		}
	}

	private static void throwIfFloatNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			if (!GLContext.getCapabilities().GL_ARB_texture_float) {
				throw new ImageFormatUnsupportedException("Float textures not supported.");
			}
		}
	}
	
	private static void throwIfEXT_FloatNotSupported() {
		if (!GLContext.getCapabilities().GL_ARB_texture_float) {
			throw new ImageFormatUnsupportedException("ARB Float textures not supported.");
		}
	}

	private static void throwIfHalfFloatNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			if (!GLContext.getCapabilities().GL_ARB_half_float_pixel) {
				throw new ImageFormatUnsupportedException("Half floats textures not supported.");
			}
		}
	}

	private static void throwIfSnormNotSupported() {
		if (!GLContext.getCapabilities().OpenGL31) {
			if (!GLContext.getCapabilities().GL_EXT_texture_snorm) {
				throw new ImageFormatUnsupportedException("Signed normalized textures not supported.");
			}
		}
	}

	private static void throwIfEXT_SnormNotSupported() {
		if (!GLContext.getCapabilities().GL_EXT_texture_snorm) {
			throw new ImageFormatUnsupportedException("Signed normalized texture extension not supported.");
		}
	}

	private static void throwIfPackedFloatNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			if (!GLContext.getCapabilities().GL_EXT_packed_float) {
				throw new ImageFormatUnsupportedException("Packed 11, 11, 10 float textures not supported.");
			}
		}
	}
	
	private static void throwIfIntegralNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			if (!GLContext.getCapabilities().GL_EXT_texture_integer) {
				throw new ImageFormatUnsupportedException("Integral textures not supported.");
			}
		}
	}

	private static void throwIfEXT_IntegralNotSupported() {
		if (!GLContext.getCapabilities().GL_EXT_texture_integer) {
			throw new ImageFormatUnsupportedException("Integral texture extension not supported.");
		}
	}

	private static void throwIfRGNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			if (!GLContext.getCapabilities().GL_ARB_texture_rg) {
				throw new ImageFormatUnsupportedException("RG textures not supported.");
			}
		}
	}

	private static void throwIfLANotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {
			return;
		}
		
		if (!GLContext.getCapabilities().OpenGL32) {
			if (!GLContext.getCapabilities().GL_ARB_compatibility) {
				throw new ImageFormatUnsupportedException("Core OpenGL contexts cannot use Luminance/alpha.");
			}
		}
		else {
			int profileMask = glGetInteger(GL32.GL_CONTEXT_PROFILE_MASK);
			
			if ((profileMask & GL32.GL_CONTEXT_CORE_PROFILE_BIT) != 0) {
				throw new ImageFormatUnsupportedException("Core OpenGL contexts cannot use Luminance/alpha.");

			}
		}
	}

	private static void throwIfForceRendertarget(int forceConvertBits) {
		if ((forceConvertBits & ForcedConvertFlags.FORCE_COLOR_RENDERABLE_FMT) != 0) {
			throw new CannotForceRenderTargetException();
		}
	}

	private static void throwIfDepthNotSupported() {
		if (!GLContext.getCapabilities().OpenGL14) {			// Yes, really. Depth textures are old.
			if (!GLContext.getCapabilities().GL_ARB_depth_texture) {
				throw new ImageFormatUnsupportedException("Depth textures not supported.");
			}
		}
	}

	private static void throwIfDepthStencilNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {			
			if (!GLContext.getCapabilities().GL_EXT_packed_depth_stencil || !GLContext.getCapabilities().GL_ARB_framebuffer_object) {
				throw new ImageFormatUnsupportedException("Depth/stencil textures not supported.");
			}
		}
	}

	private static void throwIfDepthFloatNotSupported() {
		if (!GLContext.getCapabilities().OpenGL30) {			
			if (!GLContext.getCapabilities().GL_NV_depth_buffer_float) {
				throw new ImageFormatUnsupportedException("Floating-point depth buffers not supported.");
			}
		}
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	// Ordered by BaseDataFormat*2. The first is 16-bit, the second is 32-bit.
	private static int[] s_floatFormats = {
			GL_R16F,
			GL_R32F,
			GL_RG16F,
			GL_RG32F,
			GL_RGB16F,
			GL_RGB32F,
			GL_RGB16F,
			GL_RGB32F,
			GL_RGBA16F,
			GL_RGBA32F,
			0,								// sRGB
			0,								// sRGB
			0,								// sRGB
			0,								// sRGB
			0,								// sRGB
			0,								// sRGB
			0,								// 16-bit float depth buffer. 
			GL_DEPTH_COMPONENT32F,
			0,								// UNorm+stencil.
	};
	
	// Ordered by number of components * size (8, 16=>0, 1, 2).
	private static int[] s_signedNormFormats = {
			EXTTextureSnorm.GL_R8_SNORM,	EXTTextureSnorm.GL_R16_SNORM,
			EXTTextureSnorm.GL_RG8_SNORM,	EXTTextureSnorm.GL_RG16_SNORM,
			EXTTextureSnorm.GL_RGB8_SNORM,	EXTTextureSnorm.GL_RGB16_SNORM,
			EXTTextureSnorm.GL_RGBA8_SNORM,	EXTTextureSnorm.GL_RGBA16_SNORM,
	};
	
	// Ordered by number of components * size (8, 16, 32=>0, 1, 2).
	private static int[] s_signIntegralFormats = {
			GL_R8I,		GL_R16I,	GL_R32I,
			GL_RG8I,	GL_RG16I,	GL_RG32I,
			GL_RGB8I,	GL_RGB16I,	GL_RGB32I,
			GL_RGBA8I,	GL_RGBA16I,	GL_RGBA32I,
	};

	// Ordered by number of components * size (8, 16, 32=>0, 1, 2).
	private static int[] s_unsignIntegralFormats = {
			GL_R8UI,	GL_R16UI,		GL_R32UI,
			GL_RG8UI,	GL_RG16UI,		GL_RG32UI,
			GL_RGB8UI,	GL_RGB16UI,		GL_RGB32UI,
			GL_RGBA8UI,	GL_RGBA16UI,	GL_RGBA32UI,
	};
	
	
	private static int getStandardOpenGLFormat(ImageFormat imageFormat, int forceConvertBits) {
		PixelDataType pixelDataType = getDataType(imageFormat, forceConvertBits);

		switch (pixelDataType) {
		case NORM_UNSIGNED_INTEGER:
			// Only 16-bit for non Depth_x.
			if (imageFormat.getPixelComponents() == PixelComponents.DEPTH) {
				throwIfDepthNotSupported();
				return GL_DEPTH_COMPONENT16;
			}

			// Only 24x8 for this.
			if (imageFormat.getPixelComponents() == PixelComponents.DEPTH_X) {
				throwIfDepthStencilNotSupported();
				return GL_DEPTH24_STENCIL8;
			}

			// Color formats.
			if (isSRGBFormat(imageFormat, forceConvertBits)) {
				throwIfSRGBNotSupported();

				if (getComponentCount(imageFormat, forceConvertBits) == 3) {
					return GL_SRGB8;
				} else {
					return GL_SRGB8_ALPHA8;
				}
			}

			switch (imageFormat.getBitDepth()) {
			case PER_COMP_8: {
				int components[] = {GL_R8, GL_RG8, GL_RGB8, GL_RGBA8};
				int numComponents = getComponentCount(imageFormat, forceConvertBits);

				return components[numComponents - 1];
			}

			case PER_COMP_16: {
				int components[] = {GL_R16, GL_RG16, GL_RGB16, GL_RGBA16};
				int numComponents = getComponentCount(imageFormat, forceConvertBits);

				return components[numComponents - 1];
			}

			case PACKED_16_BIT_565:
			case PACKED_16_BIT_565_REV:
				return GL_RGB8;

			case PACKED_16_BIT_5551:
			case PACKED_16_BIT_1555_REV:
				return GL_RGB5_A1;

			case PACKED_16_BIT_4444:
			case PACKED_16_BIT_4444_REV:
				return GL_RGBA4;

			case PACKED_32_BIT_8888:
			case PACKED_32_BIT_8888_REV:
				return GL_RGBA8;

			case PACKED_32_BIT_1010102:
			case PACKED_32_BIT_2101010_REV:
				return GL_RGB10_A2;
			
			default:
				break;
			}

			throw new ImageFormatUnsupportedException("Unisgned normalize integer doesn't match accepted bitdepths.");

		case NORM_SIGNED_INTEGER: {
			throwIfSnormNotSupported();
			throwIfForceRendertarget(forceConvertBits);

			int numComponents = getComponentCount(imageFormat, forceConvertBits);
			int compSize = perComponentSize(imageFormat, forceConvertBits);
			compSize /= 16; // map to 0, 1.

			int index = ((numComponents - 1) * 2) + compSize;

			return s_signedNormFormats[index];
		}
			
		case UNSIGNED_INTEGRAL:
		case SIGNED_INTEGRAL: {
			throwIfIntegralNotSupported();
			
			int numComponents = getComponentCount(imageFormat, forceConvertBits);
			int compSize = perComponentSize(imageFormat, forceConvertBits);
			compSize /= 16; // map to 0, 1, 2.

			int index = ((numComponents - 1) * 3) + compSize;

			if (pixelDataType == PixelDataType.SIGNED_INTEGRAL) {
				return s_signIntegralFormats[index];
			} else {
				return s_unsignIntegralFormats[index];
			}
		}
		
		case FLOAT: {
			throwIfFloatNotSupported();
			
			if (imageFormat.getBitDepth().ordinal() < BitDepth.NUM_PER_COMPONENT.ordinal()) {
				int offset = 0;
				if (imageFormat.getBitDepth() == BitDepth.PER_COMP_32) {
					offset = 1;
				} else {
					throwIfHalfFloatNotSupported();
				}

				if (imageFormat.getPixelComponents() == PixelComponents.DEPTH) {
					throwIfDepthFloatNotSupported();
				}

				return throwInvalidFormatIfZero(s_floatFormats[(2 * imageFormat.getPixelComponents().ordinal()) + offset]);
			} else {
				// Only one packed format.
				throwIfPackedFloatNotSupported();
				return GL_R11F_G11F_B10F;
			}
		}

		case SHARED_EXP_FLOAT:
			throwIfSharedExpNotSupported();
			throwIfForceRendertarget(forceConvertBits);
			return GL_RGB9_E5;

		case COMPRESSED_BC1: {
			throwIfS3TCNotSupported();

			if (isSRGBFormat(imageFormat, forceConvertBits)) {
				throwIfEXT_SRGBNotSupported();

				if ((forceConvertBits & ForcedConvertFlags.FORCE_BC1_ALPHA_FMT) != 0) {
					return GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;
				} else {
					if (getComponentCount(imageFormat, forceConvertBits) == 3) {
						return GL_COMPRESSED_SRGB_S3TC_DXT1_EXT;
					} else {
						return GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;
					}
				}
			} else {
				if ((forceConvertBits & ForcedConvertFlags.FORCE_BC1_ALPHA_FMT) != 0) {
					return GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
				} else {
					if (getComponentCount(imageFormat, forceConvertBits) == 3) {
						return GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
					} else {
						return GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
					}
				}
			}
		}

		case COMPRESSED_BC2:
			throwIfS3TCNotSupported();

			if (isSRGBFormat(imageFormat, forceConvertBits)) {
				throwIfEXT_SRGBNotSupported();
				return GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT;
			} else {
				return GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
			}

		case COMPRESSED_BC3:
			throwIfS3TCNotSupported();

			if (isSRGBFormat(imageFormat, forceConvertBits)) {
				throwIfEXT_SRGBNotSupported();
				return GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT;
			} else {
				return GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
			}

		case COMPRESSED_UNSIGNED_BC4:
			return GL_COMPRESSED_RED_RGTC1;
			
		case COMPRESSED_SIGNED_BC4:
			return GL_COMPRESSED_SIGNED_RED_RGTC1;

		case COMPRESSED_UNSIGNED_BC5:
			throw new RuntimeException("No such constant in LWJGL :(");
			//return GL_COMPRESSED_RG_RGTC2;
			
		case COMPRESSED_SIGNED_BC5:
			throw new RuntimeException("No such constant in LWJGL :(");
			//return GL_COMPRESSED_SIGNED_RG_RGTC2;

		case COMPRESSED_UNSIGNED_BC6H:
			throwIfBPTCNotSupported();
			return GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_ARB;

		case COMPRESSED_SIGNED_BC6H:
			throwIfBPTCNotSupported();
			return GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_ARB;

		case COMPRESSED_BC7:
			throwIfBPTCNotSupported();

			if (isSRGBFormat(imageFormat, forceConvertBits)) {
				return GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM_ARB;
			} else {
				return GL_COMPRESSED_RGBA_BPTC_UNORM_ARB;
			}
			
		default:
			break;
		}

		throw new ImageFormatUnsupportedException("???");
	}


	private static boolean isSRGBFormat(ImageFormat imageFormat, int forceConvertBits) {
		PixelComponents[] srgbFormats = {PixelComponents.COLOR_RGB_SRGB, PixelComponents.COLOR_RGBX_SRGB, 
				PixelComponents.COLOR_RGBA_SRGB};
		
		if (Util.isOneOfThese(imageFormat.getPixelComponents(), srgbFormats)) {
			return true;
		}
	
		if ((forceConvertBits & ForcedConvertFlags.FORCE_SRGB_COLORSPACE_FMT) == 0) {
			return false;
		}
		
		PixelDataType[] srgbTypes = {PixelDataType.NORM_UNSIGNED_INTEGER, PixelDataType.COMPRESSED_BC1, 
				PixelDataType.COMPRESSED_BC2, PixelDataType.COMPRESSED_BC3, PixelDataType.COMPRESSED_BC7};
	
		if (Util.isOneOfThese(imageFormat.getPixelDataType(), srgbTypes)) {
			if (imageFormat.getPixelDataType() != PixelDataType.NORM_UNSIGNED_INTEGER) {
				return true;
			}
		} else {
			return false;
		}
		
		// Unsigned normalized integers. Check for RGB or RGBA components.
		PixelComponents convertableFormats[] = {PixelComponents.COLOR_RGB, PixelComponents.COLOR_RGBX, PixelComponents.COLOR_RGBA};
		
		if (Util.isOneOfThese(imageFormat.getPixelComponents(), convertableFormats)) {
			return true;
		}
	
		return false;
	}
	
	private static int perComponentSize(ImageFormat imageFormat, int forceConvertBits) {
		// TODO: Forceconv.
		switch (imageFormat.getBitDepth()) {
		case PER_COMP_8: 	return 8;
		case PER_COMP_16: 	return 16;
		case PER_COMP_32: 	return 32;
		default:			return -1;
		}
	}
	
	private static int throwInvalidFormatIfZero(int input) {
		if (input == 0) {
			throw new ImageFormatUnsupportedException();
		}
			
		return input;
	}
	
	
	private static int LARG_COMPRESSED_CONV(boolean bConvertToLA, int oldformat, int newFormat) {
		if (bConvertToLA) {
			throwIfLATCNotSupported();
			return newFormat;
		} else {
			throwIfRGTCNotSupported();
		}
		
		return oldformat;
	}

	private static int LARG_INTEGRAL_CONV(boolean bConvertToLA, int oldformat, int newFormat) {
		if (bConvertToLA) {
			throwIfEXT_IntegralNotSupported();
			return newFormat;
		} else {
			throwIfRGNotSupported();
		}
		
		return oldformat;
	}

	private static int LARG_FLOAT_CONV(boolean bConvertToLA, int oldformat, int newFormat) {
		if (bConvertToLA) {
			throwIfEXT_FloatNotSupported();
			return newFormat;
		} else {
			throwIfRGNotSupported();
		}
		
		return oldformat;
	}
	
	private static int LARG_SNORM_CONV(boolean bConvertToLA, int oldformat, int newFormat) {
		if (bConvertToLA) {
			throwIfEXT_SnormNotSupported();
			return newFormat;
		} else {
			throwIfRGNotSupported();
		}
		
		return oldformat;
	}
	
	private static int LARG_NORM_CONV(boolean bConvertToLA, int oldformat, int newFormat) {
		if (bConvertToLA) {
			throwIfLANotSupported();
			return newFormat;
		} else {
			throwIfRGNotSupported();
		}
		
		return oldformat;
	}	
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private static class TextureBinder {
		
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			
			if (m_texture != 0) {
				glBindTexture(m_texTarget, 0);
			}
		}
		
		
		void bind(int textureTarget, int texture) {
			m_texture = texture;
			m_texTarget = textureTarget;
			glBindTexture(m_texTarget, m_texture);
		}

		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private int m_texture;
		private  int m_texTarget;
	};
	
	
	private static class CannotForceTextureStorage extends RuntimeException {
		private static final long serialVersionUID = -3344749448213535867L;

		private CannotForceTextureStorage() {
			super("The current OpenGL implementation does not support ARB_texture_storage or GL 4.2 or above.");
		}
	}
	
	private static class CannotForceDSAUsage extends RuntimeException {
		private static final long serialVersionUID = 7928385809263161372L;
		
		private CannotForceDSAUsage() {
			super("The current OpenGL implementation does not support EXT_direct_state_access.");
		}
	}
	
	private static class CannotForceRenderTargetException extends RuntimeException {
		private static final long serialVersionUID = 652152072608548652L;

		private CannotForceRenderTargetException() {
			super("The image format cannot be forced to be a renderable format without compromising the data.");
		}		
	}
	
	
	private static void createTexture(int textureName, ImageSet imageSet, int forceConvertBits) {
		if ((forceConvertBits & ForcedConvertFlags.FORCE_TEXTURE_STORAGE) != 0) {
			if (!isTextureStorageSupported()) {
				throw new CannotForceTextureStorage();
			}
			
			forceConvertBits |= ForcedConvertFlags.USE_TEXTURE_STORAGE;
		}

		if ((forceConvertBits & ForcedConvertFlags.USE_TEXTURE_STORAGE) != 0) {
			if (!isTextureStorageSupported()) {
				forceConvertBits &= ~ForcedConvertFlags.USE_TEXTURE_STORAGE;
			}
		}

		if ((forceConvertBits & ForcedConvertFlags.FORCE_DSA) != 0) {
			if (!isDirectStateAccessSupported()) {
				throw new CannotForceDSAUsage();
			}
			
			forceConvertBits |= ForcedConvertFlags.USE_DSA;
		}

		if ((forceConvertBits & ForcedConvertFlags.USE_DSA) != 0) {
			if (!isDirectStateAccessSupported()) {
				forceConvertBits &= ~ForcedConvertFlags.USE_DSA;
			}
		}
		
		ImageFormat imageFormat = imageSet.getFormat();
		int internalFormat = getInternalFormat(imageFormat, forceConvertBits);
		OpenGLPixelTransferParams upload = getUploadFormatType(imageFormat, forceConvertBits);

		switch (getTextureType(imageSet, forceConvertBits)) {
		case GL_TEXTURE_2D:
			build2DTexture(textureName, imageSet, forceConvertBits, internalFormat, upload);
			break;
			
		default:
			Util.throwNotYetPortedException();
		}
	}
	
	
	private static boolean isTextureStorageSupported() {
		if (!GLContext.getCapabilities().OpenGL42) {
			if (!GLContext.getCapabilities().GL_ARB_texture_storage) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean isDirectStateAccessSupported() {
		if (!GLContext.getCapabilities().GL_EXT_direct_state_access) {
			return false;
		}
		
		return true;
	}

	
	private static void build2DTexture(int textureName, ImageSet imageSet,
			int forceConvertBits, int internalFormat,
			OpenGLPixelTransferParams upload) {
		setupUploadState(imageSet.getFormat(), forceConvertBits);
		TextureBinder textureBinder = new TextureBinder();
		
		if ((forceConvertBits & ForcedConvertFlags.USE_DSA) == 0) {
			textureBinder.bind(GL_TEXTURE_2D, textureName);
			textureName = 0;
		}

		int numMipmaps = imageSet.getMipmapCount();
		
		texStorageBase(GL_TEXTURE_2D, forceConvertBits, imageSet.getDimensions(),
			numMipmaps, internalFormat, upload, textureName);

		for (int mipmapLevel = 0; mipmapLevel < numMipmaps; mipmapLevel++) {
			Dimensions imageDimensions = imageSet.getDimensions(mipmapLevel);
			ByteBuffer imageDataBuffer = imageSet.getImageData(mipmapLevel, 0, 0);

			texSubImage(textureName, GL_TEXTURE_2D, mipmapLevel, internalFormat, imageDimensions, upload,
				imageDataBuffer, imageSet.getSize(mipmapLevel));
		}

		finalizeTexture(textureName, GL_TEXTURE_2D, imageSet);
	}

	private static void setupUploadState(ImageFormat imageFormat, int forceConvertBits) {
		glPixelStorei(GL_UNPACK_SWAP_BYTES, GL_FALSE);
		glPixelStorei(GL_UNPACK_LSB_FIRST, GL_FALSE);
		glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
		glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
		glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
		glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, 0);
		glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0);
		glPixelStorei(GL_UNPACK_ALIGNMENT, imageFormat.getLineAlignment());
	}
	
	
	// Works for just 1D/2D/3D.
	private static void texStorageBase(int textureTarget, int forceConvertBits,
			Dimensions imageDimensions, int numMipmaps, int internalFormat,
			OpenGLPixelTransferParams upload, int textureName) {
		if ((forceConvertBits & ForcedConvertFlags.USE_TEXTURE_STORAGE) != 0) {
			Util.throwNotYetPortedException();
		} else {
			manTexStorageBase(textureName, textureTarget, imageDimensions,
					numMipmaps, internalFormat, upload);
		}
	}

	// Only works for TEXTURE_1D, 2D, and 3D.
	// DSA-style
	private static void manTexStorageBase(int texture, int textureTarget,
			Dimensions imageDimensions, int numMipmaps, int internalFormat,
			OpenGLPixelTransferParams upload) {
		// Zero means bound, so no DSA.
		if (texture == 0) {
			manTexStorageBase(textureTarget, imageDimensions, numMipmaps,
					internalFormat, upload);
			return;
		}

		Util.throwNotYetPortedException();
	}

	private static void manTexStorageBase(int textureTarget,
			Dimensions imageDimensions, int numMipmaps, int internalFormat,
			OpenGLPixelTransferParams upload) {
		for (int mipmap = 0; mipmap < numMipmaps; mipmap++) {
			Dimensions levelDims = Util.calcMipmapLevelDimensions(imageDimensions, mipmap);
			
			switch (imageDimensions.numDimensions) {
			case 1:
				Util.throwNotYetPortedException();
				break;

			case 2:
				glTexImage2D(textureTarget, mipmap, internalFormat,
						levelDims.width, levelDims.height, 0,
						upload.format, upload.type, (ByteBuffer) null);
				break;

			case 3:
				Util.throwNotYetPortedException();
				break;
			}
		}
	}
	
	
	private static void texSubImage(int texture, int textureTarget,
			int mipmapLevel, int internalFormat, Dimensions imageDimensions,
			OpenGLPixelTransferParams upload, ByteBuffer imageData, int imageSize) {
		// Zero means bound, so no DSA.
		if (texture == 0) {
			texSubImage(textureTarget, mipmapLevel, internalFormat,
					imageDimensions, upload, imageData, imageSize);
			return;
		}

		Util.throwNotYetPortedException();
	}

	private static void texSubImage(int textureTarget, int mipmapLevel,
			int internalFormat, Dimensions imageDimensions,
			OpenGLPixelTransferParams upload, ByteBuffer imageData, int imageSize) {
		switch (imageDimensions.numDimensions) {
		case 1:
			if (upload.blockByteCount != 0) {
				glCompressedTexSubImage1D(textureTarget, mipmapLevel, 0,
						imageDimensions.width, internalFormat, imageData);
			} else {
				glTexSubImage1D(textureTarget, mipmapLevel, 0,
						imageDimensions.width, upload.format, upload.type,
						imageData);
			}
			break;

		case 2:
			if (upload.blockByteCount != 0) {
				glCompressedTexSubImage2D(textureTarget, mipmapLevel, 0, 0,
						imageDimensions.width, imageDimensions.height,
						internalFormat, imageData);
			} else {
				glTexSubImage2D(textureTarget, mipmapLevel, 0, 0,
						imageDimensions.width, imageDimensions.height,
						upload.format, upload.type, imageData);
			}
			break;

		case 3:
			if (upload.blockByteCount != 0) {
				glCompressedTexSubImage3D(textureTarget, mipmapLevel, 0, 0, 0,
						imageDimensions.width, imageDimensions.height,
						imageDimensions.depth, internalFormat, imageData);
			} else {
				glTexSubImage3D(textureTarget, mipmapLevel, 0, 0, 0,
						imageDimensions.width, imageDimensions.height,
						imageDimensions.depth, upload.format, upload.type,
						imageData);
			}
			break;
		}
	}
	
	
	private static void finalizeTexture(int texture, int textureTarget, ImageSet imageSet) {
		// Zero means bound, so no DSA.
		if (texture == 0) {
			finalizeTexture(textureTarget, imageSet);
			return;
		}

		Util.throwNotYetPortedException();
	}

	// Texture must be bound to the target.
	private static void finalizeTexture(int textureTarget, ImageSet imageSet) {
		int numMipmaps = imageSet.getMipmapCount();
		
		glTexParameteri(textureTarget, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(textureTarget, GL_TEXTURE_MAX_LEVEL, numMipmaps - 1);

		// Ensure the texture is texture-complete.
		ImageFormat imageFormat = imageSet.getFormat();
		if (isTypeIntegral(imageFormat.getPixelDataType())) {
			glTexParameteri(textureTarget, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(textureTarget, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		} else {
			glTexParameteri(textureTarget, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(textureTarget, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}
	}
}