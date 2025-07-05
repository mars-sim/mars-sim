/*
 * Mars Simulation Project
 * RadiationCommand.java
 * @date 2023-11-05/
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.structure.RadiationStatus;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to handle radiation in a Settlement
 * This is a singleton.
 */
public class RadiationCommand extends AbstractSettlementCommand {

	public static final ChatCommand RADIATION = new RadiationCommand();

	private RadiationCommand() {
		super("rc", "create radiation", "Create Radiation");

		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	/** 
	 * Creates a radiation event.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		boolean baseLineRad = context.getBooleanInput("Create baseline Radiation");
		boolean gcrRad = context.getBooleanInput("Create GCR Radiation");
		boolean sepRad = context.getBooleanInput("Create SEP Radiation");

		if (baseLineRad || gcrRad || sepRad) {
			RadiationStatus newStatus = new RadiationStatus(baseLineRad, gcrRad, sepRad);
			settlement.setExposed(newStatus);
			context.println("Radiation event(s) created.");
		}
		return true;
	}
}
