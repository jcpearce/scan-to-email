package osi.scan;

import osi.model.DebugLog;
import osi.utilities.ImageAnalysis;
import osi.utilities.ScanUtilities;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The ScanShellHelper class provides utility methods to perform scanning operations using a shell command.
 */
public class ScanShellHelper {

    private int numberofpages;   // number of pages that have been scanned for billing purposes
    private boolean duplex = false;
    private boolean largeenvelope = false;
    private String shellScanCommand = "naps-scan.sh";
    //  private String shellScanCommand = "naps-scan.bat";
    private ScanState scanState;
    private static String tmpDir;

    public static void main(String[] args) {
        ScanState scanState1 = new ScanState();

        ScanShellHelper shellHelper = new ScanShellHelper(scanState1);
       // ScanUtilities.showErrorToWindows("error message test");
        // String currentWorkingDir = System.getProperty("user.dir");
        // String filePath = currentWorkingDir + File.separator + "/tempimages/";
        // shellHelper.getScannedFiles(filePath,"testid");
        try {
            // String simpleScan = shellHelper.surfaceScan("test.jpeg");

            ArrayList<String> testFiles = shellHelper.batchScanWithID("testid" + System.currentTimeMillis());
        } catch (Exception ex) {
            Logger.getLogger(ScanShellHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param id
     * @return
     * @throws Exception
     */
    public ArrayList<String> batchScanWithID(String id) throws Exception {
        return runScanningScript(id, true, this.duplex);

    }

    public ScanShellHelper() {
        tmpDir = CacheData.getTempDirectory();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In constructor without scan state");

    }

    public ScanShellHelper(ScanState scanState) {
        this.scanState = scanState;
        tmpDir = CacheData.getTempDirectory();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In constructor");
    }

    /**
     * Performs a simple scan of the specified finalname.
     *
     * @param finalname The name of the file to scan.
     * @return The name of the scanned file.
     * @throws Exception If an error occurs during the scan.
     */
    public String surfaceScan(String finalname) throws Exception {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(),DebugLog.INFO,"Calling surface scan for file: " + finalname);
      //  ScanUtilities.showErrorToWindows("error message test");
        List<String> strings = this.runScanningScript(finalname, false, false);
        String returnedFile = "";
        boolean isenvelope=false;
        if (strings.size() == 0) {
            throw new Exception("No files returned from scan");
        } else {
            returnedFile = strings.get(0);
            try {
                 isenvelope = ImageAnalysis.analyzeImageForBeingLetterSized(returnedFile);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "shell surface scanning finished returned file:" + returnedFile+" is it a small envelope: "+isenvelope);

            if (!isenvelope) {

            } else {
                DebugLog.logEvent(MethodHandles.lookup().lookupClass(),DebugLog.INFO,"This is a letter envelope croping and rotating");
                // if this is an envelope we need to crop and rotate
                ImageResizer.rotateAndCropEnvelopeImage(returnedFile);

                System.out.println("Finished resizing and cropping image:" + returnedFile);
            }
        }
        return returnedFile;
    }


    /**
     * Executes a  script for scanning files.
     *
     * @param id         The prefix to use for the file names of the scanned files.
     * @param adfMode    The scan mode to use/"
     * @param duplexMode The duplex mode to use. # Valid options are "Duplex" or "Simplex"
     * @return A list of scanned file names.
     * @throws IOException          If an I/O error occurs during script execution.
     * @throws InterruptedException If the script execution is interrupted.
     */
    private ArrayList<String> runScanningScript(String id, boolean adfMode, boolean duplexMode) throws IOException, InterruptedException {
        //   String filename = tmpDir + id + "-pagenum" + (pageNo++) + ".jpg";

        ArrayList<String> scannedFiles = new ArrayList<>();
        String currentWorkingDir = System.getProperty("user.dir");
        String[] commands = null;
        String scriptPath = currentWorkingDir + File.separator + this.shellScanCommand; // Assuming the script is in the current working directory

        if (adfMode == false) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Shell scanning just surface");
            // Just scan the surface
            String profileName = "main";
            commands = new String[]{scriptPath,  "glass", "color",id + ".jpg"};


        } else {
            //naps2.console.exe -d "Your Scanner Name" --adf --output "C:\path\to\save\page_%d.jpg" --image-output jpeg
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Shell scanning with ADF");
            if (duplexMode) {
                commands = new String[]{scriptPath,  "duplex", "color", id + ".jpg"};
            } else {
                commands = new String[]{scriptPath,  "feeder", "color", id + ".jpg"};
            }

        }
        try {

            scannedFiles = this.runProcess(commands, id);
        } catch (Exception e) {
            e.printStackTrace();
        }


        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, " Shell scanned files: " + scannedFiles);
        return scannedFiles;
    }

    /**
     * Returns a list of file paths in the specified directory that match the given id. The files are sorted by ascending last modified date.
     *
     * @param directoryPath The path of the directory to scan.
     * @param id            The prefix to use for the file names of the scanned files.
     * @return An ArrayList of file paths that match the id, sorted by last modified date.
     */
    private ArrayList<String> getScannedFiles(String directoryPath, String id) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Getting files in: " + directoryPath + " for id:" + id);
        File dir = new File(directoryPath);
        ArrayList<String> filePaths = new ArrayList<>();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(id); // Modify this to match your desired pattern
            }
        });

        if (files != null) {
            List<File> fileList = new ArrayList<>(Arrays.asList(files));


            // Sort the list by ascending date (last modified)
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            });

            // Output the sorted list
            for (File file : fileList) {
                System.out.println("Adding to array" + file.getAbsolutePath());
                filePaths.add(file.getAbsolutePath());
            }
        }
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Returning # of files:" + filePaths.size());
        return filePaths;
    }

    private ArrayList<String> runProcess(String[] commands, String id) throws IOException, InterruptedException {
        String currentWorkingDir = System.getProperty("user.dir");
        String filePath = currentWorkingDir + File.separator + "/tempimages/";
        ArrayList<String> scannedFiles = new ArrayList<>();
        System.out.println("------------Executing command:");
        for (String part : commands) {
            System.out.print(part + " ");
        }
        System.out.println("-----------------");


        // Start the process
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        Process process = processBuilder.start();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));


        // Read output from the script
        // Read command standard output
        String s;
        System.out.println("Standard output: ");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        System.out.println("----------");
        // Read command errors
        System.out.println("Standard error: ");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
        System.out.println("----------");
        int exitCode = process.waitFor();
        System.out.println("Exited with code: " + exitCode);
        if (exitCode == 0) {

        }

        return this.getScannedFiles(filePath, id);
    }

    private Process executeCommands(String commands, String param1) {
        Process process = null;
        //   param1 = "test.jpeg";
        System.out.println("Calling command: " + commands + " with param: " + param1);
        ProcessBuilder pb = new ProcessBuilder(commands, param1);
        //  pb.directory(new File("/Users/josephpearce/")); //Set current directory
        pb.redirectError(new File("/tmp/scan.err")); //Log errors in specified log file.
        try {
            process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    /**
     * @param tempfile
     * @return a PDF file of the scanned image
     * @throws Exception
     */
    public String batchScan(String tempfile) throws Exception {

        return tempfile;
    }


    /**
     * @return the duplex
     */
    public boolean isDuplex() {
        return duplex;
    }

    /**
     * @param duplex the duplex to set
     */
    public void setDuplex(boolean duplex) {
        this.duplex = duplex;
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

}
