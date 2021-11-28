/*
 * Mars Simulation Project
 * Settlement.java
 * @date 2021-10-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.environment.DustStorm;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentInventory;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.ItemHolder;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Commander;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnector;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The Settlement class represents a settlement unit on virtual Mars. It
 * contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements Serializable, Temporal,
	LifeSupportInterface, Objective, EquipmentOwner, ItemHolder  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Settlement.class.getName());

	// Static members
	private static final String DETECTOR_GRID = "The detector grid forecast a ";
	private static final String TRADING_OUTPOST = "Trading Outpost";
	private static final String MINING_OUTPOST = "Mining Outpost";
	private static final String ASTRONOMY_OBSERVATORY = "Astronomy Observatory";

	public static final int CHECK_MISSION = 20; // once every 10 millisols
	public static final int MAX_NUM_SOLS = 3;
	public static final int MAX_SOLS_DAILY_OUTPUT = 14;
	public static final int SUPPLY_DEMAND_REFRESH = 7;
	private static final int RESOURCE_UPDATE_FREQ = 50;
	private static final int CHECK_WATER_RATION = 66;
	private static final int RESOURCE_SAMPLING_FREQ = 50; // in msols
	public static final int NUM_CRITICAL_RESOURCES = 10;
	private static final int RESOURCE_STAT_SOLS = 12;
	private static final int SOL_SLEEP_PATTERN_REFRESH = 3;
	public static final int REGOLITH_MAX = 2000;
	public static final int MIN_REGOLITH_RESERVE = 80; // per person
	public static final int MIN_SAND_RESERVE = 5; // per person
	public static final int ICE_MAX = 4000;
	public static final int WATER_MAX = 8_000;
	public static final int MIN_ICE_RESERVE = 200; // per person

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int HYDROGEN_ID = ResourceUtil.hydrogenID;
	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int CO2_ID = ResourceUtil.co2ID;
	private static final int FOOD_ID = ResourceUtil.foodID;
	private static final int METHANE_ID = ResourceUtil.methaneID;
	private static final int REGOLITH_ID = ResourceUtil.regolithID;
	private static final int SAND_ID = ResourceUtil.sandID;
	private static final int ICE_ID = ResourceUtil.iceID;
	private static final int GREY_WATER_ID = ResourceUtil.greyWaterID;
	private static final int BLACK_WATER_ID = ResourceUtil.blackWaterID;
	private static final int ROCK_SAMPLES_ID = ResourceUtil.rockSamplesID;

	// Threshold to adjust filtering rate
	private static final double GREY_WATER_THRESHOLD = 0.00001;

	public static final double MIN_WATER_RESERVE = 400D; // per person
	public static final double SAFE_TEMPERATURE_RANGE = 18;
	// Initial mission passing score
	private static final double INITIAL_MISSION_SCORE = 400D;
	// Hvae a maximum mission score that can be recorded
	private static final double MAX_MISSION_SCORE = 1000D;

	/** Normal air pressure [in kPa] */
	private static final double NORMAL_AIR_PRESSURE = CompositionOfAir.SKYLAB_TOTAL_AIR_PRESSURE_kPA;

	/** The settlement water consumption */
	public static double water_consumption_rate;
	/** The settlement minimum air pressure requirement. */
	public static double minimum_air_pressure;
	/** The settlement life support requirements. */
	public static double[][] life_support_value = new double[2][7];

	/** The Flag showing if the settlement has been exposed to the last radiation event. */
	private boolean[] exposed = { false, false, false };
	/** The cache for the number of building connectors. */
	private transient int numConnectorsCache = 0;
	/** The cache for the msol. */
	private transient int msolCache = 0;

	/** The settlement sampling resources. */
	public final static int[] samplingResources;

	/** The definition of static arrays */
	static {
		samplingResources = new int[] {
				OXYGEN_ID,
				HYDROGEN_ID,
				CO2_ID,
				METHANE_ID,
				WATER_ID,
				GREY_WATER_ID,
				BLACK_WATER_ID,
				ROCK_SAMPLES_ID,
				ICE_ID,
				REGOLITH_ID };
	}

	/** The flag for checking if the simulation has just started. */
	private boolean justLoaded = true;
	/** The base mission probability of the settlement. */
	private boolean missionProbability = false;
	/** The water ration level of the settlement. */
	private int waterRationLevel = 1;
	/** The number of people at the start of the settlement. */
	private int initialPopulation;
	/** The number of robots at the start of the settlement. */
	private int projectedNumOfRobots;
	/** The template ID of the settlement. */
	private int templateID;
	/** The cache for the mission sol. */
	private int solCache = 0;
	// NOTE: can't be static since each settlement needs to keep tracking of it
	private int numShiftsCache;
	/** number of people with work shift A */
	private int numA;
	/** number of people with work shift B */
	private int numB;
	/** number of people with work shift X */
	private int numX;
	/** number of people with work shift Y */
	private int numY;
	/** number of people with work shift Z */
	private int numZ;
	/** number of people with work shift "On Call" */
	private int numOnCall;
	/** number of people with work shift "Off" */
	private int numOff;
	/** The cache for the numbers of crops that need tending. */
	private int cropsNeedingTendingCache = 5;
	/** The cache for millisol. */
	private int millisolCache = -5;
	/** Numbers of citizens of this settlement. */
	private int numCitizens;
	/** Numbers of bots owned by this settlement. */
	private int numOwnedBots;
	/** Numbers of vehicles owned by this settlement. */
	private int numOwnedVehicles;
	/** Numbers of equipment owned by this settlement. */
//	private int numOwnedEquipment;
	/** Minimum amount of methane to stay in this settlement when considering a mission. */
	private int minMethane = 50;
	/** Minimum amount of oxygen to stay in this settlement when considering a mission. */
	private int mineOxygen = 50;
	/** Minimum amount of water to stay in this settlement when considering a mission. */
	private int minWater = 50;
	/** Minimum amount of food to stay in this settlement when considering a mission. */
	private int minFood = 50;

	/** The flag signifying this settlement as the destination of the user-defined commander. */
	private boolean hasDesignatedCommander = false;

	/** The average regolith collection rate nearby */
	private double regolithCollectionRate = RandomUtil.getRandomDouble(4, 8);
	/** The average ice collection rate of the water ice nearby */
	private double iceCollectionRate = RandomUtil.getRandomDouble(0.2, 1);
	/** The composite value of the minerals nearby. */
	public double mineralValue = -1;
	/** The rate [kg per millisol] of filtering grey water for irrigating the crop. */
	public double greyWaterFilteringRate = 1;
	/** The currently minimum passing score for mission approval. */
	private double minimumPassingScore = INITIAL_MISSION_SCORE;
	/** Goods manager update time. */
	private double goodsManagerUpdateTime = 0D;
	/** The settlement's current indoor temperature. */
	private double currentTemperature = 22.5;
	/** The settlement's current indoor pressure [in kPa], not Pascal. */
	private double currentPressure = NORMAL_AIR_PRESSURE;
//	/** Amount of time (millisols) that the settlement has had zero population. */
//	private double zeroPopulationTime;
	/** The settlement's current meal replenishment rate. */
	public double mealsReplenishmentRate = 0.6;
	/** The settlement's current dessert replenishment rate. */
	public double dessertsReplenishmentRate = 0.7;
	/** The settlement's current probability value for ice. */
	private double iceProbabilityValue = 0;
	/** The settlement's current probability value for regolith. */
	private double regolithProbabilityValue = 0;
	/** The settlement's current probability value for oxygen. */
	private double oxygenProbabilityValue = 0;
	/** The settlement's current probability value for methane. */
	private double methaneProbabilityValue = 0;
	/** The settlement's outside temperature. */
	private double outside_temperature;

	/** The settlement terrain profile. */
	public double[] terrainProfile = new double[2];

	/** The settlement template name. */
	private String template;

	/** The settlement's ReportingAuthority instance. */
	private ReportingAuthority ra;
	/** The settlement's building manager. */
	protected BuildingManager buildingManager;
	/** The settlement's building connector manager. */
	protected BuildingConnectorManager buildingConnectorManager;
	/** The settlement's goods manager. */
	protected GoodsManager goodsManager;
	/** The settlement's construction manager. */
	protected ConstructionManager constructionManager;
	/** The settlement's building power grid. */
	protected PowerGrid powerGrid;
	/** The settlement's heating system. */
	protected ThermalSystem thermalSystem;
	/** The settlement's chain of command. */
	private ChainOfCommand chainOfCommand;
	/** The settlement's composition of air. */
	private CompositionOfAir compositionOfAir;
	/** The settlement's location. */
	private Coordinates location;
	/** The settlement's last dust storm. */
	private DustStorm storm;
	/** The person's EquipmentInventory instance. */
	private EquipmentInventory eqmInventory;

	/** The settlement objective type instance. */
	private ObjectiveType objectiveType;

	/** The settlement's water consumption in kitchen when preparing/cleaning meal and dessert. */
	private SolMetricDataLogger<WaterUseType> waterConsumption;
	/** The settlement's daily output (resources produced). */
	private SolMetricDataLogger<Integer> dailyResourceOutput;
	/** The settlement's daily labor hours output. */
	private SolMetricDataLogger<Integer> dailyLaborTime;

	/** The settlement's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement;
	/** The map of settlements allowed to trade. */
	private Map<Integer, Boolean> allowTradeMissionSettlements;
	/** The map of mission modifiers. */
	private Map<MissionType, Integer> missionModifiers;
	/** The mission radius [in km] for the rovers of this settlement for each type of mission . */
	private Map<MissionType, Integer> missionRange = new EnumMap<>(MissionType.class);
	/** The settlement's map of adjacent buildings. */
	private transient Map<Building, List<Building>> adjacentBuildingMap = new ConcurrentHashMap<>();
	/** The total amount resource collected/studied. */
	private Map<Integer, Double> resourcesCollected = new HashMap<>();
	/** The settlement's resource statistics. */
	private Map<Integer, Map<Integer, Map<Integer, Double>>> resourceStat = new ConcurrentHashMap<>();

	/** The last 20 mission scores */
	private List<Double> missionScores;

	/** The set of processes being overridden. */
	private Set<OverrideType> processOverrides = new HashSet<>();
	/** The set of disabled missions. */
	private Set<MissionType> disabledMissions = new HashSet<>();
	/** The set of available airlocks. */
	private Set<Integer> availableAirlocks = new HashSet<>();

	/** The settlement's list of citizens. */
	private Set<Person> citizens;// = new ConcurrentLinkedQueue<Person>();
	/** The settlement's list of owned robots. */
	private Set<Robot> ownedRobots;// = new ConcurrentLinkedQueue<Robot>();
	/** The settlement's list of owned vehicles. */
	private Set<Vehicle> ownedVehicles;// = new ConcurrentLinkedQueue<Vehicle>();
	/** The settlement's list of parked vehicles. */
	private Set<Vehicle> parkedVehicles;// = new ConcurrentLinkedQueue<Vehicle>();
	/** The list of people currently within the settlement. */
	private Set<Person> peopleWithin;// = new ConcurrentLinkedQueue<Person>();
	/** The settlement's list of robots within. */
	private Set<Robot> robotsWithin;// = new ConcurrentLinkedQueue<Robot>();

	private static SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
	private static PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();


	static {
		water_consumption_rate = personConfig.getWaterConsumptionRate();
		minimum_air_pressure = personConfig.getMinAirPressure();
		life_support_value = settlementConfig.getLifeSupportRequirements();
	}

	/**
	 * Constructor 1 called by ConstructionStageTest suite for maven testing.
	 */
	private Settlement() {
		super(null, null);

		unitManager = sim.getUnitManager();

		// set location
		location = getCoordinates();
	}

	/**
	 * The static factory method called by ConstructionStageTest to return a new
	 * instance of Settlement for maven testing.
	 */
	public static Settlement createConstructionStage() {
		return new Settlement();
	}

	/**
	 * Constructor 2 called by MockSettlement for maven testing.
	 *
	 * @param name
	 * @param id
	 * @param location
	 */
	public Settlement(String name, int id, Coordinates location) {
		// Use Structure constructor.
		super(name, location);

		if (unitManager == null) {// for passing maven test
			unitManager = sim.getUnitManager();
		}

		this.templateID = id;
		this.location = location;

		citizens = new UnitSet<>();
		ownedRobots = new UnitSet<>();
		ownedVehicles = new UnitSet<>();
		parkedVehicles = new UnitSet<>();
		peopleWithin = new UnitSet<>();
		robotsWithin = new UnitSet<>();

		if (missionManager == null) {// for passing maven test
			missionManager = sim.getMissionManager();
		}

		final double GEN_MAX = 1_000_000;
		// Create EquipmentInventory instance
		eqmInventory = new EquipmentInventory(this, GEN_MAX);
	}

	/**
	 * Constructor 3 for creating settlements. Called by UnitManager to create the
	 * initial settlement Called by ArrivingSettlement to create a brand new
	 * settlement
	 *
	 * @param name
	 * @param id
	 * @param template
	 * @param sponsor
	 * @param location
	 * @param populationNumber
	 * @param initialNumOfRobots
	 */
	private Settlement(String name, int id, String template, ReportingAuthority sponsor, Coordinates location, int populationNumber,
			int projectedNumOfRobots) {
		// Use Structure constructor
		super(name, location);

		this.template = template;
		this.location = location;
		this.templateID = id;
		this.projectedNumOfRobots = projectedNumOfRobots;
		this.initialPopulation = populationNumber;

		// Determine the reporting authority
		this.ra = sponsor;

		citizens = new UnitSet<>();
		ownedRobots = new UnitSet<>();
		ownedVehicles = new UnitSet<>();
		parkedVehicles = new UnitSet<>();
		peopleWithin = new UnitSet<>();
		robotsWithin = new UnitSet<>();

		// Determine the mission directive modifiers
		determineMissionAgenda();

		allowTradeMissionSettlements = new ConcurrentHashMap<Integer, Boolean>();
	}


	/**
	 * The static factory method called by UnitManager and ArrivingSettlement to
	 * create a brand new settlement
	 *
	 * @param name
	 * @param id
	 * @param template
	 * @param sponsor
	 * @param location
	 * @param populationNumber
	 * @param initialNumOfRobots
	 * @return
	 */
	public static Settlement createNewSettlement(String name, int id, String template, ReportingAuthority sponsor,
			Coordinates location, int populationNumber, int initialNumOfRobots) {
		return new Settlement(name, id, template, sponsor, location, populationNumber, initialNumOfRobots);
	}


	/**
	 * Initialize field data, class and maps
	 */
	public void initialize() {
		if (terrainElevation == null)
			terrainElevation = surfaceFeatures.getTerrainElevation();

//		// Get the elevation and terrain gradient factor
		terrainProfile = terrainElevation.getTerrainProfile(location);

//		Note: to check elevation, do this -> double elevation = terrainProfile[0];
//		Note: to check gradient, do this ->double gradient = terrainProfile[1];

		iceCollectionRate = iceCollectionRate + terrainElevation.getIceCollectionRate(location);

		final double GEN_MAX = 1_000_000;
		// Create EquipmentInventory instance
		eqmInventory = new EquipmentInventory(this, GEN_MAX);

		final double INITIAL_FREE_OXYGEN = 1_000;
		// Stores limited amount of oxygen in this settlement
		storeAmountResource(ResourceUtil.oxygenID, INITIAL_FREE_OXYGEN);

		// Initialize building manager
		buildingManager = new BuildingManager(this);
		// Initialize building connector manager.
		buildingConnectorManager = new BuildingConnectorManager(this);

		// Initialize goods manager.
		goodsManager = new GoodsManager(this);

		// Initialize construction manager.
		constructionManager = new ConstructionManager(this);

		// Initialize power grid
		powerGrid = new PowerGrid(this);

		// Added thermal control system
		thermalSystem = new ThermalSystem(this);

		// Initialize scientific achievement.
		scientificAchievement = new HashMap<ScienceType, Double>(0);

		// Add chain of command
		chainOfCommand = new ChainOfCommand(this);

		// Add tracking composition of air
		compositionOfAir = new CompositionOfAir(this);

		// Set objective()
		if (template.equals(TRADING_OUTPOST))
			setObjective(ObjectiveType.TRADE_CENTER, 2);
		else if (template.equals(MINING_OUTPOST))
			setObjective(ObjectiveType.MANUFACTURING_DEPOT, 2);
		else
			setObjective(ObjectiveType.CROP_FARM, 2);

		// initialize the missionScores list
		missionScores = new ArrayList<>();
		missionScores.add(INITIAL_MISSION_SCORE);

		// Create the water consumption map
		waterConsumption = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		// Create the daily output map
		dailyResourceOutput = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		// Create the daily labor hours map
		dailyLaborTime = new SolMetricDataLogger<>(MAX_NUM_SOLS);

		// Set default mission radius
		missionRange.put(MissionType.AREOLOGY, 500);
		missionRange.put(MissionType.BIOLOGY,500);
		missionRange.put(MissionType.COLLECT_ICE,500);
		missionRange.put(MissionType.COLLECT_REGOLITH,500);
		missionRange.put(MissionType.DELIVERY,4000);
		missionRange.put(MissionType.EMERGENCY_SUPPLY,1000);
		missionRange.put(MissionType.EXPLORATION, 500);
		missionRange.put(MissionType.METEOROLOGY, 500);
		missionRange.put(MissionType.MINING, 500);
		missionRange.put(MissionType.RESCUE_SALVAGE_VEHICLE, 1000);
		missionRange.put(MissionType.TRADE, 2000);
		missionRange.put(MissionType.TRAVEL_TO_SETTLEMENT, 4000);
	}

	/**
	 * Sets the mission agenda based on the sponsors' mission objectives
	 */
	private void determineMissionAgenda() {
		missionModifiers = new EnumMap<>(MissionType.class);

		// Default all modifiers to zero to start with
		for (MissionType mt : MissionType.values()) {
			missionModifiers.put(mt, 0);
		}

		Map<MissionType, Integer> agendaModifers =
				ra.getMissionAgenda().getAgendas().stream()
				  .flatMap(e -> e.getModifiers().entrySet().stream())
				  .collect(Collectors.groupingBy(Map.Entry::getKey,
						  Collectors.summingInt(Map.Entry::getValue)));

		missionModifiers.putAll(agendaModifers);
	}

	/*
	 * Gets sponsoring agency for the person
	 */
	public ReportingAuthority getSponsor() {
		return ra;
	}

	/**
	 * Create a map of buildings with their lists of building connectors attached to
	 * it
	 *
	 * @return a map
	 */
	private Map<Building, List<Building>> createAdjacentBuildingMap() {
		if (adjacentBuildingMap == null)
			adjacentBuildingMap = new ConcurrentHashMap<>();
		for (Building b : buildingManager.getBuildings()) {
			List<Building> connectors = createAdjacentBuildings(b);
			adjacentBuildingMap.put(b, connectors);
		}

		return adjacentBuildingMap;
	}

	/**
	 * Gets a list of building connectors attached to this building
	 *
	 * @param building
	 * @return
	 */
	public List<Building> getBuildingConnectors(Building building) {
		if (!adjacentBuildingMap.containsKey(building))
			adjacentBuildingMap = createAdjacentBuildingMap();

		return adjacentBuildingMap.get(building);
	}

	/**
	 * Creates a list of adjacent buildings attached to this building
	 *
	 * @param building
	 * @return a list of adjacent buildings
	 */
	public List<Building> createAdjacentBuildings(Building building) {
		List<Building> buildings = new ArrayList<>();

		Set<BuildingConnector> connectors = buildingConnectorManager.getConnectionsToBuilding(building);
		for (BuildingConnector c : connectors) {
			Building b1 = c.getBuilding1();
			Building b2 = c.getBuilding2();
			if (b1 != building) {
				buildings.add(b1);
			} else if (b2 != building) {
				buildings.add(b2);
			}
		}

		return buildings;
	}

	/**
	 * Gets the settlement's meals replenishment rate.
	 *
	 * @return mealsReplenishmentRate
	 */
	public double getMealsReplenishmentRate() {
		return mealsReplenishmentRate;
	}

	/**
	 * Sets the settlement's meals replenishment rate.
	 *
	 * @param rate
	 */
	public void setMealsReplenishmentRate(double rate) {
		mealsReplenishmentRate = rate;
	}

	/**
	 * Gets the settlement's desserts replenishment rate.
	 *
	 * @return DessertsReplenishmentRate
	 */
	public double getDessertsReplenishmentRate() {
		return dessertsReplenishmentRate;
	}

	/**
	 * Sets the settlement's desserts replenishment rate.
	 *
	 * @param rate
	 */
	public void setDessertsReplenishmentRate(double rate) {
		dessertsReplenishmentRate = rate;
	}

	/**
	 * Gets the settlement template's unique ID.
	 *
	 * @return ID number.
	 */
	public int getID() {
		return templateID;
	}

	/**
	 * Gets the population capacity of the settlement
	 *
	 * @return the population capacity
	 */
	public int getPopulationCapacity() {
		return buildingManager.getPopulationCapacity();
	}

	/**
	 * Gets the current number of people who are inside the settlement
	 *
	 * @return the number indoor
	 */
	public int getIndoorPeopleCount() {
		return peopleWithin.size();
	}

	public void endAllIndoorTasks() {
		for (Person p : getIndoorPeople()) {
			logger.log(this, p, Level.INFO, 4_000,
						"Had to end the current indoor tasks at ("
						+ Math.round(p.getXLocation()*10.0)/10.0 + ", "
						+ Math.round(p.getYLocation()*10.0)/10.0 + ")", null);
			p.getMind().getTaskManager().clearAllTasks("Stop indoor tasks");
		}
	}

	/**
	 * Gets a collection of the people who are currently inside the settlement.
	 *
	 * @return Collection of people within
	 */
	public Collection<Person> getIndoorPeople() {
		return peopleWithin;
	}

	/**
	 * Gets a collection of people who are doing EVA outside the settlement.
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getOutsideEVAPeople() {

		return citizens.stream()
				.filter(p -> !p.isDeclaredDead()
						&& (p.getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY
						|| p.getLocationStateType() == LocationStateType.MARS_SURFACE))
				.collect(Collectors.toList());

	}

	/**
	 * Gets a collection of people who are doing EVA outside the settlement.
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getOnMissionPeople() {

		return citizens.stream()
				.filter(p -> !p.isDeclaredDead() && p.getMind().getMission() != null
						&& !p.getMind().getMission().getPhase().equals(VehicleMission.REVIEWING))
				.collect(Collectors.toList());

	}

	/**
	 * Gets the robot capacity of the settlement
	 *
	 * @return the robot capacity
	 */
	public int getRobotCapacity() {
		int result = 0;
		int stations = 0;
		List<Building> bs = buildingManager.getBuildings(FunctionType.ROBOTIC_STATION);
		for (Building b : bs) {
			stations += b.getRoboticStation().getSlots();
		}

		result = result + stations;

		return result;
	}

	/**
	 * Gets the current number of robots in the settlement
	 *
	 * @return the number of robots
	 */
	public int getIndoorRobotsCount() {
		return ownedRobots.size();
//		return Math.toIntExact(getInventory().getAllContainedUnitIDs()
//				.stream().filter(id -> unitManager.getRobotByID(id) instanceof Robot)
//				.collect(Collectors.counting()));
	}

	/**
	 * Gets a collection of the number of robots of the settlement.
	 *
	 * @return Collection of robots
	 */
	public Collection<Robot> getRobots() {
		return ownedRobots;//CollectionUtils.getRobot(getInventory().getContainedUnits());
	}

	/**
	 * Gets a collection of the number of robots of a particular type.
	 *
	 * @return Collection of robots
	 */
	public Collection<Robot> getRobots(RobotType type) {
		// using java 8 stream
		return getRobots().stream()
				.filter(r -> r.getRobotType() == type)
				.collect(Collectors.toList());
	}

	/**
	 * Returns true if life support is working properly and is not out of oxygen or
	 * water.
	 *
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	public boolean lifeSupportCheck() {

		try {
			double amount = getAmountResourceStored(ResourceUtil.oxygenID);
			if (amount <= 0D) {
				logger.warning(this, "No more oxygen.");
				return false;
			}
			amount = getAmountResourceStored(WATER_ID);
			if (amount <= 0D) {
				logger.warning(this, "No more water.");
				return false;
			}

			// Check against indoor air pressure
			double p = getAirPressure();
			if (p > PhysicalCondition.MAXIMUM_AIR_PRESSURE || p < Settlement.minimum_air_pressure) {
				logger.warning(this, "Out-of-range overall air pressure at " + Math.round(p * 10D) / 10D + " kPa detected.");
				return false;
			}

			double t = currentTemperature;
			if (t < life_support_value[0][4] - SAFE_TEMPERATURE_RANGE
					|| t > life_support_value[1][4] + SAFE_TEMPERATURE_RANGE) {
				logger.warning(this, "Out-of-range overall temperature at "
						   + Math.round(t * 10D) / 10D
						   + " " + Msg.getString("temperature.sign.degreeCelsius") + " detected.");
				return false;
			}

		} catch (Exception e) {
			logger.severe("Problems in lifeSupportCheck(): " + e.getMessage());
		}

		return true;
	}

	/**
	 * Gets the number of people the life support can provide for.
	 *
	 * @return the capacity of the life support system
	 */
	@Override
	public int getLifeSupportCapacity() {
		return getPopulationCapacity();
	}

	/**
	 * Gets oxygen from the inventory.
	 *
	 * @param oxygenTaken the amount of oxygen requested [kg]
	 * @return the amount of oxygen actually received [kg]
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double oxygenTaken) {
//		double oxygenLacking = 0;

//		double oxygenLeft = getAmountResourceStored(OXYGEN_ID);

		// Note: do NOT retrieve O2 here since calculateGasExchange() in
		// CompositionOfAir is doing it for all inhabitants once per frame.

//		oxygenLacking = retrieveAmountResource(OXYGEN_ID, oxygenTaken);
//		addAmountDemand(OXYGEN_ID, oxygenTaken);

//		double carbonDioxideProvided = ratio * (oxygenTaken - oxygenLacking);

		// Note: do NOT store CO2 here since calculateGasExchange() in CompositionOfAir
		// is doing it for all inhabitants once per frame.

//		storeAmountResource(CO2_ID, carbonDioxideProvided);

		return oxygenTaken;
	}

	/**
	 * Gets water from the inventory
	 *
	 * @param waterTaken the amount of water requested [kg]
	 * @return the amount of water actually received [kg]
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double waterTaken) {
		double lacking = retrieveAmountResource(WATER_ID, waterTaken);
		return waterTaken - lacking;
	}

	/**
	 * Computes the average air pressure & temperature of the life support system.
	 *
	 */
	private void computeEnvironmentalAverages() {

		double totalArea = 0;
		double totalTArea = 0;
		double totalPressureArea = 0;
		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();
		for (Building b : buildings) {
			int id = b.getInhabitableID();
			double area = b.getFloorArea();
			totalArea += area;
			totalPressureArea += compositionOfAir.getTotalPressure()[id] * area;
			totalTArea += b.getCurrentTemperature() * area;
		}
		if (totalArea == 0) {
			totalArea = 0.1;
		}

		// convert from atm to kPascal
		currentPressure =  totalPressureArea * CompositionOfAir.KPA_PER_ATM / totalArea;
		currentTemperature = totalTArea / totalArea;
	}

	/**
	 * Gets the air pressure of a particular building.
	 *
	 * @param building
	 * @return air pressure [in kPa] (not Pa)
	 */
	public double getBuildingAirPressure(Building building) {
		double p = 0;
		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();
		for (Building b : buildings) {
			if (b == building) {
				int id = b.getInhabitableID();
				p = compositionOfAir.getTotalPressure()[id];
			}
		}
		// convert from atm to kPascal
		return p * CompositionOfAir.KPA_PER_ATM;
	}

	/**
	 * Gets the air pressure of the life support system.
	 *
	 * @return air pressure [in kPa] (not Pa)
	 */
	public double getAirPressure() {
		return currentPressure;
	}

	public double getTemperature() {
		return currentTemperature;
	}

	/**
	 * Reloads instances after loading from a saved sim
	 *
	 * @param clock
	 * @param w
	 */
	public static void initializeInstances(UnitManager u) {
		unitManager = u;
	}

	/**
	 * Perform time-related processes
	 *
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception error during time passing.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!isValid(pulse)) {
			return false;
		}

		// If settlement is overcrowded, increase inhabitant's stress.
		// should the number of robots be accounted for here?

		double time = pulse.getElapsed();
		int overCrowding = getIndoorPeopleCount() - getPopulationCapacity();
		if (overCrowding > 0) {
			double stressModifier = .1D * overCrowding * time;
			for (Person p : getIndoorPeople()) {
				PhysicalCondition c = p.getPhysicalCondition();
				c.setStress(c.getStress() + stressModifier);
			}
		}

		doCropsNeedTending(pulse);

		// what to take into consideration the presence of robots ?
		// If no current population at settlement for one sol, power down the
		// building and turn the heat off ?

		if (powerGrid.getPowerMode() != PowerMode.POWER_UP)
			powerGrid.setPowerMode(PowerMode.POWER_UP);
		// check if POWER_UP is necessary
		// Question: is POWER_UP a prerequisite of FULL_POWER ?

		powerGrid.timePassing(pulse);

		thermalSystem.timePassing(pulse);

		buildingManager.timePassing(pulse);

		if (pulse.isNewSol()) {
			performEndOfDayTasks(pulse.getMarsTime());
		}

		// Sample a data point every SAMPLE_FREQ (in msols)
		int msol = pulse.getMarsTime().getMillisolInt();

		// Avoid checking at < 10 or 1000 millisols
		// due to high cpu util during the change of day
		if (msolCache != msol && msol >= 10 && msol != 1000) {
			msolCache = msol;

			// Initialize tasks at the start of the sim only
			if (justLoaded) {
				justLoaded = false;

				for (Person p : citizens) {
					// Register each settler a quarter/bed
					Building b = getBestAvailableQuarters(p, true);
					if (b != null)
						b.getLivingAccommodations().registerSleeper(p, false);
				}

				// Initialize the goods manager
				goodsManager.timePassing(pulse);

				// Initialize the good values
				Iterator<Good> i = GoodsUtil.getGoodsList().iterator();
				while (i.hasNext()) {
					Good g = i.next();
					goodsManager.determineGoodValue(g, 0, false);
				}
			}

			// Check for available airlocks
			checkAvailableAirlocks();

			// May update the goods manager updateGoodsManager(pulse);

			int cycles = settlementConfig.getTemplateID();
			int remainder = msol % cycles;
			if (remainder == templateID) {
				// Update the goods value gradually with the use of buffers
				if (goodsManager.isInitialized()) {
					Iterator<Good> i = GoodsUtil.getGoodsList().iterator();
					while (i.hasNext()) {
						Good g = i.next();
						g.adjustGoodValue();
						goodsManager.determineGoodValue(g, 1, false);
					}
				}
			}

			remainder = msol % CHECK_MISSION;
			if (remainder == 1) {
				// Reset the mission probability back to 1
				missionProbability = false;
				mineralValue = -1;
			}

			remainder = msol % RESOURCE_SAMPLING_FREQ;
			if (remainder == 1) {
				// will NOT check for radiation at the exact 1000 millisols in order to balance
				// the simulation load
				// take a sample of how much each critical resource has in store
				sampleAllResources();
			}

			remainder = msol % CHECK_WATER_RATION;
			if (remainder == 1) {
				// Recompute the water ration level
				computeWaterRationLevel();
			}

			// Check every RADIATION_CHECK_FREQ (in millisols)
			// Compute whether a baseline, GCR, or SEP event has occurred
			remainder = msol % RadiationExposure.RADIATION_CHECK_FREQ;
			if (remainder == 5) {
				checkRadiationProbability(time);
			}

			remainder = msol % RESOURCE_UPDATE_FREQ;
			if (remainder == 5) {
				iceProbabilityValue = computeIceProbability();
			}

			if (remainder == 10) {
				regolithProbabilityValue = computeRegolithProbability();
			}
		}

		compositionOfAir.timePassing(pulse);

		computeEnvironmentalAverages();

		outside_temperature = weather.getTemperature(location);

		if (adjacentBuildingMap != null && !adjacentBuildingMap.isEmpty()) {
			int numConnectors = adjacentBuildingMap.size();

			if (numConnectorsCache != numConnectors) {
				numConnectorsCache = numConnectors;
				createAdjacentBuildingMap();
			}
		} else {
			createAdjacentBuildingMap();
		}

		// Update owned Units
		timePassing(pulse, ownedVehicles);

		// Persons can die and leave the Settlement
		Set<Person> died = new HashSet<>();
		for (Person p : citizens) {
			try {
				p.timePassing(pulse);
				if (p.isDeclaredDead()) {
					logger.info(p, "Dead so removing from citizens");
					died.add(p);
				}
			}
			catch (RuntimeException rte) {
				logger.severe(this, "Problem applying pulse : " + rte.getMessage(),
						      rte);
			}
		}
		if (!died.isEmpty()) {
			citizens.removeAll(died);
		}
		timePassing(pulse, ownedRobots);

		return true;
	}

	/**
	 * Apply a clock pulse to a list of Temporal objects. This traps exceptions
	 * to avoid the impact spreading to other units.
	 * @param pulse
	 * @param ownedUnits
	 */
	private void timePassing(ClockPulse pulse, Collection<? extends Temporal> ownedUnits) {
		for (Temporal t : ownedUnits) {
			try {
				t.timePassing(pulse);
			}
			catch (RuntimeException rte) {
				logger.severe(this, "Problem applying pulse : " + rte.getMessage(),
						      rte);
			}
		}
	}

	/**
	 * Gets the best available living accommodations building that the person can
	 * use. Returns null if no living accommodations building is currently
	 * available.
	 *
	 * @param person   the person
	 * @param unmarked does the person wants an unmarked(aka undesignated) bed or
	 *                 not.
	 * @return a building with available bed(s)
	 */
	private static Building getBestAvailableQuarters(Person person, boolean unmarked) {

		Building result = null;

		if (person.isInSettlement()) {
			List<Building> b = person.getSettlement().getBuildingManager()
					.getBuildings(FunctionType.LIVING_ACCOMMODATIONS);
			b = BuildingManager.getNonMalfunctioningBuildings(b);
			b = getQuartersWithEmptyBeds(b, unmarked);
			if (b.size() == 1) {
				return b.get(0);
			}
			if (b.size() > 0) {
				b = BuildingManager.getLeastCrowdedBuildings(b);
			}

			if (b.size() > 1) {
				Map<Building, Double> probs = BuildingManager.getBestRelationshipBuildings(person,
						b);
				result = RandomUtil.getWeightedRandomObject(probs);
			}
			else if (b.size() == 1) {
				return b.get(0);
			}
		}

		return result;
	}

	/**
	 * Gets living accommodations with empty beds from a list of buildings with the
	 * living accommodations function.
	 *
	 * @param buildingList list of buildings with the living accommodations
	 *                     function.
	 * @param unmarked     does the person wants an unmarked(aka undesignated) bed
	 *                     or not.
	 * @return list of buildings with empty beds.
	 */
	private static List<Building> getQuartersWithEmptyBeds(List<Building> buildingList, boolean unmarked) {
		List<Building> result = new ArrayList<>();

		for (Building building : buildingList) {
			LivingAccommodations quarters = building.getLivingAccommodations();

			// Check if an unmarked bed is wanted
			if (unmarked) {
				if (quarters.hasAnUnmarkedBed()) {// && notFull) {
					result.add(building);
				}
			}
		}

		return result;
	}

	/**
	 * Samples all the critical resources for stats
	 *
	 * @param resourceType
	 */
	private void sampleAllResources() {
		int size = samplingResources.length;
		int sol = marsClock.getMissionSol();
		for (int i = 0; i < size; i++) {
			int id = samplingResources[i];
			sampleOneResource(id, sol);
		}
	}

	/**
	 * Samples a critical resources for stats.
	 * Creates a map for sampling how many or
	 * how much a resource has in a settlement
	 *
	 * @param resourceType
	 */
	private void sampleOneResource(int resourceType, int sol) {
		int msol = marsClock.getMillisolInt();

		if (resourceStat == null)
			resourceStat = new ConcurrentHashMap<>();

		Map<Integer, Map<Integer, Double>> todayMap = null;
		Map<Integer, Double> msolMap = null;
		double newAmount = getAmountResourceStored(resourceType);

		if (resourceStat.containsKey(sol)) {
			todayMap = resourceStat.get(sol);
			if (todayMap.containsKey(resourceType)) {
				msolMap = todayMap.get(resourceType);
			}
			else {
				msolMap = new ConcurrentHashMap<>();
			}
		}
		else {
			msolMap = new ConcurrentHashMap<>();
			todayMap = new ConcurrentHashMap<>();
		}

		msolMap.put(msol, newAmount);
		todayMap.put(resourceType, msolMap);
		resourceStat.put(sol, todayMap);
	}


	/**
	 * Gathers yestersol's statistics for the critical resources
	 *
	 * @return
	 */
	public Map<Integer, Double> gatherResourceStat(int sol) {
		Map<Integer, Double> map = new HashMap<>();
		int size = samplingResources.length;

		for (int i=0; i<size; i++) {
			int id = samplingResources[i];
			double amount = calculateDailyAverageResource(sol, id);
			map.put(id, amount);
		}
		return map;
	}

	/**
	 * Gets the average amount of a critical resource on a sol
	 *
	 * @param sol
	 * @param resourceType
	 * @return
	 */
	private double calculateDailyAverageResource(int sol, int resourceType) {
		int size = 0;
		double average = 0;

		if (resourceStat.containsKey(sol)) {
			Map<Integer, Map<Integer, Double>> solMap = resourceStat.get(sol);
			if (solMap.containsKey(resourceType)) {
				Map<Integer, Double> msolMap = solMap.get(resourceType);
				size = msolMap.size();
				Iterator<Double> i = msolMap.values().iterator();
				while (i.hasNext()) {
					average += i.next();
				}
				average = Math.round(average / size * 10.0)/10.0;
			}
		}

		return average;
	}

	/**
	 * Removes all airlock reservations
	 */
	public void removeAllReservations() {
		List<Airlock> airlocks = getAllAirlocks();

		for (Airlock a: airlocks) {
			a.getReservationMap().clear();
		}
	}

	/**
	 * Provides the daily reports for the settlement
	 */
	private void performEndOfDayTasks(MarsClock marsNow) {
		int solElapsed = marsNow.getMissionSol();

		removeAllReservations();

		reassignWorkShift();

		tuneJobDeficit();

		refreshResourceStat();

		refreshSleepMap(solElapsed);

		refreshSupplyDemandMap(solElapsed);

		// clear estimated orbit repair parts cache value
		goodsManager.clearOrbitRepairParts();

		// Decrease the Mission score.
		minimumPassingScore *= 0.9D;

		// Check the Grey water situation
		if (getAmountResourceStored(GREY_WATER_ID) < GREY_WATER_THRESHOLD) {
			// Adjust the grey water filtering rate
			changeGreyWaterFilteringRate(false);
			double r = getGreyWaterFilteringRate();
			logger.log(this, Level.WARNING, 1_000,
					"Low stores of grey water decreases filtering rate to " + Math.round(r*100.0)/100.0 + ".");
		}
		else if (getAmountResourceRemainingCapacity(GREY_WATER_ID) < GREY_WATER_THRESHOLD) {
			// Adjust the grey water filtering rate
			changeGreyWaterFilteringRate(true);
			double r = getGreyWaterFilteringRate();
			logger.log(this, Level.WARNING, 10_000,
					   "Low capacity for grey water increases filtering rate to " + Math.round(r*100.0)/100.0 + ".");
		}

		solCache = solElapsed;
	}

	private void refreshResourceStat() {
		if (resourceStat == null)
			resourceStat = new ConcurrentHashMap<>();
		// Remove the resourceStat map data from 12 sols ago
		if (resourceStat.size() > RESOURCE_STAT_SOLS)
			resourceStat.remove(0);
	}

	/***
	 * Refreshes the sleep map for each person in the settlement
	 *
	 * @param solElapsed
	 */
	private void refreshSleepMap(int solElapsed) {
		// Update the sleep pattern once every x number of days
		if (solElapsed % SOL_SLEEP_PATTERN_REFRESH == 0) {
			Collection<Person> people = getIndoorPeople();
			for (Person p : people) {
				p.getCircadianClock().inflateSleepHabit();
			}
		}
	}

	/*
	 * Reassigns the work shift for all
	 */
	public void reassignWorkShift() {
		// Should call this method at, say, 800 millisols, not right at 1000
		// millisols
		Collection<Person> people = citizens;
		int pop = people.size();

		for (Person p : people) {

			// Skip the person who is dead or is on a mission
			if (!p.isBuried() && !p.isDeclaredDead() && !p.getPhysicalCondition().isDead()
					&& p.getMind().getMission() == null) {

				assignWorkShift(p, pop);
			}

			// Just for sanity check for those on a vehicle mission
			// Note: shouldn't be needed this way but currently, when currently when
			// starting a trade mission,
			// the code fails to change a person's work shift to On-call.
			else if (p.getMind().getMission() != null && p.isInVehicle()) {

				ShiftType oldShift = p.getTaskSchedule().getShiftType();

				if (oldShift != ShiftType.ON_CALL) {
					p.setShiftType(ShiftType.ON_CALL);
				}
			}
		} // end of people for loop
	}

	/**
	 * Assign this person a work shift, based on the need of the settlement
	 *
	 * @param p
	 * @param pop
	 */
	public void assignWorkShift(Person p, int pop) {

		// Check if person is an astronomer.
		boolean isAstronomer = (p.getMind().getJob() == JobType.ASTRONOMER);

		ShiftType oldShift = p.getTaskSchedule().getShiftType();

		if (isAstronomer) {
			// Find the darkest time of the day
			// and set work shift to cover time period

			// For now, we may assume it will usually be X or Z, NOT Y
			// since Y is usually where midday is at unless a person is at polar region.
			if (oldShift == ShiftType.Y || oldShift == ShiftType.Z) {

				ShiftType newShift = ShiftType.X;

				p.setShiftType(newShift);
			}

		} // end of if (isAstronomer)

		else {
			// Not an astronomer

			// Note: if a person's shift is over-filled or saturated,
			// he will need to change shift

			// Get an unfilled work shift
			ShiftType newShift = getAnEmptyWorkShift(pop);

			int tendency = p.getTaskSchedule().getWorkShiftScore(newShift);
			// Should find the person with the highest tendency to take this shift

			// if the person just came back from a mission, he would have on-call shift
			if (oldShift == ShiftType.ON_CALL) {
				// Check a person's sleep habit map and request changing his work shift
				// to avoid taking a work shift that overlaps his sleep hour

				if (newShift != oldShift) {// sanity check

					if (tendency > 50) {
						p.setShiftType(newShift);
					}

					else {
						ShiftType anotherShift = getAnEmptyWorkShift(pop);
						if (anotherShift == newShift) {
							anotherShift = getAnEmptyWorkShift(pop);
						}

						tendency = p.getTaskSchedule().getWorkShiftScore(newShift);

						if (newShift != oldShift && tendency > 50) { // sanity check
							p.setShiftType(newShift);
						}
					}
				}
			}

			else {
				// The old shift is NOT on-call

				// Note: if a person's shift is NOT over-filled or saturated, he doesn't need to
				// change shift
				boolean oldShift_ok = isWorkShiftSaturated(oldShift, true);

				// TODO: check a person's sleep habit map and request changing his work shift
				// to avoid taking a work shift that overlaps his sleep hour

				if (!oldShift_ok) {

					if (newShift != oldShift && tendency > 50) { // sanity check
						p.setShiftType(newShift);
					}

					else if (tendency <= 50) {
						ShiftType anotherShift = getAnEmptyWorkShift(pop);
						if (anotherShift == newShift) {
							anotherShift = getAnEmptyWorkShift(pop);
						}

						tendency = p.getTaskSchedule().getWorkShiftScore(newShift);

						if (newShift != oldShift && tendency > 50) { // sanity check
							p.setShiftType(newShift);
						}

						else {
							ShiftType shift3 = getAnEmptyWorkShift(pop);
							if (shift3 == newShift) {
								shift3 = getAnEmptyWorkShift(pop);
							}

							if (shift3 != oldShift) { // sanity check
								p.setShiftType(shift3);
							}
						}
					}
				}
			}
		} // end of if (isAstronomer)
	}

	/**
	 * Refreshes and clears settlement's data on the supply/demand and weather
	 *
	 * @param solElapsed # of sols since the start of the sim
	 */
	private void refreshSupplyDemandMap(int solElapsed) {
		// Clear maps once every x number of days
		if (solElapsed % SUPPLY_DEMAND_REFRESH == 0) {
			// True if solElapsed is an exact multiple of x

			// Compact amount resource map
			// Carry out the daily average of the previous x days
//			getInventory().compactAmountSupplyMap(SUPPLY_DEMAND_REFRESH);
//			getInventory().clearAmountSupplyRequestMap();
//			// Carry out the daily average of the previous x days
//			getInventory().compactAmountDemandMap(SUPPLY_DEMAND_REFRESH);
//			getInventory().clearAmountDemandTotalRequestMap();
//			getInventory().clearAmountDemandMetRequestMap();
//
//			// compact item resource map
//			getInventory().compactItemSupplyMap(SUPPLY_DEMAND_REFRESH);
//			getInventory().clearItemSupplyRequestMap();
//			// Carry out the daily average of the previous x days
//			getInventory().compactItemDemandMap(SUPPLY_DEMAND_REFRESH);
//			getInventory().clearItemDemandTotalRequestMap();
//			getInventory().clearItemDemandMetRequestMap();

			// Added clearing of weather data map
			weather.clearMap();
		}
	}

	/**
	 * Updates the GoodsManager twice per day
	 *
	 * @param time
	 */
	private void updateGoodsManager(ClockPulse pulse) {
		goodsManagerUpdateTime += pulse.getElapsed();

		// Randomly update goods manager at a certain time
		double timeThreshold = 250D + RandomUtil.getRandomRegressionInteger(125);
		if (!goodsManager.isInitialized() || (goodsManagerUpdateTime > timeThreshold)) {
			goodsManager.timePassing(pulse);
			goodsManagerUpdateTime = 0D;
		}
	}


	/**
	 * Gets a collection of people who are available for social conversation in the
	 * same/another building in the same/another settlement
	 *
	 * @param initiator      the initiator of this conversation
	 * @param checkIdle      true if the invitee is idling/relaxing (false if the
	 *                       invitee is in a chat)
	 * @param sameBuilding   true if the invitee is at the same building as the
	 *                       initiator (false if it doesn't matter)
	 * @param sameSettlement true if the collection includes all settlements (false
	 *                       if only the initiator's settlement)
	 * @return person a collection of invitee(s)
	 */
	public Collection<Person> getChattingPeople(Person initiator, boolean checkIdle, boolean sameBuilding,
			boolean sameSettlement) {
		Collection<Person> people = new ArrayList<>();
		Iterator<Person> i;
		// Set up rules that allows

		if (sameSettlement) {
			// could be either radio (non face-to-face) conversation, don't care
			i = unitManager.getPeople().iterator();
			sameBuilding = false;
		} else {
			// the only initiator's settlement
			// may be radio or face-to-face conversation
			i = getIndoorPeople().iterator();
		}

		while (i.hasNext()) {
			Person person = i.next();
			if (person.isInSettlement() // .getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT
					&& initiator.isInSettlement()) {// getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT) {
				Task task = person.getMind().getTaskManager().getTask();

				if (sameBuilding) {
					// face-to-face conversation
					if (initiator.getBuildingLocation().equals(person.getBuildingLocation())) {
						if (checkIdle) {
							if (isIdleTask(task)) {
								if (!person.equals(initiator))
									people.add(person);
							}
						} else if (task instanceof HaveConversation) {

							if (!person.equals(initiator))
								people.add(person);
						}
					}
				}

				else {
					// may be radio (non face-to-face) conversation

					if (checkIdle) {
						if (isIdleTask(task)) {
							if (!person.equals(initiator))
								people.add(person);
						}
					} else if (task instanceof HaveConversation) {

						if (!person.equals(initiator))
							people.add(person);
					}
				}
			}
		}

		return people;
	}

	private boolean isIdleTask(Task task) {
        return task instanceof Relax
                || task instanceof Read
                || task instanceof HaveConversation
                || task instanceof EatDrink;
    }

	/**
	 * Gets the settlement's building manager.
	 *
	 * @return building manager
	 */
	public BuildingManager getBuildingManager() {
		return buildingManager;
	}

	/**
	 * Gets the settlement's building connector manager.
	 *
	 * @return building connector manager.
	 */
	public BuildingConnectorManager getBuildingConnectorManager() {
		return buildingConnectorManager;
	}

	/**
	 * Gets the settlement's goods manager.
	 *
	 * @return goods manager
	 */
	public GoodsManager getGoodsManager() {
		return goodsManager;
	}

	/**
	 * Gets a list of airlock of this settlement
	 *
	 * @return
	 */
	public List<Airlock> getAllAirlocks() {
		return buildingManager.getBuildings(FunctionType.EVA).stream()
				.map(b -> b.getEVA().getAirlock())
				.collect(Collectors.toList());
	}


	/**
	 * Gets the closest available airlock to a person.
	 *
	 * @param person the person.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestAvailableAirlock(Person person) {

		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;
		BuildingManager manager = buildingManager;
		Iterator<Building> i = manager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			boolean chamberFull = building.getEVA().getAirlock().isChamberFull();
			boolean reservationFull = building.getEVA().getAirlock().isReservationFull();

			if (!ASTRONOMY_OBSERVATORY.equalsIgnoreCase(building.getBuildingType())) {
				if (!chamberFull || !reservationFull) {
					double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), person.getXLocation(),
							person.getYLocation());
					if (distance < leastDistance) {
						result = building.getEVA().getAirlock();
						leastDistance = distance;
					}
				}
			}
		}

		return result;
	}


	/**
	 * Gets the closest available airlock at the settlement to the given location.
	 * The airlock must have a valid walkable interior path from the person's
	 * current location.
	 *
	 * @param person    the person.
	 * @param xLocation the X location.
	 * @param yLocation the Y location.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableAvailableAirlock(Person person, double xLocation, double yLocation) {
		Building currentBuilding = BuildingManager.getBuilding(person);

		if (currentBuilding == null) {
			// Note: What if a person is out there in ERV building for maintenance ?
			// ERV building has no LifeSupport function. currentBuilding will be null
			logger.log(person, Level.WARNING, 10_000, "Not currently in a building.");
			return null;
		}

		return getAirlock(currentBuilding, xLocation, yLocation);
	}

	/**
	 * Gets an airlock for an EVA egress
	 *
	 * @param robot
	 * @param xLocation
	 * @param yLocation
	 * @return
	 */
	public Airlock getClosestWalkableAvailableAirlock(Robot robot, double xLocation, double yLocation) {
		Building currentBuilding = BuildingManager.getBuilding(robot);

		if (currentBuilding == null) {
			// Note: need to refine the concept of where a robot can go. They are thought to need
			// RoboticStation function to "survive", much like a person who needs LifeSupport function
			logger.log(robot, Level.WARNING, 10_000, "Not currently in a building.");
			return null;
		}

		return getAirlock(currentBuilding, xLocation, yLocation);
	}


	/**
	 * Gets an airlock for an EVA egress, preferably an pressurized airlock.
	 * Consider if the chambers are full and if the reservation is full.
	 *
	 * @param currentBuilding
	 * @param xLocation
	 * @param yLocation
	 * @return
	 */
	private Airlock getAirlock(Building currentBuilding, double xLocation, double yLocation) {
		Airlock result = null;

		// Search the closest of the buildings
		double leastDistance = Double.MAX_VALUE;

		for(int id: availableAirlocks) {
			Building building = unitManager.getBuildingByID(id);
			boolean chamberFull = building.getEVA().getAirlock().isChamberFull();
			boolean reservationFull = building.getEVA().getAirlock().isReservationFull();

			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
			if ((!chamberFull || !reservationFull)
				&& buildingConnectorManager.hasValidPath(currentBuilding, building)) {

				double distance = Point2D.distance(building.getXLocation(), building.getYLocation(),
						xLocation, yLocation);
				if (distance < leastDistance) {

					result = building.getEVA().getAirlock();
					leastDistance = distance;
				}
			}
		}

		return result;
	}


	/**
	 * Check for available airlocks
	 */
	public void checkAvailableAirlocks() {
		List<Building> pressurizedBldgs = new ArrayList<>();
		List<Building> depressurizedBldgs = new ArrayList<>();
		List<Building> selectedPool = new ArrayList<>();

		for(Building airlockBdg : buildingManager.getBuildings(FunctionType.EVA)) {
			Airlock airlock = airlockBdg.getEVA().getAirlock();
			if (airlock.isPressurized()	|| airlock.isPressurizing())
				pressurizedBldgs.add(airlockBdg);
			else if (airlock.isDepressurized() || airlock.isDepressurizing())
				depressurizedBldgs.add(airlockBdg);
		}

		if (pressurizedBldgs.size() > 1) {
			selectedPool = pressurizedBldgs;
		}

		else if (pressurizedBldgs.size() == 1) {
			selectedPool.addAll(pressurizedBldgs);
		}

		else if (depressurizedBldgs.size() > 1) {
			selectedPool = depressurizedBldgs;
		}

		else if (depressurizedBldgs.size() == 1) {
			selectedPool.addAll(depressurizedBldgs);
		}

		for (Building building : selectedPool) {
			boolean chamberFull = building.getEVA().getAirlock().isChamberFull();
			boolean reservationFull = building.getEVA().getAirlock().isReservationFull();
			int id = building.getIdentifier();
			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
			if (!chamberFull || !reservationFull) {
				if (!availableAirlocks.contains(id)) {
					availableAirlocks.add(id);
				}
			}
			else {
				if (availableAirlocks.contains(id)) {
					availableAirlocks.remove(id);
				}
			}
		}
	}

	/**
	 * Gets the closest available airlock at the settlement to the given location.
	 * The airlock must have a valid walkable interior path from the given
	 * building's current location.
	 *
	 * @param building  the building in the walkable interior path.
	 * @param xLocation the X location.
	 * @param yLocation the Y location.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableAvailableAirlock(Building building, double xLocation, double yLocation) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;

		Iterator<Building> i = buildingManager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building nextBuilding = i.next();
			boolean chamberFull = nextBuilding.getEVA().getAirlock().isChamberFull();
			boolean reservationFull = nextBuilding.getEVA().getAirlock().isReservationFull();

			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
			if ((!chamberFull || !reservationFull)
				&& buildingConnectorManager.hasValidPath(building, nextBuilding)) {

				double distance = Point2D.distance(nextBuilding.getXLocation(), nextBuilding.getYLocation(), xLocation,
						yLocation);
				if (distance < leastDistance) {
					EVA eva = nextBuilding.getEVA();
					if (eva != null) {
						result = eva.getAirlock();
						leastDistance = distance;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks if a building has a walkable path from it to an airlock.
	 *
	 * @param building the building.
	 * @return true if an airlock is walkable from the building.
	 */
	public boolean hasWalkableAvailableAirlock(Building building) {
		return (getClosestWalkableAvailableAirlock(building, 0D, 0D) != null);
	}

	/**
	 * Gets the number of airlocks at the settlement.
	 *
	 * @return number of airlocks.
	 */
	public int getAirlockNum() {
		return buildingManager.getBuildings(FunctionType.EVA).size();
	}

	/**
	 * Gets the settlement's power grid.
	 *
	 * @return the power grid.
	 */
	public PowerGrid getPowerGrid() {
		return powerGrid;
	}

	/**
	 * Gets the settlement's heating system.
	 *
	 * @return thermalSystem.
	 */
	public ThermalSystem getThermalSystem() {
		return thermalSystem;
	}

	/**
	 * Gets the settlement template.
	 *
	 * @return template as string.
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Gets the number of associated people with this settlement
	 *
	 * @return the number of associated people.
	 */
	public int getNumCitizens() {
		return numCitizens;
	}

	/**
	 * Gets all people associated with this settlement, even if they are out on
	 * missions.
	 *
	 * @return collection of associated people.
	 */
	public Collection<Person> getAllAssociatedPeople() {
		return citizens;
	}

	/**
	 * Returns a collection of people buried outside this settlement
	 *
	 * @return {@link Collection<Person>}
	 */
	public Collection<Person> getBuriedPeople() {
		// using java 8 stream
		return unitManager.getPeople().stream()
				.filter(p -> p.getBuriedSettlement() == this)
				.collect(Collectors.toList());
	}

	/**
	 * Returns a collection of deceased people who may or may NOT have been buried
	 * outside this settlement
	 *
	 * @return {@link Collection<Person>}
	 */
	public Collection<Person> getDeceasedPeople() {
		// using java 8 stream
		return unitManager.getPeople().stream()
				.filter(p -> (p.getAssociatedSettlement() == this && p.isDeclaredDead()) || p.getBuriedSettlement() == this)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the person by id
	 *
	 * @param id
	 * @return
	 */
	public Person getPerson(int id) {
		return peopleWithin.stream()
				.filter(p -> p.getIdentifier() == id).findFirst().orElse(null);
	}

	/**
	 * Gets the associated person by id
	 *
	 * @param id
	 * @return
	 */
	public Person getAssociatedPerson(int id) {
		return citizens.stream()
				.filter(p -> p.getIdentifier() == id).findFirst().orElse(null);
	}


	/**
	 * Does it contains this person
	 *
	 * @param p the person
	 * @return true if added successfully
	 */
	public boolean containsPerson(Person p) {
		if (peopleWithin.contains(p))
			return true;
		return false;
	}

	/**
	 * Makes this person within this settlement
	 *
	 * @param p the person
	 * @return true if added successfully
	 */
	public boolean addPeopleWithin(Person p) {
		if (peopleWithin.contains(p)) {
			return true;
		}
		if (peopleWithin.add(p)) {
			p.setContainerUnit(this);
			return true;
		}
		return false;
	}

	/**
	 * Removes this person from being within this settlement
	 *
	 * @param p the person
	 * @return true if removed successfully
	 */
	public boolean removePeopleWithin(Person p) {
		if (!peopleWithin.contains(p))
			return true;
		if (peopleWithin.remove(p)) {
			return true;
		}
		return false;
	}

	/**
	 * Makes this person a legal citizen of this settlement
	 *
	 * @param p the person
	 * @return true if removed successfully
	 */
	public boolean addACitizen(Person p) {
		if (citizens.contains(p))
			return true;
		if (citizens.add(p)) {
			addPeopleWithin(p);
			p.setCoordinates(getCoordinates());
			p.setContainerUnit(this);
			// Update the numCtizens
			numCitizens = citizens.size();
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Removes this person from being a legal citizen of this settlement
	 *
	 * @param p the person
	 */
	public boolean removeACitizen(Person p) {
		if (!citizens.contains(p))
			return true;
		if (citizens.remove(p)) {
			// Update the numCtizens
			numCitizens = citizens.size();
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Adds a robot to be owned by the settlement
	 *
	 * @param r
	 */
	public boolean addOwnedRobot(Robot r) {
		if (ownedRobots.contains(r))
			return true;
		if (ownedRobots.add(r)) {
			addRobot(r);
			r.setCoordinates(getCoordinates());
			r.setContainerUnit(this);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT, this);
			numOwnedBots = ownedRobots.size();
			return true;
		}
		return false;
	}

	/**
	 * Removes a robot from being owned by the settlement
	 *
	 * @param r
	 */
	public boolean removeOwnedRobot(Robot r) {
		if (!ownedRobots.contains(r))
			return true;
		if (ownedRobots.remove(r)) {
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT, this);
			numOwnedBots = ownedRobots.size();
			return true;
		}
		return false;
	}

	/**
	 * Adds a robot to be owned by the settlement
	 *
	 * @param r
	 */
	public boolean addRobot(Robot r) {
		if (robotsWithin.contains(r)) {
			return true;
		}
		if (robotsWithin.add(r)) {
			return true;
		}
		return false;
	}

	/**
	 * Removes a robot from being owned by the settlement
	 *
	 * @param r
	 */
	public boolean removeRobot(Robot r) {
		if (!robotsWithin.contains(r)) {
			return true;
		}
		if (robotsWithin.remove(r)) {
			return true;
		}
		return false;
	}

	/**
	 * Adds a parked vehicle
	 *
	 * @param vehicle
	 * @param true if the parked vehicle can be added
	 */
	public boolean addParkedVehicle(Vehicle vehicle) {
		if (parkedVehicles.contains(vehicle)) {
			return true;
		}
		if (parkedVehicles.add(vehicle)) {
			vehicle.setContainerUnit(this);
			return true;
		}
		return false;
	}

	/**
	 * Removes a parked vehicle
	 *
	 * @param vehicle
	 * @param true if the parked vehicle can be removed
	 */
	public boolean removeParkedVehicle(Vehicle vehicle) {
		if (!parkedVehicles.contains(vehicle))
			return true;
		if (parkedVehicles.remove(vehicle)) {
			return true;
		}
		return false;
	}

	/**
	 * Does it have this vehicle parked at the settlement ?
	 *
	 * @param vehicle
	 * @return
	 */
	public boolean containsParkedVehicle(Vehicle vehicle) {
		if (parkedVehicles.contains(vehicle)) {
			return true;
		}
		return false;
	}

	/**
	 * Adds a vehicle into ownership
	 *
	 * @param vehicle
	 * @param true if the vehicle can be added
	 */
	public boolean addOwnedVehicle(Vehicle vehicle) {
		if (ownedVehicles.contains(vehicle))
			return true;
		if (ownedVehicles.add(vehicle)) {
			addParkedVehicle(vehicle);
			vehicle.setCoordinates(getCoordinates());
			vehicle.setContainerUnit(this);
			numOwnedVehicles = ownedVehicles.size();
			return true;
		}
		return false;
	}

	/**
	 * Removes a vehicle from ownership
	 *
	 * @param vehicle
	 * @param true if the vehicle can be removed
	 */
	public boolean removeOwnedVehicle(Vehicle vehicle) {
		if (!ownedVehicles.contains(vehicle))
			return true;
		if (ownedVehicles.remove(vehicle)) {
			numOwnedVehicles = ownedVehicles.size();
			return true;
		}
		return false;
	}

	/**
	 * Adds an equipment to be owned by the settlement
	 *
	 * @param e
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			e.setCoordinates(getCoordinates());
			e.setContainerUnit(this);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Removes an equipment from being owned by the settlement
	 *
	 * @param e
	 */
	@Override
	public boolean removeEquipment(Equipment e) {
		if (eqmInventory.removeEquipment(e)) {
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Finds all of the containers (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findAllContainers() {
		return eqmInventory.findAllContainers();
	}

	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findContainersOfType(EquipmentType type){
		return eqmInventory.findContainersOfType(type);
	}

	/**
	 * Gets all robots owned by this settlement, even if they are out on
	 * missions.
	 *
	 * @return collection of owned robots.
	 */
	public Collection<Robot> getAllAssociatedRobots() {
		return ownedRobots;
	}

	/**
	 * Gets the number of associated bots with this settlement
	 *
	 * @return the number of associated bots.
	 */
	public int getNumBots() {
		return numOwnedBots;
	}

	/**
	 * Gets all vehicles associated with this settlement, even if they are out on
	 * missions.
	 *
	 * @return collection of associated vehicles.
	 */
	public Collection<Vehicle> getAllAssociatedVehicles() {
		return ownedVehicles;
	}

	/**
	 * Gets all equipment associated with this settlement, even if they are out on
	 * missions.
	 *
	 * @return collection of associated equipment.
	 */
	public Collection<Equipment> getAllAssociatedEquipment() {
		return getEquipmentSet();
	}

	/**
	 * Gets all associated vehicles currently already embarked on missions out there
	 * (include vehicles doing building construction/salvage missions in a settlement)
	 *
	 * @return collection of vehicles.
	 */
	public Collection<Vehicle> getMissionVehicles() {
		return ownedVehicles.stream()
				.filter(v -> v.getMission() != null
					&& (v.getSettlement() == null
					|| v.getMission().getMissionType() == MissionType.BUILDING_CONSTRUCTION
					|| v.getMission().getMissionType() == MissionType.BUILDING_SALVAGE))
				.collect(Collectors.toList());

//		Collection<Vehicle> result = new ArrayList<>();
//		Iterator<Mission> i = missionManager.getMissionsForSettlement(this).iterator();
//		while (i.hasNext()) {
//			Mission mission = i.next();
//			if (mission instanceof VehicleMission) {
//				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
//				if ((vehicle != null)
//						&& !result.contains(vehicle)
//						&& this.equals(vehicle.getAssociatedSettlement()))
//					result.add(vehicle);
//			}
//
//			else if (mission.getMissionType() == MissionType.BUILDING_CONSTRUCTION) {
//				result.addAll(((BuildingConstructionMission) mission).getConstructionVehicles());
//
//			}
//		}
//
//		return result;
	}

	/**
	 * Gets numbers vehicles currently on mission
	 *
	 * @return numbers of vehicles on mission.
	 */
	public int getMissionVehicleNum() {
		return Math.toIntExact(ownedVehicles
				.stream()
				.filter(v -> v.getMission() != null)
				.collect(Collectors.counting()));
	}

	/**
	 * Gets a collection of drones parked or garaged at the settlement.
	 *
	 * @return Collection of parked drones
	 */
	public Collection<Drone> getParkedDrones() {
		return parkedVehicles.stream()
				.filter(v -> v.getVehicleType() == VehicleType.DELIVERY_DRONE)
				.map(Drone.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Gets a collection of drones parked or garaged at the settlement.
	 *
	 * @return Collection of parked drones
	 */
	public Collection<Unit> getVehicleTypeList(VehicleType vehicleType) {
		return ownedVehicles.stream()
				.filter(v -> v.getVehicleType() == vehicleType)
				.map(Unit.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Finds the number of vehicles of a particular type
	 *
	 * @param vehicleType the vehicle type.
	 * @return number of vehicles.
	 */
	public int findNumVehiclesOfType(VehicleType vehicleType) {
		return Math.toIntExact(ownedVehicles
					.stream()
					.filter(v -> v.getVehicleType() == vehicleType)
					.collect(Collectors.counting()));
	}

	/**
	 * Finds the number of parked rovers
	 *
	 * @return number of parked rovers.
	 */
	public int findNumParkedRovers() {
		return Math.toIntExact(parkedVehicles
					.stream()
					.filter(v -> v.getVehicleType() == VehicleType.CARGO_ROVER
					|| v.getVehicleType() == VehicleType.EXPLORER_ROVER
					|| v.getVehicleType() == VehicleType.TRANSPORT_ROVER)
					.collect(Collectors.counting()));
	}

	/**
	 * Gets a collection of vehicles parked or garaged at the settlement.
	 *
	 * @return Collection of parked vehicles
	 */
	public Collection<Vehicle> getParkedVehicles() {
		return parkedVehicles;
	}

	/**
	 * Gets the number of vehicles parked or garaged at the settlement.
	 *
	 * @return parked vehicles number
	 */
	public int getParkedVehicleNum() {
		return getParkedVehicles().size();
	}

	/**
	 * Gets the number of vehicles owned by the settlement.
	 *
	 * @return number of owned vehicles
	 */
	public int getOwnedVehicleNum() {
		return numOwnedVehicles;
	}

	/**
	 * Sets the process override flag.
	 *
	 * @param type Name of process type
	 * @param override override for processes.
	 */
	public void setProcessOverride(OverrideType type, boolean override) {
		if (override) {
			this.processOverrides.add(type);
		}
		else {
			this.processOverrides.remove(type);
		}
	}

	/**
	 * Gets the process override flag.
	 *
	 * @param type Name of process type
	 * @return Is this override flag set
	 */
	public boolean getProcessOverride(OverrideType type) {
		return this.processOverrides .contains(type);
	}

	/**
	 * Gets the settlement's construction manager.
	 *
	 * @return construction manager.
	 */
	public ConstructionManager getConstructionManager() {
		return constructionManager;
	}

	/**
	 * Gets the settlement's achievement credit for a given scientific field.
	 *
	 * @param science the scientific field.
	 * @return achievement credit.
	 */
	public double getScientificAchievement(ScienceType science) {
		double result = 0D;

		if (scientificAchievement.containsKey(science))
			result = scientificAchievement.get(science);

		return result;
	}

	/**
	 * Gets the settlement's total scientific achievement credit.
	 *
	 * @return achievement credit.
	 */
	public double getTotalScientificAchievement() {
		double result = 0D;

		Iterator<Double> i = scientificAchievement.values().iterator();
		while (i.hasNext())
			result += i.next();

		return result;
	}

	/**
	 * Add achievement credit to the settlement in a scientific field.
	 * Must be synchronized because Scientific Research is cross-Settlement
	 * @param achievementCredit the achievement credit.
	 * @param science           the scientific field.
	 */
	public synchronized void addScientificAchievement(double achievementCredit, ScienceType science) {
		if (scientificAchievement.containsKey(science))
			achievementCredit += scientificAchievement.get(science);

		scientificAchievement.put(science, achievementCredit);
	}

	/**
	 * Gets the initial population of the settlement.
	 *
	 * @return initial population number.
	 */
	public int getInitialPopulation() {
		return initialPopulation;
	}

	/**
	 * Gets the initial number of robots the settlement.
	 *
	 * @return initial number of robots.
	 */
	public int getProjectedNumOfRobots() {
		return projectedNumOfRobots;
	}

	/**
	 * Returns the chain of command
	 *
	 * @return chainOfCommand
	 */
	public ChainOfCommand getChainOfCommand() {
		return chainOfCommand;
	}

	/**
	 * Decrements a particular shift type
	 *
	 * @param shiftType
	 */
	private void decrementShiftType(ShiftType shiftType) {

		if (shiftType.equals(ShiftType.A)) {
			numA--;
		}

		else if (shiftType.equals(ShiftType.B)) {
			numB--;
		}

		else if (shiftType.equals(ShiftType.X)) {
			numX--;
		}

		else if (shiftType.equals(ShiftType.Y)) {
			numY--;
		}

		else if (shiftType.equals(ShiftType.Z)) {
			numZ--;
		}

		else if (shiftType.equals(ShiftType.ON_CALL)) {
			numOnCall--;
		}

		else if (shiftType.equals(ShiftType.OFF)) {
			numOff--;
		}

	}

	/**
	 * Increments a particular shift type
	 *
	 * @param shiftType
	 */
	private void incrementShiftType(ShiftType shiftType) {

		if (shiftType == ShiftType.A) {
			numA++;
		}

		else if (shiftType == ShiftType.B) {
			numB++;
		}

		else if (shiftType == ShiftType.X) {
			numX++;
		}

		else if (shiftType == ShiftType.Y) {
			numY++;
		}

		else if (shiftType == ShiftType.Z) {
			numZ++;
		}

		else if (shiftType == ShiftType.ON_CALL) {
			numOnCall++;
		}

		else if (shiftType == ShiftType.OFF) {
			numOff++;
		}
	}

	/**
	 * Checks if a particular work shift has been saturated
	 *
	 * @param st                The ShiftType
	 * @param inclusiveChecking
	 * @return true/false
	 */
	private boolean isWorkShiftSaturated(ShiftType st, boolean inclusiveChecking) {
		boolean result = false;

		// Reduce the shiftType of interest to find out if it's saturated
		if (inclusiveChecking)
			decrementShiftType(st);

		int pop = getNumCitizens();// getIndoorPeopleCount();
		int quotient = pop / numShiftsCache;
		int remainder = pop % numShiftsCache;

		switch (numShiftsCache) {
		case 1: // (numShift == 1)
			if (st != ShiftType.ON_CALL)
				result = false;
			break;
		case 2: // else if (numShift == 2) {

			switch (remainder) {
			case 0: // if (remainder == 0) {
				if (numA < quotient && numB < quotient) {
					result = true;
					break;
				}
				if (quotient == 1) {
					if (numA < 1) { // allow only 1 person with "A shift"
						if (st == ShiftType.A)
							result = true;
					} else {
						if (st == ShiftType.B)
							result = true;
					}
				} else if (quotient == 2) {
					if (numA < 2) { // allow 2 persons with "A shift"
						if (st == ShiftType.A)
							result = true;
					} else {
						if (st == ShiftType.B)
							result = true;
					}
				}
				break;
			case 1: // else { //if (remainder == 1) {
				if (numA < quotient && numB < quotient) {
					result = true;
					break;
				}
				if (quotient == 1) {
					if (numA < 2) { // allow 1 person with "A shift"
						if (st == ShiftType.A)
							result = true;
					} else {
						if (st == ShiftType.B)
							result = true;
					}
				} else if (quotient == 2) {
					if (numA < 3) { // allow 2 persons with "A shift"
						if (st == ShiftType.A)
							result = true;
					} else {
						if (st == ShiftType.B)
							result = true;
					}
				}
				break;
			} // end of switch (remainder)
			break;
		case 3: // else if (numShift == 3) {
			switch (remainder) {
			case 0: // if (remainder == 0) {
				if (numX < quotient && numY < quotient && numZ < quotient) {
					result = true;
					break;
				}
				if (numX < quotient + 1) { // allow up to q persons with "X shift"
					if (st == ShiftType.X)
						result = true;
				} else if (numY < quotient + 1) { // allow up to q persons with "Y shift"
					if (st == ShiftType.Y)
						result = true;
				} else {
					if (st == ShiftType.Z)
						result = true;
				}
				break;
			case 1: // else if (remainder == 1) {
				if (numX < quotient && numY < quotient && numZ < quotient) {
					result = true;
					break;
				}
				if (numX < quotient + 1) { // allow up to q persons with "X shift"
					if (st == ShiftType.X)
						result = true;
				} else if (numY < quotient + 2) { // allow up to q + 1 persons with "Y shift"
					if (st == ShiftType.Y)
						result = true;
				} else {
					if (st == ShiftType.Z)
						result = true;
				}
				break;
			case 2: // else {//if (remainder == 2) {
				if (numX < quotient && numY < quotient && numZ < quotient) {
					result = true;
					break;
				}
				if (numX < quotient + 2) { // allow up to q+1 persons with "X // shift"
					if (st == ShiftType.X)
						result = true;
				} else if (numY < quotient + 2) { // allow up to q+1 persons with "Y shift"
					if (st == ShiftType.Y)
						result = true;
				} else {
					if (st == ShiftType.Z)
						result = true;
				}
				break;
			} // end of switch for case 3
			break;
		} // end of switch

		// fill back the shiftType of interest
		if (inclusiveChecking)
			incrementShiftType(st);

		return result;
	}

	/**
	 * Finds an empty work shift in a settlement
	 *
	 * @param pop If it wasn't known, use -1 to obtain the latest figure
	 * @return The ShifType
	 */
	public ShiftType getAnEmptyWorkShift(int population) {
		int pop = 0;
		if (population == -1)
			pop = this.getNumCitizens();// getIndoorPeopleCount();
		else
			pop = population;

		int rand = -1;
		ShiftType shiftType = ShiftType.OFF;
		int quotient = pop / numShiftsCache;
		int remainder = pop % numShiftsCache;

		int limX = 0;
		int limY = 0;
		int limZ = 0;

		limX = quotient;

		if (remainder == 0)
			limZ = limX + 1;
		else
			limZ = limX + 2;

		limY = pop - limX - limZ;

		switch (numShiftsCache) {

		case 1: // for numShift = 1
			shiftType = ShiftType.ON_CALL;
			break;

		case 2: // for umShift = 2
			switch (remainder) {
			case 0: // if (remainder == 0) {
				if (numA < quotient && numB < quotient) {
					rand = RandomUtil.getRandomInt(1);
					if (rand == 0) {
						shiftType = ShiftType.A;
					} else if (rand == 1) {
						shiftType = ShiftType.B;
					}
					break;
				}
				if (quotient == 1) {
					if (numA < 1) { // allow only 1 person with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				} else if (quotient == 2) {
					if (numA < 2) { // allow 2 persons with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				}
				break;

			case 1: // else { //if (remainder == 1) {
				if (numA < quotient && numB < quotient) {
					rand = RandomUtil.getRandomInt(1);
					if (rand == 0) {
						shiftType = ShiftType.A;
					} else if (rand == 1) {
						shiftType = ShiftType.B;
					}
					break;
				}
				if (quotient == 1) {
					if (numA < 2) { // allow 1 person with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				} else if (quotient == 2) {
					if (numA < 3) { // allow 2 persons with "A shift"
						shiftType = ShiftType.A;
					} else {
						shiftType = ShiftType.B;
					}
					break;
				}
				break;

			} // end of switch (remainder)
			break;

		case 3: // for numShift = 3

			if (numX < limX && numZ < limZ) {
				rand = RandomUtil.getRandomInt(3);
				if (rand == 0)
					shiftType = ShiftType.X;
				else if (rand == 1)
					shiftType = ShiftType.Z;
				else
					shiftType = ShiftType.Y;
			} else if (numX < limX) {
				rand = RandomUtil.getRandomInt(2);
				if (rand == 0)
					shiftType = ShiftType.X;
				else {
					if (numY <= limY)
						shiftType = ShiftType.Y;
					else
						shiftType = ShiftType.X;
				}

			} else if (numZ < limZ) {
				rand = RandomUtil.getRandomInt(2);
				if (rand == 0)
					shiftType = ShiftType.Z;
				else {
					if (numY <= limY)
						shiftType = ShiftType.Y;
					else
						shiftType = ShiftType.Z;
				}
			} else
				shiftType = ShiftType.Y;

			break;
		} // end of switch

		return shiftType;
	}

	/**
	 * Sets the number of shift of a settlement
	 *
	 * @param numShift
	 */
	public void setNumShift(int numShift) {
		this.numShiftsCache = numShift;
	}

	/**
	 * Gets the current number of work shifts in a settlement
	 *
	 * @return a number, either 2 or 3
	 */
	public int getNumShift() {
		return numShiftsCache;
	}

	/*
	 * Increments the number of people in a particular work shift
	 *
	 * @param shiftType
	 */
	public void incrementAShift(ShiftType shiftType) {
		if (shiftType != null) {
			if (shiftType == ShiftType.A)
				numA++;
			else if (shiftType == ShiftType.B)
				numB++;
			else if (shiftType == ShiftType.X)
				numX++;
			else if (shiftType == ShiftType.Y)
				numY++;
			else if (shiftType == ShiftType.Z)
				numZ++;
			else if (shiftType == ShiftType.ON_CALL)
				numOnCall++;
			else if (shiftType == ShiftType.OFF)
				numOff++;
		}
	}

	/*
	 * Decrements the number of people in a particular work shift
	 *
	 * @param shiftType
	 */
	public void decrementAShift(ShiftType shiftType) {
		if (shiftType != null) {
			if (shiftType == ShiftType.A)
				numA--;
			else if (shiftType == ShiftType.B)
				numB--;
			else if (shiftType == ShiftType.X)
				numX--;
			else if (shiftType == ShiftType.Y)
				numY--;
			else if (shiftType == ShiftType.Z)
				numZ--;
			else if (shiftType == ShiftType.ON_CALL)
				numOnCall--;
			else if (shiftType == ShiftType.OFF)
				numOff--;
		}
	}

	public boolean[] getExposed() {
		return exposed;
	}

	/*
	 * Compute the probability of radiation exposure during EVA/outside walk
	 */
	private void checkRadiationProbability(double time) {
		// boolean[] exposed = {false, false, false};
		// double exposure = 0;
		double ratio = time / RadiationExposure.RADIATION_CHECK_FREQ;
		double mag_variation1 = 1 + RandomUtil.getRandomDouble(RadiationExposure.GCR_CHANCE_SWING)
				- RandomUtil.getRandomDouble(RadiationExposure.GCR_CHANCE_SWING);
		if (mag_variation1 < 0)
			mag_variation1 = 0;
		double mag_variation2 = 1 + RandomUtil.getRandomDouble(RadiationExposure.SEP_CHANCE_SWING)
				- RandomUtil.getRandomDouble(RadiationExposure.SEP_CHANCE_SWING);
		if (mag_variation2 < 0)
			mag_variation2 = 0;

		// Galactic cosmic rays (GCRs) event
		double chance1 = RadiationExposure.GCR_PERCENT * ratio * mag_variation1; // normally 1.22%
		// Solar energetic particles (SEPs) event
		double chance2 = RadiationExposure.SEP_PERCENT * ratio * mag_variation2; // 0.122 %
		// Baseline radiation event
		double chance0 = 100 - chance1 - chance2;
		// Note that RadiationExposure.BASELINE_PERCENT * ratio * (variation1 + variation2);
		// average 3.53%

		if (chance0 < 0)
			chance0 = 0;

		// Galactic cosmic rays (GCRs) event
		// double rand2 = Math.round(RandomUtil.getRandomDouble(100) * 100.0)/100.0;
		if (RandomUtil.lessThanRandPercent(chance1)) {
			exposed[1] = true;
			logger.log(this, Level.INFO, 1_000, DETECTOR_GRID + UnitEventType.GCR_EVENT.toString() + " is imminent.");
			this.fireUnitUpdate(UnitEventType.GCR_EVENT);
		} else
			exposed[1] = false;

		// ~ 300 milli Sieverts for a 500-day mission
		// Solar energetic particles (SEPs) event
		if (RandomUtil.lessThanRandPercent(chance2)) {
			exposed[2] = true;
			logger.log(this, Level.INFO, 1_000, DETECTOR_GRID + UnitEventType.SEP_EVENT.toString() + " is imminent.");
			this.fireUnitUpdate(UnitEventType.SEP_EVENT);
		} else
			exposed[2] = false;
	}

	public CompositionOfAir getCompositionOfAir() {
		return compositionOfAir;
	}

	/**
	 * Computes the water ration level at the settlement due to low water supplies.
	 *
	 * @return level of water ration.
	 */
	public int getWaterRationLevel() {
		return waterRationLevel;
	}

	/**
	 * Computes the water ration level at the settlement due to low water supplies.
	 *
	 * @return level of water ration.
	 */
	private void computeWaterRationLevel() {
		double storedWater = Math.max(1, getAmountResourceStored(WATER_ID) - getNumCitizens() * 500.0);
		double requiredDrinkingWaterOrbit = water_consumption_rate * getNumCitizens()
				* MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;

		double ratio = requiredDrinkingWaterOrbit / storedWater;
		waterRationLevel = (int) ratio;

		if (waterRationLevel < 1)
			waterRationLevel = 1;
		else if (waterRationLevel > 1000)
			waterRationLevel = 1000;

		if (waterRationLevel > 100)
			logger.severe(this, 20_000L, "Water Ration Level: " + waterRationLevel);//Math.round(ratio * 10.0)/10.0);
	}

	/**
	 * Sets the objective
	 *
	 * @param {@link ObjectiveType}
	 * @param level
	 */
	public void setObjective(ObjectiveType objectiveType, int level) {
		this.objectiveType = objectiveType;
		double lvl = 1.25 * level;

		// reset all to 1
		goodsManager.setCropFarmFactor(1);
		goodsManager.setManufacturingFactor(1);
		goodsManager.setResearchFactor(1);
		goodsManager.setTransportationFactor(1);
		goodsManager.setTradeFactor(1);

		if (objectiveType == ObjectiveType.CROP_FARM) {
			goodsManager.setCropFarmFactor(lvl);
		}

		else if (objectiveType == ObjectiveType.MANUFACTURING_DEPOT) {
			goodsManager.setManufacturingFactor(lvl);
		}

		else if (objectiveType == ObjectiveType.RESEARCH_CAMPUS) {
			goodsManager.setResearchFactor(lvl);
		}

		else if (objectiveType == ObjectiveType.TRANSPORTATION_HUB) {
			goodsManager.setTransportationFactor(lvl);
		}

		else if (objectiveType == ObjectiveType.TRADE_CENTER) {
			goodsManager.setTradeFactor(lvl);
		}

		else if (objectiveType == ObjectiveType.TOURISM) {
			goodsManager.setTourismFactor(lvl);
		}

	}

	/**
	 * Gets the objective level
	 *
	 * @param {@link ObjectiveType}
	 * @return the level
	 */
	public double getObjectiveLevel(ObjectiveType objectiveType) {

		if (objectiveType == ObjectiveType.CROP_FARM) {
			return goodsManager.getCropFarmFactor();
		}

		else if (objectiveType == ObjectiveType.MANUFACTURING_DEPOT) {
			return goodsManager.getManufacturingFactor();
		}

		else if (objectiveType == ObjectiveType.RESEARCH_CAMPUS) {
			return goodsManager.getResearchFactor();
		}

		else if (objectiveType == ObjectiveType.TRANSPORTATION_HUB) {
			return goodsManager.getTransportationFactor();
		}

		else if (objectiveType == ObjectiveType.TRADE_CENTER) {
			return goodsManager.getTradeFactor();
		}

		else if (objectiveType == ObjectiveType.TOURISM) {
			return goodsManager.getTourismFactor();
		}

		return -1;
	}

	/**
	 * Gets the objective
	 */
	public ObjectiveType getObjective() {
		return objectiveType;
	}

	/**
	 * Gets the building type related to the objective
	 *
	 * @return
	 */
	public String getObjectiveBuildingType() {

		// TODO: check if a particular building has existed, if yes, build the next
		// relevant building
		if (objectiveType == ObjectiveType.CROP_FARM)
			return "inflatable greenhouse";// "inground greenhouse";//"Inflatable Greenhouse";
		// alternatives : "Large Greenhouse"
		else if (objectiveType == ObjectiveType.MANUFACTURING_DEPOT)
			return "manufacturing shed";// "Workshop";
		// alternatives : "Manufacturing Shed", MD1, MD4
		else if (objectiveType == ObjectiveType.RESEARCH_CAMPUS)
			return "mining lab"; // Laboratory";
		// alternatives : "Mining Lab", "Astronomy Observatory"
		else if (objectiveType == ObjectiveType.TRANSPORTATION_HUB)
			return "loading dock garage";
		// alternatives :"Garage";
		else if (objectiveType == ObjectiveType.TRADE_CENTER)
			return "storage shed";
		else if (objectiveType == ObjectiveType.TOURISM)
			return "loading dock garage";
		else
			return null;
	}

	/**
	 * Gets the number of crops that currently need work this Sol.
	 *
	 * @return number of crops.
	 */
	public int getCropsNeedingTending() {
		return cropsNeedingTendingCache;
	}

	/**
	 * Calculate if the crops need tending. Add a buffer of 5 millisol.
	 * @param now Current time
	 */
	private void doCropsNeedTending(ClockPulse now) {

		int m = now.getMarsTime().getMillisolInt();

		// Check for the day rolling over
		if (now.isNewSol() || (millisolCache + 5) < m) {
			millisolCache = m;
			cropsNeedingTendingCache = 0;
			for (Building b : buildingManager.getBuildings(FunctionType.FARMING)) {
				Farming farm = b.getFarming();
				cropsNeedingTendingCache += farm.getNumNeedTending();
			}
		}
	}

	/***
	 * Computes the probability of the presence of regolith
	 *
	 * @return probability of finding regolith
	 */
	private double computeRegolithProbability() {
		double result = 0;

		double regolith_value = goodsManager.getGoodValuePerItem(REGOLITH_ID);
		regolith_value = regolith_value * GoodsManager.REGOLITH_VALUE_MODIFIER;
		if (regolith_value > REGOLITH_MAX)
			regolith_value = REGOLITH_MAX;
		else if (regolith_value < 0)
			return 0;

		double sand_value = goodsManager.getGoodValuePerItem(SAND_ID);
		sand_value = sand_value * GoodsManager.SAND_VALUE_MODIFIER;
		if (sand_value > REGOLITH_MAX)
			sand_value = REGOLITH_MAX;
		else if (sand_value < 0)
			return 0;

		int pop = numCitizens;

		double regolith_available = getAmountResourceStored(REGOLITH_ID);
		double sand_available = getAmountResourceStored(SAND_ID);

		if (regolith_available < MIN_REGOLITH_RESERVE * pop + regolith_value / 10
				|| sand_available < MIN_SAND_RESERVE * pop + sand_value / 10) {
			result = (MIN_REGOLITH_RESERVE * pop - regolith_available + regolith_value) / 10;
		}

		// Prompt the regolith mission if regolith resource is low,
		if (regolith_available > MIN_REGOLITH_RESERVE * pop) {
			// no change to missionProbability
		}
		else {
			result = 5 * (MIN_REGOLITH_RESERVE * pop - regolith_available);
		}

		if (result < 0)
			result = 0;

		return result;
	}

	/***
	 * Computes the probability of the presence of ice
	 *
	 * @return probability of finding ice
	 */
	private double computeIceProbability() {
		double result = 0;

		double ice_value = goodsManager.getGoodValuePerItem(ICE_ID);

		if (ice_value > ICE_MAX)
			ice_value = ICE_MAX;
		if (ice_value < 1)
			ice_value = 1;

		double water_value = goodsManager.getGoodValuePerItem(WATER_ID);
		water_value = water_value * waterRationLevel;
		if (water_value > WATER_MAX)
			water_value = WATER_MAX;
		if (water_value < 1)
			water_value = 1;

		// Compare the available amount of water and ice reserve
		double ice_available =  getAmountResourceStored(ICE_ID);
		double water_available =  Math.log(1 + getAmountResourceStored(WATER_ID));

		int pop = numCitizens;

		// Create a task to find local ice and simulate the probability of finding
		// local ice and its quantity
		if (ice_available < MIN_ICE_RESERVE * pop + ice_value / 10D
				&& water_available < MIN_WATER_RESERVE * pop + water_value / 10D) {
			result = (MIN_ICE_RESERVE * pop - ice_available
					+ MIN_WATER_RESERVE * pop - water_available)
					+ water_value + ice_value;
		}

		// Prompt the collect ice mission to proceed more easily if water resource is
		// dangerously low,
		if (water_available > MIN_WATER_RESERVE * pop) {
			// no change to missionProbability
		}
		else {
			result = .1 * (MIN_WATER_RESERVE * pop - water_available);
		}

		return result;
	}

	/**
	 * Checks if the last 20 mission scores are above the threshold
	 *
	 * @param score
	 * @return true/false
	 */
	public boolean passMissionScore(double score) {

		return (score > minimumPassingScore);
	}

	/**
	 * Calculates the current minimum passing score
	 *
	 * @return
	 */
	public double getMinimumPassingScore() {
		return minimumPassingScore;
	}

	/**
	 * Saves the mission score
	 *
	 * @param score
	 */
	public void saveMissionScore(double score) {

		// Recalculate the new minimum score; minimum score ages in the timePulse method
		double total = 0;
		for (double s : missionScores) {
			total += s;
		}

		// Simplify how minimum score is calculated. Use of trending scores
		// seems to make it harder to get missions approved over time
		minimumPassingScore = total / missionScores.size();

		// Cap any very large score to protect the average
		double desiredMax = Math.min(MAX_MISSION_SCORE, minimumPassingScore * 1.5D);
		missionScores.add(Math.min(score, desiredMax));

		if (missionScores.size() > 20)
			missionScores.remove(0);
	}

	public double getIceProbabilityValue() {
		return iceProbabilityValue;
	}

	public double getRegolithProbabilityValue() {
		return regolithProbabilityValue;
	}

	public double getOxygenProbabilityValue() {
		return oxygenProbabilityValue;
	}

	public double getMethaneProbabilityValue() {
		return methaneProbabilityValue;
	}

	public double getOutsideTemperature() {
		return outside_temperature;
	}

	public DustStorm getDustStorm() {
		return storm;
	}

	public void setDustStorm(DustStorm storm) {
		this.storm = storm;
	}

	public int getMissionRadius(MissionType missionType) {
		return missionRange.getOrDefault(missionType, 1000);
	}

	public void setMissionRadius(MissionType missionType, int newRange) {
		missionRange.put(missionType, newRange);
	}

	public boolean hasDesignatedCommander() {
		return hasDesignatedCommander;
	}

	public Person setDesignatedCommander(Commander profile) {
		hasDesignatedCommander = true;

		return chainOfCommand.applyCommander(profile);
	}

	private void changeGreyWaterFilteringRate(boolean increase) {
		if (increase && (greyWaterFilteringRate < 100)) {
			greyWaterFilteringRate = 1.1 * greyWaterFilteringRate;
		}
		else if (!increase && (greyWaterFilteringRate > .01)) {
			greyWaterFilteringRate = 0.9 * greyWaterFilteringRate;
		}
	}

	public double getGreyWaterFilteringRate() {
		return greyWaterFilteringRate;
	}

	/**
	 * Records the daily output.
	 *
	 * @param resource the resource id of the good
	 * @param amount the amount or quantity produced
	 * @param millisols the labor time
	 */
	public void addOutput(Integer resource, double amount, double millisols) {

		// Record the amount of resource produced
		dailyResourceOutput.increaseDataPoint(resource, amount);
		dailyLaborTime.increaseDataPoint(resource, millisols);
	}

	/**
	 * Records the amount of water being consumed.
	 *
	 * @param type
	 * @param amount
	 */
	public void addWaterConsumption(WaterUseType type, double amount) {
		waterConsumption.increaseDataPoint(type, amount);
	}

	/**
	 * Gets the daily average water usage of the last x sols Not: most weight on
	 * yesterday's usage. Least weight on usage from x sols ago
	 *
	 * @return
	 */
	public double getDailyWaterUsage(WaterUseType type) {
		return waterConsumption.getDailyAverage(type);
	}

	private static void assignBestCandidate(Settlement settlement, JobType job) {
		Person p0 = JobUtil.findBestFit(settlement, job);
		// Designate a specific job to a person
		if (p0 != null) {
			p0.getMind().assignJob(job, true, JobUtil.SETTLEMENT, JobAssignmentType.APPROVED, JobUtil.SETTLEMENT);
		}
	}

	/**
	 * Tune up the settlement with unique job position
	 */
	public void tuneJobDeficit() {
		int numEngs = JobUtil.numJobs(JobType.ENGINEER, this);
		int numTechs = JobUtil.numJobs(JobType.TECHNICIAN, this);

		if ((numEngs == 0) || (numTechs == 0)) {
			Person bestEng = JobUtil.findBestFit(this, JobType.ENGINEER);
			Person bestTech = JobUtil.findBestFit(this, JobType.TECHNICIAN);

			// Make sure the best person is not the only one in the other Job
			if ((bestEng != null) && bestEng.equals(bestTech)) {
				// Can only do one so job
				if (numEngs == 0) {
					// Keep the bestEng find
					bestTech = null;
				}
				else if (numTechs == 0) {
					// Keep best tech Loose the eng
					bestEng = null;
				}
			}
			if ((numEngs == 0) && (bestEng != null)) {
				bestEng.getMind().assignJob(JobType.ENGINEER, true,
						JobUtil.SETTLEMENT,
						JobAssignmentType.APPROVED,
						JobUtil.SETTLEMENT);
			}
			if ((numTechs == 0) && (bestTech != null)) {
				bestTech.getMind().assignJob(JobType.TECHNICIAN, true,
						JobUtil.SETTLEMENT,
						JobAssignmentType.APPROVED,
						JobUtil.SETTLEMENT);
			}
		}


		if (this.getNumCitizens() > ChainOfCommand.POPULATION_WITH_CHIEFS) {
			int numWeatherman = JobUtil.numJobs(JobType.METEOROLOGIST, this);
			if (numWeatherman == 0) {
				assignBestCandidate(this, JobType.METEOROLOGIST);
			}
		}
	}

	public void setMissionDisable(MissionType mission, boolean disable) {
		if (disable) {
			disabledMissions.add(mission);
		}
		else {
			disabledMissions.remove(mission);
		}
	}

	public boolean isMissionDisable(MissionType mission) {
		return disabledMissions.contains(mission);
	}

	public void setAllowTradeMissionFromASettlement(Settlement settlement, boolean allowed) {
		allowTradeMissionSettlements.put(settlement.getIdentifier(), allowed);
	}

	public void setTradeMissionFromAllSettlements(boolean allowed) {
		for (Settlement s: unitManager.getSettlements()) {
			allowTradeMissionSettlements.put(s.getIdentifier(), allowed);
		}
	}

	/**
	 * Calculate the base mission probability used by all missions
	 *
	 * @param mission the type of the mission calling this method
	 * @return probability value
	 */
	public boolean getMissionBaseProbability(MissionType mission) {

		if (disabledMissions.contains(mission)) {
			return false;
		}

		// If the Settlement mission probably is false; then recheck it
		// This get reset in the timePulse method
		if (!missionProbability) {
			missionProbability = isMissionPossible();
		}

		return missionProbability;
	}


	/**
	 * Can this settlement start a mission ?
	 *
	 * @return
	 */
	public boolean isMissionPossible() {

		if (!missionProbability) {
			// 1. Check if a mission-capable rover is available.
			if (!RoverMission.areVehiclesAvailable(this, false)) {
				return false;
			}
			// 2. Check if available backup rover.
			if (!RoverMission.hasBackupRover(this)) {
				return false;
			}
			// 3. Check if at least 1 person is there
			// A settlement with <= 4 population can always do DigLocalRegolith task
			// should avoid the risk of mission.
			if (getIndoorPeopleCount() <= 1) {// .getAllAssociatedPeople().size() <= 4)
				return false;
			}
			// 4. Check if minimum number of people are available at the settlement.
			if (!RoverMission.minAvailablePeopleAtSettlement(this, RoverMission.MIN_STAYING_MEMBERS)) {
				return false;
			}

			// 5. Check if min number of EVA suits at settlement.
			if (findNumContainersOfType(EquipmentType.EVA_SUIT) < RoverMission.MIN_GOING_MEMBERS) {
				return false;
			}

			// 6. Check if settlement has enough basic resources for a rover mission.
			if (!hasEnoughBasicResources(true)) {
				return false;
			}

			// 7. Check if starting settlement has minimum amount of methane fuel.
			if (getAmountResourceStored(METHANE_ID) < RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
				return false;
			}

			if (VehicleMission.numEmbarkingMissions(this) > getNumCitizens() / 4D) {
				return false;
			}

			if (VehicleMission.numApprovingMissions(this) > getNumCitizens() / 4D) {
				return false;
			}

			missionProbability = true;

		}

		return missionProbability;
	}

	public double getTotalMineralValue(Rover rover) {
		if (mineralValue == -1) {
			// Check if any mineral locations within rover range and obtain their
			// concentration
			Map<String, Double> minerals = Exploration.getNearbyMineral(rover, this);
			if (!minerals.isEmpty()) {
				mineralValue = Exploration.getTotalMineralValue(this, minerals);
			}
		}
		return mineralValue;
	}

	/**
	 * Checks if there are enough basic mission resources at the settlement to start
	 * mission.
	 *
	 * @param settlement the starting settlement.
	 * @return true if enough resources.
	 */
	private boolean hasEnoughBasicResources(boolean unmasked) {
		// if unmasked is false, it won't check the amount of H2O and O2.
		// the goal of this mission can potentially increase O2 & H2O of the settlement
		// e.g. an ice mission is desperately needed especially when there's
		// not enough water since ice will produce water.

		try {
			if (getAmountResourceStored(METHANE_ID) < minMethane) {
				return false;
			}
			if (unmasked && getAmountResourceStored(OXYGEN_ID) < mineOxygen) {
				return false;
			}
			if (unmasked && getAmountResourceStored(WATER_ID) < minWater) {
				return false;
			}
			if (getAmountResourceStored(FOOD_ID) < minFood) {
				return false;
			}
		} catch (Exception e) {
			logger.severe("Problems in hasEnoughBasicResources(): " + e.getMessage());
		}

		return true;
	}

    public double getIceCollectionRate() {
    	return iceCollectionRate;
    }

    public double getRegolithCollectionRate() {
    	return regolithCollectionRate;
    }

	public int getMissionDirectiveModifier(MissionType mission) {
		return missionModifiers.get(mission);
	}

	/**
	 * Remove the record of the deceased person from airlock
	 *
	 * @param person
	 */
	public void removeAirlockRecord(Person person) {
		List<Building> list = buildingManager.getBuildings(FunctionType.EVA);
		for (Building b : list) {
			Airlock lock = b.getEVA().getAirlock();
			lock.removeAirlockRecord(person);
		}
	}

	/**
	 * The total amount of rock samples collected.
	 * @param id resource id
	 */
	public double getResourceCollected(int id) {
		return resourcesCollected.get(id);
	}

	/**
	 * Adds the amount of resource collected.
	 * @param id resource id
	 * @param value collected
	 */
	public void addResourceCollected(int id, double value) {
		if (resourcesCollected.containsKey(id)) {
			double rs = resourcesCollected.get(id);
			resourcesCollected.put(id, rs + value);
		}
		else {
			resourcesCollected.put(id, value);
		}
	}

	/**
	 * Gets the buy list
	 *
	 * @return
	 */
	public List<Good> getBuyList() {
		return GoodsManager.getBuyList();
	}


	public int getSolCache() {
		return solCache;
	}

	public boolean isFirstSol() {
        return solCache == 0 || solCache == 1;
    }

	@Override
	public Settlement getSettlement() {
		return null;
	}

	/**
	 * Generate a unique name for the Settlement
	 * @return
	 */
	public static String generateName(ReportingAuthority sponsor) {
		List<String> remainingNames = new ArrayList<>(sponsor.getSettlementNames());

		List<String> usedNames = unitManager.getSettlements().stream()
							.map(s -> s.getName()).collect(Collectors.toList());

		remainingNames.removeAll(usedNames);
		int idx = RandomUtil.getRandomInt(remainingNames.size());

		return remainingNames.get(idx);
	}

	/**
	 * Gets the stored mass
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Get the equipment list
	 *
	 * @return the equipment list
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		return eqmInventory.getEquipmentSet();
	}

	/**
	 * Get a list of the equipment with particular equipment type
	 *
	 * @return the equipment list
	 */
	public Set<Equipment> getEquipmentTypeList(EquipmentType equipmentType) {
		return eqmInventory.getEquipmentSet().stream()
				.filter(e -> e.getEquipmentType() == equipmentType)
				.collect(Collectors.toSet());
	}

	/**
	 * Does it possess an equipment of this equipment type
	 *
	 * @param typeID
	 * @return true if this person possess this equipment type
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		return eqmInventory.containsEquipment(type);
	}

	/**
	 * Stores the item resource
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public int storeItemResource(int resource, int quantity) {
		return eqmInventory.storeItemResource(resource, quantity);
	}

	/**
	 * Retrieves the item resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return eqmInventory.retrieveItemResource(resource, quantity);
	}

	/**
	 * Gets the item resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return eqmInventory.getItemResourceStored(resource);
	}

	/**
	 * Stores the amount resource
	 *
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		return eqmInventory.storeAmountResource(resource, quantity);
	}

	/**
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		return eqmInventory.retrieveAmountResource(resource, quantity);
	}

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.getAmountResourceRemainingCapacity(resource);
	}

	/**
	 * Obtains the remaining general storage space
	 *
	 * @return quantity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return eqmInventory.getRemainingCargoCapacity();
	}

	/**
     * Gets the total capacity that this robot can hold.
     *
     * @return total capacity (kg).
     */
	@Override
	public double getCargoCapacity() {
		return eqmInventory.getCargoCapacity();
	}

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return eqmInventory.getAmountResourceStored(resource);
	}

	/**
	 * Gets all stored amount resources
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return eqmInventory.getAmountResourceIDs();
	}

	/**
	 * Gets all stored item resources
	 *
	 * @return all stored item resources.
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return eqmInventory.getItemResourceIDs();
	}

	/**
	 * Does it have this item resource ?
	 *
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasItemResource(int resource) {
		return eqmInventory.hasItemResource(resource);
	}

	/**
	 * Gets the remaining quantity of an item resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		return eqmInventory.getItemResourceRemainingQuantity(resource);
	}

	/**
	 * Finds the number of empty containers of a class that are contained in storage and have
	 * an empty inventory.
	 *
	 * @param containerClass  the unit class.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return eqmInventory.findNumEmptyContainersOfType(containerType, brandNew);
	}

	/**
	 * Finds the number of containers of a particular type
	 *
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return eqmInventory.findNumContainersOfType(containerType);
	}

	/**
	 * Finds a container in storage.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	@Override
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		return eqmInventory.findContainer(containerType, empty, resource);
	}

	/**
	 * Gets the EquipmentInventory instance.
	 * @return
	 */
	public EquipmentInventory getEquipmentInventory() {
		return eqmInventory;
	}

	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	@Override
	public void setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return;
			}
			// 1. Set Coordinates
			setCoordinates(newContainer.getCoordinates());
			// 2. Set LocationStateType
			currentStateType = LocationStateType.MARS_SURFACE;
			// 3. Set containerID
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}
		else {
			setContainerID(MARS_SURFACE_UNIT_ID);
		}
	}

	/**
	 * Gets the unit's container unit. Returns null if unit has no container unit.
	 *
	 * @return the unit's container unit
	 */
	@Override
	public Unit getContainerUnit() {
		if (unitManager == null) // for maven test
			return null;
		return unitManager.getMarsSurface();
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.SETTLEMENT;
	}

	/**
	 * Is this unit inside a settlement
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {
		return false;
	}

	/**
	 * Gets the holder's unit instance
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	/**
	 * Reinitialize references after loading from a saved sim
	 */
	public void reinit() {
		buildingManager.reinit();
	}

	@Override
	public void destroy() {
		super.destroy();

		if (buildingManager != null) {
			buildingManager.destroy();
		}
		buildingManager = null;
		if (buildingConnectorManager != null) {
			buildingConnectorManager.destroy();
		}
		buildingConnectorManager = null;
		if (goodsManager != null) {
			goodsManager.destroy();
		}
		goodsManager = null;
		if (constructionManager != null) {
			constructionManager.destroy();
		}
		constructionManager = null;
		if (powerGrid != null) {
			powerGrid.destroy();
		}
		powerGrid = null;

		if (thermalSystem != null) {
			thermalSystem.destroy();
		}
		thermalSystem = null;

		template = null;

		scientificAchievement = null;
	}
}
