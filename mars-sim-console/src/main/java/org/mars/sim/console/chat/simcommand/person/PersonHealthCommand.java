package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;

/** 
 * Reports on a Persons health
 */
public class PersonHealthCommand extends AbstractPersonCommand {
	
	private static final String MILLISOLS = " millisols";

	public PersonHealthCommand() {
		super("h", "health", "About health");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {

		StructuredResponse responseText = new StructuredResponse();		
		responseText.appendHeading("Health Indicators");
		
		PhysicalCondition pc = person.getPhysicalCondition();
		
		double fatigue = Math.round(pc.getFatigue()*10.0)/10.0;
		double thirst = Math.round(pc.getThirst()*10.0)/10.0;
		double hunger = Math.round(pc.getHunger()*10.0)/10.0;
		double energy = Math.round(pc.getEnergy()*10.0)/10.0;
        double stress = Math.round(pc.getStress()*10.0)/10.0;
        double perf = Math.round(pc.getPerformanceFactor()*1_000.0)/10.0;
                
    	double ghrelin = Math.round(person.getCircadianClock().getSurplusGhrelin()*10.0)/10.0;
    	double leptin = Math.round(person.getCircadianClock().getSurplusLeptin()*10.0)/10.0;
    	
		String h = !pc.isHungry() ? "(Not Hungry)" : "(Hungry)";
		String t = !pc.isThirsty() ? "(Not Thirsty)" : "(Thirsty)";
		
		responseText.appendLabeledString("Thrist", thirst + MILLISOLS + " " + t);		
		responseText.appendLabeledString("Hunger", hunger + MILLISOLS + " " + h);
		responseText.appendLabeledString("Energy", energy + " kJ");
		responseText.appendLabeledString("Fatigue", fatigue + MILLISOLS);		
		responseText.appendLabeledString("Performance", perf + " %");
		responseText.appendLabeledString("Stress", stress + " %");		
		responseText.appendLabeledString("Surplus Ghrelin", ghrelin + MILLISOLS);
		responseText.appendLabeledString("Surplus Leptin", leptin + MILLISOLS);
		
		context.println(responseText.getOutput());
	}

}
