package integeruser.jglsdk.glimg;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;


/**
 * Visit https://github.com/integeruser/jgltut for info and updates.
 */
public class ImageSet {
    public static class Dimensions {
        public int numDimensions;  // The number of dimensions of an image. Can be 1, 2, or 3.
        public int width;          // The width of the image. Always valid.
        public int height;         // The height of the image. Only valid if numDimensions is 2 or 3.
        public int depth;          // The depth of the image. Only valid if numDimensions is 3.

        ////////////////////////////////
        public Dimensions() {
        }

        public Dimensions(Dimensions dimensions) {
            numDimensions = dimensions.numDimensions;
            width = dimensions.width;
            height = dimensions.height;
            depth = dimensions.depth;
        }

        ////////////////////////////////
        // Computes the number of rows of pixel data in the image.
        public int calcNumLines() {
            switch (numDimensions) {
                case 1:
                    return 1;

                case 2:
                    return height;

                case 3:
                    return depth * height;
            }
            // Should not be possible.
            return -1;
        }
    }

    public static class SingleImage {
        public SingleImage(ImageSet imageSet, int mipmapLevel, int arrayIx, int faceIx) {
            this.imageSet = imageSet;
            this.arrayIx = arrayIx;
            this.faceIx = faceIx;
            this.mipmapLevel = mipmapLevel;
        }

        ////////////////////////////////
        public Dimensions getDimensions() {
            return imageSet.getDimensions(mipmapLevel);
        }

        public ByteBuffer getImageData() {
            return imageSet.getImageData(mipmapLevel, arrayIx, faceIx);
        }

        public final int getSize() {
            return imageSet.getSize(mipmapLevel);
        }

        ////////////////////////////////
        private ImageSet imageSet;
        private int arrayIx, faceIx, mipmapLevel;
    }

    ////////////////////////////////
    ImageSet(ImageFormat imageFormat, Dimensions imageDimensions, int mipmapCount, int arrayCount, int faceCount,
             ArrayList<byte[]> imageData, int[] imageSizes) {
        format = imageFormat;
        dimensions = imageDimensions;
        this.imageData = imageData;
        this.imageSizes = imageSizes;
        this.arrayCount = arrayCount;
        this.mipmapCount = mipmapCount;
        this.faceCount = faceCount;
    }

    ////////////////////////////////
    public int getMipmapCount() {
        return mipmapCount;
    }


    public ImageFormat getFormat() {
        return format;
    }


    public Dimensions getDimensions() {
        return dimensions;
    }

    public Dimensions getDimensions(int mipmapLevel) {
        return Util.calcMipmapLevelDimensions(dimensions, mipmapLevel);
    }


    public SingleImage getImage(int mipmapLevel, int arrayIx, int faceIx) {
        return new SingleImage(this, mipmapLevel, arrayIx, faceIx);
    }

    ////////////////////////////////
    public int getArrayCount() {
        return arrayCount;
    }

    public int getFaceCount() {
        return faceCount;
    }


    public int getSize(int mipmap) {
        return imageSizes[mipmap];
    }


    public ByteBuffer getImageData(int mipmapLevel, int arrayIx, int faceIx) {
        byte[] imageData = this.imageData.get(mipmapLevel);
        int imageDataOffset = ((arrayIx * faceCount) + faceIx) * imageSizes[mipmapLevel];

        ByteBuffer imageDataBuffer = BufferUtils.createByteBuffer(imageSizes[mipmapLevel]);
        for (int i = imageDataOffset; i < imageDataOffset + imageSizes[mipmapLevel]; i++) {
            imageDataBuffer.put(imageData[i]);
        }
        imageDataBuffer.flip();
        return imageDataBuffer;
    }

    ////////////////////////////////
    private ImageFormat format;
    private Dimensions dimensions;

    private int[] imageSizes;
    private ArrayList<byte[]> imageData;

    private int arrayCount;
    private int faceCount;
    private int mipmapCount;
}
