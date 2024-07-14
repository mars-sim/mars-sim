/*
 * Mars Simulation Project
 * VehicleCommand.java
 * @date 2022-06-27
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
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Command to display vehicles
 * This is a singleton.
 */
public class VehicleCommand extends AbstractSettlementCommand {

	public static final ChatCommand VEHICLE = new VehicleCommand();

	private VehicleCommand() {
		super("v", "vehicles", "Vehicle list");
	}

	/** 
	 * Output the answer
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
	
		response.appendHeading("Vehicles");
		
		// Sort the vehicle list according to the name
		List<Vehicle> vlist = new ArrayList<>(settlement.getAllAssociatedVehicles());
		Collections.sort(vlist);

		response.appendTableHeading("Name", CommandHelper.PERSON_WIDTH, "Type", 21, 
									"Status", 7, "Home", "Maint Due", "Mission", 25);

		for (Vehicle v : vlist) {
			String vTypeStr = v.getName();	

			// Print mission name
			String missionName = "";
			Mission mission = v.getMission();
			if (mission != null) {
				missionName = mission.getName();
			}

			MalfunctionManager mm = v.getMalfunctionManager();
			boolean needMaintenance = mm.getTimeSinceLastMaintenance() > mm.getMaintenancePeriod();
			
			// Dropped Parked once fix problem
			boolean isHome = settlement.equals(v.getSettlement());
			response.appendTableRow(v.getName(), vTypeStr, v.getPrimaryStatus().getName(),
						isHome, needMaintenance, missionName);
		}
		
		context.println(response.getOutput());
		return true;
	}
}
