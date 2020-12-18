package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;

/** 
 * Reports on a Persons health
 */
public class PersonHealthCommand extends ChatCommand {
	private static final String COLUMN1_FORMAT = "%24s";
	private static final String COLUMN3_FORMAT = "%16s %8s %-9s%n";
	private static final String COLUMN4_FORMAT = "%16s %8s %-9s %-s%n";
	
	public PersonHealthCommand() {
		super(PersonChat.PERSON_GROUP, "h", "health", "About my health");
	}

	@Override
	public void execute(Conversation context, String input) {
		PersonChat parent = (PersonChat) context.getCurrentCommand();
		Person person = parent.getPerson();
		
		StringBuffer responseText = new StringBuffer();
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());
		
		responseText.append(String.format(COLUMN1_FORMAT, "Health Indicators"));
		responseText.append(System.lineSeparator());
		responseText.append(" -------------------------------------------------- ");
		responseText.append(System.lineSeparator());		
		
		PhysicalCondition pc = person.getPhysicalCondition();
		
		double fatigue = Math.round(pc.getFatigue()*10.0)/10.0;
		double thirst = Math.round(pc.getThirst()*10.0)/10.0;
		double hunger = Math.round(pc.getHunger()*10.0)/10.0;
		double energy = Math.round(pc.getEnergy()*10.0)/10.0;
        double stress = Math.round(pc.getStress()*10.0)/10.0;
        double perf = Math.round(pc.getPerformanceFactor()*1_000.0)/10.0;
                
    	double ghrelin = Math.round(person.getCircadianClock().getSurplusGhrelin()*10.0)/10.0;
    	double leptin = Math.round(person.getCircadianClock().getSurplusLeptin()*10.0)/10.0;
    	
		boolean notHungry = !pc.isHungry();
		boolean notThirsty = !pc.isThirsty();
		String h = notHungry ? "(Not Hungry)" : "(Hungry)";
		String t = notThirsty ? "(Not Thirsty)" : "(Thirsty)";
		

		responseText.append(String.format(COLUMN4_FORMAT, "Thrist", thirst, "millisols", t));
		responseText.append(System.lineSeparator());
		
		responseText.append(String.format(COLUMN4_FORMAT, "Hunger", hunger, "millisols", h));
		responseText.append(System.lineSeparator());

		responseText.append(String.format(COLUMN3_FORMAT, "Energy", energy, "kJ"));
		responseText.append(System.lineSeparator());

		responseText.append(String.format(COLUMN3_FORMAT, "Fatigue", fatigue, "millisols"));
		responseText.append(System.lineSeparator());
		
		responseText.append(String.format(COLUMN3_FORMAT, "Performance", perf, "%"));
		responseText.append(System.lineSeparator());
		responseText.append(String.format(COLUMN3_FORMAT, "Stress", stress, "%"));
		responseText.append(System.lineSeparator());
		
		responseText.append(String.format(COLUMN3_FORMAT, "Surplus Ghrelin", ghrelin, "millisols"));
		responseText.append(System.lineSeparator());
		responseText.append(String.format(COLUMN3_FORMAT, "Surplus Leptin", leptin, "millisols"));
		responseText.append(System.lineSeparator());
		
		context.println(responseText.toString());
	}

}
