package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class MissionCommand extends AbstractPersonCommand {
	public static final ChatCommand MISSION = new MissionCommand();
	
	private MissionCommand() {
		super("m", "mission", "About my missions");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
