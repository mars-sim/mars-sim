/*
 * Mars Simulation Project
 * IllnessCommand.java
 * @date 2023-11-02
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;

/** 
 * 
 */
public class IllnessCommand extends AbstractPersonCommand {
	public static final ChatCommand ILLNESS = new IllnessCommand();
	
	private IllnessCommand() {
		super("il", "illness", "Get ill");
		
		setInteractive(true);
		addRequiredRole(ConversationRole.EXPERT);
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		
		List<ComplaintType> complaints = Arrays.asList(ComplaintType.values());

		List<String> complaintNames = new ArrayList<>();
		for (ComplaintType c: complaints) {
			complaintNames.add(c.getName());
		}
		Collections.sort(complaintNames);
	
		// Choose one
		int choice = CommandHelper.getOptionInput(context, complaintNames, 
				"Pick an illness from above by entering a number");
		
		if (choice < 0) {
			context.println("No illness was chosen. Try again.");
			return false;
		}

		ComplaintType complaintType = complaints.get(choice);
		
		HealthProblem problem = new HealthProblem(complaintType, person);
		person.getPhysicalCondition().addMedicalComplaint(problem.getComplaint());
		
		context.println("You picked the illness '" + complaintType + "'.");
		
		GenderType type = person.getGender();
		String pronoun = "him";
		if (type == GenderType.FEMALE)
			pronoun = "her";
		
		String toSave = context.getInput("Do you want " 
				+ pronoun
				+ " to be dead (Y/N)?");
		
        if ("Y".equalsIgnoreCase(toSave)) {
            context.println(person + " is now dead.");
            person.getPhysicalCondition().recordDead(problem, true, 
    				"I got inflicted with " + complaintType + " (not by my own choice).");
        }
        else {
        	context.println(person + " suffered from'" + complaintType + "'.");
        }
		
		context.println("");
		
		return true;
	}
}
