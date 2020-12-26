package org.mars.sim.console.chat.simcommand.person;

import java.util.List;
import java.util.Map;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;

/** 
 * 
 */
public class AttributeCommand extends AbstractPersonCommand {
	public static final ChatCommand ATTRIBUTES = new AttributeCommand();
	
	private AttributeCommand() {
		super("at", "attributes", "About my attributes");
	}

	@Override
	public void execute(Conversation context, String input, Person person) {
		StructuredResponse response = new StructuredResponse();

		response.appendTableHeading("Attributes", 20, "Score");

		NaturalAttributeManager n_manager = person.getNaturalAttributeManager();
		Map<NaturalAttributeType, Integer> n_attributes = n_manager.getAttributeMap();
		List<String> attributeList = n_manager.getAttributeList();

		for (String attr : attributeList) {
			response.appendTableDigit(attr,
					n_attributes.get(NaturalAttributeType.valueOfIgnoreCase(attr)));
		}
		context.println(response.getOutput());
	}
}
