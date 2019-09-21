/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.networking;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.SimpleEvent;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;
import org.mars_sim.network.ClientRegistry;
import org.mars_sim.network.Receiver;
import org.mars_sim.network.SettlementRegistry;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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
import javafx.scene.image.ImageView;
import javafx.scene.control.ButtonType;

/**
 * The MultiplayerClient class allows the computer to take on the client role.
 */
public class MultiplayerClient implements UnitListener, HistoricalEventListener, UnitManagerListener {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerClient.class.getName());

	private static final int RECORDS = 0;
	private static final int NEW_ID = 1;

	private static final int PORT = 9090;
	private static final int PORT_CHAT = 9876;

	//private static final int TIME_DELAY = 60 * 1000; // in miliseconds

	private static final String MULTICAST_ADDRESS = "ff02::fb"; // FF02:0:0:0:0:0:0:2";
																// //
																// ff01::114";
																// //224.0.0.7";
	// private static final String LOCALHOST = "localhost";

	private int clientID = 0;
	private boolean ready = false;

	private String serverAddressStr;
	private String clientAddressStr;
	private String playerName;
	InetAddress iadr;
	MulticastSocket so;

	// private List<String> addresses = new ArrayList<>();
	private static MultiplayerClient instance = null;

	private Stage stage;
	private Socket sock;
	private BufferedReader in; // i/o for the client
	private PrintWriter out;

	private TextArea ta;
	private TextField tfChat, tfName, tfTemplate, tfPop, tfBots, tfLat, tfLong;
	private Button bGetRecords;
	private Button bCreateNew;
	private Button bRegister;

	private MainMenu mainMenu;
	private Timeline timeline;
	private ClientTask clientTask;
	private MultiplayerTray multiplayerTray;
	private Alert alert;
	private transient ThreadPoolExecutor clientExecutor;

	private static List<SettlementRegistry> settlementList;

	/*
	 * Method 1 : ThreadSafe Singleton Pattern: (Lazy Initialization + ThreadSafe
	 * with synchronized block) This implementation using the synchronized
	 * keyword to make the traditional approach thread-safe.
	 *
	 * http://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-with-examples
	 * Note we need it only for the first few threads who might create the separate instances.
	 * To avoid this extra overhead every time, double checked locking principle is used.
	 * In this approach, the synchronized block is used inside the if condition with an additional check
	 * to ensure that only one instance of singleton class is created.
	*/
	 //requires private static MultiplayerClient instance = null;

	 protected MultiplayerClient() { }

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



	/*
	 * Method 3: Lazy Creation of Singleton ThreadSafe Instance without Using
	 * Synchronized Keyword. This implementation relies on the well-specified
	 * initialization phase of execution within the Java Virtual Machine (JVM).
	 * see http://crunchify.com/lazy-creation-of-singleton-threadsafe-instance-
	 * without-using-synchronized-keyword/
	 */
	//private MultiplayerClient() {
	//}

	//private static class HoldInstance {
	//	 private static final MultiplayerClient INSTANCE = new MultiplayerClient();
	//}

	//public static MultiplayerClient getInstance() {
	//	return HoldInstance.INSTANCE;
	//}



	/*
	 * Method 2 : Auto ThreadSafe Singleton Pattern using Object This
	 * implementation is more optimized than others since the need for checking
	 * the value of the Singleton instance ( i.e. instance == null ) is
	 * eliminated
	 *
	 * private static final Object instance = new Object();
	 *
	 * protected MultiplayerClient() { }
	 *
	 * public static Object getInstance() { return instance; }
	 */

	public void runClient(MainMenu mainMenu) {

		this.mainMenu = mainMenu;

		new ClientRegistry();
		settlementList = ClientRegistry.getSettlementList();

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
		} );

		clientTask = new ClientTask();// clientAddressStr);
		clientExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
		clientExecutor.execute(clientTask);
	}

	public void setUserName(String name) {
		playerName = name;
	}

	public String getAddressStr() {
		return serverAddressStr;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	public ClientTask getClientTask() {
		return clientTask;
	}

	/*
	 * Creates the task that will prompt users to create a dialog box for
	 * choosing the host server's IP address
	 */
	class ClientTask implements Runnable {

		// private String addressStr;

		private ClientTask() { // String addressStr) {
			// this.addressStr = addressStr;
		}

		@Override
		public void run() {
			Platform.runLater(() -> {
				mainMenu.getStage().setIconified(true);
				connectDialog();
			} );
		}
	}

	public void connectDialog() {

		// Create the custom dialog.
		Dialog<String> dialog = new Dialog<>();
		// Get the Stage.
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
/*		
    	stage.setOnCloseRequest(e -> {
			boolean isExit = mainMenu.exitDialog(stage);//.getScreensSwitcher().exitDialog(stage);
			if (!isExit) {
				e.consume();
			}
			else {
				Platform.exit();
			}
		});
*/
		// Add corner icon.
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/network/client48.png").toString()));
		// Add Stage icon
		dialog.setGraphic(new ImageView(this.getClass().getResource("/icons/network/network256.png").toString()));
		// dialog.initOwner(mainMenu.getStage());
		dialog.setTitle("Mars Simulation Project");
		dialog.setHeaderText("Multiplayer Client");
		dialog.setContentText("Enter the host address : ");

		// Set the button types.
		ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
		// ButtonType localHostButton = new ButtonType("localhost");
		// dialog.getDialogPane().getButtonTypes().addAll(localHostButton,
		// loginButtonType, ButtonType.CANCEL);
		dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		// TextField tfUser = new TextField();
		// tfUser.setPromptText("Username");
		TextField tfServerAddress = new TextField();
		// PasswordField password = new PasswordField();
		tfServerAddress.setPromptText("192.168.xxx.xxx");
		tfServerAddress.setText("192.168.xxx.xxx");
		Button localhostB = new Button("Use localhost");

		// grid.add(new Label("Username:"), 0, 0);
		// grid.add(tfUser, 1, 0);
		grid.add(new Label("Server Address:"), 0, 1);
		grid.add(tfServerAddress, 1, 1);
		grid.add(localhostB, 2, 1);

		// Enable/Disable connect button depending on whether the host address
		// was entered.
		Node connectButton = dialog.getDialogPane().lookupButton(connectButtonType);
		connectButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		tfServerAddress.textProperty().addListener((observable, oldValue, newValue) -> {
			connectButton.setDisable(newValue.trim().isEmpty());
		} );

		dialog.getDialogPane().setContent(grid);

		// Request focus on the username field by default.
		Platform.runLater(() -> tfServerAddress.requestFocus());

		// Convert the result to a username-password-pair when the login button
		// is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == connectButtonType) {
				return tfServerAddress.getText();
			}
			return null;
		} );

		localhostB.setOnAction(event -> {
			tfServerAddress.setText("127.0.0.1");
		} );
		// localhostB.setPadding(new Insets(1));
		// localhostB.setPrefWidth(10);

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(input -> {
			String serverAddressStr = tfServerAddress.getText();
			this.serverAddressStr = serverAddressStr;
			logger.info("Connecting to server at " + serverAddressStr);
			loginDialog();
		} );

	}

	public void loginDialog() {

		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		// Get the Stage.
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		// Add corner icon.
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/network/login32.png").toString()));
/*
		stage.setOnCloseRequest(e -> {
				boolean isExit = mainMenu.exitDialog(stage);//getScreensSwitcher().exitDialog(stage);
				if (!isExit) {
					e.consume();
				}
				else {
					Platform.exit();
				}
		});
*/		
		// Add Stage icon
		dialog.setGraphic(new ImageView(this.getClass().getResource("/icons/network/login256.png").toString()));
		// dialog.initOwner(mainMenu.getStage());
		dialog.setTitle("Mars Simulation Project");
		dialog.setHeaderText("Log in");
		dialog.setContentText("Enter your username and password : ");
		//dialog.initModality(Modality.NONE);
		dialog.initModality(Modality.APPLICATION_MODAL);
		//dialog.setOnCloseRequest((event) -> stage.close());

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
		// ButtonType localHostButton = new ButtonType("localhost");
		// dialog.getDialogPane().getButtonTypes().addAll(localHostButton,
		// loginButtonType, ButtonType.CANCEL);
		dialog.getDialogPane().getButtonTypes().add(loginButtonType);// , ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField tfPlayer = new TextField();
		tfPlayer.setPromptText("e.g. m03j");
		// TextField tfPassword = new TextField();
		PasswordField tfPassword = new PasswordField();
		tfPassword.setPromptText("xxxx");

		Button defaultPWB = new Button("Use Default");
		Button guestB = new Button("As Guest");

		defaultPWB.setOnAction(event -> {
			tfPassword.setText("msp0");
		} );

		guestB.setOnAction(event -> {
			tfPlayer.setText("Guest_");
			tfPassword.setText("msp0");
		} );

		grid.add(new Label("Player Name :"), 0, 0);
		grid.add(tfPlayer, 1, 0);
		grid.add(guestB, 2, 0);
		grid.add(new Label("Password :"), 0, 1);
		grid.add(tfPassword, 1, 1);
		grid.add(defaultPWB, 2, 1);

		// Enable/Disable login button depending on whether a username was
		// entered.
		Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(true);

		// Do some validation (using the Java 8 lambda syntax).
		tfPlayer.textProperty().addListener((observable, oldValue, newValue) -> {
			loginButton.setDisable(newValue.trim().isEmpty());
		} );

		dialog.getDialogPane().setContent(grid);

		// Request focus on the player name field by default.
		Platform.runLater(() -> tfPlayer.requestFocus());

		// Convert the result to a player name /host address pair when the login
		// button is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return new Pair<>(tfPlayer.getText(), tfPassword.getText());
			}
			return null;
		} );

		dialog.show();

		loginButton.addEventFilter(EventType.ROOT,  e-> {
			if(e.getEventType().equals(ActionEvent.ACTION)) {

				e.consume();

				playerName = tfPlayer.getText();
				logger.info("Player " + tfPlayer.getText() + " connecting to server at " + serverAddressStr);
				// logger.info("Connecting to the host at " + hostAddressStr);

			try {
				makeContact(serverAddressStr);
				// obtain a client id
				boolean isSuccessful = sendRegister();

				if (isSuccessful) {
					dialog.close();
					// establish chat
					iadr = InetAddress.getByName(MULTICAST_ADDRESS);
					so = new MulticastSocket(PORT_CHAT);
					so.joinGroup(iadr);
					new Receiver(so, ta);
					sendChatText("Online");

					// createAlert(hostAddressStr);
					multiplayerTray = new MultiplayerTray(this);
					sendGetRecords(); // obtain the existing settlement list
					// startTimer();
					mainMenu.runNew(false, false);
					// prepareListeners(); // to be run in
					// ScenarioConfigEditorFX
				} else {
					// shake the dialog or send an alert to inform the user the
					// player name is NOT valid
	                ShakeTransition anim = new ShakeTransition(dialog.getDialogPane(), t-> tfPlayer.requestFocus()); //dialog.close());
	                anim.playFromStart();
					//DialogEarthquakeCenter dec = new DialogEarthquakeCenter(dialog);
					//dec.startTimer();
					//dec.startNudging();
	                //new PauseTransition(Duration.seconds(2));
					//try {
					//	System.out.println("start sleeping ");
					//	Thread.sleep(2000);
					//	System.out.println("done sleeping ");
					//}	catch (InterruptedException e) {}
					//dialog.close();
					//loginDialog();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		//else // if cancel is pressed
			//loginDialog();
	});
	}

	/*
	 * Creates an alert to inform the user that the client can make a connection
	 * with the server
	 */
	public void createAlert(String header, String content) {
		alert = new Alert(AlertType.INFORMATION);
		alert.initModality(Modality.NONE);
		alert.setTitle("Mars Simulation Project");
		// alert.initOwner(mainMenu.getStage());
		alert.setHeaderText("Multiplayer Client");
		alert.setContentText(content); // "Connection verified with " + address
		alert.show();
	}

	/*
	 * Creates a stage for displaying commands and responses between host server
	 * and client
	 */
	private void createConnectionStage() {
		stage = new Stage();
		stage.setTitle("Client Connection Panel");
		//stage.setOnCloseRequest(e -> {
		//	sendBye();
		//});
/*		
	   	stage.setOnCloseRequest(e -> {
				boolean isExit = mainMenu.exitDialog(stage);//getScreensSwitcher().exitDialog(stage);
				if (!isExit) {
					e.consume();
				}
				else {
					sendBye();
					Platform.exit();
				}
		});
*/
		BorderPane b = new BorderPane();
		ta = new TextArea();
		ta.setEditable(false);
		// ta.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
		javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(ta);
		scrollPane.setContent(ta);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefWidth(600);
		// scrollPane.setPrefHeight(400);
		b.setCenter(scrollPane);
		// scrollPane.prefWidthProperty().bind(<parentControl>.prefWidthProperty());
		// scrollPane.prefHeightProperty().bind(<parentConrol>.prefHeightProperty());

		tfChat = new TextField();
		tfChat.setPromptText("Type here to chat and hit enter to send");
		tfChat.setPrefColumnCount(50);

		// Listen for TextField text changes
		/*
		 * tfChat.textProperty().addListener(new ChangeListener<String>() {
		 * public void changed(ObservableValue<? extends String> observable,
		 * String oldValue, String newValue) { ta.appendText(playerName + " : "
		 * + newValue + "\n"); sendMess(newValue); } });
		 */
		tfChat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String text = tfChat.getText();
				// ta.appendText(playerName + " : " + text + "\n");
				sendChatText(text);
				// tfChat.setText("");
				tfChat.clear();
			}
		});

		Label lName = new Label("Name: ");
		tfName = new TextField();
		tfName.setPromptText("Ismenius Lacus");
		tfName.setPrefColumnCount(10);

		Label lTemplate = new Label("Template: ");
		tfTemplate = new TextField();
		tfTemplate.setText("Mars Direct Base (phase 1)");
		// tfTemplate.setPromptText("Utopia Base 1");
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
		bCreateNew.setOnAction(e -> sendNew());
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

		vb.getChildren().addAll(tfChat, hb1, hb2, hb3);
		vb.setPadding(new Insets(5, 5, 5, 5));
		vb.setAlignment(Pos.CENTER);
		hb1.getChildren().addAll(lName, tfName, lTemplate, tfTemplate);
		hb2.getChildren().addAll(lPop, tfPop, lBots, tfBots, lLat, tfLat, lLong, tfLong);
		hb3.getChildren().addAll(bRegister, bCreateNew, bGetRecords);
		// hb3.setSpacing(10.0);

		Scene scene = new Scene(b);
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/network/lander_hab64.png").toString()));
		stage.setScene(scene);
		stage.show();
	}

	/*
	 * Closes sockets to terminate contact with the server
	 */
	private void sendBye() {
		try {
			// tell server that the player is disconnecting
			out.println("bye " + playerName);
			// out.close(); // not working for threaded server
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	/*
	 * Opens sockets to initiate contact with the server
	 */
	private void makeContact(String address) {
		try {

			initSocketConnection();

			if (sock != null && sock.isConnected()) {
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new PrintWriter(sock.getOutputStream(), true); // autoflush
			}
			/*
			 * Notify SocketReaderThread that it can now start.
			 */
			// notifyReady();

		} catch (Exception e) {
			e.printStackTrace();
			/*
			 * This will notify the SocketReaderThread that it should exit.
			 */
			// notifyReady();
		}
	}

	/**
	 * Initialize the SocketClient up to and including issuing the accept()
	 * method on its socketConnection.
	 *
	 * @throws java.net.SocketException
	 */
	protected void initSocketConnection() throws SocketException {
		try {
			sock = new Socket();
			/*
			 * Allows the socket to be bound even though a previous connection
			 * is in a timeout state.
			 */
			sock.setReuseAddress(true);
			/*
			 * Create a socket connection to the server
			 */
			sock.connect(new InetSocketAddress(serverAddressStr, PORT));
			// if (debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
			// System.out.println("Connected to " + host
			// + "at port " + port);
			// }
		} catch (IOException e) {
			// if (debugFlagIsSet(Constants.instance().DEBUG_EXCEPTIONS)) {
			e.printStackTrace();
			// }
			throw new SocketException();
		}
	}

	/*
	 * Responds to pressing either "Send New" or "Get Records" buttons
	 *
	 * public void actionPerformed(ActionEvent e) { if (e.getSource() ==
	 * bGetRecords) sendGetRecords(); else if (e.getSource() == bSendNew)
	 * sendNew(); else if (e.getSource() == bRegister) sendRegister(); }
	 */

	/*
	 * Creates a new settlement from user input and sends "new ..." to server
	 */
	private void sendNew() {
		String playerName = this.playerName;
		String id = clientID + "";
		String name = tfName.getText().trim();
		String template = tfTemplate.getText().trim(); // "Mars Direct Base
														// (phase 1)";
		String pop = tfPop.getText().trim();
		String bots = tfBots.getText().trim();
		String lat = tfLat.getText().trim();
		String lo = tfLong.getText().trim();

		if (playerName.equals("") | (name.equals("")) | (template.equals("")) | (pop.equals("")) | (bots.equals(""))
				| (lat.equals("")) | (lo.equals(""))) {
			createAlert("Input Error", "Please check on the typed settlement data.");
			logger.info("Cannot create the new settlement. Please check on the typed settlement data.");
		}
		else {
			out.println("new " + playerName + " & " + id + " & " + name + " & " + template + " & " + pop + " & " + bots
					+ " & " + lat + " & " + lo + " & ");
			ta.appendText("Sent : new " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop + " , "
					+ bots + " , " + lat + " , " + lo + "\n");
		}
	}

	/*
	 * Sends the info of the new settlement created in ScenarioConfigEditorFX to the host server
	 * @param settlementRegistry
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

		if (playerName.equals("") | name.equals("") | template.equals("") | pop.equals("") | bots.equals("")
				| lat.equals("") | lo.equals("")) {
			createAlert("Send Error", "Please check on settlement data.");
			logger.info("Cannot create the new settlement. Please check on the settlement data.");
		}
		else {
			out.println("new " + playerName + " & " + id + " & " + name + " & " + template + " & " + pop + " & " + bots
					+ " & " + lat + " & " + lo + " & ");
			ta.appendText("Sent : new " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop
					+ " , " + bots + " , " + lat + " , " + lo + "\n");
		}
	}

	/*
	 * Creates a new settlement from the simulation and sends "new ..." to
	 * server
	 */
	private void sendNewInSimulation(Settlement s) {
		String playerName = this.playerName;
		String id = clientID + "";
		String name = s.getName();
		String template = s.getTemplate();
		String pop = s.getNumCitizens() + "";
		String bots = s.getNumBots() + "";
		String lat = Math.round(s.getCoordinates().getLatitudeDouble() * 10.0)/10.0 + ""; //.getPhi()
		String lo = Math.round(s.getCoordinates().getLongitudeDouble() * 10.0)/10.0 + "";

		if (playerName.equals("") | (name.equals("")) | (template.equals("")) | (pop.equals("")) | (bots.equals(""))
				| (lat.equals("")) | (lo.equals(""))) {
			createAlert("Input Error", "Please check on the settlement data.");
		logger.info("Cannot create the new settlement. Please check on the settlement data.");
		}
		else {
			out.println("new " + playerName + " & " + id + " & " + name + " & " + template + " & " + pop + " & " + bots
					+ " & " + lat + " & " + lo + " & ");
			ta.appendText("Sent : new " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop + " , "
					+ bots + " , " + lat + " , " + lo + "\n");
		}
	}

	/*
	 * Removes a settlement from the simulation and sends "new ..." to server
	 */
	private void removeOld(Settlement s) {
		String playerName = this.playerName;
		String id = clientID + "";
		String name = s.getName();


		Iterator<SettlementRegistry> i = settlementList.iterator();
			while (i.hasNext()) {
				SettlementRegistry ss = i.next(); // must be called before you can call i.remove()
				if (ss.getPlayerName().equals(playerName) && ss.getName().equals(name)) {
					if (playerName.equals("") | (name.equals("")))
						createAlert("remove error", "Please ensure the settlement info are correct.");
					else {
						out.println("remove " + playerName + " & " + id + " & " + name + " & ");
						ta.appendText("Sent : remove " + playerName + " , " + id + " , " + name);
					}
					i.remove();
				}
			}

/*
		settlementList.forEach(ss -> {
			if (ss.getPlayerName().equals(playerName) && ss.getName().equals(name)) {

				if (playerName.equals("") | (name.equals("")))
					createAlert("remove error", "Please ensure the settlement info are correct.");
				else {
					out.println("remove " + playerName + " & " + id + " & " + name + " & ");
					ta.appendText("Sent : remove " + playerName + " , " + id + " , " + name);
				}
			}
		} );
*/
	}

	/*
	 * Updates the info of a settlement and sends "update ..." to server
	 */
	private void sendUpdate(SettlementRegistry s) {
		String playerName = s.getPlayerName();
		String id = s.getClientID() + "";
		String name = s.getName();
		String template = s.getTemplate();
		String pop = s.getPopulation() + "";
		String bots = s.getNumOfRobots() + "";
		String lat = s.getLatitude() + "";
		String lo = s.getLongitude() + "";

		out.println("update " + playerName + " & " + id + " & " + name + " & " + template + " & " + pop + " & " + bots
				+ " &" + lat + " & " + lo + " & ");
		ta.appendText("Sent : update " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop + " , "
				+ bots + " , " + lat + " , " + lo + "\n");
	}

	/*
	 * Sends register command to host server to request a new client id
	 */
	public boolean sendRegister() {
		boolean isSuccessful = false;
		try {
			out.println("register " + playerName);
			logger.info("Sent : register " + playerName);
			ta.appendText("Sent : register " + playerName + "\n");
			String line = in.readLine();
			if ((line.length() >= 7) && // "NEW ID "
					(line.substring(0, 6).equals("NEW_ID"))) {
				logger.info("Received : " + line);
				showRegistryContent(NEW_ID, line.substring(6).trim());
				isSuccessful = true;
			} else { // this playerName has been taken
				logger.info(line);
				ta.appendText(line + "\n");
				isSuccessful = false;
				// logger.info("[Unknown format] Received : " + line);
				// ta.appendText("[Unknown format] Received : " + line + "\n");
			}
		} catch (Exception ex) {
			ta.appendText("Problem obtaining a new client id\n");
			ex.printStackTrace();
		}

		return isSuccessful;
	}

	/*
	 * Sends out "get" command, read and display responses from server Note:
	 * normal response should be
	 * "RECORDS n1 & lat1 & long1 .... nN & latN & longN"
	 */
	private void sendGetRecords() {
		try {
			out.println("get");
			logger.info("Sent : get");
			ta.appendText("Sent : get\n");
			String line = in.readLine();
			if ((line.substring(0, 9).trim().equals("RECORDS 0"))) { // "RECORDS
																		// 0"
				logger.info("Received : " + line);
				ta.appendText("Received : " + line + "\n");
			} else if ((line.length() >= 8) && // "RECORDS "
					(line.substring(0, 7).equals("RECORDS"))) {
				logger.info("Received : " + line);
				showRegistryContent(RECORDS, line.substring(7).trim());
			} else { // should not happen but just in case
				logger.info("[Unknown format] Received : " + line);
				ta.appendText("[Unknown format] Received : " + line + "\n");
			}
		} catch (Exception ex) {
			ta.appendText("Problem obtaining records\n");
			ex.printStackTrace();
		}
	}

	/*
	 * Parses and displays the registry entries in a formatted view
	 */
	private void showRegistryContent(int type, String line) {
		if (type == NEW_ID) {
			clientID = Integer.parseInt(line);
			try {
				ta.appendText("Received : " + playerName + "'s Player ID is " + clientID + "\n");
				logger.info("Received : " + playerName + "'s Player ID is " + clientID);
				bRegister.setDisable(true);
			} catch (Exception e) {
				ta.appendText("Problem parsing client id\n");
				logger.info("Parsing error with client id:");
				e.printStackTrace();
			}
		} else if (type == RECORDS) {
			// clear the existing list
			//settlementList.clear();
			if (settlementList != null)
				ClientRegistry.clearSettlementList();
			StringTokenizer st = new StringTokenizer(line, "&");
			String playerName, name, template, sponsor;
			int i = 0;
			int id, pop, bots;
			double lat, lo;
			try {
				String text = null;
				ta.appendText("Received :\n");
				while (st.hasMoreTokens()) {
					playerName = st.nextToken().trim();
					id = Integer.parseInt(st.nextToken().trim());
					name = st.nextToken().trim();
					template = st.nextToken().trim();
					pop = Integer.parseInt(st.nextToken().trim());
					bots = Integer.parseInt(st.nextToken().trim());
					sponsor = st.nextToken().trim();
					lat = Double.parseDouble(st.nextToken().trim());
					lo = Double.parseDouble(st.nextToken().trim());
					// Add an entry into the list
					settlementList.add(new SettlementRegistry(playerName, id, name, template, pop, bots, sponsor, lat, lo));
					// System.out.println("settlementList.size() is now " +
					// settlementList.size());
					int num = i + 1;
					text = "(" + num + "). " + playerName + " , " + id + " , " + name + " , " + template + " , " + pop
							+ " , " + bots + " , " + lat + " , " + lo;
					ta.appendText(text + "\n");
					i++;
				}
			} catch (Exception e) {
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
			if (line.trim().equals("SETTLEMENTS 0")) {
				result = 0;
				logger.info("Received : SETTLEMENTS 0");
				ta.appendText("Received : SETTLEMENTS 0\n");
			} else if ((line.length() >= 12) && // "SETTLEMENTS "
					(line.substring(0, 11).equals("SETTLEMENTS"))) {
				result = Integer.parseInt(line.substring(11).trim());
				logger.info("Received : " + line);
				ta.appendText("Received : " + result + "\n");
			}
		} catch (Exception ex) {
			logger.info("Problem obtaining records");
			ta.appendText("Problem obtaining records\n");
			ex.printStackTrace();
		}

		return result;
	}

	/*
	 * Synchronized method set up to wait until the SetupThread is sufficiently
	 * initialized. When notifyReady() is called, waiting will cease.
	 *
	 * private synchronized void waitForReady() { while (!ready) { try { wait();
	 * } catch (InterruptedException e) { } } }
	 */
	/*
	 * Synchronized method responsible for notifying waitForReady() method that
	 * it's OK to stop waiting.
	 *
	 * private synchronized void notifyReady() { ready = true; notifyAll(); }
	 */

	/**
	 * Sends chat text
	 *
	 * @param text
	 */
	public void sendChatText(String text) {
		byte[] data = null;

		if (playerName != null) {

			if (text.equals("Online"))
				data = ("[Status Update] : " + playerName + " is now online").getBytes();
			else
				data = (playerName + " : " + text).getBytes();

			DatagramPacket packet = new DatagramPacket(data, data.length, iadr, PORT_CHAT);

			try {
				so.send(packet);
			} catch (IOException ie) {
				Toolkit.getDefaultToolkit().beep();
				// JOptionPane.showMessageDialog(null, "Data overflow !");
				logger.info("Data overflow error. Cannot send chat text");
			}
		} else
			logger.info("Cannot send until registering with an valid player name.");
	}

	public List<SettlementRegistry> getSettlementRegistryList() {
		return settlementList;
	}

	/**
	 * Checks if a player has any activity within a predefined period of time

	public void checkPlayerActivity() {
		String startTime = "10:00";
		// String endTime = "12:00";
		// if (startTime == null )
		// ; // tag the playername with currentTime
		// else {
		String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;

		try {
			d1 = sdf.parse(startTime);
			d2 = sdf.parse(currentTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long elapsed = d2.getTime() - d1.getTime();
		// check which player has a time tag older than TIME_DELAY ago
		// kick the player off from the idMap and addressMap
		if (Math.abs(elapsed) > TIME_DELAY)
			; // unregister player, clear idMap, addressMap and timeTagMap, send
				// msg
	}
*/

	/**
	 * Creates and starts the timer
	 *
	 * @return Scene

	public void startTimer() {
		// Set up earth time text update
		timeline = new Timeline(new KeyFrame(Duration.millis(TIME_DELAY), ae -> checkPlayerActivity()));
		timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
		timeline.play();
	}
*/

	/**
	 * Sets up this class with two listeners
*/
	// 2015-04-22 Added prepareListeners()
	public void prepareListeners() {

		// Add addUnitManagerListener()
		UnitManager unitManager = Simulation.instance().getUnitManager();
		unitManager.addUnitManagerListener(this);

		// Add addUnitListener()
		Collection<Settlement> settlements = unitManager.getSettlements();
		//List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
		//settlementList.get(0).addUnitListener(this);

		// Iterator<Settlement> i = settlementList.iterator();
		// while (i.hasNext()) {
		// i.next().addUnitListener(this);
		// }
		settlements.forEach(s -> {
			s.addUnitListener(this);
		} );
	}

	@Override
	public void eventAdded(int index, SimpleEvent se, HistoricalEvent he) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventsRemoved(int startIndex, int endIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unitUpdate(UnitEvent event) {
		boolean hasChanged = false;
		Settlement settlement = null;
		UnitEventType eventType = event.getType();
		Object target = event.getTarget();
		//addUnit((Unit) event.getTarget());
		if (eventType == UnitEventType.ADD_ASSOCIATED_PERSON_EVENT) {
			//Person person = (Person) event.getSource();
			//System.out.println("MultiplayerClient : unitUpdate() : ADD_ASSOCIATED_PERSON_EVENT");
			settlement = ((Person) target).getAssociatedSettlement();
			hasChanged = true;
		} else if (eventType == UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT) {
			//Person person = (Person) event.getSource();
			//System.out.println("MultiplayerClient : unitUpdate() : REMOVE_ASSOCIATED_PERSON_EVENT");
			settlement = ((Person) target).getBuriedSettlement();
			hasChanged = true;
		}
		if (eventType == UnitEventType.ASSOCIATED_SETTLEMENT_EVENT) {
			//System.out.println("MultiplayerClient : unitUpdate() : ASSOCIATED_SETTLEMENT_EVENT");
			settlement = (Settlement) target;
			hasChanged = true;
		}
		if (hasChanged) {
			//System.out.println("MultiplayerClient : unitUpdate() : hasChanged is true");
			int n = settlement.getNumCitizens();
			String name = settlement.getName();
			sendGetRecords();
			//System.out.println(playerName + " "  + name);
			settlementList.forEach(s -> {
				//System.out.println(s.getPlayerName() + " "  + s.getName());
				if (s.getPlayerName().equals(playerName) && s.getName().equals(name)) {
					s.setPop(n);
					//System.out.println("MultiplayerClient : unitUpdate() : just called sendUpdate() : New population is now " + n);
					sendUpdate(s);
					sendGetRecords();
				}});

/*
			settlementList.parallelStream()
					.filter(s -> s.getPlayerName().contains(playerName) && s.getName().contains(name))
					.forEach(ss -> {
						ss.setPop(n);
						sendUpdate(ss);
						System.out.println("MultiplayerClient : unitUpdate() : just called sendUpdate() : New population is now " + n);
					} );
*/
		}
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {
		Object unit = event.getUnit();
		if (unit instanceof Settlement) {
			Settlement settlement = (Settlement) unit;
			UnitManagerEventType eventType = event.getEventType();
			if (eventType == UnitManagerEventType.ADD_UNIT) { // REMOVE_UNIT;
				//System.out.println("MultiplayerClient : " + settlement.getName() + " just added");
				sendNewInSimulation(settlement);
				settlement.addUnitListener(this);
			} else if (eventType == UnitManagerEventType.REMOVE_UNIT) { // REMOVE_UNIT;
				//System.out.println("MultiplayerClient : " + settlement.getName() + " just deleted");
				removeOld(settlement);
				settlement.removeUnitListener(this);
			}
		}
	}

	public void destroy() {
		sock = null;
		in = null;
		out = null;
		ta = null;
		tfName = null;
		tfLat = null;
		tfLong = null;
		bGetRecords = null;
		bCreateNew = null;
		// mainMenu= null;
		clientTask = null;
		multiplayerTray = null;
		alert = null;
	}
}
