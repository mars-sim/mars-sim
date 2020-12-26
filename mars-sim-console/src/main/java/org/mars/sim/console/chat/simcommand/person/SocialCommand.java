package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class SocialCommand extends AbstractPersonCommand {
	public static final ChatCommand SOCIAL = new SocialCommand();
	
	private SocialCommand() {
		super("so", "social", "About my social circle");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
