/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The MultiplayerServer class allows the computer to take on the host server role.
 */
public class MultiplayerServer extends Application {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerServer.class.getName());

	private static final int MAX_NUM_THREADS = 5;

	private static final int TIME_DELAY = 60 ; // in seconds

	private int port = 9090;

    //private boolean ready = false;
    boolean serverStopped = false;

	private String hostServerAddress;

    //private Stage stage = new Stage();
	private HostTask hostTask;
	//private MainMenu mainMenu;
	private MultiplayerTray multiplayerTray;

	//static final Object instance = new Object();
	public static final Object instance = new MultiplayerServer();

	private Socket socket = null;
	private ServerSocket ss = null;
	private CentralRegistry centralRegistry;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private Timeline timeline;
	private Instant oldLogTime;

	private ConnectionTask connectionTask;
	private transient ThreadPoolExecutor serverExecutor;
	private transient ThreadPoolExecutor connectionTaskExecutor;

	//private Map<Integer, String> addressMap = new ConcurrentHashMap<>(); // store clientID & address
	//private Map<Integer, String> idMap = new ConcurrentHashMap<>();
	private Map<Integer, Instant> timeTagMap = new ConcurrentHashMap<>();

	/* Method 3: Lazy Creation of Singleton ThreadSafe Instance without Using Synchronized Keyword.
	 * This implementation relies on the well-specified initialization phase of execution within the Java Virtual Machine (JVM).
	 * see http://crunchify.com/lazy-creation-of-singleton-threadsafe-instance-without-using-synchronized-keyword/

    private static class HoldInstance {
        private static final MultiplayerServer INSTANCE = new MultiplayerServer();
    }

    public static MultiplayerServer getInstance() {
        return HoldInstance.INSTANCE;
    }

    protected MultiplayerServer() {
		System.out.println("constructor");
	}
*/

    /*	Method 2 : Auto ThreadSafe Singleton Pattern using Object
     *  This implementation is more optimized than others since the need for checking
     *  the value of the Singleton instance ( i.e. instance == null ) is eliminated
*/
    public static Object getInstance() {
    	return instance;
    }

    // To start MultiplayerServer by itself, the following empty constructor must be commented out.
    //protected MultiplayerServer() {
 		//System.out.println("calling MultiplayerServer constructor");
 	//}

	//public void runServer() {//MainMenu mainMenu) {
		//this.mainMenu = mainMenu;
	//	System.out.println("start runServer()");
	//	startServer();
	//}


	public void runServer() {
		//System.out.println("running runServer()");
		serverExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
		connectionTaskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_NUM_THREADS); // newCachedThreadPool();
		//System.out.println("running startServer()");
		//stage.getIcons().add(new javafx.scene.image.Image(this.getClass().getResource("/icons/lander_hab64.png").toString()));
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		hostServerAddress = ip.getHostAddress();
		logger.info("Running the host at " + hostServerAddress);

		hostTask = new HostTask(hostServerAddress);
		serverExecutor.execute(hostTask);

		multiplayerTray = new MultiplayerTray(this);
	}

	public String getAddressStr() {
		return hostServerAddress;
	}

	//public MainMenu getMainMenu() {
	//	return mainMenu;
	//}

	public HostTask getHostTask() {
		return hostTask;
	}

	class HostTask implements Runnable {

		private String addressStr;

		private HostTask(String addressStr) {
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
		//long SLEEP_TIME = 500; // .5 second.

		centralRegistry = new CentralRegistry();

		String msg = "Ready to host at " + addressStr + "\nWaiting for clients to connect...";
		Platform.runLater(() -> {
			//if (mainMenu != null)
			createAlert(msg);
        });

		try {
			ss = new ServerSocket(port);//, 0);// InetAddress.getByAddress(new byte[] {127,0,0,1}));
	        /*
             * Allows the socket to be bound even though a previous
             * connection is in a timeout state.
             */
            ss.setReuseAddress(true);

			while (!serverStopped) {
	        	logger.info("Waiting for clients to connect...");
	        	socket = ss.accept();
	        	socket.setKeepAlive(true);
	        	connectionTask = new ConnectionTask(socket);
	        	connectionTaskExecutor.execute(connectionTask);
	          	//Thread t = new Thread(new ConnectionThread(socket));
	          	//t.start();
				//try {
					//TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
				//} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
			}

		//} catch (BindException e) {
		//	System.err.println("server socket already running.");
		 //   System.exit(1);
		} catch (IOException e) {
		    System.err.println("Unexpected error.");
		    e.printStackTrace();
		    System.exit(2);
		}

	}

	class ConnectionTask implements Runnable {

		private Socket socket;

		//private long SLEEP_TIME = 1000; // 1 second.

		public ConnectionTask(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			int id = 0;
			String clientAddress = socket.getInetAddress().toString().split("/")[1];
			String hostname = socket.getInetAddress().toString().split("/")[0];
        	//String clientIP = socket.getInetAddress().toString();
			if (hostname.trim() == "" || hostname == null)
				hostname = "[No Name]";
        	//String msg = "A client named at " + address + " has just established a connection with you.";
        	String msg = "A client at " + clientAddress + " has just established a connection with you.";
        	logger.info(msg);

			Platform.runLater(() -> {
	        	createAlert(msg);
			});

			// TODO: create a host dialog that lists all clients currently connected
			//add(clientAddress);
			try {
				startTimer();
	        	in = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
				out = new PrintWriter(socket.getOutputStream(), true);

                /*
                 * Notify SocketReaderThread that it can now start.
                 */
                //notifyReady();

				//out.println(new Date().toString());
				id = processInput(in, out, clientAddress);

				//TimeUnit.SECONDS.sleep(SLEEP_TIME);
				//System.out.flush();
				// TODO: create a main host dialog. If this dialog is closed, close all sockets
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
                /*
                 * This will notify the SocketReaderThread that it should exit.
                 */
                //notifyReady();
			} finally {
				removeSettlement(id);
				centralRegistry.getIdMap().remove(id);
				centralRegistry.getAddressMap().remove(id);
				logger.info("Socket closed, client id removed and connection closed.");
			}
		} // end of run()

	}

	/* Removes all settlements associated with that ID from the registry
	 * @param id
	 */
	public void removeSettlement(int id) {
		List<SettlementRegistry> oldList = centralRegistry.getSettlementRegistryList();
		//List<SettlementRegistry> newList = new ArrayList<> (oldList);
		// look for all the settlement with a particular client id and remove them from the central registry
		oldList.forEach( s -> {
			if (s.getClientID() == id) {
				oldList.remove(s);
				logger.info("");
			}
		});
		//oldList = newList;
	}

	/* Creates an info alert dialog
	 * @param string message
	 */
	public void createAlert(String str) {
		Alert alert = new Alert(AlertType.INFORMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		// Add corner icon
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/server48.png").toString()));
		// Add Stage icon
		alert.setGraphic(new ImageView(this.getClass().getResource("/icons/server256.png").toString()));
		//alert.initOwner(stage);
		alert.setTitle("Mars Simulation Project");
		alert.setHeaderText("Multiplayer Host");
		//if (mainMenu != null) {
		//   alert.initOwner(mainMenu.getStage());
		//}
		alert.setContentText(str);
		alert.show();
	}

	/* Processes the input line
	 * Note: stop when the input stream closes (is null) or "bye" is sent
	 * Otherwise, pass the input to doRequest()
	 */
	private int processInput(BufferedReader in, PrintWriter out, String clientAddress) {
		long SLEEP_TIME = 500; // .5 second.
		String line;
		boolean done = false;
		int id = 0;
		try {
	       while (!done) {
	    	   //System.out.println("processInput() : inside while(!done");
	    	   //if (in.readLine() != null && !socket.isClosed()) { // Get Records button got stuck
	    	   if (in != null) {
	    		   if ((line = in.readLine()) == null) { // java.net.SocketException: Connection reset, if hitting the red button on eclipse
	       				done = true;
	    		   } else {
			           logger.info("Command received : '" + line + "'");

			            if ((line.length() >= 4) &&     // "bye "
			       	        (line.substring(0, 3).toLowerCase().equals("bye"))) {
			       				logger.info("Command processed : 'bye'");
			       				String playerName = line.substring(4).trim(); // cut out the get keyword
			       				// extract the clientID of the player
			       				//id = Integer.parseInt(idStr);
			       				//TODO: should I set the timer to wait for a minute b4 removing the playerName?
			       				centralRegistry.getIdMap().remove(playerName);
			       				// when a value is removed, does it also remove key?
			       				centralRegistry.getAddressMap().remove(playerName);
			       				// TODO: if idMap is empty and done is true, will server just quit processing ?
					        	if (centralRegistry.getIdMap().size() == 0)
					        		done = true;
			       	    }

			           else {
			        	   id = executeCommand(line, out, clientAddress, id);
			        	   //centralRegistry.saveRecords();
			           }

			           try {
			        	   TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
			           } catch (InterruptedException e) {e.printStackTrace();}
	    		   }
	    	   } // end of if (in != null)
	       } // end of while()
		} catch(IOException e) { //| SocketException e) {
			e.printStackTrace();
			//logger.log(e);
		    //throw e;
		}
		return id;
	}


	/* Executes the command from client
	 * case 1: "new name & lat & long"
	 * case 2: "get"
	 * case 3: "register"
	 * @return id
	 */
	private int executeCommand(String line, PrintWriter out, String clientAddress, int id) {
		int newId = 0;
		boolean updateTimeTag = true;

	    if ((line.length() >= 7) &&     // "update "
		        (line.substring(0, 6).toLowerCase().equals("update"))) {
					logger.info("Command processed : update");
					centralRegistry.updateEntry( line.substring(6) );    // cut out the update keyword
		    }

	    else if (line.trim().toLowerCase().equals("get")) {
			logger.info("Command processed : get");
			out.println( centralRegistry.toString() );
		}
		// TODO: add command get all vs. get

	    else if ((line.length() >= 9) &&     // "register abc"
		        (line.substring(0, 8).toLowerCase().equals("register"))) {
			String playerName = line.substring(8).trim();    // cut out the register keyword
			logger.info("Command processed : register " + playerName);
			//centralRegistry.addEntry(userName);
			boolean doesPlayerNameExist = centralRegistry.verifyPlayerName(playerName, clientAddress);

			if (doesPlayerNameExist) {
				out.println( centralRegistry.disapprovePlayerName(playerName) );
				updateTimeTag = false;
			}
			else {
				newId = centralRegistry.assignNewID(playerName, clientAddress);
				//addressMap.put(id, clientAddress);
				//idMap.put(id, userName); // already included inside getNewID(userName);
				out.println( centralRegistry.approveID(newId, playerName) );
			}
	    }

	    else if ((line.length() >= 4) &&     // "new "
	        (line.substring(0, 3).toLowerCase().equals("new"))) {
				logger.info("Command processed : new");
				centralRegistry.addEntry( line.substring(3) );    // cut out the new keyword
				//centralRegistry.saveRecords(); // the best place to save
				//TODO: determine under what circumstance shall it be saved.
	    }

	    else if ((line.length() >= 7) &&     // "remove "
		        (line.substring(0, 6).toLowerCase().equals("remove"))) {
					logger.info("Command processed : remove");
					centralRegistry.removeEntry( line.substring(6) );    // cut out the remove keyword
		    }


	    else if (line.trim().substring(0, 1).toLowerCase().equals("s")) {
				logger.info("Command processed : 's'");
				int num = centralRegistry.getSettlementRegistryList().size();
				out.println("SETTLEMENTS " + num);
				logger.info("Sent : SETTLEMENTS " + num);

	    }

	    else {
	    	logger.info("The command from client cannot be recognized.");
			updateTimeTag = false;
	    }

		if (updateTimeTag && id != 0) {
			timeTagMap.put(id, Instant.now());
			newId = id;
		}

		return newId;
	  }

	/**
	 * Updates timeTagMap with latest timestamp
	 */
	public void updateTimeTag(int id) {
	}

	/*
	 * Closes sockets to terminate contact with the server
	 */
	  void closeSocket() {
	    try {
	    	in.close();
	    	out.close();
	    	socket.close(); // NullPointerException
	    	ss.close();
	    	if (ss != null && !ss.isClosed())
	    		ss.close();
	    }
	    catch(Exception e) {
	    	throw new RuntimeException("Error closing server", e);
	    	//e.printStackTrace();
	    }

	    System.exit(0);
	  }




	//public int assignClientID() {
	//	return ++lastClientID;
	//}

	public void setServerStopped(boolean value) {
		connectionTaskExecutor.shutdown();
		serverExecutor.shutdown();
		serverStopped = value;
		closeSocket();
	}


    /*
     * Synchronized method set up to wait until the SetupThread is
     * sufficiently initialized.  When notifyReady() is called, waiting
     * will cease.

    private synchronized void waitForReady() {
        while (!ready) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
    */
    /*
     * Synchronized method responsible for notifying waitForReady()
     * method that it's OK to stop waiting.

    private synchronized void notifyReady() {
        ready = true;
        notifyAll();
    }
 */

	/**
	 * Creates and starts the timer
	 *
	 * @return Scene
	 */
	public void startTimer() {
		// Set up earth time text update
		timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY),
				ae -> checkPlayerActivity()
				));
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();
	}

	/**
	 * Checks if a player has any activity within a predefined period of time
	 */
	public void checkPlayerActivity() {

		for (Map.Entry<Integer, Instant> entry : timeTagMap.entrySet()) {
			int key = entry.getKey();
			Instant value = entry.getValue();
			// check which player has a time tag older than TIME_DELAY ago
			// kick the player off from the idMap and addressMap
			System.out.println("Key = " + key + ", Value = " + value);
			// each player has its own time tag

			if (value == null)
				entry.setValue(Instant.now());

			Instant newLogTime = Instant.now();
			long elapsed = java.time.Duration.between(oldLogTime, newLogTime).toMillis() / 1000;

			if (elapsed > TIME_DELAY) {
				// unregister player,
				// clear idMap, addressMap
				centralRegistry.getIdMap().remove(key);
				centralRegistry.getAddressMap().remove(key);
				// Do NOT clear timeTagMap. leave it as it is.
				// send msg to panel
				//sendDisconnect();
				// TODO: where in this class to update new value e.g. entry.setValue(newLogTime);
			}

		}

	}


	public void start(Stage stage) throws Exception {
		runServer();
		//serverExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
		//connectionTaskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_NUM_THREADS); // newCachedThreadPool();
		//serverExecutor.execute(getHostTask());
	}

    public static void main(String[] args) {
    	launch(args);
    	//new MultiplayerServer();
    }

	public void destroy() {
		socket= null;
		serverExecutor = null;
		connectionTaskExecutor= null;
	    hostTask= null;
		connectionTask= null;
		//mainMenu= null;
		multiplayerTray= null;
		socket = null;
		centralRegistry= null;
	}


}