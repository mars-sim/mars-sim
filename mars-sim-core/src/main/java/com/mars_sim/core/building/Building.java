/*
 * Mars Simulation Project
 * Building.java
 * @date 2025-09-01
 * @author Scott Davis
 */
package com.mars_sim.core.building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.building.connection.InsidePathLocation;
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
import com.mars_sim.core.building.task.MaintainBuilding;
import com.mars_sim.core.building.utility.heating.ThermalGeneration;
import com.mars_sim.core.building.utility.power.PowerGeneration;
import com.mars_sim.core.building.utility.power.PowerMode;
import com.mars_sim.core.building.utility.power.PowerStorage;
import com.mars_sim.core.environment.MeteoriteImpactProperty;
import com.mars_sim.core.equipment.ItemHolder;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.hazard.HazardEvent;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionFactory;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.malfunction.task.Repair;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.unit.FixedUnit;
import com.mars_sim.core.unit.UnitHolder;

/**
 * The Building class is a settlement's building.
 */
public class Building extends FixedUnit implements Malfunctionable,
	 InsidePathLocation, Temporal, ResourceHolder, UnitHolder, ItemHolder {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(Building.class.getName());

	private static final String _HAB = " hab";
	private static final String _HUB = " hub";
		
	/** The height of an airlock in meters */
	// Assume an uniform height of 2.5 meters in all buildings
	private static final double HEIGHT = 2.5;
	/** 500 W heater for use during EVA ingress */
	private static final double EVA_HEATER_KW = .5D;
	// Assuming 20% chance for each person to witness or be conscious of the
	// meteorite impact in an affected building
	private static final double METEORITE_IMPACT_PROBABILITY_AFFECTED = 20;

	// Data members
	/** The flag to track if the impact is immientnt for the current sol. */
	boolean isImpactImminent = false;
	/** Checked by getAllImmovableBoundedObjectsAtLocation() in LocalAreaUtil */
	boolean inTransportMode = true;
	
	private int momentOfImpact = 999;
	/** The designated zone where this building is located at. */
	private int zone;
	/** The base level for this building. -1 for in-ground, 0 for above-ground. */
	private int baseLevel;

	/** Default : 22.5 deg celsius. */
	private double presetTemperature = 0; //22.5D
	private double width;
	// Q: how to handle the indefinite length of hallway/tunnel ?
	// "-1" if it doesn't exist.
	private double length;
	private double floorArea;
	private double areaFactor;
	private double facing;
	private double baseFullPowerRequirement;
	private double baseLowPowerRequirement;
	private double powerNeededForEVAHeater;
	
	/** Unique template id assigned for the settlement template of this building belong. */
	private String templateID;
	/** Type of building. */
	private String buildingType;

	/** The MalfunctionManager instance. */
	protected MalfunctionManager malfunctionManager;

	private EVA eva;
	private LifeSupport lifeSupport;
	private ThermalGeneration furnace;
	private Recreation rec;
	private Research lab;
	private RoboticStation roboticStation;
	private PowerGeneration generator;

	private PowerMode powerModeCache;
	private BuildingCategory category;
	private ConstructionType constructionType;
	/** The x and y location of this building. */
	private LocalPosition loc;

	/** A map of functions for this building. */
	private Map<FunctionType, Function> functionMap = new EnumMap<>(FunctionType.class);
	
	private static HistoricalEventManager eventManager;
	
	private static SimulationConfig simulationConfig = SimulationConfig.instance();

	/**
	 * Factory method to create from a building template
	 *
	 * @param spec the building 
	 * @param owner The owning Settlement
	 */
	public static Building createBuilding(BuildingTemplate template, Settlement owner) {
		var spec = BuildingManager.getBuildingConfig().getBuildingSpec(template.getBuildingType());

		return new Building(owner, template.getID(), template.getZone(), template.getBuildingName(),
							template.getBounds(), spec);
	}

	/**
	 * Constructor 1: the mandatory properties. 
	 *
	 * @param id           the building's unique ID number.
	 * @param buildingType the building Type.
	 * @param name         the building's name.
	 * @param bounds       the physical position of this Building
	 */
	public Building(Settlement owner, String id, int zone, String name,
					BoundedObject bounds, String buildingType, BuildingCategory category) {
		super(name, owner);

		this.templateID = id;
		this.zone = zone;
		this.buildingType = buildingType;
		this.category = category;

		this.loc = bounds.getPosition();
		this.facing = bounds.getFacing();
		this.width = bounds.getWidth();
		this.length = bounds.getLength();

		this.powerModeCache = PowerMode.FULL_POWER;

		if (name.toLowerCase().contains(_HAB) || name.toLowerCase().contains(_HUB)) {
			// For Habs and Hubs that have a circular footprint
			this.floorArea = Math.PI * (.5 * length) * (.5 * length);
		}
		else
			this.floorArea = length * width;
		
		areaFactor = Math.sqrt(floorArea) / 2;
				
		if (floorArea <= 0) {
			throw new IllegalArgumentException("Floor area cannot be -ve (w=" + width + ", l=" + length + ").");
		}
	}

	/**
	 * Constructor 2 : Constructs a Building object.
	 * Called by Constructor 1 and ConstructionSite's createBuilding().
	 *
	 * @param id           the building's unique ID number.
	 * @param buildingType the building Type.
	 * @param name         the building's name.
	 * @param bounds       the physical position of this Building
	 * @param manager      the building's building manager.
	 * @throws BuildingException if building can not be created.
	 */
	public Building(Settlement owner, String id, int zone, String name, BoundedObject bounds,
						BuildingSpec buildingSpec) {
		this(owner, id, zone, name, buildingSpec.getValidBounds(bounds), buildingSpec.getName(), buildingSpec.getCategory());
		
		constructionType = buildingSpec.getConstruction();

		baseLevel = buildingSpec.getBaseLevel();
		setDescription(buildingSpec.getDescription());

		// Get base power requirements.
		baseFullPowerRequirement = buildingSpec.getBasePowerRequirement();
		baseLowPowerRequirement = buildingSpec.getBasePowerDownPowerRequirement();

		// Set room temperature
		presetTemperature = buildingSpec.getPresetTemperature();
		
		// Determine total maintenance time.
		double totalMaintenanceTime = buildingSpec.getMaintenanceTime() / 3D;

		// Set up the function map
		for (FunctionType supported : buildingSpec.getFunctionSupported()) {
			totalMaintenanceTime += addFunction(buildingSpec.getFunctionSpec(supported)).getMaintenanceTime();
		}

		// Set up malfunction manager.
		malfunctionManager = new MalfunctionManager(this, buildingSpec.getWearLifeTime(), totalMaintenanceTime);
	
		malfunctionManager.addScopeString(buildingSpec.getName());
		// Add building type to the part scope
		simulationConfig.getPartConfiguration().addScopes(buildingSpec.getName());
		
		if (!buildingSpec.getScopeDone()) {
			// Note: Only need to set up the system scope once for building type
	
			// Add the building type to buildingSpec.
			malfunctionManager.addScopeString(buildingSpec.getSystemScopes());
				
			// Add the building spec scopes to malfunction manager.
			malfunctionManager.addScopeString(buildingSpec.getSystemScopes());
					
			// Add each function to the malfunction scope.
			for (Function sfunction : getFunctions()) {
				Set<String> scopes = sfunction.getMalfunctionScopeStrings();
				for (String scope : scopes) {
					malfunctionManager.addScopeString(scope);
				}
			}
			
			buildingSpec.setScopeDone(true);
		}
	
		// Transfer all system scopes from BuildingSpec to malfunction manager.
		malfunctionManager.addScopeString(buildingSpec.getSystemScopes());
					
		// If no life support then no internal repairs.
		malfunctionManager.setSupportInsideRepair(hasFunction(FunctionType.LIFE_SUPPORT));
		
		// Initialize the scope map.
		malfunctionManager.initScopes();
	}

	/**
	 * Gets the category of this building.
	 */
	public BuildingCategory getCategory() {
		return category;
	}

	/**
	 * Gets the preset temperature of a building.
	 *
	 * @return temperature (deg C)
	 */
	public double getPresetTemperature() {
		return presetTemperature;
	}

	public Administration getAdministration() {
		return getFunction(FunctionType.ADMINISTRATION);
	}

	public AlgaeFarming getAlgae() {
		return getFunction(FunctionType.ALGAE_FARMING);
	}
	
	public AstronomicalObservation getAstronomicalObservation() {
		return getFunction(FunctionType.ASTRONOMICAL_OBSERVATION);
	}

	public Dining getDining() {
		return getFunction(FunctionType.DINING);
	}

	public LifeSupport getLifeSupport() {
		return getFunction(FunctionType.LIFE_SUPPORT);
	}

	public Farming getFarming() {
		return getFunction(FunctionType.FARMING);
	}

	public Fishery getFishery() {
		return getFunction(FunctionType.FISHERY);
	}

	public Communication getComm() {
		return getFunction(FunctionType.COMMUNICATION);
	}

	public Cooking getCooking() {
		return getFunction(FunctionType.COOKING);
	}

	public Exercise getExercise() {
		return getFunction(FunctionType.EXERCISE);
	}

	public EVA getEVA() {
		return getFunction(FunctionType.EVA);
	}

	public EarthReturn getEarthReturn() {
		return getFunction(FunctionType.EARTH_RETURN);
	}

	public FoodProduction getFoodProduction() {
		return getFunction(FunctionType.FOOD_PRODUCTION);
	}

	public VehicleGarage getVehicleParking() {
		return getFunction(FunctionType.VEHICLE_MAINTENANCE);
	}

	public LivingAccommodation getLivingAccommodation() {
		return getFunction(FunctionType.LIVING_ACCOMMODATION);
	}

	public Management getManagement() {
		return getFunction(FunctionType.MANAGEMENT);
	}

	public Manufacture getManufacture() {
		return getFunction(FunctionType.MANUFACTURE);
	}

	public MedicalCare getMedical() {
		return getFunction(FunctionType.MEDICAL_CARE);
	}

	public PowerGeneration getPowerGeneration() {
		return getFunction(FunctionType.POWER_GENERATION);
	}

	public Computation getComputation() {
		return getFunction(FunctionType.COMPUTATION);
	}

	public PowerStorage getPowerStorage() {
		return getFunction(FunctionType.POWER_STORAGE);
	}

	public Recreation getRecreation() {
		return getFunction(FunctionType.RECREATION);
	}

	public Research getResearch() {
		return getFunction(FunctionType.RESEARCH);
	}

	public ResourceProcessing getResourceProcessing() {
		return getFunction(FunctionType.RESOURCE_PROCESSING);
	}
	
	public RoboticStation getRoboticStation() {
		return getFunction(FunctionType.ROBOTIC_STATION);
	}

	public ThermalGeneration getThermalGeneration() {
		return getFunction(FunctionType.THERMAL_GENERATION);
	}

	public Storage getStorage() {
		return getFunction(FunctionType.STORAGE);
	}

	public VehicleMaintenance getVehicleMaintenance() {
		return getFunction(FunctionType.VEHICLE_MAINTENANCE);
	}

	public WasteProcessing getWasteProcessing() {
		return getFunction(FunctionType.WASTE_PROCESSING);
	}
	
	/**
	 * Gets the temperature of a building.
	 *
	 * @return temperature (deg C)
	 */
	public double getCurrentTemperature() {
		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			return furnace.getHeating().getCurrentTemperature();
	
		return presetTemperature;
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
	 * Gets a function that has with openly available (or empty) activity spot.
	 *
	 * @return Function
	 */
	public Function getEmptyActivitySpotFunction() {
		List<Function> goodFunctions = new ArrayList<>();

		// First, use recreation function's empty activity spot if available
		if (rec == null) {
			rec = getRecreation();
		}
		if (rec != null && rec.hasEmptyActivitySpot()) {
			return rec;
		}
								
		for (Function f : getFunctions()) {
			if (f.getFunctionType() != FunctionType.EVA 
				&& f.hasEmptyActivitySpot())
				goodFunctions.add(f);
		}

		if (goodFunctions.isEmpty())
			return null;

		// Choose a random function
		int index = RandomUtil.getRandomInt(goodFunctions.size() - 1);

		return goodFunctions.get(index);
	}

	/**
	 * Gets a local activity spot that is available (empty).
	 *
	 * @return FunctionType
	 */
	public LocalPosition getRandomEmptyActivitySpot() {
		
		// First, use the recreation function activity spot if available
		if (rec == null) {
			rec = getRecreation();
		}
		if (rec != null) {
			LocalPosition availableLoc = rec.getAvailableActivitySpot();
			if (availableLoc != null) {
				return availableLoc;
			}
		}

		for (Function f : getFunctions()) {
			if (f.getFunctionType() != FunctionType.EVA) {
				LocalPosition loc = f.getAvailableActivitySpot();
				if (loc != null)
					return loc;
			}
		}

		return null;
	}
		
	/**
	 * Determines the building functions.
	 *
	 * @return list of building .
	 * @throws Exception if error in functions.
	 */
	public Function addFunction(FunctionSpec fSpec) {
		var f =  switch (fSpec.getType()) {
			case ADMINISTRATION -> new Administration(this, fSpec);
			case ALGAE_FARMING -> new AlgaeFarming(this, fSpec);				
			case ASTRONOMICAL_OBSERVATION -> new AstronomicalObservation(this, fSpec);
			case CONNECTION -> new BuildingConnection(this, fSpec);
			case COMMUNICATION -> new Communication(this, fSpec);
			case COMPUTATION -> new Computation(this, fSpec);
			case COOKING -> new Cooking(this, fSpec);
			case DINING -> new Dining(this, fSpec);
			case EARTH_RETURN -> new EarthReturn(this, fSpec);
			case EVA -> new EVA(this, fSpec);
			case EXERCISE -> new Exercise(this, fSpec);
			case FARMING -> new Farming(this, fSpec);
			case FISHERY -> new Fishery(this, fSpec);
			case FOOD_PRODUCTION -> new FoodProduction(this, fSpec);
			case VEHICLE_MAINTENANCE -> new VehicleGarage(this, fSpec);
			case LIFE_SUPPORT -> new LifeSupport(this, fSpec);
			case LIVING_ACCOMMODATION -> new LivingAccommodation(this, fSpec);
			case MANAGEMENT -> new Management(this, fSpec);
			case MANUFACTURE -> new Manufacture(this, fSpec);
			case MEDICAL_CARE -> new MedicalCare(this, fSpec);
			case POWER_GENERATION -> new PowerGeneration(this, fSpec);
			case POWER_STORAGE -> new PowerStorage(this, fSpec);
			case RECREATION -> new Recreation(this, fSpec);
			case RESEARCH -> new Research(this, fSpec);
			case RESOURCE_PROCESSING -> new ResourceProcessing(this, fSpec);
			case ROBOTIC_STATION -> new RoboticStation(this, fSpec);
			case STORAGE -> new Storage(this, fSpec);
			case THERMAL_GENERATION -> new ThermalGeneration(this, fSpec);
			case WASTE_PROCESSING -> new WasteProcessing(this, fSpec);
			default ->
				throw new IllegalArgumentException("Do not know how to build Function " + fSpec.getType());
			};

		// Add to the function map
		functionMap.put(fSpec.getType(), f);	
		
		return f;
	}

	/**
	 * Checks if this building has a particular function.
	 *
	 * @param function the enum name of the function.
	 * @return true if it does.
	 */
	public boolean hasFunction(FunctionType functionType) {
		return functionMap.containsKey(functionType);
	}

	/**
	 * Gets a specific function from the function map.
	 * 
	 * @param key function type key
	 * @return the stored Function, cast to the expected subtype
	 */
	@SuppressWarnings("unchecked")
	public <T extends Function> T getFunction(FunctionType key) {
		return (T) functionMap.get(key);
	}

	/**
	 * Gets a collection of functions from the function map.
	 */
	public Collection<? extends Function> getFunctions() {
		return functionMap.values();
	}
		
	/**
	 * Removes the building's functions from the settlement.
	 */
	public void removeFunctionsFromSettlement() {

		for (Function f : getFunctions()) {
			f.removeFromSettlement();
			removeFunction(f);
		}

		functionMap.clear(); // Do NOT nullify
	}

	/**
	 * Removes a building function.
	 *
	 * @param function
	 */
	public void removeFunction(Function function) {
		if (functionMap.containsValue(function)) {
			functionMap.remove(function.getFunctionType());
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
		return getAssociatedSettlement().getBuildingManager();
	}

	/**
	 * Gets the building type.
	 *
	 * @return building type as a String.
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

	public double getAreaFactor() {
		return areaFactor;
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
	 * Gets the total power requirement for full-power mode on all functions.
	 *
	 * @return power in kW.
	 */
	public double getFullPowerRequired() {
		double result = baseFullPowerRequirement;

		// Determine power required for each function.
		for (Function function : getFunctions()) {
			double power = function.getCombinedPowerLoad();
			if (power > 0) {
				result += power;
			}
		}

		return result + powerNeededForEVAHeater;
	}

	/**
	 * Gets the power generated for full-power mode on all functions.
	 *
	 * @return power in kW.
	 */
	public double getGeneratedPower() {
		double result = 0;

		if (generator == null)
			generator = getFunction(FunctionType.POWER_GENERATION);
		
		if (generator != null) {
			result = generator.getGeneratedPower();
		}

		return result;
	}
	
	/**
	 * Gets the total power requirement for low-power mode on all functions.
	 *
	 * @return power in kW.
	 */
	public double getLowPowerRequired() {
		double result = baseLowPowerRequirement;

		// Determine power required for each function.
		for (Function function : getFunctions()) {
			result += function.getPoweredDownPowerRequired();
		}

		return result;
	}

	/**
	 * Gets the building's power mode.
	 */
	public PowerMode getPowerMode() {
		return powerModeCache;
	}

	/**
	 * Sets the building's power mode.
	 */
	public void setPowerMode(PowerMode powerMode) {
		if (powerModeCache != powerMode) {
			powerModeCache = powerMode;
			fireUnitUpdate(UnitEventType.POWER_MODE_EVENT, this);
		}
	}

	/**
	 * Gets the heat this building currently required.
	 *
	 * @return heat in kW.
	 */
	public double getHeatRequired() {
		double result = 0;
		
		// Set the instance of furnace function.
		prepareFurnace();
		 
		if (furnace != null)
			result = furnace.getHeating().getHeatRequired();

		return result;
	}

	/**
	 * Prepares the furnace instance.
	 */
	private void prepareFurnace() {
		if (furnace == null) {
			furnace = getFunction(FunctionType.THERMAL_GENERATION);
		}
	}

	
	/**
	 * Gets the heat gain of this building.
	 *
	 * @return heat in kW.
	 */
	public double getHeatGain() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getHeatGain();

		return result;
	}
	
	/**
	 * Gets the heat loss of this building.
	 *
	 * @return heat in kW.
	 */
	public double getHeatLoss() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getHeatLoss();

		return result;
	}
	
	/**
	 * Gets the initial net heat gain of this building.
	 *
	 * @return heat in kW.
	 */
	public double getPreNetHeat() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getPreNetHeat();

		return result;
	}
	
	/**
	 * Gets the post net heat gain of this building.
	 *
	 * @return heat in kW.
	 */
	public double getPostNetHeat() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getPostNetHeat();

		return result;
	}
	
	/**
	 * Gets the air heat sink of this building.
	 *
	 * @return heat in kW.
	 */
	public double getAirHeatSink() {
		double result = 0;
		
		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getAirHeatSink();

		return result;
	}
	
	/**
	 * Gets the water heat sink of this building.
	 *
	 * @return heat in kW.
	 */
	public double getWaterHeatSink() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getWaterHeatSink();

		return result;
	}
	
	/**
	 * Gets the heat generated for this building currently.
	 *
	 * @return heat in kW.
	 */
	public double getHeatGenerated() {
		double result = 0;
		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getHeatGenerated();

		return result;
	}
	
	/**
	 * Gets the excess heat dumped on this building.
	 *
	 * @return heat in kW.
	 */
	public double getExcessHeat() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getExcessHeat();

		return result;
	}
	
	/**
	 * Gets the vent heat passively initiated by other building toward this building.
	 *
	 * @return heat in kW.
	 */
	public double getPassiveVentHeat() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getPassiveVentHeat();

		return result;
	}
	
	/**
	 * Gets the vent heat actively managed by this building via ventilation.
	 *
	 * @return heat in kW.
	 */
	public double getActiveVentHeat() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getActiveVentHeat();

		return result;
	}
	
	
	/**
	 * Gets the heat surplus of this building.
	 *
	 * @return heat in kW.
	 */
	public double getHeatSurplus() {
		double result = 0;
		
		// Gets the furnace instance.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeatSurplus();

		return result;
	}
	
	
	/**
	 * Gets the delta temperature match of this building.
	 *
	 * @return heat in kW.
	 */
	public double getDeltaTemp() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getDeltaTemp();

		return result;
	}

	/**
	 * Gets the deviation temperature match of this building.
	 *
	 * @return heat in kW.
	 */
	public double getDevTemp() {
		double result = 0;

		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			result = furnace.getHeating().getDevTemp();

		return result;
	}
	
	/**
	 * Dumps the excess heat to the building.
	 *
	 * @param heatGenerated
	 */
	public void dumpExcessHeat(double heatGenerated) {
		// Set the instance of furnace function.
		prepareFurnace();
		
		if (furnace != null)
			furnace.getHeating().insertExcessHeatComputation(heatGenerated);
	}

	/**
	 * Gets the nuclear heat of this building currently.
	 *
	 * @return heat in kW.
	 */
	public double getNuclearPowerGen() {
		double result = 0;

		// Gets the furnace instance.
		prepareFurnace();
				
		if (furnace != null)
			result = furnace.getNuclearPowerGen();

		return result;
	}
	
	/**
	 * Gets the solar heat of this building currently.
	 *
	 * @return heat in kW.
	 */
	public double getSolarPowerGen() {
		double result = 0;

		// Gets the furnace instance.
		prepareFurnace();
				
		if (furnace != null)
			result = furnace.getSolarPowerGen();

		return result;
	}
	
	/**
	 * Gets the electric heat of this building currently.
	 *
	 * @return heat in kW.
	 */
	public double getElectricPowerGen() {
		double result = 0;

		// Gets the furnace instance.
		prepareFurnace();
				
		if (furnace != null)
			result = furnace.getElectricPowerGen();

		return result;
	}
	
	/**
	 * Gets the fuel heat of this building currently.
	 *
	 * @return heat in kW.
	 */
	public double getFuelPowerGen() {
		double result = 0;

		// Gets the furnace instance.
		prepareFurnace();
				
		if (furnace != null)
			result = furnace.getFuelPowerGen();

		return result;
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
			eva = getFunction(FunctionType.EVA);
		if (eva != null) {
			num = eva.getAirlock().getOccupants123().size();
			// Note: Assuming (.5) half of people are doing EVA ingress statistically
			powerNeededForEVAHeater = num * EVA_HEATER_KW * .5D; 
		}
		return num;
	}


	/**
	 * Gets the number of people.
	 *
	 * @return
	 */
	public int getNumPeople() {
		if (lifeSupport == null) {
			lifeSupport = getFunction(FunctionType.LIFE_SUPPORT);
		}
		if (lifeSupport != null) {
			return lifeSupport.getOccupantNumber();
		}

		return 0;
	}


	/**
	 * Gets a collection of building occupants.
	 *
	 * @return
	 */
	public Collection<Person> getOccupants() {
		if (lifeSupport == null) {
			lifeSupport = getFunction(FunctionType.LIFE_SUPPORT);
		}
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
		if (roboticStation == null) {
			roboticStation = getFunction(FunctionType.ROBOTIC_STATION);
		}
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

		Collection<Person> people = new HashSet<>();
		// Check all people in settlement.
		for(Person person : getAssociatedSettlement().getIndoorPeople()) {
			if (person.getBuildingLocation() == this) {
				people.add(person);
			}

			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this building.
			if ((task instanceof MaintainBuilding maintainBuilding)
				&& (maintainBuilding.getEntity() == this) && !people.contains(person)) {
				people.add(person);
			}

			// Add all people repairing this facility.
			if ((task instanceof Repair repair) && (repair.getEntity() == this)
				&& !people.contains(person)) {
				people.add(person);
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
		for (Function f : getFunctions())
			// Not needed for now. Will need this for future work
			f.timePassing(pulse);

		// If powered up, active time passing.
		if (powerModeCache == PowerMode.FULL_POWER)
			malfunctionManager.activeTimePassing(pulse);

		// Update malfunction manager.
		malfunctionManager.timePassing(pulse);

		if (pulse.isNewSol()) {
			// Determine if a meteorite impact will occur within the new sol
			momentOfImpact = checkImpactProbability();
		}
		
		if (isImpactImminent && pulse.isNewIntMillisol()) {
			// The impact is not or no longer imminent.
			// Note: if the impact has already once. isImpactImminent will be set back to false.
			checkImpactSeverity(pulse);
		}
		
		inTransportMode = false;
		return true;
	}

	/**
	 * Checks for probability of a meteorite impact for this building in a sol.
	 * 
	 * @return momentOfImpact in millisol
	 */
	private int checkImpactProbability() {

		var meteorite = getBuildingManager().getMeteorite();

		// Note: if assuming a gauissan profile, p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Will have 70% of values will fall between mean +/- standardDeviation, i.e., within one std deviation
	
		// Vary the angle
		double angleDegree = meteorite.getStandardIncidentAngle() * RandomUtil.getRandomDouble(.75, 1.25);
		
		double angularFactor = Math.sin(Math.toRadians(angleDegree)) ;
		
		// Note: vary the angle of impact will reduce the floorArea 
		
		double probability = angularFactor * floorArea * meteorite.getProbabilityOfImpactPerSQMPerSol();

		// Probability is in percentage unit between 0% and 100%
		if (probability > RandomUtil.getRandomDouble(100)) {
			// Set to true that an impact is about to occur for this sol
			isImpactImminent = true;
			// Set a time for the impact to happen any time between 0 and 1000 milisols
			momentOfImpact = RandomUtil.getRandomInt(1, 999);
			
			logger.log(this, Level.INFO, 0, 
					"A meteorite impact event was imminent (prob:" + Math.round(probability * 100_000.0)/100_000.0 
						+ "), forecasting at " + momentOfImpact + " millisols.");
		}
		else {
			// Set to false that an impact is about to occur for this sol
			isImpactImminent = false;
			
			// No the impact does not occur in the vicinity
//			May add back for debugging: logger.log(this, Level.INFO, 30_000, "Meteorite Impact event observed but occurred in settlement vicinity.");
		}
		
		return momentOfImpact;
	}
	
	/**
	 * Checks for the severity of a meteorite impact for this building.
	 * 
	 * @param pulse
	 */
	public void checkImpactSeverity(ClockPulse pulse) {
	
		int now = pulse.getMarsTime().getMillisolInt();

		if (now >= momentOfImpact) {

			// Reset the boolean immediately for keeping track of whether 
			// the impact has occurred
			isImpactImminent = false;
				
			MeteoriteImpactProperty meteorite = getBuildingManager().getMeteorite();
			
			// Find the length this meteorite can penetrate
			double penetratedLength = meteorite.getWallPenetration();

			double wallThick = getWallThickness();
			
			double reductionFraction = penetratedLength / wallThick;
			if (reductionFraction > 1)
				reductionFraction = 1;
			
			// The impact reduces the structural health of the building 
			// Future: it should also generates a repair task to at least assess the damage
			malfunctionManager.reduceWearLifeTime(reductionFraction);
			
			if (penetratedLength < wallThick) {
				// Case A: No. it's not breached
				logger.warning(this, 0, "Meteorite Impact event observed. Building wall not breached but damaged. "
						+ "Penetration fraction: " + Math.round(reductionFraction * 10.0)/10.0 + ".");
				
				// Future: need to set up a maintenance task to replace possibly broken external wall
				
				return ;
			}
	
			logger.warning(this, 0, "Meteorite Impact event observed. Building wall penetrated.");
			
			// Case B: Yes it's breached !	
			
			// Simulate the meteorite impact as a malfunction event for now
			Malfunction mal = malfunctionManager.triggerMalfunction(MalfunctionFactory
					.getMalfunctionByName(MalfunctionFactory.METEORITE_IMPACT_DAMAGE),
					true, this);

			logger.log(this, Level.INFO, 0, mal.getName() + " registered.");
			
			checkPersonImpact(mal);

			// Future: 
			// 1. Record this amount in the landscape in the settlement vicinity
			// 2. Send a person out to pick them up during a MaintainEVABuilding task
			
			// Store the meteorite fragment in the settlement
//			settlement.storeAmountResource(ResourceUtil.METEORITE_ID, floorArea * meteorite.getDebrisMass());

			logger.info(this, 0, "Estimated " + Math.round(meteorite.getDebrisMass() * 100.0)/100.0
					+ " kg of meteorite fragments in settlement vicinity");

		}
	}

	/**
	 * Determines the impacts on affected people.
	 * 
	 * @param mal
	 */
	private void checkPersonImpact(Malfunction mal) {

		String victimNames = null;
//		var settlement = getAssociatedSettlement();
		
		// check if someone under this roof may have seen/affected by the impact
		for (Person person : getOccupants()) {
			
			if (RandomUtil.lessThanRandPercent(METEORITE_IMPACT_PROBABILITY_AFFECTED)) {

				// Note 1: someone got hurt, declare medical emergency
				// Note 2: delineate the accidents from those listed in malfunction.xml
				// currently, malfunction whether a person gets hurt is handled by Malfunction
				// above
				int resilience = person.getNaturalAttributeManager()
						.getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
				int courage = person.getNaturalAttributeManager()
						.getAttribute(NaturalAttributeType.COURAGE);
				double stressFactor = 10 + RandomUtil.getRandomDouble(10) - resilience / 10D - courage / 10D;
				PhysicalCondition pc = person.getPhysicalCondition();
				if (stressFactor > 0) {
		            logger.info(person, 0, "Adding " + Math.round(stressFactor * 100.0)/100.0 + " to the stress.");
					pc.addStress(stressFactor);
				}
					
				if (victimNames != null)
					victimNames += ", " + person.getName();
				else
					victimNames = person.getName();
				
				mal.setTraumatized(victimNames);

				if (pc.getStress() > 50)
					logger.warning(this, 0, victimNames + " was traumatized by the meteorite impact");

			} // check if this person happens to be inside the affected building
			
		} // loop for persons
		

		if (victimNames == null)
			victimNames = "";
			
		// Pick the last person who witness this event in the affected building. Could be no one.
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
	
	/**
	 * Gets the wall thickness based on the constructionType type.
	 */
	private double getWallThickness() {
		return switch(constructionType) {
			case PRE_FABRICATED 	-> 0.000_025_4;
			case INFLATABLE 		-> 0.000_001_8;
			case SEMI_ENGINEERED 	-> 0.000_010_0;
			default -> 0;
		};
	}

	public ConstructionType getConstruction() {
		return constructionType;
	}

	/**
	 * Gets the building's settlement template ID number.
	 *
	 * @return id.
	 */
	public String getTemplateID() {
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
	 * Adds incoming heat arriving at this building due to ventilation.
	 * Note: heat gain if positive; heat loss if negative.
	 * 
	 * @param heat removed or added
	 */
	public void addVentInHeat(double heat) {
		// Set the instance of furnace function.
		prepareFurnace();

		if (furnace != null)
			furnace.getHeating().addVentInHeat(heat);
	}

	/**
	 * Gets the building air pressure.
	 * 
	 * @return
	 */
	public double getCurrentAirPressure() {
		double p = 0D;
		
		if (lifeSupport == null) {
			lifeSupport = getFunction(FunctionType.LIFE_SUPPORT);
		}
		if (lifeSupport != null) {
			p = lifeSupport.getAir().getTotalPressure();
		}
		
		// convert from atm to kPascal
		return p * AirComposition.KPA_PER_ATM;
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
		if (lab == null)
			lab = getFunction(FunctionType.RESEARCH);
		if (lab == null)
			return false;

		return lab.hasSpecialty(type);
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.BUILDING;
	}

	/**
	 * Gets the specific amount resources stored, NOT including those inside equipment.
	 *
	 * @param resource
	 * @return amount
	 */
	@Override
	public double getSpecificAmountResourceStored(int resource) {
		return getAssociatedSettlement().getSpecificAmountResourceStored(resource);
	}

	/**
	 * Gets all the specific amount resources stored, including those inside equipment.
	 *
	 * @param resource
	 * @return amount
	 */
	@Override
	public double getAllSpecificAmountResourceStored(int resource) {
		return getAssociatedSettlement().getAllSpecificAmountResourceStored(resource);
	}
	
	/**
	 * Gets the quantity of all stock and specific amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return getAssociatedSettlement().getAllAmountResourceStored(resource);
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
		return getAssociatedSettlement().storeAmountResource(resource, quantity);
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
		return getAssociatedSettlement().retrieveAmountResource(resource, quantity);
	}

	/**
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getSpecificCapacity(int resource) {
		return getAssociatedSettlement().getSpecificCapacity(resource);
	}

	/**
	 * Obtains the remaining combined storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingCombinedCapacity(int resource) {
		return getAssociatedSettlement().getRemainingCombinedCapacity(resource);
	}

	/**
	 * Obtains the remaining specific storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingSpecificCapacity(int resource) {
		return getAssociatedSettlement().getRemainingSpecificCapacity(resource);
	}
	
	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return getAssociatedSettlement().hasAmountResourceRemainingCapacity(resource);
	}
	
	/**
	 * Gets all stored amount resources.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getSpecificResourceStoredIDs() {
		return getAssociatedSettlement().getSpecificResourceStoredIDs();
	}
	
	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceStoredIDs() {
		return getAssociatedSettlement().getAllAmountResourceStoredIDs();
	}

	@Override
	public int storeItemResource(int resource, int quantity) {
		return getAssociatedSettlement().storeItemResource(resource, quantity);
	}

	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return getAssociatedSettlement().retrieveItemResource(resource, quantity);
	}


	@Override
	public int getItemResourceStored(int resource) {
		return getAssociatedSettlement().getItemResourceStored(resource);
	}

	/**
	 * Gets the remaining quantity of an item resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceRemainingQuantity(int resource) {
		return getAssociatedSettlement().getItemResourceRemainingQuantity(resource);
	}

	@Override
	public Set<Integer> getItemResourceIDs() {
		return getAssociatedSettlement().getItemResourceIDs();
	}
	
	@Override
	public double getCargoCapacity() {
		return 0;
	}
	
	/**
	 * Checks if this building is isInhabitable.
	 * 
	 * @return
	 */
	public boolean isInhabitable() {
		return getLifeSupport() == null;
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
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
}

