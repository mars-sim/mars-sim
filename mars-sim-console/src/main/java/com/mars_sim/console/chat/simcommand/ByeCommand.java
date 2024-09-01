/*
 * Mars Simulation Project
 * ByeCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;

/**
 * Command to stop speaking with an entity.
 * This is a singleton.
 */
public class ByeCommand extends ChatCommand {

	public static final ChatCommand BYE = new ByeCommand();

	private ByeCommand() {
		super(TopLevel.SIMULATION_GROUP, "#", "bye", "Stop talking to the Unit");
	}

	@Override
	public boolean execute(Conversation context, String input) {
		context.println("Bye");
		context.resetCommand();
		
		return true;
	}

}
