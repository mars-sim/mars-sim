package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.structure.Settlement;

public class ResourceCommand extends AbstractSettlementCommand {
	public static final ChatCommand RESOURCE = new ResourceCommand();

	private ResourceCommand() {
		super("rs", "resource", "Settlement resources; either oxygen, co2 or water");
	}
	
	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {
		// TODO Auto-generated method stub

	}

}
