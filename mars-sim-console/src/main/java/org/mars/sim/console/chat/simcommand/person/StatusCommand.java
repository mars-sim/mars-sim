/*
 * Mars Simulation Project
 * StatusCommand.java
 * @date 2023-06-14
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
		buffer.append(System.lineSeparator());
		buffer.append("Hi, My name is ");
		buffer.append(person.getName());
		buffer.append(" and I am a ");
		buffer.append(person.getMind().getJob().getName().toLowerCase()).append(".");
		buffer.append(System.lineSeparator());
		
		Settlement home = person.getAssociatedSettlement();
		if (home != null) {
			buffer.append("My home base is ");
			buffer.append(person.getAssociatedSettlement().getName()).append(",");
			buffer.append(System.lineSeparator());
			buffer.append("where I am a/an ");
			buffer.append(person.getRole().getType().getName()).append(".");
		}
		buffer.append(System.lineSeparator());
		
		PhysicalCondition condition = person.getPhysicalCondition();
		if (person.isDeclaredDead()) {
			DeathInfo death = condition.getDeathDetails();
			buffer.append("I died on ");
			buffer.append(death.getTimeOfDeath());
			buffer.append(" doing ");
			buffer.append(death.getTask());
			buffer.append(". Cause : ");
			buffer.append(death.getCause());
		}
		else {
			buffer.append("At the moment, I am ");
			buffer.append(person.getTaskDescription().toLowerCase()).append(".");

			double p = condition.getPerformanceFactor();
			double h = condition.getHunger();
			double e = condition.getHunger();
			double t = condition.getThirst();
			double s = condition.getStress();
			double f = condition.getFatigue();

			buffer.append(System.lineSeparator());
			buffer.append(System.lineSeparator());
			buffer.append("Hunger: ").append(PhysicalCondition.getHungerStatus(h, e));
			buffer.append(System.lineSeparator());
			buffer.append("Thirst: ").append(PhysicalCondition.getThirstyStatus(t));
			buffer.append(System.lineSeparator());
			buffer.append("Stress: ").append(PhysicalCondition.getStressStatus(s));
			buffer.append(System.lineSeparator());
			buffer.append("Fatigue: ").append(PhysicalCondition.getFatigueStatus(f));
			buffer.append(System.lineSeparator());
			buffer.append(System.lineSeparator());
			buffer.append("I am ");
			buffer.append(PhysicalCondition.getPerformanceStatus(p).toLowerCase());
			buffer.append(" in performance.");
			buffer.append(System.lineSeparator());
		}
		return buffer.toString();
	}

}
