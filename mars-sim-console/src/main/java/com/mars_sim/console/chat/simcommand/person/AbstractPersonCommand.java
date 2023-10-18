/*
 * Mars Simulation Project
 * AbstractPersonCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.person.Person;

public abstract class AbstractPersonCommand extends ChatCommand {

	protected AbstractPersonCommand(String shortCommand, String longCommand, String desc) {
		super(PersonChat.PERSON_GROUP, shortCommand, longCommand, desc);
	}
	
	@Override
	public boolean execute(Conversation context, String input) {
		PersonChat parent = (PersonChat) context.getCurrentCommand();
		Person person = parent.getPerson();
		
		return execute(context, input, person);
	}

	protected abstract boolean execute(Conversation context, String input, Person person);
}
