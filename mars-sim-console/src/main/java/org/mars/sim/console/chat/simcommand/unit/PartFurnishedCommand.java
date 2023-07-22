/**
 * Mars Simulation Project
 * PartFurnishedCommand.java
 * @date 2023-07-22
 * @author Manny Kung
 */

package org.mars.sim.console.chat.simcommand.unit;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;

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
		boolean result = false;
		if (!(source instanceof EquipmentOwner)) {
			context.println("Sorry this Unit does not hold parts");
		}
		else if (input == null) {
			context.println("Specify '[command] [part]:[quantity]'. e.g. /fu steel ingot:2");
		}
		else {
			String [] args = input.split(":");
			if (args.length != 2) {
				context.println("Argument format is '[command] [part]:[quantity]'. e.g. /fu steel ingot:2");
			}
			else {
				EquipmentOwner eo = (EquipmentOwner) source;
		
				Part part = (Part) ItemResourceUtil.findItemResource(args[0]);
				if (part == null) {
					context.println(input + " is an unknown part.");
				}
				else {
					int existingQuantity = eo.getItemResourceStored(part.getID());
					
					int quantity = Integer.parseInt(args[1]);
					eo.storeItemResource(part.getID(), quantity);
					
					int newQuantity = eo.getItemResourceStored(part.getID());
					
					context.println(quantity + "x " + part.getName() + " added.");
							
					context.println(part.getName() + ": " 
							+ existingQuantity 
							+ "x -> " + newQuantity + "x.");
				}
			}
		}

		return result;
	}
}
