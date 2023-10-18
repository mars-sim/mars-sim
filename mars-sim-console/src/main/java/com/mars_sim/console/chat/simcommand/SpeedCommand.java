/*
 * Mars Simulation Project
 * SpeedCommand.java
 * @date 2023-09-09
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.time.MasterClock;

public class SpeedCommand extends ChatCommand {

	private static final int MAX_RATE = (int)MasterClock.MAX_TIME_RATIO;

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
        
        context.println("The current 'desired' time ratio is " + desiredRatio + "x");
        context.println("The 'actual' time ratio achieved is " + currentRatio + "x");
        context.println("Input your 'desired' time ratio : ");
        
		if (input != null) {
			boolean failed = false;
			try {
				int newRate = Integer.parseInt(input);
				if ((1 <= newRate) && (newRate <= MAX_RATE)) {
					clock.setDesiredTR(newRate);
					context.println("The new 'desired' time ratio is " + newRate + "x");
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
