package osi.scan;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import osi.model.Client;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
//import osi.util.AppData;

/**
 * This class is a TextField which implements an "autocomplete" functionality,
 * based on a supplied list of entries.
 *
 * @author Caleb Brinkman
 */
public class AutoCompleteTextField extends TextField {

    /**
     * The existing autocomplete entries.
     */
    private final SortedSet<String> entries;
    /**
     * The popup used to select an entry.
     */
    private ContextMenu entriesPopup;
    private List<String> completeNames;
    private String prname;
    private Client client;

    /**
     * Construct a new AutoCompleteTextField.
     */
    public AutoCompleteTextField() {
        super();
       // System.out.println("In autocomplete text field");
        entries = new TreeSet<>();
        // this.setStyle("font-size: 22px;");
        this.setFont(Font.font("Serif", FontWeight.BOLD, 22));

        entriesPopup = new ContextMenu();
        entriesPopup.setStyle("font-size: 22px !Important;");
        textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {

                completeNames = completeNames(getText());
                //   System.out.println("CHANGED: " + getText() + " matches: " + completeNames.size());

                FXMLLoader fxmlLoader = new FXMLLoader();
                try {
                    Pane p = fxmlLoader.load(getClass().getResource("FXMLDocument.fxml").openStream());
                    FXMLDocumentController fooController = (FXMLDocumentController) fxmlLoader.getController();
                    fooController.getLabel().setText("bbbb");
                } catch (IOException ex) {
                    Logger.getLogger(AutoCompleteTextField.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (getText().length() == 0) {
                    entriesPopup.hide();
                } else {
                    if (completeNames.size() > 0) {
                        populatePopup(completeNames);
                        if (!entriesPopup.isShowing()) {
                            entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                        }
                    } else {
                        entriesPopup.hide();
                    }
                }
            }
        });

        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {

                //    
                if (getText().length() > 1) {
                    //        client = AppData.getClientFromLongName(getText());
                    client = CacheData.getClientFromLongName(getText());
                    //       EmailThread eT = new EmailThread("joseph@osioffices.com", "body", "subject");
                //    System.out.println("CHANGED2: " + getText() + " " + client.getEmail());
                    //   prname=clientFromLongName.getFirstName();
                    //  eT.start();
                }

                entriesPopup.hide();
            }
        });

    }

    public List<String> completeNamesOld(String query) {
        query = query.toLowerCase();
        System.out.println("Trying to completeNames with query: " + query);
        //  List<String> clientNameList = AppData.getExistingClientNamesList();
        List<String> clientNameList = CacheData.getExistingClientNamesList();
        List<String> nameList = new ArrayList<String>();
        //  String clientstring="";
        for (int i = 0; i < clientNameList.size(); i++) {
            if (clientNameList.get(i).contains(query)) {

                nameList.add(clientNameList.get(i));
            }

        }

        return nameList;
    }

    public List<String> completeNames(String query) {
        query = query.toLowerCase();
    //    System.out.println("Trying to completeNames1 with query: " + query);
        //  List<String> clientNameList = AppData.getExistingClientNamesList();
        List<String> clientNameList = CacheData.getExistingClientNamesList();
        List<String> nameList = new ArrayList<String>();
        String[] queryterms = query.split(" ");
        if (queryterms.length == 1) {
            //  String clientstring="";
            for (int i = 0; i < clientNameList.size(); i++) {
                if (clientNameList.get(i).contains(query)) {
                    String name = clientNameList.get(i);
                //    System.out.println("Adding name: " + name);
                  //  name="<i>"+name+"</i>";
                    nameList.add(name);
                }

            }
        }
        if (queryterms.length == 2) {
            //  System.out.println("query terms is 2");
            //  String clientstring="";
            for (int i = 0; i < clientNameList.size(); i++) {
                if (clientNameList.get(i).contains(queryterms[0]) && clientNameList.get(i).contains(queryterms[1])) {

                    nameList.add(clientNameList.get(i));
                }

            }
        }

        if (queryterms.length == 3) {
            //    System.out.println("query terms is 3");
            //  String clientstring="";
            for (int i = 0; i < clientNameList.size(); i++) {
                if (clientNameList.get(i).contains(queryterms[0]) && clientNameList.get(i).contains(queryterms[1]) && clientNameList.get(i).contains(queryterms[2])) {

                    nameList.add(clientNameList.get(i));
                }

            }
        }

        return nameList;
    }

    public List<String> completeNamesNew(String query) {
        long start = System.currentTimeMillis();
        query = query.toLowerCase();
        List<String> clientNameList = CacheData.getExistingClientNamesList();
        List<String> nameList = new ArrayList<String>();
        String clientstring = "";
        for (int i = 0; i < clientNameList.size(); i++) {

            clientstring = clientNameList.get(i);

            String[] queryterms = query.split(" ");
            System.out.println("number of query terms: " + queryterms.length);
            if (queryterms.length == 1) {
                if (clientNameList.get(i).contains(query)) {

                    nameList.add(clientNameList.get(i));
                }

            } else {

                int totalmatches = 0;
                for (int j = 0; j < queryterms.length; j++) {

                    System.out.println("Checking if " + queryterms[j] + " is in " + clientstring);
                    if (clientstring.contains(queryterms[j])) {
                        totalmatches++;
                        System.out.println("Found a match");

                    }

                }
                if (totalmatches == queryterms.length) {
                    nameList.add(clientNameList.get(i));
                    System.out.println("Found a complete match for: " + clientstring + " for query " + query);
                }
            }

            nameList.add(clientNameList.get(i));
        }
        System.out.println("Total time to scan and look for match: " + (System.currentTimeMillis() - start) + " size of return:" + nameList.size());
        return nameList;
    }

    /**
     * Get the existing set of autocomplete entries.
     *
     * @return The existing autocomplete entries.
     */
    public SortedSet<String> getEntries() {
        return entries;
    }

    /**
     * Populate the entry set with the given search results. Display is limited
     * to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<String> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        // If you'd like more entries, modify this line.
        int maxEntries = 10;
        int count = Math.min(searchResult.size(), maxEntries);
        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            Label entryLabel = new Label(result);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    setText(result);
                    entriesPopup.hide();
                }
            });
            menuItems.add(item);
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);

    }

    /**
     * @return the completeNames
     */
    public List<String> getCompleteNames() {
        return completeNames;
    }

    /**
     * @return the prname
     */
    public String getPrname() {
        System.out.println("Getting: " + prname);
        return prname;
    }

    /**
     * @return the client
     */
    public Client getClient() {
        return client;
    }
}
