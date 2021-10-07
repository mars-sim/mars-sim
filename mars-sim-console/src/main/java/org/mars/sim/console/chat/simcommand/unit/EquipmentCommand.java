/**
 * Mars Simulation Project
 * EquipmentCommand.java
 * @version 3.3.2 2021-10-07
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.Collection;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Command to get the Equipment of a Unit. Only Equipment with a resource
 * is displayed; empty equipment is skipped.
 */
public class EquipmentCommand extends AbstractUnitCommand {

	private static final String RESOURCE_FORMAT = "%s - %.2f/%.2f";

	public EquipmentCommand(String commandGroup) {
		super(commandGroup, "eq", "equipment", "Show the details of any Equipment owned; optional filter to Equipment type");
		setIntroduction("Inventory held");
	}

	/** 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {

		Inventory inv = source.getInventory();
		
		StructuredResponse buffer = new StructuredResponse();

		Collection<Equipment> equipment = inv.findAllEquipment();

		buffer.appendTableHeading("Equipment", 20, "Stored (kg)");
		for (Equipment e : equipment) {
			String stored = null;
			if (e instanceof EVASuit) {
				EVASuit suit = (EVASuit) e;
				StringBuilder builder = new StringBuilder();

				for(int resourceID: suit.getResourceIDs()) {
					if (builder.length() > 0) {
						builder.append(", ");
					}
					builder.append(String.format(RESOURCE_FORMAT, 
								ResourceUtil.findAmountResourceName(resourceID),
								suit.getAmountResourceStored(resourceID),
								suit.getAmountResourceCapacity(resourceID)));
				}
				if (builder.length() > 0) {
					stored = builder.toString();
				}
			}
			else if (!(e instanceof Robot)) {
				int resourceID = e.getResource();
				if (resourceID >= 0) {
					stored = String.format(RESOURCE_FORMAT, 
								ResourceUtil.findAmountResourceName(resourceID),
								e.getAmountResourceStored(resourceID),
								e.getAmountResourceCapacity(resourceID));
				}
			}
			
			if (stored != null) {
				buffer.appendTableRow(e.getName(), stored.toString());
			}
		}
		context.println(buffer.getOutput());
		return true;
	}
}
