/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javafx.scene.control.Alert;
import osi.factory.MailItemFactory;
import osi.model.Client;
import osi.model.Log;
import osi.model.MailItem;

/**
 * creationdate Dec 8, 2015 3:21:33 PM
 *
 * @author joseph pearce
 */
public class MailItemInsertNoThread {

    private Client client;
    private String filename;
    private boolean envelope;
    private int numpages;
    private String filenopathname;
    private String id;
    private String parentid;

    MailItemInsertNoThread(Client client, String filename, boolean envelope, int numpages, String filenopathname, String id, String parentid) {

        this.client = client;
        this.filename = filename;
        this.id = id;
        this.envelope = envelope;
        this.numpages = numpages;
        this.filenopathname = filenopathname;
        this.parentid = parentid;

    }

    public void insertItem() throws Exception {
        
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
        
    }
}
