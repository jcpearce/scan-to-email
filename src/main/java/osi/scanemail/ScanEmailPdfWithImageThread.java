/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scanemail;

import osi.email.EmailThread;
import osi.email.SendGmail;
import osi.model.Client;
import osi.model.Log;
import osi.util.Format;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * @author Joseph Pearce
 */
public class ScanEmailPdfWithImageThread extends Thread {

    //public final int MANAGERSESSION = 1;
    private int sendertype;
    private String receipient;
    //  private String sender;
    private String body;
    private String subject;
    private String file;
    private Client client;
    private String jpegfile;
    private String bodyOCR; // the ocr of the pdf / if available

    public void run() {
        try {
            ArrayList<String> list = new ArrayList<String>();

            if (bodyOCR != null && !bodyOCR.trim().isEmpty()) {
                System.out.println("Attaching: " + bodyOCR);
                Path tempFile = Files.createTempFile("bodycontent", ".txt");
                Files.write(tempFile, bodyOCR.getBytes(StandardCharsets.UTF_8));
                list.add(tempFile.toString());
            }
            list.add(jpegfile);
            list.add(file);
            SendGmail sendGmail = new SendGmail(SendGmail.RECEPTIONIST);
            sendGmail.sendMessageAR(receipient, body, subject, list);


            Log log2 = new Log(Log.SEVERITY_INFO, "Image-send", "sent image email to " + client.getFirmname() + " :N " + client.getMemnum(), body);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * @param client
     * @param file
     */
    public ScanEmailPdfWithImageThread(Client client, String file, String jpegfile) {

        this.receipient = client.getEmail();
        this.client = client;
        this.body = "This piece of mail came in for you. <br/><br/> " + Format.getLineBreak();

        this.subject = "Scanned mail & contents at OSI";
        System.out.println("In constructor for EmailIMageThread sending file: " + file);
        this.file = file;
        this.jpegfile = jpegfile;

    }

    /**
     * @param client
     * @param file
     */
    public ScanEmailPdfWithImageThread(Client client, String file, String jpegfile, String subject, String ocrText) {

        this.receipient = client.getEmail();
        this.client = client;
        this.body = "This piece of mail came in for you. <br/><br/> " + Format.getLineBreak();

        this.bodyOCR = ocrText;
        if (ocrText.trim().length() > 5) {
            body = body + "<hr/> To aid in mail searches the decipherable text of the mail contents has been attached <br/> ";
            // body = body + ocrText;
        }

        this.subject = subject;
        //  System.out.println("In constructor for EmailIMageThread sending file: " + file);
        this.file = file;
        this.jpegfile = jpegfile;

    }

    /**
     * @param client
     * @param file
     */
    public ScanEmailPdfWithImageThread(Client client, String file, String jpegfile, String subject) {

        this.receipient = client.getEmail();
        this.client = client;
        this.body = "This piece of mail came in for you. <br/><br/> " + Format.getLineBreak();

        this.subject = subject;
        //  System.out.println("In constructor for EmailIMageThread sending file: " + file);
        this.file = file;
        this.jpegfile = jpegfile;

    }

    /**
     * Sends a message to a given receipient. It defaults to the manager session
     *
     * @param receipient Receipent of email
     * @param body       Body of message
     * @param subject    Subject of message
     */
    public ScanEmailPdfWithImageThread(String receipient, String body, String subject) {
        this.receipient = receipient;
        //    this.sender = ConfigHandler.getSMTPServerUserName();
        this.body = body;
        this.subject = subject;
        sendertype = SendGmail.MANAGERSESSION;

    }

}
