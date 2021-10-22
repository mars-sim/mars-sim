/**
 * Mars Simulation Project
 * MissionSummaryCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
									"Phase", 18,
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
				response.appendTableRow(m.getTypeID(),
										m.getPhase().getName(),
										vName,
										m.getAssociatedSettlement());
			}
		}

		context.println(response.getOutput());
		return true;
	}
}
