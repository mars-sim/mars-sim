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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.mars_sim.msp.javafx.MainMenu;
import org.mars_sim.networking.MultiplayerMode.ModeTask;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * The MultiplayerServerClient class allows the computer to take on the server role.
 */
public class MultiplayerServer { //extends Application {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerServer.class.getName());

	private List<String> roles = new ArrayList<>();
	private List<String> addresses = new ArrayList<>();

	private int port = 9090;
	private transient ThreadPoolExecutor executor;

    boolean serverStopped = false;

	private ModeTask modeTask;
	private ConnectionThread connectionThread;
	private MainMenu mainMenu;

	public MultiplayerServer(MainMenu mainMenu) throws IOException {
		this.mainMenu = mainMenu;

		InetAddress ip = InetAddress.getLocalHost();
		String addressStr = ip.getHostAddress();

		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();

		//logger.info("Running the host at " + addressStr);
		modeTask = new ModeTask(addressStr);

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
					createHost(addressStr);
		        });
		}
	}

	public void createHost(String addressStr) {
		ServerSocket ss = null;

		try {

			createAlert("Preparing to run the host at " + addressStr + "...\n\nClick OK to exit the Main Menu and start hosting.");

			ss = new ServerSocket(port);
			Socket socket;

			while (!serverStopped) {
	        	logger.info("Waiting for clients to connect...");
	        	socket = ss.accept();

        		connectionThread = new ConnectionThread(socket);
        		executor.execute(connectionThread);
	          	//Thread t = new Thread(new ConnectionThread(socket));
	          	//t.start();
	       }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
	}

	class ConnectionThread implements Runnable {

		private Socket socket;

		private long SLEEP_TIME = 1000; // 1 second.

		public ConnectionThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			String address = socket.getInetAddress().toString().split("/")[1];
			String hostname = socket.getInetAddress().toString().split("/")[0];
        	//String clientIP = socket.getInetAddress().toString();
			if (hostname.trim() == "" || hostname == null)
				hostname = "[No Name]";
        	String msg = "A client named at " + address + " has just established a connection with you.";
        	logger.info(msg);

			Platform.runLater(() -> {
	        	createAlert(msg);
			});

			// TODO: create a host dialog to list out all exiting connections.

         	PrintWriter out = null;
			BufferedReader in = null;

			try {

	        	in = new BufferedReader( new InputStreamReader(socket.getInputStream()) );

				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(new Date().toString());

				TimeUnit.SECONDS.sleep(SLEEP_TIME);

				// TODO: if the host dialog is closed, close socket
				socket.close();
				logger.info("Done. Client connection closed.");

			} catch (Exception e) {e.printStackTrace();}

		} // end of run()

	}

	public void createAlert(String str) {
		   Alert alert = new Alert(AlertType.INFORMATION);
		   alert.initOwner(mainMenu.getStage());
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