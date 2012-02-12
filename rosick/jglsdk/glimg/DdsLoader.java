package rosick.jglsdk.glimg;

import static rosick.PortingUtils.*;
import static rosick.jglsdk.glimg.ImageFormat.Bitdepth.*;
import static rosick.jglsdk.glimg.ImageFormat.ComponentOrder.*;
import static rosick.jglsdk.glimg.ImageFormat.PixelComponents.*;
import static rosick.jglsdk.glimg.ImageFormat.PixelDataType.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import rosick.jglsdk.glimg.ImageCreator.BadFaceCountException;
import rosick.jglsdk.glimg.ImageCreator.CubemapsMustBe2DException;
import rosick.jglsdk.glimg.ImageCreator.No3DTextureArrayException;
import rosick.jglsdk.glimg.ImageCreator.NoImagesSpecifiedException;
import rosick.jglsdk.glimg.ImageFormat.UncheckedImageFormat;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class DdsLoader {
	
	static class MagicNumbers {
		static final int DDS_MAGIC_NUMBER 	= 0x20534444;							// "DDS "
		static final int DDS10_FOUR_CC 		= 0x30314458;							// "DX10"

		static final int DDSFOURCC_DXT1 	= 0x31545844; 							// "DXT1"
		static final int DDSFOURCC_DXT3 	= 0x33545844;	 						// "DXT3"
		static final int DDSFOURCC_DXT5 	= 0x35545844; 							// "DXT5"
	};
	
	
	static class DdsFlags {
		static final int DDSD_CAPS			= 0x00000001;							
		static final int DDSD_HEIGHT 		= 0x00000002;							
		static final int DDSD_WIDTH 		= 0x00000004;							
		static final int DDSD_PITCH 		= 0x00000008;							
		static final int DDSD_PIXELFORMAT 	= 0x00001000;							
		static final int DDSD_MIPMAPCOUNT 	= 0x00020000;							
		static final int DDSD_LINEARSIZE 	= 0x00080000;							
		static final int DDSD_DEPTH 		= 0x00800000;							

	}
	    
	static class Dds10ResourceDimensions {
		static final int DDS_DIMENSION_TEXTURE1D = 2;							
		static final int DDS_DIMENSION_TEXTURE2D = 3;							
		static final int DDS_DIMENSION_TEXTURE3D = 4;							
	}
	
	static class Dds10MiscFlags {
		static final int DDS_RESOURCE_MISC_TEXTURECUBE = 0x00000004;													
	}
	
	static class DdsCaps2 {
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
	
	static class DXGI_FORMAT {
		static final int DXGI_FORMAT_UNKNOWN = 0;
	}
		


	static class DdsPixelFormat {
		int	dwSize;
		int	dwFlags;
		int	dwFourCC;
		int	dwRGBBitCount;
		int	dwRBitMask;
		int	dwGBitMask;
		int	dwBBitMask;
		int	dwABitMask;

		DdsPixelFormat(ArrayList<Character> ddsData, int startIndex) {
			dwSize =		(int) toLong(toByteArray(ddsData, startIndex, 		4));
			dwFlags =		(int) toLong(toByteArray(ddsData, startIndex + 4, 	4));
			dwFourCC =		(int) toLong(toByteArray(ddsData, startIndex + 8, 	4));
			dwRGBBitCount =	(int) toLong(toByteArray(ddsData, startIndex + 12, 	4));
			dwRBitMask =	(int) toLong(toByteArray(ddsData, startIndex + 16, 	4));
			dwGBitMask =	(int) toLong(toByteArray(ddsData, startIndex + 20, 	4));
			dwBBitMask =	(int) toLong(toByteArray(ddsData, startIndex + 24, 	4));
			dwABitMask =	(int) toLong(toByteArray(ddsData, startIndex + 28, 	4));
		}
	};
	
	static class DdsPixelFormatFlags {
		static final int DDPF_ALPHAPIXELS 	= 0x00000001;
		static final int DDPF_ALPHA 		= 0x00000002;
		static final int DDPF_FOURCC 		= 0x00000004;
		static final int DDPF_RGB 			= 0x00000040;
		static final int DDPF_YUV 			= 0x00000200;
		static final int DDPF_LUMINANCE 	= 0x00020000;
    };

	static class DdsHeader {
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

		static final int SIZE = 31 * (Integer.SIZE / 8);


		DdsHeader(ArrayList<Character> ddsData, int startIndex) {	
			dwSize = 				(int) toLong(toByteArray(ddsData, startIndex, 		4));
			dwFlags = 				(int) toLong(toByteArray(ddsData, startIndex + 4, 	4));
			dwHeight = 				(int) toLong(toByteArray(ddsData, startIndex + 8, 	4));
			dwWidth = 				(int) toLong(toByteArray(ddsData, startIndex + 12, 	4));
			dwPitchOrLinearSize = 	(int) toLong(toByteArray(ddsData, startIndex + 16, 	4));
			dwDepth = 				(int) toLong(toByteArray(ddsData, startIndex + 20, 	4));
			dwMipMapCount = 		(int) toLong(toByteArray(ddsData, startIndex + 24, 	4));

			for (int i = 0; i < 11; i++) {
				dwReserved1[i] = 	(int) toLong(toByteArray(ddsData, (startIndex + 28) + 4 * i, 4));
			}

			ddspf = 				new DdsPixelFormat(ddsData, 76);
			dwCaps = 				(int) toLong(toByteArray(ddsData, startIndex + 104, 4));
			dwCaps2 = 				(int) toLong(toByteArray(ddsData, startIndex + 108, 4));
			dwCaps3 = 				(int) toLong(toByteArray(ddsData, startIndex + 112, 4));
			dwCaps4 = 				(int) toLong(toByteArray(ddsData, startIndex + 116, 4));
			dwReserved2 = 			(int) toLong(toByteArray(ddsData, startIndex + 120, 4));
		}
	};


	static class Dds10Header {
		int dxgiFormat;
		int resourceDimension;
		int miscFlag;
		int arraySize;
		int reserved;

		static final int SIZE = 5 * (Integer.SIZE / 8);

		
		Dds10Header() {}

		Dds10Header(ArrayList<Character> ddsData, int startIndex) {
			dxgiFormat = 		(int) toLong(toByteArray(ddsData, startIndex, 		4));
			resourceDimension = (int) toLong(toByteArray(ddsData, startIndex + 4, 	4));
			miscFlag = 			(int) toLong(toByteArray(ddsData, startIndex + 8, 	4));
			arraySize = 		(int) toLong(toByteArray(ddsData, startIndex + 12, 	4));
			reserved = 			(int) toLong(toByteArray(ddsData, startIndex + 16, 	4));
		}
	}

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	static class DdsFileNotFoundException extends Exception {
		private static final long serialVersionUID = 7749946923854530980L;

		public DdsFileNotFoundException(String filename) {
			super(filename);
		}
	}
	
	static class DdsFileMalformedException extends Exception {
		private static final long serialVersionUID = 7351687754827086128L;

		public DdsFileMalformedException(String filename, String message) {
			super(filename + ": " + message);
		}
	}
	
	static class DdsFileUnsupportedException extends Exception {
		private static final long serialVersionUID = 377383320427260974L;

		public DdsFileUnsupportedException(String filename, String message) {
			super(filename + ": " + message);
		}
	}
		
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
	
	public static ImageSet processDDSData(ArrayList<Character> ddsData, String filename) throws DdsFileMalformedException, UnsupportedEncodingException, DdsFileUnsupportedException, CubemapsMustBe2DException, BadFaceCountException, No3DTextureArrayException, NoImagesSpecifiedException {
		// Check the first 4 bytes.
		int magicTest = (int) toLong(toByteArray(ddsData, 0, 4));

		if (magicTest != MagicNumbers.DDS_MAGIC_NUMBER) {
			throw new DdsFileMalformedException(filename, "The Magic number is missing from the file.");
		}
		
		if (ddsData.size() < DdsHeader.SIZE + 4)
			throw new DdsFileMalformedException(filename, "The data is way too small to store actual information.");

		
		// Now, get a DDS header.
		DdsHeader header = new DdsHeader(ddsData, 4);
		
		
		// Collect info from the DDS file.
		Dds10Header header10 = getDDS10Header(header, ddsData);
		Dimensions dims = getDimensions(header, header10);
		UncheckedImageFormat fmt = getImageFormat(header, header10);

		int data[] = getImageCounts(header, header10);
		int numMipmaps = data[0];
		int numFaces = data[1];
		int numArrays = data[2];
			
		int baseOffset = getByteOffsetToData(header);

	
		

		//Build the image creator. No more exceptions, except for those thrown by the ImageCreator.
		ImageCreator imgCreator = new ImageCreator(new ImageFormat(fmt), dims, numMipmaps, numArrays, numFaces);
		int cumulativeOffset = baseOffset;
		for (int arrayIx = 0; arrayIx < numArrays; arrayIx++)
		{
			for(int faceIx = 0; faceIx < numFaces; faceIx++)
			{
				for(int mipmapLevel = 0; mipmapLevel < numMipmaps; mipmapLevel++)
				{
					imgCreator.setImageData(ddsData, cumulativeOffset, true, mipmapLevel, arrayIx, faceIx);
					cumulativeOffset += calcMipmapSize(dims, mipmapLevel, fmt);
				}
			}
		}
		
		return imgCreator.createImage();
	}
	
	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static int calcMipmapSize(Dimensions dims, int currLevel, UncheckedImageFormat fmt) {
		Dimensions mipmapDims = new Dimensions(ImageCreator.modifySizeForMipmap(dims, currLevel));

		int lineSize = calcLineSize(fmt, mipmapDims.width);

		int effectiveHeight = 1;
		if(mipmapDims.numDimensions > 1)
		{
			effectiveHeight = mipmapDims.height;
			if(fmt.eBitdepth == BD_COMPRESSED)
				effectiveHeight = (effectiveHeight + 3) / 4;
		}

		int effectiveDepth = 1;
		if(mipmapDims.numDimensions > 2)
		{
			effectiveDepth = mipmapDims.depth;
			if(fmt.eBitdepth == BD_COMPRESSED)
				effectiveDepth = (effectiveDepth + 3) / 4;
		}

		int numLines = effectiveHeight * effectiveDepth;
		return numLines * lineSize;
	}
	


	private static int calcLineSize(UncheckedImageFormat fmt, int lineWidth) {
		//This is from the DDS suggestions for line size computations.
		if(fmt.eBitdepth == BD_COMPRESSED)
		{
			int blockSize = 16;

			if(fmt.eType == DT_COMPRESSED_BC1 ||
				fmt.eType == DT_COMPRESSED_UNSIGNED_BC4 || fmt.eType == DT_COMPRESSED_SIGNED_BC4)
				blockSize = 8;

			return ((lineWidth + 3) / 4) * blockSize;
		}

		int bytesPerPixel = ImageCreator.calcBytesPerPixel(new ImageFormat(fmt));
		return lineWidth * bytesPerPixel;
	}


	private static int getByteOffsetToData(DdsHeader header) {
		int byteOffset = DdsHeader.SIZE + 4;

		if (header.ddspf.dwFourCC == MagicNumbers.DDS10_FOUR_CC) {
			byteOffset += Dds10Header.SIZE;
		}

		return byteOffset;
	}


	private static int[] getImageCounts(DdsHeader header, Dds10Header header10) {
		int data[] = new int[3];
		int numMipmaps;
		int numFaces;
		int numArrays;
		
		if ((header.dwFlags & DdsFlags.DDSD_MIPMAPCOUNT) != 0) {
			numMipmaps = header.dwMipMapCount;
		} else {
			numMipmaps = 1;
		}
		
		if ((header10.miscFlag & Dds10MiscFlags.DDS_RESOURCE_MISC_TEXTURECUBE) != 0) {
			numFaces = 6;
		} else {
			numFaces = 1;
		}
		
		if (header10.arraySize > 1) {
			numArrays = header10.arraySize;
		} else {
			numArrays = 1;
		}
		
		data[0] = numMipmaps;
		data[1] = numFaces;
		data[2] = numArrays;
		
		return data;
	}


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
		UncheckedImageFormat fmt;
		OldDdsFmtMatch ddsFmt;
		
		OldDdsFormatConv(UncheckedImageFormat fmt, OldDdsFmtMatch ddsFmt) {
			this.fmt = fmt;
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
		} else {
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
	
	private static OldDdsFormatConv g_oldFmtConvert[] = {
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

	private static UncheckedImageFormat getImageFormat(DdsHeader header, Dds10Header header10) throws DdsFileUnsupportedException {
		for (int convIx = 0; convIx < g_oldFmtConvert.length; convIx++) {
			if (doesMatchFormat(g_oldFmtConvert[convIx].ddsFmt, header)) {
				return g_oldFmtConvert[convIx].fmt;
			}
		}

		throw new DdsFileUnsupportedException("", "Could not use the DDS9's image format.");
	}




	public static ImageSet loadFromFile(String filename) throws DdsFileMalformedException, UnsupportedEncodingException, DdsFileUnsupportedException, CubemapsMustBe2DException, BadFaceCountException, No3DTextureArrayException, NoImagesSpecifiedException {
		// Load the file.
		ArrayList<Character> fileData = new ArrayList<>();

		InputStream in = ClassLoader.class.getResourceAsStream(filename);
		Reader buffer = new BufferedReader(new InputStreamReader(in));
		int r;
		
        try {
			while ((r = buffer.read()) != -1) {
				fileData.add((char) r);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		return processDDSData(fileData, filename);
	}
	
	
	private static Dds10Header getDDS10Header(DdsHeader header, ArrayList<Character> ddsData) throws DdsFileUnsupportedException {
		if (header.ddspf.dwFourCC == MagicNumbers.DDS10_FOUR_CC) {
			Dds10Header header10;
			int offsetToNewHeader = 4 + DdsHeader.SIZE;

			header10 = new Dds10Header(ddsData, offsetToNewHeader);

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
		} else {
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
	
	
	private static Dimensions getDimensions(DdsHeader header, Dds10Header header10) {
		Dimensions dims = new Dimensions();
		dims.numDimensions = 1;
		dims.width = header.dwWidth;
		
		if ((header.dwFlags & DdsFlags.DDSD_HEIGHT) != 0) {
			dims.numDimensions = 2;
			dims.height = header.dwHeight;
		}
		
		if ((header.dwFlags & DdsFlags.DDSD_DEPTH) != 0) {
			dims.numDimensions = 3;
			dims.depth = header.dwDepth;
		}

		return dims;
	}
}
