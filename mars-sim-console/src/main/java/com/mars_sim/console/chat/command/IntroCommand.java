/*
 * Mars Simulation Project
 * IntroCommand.java
 * @date 2022-06-20
 * @author Barry Evans
 */

package com.mars_sim.console.chat.command;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;

/**
 * Generic command to repeat the Introduction message of the current command.
 */
public class IntroCommand extends ChatCommand {

	public IntroCommand() {
		super(COMMAND_GROUP, "-", "intro", "Repeat the introduction of this command");
	}

	@Override
	public boolean execute(Conversation context, String input) {
		InteractiveChatCommand current = context.getCurrentCommand();
		context.println(current.getIntroduction());
		
		return true;
	}
}
