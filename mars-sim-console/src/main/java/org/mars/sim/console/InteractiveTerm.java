/**
 * Mars Simulation Project
 * InteractiveTerm.java
 * @version 3.1.0 2018-10-04
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars.sim.console;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import org.beryx.textio.AbstractTextTerminal;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.jline.JLineTextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * The InteractiveTerm class builds a text-based console interface and handles the interaction with players
 */
public class InteractiveTerm {

	private static Logger logger = Logger.getLogger(InteractiveTerm.class.getName());

    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";
    private static final String KEY_ESC = "ESCAPE";
    
    private String originalInput = "";
    private String[] choices = {};

    private int choiceIndex = -1;

    private static boolean consoleEdition = false;
    
    private static boolean keepRunning;
    
    private static boolean useCrew = true;
	
	private static MarsTerminal terminal;
	
	private static ChatMenu chatMenu;

	private static CommanderProfile profile;
	
	private static TextIO textIO;
	
	private static MasterClock masterClock;
	
	private static SwingHandler handler;
	
	private static GameManager gm;

	public InteractiveTerm(boolean consoleEdition) {
		this.consoleEdition = consoleEdition;
		
		terminal = new MarsTerminal();
        terminal.init();
        
        textIO = new TextIO(terminal);
        
        setUpArrows();
        
        setUpESC();
	}
	
    
    public static void main(String[] args) {	
    	new InteractiveTerm(true).startModeSelection();
    }
 
	
	/**
	 * Asks users what mode to run in a text-io terminal.
	 */
	public boolean startModeSelection() {

		initializeTerminal();
		
		profile = new CommanderProfile(this);

		gm = new GameManager();
	
        handler = new SwingHandler(textIO, "console", gm);
        
		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
		terminal.registerUserInterruptHandler(term -> {}, false);
		
		terminal.print(System.lineSeparator() 
				+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------\n");
//				+ "                                   r" + Simulation.BUILD +"\n");
//				+ System.lineSeparator()
//				+ System.lineSeparator());
		
		return selectMode();
	}
	
	
	/**
	 * Selects the game mode
	 * 
	 * @return
	 */
	public boolean selectMode() {
		boolean useSCE = false;
		
		terminal.print(System.lineSeparator()
				+ System.lineSeparator()
				+ "0. Exit "
				+ System.lineSeparator()
				+ "1. Command Mode "
				+ System.lineSeparator()
				+ "2. Sandbox Mode "
				+ System.lineSeparator()
				+ System.lineSeparator()
				);
			
        handler.addStringTask("input", "Select the Game Mode:", false).addChoices("0", "1", "2").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.input.equals("0")) {
            Simulation sim = Simulation.instance();
        	sim.endSimulation(); 
    		sim.getSimExecutor().shutdownNow();
//    		if (sim.getMasterClock() != null)
//    			sim.getMasterClock().exitProgram();
    		logger.info("Exiting the Simulation.");
    		setKeepRunning(false);
			System.exit(0);
    		disposeTerminal();
        }
        else if (GameManager.input.equals("1")) {
        	useSCE = selectCommandMode();
        }
        
        else if (GameManager.input.equals("2")) {
        	useSCE = selectSandoxMode();
        }
        
		terminal.print(System.lineSeparator());
		
        return useSCE;
	}
	
	/**
	 * Selects the simulation configuration editor 
	 * 
	 * @return
	 */
	public boolean selectSCE() {
		boolean useSCE = false;
		terminal.println(System.lineSeparator());
		
	    terminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "           * * *  Command Mode - Site Selection  * * *" 
         		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Proceed with default site selection."
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "1. Open Simulation Configuration Editor (SCE)."
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Note: Console Editon does not have SCE."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("useSCE", "Enter your choice:", false).addChoices("0", "1").constrainInputToChoices();
        handler.executeOneTask();

        if ((GameManager.useSCE).equals("0")) {
        	terminal.print(System.lineSeparator());
			terminal.print("Starting the simulation...");	
        }
        
        else if ((GameManager.useSCE).equals("1")) {
        	if (consoleEdition) {
				terminal.print(System.lineSeparator());
				terminal.print("Sorry. The Console Edition of mars-sim does not come with the SCE.");	
				terminal.print(System.lineSeparator());
        	}
        	else {
				terminal.print(System.lineSeparator());
				terminal.print("Loading Simulation Configuration Editor...");
				terminal.print(System.lineSeparator());
				useSCE = true;
        	}
        }
        
		terminal.print(System.lineSeparator());
		
        return useSCE;
	}
	
	/**
	 * Selects the command mode
	 * 
	 * @return
	 */
	public boolean selectCommandMode() {
		boolean useSCE = false;
		
		// Set the Game Mode to Command Mode in GameManager
		GameManager.mode = GameMode.COMMAND;
		
        terminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "            * * *  Command Mode - Crew Selection  * * *" 
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Back to previous"
				+ System.lineSeparator()
				+ "1. Enable/Disable Alpha Crew"
				+ System.lineSeparator()
				+ "2. Set up Commander's Profile"
				+ System.lineSeparator()
				+ "3. Load from previously saved Commander Profile"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Note 1: Alpha Crew is loaded by default."
				+ System.lineSeparator()
				+ "Note 2: Console Editon does NOT have the SCE."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("command0", "Enter your choice:", false).addChoices("0", "1", "2", "3").constrainInputToChoices();
        handler.executeOneTask();

        if ((GameManager.command0).equals("0")) {
			terminal.print(System.lineSeparator());
			terminal.print("Back to the previous page..");
			return selectMode();
        }
        
        else if ((GameManager.command0).equals("1")) {
			terminal.print(System.lineSeparator());
			if (useCrew) {			
				useCrew = false;
				terminal.print("Alpha Crew is now DISABLED.");
			}
			else {
				useCrew = true;
				terminal.print("Alpha Crew is now ENABLED.");
			}
			
	    	// Set the alpha crew use
	    	UnitManager.setCrew(useCrew);
	    	
			useSCE = selectCommandMode();
			
    	}
    	
    	else if ((GameManager.command0).equals("2")) {
			terminal.print(System.lineSeparator());
			// Set new profile
			profile.accept(textIO, null);
			
			useSCE = selectSCE();
    	}
    	
    	else if ((GameManager.command0).equals("3")) {
    		// Load from previously saved profile
    		loadPreviousProfile();
    		
    		useSCE = selectSCE();
    	}
        
		terminal.print(System.lineSeparator());
		
        return useSCE;
	}
 
	
	/**
	 * Selects the sandbox mode
	 * 
	 * @return
	 */
	public boolean selectSandoxMode() {
		boolean useSCE = false;
		
		GameManager.mode = GameMode.SANDBOX;

        terminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "           * * *  Sandbox Mode - Crew and Site Selection  * * *" 
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Proceed with the Default Site Templates."
        		+ System.lineSeparator()
				+ "1. Open Simulation Configuration Editor (SCE) "
				+ System.lineSeparator()
				+ "2. Enable/Disable Alpha Crew"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Note 1: Alpha Crew is loaded by default."
				+ System.lineSeparator()
				+ "Note 2: Console Editon does not have SCE."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("sandbox0", "Enter your choice:", false).addChoices("0", "1", "2").constrainInputToChoices();
        handler.executeOneTask();

    	if ((GameManager.sandbox0).equals("0")) {
			terminal.print(System.lineSeparator());
			terminal.print("Starting the simulation...");
			terminal.print(System.lineSeparator());
    	}
    	
    	else if ((GameManager.sandbox0).equals("1")) {
        	if (consoleEdition) {
				terminal.print(System.lineSeparator());
				terminal.print("Sorry. The Console Edition of mars-sim does not come with the SCE.");	
				terminal.println(System.lineSeparator());
				
				useSCE = selectSandoxMode();
        	}
        	else {
				terminal.print(System.lineSeparator());
				terminal.print("Loading Simulation Configuration Editor...");
				terminal.println(System.lineSeparator());
				
				useSCE = true;
        	}
        }
        
        else if ((GameManager.sandbox0).equals("2")) {
			terminal.print(System.lineSeparator());
			if (useCrew) {			
				useCrew = false;
				terminal.print("Alpha Crew is now DISABLED.");	
			}
			else {
				useCrew = true;
				terminal.print("Alpha Crew is now ENABLED.");
			}
			
			terminal.print(System.lineSeparator());
			terminal.print(System.lineSeparator());
			
	    	// Set the alpha crew use
	    	UnitManager.setCrew(useCrew);
	    	
	    	useSCE = selectSandoxMode();
    	}
    	
//        else {
//        	useSCE = selectSandoxMode();
//        }
    	
    	return useSCE;
	}
	
	/**
	 * Loads the previously saved commander profile
	 */
	public void loadPreviousProfile() {
		 
		try {
			boolean canLoad = CommanderProfile.loadProfile();
			
			if (canLoad) {
	            terminal.println(System.lineSeparator() 
	            		+ System.lineSeparator()
	            		+ "                * * *  Commander's Profile * * *" 
	            		+ System.lineSeparator()
	            		+ profile.getCommander().toString()
	            		+ System.lineSeparator());
//	            UnitManager.setCommanderMode(true);
	            
	            boolean like = textIO.newBooleanInputReader().withDefaultValue(true).read("Would you like to us this profile ?");
	            
	        	if (!like) {
	    			terminal.print(System.lineSeparator() 
	    					+ "Back to the beginning." 
	    					+ System.lineSeparator()
	    					+ System.lineSeparator());
	    			selectMode();
	        	}
			}
			
			else {
    			terminal.print(System.lineSeparator() 
    					+ "Can't find the 'commander.profile' file." 
    					+ System.lineSeparator()
    					+ System.lineSeparator());
    			selectMode();
			}
    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.severe("Error loading the commander's profile.");
			terminal.print(System.lineSeparator() 
					+ "Error loading the commander's profile." 
					+ System.lineSeparator()
					+ System.lineSeparator());
			selectMode();
		}
		
	}
	
	/**
	 * Initialize the terminal
	 */
	public static void initializeTerminal() {
		keepRunning = true;
	}
	
	
	/**
	 * Loads the terminal menu
	 */
	public static void loadTerminalMenu() {
		// WARNING : loadTerminalMenu() Need to be inside Sim Executor Thread in order to work
		logger.config("Calling loadTerminalMenu()");

		// Call ChatUils' default constructor to initialize instances
		new ChatUtils();
		chatMenu = new ChatMenu();
		
		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
//		terminal.registerUserInterruptHandler(term -> {
//				chatMenu.executeQuit();
//				terminal.resetToBookmark("MENU");
//			}, false);
            
	    // Set the bookmark here
//        terminal.setBookmark("MENU");
		keepRunning = true;
		
		while (keepRunning) {
			     
		    BiConsumer<TextIO, RunnerData> menu = chooseMenu(textIO);
		    terminal.printf(System.lineSeparator());
	       
		    // Set up the prompt for the menu
		    menu.accept(textIO, null);
	        
	    	if (masterClock == null)
	    		masterClock = Simulation.instance().getMasterClock();
	    	
		    // if the sim is being saved, enter this while loop
			while (masterClock.isSavingSimulation()) {
		    	delay(500L);
		    }
		}
//        terminal.resetToBookmark("MENU");
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
     * Presents choices in the console menu
     * 
     * @param textIO
     * @return {@link BiConsumer}
     */
    private static BiConsumer<TextIO, RunnerData> chooseMenu(TextIO textIO) {
        List<BiConsumer<TextIO, RunnerData>> apps = Arrays.asList(
        		chatMenu,
                new AutosaveMenu(),
                new SaveMenu(),
                new TimeRatioMenu(),
                new LogMenu(),
                new ExitMenu()
        );
       
        BiConsumer<TextIO, RunnerData> app = textIO.<BiConsumer<TextIO, RunnerData>>newGenericInputReader(null)
            .withNumberedPossibleValues(apps)
            .read(System.lineSeparator() 
            		+ "-------------------  C O N S O L E   M E N U  -------------------" 
            		+ System.lineSeparator());
        String propsFileName = app.getClass().getSimpleName() + ".properties";
        System.setProperty(AbstractTextTerminal.SYSPROP_PROPERTIES_FILE_LOCATION, propsFileName);
//        profile.term().moveToLineStart();	    
        return app;
    }
    
    
    /**
     * Sets up arrow keys
     */
    public void setUpArrows() {
        terminal.registerHandler(KEY_STROKE_UP, t -> {
            if(choiceIndex < 0) {
                originalInput = terminal.getPartialInput();
            }
            if(choiceIndex < choices.length - 1) {
                choiceIndex++;
                t.replaceInput(choices[choiceIndex], false);
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

        terminal.registerHandler(KEY_STROKE_DOWN, t -> {
            if(choiceIndex >= 0) {
                choiceIndex--;
                String text = (choiceIndex < 0) ? originalInput : choices[choiceIndex];
                t.replaceInput(text, false);
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });
    }
    
    /**
     * Sets up the ESC key
     */
    public void setUpESC() {
        terminal.registerHandler(KEY_ESC, t -> {
        	MasterClock mc = Simulation.instance().getMasterClock();
        	if (mc != null) {
				if (mc.isPaused()) {
					mc.setPaused(false, false);
					terminal.printf(System.lineSeparator() + System.lineSeparator());
					terminal.printf("                          [ Simulation Unpaused ]");
					//terminal.printf(System.lineSeparator() + System.lineSeparator());
				}
				else {
					terminal.resetLine();
					mc.setPaused(true, false);
//					terminal.printf(System.lineSeparator() + System.lineSeparator());
					terminal.printf("                           [ Simulation Paused ]");
					//terminal.printf(System.lineSeparator() + System.lineSeparator());
				}
        	}
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

    }
    	
    /**
     * Sets choice strings
     * 
     * @param choices
     */
    public void setChoices(String... choices) {
        this.originalInput = "";
        this.choiceIndex = -1;
        this.choices = choices;
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
    	return terminal;
    }
    
    public static TextIO getTextIO() {
    	return textIO;
    }
	
    public static void setKeepRunning(boolean value) {
    	keepRunning = value;
    }
    
    public static void disposeTerminal() {
    	terminal.dispose(null);
    }
    
    public SwingHandler getHandler() {
    	return handler;
    }
    
    public GameManager getGameManager() {
    	return gm;
    }
}
