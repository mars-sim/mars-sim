package org.mars.sim.console.chat.simcommand.person;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;

/** 
 * 
 */
public class CountryCommand extends AbstractPersonCommand {
	public static final ChatCommand COUNTRY = new CountryCommand();
	
	private CountryCommand() {
		super("cy", "country", "About my country & sponsor");
	}

	@Override
	public boolean execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();
		response.appendLabeledString("Country", person.getCountry());
		response.appendLabeledString("Sponsor", person.getReportingAuthority().getOrg().getName());
		
		context.println(response.getOutput());
		return true;
	}

}
