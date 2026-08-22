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
		var eo = EquipmentOwner.getAttached(source);
		if (eo == null) {
			context.println("Sorry this Entity does not hold Equipment");
			return false;
		}
		Collection<Equipment> equipment = eo.getEquipmentSet();

		if ((input != null) && input.equalsIgnoreCase("stats")) {
			showStats(context, equipment);
			return true;
		}
		
		if (input != null) {
			equipment = equipment.stream()
					.filter(e -> e.getEquipmentType().getName().equalsIgnoreCase(input))
					.toList();
		}
		showDetails(context, equipment);
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

	private void showDetails(Conversation context, Collection<Equipment> equipment) {
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
			}
			else {
				var suit = ResourceHolder.getAttached(e);
				if (suit == null) {
					continue;
				}

				stored = suit.getSpecificResourceStoredIDs().stream()
						.map(r -> formatResource(suit, r))
						.collect(Collectors.joining(", "));
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
				holder.getSpecificAmountResourceStored(resourceID),
				holder.getSpecificCapacity(resourceID));
	}
}
