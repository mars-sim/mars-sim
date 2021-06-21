/**
 * Mars Simulation Project
 * IntroCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.command;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;

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
