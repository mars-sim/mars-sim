/**
 * Mars Simulation Project
 * WasteProcessCommand.java
 * @date 2022-06-15
 * @author Manny Kung
 */
package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Shows the status of WasteProcessing in a Settlement
 *
 */
public class WasteProcessCommand extends AbstractSettlementCommand {

	public static final ChatCommand WASTE = new WasteProcessCommand();

	private WasteProcessCommand() {
		super("wp", "wastes", "Settlement Waste Processing");
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		MarsClock clock = context.getSim().getMasterClock().getMarsClock();
		int missionMilliSol = (clock.getMissionSol() * 1000) + clock.getMillisolInt();
		
		// Loop buildings that do processing
		for(Building building : settlement.getBuildingManager().getBuildings(FunctionType.WASTE_PROCESSING)) {
			CommandHelper.outputProcesses(response, "Waste process", missionMilliSol, building.getName(),
											building.getWasteProcessing());
		}

		context.println(response.getOutput());
		return true;
	}

}
