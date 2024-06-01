/*
 * Mars Simulation Project
 * FishCommand.java
 * @date 2023-11-22
 * @author Barry Evans
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
import com.mars_sim.core.structure.building.function.farming.Fishery;

/**
 * Command to display the fishery status.
 */
public class FishCommand extends AbstractSettlementCommand {

	public static final ChatCommand FISH = new FishCommand();

	private FishCommand() {
		super("fi", "fish", "Status of fish stocks");
	}

	/** 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, "Size m3",
									"Fish #", 6,
									"Surplus", "Weeds",  "Inspection", "Cleanliness");

		List<Fishery> tanks = settlement.getBuildingManager().getBuildings(FunctionType.FISHERY)
				 					.stream().map(Building::getFishery)
									.collect(Collectors.toList());
		// Display each farm separately
		for (Fishery tank : tanks) {			
			var keeping = tank.getHousekeeping();
			response.appendTableRow(tank.getBuilding().getName(),
									tank.getTankSize(),
									tank.getNumFish(),
									tank.getSurplusStock(),
									tank.getWeedDemand(),
									keeping.getAverageInspectionScore(),
									keeping.getAverageCleaningScore());
		}
		context.println(response.getOutput());
		return true;
	}
}
