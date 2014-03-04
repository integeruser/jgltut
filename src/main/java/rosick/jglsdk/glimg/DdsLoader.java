package rosick.jglsdk.glimg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import rosick.jglsdk.glimg.ImageFormat.BitDepth;
import rosick.jglsdk.glimg.ImageFormat.ComponentOrder;
import rosick.jglsdk.glimg.ImageFormat.PixelComponents;
import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageFormat.UncheckedImageFormat;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/integeruser/jglsdk for project info, updates and license terms.
 * 
 * C++
 * 		unsigned char	: 1 byte
 * 		unsigned int 	: 4 bytes
 * 
 * Java
 * 		byte			: 1 byte
 * 		char			: 2 bytes
 * 		int				: 4 bytes
 * 
 * @author integeruser
 */
public class DdsLoader {
	
	public static ImageSet loadFromFile(String ddsFilepath) throws IOException {		
		InputStream ddsInputStream = ClassLoader.class.getResourceAsStream(ddsFilepath);
		byte[] ddsFile = readDdsFile(ddsInputStream);
		
		// Check the first 4 bytes.
		int magicTest = readDoubleWord(ddsFile, 0);
		if (magicTest != MagicNumbers.DDS_MAGIC_NUMBER) {
			throw new DdsFileMalformedException(ddsFilepath, "The Magic number is missing from the file.");
		}
		
		if (ddsFile.length < DdsHeader.SIZE + 4) {
			throw new DdsFileMalformedException(ddsFilepath, "The data is way too small to store actual information.");
		}
				
		// Collect info from the DDS file.
		DdsHeader ddsHeader = new DdsHeader(ddsFile);
		Dds10Header dds10Header = getDds10Header(ddsFile, ddsHeader);
		Dimensions ddsDimensions = getDimensions(ddsHeader);
		UncheckedImageFormat ddsFormat = getFormat(ddsHeader, dds10Header);

		// Get image counts.
		int numArrays 	= (dds10Header.arraySize > 1) ? dds10Header.arraySize : 1;
		int numFaces	= (dds10Header.miscFlag & Dds10MiscFlags.RESOURCE_MISC_TEXTURECUBE) != 0 ? 6 : 1;
		int numMipmaps 	= (ddsHeader.flags & DdsFlags.MIPMAPCOUNT) != 0 ? ddsHeader.mipmapCount : 1;

		int baseOffset = getOffsetToData(ddsHeader);

		// Build the image creator.
		ImageCreator imageCreator = new ImageCreator(new ImageFormat(ddsFormat), ddsDimensions, numMipmaps, numArrays, numFaces);		
		int cumulativeOffset = baseOffset;
		
		for (int arrayIx = 0; arrayIx < numArrays; arrayIx++) {
			for (int faceIx = 0; faceIx < numFaces; faceIx++) {			
				for (int mipmapLevel = 0; mipmapLevel < numMipmaps; mipmapLevel++) {
					int mipmapLevelSize = calcMipmapSize(ddsFormat, ddsDimensions, mipmapLevel);

					// Get specific data from ddsFile.
					byte[] mipmapLevelData = Arrays.copyOfRange(ddsFile, cumulativeOffset, cumulativeOffset + mipmapLevelSize);
					
					// Set data for the current mipmap level in imageCreator.
					imageCreator.setImageData(mipmapLevelData, true, mipmapLevel, arrayIx, faceIx);
					
					// Advance offset to read next mipmapLevelData from ddsFile.
					cumulativeOffset += mipmapLevelSize;
				}
			}
		}
		
		return imageCreator.createImage();
	}
	
	
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static class DdsFileMalformedException extends RuntimeException {
		private static final long serialVersionUID = -1523221969465221880L;

		private DdsFileMalformedException(String filename, String message) {
			super(filename + ": " + message);
		}
	}
	
	private static class DdsFileUnsupportedException extends RuntimeException {
		private static final long serialVersionUID = 377383320427260974L;

		private DdsFileUnsupportedException(String filename, String message) {
			super(filename + ": " + message);
		}		
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static class MagicNumbers {
		static final int DDS_MAGIC_NUMBER 	= 0x20534444;		// "DDS "
		static final int DDS10_FOUR_CC 		= 0x30314458;		// "DX10"

		static final int DDSFOURCC_DXT1 	= 0x31545844; 		// "DXT1"
		static final int DDSFOURCC_DXT3 	= 0x33545844;	 	// "DXT3"
		static final int DDSFOURCC_DXT5 	= 0x35545844; 		// "DXT5"
	};
	

	@SuppressWarnings("unused")
	private static class DdsFlags {
		static final int CAPS			= 0x00000001;							
		static final int HEIGHT 		= 0x00000002;							
		static final int WIDTH 			= 0x00000004;							
		static final int PITCH 			= 0x00000008;							
		static final int PIXELFORMAT 	= 0x00001000;							
		static final int MIPMAPCOUNT 	= 0x00020000;							
		static final int LINEARSIZE 	= 0x00080000;							
		static final int DEPTH 			= 0x00800000;							

	}
	 
	private static class Dds10MiscFlags {
		static final int RESOURCE_MISC_TEXTURECUBE = 0x00000004;													
	}
	
	@SuppressWarnings("unused")
	private static class Dds10ResourceDimensions {
		static final int TEXTURE1D = 2;							
		static final int TEXTURE2D = 3;							
		static final int TEXTURE3D = 4;							
	}
	
	private static class DdsCaps2 {
		static final int CUBEMAP 			= 0x00000200;							
		static final int CUBEMAP_POSITIVEX 	= 0x00000400;							
		static final int CUBEMAP_NEGATIVEX 	= 0x00000800;
		static final int CUBEMAP_POSITIVEY 	= 0x00001000;							
		static final int CUBEMAP_NEGATIVEY	= 0x00002000;							
		static final int CUBEMAP_POSITIVEZ	= 0x00004000;							
		static final int CUBEMAP_NEGATIVEZ 	= 0x00008000;							
		static final int VOLUME 			= 0x00200000;		
		
		static final int CUBEMAP_ALL		= CUBEMAP | 
				CUBEMAP_POSITIVEX | CUBEMAP_NEGATIVEX | 
				CUBEMAP_POSITIVEY | CUBEMAP_NEGATIVEY | 
				CUBEMAP_POSITIVEZ | CUBEMAP_NEGATIVEZ;
	}
	
	private static class DxgiFormat {
		static final int UNKNOWN = 0;
	}
		

	@SuppressWarnings("unused")
	private static class DdsPixelFormat {
		int	size;
		int	flags;
		int	fourCC;
		int	rgbBitCount;
		int	rBitMask;
		int	gBitMask;
		int	bBitMask;
		int	aBitMask;
	};
	
	@SuppressWarnings("unused")
	private static class DdsPixelFormatFlags {
		static final int ALPHAPIXELS	= 0x00000001;
		static final int ALPHA 			= 0x00000002;
		static final int FOURCC 		= 0x00000004;
		static final int RGB 			= 0x00000040;
		static final int YUV 			= 0x00000200;
		static final int LUMINANCE 		= 0x00020000;
    };

    
	@SuppressWarnings("unused")
    private static class DdsHeader {
		static final int SIZE = (7 + 11 + 8 + 5) * (Integer.SIZE / 8);

		int	size;
		int flags;
		int	height;
		int	width;
		int	pitchOrLinearSize;
		int	depth;
		int	mipmapCount;
		
		int	reserved1[] = new int[11];
		
		DdsPixelFormat ddsPixelFormat;
		
		int	caps;
		int	caps2;
		int	caps3;
		int	caps4;
		int	reserved2;
		
		DdsHeader(byte[] ddsFile) {		
			size = 				readDoubleWord(ddsFile, 4);
			flags = 			readDoubleWord(ddsFile, 8);
			height = 			readDoubleWord(ddsFile, 12);
			width = 			readDoubleWord(ddsFile, 16);
			pitchOrLinearSize =	readDoubleWord(ddsFile, 20);
			depth = 			readDoubleWord(ddsFile, 24);
			mipmapCount = 		readDoubleWord(ddsFile, 28);

			for (int i = 0; i < reserved1.length; i++) {
				reserved1[i] = readDoubleWord(ddsFile, 32 + 4 * i);
			}

			ddsPixelFormat = new DdsPixelFormat();
			ddsPixelFormat.size =			readDoubleWord(ddsFile, 76);
			ddsPixelFormat.flags =			readDoubleWord(ddsFile, 80);
			ddsPixelFormat.fourCC =			readDoubleWord(ddsFile, 84);
			ddsPixelFormat.rgbBitCount =	readDoubleWord(ddsFile, 88);
			ddsPixelFormat.rBitMask =		readDoubleWord(ddsFile, 92);
			ddsPixelFormat.gBitMask =		readDoubleWord(ddsFile, 96);
			ddsPixelFormat.bBitMask =		readDoubleWord(ddsFile, 100);
			ddsPixelFormat.aBitMask =		readDoubleWord(ddsFile, 104);
			
			caps =		readDoubleWord(ddsFile, 108);
			caps2 = 	readDoubleWord(ddsFile, 112);
			caps3 = 	readDoubleWord(ddsFile, 116);
			caps4 = 	readDoubleWord(ddsFile, 120);
			reserved2 =	readDoubleWord(ddsFile, 124);
		}
	};

	@SuppressWarnings("unused")
	private static class Dds10Header {
		static final int SIZE = 5 * (Integer.SIZE / 8);

		int dxgiFormat;
		int resourceDimension;
		int miscFlag;
		int arraySize;
		int reserved;
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static byte[] readDdsFile(InputStream ddsInStream) throws IOException {
		assert ddsInStream != null;
		
		ByteArrayOutputStream ddsOutStream = new ByteArrayOutputStream();
		int bytesRead;
		
		byte[] buffer = new byte[4096]; 
		
		while ((bytesRead = ddsInStream.read(buffer)) != -1) {
			ddsOutStream.write(buffer, 0, bytesRead);
		}				
								
		return ddsOutStream.toByteArray();
	}
	
	
	// Read four bytes.
	private static int readDoubleWord(byte bytes[], int startIx) {
		int dw = 0;
		
		// Read 4 bytes.
		for (int i = startIx; i < startIx + 4; i++) {
		   dw += (bytes[i] & 0xff) << (8 * (i - startIx));
		}
		
		return dw;
	}
	
	
	// Will either generate this or return the actual one.
	private static Dds10Header getDds10Header(byte[] ddsFile, DdsHeader ddsHeader) {
		if (ddsHeader.ddsPixelFormat.fourCC == MagicNumbers.DDS10_FOUR_CC) {
			Dds10Header dds10Header = new Dds10Header();
			int offsetToNewHeader = DdsHeader.SIZE + 4;

			dds10Header.dxgiFormat = 		readDoubleWord(ddsFile, offsetToNewHeader);
			dds10Header.resourceDimension = readDoubleWord(ddsFile, offsetToNewHeader +  4);
			dds10Header.miscFlag = 			readDoubleWord(ddsFile, offsetToNewHeader +  8);
			dds10Header.arraySize = 		readDoubleWord(ddsFile, offsetToNewHeader + 12);
			dds10Header.reserved = 			readDoubleWord(ddsFile, offsetToNewHeader + 16);
			
			return dds10Header;
		}

		// Compute the header manually. Namely, compute the DXGI_FORMAT for the given data.
		Dds10Header dds10header = new Dds10Header();
		
		// Get dimensionality. Assume 2D unless otherwise stated.
		dds10header.resourceDimension = Dds10ResourceDimensions.TEXTURE2D;
		
		if ((ddsHeader.caps2 & DdsCaps2.VOLUME) != 0 && (ddsHeader.flags & DdsFlags.DEPTH) != 0) {
			dds10header.resourceDimension = Dds10ResourceDimensions.TEXTURE3D;
		}

		// Get cubemap.
		int cubemapTest = ddsHeader.caps2 & DdsCaps2.CUBEMAP_ALL;
		if (cubemapTest == 0) {
			dds10header.miscFlag = 0;
		} else {
			// All faces must be specified or none. Otherwise unsupported.
			if (cubemapTest != DdsCaps2.CUBEMAP_ALL) {
				throw new DdsFileUnsupportedException("", "All cubemap faces must be specified.");
			}
			
			dds10header.miscFlag = Dds10MiscFlags.RESOURCE_MISC_TEXTURECUBE;
		}

		// Array size is... zero?
		dds10header.arraySize = 0;

		// Use the old-style format.
		dds10header.dxgiFormat = DxgiFormat.UNKNOWN;

		return dds10header;
	}
	
	
	private static Dimensions getDimensions(DdsHeader ddsHeader) {
		Dimensions ddsDimensions = new Dimensions();
		ddsDimensions.numDimensions = 1;
		ddsDimensions.width = ddsHeader.width;
		
		if ((ddsHeader.flags & DdsFlags.HEIGHT) != 0) {
			ddsDimensions.numDimensions = 2;
			ddsDimensions.height = ddsHeader.height;
		}
		
		if ((ddsHeader.flags & DdsFlags.DEPTH) != 0) {
			ddsDimensions.numDimensions = 3;
			ddsDimensions.depth = ddsHeader.depth;
		}

		return ddsDimensions;
	}
	
	private static UncheckedImageFormat getFormat(DdsHeader ddsHeader, Dds10Header dds10Header) throws DdsFileUnsupportedException {
		if (dds10Header.dxgiFormat != DxgiFormat.UNKNOWN) {
			Util.throwNotYetPortedException();
		}
		
		for (int convIx = 0; convIx < s_oldFormatConvert.length; convIx++) {
			if (doesMatchFormat(s_oldFormatConvert[convIx].ddsFmt, ddsHeader)) {
				return s_oldFormatConvert[convIx].uncheckedImageFormat;
			}
		}

		throw new DdsFileUnsupportedException("", "Could not use the DDS9's image format.");
	}
	
	private static int getOffsetToData(DdsHeader ddsHeader) {
		int byteOffset = DdsHeader.SIZE + 4;

		if (ddsHeader.ddsPixelFormat.fourCC == MagicNumbers.DDS10_FOUR_CC) {
			byteOffset += Dds10Header.SIZE;
		}

		return byteOffset;
	}
	
	
	// Computes the bytesize of a single scanline of an image of the given format, with the given line width.
	// For compressed textures, the value returned is the number of bytes for every 4 scanlines.
	private static int calcLineSize(UncheckedImageFormat ddsFormat, int lineWidth) {
		// This is from the DDS suggestions for line size computations.
		if (ddsFormat.bitDepth == BitDepth.COMPRESSED) {
			int blockSize = 16;

			if (ddsFormat.type == PixelDataType.COMPRESSED_BC1
					|| ddsFormat.type == PixelDataType.COMPRESSED_UNSIGNED_BC4
					|| ddsFormat.type == PixelDataType.COMPRESSED_SIGNED_BC4) {
				blockSize = 8;
			}

			return ((lineWidth + 3) / 4) * blockSize;
		} else {
			int bytesPerPixel = Util.calcBytesPerPixel(new ImageFormat(ddsFormat));

			return lineWidth * bytesPerPixel;
		}
	}
	
	private static int calcMipmapSize(UncheckedImageFormat ddsFormat, Dimensions ddsDimensions, int mipmapLevel) {
		Dimensions mipmapDimensions = Util.calcMipmapLevelDimensions(ddsDimensions, mipmapLevel);
		int lineSize = calcLineSize(ddsFormat, mipmapDimensions.width);

		int effectiveHeight = 1;
		if (mipmapDimensions.numDimensions > 1) {
			effectiveHeight = mipmapDimensions.height;
			
			if (ddsFormat.bitDepth == BitDepth.COMPRESSED) {
				effectiveHeight = (effectiveHeight + 3) / 4;
			}
		}

		int effectiveDepth = 1;
		if (mipmapDimensions.numDimensions > 2) {
			effectiveDepth = mipmapDimensions.depth;
			
			if (ddsFormat.bitDepth == BitDepth.COMPRESSED) {
				effectiveDepth = (effectiveDepth + 3) / 4;
			}
		}

		int numLines = effectiveHeight * effectiveDepth;
		
		return lineSize * numLines;
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static OldDdsFormatConv s_oldFormatConvert[] = {
			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGBA, ComponentOrder.RGBA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB | DdsPixelFormatFlags.ALPHAPIXELS,
					32, 0xff, 0xff00, 0xff0000, 0xff000000, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGBA, ComponentOrder.BGRA,
					BitDepth.PACKED_32_BIT_8888_REV, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB | DdsPixelFormatFlags.ALPHAPIXELS,
					32, 0xff0000, 0xff00, 0xff, 0xff000000, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGBX, ComponentOrder.RGBA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 32, 0xff, 0xff00, 0xff0000, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGB, ComponentOrder.RGBA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 24, 0xff, 0xff00, 0xff0000, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGB, ComponentOrder.RGBA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 24, 0xff, 0xff00, 0xff0000, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGB, ComponentOrder.BGRA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 24, 0xff0000, 0xff00, 0xff, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGB, ComponentOrder.RGBA,
					BitDepth.PACKED_16_BIT_565, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 16, 0xf800, 0x7e0, 0x1f, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGBA, ComponentOrder.BGRA,
					BitDepth.PACKED_16_BIT_1555_REV, 1),
					new OldDdsFmtMatch(DdsPixelFormatFlags.RGB, 16, 0x7c00,
							0x3e0, 0x1f, 0x8000, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RGBA, ComponentOrder.BGRA,
					BitDepth.PACKED_16_BIT_4444_REV, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 16, 0xf00, 0xf0, 0xf, 0xf000, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.COMPRESSED_BC1, PixelComponents.COLOR_RGB,
					ComponentOrder.COMPRESSED, BitDepth.COMPRESSED, 1),
					new OldDdsFmtMatch(DdsPixelFormatFlags.FOURCC, 0, 0, 0, 0,
							0, MagicNumbers.DDSFOURCC_DXT1)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.COMPRESSED_BC2, PixelComponents.COLOR_RGBA,
					ComponentOrder.COMPRESSED, BitDepth.COMPRESSED, 1),
					new OldDdsFmtMatch(DdsPixelFormatFlags.FOURCC, 0, 0, 0, 0,
							0, MagicNumbers.DDSFOURCC_DXT3)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.COMPRESSED_BC3, PixelComponents.COLOR_RGBA,
					ComponentOrder.COMPRESSED, BitDepth.COMPRESSED, 1),
					new OldDdsFmtMatch(DdsPixelFormatFlags.FOURCC, 0, 0, 0, 0,
							0, MagicNumbers.DDSFOURCC_DXT5)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RG, ComponentOrder.RGBA,
					BitDepth.PER_COMP_16, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 32, 0xffff, 0xffff0000, 0, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RG, ComponentOrder.RGBA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.RGB, 16, 0xffff, 0xffff0000, 0, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RED, ComponentOrder.RGBA,
					BitDepth.PER_COMP_16, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.LUMINANCE, 16, 0xffff, 0, 0, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RED, ComponentOrder.RGBA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.LUMINANCE, 8, 0xff, 0, 0, 0, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RG, ComponentOrder.RGBA,
					BitDepth.PER_COMP_16, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.LUMINANCE
							| DdsPixelFormatFlags.ALPHAPIXELS, 16, 0xffff, 0,
					0, 0xffff0000, 0)),

			new OldDdsFormatConv(new UncheckedImageFormat(
					PixelDataType.NORM_UNSIGNED_INTEGER,
					PixelComponents.COLOR_RG, ComponentOrder.RGBA,
					BitDepth.PER_COMP_8, 1), new OldDdsFmtMatch(
					DdsPixelFormatFlags.LUMINANCE
							| DdsPixelFormatFlags.ALPHAPIXELS, 8, 0xff, 0, 0,
					0xff00, 0)), };
	
	
	private static class OldDdsFmtMatch {
		int flags;
		int bitDepth;
		int rBitmask;
		int gBitmask;
		int bBitmask;
		int aBitmask;
		int fourCC;
		
		OldDdsFmtMatch(int flags, int bitDepth, int rBitmask, int gBitmask,
				int bBitmask, int aBitmask, int fourCC) {
			this.flags = flags;
			this.bitDepth = bitDepth;
			this.rBitmask = rBitmask;
			this.gBitmask = gBitmask;
			this.bBitmask = bBitmask;
			this.aBitmask = aBitmask;
			this.fourCC = fourCC;
		}
	};

	private static class OldDdsFormatConv {
		UncheckedImageFormat uncheckedImageFormat;
		OldDdsFmtMatch ddsFmt;
		
		OldDdsFormatConv(UncheckedImageFormat uncheckedImageFormat, OldDdsFmtMatch ddsFmt) {
			this.uncheckedImageFormat = uncheckedImageFormat;
			this.ddsFmt = ddsFmt;
		}
	};
	
	
	private static boolean doesMatchFormat(OldDdsFmtMatch ddsFmt, DdsHeader ddsHeader) {
		if ((ddsHeader.ddsPixelFormat.flags & ddsFmt.flags) == 0) {
			return false;
		}

		if ((ddsFmt.flags & DdsPixelFormatFlags.FOURCC) != 0) {
			// None of the bit counts matter. Just check the fourCC
			if (ddsFmt.fourCC != ddsHeader.ddsPixelFormat.fourCC) {
				return false;
			}
		} else {
			// Check the bitcounts, not the fourCC.
			if (ddsHeader.ddsPixelFormat.rgbBitCount != ddsFmt.bitDepth) {
				return false;
			}

			if ((ddsFmt.rBitmask & ddsHeader.ddsPixelFormat.rBitMask) != ddsFmt.rBitmask) {
				return false;
			}

			if ((ddsFmt.gBitmask & ddsHeader.ddsPixelFormat.gBitMask) != ddsFmt.gBitmask) {
				return false;
			}

			if ((ddsFmt.bBitmask & ddsHeader.ddsPixelFormat.bBitMask) != ddsFmt.bBitmask) {
				return false;
			}

			if ((ddsFmt.aBitmask & ddsHeader.ddsPixelFormat.aBitMask) != ddsFmt.aBitmask) {
				return false;
			}
		}

		return true;
	}	
}