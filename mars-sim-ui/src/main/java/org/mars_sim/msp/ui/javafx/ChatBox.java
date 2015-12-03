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

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.*;
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
        textField.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
        	keyHandler(keyEvent);
        });

        setTop(titleLabel);
        setCenter(textArea);
        setBottom(textField);

    }

    public String checkGreetings(String text) {
    	String result = null;	
    	//
    	return result; 	
    }
    
    /*
     * Parses the text and interprets the contents
     * @param text
     */
    //2015-12-01 Added parseText()
    public void parseText(String text) {   
    	String responseText = null;
    	String name = "System";
    	boolean available = true;
    	int nameCase = 0;
    	boolean proceed = true;
    	//System.out.println("A: text is " + text + ". Running parseText()");
    	text = text.trim();
    	int len = text.length();

    	if (len >= 2 && text.substring(0,2).equalsIgnoreCase("hi")) {
  
    		if (len > 2) {
    			text = text.substring(2, len);
    		   	text = text.trim();
    		   	proceed = true;
    		}
    		else {
    			text = null;
    	   		proceed = false;
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
    			text = null;
    	   		proceed = false;
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
    			text = null;
    	   		proceed = false;
     	   		responseText = name + " : Hey Earth Control, how can I help?";
    		}
    	}
    	
    	if (text == null) {
    		//nothing
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

			if (nameCase >= 2) {
	    		name = "System";
				responseText = name + " : there are more than one \"" + text + "\". Would you be more specific?";
	        	//System.out.println(responseText);		
						
			} else if (nameCase == 1) {
				if (!available){
		    		name = "System";
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
				
			} else if (nameCase == 0) {
	    		responseText = name + " : I do not recognize anyone by the name of \"" + text + "\". Would you be more specific?";
				
			} else if (nameCase == -1) {   
	    		responseText = name + " : I'm sorry. " + text + " has passed away.";
			}
			
    	}
    	
		textArea.appendText(responseText + System.lineSeparator());
	
    }
    
    public void keyHandler(final KeyEvent keyEvent){
        switch (keyEvent.getCode()) {
	        case ENTER:
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