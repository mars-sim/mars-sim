/*
 * Mars Simulation Project
 * Drone.java
 * @date 2024-07-20
 * @author Manny Kung
 */
package com.mars_sim.core.vehicle;

import java.util.Collection;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;

public class Drone extends Flyer {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Drone.class.getName());

	public static final int METHANOL_ID = ResourceUtil.methanolID;
	
	public static final AmountResource METHANOL_AR = ResourceUtil.methanolAR;

	/** The fuel range modifier. */
	public static final double FUEL_RANGE_FACTOR = 0.95;
	
	public static final double DRONE_PENALTY_FACTOR = .6;
	
	/** The mission range modifier. */
	public static final double MISSION_RANGE_FACTOR = 1.9;
	/** The amount of work time to perform maintenance (millisols) */
	public static final double MAINTENANCE_WORK_TIME = 100D;
	
	/**
	 * Constructs a Rover object at a given settlement.
	 *
	 * @param name        the name of the rover
	 * @param spec the configuration type of the vehicle.
	 * @param settlement  the settlement the rover is parked at
	 */
	public Drone(String name, VehicleSpec spec, Settlement settlement) {
		super(name, spec, settlement, MAINTENANCE_WORK_TIME);
	}


	/**
	 * Performs time-related processes.
	 *
	 * @param pulse the amount of clock pulse passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!super.timePassing(pulse)) {
			return false;
		}

		boolean onAMission = isOutsideOnMarsMission();
		if (onAMission || isReservedForMission()) {

			Mission mission = getMission();
			if (mission != null) {
				// This code feel wrong
				Collection<Worker> members = mission.getMembers();
				for (Worker m: members) {
					if (m.getMission() == null) {
						// Defensively set the mission in the case that the delivery bot is registered as a mission member
						// but its mission is null
						// Question: why would the mission be null for this member in the first place after loading from a saved sim
						logger.info(this, m.getName() + " reregistered for " + mission + ".");
						m.setMission(mission);
					}
				}
			}
		}


		return true;
	}

	/**
	 * Gets the amount resource type that this vehicle uses as fuel.
	 *
	 * @return amount resource
	 */
	public AmountResource getFuelTypeAR() {
		return METHANOL_AR;
	}
	
	/**
	 * Gets the range of the vehicle.
	 *
	 * @return the range of the vehicle (in km)
	 * @throws Exception if error getting range.
	 */
	@Override
	public double getRange() {
		// Note: multiply by 0.95 would account for the extra distance travelled in between sites
		double fuelRange = super.getEstimatedRange() * FUEL_RANGE_FACTOR * DRONE_PENALTY_FACTOR;

		// Battery also contributes to the range
		double cap = super.getBatteryCapacity();
		double percent = super.getBatteryPercent();
		double estFC = super.getEstimatedFuelConsumption();
		double batteryRange = cap * percent / 100 / estFC * 1000;
		
		return fuelRange + batteryRange;
	}
}
