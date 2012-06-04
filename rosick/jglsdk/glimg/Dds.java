package rosick.jglsdk.glimg;

import static rosick.jglsdk.glimg.ImageFormat.Bitdepth.*;
import static rosick.jglsdk.glimg.ImageFormat.ComponentOrder.*;
import static rosick.jglsdk.glimg.ImageFormat.PixelComponents.*;
import static rosick.jglsdk.glimg.ImageFormat.PixelDataType.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import rosick.jglsdk.glimg.ImageFormat.UncheckedImageFormat;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


@SuppressWarnings("unused")										// for static members ported but not used


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class Dds {
	
	/*
	 * C++
	 * 		unsigned char	: 1 byte
	 * 		unsigned int 	: 4 bytes
	 * 
	 * 
	 * Java
	 * 		byte			: 1 byte
	 * 		char			: 2 bytes
	 * 		int				: 4 bytes
	 */

	
	public static ImageSet loadFromFile(String fileName) {
		byte fileBytes[] = null;

		// Read the file.
		try {
			Path path = Paths.get(ClassLoader.class.getResource(fileName).toURI());
			fileBytes = Files.readAllBytes(path);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		// Check the first 4 bytes.
		int magicTest = intFrom4Bytes(fileBytes, 0);
		if (magicTest != MagicNumbers.DDS_MAGIC_NUMBER) {
			throw new DdsFileMalformedException(fileName, "The Magic number is missing from the file.");
		}
		
		if (fileBytes.length < DdsHeader.SIZE + 4) {
			throw new DdsFileMalformedException(fileName, "The data is way too small to store actual information.");
		}
				
		// Collect info from the DDS file.
		DdsHeader header = getDdsHeader(fileBytes);
		Dds10Header header10 = getDds10Header(header, fileBytes);
		Dimensions dimensions = getDimensions(header);
		UncheckedImageFormat uncheckedFormat = getUncheckedImageFormat(header);

		// Get image counts.
		int mipmapCount = (header.dwFlags & DdsFlags.DDSD_MIPMAPCOUNT) != 0 ? header.dwMipMapCount : 1;
		int faceCount 	= (header10.miscFlag & Dds10MiscFlags.DDS_RESOURCE_MISC_TEXTURECUBE) != 0 ? 6 : 1;
		int arrayCount 	= header10.arraySize > 1 ? header10.arraySize : 1;
			
		// Build the image creator. No more exceptions, except for those thrown by the ImageCreator.
		ImageCreator imageCreator = new ImageCreator(new ImageFormat(uncheckedFormat), dimensions, mipmapCount, arrayCount, faceCount);
		int cumulativeMipmapsOffset = getByteOffsetToData(header);
		
		for (int array = 0; array < arrayCount; array++) {
			for (int face = 0; face < faceCount; face++) {
				for (int mipmap = 0; mipmap < mipmapCount; mipmap++) {
					byte[] mipmapBytes = Arrays.copyOfRange(fileBytes, cumulativeMipmapsOffset, fileBytes.length);
					
					imageCreator.setImageData(mipmapBytes, true, mipmap, array, face);
					cumulativeMipmapsOffset += calcMipmapSize(dimensions, mipmap, uncheckedFormat);
				}
			}
		}
		
		return imageCreator.createImage();
	}
	
	
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static class MagicNumbers {
		static final int DDS_MAGIC_NUMBER 	= 0x20534444;		// "DDS "
		static final int DDS10_FOUR_CC 		= 0x30314458;		// "DX10"

		static final int DDSFOURCC_DXT1 	= 0x31545844; 		// "DXT1"
		static final int DDSFOURCC_DXT3 	= 0x33545844;	 	// "DXT3"
		static final int DDSFOURCC_DXT5 	= 0x35545844; 		// "DXT5"
	};
	
	
	private static class DdsFlags {
		static final int DDSD_CAPS			= 0x00000001;							
		static final int DDSD_HEIGHT 		= 0x00000002;							
		static final int DDSD_WIDTH 		= 0x00000004;							
		static final int DDSD_PITCH 		= 0x00000008;							
		static final int DDSD_PIXELFORMAT 	= 0x00001000;							
		static final int DDSD_MIPMAPCOUNT 	= 0x00020000;							
		static final int DDSD_LINEARSIZE 	= 0x00080000;							
		static final int DDSD_DEPTH 		= 0x00800000;							

	}
	 
	private static class Dds10MiscFlags {
		static final int DDS_RESOURCE_MISC_TEXTURECUBE = 0x00000004;													
	}
	
	private static class Dds10ResourceDimensions {
		static final int DDS_DIMENSION_TEXTURE1D = 2;							
		static final int DDS_DIMENSION_TEXTURE2D = 3;							
		static final int DDS_DIMENSION_TEXTURE3D = 4;							
	}
	
	private static class DdsCaps2 {
		static final int DDSCAPS2_CUBEMAP 			= 0x00000200;							
		static final int DDSCAPS2_CUBEMAP_POSITIVEX = 0x00000400;							
		static final int DDSCAPS2_CUBEMAP_NEGATIVEX = 0x00000800;
		static final int DDSCAPS2_CUBEMAP_POSITIVEY = 0x00001000;							
		static final int DDSCAPS2_CUBEMAP_NEGATIVEY = 0x00002000;							
		static final int DDSCAPS2_CUBEMAP_POSITIVEZ = 0x00004000;							
		static final int DDSCAPS2_CUBEMAP_NEGATIVEZ = 0x00008000;							
		static final int DDSCAPS2_VOLUME 			= 0x00200000;		
		
		static final int DDSCAPS2_CUBEMAP_ALL		= DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_POSITIVEX | 
				DDSCAPS2_CUBEMAP_NEGATIVEX | DDSCAPS2_CUBEMAP_POSITIVEY |
			   	DDSCAPS2_CUBEMAP_NEGATIVEY | DDSCAPS2_CUBEMAP_POSITIVEZ |
			   	DDSCAPS2_CUBEMAP_NEGATIVEZ;
	}
	
	private static class DXGI_FORMAT {
		static final int DXGI_FORMAT_UNKNOWN = 0;
	}
		

	private static class DdsPixelFormat {
		int	dwSize;
		int	dwFlags;
		int	dwFourCC;
		int	dwRGBBitCount;
		int	dwRBitMask;
		int	dwGBitMask;
		int	dwBBitMask;
		int	dwABitMask;
	};
	
	private static class DdsPixelFormatFlags {
		static final int DDPF_ALPHAPIXELS 	= 0x00000001;
		static final int DDPF_ALPHA 		= 0x00000002;
		static final int DDPF_FOURCC 		= 0x00000004;
		static final int DDPF_RGB 			= 0x00000040;
		static final int DDPF_YUV 			= 0x00000200;
		static final int DDPF_LUMINANCE 	= 0x00020000;
    };

    
    private static class DdsHeader {
		int	dwSize;
		int dwFlags;
		int	dwHeight;
		int	dwWidth;
		int	dwPitchOrLinearSize;
		int	dwDepth;
		int	dwMipMapCount;
		int	dwReserved1[] = new int[11];
		DdsPixelFormat ddspf;
		int	dwCaps;
		int	dwCaps2;
		int	dwCaps3;
		int	dwCaps4;
		int	dwReserved2;

		static final int SIZE = (7 + 11 + 8 + 5) * (Integer.SIZE / 8);
	};

	private static class Dds10Header {
		int dxgiFormat;
		int resourceDimension;
		int miscFlag;
		int arraySize;
		int reserved;

		static final int SIZE = 5 * (Integer.SIZE / 8);
	}

		
	private static class DdsFileMalformedException extends RuntimeException {
		private static final long serialVersionUID = 7351687754827086128L;

		DdsFileMalformedException(String filename, String message) {
			super(filename + ": " + message);
		}
	}
	
	private static class DdsFileUnsupportedException extends RuntimeException {
		private static final long serialVersionUID = 377383320427260974L;

		DdsFileUnsupportedException(String filename, String message) {
			super(filename + ": " + message);
		}
	}
		
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static int intFrom4Bytes(byte bytes[], int startIndex) {
		int value = 0;
		
		for (int i = startIndex; i < startIndex + 4; i++) {
		   value += (bytes[i] & 0xff) << (8 * (i - startIndex));
		}
		
		return value;
	}
	
	
	private static DdsHeader getDdsHeader(byte fileBytes[]) {
		DdsHeader header = new DdsHeader();

		int startIndex = 4;
		
		header.dwSize = 				intFrom4Bytes(fileBytes, startIndex);
		header.dwFlags = 				intFrom4Bytes(fileBytes, startIndex +  4);
		header.dwHeight = 				intFrom4Bytes(fileBytes, startIndex +  8);
		header.dwWidth = 				intFrom4Bytes(fileBytes, startIndex + 12);
		header.dwPitchOrLinearSize = 	intFrom4Bytes(fileBytes, startIndex + 16);
		header.dwDepth = 				intFrom4Bytes(fileBytes, startIndex + 20);
		header.dwMipMapCount = 			intFrom4Bytes(fileBytes, startIndex + 24);

		for (int i = 0; i < 11; i++) {
			header.dwReserved1[i] = intFrom4Bytes(fileBytes, (startIndex + 28) + 4 * i);
		}

		header.ddspf = new DdsPixelFormat();
		header.ddspf.dwSize =			intFrom4Bytes(fileBytes, 76);
		header.ddspf.dwFlags =			intFrom4Bytes(fileBytes, 76 +  4);
		header.ddspf.dwFourCC =			intFrom4Bytes(fileBytes, 76 +  8);
		header.ddspf.dwRGBBitCount =	intFrom4Bytes(fileBytes, 76 + 12);
		header.ddspf.dwRBitMask =		intFrom4Bytes(fileBytes, 76 + 16);
		header.ddspf.dwGBitMask =		intFrom4Bytes(fileBytes, 76 + 20);
		header.ddspf.dwBBitMask =		intFrom4Bytes(fileBytes, 76 + 24);
		header.ddspf.dwABitMask =		intFrom4Bytes(fileBytes, 76 + 28);
		
		header.dwCaps =			intFrom4Bytes(fileBytes, startIndex + 104);
		header.dwCaps2 = 		intFrom4Bytes(fileBytes, startIndex + 108);
		header.dwCaps3 = 		intFrom4Bytes(fileBytes, startIndex + 112);
		header.dwCaps4 = 		intFrom4Bytes(fileBytes, startIndex + 116);
		header.dwReserved2 = 	intFrom4Bytes(fileBytes, startIndex + 120);
		
		return header;
	}
	
	private static Dds10Header getDds10Header(DdsHeader header, byte fileBytes[]) {
		if (header.ddspf.dwFourCC == MagicNumbers.DDS10_FOUR_CC) {
			Dds10Header header10 = new Dds10Header();
			int offsetToNewHeader = 4 + DdsHeader.SIZE;

			header10.dxgiFormat = 			intFrom4Bytes(fileBytes, offsetToNewHeader);
			header10.resourceDimension = 	intFrom4Bytes(fileBytes, offsetToNewHeader +  4);
			header10.miscFlag = 			intFrom4Bytes(fileBytes, offsetToNewHeader +  8);
			header10.arraySize = 			intFrom4Bytes(fileBytes, offsetToNewHeader + 12);
			header10.reserved = 			intFrom4Bytes(fileBytes, offsetToNewHeader + 16);
			
			return header10;
		}

		// Compute the header manually. Namely, compute the DXGI_FORMAT for the given data.
		Dds10Header header10 = new Dds10Header();
		
		// Get dimensionality. Assume 2D unless otherwise stated.
		header10.resourceDimension = Dds10ResourceDimensions.DDS_DIMENSION_TEXTURE2D;
		
		if ((header.dwCaps2 & DdsCaps2.DDSCAPS2_VOLUME) != 0 && (header.dwFlags & DdsFlags.DDSD_DEPTH) != 0) {
			header10.resourceDimension = Dds10ResourceDimensions.DDS_DIMENSION_TEXTURE3D;
		}

		// Get cubemap.
		int cubemapTest = header.dwCaps2 & DdsCaps2.DDSCAPS2_CUBEMAP_ALL;
		if (cubemapTest == 0) {
			header10.miscFlag = 0;
		} 
		else {
			// All faces must be specified or none. Otherwise unsupported.
			if (cubemapTest != DdsCaps2.DDSCAPS2_CUBEMAP_ALL) {
				throw new DdsFileUnsupportedException("", "All cubemap faces must be specified.");
			}
			
			header10.miscFlag = Dds10MiscFlags.DDS_RESOURCE_MISC_TEXTURECUBE;
		}

		// Array size is... zero?
		header10.arraySize = 0;

		// Use the old-style format.
		header10.dxgiFormat = DXGI_FORMAT.DXGI_FORMAT_UNKNOWN;

		return header10;
	}
	
	
	private static Dimensions getDimensions(DdsHeader header) {
		Dimensions dimensions = new Dimensions();
		dimensions.numDimensions = 1;
		dimensions.width = header.dwWidth;
		
		if ((header.dwFlags & DdsFlags.DDSD_HEIGHT) != 0) {
			dimensions.numDimensions = 2;
			dimensions.height = header.dwHeight;
		}
		
		if ((header.dwFlags & DdsFlags.DDSD_DEPTH) != 0) {
			dimensions.numDimensions = 3;
			dimensions.depth = header.dwDepth;
		}

		return dimensions;
	}
	
	
	private static UncheckedImageFormat getUncheckedImageFormat(DdsHeader header) {
		for (int convIx = 0; convIx < oldFormatConvert.length; convIx++) {
			if (doesMatchFormat(oldFormatConvert[convIx].ddsFmt, header)) {
				return oldFormatConvert[convIx].uncheckedImageFormat;
			}
		}

		throw new DdsFileUnsupportedException("", "Could not use the DDS9's image format.");
	}
		
	
	private static int getByteOffsetToData(DdsHeader header) {
		int byteOffset = DdsHeader.SIZE + 4;

		if (header.ddspf.dwFourCC == MagicNumbers.DDS10_FOUR_CC) {
			byteOffset += Dds10Header.SIZE;
		}

		return byteOffset;
	}
	
	
	private static int calcMipmapSize(Dimensions dimensions, int currentMipmapLevel, UncheckedImageFormat uncheckedImageFormat) {
		Dimensions mipmapDimensions = Util.modifyDimensionsForMipmap(dimensions, currentMipmapLevel);
		int lineSize = calcLineSize(uncheckedImageFormat, mipmapDimensions.width);

		int effectiveHeight = 1;
		if (mipmapDimensions.numDimensions > 1) {
			effectiveHeight = mipmapDimensions.height;
			
			if (uncheckedImageFormat.eBitdepth == BD_COMPRESSED) {
				effectiveHeight = (effectiveHeight + 3) / 4;
			}
		}

		int effectiveDepth = 1;
		if (mipmapDimensions.numDimensions > 2) {
			effectiveDepth = mipmapDimensions.depth;
			
			if (uncheckedImageFormat.eBitdepth == BD_COMPRESSED) {
				effectiveDepth = (effectiveDepth + 3) / 4;
			}
		}

		int numLines = effectiveHeight * effectiveDepth;
		return lineSize * numLines;
	}

	private static int calcLineSize(UncheckedImageFormat uncheckedImageFormat, int lineWidth) {
		// This is from the DDS suggestions for line size computations.
		if (uncheckedImageFormat.eBitdepth == BD_COMPRESSED) {
			int blockSize = 16;

			if (uncheckedImageFormat.eType == DT_COMPRESSED_BC1 ||
				uncheckedImageFormat.eType == DT_COMPRESSED_UNSIGNED_BC4 || 
				uncheckedImageFormat.eType == DT_COMPRESSED_SIGNED_BC4) 
			{
				blockSize = 8;
			}

			return ((lineWidth + 3) / 4) * blockSize;
		}
		else {
			int bytesPerPixel = Util.getBytesPerPixel(new ImageFormat(uncheckedImageFormat));
			return lineWidth * bytesPerPixel;			
		}
	}
	

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static OldDdsFormatConv oldFormatConvert[] = {
		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGBA, ORDER_RGBA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB | DdsPixelFormatFlags.DDPF_ALPHAPIXELS, 32, 0xff, 0xff00, 0xff0000, 0xff000000, 0)),
				
		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGBA, ORDER_BGRA, BD_PACKED_32_BIT_8888_REV, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB | DdsPixelFormatFlags.DDPF_ALPHAPIXELS, 32, 0xff0000, 0xff00, 0xff, 0xff000000, 0)),
	
		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGBX, ORDER_RGBA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 32, 0xff, 0xff00, 0xff0000, 0, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGB, ORDER_RGBA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 24, 0xff, 0xff00, 0xff0000, 0, 0)),
				
		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGB, ORDER_RGBA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 24, 0xff, 0xff00, 0xff0000, 0, 0)),		
				
		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGB, ORDER_BGRA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 24, 0xff0000, 0xff00, 0xff, 0, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGB, ORDER_RGBA, BD_PACKED_16_BIT_565, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 16, 0xf800, 0x7e0, 0x1f, 0, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGBA, ORDER_BGRA, BD_PACKED_16_BIT_1555_REV, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 16, 0x7c00, 0x3e0, 0x1f, 0x8000, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RGBA, ORDER_BGRA, BD_PACKED_16_BIT_4444_REV, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 16, 0xf00, 0xf0, 0xf, 0xf000, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_COMPRESSED_BC1, FMT_COLOR_RGB, ORDER_COMPRESSED, BD_COMPRESSED, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_FOURCC, 0, 0, 0, 0, 0, MagicNumbers.DDSFOURCC_DXT1)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_COMPRESSED_BC2, FMT_COLOR_RGBA, ORDER_COMPRESSED, BD_COMPRESSED, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_FOURCC, 0, 0, 0, 0, 0, MagicNumbers.DDSFOURCC_DXT3)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_COMPRESSED_BC3, FMT_COLOR_RGBA, ORDER_COMPRESSED, BD_COMPRESSED, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_FOURCC, 0, 0, 0, 0, 0, MagicNumbers.DDSFOURCC_DXT5)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RG, ORDER_RGBA, BD_PER_COMP_16, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 32, 0xffff, 0xffff0000, 0, 0, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RG, ORDER_RGBA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_RGB, 16, 0xffff, 0xffff0000, 0, 0, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RED, ORDER_RGBA, BD_PER_COMP_16, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_LUMINANCE, 16, 0xffff, 0, 0, 0, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RED, ORDER_RGBA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_LUMINANCE, 8, 0xff, 0, 0, 0, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RG, ORDER_RGBA, BD_PER_COMP_16, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_LUMINANCE | DdsPixelFormatFlags.DDPF_ALPHAPIXELS, 16, 0xffff, 0, 0, 0xffff0000, 0)),

		new OldDdsFormatConv(new UncheckedImageFormat(DT_NORM_UNSIGNED_INTEGER, FMT_COLOR_RG, ORDER_RGBA, BD_PER_COMP_8, 1),
				new OldDdsFmtMatch(DdsPixelFormatFlags.DDPF_LUMINANCE | DdsPixelFormatFlags.DDPF_ALPHAPIXELS, 8, 0xff, 0, 0, 0xff00, 0)),		
	};
	
	
	private static class OldDdsFmtMatch {
		int dwFlags;
		int bitDepth;
		int rBitmask;
		int gBitmask;
		int bBitmask;
		int aBitmask;
		int fourCC;
		
		OldDdsFmtMatch(int dwFlags, int bitDepth, int rBitmask, int gBitmask,
				int bBitmask, int aBitmask, int fourCC) {
			this.dwFlags = dwFlags;
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
		
		OldDdsFormatConv(UncheckedImageFormat fmt, OldDdsFmtMatch ddsFmt) {
			this.uncheckedImageFormat = fmt;
			this.ddsFmt = ddsFmt;
		}
	};
	
	
	private static boolean doesMatchFormat(OldDdsFmtMatch ddsFmt, DdsHeader header) {
		if ((header.ddspf.dwFlags & ddsFmt.dwFlags) == 0) {
			return false;
		}

		if ((ddsFmt.dwFlags & DdsPixelFormatFlags.DDPF_FOURCC) != 0) {
			// None of the bit counts matter. Just check the fourCC
			if (ddsFmt.fourCC != header.ddspf.dwFourCC) {
				return false;
			}
		} 
		else {
			// Check the bitcounts, not the fourCC.
			if (header.ddspf.dwRGBBitCount != ddsFmt.bitDepth)
				return false;
			if ((ddsFmt.rBitmask & header.ddspf.dwRBitMask) != ddsFmt.rBitmask)
				return false;
			if ((ddsFmt.gBitmask & header.ddspf.dwGBitMask) != ddsFmt.gBitmask)
				return false;
			if ((ddsFmt.bBitmask & header.ddspf.dwBBitMask) != ddsFmt.bBitmask)
				return false;
			if ((ddsFmt.aBitmask & header.ddspf.dwABitMask) != ddsFmt.aBitmask)
				return false;
		}

		return true;
	}	
}
