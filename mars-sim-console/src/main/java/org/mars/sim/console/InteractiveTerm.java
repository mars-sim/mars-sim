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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import org.beryx.textio.AbstractTextTerminal;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.jline.JLineTextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * The InteractiveTerm class builds a text-based console interface and handles the interaction with players
 */
public class InteractiveTerm {

	private static Logger logger = Logger.getLogger(InteractiveTerm.class.getName());
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";
    private static final String KEY_ESC = "ESCAPE";
    
    private static String originalInput = "";
    private static String[] choices = {};

    private static int choiceIndex = -1;
    
    private volatile static int width = 1366;
    private volatile static int height = 768;
	
    private static boolean consoleEdition = false;
    
    private volatile static boolean keepRunning;
    
    private static boolean useCrew = true;
	
	private static MarsTerminal marsTerminal;
	
	private static ChatMenu chatMenu;
	
	private static ChatUtils chatUtils;

	private static CommanderProfile profile;
	
	private static TextIO textIO;
	
	private static Simulation sim = Simulation.instance();
    
	private static MasterClock masterClock;
	
	private static SwingHandler handler;
	
	private static GameManager gm;
	
//	private static InteractiveTerm interactiveTerm;

	public InteractiveTerm(boolean consoleEdition, boolean restart) {
		this.consoleEdition = consoleEdition;
//		interactiveTerm = this;
		
//		SwingUtilities.invokeLater(() -> {
			marsTerminal = new MarsTerminal(this);
	        marsTerminal.init();
			// Prevent allow users from arbitrarily close the terminal by clicking top right close button
			marsTerminal.registerUserInterruptHandler(term -> {}, false);
		
			logger.config("Done with MarsTerminal on " + Thread.currentThread().getName());
			
	        textIO = new TextIO(marsTerminal);
	        
			gm = new GameManager();
			
	        handler = new SwingHandler(textIO, "console", gm);
	        
	        setUpArrows();
	        
	    	logger.config("Done with setUpArrows on " + Thread.currentThread().getName());

	        setUpESC();
	        
	    	logger.config("Done with setUpESC on " + Thread.currentThread().getName());

	        if (restart) {
	//    		profile = new CommanderProfile(this);
	//
	//    		gm = new GameManager();
	//    		//  Re-initialize the GameManager
	//    		GameManager.initializeInstances(Simulation.instance().getUnitManager());
	    		
//	            handler = new SwingHandler(textIO, "console", gm);
	//    		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
//	    		marsTerminal.registerUserInterruptHandler(term -> {}, false);
	    		
	    		setKeepRunning(true);
	    		
	    		loadTerminalMenu();
	        }
	        
			logger.config("Done with InteractiveTerm's constructor is on " + Thread.currentThread().getName());
			
//		});
		  
	}
	
    
    public static void main(String[] args) {	
    	new InteractiveTerm(true, false).startConsoleMainMenu();
    }
 
	
	/**
	 * Asks players what to choose in beryx console main menu.
	 */
	public int startConsoleMainMenu() {
		logger.config("Staring startConsoleMainMenu()");

		initializeTerminal();
		
		profile = new CommanderProfile(this);
        		
		return selectMenu();
	}
	
	/**
	 * Selects the game mode
	 * 
	 * @return
	 */
	public int selectMenu() {
		int useSCE = 0;
	
		try {
		
		marsTerminal.print(System.lineSeparator() 
				+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------\n"
				+ System.lineSeparator()
				+ "                        * * *   Main Menu   * * *\n"
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
    		sim.getSimExecutor().shutdownNow();

    		logger.info("Exiting the Simulation.");
    		setKeepRunning(false);
			System.exit(0);
    		disposeTerminal();
        }
        else if (GameManager.menu.equals("1")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Starting a new sim...");
        	marsTerminal.print(System.lineSeparator());
        	useSCE = selectMode();
        }
        
        else if (GameManager.menu.equals("2")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Loading from a saved sim...");
        	marsTerminal.print(System.lineSeparator());
        	useSCE = 2;
        }
        
        else if (GameManager.menu.equals("3")) {
        	marsTerminal.print(System.lineSeparator() 
    				+ System.lineSeparator()
    				+ "                        * * *   Resolution Menu   * * *\n"
    				+ System.lineSeparator()
    				+ System.lineSeparator()
    				+ "0. 1366 x 768 (Default)"
    				+ System.lineSeparator()
    				+ "1. 1280 x 800"
    				+ System.lineSeparator()
    				+ "2. 1280 x 1024"
    				+ System.lineSeparator()
    				+ "3. 1600 x 900"
    				+ System.lineSeparator()
       				+ "4. 1920 x 1080"
    				+ System.lineSeparator()
    	        	+ System.lineSeparator()
        			);
        	        			
        	String oldRes = "1366 x 768";
        	String newRes = "";
        	
        	marsTerminal.print("Current resolution : " + oldRes);
        	marsTerminal.print(System.lineSeparator());
        	marsTerminal.print(System.lineSeparator());
        	
        	handler.addStringTask("resolution", "Choose an option :", false).addChoices("0", "1", "2", "3", "4").constrainInputToChoices();
            handler.executeOneTask();

            if (GameManager.resolution.equals("0")) {
            	if (width != 1366)
            		width = 1366;
            	if (height != 768)
            		height = 768;
            	
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
            	if (width != 1920)
            		width = 1920;
            	if (height != 1080)
            		height = 1080;
            	
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
			LogConsolidated.log(Level.SEVERE, 0, sourceName, "RuntimeException detected.");
		}
		
        return useSCE;
	}
	
	/**
	 * Selects the game mode
	 * 
	 * @return
	 */
	public int selectMode() {
		int useSCE = 0;
		
		marsTerminal.print(System.lineSeparator() 
				+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------\n"
				+ System.lineSeparator()
				+ "                        * * *   Mode Selection   * * *\n"
//				+ "                                   r" + Simulation.BUILD +"\n");
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "0. Exit"
				+ System.lineSeparator()
				+ "1. Command Mode"
				+ System.lineSeparator()
				+ "2. Sandbox Mode"
				+ System.lineSeparator()
				+ System.lineSeparator()
				);
			
        handler.addStringTask("input", "Select the Game Mode:", false).addChoices("0", "1", "2").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.input.equals("0")) {
        	sim.endSimulation(); 
    		sim.getSimExecutor().shutdownNow();

    		logger.info("Exiting the Simulation.");
    		setKeepRunning(false);
			System.exit(0);
    		disposeTerminal();
        }
        else if (GameManager.input.equals("1")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Go to Command Mode...");
        	marsTerminal.print(System.lineSeparator());
        	useSCE = selectCommandMode();
        }
        
        else if (GameManager.input.equals("2")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Go to Sandbox Mode...");
        	marsTerminal.print(System.lineSeparator());
        	useSCE = selectSandoxMode();
        }
        
		marsTerminal.print(System.lineSeparator());
		
        return useSCE;
	}
	
	/**
	 * Selects the simulation configuration editor 
	 * 
	 * @return
	 */
	public int selectSCE() {
		int useSCE = 0;
		marsTerminal.println(System.lineSeparator());
		
	    marsTerminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "           * * *   Command Mode - Site Selection   * * *" 
         		+ System.lineSeparator()
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Exit"
        		+ System.lineSeparator()
				+ "1. Open site editor"
				+ System.lineSeparator()
				+ "2. Proceed with default site selection"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "NOTE: the Console Editon does not come with the Site Editor."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("useSCE", "Enter your choice:", false).addChoices("0", "1", "2").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.useSCE.equals("0")) {
        	sim.endSimulation(); 
    		sim.getSimExecutor().shutdownNow();

    		logger.info("Exiting the Simulation.");
    		setKeepRunning(false);
			System.exit(0);
    		disposeTerminal();
        }
        
        else if ((GameManager.useSCE).equals("1")) {
        	if (consoleEdition) {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Sorry. The Console Edition of mars-sim does not come with the Site Editor.");	
				marsTerminal.print(System.lineSeparator());
				
				useSCE = 0;
        	}
        	else {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Loading the Site Editor...");
				marsTerminal.print(System.lineSeparator());
				
				useSCE = 1;
        	}
        }
        
        if ((GameManager.useSCE).equals("2")) {
        	marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Starting a new simulation with default site selection...");
        	marsTerminal.print(System.lineSeparator());
			useSCE = 0;
        }
        
		marsTerminal.print(System.lineSeparator());
		
        return useSCE;
	}
	
	
	/**
	 * Selects the command mode
	 * 
	 * @return
	 */
	public int selectCommandMode() {
		int useSCE = 0;
		
		// Set the Game Mode to Command Mode in GameManager
		GameManager.mode = GameMode.COMMAND;
		
        marsTerminal.println(System.lineSeparator() 
        		+ System.lineSeparator()
        		+ "            * * *   Command Mode - Crew Selection   * * *" 
        		+ System.lineSeparator()
        		+ System.lineSeparator()
				+ "0. Exit"
				+ System.lineSeparator()
				+ "1. Enable/disable the alpha crew"
				+ System.lineSeparator()
				+ "2. Set up commander profile"
				+ System.lineSeparator()
				+ "3. Load commander profile"
				+ System.lineSeparator()
				+ "4. Back to previous menu"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "NOTE 1: By default, the alpha crew as defined in crew.xml is ENABLED unless it is changed."
				+ System.lineSeparator()
//				+ "Note 2: Console Editon does NOT have the Site Editor."
//				+ System.lineSeparator()
				);
		
        handler.addStringTask("commanderProfile", "Enter your choice:", false).addChoices("0", "1", "2", "3", "4").constrainInputToChoices();
        handler.executeOneTask();
        
        if (GameManager.input.equals("0")) {
        	sim.endSimulation(); 
    		sim.getSimExecutor().shutdownNow();

    		logger.info("Exiting the Simulation.");
    		setKeepRunning(false);
			System.exit(0);
    		disposeTerminal();
        }
        
        else if ((GameManager.commanderProfile).equals("1")) {
			marsTerminal.print(System.lineSeparator());
			if (useCrew) {			
				useCrew = false;
				marsTerminal.print("The alpha crew has been DISABLE.");
	        	marsTerminal.print(System.lineSeparator());
			}
			else {
				useCrew = true;
				marsTerminal.print("The alpha crew has been ENABLED.");
	        	marsTerminal.print(System.lineSeparator());
			}
			
	    	// Set the alpha crew use
	    	UnitManager.setCrew(useCrew);
	    	
			useSCE = selectCommandMode();
			
    	}
    	
    	else if ((GameManager.commanderProfile).equals("2")) {
			marsTerminal.print(System.lineSeparator());
			// Set new profile
			profile.accept(textIO, null);
			
			useSCE = selectSCE();
    	}
    	
    	else if ((GameManager.commanderProfile).equals("3")) {
    		// Load from previously saved profile
    		loadPreviousProfile();
    		
    		useSCE = selectSCE();
    	}
        
    	else if ((GameManager.commanderProfile).equals("4")) {
			marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Back to the previous menu..");
        	marsTerminal.print(System.lineSeparator());
			return selectMode();
        }
        
		marsTerminal.print(System.lineSeparator());
		
        return useSCE;
	}
 
	
	/**
	 * Selects the sandbox mode
	 * 
	 * @return
	 */
	public int selectSandoxMode() {
		int useSCE = 0;
		
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
				+ "2. Enable/disable alpha crew"
				+ System.lineSeparator()
				+ "3. Proceed"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "NOTE 1: The alpha crew defined in crew.xml will be loaded by default."
				+ System.lineSeparator()
				+ "NOTE 2: The console editon does not have the site editor."
				+ System.lineSeparator()
				);
		
        handler.addStringTask("sandbox0", "Enter your choice:", false).addChoices("0", "1", "2", "3").constrainInputToChoices();
        handler.executeOneTask();

        if (GameManager.sandbox0.equals("0")) {
        	sim.endSimulation(); 
    		sim.getSimExecutor().shutdownNow();

    		logger.info("Exiting the Simulation.");
    		setKeepRunning(false);
			System.exit(0);
    		disposeTerminal();
        }
    	
    	else if ((GameManager.sandbox0).equals("1")) {
        	if (consoleEdition) {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Sorry. The Console Edition of mars-sim does not come with the Site Editor.");	
				marsTerminal.println(System.lineSeparator());
				
				useSCE = selectSandoxMode();
        	}
        	else {
				marsTerminal.print(System.lineSeparator());
				marsTerminal.print("Loading the Site Editor...");
				marsTerminal.println(System.lineSeparator());
				
				useSCE = 1;
        	}
        }
        
        else if ((GameManager.sandbox0).equals("2")) {
			marsTerminal.print(System.lineSeparator());
			if (useCrew) {			
				useCrew = false;
				marsTerminal.print("Loading the alpha crew will be DISABLED.");	
	        	marsTerminal.print(System.lineSeparator());
			}
			else {
				useCrew = true;
				marsTerminal.print("Loading the alpha crew will be ENABLED.");
	        	marsTerminal.print(System.lineSeparator());
			}
			
			marsTerminal.print(System.lineSeparator());
			marsTerminal.print(System.lineSeparator());
			
	    	// Set the alpha crew use
	    	UnitManager.setCrew(useCrew);
	    	
	    	useSCE = selectSandoxMode();
    	}
    	
        else if ((GameManager.sandbox0).equals("3")) {
			marsTerminal.print(System.lineSeparator());
			marsTerminal.print("Starting a new simulation in Sandbox Mode...");
			marsTerminal.print(System.lineSeparator());
    	}
    	
        else {
        	useSCE = selectSandoxMode();
        }
    	
    	return useSCE;
	}
	
	/**
	 * Loads the previously saved commander profile
	 */
	public void loadPreviousProfile() {
		 
		try {
			boolean canLoad = CommanderProfile.loadProfile();
			
			if (canLoad) {
	            marsTerminal.println(System.lineSeparator() 
	            		+ System.lineSeparator()
	            		+ "                * * *   Commander Profile  * * *" 
	            		+ System.lineSeparator()
	            		+ profile.getCommander().toString()
	            		+ System.lineSeparator());
//	            UnitManager.setCommanderMode(true);
	            
	            boolean like = textIO.newBooleanInputReader().withDefaultValue(true).read("Would you like to use this profile ?");
	            
	        	if (!like) {
	    			marsTerminal.print(System.lineSeparator() 
	    					+ "Back to the mode selection" 
	    					+ System.lineSeparator()
	    					+ System.lineSeparator());
	    			selectMode();
	        	}
			}
			
			else {
    			marsTerminal.print(System.lineSeparator() 
    					+ "Can't find the 'commander.profile' file." 
    					+ System.lineSeparator()
    					+ System.lineSeparator());
    			selectMode();
			}
    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.severe("Error loading the commander profile.");
			marsTerminal.print(System.lineSeparator() 
					+ "Error loading the commander profile." 
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
	
	public ChatMenu getChatMenu() {
		return chatMenu;
	}
	
	/**
	 * Loads the terminal menu
	 */
	public static void loadTerminalMenu() {
		// WARNING : loadTerminalMenu() Need to be inside Sim Executor Thread in order to work
//		logger.config("Calling loadTerminalMenu()");

		// Call ChatUils' default constructor to initialize instances
		chatUtils = new ChatUtils();
		logger.config("Done with ChatUtils() on " + Thread.currentThread().getName());
		
		chatMenu = new ChatMenu(consoleEdition);
		logger.config("Done with ChatMenu() on " + Thread.currentThread().getName());
		
		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
//		terminal.registerUserInterruptHandler(term -> {
//				chatMenu.executeQuit();
//				terminal.resetToBookmark("MENU");
//			}, false);
            
	    // Set the bookmark here
//        terminal.setBookmark("MENU");
		
		setUpRunningLoop();
		
		logger.config("Done with loadTerminalMenu() on " + Thread.currentThread().getName());
	}
	
	
	public static void setUpRunningLoop() {
		logger.config("Starting setUpRunningLoop() on " + Thread.currentThread().getName());
		
		if (sim == null) {
			sim = Simulation.instance();
		}
//    	if (masterClock == null) {
//    		masterClock = sim.getMasterClock();
//            setMasterClock();
//    	}
    	
		while (keepRunning) {
			     
		    BiConsumer<TextIO, RunnerData> menu = chooseMenu(textIO);
		    marsTerminal.printf(System.lineSeparator());
	    	
		    // Set up the prompt for the menu
		    menu.accept(textIO, null);
	            		
		    // if the sim is being saved, enter this while loop
//			for (int i=0; i<10; i++) {
//				if (masterClock.isSavingSimulation())
//					delay(500L);
//		    }
		}
//        terminal.resetToBookmark("MENU");
		
//		return;
	}
	
//	/**
//	 * Restarts the terminal.
//	 */
//	public static void restartTerm() {
//
//		terminal = new MarsTerminal(interactiveTerm);
//        terminal.init();
//        
//        textIO = new TextIO(terminal);
//        
//        setUpArrows();
//        
//        setUpESC();
//        
//		initializeTerminal();
//		
//		profile = new CommanderProfile(interactiveTerm);
//
//		gm = new GameManager();
//	
//        handler = new SwingHandler(textIO, "console", gm);
//        
//		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
//		terminal.registerUserInterruptHandler(term -> {}, false);
//
//	}
	
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

		String dir = Simulation.SAVE_DIR;;
		String title = Msg.getString("MainWindow.dialogLoadSavedSim");

		JFileChooser chooser = new JFileChooser(dir);
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
    public static void setUpArrows() {
        marsTerminal.registerHandler(KEY_STROKE_UP, t -> {
            if(choiceIndex < 0) {
                originalInput = marsTerminal.getPartialInput();
            }
            if(choiceIndex < choices.length - 1) {
                choiceIndex++;
                t.replaceInput(choices[choiceIndex], false);
            }
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

        marsTerminal.registerHandler(KEY_STROKE_DOWN, t -> {
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
    public static void setUpESC() {
        marsTerminal.registerHandler(KEY_ESC, t -> {
    		if (sim == null) {
    			sim = Simulation.instance();
    		}
        	if (masterClock == null) {
        		masterClock = sim.getMasterClock();
        	}
        	if (masterClock != null) {
				if (masterClock.isPaused()) {
					masterClock.setPaused(false, false);
//					terminal.printf(System.lineSeparator() + System.lineSeparator());
//					terminal.printf("                          [ Simulation Resumed ]");
					//terminal.printf(System.lineSeparator() + System.lineSeparator());
				}
				else {
//					terminal.resetLine();
					masterClock.setPaused(true, false);
//					terminal.printf(System.lineSeparator() + System.lineSeparator());
//					terminal.printf("                           [ Simulation Paused ]");
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
    	return marsTerminal;
    }
    
    public static TextIO getTextIO() {
    	return textIO;
    }
	
    public static void setKeepRunning(boolean value) {
    	keepRunning = value;
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
		return height;
	}
	
//    public static void setMasterClock() {
//    	marsTerminal.setMasterClock(masterClock);
//    }
	
}
