package osi.scan;

//import eu.gnome.morena.Manager;
//import java.awt.Rectangle;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;
import osi.model.Client;
import osi.model.DebugLog;

import javax.swing.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;
//import static osi.scan.ScanHelper.manager;

/**
 * @author Joseph Pearce <joseph.pearce at your osioffices.com>
 */
public class FXMLDocumentController implements Initializable {

    //   private Manager instance;
    private String image;
    StackPane stack;
    @FXML
    private Label label;
    @FXML
    private Button button;
    @FXML
    private TextArea statustext;


    @FXML
    private AutoCompleteTextField autofield;
    @FXML
    private CheckBox scancheck;
    @FXML
    private CheckBox contentcheck;

    @FXML
    private CheckBox chargeclient;

    @FXML
    private CheckBox docupload;

    @FXML
    private ChoiceBox<String> optionalmsgbox;

    private String[] optionalSubjects;
    /*
     @FXML
     private CheckBox checkadf;
     @FXML
     private CheckBox checkflat;
     */
    private String tmpDir;
    // @FXML
    // private CheckBox flatpanel;
    // @FXML
    // private CheckBox adf;

    @FXML
    private CheckBox scanduplex;
    @FXML
    private CheckBox largeenvelope;
    @FXML
    private Hyperlink helplink;

    private HostServices hostServices;

    private Stage stage;

    private Alert alertGlobal;

    private Client client;

    private boolean usesane;  // wheither to use the sane scan method

    private String id;

    private String parentid;

    @FXML
    TextField optionalsubject;

    @FXML
    private void handleCheckAction(ActionEvent event) {

        try {
            if (scancheck.isSelected()) {
                contentcheck.setSelected(false);
            }
            if (contentcheck.isSelected()) {
                scancheck.setSelected(false);
            }
            if (!scancheck.isSelected()) {
                if (!contentcheck.isSelected()) {
                    scancheck.setSelected(true);
                }
            }
            if (!contentcheck.isSelected()) {
                if (!scancheck.isSelected()) {
                    scancheck.setSelected(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.TRACE, "finished method handleCheckAction");
    }


    @FXML
    private void changeOptionalSubject(ActionEvent event) {
        String subject = optionalmsgbox.getValue();
        optionalsubject.setText(subject);
    }

    @FXML
    private void handleCheckAction1(ActionEvent event) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Just called handle check action");

        try {

            // System.out.println("clicked on box " + event.getEventType());
            if (scancheck.isSelected()) {
                //   scanduplex.setSelected(false);
                scancheck.setSelected(false);
            }
            if (!contentcheck.isSelected()) {
                if (!scancheck.isSelected()) {

                    //      System.out.println("Neither is selected lets make one selected 4");
                    scancheck.setSelected(true);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Just called handlebuttonaction");

        // sc = new ScanHelper();
        long startbutton = System.currentTimeMillis();
        tmpDir = System.getProperty("user.dir") + "/tempimages/";
        try {
            //   DebugLog.logEvent(MethodHandles.lookup().lookupClass(),DebugLog.INFO,"CLEANING: "+tmpDir);
            //        FileUtils.cleanDirectory(new File(tmpDir));
        } catch (Exception e) {
            e.printStackTrace();
        }

        alertGlobal = new Alert(AlertType.INFORMATION);
        long start = System.currentTimeMillis();
        this.client = autofield.getClient();
        String id2 = String.valueOf(client.getMemnum());
        this.id = id2 + String.valueOf(System.currentTimeMillis());
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "ID CALCULCATED: " + id + " optional text: " + optionalsubject.getText());
        this.parentid = "";
        String note = client.getMailnote().trim();

        if (client.isOpenscan()) {
            note = note + " All mail is to be opened and scanned";
        }
        if (client.isMailforward()) {
            note = note + " Mail is to be forwarded ";
        }

        try {
            if (client.getMailnote().trim().length() > 5) {
                alertGlobal.setTitle("Instructions");
                alertGlobal.setHeaderText("This client has special instructions--");
                alertGlobal.setContentText(note);
                alertGlobal.showAndWait();

            } else {

                if (note.trim().length() > 5) {
                    alertGlobal.setHeaderText(note);
                    alertGlobal.setTitle("Instructions");
                    alertGlobal.showAndWait();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error message:" + e.getLocalizedMessage(), "There was an error", JOptionPane.ERROR_MESSAGE);

        }
        ScanState scanState = new ScanState();
        //  scanState.setSc(sc); // adds the scan helper
        scanState.setId(id);  // adds the id which is the memnmbr plus the systemcurrenttimems
        scanState.setParentid(parentid); // ?
        scanState.setClient(client); // the cliwnt to scan to
        scanState.setContentcheck(contentcheck.isSelected());
        scanState.setDocupload(docupload.isSelected());
        scanState.setLargeenvelope(largeenvelope.isSelected());
        scanState.setScancheck(scancheck.isSelected());
        scanState.setScanduplex(scanduplex.isSelected());
        scanState.setOptionalsubject(this.optionalsubject.getText());
        scanState.setAlertGlobal(this.alertGlobal);

     //   DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Using morena implemtnation");
        ScanFXThread sThread = new ScanFXThread(scanState);
        //     label.textProperty().bind(sThread.messageProperty());
        sThread.setOnFailed(evt -> {
                    Throwable exception = sThread.getException();
                    System.err.println("Error has occured: " + exception.getMessage());
                    alertGlobal = new Alert(AlertType.ERROR);
                    alertGlobal.setTitle("Error");
                    alertGlobal.setHeaderText(exception.getMessage());
                    alertGlobal.setContentText("note");
                    alertGlobal.showAndWait();
                }
        );
        statustext.textProperty().bind(sThread.messageProperty());
        //   errortext.textProperty().bind(sThread.titleProperty());
        long starttime = System.currentTimeMillis();
        Thread th = new Thread(sThread);
        th.setDaemon(true);
        th.start();
        long endtime = System.currentTimeMillis();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.ERROR, "Just returned from ScanFxThread time:" + (endtime - starttime));

        //
        //  sThread.run();

        autofield.clear();
        scancheck.setSelected(true);
        contentcheck.setSelected(false);
        chargeclient.setSelected(true);
        docupload.setSelected(false);
        optionalsubject.clear();
        long endbutton = System.currentTimeMillis();
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "Finished handle button action time:" + (endbutton - startbutton));
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        long startinit = System.currentTimeMillis();
//        NotificationPane notificationPane;
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.INFO, "In FXMLDocumentController initialization");
        optionalSubjects = CacheData.getScanMailDefaultMessages();
        optionalmsgbox.getItems().addAll(optionalSubjects);
        optionalmsgbox.setOnAction(this::changeOptionalSubject);
        // statustext.setText("STATUS check");
        //tmpDir = System.getProperty("java.io.tmpdir");
        scancheck.setSelected(true);
        contentcheck.setSelected(false);
        chargeclient.setSelected(true);
        chargeclient.setTooltip(new Tooltip("If this is selected the scanning will be recorded and billed. If this scan is free unclick this button"));
        scancheck.setTooltip(new Tooltip("Put the envelope facing down and on the left side lengthwise "));
        contentcheck.setTooltip(new Tooltip("Put the documents in the feeder AND the envelope lengthwise on the scanner  "));
        scanduplex.setTooltip(new Tooltip("Click this for documents with content on each side"));
        largeenvelope.setTooltip(new Tooltip("This is for envelopes bigger than standard. Put facedown width wise, top nearest the front of the scanner "));

        String saneproperty = "";
        saneproperty = System.getProperty("useSane");
        if (StringUtils.contains(saneproperty, "yes")) {
            usesane = true;

        } else {
            usesane = false;
        }

        //   long getsc=System.currentTimeMillis();
        try {
            //  sc = new ScanHelper();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long endgetsc = System.currentTimeMillis();
        System.out.println("Time to finish initialization: " + (endgetsc - startinit));

    }

    public Stage getStage() {
        DebugLog.logEvent(MethodHandles.lookup().lookupClass(), DebugLog.DEBUG, "Getting stage");

        if (this.stage == null) {
            this.stage = (Stage) this.stack.getScene().getWindow();
        }
        return stage;
    }


    public String getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @return the label
     */
    public Label getLabel() {
        return label;
    }

    /**
     * Might show the image after it was scanned to help the user determine if
     * it usable
     *
     * @param imageUrl
     */
    private void postImage(String imageUrl) {
        Image image = new Image(imageUrl, 160, 60, false, true);

        // Create the ImageView
        ImageView imageView = new ImageView(image);

        // Create the HBox      
        HBox root = new HBox();
        // Add Children to the HBox
        root.getChildren().add(imageView);

        // Set the padding of the HBox
        root.setStyle("-fx-padding: 10;");
        // Set the border-style of the HBox
        root.setStyle("-fx-border-style: solid inside;");
        // Set the border-width of the HBox
        root.setStyle("-fx-border-width: 2;");
        // Set the border-insets of the HBox
        root.setStyle("-fx-border-insets: 5;");
        // Set the border-radius of the HBox
        root.setStyle("-fx-border-radius: 5;");
        // Set the border-color of the HBox
        root.setStyle("-fx-border-color: blue;");
        // Set the size of the HBox
        root.setPrefSize(300, 200);

        // Create the Scene
        Scene scene = new Scene(root);
        // Add the scene to the Stage
        this.getStage().setScene(scene);
        // Set the title of the Stage
        this.getStage().setTitle("Displaying an Image");
        // Display the Stage
        this.getStage().show();
    }

    /**
     * @param label the label to set
     */
    public void setLabel(Label label) {
        this.label = label;
    }

}
