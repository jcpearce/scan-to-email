/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.utilities;

import osi.factory.MailItemFactory;
import osi.model.Client;
import osi.model.DebugLog;
import osi.model.Log;
import osi.model.MailItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

/**
 * creationdate Dec 8, 2015 3:21:33 PM
 *
 * @author joseph pearce
 */
public class MailItemInsertThread extends Thread {

    private Client client;
    private String filename;
    private boolean envelope;
    private int numpages;
    private String filenopathname;
    private String id;
    private String parentid;

    public MailItemInsertThread(Client client, String filename, boolean envelope, int numpages, String filenopathname, String id, String parentid) {

        DebugLog.logEvent(MethodHandles.lookup().lookupClass(),8,"Trying to insert a file into the database: "+filename+" : "+filenopathname);
        this.client = client;
        this.filename = filename;
        this.id = id;
        this.envelope = envelope;
        this.numpages = numpages;
        this.filenopathname = filenopathname;
        this.parentid = parentid;

    }

    public void run() {
        try {
            MailItem mI = new MailItem();

            mI.setDocname(filenopathname);
            mI.setMemnmbr(client.getMemnum());
            mI.setPages(numpages);
            mI.setParentid(parentid);
            mI.setId(id);
            if (envelope) {
                InputStream fis2 = new FileInputStream(filename);
                File file = new File(filename);
                mI.setSurfacePic(fis2, file.length());
                mI.setType(MailItem.ENVELOPE);
                mI.setStoragetype(MailItem.JPG);
                //    System.out.println("Setting envelope of type: " + mI.getStoragetype());

            } else {
                InputStream fis2 = new FileInputStream(filename);
                File file = new File(filename);
                mI.setSurfacePic(fis2, file.length());
                mI.setType(MailItem.CONTENT);
                mI.setStoragetype(MailItem.PDF);
            }

            MailItemFactory.insertMailItem(mI);

            //     System.out.println("returnString from upload: " + uploadFile + " try delete " + delete + " " + filename);
        } catch (Exception e) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Problem inserting the scanned mail " ,e);

           // Alert alertnote = new Alert(Alert.AlertType.ERROR);
           // alertnote.setTitle("Problem scanning mail");
           // alertnote.setHeaderText("An error was encountered. ");
           // alertnote.setContentText(e.getLocalizedMessage());

            //alertnote.show();

            Log log = new Log(Log.SEVERITY_ERROR, "OSISCAN", "Error in MailInsertThread", e);
            e.printStackTrace();
        }
    }
}
