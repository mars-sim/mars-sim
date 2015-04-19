/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-17
 * @author Manny Kung
 */

package org.mars_sim.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.control.ButtonType;

/**
 * The MultiplayerClient class allows the computer to take on the client role.
 */
public class MultiplayerClient {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerClient.class.getName());

	private static final int RECORDS = 0;
	private static final int NEW_ID = 1;

	private static final int PORT = 9090;

	//private static final String LOCALHOST = "localhost";

	private int clientID = 0;
    private boolean ready = false;

	private String hostAddressStr;
	private String clientAddressStr;
	private String playerName;

	private List<String> addresses = new ArrayList<>();

	//private static MultiplayerClient instance = null;

	private Stage stage;
	private Socket sock;
	private BufferedReader in;     // i/o for the client
	private PrintWriter out;

	private TextArea ta;
	private TextField tfName, tfTemplate, tfPop, tfBots, tfLat, tfLong;
	private Button bGetRecords;
	private Button bCreateNew;
	private Button bRegister;
	//Container c;

	//private MainMenu mainMenu;
	private ClientTask clientTask;
	private MultiplayerTray multiplayerTray;
	private Alert alert;
	private transient ThreadPoolExecutor clientExecutor;

	//private SettlementRegistry[] registryArray = new SettlementRegistry[CentralRegistry.MAX];
	private List<SettlementRegistry> settlementList;

	protected MultiplayerClient() {
	}

	/* Method 3: Lazy Creation of Singleton ThreadSafe Instance without Using Synchronized Keyword.
	 * This implementation relies on the well-specified initialization phase of execution within the Java Virtual Machine (JVM).
	 * see http://crunchify.com/lazy-creation-of-singleton-threadsafe-instance-without-using-synchronized-keyword/
	 */
    private static class HoldInstance {
        private static final MultiplayerClient INSTANCE = new MultiplayerClient();
    }

    public static MultiplayerClient getInstance() {
        return HoldInstance.INSTANCE;
    }


/*  Method 1 : Simple Singleton Pattern: (Lazy Initialization + ThreadSafe with synchronized block)
 *  This implementation using the synchronized keyword to make the traditional approach thread-safe
 *
  	private static MultiplayerClient instance = null;

  	protected MultiplayerClient() {	}

	public static MultiplayerClient getInstance() {
		if (instance == null) {
			synchronized (MultiplayerClient.class) {
				if (instance == null) {
					instance = new MultiplayerClient();
				}
			}
		}
		return instance;
	}

*/

/*	Method 2 : Auto ThreadSafe Singleton Pattern using Object
 *  This implementation is more optimized than others since the need for checking
 *  the value of the Singleton instance ( i.e. instance == null ) is eliminated
 *
	private static final Object instance = new Object();

  	protected MultiplayerClient() {	}

	public static Object getInstance() {
		return instance;
	}
*/

	public void runClient() { //MainMenu mainMenu) {

		//this.mainMenu = mainMenu;

		settlementList = new CopyOnWriteArrayList<>();

		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		clientAddressStr = ip.getHostAddress();
		logger.info("Running the client at " + clientAddressStr + ". Waiting to connect to a host...");

        Platform.runLater(() -> {
    	   	createConnectionStage();
        });

		clientTask = new ClientTask();//clientAddressStr);
		clientExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
		clientExecutor.execute(clientTask);
	}

	public void setUserName(String name) {
		playerName = name;
	}

	//public Container getContainer() {
	//	return c;
	//}

	public String getAddressStr() {
		return hostAddressStr;
	}

	//public MainMenu getMainMenu() {
	//	return mainMenu;
	//}

	public ClientTask getClientTask() {
		return clientTask;
	}

	/*
	 * Creates the task that will prompt users to create a dialog box for choosing the host server's IP address
	 */
	class ClientTask implements Runnable {

		//private String addressStr;

		private ClientTask() { //String addressStr) {
			//this.addressStr = addressStr;
		}

		@Override
		public void run() {
            Platform.runLater(() -> {
				//mainMenu.getStage().setIconified(true);
            	createDialog();
            });
		}
	}

	public void createDialog() {

		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		//dialog.initOwner(mainMenu.getStage());
		dialog.setTitle("Mars Simulation Project");
		dialog.setHeaderText("Multiplayer Client");
		dialog.setContentText("Enter your username and host : ");

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
		//ButtonType localHostButton = new ButtonType("localhost");
		//dialog.getDialogPane().getButtonTypes().addAll(localHostButton, loginButtonType, ButtonType.CANCEL);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField tfUser = new TextField();
		tfUser.setPromptText("Username");
		TextField tfHostAddress = new TextField();
		//PasswordField password = new PasswordField();
		tfHostAddress.setPromptText("192.168.xxx.xxx");
		//hostAddress.setText("192.168.xxx.xxx");
		Button localhostB = new Button("use localhost");

		grid.add(new Label("Username:"), 0, 0);
		grid.add(tfUser, 1, 0);
		grid.add(new Label("Host Address:"), 0, 1);
		grid.add(tfHostAddress, 1, 1);
		grid.add(localhostB, 2, 1);

		// Enable/Disable login button depending on whether a username was entered.
		Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		tfUser.textProperty().addListener((observable, oldValue, newValue) -> {
		    loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> tfUser.requestFocus());

		// Convert the result to a username/host address pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == loginButtonType) {
		        return new Pair<>(tfUser.getText(), tfHostAddress.getText());
		    }
		    return null;
		});

		localhostB.setOnAction(event -> {
			tfHostAddress.setText("127.0.0.1");
        });
		//localhostB.setPadding(new Insets(1));
		//localhostB.setPrefWidth(10);

		//Optional<ButtonType> hostButton = dialog.showAndWait();
		//if (hostButton.get() == localHostButton){
		//	hostAddress.setText("127.0.0.1");
		//}

		Optional<Pair<String, String>> result = dialog.showAndWait();

		result.ifPresent(input -> {
			String hostAddressStr = tfHostAddress.getText();
			this.hostAddressStr = hostAddressStr;
			playerName = tfUser.getText();
		    logger.info("User " + input.getKey() + " connecting to host at " + input.getValue());
		    //logger.info("Connecting to the host at " + hostAddressStr);

			   try {
				    makeContact(hostAddressStr);
				    sendRegister(); // obtain a client id
				    //createAlert(hostAddressStr);
					multiplayerTray = new MultiplayerTray(this);
					sendGetRecords(); // obtain the existing settlement list
					//mainMenu.runOne();

				} catch (Exception e) {
					e.printStackTrace();
				}
		});

	}

	/*
	 * Creates an alert to inform the user that the client can make a connection with the server
	 */
	public void createAlert(String header, String content) {
	    alert = new Alert(AlertType.INFORMATION);
	    alert.initModality(Modality.NONE);
		alert.setTitle("Mars Simulation Project");
		//alert.initOwner(mainMenu.getStage());
		alert.setHeaderText("Multiplayer Client");
		alert.setContentText(content); // "Connection verified with " + address
		alert.show();
	}

	/*
	 * Creates a stage for displaying commands and responses between host server and client
	 */
	private void createConnectionStage() {

	    stage = new Stage();
	    stage.setTitle("Client Connection Panel");

		stage.setOnCloseRequest(e -> {
			sendBye();
		});

		BorderPane b = new BorderPane();
	    ta = new TextArea();
	    ta.setEditable(false);
	    //ta.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
		javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(ta);
        scrollPane.setContent(ta);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(600);
        //scrollPane.setPrefHeight(400);
        b.setCenter(scrollPane);
	    //scrollPane.prefWidthProperty().bind(<parentControl>.prefWidthProperty());
	    //scrollPane.prefHeightProperty().bind(<parentConrol>.prefHeightProperty());
	    Label lName = new Label("Name: ");
	    tfName = new TextField();
	    tfName.setPromptText("Ismenius Lacus");
	    tfName.setPrefColumnCount(10);

	    Label lTemplate = new Label("Template: ");
	    tfTemplate = new TextField();
	    tfTemplate.setText("Mars Direct Base (phase 1)");
	    //tfTemplate.setPromptText("Utopia Base 1");
	    tfTemplate.setPrefColumnCount(15);

	    Label lPop = new Label("Population: ");
	    tfPop = new TextField();
	    tfPop.setPromptText("12");
	    tfPop.setPrefColumnCount(3);

	    Label lBots = new Label("Bots: ");
	    tfBots = new TextField();
	    tfBots.setPromptText("12");
	    tfBots.setPrefColumnCount(3);

	    Label lLat = new Label("Lat: ");
	    tfLat = new TextField();
	    lLat.setTooltip(new Tooltip("Enter Latitude with + being North and - being South"));
	    tfLat.setPromptText("-12.7");
	    tfLat.setPrefColumnCount(5);

	    Label lLong = new Label("Long: ");
	    lLong.setTooltip(new Tooltip("Enter Longitude with + being East and - being West"));
	    tfLong = new TextField();
	    tfLong.setPromptText("-10.5");
	    tfLong.setPrefColumnCount(5);

	    bRegister = new Button("Register");
	    bRegister.setOnAction(e -> sendRegister());
	    bRegister.setPadding(new Insets(5, 5, 5, 5));

	    bCreateNew = new Button("Create New");
	    bCreateNew.setTooltip(new Tooltip("Fill in all textfield above to create a new settlement"));
	    bCreateNew.setOnAction(e -> createNew());
	    bCreateNew.setPadding(new Insets(5, 5, 5, 5));

	    bGetRecords = new Button("Get Records");
	    bGetRecords.setOnAction(e -> sendGetRecords());
	    bGetRecords.setPadding(new Insets(5, 5, 5, 5));

	    HBox hb1 = new HBox(15);
	    hb1.setPadding(new Insets(5, 5, 5, 5));
	    hb1.setAlignment(Pos.CENTER);
	    HBox hb2 = new HBox(15);
	    hb2.setPadding(new Insets(5, 5, 5, 5));
	    hb2.setAlignment(Pos.CENTER);
	    HBox hb3 = new HBox(15);
	    hb3.setPadding(new Insets(5, 5, 5, 5));
	    hb3.setAlignment(Pos.CENTER);
	    VBox vb = new VBox(15);
	    b.setBottom(vb);
	    vb.getChildren().addAll(hb1, hb2, hb3);
	    vb.setPadding(new Insets(5, 5, 5, 5));
		vb.setAlignment(Pos.CENTER);
	    hb1.getChildren().addAll(lName, tfName, lTemplate, tfTemplate);
	    hb2.getChildren().addAll(lPop, tfPop, lBots, tfBots, lLat, tfLat, lLong, tfLong);
	    hb3.getChildren().addAll(bRegister, bCreateNew, bGetRecords);
	    //hb3.setSpacing(10.0);

	    Scene scene = new Scene(b);
	    stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toString()));
	    stage.setScene(scene);
	    stage.show();
	  }

	/*
	 * Closes sockets to terminate contact with the server
	 */
	  private void sendBye() {
	    try {
	      out.println("bye " + clientID);    // tell server that client is disconnecting
	      //out.close();		// not working for threaded server
	      sock.close();
	    }
	    catch(Exception e)
	    { e.printStackTrace();}

	    System.exit( 0 );
	  }

	/*
	 * Opens sockets to initiate contact with the server
	 */
	private void makeContact(String address) {
	    try {

          initSocketConnection();

          if (sock != null && sock.isConnected()) {
		      in  = new BufferedReader( new InputStreamReader( sock.getInputStream() ) );
		      out = new PrintWriter( sock.getOutputStream(), true );  // autoflush
          }
          /*
           * Notify SocketReaderThread that it can now start.
           */
          //notifyReady();

	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	        /*
	         * This will notify the SocketReaderThread that it should exit.
	         */
	        //notifyReady();
        }
	  }



    /**
     * Initialize the SocketClient up to and including issuing the accept()
     * method on its socketConnection.
     * @throws java.net.SocketException
     */
    protected void initSocketConnection() throws SocketException {
        try {
            sock = new Socket();
            /*
             * Allows the socket to be bound even though a previous
             * connection is in a timeout state.
             */
            sock.setReuseAddress(true);
            /*
             * Create a socket connection to the server
             */
            sock.connect(new InetSocketAddress(hostAddressStr, PORT));
            //if (debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
            //    System.out.println("Connected to " + host
            //            + "at port " + port);
            //}
        } catch (IOException e) {
            //if (debugFlagIsSet(Constants.instance().DEBUG_EXCEPTIONS)) {
                e.printStackTrace();
            //}
            throw new SocketException();
        }
    }

	/*
	 * Responds to pressing either "Send New" or "Get Records" buttons

	public void actionPerformed(ActionEvent e) {
	     if (e.getSource() == bGetRecords)
	    	 sendGetRecords();
	     else if (e.getSource() == bSendNew)
	    	 sendNew();
	     else if (e.getSource() == bRegister)
	    	 sendRegister();
	   }
*/


	/*
	 * Creates a new settlement and sends "new ..." to server
	 */
	private void createNew() {
		String playerName = this.playerName;
		String id = clientID + "";
	    String name = tfName.getText().trim();
	    String template = tfTemplate.getText().trim(); // "Mars Direct Base (phase 1)";
	    String pop = tfPop.getText().trim();
	    String bots = tfBots.getText().trim();
	    String lat = tfLat.getText().trim();
	    String lo = tfLong.getText().trim();

	    if ( playerName.equals("") | (name.equals("")) | (template.equals("")) | (pop.equals("")) | (bots.equals("")) | (lat.equals("")) | (lo.equals("")))
	    	createAlert("Input Error", "Please type in settlement data with the correct format.");
	    	//JOptionPane.showMessageDialog( null,  "No name/coordinates entered", "Send Error", JOptionPane.ERROR_MESSAGE);
	    else {
	      out.println("new " + playerName + " & " + id + " & " + name + " & " + template + " & " + pop + " & " + bots + " &" + lat + " & " + lo + " & ");
	      ta.appendText("Sent : " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop + " , " + bots + " , " + lat + " , " + lo + "\n");
	    }
	  }

	/*
	 * Updates the info of a settlement and sends "update ..." to server
	 */
	private void updateSettlement(SettlementRegistry s) {
		String playerName = s.getPlayerName();
		String id = s.getClientID() + "";
	    String name = s.getName();
	    String template = s.getTemplate();
	    String pop = s.getPopulation() + "";
	    String bots = s.getNumOfRobots() + "";
	    String lat = s.getLatitude() + "";
	    String lo = s.getLongitude() + "";

	      out.println("update " + playerName + " & " + id + " & " + name + " & " + template + " & " + pop + " & " + bots + " &" + lat + " & " + lo + " & ");
	      ta.appendText("Sent : update " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop + " , " + bots + " , " + lat + " , " + lo + "\n");
	  }

	/* Checks if the user types in a correct settlement name and coordinates
	 * If true, send "new name & lat & long &" to server
	 */
	public void sendNew(SettlementRegistry s) {
		String playerName = s.getPlayerName();
		String id = s.getClientID() + "";
	    String name = s.getName();
	    String template = s.getTemplate();
	    String pop = s.getPopulation() + "";
	    String bots = s.getNumOfRobots() + "";
	    String lat = s.getLatitude() + "";
	    String lo = s.getLongitude() + "";

	    if ( playerName.equals("") | name.equals("") | template.equals("") | pop.equals("") | bots.equals("") | lat.equals("") | lo.equals(""))
	    	createAlert("Send Error", "Please check on settlement data.");
	    	//JOptionPane.showMessageDialog( null, "No name/coordinates entered", "Send Error",JOptionPane.ERROR_MESSAGE);
	    else {
	      out.println("new " + playerName + " & " + id + " & " + name + " & " + template + " & " + pop + " & " + bots + " & " + lat + " & " + lo + " & ");
	      ta.appendText("Sent : new " + playerName + " , " +  id + " , " + name + " , " + template + " , " + pop + " , " + bots + " , " + lat + " , " + lo + "\n");
	    }
	  }

	/*
	 * Sends register command to host server to request a new client id
	 */
	public void sendRegister()  {
	     try {
	    	 out.println("register " + playerName);
	    	 logger.info("Sent : register " + playerName);
	    	 ta.appendText("Sent : register " + playerName + "\n");
	    	 String line = in.readLine();
	    	 if ((line.length() >= 7) &&     // "NEW ID "
	    			 (line.substring(0, 6).equals("NEW_ID"))) {
		    	 logger.info("Received : " + line);
	    		 showRegistryContent(NEW_ID, line.substring(6).trim() );
	    	 }
	    	 else  {   // should not happen but just in case
		    	 logger.info("[Unknown format] Received : " + line);
		    	 ta.appendText("[Unknown format] Received : " + line + "\n");
	    	 }
	     }
	     catch(Exception ex){
	    	 ta.appendText("Problem obtaining a new client id\n");
	    	 ex.printStackTrace();
	     }
	}


	   /* Sends out "get" command, read and display responses from server
	    * Note: normal response should be "RECORDS n1 & lat1 & long1 .... nN & latN & longN"
	    */
	   private void sendGetRecords()  {
	     try {
	    	 out.println("get");
	    	 logger.info("Sent : get");
	    	 ta.appendText("Sent : get\n");
	    	 String line = in.readLine();
	    	 if ((line.substring(0, 9).trim().equals("RECORDS 0"))) { // "RECORDS 0"
		    	 logger.info("Received : " + line);
	    		 ta.appendText("Received : " + line + "\n");
	    	 }
	    	 else if ((line.length() >= 8) &&     // "RECORDS "
	    		(line.substring(0, 7).equals("RECORDS"))) {
		    	 logger.info("Received : " + line);
	    		 showRegistryContent(RECORDS, line.substring(7).trim() );
	    	 }
	    	 else {   // should not happen but just in case
		    	 logger.info("[Unknown format] Received : " + line);
		    	 ta.appendText("[Unknown format] Received : " + line + "\n");
	    	 }
		     }
		     catch(Exception ex)
		     {
		       ta.appendText("Problem obtaining records\n");
			   ex.printStackTrace();
		     }
	   }

	/*
	 * 	Parses and displays the registry entries in a formatted view
	 */
	private void showRegistryContent(int type, String line) {
		if (type == NEW_ID) {
			clientID = Integer.parseInt(line);
		    try {
		    	ta.appendText("Received : " + playerName + "'s Player ID is "  + clientID + "\n");
		    	logger.info("Received : " + playerName + "'s Player ID is "  + clientID);
		    	bRegister.setDisable(true);
		    }
		    catch(Exception e) {
		      ta.appendText("Problem parsing client id\n");
		      logger.info("Parsing error with client id:");
		      e.printStackTrace();
		    }
		}
		else if (type == RECORDS) {
		    StringTokenizer st = new StringTokenizer(line, "&");
		    String playerName, name, template;
		    int i = 1;
		    int id, pop, bots;
		    double lat, lo;
		    try {
		    	String text = null;
			    while (st.hasMoreTokens()) {
			        playerName = st.nextToken().trim();
			    	id = Integer.parseInt( st.nextToken().trim() );
			        name = st.nextToken().trim();
			        template = st.nextToken().trim();
			        pop = Integer.parseInt( st.nextToken().trim() );
			        bots = Integer.parseInt( st.nextToken().trim() );
			        lat = Double.parseDouble( st.nextToken().trim() );
			        lo = Double.parseDouble( st.nextToken().trim() );
			        // Add the new entry into the array
			        settlementList.clear();
			        settlementList.add(new SettlementRegistry(playerName, id, name, template, pop, bots, lat, lo));
			        text = "(" + i + "). " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop + " , " + bots
			        	+ " , " + lat + " , " + lo;
			        i++;
			    }
			    ta.appendText("Received :\n" + text + "\n");
			    logger.info("Received : " + text);
		    }
		    catch(Exception e) {
		      ta.appendText("Problem parsing records\n");
		      logger.info("Parsing error with records:" + e);
		      e.printStackTrace();
		    }
		}
	}


	public int getClientID() {
		return clientID;
	}

	public String getPlayerName() {
		return playerName;
	}

	public int getNumSettlement() {
		int result = 0;
		try {
		   out.println("s");
		   logger.info("Sent : s");
		   ta.appendText("Sent : s\n");
		   String line = in.readLine();
		   if ( line.trim().equals("SETTLEMENTS 0") ) {
			   result = 0;
			   logger.info("Received : SETTLEMENTS 0");
			   ta.appendText("Received : SETTLEMENTS 0\n");
		   }
		   else if ((line.length() >= 12) &&     // "SETTLEMENTS "
	    			 (line.substring(0, 11).equals("SETTLEMENTS"))) {
			   result = Integer.parseInt(line.substring(11).trim());
			   logger.info("Received : " + line);
			   ta.appendText("Received : "+ result + "\n");
		   }
		} catch(Exception ex) {
			logger.info("Problem obtaining records");
			ta.appendText("Problem obtaining records\n");
			ex.printStackTrace();
		}

		return result;
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

	public List<SettlementRegistry> getSettlementRegistryList() {
		return settlementList;
	}

	public void destroy() {
		sock= null;
		in= null;
		out= null;
		ta= null;
		tfName= null;
		tfLat= null;
		tfLong= null;
		bGetRecords= null;
		bCreateNew= null;
		//mainMenu= null;
		clientTask= null;
		multiplayerTray= null;
		alert= null;
	}
}
