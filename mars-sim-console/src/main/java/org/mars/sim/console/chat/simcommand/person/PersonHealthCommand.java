/**
 * Mars Simulation Project
 * PersonHealthCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.person;

import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;

/** 
 * Reports on a Persons health
 */
public class PersonHealthCommand extends AbstractPersonCommand {
	
	public PersonHealthCommand() {
		super("h", "health", "About health");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {

		StructuredResponse responseText = new StructuredResponse();		
		responseText.appendHeading("Health Indicators");
		
		PhysicalCondition pc = person.getPhysicalCondition();
		

		double energy = Math.round(pc.getEnergy()*10.0)/10.0;
        double stress = Math.round(pc.getStress()*10.0)/10.0;
        double perf = Math.round(pc.getPerformanceFactor()*1_000.0)/10.0;
                
		String h = !pc.isHungry() ? "(Not Hungry)" : "(Hungry)";
		String t = !pc.isThirsty() ? "(Not Thirsty)" : "(Thirsty)";
		
		responseText.appendLabeledString("Thirst", String.format(CommandHelper.MILLISOL_FORMAT, pc.getThirst()) + " " + t);		
		responseText.appendLabeledString("Hunger", String.format(CommandHelper.MILLISOL_FORMAT, pc.getHunger()) + " " + h);
		responseText.appendLabeledString("Energy", energy + " kJ");
		responseText.appendLabeledString("Fatigue", String.format(CommandHelper.MILLISOL_FORMAT, pc.getFatigue()));		
		responseText.appendLabeledString("Performance", perf + " %");
		responseText.appendLabeledString("Stress", stress + " %");		
		responseText.appendLabeledString("Surplus Ghrelin", String.format(CommandHelper.MILLISOL_FORMAT,
										person.getCircadianClock().getSurplusGhrelin()));
		responseText.appendLabeledString("Surplus Leptin", String.format(CommandHelper.MILLISOL_FORMAT,
										 person.getCircadianClock().getSurplusLeptin()));
		
		List<String> probs = pc.getProblems().stream().map(hp -> hp.getIllness().getType().getName())
											 .collect(Collectors.toList());
		responseText.appendNumberedList("Problems", probs);
		
		context.println(responseText.getOutput());
		
		return true;
	}
}
