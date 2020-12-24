package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display population country
 * This is a singleton.
 */
public class CountryCommand extends AbstractSettlementCommand {

	public static final ChatCommand COUNTRY = new CountryCommand();

	private CountryCommand() {
		super("cy", "country", "Breakdown of population country of origin");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected void execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		Map<String, List<Person>> map = settlement.getAllAssociatedPeople().stream()
				.collect(Collectors.groupingBy(Person::getCountry));

		response.appendTableHeading("Country", 16, "Number");
		for (Map.Entry<String, List<Person>> entry : map.entrySet()) {
			String country = entry.getKey();
			//String sponsor = UnitManager.mapCountry2Sponsor(country);

			response.appendTableDigit(country, entry.getValue().size());
		}
		
		context.println(response.getOutput());
	}

}
