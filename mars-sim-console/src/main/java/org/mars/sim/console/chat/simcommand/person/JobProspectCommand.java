package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class JobProspectCommand extends AbstractPersonCommand {
	public static final ChatCommand JOB_PROSPECT = new JobProspectCommand();
	
	private JobProspectCommand() {
		super("jp", "job prospect", "About my job prospects");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
