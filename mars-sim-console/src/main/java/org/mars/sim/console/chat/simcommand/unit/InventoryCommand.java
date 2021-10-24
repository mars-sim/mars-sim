/*
 * Mars Simulation Project
 * InventoryCommand.java
 * @date 2021-10-21
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;

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

		EquipmentOwner eqmOwner = null;
		if (source instanceof EquipmentOwner) {
			eqmOwner = (EquipmentOwner)source;
		}
		
		StructuredResponse buffer = new StructuredResponse();
		String capacity = "Limitless";
		String available = "All";

		double eqmCapacity = eqmOwner.getTotalCapacity();
		capacity = String.format(CommandHelper.KG_FORMAT, eqmCapacity);
		available = String.format(CommandHelper.KG_FORMAT, (eqmCapacity - eqmOwner.getStoredMass()));

		buffer.appendLabeledString("Capacity", capacity);
		buffer.appendLabeledString("Available", available);
	
		// Find attached Equipment
		Map<String,String> entries = new TreeMap<>();
		Collection<Equipment> equipment = null;
		if (eqmOwner != null) {
			equipment = eqmOwner.getEquipmentList();
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
		if (eqmOwner != null) {
			extractResources(eqmOwner, input, entries);
		}

		// Displa all as a singel table
		buffer.appendTableHeading("Item", 30, "Amount", 10);
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
									.map(d -> ItemResourceUtil.findItemResource(d))
									.filter(Objects::nonNull)
									.filter(i -> i.getName().contains(input))
									.collect(Collectors.toSet());
		}
		else {
			itemResources = itemIDs.stream()
					.map(d -> ItemResourceUtil.findItemResource(d))
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
		Set<Integer> amountIDs = eqmOwner.getAmountResourceIDs();
		Set<AmountResource> amountResources;
		if (input != null) {
			// Filter according to input
			amountResources = amountIDs.stream()
									.map(d -> ResourceUtil.findAmountResource(d))
									.filter(Objects::nonNull)
									.filter(a -> a.getName().contains(input))
									.collect(Collectors.toSet());
		}
		else {
			amountResources = amountIDs.stream()
					.map(d -> ResourceUtil.findAmountResource(d))
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());		
		}
		for (AmountResource ar : amountResources) {
			String name = ar.getName();
			double amount = eqmOwner.getAmountResourceStored(ar.getID());
			if (amount > 0) {
				entries.put(name, "" + String.format(CommandHelper.KG_FORMAT, amount));
			}
		}
	}

	private void extractResources(Inventory inv, String input, Map<String, String> entries) {
		Set<ItemResource> itemResources = inv.getAllIRStored();
		if (input != null) {
			// Filter according to input
			itemResources = itemResources.stream().filter(i -> i.getName().contains(input)).collect(Collectors.toSet());
		}
		for (ItemResource ir : itemResources) {
			String name = ir.getName();
			int amount = inv.getItemResourceNum(ir);
			if (amount > 0) {
				entries.put(name, "" + amount);
			}
		}
		
		// Add Resources allow dirty to avoid updating
		Set<AmountResource> amountResources = inv.getAllAmountResourcesStored(true);
		if (input != null) {
			// Filter according to input
			amountResources = amountResources.stream().filter(a -> a.getName().contains(input)).collect(Collectors.toSet());
		}
		for (AmountResource ar : amountResources) {
			String name = ar.getName();
			double amount = inv.getAmountResourceStored(ar, true);
			if (amount > 0) {
				entries.put(name, "" + String.format(CommandHelper.KG_FORMAT, amount));
			}
		}
	}
}
