/*
 * Mars Simulation Project
 * BuildingManager.java
 * @date 2025-09-02
 * @author Scott Davis
 */
package com.mars_sim.core.building;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.config.BuildingConfig;
import com.mars_sim.core.building.config.BuildingSpec;
import com.mars_sim.core.building.connection.BuildingConnector;
import com.mars_sim.core.building.connection.BuildingConnectorManager;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.building.function.Computation;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.LivingAccommodation;
import com.mars_sim.core.building.function.Research;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.environment.MeteoriteImpactProperty;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.EquipmentGood;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.goods.PartGood;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionFactory;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.Converse;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.AlphanumComparator;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * The BuildingManager manages the settlement's buildings.
 */
public class BuildingManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(BuildingManager.class.getName());

	private static final int BUILDING_VALUES_UPDATE = 100;

	// Data members
	/** The population capacity (determined by the # of beds) of the settlement. */
	private int popCap = 0;

	/** The settlement's total building values. */
	private double totalBuildingValues = 0D;

	private double farmTimeCache = -5D;

	/** The id of the settlement. */
	private int settlementID;

	private Set<Building> buildings = new UnitSet<>();
	private Set<Building> garages = new UnitSet<>();
	private Set<Building> observatories = new UnitSet<>();
	private Set<Building> airlocks = new UnitSet<>();
	private Set<Building> comNodes = new UnitSet<>();

	/** A map of each building type and its value. */
	private transient Map<Building, Double> buildingValueMap = new HashMap<>();

	/** A map of each function type and its set of buildings. */
	private transient EnumMap<FunctionType, Set<Building>> functionSetOfBuildings;

	/** A map of each building with a set of function type values. */
	private transient Map<Building, EnumMap<FunctionType, Double>> buildingsOfFunctionTypeValues = new HashMap<>();

	/** The settlement's map of adjacent buildings. */
	private transient Map<Building, Set<Building>> adjacentBuildingMap = new HashMap<>();

	/** The settlement's maintenance parts map. */
	private Map<Malfunctionable, Map<MaintenanceScope, Integer>> partsMaint = new HashMap<>();

	private Set<Building> farmsNeedingWorkCache = new UnitSet<>();

	private transient MarsTime lastVPUpdateTime;

	private transient Settlement settlement;

	private MeteoriteImpactProperty meteorite;

	private static SimulationConfig simulationConfig;
	private static MasterClock masterClock;
	private static UnitManager unitManager;

	private transient List<BuildingTemplate> buildingTemplates;
	
	/**
	 * Constructor 1 : construct buildings from name list. Called by constructor 1.
	 *
	 * @param settlement        the manager's settlement
	 * @param buildingTemplates the settlement's building templates.
	 * @throws Exception if buildings cannot be constructed.
	 */
	public BuildingManager(Settlement settlement, List<BuildingTemplate> buildingTemplates) {
		this.settlement = settlement;
		this.settlementID = settlement.getIdentifier();
		this.buildings = new UnitSet<>();
		this.buildingTemplates = buildingTemplates;
	}

	/**
	 * Initializes building templates.
	 */
	public void initializeBuildingTemplates() {

		if (buildingTemplates != null && !buildingTemplates.isEmpty()) {
			for (var bt : buildingTemplates) {

				BuildingSpec spec = simulationConfig.getBuildingConfiguration().getBuildingSpec(bt.getBuildingType());

				// Check for possibility of collision
				if (!Resupply.isTemplatePositionClear(spec, bt, this)) {
					throw new IllegalArgumentException(settlement.getName() + " - Type: " + bt.getBuildingType() 
						+ ". ID: " + bt.getID() + ". Name: " + bt.getBuildingName() + ". This buildingTemplate collides with an existing BuildingTemplate.");
					// May relocate with bt = Resupply.clearCollision(spec, bt,
					// Resupply.MAX_COUNTDOWN, this);
				}

				addBuilding(Building.createBuilding(bt, settlement), bt, false);
			}
		}
	}
	/**
	 * Initializes functions map and meteorite instance.
	 */
	public void initializeFunctionsNMeteorite() {

		if (functionSetOfBuildings == null)
			setupBuildingFunctionsMap();

		meteorite = new MeteoriteImpactProperty(settlement);
	}

	/**
	 * Sets up the map for the building functions.
	 */
	public void setupBuildingFunctionsMap() {
		functionSetOfBuildings = new EnumMap<>(FunctionType.class);

		for (Building b : buildings) {
			addBuildingToMap(b);
		}

		// Get a handy shortcut to a set of garages
		garages = functionSetOfBuildings.computeIfAbsent(FunctionType.VEHICLE_MAINTENANCE, ft -> new UnitSet<>());

		// Get a handy shortcut to a set of observatories
		observatories = functionSetOfBuildings.computeIfAbsent(FunctionType.ASTRONOMICAL_OBSERVATION,
				ft -> new UnitSet<>());

		// Get a handy shortcut to a set of airlocks
		airlocks = functionSetOfBuildings.computeIfAbsent(FunctionType.EVA, ft -> new UnitSet<>());

		// Get a handy shortcut to a set of airlocks
		comNodes = functionSetOfBuildings.computeIfAbsent(FunctionType.COMPUTATION, ft -> new UnitSet<>());
	}

	/**
	 * Adds a building to the function map.
	 * 
	 * @param b
	 */
	private void addBuildingToMap(Building b) {
		for (Function f : b.getFunctions()) {
			functionSetOfBuildings.computeIfAbsent(f.getFunctionType(), ft -> new UnitSet<>()).add(b);
		}
	}

	/**
	 * Removes a building from the settlement.
	 *
	 * @param oldBuilding the building to remove.
	 */
	public void removeBuilding(Building oldBuilding) {

		if (buildings.contains(oldBuilding)) {
			// Remove building connections (hatches) to old building.
			getBuildingConnectorManager().removeAllConnectionsToBuilding(oldBuilding);

			buildings.remove(oldBuilding);

			// use this only after buildingFunctionsMap has been created
			for (var f : oldBuilding.getFunctions()) {
				removeOneFunctionfromBFMap(oldBuilding, f);
			}

			// Remove the building's functions from the settlement.
			oldBuilding.removeFunctionsFromSettlement();

			settlement.fireUnitUpdate(EntityEventType.REMOVE_BUILDING_EVENT, oldBuilding);
		}
	}

	/**
	 * Removes the reference of this building for a functions in
	 * buildingFunctionsMap.
	 *
	 * @param a building
	 * @param a function
	 */
	public void removeOneFunctionfromBFMap(Building b, Function f) {
		if (functionSetOfBuildings != null) {
			FunctionType ft = f.getFunctionType();
			Set<Building> list = functionSetOfBuildings.get(ft);
			if (list != null) {
				list.remove(b);
			}
		}

		// Computes the population capacity based on the # of beds available
		computePopulationCapacity();
	}

	/**
	 * Computes the population capacity of the settlement.
	 *
	 * @return the population capacity
	 */
	private void computePopulationCapacity() {
		int result = 0;
		Set<Building> bs = getBuildingSet(FunctionType.LIVING_ACCOMMODATION);
		for (Building building : bs) {
			result += building.getLivingAccommodation().getBedCap();
		}
		popCap = result;
	}

	/**
	 * Gets the population capacity of the settlement.
	 *
	 * @return the population capacity
	 */
	public int getPopulationCapacity() {
		return popCap;
	}

	/**
	 * Adds references of this building in all functions in buildingFunctionsMap.
	 *
	 * @param oldBuilding
	 */
	public void refreshFunctionMapForBuilding(Building newBuilding) {
		if (functionSetOfBuildings == null)
			setupBuildingFunctionsMap();
		addBuildingToMap(newBuilding);

		// Computes the population capacity based on the # of beds available
		computePopulationCapacity();
	}

	/**
	 * Adds a new building to the settlement.
	 *
	 * @param newBuilding               the building to add.
	 * @param buildingTemplate          the building template to add.
	 * @param createBuildingConnections true if automatically create building
	 *                                  connections.
	 */
	public void addBuilding(Building newBuilding, BuildingTemplate buildingTemplate,
			boolean createBuildingConnections) {
		addBuilding(newBuilding, createBuildingConnections);

		if (!buildings.contains(newBuilding) && createBuildingConnections) {
			// Process the building template and make connections with adjacent building
			getBuildingConnectorManager().processBuildingTemplate(settlement, buildingTemplate);
		}
	}

	/**
	 * Adds a new building to the settlement.
	 *
	 * @param newBuilding               the building to add.
	 * @param createBuildingConnections true if automatically create building
	 *                                  connections.
	 */
	public void addBuilding(Building newBuilding, boolean createBuildingConnections) {
		if (!buildings.contains(newBuilding)) {
			unitManager.addUnit(newBuilding);

			buildings.add(newBuilding);

			// Insert this new building into buildingFunctionsMap
			refreshFunctionMapForBuilding(newBuilding);

			settlement.fireUnitUpdate(EntityEventType.ADD_BUILDING_EVENT, newBuilding);

			if (createBuildingConnections) {
				// Note: at the star of the sim, BuildingConnectorManager is still null
				getBuildingConnectorManager().createBuildingConnections(newBuilding);
				// Create an adjacent building map
				createAdjacentBuildingMap();
			}
		}
	}

	/**
	 * Adds a new mock building to the settlement.
	 *
	 * @param newBuilding the building to add.
	 */
	public void addMockBuilding(Building newBuilding) {
		if (!buildings.contains(newBuilding)) {
			buildings.add(newBuilding);
		}
	}

	/**
	 * Gets a building.
	 *
	 * @return collection of buildings
	 */
	public Building getABuilding() {
		return buildings.stream().findAny().orElse(null); // or use .findFirst()
	}

	/**
	 * Gets a set of settlement's buildings.
	 *
	 * @return a set of buildings
	 */
	public Set<Building> getBuildingSet() {
		return buildings;
	}

	/**
	 * Gets a collection of alphanumerically sorted buildings.
	 *
	 * @return collection of alphanumerically sorted buildings
	 */
	public List<Building> getSortedBuildings() {
		return buildings.stream().sorted(new AlphanumComparator()).toList();
	}

	/**
	 * Gets a list of settlement's buildings with Life Support function.
	 *
	 * @return list of buildings
	 */
	public List<Building> getBuildingsWithLifeSupport() {
		return getBuildings(FunctionType.LIFE_SUPPORT);
	}

	/**
	 * Gets a list of settlement's buildings (not including hallway, tunnel or
	 * observatory) having a particular function type.
	 *
	 * @param functionType
	 * @return list of buildings
	 */
	public List<Building> getBuildingsNoHallwayTunnelObservatory(FunctionType functionType) {
		// Filter off hallways and tunnels
		return getBuildings(functionType).stream().filter(b -> b.getCategory() != BuildingCategory.CONNECTION
				&& !b.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)).toList();
	}

	/**
	 * Checks if the settlement contains a given building.
	 *
	 * @param building the building.
	 * @return true if settlement contains building.
	 */
	public boolean containsBuilding(Building building) {
		return buildings.contains(building);
	}

	/**
	 * Gets the building with the given template ID.
	 *
	 * @param id the template ID .
	 * @return building or null if none found.
	 */
	public Building getBuildingByTemplateID(String id) {
		Building result = null;

		for (Building b : buildings) {
			if (b.getTemplateID().equalsIgnoreCase(id)) {
				result = b;
			}
		}
		return result;
	}

	/**
	 * Gets the buildings in a settlement that has a given function.
	 *
	 * @param building function {@link FunctionType} the function of the building.
	 * @return list of buildings.
	 */
	public List<Building> getBuildings(FunctionType bf) {
		return new ArrayList<>(getBuildingSet(bf));
	}

	/**
	 * Gets the buildings in a settlement that has a given function.
	 *
	 * @param building function {@link FunctionType} the function of the building.
	 * @return list of buildings.
	 */
	public Set<Building> getBuildingSet(FunctionType bf) {
		if (functionSetOfBuildings == null) {
			setupBuildingFunctionsMap();
		}

		if (functionSetOfBuildings.containsKey(bf)) {
			return functionSetOfBuildings.get(bf);
		}

		else {
			Set<Building> set = buildings.stream().filter(b -> b.hasFunction(bf)).collect(Collectors.toSet());
			functionSetOfBuildings.put(bf, set);
			return set;
		}
	}

	/**
	 * Gets a list of buildings in a settlement that does not have a given function.
	 *
	 * @param building function {@link FunctionType} the function of the building.
	 * @return list of buildings
	 */
	public Set<Building> getBuildingsWithoutFctNotAstro(FunctionType bf) {
		return buildings.stream()
				.filter(b -> !b.hasFunction(bf) && !b.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION))
				.collect(Collectors.toSet());
	}

	/**
	 * Gets a list of buildings in a settlement that has a given science type.
	 * 
	 * @param type ScienceType
	 * @return list of buildings
	 */
	public Set<Building> getBuildingsWithScienceType(Person person, ScienceType type) {
		Building origin = person.getBuildingLocation();
		if (origin != null) {
			return buildings.stream().filter(
					b -> b.hasSpecialty(type) && isGoodZone(origin, b) && !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}

		return buildings.stream().filter(b -> b.hasSpecialty(type)
				// This avoid a person to go to astronomy observatory (in zone 1)
				// needlessly
				&& b.getZone() == 0 && !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());

	}

	/**
	 * Checks if a building is in 'good' zone.
	 * 
	 * @param origin
	 * @param destination
	 * @return
	 */
	private boolean isGoodZone(Building origin, Building destination) {
		// Assuming zone 0 is the main zone, where most service are available,
		// this will allow someone in astronomy observatory (in zone 1) to come back
		// home
		return (destination.getZone() == 0 || (destination.getZone() == origin.getZone()));
	}

	/**
	 * Gets an available building that the person can use.
	 *
	 * @param person the person
	 * @return available building or null if none.
	 */
	public static Building getAvailableBuilding(ScienceType sType, Person person) {
		Building b = person.getBuildingLocation();

		// If this person is located in the settlement
		if (person.isInSettlement()) {
			Set<Building> buildings = null;

			if (sType != null) {
				if (sType == ScienceType.ASTRONOMY) {

					buildings = person.getSettlement().getBuildingManager()
							.getBuildingsOfSameCategoryZone0(BuildingCategory.ASTRONOMY);

					if (buildings.isEmpty()) {
						buildings = person.getSettlement().getBuildingManager()
								.getBuildingsOfSameCategory(BuildingCategory.ASTRONOMY);
					}
				} else
					buildings = person.getSettlement().getBuildingManager().getBuildingsWithScienceType(person, sType);
			}

			if (buildings != null && !buildings.isEmpty()) {
				Map<Building, Double> possibleBuildings = BuildingManager.getBestRelationshipBuildings(person,
						buildings);
				b = RandomUtil.getWeightedRandomObject(possibleBuildings);
			}

			if (b == null && buildings != null && !buildings.isEmpty()) {
				List<Building> bldg = new ArrayList<>(buildings);
				b = bldg.get(0);
			}
			if (b == null) {
				b = getBuildingWithSpot(person, FunctionType.RESEARCH, FunctionType.ADMINISTRATION, FunctionType.DINING,
						FunctionType.LIVING_ACCOMMODATION);
			}
		}

		return b;
	}

	/**
	 * Gets a building with one of the spot.
	 * 
	 * @param person
	 * @return
	 */
	private static Building getBuildingWithSpot(Person person, FunctionType type1, FunctionType type2,
			FunctionType type3, FunctionType type4) {
		Set<Building> buildings = null;

		if (buildings == null || buildings.isEmpty()) {
			buildings = getBuildingsinSameZone(person, type1);
		}
		if (buildings == null || buildings.isEmpty()) {
			buildings = getBuildingsinSameZone(person, type2);
		}
		if (buildings == null || buildings.isEmpty()) {
			buildings = getBuildingsinSameZone(person, type3);
		}
		if (buildings == null || buildings.isEmpty()) {
			buildings = getBuildingsinSameZone(person, type4);
		}

		if (buildings != null && !buildings.isEmpty()) {
			Map<Building, Double> possibleBuildings = BuildingManager.getBestRelationshipBuildings(person, buildings);
			return RandomUtil.getWeightedRandomObject(possibleBuildings);
		}

		List<Building> bldg = new ArrayList<>(buildings);
		if (bldg.size() > 0)
			return bldg.get(0);

		return null;
	}

	/**
	 * Gets a list of non-malfunctioned diners in the same zone.
	 * 
	 * @param person
	 * @return
	 */
	public Set<Building> getDiningBuildings(Person person) {
		Building origin = person.getBuildingLocation();

		if (origin != null) {
			return getBuildingSet(FunctionType.DINING).stream()
					.filter(b -> isGoodZone(origin, b) && !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}

		return getBuildingSet(FunctionType.DINING).stream()
				.filter(b -> b.getZone() == 0 && !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());
	}

	/**
	 * Gets an available dining building.
	 *
	 * @param settlement
	 * @param zone
	 * @return available dining building
	 * @throws BuildingException if error finding dining building.
	 */
	public static Building getAvailableDiningBuilding(Settlement settlement, int zone) {
		Building bldg = null;

		if (settlement != null) {

			List<Building> list = settlement.getBuildingManager().getBuildingSet(FunctionType.DINING).stream()
					.filter(b -> b.getZone() == zone && !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toList());

			if (!list.isEmpty()) {
				bldg = list.get(0);
			}
		}

		return bldg;
	}

	/**
	 * Gets an available kitchen for a worker. DO NOT DELETE. LEAVE HERE AS A
	 * REFERENCE
	 * 
	 * @param worker
	 * @param functionType
	 * @return
	 */
	public static Building getAvailableKitchen(Worker worker) {
		Building result = null;

		if (worker.isInSettlement()) {
			BuildingManager manager = worker.getSettlement().getBuildingManager();

			Set<Building> kitchenBuildings = null;

			if (worker.getBuildingLocation() != null) {
				kitchenBuildings = manager.getBuildings(FunctionType.COOKING).stream()
						.filter(b -> b.getZone() == worker.getBuildingLocation().getZone()
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			} else {
				kitchenBuildings = manager.getBuildings(FunctionType.COOKING).stream()
						.filter(b -> b.getZone() == 0 && !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}

			if (UnitType.PERSON == worker.getUnitType()) {
				kitchenBuildings = getLeastCrowdedBuildings(kitchenBuildings);

				if (!kitchenBuildings.isEmpty()) {
					Map<Building, Double> selectedBldgs = getBestRelationshipBuildings((Person) worker,
							kitchenBuildings);
					result = RandomUtil.getWeightedRandomObject(selectedBldgs);
				}
			}

			else {
				if (RandomUtil.getRandomInt(2) == 0) // robot is not as inclined to move around
					kitchenBuildings = getLeastCrowded4BotBuildings(kitchenBuildings);

				if (!kitchenBuildings.isEmpty()) {
					result = RandomUtil.getARandSet(kitchenBuildings);
				}
			}
		}

		return result;
	}

	/**
	 * Gets an available dining building that the person can use. Returns null if no
	 * dining building is currently available.
	 *
	 * @param person  the person
	 * @param canChat
	 * @return available dining building
	 * @throws BuildingException if error finding dining building.
	 */
	public static Building getAvailableDiningBuilding(Person person, boolean canChat) {
		Building b = null;

		// If this person is located in the settlement
		Settlement settlement = person.getSettlement();
		if (settlement != null) {

			Set<Building> list0 = settlement.getBuildingManager().getDiningBuildings(person);
			if (list0.isEmpty())
				return null;

			if (canChat) {
				// Choose between the most crowded or the least crowded dining hall
				BuildingManager.getChattyBuildings(list0);
			} else {
				BuildingManager.getLeastCrowdedBuildings(list0);
			}

			if (!list0.isEmpty()) {
				Map<Building, Double> probs = BuildingManager.getBestRelationshipBuildings(person, list0);
				b = RandomUtil.getWeightedRandomObject(probs);
			}
		}

		return b;
	}

	/**
	 * Gets a list of non-malfunctioned buildings with a particular function type.
	 *
	 * @param worker
	 * @param functionType
	 * @return
	 */
	public static Set<Building> getBuildingsinSameZone(Worker worker, FunctionType functionType) {
		if (worker.getBuildingLocation() != null) {
			return worker.getSettlement().getBuildingManager().getBuildingSet(functionType).stream()
					.filter(b -> b.getZone() == worker.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}

		return worker.getSettlement().getBuildingManager().getBuildingSet(functionType).stream()
				// Not possible to nail down the same zone
				.filter(b -> b.getZone() == 0 && !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the buildings in the same zone as the person in a settlement that has
	 * function f1 and have no f2.
	 *
	 * @param p
	 * @param f1 the required function
	 * @param f2 the excluded function
	 * @return list of buildings
	 */
	public Set<Building> getSameZoneBuildingsF1NoF2(Person p, FunctionType f1, FunctionType f2) {
		return buildings.stream().filter(
				b -> b.hasFunction(f1) && !b.hasFunction(f2) && p.getBuildingLocation().getZone() == b.getZone())
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the buildings in a settlement have no functions f1 and f2.
	 *
	 * @param functions the array of required functions {@link BuildingFunctions}.
	 * @return list of buildings.
	 */
	public List<Building> getBuildingsNoF1F2(FunctionType f1, FunctionType f2) {
		return buildings.stream().filter(b -> !b.hasFunction(f1) && !b.hasFunction(f2)).toList();
	}

	/**
	 * Gets the buildings in a settlement have function f1 but with no functions f2
	 * and f3.
	 *
	 * @param functions the array of required functions {@link BuildingFunctions}.
	 * @return list of buildings.
	 */
	public List<Building> getBuildingsF1NoF2F3(FunctionType f1, FunctionType f2, FunctionType f3) {
		return buildings.stream().filter(b -> b.hasFunction(f1) && !b.hasFunction(f2) && !b.hasFunction(f3)).toList();
	}

	/**
	 * Gets the buildings in the settlement with a given building category.
	 *
	 * @param category the building type.
	 * @return list of buildings.
	 */
	public Set<Building> getBuildingsOfSameCategory(BuildingCategory category) {
		// Called by Resupply.java and BuildingConstructionMission.java
		// for putting new building next to the same building "type".
		return buildings.stream().filter(b -> b.getCategory() == category).collect(Collectors.toSet());
	}

	/**
	 * Gets the buildings in the settlement with a given building category and in
	 * zone 0.
	 *
	 * @param category the building type.
	 * @return list of buildings.
	 */
	public Set<Building> getBuildingsOfSameCategoryZone0(BuildingCategory category) {
		// Called by Resupply.java and BuildingConstructionMission.java
		// for putting new building next to the same building "type".
		return buildings.stream().filter(b -> b.getCategory() == category && b.getZone() == 0)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the buildings in the settlement with a given building type.
	 *
	 * @param buildingType the building type.
	 * @return list of buildings.
	 */
	public List<Building> getBuildingsOfSameType(String buildingType) {
		// Called by Resupply.java and BuildingConstructionMission.java
		// for putting new building next to the same building "type".
		return buildings.stream().filter(b -> b.getBuildingType().equalsIgnoreCase(buildingType)).toList();
	}

	/**
	 * Gets a random building in a settlement that has a given function.
	 *
	 * @param bf {@link FunctionType} the function of the building.
	 * @return a building.
	 */
	public Building getABuilding(FunctionType bf) {
		if (functionSetOfBuildings == null) {
			functionSetOfBuildings = new EnumMap<>(FunctionType.class);
			setupBuildingFunctionsMap();
		}

		if (functionSetOfBuildings.containsKey(bf)) {
			return RandomUtil.getARandSet(functionSetOfBuildings.get(bf));
		}

		return null;
	}

	/**
	 * Gets a random building having these two functions.
	 *
	 * @param f1
	 * @param f2
	 * @return a building.
	 */
	public Building getABuilding(FunctionType f1, FunctionType f2) {
		Optional<Building> value = buildings.stream().filter(b -> b.hasFunction(f1) && b.hasFunction(f2)).findAny(); // .findFirst();

		return value.orElse(null);

	}

	/**
	 * Gets the number of buildings at the settlement.
	 *
	 * @return number of buildings
	 */
	public int getNumBuildings() {
		return buildings.size();
	}

	/**
	 * Time passing for all buildings.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws Exception if error.
	 */
	public boolean timePassing(ClockPulse pulse) {

		if (functionSetOfBuildings == null) {
			functionSetOfBuildings = new EnumMap<>(FunctionType.class);
			setupBuildingFunctionsMap();
		}

		if (pulse.isNewSol()) {
			// Update the impact probability for each settlement based on the size and speed
			// of the new meteorite
			meteorite.calculateMeteoriteProbability();
		}

		if (pulse.getMarsTime().getMissionSol() != 1 && pulse.isNewHalfSol()) {
			// Check if there are any maintenance parts to be submitted
			retrieveAllEntitiesMaintParts();
		}

		for (Building b : buildings) {
			try {
				b.timePassing(pulse);
			} catch (RuntimeException rte) {
				logger.severe(b, "Problem applying pulse to Building", rte);
			}
		}
		return true;
	}

	/**
	 * Gets a random building with an airlock.
	 *
	 * @return random building.
	 */
	public Building getRandomAirlockBuilding() {
		return getABuilding(FunctionType.EVA);
	}

	/**
	 * Adds a patient to a medical bed within a settlement.
	 *
	 * @param p the patient
	 * @param s the settlement with medical beds
	 * @return
	 */
	public static boolean addPatientToMedicalBed(Person p, Settlement s) {
		boolean success = false;

		Building building = s.getBuildingManager().getABuilding(FunctionType.MEDICAL_CARE, FunctionType.LIFE_SUPPORT);

		if (building != null) {

			success = addToActivitySpot(p, building, FunctionType.MEDICAL_CARE);
			
			if (success) {
				
//				success = building.getMedical().addToBed(); 
	
				if (success) {
					logger.info(p, 10_000L, "Sent to a medical bed in " + building.getName() + ".");
				}
				else {	
//					building.getMedical().removeFromBed();				
					logger.info(p, 10_000L, "Unable to find a bed or an activity spot in " + building.getName() + ".");
				}
			}
			else {
				logger.info(p, 10_000L, "No spare medical bed available in " + building.getName() + ".");
			}
		}

		else {
			// Send to his/her registered bed
			logger.log(p, Level.WARNING, 10_000L,
					"No medical facility available for " + p.getName() + ". Go to his/her bed.");

			success = walkToBed(p, s);
		}

		return success;
	}

	/**
	 * Walks to a bed.
	 * 
	 * @param p
	 * @param s
	 * @return
	 */
	public static boolean walkToBed(Person p, Settlement s) {
		boolean success = false;

		AllocatedSpot bed = p.getBed();

		if (bed == null) {
			// It will look for a permanent bed if possible
			AllocatedSpot tempBed = Sleep.findABed(s, p);

			if (tempBed == null) {
				// Assign a temporary bed to this person
				bed = LivingAccommodation.allocateBed(p.getSettlement(), p, false);
			}
		}

		if (bed != null) {

			Building b = bed.getOwner();
			// Question: does it still need to claim since this is already his own bed ?
			success = b.getLivingAccommodation().claimActivitySpot(bed.getAllocated().getPos(), p);

			if (success) {
				logger.log(p, Level.INFO, 10_000L, "Able to claim the activity spot of a bed.");
			}
		}

		if (bed == null) {
			logger.log(p, Level.INFO, 10_000L, "Unsuccessful in finding a bed.");

			return success;
		}

		if (success) {
			// Check my own position
			LocalPosition myLoc = p.getPosition();
			// Allocate it
			p.setActivitySpot(bed);

			LocalPosition bedLoc = bed.getAllocated().getPos();

			if (myLoc.equals(bedLoc)) {
				// Already at that location and no need to walk further
				return success;
			} else {
				// Create subtask for walking to destination.
				return createWalkingSubtask(p, bed.getOwner(), bedLoc, false, true);
			}
		}

		else {
			logger.log(p, Level.INFO, 10_000L, "Unsuccessful claiming the activity spot of a bed.");
		}

		return success;
	}

	/**
	 * Creates a walk to an interior position in a building or vehicle.
	 * 
	 * @Note: need to ensure releasing the old activity spot prior to calling this
	 *        method and take in the new activity spot after this method.
	 * @param interiorObject the destination interior object.
	 * @param sLoc           the settlement local position destination.
	 * @param allowFail      true if walking is allowed to fail.
	 * @param needEVA
	 */
	public static boolean createWalkingSubtask(Worker worker, LocalBoundedObject interiorObject, LocalPosition sLoc,
			boolean allowFail, boolean needEVA) {
		// Check my own position
		LocalPosition myLoc = worker.getPosition();

		if (myLoc.equals(sLoc)) {
			// May add back checking: logger.info(worker, 4_000, "Already at the spot and no
			// need to walk further.")
			return true;
		}

		Walk walkingTask = Walk.createWalkingTask(worker, sLoc, interiorObject, needEVA);

		if (walkingTask != null) {

			// Walk back home
			if (worker.getTaskManager().directlyAssignTask(walkingTask, false)) {
				return true;
			}
			else {
				logger.log(worker, Level.INFO, 4_000, "Failed to be assigned to walk to " + interiorObject + ".");
				return false;
			}
		} else {
			if (!allowFail) {
				logger.log(worker, Level.INFO, 4_000, "Failed to walk to " + interiorObject + ".");
			} else {
				logger.log(worker, Level.INFO, 4_000, "Unable to walk to " + interiorObject + ".");
			}
		}

		return false;
	}

	/**
	 * Adds a person to a habitable building activity spot within a
	 * settlement based on his job type.
	 * Note: excluding the EVA building
	 *
	 * @param person     the person to add.
	 * @param settlement the settlement to find a building.
	 * @return
	 * @throws BuildingException if person cannot be added to any building.
	 */
	public static boolean addPersonToBuildingSpotByJobType(Person person, Settlement settlement) {

		boolean found = false;

		FunctionType functionType = FunctionType.getDefaultFunction(person.getMind().getJobType());
		
		// Go to the default zone 0 only
		Set<Building> bldgSet = person.getAssociatedSettlement().getBuildingManager()
				.getBuildingSet(functionType).stream().filter(b -> b.getZone() == 0
						&& b.getCategory() != BuildingCategory.CONNECTION
						&& b.getCategory() != BuildingCategory.EVA && !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());

		if (!bldgSet.isEmpty()) {
			for (Building building : bldgSet) {
				// Add the person to a building activity spot
				found = addToActivitySpot(person, building, functionType);
				if (found)
					return true;
			}
		}

		List<Building > bldglist = person.getAssociatedSettlement().getBuildingManager()
				.getBuildings(FunctionType.LIFE_SUPPORT).stream().filter(b -> b.getZone() == 0
						&& b.getCategory() != BuildingCategory.EVA && !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toList());
		
		if (!bldglist.isEmpty()) {
			for (Building building : bldglist) {
				// Add the person to an empty building activity spot
				// Therefore, set FuntionType to null
				found = addToActivitySpot(person, building, null);
				if (found)
					return true;
			}
		}

		if (!found) {
			logger.warning(person, "No habitable buildings with empty activity spot available in zone 0.");
		}
		
		return found;
	}

	/**
	 * Adds a person to a random habitable building within a settlement. Note:
	 * excluding the EVA building (and astronomical observation) building
	 *
	 * @param person     the person to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if person cannot be added to any building.
	 */
	public static void addPersonToRandomBuilding(Person person, Settlement settlement) {

		// Go to the default zone 0 only
		Set<Building> bldgSet = settlement
				.getBuildingManager().getBuildingSet(FunctionType.LIFE_SUPPORT).stream().filter(b -> b.getZone() == 0
						&& b.getCategory() != BuildingCategory.EVA && !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());

		if (bldgSet.isEmpty()) {
			return;
		}

		for (Building building : bldgSet) {
			if (building.getCategory() != BuildingCategory.CONNECTION
					&& building.getCategory() != BuildingCategory.EVA) {

				// Add the person to the life support
				if (building.getLifeSupport() != null) {
					building.getLifeSupport().addPerson(person);

					person.setCurrentBuilding(building);

					return;
				}
			}
		}

		logger.warning(person, "No habitable buildings with life support available in zone 0.");
	}

	/**
	 * Adds a robot to a random habitable building within a settlement.
	 *
	 * @param unit the robot to add.
	 * @param s    the settlement to find a building.
	 * @throws BuildingException if robot cannot be added to any building.
	 */
	public static void addRobotToRandomBuilding(Robot robot, Settlement s) {
		BuildingManager manager = s.getBuildingManager();

		final FunctionType functionType = FunctionType.getDefaultFunction(robot.getRobotType());

		Set<Building> functionBuildings = manager.getBuildingSet(functionType);

		Building destination = null;
		boolean canAdd = false;

		for (Building bldg : functionBuildings) {
			// Go to the default zone 0 only
			if (!canAdd && bldg.getZone() == 0
			// Do not add robot to EVA airlock, hallway and tunnel
					&& bldg.getCategory() != BuildingCategory.EVA && bldg.getCategory() != BuildingCategory.CONNECTION
					&& bldg.getFunction(functionType).hasEmptyActivitySpot()) {
				destination = bldg;
				canAdd = addToActivitySpot(robot, destination, functionType);
			}
		}

		functionBuildings = manager.getBuildingSet(FunctionType.ROBOTIC_STATION);
		for (Building bldg : functionBuildings) {
			if (!canAdd && bldg.getZone() == 0 && bldg.getCategory() != BuildingCategory.EVA
					&& bldg.getFunction(FunctionType.ROBOTIC_STATION).hasEmptyActivitySpot()) {
				destination = bldg;
				canAdd = addToActivitySpot(robot, destination, FunctionType.ROBOTIC_STATION);
			}
		}

		Set<Building> buildings = manager.getBuildingSet();
		for (Building bldg : buildings) {
			// Avoid going inside an EVA Airlock that will interfere its intricate operation
			if (bldg.getCategory() != BuildingCategory.EVA) {
				for (Function function : bldg.getFunctions()) {
					if (!canAdd && bldg.getZone() == 0 && function.hasEmptyActivitySpot()) {
						destination = bldg;
						canAdd = addToActivitySpot(robot, destination, function.getFunctionType());
					}
				}
			}
		}
	}

	/**
	 * Adds a vehicle to a random ground vehicle maintenance building within a
	 * settlement.
	 *
	 * @param vehicle    the vehicle to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if vehicle cannot be added to any building.
	 *
	 * @return the garage building already in or just added
	 */
	public Building addToGarageBuilding(Vehicle vehicle) {
		// if no garage buildings are present in this settlement
		if (garages.isEmpty()) {
			return null;
		}

		if (vehicle.isBeingTowed()
				|| (VehicleType.isRover(vehicle.getVehicleType()) && ((Rover) vehicle).isTowingAVehicle())) {
			return null;
		}

		for (Building garageBuilding : garages) {
			VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();

			if (vehicle instanceof Rover r) {
				if (garage.containsRover(r)) {
					logger.info(r, 60_000, "Already inside " + garageBuilding.getName() + ".");

					return garageBuilding;
				} else {
					boolean vacated = false;

					// If there is no garage space, check if an existing rover can leave
					// the garage to make room for a new rover to come in
					if (garage.getAvailableRoverCapacity() == 0) {
						// Try removing a non-reserved vehicle inside a garage
						for (Rover rover : garage.getRovers()) {
							if (!vacated && !rover.isReserved() && !rover.isReservedForMaintenance()
									&& rover.getMission() == null && rover.hasNoCrew()
									&& garage.removeRover(rover, true)) {
								vacated = true;
								break;
							}
						}
					}

					if ((garage.getAvailableRoverCapacity() > 0) && garage.addRover(r, true)) {

						return garageBuilding;
					}
				}
			}

			else if (vehicle instanceof Flyer f) {

				if (garage.containsFlyer(f)) {
					logger.info(f, 60_000, "Already inside " + garageBuilding.getName() + ".");

					return garageBuilding;
				} else {
					boolean vacated = false;

					// If there is no garage space, check if an existing flyer can leave
					// the garage to make room for a new flyer to come in
					if (garage.getAvailableFlyerCapacity() == 0) {
						// Try removing a non-reserved drone inside a garage
						for (Flyer flyer : garage.getFlyers()) {
							if (!vacated && !flyer.isReserved() && !flyer.isReservedForMaintenance()
									&& flyer.getMission() == null && garage.removeFlyer(flyer, true)) {
								vacated = true;
								break;
							}
						}
					}

					if (garage.getAvailableFlyerCapacity() > 0 && garage.addFlyer(f, true)) {

						return garageBuilding;
					}
				}
			}

			else if (vehicle instanceof LightUtilityVehicle luv) {
				if (garage.containsUtilityVehicle(luv)) {
					logger.info(luv, 60_000, "Already inside " + garageBuilding.getName() + ".");

					return garageBuilding;
				} else {
					boolean vacated = false;

					if (garage.getAvailableUtilityVehicleCapacity() == 0) {
						// Try removing a non-reserved vehicle inside a garage
						for (LightUtilityVehicle l : garage.getUtilityVehicles()) {
							if (!vacated && !l.isReserved() && !l.isReservedForMaintenance() && l.getMission() == null
									&& l.hasNoCrew() && garage.removeUtilityVehicle(l, false)) {
								vacated = true;
								break;
							}
						}
					}

					if ((garage.getAvailableUtilityVehicleCapacity() > 0) && garage.addUtilityVehicle(luv, true)) {

						return garageBuilding;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Adds a vehicle to a random ground vehicle maintenance building within a
	 * settlement.
	 *
	 * @param vehicle    the vehicle to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if vehicle cannot be added to any building.
	 *
	 * @return true if it's already in the garage or added to a garage
	 */
	public boolean addToGarage(Vehicle vehicle) {
		// Check if the vehicle is already inside garage
		// Note: use vehicle.isInGarage() to check since it returns the boolean value
		// of isInGarage instead of having to go through the long steps of
		// BuildingManager's isInGarage() as shown below.
		if (vehicle.isInGarage()) {
			return true;
		}
		return (addToGarageBuilding(vehicle) != null);
	}

	/**
	 * Checks if the vehicle is currently in a garage or not.
	 *
	 * @return true if vehicle is in a garage.
	 */
	public boolean isInGarage(Vehicle vehicle) {
		// Note: do not use vehicle.isInGarage() here

		if (getGarages().isEmpty())
			return false;

		for (Building garageBuilding : getGarages()) {
			VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
			if (garage == null) {
				continue;
			}

			if (vehicle instanceof Rover r && garage.containsRover(r)) {
				return true;
			}

			if (vehicle instanceof Drone d && garage.containsFlyer(d)) {
				return true;
			}

			if (vehicle instanceof LightUtilityVehicle luv && garage.containsUtilityVehicle(luv)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets an available vehicle maintenance building for resource hookup.
	 *
	 * @param settlement
	 * @return building or null if none.
	 */
	public static Building getAGarage(Settlement settlement) {
		if (settlement != null) {
			List<Building> list = settlement.getBuildingManager().getBuildings(FunctionType.VEHICLE_MAINTENANCE);
			return RandomUtil.getRandomElement(list);
		}
		return null;
	}

	/**
	 * Removes a vehicle from garage and transfers to a new parking location.
	 *
	 * @param vehicle
	 * @return true if the vehicle is inside a garage and can be removed
	 */
	public static boolean removeFromGarage(Vehicle vehicle) {
		// If the vehicle is in a garage, put the vehicle outside.
		Building garage = vehicle.getGarage();
		if (garage == null) {
			return false;
		}
		
		if (vehicle instanceof Rover rover 
				&& garage.getVehicleMaintenance().removeRover(rover, true)) {
			return true;
		} else if (vehicle instanceof Flyer flyer && garage.getVehicleMaintenance().removeFlyer(flyer, true)) {
			return true;
		} else if (vehicle instanceof LightUtilityVehicle luv
				&& garage.getVehicleMaintenance().removeUtilityVehicle(luv, true)) {
			return true;
		}

		return false;
	}

	/**
	 * Gets the building a person or robot is in.
	 *
	 * @return building or null if none.
	 */
	public static Building getBuilding(Worker worker) {

		if (worker.isInSettlement()) {
			return worker.getBuildingLocation();
		}

		if (worker.isInVehicleInGarage()) {
			return worker.getVehicle().getGarage();
		}

		return null;
	}

	/**
	 * Gets a list of the least crowded buildings from a given list of buildings
	 * with life support.
	 *
	 * @param buildingList list of buildings with the life support function.
	 * @return list of least crowded buildings.
	 * @throws BuildingException if building in list does not have the life support
	 *                           function.
	 */
	public static Set<Building> getLeastCrowdedBuildings(Set<Building> buildingList) {

		Set<Building> result = new UnitSet<>();

		// Find least crowded population.
		int leastCrowded = Integer.MAX_VALUE;
		for (Building b0 : buildingList) {
			if (b0.getCategory() != BuildingCategory.EVA) {
				LifeSupport lifeSupport = b0.getLifeSupport();
				int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
				if (crowded < -1)
					crowded = -1;
				if (crowded < leastCrowded) {
					// New leastCrowded so reset the list
					leastCrowded = crowded;
					result = new UnitSet<>();
					result.add(b0);
				} else if (crowded == leastCrowded) {
					result.add(b0);
				}
			}
		}

		return result;
	}

	/**
	 * Gets a list of the least crowded buildings from a given list of buildings
	 * with robotic stations.
	 *
	 * @param buildingList list of buildings with the robotic station function.
	 * @return list of least crowded buildings.
	 * @throws BuildingException if building in Set does not have robotic stations.
	 */
	public static Set<Building> getLeastCrowded4BotBuildings(Set<Building> buildingSet) {

		Set<Building> result = new UnitSet<>();

		// Find least crowded bot population.
		int leastCrowded = Integer.MAX_VALUE;
		for (Building building : buildingSet) {
//			if (building.getCategory() != BuildingCategory.EVA) {
			RoboticStation roboticStation = building.getRoboticStation();
			int crowded = roboticStation.getRobotOccupantNumber() - roboticStation.getOccupantCapacity();
			if (crowded < -1)
				crowded = -1;
			if (crowded < leastCrowded) {
				leastCrowded = crowded;
				result = new UnitSet<>();
				result.add(building);
			} else if (crowded == leastCrowded) {
				result.add(building);
			}
//			}
		}

		return result;
	}

	/**
	 * Gets a map of buildings and their probabilities for being chosen based on the
	 * best relationships for a given person from a list of buildings.
	 *
	 * @param person       the person to check for.
	 * @param buildingList the list of buildings to filter.
	 * @return map of buildings and their probabilities.
	 */
	public static Map<Building, Double> getBestRelationshipBuildings(Person person, Set<Building> buildings) {
		Map<Building, Double> result = new HashMap<>();
		// Determine probabilities based on relationships in buildings.
		for (Building building : buildings) {
			if (building.getCategory() != BuildingCategory.EVA) {
				LifeSupport lifeSupport = building.getLifeSupport();
				double buildingRelationships = 0D;
				int numPeople = 0;

//				List<Person> occupants = lifeSupport.getOccupants()
//						  .stream()
//						  .collect(Collectors.toList());
//				
//				for (Person occupant: occupants) {
//					if (person.equals(occupant)) {
//						buildingRelationships += RelationshipUtil.getOpinionOfPerson(person, occupant);
//						numPeople++;
//					}
//				}

				Optional<Person> found = lifeSupport.getOccupants().stream().filter(e -> person.equals(e)).findFirst();

				if (found.isPresent()) {
					Person occupant = found.get();
					buildingRelationships += RelationshipUtil.getOpinionOfPerson(person, occupant);
					numPeople++;
				}

				double prob = 50D;
				if (numPeople > 0) {
					prob = buildingRelationships / numPeople;
					if (prob < 0D) {
						prob = 0D;
					}
				}
				result.put(building, prob);
			}
		}
		return result;
	}

	/**
	 * Gets a map of buildings having on-going social conversations.
	 *
	 * @param buildingList the list of buildings to filter.
	 * @return map of buildings and their probabilities.
	 */
	private static Set<Building> getChattyBuildings(Set<Building> buildingList) {

		Set<Building> result = new HashSet<>();
		for (Building building : buildingList) {
			int numPeople = 0;
			for (Person occupant : building.getLifeSupport().getOccupants()) {
				if (occupant.getMind().getTaskManager().getTask() instanceof Converse) {
					numPeople++;
				}
			}
			if (numPeople > 0)
				result.add(building);
		}
		return result;
	}

	/**
	 * Gets a list of buildings that don't have any malfunctions from a list of
	 * buildings.
	 *
	 * @param buildingList the list of buildings.
	 * @return list of buildings without malfunctions.
	 */
	public static Set<Building> getNonMalfunctioningBuildings(Set<Building> buildingList) {
		return buildingList.stream().filter(b -> !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());
	}

	/**
	 * Transfers the worker from one building to another Note: Will add to or remove
	 * from life support/robotic station.
	 * 
	 * Note: origin building can be null
	 *
	 * @param worker      the worker to add.
	 * @param origin      the building to leave behind.
	 * @param destination the building to go
	 */
	public static void transferToBldg(Worker worker, Building origin, Building destination) {

		if (destination != null) {
			if (worker instanceof Person person) {

				if (origin != null && origin.getLifeSupport() != null) {
					origin.getLifeSupport().removePerson(person);
				}

				if (destination.getLifeSupport() != null) {
					destination.getLifeSupport().addPerson(person);

					person.setCurrentBuilding(destination);
				}
			}

			else {
				Robot robot = (Robot) worker;

				if (origin != null && origin.getRoboticStation() != null) {
					origin.getRoboticStation().removeRobot(robot);
				}

				if (destination.getRoboticStation() != null) {
					destination.getRoboticStation().addRobot(robot);

					robot.setCurrentBuilding(destination);
				}
			}
		}

		else
			logger.severe(worker, 2000, "The destination building is null.");
	}

	/**
	 * Adds a worker to the building if possible. Note: it will add the worker to
	 * life support / robotic station as well.
	 *
	 * @param worker   the worker to add.
	 * @param building the building to add.
	 */
	public static boolean addToBuilding(Worker worker, Building building) {
		return addToActivitySpot(worker, building, null);
	}

	/**
	 * Adds a worker to the building if possible. Note: it will add the worker to
	 * life support / robotic station as well.
	 *
	 * @param worker   the worker to add.
	 * @param building the building to add.
	 * @param type     the function type
	 * @return
	 */
	public static boolean addToActivitySpot(Worker worker, Building building, FunctionType type) {
		boolean result = false;

		Building originBuilding = worker.getBuildingLocation();

		if (originBuilding == null) {
			// Instantly set the worker's current building and add occupant since this
			// worker has
			// just been added to the settlement or just returned to the settlement from
			// outside
			transferToBldg(worker, null, building);
		}

		FunctionType functionType = type;

		if (functionType != null) {
			// Try claiming a spot
			result = claimActivitySpot(worker, building, functionType);
		}
		
		if (!result) {
		
			for (Function f: building.getFunctions()) {
					
				if (f.hasEmptyActivitySpot()) {
					functionType = f.getFunctionType();
					
					if (functionType != null) {
						// Try claiming a spot
						result = claimActivitySpot(worker, building, functionType);
						
						if (result) {
							break;
						}
					}
				}
			}
		}

		if (result) {
			// Load the claimed spot
			AllocatedSpot as = worker.getActivitySpot();
			// Set robot's location
			worker.setPosition(as.getAllocated().getPos());

//			if (originBuilding != null && !originBuilding.equals(building)) {
				// Instantly transfer the worker to the new building
				transferToBldg(worker, originBuilding, building);
//			}
		}

		else if (functionType != null) {

			logger.info(worker, 10_000L,
					"Unable to claim a spot at " + functionType.getName() + " in " + building.getName() + ".");
		}

		return result;
	}

	/**
	 * Claims an activity spot.
	 * 
	 * @param worker
	 * @param building
	 * @param functionType
	 * @return
	 */
	public static boolean claimActivitySpot(Worker worker, Building building, FunctionType functionType) {

		Function f = building.getFunction(functionType);

		LocalPosition loc = f.getAvailableActivitySpot();

		if (loc != null) {
			// Note: if the following log is enabled, it will be excessive.
			// May add back: logger.info(worker, 10_000L, "Available loc " + loc + " found.
			// Trying to claim it.")
			// Claim this activity spot
			return f.claimActivitySpot(loc, worker);
		}

		return false;
	}

	/**
	 * Removes the person from a building if possible.
	 *
	 * @param person   the person to remove.
	 * @param building the building to remove the person from.
	 */
	public static void removePersonFromBuilding(Person person, Building building) {
		if (building != null && building.getLifeSupport() != null) {
			building.getLifeSupport().removePerson(person);
			person.setCurrentBuilding(null);
			person.leaveActivitySpot(false);
		}
	}

	/**
	 * Removes the robot from a building if possible.
	 *
	 * @param robot    the robot to remove.
	 * @param building the building to remove the robot from.
	 */
	public static void removeRobotFromBuilding(Robot robot, Building building) {
		if (building != null && building.getRoboticStation() != null) {
			building.getRoboticStation().removeRobot(robot);
			robot.setCurrentBuilding(null);
			robot.leaveActivitySpot(false);
		}
	}

	/**
	 * Gets the values of each building type at the settlement.
	 *
	 * @return a map of building values.
	 */
	public Map<Building, Double> getAllBuildingTypeValues() {

		// Update building values cache once per Sol.
		MarsTime now = masterClock.getMarsTime();
		if (totalBuildingValues == 0D || (lastVPUpdateTime == null)
				|| (now.getTimeDiff(lastVPUpdateTime) > BUILDING_VALUES_UPDATE)) {

			buildingValueMap.clear();
			lastVPUpdateTime = now;

			computeAllFunctionTypeValues();
		}

		return buildingValueMap;
	}

	/**
	 * 
	 * Computes all the function type values for all buildings.
	 */
	private void computeAllFunctionTypeValues() {
		double total = 0;
		for (Building building : getBuildingSet()) {
			total += computeOneBuildingFunctionTypeValues(building);
		}
		totalBuildingValues = total;
	}

	/**
	 * Computes a map of function value of a building.
	 *
	 * @param building the building.
	 * @return a map of each function type with function value.
	 */
	public Map<FunctionType, Double> computeFunctionTypeValue(Building building) {

		if (!buildingsOfFunctionTypeValues.containsKey(building) || computeSumOfFunctionTypeValue(building) == 0D) {
			computeOneBuildingFunctionTypeValues(building);
		}

		return buildingsOfFunctionTypeValues.get(building);
	}

	/**
	 * Computes the sum of all function type values.
	 * 
	 * @param building
	 * @return
	 */
	public double computeSumOfFunctionTypeValue(Building building) {
		return buildingsOfFunctionTypeValues.get(building).values().stream().mapToDouble(Double::doubleValue).sum();
	}

	/**
	 * Computes all the function type values for one single buildings.
	 * 
	 * @param building
	 */
	private double computeOneBuildingFunctionTypeValues(Building building) {

		double totalValue = 0D;

		/** A map of each function type and its value. */
		EnumMap<FunctionType, Double> functionTypeValues = new EnumMap<>(FunctionType.class);

		for (Function f : building.getFunctions()) {

			FunctionType ft = f.getFunctionType();

			double value = f.getFunctionValue();

			totalValue += value;

			// Note: Remove the wear condition modification in each getFunctionValue() in a
			// Function subclass

			// Modify building value by its wear condition.
//			double wearCondition = building.getMalfunctionManager().getWearCondition();
//			value *= (wearCondition / 100D) * .75D + .25D;

			functionTypeValues.put(ft, value);
		}

		buildingsOfFunctionTypeValues.put(building, functionTypeValues);

		buildingValueMap.put(building, totalValue);

		return totalValue;
	}

	/**
	 * Gets the values of each building type at the settlement.
	 *
	 * @return a map of building values.
	 */
	public double getTotalBuildingValues() {
		return totalBuildingValues;
	}

//	/**
//	 * Checks if a proposed building location is open or intersects with existing
//	 * buildings or construction sites.
//	 *
//	 * @param position The position of the new building
//	 * @return true if new building location is open.
//	 */
//	public boolean isBuildingLocationOpen(BoundedObject position) {
//		return isBuildingLocationOpen(position, null);
//	}

	/**
	 * Checks if a proposed building location is open and without intersecting with
	 * any existing buildings or construction sites.
	 *
	 * @param position New building position
	 * @return true if new building location is open.
	 */
	public boolean isBuildingLocationOpen(BoundedObject position) {
		boolean goodLocation = true;

		goodLocation = LocalAreaUtil.isObjectCollisionFree(position, position.getWidth(), position.getLength(),
				position.getXLocation(), position.getYLocation(), position.getFacing(), settlement.getCoordinates(), settlement);

		return goodLocation;
	}

	/**
	 * Gets the next template ID for a new building in a settlement (but not unique
	 * in a simulation).
	 *
	 * @return template ID (starting from 0).
	 */
	public int getNextTemplateID(String buildingType) {
		return buildings.size();
		// Note: check with getUniqueName() and getUniqueNum() methods below for
		// comparison
	}

	/**
	 * Gets an unique name for a new building.
	 *
	 * @return a unique nick name
	 */
	public String getUniqueName(String buildingType) {
		return buildingType + " " + getUniqueNum(buildingType);
	}

	/**
	 * Gets an unique number for a new building.
	 *
	 * @return a unique number
	 */
	public int getUniqueNum(String buildingType) {
		long id = buildings.stream().filter(b -> b.getBuildingType().equals(buildingType)).count() + 1;
		return (int) id;
	}

	/**
	 * Gets total combined power loads from all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double[] getTotalCombinedLoads() {
		double loadTotal = 0;
		double nonloadTotal = 0;
		Set<Building> nodeBldgs = getComNodes();
		if (nodeBldgs.isEmpty())
			return new double[] { 0, 0 };
		for (Building b : nodeBldgs) {
			Computation node = b.getComputation();
			double[] combined = node.getSeparatePowerLoadNonLoad();
			double load = combined[0];
			double nonload = combined[1];
			loadTotal += load;
			nonloadTotal += nonload;
		}
		return new double[] { loadTotal, nonloadTotal };
	}

	/**
	 * Gets usage percentage from all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double[] getPeakCurrentPercent() {
		double peak = 0;
		double current = 0;
		for (Building b : getComNodes()) {
			Computation node = b.getComputation();
			current += node.getCurrentCU();
			peak += node.getPeakCU();
		}

		return new double[] { current, peak };
	}

	/**
	 * Gets total entropy of all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double getTotalEntropy() {
		double entropy = 0;
		for (Building b : getComNodes()) {
			Computation node = b.getComputation();
			entropy += node.getEntropy();
		}
		return entropy;
	}

	/**
	 * Gets total entropy of all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double[] getTotalEntropyPerNode() {
		double entropy = 0;
		Set<Building> nodeBldgs = getComNodes();
		if (nodeBldgs.isEmpty())
			return new double[] { 0, 0 };
		int size = nodeBldgs.size();
		for (Building b : nodeBldgs) {
			Computation node = b.getComputation();
			entropy += node.getEntropy();
		}
		return new double[] { size, entropy };
	}

	/**
	 * Gets total entropy per CU of all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double[] getTotalEntropyPerCU() {
		double entropyPerCU = 0;
		Set<Building> nodeBldgs = getComNodes();
		int size = nodeBldgs.size();
		if (nodeBldgs.isEmpty())
			return new double[] { 0, 0 };
		for (Building b : nodeBldgs) {
			Computation node = b.getComputation();
			double ePerCU = node.getEntropyPerCU();
			entropyPerCU += ePerCU;
		}
		return new double[] { size, entropyPerCU };
	}

	/**
	 * Gets a computing node for having the worst entropy by probability.
	 * 
	 * @param person
	 * @param anyZones
	 * @return
	 */
	public Computation getWorstEntropyComputingNodeByProbability(Person person, boolean anyZones) {
		Map<Computation, Double> scores = new HashMap<>();
		Set<Building> bldgs = getComNodes();

		if (bldgs.isEmpty())
			return null;

		if (person.getBuildingLocation() != null) {
			int personZone = person.getBuildingLocation().getZone();

			if (anyZones) {
				bldgs = bldgs.stream()
						// Condition: the building doesn't need to be in the same zone as the person
						.filter(b -> !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			} else {
				bldgs = bldgs.stream().filter(b ->
				// Condition: the building must be in the same zone as the person
				// Note: the condition below needs to be true
				b.getZone() == personZone && !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			}

		} else {
			if (anyZones) {
				bldgs = bldgs.stream()
						// Condition: the building doesn't need to be in the same zone as the person
						// .filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			} else {
				bldgs = bldgs.stream().filter(b ->
				// Condition: the building must be in the same zone as the person
				// Note: only buildings in zone 0 will be chosen
				b.getZone() == 0 && !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			}
		}

		if (bldgs.isEmpty()) {
			return null;
		}

		for (Building b : bldgs) {
			Computation node = b.getComputation();
			double entropy = node.getEntropy();
			scores.put(node, entropy);
		}

		return RandomUtil.getWeightedRandomObject(scores);
	}

	/**
	 * Gets a computing center for having the most free resources by probability.
	 * 
	 * @param need      CU(s) per millisol
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Computation getMostFreeComputingNode(double need, int startTime, int endTime) {
		Map<Computation, Double> scores = new HashMap<>();

		for (Building b : getComNodes()) {
			Computation node = b.getComputation();
			double score = node.evaluateScheduleTask(need, startTime, endTime);
			if (score > 0)
				scores.put(node, score);
		}

		if (scores.isEmpty())
			return null;

		// Note: Use probability selection
		return RandomUtil.getWeightedRandomObject(scores);
	}

	/**
	 * Gets total entropy of all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double getTotalEntropyPerLab() {
		double entropy = 0;
		Set<Building> bldgs = getBuildingSet(FunctionType.RESEARCH);

		if (bldgs.isEmpty())
			return 0;

		int size = bldgs.size();
		for (Building b : bldgs) {
			Research lab = b.getResearch();
			entropy += lab.getEntropy();
		}
		return entropy / size;
	}

	/**
	 * Gets a lab for having the worst entropy by probability.
	 * 
	 * @param person
	 * @param anyZones
	 * @return
	 */
	public Research getWorstEntropyLabByProbability(Person person, boolean anyZones) {
		Map<Research, Double> scores = new HashMap<>();
		Set<Building> bldgs = getBuildingSet(FunctionType.RESEARCH);

		if (bldgs.isEmpty())
			return null;

		if (person.getBuildingLocation() != null) {
			int personZone = person.getBuildingLocation().getZone();

			if (anyZones) {
				bldgs = bldgs.stream()
						// Condition: the building doesn't need to be in the same zone as the person
						.filter(b -> !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			} else {
				bldgs = bldgs.stream().filter(b ->
				// Condition: the building must be in the same zone as the person
				// Note: the condition below needs to be true
				b.getZone() == personZone && !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			}

		} else {
			if (anyZones) {
				bldgs = bldgs.stream()
						// Condition: the building doesn't need to be in the same zone as the person
						// .filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			} else {
				bldgs = bldgs.stream().filter(b ->
				// Condition: the building must be in the same zone as the person
				// Note: only buildings in zone 0 will be chosen
				b.getZone() == 0 && !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			}
		}

		if (bldgs.isEmpty()) {
			return null;
		}

		for (Building b : bldgs) {
			Research lab = b.getResearch();
			double entropy = lab.getEntropy();
			scores.put(lab, entropy);
		}

		return RandomUtil.getWeightedRandomObject(scores);
	}

	/**
	 * Gets a set of farm buildings needing work from a list of buildings with the
	 * farming function.
	 *
	 * @param buildingList list of buildings with the farming function.
	 * @return list of farming buildings needing work.
	 */
	public Set<Building> getFarmsNeedingWork() {
		Set<Building> result = null;

		if (farmsNeedingWorkCache == null)
			farmsNeedingWorkCache = new UnitSet<>();

		// Must use the absolute time otherwise it stalls after one sol day
		double m = masterClock.getMarsTime().getTotalMillisols();

		// Add caching and relocate from TendGreenhouse
		if ((farmTimeCache + 20) >= m && !farmsNeedingWorkCache.isEmpty()) {
			result = farmsNeedingWorkCache;
		}

		else {
			farmTimeCache = m;
			Set<Building> farmBuildings = getNonMalfunctioningBuildings(getBuildingSet(FunctionType.FARMING));
			result = new UnitSet<>();

			for (Building b : farmBuildings) {
				if (b.getFarming().requiresWork()) {
					result.add(b);
				}
			}

			farmsNeedingWorkCache = result;
		}

		return result;
	}

	/**
	 * Gets an available building with a particular function in the same zone.
	 *
	 * @param person the person looking for a facility.
	 * @return an available space or null if none found.
	 */
	public static Building getAvailableFunctionTypeBuilding(Person person, FunctionType functionType) {
		return getAvailableFunctionBuilding(person, functionType, false);
	}

	/**
	 * Gets an available building with a particular function in a particular zone.
	 *
	 * @param person       the person looking for a facility.
	 * @param functionType
	 * @param anyZones
	 * @return
	 */
	public static Building getAvailableFunctionBuilding(Person person, FunctionType functionType, boolean anyZones) {

		Set<Building> buildings = null;

		if (person.getBuildingLocation() != null) {
			int personZone = person.getBuildingLocation().getZone();

			if (anyZones) {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType).stream()
						// Condition: the building doesn't need to be in the same zone as the person
						.filter(b -> !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			} else {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType).stream().filter(b ->
				// Condition: the building must be in the same zone as the person
				// Note: the condition below needs to be true
				b.getZone() == personZone && !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			}

		} else {
			if (anyZones) {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType).stream()
						// Condition: the building doesn't need to be in the same zone as the person
						.filter(b -> !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			} else {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType).stream()

						.filter(b ->
						// Condition: the building must be in the same zone as the person
						// Note: only buildings in zone 0 will be chosen
						b.getZone() == 0 && !b.getMalfunctionManager().hasMalfunction()).collect(Collectors.toSet());
			}
		}

		buildings = getLeastCrowdedBuildings(buildings);

		if (!buildings.isEmpty()) {
			return RandomUtil.getWeightedRandomObject(getBestRelationshipBuildings(person, buildings));
		}

		return null;
	}

	/**
	 * Is the astronomy observatory the owner of this EVA Airlock ?
	 * 
	 * @param airlockBuilding
	 * @return
	 */
	public boolean isObservatoryAttached(Building airlockBuilding) {
		if (airlockBuilding.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION))
			return true;

		for (Building bb : createAdjacentBuildings(airlockBuilding)) {
			if (bb.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Creates a set of adjacent buildings attached to this building.
	 *
	 * @param building
	 * @return a set of adjacent buildings
	 */
	private Set<Building> createAdjacentBuildings(Building building) {
		Set<Building> adjBuildings = new HashSet<>();

		for (BuildingConnector c : getBuildingConnectorManager().getConnectionsToBuilding(building)) {
			Building b1 = c.getBuilding1();
			Building b2 = c.getBuilding2();
			if (b1 != building) {
				adjBuildings.add(b1);
			} else if (b2 != building) {
				adjBuildings.add(b2);
			}
		}

		return adjBuildings;
	}

	/**
	 * Creates a map of buildings with their lists of building connectors attached
	 * to it.
	 */
	public void createAdjacentBuildingMap() {
		if (adjacentBuildingMap == null)
			adjacentBuildingMap = new HashMap<>();
		for (Building b : getBuildingSet()) {
			Set<Building> connectors = createAdjacentBuildings(b);
			adjacentBuildingMap.put(b, connectors);
		}
	}

	/**
	 * Gets a set of buildings attached to this building.
	 *
	 * @param building
	 * @return
	 */
	public Set<Building> getAdjacentBuildings(Building building) {
		if (adjacentBuildingMap == null) {
			createAdjacentBuildingMap();
		}

		if (!adjacentBuildingMap.containsKey(building)) {
			return new UnitSet<>();
		}

		return adjacentBuildingMap.get(building);
	}

	/**
	 * Retrieves maintenance parts from all entities associated with this
	 * settlement.
	 */
	public void retrieveAllEntitiesMaintParts() {
		for (Malfunctionable entity : MalfunctionFactory.getAssociatedMalfunctionables(settlement)) {
			retrieveMaintParts(entity);
		}
	}

	/**
	 * Retrieves maintenance parts from an entity.
	 * 
	 * @param entity
	 */
	public void retrieveMaintParts(Malfunctionable entity) {

		Map<MaintenanceScope, Integer> parts = entity.getMalfunctionManager().retrieveMaintenancePartsFromManager();

		if (!parts.isEmpty()) {

			if (!partsMaint.isEmpty()) {
				Map<MaintenanceScope, Integer> partsMaintEntry = partsMaint.get(entity);
				if (partsMaintEntry == null || partsMaintEntry.isEmpty()) {
					// Post the parts and inject the demand
					injectMaintenancePartsDemand(entity, parts);
				}

				if (partsMaintEntry != null && partsMaintEntry.equals(parts)) {
//						logger.info(entity, 30_000L, "Both are already equal: " + partsMaintEntry + " and " + parts);
				} else {
					// Post the parts and inject the demand
					injectMaintenancePartsDemand(entity, parts);
				}
			} else {
				logger.info(entity, 30_000L, "The maint list was empty. " + parts + " just got posted.");
				// Post the parts and inject the demand
				injectMaintenancePartsDemand(entity, parts);
			}
		}
	}

	/**
	 * Posts the part and injects the demand.
	 * 
	 * @param entity
	 * @param parts
	 */
	public void injectMaintenancePartsDemand(Malfunctionable entity, Map<MaintenanceScope, Integer> parts) {
		// Post it up as maintenance parts
		partsMaint.put(entity, parts);
		// Inject demand
		for (MaintenanceScope ms : parts.keySet()) {
			Part part = ms.getPart();
			int num = parts.get(ms);
			// Inject the demand onto this part
			if (num > 0)
				injectPartDemand(part, settlement, num, MalfunctionManager.MAINTENANCE_REQUIRED_PART_FACTOR);
		}
	}

	/**
	 * Injects part demand directly.
	 * 
	 * @param part
	 * @param settlement
	 * @param num
	 */
	public static void injectPartDemand(Part part, Settlement settlement, int num, double factor) {
		Good good = GoodsUtil.getGood(part.getID());
		((PartGood) good).injectPartDemand(part, settlement.getGoodsManager(), num, factor);
	}

	/**
	 * Injects equipment demand directly.
	 * 
	 * @param type
	 * @param settlement
	 * @param stored
	 * @param needNum
	 */
	public static void injectEquipmentDemand(EquipmentType type, Settlement settlement, int stored, int needNum) {
		Good good = GoodsUtil.getGood(EquipmentType.getResourceID(type));
		((EquipmentGood) good).injectEquipmentDemand(type, settlement.getGoodsManager(), stored, needNum);
	}

	/**
	 * Updates the needed maintenance parts for a entity.
	 * 
	 * @param requestEntity
	 */
	public void updateMaintenancePartsMap(Malfunctionable requestEntity, Map<MaintenanceScope, Integer> newParts) {
		if (partsMaint.isEmpty()) {
			partsMaint.put(requestEntity, newParts);
			logger.info(requestEntity, 20_000L,
					"Maintenance parts updated: " + MalfunctionManager.getPartsString(newParts));
		} else {
			Iterator<Malfunctionable> i = partsMaint.keySet().iterator();
			while (i.hasNext()) {
				Malfunctionable entity = i.next();
				if (requestEntity.equals(entity)) {
					if (newParts == null || newParts.isEmpty()) {
						// This means that this part has been consumed
						i.remove();
						logger.info(entity, 20_000L, "Maintenance parts installed.");
					} else {
						// Overwrite with the parts that are still in shortfall
						partsMaint.put(entity, newParts);
						logger.info(entity, 20_000L,
								"Maintenance parts updated: " + MalfunctionManager.getPartsString(newParts));
					}
				}
			}
		}
	}

	/**
	 * Gets the number of maintenance parts from a particular settlement.
	 * 
	 * @param settlement
	 * @param part
	 */
	public int getMaintenanceDemand(Part part) {

		if (partsMaint.isEmpty())
			return 0;

		int numRequest = 0;

		for (Malfunctionable entity : partsMaint.keySet()) {
			Map<MaintenanceScope, Integer> partMap = partsMaint.get(entity);
			for (MaintenanceScope ms : partMap.keySet()) {
				if (ms.getPart().equals(part))
					numRequest += partMap.get(ms);
			}
		}

		return numRequest;
	}

	/**
	 * Gets the building manager's settlement.
	 *
	 * @return settlement
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Get the properties of any Meteorites hitting this building manager
	 */
	public MeteoriteImpactProperty getMeteorite() {
		return meteorite;
	}

	/**
	 * Returns the BuildingConnectorManager instance.
	 * 
	 * @return
	 */
	public BuildingConnectorManager getBuildingConnectorManager() {
		return settlement.getBuildingConnectorManager();
	}

	/**
	 * Gets a handy set of garages for the settlement.
	 *
	 * @return
	 */
	public Set<Building> getGarages() {
		return garages;
	}

	/**
	 * Gets a handy set of observatories for the settlement.
	 *
	 * @return
	 */
	public Set<Building> getObservatories() {
		return observatories;
	}

	/**
	 * Gets a handy set of airlocks for the settlement.
	 *
	 * @return
	 */
	public Set<Building> getAirlocks() {
		return airlocks;
	}

	/**
	 * Gets a handy set of computational nodes for the settlement.
	 *
	 * @return
	 */
	public Set<Building> getComNodes() {
		return comNodes;
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param {@link  MasterClock}
	 * @param {{@link MarsClock}
	 */
	public static void initializeInstances(SimulationConfig sc, MasterClock c0, UnitManager u) {
		simulationConfig = sc;
		masterClock = c0;
		unitManager = u;
	}

	public static BuildingConfig getBuildingConfig() {
		return simulationConfig.getBuildingConfiguration();
	}

	/**
	 * Reconstructs the building lists after loading from a saved sim.
	 */
	public void reinit() {
		settlement = unitManager.getSettlementByID(settlementID);

		// Re-initializes maps and meteorite instance
		initializeFunctionsNMeteorite();

		// Re-create adjacent building map
		createAdjacentBuildingMap();
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		for (Building building : buildings) {
			building.destroy();
		}
		garages = null;
		observatories = null;
		airlocks = null;
		comNodes = null;
		buildings = null;
		partsMaint = null;
		lastVPUpdateTime = null;
		meteorite = null;
	}
}
