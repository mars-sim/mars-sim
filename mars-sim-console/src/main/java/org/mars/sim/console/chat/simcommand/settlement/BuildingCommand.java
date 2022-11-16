/**
 * Mars Simulation Project
 * BuildingCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

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
		List<Building> i = new ArrayList<>(bm.getBuildings());
		Collections.sort(i);
		
		response.appendTableHeading("Building", CommandHelper.BUILIDNG_WIDTH, "Category", 12, "Power", 10,
									"Dem. (kwh)", "Heat %",
									"Temp.", 6, "People", "Maint.");
		for (Building building : i) {
			MalfunctionManager mm = building.getMalfunctionManager();

			response.appendTableRow(building.getName(), building.getCategory().getName(), building.getPowerMode().getName(),
									building.getFullPowerRequired(),
									building.getHeatMode().getPercentage(), building.getCurrentTemperature(),
									building.getNumPeople(),
									String.format(MAINT_FORMAT, (mm.getEffectiveTimeSinceLastMaintenance()
																- mm.getMaintenancePeriod())));
		}

		context.println(response.getOutput());
		return true;
	}
}
