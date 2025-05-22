/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package osi.utilities;

import osi.Exception.ScanException;
import osi.ai.DocumentAnalysisService;
import osi.email.EmailDispatcher;
import osi.model.Client;
import osi.model.DebugLog;
import osi.model.EmailSendResult;
import osi.scanemail.ScanGmailPdfWithImage;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The AfterScan class represents a process to be performed after scanning files.
 * It processes the scanned files, performs OCR, converts images to PDF,
 * inserts the PDF into a database, sends emails with attachments, and summarizes
 * the OCR text using GPT.
 */
public class AfterScan extends Thread {

    private Client client;
    private String letterName;
    private String pdfName;
    private String ocrText;
    private String docName;
    private ArrayList<String> filesToConvert;
    private String id;
    private String parentid;
    String tmpDir;
    private String subject;
    private final int smallestOCRSize = 200;
    private final int maxOCRSize = 1000000;


    /**
     * Processes the scanned images and performs OCR on them.
     * Converts the scanned images to PDF and sends them via email.
     */
    public void process() {
        ArrayList<String> pdfFromImages = new ArrayList<>();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In after scan thread for number of files: " + filesToConvert.size());
        ocrText = "";

        List<File> filesToDelete = new ArrayList<File>();
        String envOCR = ScanUtilities.performOCR(letterName);
        //String docname2="";

        for (int i = 0; i < filesToConvert.size(); i++) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Performing OCR on " + filesToConvert.get(i));
            ocrText = ocrText + ScanUtilities.performOCR(filesToConvert.get(i));
            //  ocrText = ocrText + "--------------------------------------------------------------" + Format.getLineBreak();
        }

        PDFConvert2 pD = new PDFConvert2();
        try {
            pdfFromImages = pD.createPDFFromImages2(filesToConvert, pdfName);
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.TRACE, "number of pdf files returned: " + pdfFromImages.size());
            if (pdfFromImages.size() == 1) {
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Inserting a single file into the database");
                MailItemInsert mT2 = new MailItemInsert(client, pdfName, false, filesToConvert.size(), docName, "child" + id, parentid);
                try {
                    mT2.process();
                } catch (Exception e) {
                    DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Unable to insert into the database: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }

            } else if ((pdfFromImages.size() > 1)) {
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "The file is too big to email as one file, recreating one file for the database");
                // we need to create one mammoth pdf and insert it and then
                pD.createPDFFromImages(filesToConvert, pdfName);
                MailItemInsert mT2 = new MailItemInsert(client, pdfName, false, filesToConvert.size(), docName, "child" + id, parentid);
                mT2.process();


            }
        } catch (Exception e) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Unable to scan pdf");
            e.printStackTrace();
            try {
                throw new ScanException("Error creating pdf for: " + client.getFirmname() + " " + e.getLocalizedMessage());
            } catch (ScanException ex1) {
                throw new RuntimeException(ex1);
            }
        }
        try {
            if (pdfFromImages.size() == 0) {
                ScanGmailPdfWithImage iT = new ScanGmailPdfWithImage(client, null, letterName, subject, ocrText);
                EmailSendResult emailSendResult = iT.send();

                if (emailSendResult.isSuccess()) {
                    String googleid = "[googlemailid:" + emailSendResult.getEmailId() + "]";
                    DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Google id: " + googleid);
                    ScanUtilities.saveSucessEmailSent(id, "success " + googleid);

                } else {
                    ScanUtilities.saveFailedEmailSent(id, emailSendResult.getErrorMessage());
                    throw new ScanException("Error sending email to : " + client.getFirmname() + " " + emailSendResult.getErrorMessage());

                }


            } else {
                if (ocrText.trim().length() > smallestOCRSize && ocrText.trim().length() < maxOCRSize) {
                    try {
                        //  ocrText = this.summarizeOCRText() + "<br/>" + ocrText;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ScanUtilities.writeTempFile(id + "ocr.txt", ocrText);

                }
                ScanGmailPdfWithImage iT = new ScanGmailPdfWithImage(client, pdfFromImages.get(0), letterName, subject, ocrText);

                EmailSendResult emailSendResult = iT.send();

                if (emailSendResult.isSuccess()) {
                    String googleid = "[googlemailid:" + emailSendResult.getEmailId() + "]";
                    DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Google id: " + googleid);
                    ScanUtilities.saveSucessEmailSent(id, "success " + googleid);

                } else {
                    ScanUtilities.saveFailedEmailSent(id, emailSendResult.getErrorMessage());
                    throw new ScanException("Error sending email to : " + client.getFirmname() + " " + emailSendResult.getErrorMessage());

                }

            }
        } catch (Exception e) {
            ScanUtilities.saveFailedEmailSent(id, e.getLocalizedMessage());
            e.printStackTrace();
            try {
                throw new ScanException("Error sending email to : " + client.getFirmname() + " " + e.getLocalizedMessage());
            } catch (ScanException ex1) {
                throw new RuntimeException(ex1);
            }

        }
        if (pdfFromImages != null && pdfFromImages.size() > 1) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "The PDF to attach has multiple files");


            for (int k = 1; k < pdfFromImages.size(); k++) {
                String text = "Scanned mail & contents at OSI. Additonal part " + (k + 1) + " of " + pdfFromImages.size();
                try {
                    EmailDispatcher emailDispatcher = new EmailDispatcher(EmailDispatcher.RECEPTIONIST);
                    emailDispatcher.sendMessage(client.getEmail(), "The scanned image was to big to send as one attachment. Here is an additional part.", text, pdfFromImages.get(k));

                } catch (Exception e) {

                }
                this.subject = "Scanned mail & contents at OSI";
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Sending additional part #: " + k);

            }
        }
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Finished sending all email parts now trying ocr size: " + ocrText.trim().length());


        try {
            this.moveFilesToSubdirectory(filesToConvert, "processedimages");
        } catch (IOException e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
        }


    }

    public void run() {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Starting thread After scan");
        this.process();
    }

    private List<String> moveFilesToSubdirectory(ArrayList<String> filesToMove, String subdirectory) throws IOException {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Moving # files to sub: " + filesToMove.size() + " and sleeping for 5 seconds");
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> movedFiles = new ArrayList<>();

        // Ensure the subdirectory exists
        Path subdirectoryPath = Paths.get(subdirectory);
        if (!Files.exists(subdirectoryPath)) {
            Files.createDirectories(subdirectoryPath);
        }

        for (String filePathString : filesToMove) {
            Path sourcePath = Paths.get(filePathString);
            if (Files.exists(sourcePath) && !Files.isDirectory(sourcePath)) {
                Path destinationPath = subdirectoryPath.resolve(sourcePath.getFileName());
                // Move file to the new directory
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                movedFiles.add(destinationPath.toString());
            }
        }


        return movedFiles;
    }

    /**
     * Summarizes the OCR text and performs additional actions.
     *
     * @return the summarized OCR text
     */
    private String summarizeOCRText() {
        String summ = "";
        try {
            // DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Doing summary on text: " + ocrText);
            osi.ai.DocumentAnalysisService service = new DocumentAnalysisService();
            summ = service.analyzeDocument(ocrText);
            //  SendEmail2 sE = new SendEmail2(SendEmail2.NOTIFICATIONS);
            // sE.sendMessage("joseph@osioffices.com", summ + "<hr/>" + ocrText, "Scanned GPT Contents");
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.TRACE, "GPTtext: " + summ);
        } catch (Exception e) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Could not do chat gpt summary: " + e.getMessage());
        }
        return summ;
    }

    /**
     * Constructs a new instance of the AfterScan class.
     *
     * @param tmpDir         the temporary directory for storing the scanned images
     * @param client         the client associated with the document
     * @param letterName     the name of the letter
     * @param pdfName        the name of the PDF document
     * @param docName        the name of the document
     * @param filesToConvert the list of files to convert
     * @param id             the unique identifier for the document
     * @param parentid       the parent identifier for the document
     * @param subject        the subject of the document
     */
    public AfterScan(String tmpDir, Client client, String letterName, String pdfName, String docName, ArrayList<String> filesToConvert, String id, String parentid, String subject) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In constructor");
        this.client = client;
        this.letterName = letterName;
        this.pdfName = pdfName;
        this.docName = docName;
        this.filesToConvert = filesToConvert;
        this.id = id;
        this.parentid = parentid;
        this.subject = subject;
    }

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }
}
