package org.mars.sim.console.chat.simcommand.vehicle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Command to get the cargo of a vehicle
 * This is a singleton.
 */
public class CargoCommand extends ChatCommand {

	public static final ChatCommand CARGO = new CargoCommand();

	private CargoCommand() {
		super(VehicleChat.VEHICLE_GROUP, "cg", "cargo", "What is the vehicle carrying");
	}

	/** 

	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();

		Inventory inv = source.getInventory();
		
		StructuredResponse buffer = new StructuredResponse();
		buffer.appendLabeledString("Capacity", String.format(CommandHelper.KG_FORMAT, inv.getGeneralCapacity()));
		buffer.appendLabeledString("Available", String.format(CommandHelper.KG_FORMAT, inv.getRemainingGeneralCapacity(false)));

		buffer.appendTableHeading("Item", 30, "Amount");
		for (ItemResource ir : inv.getAllItemRsStored()) {
			String name = ir.getName();
			int amount = inv.getItemResourceNum(ir);
			buffer.appendTableRow(name, amount);
		};
		
		for (AmountResource ar : inv.getAllAmountResourcesStored(true)) {
			String name = ar.getName();
			double amount = inv.getAmountResourceStored(ar, true);
			buffer.appendTableRow(name, String.format(CommandHelper.KG_FORMAT, amount));
		};
		
		context.println(buffer.getOutput());
		return true;
	}

}
