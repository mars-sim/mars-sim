/**
 * Mars Simulation Project
 * MultiplayerTray.java
 * @version 3.1.0 2017-06-22
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.networking;

import javafx.application.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.text.*;
import java.util.*;

// Original Java 8 codes by jewelsea at https://gist.github.com/jewelsea/e231e89e8d36ef4e5d8a
public class MultiplayerTray { //extends Application {

	private static String CLIENT_RUNNING = "MSP client connector is running";
	private static String CLIENT_CONNECTOR  = "MSP Client Connector";

	private String running;
	private String connector;

	private String addressStr;

    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
    private Stage stage;

    private MultiplayerClient multiplayerClient;

    // a timer allowing the tray icon to provide a periodic notification event.
    private Timer notificationTimer = new Timer();

    private java.awt.SystemTray tray;
    private java.awt.TrayIcon trayIcon ;

    // format used to display the current time in a tray icon notification.
    private DateFormat timeFormat = SimpleDateFormat.getTimeInstance();

    public MultiplayerTray(MultiplayerClient multiplayerClient) { //final Stage stage) {
    	this.multiplayerClient = multiplayerClient;
    	this.addressStr = multiplayerClient.getAddressStr();
        //this.stage = stage;
    	running =  CLIENT_RUNNING;
    	connector = CLIENT_CONNECTOR;

    	setStage();

    }

    public void setStage() {
        Platform.runLater(() -> {
        	createStage();
        });

        // Note: instructs the javafx system not to exit implicitly when the last application window is shut.
        // If this attribute is false, the application will continue to run normally even after the last window is closed, until the application calls exit(). The default value is true.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
    }

    private void createStage() {
    	stage = new Stage();
        // out stage will be translucent, so give it a transparent style.
        stage.initStyle(StageStyle.TRANSPARENT);
       	//stage = multiplayerClient.getMainMenu().getStage();
        // create the layout for the javafx stage.
        StackPane layout = new StackPane(createContent());
        layout.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5);");
        layout.setPrefSize(300, 200);

        layout.setOnMouseClicked(event -> stage.hide());

        Scene scene = new Scene(layout);
        // a scene with a transparent fill is necessary to implement the translucent app window.
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.setIconified(true);
    }

    /**
     * Creates the GUI
     * @return the main window application content.
     */
    private Node createContent() {
        Label hello = new Label("Mars Simulation Project\nMultiplayer Client Connector");
        hello.setStyle("-fx-font-size: 15px; -fx-text-fill: forestgreen;");
        Label instructions = new Label("(click to hide)");
        instructions.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");

        VBox content = new VBox(10, hello, instructions);
        content.setAlignment(Pos.CENTER);

        return content;
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            tray = java.awt.SystemTray.getSystemTray();
            //URL imageLoc = new URL(iconImageLoc);
            java.awt.Image image = ImageIO.read(this.getClass().getResource("/icons/lander_hab64.png"));
            trayIcon = new java.awt.TrayIcon(image);
            trayIcon.setImageAutoSize(true);

            // if the user double-clicks on the tray icon, show the main app stage.
            if (multiplayerClient != null)
            	trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem titleItem = new java.awt.MenuItem(connector);
            java.awt.MenuItem ipItem = new java.awt.MenuItem("Your IP is " + addressStr);
            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);

            java.awt.MenuItem panelItem = null;
            java.awt.MenuItem openItem = null;
            if (multiplayerClient != null) {
	            panelItem = new java.awt.MenuItem("Panel");
	            	panelItem.addActionListener(event -> {
	                    //multiplayerClient.getContainer().show();
	            	});

	            //openItem = new java.awt.MenuItem("Status");
	            	//openItem.addActionListener(event -> Platform.runLater(this::showStage));

	            //openItem.setFont(boldFont);
            }


            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
            	String msg = "Are you sure you want to quit?";
            	//System.out.println("Just clicked Exit.");
            	//if (multiplayerClient != null) {
                	Platform.runLater(() -> createAlert(msg));
/*            	}
            	else if (multiplayerServer != null) {
                	// TODO: fix the loading problem for server mode
        			notificationTimer.cancel();
        			Platform.exit();
        			tray.remove(trayIcon);
        			multiplayerServer.closeServerSocket();
                	// Use System.exit(0) to exit tentatively
                	//System.exit(0);
                }
*/
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(titleItem);
            popup.add(ipItem);
            popup.addSeparator();
            if (multiplayerClient != null) {
	            popup.add(panelItem);
	           // popup.add(openItem);
	            popup.addSeparator();
            }
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // TODO: report how long the host server has been running since start

            // create a timer which periodically displays a notification message.
            notificationTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            javax.swing.SwingUtilities.invokeLater(() ->
                                trayIcon.displayMessage(
                                		running,
                                        "System time : " + timeFormat.format(new Date()),
                                        java.awt.TrayIcon.MessageType.INFO
                                )
                            );
                        }
                    },
                    1_000,
                    600_000
            );

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    /**
     * Shows the application stage and ensures that it is brought to the front of all stages.
     */
    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
            //System.out.println("showing Stage");
        }
    }

    public void createAlert(String text) {
    	//Stage stage = new Stage();
        stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
        String header = null;
        //String text = null;
        if (multiplayerClient != null) {
        	header = "Multiplayer Client Connector";
        }

        //System.out.println("confirm dialog pop up.");
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(stage);
		alert.setTitle("Mars Simulation Project");
		alert.setHeaderText(header);
		alert.setContentText(text);
		alert.initModality(Modality.APPLICATION_MODAL);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.YES){
			notificationTimer.cancel();
			Platform.exit();
			tray.remove(trayIcon);
		    System.exit(0);
		}
		//else {
		//}
	}


    //public static void main(String[] args) throws IOException, java.awt.AWTException {
    //    launch(args);
    //}
}