package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class AirlockCommand extends AbstractPersonCommand {
	public static final ChatCommand AIRLOCK = new AirlockCommand();
	
	private AirlockCommand() {
		super("al", "airlock", "Airlock times");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println("Not implemented");
	}

}
