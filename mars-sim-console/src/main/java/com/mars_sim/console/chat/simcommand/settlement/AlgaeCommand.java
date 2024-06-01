/*
 * Mars Simulation Project
 * AlgaeCommand.java
 * @date 2023-11-22
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.AlgaeFarming;

/**
 * Command to display the algae farming status.
 */
public class AlgaeCommand extends AbstractSettlementCommand {

	public static final ChatCommand ALGAE = new AlgaeCommand();

	private AlgaeCommand() {
		super("al", "algae", "Status of algae farming");
	}

	/** 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, "Size m3",
									"Mass of Algae", 6,
									"Surplus Ratio", "Nutrient Demand",  "Inspection", "Cleanliness");

		List<AlgaeFarming> farms = settlement.getBuildingManager().getBuildings(FunctionType.ALGAE_FARMING)
				 					.stream().map(Building::getAlgae)
									.collect(Collectors.toList());
		// Display each farm separately
		for (AlgaeFarming farm : farms) {			
			var housekeeping = farm.getHousekeeping();
			response.appendTableRow(farm.getBuilding().getName(),
									farm.getTankSize(),
									farm.getCurrentAlgae(),
									farm.getSurplusRatio(),
									farm.getNutrientDemand(),
									housekeeping.getAverageInspectionScore(),
									housekeeping.getAverageInspectionScore());
		}
		context.println(response.getOutput());
		return true;
	}
}
