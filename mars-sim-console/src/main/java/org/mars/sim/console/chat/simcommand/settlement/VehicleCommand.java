/**
 * Mars Simulation Project
 * VehicleCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Command to display vehicles
 * This is a singleton.
 */
public class VehicleCommand extends AbstractSettlementCommand {

	public static final ChatCommand VEHICLE = new VehicleCommand();

	private VehicleCommand() {
		super("v", "vehicles", "Vehicle list");
	}

	/** 
	 * Output the answer
	 */
	@Override
	protected boolean execute(Conversation context, String input, Settlement settlement) {
		StructuredResponse response = new StructuredResponse();

		int parkedRovers = 0;
		int parkedExplorers = 0;
		int parkedCargo = 0;
		int parkedLUV = 0;
		for (Vehicle vehicle : settlement.getParkedVehicles()) {
			String d = vehicle.getVehicleType();
			if (d.equals(VehicleType.TRANSPORT_ROVER.getName()))
				parkedRovers++;
			else if (d.equals(VehicleType.EXPLORER_ROVER.getName()))
				parkedExplorers++;
			else if (d.equals(VehicleType.CARGO_ROVER.getName()))
				parkedCargo++;
			else if (vehicle instanceof LightUtilityVehicle)
				parkedLUV++;
		}

		int missionRovers = 0;
		int missionExplorers = 0;
		int missionCargo = 0;
		int missionLUV = 0;
		for (Vehicle vehicle : settlement.getMissionVehicles()) {
			String d = vehicle.getVehicleType();
			if (d.equals(VehicleType.TRANSPORT_ROVER.getName()))
				missionRovers++;
			else if (d.equals(VehicleType.EXPLORER_ROVER.getName()))
				missionExplorers++;
			else if (d.equals(VehicleType.CARGO_ROVER.getName()))
				missionCargo++;
			else if (vehicle instanceof LightUtilityVehicle)
				missionLUV++;
		}
		
		response.appendHeading("Summary");
		response.appendLabelledDigit("Total # of Rovers", settlement.getAllAssociatedVehicles().size());
		response.appendLabelledDigit("Cargo Rovers on Mission", missionCargo);
		response.appendLabelledDigit("Transports on Mission", missionRovers);
		response.appendLabelledDigit("Explorers on Mission", missionExplorers);
		response.appendLabelledDigit("LUVs on Mission", missionLUV);
		response.appendLabelledDigit("Rovers on Mission", settlement.getMissionVehicles().size());

		response.appendLabelledDigit("Parked/Garaged Cargo Rovers", parkedCargo);
		response.appendLabelledDigit("Parked/Garaged Transports", parkedRovers);
		response.appendLabelledDigit("Parked/Garaged Explorers", parkedExplorers);
		response.appendLabelledDigit("Parked/Garaged LUVs", parkedLUV);
		response.appendLabelledDigit("Rovers NOT on mission", settlement.getParkedVehicleNum());
		
		response.appendHeading("Vehicles");
		
		// Sort the vehicle list according to the type
		Collection<Vehicle> list = settlement.getAllAssociatedVehicles();
		List<Vehicle> vlist = list.stream().sorted((p1, p2) -> p1.getVehicleType().compareTo(p2.getVehicleType()))
				.collect(Collectors.toList());

		response.appendTableHeading("Name", CommandHelper.PERSON_WIDTH, "Type", 15, "Mission", 25, "Lead", CommandHelper.PERSON_WIDTH);

		for (Vehicle v : vlist) {

			String vTypeStr = Conversion.capitalize(v.getVehicleType());
			if (vTypeStr.equalsIgnoreCase("Light Utility Vehicle"))
				vTypeStr = "LUV";

			// Print mission name
			String missionName = "";
			Mission mission = context.getSim().getMissionManager().getMissionForVehicle(v);
			String personName = ((mission != null) ? 
									mission.getStartingPerson().getName() : "");

			response.appendTableRow(v.getName(), vTypeStr, missionName, personName);
		}
		
		context.println(response.getOutput());
		return true;
	}
}
