package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class RoleProspectCommand extends AbstractPersonCommand {
	public static final ChatCommand ROLE_PROSPECT = new RoleProspectCommand();
	
	private RoleProspectCommand() {
		super("rp", "role prospect", "About my role prospects");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
