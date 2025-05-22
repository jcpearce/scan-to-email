/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scanemail;

import javafx.scene.control.Alert;
import org.apache.commons.lang.StringUtils;
import osi.email.SendGmail;
import osi.factory.ClientFactory;
import osi.factory.MailItemFactory;
import osi.model.Client;
import osi.model.DebugLog;
import osi.model.Log;
import osi.model.OSITask;
import osi.scan.ScanState;
import osi.util.ConfigHandler;
import osi.util.Format;
import osi.util.OSIFileUtils;
import osi.utilities.ScanUtilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joseph Pearce
 */
public class ScanGmailEnvelopeThread extends Thread {

    //public final int MANAGERSESSION = 1;
    private int sendertype;
    private String receipient;
    //  private String sender;
    private String body;
    private String subject;
    private String file;
    private Client client;
    private String id;
    private ScanState scanState;

    public void run() {
        try {
//            System.out.println("Sending a new email thread message to: " + receipient + " from:" + sender);
            //System.out.println("WELCOME MESSAGE: " + body);
            //  MimeMessage newmsg = new MimeMessage(jmaSession);
            // throwError("test inside","test inside2");
            SendGmail sGmail = new SendGmail(SendGmail.RECEPTIONIST);
            // sGmail.setDeleteFilesAfterSend(true);

            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Trying to attach file: " + file);
            ArrayList<String> files=new ArrayList<>();
            files.add(file);

            sGmail.sendMessageAR(receipient, body, subject, files);
            ScanUtilities.saveSucessEmailSent(id,"success");
            Log log2 = new Log(Log.SEVERITY_INFO, "Image-send", "sent image email to " + client.getFirmname() + " :N " + client.getMemnum(), body);

        } catch (Exception e) {
            ScanUtilities.saveFailedEmailSent(id,e.getLocalizedMessage());
            //  Log log = new Log(Log.SEVERITY_ERROR, "image-failure", "error sending EmailEnvelopeImageThread:  ", e);
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Error sending a flat scan ", e);

            e.printStackTrace();
        }
    }

    /**
     * Sends the document as well as the ocrText
     *
     * @param client
     * @param file
     * @param id
     * @param ocrText
     */
    public ScanGmailEnvelopeThread(Client client, String file, String id, String optionalSubject, String ocrText) {
        System.out.println("In scan Email Envelope Thread with OCR text subject: " + optionalSubject);
        this.client = client;
        this.id = id;
        //  this.body = OSIFileUtils.getFileContentsText("scan.html");
        this.body = ConfigHandler.getEntry("ScanHTMLLetter");
        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);

        this.subject = "Scanned mail at OSI Mail Item #" + id;
        try {
            if (!StringUtils.isEmpty(optionalSubject) && optionalSubject.trim().length() > 3) {
                subject = optionalSubject;
            }
        } catch (Exception e) {
            //  e.printStackTrace();
        }
        //  System.out.println("In constructor for EmailIMageThread sending file: " + file);
        this.file = file;
        //  this(client, file, id);
        if (ocrText.trim().length() > 5) {
            body = body + "<hr/> Decipherable text of the envelope: <br/> ";
            body = body + ocrText;
        }
        this.client = client;
        this.receipient = client.getEmail();

    }

    /**
     * @param client
     * @param file
     */
    public ScanGmailEnvelopeThread(Client client, String file, String id) {

        this.receipient = client.getEmail();
        this.client = client;
        this.id = id;
        //  this.body = OSIFileUtils.getFileContentsText("scan.html");
        this.body = ConfigHandler.getEntry("ScanHTMLLetter");
        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);

        this.subject = "Scanned mail at OSI. Mail Item #" + id;
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "In constructor for EmailIMageThread sending file: " + file);
        this.file = file;

    }

    /**
     * Sends an email to a client from a remotely inserted image
     *
     * @param mem_nmbr
     * @param id
     */
    public ScanGmailEnvelopeThread(int mem_nmbr, String id) {
        System.out.println("In constructor for EmailIMageThread id: " + id);
        this.id = id;
        this.client = ClientFactory.getClientByMemNum(mem_nmbr);
        this.receipient = client.getEmail();
        //  this.receipient = "joseph@osioffices.com";
        ClassLoader objClassLoader = getClass().getClassLoader();
        try {
            FileInputStream objFileInputStream = new FileInputStream(objClassLoader.getResource("scan.html").getFile());
            String file = objClassLoader.getResource("scan.html").getFile();
            // System.out.println("Found file: " + file);
            //   this.body = OSIFileUtils.getFileContentsText(file);
            this.body = ConfigHandler.getEntry("ScanHTMLLetter");
            objFileInputStream.close();
        } catch (Exception ex) {
            Logger.getLogger(ScanGmailEnvelopeThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);

        this.subject = "Scanned mail at OSI. Mail Item #" + id;
        this.file = MailItemFactory.writeMailItem(id);
        //    System.out.println("In constructor for EmailIMageThread sending body: " + body);
        //  this.file = file;

    }

    /**
     * @param client
     * @param file
     * @param id
     * @param subject
     */
    public ScanGmailEnvelopeThread(Client client, String file, String id, String subject) {

        this.receipient = client.getEmail();
        this.client = client;
        this.body = OSIFileUtils.getFileContentsText("scan.html");
        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);
        this.id = id;
        this.subject = subject;
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "In constructor for EmailIMageThread sending file: " + file);
        this.file = file;

    }

    private String getTermsOfService2() {
        String file = "toslink.html";
        String path = "resources//" + file;

        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String content = br.lines().reduce("", String::concat);
        return content;
    }

    private String getTermsOfService() {
        String link = "<font size=\"2\" style=\"font-size: 9pt\">By\n"
                + "clicking on the link and requesting OSI open and scan your mail you\n"
                + "agree to the <a href=\"https://osioffices.com/terms-of-service\">Terms\n"
                + "of Service</a></font></font></p>";

        return link;
    }

    /**
     * Sends a message to a given receipient. It defaults to the manager session
     *
     * @param receipient Receipent of email
     * @param body       Body of message
     * @param subject    Subject of message
     */
    public ScanGmailEnvelopeThread(String receipient, String body, String subject) {
        this.receipient = receipient;
        //    this.sender = ConfigHandler.getSMTPServerUserName();
        this.body = body;
        this.subject = subject;
        sendertype = SendGmail.MANAGERSESSION;

    }

    private void throwError(String header, String message) {
        Alert alertGlobal = this.scanState.getAlertGlobal();
        alertGlobal = new Alert(Alert.AlertType.ERROR);
        alertGlobal.setHeaderText(header);
        alertGlobal.setTitle(message);
        alertGlobal.showAndWait();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "showed an error message: " + header + ":" + message);
    }

    public ScanState getScanState() {
        return scanState;
    }

    public void setScanState(ScanState scanState) {
        this.scanState = scanState;
    }
}
