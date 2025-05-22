package osi.Exception;

import osi.model.DebugLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

public class ScanException extends Exception {


    public ScanException(String message) {
        super(message);
        // Call the method to execute the command line process when this exception is instantiated
        executeShowWarning(message);
    }

    private void executeShowWarning(String message) {
        try {
            String currentWorkingDir = System.getProperty("user.dir");
            String script = currentWorkingDir + File.separator+  "show-warning.bat \""+message+"\"";
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(),DebugLog.ERROR,"sending to vbs error: "+script);
            Runtime.getRuntime().exec(script);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}