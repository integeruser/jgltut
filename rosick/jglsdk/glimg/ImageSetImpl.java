package rosick.jglsdk.glimg;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

import rosick.jglsdk.glimg.ImageSet.Dimensions;


public class ImageSetImpl {
	
	ImageFormat m_format;
	Dimensions m_dimensions;
	ArrayList<Integer> m_imageSizes;
	ArrayList<ArrayList<Integer>> m_imageData;
	int m_mipmapCount;
	int m_faceCount;

	
	ImageSetImpl(ImageFormat format, Dimensions dimensions,
			int mipmapCount, int arrayCount, int faceCount,
			ArrayList<ArrayList<Integer>> imageData,
			ArrayList<Integer> imageSizes) {
		m_format = format;
		m_dimensions = dimensions;
		m_imageData = imageData;
		m_imageSizes = imageSizes;
		m_mipmapCount = mipmapCount;
		m_faceCount = faceCount;
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	public int getMipmapCount() {
		return m_mipmapCount;
	}

	public ImageFormat getFormat() {
		return m_format;
	}
	
	public Dimensions getDimensions(int mipmapLevel) {
		return Util.modifySizeForMipmap(m_dimensions, mipmapLevel);
	}

	public ByteBuffer getImageData(int mipmapLevel, int arrayIx, int faceIx) {		
		int imageOffset = ((arrayIx * m_faceCount) + faceIx) * m_imageSizes.get(mipmapLevel);
		ArrayList<Integer> image = m_imageData.get(mipmapLevel);
		List<Integer> offsettedImage = image.subList(imageOffset, image.size());

		ByteBuffer tempBuffer = BufferUtils.createByteBuffer(offsettedImage.size());
		
		for (Integer integer : offsettedImage) {
			tempBuffer.put(integer.byteValue());
		}
		
		tempBuffer.flip();
		
		return tempBuffer;
	}
}
