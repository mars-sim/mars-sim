/**
 * Mars Simulation Project
 * TimeRatioMenu.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */
package org.mars.sim.console;

import java.util.List;
import java.util.function.BiConsumer;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * A menu for choosing the time ratio in TextIO.
 */
public class ChatMenu implements BiConsumer<TextIO, RunnerData> {
	   
	private static final String EXPERT_MODE = " [Expert Mode]";
	
	private volatile static boolean leaveSystem = false;
	private volatile boolean quit = false;
    private static boolean consoleEdition = false;
    
	private static String prompt;
	
	private static SwingTextTerminal terminal;
	
	private SwingHandler handler;
	
	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;
	
	public ChatMenu(boolean consoleEdition) {
		this.consoleEdition = consoleEdition;
		masterClock = sim.getMasterClock();
	}
	
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new ChatMenu(true).accept(textIO, null);
    }

    public static String determinePrompt() {
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
    	
    	return prompt;
    }
    
    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
    	terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        Party party = new Party();
        handler = new SwingHandler(textIO, "console", party);
      
        if (ChatUtils.getConnectionMode() == 1) {
        	terminal.println("Cannot establish more than one line of connections. Please type 'exit' to leave the graphic chat box first." + System.lineSeparator());			
		}
        
		else if (ChatUtils.getConnectionMode() == -1) {
			// Set to headless mode
			ChatUtils.setConnectionMode(0);

	       	terminal.println("            << Connection to MarsNet established >>"
		       	+ System.lineSeparator() 		       	
	       	);
		       		       
	        terminal.println("  ------------------------------------------------------------ ");
	        terminal.println("|      Ctrl + LEFT / RIGHT : scroll through input history      |");
	        terminal.println("|                UP / DOWN : scroll through choices            |");
	        terminal.println("|                       UP : autocomplete your input           |");
	        terminal.println("  ------------------------------------------------------------ "
	        	+ System.lineSeparator() + System.lineSeparator() 	       	
			    + "'/h' : help,  '/k' : keywords,  '/p' : pause/resume,  '/q' : quit"
			    + System.lineSeparator()		
	        ); 
     	
//	        setUpArrows();
	        
	        // Person
	        // Robot
	        // Settlement
	        // Vehicle
	        
	        setupPrompt();
		}
    }
        
        
	public void setupPrompt() {
        List<String> keywords = ChatUtils.createAutoCompleteKeywords();
//	        String[] array = names.toArray(new String[names.size()]);
	        quit = false;
 
	        while (!quit) {
	        	
	        	try {
	        		String prompt = determinePrompt();
	
			        handler.addStringTask("name", prompt, false).addChoices(keywords);//.constrainInputToChoices();
			        handler.executeOneTask();
		
//					System.out.println("ChatMenu's accept()");
				
//					// if no settlement, robot, person, or vehicle has been selected yet
//					if (ChatUtils.personCache == null && ChatUtils.robotCache == null 
//							&& ChatUtils.settlementCache == null && ChatUtils.vehicleCache == null) {	
//						// Call parse() to obtain a new value of unit
////						askSystem(Party.party);
//					} 
				
//					else {
					// Connect to a certain party
					askParty(Party.name);
					// Note : if all xxx_Cache are null, then leave
					// askParty() and go back to askSystem()
//					}
					
		        // if choosing to quit the chat mode
				if (leaveSystem && ChatUtils.isQuitting(Party.name)) {
					ChatUtils.personCache = null;
					ChatUtils.robotCache = null;
					ChatUtils.settlementCache = null;
					ChatUtils.vehicleCache = null;
					
					terminal.printf("Disconnecting MarsNet. Farewell." + System.lineSeparator() );
		        	
					quit = true;
					handler.save();
					ChatUtils.setConnectionMode(-1);
				}		
        	} catch (Exception ne) {
        		ne.printStackTrace();      		
        		quit = true;
				handler.save();
        	}
        }
    }
    

 
	/**
	 * Processes a question and return an answer regarding an unit
	 * 
	 * @param text
	 */
	public static void askParty(String text) { 
//		System.out.println("ChatMenu's askParty()");

		String questionText = "";
		String responseText = "";
		
		String[] ans;
		
		leaveSystem = false;
		
//		terminal.printf(System.lineSeparator());
		
		if (isPause(Party.name)){
			if (masterClock.isPaused()) {
				masterClock.setPaused(false, false);
				terminal.printf("The simulation is now unpaused.");
			}
			else {
				masterClock.setPaused(true, false);
				terminal.printf("The simulation is now paused.");
			}
		}

		else if (ChatUtils.isQuitting(text)) {
			ans = ChatUtils.farewell(ChatUtils.SYSTEM, false);
			questionText = ans[0];
			responseText = ans[1];
			leaveSystem = true;
			
			ChatUtils.personCache = null;
			ChatUtils.robotCache = null;
			ChatUtils.settlementCache = null;
			ChatUtils.vehicleCache = null;
		}
		
		else if (ChatUtils.checkExpertMode(text)) {
			ChatUtils.toggleExpertMode();
			responseText = System.lineSeparator() + "Set Expert Mode to " + ChatUtils.isExpertMode();
		}
		
		else if (ChatUtils.personCache == null && ChatUtils.robotCache == null 
			&& ChatUtils.settlementCache == null && ChatUtils.vehicleCache == null) {
			ans = SystemChatUtils.askSystem(text);
			questionText = ans[0];
			responseText = ans[1];
		}
		
		else {
			ans = SystemChatUtils.connectToAUnit(text);
			questionText = ans[0];
			responseText = ans[1];
		}

		if (!questionText.equals("")) {
			terminal.printf(questionText + System.lineSeparator());
		}
		
		terminal.printf(System.lineSeparator());
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

	public void restartMenu() {
		// Restart any selected objects to null
		ChatUtils.personCache = null;
		ChatUtils.robotCache = null;
		ChatUtils.settlementCache = null;
		ChatUtils.vehicleCache = null;
		
		// Dispose the old terminal
		InteractiveTerm.disposeTerminal();
		// Restart the MarsTerminal, SwingHandler, TextIO, InteractiveTerm
		new InteractiveTerm(consoleEdition, true);
		
//		InteractiveTerm.setKeepRunning(false);
//		InteractiveTerm.disposeTerminal();
//		InteractiveTerm.delay(500L);
//		
//		InteractiveTerm.restartTerm();
//		InteractiveTerm.setUpRunningLoop();
		
//		quit = true;
//		leaveSystem = true;
//		handler.save();
		
//		ChatUtils.setConnectionMode(-1);
		
//		prompt = "/q";
	
//		return;
//		prompt = "Enter your choice:";
//		terminal.println();
//		handler.executeOneTask();
		
		// Restart the prompt
//		setupPrompt();
//		quit = false;
//		leaveSystem = false;
		

	}
	
    @Override
    public String toString() {
        return "Connect with MarsNet";
    }
    
    public static class Party {
        public static String name;
        public static boolean isSettlement;
        public static boolean isPerson;
        public static boolean isRobot;
        public static boolean isVehicle;
        public static boolean isSystem;
        
        @Override
        public String toString() {
            return System.lineSeparator() +">" + name;
        }
    }
}
