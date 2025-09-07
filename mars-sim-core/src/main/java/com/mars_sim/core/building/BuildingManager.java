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

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.config.BuildingConfig;
import com.mars_sim.core.building.config.BuildingSpec;
import com.mars_sim.core.building.connection.BuildingConnector;
import com.mars_sim.core.building.connection.BuildingConnectorManager;
import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.building.function.Administration;
import com.mars_sim.core.building.function.AstronomicalObservation;
import com.mars_sim.core.building.function.BuildingConnection;
import com.mars_sim.core.building.function.Communication;
import com.mars_sim.core.building.function.Computation;
import com.mars_sim.core.building.function.EVA;
import com.mars_sim.core.building.function.EarthReturn;
import com.mars_sim.core.building.function.Exercise;
import com.mars_sim.core.building.function.FoodProduction;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.LivingAccommodation;
import com.mars_sim.core.building.function.Management;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.building.function.MedicalCare;
import com.mars_sim.core.building.function.Recreation;
import com.mars_sim.core.building.function.Research;
import com.mars_sim.core.building.function.ResourceProcessing;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.building.function.Storage;
import com.mars_sim.core.building.function.VehicleGarage;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.building.function.WasteProcessing;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.building.function.cooking.Dining;
import com.mars_sim.core.building.function.farming.AlgaeFarming;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.building.function.farming.Fishery;
import com.mars_sim.core.building.utility.heating.ThermalGeneration;
import com.mars_sim.core.building.utility.power.PowerGeneration;
import com.mars_sim.core.building.utility.power.PowerStorage;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.environment.MeteoriteImpactProperty;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.goods.PartGood;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionFactory;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.Converse;
import com.mars_sim.core.person.ai.task.Sleep;
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
import com.mars_sim.core.vehicle.StatusType;
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

	private transient MarsTime lastVPUpdateTime;

	private Set<Building> buildings = new UnitSet<>();
	private Set<Building> garages = new UnitSet<>();
	private Set<Building> observatories = new UnitSet<>();
	private Set<Building> airlocks = new UnitSet<>();
	private Set<Building> comNodes = new UnitSet<>();
	
	private transient Map<String, Double> vPNewCache = new HashMap<>();
	private transient Map<String, Double> vPOldCache = new HashMap<>();
	private transient EnumMap<FunctionType, Set<Building>> buildingFunctionsMap;
	/** The settlement's map of adjacent buildings. */
	private transient Map<Building, Set<Building>> adjacentBuildingMap = new HashMap<>();
	/** The settlement's maintenance parts map. */
	private Map<Malfunctionable, Map<MaintenanceScope, Integer>> partsMaint = new HashMap<>();
	
	private transient Settlement settlement;
	private MeteoriteImpactProperty meteorite;
	
	// Data members
	/** The population capacity (determined by the # of beds) of the settlement. */
	private int popCap = 0;

	private double farmTimeCache = -5D;
	
	/** The id of the settlement. */
	private int settlementID;
	
	private Set<Building> farmsNeedingWorkCache = new UnitSet<>();
	
	private static SimulationConfig simulationConfig;
	private static MasterClock masterClock;
	private static UnitManager unitManager;

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

		// Construct all buildings in the settlement.
		buildings = new UnitSet<>();
		
		if (buildingTemplates != null && !buildingTemplates.isEmpty()) {
			for (var bt : buildingTemplates) {
				
				BuildingSpec spec = simulationConfig.getBuildingConfiguration().getBuildingSpec(bt.getBuildingType());
				
				// Check for possibility of collision
				if (!Resupply.isTemplatePositionClear(spec, bt, this)) {
					throw new IllegalArgumentException(bt.getBuildingName() + " collided with another building.");
					// May relocate with bt = Resupply.clearCollision(spec, bt, Resupply.MAX_COUNTDOWN, this);
				}
	
				addBuilding(Building.createBuilding(bt, settlement), bt, false);
			}
		}
	}
	
	/**
	 * Initializes functions map and meteorite instance.
	 */
	public void initializeFunctionsNMeteorite() {

		if (buildingFunctionsMap == null)
			setupBuildingFunctionsMap();
		
		meteorite = new MeteoriteImpactProperty(settlement);
	}

	/**
	 * Sets up the map for the building functions.
	 */
	public void setupBuildingFunctionsMap() {
		buildingFunctionsMap = new EnumMap<>(FunctionType.class); 

		for (Building b : buildings) {
			addBuildingToMap(b);
		}

		// Get a handy shortcut to a set of garages
		garages = buildingFunctionsMap.computeIfAbsent(FunctionType.VEHICLE_MAINTENANCE,
				ft -> new UnitSet<>());
		
		// Get a handy shortcut to a set of observatories
		observatories = buildingFunctionsMap.computeIfAbsent(FunctionType.ASTRONOMICAL_OBSERVATION,
				ft -> new UnitSet<>());
		
		// Get a handy shortcut to a set of airlocks
		airlocks = buildingFunctionsMap.computeIfAbsent(FunctionType.EVA,
				ft -> new UnitSet<>());
		
		// Get a handy shortcut to a set of airlocks
		comNodes = buildingFunctionsMap.computeIfAbsent(FunctionType.COMPUTATION,
				ft -> new UnitSet<>());
	}
	

	/**
	 * Adds a building to the function map.
	 * 
	 * @param b
	 */
	private void addBuildingToMap(Building b) {
		for(Function f : b.getFunctions()) {
			buildingFunctionsMap.computeIfAbsent(f.getFunctionType(),
						ft -> new UnitSet<>()).add(b);
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

			settlement.fireUnitUpdate(UnitEventType.REMOVE_BUILDING_EVENT, oldBuilding);
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
		if (buildingFunctionsMap != null) {
			FunctionType ft = f.getFunctionType();
			Set<Building> list = buildingFunctionsMap.get(ft);
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
		if (buildingFunctionsMap == null)
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
	public void addBuilding(Building newBuilding, BuildingTemplate buildingTemplate, boolean createBuildingConnections) {
		addBuilding(newBuilding, createBuildingConnections);
				
		if (!buildings.contains(newBuilding)
			&& createBuildingConnections) {
			List<BuildingTemplate> buildingTemplates = new ArrayList<>();
			buildingTemplates.add(buildingTemplate);
			getBuildingConnectorManager().initialize(settlement, buildingTemplates);
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

			settlement.fireUnitUpdate(UnitEventType.ADD_BUILDING_EVENT, newBuilding);
			
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
	 * @param newBuilding               the building to add.
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
	 * Gets a list of settlement's buildings (not including hallway, tunnel or observatory)
	 * having a particular function type.
	 *
	 * @param functionType
	 * @return list of buildings
	 */
	public List<Building> getBuildingsNoHallwayTunnelObservatory(FunctionType functionType) {
		// Filter off hallways and tunnels
		return getBuildings(functionType).stream().filter(b ->
				b.getCategory() != BuildingCategory.CONNECTION
				&& !b.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)
				).toList();
	}

	/**
	 * Gets a list of settlement's buildings with thermal function.
	 *
	 * @return list of buildings
	 */
	public List<Building> getBuildingsWithThermal() {
		return getBuildings(FunctionType.THERMAL_GENERATION);
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
		if (buildingFunctionsMap == null) {
			setupBuildingFunctionsMap();
		}

		if (buildingFunctionsMap.containsKey(bf)) {
			return buildingFunctionsMap.get(bf);
		}

		else {
			Set<Building> set = buildings.stream().filter(b -> b.hasFunction(bf)).collect(Collectors.toSet());
			buildingFunctionsMap.put(bf, set);
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
		return buildings.stream().filter(b -> 
			!b.hasFunction(bf)
			&& !b.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)
			).collect(Collectors.toSet());
	}
	
	/**
	 * Gets a list of buildings in a settlement that has a given science type.
	 * 
	 * @param type ScienceType
	 * @return list of buildings
	 */
	public Set<Building>getBuildingsWithScienceType(Person person, ScienceType type) {
		Building origin = person.getBuildingLocation();
		if (origin != null) {
			return buildings
					.stream()
					.filter(b -> b.hasSpecialty(type)
							&& isGoodZone(origin, b)
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		
		return buildings
				.stream()
				.filter(b -> b.hasSpecialty(type)
						// This avoid a person to go to astronomy observatory (in zone 1)
						// needlessly
						&& b.getZone() == 0
						&& !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());		
		
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
		// this will allow someone in astronomy observatory (in zone 1) to come back home
		return (destination.getZone() == 0 
				|| (destination.getZone() == origin.getZone())); 
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
				}
				else 
					buildings = person.getSettlement().getBuildingManager()
						.getBuildingsWithScienceType(person, sType);
			}
			
			if (buildings != null && !buildings.isEmpty()) {
				Map<Building, Double> possibleBuildings = BuildingManager.
						getBestRelationshipBuildings(person, buildings);
				b = RandomUtil.getWeightedRandomObject(possibleBuildings);
			}
			
			if (b == null && buildings != null && !buildings.isEmpty()) {
				List<Building> bldg = new ArrayList<>(buildings);
				b = bldg.get(0);
			}
			if (b == null) {
				b = getBuildingWithSpot(person, FunctionType.RESEARCH,
						FunctionType.ADMINISTRATION,
						FunctionType.DINING,
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
	private static Building getBuildingWithSpot(Person person, FunctionType type1,
			FunctionType type2, FunctionType type3, FunctionType type4) {
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
			Map<Building, Double> possibleBuildings = BuildingManager.
					getBestRelationshipBuildings(person, buildings);
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
			return getBuildingSet(FunctionType.DINING)
					.stream()
					.filter(b -> isGoodZone(origin, b)
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		
		return getBuildingSet(FunctionType.DINING)
				.stream()
				.filter(b -> b.getZone() == 0
						&& !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());
	}
	
	
	/**
	 * Gets an available dining building that the person can use. Returns null if no
	 * dining building is currently available.
	 *
	 * @param person the person
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
            }
			else {
				BuildingManager.getLeastCrowdedBuildings(list0);
			}

			if (!list0.isEmpty()) {
				Map<Building, Double> probs = BuildingManager
						.getBestRelationshipBuildings(person, list0);
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
			return worker.getSettlement().getBuildingManager().getBuildingSet(functionType)
					.stream()
					.filter(b -> b.getZone() == worker.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		
		return worker.getSettlement().getBuildingManager().getBuildingSet(functionType)
				.stream()
				// Not possible to nail down the same zone
				.filter(b -> b.getZone() == 0
						&& !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());		
	}
	
	/**
	 * Gets the buildings in the same zone as the person in a settlement that 
	 * has function f1 and have no f2.
	 *
	 * @param p
	 * @param f1 the required function 
	 * @param f2 the excluded function
	 * @return list of buildings
	 */
	public Set<Building> getSameZoneBuildingsF1NoF2(Person p, FunctionType f1, FunctionType f2) {
		return buildings.stream()
				.filter(b -> b.hasFunction(f1) && !b.hasFunction(f2)
						&& p.getBuildingLocation().getZone() == b.getZone())
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the buildings in a settlement have no functions f1 and f2.
	 *
	 * @param functions the array of required functions {@link BuildingFunctions}.
	 * @return list of buildings.
	 */
	public List<Building> getBuildingsNoF1F2(FunctionType f1, FunctionType f2) {
		return buildings.stream()
				.filter(b -> !b.hasFunction(f1) && !b.hasFunction(f2))
				.toList();
	}
	
	/**
	 * Gets the buildings in a settlement have function f1 but with no functions f2 and f3.
	 *
	 * @param functions the array of required functions {@link BuildingFunctions}.
	 * @return list of buildings.
	 */
	public List<Building> getBuildingsF1NoF2F3(FunctionType f1, FunctionType f2, FunctionType f3) {
		return buildings.stream()
				.filter(b -> b.hasFunction(f1) && !b.hasFunction(f2) && !b.hasFunction(f3))
				.toList();
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
		return buildings.stream()
				.filter(b -> b.getCategory() == category)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the buildings in the settlement with a given building category and in zone 0.
	 *
	 * @param category the building type.
	 * @return list of buildings.
	 */
	public Set<Building> getBuildingsOfSameCategoryZone0(BuildingCategory category) {
		// Called by Resupply.java and BuildingConstructionMission.java
		// for putting new building next to the same building "type".
		return buildings.stream()
				.filter(b -> b.getCategory() == category
						&& b.getZone() == 0)
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
		return buildings.stream()
				.filter(b -> b.getBuildingType().equalsIgnoreCase(buildingType))
				.toList();
	}

	/**
	 * Gets a random building in a settlement that has a given function.
	 *
	 * @param bf {@link FunctionType} the function of the building.
	 * @return a building.
	 */
	public Building getABuilding(FunctionType bf) {
		if (buildingFunctionsMap == null) {
			buildingFunctionsMap = new EnumMap<>(FunctionType.class);
			setupBuildingFunctionsMap();
		}

		if (buildingFunctionsMap.containsKey(bf)) {
			return RandomUtil.getARandSet(buildingFunctionsMap.get(bf));
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
		Optional<Building> value = buildings.stream()
				.filter(b -> b.hasFunction(f1) && b.hasFunction(f2))
				.findAny(); //  .findFirst();

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

		if (buildingFunctionsMap == null) {
			buildingFunctionsMap = new EnumMap<>(FunctionType.class);
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
			}
			catch (RuntimeException rte) {
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
		
		Building building = s.getBuildingManager()
				.getABuilding(FunctionType.MEDICAL_CARE, FunctionType.LIFE_SUPPORT);

		if (building != null) {

			success = building.getMedical().addPatientToBed(p);
	
			if (success) {
				p.setCurrentBuilding(building);
				
				logger.info(p, 0, "Sent to a medical bed in " + building.getName() + ".");
			}
			else {
				logger.info(p, 0, "Unable to send to a medical bed in " + building.getName() + ".");
			}
		}

		else {
			// Send to his/her registered bed
			logger.log(p, Level.WARNING, 0,	"No medical facility available for "
							+ p.getName() + ". Go to his/her bed.");
			
			AllocatedSpot bed = p.getBed();
			
			if (bed != null) {
					
				Building b = bed.getOwner();
				// Question: does it still need to claim since this is already his own bed ?
				success = b.getLivingAccommodation().claimActivitySpot(bed.getAllocated().getPos(), p);
				
				if (success) {
					// Allocate it to the person
					p.setActivitySpot(bed);
					
					LocalPosition bedLoc = bed.getAllocated().getPos();	
			
					p.setPosition(bedLoc);
					
					logger.log(p, Level.WARNING, 0, "Go to his/her bed.");
					
					return success;
				}
			}

			// It will look for a permanent bed if possible
			AllocatedSpot tempBed = Sleep.findABed(s, p);
			
			if (tempBed == null) {
				// Assign a temporary bed to this person
				bed = LivingAccommodation.allocateBed(p.getSettlement(), p, false);	
				
				if (bed != null) {
					success = true;
					return success;
				}
			}	
		}
		
		return success;
	}

	/**
	 * Adds a person to a random habitable building activity spot within a settlement.
	 * Note: excluding the EVA (and astronomical observation) building
	 *
	 * @param person the person to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if person cannot be added to any building.
	 */
	public static void addPersonToRandomBuildingSpot(Person person, Settlement settlement) {
		
		// Go to the default zone 0 only
		Set<Building> bldgSet = person.getAssociatedSettlement().getBuildingManager()
					.getBuildingSet(FunctionType.LIFE_SUPPORT)
					.stream()
					.filter(b -> b.getZone() == 0
							&& b.getCategory() != BuildingCategory.EVA
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());

		if (bldgSet.isEmpty()) {
			return;
		}
		
		boolean found = false;
		
		for (Building building: bldgSet) {
			if (!found && building != null
					&& building.getCategory() != BuildingCategory.CONNECTION
					&& building.getCategory() != BuildingCategory.EVA) {
				// Add the person to a building activity spot
				found = addToActivitySpot(person, building, null);
			}
		}

		if (!found) {
			logger.warning(person, "No habitable buildings with empty activity spot available in zone 0.");
		}
	}

	/**
	 * Adds a person to a random habitable building within a settlement.
	 * Note: excluding the EVA building (and astronomical observation) building
	 *
	 * @param person       the person to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if person cannot be added to any building.
	 */
	public static void addPersonToRandomBuilding(Person person, Settlement settlement) {
		
		// Go to the default zone 0 only
		Set<Building> bldgSet = settlement.getBuildingManager()
					.getBuildingSet(FunctionType.LIFE_SUPPORT)
					.stream()
					.filter(b -> b.getZone() == 0
							&& b.getCategory() != BuildingCategory.EVA
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());

		if (bldgSet.isEmpty()) {
			return;
		}
				
		for (Building building: bldgSet) {
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
	 * @param unit       the robot to add.
	 * @param s the settlement to find a building.
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
					&& bldg.getCategory() != BuildingCategory.EVA
					&& bldg.getCategory() != BuildingCategory.CONNECTION
					&& bldg.getFunction(functionType).hasEmptyActivitySpot()) {
					destination = bldg;
					canAdd = addToActivitySpot(robot, destination, functionType);
			}
		}

		functionBuildings = manager.getBuildingSet(FunctionType.ROBOTIC_STATION);
		for (Building bldg : functionBuildings) {
			if (!canAdd && bldg.getZone() == 0
					&& bldg.getCategory() != BuildingCategory.EVA
					&& bldg.getFunction(FunctionType.ROBOTIC_STATION).hasEmptyActivitySpot()) {
				destination = bldg;
				canAdd = addToActivitySpot(robot, destination, FunctionType.ROBOTIC_STATION);
			}
		}	
		
		Set<Building> buildings = manager.getBuildingSet();
		for (Building bldg : buildings) {
			// Avoid going inside an EVA Airlock that will interfere its intricate operation
			if (bldg.getCategory() != BuildingCategory.EVA) {
				for (Function function: bldg.getFunctions()) {
					if (!canAdd && bldg.getZone() == 0
							&& function.hasEmptyActivitySpot()) {
						destination = bldg;		
						canAdd = addToActivitySpot(robot, destination, function.getFunctionType());
					}
				}
			}
		}
	}
	
	/**
	 * Adds a vehicle to a random ground vehicle maintenance building within
	 * a settlement.
	 *
	 * @param vehicle    the vehicle to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if vehicle cannot be added to any building.
	 *
	 * @return the garage building already in or just added
	 */
	public Building addToGarageBuilding(Vehicle vehicle) {

		if (vehicle.isBeingTowed() 
			|| (VehicleType.isRover(vehicle.getVehicleType())
					&& ((Rover)vehicle).isTowingAVehicle())) {
			return null;
		}

		// if no garage buildings are present in this settlement
		if (garages.isEmpty()) {
			// The vehicle may already be PARKED ?
//			vehicle.setPrimaryStatus(StatusType.PARKED);
			return null;
		}

		for (Building garageBuilding : garages) {
			VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
		
			 if (vehicle instanceof Rover r) {
				if (garage.containsRover(r)) {
					logger.info(r, 60_000,
							"Already inside " + garageBuilding.getName() + ".");

					return garageBuilding;
				}
				else { 
					boolean vacated = false;
					
					if (garage.getAvailableRoverCapacity() == 0) {
						// Try removing a non-reserved vehicle inside a garage		
						for (Rover rover: garage.getRovers()) {
							if (!vacated && !rover.isReserved() && rover.getMission() != null) {
								if (garage.removeRover(rover, true)) {
									vacated = true;
								}
							}
						}
					}
					
					if ((garage.getAvailableRoverCapacity() > 0)
						&& garage.addRover(r)) {

						// Vehicle already on Garage
						vehicle.setPrimaryStatus(StatusType.GARAGED);
						// Directly update the location state type
						vehicle.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
						
						logger.info(r, 60_000,
 							   "Just stowed inside " + garageBuilding.getName() + ".");
						return garageBuilding;
					}
				}
			}
			
			else if (vehicle instanceof Flyer f) {
				
				if (garage.containsFlyer(f)) {
					logger.info(f, 60_000,
							"Already inside " + garageBuilding.getName() + ".");

					return garageBuilding;
				}
				else { 
					boolean vacated = false;
					
					if (garage.getAvailableFlyerCapacity() == 0) {
						// Try removing a non-reserved drone inside a garage		
						for (Flyer flyer: garage.getFlyers()) {
							if (!vacated && !flyer.isReserved() && flyer.getMission() != null) {
								if (garage.removeFlyer(flyer)) {
									vacated = true;
								}
							}
						}
					}
					
					if (garage.getAvailableFlyerCapacity() > 0 
							&& garage.addFlyer(f)) {

						// Vehicle already on Garage
						vehicle.setPrimaryStatus(StatusType.GARAGED);
						// Directly update the location state type
						vehicle.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
						
						logger.info(f, 60_000,
 							   "Just stowed inside " + garageBuilding.getName() + ".");
						return garageBuilding;
					}
				}
			}
				
			else if (vehicle instanceof LightUtilityVehicle luv) {
				if (garage.containsUtilityVehicle(luv)) {
					logger.info(luv, 60_000,
							"Already inside " + garageBuilding.getName() + ".");

					return garageBuilding;
				}
				else { 
					boolean vacated = false;
					
					if (garage.getAvailableUtilityVehicleCapacity() == 0) {
						// Try removing a non-reserved vehicle inside a garage		
						for (LightUtilityVehicle l: garage.getUtilityVehicles()) {
							if (!vacated && !l.isReserved() && l.getMission() != null) {
								if (garage.removeUtilityVehicle(l, true)) {
									vacated = true;
								}
							}
						}
					}
					
					if ((garage.getAvailableUtilityVehicleCapacity() > 0)
						&& garage.addUtilityVehicle(luv)) {

						// Vehicle already on Garage
						vehicle.setPrimaryStatus(StatusType.GARAGED);
						// Directly update the location state type
						vehicle.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
						
						logger.info(luv, 60_000,
 							   "Just stowed inside " + garageBuilding.getName() + ".");
						return garageBuilding;
					}
				}
			}
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
		
		if (vehicle instanceof Rover rover) {
			if (garage.getVehicleMaintenance().removeRover(rover, true)) {
				return true;
			}
		}
		else if (vehicle instanceof Flyer flyer) {
			if (garage.getVehicleMaintenance().removeFlyer(flyer)) {
				return true;
			}
		}
		else if (vehicle instanceof LightUtilityVehicle luv) {
			if (garage.getVehicleMaintenance().removeUtilityVehicle(luv, true)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Adds a vehicle to a random ground vehicle maintenance building within
	 * a settlement.
	 *
	 * @param vehicle    the vehicle to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if vehicle cannot be added to any building.
	 *
	 * @return true if it's already in the garage or added to a garage
	 */
	public boolean addToGarage(Vehicle vehicle) {
		// Check if the vehicle is already inside garage
		if (isInGarage(vehicle)) {
			// Vehicle already on Garage
			vehicle.setPrimaryStatus(StatusType.GARAGED);
			// Directly update the location state type
			vehicle.setLocationStateType(LocationStateType.INSIDE_SETTLEMENT);
			
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
		if (settlement != null) {
			for (Building garageBuilding : getGarages()) {
				VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
				if (garage == null) {
					return false;
				}
				
				if (vehicle instanceof Rover r
						&& garage.containsRover(r)) {
						return true;
					}
				else if (vehicle instanceof Drone d
					&& garage.containsFlyer(d)) {
					return true;
				}
				else if (vehicle instanceof LightUtilityVehicle luv
					&& garage.containsUtilityVehicle(luv)) {
					return true;
				}
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
				}
				else if (crowded == leastCrowded) {
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
				}
				else if (crowded == leastCrowded) {
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
				for (Person occupant : lifeSupport.getOccupants()) {
					if (person.equals(occupant)) {
						buildingRelationships += RelationshipUtil.getOpinionOfPerson(person, occupant);
						numPeople++;
					}
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
	 * Sets the building of a worker and add to life support or robotic station.
	 *
	 * @param worker   the worker to add.
	 * @param building the building to add.
	 */
	public static void setToBuilding(Worker worker, Building building) {

		if (building != null) {
			if (worker instanceof Person person) {
	
				if (building.getLifeSupport() != null) {
					building.getLifeSupport().addPerson(person);

					person.setCurrentBuilding(building);
				}
			}

			else {
				Robot robot = (Robot) worker;
		
				if (building.getRoboticStation() != null) {
					building.getRoboticStation().addRobot(robot);
					
					robot.setCurrentBuilding(building);
				}
			}
		}

		else
			logger.severe(worker, 2000, "The building is null.");
	}

	/**
	 * Transfers the worker from one building to another 
	 * Note: Will add to or remove from life support/robotic station.
	 *
	 * @param worker   the worker to add.
	 * @param origin   the building to leave behind.
	 * @param destination the building to go
	 */
	public static void transferFromBuildingToBuilding(Worker worker, Building origin, Building destination) {

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
	 * Adds a worker to the building if possible.
	 * Note: it will add the worker to life support / robotic station as well.
	 *
	 * @param worker   the worker to add.
	 * @param building the building to add.
	 */
	public static boolean addToBuilding(Worker worker, Building building) {
		return addToActivitySpot(worker, building, null);
	}
	
	/**
	 * Adds a worker to the building if possible.
	 * Note: it will add the worker to life support / robotic station as well.
	 *
	 * @param worker   the worker to add.
	 * @param building the building to add.
	 * @return
	 */
	public static boolean addToActivitySpot(Worker worker, Building building, FunctionType functionType) {
		boolean result = false;

		Building originBuilding = worker.getBuildingLocation();
		
		if (functionType == null) {
			
			Function f = building.getEmptyActivitySpotFunction();
			if (f != null) {
				functionType = f.getFunctionType();
			}
		}
		
		if (originBuilding == null) {
			// Instantly set the worker's current building and add occupant
			setToBuilding(worker, building);
		}
		
		if (functionType != null) {
			result = claimActivitySpot(worker, building, functionType);
		}

		if (result) {
			// Load the claimed spot
			AllocatedSpot as = worker.getActivitySpot();
			// Set robot's location
			worker.setPosition(as.getAllocated().getPos());
			
			if (originBuilding != null && !originBuilding.equals(building)) {
				// Instantly transfer the worker to the new building
				transferFromBuildingToBuilding(worker, originBuilding, building);
			}
		}
		else {
			if (functionType != null) {
				logger.info(worker, 10_000L, "Unable to claim a " + functionType.getName() + " spot.");
			}
			else {
				logger.info(worker, 10_000L, "Unable to claim a spot.");
			}
				
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
//			logger.info(worker, 10_000L, "Available loc " + loc + " found. Trying to claim it.");
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
		} 
	}

	/**
	 * Removes the robot from a building if possible.
	 *
	 * @param robot   the robot to remove.
	 * @param building the building to remove the robot from.
	 */
	public static void removeRobotFromBuilding(Robot robot, Building building) {
		if (building != null && building.getRoboticStation() != null) {
			building.getRoboticStation().removeRobot(robot);
			robot.setCurrentBuilding(null);
		} 
	}

	/**
	 * Gets the value of a building at the settlement.
	 *
	 * @param buildingType the building type.
	 * @param newBuilding  true if adding a new building.
	 * @return building value (VP).
	 */
	public double getBuildingValue(String type, boolean newBuilding) {

		// Make sure building name is lower case.
		String buildingType = type.toLowerCase().trim();

		if (vPNewCache == null)
			vPNewCache = new HashMap<>();
		if (vPOldCache == null)
			vPOldCache = new HashMap<>();

		// Update building values cache once per Sol.
		MarsTime now = masterClock.getMarsTime();
		if ((lastVPUpdateTime == null)
				|| (now.getTimeDiff(lastVPUpdateTime) > 1000D)) {
			vPNewCache.clear();
			vPOldCache.clear();
			lastVPUpdateTime = now;
		}

		if (newBuilding && vPNewCache.containsKey(buildingType)) {
			return vPNewCache.get(buildingType);
		}

		else if (!newBuilding && vPOldCache.containsKey(buildingType)) {
			return vPOldCache.get(buildingType);
		}

		else {
			double result = 0D;
			BuildingSpec spec = simulationConfig.getBuildingConfiguration().getBuildingSpec(buildingType);

			for (FunctionType supported : spec.getFunctionSupported()) {
				result += switch (supported) {
					case ADMINISTRATION -> Administration.getFunctionValue(buildingType, newBuilding, settlement);
					case ALGAE_FARMING -> AlgaeFarming.getFunctionValue(buildingType, newBuilding, settlement);
					case ASTRONOMICAL_OBSERVATION -> AstronomicalObservation.getFunctionValue(buildingType, newBuilding, settlement);
					case CONNECTION -> BuildingConnection.getFunctionValue(buildingType, newBuilding, settlement);
					case COMMUNICATION -> Communication.getFunctionValue(buildingType, newBuilding, settlement);
					case COMPUTATION -> Computation.getFunctionValue(buildingType, newBuilding, settlement);
					case COOKING -> Cooking.getFunctionValue(buildingType, newBuilding, settlement);
					case DINING -> Dining.getFunctionValue(buildingType, newBuilding, settlement);
					case EARTH_RETURN -> EarthReturn.getFunctionValue(buildingType, newBuilding, settlement);
					case EVA -> EVA.getFunctionValue(buildingType, newBuilding, settlement);
					case EXERCISE -> Exercise.getFunctionValue(buildingType, newBuilding, settlement);
					case FARMING -> Farming.getFunctionValue(buildingType, newBuilding, settlement);
					case FISHERY -> Fishery.getFunctionValue(buildingType, newBuilding, settlement);
					case FOOD_PRODUCTION -> FoodProduction.getFunctionValue(buildingType, newBuilding, settlement);
					case VEHICLE_MAINTENANCE -> VehicleGarage.getFunctionValue(buildingType, newBuilding, settlement);
					case LIFE_SUPPORT -> LifeSupport.getFunctionValue(buildingType, newBuilding, settlement);
					case LIVING_ACCOMMODATION -> LivingAccommodation.getFunctionValue(buildingType, newBuilding, settlement);
					case MANAGEMENT -> Management.getFunctionValue(buildingType, newBuilding, settlement);
					case MANUFACTURE -> Manufacture.getFunctionValue(buildingType, newBuilding, settlement);
					case MEDICAL_CARE -> MedicalCare.getFunctionValue(buildingType, newBuilding, settlement);
					case POWER_GENERATION -> PowerGeneration.getFunctionValue(buildingType, newBuilding, settlement);
					case POWER_STORAGE -> PowerStorage.getFunctionValue(buildingType, newBuilding, settlement);
					case RECREATION -> Recreation.getFunctionValue(buildingType, newBuilding, settlement);
					case RESEARCH -> Research.getFunctionValue(buildingType, newBuilding, settlement);
					case RESOURCE_PROCESSING -> ResourceProcessing.getFunctionValue(buildingType, newBuilding, settlement);
					case ROBOTIC_STATION -> RoboticStation.getFunctionValue(buildingType, newBuilding, settlement);
					case STORAGE -> Storage.getFunctionValue(buildingType, newBuilding, settlement);
					case THERMAL_GENERATION -> ThermalGeneration.getFunctionValue(buildingType, newBuilding, settlement);
					case WASTE_PROCESSING -> WasteProcessing.getFunctionValue(buildingType, newBuilding, settlement);
					default -> throw new IllegalArgumentException("Do not know how to build Function " + supported);
				};
			}

			// Multiply value.
			result *= 1000D;

			// Subtract power costs per Sol.
			double power = spec.getBasePowerRequirement();
			double powerPerSol = power * MarsTime.HOURS_PER_MILLISOL;
			double powerValue = powerPerSol * settlement.getPowerGrid().getPowerValue();
			result -= powerValue;

			if (result < 0D)
				result = 0D;

			// Check if a building of this type is already in flight
			if (newBuilding) {
				// Check if the building's frame exists at the settlement.
				ConstructionManager constManager = settlement.getConstructionManager();
				boolean hasFrame = constManager.getConstructionSites().stream()
									.anyMatch(s -> s.getBuildingName().equalsIgnoreCase(buildingType));				
				if (hasFrame) {
					// If frame exist the building has zero value.
					result = 0D;
				}
			}

			if (newBuilding)
				vPNewCache.put(buildingType, result);
			else
				vPOldCache.put(buildingType, result);

			return result;
		}
	}

	/**
	 * Gets the value of a building at the settlement.
	 *
	 * @param building the building.
	 * @return building value (VP).
	 */
	public double getBuildingValue(Building building) {
		double result = getBuildingValue(building.getBuildingType(), false);

		// Modify building value by its wear condition.
		double wearCondition = building.getMalfunctionManager().getWearCondition();
		result *= (wearCondition / 100D) * .75D + .25D;

		return result;
	}

	/**
	 * Checks if a proposed building location is open or intersects with existing
	 * buildings or construction sites.
	 *
	 * @param position The position of the new building
	 * @return true if new building location is open.
	 */
	public boolean isBuildingLocationOpen(BoundedObject position) {
		return isBuildingLocationOpen(position, null);
	}

	/**
	 * Checks if a proposed building location is open and without intersecting with 
	 * any existing buildings or construction sites.
	 *
	 * @param position New building position
	 * @param site   the new construction site or null if none.
	 * @return true if new building location is open.
	 */
	public boolean isBuildingLocationOpen(BoundedObject position, ConstructionSite site) {
		boolean goodLocation = true;

		goodLocation = LocalAreaUtil.isObjectCollisionFree(site, position.getWidth(), position.getLength(),
														   position.getXLocation(), position.getYLocation(),
														   position.getFacing(),
														   settlement.getCoordinates());

		return goodLocation;
	}

	/**
	 * Gets the next template ID for a new building in a settlement (but not unique
	 * in a simulation).
	 *
	 * @return template ID (starting from 0).
	 */
	public int getNextTemplateID() {
		return buildings.size();
	}

	/**
	 * Gets an unique name for a new building.
	 *
	 * @return a unique nick name
	 */
	public String getUniqueName(String buildingType) {
		long id = buildings.stream().filter(b -> b.getBuildingType().equals(buildingType)).count() + 1;
		return buildingType + " " + id;
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
			return new double[] {0, 0};
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			double[] combined = node.getSeparatePowerLoadNonLoad();
			double load = combined[0];
			double nonload = combined[1];
			loadTotal += load;
			nonloadTotal += nonload;
		}
		return new double[] {loadTotal, nonloadTotal};
	}
	
	/**
	 * Gets usage percentage from all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double[] getPeakCurrentPercent() {
		double peak = 0;
		double current = 0;
		for (Building b: getComNodes()) {
			Computation node = b.getComputation();
			current += node.getCurrentCU();
			peak += node.getPeakCU();
		}
		
		return new double[] {current, peak};
	}
	
	/**
	 * Gets total entropy of all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double getTotalEntropy() {
		double entropy = 0;
		for (Building b: getComNodes()) {
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
			return new double[]{0, 0};		
		int size = nodeBldgs.size();
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			entropy += node.getEntropy();
		}
		return new double[]{size, entropy};
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
			return new double[]{0, 0};	
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			double ePerCU = node.getEntropyPerCU();
			entropyPerCU += ePerCU;
		}
		return new double[]{size, entropyPerCU};
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
						.filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				bldgs = bldgs.stream()
						.filter(b -> 
							// Condition: the building must be in the same zone as the person
							// Note: the condition below needs to be true 
							b.getZone() == personZone
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}

		}
		else {
			if (anyZones) {
				bldgs = bldgs.stream()
						// Condition: the building doesn't need to be in the same zone as the person						.filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				bldgs = bldgs.stream()
						.filter(b -> 
							// Condition: the building must be in the same zone as the person
							// Note: only buildings in zone 0 will be chosen
							b.getZone() == 0
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}	
		}

		if (bldgs.isEmpty()) {
			return null;
		}
		
		for (Building b: bldgs) {
			Computation node = b.getComputation();
			double entropy = node.getEntropy();
			scores.put(node, entropy);
		}
		
		return RandomUtil.getWeightedRandomObject(scores);
	}
	

	/**
	 * Gets a computing center for having the most free resources by probability.
	 * 
	 * @param need CU(s) per millisol
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Computation getMostFreeComputingNode(double need, int startTime, int endTime) {
		Map<Computation, Double> scores = new HashMap<>();

		for (Building b: getComNodes()) {
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
		for (Building b: bldgs) {
			Research lab = b.getResearch();
			entropy += lab.getEntropy();
		}
		return entropy/size;
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
						.filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				bldgs = bldgs.stream()
						.filter(b -> 
							// Condition: the building must be in the same zone as the person
							// Note: the condition below needs to be true 
							b.getZone() == personZone
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}

		}
		else {
			if (anyZones) {
				bldgs = bldgs.stream()
						// Condition: the building doesn't need to be in the same zone as the person						.filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				bldgs = bldgs.stream()
						.filter(b -> 
							// Condition: the building must be in the same zone as the person
							// Note: only buildings in zone 0 will be chosen
							b.getZone() == 0
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}	
		}

		if (bldgs.isEmpty()) {
			return null;
		}
		
		
		for (Building b: bldgs) {
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
	public static Building getAvailableFunctionTypeBuilding(
			Person person, FunctionType functionType) {
		return getAvailableFunctionBuilding(person, functionType, false);
	}

	/**
	 * Gets an available building with a particular function in a particular zone. 
	 *
	 * @param person the person looking for a facility.
	 * @param functionType
	 * @param anyZones
	 * @return
	 */
	public static Building getAvailableFunctionBuilding(
			Person person, FunctionType functionType, boolean anyZones) {
		
		Set<Building> buildings = null;
		
		if (person.getBuildingLocation() != null) {
			int personZone = person.getBuildingLocation().getZone();
			
			if (anyZones) {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType)
						.stream()
						// Condition: the building doesn't need to be in the same zone as the person
						.filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType)
						.stream()
						.filter(b -> 
							// Condition: the building must be in the same zone as the person
							// Note: the condition below needs to be true 
							b.getZone() == personZone
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}

		}
		else {
			if (anyZones) {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType)
						.stream()
						// Condition: the building doesn't need to be in the same zone as the person						.filter(b -> !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				buildings = person.getSettlement().getBuildingManager().getBuildings(functionType)
						.stream()
						
						.filter(b -> 
						// Condition: the building must be in the same zone as the person
						// Note: only buildings in zone 0 will be chosen
							b.getZone() == 0
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}	
		}
		
		buildings = getLeastCrowdedBuildings(buildings);

		if (!buildings.isEmpty()) {
			return RandomUtil.getWeightedRandomObject(getBestRelationshipBuildings(person, buildings));
		}
		
		return null;
	}
	
		
	/**
	 * Gets an available kitchen for a worker.
	 * DO NOT DELETE. LEAVE HERE AS A REFERENCE
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
				kitchenBuildings = manager.getBuildings(FunctionType.COOKING)
						.stream()
						.filter(b -> b.getZone() == worker.getBuildingLocation().getZone()
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				kitchenBuildings = manager.getBuildings(FunctionType.COOKING)
						.stream()
						.filter(b -> b.getZone() == 0
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());		
			}
			
			if (UnitType.PERSON == worker.getUnitType()) {
				kitchenBuildings = getLeastCrowdedBuildings(kitchenBuildings);

				if (!kitchenBuildings.isEmpty()) {
					Map<Building, Double> selectedBldgs = getBestRelationshipBuildings((Person)worker, kitchenBuildings);
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
	 * Creates a map of buildings with their lists of building connectors attached to
	 * it.
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
	  * Retrieves maintenance parts from all entities associated with this settlement. 
	  */
	public void retrieveAllEntitiesMaintParts() {
        for (Malfunctionable entity : MalfunctionFactory.getAssociatedMalfunctionables(settlement)) {
        	retrieveMaintParts(entity);
        }
	}
	
	 /**
	  * Retrieves maintenance parts from an entity. 
	  */
	public void retrieveMaintParts(Malfunctionable entity) {
      
       Map<MaintenanceScope, Integer> parts = entity.getMalfunctionManager().retrieveMaintenancePartsFromManager();

       if (!parts.isEmpty()) {

           if (!partsMaint.isEmpty()) {
               Map<MaintenanceScope, Integer> partsMaintEntry = partsMaint.get(entity);
               if (partsMaintEntry == null || partsMaintEntry.isEmpty()) {
                   // Post the parts and inject the demand
                   postInjectPartsDemand(entity, parts);
               }
               
               if (partsMaintEntry != null && partsMaintEntry.equals(parts)) {
//						logger.info(entity, 30_000L, "Both are already equal: " + partsMaintEntry + " and " + parts);
               } 
               else {
                   // Post the parts and inject the demand
                   postInjectPartsDemand(entity, parts);
               }   
           } 
           else {
               logger.info(entity, 30_000L, "The maint list was empty. " + parts + " just got posted.");
               // Post the parts and inject the demand
               postInjectPartsDemand(entity, parts);
           }
       }
	}
	
	/**
	 * Posts the part and injects the demand.
	 * 
	 * @param entity
	 * @param parts
	 */
	public void postInjectPartsDemand(Malfunctionable entity, Map<MaintenanceScope, Integer> parts) {
		// Post it
        partsMaint.put(entity, parts);
        for (MaintenanceScope ms : parts.keySet()) {
        	Part part = ms.getPart();
            int num = parts.get(ms);
            Good good = GoodsUtil.getGood(part.getID());
            
            // Inject the demand onto this part
            ((PartGood) good).injectPartsDemand(part, settlement.getGoodsManager(), num);
        }
	}
	
	/**
	 * Updates the needed maintenance parts for a entity.
	 * 
	 * @param requestEntity
	 */
	public void updateMaintenancePartsMap(Malfunctionable requestEntity, Map<MaintenanceScope, Integer> newParts) {
		if (partsMaint.isEmpty()) {
			partsMaint.put(requestEntity, newParts);
			logger.info(requestEntity, 20_000L, "Maintenance parts updated: " 
					+ MalfunctionManager.getPartsString(newParts));	
		}
		else {
			Iterator<Malfunctionable> i = partsMaint.keySet().iterator();
			while (i.hasNext()) {
				Malfunctionable entity = i.next();
				if (requestEntity.equals(entity)) {
					if (newParts == null || newParts.isEmpty()) {
						// This means that this part has been consumed
						i.remove();
						logger.info(entity, 20_000L, "Maintenance parts installed.");
					}
					else {
						// Overwrite with the parts that are still in shortfall
						partsMaint.put(entity, newParts);
						logger.info(entity, 20_000L, "Maintenance parts updated: " 
								+ MalfunctionManager.getPartsString(newParts));
					}
				}
			}
		}
	}

	
//	/**
//	 * Gets the demand of the parts needed for maintenance.
//	 *
//	 * @return map of parts and their number.
//	 */
//	private Map<Integer, Integer> getMaintenancePartsDemand() {
//		if (partsMaint.isEmpty())
//			return new HashMap<>();
//		
//		Map<Integer, Integer> partsList = new HashMap<>();
//        for (Malfunctionable entity : partsMaint.keySet()) {
//            Map<Integer, Integer> partMap = partsMaint.get(entity);
//
//            for (Entry<Integer, Integer> entry : partMap.entrySet()) {
//                Integer part = entry.getKey();
//                int number = entry.getValue();
//                if (!settlement.getItemResourceIDs().contains(part)) {
//                    if (partsList.containsKey(part)) {
//                        number += partsList.get(part).intValue();
//                    }
//                    partsList.put(part, number);
//                }
//            }
//        }
//		
//		return partsList;
//	}
	
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
            for (MaintenanceScope ms: partMap.keySet()) {
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
	 * @param {@link MasterClock}
	 * @param {{@link MarsClock}
	 */
	public static void initializeInstances(SimulationConfig sc, MasterClock c0,
			UnitManager u) {
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
		buildings = null;
		vPNewCache = null;
		vPOldCache = null;
		lastVPUpdateTime = null;
		meteorite = null;
	}
}
