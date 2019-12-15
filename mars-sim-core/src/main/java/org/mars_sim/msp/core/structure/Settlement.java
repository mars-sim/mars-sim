/**
 * Mars Simulation Project
 * Settlement.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.mars.DustStorm;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.job.Engineer;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.job.Meteorologist;
import org.mars_sim.msp.core.person.ai.job.Technician;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.mission.meta.BuildingConstructionMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.CollectIceMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.CollectRegolithMeta;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.meta.ConstructBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairEVAMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.reportingAuthority.CNSAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.CSAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ESAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ISROMissionControl;
import org.mars_sim.msp.core.reportingAuthority.JAXAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.MarsSocietyMissionControl;
import org.mars_sim.msp.core.reportingAuthority.NASAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.RKAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.reportingAuthority.SpaceXMissionControl;
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
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The Settlement class represents a settlement unit on virtual Mars. It
 * contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements Serializable, LifeSupportInterface, Objective {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static final Logger logger = Logger.getLogger(Settlement.class.getName());

	private static final String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	private static final String DETECTOR_GRID = "] The detector grid forecast a ";
	private static final String TRADING_OUTPOST = "Trading Outpost";
	private static final String MINING_OUTPOST = "Mining Outpost";
	private static final String ASTRONOMY_OBSERVATORY = "Astronomy Observatory";
	
	public static final int CHECK_GOODS = 15;
	
	public static final int CHECK_MISSION = 20; // once every 10 millisols

	public static final int MAX_NUM_SOLS = 3;

	public static final int MAX_SOLS_DAILY_OUTPUT = 14;

	public static final int SUPPLY_DEMAND_REFRESH = 7;

	private static final int RESOURCE_UPDATE_FREQ = 50;

	private static final int CHECK_WATER_RATION = 100;
	
	private static final int SAMPLING_FREQ = 250; // in millisols

	public static final int NUM_CRITICAL_RESOURCES = 9;

	private static final int RESOURCE_STAT_SOLS = 12;

	private static final int SOL_SLEEP_PATTERN_REFRESH = 3;

	public static final int MIN_REGOLITH_RESERVE = 20; // per person

	public static final int MIN_SAND_RESERVE = 5; // per person

	public static final int MIN_ICE_RESERVE = 200; // per person

	public static final double MIN_WATER_RESERVE = 400D; // per person

	public static final double SAFE_TEMPERATURE_RANGE = 18;

	// public static final double SAFETY_PRESSURE = 20;

	/** Normal air pressure [in kPa] */
	private static final double NORMAL_AIR_PRESSURE = CompositionOfAir.SKYLAB_TOTAL_AIR_PRESSURE_kPA;
	/** The minimal amount of resource to be retrieved. */
	private static final double MIN = 0.00001;
	
	/** The unit count for this settlement. */
	private static int uniqueCount = Unit.FIRST_SETTLEMENT_UNIT_ID;
	
	/** The settlement water consumption */
	public static double water_consumption_rate;
	/** The settlement minimum air pressure requirement. */
	public static double minimum_air_pressure;
	/** The settlement life support requirements. */
	public static double[][] life_support_value = new double[2][7];
	
	/** Amount of time (millisols) required for periodic maintenance. */
	// private static final double MAINTENANCE_TIME = 1000D;

	/** Override flag for food production. */
	private transient boolean foodProductionOverride = false;
	/** Override flag for mission creation at settlement. */
	private transient boolean missionCreationOverride = false;
	/** Override flag for manufacturing at settlement. */
	private transient boolean manufactureOverride = false;
	/** Override flag for resource process at settlement. */
	private transient boolean resourceProcessOverride = false;
	/* Override flag for construction/salvage mission creation at settlement. */
	private transient boolean constructionOverride = false;
	
	/** The Flag showing if the settlement has been exposed to the last radiation event. */
	private boolean[] exposed = { false, false, false };
	/** The cache for the number of building connectors. */
	private transient int numConnectorsCache = 0;

	/** The settlement objective type string array. */
	private final static String[] objectiveArray;
	/** The settlement objective type array. */
	private final static ObjectiveType[] objectives;

	static {
		objectiveArray = new String[] { 
				Msg.getString("ObjectiveType.crop"),
				Msg.getString("ObjectiveType.manu"), 
				Msg.getString("ObjectiveType.research"),
				Msg.getString("ObjectiveType.transportation"), 
				Msg.getString("ObjectiveType.trade"),
				Msg.getString("ObjectiveType.tourism") };
		
		objectives = new ObjectiveType[] { 
				ObjectiveType.CROP_FARM,
				ObjectiveType.MANUFACTURING_DEPOT, 
				ObjectiveType.RESEARCH_CAMPUS, 
				ObjectiveType.TRANSPORTATION_HUB,
				ObjectiveType.TRADE_CENTER, 
				ObjectiveType.TOURISM };
	}
	
	/** The settlement's resource statistics. */
	private transient Map<Integer, Map<Integer, List<Double>>> resourceStat = new HashMap<>();
	/** The settlement's map of adjacent buildings. */
	private transient Map<Building, List<Building>> adjacentBuildingMap = new HashMap<>();
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
	private int identifier;
	/** The base mission probability of the settlement. */
	private int missionProbability = -1;
	/** The water ration level of the settlement. */
	private int waterRationLevel = 1;
	/** The number of people at the start of the settlement. */
	private int initialPopulation;
	/** The number of robots at the start of the settlement. */
	private int projectedNumOfRobots;
	/** The scenario ID of the settlement. */
	private int scenarioID;
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
	private double maxMssionRange = 2200;
	/** The mission radius [in km] for the rovers of this settlement for each type of mission . */
	private double[] missionRange = new double[] {
			500, // 0. Areo
			500, // 1. Bio
			500, // 2. CollectIce
			500, // 3. CollectRegolith
			1000,// 4. Emergency
			500, // 5. Exploration
			500, // 6. Meteorology
			500, // 7. Mining
			1000,// 8. RescueSalvageVehicle
			maxMssionRange,   // 9. Trade
			maxMssionRange*2, // 10.TravelToSettlement			
	};
	/** The settlement terrain profile. */
	public double[] terrainProfile = new double[2];
	/** The settlement mission directive modifiers. */
	public double[] missionModifiers = new double[9];

	/** The settlement sponsor. */
	private String sponsor;
	/** The settlement template name. */
	private String template;
	/** The settlement name. */
	private String name;

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
	private Map<Integer, Map<Integer, Double>> waterConsumption;
	/** The settlement's daily output (resources produced). */
	private Map<Integer, Map<Integer, Double>> dailyResourceOutput;
	/** The settlement's daily labor hours output. */
	private Map<Integer, Map<Integer, Double>> dailyLaborTime;

	private Map<Integer, Boolean> allowTradeMissionSettlements;
	
	// Static members	
	private static final int sample1 = ResourceUtil.findIDbyAmountResourceName("regolith");// "polyethylene");
	private static final int sample2 = ResourceUtil.findIDbyAmountResourceName("ice");// concrete");

	private static final int oxygenID = ResourceUtil.oxygenID;
	private static final int waterID = ResourceUtil.waterID;
	private static final int co2ID = ResourceUtil.co2ID;
	private static final int foodID = ResourceUtil.foodID;
	private static final int methaneID = ResourceUtil.methaneID;

	private static MaintenanceMeta maintenanceMeta;
	private static MaintenanceEVAMeta maintenanceEVAMeta;
	private static RepairMalfunctionMeta repairMalfunctionMeta;
	private static RepairEVAMalfunctionMeta repairEVAMalfunctionMeta;

	private static ConstructBuildingMeta constructBuildingMeta;
	private static BuildingConstructionMissionMeta buildingConstructionMissionMeta;
	private static CollectRegolithMeta collectRegolithMeta;
	private static CollectIceMeta collectIceMeta;

	private static SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
	private static PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
	
	private static List<String> travelMissionNames;
	
	static {
		/**
		 * Creates an array of all missions
		 */
		travelMissionNames = Arrays.asList(
					AreologyFieldStudy.DEFAULT_DESCRIPTION,
					BiologyFieldStudy.DEFAULT_DESCRIPTION,
					CollectIce.DEFAULT_DESCRIPTION,
					CollectRegolith.DEFAULT_DESCRIPTION,
					EmergencySupply.DEFAULT_DESCRIPTION,
					
					Exploration.DEFAULT_DESCRIPTION,
					MeteorologyFieldStudy.DEFAULT_DESCRIPTION,
					Mining.DEFAULT_DESCRIPTION,
					RescueSalvageVehicle.DEFAULT_DESCRIPTION,
					Trade.DEFAULT_DESCRIPTION,
					
					TravelToSettlement.DEFAULT_DESCRIPTION
			);
	}
	
	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return uniqueCount++;
	}
	
	/**
	 * Get the unique identifier for this settlement
	 * 
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	public void incrementID() {
		// Gets the identifier
		this.identifier = getNextIdentifier();
	}
	
	static {
		water_consumption_rate = personConfig.getWaterConsumptionRate();
		minimum_air_pressure = personConfig.getMinAirPressure();
		life_support_value = settlementConfig.loadLifeSupportRequirements();
	}
	
	/**
	 * Constructor 1 called by ConstructionStageTest suite for maven testing.
	 */
	private Settlement() {
		super(null, null);
	
		unitManager = sim.getUnitManager();
		// Add this settlement to the lookup map
		unitManager.addSettlementID(this);
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
	 * @param scenarioID
	 * @param location
	 */
	public Settlement(String name, int scenarioID, Coordinates location) {
		// Use Structure constructor.
		super(name, location);
		
		if (unitManager == null) {// for passing maven test
			unitManager = sim.getUnitManager();
		}

		if (unitManager != null) {// for passing maven test
			// Add this settlement to the lookup map
			unitManager.addSettlementID(this);
		}
		
		this.name = name;
		this.scenarioID = scenarioID;
		this.location = location;

		if (missionManager == null) {// for passing maven test
			missionManager = sim.getMissionManager();
		}
	}

	/**
	 * The static factory method called by ConstructionStageTest to return a new
	 * instance of Settlement for maven testing.
	 * 
	 * @param name
	 * @param scenarioID
	 * @param location
	 * @return
	 */
	public static Settlement createMockSettlement(String name, int scenarioID, Coordinates location) {
		return new Settlement(name, scenarioID, location);
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
	private Settlement(String name, int id, String template, String sponsor, Coordinates location, int populationNumber,
			int projectedNumOfRobots) {
		// Use Structure constructor
		super(name, location);
		
		unitManager = sim.getUnitManager();
		// Add this settlement to the lookup map
		unitManager.addSettlementID(this);
		
		this.name = name;
		this.template = template;
		this.sponsor = sponsor;
		this.location = location;
		this.scenarioID = id;
		this.projectedNumOfRobots = projectedNumOfRobots;
		this.initialPopulation = populationNumber;

		// Determine the reporting authority
		setReportingAuthority(sponsor);
		// Determine the mission directive modifiers
		determineMissionAgenda();
		
//		// The surface of Mars contains this settlement
//		setContainerUnit(marsSurface);
//		// Set the containerID
//		setContainerID(Unit.MARS_SURFACE_ID);
		
		// Create Task instances
		maintenanceMeta = new MaintenanceMeta();
		maintenanceEVAMeta = new MaintenanceEVAMeta();
		repairMalfunctionMeta = new RepairMalfunctionMeta();
		repairEVAMalfunctionMeta = new RepairEVAMalfunctionMeta();
		constructBuildingMeta = new ConstructBuildingMeta();

		buildingConstructionMissionMeta = new BuildingConstructionMissionMeta();
		collectRegolithMeta = new CollectRegolithMeta();
		collectIceMeta = new CollectIceMeta();

		int size = missionsDisable.length;
		for (int i=0; i<size; i++) {
			missionsDisable[i] = false;
		}
		
		allowTradeMissionSettlements = new HashMap<Integer, Boolean>(); 
		
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
	public static Settlement createNewSettlement(String name, int id, String template, String sponsor,
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
		scientificAchievement = new HashMap<ScienceType, Double>(0);
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
		waterConsumption = new ConcurrentHashMap<>();
		// Create the daily output map
		dailyResourceOutput = new ConcurrentHashMap<>();
		// Create the daily labor hours map
		dailyLaborTime = new ConcurrentHashMap<>();
//		logger.config("Done initialize()");
	}

	/*
	 * Sets sponsoring agency for this settlement
	 */
	public void setReportingAuthority(String sponsor) {
		
		if (sponsor.contains(ReportingAuthorityType.CNSA.getName())) {
			ra = CNSAMissionControl.createMissionControl(); // ProspectingMineral

		} else if (sponsor.contains(ReportingAuthorityType.CSA.getName())) {
			ra = CSAMissionControl.createMissionControl(); // AdvancingSpaceKnowledge

		} else if (sponsor.contains(ReportingAuthorityType.ESA.getName())) {
			ra = ESAMissionControl.createMissionControl(); // DevelopingSpaceActivity;

		} else if (sponsor.contains(ReportingAuthorityType.ISRO.getName())) {
			ra = ISROMissionControl.createMissionControl(); // DevelopingAdvancedTechnology

		} else if (sponsor.contains(ReportingAuthorityType.JAXA.getName())) {
			ra = JAXAMissionControl.createMissionControl(); // ResearchingSpaceApplication

		} else if (sponsor.contains(ReportingAuthorityType.NASA.getName())) {
			ra = NASAMissionControl.createMissionControl(); // FindingLife

		} else if (sponsor.contains(ReportingAuthorityType.RKA.getName())) {
			ra = RKAMissionControl.createMissionControl(); // ResearchingHealthHazard

		} else if (sponsor.contains(ReportingAuthorityType.MS.getName())) {
			ra = MarsSocietyMissionControl.createMissionControl(); // SettlingMars

		} else if (sponsor.contains(ReportingAuthorityType.SPACEX.getName())) {
			ra = SpaceXMissionControl.createMissionControl(); // BuildingSelfSustainingColonies

		} else {
			logger.warning(getName() + " has no reporting authority!");
		}
	}
	
	
	public void determineMissionAgenda() {
		int[][] mod = ra.getMissionAgenda().getMissionModifiers();
		int row = mod.length;
		for (int i=0; i<row; i++) {
			int sum = 0;
			int column = mod[0].length;
			for (int j=0; j<column; j++) {
				sum += mod[i][j];
			}
			double total = 1D * sum / row;
			setMissionDirectiveModifiers(i, total);
		}
		
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
	public Map<Building, List<Building>> createBuildingConnectionMap() {
//		adjacentBuildingMap.clear();
		if (adjacentBuildingMap == null)
			adjacentBuildingMap = new HashMap<>();
		for (Building b : buildingManager.getBuildings()) {
			List<Building> connectors = createAdjacentBuildingConnectors(b);
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
			adjacentBuildingMap = createBuildingConnectionMap();

		return adjacentBuildingMap.get(building);
	}

	/**
	 * Creates a list of building connectors attached to this building
	 * 
	 * @param building
	 * @return a list of building connectors
	 */
	public List<Building> createAdjacentBuildingConnectors(Building building) {
		List<Building> buildings = new ArrayList<>();
		Set<BuildingConnector> connectors = getConnectionsToBuilding(building);
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

	public Set<BuildingConnector> getConnectionsToBuilding(Building building) {
		return getBuildingConnectorManager().getConnectionsToBuilding(building);
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
		return scenarioID;
	}

	/**
	 * Gets the population capacity of the settlement
	 * 
	 * @return the population capacity
	 */
	public int getPopulationCapacity() {
		int result = 0;
		List<Building> bs = buildingManager.getBuildings(FunctionType.LIVING_ACCOMODATIONS);
		for (Building building : bs) {
			result += building.getLivingAccommodations().getBeds();
		}

		return result;
	}

	public int getSleepers() {
		int result = 0;
		List<Building> bs = buildingManager.getBuildings(FunctionType.LIVING_ACCOMODATIONS);
		for (Building building : bs) {
			result += building.getLivingAccommodations().getSleepers();
		}
		return result;
	}

	public int getTotalNumDesignatedBeds() {
		int result = 0;
		List<Building> bs = buildingManager.getBuildings(FunctionType.LIVING_ACCOMODATIONS);
		for (Building building : bs) {
			result += building.getLivingAccommodations().getBedMap().size();
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
		
//		return Math.toIntExact(getInventory().getAllContainedUnits().stream().filter(p -> p instanceof Person)
//				.collect(Collectors.counting()));

		// TODO: need to factor in those inside a vehicle parked inside a garage

//		int n = 0;
//		Iterator<Unit> i = getInventory().getAllContainedUnits().iterator();
//		while (i.hasNext()) {
//			if (i.next() instanceof Person)
//				n++;
//		}
//		
//		return n;

	}

	public void endAllIndoorTasks() {
		for (Person p : getIndoorPeople()) {
			p.getMind().getTaskManager().clearAllTasks();
		}
	}

	/**
	 * Gets a collection of the people who are currently inside the settlement.
	 * 
	 * @return Collection of people within
	 */
	public Collection<Person> getIndoorPeople() {
		return getPeopleWithin();
		
//		return CollectionUtils.getPerson(getInventory().getContainedUnits());
//		return allAssociatedPeople
//				.stream().filter(p -> !p.isDeclaredDead()
//						&& p.getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT && p.getSettlement() == this)
//				.collect(Collectors.toList());
	}

	/**
	 * Gets a collection of people who are doing EVA outside the settlement.
	 * 
	 * @return Collection of people
	 */
	public Collection<Person> getOutsideEVAPeople() {

		return citizens.stream().filter(
				p -> !p.isDeclaredDead() && (p.getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY
						|| p.getLocationStateType() == LocationStateType.OUTSIDE_ON_THE_SURFACE_OF_MARS))
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
	 * Gets the number of people currently doing EVA outside the settlement
	 * 
	 * @return the available population capacity
	 */
	public int getNumOutsideEVAPeople() {
		Collection<Person> ppl = getOutsideEVAPeople();
		if (ppl == null || ppl.isEmpty())
			return 0;
		else
			return ppl.size();
	}

	/**
	 * Gets the current available population capacity of the settlement
	 * 
	 * @return the available population capacity
	 */
	public int getAvailableSpace() {
		return getPopulationCapacity() - numCitizens;// getIndoorPeopleCount();
	}

//	/**
//	 * Gets an array of current inhabitants of the settlement
//	 * 
//	 * @return array of inhabitants
//	 */
//	public Person[] getInhabitantArray() {
//		return getIndoorPeople().toArray(new Person[getIndoorPeople().size()]);
//		
//		Collection<Person> people = getIndoorPeople();
//		Person[] personArray = new Person[people.size()];
//		Iterator<Person> i = people.iterator();
//		int count = 0;
//		while (i.hasNext()) {
//			personArray[count] = i.next();
//			count++;
//		}
//		return personArray;
//	}

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
		
		
//		int n = 0;
//		Iterator<Unit> i = getInventory().getAllContainedUnits().iterator();
//		while (i.hasNext()) {
//			if (i.next() instanceof Robot)
//				n++;
//		}
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
	 * Gets the current available robot capacity of the settlement
	 * 
	 * @return the available robots capacity
	 */
	public int getAvailableRobotCapacity() {
		return getRobotCapacity() - numOwnedBots;
	}

	/**
	 * Gets an array of current robots of the settlement
	 * 
	 * @return array of robots
	 */
	public Robot[] getRobotArray() {
		return getRobots().toArray(new Robot[getRobots().size()]);
//		Collection<Robot> robots = getRobots();
//		Robot[] robotArray = new Robot[robots.size()];
//		Iterator<Robot> i = robots.iterator();
//		int count = 0;
//		while (i.hasNext()) {
//			robotArray[count] = i.next();
//			count++;
//		}
//		return robotArray;
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
				LogConsolidated.log(Level.SEVERE, 10_000, sourceName, "[" + this.getName()
						+ "] out-of-range overall air pressure at " + Math.round(p * 10D) / 10D + " kPa detected.");
				return false;
			}

			double t = currentTemperature;
			if (t < life_support_value[0][4] - SAFE_TEMPERATURE_RANGE
					|| t > life_support_value[1][4] + SAFE_TEMPERATURE_RANGE) {
				LogConsolidated.log(Level.SEVERE, 10_000, sourceName,
						"[" + this.getName() + "] out-of-range overall temperature at " + Math.round(t * 10D) / 10D
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
			// System.out.println("oxygenLeft : " + oxygenLeft);
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
			LogConsolidated.log(Level.SEVERE, 5000, sourceName, name + " - Error in providing O2/removing CO2: ", e);
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
			LogConsolidated.log(Level.SEVERE, 5000, sourceName, name + " - Error in providing H2O needs: ", e);
		}

		return waterTaken;
//		return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
	}

	/**
	 * Computes the average air pressure of the life support system.
	 * 
	 * @return air pressure [kPa]
	 */
	public double computeAveragePressure() {
//		double total = 0;
//		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();
//		int size = buildings.size();
//		for (Building b : buildings) {
//			int id = b.getInhabitableID();
//			total += compositionOfAir.getTotalPressure()[id];
//		}
//		// convert from atm to kPascal
//		return total * CompositionOfAir.KPA_PER_ATM / size;

		double totalArea = 0;
		double totalPressureArea = 0;
		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();
		for (Building b : buildings) {
			int id = b.getInhabitableID();
			double area = b.getFloorArea();
			totalArea += area;
			totalPressureArea += compositionOfAir.getTotalPressure()[id] * area;
		}
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
	public double computeAverageTemperature() {
//		List<Building> buildings = buildingManager.getBuildingsWithThermal();
//		int size = buildings.size();
//		double total = 0;
//		for (Building b : buildings) {
//			total += b.getCurrentTemperature();
//		}
//
//		return total / size;

		double totalArea = 0;
		double totalTArea = 0;
		List<Building> buildings = buildingManager.getBuildingsWithThermal();
		for (Building b : buildings) {
			double area = b.getFloorArea();
			totalArea += area;
			totalTArea += b.getCurrentTemperature() * area;
		}
		return totalTArea / totalArea;
	}

	public ShiftType getCurrentSettlementShift() {

		int millisols = marsClock.getMillisolInt();

		int num = getNumShift();

		if (num == 2) {

			if (millisols >= TaskSchedule.A_START && millisols <= TaskSchedule.A_END)
				return ShiftType.A;

			else if (millisols >= TaskSchedule.B_START && millisols <= TaskSchedule.B_END)
				return ShiftType.B;

		}

		else if (num == 3) {

			if (millisols >= TaskSchedule.X_START && millisols <= TaskSchedule.X_END)
				return ShiftType.X;

			else if (millisols >= TaskSchedule.Y_START && millisols <= TaskSchedule.Y_END)
				return ShiftType.Y;

			else if (millisols >= TaskSchedule.Z_START && millisols <= TaskSchedule.Z_END)
				return ShiftType.Z;

		} else
			return ShiftType.ON_CALL;

		return null;

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
	 * Prints the raw scores of certain tasks
	 */
	public void printTaskProbability() {
		double maintScore = maintenanceMeta.getSettlementProbability(this);
		double maintEVAScore = maintenanceEVAMeta.getSettlementProbability(this);
		double repairScore = repairMalfunctionMeta.getSettlementProbability(this);
		double repairEVAScore = repairEVAMalfunctionMeta.getSettlementProbability(this);
//		double buildingScore = constructBuildingMeta.getProbability(this);

		LogConsolidated.log(Level.INFO, 0, sourceName,
				"[" + name + "] MaintenanceMeta Task score : " + Math.round(maintScore * 10.0) / 10.0 + ".");
		LogConsolidated.log(Level.INFO, 0, sourceName,
				"[" + name + "] MaintenanceEVAMeta Task score : " + Math.round(maintEVAScore * 10.0) / 10.0 + ".");
		LogConsolidated.log(Level.INFO, 0, sourceName,
				"[" + name + "] RepairMalfunctionMeta Task score : " + Math.round(repairScore * 10.0) / 10.0 + ".");
		LogConsolidated.log(Level.INFO, 0, sourceName, "[" + name + "] RepairEVAMalfunctionMeta Task score : "
				+ Math.round(repairEVAScore * 10.0) / 10.0 + ".");
//		LogConsolidated.log(Level.INFO,0, sourceName,
//				"[" + name + "] ConstructBuildingMeta Task score : "+  Math.round(buildingScore*10.0)/10.0 + ".");

	}

//	/**
//	 * Prints the raw scores of certain tasks
//	 */
//	public void printMissionProbability() {
//		double bConstScore = buildingConstructionMissionMeta.getSettlementProbability(this);
//		double regolithScore = collectRegolithMeta.getSettlementProbability(this);
//		double iceScore = collectIceMeta.getSettlementProbability(this);
//
//		LogConsolidated.log(Level.INFO, 0, sourceName, "[" + name + "] BuildingConstructionMissionMeta Task score : "
//				+ Math.round(bConstScore * 10.0) / 10.0 + ".");
//		LogConsolidated.log(Level.INFO, 0, sourceName,
//				"[" + name + "] CollectRegolithMeta Task score : " + Math.round(regolithScore * 10.0) / 10.0 + ".");
//		LogConsolidated.log(Level.INFO, 0, sourceName,
//				"[" + name + "] CollectIceMeta Task score : " + Math.round(iceScore * 10.0) / 10.0 + ".");
//	}

	/**
	 * Perform time-related processes
	 *
	 * @param time the amount of time passing (in millisols)
	 * @throws Exception error during time passing.
	 */
	public void timePassing(double time) {
		// If settlement is overcrowded, increase inhabitant's stress.
		// TODO: should the number of robots be accounted for here?

		int overCrowding = getIndoorPeopleCount() - getPopulationCapacity();
		if (overCrowding > 0) {
			double stressModifier = .1D * overCrowding * time;
			for (Person p : getIndoorPeople()) {
				PhysicalCondition c = p.getPhysicalCondition();
				c.setStress(c.getStress() + stressModifier);
			}
		}

		// TODO: what to take into consideration the presence of robots ?
		// If no current population at settlement for one sol, power down the
		// building and turn the heat off ?

		if (powerGrid.getPowerMode() != PowerMode.POWER_UP)
			powerGrid.setPowerMode(PowerMode.POWER_UP);
		// TODO: check if POWER_UP is necessary
		// Question: is POWER_UP a prerequisite of FULL_POWER ?

		powerGrid.timePassing(time);

		thermalSystem.timePassing(time);

		buildingManager.timePassing(time);

		performEndOfDayTasks();

		// Sample a data point every SAMPLE_FREQ (in millisols)
		int millisols = marsClock.getMillisolInt();

		// Avoid checking at < 10 or 1000 millisols
		// due to high cpu util during the change of day
		if (millisols >= 10 && millisols != 1000) {

			// Reduce the recurrent passing score daily to its 90% value
			minimumPassingScore = minimumPassingScore * .9;
			
			// Updates the goods manager 
			updateGoodsManager(time);

			int remainder = millisols % CHECK_GOODS;
			if (remainder == 1) {
				// Update the goods value gradually with the use of buffers
				if (goodsManager.isInitialized()) 
					goodsManager.updateGoodsValueBuffers(time);
			}
			
			remainder = millisols % CHECK_MISSION;
			if (remainder == 1) {
				// Reset the mission probability back to 1
				missionProbability = -1;
				mineralValue = -1;
			}

			remainder = millisols % SAMPLING_FREQ;
			if (remainder == 1) {
				// will NOT check for radiation at the exact 1000 millisols in order to balance
				// the simulation load
				// take a sample for each critical resource
				sampleAllResources();
			}

			remainder = millisols % CHECK_WATER_RATION;
			if (remainder == 1) {
				// Recompute the water ration level
				computeWaterRation();
			}

			// Check every RADIATION_CHECK_FREQ (in millisols)
			// Compute whether a baseline, GCR, or SEP event has occurred
			remainder = millisols % RadiationExposure.RADIATION_CHECK_FREQ;
			if (remainder == 5) {
				checkRadiationProbability(time);
			}

			remainder = millisols % RESOURCE_UPDATE_FREQ;
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

		compositionOfAir.timePassing(time);

		currentPressure = computeAveragePressure();

		currentTemperature = computeAverageTemperature();

		outside_temperature = weather.getTemperature(location);

		if (adjacentBuildingMap != null && !adjacentBuildingMap.isEmpty()) {
			int numConnectors = adjacentBuildingMap.size();

			if (numConnectorsCache != numConnectors) {
				numConnectorsCache = numConnectors;
				createBuildingConnectionMap();
			}
		} else {
			createBuildingConnectionMap();
		}

	}

	public void sampleAllResources() {

		for (int i = 0; i < NUM_CRITICAL_RESOURCES; i++) {
			sampleOneResource(i);
		}
	}

	public void sampleOneResource(int resourceType) {
		int resource = -1;

		if (resourceType == 0) {
			resource = ResourceUtil.oxygenID;// LifeSupportType.OXYGEN;
		} else if (resourceType == 1) {
			resource = ResourceUtil.hydrogenID;// "hydrogen";
		} else if (resourceType == 2) {
			resource = ResourceUtil.co2ID;// "carbon dioxide";
		} else if (resourceType == 3) {
			resource = ResourceUtil.methaneID;// "methane";
		} else if (resourceType == 4) {
			resource = ResourceUtil.waterID;// LifeSupportType.WATER;
		} else if (resourceType == 5) {
			resource = ResourceUtil.greyWaterID;// "grey water";
		} else if (resourceType == 6) {
			resource = ResourceUtil.blackWaterID;// "black water";
		} else if (resourceType == 7) {
			resource = ResourceUtil.rockSamplesID;// "rock samples";
		} else if (resourceType == 8) {
			resource = ResourceUtil.iceID;// "ice";
		}

		if (resourceStat == null)
			resourceStat = new HashMap<>();
		if (resourceStat.containsKey(solCache)) {
			Map<Integer, List<Double>> todayMap = resourceStat.get(solCache);

			if (todayMap.containsKey(resourceType)) {
				List<Double> list = todayMap.get(resourceType);
				double newAmount = getInventory().getAmountResourceStored(resource, false);
				list.add(newAmount);

			}

			else {
				List<Double> list = new ArrayList<>();
				double newAmount = getInventory().getAmountResourceStored(resource, false);
				list.add(newAmount);
				todayMap.put(resourceType, list);
			}

		}

		else {
			List<Double> list = new ArrayList<>();
			Map<Integer, List<Double>> todayMap = new HashMap<>();
			double newAmount = getInventory().getAmountResourceStored(resource, false);
			list.add(newAmount);
			todayMap.put(resourceType, list);
			resourceStat.put(solCache, todayMap);
		}
	}

	/*
	 * Gets the average amount of a resource on a particular sol
	 */
	// Called by getOneResource() in MarqueeTicker.java
	public double getAverage(int solType, int resourceType) {
		int sol = 0;
		if (solType == 0) // today's average
			sol = solCache;
		else if (solType == -1) // yesterday's average
			sol = solCache - 1;
		else if (solType == -3) // average from 3 sols ago
			sol = solCache - 3;
		else if (solType == -10) // average from 10 sols ago
			sol = solCache - 10;

		int size = 0;
		double average = 0;

		if (resourceStat.containsKey(sol)) {
			Map<Integer, List<Double>> map = resourceStat.get(sol);
			// map.containsKey(resourceType));
			if (map.containsKey(resourceType)) {
				List<Double> list = map.get(resourceType);
				size = list.size();
				for (int i = 0; i < size; i++) {
					average += list.get(i);
				}

				average = average / size;

			} else {
				average = 0; // how long will it be filled ? ?
			}

		} else
			average = 0;

		// if (size != 0)
		// average = average/size;

		return average;
	}

//	 public void updateRegistry() {
//
//		 List<SettlementRegistry> settlementList =  MultiplayerClient.getInstance().getSettlementRegistryList();
//		 int clientID = Integer.parseInt( st.nextToken().trim() );
//		 
//		 String template = st.nextToken().trim(); int pop = Integer.parseInt(
//		 st.nextToken().trim() ); int bots = Integer.parseInt(
//		 st.nextToken().trim() ); double lat = Double.parseDouble(
//		 st.nextToken().trim() ); double lo = Double.parseDouble(
//		 st.nextToken().trim() );
//		 
//		 settlementList.forEach( s -> { 
//			 String pn = s.getPlayerName(); 
//			 String sn = s.getName(); 
//		 
//			 if (pn.equals(playerName) && sn.equals(name))
//				 s.updateRegistry(playerName, clientID, name, template, pop, bots, lat, lo); 
//		});
//	 
//	 }

	/**
	 * Provides the daily statistics on inhabitant's food energy intake
	 */
	public void getFoodEnergyIntakeReport() {
		Iterator<Person> i = citizens.iterator();
		while (i.hasNext()) {
			Person p = i.next();
			PhysicalCondition condition = p.getPhysicalCondition();
			double energy = Math.round(condition.getEnergy() * 100.0) / 100.0;
			String name = p.getName();
			LogConsolidated.log(Level.INFO, 0, sourceName,
					"[" + this + "] " + name + "'s current energy level : " + energy + " kJ");
		}
	}

	/**
	 * Provides the daily reports for the settlement
	 */
	public void performEndOfDayTasks() {
		// check for the passing of each day
		int solElapsed = marsClock.getMissionSol();
		if (solCache != solElapsed) {

			// Limit the size of the dailyWaterUsage to x key value pairs
			if (waterConsumption.size() > MAX_NUM_SOLS)
				waterConsumption.remove(solElapsed - MAX_NUM_SOLS);

			// Limit the size of the dailyWaterUsage to x key value pairs
			if (dailyResourceOutput.size() > MAX_SOLS_DAILY_OUTPUT)
				dailyResourceOutput.remove(solElapsed - MAX_SOLS_DAILY_OUTPUT);

//			printTaskProbability();
//			printMissionProbability();

			// getFoodEnergyIntakeReport();
			reassignWorkShift();

			tuneJobDeficit();

			refreshResourceStat();

			refreshSleepMap(solElapsed);

//			getSupplyDemandSampleReport(solElapsed);

			refreshSupplyDemandMap(solElapsed);

			solCache = solElapsed;
		}
	}

	public void refreshResourceStat() {
		if (resourceStat == null)
			resourceStat = new HashMap<>();
		// Remove the resourceStat map data from 12 days ago
		if (resourceStat.size() > RESOURCE_STAT_SOLS)
			resourceStat.remove(0);
		// if (counter30 == 31) {
		// resourceStat.remove(0);
		// resourceStat.clear();
		// resourceStat = new HashMap<>();
		// counter30--;
		// }
		// else
		// counter30++;
	}

	/***
	 * Refreshes the sleep map for each person in the settlement
	 * 
	 * @param solElapsed
	 */
	public void refreshSleepMap(int solElapsed) {
		// Update the sleep pattern once every x number of days
		if (solElapsed % SOL_SLEEP_PATTERN_REFRESH == 0) {
			Collection<Person> people = getIndoorPeople();
			for (Person p : people) {
				p.getCircadianClock().inflateSleepHabit();
			}
		}
	}

	/***
	 * Prints out the work shift status for this settlement
	 * 
	 * @param sol
	 */
	public void printWorkShift(String sol) {
		logger.info(sol + " " + getName() + "'s Work Shift " + "-- [A:" + numA + " B:" + numB + "], [X:" + numX + " Y:"
				+ numY + " Z:" + numZ + "], OnCall:" + numOnCall + ", Off:" + numOff);
	}

	/*
	 * Reassigns the work shift for all
	 */
	public void reassignWorkShift() {
		// TODO: should call this method at, say, 800 millisols, not right at 1000
		// millisols
		Collection<Person> people = citizens;// getIndoorPeople();
		int pop = people.size();

		int nShift = 0;

		if (pop == 1) {
			nShift = 1;
		} else if (pop < UnitManager.THREE_SHIFTS_MIN_POPULATION) {
			nShift = 2;
		} else {// if pop => 6
			nShift = 3;
		}

//		System.out.println();
//		System.out.println(this + " used to have " + numShiftsCache + " shifts.");

		if (numShiftsCache != nShift) {
			numShiftsCache = nShift;

//			System.out.println(this + " now has " + nShift + " shifts.");

			for (Person p : people) {

				// Skip the person who is dead or is on a mission
				if (!p.isBuried() && !p.isDeclaredDead() && !p.getPhysicalCondition().isDead()
						&& p.getMind().getMission() == null) {

					// Check if person is an astronomer.
					boolean isAstronomer = (p.getMind().getJob() instanceof Astronomer);

					ShiftType oldShift = p.getTaskSchedule().getShiftType();

//					System.out.println(p + " has " + oldShift + " shift in " + this);

					if (isAstronomer) {
						// TODO: find the darkest time of the day
						// and set work shift to cover time period

						// For now, we may assume it will usually be X or Z, NOT Y
						// since Y is usually where midday is at unless a person is at polar region.
						if (oldShift == ShiftType.Y) {

							boolean x_ok = isWorkShiftSaturated(ShiftType.X, false);
							boolean z_ok = isWorkShiftSaturated(ShiftType.Z, false);
							// TODO: Instead of throwing a dice,
							// take the shift that has less sunlight
							int rand;
							ShiftType newShift = null;

							if (x_ok && z_ok) {
								rand = RandomUtil.getRandomInt(1);
								if (rand == 0)
									newShift = ShiftType.X;
								else
									newShift = ShiftType.Z;
							}

							else if (x_ok)
								newShift = ShiftType.X;

							else if (z_ok)
								newShift = ShiftType.Z;

							p.setShiftType(newShift);
						}

					} // end of if (isAstronomer)

					else {
						// Not an astronomer
						// Note: if a person's shift is over-filled or saturated, he will need to change
						// shift

						// Get an unfilled work shift
						ShiftType newShift = getAnEmptyWorkShift(pop);
//						System.out.println(this + " found a new unfilled work shift : " + newShift);

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
							// Not on-call

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
//				System.out.println(p + " is now on " + p.getShiftType() + " shift in " + this);
			} // end of people for loop
		} // end of for loop
	}

	/**
	 * Provides the daily demand statistics on sample amount resources
	 */
	// Added supply data
	public void getSupplyDemandSampleReport(int solElapsed) {
		logger.info("<<< Sol " + solElapsed + " at " + this.getName()
				+ " End of Day Report of Amount Resource Supply and Demand Statistics >>>");

		// Sample supply and demand data on Potato and Water

		double vp1 = goodsManager.getGoodValuePerItem(GoodsUtil.getResourceGood(sample1));
		double vp2 = goodsManager.getGoodValuePerItem(GoodsUtil.getResourceGood(sample2));

		double supplyAmount1 = getInventory().getAmountSupply(sample1);
		double supplyAmount2 = getInventory().getAmountSupply(sample2);

		int supplyRequest1 = getInventory().getAmountSupplyRequest(sample1);
		int supplyRequest2 = getInventory().getAmountSupplyRequest(sample2);

		double demandAmount1 = getInventory().getAmountDemand(sample1);
		double demandAmount2 = getInventory().getAmountDemand(sample2);

		// For items :
		double demandItem1 = getInventory().getItemDemand(sample1);
		double demandItem2 = getInventory().getItemDemand(sample2);

		// int totalRequest1 = getInventory().getDemandTotalRequest(sample1);
		// int totalRequest2 = getInventory().getDemandTotalRequest(sample2);

		int demandSuccessfulRequest1 = getInventory().getAmountDemandMetRequest(sample1);
		int demandSuccessfulRequest2 = getInventory().getAmountDemandMetRequest(sample2);
		// int numOfGoodsInDemandAmountMap = getInventory().getDemandAmountMapSize();
		// int numOfGoodsInDemandTotalRequestMap =
		// getInventory().getDemandTotalRequestMapSize();
		// int numOfGoodsInDemandSuccessfulRequestMap =
		// getInventory().getDemandSuccessfulRequestMapSize();

		// logger.info(" numOfGoodsInDemandRequestMap : " +
		// numOfGoodsInDemandTotalRequestMap);
		// logger.info(" numOfGoodsInDemandSuccessfulRequestMap : " +
		// numOfGoodsInDemandSuccessfulRequestMap);
		// logger.info(" numOfGoodsInDemandAmountMap : " +
		// numOfGoodsInDemandAmountMap);
		String name1 = ResourceUtil.findAmountResourceName(sample1);
		String name2 = ResourceUtil.findAmountResourceName(sample2);

		logger.info(name1 + " (" + sample1 + ")" + "  vp : " + Math.round(vp1 * 100.0) / 100.0);
		logger.info(name2 + " (" + sample2 + ")" + "  vp : " + Math.round(vp2 * 100.0) / 100.0);

		logger.info(name1 + " Supply Amount : " + Math.round(supplyAmount1 * 100.0) / 100.0);
		logger.info(name1 + " Supply Request : " + supplyRequest1);

		logger.info(name1 + " Demand Amount : " + Math.round(demandAmount1 * 100.0) / 100.0);
		// logger.info(sample1 + " Demand Total Request : " + totalRequest1);
		logger.info(name1 + " Demand Successful Request : " + demandSuccessfulRequest1);

		logger.info(name2 + " Supply Amount : " + Math.round(supplyAmount2 * 100.0) / 100.0);
		logger.info(name2 + " Supply Request : " + supplyRequest2);

		logger.info(name2 + " Demand Amount : " + Math.round(demandAmount2 * 100.0) / 100.0);
		// logger.info(sample2 + " Demand Total Request : " + totalRequest2);
		logger.info(name2 + " Demand Successful Request : " + demandSuccessfulRequest2);

	}

	/**
	 * Refreshes and clears settlement's data on the supply/demand and weather
	 * 
	 * @param solElapsed # of sols since the start of the sim
	 */
	public void refreshSupplyDemandMap(int solElapsed) {
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
	private void updateGoodsManager(double time) {

		if (justLoaded) {
			justLoaded = false;
			goodsManager.timePassing(time);
		}

		goodsManagerUpdateTime += time;

		// Randomly update goods manager twice per Sol.
		double timeThreshold = 250D + RandomUtil.getRandomDouble(250D);
		if (!goodsManager.isInitialized() || (goodsManagerUpdateTime > timeThreshold)) {
			goodsManager.timePassing(time);
			goodsManagerUpdateTime = 0D;
		}
	}

	/**
	 * Gets a collection of people affected by this entity.
	 *
	 * @return person collection
	 */
	// TODO: will this method be called by robots?
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>(getIndoorPeople());

		// Check all people.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this settlement.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}

			// Add all people repairing this settlement.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}
		}

		return people;
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
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();
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

	public boolean isIdleTask(Task task) {
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

	public Airlock getClosestAvailableAirlock(Robot robot) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;
		BuildingManager manager = buildingManager;
		Iterator<Building> i = manager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (!ASTRONOMY_OBSERVATORY.equalsIgnoreCase(building.getBuildingType())) {
				double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), robot.getXLocation(),
						robot.getYLocation());
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
			LogConsolidated.log(Level.WARNING, 10_000, sourceName, person.getName() + " is not currently in a building.");
			return null;
		}

		result = getAirlock(currentBuilding, xLocation, yLocation);

		return result;
	}

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
			LogConsolidated.log(Level.WARNING, 10_000, sourceName, robot.getName() + " is not currently in a building.");
			return null;
		}

		result = getAirlock(currentBuilding, xLocation, yLocation);

		return result;
	}

	public Airlock getAirlock(Building currentBuilding, double xLocation, double yLocation) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;
//		BuildingManager manager = buildingManager;
		Iterator<Building> i = buildingManager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building building = i.next();

			if (buildingConnectorManager.hasValidPath(currentBuilding, building)) {

				double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), xLocation,
						yLocation);
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
	 * Gets the number of deceased people
	 * 
	 * @return int
	 */
	public int getNumBuried() {
		return getBuriedPeople().size();
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
	 * Gets the number of deceased people
	 * 
	 * @return int
	 */
	public int getNumDeceased() {
		return getDeceasedPeople().size();
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
	 * Gets a list of people (may or may not be citizens) within this settlement
	 * 
	 * @return collection of people within.
	 */
	public Collection<Person> getPeopleWithin() {
		return peopleWithin;
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
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT, this);
			// Update the numCtizens
			numCitizens = citizens.size();
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
	 * Checks if the settlement has a particular person
	 * 
	 * @param aPerson
	 * @return boolean
	 */
	public boolean hasPerson(Person aPerson) {

		return citizens.stream()
				// .filter(p -> p.equals(aPerson))
				// .findFirst() != null;
				.anyMatch(p -> p.equals(aPerson));
	}

	/**
	 * Returns a list of persons with a given name (first or last)
	 * 
	 * @param aName a person's first/last name
	 * @param exactMatch want an exact word-to-word match
	 * @return a list of persons
	 */
	public List<Person> returnPersonList(String aName, boolean exactMatch) {
		List<Person> personList = new ArrayList<>();
		aName = aName.trim();

		// Checked if "," is presented in "last, first"
		if (aName.contains(", ")) {
			int index = aName.indexOf(",");
			String last = aName.substring(0, index);
			String first = aName.substring(index + 2, aName.length());
			aName = first + " " + last;
		}

		String initial = null;
		boolean hasASpace = aName.contains(" ");
		// int found = 0;
		int s_Index = 0;
		// int dead = 0;

		int len = aName.length();
		boolean hasInitial = len > 3 && hasASpace;

		if (hasInitial) {
			for (int i = 0; i < len; i++) {
				if (aName.charAt(i) == ' ')
					s_Index = i;
			}

			if (s_Index == len - 2) {
				// e.g. Cory_S
				initial = aName.substring(len - 1, len);
				aName = aName.substring(0, len - 2);
				// System.out.println("initial is " + initial);
				// System.out.println("aName is " + aName);
			} else if (s_Index == 1) {
				// e.g. S_Cory
				initial = aName.substring(0);
				aName = aName.substring(2, len);
				// System.out.println("initial is " + initial);
				// System.out.println("aName is " + aName);
			}
		}

		Set<Person> list = new HashSet<>(citizens);

		// Add those who are deceased
		list.addAll(getDeceasedPeople());
		list.addAll(getBuriedPeople());

		Iterator<Person> i = list.iterator();
		while (i.hasNext()) {
			Person person = i.next();
			// Case 1: if aName is a full name
			if (hasASpace && person.getName().equalsIgnoreCase(aName)) {
				// found++;
				personList.add(person);
				// if (person.getPhysicalCondition().isDead())
				// dead--;
				// personList.add(person);
			} else if (!exactMatch) {
				
				if (hasInitial) {
					// Case 2: if aName is a first name + space + last initial
					if (person.getName().toLowerCase().contains((aName + " " + initial).toLowerCase())) {
						// found++;
						personList.add(person);
					}
					// Case 3: if aName is a first initial + space + last name
					else if (person.getName().toLowerCase().contains((initial + " " + aName).toLowerCase())) {
						// found++;
						personList.add(person);
					}
				} else {
					String first = "";
					String last = "";
					String full = person.getName();
					int len1 = full.length();
					// int index1 = 0;
	
					for (int j = len1 - 1; j > 0; j--) {
						// Note: finding the whitespace from the end to 0 (from right to left) works
						// better than from left to right
						// e.g. Mary L. Smith (last name should be "Smith", not "L. Smith"
						if (full.charAt(j) == ' ') {
							// index1 = j;
							first = full.substring(0, j);
							last = full.substring(j + 1, len1);
							break;
						} else {
							first = full;
						}
					}
	
					// Case 4: if aName is a last name
					if (first.equalsIgnoreCase(aName)) {
						// found++;
						personList.add(person);
					}
	
					// Case 5: if aName is a first name
					else if (last != null)
						if (last.equalsIgnoreCase(aName)) {
							// found++;
							personList.add(person);
						}
				}
			}
		}

		return personList;
	}

	/**
	 * Checks if any elements from the two string are matching
	 * 
	 * @param aNameArray
	 * @param vNameArray
	 * @return
	 */
	public boolean hasAnyMatch(String[] aNameArray, String[] vNameArray) {
//		boolean value = false;
		for (String a : aNameArray) {
			for (String v : vNameArray) {
				if (v.equalsIgnoreCase(a))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns a list of robots containing a particular name
	 * 
	 * @param aName bot's name
	 * @param exactMatch want an exact word-to-word match
	 * @return a list of robots
	 *
	 */
	public List<Vehicle> returnVehicleList(String aName, boolean exactMatch) {
		String[] aNameArray = aName.split(" ");
//		String first = elements[0];
//		String[] trailing = Arrays.copyOfRange(elements,1,elements.length);
		
		List<Vehicle> vList = new ArrayList<>();
		Iterator<Vehicle> i = getAllAssociatedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle v = i.next();
			String vName =  v.getName();
			String[] vNameArray = vName.split(" ");
			
			// Case 1: exact match
			if (vName.equalsIgnoreCase(aName)
					|| vName.replace(" ", "").equalsIgnoreCase(aName.replace(" ", ""))) {
				vList.add(v);
			}
			
			else if (!exactMatch && hasAnyMatch(aNameArray, vNameArray)) {
				vList.add(v);
			}
		}

		return vList;
	}

	/**
	 * Returns a list of robots containing a particular name
	 * 
	 * @param aName bot's name
	 * @param exactMatch want an exact word-to-word match
	 * @return a list of robots
	 *
	 */
	public List<Robot> returnRobotList(String aName, boolean exactMatch) {
		List<Robot> robotList = new ArrayList<>();
		aName = aName.trim();
		aName = aName.replace(" ", "");

		String last4digits = null;
		char first = '?';
		char second = '?';
		char third = '?';
		char fourth = '?';

		boolean firstIsDigit = false;
		boolean secondIsDigit = false;
		boolean thirdIsDigit = false;
		boolean fourthIsDigit = false;

		int size = aName.length();

		if (size >= 8) {
			last4digits = aName.substring(size - 4, size);
			first = last4digits.charAt(0);
			second = last4digits.charAt(1);
			third = last4digits.charAt(2);
			fourth = last4digits.charAt(3);

			// System.out.println("The last 4 are : " + first + second + third + fourth);

			firstIsDigit = (first >= '0' && first <= '9');
			secondIsDigit = (second >= '0' && second <= '9');
			thirdIsDigit = (third >= '0' && third <= '9');
			fourthIsDigit = (fourth >= '0' && fourth <= '9');
		}

		Collection<Robot> list = getAllAssociatedRobots();

		Iterator<Robot> i = list.iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			// Case 1: exact match e.g. chefbot001
			if (robot.getName().equalsIgnoreCase(aName)
					|| robot.getName().replace(" ", "").equalsIgnoreCase(aName.replace(" ", ""))) {
				robotList.add(robot);
			}
			// Case 2: some parts are matched
			else if (!exactMatch) {
				// Case 2 e.g. chefbot, chefbot0_, chefbot0__, chefbot1_, chefbot00_, chefbot01_
				// need more information !
				if (robot.getName().replace(" ", "").toLowerCase().contains(aName.toLowerCase().replace(" ", ""))) {
					// System.out.println("aName is a part of " + robot.getName().replace(" ", ""));
					robotList.add(robot);
				}

				else if (size >= 8) { // Case 3 e.g. filter out semi-good names such as chefbot01, chefbot1, chefbot10
										// from bad/invalid name

					// Case 3A : e.g. chefbot0003 -- a typo having an extra zero before the digit
					if (size >= 11 && !firstIsDigit && secondIsDigit && thirdIsDigit && fourthIsDigit) {
						if (first == '0' && second == '0') {
							String newName = aName.substring(0, size - 4) + aName.substring(size - 3, size);
							// System.out.println("Case 3A: newName is : " + newName);
							boolean result = checkRobotName(robot, newName);
							if (result)
								robotList.add(robot);
						}
					}

					// Case 3B : e.g. chefbot01 or chefbot11 -- a typo missing a zero before the
					// digit
					else if (size >= 9 && !firstIsDigit && !secondIsDigit && thirdIsDigit && fourthIsDigit) {
						String newName = aName.substring(0, size - 2) + "0" + aName.substring(size - 2, size);
						// System.out.println("Case 3B: newName is : " + newName);
						boolean result = checkRobotName(robot, newName);
						if (result)
							robotList.add(robot);
					}

					// Case 3C : e.g. chefbot1 -- a typo missing two zeroes before the digit
					else if (size >= 8 && !firstIsDigit && !secondIsDigit && !thirdIsDigit && fourthIsDigit) {
						String newName = aName.substring(0, size - 1) + "00" + aName.substring(size - 1, size);
						// System.out.println("Case 3C: newName is : " + newName);
						boolean result = checkRobotName(robot, newName);
						if (result)
							robotList.add(robot);
					}
				}
			}
		}

		return robotList;
	}

	/**
	 * Checks against the input name with the robot name
	 * 
	 * @param robot
	 * @param aName
	 * @return
	 */
	public boolean checkRobotName(Robot robot, String aName) {
		// System.out.println("modified aName is " + aName);
		// aName = aName.substring(0, size-2) + "0" + aName.substring(size-2, size-1);
		if (robot.getName().replace(" ", "").equalsIgnoreCase(aName)) {
			return true;
		} else
			return false;
	}

//	/**
//	 * Returns a collection of deceased people buried outside this settlement
//	 * 
//	 * @return {@link Collection<Person>}
//	 */
//	public Collection<Person> getDecomissionedRobots() {
//		// using java 8 stream
//		return Simulation.instance().getUnitManager()
//				.getRobots().stream()
//				.filter(r -> r.getDecommissionedSettlement() == this)
//				.collect(Collectors.toList());
//	}

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
	 * Gets all associated vehicles currently reserved for mission or on mission
	 *
	 * @return collection of vehicles.
	 */
	public Collection<Vehicle> getMissionVehicles() {
		return getAllAssociatedVehicles().stream()
				.filter(v -> v.isReservedForMission())
				.collect(Collectors.toList());

//		Collection<Vehicle> result = new ArrayList<Vehicle>();
//
//		Iterator<Mission> i = missionManager.getMissionsForSettlement(this).iterator();
//		while (i.hasNext()) {
//			Mission mission = i.next();
//			if (mission instanceof VehicleMission) {
//				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
//				if ((vehicle != null) && !this.equals(vehicle.getSettlement()))
//					result.add(vehicle);
//			}
//		}
//
//		return result;
	}

	/**
	 * Gets a collection of vehicles parked or garaged at the settlement.
	 *
	 * @return Collection of parked vehicles
	 */
	public Collection<Vehicle> getParkedVehicles() {
		return getInventory().getContainedVehicles();
//		return getAllAssociatedVehicles()
//				.stream()
//				.filter(v -> !v.isSalvaged() && v.isParked())//!v.isReservedForMission())
//				.collect(Collectors.toList());
	}

	/**
	 * Gets the number of vehicles parked or garaged at the settlement.
	 * 
	 * @return parked vehicles number
	 */
	public int getParkedVehicleNum() {
		return getInventory().getNumContainedVehicles();
//		return Math.toIntExact(getAllAssociatedVehicles().stream()
//				.filter(v -> !v.isSalvaged() && v.isParked())// !v.isReservedForMission())
//				.collect(Collectors.counting()));
//		return getParkedVehicles().size();
	}

	/**
	 * Gets the number of vehicles owned by the settlement.
	 * 
	 * @return number of owned vehicles
	 */
	public int getVehicleNum() {
		return numOwnedVehicles;
	}

	public Collection<Vehicle> getLUVs(int mode) {
		Collection<Vehicle> LUVs = new ArrayList<Vehicle>();

		if (mode == 0 || mode == 1) {
			Collection<Vehicle> parked = getParkedVehicles();
			Iterator<Vehicle> i = parked.iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				if (vehicle instanceof LightUtilityVehicle) {
					LUVs.add(vehicle);
				}
			}
		}

		if (mode == 0 || mode == 2) {
			Collection<Vehicle> onMission = getMissionVehicles();
			Iterator<Vehicle> j = onMission.iterator();
			while (j.hasNext()) {
				Vehicle vehicle = j.next();
				if (vehicle instanceof LightUtilityVehicle) {
					LUVs.add(vehicle);
				}
			}
		}
		return LUVs;
	}

	public List<Vehicle> getCargoRovers(int mode) {

		List<Vehicle> rovers = new ArrayList<Vehicle>();

		if (mode == 0 || mode == 1) {
			Collection<Vehicle> parked = getParkedVehicles();
			Iterator<Vehicle> i = parked.iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				// if (vehicle instanceof Rover) {
				String d = vehicle.getVehicleType();
				// System.out.println("type is " + d);
				if (d.equals(VehicleType.CARGO_ROVER.getName()))
					rovers.add(vehicle);
				// }
			}
		}

		if (mode == 0 || mode == 2) {
			Collection<Vehicle> onMission = getMissionVehicles();
			Iterator<Vehicle> j = onMission.iterator();
			while (j.hasNext()) {
				Vehicle vehicle = j.next();
				// if (vehicle instanceof Rover) {
				String d = vehicle.getVehicleType();
				if (d.equals(VehicleType.CARGO_ROVER.getName()))
					rovers.add(vehicle);
				// }
			}
		}
		return rovers;
	}

	public List<Vehicle> getTransportRovers(int mode) {
		List<Vehicle> rovers = new ArrayList<Vehicle>();

		if (mode == 0 || mode == 1) {
			Collection<Vehicle> parked = getParkedVehicles();
			Iterator<Vehicle> i = parked.iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				// if (vehicle instanceof Rover) {
				String d = vehicle.getVehicleType();
				// System.out.println("type is " + d);
				if (d.equals(VehicleType.TRANSPORT_ROVER.getName()))
					rovers.add(vehicle);
				// }
			}
		}

		if (mode == 0 || mode == 2) {
			Collection<Vehicle> onMission = getMissionVehicles();
			Iterator<Vehicle> j = onMission.iterator();
			while (j.hasNext()) {
				Vehicle vehicle = j.next();
				// if (vehicle instanceof Rover) {
				String d = vehicle.getVehicleType();
				if (d.equals(VehicleType.TRANSPORT_ROVER.getName()))
					rovers.add(vehicle);
				// }
			}
		}
		return rovers;
	}

	public List<Vehicle> getExplorerRovers(int mode) {
		List<Vehicle> rovers = new ArrayList<Vehicle>();

		if (mode == 0 || mode == 1) {
			Collection<Vehicle> parked = getParkedVehicles();
			Iterator<Vehicle> i = parked.iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				// if (vehicle instanceof Rover) {
				String d = vehicle.getVehicleType();
				// System.out.println("type is " + d);
				if (d.equals(VehicleType.EXPLORER_ROVER.getName()))
					rovers.add(vehicle);
				// }
			}
		}

		if (mode == 0 || mode == 2) {
			Collection<Vehicle> onMission = getMissionVehicles();
			Iterator<Vehicle> j = onMission.iterator();
			while (j.hasNext()) {
				Vehicle vehicle = j.next();
				// if (vehicle instanceof Rover) {
				String d = vehicle.getVehicleType();
				if (d.equals(VehicleType.EXPLORER_ROVER.getName()))
					rovers.add(vehicle);
				// }
			}
		}
		return rovers;
	}

	/**
	 * Sets the mission creation override flag.
	 *
	 * @param missionCreationOverride override for settlement mission creation.
	 */
	public void setMissionCreationOverride(boolean missionCreationOverride) {
		this.missionCreationOverride = missionCreationOverride;
	}

	/**
	 * Gets the mission creation override flag.
	 *
	 * @return override for settlement mission creation.
	 */
	public boolean getMissionCreationOverride() {
		return missionCreationOverride;
	}

	/**
	 * Sets the construction override flag.
	 *
	 * @param constructionOverride override for settlement construction/salvage
	 *                             mission creation.
	 */
	public void setConstructionOverride(boolean constructionOverride) {
		this.constructionOverride = constructionOverride;
	}

	/**
	 * Gets the construction override flag.
	 *
	 * @return override for settlement construction mission creation.
	 */
	public boolean getConstructionOverride() {
		return constructionOverride;
	}

	/**
	 * Sets the FoodProduction override flag.
	 *
	 * @param foodProductionOverride override for FoodProduction.
	 */
	public void setFoodProductionOverride(boolean foodProductionOverride) {
		this.foodProductionOverride = foodProductionOverride;
	}

	/**
	 * Gets the FoodProduction override flag.
	 *
	 * @return override for settlement FoodProduction.
	 */
	public boolean getFoodProductionOverride() {
		return foodProductionOverride;
	}

	/**
	 * Sets the manufacture override flag.
	 *
	 * @param manufactureOverride override for manufacture.
	 */
	public void setManufactureOverride(boolean manufactureOverride) {
		this.manufactureOverride = manufactureOverride;
	}

	/**
	 * Gets the manufacture override flag.
	 *
	 * @return override for settlement manufacture.
	 */
	public boolean getManufactureOverride() {
		return manufactureOverride;
	}

	/**
	 * Sets the resource process override flag.
	 *
	 * @param resourceProcessOverride override for resource processes.
	 */
	public void setResourceProcessOverride(boolean resourceProcessOverride) {
		this.resourceProcessOverride = resourceProcessOverride;
	}

	/**
	 * Gets the resource process override flag.
	 *
	 * @return override for settlement resource processes.
	 */
	public boolean getResourceProcessOverride() {
		return resourceProcessOverride;
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
	 * 
	 * @param achievementCredit the achievement credit.
	 * @param science           the scientific field.
	 */
	public void addScientificAchievement(double achievementCredit, ScienceType science) {
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
	public void decrementShiftType(ShiftType shiftType) {

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
	public void incrementShiftType(ShiftType shiftType) {

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
	public boolean isWorkShiftSaturated(ShiftType st, boolean inclusiveChecking) {
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

		if (pop >= 12) {
			limX = quotient - 1;
			if (remainder == 0)
				limZ = limX - 1;
			else
				limZ = limX;
		} else {
			limX = 2;
			limZ = 2;
		}

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

	public Map<Integer, Map<Integer, List<Double>>> getResourceStat() {
		return resourceStat;
	}

	public int getSolCache() {
		return solCache;
	}

	public boolean[] getExposed() {
		return exposed;
	}

	/*
	 * Compute the probability of radiation exposure during EVA/outside walk
	 */
	public void checkRadiationProbability(double time) {
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
			LogConsolidated.log(Level.INFO, 1_000, sourceName,
					"[" + name + DETECTOR_GRID + UnitEventType.GCR_EVENT.toString() + " is imminent.");
			this.fireUnitUpdate(UnitEventType.GCR_EVENT);
		} else
			exposed[1] = false;

		// ~ 300 milli Sieverts for a 500-day mission
		// Solar energetic particles (SEPs) event
		if (RandomUtil.lessThanRandPercent(chance2)) {
			exposed[2] = true;
			LogConsolidated.log(Level.INFO, 1_000, sourceName,
					"[" + name + DETECTOR_GRID + UnitEventType.SEP_EVENT.toString() + " is imminent.");
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
	public void computeWaterRation() {
		double storedWater = getInventory().getAmountResourceStored(ResourceUtil.waterID, false);
		double requiredDrinkingWaterOrbit = water_consumption_rate * getNumCitizens() // getIndoorPeopleCount()
				* MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;

		// If stored water is less than 20% of required drinking water for Orbit, wash
		// water should be rationed.
		double ratio = storedWater / requiredDrinkingWaterOrbit;
		GoodsManager.WATER_VALUE_MODIFIER = GoodsManager.WATER_VALUE_MODIFIER / ratio;
		if (GoodsManager.WATER_VALUE_MODIFIER < 1)
			GoodsManager.WATER_VALUE_MODIFIER = 1;
		else if (GoodsManager.WATER_VALUE_MODIFIER > 1000)
			GoodsManager.WATER_VALUE_MODIFIER = 1000;

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
	 * @param       {{@link ObjectiveType}
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
	 * Sets the objective
	 * 
	 * @param       {{@link ObjectiveType}
	 * @param level
	 */
	
	/**
	 * Gets the objective level
	 * 
	 * @param objectiveType
	 * @return
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

	public String[] getObjectiveArray() {
		return objectiveArray;
	}

	public ObjectiveType[] getObjectives() {
		return objectives;
	}

	public String getSponsor() {
		return sponsor;
	}

	public int getSumOfManuProcesses() {
		return sumOfCurrentManuProcesses;
	}

	public void addManuProcesses(int value) {
		sumOfCurrentManuProcesses = sumOfCurrentManuProcesses + value;
	}

	/**
	 * Gets the number of crops that currently need work this Sol.
	 * 
	 * @return number of crops.
	 */
	public int getCropsNeedingTending() {
		int result = 0;

		int m = marsClock.getMillisolInt();
		if (millisolCache + 5 >= m) {
			result = cropsNeedingTendingCache;
		}

		else {
			millisolCache = m;
			for (Building b : buildingManager.getBuildings(FunctionType.FARMING)) {
//				Farming farm = b.getFarming();
				for (Crop c : b.getFarming().getCrops()) {
					if (c.requiresWork()) {
						result++;
					}
					// if the health condition is below 50%,
					// need special care
					if (c.getHealthCondition() < .5)
						result++;
				}
			}
			cropsNeedingTendingCache = result;
		}

		return result;
	}

	public int getCropsNeedingTendingCache() {
		return cropsNeedingTendingCache;
	}

	@Override
	public String toString() {
		return name;
	}

	/***
	 * Computes the probability of the presence of regolith
	 * 
	 * @return probability of finding regolith
	 */
	public double computeRegolithProbability() {
		double result = 0;

		double regolith_value = goodsManager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.regolithID));
		regolith_value = regolith_value * GoodsManager.REGOLITH_VALUE_MODIFIER;
		if (regolith_value > 5000)
			regolith_value = 5000;
		else if (regolith_value < 0)
			return 0;

		double sand_value = goodsManager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.sandID));
		sand_value = sand_value * GoodsManager.SAND_VALUE_MODIFIER;
		if (sand_value > 5000)
			sand_value = 5000;
		else if (sand_value < 0)
			return 0;

		int pop = numCitizens;// getAllAssociatedPeople().size();// getCurrentPopulationNum();

		double regolith_available = getInventory().getAmountResourceStored(ResourceUtil.regolithID, false);
		double sand_available = getInventory().getAmountResourceStored(ResourceUtil.sandID, false);

		if (regolith_available < MIN_REGOLITH_RESERVE * pop + regolith_value / 10
				&& sand_available < MIN_SAND_RESERVE * pop + sand_value / 10) {
			result = (MIN_REGOLITH_RESERVE * pop - regolith_available) / 10D;
		}

		// Prompt the regolith mission if regolith resource is low,
		if (regolith_available > MIN_REGOLITH_RESERVE * pop) {
			;// no change to missionProbability
		} else if (regolith_available > MIN_REGOLITH_RESERVE * pop / 1.5) {
			result = 20D * result + (MIN_REGOLITH_RESERVE * pop - regolith_available);
		} else if (regolith_available > MIN_REGOLITH_RESERVE * pop / 2D) {
			result = 10D * result + (MIN_REGOLITH_RESERVE * pop - regolith_available);
		} else
			result = 5D * result + (MIN_REGOLITH_RESERVE * pop - regolith_available);

		if (result < 0)
			result = 0;

//		System.out.println("computeRegolithProbability() " + result);
		return result;
	}

	/***
	 * Computes the probability of the presence of ice
	 * 
	 * @return probability of finding ice
	 */
	public double computeIceProbability() {
		double result = 0;

		double ice_value = goodsManager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.iceID));
		ice_value = ice_value * GoodsManager.ICE_VALUE_MODIFIER;
		if (ice_value > 4_000)
			ice_value = 4_000;
		if (ice_value < 1)
			ice_value = 1;

		double water_value = goodsManager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.waterID));
		water_value = water_value * GoodsManager.WATER_VALUE_MODIFIER;
		if (water_value > 16_000)
			water_value = 16_000;
		if (water_value < 1)
			water_value = 1;

//        double oxygen_value = goodsManager.getGoodValuePerItem(GoodsUtil.getResourceGood(ResourceUtil.oxygenAR));
//        oxygen_value = oxygen_value * GoodsManager.OXYGEN_VALUE_MODIFIER;
//        if (oxygen_value > 4000)
//        	oxygen_value = 4000;
//    	if (oxygen_value < 1)
//    		oxygen_value = 1;

		// Compare the available amount of water and ice reserve
		double ice_available = getInventory().getAmountResourceStored(ResourceUtil.iceID, false);
		double water_available = getInventory().getAmountResourceStored(ResourceUtil.waterID, false);
		// double oxygen_available =
		// getInventory().getAmountResourceStored(ResourceUtil.oxygenAR, false);

		int pop = numCitizens;// getCurrentPopulationNum();

		// TODO: create a task to find local ice and simulate the probability of finding
		// local ice and its quantity
		if (ice_available < MIN_ICE_RESERVE * pop + ice_value / 10D
				&& water_available < MIN_WATER_RESERVE * pop + water_value / 10D) {
			result = (MIN_ICE_RESERVE * pop - ice_available + MIN_WATER_RESERVE * pop - water_available) * 2D
					+ water_value + ice_value;
		}

		// Prompt the collect ice mission to proceed more easily if water resource is
		// dangerously low,
		if (water_available > MIN_WATER_RESERVE * pop) {
			;// no change to missionProbability
		} else if (water_available > MIN_WATER_RESERVE * pop / 1.5) {
			result = 20D * result + (MIN_WATER_RESERVE * pop - water_available);
		} else if (water_available > MIN_WATER_RESERVE * pop / 2D) {
			result = 10D * result + (MIN_WATER_RESERVE * pop - water_available);
		} else
			result = 5D * result + (MIN_WATER_RESERVE * pop - water_available);

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
//		double total = 0;
//		for (double s : missionScores) {
//			total += s;
//		}
//		double ave = total/ missionScores.size();
//		
//		return Math.round((currentAverageScore + trendingScore) * 10D) / 10D;

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
	 * @param missionType : must be a number from 0 to 10; 
	 * 			0 : Areo, 
	 *		    1 : Bio, 
	 *		    2 : CollectIce, 
	 *			3 : CollectRegolith, 
	 *			4 : Emergency, 
	 *
	 *			5 : Exploration, 
	 * 			6 : Meteorology, 
	 *			7 : Mining, 
     * 			8 : RescueSalvageVehicle, 
	 * 			9 : Trade, 
	 * 
	 * 		   10 : TravelToSettlement	
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

	public void setDesignatedCommander(boolean value) {
		hasDesignatedCommander = value;
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
	 * @param id the resource id of the good
	 * @param amount the amount or quantity produced
	 * @param millisols the labor time
	 */
	public void addOutput(Integer id, double amount, double millisols) {

		// Record the amount of resource produced
		Map<Integer, Double> amountMap = null;

		if (dailyResourceOutput.containsKey(solCache)) {
			amountMap = dailyResourceOutput.get(solCache);
			if (amountMap.containsKey(id)) {
				double oldAmt = amountMap.get(id);
				amountMap.put(id, amount + oldAmt);
			} else {
				amountMap.put(id, amount);
			}
		} else {
			amountMap = new ConcurrentHashMap<>();
			amountMap.put(id, amount);
		}

		dailyResourceOutput.put(solCache, amountMap);

		// Record the labor hours
		Map<Integer, Double> laborHrMap = null;

		if (dailyLaborTime.containsKey(solCache)) {
			laborHrMap = dailyLaborTime.get(solCache);
			if (laborHrMap.containsKey(id)) {
				double oldAmt = laborHrMap.get(id);
				laborHrMap.put(id, millisols + oldAmt);
			} else {
				laborHrMap.put(id, millisols);
			}
		} else {
			laborHrMap = new ConcurrentHashMap<>();
			laborHrMap.put(id, millisols);
		}

		dailyLaborTime.put(solCache, amountMap);

	}

	/**
	 * Gets the daily resource output in kg or quantity 
	 * 
	 * @param id  the resource id of the good
	 * @return
	 */
	public double getDailyesourceOutput(Integer id) {
		if (solCache - 1 > 0)
			return dailyResourceOutput.get(solCache-1).get(id);
		return 0;
	}
	
	/**
	 * Gets the daily labor time [millisols]
	 * 
	 * @param id  the resource id of the good
	 * @return
	 */
	public double getDailyLaborTime(Integer id) {
		if (solCache - 1 > 0)
			return dailyLaborTime.get(solCache-1).get(id);
		return 0;
	}
	
	/**
	 * Records the amount of water being consumed.
	 * 
	 * @param type
	 * @param amount
	 */
	public void addWaterConsumption(int type, double amount) {
		// type = 0 : preparing meal
		// type = 1 : preparing dessert
		// type = 2 : cleaning kitchen for meal
		// type = 3 : cleaning kitchen for dessert
		Map<Integer, Double> map = null;

		if (waterConsumption.containsKey(solCache)) {
			map = waterConsumption.get(solCache);
			if (map.containsKey(type)) {
				double oldAmt = map.get(type);
				map.put(type, amount + oldAmt);
			} else {
				map.put(type, amount);
			}
		} else {
			map = new ConcurrentHashMap<>();
			map.put(type, amount);
		}

		waterConsumption.put(solCache, map);
	}

	/**
	 * Gets the total amount consumed
	 * 
	 * @param type
	 * @return
	 */
	public Map<Integer, Double> getTotalConsumptionBySol(int type) {
		Map<Integer, Double> map = new ConcurrentHashMap<>();

		for (Integer sol : waterConsumption.keySet()) {
			for (Integer t : waterConsumption.get(sol).keySet()) {
				if (t == type) {
					map.put(sol, waterConsumption.get(sol).get(t));
				}
			}
		}

		return map;
	}

	/**
	 * Gets the daily average water usage of the last x sols Not: most weight on
	 * yesterday's usage. Least weight on usage from x sols ago
	 * 
	 * @return
	 */
	public double getDailyUsage(int type) {
		Map<Integer, Double> map = getTotalConsumptionBySol(type);

		boolean quit = false;
		int today = solCache;
		int sol = solCache;
		double sum = 0;
		double numSols = 0;
		double cumulativeWeight = 0.75;
		double weight = 1;

		while (!quit) {
			if (map.size() == 0) {
				quit = true;
				return 0;
			}

			else if (map.containsKey(sol)) {
				if (today == sol) {
					// If it's getting the today's average, one may
					// project the full-day usage based on the usage up to this moment
					weight = .25;
					sum = sum + map.get(sol) * 1_000D / marsClock.getMillisol() * weight;
				}

				else {
					sum = sum + map.get(sol) * weight;
				}
			}

			else if (map.containsKey(sol - 1)) {
				sum = sum + map.get(sol - 1) * weight;
				sol--;
			}

			cumulativeWeight = cumulativeWeight + weight;
			weight = (numSols + 1) / (cumulativeWeight + 1);
			numSols++;
			sol--;
			// Get the last x sols only
			if (numSols > MAX_NUM_SOLS)
				quit = true;
		}

		return sum / cumulativeWeight;
	}

	public static void assignBestCandidate(Settlement settlement, Class<? extends Job> jobClass) {// String jobName) {
		Job job = JobUtil.getJob(jobClass.getSimpleName());
//		Job job = (Job)(jobClass);
		Person p0 = JobUtil.findBestFit(settlement, job);
		// Designate a specific job to a person
		if (p0 != null) {
			p0.getMind().setJob(job, true, JobUtil.SETTLEMENT, JobAssignmentType.APPROVED, JobUtil.SETTLEMENT);
		}
	}

	/**
	 * Tune up the settlement with unique job position
	 */
	public void tuneJobDeficit() {
		int numEngs = JobUtil.numJobs(Engineer.class.getSimpleName(), this);
		if (numEngs == 0) {
			assignBestCandidate(this, Engineer.class);
		}

		int numTechs = JobUtil.numJobs(Technician.class.getSimpleName(), this);
		if (numTechs == 0) {
			assignBestCandidate(this, Technician.class);
		}

		if (this.getNumCitizens() > ChainOfCommand.POPULATION_WITH_CHIEFS) {
			int numWeatherman = JobUtil.numJobs(Meteorologist.class.getSimpleName(), this);
			if (numWeatherman == 0) {
				assignBestCandidate(this, Meteorologist.class);
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
	
	public void allowTradeMissionFromAllSettlements(boolean allowed) {
		for (Settlement s: unitManager.getSettlements()) {
			allowTradeMissionSettlements.put(s.getIdentifier(), allowed);
		}
	}
	
	public boolean isTradeMissionAllowedFromASettlement(Settlement settlement) {
		if (allowTradeMissionSettlements.containsKey(settlement.getIdentifier())) {
			return allowTradeMissionSettlements.get(settlement.getIdentifier());
		}
		// by default it's allowed
		return true;
	}
	
	/**
	 * Calculate the base mission probability used by all missions
	 * 
	 * @param missionName the name of the mission calling this method
	 * @return probability value
	 */
	public double getMissionBaseProbability(String missionName) {

		if (missionProbability == -1) {
			
			List<String> names = MissionManager.getMissionNames();
			int size = names.size();
			// 0. Check if a mission has been overridden
			for (int i=0; i<size; i++) {
				if (missionName.equalsIgnoreCase(names.get(i)) 
					&& missionsDisable[i] == true) {
					missionProbability = 0;
					return 0;
				}
			}
		
			// 1. Check if a mission-capable rover is available.
			if (!RoverMission.areVehiclesAvailable(this, false)) {
				missionProbability = 0;
				return 0;
			}
//			System.out.println("1.  missionProbability is " + missionProbability);
			// 2. Check if available backup rover.
			if (!RoverMission.hasBackupRover(this)) {
				missionProbability = 0;
				return 0;
			}
//			System.out.println("2.  missionProbability is " + missionProbability);	
			// 3. Check if at least 1 person is there
			// A settlement with <= 4 population can always do DigLocalRegolith task
			// should avoid the risk of mission.
			if (getIndoorPeopleCount() <= 1) {// .getAllAssociatedPeople().size() <= 4)
				missionProbability = 0;
				return 0;
			}
//			System.out.println("3.  missionProbability is " + missionProbability);			
			// 4. Check if minimum number of people are available at the settlement.
			if (!RoverMission.minAvailablePeopleAtSettlement(this, RoverMission.MIN_STAYING_MEMBERS)) {
				missionProbability = 0;
				return 0;
			}
//			System.out.println("4.  missionProbability is " + missionProbability);
			// // Check for embarking missions.
			// else if (VehicleMission.hasEmbarkingMissions(this)) {
			// return 0;
			// }

			// 5. Check if min number of EVA suits at settlement.
			if (Mission.getNumberAvailableEVASuitsAtSettlement(this) < RoverMission.MIN_GOING_MEMBERS) {
				missionProbability = 0;
				return 0;
			}
//			System.out.println("5.  missionProbability is " + missionProbability);	
			// // Check for embarking missions.
			// else if (getNumCitizens() / 4.0 < VehicleMission.numEmbarkingMissions(this))
			// {
			// return 0;
			// }

			// 6. Check if settlement has enough basic resources for a rover mission.
			if (!hasEnoughBasicResources(true)) {
				missionProbability = 0;
				return 0;
			}
//			System.out.println("6.  missionProbability is " + missionProbability);	
			// 7. Check if starting settlement has minimum amount of methane fuel.
			if (getInventory().getAmountResourceStored(ResourceUtil.methaneID,
					false) < RoverMission.MIN_STARTING_SETTLEMENT_METHANE) {
				missionProbability = 0;
				return 0;
			}

			if (VehicleMission.numEmbarkingMissions(this) > getNumCitizens() / 4D) {
				missionProbability = 0;
				return 0;
			}

			if (VehicleMission.numApprovingMissions(this) > getNumCitizens() / 4D) {
				missionProbability = 0;
				return 0;
			}

//			System.out.println("7.  missionProbability is " + missionProbability);			
			missionProbability = 1;

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
	public boolean hasEnoughBasicResources(boolean unmasked) {
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
	
	public void setName(String value) {
		super.setName(value);
		this.name = value;
		fireUnitUpdate(UnitEventType.NAME_EVENT, name);
	}

	public double getAverageSettlerWeight() {
		double value = 0;
		for (Person p: citizens) {
			value += p.getMass();
		}
		return value;
	}
	
    public double[] getTerrainProfile() {
        return terrainProfile;
    }
    
    public double getIceCollectionRate() {
//    	if (iceCollectionRate == -1) {
//			if (terrainElevation == null)
//				terrainElevation = surfaceFeatures.getTerrainElevation();
//			iceCollectionRate = terrainElevation.getIceCollectionRate(location);
//    	}
    	return iceCollectionRate;
    }
	
	/**
	 * Gets a list of all travel related mission names
	 */
	public static List<String> getTravelMissionNames() {
		return travelMissionNames;
	}
	
	/** 
	 * Sets the settlement mission directive modifiers. 
	 * 
	 * @param index
	 * @param value
	 */
	public void setMissionDirectiveModifiers(int index, double value) {
		missionModifiers[index] = value;
	}
	
	public double getMissionDirectiveModifier(int index) {
		return missionModifiers[index];
	}
	
	/**
	 * Reset uniqueCount to the current number of settlements
	 */
	public static void reinitializeIdentifierCount() {
		uniqueCount = unitManager.getSettlementNum() + Unit.FIRST_SETTLEMENT_UNIT_ID;
	}

	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Settlement s = (Settlement) obj;
		return this.identifier == s.getIdentifier()
				&& this.name.equals(s.getName())
				&& this.template.equals(s.getTemplate());
	}
	
	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = name.hashCode();
		hashCode *= identifier;
		hashCode *= template.hashCode();
		return hashCode;
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
}