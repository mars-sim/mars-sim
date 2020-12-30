package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars.sim.console.chat.simcommand.settlement.MissionNowCommand;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;

/** 
 * 
 */
public class MissionCommand extends AbstractPersonCommand {
	public static final ChatCommand MISSION = new MissionCommand();
	
	private MissionCommand() {
		super("m", "mission", "About my missions");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		Mission mission = person.getMind().getMission();
		if (mission != null) {
			StructuredResponse response = new StructuredResponse();
			response.appendHeading(mission.getName());
			MissionNowCommand.outputMissionDetails(response, mission);
			
			context.println(response.getOutput());
		}
		else {
			context.println("Not implemented");
		}
		return true;
	}

}
