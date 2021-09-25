/**
 * Mars Simulation Project
 * MalfunctionCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Command to create a malfunction in a Settlement.
 */
public class MalfunctionCommand extends AbstractSettlementCommand {

	public static final MalfunctionCommand MALFUNCTION = new MalfunctionCommand();

	/**
	 * Create a command that is assigned to a command group
	 * @param group
	 */
	private MalfunctionCommand() {
		super("ml", "malfunction", "Display any malfunctions in the buildings");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {

		StructuredResponse response = new StructuredResponse();
		
		for(Building building : settlement.getBuildingManager().getBuildings()) {
			for(Malfunction m : building.getMalfunctionManager().getMalfunctions()) {
				response.appendHeading(m.getName());
				response.appendLabeledString("Building", building.getNickName());
				CommandHelper.outputMalfunction(response, m);
				response.appendBlankLine();
			}
		}

		context.println(response.getOutput());
		return true;
	}
}
