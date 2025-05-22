/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scan;

import javafx.scene.control.Alert;
import osi.model.Client;

/**
 * Represents a class used for multi tasking in JavaFX. It has all the
 * parameters to work in the background.
 *
 * @author josephpearce
 */
public class ScanState {

    private Client client;
    private String id;
    private String parentid;

    private boolean scanduplex;
    private boolean largeenvelope;
    private boolean scancheck;
    private boolean contentcheck;
    private String optionalsubject;
    private boolean docupload;
    private Alert alertGlobal;


    /**
     * @return the client
     */
    public Client getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the parentid
     */
    public String getParentid() {
        return parentid;
    }

    /**
     * @param parentid the parentid to set
     */
    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    /**
     * @return the sc
     */


    /**
     * @return the scanduplex
     */
    public boolean isScanduplex() {
        return scanduplex;
    }

    /**
     * @param scanduplex the scanduplex to set
     */
    public void setScanduplex(boolean scanduplex) {
        this.scanduplex = scanduplex;
    }

    /**
     * @return the largeenvelope
     */
    public boolean isLargeenvelope() {
        return largeenvelope;
    }

    /**
     * @param largeenvelope the largeenvelope to set
     */
    public void setLargeenvelope(boolean largeenvelope) {
        this.largeenvelope = largeenvelope;
    }

    /**
     * @return the scancheck
     */
    public boolean isScancheck() {
        return scancheck;
    }

    /**
     * @param scancheck the scancheck to set
     */
    public void setScancheck(boolean scancheck) {
        this.scancheck = scancheck;
    }

    /**
     * @return the contentcheck
     */
    public boolean isContentcheck() {
        return contentcheck;
    }

    /**
     * @param contentcheck the contentcheck to set
     */
    public void setContentcheck(boolean contentcheck) {
        this.contentcheck = contentcheck;
    }

    /**
     * @return the optionalsubject
     */
    public String getOptionalsubject() {
        return optionalsubject;
    }

    /**
     * @param optionalsubject the optionalsubject to set
     */
    public void setOptionalsubject(String optionalsubject) {
        this.optionalsubject = optionalsubject;
    }

    /**
     * @return the docupload
     */
    public boolean isDocupload() {
        return docupload;
    }

    /**
     * @param docupload the docupload to set
     */
    public void setDocupload(boolean docupload) {
        this.docupload = docupload;
    }

    public Alert getAlertGlobal() {
        return alertGlobal;
    }

    public void setAlertGlobal(Alert alertGlobal) {
        this.alertGlobal = alertGlobal;
    }
}
