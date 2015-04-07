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

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert.AlertType;

/**
 * The MultiplayerServerClient class serves to set up multiplayer mode in MSP
 * and allows users to select the computer role as either host or client.
 */
public class MultiplayerClient {// extends Application {

	private List<String> addresses = new ArrayList<>();

	private int port = 9090;

	public MultiplayerClient() throws IOException {
		InetAddress ip = InetAddress.getLocalHost();
		String addressStr = ip.getHostAddress();
		System.out.println("Running the client at " + addressStr + ". Waiting to connect to a host...");
		createClient(addressStr);
	}


	public void createClient(String addressStr) throws IOException {

			//searchAddresses("192.168.0");
			//addresses = getIPAddressList();
			addresses.add("127.0.0.1");

			ChoiceDialog<String> dialog = new ChoiceDialog<>("127.0.0.1", addresses);
			dialog.setTitle("Mars Simulation Project");
			dialog.setHeaderText("Multiplayer Client");
			dialog.setContentText("You are at " + addressStr + ". Choose the desire local address : ");
			Optional<String> result = dialog.showAndWait();
			//if (result.isPresent()){
			//    System.out.println("Your choice: " + result.get());
			//}
			result.ifPresent(address -> {
			   System.out.println("Your choice: " + address);

			   Socket socket = null;

			   try {

				    socket = new Socket(address, port);
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				    String answer = input.readLine();
				    //dialog.close();
				    Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Mars Simulation Project");
					alert.setHeaderText("Multiplayer Client");
					alert.setContentText("Connection established with " + address);
					alert.showAndWait();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				} finally {
	                //try {
					//	socket.close();
					//} catch (Exception e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					//}
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