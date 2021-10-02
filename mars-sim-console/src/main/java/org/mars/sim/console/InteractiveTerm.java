/*
 * Mars Simulation Project
 * InteractiveTerm.java
 * @date 2021-10-02
 * @author Manny Kung
 */

package org.mars.sim.console;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.beryx.textio.TextIO;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.TextIOChannel;
import org.mars.sim.console.chat.UserChannel;
import org.mars.sim.console.chat.simcommand.TopLevel;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Simulation;

/**
 * The InteractiveTerm class builds a text-based console interface and handles the interaction with players
 */
public class InteractiveTerm {

	private static final Logger logger = Logger.getLogger(InteractiveTerm.class.getName());
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	// Screen sizes presented to user
	private final static Dimension[] screenSizes = {
							new Dimension(1920, 1080),
							new Dimension(1280, 800),
							new Dimension(1280, 1024),
							new Dimension(1600, 900),
							new Dimension(1366, 768)
						};
	
    private static boolean consoleEdition = false;
    
    private static boolean useCrew = true;
	
	private static MarsTerminal marsTerminal;

	private static CommanderProfile profile;
	
	private static TextIO textIO;
	
	private static Simulation sim = Simulation.instance();
	
	private static SwingHandler handler;
	
	private static GameManager gm;
	private static int selectedScreen = -1;
	
	public InteractiveTerm(boolean restart) {
		
		marsTerminal = new MarsTerminal();
        marsTerminal.init();
		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
		marsTerminal.registerUserInterruptHandler(term -> {}, false);
	
		logger.config("Done with MarsTerminal on " + Thread.currentThread().getName());
		
        textIO = new TextIO(marsTerminal);
        
		gm = new GameManager();
		
        handler = new SwingHandler(textIO, gm);
        
       
        if (restart) {
    		loadTerminalMenu();
        }
        
		logger.config("Done with InteractiveTerm's constructor is on " + Thread.currentThread().getName());
	}
	
	/**
	 * Asks players what to choose in beryx console main menu.
	 */
	public int startConsoleMainMenu() {
		logger.config("Staring startConsoleMainMenu()");
		
		profile = new CommanderProfile(this);
        		
		return selectMenu();
	}
	
	/**
	 * Selects the game mode
	 * 
	 * @return
	 */
	private int selectMenu() {
		int menuChoice = 0;
	
		try {
		
			marsTerminal.print(System.lineSeparator() 
					+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------\n"
					+ System.lineSeparator()
					+ "                          * * *   Main Menu   * * *\n"
	//				+ "                                   r" + Simulation.BUILD +"\n");
					+ System.lineSeparator()
					+ System.lineSeparator()
					+ "0. Exit "
					+ System.lineSeparator()
					+ "1. Start a new Sim"
					+ System.lineSeparator()
					+ "2. Load a saved Sim"
					+ System.lineSeparator()
					+ "3. Change screen resolution"
					+ System.lineSeparator()
					+ System.lineSeparator()
					);
				
	        handler.addStringTask("menu", "Choose an option :", false).addChoices("0", "1", "2", "3").constrainInputToChoices();
	        handler.executeOneTask();
	
	        if (GameManager.menu.equals("0")) {
	        	sim.endSimulation(); 
	
				System.exit(0);
	    		disposeTerminal();
	    		return menuChoice;
	        }
	        
	        else if (GameManager.menu.equals("1")) {
	        	marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Starting a new sim...");
	        	marsTerminal.print(System.lineSeparator());
	        	// Go to selectMode() to choose a mode
	        	menuChoice = selectMode();
	        	
	    		marsTerminal.print(System.lineSeparator());
	    		
	            return menuChoice;
	        }
	        
	        else if (GameManager.menu.equals("2")) {
	        	marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Loading from a saved sim...");
	        	marsTerminal.print(System.lineSeparator());
	        	
	        	menuChoice = 2;
	        	
	    		marsTerminal.print(System.lineSeparator());
	    		
	            return menuChoice;
	        }
	        
	        else if (GameManager.menu.equals("3")) {
	        	StringBuilder buffer = new StringBuilder();
	        	for(int i = 0; i < screenSizes.length; i++) {
	        		buffer.append(i).append(". ")
	        		.append(screenSizes[i].width)
	        		.append(" x ")
	        		.append(screenSizes[i].height)
	        		.append(System.lineSeparator());
	        	}
	        	
	        	marsTerminal.print(System.lineSeparator() 
	    				+ System.lineSeparator()
	    				+ "                        * * *   Resolution Menu   * * *\n"
	    				+ System.lineSeparator()
	    				+ System.lineSeparator()
	    				+ buffer.toString()
	    	        	+ System.lineSeparator()
	        			);
	        	        			
	        	String oldRes = "Screen size";
	        	if (selectedScreen >= 0) {
	        		oldRes = screenSizes[selectedScreen].width + " x " + screenSizes[selectedScreen].height;
	        	}
	        	
	        	marsTerminal.print("Current resolution : " + oldRes);
	        	marsTerminal.print(System.lineSeparator());
	        	marsTerminal.print(System.lineSeparator());
	        	
	        	handler.addStringTask("resolution", "Choose an option :", false).addChoices("0", "1", "2", "3", "4").constrainInputToChoices();
	            handler.executeOneTask();
	
	            String userInput = GameManager.resolution;
	            selectedScreen = Integer.parseInt(userInput);
	            
	    		String newRes = screenSizes[selectedScreen].width + " x " + screenSizes[selectedScreen].height;
	                      
	        	marsTerminal.print(System.lineSeparator());
				marsTerminal.print("The screen resolution has been changed from '" + oldRes + "' to '" + newRes + "'");
	        	marsTerminal.print(System.lineSeparator());
	        	
	        	return selectMenu();
	        }
	        
	        else {
	        	
	        	return selectMenu();
	        }        
		
		} catch(RuntimeException e) {
			logger.severe(sourceName + ": RuntimeException detected.");
		}
		
		return menuChoice;
	}
	
	/**
	 * Selects the game mode
	 * 
	 * @return
	 */
	private int selectMode() {
		int modeChoice = 0;
		
		marsTerminal.print(System.lineSeparator() 
				+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------\n"
				+ System.lineSeparator()
				+ "                        * * *   Mode Selection   * * *\n"
//				+ "                                   r" + Simulation.BUILD +"\n");
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "0. Exit"
				+ System.lineSeparator()
				+ "1. Command Mode  (Experimental only)"
				+ System.lineSeparator()
				+ "2. Sandbox Mode"
				+ System.lineSeparator()
				+ System.lineSeparator()
				);
			
        handler.addStringTask("input", "Select the Game Mode:", false).addChoices("0", "1", "2").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.input.equals("0")) {
        	sim.endSimulation(); 

			System.exit(0);
    		disposeTerminal();
        }
        else if (GameManager.input.equals("1")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Go to Command Mode...");
        	marsTerminal.print(System.lineSeparator());
        	
        	modeChoice = configureCommandMode();
        }
        
        else if (GameManager.input.equals("2")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Go to Sandbox Mode...");
        	marsTerminal.print(System.lineSeparator());
        	
        	modeChoice = configureSandoxMode();
        }
        
		marsTerminal.print(System.lineSeparator());
		
        return modeChoice;
	}
	
	/**
	 * Selects the simulation configuration editor 
	 * 
	 * @return
	 */
	private int selectSite() {
		int siteChoice = 0;
		marsTerminal.println(System.lineSeparator());
		
	    marsTerminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "           * * *               Scenario Selection               * * *" 
         		+ System.lineSeparator()
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Exit"
        		+ System.lineSeparator()
				+ "1. Open Scenario editor"
				+ System.lineSeparator()
				+ "2. Proceed to start the sim"
				+ System.lineSeparator()
				+ "3. Back to previous menu"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "NOTE: the Console Editon does not come with the Scenario Editor."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("site", "Enter your choice:", false).addChoices("0", "1", "2", "3").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.site.equals("0")) {
        	sim.endSimulation(); 

			System.exit(0);
    		disposeTerminal();
        }
        
        else if ((GameManager.site).equals("1")) {
        	if (consoleEdition) {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Sorry. The Console Edition of mars-sim does not come with the Scenario Editor.");	
				marsTerminal.print(System.lineSeparator());
				
				siteChoice = 0;
        	}
        	else {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Loading the Scenario Editor...");
//				marsTerminal.print(System.lineSeparator());
				
				siteChoice = 1;
        	}
        }
        
        else if ((GameManager.site).equals("2")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Starting a new simulation with default site selection...");
        	marsTerminal.print(System.lineSeparator());
        	
			siteChoice = 0;
        }
        
        else if ((GameManager.site).equals("3")) {
			marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Back to the previous menu.");
        	marsTerminal.print(System.lineSeparator());
        	
			return selectMode();
        }
        
		marsTerminal.print(System.lineSeparator());
		
        return siteChoice;
	}
	
	
	/**
	 * Configures the command mode
	 * 
	 * @return
	 */
	private int configureCommandMode() {
		int commandCfg = 0;
		
		// Set the Game Mode to Command Mode in GameManager
		GameManager.mode = GameMode.COMMAND;
		
        marsTerminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "            * * *   Command Mode (Experimental only) - Crew Selection   * * *" 
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Exit"
				+ System.lineSeparator()
				+ "1. Enable/disable loading of crews (currently " + (useCrew ?  "Enabled" : "Disabled")  + ")"
				+ System.lineSeparator()
				+ "2. Set up commander profile"
				+ System.lineSeparator()
				+ "3. Load commander profile"
				+ System.lineSeparator()
				+ "4. Back to previous menu"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Note 1: By default the loading of crews is Enabled."
				+ System.lineSeparator()
//				+ "Note 2: Console Editon does NOT have the Site Editor."
//				+ System.lineSeparator()
				);
		
        handler.addStringTask("commandCfg", "Enter your choice:", false).addChoices("0", "1", "2", "3", "4").constrainInputToChoices();
        handler.executeOneTask();
        
        if (GameManager.commandCfg.equals("0")) {
        	sim.endSimulation(); 

			System.exit(0);
    		disposeTerminal();
        }
        
        else if ((GameManager.commandCfg).equals("1")) {
			marsTerminal.print(System.lineSeparator());
			if (useCrew) {			
				useCrew = false;
				marsTerminal.print("The crew loading has been disabled.");
	        	marsTerminal.print(System.lineSeparator());
			}
			else {
				useCrew = true;
				marsTerminal.print("The crew loading has been enabled.");
	        	marsTerminal.print(System.lineSeparator());
			}
	    	
			commandCfg = configureCommandMode();
    	}
    	
    	else if ((GameManager.commandCfg).equals("2")) {
			marsTerminal.print(System.lineSeparator());
			// Set new profile
			profile.accept(textIO, null);
			
			commandCfg = selectSite();
    	}
    	
    	else if ((GameManager.commandCfg).equals("3")) {
    		// Load from previously saved profile
    		loadPreviousProfile();
    		
    		commandCfg = selectSite();
    	}
        
    	else if ((GameManager.commandCfg).equals("4")) {
			marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Back to the previous menu.");
        	marsTerminal.print(System.lineSeparator());
        	
			return selectMode();
        }
        
        else {
        	commandCfg = configureCommandMode();
        }
        
		marsTerminal.print(System.lineSeparator());
		
        return commandCfg;
	}
 
	
	/**
	 * Configures the sandbox mode
	 * 
	 * @return
	 */
	private int configureSandoxMode() {
		int sandboxCfg = 0;
		
		GameManager.mode = GameMode.SANDBOX;

        marsTerminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "           * * *  Sandbox Mode - Crew and Scenario Selection  * * *" 
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Exit"
        		+ System.lineSeparator()
				+ "1. Open Scenario editor"
				+ System.lineSeparator()
				+ "2. Enable/disable crew loading (currently " + (useCrew ? "Enabled" : "Disabled")  + ")"
				+ System.lineSeparator()
				+ "3. Proceed to start the sim"
				+ System.lineSeparator()
				+ "4. Back to previous menu"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Note 1: By default the crews are loaded. Choose 2 to enable/disable."
				+ System.lineSeparator()
				+ "Note 2: The Console Editon does not come with the Scenario Editor."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("sandboxCfg", "Enter your choice:", false)
        	.addChoices("0", "1", "2", "3", "4").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.sandboxCfg.equals("0")) {
        	sim.endSimulation(); 

			System.exit(0);
    		disposeTerminal();
        }
    	
    	else if ((GameManager.sandboxCfg).equals("1")) {
        	if (consoleEdition) {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Sorry. The Console Edition of mars-sim does not come with the Site Editor.");	
				marsTerminal.println(System.lineSeparator());
				
				sandboxCfg = configureSandoxMode();
        	}
        	else {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Loading the Scenario Editor...");
//				marsTerminal.println(System.lineSeparator());
				
				sandboxCfg = 1;
        	}
        }
        
        else if ((GameManager.sandboxCfg).equals("2")) {
			marsTerminal.print(System.lineSeparator());
			if (useCrew) {			
				useCrew = false;
				marsTerminal.print("The crew loading is now disabled.");	
	        	marsTerminal.print(System.lineSeparator());
			}
			else {
				useCrew = true;
				marsTerminal.print("The crew loading is now enabled.");
	        	marsTerminal.print(System.lineSeparator());
			}

	    	sandboxCfg = configureSandoxMode();
    	}
    	
        else if ((GameManager.sandboxCfg).equals("3")) {
			marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Starting a new simulation in Sandbox Mode...");
    	}
    	
    	else if ((GameManager.sandboxCfg).equals("4")) {
			marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Back to the previous menu.");
        	marsTerminal.print(System.lineSeparator());
        	
			return selectMode();
        }
        
        else {
        	sandboxCfg = configureSandoxMode();
        }
    	
    	return sandboxCfg;
	}
	
	/**
	 * Loads the previously saved commander profile
	 */
	private void loadPreviousProfile() {
		 
		try {
			boolean canLoad = CommanderProfile.loadProfile();
			if (canLoad) {
		        StringBuilder details = new StringBuilder();
		        profile.getCommander().outputDetails(details);
		        marsTerminal.println(System.lineSeparator() 
		        		+ "                * * *    Commander's Profile    * * *" 
		        		+ System.lineSeparator()
		        		+ details.toString()
		        		+ System.lineSeparator());
	            
	            boolean like = textIO.newBooleanInputReader().withDefaultValue(true)
	            		.read("Would you like to use this profile ?");
	            
	        	if (!like) {
	    			marsTerminal.print(System.lineSeparator() 
	    					+ "Back to the mode selection." 
	    					+ System.lineSeparator()
	    					+ System.lineSeparator());
	    			
	    			selectMode();
	        	}
			}
			
			else {
				System.out.println("Can't find the 'commander.txt' file.");
    			marsTerminal.print(System.lineSeparator() 
    					+ "Can't find the 'commander.txt' file." 
    					+ System.lineSeparator()
    					+ System.lineSeparator());
    			
    			selectMode();
			}
    	
		} catch (IOException e) {
			logger.severe("Error loading the commander profile.");
			marsTerminal.print(System.lineSeparator() 
					+ "Error loading the commander profile." 
					+ System.lineSeparator()
					+ System.lineSeparator());
			
			selectMode();
		}
		
	}

	public static void startLayer() {
		marsTerminal.startLayer();
	}
	
	/**
	 * Loads the terminal menu
	 */
	public static void loadTerminalMenu() {

		UserChannel channel = new TextIOChannel(textIO);
		// Console is always an admin
		Set<ConversationRole> roles = new HashSet<>();
		roles.add(ConversationRole.ADMIN);
        Conversation conversation = new Conversation(channel, new TopLevel(), roles, sim);

        conversation.interact();
		logger.info("Conversation ended");

	}
	
	/**
	 * Get the Commander's profile
	 * 
	 * @return profile
	 */
	public CommanderProfile getProfile() {
		return profile;
	}
	
    public MarsTerminal getTerminal() {
    	return marsTerminal;
    }
    
    public static void disposeTerminal() {
    	marsTerminal.getFrame().setVisible(false);
    	marsTerminal.dispose(null);
    }
    
    public SwingHandler getHandler() {
    	return handler;
    }
    
    public GameManager getGameManager() {
    	return gm;
    }
    
	public boolean getUseCrew() {
		return useCrew;
	}

	/*
	 * Get the dimension of the screen size selected by the user.
	 * This is null if none has been selected.
	 */
	public static Dimension getSelectedScreen() {
		if (selectedScreen >= 0) {
			return screenSizes[selectedScreen];
		}
		return null;
	}

}
