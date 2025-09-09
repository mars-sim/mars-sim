/*
 * Mars Simulation Project
 * ProfileCommand.java
 * @date 2022-08-24
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.person;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.PhysicalConditionFormat;
import com.mars_sim.core.person.ai.MBTIPersonality;

/** 
 * 
 */
public class ProfileCommand extends AbstractPersonCommand {
	public static final ChatCommand PROFILE = new ProfileCommand();
	
	private ProfileCommand() {
		super("pf", "profile", "About me");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		response.appendLabeledString("Name", person.getName());
		response.appendLabeledString("Gender", person.getGender().getName());
		response.appendLabelledDigit("Age", person.getAge());
		response.appendLabeledString("Weight", String.format(CommandHelper.KG_FORMAT, person.getMass()));
		response.appendLabeledString("Height", String.format("%.2f m", person.getHeight()/100D));
		response.appendLabeledString("Job", person.getMind().getJobType().getName());

		response.appendBlankLine();
		PhysicalCondition condition = person.getPhysicalCondition();
		response.appendLabeledString("Hunger", PhysicalConditionFormat.getHungerStatus(condition, false));
		response.appendLabeledString("Thirst", PhysicalConditionFormat.getThirstyStatus(condition, false));
		response.appendLabeledString("Fatigue", PhysicalConditionFormat.getFatigueStatus(condition, false));
		response.appendLabeledString("Performance",
									PhysicalConditionFormat.getPerformanceStatus(condition, false));
		response.appendLabeledString("Stress", PhysicalConditionFormat.getStressStatus(condition, false));
		MBTIPersonality mbti = person.getMind().getMBTI();
		response.appendLabeledString("Personality", mbti.getDescriptor() + " (" + mbti.getTypeString() + ')');

		response.appendBlankLine();
		response.appendLabeledString("Country", person.getCountry());
		response.appendLabeledString("Sponsor", person.getReportingAuthority().getName());
		
		context.println(response.getOutput());
		return true;
	}

}
