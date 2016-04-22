/* Mars Simulation Project
 * ChatBox.java
 * @version 3.08 2015-12-02
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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationState;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import javafx.application.Application;
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

    protected int historyPointer = 0;

    protected boolean hasPerson = false;

    protected Person personCache;
    protected Robot robotCache;
    protected Settlement settlementCache;
    
    protected Vehicle vehicle;
    protected Settlement settlement;
    protected Building building;
    protected Equipment equipment;
    protected MainScene mainScene;
    
    protected final TextArea textArea = new TextArea();
    //protected final TextField textField = new TextField();
    protected final AutoFillTextBox autoFillTextBox;
    
    protected final List<String> history = new ArrayList<>();

    private Consumer<String> onMessageReceivedHandler;
/* 
    private String[] autoCompleteArray = new String[]{ "apple","ball","cat","doll","elephant",
            "fight","georgeous","height","ice","jug",
             "aplogize","bank","call","done","ego",
             "finger","giant","hollow","internet","jumbo",
             "kilo","lion","for","length","primary","stage",
             "scene","zoo","jumble","auto","text",
            "root","box","items","hip-hop","himalaya","nepal",
            "kathmandu","kirtipur","everest","buddha","epic","hotel"};
*/    
    /**
     * Constructor for ChatBox
     */
    public ChatBox(MainScene mainScene) {
    	this.mainScene = mainScene;
    	// 2016-01-01 Added autoCompleteData
    	ObservableList<String> autoCompleteData = createAutoCompleteData();
        
        Label titleLabel = new Label("   MarsNet Chat Box");
        int theme = mainScene.getTheme();
        if (theme == 6)
        	titleLabel.setStyle("-fx-text-fill: white; -fx-font: bold 12pt 'Corbel'; -fx-effect: dropshadow( one-pass-box , blue , 8 , 0.0 , 2 , 0 );");
        else if (theme == 7)
            titleLabel.setStyle("-fx-text-fill: white; -fx-font: bold 12pt 'Corbel'; -fx-effect: dropshadow( one-pass-box , orange , 8 , 0.0 , 2 , 0 );");
        else 
            titleLabel.setStyle("-fx-text-fill: white; -fx-font: bold 12pt 'Corbel'; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );");
        
        	
        
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font: 11pt 'Corbel';");
        textArea.setTooltip(new Tooltip ("Chatters on MarsNet"));
		//ta.appendText("System : WARNING! A small dust storm 20 km away NNW may be heading toward the Alpha Base" + System.lineSeparator());
  		textArea.appendText("<< Connecting to MarsNet...done. >>" + System.lineSeparator());
  		textArea.appendText("System : how can I help? " + System.lineSeparator());

  		// 2016-01-01 Replaced textField with autoFillTextBox
        autoFillTextBox = new AutoFillTextBox(autoCompleteData);
        autoFillTextBox.setFilterMode(false);
        autoFillTextBox.getTextbox().addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
        	keyHandler(keyEvent);
        });
        
        autoFillTextBox.getTextbox().requestFocus();
        autoFillTextBox.getStylesheets().addAll("/css/autofill.css");
        autoFillTextBox.getTextbox().setPrefWidth(560);
        autoFillTextBox.setStyle("-fx-font: 11pt 'Corbel';");
        autoFillTextBox.setPadding(new Insets(0, 0, 0, 0));
  		autoFillTextBox.setTooltip(new Tooltip ("Use UP/DOWN arrows to scroll input history."));
  		autoFillTextBox.getTextbox().setPromptText("Type your msg here");// to broadcast to a channel");
  		
  		//TextField tf = cb.getTextField();
  		//tf.setTooltip(new Tooltip ("Use UP/DOWN arrows to scroll input history."));
  		//tf.setPromptText("Type your msg here");// to broadcast to a channel");
        //textField.setStyle("-fx-font: 11pt 'Corbel';");
        //textField.setPrefWidth(500);
        //textField.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
        //	keyHandler(keyEvent);
        //});

        //Button button = new Button("Submit");
        MaterialDesignButton button = new MaterialDesignButton("Broadcast");
        //button.setAlignment(Pos.CENTER_RIGHT);
        button.setTooltip(new Tooltip ("Click to broadcast"));
        //button.setPadding(new Insets(5, 5, 5, 5));
        button.setStyle("-fx-font: bold 10pt 'Corbel';");
        button.setOnAction(e -> {
        	hitEnter();
        });

        HBox hbox = new HBox();
        //hbox.setPadding(new Insets(2, 2, 2, 2));
        //hbox.setSpacing(5);      
        //textField.setPadding(new Insets(5, 5, 5, 5));
        //hbox.getChildren().addAll(button, textField);
        hbox.getChildren().addAll(button, autoFillTextBox);//.getTextbox());

        setTop(titleLabel);
        setCenter(textArea);
        setBottom(hbox);

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
		Iterator<Settlement> i = settlementList.iterator();
		while (i.hasNext()) {
			Settlement s = i.next();
				nameList.add(s.getName());
			
			Iterator<Person> j = s.getAllAssociatedPeople().iterator();
			while (j.hasNext()) {
				Person p = j.next();
				nameList.add(p.getName());
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
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
    
    /**
     * Processes a question and return an answer regarding an unit
     * @param text
     */
    //2015-12-21 askQuestion()
    public void askQuestion(String text) {
    	String sys = "System";
    	String responseText = null;
    	String questionText = null;
    	Unit cache = null;
    	String name = null;

    	if (personCache != null) {
    		cache = (Person) personCache;
    		name = cache.getName();
    	}
    	else if (robotCache != null) {
    		cache = (Robot) robotCache;
    		name = cache.getName();
    	}
    	else if (settlementCache != null) {
    		cache = (Settlement) settlementCache;
    		name = cache.getName();
    	}	

    	// Case 0 : exit
    	if (text.equalsIgnoreCase("exit") || text.equalsIgnoreCase("quit")) {
    		// set personCache and robotCache to null so as to quit the conversation

    		sys = name;
    		questionText = "You : have a nice sol. Bye!";
    		if (settlementCache != null)
    			responseText = "Bye! " + System.lineSeparator() + " System : exit inquiry regarding " + name;
    		else
    			responseText = "Bye! " + System.lineSeparator() + " System : " + sys + " had left the conversation.";
    		
    		personCache = null; 
	    	robotCache = null;
	    	settlementCache = null;
    	}
 	
    	// Case 1: ask about a settlement
    	else if (settlementCache != null) {
    		
    		if (isInteger(text, 10) ) {
    		    
    	    	int num = Integer.parseUnsignedInt(text, 10);
    	    	
    	    	if (num == 0) {
    	    		// exit
    	    		sys = name;
    	    		questionText = "You : exit asking about the " + name;
    	    		responseText = "Bye! " + System.lineSeparator() + " System : exit the inquiry.";
    	    		
    	    		personCache = null; 
    		    	robotCache = null;
    		    	settlementCache = null;
    		    }
    	
    	    	else if (num == 1) {
    	    		questionText = "You : how many beds are there in total ? ";
    	    		responseText = "The total number of beds is " + settlementCache.getPopulationCapacity();
    	    		
    	    	}

    	    	else if (num == 2) {
    	    		questionText = "You : how many beds that have already been designated to a person ? ";
    	    		responseText = "There are " + settlementCache.getTotalNumDesignatedBeds() + " designated beds. ";
    	    		
    	    	}

    	    	else if (num == 3) {
    	    		questionText = "You : how many beds that are currently NOT occupied ? ";
    	    		responseText = "There are " + (settlementCache.getPopulationCapacity() - settlementCache.getSleepers()) + " unoccupied beds. ";
    	    		
    	    	}

    	    	else if (num == 4) {
    	    		questionText = "You : how many people are currently sleeping ? ";
    	    		responseText = "There are " + settlementCache.getSleepers() + " people sleeping at this moment. ";
    	    		
    	    	}
	
    	    } else {
        		// set personCache and robotCache to null only if you want to quit the conversation
        		//personCache = null; 
    	    	//robotCache = null;
        		sys = name;
        		questionText = "You : you were mumbling something about....";
        		int rand0 = RandomUtil.getRandomInt(3);
        		if (rand0 == 0)
            		responseText = "could you repeat that?";
        		else if (rand0 == 1)
               		responseText = "pardon me?";
        		else if (rand0 == 2)
               		responseText = "what did you say?";
            	else if (rand0 == 3)
            		responseText = "I beg your pardon?"; 		
        	}
 		
    	}
    	
    	// Case 2: ask to talk to a person or robot
    	else if (isInteger(text, 10) && settlementCache == null) {
    
	    	int num = Integer.parseUnsignedInt(text, 10);
	    	
	    	if (num == 0) {
	    		// exit
	    		sys = name;
	    		questionText = "You : have a nice sol. Bye!";
	    		responseText = "Bye! " + System.lineSeparator() + " System : " + sys + " had left the conversation.";
	    		
	    		personCache = null; 
		    	robotCache = null;
		    	settlementCache = null;
		    }
	
	    	else if (num == 1) {
	    		questionText = "You : what is your LocationState ?";
	    		LocationState state = cache.getLocationState();
	    		if (state != null) {
	    			if (personCache != null) {
	    				if (personCache.getBuildingLocation() != null)
	    					responseText = "My current LocationState is " + state.getName() + " (" + personCache.getBuildingLocation().getNickName() + ")";
	    				else {
	    					responseText = "My current LocationState is " + state.getName();				
	    				}
	    	    	}
	    	    	else if (robotCache != null) {
	    	    		responseText = "My current LocationState is " + state.getName() + " (" + robotCache.getBuildingLocation().getNickName() + ")";
	    	    	}	
	    		}
	    		else
	    			responseText = "My current LocationState is " + state;
	    	}
	
	       	else if (num == 2) {
	    		questionText = "You : what are you doing ?";
	    		if (personCache != null) {
	    			responseText = personCache.getTaskDescription();
    	    	}
    	    	else if (robotCache != null) {
    	    		responseText = robotCache.getTaskDescription();
    	    	}
	       	}
	
	    	else if (num == 3) {
	    		questionText = "You : where is your designated quarters ? ";
	    		Point2D bed = personCache.getBed();
	    		if (bed == null)
	    			responseText = "I don't have my own private quarters.";
	    		else {
	    			if (personCache != null) {
	    				Settlement s1 = personCache.getSettlement();
		    			if (s1 != null) {	
		    				// check to see if a person is on a trading mission
		    				Settlement s2 = personCache.getAssociatedSettlement();		    				
		    				if (s2 != null) {
		    					if (s1 == s2)
		    						responseText = "My designated bed is at (" + bed.getX() + ", " + bed.getY() + ") in " 
		    								+ personCache.getQuarters() + " at " + s1;
		    					else
				    				// yes, a person is on a trading mission
		    						responseText = "My designated bed is at (" + bed.getX() + ", " + bed.getY() + ") in " 
		    								+ personCache.getQuarters() + " at " + s2;
		    				}
		    			}
		    			else
		    				responseText = "My designated bed is at (" + bed.getX() + ", " + bed.getY() + ") in " 
		    						+ personCache.getQuarters();
	    	    	}
	    	    	else if (robotCache != null) {
	    	    		responseText = "I don't need one. My battery can be charged at any robotic station.";
	    	    	}
	    		} 		
	    	}
	
	    	else if (num == 4) {
	    		questionText = "You : what is your container unit ?";
	    		Unit c = cache.getContainerUnit();
	    		if (c != null)
	    			responseText = "My container unit is " + c.getName();
	    		else
	    			responseText = "I don't have a container unit. ";
	    	}
	
	    	else if (num == 5) {
	
	    		questionText = "You : what is your top container unit ?";
	    		Unit tc = cache.getTopContainerUnit();
	    		if (tc != null)
	    			responseText = "My top container unit is " + tc.getName();
	    		else
	    			responseText = "I don't have a top container unit.";
	
	    	}
	    	else if (num == 6) {
	    		questionText = "You : what is your LocationSituation ?";
	       		responseText = "My LocationSituation is " + Conversion.capitalize(cache.getLocationSituation().getName());
	    	}
	
	       	else if (num == 7) {
	       		questionText = "You : what vehicle are you in and how is it ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	           		String d = cache.getVehicle().getDescription();
	           		String status = cache.getVehicle().getStatus();
	       			responseText = "My vehicle is " + v.getName() + " (a " + Conversion.capitalize(d)
	       				+ " type). It's currently " + status.toLowerCase() + ".";
	       		}
	       		else
	       			responseText = "I'm not on a vehicle.";
	       	}
	
	    	else if (num == 8) {
	    		questionText = "You : what building are you at ?";
	    		Settlement s = cache.getSettlement();
	    		if (s != null) {
		    		//Building b1 = s.getBuildingManager().getBuilding(cache);
		    		Building b = cache.getBuildingLocation();
		    		if (b != null) // && b1 != null)
		    			responseText = "The building I'm in is " + b.getNickName();
		    				//+ "  (aka " + b1.getNickName() + ").";
		    		else
		    			responseText = "I'm not in a building.";
	    		}
	    		else
	    			responseText = "I'm not in a building.";
	
	    	}
	
	    	else if (num == 9) {
	    		questionText = "You : what is the settlement that you are at ?";
	    		Settlement s = cache.getSettlement();
	   			if (s != null)
	   				responseText = "My settlement is " + s.getName();
	   			else
	   				responseText = "I'm not inside a settlement";
	
	    	}
	
	    	else if (num == 10) {
	    		questionText = "You : what is your associated settlement ?";
	    		Settlement s = cache.getAssociatedSettlement();
	    		if (s  != null) {
	    			responseText = "My associated settlement is " + s.getName();
	    		}
	    		else
	    			responseText = "I don't have an associated settlement";
	    	}
	
	    	else if (num == 11) {
	    		questionText = "You : what is the buried settlement ?";
	    		Settlement s = cache.getBuriedSettlement();
	    		if (s  != null) {
	           		responseText = "The buried settlement is " + s.getName();
	           		sys = "System : ";
	       		}
	       		else
	       			responseText = "I'm not dead.";
	    	}
	
	    	else if (num == 12) {
	    		questionText = "You : what is your vehicle's container unit ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Unit c = v.getContainerUnit();
	       			if (c != null)
	       				responseText = "My vehicle's container unit is " + c.getName();
	       			else
	       				responseText = "My vehicle doesn't have a container unit.";
	
	       		}
	       		else
	       			responseText = "I'm not on a vehicle.";
	       	}
	
	    	else if (num == 13) {
	    		questionText = "You : what is your vehicle's top container unit ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Unit tc = v.getTopContainerUnit();
	       			if (tc != null)
	       				responseText = "My vehicle's top container unit is " + tc.getName();
	       			else
	       				responseText = "My vehicle doesn't have a top container unit.";
	       		}
	       		else
	       			responseText = "I'm not on a vehicle.";
	       	}
	
	    	else if (num == 14) {
	    		questionText = "You : what building does your vehicle park at ?";
	    		Vehicle v = cache.getVehicle();
		       	if (v != null) {
		       		Settlement s = v.getSettlement();
		       		if (s != null) {
		       			Building b = s.getBuildingManager().getBuilding(v);
		       			if (b != null)
		       				responseText = "My vehicle is parked inside " + b.getNickName();
		       			else
		       				responseText = "My vehicle does not park inside a building/garage";
		       		}
		       		else
		       			responseText = "My vehicle is not at a settlement.";
	   			}
		       	else
	       			responseText = "I'm not on a vehicle.";
	       	}
	
	    	else if (num == 15) {
	    		questionText = "You : what settlement is your vehicle located at ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Settlement s = v.getSettlement();
	       			if (s != null)
	       				responseText = "My vehicle is at " + s.getName();
	       			else
	       				responseText = "My vehicle is not at a settlement.";
	       		}
	       		else
	       			responseText = "I'm not on a vehicle.";
	    	}
	
	    	else {
	    		questionText = "System : you were mumbling about...";
	    		responseText = "Can you be more specific ?";
	    	}
	    	
    	}
    	
    	else if (text.equalsIgnoreCase("mission")) {
    		sys = name;
    		questionText = "You : are you involved in any particular mission at this moment?";
    		Mission mission = null;
    		if (personCache != null) {
    			mission = personCache.getMind().getMission();
	    	}
	    	else if (robotCache != null) {
	    		mission = robotCache.getBotMind().getMission();
	    	}
    		
    		if (mission == null)
    			responseText = "No. I'm not. ";
    		else
    			responseText = mission.getDescription();
    	}
    	
    	else if (text.equalsIgnoreCase("task")) {
    		sys = name;
    		questionText = "You : what are you doing at this moment?";
    		String task = null;
    		if (personCache != null) {
        		task = personCache.getMind().getTaskManager().getTaskDescription(true);
	    	}
	    	else if (robotCache != null) {
	    		task = robotCache.getBotMind().getTaskManager().getTaskDescription(true);
	    	}
    		if (task == null)
    			responseText = "No. I'm not. ";
    		else
    			responseText = task;
    	}
    	
    	else {
    		// set personCache and robotCache to null only if you want to quit the conversation
    		//personCache = null; 
	    	//robotCache = null;
    		sys = name;
    		questionText = "You : you were mumbling something about....";
    		int rand0 = RandomUtil.getRandomInt(3);
    		if (rand0 == 0)
        		responseText = "could you repeat that?";
    		else if (rand0 == 1)
           		responseText = "pardon me?";
    		else if (rand0 == 2)
           		responseText = "what did you say?";
        	else if (rand0 == 3)
        		responseText = "I beg your pardon?"; 		
    	}
    	
    	textArea.appendText(questionText + System.lineSeparator());	
		sys = name;
   		textArea.appendText(sys + " : " + responseText + System.lineSeparator());
    }

    /*
     * Parses the text and interprets the contents in the chat box
     * @param input text
     */
    //2015-12-01 Added parseText()
    public Unit parseText(String text) {
    	Unit unit = null;
    	String responseText = null;
    	String name = "System";
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

    	else if (text.equalsIgnoreCase("\\h")
    			|| text.equalsIgnoreCase("/h")
    			|| text.equalsIgnoreCase("h")
    			|| text.equalsIgnoreCase("help")
    			|| text.equalsIgnoreCase("/help") 
    			|| text.equalsIgnoreCase("\\help")
    			|| text.equals("\\?")
    			|| text.equals("?")
    			|| text.equals("/?")
    			) {

    		responseText = name + " : (1) type in the name of a person/bot/settlement. (2) ask a pre-defined question by entering a number (1 to 15).";
    		//proceed = true; 		
    		
    	}
    	
     	else if (text.equalsIgnoreCase("quit")
    			|| text.equalsIgnoreCase("/quit") 
    			|| text.equalsIgnoreCase("\\quit")
    			|| text.equalsIgnoreCase("\\q")
    			//|| text.equalsIgnoreCase("q")
    			|| text.equalsIgnoreCase("/q")
    			|| text.equalsIgnoreCase("/exit") 
    			|| text.equalsIgnoreCase("\\exit")
    			|| text.equalsIgnoreCase("\\x")
    			//|| text.equalsIgnoreCase("x")
    			|| text.equalsIgnoreCase("/x")

    			) {

    		responseText = name + " : Bye! ";
    		//proceed = true; 		
            mainScene.getFlyout().dismiss();

    		
    	}
    	else if (len >= 2 && text.substring(0,2).equalsIgnoreCase("hi")) {

    		if (len > 2) {
    			text = text.substring(2, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
     	   		responseText = name + " : Hi Earth Control, how can I help?";
    		}
    	}

    	else if (len >= 5 && text.substring(0,5).equalsIgnoreCase("hello")) {

    		if (len > 5) {
    			text = text.substring(5, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
     	   		responseText = name + " : Hello Earth Control, how can I help?";
    		}
    	}

    	else if (len >= 3 && text.substring(0,3).equalsIgnoreCase("hey")) {

    		if (len > 3) {
    			text = text.substring(3, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
     	   		responseText = name + " : Hey Earth Control, how can I help?";
    		}
    	}

    	else if (len >= 2) {
    		proceed = true;
    	}


    	// Part 2 //

    	if (text == null) {
    		responseText = name + " : I'm afraid I don't understand. Could you repeat that?";
    	}

    	else if (text.length() == 1) {
		   	responseText = name + " : I'm afraid I don't understand. Could you repeat that?";
    	}

    	else if (proceed) { // && text.length() > 1) {
	    	//System.out.println("B: text is " + text);
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
				responseText = name + " : there are more than one \"" + text + "\". Would you be more specific?";
	        	//System.out.println(responseText);

			// Case 2: there is one person
			} else if (nameCase == 1) {

				if (!available){
					// TODO: check if the person is available or not (e.g. if on a mission and comm broke down)
	    			responseText = name + " : I'm sorry. " + text + " is unavailable at this moment";

	    		} else {

					if (!personList.isEmpty()) {
						person = personList.get(0);
						if (person.getPhysicalCondition().isDead()) {
						    // Case 4: passed away
					    	responseText = name + " : I'm sorry. " + text + " has passed away.";
						}
						else
							unit = person;
					}

					if (!robotList.isEmpty()) {
						robot = robotList.get(0);
						if (robot.getPhysicalCondition().isDead()) {
						    // Case 4: decomissioned
							responseText = name + " : I'm sorry. " + text + " has been decomissioned.";
						}
						else
							unit = robot;
					}

					if (unit != null) {
		    			// construct a reply in 4 variations
			    		int rand0 = RandomUtil.getRandomInt(3);
			    		if (rand0 == 0)
			        		responseText = "how can I help?";
			    		else if (rand0 == 1)
			           		responseText = "I'm listening.";
			    		else if (rand0 == 2)
			           		responseText = "I'm all ears.";
			        	else if (rand0 == 3)
			        		responseText = "Is there something I can help?";
			        	else if (rand0 == 4)
			        		responseText = "";

			    		// may add "over" at the end of a sentence
			    		int rand1 = RandomUtil.getRandomInt(2);
			    		if (rand1 > 0 && (rand0 == 1 || rand0 == 3))
			    			responseText += " Over.";

			    		responseText = unit.getName() + " : This is " + text + ". " + responseText;
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
							responseText = name + " : yes, what would like to know about \"" + s_name + "\" ?";
							
							settlementCache = settlement;
							//System.out.println("matching settlement name " + s_name);
							break;
						}

						else if (s_name.toLowerCase().contains(text.toLowerCase()) || text.toLowerCase().contains(s_name.toLowerCase())) {
							responseText = name + " : do you mean \"" + s_name + "\" ?";
							//System.out.println("partially matching settlement name " + s_name);
							break;
						}
						else
				    		responseText = name + " : I do not recognize anyone or any settlements by \"" + text + "\"." ;

						
						// check vehicle names
						
						
						// check commander's name
						
						
					}
		    	}
		    	else
		    		responseText = name + " : I do not recognize anyone or any settlements by \"" + text + "\"." ;

			}

			else
				responseText = name + " : I do not recognize anyone or any settlements by \"" + text + "\"." ;

    	}

		textArea.appendText(responseText + System.lineSeparator());

		return unit;
    }

    /**
     * Processes the textfield input
     */
    public void hitEnter() {
    	//String text = textField.getText();  	
    	String text = autoFillTextBox.getTextbox().getText();
    	
        if (text != "" && text != null && !text.trim().isEmpty()) {
            textArea.appendText("You : " + text + System.lineSeparator());
            Unit unit = null;

            if (personCache == null && robotCache == null && settlementCache == null)
            	unit = parseText(text);
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
            
            autoFillTextBox.getTextbox().clear();
            //textField.clear();
        }
    }

    /**
     * Processes the textfield in the chat box and saves a history of input
     * @param keyEvent
     */
    public void keyHandler(final KeyEvent keyEvent){
        //System.out.println("ChatBox's keyHandler() : keyEvent.getCode() is " + keyEvent.getCode());
        switch (keyEvent.getCode()) {
	        case ENTER:
	        	hitEnter();
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

    //public TextField getTextField() {
    //	return textField;
    //}
    
    public AutoFillTextBox getAutoFillTextBox() {
    	return autoFillTextBox;
    }
}