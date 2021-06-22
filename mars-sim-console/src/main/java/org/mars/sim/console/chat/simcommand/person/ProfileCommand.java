/**
 * Mars Simulation Project
 * ProfileCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.MBTIPersonality;

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
		response.appendLabeledString("Job", person.getMind().getJob().getName());

		response.appendBlankLine();
		PhysicalCondition condition = person.getPhysicalCondition();
		double hunger = condition.getHunger();
		double energy = condition.getEnergy();
		response.appendLabeledString("Hunger", PhysicalCondition.getHungerStatus(hunger, energy));
		response.appendLabeledString("Thirst", PhysicalCondition.getThirstyStatus(condition.getThirst()));
		response.appendLabeledString("Fatigue", PhysicalCondition.getFatigueStatus(condition.getFatigue()));
		response.appendLabeledString("Performance",
									PhysicalCondition.getPerformanceStatus(condition.getPerformanceFactor() * 100D));
		response.appendLabeledString("Stress", PhysicalCondition.getStressStatus(condition.getStress()));
		MBTIPersonality mbti = person.getMind().getMBTI();
		response.appendLabeledString("Personality", mbti.getDescriptor() + " (" + mbti.getTypeString() + ')');

		response.appendBlankLine();
		response.appendLabeledString("Country", person.getCountry());
		response.appendLabeledString("Sponsor", person.getReportingAuthority().getOrg().getShortName());
		
		context.println(response.getOutput());
		return true;
	}

}
