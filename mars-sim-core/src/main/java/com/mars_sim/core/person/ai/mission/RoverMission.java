/*
 * Mars Simulation Project
 * RoverMission.java
 * @date 2023-07-19
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.WalkingSteps;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.task.RequestMedicalTreatment;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.task.DriveGroundVehicle;
import com.mars_sim.core.vehicle.task.OperateVehicle;
import com.mars_sim.core.vehicle.task.UnloadVehicleEVA;
import com.mars_sim.core.vehicle.task.UnloadVehicleMeta;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

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
	@Override
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
	 * Calculates the mission capacity the lower of desired capacity or number of EVASuits.
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

		// While still in the vicinity of the settlement, check if the beacon is turned on. 
		// If true, call endMission
		else if (v.isBeaconOn()) {
			endMission(VEHICLE_BEACON_ACTIVE);
			return;
		}

		// Can depart if everyone is on the vehicle or time has run out
		boolean canDepart = isEveryoneInRover();
			
		if (!canDepart && (getPhaseDuration() > DEPARTURE_DURATION)) {
			// Find who has not boarded
			List<Person> ejectedMembers = new ArrayList<>();
			Rover r = getRover();
			for (Worker m : getMembers()) {
				Person p = (Person) m;
				if (!r.isCrewmember(p)) {
					ejectedMembers.add(p);
				}
			}

			// Must have the leader
			if (!ejectedMembers.contains(getStartingPerson())) {
				// Still enough members ? If so eject late arrivals
				if ((getMembers().size() - ejectedMembers.size()) >= 2) {
					for (Person ej : ejectedMembers) {
						logger.info(ej, "Ejected from mission " + getName() + " missed the departure.");
						removeMember(ej);
						addMissionLog(ej.getName() + " evicted");
					}
					canDepart = true;
				}
			}
//			else {
				// Too many generated
				// logger.info(member, "Leader " + getStartingPerson().getName() + " still not boarded for mission " + getName());
//			}
		}

		if (canDepart) {
			// If the rover is in a garage
			if (v.isInAGarage()) {
				
				// Check to ensure it meets the baseline # of EVA suits
				meetBaselineNumEVASuits(settlement, v);
			}

			// Record the start mass right before departing the settlement
			recordStartMass();

			// Embark from settlement
			if (v.transfer(unitManager.getMarsSurface())) {
				logger.info(v, 10_000L, "Just embarked from the settlement.");
				setPhaseEnded(true);
			}
			else {
				endMissionProblem(v, "Could not exit Settlement.");
			}

			// Record and mark everyone departing
			for (Worker m : getMembers()) {
				((Person) m).getTaskManager().recordActivity(getName(), "Departed", getName(), this);
			}
		}
		else {
			// Gets a random location within rover.
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(v);
			callMembersToMission((DEPARTURE_DURATION - DEPARTURE_PREPARATION));
			
			if (member instanceof Person person) {
				// If person is not aboard the rover, board the rover and be ready to depart.
				if (!getRover().isCrewmember(person)) {

					WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, v);
					boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
					
					if (canWalk) {
						boolean canDo = assignTask(person, new Walk(person, walkingSteps));
						if (!canDo) {
							logger.warning(person, "Unable to start walking to " + v + ".");
						}
					}

					else { // this crew member cannot find the walking steps to enter the rover
						logger.warning(member, Msg.getString("RoverMission.log.unableToEnter", //$NON-NLS-1$
								v.getName()));
					}
				}
			}

			else if (member instanceof Robot robot) {
				
				WalkingSteps walkingSteps = new WalkingSteps(robot, adjustedLoc, v);
				boolean canWalk = Walk.canWalkAllSteps(robot, walkingSteps);
				
				if (canWalk) {
					boolean canDo = assignTask(robot, new Walk(robot, walkingSteps));
					if (!canDo) {
						logger.warning(robot, "Unable to start walk to " + v + ".");
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
		int availableSuitNum = settlement.getNumEVASuit();
	
		if (availableSuitNum > 1 && !EVASuitUtil.hasBaselineNumEVASuit(v, this)) {
	
			for (Worker w: getMembers()) {
				// Check to see if there's enough EVA suits
				if (UnitType.PERSON == w.getUnitType()) {
	
					// Check if an EVA suit is available
					if (availableSuitNum > 0
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
			
			if (v1 == null && v2 == null) {
				registerVehicle(v0, disembarkSettlement);
				
	        	// Add vehicle to a garage if available.
				Building garage = disembarkSettlement.getBuildingManager().addToGarageBuilding(v0);
				
				if (garage != null) {
					logger.info(v0, "Done transferring to " + disembarkSettlement.getName() + " in " + garage + ".");
				}
			}
			else {
				if (v1 != null) {
					registerVehicle(v1, disembarkSettlement);
					
					untetherVehicle(v0, v1, disembarkSettlement);
				}
				
				if (v2 != null) {
					registerVehicle(v1, disembarkSettlement);
					
					untetherVehicle(v2, v0, disembarkSettlement);
				}
			}
			
			// Record and mark everyone arriving
			for (Worker m : getMembers()) {
				((Person) m).getTaskManager().recordActivity(getName(), "Arrived", getName(), this);
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
	 * Register the vehicle's presence and transfer the vehicle into the settlement vicinity.
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
	 * Untethers the towing and towed vehicle from each other and enter a garage if space is available.
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
        	// Add vehicle to a garage if available.
			Building garage = disembarkSettlement.getBuildingManager().addToGarageBuilding(towingRover);
			
			if (garage != null) {
				logger.info(towedRover, "Done transferring to " + disembarkSettlement.getName() + " in " + garage + ".");
			}
        }
        
        towedRover.setReservedForMission(false);
		
		// Then add towing vehicle to a garage if available.
        if (!towingRover.isTowingAVehicle()) {
           	// Add vehicle to a garage if available.
			Building garage = disembarkSettlement.getBuildingManager().addToGarageBuilding(towingRover);
			
			if (garage != null) {
				logger.info(towingRover, "Done transferring to " + disembarkSettlement.getName() + " in " + garage + ".");
			}
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
		Set<Person> crew = new UnitSet<>();
		crew.addAll(rover.getCrew());
		
		if (!crew.isEmpty()) {
			
			// Add vehicle to a garage if available.
			boolean isRoverInAGarage = disembarkSettlement.getBuildingManager().isInGarage(v);
		
            if (!isRoverInAGarage) {

            	// Outside so preload all EVASuits before the Unloading starts
            	int suitsNeeded = crew.size();
            	logger.info(rover, 10_000, "Preloading " + suitsNeeded + " EVA suits for disembarking.");
            	Iterator<Equipment> eIt = rover.getSuitSet().iterator();
            	while ((suitsNeeded > 0) && eIt.hasNext()) {
            		Equipment e = eIt.next();
            		if (((EVASuit)e).loadResources(rover) >= EVA_LOWEST_FILL) {
            			suitsNeeded--;
            		}
            	}
            }
        	
        	for (Person p : crew) {
				if (p.isDeclaredDead()) {
					logger.fine(p, "Dead body will be retrieved from rover " + v.getName() + ".");
				}
	
				// Initiate an rescue operation
				// Future : Gets a lead person to perform it and give him a rescue badge
				else if (!p.getPhysicalCondition().isFitByLevel(1500, 90, 1500)) {
					rescueOperation(rover, p, disembarkSettlement);
				}
	
				else if (isRoverInAGarage) {
										
					// Transfer the person from vehicle to settlement
					p.transfer(disembarkSettlement);
					
					// Remove this person from the rover
					rover.removePerson(p);
					
					// Add this person to the building
					BuildingManager.setToBuilding(p, rover.getGarage());
					
					String roverName = "None";
					
					if (p.getVehicle() != null)
						roverName = p.getVehicle().getName();
					
					if (p.isInSettlement()) {
						logger.info(p, "[Status Report] " + roverName
								+ " in " + rover.getBuildingLocation().getName()
								+ ".  Person's Location: " + p.getLocationStateType().getName()
								);
					}
					
					else {						
						// Not in settlement yet
						logger.info(p, "[Status Report] " + roverName
								+ " in " + rover.getLocationStateType().getName()
								+ ".  Person's Location: " + p.getLocationStateType().getName()
								);
					}
				}
				
				else {
					// Not in a garage
					
					// See if this person needs an EVA suit
					// This is considered cheating since missing EVA suits are automatically
					// transfered to the vehicle
					EVASuitUtil.transferSuitsToVehicle(p, disembarkSettlement, this);
				}
            }
		}
		
		// Unload rover if necessary.
		boolean roverUnloaded = UnloadVehicleEVA.isFullyUnloaded(rover);
		// Note : Set random chance of having person unloading resources,
		// thus allowing person to do other urgent things
		if (!roverUnloaded && RandomUtil.lessThanRandPercent(50)) {
			unloadCargo(member, rover);
		}
		
        // Check the crew again since it's possible that in the same frame, 
        // some other crew members have just been vacated, causing the result to be diff.
        crew = rover.getCrew();
        
		if (!crew.isEmpty()) {
			// Check to see if no one is in the rover, unload the resources and end phase.
			// Walk back to the airlock
        	for (Person p : crew) {
				if (!p.isInSettlement())
					walkToAirlock(rover, p, disembarkSettlement);
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
	 * Gives a person the task from unloading the vehicle.
	 *
	 * @param p
	 * @param rover
	 * @return
	 */
	private boolean unloadCargo(Worker worker, Rover rover) {

		TaskJob job = UnloadVehicleMeta.createUnloadJob(worker.getAssociatedSettlement(), rover);
		boolean assigned = false;
        if (job != null) {
            Task task = null;
            // Create the Task ready for assignment
            if (worker instanceof Person p) {
                task = job.createTask(p);
                // Task may be rejected because of the Worker's profile
                assigned = assignTask(p, task);
            }
            else if (worker instanceof Robot r && isInAGarage()) {
                task = job.createTask(r);
                // Task may be rejected because of the Worker's profile
                assigned = assignTask(r, task);
            }
		}
        return assigned;
	}
	
	/**
	 * Checks on a person's status to see if he can walk toward the airlock or else be rescued.
	 *
	 * @param rover
	 * @param person
	 * @param disembarkSettlement
	 */
	private void walkToAirlock(Rover rover, Person person, Settlement disembarkSettlement) {

			// Get random airlock building at settlement.
			Building destinationBuilding = disembarkSettlement.getBuildingManager().getRandomAirlockBuilding();

			if (destinationBuilding != null) {
				LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(destinationBuilding);

				boolean hasStrength = person.getPhysicalCondition().isFitByLevel(1500, 90, 1500);

				WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, rover);
				boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
				
				if (canWalk) {
					boolean canDo = assignTask(person, new Walk(person, walkingSteps));
					if (!canDo) {
						logger.warning(person, "Unable to walk to " + destinationBuilding + ".");
					}
				}

				else if (!hasStrength) {
					// Note 1: Help this person put on an EVA suit
					// Note 2: consider inflatable medical tent for emergency transport of incapacitated personnel
					logger.info(person,
							 Msg.getString("RoverMission.log.emergencyEnterSettlement", person.getName(),
									disembarkSettlement.getName())); //$NON-NLS-1$

					// Initiate an rescue operation
					// Note: Gets a lead person to perform it and give him a rescue badge
					rescueOperation(rover, person, disembarkSettlement);

					logger.info(person, "Transported to ("
							+ person.getPosition() + ") in "
							+ person.getBuildingLocation().getName()); //$NON-NLS-1$

					// Note: how to force the person to receive some form of medical treatment ?
			
        			Task currentTask = person.getMind().getTaskManager().getTask();
        			if (currentTask != null && !currentTask.getName().equalsIgnoreCase(RequestMedicalTreatment.NAME)) {
        				person.getMind().getTaskManager().addPendingTask(RequestMedicalTreatment.SIMPLE_NAME);
        			}
				}
				else {
					logger.severe(person, "Cannot find a walk path from "
									+ rover.getName() + " to " + disembarkSettlement.getName());
				}
			}

			else {
				logger.severe(person, 20_000L, "No airlock found at " + disembarkSettlement);
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
	 * @param lastOperateVehicleTaskPhase The last task phase
	 * @return an OperateVehicle task for the person.
	 */
	@Override
	protected OperateVehicle createOperateVehicleTask(Worker member, TaskPhase lastOperateVehicleTaskPhase) {
		OperateVehicle result = null;
		
		boolean areAllOthersUnfit = true;
		// Check if everyone is unfit
		for (Worker w: getMembers()) {
			if (!w.equals(member) && w instanceof Person p) {
				if (!p.isSuperUnFit()) {
					areAllOthersUnfit = false;
				}
			}
		}
		
		if (member instanceof Person person) {
			// Check for fitness
			if (person.isSuperUnFit()) {
				
				if (areAllOthersUnfit) {
					logger.warning(person, 10_000L, "As every is unfit to operate " + getRover() + ", " 
						+ person + " decided to step up to be the pilot.");
				} else {
					logger.warning(person, 10_000L, "Super unfit to operate " + getRover() + ".");
					return null;
				}
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
