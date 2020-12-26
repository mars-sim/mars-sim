package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class StatusCommand extends AbstractPersonCommand {
	public static final ChatCommand STATUS = new StatusCommand();
	
	private StatusCommand() {
		super("st", "status", "Status report");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		context.println(status(person));
	}

	public static String status(Person person) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Hi, My name is ");
		buffer.append(person.getName());
		buffer.append(" and I am a ");
		buffer.append(person.getJobName());
		buffer.append(" based in ");
		buffer.append(person.getAssociatedSettlement().getName());
		buffer.append(" where I am the ");
		buffer.append(person.getRole().getType().getName());
		buffer.append(System.lineSeparator());
		
		buffer.append("At the moment I am ");
		buffer.append(person.getTaskDescription());
		buffer.append(System.lineSeparator());
		buffer.append("Status is ");
		buffer.append(person.getStatus());
		
		return buffer.toString();
	}

}
