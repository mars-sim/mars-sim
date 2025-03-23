/*
 * Mars Simulation Project
 * BedCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LivingAccommodation;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display bed allocation in a Settlement
 * This is a singleton.
 */
public class BedCommand extends AbstractSettlementCommand {

	public static final ChatCommand BED = new BedCommand();

	private BedCommand() {
		super("b", "bed", "Allocation of beds");
	}

	/** 
	 * Outputs the current immediate location of the Unit.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		UnitManager um = context.getSim().getUnitManager();
		
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH,
							 "Bed", 7, "Person");
		List<Building> bs = settlement.getBuildingManager().getBuildings(FunctionType.LIVING_ACCOMMODATION);
		for (Building building : bs) {
			LivingAccommodation living = building.getLivingAccommodation();
			for(var as : living.getActivitySpots()) {
				String name = "";
				if (as.getID() >= 0) {
					name = um.getPersonByID(as.getID()).getName();
				}
				response.appendTableRow(building.getName(), as.getName(), name);
			}
		}

		context.println(response.getOutput());
		
		return true;
	}

}
