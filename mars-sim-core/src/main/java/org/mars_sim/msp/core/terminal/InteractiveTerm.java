/**
 * Mars Simulation Project
 * InteractiveTerm.java
 * @version 3.1.0 2018-10-04
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.msp.core.terminal;

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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.time.MasterClock;

public class InteractiveTerm {

	private static Logger logger = Logger.getLogger(InteractiveTerm.class.getName());

    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";
    private static final String KEY_ESC = "ESCAPE";
    
    private String originalInput = "";
    private String[] choices = {};

    private int choiceIndex = -1;

    private boolean keepRunning;
	
	private static MarsTerminal terminal;
	
	private static ChatMenu chatMenu;

	private static CommanderProfile profile;
	
	private static TextIO textIO;
	
	private static MasterClock masterClock;
	
	private static SwingHandler handler;
	

	public InteractiveTerm() {
		
		terminal = new MarsTerminal();
        terminal.init();
        
        textIO = new TextIO(terminal);
        
        setUpArrows();
        
        setUpESC();
	}
	
    
    public static void main(String[] args) {	
    	new InteractiveTerm().startCommanderMode();
    	
    }
 
	
	/**
	 * Asks users what mode to run in a text-io terminal.
	 */
	public void startCommanderMode() {

		initializeTerminal();
		
		profile = new CommanderProfile(this);

		CommanderInput ci = new CommanderInput();
		
        handler = new SwingHandler(textIO, ci);
        
		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
		terminal.registerUserInterruptHandler(term -> {}, false);
		
		terminal.print(System.lineSeparator() 
				+ " ---------------  M A R S   S I M U L A T I O N   P R O J E C T  ---------------" 
				+ System.lineSeparator()
				+ System.lineSeparator());
		
		selectMode();
	}
	
	
	public void selectMode() {
		terminal.print("1. Commander Mode "
				+ System.lineSeparator()
				+ "2. Sandbox Mode "
				+ System.lineSeparator()
				+ System.lineSeparator()
				);
			
        handler.addStringTask("mode", "Enter your choice:", false).addChoices("1", "2").constrainInputToChoices();
        handler.executeOneTask();
        
		if ((CommanderInput.mode).equals("1")) {
			
	        terminal.println(System.lineSeparator() 
	        		+ "                * * *  COMMANDER'S PROFILE * * *" 
	        		+ System.lineSeparator()
					+ System.lineSeparator()
					+ "1. Set up new profile"
					+ System.lineSeparator()
					+ "2. Load from previous profile"
					+ System.lineSeparator()
					+ System.lineSeparator()
					);
			
	        handler.addStringTask("choice", "Enter your choice:", false).addChoices("1", "2").constrainInputToChoices();
	        handler.executeOneTask();
	        
	    	if ((CommanderInput.choice).equals("1")) {
				terminal.print(System.lineSeparator());
				profile.accept(textIO, null);
	    	}
	    	else {
	    		try {
					boolean canLoad = profile.loadProperties();
					
					if (canLoad) {
			            terminal.println(System.lineSeparator() 
			            		+ "                * * *  COMMANDER'S PROFILE * * *" 
			            		+ System.lineSeparator()
			            		+ profile.getCommander().toString()
			            		+ System.lineSeparator());
			            UnitManager.setCommander(true);
			            
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
//					e.printStackTrace();
					logger.severe("Error loading the commander's profile.");
	    			terminal.print(System.lineSeparator() 
	    					+ "Error loading the commander's profile." 
	    					+ System.lineSeparator()
	    					+ System.lineSeparator());
					selectMode();
				}
	    	}
		}
	}
	
	
	/**
	 * Initialize the terminal
	 */
	public void initializeTerminal() {
		keepRunning = true;
	}
	
	
	/**
	 * Loads the terminal menu
	 */
	public void loadTerminalMenu() {
		keepRunning = true;
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
        
		while (keepRunning) {
			     
		    BiConsumer<TextIO, RunnerData> menu = chooseMenu(textIO);
		    //TextIO textIO = chooseTextIO();
		    terminal.printf(System.lineSeparator());
		    
//			setChoices();//"1", "2", "3", "4");
		       
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
    
	
    public static void clearScreen(TextTerminal<?> terminal) {
        if (terminal instanceof JLineTextTerminal) {
            terminal.print("\033[H\033[2J");
        } else if (terminal instanceof SwingTextTerminal) {
            ((SwingTextTerminal) terminal).resetToOffset(0);
        }
    }
    
    public static void delay(long millis) {
        try {
			TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private static BiConsumer<TextIO, RunnerData> chooseMenu(TextIO textIO) {
        List<BiConsumer<TextIO, RunnerData>> apps = Arrays.asList(
        		chatMenu,
                new AutosaveMenu(),
                new SaveMenu(),
                new TimeRatioMenu(),
//                new Weather(),
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
					mc.setPaused(true, false);
					terminal.printf(System.lineSeparator() + System.lineSeparator());
					terminal.printf("                           [ Simulation Paused ]");
					//terminal.printf(System.lineSeparator() + System.lineSeparator());
				}
        	}
            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
        });

    }
    	
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
    
    public TextIO getTextIO() {
    	return textIO;
    }
	
    public void setKeepRunning(boolean value) {
    	keepRunning = value;
    }
    
    public void disposeTerminal() {
    	terminal.dispose(null);
    }
    
    public SwingHandler getHandler() {
    	return handler;
    }
    
    private static class CommanderInput {
        public static String mode;
        public static String choice;

        @Override
        public String toString() {
            return System.lineSeparator() +">" + mode;
        }
    }
    
}
