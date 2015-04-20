/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-20
 * @author Manny Kung
 */

package org.mars_sim.msp.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.mars_sim.msp.javafx.MainMenu;
import org.mars_sim.msp.network.MultiplayerClient;
import org.mars_sim.msp.network.MultiplayerServer;

import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
/**
 * The MultiplayerServerClient class serves to set up multiplayer mode in MSP
 * and allows user to select the computer as a host or a client.
 */
public class MultiplayerMode {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerMode.class.getName());


	private List<String> roles = new ArrayList<>();

	private ChoiceDialog<String> dialog;

	private ModeTask modeTask = new ModeTask();

	private MainMenu mainMenu;

	private MultiplayerServer multiplayerServer;

	private MultiplayerClient multiplayerClient;

	//private transient ThreadPoolExecutor serverClientExecutor;

	//public void start(Stage primaryStage) throws IOException {
	public MultiplayerMode(MainMenu mainMenu) throws IOException {
		this.mainMenu = mainMenu;

		roles.add("Host");
		roles.add("Client");

		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {e.printStackTrace();}
		String addressStr = ip.getHostAddress();
		logger.info("Your IP address is : " + addressStr);

		//serverClientExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();

	}

	public ChoiceDialog<String> getChoiceDialog() {
		return dialog;
	}

	public ModeTask getModeTask() {
		return modeTask;
	}

	public MultiplayerServer getMultiplayerServer() {
		return multiplayerServer;
	}

	// MultiplayerClient getMultiplayerClient() {
	//	return multiplayerClient;
	//}


	class ModeTask implements Runnable {

		private ModeTask() {
		}

		@Override
		public void run() {
			try {
				Platform.runLater(() -> {
	    				createChoiceDialog();
	            });
			} catch (ConcurrentModificationException e) {} //Exception e) {}
		}
	}


	public void createChoiceDialog() {
		dialog = new ChoiceDialog<>("Client", roles);
		//dialog.initOwner(mainMenu.getStage());
		// Get the Stage.
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		// Add corner icon.
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/server48.png").toString()));
		// Add Stage icon
		dialog.setGraphic(new ImageView(this.getClass().getResource("/icons/server256.png").toString()));
		dialog.initModality(Modality.NONE);
		dialog.setTitle("Mars Simulation Project");
		dialog.setHeaderText("Multiplayer Setup");
		dialog.setContentText("Choose your role : ");
		Optional<String> result = dialog.showAndWait();

		   result.ifPresent(role -> {
			   logger.info("Your Multiplayer Role is : " + role);
			   if (role.equals("Host")) {
					try {
						//dialog.close();
						mainMenu.getStage().close(); // needed in order to start a new UI application thread
						//MultiplayerServer server =  (MultiplayerServer) MultiplayerServer.instance;
						//MultiplayerServer server =  MultiplayerServer.getInstance();
						//server.runServer();
                        //javax.swing.SwingUtilities.invokeLater(() ->);
						//Platform.setImplicitExit(true);
						//Platform.exit();
						((MultiplayerServer) MultiplayerServer.instance).runServer();
						//mainMenu.getStage().fireEvent(new WindowEvent(mainMenu.getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));

						//serverClientExecutor.execute(server.getHostTask());
					} catch (Exception e) {
						e.printStackTrace();
					}

			   }

			   else if (role.equals("Client")) {
					try {
						//MultiplayerClient client = MultiplayerClient.getInstance();
						//client.runClient();
						MultiplayerClient.getInstance().runClient();
						//serverClientExecutor.execute(client.getClientTask());
					} catch (Exception e) {
						e.printStackTrace();
					}
			   }
		   });
		}


}