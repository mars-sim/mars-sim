/**
 * Mars Simulation Project
 * DroneMission.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.util.Iterator;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.PilotDrone;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

public abstract class DroneMission extends VehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Drone.class.getName());
	
	// Data members
	private Settlement startingSettlement;


	/**
	 * Constructor.
	 * 
	 * @param name           the name of the mission.
	 * @param startingMember the mission member starting the mission.
	 */
	protected DroneMission(String name, MissionType missionType, MissionMember startingMember) {
		// Use VehicleMission constructor.
		super(name, missionType, startingMember, 2);
	}
	
	/**
	 * Constructor with min people and drone. Initiated by MissionDataBean.
	 * 
	 * @param missionName    the name of the mission.
	 * @param startingMember the mission member starting the mission.
	 * @param minPeople      the minimum number of people required for mission.
	 * @param drone          the drone to use on the mission.
	 */
	protected DroneMission(String missionName, MissionType missionType, MissionMember startingMember, int minPeople, Drone drone) {
		// Use VehicleMission constructor.
		super(missionName, missionType, startingMember, minPeople, drone);
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
	 * Sets the starting settlement.
	 * 
	 * @param startingSettlement the new starting settlement
	 */
	protected final void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
		fireMissionUpdate(MissionEventType.STARTING_SETTLEMENT_EVENT);
	}

	/**
	 * Gets the starting settlement.
	 * 
	 * @return starting settlement
	 */
	public final Settlement getStartingSettlement() {
		return startingSettlement;
	}

	/**
	 * Gets the available vehicle at the settlement with the greatest range.
	 * 
	 * @param settlement         the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return vehicle or null if none available.
	 * @throws Exception if error finding vehicles.
	 */
	public static Drone getDroneWithGreatestRange(MissionType missionType, Settlement settlement, boolean allowMaintReserved) {
		Drone result = null;

		Iterator<Drone> i = settlement.getParkedDrones().iterator();
		while (i.hasNext()) {
			Drone drone = i.next();

			boolean usable = !drone.isReservedForMission();

            if (!allowMaintReserved && drone.isReserved())
				usable = false;

			usable = drone.isVehicleReady();
			
			if (drone.getStoredMass() > 0D)
				usable = false;
			
			if (usable) {
				if (result == null)
					// so far, this is the first vehicle being picked
					result = drone;
				else if (drone.getRange(missionType) > result.getRange(missionType))
					// This vehicle has a better range than the previously selected vehicle
					result = drone;
			}
		}

		return result;
	}

	/**
	 * Checks to see if any drones are available at a settlement.
	 * 
	 * @param settlement         the settlement to check.
	 * @param allowMaintReserved allow drones that are reserved for maintenance.
	 * @return true if drones are available.
	 */
	public static boolean areDronesAvailable(Settlement settlement, boolean allowMaintReserved) {

		boolean result = false;

		Iterator<Drone> i = settlement.getParkedDrones().iterator();
		while (i.hasNext()) {
			Drone drone = i.next();

			boolean usable = !drone.isReservedForMission();

            if (!allowMaintReserved && drone.isReserved())
				usable = false;
			
			usable = drone.isVehicleReady();
				
			if (drone.getStoredMass() > 0D)
				usable = false;

			if (usable)
				result = true;
		}

		return result;
	}

	/**
	 * Checks if vehicle is usable for this mission. (This method should be
	 * overridden by children)
	 * 
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws MissionException if problem determining if vehicle is usable.
	 */
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
	protected OperateVehicle createOperateVehicleTask(MissionMember member, TaskPhase lastOperateVehicleTaskPhase) {
		OperateVehicle result = null;
		if (member instanceof Person) {
			Person person = (Person) member;
			Drone d = (Drone) getDrone();
			// Note : should it check for fatigue
//			if (person.getFatigue() < 750) {
			if (!d.haveStatusType(StatusType.OUT_OF_FUEL)) {
				if (lastOperateVehicleTaskPhase != null) {
					result = new PilotDrone(person, getDrone(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
				} else {
					result = new PilotDrone(person, getDrone(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance());
				}
			}
		}

		else if (member instanceof Robot) {
			Robot robot = (Robot) member;
			Drone d = (Drone) getDrone();
			if (!d.haveStatusType(StatusType.OUT_OF_FUEL)) {
				if (lastOperateVehicleTaskPhase != null) {
					result = new PilotDrone(robot, getDrone(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
				} else {
					result = new PilotDrone(robot, getDrone(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance());
				}
			}
		}
		
		return result;
	}

	/**
	 * Performs the embark from settlement phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission
	 */
	@Override
	protected void performEmbarkFromSettlementPhase(MissionMember member) {
		Vehicle v = getVehicle();
		
		if (v == null) {
			addMissionStatus(MissionStatus.NO_AVAILABLE_VEHICLES);
			endMission();
			return;
		}
			
		Settlement settlement = v.getSettlement();
		if (settlement == null) {
			logger.warning(Msg.getString("RoverMission.log.notAtSettlement", getPhase().getName())); //$NON-NLS-1$
			addMissionStatus(MissionStatus.NO_AVAILABLE_VEHICLES);
			endMission();
			return;
		}
		
		// While still in the settlement, check if the beacon is turned on and and endMission()
		else if (v.isBeaconOn()) {
			endMission();
			return;
		}

		// Add the drone to a garage if possible.
		boolean	isDroneInAGarage = settlement.getBuildingManager().addToGarage(v);

		// Load vehicle if not fully loaded.
		if (!isVehicleLoaded()) {
			// Check if vehicle can hold enough supplies for mission.
			if (isVehicleLoadable()) {
				
				if (member.isInSettlement()) {
					// Load drone
					// Random chance of having person load (this allows person to do other things
					// sometimes)
					if (RandomUtil.lessThanRandPercent(50)) {
						if (member instanceof Person) {
							Person person = (Person) member;
							if (isDroneInAGarage) {
								// TODO Refactor.
								assignTask(person,
											new LoadVehicleGarage(person, this));
							} else {
								// Check if it is day time.
//										if (!EVAOperation.isGettingDark(person)) {
									assignTask(person, new LoadVehicleEVA(person, this));
//										}
							}
						}
					}
				}
				else {
					if (member instanceof Person) {
						Person person = (Person) member;
						// Check if it is day time.
//								if (!EVAOperation.isGettingDark(person)) {
							assignTask(person, new LoadVehicleEVA(person, this));
//								}
					}
				}
				
			} else {
				addMissionStatus(MissionStatus.CANNOT_LOAD_RESOURCES);
				endMission();
				return;
			}
		}
		
		else {

			// Gets a random location within drone.
//			Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(v);
//			Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(),
//					vehicleLoc.getY(), v);

			// If drone is loaded and everyone is aboard, embark from settlement.
			if (!isDone()) {
				
				// Set the members' work shift to on-call to get ready
				for (MissionMember m : getMembers()) {
					if (m instanceof Person) {
						Person pp = (Person) m;
						if (pp.getShiftType() != ShiftType.ON_CALL)
							pp.setShiftType(ShiftType.ON_CALL);
					}
				}

				// Remove from garage if in garage.
				Building garage = BuildingManager.getBuilding(v);
				if (garage != null) {
					garage.getVehicleMaintenance().removeVehicle(v);
				}

				// Record the start mass right before departing the settlement
				recordStartMass();
				
				// Embark from settlement
				if (v.transfer(unitManager.getMarsSurface())) {
					setPhaseEnded(true);
				}
				else {
					addMissionStatus(MissionStatus.COULD_NOT_EXIT_SETTLEMENT);
					endMission();
				}
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
	protected void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement) {
		
		Vehicle v0 = getVehicle();
		disembark(member, v0, disembarkSettlement);
		
//		// If v0 is being towed by a vehicle, gets the towing vehicle
//		Vehicle v1 = v0.getTowingVehicle();
//		if (v1 != null)
//			disembark(member, v1, disembarkSettlement);
//		
//		// If v0 is towing a vehicle, gets the towed vehicle
//		Vehicle v2 = ((Drone)v0).getTowedVehicle();
//		if (v2 != null)
//			disembark(member, v2, disembarkSettlement);
	}
	
	/**
	 * Disembarks the vehicle and unload cargo upon arrival
	 * 
	 * @param member
	 * @param v
	 * @param disembarkSettlement
	 */
	public void disembark(MissionMember member, Vehicle v, Settlement disembarkSettlement) {
		logger.log(v, Level.INFO, 10_000, 
				"Disemabarked at " + disembarkSettlement.getName() + ".");
		
		Drone drone = (Drone) v;

		if (v != null) {
			// Add vehicle to a garage if available.
			boolean inAGarage = disembarkSettlement.getBuildingManager().addToGarage(v);
			Settlement currentSettlement = v.getSettlement();
			if ((currentSettlement == null) || !currentSettlement.equals(disembarkSettlement)) {
				// If drone has not been parked at settlement, park it.
				disembarkSettlement.addParkedVehicle(v);	
			}

			// Make sure the drone chasis is not overlapping a building structure in the settlement map
	        if (!isInAGarage())
	        	drone.findNewParkingLoc();

			// Reset the vehicle reservation
			v.correctVehicleReservation();

			// Unload drone if necessary.
			boolean droneUnloaded = drone.getStoredMass() == 0D;
			
			if (!droneUnloaded) {
				
				boolean result = false;
				// Alert the people in the disembarked settlement to unload cargo
				for (Person person: disembarkSettlement.getIndoorPeople()) {
					if (person.isInSettlement() && person.isFit()) {
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

				// If the drone is in a garage, put the drone outside.
				if (inAGarage) {
					Building garage = BuildingManager.getBuilding(v);
					if (garage != null)
						garage.getVehicleMaintenance().removeVehicle(v);
				}

				// Leave the vehicle.
				leaveVehicle();
				setPhaseEnded(true);
			}
		}
	}
	
	/**
	 * Give a person the task from unloading the drone
	 * 
	 * @param p
	 * @param drone
	 */
	private boolean unloadCargo(Person p, Drone drone) {
		boolean result = false;
		if (isInAGarage()) {
			result = assignTask(p, new UnloadVehicleGarage(p, drone));
		} 
		
		else {
			// Check if it is day time.
			if (!EVAOperation.isGettingDark(p)) {
				result = assignTask(p, new UnloadVehicleEVA(p, drone));
			}
		}
		return result;
	}
	
	/**
	 * Checks if the drone is currently in a garage or not.
	 * 
	 * @return true if drone is in a garage.
	 */
	protected boolean isInAGarage() {
		return BuildingManager.isInAGarage(getVehicle());
	}
	
	@Override
	protected boolean recruitMembersForMission(MissionMember startingMember, boolean sameSettlement) {
		// Get all people qualified for the mission.
		Iterator<Robot> r = startingSettlement.getRobots().iterator();
		while (r.hasNext()) {
			Robot robot = r.next();
			if (robot.getRobotType() == RobotType.DELIVERYBOT) {
				setMembers(robot);
			}
		}
		
		super.recruitMembersForMission(startingMember, sameSettlement);

		return true;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		startingSettlement = null;
	}
}
