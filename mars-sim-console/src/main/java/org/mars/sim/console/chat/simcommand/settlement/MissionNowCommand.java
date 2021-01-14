package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;

public class MissionNowCommand extends AbstractSettlementCommand {
	public static final ChatCommand MISSION_NOW = new MissionNowCommand();
	

	private MissionNowCommand() {
		super("mn", "mission now", "Settlement Missions");		
	}
	
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
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
			int i = 1;
			for (Mission mission : missions) {
				response.appendHeading(" (" + i++ + ") " + mission.getName());
				
				CommandHelper.outputMissionDetails(response, mission);
				response.append(System.lineSeparator());
			}
		}
		context.println(response.getOutput());
		
		return true;
	}
}