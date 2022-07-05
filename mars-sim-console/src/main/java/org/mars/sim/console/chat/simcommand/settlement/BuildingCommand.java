/**
 * Mars Simulation Project
 * BuildingCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

public class BuildingCommand extends AbstractSettlementCommand {

	public static final ChatCommand BUILDING = new BuildingCommand();
	
	private BuildingCommand() {
		super("bu", "building", "Status of all building");

	}

	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		BuildingManager bm = settlement.getBuildingManager();
		List<Building> i = bm.getBuildings();
		
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, "Category", 12, "Power", 10, "Demand (kwh)", "Heat %",
									"Temp.", 6, "People");
		for (Building building : i) {
			response.appendTableRow(building.getName(), building.getCategory().getName(), building.getPowerMode().getName(),
									building.getFullPowerRequired(),
									building.getHeatMode().getPercentage(), building.getCurrentTemperature(),
									building.getNumPeople());
		}

		context.println(response.getOutput());
		return true;
	}
}
