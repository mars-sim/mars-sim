/*
 * Mars Simulation Project
 * EquipmentCommand.java
 * @date 2021-10-21
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.resource.ResourceUtil;

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
		Collection<Equipment> equipment = null;
		if (source instanceof EquipmentOwner eo) {
			equipment = eo.getEquipmentSet();
		}
		else {
			context.println("Sorry this Entity does not hold Equipment");
			return false;
		}

		if (input == null) {
			showDetails(context, equipment, false);
		}
		else if (input.equalsIgnoreCase("all")) {
			showDetails(context, equipment, true);
		}
		else if (input.equalsIgnoreCase("stats")) {
			showStats(context, equipment);
		}
		
		return true;
	}

	private void showStats(Conversation context, Collection<Equipment> equipment) {
		var eqmsByType = equipment.stream()
				.collect(Collectors.groupingBy(Equipment::getEquipmentType));

		StructuredResponse buffer = new StructuredResponse();
		buffer.appendTableHeading("Equipment", 20, "# Empty", "# Total");
		for(var e : eqmsByType.entrySet()) {
			var v = e.getValue();
			buffer.appendTableRow(e.getKey().getName(),
									v.stream().filter(b -> b.isEmpty(false)).count(),
									v.size());			
		}
		
		context.println(buffer.getOutput());
		
	}

	private void showDetails(Conversation context, Collection<Equipment> equipment, boolean showAll) {
		StructuredResponse buffer = new StructuredResponse();
		SortedMap<String,String> entries = new TreeMap<>();
		for (Equipment e : equipment) {
			String stored = null;
			
			// Container must come first
			if (e instanceof Container c) {
				int resourceID = c.getResource();
				if (resourceID >= 0) {
					stored = formatResource(c, resourceID);
				}
				else if (showAll) {
					stored = "empty";
				}
			}
			else if (e instanceof ResourceHolder suit) {
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

		// Output entries which will be order via TreeMap
		buffer.appendTableHeading("Equipment", 20, "Stored (kg)");
		for(var e : entries.entrySet()) {
			buffer.appendTableRow(e.getKey(), e.getValue());			
		}
		
		context.println(buffer.getOutput());
	}
	
	private static String formatResource(ResourceHolder holder, int resourceID) {
		return String.format(RESOURCE_FORMAT, 
				ResourceUtil.findAmountResourceName(resourceID),
				holder.getAmountResourceStored(resourceID),
				holder.getAmountResourceCapacity(resourceID));
	}
}
