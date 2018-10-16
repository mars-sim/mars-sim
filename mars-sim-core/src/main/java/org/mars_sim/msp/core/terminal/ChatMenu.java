/**
 * Mars Simulation Project
 * TimeRatioMenu.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.terminal;

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
     	
//	        setUpArrows();
	        
	        // Person
	        // Robot
	        // Settlement
	        // Vehicle
	     
	        List<String> names = CollectionUtils.createAutoCompleteData();//.createSettlerNames();
//	        String[] array = names.toArray(new String[names.size()]);
	        boolean quit = false;
	          
	        while (!quit) {
	        	String prompt = "MarsNet System >";

	        	if (ChatUtils.personCache != null)
	        		prompt = "Connected with " + ChatUtils.personCache.toString() +" >";
	        	else if (ChatUtils.robotCache != null) 
	        		prompt = "Connected with " + ChatUtils.robotCache.toString() +" >";
	        	else if (ChatUtils.settlementCache != null)
	        		prompt = "Connected with " + ChatUtils.settlementCache.toString() +" >";	
	        	else if (ChatUtils.vehicleCache != null)
	        		prompt = "Connected with " + ChatUtils.vehicleCache.toString() +" >";	

		        handler.addStringTask("party", prompt, false).addChoices(names);//.constrainInputToChoices();
		        handler.executeOneTask();
		        		        
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

		String responseText = "";
		String questionText = "";

		text = text.trim();

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
			responseText = ChatUtils.askSystem(text);
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
