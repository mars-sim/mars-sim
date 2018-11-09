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
public class TimeRatioMenu implements BiConsumer<TextIO, RunnerData> {
	   
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

        Speed s = new Speed();
        SwingHandler handler = new SwingHandler(textIO, s);
        
        int currentSpeed = getCurrentSpeed();
        terminal.println("The current simulation speed is " + currentSpeed 
        		+ System.lineSeparator());
        
        terminal.println("----------------------------------------------------------------");
        terminal.println("|   Press UP/DOWN arrow keys to scroll through choices.        |");
        terminal.println("----------------------------------------------------------------"
        		+ System.lineSeparator());

        handler.addIntTask("speed", "Enter the new simulation speed", false)
        	.withInputReaderConfigurator(r -> r.withMinVal(0).withMaxVal(13))
        	.addChoices(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
        
        handler.executeOneTask();

        int speedInt = Speed.speed;
        
		if (speedInt >= 0 && speedInt <= 14) {
        	double ratio = Math.pow(2, speedInt);
        	Simulation.instance().getMasterClock().setTimeRatio(ratio);   
            terminal.printf(System.lineSeparator() 
            		+ "The new simulation speed becomes %d"//  -->  New Time-Ratio = 2^speed = %dx" 
            		+ System.lineSeparator(),
            		speedInt);
            		//, (int)ratio);
		}
        else
            terminal.printf(
            		"Invalid value." 
            		+ System.lineSeparator() 
            		+  "Please choose a number between 0 and 14." 
                		+ System.lineSeparator());
  
    }

    /**
     * Gets the simulation speed
     * 
     * @return
     */
    public int getCurrentSpeed() {
    	int speed = 0;
    	int tr = (int)Simulation.instance().getMasterClock().getTimeRatio();	
        int base = 2;

        while (tr != 1) {
            tr = tr/base;
            --speed;
        }
        
    	return -speed;
    }

    public boolean isInteger(String string) {
        try {
            Integer.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

//    public void setChoices(String... choices) {
//        this.originalInput = "";
//        this.choiceIndex = -1;
//        this.choices = choices;
//    }
    
//    public void setUpArrows() {
//        terminal.registerHandler(KEY_STROKE_UP, t -> {
//            if(choiceIndex < 0) {
//                originalInput = terminal.getPartialInput();
//            }
//            if(choiceIndex < choices.length - 1) {
//                choiceIndex++;
//                t.replaceInput(choices[choiceIndex], false);
//            }
//            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
//        });
//
//        terminal.registerHandler(KEY_STROKE_DOWN, t -> {
//            if(choiceIndex >= 0) {
//                choiceIndex--;
//                String text = (choiceIndex < 0) ? originalInput : choices[choiceIndex];
//                t.replaceInput(text, false);
//            }
//            return new ReadHandlerData(ReadInterruptionStrategy.Action.CONTINUE);
//        });
//    }
    	
    
    @Override
    public String toString() {
        return "Change the Simulation Speed";
    }
    
    private static class Speed {
        public static int speed;

        @Override
        public String toString() {
            return "\n\tThe new Simulation Speed : " + speed;
        }
    }
    
}
