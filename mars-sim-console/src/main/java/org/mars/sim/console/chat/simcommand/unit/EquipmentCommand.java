/*
 * Mars Simulation Project
 * EquipmentCommand.java
 * @date 2021-10-21
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.resource.ResourceUtil;

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

		boolean showAll = ((input != null) && input.equalsIgnoreCase("all"));
		
		StructuredResponse buffer = new StructuredResponse();
		
		Collection<Equipment> equipment = null;
		if (source instanceof EquipmentOwner) {
			equipment = ((EquipmentOwner)source).getEquipmentSet();
		}
		else {
			context.println("Sorry this Entity does not hold Equipment");
			return false;
		}
		
		SortedMap<String,String> entries = new TreeMap<>();
		for (Equipment e : equipment) {
			String stored = null;
			
			// Container must come first
			if (e instanceof Container) {
				Container c = (Container) e;
				int resourceID = c.getResource();
				if (resourceID >= 0) {
					stored = formatResource(c, resourceID);
				}
				else if (showAll) {
					stored = "empty";
				}
			}
			else if (e instanceof ResourceHolder) {
				ResourceHolder suit = (ResourceHolder) e;
				StringBuilder builder = new StringBuilder();

				for(int resourceID: suit.getAmountResourceIDs()) {
					if (builder.length() > 0) {
						builder.append(", ");
					}
					builder.append(formatResource(suit, resourceID));
				}
				if (builder.length() > 0) {
					stored = builder.toString();
				}
				else if (showAll) {
					stored = "empty";
				}
			}

			
			if (stored != null) {
				entries.put(e.getName(), stored);
			}
		}

		// Output entrires which will be order via TreeMap
		buffer.appendTableHeading("Equipment", 20, "Stored (kg)");
		for(String name : entries.keySet()) {
			buffer.appendTableRow(name, entries.get(name));			
		}
		
		context.println(buffer.getOutput());
		return true;
	}
	
	private static String formatResource(ResourceHolder holder, int resourceID) {
		return String.format(RESOURCE_FORMAT, 
				ResourceUtil.findAmountResourceName(resourceID),
				holder.getAmountResourceStored(resourceID),
				holder.getAmountResourceCapacity(resourceID));
	}
}
