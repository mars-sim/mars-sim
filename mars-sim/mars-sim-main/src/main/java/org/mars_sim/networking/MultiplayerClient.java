/**
 * Mars Simulation Project
 * MultiplayerServerClient.java
 * @version 3.08 2015-04-13
 * @author Manny Kung
 */

package org.mars_sim.networking;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.mars_sim.msp.core.networking.SettlementRegistry;
import org.mars_sim.msp.javafx.MainMenu;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.util.Pair;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

/**
 * The MultiplayerClient class allows the computer to take on the client role.
 */
public class MultiplayerClient extends JFrame implements ActionListener {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MultiplayerClient.class.getName());

	private static final int RECORDS = 0;
	private static final int NEW_ID = 1;

	private static final int PORT = 9090;

	//private static final String LOCALHOST = "localhost";

	private int clientID = 0;

	private String hostAddressStr;
	private String clientAddressStr;

	private String playerName;

	private List<String> addresses = new ArrayList<>();

	private Socket sock;
	private BufferedReader in;     // i/o for the client
	private PrintWriter out;

	private JTextArea ta;
	private JTextField tfName, tfTemplate, tfPop, tfBots, tfLat, tfLong;
	private JButton bGetRecords;
	private JButton bSendNew;
	private JButton bRegister;
	Container c;

	private MainMenu mainMenu;
	private ModeTask modeTask;
	private MultiplayerTray multiplayerTray;
	private Alert alert;
	//private SettlementRegistry[] registryArray = new SettlementRegistry[CentralRegistry.MAX];
	private List<SettlementRegistry> settlementList;

	public MultiplayerClient(MainMenu mainMenu) throws IOException {
	    super( "Settlement Registry Client" );

		this.mainMenu = mainMenu;

		settlementList = new CopyOnWriteArrayList<>();

		InetAddress ip = InetAddress.getLocalHost();
		clientAddressStr = ip.getHostAddress();
		logger.info("Running the client at " + clientAddressStr + ". Waiting to connect to a host...");
		modeTask = new ModeTask();//clientAddressStr);

	    createPanel();

	    addWindowListener( new WindowAdapter() {
	       public void windowClosing(WindowEvent e) {
	    	   sendBye();
	    }});

	    //setState(Frame.ICONIFIED);
	    setSize(850,400);
	    setVisible(true);
	}

	public void setUserName(String name) {
		playerName = name;
	}

	public Container getContainer() {
		return c;
	}

	public String getAddressStr() {
		return hostAddressStr;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	public ModeTask getModeTask() {
		return modeTask;
	}

	/*
	 * Creates the task that will prompt users to create a dialog box for choosing the host server's IP address
	 */
	class ModeTask implements Runnable {

		//private String addressStr;

		private ModeTask() { //String addressStr) {
			//this.addressStr = addressStr;
		}

		@Override
		public void run() {
				Platform.runLater(() -> {
					//createClient();
					createDialog();
		        });
		}
	}

	/*
	 * Creates a dialog box for choosing the host server's IP address

	public void createClient() { //String addressStr) {

		addresses.add("127.0.0.1");

		ChoiceDialog<String> dialog = new ChoiceDialog<>("127.0.0.1", addresses);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(mainMenu.getStage());
		dialog.setTitle("Mars Simulation Project");
		dialog.setHeaderText("Multiplayer Client");
		//dialog.setContentText("Your IP: " + addressStr + ".\nChoose host : ");
		dialog.setContentText("Choose your host : ");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(address -> {
			logger.info("Connecting to the host at " + address);

		   try {
			    makeContact(address);
			    createAlert(address);
				multiplayerTray = new MultiplayerTray(this);

			} catch (Exception e) {
				e.printStackTrace();
			}
	   });
	}
*/

	public void createDialog() {
		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.initOwner(mainMenu.getStage());
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
				    //System.out.println("done with makeContact()");
				    sendRegister(); // obtain a client id
				    //System.out.println("done with sendRegister()");
				    //createAlert(hostAddressStr);
					multiplayerTray = new MultiplayerTray(this);

					sendGetRecords(); // obtain the existing settlement list

					mainMenu.runOne();

				} catch (Exception e) {
					e.printStackTrace();
				}
		});

	}

	/*
	 * Creates an alert to inform the user that the client can make a connection with the server
	 */
	public void createAlert(String address) {
	    alert = new Alert(AlertType.INFORMATION);
	    alert.initModality(Modality.NONE);
		alert.setTitle("Mars Simulation Project");
		alert.initOwner(mainMenu.getStage());
		alert.setHeaderText("Multiplayer Client");
		alert.setContentText("Connection verified with " + address);
		alert.show();
	}

	/*
	 * Creates a panel to download settlement registry and upload new settlement info
	 */
	private void createPanel() {
		    c = getContentPane();
		    c.setLayout( new BorderLayout() );

		    ta = new JTextArea(7, 7);
		    ta.setEditable(false);
		    JScrollPane jsp = new JScrollPane( ta);
		    c.add( jsp, "Center");

		    JLabel lName = new JLabel("Name: ");
		    tfName = new JTextField(10);

		    JLabel lTemplate = new JLabel("Template: ");
		    tfTemplate = new JTextField(15);

		    JLabel lPop = new JLabel("Population: ");
		    tfPop = new JTextField(3);

		    JLabel lBots = new JLabel("Bots: ");
		    tfBots = new JTextField(3);

		    JLabel lLat = new JLabel("Lat: ");
		    tfLat = new JTextField(5);

		    JLabel lLong = new JLabel("Long: ");
		    tfLong = new JTextField(5);

		    bRegister = new JButton("Register");
		    bRegister.addActionListener(this);

		    bSendNew = new JButton("Send New");
		    bSendNew.addActionListener(this);

		    bGetRecords = new JButton("Get Records");
		    bGetRecords.addActionListener(this);

		    JPanel p1 = new JPanel( new FlowLayout() );
		    p1.add(lName); p1.add(tfName);
		    p1.add(lTemplate); p1.add(tfTemplate);
		    p1.add(lPop); p1.add(tfPop);
		    p1.add(lBots); p1.add(tfBots);
		    p1.add(lLat); p1.add(tfLat);
		    p1.add(lLong); p1.add(tfLong);

		    JPanel p2 = new JPanel( new FlowLayout() );
		    p2.add(bRegister);
		    p2.add(bSendNew);
		    p2.add(bGetRecords);

		    JPanel p = new JPanel();
		    p.setLayout( new BoxLayout(p, BoxLayout.Y_AXIS));
		    p.add(p1); p.add(p2);

		    c.add(p, "South");

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
	      sock = new Socket(address, PORT);
		    //System.out.println("done with sock in makeContact()");
	      in  = new BufferedReader( new InputStreamReader( sock.getInputStream() ) );
		    //System.out.println("done with in in makeContact()");
	      out = new PrintWriter( sock.getOutputStream(), true );  // autoflush
		    //System.out.println("done with out in makeContact()");
	    }
	    catch(Exception e)
	    { e.printStackTrace();}
	  }

	/*
	 * Responds to pressing either "Send New" or "Get Records" buttons
	 */
	public void actionPerformed(ActionEvent e) {
	     if (e.getSource() == bGetRecords)
	    	 sendGetRecords();
	     else if (e.getSource() == bSendNew)
	    	 sendNew();
	     else if (e.getSource() == bRegister)
	    	 sendRegister();
	   }

	/* Checks if the user types in a correct settlement name and coordinates
	 * If true, send "new name & lat & long &" to server
	 */
	private void sendNew() {
		String id = clientID + "";
	    String name = tfName.getText().trim();
	    String template = "Mars Direct Base (phase 1)";
	    String pop = tfPop.getText().trim();
	    String bots = tfBots.getText().trim();
	    String lat = tfLat.getText().trim();
	    String lo = tfLong.getText().trim();

	    if ((name.equals("")) | (template.equals("")) | (pop.equals("")) | (bots.equals("")) | (lat.equals("")) | (lo.equals("")))
	      JOptionPane.showMessageDialog( null,
	           "No name/coordinates entered", "Send Error",
				JOptionPane.ERROR_MESSAGE);
	    else {
	      out.println("new " + id + " & " + name + " & " + template + " & " + pop + " & " + bots + " &" + lat + " & " + lo + " & ");
	      ta.append("Sent :\n" + id + " , " + name + " , " + template + " , " + pop + " , " + bots + " , " + lat + " , " + lo + "\n\n");
	    }
	  }

	/* Checks if the user types in a correct settlement name and coordinates
	 * If true, send "new name & lat & long &" to server
	 */
	public void sendNew(SettlementRegistry s) {
		String id = s.getClientID() + "";
	    String name = s.getName();
	    String template = s.getTemplate();
	    String pop = s.getPopulation() + "";
	    String bots = s.getNumOfRobots() + "";
	    String lat = s.getLatitude() + "";
	    String lo = s.getLongitude() + "";

	    if ((name.equals("")) | (template.equals("")) | (pop.equals("")) | (bots.equals("")) | (lat.equals("")) | (lo.equals("")))
		      JOptionPane.showMessageDialog( null,
		           "No name/coordinates entered", "Send Error",
					JOptionPane.ERROR_MESSAGE);
	    else {
	      out.println("new " + id + " & " + name + " & " + template + " & " + pop + " & " + bots + " & " + lat + " & " + lo + " & ");
	      ta.append("Sent :\n" + id + " , " + name + " , " + template + " , " + pop + " , " + bots + " , " + lat + " , " + lo + "\n\n");
	    }
	  }

	/*
	 * Sends register command to host server to request a new client id
	 */
	public void sendRegister()  {
	     try {
	    	 logger.info("Sent : register " + playerName);
	    	 out.println("register " + playerName);
	    	 //System.out.println("sendRegister() : out is " + out);
	    	 String line = in.readLine();
	    	 //System.out.println("sendRegister() : line is " + line);
	    	 logger.info("Received : " + line);
	    	 if ((line.length() >= 7) &&     // "NEW ID "
	    			 (line.substring(0, 6).equals("NEW_ID")))
	    		 		showRegistryContent(NEW_ID, line.substring(6).trim() );
	    	 else    // should not happen but just in case
	    		 ta.append( line + "\n");
	     }
	     catch(Exception ex){
	    	 ta.append("Problem obtaining a new client id\n");
	    	 ex.printStackTrace();
	     }
	}


	   /* Sends out "get" command, read and display responses from server
	    * Note: normal response should be "RECORDS n1 & lat1 & long1 .... nN & latN & longN"
	    */
	   private void sendGetRecords()  {
	     try {
	    	 logger.info("Sent : get");
		       out.println("get");
		       String line = in.readLine();
			    logger.info("Received :\n" + line);
			   if ((line.substring(0, 9).trim().equals("RECORDS 0")))  // "RECORDS 0"
				   ta.append("Received : " + line + "\n\n");
			   else if ((line.length() >= 8) &&     // "RECORDS "
		           (line.substring(0, 7).equals("RECORDS")))
		         showRegistryContent(RECORDS, line.substring(7).trim() );
		       else    // should not happen but just in case
		         ta.append("Received : " + line + "\n\n");
		     }
		     catch(Exception ex)
		     {
		       ta.append("Problem obtaining records\n");
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
		    	ta.append("Sent : register\n\n");
		    	ta.append("Received : " + playerName + "'s Player ID is "  + clientID + "\n\n");
		    	bRegister.setEnabled(false);
		    }
		    catch(Exception e) {
		      ta.append("Problem parsing client id\n");
		      logger.info("Parsing error with client id:\n" + e);
		    }
		}
		else if (type == RECORDS) {
		    StringTokenizer st = new StringTokenizer(line, "&");
		    String name;
		    String template;
		    int i = 1;
		    int id, pop, bots;
		    double lat, lo;
		    try {
		    	ta.append("Sent : get\n\n");
		    	ta.append("Received :\n");
			    while (st.hasMoreTokens()) {
			    	id = Integer.parseInt( st.nextToken().trim() );
			        name = st.nextToken().trim();
			        template = st.nextToken().trim();
			        pop = Integer.parseInt( st.nextToken().trim() );
			        bots = Integer.parseInt( st.nextToken().trim() );
			        lat = Double.parseDouble( st.nextToken().trim() );
			        lo = Double.parseDouble( st.nextToken().trim() );
			        // Add the new entry into the array
			        settlementList.clear();
			        settlementList.add(new SettlementRegistry(id, name, template, pop, bots, lat, lo));
			        ta.append("(" + i + "). id : " + id + " , " + name + " , " + template + " , " + pop + " , " + bots
			        	+ " , " + lat + " , " + lo + "\n");
			        i++;
			    }
			    ta.append("\n");
		    }
		    catch(Exception e) {
		      ta.append("Problem parsing records\n");
		      logger.info("Parsing error with records:\n" + e);
				e.printStackTrace();
		    }
		}
	}


	public int getClientID() {
		return clientID;
	}

	public String getUsername() {
		return playerName;
	}

	public int getNumSettlement() {
		int result = 0;
		try {
			logger.info("Sent : s");
			ta.append("Sent : s\n\n");
		   out.println("s");
		   String line = in.readLine();
		    //logger.info("Received : " + line);
		   if ( line.trim().equals("SETTLEMENTS 0") ) {
			   result = 0;
			   ta.append("Received :SETTLEMENTS 0\n\n");
		   }
		   else if ((line.length() >= 12) &&     // "SETTLEMENTS "
	    			 (line.substring(0, 11).equals("SETTLEMENTS"))) {
			   result = Integer.parseInt(line.substring(11).trim());
			   ta.append("Received : "+ result + "\n\n");
			   logger.info("Received : " + line);
		   }
		} catch(Exception ex) {
			ta.append("Problem obtaining records\n");
			ex.printStackTrace();
		}

		return result;
	}


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
		bSendNew= null;
		mainMenu= null;
		modeTask= null;
		multiplayerTray= null;
		alert= null;
	}
}
