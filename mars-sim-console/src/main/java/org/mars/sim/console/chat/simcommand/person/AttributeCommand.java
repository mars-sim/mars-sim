package org.mars.sim.console.chat.simcommand.person;

import java.util.Map;
import java.util.Map.Entry;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.ConnectedUnitCommand;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.robot.Robot;

/** 
 * 
 */
public class AttributeCommand extends ChatCommand {
	
	public AttributeCommand(String group) {
		super(group, "at", "attributes", "About my attributes");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		ConnectedUnitCommand parent = (ConnectedUnitCommand) context.getCurrentCommand();	
		Unit target = parent.getUnit();

		NaturalAttributeManager nManager = null;

		if (target instanceof Person) {
			nManager = ((Person)target).getNaturalAttributeManager();
		}
		else if (target instanceof Robot) {
			nManager = ((Robot)target).getRoboticAttributeManager();
		}
		else {
			context.println("Sorry I doing have any Attributes");
			return false;
		}

		StructuredResponse response = new StructuredResponse();

		response.appendTableHeading("Attributes", 20, "Score");

		Map<NaturalAttributeType, Integer> nAttributes = nManager.getAttributeMap();

		for (Entry<NaturalAttributeType, Integer> attr : nAttributes.entrySet()) {
			response.appendTableRow(attr.getKey().getName(),
									attr.getValue());
		}
		context.println(response.getOutput());
		
		return true;
	}
}
