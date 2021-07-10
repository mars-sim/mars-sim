/**
 * Mars Simulation Project
 * SpeedCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.time.MasterClock;

public class SpeedCommand extends ChatCommand {

	public SpeedCommand() {
		super(TopLevel.SIMULATION_GROUP, "sp", "speed", "Change the simulation speed");
		setInteractive(true);
		addRequiredRole(ConversationRole.ADMIN);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		MasterClock clock = context.getSim().getMasterClock();
        int currentRatio = clock.getTargetTR();
        int currentSpeed = (int)(Math.log(currentRatio) / Math.log(2));
        
        context.println("The target simulation ratio is x" + currentRatio + ", speed " + currentSpeed); 

		String change = context.getInput("Change (Y/N)?");
        
        if ("Y".equalsIgnoreCase(change)) {
            int newSpeed = context.getIntInput("Enter the new simulation speed [1..12]");
            if ((1 <= newSpeed) && (newSpeed <= MasterClock.MAX_SPEED)) {
            	int ratio = (int) Math.pow(2, newSpeed);
            	clock.setTargetTR(ratio);
            	context.println("New speed is " + newSpeed + ", ratio x" + ratio);
            }
        }
        return true;
	}     
}
