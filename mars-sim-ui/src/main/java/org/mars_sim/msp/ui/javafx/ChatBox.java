/* Mars Simulation Project
 * ChatBox.java
 * @version 3.1.0 2016-10-06
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationState;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.medical.Complaint;
import org.mars_sim.msp.core.person.medical.ComplaintType;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;

@SuppressWarnings("restriction")
public class ChatBox extends BorderPane {

	public final static String SYSTEM_PROMPT = "System : ";
	public final static String YOU_PROMPT = "You : ";
	public final static String REQUEST_HEIGHT_CHANGE = YOU_PROMPT + "I'd like to change the chat box height to ";
	public final static String REQUEST_HELP = YOU_PROMPT + "I need some help! What are the available keywords/commands ? ";
	public final static int[] box_height = new int[]{0,0,0,0};

	public final static String INSTRUCTION = " ***** INSTRUCTIONS ***** " + System.lineSeparator()
	+ "(1) First, type in the name of a person, bot, or settlement to connect with." + System.lineSeparator()
	+ "(2) OR, type 'settlement' to get an overview of the established settlements." + System.lineSeparator()
	+ "(2) Next, type in 1 to 15 (specific questions on a person/bot/settlement) or a keyword ['/k' or 'key']." + System.lineSeparator()
	+ "(3) 'bye', 'exit', 'quit', '/q', '/x', to close chat box" + System.lineSeparator()
	+ "(4) 'help', '/h' for guidance/help" + System.lineSeparator()
	+ "(5) '/y1' to reset height to 256 pixels (by default) after closing chat box. '/y2'->512 pixels, '/y3'->768 pixels, '/y4'->1024 pixels" + System.lineSeparator();

	public final static String HELP_TEXT = " ***** KEYWORDS ***** " + System.lineSeparator()
	+ "(1) 'where', 'location', 'located', 'task', 'activity', 'action', 'mission', "+ System.lineSeparator()
	+ "(2) 'bed', 'quarters', 'building', 'inside', 'outside',"+ System.lineSeparator()
	+ "(3) 'settlement', 'settlements', 'associated settlement', 'buried settlement', " + System.lineSeparator()
	+ "(4) 'vehicle inside', 'vehicle outside', 'vehicle park', 'vehicle settlement'," + System.lineSeparator()
	+ "(5) 'vehicle container', 'vehicle top container'" + System.lineSeparator()
	+ " ***** NUMERAL ***** " + System.lineSeparator()
	+ " 1 to 15 are specific questions on a person/bot/settlement" + System.lineSeparator()
	+ " *****  MISCS  ***** " + System.lineSeparator()
	+ "'bye', 'exit', 'quit', '/q', '/x', to close chat box" + System.lineSeparator()
	+ "'help', '/h' for guidance/help" + System.lineSeparator()
	+ "'/y1' to reset height to 256 pixels (by default) after closing chat box. '/y2'->512 pixels, '/y3'->768 pixels, '/y4'->1024 pixels" + System.lineSeparator();

    protected int historyPointer = 0;

    protected boolean hasPerson = false, reset = false;

    protected String image = MainScene.class.getResource("/images/starfield.png").toExternalForm();

    protected Label titleLabel;

    protected Person personCache;
    protected Robot robotCache;
    protected Settlement settlementCache;

    protected Vehicle vehicle;
    protected Settlement settlement;
    protected Building building;
    protected Equipment equipment;
    protected MainScene mainScene;

    protected final TextArea textArea;
    protected final JFXButton broadcastButton;
    //protected final TextField textField = new TextField();
    protected final AutoFillTextBox<String> autoFillTextBox;

    protected final List<String> history = new ArrayList<>();

    private Consumer<String> onMessageReceivedHandler;

    /**
     * Constructor for ChatBox
     */
    public ChatBox(MainScene mainScene) {
    	this.mainScene = mainScene;
    	//this.setHeight(height);
    	//super.setHeight(height);

    	//this.setStyle("-fx-background-color: white;"
    	//        		+ "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"
    	//				+ "-fx-text-fill: white;"
        //  			+ "-fx-background-radius: 5px;"
    	//        		);

		this.setStyle("-fx-background-color: grey;"//#7ebcea;" //#426ab7;"//
				+ "-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
				+ "-fx-background-radius: 3px;"
				+ "-fx-text-fill: black;"
				+ "-fx-border-color: white;"
	    		+ "-fx-border-radius: 3px;"
	    		+ "-fx-border-width: 1px;"
	    		+ "-fx-border-style: solid; "
				);

       	box_height[0] = 256; //mainScene.getFlyout().getContainerHeight();
    	box_height[1] = 512;//box_height[0] * 1.5D;
    	box_height[2] = 768;//box_height[0] * 3D;
    	box_height[3] = 1024;//box_height[0] * 4D;

    	// 2016-01-01 Added autoCompleteData
    	ObservableList<String> autoCompleteData = createAutoCompleteData();

        textArea = new TextArea();
        //textArea.setPadding(new Insets(2, 0, 2, 0));
        //textArea.setPrefWidth(560);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setTooltip(new Tooltip ("Chatters on MarsNet"));
		//ta.appendText("System : WARNING! A small dust storm 20 km away NNW may be heading toward the Alpha Base" + System.lineSeparator());

  		// 2016-01-01 Replaced textField with autoFillTextBox
        autoFillTextBox = new AutoFillTextBox<String>(autoCompleteData);
        autoFillTextBox.setPadding(new Insets(2, 0, 0, 0));
        autoFillTextBox.getStylesheets().addAll("/css/autofill.css");
        autoFillTextBox.setStyle(
        //		"-fx-background-color: white;"
        //		+ "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"
        //		+ "-fx-text-fill: white;"
        		"-fx-background-radius: 5px;"
        		);

        autoFillTextBox.setFilterMode(false);
        autoFillTextBox.getTextbox().addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
        	keyHandler(keyEvent);
        });

        autoFillTextBox.getTextbox().setMaxWidth(565);
        autoFillTextBox.getTextbox().setMinWidth(565);
        autoFillTextBox.getTextbox().setPrefWidth(565);
        //autoFillTextBox.setStyle("-fx-font: 11pt 'Corbel';");
  		autoFillTextBox.setTooltip(new Tooltip ("Use UP/DOWN arrows to scroll history."));
  		autoFillTextBox.getTextbox().setPromptText("Type your msg here");// to broadcast to a channel");

  		broadcastButton = new JFXButton(" Broadcast ".toUpperCase());
  		broadcastButton.getStyleClass().clear();
		broadcastButton.getStyleClass().add("button-broadcast");
        broadcastButton.setTooltip(new Tooltip ("Click or hit enter to broadcast"));
        broadcastButton.setOnAction(e -> {
          	String text = autoFillTextBox.getTextbox().getText();
            if (text != "" && text != null && !text.trim().isEmpty()) {
            	hitEnter();
                //if (reset)
             	//	mainScene.fireMarsNetButton();
                //reset = false;
            }
            else {
            	 autoFillTextBox.getTextbox().clear();
            }
        });

        HBox hbox = new HBox();
        hbox.setSpacing(1);
        hbox.setPadding(new Insets(0, 0, 0, 0));
        hbox.getChildren().addAll(broadcastButton, autoFillTextBox);//.getTextbox());

        titleLabel = new Label("  " + Msg.getString("ChatBox.title")); //$NON-NLS-1$

        //VBox vbox = new VBox();
        //vbox.getChildren().addAll(titleLabel,textArea);

        //setTop(titleLabel);
        //setCenter(vbox);
        setCenter(textArea);
        setBottom(hbox);

        connect();
    }


    public void connect() {
        textArea.appendText("<< Connection to MarsNet established >>" + System.lineSeparator());

  		int rand = RandomUtil.getRandomInt(2);

  		if (rand == 0)
  			textArea.appendText("System : how can I help you ? /h for help");
  		else if (rand == 1)
  			textArea.appendText("System : how may I assist you ? /h for help");
  		else if (rand == 2)
  			textArea.appendText("System : Is there anything I can help ? /h for help");

  		textArea.appendText(System.lineSeparator());
    }


    /*
     * Display the initial system greeting and update the css style
     */
    // 2016-06-17 Added update()
    public void update() {

    	int theme = MainScene.getTheme();
        if (theme == 7) {
    		String cssFile = "/fxui/css/nimrodskin.css";
        	broadcastButton.getStylesheets().clear();
        	//broadcastButton.setStyle("-fx-text-fill: #E5AB00;");
    		broadcastButton.getStyleClass().clear();
    		broadcastButton.getStyleClass().add("button-broadcast");
    		broadcastButton.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

        	titleLabel.getStylesheets().clear();
            titleLabel.setStyle("-fx-text-fill: #E5AB00;"
            		+ " -fx-font: bold 12pt 'Corbel';"
            		//+ " -fx-effect: dropshadow( one-pass-box , orange , 8 , 0.0 , 2 , 0 );"
            		);
        	textArea.getStylesheets().clear();
            textArea.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        }
        else {
    		String cssFile = "/fxui/css/snowBlue.css";
        	broadcastButton.getStylesheets().clear();
        	//broadcastButton.setStyle("-fx-text-fill: #3291D2;");
    		broadcastButton.getStyleClass().clear();
    		broadcastButton.getStyleClass().add("button-broadcast");
    		broadcastButton.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        	titleLabel.getStylesheets().clear();
        	titleLabel.setStyle("-fx-text-fill: #3291D2;"
        			+ " -fx-font: bold 12pt 'Corbel';"
        			//+ " -fx-effect: dropshadow( one-pass-box , blue , 8 , 0.0 , 2 , 0 );"
        			);
        	textArea.getStylesheets().clear();
            textArea.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        }

		textArea.setStyle("-fx-text-fill: black;");
        //textArea.setStyle("-fx-background-color: black;");
  		textArea.positionCaret(textArea.getText().length());
    }

    public void closeChatBox(boolean disconnected) {

    	if (disconnected) {
    		textArea.appendText(YOU_PROMPT + "Farewell!" + System.lineSeparator() + "<< Disconnected from MarsNet >>" + System.lineSeparator() + System.lineSeparator());
	    	personCache = null;
	    	robotCache = null;
	    	settlementCache = null;
    	}

        mainScene.getFlyout().hide();//.close();//.dismiss();
        //mainScene.ToggleMarsNetButton(false);
    }

    /**
     * Compiles the names of settlements and people and robots into the autocomplete data list
     * @return ObservableList<String>
     */
    public ObservableList<String> createAutoCompleteData() {

    	ObservableList<String> autoCompleteData = null;

		// Creates an array with the names of all of settlements
		Collection<Settlement> settlements = Simulation.instance().getUnitManager().getSettlements();
		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);

		//autoCompleteArray = settlementList.toArray(new String[settlementList.size()]);
		// or with java 8 stream
		//autoCompleteArray = settlementList.stream().toArray(String[]::new);

	   	// Creates an array with the names of all of people and robots
		List<String> nameList = new ArrayList<>();

		// 2016-10-06 Added misc keywords
		nameList.add("settlement");
		nameList.add("settlements");
		nameList.add("vehicle");
		nameList.add("vehicles");
		nameList.add("rover");
		nameList.add("rovers");

		Iterator<Settlement> i = settlementList.iterator();
		while (i.hasNext()) {
			Settlement s = i.next();
				nameList.add(s.getName());

			Iterator<Person> j = s.getAllAssociatedPeople().iterator();
			while (j.hasNext()) {
				Person p = j.next();

				String first = "";
				String last = "";
				// 2016-06-15 Added names in both orders, namely, "first last" or "last, first"
				String firstLast = p.getName();
				String lastFirst = "";
				int len1 = firstLast.length();
				// 2016-10-01 Used for loop to find the last is the best approach instead of int index = firstLast.indexOf(" ");
				int index = 0;

				for (int k = len1-1 ; k > 0 ; k--) {
					// Note: finding the whitespace from the end to 0 (from right to left) works better than from left to right
					// e.g. Mary L. Smith (last name should be "Smith", not "L. Smith"
			        if (firstLast.charAt(k) == ' ') {
			        	index = k;
				        first = firstLast.substring(0, k);
				        last = firstLast.substring(k+1, len1);
				        break;
			        }
			        else
			        	first = firstLast;
				}


				if (index == -1) {
					// the person has no last name
					first = firstLast;
					//last = "";
					//lastFirst = first;
					nameList.add(first);
				}
				else {
					first = firstLast.substring(0, index);
					last = firstLast.substring(index+1, firstLast.length());
					lastFirst = last + ", " + first;
					nameList.add(firstLast);
					nameList.add(lastFirst);
				}

			}

			Iterator<Robot> k = s.getAllAssociatedRobots().iterator();
			while (k.hasNext()) {
				Robot r = k.next();
				nameList.add(r.getName());
			}
		}

		//String[] n2 = settlementList.stream().toArray(String[]::new);
		//autoCompleteArray = Stream.concat(Arrays.stream(n1), Arrays.stream(n2))
	    //        .toArray(String[]::new);
		// alternatively,
		//String[] both = Stream.of(n1, n2).flatMap(Stream::of).toArray(String[]::new);
		// Use Apache Commons Lang lib
		//autoCompleteArray = (String[])ArrayUtils.addAll(autoCompleteArray, peopleRobotsArray);

		autoCompleteData = FXCollections.observableArrayList(nameList);
	    //autoCompleteData.addAll(nameList);
	    //for(int j=0; j<autoCompleteArray.length; j++){
	    //    autoCompleteData.add(autoCompleteArray[j]);
	    //}
	    return autoCompleteData;
	}

	// public String checkGreetings(String text) {
    //	String result = null;
    //	return result;

    //2016-01-01 Added isInteger()
    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty())
        	return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1)
                	return false;
                else
                	continue;
            }
            if  (Character.digit(s.charAt(i),radix) < 0)
            	return false;
        }
        return true;
    }

    /**
     * Processes a question and return an answer regarding an unit
     * @param text
     */
    //2015-12-21 askQuestion()
    public void askQuestion(String text) {
    	String the_other_party = "";
    	//String SYSTEM = "System ";

    	StringBuilder responseText = new StringBuilder();
    	String questionText = null;
    	Unit cache = null;
    	String name = null;
    	boolean help = false;

    	if (personCache != null) {
    		cache = (Person) personCache;
    	}
    	else if (robotCache != null) {
    		cache = (Robot) robotCache;
    	}
    	else if (settlementCache != null) {
    		cache = (Settlement) settlementCache;
    	}

		name = cache.getName();

    	// Case 0 : exit the conversation
    	if (text.equalsIgnoreCase("quit")
    			|| text.equalsIgnoreCase("/quit")
    			|| text.equalsIgnoreCase("\\quit")
    			|| text.equalsIgnoreCase("\\q")
    			|| text.equalsIgnoreCase("/q")

    			|| text.equalsIgnoreCase("exit")
    			|| text.equalsIgnoreCase("/exit")
    			|| text.equalsIgnoreCase("\\exit")
    			|| text.equalsIgnoreCase("\\x")
    			|| text.equalsIgnoreCase("/x")

       			|| text.equalsIgnoreCase("bye")
       			|| text.equalsIgnoreCase("/bye")
      			|| text.equalsIgnoreCase("\\bye")
    			) {
    		// set personCache and robotCache to null so as to quit the conversation

    		String[] txt = farewell();
    		questionText = txt[0];
    		responseText.append(txt[1]);


    		if (settlementCache != null)
    			// you were asking about a settlement earlier
    			;//responseText = "disconnected.";

    		else {

    			the_other_party = name;

    			int rand = RandomUtil.getRandomInt(6);

    			if (rand == 0)
    				responseText.append("bye!");
    			else if (rand == 1)
    				responseText.append("farewell!");
    			else if (rand == 2)
    				responseText.append("bye now");
    			else if (rand == 3)
    				responseText.append("have a nice sol!");
    			else if (rand == 4)
    				responseText.append("Take it easy!");
    			else if (rand == 5)
    				responseText.append("Take care!");
    			else
    				responseText.append("oh well! ");

				responseText.append(System.lineSeparator());
				responseText.append(SYSTEM_PROMPT);
				responseText.append(the_other_party);

    			int rand1 = RandomUtil.getRandomInt(1);

    			if (rand1 == 0)
    				responseText.append(" had left the conversation.");
    			else
    				responseText.append(" is disconnected.");
    		}

    		personCache = null;
	    	robotCache = null;
	    	settlementCache = null;
    	}


     	// 2016-10-06 Added changing the height of the chat box
     	else if (text.equalsIgnoreCase("/y1")) {
     		questionText = REQUEST_HEIGHT_CHANGE + box_height[0] + " pixels.";

     		responseText.append(SYSTEM_PROMPT);
     		responseText.append("Close the chat box to reset the chat box height to 256 pixels.");

     		mainScene.setChatBoxPaneHeight(box_height[0]);
     		closeChatBox(false);
     		mainScene.openChatBox();
     	}

     	else if (text.equalsIgnoreCase("/y2")) {
     		questionText = REQUEST_HEIGHT_CHANGE + box_height[1] + " pixels.";

     		responseText.append(SYSTEM_PROMPT);
     		responseText.append("Close the chat box to reset the chat box height to 256 pixels.");

     		mainScene.setChatBoxPaneHeight(box_height[1]);
            closeChatBox(false);
            mainScene.openChatBox();
     	}

     	else if (text.equalsIgnoreCase("/y3")) {
     		questionText = REQUEST_HEIGHT_CHANGE + box_height[2] + " pixels.";

     		responseText.append(SYSTEM_PROMPT);
     		responseText.append("Close the chat box to reset the chat box height to 256 pixels.");

     		mainScene.setChatBoxPaneHeight(box_height[2]);
            closeChatBox(false);
            mainScene.openChatBox();
     	}

     	else if (text.equalsIgnoreCase("/y4")) {
     		questionText = REQUEST_HEIGHT_CHANGE + box_height[3] + " pixels.";

     		responseText.append(SYSTEM_PROMPT);
     		responseText.append("Close the chat box to reset the chat box height to 256 pixels.");

     		mainScene.setChatBoxPaneHeight(box_height[3]);
     		closeChatBox(false);
            mainScene.openChatBox();
	    }

    	// Case 1: ask about a particular settlement
    	else if (settlementCache != null) {

    		if (isInteger(text, 10)) {

    	    	int num = Integer.parseUnsignedInt(text, 10);
/*
    	    	if (num == 0) {
    	    		// exit
    	    		sys = name;
    	    		questionText = YOU_PROMPT + "have a nice sol. Bye!";
    	    		responseText = "Bye! " + System.lineSeparator() + "System : exit the inquiry regarding " + name;

    	    		personCache = null;
    		    	robotCache = null;
    		    	settlementCache = null;
    		    }
*/
    	    	if (num == 1) {
    	    		questionText = YOU_PROMPT + "how many beds are there in total ? ";
    	    		responseText.append("The total number of beds is ");
    	    		responseText.append(settlementCache.getPopulationCapacity());

    	    	}

    	    	else if (num == 2) {
    	    		questionText = YOU_PROMPT + "how many beds that have already been designated to a person ? ";
    	    		responseText.append("There are ");
    	    		responseText.append(settlementCache.getTotalNumDesignatedBeds());
    	    		responseText.append(" designated beds. ");

    	    	}

    	    	else if (num == 3) {
    	    		questionText = YOU_PROMPT + "how many beds that are currently NOT occupied ? ";
    	    		responseText.append("There are ");
    	    		responseText.append(settlementCache.getPopulationCapacity() - settlementCache.getSleepers());
    	    		responseText.append(" unoccupied beds. ");

    	    	}

    	    	else if (num == 4) {
    	    		questionText = YOU_PROMPT + "how many beds are currently occupied ? ";
    	    		responseText.append("There are ");
    	    		responseText.append(settlementCache.getSleepers());
    	    		responseText.append(" occupied beds with people sleeping on it at this moment. ");

    	    	}

    	    	else {
    	    		questionText = YOU_PROMPT + "You entered '" + num + "'.";
    	    		responseText.append("Sorry. This number is not assigned to a valid question.");
    	    	}

    	    // if it's not a integer input
    		}

    		else if (text.contains("bed")
    				|| text.contains("sleep")
    				|| text.equalsIgnoreCase("lodging")
    				|| text.contains("quarters")) {

    			questionText = YOU_PROMPT + "how well are the beds utilized ? ";
	    		responseText.append("Total number of beds : ");
	    		responseText.append(settlementCache.getPopulationCapacity());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("Desginated beds : ");
	    		responseText.append(settlementCache.getTotalNumDesignatedBeds());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("Unoccupied beds : ");
	    		responseText.append(settlementCache.getPopulationCapacity() - settlementCache.getSleepers());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("Occupied beds : ");
	    		responseText.append(settlementCache.getSleepers());
	    		responseText.append(System.lineSeparator());
    		}

    		else if (text.equalsIgnoreCase("vehicle")
    				|| text.equalsIgnoreCase("rover")
    				|| text.contains("rover")
    				|| text.contains("vehicle")) {

    			questionText = YOU_PROMPT + "What are the vehicles in the settlement ? ";
	    		responseText.append(" ** Rovers/Vehicles Inventory **");
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(1). Total # of Rovers/Vehicles : ");
	    		responseText.append(settlementCache.getAllAssociatedVehicles().size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(2). Total # of Rovers/Vehicles on Mission : ");
	    		responseText.append(settlementCache.getMissionVehicles().size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(3). Total # of Parked Rovers/Vehicles (NOT on Mission) : ");
	    		responseText.append(settlementCache.getParkedVehicleNum());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(4). # Cargo Rovers on Mission : ");
	    		responseText.append(settlementCache.getCargoRovers(2).size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(5). # Transport Rovers on Mission : ");
	    		responseText.append(settlementCache.getTransportRovers(2).size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(6). # Explorer Rovers on Mission : ");
	    		responseText.append(settlementCache.getExplorerRovers(2).size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(7). # Light Utility Vehicles (LUVs) on Mission : ");
	    		responseText.append(settlementCache.getLUVs(2).size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(8). # Parked Cargo Rovers : ");
	    		responseText.append(settlementCache.getCargoRovers(1).size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(9). # Parked Transport Rovers : ");
	    		responseText.append(settlementCache.getTransportRovers(1).size());
	    		responseText.append(System.lineSeparator());
	    		responseText.append("(10). # Parked Explorer Rovers : ");
	    		responseText.append(settlementCache.getExplorerRovers(1).size());
	    		responseText.append(System.lineSeparator());
				responseText.append("(11). # Parked Light Utility Vehicles (LUVs) : ");
				responseText.append(settlementCache.getLUVs(1).size());
				responseText.append(System.lineSeparator());
    		}

        	else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("/k")
	    			|| text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h")
	       			|| text.equalsIgnoreCase("/?") || text.equalsIgnoreCase("?")
	       			){

        		help = true;
    	    	questionText = REQUEST_HELP;
    	    	responseText.append(HELP_TEXT);

    	    }

	     	// 2016-10-06 Added changing the height of the chat box
	     	else if (text.equalsIgnoreCase("/y1")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[0] + " pixels.";
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Close the chat box to reset the chat box height to 256 pixels.");
	     		mainScene.setChatBoxPaneHeight(box_height[0]);
	     		closeChatBox(false);
	     	}

	     	else if (text.equalsIgnoreCase("/y2")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[1] + " pixels.";
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Close the chat box to reset the chat box height to 512 pixels.");
	     		mainScene.setChatBoxPaneHeight(box_height[1]);
	            closeChatBox(false);
	     	}

	     	else if (text.equalsIgnoreCase("/y3")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[2] + " pixels.";
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Close the chat box to reset the chat box height to 768 pixels.");
	     		mainScene.setChatBoxPaneHeight(box_height[2]);
	            closeChatBox(false);
	     	}

	     	else if (text.equalsIgnoreCase("/y4")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[3] + " pixels.";
	     		mainScene.setChatBoxPaneHeight(box_height[3]);
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Will close the chat box to reset the chat box height to 1024 pixels.");
	            closeChatBox(false);

		    }

	     	else {
    	    	String[] txt = clarify();
    	    	questionText = txt[0];
    	    	responseText.append(txt[1]);
    		}

    	}
    	// Case 2: ask to talk to a person or robot
    	else if (settlementCache == null) { // better than personCache != null || robotCache != null since it can incorporate help and other commands

    		int num = 0;

    		if (isInteger(text, 10))
    			num = Integer.parseUnsignedInt(text, 10);

    		// 2017-03-31 Add command "die"
    		if (text.equalsIgnoreCase("die")) {

    			if (personCache != null) {
    				questionText = YOU_PROMPT + "I hereby pronounce you dead.";

    				String lastWord = null;

    				int rand = RandomUtil.getRandomInt(12);
    				// Quotes from http://www.phrases.org.uk/quotes/last-words/suicide-notes.html
    				// https://www.goodreads.com/quotes/tag/suicide-note
    				if (rand == 0)
    					lastWord = "This is all too heartbreaking for me. Farewell, my friend.";
    				else if (rand == 1)
    					lastWord = "Things just seem to have gone too wrong too many times...";
    				else if (rand == 2)
    					lastWord = "So I leave this world, where the heart must either break or turn to lead.";
    				else if (rand == 3)
    					lastWord = "Let's have no sadness——furrowed brow. There's nothing new in dying now. Though living is no newer.";
    				else if (rand == 4)
    					lastWord = "I myself——in order to escape the disgrace of deposition or capitulation——choose death.";
    				else if (rand == 5)
    					lastWord = "When all usefulness is over, when one is assured of an unavoidable and imminent death, "
    							+ "it is the simplest of human rights to choose a quick and easy death in place of a slow and horrible one. ";
    				else if (rand == 6)
    					lastWord = "I am going to put myself to sleep now for a bit longer than usual. Call it Eternity.";
    				else if (rand == 7)
    					lastWord = "All fled——all done, so lift me on the pyre; the feast is over, and the lamps expire.";
    				else if (rand == 8)
    					lastWord = "No more pain. Wake no more. Nobody owns.";
    				else if (rand == 9)
    					lastWord = "Dear World, I am leaving because I feel I have lived long enough. I am leaving you with your worries in this sweet cesspool. Good luck.";
    				else if (rand == 10)
    					lastWord = "This is what I want so don't be sad.";
    				else if (rand == 11)
    					lastWord = "I don't want to hurt you or anybody so please forget about me. Just try. Find yourself a better friend.";
    				else
    					lastWord = "They tried to get me——I got them first!";

    				responseText.append(lastWord);

    				personCache.setLastWord(lastWord);

    				personCache.getPhysicalCondition().setDead(new HealthProblem(new Complaint(ComplaintType.SUICIDE), personCache), true);

    	    		personCache = null;
    		    	robotCache = null;
    		    	settlementCache = null;
    			}
    		}

    		else if (num == 1 || text.equalsIgnoreCase("where") || text.equalsIgnoreCase("location")) {// || text.contains("location")) {
	    		questionText = YOU_PROMPT + "Where are you ? "; //what is your Location State [Expert Mode only] ?";
	    		//LocationState state = cache.getLocationState();
	    		LocationStateType stateType = cache.getLocationStateType();

	    		if (stateType != null) {
	    			if (personCache != null) {

	    				responseText.append("I'm ");
	    				responseText.append(stateType.getName());

	    				if (personCache.getBuildingLocation() != null) {
	    					responseText.append(" (");
	    					responseText.append(personCache.getBuildingLocation().getNickName());
	    					responseText.append(")");
	    				}


	    	    	}
	    	    	else if (robotCache != null) {

	    	    		responseText.append("I'm ");
	    	    		responseText.append(stateType.getName());
	    	    		responseText.append(" (");
	    	    		responseText.append(robotCache.getBuildingLocation().getNickName());
	    	    		responseText.append(")");
	    	    	}
	    		}
	    		else
	    			responseText.append("It may sound strange but I don't know where I'm at. ");
	    	}

	    	else if (num == 2 || text.contains("located") || text.contains("location") && text.contains("situation")) {
	    		questionText = YOU_PROMPT + "Where are you located ? "; //what is your Location Situation [Expert Mode only] ?";
	    		responseText.append("I'm located at ");
	    		responseText.append(Conversion.capitalize(cache.getLocationSituation().getName()));
	    	}

	       	else if (num == 3 || text.equalsIgnoreCase("task") || text.equalsIgnoreCase("activity")
	       			|| text.equalsIgnoreCase("action")) {
	    		questionText = YOU_PROMPT + "What are you doing ?";
	    		if (personCache != null) {
	    			responseText.append(personCache.getTaskDescription());
    	    	}
    	    	else if (robotCache != null) {
    	    		responseText.append(robotCache.getTaskDescription());
    	    	}

	       	}

	       	else if (num == 4 || text.equalsIgnoreCase("mission")) {

    			//sys = name;
	    		questionText = YOU_PROMPT + "Are you involved in a particular mission at this moment?";
	    		Mission mission = null;
	    		if (personCache != null) {
	    			mission = personCache.getMind().getMission();
		    	}
		    	else if (robotCache != null) {
		    		mission = robotCache.getBotMind().getMission();
		    	}

	    		if (mission == null)
	    			responseText.append("No. I'm not. ");
	    		else
	    			responseText.append(mission.getDescription());

    		}

	    	else if (num == 5 || text.equalsIgnoreCase("bed") || text.equalsIgnoreCase("quarter") || text.equalsIgnoreCase("quarters")) {
	    		questionText = YOU_PROMPT + "Where is your designated quarters/bed ? ";
	    		Point2D bed = personCache.getBed();
	    		if (bed == null)
	    			responseText.append("I don't have my own private quarters/bed.");
	    		else {
	    			if (personCache != null) {
	    				Settlement s1 = personCache.getSettlement();
		    			if (s1 != null) {
		    				// check to see if a person is on a trading mission
		    				Settlement s2 = personCache.getAssociatedSettlement();
		    				if (s2 != null) {
		    					responseText.append("My designated quarters/bed is at (");
		    					responseText.append(bed.getX());
		    					responseText.append(", ");
		    					responseText.append(bed.getY());
		    					responseText.append(") in ");
		    					responseText.append(personCache.getQuarters());
		    					responseText.append(" at ");

		    					if (s1 == s2) {
		    						responseText.append(s1);
		    					}

		    					else {
				    				// yes, a person is on a trading mission
		    						responseText.append(s2);
		    					}
		    				}
		    			}
		    			else {
	    					responseText.append("My designated quarters/bed is at (");
	    					responseText.append(bed.getX());
	    					responseText.append(", ");
	    					responseText.append(bed.getY());
	    					responseText.append(") in ");
	    					responseText.append(personCache.getQuarters());
	    					responseText.append(" at ");
		    			}
	    	    	}
	    	    	else if (robotCache != null) {
	    	    		responseText.append("I don't need one. My battery can be charged at any robotic station.");
	    	    	}
	    		}
	    	}

	    	else if (num == 6 || text.equalsIgnoreCase("inside") || text.equalsIgnoreCase("container")) {
	    		questionText = YOU_PROMPT + "Are you inside?"; //what is your Container unit [Expert Mode only] ?";
	    		Unit c = cache.getContainerUnit();
	    		if (c != null) {
	    			responseText.append("I'm at/in ");
	    			responseText.append(c.getName());
	    		}
	    		else
	    			responseText.append("I'm not inside a building or vehicle"); //"I don't have a Container unit. ";
	    	}

	    	else if (num == 7 ||text.equalsIgnoreCase("outside") || text.contains("top") && text.contains("container")) {
	    		questionText = YOU_PROMPT + "Are you inside?"; //YOU_PROMPT + "what is your Top Container unit [Expert Mode only] ?";
	    		Unit tc = cache.getTopContainerUnit();
	    		if (tc != null) {
	    			responseText.append("I'm in ");
	    			responseText.append(tc.getName());
	    		}

	    		else
	    			responseText.append("I'm nowhere");// don't have a Top Container unit.";

	    	}

	    	else if (num == 8 || text.equalsIgnoreCase("building") ) {
	    		questionText = YOU_PROMPT + "What building are you at ?";
	    		Settlement s = cache.getSettlement();
	    		if (s != null) {
		    		//Building b1 = s.getBuildingManager().getBuilding(cache);
		    		Building b = cache.getBuildingLocation();
		    		if (b != null) {// && b1 != null)
		    			responseText.append("The building I'm in is ");
		    			responseText.append(b.getNickName());
		    				//+ "  (aka " + b1.getNickName() + ").";
		    		}
		    		else
		    			responseText.append("I'm not in a building.");
	    		}
	    		else
	    			responseText.append("I'm not in a building.");

	    	}


	    	else if (num == 9 || text.equalsIgnoreCase("settlement")) {
	    		questionText = YOU_PROMPT + "What is the settlement that you are at ?";
	    		Settlement s = cache.getSettlement();
	   			if (s != null) {
	   				responseText.append("My settlement is ");
	   				responseText.append(s.getName());
	   			}
	   			else
	   				responseText.append("I'm not inside a settlement");

	    	}

	    	else if (num == 10 || text.equalsIgnoreCase("associated settlement")) {
	    		questionText = YOU_PROMPT + "What is your associated settlement ?";
	    		Settlement s = cache.getAssociatedSettlement();
	    		if (s  != null) {
	    			responseText.append("My associated settlement is ");
	    			responseText.append(s.getName());
	    		}
	    		else
	    			responseText.append("I don't have an associated settlement");
	    	}

/*
	    	else if (num == 9 || text.equalsIgnoreCase("buried settlement")) {
	    		questionText = YOU_PROMPT + "What is his/her buried settlement ?";
	    		if personCache.
	    		Settlement s = cache.getBuriedSettlement();
	    		if (s == null) {
	           		responseText = "The buried settlement is " + s.getName();
	           		sys = "System : ";
	       		}
	       		else
	       			responseText = "I'm not dead.";
	    	}
*/

	       	else if (num == 11 || text.equalsIgnoreCase("vehicle") ) {
	       		questionText = YOU_PROMPT + "What vehicle are you in and where is it ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	           		String d = cache.getVehicle().getDescription();
	           		String status = cache.getVehicle().getStatus();
	           		responseText.append("My vehicle is ");
	           		responseText.append(v.getName());
	           		responseText.append(" (a ");
	           		responseText.append(Conversion.capitalize(d));
	           		responseText.append(" type). It's currently ");
	           		responseText.append(status.toLowerCase());
	           		responseText.append(".");
	       		}
	       		else
	       			responseText.append("I'm not in a vehicle.");
	       	}

	    	else if (num == 12 || text.equalsIgnoreCase("vehicle inside") || text.equalsIgnoreCase("vehicle container") || text.contains("vehicle") && text.contains("container")) {
	    		questionText = YOU_PROMPT + "Where is your vehicle at?";//'s container unit ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Unit c = v.getContainerUnit();
	       			if (c != null) {
	       				responseText.append("My vehicle is at ");
	       				responseText.append(c.getName());
	       			}

	       			else
	       				responseText.append("My vehicle is not inside");//doesn't have a container unit.";

	       		}
	       		else
	       			responseText.append("I'm not in a vehicle.");
	       	}

	    	else if (num == 13 || text.equalsIgnoreCase("vehicle outside") || text.equalsIgnoreCase("vehicle top container") || text.contains("vehicle") && text.contains("top") && text.contains("container")) {
	    		questionText = YOU_PROMPT + "What is your vehicle located?";//'s top container unit ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Unit tc = v.getTopContainerUnit();
	       			if (tc != null) {
	       				responseText.append("My vehicle is at ");
	       				responseText.append(tc.getName());
	       			}
	       			else
	       				responseText.append("My vehicle is not inside");//doesn't have a top container unit.";
	       		}
	       		else
	       			responseText.append("I'm not in a vehicle.");
	       	}

	    	else if (num == 14  || text.contains("vehicle") && text.contains("park")) {
	    		questionText = YOU_PROMPT + "What building does your vehicle park at ?";
	    		Vehicle v = cache.getVehicle();
		       	if (v != null) {
		       		Settlement s = v.getSettlement();
		       		if (s != null) {
		       			Building b = s.getBuildingManager().getBuilding(v);
		       			if (b != null) {
		       				responseText.append("My vehicle is parked inside ");
		       				responseText.append(b.getNickName());
		       			}

		       			else
		       				responseText.append("My vehicle does not park inside a building/garage");
		       		}
		       		else
		       			responseText.append("My vehicle is not at a settlement.");
	   			}
		       	else
	       			responseText.append("I'm not on a vehicle.");
	       	}

	    	else if (num == 15 || text.contains("vehicle") && text.contains("settlement")) {
	    		questionText = YOU_PROMPT + "What settlement is your vehicle located at ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Settlement s = v.getSettlement();
	       			if (s != null)  {
	       				responseText.append("My vehicle is at ");
	       				responseText.append(s.getName());
	       			}
	       			else
	       				responseText.append("My vehicle is not at a settlement.");
	       		}
	       		else
	       			responseText.append("I'm not on a vehicle.");
	    	}

	    	else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("/k")
	    			|| text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h")
	       			|| text.equalsIgnoreCase("/?") || text.equalsIgnoreCase("?")
	    			){

        		help = true;
		    	questionText = REQUEST_HELP;
				responseText.append(HELP_TEXT);

	    	}

	     	// 2016-10-06 Added changing the height of the chat box
	     	else if (text.equalsIgnoreCase("/y1")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[0] + " pixels.";
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Close the chat box to reset the chat box height to 256 pixels.");
	     		mainScene.setChatBoxPaneHeight(box_height[0]);
	     		closeChatBox(false);
	     	}

	     	else if (text.equalsIgnoreCase("/y2")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[1] + " pixels.";
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Close the chat box to reset the chat box height to 512 pixels.");
	     		mainScene.setChatBoxPaneHeight(box_height[1]);
	            closeChatBox(false);
	     	}

	     	else if (text.equalsIgnoreCase("/y3")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[2] + " pixels.";
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Close the chat box to reset the chat box height to 768 pixels.");
	     		mainScene.setChatBoxPaneHeight(box_height[2]);
	            closeChatBox(false);
	     	}

	     	else if (text.equalsIgnoreCase("/y4")) {
	     		questionText = REQUEST_HEIGHT_CHANGE + box_height[3] + " pixels.";
	     		mainScene.setChatBoxPaneHeight(box_height[3]);
	     		responseText.append(SYSTEM_PROMPT);
	     		responseText.append("Will close the chat box to reset the chat box height to 1024 pixels.");
	            closeChatBox(false);

		    } else {
    	    	String[] txt = clarify();
    	    	questionText = txt[0];
    	    	responseText.append(txt[1]);
	    	}

    	}

    	else {
    		// set personCache and robotCache to null only if you want to quit the conversation
	    	String[] txt = clarify();
	    	questionText = txt[0];
	    	responseText.append(txt[1]);
    	}

    	textArea.appendText(questionText);
    	textArea.appendText(System.lineSeparator());

    	if (help)
			the_other_party = "System";
    	else
    		the_other_party = name;

   		textArea.appendText(the_other_party);
   		textArea.appendText(" : ");
   		textArea.appendText(responseText.toString());
   		textArea.appendText(System.lineSeparator());
    }

    /*
     * Parses the text and interprets the contents in the chat box
     * @param input text
     */
    //2015-12-01 Added parseText()
    public Unit parseText(String text) {
    	//System.out.println("starting parseText()");
    	Unit unit = null;
    	String responseText = null;
    	//String SYSTEM_PROMPT = "System : ";
    	boolean available = true;
    	int nameCase = 0;
    	boolean proceed = false;
    	//System.out.println("A: text is " + text + ". Running parseText()");
    	text = text.trim();
    	int len = text.length();

    	List<Person> personList = new ArrayList<>();
    	List<Robot> robotList = new ArrayList<>();
    	Person person = null;
    	Robot robot = null;

    	// Part 1 //

    	// 2015-12-17 detect "\" backslash and the name that follows
     	if (len >= 3 && text.substring(0,1).equalsIgnoreCase("\\")) {

    		text = text.substring(1, len).trim();
    		proceed = true;
    	}

    	else if (
    			text.equalsIgnoreCase("key") || text.equalsIgnoreCase("/k")
    			|| text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h")
       			|| text.equalsIgnoreCase("/?") || text.equalsIgnoreCase("?")
    			) {

    		responseText = INSTRUCTION;

    	}

     	else if (text.equalsIgnoreCase("quit")
    			|| text.equalsIgnoreCase("/quit")
    			|| text.equalsIgnoreCase("\\quit")
    			|| text.equalsIgnoreCase("\\q")
    			|| text.equalsIgnoreCase("/q")

    			|| text.equalsIgnoreCase("exit")
    			|| text.equalsIgnoreCase("/exit")
    			|| text.equalsIgnoreCase("\\exit")
    			|| text.equalsIgnoreCase("\\x")
    			|| text.equalsIgnoreCase("/x")

       			|| text.equalsIgnoreCase("bye")
       			|| text.equalsIgnoreCase("/bye")
      			|| text.equalsIgnoreCase("\\bye")
     			){

    		String[] txt = farewell();
    		//questionText = txt[0];
    		responseText = txt[1];

            closeChatBox(true);

    	}

     	// 2016-10-06 Added changing the height of the chat box
     	else if (text.equalsIgnoreCase("/y1")) {
     		responseText = SYSTEM_PROMPT + "Close the chat box to reset the chat box height to 256 pixels.";
     		mainScene.setChatBoxPaneHeight(box_height[0]);
     		closeChatBox(false);
     		//mainScene.ToggleMarsNetButton(true);
     		mainScene.getFlyout().show(mainScene.getMarsNetBtn(), PopupVPosition.TOP, PopupHPosition.RIGHT, -50, 20);


/*
     		System.out.println("1. is it selected : " + mainScene.isToggleMarsNetButtonSelected());
     		mainScene.ToggleMarsNetButton(true);
     		System.out.println("2. is it selected : " + mainScene.isToggleMarsNetButtonSelected());
       		mainScene.fireMarsNetButton();
    		System.out.println("3. is it selected : " + mainScene.isToggleMarsNetButtonSelected());
     		mainScene.getFlyout().dismiss();
    		System.out.println("4. is it selected : " + mainScene.isToggleMarsNetButtonSelected());
     		mainScene.getFlyout().flyout();
    		System.out.println("5. is it selected : " + mainScene.isToggleMarsNetButtonSelected());
     		mainScene.ToggleMarsNetButton(true);
    		System.out.println("6. is it selected : " + mainScene.isToggleMarsNetButtonSelected());
*/
     		//try {
			//	TimeUnit.MILLISECONDS.sleep(1000L);
			//} catch (InterruptedException e) {
			//}
     		//mainScene.getFlyout().flyout();
     		//mainScene.ToggleMarsNetButton(false);
     		//mainScene.fireMarsNetButton();
     		//mainScene.ToggleMarsNetButton(true);
     		//mainScene.fireMarsNetButton();
     		//mainScene.ToggleMarsNetButton(false);
	        //IntStream.range(0, 1).forEach(i -> mainScene.fireMarsNetButton());
     	}

     	else if (text.equalsIgnoreCase("/y2")) {
     		responseText = SYSTEM_PROMPT + "Close the chat box to reset the chat box height to 512 pixels.";
     		mainScene.setChatBoxPaneHeight(box_height[1]);
            closeChatBox(false);
     		//mainScene.ToggleMarsNetButton(true);
     		//mainScene.getFlyout().flyout();
     	}

     	else if (text.equalsIgnoreCase("/y3")) {
     		responseText = SYSTEM_PROMPT + "Close the chat box to reset the chat box height to 768 pixels.";
     		mainScene.setChatBoxPaneHeight(box_height[2]);
            closeChatBox(false);
     	}

     	else if (text.equalsIgnoreCase("/y4")) {
     		mainScene.setChatBoxPaneHeight(box_height[3]);
     		responseText = SYSTEM_PROMPT + "Will close the chat box to reset the chat box height to 1024 pixels.";
            closeChatBox(false);
     	}

    	// 2016-10-06 Added asking about settlements in general
    	else if (text.toLowerCase().equals("settlement")
    			 || text.toLowerCase().equals("settlements")) {

    		//questionText = YOU_PROMPT + "What are the names of the settlements ?";

    		// Creates an array with the names of all of settlements
    		List<Settlement> settlementList = new ArrayList<Settlement>(Simulation.instance().getUnitManager().getSettlements());

    		int num = settlementList.size();
    		String s = "";

    		if (num > 2) {
	    		for (int i = 0; i < num ; i++) {
	    			if (i == num - 2)
	    				s = s + settlementList.get(i) + ", and ";
	    			else if (i == num - 1)
	    				s = s + settlementList.get(i) + ".";
	    			else
	    				s = s + settlementList.get(i) + ", ";
	    		}
	    		responseText = SYSTEM_PROMPT + "There is a total of " + num + " settlements : " + s;
    		}

    		else if (num == 2) {
	    		s = settlementList.get(0) + " and " + settlementList.get(1);
	    		responseText = SYSTEM_PROMPT + "There is a total of " + num + " settlements : " + s;
    		}

    		else if (num == 1) {
    			responseText = SYSTEM_PROMPT + "There is just one settlement : " + settlementList.get(0);
    		}

    		else
    			responseText = SYSTEM_PROMPT + "Currently, there is no settlement established on Mars.";

    	}

    	else if (len >= 5 && text.substring(0,5).equalsIgnoreCase("hello")
    			|| len >= 4 && text.substring(0,4).equalsIgnoreCase("helo")) {

    		if (len > 5) {
    			text = text.substring(5, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else if (len > 4) {
    			text = text.substring(4, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
     	   		responseText = SYSTEM_PROMPT + "Hello, how can I help? /h for guidance/help";
    		}
    	}

    	else if (len >= 3 && text.substring(0,3).equalsIgnoreCase("hey")) {

    		if (len > 3) {
    			text = text.substring(3, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
     	   		responseText = SYSTEM_PROMPT + "Hey, how can I help? /h for guidance/help";
    		}
    	}

    	else if (len >= 2 && text.substring(0,2).equalsIgnoreCase("hi")) {

    		if (len > 2) {
    			text = text.substring(2, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
     	   		responseText = SYSTEM_PROMPT + "Hi, how can I help? /h for guidance/help";
    		}
    	}

    	else if (len >= 2) {
    		proceed = true;
    	}


    	// Part 2 //

    	if (len == 0 || text == null || text.length() == 1) {

    		responseText = clarify()[1];
/*
    		int rand = RandomUtil.getRandomInt(2);
			if (rand == 0)
				responseText = SYSTEM_PROMPT + "I'm not sure if I understand. Can you say it again?";
			else if (rand == 1)
				responseText = SYSTEM_PROMPT + "I'm sorry. Can you say that again?";
			else
				responseText = SYSTEM_PROMPT + "I'm afraid I don't understand. Would you repeat that?";
*/
    	}


    	else if (proceed) { // && text.length() > 1) {
	    	//System.out.println("B: text is " + text);

    		// person and robot
    		Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				// Check if anyone has this name (as first/last name) in any settlements
				// and if he/she is still alive
				if (text.contains("bot") || text.contains("Bot")) {
		    		// Check if it is a bot
				 	robotList.addAll(settlement.returnRobotList(text));
				}
				else {
					personList.addAll(settlement.returnPersonList(text));

				}

				if (personList.size() != 0)
					nameCase = personList.size();
				else
					nameCase = robotList.size();

				//System.out.println("nameCase is " + nameCase);
			}

			//System.out.println("total nameCase is " + nameCase);

			// capitalize the first initial of a name
    		text = Conversion.capitalize(text);

    		// Case 1: more than one person with that name
			if (nameCase >= 2) {
				responseText = SYSTEM_PROMPT + "There are more than one \"" + text + "\". Would you be more specific?";
	        	//System.out.println(responseText);

			// Case 2: there is one person
			} else if (nameCase == 1) {

				if (!available){
					// TODO: check if the person is available or not (e.g. if on a mission and comm broke down)
	    			responseText = SYSTEM_PROMPT + "I'm sorry. " + text + " is unavailable at this moment";

	    		} else {

					if (!personList.isEmpty()) {
						person = personList.get(0);
						if (person.getPhysicalCondition().isDead()) {
						    // Case 4: passed away
							int rand = RandomUtil.getRandomInt(1);
							if (rand == 0)
								responseText = SYSTEM_PROMPT + "I'm sorry. " + text + " has passed away and is buried at " + person.getBuriedSettlement().getName();
							else
								responseText = SYSTEM_PROMPT + "Perhaps you don't know that " + text + " is dead and is buried at " + person.getBuriedSettlement().getName();
						}
						else
							unit = person;
					}

					if (!robotList.isEmpty()) {
						robot = robotList.get(0);
						if (robot.getSystemCondition().isInoperable()) {
						    // Case 4: decomissioned
							responseText = SYSTEM_PROMPT + "I'm sorry. " + text + " has been decomissioned.";
						}
						else
							unit = robot;
					}

					if (unit != null) {

/*
		    			// construct a reply in 4 variations
			    		int rand0 = RandomUtil.getRandomInt(3);
			    		if (rand0 == 0)
			        		responseText = "How can I help? /h for guidance/help";
			    		else if (rand0 == 1)
			           		responseText = "I'm listening. /h for guidance/help";
			    		else if (rand0 == 2)
			           		responseText = "I'm all ears. /h for guidance/help";
			        	else if (rand0 == 3)
			        		responseText = "Is there something I can help? /h for guidance/help";
			        	else if (rand0 == 4)
			        		responseText = "";
*/
			    		// may add "over" at the end of a sentence
			    		//int rand1 = RandomUtil.getRandomInt(2);
			    		//if (rand1 > 0 && (rand0 == 1 || rand0 == 3))
			    		//	responseText += " Over.";

			    		responseText = unit.getName() + " : This is " + text + ". ";// + responseText;
			        	//System.out.println(responseText);
					}
	    		}


			// Case 3: doesn't exist, check settlement's name
			} else if (nameCase == 0) {

				//System.out.println("nameCase is 0");
		      	// 2015-12-17 match a settlement's name
		    	if (text.length() >= 2) {
		    		Iterator<Settlement> j = Simulation.instance().getUnitManager().getSettlements().iterator();
					while (j.hasNext()) {
						Settlement settlement = j.next();
						String s_name = settlement.getName();

						if (s_name.equalsIgnoreCase(text)) {
							//name = "System";
							responseText = SYSTEM_PROMPT + "Yes, what would like to know about \"" + s_name + "\" ?";

							settlementCache = settlement;
							//System.out.println("matching settlement name " + s_name);
							break;
						}

						else if (s_name.toLowerCase().contains(text.toLowerCase()) || text.toLowerCase().contains(s_name.toLowerCase())) {
							responseText = SYSTEM_PROMPT + "Do you mean \"" + s_name + "\" ?";
							//System.out.println("partially matching settlement name " + s_name);
							break;
						}
						else
				    		responseText = SYSTEM_PROMPT + "I do not recognize anyone or any settlements by \"" + text + "\"." ;


						// TODO: check vehicle names


						// TODO: check commander's name


					}
		    	}
		    	else
		    		responseText = SYSTEM_PROMPT + "I do not recognize anyone or any settlements by \"" + text + "\"." ;

			}

			else
				responseText = SYSTEM_PROMPT + "I do not recognize anyone or any settlements by \"" + text + "\"." ;

    	}

    	if (len > 0)
    		textArea.appendText(responseText + System.lineSeparator());

		return unit;
    }


    public String[] clarify() {
		String questionText = YOU_PROMPT + "You were mumbling something about....";
		String responseText = null;
		int rand0 = RandomUtil.getRandomInt(4);
		if (rand0 == 0)
			responseText = SYSTEM_PROMPT + "Could you repeat that? /h for guidance/help";
		else if (rand0 == 1)
	   		responseText = SYSTEM_PROMPT + "Pardon me? /h for guidance/help";
		else if (rand0 == 2)
	   		responseText = SYSTEM_PROMPT + "What did you say? /h for guidance/help";
		else if (rand0 == 3)
			responseText = SYSTEM_PROMPT + "I beg your pardon? /h for guidance/help";
		else
			responseText = SYSTEM_PROMPT + "Can you be more specific? /h for guidance/help";

		return new String[]{questionText, responseText};
    }

    public String[] farewell() {
		String questionText = YOU_PROMPT + farewellText();// + System.lineSeparator();
		String responseText = SYSTEM_PROMPT + farewellText();// + System.lineSeparator();
		return new String[]{questionText, responseText};
    }

    public String farewellText() {

    	int r0 = RandomUtil.getRandomInt(5);
    	if (r0 == 0)
    		return "Bye!";
    	else if (r0 == 1)
    		return "Farewell!";
    	else if (r0 == 2)
    		return "Bye now!";
    	else if (r0 == 3)
    		return "Have a nice sol!";
    	else if (r0 == 4)
    		return "Take it easy!";
    	else
    		return "Take care!";
    }


    /**
     * Processes the textfield input
     */
    public void hitEnter() {
    	//System.out.println("starting hitEnter()");
    	//String text = textField.getText();
       	String text = autoFillTextBox.getTextbox().getText();

        if (text != "" && text != null && !text.trim().isEmpty()) {
            textArea.appendText(YOU_PROMPT + text + System.lineSeparator());
            Unit unit = null;

            if (personCache == null && robotCache == null && settlementCache == null) {
            	unit = parseText(text);
            }
            else {
                // if both personCache and robotCache are set to null, then quit the askQuestion() stage and go back to parseText() stage
            	askQuestion(text);
            }

            if (unit != null)
            	if (unit instanceof Person)
            		personCache = (Person) unit;
            	else if (unit instanceof Robot)
            		robotCache = (Robot) unit;
            	else if (unit instanceof Settlement)
            		settlementCache = (Settlement) unit;

        	// Checks if the text already exists
        	if (history.contains(text)) {
        		history.remove(text);
        		history.add(text);
        	}
        	else {
        		history.add(text);
        		historyPointer++;
        	}

            if (onMessageReceivedHandler != null) {
                onMessageReceivedHandler.accept(text);
            }
        }

        autoFillTextBox.getTextbox().clear();
    }

    /**
     * Processes the textfield in the chat box and saves a history of input
     * @param keyEvent
     */
    public void keyHandler(final KeyEvent keyEvent){
        //System.out.println("ChatBox's keyHandler() : keyEvent.getCode() is " + keyEvent.getCode());
        switch (keyEvent.getCode()) {
	        case ENTER:
	          	String text = autoFillTextBox.getTextbox().getText();
	            if (text != "" && text != null && !text.trim().isEmpty()) {
	            	hitEnter();
	            }
	            else {
	            	 autoFillTextBox.getTextbox().clear();
	            }
	            break;

	        case UP:
	            if (historyPointer == 0) {
	                break;
	            }

	            historyPointer--;
                //System.out.println("historyPointer is " + historyPointer);
	            ChatUtil.runSafe(() -> {
	            	///textField.setText(history.get(historyPointer));
	                //textField.selectAll();
	                autoFillTextBox.getTextbox().setText(history.get(historyPointer));
	                autoFillTextBox.getTextbox().selectAll();
	            });

	            break;

	        case DOWN:
	            if (historyPointer >= history.size() - 1) {
	                break;
	            }

                historyPointer++;
                //System.out.println("historyPointer is " + historyPointer);
	            ChatUtil.runSafe(() -> {
	           		//textField.setText(history.get(historyPointer));
	                //textField.selectAll();
	                autoFillTextBox.getTextbox().setText(history.get(historyPointer));
	                autoFillTextBox.getTextbox().selectAll();
	            });

	            break;

	        default:
	            break;
	    }

    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        //textField.requestFocus();
        autoFillTextBox.getTextbox().requestFocus();
    }

    public void setOnMessageReceivedHandler(final Consumer<String> onMessageReceivedHandler) {
        this.onMessageReceivedHandler = onMessageReceivedHandler;
    }

    public void clear() {
        ChatUtil.runSafe(() -> textArea.clear());
    }

    public void print(final String text) {
        Objects.requireNonNull(text, "text");
        ChatUtil.runSafe(() -> textArea.appendText(text));
    }

    public void println(final String text) {
        Objects.requireNonNull(text, "text");
        ChatUtil.runSafe(() -> textArea.appendText(text + System.lineSeparator()));
    }

    public void println() {
        ChatUtil.runSafe(() -> textArea.appendText(System.lineSeparator()));
    }

    public TextArea getTextArea() {
    	return textArea;
    }


    public AutoFillTextBox<String> getAutoFillTextBox() {
    	return autoFillTextBox;
    }

    public Label getTitleLabel() {
    	return titleLabel;
    }

    public JFXButton getBroadcastButton() {
    	return broadcastButton;
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        personCache = null;
        robotCache = null;
        settlementCache = null;
        vehicle = null;
        settlement = null;
        building = null;
        equipment = null;
        mainScene = null;
        if (history != null)
        	history.clear();
        onMessageReceivedHandler = null;
    }


}