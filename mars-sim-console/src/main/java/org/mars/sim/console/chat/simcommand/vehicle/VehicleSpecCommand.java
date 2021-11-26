/**
 * Mars Simulation Project
 * VehicleSpecCommand.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand.vehicle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Medical;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Command to get the specs of a vehicle
 * This is a singleton.
 */
public class VehicleSpecCommand extends ChatCommand {

	public static final ChatCommand SPEC = new VehicleSpecCommand();
	private static final String KM_KG_FORMAT = "%.2f km/kg";

	private VehicleSpecCommand() {
		super(VehicleChat.VEHICLE_GROUP, "spe", "specs", "What are the vehicle specs.");
	}

	/** 
	 * Output the current immediate location of the Unit
	 * @return 
	 */
	@Override
	public boolean execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		
		// Rovers has more capabilities.
		boolean isRover = (source instanceof Rover);

		StructuredResponse buffer = new StructuredResponse();
		buffer.appendLabeledString("Name", source.getName());
		buffer.appendLabeledString("Type", source.getVehicleTypeString());
		buffer.appendLabeledString("Description", source.getDescription());
		buffer.appendLabeledString("Base Mass", String.format(CommandHelper.KG_FORMAT, source.getBaseMass()));
		buffer.appendLabeledString("Base Speed", String.format(CommandHelper.KMPH_FORMAT,source.getBaseSpeed()));
		buffer.appendLabeledString("Drivetrain Efficiency", source.getDrivetrainEfficiency() + " kWh/km");

		int id = source.getFuelType();
		String fuelName = ResourceUtil.findAmountResourceName(id);
		buffer.appendLabeledString("Power Source",Conversion.capitalize(fuelName));

		if (source instanceof GroundVehicle) {
			GroundVehicle gv = (GroundVehicle) source;

			buffer.appendLabeledString("Fuel Capacity", String.format(CommandHelper.KG_FORMAT, source.getFuelCapacity()));
			buffer.appendLabeledString("Base Range Est.", String.format(CommandHelper.KM_FORMAT, source.getBaseRange()));
			buffer.appendLabeledString("Base Fuel Economy", String.format(KM_KG_FORMAT, source.getBaseFuelEconomy()));
			buffer.appendLabeledString("Est. Average Economy", String.format(KM_KG_FORMAT, source.getEstimatedAveFuelEconomy()));
			buffer.appendLabeledString("Actual Economy", String.format(KM_KG_FORMAT, source.getIFuelEconomy()));
			buffer.appendLabeledString("Terrain Handling", String.format("%.2f", gv.getTerrainHandlingCapability()));
		}

		if (source instanceof Crewable) {
			int crewSize = ((Crewable) source).getCrewCapacity();
			buffer.appendLabelledDigit("Crew Size", crewSize);
		}	

		if (isRover) {
			double cargo = ((Rover) source).getCargoCapacity();
			buffer.appendLabeledString("Cargo Capacity", String.format(CommandHelper.KG_FORMAT, cargo));
		}

		if (source instanceof Medical) {
			SickBay sickbay = ((Medical) source).getSickBay();
			if (sickbay != null) {
				buffer.appendLabelledDigit("# Beds (Sick Bay)", sickbay.getSickBedNum());
				buffer.appendLabelledDigit("Tech Level (Sick Bay)", sickbay.getTreatmentLevel());
			}
		}
		
		if (isRover) {
			Lab lab = ((Rover)source).getLab();
			if (lab != null) {
				buffer.appendLabelledDigit("Tech Level (Lab)", lab.getTechnologyLevel());
				buffer.appendLabelledDigit("Lab Size", lab.getLaboratorySize());
	
				ScienceType[] types = lab.getTechSpecialties();
				StringBuffer names = new StringBuffer();
				String prefix = "";
				for (ScienceType t : types) {
					names.append(prefix);
					prefix = ", ";
					names.append(t.getName());
				}
				buffer.appendLabeledString("Lab Specialties", names.toString());
			}
		}
		
		context.println(buffer.getOutput());
		return true;
	}

}
