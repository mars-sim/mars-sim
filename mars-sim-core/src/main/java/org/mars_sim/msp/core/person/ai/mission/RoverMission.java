/*
 * Mars Simulation Project
 * RoverMission.java
 * @date 2021-10-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.RequestMedicalTreatment;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission that involves driving a rover vehicle along a series of navpoints.
 */
public abstract class RoverMission extends VehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RoverMission.class.getName());

	// Static members
	public static final int MIN_STAYING_MEMBERS = 1;
	public static final int MIN_GOING_MEMBERS = 2;
	
	public static final double FUEL_CELL_FACTOR = 1D; //.5;

	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    public static final double LEAST_AMOUNT = GroundVehicle.LEAST_AMOUNT;

	public static final double MIN_STARTING_SETTLEMENT_METHANE = 500D;

	public static final String PHASE_1 = "phase 1";
	public static final String MINING = "mining";
	public static final String TRADING = "trading";

	// What is the lowest fullness of an EVASuit to be usable
	private static final double EVA_LOWEST_FILL = 0.5D;

	/** The factor for determining how many more EVA suits are needed for a trip. */
	private static final double EXTRA_EVA_SUIT_FACTOR = .2;

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
	 * Gets the available vehicle at the settlement with the greatest range.
	 *
	 * @param settlement         the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return vehicle or null if none available.
	 * @throws Exception if error finding vehicles.
	 */
	public static Vehicle getVehicleWithGreatestRange(MissionType missionType, Settlement settlement, boolean allowMaintReserved) {
		Vehicle result = null;

		for(Vehicle vehicle : settlement.getAllAssociatedVehicles()) {

			boolean usable = !vehicle.isReservedForMission();

            usable = usable && (allowMaintReserved || !vehicle.isReserved());

			usable = usable && vehicle.isVehicleReady();

			usable = usable && (vehicle.getStoredMass() <= 0D);

			usable = usable && (vehicle instanceof Rover);

			if (usable) {
				if (result == null)
					// so far, this is the first vehicle being picked
					result = vehicle;
				else if (vehicle.getRange(missionType) > result.getRange(missionType))
					// This vehicle has a better range than the previously selected vehicle
					result = vehicle;
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
	 * Does the vehicle have at least the baseline numbers of EVA suits ?
	 *
	 * @param vehicle
	 * @return
	 */
	protected boolean hasBaselineNumEVASuit(Vehicle vehicle) {
		boolean result = false;

		int numV = vehicle.findNumContainersOfType(EquipmentType.EVA_SUIT);

		int baseline = (int)(getMembers().size() * 1.5);

		int numP = 0;

		for (Person p: ((Crewable)vehicle).getCrew()) {
			if (p.getSuit() != null)
				numP++;
			else if (p.getInventorySuit() != null)
				numP++;
		}

		if (numV + numP > baseline)
			return true;

		return result;
	}

	/**
	 * Performs the embark from settlement phase of the mission.
	 *
	 * @param member the mission member currently performing the mission
	 */
	@Override
	protected void performEmbarkFromSettlementPhase(Worker member) {
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

		// Add the rover to a garage if possible.
		boolean	isRoverInAGarage = settlement.getBuildingManager().addToGarage(v);

		// Load vehicle if not fully loaded.
		if (!isVehicleLoaded()
			&& member.isInSettlement()
			// Note: randomly select this member to load resources for the rover
			// This allows person to do other important things such as eating
			&& RandomUtil.lessThanRandPercent(75)
			&& member instanceof Person) {
				Person person = (Person) member;

				boolean hasAnotherMission = false;
				Mission m = person.getMission();
				if (m != null && m != this)
					hasAnotherMission = true;
				if (!hasAnotherMission) {
					if (isRoverInAGarage && !person.getMind().getTaskManager().hasSameTask("LoadVehicleGarage")) {
						person.getMind().getTaskManager().addAPendingTask("LoadVehicleGarage", false);
					} else if (person.isNominallyFit() && !person.getMind().getTaskManager().hasSameTask("LoadVehicleEVA")) {
						person.getMind().getTaskManager().addAPendingTask("LoadVehicleEVA", false);
					}
				}
		}

		else {

			// Gets a random location within rover.
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(v);

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

						if (!isDone() && isRoverInAGarage) {

							if (settlement.findNumContainersOfType(EquipmentType.EVA_SUIT) > 1
									&& !hasBaselineNumEVASuit(v)) {

								EVASuit suit = InventoryUtil.getGoodEVASuitNResource(settlement, person);
								if (suit != null && !suit.transfer(v)) {
									logger.warning(person, "Unable to transfer a spare " + suit.getName() + " from "
											+ settlement + " to " + v + ".");
								}
							}
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

		// If rover is loaded and everyone is aboard, embark from settlement.
		if (!isDone()) {

			// Set the members' work shift to on-call to get ready
			for (Worker m : getMembers()) {
				Person pp = (Person) m;
				if (pp.getShiftType() != ShiftType.ON_CALL)
					pp.setShiftType(ShiftType.ON_CALL);
			}

			if (isEveryoneInRover()) {

				// Double check, this shoud never happen
				if (!isVehicleLoaded()) {
					logger.warning(v, "Not fully loaded when travelling");
				}

				// If the rover is in a garage, put the rover outside.
				if (v.isInAGarage()) {
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

		// If v0 is being towed by a vehicle, gets the towing vehicle
		Vehicle v1 = v0.getTowingVehicle();
		if (v1 != null)
			disembark(member, v1, disembarkSettlement);

		// If v0 is towing a vehicle, gets the towed vehicle
		Vehicle v2 = ((Rover)v0).getTowedVehicle();
		if (v2 != null)
			disembark(member, v2, disembarkSettlement);
	}

	/**
	 * Disembarks the vehicle and unload cargo.
	 *
	 * @param member
	 * @param v
	 * @param disembarkSettlement
	 */
	public void disembark(Worker member, Vehicle v, Settlement disembarkSettlement) {
		logger.info(v, 10_000, "Disembarked at " + disembarkSettlement.getName()
					+ " triggered by " + member.getName() +  ".");

		Rover rover = (Rover) v;
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

		// Test if this rover is towing another vehicle or is being towed
        boolean tethered = v.isBeingTowed() || rover.isTowingAVehicle();

        // Feels like these stesp should only be done once at the start of disembarking
		// Add vehicle to a garage if available.
		boolean isRoverInAGarage = false;
        if (!tethered) {
        	isRoverInAGarage = disembarkSettlement.getBuildingManager().addToGarage(v);
        }

		// Make sure the rover chasis is not overlapping a building structure in the settlement map
        if (!isRoverInAGarage) {
//        	rover.findNewParkingLoc();

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

        Set<Person> currentCrew = new HashSet<>(rover.getCrew());
		for (Person p : currentCrew) {
			if (p.isDeclaredDead()) {
				logger.fine(p, "Dead body will be retrieved from rover " + v.getName() + ".");
			}

			// Initiate an rescue operation
			// Note : Gets a lead person to perform it and give him a rescue badge
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
				// See if this person needs an EVA suit
		        getEVASuit(p, disembarkSettlement);
			}
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
		else if (rover.getCrewNum() > 0) {
			// Check to see if no one is in the rover, unload the resources and end phase.
			for (Worker mm  : getMembers()) {
				// Walk back to the airlock
				if (((Person)mm).isInVehicle() || ((Person)mm).isOutside())
					walkToAirlock(rover, ((Person)mm), disembarkSettlement);
			}
		}
		else {
			// Complete embark once everyone is out of the Vehicle
			// Leave the vehicle.
			leaveVehicle();
			// Reset the vehicle reservation
			v.correctVehicleReservation();
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

		if (isInAGarage() && !person.getMind().getTaskManager().hasSameTask("UnloadVehicleGarage")) {
			person.getMind().getTaskManager().addAPendingTask("UnloadVehicleGarage", false);
		}

		else if (person.isNominallyFit() && !EVAOperation.isGettingDark(person)
				&& !person.getMind().getTaskManager().hasSameTask("UnloadVehicleEVA")) {
			person.getMind().getTaskManager().addAPendingTask("UnloadVehicleEVA", false);
		}
	}

	/**
	 * Gets an EVA suit from the Vehicle. If one can not be found; then take
	 * one from the Settlement
	 *
	 * @param p
	 * @param disembarkSettlement
	 */
	protected void getEVASuit(Person p, Settlement disembarkSettlement) {
		if (p.getSuit() == null && p.isInVehicle()) {
			// Checks to see if the rover has any EVA suit
			EVASuit suit = getEVASuit(p);
			Vehicle v = getVehicle();

			if (suit == null) {

				logger.warning(p, "Could not find a working EVA suit in " + v + " and needed to wait.");

				// If the person does not have an EVA suit
				int availableSuitNum = disembarkSettlement.findNumContainersOfType(EquipmentType.EVA_SUIT);

				if (availableSuitNum > 1 && !hasBaselineNumEVASuit(v)) {
					// Deliver an EVA suit from the settlement to the rover
					// Note: Need to generate a task for a person to hand deliver an extra suit
					suit = InventoryUtil.getGoodEVASuitNResource(disembarkSettlement, p);
					if (suit != null) {
						boolean success = suit.transfer(v);
						if (success)
							logger.warning(p, "Just borrowed " + suit + " from " + disembarkSettlement + " to " + v);
						else
							logger.warning(p, "Unable to borrow a spare EVA suit from " + disembarkSettlement + " to " + v);
					}
				}
			}
		}
	}


	/**
	 * Finds a EVA suit in storage. Select one with the most resources already
	 * loaded.
	 *
	 * @param person Person needing the suit
	 * @return instance of EVASuit or null if none.
	 */
	protected EVASuit getEVASuit(Person p) {
		EVASuit goodSuit = null;
		double goodFullness = 0D;

		for (Equipment e : getVehicle().getEquipmentSet()) {
			if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
				EVASuit suit = (EVASuit)e;
				boolean malfunction = suit.getMalfunctionManager().hasMalfunction();
				double fullness = suit.getFullness();
				boolean lastOwner = p.equals(suit.getLastOwner());

				if (!malfunction && (fullness >= EVA_LOWEST_FILL)) {
					if (lastOwner) {
						// Pick this EVA suit since it has been used by the same person
						return suit;
					}
					else if (fullness > goodFullness){
						// For now, make a note of this suit but not selecting it yet.
						// Continue to look for a better suit
						goodSuit = suit;
						goodFullness = fullness;
					}
				}
			}
		}

		return goodSuit;
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
					assignTask(p, walk);
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
		
    				p.getMind().getTaskManager().addAPendingTask(RequestMedicalTreatment.SIMPLE_NAME, false);

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
	 * Rescue the person from the rover
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

		// Gets the settlement id
		int id = s.getIdentifier();
		// Store the person into a medical building
		BuildingManager.addToMedicalBuilding(p, id);

		// Register the historical event
		HistoricalEvent rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON,
				this,
				p.getPhysicalCondition().getHealthSituation(),
				p.getTaskDescription(),
				p.getName(),
				(r != null ? r.getNickName() : "Outside"),
				p.getLocationTag().getLocale(),
				p.getAssociatedSettlement().getName()
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
			if (!person.isBarelyFit()) {
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
	 * Checks to see if at least a minimum number of people are available for a
	 * mission at a settlement.
	 *
	 * @param settlement the settlement to check.
	 * @param minNum     minimum number of people required.
	 * @return true if minimum people available.
	 */
	public static boolean minAvailablePeopleAtSettlement(Settlement settlement, int minNum) {
		boolean result = false;
		int min = minNum;
		if (settlement != null) {

			String template = settlement.getTemplate();
			// Override the mininum num req if the settlement is too small
			if (template.toLowerCase().contains(PHASE_1)
					|| template.toLowerCase().contains(MINING)
					|| template.toLowerCase().contains(TRADING))
				min = 0;

			int numAvailable = 0;
			Iterator<Person> i = settlement.getIndoorPeople().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission())
					numAvailable++;
			}
			if (numAvailable >= min)
				result = true;
		}

		return result;
	}

	/**
	 * Checks if there is only one person at the associated settlement and he/she
	 * has a serious medical problem.
	 *
	 * @return true if serious medical problem
	 */
	protected final boolean hasDangerousMedicalProblemAtAssociatedSettlement() {
		boolean result = false;
		if (getAssociatedSettlement() != null) {
			if (getAssociatedSettlement().getIndoorPeopleCount() == 1) {
				Person person = (Person) getAssociatedSettlement().getIndoorPeople().toArray()[0];
				if (person.getPhysicalCondition().hasSeriousMedicalProblems())
					result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if the mission has an emergency situation.
	 *
	 * @return true if emergency.
	 */
	@Override
	protected final boolean hasEmergency() {
		boolean result = super.hasEmergency();
		if (hasDangerousMedicalProblemAtAssociatedSettlement())
			result = true;
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
		int numEVA = (int) (getPeopleNumber() * EXTRA_EVA_SUIT_FACTOR);
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
		// Note: currently, it uses methane as fuel. May switch to methanol in near future
		Map<Integer, Number> result = super.getResourcesNeededForTrip(useBuffer, distance);

		// Determine estimate time for trip.
		double time = getEstimatedTripTime(useBuffer, distance);
		double timeSols = time / 1000D;

		int people = getPeopleNumber();
		addLifeSupportResources(result, people, timeSols, useBuffer);

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
	protected static void addLifeSupportResources(Map<Integer, Number> result,
												  int crewNum, double timeSols,
												  boolean useBuffer) {

		// Determine life support supplies needed for trip.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			oxygenAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.OXYGEN_MARGIN;
		result.merge(OXYGEN_ID, oxygenAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			waterAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.WATER_MARGIN * FUEL_CELL_FACTOR; 
			// water is generated by fuel cells. no need of margins 
		result.merge(WATER_ID, waterAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			foodAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.FOOD_MARGIN;
		result.merge(FOOD_ID, foodAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));
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
	 * Find members for a mission, for RoverMissions all members must be at the same
	 * settlement.
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
			 for (Iterator<Worker> i = getMemberList().iterator(); 
					 i.hasNext();) {      
				 Worker member = i.next();
				if (member instanceof Person) {
					lastPerson = (Person) member;
					// Use Iterator's remove() method
					i.remove();
					// Adjust the work shift
					adjustShift(member);
				}
			 }
			
			 if (lastPerson != null) {
				lastPerson.getMind().setMission(null);
				if (getMembersNumber() < minMembers) {
					endMission(NOT_ENOUGH_MEMBERS);
					return false;
				} 
			}
		}

		return true;
	}
}
