/**
 * Mars Simulation Project
 * EmergencySupplyMission.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EnterAirlock;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodType;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission for delivering emergency supplies from one settlement to another.
 */
public class EmergencySupplyMission
extends RoverMission
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(EmergencySupplyMission.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = "Deliver Emergency Supplies";

	// Static members
	private static final int MAX_MEMBERS = 2;
	private static final double BASE_STARTING_PROBABILITY = 100D;
	private static final double VEHICLE_FUEL_DEMAND = 1000D;
	private static final double VEHICLE_FUEL_REMAINING_MODIFIER = 2D;
	private static final double MINIMUM_EMERGENCY_SUPPLY_AMOUNT = 100D;

	// Mission phases.
	public static final String SUPPLY_DELIVERY_DISEMBARKING = "Supply Delivery Disembarking";
	public static final String SUPPLY_DELIVERY = "Unload Supply Goods";
	public static final String LOAD_RETURN_TRIP_SUPPLIES = "Load Return Trip Supplies";
	public static final String RETURN_TRIP_EMBARKING = "Return Trip Embarking";

	// Data members.
	private Settlement emergencySettlement;
	private boolean outbound;
	private Map<AmountResource, Double> emergencyResources;
	private Map<Class, Integer> emergencyEquipment;
	private Map<Part, Integer> emergencyParts;
	private Vehicle emergencyVehicle;

	/**
	 * Constructor.
	 * @param startingPerson the person starting the settlement.
	 */
	public EmergencySupplyMission(Person startingPerson) {
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson);

		// Set the mission capacity.
		setMissionCapacity(MAX_MEMBERS);
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
		if (availableSuitNum < getMissionCapacity()) {
			setMissionCapacity(availableSuitNum);
		}

		outbound = true;

		if (!isDone()) {

			// Initialize data members
			setStartingSettlement(startingPerson.getSettlement());

			// Determine emergency settlement.
			emergencySettlement = findSettlementNeedingEmergencySupplies(getStartingSettlement(), getRover());

			if (emergencySettlement != null) {

				// Update mission information for emergency settlement.
				addNavpoint(new NavPoint(emergencySettlement.getCoordinates(), emergencySettlement,
						emergencySettlement.getName()));

				// Determine emergency supplies.
				determineNeededEmergencySupplies();

				// Recruit additional people to mission.
				if (!isDone()) {
					recruitPeopleForMission(startingPerson);
				}
			}
			else {
				endMission("No settlement could be found to deliver emergency supplies to.");
			}
		}

		// Add emergency supply mission phases.
		addPhase(SUPPLY_DELIVERY_DISEMBARKING);
		addPhase(SUPPLY_DELIVERY);
		addPhase(LOAD_RETURN_TRIP_SUPPLIES);
		addPhase(RETURN_TRIP_EMBARKING);

		// Set initial phase
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
		if (logger.isLoggable(Level.INFO)) {
			if (startingPerson != null && getRover() != null) {
				logger.info(startingPerson.getName() + " starting emergency supply mission from " + getStartingSettlement() + 
						" to " + getEmergencySettlement() + " using " + getRover().getName());
			}
		}
	}

	/**
	 * Constructor with explicit parameters.
	 * @param members collection of mission members.
	 * @param startingSettlement the starting settlement.
	 * @param emergencySettlement the settlement to deliver emergency supplies to.
	 * @param rover the rover used on the mission.
	 * @param description the mission's description.
	 */
	public EmergencySupplyMission(
		Collection<Person> members, Settlement startingSettlement, 
		Settlement emergencySettlement, Map<Good, Integer> emergencyGoods, 
		Rover rover, String description
	) {
		// Use RoverMission constructor.
		super(description, (Person) members.toArray()[0], 1, rover);

		outbound = true;

		// Initialize data members
		setStartingSettlement(startingSettlement);

		// Sets the mission capacity.
		setMissionCapacity(MAX_MEMBERS);
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
		if (availableSuitNum < getMissionCapacity()) {
			setMissionCapacity(availableSuitNum);
		}

		// Set emergency settlement.
		this.emergencySettlement = emergencySettlement;
		addNavpoint(new NavPoint(emergencySettlement.getCoordinates(), emergencySettlement,
				emergencySettlement.getName()));

		// Determine emergency supplies.
		emergencyResources = new HashMap<AmountResource, Double>();
		emergencyParts = new HashMap<Part, Integer>();
		emergencyEquipment = new HashMap<Class, Integer>();

		Iterator<Good> j = emergencyGoods.keySet().iterator();
		while (j.hasNext()) {
			Good good = j.next();
			int amount = emergencyGoods.get(good);
			if (GoodType.AMOUNT_RESOURCE == good.getCategory()) {
				AmountResource resource = (AmountResource) good.getObject();
				emergencyResources.put(resource, (double) amount);
			}
			else if (GoodType.ITEM_RESOURCE == good.getCategory()) {
				Part part = (Part) good.getObject();
				emergencyParts.put(part, amount);
			}
			else if (GoodType.EQUIPMENT == good.getCategory()) {
				Class<? extends Equipment> equipmentClass = good.getClassType();
				emergencyEquipment.put(equipmentClass, amount);
			}
			else if (GoodType.VEHICLE == good.getCategory()) {
				String vehicleType = good.getName();
				Iterator<Vehicle> h = startingSettlement.getParkedVehicles().iterator();
				while (h.hasNext()) {
					Vehicle vehicle = h.next();
					if (vehicleType.equalsIgnoreCase(vehicle.getDescription())) {
						if ((vehicle != getVehicle()) && !vehicle.isReserved()) {
							emergencyVehicle = vehicle;
							break;
						}
					}
				}
			}
		}

		// Add mission members.
		Iterator<Person> i = members.iterator();
		while (i.hasNext()) {
			i.next().getMind().setMission(this);
		}

		// Add emergency supply mission phases.
		addPhase(SUPPLY_DELIVERY_DISEMBARKING);
		addPhase(SUPPLY_DELIVERY);
		addPhase(LOAD_RETURN_TRIP_SUPPLIES);
		addPhase(RETURN_TRIP_EMBARKING);

		// Set initial phase
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
		if (logger.isLoggable(Level.INFO)) {
			Person startingPerson = (Person) members.toArray()[0];
			if (startingPerson != null && getRover() != null) {
				logger.info(startingPerson.getName() + " starting emergency supply mission from " + getStartingSettlement() + 
						" to " + getEmergencySettlement() + "on " + getRover().getName());
			}
		}
	}

	/** 
	 * Gets the weighted probability that a given person would start this mission.
	 * @param person the given person
	 * @return the weighted probability
	 */
	public static double getNewMissionProbability(Person person) {

		double missionProbability = 0D;

		// Determine job modifier.
		Job job = person.getMind().getJob();
		double jobModifier = 0D;
		if (job != null) {
			jobModifier = job.getStartMissionProbabilityModifier(EmergencySupplyMission.class);
		}

		// Check if person is in a settlement.
		boolean inSettlement = person.getLocationSituation().equals(Person.INSETTLEMENT);

		if (inSettlement && (jobModifier > 0D)) {

			// Check if mission is possible for person based on their circumstance.
			boolean missionPossible = true;
			Settlement settlement = person.getSettlement();

			// Check if available rover.
			if (!areVehiclesAvailable(settlement, false)) {
				missionPossible = false;
			}

			// Check if available backup rover.
			if (!hasBackupRover(settlement)) {
				missionPossible = false;
			}

			// Check if minimum number of people are available at the settlement.
			// Plus one to hold down the fort.
			if (!minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_PEOPLE + 1)) {
				missionPossible = false;
			}

			// Check if minimum number of EVA suits at settlement.
			if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_PEOPLE) {
				missionPossible = false;
			}

			// Check for embarking missions.
			if (VehicleMission.hasEmbarkingMissions(settlement)) {
				missionPossible = false;
			}

			// Check if settlement has enough basic resources for a rover mission.
			if (!RoverMission.hasEnoughBasicResources(settlement)) {
				missionPossible = false;
			}

			if (missionPossible) {

				Rover rover = (Rover) getVehicleWithGreatestRange(settlement, false);
				if (rover != null) {
					Settlement targetSettlement = findSettlementNeedingEmergencySupplies(settlement, rover);
					if (targetSettlement == null) {
						missionPossible = false;
					}
				}
			}

			if (missionPossible) {
				missionProbability = BASE_STARTING_PROBABILITY;

				// Crowding modifier.
				int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
				if (crowding > 0) missionProbability *= (crowding + 1);

				// Job modifier.
				missionProbability *= jobModifier;
			}
		}

		return missionProbability;
	}

	@Override
	protected void determineNewPhase() {
		if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
		} else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				if (outbound) {
					setPhase(SUPPLY_DELIVERY_DISEMBARKING);
					setPhaseDescription("Disembarking at " + emergencySettlement);
				} else {
					setPhase(VehicleMission.DISEMBARKING);
					setPhaseDescription("Disembarking at " + getCurrentNavpoint().getDescription());
				}
			}
		} else if (SUPPLY_DELIVERY_DISEMBARKING.equals(getPhase())) {
			setPhase(SUPPLY_DELIVERY);
			setPhaseDescription("Delivering emergency supplies to " + emergencySettlement);
		} else if (SUPPLY_DELIVERY.equals(getPhase())) {
			setPhase(LOAD_RETURN_TRIP_SUPPLIES);
			setPhaseDescription("Loading return trip supplies at " + emergencySettlement);
		} else if (LOAD_RETURN_TRIP_SUPPLIES.equals(getPhase())) {
			setPhase(RETURN_TRIP_EMBARKING);
			setPhaseDescription("Embarking at " + emergencySettlement);
		} else if (RETURN_TRIP_EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
		} else if (DISEMBARKING.equals(getPhase())) {
			endMission("Successfully disembarked.");
		}
	}

	@Override
	protected void performPhase(Person person) {
		super.performPhase(person);
		if (SUPPLY_DELIVERY_DISEMBARKING.equals(getPhase())) {
			performSupplyDeliveryDisembarkingPhase(person);
		} else if (SUPPLY_DELIVERY.equals(getPhase())) {
			performSupplyDeliveryPhase(person);
		} else if (LOAD_RETURN_TRIP_SUPPLIES.equals(getPhase())) {
			performLoadReturnTripSuppliesPhase(person);
		} else if (RETURN_TRIP_EMBARKING.equals(getPhase())) {
			performReturnTripEmbarkingPhase(person);
		}
	}

	@Override
	protected void performEmbarkFromSettlementPhase(Person person) {
		super.performEmbarkFromSettlementPhase(person);

		// Set emergency vehicle (if any) to be towed.
		if (!isDone() && (getRover().getTowedVehicle() == null)) {
			if (emergencyVehicle != null) {
				emergencyVehicle.setReservedForMission(true);
				getRover().setTowedVehicle(emergencyVehicle);
				emergencyVehicle.setTowingVehicle(getRover());
				getStartingSettlement().getInventory().retrieveUnit(emergencyVehicle);
			}
		}
	}

	@Override
	protected void performDisembarkToSettlementPhase(Person person, Settlement disembarkSettlement) {

		// Unload towed vehicle if any.
		if (!isDone() && (getRover().getTowedVehicle() != null)) {
			emergencyVehicle.setReservedForMission(false);
			getRover().setTowedVehicle(null);
			emergencyVehicle.setTowingVehicle(null);
			disembarkSettlement.getInventory().storeUnit(emergencyVehicle);
			emergencyVehicle.determinedSettlementParkedLocationAndFacing();
		}

		super.performDisembarkToSettlementPhase(person, disembarkSettlement);
	}

	/**
	 * Perform the supply delivery disembarking phase.
	 * @param person the person performing the phase.
	 */
	private void performSupplyDeliveryDisembarkingPhase(Person person) {

		Building garageBuilding = null;

		// If rover is not parked at settlement, park it.
		if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {

			emergencySettlement.getInventory().storeUnit(getVehicle());
			getVehicle().determinedSettlementParkedLocationAndFacing();

			// Add vehicle to a garage if available.
			BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(), emergencySettlement);
			garageBuilding = BuildingManager.getBuilding(getVehicle());
		}

		// Have person exit rover if necessary.
		if (person.getLocationSituation().equals(Person.INVEHICLE)) {
			// If rover is in a garage, exit to the garage.
			if (isRoverInAGarage()) {
				getVehicle().getInventory().retrieveUnit(person);
				emergencySettlement.getInventory().storeUnit(person);
				garageBuilding = BuildingManager.getBuilding(getVehicle());
				BuildingManager.addPersonToBuildingRandomLocation(person, garageBuilding);
			}
			else {
				// Have person exit the rover via its airlock if possible.
				if (ExitAirlock.canExitAirlock(person, getRover().getAirlock())) {
					assignTask(person, new ExitAirlock(person, getRover().getAirlock()));
				}
				else {
					logger.info(person + " unable to exit " + getRover() + " through airlock to settlement " + 
							emergencySettlement + " due to health problems or being unable to obtain a functioning EVA suit.  " + 
							"Using emergency exit procedure.");
					getVehicle().getInventory().retrieveUnit(person);
					emergencySettlement.getInventory().storeUnit(person);
					BuildingManager.addToRandomBuilding(person, emergencySettlement);
				}
			}
		}
		else if (person.getLocationSituation().equals(Person.OUTSIDE)) {
			// Have person enter the settlement via an airlock.
			assignTask(person, new EnterAirlock(person, emergencySettlement.getClosestAvailableAirlock(person)));
		}

		// End the phase when everyone is out of the rover.
		if (isNoOneInRover()) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the supply delivery phase.
	 * @param person the person performing the phase.
	 */
	private void performSupplyDeliveryPhase(Person person) {

		// Unload towed vehicle (if necessary).
		if (getRover().getTowedVehicle() != null) {
			emergencyVehicle.setReservedForMission(false);
			getRover().setTowedVehicle(null);
			emergencyVehicle.setTowingVehicle(null);
			emergencySettlement.getInventory().storeUnit(emergencyVehicle);
			emergencyVehicle.determinedSettlementParkedLocationAndFacing();
		}

		// Unload rover if necessary.
		boolean roverUnloaded = getRover().getInventory().getTotalInventoryMass(false) == 0D;
		if (!roverUnloaded) {
			// Random chance of having person unload (this allows person to do other things sometimes)
			if (RandomUtil.lessThanRandPercent(50)) {
				if (isRoverInAGarage()) {
					assignTask(person, new UnloadVehicleGarage(person, getRover()));
				}
				else {
					// Check if it is day time.
					SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
					if ((surface.getSurfaceSunlight(person.getCoordinates()) > 0D) || 
							surface.inDarkPolarRegion(person.getCoordinates())) {
						assignTask(person, new UnloadVehicleEVA(person, getRover()));
					}
				}

				return;
			}
		} 
		else {
			outbound = false;
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), getStartingSettlement(),
					getStartingSettlement().getName()));
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the load return trip supplies phase.
	 * @param person the person performing the phase.
	 */
	private void performLoadReturnTripSuppliesPhase(Person person) {

		if (!isDone() && !isVehicleLoaded()) {

			// Check if vehicle can hold enough supplies for mission.
			if (isVehicleLoadable()) {
				// Random chance of having person load (this allows person to do other things sometimes)
				if (RandomUtil.lessThanRandPercent(50)) {
					if (isRoverInAGarage()) {
						assignTask(person, new LoadVehicleGarage(person, getVehicle(), getRequiredResourcesToLoad(),
								getOptionalResourcesToLoad(), getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
					}
					else {
						// Check if it is day time.
						SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
						if ((surface.getSurfaceSunlight(person.getCoordinates()) > 0D) || 
								surface.inDarkPolarRegion(person.getCoordinates())) {
							assignTask(person, new LoadVehicleEVA(person, getVehicle(), getRequiredResourcesToLoad(),
									getOptionalResourcesToLoad(), getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
						}
					}
				}
			} else {
				endMission("Vehicle is not loadable (RoverMission).");
			}
		} else {
			setPhaseEnded(true);
		}
	}


	/**
	 * Perform the return trip embarking phase.
	 * @param person the person performing the phase.
	 */
	private void performReturnTripEmbarkingPhase(Person person) {

		// If person is not aboard the rover, board rover.
		if (!person.getLocationSituation().equals(Person.INVEHICLE) && !person.getLocationSituation().equals(Person.BURIED)) {

			if (isRoverInAGarage()) {
				if (getVehicle().getInventory().canStoreUnit(person, false)) {
					if (emergencySettlement.getInventory().containsUnit(person)) {
						emergencySettlement.getInventory().retrieveUnit(person);
					}
					getVehicle().getInventory().storeUnit(person);
				}
				else {
					endMission("Crew member " + person + " cannot be loaded in rover " + getVehicle());
					return;
				}

				// Store one EVA suit for person (if possible).
				if (emergencySettlement.getInventory().findNumUnitsOfClass(EVASuit.class) > 0) {
					EVASuit suit = (EVASuit) emergencySettlement.getInventory().findUnitOfClass(EVASuit.class);
					if (getVehicle().getInventory().canStoreUnit(suit, false)) {
						emergencySettlement.getInventory().retrieveUnit(suit);
						getVehicle().getInventory().storeUnit(suit);
					}
					else {
						endMission("Equipment " + suit + " cannot be loaded in rover " + getVehicle());
						return;
					}
				}

				// Move person to random location within rover.
				Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(getVehicle());
				Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
						vehicleLoc.getY(), getVehicle());
				person.setXLocation(settlementLoc.getX());
				person.setYLocation(settlementLoc.getY());
			}
			else {
				if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {

					// Have person exit the settlement via an airlock.
					Airlock airlock = emergencySettlement.getClosestWalkableAvailableAirlock(person, 
							getVehicle().getXLocation(), getVehicle().getYLocation());
					if (airlock != null) {
						if (ExitAirlock.canExitAirlock(person, airlock)) {
							assignTask(person, new ExitAirlock(person, airlock));
						}
						else {
							logger.info(person + " unable to exit airlock at " + emergencySettlement + " to rover " + 
									getRover() + " due to health problems or being unable to obtain a functioning EVA suit.");
							endMission(person + " unable to exit airlock from " + emergencySettlement + 
									" due to health problems or being unable to obtain a functioning EVA suit.");                  
						}
					}
					else {
						logger.info(person + " unable to exit airlock at " + emergencySettlement + " to rover " + 
								getRover() + " due to no walkable airlock found.");
						endMission(person + " unable to exit airlock from " + emergencySettlement + 
								" due to no walkable airlock found.");
					}
				}
				else if (person.getLocationSituation().equals(Person.OUTSIDE)) {

					// Have person enter the rover airlock.
					assignTask(person, new EnterAirlock(person, getRover().getAirlock()));
				}
			}
		}

		// If rover is loaded and everyone is aboard, embark from settlement.
		if (isEveryoneInRover()) {

			// Remove from garage if in garage.
			Building garageBuilding = BuildingManager.getBuilding(getVehicle());
			if (garageBuilding != null) {
				VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
				garage.removeVehicle(getVehicle());
			}

			// Embark from settlement
			emergencySettlement.getInventory().retrieveUnit(getVehicle());
			setPhaseEnded(true);
		}
	}

	/**
	 * Finds a settlement within range that needs emergency supplies.
	 * @param startingSettlement the starting settlement.
	 * @param rover the rover to carry the supplies.
	 * @return settlement needing supplies or null if none found.
	 */
	private static Settlement findSettlementNeedingEmergencySupplies(Settlement startingSettlement, Rover rover) {

		Settlement result = null;

		Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
		while (i.hasNext()) {
			Settlement settlement = i.next();

			if (settlement != startingSettlement) {

				// Check if an emergency supply mission is currently ongoing to settlement.
				if (!hasCurrentEmergencySupplyMission(settlement)) {

					// Check if settlement is within rover range.
					double settlementRange = settlement.getCoordinates().getDistance(startingSettlement.getCoordinates());
					if (settlementRange <= (rover.getRange() * .8D)) {

						// Find what emergency supplies are needed at settlement.
						Map<AmountResource, Double> emergencyResourcesNeeded = getEmergencyResourcesNeeded(settlement);
						Map<Class<? extends Container>, Integer> emergencyContainersNeeded = 
								getContainersRequired(emergencyResourcesNeeded);

						if (!emergencyResourcesNeeded.isEmpty()) {

							// Check if starting settlement has enough supplies itself to send emergency supplies.
							if (hasEnoughSupplies(startingSettlement, emergencyResourcesNeeded, emergencyContainersNeeded)) {
								result = settlement;
								break;
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a settlement has sufficient supplies to send an emergency supply mission.
	 * @param startingSettlement the starting settlement.
	 * @param emergencyResourcesNeeded the emergency resources needed.
	 * @param emergencyContainersNeeded the containers needed to hold emergency resources.
	 * @return true if enough supplies at starting settlement.
	 */
	private static boolean hasEnoughSupplies(Settlement startingSettlement,
			Map<AmountResource, Double> emergencyResourcesNeeded,
			Map<Class<? extends Container>, Integer> emergencyContainersNeeded) {

		boolean result = true;

		// Check if settlement has enough extra resources to send as emergency supplies.
		Iterator<AmountResource> i = emergencyResourcesNeeded.keySet().iterator();
		while (i.hasNext() && result) {
			AmountResource resource = i.next();
			double amountRequired = emergencyResourcesNeeded.get(resource);
			double amountNeededAtStartingSettlement = getResourceAmountNeededAtStartingSettlement(
					startingSettlement, resource);
			double amountAvailable = startingSettlement.getInventory().getAmountResourceStored(
					resource, false);
			if (amountAvailable < (amountRequired + amountNeededAtStartingSettlement)) {
				result = false;
			}
		}

		// Check if settlement has enough empty containers to hold emergency resources.
		Iterator<Class<? extends Container>> j = emergencyContainersNeeded.keySet().iterator();
		while (j.hasNext() && result) {
			Class<? extends Container> container = j.next();
			int numberRequired = emergencyContainersNeeded.get(container);
			int numberAvailable = startingSettlement.getInventory().findNumEmptyUnitsOfClass(
					(Class<? extends Unit>) container, false);
			if (numberAvailable < numberRequired) {
				result = false;
			}
		}

		return result;
	}

	/**
	 * Gets the amount of a resource needed at the starting settlement.
	 * @param startingSettlement the starting settlement.
	 * @param resource the amount resource.
	 * @return amount (kg) needed.
	 */
	private static double getResourceAmountNeededAtStartingSettlement(Settlement startingSettlement, 
			AmountResource resource) {

		double result = 0D;

		if (resource.isLifeSupport()) {
			double amountNeededSol = 0D;
			PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
			AmountResource oxygen = AmountResource.findAmountResource("oxygen");
			if (resource.equals(oxygen)) amountNeededSol = config.getOxygenConsumptionRate();
			AmountResource water = AmountResource.findAmountResource("water");
			if (resource.equals(water)) amountNeededSol = config.getWaterConsumptionRate();
			AmountResource food = AmountResource.findAmountResource("food");
			if (resource.equals(food)) amountNeededSol = config.getFoodConsumptionRate();

			double amountNeededOrbit = amountNeededSol * (MarsClock.SOLS_IN_MONTH_LONG * 3D);
			int numPeople = startingSettlement.getAllAssociatedPeople().size();
			result = numPeople * amountNeededOrbit ;
		}
		else {
			AmountResource methane = AmountResource.findAmountResource("methane");
			if (resource.equals(methane)) {
				Iterator<Vehicle> i = startingSettlement.getAllAssociatedVehicles().iterator();
				while (i.hasNext()) {
					double fuelDemand = i.next().getInventory().getAmountResourceCapacity(resource, false);
					result += fuelDemand * VEHICLE_FUEL_REMAINING_MODIFIER;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a settlement has a current mission to deliver emergency supplies to it.
	 * @param settlement the settlement.
	 * @return true if current emergency supply mission.
	 */
	private static boolean hasCurrentEmergencySupplyMission(Settlement settlement) {

		boolean result = false;

		Iterator<Mission> i = Simulation.instance().getMissionManager().getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof EmergencySupplyMission) {
				EmergencySupplyMission emergencyMission = (EmergencySupplyMission) mission;
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
		emergencyResources = getEmergencyResourcesNeeded(emergencySettlement);

		// Determine containers needed to hold emergency resources.
		Map<Class<? extends Container>, Integer> containers = getContainersRequired(emergencyResources);
		emergencyEquipment = new HashMap<Class, Integer>(containers.size());
		Iterator<Class<? extends Container>> i = containers.keySet().iterator();
		while (i.hasNext()) {
			Class<? extends Container> container = i.next();
			int number = containers.get(container);
			emergencyEquipment.put(container, number);
		}

		// Determine emergency parts needed.
		emergencyParts = getEmergencyPartsNeeded(emergencySettlement);
	}

	/**
	 * Gets the emergency resource supplies needed at a settlement.
	 * @param settlement the settlement
	 * @return map of resources and amounts needed.
	 */
	private static Map<AmountResource, Double> getEmergencyResourcesNeeded(Settlement settlement) {

		Map<AmountResource, Double> result = new HashMap<AmountResource, Double>();

		double solsMonth = MarsClock.SOLS_IN_MONTH_LONG;
		int numPeople = settlement.getAllAssociatedPeople().size();
		PersonConfig config = SimulationConfig.instance().getPersonConfiguration();

		// Determine oxygen amount needed.
		AmountResource oxygen = AmountResource.findAmountResource("oxygen");
		double oxygenAmountNeeded = config.getOxygenConsumptionRate() * numPeople * solsMonth;
		double oxygenAmountAvailable = settlement.getInventory().getAmountResourceStored(oxygen, false);
		oxygenAmountAvailable += getResourcesOnMissions(settlement, oxygen);
		if (oxygenAmountAvailable < oxygenAmountNeeded) {
			double oxygenAmountEmergency = oxygenAmountNeeded - oxygenAmountAvailable;
			if (oxygenAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				oxygenAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(oxygen, oxygenAmountEmergency);
		}

		// Determine water amount needed.
		AmountResource water = AmountResource.findAmountResource("water");
		double waterAmountNeeded = config.getWaterConsumptionRate() * numPeople * solsMonth;
		double waterAmountAvailable = settlement.getInventory().getAmountResourceStored(water, false);
		waterAmountAvailable += getResourcesOnMissions(settlement, water);
		if (waterAmountAvailable < waterAmountNeeded) {
			double waterAmountEmergency = waterAmountNeeded - waterAmountAvailable;
			if (waterAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				waterAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(water, waterAmountEmergency);
		}

		// Determine food amount needed.
		AmountResource food = AmountResource.findAmountResource("food");
		double foodAmountNeeded = config.getFoodConsumptionRate() * numPeople * solsMonth;
		double foodAmountAvailable = settlement.getInventory().getAmountResourceStored(food, false);
		foodAmountAvailable += getResourcesOnMissions(settlement, food);
		if (foodAmountAvailable < foodAmountNeeded) {
			double foodAmountEmergency = foodAmountNeeded - foodAmountAvailable;
			if (foodAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				foodAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(food, foodAmountEmergency);
		}

		// Determine methane amount needed.
		AmountResource methane = AmountResource.findAmountResource("methane");
		double methaneAmountNeeded = VEHICLE_FUEL_DEMAND;
		double methaneAmountAvailable = settlement.getInventory().getAmountResourceStored(methane, false);
		methaneAmountAvailable += getResourcesOnMissions(settlement, methane);
		if (methaneAmountAvailable < methaneAmountNeeded) {
			double methaneAmountEmergency = methaneAmountNeeded - methaneAmountAvailable;
			if (methaneAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				methaneAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(methane, methaneAmountEmergency);
		}

		return result;
	}

	/**
	 * Gets the amount of a resource on associated rover missions.
	 * @param settlement the settlement.
	 * @param resource the amount resource.
	 * @return the amount of resource on missions.
	 */
	private static double getResourcesOnMissions(Settlement settlement, AmountResource resource) {
		double result = 0D;

		MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = manager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof RoverMission) {
				RoverMission roverMission = (RoverMission) mission;
				boolean isTradeMission = roverMission instanceof Trade;
				boolean isEmergencySupplyMission = roverMission instanceof EmergencySupplyMission;
				if (!isTradeMission && !isEmergencySupplyMission) {
					Rover rover = roverMission.getRover();
					if (rover != null) {
						result += rover.getInventory().getAmountResourceStored(resource, false);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the containers required to hold a collection of resources.
	 * @param resources the map of resources and their amounts.
	 * @return map of containers and the number required of each.
	 */
	private static Map<Class<? extends Container>, Integer> getContainersRequired(
			Map<AmountResource, Double> resources) {

		Map<Class<? extends Container>, Integer> result = new HashMap<Class<? extends Container>, Integer>();

		Iterator<AmountResource> i = resources.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();

			Class<? extends Container> containerClass = ContainerUtil.getContainerClassToHoldResource(resource);
			if (containerClass != null) {
				double resourceAmount = resources.get(resource);
				double containerCapacity = ContainerUtil.getContainerCapacity(containerClass);
				int numContainers = (int) Math.ceil(resourceAmount / containerCapacity);
				result.put(containerClass, numContainers);
			}
			else {
				throw new IllegalStateException("No container found to hold resource: " + resource);
			}
		}

		return result;
	}

	/**
	 * Gets the emergency part supplies needed at a settlement.
	 * @param settlement the settlement
	 * @return map of parts and numbers needed.
	 */
	private static Map<Part, Integer> getEmergencyPartsNeeded(Settlement settlement) {

		Map<Part, Integer> result = new HashMap<Part, Integer>();

		// Get all malfunctionables associated with settlement.
		Iterator<Malfunctionable> i = MalfunctionFactory.getAssociatedMalfunctionables(settlement).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();

			// Determine parts needed but not available for repairs.
			Iterator<Malfunction> j = entity.getMalfunctionManager().getMalfunctions().iterator();
			while (j.hasNext()) {
				Malfunction malfunction = j.next();
				Map<Part, Integer> repairParts = malfunction.getRepairParts();
				Iterator<Part> k = repairParts.keySet().iterator();
				while (k.hasNext()) {
					Part part = k.next();
					int number = repairParts.get(part);
					if (!settlement.getInventory().hasItemResource(part)) {
						if (result.containsKey(part)) {
							number += result.get(part).intValue();
						}
						result.put(part, number);
					}
				}
			}

			// Determine parts needed but not available for maintenance.
			Map<Part, Integer> maintParts = entity.getMalfunctionManager().getMaintenanceParts();
			Iterator<Part> l = maintParts.keySet().iterator();
			while (l.hasNext()) {
				Part part = l.next();
				int number = maintParts.get(part);
				if (!settlement.getInventory().hasItemResource(part)) {
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
	public Map<Class, Integer> getEquipmentNeededForRemainingMission(
			boolean useBuffer) {

		return new HashMap<Class, Integer>(0);
	}

	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		if ((result == 0) && isUsableVehicle(firstVehicle) && isUsableVehicle(secondVehicle)) {
			// Check if one has more general cargo capacity than the other.
			double firstCapacity = firstVehicle.getInventory().getGeneralCapacity();
			double secondCapacity = secondVehicle.getInventory().getGeneralCapacity();
			if (firstCapacity > secondCapacity) {
				result = 1;
			} else if (secondCapacity > firstCapacity) {
				result = -1;
			}

			// Vehicle with superior range should be ranked higher.
			if (result == 0) {
				if (firstVehicle.getRange() > secondVehicle.getRange()) {
					result = 1;
				} else if (firstVehicle.getRange() < secondVehicle.getRange()) {
					result = -1;
				}
			}
		}

		return result;
	}

	@Override
	protected void recruitPeopleForMission(Person startingPerson) {
		super.recruitPeopleForMission(startingPerson);

		// Make sure there is at least one person left at the starting settlement.
		if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(), startingPerson)) {
			// Remove last person added to the mission.
			Person lastPerson = null;
			int amount = getPeopleNumber() - 1;
			Object[] array = getPeople().toArray();

			if (amount >= 0 && amount < array.length) {
				lastPerson = (Person) array[amount];
			}

			if (lastPerson != null) {
				lastPerson.getMind().setMission(null);
				if (getPeopleNumber() < getMinPeople()) {
					endMission("Not enough members.");
				}
			}
		}
	}

	@Override
	public Map<Resource, Number> getRequiredResourcesToLoad() {
		Map<Resource, Number> result = super.getResourcesNeededForRemainingMission(true);

		// Add any emergency resources needed.
		if (outbound && (emergencyResources != null)) {

			Iterator<AmountResource> i = emergencyResources.keySet().iterator();
			while (i.hasNext()) {
				AmountResource resource = i.next();
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
	public Map<Resource, Number> getOptionalResourcesToLoad() {

		Map<Resource, Number> result = super.getOptionalResourcesToLoad();

		// Add any emergency parts needed.
		if (outbound && (emergencyParts != null)) {

			Iterator<Part> i = emergencyParts.keySet().iterator();
			while (i.hasNext()) {
				Part part = i.next();
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
	public Map<Class, Integer> getRequiredEquipmentToLoad() {

		Map<Class, Integer> result = getEquipmentNeededForRemainingMission(true);

		// Add any emergency equipment needed.
		if (outbound && (emergencyEquipment != null)) {

			Iterator<Class> i = emergencyEquipment.keySet().iterator();
			while (i.hasNext()) {
				Class equipment = i.next();
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
	 * @return map of goods and amounts.
	 */
	public Map<Good, Integer> getEmergencySuppliesAsGoods() {
		Map<Good, Integer> result = new HashMap<Good, Integer>();

		// Add emergency resources.
		Iterator<AmountResource> i = emergencyResources.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			double amount = emergencyResources.get(resource);
			Good resourceGood = GoodsUtil.getResourceGood(resource);
			result.put(resourceGood, (int) amount);
		}

		// Add emergency parts.
		Iterator<Part> j = emergencyParts.keySet().iterator();
		while (j.hasNext()) {
			Part part = j.next();
			int number = emergencyParts.get(part);
			Good partGood = GoodsUtil.getResourceGood(part);
			result.put(partGood, number);
		}

		// Add emergency equipment.
		Iterator<Class> k = emergencyEquipment.keySet().iterator();
		while (k.hasNext()) {
			Class equipmentClass = k.next();
			int number = emergencyEquipment.get(equipmentClass);
			Good equipmentGood = GoodsUtil.getEquipmentGood(equipmentClass);
			result.put(equipmentGood, number);
		}

		// Add emergency vehicle.
		if (emergencyVehicle != null) {
			Good vehicleGood = GoodsUtil.getVehicleGood(emergencyVehicle.getDescription());
			result.put(vehicleGood, 1);
		}

		return result;
	}

	@Override
	public void endMission(String reason) {
		super.endMission(reason);

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