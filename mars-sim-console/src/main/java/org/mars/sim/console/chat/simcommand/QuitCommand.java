package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;

public class QuitCommand extends ChatCommand {

	public QuitCommand() {
		super(TopLevel.SIMULATION_GROUP, "x", "exit", "Exit the simulation");
	}

	@Override
	public void execute(Conversation context, String input) {
		context.setCompleted();
		context.println("Closing conversation.");
	}

}
