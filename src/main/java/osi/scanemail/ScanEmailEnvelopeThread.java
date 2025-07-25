/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scanemail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;
import osi.email.SendGmail;
import osi.factory.ClientFactory;
import osi.factory.ConfigEntryFactory;
import osi.factory.MailItemFactory;
import osi.model.Client;
import osi.model.DebugLog;
import osi.model.Log;
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


public class ScanEmailEnvelopeThread extends Thread {

    //public final int MANAGERSESSION = 1;
    private int sendertype;
    private String receipient;
    //  private String sender;
    private String body;
    private String subject;
    private String file;
    private Client client;

    public void run() {
        try {
//
            ArrayList<String> list = new ArrayList<String>();
            list.add(file);
            SendGmail sendGmail=new SendGmail(SendGmail.RECEPTIONIST);
            sendGmail.sendMessageAR(receipient,body,subject,list);

            Log log2 = new Log(Log.SEVERITY_INFO, "Image-send", "sent image email to " + client.getFirmname() + " :N " + client.getMemnum(), body);

        } catch (Exception e) {
            // osi.OSIException osiexp = new osi.OSIException(e, "Unable to send message");
//            FacesContext context = FacesContext.getCurrentInstance();
//            context.addMessage(null, new FacesMessage("Email Error", e.getLocalizedMessage()));
            Log log = new Log(Log.SEVERITY_ERROR, "image-failure", "error sending EmailEnvelopeImageThread:  ", e);

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
    public ScanEmailEnvelopeThread(Client client, String file, String id, String optionalSubject, String ocrText) {
        System.out.println("In scan Email Envelope Thread with OCR text subject: " + optionalSubject);
        this.client = client;
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
     *
     * @param client
     * @param file
     */
    public ScanEmailEnvelopeThread(Client client, String file, String id) {

        this.receipient = client.getEmail();
        this.client = client;
        //  this.body = OSIFileUtils.getFileContentsText("scan.html");
        this.body = ConfigHandler.getEntry("ScanHTMLLetter");
        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);

        this.subject = "Scanned mail at OSI. Mail Item #" + id;
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8,"In constructor for EmailIMageThread sending file: " + file);
        this.file = file;

    }

    /**
     * Sends an email to a client from a remotely inserted image
     *
     * @param mem_nmbr
     * @param id
     */
    public ScanEmailEnvelopeThread(int mem_nmbr, String id) {
        System.out.println("In constructor for EmailIMageThread id: " + id);

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
            Logger.getLogger(ScanEmailEnvelopeThread.class.getName()).log(Level.SEVERE, null, ex);
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
     *
     * @param client
     * @param file
     * @param id
     * @param subject
     */
    public ScanEmailEnvelopeThread(Client client, String file, String id, String subject) {

        this.receipient = client.getEmail();
        this.client = client;
        this.body = OSIFileUtils.getFileContentsText("scan.html");
        String url = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSCAN + "&uid=" + id;
        String urlshred = "https://osiitservices.com/osidashboard/pub/noti.xhtml?memnum=" + client.getMemnum() + "&action=" + OSITask.ACTIONREQUESTSHRED + "&uid=" + id;

        body = Format.replaceText(body, "SCANURLXXXXX", url);
        body = Format.replaceText(body, "SHREDURLXXXXX", urlshred);

        this.subject = subject;
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8,"In constructor for EmailIMageThread sending file: " + file);
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
     * @param body Body of message
     * @param subject Subject of message
     */
    public ScanEmailEnvelopeThread(String receipient, String body, String subject) {
        this.receipient = receipient;
        //    this.sender = ConfigHandler.getSMTPServerUserName();
        this.body = body;
        this.subject = subject;
        sendertype = SendGmail.RECEPTIONIST;

    }

}
