/*
 * Mars Simulation Project
 * StatusCommand.java
 * @date 2023-06-14
 * @author Barry Evans
 */
package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.structure.Settlement;

/** 
 * The command showing the general status of a person
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
		
		if (person.isDeclaredDead()) {
			outputDead(buffer, person);
		}
		
		else {
			outputAlive(buffer, person);
		}

		PhysicalCondition condition = person.getPhysicalCondition();
		buffer.append(System.lineSeparator());
		buffer.append(System.lineSeparator());
		buffer.append("     Hunger : ").append(condition.getHungerLevel().getName()); 
		buffer.append(System.lineSeparator());
		buffer.append("     Energy : ").append(Math.round(condition.getEnergy() *10.0)/10.0 + " kJ");
		buffer.append(System.lineSeparator());
		buffer.append("     Thirst : ").append(condition.getThirstLevel().getName()); 
		buffer.append(System.lineSeparator());
		buffer.append("     Stress : ").append(condition.getStressLevel().getName());
		buffer.append(System.lineSeparator());
		buffer.append("    Fatigue : ").append(condition.getFatigueLevel().getName());
		buffer.append(System.lineSeparator());
		buffer.append("Performance : ").append(condition.getPerformanceLevel().getName());
		buffer.append(System.lineSeparator());
		buffer.append("    Emotion : ").append(person.getMind().getEmotion().getDescription());
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
	}

	private static void outputAlive(StringBuilder buffer, Person person) {
		buffer.append("Hi, my name is ");
		buffer.append(person.getName());
		buffer.append(" and my job is ");
		String job = person.getMind().getJobType().getName();
		buffer.append(job).append(".");
		buffer.append(System.lineSeparator());
		
		Settlement home = person.getAssociatedSettlement();
		if (home != null) {
			buffer.append("My home base is ");
			buffer.append(person.getAssociatedSettlement().getName()).append(",");
			buffer.append(System.lineSeparator());
			buffer.append("where my role is ");
			String role = person.getRole().getType().getName();
			buffer.append(role).append(".");
		}
		buffer.append(System.lineSeparator());		
		
		buffer.append("At this moment, I am ");
		buffer.append(person.getTaskDescription().toLowerCase()).append(".");
	}

	private static void outputDead(StringBuilder buffer, Person person) {
		buffer.append("It's been reported that ");
		buffer.append(person.getName());
		buffer.append(" has passed away. ");
		
		buffer.append("Their job was");
		
		String job = person.getMind().getJobType().getName();
		buffer.append(job).append(".");
		buffer.append(System.lineSeparator());
		
		Settlement home = person.getAssociatedSettlement();
		if (home != null) {
			buffer.append("Their home base was ");
			buffer.append(person.getAssociatedSettlement().getName()).append(", ");
			buffer.append("where their role was ");
			String role = person.getRole().getType().getName();
			buffer.append(role).append(". ");
		}
		buffer.append(System.lineSeparator());
		
		PhysicalCondition condition = person.getPhysicalCondition();
		DeathInfo death = condition.getDeathDetails();
		buffer.append("They died on ");
		buffer.append(death.getTimeOfDeath());
		buffer.append(" while ");
		buffer.append(death.getTask() + ".");
		buffer.append(System.lineSeparator());
		buffer.append("Cause of Death: ");
		buffer.append(death.getCause());
	}

}
