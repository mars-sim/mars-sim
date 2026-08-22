/**
 * Mars Simulation Project
 * PartFurnishedCommand.java
 * @date 2023-07-22
 * @author Manny Kung
 */

package com.mars_sim.console.chat.simcommand.unit;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;

/**
 * Command to furnish a Part in expert mode.
 */
public class PartFurnishedCommand extends AbstractUnitCommand {

	public PartFurnishedCommand(String commandGroup) {
		super(commandGroup, "fu", "furnish", "Furnish a Part to a Unit. Specify '[command] [part]:[quantity]'. e.g. /fu steel ingot:2");
		addRequiredRole(ConversationRole.EXPERT);
	}

	/** 
	 * Executes the command.
	 * 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {
		var eo = EquipmentOwner.getAttached(source);
		if (eo == null) {
			context.println("Sorry this Unit does not have an inventory");
			return false;
		}
		if (input == null) {
			context.println("Specify '[command] [part]:[quantity]'. e.g. /fu steel ingot:2");
			return false;
		}

		String [] args = input.split(":");
		if (args.length != 2) {
			context.println("Argument format is '[command] [part]:[quantity]'. e.g. /fu steel ingot:2");
			return false;
		}
		
		Part part = (Part) ItemResourceUtil.findItemResource(args[0]);
		if (part == null) {
			context.println(input + " is an unknown part.");
			return false;
		}
		
		int existingQuantity = eo.getItemResourceStored(part.getID());
		
		int quantity = Integer.parseInt(args[1]);
		eo.storeItemResource(part.getID(), quantity);
		
		int newQuantity = eo.getItemResourceStored(part.getID());
		
		context.println(quantity + "x " + part.getName() + " added.");
				
		context.println(part.getName() + ": " 
				+ existingQuantity 
				+ "x -> " + newQuantity + "x.");

		return true;
	}
}
