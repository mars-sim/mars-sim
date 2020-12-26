package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class EvaCommand extends AbstractPersonCommand {
	public static final ChatCommand EVA = new EvaCommand();
	
	private EvaCommand() {
		super("e", "eva", "EVA time");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
