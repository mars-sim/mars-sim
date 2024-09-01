/*
 * Mars Simulation Project
 * Settlement.java
 * @date 2024-07-23
 * @author Scott Davis
 */

package com.mars_sim.core.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.LifeSupportInterface;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.activities.GroupActivity;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.SolMetricDataLogger;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.environment.DustStorm;
import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.environment.RandomMineralMap;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.equipment.AmountResourceBin;
import com.mars_sim.core.equipment.Bin;
import com.mars_sim.core.equipment.BinHolder;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.equipment.ItemHolder;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.events.ScheduledEventManager;
import com.mars_sim.core.goods.CreditManager;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.person.Commander;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.MissionLimitParameters;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.MissionWeightParameters;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.shift.ShiftManager;
import com.mars_sim.core.person.ai.shift.ShiftPattern;
import com.mars_sim.core.person.ai.social.Appraiser;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.SettlementTaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.core.process.CompletedProcess;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.connection.BuildingConnectorManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.LivingAccommodation;
import com.mars_sim.core.structure.building.utility.heating.ThermalSystem;
import com.mars_sim.core.structure.building.utility.power.PowerGrid;
import com.mars_sim.core.structure.construction.ConstructionManager;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * The Settlement class represents a settlement unit on virtual Mars. It
 * contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements Temporal,
	LifeSupportInterface, EquipmentOwner, ItemHolder, BinHolder, Appraiser {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Settlement.class.getName());

	// Static members
	private static final int NUM_BACKGROUND_IMAGES = 20;

	/**
	 * Shared preference key for Mission limits
	 */
	private static final int CHECK_MISSION = 20; // once every 10 millisols
	private static final int MAX_NUM_SOLS = 3;
	private static final int RESOURCE_UPDATE_FREQ = 30;
	private static final int RESOURCE_SAMPLING_FREQ = 50; // in msols
	private static final int RESOURCE_STAT_SOLS = 12;

	private static final int MAX_PROB = 3000;
	private static final int MIN_REGOLITH_RESERVE = 400; // per person
	private static final int MIN_SAND_RESERVE = 400; // per person
	
	private static final int REGOLITH_MAX = 4000;
	private static final int ICE_MAX = 4000;
	private static final int WATER_MAX = 10_000;

	private static final int MIN_WATER_RESERVE = 400; // per person
	private static final int MIN_ICE_RESERVE = 400; // per person
	
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
	protected static final int[] samplingResources;

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
	private static final double INITIAL_MISSION_PASSING_SCORE = 50D;
	/** The Maximum mission score that can be recorded. */
	private static final double MAX_MISSION_SCORE = 1000D;
	/** Normal air pressure [in kPa]. */
	private static final double NORMAL_AIR_PRESSURE = 34D;
	/** Maximum percentage of citizens that are EVA'ing. */
	private static final double EVA_PERCENTAGE = 0.25;

	/** The settlement water consumption */
	private static double waterConsumptionRate;
	/** The settlement minimum air pressure requirement. */
	private static double minimumAirPressure;
	/** The settlement life support requirements. */
	private static double[][] lifeSupportValues = new double[2][7];
	
	private static final String IMMINENT = " be imminent.";
	private static final String DETECTOR = "The radiation detector just forecasted a ";
	private static final String ASTRONOMY_OBSERVATORY = "Astronomy Observatory";

	
	/** The flag for checking if the simulation has just started. */
	private boolean justLoaded = true;
	/** The flag signifying this settlement as the destination of the user-defined commander. */
	private boolean hasDesignatedCommander = false;
	/** The flag to see if a water ration review is due. */
	private boolean waterRatioReviewFlag = false;
	/** The water ratio of the settlement. The higher the more urgent for water resource. */
	private int waterRatioCache = 1;
	/** The new water ratio of the settlement. */
	private int newWaterRatio = 0;
	
	/** The number of people at the start of the settlement. */
	private int initialPopulation;
	/** The number of robots at the start of the settlement. */
	private int initialNumOfRobots;
	/** The cache for the mission sol. */
	private int solCache = 0;
	/** Numbers of citizens of this settlement. */
	private int numCitizens;
	/** Numbers of bots owned by this settlement. */
	private int numOwnedBots;
	/** Numbers of vehicles owned by this settlement. */
	private int numOwnedVehicles;
	/** The composite value of the minerals nearby. */
	private int mineralValue = -1;
	/** The background map image id used by this settlement. */
	private int mapImageID;
	
	/** The extra search radius [in km] to be added. */
	private double extraKM;
	/** The average areothermal potential at this location. */
	private double areothermalPotential = 0;
	/** The average regolith collection rate at this location. */
	private double regolithCollectionRate = RandomUtil.getRandomDouble(4, 8);
	/** The average collection rate of the water ice at this location. */
	private double iceCollectionRate = RandomUtil.getRandomDouble(0.2, 1);
	/** The rate [kg per millisol] of filtering grey water for irrigating the crop. */
	private double greyWaterFilteringRate = 1;
	/** The currently minimum passing score for mission approval. */
	private double minimumPassingScore = INITIAL_MISSION_PASSING_SCORE;
	/** The settlement's current indoor temperature. */
	private double currentTemperature = 22.5;
	/** The settlement's current indoor pressure [in kPa], not Pascal. */
	private double currentPressure = NORMAL_AIR_PRESSURE;
	/** The settlement's current meal replenishment rate. */
	private double mealsReplenishmentRate = 0.3;
	/** The settlement's current dessert replenishment rate. */
	private double dessertsReplenishmentRate = 0.4;
	/** The settlement's current probability value for ice. */
	private double iceProbabilityValue = 400D;
	/** The settlement's current probability value for regolith. */
	private double regolithProbabilityValue = 400D;
	/** The settlement's outside temperature. */
	private double outsideTemperature;
	/** Total Crop area */
	private double cropArea = -1;

	/** The settlement terrain profile. */
	private double[] terrainProfile = new double[2];
	/** The settlement template name. */
	private String stormMsg;
	/** The settlement template name. */
	private String template;
	/** The settlement code. */
	private String settlementCode;
	
	/** The radiation status instance that capture if the settlement has been exposed to a radiation event. */
	private RadiationStatus exposed = RadiationStatus.calculateChance(0D);
	/** The settlement's ReportingAuthority instance. */
	private Authority sponsor;
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
	/** Manages the shifts */
	private ShiftManager shiftManager;
	private SettlementTaskManager taskManager;
	private ScheduledEventManager futureEvents;
	
	/** The settlement objective type instance. */
	private ObjectiveType objectiveType;

	/** The settlement's water consumption in kitchen when preparing/cleaning meal and dessert. */
	private SolMetricDataLogger<WaterUseType> waterConsumption = new SolMetricDataLogger<>(MAX_NUM_SOLS);
	/** The settlement's daily output (resources produced). */
	private SolMetricDataLogger<Integer> dailyResourceOutput = new SolMetricDataLogger<>(MAX_NUM_SOLS);
	/** The settlement's daily labor hours output. */
	private SolMetricDataLogger<Integer> dailyLaborTime = new SolMetricDataLogger<>(MAX_NUM_SOLS);
	
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

	/** The set of available pressurized/pressurizing airlocks. */
	private Set<Integer> pressurizedAirlocks = new HashSet<>();
	/** The set of available depressurized/depressurizing airlocks. */
	private Set<Integer> depressurizedAirlocks = new HashSet<>();
	/** The settlement's list of citizens. */
	private Set<Person> citizens;
	/** The settlement's list of owned robots. */
	private Set<Robot> ownedRobots;
	/** The settlement's list of owned vehicles. */
	private Set<Vehicle> ownedVehicles;
	/** The settlement's list of parked vehicles. */
	private Set<Vehicle> vicinityParkedVehicles;
	/** The list of people currently within the settlement. */
	private Set<Person> indoorPeople;
	/** The settlement's list of robots within. */
	private Set<Robot> robotsWithin;
	/** The settlement's preference map. */
	private ParameterManager preferences = new ParameterManager();
	/** A set of nearby mineral locations. */
	private Map<Coordinates, Double> nearbyMineralLocations = new HashMap<>();
	/** A list of nearby mineral locations. */
	private Set<ExploredLocation> declaredMineralLocations = new HashSet<>();
	/** A history of completed processes. */
	private History<CompletedProcess> processHistory = new History<>(40);
	
	private static SettlementConfig settlementConfig = SimulationConfig.instance().getSettlementConfiguration();
	private static PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
	private static SurfaceFeatures surfaceFeatures;
	private static TerrainElevation terrainElevation;
	
	static {
		waterConsumptionRate = personConfig.getWaterConsumptionRate();
		minimumAirPressure = personConfig.getMinAirPressure();
		lifeSupportValues = settlementConfig.getLifeSupportRequirements();
	}

	/**
	 * Constructor 2 called by MockSettlement for maven testing.
	 *
	 * @param name
	 * @param id
	 * @param location
	 */
	public Settlement(String name, Coordinates location) {
		// Use Structure constructor.
		super(name, location);

		this.settlementCode = createCode(name);
		this.location = location;

		
		citizens = new UnitSet<>();
		ownedRobots = new UnitSet<>();
		ownedVehicles = new UnitSet<>();
		vicinityParkedVehicles = new UnitSet<>();
		indoorPeople = new UnitSet<>();
		robotsWithin = new UnitSet<>();

		final double GEN_MAX = 1_000_000;
		
		// Create equipment inventory
		eqmInventory = new EquipmentInventory(this, GEN_MAX);
		// Create schedule event manager
		futureEvents = new ScheduledEventManager(masterClock);
		// Create credit manager
		creditManager = new CreditManager(this, unitManager);

		// Mock use the default shifts
		ShiftPattern shifts = settlementConfig.getShiftByPopulation(10);
		shiftManager = new ShiftManager(this, shifts,
										masterClock.getMarsTime().getMillisolInt());

		// Initialize scientific achievement.
		scientificAchievement = new EnumMap<>(ScienceType.class);
	}

	/**
	 * Constructor 3 for creating settlements. Called by UnitManager to create the
	 * initial settlement Called by ArrivingSettlement to create a brand new
	 * settlement.
	 *
	 * @param name
	 * @param template
	 * @param sponsor
	 * @param location
	 * @param populationNumber
	 * @param initialNumOfRobots
	 */
	private Settlement(String name, String template, Authority sponsor, Coordinates location, int populationNumber,
			int initialNumOfRobots) {
		// Use Structure constructor
		super(name, location);

		this.settlementCode = createCode(name);
		this.location = location;
		this.template = template;
		this.initialNumOfRobots = initialNumOfRobots;
		this.initialPopulation = populationNumber;
		this.sponsor = sponsor;

		this.mapImageID = RandomUtil.getRandomInt(NUM_BACKGROUND_IMAGES - 1) + 1;
				
		citizens = new UnitSet<>();
		ownedRobots = new UnitSet<>();
		ownedVehicles = new UnitSet<>();
		vicinityParkedVehicles = new UnitSet<>();
		indoorPeople = new UnitSet<>();
		robotsWithin = new UnitSet<>();
		allowTradeMissionSettlements = new HashMap<>();
		
		logger.info(name + " (" + settlementCode + ")");
		
		preferences.resetValues(sponsor.getPreferences());

		// Do mission limits; all have a limit of 1 first
		preferences.putValue(MissionLimitParameters.INSTANCE,
							 MissionType.CONSTRUCTION.name(), 1);

		// Call weather to add this location
		weather.addLocation(location);
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
	public static Settlement createNewSettlement(String name, String template, Authority sponsor,
			Coordinates location, int populationNumber, int initialNumOfRobots) {
		return new Settlement(name, template, sponsor, location, populationNumber, initialNumOfRobots);
	}

	/**
	 * Initializes field data, class and maps.
	 */
	public static void initializeStatics() {
		if (surfaceFeatures == null) 
			surfaceFeatures = Simulation.instance().getSurfaceFeatures();
		if (terrainElevation == null) 
			terrainElevation = surfaceFeatures.getTerrainElevation();
	}
	
	/**
	 * Initializes field data, class and maps.
	 */
	public void initializeDataNInstances() {
		// Get the elevation and terrain gradient factor
		terrainProfile = terrainElevation.getTerrainProfile(location);

		iceCollectionRate = iceCollectionRate + terrainElevation.obtainIceCollectionRate(location);
		regolithCollectionRate = regolithCollectionRate + terrainElevation.obtainRegolithCollectionRate(location);

		logger.config(this, "Ice Collection Rate: " + Math.round(iceCollectionRate * 100.0)/100.0 + ".");
		logger.config(this, "Regolith Collection Rate: " + Math.round(regolithCollectionRate * 100.0)/100.0 + ".");
		
		// Create local/nearby mineral locations
		surfaceFeatures.getMineralMap().createLocalConcentration(location);
		
		areothermalPotential = surfaceFeatures.getAreothermalPotential(location);
		
		logger.config(this, "Areothermal Potential: " + Math.round(areothermalPotential * 100.0)/100.0 + " %.");
		
		final double GEN_MAX = 1_000_000;
		// Create EquipmentInventory instance
		eqmInventory = new EquipmentInventory(this, GEN_MAX);

		final double INITIAL_FREE_OXYGEN = 5_000;
		// Stores limited amount of oxygen in this settlement
		storeAmountResource(ResourceUtil.oxygenID, INITIAL_FREE_OXYGEN);

		SettlementTemplate sTemplate = settlementConfig.getItem(template);

		// Initialize building manager
		buildingManager = new BuildingManager(this, sTemplate.getBuildings());
		
		buildingManager.initializeFunctionsNMeteorite();
		
		// Initialize building connector manager.
		buildingConnectorManager = new BuildingConnectorManager(this, sTemplate.getBuildings());

		// Create adjacent building map
		buildingManager.createAdjacentBuildingMap();
		
		// Initialize schedule event manager
		futureEvents = new ScheduledEventManager(masterClock);


		shiftManager = new ShiftManager(this, sTemplate.getShiftDefinition(),
										 masterClock.getMarsTime().getMillisolInt());

		// Initialize Credit Manager.
		creditManager = new CreditManager(this);
		
		// Initialize goods manager.
		goodsManager = new GoodsManager(this);

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
		setObjective(sTemplate.getObjective(), 2);

		// initialize the missionScores list
		missionScores = new ArrayList<>();
		missionScores.add(INITIAL_MISSION_PASSING_SCORE);

		// Create meetings
		var meetings = sTemplate.getActivitySchedule();
		if (meetings != null) {
			for(var ga : meetings.meetings()) {
				new GroupActivity(ga, this, masterClock.getMarsTime());
			}
		}
	}

	/**
	 * Gets the 2-letter settlement code.
	 * 
	 * @return
	 */
	public String getSettlementCode() {
		return settlementCode;
	}
	
	/**
	 * Creates a code name for this settlement.
	 * 
	 * @return
	 */
	private String createCode(String name) {
		// In theory this settlement should not be in the existing Settlements but be safe
		var existingCodes = unitManager.getSettlements().stream()
								.filter(s -> !s.equals(this))
								.map(Settlement::getSettlementCode)
								.collect(Collectors.toSet());
		
		// First strategy use the words
		char [] letters = new char[2];
		letters[0] = name.charAt(0);
		String[] words = name.split(" ");
		for (int secondWord = 1; secondWord < words.length; secondWord++) {
			letters[1]  = words[secondWord].charAt(0);
			String newCode = new String(letters);
			newCode = newCode.toUpperCase();
			if (!existingCodes.contains(newCode)) {
				return newCode;
			}
		}

		// Second Strategy is based on any letter in the name
		String filteredName = name.replaceAll("[^A-Za-z]+", "");
		for (int secondIdx = 1; secondIdx < filteredName.length(); secondIdx++) {
			letters[1] = filteredName.charAt(secondIdx);
			String newCode = new String(letters);
			newCode = newCode.toUpperCase();
			if (!existingCodes.contains(newCode)) {
				return newCode;
			}
		}

		int size = unitManager.getSettlements().size() + 1;
		return String.format("%02d", size);
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
	public Authority getReportingAuthority() {
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
	 * Ends all the indoor tasks people are doing.
	 */
	public void endAllIndoorTasks() {
		for (Person p : getIndoorPeople()) {
			logger.info(p, 4_000, "Ended the current indoor task at "
						+  p.getPosition() + ".");
			p.getMind().getTaskManager().clearAllTasks("Stop indoor tasks");
		}
	}

	/**
	 * Gets a collection of people who are doing EVA outside the settlement.
	 *
	 * @return Collection of people
	 */
	public Collection<Person> getOutsideEVAPeople() {
		return citizens.stream()
				.filter(p -> !p.isDeclaredDead()
						&& (p.getLocationStateType() == LocationStateType.SETTLEMENT_VICINITY
						|| p.getLocationStateType() == LocationStateType.MARS_SURFACE))
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
		Set<Building> bs = buildingManager.getBuildingSet(FunctionType.ROBOTIC_STATION);
		for (Building b : bs) {
			stations += b.getRoboticStation().getOccupantCapacity();
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
	 * Gets a collection of the number of robots of a particular type.
	 *
	 * @return Collection of robots
	 */
	public Collection<Robot> getRobots(RobotType type) {
		// using java 8 stream
		return getAllAssociatedRobots().stream()
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
			if (p > PhysicalCondition.MAXIMUM_AIR_PRESSURE || p < Settlement.minimumAirPressure) {
				logger.warning(this, "Out-of-range overall air pressure at " + Math.round(p * 10D) / 10D + " kPa detected.");
				return false;
			}

			double t = currentTemperature;
			if (t < lifeSupportValues[0][4] - SAFE_TEMPERATURE_RANGE
					|| t > lifeSupportValues[1][4] + SAFE_TEMPERATURE_RANGE) {
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
		Set<Building> buildings = buildingManager.getBuildingSet(FunctionType.LIFE_SUPPORT);
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

		outsideTemperature = weather.getTemperature(location);
	}

	/**
	 * Settlement is a top level entity so this return null
	 */
	@Override
	public String getContext() {
		return null;
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

		// Run at the start of the sim once only
		if (sol == 1 && justLoaded) {
			// Reset justLoaded
			justLoaded = false;

			iceProbabilityValue = computeIceProbability();

			regolithProbabilityValue = computeRegolithProbability();

			// Initialize the goods manager
			goodsManager.updateGoodValues();
		}
		
		
		// Calls other time passings
		futureEvents.timePassing(pulse);
		powerGrid.timePassing(pulse);
		thermalSystem.timePassing(pulse);
		buildingManager.timePassing(pulse);
		taskManager.timePassing();

		// Update citizens
		timePassingCitizens(pulse);

		// Update vehicles
		timePassing(pulse, ownedVehicles);
		
		// Update robots
		timePassing(pulse, ownedRobots);
	
		if (pulse.isNewHalfSol()) {
			// Reset the flag for water ratio review
			setReviewWaterRatio(false);
		}

	
		if (sol > 1 && pulse.isNewSol()) {

			// Perform the end of day tasks
			performBeginningOfDayTasks();	

			// Report mineral site improvement
			reportsMineralSiteImprovement(sol);	
		}

		// Keeps track of things based on msol
		trackByMSol(pulse);

		return true;
	}
	
	/**
	 * Determines the first exploration site.
	 * 
	 * @param limit
	 * @param areologySkill
	 * @return
	 */
	public Coordinates determineFirstSiteCoordinate(double limit, int areologySkill) {
		return getARandomNearbyMineralLocation(true, limit, areologySkill);
	}
	
	/**
	 * Checks and reports on the average mineral site improvement made.
	 * 
	 * @param sol
	 */
	private void reportsMineralSiteImprovement(int sol) {
    	/**
    	 * DO NOT DELETE.
    	 *
    	 * A note on benchmark: This mineral map improvement method takes between 2 and 5 ms to complete
    	 * Debug the real time elapsed [in milliseconds] long tnow = System.currentTimeMillis();
    	 */
		
		int improved = 0;
		
		List<ExploredLocation> siteList = surfaceFeatures
    			.getAllPossibleRegionOfInterestLocations().stream()
    			.filter(site -> site.isMinable())
    			.collect(Collectors.toList());  	
    	
    	for (ExploredLocation el: siteList) {   	
    		 int est = el.getNumEstimationImprovement();
    		 improved += est;
    	}
    	    	
    	int size = siteList.size();
    	
    	if (size > 0 && improved > 0) {
	    	double result = 1.0 * improved / size;
			logger.info(this, "Sol " + sol + ". Average improvement score on " + size 
					+ " mineral location(s): " + Math.round(result * 10.0)/10.0);
    	}
    	else if (size > 0) {
			logger.info(this, "Sol " + sol + ". No improvement on " + size 
					+ " mineral locations.");
    	}
    	
    	/**
    	 * DO NOT DELETE. 
    	 * Debug the real time elapsed [in milliseconds]
    	 *
    	 * tLast = System.currentTimeMillis();
    	 * long elapsedMS = tLast - tnow;
    	 * if (elapsedMS > 1)
    	 *	logger.severe(this, "checkMineralMapImprovement() elapsedMS: " + elapsedMS);
    	 */
	}
	
	/**
	 * Gets the stress factor due to occupancy density.
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
		if (pulse.isNewIntMillisol() && msol >= 10 && msol < 995) {

			// Computes the average air pressure & temperature of the life support system.
			computeEnvironmentalAverages();
			
			// Tag available airlocks into two categories
			checkAvailableAirlocks();

			int remainder = msol % CHECK_MISSION;
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

			// Check every RADIATION_CHECK_FREQ (in millisols)
			// Compute whether a baseline, GCR, or SEP event has occurred
			remainder = msol % RadiationExposure.RADIATION_CHECK_FREQ;
			if (remainder == RadiationExposure.RADIATION_CHECK_FREQ - 1) {
				RadiationStatus newExposed = RadiationStatus.calculateChance(pulse.getElapsed());
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
	 * Applies a clock pulse to a list of Temporal objects. This traps exceptions
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
	 * Passes a pulse to citizens that are not dead. Those that are buried are removed.
	 * 
	 * @param pulse
	 */
	private void timePassingCitizens(ClockPulse pulse) {
		List<Person> remove = null;
		for (Person p : citizens) {
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
			for (Person r : remove) {
				removeACitizen(r);
			}
		}
	}
	


	/**
	 * Samples all the critical resources for stats.
	 *
	 * @param now
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
	 * Gathers yestersol's statistics for the critical resources.
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
	 * Gets the average amount of a critical resource on a sol.
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

		// Load data from file
		return average;
	}

	/**
	 * Provides the daily reports for the settlement.
	 */
	private void performBeginningOfDayTasks() {

		Walk.removeAllReservations(buildingManager);

		JobUtil.tuneJobDeficit(this);

		refreshResourceStat();
		// refresh yesterday sleep map
		refreshSleepMap();

		// Decrease the Mission score.
		minimumPassingScore *= 0.9D;

		// Check the Grey water situation
		if (getAmountResourceStored(GREY_WATER_ID) < GREY_WATER_THRESHOLD) {
			// Adjust the grey water filtering rate
			changeGreyWaterFilteringRate(false);
			double r = getGreyWaterFilteringRate();
			logger.log(this, Level.WARNING, 10_000,
					"Low storage of grey water decreases filtering rate to " + Math.round(r*100.0)/100.0 + ".");
		}
		else if (getAmountResourceRemainingCapacity(GREY_WATER_ID) < GREY_WATER_THRESHOLD) {
			// Adjust the grey water filtering rate
			changeGreyWaterFilteringRate(true);
			double r = getGreyWaterFilteringRate();
			logger.log(this, Level.WARNING, 10_000,
					   "Low capacity for grey water increases filtering rate to " + Math.round(r*100.0)/100.0 + ".");
		}
	}

	/**
	 * Refresh the resource statistics map.
	 */
	private void refreshResourceStat() {
		if (resourceStat == null)
			resourceStat = new HashMap<>();
		// Remove the resourceStat map data from 12 sols ago
		if (resourceStat.size() > RESOURCE_STAT_SOLS)
			resourceStat.remove(0);
	}

	/**
	 * Refreshes the sleep map for each person in the settlement.
	 */
	private void refreshSleepMap() {
		Collection<Person> people = getAllAssociatedPeople();
		for (Person p : people) {
			p.getCircadianClock().inflateSleepHabit();
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
	 * Checks for available airlocks.
	 * 
	 * @param buildingManager
	 */
	public void checkAvailableAirlocks() {
		Set<Building> pressurizedBldgs = new UnitSet<>();
		Set<Building> depressurizedBldgs = new UnitSet<>();

		for (Building airlockBdg : buildingManager.getBuildingSet(FunctionType.EVA)) {
			Airlock airlock = airlockBdg.getEVA().getAirlock();
			if (airlock.isPressurized()	|| airlock.isPressurizing())
				pressurizedBldgs.add(airlockBdg);
			else if (airlock.isDepressurized() || airlock.isDepressurizing())
				depressurizedBldgs.add(airlockBdg);
		}

		if (!pressurizedBldgs.isEmpty()) {
			trackAirlocks(pressurizedBldgs, true);
		}

		if (!depressurizedBldgs.isEmpty()) {
			trackAirlocks(depressurizedBldgs, false);
		}
	}
	
	/**
	 * Is there an airlock available ? 
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
			bldgs = depressurizedAirlocks;
		else
			bldgs = pressurizedAirlocks;
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
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestWalkableEgressAirlock(Worker worker, LocalPosition pos) {
		Building currentBuilding = BuildingManager.getBuilding(worker);

		if (currentBuilding == null) {
			// Note: What if a person is out there in ERV building for maintenance ?
			// ERV building has no LifeSupport function. currentBuilding will be null
			logger.log(worker, Level.WARNING, 10_000, "Not currently in a building.");
			return null;
		}

		return getAirlock(currentBuilding, pos, false);
	}

	/**
	 * Gets the closest ingress airlock for a person.
	 *
	 * @param person the person.
	 * @return airlock or null if none available.
	 */
	public Airlock getClosestIngressAirlock(Person person) {
		Airlock result = null;

		result = getAvailableAirlock(person, depressurizedAirlocks);
		
		if (result == null) {
			result = getAvailableAirlock(person, pressurizedAirlocks);
		}
		
		return result;
	}
	
	/**
	 * Gets an available airlock to a person.
	 *  
	 * @param person
	 * @param bldgs
	 * @return
	 */
	private Airlock getAvailableAirlock(Person person, Set<Integer> bldgs) {

		Airlock result = null;
		double leastDistance = Double.MAX_VALUE;

		Iterator<Integer> i = bldgs.iterator();
		while (i.hasNext()) {
			Building nextBuilding = unitManager.getBuildingByID(i.next());
			Airlock airlock = nextBuilding.getEVA().getAirlock();
		
			boolean chamberFull = nextBuilding.getEVA().getAirlock().areAll4ChambersFull();

			if (!ASTRONOMY_OBSERVATORY.equalsIgnoreCase(nextBuilding.getBuildingType())) {

				double distance = nextBuilding.getPosition().getDistanceTo(person.getPosition());
				
				if (result == null) {
					result = airlock;
					leastDistance = distance;
					continue;
				}
				
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
	 * Gets an airlock for an EVA ingress or egress.
	 * Considers if the chambers are full and if the reservation is full.
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
			bldgs = depressurizedAirlocks;
		}
		else {
			bldgs = pressurizedAirlocks;
		}
		Iterator<Integer> i = bldgs.iterator();
		while (i.hasNext()) {
			Building building = unitManager.getBuildingByID(i.next());
			Airlock airlock = building.getEVA().getAirlock();
			boolean chamberFull = airlock.areAll4ChambersFull();

			// Select airlock that fulfill either conditions:
			// 1. Chambers are NOT full
			// 2. Chambers are full but the reservation is NOT full
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
				if (pressurized
					&& !pressurizedAirlocks.contains(id)) {
						pressurizedAirlocks.add(id);
				}
				else if (!depressurizedAirlocks.contains(id)) {
					depressurizedAirlocks.add(id);
				}
			}
			else {
				if (pressurized
					&& pressurizedAirlocks.contains(id)) {
						pressurizedAirlocks.remove(id);
				}
				else if (!depressurizedAirlocks.contains(id)) {
					depressurizedAirlocks.add(id);
				}
			}
		}
	}

	/**
	 * Gets the best available airlock at the settlement to the given location.
	 * The airlock must have a valid walkable interior path from the given
	 * building's current location.
	 *
	 * @param building  the building in the walkable interior path.
	 * @param location  Starting position.
	 * @param isIngress is airlock in ingress mode ?
	 * @return airlock or null if none available.
	 */
	public Airlock getBestWalkableAvailableAirlock(Building building, LocalPosition location, 
			boolean isIngres) {

		double leastDistance = Double.MAX_VALUE;
		double leastPeople = 4;
		double leastInnerDoor = 4;
		double leastOuterDoor = 4;
		Map<Airlock, Integer> airlockMap = new HashMap<>();
		
		List<Building> airlocks = buildingManager.getBuildings(FunctionType.EVA);
		Collections.sort(airlocks);
		
		if (airlocks.isEmpty())
			return null;
		
		Iterator<Building> i = airlocks.iterator();
		while (i.hasNext()) {
			Building nextBuilding = i.next();
			Airlock airlock = nextBuilding.getEVA().getAirlock();
			
			boolean chamberFull = airlock.areAll4ChambersFull();
			if (chamberFull
				|| !buildingConnectorManager.hasValidPath(building, nextBuilding)) {
				// This will eliminate airlocks that are not in the same zone
				continue;
			}
			
			AirlockMode airlockMode = airlock.getAirlockMode();
			boolean isIngressMode = airlockMode == AirlockMode.INGRESS;
			boolean isEgressMode = airlockMode == AirlockMode.EGRESS;
			// May also consider boolean notInUse = airlockMode == AirlockMode.NOT_IN_USE;
			
			int numInnerDoor = airlock.getNumAwaitingInnerDoor();
			int numOuterDoor = airlock.getNumAwaitingOuterDoor();
			int numOccupants = airlock.getNumOccupants();
			int numEmpty = airlock.getNumEmptied();
			
			// Select an airlock that fulfill these conditions:
			// 
			// 1. Chambers are NOT full
			// 2. Least number of occupants in chambers
			// 3. Least number waiting at outer door
			// 4. Least number waiting at inner door
			// 5. Least distance

			// Note: the use of reservationFull are being put on hold
			// since it creates excessive logs. Thus it needs to be handled differently 

			airlockMap.put(airlock, 1);

			// Note that the airlock can be not in use
			if (isIngressMode == isIngres
				|| isEgressMode != isIngres
					) {
				airlockMap.put(airlock, 2 + airlockMap.get(airlock));
			}
			
			double distance = nextBuilding.getPosition().getDistanceTo(location);
			if (distance <= leastDistance) {
				leastDistance = distance;
				airlockMap.put(airlock, 1 + airlockMap.get(airlock));
			}
			
			if (numOccupants <= leastPeople) {
				leastPeople = numOccupants;
				airlockMap.put(airlock, 1 + airlockMap.get(airlock));
			}
			
			airlockMap.put(airlock, numEmpty + airlockMap.get(airlock));
					
			if (isIngres) {
				// If the person is coming in
				if (numOuterDoor <= leastOuterDoor) {
					leastOuterDoor = numOuterDoor;
					airlockMap.put(airlock, 1 + (4 - numOuterDoor) + airlockMap.get(airlock));
				}
				
				if (numInnerDoor <= leastInnerDoor) {
					leastInnerDoor = numInnerDoor;
					airlockMap.put(airlock, 1 + airlockMap.get(airlock));
				}
			}
			else {
				// If the person is leaving
				if (numOuterDoor <= leastOuterDoor) {
					leastOuterDoor = numOuterDoor;
					airlockMap.put(airlock, 1 + airlockMap.get(airlock));
				}
				
				if (numInnerDoor <= leastInnerDoor) {
					leastInnerDoor = numInnerDoor;
					airlockMap.put(airlock, 1 + (4 - numInnerDoor) + airlockMap.get(airlock));
				}
			}
		}

		if (airlockMap.isEmpty())
			return null;
		
		return selectBestScoreAirlock(airlockMap);
	}
	
	/**
	 * Selects the airlock with the highest score.
	 * 
	 * @param map
	 * @return
	 */
	public Airlock selectBestScoreAirlock(Map<Airlock, Integer> map) {
		return Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getKey();
	}
	
	/**
	 * Is there a closest available airlock at the settlement to the given location ?
	 * The airlock must have a valid walkable interior path from the given
	 * building's current location.
	 *
	 * @param building  the building in the walkable interior path.
	 * @param location  Starting position.
	 * @param isIngress is airlock in ingress mode ?
	 * @return airlock or null if none available.
	 */
	public boolean hasClosestWalkableAvailableAirlock(Building building, LocalPosition location) {
		Iterator<Building> i = buildingManager.getBuildingSet(FunctionType.EVA).iterator();
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
		return buildingManager.getBuildingSet(FunctionType.EVA).size();
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
	 * Returns a collection of people buried outside this settlement.
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
		return indoorPeople.contains(p);
	}

	/**
	 * Makes this person's physical location to be inside this settlement.
	 * Note: a visitor needs to be added.
	 *
	 * @param p the person
	 * @return true if added successfully
	 */
	public boolean addToIndoor(Person p) {
		if (indoorPeople.contains(p)) {
			return true;
		}
		return indoorPeople.add(p);
	}
	

	/**
	 * Removes this person's physical location from being inside this settlement.
 	 * Note: they can be just the visitors and don't need to be the citizen.
	 *
	 * @param p the person
	 * @return true if removed successfully
	 */
	public boolean removePeopleWithin(Person p) {
		if (!indoorPeople.contains(p)) {
			return true;
		}
		return indoorPeople.remove(p);
	}

	/**
	 * Gets a collection of the people who are currently inside the settlement.
	 *
	 * @return Collection of people within
	 */
	public Collection<Person> getIndoorPeople() {
		return indoorPeople;
	}

	/**
	 * Gets the current number of people who are inside the settlement.
	 *
	 * @return the number indoor
	 */
	public int getIndoorPeopleCount() {
		return indoorPeople.size();
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

			// Set x and y coordinates first prior to adding the person 
			p.setCoordinates(getCoordinates());
				
			// Transfer the person to this settlement
			p.transfer(this);
						
			// Add this person indoor map of the settlement
			addToIndoor(p);
			
			// Add to a random building
			BuildingManager.landOnRandomBuilding(p, getAssociatedSettlement());
			
			// Assign a permanent bed reservation if possible
			LivingAccommodation.allocateBed(this, p, true);

			// Update the numCtizens
			numCitizens = citizens.size();

			// Update mission limit dependent upon population
			setMissionLimit(MissionLimitParameters.TOTAL_MISSIONS, 1, 5);
			setMissionLimit(MissionType.MINING.name(), 0, 8);
			setMissionLimit(MissionType.COLLECT_ICE.name(), 1, 5);
			setMissionLimit(MissionType.COLLECT_REGOLITH.name(), 1, 5);
			setMissionLimit(MissionType.EXPLORATION.name(), 1, 5);
			setMissionLimit(MissionType.AREOLOGY.name(), 1, 6);
			setMissionLimit(MissionType.BIOLOGY.name(), 1, 6);
			setMissionLimit(MissionType.METEOROLOGY.name(), 1, 6);
			setMissionLimit(MissionType.TRADE.name(), 0, 10);
			setMissionLimit(MissionType.TRAVEL_TO_SETTLEMENT.name(), 0, 20);
			setMissionLimit(MissionType.DELIVERY.name(), 0, 6);

			// EVA capacity
			int evaCapacity = (int)Math.ceil(numCitizens * EVA_PERCENTAGE);
			preferences.putValue(SettlementParameters.INSTANCE, SettlementParameters.MAX_EVA, evaCapacity);

			// Fire unit update
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, this);
			
			return true;
		}
		return false;
	}

	/**
	 * Calculates the mission limit parameter based on the population and person ratio.
	 * 
	 * @param id Id of the parameter value
	 * @param minMissions Minimum numebr of missions
	 * @param personRatio Ratio of person to mission
	 */
	private void setMissionLimit(String id, int minMissions, int personRatio) {
		int optimalMissions = Math.max(minMissions, (numCitizens/personRatio));
		preferences.putValue(MissionLimitParameters.INSTANCE, id, optimalMissions);
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
			// Fire unit update
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
			
			// Set x and y coordinates first prior to adding the robot 
			r.setCoordinates(getCoordinates());	
			
			// Transfer the robot to this settlement
			r.transfer(this);
			
			// Add the robot to the settlement
			addRobotsWithin(r);
			
			// Update the numOwnedBots
			numOwnedBots = ownedRobots.size();
			// Fire unit update
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT, this);

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
			// Update the numOwnedBots
			numOwnedBots = ownedRobots.size();
			// Fire unit update
			fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT, this);
			
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
		return robotsWithin.add(r);
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
		return robotsWithin.remove(r);
	}

	/**
	 * Adds a vicinity parked vehicle.
	 *
	 * @param vehicle
	 * @param true if the vicinity parked vehicle can be added
	 */
	public boolean addVicinityVehicle(Vehicle vehicle) {
		if (vicinityParkedVehicles.contains(vehicle)) {
			return true;
		}
		if (vicinityParkedVehicles.add(vehicle)) {
			// Directly update the location state type
			vehicle.updateLocationStateType(LocationStateType.SETTLEMENT_VICINITY);
			
			return true;
		}
		return false;
	}

	/**
	 * Removes a vicinity parked vehicle.
	 *
	 * @param vehicle
	 * @param true if the vicinity parked vehicle can be removed
	 */
	public boolean removeVicinityParkedVehicle(Vehicle vehicle) {
		if (!vicinityParkedVehicles.contains(vehicle))
			return true;
		return vicinityParkedVehicles.remove(vehicle);
	}

	/**
	 * Does it have this vicinity vehicle parked at the settlement ?
	 *
	 * @param vehicle
	 * @return
	 */
	public boolean containsVicinityParkedVehicle(Vehicle vehicle) {
		return vicinityParkedVehicles.contains(vehicle);
	}
	
	/**
	 * Adds a vehicle into ownership.
	 *
	 * @param vehicle
	 * @param true if the vehicle can be added
	 */
	public boolean addOwnedVehicle(Vehicle vehicle) {
		if (ownedVehicles.contains(vehicle))
			return true;
		if (ownedVehicles.add(vehicle)) {
			
			// Add this vehicle as parked
			addVicinityVehicle(vehicle);
			// Set vehicle's coordinates to that of settlement
			vehicle.setCoordinates(getCoordinates());
			// Transfer to this settlement
			vehicle.transfer(this);
			// Call findNewParkingLoc to get a non-collided x and y coordinates
			vehicle.findNewParkingLoc();
			// Update the numOwnedVehicles
			numOwnedVehicles = ownedVehicles.size();

			return true;
		}
		return false;
	}

	/**
	 * Removes a vehicle from ownership.
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
	 * Adds a bin to be owned by the settlement.
	 *
	 * @param bin the bin
	 * @return true if this settlement can carry it
	 */
	@Override
	public boolean addBin(Bin bin) {
		if (eqmInventory.addBin(bin)) {
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_BIN_EVENT, this);
			return true;
		}
		return false;
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
	 * Finds all of the bins of a particular type.
	 *
	 * @return collection of bins or empty collection if none.
	 */
	@Override
	public Collection<Bin> findBinsOfType(BinType binType){
		return eqmInventory.findBinsOfType(binType);
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
	 * Gets all associated vehicles currently already embarked on missions out there
	 * (include vehicles doing building construction/salvage missions in a settlement).
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
	 * Gets numbers vehicles currently on mission.
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
	 * @return Collection of parked or garaged drones
	 */
	public Collection<Drone> getParkedGaragedDrones() {
		return ownedVehicles.stream()
				.filter(v -> VehicleType.isDrone(v.getVehicleType()))
				.filter(v -> this.equals(v.getSettlement()))
				.map(Drone.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the number of drones parked or garaged at the settlement.
	 *
	 * @return parked or garaged drones number
	 */
	public int getNumParkedGaragedDrones() {
		return Math.toIntExact(ownedVehicles
				.stream()
				.filter(v -> VehicleType.isDrone(v.getVehicleType()))
				.filter(v -> this.equals(v.getSettlement()))
				.collect(Collectors.counting()));
	}
	
	/**
	 * Gets a collection of vehicles parked or garaged at the settlement.
	 *
	 * @return Collection of Unit
	 */
	public Collection<Unit> getVehicleTypeUnit(VehicleType vehicleType) {
		return ownedVehicles.stream()
				.filter(v -> v.getVehicleType() == vehicleType)
				.map(Unit.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Finds the number of vehicles of a particular type.
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
	 * Finds the number of parked rovers.
	 *
	 * @return number of parked rovers
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
	 * @return Collection of parked or garaged vehicles
	 */
	public Collection<Vehicle> getParkedGaragedVehicles() {
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
		logger.log(this, Level.CONFIG, 0L, "Player " + (override ? "enables" : "disable")
						+ " the override on '" + type.getName() + "'.");
		preferences.putValue(ProcessParameters.INSTANCE, type.name(), Boolean.valueOf(override));
	}

	/**
	 * Gets the process override flag.
	 *
	 * @param type Name of process type
	 * @return Is this override flag set
	 */
	public boolean getProcessOverride(OverrideType type) {
		return preferences.getBooleanValue(ProcessParameters.INSTANCE, type.name(), false);
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
	 * Adds achievement credit to the settlement in a scientific field.
	 * Must be synchronized because Scientific Research is cross-Settlement.
	 * 
	 * @param achievementCredit the achievement credit
	 * @param science           the scientific field
	 */
	public synchronized void addScientificAchievement(double achievementCredit, ScienceType science) {
		if (scientificAchievement.containsKey(science))
			achievementCredit += scientificAchievement.get(science);

		scientificAchievement.put(science, achievementCredit);
	}

	/**
	 * Gets the initial population of the settlement.
	 *
	 * @return initial population number
	 */
	public int getInitialPopulation() {
		return initialPopulation;
	}

	/**
	 * Gets the initial number of robots the settlement.
	 *
	 * @return initial number of robots
	 */
	public int getInitialNumOfRobots() {
		return initialNumOfRobots;
	}

	/**
	 * Returns the chain of command.
	 *
	 * @return chainOfCommand
	 */
	public ChainOfCommand getChainOfCommand() {
		return chainOfCommand;
	}

	/**
	 * Gets the shift manager for this Settlement.
	 */
	public ShiftManager getShiftManager() {
		return shiftManager;
	}

	/**
	 * Gets the radiation status.
	 * 
	 * @return
	 */
	public RadiationStatus getExposed() {
		return exposed;
	}

	/*
	 * Updates the status of Radiation exposure.
	 * 
	 * @param newExposed
	 */
	public void setExposed(RadiationStatus newExposed) {
		exposed = newExposed;
		
		if (exposed.isBaselineEvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR + UnitEventType.BASELINE_EVENT.toString() + IMMINENT);
			this.fireUnitUpdate(UnitEventType.BASELINE_EVENT);
		}

		if (exposed.isGCREvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR + UnitEventType.GCR_EVENT.toString() + IMMINENT);
			this.fireUnitUpdate(UnitEventType.GCR_EVENT);
		}

		if (exposed.isSEPEvent()) {
			logger.log(this, Level.INFO, 1_000, DETECTOR + UnitEventType.SEP_EVENT.toString() + IMMINENT);
			this.fireUnitUpdate(UnitEventType.SEP_EVENT);
		}
	}

	
	/** 
	 * Gets the new water ratio at the settlement. 
	 */
	public int getNewWaterRatio() {
		return newWaterRatio;
	}
	
	/** 
	 * Gets the current water level at the settlement. 
	 */
	public int getWaterRationLevel() {
		return waterRatioCache;
	}
	
	/**
	 * Returns the difference between the new and old water ration level. 
	 *
	 * @return level of water ration.
	 */
	public int getWaterRatioDiff() {
		return newWaterRatio - waterRatioCache;
	}

	/**
	 * Sets the water ratio.
	 */
	public void setWaterRatio() {
		waterRatioCache = newWaterRatio;
	}
	
	/**
	 * Sets the flag for reviewing water ratio.
	 * 
	 * @param value
	 */
	public void setReviewWaterRatio(boolean value) {
		waterRatioReviewFlag = value;
	}
	
	/**
	 * Returns if the water ratio has been reviewed.
	 * 
	 * @return
	 */
	public boolean canReviewWaterRatio() {
		return waterRatioReviewFlag;
	}
	
	/**
	 * Computes the water ratio at the settlement.
	 *
	 * @return level of water ration.
	 */
	public boolean isWaterRatioChanged() {
		double storedWater = getAmountResourceStored(WATER_ID);
		int reserveWater = getNumCitizens() * MIN_WATER_RESERVE;
		// Assuming a 90-day supply of water
		double requiredWater = waterConsumptionRate * getNumCitizens() * 90;

		int newRatio = Math.max(1, (int)((requiredWater + reserveWater) / storedWater));
		if (newRatio < 1)
			newRatio = 1;
		else if (newRatio > 1)
			logger.info(this, 20_000L, "Calculated Water Ratio: " + newRatio);
		else if (newRatio > 1000)
			newRatio = 1000;

		if (newRatio > 500)
			logger.severe(this, 20_000L, "Unsafe Water Ratio: " + newRatio);

		newWaterRatio = newRatio;
		
		return waterRatioCache != newRatio;
	}

	/**
	 * Sets the objective.
	 *
	 * @param {@link ObjectiveType}
	 * @param level
	 */
	public void setObjective(ObjectiveType objectiveType, int level) {
		this.objectiveType = objectiveType;
		double lvl = 1.25 * level;

		// reset all to 1
		goodsManager.resetCommerceFactors();
		CommerceType cType = ObjectiveUtil.toCommerce(objectiveType);
		if (cType != null) {
			goodsManager.setCommerceFactor(cType, lvl);
		}
	}

	/**
	 * Gets the objective level.
	 *
	 * @param {@link ObjectiveType}
	 * @return the level
	 */
	public double getObjectiveLevel(ObjectiveType objectiveType) {
		CommerceType cType = ObjectiveUtil.toCommerce(objectiveType);
		if (cType == null) {
			return -1;
		}
		return goodsManager.getCommerceFactor(cType);
	}

	/**
	 * Gets the objective.
	 */
	public ObjectiveType getObjective() {
		return objectiveType;
	}

	/**
	 * Gets the total area of Crops in this Settlement.
	 */
	public double getTotalCropArea() {
		if (cropArea < 0D) {
			cropArea = 0D;
			
			for (Building b : buildingManager.getBuildingSet(FunctionType.FARMING)) {
				cropArea += b.getFarming().getGrowingArea();
			}
		}

		return cropArea;
	} 
	
	/**
	 * Computes the probability of the presence of regolith.
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
		return result;
	}

	/**
	 * Computes the probability of the presence of ice.
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
		waterDemand = waterDemand * waterRatioCache / 10;
		if (waterDemand > WATER_MAX)
			waterDemand = WATER_MAX;
		if (waterDemand < 1)
			waterDemand = 1;
		
		double brineWaterDemand = goodsManager.getDemandValueWithID(BRINE_WATER_ID);
		brineWaterDemand = brineWaterDemand * waterRatioCache / 10;
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
		
		if (result > MAX_PROB)
			result = MAX_PROB;
		
		return result;
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
		return outsideTemperature;
	}

	public DustStorm getDustStorm() {
		return storm;
	}

	public void setDustStorm(DustStorm storm) {
		this.storm = storm;
	}
	
	public void setDustStormMsg(String msg) {
		this.stormMsg = msg;
	}

	public String getDustStormMsg() {
		return stormMsg;
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
	 * Records a completed process.
	 * 
	 * @param name Name of the process
	 * @param type Type of process
	 * @param locn On what building it was completed
	 * @param products The outputs produced
	 */
    public void recordProcess(ProcessInfo process, String type, Building locn) {
        var ph = new CompletedProcess(process, type, locn.getName());
		processHistory.add(ph);
    }
	
    /**
     * Returns the process history.
     * 
     * @return
     */
	public History<CompletedProcess> getProcessHistory() {
		return processHistory;
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
	 * Enables or disable a mission type.
	 *  
	 * @param mission
	 * @param disable
	 */
	public void setMissionDisable(MissionType mission, boolean disable) {
		preferences.putValue(MissionWeightParameters.INSTANCE, mission.name(), (disable ? 0D : 1D));
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
		return preferences.getIntValue(MissionLimitParameters.INSTANCE, mission.name(), 0) > 0;
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
				else if (vehicle.getEstimatedRange() < result.getEstimatedRange())
					// This vehicle has a lesser range than the previously selected vehicle
					result = rover;
			}
		}

		return result;
	}
	
	/**
	 * Adds a nearby mineral location in random.
	 * 
	 * @param limit
	 * @param sol
	 * @param a new nearby mineral coordinate
	 */
	public Coordinates acquireNearbyMineralLocation(double limit, int sol) {
		
		Map.Entry<Coordinates, Double> pair = surfaceFeatures.getMineralMap().
				findRandomMineralLocation(getCoordinates(), limit + extraKM, sol, nearbyMineralLocations.keySet());
		
		if (pair == null) {
			logger.warning(this, 30_000, "No nearby mineral locations found within " + Math.round(limit + extraKM) + " km.");
			extraKM = extraKM + 0.3;
			return null;
		}
		
		nearbyMineralLocations.put(pair.getKey(), pair.getValue());
		
		return pair.getKey();
	}
	
	/**
	 * Gets the next closest mineral location.
	 * 
	 * @param limit
	 * @return
	 */
	public Coordinates getNextClosestMineralLoc(double limit) {
		int sol = masterClock.getMarsTime().getMissionSol();
		
		acquireNearbyMineralLocation(limit, sol);
		
		int size =  nearbyMineralLocations.size();
		
		double shortestDist = 1000;
		Coordinates chosen = null;
		
		List<Coordinates> keys = new ArrayList<>(nearbyMineralLocations.keySet());
		for (int i = 0; i < size; i++) {
			Coordinates c = keys.get(i);
			double dist = nearbyMineralLocations.get(c);
			if (surfaceFeatures.isDeclaredARegionOfInterest(c, this, false)) {
				continue;
			}
			if (shortestDist >= dist) {
				shortestDist = dist;
				chosen = c;
			}
		}	
		
		return chosen;
	}
	
	/**
	 * Gets a set of nearby potential mineral locations.
	 * 
	 * @param rover
	 */
	public Set<Coordinates> getNearbyMineralLocations() {
		return nearbyMineralLocations.keySet();
	}
	
	/**
	 * Gets the number of nearby potential mineral location.
	 * 
	 * @return
	 */
	public int numNearbyMineralLocations() {
		return nearbyMineralLocations.size();
	}
	
	
	/**
	 * Gets one of the existing nearby mineral location that has not been declared yet.
	 * 
	 * @param closest
	 * @param limit
	 * @param skill
	 * @return
	 */
	public Coordinates getExistingNearbyMineralLocation() {

		for (Coordinates c : nearbyMineralLocations.keySet()) {
			for (ExploredLocation el : declaredMineralLocations) {
				if (!c.equals(el.getLocation())) {
					return c;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Gets a random nearby mineral location that can be reached by any rover.
	 * 
	 * @param closest is selecting to return one of the closest locations
	 * @param limit
	 * @param skill
	 * @return
	 */
	public Coordinates getARandomNearbyMineralLocation(boolean closest, double limit, int skill) {
		int sol = masterClock.getMarsTime().getMissionSol();
		
		double newRange = limit;
		
		if (limit == -1) {		
			return getNextClosestMineralLoc(limit);
		}
//		else
//			acquireNearbyMineralLocation(getCoordinates(), limit, sol);
		
		logger.info(this, "nearbyMineralLocations: " + nearbyMineralLocations.size());
		
		if (nearbyMineralLocations == null || nearbyMineralLocations.isEmpty()) {
			return null;
		}
		
		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		for (Coordinates c : nearbyMineralLocations.keySet()) {
			for (ExploredLocation el : declaredMineralLocations) {
				if (c.equals(el.getLocation())) {
					// Skip this c
					continue;
				}
			
				double distance = nearbyMineralLocations.get(c);
				double prob = 0;
				double delta = newRange - distance + Math.max(0, 100 - sol);
				if (delta > 0) {
					prob = 0;
					continue;
				}
					
				if (closest) {		
					prob = delta * delta / newRange / newRange;
	
				}
				else {
					prob = delta / newRange;
				}
				
				if (distance >= RandomMineralMap.MIN_DISTANCE && prob > 0) {
					// Fill up the weight map
					weightedMap.put(c, prob);
				}
			}
		}

		// Choose one with weighted randomness 
		Coordinates chosen = RandomUtil.getWeightedRandomObject(weightedMap);

		if (weightedMap.isEmpty() || chosen == null) {
			logger.info(this, 30_000, "No ROIs found within " + Math.round(limit + extraKM) + " km.");
			extraKM = extraKM + 0.3;
			return null;
		}
		
		return chosen;
	}
	
	
	/**
	 * Gets the number of all declared locations (claimed or unclaimed).
	 * 
	 * @param isClaimed
	 * @return
	 */
	public int numAllDeclaredLocation(boolean isClaimed) {
		return (int)surfaceFeatures.numAllDeclaredLocations(this, isClaimed);
	}
	
	/**
	 * Returns the number of locations declared/considered by this settlement as a Region Of Interest (ROI), regardless of its claimed status.
	 * 
	 * @return
	 */
	public int numAllDeclaredLocations() {
		return (int)surfaceFeatures.numAllDeclaredLocations(this);
	}
	
	/**
	 * Returns number of locations being claimed or not being claimed as a Region Of Interest (ROI).
	 * 
	 * @param isClaimed
	 * @return
	 */
	public int numDeclaredLocation(boolean isClaimed) {	
		int num = 0;
		for (Coordinates c: nearbyMineralLocations.keySet()) {
			if (surfaceFeatures.isDeclaredARegionOfInterest(c, this, isClaimed))
				num++;
		}
		return num;
	}
	
	/**
	 * Returns number of locations being claimed or not being claimed as a Region Of Interest (ROI).
	 * 
	 * @return
	 */
	public int numDeclaredLocation() {	
		return declaredMineralLocations.size();
	}
	
	/**
	 * Returns list of declared locations of Region Of Interest (ROI).
	 * 
	 * @return
	 */
	public Set<ExploredLocation> getDeclaredLocations() {	
		return declaredMineralLocations;
	}

	
	/**
	 * Returns average distance of mineral locations being claimed or not being claimed as a Region Of Interest (ROI).
	 * 
	 * @param coord
	 * @param settlement
	 * @param isClaimed
	 * @return
	 */
	public double getMeanMineralLocDistance(boolean isClaimed) {
		double dist = 0;
		int num = 0;
		for (Coordinates c: nearbyMineralLocations.keySet()) {
			if (surfaceFeatures.isDeclaredARegionOfInterest(c, this, isClaimed)) {
				dist += nearbyMineralLocations.get(c);
				num++;
			}
		}
		if (num != 0)
			return dist/num;
		return 0;
	}
	
	/**
	 * Returns the standard deviation of distance of mineral locations being claimed or not being claimed as a Region Of Interest (ROI).
	 * 
	 * @param coord
	 * @param settlement
	 * @param isClaimed
	 * @return
	 */
	public double getStandardDevMineralLocDistance(boolean isClaimed) {
		double powerSum1 = 0;
		double powerSum2 = 0;
		double stdev = 0;
		List<Coordinates> list = new ArrayList<>(nearbyMineralLocations.keySet());
		int size = list.size();
		for (int i=0; i<size; i++) {
			  Coordinates c = list.get(i);
			if (surfaceFeatures.isDeclaredARegionOfInterest(c, this, isClaimed)) {
				double dist = getCoordinates().getDistance(c);
				powerSum1 += dist;
				powerSum2 += Math.pow(dist, 2);
				if (i > 0)
					stdev = Math.sqrt(i*powerSum2 - Math.pow(powerSum1, 2))/i;
			}
		}
		return stdev;
	}

	/**
	 * Computes the mineral sites statistics.
	 * 
	 * @param status; if 0, all sites; if 1, claimed sites; if 2, unclaimed sites
	 * @return
	 */
	public int[] getStatistics(int status) {
		List<Coordinates> keys = new ArrayList<>(nearbyMineralLocations.keySet());
		List<Double> list = new ArrayList<>();
		int size = keys.size();
		for (int i = 0; i < size; i++) {
			Coordinates c = keys.get(i);		
			if (status == 0) {
				list.add(nearbyMineralLocations.get(c));
			}
			else if (status == 1) {
				if (surfaceFeatures.isDeclaredARegionOfInterest(c, this, true)) {
					list.add(nearbyMineralLocations.get(c));
				}
			}
			else if (status == 2) {
				if (surfaceFeatures.isDeclaredARegionOfInterest(c, this, false)) {
					list.add(nearbyMineralLocations.get(c));
				}
			}
		}
		
		double sum = 0.0;
	    double total = 0.0;
	    double mean = 0.0;
	    double sd = 0.0;
	    double variance = 0.0;
	    size = list.size();
	    
		for (int i = 0; i < size; i++) {
            sum += list.get(i);
        }
       
		if (size == 0) {
			// everything is zero
		}
			
		else {
			mean = sum / size;
 
	        for (int i = 0; i < size; i++) {
	            total += Math.pow((list.get(i) - mean), 2);
	        }
	       
	        variance = total / size;
	        sd = Math.sqrt(variance);
		}
		
        return new int[] {(int)mean, (int)sd};
	}
	
	
	/**
	 * Gets a comfortable nearby mineral location, based on skills.
	 * 
	 * @param limit
	 * @param skillDistance
	 */
	public Coordinates getAComfortableNearbyMineralLocation(double limit, int skill) {
		return getARandomNearbyMineralLocation(false, limit, skill);
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
		ExploredLocation el = surfaceFeatures.createARegionOfInterest(siteLocation, this, skill);
		if (el != null) {
			declaredMineralLocations.add(el);
			return el;
		}
		return null;
	}
	
	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 * Note: Called by getTotalMineralValue()
	 *
	 * @param rover          the rover to use.
	 * @param homeSettlement the starting settlement.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	public Map<String, Integer> getNearbyMineral(Rover rover, int skill) {
		int sol = masterClock.getMarsTime().getMissionSol();
				
		Map<String, Integer> minerals = new HashMap<>();

		double roverRange = rover.getEstimatedRange();
		double tripTimeLimit = rover.getTotalTripTimeLimit(true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

//		acquireNearbyMineralLocation(getCoordinates(), range, sol);

		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		for (Coordinates c : nearbyMineralLocations.keySet()) {
			double distance = nearbyMineralLocations.get(c);
			double prob = 0;
			double delta = range - distance + Math.max(0, 100 - sol);
			if (delta > 0) {
				prob = delta * delta / range / range;
			}
			
			if (distance >= RandomMineralMap.MIN_DISTANCE && prob > 0) {
				// Fill up the weight map
				weightedMap.put(c, prob);
			}
		}
		
		// Choose one with weighted randomness 
		Coordinates chosen = RandomUtil.getWeightedRandomObject(weightedMap);

		if (weightedMap.isEmpty() || chosen == null) {
			logger.info(this, 30_000, "No mineral site of interest found within " + Math.round(range + extraKM) + " km.");
			return null;
		}
		
		double chosenDist = weightedMap.get(chosen);
		
		logger.info(unitManager.findSettlement(getCoordinates()), 30_000L, 
				"Investigating a mineral site at " + chosen + " (" + Math.round(chosenDist * 10.0)/10.0 + " km away).");
		
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
	public int getTotalMineralValue(Rover rover, int skill) {
		if (mineralValue == -1) {
			// Check if any mineral locations within rover range and obtain their
			// concentration
			Map<String, Integer> minerals = getNearbyMineral(rover, skill);
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
	 * Returns the areothermal potential in the vicinity of this settlement.
     * 
     * @return
     */
    public double getAreothermalPotential() {
    	return areothermalPotential;
    }
    
    
	/**
	 * Removes the record of the deceased person from airlock.
	 *
	 * @param person
	 */
	public void removeAirlockRecord(Person person) {
		for (Building b : buildingManager.getBuildingSet(FunctionType.EVA)) {
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
	 * Gets the stored mass.
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
	 * Gets the container set.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getContainerSet() {
		return eqmInventory.getContainerSet();
	}

	/**
	 * Gets the EVA suit set.
	 * 
	 * @return
	 */
	@Override
	public Set<Equipment> getSuitSet() {
		return eqmInventory.getSuitSet();
	}
	
	/**
	 * Gets a set of the container with particular container type.
	 *
	 * @return the equipment list
	 */
	public Set<Equipment> getContainerSet(EquipmentType equipmentType) {
		return eqmInventory.getContainerSet().stream()
				.filter(e -> e.getEquipmentType() == equipmentType)
				.collect(Collectors.toSet());
	}

	/**
	 * Gets a collection of bins with particular bin type.
	 *
	 * @return the bin list
	 */
	public Collection<Bin> getBinTypeSet(BinType binType) {
		for (AmountResourceBin arb: eqmInventory.getAmountResourceBinSet()) {
			if (binType == arb.getBinType()) {
				return arb.getBinMap().values();
			}
		}
		
		return null;
	}

	/**
	 * Gets the number of available EVA suits.
	 * 
	 * @return
	 */
	public int getNumEVASuit() {
		return getSuitSet().size();
//		return Math.toIntExact(getEquipmentTypeSet(EquipmentType.EVA_SUIT)
//				.stream()
//				.collect(Collectors.counting()));
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
	 * Stores the item resource.
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
	 * Retrieves the item resource.
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
	 * Gets the item resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return eqmInventory.getItemResourceStored(resource);
	}

	/**
	 * Stores the amount resource.
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
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return shortfall quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		return eqmInventory.retrieveAmountResource(resource, quantity);
	}

	/**
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource.
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
		return eqmInventory.hasAmountResourceRemainingCapacity(resource);
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
	 * Finds the number of empty containers (from a copy set of containers) of a class that are contained in storage and have
	 * an empty inventory.
	 * 
	 * @param containerType
	 * @param brandNew
	 * @return
	 */
	public int findNumEmptyCopyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return eqmInventory.findNumEmptyCopyContainersOfType(containerType, brandNew);
	}
	
	
	/**
	 * Finds the number of containers of a particular type.
	 *
	 * Note: will not count EVA suits.
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
	 * Note: will not count EVA suits.
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
	 * Finds the number of bins of a particular type.
	 *
	 * @param containerType the bin type.
	 * @return number of empty bins.
	 */
	@Override
	public int findNumBinsOfType(BinType binType) {
		return eqmInventory.findNumBinsOfType(binType);
	}
	
	@Override
	public Set<AmountResourceBin> getAmountResourceBinSet() {
		return eqmInventory.getAmountResourceBinSet();
	}

	@Override
	public double getAmountResourceStored(BinType type, int id, int resource) {
		return eqmInventory.getAmountResourceStored(type, id, resource);
	}

	@Override
	public double storeAmountResource(BinType type, int id, int resource, double quantity) {
		return eqmInventory.storeAmountResource(type, id, resource, quantity);
	}

	@Override
	public double retrieveAmountResource(BinType type, int id, int resource, double quantity) {
		return eqmInventory.retrieveAmountResource(type, id, resource, quantity);
	}

	@Override
	public double getAmountResourceCapacity(BinType type, int id, int resource) {
		return eqmInventory.getAmountResourceCapacity(type, id, resource);
	}

	@Override
	public double getAmountResourceRemainingCapacity(BinType type, int id, int resource) {
		return eqmInventory.getAmountResourceRemainingCapacity(type, id, resource);
	}

	@Override
	public boolean hasAmountResourceRemainingCapacity(BinType type, int id, int resource) {
		return eqmInventory.hasAmountResourceRemainingCapacity(type, id, resource);
	}

	@Override
	public double getCargoCapacity(BinType type, int id) {
		return eqmInventory. getCargoCapacity(type, id);
	}

	@Override
	public int getAmountResource(BinType type, int id) {
		return eqmInventory.getAmountResource(type, id);
	}

	@Override
	public Unit getOwner() {
		return eqmInventory.getOwner();
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
	 * Is this unit inside a settlement ?
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
	 * Gets the preference that this Settlement influences.
	 * 
	 * @return A read only copy of preferences
	 */
	public ParameterManager getPreferences() {
		return preferences;
	}


	/** 
	 * Gets the background map image id used by this settlement. 
	 */
	public int getMapImageID() {
		return mapImageID;
	}
	
	/**
	 * Gets the GroupActivity instances associated with a Settlement.
	 * 
	 * @param justActive Filter to only return active meetings
	 */
    public List<GroupActivity> getGroupActivities(boolean justActive) {
       return getFutureManager().getEvents().stream()
                .filter(e -> e.getHandler() instanceof GroupActivity)
                .map(e -> (GroupActivity)e.getHandler())
                .filter(e -> (!justActive || e.isActive()))
                .toList();
    }
    
	/**
	 * Gets the associated settlement this unit is with.
	 *
	 * @return the associated settlement
	 */
	public Settlement getAssociatedSettlement() {
		return this;
	}
	
	/**
	 * Gets the preset life support value.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public static double getLifeSupportValues(int i, int j) {
		return lifeSupportValues[i][j];
	}
	
	/**
	 * Reinitializes references after loading from a saved sim.
	 */
	public void reinit() {
		if (surfaceFeatures == null) 
			surfaceFeatures = Simulation.instance().getSurfaceFeatures();
		
		if (terrainElevation == null) 
			terrainElevation = surfaceFeatures.getTerrainElevation();
		
		buildingManager.reinit();
	}
	
	/**
	 * Compares if an object is the same as this settlement.
	 *
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Settlement e = (Settlement) obj;
		return this.getName() == e.getName() 
			&& this.getIdentifier() == e.getIdentifier();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		int hashCode = getIdentifier();
		return hashCode % 64;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		for (Person p: citizens) {
			p.destroy();
		}
		citizens.clear();
		citizens = null;
		
		for (Person p: indoorPeople) {
			p.destroy();
		}
		indoorPeople.clear();
		indoorPeople = null;
		
		for (Robot r: ownedRobots) {
			r.destroy();
		}
		ownedRobots.clear();
		ownedRobots = null;
		
		for (Robot r: robotsWithin) {
			r.destroy();
		}
		robotsWithin.clear();
		robotsWithin = null;
		
		for (Vehicle v: ownedVehicles) {
			v.destroy();
		}
		ownedVehicles.clear();
		ownedVehicles = null;
		
		for (Vehicle v: vicinityParkedVehicles) {
			v.destroy();
		}
		vicinityParkedVehicles.clear();
		vicinityParkedVehicles = null;
		
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
		
		scientificAchievement = null;
	}
}
