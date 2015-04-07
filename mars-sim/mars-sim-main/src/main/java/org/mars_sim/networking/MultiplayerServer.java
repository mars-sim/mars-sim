/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-06
 * @author Manny Kung
 */

package org.mars_sim.networking;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * The MultiplayerServerClient class serves to set up multiplayer mode in MSP
 * and allows users to select the computer role as either host or client.
 */
public class MultiplayerServer { //extends Application {

	private List<String> roles = new ArrayList<>();
	private List<String> addresses = new ArrayList<>();

	private int port = 9090;

	public MultiplayerServer() throws IOException {
		InetAddress ip = InetAddress.getLocalHost();
		String addressStr = ip.getHostAddress();
		System.out.println("Running the host at " + addressStr);
		createHost(addressStr);
	}

	public void createHost(String addressStr) throws IOException {

		try {

			createAlert("Running the host at " + addressStr + ". Click OK to begin");

			ServerSocket listener = new ServerSocket(port);

			while (true) {
	        	Socket socket = listener.accept();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(new Date().toString());
	          	Thread t = new Thread(new HostThread(socket));
	          	t.start();
	       }

		} catch (Exception e) {
			e.printStackTrace();
		}
/*
		} finally {
			if (listener != null)
	    		  listener.close();
				socket.close();
				alert.close();
	       	}
*/
/*
	        try {

	 		   listener = new ServerSocket(9090);

	            while (true) {
	                Socket socket = listener.accept();
	                try {
	                    System.out.println("A client has just established the connection with you");
	                    //alert.close();
	                    alert.setTitle("Mars Simulation Project");
	         		   	alert.setHeaderText("Multiplayer Host");
	         		   	alert.setContentText("A client has just established the connection with you");
	         		   	alert.showAndWait();
	                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	                    out.println(new Date().toString());
	                } finally {
	                    socket.close();
	                    alert.close();
	                }
	            }
	        }
	        finally {
	        	if (listener != null)
	        		listener.close();
	        	alert.close();
	        }
*/
	}

	class HostThread implements Runnable {

		private Socket socket;

		public HostThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
        	String clientIP = socket.getInetAddress().toString();
        	System.out.println("Waiting for clients to connect...");

        	String msg = "A client from " + clientIP + " has just established the connection with you";
            System.out.println(msg);
        	createAlert(msg);

         	PrintWriter out = null;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          	out.println(new Date().toString());

		}

	}

	public void createAlert(String str) {
		   Alert alert = new Alert(AlertType.INFORMATION);
		   alert.setTitle("Mars Simulation Project");
		   alert.setHeaderText("Multiplayer Host");
		   alert.setContentText(str);
		   alert.showAndWait();
	}

    /**
     * Runs the server.

    public static void main(String[] args) {
    	launch(args);
    }

	@Override
	public void start(Stage arg0) throws Exception {
		new MultiplayerServer();
	}
	*/
}