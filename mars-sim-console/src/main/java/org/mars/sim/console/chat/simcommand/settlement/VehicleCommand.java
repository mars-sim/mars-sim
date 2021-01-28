package org.mars.sim.console.chat.simcommand.settlement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Vehicle;

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

		response.appendHeading("Summary");
		response.appendLabelledDigit("Total # of Rovers", settlement.getAllAssociatedVehicles().size());
		response.appendLabelledDigit("Cargo Rovers on Mission", settlement.getCargoRovers(2).size());
		response.appendLabelledDigit("Transports on Mission", settlement.getTransportRovers(2).size());
		response.appendLabelledDigit("Explorers on Mission", settlement.getExplorerRovers(2).size());
		response.appendLabelledDigit("LUVs on Mission", settlement.getLUVs(2).size());
		response.appendLabelledDigit("Rovers on Mission", settlement.getMissionVehicles().size());

		response.appendLabelledDigit("Parked/Garaged Cargo Rovers", settlement.getCargoRovers(1).size());
		response.appendLabelledDigit("Parked/Garaged Transports", settlement.getTransportRovers(1).size());
		response.appendLabelledDigit("Parked/Garaged Explorers", settlement.getExplorerRovers(1).size());
		response.appendLabelledDigit("Parked/Garaged LUVs", settlement.getLUVs(1).size());
		response.appendLabelledDigit("Rovers NOT on mission", settlement.getParkedVehicleNum());
		
		response.appendHeading("Vehicles");
		
		// Sort the vehicle list according to the type
		Collection<Vehicle> list = settlement.getAllAssociatedVehicles();
		List<Vehicle> vlist = list.stream().sorted((p1, p2) -> p1.getVehicleType().compareTo(p2.getVehicleType()))
				.collect(Collectors.toList());

		response.appendTableHeading("Name", CommandHelper.PERSON_WIDTH, "Type", 15, "Mission", 14, "Lead", CommandHelper.PERSON_WIDTH);

		for (Vehicle v : vlist) {

			String vTypeStr = Conversion.capitalize(v.getVehicleType());
			if (vTypeStr.equalsIgnoreCase("Light Utility Vehicle"))
				vTypeStr = "LUV";

			// Print mission name
			String missionName = "";
			Mission mission = null;
			List<Mission> missions = context.getSim().getMissionManager().getMissions();
			for (Mission m : missions) {
				if (m instanceof VehicleMission) {
					Vehicle vv = ((VehicleMission) m).getVehicle();
					if (vv.getName().equals(v.getName())) {
						mission = m;
						missionName = m.getDescription();
					}
				}
			}

			String personName = ((mission != null) ? 
									mission.getStartingMember().getName() : "");

			response.appendTableRow(v.getName(), vTypeStr, missionName, personName);
		}
		
		context.println(response.getOutput());
		return true;
	}
}
