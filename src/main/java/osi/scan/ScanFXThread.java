/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scan;

import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import osi.Exception.ScanException;
import osi.model.Client;
import osi.model.DebugLog;
import osi.model.EmailSendResult;
import osi.model.Usage;
import osi.scanemail.ScanGmailEnvelope;
import osi.utilities.AfterScan;
import osi.utilities.MailItemInsert;
import osi.utilities.ScanUtilities;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

/**
 * Handles the background running of a scan operation;
 *
 * @author josephpearce
 */
public class ScanFXThread extends Task<ScanState> {

    private final ScanState scanState;

    private ScanShellHelper scanShellHelper;
    private final boolean scanduplex;
    private final boolean largeenvelope;
    private final boolean scancheck;
    private final boolean contentcheck;


    private final String optionalsubject;
    private final Client client;
    private final String tmpDir;
    private final String id;
    private String parentid;
    private final boolean docupload;

    private String statusMessages;
    private String errorMessages;
    private String scanMethod;


    public ScanFXThread(ScanState scanState) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "ScanFXThread constructor for shell implementation");
        this.scanShellHelper = new ScanShellHelper(scanState);
        this.scanState = scanState;
        this.scanduplex = scanState.isScanduplex();
        this.largeenvelope = scanState.isLargeenvelope();
        this.scancheck = scanState.isScancheck();
        this.contentcheck = scanState.isContentcheck();
        this.optionalsubject = scanState.getOptionalsubject();
        this.client = scanState.getClient();
        this.docupload = scanState.isDocupload();
        this.errorMessages = "";
        this.statusMessages = "";
        tmpDir = CacheData.getTempDirectory();
        id = scanState.getId();
        parentid = scanState.getParentid();


        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "In ScanFXThread constructor with client: " + client.getEmail() + " with optional subject: " + optionalsubject + " method: " + scanMethod);

    }

    /**
     * Shows a message to the user of an error
     *
     * @param message
     */
    private void updateErrors(String message) {
//if no error show id and name
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Trying to show error: " + message);

        if (errorMessages.isEmpty()) {
            errorMessages = "id#:" + id + " : " + client.getFirmname() + "\n";
        }

        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Trying to show error :" + errorMessages);
        errorMessages = errorMessages + message + "\n";
        updateMessage(errorMessages);

        //   DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "showed an error message: " + header + ":" + message);
    }

    private void updateStatus(String message) {

        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Updating status with: " + message);
        //  statusMessages = statusMessages + message + Format.getLineBreak();
        statusMessages = statusMessages + message + "\n";
        updateMessage(statusMessages);
    }

    /**
     * Executes the scan process for a given client and mail item.
     *
     * @return the state of the scan process
     * @throws Exception if an error occurs during the scan process
     */
    @Override
    protected ScanState call() throws Exception {
        updateStatus("ID: " + id + " for client: " + client.getFirmname());

        updateStatus("Starting scan to: " + client.getFirmname());
        //  updateErrors("error text");
        long start = System.currentTimeMillis();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In call method ");
        String finalname = "";
        String fileend = "";
        Usage scanUsage = new Usage(client, true);
        if (contentcheck) {
            this.handleContentChecked(scanUsage);
        } else {
            this.handleSurfaceChecked(scanUsage);

        }

        long totalend = System.currentTimeMillis();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Total time in ScanFxThread:  " + (totalend - start));
        return scanState;
    }

    private void handleSurfaceChecked(Usage scanUsage) {
        long start = System.currentTimeMillis();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Only surface is checked for scan");
        String subject = "Scanned mail at OSI. Mail Item: #" + id;
        String customTitle = "";
        try {
            if (!StringUtils.isEmpty(optionalsubject) && optionalsubject.trim().length() > 3) {
                subject = optionalsubject;
            }
        } catch (Exception e) {
            //  e.printStackTrace();
        }

        String lettername = ScanUtilities.getFlatScanFileName(client, id);
        String letterfilename = ScanUtilities.getFlatScanFullPathFileName(client, id);

        String performOCRResult = "";
        try {

            scanShellHelper.setLargeenvelope(largeenvelope);
            letterfilename = scanShellHelper.surfaceScan(id);


            updateStatus("Finished scanning the surface");
            long end5 = System.currentTimeMillis();
            final String file = letterfilename;
            final String finalsubject = subject;
            new Thread(() -> {
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Time to do surface scan:" + (end5 - start));

                String performOCRResult1 = ScanUtilities.performOCR(file);
                long end6 = System.currentTimeMillis();
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Time to do OCR of surface scan:" + (end6 - end5));
                long end7 = System.currentTimeMillis();
                //    throwError("test " + client.getFirmname(), "test2");
                MailItemInsert mT = new MailItemInsert(client, file, true, 1, lettername, id, parentid);
                try {
                    mT.process();
                    scanUsage.setEnvscan(1);
                    scanUsage.save();
                } catch (Exception ex) {
                    updateErrors("Error trying to insert the document into the database: " + client.getFirmname() + " : " + ex.getLocalizedMessage());

                    DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Error in document insert", ex);
                    try {
                        throw new ScanException("Error doing a surface scan for: " + client.getFirmname() + ex.getLocalizedMessage());
                    } catch (ScanException e) {
                        throw new RuntimeException(e);
                    }


                }
                long end8 = System.currentTimeMillis();
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Time to do OCR of surface scan:" + (end8 - end7));
                ScanGmailEnvelope iT = new ScanGmailEnvelope(client, file, id, finalsubject, performOCRResult);
                try {

                    EmailSendResult emailSendResult = iT.send();

                    if (emailSendResult.isSuccess()) {
                        String googleid = "[googlemailid:" + emailSendResult.getEmailId() + "]";
                        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Google id: " + googleid);
                        ScanUtilities.saveSucessEmailSent(id, "success " + googleid);

                    } else {
                        ScanUtilities.saveFailedEmailSent(id, emailSendResult.getErrorMessage());
                        throw new ScanException("Error sending email to : " + client.getFirmname() + " " + emailSendResult.getErrorMessage());

                    }

                    //    updateStatus("Sent scan to: " + client.getFirmname());
                } catch (Exception e) {
                    ScanUtilities.saveFailedEmailSent(id, e.getMessage());
                    updateStatus("Error sending email to: " + client.getFirmname() + " : " + e.getLocalizedMessage());

                    DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Error sending email to client" + client.getFirmname(), e);
                    e.printStackTrace();
                    try {
                        throw new ScanException("Error sending email to: " + client.getFirmname() + " : " + e.getLocalizedMessage());
                    } catch (ScanException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }).start();
            long end9 = System.currentTimeMillis();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Total after scan time :" + (end9 - end5));
        } catch (Exception ex) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "In ScanFXThread, no content  scan, envelope name: " + letterfilename, ex);
            updateStatus("ERROR PROCESSING SCAN: " + client.getFirmname() + " : " + ex.getLocalizedMessage());
            ex.printStackTrace();
            try {
                throw new ScanException("ERROR PROCESSING SCAN: " + client.getFirmname() + " : " + ex.getLocalizedMessage());
            } catch (ScanException ex1) {
                throw new RuntimeException(ex);
            }
        }

    }

    private void handleContentChecked(Usage scanUsage) throws ScanException {
        int numberofpages = 0;
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Content is checked for scan");
        String subject = "Scanned mail & contents at OSI";
        String customTitle = "";
        if (!StringUtils.isEmpty(optionalsubject) && optionalsubject.trim().length() > 3) {
            subject = customTitle;
        }

        String lettername = ScanUtilities.getFlatScanFileName(client, id);
        String letterfilename = ScanUtilities.getFlatScanFullPathFileName(client, id);

        String envOCR = "";
        try {

            scanShellHelper.setDuplex(scanduplex);
            scanShellHelper.setLargeenvelope(largeenvelope);

            letterfilename = scanShellHelper.surfaceScan(id);


            updateStatus("finished surface scan for: " + client.getFirmname());
            long end1 = System.currentTimeMillis();
            //  DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Time to do surface scan:" + (end1 - start));


            parentid = id;
            String pdfname = ScanUtilities.getAdfFullPathScanFileName(client, id);
            String docname = ScanUtilities.getAdfScanFileName(client, id);
            String ocrText = "";
            ArrayList<String> batchFiles = new ArrayList<String>();

            try {

                updateStatus("Starting shell content scan to: " + client.getFirmname());
                batchFiles = scanShellHelper.batchScanWithID(id);
                numberofpages = batchFiles.size();
                updateStatus("Finished contact scan to: " + client.getFirmname() + " # pages:" + batchFiles.size());
            } catch (Exception ex) {
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Error in batch scan", ex);
                updateErrors("Error doing a batch scan: " + client.getFirmname() + " : " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }


            long end2 = System.currentTimeMillis();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Time to do batch scan:" + (end2 - end1));


            try {
                MailItemInsert mT = new MailItemInsert(client, letterfilename, true, numberofpages, lettername, id, parentid);
                mT.process();

            } catch (Exception ex) {
                updateErrors("Error trying to insert the document into the database: " + client.getFirmname() + " " + ex.getLocalizedMessage());
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Error in document insert", ex);
                ex.printStackTrace();
                try {
                    throw new ScanException("Error trying to insert the document into the database: " + client.getFirmname() + " " + ex.getLocalizedMessage());
                } catch (ScanException ex1) {
                    throw new RuntimeException(ex);
                }
            }
            long end3 = System.currentTimeMillis();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Time to insert into database:" + (end3 - end2));

            subject = subject + " ID#" + id;
            AfterScan afterScan = new AfterScan(tmpDir, client, letterfilename, pdfname, docname, batchFiles, id, parentid, subject);
            //    afterScan.setOcrText();
            try {
                updateStatus("inserting into the database, emailing and running ocr to: " + client.getFirmname());
                //  afterScan.process();
                afterScan.start();
                //  updateStatus(" finished inserting into the database, emailing and running ocr to: " + client.getFirmname());
            } catch (Exception e) {
                updateErrors("Error processesing scan: " + client.getFirmname() + " : " + e.getLocalizedMessage());
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Error sending scan to client" + client.getFirmname(), e);
                e.printStackTrace();

            }
            long end4 = System.currentTimeMillis();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Time to do after scan processing:" + (end4 - end3));

            scanUsage.setPagesscan(batchFiles.size());
            scanUsage.save();

        } catch (Exception ex) {
            // Logger.getLogger(ScanFXThread.class.getName()).log(Level.SEVERE, null, ex);
            updateErrors("Error doing a surface scan for: " + client.getFirmname() + ex.getLocalizedMessage());
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Error in surface scan", ex);
            throw new ScanException("Error doing a surface scan for: " + client.getFirmname() + ex.getLocalizedMessage());
        }


    }


}



