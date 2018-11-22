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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.terminal.AppUtil;
import org.mars_sim.msp.core.terminal.RunnerData;
import org.mars_sim.msp.core.time.MasterClock;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A menu for choosing the time ratio in TextIO.
 */
public class ChatMenu implements BiConsumer<TextIO, RunnerData> {
	   
	private static final String EXPERT_MODE = " [Expert Mode]";
	
	private boolean leaveSystem = false;
	private boolean quit = false;
	private String prompt;
	
	private SwingTextTerminal terminal;
	private SwingHandler handler;
	
	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;
	
	public ChatMenu() {
		masterClock = sim.getMasterClock();
	}
	
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
        handler = new SwingHandler(textIO, party);
      
        if (ChatUtils.getConnectionMode() == 1) {
        	terminal.println("Cannot establish more than one line of connections. Please type 'exit' to leave the graphic chat box first." + System.lineSeparator());			
		}
        
		else if (ChatUtils.getConnectionMode() == -1) {
			// Set to headless mode
			ChatUtils.setConnectionMode(0);

	       	terminal.println("            << Connection to MarsNet established >>"
		       	+ System.lineSeparator() 		       	
	       	);
		       		       
	        terminal.println(" -------------------------------------------------------------- ");
	        terminal.println("|     Press UP arrow key to autocomplete with a keyword.       |");
	        terminal.println("|     Press UP/DOWN arrow keys to scroll through choices.      |");
	        terminal.println(" -------------------------------------------------------------- "
	        	+ System.lineSeparator() + System.lineSeparator() 	       	
			    + "'/h' : help,  '/k' : keywords,  '/p' : pause/unpause,  '/q' : quit"
			    + System.lineSeparator()		
	        ); 
     	
//	        setUpArrows();
	        
	        // Person
	        // Robot
	        // Settlement
	        // Vehicle
	     
	        List<String> names = CollectionUtils.createAutoCompleteData();
//	        String[] array = names.toArray(new String[names.size()]);
	        quit = false;
 
	        while (!quit) {
	        	
		        String expertString = "";
		        // See if user opts in the expert mode
	        	if (ChatUtils.isExpertMode()) {
	        		expertString = EXPERT_MODE;
	        	}
	        	
	        	prompt = "Connected with MarsNet" + expertString + " >";
	        	
	        	if (ChatUtils.personCache != null)
	        		prompt = "Connected with " + ChatUtils.personCache.toString() + expertString + " >";
	        	else if (ChatUtils.robotCache != null) 
	        		prompt = "Connected with " + ChatUtils.robotCache.toString() + expertString + " >";
	        	else if (ChatUtils.settlementCache != null)
	        		prompt = "Connected with " + ChatUtils.settlementCache.toString() + expertString + " >";	
	        	else if (ChatUtils.vehicleCache != null)
	        		prompt = "Connected with " + ChatUtils.vehicleCache.toString() + expertString + " >";	

		        handler.addStringTask("party", prompt, false).addChoices(names);//.constrainInputToChoices();
		        handler.executeOneTask();
		        		        
				// if no settlement, robot, person, or vehicle has been selected yet
				if (ChatUtils.personCache == null && ChatUtils.robotCache == null 
						&& ChatUtils.settlementCache == null && ChatUtils.vehicleCache == null) {	
					// Call parse() to obtain a new value of unit
					askSystem(Party.party);
				} 
				
				else {
					// Connect to a certain party
					askParty(Party.party);
					// Note : if all xxx_Cache are null, then leave
					// askParty() and go back to askSystem()
				}
				
		        // if choosing to quit the chat mode
				if (leaveSystem && ChatUtils.isQuitting(Party.party)) {
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
    public void askSystem(String text) {

		String responseText = "";
		String questionText = "";

		text = text.trim();

		if (isPause(Party.party)){
			if (masterClock.isPaused()) {
				masterClock.setPaused(false, false);
				terminal.printf(System.lineSeparator());
				terminal.printf("The simulation is now unpaused.");
			}
			else {
				masterClock.setPaused(true, false);
				terminal.printf(System.lineSeparator());
				terminal.printf("The simulation is now paused.");
			}
		}

		else if (ChatUtils.isQuitting(text)) {
			String[] txt = ChatUtils.farewell(ChatUtils.SYSTEM, false);
			questionText = txt[0];
			responseText = txt[1];
			leaveSystem = true;
			terminal.printf(System.lineSeparator());
			
		}
		
		else if (ChatUtils.checkExpertMode(text)) {
			ChatUtils.toggleExpertMode();
			responseText = System.lineSeparator() + "Set Expert Mode to " + ChatUtils.isExpertMode();
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
	public void askParty(String text) { 
		String questionText = null;
		String responseText = "";
		leaveSystem = false;
		
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
	
	/*
	 * Checks if the user wants to pause the simulation
	 * 
	 * @param text
	 */
	public static boolean isPause(String text) {
		if (text.equalsIgnoreCase("pause") || text.equalsIgnoreCase("/p") ) {
			return true;
		}

		else
			return false;
	}

	public void executeQuit() {
		quit = true;
		leaveSystem = true;
		ChatUtils.setConnectionMode(-1);
//		prompt = "/q";
		ChatUtils.personCache = null;
		ChatUtils.robotCache = null;
		ChatUtils.settlementCache = null;
		ChatUtils.vehicleCache = null;
//		return;
//		prompt = "Enter your choice:";
//		terminal.println();
//		handler.executeOneTask();
	}
	
    @Override
    public String toString() {
        return "Connect with MarsNet";
    }
    
    private static class Party {
        public static String party;

        @Override
        public String toString() {
            return System.lineSeparator() +">" + party;
        }
    }
}
