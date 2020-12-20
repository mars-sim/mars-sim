package org.mars.sim.console.chat.simcommand.vehicle;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Crewable;
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
	private static final String ONE_COLUMN = "%28s : %s%n";
	private static final String ONE_DIGITCOLUMN = "%28s : %d%n";

	private VehicleSpecCommand() {
		super(VehicleChat.VEHICLE_GROUP, "sp", "specs", "What are the vehicle specs.");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	public void execute(Conversation context, String input) {
		VehicleChat parent = (VehicleChat) context.getCurrentCommand();
		Vehicle source = parent.getVehicle();
		
		// Rovers has more capabilities.
		boolean isRover = (source instanceof Rover);

		StringBuffer buffer = new StringBuffer();
		buffer.append(String.format(ONE_COLUMN, "Name", source.getName()));
		buffer.append(String.format(ONE_COLUMN, "Type", source.getVehicleType()));
		buffer.append(String.format(ONE_COLUMN, "Description", source.getDescription()));
		buffer.append(String.format(ONE_COLUMN, "Base Mass", source.getBaseMass() + " kg"));
		buffer.append(String.format(ONE_COLUMN, "Base Speed", source.getBaseSpeed() + " km/h"));
		buffer.append(String.format(ONE_COLUMN, "Drivetrain Efficiency", source.getDrivetrainEfficiency() + " kWh/km"));

		// Needs reworking as ResourceType is ID not a class !! 
		String fuel = "Electrical Battery";
		if (isRover) {
			int id = ((Rover) source).getFuelType();
			String fuelName = ResourceUtil.findAmountResourceName(id);
			fuel = Conversion.capitalize(fuelName) + " (Solid Oxide Fuel Cell)";

			buffer.append(String.format(ONE_COLUMN, "Power Source", fuel));
			buffer.append(String.format(ONE_COLUMN, "Fuel Capacity", source.getFuelCapacity() + " kg"));
			buffer.append(String.format(ONE_COLUMN, "Base Range",
								Math.round(source.getBaseRange() * 100.0) / 100.0 + " km (Estimated)"));
			buffer.append(String.format(ONE_COLUMN, "Base Fuel Consumption", 
								Math.round(source.getBaseFuelEconomy() * 100.0) / 100.0 + " km/kg (Estimated)"));
		}
		else {
			buffer.append(String.format(ONE_COLUMN, "Power Source", fuel));
		}

		if (source instanceof Crewable) {
			int crewSize = ((Crewable) source).getCrewCapacity();
			buffer.append(String.format(ONE_DIGITCOLUMN, "Crew Size", crewSize));
		}	

		if (isRover) {
			double cargo = ((Rover) source).getCargoCapacity();
			buffer.append(String.format(ONE_COLUMN, "Cargo Capacity", cargo + " kg"));
		}

		if (source instanceof Medical) {
			SickBay sickbay = ((Medical) source).getSickBay();
			if (sickbay != null) {
				buffer.append(String.format(ONE_DIGITCOLUMN, "# Beds (Sick Bay)", sickbay.getSickBedNum()));
				buffer.append(String.format(ONE_DIGITCOLUMN, "Tech Level (Sick Bay)", sickbay.getTreatmentLevel()));
				
			}
		}
		
		if (isRover) {
			Lab lab = ((Rover)source).getLab();
			if (lab != null) {
				buffer.append(String.format(ONE_DIGITCOLUMN, "Tech Level (Lab)", lab.getTechnologyLevel()));
				buffer.append(String.format(ONE_DIGITCOLUMN, "Lab Size", lab.getLaboratorySize()));
	
				ScienceType[] types = lab.getTechSpecialties();
				StringBuffer names = new StringBuffer();
				String prefix = "";
				for (ScienceType t : types) {
					names.append(prefix);
					prefix = ", ";
					names.append(t.getName());
				}
				buffer.append(String.format(ONE_COLUMN, "Lab Specialties", names.toString()));
			}
		}
		
		context.println(buffer.toString());
	}

}
