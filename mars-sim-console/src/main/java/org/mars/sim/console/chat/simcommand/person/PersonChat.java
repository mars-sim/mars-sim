package org.mars.sim.console.chat.simcommand.person;

import java.util.Arrays;
import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars_sim.msp.core.person.Person;

/**
 * A connect to a Person object
 */
public class PersonChat extends ConnectedUnitCommand {

	private static final List<ChatCommand> COMMANDS = Arrays.asList(new PersonHealthCommand(),
																    new PersonTrainingCommand());
	
	public static final String PERSON_GROUP = "Person";
	
	private Person person;
	
	public PersonChat(Person person) {
		super("Connected to " + person.getName(), person.getName(), COMMANDS);
		this.person = person;
	}

	public Person getPerson() {
		return person;
	}

	@Override
	public String toString() {
		return "PersonChat [person=" + person.getName() + "]";
	}
}
