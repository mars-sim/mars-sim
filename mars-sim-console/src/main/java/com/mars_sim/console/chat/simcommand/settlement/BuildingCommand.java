/**
 * Mars Simulation Project
 * BuildingCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;

public class BuildingCommand extends AbstractSettlementCommand {

	public static final ChatCommand BUILDING = new BuildingCommand();
	private static final String MAINT_FORMAT = "%.0f";
	
	private BuildingCommand() {
		super("bu", "building", "Status of all building");

	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		BuildingManager bm = settlement.getBuildingManager();
		List<Building> i = new ArrayList<>(bm.getBuildingSet());
		Collections.sort(i);
		
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, 
									"Category", 12, 
									"Power Mode", 10,
									"Power Load kWe", 
									"Heat Load kWt",
									"Deg C", 6, 
									"People", 
									"Maint.");
		for (Building building : i) {
			MalfunctionManager mm = building.getMalfunctionManager();

			response.appendTableRow(building.getName(), 
									building.getCategory().getName(), 
									building.getPowerMode().getName(),
									building.getFullPowerRequired(),
									building.getHeatRequired(), 
									building.getCurrentTemperature(),
									building.getNumPeople(),
									String.format(MAINT_FORMAT,
												(mm.getMaintenancePeriod() - mm.getEffectiveTimeSinceLastMaintenance())/1000D));
		}

		context.println(response.getOutput());
		return true;
	}
}
