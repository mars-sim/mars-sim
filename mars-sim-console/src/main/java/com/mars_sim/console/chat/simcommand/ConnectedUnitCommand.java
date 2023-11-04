/*
 * Mars Simulation Project
 * ConnectedUnitCommand.java
 * @date 2022-07-15
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.command.InteractiveChatCommand;
import com.mars_sim.core.Unit;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

/**
 * This class is used to handle the outcome of a Connect command. It repesents a connection with a Unit. 
 */
public abstract class ConnectedUnitCommand extends InteractiveChatCommand {

	private Unit unit;
	private String unitName;

	protected ConnectedUnitCommand(Unit unit, List<ChatCommand> commands, InteractiveChatCommand parent) {
		super(null, null, null, null, unit.getName(), commands);

		this.unit = unit;
		this.unitName = unit.getName();

		// Add the Command commands from the parent
		if (parent != null) {
			addSubCommands(parent.getSubCommands());
		}
		// Add in the standard commands to reconnect and leave Unit
		addSubCommand(ByeCommand.BYE);

		setIntroduction("*** Connection established with " + unit.getName() + " ***");
	}

	/**
	 * Gets the Unit with the connection.
	 * 
	 * @return
	 */
	public Unit getUnit() {
		return unit;
	}

	@Override
	public String getPrompt(Conversation context) {
		StringBuilder prompt = new StringBuilder();
		MasterClock clock = context.getSim().getMasterClock();
		MarsTime marsClock = clock.getMarsTime();
		prompt.append("[" + clock.getMarsTime().getTruncatedDateTimeStamp());
		prompt.append(" Sol " + marsClock.getMissionSol());
		prompt.append("] ").append(unitName);
		return prompt.toString();
	}

}
