/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package osi.utilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import osi.database.DBFactory;
import osi.factory.EventFactory;
import osi.model.Client;
import osi.model.DebugLog;
import osi.scan.CacheData;
import osi.util.Format;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Various helper functions. As multiple implementations may happen, this holds
 * some of the common functions
 *
 * @author josephpearce
 */
public class ScanUtilities {
    public static void main(String[] args) {

        ScanUtilities.saveFailedEmailSent("100191662051916224","test fail note");
    }

    /**
     * Returns just the name of the file;
     *
     * @param client
     * @param id
     * @return
     */
    public static String getFlatScanFileName(Client client, String id) {
        String filename = "";
        String fileend = ".jpeg";
        //  finalname = "Envelope ";
        java.text.SimpleDateFormat formatter1 = new java.text.SimpleDateFormat("MM-dd-yy hh.mm a ");
        String format1 = formatter1.format(new Date(System.currentTimeMillis()));
        //   finalname = finalname + " " + format + " ID#" + id + fileend;
        // For some reason some of the file names where off by a minute 
        String lettername = format1 + "-ID#" + id + fileend;

        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 5, "flat scan starting letter scan: " + lettername);

        //  String finalname = System.getProperty("user.dir") + "/tempimages/" + lettername;
        //System.out.println("Returning flat scan filename: " + lettername);
        return lettername;

    }
    public static void saveSucessEmailSent(String id,  String emailnote) {
        ScanUtilities.saveSucessEmailSent(id, new Date(System.currentTimeMillis()),emailnote);

    }
    private static String getMachineIP() {
        String ip="";
        String hostname="";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    ip=inetAddress.getHostAddress();
                    String host=  inetAddress.getHostName();
                  //.getHostName()
                 //  System.out.println("host: " + host+" ip:"+ip);
                    if (host.contains("recep")) {
                     hostname=host;
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(),DebugLog.INFO,"Returning host: ip: "+hostname+" : "+ip);
        return hostname;
    }

    public static void saveSucessEmailSent(String id, Date senddate, String emailnote) {

        System.out.println("Updating success sent for id:"+id+":"+senddate+" "+emailnote);
        String sql = "update clientclouddocs set emaildate=?, emailnote=? where id=? or parentid=?";
        try (Connection con = DBFactory.getConnection();
             PreparedStatement pS5 = con.prepareStatement(sql)) {
            pS5.setTimestamp(1, Format.convToTimeStamp(senddate));
            pS5.setString(2, emailnote+" : "+ScanUtilities.getMachineIP());
            pS5.setString(3, id );
            pS5.setString(4, id );
            int executeUpdate = pS5.executeUpdate();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Inserted success email send for id:" + executeUpdate);
        } catch (Exception ex) {
            Logger.getLogger(EventFactory.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    public static void saveFailedEmailSent(String id,  String emailnote) {

     //   System.out.println("Updating success sent for id:"+id+":"+senddate+" "+emailnote);
        String sql = "update clientclouddocs set  emailnote=? where id=? or parentid=?";
        try (Connection con = DBFactory.getConnection();
             PreparedStatement pS5 = con.prepareStatement(sql)) {

            pS5.setString(1, "FAILURE:"+emailnote+" : "+ScanUtilities.getMachineIP());
            pS5.setString(2, id );
            pS5.setString(3, id );
            int executeUpdate = pS5.executeUpdate();
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Inserted failed email send for id:"+id+" note:"+emailnote+":" + executeUpdate);
        } catch (Exception ex) {
            Logger.getLogger(EventFactory.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    /**
     * Returns the full path of the file
     *
     * @param client
     * @param id
     * @return
     */
    public static String getFlatScanFullPathFileName(Client client, String id) {

        String finalname = System.getProperty("user.dir") + "/tempimages/" + ScanUtilities.getFlatScanFileName(client, id);
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Returning flat scan full path filename: " + finalname);
        return finalname;

    }

    public static String getAdfScanFileName(Client client, String id) {
        String parentid = id;
        String fileend = ".pdf";
        String finalname = "Content ";
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MM-dd-yy hh.mm a ");
        String format2 = formatter.format(new Date(System.currentTimeMillis()));
        String docname = format2 + "-ID#" + id + fileend;

        String pdfname = docname;

        return pdfname;

    }

    public static String getAdfFullPathScanFileName(Client client, String id) {
        String finalname = System.getProperty("user.dir") + "/tempimages/" + ScanUtilities.getAdfScanFileName(client, id);
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Returning ADF scan full path filename: " + finalname);
        return finalname;

    }

    /**
     * Utility method to write files to temp directory
     *
     * @param filename
     * @param content
     */
    public static void writeTempFile(String filename, String content) {
        String file = CacheData.getTempDirectory() + filename;
        CacheData.getTempDirectory();
        try {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "writing file: " + file);
            FileUtils.writeStringToFile(new File(file), content);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
            //  e.printStackTrace();
        }
    }

    public static void deleteFilesOlderThan(String directoryPath, int daysOld) throws IOException {
        Path dirPath = Paths.get(directoryPath);

        if (!Files.isDirectory(dirPath)) {
            throw new IOException("The provided path is not a directory.");
        }

        Instant threshold = Instant.now().minus(daysOld, ChronoUnit.DAYS);

        try {
            Files.list(dirPath)
                    .filter(path -> isFileOlderThan(path, threshold))
                    .forEach(path -> deleteFile(path));
        } catch (IOException e) {
            throw new IOException("An error occurred while listing files in the directory.", e);
        }
    }

    private static boolean isFileOlderThan(Path path, Instant threshold) {
        try {
            return Files.getLastModifiedTime(path).toInstant().isBefore(threshold);
        } catch (IOException e) {
            System.err.println("Error getting last modified time of file " + path + ": " + e.getMessage());
            return false;
        }
    }

    private static void deleteFile(Path path) {
        try {
            Files.delete(path);
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Deleted file: " + path);
        } catch (IOException e) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Error deleting file " + path + ": " + e.getMessage(), e);

            // System.err.println("Error deleting file " + path + ": " + e.getMessage());
        }
    }

    /**
     * Runs the command call to scan an image
     *
     * @param fullpathnameImageFile
     * @return The text found in am image the OCR
     */
    public static String performOCR(String fullpathnameImageFile) {
        // System.out.println("Performing OCR on file: " + fullpathnameImageFile);
        String launchDirectory = System.getProperty("user.dir");
        String os = System.getProperty("os.name");
        String command = "";
        //System.out.println("Checking os: " + os);
        boolean windows = false;
        if (StringUtils.contains(os, "Linux")) {
            windows = false;
            command = launchDirectory + "/unix-ocr-to-txt.sh";
        } else {
            windows = true;
            command = launchDirectory + "/win-ocr-file-to-txt.bat";
        }
        Process p = ScanUtilities.executeCommands(command, fullpathnameImageFile);
        String textReturned = "";
        String total = "";
        try {
            BufferedReader input
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int i = 0;
            while ((line = input.readLine()) != null) {
                // textReturned = textReturned + line;
                if (i > 1) {
                    total = total + line.toString() + Format.getLineBreak();
                }
                //  System.out.println("line" + i + " :" + line);
                i++;
                //    System.out.println(" textline3: ' " + total+" '");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  System.out.println("Returning text: " + total);

        return total;
    }

    private static Process executeCommands(String commands, String param1) {
        Process process = null;
        // param1 = "test.jpeg";
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 5, "Calling scan utilities command: " + commands + " with param: " + param1);
        ProcessBuilder pb = new ProcessBuilder(commands, param1);
        String launchDirectory = System.getProperty("user.dir");
        //  pb.directory(new File("/Users/josephpearce/")); //Set current directory
        pb.redirectError(new File(launchDirectory + "/scan-utilities.err")); //Log errors in specified log file.
        try {
            process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

}
