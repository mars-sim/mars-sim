/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-11
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import org.mars_sim.msp.javafx.MainMenu;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * The MultiplayerServerClient class allows the computer to take on the server role.
 */
public class MultiplayerServer { //extends Application {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerServer.class.getName());

	//private List<String> roles = new ArrayList<>();
	//private List<String> addresses = new ArrayList<>();

	private int port = 9090;

	private int lastClientID = 0;

	private String addressStr;

    boolean serverStopped = false;

	private transient ThreadPoolExecutor executor;
    //private Stage stage;
	private ModeTask modeTask;
	private ConnectionThread connectionThread;
	private MainMenu mainMenu;
	private MultiplayerTray serverTray;

	private Socket socket = null;
	private ServerSocket ss = null;
	private CentralRegistry centralRegistry;


	public MultiplayerServer(MainMenu mainMenu) throws IOException {
		this.mainMenu = mainMenu;

		//stage = new Stage();

		InetAddress ip = InetAddress.getLocalHost();
		addressStr = ip.getHostAddress();

		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();

		//logger.info("Running the host at " + addressStr);
		modeTask = new ModeTask(addressStr);

		serverTray = new MultiplayerTray(this);
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
			createHost(addressStr);
		}
	}

	public void createHost(String addressStr) {
		centralRegistry = new CentralRegistry();

		try {

			Platform.runLater(() -> {
				createAlert("Preparing to run the host at " + addressStr + "...\n\nClick OK to exit the Main Menu and start hosting.");
	        });

			ss = new ServerSocket(port);

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
		//} finally {
		//	try {
		//		ss.close();
		//	} catch (IOException e) {e.printStackTrace();}
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
        	//String msg = "A client named at " + address + " has just established a connection with you.";
        	String msg = "A client at " + address + " has just established a connection with you.";
        	logger.info(msg);

			Platform.runLater(() -> {
	        	createAlert(msg);
			});

			// TODO: create a host dialog that lists all exiting connections.

         	PrintWriter out = null;
			BufferedReader in = null;

			try {

	        	in = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
				out = new PrintWriter(socket.getOutputStream(), true);
				//out.println(new Date().toString());
				processInput(in, out);

				//TimeUnit.SECONDS.sleep(SLEEP_TIME);
				//System.out.flush();
				// TODO: if the host dialog is closed, close socket
				socket.close();
				logger.info("Done. Client connection closed.");
				centralRegistry.saveRecords();
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

	/* Processes the input line
	 * Note: stop when the input stream closes (is null) or "bye" is sent
	 * Otherwise, pass the input to doRequest()
	 */
	private void processInput(BufferedReader in, PrintWriter out) {
	     String line;
	     boolean done = false;
	     try {
	       while (!done) {
	         if((line = in.readLine()) == null) {
	        	 done = true;
		         //centralRegistry.saveRecords();
		         //socket.close();
	         }
	         else {
	           logger.info("Command received : '" + line + "'");
	           if (line.trim().equals("bye")) {
	        	   done = true;
	        	   //centralRegistry.saveRecords();
	        	   //socket.close();
	           }
	           else
	        	   executeCommand(line, out);
	         }
	       }
	     }
	     catch(IOException e)
		    { e.printStackTrace();}
	   }


	/* Executes the command from client
	 * case 1: "new name & lat & long"
	 * case 2: "get"
	 */
	private void executeCommand(String line, PrintWriter out) {
		if (line.trim().toLowerCase().equals("get")) {
			logger.info("Command processed : 'get'");
			out.println( centralRegistry.toString() );
		}
		else if (line.trim().toLowerCase().equals("register")) {
			logger.info("Command processed : 'register'");
			int id = getNewID();
			out.println( centralRegistry.returnID(id) );
		}
	    else if ((line.length() >= 4) &&     // "new "
	        (line.substring(0,3).toLowerCase().equals("new"))) {
				logger.info("Command processed : 'new'");
				centralRegistry.addEntry( line.substring(3) );    // cut out the new keyword
				//centralRegistry.saveRecords();
	    }
	    else
	      logger.info("The command from client cannot be recognized.");
	  }

	/*
	 * Closes sockets to terminate contact with the server
	 */
	  void closeServerSocket() {
	    try {
	      ss.close();
	    }
	    catch(Exception e)
	    { e.printStackTrace();}

	    System.exit(0);
	  }

	public int getNewID() {
		return ++lastClientID;
	}

	//public int assignClientID() {
	//	return ++lastClientID;
	//}

	public void destroy() {
		socket= null;
		executor= null;
	    modeTask= null;
		connectionThread= null;
		mainMenu= null;
		serverTray= null;
		socket = null;
		centralRegistry= null;

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