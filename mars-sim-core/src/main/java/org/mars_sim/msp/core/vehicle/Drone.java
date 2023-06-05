/*
 * Mars Simulation Project
 * Drone.java
 * @date 2021-10-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.vehicle;

import java.util.Collection;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;

public class Drone extends Flyer {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Drone.class.getName());

	public static final int METHANOL_ID = ResourceUtil.methanolID;
	
	public static final AmountResource METHANOL_AR = ResourceUtil.methanolAR;

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
	 * @param spec the configuration type of the vehicle.
	 * @param settlement  the settlement the rover is parked at
	 */
	public Drone(String name, VehicleSpec spec, Settlement settlement) {
		super(name, spec, settlement, MAINTENANCE_WORK_TIME);
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

				if (isInSettlement()) {
					if (mission instanceof VehicleMission) {
						LoadingController lp = ((VehicleMission)mission).getLoadingPlan();

						if ((lp != null) && !lp.isCompleted()) {
							double time = pulse.getElapsed();
							double transferSpeed = 10; // Assume 10 kg per msol
							double amountLoading = time * transferSpeed;

							lp.backgroundLoad(amountLoading);
						}
					}
				}
			}
		}


		return true;
	}

	/**
	 * Gets the amount resource type that this vehicle uses as fuel
	 *
	 * @return amount resource
	 */
	public AmountResource getFuelTypeAR() {
		return METHANOL_AR;
	}
	
	/**
	 * Gets the range of the vehicle
	 *
	 * @return the range of the vehicle (in km)
	 * @throws Exception if error getting range.
	 */
	@Override
	public double getRange() {
		// Note: multiply by 0.9 would account for the extra distance travelled in between sites
		return super.getRange() * FUEL_RANGE_FACTOR;
	}
}
