/**
 * Mars Simulation Project
 * WasteProcessCommand.java
 * @date 2022-06-15
 * @author Manny Kung
 */
package com.mars_sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.time.MarsTime;

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

		MarsTime clock = context.getSim().getMasterClock().getMarsTime();
		int missionMilliSol = (clock.getMissionSol() * 1000) + clock.getMillisolInt();
		
		// Loop buildings that do processing
		List<Building> processors = new ArrayList<>(
			settlement.getBuildingManager().getBuildings(FunctionType.WASTE_PROCESSING));
		Collections.sort(processors);

		for(Building building : processors) {
			CommandHelper.outputProcesses(response, "Waste process", missionMilliSol, building.getName(),
											building.getWasteProcessing());
		}

		context.println(response.getOutput());
		return true;
	}

}
