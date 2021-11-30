/*
 * Mars Simulation Project
 * Building.java
 * @date 2021-08-28
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.ItemHolder;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.connection.InsidePathLocation;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.BuildingConnection;
import org.mars_sim.msp.core.structure.building.function.Communication;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.EarthReturn;
import org.mars_sim.msp.core.structure.building.function.Exercise;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.core.structure.building.function.Heating;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.Management;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.structure.building.function.Recreation;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.WasteDisposal;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.Dining;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Building class is a settlement's building.
 */
public class Building extends Structure implements Malfunctionable, Indoor, // Comparable<Building>,
		LocalBoundedObject, InsidePathLocation, Temporal, Serializable, ResourceHolder, ItemHolder {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Building.class.getName());

	public static final String HALLWAY = "hallway";
	public static final String TUNNEL = "tunnel";
	public static final String ASTRONOMY_OBSERVATORY = "Astronomy Observatory";
	public static final String EVA_AIRLOCK = "EVA Airlock";
	public static final String ERV = "ERV";
	public static final String GREENHOUSE = "Greenhouse";
	public static final String INFLATABLE_GREENHOUSE = "Inflatable " + GREENHOUSE;
	public static final String ARRAY = "Array";
	public static final String TURBINE = "Turbine";
	public static final String WELL = "Well";

	public static final int TISSUE_CAPACITY = 20;

	/** The height of an airlock in meters */
	// Assume an uniform height of 2.5 meters in all buildings
	public static final double HEIGHT = 2.5;
	/** The volume of an airlock in cubic meters */
	public static final double AIRLOCK_VOLUME_IN_CM = BuildingAirlock.AIRLOCK_VOLUME_IN_CM; // 3 * 2 * 2; //in m^3
	/** 500 W heater for use during EVA ingress */
	public static final double kW_EVA_HEATER = .5D; //
	// Assuming 20% chance for each person to witness or be conscious of the
	// meteorite impact in an affected building
	public static final double METEORITE_IMPACT_PROBABILITY_AFFECTED = 20;

	/** A list of functions of this building. */
	protected List<Function> functions;

	// Data members
	boolean isImpactImminent = false;
	/** Checked by getAllImmovableBoundedObjectsAtLocation() in LocalAreaUtil */
	boolean inTransportMode = true;
	/** building id on the building template. */
	private int bid;
	/** Unique template id assigned for the settlement template of this building belong. */
	protected int templateID;
	/** The inhabitable ID for this building. */
	protected int inhabitableID = -1;
	/** The base level for this building. -1 for in-ground, 0 for above-ground. */
	protected int baseLevel;

	/** Default : 22.5 deg celsius. */
	private double initialTemperature = 22.5D;
	protected double width;
	protected double length;
	protected double floorArea;
	private double wallThickness;
	protected LocalPosition loc;
	protected double zLoc;
	protected double facing;
	protected double basePowerRequirement;
	protected double basePowerDownPowerRequirement;
	protected double powerNeededForEVAheater;


	/** Type of building. */
	protected String buildingType;
	/** Nick name for this building. */
	private String nickName;
	/** Description for this building. */
	private String description;

	/** Unique identifier for the settlement of this building. */
	private Integer settlementID;
	/** The settlement of this building. */
	private transient Settlement settlement;

	/** The MalfunctionManager instance. */
	protected MalfunctionManager malfunctionManager;

	private Administration admin;
	private AstronomicalObservation astro;
	private Communication comm;
	private Computation computation;
	private Cooking cooking;
	private Dining dine;
	private EarthReturn earthReturn;
	private EVA eva;
	private Exercise gym;
	private Farming farm;
	private Fishery fish;
	private FoodProduction foodFactory;
	private GroundVehicleMaintenance maint;
	private Heating heating;
	private LivingAccommodations livingAccommodations;
	private LifeSupport lifeSupport;
	private Management management;
	private Manufacture workshop;
	private MedicalCare medical;
	private ThermalGeneration furnace;
	private PowerGeneration powerGen;
	private PowerStorage powerStorage;
	private PreparingDessert preparingDessert;
	private Recreation rec;
	private Research lab;
	private ResourceProcessing processing;
	private RoboticStation roboticStation;
	private Storage storage;
	private WasteDisposal waste;
	private VehicleMaintenance garage;

	protected PowerMode powerModeCache;
	protected HeatMode heatModeCache;

	/**
	 * Constructor 1. Constructs a Building object.
	 *
	 * @param template the building template.
	 * @param manager  the building's building manager.
	 * @throws BuildingException if building can not be created.
	 */
	public Building(BuildingTemplate template, BuildingManager manager) {
		this(template.getID(), template.getBuildingType(), template.getNickName(), template.getBounds(), manager);

		this.bid = template.getID();
//		this.manager = manager;
		buildingType = template.getBuildingType();
		settlement = manager.getSettlement();
		settlementID = settlement.getIdentifier();

		// Set the instance of life support
		// NOTE: needed for setting inhabitable id
		if (hasFunction(FunctionType.LIFE_SUPPORT)) {
			if (lifeSupport == null) {
				lifeSupport = (LifeSupport) getFunction(FunctionType.LIFE_SUPPORT);
				// Set up an inhabitable_building id for tracking composition of air
				int id = manager.obtainNextInhabitableID();
				setInhabitableID(id);
			}
		}
	}

	/**
	 * Constructor 2 Constructs a Building object.
	 *
	 * @param id           the building's unique ID number.
	 * @param buildingType the building Type.
	 * @param nickName     the building's nick name.
	 * @param bounds       the physical position of this Building
	 * @param manager      the building's building manager.
	 * @throws BuildingException if building can not be created.
	 */
	public Building(int id, String buildingType, String nickName, BoundedObject bounds, BuildingManager manager) {
		super(nickName, manager.getSettlement().getCoordinates());

		this.templateID = id;
		this.buildingType = buildingType;
		this.nickName = nickName;

		this.settlement = manager.getSettlement();
		this.settlementID = settlement.getIdentifier();

		this.loc = bounds.getPosition();
		this.facing = bounds.getFacing();

		BuildingSpec spec = SimulationConfig.instance().getBuildingConfiguration().getBuildingSpec(buildingType);

		wallThickness = spec.getWallThickness();
		powerModeCache = PowerMode.FULL_POWER;
		heatModeCache = HeatMode.HALF_HEAT;
		width = spec.getWidth();
		if (width < 0) {
			width = bounds.getWidth();
		}
		length = spec.getLength();
		if (length < 0) {
			length = bounds.getLength();
		}

		floorArea = length * width;
		if (floorArea < 0) {
			throw new IllegalArgumentException("Floor area cannot be -ve w=" + width + ", l=" + length);
		}

		// Sets the base mass
		setBaseMass(spec.getBaseMass());

		baseLevel = spec.getBaseLevel();
		description = spec.getDescription();

		// Get the building's functions
		functions = buildFunctions(spec);

		// Get base power requirements.
		basePowerRequirement = spec.getBasePowerRequirement();
		basePowerDownPowerRequirement = spec.getBasePowerDownPowerRequirement();

		// Set room temperature
		initialTemperature = spec.getRoomTemperature();

		// Determine total maintenance time.
		double totalMaintenanceTime = spec.getMaintenanceTime();
		for (Function mfunction : functions) {
			totalMaintenanceTime += mfunction.getMaintenanceTime();
		}

		// Set up malfunction manager.
		malfunctionManager = new MalfunctionManager(this, spec.getWearLifeTime(), totalMaintenanceTime);
		// Add scope to malfunction manager.
		malfunctionManager.addScopeString(SystemType.BUILDING.getName());

		// Add each function to the malfunction scope.
		for (Function sfunction : functions) {
			String[] scopes = sfunction.getMalfunctionScopeStrings();
			for (String scope : scopes) {
				malfunctionManager.addScopeString(scope);
			}
		}

		// If no life support then no internal repairs
		malfunctionManager.setSupportsInside(hasFunction(FunctionType.LIFE_SUPPORT));
	}


	/**
	 * Constructor 3 (for use by Mock Building in Unit testing)
	 *
	 * @return manager
	 */
	protected Building(BuildingManager manager, String name) {
//		super("Mock Building", new Coordinates(0D, 0D));
		super(name, new Coordinates(0D, 0D));

		if (manager != null) {
			this.settlement = manager.getSettlement();
			this.settlementID = settlement.getIdentifier();
		}
	}

	/**
	 * Constructor 4 (for use by Unit testing)
	 *
	 * @return manager
	 */
	protected Building() {
		super("Mock Building", new Coordinates(0D, 0D));
	}

	/**
	 * Gets the description of a building.
	 *
	 * @return String description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets building nickname
	 *
	 * @param nick name
	 */
	public void setBuildingNickName(String name) {
		this.nickName = name;
	}

	/**
	 * Gets the initial temperature of a building.
	 *
	 * @return temperature (deg C)
	 */
	public double getInitialTemperature() {
		return initialTemperature;
	}


	public Administration getAdministration() {
		if (admin == null)
			admin = (Administration) getFunction(FunctionType.ADMINISTRATION);
		return admin;
	}


	public AstronomicalObservation getAstronomicalObservation() {
		if (astro == null)
			astro = (AstronomicalObservation) getFunction(FunctionType.ASTRONOMICAL_OBSERVATION);
		return astro;
	}

	public Dining getDining() {
		if (dine == null)
			dine = (Dining) getFunction(FunctionType.DINING);
		return dine;
	}

	public LifeSupport getLifeSupport() {
		if (lifeSupport == null)
			lifeSupport = (LifeSupport) getFunction(FunctionType.LIFE_SUPPORT);
		return lifeSupport;
	}

	public Farming getFarming() {
		if (farm == null)
			farm = (Farming) getFunction(FunctionType.FARMING);
		return farm;
	}

	public Fishery getFishery() {
		if (fish == null)
			fish = (Fishery) getFunction(FunctionType.FISHERY);
		return fish;
	}

	public Communication getComm() {
		if (comm == null)
			comm = (Communication) getFunction(FunctionType.COMMUNICATION);
		return comm;
	}


	public Cooking getCooking() {
		if (cooking == null)
			cooking = (Cooking) getFunction(FunctionType.COOKING);
		return cooking;
	}


	public Exercise getExercise() {
		if (gym == null)
			gym = (Exercise) getFunction(FunctionType.EXERCISE);
		return gym;
	}

	public EVA getEVA() {
		if (eva == null)
			eva = (EVA) getFunction(FunctionType.EVA);
		return eva;
	}

	public EarthReturn getEarthReturn() {
		if (earthReturn == null)
			earthReturn = (EarthReturn) getFunction(FunctionType.EARTH_RETURN);
		return earthReturn;
	}


	public FoodProduction getFoodProduction() {
		if (foodFactory == null)
			foodFactory = (FoodProduction) getFunction(FunctionType.FOOD_PRODUCTION);
		return foodFactory;
	}

	public GroundVehicleMaintenance getGroundVehicleMaintenance() {
		if (maint == null)
			maint = (GroundVehicleMaintenance) getFunction(FunctionType.GROUND_VEHICLE_MAINTENANCE);
		return maint;
	}

	public LivingAccommodations getLivingAccommodations() {
		if (livingAccommodations == null)
			livingAccommodations = (LivingAccommodations) getFunction(FunctionType.LIVING_ACCOMMODATIONS);
		return livingAccommodations;
	}

	public Management getManagement() {
		if (management == null)
			management = (Management) getFunction(FunctionType.MANAGEMENT);
		return management;
	}

	public Manufacture getManufacture() {
		if (workshop == null)
			workshop = (Manufacture) getFunction(FunctionType.MANUFACTURE);
		return workshop;
	}

	public MedicalCare getMedical() {
		if (medical == null)
			medical = (MedicalCare) getFunction(FunctionType.MEDICAL_CARE);
		return medical;
	}

	public PowerGeneration getPowerGeneration() {
		if (powerGen == null)
			powerGen = (PowerGeneration) getFunction(FunctionType.POWER_GENERATION);
		return powerGen;
	}

	public Computation getComputation() {
		if (computation == null)
			computation = (Computation) getFunction(FunctionType.COMPUTATION);
		return computation;
	}

	public PowerStorage getPowerStorage() {
		if (powerStorage == null)
			powerStorage = (PowerStorage) getFunction(FunctionType.POWER_STORAGE);
		return powerStorage;
	}

	public PreparingDessert getPreparingDessert() {
		if (preparingDessert == null)
			preparingDessert = (PreparingDessert) getFunction(FunctionType.PREPARING_DESSERT);
		return preparingDessert;
	}

	public Recreation getRecreation() {
		if (rec == null)
			rec = (Recreation) getFunction(FunctionType.RECREATION);
		return rec;
	}

	public Research getResearch() {
		if (lab == null)
			lab = (Research) getFunction(FunctionType.RESEARCH);
		return lab;
	}

	public ResourceProcessing getResourceProcessing() {
		if (processing == null)
			processing = (ResourceProcessing) getFunction(FunctionType.RESOURCE_PROCESSING);
		return processing;
	}

	public RoboticStation getRoboticStation() {
		if (roboticStation == null)
			roboticStation = (RoboticStation) getFunction(FunctionType.ROBOTIC_STATION);

		return roboticStation;
	}

	public ThermalGeneration getThermalGeneration() {
		if (furnace == null)
			furnace = (ThermalGeneration) getFunction(FunctionType.THERMAL_GENERATION);
		return furnace;
	}

	public Storage getStorage() {
		if (storage == null)
			storage = (Storage) getFunction(FunctionType.STORAGE);
		return storage;
	}

	public WasteDisposal getWaste() {
		if (waste == null)
			waste = (WasteDisposal) getFunction(FunctionType.WASTE_DISPOSAL);
		return waste;
	}

	public VehicleMaintenance getVehicleMaintenance() {
		if (garage == null)
			garage = (VehicleMaintenance) getFunction(FunctionType.GROUND_VEHICLE_MAINTENANCE);
		return garage;
	}

	/**
	 * Gets the temperature of a building.
	 *
	 * @return temperature (deg C)
	 */
	public double getCurrentTemperature() {
		if (heating != null)
			return heating.getCurrentTemperature();
		else
			return initialTemperature;
	}

	/**
	 * Gets a function type that has with openly available (empty) activity spot
	 *
	 * @return FunctionType
	 */
	public FunctionType getEmptyActivitySpotFunctionType() {
		Function f = getEmptyActivitySpotFunction();
		if (f != null)
			return f.getFunctionType();
		else
			return null;
	}

	/**
	 * Gets a function that has with openly available (empty) activity spot
	 *
	 * @return FunctionType
	 */
	public Function getEmptyActivitySpotFunction() {
		List<Function> goodFunctions = new ArrayList<>();

		for (Function f : functions) {
			if (f.hasEmptyActivitySpot())
				goodFunctions.add(f);
		}

		if (goodFunctions.isEmpty())
			return null;

		// Choose a random function
		int index = RandomUtil.getRandomInt(goodFunctions.size() - 1);

		return goodFunctions.get(index);
	}

	/**
	 * Determines the building functions.
	 *
	 * @return list of building .
	 * @throws Exception if error in functions.
	 */
	private List<Function> buildFunctions(BuildingSpec spec) {
		List<Function> buildingFunctions = new ArrayList<>();

		for(FunctionType supported : spec.getFunctionSupported()) {
			switch (supported) {

			case ADMINISTRATION:
				buildingFunctions.add(new Administration(this));
				break;

			case ASTRONOMICAL_OBSERVATION:
				buildingFunctions.add(new AstronomicalObservation(this));
				break;

			case BUILDING_CONNECTION:
				buildingFunctions.add(new BuildingConnection(this));
				break;

			case COMMUNICATION:
				buildingFunctions.add(new Communication(this));
				break;

			case COMPUTATION:
				buildingFunctions.add(new Computation(this));
				break;

			case COOKING:
				buildingFunctions.add(new Cooking(this));
				buildingFunctions.add(new PreparingDessert(this));
				break;

			case DINING:
				buildingFunctions.add(new Dining(this));
				break;

			case EARTH_RETURN:
				buildingFunctions.add(new EarthReturn(this));
				break;

			case EVA:
				buildingFunctions.add(new EVA(this));
				break;

			case EXERCISE:
				buildingFunctions.add(new Exercise(this));
				break;

			case FARMING:
				buildingFunctions.add(new Farming(this));
				break;

			case FISHERY:
				buildingFunctions.add(new Fishery(this));
				break;

			case FOOD_PRODUCTION:
				buildingFunctions.add(new FoodProduction(this));
				break;

			case GROUND_VEHICLE_MAINTENANCE:
				buildingFunctions.add(new GroundVehicleMaintenance(this));
				break;

			case LIFE_SUPPORT:
				buildingFunctions.add(new LifeSupport(this));
				break;

			case LIVING_ACCOMMODATIONS:
				buildingFunctions.add(new LivingAccommodations(this));
				break;

			case MANAGEMENT:
				buildingFunctions.add(new Management(this));
				break;

			case MANUFACTURE:
				buildingFunctions.add(new Manufacture(this));
				break;

			case MEDICAL_CARE:
				buildingFunctions.add(new MedicalCare(this));
				break;

			case POWER_GENERATION:
				buildingFunctions.add(new PowerGeneration(this));
				break;

			case POWER_STORAGE:
				buildingFunctions.add(new PowerStorage(this));
				break;

			case RECREATION:
				buildingFunctions.add(new Recreation(this));
				break;

			case RESEARCH:
				buildingFunctions.add(new Research(this));
				break;

			case RESOURCE_PROCESSING:
				buildingFunctions.add(new ResourceProcessing(this));
				break;

			case ROBOTIC_STATION:
				buildingFunctions.add(new RoboticStation(this));
				break;

			case STORAGE:
				buildingFunctions.add(new Storage(this));
				break;

			case THERMAL_GENERATION:
				buildingFunctions.add(new ThermalGeneration(this));
				break;

			case WASTE_DISPOSAL:
				// No Waste Disposal at the moment. Why ?
				//buildingFunctions.add(new WasteDisposal(this));
				break;

			default:
				throw new IllegalArgumentException("Do not know how to build Function " + supported);
			}
		}
		return buildingFunctions;
	}

	/**
	 * Checks if this building has a particular function.
	 *
	 * @param function the enum name of the function.
	 * @return true if it does.
	 */
	public boolean hasFunction(FunctionType functionType) {
		for (Function f : functions) {
			if (f.getFunctionType() == functionType) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets a function if the building has it.
	 *
	 * @param functionType {@link FunctionType} the function of the building.
	 * @return function.
	 * @throws BuildingException if building doesn't have the function.
	 */
	public Function getFunction(FunctionType functionType) {
		for (Function f : functions) {
			if (f.getFunctionType() == functionType) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Remove the building's functions from the settlement.
	 */
	public void removeFunctionsFromSettlement() {

		Iterator<Function> i = functions.iterator();
		while (i.hasNext()) {
			i.next().removeFromSettlement();
		}
	}

	/**
	 * Remove a building function
	 *
	 * @param function
	 */
	public void removeFunction(Function function) {
		if (functions.contains(function)) {
			functions.remove(function);
			// Need to remove the function from the building function map
			getBuildingManager().removeOneFunctionfromBFMap(this, function);
		}
	}

	/**
	 * Gets the building's building manager.
	 *
	 * @return building manager
	 */
	public BuildingManager getBuildingManager() {
		return settlement.getBuildingManager();
	}

	/**
	 * Sets the building's nickName
	 *
	 * @return none
	 */
	// Called by TabPanelBuilding.java for building nickname change
	public void setNickName(String nickName) {
		this.nickName = nickName;
		changeName(nickName);
	}

	/**
	 * Gets the building's nickName
	 *
	 * @return building's nickName as a String
	 */
	// Called by TabPanelBuilding.java for building nickname change
	public String getNickName() {
		if (nickName == null || nickName.equalsIgnoreCase(""))
			nickName = getName();
		return nickName;
	}

	/**
	 * Gets the building type, not building's nickname
	 *
	 * @return building type as a String.
	 */
	public String getName() {
		return buildingType;
	}

	/**
	 * Gets the building type.
	 *
	 * @return building type as a String. TODO internationalize building names for
	 *         display in user interface.
	 */
	public String getBuildingType() {
		return buildingType;
	}

	public double getWidth() {
		return width;
	}

	public double getLength() {
		return length;
	}

	public double getFloorArea() {
		return floorArea;
	}

	/**
	 * Returns the volume of the building in liter
	 *
	 * @return volume in liter
	 */
	public double getVolumeInLiter() {
		return floorArea * HEIGHT * 1000; // 1 Cubic Meter = 1,000 Liters
	}

	@Override
	public double getXLocation() {
		return loc.getX();
	}
	
	@Override
	public double getYLocation() {
		return loc.getY();
	}
	
	@Override
	public LocalPosition getPosition() {
		return loc;
	}

	@Override
	public double getFacing() {
		return facing;
	}

	public boolean getInTransport() {
		return inTransportMode;
	}

	public void setInTransport(boolean value) {
		inTransportMode = value;
	}

	/**
	 * Gets the base level of the building.
	 *
	 * @return -1 for in-ground, 0 for above-ground.
	 */
	public int getBaseLevel() {
		return baseLevel;
	}

	/**
	 * Gets the power this building currently requires for full-power mode.
	 *
	 * @return power in kW.
	 */
	public double getFullPowerRequired() {
		double result = basePowerRequirement;

		// Determine power required for each function.
		for (Function function : functions) {
			double power = function.getFullPowerRequired();
			if (power > 0)
//				System.out.println(nickName + " : "
//					+ function.getFunctionType().toString() + " : "
//					+ Math.round(power * 10.0)/10.0 + " kW");
			result += power;
		}

		return result + powerNeededForEVAheater;
	}

	/**
	 * Gets the power the building requires for power-down mode.
	 *
	 * @return power in kW.
	 */
	public double getPoweredDownPowerRequired() {
		double result = basePowerDownPowerRequirement;

		// Determine power required for each function.
		for (Function function : functions) {
			result += function.getPoweredDownPowerRequired();
		}

		return result;
	}

	/**
	 * Gets the building's heat mode.
	 */
	public PowerMode getPowerMode() {
		return powerModeCache;
	}

	/**
	 * Sets the building's heat mode.
	 */
	public void setPowerMode(PowerMode powerMode) {
		this.powerModeCache = powerMode;
	}

	/**
	 * Gets the heat this building currently requires for full-power mode.
	 *
	 * @return heat in kW.
	 */
	public double getFullHeatRequired() {
		// double result = baseHeatRequirement;
		double result = 0;

		if (furnace != null && heating != null)
			result = furnace.getHeating().getFullHeatRequired();

		return result;
	}

	/**
	 * Sets the value of the heat generated
	 *
	 * @param heatGenerated
	 */
	public void setHeatGenerated(double heatGenerated) {
		if (heating == null)
			heating = furnace.getHeating();
		heating.setHeatGenerated(heatGenerated);
	}

	/**
	 * Sets the required power for heating
	 *
	 * @param powerReq
	 */
	public void setPowerRequiredForHeating(double powerReq) {
		if (heating == null)
			heating = furnace.getHeating();
		heating.setPowerRequired(powerReq);
	}

//	 public void setPowerGenerated(double powerGenerated) {
//		 powerGen.setPowerGenerated(powerGenerated);
//	 }

	/**
	 * Gets the heat the building requires for power-down mode.
	 *
	 * @return heat in kJ/s.
	 */
	public double getPoweredDownHeatRequired() {
		double result = 0;
		if (furnace != null && heating != null)
			result = heating.getPoweredDownHeatRequired();
		return result;
	}

	/**
	 * Gets the building's power mode.
	 */
	public HeatMode getHeatMode() {
		return heatModeCache;
	}

	/**
	 * Sets the building's heat mode.
	 */
	public void setHeatMode(HeatMode heatMode) {
		heatModeCache = heatMode;
	}

	/**
	 * Gets the entity's malfunction manager.
	 *
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	/**
	 * Gets the total amount of lighting power in this greenhouse.
	 *
	 * @return power (kW)
	 */
	public double getTotalPowerForEVA() {
		return powerNeededForEVAheater;
	}

	/**
	 * Calculates the number of people in the airlock
	 *
	 * @return number of people
	 */
	public int numOfPeopleInAirLock() {
		int num = 0;
		if (eva == null)
			eva = (EVA) getFunction(FunctionType.EVA);
		if (eva != null) {
			num = eva.getAirlock().getOccupants().size();
			powerNeededForEVAheater = num * kW_EVA_HEATER * .5D; // assume half of people are doing EVA ingress
																	// statistically
		}
		return num;
	}


	/**
	 * Gets the number of people
	 *
	 * @return
	 */
	public int getNumPeople() {

		int people = 0;

		if (lifeSupport != null) {
			people = lifeSupport.getOccupants().size();
		}

		return people;
	}


	/**
	 * Gets a collection of inhabitants
	 *
	 * @return
	 */
	public Collection<Person> getInhabitants() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		if (lifeSupport != null) {
			for (Person occupant : lifeSupport.getOccupants()) {
				if (!people.contains(occupant))
					people.add(occupant);
			}
		}

		return people;
	}

	/**
	 * Gets a collection of robots
	 *
	 * @return
	 */
	public Collection<Robot> getRobots() {
		Collection<Robot> robots = new ConcurrentLinkedQueue<Robot>();

		if (roboticStation != null) {
			for (Robot occupant : roboticStation.getRobotOccupants()) {
				if (!robots.contains(occupant))
					robots.add(occupant);
			}
		}

		return robots;
	}

	/**
	 * Gets a collection of people affected by this entity. Children buildings
	 * should add additional people as necessary.
	 *
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {

		Collection<Person> people = getInhabitants();
		// Check all people in settlement.
		Iterator<Person> i = settlement.getIndoorPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this building.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}

			// Add all people repairing this facility.
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
	 * String representation of this building.
	 *
	 * @return building's nickName.
	 */
	public String toString() {
		return nickName;
	}

	/**
	 * Compares this object with the specified object for order.
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Building o) {
		return buildingType.compareToIgnoreCase(o.buildingType);
	}

	/**
	 * Time passing for building.
	 *
	 * @param time amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!isValid(pulse)) {
			return false;
		}

		// Send time to each building function.
		for (Function f : functions)
			f.timePassing(pulse);

		// If powered up, active time passing.
		if (powerModeCache == PowerMode.FULL_POWER)
			malfunctionManager.activeTimePassing(pulse.getElapsed());

		// Update malfunction manager.
		malfunctionManager.timePassing(pulse);

		if (pulse.isNewSol()) {
			// Determine if a meteorite impact will occur within the new sol
			checkForMeteoriteImpact(pulse);
		}

		inTransportMode = false;
		return true;
	}

	public List<Function> getFunctions() {
		return functions;
	}

	/*
	 * Checks for possible meteorite impact for this building
	 */
	private void checkForMeteoriteImpact(ClockPulse pulse) {
		// check for the passing of each day

		int moment_of_impact = 0;

		BuildingManager manager = settlement.getBuildingManager();

		// if assuming a gauissan profile, p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Note: Will have 70% of values will fall between mean +/- standardDeviation, i.e., within one std deviation
		double probability = floorArea * manager.getProbabilityOfImpactPerSQMPerSol();
		// probability is in percentage unit between 0% and 100%
		if (probability > 0 && RandomUtil.getRandomDouble(100D) <= probability) {
			isImpactImminent = true;
			// set a time for the impact to happen any time between 0 and 1000 milisols
			moment_of_impact = RandomUtil.getRandomInt(1000);
		}

		if (isImpactImminent) {

			int now = pulse.getMarsTime().getMillisolInt();
			// Note: at the fastest sim speed, up to ~5 millisols may be skipped.
			// need to set up detection of the impactTimeInMillisol with a +/- 3 range.
			int delta = (int) Math.sqrt(Math.sqrt(pulse.getMasterClock().getTargetTR()));
			if (now > moment_of_impact - 2 * delta && now < moment_of_impact + 2 * delta) {
				logger.log(this, Level.INFO, 0, "A meteorite impact over is imminent.");

				// Reset the boolean immmediately. This is for keeping track of whether the
				// impact has occurred at msols
				isImpactImminent = false;
				// find the length this meteorite can penetrate
				double penetrated_length = manager.getWallPenetration();

				if (penetrated_length >= wallThickness) {
					// Yes it's breached !
					// Simulate the meteorite impact as a malfunction event for now
					Malfunction mal = malfunctionManager.triggerMalfunction(MalfunctionFactory
							.getMalfunctionByname(MalfunctionFactory.METEORITE_IMPACT_DAMAGE),
							true, null);

					String victimName = "None";
//					String task = "N/A";

					// check if someone under this roof may have seen/affected by the impact
					for (Person person : getInhabitants()) {
						if (person.getBuildingLocation() == this
								&& RandomUtil.lessThanRandPercent(METEORITE_IMPACT_PROBABILITY_AFFECTED)) {

							// TODO: someone got hurt, declare medical emergency
							// TODO: delineate the accidents from those listed in malfunction.xml
							// currently, malfunction whether a person gets hurt is handled by Malfunction
							// above
							int resilience = person.getNaturalAttributeManager()
									.getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
							int courage = person.getNaturalAttributeManager()
									.getAttribute(NaturalAttributeType.COURAGE);
							double factor = 1 + RandomUtil.getRandomDouble(1) - resilience / 100 - courage / 100D;
							PhysicalCondition pc = person.getPhysicalCondition();
							if (factor > 1)
								pc.setStress(pc.getStress() * factor);

							victimName = person.getName();
							mal.setTraumatized(victimName);

							// Store the meteorite fragment in the settlement
							settlement.storeAmountResource(ResourceUtil.meteoriteID, manager.getDebrisMass());

							logger.log(this, Level.INFO, 0, "Found " + Math.round(manager.getDebrisMass() * 100.0)/100.0
									+ " kg of meteorite fragments in " + getNickName() + ".");

							if (pc.getStress() > 30)
								logger.log(this, Level.WARNING, 0, victimName + " was traumatized by the meteorite impact");

						}
					}
				}
			}
		}
	}

	public Coordinates getLocation() {
		return settlement.getCoordinates();
	}

	/**
	 * Gets the building's inhabitable ID number.
	 *
	 * @return id.
	 */
	public int getInhabitableID() {
		return inhabitableID;
	}

	/**
	 * Sets the building's settlement inhabitable ID number.
	 *
	 * @param id.
	 */
	public void setInhabitableID(int id) {
		inhabitableID = id;
	}

	/**
	 * Gets the building's settlement template ID number.
	 *
	 * @return id.
	 */
	public int getTemplateID() {
		return templateID;
	}

	/**
	 * Sets the building's settlement template ID number.
	 *
	 * @param id.
	 */
	public void setTemplateID(int id) {
		templateID = id;
	}

	/**
	 * Gets the settlement it is currently associated with.
	 *
	 * @return settlement or null if none.
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	public void extractHeat(double heat) {
		// Set the instance of thermal generation function.
		if (furnace == null)
			furnace = (ThermalGeneration) getFunction(FunctionType.THERMAL_GENERATION);
		if (heating == null)
			heating = furnace.getHeating();

		heating.setHeatLoss(heat);
	}

	public double getCurrentAirPressure() {
		return getSettlement().getBuildingAirPressure(this);
	}

	@Override
	public Building getBuildingLocation() {
		return this;
	}

	/**
	 * Gets the settlement the person is currently associated with.
	 *
	 * @return associated settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return getSettlement();
	}

	public int getBuildingID() {
		return bid;
	}

	public void reinit() {
		settlement = unitManager.getSettlementByID(settlementID);

		// Get the building's functions
		if (functions == null) {
			BuildingSpec spec = SimulationConfig.instance().getBuildingConfiguration().getBuildingSpec(buildingType);
			functions = buildFunctions(spec);
		}
	}

	public boolean isAHabOrHub() {
        return buildingType.contains(" Hab")
                || buildingType.contains(" Hub");
    }

	/**
	 * Checks if the building has a lab with a particular science type
	 *
	 * @param type
	 * @return
	 */
	public boolean hasSpecialty(ScienceType type) {
		if (getResearch() == null)
			return false;

		return lab.hasSpecialty(type);
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.BUILDING;
	}

	/**
	 * Is this unit inside a settlement
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {
		return true;
	}

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return getSettlement().getAmountResourceStored(resource);
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
		return getSettlement().storeAmountResource(resource, quantity);
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
		return getSettlement().retrieveAmountResource(resource, quantity);
	}

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return getSettlement().getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		return getSettlement().getAmountResourceRemainingCapacity(resource);
	}

	/**
	 * Gets all stored amount resources
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return getSettlement().getAmountResourceIDs();
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
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
			// 3. Set containerID
			// Q: what to set for a deceased person ?
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
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
		return unitManager.getSettlementByID(containerID);
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

	@Override
	public int storeItemResource(int resource, int quantity) {
		return getSettlement().storeItemResource(resource, quantity);
	}

	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return getSettlement().retrieveItemResource(resource, quantity);
	}


	@Override
	public int getItemResourceStored(int resource) {
		return getSettlement().getItemResourceStored(resource);
	}

	/**
	 * Gets the remaining quantity of an item resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		return getSettlement().getItemResourceRemainingQuantity(resource);
	}

	@Override
	public Set<Integer> getItemResourceIDs() {
		return getSettlement().getItemResourceIDs();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Building b = (Building) obj;
		return this.getIdentifier() == b.getIdentifier()
			&& this.buildingType.equals(b.getBuildingType());
//			&& this.nickName.equals(b.getNickName());
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = nickName.hashCode();
		hashCode *= getIdentifier();
		hashCode *= buildingType.hashCode();
		return hashCode;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		functions = null;
		furnace = null;
		lifeSupport = null;
		roboticStation = null;
		powerGen = null;
		heatModeCache = null;
		buildingType = null;
		powerModeCache = null;
		heatModeCache = null;
//		malfunctionManager.destroy();
		malfunctionManager = null;
	}
}
