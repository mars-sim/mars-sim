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

/**
 * A menu for choosing the time ratio in TextIO.
 */
public class SaveMenu implements BiConsumer<TextIO, RunnerData> {
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new SaveMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        TextTerminal<?> terminal = textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        boolean toSave = textIO.newBooleanInputReader()//.withDefaultValue(true)
                .read("Save now");

        terminal.printf("\n");
        
        if (toSave) {
            terminal.printf("Saving Simulation...\n");
        	Simulation.instance().getMasterClock().setSaveSim(Simulation.SAVE_DEFAULT, null); 
        }
        else
            terminal.printf("You don't want to save the Simulation.\n");
        	
//        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to return to the menu\n");

    }

    
    
    @Override
    public String toString() {
        return "Save the Simulation";
//        		getClass().getSimpleName() + ": reading personal data.\n" +
//                "(Properties are initialized at start-up.\n" +
//                "Properties file: " + getClass().getSimpleName() + ".properties.)";
    }
}
