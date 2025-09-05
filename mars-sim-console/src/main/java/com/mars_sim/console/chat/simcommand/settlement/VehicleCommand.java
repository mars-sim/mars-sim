/*
 * Mars Simulation Project
 * VehicleCommand.java
 * @date 2025-07-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Command to display a list of vehicles. This is a singleton.
 */
public class VehicleCommand extends AbstractSettlementCommand {

	public static final ChatCommand VEHICLE = new VehicleCommand();

	private VehicleCommand() {
		super("v", "vehicles", "Vehicle list");
	}

	/** 
	 * Outputs the answer.
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
	
		response.appendHeading("A list of vehicles own by this settlement : ");
		
		// Sort the vehicle list according to the name
		List<Vehicle> vlist = new ArrayList<>(settlement.getAllAssociatedVehicles());
		Collections.sort(vlist);

		response.appendTableHeading("Name", 16, "Type", 14, "Home", "Reserved", 
									"Pri Stat", 8, "Other Stats", 11, 
									"Maint Due", "Mission", 25);

		for (Vehicle v : vlist) {
			String vTypeStr = v.getVehicleType().getName();	

			// Print mission name
			String missionName = "";
			Mission mission = v.getMission();
			if (mission != null) {
				missionName = mission.getName();
			}

			MalfunctionManager mm = v.getMalfunctionManager();
			boolean needMaintenance = mm.getEffectiveTimeSinceLastMaintenance() > mm.getStandardInspectionWindow();
			
			boolean isReserved = v.isReserved();
			
			// Dropped Parked once fix problem
			boolean isHome = settlement.equals(v.getSettlement());
			response.appendTableRow(v.getName(), vTypeStr, isHome, isReserved,
					v.getPrimaryStatus().getName(), v.printStatusTypes(), 
						 needMaintenance, missionName);
		}
		
		context.println(response.getOutput());
		return true;
	}
}
