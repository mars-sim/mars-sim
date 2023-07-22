/*
 * Mars Simulation Project
 * ResourceHolderRefillCommand.java
 * @date 2023-07-22
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
 * Command to get the refill a Resource Holder in expert mode.
 */
public class ResourceHolderRefillCommand extends AbstractUnitCommand {


	public ResourceHolderRefillCommand(String commandGroup) {
		super(commandGroup, "rf", "refill", "Refill a Unit with a resource. Specify '[command] [resource]:[quantity]'. e.g. /rf oxygen:100");
		addRequiredRole(ConversationRole.EXPERT);
	}

	/** 
	 * Executes the command.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		boolean result = false;
		if (!(source instanceof ResourceHolder)) {
			context.println("Sorry this Unit does not hold resources");
		}
		else if (input == null) {
			context.println("Specify '[command] [resource]:[quantity]'. e.g. /rf oxygen:100");
		}
		else {
			String [] args = input.split(":");
			if (args.length != 2) {
				context.println("Argument format is '[command] [resource]:[quantity]'. e.g. /rf oxygen:100");
			}
			else {
				ResourceHolder rh = (ResourceHolder) source;
		
				AmountResource resource = ResourceUtil.findAmountResource(args[0]);
				if (resource == null) {
					context.println(input + " is an unknown resource.");
				}
				else {
					double existingAmount = rh.getAllAmountResourceStored(resource.getID());
	
					double quantity = Double.parseDouble(args[1]);
					rh.storeAmountResource(resource.getID(), quantity);
					
					double newAmount = rh.getAllAmountResourceStored(resource.getID());
					
					context.println(Math.round(quantity * 1000.0)/1000.0 + " kg " + resource.getName() + " added.");
					
					context.println(resource.getName() + ": " 
							+ Math.round(existingAmount * 1000.0)/1000.0 
							+ " kg -> " + Math.round(newAmount * 1000.0)/1000.0 + " kg.");

					result = true;
				}
			}
		}

		return result;
	}
}
