package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.structure.Settlement;

/** 
 * 
 */
public class StatusCommand extends AbstractPersonCommand {
	public static final ChatCommand STATUS = new StatusCommand();
	
	private StatusCommand() {
		super("ss", "status", "Status report");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {

		context.println(getStatus(person));
		return true;
	}

	public static String getStatus(Person person) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Hi, My name is ");
		buffer.append(person.getName());
		buffer.append(" and I am a ");
		buffer.append(person.getMind().getJob().getName());
		
		Settlement home = person.getAssociatedSettlement();
		if (home != null) {
			buffer.append(" based in ");
			buffer.append(person.getAssociatedSettlement().getName());
			buffer.append(" where I am the ");
			buffer.append(person.getRole().getType().getName());
		}
		buffer.append(System.lineSeparator());

		if (person.isDeclaredDead()) {
			DeathInfo death = person.getPhysicalCondition().getDeathDetails();
			buffer.append("I died on ");
			buffer.append(death.getTimeOfDeath());
			buffer.append(" doing ");
			buffer.append(death.getTask());
			buffer.append(". Cause was ");
			buffer.append(death.getCause());
		}
		else {
			buffer.append("At the moment I am ");
			buffer.append(person.getTaskDescription());
			buffer.append(System.lineSeparator());
			buffer.append("Status is ");
			buffer.append(person.getStatus());
		}
		return buffer.toString();
	}

}
