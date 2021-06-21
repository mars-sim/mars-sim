/**
 * Mars Simulation Project
 * PeopleCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display people in a Settlement
 * This is a singleton.
 */
public class PeopleCommand extends AbstractSettlementCommand {

	public static final ChatCommand PEOPLE = new PeopleCommand();

	private PeopleCommand() {
		super("pe", "people", "Settlement population");
	}

	/** 
	 * Output the 
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
		Collection<Person> onMission = settlement.getOnMissionPeople();

		Set<Person> everyone = new TreeSet<>();
		everyone.addAll(citizens);
		everyone.addAll(eva);
		everyone.addAll(indoorP);
		everyone.addAll(buriedP);
		everyone.addAll(deceasedP);
		everyone.addAll(onMission);
		
		response.appendHeading("Summary");
		response.appendLabelledDigit("Registered", citizens.size());
		response.appendLabelledDigit("Inside", indoorP.size());
		response.appendLabelledDigit("On a Mission", onMission.size());
		response.appendLabelledDigit("EVA Operation", eva.size());
		response.appendLabeledString("Deceased (Buried)", deceasedP.size() + "(" + buriedP.size() + ")");

		response.appendTableHeading("Name", CommandHelper.PERSON_WIDTH,
									"Citizen", "Inside", CommandHelper.BUILIDNG_WIDTH,
									"Mission", "EVA",
									"Dead", "Buried");
		for (Person person : everyone) {
			response.appendTableRow(person.getName(),
									(citizens.contains(person) ? "Yes" : "No"),
									(indoorP.contains(person) ? person.getBuildingLocation().getNickName() : "No"),
									(onMission.contains(person) ? "Yes" : "No"),
									(eva.contains(person) ? "Yes" : "No"),
									(deceasedP.contains(person) ? "Yes" : "No"),
									(buriedP.contains(person) ? "Yes" : "No")
									);
		}
		
		context.println(response.getOutput());
		
		return true;
	}
}
