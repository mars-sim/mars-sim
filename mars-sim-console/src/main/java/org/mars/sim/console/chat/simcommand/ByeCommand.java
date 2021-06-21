/**
 * Mars Simulation Project
 * ByeCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;

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
