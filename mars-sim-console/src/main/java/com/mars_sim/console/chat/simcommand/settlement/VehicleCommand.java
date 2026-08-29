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
	
		response.appendBlankLine();

		// Sort the vehicle list according to the name
		List<Vehicle> vlist = new ArrayList<>(settlement.getAllAssociatedVehicles());
		
		int vlistSize = vlist.size();
		
		Collections.sort(vlist);
		
		response.appendText(settlement.getName() + " owns the following " + vlistSize + " vehicles: ");

		response.appendDoubleBlankLine();
		
		printVehicleList(settlement, vlist, response);
		
		response.appendDoubleBlankLine();

		response.appendText("Now let's compare between the associated vehicle list and the vicinity vehicle list. ");
		
		response.appendDoubleBlankLine();
	
		List<Vehicle> vicinityList = new ArrayList<>(settlement.getParkedNGaragedVehicles());
		int vicinityListSize = vicinityList.size();
		Collections.sort(vicinityList);
		
		response.appendText(settlement.getName() + " has the following " + vicinityListSize + " vehicles parked or garaged in the vicinity: ");

		response.appendDoubleBlankLine();
		
		printVehicleList(settlement, vicinityList, response);
		
		response.appendDoubleBlankLine();

		boolean containsAll = vicinityList.stream()
			    .allMatch(vlist::contains);
		
		if (containsAll) {
			response.appendText("In summary, the associated list of owned vehicles (" + vlistSize 
					+ ") contains all the vehicles in the vicinity list (" + vicinityListSize + ").");
		}
		else {
			response.appendText("In summary, the associated list of owned vehicles (" + vlistSize 
					+ ") does not contain all the vehicles in the vicinity list (" + vicinityListSize + ") !");
		}

		response.appendBlankLine();
		
		context.println(response.getOutput());
		return true;
	}
	
	/**
	 * Prints the vehicle table.
	 * 
	 * @param settlement
	 * @param list
	 * @param response
	 */
	private void printVehicleList(Settlement settlement, List<Vehicle> list, StructuredResponse response) {
		response.appendTableHeading("Name", 16, "Type", 21, "Home", "Reserved", "Salvage",
				"Pri Stat", 8, "Maint Due", "Other Stats", 18, 
				 "Mission", 10);

		for (Vehicle v : list) {
		String vTypeStr = v.getVehicleType().getName();	
		
		// Print mission name
		String missionName = "";
		Mission mission = v.getMission();
		if (mission != null) {
		missionName = mission.getName();
		}
		
		MalfunctionManager mm = v.getMalfunctionManager();
		boolean needMaintenance = mm.getEffectiveTimeSinceLastMaintenance() > mm.getStandardInspectionWindow();
		
		boolean isReserved = v.isReservedForMission();
		
		boolean isSalvage = v.isSalvaged();
		
		// Dropped Parked once fix problem
		boolean isHome = settlement.equals(v.getSettlement());
		response.appendTableRow(v.getName(), vTypeStr, isHome, isReserved, isSalvage,
		v.getPrimaryStatus().getName(), 
			 needMaintenance, v.printStatusTypes(), missionName);
		}
	}
	
}
