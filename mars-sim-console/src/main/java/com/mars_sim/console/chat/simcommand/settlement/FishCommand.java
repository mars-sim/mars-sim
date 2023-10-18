/**
 * Mars Simulation Project
 * FishCommand.java
 * @version 3.1.2 2020-12-30
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
 * Command to display settlement crop
 * This is a singleton.
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
									"Surplus", "Weeds",  "To Inspect", "To Clean");

		List<Fishery> tanks = settlement.getBuildingManager().getBuildings(FunctionType.FISHERY)
				 					.stream().map(Building::getFishery)
									.collect(Collectors.toList());
		// Display each farm seperately
		for (Fishery tank : tanks) {			

			response.appendTableRow(tank.getBuilding().getName(),
									tank.getTankSize(),
									tank.getNumFish(),
									tank.getSurplusStock(),
									tank.getWeedDemand(),
									tank.getUninspected().size(),
									tank.getUncleaned().size());
		}
		context.println(response.getOutput());
		return true;
	}
}
