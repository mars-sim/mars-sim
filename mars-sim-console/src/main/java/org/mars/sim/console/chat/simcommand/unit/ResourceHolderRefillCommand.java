/**
 * Mars Simulation Project
 * ResourceHolderRefillCommand.java
 * @version 3.1.2 2021-11-20
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * Command to get the refill a Resource Holder in expert mode
 */
public class ResourceHolderRefillCommand extends AbstractUnitCommand {


	public ResourceHolderRefillCommand(String commandGroup) {
		super(commandGroup, "rf", "refill", "Refill a Unit with a resource. Takes 'resource name'quantity' as arguments, e.g. oxygen@100");
		addRequiredRole(ConversationRole.EXPERT);
	}

	/** 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		boolean result = false;
		if (!(source instanceof ResourceHolder)) {
			context.println("Sorry this Unit does not hold resources");
		}
		else if (input == null) {
			context.println("Specify a 'resource@quantity' as an arguments");
		}
		else {
			String [] args = input.split("@");
			if (args.length != 2) {
				context.println("Argumetn format is 'resource@quantity'");
			}
			else {
				ResourceHolder rh = (ResourceHolder) source;
		
				AmountResource resource = ResourceUtil.findAmountResource(args[0]);
				if (resource == null) {
					context.println(input + " is an unknown resource.");
				}
				else {
					int quantity = Integer.parseInt(args[1]);
					rh.storeAmountResource(resource.getID(), quantity);
					context.println("Added " + quantity + " kg of " + resource.getName());
					result = true;
				}
			}
		}

		return result;
	}
}
