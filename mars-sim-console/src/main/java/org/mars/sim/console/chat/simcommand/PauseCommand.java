package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.time.MasterClock;

public class PauseCommand extends ChatCommand {

	public PauseCommand() {
		super(TopLevel.SIMULATION_GROUP, "p", "pause", "Pause the simulation");
		setInteractive(true);
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
