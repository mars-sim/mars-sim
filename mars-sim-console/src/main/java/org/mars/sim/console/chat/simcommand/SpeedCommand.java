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

	private static final int MAX_RATE = MasterClock.MAX_TIME_RATIO;

	public SpeedCommand() {
		super(TopLevel.SIMULATION_GROUP, "sp", "speed",
					"Simulation speed in terms of the time ratio");
		addRequiredRole(ConversationRole.ADMIN);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		MasterClock clock = context.getSim().getMasterClock();
        double desiredRatio = clock.getDesiredTR();
        double currentRatio = clock.getActualTR();
        
        context.println("The current target time ratio is " + desiredRatio + "x");
        context.println("The actual time ratio achieved is " + currentRatio + "x");
        context.println("Input your desired time ratio : ");
        
		if (input != null) {
			boolean failed = false;
			try {
				int newRate = Integer.parseInt(input);
				if ((1 <= newRate) && (newRate <= MAX_RATE)) {
					clock.setDesiredTR(newRate);
					context.println("The new time ratio is " + newRate+ "x");
				}
				else {
					failed = true;
				}
			}
			catch (NumberFormatException nfe) {
				failed = true;
			}
			
			if (failed) {
        		context.println("Invalid input. Must be an integer between 1 and " + MAX_RATE);
        		return false;
			}
        }

        return true;
	}
}
