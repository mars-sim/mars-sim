/*
 * Mars Simulation Project
 * RoverMission.java
 * @date 2021-10-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EVASuitUtil;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.RequestMedicalTreatment;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleMeta;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A mission that involves driving a rover vehicle along a series of navpoints.
 */
public abstract class RoverMission extends AbstractVehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RoverMission.class.getName());

	// Static members
	public static final int MIN_STAYING_MEMBERS = 1;
	public static final int MIN_GOING_MEMBERS = 2;
	
	/* The marginal factor for the amount of water to be brought during a mission. */
	private static final double WATER_MARGIN = 1.5;
	
	/* The marginal factor for the amount of oxygen to be brought during a mission. */
	private static final double OXYGEN_MARGIN = 1.25;
	
	/* The marginal factor for the amount of food to be brought during a mission. */
	private static final double FOOD_MARGIN = 1.75;

	/* What is the lowest fullness of an EVASuit to be usable. */
	public static final double EVA_LOWEST_FILL = 0.5D;

	/* The factor for determining how many more EVA suits are needed for a trip. */
	private static final double EXTRA_EVA_SUIT_FACTOR = .2;

	/* How long do Worker have to complete departure */
	private static final int DEPARTURE_DURATION = 150;
	private static final int DEPARTURE_PREPARATION = 15;

	private boolean justArrived;
	
	/**
	 * Constructor with min people and rover. Initiated by MissionDataBean.
	 *
	 * @param missionType    the type of the mission.
	 * @param startingMember the mission member starting the mission.
	 * @param minPeople      the minimum number of people required for mission.
	 * @param rover          the rover to use on the mission.
	 */
	protected RoverMission(MissionType missionType, Worker startingMember, Rover rover) {
		// Use VehicleMission constructor.
		super(missionType, startingMember, rover);
		if (!isDone()) {
			calculateMissionCapacity(getRover().getCrewCapacity());
		}
	}

	/**
	 * Gets the mission's rover if there is one.
	 *
	 * @return vehicle or null if none.
	 */
	public final Rover getRover() {
		return (Rover) getVehicle();
	}

	/**
	 * Gets a collection of available Rovers at a settlement that are usable for
	 * this mission.
	 *
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 * @throws MissionException if problem determining if vehicles are usable.
	 */
	@Override
	protected Collection<Vehicle> getAvailableVehicles(Settlement settlement) {
		Collection<Vehicle> result = new ArrayList<>();
		Collection<Vehicle> list = settlement.getParkedVehicles();
		if (list.isEmpty())
			return result;
		for (Vehicle v : list) {
			if (VehicleType.isRover(v.getVehicleType())
					&& !v.haveStatusType(StatusType.MAINTENANCE)
					&& v.getMalfunctionManager().getMalfunctions().isEmpty()
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
	public static Rover getVehicleWithGreatestRange(Settlement settlement, boolean allowMaintReserved) {
		Rover result = null;

		for (Vehicle vehicle : settlement.getAllAssociatedVehicles()) {

			boolean usable = !vehicle.isReservedForMission();

            usable = usable && (allowMaintReserved || !vehicle.isReserved());

			usable = usable && vehicle.isVehicleReady();

			usable = usable && (vehicle.getStoredMass() <= 0D);

			if (usable && (vehicle instanceof Rover rover)) {
				if (result == null)
					// so far, this is the first vehicle being picked
					result = rover;
				else if (vehicle.getRange() > result.getRange())
					// This vehicle has a better range than the previously selected vehicle
					result = rover;
			}
		}

		return result;
	}

	/**
	 * Checks to see if any vehicles are available at a settlement.
	 *
	 * @param settlement         the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return true if vehicles are available.
	 */
	public static boolean areVehiclesAvailable(Settlement settlement, boolean allowMaintReserved) {

		boolean result = false;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();

			boolean usable = !vehicle.isReservedForMission();

            if (!allowMaintReserved && vehicle.isReserved())
				usable = false;

			usable = vehicle.isVehicleReady();

			if (!(vehicle instanceof Rover))
				usable = false;

			if (vehicle.getStoredMass() > 0D)
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
		if (!(newVehicle instanceof Rover))
			usable = false;
		return usable;
	}

	/**
	 * Checks that everyone in the mission is aboard the rover and not 
	 * doing an EVAOperation leaving the Vehicle
	 *
	 * @return true if everyone is aboard
	 */
	protected final boolean isEveryoneInRover() {
		Rover r = getRover();
		for(Worker m : getMembers()) {
			Person p = (Person) m;
			if (!r.isCrewmember(p)) {
				return false;
			}

			if (p.getTaskManager().getTask() instanceof EVAOperation) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks that no one in the mission is aboard the rover.
	 *
	 * @return true if no one is aboard
	 */
	protected final boolean isNoOneInRover() {
        return getRover().getCrewNum() == 0
                && getRover().getRobotCrewNum() == 0;
	}

	/**
	 * Checks if the rover is currently in a garage or not.
	 *
	 * @return true if rover is in a garage.
	 */
	protected boolean isInAGarage() {
		return getVehicle().isInAGarage();
	}



	/**
	 * Calculate the mission capacity the lower of desired capacity or number of EVASuits.
	 */
	protected void calculateMissionCapacity(int desiredCap) {
		if (!isDone()) {
			// Set mission capacity.
			int availableSuitNum = MissionUtil.getNumberAvailableEVASuitsAtSettlement(getStartingPerson().getAssociatedSettlement());
			if (availableSuitNum < desiredCap) {
				desiredCap = availableSuitNum;
			}
			setMissionCapacity(desiredCap);
		}
	}

	/**
	 * Performs the departing from settlement phase of the mission.
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
			logger.warning(member,
					Msg.getString("RoverMission.log.notAtSettlement", getPhase().getName())); //$NON-NLS-1$
			endMission(NO_AVAILABLE_VEHICLES);
			return;
		}

		// While still in the settlement, check if the beacon is turned on and and endMission()
		else if (v.isBeaconOn()) {
			endMission(VEHICLE_BEACON_ACTIVE);
			return;
		}

		// Can depart if every is on the vehicle or time has run out
		boolean canDepart = isEveryoneInRover();
			
		if (!canDepart && (getPhaseDuration() > DEPARTURE_DURATION)) {
			// Find who has not boarded
			List<Person> ejectedMembers = new ArrayList<>();
			Rover r = getRover();
			for(Worker m : getMembers()) {
				Person p = (Person) m;
				if (!r.isCrewmember(p)) {
					ejectedMembers.add(p);
				}
			}

			// Must have the leader
			if (!ejectedMembers.contains(getStartingPerson())) {
				// Still enough members ? If so eject late arrivals
				if ((getMembers().size() - ejectedMembers.size()) >= 2) {
					for(Person ej : ejectedMembers) {
						logger.info(ej, "Ejected from mission " + getName() + " missed Departure");
						removeMember(ej);
						addMissionLog(ej.getName() + " evicted");
					}
					canDepart = true;
				}
			}
			else {
				// Too many generated
				//logger.info(member, "Leader " + getStartingPerson().getName() + " still not boarded for mission " + getName());
			}
		}

		// Check if everyone is boarded
		if (canDepart) {
			// If the rover is in a garage
			if (v.isInAGarage()) {
				
				// Check to ensure it meets the baseline # of EVA suits
				meetBaselineNumEVASuits(settlement, v);
				
				// Put the rover outside.
				BuildingManager.removeFromGarage(v);
			}

			// Record the start mass right before departing the settlement
			recordStartMass();

			// Embark from settlement
			if (v.transfer(unitManager.getMarsSurface())) {
				setPhaseEnded(true);
			}
			else {
				endMissionProblem(v, "Could not exit Settlement");
			}

			// Marks everyone departed
			for(Worker m : getMembers()) {
				Person p = (Person) m;
				p.getTaskManager().recordActivity(getName(), "Departed", getName(), this);
			}
		}
		else {			
			// Gets a random location within rover.
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(v);
			callMembersToMission((int)(DEPARTURE_DURATION - DEPARTURE_PREPARATION));
			
			if (member instanceof Person) {
				Person person = (Person) member;

				// If person is not aboard the rover, board the rover and be ready to depart.
				if (!getRover().isCrewmember(person)) {

					Walk walk = Walk.createWalkingTask(person, adjustedLoc, 0, v);
					if (walk != null) {
						boolean canDo = assignTask(person, walk);
						if (!canDo) {
							logger.warning(person, "Unable to start walk to " + v + ".");
						}
					}

					else { // this crew member cannot find the walking steps to enter the rover
						logger.warning(member, Msg.getString("RoverMission.log.unableToEnter", //$NON-NLS-1$
								v.getName()));
					}
				}
			}

			else if (member instanceof Robot) {
				Robot robot = (Robot) member;
				Walk walkingTask = Walk.createWalkingTask(robot, adjustedLoc, v);
				if (walkingTask != null) {
					boolean canDo = assignTask(robot, walkingTask);
					if (!canDo) {
						logger.warning(robot, "Unable to walk to " + v + ".");
					}
				}
				else {
					logger.severe(member, Msg.getString("RoverMission.log.unableToEnter", //$NON-NLS-1$
							v.getName()));
				}
			}
		}
	}

	/**
	 * Meets the baseline standard for having enough EVA suits in the vehicle in garage.
	 * 
	 * @param settlement
	 * @param v
	 */
	public void meetBaselineNumEVASuits(Settlement settlement, Vehicle v) {
		// See if the there's enough EVA suits
		int availableSuitNum = settlement.findNumContainersOfType(EquipmentType.EVA_SUIT);
	
		if (availableSuitNum > 1 && !EVASuitUtil.hasBaselineNumEVASuit(v, this)) {
	
			for (Worker w: getMembers()) {
				// Check to see if there's enough EVA suits
				if (UnitType.PERSON == w.getUnitType()) {
	
					// Check if an EVA suit is available
					if (settlement.findNumContainersOfType(EquipmentType.EVA_SUIT) > 0
							|| !EVASuitUtil.hasBaselineNumEVASuit(v, this)) {
						// Obtain a suit from the settlement and transfer it to vehicle
						EVASuitUtil.fetchEVASuitFromSettlement((Person) w, v, settlement);
					}
				}
			}
		}
	}
	
	/**
	 * Performs the disembarking phase of the mission right after a rover returns home.
	 *
	 * @param member              the mission member currently performing the
	 *                            mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 */
	@Override
	protected void performDisembarkToSettlementPhase(Worker member, Settlement disembarkSettlement) {

		Vehicle v0 = getVehicle();
		
		// If v0 is being towed by a vehicle, gets the towing vehicle
		Vehicle v1 = v0.getTowingVehicle();
		
		// If v0 is towing a vehicle, gets the towed vehicle
		Vehicle v2 = ((Rover)v0).getTowedVehicle();
		
		if (!justArrived) {
			// Execute this only once upon arrival
			justArrived = true;
			
			// FUTURE: should make this an actual task to be taken by a settler in Command & Control
			registerVehicle(v0, disembarkSettlement);
			
			if (v1 != null) {
				registerVehicle(v1, disembarkSettlement);
				
				untetherVehicle(v0, v1, disembarkSettlement);
			}
			
			if (v2 != null) {
				registerVehicle(v1, disembarkSettlement);
				
				untetherVehicle(v2, v0, disembarkSettlement);
			}
		}

		// Disembark v0 - may take many frames to complete
		disembark(member, v0, disembarkSettlement);

		// Disembark v1 if exists - may take many frames to complete
		if (v1 != null)
			disembark(member, v1, disembarkSettlement);

		// Disembark v2 if exists - may take many frames to complete
		if (v2 != null)
			disembark(member, v2, disembarkSettlement);
	}

	
	/**
	 * Enters officially into the settlement vicinity and register its presence.
	 * 
	 * @param v
	 * @param disembarkSettlement
	 */
	public void registerVehicle(Vehicle v, Settlement disembarkSettlement) {
		
		Settlement currentSettlement = v.getSettlement();
		
		if ((currentSettlement == null) || !currentSettlement.equals(disembarkSettlement)) {
			// If rover has not been parked at settlement, park it.
			if (v.transfer(disembarkSettlement)) {
				logger.info(v, "Done transferring to " + disembarkSettlement.getName() + ".");
			}
			else {
				logger.info(v, "Unable to transfer to " + disembarkSettlement.getName() + ".");
			}
		}
	}
	
	/**
	 * Untethers the towing and towed vehicle from each other. 
	 * 
	 * @param v
	 * @param disembarkSettlement
	 */
	public void untetherVehicle(Vehicle towed, Vehicle towing, Settlement disembarkSettlement) {
		// Need to do these only once upon arrival 
		Rover towedRover = (Rover) towed;
		
		Rover towingRover = (Rover) towing;
		
		// Unhook both towed and towing vehicles.
		towingRover.setTowedVehicle(null);
		
		towedRover.setTowingVehicle(null);
		
		logger.log(towingRover, Level.INFO, 0,"Unhooked from " + towedRover + " at " + disembarkSettlement);
		
		logger.log(towedRover, Level.INFO, 0, "Successfully towed by " + towingRover + " to " + disembarkSettlement.getName());

		// First add towed vehicle (usually more damaged) to a garage if available.
        if (!towedRover.isBeingTowed()) {
        	disembarkSettlement.getBuildingManager().addToGarage(towedRover);
        }
        
        towedRover.setReservedForMission(false);
		
		// Then add towing vehicle to a garage if available.
        if (!towingRover.isTowingAVehicle()) {
        	disembarkSettlement.getBuildingManager().addToGarage(towingRover);
        }
        
        towingRover.setReservedForMission(false);
        
	}
	
	/**
	 * Disembarks the vehicle and unload cargo, for a rover just returned home.
	 *
	 * @param member
	 * @param v
	 * @param disembarkSettlement
	 */
	public void disembark(Worker member, Vehicle v, Settlement disembarkSettlement) {
		logger.info(v, 10_000, "Disembarked at " + disembarkSettlement.getName()
					+ " triggered by " + member.getName() +  ".");

		Rover rover = (Rover) v;

		Set<Person> crew = rover.getCrew();
		
		if (!crew.isEmpty()) {
//			boolean hasOccupants = !crew.isEmpty();
			
			// Add vehicle to a garage if available.
			boolean isRoverInAGarage = disembarkSettlement.getBuildingManager().isInGarage(v);
	
//	        if (hasOccupants) {
	        	
	            if (!isRoverInAGarage) {
	
	            	// Outside so preload all EVASuits before the Unloading starts
	            	int suitsNeeded = rover.getCrew().size();
	            	logger.info(rover, 10_000, "Preloading " + suitsNeeded + " EVA suits for disembarking.");
	            	Iterator<Equipment> eIt = rover.getEquipmentSet().iterator();
	            	while ((suitsNeeded > 0) && eIt.hasNext()) {
	            		Equipment e = eIt.next();
	            		if (e instanceof EVASuit) {
	            			if (((EVASuit)e).loadResources(rover) >= EVA_LOWEST_FILL) {
	            				suitsNeeded--;
	            			}
	            		}
	            	}
	            }
	            
				for (Person p : rover.getCrew()) {
					if (p.isDeclaredDead()) {
						logger.fine(p, "Dead body will be retrieved from rover " + v.getName() + ".");
					}
		
					// Initiate an rescue operation
					// Future : Gets a lead person to perform it and give him a rescue badge
					else if (!p.getPhysicalCondition().isFitByLevel(1500, 90, 1500)) {
						rescueOperation(rover, p, disembarkSettlement);
					}
		
					else if (isRoverInAGarage) {
						if (p.isInSettlement()) {
							// Something is wrong because the Person is in a Settlement
							// so it cannot be in the crew.
							logger.warning(rover, "Reports " + p.getName() + " is in the crew but already in a Settlement");
							rover.removePerson(p);
						}
						else {
							// Welcome this person home
					        p.transfer(disembarkSettlement);
							BuildingManager.addPersonOrRobotToBuilding(p, rover.getBuildingLocation());
						}
					}
					else {
						// Not in a garage
						
						// See if this person needs an EVA suit
						EVASuitUtil.metBaselineNumEVASuits(p, disembarkSettlement, this);
					}
				}
//	        }
		}
		
		// Unload rover if necessary.
		boolean roverUnloaded = UnloadVehicleEVA.isFullyUnloaded(rover);
		if (!roverUnloaded) {
			// Note : Set random chance of having person unloading resources,
			// thus allowing person to do other urgent things
			for (Worker mm : getMembers()) {
				if (RandomUtil.lessThanRandPercent(50)) {
					unloadCargo(((Person)mm), rover);
				}
			}
		}
		else if (!crew.isEmpty()) {
			// Check to see if no one is in the rover, unload the resources and end phase.
			for (Worker mm  : getMembers()) {
				// Walk back to the airlock
				if (((Person)mm).isInVehicle() || ((Person)mm).isOutside())
					walkToAirlock(rover, ((Person)mm), disembarkSettlement);
			}
		}
		else {
			// Complete disembarking once everyone is out of the Vehicle
			// Leave the vehicle.
			releaseVehicle(rover);
			// End the phase.
			setPhaseEnded(true);
		}
	}

	/**
	 * Give a person the task from unloading the vehicle
	 *
	 * @param p
	 * @param rover
	 */
	private void unloadCargo(Person person, Rover rover) {

		Mission m = person.getMission();
		if (m != null && !m.equals(this))
			return;

		TaskJob job = UnloadVehicleMeta.createUnloadJob(person.getAssociatedSettlement(), rover);
		if (job != null) {
			person.getMind().getTaskManager().addPendingTask(job, false);
		}
	}

	/**
	 * Checks on a person's status to see if he can walk toward the airlock or else be rescued
	 *
	 * @param rover
	 * @param p
	 * @param disembarkSettlement
	 */
	private void walkToAirlock(Rover rover, Person p, Settlement disembarkSettlement) {

		if (p.isInVehicle() || p.isOutside()) {
			// Get random inhabitable building at emergency settlement.
			Building destinationBuilding = disembarkSettlement.getBuildingManager().getRandomAirlockBuilding();

			if (destinationBuilding != null) {
				LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(destinationBuilding);

				boolean hasStrength = p.getPhysicalCondition().isFitByLevel(1500, 90, 1500);

				Walk walk = Walk.createWalkingTask(p, adjustedLoc, 0, destinationBuilding);
				if (walk != null) {
					// walk back home
					boolean canDo = assignTask(p, walk);
					if (!canDo) {
						logger.warning(p, "Unable to walk to " + destinationBuilding + ".");
					}
//					p.getMind().getTaskManager().getTask().addSubTask(walk);
				}

				else if (!hasStrength) {
					// Note 1: Help this person put on an EVA suit
					// Note 2: consider inflatable medical tent for emergency transport of incapacitated personnel
					logger.info(p,
							 Msg.getString("RoverMission.log.emergencyEnterSettlement", p.getName(),
									disembarkSettlement.getNickName())); //$NON-NLS-1$

					// Initiate an rescue operation
					// Note: Gets a lead person to perform it and give him a rescue badge
					rescueOperation(rover, p, disembarkSettlement);

					logger.info(p, "Transported to ("
							+ p.getPosition() + ") in "
							+ p.getBuildingLocation().getNickName()); //$NON-NLS-1$

					// Note: how to force the person to receive some form of medical treatment ?
		
    				p.getMind().getTaskManager().addAPendingTask(RequestMedicalTreatment.SIMPLE_NAME, false, 0, -1);

				}
				else {
					logger.severe(p, "Cannot find a walk path from "
									+ rover.getName() + " to " + disembarkSettlement.getName());
				}
			}

			else {
				logger.severe(p, 20_000L, "No airlock found at " + disembarkSettlement);
			}
		}
	}

	/**
	 * Rescues the person from the rover.
	 *
	 * @param r the rover
	 * @param p the person
	 * @param s the settlement
	 */
	private void rescueOperation(Rover r, Person p, Settlement s) {

		if (p.isDeclaredDead()) {
			if (p.transfer(s)) {
				logger.info(p, "Done emergency transfer of the body from "
						+ r + " to " + s + ".");
			}
			else
				logger.info(p, "Unable to do emergency transfer of the body from "
						+ r + " to " + s + ".");
		}
		// Retrieve the person from the rover
		else if (r != null && !p.isInSettlement()) {
			if (p.transfer(s)) {
				logger.info(p, "Done emergency transfer from "
						+ r + " to " + s + ".");
			}
			else
				logger.info(p, "Unable to do emergency transfer from "
						+ r + " to " + s + ".");
		}
		else if (p.isOutside()) {
			if (p.transfer(s)) {
				logger.info(p, "Done emergency transfer to " + s + ".");
			}
			else
				logger.info(p, "Unable to do emergency transfer to " + s + ".");
		}

		// Store the person into a medical building
		BuildingManager.addToMedicalBuilding(p, s);

		// Register the historical event
		HistoricalEvent rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON,
				this,
				p.getPhysicalCondition().getHealthSituation(),
				p.getTaskDescription(),
				p.getName(),
				p
				);
		eventManager.registerNewEvent(rescueEvent);
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
		if (member.getUnitType() == UnitType.PERSON) {
			Person person = (Person) member;
			// Check for fitness
			if (person.isSuperUnFit()) {
				logger.warning(person, 10_000L, "Not fit to operate " + getRover() + ".");
				return null;
			}
			
			if (!((Vehicle)getRover()).haveStatusType(StatusType.OUT_OF_FUEL)) {
				if (lastOperateVehicleTaskPhase != null) {
					result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
				} else {
					result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance());
				}
			}

			else {
				logger.warning(getRover(), 10_000L, "Out of fuel. Quit assigning the driving task.");
				return null;
			}
		}

		return result;
	}

	/**
	 * Checks to see if at least one inhabitant a settlement is remaining there.
	 *
	 * @param settlement the settlement to check.
	 * @param member     the mission member checking
	 * @return true if at least one person left at settlement.
	 */
	protected static boolean atLeastOnePersonRemainingAtSettlement(Settlement settlement, Worker member) {
		boolean result = false;

		if (settlement != null) {
			Iterator<Person> i = settlement.getIndoorPeople().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if ((inhabitant != member) && !inhabitant.getMind().hasActiveMission()) {
					result = true;
				}
			}
		}

		return result;
	}


	/**
	 * Gets the optional containers for a Rover mission. Add a spare EVASuit
	 *
	 * @return the containers needed.
	 */
	@Override
	protected Map<Integer, Integer> getOptionalEquipmentToLoad() {
		Map<Integer, Integer> result = super.getOptionalEquipmentToLoad();

		// Gets a spare EVA suit for each 4 members in a mission
		int numEVA = (int) (getMembers().size() * EXTRA_EVA_SUIT_FACTOR);
		int id = EquipmentType.getResourceID(EquipmentType.EVA_SUIT);
		result.put(id, numEVA);

		return result;
	}

	/**
	 * Gets the number and amounts of resources needed for the mission.
	 *
	 * @param useMargin Apply safety margin when loading resources before embarking if true.
	 *        Note : True if estimating trip. False if calculating remaining trip.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	@Override
	public Map<Integer, Number> getResourcesNeededForTrip(boolean useBuffer, double distance) {

		Map<Integer, Number> result = super.getResourcesNeededForTrip(useBuffer, distance);

		// Determine estimate time for trip.
		double time = getEstimatedTripTime(useBuffer, distance);
		double timeSols = time / 1000D;

		int people = getMembers().size();
		
		result = addLifeSupportResources(result, people, timeSols, useBuffer);

		// Add resources to load EVA suit of each person
		// Determine life support supplies needed for trip.
		result.merge(OXYGEN_ID, (EVASuit.OXYGEN_CAPACITY * people),
					 (a,b) -> (a.doubleValue() + b.doubleValue()));
		result.merge(WATER_ID, (EVASuit.WATER_CAPACITY * people),
					 (a,b) -> (a.doubleValue() + b.doubleValue()));

		return result;
	}

	/**
	 * Adds life support resources based on number of people and number of sols.
	 * 
	 * @param result
	 * @param crewNum
	 * @param timeSols
	 * @param useBuffer
	 */
	protected Map<Integer, Number> addLifeSupportResources(Map<Integer, Number> result,
												  int crewNum, double timeSols,
												  boolean useBuffer) {

		double lifeSupportRangeErrorMargin = Vehicle.getLifeSupportRangeErrorMargin();
		// Determine life support supplies needed for trip.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			oxygenAmount *= lifeSupportRangeErrorMargin * OXYGEN_MARGIN;
		result.merge(OXYGEN_ID, oxygenAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			waterAmount *= lifeSupportRangeErrorMargin * WATER_MARGIN; 
			// water is generated by fuel cells. no need of margins 
		result.merge(WATER_ID, waterAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			foodAmount *= lifeSupportRangeErrorMargin * FOOD_MARGIN;
		result.merge(FOOD_ID, foodAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));
		
		return result;
	}

	/**
	 * Gets EVA suit parts for the trip.
	 *
	 * @param numberMalfunctions
	 * @return
	 */
	protected Map<Integer, Number> getEVASparePartsForTrip(double numberMalfunctions) {
		Map<Integer, Number> map = new HashMap<>();

		// Determine needed repair parts for EVA suits.
		for(Entry<Integer, Double> part : EVASuit.getNormalRepairPart().entrySet()) {
			int number = (int) Math.round(part.getValue() * numberMalfunctions);
			if (number > 0) {
				map.put(part.getKey(), number);
			}
		}

		return map;
	}


	/**
	 * Checks if there is an available backup rover at the settlement for the
	 * mission.
	 *
	 * @param settlement the settlement to check.
	 * @return true if available backup rover.
	 */
	public static boolean hasBackupRover(Settlement settlement) {
		int availableVehicleNum = 0;
		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if ((vehicle instanceof Rover) && !vehicle.isReservedForMission())
				availableVehicleNum++;
		}
		return (availableVehicleNum >= 2);
	}

	/**
	 * Gets the time limit of the trip based on life support capacity.
	 *
	 * @param useBuffer use time buffer in estimation if true.
	 * @return time (millisols) limit.
	 * @throws MissionException if error determining time limit.
	 */
	public static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) {

		double timeLimit = Double.MAX_VALUE;

		// Check food capacity as time limit.
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = rover.getAmountResourceCapacity(FOOD_ID);
		double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
		if (foodTimeLimit < timeLimit) {
			timeLimit = foodTimeLimit;
		}

		// Check water capacity as time limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = rover.getAmountResourceCapacity(WATER_ID);
		double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
		if (waterTimeLimit < timeLimit) {
			timeLimit = waterTimeLimit;
		}

		// Check oxygen capacity as time limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = rover.getAmountResourceCapacity(OXYGEN_ID);
		double oxygenTimeLimit = oxygenCapacity / (oxygenConsumptionRate * memberNum);
		if (oxygenTimeLimit < timeLimit) {
			timeLimit = oxygenTimeLimit;
		}

		// Convert timeLimit into millisols and use error margin.
		timeLimit = (timeLimit * 1000D);
		if (useBuffer) {
			timeLimit /= Vehicle.getLifeSupportRangeErrorMargin();
		}
		
		return timeLimit;
	}

	/**
	 * Finds members for a mission, for RoverMissions all members must be at the same
	 * settlement.
	 * 
	 * @param startingMember
	 * @return
	 */
	protected boolean recruitMembersForMission(Worker startingMember, int minMembers) {
		return recruitMembersForMission(startingMember, true, minMembers);
	}

	@Override
	protected boolean recruitMembersForMission(Worker startingMember, boolean sameSettlement,
										int minMembers) {
		super.recruitMembersForMission(startingMember, sameSettlement, minMembers);

		// Make sure there is at least one person left at the starting
		// settlement.
		if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(), startingMember)) {
			// Remove last person added to the mission.
			Person lastPerson = null;
			 for (Iterator<Worker> i = getMembers().iterator(); 
					 i.hasNext();) {      
				 Worker member = i.next();
				if (member instanceof Person) {
					lastPerson = (Person) member;
					// Use Iterator's remove() method
					i.remove();
					// Adjust the work shift
					memberLeave(member);
				}
			 }
			
			 if (lastPerson != null) {
				lastPerson.getMind().setMission(null);
				if (getMembers().size() < minMembers) {
					endMission(NOT_ENOUGH_MEMBERS);
					return false;
				} 
			}
		}

		return true;
	}
}
