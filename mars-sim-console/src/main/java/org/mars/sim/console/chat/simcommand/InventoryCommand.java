package org.mars.sim.console.chat.simcommand;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;

/**
 * Command to get the inventory of a Unit
 */
public class InventoryCommand extends ChatCommand {

	public InventoryCommand(String commandGroup) {
		super(commandGroup, "i", "inventory", "What is the inventory");
	}

	/** 

	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		ConnectedUnitCommand parent = (ConnectedUnitCommand) context.getCurrentCommand();
		Unit source = parent.getUnit();

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
		for (ItemResource ir : inv.getAllItemRsStored()) {
			String name = ir.getName();
			int amount = inv.getItemResourceNum(ir);
			if (amount > 0) {
				entries.put(name, "" + amount);
			}
		}
		
		for (AmountResource ar : inv.getAllAmountResourcesStored(false)) {
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
