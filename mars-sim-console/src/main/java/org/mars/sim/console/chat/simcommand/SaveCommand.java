package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;

public class SaveCommand extends ChatCommand {

	public SaveCommand() {
		super(TopLevel.SIMULATION_GROUP, "s", "save", "Save the simulation");
	}

	@Override
	public void execute(Conversation context, String input) {
		context.println("Saving the simulation");
		// save it
		context.println("Done");
	}
}
