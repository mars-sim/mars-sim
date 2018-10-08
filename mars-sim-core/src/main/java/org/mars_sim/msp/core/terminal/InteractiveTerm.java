/**
 * Mars Simulation Project
 * InteractiveTerm.java
 * @version 3.1.0 2018-10-04
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.msp.core.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.beryx.textio.AbstractTextTerminal;
import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.jline.JLineTextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MasterClock;

public class InteractiveTerm {


    private static final String KEY_STROKE_UP = "pressed UP";
    private static final String KEY_STROKE_DOWN = "pressed DOWN";

    private String originalInput = "";
    private int choiceIndex = -1;
    private String[] choices = {};

	private final SwingTextTerminal terminal;

	private static CommanderProfile profile;
	
	private static TextIO textIO;
	
	private static MasterClock masterClock;
	
	private boolean keepRunning;
	
	public InteractiveTerm() {
		
        terminal = new SwingTextTerminal();
        terminal.init();
        
        textIO = new TextIO(terminal);
        
        setUpArrows();
	}
	
    public static void main(String[] args) {	
    	new InteractiveTerm().startCommanderMode(); 
    }
    
	/**
	 * Initialize the text-io terminal.
	 */
	public void startCommanderMode() {

		initializeTerminal();
		
		profile = new CommanderProfile(this);

		terminal.print(System.lineSeparator() 
				+ "---------------------- Mars Simulation Project ----------------------" 
				+ System.lineSeparator()
				+ System.lineSeparator());
		
		setChoices("y", "n");
		String input = textIO.newStringInputReader()//.withDefaultValue('n')
//				.withInlinePossibleValues("y", "n")
			    .read("Do you want to be added as the commander of a settlement? [y/n]");	
		
		if (input.equals("y") || input.equals("Y")) {
			terminal.print(
//					System.lineSeparator() +
//					"Press UP and DOWN to show a possible list of values (if available)" +
					System.lineSeparator());
			setChoices();
			profile.accept(textIO, null);
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
		
		// Prevent allow users from arbitrarily close the terminal by clicking top right close button
		terminal.registerUserInterruptHandler(term -> {}, false);
        
		while (keepRunning) {
		    BiConsumer<TextIO, RunnerData> menu = chooseMenu(textIO);
		    //TextIO textIO = chooseTextIO();
		    terminal.printf(System.lineSeparator());
//			setChoices();//"1", "2", "3", "4");
		    menu.accept(textIO, null);
	    	if (masterClock == null)
	    		masterClock = Simulation.instance().getMasterClock();
		    // if the sim is being saved, enter this while loop
			while (masterClock.isSavingSimulation()) {
		    	delay(500L);
		    }
		}
	}
    
	
    public static void clearScreen(TextTerminal terminal) {
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
        		new ChatMenu(),
                new TimeRatioMenu(),
                new SaveMenu(),
                new ExitMenu()
        );

        BiConsumer<TextIO, RunnerData> app = textIO.<BiConsumer<TextIO, RunnerData>>newGenericInputReader(null)
            .withNumberedPossibleValues(apps)
            .read(System.lineSeparator() 
            		+ "---------------------- Mars Simulation Project ----------------------" 
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
	
    public SwingTextTerminal getTerminal() {
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
    
}
