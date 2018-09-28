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
import org.mars_sim.msp.core.MathUtils;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.terminal.AppUtil;
import org.mars_sim.msp.core.terminal.RunnerData;

import java.util.function.BiConsumer;

/**
 * A menu for choosing the time ratio in TextIO.
 */
public class TimeRatioMenu implements BiConsumer<TextIO, RunnerData> {
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new TimeRatioMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
        TextTerminal<?> terminal = textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

        int ratio = textIO.newIntInputReader()
                .withMinVal(1).withMaxVal(8192)
                .read("Time Ratio");

        terminal.printf("\n");
        
        if (MathUtils.isPowerOf2(ratio) && ratio <= 8192) {
        	Simulation.instance().getMasterClock().setTimeRatio(ratio);   
            terminal.printf("The New Time-Ratio is %dx\n", ratio);
        }
        else
            terminal.printf("Invalid value.\nPlease choose a number that's a power of 2 and is between 2 and 8192\n");

        	
        textIO.newStringInputReader().withMinLength(0).read("\nPress enter to return to the menu\n");

    }

    
    
    @Override
    public String toString() {
        return "Change the Time Ratio";
//        		getClass().getSimpleName() + ": reading personal data.\n" +
//                "(Properties are initialized at start-up.\n" +
//                "Properties file: " + getClass().getSimpleName() + ".properties.)";
    }
}
