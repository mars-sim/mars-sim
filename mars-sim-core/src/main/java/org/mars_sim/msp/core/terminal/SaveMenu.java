/**
 * Mars Simulation Project
 * TimeRatioMenu.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.terminal;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.terminal.AppUtil;
import org.mars_sim.msp.core.terminal.RunnerData;

import java.util.function.BiConsumer;

/**
 * A menu for choosing the time ratio in TextIO.
 */
public class SaveMenu implements BiConsumer<TextIO, RunnerData> {
	
//	private static final String KEY_STROKE_UP = "pressed UP";
//	private static final String KEY_STROKE_DOWN = "pressed DOWN";
//
//	private String originalInput = "";
//	private int choiceIndex = -1;
//	private String[] choices = {};
	    
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
   
        boolean toSave = textIO.newBooleanInputReader()//.withDefaultValue(true)
               .read("Save now");

        terminal.println(System.lineSeparator());
        
//       	terminal.println("Press UP/DOWN to show a list of possible values"
//       			+ System.lineSeparator());
//   
//		setChoices("y", "n");
//		
//		String input = textIO.newStringInputReader()//.withDefaultValue('n')
////				.withInlinePossibleValues("y", "n")
//			    .read("Do you want to be added as the commander of a settlement? [y/n]");	
//		
//		if (input.equals("y") || input.equals("Y")) {
//			terminal.print(
////					System.lineSeparator() +
////					"Press UP and DOWN to show a possible list of values (if available)" +
//					System.lineSeparator());
//			setChoices();
//			profile.accept(textIO, null);
//		}
//		
        if (toSave) {
            terminal.printf("Saving Simulation..." + System.lineSeparator());
        	Simulation.instance().getMasterClock().setSaveSim(Simulation.SAVE_DEFAULT, null); 
        }
        else {
            terminal.printf("You don't want to save the Simulation." + System.lineSeparator());
        }
            
    }

    
    
    @Override
    public String toString() {
        return "Save the Simulation";
//        		getClass().getSimpleName() + ": reading personal data.\n" +
//                "(Properties are initialized at start-up.\n" +
//                "Properties file: " + getClass().getSimpleName() + ".properties.)";
    }
}
