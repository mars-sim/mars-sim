/**
 * Mars Simulation Project
 * Drone.java
 * @version 3.2.0 2021-06-20
 * @author Manny
 */

package org.mars_sim.msp.core.vehicle;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;

public class Drone extends Flyer implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Drone.class.getName());
	
	/** Vehicle name. */
	public static final String NAME = VehicleType.DELIVERY_DRONE.getName();
	
	/** The fuel range modifier. */
	public static final double FUEL_RANGE_FACTOR = 0.95;
	/** The mission range modifier. */  
	public static final double MISSION_RANGE_FACTOR = 1.9;
	/** The amount of work time to perform maintenance (millisols) */
	public static final double MAINTENANCE_WORK_TIME = 100D;
	
	/**
	 * Constructs a Rover object at a given settlement
	 * 
	 * @param name        the name of the rover
	 * @param type the configuration type of the vehicle.
	 * @param settlement  the settlement the rover is parked at
	 */
	public Drone(String name, String type, Settlement settlement) {
		super(name, type, settlement, MAINTENANCE_WORK_TIME);
		
		VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
//		if (vehicleConfig.hasPartAttachments(type)) {
//			attachments = vehicleConfig.getAttachableParts(type);
//			slotNumber = vehicleConfig.getPartAttachmentSlotNumber(type);
//		}

		Inventory inv = getInventory();
		inv.addGeneralCapacity(vehicleConfig.getTotalCapacity(type));
		
		// Set inventory resource capacities.
		inv.addAmountResourceTypeCapacity(ResourceUtil.methaneID, 
				vehicleConfig.getCargoCapacity(type, ResourceUtil.METHANE));
	}

	
	/**
	 * Perform time-related processes
	 * 
	 * @param pulse the amount of clock pulse passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!super.timePassing(pulse)) {
			return false;
		}

		boolean onAMission = isOnAMission();
		if (onAMission || isReservedForMission()) {
		
			Mission mission = getMission();
			Collection<MissionMember> members = mission.getMembers();
			for (MissionMember m: members) {
				if (m.getMission() == null) {
					// Defensively set the mission in the case that the deliverybot is registered as a mission member 
					// but its mission is null 
					// Question: why would the mission be null for this member in the first place after loading from a saved sim
					logger.info(this, m.getName() + " reregistered for " + mission + ".");
					m.setMission(mission);
				}
			}
			
			if (isInSettlement()) {
//				plugInTemperature(pulse.getElapsed());
//				plugInAirPressure(pulse.getElapsed());	
			}
			
			if (getInventory().getAmountResourceStored(getFuelType(), false) > Flyer.LEAST_AMOUNT) {
				if (super.haveStatusType(StatusType.OUT_OF_FUEL))
					super.removeStatus(StatusType.OUT_OF_FUEL);
			}
		}

		
		return true;
	}
	
	
	/**
	 * Checks if a particular operator is appropriate for the drone.
	 * 
	 * @param operator the operator to check
	 * @return true if appropriate operator has been designated for this drone.
	 */
	@Override
	public boolean isAppropriateOperator(VehicleOperator operator) {
		return operator instanceof Person || operator instanceof Robot;
	}

	/**
	 * Gets the resource type id that this vehicle uses as fuel, namely, methane
	 * 
	 * @return resource type id
	 */
	@Override
	public int getFuelType() {
		return ResourceUtil.methaneID;
	}
	
	/**
	 * Gets the range of the vehicle
	 * 
	 * @return the range of the vehicle (in km)
	 * @throws Exception if error getting range.
	 */
	public double getRange(MissionType missionType) {
		// Note: multiply by 0.9 would account for the extra distance travelled in between sites 
		double fuelRange = super.getRange(missionType) * FUEL_RANGE_FACTOR;
		// Obtains the max mission range [in km] based on the type of mission
		// Note: total route ~= mission radius * 2   
		double missionRange = super.getMissionRange(missionType) * MISSION_RANGE_FACTOR;
		
		return Math.min(missionRange, fuelRange);
	}
}
