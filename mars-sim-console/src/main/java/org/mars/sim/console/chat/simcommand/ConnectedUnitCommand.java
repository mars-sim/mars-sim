package org.mars.sim.console.chat.simcommand;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.command.InteractiveChatCommand;

/**
 * This class is used to handle the outcome of a Connect command. It repesents a connection with a Unit. 
 */
public abstract class ConnectedUnitCommand extends InteractiveChatCommand {

	protected ConnectedUnitCommand(String description, String unitName,
			List<ChatCommand> commands) {
		super(null, null, null, description, unitName, "Connected to " + unitName, commands);

		// Add in the standard commands to reconnect and leave Unit
		addSubCommand(ConnectCommand.CONNECT);
		addSubCommand(ByeCommand.BYE);
		addSubCommand(DateCommand.DATE);
		// add location, date
	}

}
