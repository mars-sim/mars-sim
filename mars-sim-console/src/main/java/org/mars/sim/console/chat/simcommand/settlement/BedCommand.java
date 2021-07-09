/**
 * Mars Simulation Project
 * BedCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.List;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;

/**
 * Command to display bed allocation in a Settlement
 * This is a singleton.
 */
public class BedCommand extends AbstractSettlementCommand {

	public static final ChatCommand BED = new BedCommand();

	private BedCommand() {
		super("b", "bed", "Allocation of beds");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		int designedBeds = 0;
		int unoccupiedBeds = 0;
		int occupiedBeds =  0;
		
		List<Building> bs = settlement.getBuildingManager().getBuildings(FunctionType.LIVING_ACCOMMODATIONS);
		for (Building building : bs) {
			LivingAccommodations living = building.getLivingAccommodations();
			designedBeds += living.getNumAssignedBeds();
			unoccupiedBeds += living.getNumEmptyActivitySpots();
			occupiedBeds += living.getNumOccupiedActivitySpots();
		}
		
		response.appendLabelledDigit("Total number of beds", settlement.getPopulationCapacity());
		response.appendLabelledDigit("Desginated beds", designedBeds);
		response.appendLabelledDigit("Unoccupied beds", unoccupiedBeds);
		response.appendLabelledDigit("Occupied beds", occupiedBeds);
		
		context.println(response.getOutput());
		
		return true;
	}

}
