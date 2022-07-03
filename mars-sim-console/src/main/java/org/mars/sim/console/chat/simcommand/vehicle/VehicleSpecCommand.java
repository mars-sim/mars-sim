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
import org.mars_sim.msp.core.vehicle.Flyer;
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
	private static final String KM_PER_KG_FORMAT = "%.2f km/kg";
	private static final String WH_PER_KM_FORMAT = "%.2f Wh/km";
	private static final String KWh_FORMAT = "%.2f kWh";
	private static final String M_PER_S_FORMAT = "%.2f m/s^2";
	
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
		boolean isDrone = (source instanceof Flyer);
		
		StructuredResponse buffer = new StructuredResponse();
		buffer.appendLabeledString("Name", source.getName());
		buffer.appendLabeledString("Type", Conversion.capitalize(source.getVehicleTypeString()));
		buffer.appendLabeledString("Description", Conversion.capitalize(source.getDescription()));
		buffer.appendLabeledString("Base Mass", String.format(CommandHelper.KG_FORMAT, source.getBaseMass()));
		buffer.appendLabeledString("Base Speed", String.format(CommandHelper.KMPH_FORMAT,source.getBaseSpeed()));
		buffer.appendLabeledString("Drivetrain Efficiency", source.getDrivetrainEfficiency() + "");

		int id = source.getFuelType();
		String fuelName = ResourceUtil.findAmountResourceName(id);
		buffer.appendLabeledString("Power Source", Conversion.capitalize(fuelName));

		buffer.appendLabeledString("Fuel Capacity", String.format(CommandHelper.KG_FORMAT, source.getFuelCapacity()));
		buffer.appendLabeledString("Energy Capacity", String.format(KWh_FORMAT, source.getEnergyCapacity()));		
		buffer.appendLabeledString("Drivetrain Energy", String.format(KWh_FORMAT, source.getDrivetrainEnergy()));
		buffer.appendLabeledString("Base Acceleration", String.format(M_PER_S_FORMAT, source.getAccel()));
		buffer.appendLabeledString("averagePower", String.format("%.2f kW", source.getAveragePower()));
		buffer.appendLabeledString("Base Range", String.format(CommandHelper.KM_FORMAT, source.getBaseRange()));
	
		if (source instanceof GroundVehicle) {
			GroundVehicle gv = (GroundVehicle) source;
			buffer.appendLabeledString("Terrain Handling", String.format("%.2f", gv.getTerrainHandlingCapability()));
		}
		
		buffer.appendLabeledString("Base Fuel Economy", String.format(KM_PER_KG_FORMAT, source.getBaseFuelEconomy()));
		buffer.appendLabeledString("Estimated Fuel Economy", String.format(KM_PER_KG_FORMAT, source.getEstimatedFuelEconomy()));
		buffer.appendLabeledString("Initial Fuel Economy", String.format(KM_PER_KG_FORMAT, source.getInitialFuelEconomy()));
		buffer.appendLabeledString("Instantaneous Fuel Economy", String.format(KM_PER_KG_FORMAT, source.getIFuelEconomy()));
		buffer.appendLabeledString("Cumulative Fuel Economy", String.format(KM_PER_KG_FORMAT, source.getCumFuelEconomy()));
		buffer.appendLabeledString("Base Fuel Consumption", String.format(WH_PER_KM_FORMAT, source.getBaseFuelConsumption()));
		buffer.appendLabeledString("Instantaneous Fuel Consumption", String.format(WH_PER_KM_FORMAT, source.getIFuelConsumption()));
		buffer.appendLabeledString("Cumulative Fuel Consumption", String.format(WH_PER_KM_FORMAT, source.getCumFuelConsumption()));	
	
		if (source instanceof Crewable) {
			int crewSize = ((Crewable) source).getCrewCapacity();
			buffer.appendLabelledDigit("Crew Size", crewSize);
		}	

		if (isRover || isDrone) {
			buffer.appendLabeledString("Cargo Capacity", String.format(CommandHelper.KG_FORMAT, source.getCargoCapacity()));
			buffer.appendLabeledString("Odometer Distance", String.format(CommandHelper.KM_FORMAT, source.getOdometerMileage()));	
			buffer.appendLabeledString("Cumulative Fuel Usage", String.format(CommandHelper.KG_FORMAT, source.getFuelCumulativeUsage()));	
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
