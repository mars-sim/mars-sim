/*
 * Mars Simulation Project
 * Building.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mars.sim.mapdata.location.BoundedObject;
import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.LocalBoundedObject;
import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.equipment.ItemHolder;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.hazard.HazardEvent;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Structure;
import org.mars_sim.msp.core.structure.building.connection.InsidePathLocation;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.BuildingConnection;
import org.mars_sim.msp.core.structure.building.function.Communication;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.EarthReturn;
import org.mars_sim.msp.core.structure.building.function.Exercise;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
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
import org.mars_sim.msp.core.structure.building.function.VehicleGarage;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.WasteProcessing;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.Dining;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

/**
 * The Building class is a settlement's building.
 */
public class Building extends Structure implements Malfunctionable, Indoor, 
		LocalBoundedObject, InsidePathLocation, Temporal, ResourceHolder, ItemHolder {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Building.class.getName());

	public static final int TISSUE_CAPACITY = 20;

	/** The height of an airlock in meters */
	// Assume an uniform height of 2.5 meters in all buildings
	public static final double HEIGHT = 2.5;
	/** 500 W heater for use during EVA ingress */
	public static final double kWEvaHeater = .5D;
	// Assuming 20% chance for each person to witness or be conscious of the
	// meteorite impact in an affected building
	public static final double METEORITE_IMPACT_PROBABILITY_AFFECTED = 20;

	/** A list of functions of this building. */
	protected List<Function> functions;

	// Data members
	boolean isImpactImminent = false;
	/** Checked by getAllImmovableBoundedObjectsAtLocation() in LocalAreaUtil */
	boolean inTransportMode = true;
	
	/** The designated zone where this building is located at. */
	protected int zone;
	/** Unique template id assigned for the settlement template of this building belong. */
	protected int templateID;
	/** The base level for this building. -1 for in-ground, 0 for above-ground. */
	protected int baseLevel;

	/** Default : 22.5 deg celsius. */
	private double initialTemperature = 22.5D;
	protected double width;
	protected double length;
	protected double floorArea;
	private ConstructionType construction;
	protected LocalPosition loc;
	protected double zLoc;
	protected double facing;
	protected double basePowerRequirement;
	protected double basePowerDownPowerRequirement;
	protected double powerNeededForEVAHeater;


	/** Type of building. */
	protected String buildingType;
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
	private VehicleGarage maint;
	private Heating heating;
	private LivingAccommodations livingAccommodations;
	private LifeSupport lifeSupport;
	private Management management;
	private Manufacture manufacture;
	private MedicalCare medicalCare;
	private ThermalGeneration furnace;
	private PowerGeneration powerGen;
	private PowerStorage powerStorage;
	private PreparingDessert preparingDessert;
	private Recreation rec;
	private Research lab;
	private ResourceProcessing resourceProcessing;
	private RoboticStation roboticStation;
	private Storage storage;
	private VehicleMaintenance vehicleMaintenance;
	private WasteProcessing wasteProcessing;
	
	protected PowerMode powerModeCache;
	protected HeatMode heatModeCache;
	private BuildingCategory category;
	
	private static HistoricalEventManager eventManager;
	private static BuildingConfig buildingConfig;

	/**
	 * Constructor 1 : Constructs a Building object.
	 *
	 * @param template the building template.
	 * @param manager  the building's building manager.
	 * @throws BuildingException if building can not be created.
	 */
	public Building(BuildingTemplate template, BuildingManager manager) {
		this(template.getID(), template.getZone(), template.getBuildingType(), template.getBuildingName(), template.getBounds(), manager);

		buildingType = template.getBuildingType();
		settlement = manager.getSettlement();
		settlementID = settlement.getIdentifier();

		// NOTE: needed for setting inhabitable id
		if (hasFunction(FunctionType.LIFE_SUPPORT)
			&& lifeSupport == null) {
			// Set the instance of life support
			lifeSupport = (LifeSupport) getFunction(FunctionType.LIFE_SUPPORT);
		}
	}

	/**
	 * Constructor 2 : Constructs a Building object.
	 *
	 * @param id           the building's unique ID number.
	 * @param buildingType the building Type.
	 * @param name         the building's name.
	 * @param bounds       the physical position of this Building
	 * @param manager      the building's building manager.
	 * @throws BuildingException if building can not be created.
	 */
	public Building(int id, int zone, String buildingType, String name, BoundedObject bounds, BuildingManager manager) {
		super(name, manager.getSettlement().getCoordinates());

		this.templateID = id;
		this.zone = zone;
		this.buildingType = buildingType;

		this.settlement = manager.getSettlement();
		this.settlementID = settlement.getIdentifier();
		setContainerID(settlementID);

		this.loc = bounds.getPosition();
		this.facing = bounds.getFacing();
		
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		
		BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);

		construction = spec.getConstruction();
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
		this.category = spec.getCategory();

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
		// Add 'Building' to malfunction manager.
		malfunctionManager.addScopeString(SystemType.BUILDING.getName());
		
		// Add building type to the standard scope
		PartConfig.addScopes(spec.getBuildingType());
		
		// Add building type to malfunction manager.
		malfunctionManager.addScopeString(spec.getBuildingType());
		
		
		// Add each function to the malfunction scope.
		for (Function sfunction : functions) {
			Set<String> scopes = sfunction.getMalfunctionScopeStrings();
			for (String scope : scopes) {
				malfunctionManager.addScopeString(scope);
			}
		}

		// Compute maintenance needed parts prior to starting
//		malfunctionManager.determineNewMaintenanceParts();
		
		// If no life support then no internal repairs
		malfunctionManager.setSupportsInside(hasFunction(FunctionType.LIFE_SUPPORT));
	}


	/**
	 * Constructor 3 : (for use by Mock Building in Unit testing).
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
	 * Constructor 4 : (for use by Unit testing).
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
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the category of this building.
	 */
	public BuildingCategory getCategory() {
		return category;
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

	public VehicleGarage getVehicleParking() {
		if (maint == null)
			maint = (VehicleGarage) getFunction(FunctionType.VEHICLE_MAINTENANCE);
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
		if (manufacture == null)
			manufacture = (Manufacture) getFunction(FunctionType.MANUFACTURE);
		return manufacture;
	}

	public MedicalCare getMedical() {
		if (medicalCare == null)
			medicalCare = (MedicalCare) getFunction(FunctionType.MEDICAL_CARE);
		return medicalCare;
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
		if (resourceProcessing == null)
			resourceProcessing = (ResourceProcessing) getFunction(FunctionType.RESOURCE_PROCESSING);
		return resourceProcessing;
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

	public VehicleMaintenance getVehicleMaintenance() {
		if (vehicleMaintenance == null)
			vehicleMaintenance = (VehicleMaintenance) getFunction(FunctionType.VEHICLE_MAINTENANCE);
		return vehicleMaintenance;
	}

	public WasteProcessing getWasteProcessing() {
		if (wasteProcessing == null)
			wasteProcessing = (WasteProcessing) getFunction(FunctionType.WASTE_PROCESSING);
		return wasteProcessing;
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
	 * Gets a function type that has with openly available (empty) activity spot.
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
	 * Gets a function that has with openly available (empty) activity spot.
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
			FunctionSpec fSpec = spec.getFunctionSpec(supported);
			switch (supported) {

			case ADMINISTRATION:
				buildingFunctions.add(new Administration(this, fSpec));
				break;

			case ASTRONOMICAL_OBSERVATION:
				buildingFunctions.add(new AstronomicalObservation(this, fSpec));
				break;

			case BUILDING_CONNECTION:
				buildingFunctions.add(new BuildingConnection(this, fSpec));
				break;

			case COMMUNICATION:
				buildingFunctions.add(new Communication(this, fSpec));
				break;

			case COMPUTATION:
				buildingFunctions.add(new Computation(this, fSpec));
				break;

			case COOKING:
				buildingFunctions.add(new Cooking(this, fSpec));
				break;

			case DINING:
				buildingFunctions.add(new Dining(this, fSpec));
				break;

			case EARTH_RETURN:
				buildingFunctions.add(new EarthReturn(this, fSpec));
				break;

			case EVA:
				buildingFunctions.add(new EVA(this, fSpec));
				break;

			case EXERCISE:
				buildingFunctions.add(new Exercise(this, fSpec));
				break;

			case FARMING:
				buildingFunctions.add(new Farming(this, fSpec));
				break;

			case FISHERY:
				buildingFunctions.add(new Fishery(this, fSpec));
				break;

			case FOOD_PRODUCTION:
				buildingFunctions.add(new FoodProduction(this, fSpec));
				break;

			case VEHICLE_MAINTENANCE:
				buildingFunctions.add(new VehicleGarage(this, fSpec));
				break;

			case LIFE_SUPPORT:
				buildingFunctions.add(new LifeSupport(this, fSpec));
				break;

			case LIVING_ACCOMMODATIONS:
				buildingFunctions.add(new LivingAccommodations(this, fSpec));
				break;

			case MANAGEMENT:
				buildingFunctions.add(new Management(this, fSpec));
				break;

			case MANUFACTURE:
				buildingFunctions.add(new Manufacture(this, fSpec));
				break;

			case MEDICAL_CARE:
				buildingFunctions.add(new MedicalCare(this, fSpec));
				break;

			case POWER_GENERATION:
				buildingFunctions.add(new PowerGeneration(this, fSpec));
				break;

			case POWER_STORAGE:
				buildingFunctions.add(new PowerStorage(this, fSpec));
				break;

			case PREPARING_DESSERT:
				buildingFunctions.add(new PreparingDessert(this, fSpec));
				break;
				
			case RECREATION:
				buildingFunctions.add(new Recreation(this, fSpec));
				break;

			case RESEARCH:
				buildingFunctions.add(new Research(this, fSpec));
				break;

			case RESOURCE_PROCESSING:
				buildingFunctions.add(new ResourceProcessing(this, fSpec));
				break;

			case ROBOTIC_STATION:
				buildingFunctions.add(new RoboticStation(this, fSpec));
				break;

			case STORAGE:
				buildingFunctions.add(new Storage(this, fSpec));
				break;

			case THERMAL_GENERATION:
				buildingFunctions.add(new ThermalGeneration(this, fSpec));
				break;

			case WASTE_PROCESSING:
				buildingFunctions.add(new WasteProcessing(this, fSpec));
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
	 * Removes the building's functions from the settlement.
	 */
	public void removeFunctionsFromSettlement() {

		Iterator<Function> i = functions.iterator();
		while (i.hasNext()) {
			i.next().removeFromSettlement();
		}
	}

	/**
	 * Removes a building function.
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
	 * Returns the volume of the building in liter.
	 *
	 * @return volume in liter
	 */
	public double getVolumeInLiter() {
		return floorArea * HEIGHT * 1000; // 1 Cubic Meter = 1,000 Liters
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
			if (power > 0) {
//			Test for System.out.println(nickName + " : "
//					+ function.getFunctionType().toString() + " : "
//					+ Math.round(power * 10.0)/10.0 + " kW")
				result += power;
			}
		}

		return result + powerNeededForEVAHeater;
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
	 * Sets the value of the heat generated.
	 *
	 * @param heatGenerated
	 */
	public void setHeatGenerated(double heatGenerated) {
		if (heating == null)
			heating = furnace.getHeating();
		heating.setHeatGenerated(heatGenerated);
	}

	/**
	 * Sets the required power for heating.
	 *
	 * @param powerReq
	 */
	public void setPowerRequiredForHeating(double powerReq) {
		if (heating == null)
			heating = furnace.getHeating();
		heating.setPowerRequired(powerReq);
	}

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
		return powerNeededForEVAHeater;
	}

	/**
	 * Calculates the number of people in the airlock.
	 *
	 * @return number of people
	 */
	public int numOfPeopleInAirLock() {
		int num = 0;
		if (eva == null)
			eva = (EVA) getFunction(FunctionType.EVA);
		if (eva != null) {
			num = eva.getAirlock().getOccupants().size();
			// Note: Assuming (.5) half of people are doing EVA ingress statistically
			powerNeededForEVAHeater = num * kWEvaHeater * .5D; 
		}
		return num;
	}


	/**
	 * Gets the number of people.
	 *
	 * @return
	 */
	public int getNumPeople() {

		int people = 0;

		if (lifeSupport != null) {
			people = lifeSupport.getOccupantNumber();
		}

		return people;
	}


	/**
	 * Gets a collection of inhabitants.
	 *
	 * @return
	 */
	public Collection<Person> getInhabitants() {
		if (lifeSupport != null) {
			return lifeSupport.getOccupants();
		}
		
		return Collections.emptySet();
	}

	/**
	 * Gets a collection of robots.
	 *
	 * @return
	 */
	public Collection<Robot> getRobots() {
		if (roboticStation != null) {
			return roboticStation.getRobotOccupants();
		}
		
		return Collections.emptySet();
	}

	/**
	 * Gets a collection of people affected by this entity. Children buildings
	 * should add additional people as necessary.
	 *
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {

		Collection<Person> people = new UnitSet<>();
		// Check all people in settlement.
		Iterator<Person> i = settlement.getIndoorPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();

			if (person.getBuildingLocation() == this) {
				people.add(person);
			}

			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this building.
			if (task instanceof MaintainBuilding) {
				if (((MaintainBuilding) task).getEntity() == this) {
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
			malfunctionManager.activeTimePassing(pulse);

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
	 * Checks for possible meteorite impact for this building.
	 */
	private void checkForMeteoriteImpact(ClockPulse pulse) {
		// Reset the impact time
		int moment_of_impact = 0;

		// if assuming a gauissan profile, p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Note: Will have 70% of values will fall between mean +/- standardDeviation, i.e., within one std deviation
		double probability = floorArea * getBuildingManager().getProbabilityOfImpactPerSQMPerSol();
		// Probability is in percentage unit between 0% and 100%
		if (probability > 0 && RandomUtil.getRandomDouble(100D) <= probability) {
			isImpactImminent = true;
			// Set a time for the impact to happen any time between 0 and 1000 milisols
			moment_of_impact = RandomUtil.getRandomInt(1000);
		}

		if (isImpactImminent) {

			int now = pulse.getMarsTime().getMillisolInt();
			// Note: at the fastest sim speed, up to ~5 millisols may be skipped.
			// need to set up detection of the impactTimeInMillisol with a +/- 3 range.
			int delta = (int) Math.sqrt(Math.sqrt(pulse.getMasterClock().getActualTR()));
			
			if (now > moment_of_impact - 2 * delta && now < moment_of_impact + 2 * delta) {
				logger.log(this, Level.INFO, 0, "A meteorite impact over is imminent.");

				// Reset the boolean immediately for keeping track of whether 
				// the impact has occurred
				isImpactImminent = false;
				// Find the length this meteorite can penetrate
				double penetrated_length = getBuildingManager().getWallPenetration();

				if (penetrated_length >= getWallThickness()) {
					// Yes it's breached !
					// Simulate the meteorite impact as a malfunction event for now
					Malfunction mal = malfunctionManager.triggerMalfunction(MalfunctionFactory
							.getMalfunctionByName(MalfunctionFactory.METEORITE_IMPACT_DAMAGE),
							true, this);

					logger.log(this, Level.INFO, 0, mal.getName() + " just occurred.");
					logger.log(this, Level.INFO, 0, "EventType: " + mal.getMalfunctionMeta().getName() + ".");
					
					String victimNames = null;
//					String task = "N/A";

					// check if someone under this roof may have seen/affected by the impact
					for (Person person : getInhabitants()) {
						if (person.getBuildingLocation() == this
								&& RandomUtil.lessThanRandPercent(METEORITE_IMPACT_PROBABILITY_AFFECTED)) {

							// Note 1: someone got hurt, declare medical emergency
							// Note 2: delineate the accidents from those listed in malfunction.xml
							// currently, malfunction whether a person gets hurt is handled by Malfunction
							// above
							int resilience = person.getNaturalAttributeManager()
									.getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
							int courage = person.getNaturalAttributeManager()
									.getAttribute(NaturalAttributeType.COURAGE);
							double factor = 1 + RandomUtil.getRandomDouble(1) - resilience / 100D - courage / 100D;
							PhysicalCondition pc = person.getPhysicalCondition();
							if (factor > 1)
								pc.setStress(pc.getStress() * factor);

							if (victimNames != null)
								victimNames += ", " + person.getName();
							else
								victimNames = person.getName();
							
							mal.setTraumatized(victimNames);

							// Store the meteorite fragment in the settlement
							settlement.storeAmountResource(ResourceUtil.meteoriteID, getBuildingManager().getDebrisMass());

							logger.log(this, Level.INFO, 0, "Found " + Math.round(getBuildingManager().getDebrisMass() * 100.0)/100.0
									+ " kg of meteorite fragments in " + getNickName() + ".");

							if (pc.getStress() > 30)
								logger.log(this, Level.WARNING, 0, victimNames + " was traumatized by the meteorite impact");

						}
						
					} // loop for persons

					// If it's not breached, how to record the damage
					logger.log(this, Level.INFO, 0, "Meteorite Impact event observed but damage not detected.");

					if (victimNames == null)
						victimNames = "";
						
					HistoricalEvent hEvent = new HazardEvent(
							EventType.HAZARD_ACTS_OF_GOD,
							this,
							mal.getMalfunctionMeta().getName(),
							"",
							victimNames,
							this);

					if (eventManager == null)
						eventManager = Simulation.instance().getEventManager();
					
					eventManager.registerNewEvent(hEvent);

					fireUnitUpdate(UnitEventType.METEORITE_EVENT);
				}
			}
		}
	}

	/**
	 * Get the wall thickness based on the construction type.
	 */
	private double getWallThickness() {
		switch(construction) {
			case SOLID:
				return 0.0000254;
			case INFLATABLE:
				return 0.0000018;
			case SEMI_SOLID:
				return 0.0000100;
			default:
				return 0;
		}
	}

	public ConstructionType getConstruction() {
		return construction;
	}

	public Coordinates getLocation() {
		return settlement.getCoordinates();
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
	 * Gets the zone # where this building is at. It's zero by default. 
	 * Astronomy Observatory is located at zone 1.
	 *
	 * @return zone.
	 */
	public int getZone() {
		return zone;
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

	// TODO this is wrong as names can change. This is just used to identify if there are multiple floors.
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
	 * Gets all the amount resource resource stored, including inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return getSettlement().getAllAmountResourceStored(resource);
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
	 * Retrieves the resource.
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
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return getSettlement().getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		return getSettlement().getAmountResourceRemainingCapacity(resource);
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return getSettlement().hasAmountResourceRemainingCapacity(resource);
	}
	
	/**
	 * Gets all stored amount resources.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return getSettlement().getAmountResourceIDs();
	}
	
	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceIDs() {
		return getSettlement().getAllAmountResourceIDs();
	}
	
	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	public boolean setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return false;
			}
			// 1. Set Coordinates
			setCoordinates(null);
			// 2. Set LocationStateType
			currentStateType = LocationStateType.INSIDE_SETTLEMENT;
			// 3. Set containerID
			// Q: what to set for a deceased person ?
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}

		return true;
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
	 * Gets the remaining quantity of an item resource.
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
	public double getCargoCapacity() {
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public void reinit() {
		settlement = unitManager.getSettlementByID(settlementID);

		if (buildingConfig == null)
			buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
		// Get the building's functions
		if (functions == null) {
			BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);
			functions = buildFunctions(spec);
		}
	}
	
	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Prepares object for garbage collection.
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
		malfunctionManager = null;
	}
}
