/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-06
 * @author Manny Kung
 */

package org.mars_sim.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;

/**
 * The MultiplayerServerClient class serves to set up multiplayer mode in MSP
 * by allowing users to select as a host or a client.
 */
public class MultiplayerMode implements Runnable{

	private List<String> roles = new ArrayList<>();

	//public void start(Stage primaryStage) throws IOException {
	public MultiplayerMode() throws IOException {

		roles.add("Host");
		roles.add("Client");

	}

	public void run() {

		ChoiceDialog<String> dialog = new ChoiceDialog<>("Host", roles);
		dialog.setTitle("Mars Simulation Project");
		dialog.setHeaderText("Multiplayer Wizard");
		dialog.setContentText("Choose your role : ");
		Optional<String> result = dialog.showAndWait();

		   result.ifPresent(role -> {

			   System.out.println("Your choice: " + role);

			   if (role.equals("Host")) {
					try {
						dialog.close();
						InetAddress ip = InetAddress.getLocalHost();
						String addressStr = ip.getHostAddress();
						System.out.println("Host address is : " + addressStr);
						MultiplayerServer server = new MultiplayerServer();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			   }

			   else if (role.equals("Client")) {
					try {
						MultiplayerClient client = new MultiplayerClient();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			   }
		   });

		}


}