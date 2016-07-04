package org.jglsdk.glimg;

import org.jglsdk.glimg.ImageFormat.PixelDataType;
import org.jglsdk.glimg.ImageSet.Dimensions;
import org.jglsdk.glimg.Util.CompressedBlockData;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 */
class ImageCreator {
    ImageCreator(ImageFormat ddsFormat, Dimensions ddsDimensions, int mipmapCount, int arrayCount, int faceCount) {
        imageFormat = ddsFormat;
        imageDimensions = new Dimensions(ddsDimensions);
        this.mipmapCount = mipmapCount;
        this.arrayCount = arrayCount;
        this.faceCount = faceCount;

        if (faceCount != 6 && faceCount != 1) throw new BadFaceCountException();
        if (faceCount == 6 && ddsDimensions.numDimensions != 2) throw new CubemapsMustBe2DException();
        if (ddsDimensions.numDimensions == 3 && arrayCount != 1) throw new No3DTextureArrayException();
        if (mipmapCount <= 0 || arrayCount <= 0) throw new NoImagesSpecifiedException();

        imageData = new ArrayList<>(mipmapCount);
        imageSizes = new int[mipmapCount];

        // Allocate the memory for our data.
        for (int mipmapLevel = 0; mipmapLevel < mipmapCount; mipmapLevel++) {
            Dimensions mipmapLevelDimensions = Util.calcMipmapLevelDimensions(ddsDimensions, mipmapLevel);

            int mipmapLevelSize = Util.calcMipmapLevelSize(ddsFormat, mipmapLevelDimensions);
            imageSizes[mipmapLevel] = mipmapLevelSize;

            byte[] mipmapLevelData = new byte[mipmapLevelSize * faceCount * arrayCount];
            imageData.add(mipmapLevelData);
        }
    }


    ////////////////////////////////
    void setImageData(byte sourceData[], boolean isTopLeft, int mipmapLevel, int arrayIx, int faceIx) {
        if (imageData.isEmpty()) throw new ImageSetAlreadyCreatedException();

        // Check inputs.
        if ((arrayIx < 0) || (arrayCount <= arrayIx)) throw new ArrayIndexOutOfBoundsException();
        if ((faceIx < 0) || (faceCount <= faceIx)) throw new FaceIndexOutOfBoundsException();
        if ((mipmapLevel < 0) || (mipmapCount <= mipmapLevel)) throw new MipmapLayerOutOfBoundsException();

        // Get the image relative to mipmapLevel
        byte[] imageData = this.imageData.get(mipmapLevel);

        if (!isTopLeft) {
            Util.throwNotYetPortedException();
        } else {
            int imageDataOffset = ((arrayIx * faceCount) + faceIx) * imageSizes[mipmapLevel];
            copyImageFlipped(sourceData, imageData, imageDataOffset, mipmapLevel);
        }
    }


    ImageSet createImage() {
        if (imageData.isEmpty()) throw new ImageSetAlreadyCreatedException();
        return new ImageSet(imageFormat, imageDimensions, mipmapCount, arrayCount, faceCount, imageData, imageSizes);
    }

    ////////////////////////////////
    private ImageFormat imageFormat;
    private Dimensions imageDimensions;

    private int mipmapCount;
    private int arrayCount;
    private int faceCount;

    private ArrayList<byte[]> imageData;
    private int[] imageSizes;


    private static class BadFaceCountException extends RuntimeException {
    }

    private static class CubemapsMustBe2DException extends RuntimeException {
    }

    private static class No3DTextureArrayException extends RuntimeException {
    }

    private static class NoImagesSpecifiedException extends RuntimeException {
    }

    private static class ImageSetAlreadyCreatedException extends RuntimeException {
    }

    private static class MipmapLayerOutOfBoundsException extends RuntimeException {
    }

    private static class FaceIndexOutOfBoundsException extends RuntimeException {
    }

    ////////////////////////////////
    private void copyImageFlipped(byte[] sourceData, byte[] imageData, int imageDataOffset, int mipmapLevel) {
        assert (sourceData.length * faceCount * arrayCount) == imageData.length;

        Dimensions mipmapImageDimensions = Util.calcMipmapLevelDimensions(new Dimensions(imageDimensions), mipmapLevel);

        if (imageFormat.getPixelDataType().ordinal() < PixelDataType.NUM_UNCOMPRESSED_TYPES.ordinal()) {
            copyPixelsFlipped(imageFormat, sourceData, imageData, imageDataOffset, imageSizes[mipmapLevel], mipmapImageDimensions);
        } else {
            // Have to decode the pixel data and flip it manually.
            switch (imageFormat.getPixelDataType()) {
                case COMPRESSED_BC1:
                case COMPRESSED_BC2:
                case COMPRESSED_BC3:
                case COMPRESSED_UNSIGNED_BC4:
                case COMPRESSED_SIGNED_BC4:
                case COMPRESSED_UNSIGNED_BC5:
                case COMPRESSED_SIGNED_BC5:
                    copyBCFlipped(imageFormat, sourceData, imageData, imageDataOffset, imageSizes[mipmapLevel], mipmapImageDimensions, mipmapLevel);
                    break;

                default:
                    Util.throwNotYetPortedException();
                    break;
            }
        }
    }


    private void copyPixelsFlipped(ImageFormat imageFormat, byte[] sourceData, byte[] imageData, int imageDataOffset,
                                   int imageSize, Dimensions imageDimensions) {
        // Flip the data. Copy line by line.
        final int numLines = imageDimensions.calcNumLines();
        final int lineSize = imageFormat.alignByteCount(Util.calcBytesPerPixel(imageFormat) * imageDimensions.width);

        // Flipped: start from last line of source, going backward
        int sourceLineOffset = imageSize - lineSize;  // start from last line
        int imageDataLineOffset = imageDataOffset;    // start from imageDataOffset

        for (int line = 0; line < numLines; line++) {
            byte[] sourceLine = Arrays.copyOfRange(sourceData, sourceLineOffset, sourceLineOffset + lineSize);

            // Copy the source line into imageData
            System.arraycopy(sourceLine, 0, imageData, imageDataLineOffset, lineSize);

            // Update indices
            sourceLineOffset -= lineSize;
            imageDataLineOffset += lineSize;
        }
    }


    private void copyBCFlipped(ImageFormat imageFormat, byte[] sourceData, byte[] imageData, int imageDataOffset,
                               int imageSize, Dimensions imageDimensions, int mipmapLevel) {
        // No support for 3D compressed formats.
        assert imageDimensions.numDimensions != 3 : "No support for 3D compressed formats.";

        CompressedBlockData blockData = Util.getBlockCompressionData(imageFormat.getPixelDataType());
        final int blocksPerLine = (imageDimensions.width + (blockData.dimensions.width - 1)) / blockData.dimensions.width;

        final int blockLineSize = blocksPerLine * blockData.byteCount;
        final int numTotalBlocks = imageSize / blockData.byteCount;
        final int numLines = numTotalBlocks / blocksPerLine;

        // Copy each block.
        int sourceBlockOffset = imageSize - blockLineSize;  // start from last block
        int imageDataBlockOffset = imageDataOffset;         // start from imageDataOffset

        for (int line = 0; line < numLines; ++line) {
            for (int block = 0; block < blocksPerLine; ++block) {
                byte[] sourceBlock = Arrays.copyOfRange(sourceData, sourceBlockOffset, sourceBlockOffset + blockData.byteCount);

                flippingFunc(imageFormat, sourceBlock, imageData, imageDataBlockOffset);

                sourceBlockOffset += blockData.byteCount;
                imageDataBlockOffset += blockData.byteCount;
            }

            // First goes back to beginning, second goes back one row.
            sourceBlockOffset -= blockLineSize;
            sourceBlockOffset -= blockLineSize;
        }
    }

    // TODO
    private void flippingFunc(ImageFormat imageFormat, byte[] sourceData, byte[] imageData, int imageDataOffset) {
        switch (this.imageFormat.getPixelDataType()) {
            case COMPRESSED_BC1:
                copyBlockBC1Flipped(sourceData, imageData, imageDataOffset);
                break;

            default:
                Util.throwNotYetPortedException();
                break;
        }
    }

    private void copyBlockBC1Flipped(byte[] sourceData, byte[] imageData, int imageDataOffset) {
        assert sourceData.length == 8;

        // First 4 bytes are 2 16-bit colors. Keep them the same.
        for (int i = 0; i < 4; i++) {
            imageData[imageDataOffset + i] = sourceData[i];
        }

        // Next four bytes are 16 2-bit values, in row-major, top-to-bottom order,
        // representing the 4x4 pixel data for the block. So copy the bytes in reverse order.
        imageData[imageDataOffset + 4] = sourceData[7];
        imageData[imageDataOffset + 5] = sourceData[6];
        imageData[imageDataOffset + 6] = sourceData[5];
        imageData[imageDataOffset + 7] = sourceData[4];
    }
}