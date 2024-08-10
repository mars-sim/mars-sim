/*
 * Mars Simulation Project
 * MissionNowCommand.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.structure.Settlement;

public class MissionNowCommand extends AbstractSettlementCommand {
	public static final ChatCommand MISSION_NOW = new MissionNowCommand();
	

	private MissionNowCommand() {
		super("mn", "mission", "Settlement Missions {active|all}");		
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		boolean onlyActive = true;
		if (input != null) {
			onlyActive = "active".equalsIgnoreCase(input);
		}
		
		List<Mission> missions;
		if (onlyActive) {
			// By default this only return uncompleted Missions
			missions = context.getSim().getMissionManager().getMissionsForSettlement(settlement);
		}
		else {
			// This is all missions
			missions = context.getSim().getMissionManager().getMissions().stream()
								.filter(m -> settlement.equals(m.getAssociatedSettlement()))
								.collect(Collectors.toList());
		}
			
		// Display what we have found	
		if (missions.isEmpty()) {
			response.append(settlement.getName() + " : ");
			response.appendText("no on-going/pending missions right now.");
		}
		else {
			response.append(settlement.getName() + " : ");
			response.appendText("here's the mission roster.");
			int i = 1;
			for (Mission mission : missions) {
				response.appendHeading(" (" + i++ + ") " + mission.getName());
				
				CommandHelper.outputMissionDetails(response, mission);
				response.appendBlankLine();
			}
		}
		context.println(response.getOutput());
		
		return true;
	}
}