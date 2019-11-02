/**
 * Mars Simulation Project
 * SaveMenu.java
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
import org.mars_sim.msp.core.Simulation.SaveType;

import java.util.function.BiConsumer;

/**
 * A menu for choosing the time ratio in TextIO.
 */
public class SaveMenu implements BiConsumer<TextIO, RunnerData> {
	  
	private SwingTextTerminal terminal;
	
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new SaveMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
    	terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);
   
        boolean toSave = textIO.newBooleanInputReader()
               .read("Save now");

        if (toSave) {
            terminal.printf("Saving Simulation..." + System.lineSeparator());
        	Simulation.instance().getMasterClock().setSaveSim(SaveType.SAVE_DEFAULT, null); 
        }
        else {
            terminal.printf("You don't want to save the Simulation." + System.lineSeparator());
        }
            
    }

    
    
    @Override
    public String toString() {
        return "Save the Simulation";
    }
}
