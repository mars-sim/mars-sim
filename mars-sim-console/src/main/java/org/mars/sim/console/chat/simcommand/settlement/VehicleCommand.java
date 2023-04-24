/*
 * Mars Simulation Project
 * VehicleCommand.java
 * @date 2022-06-27
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
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

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

		var missionMgr = context.getSim().getMissionManager();
		for (Vehicle v : vlist) {
			String vTypeStr = v.getSpecName();	

			// Print mission name
			String missionName = "";
			Mission mission = missionMgr.getMissionForVehicle(v);
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
