package rosick.jglsdk.glimg;


/**
 * Visit https://github.com/integeruser/jglsdk for project info, updates and license terms.
 * 
 * @author integeruser
 */
class ImageFormat {
	
	enum PixelDataType {
		NORM_UNSIGNED_INTEGER,				// Image data are unsigned integers that are mapped to floats on the range [0, 1].
		NORM_SIGNED_INTEGER,				// Image data are signed integers that are mapped to floats on the range [-1, 1].
		UNSIGNED_INTEGRAL,					// Image data are unsigned integers.
		SIGNED_INTEGRAL,					// Image data are signed integers.
		FLOAT,								// Image data are individual floating-point numbers.
		SHARED_EXP_FLOAT,					// Image data are floats, but each pixel uses the same exponent.
		NUM_UNCOMPRESSED_TYPES,

		COMPRESSED_BC1,						// Image data is compressed with DXT1/BC1 compression. Unsigned normalized integers.
		COMPRESSED_BC2,						// Image data is compressed with DXT3/BC2 compression. Unsigned normalized integers.
		COMPRESSED_BC3,						// Image data is compressed with DXT5/BC3 compression. Unsigned normalized integers.
		COMPRESSED_UNSIGNED_BC4,			// Image is compressed with BC4 compression (1-component), with unsigned normalized integers.
		COMPRESSED_SIGNED_BC4,				// Image is compressed with BC4 compression (1-component), with signed normalized integers.
		COMPRESSED_UNSIGNED_BC5,			// Image is compressed with BC5 compression (2-component), with unsigned normalized integers.
		COMPRESSED_SIGNED_BC5,				// Image is compressed with BC5 compression (2-component), with signed normalized integers.
		COMPRESSED_UNSIGNED_BC6H,			// Image is compressed with BC6H compression, with unsigned floats [0, +inf).
		COMPRESSED_SIGNED_BC6H,				// Image is compressed with BC6H compression, with floats.
		COMPRESSED_BC7,						// Image data is compressed with BC7 compression. Unsigned normalized integers.

		NUM_TYPES
	};
	
	enum PixelComponents {
		COLOR_RED,							// Image contains 1 color component, namely red.
		COLOR_RG,							// Image contains 2 color components, red and green.
		COLOR_RGB,							// Image contains 3 color components, red, green, and blue.
		COLOR_RGBX,							// Image contains 3 color components, red, green, and blue. There is a fourth component, which takes up space in the data but should be discarded.
		COLOR_RGBA,							// Image contains 4 color components, red, green, blue, and alpha.
		COLOR_RGB_SRGB,						// Image contains 3 color components, which are in the sRGB colorspace.
		COLOR_RGBX_SRGB,					// Image contains 3 color components, which are in the sRGB colorspace. There is a fourth component, which takes up space in the data but should be discarded.
		COLOR_RGBA_SRGB,					// Image contains 4 color components; the RGB components are in the sRGB colorspace.

		DEPTH,								// Image contains a single depth component.
		DEPTH_X,							// Image contains a depth value (unsigned normalized integer) and a second component, who's value is discarded/irrelevant.

		NUM_FORMATS
	};
	
	enum ComponentOrder {
		RGBA,								// Standard RGBA ordering.
		BGRA,								// Often used in conjunction with _REV Bitdepths.
		RGBE,								// For PixelDataType.DT_SHARED_EXP_FLOAT types. The E is the exponent, and it comes first.

		DEPTH_STENCIL,						// Ordering for depth and depth-stencil image formats.

		COMPRESSED,							// The order is built into the compressed data format.

		NUM_ORDERS
	};
	
	enum BitDepth {
		COMPRESSED,							// Used for compressed data types. They do not have a bitdepth.

		PER_COMP_8,							// Each component takes up 8 bits.
		PER_COMP_16,						// Each component takes up 16 bits.
		PER_COMP_32,						// Each component takes up 32 bits.
		NUM_PER_COMPONENT,			

		PACKED_16_BIT_565,					// The first and third components take up 5 bits, while the second takes up 6.
		PACKED_16_BIT_5551,					// The first three components take up 5 bits, and the last takes up 1.
		PACKED_16_BIT_4444,					// Each component takes up 4 bits.

		PACKED_32_BIT_8888,					// Each component takes up 8 bits.
		PACKED_32_BIT_1010102,				// The first three components take up 10 bits, and the last takes up 2.
		PACKED_32_BIT_248,					// The first component takes up 24 bits; the second takes up 8 bits.

		PACKED_16_BIT_565_REV,				// Reverse order. The first and third components take up 5 bits, while the second takes up 6.
		PACKED_16_BIT_1555_REV,				// Reverse order. The first three components take up 5 bits, and the last takes up 1.
		PACKED_16_BIT_4444_REV,				// Reverse order. Each component takes up 4 bits.

		PACKED_32_BIT_8888_REV,				// Reverse order. Each component takes up 8 bits.
		PACKED_32_BIT_2101010_REV,			// Reverse order. The first three components take up 10 bits, and the last takes up 2.
		PACKED_32_BIT_101111_REV,			// Reverse order. The first two components take 11 bits, and the third takes 10. Used for PixelDataType.DT_FLOAT types.
		PACKED_32_BIT_5999_REV,				// Reverse order. The first 3 components take 9 bits, and the last takes 5. Used for PixelDataType.DT_SHARED_EXP_FLOAT types. 

		NUM_BITDEPTH
	};
	
	
	static class UncheckedImageFormat {
		PixelDataType type;					// The type of pixel data.
		PixelComponents format;				// The components stored by a pixel.
		ComponentOrder order;				// The order of the components of the pixel.
		BitDepth bitDepth;					// The bitdepth of each pixel component.
		int lineAlignment;					// The byte alignment of a horizontal line of pixel data.
		
		
		UncheckedImageFormat() {
		}

		UncheckedImageFormat(PixelDataType type, PixelComponents format,
				ComponentOrder order, BitDepth bitdepth, int lineAlignment) {
			this.type = type;
			this.format = format;
			this.order = order;
			this.bitDepth = bitdepth;
			this.lineAlignment = lineAlignment;
		}


		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
		private String validateFormatText() {
			// Alignment only matters for uncompressed types.
			if (type.ordinal() < PixelDataType.NUM_UNCOMPRESSED_TYPES.ordinal()) {
				Integer[] tests = {1, 2, 4, 8};

				if (!Util.isOneOfThese(lineAlignment, tests)) {
					return "Alignment must be 1, 2, 4, or 8 bytes.";
				}
					
				if (order == ComponentOrder.COMPRESSED) {
					return "ComponentOrder.COMPRESSED can only be used with compressed types.";
				}
					
				if (bitDepth == BitDepth.COMPRESSED) {
					return "BitDepth.COMPRESSED can only be used with compressed types.";
				}
			}
			else {
				if (order != ComponentOrder.COMPRESSED) {
					return "Compressed types must use ComponentOrder.COMPRESSED ordering.";
				}

				if (bitDepth != BitDepth.COMPRESSED) {
					return "Compressed types must use BitDepth.COMPRESSED bitdepth.";
				}

				// Test the compressed formats with the colors.
				if (type == PixelDataType.COMPRESSED_BC2 
						|| type == PixelDataType.COMPRESSED_BC3 
						|| type == PixelDataType.COMPRESSED_BC7) 
				{
					if (!Util.isOneOfThese(format, s_alphaFormats)) {
						return "BC2, 3, and 7 compression must use an RGBA format.";
					}
				}
				else if (type == PixelDataType.COMPRESSED_BC1) {
					if (!Util.isOneOfThese(format, s_rgbFormats) &&
						!Util.isOneOfThese(format, s_alphaFormats)) 
					{
						return "BC1 compression must use an RGB or RGBA format";
					}
				}
				
				if (type == PixelDataType.COMPRESSED_UNSIGNED_BC4 
						|| type == PixelDataType.COMPRESSED_SIGNED_BC4) 
				{
					if (format != PixelComponents.COLOR_RED) {
						return "BC4 compression must use the RED format.";
					}
				}
				
				if (type == PixelDataType.COMPRESSED_UNSIGNED_BC5 
						|| type == PixelDataType.COMPRESSED_SIGNED_BC5)
				{
					if (format != PixelComponents.COLOR_RG) {
						return "BC5 compression must use the RG format.";
					}
				}
				
				if (type == PixelDataType.COMPRESSED_UNSIGNED_BC6H 
						|| type == PixelDataType.COMPRESSED_SIGNED_BC6H)
				{
					if (format != PixelComponents.COLOR_RGB) {
						return "BC6H compression must use the RGB format.";
					}
				}

				// End of compressed stuff.
				return "";
			}

			// Check for shared float. It has only one legal arrangement of forms.
			{
				boolean sharedExp 	= (type == PixelDataType.SHARED_EXP_FLOAT);
				boolean sharedOrder	= (order == ComponentOrder.RGBE);
				boolean sharedDepth	= (bitDepth == BitDepth.PACKED_32_BIT_5999_REV);
				
				if (sharedExp != sharedOrder || sharedOrder != sharedDepth) {
					return "Shared floats must use PixelDataType.SHARED_EXP_FLOAT type, " +
							"ComponentOrder.RGBE order, and BitDepth.PACKED_32_BIT_5999_REV bitdepth.";
				}

				if (sharedExp && (format != PixelComponents.COLOR_RGB)) {
					return "Shared floats must use RGB format.";
				}

				// Shared exponent only comes in one form.
				if (sharedExp) {
					return "";
				}
			}

			// Check depth.
			if (format == PixelComponents.DEPTH || format == PixelComponents.DEPTH_X) {
				if (order != ComponentOrder.DEPTH_STENCIL) {
					return "Depth formats must use ComponentOrder.DEPTH_STENCIL ordering.";
				}	
									
				// Depth can be either 16-bit normalized, 32-bit float, or 24x8 normalized.
				if (type == PixelDataType.NORM_UNSIGNED_INTEGER) {
					if (format == PixelComponents.DEPTH) {
						if (bitDepth != BitDepth.PER_COMP_16) {
							return "PixelComponents.DEPTH format with unsigned normalized integers must use BitDepth.PER_COMP_16";
						}
					}
					else {
						if (bitDepth != BitDepth.PACKED_32_BIT_248) {
							return "PixelComponents.DEPTH_X format with unsigned normalized integers must use BitDepth.PACKED_32_BIT_248";
						}
					}
				}
				else if (type == PixelDataType.FLOAT) {
					// Must be 32-bit float.
					if (bitDepth != BitDepth.PER_COMP_32) {
						return "PixelDataType.FLOAT types with depth formats must use BitDepth.PER_COMP_32 bitdepth";
					}
				}
				else {
					// Must be either normalized unsigned or float.
					return "Depth formats must use either unsigned normalized or floating point types.";
				}

				// Short circuit. We've tested all possibilities for depth formats.
				return "";
			}

			// We would not be here if the format contained depth, and this can only use depth.
			if (bitDepth == BitDepth.PACKED_32_BIT_248) {
				return "BitDepth.PACKED_32_BIT_248 can only be used with a depth format.";
			}
				
			// Floats must be at least 16-bits per component, or 101111.
			if (type == PixelDataType.FLOAT) {
				if (!Util.isOneOfThese(bitDepth, s_floatBitdepths)) {
					return "Floating-point types must use 16 or 32-bit bitdepths, or the Bitdepth.PACKED_32_BIT_101111_REV";
				}
			}

			// Only normalized unsigned integers can be sRGB.
			if (Util.isOneOfThese(format, s_srgbFormats)) {
				if (type != PixelDataType.NORM_UNSIGNED_INTEGER) {
					return "Only normalized, unsigned integers can be in the sRGB colorspace.";
				}
			}

			// If 101111, then must be BGR, and must be float.
			if (bitDepth == BitDepth.PACKED_32_BIT_101111_REV) {
				if (! (type == PixelDataType.FLOAT && order == ComponentOrder.RGBA 
						&& format == PixelComponents.COLOR_RGB)) {
					return "The Bitdepth.PACKED_32_BIT_101111_REV bitdepth can only be used with PixelDataType.FLOAT, ComponentOrder.RGBA, and FMT_COLOR_RGB.";
				}
				
				// Short circuit.
				return "";
			}

			// Test all packed bitdepths.
			if (bitDepth.ordinal() >= BitDepth.NUM_PER_COMPONENT.ordinal()) {
				// Only unsigned integers and certain special floats can be packed.
				// The special floats have already been checked, so let them through.
				if (type != PixelDataType.NORM_UNSIGNED_INTEGER && type != PixelDataType.FLOAT) {
					return "Only normalized, unsigned integers and floats can use packed bitdepths.";
				}

				// Ensure that bitdepth and the format match in terms of component count.
				switch (Util.calcComponentCount(format)) {
				case 1:
					// This can never work.
					return "Single-component formats cannot work with packed bitdepths.";
				case 2:
					// Covered previously.
					return "Non-depth two-component formats cannot work with packed bitdepths.";
				case 3:
					if (!Util.isOneOfThese(bitDepth, s_threeCompBitdepths)) {
						return "The only packed formats available to 3-component formats are 565 or 565_REV.";
					}
					break;
				case 4:
					if (Util.isOneOfThese(bitDepth, s_threeCompBitdepths)) {
						return "Cannot use 565 or 565_REV with 4-component formats.";
					}
					break;
				}
			}

			// Normalized types cannot use 32-bit per-component.
			if (bitDepth == BitDepth.PER_COMP_32) {
				if (type == PixelDataType.NORM_UNSIGNED_INTEGER || type == PixelDataType.NORM_SIGNED_INTEGER) {
					return "Normalized integer formats cannot be used with 32-bit per-component data.";
				}
			}

			// BGRA ordering can only use 3 and 4 component types.
			if (order == ComponentOrder.BGRA) {
				if (Util.calcComponentCount(format) < 3) {
					return "BGRA ordering can only use 3 or 4 components.";
				}
			}
			
			return "";
		}
	}
	
	
	ImageFormat(UncheckedImageFormat uncheckedImageFormat) {
		m_uncheckedImageFormat = uncheckedImageFormat;
		
		String message = m_uncheckedImageFormat.validateFormatText();
		if (!message.equals("")) {
			throw new InvalidFormatException(message);
		}
	}
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
			
	final PixelDataType getPixelDataType() {
		return m_uncheckedImageFormat.type;
	}
	
	final PixelComponents getPixelComponents() {
		return m_uncheckedImageFormat.format;
	}
	
	final ComponentOrder getComponentOrder() {
		return m_uncheckedImageFormat.order;
	}
	
	final BitDepth getBitDepth() {
		return m_uncheckedImageFormat.bitDepth;
	};
	
	
	final int alignByteCount(int byteCount) {
		return (byteCount + (m_uncheckedImageFormat.lineAlignment - 1)) / m_uncheckedImageFormat.lineAlignment;
	}
	
	final int getLineAlignment() {
		return m_uncheckedImageFormat.lineAlignment;
	}	

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private static PixelComponents[] s_alphaFormats	= {PixelComponents.COLOR_RGBA, PixelComponents.COLOR_RGBA_SRGB};
	private static PixelComponents[] s_rgbFormats	= {PixelComponents.COLOR_RGB, PixelComponents.COLOR_RGB_SRGB};
	private static PixelComponents[] s_srgbFormats	= {PixelComponents.COLOR_RGB_SRGB, PixelComponents.COLOR_RGBX_SRGB, PixelComponents.COLOR_RGBA_SRGB};

	private static BitDepth[] s_floatBitdepths 		= {BitDepth.PER_COMP_16, BitDepth.PER_COMP_32, BitDepth.PACKED_32_BIT_101111_REV};
	private static BitDepth[] s_threeCompBitdepths	= {BitDepth.PACKED_16_BIT_565, BitDepth.PACKED_16_BIT_565_REV};
	
	
	private UncheckedImageFormat m_uncheckedImageFormat;
	
	
	private static class InvalidFormatException extends RuntimeException {
		private static final long serialVersionUID = -7933858147860721000L;

		InvalidFormatException(String message) {
			super(message);
		}
	}
}
