/*
 * Mars Simulation Project
 * BuildingManager.java
 * @date 2023-06-15
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.environment.MeteoriteImpactProperty;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.goods.PartGood;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionFactory;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.Converse;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.connection.BuildingConnector;
import com.mars_sim.core.structure.building.connection.BuildingConnectorManager;
import com.mars_sim.core.structure.building.function.Administration;
import com.mars_sim.core.structure.building.function.AstronomicalObservation;
import com.mars_sim.core.structure.building.function.BuildingConnection;
import com.mars_sim.core.structure.building.function.Communication;
import com.mars_sim.core.structure.building.function.Computation;
import com.mars_sim.core.structure.building.function.EVA;
import com.mars_sim.core.structure.building.function.EarthReturn;
import com.mars_sim.core.structure.building.function.Exercise;
import com.mars_sim.core.structure.building.function.FoodProduction;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.LifeSupport;
import com.mars_sim.core.structure.building.function.LivingAccommodation;
import com.mars_sim.core.structure.building.function.Management;
import com.mars_sim.core.structure.building.function.Manufacture;
import com.mars_sim.core.structure.building.function.MedicalCare;
import com.mars_sim.core.structure.building.function.Recreation;
import com.mars_sim.core.structure.building.function.Research;
import com.mars_sim.core.structure.building.function.ResourceProcessing;
import com.mars_sim.core.structure.building.function.RoboticStation;
import com.mars_sim.core.structure.building.function.Storage;
import com.mars_sim.core.structure.building.function.VehicleGarage;
import com.mars_sim.core.structure.building.function.VehicleMaintenance;
import com.mars_sim.core.structure.building.function.WasteProcessing;
import com.mars_sim.core.structure.building.function.cooking.Cooking;
import com.mars_sim.core.structure.building.function.cooking.Dining;
import com.mars_sim.core.structure.building.function.cooking.PreparingDessert;
import com.mars_sim.core.structure.building.function.farming.AlgaeFarming;
import com.mars_sim.core.structure.building.function.farming.Farming;
import com.mars_sim.core.structure.building.function.farming.Fishery;
import com.mars_sim.core.structure.building.function.task.PrepareDessert;
import com.mars_sim.core.structure.building.utility.heating.ThermalGeneration;
import com.mars_sim.core.structure.building.utility.power.PowerGeneration;
import com.mars_sim.core.structure.building.utility.power.PowerStorage;
import com.mars_sim.core.structure.construction.ConstructionManager;
import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.core.structure.construction.ConstructionStageInfo;
import com.mars_sim.core.structure.construction.ConstructionUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.AlphanumComparator;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Flyer;
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

	private transient Map<String, Double> vPNewCache = new HashMap<>();
	private transient Map<String, Double> vPOldCache = new HashMap<>();
	private transient Map<FunctionType, Set<Building>> buildingFunctionsMap;
	/** The settlement's map of adjacent buildings. */
	private transient Map<Building, Set<Building>> adjacentBuildingMap = new HashMap<>();
	/** The settlement's maintenance parts map. */
	private Map<Malfunctionable, Map<Integer, Integer>> partsMaint = new HashMap<>();
	
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
		if (buildingTemplates != null) {
			for(var template : buildingTemplates) {
				addBuilding(Building.createBuilding(template, settlement), template, false);
			}
		}
	}
	
	/**
	 * Initializes functions map and meteorite instance.
	 */
	public void initializeFunctionsNMeteorite() {

		if (buildingFunctionsMap == null)
			setupBuildingFunctionsMap();
		
		meteorite = new MeteoriteImpactProperty();
	}

	/**
	 * Constructor 2 : Called by MockSettlement for maven test.
	 *
	 * @param settlement        the manager's settlement
	 * @param buildingTemplates the settlement's building templates.
	 * @throws Exception if buildings cannot be constructed.
	 */
	public BuildingManager(Settlement settlement, String name) {
		this.settlement = settlement;
		this.settlementID = settlement.getIdentifier();

		// Construct all buildings in the settlement.
		buildings = new UnitSet<>();
	}


	/**
	 * Sets up the map for the building functions.
	 */
	public void setupBuildingFunctionsMap() {
		buildingFunctionsMap = new EnumMap<>(FunctionType.class); 

		for(Building b : buildings) {
			addBuildingToMap(b);
		}

		// Get a handy shortcut to garages
		garages = buildingFunctionsMap.computeIfAbsent(FunctionType.VEHICLE_MAINTENANCE,
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
			// Remove the building's functions from the settlement.
			oldBuilding.removeFunctionsFromSettlement();

			buildings.remove(oldBuilding);

			// use this only after buildingFunctionsMap has been created
			for (var f : oldBuilding.getFunctions()) {
				removeOneFunctionfromBFMap(oldBuilding, f);
			}

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
		return buildings.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
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
				).collect(Collectors.toList());
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
		
		if (person.getBuildingLocation() != null) {
			return buildings
					.stream()
					.filter(b -> b.hasSpecialty(type)
							&& b.getZone() == person.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		
		return buildings
				.stream()
				.filter(b -> b.hasSpecialty(type)
						&& b.getZone() == 0
						&& !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toSet());		
		
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
				buildings = person.getSettlement().getBuildingManager()
						.getBuildingsWithScienceType(person, sType);
			}

			if (buildings == null || buildings.isEmpty()) {
				buildings = getBuildingsinSameZone(person, FunctionType.RESEARCH);
			}
			if (buildings == null || buildings.isEmpty()) {
				buildings = getBuildingsinSameZone(person, FunctionType.ADMINISTRATION);
			}
			if (buildings == null || buildings.isEmpty()) {
				buildings = getBuildingsinSameZone(person, FunctionType.DINING);
			}
			if (buildings == null || buildings.isEmpty()) {
				buildings = getBuildingsinSameZone(person, FunctionType.LIVING_ACCOMMODATION);
			}

			if (buildings != null && !buildings.isEmpty()) {
				Map<Building, Double> possibleBuildings = BuildingManager.getBestRelationshipBuildings(person,
						buildings);
				b = RandomUtil.getWeightedRandomObject(possibleBuildings);
			}
		}
		return b;
	}
	
	
	/**
	 * Gets a list of non-malfunctioned diners in the same zone.
	 * 
	 * @param person
	 * @return
	 */
	public Set<Building> getDiningBuildings(Person person) {
		if (person.getBuildingLocation() != null) {
			return getBuildingSet()
					.stream()
					.filter(b -> b.hasFunction(FunctionType.DINING)
							&& b.getZone() == person.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		
		return getBuildingSet()
				.stream()
				.filter(b -> b.hasFunction(FunctionType.DINING)
						&& b.getZone() == 0
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
			
			Set<Building> list1 = list0;

            if (canChat)
				// Choose between the most crowded or the least crowded dining hall
				list1 = BuildingManager.getChattyBuildings(list1);
			else
				list1 = BuildingManager.getLeastCrowdedBuildings(list1);

			if (!list1.isEmpty()) {
				Map<Building, Double> probs = BuildingManager.getBestRelationshipBuildings(person,
						list1);
				b = RandomUtil.getWeightedRandomObject(probs);
			}
		}
		
		return b;
	}
	
	/**
	 * Gets a list of non-malfunctioned buildings with a particular function type.
	 *
	 * @param person
	 * @param functionType
	 * @return
	 */
	private static Set<Building> getBuildingsinSameZone(Person person, FunctionType functionType) {		
		if (person.getBuildingLocation() != null) {
			return person.getSettlement().getBuildingManager().getBuildingSet()
					.stream()
					.filter(b -> b.hasFunction(functionType)
							&& b.getZone() == person.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		
		return person.getSettlement().getBuildingManager().getBuildingSet()
				.stream()
				.filter(b -> b.hasFunction(functionType)
						&& b.getZone() == 0
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
				.collect(Collectors.toList());
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
				.collect(Collectors.toList());
	}
	
	
	
	/**
	 * Gets the buildings in the settlement with a given building category.
	 *
	 * @param category the building type.
	 * @return list of buildings.
	 */
	public List<Building> getBuildingsOfSameCategory(BuildingCategory category) {
		// Called by Resupply.java and BuildingConstructionMission.java
		// for putting new building next to the same building "type".
		return buildings.stream()
				.filter(b -> b.getCategory() == category)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the buildings in the settlement with a given building category and in zone 0.
	 *
	 * @param category the building type.
	 * @return list of buildings.
	 */
	public List<Building> getBuildingsOfSameCategoryNZone0(BuildingCategory category) {
		// Called by Resupply.java and BuildingConstructionMission.java
		// for putting new building next to the same building "type".
		return buildings.stream()
				.filter(b -> b.getCategory() == category
						&& b.getZone() == 0)
				.collect(Collectors.toList());
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
				.collect(Collectors.toList());
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
//				.skip(getRandomInt(collection.size()))
                .findFirst();

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

		if (pulse.isNewIntMillisol()) {
			// Check if there are any maintenance parts to be submitted
			retrieveMaintPartsFromMalfunctionMgrs();
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
	 * Adds a person to a random medical building within a settlement.
	 *
	 * @param unit       the person/robot to add.
	 * @param s the settlement to find a building.
	 * @throws BuildingException if person/robot cannot be added to any building.
	 */
	public static void addToMedicalBuilding(Person p, Settlement s) {

		Building building = s.getBuildingManager()
				.getABuilding(FunctionType.MEDICAL_CARE, FunctionType.LIFE_SUPPORT);

		if (building != null) {
			addPersonToActivitySpot(p, building, FunctionType.MEDICAL_CARE);
			logger.info(p, 2000, "Brought to " + building.getName() + " for possible treatment.");
		}

		else {
			logger.log(p, Level.WARNING, 2000,	"No medical facility available for "
							+ p.getName() + ". Go to a random building.");
			addPersonToRandomBuilding(p, s);
		}
	}

	/**
	 * Adds a person to a random habitable building within a settlement.
	 * Note: excluding the astronomical observation building
	 *
	 * @param unit       the person to add.
	 * @param s the settlement to find a building.
	 * @throws BuildingException if person cannot be added to any building.
	 */
	public static void addPersonToRandomBuilding(Person person, Settlement s) {
		
		// Go to the default zone 0 only
		Set<Building> bldgSet = person.getSettlement().getBuildingManager()
					.getBuildingSet(FunctionType.LIFE_SUPPORT)
					.stream()
					.filter(b -> b.getZone() == 0
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());

		if (bldgSet.isEmpty()) {
			logger.warning(person, "No habitable buildings available in zone 0.");
			return;
		}
		
		boolean found = false;
		
		for (Building building: bldgSet) {
			if (!found && building != null
					&& building.getCategory() != BuildingCategory.CONNECTION
					&& building.getCategory() != BuildingCategory.EVA) {
				// Add the person to a building activity spot
				found = addPersonToActivitySpot(person, building, null);
			}
		}

		if (!found) {
			logger.warning(person, "No habitable buildings with empty activity spot available in zone 0.");
		}
	}

	/**
	 * Adds a person to a random habitable building within a settlement.
	 * Note: excluding the astronomical observation building
	 *
	 * @param unit       the person to add.
	 * @param s the settlement to find a building.
	 * @throws BuildingException if person cannot be added to any building.
	 */
	public static void landOnRandomBuilding(Person person, Settlement s) {
		
		// Go to the default zone 0 only
		Set<Building> bldgSet = person.getSettlement().getBuildingManager()
					.getBuildingSet(FunctionType.LIFE_SUPPORT)
					.stream()
					.filter(b -> b.getZone() == 0
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());

		if (bldgSet.isEmpty()) {
			logger.warning(person, "No habitable buildings available in zone 0.");
			return;
		}
				
		for (Building building: bldgSet) {
			if (building.getCategory() != BuildingCategory.CONNECTION
					&& building.getCategory() != BuildingCategory.EVA) {
				
				// Add the person to the life support
				LifeSupport lifeSupport = building.getLifeSupport();

				if (!lifeSupport.containsOccupant(person)) {
					lifeSupport.addPerson(person);

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
					&& bldg.getFunction(functionType).hasEmptyActivitySpot()) {
				BuildingCategory category = bldg.getCategory();
				// Do not add robot to hallway and tunnel
				if (category != BuildingCategory.CONNECTION) {
					destination = bldg;
					canAdd = addRobotToActivitySpot(robot, destination, functionType);
				}
			}
		}

		functionBuildings = manager.getBuildingSet(FunctionType.ROBOTIC_STATION);
		for (Building bldg : functionBuildings) {
			if (!canAdd && bldg.getZone() == 0
					&& bldg.getFunction(FunctionType.ROBOTIC_STATION).hasEmptyActivitySpot()) {
				destination = bldg;
				canAdd = addRobotToActivitySpot(robot, destination, FunctionType.ROBOTIC_STATION);
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
						canAdd = addRobotToActivitySpot(robot, destination, function.getFunctionType());
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
		
			if (vehicle instanceof Drone d) {
				
				if (garage.containsFlyer(d)) {
					logger.info(vehicle, 60_000,
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
							&& garage.addFlyer((Flyer)vehicle)) {

						logger.info(vehicle, 60_000,
 							   "Just stowed inside " + garageBuilding.getName() + ".");
						return garageBuilding;
					}
				}
			}
			else {
				if (garage.containsVehicle(vehicle)) {
					logger.info(vehicle, 60_000,
							"Already inside " + garageBuilding.getName() + ".");

					return garageBuilding;
				}
				else { 
					boolean vacated = false;
					
					if (garage.getAvailableCapacity() == 0) {
						// Try removing a non-reserved vehicle inside a garage		
						for (Vehicle v: garage.getVehicles()) {
							if (!vacated && !v.isReserved() && v.getMission() != null) {
								if (garage.removeVehicle(v, true)) {
									vacated = true;
								}
							}
						}
					}
					
					if ((garage.getAvailableCapacity() > 0)
						&& garage.addVehicle(vehicle)) {

						logger.info(vehicle, 60_000,
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
		
		if (vehicle instanceof Flyer flyer) {
			if (garage.getVehicleMaintenance().removeFlyer(flyer)) {
				return true;
			}
		}
		else if (garage.getVehicleMaintenance().removeVehicle(vehicle, true)) {
			return true;
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
		if (isInGarage(vehicle)) {
			// Vehicle already on Garage
			vehicle.setPrimaryStatus(StatusType.GARAGED);

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
				
				if (vehicle instanceof Drone d
					&& garage.containsFlyer(d)) {
					return true;
				}
				else if (garage.containsVehicle(vehicle)) {
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
				LifeSupport lifeSupport = building.getLifeSupport();

				if (!lifeSupport.containsOccupant(person)) {
					lifeSupport.addPerson(person);

					person.setCurrentBuilding(building);
				}
			}

			else {
				Robot robot = (Robot) worker;
				RoboticStation roboticStation = building.getRoboticStation();

				if (roboticStation != null && !roboticStation.containsRobotOccupant(robot)) {
					roboticStation.addRobot(robot);
					
					robot.setCurrentBuilding(building);
				}
			}
		}

		else
			logger.severe(worker, 2000, "The building is null.");
	}

	/**
	 * Adds a worker to the building if possible.
	 *
	 * @param worker   the worker to add.
	 * @param building the building to add.
	 */
	public static void addToBuilding(Worker worker, Building building) {
		
		if (worker instanceof Person person)
			addPersonToActivitySpot(person, building, null);
		else if (worker instanceof Robot robot)
			addRobotToActivitySpot(robot, building, null);
	}
	
	/**
	 * Adds the person to an activity spot in a building.
	 *
	 * @param person   the person to add.
	 * @param building the building to add the person to.
	 * @return
	 */
	public static boolean addPersonToActivitySpot(Person person, Building building, FunctionType functionType) {
		boolean result = false;

		try {
			LifeSupport lifeSupport = building.getLifeSupport();
			Function f = lifeSupport;
			
			LocalPosition loc = null;
			
			if (functionType != null)  {
				f = building.getFunction(functionType);
				if (f != null)
					loc = f.getAvailableActivitySpot();	
			}
			else {
				// Find an empty spot in life support
				loc = lifeSupport.getAvailableActivitySpot();
			}

			// Add the person to this building, even if an activity spot is not available				
			if (lifeSupport != null && !lifeSupport.containsOccupant(person)) {
				lifeSupport.addPerson(person);
			}
			
			if (loc == null) {
				f = building.getEmptyActivitySpotFunction();
				if (f != null)
					loc = f.getAvailableActivitySpot();	
			}
			
			if (loc != null) {
				// Put the person there
				person.setPosition(loc);
				// Set the building
				person.setCurrentBuilding(building);
				
				// Claim this activity spot
				boolean canClaim = f.claimActivitySpot(loc, person);
				
				if (!canClaim)
					result = false;
				else
					result = true;
			}

		} catch (Exception e) {
			logger.severe(person, 2000, "Could not be added to " + building.getName(), e);
		}
		
		return result;
	}

	/**
	 * Adds the robot to aN activity spot in a building.
	 *
	 * @param robot   the robot to add.
	 * @param building the building to add the robot to.
	 * @param functionType
	 * @return
	 */
	public static boolean addRobotToActivitySpot(Robot robot, Building building, FunctionType functionType) {
		boolean result = false;

		try {
			Function f = null;
			LocalPosition loc = null;
			
			// Case 1: Gets an empty pos from functionType
			if (functionType != null)  {
				var specificF = building.getFunction(functionType);
				if (specificF == null) {
					logger.warning(robot, "No " + functionType.getName() + " in " + building.getName() + ".");
				}
				else {
					f = specificF;
					// Find an empty spot in this function
					loc = f.getAvailableActivitySpot();
				}
			}
			
			// Case 2: Gets an empty pos from building's robotic station
			if (loc == null) {
				RoboticStation roboticStation = building.getRoboticStation();
				if (roboticStation == null) {
					logger.warning(robot, "No robotic function in " + building.getName() + ".");
				}
				else {	
					f = roboticStation;
					// Find an empty spot in robotic station
					loc = roboticStation.getAvailableActivitySpot();
					
					// Add the robot to the station
					if (loc != null && !roboticStation.containsRobotOccupant(robot)) {
						roboticStation.addRobot(robot);
					}
				}
			}
			
			// Case 3: Gets an empty pos from any building's function
			if (loc == null) {
				f = building.getEmptyActivitySpotFunction();
				if (f == null) {
					logger.warning(robot, "No empty activity spot function in " + building.getName() + ".");
					return false;
				}
				loc = f.getAvailableActivitySpot();	
			}

			if (loc != null) {		
				// Put the robot there
				robot.setPosition(loc);
				// Set the building
				robot.setCurrentBuilding(building);
				// Claim this activity spot
				boolean canClaim = f.claimActivitySpot(loc, robot);

				if (!canClaim)
					result = false;
				else
					result = true;
			}	
		
		} catch (Exception e) {
			logger.severe(robot, 2000, "Could not be added to " + building.getName(), e);
		}
		
		return result;
	}
	
	/**
	 * Removes the person from a building if possible.
	 *
	 * @param person   the person to remove.
	 * @param building the building to remove the person from.
	 */
	public static void removePersonFromBuilding(Person person, Building building) {
		if (building != null) {
			try {
				LifeSupport lifeSupport = building.getLifeSupport();

				if (lifeSupport.containsOccupant(person)) {
					lifeSupport.removePerson(person);

					person.setCurrentBuilding(null);
				}
			} catch (Exception e) {
				logger.severe(person, 2000, "Could not be removed from " + building.getName(), e);

			}
		} else {
			logger.severe(person, 2000, "Building is null.");
		}
	}

	/**
	 * Removes the robot from a building if possible.
	 *
	 * @param robot   the robot to remove.
	 * @param building the building to remove the robot from.
	 */
	public static void removeRobotFromBuilding(Robot robot, Building building) {
		if (building != null) {
			try {
				RoboticStation roboticStation = building.getRoboticStation();

				if (roboticStation != null && roboticStation.containsRobotOccupant(robot)) {
					roboticStation.removeRobot(robot);
				}
				
				robot.setCurrentBuilding(null);
			} catch (Exception e) {
				logger.severe(robot, 2000, "Could not be removed from " + building.getName(), e);
			}
		} else {
			logger.severe(robot, 2000, "building is null.");
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
					case PREPARING_DESSERT -> PreparingDessert.getFunctionValue(buildingType, newBuilding, settlement);
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

			// Check if a new non-constructable building has a frame that already exists at
			// the settlement.
			if (newBuilding) {
				ConstructionStageInfo buildingConstInfo = ConstructionUtil.getConstructionStageInfo(buildingType);
				if (buildingConstInfo != null) {
					ConstructionStageInfo frameConstInfo = ConstructionUtil.getPrerequisiteStage(buildingConstInfo);
					if (frameConstInfo != null) {
						// Check if frame is not constructable.
						if (!frameConstInfo.isConstructable()) {
							// Check if the building's frame exists at the settlement.
							if (!hasBuildingFrame(frameConstInfo.getName())) {
								// If frame doesn't exist and isn't constructable, the building has zero value.
								result = 0D;
							}
						}
					}
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
	 * Checks if a building frame exists at the settlement. Either with an existing
	 * building or at a construction site.
	 *
	 * @param frameName the frame's name.
	 * @return true if frame exists.
	 */
	private boolean hasBuildingFrame(String frameName) {
		boolean result = false;

		// Check if any existing buildings have this frame.
		for (Building building : buildings) {
			ConstructionStageInfo buildingStageInfo = ConstructionUtil
					.getConstructionStageInfo(building.getBuildingType());
			if (buildingStageInfo != null) {
				ConstructionStageInfo frameStageInfo = ConstructionUtil.getPrerequisiteStage(buildingStageInfo);
				if (frameStageInfo != null) {
					if (frameStageInfo.getName().equals(frameName)) {
						result = true;
						break;
					}
				}
			}
		}

		// Check if any construction projects have this frame.
		if (!result) {
			ConstructionStageInfo frameStageInfo = ConstructionUtil.getConstructionStageInfo(frameName);
			if (frameStageInfo != null) {
				ConstructionManager constManager = settlement.getConstructionManager();
				Iterator<ConstructionSite> j = constManager.getConstructionSites().iterator();
				while (j.hasNext()) {
					ConstructionSite site = j.next();
					if (site.hasStage(frameStageInfo)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;
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
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
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
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			current += node.getCurrentCU();
			peak += node.getPeakCU();
		}
		
		return new double[] {current, peak};
	}
	
	/**
	 * Gets peak available total CUs from all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double getPeakTotalComputing() {
		double peakTotal = 0;
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
		if (nodeBldgs.isEmpty())
			return 0;
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			peakTotal += node.getPeakCU();
		}
		return peakTotal;
	}
	
	/**
	 * Gets the sum of all computing capacity in a settlement.
	 * 
	 * @return amount in CUs
	 */
	public double getTotalCapacityCUsComputing() {
		double units = 0;
		for (Building b: getBuildingSet(FunctionType.COMPUTATION)) {
			Computation node = b.getComputation();
			units += node.getCurrentCU();
		}
		return units;
	}
	
	/**
	 * Gets total entropy of all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double getTotalEntropy() {
		double entropy = 0;
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
		if (nodeBldgs.isEmpty())
			return 0;
		for (Building b: nodeBldgs) {
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
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
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
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
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
	 * Gets average minimum entropy of all computing nodes in a settlement.
	 * 
	 * @return
	 */
	public double getAverageMinimumEntropy() {
		double entropy = 0;
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
		if (nodeBldgs.isEmpty())
			return 0;
		int size = nodeBldgs.size();
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			entropy += node.getMinEntropy();
		}
		return entropy/size;
	}
	
	/**
	 * Gets a computing node for having the worst entropy.
	 * 
	 * @return
	 */
	public Computation getWorstEntropyComputingNode() {
		double highestEntropy = Integer.MIN_VALUE;
		Computation worstNode = null;
		
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
		
		if (nodeBldgs.isEmpty())
			return null;
		
		for (Building b: nodeBldgs) {
			Computation cNode = b.getComputation();
			double entropy = cNode.getEntropy();
			if (highestEntropy < entropy) {
				highestEntropy = entropy;
				worstNode = cNode;
			}
		}
				
		return worstNode;
	}
	
	/**
	 * Gets a computing node for having the worst entropy by probability.
	 * 
	 * @param person
	 * @return
	 */
	public Computation getWorstEntropyComputingNodeByProbability(Person person) {
		Map<Computation, Double> scores = new HashMap<>();
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
		if (person.getBuildingLocation() != null) {
			nodeBldgs = nodeBldgs
					.stream()
					.filter(b -> b.getZone() == person.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		else {
			nodeBldgs = nodeBldgs
					.stream()
					.filter(b -> b.getZone() == 0
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());		
		}
				
		if (nodeBldgs.isEmpty())
			return null;
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			double entropy = node.getEntropy();
			scores.put(node, entropy);
		}

		if (scores.isEmpty())
			return null;

		// Note: Use probability selection	
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
		Set<Building> nodeBldgs = getBuildingSet(FunctionType.COMPUTATION);
		if (nodeBldgs.isEmpty())
			return null;
		for (Building b: nodeBldgs) {
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
	 * @return
	 */
	public Research getWorstEntropyLabByProbability(Person person) {
		Map<Research, Double> scores = new HashMap<>();
		Set<Building> bldgs = getBuildingSet(FunctionType.RESEARCH);
		if (person.getBuildingLocation() != null) {
			bldgs = bldgs
					.stream()
					.filter(b -> b.getZone() == person.getBuildingLocation().getZone()
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());
		}
		else {
			bldgs = bldgs
					.stream()
					.filter(b -> b.getZone() == 0
							&& !b.getMalfunctionManager().hasMalfunction())
					.collect(Collectors.toSet());		
		}
				
		if (bldgs.isEmpty())
			return null;
		for (Building b: bldgs) {
			Research lab = b.getResearch();
			double entropy = lab.getEntropy();
			scores.put(lab, entropy);
		}

		if (scores.isEmpty())
			return null;

		// Note: Use probability selection	
		return RandomUtil.getWeightedRandomObject(scores);
	}
	
	/**
	 * Gets a list of farm buildings needing work from a list of buildings with the
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
		return getAvailableFunctionBuilding(person, functionType, true);
	}

	/**
	 * Gets an available building with a particular function in a particular zone. 
	 *
	 * @param person the person looking for a facility.
	 * @return an available space or null if none found.
	 */
	public static Building getAvailableFunctionBuilding(
			Person person, FunctionType functionType, boolean sameZone) {
		
		// If person is in a settlement, try to find a building of functionType
		if (person.isInSettlement()) {
			
			Set<Building> bldgs0 = null;
					
			if (sameZone && person.getBuildingLocation() != null) {
				bldgs0 = person.getSettlement().getBuildingManager().getBuildings(functionType)
						.stream()
						.filter(b -> b.getZone() == person.getBuildingLocation().getZone()
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				bldgs0 = person.getSettlement().getBuildingManager().getBuildings(functionType)
						.stream()
						.filter(b -> b.getZone() == 0
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());		
			}
			
			Set<Building> bldgs1 = getLeastCrowdedBuildings(bldgs0);

			if (!bldgs1.isEmpty()) {
				return RandomUtil.getWeightedRandomObject(getBestRelationshipBuildings(person, bldgs1));
			}
			
			return RandomUtil.getWeightedRandomObject(getBestRelationshipBuildings(person, bldgs0));
		}
		
		return null;
	}
	
		
	/**
	 * Gets an available kitchen for a worker.
	 * 
	 * @param worker
	 * @param functionType
	 * @return
	 */
	public static Building getAvailableKitchen(Worker worker, FunctionType functionType) {
		Building result = null;		
		
		if (worker.isInSettlement()) {
			BuildingManager manager = worker.getSettlement().getBuildingManager();
			
			Set<Building> kitchenBuildings = null;
					
			if (worker.getBuildingLocation() != null) {
				kitchenBuildings = manager.getBuildings(functionType)
						.stream()
						.filter(b -> b.getZone() == worker.getBuildingLocation().getZone()
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());
			}
			else {
				kitchenBuildings = manager.getBuildings(functionType)
						.stream()
						.filter(b -> b.getZone() == 0
								&& !b.getMalfunctionManager().hasMalfunction())
						.collect(Collectors.toSet());		
			}
			
			kitchenBuildings = PrepareDessert.getKitchensNeedingCooks(kitchenBuildings);
			
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
	private void retrieveMaintPartsFromMalfunctionMgrs() {
		Iterator<Malfunctionable> i = MalfunctionFactory.getAssociatedMalfunctionables(settlement).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next(); 		
			Map<Integer, Integer> parts = entity.getMalfunctionManager().retrieveMaintenancePartsFromManager();
			
			if (!parts.isEmpty()) {
			
				if (!partsMaint.isEmpty()) {
					Map<Integer, Integer> partsMaintEntry = partsMaint.get(entity);
					if (partsMaintEntry == null || partsMaintEntry.isEmpty()) {
						// Post it
						partsMaint.put(entity, parts);
						for (int id: parts.keySet()) {							
							int num = parts.get(id);		
							Good good = GoodsUtil.getGood(id);						
							Part part = ItemResourceUtil.findItemResource(id);					
							// Inject the demand onto this part
							((PartGood)good).injectPartsDemand(part, settlement.getGoodsManager(), num);
						}
					}
					if (partsMaintEntry != null && partsMaintEntry.equals(parts)) {
//						logger.info(entity, 30_000L, "Both are equal : " + partsMaintEntry + " and " + parts);
					}
					else {
						// Post it
						partsMaint.put(entity, parts);
						for (int id: parts.keySet()) {							
							int num = parts.get(id);		
							Good good = GoodsUtil.getGood(id);						
							Part part = ItemResourceUtil.findItemResource(id);					
							// Inject the demand onto this part
							((PartGood)good).injectPartsDemand(part, settlement.getGoodsManager(), num);
						}
					}
				}
				else {
					// Post it
					partsMaint.put(entity, parts);
					logger.info(parts + " was posted in empty partsMaint.");
					for (int id: parts.keySet()) {							
						int num = parts.get(id);		
						Good good = GoodsUtil.getGood(id);						
						Part part = ItemResourceUtil.findItemResource(id);					
						// Inject the demand onto this part
						((PartGood)good).injectPartsDemand(part, settlement.getGoodsManager(), num);
					}
				}
			}
		}			
	}
	
	/**
	 * Updates the needed maintenance parts for a entity.
	 * 
	 * @param requestEntity
	 */
	public void updateMaintenancePartsMap(Malfunctionable requestEntity, Map<Integer, Integer> newParts) {
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

	
	/**
	 * Gets the demand of the parts needed for maintenance.
	 *
	 * @return map of parts and their number.
	 */
	private Map<Integer, Integer> getMaintenancePartsDemand() {
		if (partsMaint.isEmpty())
			return new HashMap<>();
		Map<Integer, Integer> partsList = new HashMap<>();
		Iterator<Malfunctionable> i = partsMaint.keySet().iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();
			Map<Integer, Integer> partMap = partsMaint.get(entity);
			
			for (Entry<Integer, Integer> entry: partMap.entrySet()) {
				Integer part = entry.getKey();
				int number = entry.getValue();
				if (!settlement.getItemResourceIDs().contains(part)) {
					if (partsList.containsKey(part)) {
						number += partsList.get(part).intValue();
					}
					partsList.put(part, number);
				}
			}
		}
		
		return partsList;
	}
	
	/**
	 * Gets the number of maintenance parts from a particular settlement.
	 * 
	 * @param settlement
	 * @param part
	 */
	public int getMaintenanceDemand(Part part) {
		int numRequest = 0;
		Map<Integer, Integer> partMap = getMaintenancePartsDemand();
		
		for (Entry<Integer, Integer> entry: partMap.entrySet()) {
			int p = entry.getKey();
			int number = entry.getValue();
			if (part.getID() == p) {
				numRequest += number;
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
	 * Gets a set of garages for the settlement.
	 *
	 * @return
	 */
	public Set<Building> getGarages() {
		return garages;
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

	static BuildingConfig getBuildingConfig() {
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
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}
		buildings = null;
		vPNewCache = null;
		vPOldCache = null;
		lastVPUpdateTime = null;
		meteorite = null;
	}
}
