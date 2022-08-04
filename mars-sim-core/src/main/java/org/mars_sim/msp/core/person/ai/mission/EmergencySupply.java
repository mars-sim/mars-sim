/*
 * Mars Simulation Project
 * EmergencySupply.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission for delivering emergency supplies from one settlement to another.
 * TODO externalize strings
 */
public class EmergencySupply extends RoverMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EmergencySupply.class.getName());

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.emergencySupply"); //$NON-NLS-1$

	// Static members
	private static final int MAX_MEMBERS = 2;

	private static final double VEHICLE_FUEL_DEMAND = 1000D;
	private static final double VEHICLE_FUEL_REMAINING_MODIFIER = 2D;
	private static final double MINIMUM_EMERGENCY_SUPPLY_AMOUNT = 100D;

	private static final int METHANE_ID = ResourceUtil.methaneID;

	public static final double BASE_STARTING_PROBABILITY = 20D;

	/** Mission phases. */
	private static final MissionPhase SUPPLY_DELIVERY_DISEMBARKING = new MissionPhase("Mission.phase.supplyDeliveryDisembarking");
	private static final MissionPhase SUPPLY_DELIVERY = new MissionPhase("Mission.phase.supplyDelivery");
	private static final MissionPhase LOAD_RETURN_TRIP_SUPPLIES = new MissionPhase("Mission.phase.loadReturnTripSupplies");
	private static final MissionPhase RETURN_TRIP_EMBARKING = new MissionPhase("Mission.phase.returnTripEmbarking");

	// Data members.
	private boolean outbound;

	private Settlement emergencySettlement;
	private Vehicle emergencyVehicle;

	private Map<Integer, Double> emergencyResources;
	private Map<Integer, Integer> emergencyEquipment;
	private Map<Integer, Integer> emergencyParts;

	// Static members

	/**
	 * Constructor.
	 *
	 * @param startingPerson the person starting the settlement.
	 */
	public EmergencySupply(Person startingPerson, boolean needsReview) {
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, MissionType.EMERGENCY_SUPPLY, startingPerson, null);

		if (isDone()) {
			return;
		}

		// Set the mission capacity.
		calculateMissionCapacity(MAX_MEMBERS);

		outbound = true;

		Settlement s = startingPerson.getSettlement();

		if (s != null && !isDone()) {

			// Determine emergency settlement.
			emergencySettlement = findSettlementNeedingEmergencySupplies(s, getRover());

			if (emergencySettlement != null) {

				// Update mission information for emergency settlement.
				addNavpoint(emergencySettlement);

				// Determine emergency supplies.
				determineNeededEmergencySupplies();

				// Recruit additional members to mission.
				if (!isDone()) {
					if (!recruitMembersForMission(startingPerson, MAX_MEMBERS))
						return;
				}
			} else {
				endMission(MissionStatus.NO_SETTLEMENT_FOUND_TO_DELIVER_EMERGENCY_SUPPLIES);
				logger.warning("No settlement could be found to deliver emergency supplies to.");
				return;
			}
		}

		if (s != null) {
			// Set initial phase
			setInitialPhase(needsReview);
		}
	}

	/**
	 * Constructor with explicit parameters.
	 *
	 * @param members             collection of mission members.
	 * @param emergencySettlement the starting settlement.
	 * @param rover               the rover used on the mission.
	 * @param description         the mission's description.
	 */
	public EmergencySupply(Collection<MissionMember> members, Settlement emergencySettlement,
			Map<Good, Integer> emergencyGoods, Rover rover, String description) {
		// Use RoverMission constructor.
		super(description, MissionType.EMERGENCY_SUPPLY, (Person) members.toArray()[0], rover);

		outbound = true;

		// Sets the mission capacity.
		calculateMissionCapacity(MAX_MEMBERS);

		// Set emergency settlement.
		this.emergencySettlement = emergencySettlement;
		addNavpoint(emergencySettlement);

		// Determine emergency supplies.
		emergencyResources = new HashMap<>();
		emergencyParts = new HashMap<>();
		emergencyEquipment = new HashMap<>();

		Iterator<Good> j = emergencyGoods.keySet().iterator();
		while (j.hasNext()) {
			Good good = j.next();
			int amount = emergencyGoods.get(good);
			switch(good.getCategory()) {
			case AMOUNT_RESOURCE:
				emergencyResources.put(good.getID(), (double) amount);
				break;

			case ITEM_RESOURCE:
				emergencyParts.put(good.getID(), amount);
				break;

			case EQUIPMENT:
			case CONTAINER:
				emergencyEquipment.put(good.getID(), amount);
				break;

			case VEHICLE:
				String vehicleType = good.getName();
				Iterator<Vehicle> h = getStartingSettlement().getParkedVehicles().iterator();
				while (h.hasNext()) {
					Vehicle vehicle = h.next();
					if (vehicleType.equalsIgnoreCase(vehicle.getDescription())) {
						if ((vehicle != getVehicle()) && !vehicle.isReserved()) {
							emergencyVehicle = vehicle;
							break;
						}
					}
				}
				break;
				
			case ROBOT:
			default: 
			}
		}

		// Add mission members.
		addMembers(members, false);

		// Set initial phase
		setInitialPhase(false);
	}

	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (isCurrentNavpointSettlement()) {
					if (outbound) {
						setPhase(SUPPLY_DELIVERY_DISEMBARKING, emergencySettlement.getName());
					} else {
						startDisembarkingPhase();
					}
				}
			}

			else if (SUPPLY_DELIVERY_DISEMBARKING.equals(getPhase())) {
				setPhase(SUPPLY_DELIVERY, emergencySettlement.getName());
			}

			else if (SUPPLY_DELIVERY.equals(getPhase())) {
				// Check if vehicle can hold enough supplies for mission.
				if (!isVehicleLoadable()) {
					endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
				}
				else {
					setPhase(LOAD_RETURN_TRIP_SUPPLIES, emergencySettlement.getName());
				}
			}

			else if (LOAD_RETURN_TRIP_SUPPLIES.equals(getPhase())) {
				setPhase(RETURN_TRIP_EMBARKING, emergencySettlement.getName());
			}

			else if (RETURN_TRIP_EMBARKING.equals(getPhase())) {
				startTravellingPhase();
			}

			else {
				handled = false;
			}
		}
		return handled;
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		// NOTE: The following 4 phases are unique to this mission
		if (SUPPLY_DELIVERY_DISEMBARKING.equals(getPhase())) {
			performSupplyDeliveryDisembarkingPhase(member);
		} else if (SUPPLY_DELIVERY.equals(getPhase())) {
			performSupplyDeliveryPhase(member);
		} else if (LOAD_RETURN_TRIP_SUPPLIES.equals(getPhase())) {
			performLoadReturnTripSuppliesPhase(member);
		} else if (RETURN_TRIP_EMBARKING.equals(getPhase())) {
			performReturnTripEmbarkingPhase(member);
		}
	}

	@Override
	protected void performEmbarkFromSettlementPhase(MissionMember member) {
		super.performEmbarkFromSettlementPhase(member);

		// Set emergency vehicle (if any) to be towed.
		if (!isDone() && (getRover().getTowedVehicle() == null)) {
			if (emergencyVehicle != null) {
				emergencyVehicle.setReservedForMission(true);
				getRover().setTowedVehicle(emergencyVehicle);
				emergencyVehicle.setTowingVehicle(getRover());
				getStartingSettlement().removeParkedVehicle(emergencyVehicle);
			}
		}
	}

	@Override
	protected void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement) {

		// Unload towed vehicle if any.
		if (!isDone() && (getRover().getTowedVehicle() != null && emergencyVehicle != null)) {
			emergencyVehicle.setReservedForMission(false);

			disembarkSettlement.addParkedVehicle(emergencyVehicle);

			getRover().setTowedVehicle(null);

			emergencyVehicle.setTowingVehicle(null);

			emergencyVehicle.findNewParkingLoc();
		}

		super.performDisembarkToSettlementPhase(member, disembarkSettlement);
	}

	/**
	 * Perform the supply delivery disembarking phase.
	 *
	 * @param member the member performing the phase.
	 */
	private void performSupplyDeliveryDisembarkingPhase(MissionMember member) {

		// If rover is not parked at settlement, park it.
		if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {

			emergencySettlement.addParkedVehicle(getVehicle());

			// Add vehicle to a garage if available.
			if (!emergencySettlement.getBuildingManager().addToGarage(getVehicle())) {
				// or else re-orient it
//				getVehicle().findNewParkingLoc();
			}
		}

		// Have member exit rover if necessary.
		if (member.isInSettlement()) {

			// Get random inhabitable building at emergency settlement.
			Building destinationBuilding = emergencySettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(destinationBuilding);

				if (member instanceof Person) {
					Person person = (Person) member;
					Walk walk = Walk.createWalkingTask(person, adjustedLoc, 0, destinationBuilding);
					if (walk != null) {
						assignTask(person, walk);
					}
					else {
						logger.severe("Unable to walk to building " + destinationBuilding);
					}
				}
				else if (member instanceof Robot) {
					Robot robot = (Robot) member;
					Walk walkingTask = Walk.createWalkingTask(robot, adjustedLoc, destinationBuilding);
					if (walkingTask != null) {
						assignTask(robot, walkingTask);
					}
					else {
						logger.severe("Unable to walk to building " + destinationBuilding);
					}
				}
			}
			else {
				logger.severe("No inhabitable buildings at " + emergencySettlement);
				endMission(MissionStatus.NO_INHABITABLE_BUILDING);
			}
		}

		// End the phase when everyone is out of the rover.
		if (isNoOneInRover()) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the supply delivery phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performSupplyDeliveryPhase(MissionMember member) {

		// Unload towed vehicle (if necessary).
		if (getRover().getTowedVehicle() != null) {
			emergencyVehicle.setReservedForMission(false);
			getRover().setTowedVehicle(null);
			emergencyVehicle.setTowingVehicle(null);
			emergencySettlement.addParkedVehicle(emergencyVehicle);
			emergencyVehicle.findNewParkingLoc();
		}

		// Unload rover if necessary.
		boolean roverUnloaded = getRover().getStoredMass() == 0D;
		if (!roverUnloaded) {
			// Random chance of having person unload (this allows person to do other things
			// sometimes)
			if (RandomUtil.lessThanRandPercent(50)) {
				// TODO Refactor to allow robots.
				if (member instanceof Person) {
					Person person = (Person) member;
					if (isInAGarage()) {
						assignTask(person, new UnloadVehicleGarage(person, getRover()));
					} else {
						// Check if it is day time.
						if (!EVAOperation.isGettingDark(person) && person.isFit()) {
							assignTask(person, new UnloadVehicleEVA(person, getRover()));
						}
					}
				}

				return;
			}
		} else {
			outbound = false;
			addNavpoint(getStartingSettlement());
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the load return trip supplies phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performLoadReturnTripSuppliesPhase(MissionMember member) {

		if (!isDone() && !isVehicleLoaded()) {
			// Random chance of having person load (this allows person to do other things
			// sometimes)
			if (RandomUtil.lessThanRandPercent(50)) {
				// TODO Refactor to allow robots.
				if (member instanceof Person) {
					Person person = (Person) member;
					if (isInAGarage()) {
						assignTask(person,
								new LoadVehicleGarage(person, this));
					} else {
						// Check if it is day time.
						if (EVAOperation.isGettingDark(person)) {
							assignTask(person,
									new LoadVehicleEVA(person, this));
						}
					}
				}
			}
		} else {
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the return trip embarking phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performReturnTripEmbarkingPhase(MissionMember member) {

		if (member.isInVehicle()) {

			// Move person to random location within rover.
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalRelativePosition(getVehicle());
			// TODO Refactor
			if (member instanceof Person) {
				Person person = (Person) member;
				if (!person.isDeclaredDead()) {

					EVASuit suit0 = getEVASuit(person);
					if (suit0 == null) {
						EVASuit suit1 = InventoryUtil.getGoodEVASuitNResource(emergencySettlement, person);
						if (!suit1.transfer(getVehicle())) {
							logger.warning(person, "EVA suit not provided for by " + emergencySettlement);
						}
					}

					// If person is not aboard the rover, board rover.
					Walk walk = Walk.createWalkingTask(person, adjustedLoc, 0, getVehicle());
					if (walk != null) {
						assignTask(person, walk);
					}
					else {
						logger.severe(person.getName() + " unable to enter rover " + getVehicle());
						endMission(MissionStatus.CANNOT_ENTER_ROVER);
					}
				}
			}

			else if (member instanceof Robot) {
				Robot robot = (Robot) member;
				// If robot is not aboard the rover, board rover.
				Walk walkingTask = Walk.createWalkingTask(robot, adjustedLoc, getVehicle());
				if (walkingTask != null) {
					assignTask(robot, walkingTask);
				}
				else {
					logger.severe(robot.getName() + " unable to enter rover " + getVehicle());
					endMission(MissionStatus.CANNOT_ENTER_ROVER);
				}
			}
		}

		// If rover is loaded and everyone is aboard, embark from settlement.
		if (isEveryoneInRover()) {

			// If the rover is in a garage, put the rover outside.
			BuildingManager.removeFromGarage(getVehicle());

			// Embark from settlement
			emergencySettlement.removeParkedVehicle(getVehicle());
			setPhaseEnded(true);
		}
	}

	/**
	 * Finds a settlement within range that needs emergency supplies.
	 *
	 * @param startingSettlement the starting settlement.
	 * @param rover              the rover to carry the supplies.
	 * @return settlement needing supplies or null if none found.
	 */
	public static Settlement findSettlementNeedingEmergencySupplies(Settlement startingSettlement, Rover rover) {

		Settlement result = null;

		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement settlement = i.next();

			if (settlement != startingSettlement 
				&& !settlement.equals(startingSettlement)
				// Check if an emergency supply mission is currently ongoing to settlement.
				&& !hasCurrentEmergencySupplyMission(settlement)) {

				// Check if settlement is within rover range.
				double settlementRange = Coordinates.computeDistance(settlement.getCoordinates(), startingSettlement.getCoordinates());
				if (settlementRange <= (rover.getRange(MissionType.EMERGENCY_SUPPLY) * .8D)) {

					// Find what emergency supplies are needed at settlement.
					Map<Integer, Double> emergencyResourcesNeeded = getEmergencyAmountResourcesNeeded(settlement);
					Map<Integer, Integer> emergencyContainersNeeded = getContainersRequired(
							emergencyResourcesNeeded);

					if (!emergencyResourcesNeeded.isEmpty()) {

						// Check if starting settlement has enough supplies itself to send emergency
						// supplies.
						if (hasEnoughSupplies(startingSettlement, emergencyResourcesNeeded,
								emergencyContainersNeeded)) {
							result = settlement;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a settlement has sufficient supplies to send an emergency supply
	 * mission.
	 *
	 * @param startingSettlement        the starting settlement.
	 * @param emergencyResourcesNeeded  the emergency resources needed.
	 * @param emergencyContainersNeeded the containers needed to hold emergency
	 *                                  resources.
	 * @return true if enough supplies at starting settlement.
	 */
	private static boolean hasEnoughSupplies(Settlement startingSettlement,
			Map<Integer, Double> emergencyResourcesNeeded, Map<Integer, Integer> emergencyContainersNeeded) {

		boolean result = true;

		// Check if settlement has enough extra resources to send as emergency supplies.
		Iterator<Integer> i = emergencyResourcesNeeded.keySet().iterator();
		while (i.hasNext() && result) {
			Integer resource = i.next();
			double amountRequired = emergencyResourcesNeeded.get(resource);
			double amountNeededAtStartingSettlement = getResourceAmountNeededAtStartingSettlement(startingSettlement,
					resource);
			double amountAvailable = startingSettlement.getAmountResourceStored(resource);
			// Adding tracking demand
			if (amountAvailable < (amountRequired + amountNeededAtStartingSettlement)) {
				result = false;
			}
		}

		// Check if settlement has enough empty containers to hold emergency resources.
		Iterator<Integer> j = emergencyContainersNeeded.keySet().iterator();
		while (j.hasNext() && result) {
			Integer containerType = j.next();
			int numberRequired = emergencyContainersNeeded.get(containerType);
			EquipmentType type = EquipmentType.convertID2Type(containerType);
			int numberAvailable = startingSettlement.findNumEmptyContainersOfType(type, false);

			// Note: add tracking demand for containers
			if (numberAvailable < numberRequired) {
				result = false;
			}
		}

		return result;
	}

	/**
	 * Gets the amount of a resource needed at the starting settlement.
	 *
	 * @param startingSettlement the starting settlement.
	 * @param resource           the amount resource.
	 * @return amount (kg) needed.
	 */
	private static double getResourceAmountNeededAtStartingSettlement(Settlement startingSettlement, Integer resource) {

		double result = 0D;

		if (ResourceUtil.isLifeSupport(resource)) {
			double amountNeededSol = 0D;
			if (resource.equals(OXYGEN_ID))
				amountNeededSol = personConfig.getNominalO2ConsumptionRate();
			if (resource.equals(WATER_ID))
				amountNeededSol = personConfig.getWaterConsumptionRate();
			if (resource.equals(FOOD_ID))
				amountNeededSol = personConfig.getFoodConsumptionRate();

			double amountNeededOrbit = amountNeededSol * (MarsClock.SOLS_PER_MONTH_LONG * 3D);
			int numPeople = startingSettlement.getNumCitizens();
			result = numPeople * amountNeededOrbit;
		} else {
			if (resource.equals(METHANE_ID)) {
				Iterator<Vehicle> i = startingSettlement.getAllAssociatedVehicles().iterator();
				while (i.hasNext()) {
					double fuelDemand = i.next().getAmountResourceCapacity(resource);
					result += fuelDemand * VEHICLE_FUEL_REMAINING_MODIFIER;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a settlement has a current mission to deliver emergency supplies to
	 * it.
	 *
	 * @param settlement the settlement.
	 * @return true if current emergency supply mission.
	 */
	private static boolean hasCurrentEmergencySupplyMission(Settlement settlement) {

		boolean result = false;

		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof EmergencySupply) {
				EmergencySupply emergencyMission = (EmergencySupply) mission;
				if (settlement.equals(emergencyMission.getEmergencySettlement())) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Determines needed emergency supplies at the destination settlement.
	 */
	private void determineNeededEmergencySupplies() {

		// Determine emergency resources needed.
		emergencyResources = getEmergencyAmountResourcesNeeded(emergencySettlement);

		// Determine containers needed to hold emergency resources.
		Map<Integer, Integer> containers = getContainersRequired(emergencyResources);
		emergencyEquipment = new HashMap<>(containers.size());
		Iterator<Integer> i = containers.keySet().iterator();
		while (i.hasNext()) {
			Integer container = i.next();
			int number = containers.get(container);
			emergencyEquipment.put(container, number);
		}

		// Determine emergency parts needed.
		emergencyParts = getEmergencyPartsNeeded(emergencySettlement);
	}

	/**
	 * Gets the emergency amount resource supplies needed at a settlement.
	 *
	 * @param settlement the settlement
	 * @return map of amount resources and amounts needed.
	 */
	private static Map<Integer, Double> getEmergencyAmountResourcesNeeded(Settlement settlement) {

		Map<Integer, Double> result = new HashMap<>();

		double solsMonth = MarsClock.SOLS_PER_MONTH_LONG;
		int numPeople = settlement.getNumCitizens();

		// Determine oxygen amount needed.
		double oxygenAmountNeeded = personConfig.getNominalO2ConsumptionRate() * numPeople * solsMonth;//* Mission.OXYGEN_MARGIN;
		double oxygenAmountAvailable = settlement.getAmountResourceStored(OXYGEN_ID);

		oxygenAmountAvailable += getResourcesOnMissions(settlement, OXYGEN_ID);
		if (oxygenAmountAvailable < oxygenAmountNeeded) {
			double oxygenAmountEmergency = oxygenAmountNeeded - oxygenAmountAvailable;
			if (oxygenAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				oxygenAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(OXYGEN_ID, oxygenAmountEmergency);
		}

		// Determine water amount needed.
		double waterAmountNeeded = personConfig.getWaterConsumptionRate() * numPeople * solsMonth;// * Mission.WATER_MARGIN;
		double waterAmountAvailable = settlement.getAmountResourceStored(WATER_ID);

		waterAmountAvailable += getResourcesOnMissions(settlement, WATER_ID);
		if (waterAmountAvailable < waterAmountNeeded) {
			double waterAmountEmergency = waterAmountNeeded - waterAmountAvailable;
			if (waterAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				waterAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(WATER_ID, waterAmountEmergency);
		}

		// Determine food amount needed.
		double foodAmountNeeded = personConfig.getFoodConsumptionRate() * numPeople * solsMonth;// * Mission.FOOD_MARGIN;
		double foodAmountAvailable = settlement.getAmountResourceStored(FOOD_ID);

		foodAmountAvailable += getResourcesOnMissions(settlement, FOOD_ID);
		if (foodAmountAvailable < foodAmountNeeded) {
			double foodAmountEmergency = foodAmountNeeded - foodAmountAvailable;
			if (foodAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				foodAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(FOOD_ID, foodAmountEmergency);
		}

		// Determine methane amount needed.
		double methaneAmountNeeded = VEHICLE_FUEL_DEMAND;
		double methaneAmountAvailable = settlement.getAmountResourceStored(METHANE_ID);

		methaneAmountAvailable += getResourcesOnMissions(settlement, METHANE_ID);
		if (methaneAmountAvailable < methaneAmountNeeded) {
			double methaneAmountEmergency = methaneAmountNeeded - methaneAmountAvailable;
			if (methaneAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				methaneAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(METHANE_ID, methaneAmountEmergency);
		}

		return result;
	}

	/**
	 * Gets the amount of a resource on associated rover missions.
	 *
	 * @param settlement the settlement.
	 * @param resource   the amount resource.
	 * @return the amount of resource on missions.
	 */
	private static double getResourcesOnMissions(Settlement settlement, Integer resource) {
		double result = 0D;

		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof RoverMission) {
				RoverMission roverMission = (RoverMission) mission;
				boolean isTradeMission = roverMission instanceof Trade;
				boolean isEmergencySupplyMission = roverMission instanceof EmergencySupply;
				if (!isTradeMission && !isEmergencySupplyMission) {
					Rover rover = roverMission.getRover();
					if (rover != null) {
						result += rover.getAmountResourceStored(resource);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the containers required to hold a collection of resources.
	 *
	 * @param resourcesMap the map of resources and their amounts.
	 * @return map of containers and the number required of each.
	 */
	private static Map<Integer, Integer> getContainersRequired(Map<Integer, Double> resourcesMap) {

		Map<Integer, Integer> result = new HashMap<>();

		Iterator<Integer> i = resourcesMap.keySet().iterator();
		while (i.hasNext()) {
			Integer id = i.next();

			if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				double amount = (double) resourcesMap.get(id);
				EquipmentType containerType = ContainerUtil.getContainerClassToHoldResource(id);
				int containerID = EquipmentType.getResourceID(containerType);
				double capacity = ContainerUtil.getContainerCapacity(containerType);
				int numContainers = (int) Math.ceil(amount / capacity);
				if (result.containsKey(containerID)) {
					numContainers += (int) (result.get(containerID));
				}

				result.put(containerID, numContainers);

			}
		}

		return result;
	}

	/**
	 * Gets the emergency part supplies needed at a settlement.
	 *
	 * @param settlement the settlement
	 * @return map of parts and numbers needed.
	 */
	private static Map<Integer, Integer> getEmergencyPartsNeeded(Settlement settlement) {

		Map<Integer, Integer> result = new HashMap<>();

		// Get all malfunctionables associated with settlement.
		Iterator<Malfunctionable> i = MalfunctionFactory.getAssociatedMalfunctionables(settlement).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();

			// Determine parts needed but not available for repairs.
			Iterator<Malfunction> j = entity.getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				Malfunction malfunction = j.next();
				Map<Integer, Integer> repairParts = malfunction.getRepairParts();
				Iterator<Integer> k = repairParts.keySet().iterator();
				while (k.hasNext()) {
					Integer part = k.next();
					int number = repairParts.get(part);
					if (!settlement.getItemResourceIDs().contains(part)) {
						if (result.containsKey(part)) {
							number += result.get(part).intValue();
						}
						result.put(part, number);
					}
				}
			}

			// Determine parts needed but not available for maintenance.
			Map<Integer, Integer> maintParts = entity.getMalfunctionManager().getMaintenanceParts();
			Iterator<Integer> l = maintParts.keySet().iterator();
			while (l.hasNext()) {
				Integer part = l.next();
				int number = maintParts.get(part);
				if (!settlement.getItemResourceIDs().contains(part)) {
					if (result.containsKey(part)) {
						number += result.get(part).intValue();
					}
					result.put(part, number);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the settlement that emergency supplies are being delivered to.
	 *
	 * @return settlement
	 */
	public Settlement getEmergencySettlement() {
		return emergencySettlement;
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {

		return new HashMap<>(0);
	}

	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		if ((result == 0) && isUsableVehicle(firstVehicle) && isUsableVehicle(secondVehicle)) {
			// Check if one has more general cargo capacity than the other.
			double firstCapacity = firstVehicle.getCargoCapacity();
			double secondCapacity = secondVehicle.getCargoCapacity();
			if (firstCapacity > secondCapacity) {
				result = 1;
			} else if (secondCapacity > firstCapacity) {
				result = -1;
			}

			// Vehicle with superior range should be ranked higher.
			if (result == 0) {
				if (firstVehicle.getRange(MissionType.EMERGENCY_SUPPLY) > secondVehicle.getRange(MissionType.EMERGENCY_SUPPLY)) {
					result = 1;
				} else if (firstVehicle.getRange(MissionType.EMERGENCY_SUPPLY) < secondVehicle.getRange(MissionType.EMERGENCY_SUPPLY)) {
					result = -1;
				}
			}
		}

		return result;
	}

	@Override
	protected Map<Integer, Number> getRequiredResourcesToLoad() {
		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(true);

		// Add any emergency resources needed.
		if (outbound && (emergencyResources != null)) {

			Iterator<Integer> i = emergencyResources.keySet().iterator();
			while (i.hasNext()) {
				Integer resource = i.next();
				double amount = emergencyResources.get(resource);
				if (result.containsKey(resource)) {
					amount += (Double) result.get(resource);
				}
				result.put(resource, amount);
			}
		}

		return result;
	}

	@Override
	protected Map<Integer, Number> getOptionalResourcesToLoad() {

		Map<Integer, Number> result = super.getOptionalResourcesToLoad();

		// Add any emergency parts needed.
		if (outbound && (emergencyParts != null)) {

			Iterator<Integer> i = emergencyParts.keySet().iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				int num = emergencyParts.get(part);
				if (result.containsKey(part)) {
					num += (Integer) result.get(part);
				}
				result.put(part, num);
			}
		}

		return result;
	}

	@Override
	public Map<Integer, Integer> getRequiredEquipmentToLoad() {

		Map<Integer, Integer> result = getEquipmentNeededForRemainingMission(true);

		// Add any emergency equipment needed.
		if (outbound && (emergencyEquipment != null)) {

			Iterator<Integer> i = emergencyEquipment.keySet().iterator();
			while (i.hasNext()) {
				Integer equipment = i.next();
				int num = emergencyEquipment.get(equipment);
				if (result.containsKey(equipment)) {
					num += (Integer) result.get(equipment);
				}
				result.put(equipment, num);
			}
		}

		return result;
	}

	/**
	 * Gets the emergency supplies as a goods map.
	 *
	 * @return map of goods and amounts.
	 */
	public Map<Good, Integer> getEmergencySuppliesAsGoods() {
		Map<Good, Integer> result = new HashMap<Good, Integer>();

		// Add emergency resources.
		Iterator<Integer> i = emergencyResources.keySet().iterator();
		while (i.hasNext()) {
			Integer id = i.next();
			double amount = emergencyResources.get(id);
			result.put(GoodsUtil.getGood(id), (int) amount);
		}

		// Add emergency parts.
		Iterator<Integer> j = emergencyParts.keySet().iterator();
		while (j.hasNext()) {
			Integer id = j.next();
			int number = emergencyParts.get(id);
			result.put(GoodsUtil.getGood(id), number);
		}

		// Add emergency equipment.
		Iterator<Integer> k = emergencyEquipment.keySet().iterator();
		while (k.hasNext()) {
			Integer id = k.next();
			int number = emergencyEquipment.get(id);
			result.put(GoodsUtil.getGood(id), number);
		}

		// Add emergency vehicle.
		if (emergencyVehicle != null) {
			Good vehicleGood = GoodsUtil.getVehicleGood(emergencyVehicle.getDescription());
			result.put(vehicleGood, 1);
		}

		return result;
	}

	@Override
	protected void endMission(MissionStatus endStatus) {
		super.endMission(endStatus);

		// Unreserve any towed vehicles.
		if (getRover() != null) {
			if (getRover().getTowedVehicle() != null) {
				Vehicle towed = getRover().getTowedVehicle();
				towed.setReservedForMission(false);
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		emergencySettlement = null;

		if (emergencyResources != null) {
			emergencyResources.clear();
			emergencyResources = null;
		}

		if (emergencyEquipment != null) {
			emergencyEquipment.clear();
			emergencyEquipment = null;
		}

		if (emergencyParts != null) {
			emergencyParts.clear();
			emergencyParts = null;
		}
	}
}
