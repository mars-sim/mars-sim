/**
 * Mars Simulation Project
 * AbstractUnitCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars_sim.msp.core.Unit;

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
