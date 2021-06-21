/**
 * Mars Simulation Project
 * AbstractSettlementCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This is a wrapper class to handle common code for Settlement commands.
 * The high number of Settlement commands justifies the class.
 */
public abstract class AbstractSettlementCommand extends ChatCommand {

	protected AbstractSettlementCommand(String shortCommand, String longCommand, String desc) {
		super(SettlementChat.SETTLEMENT_GROUP, shortCommand, longCommand, desc);
	}

	/**
	 * Execute this command. This will identify the target Settlement for the current ChatCommand in
	 * the Conversation.
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		SettlementChat parent = (SettlementChat) context.getCurrentCommand();		
		Settlement settlement = parent.getSettlement();
		
		return execute(context, input, settlement);
	}

	/**
	 * Execute the command for the target Settlement.
	 * @param context
	 * @param input
	 * @param settlement
	 * @return Did the command work
	 */
	protected abstract boolean execute(Conversation context, String input, Settlement settlement);
}
