/**
 * Mars Simulation Project
 * BuildingManager.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.AlphanumComparator;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.mars.Meteorite;
import org.mars_sim.msp.core.mars.MeteoriteModule;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.connection.InsideBuildingPath;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.BuildingConnection;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Communication;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.EarthReturn;
import org.mars_sim.msp.core.structure.building.function.Exercise;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.Management;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.structure.building.function.Recreation;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.Dining;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The BuildingManager manages the settlement's buildings.
 */
public class BuildingManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default serial id. */
	private static Logger logger = Logger.getLogger(BuildingManager.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	// Data members
	private int numBuildings;
	private int solCache = 0;
	private int millisolCache = -5;
	private double probabilityOfImpactPerSQMPerSol;
	private double wallPenetrationThicknessAL;

	private Settlement settlement;
	private MarsClock lastBuildingValuesUpdateTime;

	private Resupply resupply;
	private Meteorite meteorite;

	private static HistoricalEventManager eventManager;
	private static MarsClock marsClock;
	private static MasterClock masterClock;
	private static BuildingConfig buildingConfig;
	private static RelationshipManager relationshipManager;

	private List<Building> buildings, farmsNeedingWorkCache, buildingsNickNames;
	private Map<String, Double> buildingValuesNewCache;
	private Map<String, Double> buildingValuesOldCache;
	private Map<FunctionType, List<Building>> buildingFunctionsMap;
	private Map<String, Integer> buildingTypeIDMap;

	/**
	 * Constructor 1 : construct buildings from settlement config template. Called
	 * by Settlement
	 * 
	 * @param settlement the manager's settlement.
	 * @throws Exception if buildings cannot be constructed.
	 */
	public BuildingManager(Settlement settlement) {
		this(settlement, SimulationConfig.instance().getSettlementConfiguration()
				.getSettlementTemplate(settlement.getTemplate()).getBuildingTemplates());

	}

	/**
	 * Constructor 2 : construct buildings from name list. Called by constructor 1
	 * and by MockSettlement
	 * 
	 * @param settlement        the manager's settlement
	 * @param buildingTemplates the settlement's building templates.
	 * @throws Exception if buildings cannot be constructed.
	 */
	public BuildingManager(Settlement settlement, List<BuildingTemplate> buildingTemplates) {
		this.settlement = settlement;

		masterClock = Simulation.instance().getMasterClock();
		marsClock = masterClock.getMarsClock();

		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		eventManager = Simulation.instance().getEventManager();
		relationshipManager = Simulation.instance().getRelationshipManager();

		// Construct all buildings in the settlement.
		buildings = new ArrayList<Building>();
		if (buildingTemplates != null) {
			Iterator<BuildingTemplate> i = buildingTemplates.iterator();
			while (i.hasNext()) {
				BuildingTemplate template = i.next();
				addBuilding(template, false);
			}
		}

		buildings.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());

		buildingTypeIDMap = new HashMap<>();
		for (Building b : buildings) {
			createBuildingTypeIDMap(b);
		}

		setupBuildingFunctionsMap();

		numBuildings = getNumBuilding();

		// Initialize building value caches.
		buildingValuesNewCache = new HashMap<String, Double>();
		buildingValuesOldCache = new HashMap<String, Double>();

		// Make use of Guice for Meteorite
		Injector injector = Guice.createInjector(new MeteoriteModule());
		meteorite = injector.getInstance(Meteorite.class);

	}

	/**
	 * Constructor 3 : Called by MockSettlement for maven test
	 * 
	 * @param settlement        the manager's settlement
	 * @param buildingTemplates the settlement's building templates.
	 * @throws Exception if buildings cannot be constructed.
	 */
	public BuildingManager(Settlement settlement, boolean isTest) {
		this.settlement = settlement;

		if (isTest)
			logger.info("Loading BuildingManager's constructor 2 for " + settlement.getName() + " on "
					+ Thread.currentThread().getName());

		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		relationshipManager = Simulation.instance().getRelationshipManager();

		// Construct all buildings in the settlement.
		buildings = new ArrayList<Building>();

		// setupBuildingFunctionsMap();

		buildingFunctionsMap = new ConcurrentHashMap<FunctionType, List<Building>>(); // HashMap<>();

		// Initialize building value caches.
		buildingValuesNewCache = new HashMap<String, Double>();
		buildingValuesOldCache = new HashMap<String, Double>();
	}

	public void setupBuildingFunctionsMap() {
		buildingFunctionsMap = new ConcurrentHashMap<FunctionType, List<Building>>(); // HashMap<>();
		List<FunctionType> functions = buildingConfig.getBuildingFunctions();
		for (FunctionType f : functions) {
			List<Building> l = new ArrayList<Building>();
			for (Building b : buildings) {
				if (b.hasFunction(f))
					l.add(b);
			}
			buildingFunctionsMap.put(f, l);
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
			settlement.getBuildingConnectorManager().removeAllConnectionsToBuilding(oldBuilding);
			// Remove the building's functions from the settlement.
			oldBuilding.removeFunctionsFromSettlement();

			buildings.remove(oldBuilding);

			// Call to remove all references of this building in all functions
			removeAllFunctionsfromBFMap(oldBuilding);

			settlement.fireUnitUpdate(UnitEventType.REMOVE_BUILDING_EVENT, oldBuilding);
		}
	}

	/**
	 * Removes all references of this building in all functions in
	 * buildingFunctionsMap
	 * 
	 * @param oldBuilding
	 */
	public void removeAllFunctionsfromBFMap(Building oldBuilding) {
		if (buildingFunctionsMap != null) {
			// use this only after buildingFunctionsMap has been created
			for (FunctionType f : buildingConfig.getBuildingFunctions()) {
				// if this building has this function
				if (oldBuilding.hasFunction(f)) {
					List<Building> list = buildingFunctionsMap.get(f);
					if (!list.contains(oldBuilding))
						list.remove(oldBuilding);
				}
			}
		}
	}

	/**
	 * Removes the reference of this building for a functions in
	 * buildingFunctionsMap
	 * 
	 * @param a building
	 * @param a function
	 */
	public void removeOneFunctionfromBFMap(Building b, Function f) {
		if (buildingFunctionsMap != null) {
			FunctionType ft = f.getFunctionType();
			List<Building> list = buildingFunctionsMap.get(ft);
			if (!list.contains(b))
				list.remove(b);
		}
	}

	/**
	 * Add references of this building in all functions in buildingFunctionsMap
	 * 
	 * @param oldBuilding
	 */
	public void addAllFunctionstoBFMap(Building newBuilding) {
		if (buildingFunctionsMap != null) {
			// use this only after buildingFunctionsMap has been created
			for (FunctionType f : buildingConfig.getBuildingFunctions()) {
				// if this building has this function
				if (newBuilding.hasFunction(f)) {
					List<Building> list = null;
					if (buildingFunctionsMap.containsKey(f)) {
						list = buildingFunctionsMap.get(f);
						if (!list.contains(newBuilding))
							list.add(newBuilding);
					} else {
						list = new ArrayList<>();
						list.add(newBuilding);
						buildingFunctionsMap.put(f, list);
					}
				}
			}
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

			buildings.add(newBuilding);

			// Insert this new building into buildingFunctionsMap
			addAllFunctionstoBFMap(newBuilding);

			settlement.fireUnitUpdate(UnitEventType.ADD_BUILDING_EVENT, newBuilding);
			// Create new building connections if needed.
			if (createBuildingConnections) {
				settlement.getBuildingConnectorManager().createBuildingConnections(newBuilding);
			}
		}
	}

	/**
	 * Adds a building with a template to the settlement.
	 * 
	 * @param template                  the building template.
	 * @param createBuildingConnections true if automatically create building
	 *                                  connections.
	 */
	public void addBuilding(BuildingTemplate template, boolean createBuildingConnections) {
		Building newBuilding = new Building(template, this);
		addBuilding(newBuilding, createBuildingConnections);
	}

	/**
	 * Adds a building with a template to the settlement.
	 * 
	 * @param template                  the building template.
	 * @param createBuildingConnections true if automatically create building
	 *                                  connections.
	 * @return newBuilding
	 */
	public Building prepareToAddBuilding(BuildingTemplate template, Resupply resupply,
			boolean createBuildingConnections) {
		// Add prepareToAddBuilding-- called by confirmBuildingLocation() in
		// Resupply.java
		Building newBuilding = new Building(template, this);
		addBuilding(newBuilding, createBuildingConnections);
		return newBuilding;
	}

	public Resupply getResupply() {
		return resupply;
	}

	public void addResupply(Resupply resupply) {
		this.resupply = resupply;
	}

	/**
	 * Gets a copy of settlement's collection of buildings.
	 * 
	 * @return collection of buildings
	 */
	public List<Building> getACopyOfBuildings() {
		return new ArrayList<Building>(buildings);
	}

	/**
	 * Gets a collection of buildings.
	 * 
	 * @return collection of buildings
	 */
	public List<Building> getBuildings() {
		return buildings;
	}

	/**
	 * Gets a collection of buildings.
	 * 
	 * @return collection of buildings
	 */
	public List<Building> getSortedBuildings() {
		return buildings.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
	}

	/**
	 * Gets the settlement's collection of buildings (in their nicknames)
	 * 
	 * @return collection of buildings (in their nicknames)
	 */
	public List<Building> getBuildingsNickNames() {
		return new ArrayList<Building>(buildingsNickNames);
	}

	/**
	 * Gets a list of settlement's buildings with Robotic Station function
	 * 
	 * @return list of buildings
	 */
	public List<Building> getBuildingsWithRoboticStation() {
		return getBuildings(FunctionType.ROBOTIC_STATION);
		// Using JavaFX/8 Stream
//		List<Building> buildings = getACopyOfBuildings();
//    	List<Building> buildingsWithRoboticStation =
//            	buildings.stream()
//        		//buildings.parallelStream() // parallelStream makes it 3x slower than sequential stream
//    	        .filter(s -> buildingConfig.hasRoboticStation(s.getBuildingType()))
//    	        .collect(Collectors.toList());
//
//    	return buildingsWithRoboticStation;
	}

	/**
	 * Gets a list of settlement's buildings with Life Supportfunction
	 * 
	 * @return list of buildings
	 */
	public List<Building> getBuildingsWithLifeSupport() {
		return getBuildings(FunctionType.LIFE_SUPPORT);
		// Using JavaFX/8 Stream
//		List<Building> buildings = getACopyOfBuildings();
//    	List<Building> buildingsWithLifeSupport =
//            	buildings.stream()
//        		//buildings.parallelStream() // parallelStream makes it 3x slower than sequential stream
//    	        .filter(s -> buildingConfig.hasLifeSupport(s.getBuildingType()))
//    	        .collect(Collectors.toList());
//
//    	return buildingsWithLifeSupport;
	}

	/**
	 * Gets a list of settlement's buildings with power generation function
	 * 
	 * @return list of buildings
	 */
	public List<Building> getBuildingsWithPowerGeneration() {
		return getBuildings(FunctionType.POWER_GENERATION);
		// Using JavaFX/8 Stream
//		List<Building> buildings = getACopyOfBuildings();
//    	List<Building> buildingsWithPower =
//            	buildings.stream()
//        		//buildings.parallelStream() // parallelStream makes it 3x slower than sequential stream
//    	        .filter(s -> buildingConfig.hasPowerGeneration(s.getBuildingType()))
//    	        .collect(Collectors.toList());
//
//    	return buildingsWithPower;
	}

	/**
	 * Gets a list of settlement's buildings with thermal function
	 * 
	 * @return list of buildings
	 */
	public List<Building> getBuildingsWithThermal() {
		return getBuildings(FunctionType.THERMAL_GENERATION);
		// Using JavaFX/8 Stream
//		List<Building> buildings = getACopyOfBuildings();
//    	List<Building> buildingsWithThermal =
//        	buildings.stream()
//    		//buildings.parallelStream() // parallelStream makes it 3x slower than sequential stream
//    	        .filter(s -> buildingConfig.hasThermalGeneration(s.getBuildingType()))
//    	        .collect(Collectors.toList());
//
//    	//List<Building> buildings = getBuildings();
//    	//List<Building> buildingsWithThermal = new ArrayList<Building>();
//    	Iterator<Building> i = buildings.iterator();
//		while (i.hasNext()) {
//			Building b = i.next();
//			String buildingType = b.getBuildingType();
//			//if (config.hasThermalGeneration(buildingType) && !buildingType.equals("Hallway") && !buildingType.equals("Tunnel") ) {
//			if (config.hasThermalGeneration(buildingType)) {
//				buildingsWithThermal.add(b);
//			}
//		}
//
//		//Using Lambda Expression with internal iterator
//    	buildings.forEach(b -> {
//			String buildingType = b.getBuildingType();
//			if (config.hasThermalGeneration(buildingType)) {
//				buildingsWithThermal.add(b);
//			}
//		});
//    	return buildingsWithThermal;
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
	public Building getBuilding(int id) {
		// Use Java 8 stream
		// Note: stream won't pass junit test.
//		return buildings
//				.stream()
//				.filter(b-> b.getID() == id)
//				.findFirst().orElse(null);//.get();	// .findAny()

		// Note: the version below can pass junit test.
		Building result = null;

		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			Building b = i.next();
			if (b.getTemplateID() == id) {
				result = b;
				// return b;
				// NOTE: do NOT use return b or else it fails maven test.
				// break;
				// NOTE: the word 'break' here will cause maven test to fail
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
	public List<Building> getSortedBuildings(FunctionType bf) {

		if (buildingFunctionsMap.containsKey(bf)) {
			return buildingFunctionsMap.get(bf).stream().sorted(new AlphanumComparator()).collect(Collectors.toList());

		}

		else {
			List<Building> list = buildings.stream().filter(b -> b.hasFunction(bf)).sorted(new AlphanumComparator())
					.collect(Collectors.toList());

			buildingFunctionsMap.put(bf, list);
			logger.info(bf + " was not found in buildingFunctionsMap yet. Just added.");

			return list;
		}

	}

	/**
	 * Gets the buildings in a settlement that has a given function.
	 * 
	 * @param building function {@link FunctionType} the function of the building.
	 * @return list of buildings.
	 */
	public List<Building> getBuildings(FunctionType bf) {
		if (buildingFunctionsMap.containsKey(bf)) {
			return buildingFunctionsMap.get(bf);
		}

		else {
			List<Building> list = buildings.stream().filter(b -> b.hasFunction(bf)).collect(Collectors.toList());

			buildingFunctionsMap.put(bf, list);
			logger.info(bf + " was not found in buildingFunctionsMap yet. Just added.");

			return list;
		}

	}

	/**
	 * Gets the buildings in a settlement have have all of a given array of
	 * functions.
	 * 
	 * @param functions the array of required functions {@link BuildingFunctions}.
	 * @return list of buildings.
	 */
	public List<Building> getBuildings(FunctionType f1, FunctionType f2) {
		return buildings.stream().filter(b -> b.hasFunction(f1) && b.hasFunction(f2)).collect(Collectors.toList());
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
		return buildings.stream().filter(b -> b.getBuildingType().equalsIgnoreCase(buildingType))
				.collect(Collectors.toList());
	}

	/**
	 * Gets the number of buildings at the settlement.
	 * 
	 * @return number of buildings
	 */
	public int countBuildingNum() {
		return buildings.size();
	}

	/**
	 * Gets the number of buildings at the settlement.
	 * 
	 * @return number of buildings
	 */
	public int getNumBuilding() {
		return numBuildings;
	}

	/**
	 * Time passing for all buildings.
	 *
	 * @param time amount of time passing (in millisols)
	 * @throws Exception if error.
	 */
	public void timePassing(double time) {
		// check for the passing of each day
		int solElapsed = marsClock.getMissionSol();

		if (solCache != solElapsed) {
			solCache = solElapsed;

			// Update the impact probability for each settlement based on the size and speed
			// of the new meteorite
			meteorite.startMeteoriteImpact(this);
		}

		numBuildings = countBuildingNum();

		for (Building b : buildings) {
			b.timePassing(time);
		}
	}

	/**
	 * Gets a random inhabitable building.
	 * 
	 * @return inhabitable building.
	 */
	public Building getRandomInhabitableBuilding() {
		return getBuildings(FunctionType.LIFE_SUPPORT).stream().findAny().orElse(null);
	}

	/**
	 * Gets a random building with an airlock.
	 * 
	 * @return random building.
	 */
	public Building getRandomAirlockBuilding() {
		return getBuildings(FunctionType.EVA).stream().findAny().orElse(null);

	}

	/**
	 * Adds a person/robot to a random inhabitable building within a settlement.
	 *
	 * @param unit       the person/robot to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if person/robot cannot be added to any building.
	 */
	public static void addToRandomBuilding(Unit unit, Settlement settlement) {
		Person person = null;
		Robot robot = null;
		if (unit instanceof Person) {
			person = (Person) unit;

			Building building = getLeastCrowdedBuildings(
					settlement.getBuildingManager().getBuildings(FunctionType.EVA, FunctionType.LIFE_SUPPORT)).stream()
							.findAny().orElse(null);

			if (building != null) {
				addPersonOrRobotToBuildingRandomLocation(person, building);
			} else {
				// throw new IllegalStateException("No inhabitable buildings available for " +
				// person.getName());
				LogConsolidated.log(logger, Level.INFO, 2000, sourceName,
						"[" + person.getLocationTag().getQuickLocation() + "] No inhabitable buildings available for "
								+ person.getName(),
						null);
			}

		}

		else if (unit instanceof Robot) {
			robot = (Robot) unit;
			// find robot type
			RobotType robotType = robot.getRobotType();
			FunctionType function = null;

			if (robotType == RobotType.CHEFBOT) {
				function = FunctionType.COOKING;
			} else if (robotType == RobotType.CONSTRUCTIONBOT) {
				function = FunctionType.MANUFACTURE;
			} else if (robotType == RobotType.DELIVERYBOT) {
				function = FunctionType.GROUND_VEHICLE_MAINTENANCE;
			} else if (robotType == RobotType.GARDENBOT) {
				function = FunctionType.FARMING;
			} else if (robotType == RobotType.MAKERBOT) {
				function = FunctionType.MANUFACTURE;
			} else if (robotType == RobotType.MEDICBOT) {
				function = FunctionType.MEDICAL_CARE;
			} else if (robotType == RobotType.REPAIRBOT) {
				function = FunctionType.ROBOTIC_STATION;
				// TODO: create another building function to house repairbot ?
			}

			List<Building> functionBuildings = settlement.getBuildingManager().getBuildings(function);

			Building destination = null;

			// Note: if the function is robotic-station, go through the list and remove
			// hallways
			// since we don't want robots to stay in a hallway
			List<Building> validBuildings = new ArrayList<Building>();
			for (Building bldg : functionBuildings) {
				RoboticStation roboticStation = bldg.getRoboticStation();
				// remove hallway, tunnel, observatory
				if (roboticStation != null) {
					if (bldg.getBuildingType().toLowerCase().contains("hallway")
							|| bldg.getBuildingType().toLowerCase().contains("tunnel")
							|| bldg.getBuildingType().toLowerCase().contains("observatory")) {
						// functionBuildings.remove(bldg); // will cause ConcurrentModificationException
					} else if (function == FunctionType.FARMING) {
						if (bldg.getBuildingType().toLowerCase().contains("greenhouse")) {
							validBuildings.add(bldg);
						}
					} else if (function == FunctionType.MANUFACTURE) {
						if (bldg.getBuildingType().toLowerCase().contains("workshop")
								|| bldg.getBuildingType().toLowerCase().contains("manufacturing shed")
								|| bldg.getBuildingType().toLowerCase().contains("machinery hab")) {
							validBuildings.add(bldg);
						}
					} else if (function == FunctionType.COOKING) {
						if (bldg.getBuildingType().toLowerCase().contains("lounge")) {
							validBuildings.add(bldg);
						}
					} else if (function == FunctionType.MEDICAL_CARE) {
						if (bldg.getBuildingType().toLowerCase().contains("infirmary")
								|| bldg.getBuildingType().toLowerCase().contains("medical")) {
							validBuildings.add(bldg);
						}
					} else if (function == FunctionType.GROUND_VEHICLE_MAINTENANCE) {
						if (bldg.getBuildingType().toLowerCase().contains("garage")) {
							validBuildings.add(bldg);
						}
					} else { // if there is no specialized buildings,
								// then add general purpose buildings like "Lander Hab"
						validBuildings.add(bldg);
					}
				}
			}

			// Randomly pick one of the buildings
			if (validBuildings.size() >= 1) {
				int rand = RandomUtil.getRandomInt(validBuildings.size() - 1);
				destination = validBuildings.get(rand);
				addPersonOrRobotToBuildingRandomLocation(robot, destination);
			}

			else {
				List<Building> validBuildings1 = new ArrayList<Building>();
				List<Building> stations = settlement.getBuildingManager().getBuildings(FunctionType.ROBOTIC_STATION);
				for (Building bldg : stations) {
					// remove hallway, tunnel, observatory
					if (bldg.getBuildingType().toLowerCase().contains("hallway")
							|| bldg.getBuildingType().toLowerCase().contains("tunnel")
							|| bldg.getBuildingType().toLowerCase().contains("observatory")) {
						// stations.remove(bldg);// will cause ConcurrentModificationException
					} else {
						validBuildings1.add(bldg); // do nothing
					}
				}
				// Randomly pick one of the buildings
				if (validBuildings1.size() >= 1) {
					int rand = RandomUtil.getRandomInt(validBuildings1.size() - 1);
					destination = validBuildings1.get(rand);
				}
				addPersonOrRobotToBuildingRandomLocation(robot, destination);
				// throw new IllegalStateException("No inhabitable buildings available for " +
				// unit.getName());
			}

		}

	}

	/**
	 * Adds a ground vehicle to a random ground vehicle maintenance building within
	 * a settlement.
	 * 
	 * @param vehicle    the ground vehicle to add.
	 * @param settlement the settlement to find a building.
	 * @throws BuildingException if vehicle cannot be added to any building.
	 * 
	 * @return true if it has been added to a garage 
	 */
	public static boolean addToGarage(GroundVehicle vehicle, Settlement settlement) {

		if (vehicle.getStatus() == StatusType.GARAGED) {
			if (vehicle.getSettlement() == null)
				settlement.getInventory().storeUnit(vehicle);
			LogConsolidated.log(logger, Level.INFO, 1000, sourceName,
					"[" + settlement.getName() + "] " + vehicle.getName() + " already garaged in " + getBuilding(vehicle, settlement), null);
			return true;
		}
		
		List<Building> garages = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE);
		List<VehicleMaintenance> openGarages = new ArrayList<VehicleMaintenance>();
		for (Building garageBuilding : garages) {
			VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
			if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity())
				openGarages.add(garage);
		}

		if (openGarages.size() > 0) {
			int rand = RandomUtil.getRandomInt(openGarages.size() - 1);
			openGarages.get(rand).addVehicle(vehicle);
			if (vehicle.getSettlement() == null)
				settlement.getInventory().storeUnit(vehicle);
			LogConsolidated.log(logger, Level.INFO, 1000, sourceName,
				"[" + settlement.getName() + "] " +  vehicle.getName() + " has just been stowed inside " + getBuilding(vehicle, settlement), null);
			vehicle.setStatus(StatusType.GARAGED);
			return true;
		}
//		else {
//			logger.info("[" + settlement.getName() + "] No available garage space found for " + vehicle.getName() + ".");
//			return false;
//		}
		
		return false;

	}

	/**
	 * Gets the vehicle maintenance building a given vehicle is in.
	 * 
	 * @return building or null if none.
	 */
	public static Building getBuilding(Vehicle vehicle) {
		if (vehicle == null)
			throw new IllegalArgumentException("vehicle is null");
		Building result = null;
		Settlement settlement = vehicle.getSettlement();
		if (settlement != null) {
			List<Building> list = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE);
			for (Building garageBuilding : list) {
				try {
					VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
					if (garage != null && garage.containsVehicle(vehicle)) {
						return garageBuilding;
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Calling getBuilding(vehicle): " + e.getMessage());
				}
			}
		}
		return result;
	}

	/**
	 * Gets the vehicle maintenance building a given vehicle is in.
	 * 
	 * @return building or null if none.
	 */
	public static Building getBuilding(Vehicle vehicle, Settlement settlement) {
		if (vehicle == null)
			throw new IllegalArgumentException("vehicle is null");
		if (settlement != null) {
			List<Building> list = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE);
			for (Building garageBuilding : list) {
				try {
					VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
					if (garage != null && garage.containsVehicle(vehicle)) {
						return garageBuilding;
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Calling getBuilding(vehicle, settlement) : " + e.getMessage());
				}
			}
		}
		return null;
	}

	/**
	 * Gets an available vehicle maintenance building for resource hookup.
	 * 
	 * @param settlement
	 * @return building or null if none.
	 */
	public static Building getAGarage(Settlement settlement) {
		if (settlement != null) {
			List<Building> list = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE);
			int size = list.size();
			int rand = RandomUtil.getRandomInt(size-1);
			return list.get(rand);
//			for (Building garageBuilding : list) {
//				try {
//					VehicleMaintenance garage = garageBuilding.getVehicleMaintenance();
//					if (garage != null) {
//						return garageBuilding;
//					}
//				} catch (Exception e) {
//					logger.log(Level.SEVERE, "Calling getAGarage(settlement) : " + e.getMessage());
//				}
//			}
		}
		return null;
	}
	
	/**
	 * Gets the building a person or robot is in.
	 * 
	 * @return building or null if none.
	 */
	public static Building getBuilding(Unit unit) {
		Building result = null;
		Person person = null;
		Robot robot = null;

		if (unit instanceof Person) {
			person = (Person) unit;
			if (person.isInSettlement()) {
				return person.getBuildingLocation();

//	            Settlement settlement = person.getSettlement();
//	            List<Building> list = settlement.getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT);
//	            for (Building building : list) {
//	                try {
//	                    LifeSupport lifeSupport = building.getLifeSupport();
//	                    if (lifeSupport.containsOccupant(person)) {
//	                        //if (result == null) {
//	                            result = building;
//	                        //}
//	                        //else {
//	                        //    throw new IllegalStateException(person + " is located in more than one building: " + result +
//	                        //           " and " + building);
//	                        //}
//	                    }
//	                }
//	                catch (Exception e) {
//	                    logger.log(Level.SEVERE,"BuildingManager.getBuilding(): " + e.getMessage());
//	                }
//	            }
			}
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			if (robot.isInSettlement()) {
				return robot.getBuildingLocation();
//				Settlement settlement = robot.getSettlement();
// 	            Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
//	            while (i.hasNext()) {
//	                Building building = i.next();
//	                try {
//	                	RoboticStation roboticStation = building.getRoboticStation();
//	                	if (roboticStation.containsRobotOccupant(robot)) {
//	                		result = building;
//	                    }
//	                }
//	                catch (Exception e) {
//	                    logger.log(Level.SEVERE,"BuildingManager.getBuilding(): " + e.getMessage());
//	                }
//	            }
			}
		}
		return result;
	}

	/**
	 * Check what building a given local settlement position is within.
	 * 
	 * @param xLoc the local X position.
	 * @param yLoc the local Y position.
	 * @return building the position is within, or null if the position is not
	 *         within any building.
	 */
	public Building getBuildingAtPosition(double xLoc, double yLoc) {
		// Use Java 8 stream
		return buildings.stream().filter(b -> LocalAreaUtil.checkLocationWithinLocalBoundedObject(xLoc, yLoc, b))
				.findFirst().orElse(null);// get();
//        Building result = null;
//        //for (Building building : buildings) {
//        Iterator<Building> i = buildings.iterator();
//        while (i.hasNext()) {// && (result == null)) {
//            Building building = i.next();
//            if (LocalAreaUtil.checkLocationWithinLocalBoundedObject(xLoc, yLoc, building)) {
//                result = building;
//            }
//        }
//
//        return result;
	}

	/**
	 * Gets a list of uncrowded buildings from a given list of buildings with life
	 * support.
	 * 
	 * @param buildingList list of buildings with the life support function.
	 * @return list of buildings that are not at or above maximum occupant capacity.
	 * @throws BuildingException if building in list does not have the life support
	 *                           function.
	 */
	public static List<Building> getUncrowdedBuildings(List<Building> buildingList) {
//    	List<Building> result = new ArrayList<Building>();
//        try {
//            for (Building building : buildingList) {
//            //Iterator<Building> i = buildingList.iterator();
//            //while (i.hasNext()) {
//            //    Building building = i.next();
//                LifeSupport lifeSupport = (LifeSupport) building.getFunction(BuildingFunction.LIFE_SUPPORT);
//                if (lifeSupport.getAvailableOccupancy() > 0) {
//                    result.add(building);
//                }
//            }
//        }
//        catch (ClassCastException e) {
//            throw new IllegalStateException("BuildingManager.getUncrowdedBuildings(): " +
//                    "building isn't a life support building.");
//        }
//        return result;
		return buildingList.stream()
				.filter(b -> ((RoboticStation) b.getFunction(FunctionType.LIFE_SUPPORT)).getAvailableOccupancy() > 0)
				.collect(Collectors.toList());
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
	public static List<Building> getLeastCrowdedBuildings(List<Building> buildingList) {

		List<Building> result = new ArrayList<Building>();

		// Find least crowded population.
		int leastCrowded = Integer.MAX_VALUE;
		for (Building b0 : buildingList) {
			LifeSupport lifeSupport = b0.getLifeSupport();
			int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
			if (crowded < -1)
				crowded = -1;
			if (crowded < leastCrowded)
				leastCrowded = crowded;
		}

		// Add least crowded buildings to list.
		for (Building b : buildingList) {
			LifeSupport lifeSupport = b.getLifeSupport();
			int crowded = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity();
			if (crowded < -1)
				crowded = -1;
			if (crowded == leastCrowded)
				result.add(b);
		}
		// Add least crowded buildings to list.
//        Iterator<Building> j = buildingList.iterator();
//        while (j.hasNext()) {
//            Building building = j.next();
//            EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
//            if (eva != null) {
//	            int crowded = eva.getAirlock().getOccupants().size() - eva.getAirlock().getCapacity();
//	            if (crowded < -1) crowded = -1;
//	            if (crowded < leastCrowded) leastCrowded = crowded;
//            }
//        }
//
//        // Add least crowded buildings to list.
//        Iterator<Building> j = buildingList.iterator();
//        while (j.hasNext()) {
//            Building building = j.next();
//            EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
//            if (eva != null) {
//	            int crowded = eva.getAirlock().getOccupants().size() - eva.getAirlock().getCapacity();
//	            if (crowded < -1) crowded = -1;
//	            if (crowded == leastCrowded) result.add(building);
//            }
//        }

		return result;
	}

	public static List<Building> getLeastCrowded4BotBuildings(List<Building> buildingList) {

		List<Building> result = new ArrayList<Building>();

		// Find least crowded population.
		int leastCrowded = Integer.MAX_VALUE;
		for (Building building : buildingList) {
			RoboticStation roboticStation = building.getRoboticStation();
			int crowded = roboticStation.getRobotOccupantNumber() - roboticStation.getOccupantCapacity();
			if (crowded < -1)
				crowded = -1;
			if (crowded < leastCrowded)
				leastCrowded = crowded;
		}

		// Add least crowded buildings to list.
		for (Building building : buildingList) {
			RoboticStation roboticStation = building.getRoboticStation();
			int crowded = roboticStation.getRobotOccupantNumber() - roboticStation.getOccupantCapacity();
			if (crowded < -1)
				crowded = -1;
			if (crowded == leastCrowded)
				result.add(building);
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
	public static Map<Building, Double> getBestRelationshipBuildings(Person person, List<Building> buildingList) {
		Map<Building, Double> result = new HashMap<Building, Double>(buildingList.size());
		// RelationshipManager relationshipManager =
		// Simulation.instance().getRelationshipManager();
		// Determine probabilities based on relationships in buildings.
		for (Building building : buildingList) {
			LifeSupport lifeSupport = building.getLifeSupport();
			double buildingRelationships = 0D;
			int numPeople = 0;
			for (Person occupant : lifeSupport.getOccupants()) {
				if (person != occupant) {
					buildingRelationships += relationshipManager.getOpinionOfPerson(person, occupant);
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
		return result;
	}

	/**
	 * Gets a map of buildings having on-going social conversations
	 * 
	 * @param buildingList the list of buildings to filter.
	 * @return map of buildings and their probabilities.
	 */
	public static List<Building> getChattyBuildings(List<Building> buildingList) {

		List<Building> result = new ArrayList<Building>();
		for (Building building : buildingList) {
			LifeSupport lifeSupport = building.getLifeSupport();
			int numPeople = 0;
			for (Person occupant : lifeSupport.getOccupants()) {
				// Task task = occupant.getMind().getTaskManager().getTask();
				if (occupant.getMind().getTaskManager().getTask() instanceof HaveConversation) {
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
	public static List<Building> getNonMalfunctioningBuildings(List<Building> buildingList) {

//    	List<Building> result = new ArrayList<Building>();
//
//        for (Building building : buildingList) {
//        //Iterator<Building> i = buildingList.iterator();
//        //while (i.hasNext()) {
//        //	Building building = i.next();
//        //	boolean malfunction = building.getMalfunctionManager().hasMalfunction();
//        //	if (!malfunction) result.add(building);
//            if (!building.getMalfunctionManager().hasMalfunction()) result.add(building);
//        }
//
//        return result;

		return buildingList.stream().filter(b -> !b.getMalfunctionManager().hasMalfunction())
				.collect(Collectors.toList());
	}

	/**
	 * Gets a list of buildings that have a valid interior walking path from the
	 * person's current building.
	 * 
	 * @param person       the person.
	 * @param buildingList initial list of buildings.
	 * @return list of buildings with valid walking path.
	 */
	public static List<Building> getWalkableBuildings(Unit unit, List<Building> buildingList) {
		List<Building> result = new ArrayList<Building>();
		Person person = null;
		Robot robot = null;

		if (unit instanceof Person) {
			person = (Person) unit;
			Building currentBuilding = BuildingManager.getBuilding(person);
			if (currentBuilding != null) {
				BuildingConnectorManager connectorManager = person.getSettlement().getBuildingConnectorManager();

				for (Building building : buildingList) {
					InsideBuildingPath validPath = connectorManager.determineShortestPath(currentBuilding,
							currentBuilding.getXLocation(), currentBuilding.getYLocation(), building,
							building.getXLocation(), building.getYLocation());

					if (validPath != null) {
						result.add(building);
					}
				}
			}
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			Building currentBuilding = BuildingManager.getBuilding(robot);
			if (currentBuilding != null) {
				BuildingConnectorManager connectorManager = robot.getSettlement().getBuildingConnectorManager();

				for (Building building : buildingList) {
					InsideBuildingPath validPath = connectorManager.determineShortestPath(currentBuilding,
							currentBuilding.getXLocation(), currentBuilding.getYLocation(), building,
							building.getXLocation(), building.getYLocation());

					if (validPath != null) {
						result.add(building);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Adds the person to the building the person's same location if possible.
	 * 
	 * @param person   the person to add.
	 * @param building the building to add the person to.
	 */
	public static void addPersonOrRobotToBuildingSameLocation(Unit unit, Building building) {
		if (building != null) {
			try {

				if (unit instanceof Person) {
					Person person = (Person) unit;
					LifeSupport lifeSupport = building.getLifeSupport();

					if (!lifeSupport.containsOccupant(person)) {
						lifeSupport.addPerson(person);
					}

					person.setCurrentBuilding(building);
				}

				else if (unit instanceof Robot) {
					Robot robot = (Robot) unit;
					RoboticStation roboticStation = building.getRoboticStation();

					if (!roboticStation.containsRobotOccupant(robot)) {
						roboticStation.addRobot(robot);
					}

					robot.setCurrentBuilding(building);
				}

			} catch (Exception e) {
				throw new IllegalStateException(
						"BuildingManager.addPersonOrRobotToBuildingSameLocation(): " + e.getMessage());
			}
		} else
			throw new IllegalStateException("Building is null");
	}

	/**
	 * Adds the person to the building at a given location if possible.
	 * 
	 * @param person   the person to add.
	 * @param building the building to add the person to.
	 */
	public static void addPersonOrRobotToBuilding(Unit unit, Building building, double xLocation, double yLocation) {
		if (building != null) {

			if (!LocalAreaUtil.checkLocationWithinLocalBoundedObject(xLocation, yLocation, building)) {
				throw new IllegalArgumentException(
						building.getNickName() + " does not contain location x: " + xLocation + ", y: " + yLocation);
			}

			try {
				if (unit instanceof Person) {
					Person person = (Person) unit;
					LifeSupport lifeSupport = building.getLifeSupport();

					if (!lifeSupport.containsOccupant(person)) {
						lifeSupport.addPerson(person);
					}

					person.setXLocation(xLocation);
					person.setYLocation(yLocation);
					person.setCurrentBuilding(building);
				}

				else if (unit instanceof Robot) {
					Robot robot = (Robot) unit;
					RoboticStation roboticStation = building.getRoboticStation();

					if (roboticStation.containsRobotOccupant(robot)) {
						roboticStation.addRobot(robot);
					}
					robot.setXLocation(xLocation);
					robot.setYLocation(yLocation);
					robot.setCurrentBuilding(building);
				}

			} catch (Exception e) {
				throw new IllegalStateException("BuildingManager.addPersonOrRobotToBuilding(): " + e.getMessage());
			}
		} else {
			throw new IllegalStateException("Building is null");
		}
	}

	/**
	 * Adds the person to the building at a random location if possible.
	 * 
	 * @param person   the person to add.
	 * @param building the building to add the person to.
	 */
	public static void addPersonOrRobotToBuildingRandomLocation(Unit unit, Building building) {
		if (building != null) {
			try {
				// Add person to random location within building.
				// TODO: Modify this when implementing active locations in buildings.
				Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(building);
				Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(),
						buildingLoc.getY(), building);

				if (unit instanceof Person) {
					Person person = (Person) unit;
					LifeSupport lifeSupport = building.getLifeSupport();

					if (!lifeSupport.containsOccupant(person)) {
						// System.out.println("!lifeSupport.containsRobotOccupant(person) is true");
						lifeSupport.addPerson(person);
					}
					person.setXLocation(settlementLoc.getX());
					person.setYLocation(settlementLoc.getY());
					person.setCurrentBuilding(building);
				}

				else if (unit instanceof Robot) {
					Robot robot = (Robot) unit;
					RoboticStation roboticStation = building.getRoboticStation();

					if (!roboticStation.containsRobotOccupant(robot)) {
						roboticStation.addRobot(robot);
					}
					robot.setXLocation(settlementLoc.getX());
					robot.setYLocation(settlementLoc.getY());
					robot.setCurrentBuilding(building);
				}
			} catch (Exception e) {
				throw new IllegalStateException(
						"BuildingManager.addPersonOrRobotToBuildingRandomLocation(): " + e.getMessage());
			}
		} else {
			throw new IllegalStateException("Building is null");
		}
	}

	/**
	 * Removes the person from a building if possible.
	 * 
	 * @param person   the person to remove.
	 * @param building the building to remove the person from.
	 */
	public static void removePersonOrRobotFromBuilding(Unit unit, Building building) {
		if (building != null) {
			try {

				if (unit instanceof Person) {
					Person person = (Person) unit;
					LifeSupport lifeSupport = building.getLifeSupport();

					if (lifeSupport.containsOccupant(person)) {
						lifeSupport.removePerson(person);
					}
					person.setCurrentBuilding(null);
				}

				else if (unit instanceof Robot) {
					Robot robot = (Robot) unit;
					RoboticStation roboticStation = building.getRoboticStation();

					if (roboticStation.containsRobotOccupant(robot)) {
						roboticStation.removeRobot(robot);
					}
					robot.setCurrentBuilding(null);
				}

			} catch (Exception e) {
				throw new IllegalStateException("BuildingManager.removePersonOrRobotFromBuilding(): " + e.getMessage());
			}
		} else {
			throw new IllegalStateException("Building is null");
		}
	}

	/**
	 * Gets the value of a building at the settlement.
	 * 
	 * @param buildingType the building type.
	 * @param newBuilding  true if adding a new building.
	 * @return building value (VP).
	 */
	public double getBuildingValue(String buildingType, boolean newBuilding) {

		// Make sure building name is lower case.
		buildingType = buildingType.toLowerCase().trim();

		// Update building values cache once per Sol.
		// MarsClock currentTime =
		// Simulation.instance().getMasterClock().getMarsClock(); ?
		if ((lastBuildingValuesUpdateTime == null)
				|| (MarsClock.getTimeDiff(marsClock, lastBuildingValuesUpdateTime) > 1000D)) {
			buildingValuesNewCache.clear();
			buildingValuesOldCache.clear();
			lastBuildingValuesUpdateTime = (MarsClock) marsClock.clone();
		}

		if (newBuilding && buildingValuesNewCache.containsKey(buildingType)) {
			return buildingValuesNewCache.get(buildingType);
		} else if (!newBuilding && buildingValuesOldCache.containsKey(buildingType)) {
			return buildingValuesOldCache.get(buildingType);
		} else {
			double result = 0D;

			// Determine value of all building functions.
			if (buildingConfig.hasCommunication(buildingType))
				result += Communication.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasCooking(buildingType)) {
				result += Cooking.getFunctionValue(buildingType, newBuilding, settlement);
				result += PreparingDessert.getFunctionValue(buildingType, newBuilding, settlement);
			}
			if (buildingConfig.hasDining(buildingType))
				result += Dining.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasEVA(buildingType))
				result += EVA.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasExercise(buildingType))
				result += Exercise.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasFarming(buildingType))
				result += Farming.getFunctionValue(buildingType, newBuilding, settlement);

			if (buildingConfig.hasFoodProduction(buildingType))
				result += FoodProduction.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasGroundVehicleMaintenance(buildingType))
				result += GroundVehicleMaintenance.getFunctionValue(buildingType, newBuilding, settlement);

			if (buildingConfig.hasThermalGeneration(buildingType))
				result += ThermalGeneration.getFunctionValue(buildingType, newBuilding, settlement);
			// if (config.hasThermalStorage(buildingType))
			// result += ThermalStorage.getFunctionValue(buildingType, newBuilding,
			// settlement);
			if (buildingConfig.hasLifeSupport(buildingType))
				result += LifeSupport.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasLivingAccommodations(buildingType))
				result += LivingAccommodations.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasManufacture(buildingType))
				result += Manufacture.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasMedicalCare(buildingType))
				result += MedicalCare.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasPowerGeneration(buildingType))
				result += PowerGeneration.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasPowerStorage(buildingType))
				result += PowerStorage.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasRecreation(buildingType))
				result += Recreation.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasResearchLab(buildingType))
				result += Research.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasResourceProcessing(buildingType))
				result += ResourceProcessing.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasStorage(buildingType))
				result += Storage.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasAstronomicalObservation(buildingType))
				result += AstronomicalObservation.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasManagement(buildingType))
				result += Management.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasEarthReturn(buildingType))
				result += EarthReturn.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasBuildingConnection(buildingType))
				result += BuildingConnection.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasAdministration(buildingType))
				result += Administration.getFunctionValue(buildingType, newBuilding, settlement);
			if (buildingConfig.hasRoboticStation(buildingType))
				result += RoboticStation.getFunctionValue(buildingType, newBuilding, settlement);

			// Multiply value.
			result *= 1000D;

			// Subtract power costs per Sol.
			double power = buildingConfig.getBasePowerRequirement(buildingType);
			double hoursInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
			double powerPerSol = power * hoursInSol;
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

			// System.out.println("Building " + buildingType + " value: " + (int) result);

			if (newBuilding)
				buildingValuesNewCache.put(buildingType, result);
			else
				buildingValuesOldCache.put(buildingType, result);

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
		double result = 0D;

		result = getBuildingValue(building.getBuildingType(), false);

		// Modify building value by its wear condition.
		double wearCondition = building.getMalfunctionManager().getWearCondition();
		result *= (wearCondition / 100D) * .75D + .25D;

		// logger.fine("getBuildingValue() : value is " + result);
		return result;
	}

	/**
	 * Checks if a proposed building location is open or intersects with existing
	 * buildings or construction sites.
	 * 
	 * @param xLoc   the new building's X location.
	 * @param yLoc   the new building's Y location.
	 * @param width  the new building's width (meters).
	 * @param length the new building's length (meters).
	 * @param facing the new building's facing (degrees clockwise from North).
	 * @return true if new building location is open.
	 */
	public boolean isBuildingLocationOpen(double xLoc, double yLoc, double width, double length, double facing) {
		return isBuildingLocationOpen(xLoc, yLoc, width, length, facing, null);
	}

	/**
	 * Checks if a proposed building location is open or intersects with existing
	 * buildings or construction sites.
	 * 
	 * @param xLoc   the new building's X location.
	 * @param yLoc   the new building's Y location.
	 * @param width  the new building's width (meters).
	 * @param length the new building's length (meters).
	 * @param facing the new building's facing (degrees clockwise from North).
	 * @param site   the new construction site or null if none.
	 * @return true if new building location is open.
	 */
	public boolean isBuildingLocationOpen(double xLoc, double yLoc, double width, double length, double facing,
			ConstructionSite site) {
		boolean goodLocation = true;

		goodLocation = LocalAreaUtil.isObjectCollisionFree(site, width, length, xLoc, yLoc, facing,
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
	public boolean hasBuildingFrame(String frameName) {
		boolean result = false;

		// Check if any existing buildings have this frame.
		for (Building building : buildings) {
			// TODO: determine if getName() needed to be changed to getNickName()
			ConstructionStageInfo buildingStageInfo = ConstructionUtil
					.getConstructionStageInfo(building.getBuildingType());
			if (buildingStageInfo != null) {
				ConstructionStageInfo frameStageInfo = ConstructionUtil.getPrerequisiteStage(buildingStageInfo);
				if (frameStageInfo != null) {
					// TODO: determine if getName() needed to be changed to getNickName()
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
	 * Obtains the inhabitable building having that particular id
	 * 
	 * @param id
	 * @return inhabitable building
	 */
	public Building getInhabitableBuilding(int id) {
		// Use Java 8 stream
		return buildings.stream().filter(b -> b.getInhabitableID() == id).findFirst().orElse(null);// .get();
//    	Building result = null;
//        Iterator<Building> i = buildings.iterator();
//        while (i.hasNext()) {
//            Building b = i.next();
//            if (b.getInhabitable_id() == id) {
//            	return b;
//            	//break;
//            }
//        }
//
//        return result;
	}

	/**
	 * Gets the next template ID for a new building in a settlement (but not unique
	 * in a simulation)
	 * 
	 * @return template ID (starting from 0).
	 */
	public int getNextTemplateID() {

		int largestID = 0;
		for (Building building : buildings) {
			int id = building.getTemplateID();
			if (id > largestID) {
				largestID = id;
			}
		}

		return largestID + 1;
	}

	/**
	 * Gets a unique ID for a new inhabitable building in a settlement (but not
	 * unique in a simulation)
	 * 
	 * @return inhabitable ID (starting from 0).
	 */
	public int getNextInhabitableID() {

		int max = -1;
		for (Building b : buildings) {
			if (b.hasFunction(FunctionType.LIFE_SUPPORT)) {
				int id = b.getInhabitableID();
				max = Math.max(id, max);
				// if (id > nextNum)
				// nextNum++;
			}
		}
		return max + 1;
	}

	/**
	 * Gets the largest inhabitable ID in a settlement
	 * 
	 * @return inhabitable ID (starting from 0).
	 */
	public int getLargestInhabitableID() {

		int max = -1;
		for (Building b : buildings) {
			if (b.hasFunction(FunctionType.LIFE_SUPPORT)) {
				int id = b.getInhabitableID();
				max = Math.max(id, max);
			}
		}
		return max;
	}

	/**
	 * Creates a map of building type id
	 * 
	 * @param b a given building
	 */
	public void createBuildingTypeIDMap(Building b) {
		String buildingType = b.getBuildingType();
		String n = b.getNickName();
		int new_id = Integer.parseInt(b.getNickName().substring(n.lastIndexOf(" ") + 1, n.length()));

		if (buildingTypeIDMap.containsKey(buildingType)) {
			int old_id = buildingTypeIDMap.get(buildingType);
			if (old_id < new_id)
				buildingTypeIDMap.put(buildingType, new_id);
		} else
			buildingTypeIDMap.put(buildingType, new_id);
	}

	/**
	 * Gets an available building type ID for a new building.
	 * 
	 * @param buildingType
	 * @return type ID (starting from 1).
	 */
	public int getNextBuildingTypeID(String buildingType) {
		int id = 1;
		if (buildingTypeIDMap.containsKey(buildingType)) {
			id = buildingTypeIDMap.get(buildingType);
			buildingTypeIDMap.put(buildingType, id + 1);
			return id;
		} else {
			buildingTypeIDMap.put(buildingType, id);
			return id;
		}

//        int largest = 0;
//        Iterator<Building> i = buildings.iterator();
//        while (i.hasNext()) {
//            Building b = i.next();
//            String type = b.getBuildingType();
//            if (buildingType.equals(type)) {
//            	int id = b.getTemplateID();
//            	//largest = Math.max(id, largest);
//            	if (id > largest)
//            	largest++;
//            }
//        }
//
//        return largest + 1;
	}

	/**
	 * Gets a unique nick name for a new building
	 * 
	 * @return a unique nick name
	 */
	public String getBuildingNickName(String buildingType) {
		return buildingType + " " + getNextBuildingTypeID(buildingType);
	}

//	private String getCharForNumber(int i) {
//		// NOTE: i must be > 1, if i = 0, return null
//	    return i > 0 && i < 27 ? String.valueOf((char)(i + 'A' - 1)) : null;
//	}

	/**
	 * Gets a list of farm buildings needing work from a list of buildings with the
	 * farming function.
	 * 
	 * @param buildingList list of buildings with the farming function.
	 * @return list of farming buildings needing work.
	 */
	public List<Building> getFarmsNeedingWork() {
		List<Building> result = null;

		int m = marsClock.getMillisolInt();
		// Add caching and relocate from TendGreenhouse
		if (millisolCache + 5 >= m) {
			result = farmsNeedingWorkCache;
		}

		else {
			millisolCache = m;
			List<Building> farmBuildings = getLeastCrowdedBuildings(
					getNonMalfunctioningBuildings(getBuildings(FunctionType.FARMING)));
			// farmBuildings = getNonMalfunctioningBuildings(farmBuildings);
			// farmBuildings = getLeastCrowdedBuildings(farmBuildings);
			result = new ArrayList<Building>();
			for (Building b : farmBuildings) {
				Farming farm = b.getFarming();
				if (farm.requiresWork()) {
					result.add(b);
				}
			}

			farmsNeedingWorkCache = result;
		}
		return result;
	}

	public List<Building> getFarmsNeedingWorkCache() {
		return farmsNeedingWorkCache;
	}

	// This method is called by MeteoriteImpactImpl
	public void setProbabilityOfImpactPerSQMPerSol(double value) {
		probabilityOfImpactPerSQMPerSol = value;
	}

	// Called by each building once a sol to see if an impact is imminent
	public double getProbabilityOfImpactPerSQMPerSol() {
		return probabilityOfImpactPerSQMPerSol;
	}

	public void setWallPenetration(double value) {
		wallPenetrationThicknessAL = value;
	}

	public double getWallPenetration() {
		return wallPenetrationThicknessAL;
	}

	public Meteorite getMeteorite() {
		return meteorite;
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
	 * Gets an instance of the historical event manager
	 * 
	 * @return
	 */
	public HistoricalEventManager getEventManager() {
		return eventManager;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		Iterator<Building> i = buildings.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}
		// buildings.clear();
		buildings = null;
		settlement = null;
		// buildingValuesNewCache.clear();
		buildingValuesNewCache = null;
		// buildingValuesOldCache.clear();
		buildingValuesOldCache = null;
		lastBuildingValuesUpdateTime = null;
		resupply = null;
		meteorite = null;
		marsClock = null;
		masterClock = null;
		buildingConfig = null;
	}

}