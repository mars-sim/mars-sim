/*
 * Mars Simulation Project
 * ResourceHolderRefillCommand.java
 * @date 2023-07-22
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.ItemHolder;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

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

		if (input == null) {
			context.println("Specify '[command] [resource]:[quantity]'. e.g. /rf oxygen:100");
			return false;
		}

		String [] args = input.split(":");
		if (args.length != 2) {
			context.println("Argument format is '[command] [resource]:[quantity]'. e.g. /rf oxygen:100");
			return false;
		}

		double quantity = Double.parseDouble(args[1]);
		var name = args[0].replace('_', ' ');
		try {
			var ar = ResourceUtil.findAmountResource(name);
			if (ar != null)  {
				return adjustResource(source, ar, quantity, context);
			}
		} catch (IllegalArgumentException e) {
			// Name is not a amount resource
		}

		try {
			var ir = ItemResourceUtil.findItemResource(name);
			if (ir != null) {
				return adjustPart(source, ir, quantity, context);
			}
		} catch (IllegalArgumentException ee) {
			// Name is not a amount resource
		}
		context.println("Unknown resource '" + name + "'.");
		return false;
	}

	private boolean adjustPart(Unit source, ItemResource ir, double quantity, Conversation context) {
		if (!(source instanceof ItemHolder)) {
			context.println("Sorry this Unit does not hold Items");
			return false;
		}
		var ih = (ItemHolder) source;

		int existingAmount = ih.getItemResourceStored(ir.getID());
		if (quantity > 0) {
			ih.storeItemResource(ir.getID(), (int) quantity);
		}
		else {
			ih.retrieveItemResource(ir.getID(), (int) -quantity);
		}
		
		double newAmount = ih.getItemResourceStored(ir.getID());
		
		context.println(quantity + " " + ir.getName() + " added.");
		
		context.println(ir.getName() + ": " 
				+ existingAmount + " -> " + newAmount );
		return true;
	}

	private boolean adjustResource(Unit source, AmountResource resource, double quantity, Conversation context) {
		if (!(source instanceof ResourceHolder)) {
			context.println("Sorry this Unit does not hold resources");
			return false;
		}
		ResourceHolder rh = (ResourceHolder) source;

		double existingAmount = rh.getAllAmountResourceStored(resource.getID());
		if (quantity> 0) {
			rh.storeAmountResource(resource.getID(), quantity);
		}
		else {
			rh.retrieveAmountResource(resource.getID(), -quantity);
		}
		
		double newAmount = rh.getAllSpecificAmountResourceStored(resource.getID());
		
		context.println(Math.round(quantity * 1000.0)/1000.0 + " kg " + resource.getName() + " added.");
		
		context.println(resource.getName() + ": " 
				+ Math.round(existingAmount * 1000.0)/1000.0 
				+ " kg -> " + Math.round(newAmount * 1000.0)/1000.0 + " kg.");
		return true;
	}
}
