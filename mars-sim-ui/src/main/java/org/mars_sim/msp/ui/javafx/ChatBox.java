/* Mars Simulation Project
 * ChatBox.java
 * @version 3.08 2015-12-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationState;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import javafx.application.Application;
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
    protected Vehicle vehicle;
    protected Settlement settlement;
    protected Building building;
    protected Equipment equipment;

    protected final TextArea textArea = new TextArea();
    protected final TextField textField = new TextField();

    protected final List<String> history = new ArrayList<>();

    private Consumer<String> onMessageReceivedHandler;

    public ChatBox() {
        Label titleLabel = new Label("MarsNet Chat Box");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font: bold 12pt 'Corbel'; -fx-effect: dropshadow( one-pass-box , black , 8 , 0.0 , 2 , 0 );");

        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font: 11pt 'Corbel';");

        textField.setStyle("-fx-font: 11pt 'Corbel';");
        textField.setPrefWidth(500);
        textField.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
        	keyHandler(keyEvent);
        });

        //Button button = new Button("Submit");
        MaterialDesignButton button = new MaterialDesignButton("Broadcast");
        //button.setAlignment(Pos.CENTER_RIGHT);
        button.setTooltip(new Tooltip ("Click to broadcast"));
        button.setStyle("-fx-font: bold 10pt 'Corbel';");
        button.setOnAction(e -> {
        	hitEnter();
        });

        HBox hbox = new HBox();
        hbox.setSpacing(2);
        textField.setPadding(new Insets(5, 5, 5, 5));
        hbox.getChildren().addAll(button,textField);

        setTop(titleLabel);
        setCenter(textArea);

        setBottom(hbox);

    }

    public String checkGreetings(String text) {
    	String result = null;
    	//
    	return result;
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
    	//Robot robot;
    	//Person person;
    	if (personCache != null) {
    		cache = (Person) personCache;
    		name = cache.getName();
    	}
    	else if (robotCache != null) {
    		cache = (Robot) robotCache;
    		name = cache.getName();
    	}

    	int num = Integer.parseUnsignedInt(text, 10);

    	if (num == 0) {
    		// exit
    		personCache = null;
	    	robotCache = null;
    		sys = name;
    		questionText = "You : exit";
    		responseText = "Bye. " + System.lineSeparator() + " System : " + sys + " had left the conversation.";
	    }

    	else if (num == 1) {
    		questionText = "You : what is your LocationState ?";
    		LocationState state = cache.getLocationState();
    		if (state != null)
    			responseText = "My current LocationState is " + state.getName();
    		else
    			responseText = "My current LocationState is " + state;
    	}

       	else if (num == 2) {
    		questionText = "You : reserved";
    		responseText = "reserved";
       	}

    	else if (num == 3) {
    		questionText = "You : reserved";
    		responseText = "reserved";
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
	    		Building b = s.getBuildingManager().getBuilding(cache);
	    		Building b1 = cache.getBuildingLocation();
	    		if (b != null && b1 != null)
	    			responseText = "The building I'm in is " + b.getNickName()
	    				+ "  (aka " + b1.getNickName() + ").";
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
    	String text = textField.getText();
        if (text != "" && text != null && !text.trim().isEmpty()) {
            textArea.appendText("You : " + text + System.lineSeparator());
            Unit unit = null;
            if (personCache == null && robotCache == null)
            	unit = parseText(text);
            else {
            	askQuestion(text);
            }

            if (unit != null)
            	if (unit instanceof Person)
            		personCache = (Person) unit;
            	else if (unit instanceof Robot)
            		robotCache = (Robot) unit;


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
            textField.clear();
        }
    }

    /**
     * Processes the textfield in the chat box and saves a history of input
     * @param keyEvent
     */
    public void keyHandler(final KeyEvent keyEvent){
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
	            	textField.setText(history.get(historyPointer));
	                textField.selectAll();
	            });

	            break;

	        case DOWN:
	            if (historyPointer >= history.size() - 1) {
	                break;
	            }

                historyPointer++;
                //System.out.println("historyPointer is " + historyPointer);
	            ChatUtil.runSafe(() -> {
	           		textField.setText(history.get(historyPointer));
	                textField.selectAll();
	            });

	            break;

	        default:
	            break;
	    }

    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        textField.requestFocus();
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

    public TextField getTextField() {
    	return textField;
    }
}