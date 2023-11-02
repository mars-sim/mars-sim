/*
 * Mars Simulation Project
 * StatusCommand.java
 * @date 2023-06-14
 * @author Barry Evans
 */
package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.structure.Settlement;

/** 
 * 
 */
public class StatusCommand extends AbstractPersonCommand {
	public static final ChatCommand STATUS = new StatusCommand();
	
	static String vowels = "aeiou";
	
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
			buffer.append("It's reported that ");
			buffer.append(person.getName());
			buffer.append(" had passed away. ");
			
			String genderNoun0 = "He was ";	
			String genderNoun1 = "His ";
			String genderNoun2 = "He ";	
			
			GenderType gender = person.getGender();
			if (gender == GenderType.FEMALE) {
				genderNoun0 = "She was";
				genderNoun1 = "Her ";
				genderNoun2 = "She ";	
			}
			
			buffer.append(genderNoun0.toLowerCase());
			
			String job = person.getMind().getJob().getName().toLowerCase();
			if (vowels.indexOf(Character.toLowerCase(job.charAt(0))) != -1) {	
				buffer.append(" a ");
			}
			else {
				buffer.append(" an ");
			}
			buffer.append(job).append(".");
			buffer.append(System.lineSeparator());
			
			Settlement home = person.getAssociatedSettlement();
			if (home != null) {
				buffer.append(genderNoun1);
				buffer.append(" home base was ");
				buffer.append(person.getAssociatedSettlement().getName()).append(", ");
				buffer.append(System.lineSeparator());
				buffer.append("where ");
				buffer.append(genderNoun0.toLowerCase());
				String role = person.getRole().getType().getName().toLowerCase();
				if (vowels.indexOf(Character.toLowerCase(role.charAt(0))) == -1) {	
					buffer.append(" a ");
				}
				else {
					buffer.append(" an ");
				}
				buffer.append(role).append(". ");
			}
			buffer.append(System.lineSeparator());
			
			PhysicalCondition condition = person.getPhysicalCondition();
			DeathInfo death = condition.getDeathDetails();
			buffer.append(genderNoun2);
			buffer.append(" died on ");
			buffer.append(death.getTimeOfDeath());
			buffer.append(" while ");
			buffer.append(death.getTask());
			buffer.append(System.lineSeparator());
			buffer.append(". Cause of Death: ");
			buffer.append(death.getCause());
		}
		
		else {
			buffer.append("Hi, my name is ");
			buffer.append(person.getName());
			buffer.append(" and I am ");
			String job = person.getMind().getJob().getName().toLowerCase();
			if (vowels.indexOf(Character.toLowerCase(job.charAt(0))) == -1) {	
				buffer.append("a ");
			}
			else {
				buffer.append("an ");
			}
			buffer.append(job).append(".");
			buffer.append(System.lineSeparator());
			
			Settlement home = person.getAssociatedSettlement();
			if (home != null) {
				buffer.append("My home base is ");
				buffer.append(person.getAssociatedSettlement().getName()).append(",");
				buffer.append(System.lineSeparator());
				buffer.append("where I am ");
				String role = person.getRole().getType().getName().toLowerCase();
				if (vowels.indexOf(Character.toLowerCase(role.charAt(0))) == -1) {	
					buffer.append("a ");
				}
				else {
					buffer.append("an ");
				}
				buffer.append(role).append(".");
			}
			buffer.append(System.lineSeparator());		
			
			buffer.append("At the moment, I am ");
			buffer.append(person.getTaskDescription().toLowerCase()).append(".");
		}

		PhysicalCondition condition = person.getPhysicalCondition();

		double p = condition.getPerformanceFactor();
		double h = condition.getHunger();
		double e = condition.getEnergy();
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
		buffer.append("Performance: ")
			.append(PhysicalCondition.getPerformanceStatus(p));
		buffer.append(System.lineSeparator());

		return buffer.toString();
	}

}
