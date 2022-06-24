/**
 * Mars Simulation Project
 * StatusCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.structure.Settlement;

/** 
 * 
 */
public class StatusCommand extends AbstractPersonCommand {
	public static final ChatCommand STATUS = new StatusCommand();
	
	private StatusCommand() {
		super("sts", "status", "Status report");
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
		PhysicalCondition condition = person.getPhysicalCondition();
		if (person.isDeclaredDead()) {
			DeathInfo death = condition.getDeathDetails();
			buffer.append("I died on ");
			buffer.append(death.getTimeOfDeath());
			buffer.append(" doing ");
			buffer.append(death.getTask());
			buffer.append(". Cause was ");
			buffer.append(death.getCause());
		}
		else {
			buffer.append("At the moment, I am ");
			buffer.append(person.getTaskDescription());
			buffer.append(System.lineSeparator());
			buffer.append("I am ");

			double p = condition.getPerformanceFactor();
			double h = condition.getHunger();
			double e = condition.getHunger();
			double t = condition.getThirst();
			double s = condition.getStress();
			double f = condition.getFatigue();

			buffer.append(PhysicalCondition.getPerformanceStatus(p));
			buffer.append(" in performance, ");
			buffer.append(PhysicalCondition.getHungerStatus(h, e)).append(", ");
			buffer.append(PhysicalCondition.getThirstyStatus(t)).append(", ");
			buffer.append(PhysicalCondition.getStressStatus(s)).append(" and ");
			buffer.append(PhysicalCondition.getFatigueStatus(f)).append(".");
			buffer.append(System.lineSeparator());
		}
		return buffer.toString();
	}

}
