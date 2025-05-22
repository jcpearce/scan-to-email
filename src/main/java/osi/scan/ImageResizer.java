/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scan;


import osi.model.DebugLog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * This program demonstrates how to resize an image.
 *
 * @author www.codejava.net
 */
public class ImageResizer {

    /**
     * Resizes an image to a absolute width and height (the image may not be
     * proportional)
     *
     * @param inputImagePath  Path of the original image
     * @param outputImagePath Path to save the resized image
     * @param scaledWidth     absolute width in pixels
     * @param scaledHeight    absolute height in pixels
     * @throws IOException
     */
    public static void resize(String inputImagePath,
                              String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "IMAGEIO resize method from: " + inputImagePath + " to " + outputImagePath);
        // reads input image
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);

        // writes to output file
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "About to resize image to: " + outputImagePath);
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }

    public static void rotateandcrop(String inputImagePath,
                                        String outputImagePath, int degree, int xcrop, int ycrop) {
        try {

            BufferedImage oldImage = ImageIO.read(new FileInputStream(inputImagePath));
            // below creates a new buffered image with the height as the width and the width as the height
            BufferedImage newImage = new BufferedImage(oldImage.getHeight(), oldImage.getWidth(), oldImage.getType());
            Graphics2D graphics = (Graphics2D) newImage.getGraphics();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "old Image height: " + oldImage.getHeight() + " " + oldImage.getWidth());

            graphics.rotate(Math.toRadians(degree), newImage.getWidth() / 2, newImage.getHeight() / 2);
            graphics.translate((newImage.getWidth() - oldImage.getWidth()) / 2, (newImage.getHeight() - oldImage.getHeight()) / 2);
            graphics.drawImage(oldImage, 0, 0, oldImage.getWidth(), oldImage.getHeight(), null);

            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Image height: " + newImage.getHeight() + " " + newImage.getWidth());


            //   BufferedImage subimage = newImage.getSubimage(450, 0, 1600, 1500);
            FileOutputStream rotated = new FileOutputStream(outputImagePath + "rotated.jpg");
            ImageIO.write(newImage, "JPG", rotated);
            rotated.flush();
            rotated.close();
            BufferedImage rotatedImage = ImageIO.read(new FileInputStream(outputImagePath + "rotated.jpg"));
            File tmpFile = new File(outputImagePath + "rotated.jpg");

            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "About to crop a file of height: " + rotatedImage.getHeight() + " and width: " + rotatedImage.getWidth());
//to make the envelope cropped we need to get this number right
            BufferedImage subimage = rotatedImage.getSubimage(0, 0, 1400, 680);
            //  BufferedImage subimage = newImage.getSubimage(400, 0, 1650, 720);
            //    FileOutputStream croped=   new FileOutputStream(outputImagePath+"-cropped.jpg");
            FileOutputStream croped = new FileOutputStream(outputImagePath);

            ImageIO.write(subimage, "JPG", croped);
            croped.flush();
            croped.close();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "About to delete file: " + tmpFile.getAbsolutePath());
            // boolean delete = FileUtils.deleteQuietly(tmpFile);
            //  boolean delete = tmpFile.delete();
            // System.out.println("WAS IT DELETED: ?" + delete);

            //      ImageIO.write(newImage, "JPG", new FileOutputStream(outputImagePath));

            // now lets crop it

            //  BufferedImage dest = src.getSubimage(0, 0, rect.width, rect.height);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Rotates an image by 90 degrees clockwise and saves it as a new file.
     *
     * @param inputImagePath  Path to the input image file.
     * @param outputImagePath Path to the output image file.
     * @throws IOException If an error occurs during reading or writing the image.
     */
    public static void rotateImageAndSave(String inputImagePath, String outputImagePath) throws IOException {
        // Read the original image
        BufferedImage originalImage = ImageIO.read(new File(inputImagePath));

        // Calculate the new size of the image based on its rotation
        int newWidth = originalImage.getHeight();
        int newHeight = originalImage.getWidth();

        // Create a new buffered image with the new size
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        // Create the transformation object (rotating by 90 degrees)
        AffineTransform transform = new AffineTransform();

        // Move the image to the center of the rotated space
        transform.translate((newWidth - originalImage.getWidth()) / 2.0, (newHeight - originalImage.getHeight()) / 2.0);

        // Rotate the image by 90 degrees around the new center
        transform.rotate(Math.toRadians(90), originalImage.getWidth() / 2.0, originalImage.getHeight() / 2.0);

        // Apply the rotation
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        op.filter(originalImage, rotatedImage);

        // Save the rotated image to the output file
        ImageIO.write(rotatedImage, "JPG", new File(outputImagePath));
    }
    /**
     * Crops an image from the upper right corner and saves the cropped image as a new file.
     *
     * @param inputImagePath  Path to the input image file.
     * @param outputImagePath Path to the output image file.
     * @param cropWidth       Width of the cropped area.
     * @param cropHeight      Height of the cropped area.
     * @throws IOException If an error occurs during reading or writing the image.
     */
    public static void cropImageFromUpperRight(String inputImagePath, String outputImagePath, int cropWidth, int cropHeight) throws IOException {
        // Read the original image
        BufferedImage originalImage = ImageIO.read(new File(inputImagePath));

        // Calculate the starting point for the crop from the upper right corner
        int startX = originalImage.getWidth() - cropWidth;
        int startY = 0; // Starting from the top

        // Ensure the crop dimensions are within the bounds of the original image
        if (startX < 0 || cropHeight > originalImage.getHeight()) {
            throw new IllegalArgumentException("Crop dimensions exceed the bounds of the original image.");
        }

        // Perform the crop
        BufferedImage croppedImage = originalImage.getSubimage(startX, startY, cropWidth, cropHeight);

        // Save the cropped image to the output file
        ImageIO.write(croppedImage, "JPG", new File(outputImagePath));
    }
    public static void rotateandcropNew(String inputImagePath,
                                     String outputImagePath,int degree, int xcrop, int ycrop) {
        ImageResizer.rotateandcropNew(inputImagePath, outputImagePath, degree,0, 0, xcrop, ycrop);
    }

    public static void rotateandcropNew(String inputImagePath,
                                     String outputImagePath, int degree, int xstart, int ystart, int xcrop, int ycrop) {
        try {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Trying to rotate: " + inputImagePath + ":" + xcrop + ":" + ycrop);
            BufferedImage oldImage = ImageIO.read(new FileInputStream(inputImagePath));
            // below creates a new buffered image with the height as the width and the width as the height
            BufferedImage newImage = new BufferedImage(oldImage.getHeight(), oldImage.getWidth(), oldImage.getType());
            Graphics2D graphics = (Graphics2D) newImage.getGraphics();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "old Image height: " + oldImage.getHeight() + " " + oldImage.getWidth());

            graphics.rotate(Math.toRadians(degree), newImage.getWidth() / 2, newImage.getHeight() / 2);
            graphics.translate((newImage.getWidth() - oldImage.getWidth()) / 2, (newImage.getHeight() - oldImage.getHeight()) / 2);
            graphics.drawImage(oldImage, 0, 0, oldImage.getWidth(), oldImage.getHeight(), null);

            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Image height: " + newImage.getHeight() + " " + newImage.getWidth());


            //   BufferedImage subimage = newImage.getSubimage(450, 0, 1600, 1500);
            FileOutputStream rotated = new FileOutputStream(outputImagePath + "rotated.jpg");
            ImageIO.write(newImage, "JPG", rotated);
            rotated.flush();
            rotated.close();
            BufferedImage rotatedImage = ImageIO.read(new FileInputStream(outputImagePath + "rotated.jpg"));
            File tmpFile = new File(outputImagePath + "rotated.jpg");

            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "About to crop a file of height: " + rotatedImage.getHeight() + " and width: " + rotatedImage.getWidth());
//to make the envelope cropped we need to get this number right
            BufferedImage subimage = rotatedImage.getSubimage(xstart, ystart, xcrop, ycrop);
            //  BufferedImage subimage = newImage.getSubimage(400, 0, 1650, 720);
            //    FileOutputStream croped=   new FileOutputStream(outputImagePath+"-cropped.jpg");
            FileOutputStream croped = new FileOutputStream(outputImagePath);

            ImageIO.write(subimage, "JPG", croped);
            croped.flush();
            croped.close();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "About to delete file: " + tmpFile.getAbsolutePath());
            // boolean delete = FileUtils.deleteQuietly(tmpFile);
            //  boolean delete = tmpFile.delete();
            // System.out.println("WAS IT DELETED: ?" + delete);

            //      ImageIO.write(newImage, "JPG", new FileOutputStream(outputImagePath));

            // now lets crop it

            //  BufferedImage dest = src.getSubimage(0, 0, rect.width, rect.height);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    /**
     * Converts a JPEG image to a PNG image.
     *
     * @param jpgFilePath The file path of the JPEG image.
     */
    public static void convertJpgToPng(String jpgFilePath) {
        try {
            // Read the JPEG file
            File jpgFile = new File(jpgFilePath);
            BufferedImage image = ImageIO.read(jpgFile);

            // Construct the path for the PNG file
            String pngFilePath = jpgFilePath.substring(0, jpgFilePath.lastIndexOf('.')) + ".png";

            // Write the image as a PNG file
            File pngFile = new File(pngFilePath);
            ImageIO.write(image, "png", pngFile);

            System.out.println("PNG file created at: " + pngFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void rotateAndCropEnvelopeImage(String fileToCrop) throws IOException {
        ImageResizer.rotateImageAndSave(fileToCrop, fileToCrop);
        ImageResizer.cropImageFromUpperRight(fileToCrop,fileToCrop,1900,825);

    }

    /**
     * Test resizing images
     */
    public static void main(String[] args) {
        //System.out.println("In Image resizer");
        String inputImagePath = "D:/Photo/Puppy.jpg";
        String outputImagePath1 = "D:/Photo/Puppy_Fixed.jpg";
        String outputImagePath2 = "D:/Photo/Puppy_Smaller.jpg";
        String outputImagePath3 = "D:/Photo/Puppy_Bigger.jpg";

        try {
         //   ImageResizer.rotateandcrop("/Users/josephpearce/Downloads/test.jpg", "/Users/josephpearce/Downloads/test2.jpg", 90, 1650, 720);

    //   ImageResizer.rotateImageAndSave("/Users/josephpearce/Downloads/test.jpg", "/Users/josephpearce/Downloads/test-rotated.jpg");

      //      ImageResizer.cropImageFromUpperRight("/Users/josephpearce/Downloads/test-rotated.jpg","/Users/josephpearce/Downloads/test-cropped.jpg",1900,825);
        ImageResizer.rotateAndCropEnvelopeImage("/Users/josephpearce/Downloads/test.jpg");

        } catch (Exception ex) {
            //   System.out.println("Error resizing the image.");
            ex.printStackTrace();
        }
    }

}
