/**
 * Mars Simulation Project
 * PeopleCommand.java
 * @date 2023-06-14
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.settlement;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.mars_sim.console.chat.ChatCommand;
import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.PopulationStats;
import com.mars_sim.core.structure.Settlement;

/**
 * Command to display people in a Settlement.
 * This is a singleton.
 */
public class PeopleCommand extends AbstractSettlementCommand {

	public static final ChatCommand PEOPLE = new PeopleCommand();

	private PeopleCommand() {
		super("pe", "people", "Settlement population");
	}

	/** 
	 * Outputs the population info.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();
		
		Collection<Person> citizens = settlement.getAllAssociatedPeople();
		Collection<Person> eva = settlement.getOutsideEVAPeople();
		Collection<Person> indoorP = settlement.getIndoorPeople();
		Collection<Person> deceasedP = settlement.getDeceasedPeople();
		Collection<Person> buriedP = settlement.getBuriedPeople();

		Set<Person> everyone = new TreeSet<>();
		everyone.addAll(citizens);
		everyone.addAll(eva);
		everyone.addAll(indoorP);
		everyone.addAll(buriedP);
		everyone.addAll(deceasedP);
		
		response.appendHeading("Summary");
		response.appendLabelledDigit("Registered", citizens.size());
		response.appendLabelledDigit("Inside", indoorP.size());
		response.appendLabelledDigit("EVA Operation", eva.size());
		response.appendLabeledString("Deceased (Buried)", deceasedP.size() + "(" + buriedP.size() + ")");
		response.appendLabeledString("Male/Female Ratio", PopulationStats.getGenderRatioAsString(citizens));	
		response.appendLabeledString("Average Age", String.format(CommandHelper.DOUBLE_FORMAT,
												PopulationStats.getAverageAge(citizens)));

		response.appendTableHeading("Name", CommandHelper.PERSON_WIDTH,
									"Citizen", "Inside", CommandHelper.BUILIDNG_WIDTH,
									"Mission", "EVA",
									"Dead", 6);
		for (Person person : everyone) {
			String status = (deceasedP.contains(person) ? "Yes" : "No");
			if (buriedP.contains(person)) {
				status = "Buried";
			}
			response.appendTableRow(person.getName(),
									citizens.contains(person),
									(indoorP.contains(person) ? person.getBuildingLocation().getName() : "No"),
									(person.getMission() != null),
									eva.contains(person),
									status
									);
		}
		
		context.println(response.getOutput());
		
		return true;
	}
}
