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

	private static final int MAX_RATE = 2048;

	public SpeedCommand() {
		super(TopLevel.SIMULATION_GROUP, "sp", "speed",
					"Simulation speed; optional argument of new speed");
		addRequiredRole(ConversationRole.ADMIN);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		MasterClock clock = context.getSim().getMasterClock();
        double desiredRatio = clock.getDesiredTR();
        double currentRatio = clock.getActualTR();
        

        context.println("The target simulation ratio is x" + desiredRatio);
        context.println("The actual simualtion ratio achieved is x" + currentRatio);
        
		if (input != null) {
			boolean failed = false;
			try {
				int newRate = Integer.parseInt(input);
				if ((1 <= newRate) && (newRate <= MAX_RATE)) {
					clock.setDesiredTR(newRate);
					context.println("New desired ratio is x" + newRate);
				}
				else {
					failed = true;
				}
			}
			catch (NumberFormatException nfe) {
				failed = true;
			}
			
			if (failed) {
        		context.println("Invalid input. Must be an integer between 1 & " + MAX_RATE);
        		return false;
			}
        }

        return true;
	}
}
