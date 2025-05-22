/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osi.scan;

import osi.factory.ClientFactory;
import osi.model.Client;
import osi.model.DebugLog;
import osi.util.ConfigHandler;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * @author josephpearce
 */
public class CacheData {

    private static List<String> existingClientNamesList;
    private static boolean init = false;

    private static String tempDirectory;
    private static String[] scanMailDefaultMessages;

    public static void init() {
        long start = System.currentTimeMillis();
        existingClientNamesList = new ArrayList<String>();
        List<Client> clientList = ClientFactory.getAllMembers();
        tempDirectory = System.getProperty("user.dir") + "/tempimages/";
        String line = "";
        String messages = ConfigHandler.getEntry("ScanMailDefaultMessages");
        //  System.out.println(messages);
        scanMailDefaultMessages = messages.split(",");
        for (int i = 0; i < clientList.size(); i++) {
            try {
                //        existingClientNamesList.add(clientList.get(i).getFirmname().toLowerCase() + " | " + clientList.get(i).getFirstName().toLowerCase());
                String noticeAboutService = "";
                if (clientList.get(i).isUmmail() == false) {

                    noticeAboutService = "NO SCANNING |";

                } else {

                    if (clientList.get(i).isOpenscan()) {
                        noticeAboutService = "SCANNING ENV & CONTENTS |";

                    } else {
                        noticeAboutService = "SCANNING ENV |";
                    }

                }
                if (clientList.get(i).isMailforward()) {

                    noticeAboutService = noticeAboutService + " FORWARDING";
                } else {
                    noticeAboutService = noticeAboutService + " MAILROOM";
                }
                if (clientList.get(i).getPermCheck().isBlockedfromopenscan()) {
                    noticeAboutService = noticeAboutService + " CLIENT IS SUSPENDED FROM OPEN SCAN";
                }

                line = clientList.get(i).getFirmname().toLowerCase() + " | " + clientList.get(i).getFirstName().toLowerCase() + " | " + noticeAboutService;
                if (line.trim().length() > 5) {
                    existingClientNamesList.add(clientList.get(i).getFirmname().toLowerCase() + " | " + clientList.get(i).getFirstName().toLowerCase() + " | " + noticeAboutService);
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Exception with user: " + clientList.get(i).getMemnum());
                System.err.println("line: " + line);
            }
        }
       // DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Cache Data init time: " + (System.currentTimeMillis() - start));
        init = true;
    }

    public static Client getClientFromLongName(String longName) {
        //  String firstname=this.getLongName();
        //     System.err.println("longName: " + longName);
        Client client = null;
        try {
            String firstname = longName.substring(0, longName.indexOf("|"));
            client = ClientFactory.getClientByName(firstname);

        } catch (Exception e) {
            DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Unable to get Client from longname: " + longName);
            e.printStackTrace();
        }

        //  System.out.println("Client first name:" +   firstname);
        //  this.selectedClient = ClientFactory.getClientByName(firstname);
        return client;

    }

    public static List<String> getExistingClientNamesList() {

        if (!init) {
            init();

        }

        return existingClientNamesList;
    }

    public static String getTempDirectory() {
        return tempDirectory;
    }

    public static void setTempDirectory(String tempDirectory) {
        CacheData.tempDirectory = tempDirectory;
    }

    public static String[] getScanMailDefaultMessages() {
        return scanMailDefaultMessages;
    }

    public static void setScanMailDefaultMessages(String[] scanMailDefaultMessages) {
        CacheData.scanMailDefaultMessages = scanMailDefaultMessages;
    }
}
