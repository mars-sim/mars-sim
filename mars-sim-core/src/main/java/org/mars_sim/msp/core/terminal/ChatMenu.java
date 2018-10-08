/**
 * Mars Simulation Project
 * TimeRatioMenu.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.terminal;

import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.terminal.AppUtil;
import org.mars_sim.msp.core.terminal.RunnerData;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A menu for choosing the time ratio in TextIO.
 */
public class ChatMenu implements BiConsumer<TextIO, RunnerData> {
	
//	private static final String KEY_STROKE_UP = "pressed UP";
//	private static final String KEY_STROKE_DOWN = "pressed DOWN";
//
//	private String originalInput = "";
//	private int choiceIndex = -1;
//	private String[] choices = {};
	    
	private SwingTextTerminal terminal;
	
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new ChatMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
    	terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        Party party = new Party();
        SwingHandler handler = new SwingHandler(textIO, party);
      
        if (ChatUtils.getConnectionMode() == 1) {
        	terminal.println("Cannot establish more than one line of connections. Please disactivate the graphic chat box first." + System.lineSeparator());			
		}
        
		else if (ChatUtils.getConnectionMode() == -1) {
			// Set to headless mode
			ChatUtils.setConnectionMode(0);

	       	terminal.println("<< Connection to MarsNet established >>"
		       	+ System.lineSeparator() + System.lineSeparator() 
		       	+ "Type '/h' for help,   '/k' for keywords,   '/q' to quit"
		       	+ System.lineSeparator() + System.lineSeparator());
		       		       
	        terminal.println(" -------------------------------------------------------------- ");
	        terminal.println("|     Press UP arrow key to autocomplete the keyword.          |");
	        terminal.println("|     Press UP/DOWN arrow keys to scroll through choices.      |");
	        terminal.println(" -------------------------------------------------------------- "
	        		+ System.lineSeparator()); 
		        
//		       	+ "Press UP/DOWN to show a list of possible values"
//		       	+ System.lineSeparator());
	       	
//	        setUpArrows();
	        
	        // Person
	        // Robot
	        // Settlement
	        // Vehicle
	     
	        List<String> names = CollectionUtils.createAutoCompleteData();//.createSettlerNames();
//	        String[] array = names.toArray(new String[names.size()]);
	        boolean quit = false;
	          
	        while (!quit) {
	        	String prompt = "Which party do you want to connect with >";

	        	if (ChatUtils.personCache != null)
	        		prompt = "Connected with " + ChatUtils.personCache.toString() +" >";
	        	else if (ChatUtils.robotCache != null) 
	        		prompt = "Connected with " + ChatUtils.robotCache.toString() +" >";
	        	else if (ChatUtils.settlementCache != null)
	        		prompt = "Connected with " + ChatUtils.settlementCache.toString() +" >";	
	        	else if (ChatUtils.vehicleCache != null)
	        		prompt = "Connected with " + ChatUtils.vehicleCache.toString() +" >";	
	        	
//	        	System.out.println("prompt : " + prompt);
//		        setChoices(array);
		        
//		        Party.party = textIO.newStringInputReader()
//		//        		.withInlinePossibleValues(array)
//		                .read(">");//What party do you want to reach");
		
		//        terminal.printf(System.lineSeparator());
		    
		        handler.addStringTask("party", prompt, false).addChoices(names);//.constrainInputToChoices();
		        handler.executeOneTask();
		        
//		        System.out.println("personCache : " + ChatUtils.personCache);
//		        System.out.println("robotCache : " + ChatUtils.robotCache);
//		        System.out.println("settlementCache : " + ChatUtils.settlementCache);
//		        System.out.println("vehicleCache : " + ChatUtils.vehicleCache);
		        
				// if no settlement, robot, person, or vehicle has been selected yet
				if (ChatUtils.personCache == null && ChatUtils.robotCache == null 
						&& ChatUtils.settlementCache == null && ChatUtils.vehicleCache == null) {	
					// Call parse() to obtain a new value of unit
					parse(Party.party);
				} 
				
				else {
					// Call ask() to further engage the conversion
					ask(Party.party);
					// Note : if all _Cache are null, then leave
					// ask() and go back to parse()
				}
				
		        
				if (ChatUtils.isQuitting(Party.party)) {
					quit = true;
					ChatUtils.setConnectionMode(-1);
				}				
	        }
        }
    }
    
    /*
	 * Parses the text and interprets the contents in the chat
	 * 
	 * @param input text
	 */
    public void parse(String text) {
//		System.out.println("parse() in ChatMenu");
		String responseText = "";
		String questionText = "";

		// System.out.println("A: text is " + text + ". Running parse()");
		text = text.trim();
//		int len = text.length();

		if (ChatUtils.isQuitting(text)) {
			String[] txt = ChatUtils.farewell(ChatUtils.SYSTEM, false);
			questionText = txt[0];
			responseText = txt[1];

			terminal.printf(System.lineSeparator());
			
		}
		
		else {
			terminal.printf(System.lineSeparator());
	        //ChatUtils.setConnectionMode(0);
			// Call ChatUtils' parseText	
			responseText = ChatUtils.parseText(text);
		}
		
		// print question
		if (!questionText.equals(""))
			terminal.printf(questionText + System.lineSeparator());
		
		// print response
        terminal.printf(responseText + System.lineSeparator());
        
		terminal.printf(System.lineSeparator());
    }
 
	/**
	 * Processes a question and return an answer regarding an unit
	 * 
	 * @param text
	 */
	public void ask(String text) { 
//		System.out.println("ask() in ChatMenu");
		String questionText = null;
		String responseText = "";
				
        //ChatUtils.setConnectionMode(0);
		String[] ss = ChatUtils.askQuestion(text);
		
		// Obtain responses
		questionText = ss[0];
		responseText = ss[1];

		terminal.printf(System.lineSeparator());
		
		// print question
		if (!questionText.equals(""))
			terminal.printf(questionText + System.lineSeparator());

		// print response
		terminal.printf(responseText + System.lineSeparator());
		
		terminal.printf(System.lineSeparator());
	}
	

//    public void setChoices(String... choices) {
//        this.originalInput = "";
//        this.choiceIndex = -1;
//        this.choices = choices;
//    }
//    
//    public void setUpArrows() {
//        terminal.registerHandler(KEY_STROKE_UP, t -> {
//            if(choiceIndex < 0) {
//                originalInput = terminal.getPartialInput();
//            }
//            if(choiceIndex < choices.length - 1) {
//                choiceIndex++;
//                t.replaceInput(choices[choiceIndex], false);
//            }
//            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
//        });
//
//        terminal.registerHandler(KEY_STROKE_DOWN, t -> {
//            if(choiceIndex >= 0) {
//                choiceIndex--;
//                String text = (choiceIndex < 0) ? originalInput : choices[choiceIndex];
//                t.replaceInput(text, false);
//            }
//            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
//        });
//    }
    	
    
    @Override
    public String toString() {
        return "Chat with a party";
    }
    
    private static class Party {
        public static String party;

        @Override
        public String toString() {
            return System.lineSeparator() +">" + party;
        }
    }
}
