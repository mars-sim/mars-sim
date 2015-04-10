/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-06
 * @author Manny Kung
 */

package org.mars_sim.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.mars_sim.msp.javafx.MainMenu;
import org.mars_sim.networking.MultiplayerServer.ModeTask;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;
import javafx.scene.control.Alert.AlertType;

/**
 * The MultiplayerClient class allows the computer to take on the client role.
 */
public class MultiplayerClient {// extends Application {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerClient.class.getName());

	private int port = 9090;

	private String addressStr;

	private List<String> addresses = new ArrayList<>();

	private MainMenu mainMenu;
	private ModeTask modeTask;
	private MultiplayerTray multiplayerTray;

	private Alert alert;

	public MultiplayerClient(MainMenu mainMenu) throws IOException {
		this.mainMenu = mainMenu;

		InetAddress ip = InetAddress.getLocalHost();
		addressStr = ip.getHostAddress();
		logger.info("Running the client at " + addressStr + ". Waiting to connect to a host...");
		modeTask = new ModeTask(addressStr);

		multiplayerTray = new MultiplayerTray(this);


	}


	public String getAddressStr() {
		return addressStr;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	public ModeTask getModeTask() {
		return modeTask;
	}

	class ModeTask implements Runnable {

		private String addressStr;

		private ModeTask(String addressStr) {
			this.addressStr = addressStr;
		}

		@Override
		public void run() {
				Platform.runLater(() -> {
					createClient(addressStr);
		        });
		}
	}

	public void createClient(String addressStr) {

			addresses.add("127.0.0.1");

			ChoiceDialog<String> dialog = new ChoiceDialog<>("127.0.0.1", addresses);
			dialog.initModality(Modality.NONE);
			dialog.initOwner(mainMenu.getStage());
			dialog.setTitle("Mars Simulation Project");
			dialog.setHeaderText("Multiplayer Client");
			dialog.setContentText("You are at " + addressStr + ".\nChoose the host address : ");

			Optional<String> result = dialog.showAndWait();
			//if (result.isPresent()){
			//    System.out.println("Your choice: " + result.get());
			//}
			result.ifPresent(address -> {
				logger.info("Connecting to the host at " + address);

			   Socket socket = null;
			   BufferedReader input;
			   try {
				    socket = new Socket(address, port);
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				    String answer = input.readLine();
				    alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Mars Simulation Project");
					alert.initOwner(mainMenu.getStage());
					alert.setHeaderText("Multiplayer Client");
					alert.setContentText("Connection established with " + address);
					alert.showAndWait();
					//alert.setOnCloseRequest(e -> {
					//	 try {
					//		socket.close();
					//	} catch (Exception e1) {e1.printStackTrace();}
					//});

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				} finally {
					try {
						socket.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                //alert.close();
	            }

		   });


		}
/*
	@Override
	public void start(Stage arg0) throws Exception {
		new MultiplayerClient();
	}

    public static void main(String[] args) {
    	launch(args);
    }
*/
}