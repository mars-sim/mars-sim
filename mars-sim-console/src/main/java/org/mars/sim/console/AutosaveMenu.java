/**
 * Mars Simulation Project
 * AutosaveMenu.java
 * @version 3.1.0 2018-11-15
 * @author Manny Kung
 */
package org.mars.sim.console;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars.sim.console.AppUtil;
import org.mars.sim.console.RunnerData;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.time.AutosaveScheduler;

import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * A menu for choosing the autosave time interval in TextIO.
 */
public class AutosaveMenu implements BiConsumer<TextIO, RunnerData> {
	  
	private static final Logger logger = Logger.getLogger(AutosaveMenu.class.getName());

	private SwingTextTerminal terminal;
	
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new AutosaveMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
    	terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);
        
        Interval interval = new Interval();
        SwingHandler handler = new SwingHandler(textIO, "console", interval);

        printCurrentSetting();
        
        handler.addIntTask("interval", "Enter the new time interval [in mins]", false)
    	.withInputReaderConfigurator(r -> r.withMinVal(0).withMaxVal(360));
//    	.addChoices(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
    
	    handler.executeOneTask();
	
	    int m = Interval.interval;
	    
	    if (m == 0) {
	    	// cancel
	    	String s = "The autosave time interval remains unchanged.";
	        terminal.printf(System.lineSeparator() + s + System.lineSeparator());
	        logger.config(s);
	    }
	    
	    else if (m <= 360) {
			AutosaveScheduler.cancel();
			AutosaveScheduler.start(m);   
			String s = "The new autosave time interval is now once every " + m + " minutes.";
	        terminal.printf(System.lineSeparator() + s + System.lineSeparator());
	        logger.config(s);
	        
		}
	    else
	        terminal.printf(
	        		"Invalid value." 
	        		+ System.lineSeparator() 
	        		+  "Please choose a number between 1 and 360 (0 to quit)" 
	            		+ System.lineSeparator());
            
    }

    /**
     * Prints the current autosave setting
     */
    public void printCurrentSetting() {
    	int m =  SimulationConfig.instance().getAutosaveInterval();
        terminal.println("The current autosave time interval is once every " + m + " minutes."
        		+ System.lineSeparator());
        double sec = Math.round(AutosaveScheduler.getRemainingSeconds()/60.0 *10.0)/10.0;
        terminal.println("You have " + sec + " remaining minutes before the next autosave"
        		+ System.lineSeparator());
        
        terminal.println("Please choose a number between 1 and 360 (0 to quit)"
        		+ System.lineSeparator());
    }
    
    @Override
    public String toString() {
        return "Change Autosave Timer";
    }
    
    private static class Interval {
        public static int interval;

        @Override
        public String toString() {
            return "\n\tThe new Autosave time interval is once every " + interval + " minutes.";
        }
    }
}
