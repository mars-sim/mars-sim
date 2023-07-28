/*
 * Mars Simulation Project
 * Settlement.java
 * @date 2023-06-30
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.air.AirComposition;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.environment.DustStorm;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentInventory;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.ItemHolder;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.events.ScheduledEventManager;
import org.mars_sim.msp.core.goods.CreditManager;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Commander;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.AssignmentType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.util.Appointment;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.reportingAuthority.PreferenceKey;
import org.mars_sim.msp.core.reportingAuthority.PreferenceKey.Type;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Airlock.AirlockMode;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The Settlement class represents a settlement unit on virtual Mars. It
 * contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements Temporal,
	LifeSupportInterface, Objective, EquipmentOwner, ItemHolder  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Settlement.class.getName());

	// Static members

	private static final String DETECTOR_GRID = "The detector grid forecast a ";
	private static final String TRADING_OUTPOST = "Trading Outpost";
	private static final String MINING_OUTPOST = "Mining Outpost";
	private static final String ASTRONOMY_OBSERVATORY = "Astronomy Observatory";

	/**
	 * Shared preference key for Mission limits
	 */
	public static final PreferenceKey MISSION_LIMIT = new PreferenceKey(Type.CONFIGURATION,
																	"Active Missions");
	private static final int WAIT_FOR_SUNLIGHT_DELAY = 40;
	
	private static final int START_TIME = 400;
	private static final int DURATION = 150;
	
	private static final int UPDATE_GOODS_PERIOD = (1000/20); // Update 20 times per day
	public static final int CHECK_MISSION = 20; // once every 10 millisols
	public static final int MAX_NUM_SOLS = 3;
	public static final int MAX_SOLS_DAILY_OUTPUT = 14;
	public static final int SUPPLY_DEMAND_REFRESH = 7;
	private static final int RESOURCE_UPDATE_FREQ = 30;
	private static final int CHECK_WATER_RATION = 66;
	private static final int RESOURCE_SAMPLING_FREQ = 50; // in msols
	public static final int NUM_CRITICAL_RESOURCES = 10;
	private static final int RESOURCE_STAT_SOLS = 12;
	private static final int SOL_SLEEP_PATTERN_REFRESH = 3;
	
    private static final int PERSON_PER_MISSION = 5;

	private static final int MAX_PROB = 3000;
	public static final int REGOLITH_MAX = 4000;
	public static final int MIN_REGOLITH_RESERVE = 400; // per person
	public static final int MIN_SAND_RESERVE = 400; // per person
	
	public static final int ICE_MAX = 4000;
	public static final int WATER_MAX = 4000;
	public static final int OXYGEN_MAX = 4000;
	
	public static final int MIN_OXYGEN_RESERVE = 200; // per person
	public static final int MIN_WATER_RESERVE = 400; // per person
	public static final int MIN_ICE_RESERVE = 400; // per person

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int HYDROGEN_ID = ResourceUtil.hydrogenID;
	private static final int METHANE_ID = ResourceUtil.methaneID;
	private static final int METHANOL_ID = ResourceUtil.methanolID;
	private static final int CO2_ID = ResourceUtil.co2ID;
	
	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int ICE_ID = ResourceUtil.iceID;
	private static final int BRINE_WATER_ID = ResourceUtil.brineWaterID;

	private static final int REGOLITH_ID = ResourceUtil.regolithID;
	private static final int SAND_ID = ResourceUtil.sandID;
	private static final int CONCRETE_ID = ResourceUtil.concreteID;
	private static final int CEMENT_ID = ResourceUtil.cementID;
	

	private static final int GREY_WATER_ID = ResourceUtil.greyWaterID;
	private static final int BLACK_WATER_ID = ResourceUtil.blackWaterID;
	private static final int ROCK_SAMPLES_ID = ResourceUtil.rockSamplesID;

	/** The settlement sampling resources. */
	public static final int[] samplingResources;
	
	/** The definition of static arrays */
	static {
		samplingResources = new int[] {
				OXYGEN_ID,
				HYDROGEN_ID,
				CO2_ID,
				METHANE_ID,
				METHANOL_ID,
				BRINE_WATER_ID,
				WATER_ID,
				
				ICE_ID,
				BRINE_WATER_ID,
				GREY_WATER_ID,
				BLACK_WATER_ID,
				ROCK_SAMPLES_ID,

				REGOLITH_ID };
	}
	
	/** Threshold to adjust filtering rate. */
	private static final double GREY_WATER_THRESHOLD = 0.00001;
	/** Safe low temperature range. */
	public static final double SAFE_TEMPERATURE_RANGE = 18;
	/** Initial mission passing score. */
	private static final double INITIAL_MISSION_PASSING_SCORE = 500D;
	/** The Maximum mission score that can be recorded. */
	private static final double MAX_MISSION_SCORE = 1000D;

	/** Normal air pressure [in kPa]. */
	private static final double NORMAL_AIR_PRESSURE = 34D;

	/** The settlement water consumption */
	public static double water_consumption_rate;
	/** The settlement minimum air pressure requirement. */
	public static double minimum_air_pressure;
	/** The settlement life support requirements. */
	public static double[][] life_support_value = new double[2][7];
	
	/** The flag for checking if the simulation has just started. */
	private boolean justLoaded = true;
	/** The flag signifying this settlement as the destination of the user-defined commander. */
	private boolean hasDesignatedCommander = false;
	/** The Flag showing if the settlement has been exposed to the last radiation event. */
	private RadiationStatus exposed = RadiationStatus.calculateCurrent(0D);
	
	/** The water ration level of the settlement. The higher the more urgent. */
	private int waterRationLevel = 1;
	/** The number of people at the start of the settlement. */
	private int initialPopulation;
	/** The number of robots at the start of the settlement. */
	private int initialNumOfRobots;
	/** The template ID of the settlement. */
	private int templateID;
	/** The cache for the mission sol. */
	private int solCache = 0;
	/** Numbers of citizens of this settlement. */
	private int numCitizens;
	/** Numbers of bots owned by this settlement. */
	private int numOwnedBots;
	/** Numbers of vehicles owned by this settlement. */
	private int numOwnedVehicles;


	/** The average regolith collection rate nearby. */
	private double regolithCollectionRate = RandomUtil.getRandomDouble(4, 8);
	/** The average ice collection rate of the water ice nearby. */
	private double iceCollectionRate = RandomUtil.getRandomDouble(0.2, 1);
	/** The composite value of the minerals nearby. */
	public int mineralValue = -1;
	/** The rate [kg per millisol] of filtering grey water for irrigating the crop. */
	public double greyWaterFilteringRate = 1;
	/** The currently minimum passing score for mission approval. */
	private double minimumPassingScore = INITIAL_MISSION_PASSING_SCORE;
	/** The settlement's current indoor temperature. */
	private double currentTemperature = 22.5;
	/** The settlement's current indoor pressure [in kPa], not Pascal. */
	private double currentPressure = NORMAL_AIR_PRESSURE;
	/** The settlement's current meal replenishment rate. */
	public double mealsReplenishmentRate = 0.3;
	/** The settlement's current dessert replenishment rate. */
	public double dessertsReplenishmentRate = 0.4;
	/** The settlement's current probability value for ice. */
	private double iceProbabilityValue = 400D;
	/** The settlement's current probability value for regolith. */
	private double regolithProbabilityValue = 400D;
	/** The settlement's outside temperature. */
	private double outside_temperature;
	/** Total Crop area */
	private double cropArea = -1;

	/** The settlement terrain profile. */
	public double[] terrainProfile = new double[2];

	/** The settlement template name. */
	private String template;

	/** The settlement's ReportingAuthority instance. */
	private ReportingAuthority sponsor;
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
	/** The settlement's location. */
	private Coordinates location;
	/** The settlement's last dust storm. */
	private DustStorm storm;
	/** The settlement's EquipmentInventory instance. */
	private EquipmentInventory eqmInventory;
	/** The settlement's CreditManager instance manages trade credit between settlements. */
	private CreditManager creditManager;
	/** Mamanges the shifts */
	private ShiftManager shiftManager;
	private SettlementTaskManager taskManager;
	private ScheduledEventManager futureEvents;
	
	/** The settlement objective type instance. */
	private ObjectiveType objectiveType;

	/** The settlement's water consumption in kitchen when preparing/cleaning meal and dessert. */
	private SolMetricDataLogger<WaterUseType> waterConsumption;
	/** The settlement's daily output (resources produced). */
	private SolMetricDataLogger<Integer> dailyResourceOutput;
	/** The settlement's daily labor hours output. */
	private SolMetricDataLogger<Integer> dailyLaborTime;

	/** The object that keeps track of wheelbarrows. */
//	private StorableItem wheelbarrows;
	
	/** The settlement's achievement in scientific fields. */
	private EnumMap<ScienceType, Double> scientificAchievement;
	/** The map of settlements allowed to trade. */
	private Map<Integer, Boolean> allowTradeMissionSettlements;
	/** The total amount resource collected/studied. */
	private Map<Integer, Double> resourcesCollected = new HashMap<>();
	/** The settlement's resource statistics. */
	private Map<Integer, Map<Integer, Map<Integer, Double>>> resourceStat = new HashMap<>();
	
	/** The last 20 mission scores */
	private List<Double> missionScores;

	/** The set of processes being overridden. */
	private Set<OverrideType> processOverrides = new HashSet<>();
	/** The set of available pressurized/pressurizing airlocks. */
	private Set<Integer> availablePAirlocks = new HashSet<>();
	/** The set of available depressurized/depressurizing airlocks. */
	private Set<Integer> availableDAirlocks = new HashSet<>();
	/** The settlement's list of citizens. */
	private Set<Person> citizens;
	/** The settlement's list of owned robots. */
	private Set<Robot> ownedRobots;
	/** The settlement's list of owned vehicles. */
	private Set<Vehicle> ownedVehicles;
	/** The settlement's list of parked vehicles. */
	private Set<Vehicle> parkedVehicles;
	/** The list of people currently within the settlement. */
	private Set<Person> peopleWithin;
	/** The settlement's list of robots within. */
	private Set<Robot> robotsWithin;
	/** The settlement's preference modifiers map. */
	private Map<PreferenceKey, Double> preferenceModifiers = new HashMap<>();
	/** A set of nearby mineral locations. */
	private Set<Coordinates> nearbyMineralLocations = new HashSet<>();
	
	private static SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
	private static PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
	private static SurfaceFeatures surfaceFeatures;

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

		// set location
		location = getCoordinates();
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

		this.templateID = id;
		this.location = location;

		citizens = new UnitSet<>();
		ownedRobots = new UnitSet<>();
		ownedVehicles = new UnitSet<>();
		parkedVehicles = new UnitSet<>();
		peopleWithin = new UnitSet<>();
		robotsWithin = new UnitSet<>();

		final double GEN_MAX = 1_000_000;
		
		// Create EquipmentInventory instance
		eqmInventory = new EquipmentInventory(this, GEN_MAX);

		// Initialize schedule event manager
		futureEvents = new ScheduledEventManager(masterClock);

		creditManager = new CreditManager(this, unitManager);

		// Mock use the default shifts
		ShiftPattern shifts = settlementConfig.getShiftPattern(SettlementConfig.DEFAULT_3SHIFT);
		shiftManager = new ShiftManager(this, shifts, 0,
										masterClock.getMarsTime().getMillisolInt());
	}

	/**
	 * Constructor 3 for creating settlements. Called by UnitManager to create the
	 * initial settlement Called by ArrivingSettlement to create a brand new
	 * settlement.
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
			int initialNumOfRobots) {
		// Use Structure constructor
		super(name, location);

		this.template = template;
		this.location = location;
		this.templateID = id;
		this.initialNumOfRobots = initialNumOfRobots;
		this.initialPopulation = populationNumber;

		// Determine the reporting authority
		this.sponsor = sponsor;
		preferenceModifiers.putAll(sponsor.getPreferences());

		citizens = new UnitSet<>();
		ownedRobots = new UnitSet<>();
		ownedVehicles = new UnitSet<>();
		parkedVehicles = new UnitSet<>();
		peopleWithin = new UnitSet<>();
		robotsWithin = new UnitSet<>();
		allowTradeMissionSettlements = new HashMap<>();
	}


	/**
	 * The static factory method called by UnitManager and ArrivingSettlement to
	 * create a brand new settlement.
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
	 * Initializes field data, class and maps.
	 */
	public void initialize() {
		if (surfaceFeatures == null) 
			surfaceFeatures = Simulation.instance().getSurfaceFeatures();
		
		TerrainElevation terrainElevation = surfaceFeatures.getTerrainElevation();
		
//		// Get the elevation and terrain gradient factor
		terrainProfile = terrainElevation.getTerrainProfile(location);
		
		logger.config(this, "elevation: " + Math.round(terrainProfile[0] * 100.0)/100.0
				+ " km. gradient: " + Math.round(terrainProfile[1] * 100.0)/100.0);
		
//		Note: to check elevation, do this -> double elevation = terrainProfile[0];
//		Note: to check gradient, do this ->double gradient = terrainProfile[1];

		iceCollectionRate = iceCollectionRate + terrainElevation.obtainIceCollectionRate(location);
		regolithCollectionRate = regolithCollectionRate + terrainElevation.obtainRegolithCollectionRate(location);

		logger.config(this, "Ice Collection Rate: " + Math.round(iceCollectionRate * 100.0)/100.0);
		logger.config(this, "Regolith Collection Rate: " + Math.round(regolithCollectionRate * 100.0)/100.0);
		
		// Create local mineral locations
		surfaceFeatures.getMineralMap().createLocalConcentration(location);
		
		double areoThermalPot = surfaceFeatures.getAreothermalPotential(location);
		
		logger.config(this, "Areothermal Potential: " + Math.round(areoThermalPot * 1000.0)/1000.0);
		
		final double GEN_MAX = 1_000_000;
		// Create EquipmentInventory instance
		eqmInventory = new EquipmentInventory(this, GEN_MAX);

		final double INITIAL_FREE_OXYGEN = 5_000;
		// Stores limited amount of oxygen in this settlement
		storeAmountResource(ResourceUtil.oxygenID, INITIAL_FREE_OXYGEN);

		SettlementTemplate sTemplate = settlementConfig.getItem(template);

		// Initialize building manager
		buildingManager = new BuildingManager(this, sTemplate.getBuildings());
		
		buildingManager.initialize();
		
		// Initialize building connector manager.
		buildingConnectorManager = new BuildingConnectorManager(this, sTemplate.getBuildings());

		// Create adjacent building map
		buildingManager.createAdjacentBuildingMap();
		
		// Initialize schedule event manager
		futureEvents = new ScheduledEventManager(masterClock);

		// Get the rotation about the planet and convert that to a fraction of the Sol.
		double fraction = getCoordinates().getTheta()/(Math.PI * 2D); 
		if (fraction == 1D) {
			// Gone round the planet
			fraction = 0D;
		}
		int sunRiseOffSet = (int) (100 * fraction) * 10; // Do the offset in units of 10

		shiftManager = new ShiftManager(this, sTemplate.getShiftDefinition(),
										sunRiseOffSet, masterClock.getMarsTime().getMillisolInt());

		// Initialize Credit Manager.
		creditManager = new CreditManager(this);
		
		// Initialize goods manager.
		goodsManager = new GoodsManager(this, sunRiseOffSet);

		// Initialize construction manager.
		constructionManager = new ConstructionManager(this);

		// Initialize power grid
		powerGrid = new PowerGrid(this);

		// Initialize thermal control system
		thermalSystem = new ThermalSystem(this);

		// Initialize settlement task manager
		taskManager = new SettlementTaskManager(this);

		// Initialize scientific achievement.
		scientificAchievement = new EnumMap<>(ScienceType.class);

		// Add chain of command
		chainOfCommand = new ChainOfCommand(this);

		// Set objective()
		if (template.equals(TRADING_OUTPOST))
			setObjective(ObjectiveType.TRADE_CENTER, 2);
		else if (template.equals(MINING_OUTPOST))
			setObjective(ObjectiveType.MANUFACTURING_DEPOT, 2);
		else
			setObjective(ObjectiveType.CROP_FARM, 2);

		// initialize the missionScores list
		missionScores = new ArrayList<>();
		missionScores.add(INITIAL_MISSION_PASSING_SCORE);

		// Create the water consumption map
		waterConsumption = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		// Create the daily output map
		dailyResourceOutput = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		// Create the daily labor hours map
		dailyLaborTime = new SolMetricDataLogger<>(MAX_NUM_SOLS);

	}

	/**
	 * Gets the terrain elevation.
	 * 
	 * @return
	 */
	public double getElevation() {
		return terrainProfile[0];
	}

	/**
	 * Gets the terrain gradient.
	 * 
	 * @return
	 */
	public double getGradient() {
		return terrainProfile[1];
	}
	
	/**
	 * Gets the space agency.
	 */
	public ReportingAuthority getReportingAuthority() {
		return sponsor;
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
	 * Gets the population capacity of the settlement.
	 *
	 * @return the population capacity
	 */
	public int getPopulationCapacity() {
		return buildingManager.getPopulationCapacity();
	}

	/**
	 * Gets the current number of people who are inside the settlement.
	 *
	 * @return the number indoor
	 */
	public int getIndoorPeopleCount() {
		return peopleWithin.size();
	}

	/**
	 * Ends all the indoor tasks people are doing.
	 */
	public void endAllIndoorTasks() {
		for (Person p : getIndoorPeople()) {
			logger.log(this, p, Level.INFO, 4_000,
						"Ended the current indoor task at "
						+  p.getPosition() + ".", null);
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
	 * Gets the number of citizens doing EVA outside.
	 * 
	 * @return
	 */
	public Long getNumOutsideEVA() {
		return citizens.stream()
				.filter(p -> !p.isDeclaredDead()
						&& (p.getLocationStateType() == LocationStateType.WITHIN_SETTLEMENT_VICINITY
						|| p.getLocationStateType() == LocationStateType.MARS_SURFACE))
				.collect(Collectors.counting());
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
	}

	/**
	 * Gets a collection of the number of robots of the settlement.
	 *
	 * @return Collection of robots
	 */
	public Collection<Robot> getRobots() {
		return ownedRobots;
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
	@Override
	public double provideOxygen(double oxygenTaken) {
		// Note: do NOT retrieve O2 here since calculateGasExchange() in
		// CompositionOfAir is doing it for all inhabitants once per frame.
		return oxygenTaken;
	}

	/**
	 * Gets water from the inventory
	 *
	 * @param waterTaken the amount of water requested [kg]
	 * @return the amount of water actually received [kg]
	 * @throws Exception if error providing water.
	 */
	@Override
	public double provideWater(double waterTaken) {
		double lacking = retrieveAmountResource(WATER_ID, waterTaken);
		return waterTaken - lacking;
	}

	/**
	 * Computes the average air pressure & temperature of the life support system.
	 */
	private void computeEnvironmentalAverages() {

		double totalArea = 0;
		double totalTArea = 0;
		double totalPressureArea = 0;
		List<Building> buildings = buildingManager.getBuildingsWithLifeSupport();
		for (Building b : buildings) {
			double area = b.getFloorArea();
			totalArea += area;
			totalPressureArea += b.getLifeSupport().getAir().getTotalPressure() * area;
			totalTArea += b.getCurrentTemperature() * area;
		}
		if (totalArea == 0) {
			totalArea = 0.1;
		}

		// convert from atm to kPascal
		currentPressure =  totalPressureArea * AirComposition.KPA_PER_ATM / totalArea;
		currentTemperature = totalTArea / totalArea;

		outside_temperature = weather.getTemperature(location);
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
				p = b.getLifeSupport().getAir().getTotalPressure();
			}
		}
		// convert from atm to kPascal
		return p * AirComposition.KPA_PER_ATM;
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

		int sol = pulse.getMarsTime().getMissionSol();

		// Calls other time passings
		futureEvents.timePassing(pulse);
		powerGrid.timePassing(pulse);
		thermalSystem.timePassing(pulse);
		buildingManager.timePassing(pulse);
		taskManager.timePassing();

		// Update citizens
		timePassingCitizens(pulse);

		// Update remaining Units
		timePassing(pulse, ownedVehicles);
		timePassing(pulse, ownedRobots);

		// Run at the start of the sim once only
		if (justLoaded) {	
			// Reset justLoaded
			justLoaded = false;

			iceProbabilityValue = computeIceProbability();

			regolithProbabilityValue = computeRegolithProbability();
			
			for (Person p : citizens) {
				// Register each settler a quarter/bed
				Building b = getBestAvailableQuarters(p, true);
				if (b != null)
					b.getLivingAccommodations().registerSleeper(p, false);
			}

			// Initialize the goods manager
			goodsManager.updateGoodValues();
			
			// Initialize a set of nearby mineral locations at the start of the sim only
			Rover rover = getVehicleWithMinimalRange();
			
			// Creates a set of nearby mineral locations			
			createNearbyMineralLocations(rover);
			// Note : may call it again if a new rover is made with longer range
			
			// Look for the first site to be analyzed and explored
			Coordinates firstSite = getAComfortableNearbyMineralLocation(
					rover.getRange() / 100, 10 * pulse.getMarsTime().getMissionSol());
			
			// Creates an initial explored site in SurfaceFeatures
			createARegionOfInterest(firstSite, 0);
			
			logger.info(this, "On Sol 1, " + firstSite.getFormattedString() 
						+ " was the very first exploration site chosen to be analyzed and explored.");
			
			checkMineralMapImprovement();
			
			setAppointedTask(sol);
		}
		
//		int rand = RandomUtil.getRandomInt(100);
	
		// At the beginnning of a new sol,
		// there's a chance a new site is automatically discovered
		if (pulse.isNewSol()) {
			
			// Perform the end of day tasks
			performEndOfDayTasks(pulse.getMarsTime());	

			setAppointedTask(sol);

			int range = (int) getVehicleWithMinimalRange().getRange();
			
			int skill = 0;
			int num = 0;
			for (Person p: citizens) {
				skill += p.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
				num++;
			}
			
			int skillDistance = Math.min(range, 2 * sol * skill / num);
			
			int limit =  Math.min(100, sol) * range / 100;
			
			// Add another explored site 
			Coordinates anotherSite = getAComfortableNearbyMineralLocation(limit, skillDistance);
			
			// Creates an initial explored site in SurfaceFeatures
			createARegionOfInterest(anotherSite, skill);
			
			logger.info(this, "On Sol " + sol + ", " +  anotherSite.getFormattedString() 
						+ " was added to be analyzed and explored.");
			
			checkMineralMapImprovement();
		}

		// Keeps track of things based on msol
		trackByMSol(pulse);

		// Computes the average air pressure & temperature of the life support system.
		computeEnvironmentalAverages();

		return true;
	}

	/**
	 * Sets up the daily appointed task of doing EVA.
	 * 
	 * @param sol
	 */
	public void setAppointedTask(int sol) {
		for (Person p: citizens) {
			if (p.getRole().getType() == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
					|| p.getRole().getType() == RoleType.LOGISTIC_SPECIALIST
					|| p.getRole().getType() == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES
					|| p.getRole().getType() == RoleType.RESOURCE_SPECIALIST
					) {
				
				int startTimeEVA = WAIT_FOR_SUNLIGHT_DELAY + (int)(surfaceFeatures.getOrbitInfo().getSunriseSunsetTime(location))[0];
				int numDigits = ("" + startTimeEVA).length();
				String startTimeEVAString = ""; 
				if (numDigits == 1) {
					startTimeEVAString = "00" + startTimeEVA;
				}
				else if (numDigits == 2) {
					startTimeEVAString = "0" + startTimeEVA;
				}
				else if (numDigits == 3) {
					startTimeEVAString = "" + startTimeEVA;
				}		
				
				Appointment ap = new Appointment(p, sol, startTimeEVA, DURATION, null, DigLocalRegolith.SIMPLE_NAME, null);
				p.getScheduleManager().setAppointment(ap);
				logger.info(this, p, "Authorized the daily EVA task '" + DigLocalRegolith.SIMPLE_NAME 
						+ "' at sol " + sol + ":" + startTimeEVAString + " for " + DURATION + " millisols.");
			}
		}
	}
	
	/**
	 * Checks and prints the average mineral map improvement made.
	 */
	public void checkMineralMapImprovement() {
		
		int improved = 0;
		
		List<ExploredLocation> siteList = surfaceFeatures
    			.getAllRegionOfInterestLocations().stream()
    			.filter(site -> site.isMinable())
    			.collect(Collectors.toList());  	
    	
    	for (ExploredLocation el: siteList) {   	
    		 int est = el.getNumEstimationImprovement();
    		 improved += est;
    	}
    	    	
    	int size = siteList.size();
    	
    	if (size > 0) {
	    	double result = 1.0 * improved / size;
	    
			logger.info(this, "Overall average # of improvement made on all mineral locations: " + Math.round(result * 10.0)/10.0);
    	}
	}
	
	/**
	 * Gets the stress factor due to occupancy density
	 *
	 * @param time
	 * @return
	 */
	public double getStressFactor(double time) {
		int overCrowding = getIndoorPeopleCount() - getPopulationCapacity();
		if (overCrowding > 0) {
			return .1D * overCrowding * time;
		}
		return 0;
	}


	/**
	 * Keeps track of things based on msol.
	 *
	 * @param pulse
	 */
	private void trackByMSol(ClockPulse pulse) {
		// Sample a data point every SAMPLE_FREQ (in msols)
		int msol = pulse.getMarsTime().getMillisolInt();

		// Avoid checking at < 10 or 1000 millisols
		// due to high cpu util during the change of day
		if (pulse.isNewMSol() && msol >= 10 && msol < 995) {

			// Check on demand and supply and amount of oxygen
			checkOxygenDemand();
			
			// Tag available airlocks into two categories
			Walk.checkAvailableAirlocks(buildingManager);

			// Check if good need updating
			int remainder = msol % UPDATE_GOODS_PERIOD;
			if (remainder == templateID) {
				// Update the goods value gradually with the use of buffers
				goodsManager.updateGoodValues();
			}

			remainder = msol % CHECK_MISSION;
			if (remainder == 1) {
				// Reset the mission probability back to 1
				mineralValue = -1;
			}

			remainder = msol % RESOURCE_SAMPLING_FREQ;
			if (remainder == 1) {
				// will NOT check for radiation at the exact 1000 millisols in order to balance
				// the simulation load
				// take a sample of how much each critical resource has in store
				sampleAllResources(pulse.getMarsTime());
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
				RadiationStatus newExposed = RadiationStatus.calculateCurrent(pulse.getElapsed());
				setExposed(newExposed);
			}

			remainder = msol % RESOURCE_UPDATE_FREQ;
			if (remainder == 9) {
				iceProbabilityValue = computeIceProbability();
			}

			if (remainder == 8) {
				regolithProbabilityValue = computeRegolithProbability();
			}
		}
	}

	/**
	 * Apply a clock pulse to a list of Temporal objects. This traps exceptions
	 * to avoid the impact spreading to other units.
	 * 
	 * @param pulse
	 * @param ownedUnits
	 */
	private void timePassing(ClockPulse pulse, Collection<? extends Temporal> ownedUnits) {
		for (Temporal t : ownedUnits) {
			t.timePassing(pulse);
		}
	}

	/**
	 * Pass pulse to Citizens that are not dead. Thos etht are buried are removed.
	 */
	private void timePassingCitizens(ClockPulse pulse) {
		List<Person> remove = null;
		for(Person p : citizens) {
			if (p.isDeclaredDead()) {
				// If also buried then remove it at the end of loop
				if (p.isBuried()) {
					if (remove == null) {
						remove = new ArrayList<>();
					}
					remove.add(p);
				}
			}
			else {
				p.timePassing(pulse);
			}
		}

		if (remove != null) {
			for(Person r : remove) {
				removeACitizen(r);
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
			Set<Building> b = person.getSettlement().getBuildingManager()
					.getBuildingSet(FunctionType.LIVING_ACCOMMODATIONS);
			b = BuildingManager.getNonMalfunctioningBuildings(b);
			b = getQuartersWithEmptyBeds(b, unmarked);

			if (b.size() > 0) {
				b = BuildingManager.getLeastCrowdedBuildings(b);
			}

			if (b.size() > 1) {
				Map<Building, Double> probs = BuildingManager.getBestRelationshipBuildings(person,
						b);
				result = RandomUtil.getWeightedRandomObject(probs);
			}
//			else if (b.size() == 1) {
//				return b.get(0);
//			}
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
	private static Set<Building> getQuartersWithEmptyBeds(Set<Building> buildingList, boolean unmarked) {
		Set<Building> result = new UnitSet<>();

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
	private void sampleAllResources(MarsTime now) {
		int size = samplingResources.length;
		for (int i = 0; i < size; i++) {
			int id = samplingResources[i];
			sampleOneResource(id, now);
		}
	}

	/**
	 * Samples a critical resources for stats.
	 * Creates a map for sampling how many or
	 * how much a resource has in a settlement
	 *
	 * @param resourceType
	 */
	private void sampleOneResource(int resourceType, MarsTime now) {
		int msol = now.getMillisolInt();

		if (resourceStat == null)
			resourceStat = new HashMap<>();

		Map<Integer, Map<Integer, Double>> todayMap = null;
		Map<Integer, Double> msolMap = null;
		double newAmount = getAmountResourceStored(resourceType);

		int sol = now.getMissionSol();
		if (resourceStat.containsKey(sol)) {
			todayMap = resourceStat.get(sol);
			if (todayMap.containsKey(resourceType)) {
				msolMap = todayMap.get(resourceType);
			}
			else {
				msolMap = new HashMap<>();
			}
		}
		else {
			msolMap = new HashMap<>();
			todayMap = new HashMap<>();
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
	private void performEndOfDayTasks(MarsTime marsTime) {
		int solElapsed = marsTime.getMissionSol();

		Walk.removeAllReservations(buildingManager);

		tuneJobDeficit();

		refreshResourceStat();

		refreshSleepMap(solElapsed);

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
			resourceStat = new HashMap<>();
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

	/**
	 * Gets the settlement's building manager.
	 *
	 * @return building manager
	 */
	public BuildingManager getBuildingManager() {
		return buildingManager;
	}

	/**
	 * Gets a set of adjacent buildings.
	 *
	 * @param building
	 * @return 
	 */
	public Set<Building> getAdjacentBuildings(Building building) {
		return buildingManager.getAdjacentBuildings(building);
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
	 * is an airlock available ? 
	 * Note: currently not being used.
	 * 
	 * @param person
	 * @param ingress
	 * @return
	 */
	public boolean isAirlockAvailable(Person person, boolean ingress) {
		Building currentBldg = person.getBuildingLocation();
	
		Set<Integer> bldgs = null;
		if (ingress)
			bldgs = availableDAirlocks;
		else
			bldgs = availablePAirlocks;
		Iterator<Integer> i = bldgs.iterator();
		while (i.hasNext()) {
			Building building = unitManager.getBuildingByID(i.next());
			boolean chamberFull = building.getEVA().getAirlock().areAll4ChambersFull();
			boolean reservationFull = building.getEVA().getAirlock().isReservationFull();

			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
			if ((!chamberFull || !reservationFull)
				// Check valid path
				&& buildingConnectorManager.hasValidPath(currentBldg, building)) {
					return true;
			}
		}

		return false;
	}

	/**
	 * Gets the closest available airlock at the settlement to the given location.
	 * The airlock must have a valid walkable interior path from the person's
	 * current location.
	 *
	 * @param person    the person.
	 * @param pos Position to search
	 * @param ingress is the person ingressing ?
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableAvailableAirlock(Worker worker, LocalPosition pos, boolean ingress) {
		Building currentBuilding = BuildingManager.getBuilding(worker);

		if (currentBuilding == null) {
			// Note: What if a person is out there in ERV building for maintenance ?
			// ERV building has no LifeSupport function. currentBuilding will be null
			logger.log(worker, Level.WARNING, 10_000, "Not currently in a building.");
			return null;
		}

		return getAirlock(currentBuilding, pos, ingress);
	}

	/**
	 * Gets the closest available airlock to a person.
	 *
	 * @param person the person.
	 * @param ingress is the person ingressing ?
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestAvailableAirlock(Person person, boolean ingress) {

		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;

		Set<Integer> bldgs = null;
		if (ingress) {
			bldgs = availableDAirlocks;
		}
		else {
			bldgs = availablePAirlocks;
		}
		Iterator<Integer> i = bldgs.iterator();
		while (i.hasNext()) {
			Building nextBuilding = unitManager.getBuildingByID(i.next());
			Airlock airlock = nextBuilding.getEVA().getAirlock();
			boolean chamberFull = nextBuilding.getEVA().getAirlock().areAll4ChambersFull();
//			boolean reservationFull = building.getEVA().getAirlock().isReservationFull();

			// Note: ingress is not being used here
			
			if (!ASTRONOMY_OBSERVATORY.equalsIgnoreCase(nextBuilding.getBuildingType())) {
				if (result == null) {
					result = airlock;
					continue;
				}
				double distance = nextBuilding.getPosition().getDistanceTo(person.getPosition());
				if (distance < leastDistance
					&& !chamberFull) {
						result = airlock;
						leastDistance = distance;
				}
			}
		}

		return result;
	}
	
	/**
	 * Gets an airlock for an EVA egress, preferably an pressurized airlock.
	 * Consider if the chambers are full and if the reservation is full.
	 *
	 * @param currentBuilding
	 * @param pos Position for search
	 * @param ingress is the person ingressing ?
	 * @return
	 */
	private Airlock getAirlock(Building currentBuilding, LocalPosition pos, boolean ingress) {
		Airlock result = null;

		// Search the closest of the buildings
		double leastDistance = Double.MAX_VALUE;

		Set<Integer> bldgs = null;
		if (ingress) {
			bldgs = availableDAirlocks;
		}
		else {
			bldgs = availablePAirlocks;
		}
		Iterator<Integer> i = bldgs.iterator();
		while (i.hasNext()) {
			Building building = unitManager.getBuildingByID(i.next());
			Airlock airlock = building.getEVA().getAirlock();
			boolean chamberFull = airlock.areAll4ChambersFull();
//			boolean reservationFull = building.getEVA().getAirlock().isReservationFull();

			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
//			if ((!chamberFull || !reservationFull)
					
			if (buildingConnectorManager.hasValidPath(currentBuilding, building)) {
				if (result == null) {
					result = airlock;
					continue;
				}
				double distance = building.getPosition().getDistanceTo(pos);
				if (distance < leastDistance
					&& !chamberFull) {
						result = airlock;
						leastDistance = distance;
				}
			}
		}

		return result;
	}


	/**
	 * Categorizes the state of the airlocks.
	 * 
	 * @param bldgs
	 * @param pressurized
	 */
	public void trackAirlocks(Set<Building> bldgs, boolean pressurized) {	
		for (Building building : bldgs) {
			boolean chamberFull = building.getEVA().getAirlock().areAll4ChambersFull();
			boolean reservationFull = building.getEVA().getAirlock().isReservationFull();

			int id = building.getIdentifier();
			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
			if (!chamberFull || !reservationFull) {
				if (pressurized) {
					if (!availablePAirlocks.contains(id)) {
						availablePAirlocks.add(id);
					}
				}
				else {
					if (!availableDAirlocks.contains(id)) {
						availableDAirlocks.add(id);
					}
				}
			}
			else {
				if (pressurized) {
					if (availablePAirlocks.contains(id)) {
						availablePAirlocks.remove(id);
					}
				}
				else {
					if (!availableDAirlocks.contains(id)) {
						availableDAirlocks.add(id);
					}
				}
			}
		}
	}

	/**
	 * Gets the closest available airlock at the settlement to the given location.
	 * The airlock must have a valid walkable interior path from the given
	 * building's current location.
	 * 
	 * @Note: Currently, not being in use
	 *
	 * @param building  the building in the walkable interior path.
	 * @param location  Starting position.
	 * @param isIngress is airlock in ingress mode ?
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableAvailableAirlock(Building building, LocalPosition location, 
			boolean isIngress) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;

		Iterator<Building> i = buildingManager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building nextBuilding = i.next();
			Airlock airlock = nextBuilding.getEVA().getAirlock();		
			boolean chamberFull = airlock.areAll4ChambersFull();
			
//			boolean reservationFull = airlock.isReservationFull();

			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
			// 3. if ingressing, make sure this airlock is in ingress mode or not-in-use mode
			// 4. if egressing, make sure this airlock is in egress mode or not-in-use mode

			// Note: the use of reservationFull is being put on hold
			
			AirlockMode airlockMode = airlock.getAirlockMode();
			boolean isIngressMode = airlockMode == AirlockMode.INGRESS;
			boolean isEgressMode = airlockMode == AirlockMode.EGRESS;
			boolean notInUse = airlockMode == AirlockMode.NOT_IN_USE;
			
			if (!chamberFull
				&& (notInUse
						|| (isIngress && isIngressMode)
						|| (!isIngress && isEgressMode)) 
				&& buildingConnectorManager.hasValidPath(building, nextBuilding)) {

				double distance = nextBuilding.getPosition().getDistanceTo(location);
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
	 * Gets the closest available airlock at the settlement to the given location.
	 * The airlock must have a valid walkable interior path from the given
	 * building's current location.
	 *
	 * @param building  the building in the walkable interior path.
	 * @param location  Starting position.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableAvailableAirlock(Building building, LocalPosition location) {
		Airlock result = null;

		double leastDistance = Double.MAX_VALUE;

		Iterator<Building> i = buildingManager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building nextBuilding = i.next();
			Airlock airlock = nextBuilding.getEVA().getAirlock();
			
			boolean chamberFull = airlock.areAll4ChambersFull();
//			boolean reservationFull = airlock.isReservationFull();

			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full

			// Note: the use of chamberFull and reservationFull are being put on hold
			// since it creates excessive logs. Thus it needs to be handled differently 
			
			if (buildingConnectorManager.hasValidPath(building, nextBuilding)) {
				if (result == null) {
					result = airlock;
					continue;
				}
				double distance = nextBuilding.getPosition().getDistanceTo(location);
				if (distance < leastDistance
					&& !chamberFull) {
						result = airlock;
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
	 * @param location  Starting position.
	 * @param isIngress is airlock in ingress mode ?
	 * @return airlock or null if none available.
	 */
	public boolean hasClosestWalkableAvailableAirlock(Building building, LocalPosition location) {
		Iterator<Building> i = buildingManager.getBuildings(FunctionType.EVA).iterator();
		while (i.hasNext()) {
			Building nextBuilding = i.next();
			boolean chamberFull = nextBuilding.getEVA().getAirlock().areAll4ChambersFull();
			if (!chamberFull
				&& buildingConnectorManager.hasValidPath(building, nextBuilding)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Checks if a building has a walkable path from it to an airlock.
	 *
	 * @param building the building.
	 * @return true if an airlock is walkable from the building.
	 */
	public boolean hasWalkableAvailableAirlock(Building building) {
		return hasClosestWalkableAvailableAirlock(building, LocalPosition.DEFAULT_POSITION);
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
	 * Makes this person's physical location to be inside this settlement.
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
	 * Removes this person's physical location from being inside this settlement.
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
	 * Assigns a person to be a legal citizen of this settlement.
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

			// Update active mission limit; always at least 1
			double optimalMissions = Math.max(1D, Math.floor(numCitizens/PERSON_PER_MISSION));
			setPreferenceModifier(MISSION_LIMIT, optimalMissions);

			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Removes this person from being a legal citizen of this settlement.
	 *
	 * @param p the person
	 */
	public boolean removeACitizen(Person p) {
		if (!citizens.contains(p))
			return true;
		if (citizens.remove(p)) {
			removePeopleWithin(p);
			// Update the numCtizens
			numCitizens = citizens.size();
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Returns the person instance of the commander of this settlement.
	 * 
	 * @return
	 */
	public Person getCommander() {
		for (Person p: citizens) {
			if (RoleType.COMMANDER == p.getRole().getType())
				return p;
		}
		
		return null;
	}
	
	
	/**
	 * Assigns a robot to be owned by the settlement.
	 *
	 * @param r the robot
	 */
	public boolean addOwnedRobot(Robot r) {
		if (ownedRobots.contains(r))
			return true;
		if (ownedRobots.add(r)) {
			addRobotsWithin(r);
			r.setCoordinates(getCoordinates());
			r.setContainerUnit(this);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT, this);
			numOwnedBots = ownedRobots.size();
			return true;
		}
		return false;
	}

	/**
	 * Removes a robot from being owned by the settlement.
	 *
	 * @param r the robot
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
	 * Assigns a robot to being within this settlement.
	 *
	 * @param r the robot
	 */
	public boolean addRobotsWithin(Robot r) {
		if (robotsWithin.contains(r)) {
			return true;
		}
		if (robotsWithin.add(r)) {
			return true;
		}
		return false;
	}

	/**
	 * Removes a robot from being within this settlement.
	 *
	 * @param r the robot
	 */
	public boolean removeRobotsWithin(Robot r) {
		if (!robotsWithin.contains(r)) {
			return true;
		}
		if (robotsWithin.remove(r)) {
			return true;
		}
		return false;
	}

	/**
	 * Adds a parked vehicle.
	 *
	 * @param vehicle
	 * @param true if the parked vehicle can be added
	 */
	public boolean addParkedVehicle(Vehicle vehicle) {
		if (parkedVehicles.contains(vehicle)) {
			return true;
		}
		if (parkedVehicles.add(vehicle)) {
			// Directly update the location state type
			vehicle.updateLocationStateType(LocationStateType.WITHIN_SETTLEMENT_VICINITY);
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
	 * Gets a vehicle of this particular vehicle type.
	 * 
	 * @param vehicleType
	 * @return
	 */
	public Vehicle getAVehicle(VehicleType vehicleType) {
		return ownedVehicles
		.stream()
		.filter(v -> v.getVehicleType() == vehicleType)
		.findAny().orElse(null);
	}
	
	
	/**
	 * Adds an equipment to be owned by the settlement.
	 *
	 * @param e the equipment
	 * @return true if this settlement can carry it
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Removes an equipment from being owned by the settlement.
	 *
	 * @param e the equipment
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
					&& (v.getMission().getStage() == Stage.ACTIVE))
				.collect(Collectors.toList());
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
		return ownedVehicles.stream()
				.filter(v -> v.getVehicleType() == VehicleType.DELIVERY_DRONE)
				.filter(v -> this.equals(v.getSettlement()))
				.map(Drone.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the number of drones parked or garaged at the settlement.
	 *
	 * @return parked drones number
	 */
	public int getNumParkedDrones() {
		return Math.toIntExact(ownedVehicles
				.stream()
				.filter(v -> v.getVehicleType() == VehicleType.DELIVERY_DRONE)
				.filter(v -> this.equals(v.getSettlement()))
				.collect(Collectors.counting()));
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
		return Math.toIntExact(ownedVehicles
					.stream()
					.filter(v -> VehicleType.isRover(v.getVehicleType()))
					.filter(v -> this.equals(v.getSettlement()))
					.collect(Collectors.counting()));
	}

	/**
	 * Gets a collection of vehicles parked or garaged at the settlement.
	 *
	 * @return Collection of parked vehicles
	 */
	public Collection<Vehicle> getParkedVehicles() {
		// Get all Vehicles that are back home
		return 	ownedVehicles.stream()
					.filter(v -> this.equals(v.getSettlement()))
					.collect(Collectors.toList());
	}

	/**
	 * Gets the number of vehicles (rovers, LUVs, and drones) parked or garaged at the settlement.
	 *
	 * @return parked vehicles number
	 */
	public int getNumParkedVehicles() {
		return Math.toIntExact(ownedVehicles
				.stream()
				.filter(v -> this.equals(v.getSettlement()))
				.collect(Collectors.counting()));
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
			logger.log(this, Level.CONFIG, 0L, "Player enables the override on '" + type.toString() + "'.");
			this.processOverrides.add(type);
		}
		else {
			logger.log(this, Level.CONFIG, 0L, "Player disables the override on '" + type.toString() + "'.");
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
		return this.processOverrides.contains(type);
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
	public int getInitialNumOfRobots() {
		return initialNumOfRobots;
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
	 * Get the shift manager for this Settlement
	 */
	public ShiftManager getShiftManager() {
		return shiftManager;
	}

	public RadiationStatus getExposed() {
		return exposed;
	}

	/*
	 * Update the status of Radiation exposure
	 * @param newExposed
	 */
	public void setExposed(RadiationStatus newExposed) {
		RadiationStatus oldStatus = exposed;
		exposed = newExposed;
		
		if (exposed.isBaselineEvent() && !oldStatus.isBaselineEvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR_GRID + UnitEventType.BASELINE_EVENT.toString() + " is imminent.");
			this.fireUnitUpdate(UnitEventType.BASELINE_EVENT);
		}

		if (exposed.isGCREvent() && !oldStatus.isGCREvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR_GRID + UnitEventType.GCR_EVENT.toString() + " is imminent.");
			this.fireUnitUpdate(UnitEventType.GCR_EVENT);
		}

		if (exposed.isSEPEvent() && !oldStatus.isSEPEvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR_GRID + UnitEventType.SEP_EVENT.toString() + " is imminent.");
			this.fireUnitUpdate(UnitEventType.SEP_EVENT);
		}
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
				* MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR;

		double ratio = requiredDrinkingWaterOrbit / storedWater;
		waterRationLevel = (int) ratio;

		if (waterRationLevel < 1)
			waterRationLevel = 1;
		else if (waterRationLevel > 1000)
			waterRationLevel = 1000;

		if (waterRationLevel > 100)
			logger.severe(this, 20_000L, "Water Ration Level: " + waterRationLevel);
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

		switch(objectiveType) {
			case CROP_FARM:
				return goodsManager.getCropFarmFactor();
			case MANUFACTURING_DEPOT:
				return goodsManager.getManufacturingFactor();
			case RESEARCH_CAMPUS:
				return goodsManager.getResearchFactor();
			case TRANSPORTATION_HUB:
				return goodsManager.getTransportationFactor();
			case TRADE_CENTER:
				return goodsManager.getTradeFactor();
			case TOURISM:
				return goodsManager.getTourismFactor();
			default:
				return -1;
		}
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
	 * Get the total area of Crops in this Settlement
	 */
	public double getTotalCropArea() {
		if (cropArea < 0D) {
			cropArea = 0D;
			
			for (Building b : buildingManager.getBuildings(FunctionType.FARMING)) {
				cropArea += b.getFarming().getGrowingArea();
			}
		}

		return cropArea;
	} 
	
	/**
	 * Computes the probability of the presence of regolith
	 *
	 * @return probability of finding regolith
	 */
	private double computeRegolithProbability() {
		double result = 0;
		double regolithDemand = goodsManager.getDemandValueWithID(REGOLITH_ID);
		if (regolithDemand > REGOLITH_MAX)
			regolithDemand = REGOLITH_MAX;
		else if (regolithDemand < 1)
			regolithDemand = 1;

		double sandDemand = goodsManager.getDemandValueWithID(SAND_ID);
		if (sandDemand > REGOLITH_MAX)
			sandDemand = REGOLITH_MAX;
		else if (sandDemand < 1)
			sandDemand = 1;
		
		double concreteDemand = goodsManager.getDemandValueWithID(CONCRETE_ID);
		if (concreteDemand > REGOLITH_MAX)
			concreteDemand = REGOLITH_MAX;
		else if (concreteDemand < 1)
			concreteDemand = 1;
		
		double cementDemand = goodsManager.getDemandValueWithID(CEMENT_ID);
		if (cementDemand > REGOLITH_MAX)
			cementDemand = REGOLITH_MAX;
		else if (cementDemand < 1)
			cementDemand = 1;

		double regolithAvailable = goodsManager.getSupplyValue(REGOLITH_ID);
		regolithAvailable = regolithAvailable * regolithAvailable - 1;
		
		double sandAvailable = goodsManager.getSupplyValue(SAND_ID);
		sandAvailable = sandAvailable * sandAvailable - 1;
		
		int pop = numCitizens;
		int reserve = (MIN_REGOLITH_RESERVE + MIN_SAND_RESERVE) * pop;
		
		if (regolithAvailable + sandAvailable > reserve + regolithDemand + sandDemand) {
			result = reserve + regolithDemand + sandDemand - regolithAvailable - sandAvailable;
		}
		
		else if (regolithAvailable + sandAvailable > reserve) {
			result = reserve - regolithAvailable - sandAvailable;
		}

		else {
			result = 1.0 * reserve / pop ;
		}

		result = result + .5 * concreteDemand + .5 * cementDemand;
		
		if (result < 0)
			result = 0;
		if (result > MAX_PROB)
			result = MAX_PROB;
		
//		logger.info(this, 30_000L, "regolithDemand: " + regolithDemand
//						+ "   cementDemand: " + cementDemand
//						+ "   concreteDemand: " + concreteDemand
//						+ "   sandDemand: " + sandDemand
//						+ "   regolith Prob value: " + result);
		return result;
	}

	/**
	 * Computes the probability of the presence of ice
	 *
	 * @return probability of finding ice
	 */
	private double computeIceProbability() {
		double result = 0;
		double iceDemand = goodsManager.getDemandValueWithID(ICE_ID);
		if (iceDemand > ICE_MAX)
			iceDemand = ICE_MAX;
		if (iceDemand < 1)
			iceDemand = 1;
		
		double waterDemand = goodsManager.getDemandValueWithID(WATER_ID);
		waterDemand = waterDemand * waterRationLevel / 10;
		if (waterDemand > WATER_MAX)
			waterDemand = WATER_MAX;
		if (waterDemand < 1)
			waterDemand = 1;
		
		double brineWaterDemand = goodsManager.getDemandValueWithID(BRINE_WATER_ID);
		brineWaterDemand = brineWaterDemand * waterRationLevel / 10;
		if (waterDemand > WATER_MAX)
			waterDemand = WATER_MAX;
		if (waterDemand < 1)
			waterDemand = 1;
		
		// Compare the available amount of water and ice reserve
		double iceSupply = goodsManager.getSupplyValue(ICE_ID);
		double waterSupply = goodsManager.getSupplyValue(WATER_ID);
		double brineWaterSupply = goodsManager.getSupplyValue(BRINE_WATER_ID);
		
		int pop = numCitizens;
		int reserve = (MIN_WATER_RESERVE + MIN_ICE_RESERVE) * pop;

		double totalSupply = iceSupply + waterSupply + brineWaterSupply;
		double totalDemand = iceDemand + waterDemand + brineWaterDemand;
		
		if (totalSupply > reserve + totalDemand) {
			result = reserve + totalDemand - totalSupply;
		}
		
		else if (totalSupply > reserve) {
			result = reserve - totalSupply;
		}

		// Prompt the collect ice mission to proceed more easily if water resource is
		// dangerously low,
		else {
			// no change to missionProbability
			result = 1.0 * reserve / pop;
		}
		
		if (result < 0)
			result = 0;
		
//		logger.info(this, 30_000L, "iceDemand: " + iceDemand
//				+ "   waterDemand: " + waterDemand
//				+ "   ice Prob value: " + result
//				);
		
		if (result > MAX_PROB)
			result = MAX_PROB;
		
		return result;
	}

	/**
	 * Checks the demand of oxygen.
	 *
	 * @return 
	 */
	private void checkOxygenDemand() {
		double result = 0;

		int pop = numCitizens;
		
		double demand = goodsManager.getDemandValueWithID(OXYGEN_ID);
		if (demand > OXYGEN_MAX)
			demand = OXYGEN_MAX;
		if (demand < 1)
			demand = 1;
		
		// Compare the available amount of oxygen
		double supply = goodsManager.getSupplyValue(OXYGEN_ID);

		double reserve = getAmountResourceStored(OXYGEN_ID);
	
		if (reserve + supply * pop > (MIN_OXYGEN_RESERVE + demand) * pop) {
			result = (MIN_OXYGEN_RESERVE + demand - supply) * pop - reserve;
		}

		else if (reserve + supply * pop > MIN_OXYGEN_RESERVE * pop) {
			result = (MIN_OXYGEN_RESERVE - supply) * pop - reserve;
		}

		else {
			result = (MIN_OXYGEN_RESERVE + demand - supply) * pop - reserve;
		}
		
		if (result < 0)
			result = 0;
				
		if (result > OXYGEN_MAX)
			result = OXYGEN_MAX;

		double delta = result - demand;

		if (delta > 50) {
			
			// Limit each increase to 10 only to avoid an abrupt increase 
			delta = 50;
			
			logger.info(this, 10_000L, 
					"oxygen demand: " + Math.round(demand * 10.0)/10.0 
					+ "  supply: " + Math.round(supply * 10.0)/10.0 
					+ "  reserve: " + Math.round(reserve * 10.0)/10.0		
					+ "  delta: " + Math.round(delta * 10.0)/10.0
					+ "  new demand: " + Math.round((demand + delta) * 10.0)/10.0 + ".");
			
			// Inject a sudden change of demand
			goodsManager.setDemandValue(GoodsUtil.getGood(OXYGEN_ID), (demand + delta));
		}
		
	}
	
	/**
	 * Checks if the last 20 mission scores are above the threshold.
	 *
	 * @param score
	 * @return true/false
	 */
	public boolean passMissionScore(double score) {

		return (score > minimumPassingScore);
	}

	/**
	 * Calculates the current minimum passing score.
	 *
	 * @return
	 */
	public double getMinimumPassingScore() {
		return minimumPassingScore;
	}

	/**
	 * Saves the mission score.
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

	public double getOutsideTemperature() {
		return outside_temperature;
	}

	public DustStorm getDustStorm() {
		return storm;
	}

	public void setDustStorm(DustStorm storm) {
		this.storm = storm;
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
	 * yesterday's usage. Least weight on usage from x sols ago.
	 *
	 * @param type
	 * @return
	 */
	public double getDailyWaterUsage(WaterUseType type) {
		return waterConsumption.getDailyAverage(type);
	}

	/**
	 * Assigns the best candidate to a job position.
	 * 
	 * @param settlement
	 * @param job
	 */
	private static void assignBestCandidate(Settlement settlement, JobType job) {
		Person p0 = JobUtil.findBestFit(settlement, job);
		// Designate a specific job to a person
		if (p0 != null) {
			p0.getMind().assignJob(job, true, JobUtil.SETTLEMENT, AssignmentType.APPROVED, JobUtil.SETTLEMENT);
		}
	}

	/**
	 * Tunes up the settlement with unique job position.
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
						AssignmentType.APPROVED,
						JobUtil.SETTLEMENT);
			}
			if ((numTechs == 0) && (bestTech != null)) {
				bestTech.getMind().assignJob(JobType.TECHNICIAN, true,
						JobUtil.SETTLEMENT,
						AssignmentType.APPROVED,
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
		double newValue = (disable ? 0D : 1D);
		setPreferenceModifier(new PreferenceKey(Type.MISSION, mission.name()), newValue);
	}

	public void setAllowTradeMissionFromASettlement(Settlement settlement, boolean allowed) {
		allowTradeMissionSettlements.put(settlement.getIdentifier(), allowed);
	}

	public boolean isAllowedTradeMission(Settlement settlement) {
		return allowTradeMissionSettlements.getOrDefault(settlement.getIdentifier(), Boolean.TRUE);
	}

	/**
	 * Checks if the mission is enabled.
	 *
	 * @param mission the type of the mission calling this method
	 * @return probability value
	 */
	public boolean isMissionEnable(MissionType mission) {
		return (getPreferenceModifier(new PreferenceKey(Type.MISSION, mission.name())) > 0D);
	}

	/**
	 * Gets the available vehicle at the settlement with the minimal range.
	 *
	 * @param settlement         the settlement to check.
	 * @return vehicle or null if none available.
	 */
	public Rover getVehicleWithMinimalRange() {
		Rover result = null;

		for (Vehicle vehicle : getAllAssociatedVehicles()) {

			if (vehicle instanceof Rover rover) {
				if (result == null)
					// Get the first vehicle
					result = rover;
				else if (vehicle.getRange() < result.getRange())
					// This vehicle has a lesser range than the previously selected vehicle
					result = rover;
			}
		}

		return result;
	}
	
	/**
	 * Creates a set of nearby mineral locations.
	 * 
	 * @param rover
	 */
	public void createNearbyMineralLocations(Rover rover) {
		double roverRange = rover.getRange();
		double tripTimeLimit = rover.getTotalTripTimeLimit(true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		Set<Coordinates> coords = surfaceFeatures.getMineralMap()
				.generateMineralLocations(getCoordinates(), range / 2D);
		
		nearbyMineralLocations.addAll(coords);
	}
	
	/**
	 * Gets a set of nearby mineral locations.
	 * 
	 * @param rover
	 */
	public Set<Coordinates> getNearbyMineralLocations() {
		return nearbyMineralLocations;
	}
	
	/**
	 * Gets a random nearby mineral location that can be reached by any rover.
	 * 
	 * @param rover
	 */
	public Coordinates getARandomNearbyMineralLocation(double limit) {
		
		double range = getVehicleWithMinimalRange().getRange();
			
		range = Math.min(range, limit);
		
		Coordinates chosen = null;
		
		// Remove coordinates that have been explored or staked by other settlements
		
//		Set<Coordinates> surfaceFeaturesSet = surfaceFeatures.getDeclaredCoordinates(false);
		
		// Create a set
//		Set<Coordinates> intersection = new HashSet<>(nearbyMineralLocations);
		
//		logger.info(this, "1. surfaceFeatures sets:" + surfaceFeaturesSet.size()
//					+ "  nearbyMineralLocations sets:" + nearbyMineralLocations.size()
//					+ "  intersection sets:" + intersection.size());

		// Execute to create the union of the two sets
//		intersection.retainAll(surfaceFeaturesSet);
		
//		logger.info(this, "2. surfaceFeatures sets:" + surfaceFeaturesSet.size()
//					+ "  nearbyMineralLocations sets:" + nearbyMineralLocations.size()
//					+ "  intersection sets:" + intersection.size());
		
		// Remove the union
//		nearbyMineralLocations.removeAll(intersection);
		
//		logger.info(this, "3. surfaceFeatures sets:" + surfaceFeaturesSet.size()
//					+ "  nearbyMineralLocations sets:" + nearbyMineralLocations.size()
//					+ "  intersection sets:" + intersection.size());
		
		if (nearbyMineralLocations.isEmpty()) {
			logger.info(this, "nearbyMineralLocations is empty.");
			return null;
		}
		
		Set<Coordinates> unclaimedLocations = new HashSet<>();
		
		for (Coordinates c : nearbyMineralLocations) {
			boolean unclaimed = surfaceFeatures.isDeclaredARegionOfInterest(c, this, false);
			if (unclaimed)
				unclaimedLocations.add(c);
		}

		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		for (Coordinates c : unclaimedLocations) {
			double distance = Coordinates.computeDistance(getCoordinates(), c);

			// Fill up the weight map
			weightedMap.put(c, (range - distance) / range);
		}

		// Choose one with weighted randomness 
		chosen = RandomUtil.getWeightedRandomObject(weightedMap);

		if (chosen == null) {
			logger.info(this, "Picked a nearby mineral location.");
			return new ArrayList<>(nearbyMineralLocations).get(0);
		}
		
		return chosen;
	}
	
	/**
	 * Gets a comfortable nearby mineral location, based on skills.
	 * 
	 * @param limit
	 * @param skillDistance
	 */
	public Coordinates getAComfortableNearbyMineralLocation(double limit, int skillDistance) {
		
		double range = Math.min(skillDistance, limit);

		return getARandomNearbyMineralLocation(range);
	}
	
	/**
	 * Creates a Region of Interest (ROI) at a given location and
	 * estimate its mineral concentrations.
	 * 
	 * @param siteLocation
	 * @param skill
	 * @return ExploredLocation
	 */
	public ExploredLocation createARegionOfInterest(Coordinates siteLocation, int skill) {
	
		// Check if this siteLocation has already been added or not to SurfaceFeatures
		ExploredLocation el = surfaceFeatures.checkDeclaredLocation(siteLocation, this, false);
		if (el == null) {
			// If it doesn't exist yet
			el = surfaceFeatures.declareRegionOfInterest(siteLocation,
					skill, this);
		}

//		if (el != null)
//			// remove this coordinate from nearbyMineralLocations
//			nearbyMineralLocations.remove(siteLocation);
		
		return el;
	}
	
	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 *
	 * @param rover          the rover to use.
	 * @param homeSettlement the starting settlement.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	public Map<String, Integer> getNearbyMineral(Rover rover) {
		Map<String, Integer> minerals = new HashMap<>();

		double roverRange = rover.getRange();
		double tripTimeLimit = rover.getTotalTripTimeLimit(true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		if (nearbyMineralLocations == null || nearbyMineralLocations.isEmpty()) {
			logger.severe(this, "nearbyMineralLocations is empty.");

			// Creates a set of nearby mineral locations	
			Set<Coordinates> coords = surfaceFeatures.getMineralMap()
					.generateMineralLocations(getCoordinates(), range / 2D);
			
			nearbyMineralLocations.addAll(coords);
		}
			
		for (Coordinates c : nearbyMineralLocations) {
			double distance = Coordinates.computeDistance(getCoordinates(), c);

			// Fill up the weight map
			weightedMap.put(c, (range - distance) / range);
		}

		// Choose one with weighted randomness 
		Coordinates chosen = RandomUtil.getWeightedRandomObject(weightedMap);
		double chosenDist = weightedMap.get(chosen);
		
		logger.info(CollectionUtils.findSettlement(getCoordinates()), 30_000L, 
				"Investigating mineral site at " + chosen + " (" + Math.round(chosenDist * 10.0)/10.0 + " km).");
		
		if (chosen != null)
			minerals = surfaceFeatures.getMineralMap().getAllMineralConcentrations(chosen);

		return minerals;
	}
	
	
	/**
	 * Gets the total mineral value, based on the range of a given rover.
	 * 
	 * @param rover
	 * @return
	 */
	public int getTotalMineralValue(Rover rover) {
		if (mineralValue == -1) {
			// Check if any mineral locations within rover range and obtain their
			// concentration
			Map<String, Integer> minerals = getNearbyMineral(rover);
			if (minerals != null && !minerals.isEmpty()) {
				mineralValue = Exploration.getTotalMineralValue(this, minerals);
			}
		}
		return mineralValue;
	}
	
	
	/**
	 * Gets the range of a trip based on its time limit and exploration sites.
	 *
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed  the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private double getTripTimeRange(double tripTimeLimit, double averageSpeed) {
		int sol = masterClock.getMarsTime().getMissionSol();
		int numSites = 2 + (int)(1.0 * sol / 20);
		double siteTime = 250;
		
		double tripTimeTravellingLimit = tripTimeLimit - (numSites * siteTime);
		double millisolsInHour = MarsTime.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}
	
	/**
	 * Returns the ice collection rate in the vicinity of this settlement.
	 * 
	 * @return
	 */
    public double getIceCollectionRate() {
    	return iceCollectionRate;
    }

    /**
	 * Returns the regolith collection rate in the vicinity of this settlement.
     * 
     * @return
     */
    public double getRegolithCollectionRate() {
    	return regolithCollectionRate;
    }

	/**
	 * Removes the record of the deceased person from airlock.
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
	 * 
	 * @param id resource id
	 */
	public double getResourceCollected(int id) {
		return resourcesCollected.get(id);
	}

	/**
	 * Adds the amount of resource collected.
	 * 
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
	 * Gets the stored mass
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Gets the equipment list.
	 *
	 * @return the equipment list
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		return eqmInventory.getEquipmentSet();
	}

	/**
	 * Gets a list of the equipment with particular equipment type.
	 *
	 * @return the equipment list
	 */
	public Set<Equipment> getEquipmentTypeList(EquipmentType equipmentType) {
		return eqmInventory.getEquipmentSet().stream()
				.filter(e -> e.getEquipmentType() == equipmentType)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets the number of available EVA suits.
	 * 
	 * @return
	 */
	public int getNumEVASuit() {
		return Math.toIntExact(getEquipmentTypeList(EquipmentType.EVA_SUIT)
				.stream()
				.collect(Collectors.counting()));
	}
	
	/**
	 * Does it possess an equipment of this equipment type ?
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
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return eqmInventory. hasAmountResourceRemainingCapacity(resource);
	}
	
	/**
	 * Obtains the remaining general storage space.
	 *
	 * @return quantity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return eqmInventory.getRemainingCargoCapacity();
	}

	/**
     * Gets the total capacity that this inventory can hold.
     *
     * @return total capacity (kg).
     */
	@Override
	public double getCargoCapacity() {
		return eqmInventory.getCargoCapacity();
	}

	/**
	 * Gets the amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return eqmInventory.getAmountResourceStored(resource);
	}

	/**
	 * Gets all the amount resource resource stored, including inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return eqmInventory.getAllAmountResourceStored(resource);
	}
	
	/**
	 * Gets the amount resource owned by all resource holders.
	 *
	 * @param resource
	 * @return quantity
	 */
	public double getAllAmountResourceOwned(int resource) {
		double sum = 0;
		for (ResourceHolder rh: citizens) {
			sum += rh.getAmountResourceStored(resource);
		}
		for (ResourceHolder rh: ownedVehicles) {
			sum += rh.getAmountResourceStored(resource);
		}		
		return sum + getAmountResourceStored(resource);
	}
	
	/**
	 * Gets all stored amount resources.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return eqmInventory.getAmountResourceIDs();
	}

	/**
	 * Gets all stored item resources.
	 *
	 * @return all stored item resources.
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return eqmInventory.getItemResourceIDs();
	}

	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceIDs() {
		return eqmInventory.getAllAmountResourceIDs();
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
	 * Gets the remaining quantity of an item resource.
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
	 * Finds the number of containers of a particular type.
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
	 * 
	 * @return
	 */
	public EquipmentInventory getEquipmentInventory() {
		return eqmInventory;
	}

	/**
	 * Gets the task manager that controls the backlog for the Settlement.
	 */
    public SettlementTaskManager getTaskManager() {
        return taskManager;
    }

	/**
	 * Gets the credit manager.
	 *
	 * @return credit manager.
	 */
	public CreditManager getCreditManager() {
		return creditManager;
	}
	
	/**
	 * Sets the credit manager.
	 * 
	 * @param cm
	 */
	public void setCreditManager(CreditManager cm) {
		creditManager = cm;
	}
	
	/**
	 * Gets the manager of future scheduled events for this settlement.
	 */
	public ScheduledEventManager getFutureManager() {
		return futureEvents;
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
	 * Gets the holder's unit instance.
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}
	
	/**
	 * Get the modifier to apply of a certain preference
	 * @param key The preference
	 * @return The appropriate modifier; return 1 by default
	 */
	public double getPreferenceModifier(PreferenceKey key) {
		return preferenceModifiers.getOrDefault(key, 1D);
	}

	/**
	 * Set the modifier to apply to preference of a certain type.
	 * @param key The preference to update
	 * @param value The new modifier value
	 */
	public void setPreferenceModifier(PreferenceKey key, double value) {
		preferenceModifiers.put(key, value);
	}

	/**
	 * Get the preference that this Settlement influences
	 */
	public Set<PreferenceKey> getKnownPreferences() {
		return preferenceModifiers.keySet();
	}

	/**
	 * Reinitialize references after loading from a saved sim.
	 */
	public void reinit() {
		if (surfaceFeatures == null) 
			surfaceFeatures = Simulation.instance().getSurfaceFeatures();
		
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

		if (creditManager != null) {
			creditManager.destroy();
			creditManager = null;
		}
		
		template = null;

		scientificAchievement = null;
	}
}
