package osi.utilities;

import osi.model.DebugLog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * The ImageAnalysis class provides methods to analyze an image file.
 *
 * <p>
 * The analyzeImage method loads the image from the given file, divides it into two halves (left and right),
 * and calculates the percentage of white pixels in the left half and the percentage of black pixels in the right half.
 * It then checks if the left side is more than 30% white and if the right side is more than 70% black, and prints the results.
 * </p>
 *
 * <p>
 * The isWhite method is a helper method that determines if a given pixel is considered white based on its RGB values.
 * A pixel is considered white if all RGB values are above 240.
 * </p>
 *
 * <p>
 * The isBlack method is a helper method that determines if a given pixel is considered black based on its RGB values.
 * A pixel is considered black if all RGB values are below 15.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 *     ImageAnalysis.analyzeImage("path/to/image.jpg");
 * }</pre>
 * </p>
 */
public class ImageAnalysis {

    public static boolean analyzeImageForBeingLetterSized(String filePath) {
        boolean isEnvelope=false;
        try {

            // Load the image
            BufferedImage image = ImageIO.read(new File(filePath));
            int width = image.getWidth();
            int height = image.getHeight();

            // Calculate the center to divide the image in half
            int center = width / 2;
            int whiteCountLeft = 0;
            int blackCountRight = 0;
            int totalPixelsPerSide = height * (width / 2);

            // Analyze pixels
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < center; x++) { // Left half
                    int pixel = image.getRGB(x, y);
                    if (isWhite(pixel)) {
                        whiteCountLeft++;
                    }
                }
                for (int x = center; x < width; x++) { // Right half
                    int pixel = image.getRGB(x, y);
                    if (isBlack(pixel)) {
                        blackCountRight++;
                    }
                }
            }

            // Calculate percentages
            double whitePercentageLeft = (double) whiteCountLeft / totalPixelsPerSide * 100;
            double blackPercentageRight = (double) blackCountRight / totalPixelsPerSide * 100;
            System.out.println("% left: " + whitePercentageLeft + ": right: " + blackPercentageRight);
            // Check conditions

            if (whitePercentageLeft>30 && blackPercentageRight>70){
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(),DebugLog.INFO,"Scanned image is an envelope");
                isEnvelope=true;
            }
            else {
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(),DebugLog.INFO,"Scanned image is NOT an envelope");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return isEnvelope;
    }

    private static boolean isWhite(int pixel) {
        // Assuming a pixel is considered white if all RGB values are above 240
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = pixel & 0xff;
        return red > 220 && green > 220 && blue > 220;
    }

    private static boolean isBlack(int pixel) {
        // Assuming a pixel is considered black if all RGB values are below 15
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = pixel & 0xff;
        return red < 15 && green < 15 && blue < 15;
    }

    public static void main(String[] args) {
        // Replace "path/to/image.jpg" with the actual file path
      //  analyzeImage("/Users/josephpearce/Downloads/colortest.jpg");
    }
}
