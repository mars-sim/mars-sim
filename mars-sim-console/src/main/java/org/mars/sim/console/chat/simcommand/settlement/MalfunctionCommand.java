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
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, "Malfunction", CommandHelper.MALFUNCTION_WIDTH,
									"Severity", "%age fixed");
		
		for(Building building : settlement.getBuildingManager().getBuildings()) {
			for(Malfunction m : building.getMalfunctionManager().getMalfunctions()) {
				response.appendTableRow(building.getName(), m.getName(), m.getSeverity(), m.getPercentageFixed());
			}
		}

		context.println(response.getOutput());
		return true;
	}
}
