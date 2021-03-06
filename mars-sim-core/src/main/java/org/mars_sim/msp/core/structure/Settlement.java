/**
 * Mars Simulation Project
 * Settlement.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.mars.DustStorm;
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
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.reportingAuthority.MissionSubAgenda;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnector;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
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

/**
 * The Settlement class represents a settlement unit on virtual Mars. It
 * contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements Serializable, Temporal, LifeSupportInterface, Objective {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Settlement.class.getName());

	private static final String DETECTOR_GRID = "The detector grid forecast a ";
	private static final String TRADING_OUTPOST = "Trading Outpost";
	private static final String MINING_OUTPOST = "Mining Outpost";
	private static final String ASTRONOMY_OBSERVATORY = "Astronomy Observatory";
	
//	public static final int CHECK_GOODS = 20;
	
	public static final int CHECK_MISSION = 20; // once every 10 millisols

	public static final int MAX_NUM_SOLS = 3;

	public static final int MAX_SOLS_DAILY_OUTPUT = 14;

	public static final int SUPPLY_DEMAND_REFRESH = 7;

	private static final int RESOURCE_UPDATE_FREQ = 50;

	private static final int CHECK_WATER_RATION = 100;
	
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

	public static final double MIN_WATER_RESERVE = 400D; // per person

	public static final double SAFE_TEMPERATURE_RANGE = 18;

	// public static final double SAFETY_PRESSURE = 20;

	/** Normal air pressure [in kPa] */
	private static final double NORMAL_AIR_PRESSURE = CompositionOfAir.SKYLAB_TOTAL_AIR_PRESSURE_kPA;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;
	
	/** The unit count for this settlement. */
	//private static int uniqueCount = Unit.FIRST_SETTLEMENT_UNIT_ID;
	
	/** The settlement water consumption */
	public static double water_consumption_rate;
	/** The settlement minimum air pressure requirement. */
	public static double minimum_air_pressure;
	/** The settlement life support requirements. */
	public static double[][] life_support_value = new double[2][7];
	
	/** Amount of time (millisols) required for periodic maintenance. */
	// private static final double MAINTENANCE_TIME = 1000D;
	
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
				ResourceUtil.oxygenID,
				ResourceUtil.hydrogenID,
				ResourceUtil.co2ID,
				ResourceUtil.methaneID,
				ResourceUtil.waterID,
//				ResourceUtil.greyWaterID,
//				ResourceUtil.blackWaterID,
				ResourceUtil.rockSamplesID,
				ResourceUtil.iceID,
				ResourceUtil.regolithID };
	}

	/** The total amount resource collected/studied. */
	private Map<Integer, Double> resourcesCollected = new HashMap<>();
	/** The settlement's resource statistics. */
	private Map<Integer, Map<Integer, Map<Integer, Double>>> resourceStat = new ConcurrentHashMap<>();
	/** The settlement's map of adjacent buildings. */
	private transient Map<Building, List<Building>> adjacentBuildingMap = new ConcurrentHashMap<>();
	/** The settlement's list of citizens. */
	private Collection<Person> citizens = new ConcurrentLinkedQueue<Person>();
	/** The settlement's list of owned robots. */
	private Collection<Robot> ownedRobots = new ConcurrentLinkedQueue<Robot>();
	/** The settlement's list of owned vehicles. */
	private Collection<Vehicle> ownedVehicles = new ConcurrentLinkedQueue<Vehicle>();
	/** The list of people currently within the settlement. */
	private Collection<Person> peopleWithin = new ConcurrentLinkedQueue<Person>();
	/** The list of equipment currently within the settlement. */
	private Collection<Equipment> ownedEquipment = new ConcurrentLinkedQueue<Equipment>();
	
	/** The flag for checking if the simulation has just started. */
	private boolean justLoaded = true;

	/** Unique identifier for this settlement. */
	//private int identifier;
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
	/** Total numbers of on-going manufacturing processes. */
	private int sumOfCurrentManuProcesses = 0;
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
	private int numOwnedEquipment;
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
	/** Flags that track if each of the 13 missions have been disable. */
	private boolean[] missionsDisable = new boolean[12];

	/** The average ice collection rate of the water ice nearby */
	private double iceCollectionRate = 1;
	/** The composite value of the minerals nearby. */
	public double mineralValue = -1;
	/** The rate [kg per millisol] of filtering grey water for irrigating the crop. */
	public double greyWaterFilteringRate = 1;
	/** The currently minimum passing score for mission approval. */
	private double minimumPassingScore = 100;
	/** The trending score for curving the minimum score for mission approval. */
	private double trendingScore = 30D;
	/** The recently computed average score of the missions. */
	private double currentAverageScore = 100;
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
	/** The maximum distance (in km) the rovers are allowed to travel. */
	private double maxMssionRange = 2500;
	/** The mission radius [in km] for the rovers of this settlement for each type of mission . */
	private double[] missionRange = new double[] {
			500, // 0. Areo
			500, // 1. Bio
			500, // 2. CollectIce
			500, // 3. CollectRegolith
			maxMssionRange*2,// 4. Delivery
			1000,// 5. Emergency
			500, // 6. Exploration
			500, // 7. Meteorology
			500, // 8. Mining
			1000,// 9. RescueSalvageVehicle
			maxMssionRange,   // 10. Trade
			maxMssionRange*2, // 11.TravelToSettlement			
	};
	/** The settlement terrain profile. */
	public double[] terrainProfile = new double[2];

	/** The settlement sponsor. */
	private ReportingAuthorityType sponsor;
	/** The settlement template name. */
	private String template;

	/** The settlement's ReportingAuthority instance. */
	private ReportingAuthority ra;
	
	/** The settlement objective type instance. */
	private ObjectiveType objectiveType;

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

	/** The last 20 mission scores */
	private List<Double> missionScores;
	/** The settlement's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement;
	/** The settlement's water consumption in kitchen when preparing/cleaning meal and dessert. */
	private SolMetricDataLogger<WaterUseType> waterConsumption;
	/** The settlement's daily output (resources produced). */
	private SolMetricDataLogger<Integer> dailyResourceOutput;
	/** The settlement's daily labor hours output. */
	private SolMetricDataLogger<Integer> dailyLaborTime;

	private Map<Integer, Boolean> allowTradeMissionSettlements;
	private Set<OverrideType> processOverrides = new HashSet<>();
	/** Mission modifiers */
	private Map<MissionType, Integer> missionModifiers;
	
	// Static members	
	private static final int oxygenID = ResourceUtil.oxygenID;
	private static final int waterID = ResourceUtil.waterID;
	private static final int co2ID = ResourceUtil.co2ID;
	private static final int foodID = ResourceUtil.foodID;
	private static final int methaneID = ResourceUtil.methaneID;

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

		if (missionManager == null) {// for passing maven test
			missionManager = sim.getMissionManager();
		}
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
	private Settlement(String name, int id, String template, ReportingAuthorityType sponsor, Coordinates location, int populationNumber,
			int projectedNumOfRobots) {
		// Use Structure constructor
		super(name, location);
		
		this.template = template;
		this.sponsor = sponsor;
		this.location = location;
		this.templateID = id;
		this.projectedNumOfRobots = projectedNumOfRobots;
		this.initialPopulation = populationNumber;

		// Determine the reporting authority
		this.ra = ReportingAuthorityFactory.getAuthority(sponsor);

		// Determine the mission directive modifiers
		determineMissionAgenda();
		
//		// The surface of Mars contains this settlement
//		setContainerUnit(marsSurface);
//		// Set the containerID
//		setContainerID(Unit.MARS_SURFACE_ID);
		
		// Set all mission disable flag to false
		int size = missionsDisable.length;
		for (int i=0; i<size; i++) {
			missionsDisable[i] = false;
		}
		
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
	public static Settlement createNewSettlement(String name, int id, String template, ReportingAuthorityType sponsor,
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
//				
//		double elevation = terrainProfile[0];
//		double gradient = terrainProfile[1];		
//		
//		iceCollectionRate = (- 0.639 * elevation + 14.2492) / 10D  + gradient / 250;
//		
//		if (iceCollectionRate < 0)
//			iceCollectionRate = 0;
//		
//		logger.info(this + "           elevation : " + Math.round(elevation*1000.0)/1000.0 + " km");
//		logger.info(this + "   terrain steepness : " + Math.round(gradient*10.0)/10.0);
//		logger.info(this + " ice collection rate : " + Math.round(iceCollectionRate*100.0)/100.0 + " kg/millisol");
		
		iceCollectionRate = terrainElevation.getIceCollectionRate(location);
//		logger.config("Done iceCollectionRate");
		
		// Set inventory total mass capacity.
		getInventory().addGeneralCapacity(Double.MAX_VALUE); // 10_000_000);//100_000_000);//

		double max = 500;
		// Initialize inventory of this building for resource storage
		for (AmountResource ar : ResourceUtil.getAmountResources()) {
			double resourceCapacity = getInventory().getAmountResourceRemainingCapacity(ar, true, false);
			if (resourceCapacity >= 0) {
				getInventory().addAmountResourceTypeCapacity(ar, max);
			}
		}
//		logger.config("Done addAmountResourceTypeCapacity()");
		// Initialize building manager
		buildingManager = new BuildingManager(this);
//		logger.config("Done BuildingManager()");
		// Initialize building connector manager.
		buildingConnectorManager = new BuildingConnectorManager(this);
		// Initialize goods manager.
		goodsManager = new GoodsManager(this);
//		logger.config("Done GoodsManager()");
		// Initialize construction manager.
		constructionManager = new ConstructionManager(this);
		// Initialize power grid
		powerGrid = new PowerGrid(this);
		// Added thermal control system
		thermalSystem = new ThermalSystem(this);
//		logger.config("Done ThermalSystem()");
		// Initialize scientific achievement.
		scientificAchievement = new ConcurrentHashMap<ScienceType, Double>(0);
		// Add chain of command
		chainOfCommand = new ChainOfCommand(this);
//		logger.config("Done ChainOfCommand()");
		// Add tracking composition of air
		compositionOfAir = new CompositionOfAir(this);
//		logger.config("Done CompositionOfAir()");
		// Set objective()
		if (template.equals(TRADING_OUTPOST))
			setObjective(ObjectiveType.TRADE_CENTER, 2);
		else if (template.equals(MINING_OUTPOST))
			setObjective(ObjectiveType.MANUFACTURING_DEPOT, 2);
		else
			setObjective(ObjectiveType.CROP_FARM, 2);

//		LogConsolidated.log(Level.INFO, 0, sourceName,
//				"[" + this + "] Set development objective to " + objectiveType.toString() 
//				+ " (based upon the '" + template + "' Template).", null);

		// initialize the missionScores list
		missionScores = new ArrayList<>();
		missionScores.add(200D);

		// Create the water consumption map
		waterConsumption = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		// Create the daily output map
		dailyResourceOutput = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		// Create the daily labor hours map
		dailyLaborTime = new SolMetricDataLogger<>(MAX_NUM_SOLS);
//		logger.config("Done initialize()");
		
		// TODO fire the New Unit event here
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
		
		MissionSubAgenda[] a = ra.getMissionAgenda().getAgendas();
		Map<MissionType, Integer> agendaModifers = Arrays.stream(a)
				  .flatMap(e -> e.getModifiers().entrySet().stream())
				  .collect(Collectors.groupingBy(Map.Entry::getKey,
						  Collectors.summingInt(Map.Entry::getValue)));
		
		missionModifiers.putAll(agendaModifers);
	}

	/*
	 * Gets sponsoring agency for the person
	 */
	public ReportingAuthority getReportingAuthority() {
		return ra;
	}

	/**
	 * Create a map of buildings with their lists of building connectors attached to
	 * it
	 * 
	 * @return a map
	 */
	private Map<Building, List<Building>> createAdjacentBuildingMap() {
//		adjacentBuildingMap.clear();
		if (adjacentBuildingMap == null)
			adjacentBuildingMap = new ConcurrentHashMap<>();
		for (Building b : buildingManager.getBuildings()) {
			List<Building> connectors = createAdjacentBuildings(b);
			// if (b == null)
			// System.out.println("b = null");
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
		int result = 0;
		List<Building> bs = buildingManager.getBuildings(FunctionType.LIVING_ACCOMMODATIONS);
		for (Building building : bs) {
			result += building.getLivingAccommodations().getBedCap();
		}

		return result;
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
						+ Math.round(p.getYLocation()*10.0)/10.0, null);
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

		return citizens.stream().filter(
				p -> !p.isDeclaredDead() && (p.getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY
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
		return Math.toIntExact(getInventory().getAllContainedUnitIDs()
				.stream().filter(id -> unitManager.getRobotByID(id) instanceof Robot)
				.collect(Collectors.counting()));
	}

	/**
	 * Gets a collection of the number of robots of the settlement.
	 *
	 * @return Collection of robots
	 */
	public Collection<Robot> getRobots() {
		return CollectionUtils.getRobot(getInventory().getContainedUnits());
	}

	/**
	 * Returns true if life support is working properly and is not out of oxygen or
	 * water.
	 * 
	 * @return true if life support is OK
	 * @throws Exception if error checking life support.
	 */
	public boolean lifeSupportCheck() {
		// boolean result = true;
		try {
			if (getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false) <= 0D)
				return false;
			if (getInventory().getAmountResourceStored(ResourceUtil.waterID, false) <= 0D)
				return false;

			// TODO: check against indoor air pressure
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
			// result = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
		// return result;
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
	 * @param amountRequested the amount of oxygen requested [kg]
	 * @return the amount of oxygen actually received [kg]
	 * @throws Exception if error providing oxygen.
	 */
	public double provideOxygen(double amountRequested) {
		double oxygenTaken = amountRequested;
		try {
			double oxygenLeft = getInventory().getAmountResourceStored(oxygenID, false);
			if (oxygenTaken > oxygenLeft)
				oxygenTaken = oxygenLeft;
			// Note: do NOT retrieve O2 here since calculateGasExchange() in
			// CompositionOfAir
			// is doing it for all inhabitants once per frame.
			getInventory().retrieveAmountResource(oxygenID, oxygenTaken);
//			getInventory().addAmountDemandTotalRequest(oxygenID);
			getInventory().addAmountDemand(oxygenID, oxygenTaken);

			double carbonDioxideProvided = oxygenTaken;
			double carbonDioxideCapacity = getInventory().getAmountResourceRemainingCapacity(co2ID, true, false);
			if (carbonDioxideProvided > carbonDioxideCapacity)
				carbonDioxideProvided = carbonDioxideCapacity;
			// Note: do NOT store CO2 here since calculateGasExchange() in CompositionOfAir
			// is doing it for all inhabitants once per frame.
			getInventory().storeAmountResource(co2ID, carbonDioxideProvided, true);
//			getInventory().addAmountSupply(co2ID, carbonDioxideProvided);

		} catch (Exception e) {
			logger.log(this, null, Level.SEVERE, 5000, "Error in providing O2/removing CO2 ", e);
		}

		return oxygenTaken;
		// return oxygenTaken * (malfunctionManager.geOxygenFlowModifier() / 100D);
	}

	/**
	 * Gets water from the inventory
	 *
	 * @param amountRequested the amount of water requested [kg]
	 * @return the amount of water actually received [kg]
	 * @throws Exception if error providing water.
	 */
	public double provideWater(double amountRequested) {
		double waterTaken = amountRequested;
		try {
			double waterLeft = getInventory().getAmountResourceStored(waterID, false);
			if (waterTaken > waterLeft)
				waterTaken = waterLeft;
			if (waterTaken > MIN) {
				Storage.retrieveAnResource(waterTaken, waterID, getInventory(), true);
				getInventory().retrieveAmountResource(waterID, waterTaken);
//				getInventory().addAmountDemandTotalRequest(waterID);
//				getInventory().addAmountDemand(waterID, waterTaken);
			}
		} catch (Exception e) {
			logger.log(this, null, Level.SEVERE, 5000, "Error in providing H2O needs: ", e);
		}

		return waterTaken;
//		return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
	}

	/**
	 * Computes the average air pressure of the life support system.
	 * 
	 * @return air pressure [kPa]
	 */
	private double computeAveragePressure() {

		double totalArea = 0;
		double totalPressureArea = 0;
		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();
		for (Building b : buildings) {
			int id = b.getInhabitableID();
			double area = b.getFloorArea();
			totalArea += area;
			totalPressureArea += compositionOfAir.getTotalPressure()[id] * area;
		}
		totalArea = Math.max(0.1, totalArea); // Guard against divid by zero
		
		// convert from atm to kPascal
		return totalPressureArea * CompositionOfAir.KPA_PER_ATM / totalArea;

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
	 * Computes the average temperature in the settlement (from the life support
	 * system of all buildings)
	 * 
	 * @return temperature (degrees C)
	 */
	private double computeAverageTemperature() {
		double totalArea = 0;
		double totalTArea = 0;
		List<Building> buildings = buildingManager.getBuildingsWithThermal();
		for (Building b : buildings) {
			double area = b.getFloorArea();
			totalArea += area;
			totalTArea += b.getCurrentTemperature() * area;
		}
		totalArea = Math.max(0.1, totalArea); // Guard against divid by zero

		return totalTArea / totalArea;
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
		// TODbooleanO: should the number of robots be accounted for here?

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
		
		// TODO: what to take into consideration the presence of robots ?
		// If no current population at settlement for one sol, power down the
		// building and turn the heat off ?
		
		if (powerGrid.getPowerMode() != PowerMode.POWER_UP)
			powerGrid.setPowerMode(PowerMode.POWER_UP);
		// TODO: check if POWER_UP is necessary
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
			
			// Reduce the recurrent passing score daily to its 90% value
			minimumPassingScore = minimumPassingScore * .9;
			
//			// Updates the goods manager 
//			updateGoodsManager(pulse);

			int cycles = settlementConfig.getTemplateID();
			int remainder = msol % cycles;
			if (remainder == templateID) {
				// Update the goods value gradually with the use of buffers
				if (goodsManager.isInitialized()) {
					Iterator<Good> i = GoodsUtil.getGoodsList().iterator();
					while (i.hasNext()) {
						Good g = i.next();
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
				computeWaterRation();
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

			// if (remainder == 15) {
			// oxygenProbabilityValue = computeOxygenProbability();
			// }

			// if (remainder == 20) {
			// methaneProbabilityValue = computeMethaneProbability();
			// }
		}

		// updateRegistry();

		compositionOfAir.timePassing(pulse);

		currentPressure = computeAveragePressure();

		currentTemperature = computeAverageTemperature();

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
	
		/**
		 * Construction Sites are passive
		for (ConstructionSite s : constructionManager.getConstructionSites()) {
			s.timePassing(time);
		}
		*/
		
		for (Equipment e : ownedEquipment) {
			e.timePassing(pulse);
		}
		
		for (Vehicle v : ownedVehicles) {
			v.timePassing(pulse);
		}
		
		for (Person p : citizens) {
			p.timePassing(pulse);
		}
		/**
		 * Robots are already updated as Equipment ? Seems not so should Robots be based directly on a Unit
		 */
		for (Robot r : ownedRobots) {
			r.timePassing(pulse);
		}
		
		return true;
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
			// BuildingManager manager = person.getSettlement().getBuildingManager();
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
//			boolean notFull = quarters.getNumEmptyActivitySpots() > 0;//quarters.getRegisteredSleepers() < quarters.getBedCap();
			// Check if an unmarked bed is wanted
			if (unmarked) {
				if (quarters.hasAnUnmarkedBed()) {// && notFull) {
					result.add(building);
				}
			}
//			else if (notFull) {
//				result.add(building);
//			}
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
		double newAmount = getInventory().getAmountResourceStored(resourceType, false);
		
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
	 * Provides the daily reports for the settlement
	 */
	private void performEndOfDayTasks(MarsClock marsNow) {
		int solElapsed = marsNow.getMissionSol();

		// getFoodEnergyIntakeReport();
		reassignWorkShift();

		tuneJobDeficit();

		refreshResourceStat();

		refreshSleepMap(solElapsed);

		refreshSupplyDemandMap(solElapsed);
				
		solCache = solElapsed;
		
		int size = samplingResources.length;
		for (int i=0; i<size; i++) {
			int id = samplingResources[i];
			double amount = calculateDailyAverageResource(solCache - 1, id);

		}
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
		// TODO: should call this method at, say, 800 millisols, not right at 1000
		// millisols
		Collection<Person> people = citizens;// getIndoorPeople();
		int pop = people.size();

//		int nShift = 0;
//
//		if (pop == 1) {
//			nShift = 1;
//		} else if (pop < UnitManager.THREE_SHIFTS_MIN_POPULATION) {
//			nShift = 2;
//		} else {// if pop => 6
//			nShift = 3;
//		}

//		if (numShiftsCache != nShift) {
//			numShiftsCache = nShift;

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
//		} // end of for loop
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
			// TODO: find the darkest time of the day
			// and set work shift to cover time period

			// For now, we may assume it will usually be X or Z, NOT Y
			// since Y is usually where midday is at unless a person is at polar region.
			if (oldShift == ShiftType.Y || oldShift == ShiftType.Z) {

				ShiftType newShift = ShiftType.X;
				
//				boolean x_ok = isWorkShiftSaturated(ShiftType.X, false);
//				boolean z_ok = isWorkShiftSaturated(ShiftType.Z, false);
//				// TODO: Instead of throwing a dice,
//				// take the shift that has less sunlight
//				int rand;
//				ShiftType newShift = null;
//
//				if (x_ok && z_ok) {
//					rand = RandomUtil.getRandomInt(1);
//					if (rand == 0)
//						newShift = ShiftType.X;
//					else
//						newShift = ShiftType.Z;
//				}
//
//				else if (x_ok)
//					newShift = ShiftType.X;
//
//				else if (z_ok)
//					newShift = ShiftType.Z;

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
			// TODO: should find the person with the highest tendency to take this shift

			// if the person just came back from a mission, he would have on-call shift
			if (oldShift == ShiftType.ON_CALL) {
				// TODO: check a person's sleep habit map and request changing his work shift
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
			getInventory().compactAmountSupplyMap(SUPPLY_DEMAND_REFRESH);
			getInventory().clearAmountSupplyRequestMap();
			// Carry out the daily average of the previous x days
			getInventory().compactAmountDemandMap(SUPPLY_DEMAND_REFRESH);
			getInventory().clearAmountDemandTotalRequestMap();
			getInventory().clearAmountDemandMetRequestMap();

			// compact item resource map
			getInventory().compactItemSupplyMap(SUPPLY_DEMAND_REFRESH);
			getInventory().clearItemSupplyRequestMap();
			// Carry out the daily average of the previous x days
			getInventory().compactItemDemandMap(SUPPLY_DEMAND_REFRESH);
			getInventory().clearItemDemandTotalRequestMap();
			getInventory().clearItemDemandMetRequestMap();

			// Added clearing of weather data map
			weather.clearMap();
			// logger.info(name + " : Compacted the settlement's supply demand data &
			// cleared weather data.");
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
		// TODO: set up rules that allows

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
							// boolean isOff =
							// person.getTaskSchedule().getShiftType().equals(ShiftType.OFF);
							// if (isOff)
							if (!person.equals(initiator))
								people.add(person);
						}
					}
				}

				else {
					// may be radio (non face-to-face) conversation
					// if (!initiator.getBuildingLocation().equals(person.getBuildingLocation())) {
					if (checkIdle) {
						if (isIdleTask(task)) {
							if (!person.equals(initiator))
								people.add(person);
						}
					} else if (task instanceof HaveConversation) {
						// boolean isOff =
						// person.getTaskSchedule().getShiftType().equals(ShiftType.OFF);
						// if (isOff)
						if (!person.equals(initiator))
							people.add(person);
					}
				}
			}
		}

		return people;
	}

	private boolean isIdleTask(Task task) {
		if (task instanceof Relax
				 || task instanceof Read
				 || task instanceof HaveConversation
				 || task instanceof EatDrink)
			return true;
		
		return false;
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
			if (!ASTRONOMY_OBSERVATORY.equalsIgnoreCase(building.getBuildingType())) {
				double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), person.getXLocation(),
						person.getYLocation());
				if (distance < leastDistance) {
					// EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
					result = building.getEVA().getAirlock();
					leastDistance = distance;
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
		Airlock result = null;
		Building currentBuilding = BuildingManager.getBuilding(person);

		if (currentBuilding == null) {
			// throw new IllegalStateException(person.getName() + " is not currently in a
			// building."); //throw new IllegalStateException(robot.getName() + " is not
			// currently in a building.");
			// this major bug is due to getBuilding(robot) above in BuildingManager
			// what if a person is out there in ERV building for maintenance. ERV building
			// has no LifeSupport function. currentBuilding will be null
			logger.log(person, Level.WARNING, 10_000, "Not currently in a building.");
			return null;
		}

		result = getAirlock(currentBuilding, xLocation, yLocation);

		return result;
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
		Airlock result = null;
		Building currentBuilding = BuildingManager.getBuilding(robot);

		if (currentBuilding == null) {
			// throw new IllegalStateException(robot.getName() + " is not currently in a
			// building.");
			// this major bug is due to getBuilding(robot) above in BuildingManager
			// need to refine the concept of where a robot can go. They are thought to need
			// RoboticStation function to "survive",
			// much like a person who needs LifeSupport function
			logger.log(robot, Level.WARNING, 10_000, "Not currently in a building.");
			return null;
		}

		result = getAirlock(currentBuilding, xLocation, yLocation);

		return result;
	}

	
	/**
	 * Gets an airlock for an EVA egress, preferably an pressurized airlock
	 * 
	 * @param currentBuilding
	 * @param xLocation
	 * @param yLocation
	 * @return
	 */
	private Airlock getAirlock(Building currentBuilding, double xLocation, double yLocation) {
		Airlock result = null;

		List<Building> pressurizedBldgs = new ArrayList<>();
		List<Building> depressurizedBldgs = new ArrayList<>();
		List<Building> selectedPool = new ArrayList<>();
		
		Iterator<Building> i = buildingManager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (building.getEVA().getAirlock().isPressurized()
					|| building.getEVA().getAirlock().isPressurizing())
				pressurizedBldgs.add(building);
			else if (building.getEVA().getAirlock().isDepressurized()
					|| building.getEVA().getAirlock().isDepressurizing())
				depressurizedBldgs.add(building);
		}
		
		if (pressurizedBldgs.size() > 1) {
			selectedPool = pressurizedBldgs;
		}
		
		else if (pressurizedBldgs.size() == 1) {
			return pressurizedBldgs.get(0).getEVA().getAirlock();
		}
		
		else if (depressurizedBldgs.size() > 1) {
			selectedPool = depressurizedBldgs;
		}
		
		else if (depressurizedBldgs.size() == 1) {
			return depressurizedBldgs.get(0).getEVA().getAirlock();
		}
		
		else {
			return null;
		}
		
		double leastDistance = Double.MAX_VALUE;

		Iterator<Building> ii = selectedPool.iterator();
		while (i.hasNext()) {
			Building building = ii.next();
			if (buildingConnectorManager.hasValidPath(currentBuilding, building)) {

				double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), xLocation,
						yLocation);
				if (distance < leastDistance) {

					result = building.getEVA().getAirlock();
					leastDistance = distance;
				}
			}
		}
		
		return result;
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
//		BuildingManager manager = buildingManager;
		Iterator<Building> i = buildingManager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building nextBuilding = i.next();

			if (buildingConnectorManager.hasValidPath(building, nextBuilding)) {

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
		return unitManager.getPeople().stream().filter(p -> p.getBuriedSettlement() == this)
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
		return unitManager.getPeople().stream().filter(
				p -> (p.getAssociatedSettlement() == this && p.isDeclaredDead()) || p.getBuriedSettlement() == this)
				.collect(Collectors.toList());
	}

		
	/**
	 * Makes this person within this settlement
	 * 
	 * @param p the person
	 */
	public void addPeopleWithin(Person p) {
		if (!peopleWithin.contains(p)) {
			peopleWithin.add(p);
		}
	}
	
	/**
	 * Removes this person from being within this settlement
	 * 
	 * @param p the person
	 */
	public void removePeopleWithin(Person p) {
		if (peopleWithin.contains(p)) {
			peopleWithin.remove(p);
		}
	}
	
	/**
	 * Makes this person a legal citizen of this settlement
	 * 
	 * @param p the person
	 */
	public void addACitizen(Person p) {
		if (!citizens.contains(p)) {
			citizens.add(p);
			// Update the numCtizens
			numCitizens = citizens.size();
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, this);
		}
	}
	
	/**
	 * Removes this person from being a legal citizen of this settlement
	 * 
	 * @param p the person
	 */
	public void removeACitizen(Person p) {
		if (citizens.contains(p)) {
			citizens.remove(p);
			// Update the numCtizens
			numCitizens = citizens.size();
//			System.out.println("numCitizens: " + numCitizens);
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT, this);
		}
	}

	/**
	 * Adds a robot to be owned by the settlement
	 * 
	 * @param r
	 */
	public void addOwnedRobot(Robot r) {
		if (!ownedRobots.contains(r)) {
			ownedRobots.add(r);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT, this);
			numOwnedBots = ownedRobots.size();
		}
	}

	/**
	 * Removes a robot from being owned by the settlement
	 * 
	 * @param r
	 */
	public void removeOwnedRobot(Robot r) {
		if (ownedRobots.contains(r)) {
			ownedRobots.remove(r);
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT, this);
			numOwnedBots = ownedRobots.size();
		}
	}

	/**
	 * Adds a vehicle to be owned by the settlement
	 * 
	 * @param r
	 */
	public void addOwnedVehicle(Vehicle v) {
		if (!ownedVehicles.contains(v)) {
			ownedVehicles.add(v);
			numOwnedVehicles = ownedVehicles.size();
		}
	}

	/**
	 * Removes a vehicle from being owned by the settlement
	 * 
	 * @param r
	 */
	public void removeOwnedVehicle(Vehicle v) {
		if (ownedVehicles.contains(v)) {
			ownedVehicles.remove(v);
			numOwnedVehicles = ownedVehicles.size();
		}
	}
	
	/**
	 * Adds an equipment to be owned by the settlement
	 * 
	 * @param r
	 */
	public void addOwnedEquipment(Equipment e) {
		if (!ownedEquipment.contains(e)) {
			ownedEquipment.add(e);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			numOwnedEquipment = ownedEquipment.size();
		}
	}

	/**
	 * Removes an equipment from being owned by the settlement
	 * 
	 * @param r
	 */
	public void removeOwnedEquipment(Equipment e) {
		if (ownedEquipment.contains(e)) {
			ownedEquipment.remove(e);
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_EQUIPMENT_EVENT, this);
			numOwnedEquipment = ownedEquipment.size();
		}
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
		return ownedEquipment;
	}
	
	/**
	 * Gets all associated vehicles currently on mission
	 *
	 * @return collection of vehicles.
	 */
	public Collection<Vehicle> getMissionVehicles() {
//		return getAllAssociatedVehicles().stream()
//				.filter(v -> v.isReservedForMission())
//				.collect(Collectors.toList());

		Collection<Vehicle> result = new ArrayList<>();
		Iterator<Mission> i = missionManager.getMissionsForSettlement(this).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
//			Vehicle vehicle = mission.getVehicle();
				if ((vehicle != null) 
						&& !result.contains(vehicle)
						&& this.equals(vehicle.getAssociatedSettlement()))
					result.add(vehicle);
			}
			
			else if (mission instanceof BuildingConstructionMission) {
				result.addAll(((BuildingConstructionMission) mission).getConstructionVehicles());
				
//				Iterator<GroundVehicle> ii = ((BuildingConstructionMission) mission).getConstructionVehicles().iterator();;
//				while (i.hasNext()) {
//					GroundVehicle gv = ii.next();
//				}
			}
		}
		
//		System.out.println(this + "'s Mission Vehicles : " + result);

		return result;
	}

	/**
	 * Gets a collection of drones parked or garaged at the settlement.
	 *
	 * @return Collection of parked drones
	 */
	public Collection<Drone> getParkedDrones() {
		return getInventory().getContainedDrones();
	}
	
	/**
	 * Gets a collection of vehicles parked or garaged at the settlement.
	 *
	 * @return Collection of parked vehicles
	 */
	public Collection<Vehicle> getParkedVehicles() {
		return getInventory().getContainedVehicles();
	}

	/**
	 * Gets the number of vehicles parked or garaged at the settlement.
	 * 
	 * @return parked vehicles number
	 */
	public int getParkedVehicleNum() {
		return getInventory().getNumContainedVehicles();
	}

	/**
	 * Gets the number of vehicles owned by the settlement.
	 * 
	 * @return number of owned vehicles
	 */
	public int getVehicleNum() {
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
		double chance0 = 100 - chance1 - chance2; // RadiationExposure.BASELINE_PERCENT * ratio * (variation1 +
													// variation2); // average 3.53%
		if (chance0 < 0)
			chance0 = 0;

		// Baseline radiation event
		// Note: RadiationExposure.BASELINE_CHANCE_PER_100MSOL_DURING_EVA * time / 100D
		// Assume the baseline radiation can be fully shielded by the EVA suit
//		if (RandomUtil.lessThanRandPercent(chance0)) {
//	    	//System.out.println("chance0 : " + chance0);
//	    	exposed[0] = true;
//	    	//logger.info("An unspecified low-dose radiation event is detected by the radiation sensor grid on " + getName());
//			LogConsolidated.log(Level.INFO, 0, sourceName,
//					"[" + name + DETECTOR_GRID + UnitEventType.LOW_DOSE_EVENT.toString() + " is imminent.", null);
//	    	this.fireUnitUpdate(UnitEventType.LOW_DOSE_EVENT);
//	    }
//	    else
//	    	exposed[0] = false;

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
	public int getWaterRation() {
		return waterRationLevel;
	}

	/**
	 * Computes the water ration level at the settlement due to low water supplies.
	 * 
	 * @return level of water ration.
	 */
	private void computeWaterRation() {
		double storedWater = getInventory().getAmountResourceStored(ResourceUtil.waterID, false);
		double requiredDrinkingWaterOrbit = water_consumption_rate * getNumCitizens() // getIndoorPeopleCount()
				* MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;

		// If stored water is less than 20% of required drinking water for Orbit, wash
		// water should be rationed.
		double ratio = storedWater / requiredDrinkingWaterOrbit;
		double mod = goodsManager.getWaterValue() / ratio;
		if (mod < 1)
			mod = 1;
		else if (mod > 1000)
			mod = 1000;
		goodsManager.setWaterValue(mod);
		
		waterRationLevel = (int) (1.0 / ratio);

		if (waterRationLevel < 1)
			waterRationLevel = 1;
		else if (waterRationLevel > 50)
			waterRationLevel = 50;
//				
//		if (storedWater < (requiredDrinkingWaterOrbit * .0025D)) {
//			result = 11;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 64;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .005D)) {
//			result = 10;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 48;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .01D)) {
//			result = 9;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 32;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .015D)) {
//			result = 8;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 24;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .025D)) {
//			result = 7;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 16;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .05D)) {
//			result = 6;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 12;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .075D)) {
//			result = 5;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 8;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .1D)) {
//			result = 4;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 6;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .125D)) {
//			result = 3;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 4;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .15D)) {
//			result = 2;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 3;
//		} else if (storedWater < (requiredDrinkingWaterOrbit * .2D)) {
//			result = 1;
//			GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER * 2;
//		}		
//		waterRationLevel = result;
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
		// goodsManager.setTourismFactor(1);
		// goodsManager.setFreeMarketFactor(1);

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

		// Future alternatives :
		// FREE_MARKET
		// POWER_HUB
		// RESIDENTIAL_DISTRICT
		// " bunkhouse" or "outpost hub";

		else
			return null;
	}

	public ReportingAuthorityType getSponsor() {
		return sponsor;
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
	 * @param now Curretn time
	 */
	private void doCropsNeedTending(ClockPulse now) {

		int m = now.getMarsTime().getMillisolInt();
		
		// Check for the day rolling over
		if (now.isNewSol() || (millisolCache + 5) < m) {
			millisolCache = m;
			cropsNeedingTendingCache = 0;
			for (Building b : buildingManager.getBuildings(FunctionType.FARMING)) {
				Farming farm = b.getFarming();
				for (Crop c : farm.getCrops()) {
					if (c.requiresWork()) {
						cropsNeedingTendingCache++;
					}
					// if the health condition is below 50%,
					// need special care
					if (c.getHealthCondition() < .5)
						cropsNeedingTendingCache++;
				}
				cropsNeedingTendingCache += farm.getNumCrops2Plant();
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

		double regolith_value = goodsManager.getGoodValuePerItem(ResourceUtil.regolithID);
		regolith_value = regolith_value * GoodsManager.REGOLITH_VALUE_MODIFIER;
		if (regolith_value > REGOLITH_MAX)
			regolith_value = REGOLITH_MAX;
		else if (regolith_value < 0)
			return 0;

		double sand_value = goodsManager.getGoodValuePerItem(ResourceUtil.sandID);
		sand_value = sand_value * GoodsManager.SAND_VALUE_MODIFIER;
		if (sand_value > REGOLITH_MAX)
			sand_value = REGOLITH_MAX;
		else if (sand_value < 0)
			return 0;

		int pop = numCitizens;// getAllAssociatedPeople().size();// getCurrentPopulationNum();

		double regolith_available = getInventory().getAmountResourceStored(ResourceUtil.regolithID, false);
		double sand_available = getInventory().getAmountResourceStored(ResourceUtil.sandID, false);

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
			
//		} else if (regolith_available > MIN_REGOLITH_RESERVE * pop / 1.5) {
//			result = 20D * result + (MIN_REGOLITH_RESERVE * pop - regolith_available);
//		} else if (regolith_available > MIN_REGOLITH_RESERVE * pop / 2D) {
//			result = 10D * result + (MIN_REGOLITH_RESERVE * pop - regolith_available);
//		} else
//			result = 5D * result + (MIN_REGOLITH_RESERVE * pop - regolith_available);

		if (result < 0)
			result = 0;

//		System.out.println("computeRegolithProbability: " + result);
		return result;
	}

	/***
	 * Computes the probability of the presence of ice
	 * 
	 * @return probability of finding ice
	 */
	private double computeIceProbability() {
		double result = 0;

		double ice_value = goodsManager.getGoodValuePerItem(ResourceUtil.iceID);
//		ice_value = ice_value * GoodsManager.ICE_VALUE_MODIFIER;
		if (ice_value > ICE_MAX)
			ice_value = ICE_MAX;
		if (ice_value < 1)
			ice_value = 1;

		double water_value = goodsManager.getGoodValuePerItem(ResourceUtil.waterID);
		water_value = water_value * goodsManager.getWaterValue();
		if (water_value > WATER_MAX)
			water_value = WATER_MAX;
		if (water_value < 1)
			water_value = 1;

		// Compare the available amount of water and ice reserve
		double ice_available =  getInventory().getAmountResourceStored(ResourceUtil.iceID, false);
		double water_available =  Math.log(1 + getInventory().getAmountResourceStored(ResourceUtil.waterID, false));

		int pop = numCitizens;

		// TODO: create a task to find local ice and simulate the probability of finding
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
		
//		else if (water_available > MIN_WATER_RESERVE * pop / 1.5) {
//			result = 2D * result + (MIN_WATER_RESERVE * pop - water_available) / 10;
//		} else if (water_available > MIN_WATER_RESERVE * pop / 2D) {
//			result = result + (MIN_WATER_RESERVE * pop - water_available) / 10;
//		} else
//			result = .5 * result + (MIN_WATER_RESERVE * pop - water_available) / 10;

//		System.out.println("computeIceProbability: " + result);
		return result;
	}

	/**
	 * Checks if the last 20 mission scores are above the threshold
	 * 
	 * @param score
	 * @return true/false
	 */
	public boolean passMissionScore(double score) {
		double total = 0;
		for (double s : missionScores) {
			total += s;
		}
		currentAverageScore = total / missionScores.size();

		minimumPassingScore = Math.round((currentAverageScore + trendingScore) * 10D) / 10D;

		if (score > currentAverageScore + trendingScore) {
			trendingScore = (score - currentAverageScore + 2D * trendingScore) / 3D;
			return true;
		} else {
			trendingScore = (currentAverageScore - score + 2D * trendingScore) / 3D;
			return false;
		}
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
		missionScores.add(score);

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

	public double getMaxMssionRange() {
		return maxMssionRange;
	}

	public void setMaxMssionRange(double value) {
		maxMssionRange = value;
	}

	/**
	 * Gets the allowed traveled distance for a particular type of mission
	 * 
	 * @param missionType : see maxMssionRange above 
	 *        must be a number from 0 to 11;
	 * 			0 : Areo, 
	 *		    1 : Bio, 
	 *		    2 : CollectIce, 
	 *			3 : CollectRegolith,
	 *			4 : Delivery, 
	 *			5 : Emergency, 
	 *
	 *			6 : Exploration, 
	 * 			7 : Meteorology, 
	 *			8 : Mining, 
     * 			9 : RescueSalvageVehicle, 
	 * 		   10 : Trade, 
	 * 
	 * 		   11 : TravelToSettlement	
	 * @return the range [in km]
	 */
	public double getMissionRadius(int missionType) {
		return missionRange[missionType];
	}
	
	public void setMissionRadius(int missionType, double newRange) {
		missionRange[missionType] = newRange;
	}
	
	public double getMissionRadius(MissionType missionType) {
		return missionRange[missionType.ordinal()];
	}
	
	public boolean hasDesignatedCommander() {
		return hasDesignatedCommander;
	}

	public Person setDesignatedCommander(Commander profile) {
		hasDesignatedCommander = true;
		
		return chainOfCommand.applyCommander(profile);
	}

	public void increaseGreyWaterFilteringRate() {
		if (greyWaterFilteringRate < 100) {
			greyWaterFilteringRate = 1.1 * greyWaterFilteringRate;
		}
	}

	public void decreaseGreyWaterFilteringRate() {
		if (greyWaterFilteringRate > .01) {
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

	public void setMissionDisable(String missionName, boolean disable) {
		List<String> names = MissionManager.getMissionNames();
		int size = missionsDisable.length;
		for (int i=0; i<size; i++) {
			if (missionName.equalsIgnoreCase(names.get(i)) 
					&& missionsDisable[i] == true) {
				missionsDisable[i] = disable;
			}
		}
	}
	
	public boolean isMissionDisable(String missionName) {
		List<String> names = MissionManager.getMissionNames();
		int size = missionsDisable.length;
		for (int i=0; i<size; i++) {
			if (missionName.equalsIgnoreCase(names.get(i))) {
				return missionsDisable[i];
			}
		}
		// by default it's false
		return false;
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
	 * @param missionName the name of the mission calling this method
	 * @return probability value
	 */
	public boolean getMissionBaseProbability(String missionName) {

		if (!missionProbability) {
			
			List<String> names = MissionManager.getMissionNames();
			int size = names.size();
			// 0. Check if a mission has been overridden
			for (int i=0; i<size; i++) {
				if (missionName.equalsIgnoreCase(names.get(i)) 
					&& missionsDisable[i] == true) {
//					missionProbability = false;
					return false;
				}
			}
			
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
//			System.out.println("1.  missionProbability is " + missionProbability);
			// 2. Check if available backup rover.
			if (!RoverMission.hasBackupRover(this)) {
				return false;
			}
//			System.out.println("2.  missionProbability is " + missionProbability);	
			// 3. Check if at least 1 person is there
			// A settlement with <= 4 population can always do DigLocalRegolith task
			// should avoid the risk of mission.
			if (getIndoorPeopleCount() <= 1) {// .getAllAssociatedPeople().size() <= 4)
				return false;
			}
//			System.out.println("3.  missionProbability is " + missionProbability);			
			// 4. Check if minimum number of people are available at the settlement.
			if (!RoverMission.minAvailablePeopleAtSettlement(this, RoverMission.MIN_STAYING_MEMBERS)) {
				return false;
			}
//			System.out.println("4.  missionProbability is " + missionProbability);
			// // Check for embarking missions.
			// else if (VehicleMission.hasEmbarkingMissions(this)) {
			// return 0;
			// }

			// 5. Check if min number of EVA suits at settlement.
			if (Mission.getNumberAvailableEVASuitsAtSettlement(this) < RoverMission.MIN_GOING_MEMBERS) {
				return false;
			}
//			System.out.println("5.  missionProbability is " + missionProbability);	
			// // Check for embarking missions.
			// else if (getNumCitizens() / 4.0 < VehicleMission.numEmbarkingMissions(this))
			// {
			// return 0;
			// }

			// 6. Check if settlement has enough basic resources for a rover mission.
			if (!hasEnoughBasicResources(true)) {
				return false;
			}
//			System.out.println("6.  missionProbability is " + missionProbability);	
			// 7. Check if starting settlement has minimum amount of methane fuel.
			if (getInventory().getAmountResourceStored(ResourceUtil.methaneID,
					false) < RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
				return false;
			}

			if (VehicleMission.numEmbarkingMissions(this) > getNumCitizens() / 4D) {
				return false;
			}

			if (VehicleMission.numApprovingMissions(this) > getNumCitizens() / 4D) {
				return false;
			}

//			System.out.println("7.  missionProbability is " + missionProbability);			
			missionProbability = true;

//			System.out.println(this + "  missionProbability is " + missionProbability);
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

		Inventory inv = getInventory();

		try {
			if (inv.getAmountResourceStored(methaneID, false) < minMethane) {
				return false;
			}
			if (unmasked && inv.getAmountResourceStored(oxygenID, false) < mineOxygen) {
				return false;
			}
			if (unmasked && inv.getAmountResourceStored(waterID, false) < minWater) {
				return false;
			}
			if (inv.getAmountResourceStored(foodID, false) < minFood) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		return true;
	}

    public double getIceCollectionRate() {
//    	if (iceCollectionRate == -1) {
//			if (terrainElevation == null)
//				terrainElevation = surfaceFeatures.getTerrainElevation();
//			iceCollectionRate = terrainElevation.getIceCollectionRate(location);
//    	}
    	return iceCollectionRate;
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
	
	@Override
	protected UnitType getUnitType() {
		return UnitType.SETTLEMENT;
	}

	public int getSolCache() {
		return solCache;
	}
	
	public boolean isFirstSol() {
		if (solCache == 0 || solCache == 1)
			return true;
		return false;
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
		// if (scientificAchievement != null) {
		// scientificAchievement.clear();
		// }
		scientificAchievement = null;
	}

	/**
	 * Generate a unique name for the Settlement
	 * @return
	 */
	public static String generateName(ReportingAuthorityType sponsor) {
		List<String> remainingNames = new ArrayList<>(
				settlementConfig.getSettlementNameList(sponsor));
	
		List<String> usedNames = unitManager.getSettlements().stream()
							.map(s -> s.getName()).collect(Collectors.toList());
	
		remainingNames.removeAll(usedNames);
		int idx = RandomUtil.getRandomInt(remainingNames.size());
		
		return remainingNames.get(idx);
	}
}
