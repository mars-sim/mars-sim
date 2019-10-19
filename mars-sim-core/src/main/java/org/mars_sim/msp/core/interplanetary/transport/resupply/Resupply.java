/**
 * Mars Simulation Project
 * Resupply.java
 * @version 3.1.0 2017-02-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.TransportEvent;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Favorite;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.BuildingTemplate;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Resupply mission from Earth for a settlement.
 */
public class Resupply implements Serializable, Transportable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Resupply.class.getName());

	// Default separation distance between the outer wall of buildings .
	public static final int MAX_INHABITABLE_BUILDING_DISTANCE = 6;
	public static final int MIN_INHABITABLE_BUILDING_DISTANCE = 2;

	public static final int MAX_NONINHABITABLE_BUILDING_DISTANCE = 64;
	public static final int MIN_NONINHABITABLE_BUILDING_DISTANCE = 48;

	public static final int MAX_OBSERVATORY_BUILDING_DISTANCE = 48;
	public static final int MIN_OBSERVATORY_BUILDING_DISTANCE = 36;

	private static final int BUILDING_CENTER_SEPARATION = 11; // why 11?

	public static final int MAX_COUNTDOWN = 20;

	// Default width and length for variable size buildings if not otherwise
	// determined.
	private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 9D;
	private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 9D;

	/** Minimum length of a building connector (meters). */
	private static final double MINIMUM_CONNECTOR_LENGTH = 1D;// .5D;

	// Data members
	private int newImmigrantNum;
	private int settlementID;
	private int scenarioID;
	
	private String settlementName;
	private TransitState state;

	private List<BuildingTemplate> newBuildings;
	private List<String> newVehicles;
	private Map<String, Integer> newEquipment;
	private Map<AmountResource, Double> newResources;
	private Map<Part, Integer> newParts;

	private MarsClock launchDate;
	private MarsClock arrivalDate;
	
	private static BuildingConfig buildingConfig;
	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	/**
	 * Constructor.
	 * 
	 * @param arrivalDate the arrival date of the supplies.
	 * @param settlement  the settlement receiving the supplies.
	 */
	public Resupply(MarsClock arrivalDate, Settlement settlement) {
		// Initialize data members.
		this.arrivalDate = arrivalDate;
		
        // Determine launch date.
        launchDate = (MarsClock) arrivalDate.clone();
        launchDate.addTime(-1D * ResupplyUtil.getAverageTransitTime() * 1000D);
 
//		this.settlement = settlement;
		settlementID = ((Unit)settlement).getIdentifier();
		settlementName = settlement.getName();
		scenarioID = settlement.getID();
		
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}

	/**
	 * Commits a set of modifications for the resupply mission.
	 */
	public void commitModification() {
		HistoricalEvent newEvent = new TransportEvent(this, EventType.TRANSPORT_ITEM_MODIFIED,
				"Resupply mission modded", settlementName);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);
	}

	/**
	 * Generates START_BUILDING_PLACEMENT_EVENT and test if GUI is in use
	 */
	public synchronized void startDeliveryEvent() {
//		logger.config("startDeliverBuildings() is on " + Thread.currentThread().getName() + " Thread"); 
		Settlement s = unitManager.getSettlementByID(settlementID);
//		s.getBuildingManager().addResupply(this);

		// Terminate handling of delivery by
		// Resupply.java if GUI is in use
//		if (Simulation.getUseGUI()) {
//			// if GUI is in use
//			List<BuildingTemplate> orderedBuildings = orderNewBuildings();
//			if (orderedBuildings.size() > 0) {
//				Building aBuilding = buildingManager.getACopyOfBuildings().get(0);
//				// Fires the unit update below in order to use the version of deliverBuildings()
//				// in TransportWizard.java
//				settlement.fireUnitUpdate(UnitEventType.START_TRANSPORT_WIZARD_EVENT, aBuilding);
//			} else {
//				// Deliver the rest of the supplies and add people.
//				deliverOthers();
//				settlement.fireUnitUpdate(UnitEventType.END_TRANSPORT_WIZARD_EVENT);
//			}
//
//		} else {
			// if GUI is NOT in use, use the version of deliverBuildings() here in
			// Resuppply.java
		
			// Interrupts everyone's task (Walking tasks can cause issues) 
			s.endAllIndoorTasks();
			// Deliver buildings to the destination settlement.
			deliverBuildings();

			// Deliver the rest of the supplies and add people.
			deliverOthers();
//		}

	}

	/**
	 * Delivers new buildings to the settlement
	 */
	public void deliverBuildings() {
//		logger.config("deliverBuildings() is in " + Thread.currentThread().getName() + " Thread");

		List<BuildingTemplate> orderedBuildings = orderNewBuildings();

		if (orderedBuildings.size() > 0) {

			Settlement settlement = unitManager.getSettlementByID(settlementID);
			BuildingManager buildingManager = settlement.getBuildingManager();
			
			Building aBuilding = buildingManager.getACopyOfBuildings().get(0);
			settlement.fireUnitUpdate(UnitEventType.START_BUILDING_PLACEMENT_EVENT, aBuilding);

			Iterator<BuildingTemplate> buildingI = orderedBuildings.iterator();

			while (buildingI.hasNext()) {
				BuildingTemplate template = buildingI.next();

				// Correct length and width in building template.

				// Replace width and length defaults to deal with variable width and length
				// buildings.
				double width = buildingConfig.getWidth(template.getBuildingType());

				if (template.getWidth() > 0D) {
					width = template.getWidth();
				}
				if (width <= 0D) {
					width = DEFAULT_VARIABLE_BUILDING_WIDTH;
				}

				double length = buildingConfig.getLength(template.getBuildingType());
				if (template.getLength() > 0D) {
					length = template.getLength();
				}
				if (length <= 0D) {
					length = DEFAULT_VARIABLE_BUILDING_LENGTH;
				}
				int buildingID = buildingManager.getNextTemplateID();
				
				int buildingTypeID = buildingManager.getNextBuildingTypeID(template.getBuildingType());
//				int scenarioID = settlement.getID();
				String scenario = getCharForNumber(scenarioID + 1);
				// String scenario = template.getScenario(); // Note: scenario is null since
				// template does NOT have a scenario string yet
				String buildingNickName = template.getBuildingType() + " " + buildingTypeID;

				BuildingTemplate correctedTemplate = new BuildingTemplate(template.getMissionName(), buildingID,
						scenario, template.getBuildingType(), buildingNickName, width, length, template.getXLoc(),
						template.getYLoc(), template.getFacing());

				checkTemplateAddBuilding(correctedTemplate);
			}
		}
	}

	/**
	 * Checks for collision with existing buildings/vehicles/construction sites and
	 * creates the building based on the template to the settlement
	 * 
	 * @param bt a building template
	 */
	public void checkTemplateAddBuilding(BuildingTemplate bt) {
		// Check if building template position/facing collides with any existing
		// buildings/vehicles/construction sites.
		if (!isTemplatePositionClear(bt)) {
			bt = clearCollision(bt, MAX_COUNTDOWN);
		}

		if (bt != null) {
			unitManager.getSettlementByID(settlementID).getBuildingManager().addBuilding(bt, true);
		}
	}

	/**
	 * Identifies the type of collision and gets new template if the collision is
	 * immovable
	 * 
	 * @param bt    a building template
	 * @param count number of counts
	 * @return corrected building template
	 */
	public BuildingTemplate clearCollision(BuildingTemplate bt, int count) {
		count--;
		logger.config("#" + (Resupply.MAX_COUNTDOWN - count) + " : calling clearCollision() for " + bt.getNickName());
		
		BuildingManager buildingManager = unitManager.getSettlementByID(settlementID).getBuildingManager();
		
		boolean noVehicle = true;
		boolean noImmovable = true;
		boolean noConflictResupply = true;
		boolean inZone = true;
		if (count < 1) {
			logger.config("clearCollision() : count is down to 0. Quit building placement.");
			return null;
		} else {
			// check if a vehicle is the obstacle and move it
			noVehicle = isCollisionFreeVehicle(bt);
			logger.config("noVehicle is now " + noVehicle);

			if (noVehicle) {
				noImmovable = isCollisionFreeImmovable(bt);
			}
			logger.config("noImmovable is now " + noImmovable);

			if (noImmovable) {
				noConflictResupply = isCollisionFreeResupplyBuildings(bt, buildingManager);
			}
			logger.config("noConflictResupply is now " + noConflictResupply);

			if (noConflictResupply) {
				inZone = isWithinZone(bt, buildingManager);
			}
			logger.config("inZone : " + inZone);

			if (!noImmovable || !noConflictResupply || !inZone) {// if there are obstacles
				// get a new template
				BuildingTemplate repositioned = positionNewResupplyBuilding(bt.getBuildingType());

				repositioned.setMissionName(bt.getMissionName());
				// Call again recursively to check for any collision
				bt = clearCollision(repositioned, count);
			}
		}

		return bt;
	}

	/**
	 * Checks if the building template collides with any planned resupply buildings
	 * 
	 * @param bt  a building template
	 * @param mgr BuildingManager
	 * @return true if the location is clear of collision
	 */
	public static boolean isCollisionFreeResupplyBuildings(BuildingTemplate bt, BuildingManager mgr) {

		double x0 = bt.getXLoc();
		double y0 = bt.getYLoc();
		double w0 = bt.getWidth();
		double l0 = bt.getLength();
		double f0 = bt.getFacing();
		BoundedObject b0 = new BoundedObject(x0, y0, w0, l0, f0);

		List<Resupply> resupplies = ResupplyUtil.loadInitialResupplyMissions();

		Iterator<Resupply> i = resupplies.iterator();
		while (i.hasNext()) {
			Resupply r = i.next();
			if (r.getSettlement().equals(mgr.getSettlement()) || r.getSettlement() == mgr.getSettlement()) {

				List<BuildingTemplate> templates = r.getNewBuildings();
				Iterator<BuildingTemplate> j = templates.iterator();
				while (j.hasNext()) {
					BuildingTemplate t = j.next();
					// System.out.println("t : " + t.getNickName());
					double x1 = t.getXLoc();
					double y1 = t.getYLoc();
					double w1 = t.getWidth();
					double l1 = t.getLength();
					double f1 = t.getFacing();
					BoundedObject b1 = new BoundedObject(x1, y1, w1, l1, f1);

					if (LocalAreaUtil.isTwoBoundedOjectsIntersected(b0, b1))
						return false;
					// else
					// System.out.println("no intersection from " + r.hashCode());
				}
			}
		}

		return true;

	}

	/**
	 * Checks for collision and relocate any vehicles if found
	 * 
	 * @param xLoc
	 * @param yLoc
	 * @param coordinates
	 * @return true if the location is clear of collision
	 */
	public boolean isCollisionFreeVehicle(BuildingTemplate t) {

		double xLoc = t.getXLoc();
		double yLoc = t.getYLoc();
		double w = t.getWidth();
		double l = t.getLength();
		double f = t.getFacing();

		// BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

		return !LocalAreaUtil.isVehicleBoundedOjectIntersected(new BoundedObject(xLoc, yLoc, w, l, f),
				unitManager.getSettlementByID(settlementID).getCoordinates(), true);

	}

	/**
	 * Check for collision for an immovable object
	 * 
	 * @param t a building template
	 * @return true if no collision.
	 */
	public boolean isCollisionFreeImmovable(BuildingTemplate t) {

		double xLoc = t.getXLoc();
		double yLoc = t.getYLoc();
		double w = t.getWidth();
		double l = t.getLength();
		double f = t.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

		return !LocalAreaUtil.isImmovableBoundedOjectIntersected(boundedObject, 
				unitManager.getSettlementByID(settlementID).getCoordinates());
	}

	/**
	 * Check if the building template is outside min radius and within max radius
	 * 
	 * @param bt the building template
	 * @return true if it's within the prescribed zone
	 */
	public static boolean isWithinZone(BuildingTemplate bt, BuildingManager mgr) {

		boolean withinRadius = true;
		int maxDistance = 0;
		int leastDistance = 0;
		// TOD: also check if
		boolean hasLifeSupport = buildingConfig.hasLifeSupport(bt.getBuildingType());
		if (hasLifeSupport) {

			if (bt.getBuildingType().equalsIgnoreCase("Astronomy Observatory")) {
				maxDistance = MAX_OBSERVATORY_BUILDING_DISTANCE;
				leastDistance = MIN_OBSERVATORY_BUILDING_DISTANCE;
			} else {
				maxDistance = MAX_INHABITABLE_BUILDING_DISTANCE;
				leastDistance = MIN_INHABITABLE_BUILDING_DISTANCE;
			}

		}

		else {
			maxDistance = MAX_NONINHABITABLE_BUILDING_DISTANCE;
			leastDistance = MIN_NONINHABITABLE_BUILDING_DISTANCE;
		}

		List<Building> list = mgr.getBuildings(FunctionType.LIFE_SUPPORT);
		Collections.shuffle(list);

		Iterator<Building> i = list.iterator();
		while (i.hasNext()) {
			Building startingBuilding = i.next();
			double distance = Point2D.distance(startingBuilding.getXLocation(), startingBuilding.getYLocation(),
					bt.getXLoc(), bt.getYLoc());
			// logger.config("distance : " + distance);
			if (distance < leastDistance) {
				withinRadius = false;
				break;
			}
		}

		return withinRadius;
	}

	/**
	 * Delivers vehicles, resources and immigrants to a settlement on a resupply
	 * mission
	 */
	public void deliverOthers() {
//		logger.config("deliverOthers() is in " + Thread.currentThread().getName() + " Thread");
		Settlement settlement = unitManager.getSettlementByID(settlementID);
//		BuildingManager buildingManager = settlement.getBuildingManager();
		// Deliver vehicles.
//		UnitManager unitManager = Simulation.instance().getUnitManager();
		Iterator<String> vehicleI = getNewVehicles().iterator();
		while (vehicleI.hasNext()) {
			String vehicleType = vehicleI.next();
			Vehicle vehicle = null;
			if (LightUtilityVehicle.NAME.equalsIgnoreCase(vehicleType)) {
				String name = unitManager.getNewName(UnitType.VEHICLE, "LUV", null, null);
				vehicle = new LightUtilityVehicle(name, vehicleType, settlement);
			} else {
				String name = unitManager.getNewName(UnitType.VEHICLE, null, null, null);
				vehicle = new Rover(name, vehicleType, settlement);
			}
			unitManager.addUnit(vehicle);
		}

		Inventory inv = settlement.getInventory();

		// Deliver equipment.
		Iterator<String> equipmentI = getNewEquipment().keySet().iterator();
		while (equipmentI.hasNext()) {
			String equipmentType = equipmentI.next();
			int number = getNewEquipment().get(equipmentType);
			for (int x = 0; x < number; x++) {
				Equipment equipment = EquipmentFactory.createEquipment(equipmentType, settlement.getCoordinates(),
						false);
				equipment.setName(unitManager.getNewName(UnitType.EQUIPMENT, equipmentType, null, null));
				// Place this equipment within a settlement
				inv.storeUnit(equipment);
				unitManager.addUnit(equipment);
			}
		}

		// Deliver resources.
		Iterator<AmountResource> resourcesI = getNewResources().keySet().iterator();
		while (resourcesI.hasNext()) {
			AmountResource resource = resourcesI.next();
			int id = resource.getID();
			double amount = getNewResources().get(resource);
			double capacity = inv.getAmountResourceRemainingCapacity(id, true, false);
			if (amount > capacity)
				amount = capacity;
			inv.storeAmountResource(id, amount, true);
			inv.addAmountSupply(id, amount);
		}

		// Deliver parts.
		Iterator<Part> partsI = getNewParts().keySet().iterator();
		while (partsI.hasNext()) {
			Part part = partsI.next();
			int number = getNewParts().get(part);
			inv.storeItemResources(part.getID(), number);
			inv.addItemSupply(part.getID(), number);
		}

		// Deliver Robots.
		// TODO : add a combobox for selecting what bots to send

		// Deliver immigrants.
		// TODO : add a crew editor for user to define what team and who to send
		Collection<Person> immigrants = new ConcurrentLinkedQueue<Person>();
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		for (int x = 0; x < getNewImmigrantNum(); x++) {
			PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
			GenderType gender = GenderType.FEMALE;
			if (RandomUtil.getRandomDouble(1.0D) <= personConfig.getGenderRatio()) {
				gender = GenderType.MALE;
			}

			String immigrantName = unitManager.getNewName(UnitType.PERSON, null, gender, null);
			String sponsor = settlement.getSponsor();
			String country = UnitManager.getCountry(sponsor);
			// Use Builder Pattern for creating an instance of Person
			Person immigrant = Person.create(immigrantName, settlement)
					.setGender(gender)
					.setCountry(country)
					.setSponsor(sponsor)
					.setSkill(null)
					.setPersonality(null, null)
					.setAttribute(null)
					.build();
			immigrant.initialize();

			// Initialize favorites and preferences.
			Favorite favorites = immigrant.getFavorite();
			favorites.setFavoriteMainDish(favorites.getRandomMainDish());
			favorites.setFavoriteSideDish(favorites.getRandomSideDish());
			favorites.setFavoriteDessert(favorites.getRandomDessert());
			favorites.setFavoriteActivity(favorites.getAFavoriteType());
//			immigrant.getPreference().initializePreference();

			// Assign a job 
			immigrant.getMind().getInitialJob(JobUtil.MISSION_CONTROL);

			// Set up work shift 
			immigrant.getTaskSchedule().setShiftType(ShiftType.ON_CALL);

			// Add preference
			immigrant.getPreference().initializePreference();

			// Assign sponsor
//			immigrant.assignReportingAuthority();

			unitManager.addUnit(immigrant);
			relationshipManager.addNewImmigrant(immigrant, immigrants);
			immigrants.add(immigrant);

			logger.config(immigrantName + " arrived on Mars at " + settlementName);
			// Add fireUnitUpdate()
			settlement.fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, immigrant);
		}

		// Update command/governance and work shift schedules at settlement with new
		// immigrants.
		if (immigrants.size() > 0) {

			// Reset work shift schedules at settlement.
			// unitManager.setupShift(settlement, popSize);
			settlement.reassignWorkShift();
			
			// Assign a role to each person
			unitManager.assignRoles(settlement);
			
			// Reset command/government system at settlement.
			settlement.getChainOfCommand().establishSettlementGovernance(settlement);

		}

	}

	/**
	 * Orders the new buildings with non-connector buildings first and connector
	 * buildings last.
	 * 
	 * @return list of new buildings.
	 */
	public List<BuildingTemplate> orderNewBuildings() {
		List<BuildingTemplate> result = new ArrayList<>();
		
		List<BuildingTemplate> list = getNewBuildings().stream()
//				.sorted(Comparator.reverseOrder())
//				.sorted((b1, b2)-> b1.getID().compareTo(b2.getID()))
				.sorted(Comparator.comparing(bt -> bt.getID()))
				.collect(Collectors.toList());
		
		Iterator<BuildingTemplate> i = list.iterator();
		while (i.hasNext()) {
			BuildingTemplate b = i.next();
			boolean isBuildingConnector = buildingConfig.hasBuildingConnection(b.getBuildingType());
			if (isBuildingConnector) {
				// Add hallway and tunnel to end of new building list.
				result.add(b);
			} else {
				// Add non-connector to beginning of new building list.
				result.add(0, b);
			}
		}

		return result;
	}

	/**
	 * Checks if a building template's position is clear of collisions with any
	 * existing structures.
	 * 
	 * @param template the building template.
	 * @return true if building template position is clear.
	 */
	public boolean isTemplatePositionClear(BuildingTemplate template) {

		boolean result = true;

		// Replace width and length defaults to deal with variable width and length
		// buildings.
		double width = buildingConfig.getWidth(template.getBuildingType());
		if (template.getWidth() > 0D) {
			width = template.getWidth();
		}
		if (width <= 0D) {
			width = DEFAULT_VARIABLE_BUILDING_WIDTH;
		}

		double length = buildingConfig.getLength(template.getBuildingType());
		if (template.getLength() > 0D) {
			length = template.getLength();
		}
		if (length <= 0D) {
			length = DEFAULT_VARIABLE_BUILDING_LENGTH;
		}
		
		result = unitManager.getSettlementByID(settlementID).getBuildingManager()
				.isBuildingLocationOpen(template.getXLoc(), template.getYLoc(), width,
				length, template.getFacing());

		return result;
	}

	/**
	 * Determines and sets the position of a new resupply building.
	 * 
	 * @param building type the new building type.
	 * @return the repositioned building template.
	 */
	public BuildingTemplate positionNewResupplyBuilding(String buildingType) {
		// logger.config("calling positionNewResupplyBuilding()");
		BuildingTemplate newPosition = null;
		BuildingManager buildingManager = unitManager.getSettlementByID(settlementID).getBuildingManager();
		
		// Note : only hallway and tunnel has "building-connection" function
		boolean isBuildingConnector = buildingConfig.hasBuildingConnection(buildingType);
		boolean hasLifeSupport = buildingConfig.hasLifeSupport(buildingType);

		
		if (isBuildingConnector) {
			// Try to find best location to connect between the two buildings.
			newPosition = positionNewConnector(buildingType);
			// logger.config("positionNewResupplyBuilding() : just returned from
			// positionNewConnector() for " + newPosition.getNickName());
			if (newPosition != null) {
				// logger.config("it is a hallway or tunnel");
			}
		}

		else if (hasLifeSupport) {
			// logger.config("Case 2 : building has life support");
			newPosition = positionSameType(buildingType, true);

			if (newPosition != null) {
				// logger.config("has building(s) with the same building type");
				logger.config(
						"Positioning " + newPosition.getNickName() + " near the same building type with life support");
			} else {
				// logger.config("No other same building type");
				// Put this habitable building next to another inhabitable building (e.g.
				// greenhouse, lander hab, research hab...)
				List<Building> inhabitableBuildings = buildingManager
						.getBuildings(FunctionType.LIFE_SUPPORT);
				Collections.shuffle(inhabitableBuildings);
				Iterator<Building> i = inhabitableBuildings.iterator();
				while (i.hasNext()) {
					Building building = i.next();
					if (!building.getBuildingType().equalsIgnoreCase("Astronomy Observatory")) { // don't want to place
																									// any building next
																									// to the
																									// observatory
						double dist1 = 0;
						if (buildingType.equalsIgnoreCase("Astronomy Observatory")) {
							dist1 = RandomUtil.getRandomRegressionInteger(MIN_OBSERVATORY_BUILDING_DISTANCE * 2,
									MAX_OBSERVATORY_BUILDING_DISTANCE * 2) / 2D;
						} else {
							dist1 = RandomUtil.getRandomRegressionInteger(MIN_INHABITABLE_BUILDING_DISTANCE * 2,
									MAX_INHABITABLE_BUILDING_DISTANCE * 2) / 2D;
						}

						newPosition = positionNextToBuilding(buildingType, building, Math.round(dist1), false);
						if (newPosition != null) {
							logger.config("Positioning " + building.getNickName()
									+ " near a different building type with life support");
							break;
						}
					}
				}
			}
		} else {
			// logger.config("Case 3 : no life support ");
			newPosition = positionSameType(buildingType, false);
			if (newPosition != null)
				logger.config("Positioning " + newPosition.getNickName()
						+ " near the same building type with no life support");
		}

		if (newPosition == null) {
			// System.out.println("no life support, no same type of building");
			// Put this non-habitable building next to a different type building.
			// If not successful, try again 10m from each building and continue out at 10m
			// increments
			// until a location is found.
			if (buildingManager.getNumBuildings() > 0) {
				for (int x = BUILDING_CENTER_SEPARATION; newPosition == null; x = x + 2) {
					List<Building> allBuildings = buildingManager.getACopyOfBuildings();
					// System.out.println("allBuildings.size() is "+ allBuildings.size());
					// System.out.println("Building type is "+ buildingType);
					Collections.shuffle(allBuildings);
					Iterator<Building> i = allBuildings.iterator();
					while (i.hasNext()) {
						Building building = i.next();
						newPosition = positionNextToBuilding(buildingType, building, (double) x, false);
						if (newPosition != null) {
							logger.config("Positioning " + newPosition.getNickName() + " at " + x
									+ " meters away near a different building type with no life support");
							break;
						}
					}
				}
			} else {
				// Replace width and length defaults to deal with variable width and length
				// buildings.
				double width = buildingConfig.getWidth(buildingType);
				if (width <= 0D) {
					width = DEFAULT_VARIABLE_BUILDING_WIDTH;
				}
				double length = buildingConfig.getLength(buildingType);
				if (length <= 0D) {
					length = DEFAULT_VARIABLE_BUILDING_LENGTH;
				}

				// If no buildings at settlement, position new building at 0,0 with random
				// facing.
				// TODO: check to make sure it does not overlap another building.
				int buildingID = buildingManager.getNextTemplateID();
				int buildingTypeID = buildingManager.getNextBuildingTypeID(buildingType);

				String scenario = getCharForNumber(scenarioID + 1);
				// String buildingNickName = buildingType + " " + scenario + buildingID;
				String buildingNickName = buildingType + " " + buildingTypeID;
				// TODO : ask for user to define the location for the new building as well
				newPosition = new BuildingTemplate(
						"Resupply Mission launched on " + MarsClock.getDateTimeStamp(launchDate), buildingID, scenario,
						buildingType, buildingNickName, width, length, 0, 0, 0);

				logger.config("Positioning " + buildingNickName + " at (0,0)");
			}
		}

		return newPosition;
	}

	public BuildingTemplate positionSameType(String buildingType, boolean lifeSupport) {
		BuildingTemplate newPosition = null;

		// Put this non-habitable building next to the same building type.
		List<Building> sameTypeBuildings = unitManager.getSettlementByID(settlementID).getBuildingManager()
				.getBuildingsOfSameType(buildingType);

		Collections.shuffle(sameTypeBuildings);
		Iterator<Building> j = sameTypeBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();

			if (!building.getBuildingType().equalsIgnoreCase("Astronomy Observatory")) { // don't want to place any
																							// building next to the
																							// observatory
				double dist2 = 0;
				if (lifeSupport) {
					if (buildingType.equalsIgnoreCase("Astronomy Observatory")) {
						dist2 = RandomUtil.getRandomRegressionInteger(MIN_OBSERVATORY_BUILDING_DISTANCE * 2,
								MAX_OBSERVATORY_BUILDING_DISTANCE * 2) / 2D;
					} else {
						dist2 = RandomUtil.getRandomRegressionInteger(MIN_INHABITABLE_BUILDING_DISTANCE * 2,
								MAX_INHABITABLE_BUILDING_DISTANCE * 2) / 2D;
					}
				} else
					dist2 = RandomUtil.getRandomRegressionInteger(MIN_NONINHABITABLE_BUILDING_DISTANCE * 2,
							MAX_NONINHABITABLE_BUILDING_DISTANCE * 2) / 2D;
				newPosition = positionNextToBuilding(buildingType, building, Math.round(dist2), false);
				if (newPosition != null) {
					break;
				}
			}
		}
		return newPosition;
	}

	/**
	 * Determine the position and length (for variable length) of a new building
	 * connector building.
	 * 
	 * @param newBuildingType the new building type.
	 * @return new building template with position/length, or null if none found.
	 */
	private BuildingTemplate positionNewConnector(String newBuildingType) {
		Settlement settlement = unitManager.getSettlementByID(settlementID);
		BuildingManager buildingManager = settlement.getBuildingManager();
		BuildingTemplate newTemplate = null;
		int baseLevel = buildingConfig.getBaseLevel(newBuildingType);
		List<Building> inhabitableBuildings = buildingManager.getBuildings(FunctionType.LIFE_SUPPORT);
		Collections.shuffle(inhabitableBuildings);

		// Case 1
		// Try to find a connection between an inhabitable building without access to
		// airlock and
		// another inhabitable building with access to an airlock.
		if (settlement.getAirlockNum() > 0) {
			logger.config("Case 1 in positionNewConnector()");
			Building closestStartingBuilding = null;
			Building closestEndingBuilding = null;
			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> i = inhabitableBuildings.iterator();
			while (i.hasNext()) {
				Building startingBuilding = i.next();
				if (!settlement.hasWalkableAvailableAirlock(startingBuilding)) {
					// Find a different inhabitable building that has walkable access to an airlock.
					Iterator<Building> k = inhabitableBuildings.iterator();
					while (k.hasNext()) {
						Building building = k.next();
						if (!building.equals(startingBuilding)) {
							// Check if connector base level matches either building.
							boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
									|| (baseLevel == building.getBaseLevel());

							if (settlement.hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
								double distance = Point2D.distance(startingBuilding.getXLocation(),
										startingBuilding.getYLocation(), building.getXLocation(),
										building.getYLocation());
								if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {
									// Check that new building can be placed between the two buildings.
									if (positionConnectorBetweenTwoBuildings(newBuildingType, startingBuilding,
											building) != null) {
										closestStartingBuilding = startingBuilding;
										closestEndingBuilding = building;
										leastDistance = distance;
									}
								}
							}
						}
					}
				}

				if ((closestStartingBuilding != null) && (closestEndingBuilding != null)) {
					// Determine new location/length between the two buildings.
					newTemplate = positionConnectorBetweenTwoBuildings(newBuildingType, closestStartingBuilding,
							closestEndingBuilding);
//					if (newTemplate != null)
//						logger.config("Case 1 : Just created a new connector template " +
							// newTemplate.getNickName() + " in positionNewConnector()");
				}
			}
		}

		// Case 2;
		// Try to find valid connection location between two inhabitable buildings with
		// no joining walking path.
		if (newTemplate == null) {
			logger.config("Case 2 in positionNewConnector()");
			Building closestStartingBuilding = null;
			Building closestEndingBuilding = null;
			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean hasWalkingPath = settlement.getBuildingConnectorManager().hasValidPath(startingBuilding,
							building);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {
						double distance = Point2D.distance(startingBuilding.getXLocation(),
								startingBuilding.getYLocation(), building.getXLocation(), building.getYLocation());
						if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {
							// System.out.println("positionNewConnector() : Case 3Ai");
							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(newBuildingType, startingBuilding,
									building) != null) {
								closestStartingBuilding = startingBuilding;
								closestEndingBuilding = building;
								leastDistance = distance;
							}
						}
					}
				}
			}

			if ((closestStartingBuilding != null) && (closestEndingBuilding != null)) {
				// Determine new location/length between the two buildings.
				newTemplate = positionConnectorBetweenTwoBuildings(newBuildingType, closestStartingBuilding,
						closestEndingBuilding);
//				if (newTemplate != null)
//					logger.config("Case 2 : Just created a new connector template in
						// positionNewConnector()");
			}
		}

		// Case 3
		// Try to find valid connection location between two inhabitable buildings that
		// are not directly connected.
		if (newTemplate == null) {
			logger.config("Case 3 in positionNewConnector()");
			Building closestStartingBuilding = null;
			Building closestEndingBuilding = null;
			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean directlyConnected = (settlement.getBuildingConnectorManager()
							.getBuildingConnections(startingBuilding, building).size() > 0);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
						//  Case 3A
						double distance = Point2D.distance(startingBuilding.getXLocation(),
								startingBuilding.getYLocation(), building.getXLocation(), building.getYLocation());
						if ((distance < leastDistance) && (distance >= 5D)) { // MINIMUM_CONNECTOR_LENGTH)) {
							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(newBuildingType, startingBuilding,
									building) != null) {
								closestStartingBuilding = startingBuilding;
								closestEndingBuilding = building;
								leastDistance = distance;
							}
						}
					}
				}
			}

			if ((closestStartingBuilding != null) && (closestEndingBuilding != null)) {
				// Determine new location/length between the two buildings.
				newTemplate = positionConnectorBetweenTwoBuildings(newBuildingType, closestStartingBuilding,
						closestEndingBuilding);
//				if (newTemplate != null)
//					logger.config("Case 3 : just created a new connector template in
						// positionNewConnector()");
			}
		}

		// Case 4
		// Try to find connection to existing inhabitable building.
		if (newTemplate == null) {
			logger.config("Case 4 in positionNewConnector()");
			Iterator<Building> l = inhabitableBuildings.iterator();
			while (l.hasNext()) {
				Building building = l.next();
				// Make connector building face away from building.
				newTemplate = positionNextToBuilding(newBuildingType, building, 0D, true);
//				if (newTemplate != null)
//					logger.config("Case 4 :Finding a connection to existing inhabitable
						// building.");
			}
		}

		return newTemplate;
	}

	/**
	 * Positions a new building near an existing building.
	 * 
	 * @param newBuildingType    the new building type.
	 * @param building           the existing building.
	 * @param separationDistance the separation distance (meters) from the building.
	 * @param faceAway           true if new building should face away from other
	 *                           building.
	 * @return new building template with determined position, or null if none
	 *         found.
	 */
	private BuildingTemplate positionNextToBuilding(String newBuildingType, Building building,
			double separationDistance, boolean faceAway) {
		BuildingTemplate newPosition = null;

		// Replace width and length defaults to deal with variable width and length
		// buildings.
		double width = buildingConfig.getWidth(newBuildingType);
		if (width <= 0D) {
			width = DEFAULT_VARIABLE_BUILDING_WIDTH;
		}
		double length = buildingConfig.getLength(newBuildingType);
		if (length <= 0D) {
			length = DEFAULT_VARIABLE_BUILDING_LENGTH;
		}

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		List<Integer> directions = new ArrayList<Integer>(4);
		directions.add(front);
		directions.add(back);
		directions.add(right);
		directions.add(left);
		Collections.shuffle(directions);

		double direction = 0D;
		double structureDistance = 0D;
		double rectRotation = building.getFacing();

		for (int x = 0; x < directions.size(); x++) {

			switch (directions.get(x)) {
			case front:
				direction = building.getFacing();
				structureDistance = (building.getLength() / 2D) + (length / 2D);
				// logger.config("front");
				break;
			case back:
				direction = building.getFacing() + 180D;
				structureDistance = (building.getLength() / 2D) + (length / 2D);
				if (faceAway) {
					rectRotation = building.getFacing() + 180D;
				}
				// logger.config("back");
				break;
			case right:
				direction = building.getFacing() + 90D;
				structureDistance = (building.getWidth() / 2D) + (width / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (length / 2D);
					rectRotation = building.getFacing() + 90D;
				}
				// logger.config("right");
				break;
			case left:
				direction = building.getFacing() + 270D;
				structureDistance = (building.getWidth() / 2D) + (width / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (length / 2D);
					rectRotation = building.getFacing() + 270D;
				}
				// logger.config("left");
			}

			if (rectRotation > 360D) {
				rectRotation -= 360D;
			}

			double distance = structureDistance + separationDistance;
			double radianDirection = Math.toRadians(direction);
			double rectCenterX = building.getXLocation() - (distance * Math.sin(radianDirection));
			double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));

			// Check to see if proposed new building position intersects with any existing
			// buildings
			// or construction sites.
			BuildingManager buildingManager = unitManager.getSettlementByID(settlementID).getBuildingManager();
			
			if (buildingManager.isBuildingLocationOpen(rectCenterX, rectCenterY, width, length,
					rectRotation)) {
				// Set the new building here.
				int buildingID = buildingManager.getNextTemplateID();
				int buildingTypeID = buildingManager.getNextBuildingTypeID(newBuildingType);

				String scenario = getCharForNumber(scenarioID + 1);

				String buildingNickName = newBuildingType + " " + buildingTypeID;

				logger.config("Positioning at (" + Math.round(rectCenterX * 10D) / 10D + ", "
						+ Math.round(rectCenterY * 10D) / 10D + ") at " + Math.round(rectRotation) + " deg");

				newPosition = new BuildingTemplate(
						"Resupply Mission launched on " + MarsClock.getDateTimeStamp(launchDate), buildingID, scenario,
						newBuildingType, buildingNickName, width, length, rectCenterX, rectCenterY, rectRotation);
				break;
			}
		}

		return newPosition;
	}

	/**
	 * Determine the position and length (for variable length) for a connector
	 * building between two existing buildings.
	 * 
	 * @param newBuildingType the new connector building type.
	 * @param firstBuilding   the first of the two existing buildings.
	 * @param secondBuilding  the second of the two existing buildings.
	 * @return new building template with determined position, or null if none
	 *         found.
	 */
	private BuildingTemplate positionConnectorBetweenTwoBuildings(String newBuildingType, Building firstBuilding,
			Building secondBuilding) {
		// logger.config("Calling positionConnectorBetweenTwoBuildings()");
		Settlement settlement = unitManager.getSettlementByID(settlementID);
		BuildingManager buildingManager = settlement.getBuildingManager();
		
		BuildingTemplate newPosition = null;

		// Determine valid placement lines for connector building.
		List<Line2D> validLines = new ArrayList<Line2D>();

		// Check each building side for the two buildings for a valid line unblocked by
		// obstacles.
		double width = buildingConfig.getWidth(newBuildingType);
		List<Point2D> firstBuildingPositions = getFourPositionsSurroundingBuilding(firstBuilding, .1D);
		List<Point2D> secondBuildingPositions = getFourPositionsSurroundingBuilding(secondBuilding, .1D);
		for (int x = 0; x < firstBuildingPositions.size(); x++) {
			for (int y = 0; y < secondBuildingPositions.size(); y++) {

				Point2D firstBuildingPos = firstBuildingPositions.get(x);
				Point2D secondBuildingPos = secondBuildingPositions.get(y);

				double distance = Point2D.distance(firstBuildingPos.getX(), firstBuildingPos.getY(),
						secondBuildingPos.getX(), secondBuildingPos.getY());

				if (distance >= MINIMUM_CONNECTOR_LENGTH) {
					// Check line rect between positions for obstacle collision.
					Line2D line = new Line2D.Double(firstBuildingPos.getX(), firstBuildingPos.getY(),
							secondBuildingPos.getX(), secondBuildingPos.getY());
					boolean clearPath = LocalAreaUtil.checkLinePathCollision(line, settlement.getCoordinates(), false);
					if (clearPath) {
						validLines.add(new Line2D.Double(firstBuildingPos, secondBuildingPos));
					}
				}
			}
		}

		if (validLines.size() > 0) {

			// Find shortest valid line.
			double shortestLineLength = Double.MAX_VALUE;
			Line2D shortestLine = null;
			Iterator<Line2D> i = validLines.iterator();
			while (i.hasNext()) {
				Line2D line = i.next();
				double length = Point2D.distance(line.getX1(), line.getY1(), line.getX2(), line.getY2());
				if (length < shortestLineLength) {
					shortestLine = line;
					shortestLineLength = length;
				}
			}

			// Create building template with position, facing, width and length for the
			// connector building.
			double shortestLineFacingDegrees = LocalAreaUtil.getDirection(shortestLine.getP1(), shortestLine.getP2());
			Point2D p1 = adjustConnectorEndPoint(shortestLine.getP1(), shortestLineFacingDegrees, firstBuilding, width);
			Point2D p2 = adjustConnectorEndPoint(shortestLine.getP2(), shortestLineFacingDegrees, secondBuilding,
					width);
			double centerX = (p1.getX() + p2.getX()) / 2D;
			double centerY = (p1.getY() + p2.getY()) / 2D;
			double newLength = p1.distance(p2);
			double facingDegrees = LocalAreaUtil.getDirection(p1, p2);
			// Set the new building here.
			int buildingID = buildingManager.getNextTemplateID();
			int buildingTypeID = buildingManager.getNextBuildingTypeID(newBuildingType);

			String scenario = getCharForNumber(scenarioID + 1);
			String buildingNickName = newBuildingType + " " + buildingTypeID;

			newPosition = new BuildingTemplate("Resupply Mission launched on " + MarsClock.getDateTimeStamp(launchDate),
					buildingID, scenario, newBuildingType, buildingNickName, width, newLength, centerX, centerY,
					facingDegrees);
		}

		return newPosition;
	}

	/**
	 * Adjust the connector end point based on relative angle of the connection.
	 * 
	 * @param point          the initial connector location.
	 * @param lineFacing     the facing of the connector line (degrees).
	 * @param building       the existing building being connected to.
	 * @param connectorWidth the width of the new connector.
	 * @return point adjusted location for connector end point.
	 */
	private Point2D adjustConnectorEndPoint(Point2D point, double lineFacing, Building building,
			double connectorWidth) {

		double lineFacingRad = Math.toRadians(lineFacing);
		double angleFromBuildingCenterDegrees = LocalAreaUtil
				.getDirection(new Point2D.Double(building.getXLocation(), building.getYLocation()), point);
		double angleFromBuildingCenterRad = Math.toRadians(angleFromBuildingCenterDegrees);
		double offsetAngle = angleFromBuildingCenterRad - lineFacingRad;
		double offsetDistance = Math.abs(Math.sin(offsetAngle)) * (connectorWidth / 2D);

		double newXLoc = (-1D * Math.sin(angleFromBuildingCenterRad) * offsetDistance) + point.getX();
		double newYLoc = (Math.cos(angleFromBuildingCenterRad) * offsetDistance) + point.getY();

		return new Point2D.Double(newXLoc, newYLoc);
	}

	/**
	 * Gets four positions surrounding a building with a given distance from its
	 * edge.
	 * 
	 * @param building         the building.
	 * @param distanceFromSide distance (distance) for positions from the edge of
	 *                         the building.
	 * @return list of four positions.
	 */
	private List<Point2D> getFourPositionsSurroundingBuilding(Building building, double distanceFromSide) {

		List<Point2D> result = new ArrayList<Point2D>(4);

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		for (int x = 0; x < 4; x++) {
			double xPos = 0D;
			double yPos = 0D;

			switch (x) {
			case front:
				xPos = 0D;
				yPos = (building.getLength() / 2D) + distanceFromSide;
				break;
			case back:
				xPos = 0D;
				yPos = 0D - (building.getLength() / 2D) - distanceFromSide;
				break;
			case right:
				xPos = 0D - (building.getWidth() / 2D) - distanceFromSide;
				yPos = 0D;
				break;
			case left:
				xPos = (building.getWidth() / 2D) + distanceFromSide;
				yPos = 0D;
				break;
			}

			Point2D position = LocalAreaUtil.getLocalRelativeLocation(xPos, yPos, building);
			result.add(position);
		}

		return result;
	}

	/**
	 * Maps a number to an alphabet
	 * 
	 * @param a number
	 * @return a String
	 */
	private String getCharForNumber(int i) {
		// NOTE: i must be > 1, if i = 0, return null
		return i > 0 && i < 27 ? String.valueOf((char) (i + 'A' - 1)) : null;
	}

	@Override
	public String getName() {
		return getSettlement().getName();
	}

	@Override
	public TransitState getTransitState() {
		return state;
	}

	@Override
	public void setTransitState(TransitState transitState) {
		this.state = transitState;
	}

	@Override
	public synchronized void performArrival() {
		// Deliver buildings to the destination settlement.
		startDeliveryEvent();
	}

	@Override
	public MarsClock getLaunchDate() {
		return (MarsClock) launchDate.clone();
	}

	/**
	 * Sets the launch date of the resupply mission.
	 * 
	 * @param launchDate the launch date.
	 */
	public void setLaunchDate(MarsClock launchDate) {
		this.launchDate = (MarsClock) launchDate.clone();
	}

	/**
	 * Gets a list of the resupply buildings.
	 * 
	 * @return list of building types.
	 */
	public List<BuildingTemplate> getNewBuildings() {
		return newBuildings;
	}

	/**
	 * Sets the list of resupply buildings.
	 * 
	 * @param newBuildings list of building types.
	 */
	public void setNewBuildings(List<BuildingTemplate> newBuildings) {
		this.newBuildings = newBuildings;
	}

	/**
	 * Gets a list of the resupply vehicles.
	 * 
	 * @return list of vehicle types.
	 */
	public List<String> getNewVehicles() {
		return newVehicles;
	}

	/**
	 * Sets the list of resupply vehicles.
	 * 
	 * @param newVehicles list of vehicle types.
	 */
	public void setNewVehicles(List<String> newVehicles) {
		this.newVehicles = newVehicles;
	}

	/**
	 * Gets a map of the resupply equipment.
	 * 
	 * @return map of equipment type and number.
	 */
	public Map<String, Integer> getNewEquipment() {
		return newEquipment;
	}

	/**
	 * Sets the map of resupply equipment.
	 * 
	 * @param newEquipment map of equipment type and number.
	 */
	public void setNewEquipment(Map<String, Integer> newEquipment) {
		this.newEquipment = newEquipment;
	}

	/**
	 * Gets the number of immigrants in the resupply mission.
	 * 
	 * @return the number of immigrants.
	 */
	public int getNewImmigrantNum() {
		return newImmigrantNum;
	}

	/**
	 * Sets the number of immigrants in the resupply mission.
	 * 
	 * @param newImmigrantNum the number of immigrants.
	 */
	public void setNewImmigrantNum(int newImmigrantNum) {
		this.newImmigrantNum = newImmigrantNum;
	}

	/**
	 * Gets a map of the resupply resources.
	 * 
	 * @return map of resource and amount (kg).
	 */
	public Map<AmountResource, Double> getNewResources() {
		return newResources;
	}

	/**
	 * Sets the map of resupply resources.
	 * 
	 * @param newResources map of resource and amount (kg).
	 */
	public void setNewResources(Map<AmountResource, Double> newResources) {
		this.newResources = newResources;
	}

	/**
	 * Gets a map of resupply parts.
	 * 
	 * @return map of part and number.
	 */
	public Map<Part, Integer> getNewParts() {
		return newParts;
	}

	/**
	 * Sets the map of resupply parts.
	 * 
	 * @param newParts map of part and number.
	 */
	public void setNewParts(Map<Part, Integer> newParts) {
		this.newParts = newParts;
	}

	@Override
	public MarsClock getArrivalDate() {
		return (MarsClock) arrivalDate.clone();
	}

	/**
	 * Sets the arrival date of the resupply mission.
	 * 
	 * @param arrivalDate the arrival date.
	 */
	public void setArrivalDate(MarsClock arrivalDate) {
		this.arrivalDate = (MarsClock) arrivalDate.clone();
	}

	/**
	 * Gets the destination settlement.
	 * 
	 * @return destination settlement.
	 */
	public Settlement getSettlement() {
		return unitManager.getSettlementByID(settlementID);
	}

	/**
	 * Sets the destination settlement.
	 * 
	 * @param settlement the destination settlement.
	 */
	public void setSettlement(Settlement settlement) {
		settlementID = ((Unit)settlement).getIdentifier();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(getArrivalDate().getDateString());
		buff.append(" : ");
		buff.append(getSettlement().getName());
		return buff.toString();
	}

	@Override
	public int compareTo(Transportable o) {
		int result = 0;

		double arrivalTimeDiff = MarsClock.getTimeDiff(arrivalDate, o.getArrivalDate());
		if (arrivalTimeDiff < 0D) {
			result = -1;
		} else if (arrivalTimeDiff > 0D) {
			result = 1;
		} else {
			// If arrival time is the same, compare by name alphabetically.
			result = getName().compareTo(o.getName());
		}

		return result;
	}


	@Override
	public String getSettlementName() {
		// TODO Auto-generated method stub
		return settlementName;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param pc
	 */
	public static void initializeInstances(BuildingConfig bc, UnitManager u) {
		buildingConfig = bc;
		unitManager = u;
	}
			
	@Override
	public void destroy() {
//		settlement = null;
		launchDate = null;
		arrivalDate = null;
		newBuildings.clear();
		newBuildings = null;
		newVehicles.clear();
		newVehicles = null;
		newEquipment.clear();
		newEquipment = null;
		newResources.clear();
		newResources = null;
		newParts.clear();
		newParts = null;
	}


}