package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class FeelingCommand extends AbstractPersonCommand {
	public static final ChatCommand FEELING = new FeelingCommand();
	
	private FeelingCommand() {
		super("f", "feeling", "About my feelings");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
