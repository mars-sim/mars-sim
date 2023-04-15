/**
 * Mars Simulation Project
 * RadiationCommand.java
 * @date 14/04/23
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.structure.RadiationStatus;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to handle radiation in a Settlment
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
	 * Create a radiation event
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		boolean baseLineRad = "Y".equalsIgnoreCase(context.getInput("Create baseline Radiation: Y/N"));
		boolean gcrRad = "Y".equalsIgnoreCase(context.getInput("Create GCR Radiation: Y/N"));
		boolean sepRad = "Y".equalsIgnoreCase(context.getInput("Create SEP Radiation: Y/N"));

		if (baseLineRad || gcrRad || sepRad) {
			RadiationStatus newStatus = new RadiationStatus(baseLineRad, gcrRad, sepRad);
			settlement.setExposed(newStatus);
			context.println("Radiation event created");
		}
		return true;
	}
}
