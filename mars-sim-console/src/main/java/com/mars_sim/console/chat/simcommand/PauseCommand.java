/*
 * Mars Simulation Project
 * PauseCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.time.MasterClock;

public class PauseCommand extends ChatCommand {

	public PauseCommand() {
		super(TopLevel.SIMULATION_GROUP, "p", "pause", "Pause the simulation");
		setInteractive(true);
		addRequiredRole(ConversationRole.ADMIN);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		MasterClock clock = context.getSim().getMasterClock();
		if (clock.isPaused()) {
			context.println("Restarting the simulation...");
			clock.setPaused(false, false);
			context.println("Restarted");			
		}
		else {
			context.println("Pausing the simulation...");
			clock.setPaused(true, false);
			context.println("Paused");
		}
		return true;
	}
}
