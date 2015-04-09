/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-09
 * @author Manny Kung
 */

package org.mars_sim.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import org.mars_sim.msp.javafx.MainMenu;

import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;

/**
 * The MultiplayerServerClient class serves to set up multiplayer mode in MSP
 * and allows user to select the computer as a host or a client.
 */
public class MultiplayerMode {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerMode.class.getName());

	private ModeTask modeTask = new ModeTask();

	private List<String> roles = new ArrayList<>();

	private ChoiceDialog<String> dialog;

	private MainMenu mainMenu;

	private transient ThreadPoolExecutor serverClientExecutor;

	//public void start(Stage primaryStage) throws IOException {
	public MultiplayerMode(MainMenu mainMenu) throws IOException {
		this.mainMenu = mainMenu;

		roles.add("Host");
		roles.add("Client");

		serverClientExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();

	}

	public ChoiceDialog<String> getChoiceDialog() {
		return dialog;
	}

	public ModeTask getModeTask() {
		return modeTask;
	}


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
		dialog = new ChoiceDialog<>("Host", roles);
		dialog.initOwner(mainMenu.getStage());
		dialog.initModality(Modality.NONE);
		dialog.setTitle("Mars Simulation Project");
		dialog.setHeaderText("Multiplayer Setup");
		dialog.setContentText("Choose your role : ");
		Optional<String> result = dialog.showAndWait();

		   result.ifPresent(role -> {
			   logger.info("Your Multiplayer Role is : " + role);
			   if (role.equals("Host")) {
					try {
						dialog.close();
						InetAddress ip = InetAddress.getLocalHost();
						String addressStr = ip.getHostAddress();
						logger.info("Your IP address is : " + addressStr);
						MultiplayerServer server = new MultiplayerServer(mainMenu);
						serverClientExecutor.execute(server.getModeTask());
						mainMenu.getStage().close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			   }

			   else if (role.equals("Client")) {
					try {
						MultiplayerClient client = new MultiplayerClient(mainMenu);
						serverClientExecutor.execute(client.getModeTask());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			   }
		   });
		}


}