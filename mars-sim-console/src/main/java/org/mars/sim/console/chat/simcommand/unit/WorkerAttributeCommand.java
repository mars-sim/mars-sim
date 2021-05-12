package org.mars.sim.console.chat.simcommand.unit;

import java.util.Map;
import java.util.Map.Entry;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;

/** 
 * 
 */
public class WorkerAttributeCommand extends AbstractUnitCommand {
	
	public WorkerAttributeCommand(String group) {
		super(group, "at", "attributes", "About my attributes");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit target) {

		NaturalAttributeManager nManager = null;

		if (target instanceof Worker) {
			nManager = ((Worker)target).getNaturalAttributeManager();
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
