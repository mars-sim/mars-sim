/**
 * Mars Simulation Project
 * AbstraInventoryCommandctUnitCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.unit;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.robot.Robot;

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

		Inventory inv = source.getInventory();
		
		StructuredResponse buffer = new StructuredResponse();
		String capacity = "Limitless";
		String available = "All";
		if (inv.getGeneralCapacity() < Double.MAX_VALUE) {
			capacity = String.format(CommandHelper.KG_FORMAT, inv.getGeneralCapacity());
			available = String.format(CommandHelper.KG_FORMAT, inv.getRemainingGeneralCapacity(false));
					
		}
		buffer.appendLabeledString("Capacity", capacity);
		buffer.appendLabeledString("Available", available);
	
		Map<String,String> entries = new TreeMap<>();
		Collection<Equipment> equipment = inv.findAllEquipment();
		if (input != null) {
			// Filter according to input
			equipment = equipment.stream().filter(i -> i.getType().toLowerCase().contains(input)).collect(Collectors.toList());
		}
		// Counts Equipment type but exclude Robot; Hack until Robots are correctly subclasses
		Map<String, Long> eqCounts = equipment.stream()
									.filter(e -> !(e instanceof Robot))
									.collect(Collectors.groupingBy(Equipment::getType, Collectors.counting()));
		for (Entry<String, Long> eq : eqCounts.entrySet()) {
			entries.put(eq.getKey().toLowerCase(), eq.getValue().toString());
		}
		
		// Add Items
		Set<ItemResource> itemResources = inv.getAllItemRsStored();
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
		
		// Add Resources
		Set<AmountResource> amountResources = inv.getAllAmountResourcesStored(false);
		if (input != null) {
			// Filter according to input
			amountResources = amountResources.stream().filter(a -> a.getName().contains(input)).collect(Collectors.toSet());
		}
		for (AmountResource ar : amountResources) {
			String name = ar.getName();
			double amount = inv.getAmountResourceStored(ar, false);
			if (amount > 0) {
				entries.put(name, "" + String.format(CommandHelper.KG_FORMAT, amount));
			}
		}

		buffer.appendTableHeading("Item", 30, "Amount", 10);
		for (Entry<String, String> row : entries.entrySet()) {
			buffer.appendTableRow(row.getKey(), row.getValue());
		}
		context.println(buffer.getOutput());
		return true;
	}

}
