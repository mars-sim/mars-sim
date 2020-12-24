package org.mars.sim.console.chat.simcommand.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class MissionNowCommand extends AbstractSettlementCommand {
	public static final ChatCommand MISSION_NOW = new MissionNowCommand();
	

	private MissionNowCommand() {
		super("mn", "mission now", "Settlement Missions");		
	}
	
	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		List<Mission> missions = context.getSim().getMissionManager().getMissionsForSettlement(settlement);
		if (missions.isEmpty()) {
			response.append(settlement.getName() + " : ");
			response.append("no on-going/pending missions right now.");
			response.append(System.lineSeparator());
		}
		else {
			response.append(settlement.getName() + " : ");
			response.append("here's the mission roster.");
			response.append(System.lineSeparator());

			for (Mission mission : missions) {
				List<MissionMember> plist = new ArrayList<>(mission.getMembers());
				Person startingPerson = mission.getStartingMember();
				plist.remove(startingPerson);

				double dist = 0;
				double trav = 0;
				Vehicle v = null;
				int i = 1;
				
				// Ohhh instanceof ???
				if (mission instanceof VehicleMission) {
					v = ((VehicleMission) mission).getVehicle();
					dist = Math.round(((VehicleMission) mission).getProposedRouteTotalDistance() * 10.0) / 10.0;
					trav = Math.round(((VehicleMission) mission).getActualTotalDistanceTravelled() * 10.0) / 10.0;
				}

				response.appendHeading(" (" + i++ + ") " + mission.getName());

				if (v != null) {
					response.appendLabeledString("Vehicle", v.getName());
					response.appendLabeledString("Type", v.getVehicleType());
					response.appendLabeledString("Est. Dist.", dist + " km");
					response.appendLabeledString("Travelled", trav + " km");
				}
				response.appendLabeledString("Phase", mission.getPhaseDescription());
				response.appendLabeledString("Lead", startingPerson.getName());
				response.append("Members:");
				List<String> names = plist.stream().map(p -> p.getName()).sorted().collect(Collectors.toList());
				response.appendNumberedList(names);
				response.append(System.lineSeparator());
			}
		}
		context.println(response.getOutput());
	}
}
