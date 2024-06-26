/**
 * Mars Simulation Project
 * DroneMission.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.OperateVehicle;
import com.mars_sim.core.vehicle.task.PilotDrone;
import com.mars_sim.core.vehicle.task.UnloadVehicleEVA;
import com.mars_sim.core.vehicle.task.UnloadVehicleGarage;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

public abstract class DroneMission extends AbstractVehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(DroneMission.class.getName());

	/**
	 * Constructor with min people and drone. Initiated by MissionDataBean.
	 *
	 * @param missionType    the type of the mission.
	 * @param startingMember the mission member starting the mission.
	 * @param minPeople      the minimum number of people required for mission.
	 * @param drone          the drone to use on the mission.
	 */
	protected DroneMission(MissionType missionType, Worker startingMember, Drone drone) {
		// Use VehicleMission constructor.
		super(missionType, startingMember, drone);
	}

	/**
	 * Gets the mission's drone if there is one.
	 *
	 * @return drone or null if none.
	 */
	public final Drone getDrone() {
		return (Drone) getVehicle();
	}

	/**
	 * Gets a collection of available Drones at a settlement that are usable for
	 * this mission.
	 *
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 */
	@Override
	protected Collection<Vehicle> getAvailableVehicles(Settlement settlement) {
		Collection<Vehicle> result = new ArrayList<>();
		Collection<Drone> list = settlement.getParkedGaragedDrones();
		if (list.isEmpty())
			return result;
		for (Drone v : list) {
			if (!v.haveStatusType(StatusType.MAINTENANCE)
					&& !v.getMalfunctionManager().hasMalfunction()
					&& isUsableVehicle(v)
					&& !v.isReserved()) {
				result.add(v);
			}
		}
		return result;
	}
	
	/**
	 * Gets the available vehicle at the settlement with the greatest range.
	 *
	 * @param settlement         the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return vehicle or null if none available.
	 * @throws Exception if error finding vehicles.
	 */
	public static Drone getDroneWithGreatestRange(Settlement settlement, boolean allowMaintReserved) {
		Drone bestDrone = null;
		double bestRange = 0D;

		for(Drone drone : settlement.getParkedGaragedDrones()) {

			boolean usable = !drone.isReservedForMission();
            usable = usable && (allowMaintReserved || !drone.isReserved());
			usable = usable && drone.isVehicleReady();
			usable = usable && (drone.getStoredMass() == 0);

			if (usable) {
				double range = drone.getRange();
				if ((bestDrone == null) || (bestRange > range)) {
					bestDrone = drone;
					bestRange = range;
				}
			}
		}

		return bestDrone;
	}

	/**
	 * Checks if vehicle is usable for this mission. (This method should be
	 * overridden by children).
	 *
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws MissionException if problem determining if vehicle is usable.
	 */
	@Override
	protected boolean isUsableVehicle(Vehicle newVehicle) {
		boolean usable = super.isUsableVehicle(newVehicle);
		if (!(newVehicle instanceof Drone))
			usable = false;
		return usable;
	}

	/**
	 * Gets a new instance of an OperateVehicle task for the mission member.
	 *
	 * @param member the mission member operating the vehicle.
	 * @return an OperateVehicle task for the person.
	 */
	@Override
	protected OperateVehicle createOperateVehicleTask(Worker member, TaskPhase lastOperateVehicleTaskPhase) {
		OperateVehicle result = null;
		
		if ((member instanceof Robot robot) 
				&& !robot.getSystemCondition().isBatteryAbove(10)) {
				return null;
		}
		
		Drone d = getDrone();
		if (!d.haveStatusType(StatusType.OUT_OF_FUEL)) {
			if (lastOperateVehicleTaskPhase != null) {
				result = new PilotDrone(member, getDrone(), getNextNavpoint().getLocation(),
						getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
			} else {
				result = new PilotDrone(member, getDrone(), getNextNavpoint().getLocation(),
						getCurrentLegStartingTime(), getCurrentLegDistance());
			}
		}
		else {
			logger.warning(d, 10_000L, "Out of fuel. Quit assigning the driving task.");
			return null;
		}

		return result;
	}

	/**
	 * Performs the embark from settlement phase of the mission.
	 *
	 * @param member the mission member currently performing the mission
	 */
	@Override
	protected void performDepartingFromSettlementPhase(Worker member) {
		Vehicle v = getVehicle();

		if (v == null) {
			endMission(NO_AVAILABLE_VEHICLES);
			return;
		}

		Settlement settlement = v.getSettlement();
		if (settlement == null) {
			logger.warning(Msg.getString("RoverMission.log.notAtSettlement", getPhase().getName())); //$NON-NLS-1$
			endMission(NO_AVAILABLE_VEHICLES);
			return;
		}

		// While still in the settlement, check if the beacon is turned on and and endMission()
		else if (v.isBeaconOn()) {
			endMission(VEHICLE_BEACON_ACTIVE);
			return;
		}

		// If drone is loaded and everyone is aboard, embark from settlement.
		if (!isDone()) {

			// Set the members' work shift to on-call to get ready. No deadline
			callMembersToMission(0);
			
			// Record the start mass right before departing the settlement
			recordStartMass();

			// Embark from settlement
			if (v.transfer(unitManager.getMarsSurface())) {
				setPhaseEnded(true);
			}
			else {
				endMissionProblem(v, "Could not transfer to Mars Surface.");
			}
		}
	}

	/**
	 * Performs the disembark to settlement phase of the mission.
	 *
	 * @param member              the mission member currently performing the
	 *                            mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 */
	@Override
	protected void performDisembarkToSettlementPhase(Worker member, Settlement disembarkSettlement) {

		Vehicle v0 = getVehicle();
		disembark(member, v0, disembarkSettlement);
	}

	/**
	 * Disembarks the vehicle and unload cargo upon arrival.
	 *
	 * @param member
	 * @param v
	 * @param disembarkSettlement
	 */
	public void disembark(Worker member, Vehicle v, Settlement disembarkSettlement) {
		logger.log(v, Level.INFO, 10_000,
				"Disemabarked at " + disembarkSettlement.getName() + ".");

		Drone drone = (Drone) v;

		if (v != null) {
			Settlement currentSettlement = v.getSettlement();
			if ((currentSettlement == null) || !currentSettlement.equals(disembarkSettlement)) {
				// If drone has not been parked at settlement, park it.
				v.transfer(disembarkSettlement);
			}

			// Add vehicle to a garage if available.
			disembarkSettlement.getBuildingManager().addToGarage(v);

			// Unload drone if necessary.
			boolean droneUnloaded = drone.getStoredMass() == 0D;

			if (!droneUnloaded) {

				boolean result = false;
				// Alert the people in the disembarked settlement to unload cargo
				for (Person person: disembarkSettlement.getIndoorPeople()) {
					if (person.isInSettlement()) {
						// Note : Random chance of having person unload (this allows person to do other things
						// sometimes)
						if (RandomUtil.lessThanRandPercent(50)) {
							result = unloadCargo(person, drone);
						}
					}
					if (result)
						break;
				}
			}

			else {
				// End the phase.

				// If the rover is in a garage, put the rover outside.
				BuildingManager.removeFromGarage(v);

				// Leave the vehicle.
				releaseVehicle(getVehicle());
				setPhaseEnded(true);
			}
		}
	}

	/**
	 * Gives a person the task from unloading the drone.
	 *
	 * @param p
	 * @param drone
	 */
	private boolean unloadCargo(Person person, Drone drone) {
		boolean result = false;
		
		if (person.getAssociatedSettlement().getBuildingManager().addToGarage(drone)) {
			assignTask(person, new UnloadVehicleGarage(person, drone));
		} else if (!EVAOperation.isGettingDark(person) && !person.isSuperUnfit()) {
			assignTask(person, new UnloadVehicleEVA(person, drone));
		}

		return result;
	}

	/**
	 * Checks if the drone is currently in a garage or not.
	 *
	 * @return true if drone is in a garage.
	 */
	protected boolean isInAGarage() {
		return getVehicle().isInGarage();
	}

	@Override
	protected boolean recruitMembersForMission(Worker startingMember, boolean sameSettlement, int minMembers) {
		// Get all people qualified for the mission.
		Iterator<Robot> r = getStartingSettlement().getRobots().iterator();
		while (r.hasNext()) {
			Robot robot = r.next();
			if (robot.getRobotType() == RobotType.DELIVERYBOT) {
				addMember(robot);
			}
		}

		super.recruitMembersForMission(startingMember, sameSettlement, minMembers);

		return true;
	}
}
