package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
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
		
		Collection<Person> all = settlement.getAllAssociatedPeople();
		Collection<Person> eva = settlement.getOutsideEVAPeople();
		Collection<Person> indoorP = settlement.getIndoorPeople();
		Collection<Person> deceasedP = settlement.getDeceasedPeople();
		Collection<Person> buriedP = settlement.getBuriedPeople();
		Collection<Person> onMission = settlement.getOnMissionPeople();

		response.appendHeading("Summary");
		response.appendLabelledDigit("Registered", all.size());
		response.appendLabelledDigit("Inside", indoorP.size());
		response.appendLabelledDigit("On a Mission", onMission.size());
		response.appendLabelledDigit("EVA Operation", eva.size());
		response.appendLabeledString("Deceased (Buried)", deceasedP.size() + "(" + buriedP.size() + ")");

		addPeopleList(response, "A. Registed Citizens", all);
		addPeopleList(response, "B. Inside", indoorP);
		addPeopleList(response, "C. EVA Operation", eva);
		addPeopleList(response, "D. On a Mission", onMission);
		addPeopleList(response, "E. Deceased", deceasedP);
		addPeopleList(response, "F. Buried", buriedP);
		
		context.println(response.getOutput());
		
		return true;
	}

	/**
	 * Create a sorted numbered list of the People
	 * @param response
	 * @param string
	 * @param all
	 */
	private static void addPeopleList(StructuredResponse response, String title, Collection<Person> all) {
		List<String> ordered = all.stream().map(Person::getName).sorted().collect(Collectors.toList());
		response.appendNumberedList(title, ordered);
	}

}
