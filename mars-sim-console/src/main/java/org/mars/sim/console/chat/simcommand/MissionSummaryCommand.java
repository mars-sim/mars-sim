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

/**
 * Command to display mission stats
 * This is a singleton.
 */
public class MissionSummaryCommand extends ChatCommand {

	public static final ChatCommand MISSION_SUMMARY = new MissionSummaryCommand();

	private MissionSummaryCommand() {
		super(TopLevel.SIMULATION_GROUP, "ms", "mission summary", "Summary of all missionss");
	}

	@Override
	public boolean execute(Conversation context, String input) {

		MissionManager mgr = context.getSim().getMissionManager();
		
		StructuredResponse response = new StructuredResponse();
		response.appendTableHeading("Name",  20, "Type", 19, 
									"Phase", 18,
									"Settlement", CommandHelper.PERSON_WIDTH);
		for(Mission m : mgr.getMissions()) {
			response.appendTableRow(m.getTypeID(),
					                m.getMissionType().getName(),
									m.getPhase().getName(),
									m.getAssociatedSettlement());
		}

		context.println(response.getOutput());
		return true;
	}
}
