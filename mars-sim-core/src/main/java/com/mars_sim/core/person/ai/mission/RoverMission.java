/*
 * Mars Simulation Project
 * RoverMission.java
 * @date 2025-08-17
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
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.PhysicalConditionFormat;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.EatDrink;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.WalkingSteps;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.task.RequestMedicalTreatment;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.task.DriveGroundVehicle;
import com.mars_sim.core.vehicle.task.OperateVehicle;
import com.mars_sim.core.vehicle.task.UnloadVehicleEVA;
import com.mars_sim.core.vehicle.task.UnloadVehicleMeta;

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
	private static final int DEPARTURE_DURATION = 250;
	private static final int DEPARTURE_PREPARATION = 15;

	private static final String STATUS_REPORT = "[Status Report] Left ";
	
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
	 * Gets a collection of available rovers at a settlement that are usable for
	 * this mission.
	 *
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 * @throws MissionException if problem determining if vehicles are usable.
	 */
	@Override
	protected Collection<Vehicle> getAvailableVehicles(Settlement settlement) {
		Collection<Vehicle> result = new ArrayList<>();
		Collection<Vehicle> list = settlement.getParkedGaragedVehicles();
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
			usable = usable && (vehicle.isEmpty());

			if (usable && (vehicle instanceof Rover rover)) {
				if (result == null)
					// so far, this is the first vehicle being picked
					result = rover;
				else if (vehicle.getEstimatedRange() > result.getEstimatedRange())
					// This vehicle has a better range than the previously selected vehicle
					result = rover;
			}
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
	 * Similar to {@link EVAMission#checkTeleported()} for detecting if a person has been "teleported" but not exactly the same.
	 * 
	 * @return true if everyone is aboard
	 */
	protected boolean isEveryoneInRover(Worker member) {
		boolean result = true;
		
		Rover r = getRover();
		Set<Person> crew = new UnitSet<>();
		crew.addAll(r.getCrew());
		
		for (Person p : crew) {
			if (!getMembers().contains(p)) {
				logger.warning(p, "Case 0: " + p.getTaskDescription()
						+ ". Inside " + r.getName() + " but not a mission member of " + getName() + ".");
				addMissionLog("Not a member - " + p.getName(), ((Person)member).getName());
				result = false;
			}
		}
		
		for (Worker m : getMembers()) {
			Person p = (Person) m;
			
			if (r.isInGarage()) {
				// rover is in the garage. Members are expected to be boarded
				if (p.isInVehicle()) {
					// Best case
				}
				
				else if (p.isInSettlement() && p.getBuildingLocation() != r.getBuildingLocation()) {

					logger.warning(p, 20_000L, "Case 1A: Still inside settlement but not in garage or in " + r.getName()
							+ " yet. Not ready for '" + getName() + "' yet. Current location: " 
							+ p.getLocationTag().getExtendedLocation() + ".");
					result = false;
				}
				else if (p.isInSettlementVicinity()
						|| p.isRightOutsideSettlement()) {

					logger.warning(p, 20_000L, "Case 2A: Still outside and not on " + r.getName()
							+ " yet. Not ready for '" + getName() + "' yet. Current location: " 
							+ p.getLocationTag().getExtendedLocation() + ".");				
					
					if (p.getTaskManager().getTask() instanceof EVAOperation) {
						logger.warning(p, 20_000L, "Case 2A1: " + p.getTaskDescription() 
									+ ". Soon joining " + getName() + ".");
					}
					else if (p.getTaskDescription().equals("")) {
						logger.warning(p, 20_000L, "Case 2A2: Doing no task"
								+ " outside. Soon joining " + getName() + ".");
					}
					
					result = false;
				}
				
			}
			else {
				// rover is not in the garage and is in settlement vicinity. Members are expected to be boarded
				if (p.isInVehicle()) {
					// Best case
				}
				
				else if (p.isInSettlement()) {

					logger.warning(p, 20_000L, "Case 1B: Still inside settlement. Not in " + r.getName()
							+ " yet. Not ready for '" + getName() + "' yet. Current location: " 
							+ p.getLocationTag().getExtendedLocation() + ".");
					result = false;
				}
				else if (p.isInSettlementVicinity()
						|| p.isRightOutsideSettlement()) {

					logger.warning(p, 20_000L, "Case 2B: Still outside and not on " + r.getName()
							+ " yet. Not ready for '" + getName() + "' yet. Current location: " 
							+ p.getLocationTag().getExtendedLocation() + ".");				
					
					if (p.getTaskManager().getTask() instanceof EVAOperation) {
						logger.warning(p, 20_000L, "Case 2B1: " + p.getTaskDescription() 
									+ ". Soon joining " + getName() + ".");
					}
					else if (p.getTaskDescription().equals("")) {
						logger.warning(p, 20_000L, "Case 2B2: Doing no task"
								+ " outside. Soon joining " + getName() + ".");
					}
					
					result = false;
				}
			}
		}
		
		return result;
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
		return getVehicle().isInGarage();
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
	 * Checks membership and ejects non-members.
	 * 
	 * @param member
	 * @param r
	 * @return
	 */
	private boolean checkMembership(Worker member, Rover r) {
		
		boolean canDepart = true;
		
		// Find who has not boarded after the duration is over
		List<Person> ejectedMembers = new ArrayList<>();
		
		for (Worker m : getMembers()) {
			Person p = (Person) m;
			if (!r.isCrewmember(p)) {
				ejectedMembers.add(p);
			}
		}
		
		for (Person crewmember : r.getCrew()) {
            Worker w = (Worker)crewmember; 
			if (!getMembers().contains(w)) {
				ejectedMembers.add(crewmember);
			}
		}

		// Eject the late arrival if enough members
		if ((getMembers().size() - ejectedMembers.size()) >= MIN_GOING_MEMBERS) { 
			for (Person ej : ejectedMembers) {
				// Remove the mission membership
				removeMember(ej);

				if (r.isInGarage()) {
					// Force the person to get off the vehicle and back to the garage
					// Note: may need to evaluate a better way of handling this
					ej.transfer(r.getGarage());
				}
				else {
					// Let the person automatically leave the vehicle via walking toward a settlement airlock
					walkToAirLock(ej, r.getSettlement());
				}
				
				logger.warning(ej, "(" + ej.getTaskDescription() + " in " + ej.getLocationTag().getExtendedLocation() 
						+ ") got ejected from " + r.getName() + " as the rover was departing for " + getName() + ".");
				addMissionLog("Ejected", ej.getName());
			}
		}
		
		// If the leader is not ejected, then the mission can be proceeded
		if (ejectedMembers.contains(getStartingPerson())) {

			Person lead = (Person)member;
			// If the leader is ejected, then the mission must be cancelled
			logger.info(lead, "The mission Lead " + getStartingPerson().getName() 
					+ "(" + lead.getTaskDescription() + " in " + lead.getLocationTag().getExtendedLocation() 
					+ ") got ejected from " + getName() + " and mission cancelled.");
			addMissionLog(MISSION_LEAD_NO_SHOW.getName(), lead.getName());
			endMission(MISSION_LEAD_NO_SHOW);
			canDepart = false;
		}
		
		else if (getMembers().size() == 1) {
			logger.info(r, "Only one person in the mission. Cancelling " + getName() + ".");
			addMissionLog(ONLY_ONE_MEMBER.getName(), getStartingPerson().getName());
			endMission(ONLY_ONE_MEMBER);
			canDepart = false;
		}
		
		return canDepart;
	}

	/**
	 * Walks to an airlock to come back home.
	 * 
	 * @param person
	 * @param settlement
	 */
	private void walkToAirLock(Person person, Settlement settlement) {
		Building destinationBuilding = settlement.getBuildingManager().getRandomAirlockBuilding();
        if (destinationBuilding == null) {
            logger.warning(person, "Cannot find an airlock in " + settlement.getName());
        }

		LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(destinationBuilding);
        Walk walk = Walk.createWalkingTask(person, adjustedLoc, destinationBuilding, true);
        if (walk != null) {
        	// Walk back home
        	assignTask(person, walk);
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
			endMission(NO_AVAILABLE_VEHICLE);
			return;
		}

		Settlement settlement = v.getSettlement();
		if (settlement == null) {
			logger.warning(member,
					Msg.getString("RoverMission.log.notAtSettlement", getPhase().getName())); //$NON-NLS-1$
			endMission(NO_AVAILABLE_VEHICLE);
			return;
		}

		// While still in the vicinity of the settlement, check if the beacon is turned on. 
		// If true, call endMission
		else if (v.isBeaconOn()) {
			endMission(VEHICLE_BEACON_ACTIVE);
			return;
		}
		
		if (isDone()) {
			return;
		}
		
		boolean canDepart = false;
		
		double timeLeft = DEPARTURE_DURATION - DEPARTURE_PREPARATION - getPhaseDuration();
		// Set the members' work shift to on-call to get ready. 
		if (timeLeft >= 1) {
			callMembersToMission((int)timeLeft);	
		}
		else if (getPhaseDuration() - .3 * DEPARTURE_DURATION > 0
				&& getPhaseDuration() - DEPARTURE_DURATION < 0) {
			canDepart = isEveryoneInRover(member);
			if (canDepart) {
				addMissionLog("All Boarded", getStartingPerson().getName());
				logger.info(v, 20_000, "Everyone is ready for departing " + settlement.getName() + ".");
			}
		}		
		else if (timeLeft < 1) {
			canDepart = true;
			callEveryone(v);
			logger.info(v, 20_000, "Departure wait time ended. Alerting everyone for leaving " + settlement.getName() + ".");
		}
		
		if (canDepart) {
			canDepart = evaluateDepartureCriteria(member, v, settlement);
		}
		
		if (canDepart) {	
			logger.info(v, 20_000, "Passed all tests, departing " + settlement.getName() + ".");
			depart(v, settlement);		
		}
	}
	
	/**
	 * Evaluates criteria for departure.
	 * 
	 * @param member
	 * @param v
	 * @param settlement
	 * @return
	 */
	private boolean evaluateDepartureCriteria(Worker member, Vehicle v, Settlement settlement) {
		boolean canDepart = true;

		if (canDepart) {
			// Can depart if everyone is on the vehicle
			canDepart = isEveryoneInRover(member);
			
			if (canDepart) {
				logger.info(v, 20_000, "All Boarded.");
				addMissionLog("All Boarded", member.getName());
			}
		}
				
		if (canDepart) {
			// Check if each member is qualified
			canDepart = checkMembership(member, (Rover)v);
			
			if (canDepart) {
				logger.info(v, 20_000, "Membership Checked Out.");
				addMissionLog("Membership Checked Out", member.getName());
			}
		}

		// If the rover is in a garage
		if (canDepart && v.isInGarage()) {			
			// Check to ensure it meets the baseline # of EVA suits
			canDepart = meetBaselineNumEVASuits(settlement, v);
			
			if (canDepart) {
				logger.info(v, 20_000, "Baseline EVA suit Met.");
				addMissionLog("Baseline EVA suit Met", member.getName());
			}
		}

		return canDepart;
	}
	
	/**
	 * Departs the settlement.
	 * 
	 * @param v
	 * @param settlement
	 */
	private void depart(Vehicle v, Settlement settlement) {

		// Record the start mass right before departing the settlement
		recordStartMass();

		// Embark from settlement
		if (v.transfer(unitManager.getMarsSurface())) {
			logger.info(v, 0, "Just embarked from " + settlement.getName() + ".");
			
			// Enforce each occupant to transfer/set container unit
			for (Worker w : getMembers()) {
				Person crewmember = (Person)w;
				boolean canGo = crewmember.transfer(v);
				if (canGo) {
					logger.info(crewmember, 0, "Just transferred from the settlement to " + v.getName() + ".");
				}
				else {
					logger.info(crewmember, 0, "Unable to transfer from the settlement to " + v.getName() + ".");
				}
			}
			
			// Note: calling setPhaseEnded(true) is crucial to proceed to the next phase
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
	
	/**
	 * Calls out everyone to come back to the settlement to get ready for departure.
	 * 
	 * @param member
	 * @param v
	 */
	private void callEveryone(Vehicle v) {

		// Gets a random location within rover.
		LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(v);
	
		for (Worker member: getMembers()) {
		
			if (member instanceof Person person
				// If not aboard the rover, board the rover and be ready to depart.
				&& !getRover().isCrewmember(person)) {
	
				WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, v);
				boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
				
				if (canWalk) {
					boolean canDo = assignTask(person, new Walk(person, walkingSteps));
					if (!canDo) {
						logger.warning(person, 20_000, "Unable to start walking toward " + v + ".");
					}
				}

				else { // this crew member cannot find the walking steps to enter the rover
					logger.warning(member, 20_000, Msg.getString("RoverMission.log.unableToEnter", //$NON-NLS-1$
							v.getName()));
				}
			}
			
			else if (member instanceof Robot robot
					// If not aboard the rover, board the rover and be ready to depart.
					&& !getRover().isRobotCrewmember(robot)) {
				
				WalkingSteps walkingSteps = new WalkingSteps(robot, adjustedLoc, v);
				boolean canWalk = Walk.canWalkAllSteps(robot, walkingSteps);
				
				if (canWalk) {
					boolean canDo = assignTask(robot, new Walk(robot, walkingSteps));
					if (!canDo) {
						logger.warning(robot, 20_000, "Unable to start walking to " + v + ".");
					}
				}

				else {
					logger.warning(member, 20_000, Msg.getString("RoverMission.log.unableToEnter", //$NON-NLS-1$
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
	 * @return
	 */
	public boolean meetBaselineNumEVASuits(Settlement settlement, Vehicle v) {
		boolean canDepart = false;
		
		// See if the there's enough EVA suits
		int availableSuitNum = settlement.getNumEVASuit();
	
		if (availableSuitNum > 1 && !EVASuitUtil.hasBaselineNumEVASuit(v, this)) {
	
			for (Worker w: getMembers()) {
				// Check to see if there's enough EVA suits
				if (w instanceof Person person
					// Check if an EVA suit is available
					&& (availableSuitNum > 0
						|| !EVASuitUtil.hasBaselineNumEVASuit(v, this))) {
					// Obtain a suit from the settlement and transfer it to vehicle
					canDepart = EVASuitUtil.fetchEVASuitFromSettlement(person, v, settlement);
				}
			}
		}
		return canDepart;
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
				else {
					// Park in the vicinity of the settlement outside
					v0.findNewParkingLoc();
				}
			}
			else {
				if (v1 != null) {
					registerVehicle(v1, disembarkSettlement);
					
					untetherVehicle(v0, v1, disembarkSettlement);
				}
				
				else if (v2 != null) {
					registerVehicle(v2, disembarkSettlement);
					
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
	 * Preloads all EVA suits prior to unloading other resources.
	 * 
	 * @param crew
	 * @param rover
	 */
	private void preloadEVASuits(Set<Person> crew, Rover rover) {
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
	
	/**
	 * Transfers the person back to the settlement and reports status.
	 * 
	 * @param p
	 * @param rover
	 * @param disembarkSettlement
	 */
	private void transferReport(Person p, Rover rover, Settlement disembarkSettlement) {
		// Transfer the person from vehicle to settlement
		boolean backToSettle = p.transfer(disembarkSettlement);
		
		if (backToSettle) {
			// Remove this person from the rover
			rover.removePerson(p);
			
			// Add this person to the building
			BuildingManager.setToBuilding(p, rover.getGarage());
			
			String roverName = rover.getName();
			
			if (p.isInSettlement()) {
				logger.info(p, 20_000L, STATUS_REPORT + roverName
						+ " in " + rover.getBuildingLocation().getName()
						+ ".  Building: " + p.getBuildingLocation().getName()
						+ ".  Location State: " + p.getLocationStateType().getName());
			}
			
			else {						
				// Not in settlement yet
				logger.severe(p, 20_000L, STATUS_REPORT + roverName
						+ " in " + rover.getLocationStateType().getName()
						+ ".  Location State: " + p.getLocationStateType().getName());
			}
		}
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
		
		// Add vehicle to a garage if available.
		boolean isRoverInAGarage = disembarkSettlement.getBuildingManager().isInGarage(v);
	
		if (!crew.isEmpty()) {
			
            if (!isRoverInAGarage) {    
            	// Assume there's an easy way to plug into the settlement to load up resources in EVA suits,
            	// just in case resources have been depleted
            	preloadEVASuits(crew, rover);	
            }
        	
			boolean roverUnloaded = UnloadVehicleEVA.isFullyUnloaded(rover);
            
			if (member instanceof Person p) {
				
				if (p.isDeclaredDead()) {
					logger.fine(p, "Dead body will be retrieved from rover " + v.getName() + ".");
				}
	
				// Future : Gets a lead person to perform it and give him a rescue badge
				else if (p.getPhysicalCondition().isUnfitByLevel(1500, 90, 1500, 1000)
						&& rover.getCrew().contains(p)) {
					// Initiate an rescue operation
					rescueOperation(rover, p, disembarkSettlement);
				}
	
				else if (isRoverInAGarage
						&& rover.getCrew().contains(p)) {
					transferReport(p, rover, disembarkSettlement);	
				}
				
				else {
					// Rover is NOT in a garage

					// Note: need to see if this person needs an EVA suit
					
					// Note: This is considered cheating since missing EVA suits are automatically
					// transfered to the vehicle
					EVASuitUtil.checkTransferSuitsToVehicle(p, disembarkSettlement, this);
				}
				
				// Unload rover if necessary.

				// Note : Set random chance of having person unloading resources,
				// thus allowing person to do other urgent things
				if (!roverUnloaded && RandomUtil.lessThanRandPercent(50)) {
					unloadCargo(member, rover);
				}
			}
		}
	
        // Update and reload the crew members since some may have just left the vehicle
		crew = rover.getCrew();
        
		if (!crew.isEmpty()) {
			// Check to see if anyone is still in the vehicle
			// Walk back to the airlock.	
			for (Person pp: crew) {
				if (isRoverInAGarage) {
					transferReport(pp, rover, disembarkSettlement);
				}
				else {
					walkToAirlock(rover, pp, disembarkSettlement);
				}
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
            if (worker instanceof Person p
            	&& p.getPhysicalCondition().isUnfitByLevel(1000, 90, 1000, 1000)) {
            	
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

			WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, destinationBuilding);
			boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
			
			if (canWalk) {
				boolean canDo = assignTask(person, new Walk(person, walkingSteps));
				if (!canDo) {
					logger.warning(person, 20_000, "Unable to walk back to " + destinationBuilding + ".");
				}
			}

			else if (!hasStrength) {
				// Note 1: Help this person put on an EVA suit
				// Note 2: consider inflatable medical tent for emergency transport of incapacitated personnel
				logger.info(person, 10_000, 
						 Msg.getString("RoverMission.log.emergencyEnterSettlement", person.getName(),
								disembarkSettlement.getName())); //$NON-NLS-1$

				logger.info(person, 10_000, ""
						+ "Currently at "
						+ person.getLocationTag().getExtendedLocation()); 

				// Initiate an rescue operation
				// Note: Gets a lead person to perform it and give him a rescue badge
				rescueOperation(rover, person, disembarkSettlement);

				logger.info(person, 10_000, ""
						+ "Transported to "
						+ person.getLocationTag().getExtendedLocation()); 
				
				// Note: how to force the person to receive some form of medical treatment ?
		
    			Task currentTask = person.getMind().getTaskManager().getTask();
    			if (currentTask != null && !currentTask.getName().equalsIgnoreCase(RequestMedicalTreatment.NAME)) {
    				person.getMind().getTaskManager().addPendingTask(RequestMedicalTreatment.SIMPLE_NAME);
    			}
			}
			else {
				logger.severe(person, 10_000, "Cannot find a walk path from "
								+ rover.getName() + " to " + disembarkSettlement.getName());
			}
		}

		else {
			logger.severe(person, 10_000, "No airlock found at " + disembarkSettlement);
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

		// Send the person to a medical building
		BuildingManager.addPatientToMedicalBed(p, s);

		// Register the historical event
		HistoricalEvent rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON,
				this,
				PhysicalConditionFormat.getHealthSituation(p.getPhysicalCondition()),
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
		
		boolean areAllOthersUnfit = areAllOthersUnfit(member);

		if (member instanceof Person person) {
			// Check for fitness
			if (person.isSuperUnfit()) {
				
				if (areAllOthersUnfit) {
					logger.warning(person, 10_000L, "As everyone is unfit to operate " + getRover() + ", " 
						+ person + " decided to step up to be the pilot.");
					
				} else {
					
			       	// For humans
		        	logger.warning(person, 4_000, "Super unfit to pilot " + getVehicle() + ".");
		        	// Note: How to take care of the person if he does not have high fatigue but other health issues ?
		        	
					// Note: if a person is not in fatigue but is hungry or thirsty, don't need to sleep
					double fatigue = person.getPhysicalCondition().getFatigue();
					if (fatigue > 900) {				
						boolean canSleep = assignTask(person, new Sleep(person));
			        	if (canSleep) {
			        		logger.log(person, Level.INFO, 4_000,
			            			"Instructed to sleep before piloting " + getVehicle() + " since fatigue is " + Math.round(fatigue) + ".");
			        		
			        		return null;
			        	}
		        	}
					
					double hunger = person.getPhysicalCondition().getHunger();
					double thirst = person.getPhysicalCondition().getThirst();
					if (hunger > 900 || thirst > 550) {				
						boolean canEatDrink = assignTask(person, new EatDrink(person));
			        	if (canEatDrink) {
			        		logger.log(person, Level.INFO, 4_000,
			            			"Instructed to eat/drink before piloting " + getVehicle() 
			            			+ " (hunger: " + Math.round(fatigue) + "; "
			            			+ " thirst: " + Math.round(thirst) + ").");
			        		
			        		return null;
			        	}
		        	}	
	
					logger.warning(person, 10_000L, "Super unfit to operate " + getRover() + ".");
					return null;
				}
			}
			
			Vehicle v = (Vehicle)getRover();
			
			if (!v.haveStatusType(StatusType.OUT_OF_FUEL)
					&& !v.haveStatusType(StatusType.OUT_OF_BATTERY_POWER)) {
				if (lastOperateVehicleTaskPhase != null) {
					result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
				} else {
					result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance());
				}
			}

			else {
				logger.warning(getRover(), 10_000L, "Out of fuel/battery power. Quit assigning the driving task.");
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
		result.merge(ResourceUtil.OXYGEN_ID, (EVASuit.OXYGEN_CAPACITY * people),
					 (a,b) -> (a.doubleValue() + b.doubleValue()));
		result.merge(ResourceUtil.WATER_ID, (EVASuit.WATER_CAPACITY * people),
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
		result.merge(ResourceUtil.OXYGEN_ID, oxygenAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			waterAmount *= lifeSupportRangeErrorMargin * WATER_MARGIN; 
			// water is generated by fuel cells. no need of margins 
		result.merge(ResourceUtil.WATER_ID, waterAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			foodAmount *= lifeSupportRangeErrorMargin * FOOD_MARGIN;
		result.merge(ResourceUtil.FOOD_ID, foodAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));
		
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
		Iterator<Vehicle> i = settlement.getParkedGaragedVehicles().iterator();
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
		double foodCapacity = rover.getSpecificCapacity(ResourceUtil.FOOD_ID);
		double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
		if (foodTimeLimit < timeLimit) {
			timeLimit = foodTimeLimit;
		}

		// Check water capacity as time limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = rover.getSpecificCapacity(ResourceUtil.WATER_ID);
		double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
		if (waterTimeLimit < timeLimit) {
			timeLimit = waterTimeLimit;
		}

		// Check oxygen capacity as time limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = rover.getSpecificCapacity(ResourceUtil.OXYGEN_ID);
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
			for (Iterator<Worker> i = getMembers().iterator(); i.hasNext();) {      
				Worker member = i.next();
				if (member instanceof Person p) {
					lastPerson = p;
					// Use Iterator's remove() method
					i.remove();
					// Adjust the work shift
					removeMember(member);
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
