/**
 * Mars Simulation Project
 * InteractiveTerm.java
 * @version 3.1.0 2018-09-24
 * @author Manny Kung
 */

package org.mars.sim.console;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.jline.JLineTextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.TextIOChannel;
import org.mars.sim.console.chat.UserChannel;
import org.mars.sim.console.chat.simcommand.TopLevel;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.UnitManager;

/**
 * The InteractiveTerm class builds a text-based console interface and handles the interaction with players
 */
public class InteractiveTerm {

	private static Logger logger = Logger.getLogger(InteractiveTerm.class.getName());
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
    
	private final static int DEFAULT_WIDTH = 1920;
	private final static int DEFAULT_HEIGHT = 1080;
	private final static int HEIGHT_OFFSET = 25;
	
    private volatile static int width = DEFAULT_WIDTH;
    private volatile static int height = DEFAULT_HEIGHT;
	
    private static boolean consoleEdition = false;
    
    private static boolean useCrew = true;
	
	private static MarsTerminal marsTerminal;

	private static CommanderProfile profile;
	
	private static TextIO textIO;
	
	private static Simulation sim = Simulation.instance();
	
	private static SwingHandler handler;
	
	private static GameManager gm;
	
//	private static InteractiveTerm interactiveTerm;

	public InteractiveTerm(boolean consoleEdition, boolean restart) {
		this.consoleEdition = consoleEdition;
//		interactiveTerm = this;
		
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
	
    public static void main(String[] args) {	
    	new InteractiveTerm(true, false).startConsoleMainMenu();
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
	public int selectMenu() {
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
        }
        else if (GameManager.menu.equals("1")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Starting a new sim...");
        	marsTerminal.print(System.lineSeparator());
        	// Go to selectMode() to choose a mode
        	menuChoice = selectMode();
        }
        
        else if (GameManager.menu.equals("2")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Loading from a saved sim...");
        	marsTerminal.print(System.lineSeparator());
        	
        	menuChoice = 2;
        }
        
        else if (GameManager.menu.equals("3")) {
        	marsTerminal.print(System.lineSeparator() 
    				+ System.lineSeparator()
    				+ "                        * * *   Resolution Menu   * * *\n"
    				+ System.lineSeparator()
    				+ System.lineSeparator()
    				+ "0. 1920 x 1080 (Default)"
    				+ System.lineSeparator()
    				+ "1. 1280 x 800"
    				+ System.lineSeparator()
    				+ "2. 1280 x 1024"
    				+ System.lineSeparator()
    				+ "3. 1600 x 900"
    				+ System.lineSeparator()
       				+ "4. 1366 x 768"
    				+ System.lineSeparator()
    	        	+ System.lineSeparator()
        			);
        	        			
        	String oldRes = "1920 x 1080";
        	String newRes = "";
        	
        	marsTerminal.print("Current resolution : " + oldRes);
        	marsTerminal.print(System.lineSeparator());
        	marsTerminal.print(System.lineSeparator());
        	
        	handler.addStringTask("resolution", "Choose an option :", false).addChoices("0", "1", "2", "3", "4").constrainInputToChoices();
            handler.executeOneTask();

            if (GameManager.resolution.equals("0")) {
            	if (width != DEFAULT_WIDTH)
            		width = DEFAULT_WIDTH;
            	if (height != DEFAULT_HEIGHT)
            		height = DEFAULT_HEIGHT;
            	
            	newRes = width + " x " + height;
            }
            
            else if (GameManager.resolution.equals("1")) {
            	if (width != 1280)
            		width = 1280;
            	if (height != 800)
            		height = 800;
            	
            	newRes = width + " x " + height;
            }

            else if (GameManager.resolution.equals("2")) {
            	if (width != 1280)
            		width = 1280;
            	if (height != 1024)
            		height = 1024;
            	
            	newRes = width + " x " + height;
            }
        	
            else if (GameManager.resolution.equals("3")) {
            	if (width != 1600)
            		width = 1600;
            	if (height != 900)
            		height = 900;
            	
            	newRes = width + " x " + height;
            }
            
            else if (GameManager.resolution.equals("4")) {
            	if (width != 1366)
            		width = 1366;
            	if (height != 768)
            		height = 768;
            	
            	newRes = width + " x " + height;
            }
          
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("The screen resolution has been changed from '" + oldRes + "' to '" + newRes + "'");
        	marsTerminal.print(System.lineSeparator());
        	
        	return selectMenu();
        }
        
        
		marsTerminal.print(System.lineSeparator());
		
		} catch(RuntimeException e) {
//            throw new RuntimeException("read interrupted", e);
			e.printStackTrace();
			LogConsolidated.flog(Level.SEVERE, 0, sourceName, "RuntimeException detected.");
		}
		
        return menuChoice;
	}
	
	/**
	 * Selects the game mode
	 * 
	 * @return
	 */
	public int selectMode() {
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
	public int selectSite() {
		int siteChoice = 0;
		marsTerminal.println(System.lineSeparator());
		
	    marsTerminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "           * * *               Site Selection               * * *" 
         		+ System.lineSeparator()
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Exit"
        		+ System.lineSeparator()
				+ "1. Open site editor"
				+ System.lineSeparator()
				+ "2. Proceed to start the sim"
				+ System.lineSeparator()
				+ "3. Back to previous menu"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "NOTE: the Console Editon does not come with the Site Editor."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("site", "Enter your choice:", false).addChoices("0", "1", "2").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.site.equals("0")) {
        	sim.endSimulation(); 

			System.exit(0);
    		disposeTerminal();
        }
        
        else if ((GameManager.site).equals("1")) {
        	if (consoleEdition) {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Sorry. The Console Edition of mars-sim does not come with the Site Editor.");	
				marsTerminal.print(System.lineSeparator());
				
				siteChoice = 0;
        	}
        	else {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Loading the Site Editor...");
				marsTerminal.print(System.lineSeparator());
				
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
	public int configureCommandMode() {
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
				+ "1. Enable/disable alpha crew (currently " + (useCrew ? "Disabled" : "Enabled")  + ")"
				+ System.lineSeparator()
				+ "2. Set up commander profile"
				+ System.lineSeparator()
				+ "3. Load commander profile"
				+ System.lineSeparator()
				+ "4. Back to previous menu"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Note 1: By default the alpha crew (as defined in crew.xml) is Enabled unless being changed."
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
				marsTerminal.print("The alpha crew has been Disabled.");
	        	marsTerminal.print(System.lineSeparator());
			}
			else {
				useCrew = true;
				marsTerminal.print("The alpha crew has been Enabled.");
	        	marsTerminal.print(System.lineSeparator());
			}
			
	    	// Set the alpha crew use
	    	UnitManager.setCrew(useCrew);
	    	
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
	public int configureSandoxMode() {
		int sandboxCfg = 0;
		
		GameManager.mode = GameMode.SANDBOX;

        marsTerminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "           * * *  Sandbox Mode - Crew and Site Selection  * * *" 
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Exit"
        		+ System.lineSeparator()
				+ "1. Open site editor"
				+ System.lineSeparator()
				+ "2. Enable/disable alpha crew (currently " + (useCrew ? "Disabled" : "Enabled")  + ")"
				+ System.lineSeparator()
				+ "3. Proceed to start the sim"
				+ System.lineSeparator()
				+ "4. Back to previous menu"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Note 1: By default the alpha crew defined in crew.xml is enabled. Choose 2 to enable/disable."
				+ System.lineSeparator()
				+ "Note 2: The Console Editon does not come with the Site Editor."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("sandboxCfg", "Enter your choice:", false).addChoices("0", "1", "2", "3").constrainInputToChoices();
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
				marsTerminal.print("Loading the Site Editor...");
				marsTerminal.println(System.lineSeparator());
				
				sandboxCfg = 1;
        	}
        }
        
        else if ((GameManager.sandboxCfg).equals("2")) {
			marsTerminal.print(System.lineSeparator());
			if (useCrew) {			
				useCrew = false;
				marsTerminal.print("The alpha crew is now Disabled.");	
	        	marsTerminal.print(System.lineSeparator());
			}
			else {
				useCrew = true;
				marsTerminal.print("The alpha crew is now Enabled.");
	        	marsTerminal.print(System.lineSeparator());
			}
					
	    	// Set the alpha crew use
	    	UnitManager.setCrew(useCrew);
	    	
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
	public void loadPreviousProfile() {
		 
		try {
			boolean canLoad = CommanderProfile.loadProfile();
//			System.out.println("Just ran loadProfile()");
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
	
	public static void stopLayer() {
		marsTerminal.stopLayer();
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
	 * Clears the screen
	 * 
	 * @param terminal
	 */
    public static void clearScreen(TextTerminal<?> terminal) {
        if (terminal instanceof JLineTextTerminal) {
            terminal.print("\033[H\033[2J");
        } else if (terminal instanceof SwingTextTerminal) {
            ((SwingTextTerminal) terminal).resetToOffset(0);
        }
    }
    
	/**
	 * Performs the process of loading a simulation.
	 * 
	 * return true if a sim is loaded
	 */
	public boolean loadSimulationProcess() {
		sim.stop();

		String title = Msg.getString("MainWindow.dialogLoadSavedSim");

		JFileChooser chooser = new JFileChooser(SimulationFiles.getSaveDir());
		chooser.setDialogTitle(title); // $NON-NLS-1$
		if (chooser.showOpenDialog(marsTerminal.getFrame()) == JFileChooser.APPROVE_OPTION) {
			sim.loadSimulation(chooser.getSelectedFile());
			return true;
		}
		
		return false;
	}
	
    /**
     * Starts the time delay
     * 
     * @param millis
     */
    public static void delay(long millis) {
        try {
			TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    
    public static TextIO getTextIO() {
    	return textIO;
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
    
	public static int getWidth() {
		return width;
	}
	
	public static int getHeight() {
		return height - HEIGHT_OFFSET;
	}
	
//    public static void setMasterClock() {
//    	marsTerminal.setMasterClock(masterClock);
//    }
	
}
