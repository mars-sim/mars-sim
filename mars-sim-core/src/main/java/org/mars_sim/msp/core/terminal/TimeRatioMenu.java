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
import org.beryx.textio.swing.SwingTextTerminal;
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
    	SwingTextTerminal terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

//        double oldRatio = Simulation.instance().getMasterClock().getTimeRatio();
//        int oldSpeed = (int) Math.sqrt(oldRatio);        
        
        int speed = textIO.newIntInputReader()
                .withMinVal(1).withMaxVal(14)//(16384)
                .read("Speed [1 to 14]");

        terminal.printf("\n");

//        if (MathUtils.isPowerOf2(ratio) && ratio <= 16384) {      
        if (speed >0 && speed <= 14) {
        	double ratio = Math.pow(2, speed);
        	Simulation.instance().getMasterClock().setTimeRatio(ratio);   
            terminal.printf("New Speed : %d  ==>  New Time-Ratio : %dx\n", speed, (int)ratio);
        }
        else
            terminal.printf("Invalid value.\nPlease choose a number between 1 and 14.\n");

        	
 //       textIO.newStringInputReader().withMinLength(0).read("\nPress enter to return to the menu\n");

    }

    
    
    @Override
    public String toString() {
        return "Change the Simulation Speed";
//        		getClass().getSimpleName() + ": reading personal data.\n" +
//                "(Properties are initialized at start-up.\n" +
//                "Properties file: " + getClass().getSimpleName() + ".properties.)";
    }
}
