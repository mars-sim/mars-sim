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
import com.mars_sim.core.person.PhysicalConditionFormat;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.structure.Settlement;

/** 
 * The command showing the general status of a person
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
			buffer.append("It's been reported that ");
			buffer.append(person.getName());
			buffer.append(" has passed away. ");
			
			String genderNoun0 = "He was ";	
			String genderNoun1 = "His ";
			String genderNoun2 = "He ";	
			
			GenderType gender = person.getGender();
			if (gender == GenderType.FEMALE) {
				genderNoun0 = "She was";
				genderNoun1 = "Her ";
				genderNoun2 = "She ";	
			}
			
			buffer.append(genderNoun0);
			
			String job = person.getMind().getJobType().getName().toLowerCase();
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
				buffer.append("home base was ");
				buffer.append(person.getAssociatedSettlement().getName()).append(", ");
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
			buffer.append(death.getTask() + ".");
			buffer.append(System.lineSeparator());
			buffer.append("Cause of Death: ");
			buffer.append(death.getCause());
		}
		
		else {
			buffer.append("Hi, my name is ");
			buffer.append(person.getName());
			buffer.append(" and I am ");
			String job = person.getMind().getJobType().getName().toLowerCase();
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
			
			buffer.append("At this moment, I am ");
			buffer.append(person.getTaskDescription().toLowerCase()).append(".");
		}

		PhysicalCondition condition = person.getPhysicalCondition();
		buffer.append(System.lineSeparator());
		buffer.append(System.lineSeparator());
		buffer.append("     Hunger : ").append(PhysicalConditionFormat.getHungerStatus(condition, true)); 
		buffer.append(System.lineSeparator());
		buffer.append("     Energy : ").append(Math.round(condition.getEnergy() *10.0)/10.0 + " kJ");
		buffer.append(System.lineSeparator());
		buffer.append("     Thirst : ").append(PhysicalConditionFormat.getThirstyStatus(condition, true)); 
		buffer.append(System.lineSeparator());
		buffer.append("     Stress : ").append(PhysicalConditionFormat.getStressStatus(condition, true));
		buffer.append(System.lineSeparator());
		buffer.append("    Fatigue : ").append(PhysicalConditionFormat.getFatigueStatus(condition, true));
		buffer.append(System.lineSeparator());
		buffer.append("Performance : ").append(PhysicalConditionFormat.getPerformanceStatus(condition, true));
		buffer.append(System.lineSeparator());
		buffer.append("    Emotion : ").append(person.getMind().getEmotion().getDescription());
		buffer.append(System.lineSeparator());
		
		return buffer.toString();
	}

}
