/**
 * Mars Simulation Project
 * MissionSummaryCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Command to display mission stats
 * This is a singleton.
 */
public class MissionSummaryCommand extends ChatCommand {

	public static final ChatCommand MISSION_SUMMARY = new MissionSummaryCommand();
	private static final String ACTIVE = "active";

	private MissionSummaryCommand() {
		super(TopLevel.SIMULATION_GROUP, "ms", "mission summary", "Summary of all missionss");
	}

	@Override
	public boolean execute(Conversation context, String input) {

		boolean showAll = ((input == null) || !input.equalsIgnoreCase(ACTIVE)); 
		MissionManager mgr = context.getSim().getMissionManager();
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Name",  24,
									"Phase", 20,
									"Vehicle", CommandHelper.PERSON_WIDTH,
									"Settlement", CommandHelper.PERSON_WIDTH);
		for(Mission m : mgr.getMissions()) {
			String vName = "";
			if ((m instanceof VehicleMission)) {
				VehicleMission vm = (VehicleMission)m;
				Vehicle v = vm.getVehicle();
				vName = (v != null ? v.getName() : "");
			}
			if (showAll || !m.isDone()) {
				response.appendTableRow(m.getName(),
										m.getPhaseDescription(),
										vName,
										m.getAssociatedSettlement());
			}
		}

		context.println(response.getOutput());
		return true;
	}
}
