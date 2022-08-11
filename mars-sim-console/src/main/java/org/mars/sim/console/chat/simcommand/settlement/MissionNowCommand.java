/**
 * Mars Simulation Project
 * MissionNowCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;

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