/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scanemail;

import org.apache.commons.lang.StringUtils;
import osi.email.EmailDispatcher;
import osi.email.SendGmail;
import osi.factory.ClientFactory;
import osi.factory.MailItemFactory;
import osi.model.Client;
import osi.model.DebugLog;
import osi.model.EmailSendResult;
import osi.model.OSITask;
import osi.util.ConfigHandler;
import osi.util.Format;
import osi.util.OSIFileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author josephpearce
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.


import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.mail.Transport;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.mail.internet.InternetAddress;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import java.util.logging.Level;
import java.util.logging.Logger;
import osi.factory.ClientFactory;
import osi.factory.ConfigEntryFactory;
import osi.factory.MailItemFactory;
import osi.model.Client;
import osi.model.Log;
import osi.model.OSITask;
import osi.util.Format;
import osi.util.OSIFileUtils;

/**
 *
 * @author Joseph Pearce
 */
public class ScanGmailEnvelope {

    //public final int MANAGERSESSION = 1;
    private int sendertype;
    private String receipient;
    //  private String sender;
    private String body;
    private String subject;
    private String file;
    private Client client;

    public EmailSendResult send() {
        EmailDispatcher emailDispatcher = new EmailDispatcher(EmailDispatcher.RECEPTIONIST);
        //   sGmail.setDeleteFilesAfterSend(true);
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Sending email to: " + receipient + " with file" + file);
        ArrayList<String> fileList = new ArrayList<>();
        boolean add = fileList.add(file);
        EmailSendResult eSend = emailDispatcher.sendMessage(receipient, body, subject, fileList);

        return eSend;
        // Log log2 = new Log(Log.SEVERITY_INFO, "Image-send", "sent image email to " + client.getFirmname() + " :N " + client.getMemnum(), body);
    }

    /**
     * Sends the document as well as the ocrText
     *
     * @param client
     * @param file
     * @param id
     * @param ocrText
     */
    public ScanGmailEnvelope(Client client, String file, String id, String optionalSubject, String ocrText) {
        //     System.out.println("In scan Email Envelope Thread with OCR text subject: " + optionalSubject);

        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In constructor");
        this.client = client;
        //  this.body = OSIFileUtils.getFileContentsText("scan.html");
        this.body = ConfigHandler.getEntry("ScanHTMLLetter");
        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);

        this.subject = "Scanned mail at OSI. Mail Item #" + id;
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
    public ScanGmailEnvelope(Client client, String file, String id) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In  3 field constructor");

        this.receipient = client.getEmail();
        this.client = client;
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
    public ScanGmailEnvelope(int mem_nmbr, String id) {
        //  System.out.println("In constructor for EmailIMageThread id: " + id);
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In 2 field constructor");

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
            Logger.getLogger(ScanGmailEnvelope.class.getName()).log(Level.SEVERE, null, ex);
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
    public ScanGmailEnvelope(Client client, String file, String id, String subject) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In 4 field constructor");

        this.receipient = client.getEmail();
        this.client = client;
        this.body = OSIFileUtils.getFileContentsText("scan.html");
        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);

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
    public ScanGmailEnvelope(String receipient, String body, String subject) {
        this.receipient = receipient;
        //    this.sender = ConfigHandler.getSMTPServerUserName();
        this.body = body;
        this.subject = subject;
    //    sendertype = SendGmail.MANAGERSESSION;

    }

}
