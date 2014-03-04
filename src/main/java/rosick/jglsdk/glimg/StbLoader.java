package rosick.jglsdk.glimg;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import rosick.jglsdk.glimg.ImageFormat.BitDepth;
import rosick.jglsdk.glimg.ImageFormat.ComponentOrder;
import rosick.jglsdk.glimg.ImageFormat.PixelComponents;
import rosick.jglsdk.glimg.ImageFormat.PixelDataType;
import rosick.jglsdk.glimg.ImageFormat.UncheckedImageFormat;
import rosick.jglsdk.glimg.ImageSet.Dimensions;


/**
 * Visit https://github.com/integeruser/jglsdk for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class StbLoader {

	public static ImageSet loadFromFile(String imagePath) throws IOException {
		InputStream imageInputStream = ClassLoader.class.getResourceAsStream(imagePath);
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
				
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		int numComponents = bufferedImage.getColorModel().getNumComponents();
		
		ImageSet imageSet = buildImageSetFromIntegerData(bufferedImage, width, height, numComponents);
		
		return imageSet;
	}
	
	
		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private static ImageSet buildImageSetFromIntegerData(
			BufferedImage bufferedImage, int width, int height, int numComponents) {
		Dimensions imageDimensions = new Dimensions();
		imageDimensions.numDimensions = 2;
		imageDimensions.depth = 0;
		imageDimensions.width = width;
		imageDimensions.height = height;

		UncheckedImageFormat uncheckedImageFormat = new UncheckedImageFormat();
		uncheckedImageFormat.type = PixelDataType.NORM_UNSIGNED_INTEGER;
		
		switch (numComponents) {
		case 1:
			uncheckedImageFormat.format = PixelComponents.COLOR_RED;
			break;
			
		case 2:
			uncheckedImageFormat.format = PixelComponents.COLOR_RG;
			break;
			
		case 3:
			uncheckedImageFormat.format = PixelComponents.COLOR_RGB;
			break;
			
		case 4:
			uncheckedImageFormat.format = PixelComponents.COLOR_RGBA;
			break;
		}
		
		uncheckedImageFormat.order = ComponentOrder.RGBA;
		uncheckedImageFormat.bitDepth = BitDepth.PER_COMP_8;
		uncheckedImageFormat.lineAlignment = 1;

		byte[] imageData = new byte[width * height * numComponents];
		bufferedImage.getRaster().getDataElements(0, 0, width, height, imageData);				
	
		ImageCreator imgCreator = new ImageCreator(new ImageFormat(uncheckedImageFormat), imageDimensions, 1, 1, 1);
		imgCreator.setImageData(imageData, true, 0, 0, 0);
		
		return imgCreator.createImage();
	}
}