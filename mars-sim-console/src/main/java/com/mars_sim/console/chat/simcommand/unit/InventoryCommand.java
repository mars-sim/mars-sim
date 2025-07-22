/*
 * Mars Simulation Project
 * InventoryCommand.java
 * @date 2021-10-21
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand.unit;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.simcommand.CommandHelper;
import com.mars_sim.console.chat.simcommand.StructuredResponse;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * Command to get the inventory of a Unit
 */
public class InventoryCommand extends AbstractUnitCommand {

	public InventoryCommand(String commandGroup) {
		super(commandGroup, "i", "inventory", "Inventory; apply an optional filter argument");
		setIntroduction("Inventory held");
	}

	/** 
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit source) {

		if (!(source instanceof EquipmentOwner)) {
			context.println("Sorry this Unit does not have an inventory");
			return false;
		}
		EquipmentOwner eqmOwner = (EquipmentOwner)source;
		
		StructuredResponse buffer = new StructuredResponse();
		String capacity = "Limitless";
		String available = "All";

		double eqmCapacity = eqmOwner.getCargoCapacity();
		capacity = String.format(CommandHelper.KG_FORMAT, eqmCapacity);
		available = String.format(CommandHelper.KG_FORMAT, (eqmCapacity - eqmOwner.getStoredMass()));

		buffer.appendLabeledString("Capacity", capacity);
		buffer.appendLabeledString("Available", available);
	
		// Find attached Equipment
		Map<String,String> entries = new TreeMap<>();
		Collection<Equipment> equipment = null;
		if (eqmOwner != null) {
			equipment = eqmOwner.getEquipmentSet();
		}

		if (input != null) {
			// Filter according to input
			equipment = equipment.stream()
								 .filter(i -> i.getEquipmentType().getName().toLowerCase().contains(input))
								 .collect(Collectors.toList());
		}
		
		// Counts Equipment type
		Map<EquipmentType, Long> eqCounts = equipment.stream()
									.collect(Collectors.groupingBy(Equipment::getEquipmentType, Collectors.counting()));
		for (Entry<EquipmentType, Long> eq : eqCounts.entrySet()) {
			entries.put(eq.getKey().getName(), eq.getValue().toString());
		}
		
		// Add Items
		extractResources(eqmOwner, input, entries);


		// Display all as a single table
		buffer.appendTableHeading("Item", CommandHelper.GOOD_WIDTH, "Amount", 10);
		for (Entry<String, String> row : entries.entrySet()) {
			buffer.appendTableRow(row.getKey(), row.getValue());
		}
		context.println(buffer.getOutput());
		return true;
	}

	private void extractResources(EquipmentOwner eqmOwner, String input, Map<String, String> entries) {
		Set<ItemResource> itemResources;
		Set<Integer> itemIDs = eqmOwner.getItemResourceIDs();
		if (input != null) {
			// Filter according to input
			itemResources = itemIDs.stream()
									.map(ItemResourceUtil::findItemResource)
									.filter(Objects::nonNull)
									.filter(i -> i.getName().contains(input))
									.collect(Collectors.toSet());
		}
		else {
			itemResources = itemIDs.stream()
					.map(ItemResourceUtil::findItemResource)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());	
		}
		
		for (ItemResource ir : itemResources) {
			String name = ir.getName();
			int amount = eqmOwner.getItemResourceStored(ir.getID());
			if (amount > 0) {
				entries.put(name, "" + amount);
			}
		}
		
		// Add Resources allow dirty to avoid updating
		Set<Integer> amountIDs = eqmOwner.getSpecificResourceStoredIDs();
		Set<AmountResource> amountResources;
		if (input != null) {
			// Filter according to input
			amountResources = amountIDs.stream()
									.map(ResourceUtil::findAmountResource)
									.filter(Objects::nonNull)
									.filter(a -> a.getName().contains(input))
									.collect(Collectors.toSet());
		}
		else {
			amountResources = amountIDs.stream()
					.map(ResourceUtil::findAmountResource)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());		
		}
		for (AmountResource ar : amountResources) {
			String name = ar.getName();
			double amount = eqmOwner.getSpecificAmountResourceStored(ar.getID());
			if (amount > 0) {
				entries.put(name, "" + String.format(CommandHelper.KG_FORMAT, amount));
			}
		}
	}
}
