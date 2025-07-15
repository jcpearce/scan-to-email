/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scan;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import osi.model.DebugLog;
import osi.util.ConfigHandler;

import java.lang.invoke.MethodHandles;
import java.util.Date;

/**
 * @author Joseph Pearce <joseph.pearce at your osioffices.com>
 */
public class OSIScan extends Application {

    private static HostServices hostServices;
    private String version = "3.81";
    private static boolean debug = false;

    @Override
    public void start(Stage stage) throws Exception {
        hostServices = getHostServices();
        // ArrayList<Client> allClients = ClientFactory.getAllClients();
        //   Path directory = Paths.get(System.getProperty("java.io.tmpdir"));
        //  System.out.println("TRYING TO DELETE FILES");
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Starting up OSIScan V:" + version + ":" + new Date(System.currentTimeMillis()));

        CacheData.init();
        // this.clearOldFiles(4);
        // System.err.println("Starting up OSIScan  V 1.3:" + new Date(System.currentTimeMillis()));
        Inner_Start iS = new Inner_Start();
        iS.start();
        try {
            //  Thread.sleep(2000);

        } catch (Exception e) {

        }

        //     FXMLLoader fxmlLoader = new FXMLLoader(OSIScan.class.getResource("FXMLDocument.fxml"));

        Parent root = FXMLLoader.load(OSIScan.class.getResource("FXMLDocument.fxml"));

        Scene scene = new Scene(root);
        //  scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        stage.setScene(scene);
        stage.getProperties().put("hostServices", hostServices);
        // System.out.println("Wrote local file");

        version = version + "SHL";

        String deb = ConfigHandler.getEntry("DebugScanApplication");

        if (deb.contains("yes")) {
            debug = true;
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Setting scan application to debug");
        } else {

        }
        stage.setTitle("OSI Scan and email application V" + version);

        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //  System.out.println(System.getProperty("java.io.tmpdir"));
        launch(args);
    }

    private void clearOldFiles(int numberDaysOld) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Clearing from temp directory files that are more than: " + numberDaysOld + " days old");
        try {
            //  OSIScan.deleteFilesOlderThan(CacheData.getTempDirectory(), numberDaysOld);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDebug() {
        return debug;
    }


    private class Inner_Start extends Thread {

        @Override
        public void run() {
            //   DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8,"Cache init");


            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), 8, "Finishing cache init");
        }

    }


}
