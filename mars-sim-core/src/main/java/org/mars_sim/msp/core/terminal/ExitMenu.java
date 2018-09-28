/**
 * Mars Simulation Project
 * TimeRatioMenu.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.terminal;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.terminal.AppUtil;
import org.mars_sim.msp.core.terminal.RunnerData;

import java.util.function.BiConsumer;
import java.util.logging.Logger;


/**
 * A menu for choosing the time ratio in TextIO.
 */
public class ExitMenu implements BiConsumer<TextIO, RunnerData> {
	
	private static Logger logger = Logger.getLogger(ExitMenu.class.getName());

    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new ExitMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        TextTerminal<?> terminal = textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        boolean toSave = textIO.newBooleanInputReader()//.withDefaultValue(true)
                .read("Exit now");

        terminal.printf("\n");
        
        if (toSave) {
            terminal.printf("Exiting the Simulation...\n");
        	Simulation.instance().endSimulation(); 
    		Simulation.instance().getSimExecutor().shutdownNow();
    		Simulation.instance().getMasterClock().exitProgram();
    		logger.info("Exiting the Simulation.");
			System.exit(0);
        }
        else
            terminal.printf("You don't want to exit the Simulation.\n");
        	
//        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to return to the menu\n");

    }

    
    
    @Override
    public String toString() {
        return "Exit the Simulation\n";
//        		getClass().getSimpleName() + ": reading personal data.\n" +
//                "(Properties are initialized at start-up.\n" +
//                "Properties file: " + getClass().getSimpleName() + ".properties.)";
    }
}
