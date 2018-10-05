/**
 * Mars Simulation Project
 * TimeRatioMenu.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.terminal;

import org.beryx.textio.ReadHandlerData;
import org.beryx.textio.ReadInterruptionStrategy;
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
	
	private static final String KEY_STROKE_UP = "pressed UP";
	private static final String KEY_STROKE_DOWN = "pressed DOWN";

	private String originalInput = "";
	private int choiceIndex = -1;
	private String[] choices = {};
	    
	private SwingTextTerminal terminal;
	
    public static void main(String[] args) {
        TextIO textIO = TextIoFactory.getTextIO();
        new TimeRatioMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
    	terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);

       	terminal.println("Press UP/DOWN to show a list of possible values"
       			+ System.lineSeparator());
       	
        setUpArrows();
        
        String[] nums = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
        
        setChoices(nums);
        
        double tr =  Simulation.instance().getMasterClock().getTimeRatio();
        tr = Math.log(tr)/Math.log(2);
        String trStr = "" + (int)tr;
 
        String speed = textIO.newStringInputReader()
        		.withDefaultValue(trStr)
//        		.withInlinePossibleValues(nums)
                //.withMinVal(1).withMaxVal(14)//(16384)
                .read("Speed (0 to 14)");

//        terminal.printf(System.lineSeparator());

//        if (MathUtils.isPowerOf2(ratio) && ratio <= 16384) {  
    	int speedInt = Integer.parseInt(speed);
        if (speedInt >= 0 && speedInt <= 14) {
        	double ratio = Math.pow(2, speedInt);
        	Simulation.instance().getMasterClock().setTimeRatio(ratio);   
            terminal.printf("New Speed = %d  -->  New Time-Ratio = 2 ^ speed = %dx" 
            		+ System.lineSeparator(),
            		speed, (int)ratio);
        }
        else
            terminal.printf(
            		"Invalid value." 
            		+ System.lineSeparator() 
            		+  "Please choose a number between 0 and 14." 
            		+ System.lineSeparator());


    }

    public void setChoices(String... choices) {
        this.originalInput = "";
        this.choiceIndex = -1;
        this.choices = choices;
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
    	
    
    @Override
    public String toString() {
        return "Change the Simulation Speed";
    }
}
