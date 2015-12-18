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
import org.mars_sim.msp.core.structure.Settlement;
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
    protected final TextArea textArea = new TextArea();
    protected final TextField textField = new TextField();

    protected final List<String> history = new ArrayList<>();
    protected int historyPointer = 0;

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
    
    
    /*
     * Parses the text and interprets the contents in the chat box
     * @param input text
     */
    //2015-12-01 Added parseText()
    public void parseText(String text) {   
    	String responseText = null;
    	String name = "System";
    	boolean available = true;
    	int nameCase = 0;
    	boolean proceed = false;
    	//System.out.println("A: text is " + text + ". Running parseText()");
    	text = text.trim();
    	int len = text.length();

    	// Part 1 //
    	
    	// 2015-12-17 detect "\" backslash and the name that follows
    	if (len >= 3 && text.substring(0,1).equalsIgnoreCase("\\")) { 
    		
    		text = text.substring(1, len).trim();
    		proceed = true;
    		//responseText = name + " : what would you like to know about ";
    		
    	}
    	
    	else if (len >= 2 && text.substring(0,2).equalsIgnoreCase("hi")) {
  
    		if (len > 2) {
    			text = text.substring(2, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
    			//text = null;
    	   		//proceed = false;
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
    			//text = null;
    	   		//proceed = false;
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
    			//text = null;
    	   		//proceed = false;
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
				if (text.contains("bot") || text.contains("Bot"))
		    		// Check if it is a bot
					nameCase += settlement.hasRobotName(text);
				else 
					nameCase += settlement.hasPersonName(text);

	    		// TODO: check if the person is available or not (e.g. if on a mission and comm broke down)
				//System.out.println("nameCase is " + nameCase);
			}

			//System.out.println("total nameCase is " + nameCase);

			// capitalize the first initial of a name
    		text = Conversion.capitalize(text);
    		
    		// Case 1: more than one person with that name
			if (nameCase >= 2) {
	    		//name = "System";

				responseText = name + " : there are more than one \"" + text + "\". Would you be more specific?";
	        	//System.out.println(responseText);		
						
			// Case 2: there is one person 
			} else if (nameCase == 1) {
				if (!available){
		    		//name = "System";
	    			responseText = name + " : I'm sorry. " + text + " is unavailable at this moment";
	    			
	    		} else {		
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
		    		if (rand1 > 0 && rand0 != 4)
		    			responseText += " Over."; 
		    		
		    		name = text;
		    		responseText = name + " : This is " + text + ". " + responseText;
		        	//System.out.println(responseText);
	    		}
				
		    // Case 4: passed away
			} else if (nameCase == -1) {   
		    	responseText = name + " : I'm sorry. " + text + " has passed away.";
			
				
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
							//name = "System";
							responseText = name + " : do you mean \"" + s_name + "\" ?";
							//System.out.println("partially matching settlement name " + s_name);
							break;
						}
						
					}
		    	}
		    	else		    		
		    		responseText = name + " : I do not recognize anyone or settlements by \"" + text + "\"." ;
		    	
			}
			
			else
				responseText = name + " : I do not recognize anyone or settlements by \"" + text + "\"." ;
				
    	}
    	
		textArea.appendText(responseText + System.lineSeparator());
	
    }
    
    /**
     * Processes the textfield input
     */
    public void hitEnter() {
    	String text = textField.getText();
        if (text != "" && text != null && !text.trim().isEmpty()) {	
            textArea.appendText("You : " + text + System.lineSeparator());
        	parseText(text);
        	
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
                System.out.println("historyPointer is " + historyPointer);
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
                System.out.println("historyPointer is " + historyPointer);
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