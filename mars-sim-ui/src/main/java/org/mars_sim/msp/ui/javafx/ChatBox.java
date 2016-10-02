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

import org.mars_sim.msp.core.Msg;
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

import com.jfoenix.controls.JFXButton;

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
    protected final JFXButton button;
    //protected final TextField textField = new TextField();
    protected final AutoFillTextBox autoFillTextBox;
    
    protected final List<String> history = new ArrayList<>();

    private Consumer<String> onMessageReceivedHandler;

    /**
     * Constructor for ChatBox
     */
    public ChatBox(MainScene mainScene) {
    	this.mainScene = mainScene;
    	// 2016-01-01 Added autoCompleteData
    	ObservableList<String> autoCompleteData = createAutoCompleteData();
        
        titleLabel = new Label("  " + Msg.getString("ChatBox.title")); //$NON-NLS-1$
       	       
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font: 11pt 'Corbel';"
        		+ "-fx-background-color: black;" 
        		//+ "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"
        		+ "-fx-text-fill: orange;"
        		);
        
        textArea.setStyle("-fx-background-image: url('" + image + "'); " +
                   "-fx-background-position: center center; " +
                   "-fx-background-repeat: stretch;");
        textArea.setTooltip(new Tooltip ("Chatters on MarsNet"));
		//ta.appendText("System : WARNING! A small dust storm 20 km away NNW may be heading toward the Alpha Base" + System.lineSeparator());
  		
  		// 2016-01-01 Replaced textField with autoFillTextBox
        autoFillTextBox = new AutoFillTextBox(autoCompleteData);
        autoFillTextBox.getStylesheets().addAll("/css/autofill.css");
        autoFillTextBox.setStyle(
        		"-fx-background-color: grey" 
        		+ "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"
        		+ "-fx-text-fill: white;"
        		);          

        autoFillTextBox.setFilterMode(false);
        autoFillTextBox.getTextbox().addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
        	keyHandler(keyEvent);
        });
        
 
        autoFillTextBox.getTextbox().setPrefWidth(560);
        //autoFillTextBox.setStyle("-fx-font: 11pt 'Corbel';");
        //autoFillTextBox.setPadding(new Insets(0, 0, 0, 0));
  		autoFillTextBox.setTooltip(new Tooltip ("Use UP/DOWN arrows to scroll input history."));
  		autoFillTextBox.getTextbox().setPromptText("Type your msg here");// to broadcast to a channel"); 			
 
  		
  		button = new JFXButton("Broadcast".toUpperCase());
        //MaterialDesignButton button = new MaterialDesignButton("Broadcast");
        //button.setPadding(new Insets(5, 5, 5, 5));
/*        button.setStyle("-fx-font: bold 10pt 'Corbel';" +
     		   "-fx-shadow-highlight-color : transparent;" +  // if you don't want a 3d effect highlight.
     		   "-fx-outer-border : transparent;" +  // if you don't want a button border.
     		   "-fx-inner-border : transparent;" +  // if you don't want a button border.
     		   "-fx-focus-color: transparent;" +  // if you don't want any focus ring.
     		   "-fx-faint-focus-color : transparent;"  // if you don't want any focus ring.
     		   //"-fx-background-radius: 2px;"
     		   );
*/
		//button.getStyleClass().add("button-raised");
		
        button.setTooltip(new Tooltip ("Click or hit enter to broadcast"));

        button.setOnAction(e -> {
          	String text = autoFillTextBox.getTextbox().getText();
            if (text != "" && text != null && !text.trim().isEmpty()) {
            	hitEnter();
            }
            else {
            	 autoFillTextBox.getTextbox().clear();
            }
        });
  
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(0, 0, 0, 0));
        hbox.getChildren().addAll(button, autoFillTextBox);//.getTextbox());

        setTop(titleLabel);
        setCenter(textArea);
        setBottom(hbox);
        
        connect();
    }
     
    
    public void connect() {
        textArea.appendText("<< Connection to MarsNet established >>" + System.lineSeparator());
  		
  		int rand = RandomUtil.getRandomInt(2);
  		
  		if (rand == 0)
  			textArea.appendText("System : how can I help you ? h for help");
  		else if (rand == 1)
  			textArea.appendText("System : how may I assist you ? h for help");
  		else if (rand == 2)
  			textArea.appendText("System : Is there anything I can help ? h for help");

  		textArea.appendText(System.lineSeparator() + System.lineSeparator());
    }
    
    
    /*
     * Display the initial system greeting and update the css style
     */
    // 2016-06-17 Added update() 
    public void update() {   

    	int theme = MainScene.getTheme();
        if (theme == 6) {
        	button.getStylesheets().clear();
        	button.setStyle("-fx-text-fill: #3291D2;");
        	titleLabel.getStylesheets().clear();
        	titleLabel.setStyle("-fx-text-fill: #3291D2;"
        			+ " -fx-font: bold 12pt 'Corbel';"
        			//+ " -fx-effect: dropshadow( one-pass-box , blue , 8 , 0.0 , 2 , 0 );"
        			);
        	textArea.getStylesheets().clear();
            textArea.setStyle("-fx-background-image: url('" + image + "'); " +
                    "-fx-background-position: center center; " +
                    "-fx-background-repeat: stretch;");
            textArea.setStyle("-fx-text-fill: #3291D2;"
            			+ "-fx-background-color: #000000;" );
        }
        else if (theme == 7) {
        	button.getStylesheets().clear();
        	button.setStyle("-fx-text-fill: #E5AB00;");
        	titleLabel.getStylesheets().clear();
            titleLabel.setStyle("-fx-text-fill: #E5AB00;"
            		+ " -fx-font: bold 12pt 'Corbel';"
            		//+ " -fx-effect: dropshadow( one-pass-box , orange , 8 , 0.0 , 2 , 0 );"
            		);
        	textArea.getStylesheets().clear();
            textArea.setStyle("-fx-background-image: url('" + image + "'); " +
                    "-fx-background-position: center center; " +
                    "-fx-background-repeat: stretch;");
            textArea.setStyle("-fx-text-fill: #E5AB00;"
        			+ "-fx-background-color: #000000;" );
        }
        else {
        	button.getStylesheets().clear();
        	button.setStyle("-fx-text-fill: #E5AB00;");
        	titleLabel.getStylesheets().clear();
            titleLabel.setStyle("-fx-text-fill: #E5AB00;"
            		+ " -fx-font: bold 12pt 'Corbel';"
            		//+ " -fx-effect: dropshadow( one-pass-box , orange , 8 , 0.0 , 2 , 0 );"
            		);
        	textArea.getStylesheets().clear();
            textArea.setStyle("-fx-background-image: url('" + image + "'); " +
                    "-fx-background-position: center center; " +
                    "-fx-background-repeat: stretch;");
            textArea.setStyle("-fx-text-fill: #E5AB00;"
        			+ "-fx-background-color: #000000;" );
        }
        
/*        
        textArea.appendText("<< Connection to MarsNet established >>" + System.lineSeparator());
  		
  		int rand = RandomUtil.getRandomInt(2);
  		
  		if (rand == 0)
  			textArea.appendText("System : how can I help you ? h for help");
  		else if (rand == 1)
  			textArea.appendText("System : how may I assist you ? h for help");
  		else if (rand == 2)
  			textArea.appendText("System : Is there anything I can help ? h for help");

  		textArea.appendText(System.lineSeparator() + System.lineSeparator());
*/
        
  		textArea.positionCaret(textArea.getText().length());
    }

    public void closeChatBox() {
    			
    	textArea.appendText("You : farewell!" + System.lineSeparator() + "<< Disconnected from MarsNet >>" + System.lineSeparator() + System.lineSeparator());    	
  
    	personCache = null; 
    	robotCache = null;
    	settlementCache = null;
    	
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
    	String theOtherParty = "System";
    	String responseText = null;
    	String questionText = null;
    	Unit cache = null;
    	String name = null;
    	boolean help = false;

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

    	// Case 0 : exit the conversation
    	if (text.equalsIgnoreCase("exit") || text.equalsIgnoreCase("x") || text.equalsIgnoreCase("/x")
    			|| text.equalsIgnoreCase("quit") || text.equalsIgnoreCase("q") || text.equalsIgnoreCase("/q") 
    			|| text.equalsIgnoreCase("bye")) {
    		// set personCache and robotCache to null so as to quit the conversation
   		
    		int r0 = RandomUtil.getRandomInt(5);

			if (r0 == 0)
				questionText = "You : bye!";
			else if (r0 == 1)
				questionText = "You : farewell!";
			else if (r0 == 2)
				questionText = "You : bye now!";
			else if (r0 == 3)
				questionText = "You : have a nice sol!";
			else if (r0 == 4)
				questionText = "You : Take it easy!";
			else 
				questionText = "You : Take care!";
			    
    		
    		if (settlementCache != null)
    			// it's a settlement 
    			responseText = "disconnected.";
    		
    		else {
    			
    			int rand = RandomUtil.getRandomInt(6);

    			if (rand == 0)
    				responseText = "bye!" + System.lineSeparator() + "System : disconnected. "; //" + sys + " had left the conversation.";
    			else if (rand == 1)
    				responseText = "farewell!" + System.lineSeparator() + "System : " + theOtherParty + " had left the conversation.";
    			else if (rand == 2)
    				responseText = "bye now" + System.lineSeparator() + "System : " + theOtherParty + " had left the conversation.";
    			else if (rand == 3)
    				responseText = "have a nice sol!" + System.lineSeparator() + "System : " + theOtherParty + " had left the conversation.";
    			else if (rand == 4)
    				responseText = "Take it easy!" + System.lineSeparator() + "System : " + theOtherParty + " had left the conversation.";
    			else if (rand == 5)
    				responseText = "Take care!" + System.lineSeparator() + "System : " + theOtherParty + " had left the conversation.";
    			else
    				responseText = "oh well! " + System.lineSeparator() + "System : " + theOtherParty + " had left the conversation.";
    		    
    		}
    		
    		personCache = null; 
	    	robotCache = null;
	    	settlementCache = null;
    	}
 	
    	// Case 1: ask about a settlement
    	else if (settlementCache != null) {
    		
    		if (isInteger(text, 10)) {
    		    
    	    	int num = Integer.parseUnsignedInt(text, 10);
/*    	    	
    	    	if (num == 0) {
    	    		// exit
    	    		sys = name;
    	    		questionText = "You : have a nice sol. Bye!";
    	    		responseText = "Bye! " + System.lineSeparator() + "System : exit the inquiry regarding " + name;
    	    		
    	    		personCache = null; 
    		    	robotCache = null;
    		    	settlementCache = null;
    		    }
*/    	
    	    	if (num == 1) {
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
    	    		questionText = "You : how many beds are currently occupied ? ";
    	    		responseText = "There are " + settlementCache.getSleepers() + " occupied beds with people sleeping on it at this moment. ";
    	    		
    	    	}
	
    	    	else {
    	    		questionText = "You : You entered '" + num + "'.";
    	    		responseText = "Sorry. This number is not assigned to a valid question.";
    	    	}
    	    	
    	    // if it's not a integer input	
    		} else if (text.contains("bed")
    				|| text.contains("sleep") 
    				|| text.equalsIgnoreCase("lodging") 
    				|| text.contains("quarters")) {
    			  			
    			questionText = "You : how well are the beds utilized ? ";
	    		responseText = "Total number of beds : " + settlementCache.getPopulationCapacity() + System.lineSeparator() +
	    				"Desginated beds : " + settlementCache.getTotalNumDesignatedBeds() + System.lineSeparator() +
	    				"Unoccupied beds : " + (settlementCache.getPopulationCapacity() - settlementCache.getSleepers())  + System.lineSeparator() +
	    				"Occupied beds : " + settlementCache.getSleepers() + System.lineSeparator();
    		} 
    		
        	else if (text.contains("help") || text.equalsIgnoreCase("/h") || text.equalsIgnoreCase("h")){
    	    	
        		help = true;
    	    	questionText = "You : I need some help. What are the available commands ? ";
    			responseText = "Keywords are 'bed', 'lodging', 'sleep', 'quarters" + System.lineSeparator()
						+ "Specific questions from 1 to 4" + System.lineSeparator()
    					+ "To quit, enter 'quit', 'exit', 'bye', 'q', or 'x'" + System.lineSeparator()
    					+ "For help, enter 'help', or 'h'" + System.lineSeparator();	    	
    	    		    		
    	    } else {    	    	

        		questionText = "You : you were mumbling something about....";
	    		int rand0 = RandomUtil.getRandomInt(4);
	    		if (rand0 == 0)
	        		responseText = "could you repeat that?";
	    		else if (rand0 == 1)
	           		responseText = "pardon me?";
	    		else if (rand0 == 2)
	           		responseText = "what did you say?";
	        	else if (rand0 == 3)
	        		responseText = "I beg your pardon?"; 
	        	else
	        		responseText = "Can you be more specific ?";
        		
        	}
 		
    	}	
    	// Case 2: ask to talk to a person or robot
    	else if (settlementCache == null) {
	
    		int num = 0;
  
    		if (isInteger(text, 10))  			
    			num = Integer.parseUnsignedInt(text, 10);
    		
    		if (num == 1 || text.equalsIgnoreCase("where") || text.equalsIgnoreCase("location")) {// || text.contains("location")) {
	    		questionText = "You : where are you ? "; //what is your Location State [Expert Mode only] ?";
	    		LocationState state = cache.getLocationState();
	    		if (state != null) {
	    			if (personCache != null) {
	    				if (personCache.getBuildingLocation() != null)
	    					responseText = "I'm " + state.getName() + " (" + personCache.getBuildingLocation().getNickName() + ")";
	    				else {
	    					responseText = "I'm " + state.getName();				
	    				}
	    	    	}
	    	    	else if (robotCache != null) {
	    	    		responseText = "I'm " + state.getName() + " (" + robotCache.getBuildingLocation().getNickName() + ")";
	    	    	}	
	    		}
	    		else
	    			responseText = "It may sound strange but I don't know where I'm at. ";
	    	}
	
	    	else if (num == 2 || text.contains("located") || text.contains("location") && text.contains("situation")) {
	    		questionText = "You : where are you located ? "; //what is your Location Situation [Expert Mode only] ?";
	       		responseText = "I'm located at " + Conversion.capitalize(cache.getLocationSituation().getName());
	    	}
    		
	       	else if (num == 3 || text.equalsIgnoreCase("task") || text.equalsIgnoreCase("activity") || text.equalsIgnoreCase("action")) {
	    		questionText = "You : what are you doing ?";
	    		if (personCache != null) {
	    			responseText = personCache.getTaskDescription();
    	    	}
    	    	else if (robotCache != null) {
    	    		responseText = robotCache.getTaskDescription();
    	    	}
	    		
	       	}
	
	       	else if (num == 4 || text.equalsIgnoreCase("mission")) {
    		
    			//sys = name;
	    		questionText = "You : are you involved in a particular mission at this moment?";
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
    		
	    	else if (num == 5 || text.equalsIgnoreCase("bed") || text.equalsIgnoreCase("quarter") || text.equalsIgnoreCase("quarters")) {
	    		questionText = "You : where is your designated quarters/bed ? ";
	    		Point2D bed = personCache.getBed();
	    		if (bed == null)
	    			responseText = "I don't have my own private quarters/bed.";
	    		else {
	    			if (personCache != null) {
	    				Settlement s1 = personCache.getSettlement();
		    			if (s1 != null) {	
		    				// check to see if a person is on a trading mission
		    				Settlement s2 = personCache.getAssociatedSettlement();		    				
		    				if (s2 != null) {
		    					if (s1 == s2)
		    						responseText = "My designated quarters/bed is at (" + bed.getX() + ", " + bed.getY() + ") in " 
		    								+ personCache.getQuarters() + " at " + s1;
		    					else
				    				// yes, a person is on a trading mission
		    						responseText = "My designated quarters/bed is at (" + bed.getX() + ", " + bed.getY() + ") in " 
		    								+ personCache.getQuarters() + " at " + s2;
		    				}
		    			}
		    			else
		    				responseText = "My designated quarters/bed is at (" + bed.getX() + ", " + bed.getY() + ") in " 
		    						+ personCache.getQuarters();
	    	    	}
	    	    	else if (robotCache != null) {
	    	    		responseText = "I don't need one. My battery can be charged at any robotic station.";
	    	    	}
	    		} 		
	    	}
	
	    	else if (num == 6 || text.equalsIgnoreCase("inside") || text.equalsIgnoreCase("container")) {
	    		questionText = "You : are you inside?"; //what is your Container unit [Expert Mode only] ?";
	    		Unit c = cache.getContainerUnit();
	    		if (c != null)
	    			responseText = "I'm at/in " + c.getName();
	    		else
	    			responseText = "I'm not inside a building or vehicle"; //"I don't have a Container unit. ";
	    	}
	
	    	else if (num == 7 ||text.equalsIgnoreCase("outside") || text.contains("top") && text.contains("container")) {	
	    		questionText = "You : are you inside?"; //"You : what is your Top Container unit [Expert Mode only] ?";
	    		Unit tc = cache.getTopContainerUnit();
	    		if (tc != null)
	    			responseText = "I'm in " + tc.getName();
	    		else
	    			responseText = "I'm nowhere";// don't have a Top Container unit.";
	
	    	}
	
	    	else if (num == 8 || text.equalsIgnoreCase("building") ) {
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
	
    		
	    	else if (num == 9 || text.equalsIgnoreCase("settlement")) {
	    		questionText = "You : what is the settlement that you are at ?";
	    		Settlement s = cache.getSettlement();
	   			if (s != null)
	   				responseText = "My settlement is " + s.getName();
	   			else
	   				responseText = "I'm not inside a settlement";
	
	    	}
	
	    	else if (num == 10 || text.equalsIgnoreCase("associated settlement")) {
	    		questionText = "You : what is your associated settlement ?";
	    		Settlement s = cache.getAssociatedSettlement();
	    		if (s  != null) {
	    			responseText = "My associated settlement is " + s.getName();
	    		}
	    		else
	    			responseText = "I don't have an associated settlement";
	    	}

/*    		
	    	else if (num == 9 || text.equalsIgnoreCase("buried settlement")) {
	    		questionText = "You : what is his/her buried settlement ?";
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
	       		questionText = "You : what vehicle are you in and where is it ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	           		String d = cache.getVehicle().getDescription();
	           		String status = cache.getVehicle().getStatus();
	       			responseText = "My vehicle is " + v.getName() + " (a " + Conversion.capitalize(d)
	       				+ " type). It's currently " + status.toLowerCase() + ".";
	       		}
	       		else
	       			responseText = "I'm not in a vehicle.";
	       	}
    		
	    	else if (num == 12 || text.equalsIgnoreCase("vehicle inside") || text.equalsIgnoreCase("vehicle container") || text.contains("vehicle") && text.contains("container")) {
	    		questionText = "You : where is your vehicle at?";//'s container unit ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Unit c = v.getContainerUnit();
	       			if (c != null)
	       				responseText = "My vehicle is at" + c.getName();
	       			else
	       				responseText = "My vehicle is not inside";//doesn't have a container unit.";
	
	       		}
	       		else
	       			responseText = "I'm not in a vehicle.";
	       	}
	
	    	else if (num == 13 || text.equalsIgnoreCase("vehicle outside") || text.equalsIgnoreCase("vehicle top container") || text.contains("vehicle") && text.contains("top") && text.contains("container")) {
	    		questionText = "You : what is your vehicle located?";//'s top container unit ?";
	    		Vehicle v = cache.getVehicle();
	       		if (v  != null) {
	       			Unit tc = v.getTopContainerUnit();
	       			if (tc != null)
	       				responseText = "My vehicle is at " + tc.getName();
	       			else
	       				responseText = "My vehicle is not inside";//doesn't have a top container unit.";
	       		}
	       		else
	       			responseText = "I'm not in a vehicle.";
	       	}
	
	    	else if (num == 14  || text.contains("vehicle") && text.contains("park")) {
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
	
	    	else if (num == 15 || text.contains("vehicle") && text.contains("settlement")) {
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
	
	    	else if (text.contains("help") || text.equalsIgnoreCase("/h") || text.equalsIgnoreCase("h")){
		    	
        		help = true;
		    	questionText = "You : I need some help. What are the available keywords/commands ? ";
				responseText = "Keywords are 'where', 'location', 'located', 'task', 'activity', 'action', 'mission', "
						+ "'bed', 'quarters', 'building', 'inside', 'outside',"
						+ "'settlement', 'assocated settlement', 'buried settlement', "
						+ "'vehicle inside', 'vehicle outside', 'vehicle park', 'vehicle settlement', 'vehicle container', "
						+ "'vehicle top container'" + System.lineSeparator()
						+ "Specific questions from 1 to 15" + System.lineSeparator()
						+ "To quit, enter 'quit', 'exit', 'bye', '/q', 'q', '/x', or 'x'" + System.lineSeparator()
						+ "For help, enter 'help', '/h', or 'h'" + System.lineSeparator();	    	
				
		    } else {
	    		
	    		questionText = "You : you were mumbling something about....";
	    		int rand0 = RandomUtil.getRandomInt(4);
	    		if (rand0 == 0)
	        		responseText = "could you repeat that?";
	    		else if (rand0 == 1)
	           		responseText = "pardon me?";
	    		else if (rand0 == 2)
	           		responseText = "what did you say?";
	        	else if (rand0 == 3)
	        		responseText = "I beg your pardon?"; 
	        	else
	        		responseText = "Can you be more specific ?";
	    	}
	    	
    	}
   	
    	else {
    		// set personCache and robotCache to null only if you want to quit the conversation
    		questionText = "You : you were mumbling something about....";
    		int rand0 = RandomUtil.getRandomInt(4);
    		if (rand0 == 0)
        		responseText = "could you repeat that?";
    		else if (rand0 == 1)
           		responseText = "pardon me?";
    		else if (rand0 == 2)
           		responseText = "what did you say?";
        	else if (rand0 == 3)
        		responseText = "I beg your pardon?"; 
        	else
        		responseText = "Can you be more specific ?";		
    	}
    	
    	textArea.appendText(questionText + System.lineSeparator());	
    	
    	if (help)
			theOtherParty = "System";
    	else
    		theOtherParty = name;
    	
   		textArea.appendText(theOtherParty + " : " + responseText + System.lineSeparator());
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
    			//|| text.equalsIgnoreCase("h")
    			|| text.equalsIgnoreCase("help")
    			|| text.equalsIgnoreCase("/help") 
    			|| text.equalsIgnoreCase("\\help")
    			|| text.equals("\\?")
    			|| text.equals("?")
    			|| text.equals("/?")
    			) {

    		responseText = name + " : (1) type in the name of a person, bot, or settlement to connect with." + System.lineSeparator()
    				+ "(2) type in a numeral question or a keyword ['/h' or 'help' for a list of keywords]." + System.lineSeparator()
    				+ "(3) type '/q', 'quit', 'bye', '/x' or 'exit' to close the chat box" + System.lineSeparator();	
    		
    	}
    	
     	else if (text.equalsIgnoreCase("quit")
    			|| text.equalsIgnoreCase("/quit") 
    			|| text.equalsIgnoreCase("\\quit")
    			|| text.equalsIgnoreCase("\\q")
    			//|| text.equalsIgnoreCase("q")
    			|| text.equalsIgnoreCase("/q")
    			
    			|| text.equalsIgnoreCase("exit") 
    			|| text.equalsIgnoreCase("/exit") 
    			|| text.equalsIgnoreCase("\\exit")
    			|| text.equalsIgnoreCase("\\x")
    			//|| text.equalsIgnoreCase("x")
    			|| text.equalsIgnoreCase("/x")
    			
       			|| text.equalsIgnoreCase("bye")
       			|| text.equalsIgnoreCase("/bye")
      			|| text.equalsIgnoreCase("\\bye")
     			){

     		responseText = System.lineSeparator();
            closeChatBox();
            mainScene.getFlyout().dismiss();
            mainScene.ToggleMarsNetButton(false);
    		
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
    	
    	else if (len >= 2) {
    		proceed = true;
    	}


    	// Part 2 //

    	if (len == 0 || text == null || text.length() == 1) {
    		
    		int rand = RandomUtil.getRandomInt(2);
			if (rand == 0)
				responseText = name + " : I'm not sure if I understand. Can you say it again?";
			else if (rand == 1)
				responseText = name + " : I'm sorry. Would you say it again?";
			else
				responseText = name + " : I'm afraid I don't understand. Would you repeat that?";
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
							int rand = RandomUtil.getRandomInt(1);
							if (rand == 0)
								responseText = name + " : I'm sorry. " + text + " has passed away and is buried at " + person.getBuriedSettlement();
							else
								responseText = name + " : I'm sorry. " + text + " is dead and is buried at " + person.getBuriedSettlement();
						}
						else
							unit = person;
					}

					if (!robotList.isEmpty()) {
						robot = robotList.get(0);
						if (robot.getSystemCondition().isInoperable()) {
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

    	if (len > 0)
    		textArea.appendText(responseText + System.lineSeparator());
		
		return unit;
    }

    /**
     * Processes the textfield input
     */
    public void hitEnter() {
    	//System.out.println("starting hitEnter()");
    	//String text = textField.getText();  	
       	String text = autoFillTextBox.getTextbox().getText();
    	
        if (text != "" && text != null && !text.trim().isEmpty()) {
            textArea.appendText("You : " + text + System.lineSeparator());
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
            
            //textField.clear();
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

    //public TextField getTextField() {
    //	return textField;
    //}
    
    public AutoFillTextBox getAutoFillTextBox() {
    	return autoFillTextBox;
    }
    
    public Label getTitleLabel() {
    	return titleLabel;
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
        
        //textArea = null;
        //autoFillTextBox = null;
        
        if (history != null) history.clear();
        //history = null;

        //if (onMessageReceivedHandler != null) onMessageReceivedHandler.clear();
        onMessageReceivedHandler = null;
    }
    
    
}