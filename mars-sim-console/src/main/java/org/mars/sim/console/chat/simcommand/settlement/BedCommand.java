package org.mars.sim.console.chat.simcommand.settlement;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Command to display bed allocation in a Settlement
 * This is a singleton.
 */
public class BedCommand extends ChatCommand {

	public static final ChatCommand BED = new BedCommand();

	private BedCommand() {
		super(SettlementChat.SETTLEMENT_GROUP, "b", "bed", "Allocation of beds");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	public void execute(Conversation context, String input) {
		SettlementChat parent = (SettlementChat) context.getCurrentCommand();		
		Settlement settlement = parent.getSettlement();
		StructuredResponse response = new StructuredResponse();

		response.appendLabelledDigit("Total number of beds", settlement.getPopulationCapacity());
		response.appendLabelledDigit("Desginated beds", settlement.getTotalNumDesignatedBeds());
		response.appendLabelledDigit("Unoccupied beds", settlement.getUnoccupiedBeds());
		response.appendLabelledDigit("Occupied beds", settlement.getNumOccupiedSpots());
		
		context.println(response.getOutput());
	}

}
