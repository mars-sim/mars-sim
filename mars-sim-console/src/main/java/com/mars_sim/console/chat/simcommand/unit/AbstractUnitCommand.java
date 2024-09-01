/*
 * Mars Simulation Project
 * AbstractUnitCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.ConnectedUnitCommand;
import com.mars_sim.core.Unit;

public abstract class AbstractUnitCommand extends ChatCommand {

	protected AbstractUnitCommand(String group, String shortCommand, String longCommand, String desc) {
		super(group, shortCommand, longCommand, desc);
	}
	
	@Override
	public boolean execute(Conversation context, String input) {
		ConnectedUnitCommand parent = (ConnectedUnitCommand) context.getCurrentCommand();
		Unit target = parent.getUnit();
		
		return execute(context, input, target);
	}

	protected abstract boolean execute(Conversation context,  String input, Unit source);
}
