/**
 * Mars Simulation Project
 * TimeRatioMenu.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */
package org.mars.sim.console;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars.sim.console.AppUtil;
import org.mars.sim.console.RunnerData;
import org.mars_sim.msp.core.Simulation;

import java.util.function.BiConsumer;
import java.util.logging.Logger;


/**
 * A menu for choosing the time ratio in TextIO.
 */
public class ExitMenu implements BiConsumer<TextIO, RunnerData> {
	
	private static Logger logger = Logger.getLogger(ExitMenu.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new ExitMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
    	SwingTextTerminal terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        boolean toSave = textIO.newBooleanInputReader()
                .read("Exit now");
        
        if (toSave) {
            terminal.printf("Exiting the Simulation..." + System.lineSeparator());
            Simulation sim = Simulation.instance();
        	sim.endSimulation(); 
    		sim.getSimExecutor().shutdownNow();
    		sim.getMasterClock().exitProgram();
    		logger.info("Exiting the Simulation.");
    		InteractiveTerm.setKeepRunning(false);
//    		InteractiveTerm.disposeTerminal();
//    		InteractiveTerm.delay(500L);
			System.exit(0);
			
			// TODO: how to detect if JavaFX mode is used and call MainScene.exit()
        }
        else
            terminal.printf("You don't want to exit the Simulation." + System.lineSeparator());
        	
    }
    
    @Override
    public String toString() {
        return "Exit the Simulation" + System.lineSeparator();
    }
}
