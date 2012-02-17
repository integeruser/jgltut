package rosick.jglsdk.glimg;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class ImageFormat {
	
	public enum PixelDataType {
		DT_NORM_UNSIGNED_INTEGER,			///<Image data are unsigned integers that are mapped to floats on the range [0, 1].
		DT_NORM_SIGNED_INTEGER,				///<Image data are signed integers that are mapped to floats on the range [-1, 1].
		DT_UNSIGNED_INTEGRAL,				///<Image data are unsigned integers.
		DT_SIGNED_INTEGRAL,					///<Image data are signed integers.
		DT_FLOAT,							///<Image data are individual floating-point numbers.
		DT_SHARED_EXP_FLOAT,				///<Image data are floats, but each pixel uses the same exponent.
		DT_NUM_UNCOMPRESSED_TYPES,

		DT_COMPRESSED_BC1,					///<Image data is compressed with DXT1/BC1 compression. Unsigned normalized integers.
		DT_COMPRESSED_BC2,					///<Image data is compressed with DXT3/BC2 compression. Unsigned normalized integers.
		DT_COMPRESSED_BC3,					///<Image data is compressed with DXT5/BC3 compression. Unsigned normalized integers.
		DT_COMPRESSED_UNSIGNED_BC4,			///<Image is compressed with BC4 compression (1-component), with unsigned normalized integers.
		DT_COMPRESSED_SIGNED_BC4,			///<Image is compressed with BC4 compression (1-component), with signed normalized integers.
		DT_COMPRESSED_UNSIGNED_BC5,			///<Image is compressed with BC5 compression (2-component), with unsigned normalized integers.
		DT_COMPRESSED_SIGNED_BC5,			///<Image is compressed with BC5 compression (2-component), with signed normalized integers.
		DT_COMPRESSED_UNSIGNED_BC6H,		///<Image is compressed with BC6H compression, with unsigned floats [0, +inf).
		DT_COMPRESSED_SIGNED_BC6H,			///<Image is compressed with BC6H compression, with floats.
		DT_COMPRESSED_BC7,					///<Image data is compressed with BC7 compression. Unsigned normalized integers.

		DT_NUM_TYPES
	};
	
	public enum PixelComponents {
		FMT_COLOR_RED,					///<Image contains 1 color component, namely red.
		FMT_COLOR_RG,					///<Image contains 2 color components, red and green.
		FMT_COLOR_RGB,					///<Image contains 3 color components, red, green, and blue.
		FMT_COLOR_RGBX,					///<Image contains 3 color components, red, green, and blue. There is a fourth component, which takes up space in the data but should be discarded.
		FMT_COLOR_RGBA,					///<Image contains 4 color components, red, green, blue, and alpha.
		FMT_COLOR_RGB_sRGB,				///<Image contains 3 color components, which are in the sRGB colorspace.
		FMT_COLOR_RGBX_sRGB,			///<Image contains 3 color components, which are in the sRGB colorspace. There is a fourth component, which takes up space in the data but should be discarded.
		FMT_COLOR_RGBA_sRGB,			///<Image contains 4 color components; the RGB components are in the sRGB colorspace.

		FMT_DEPTH,						///<Image contains a single depth component.
		FMT_DEPTH_X,					///<Image contains a depth value (unsigned normalized integer) and a second component, who's value is discarded/irrelevant.

		FMT_NUM_FORMATS
	};
	
	public enum ComponentOrder {
		ORDER_RGBA,				///<Standard RGBA ordering.
		ORDER_BGRA,				///<Often used in conjunction with _REV Bitdepths.
		ORDER_RGBE,				///<For DT_SHARED_EXP_FLOAT types. The E is the exponent, and it comes first.

		ORDER_DEPTH_STENCIL,	///<Ordering for depth and depth-stencil image formats.

		ORDER_COMPRESSED,		///<The order is built into the compressed data format.

		ORDER_NUM_ORDERS
	};
	
	public enum Bitdepth {
		BD_COMPRESSED,					///<Used for compressed data types. They do not have a bitdepth.

		BD_PER_COMP_8,					///<Each component takes up 8 bits.
		BD_PER_COMP_16,					///<Each component takes up 16 bits.
		BD_PER_COMP_32,					///<Each component takes up 32 bits.
		BD_NUM_PER_COMPONENT,			

		BD_PACKED_16_BIT_565,			///<The first and third components take up 5 bits, while the second takes up 6.
		BD_PACKED_16_BIT_5551,			///<The first three components take up 5 bits, and the last takes up 1.
		BD_PACKED_16_BIT_4444,			///<Each component takes up 4 bits.

		BD_PACKED_32_BIT_8888,			///<Each component takes up 8 bits.
		BD_PACKED_32_BIT_1010102,		///<The first three components take up 10 bits, and the last takes up 2.
		BD_PACKED_32_BIT_248,			///<The first component takes up 24 bits; the second takes up 8 bits.

		BD_PACKED_16_BIT_565_REV,		///<Reverse order. The first and third components take up 5 bits, while the second takes up 6.
		BD_PACKED_16_BIT_1555_REV,		///<Reverse order. The first three components take up 5 bits, and the last takes up 1.
		BD_PACKED_16_BIT_4444_REV,		///<Reverse order. Each component takes up 4 bits.

		BD_PACKED_32_BIT_8888_REV,		///<Reverse order. Each component takes up 8 bits.
		BD_PACKED_32_BIT_2101010_REV,	///<Reverse order. The first three components take up 10 bits, and the last takes up 2.
		BD_PACKED_32_BIT_101111_REV,	///<Reverse order. The first two components take 11 bits, and the third takes 10. Used for DT_FLOAT types.
		BD_PACKED_32_BIT_5999_REV,		///<Reverse order. The first 3 components take 9 bits, and the last takes 5. Used for DT_SHARED_EXP_FLOAT types. 

		BD_NUM_BITDEPTH
	};
	
	
	public static class UncheckedImageFormat {
		public PixelDataType eType;			//<The type of pixel data.
		public PixelComponents eFormat;		//<The components stored by a pixel.
		public ComponentOrder eOrder;		//<The order of the components of the pixel.
		public Bitdepth eBitdepth;			//<The bitdepth of each pixel component.
		public int lineAlignment;			//<The byte alignment of a horizontal line of pixel data.
		
		
		public UncheckedImageFormat() {
		}
		
		public UncheckedImageFormat(PixelDataType eType, PixelComponents eFormat,
				ComponentOrder eOrder, Bitdepth eBitdepth, int lineAlignment) {
			this.eType = eType;
			this.eFormat = eFormat;
			this.eOrder = eOrder;
			this.eBitdepth = eBitdepth;
			this.lineAlignment = lineAlignment;
		}
	}
	
	
	private UncheckedImageFormat fmt;

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	public ImageFormat(UncheckedImageFormat fmt) {
		this.fmt = fmt;
	}
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public int alignByteCount(int byteCount) {
		return (byteCount + (fmt.lineAlignment - 1)) / fmt.lineAlignment;
	}
	
		
	public PixelDataType type() {
		return fmt.eType;
	}
	
	public PixelComponents components() {
		return fmt.eFormat;
	}
	
	public Bitdepth depth() {
		return fmt.eBitdepth;
	};
}
