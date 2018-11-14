/**
 * Mars Simulation Project
 * EmergencySupplyMission.java
 * @version 3.1.0 2017-02-20
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

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodType;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission for delivering emergency supplies from one settlement to another.
 * TODO externalize strings
 */
public class EmergencySupplyMission extends RoverMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EmergencySupplyMission.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.emergencySupplyMission"); //$NON-NLS-1$

	// Static members
	private static final int MAX_MEMBERS = 2;
	private static final int MIN_MEMBERS = 0;

	private static final double VEHICLE_FUEL_DEMAND = 1000D;
	private static final double VEHICLE_FUEL_REMAINING_MODIFIER = 2D;
	private static final double MINIMUM_EMERGENCY_SUPPLY_AMOUNT = 100D;

	public static final double BASE_STARTING_PROBABILITY = 100D;

	/** Mission phases. */
	final public static MissionPhase SUPPLY_DELIVERY_DISEMBARKING = new MissionPhase(
			Msg.getString("Mission.phase.supplyDeliveryDisembarking")); //$NON-NLS-1$
	final public static MissionPhase SUPPLY_DELIVERY = new MissionPhase(Msg.getString("Mission.phase.supplyDelivery")); //$NON-NLS-1$
	final public static MissionPhase LOAD_RETURN_TRIP_SUPPLIES = new MissionPhase(
			Msg.getString("Mission.phase.loadReturnTripSupplies")); //$NON-NLS-1$
	final public static MissionPhase RETURN_TRIP_EMBARKING = new MissionPhase(
			Msg.getString("Mission.phase.returnTripEmbarking")); //$NON-NLS-1$

	// Data members.
	private boolean outbound;

	private Settlement emergencySettlement;
	private Vehicle emergencyVehicle;

	private Map<Integer, Double> emergencyResources;
	private Map<Integer, Integer> emergencyEquipment;
	private Map<Integer, Integer> emergencyParts;

	// Static members
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;
	private static int methaneID = ResourceUtil.methaneID;

	private static PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
	private static SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	private static MissionManager missionManager;

	/**
	 * Constructor.
	 * 
	 * @param startingPerson the person starting the settlement.
	 */
	public EmergencySupplyMission(Person startingPerson) {
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson);

		missionManager = Simulation.instance().getMissionManager();

		// Set the mission capacity.
		setMissionCapacity(MAX_MEMBERS);
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
		if (availableSuitNum < getMissionCapacity()) {
			setMissionCapacity(availableSuitNum);
		}

		outbound = true;

		Settlement s = startingPerson.getSettlement();

		if (s != null & !isDone()) {

			// Initialize data members
			setStartingSettlement(s);

			// Determine emergency settlement.
			emergencySettlement = findSettlementNeedingEmergencySupplies(s, getRover());

			if (emergencySettlement != null) {

				// Update mission information for emergency settlement.
				addNavpoint(new NavPoint(emergencySettlement.getCoordinates(), emergencySettlement,
						emergencySettlement.getName()));

				// Determine emergency supplies.
				determineNeededEmergencySupplies();

				// Recruit additional members to mission.
				if (!isDone()) {
					recruitMembersForMission(startingPerson);
				}
			} else {
				endMission("No settlement could be found to deliver emergency supplies to.");
			}
		}

		if (s != null) {
			// Add emergency supply mission phases.
			addPhase(SUPPLY_DELIVERY_DISEMBARKING);
			addPhase(SUPPLY_DELIVERY);
			addPhase(LOAD_RETURN_TRIP_SUPPLIES);
			addPhase(RETURN_TRIP_EMBARKING);

			// Set initial phase
			setPhase(VehicleMission.APPROVAL);//.EMBARKING);
			setPhaseDescription(Msg.getString("Mission.phase.approval.description", s.getName())); // $NON-NLS-1$
			if (logger.isLoggable(Level.INFO)) {
				if (startingPerson != null && getRover() != null) {
					logger.info("[" + s + "] " + startingPerson.getName()
							+ " started an emergency supply mission to help out " + getEmergencySettlement() + " using "
							+ getRover().getName());
				}
			}
		}
	}

	/**
	 * Constructor with explicit parameters.
	 * 
	 * @param members             collection of mission members.
	 * @param startingSettlement  the starting settlement.
	 * @param emergencySettlement the settlement to deliver emergency supplies to.
	 * @param rover               the rover used on the mission.
	 * @param description         the mission's description.
	 */
	public EmergencySupplyMission(Collection<Person> members, Settlement startingSettlement,
			Settlement emergencySettlement, Map<Good, Integer> emergencyGoods, Rover rover, String description) {
		// Use RoverMission constructor.
		super(description, (Person) members.toArray()[0], MIN_MEMBERS, rover);

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
		addNavpoint(
				new NavPoint(emergencySettlement.getCoordinates(), emergencySettlement, emergencySettlement.getName()));

		// Determine emergency supplies.
		emergencyResources = new HashMap<>();
		emergencyParts = new HashMap<>();
		emergencyEquipment = new HashMap<>();

		Iterator<Good> j = emergencyGoods.keySet().iterator();
		while (j.hasNext()) {
			Good good = j.next();
			int amount = emergencyGoods.get(good);
			if (GoodType.AMOUNT_RESOURCE.equals(good.getCategory())) {
				AmountResource resource = (AmountResource) good.getObject();
				emergencyResources.put(ResourceUtil.findIDbyAmountResourceName(resource.getName()), (double) amount);
			} else if (GoodType.ITEM_RESOURCE.equals(good.getCategory())) {
				Part part = (Part) good.getObject();
				emergencyParts.put(ItemResourceUtil.findIDbyItemResourceName(part.getName()), amount);
			} else if (GoodType.EQUIPMENT.equals(good.getCategory())) {
				Class<?> equipmentClass = good.getClassType();
				System.out.println("EmergencySupplyMission str : " + good.getName() + " : " + equipmentClass.getName()
						+ " : " + EquipmentType.str2int(good.getName()));
				emergencyEquipment.put(EquipmentType.str2int(good.getName()), amount);
			} else if (GoodType.VEHICLE.equals(good.getCategory())) {
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
		setPhase(VehicleMission.APPROVAL);//.EMBARKING);
		setPhaseDescription(Msg.getString("Mission.phase.approval.description", getStartingSettlement().getName())); // $NON-NLS-1$
		if (logger.isLoggable(Level.INFO)) {
			Person startingPerson = (Person) members.toArray()[0];
			if (startingPerson != null && getRover() != null) {
				logger.info("[" + startingSettlement + "] " + startingPerson.getName() + startingPerson.getName()
						+ " started an emergency supply mission to help out " + getEmergencySettlement() + "on "
						+ getRover().getName());
			}
		}
	}

	@Override
	protected void determineNewPhase() {
		if (APPROVAL.equals(getPhase())) {
			setPhase(VehicleMission.EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.embarking.description", getCurrentNavpoint().getDescription()));//startingMember.getSettlement().toString())); // $NON-NLS-1$
		}
				
		else if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				if (outbound) {
					setPhase(SUPPLY_DELIVERY_DISEMBARKING);
					setPhaseDescription(Msg.getString("Mission.phase.supplyDeliveryDisembarking.description",
							emergencySettlement.getName())); // $NON-NLS-1$
				} else {
					setPhase(VehicleMission.DISEMBARKING);
					setPhaseDescription(Msg.getString("Mission.phase.disembarking.description",
							getCurrentNavpoint().getDescription())); // $NON-NLS-1$
				}
			}
		} 
		
		else if (SUPPLY_DELIVERY_DISEMBARKING.equals(getPhase())) {
			setPhase(SUPPLY_DELIVERY);
			setPhaseDescription(
					Msg.getString("Mission.phase.supplyDelivery.description", emergencySettlement.getName())); // $NON-NLS-1$
		} 
		
		else if (SUPPLY_DELIVERY.equals(getPhase())) {
			setPhase(LOAD_RETURN_TRIP_SUPPLIES);
			setPhaseDescription(
					Msg.getString("Mission.phase.loadReturnTripSupplies.description", emergencySettlement.getName())); // $NON-NLS-1$
		} 
		
		else if (LOAD_RETURN_TRIP_SUPPLIES.equals(getPhase())) {
			setPhase(RETURN_TRIP_EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.returnTripEmbarking.description", emergencySettlement.getName())); // $NON-NLS-1$
		} 
		
		else if (RETURN_TRIP_EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
		else if (DISEMBARKING.equals(getPhase())) {
			endMission(ALL_DISEMBARKED);
		}
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
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
				getStartingSettlement().getInventory().retrieveUnit(emergencyVehicle);
			}
		}
	}

	@Override
	protected void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement) {

		// Unload towed vehicle if any.
		if (!isDone() && (getRover().getTowedVehicle() != null && emergencyVehicle != null)) {
			emergencyVehicle.setReservedForMission(false);

			disembarkSettlement.getInventory().storeUnit(emergencyVehicle);

			getRover().setTowedVehicle(null);

			emergencyVehicle.setTowingVehicle(null);

			emergencyVehicle.determinedSettlementParkedLocationAndFacing();
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

			emergencySettlement.getInventory().storeUnit(getVehicle());
			// Add vehicle to a garage if available.
			if (getVehicle().getGarage() == null) {
				BuildingManager.addToGarage((GroundVehicle) getVehicle(), emergencySettlement);
			}
				
			getVehicle().determinedSettlementParkedLocationAndFacing();

		}

		// Have member exit rover if necessary.
		if (member.getLocationSituation() != LocationSituation.IN_SETTLEMENT) {

			// Get random inhabitable building at emergency settlement.
			Building destinationBuilding = emergencySettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(),
						destinationLoc.getY(), destinationBuilding);

				if (member instanceof Person) {
					Person person = (Person) member;
					if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding)) {
						assignTask(person,
								new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding));
					} else {
						logger.severe("Unable to walk to building " + destinationBuilding);
					}
				} else if (member instanceof Robot) {
					Robot robot = (Robot) member;
					if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding)) {
						assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), destinationBuilding));
					} else {
						logger.severe("Unable to walk to building " + destinationBuilding);
					}
				}
			} else {
				logger.severe("No inhabitable buildings at " + emergencySettlement);
				endMission("No inhabitable buildings at " + emergencySettlement);
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
			emergencySettlement.getInventory().storeUnit(emergencyVehicle);
			emergencyVehicle.determinedSettlementParkedLocationAndFacing();
		}

		// Unload rover if necessary.
		boolean roverUnloaded = getRover().getInventory().getTotalInventoryMass(false) == 0D;
		if (!roverUnloaded) {
			// Random chance of having person unload (this allows person to do other things
			// sometimes)
			if (RandomUtil.lessThanRandPercent(50)) {
				// TODO Refactor to allow robots.
				if (member instanceof Person) {
					Person person = (Person) member;
					if (isRoverInAGarage()) {
						assignTask(person, new UnloadVehicleGarage(person, getRover()));
					} else {
						// Check if it is day time.
						if (surface == null)
							surface = Simulation.instance().getMars().getSurfaceFeatures();
						if ((surface.getSolarIrradiance(person.getCoordinates()) > 0D)
								|| surface.inDarkPolarRegion(person.getCoordinates())) {
							assignTask(person, new UnloadVehicleEVA(person, getRover()));
						}
					}
				}

				return;
			}
		} else {
			outbound = false;
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), getStartingSettlement(),
					getStartingSettlement().getName()));
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

			// Check if vehicle can hold enough supplies for mission.
			if (isVehicleLoadable()) {
				// Random chance of having person load (this allows person to do other things
				// sometimes)
				if (RandomUtil.lessThanRandPercent(50)) {
					// TODO Refactor to allow robots.
					if (member instanceof Person) {
						Person person = (Person) member;
						if (isRoverInAGarage()) {
							assignTask(person,
									new LoadVehicleGarage(person, getVehicle(), getRequiredResourcesToLoad(),
											getOptionalResourcesToLoad(), getRequiredEquipmentToLoad(),
											getOptionalEquipmentToLoad()));
						} else {
							// Check if it is day time.
							if (surface == null)
								surface = Simulation.instance().getMars().getSurfaceFeatures();
							if ((surface.getSolarIrradiance(person.getCoordinates()) > 0D)
									|| surface.inDarkPolarRegion(person.getCoordinates())) {
								assignTask(person,
										new LoadVehicleEVA(person, getVehicle(), getRequiredResourcesToLoad(),
												getOptionalResourcesToLoad(), getRequiredEquipmentToLoad(),
												getOptionalEquipmentToLoad()));
							}
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
	 * 
	 * @param member the mission member performing the phase.
	 */
	private void performReturnTripEmbarkingPhase(MissionMember member) {

		// If person is not aboard the rover, board rover.
		if (member.getLocationSituation() != LocationSituation.IN_VEHICLE
				&& member.getLocationSituation() != LocationSituation.BURIED) {

			// Move person to random location within rover.
			Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(getVehicle());
			Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), vehicleLoc.getY(),
					getVehicle());
			// TODO Refactor
			if (member instanceof Person) {
				Person person = (Person) member;
				if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle())) {
					assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle()));
				} else {
					logger.severe(person.getName() + " unable to enter rover " + getVehicle());
					endMission(person.getName() + " unable to enter rover " + getVehicle());
				}
			} else if (member instanceof Robot) {
				Robot robot = (Robot) member;
				if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle())) {
					assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), getVehicle()));
				} else {
					logger.severe(robot.getName() + " unable to enter rover " + getVehicle());
					endMission(robot.getName() + " unable to enter rover " + getVehicle());
				}
			}

			if (isRoverInAGarage()) {

				// Store one EVA suit for person (if possible).
				if (emergencySettlement.getInventory().findNumUnitsOfClass(EVASuit.class) > 0) {
					EVASuit suit = (EVASuit) emergencySettlement.getInventory().findUnitOfClass(EVASuit.class);
					if (getVehicle().getInventory().canStoreUnit(suit, false)) {
						emergencySettlement.getInventory().retrieveUnit(suit);
						getVehicle().getInventory().storeUnit(suit);
					} else {
						endMission("Equipment " + suit + " cannot be loaded in rover " + getVehicle());
						return;
					}
				}
			}
		}

		// If rover is loaded and everyone is aboard, embark from settlement.
		if (isEveryoneInRover()) {

			// Remove from garage if in garage.
			Building garageBuilding = BuildingManager.getBuilding(getVehicle());
			if (garageBuilding != null) {
				VehicleMaintenance garage = (VehicleMaintenance) garageBuilding
						.getFunction(FunctionType.GROUND_VEHICLE_MAINTENANCE);
				garage.removeVehicle(getVehicle());
			}

			// Embark from settlement
			emergencySettlement.getInventory().retrieveUnit(getVehicle());
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

		Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
		while (i.hasNext()) {
			Settlement settlement = i.next();

			if (settlement != startingSettlement) {

				// Check if an emergency supply mission is currently ongoing to settlement.
				if (!hasCurrentEmergencySupplyMission(settlement)) {

					// Check if settlement is within rover range.
					double settlementRange = settlement.getCoordinates()
							.getDistance(startingSettlement.getCoordinates());
					if (settlementRange <= (rover.getRange() * .8D)) {

						// Find what emergency supplies are needed at settlement.
						Map<Integer, Double> emergencyResourcesNeeded = getEmergencyResourcesNeeded(settlement);
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
			double amountAvailable = startingSettlement.getInventory().getAmountResourceStored(resource, false);
			// 2015-01-09 Added addDemandTotalRequest()
			startingSettlement.getInventory().addAmountDemandTotalRequest(resource);
			if (amountAvailable < (amountRequired + amountNeededAtStartingSettlement)) {
				result = false;
			}
		}

		// Check if settlement has enough empty containers to hold emergency resources.
		Iterator<Integer> j = emergencyContainersNeeded.keySet().iterator();
		while (j.hasNext() && result) {
			Integer container = j.next();
			int numberRequired = emergencyContainersNeeded.get(container);
			int numberAvailable = startingSettlement.getInventory().findNumEmptyUnitsOfClass(container, false);
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

		if (ResourceUtil.findAmountResource(resource).isLifeSupport()) {
			double amountNeededSol = 0D;
			if (resource.equals(oxygenID))
				amountNeededSol = config.getNominalO2ConsumptionRate();
			if (resource.equals(waterID))
				amountNeededSol = config.getWaterConsumptionRate();
			if (resource.equals(foodID))
				amountNeededSol = config.getFoodConsumptionRate();

			double amountNeededOrbit = amountNeededSol * (MarsClock.SOLS_PER_MONTH_LONG * 3D);
			int numPeople = startingSettlement.getNumCitizens();
			result = numPeople * amountNeededOrbit;
		} else {
			if (resource.equals(methaneID)) {
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
	 * Checks if a settlement has a current mission to deliver emergency supplies to
	 * it.
	 * 
	 * @param settlement the settlement.
	 * @return true if current emergency supply mission.
	 */
	private static boolean hasCurrentEmergencySupplyMission(Settlement settlement) {

		boolean result = false;

		if (missionManager == null)
			missionManager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = missionManager.getMissions().iterator();
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
	 * Gets the emergency resource supplies needed at a settlement.
	 * 
	 * @param settlement the settlement
	 * @return map of resources and amounts needed.
	 */
	private static Map<Integer, Double> getEmergencyResourcesNeeded(Settlement settlement) {

		Map<Integer, Double> result = new HashMap<>();

		double solsMonth = MarsClock.SOLS_PER_MONTH_LONG;
		int numPeople = settlement.getNumCitizens();
		Inventory inv = settlement.getInventory();
		// Determine oxygen amount needed.
		double oxygenAmountNeeded = config.getNominalO2ConsumptionRate() * numPeople * solsMonth
				* Mission.OXYGEN_MARGIN;
		double oxygenAmountAvailable = settlement.getInventory().getAmountResourceStored(oxygenID, false);

		inv.addAmountDemandTotalRequest(oxygenID);

		oxygenAmountAvailable += getResourcesOnMissions(settlement, oxygenID);
		if (oxygenAmountAvailable < oxygenAmountNeeded) {
			double oxygenAmountEmergency = oxygenAmountNeeded - oxygenAmountAvailable;
			if (oxygenAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				oxygenAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(oxygenID, oxygenAmountEmergency);
		}

		// Determine water amount needed.
		double waterAmountNeeded = config.getWaterConsumptionRate() * numPeople * solsMonth * Mission.WATER_MARGIN;
		double waterAmountAvailable = settlement.getInventory().getAmountResourceStored(waterID, false);

		inv.addAmountDemandTotalRequest(waterID);

		waterAmountAvailable += getResourcesOnMissions(settlement, waterID);
		if (waterAmountAvailable < waterAmountNeeded) {
			double waterAmountEmergency = waterAmountNeeded - waterAmountAvailable;
			if (waterAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				waterAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(waterID, waterAmountEmergency);
		}

		// Determine food amount needed.
		double foodAmountNeeded = config.getFoodConsumptionRate() * numPeople * solsMonth * Mission.FOOD_MARGIN;
		double foodAmountAvailable = settlement.getInventory().getAmountResourceStored(foodID, false);

		inv.addAmountDemandTotalRequest(foodID);

		foodAmountAvailable += getResourcesOnMissions(settlement, foodID);
		if (foodAmountAvailable < foodAmountNeeded) {
			double foodAmountEmergency = foodAmountNeeded - foodAmountAvailable;
			if (foodAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				foodAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(foodID, foodAmountEmergency);
		}

		// Determine methane amount needed.
		double methaneAmountNeeded = VEHICLE_FUEL_DEMAND;
		double methaneAmountAvailable = settlement.getInventory().getAmountResourceStored(methaneID, false);

		inv.addAmountDemandTotalRequest(methaneID);

		methaneAmountAvailable += getResourcesOnMissions(settlement, methaneID);
		if (methaneAmountAvailable < methaneAmountNeeded) {
			double methaneAmountEmergency = methaneAmountNeeded - methaneAmountAvailable;
			if (methaneAmountEmergency < MINIMUM_EMERGENCY_SUPPLY_AMOUNT) {
				methaneAmountEmergency = MINIMUM_EMERGENCY_SUPPLY_AMOUNT;
			}
			result.put(methaneID, methaneAmountEmergency);
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

		if (missionManager == null)
			missionManager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
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
						// 2015-01-09 Added addDemandTotalRequest()
						rover.getInventory().addAmountDemandTotalRequest(resource);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the containers required to hold a collection of resources.
	 * 
	 * @param resources the map of resources and their amounts.
	 * @return map of containers and the number required of each.
	 */
	private static Map<Integer, Integer> getContainersRequired(Map<Integer, Double> resources) {

		Map<Integer, Integer> result = new HashMap<>();

		Iterator<Integer> i = resources.keySet().iterator();
		while (i.hasNext()) {
			Integer resource = i.next();

//            Class<? extends Container> containerClass = ContainerUtil.getContainerClassToHoldResource(resource);
//            if (containerClass != null) {
			double resourceAmount = resources.get(resource);
			double containerCapacity = ContainerUtil.getContainerCapacity(resource);
			int numContainers = (int) Math.ceil(resourceAmount / containerCapacity);
			// result.put(EquipmentType.str2int(containerClass.getName()), numContainers);
			result.put(resource, numContainers);
//            }
//            else {
//                throw new IllegalStateException("No container found to hold resource: " + resource);
//            }
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
					if (!settlement.getInventory().hasItemResource(part)) {
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
	public Map<Integer, Number> getRequiredResourcesToLoad() {
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
	public Map<Integer, Number> getOptionalResourcesToLoad() {

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
			Integer resource = i.next();
			double amount = emergencyResources.get(resource);
			Good resourceGood = GoodsUtil.getResourceGood(ResourceUtil.findAmountResource(resource));
			result.put(resourceGood, (int) amount);
		}

		// Add emergency parts.
		Iterator<Integer> j = emergencyParts.keySet().iterator();
		while (j.hasNext()) {
			Integer part = j.next();
			int number = emergencyParts.get(part);
			Good partGood = GoodsUtil.getResourceGood(ItemResourceUtil.findItemResource(part));
			result.put(partGood, number);
		}

		// Add emergency equipment.
		Iterator<Integer> k = emergencyEquipment.keySet().iterator();
		while (k.hasNext()) {
			Integer equipmentClass = k.next();
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
	protected boolean isCapableOfMission(MissionMember member) {
		boolean result = super.isCapableOfMission(member);

		if (result) {
			boolean atStartingSettlement = false;
			if (member.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
				if (member.getSettlement() == getStartingSettlement()) {
					atStartingSettlement = true;
				}
			}
			result = atStartingSettlement;
		}

		return result;
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