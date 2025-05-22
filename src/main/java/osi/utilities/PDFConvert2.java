/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.utilities;

//import org.apache.pdfbox.exceptions.COSVisitorException;


import javafx.util.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import osi.model.DebugLog;
import osi.scan.CacheData;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * creationdate Jul 4, 2015 8:50:47 PM
 *
 * @author joseph pearce
 */
public class PDFConvert2 {
    private static final int maximumfilesize = 10000000;

    public static void main(String args[]) {
        PDFConvert2 pD = new PDFConvert2();
        try {
            //  pD.createPDFFromImage("/temp/la3.jpg", "/temp/testlarge.pdf");
            //  ImageResizer.resize("/temp/Morena_example_img_1.jpg", "/temp/newpic.jpg", 800, 1000);
            // ImageResizer.changedpi("/temp/large2.jpg", "/temp/newpicdpi.jpg");
            ArrayList<String> files = new ArrayList<String>();
            //   files.add("/temp/la.jpg");
            //  files.add("C:\\Users\\osi\\temp\\scan1.jpg");
            // files.add("C:\\Users\\osi\\temp\\scan2.jpg");
            // pD.createPDFFromUnSizedImages(files, "C:\\Users\\osi\\temp\\testres4.pdf");
            //    files.add("/Users/josephpearce/temp/scan1.jpg");
            //   files.add("/Users/josephpearce/temp/scan2.jpg");
            //  ImageResizer.resize("/Users/josephpearce/temp/scan1.jpg", "/Users/josephpearce/temp/scan1-resize.jpg", 700, 900);
            //  ImageResizer.resize("/Users/josephpearce/temp/scan2.jpg", "/Users/josephpearce/temp/scan2-resize.jpg", 800, 1000);
            for (int i = 0; i < 20; i++) {
                files.add("/Users/josephpearce/temp/scan.jpg");
            }
            //   files.add("/Users/josephpearce/temp/scan1-resize.jpg");
            // files.add("/Users/josephpearce/temp/scan2-resize.jpg");
            //ImageResizer
         //   ArrayList<Pair> brokenDownPdf = pD.getBrokenDownPdf(files);

             pD.createPDFFromImages2(files, "/Users/josephpearce/temp/test.pdf");
        } catch (Exception ex) {
            Logger.getLogger(PDFConvert2.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    /**
     * Given a list of files, creates a single file
      * @param files
     * @param outputFile
     * @return
     * @throws IOException
     */
    public String createPDFFromImages(List<String> files, String outputFile)
            throws IOException {
        // the document
        //  PDDocument doc = null;
        PDDocument doc = new PDDocument();

        try {
            for (int i = 0; i < files.size(); i++) {
                String image = files.get(i);
                PDPage page = new PDPage(PDRectangle.LETTER);
                //    PDRectangle rect = page.getMediaBox();
                //    rect.setUpperRightX(1600);
                //   rect.setUpperRightY(2070);

                // PDPage.PAGE_SIZE_LETTER is also possible
                //  PDRectangle rect = page.getMediaBox();
                // rect can be used to get the page width and height
                doc.addPage(page);

                //   doc = PDDocument.load(inputFile);
                //we will add the image to the first page.
                // PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
                PDImageXObject ximage = null;
                if (image.toLowerCase().endsWith(".jpg")) {
                    // ximage = new PDJpeg(doc, new FileInputStream(image));
                    ximage = PDImageXObject.createFromFile(image, doc);

                    //  File outputfile = new File("/tmp/saved.jpg");
                    // ImageIO.write( ximage.getImage(), "jpg", outputfile);
                } else {
                    // BufferedImage awtImage = ImageIO.read(new File(image));
                    //    ximage = new PDPixelMap(doc, awtImage);
                    throw new IOException("Image type not supported:" + image);
                }
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
                //contentStream.drawImage(ximage, 20, 20);
                contentStream.drawImage(ximage, 20, 20, 600, 750);
                contentStream.close();

            }
            doc.save(outputFile);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
        return outputFile;
    }

    /**
     * Returns possible multiple pdf files. This was created because the sending mail has a size limitation
     *
     * @param files
     * @param outputFile
     * @return
     * @throws IOException
     */
    public ArrayList<String> createPDFFromImages2(ArrayList<String> files, String outputFile)
            throws IOException {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(),8,"Creating pdf from images of file size: "+files.size());
        String filebase=outputFile;
        ArrayList<String> pdfFiles = new ArrayList<String>();
        PDDocument doc = new PDDocument();
        ArrayList<Pair> brokenDownPdf = this.getBrokenDownPdf(files);
        for (int i = 0; i < brokenDownPdf.size(); i++) {
            Pair pair = brokenDownPdf.get(i);
            int start= (int) pair.getKey();
            int end= (int)pair.getValue();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(),8,"Creating a pdf from images from index : "+start+" to "+end);
            List subSet =  files.subList(start, end);
            if (brokenDownPdf.size()>1) {
                outputFile=filebase+"-part-"+i+".pdf";
            }
            String pdfFromImages = this.createPDFFromImages(subSet, outputFile);
            pdfFiles.add(pdfFromImages);
        }

        return pdfFiles;
    }

    private ArrayList<Pair> getBrokenDownPdf(ArrayList<String> files) {
        ArrayList<Pair> listOfStartFinishes = new ArrayList<Pair>();
        int cursizecount = 0;
        boolean morethanone = false;
        long curtotalbytes = 0;
        int start = 0;
        int finish;
        for (int i = 0; i < files.size(); i++) {
            File file = new File(files.get(i));
            long length = file.length();
            curtotalbytes = curtotalbytes + length;
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Size of file: " + files.get(i) + " size: " + length + " total size: " + curtotalbytes);
            if (curtotalbytes > maximumfilesize) {
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "We have exceeded the maximum allowable file size");
                Pair pdfPair = new Pair(start, i);
                listOfStartFinishes.add(pdfPair);
               // start = i + 1;
                start = i;
                curtotalbytes = 0;

                morethanone = true;
            }
        }
        if (start < files.size()) {
            Pair pdfPair = new Pair(start, files.size());
            listOfStartFinishes.add(pdfPair);
        }
        //   System.out.println("last start: "+start);
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Number of pdf files broken down: " + listOfStartFinishes.size());
        for (int j = 0; j < listOfStartFinishes.size(); j++) {
            Pair pair = listOfStartFinishes.get(j);
            System.out.println(" one file from page: " + pair.getKey() + "to" + pair.getValue());

        }


        return listOfStartFinishes;
    }

    public String createPDFFromUnSizedImages(ArrayList<String> files, String finalfile)
            throws IOException {
        // the document
        //  PDDocument doc = null;
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "in method create pdf from unsized images :Trying to create finalfile: " + finalfile);
        String tmpDir = CacheData.getTempDirectory();
        List<File> filesToDelete = new ArrayList<File>();
        PDDocument doc = new PDDocument();
        //String outputFile = tmpDir + finalfile + "test.pdf";
        // DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "PDF convert final file: " + outputFile);
        try {
            for (int i = 0; i < files.size(); i++) {

                String image = files.get(i);
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "creating a pdf page from file: " + image);
                //  System.out.println("About to RESIZE image: " + filename);
                //   ImageResizer.resize(filename, filename+"size", 800, 1000);
                //  String newfile = image.substring(0, (image.length() - 4)) + "res.jpg";
                //  osi.scan.ImageResizer.changedpi(image, newfile);
                //    File jpgFile2=new File(newfile);

                //  image = newfile;
                filesToDelete.add(new File(image));
                //     filesToDelete.add(new File(newfile));

                PDPage page = new PDPage(PDRectangle.LETTER);
                PDRectangle rect = page.getMediaBox();
                rect.setUpperRightX(1600);
                rect.setUpperRightY(2070);

                // PDPage.PAGE_SIZE_LETTER is also possible
                //  PDRectangle rect = page.getMediaBox();
                // rect can be used to get the page width and height
                doc.addPage(page);

                //   doc = PDDocument.load(inputFile);
                //we will add the image to the first page.
                // PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
                PDImageXObject ximage = null;
                if (image.toLowerCase().endsWith(".jpg")) {
                    ximage = PDImageXObject.createFromFile(image, doc);
                    // ximage = new PDImageXObject.createFromFile. PDJpeg(doc, new FileInputStream(image));
                } else {
                    //   BufferedImage awtImage = ImageIO.read(new File(image));
                    // ximage = new PDPixelMap(doc, awtImage);
                    throw new IOException("Image type not supported:" + image);
                }
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);
                contentStream.drawImage(ximage, 20, 20);
                //contentStream.drawImage(ximage);
                contentStream.close();

            }
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "About to save file: " + finalfile);
            doc.save(finalfile);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
        //  Files.delete(source);

        for (File file : filesToDelete) {
            //    ImageResizer.resize(file.getAbsolutePath()+file.getName(), file.getAbsolutePath()+file.getName()+"te"+"size", 800, 1000);

            //     System.out.println("Deleting file: " + file.getName());
            //   file.delete();
            //     file.deleteOnExit();
        }
        return finalfile;
    }


}
