/**
 * Mars Simulation Project
 * GoodsManager.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.goods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.Barrel;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.GasCanister;
import org.mars_sim.msp.core.equipment.LargeBag;
import org.mars_sim.msp.core.equipment.SpecimenBox;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcess;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessItem;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.HotMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Ingredient;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleConfig.VehicleDescription;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A manager for computing the values of goods at a settlement.
 */
public class GoodsManager implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	/** Initialized logger. */
	private static SimLogger logger = SimLogger.getLogger(GoodsManager.class.getName());

	private static final String SCRAP = "scrap";
	private static final String INGOT = "ingot";
	private static final String SHEET = "sheet";
	private static final String TRUSS = "steel truss";
	private static final String STEEL_WIRE = "steel wire";
	private static final String STEEL_CAN = "steel canister";
	private static final String AL_WIRE = "aluminum wire";

	private static final String BOTTLE = "bottle";
	private static final String FIBERGLASS = "fiberglass";
	private static final String FIBERGLASS_CLOTH = "fiberglass cloth";

	private static final String METHANE = "methane";

	private static final String BRICK = "brick";

	private static final String[] KITCHEN_WARE = new String[] { "autoclave", "blender", "microwave", "oven",
			"refrigerator", "stove" };

	// TODO Mission types should be an enum.
	private static final String TRAVEL_TO_SETTLEMENT_MISSION = "travel to settlement";
	private static final String EXPLORATION_MISSION = "exploration";
	private static final String COLLECT_ICE_MISSION = "collect ice";
	private static final String RESCUE_SALVAGE_MISSION = "rescue/salvage mission";
	private static final String TRADE_MISSION = "trade";

	private static final String COLLECT_REGOLITH_MISSION = "collect regolith";
	private static final String MINING_MISSION = "mining";
	private static final String CONSTRUCT_BUILDING_MISSION = "construct building";
	private static final String AREOLOGY_STUDY_FIELD_MISSION = "areology field study";
	private static final String BIOLOGY_STUDY_FIELD_MISSION = "biology field study";
	private static final String METEOROLOGY_STUDY_FIELD_MISSION = "meterology field study";

	private static final String SALVAGE_BUILDING_MISSION = "salvage building";
	private static final String EMERGENCY_SUPPLY_MISSION = "deliver emergency supplies";
	private static final String DELIVERY_MISSION = "deliver resources";

	// Number modifiers for outstanding repair and maintenance parts ane EVA parts.
	private static final int BASE_REPAIR_PART = 150;
	private static final int BASE_MAINT_PART = 15;
	private static final int BASE_EVA_SUIT = 1;

	private static final int ATTACHMENT_PARTS_DEMAND = 10;

	private static final int PROJECTED_GAS_CANISTERS = 1;

	private static final double DAMPING_RATIO = .5;
	private static final double MIN = .000_001;

	private static final double INITIAL_AMOUNT_DEMAND = 10;
	private static final double INITIAL_PART_DEMAND = 10;
	private static final double INITIAL_EQUIPMENT_DEMAND = 10;
	private static final double INITIAL_VEHICLE_DEMAND = 10;

	private static final double WASTE_WATER_VALUE = .05D;
	private static final double WASTE_VALUE = .01D;
	private static final double USEFUL_WASTE_VALUE = 1.05D;

	private static final double EVA_SUIT_VALUE = 1D;

	private static final double EVA_PARTS_VALUE = 1D;
	
	private static final double ORE_VALUE = 1.1D;
	private static final double MINERAL_VALUE = 1.1D;

	private static final double ROBOT_FACTOR = 1;

	private static final double TRANSPORT_VEHICLE_FACTOR = 10D;
	private static final double CARGO_VEHICLE_FACTOR = 8D;
	private static final double EXPLORER_VEHICLE_FACTOR = 6D;
	private static final double LUV_VEHICLE_FACTOR = 2D;
	private static final double DRONE_VEHICLE_FACTOR = 10D;
	private static final double LUV_FACTOR = 2;
	private static final double DRONE_FACTOR = 2;

	private static final double VEHICLE_FUEL_FACTOR = .5D;

	private static final double RESOURCE_PROCESSING_INPUT_FACTOR = .5D;
	private static final double MANUFACTURING_INPUT_FACTOR = 2D;
	private static final double CONSTRUCTING_INPUT_FACTOR = 2D;

	private static final double COOKED_MEAL_INPUT_FACTOR = .5D;
	private static final double DESSERT_FACTOR = .1D;
	private static final double FOOD_PRODUCTION_INPUT_FACTOR = .5D;
	private static final double FARMING_FACTOR = 1000D;
	private static final double TISSUE_CULTURE_FACTOR = 1;
	private static final double LEAVES_FACTOR = .95;
	private static final double CROP_FACTOR = 100;

	private static final double CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR = 100D;
	private static final double CONSTRUCTION_SITE_REQUIRED_PART_FACTOR = 100D;

	private static final double MIN_SUPPLY = 0.05;
	private static final double MIN_DEMAND = 0.05;
	private static final double MAX_SUPPLY = 5_000;
	private static final double MAX_DEMAND = 5_000;
	private static final double MAX_PROJ_DEMAND = 50_000;
	
	private static final double LIFE_SUPPORT_MIN = 100;

	
	private static final double SPEED_TO_DISTANCE = 2D / 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;

	private static final double CROPFARM_BASE = 1;
	private static final double MANU_BASE = 1;
	private static final double RESEARCH_BASE = 1.5;
	private static final double TRANSPORT_BASE = 1;
	private static final double TRADE_BASE = 1;
	private static final double TOURISM_BASE = 1;

	private static final double GAS_CANISTER_DEMAND = .5D;
	private static final double SPECIMEN_BOX_DEMAND = 1D;
	private static final double LARGE_BAG_DEMAND = .1D;
	private static final double BAG_DEMAND = .005D;
	private static final double BARREL_DEMAND = .05D;

	private static final double SCRAP_METAL_DEMAND = .5;
	private static final double INGOT_METAL_DEMAND = .2;
	private static final double SHEET_METAL_DEMAND = .4;
	private static final double STEEL_WIRE_DEMAND = .4;
	private static final double STEEL_CAN_DEMAND = .5;
	private static final double AL_WIRE_DEMAND = .5;
	private static final double BOTTLE_DEMAND = .5;
	private static final double FIBERGLASS_DEMAND = .5;
	private static final double KITCHEN_DEMAND = .5;
	private static final double BRICK_DEMAND = 1.01;

	/** VP probability modifier. */
	public static final double ICE_VALUE_MODIFIER = .1D;
	private static final double WATER_VALUE_MODIFIER = 3D;

	public static final double REGOLITH_VALUE_MODIFIER = .2D;
	public static final double SAND_VALUE_MODIFIER = .95D;
	public static final double ROCK_MODIFIER = 0.99D;
	public static final double METEORITE_MODIFIER = 1.05;

	public static final double OXYGEN_VALUE_MODIFIER = .02D;
	public static final double METHANE_VALUE_MODIFIER = .5D;

	private static final double LIFE_SUPPORT_FACTOR = .01;
	private static final double FOOD_VALUE_MODIFIER = .1;

//	public static final double MIN_PRICE = .01;
//	public static final double MAX_PRICE = 10_000;

	// Data members
//	private double maxPrice = MAX_PRICE;
//	private double minPrice = MIN_PRICE;

	private double repairMod = BASE_REPAIR_PART;
	private double maintenanceMod = BASE_MAINT_PART;
	private double eVASuitMod = BASE_EVA_SUIT;
	private double waterValue = WATER_VALUE_MODIFIER;

	private boolean initialized = false;
	// Add modifiers due to Settlement Development Objectives
	private double cropFarm_factor = 1;
	private double manufacturing_factor = 1;
	private double research_factor = 1;
	private double transportation_factor = 1;
	private double trade_factor = 1;
	private double tourism_factor = 1;

//	private double vp_cache;
//	private double inflation_rate = 1;

	private Map<Integer, Double> goodsValues = new HashMap<>();
	private Map<Integer, Double> amountDemandCache = new HashMap<>();
	private Map<Integer, Double> goodsTradeCache = new HashMap<>();
	private Map<Integer, Double> partDemandCache = new HashMap<>();
	private Map<Integer, Double> vehicleDemandCache = new HashMap<>();
	private Map<Integer, Double> equipmentDemandCache = new HashMap<>();
//	 private Map<Good, Double> goodsSupplyCache;

	private Map<String, Double> vehicleBuyValueCache;
	private Map<String, Double> vehicleSellValueCache;

	/** A standard list of resources to be excluded in buying negotiation. */
	private static List<Good> exclusionBuyList = null;
	/** A standard list of buying resources in buying negotiation. */	
	private static List<Good> buyList = null;
	
	private Settlement settlement;

	private static SimulationConfig simulationConfig = SimulationConfig.instance();
//	private static BuildingConfig buildingConfig = simulationConfig.getBuildingConfiguration();
	private static CropConfig cropConfig = simulationConfig.getCropConfiguration();
//	private static MealConfig mealConfig = simulationConfig.getMealConfiguration();
	private static PersonConfig personConfig = simulationConfig.getPersonConfig();
	private static VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();

	private static Simulation sim = Simulation.instance();
	private static MissionManager missionManager = sim.getMissionManager();
	private static UnitManager unitManager = sim.getUnitManager();
	private static MarsClock marsClock = sim.getMasterClock().getMarsClock();
	
	/**
	 * Constructor.
	 * 
	 * @param settlement the settlement this manager is for.
	 */
	public GoodsManager(Settlement settlement) {
		this.settlement = settlement;

		populateGoodsValues();
	}

	/**
	 * Checks if goods manager has been initialized.
	 * 
	 * @return initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Populates the goods cache maps with empty values.
	 */
	private void populateGoodsValues() {
		Set<Integer> ids = GoodsUtil.getGoodsMap().keySet();
		Iterator<Integer> i = ids.iterator();
		while (i.hasNext()) {
			int id = i.next();
			goodsValues.put(id, 1D);
			goodsTradeCache.put(id, 0D);
		}

		// Create amount demand cache.
		Iterator<Integer> r = ResourceUtil.getIDs().iterator();
		while (r.hasNext()) {
			int id = r.next();
			amountDemandCache.put(id, INITIAL_AMOUNT_DEMAND);
		}

		// Create parts demand cache.
		Iterator<Integer> p = ItemResourceUtil.getItemIDs().iterator();
		while (p.hasNext()) {
			int id = p.next();
			partDemandCache.put(id, INITIAL_PART_DEMAND);
		}

		// Create eqipment demand cache.
		Iterator<Integer> e = EquipmentType.getIDs().iterator();
		while (e.hasNext()) {
			int id = e.next();
			equipmentDemandCache.put(id, INITIAL_EQUIPMENT_DEMAND);
		}

		// Create eqipment demand cache.
		Iterator<Integer> v = VehicleType.getIDs().iterator();
		while (v.hasNext()) {
			int id = v.next();
			vehicleDemandCache.put(id, INITIAL_VEHICLE_DEMAND);
		}
				
		// Create vehicle caches.
		vehicleBuyValueCache = new HashMap<String, Double>();
		vehicleSellValueCache = new HashMap<String, Double>();
	}
	
	/**
	 * Gets a list of item to be excluded in a buying negotiation 
	 * 
	 * @return
	 */
	public static List<Good> getExclusionBuyList() {
		if (exclusionBuyList == null) {
			exclusionBuyList = new ArrayList<>();
			for (VehicleType type: VehicleType.values()) {
				exclusionBuyList.add(GoodsUtil.getVehicleGood(type));
			}
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.regolithID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.iceID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.co2ID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.sandID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.greyWaterID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.blackWaterID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.compostID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.eWasteID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.toxicWasteID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.cropWasteID));
		}
		return exclusionBuyList;
	}

	/**
	 * Time passing
	 * 
	 * @param time the amount of time passing (millisols).
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		initialized = true;
//		update();

//		maxPrice = MAX_PRICE * (100 + pulse.getMarsTime().getMissionSol()) / 100D;

		return true;
	}

	/**
	 * Determines the value of a good.
	 * 
	 * @param good     the good to check.
	 * @param supply   the current supply (# of items) of the good.
	 * @param useCache use demand and trade caches to determine value?
	 * @return value of good.
	 */
	public double determineGoodValue(Good good, double supply, boolean useCache) {
		if (good != null) {
			double value = 0D;

			// Determine all amount resource good values.
			if (GoodType.AMOUNT_RESOURCE == good.getCategory())
				value = determineAmountResourceGoodValue(good, supply, useCache);

			// Determine all item resource values.
			if (GoodType.ITEM_RESOURCE == good.getCategory())
				value = determineItemResourceGoodValue(good, supply, useCache);

			// Determine all equipment values.
			if (GoodType.EQUIPMENT == good.getCategory())
				value = determineEquipmentGoodValue(good, supply, useCache);

			// Determine all vehicle values.
			if (GoodType.VEHICLE == good.getCategory())
				value = determineVehicleGoodValue(good, supply, useCache);

			return value;
		} else
			logger.severe(settlement, "Good is null.");

		return 0;
	}

	/**
	 * Determines the value of an amount resource.
	 * 
	 * @param resourceGood the amount resource good.
	 * @param supply       the current supply (kg) of the good.
	 * @param useCache     use the cache to determine value. always true if just
	 *                     traded
	 * @return value (value points / kg)
	 */
	private double determineAmountResourceGoodValue(Good resourceGood, double supply, boolean useCache) {
//		System.out.println(settlement + "::determineAmountResourceGoodValue");
		int id = resourceGood.getID();

		if (useCache) {
			double value = goodsValues.get(id);
//			if (id == ResourceUtil.regolithID) // || id == ResourceUtil.waterID || id == ResourceUtil.regolithBID)
//				System.out.println("1a. " + id 
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  cache value: " + Math.round(value*10.0)/10.0
//					);
			return value;//goodsValues.get(id);
		}
		
		else {
			double amountValue = 10;
			double previous = 10;
			double average = 10;
			double projected = 10;
			double trade = 10;
//			double lifeSupport = 10;

			double totalDemand = 0;
			double totalSupply = 0;

			// Needed for loading a saved sim
			int solElapsed = marsClock.getMissionSol();
			// Compact and/or clear supply and demand maps every x days
			int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;
			
			if (amountDemandCache.containsKey(id)) {
				// Get previous demand
				previous = amountDemandCache.get(id);
			}
			
			// Calculate total demand

			// Calculate the average demand
			average = getAverageCapAmountDemand(id, numSol);
			
			
			// Calculate projected demand
			
//			// Tune ice demand.
			projected += computeIceProjectedDemand(id);
			
			// Tune regolith projected demand.
			projected += computeRegolithProjectedDemand(id);
			
			// Tune life support demand if applicable.
			projected += getLifeSupportDemand(id);
			
//			// Tune fuel demand if applicable.
//			projected += getFuelDemand(id); // cause the sim to freeze
//
//			// Tune potable water usage demand if applicable.
//			projected += getPotableWaterUsageDemand(id);
//
//			// Tune toiletry usage demand if applicable.
//			projected += getToiletryUsageDemand(id);
//
			// Tune vehicle demand if applicable.
//			projected += getVehicleDemand(id);
//
//			// Tune farming demand.
//			projected += getFarmingDemand(id);
//
//			// Tune the crop demand
//			projected += getCropDemand(id);
//
//			// Tune resource processing demand.
//			projected += getResourceProcessingDemand(id);
//
//			// Tune manufacturing demand.
//			projected += getResourceManufacturingDemand(id);
//
//			// Tune food production related demand.
//			projected += getResourceFoodProductionDemand(id);
//
//			// Tune demand for the ingredients in a cooked meal.
//			projected += getResourceCookedMealIngredientDemand(id);
//
//			// Tune dessert demand.
//			projected += getResourceDessertDemand(id);
//
//			// Tune construction demand.
//			projected += getResourceConstructionDemand(id);
//
//			// Tune construction site demand.
//			projected += getResourceConstructionSiteDemand(id);
//
//			// Adjust the demand on various waste products with the disposal cost.
//			projected = getWasteDisposalSinkCost(id, projected);
//
//			// Adjust the demand on minerals and ores.
//			projected = getMineralDemand(id, projected);

			// Adjust the demand on life support consumables with the disposal cost.
//			projected = AdjustLifeSupport(id, projected);
			
//			if (projected > MAX_PROJ_DEMAND);
//				projected = MAX_PROJ_DEMAND;
			projected = Math.min(MAX_PROJ_DEMAND, projected);	
			
			// Add trade value.
			trade = determineTradeDemand(resourceGood, useCache);
			
			totalDemand = .85 * previous + .05 * average + .05 * projected + .05 * trade;
			
//			if (totalDemand < MIN_DEMAND)
//				totalDemand = MIN_DEMAND;
			totalDemand = Math.max(MIN_DEMAND, totalDemand);
//			if (totalDemand > MAX_DEMAND)
//				totalDemand = MAX_DEMAND;
			totalDemand = Math.min(MAX_DEMAND, totalDemand);	
			
			// Calculate total supply
			totalSupply =  getAverageAmountSupply(id, supply, solElapsed);// lowerLifeSupportSupply(id, supply * .1);
			
			if (totalSupply < MIN_SUPPLY)
				totalSupply = MIN_SUPPLY;

			if (totalSupply > MAX_SUPPLY)
				totalSupply = MAX_SUPPLY;
			
			// Save the goods demand
			amountDemandCache.put(id, totalDemand);
			// Calculate the value point
			amountValue = totalDemand / totalSupply;
			// Save the value point
			goodsValues.put(id, totalDemand);
			
//			if (id == ResourceUtil.methaneID)
//				System.out.println("ad. " + id
//						+ "  " + ResourceUtil.findAmountResourceName(id)
//						+ "  previous: " + Math.round(previous*10.0)/10.0
//						+ "  average: " + Math.round(average*10.0)/10.0
//						+ "  projected: " + Math.round(projected*10.0)/10.0
//						+ "  trade: " + Math.round(trade*10.0)/10.0
//						+ "  supply: " + Math.round(supply*10.0)/10.0
//						+ "  totalDemand: " + Math.round(totalDemand*10.0)/10.0
//						+ "  totalSupply: " + Math.round(totalSupply*10.0)/10.0
//						+ "  amountValue: " + Math.round(amountValue*10.0)/10.0
//						);
			return amountValue;
		}
	}


	public double getAverageCapAmountDemand(int id, int numSol) {
		return capLifeSupportAmountDemand(id, getAverageAmountDemand(id, numSol));
	}
	
	/**
	 * Gets the total supply amount for the amount resource
	 * 
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	public double getAverageAmountSupply(int resource, double supplyStored, int solElapsed) {
		// Gets the total produced or supplied since last time
		double supply = getInventory().getAmountSupply(resource);
		// Gets # of requests
		int requests = getInventory().getAmountSupplyRequest(resource);

		double aveSupply = Math.log(1 + supply * requests + supplyStored) / solElapsed;
				
//		if (resource == ResourceUtil.regolithID)
//			System.out.println("aas. " + resource 
//				+ "  " + ResourceUtil.findAmountResourceName(resource)
//				+ "  supply: " + Math.round(supply*100.0)/100.0 
//				+ "  requests: " + requests
//				+ "  aveSupply: " + Math.round(aveSupply*100.0)/100.0);

		return aveSupply;
	}

	/**
	 * Gets the total supply amount for the item resource
	 * 
	 * @param resource
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	public double getAverageItemSupply(int resource, double supplyStored, int solElapsed) {
		// Gets the total produced or supplied since last time
		double supply = getInventory().getItemSupply(resource);
		// Gets # of successful requests
		int requests = getInventory().getAmountSupplyRequest(resource);

		double aveSupply = Math.log(1 + supply * requests + supplyStored) / solElapsed;
	
//		if (resource == ItemResourceUtil.steelIngotID)
//			System.out.println("ais. " + resource 
//				+ "  " + ItemResourceUtil.findItemResourceName(resource)
//				+ "  supply: " + Math.round(supply*100.0)/100.0 
//				+ "  requests: " + requests
//				+ "  aveSupply: " + Math.round(aveSupply*100.0)/100.0);

		return aveSupply;
		
	}

	/**
	 * Gets the new demand
	 * 
	 * @param resource
	 * @param projectedDemand
	 * @param solElapsed
	 * @return
	 */
	public double getAverageAmountDemand(int resource, int solElapsed) {

		double aveDemand = 0;

		if (resource >= ResourceUtil.FIRST_AMOUNT_RESOURCE_ID && resource < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {

			Inventory inv = getInventory();
			// Gets the total demand on record
			double demand = inv.getAmountDemand(resource);
			// Gets the estimated demand on record
			double estDemand = inv.getAmountDemandEstimated(resource);
			// Gets # of successful requests
			int metRequests = inv.getAmountDemandMetRequest(resource);
			// Gets the total # of requests
			int totRequests = inv.getAmountDemandTotalRequest(resource);

			double demandPerMetRequest = 0;

			if (demand > MIN && metRequests > 0) {
				demandPerMetRequest = demand / (metRequests + 1);
				// Figure out the total potential demand based on good demand statistics
				aveDemand = demandPerMetRequest / solElapsed;
			}
			else {
				// Gets the total potential demand based on estimate
				aveDemand = estDemand / (totRequests + 1) / solElapsed;
			}

//			if (resource == ResourceUtil.regolithID)
//				System.out.println("aad. " + resource
//					+ "  " + ResourceUtil.findAmountResourceName(resource)
//					+ "  solElapsed: " + solElapsed
//					+ "  demand: " + Math.round(demand * 100.0)/100.0 
//					+ "  estDemand: " + Math.round(estDemand * 100.0)/100.0
//					+ "  metRequests: " + metRequests 
//					+ "  totRequests: " + totRequests 
//					+ "  demandPerMetRequest: " + demandPerMetRequest
//					+ "  aveDemand: " + Math.round(aveDemand * 100.0)/100.0 
//					);
		}

		return aveDemand;
	}

	/**
	 * Gets the new item demand
	 * 
	 * @param resource
	 * @param solElapsed
	 * @return
	 */
	public double getAverageItemDemand(int resource, int solElapsed) {

		double aveDemand = 0;

		if (resource >= ResourceUtil.FIRST_ITEM_RESOURCE_ID && resource < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {

			Inventory inv = getInventory();
			// Gets the total demand record
			double demand = inv.getItemDemand(resource);
			// Gets # of successful requests
			int metRequests = inv.getItemDemandMetRequest(resource);
			// Gets the total # of requests
			int totRequests = inv.getItemDemandTotalRequest(resource);
			// Gets the estimated demand on record
			double estDemand = inv.getItemDemandEstimated(resource);

			double demandPerMetRequest = 0;

			if (demand > MIN && metRequests > 0) {
				demandPerMetRequest = demand / (metRequests + 1);
				// Figure out the total potential demand based on good demand statistics
				aveDemand = demandPerMetRequest / solElapsed;
			}
			else {
				// Gets the total potential demand based on estimate
				aveDemand = estDemand / (totRequests + 1) / solElapsed;
			}
		
		
//		if (resource == ItemResourceUtil.steelIngotID)
//			System.out.println("aid. " + resource
//			+ "  " + ItemResourceUtil.findItemResourceName(resource)
//			+ "  solElapsed: " + solElapsed
//			+ "  demand: " + Math.round(demand * 100.0)/100.0 
//			+ "  estDemand: " + Math.round(estDemand * 100.0)/100.0
//			+ "  metRequests: " + metRequests 
//			+ "  totRequests: " + totRequests 
//			+ "  demandPerMetRequest: " + demandPerMetRequest
//			+ "  aveDemand: " + Math.round(aveDemand * 100.0)/100.0 
//			);

		}
		
		return aveDemand;
	}

	/**
	 * Gets the life support demand for an amount resource.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getLifeSupportDemand(int resource) {

		if (ResourceUtil.isLifeSupport(resource)) {
			double amountNeededSol = 0D;
			int numPeople = settlement.getNumCitizens();

			if (resource == ResourceUtil.oxygenID) {
				amountNeededSol = personConfig.getNominalO2ConsumptionRate() * OXYGEN_VALUE_MODIFIER;
			} else if (resource == ResourceUtil.waterID) {
				amountNeededSol = personConfig.getWaterConsumptionRate() * waterValue;
			} else if (resource == ResourceUtil.foodID) {
				amountNeededSol = personConfig.getFoodConsumptionRate() * FOOD_VALUE_MODIFIER;
			}

//			double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR * LIFE_SUPPORT_FACTOR;

			return numPeople * amountNeededSol * trade_factor * LIFE_SUPPORT_FACTOR;
			
		} else
			return 0;
	}

	/**
	 * Gets the fuel demand for an amount resource.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getFuelDemand(int resource) {
		double demand = 0;
		if (resource == ResourceUtil.methaneID) {
			int numPeople = settlement.getNumCitizens();
			Iterator<Vehicle> i = getAssociatedVehicles().iterator();
			while (i.hasNext()) {
//				double fuelDemand = i.next().getInventory().getAmountResourceCapacity(resource, false);
				demand += numPeople * transportation_factor * VEHICLE_FUEL_FACTOR;
			}	
//			double amountNeededOrbit = METHANE_AVERAGE_DEMAND * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
//			return 10 * Math.log(1 + numPeople) * amountNeededOrbit * FUEL_FACTOR * trade_factor;
			
			return demand;
		}

		else
			return 0D;
	}

	private double capLifeSupportAmountDemand(int resource, double demand) {
		if (resource == ResourceUtil.oxygenID || resource == ResourceUtil.waterID || resource == ResourceUtil.hydrogenID
				|| resource == ResourceUtil.methaneID)
			return Math.max(LIFE_SUPPORT_MIN, demand);
		return demand;
	}

	private double lowerLifeSupportSupply(int resource, double supply) {
		if (resource == ResourceUtil.oxygenID || resource == ResourceUtil.waterID || resource == ResourceUtil.hydrogenID
				|| resource == ResourceUtil.methaneID)
			return Math.log(1 + supply);// / LIFE_SUPPORT_FACTOR;
		return supply;
	}

	/**
	 * Gets a particular mineral demand
	 * 
	 * @param resource
	 * @param demand
	 * @return
	 */
	private double getMineralDemand(int resource, double demand) {
		double oldDemand = getAmountDemandValue(resource);
		if (resource == ResourceUtil.rockSaltID) {
			return (oldDemand + demand) / 2D;
		}

		else if (resource == ResourceUtil.epsomSaltID) {
			return (oldDemand + demand) / 2D;
		}

//		else if (resource == ResourceUtil.regolithID) {
//			return (oldDemand + demand) / 2D * REGOLITH_VALUE_MODIFIER;
//		}

		else if (resource == ResourceUtil.soilID) {
			return (oldDemand + demand) / 2D * settlement.getCropsNeedingTending();
		}

		else if (resource == ResourceUtil.sandID) {
			double regolithVP = 1 + goodsValues.get(ResourceUtil.regolithID);
			double sandVP = 1 + goodsValues.get(ResourceUtil.sandID);
			// the demand for sand is dragged up or down by that of regolith
			return (oldDemand + demand) / 2D * (.25 * regolithVP + .75 * sandVP) / sandVP * SAND_VALUE_MODIFIER;
		}

		else {
			double regolithVP = goodsValues.get(ResourceUtil.regolithID);
			double sandVP = 1 + goodsValues.get(ResourceUtil.sandID);

			for (int id : ResourceUtil.mineralConcIDs) {
				if (resource == id) {
					double vp = goodsValues.get(id);
					return (oldDemand + demand) / 2D * (.2 * regolithVP + .8 * vp) / vp * MINERAL_VALUE;
				}
			}

			for (int id : ResourceUtil.oreDepositIDs) {
				if (resource == id) {
					double vp = goodsValues.get(id);
					return (oldDemand + demand) / 2D * (.3 * regolithVP + .7 * vp) / vp * ORE_VALUE;
				}
			}

			if (resource == ResourceUtil.regolithBID || resource == ResourceUtil.regolithCID
					|| resource == ResourceUtil.regolithDID) {
				double vp = goodsValues.get(resource);
				return (oldDemand + demand) / 2D * (.3 * regolithVP + .7 * vp) / vp;// * REGOLITH_VALUE_MODIFIER;
			}

			String type = ResourceUtil.findAmountResource(resource).getType();
			if (type != null && type.equalsIgnoreCase("rock")) {
				double vp = goodsValues.get(resource);

				if (resource == ResourceUtil.findIDbyAmountResourceName("meteorite"))
					return (oldDemand + demand) / 2D * (.5 * regolithVP + .5 * vp) / vp * METEORITE_MODIFIER;
				else
					return (oldDemand + demand) / 2D * (.2 * sandVP + .8 * vp) / vp * ROCK_MODIFIER;
			}
		}

		return demand;
	}

	/**
	 * Adjusts the sink cost for various waste resources.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getWasteDisposalSinkCost(int resource, double demand) {
		double oldDemand = getAmountDemandValue(resource);
		if (resource == ResourceUtil.greyWaterID || resource == ResourceUtil.blackWaterID) {
			return (oldDemand + demand) / 2D * WASTE_WATER_VALUE;
		}

		if (resource == ResourceUtil.leavesID) {
			return (oldDemand + demand) / 2D * LEAVES_FACTOR;
		}

		if (resource == ResourceUtil.soilID) {
			return (oldDemand + demand) / 2D;
		}

//		if (resource == ResourceUtil.toxicWasteID) {
//			return (oldDemand + demand) / 2D;
//		}

		if (resource == ResourceUtil.coID) {
			return (oldDemand + demand) / 2D;
		}

		if (resource == ResourceUtil.foodWasteID) {
			return (oldDemand + demand) / 2D ;
		}

		if (resource == ResourceUtil.cropWasteID) {
			return (oldDemand + demand) / 2D ;
		}

		if (resource == ResourceUtil.compostID) {
			return (oldDemand + demand) / 2D * USEFUL_WASTE_VALUE;
		}

//		if (resource == ResourceUtil.eWasteID) {
//			return (oldDemand + demand) / 2D;
//		} 

		if (resource == ResourceUtil.co2ID) {
			return (oldDemand + demand) / 2D;
		}

		else {
			String type = ResourceUtil.findAmountResource(resource).getType();
			if (type != null && type.equalsIgnoreCase("waste")) {
				return (oldDemand + demand) / 2D * WASTE_VALUE;
			}

		}

		return 0;
	}

//    private double computeWaste(AmountResource resource) {
//    	if (wasteScore == 0) {
//	        double amountNeededOrbit = MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
//	        int numPeople = settlement.getAllAssociatedPeople().size();
//	        wasteScore = numPeople * amountNeededOrbit * WASTE_FACTOR * trade_factor;
//    	}
//    	return wasteScore;
//    }

	/**
	 * Gets the potable water usage demand for an amount resource.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getPotableWaterUsageDemand(int resource) {
		if (resource == ResourceUtil.waterID) {
			// Add the awareness of the water ration level in adjusting the water demand
			double waterRationLevel = settlement.getWaterRation();
			double amountNeededSol = personConfig.getWaterUsageRate();
			double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
			int numPeople = settlement.getNumCitizens();// .getIndoorPeopleCount();
			return numPeople * amountNeededOrbit * waterValue * trade_factor * (1 + waterRationLevel) * 10;
		}

		return 0;
	}

	/**
	 * Computes the ice demand.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double computeIceProjectedDemand(int resource) {
		if (resource == ResourceUtil.iceID) {
			double ice = getAmountDemandValue(resource);
			double water = getAmountDemandValue(ResourceUtil.waterID);
			// Use the water's VP and existing iceSupply to compute the ice demand
			double d = 1 + Math.min(96, settlement.getNumCitizens())
					* (.8 * water + .2 * ice) / (1 + ice) * ICE_VALUE_MODIFIER * waterValue;
//			System.out.println("water demand: " + Math.round(water*10.0)/10.0
//								+ "  ice demand: " + Math.round(ice*10.0)/10.0
//								+ "  ice new demand: " + Math.round(d*10.0)/10.0); 
			return d;
		}

		return 0;
	}

	/**
	 * Computes the regolith demand.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double computeRegolithProjectedDemand(int resource) {
		if (resource == ResourceUtil.regolithID) {
			double sand = 1 + getAmountDemandValue(ResourceUtil.sandID);
			double regolith = 1 + getAmountDemandValue(ResourceUtil.regolithID);
			// The sandVP influences regolithVP
			double d = 1 + Math.min(48, settlement.getNumCitizens()) 
					* (.8 * regolith + .2 * sand) / (1 + regolith)
					* REGOLITH_VALUE_MODIFIER;
//			System.out.println("sand demand: " + Math.round(sand*10.0)/10.0
//					+ "  regolith demand: " + Math.round(regolith*10.0)/10.0
//					+ "  regolith new demand: " + Math.round(d*10.0)/10.0); 
			return d;
		}

		return 0;
	}

	/**
	 * Gets the toilet tissue usage demand.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getToiletryUsageDemand(int resource) {
		if (resource == ResourceUtil.toiletTissueID) {
			double amountNeededSol = LivingAccommodations.TOILET_WASTE_PERSON_SOL;
			double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
			int numPeople = settlement.getIndoorPeopleCount();
			return numPeople * amountNeededOrbit;
		}

		return 0D;
	}

//	/**
//	 * Gets vehicle demand for an amount resource.
//	 * 
//	 * @param resource the resource to check.
//	 * @return demand (kg) for the resource.
//	 */
//	private double getVehicleDemand(int resource) {
//		double demand = 0D;
//		if (ResourceUtil.isLifeSupport(resource) || resource == ResourceUtil.methaneID) {
//			Iterator<Vehicle> i = getAssociatedVehicles().iterator();
//			while (i.hasNext()) {
//				double fuelDemand = i.next().getInventory().getAmountResourceCapacity(resource, false);
//				demand += fuelDemand * transportation_factor * VEHICLE_FUEL_FACTOR;
//			}
//		}
//		return demand;
//	}

	/**
	 * Gets all vehicles associated with the settlement.
	 * 
	 * @return collection of vehicles.
	 */
	private Collection<Vehicle> getAssociatedVehicles() {
		// Start with parked vehicles at settlement.
		Collection<Vehicle> vehicles = settlement.getParkedVehicles();

		// Add associated vehicles out on missions.
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !vehicles.contains(vehicle))
					vehicles.add(vehicle);
			}
		}

		return vehicles;
	}

	/**
	 * Gets the farming demand for the resource.
	 * 
	 * @param resource the resource to check.
	 * @return demand (kg) for the resource.
	 */
	private double getFarmingDemand(int resource) {
		double demand = 0D;

		// Determine demand for resource at each farming building at settlement.
		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.FARMING).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Farming farm = building.getFarming();
			demand += getIndividualFarmDemand(resource, farm);
		}

		// Tune demand with various factors
		demand = demand * FARMING_FACTOR * cropFarm_factor;

		return demand;
	}

	public void setCropFarmFactor(double value) {
		cropFarm_factor = value * CROPFARM_BASE;
	}

	public void setManufacturingFactor(double value) {
		manufacturing_factor = value * MANU_BASE;
	}

	public void setTransportationFactor(double value) {
		transportation_factor = value * TRANSPORT_BASE;
	}

	public void setResearchFactor(double value) {
		research_factor = value * RESEARCH_BASE;
	}

	public void setTradeFactor(double value) {
		trade_factor = value * TRADE_BASE;
	}

	// public void setFreeMarketFactor(double value) {
	// freeMarket_factor = value * freeMarket_factor;
	// }

	public void setTourismFactor(double value) {
		tourism_factor = value * TOURISM_BASE;
	}

	public double getCropFarmFactor() {
		return cropFarm_factor;
	}

	public double getManufacturingFactor() {
		return manufacturing_factor;
	}

	public double getTransportationFactor() {
		return transportation_factor;
	}

	public double getResearchFactor() {
		return research_factor;
	}

	public double getTradeFactor() {
		return trade_factor;
	}

	// public double getFreeMarketFactor() {
	// return freeMarket_factor;
	// }

	public double getTourismFactor() {
		return tourism_factor;
	}

	/**
	 * Gets the individual greenhouse demand
	 * 
	 * @param resource
	 * @param farm
	 * @return
	 */
	private double getIndividualFarmDemand(int resource, Farming farm) {

		double demand = 0D;

		double averageGrowingCyclesPerOrbit = farm.getAverageGrowingCyclesPerOrbit();
		double totalCropArea = farm.getGrowingArea();
		int solsInOrbit = MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
//		CropConfig cropConfig = simulationConfig.getCropConfiguration();

		if (resource == ResourceUtil.waterID) {
			// Average water consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getWaterConsumptionRate() * totalCropArea * solsInOrbit;
		} else if (resource == ResourceUtil.co2ID) {
			// Average co2 consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getCarbonDioxideConsumptionRate() * totalCropArea * solsInOrbit;
		} else if (resource == ResourceUtil.oxygenID) {
			// Average oxygen consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getOxygenConsumptionRate() * totalCropArea * solsInOrbit;
		} else if (resource == ResourceUtil.soilID) {
			// Estimate soil needed for average number of crop plantings for total growing
			// area.
			demand = Crop.NEW_SOIL_NEEDED_PER_SQM * totalCropArea * averageGrowingCyclesPerOrbit;
		} else if (resource == ResourceUtil.fertilizerID) {
			// Estimate fertilizer needed for average number of crop plantings for total
			// growing area.
			demand = Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM * totalCropArea * averageGrowingCyclesPerOrbit;
			// Estimate fertilizer needed when grey water not available.
			demand += Crop.FERTILIZER_NEEDED_WATERING * totalCropArea * 1000D * solsInOrbit;
		} else if (resource == ResourceUtil.greyWaterID) {
			// TODO: how to properly get rid of grey water? it should NOT be considered an
			// economically vital resource
			// Average grey water consumption rate of crops per orbit using total growing
			// area.
			// demand = cropConfig.getWaterConsumptionRate() * totalCropArea * solsInOrbit;
			demand = demand * WASTE_VALUE;
		}

		return demand;
	}

	/**
	 * Gets the crop demand
	 * 
	 * @param resource
	 * @return
	 */
	private double getCropDemand(int resource) {
		int numCropTypes = cropConfig.getNumCropTypes();
		double sum = 0;

		if (ResourceUtil.findAmountResourceName(resource).contains(Farming.TISSUE_CULTURE)) {
			// Average use of tissue culture at greenhouse each orbit.
			sum = Farming.TISSUE_PER_SQM * TISSUE_CULTURE_FACTOR;
		}

		else {
			for (String s : cropConfig.getCropTypeNames()) {
				if (ResourceUtil.findAmountResourceName(resource).equalsIgnoreCase(s)) {
					sum += Farming.TISSUE_PER_SQM * TISSUE_CULTURE_FACTOR / numCropTypes * CROP_FACTOR;
					break;
				}
			}
		}

		return sum;
	}

	/**
	 * Gets the demand for a resource from all automated resource processes at a
	 * settlement.
	 * 
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceProcessingDemand(Integer resource) {
		double demand = 0D;

		// Get all resource processes at settlement.
		Iterator<ResourceProcess> i = getResourceProcesses().iterator();
		while (i.hasNext()) {
			ResourceProcess process = i.next();
			double processDemand = getResourceProcessDemand(process, resource);
			demand += processDemand;
		}

		return demand;
	}

	/**
	 * Gets the demand for a resource from an automated resource process.
	 * 
	 * @param process  the resource process.
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceProcessDemand(ResourceProcess process, Integer resource) {
		double demand = 0D;

		Set<Integer> inputResources = process.getInputResources();
		Set<Integer> outputResources = process.getOutputResources();

		if (inputResources.contains(resource) && !process.isAmbientInputResource(resource)) {
			double outputValue = 0D;
			Iterator<Integer> i = outputResources.iterator();
			while (i.hasNext()) {
				Integer output = i.next();
				double outputRate = process.getMaxOutputResourceRate(output);
				if (!process.isWasteOutputResource(resource)) {
					outputValue += (getAmountDemandValue(GoodsUtil.getResourceGood(output)) * outputRate);
				}
			}

			double resourceInputRate = process.getMaxInputResourceRate(resource);

			// Determine value of required process power.
//			double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputValue - powerValue) * RESOURCE_PROCESSING_INPUT_FACTOR;

			if (totalInputsValue > 0D) {
				double demandMillisol = resourceInputRate;
				double demandSol = demandMillisol * 1000D;
				double demandOrbit = demandSol * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;

				demand = demandOrbit;
			}
		}

		return demand;
	}

	/**
	 * Get all resource processes at settlement.
	 * 
	 * @return list of resource processes.
	 */
	private List<ResourceProcess> getResourceProcesses() {
		List<ResourceProcess> processes = new ArrayList<ResourceProcess>();
		Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (building.hasFunction(FunctionType.RESOURCE_PROCESSING)) {
				ResourceProcessing processing = building.getResourceProcessing();
				processes.addAll(processing.getProcesses());
			}
		}
		return processes;
	}

	/**
	 * Gets the demand for an amount resource as an input in the settlement's
	 * manufacturing processes.
	 * 
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceManufacturingDemand(int resource) {
		double demand = 0D;

		// Get highest manufacturing tech level in settlement.
		if (ManufactureUtil.doesSettlementHaveManufacturing(settlement)) {
			int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
			Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechLevel(techLevel)
					.iterator();
			while (i.hasNext()) {
				double manufacturingDemand = getResourceManufacturingProcessDemand(resource, i.next());
				demand += manufacturingDemand;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for an amount resource as an input in the settlement's Food
	 * Production processes.
	 * 
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceFoodProductionDemand(int resource) {
		double demand = 0D;

		// Get highest Food Production tech level in settlement.
		if (FoodProductionUtil.doesSettlementHaveFoodProduction(settlement)) {
			int techLevel = FoodProductionUtil.getHighestFoodProductionTechLevel(settlement);
			Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getFoodProductionProcessesForTechLevel(techLevel)
					.iterator();
			while (i.hasNext()) {
				double FoodProductionDemand = getResourceFoodProductionProcessDemand(resource, i.next());
				demand += FoodProductionDemand;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for an input amount resource in a manufacturing process.
	 * 
	 * @param resource the amount resource.
	 * @param process  the manufacturing process.
	 * @return demand (kg)
	 */
	private double getResourceManufacturingProcessDemand(int resource, ManufactureProcessInfo process) {
		double demand = 0D;
		String r = ResourceUtil.findAmountResourceName(resource).toLowerCase();

		ManufactureProcessItem resourceInput = null;
		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while ((resourceInput == null) && i.hasNext()) {
			ManufactureProcessItem item = i.next();
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType()) && r.equals(item.getName())) {
				resourceInput = item;
				break;
			}
		}

		if (resourceInput != null) {
			double outputsValue = 0D;
			Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
			while (j.hasNext()) {
				outputsValue += ManufactureUtil.getManufactureProcessItemValue(j.next(), settlement, true);
			}

//			double resourceAmount = 0D;

			double totalItems = 0D;
			Iterator<ManufactureProcessItem> k = process.getInputList().iterator();
			while (k.hasNext()) {
				ManufactureProcessItem item = k.next();
				totalItems += item.getAmount();
//				if (r.equalsIgnoreCase(item.getName())) {
//					resourceAmount = item.getAmount();
//				}
			}

			// Determine value of required process power.
//			double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * trade_factor * manufacturing_factor
					* MANUFACTURING_INPUT_FACTOR;

			if (totalInputsValue > 0D) {
				demand = (1D / totalItems) * totalInputsValue;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for an input amount resource in a Food Production process.
	 * 
	 * @param resource the amount resource.
	 * @param process  the Food Production process.
	 * @return demand (kg)
	 */
	private double getResourceFoodProductionProcessDemand(int resource, FoodProductionProcessInfo process) {
		double demand = 0D;

		FoodProductionProcessItem resourceInput = null;
		Iterator<FoodProductionProcessItem> i = process.getInputList().iterator();
		while ((resourceInput == null) && i.hasNext()) {
			FoodProductionProcessItem item = i.next();
			if (ItemType.AMOUNT_RESOURCE.equals(item.getType())
					&& ResourceUtil.findAmountResourceName(resource).equalsIgnoreCase(item.getName())) {
				resourceInput = item;
				break;
			}
		}

		if (resourceInput != null) {
			double outputsValue = 0D;
			Iterator<FoodProductionProcessItem> j = process.getOutputList().iterator();
			while (j.hasNext()) {
				outputsValue += FoodProductionUtil.getFoodProductionProcessItemValue(j.next(), settlement, true);
			}

			double totalItems = 0D;
			Iterator<FoodProductionProcessItem> k = process.getInputList().iterator();
			while (k.hasNext()) {
				totalItems += k.next().getAmount();
			}

			// Determine value of required process power.
//			double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * trade_factor * cropFarm_factor
					* FOOD_PRODUCTION_INPUT_FACTOR;

			if (totalInputsValue > 0D) {
				demand = (1D / totalItems) * totalInputsValue;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a resource as a cooked meal ingredient.
	 * 
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceCookedMealIngredientDemand(int resource) {
		double demand = 0D;

		Set<AmountResource> set = ResourceUtil.getAmountResources().stream().filter(ar -> ar.isEdible() == true)
				.collect(Collectors.toSet());

		for (AmountResource ar : set) {
			int id = ar.getID();
			if (id == ResourceUtil.tableSaltID) {
				// Assuming a person takes 3 meals per sol
				return MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_SALT_PER_MEAL;
				// if (demand > TABLE_SALT_VALUE)
				// return TABLE_SALT_VALUE;
			}

			else {
				for (int oilID : Cooking.getOilMenu()) {
					if (id == oilID) {
						// Assuming a person takes 3 meals per sol
						return MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_OIL_PER_MEAL;
					}
				}

				// Determine total demand for cooked meal mass for the settlement.
				double cookedMealDemandSol = personConfig.getFoodConsumptionRate();
				double cookedMealDemandOrbit = cookedMealDemandSol * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
				int numPeople = settlement.getNumCitizens();
				double cookedMealDemand = numPeople * cookedMealDemandOrbit;

				// Determine demand for the resource as an ingredient for each cooked meal
				// recipe.
				int numMeals = MealConfig.getMealList().size();
				Iterator<HotMeal> i = MealConfig.getMealList().iterator();
				while (i.hasNext()) {
					HotMeal meal = i.next();
					Iterator<Ingredient> j = meal.getIngredientList().iterator();
					while (j.hasNext()) {
						Ingredient ingredient = j.next();
						if (id == ingredient.getAmountResourceID()) {
							demand += ingredient.getProportion() * cookedMealDemand / numMeals
									* COOKED_MEAL_INPUT_FACTOR;
						}
					}
				}
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a food dessert item.
	 * 
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */

	private double getResourceDessertDemand(int resource) {

		double demand = 0D;
		AmountResource[] dessert = PreparingDessert.getArrayOfDessertsAR();
		boolean hasDessert = false;

		if (dessert[0] != null) {
			for (AmountResource ar : dessert) {
				if (ar.getID() == resource) {
					hasDessert = true;
					break;
				}
			}

			if (hasDessert) {
				double amountNeededSol = personConfig.getDessertConsumptionRate() / dessert.length;
				double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
				int numPeople = settlement.getNumCitizens();
				demand = 5 * Math.log(1 + numPeople) * amountNeededOrbit * DESSERT_FACTOR;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a resource from construction sites.
	 * 
	 * @param resource the resource.
	 * @return demand (kg)
	 */
	private double getResourceConstructionSiteDemand(int resource) {

		double demand = 0D;

		// Add demand for resource required as remaining construction material on
		// construction sites.
		Iterator<ConstructionSite> i = settlement.getConstructionManager().getConstructionSites().iterator();
		while (i.hasNext()) {
			ConstructionSite site = i.next();
			if (site.hasUnfinishedStage() && !site.getCurrentConstructionStage().isSalvaging()) {
				ConstructionStage stage = site.getCurrentConstructionStage();
				if (stage.getRemainingResources().containsKey(resource)) {
					double requiredAmount = stage.getRemainingResources().get(resource);
					demand += requiredAmount * CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR;
				}
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for an amount resource as an input in building construction.
	 * 
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceConstructionDemand(Integer resource) {
		double demand = 0D;

		ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
		int bestConstructionSkill = ConstructionUtil.getBestConstructionSkillAtSettlement(settlement);
		Map<ConstructionStageInfo, Double> stageValues = values.getAllConstructionStageValues(bestConstructionSkill);
		Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
		while (i.hasNext()) {
			ConstructionStageInfo stage = i.next();
			double stageValue = stageValues.get(stage);
			if (stageValue > 0D && ConstructionStageInfo.BUILDING.equals(stage.getType())
					&& isLocallyConstructable(stage)) {
				double constructionDemand = getResourceConstructionStageDemand(resource, stage, stageValue);
				if (constructionDemand > 0D) {
					demand += constructionDemand;
				}
			}
		}

		return demand;
	}

	/**
	 * Checks if a building construction stage can be constructed at the local
	 * settlement.
	 * 
	 * @param buildingStage the building construction stage info.
	 * @return true if building can be constructed.
	 */
	private boolean isLocallyConstructable(ConstructionStageInfo buildingStage) {
		boolean result = false;

		if (buildingStage.isConstructable()) {
			ConstructionStageInfo frameStage = ConstructionUtil.getPrerequisiteStage(buildingStage);
			if (frameStage != null) {
				ConstructionStageInfo foundationStage = ConstructionUtil.getPrerequisiteStage(frameStage);
				if (foundationStage != null) {
					if (frameStage.isConstructable() && foundationStage.isConstructable()) {
						result = true;
					} else {
						// Check if any existing buildings have same frame stage and can be refit or
						// refurbished
						// into new building.
						Iterator<Building> i = settlement.getBuildingManager().getACopyOfBuildings().iterator();
						while (i.hasNext()) {
							ConstructionStageInfo tempBuildingStage = ConstructionUtil
									.getConstructionStageInfo(i.next().getBuildingType());
							if (tempBuildingStage != null) {
								ConstructionStageInfo tempFrameStage = ConstructionUtil
										.getPrerequisiteStage(tempBuildingStage);
								if (frameStage.equals(tempFrameStage)) {
									result = true;
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the demand for an amount resources as an input for a particular building
	 * construction stage.
	 * 
	 * @param resource   the amount resource.
	 * @param stage      the building construction stage.
	 * @param stageValue the building construction stage value (VP).
	 * @return demand (kg)
	 */
	private double getResourceConstructionStageDemand(Integer resource, ConstructionStageInfo stage,
			double stageValue) {
		double demand = 0D;

		double resourceAmount = getPrerequisiteConstructionResourceAmount(resource, stage);

		if (resourceAmount > 0D) {
			Map<Integer, Double> resources = getAllPrerequisiteConstructionResources(stage);
			Map<Integer, Integer> parts = getAllPrerequisiteConstructionParts(stage);

			double totalItems = 0D;

			Iterator<Integer> i = resources.keySet().iterator();
			while (i.hasNext()) {
				totalItems += resources.get(i.next());
			}

			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext()) {
				totalItems += parts.get(j.next());
			}

			double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;

			demand = (1D / totalItems) * totalInputsValue;
		}

		return demand;
	}

	/**
	 * Get all resource amounts required to build a stage including all pre-stages.
	 * 
	 * @param stage the stage.
	 * @return map of resources and their amounts (kg).
	 */
	private Map<Integer, Double> getAllPrerequisiteConstructionResources(ConstructionStageInfo stage) {

		// Start with all resources required to build stage.
		Map<Integer, Double> result = new HashMap<Integer, Double>(stage.getResources());

		// Add all resources required to build first prestage, if any.
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if ((preStage1 != null)) {
			Iterator<Integer> i = preStage1.getResources().keySet().iterator();
			while (i.hasNext()) {
				Integer resource = i.next();
				double amount = preStage1.getResources().get(resource);
				if (result.containsKey(resource)) {
					double totalAmount = result.get(resource) + amount;
					result.put(resource, totalAmount);
				} else {
					result.put(resource, amount);
				}
			}

			// Add all resources required to build second prestage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if ((preStage2 != null)) {
				Iterator<Integer> j = preStage2.getResources().keySet().iterator();
				while (j.hasNext()) {
					Integer resource = j.next();
					double amount = preStage2.getResources().get(resource);
					if (result.containsKey(resource)) {
						double totalAmount = result.get(resource) + amount;
						result.put(resource, totalAmount);
					} else {
						result.put(resource, amount);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the total amount of a given resource required to build a stage including
	 * all prestages.
	 * 
	 * @param resource the resource.
	 * @param stage    the stage.
	 * @return total amount (kg) of the resource.
	 */
	private double getPrerequisiteConstructionResourceAmount(Integer resource, ConstructionStageInfo stage) {

		double result = 0D;

		// Add resource amount needed for stage.
		Map<Integer, Double> stageResources = stage.getResources();
		if (stageResources.containsKey(resource)) {
			result += stageResources.get(resource);
		}

		// Add resource amount needed for first prestage, if any.
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if ((preStage1 != null) && preStage1.isConstructable()) {
			Map<Integer, Double> preStage1Resources = preStage1.getResources();
			if (preStage1Resources.containsKey(resource)) {
				result += preStage1Resources.get(resource);
			}

			// Add resource amount needed for second prestage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if ((preStage2 != null) && preStage2.isConstructable()) {
				Map<Integer, Double> preStage2Resources = preStage2.getResources();
				if (preStage2Resources.containsKey(resource)) {
					result += preStage2Resources.get(resource);
				}
			}
		}

		return result;
	}

	/**
	 * Gets a map of all parts required to build a stage including all prestages.
	 * 
	 * @param stage the stage.
	 * @return map of parts and their numbers.
	 */
	private Map<Integer, Integer> getAllPrerequisiteConstructionParts(ConstructionStageInfo stage) {

		// Start with all parts required to build stage.
		Map<Integer, Integer> result = new HashMap<Integer, Integer>(stage.getParts());

		// Add parts from first prestage, if any.
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if ((preStage1 != null)) {
			Iterator<Integer> i = preStage1.getParts().keySet().iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				int number = preStage1.getParts().get(part);
				if (result.containsKey(part)) {
					int totalNumber = result.get(part) + number;
					result.put(part, totalNumber);
				} else {
					result.put(part, number);
				}
			}

			// Add parts from second prestage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if ((preStage2 != null)) {
				Iterator<Integer> j = preStage2.getParts().keySet().iterator();
				while (j.hasNext()) {
					Integer part = j.next();
					int number = preStage2.getParts().get(part);
					if (result.containsKey(part)) {
						int totalNumber = result.get(part) + number;
						result.put(part, totalNumber);
					} else {
						result.put(part, number);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the total number of a given part required to build a stage including all
	 * prestages.
	 * 
	 * @param part  the part.
	 * @param stage the stage.
	 * @return total number of parts required.
	 */
	private int getPrerequisiteConstructionPartNum(Integer part, ConstructionStageInfo stage) {

		int result = 0;

		// Add all parts from stage.
		Map<Integer, Integer> stageParts = stage.getParts();
		if (stageParts.containsKey(part)) {
			result += stageParts.get(part);
		}

		// Add all parts from first prestage, if any.
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if ((preStage1 != null) && preStage1.isConstructable()) {
			Map<Integer, Integer> preStage1Parts = preStage1.getParts();
			if (preStage1Parts.containsKey(part)) {
				result += preStage1Parts.get(part);
			}

			// Add all parts from second prestage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if ((preStage2 != null) && preStage2.isConstructable()) {
				Map<Integer, Integer> preStage2Parts = preStage2.getParts();
				if (preStage2Parts.containsKey(part)) {
					result += preStage2Parts.get(part);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the number of a good being in use or being produced at this moment at
	 * the settlement.
	 * 
	 * @param good the good to check.
	 * @return the number of the good (or amount (kg) if amount resource good).
	 */
	public double getNumberOfGoodForSettlement(Good good) {
		if (good != null) {
			double result = 0D;

			if (GoodType.AMOUNT_RESOURCE == good.getCategory())
				result = getAmountOfResourceForSettlement(ResourceUtil.findAmountResource(good.getID()));
			else if (GoodType.ITEM_RESOURCE == good.getCategory())
				result = getNumItemResourceForSettlement(ItemResourceUtil.findItemResource(good.getID()));
			else if (GoodType.EQUIPMENT == good.getCategory())
				result = getNumberOfEquipmentForSettlement(good, EquipmentFactory.getEquipmentClass(good.getID()));
			else if (GoodType.VEHICLE == good.getCategory())
				result = getNumberOfVehiclesForSettlement(good.getName());

			return result;
		} else
			logger.severe(settlement, "Good is null.");

		return 0;
	}

	/**
	 * Gets the amount of an amount resource in use for a settlement.
	 * 
	 * @param resource the resource to check.
	 * @return amount (kg) of resource for the settlement.
	 */
	private double getAmountOfResourceForSettlement(AmountResource resource) {
		double amount = 0D;

		// Get amount of resource in settlement storage.
		amount += settlement.getInventory().getAmountResourceStored(resource, false);

		// Get amount of resource out on mission vehicles.
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					amount += vehicle.getInventory().getAmountResourceStored(resource, false);
			}
		}

		// Get amount of resource carried by people on EVA.
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.isOutside())
				amount += person.getInventory().getAmountResourceStored(resource, false);
		}

		// Get the amount of the resource that will be produced by ongoing manufacturing
		// processes.
		Good amountResourceGood = GoodsUtil.getResourceGood(resource);
		amount += getManufacturingProcessOutput(amountResourceGood);

		// Get the amount of the resource that will be produced by ongoing food
		// production processes.
		amount += getFoodProductionOutput(amountResourceGood);

		return amount;
	}

	/**
	 * Gets the amount of the good being produced at the settlement by ongoing food
	 * production.
	 * 
	 * @param good the good.
	 * @return amount (kg for amount resources, number for parts, equipment, and
	 *         vehicles).
	 */
	private double getFoodProductionOutput(Good good) {
		double result = 0D;

		// Get the amount of the resource that will be produced by ongoing food
		// production processes.
		Iterator<Building> p = settlement.getBuildingManager().getBuildings(FunctionType.FOOD_PRODUCTION).iterator();
		while (p.hasNext()) {
//			Building building = p.next();
//			FoodProduction kitchen = p.next().getFoodProduction();
			// Go through each ongoing food production process.
			Iterator<FoodProductionProcess> q = p.next().getFoodProduction().getProcesses().iterator();
			while (q.hasNext()) {
				FoodProductionProcess process = q.next();
				Iterator<FoodProductionProcessItem> r = process.getInfo().getOutputList().iterator();
				while (r.hasNext()) {
					FoodProductionProcessItem item = r.next();
					if (item.getName().equalsIgnoreCase(good.getName())) {
						result += item.getAmount();
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the amount of the good being produced at the settlement by ongoing
	 * manufacturing processes.
	 * 
	 * @param good the good.
	 * @return amount (kg for amount resources, number for parts, equipment, and
	 *         vehicles).
	 */
	private double getManufacturingProcessOutput(Good good) {

		double result = 0D;

		Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE).iterator();
		while (i.hasNext()) {
//			Building building = i.next();
//			Manufacture workshop = i.next().getManufacture();
			// Go through each ongoing manufacturing process.
			Iterator<ManufactureProcess> j = i.next().getManufacture().getProcesses().iterator();
			while (j.hasNext()) {
				ManufactureProcess process = j.next();
				Iterator<ManufactureProcessItem> k = process.getInfo().getOutputList().iterator();
				while (k.hasNext()) {
					ManufactureProcessItem item = k.next();
					if (item.getName().equalsIgnoreCase(good.getName())) {
						result += item.getAmount();
					}
				}
			}
		}

		return result;
	}

	/**
	 * Determines the value of an item resource.
	 * 
	 * @param resourceGood the resource good to check.
	 * @param supply       the current supply (# items) of the good.
	 * @param useCache     use the cache to determine value.
	 * @return value (Value Points / item)
	 */
	private double determineItemResourceGoodValue(Good resourceGood, double supply, boolean useCache) {
		double itemValue = 10;
		double itemDemand = 10;
		double totalItemDemand = 10;
		double previousItemDemand = 10;
		double projectedItemDemand = 10;
		double totalItemSupply = 10;

		// Needed for loading a saved sim
		int solElapsed = marsClock.getMissionSol();
		// Compact and/or clear supply and demand maps every x days
		int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;

		int id = resourceGood.getID();
		Part part = null;

		if (id >= ResourceUtil.FIRST_ITEM_RESOURCE_ID && id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {

			part = (Part) ItemResourceUtil.findItemResource(id);

			if (partDemandCache.containsKey(id)) {
				// Get previous demand
				previousItemDemand = partDemandCache.get(id);
			}

			if (useCache) {
				// Calculate total demand
				if (previousItemDemand > 0)
					totalItemDemand = 
							  .9 * previousItemDemand
							+ .1 * flattenRawPartDemand(part, getAverageItemDemand(id, numSol));
				// else
				// totalDemand = getAverageItemDemand(id, numSol);

				// Calculate total supply
				totalItemSupply = supply * .1;

				// Clear parts demand cache so it will be calculated next time.
//				partDemandCache.clear();
			}

			else {
				// Get demand for a part.

				// Recalculate the partsDemandCache
				if (partDemandCache.isEmpty())
					determineRepairPartsDemand();

				if (partDemandCache.containsKey(id))
					projectedItemDemand += partDemandCache.get(id);

				// NOTE: the following estimates are for each orbit (Martian year) :

				// Add manufacturing demand.
				projectedItemDemand += getPartManufacturingDemand(part);

				// Add food production demand.
				projectedItemDemand += getPartFoodProductionDemand(part);

				// Add construction demand.
				projectedItemDemand += getPartConstructionDemand(id);

				// Add construction site demand.
				projectedItemDemand += getPartConstructionSiteDemand(id);

				// Calculate individual EVA suit-related parts demand.
				projectedItemDemand = getEVASuitPartsDemand(projectedItemDemand, part);
				
				// Flatten the part for certain parts.
				projectedItemDemand = flattenRawPartDemand(part, projectedItemDemand);

				// Add trade demand.
				double tradeDemand = determineTradeDemand(resourceGood, useCache);
				if (itemDemand < tradeDemand) {
					itemDemand = tradeDemand;
				}

				if (previousItemDemand > 0)
					totalItemDemand = 
							  .85 * previousItemDemand
							+ .05 * projectedItemDemand / MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR
							+ .05 * getAverageItemDemand(id, numSol) 
							+ .05 * tradeDemand;
				// else
				// totalDemand =
				// + .6 * projectedDemand / MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR
				// + .2 * getAverageItemDemand(id, numSol)
				// + .2 * tradeDemand;

				// Calculate total supply
				totalItemSupply = getAverageItemSupply(id, supply * .1, solElapsed);
			}

			// Use MIMIMUM_STORED_SUPPLY instead of supply++ to avoid divide by zero when
			// calculating VP
			if (totalItemSupply < MIN_SUPPLY)
				totalItemSupply = MIN_SUPPLY;

			if (totalItemSupply > MAX_SUPPLY)
				totalItemSupply = MAX_SUPPLY;

			// Apply the universal damping ratio
			if (totalItemDemand / previousItemDemand > 1)
				// Reduce the increase
				totalItemDemand = previousItemDemand + (totalItemDemand - previousItemDemand) * DAMPING_RATIO;

			else // if (totalItemDemand / previousItemDemand < 1)
					// Reduce the decrease
				totalItemDemand = previousItemDemand - (previousItemDemand - totalItemDemand) * DAMPING_RATIO;

			if (totalItemDemand < MIN_DEMAND)
				totalItemDemand = MIN_DEMAND;

			if (totalItemDemand > MAX_DEMAND)
				totalItemDemand = MAX_DEMAND;

			// Save the goods demand
			partDemandCache.put(id, totalItemDemand);

			itemValue = totalItemDemand / totalItemSupply;

			// Save the value point
			goodsValues.put(id, itemValue);
		}

		return itemValue;
	}

	/**
	 * Determines the number demand for all parts at the settlement.
	 * 
	 * @return map of parts and their demand.
	 */
	private void determineRepairPartsDemand() {
		Map<Integer, Double> partsProbDemand = new HashMap<>(ItemResourceUtil.getItemIDs().size());

		// Get all malfunctionables associated with settlement.
		Iterator<Malfunctionable> i = MalfunctionFactory.getAssociatedMalfunctionables(settlement).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();

			// Determine wear condition modifier.
			double wearModifier = (entity.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;

			// Estimate repair parts needed per orbit for entity.
			sumPartsDemand(partsProbDemand, getEstimatedOrbitRepairParts(entity), wearModifier);

			// Add outstanding repair parts required.
			sumPartsDemand(partsProbDemand, getOutstandingRepairParts(entity), wearModifier);

			// Estimate maintenance parts needed per orbit for entity.
			sumPartsDemand(partsProbDemand, getEstimatedOrbitMaintenanceParts(entity), wearModifier);

			// Add outstanding maintenance parts required.
			sumPartsDemand(partsProbDemand, getOutstandingMaintenanceParts(entity), wearModifier);
		}

		// Add demand for vehicle attachment parts.
		sumPartsDemand(partsProbDemand, getVehicleAttachmentParts(), 1D);

		// Store in parts demand cache.
		Iterator<Integer> j = partsProbDemand.keySet().iterator();
		while (j.hasNext()) {
			Integer part = j.next();
			if (getPartDemandValue(part) < 1)
				partDemandCache.put(part, 1.0);
			else
				partDemandCache.put(part, partsProbDemand.get(part));
		}
	}

	/**
	 * Sums the additional parts number map into a total parts number map.
	 * 
	 * @param totalPartsDemand      the total parts number.
	 * @param additionalPartsDemand the additional parts number.
	 * @param multiplier            the multiplier for the additional parts number.
	 */
	private void sumPartsDemand(Map<Integer, Double> totalPartsDemand, Map<Integer, Number> additionalPartsDemand,
			double multiplier) {
		Iterator<Integer> i = additionalPartsDemand.keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			double number = additionalPartsDemand.get(part).doubleValue() * multiplier;
			if (totalPartsDemand.containsKey(part))
				number += totalPartsDemand.get(part);
			totalPartsDemand.put(part, number);
		}
	}

	private Map<Integer, Number> getEstimatedOrbitRepairParts(Malfunctionable entity) {
		Map<Integer, Number> result = new HashMap<>();

		MalfunctionManager manager = entity.getMalfunctionManager();

		// Estimate number of malfunctions for entity per orbit.
		double orbitMalfunctions = manager.getEstimatedNumberOfMalfunctionsPerOrbit();

		// Estimate parts needed per malfunction.
		Map<Integer, Double> partsPerMalfunction = manager.getRepairPartProbabilities();

		// Multiply parts needed by malfunctions per orbit.
		Iterator<Integer> i = partsPerMalfunction.keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			result.put(part, partsPerMalfunction.get(part) * orbitMalfunctions);
		}

		return result;
	}

	private Map<Integer, Number> getOutstandingRepairParts(Malfunctionable entity) {
		Map<Integer, Number> result = new HashMap<>(0);

		Iterator<Malfunction> i = entity.getMalfunctionManager().getMalfunctions().iterator();
		while (i.hasNext()) {
			Malfunction malfunction = i.next();
			Map<Integer, Integer> repairParts = malfunction.getRepairParts();
			Iterator<Integer> j = repairParts.keySet().iterator();
			while (j.hasNext()) {
				Integer part = j.next();
				int number = (int) Math.round(repairParts.get(part) * repairMod);
				if (result.containsKey(part))
					number += result.get(part).intValue();
				result.put(part, number);
			}
		}

		return result;
	}

	private Map<Integer, Number> getEstimatedOrbitMaintenanceParts(Malfunctionable entity) {
		Map<Integer, Number> result = new HashMap<>();

		MalfunctionManager manager = entity.getMalfunctionManager();

		// Estimate number of maintenances for entity per orbit.
		double orbitMaintenances = manager.getEstimatedNumberOfMaintenancesPerOrbit();

		// Estimate parts needed per maintenance.
		Map<Integer, Double> partsPerMaintenance = manager.getMaintenancePartProbabilities();

		// Multiply parts needed by maintenances per orbit.
		Iterator<Integer> i = partsPerMaintenance.keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			result.put(part, partsPerMaintenance.get(part) * orbitMaintenances);
		}

		return result;
	}

	private Map<Integer, Number> getOutstandingMaintenanceParts(Malfunctionable entity) {
		Map<Integer, Number> result = new HashMap<>();

		Map<Integer, Integer> maintParts = entity.getMalfunctionManager().getMaintenanceParts();
		Iterator<Integer> i = maintParts.keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			int number = (int) Math.round(maintParts.get(part) * maintenanceMod);
			result.put(part, number);

			// Add item demand
			settlement.getInventory().addItemDemandTotalRequest(part, number);
			settlement.getInventory().addItemDemand(part, number);
		}

		return result;
	}

	/**
	 * Gets the part demand for vehicle attachments.
	 * 
	 * @return map of parts and demand number.
	 */
	private Map<Integer, Number> getVehicleAttachmentParts() {
		Map<Integer, Number> result = new HashMap<>();

		// VehicleConfig vehicleConfig = simulationConfig.getVehicleConfiguration();
		Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
		while (i.hasNext()) {
			String type = i.next().getDescription().toLowerCase();
			if (vehicleConfig.hasPartAttachments(type)) {
				Iterator<Part> j = vehicleConfig.getAttachableParts(type).iterator();
				while (j.hasNext()) {
					Part part = j.next();
					int demand = ATTACHMENT_PARTS_DEMAND;
					if (result.containsKey(part.getID()))
						demand += result.get(part.getID()).intValue();
					result.put(ItemResourceUtil.findIDbyItemResourceName(part.getName()), demand);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the eva related demand for a part.
	 * 
	 * @param part the part.
	 * @return demand
	 */
	private double getEVASuitPartsDemand(double demand, Part part) {
		for (String s : EVASuit.getParts()) {
			if (part.getName().equalsIgnoreCase(s)) {
				int id = ItemResourceUtil.findIDbyItemResourceName(s);
				double d = getPartDemandValue(id);
				return (demand + d) / 2 * eVASuitMod * EVA_PARTS_VALUE;
			}
		}
		return demand;
	}

	/**
	 * Gets the EVA suit demand from its part.
	 * 
	 * @param part the part.
	 * @return demand
	 */
	private double getWholeEVASuitDemand() {
		double sum = 0;
		for (String s : EVASuit.getParts()) {
			int id = ItemResourceUtil.findIDbyItemResourceName(s);
			double d = getPartDemandValue(id);
			sum += d;
		}
		return sum;
	}

	/**
	 * Gets the manufacturing demand for a part.
	 * 
	 * @param part the part.
	 * @return demand (# of parts)
	 */
	private double getPartManufacturingDemand(Part part) {
		double demand = 0D;

		// Get highest manufacturing tech level in settlement.
		if (ManufactureUtil.doesSettlementHaveManufacturing(settlement)) {
			int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
			Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechLevel(techLevel)
					.iterator();
			while (i.hasNext()) {
				double manufacturingDemand = getPartManufacturingProcessDemand(part, i.next());
				demand += manufacturingDemand;
			}
		}
		return demand;
	}

	/**
	 * Limit the demand for a particular part.
	 * 
	 * @param part   the part.
	 * @param demand the original demand.
	 * @return the flattened demand
	 */
	private double flattenRawPartDemand(Part part, double demand) {
		// Reduce the demand on the steel/aluminum scrap metal
		// since they can only be produced by salvaging a vehicle
		// therefore it's not reasonable to have high VP
		double oldDemand = getPartDemandValue(part.getID());
		if (part.getName().contains(SCRAP))
			return (oldDemand + demand) / 2 * SCRAP_METAL_DEMAND;
		// May recycle the steel/AL scrap back to ingot
		// Note: the VP of a scrap metal could be heavily influence by VP of regolith

		if (part.getName().contains(INGOT))
			return (oldDemand + demand) / 2 * INGOT_METAL_DEMAND;

		if (part.getName().contains(SHEET))
			return (oldDemand + demand) / 2 * SHEET_METAL_DEMAND;

		if (part.getName().equalsIgnoreCase(TRUSS))
			return (oldDemand + demand) / 2 * SHEET_METAL_DEMAND;

		if (part.getName().equalsIgnoreCase(STEEL_WIRE))
			return (oldDemand + demand) / 2 * STEEL_WIRE_DEMAND;

		if (part.getName().equalsIgnoreCase(AL_WIRE))
			return (oldDemand + demand) / 2 * AL_WIRE_DEMAND;

		if (part.getName().equalsIgnoreCase(STEEL_CAN))
			return (oldDemand + demand) / 2 * STEEL_CAN_DEMAND;

		if (part.getName().equalsIgnoreCase(BOTTLE))
			return (oldDemand + demand) / 2 * BOTTLE_DEMAND;

		if (part.getName().equalsIgnoreCase(FIBERGLASS_CLOTH))
			return (oldDemand + demand) / 2 * FIBERGLASS_DEMAND;

		if (part.getName().equalsIgnoreCase(FIBERGLASS))
			return (oldDemand + demand) / 2 * FIBERGLASS_DEMAND;

		if (part.getName().equalsIgnoreCase(BRICK))
			return (oldDemand + demand) / 2 * BRICK_DEMAND;

		return flattenKitchenPartDemand(part, demand);

	}

	/**
	 * Limit the demand for kitchen parts.
	 * 
	 * @param part   the part.
	 * @param demand the original demand.
	 * @return the flattened demand
	 */
	private double flattenKitchenPartDemand(Part part, double demand) {
		double oldDemand = getPartDemandValue(part.getID());
		for (String s : KITCHEN_WARE) {
			if (part.getName().equalsIgnoreCase(s))
				return (oldDemand + demand) / 2 * KITCHEN_DEMAND;
		}
		return demand;
	}

	/**
	 * Gets the demand of an input part in a manufacturing process.
	 * 
	 * @param part    the input part.
	 * @param process the manufacturing process.
	 * @return demand (# of parts)
	 */
	private double getPartManufacturingProcessDemand(Part part, ManufactureProcessInfo process) {
		double demand = 0D;
		double totalInputNum = 0D;

		ManufactureProcessItem partInput = null;
		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while (i.hasNext()) {
			ManufactureProcessItem item = i.next();
//			if (ItemType.PART == item.getType() && 
			if (part.getName().equalsIgnoreCase(item.getName())) {
				partInput = item;
			}
			totalInputNum += item.getAmount();
		}

		if (partInput != null) {

			double outputsValue = 0D;
			Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
			while (j.hasNext()) {
				ManufactureProcessItem item = j.next();
				if (!process.getInputList().contains(item)) {
					outputsValue += ManufactureUtil.getManufactureProcessItemValue(item, settlement, true);
				}
			}

			// Determine value of required process power.
//			double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 3600D;
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * trade_factor * manufacturing_factor
					* MANUFACTURING_INPUT_FACTOR;

			if (totalInputsValue > 0D) {
				double partNum = partInput.getAmount();

				demand = totalInputsValue * (partNum / totalInputNum);
			}
		}

		return demand;
	}

	/**
	 * Gets the Food Production demand for a part.
	 * 
	 * @param part the part.
	 * @return demand (# of parts)
	 */
	private double getPartFoodProductionDemand(Part part) {
		double demand = 0D;

		// Get highest Food Production tech level in settlement.
		if (FoodProductionUtil.doesSettlementHaveFoodProduction(settlement)) {
			int techLevel = FoodProductionUtil.getHighestFoodProductionTechLevel(settlement);
			Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getFoodProductionProcessesForTechLevel(techLevel)
					.iterator();
			while (i.hasNext()) {
				double FoodProductionDemand = getPartFoodProductionProcessDemand(part, i.next());
				demand += FoodProductionDemand;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand of an input part in a Food Production process.
	 * 
	 * @param part    the input part.
	 * @param process the Food Production process.
	 * @return demand (# of parts)
	 */
	private double getPartFoodProductionProcessDemand(Part part, FoodProductionProcessInfo process) {
		double demand = 0D;
		double totalInputNum = 0D;

		FoodProductionProcessItem partInput = null;
		Iterator<FoodProductionProcessItem> i = process.getInputList().iterator();
		while (i.hasNext()) {
			FoodProductionProcessItem item = i.next();
			if (ItemType.PART.equals(item.getType()) && part.getName().equalsIgnoreCase(item.getName())) {
				partInput = item;
			}
			totalInputNum += item.getAmount();
		}

		if (partInput != null) {

			double outputsValue = 0D;
			Iterator<FoodProductionProcessItem> j = process.getOutputList().iterator();
			while (j.hasNext()) {
				FoodProductionProcessItem item = j.next();
				if (!process.getInputList().contains(item)) {
					outputsValue += FoodProductionUtil.getFoodProductionProcessItemValue(item, settlement, true);
				}
			}

			// Determine value of required process power.
//			double hoursInMillisol = MarsClock.convertMillisolsToSeconds(1D) / 60D / 60D;
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * trade_factor * cropFarm_factor
					* FOOD_PRODUCTION_INPUT_FACTOR;
			if (totalInputsValue > 0D) {
				double partNum = partInput.getAmount();
				demand = totalInputsValue * (partNum / totalInputNum);
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a part from construction sites.
	 * 
	 * @param part the part.
	 * @return demand (# of parts).
	 */
	private double getPartConstructionSiteDemand(int id) {

		double demand = 0D;

		// Add demand for part required as remaining construction material on
		// construction sites.
		Iterator<ConstructionSite> i = settlement.getConstructionManager().getConstructionSites().iterator();
		while (i.hasNext()) {
			ConstructionSite site = i.next();
			if (site.hasUnfinishedStage() && !site.getCurrentConstructionStage().isSalvaging()) {
				ConstructionStage stage = site.getCurrentConstructionStage();
				if (stage.getRemainingParts().containsKey(id)) {
					int requiredNum = stage.getRemainingParts().get(id);
					demand += requiredNum * CONSTRUCTION_SITE_REQUIRED_PART_FACTOR;
				}
			}
		}

		return demand;
	}

	/**
	 * Gets the construction demand for a part.
	 * 
	 * @param part the part.
	 * @return demand (# of parts).
	 */
	private double getPartConstructionDemand(int id) {
		double demand = 0D;

		ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
		int bestConstructionSkill = ConstructionUtil.getBestConstructionSkillAtSettlement(settlement);
		Map<ConstructionStageInfo, Double> stageValues = values.getAllConstructionStageValues(bestConstructionSkill);
		Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
		while (i.hasNext()) {
			ConstructionStageInfo stage = i.next();
			double stageValue = stageValues.get(stage);
			if (stageValue > 0D && ConstructionStageInfo.BUILDING.equals(stage.getType())
					&& isLocallyConstructable(stage)) {
				double constructionStageDemand = getPartConstructionStageDemand(id, stage, stageValue);
				if (constructionStageDemand > 0D) {
					demand += constructionStageDemand;
				}
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a part as an input for a particular building construction
	 * stage.
	 * 
	 * @param part       the part.
	 * @param stage      the building construction stage.
	 * @param stageValue the building construction stage value (VP).
	 * @return demand (# of parts).
	 */
	private double getPartConstructionStageDemand(int part, ConstructionStageInfo stage, double stageValue) {
		double demand = 0D;

		int partNumber = getPrerequisiteConstructionPartNum(part, stage);

		if (partNumber > 0) {
			Map<Integer, Double> resources = getAllPrerequisiteConstructionResources(stage);
			Map<Integer, Integer> parts = getAllPrerequisiteConstructionParts(stage);

			double totalNumber = 0D;

			Iterator<Integer> i = resources.keySet().iterator();
			while (i.hasNext())
				totalNumber += resources.get(i.next());

			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext())
				totalNumber += parts.get(j.next());

			double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;

			demand = totalInputsValue * (partNumber / totalNumber);
		}

		return demand;
	}

	/**
	 * Gets the number of an item resource in use for a settlement.
	 * 
	 * @param resource the resource to check.
	 * @return number of resource for the settlement.
	 */
	private double getNumItemResourceForSettlement(ItemResource resource) {
		double number = 0D;

		// Get number of resources in settlement storage.
		number += settlement.getInventory().getItemResourceNum(resource);

		// Get number of resources out on mission vehicles.
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					number += vehicle.getInventory().getItemResourceNum(resource);
			}
		}

		// Get number of resources carried by people on EVA.
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.isOutside())
				number += person.getInventory().getItemResourceNum(resource);
		}

		// Get the number of resources that will be produced by ongoing manufacturing
		// processes.
		Good amountResourceGood = GoodsUtil.getResourceGood(resource);
		number += getManufacturingProcessOutput(amountResourceGood);

		number += getFoodProductionOutput(amountResourceGood);

		return number;
	}

	/**
	 * Determines the value of an equipment.
	 * 
	 * @param equipmentGood the equipment good to check.
	 * @param supply        the current supply (# of items) of the good.
	 * @param useCache      use the cache to determine value.
	 * @return the value (value points)
	 */
	private double determineEquipmentGoodValue(Good equipmentGood, double supply, boolean useCache) {
		double value = 10D;
		double demand = 10D;

		int id = equipmentGood.getID();

		if (useCache) {
			demand = getEquipmentDemandValue(id);

		} else {
			// Determine demand amount.
			demand += determineEquipmentDemand(EquipmentFactory.getEquipmentClass(equipmentGood.getID()));

			// Add trade demand.
//            demand += determineTradeDemand(equipmentGood, useCache);
			double tradeDemand = determineTradeDemand(equipmentGood, useCache);
			if (tradeDemand > demand) {
				demand = tradeDemand;
			}

			equipmentDemandCache.put(id, demand);
		}

		value = demand / Math.log(supply + 1D);

		// Save the value point
		goodsValues.put(id, value);
		
		return value;
	}

	/**
	 * Determines the demand for a type of equipment.
	 * 
	 * @param equipmentClass the equipment class.
	 * @return demand (# of equipment).
	 */
	private double determineEquipmentDemand(Class<? extends Equipment> equipmentClass) {
		double demand = 1;

		int areologistFactor = getJobNum(JobType.AREOLOGIST) + 1;

		if (Robot.class.equals(equipmentClass))
			demand *= ROBOT_FACTOR;

		// Determine number of EVA suits that are needed
		if (EVASuit.class.equals(equipmentClass)) {
			// Add the whole EVA Suit demand.
			demand += getWholeEVASuitDemand();

			demand *= eVASuitMod * EVA_SUIT_VALUE; // 2D * settlement.getNumCitizens() * eVASuitMod + EVA_SUIT_VALUE;
		}

		// Determine the number of containers that are needed.
		if (Container.class.isAssignableFrom(equipmentClass) && !SpecimenBox.class.equals(equipmentClass)) {

			PhaseType containerPhase = ContainerUtil.getContainerPhase(equipmentClass);
			double containerCapacity = ContainerUtil.getContainerCapacity(equipmentClass);

			double totalPhaseOverfill = 0D;
			Iterator<AmountResource> i = ResourceUtil.getAmountResources().iterator();
			while (i.hasNext()) {
				AmountResource resource = i.next();
				if (resource.getPhase() == containerPhase) {
					double settlementCapacity = settlement.getInventory()
							.getAmountResourceCapacityNoContainers(resource);

					double resourceDemand = getAmountDemandValue(resource.getID());

					if (resourceDemand > settlementCapacity) {
						double resourceOverfill = resourceDemand - settlementCapacity;
						totalPhaseOverfill += resourceOverfill;
					}
				}
			}

			demand *= totalPhaseOverfill * containerCapacity / 100D;
		}

		if (demand < .1)
			demand = .1;

//		double regolithValue = getGoodValuePerItem(ResourceUtil.regolithID);

		// Determine number of bags that are needed.
		if (Bag.class.equals(equipmentClass)) {
			demand *= DigLocalRegolith.BASE_COLLECTION_RATE * areologistFactor * BAG_DEMAND;
		}

		if (LargeBag.class.equals(equipmentClass)) {
			demand *= CollectRegolith.REQUIRED_LARGE_BAGS * areologistFactor * LARGE_BAG_DEMAND;
		}

		if (Barrel.class.equals(equipmentClass)) {
//			double iceValue = Math.log(getGoodValuePerItem(ResourceUtil.iceID));
			demand *= CollectIce.REQUIRED_BARRELS * areologistFactor * BARREL_DEMAND;
		}

		// Determine number of specimen containers that are needed.
		if (SpecimenBox.class.equals(equipmentClass)) {
			demand *= Exploration.REQUIRED_SPECIMEN_CONTAINERS * areologistFactor * SPECIMEN_BOX_DEMAND;
		}

		// Determine number of gas canisters that are needed.
		if (GasCanister.class.equals(equipmentClass)) {
			demand *= PROJECTED_GAS_CANISTERS * areologistFactor * GAS_CANISTER_DEMAND;
		}

		return demand;
	}

//    /**
//     * Gets all non-empty containers of a given type associated with this settlement.
//     * @param equipmentClass the equipment type.
//     * @return number of non-empty containers.
//     */
//    private int getNonEmptyContainers(Class<? extends Equipment> equipmentClass) {
//        int result = 0;
//
//        Inventory inv = settlement.getSettlementInventory();
//        Collection<Unit> equipmentList = inv.findAllUnitsOfClass(equipmentClass);
//        MissionManager missionManager = missionManager;
//        Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
//        while (i.hasNext()) {
//            Mission mission = i.next();
//            if (mission instanceof VehicleMission) {
//                Vehicle vehicle = ((VehicleMission) mission).getVehicle();
//                if ((vehicle != null) && (vehicle.getSettlement() == null)) {
//                    Inventory vehicleInv = vehicle.getSettlementInventory();
//                    Iterator <Unit> j = vehicleInv.findAllUnitsOfClass(equipmentClass).iterator();
//                    while (j.hasNext()) {
//                        Unit equipment = j.next();
//                        if (!equipmentList.contains(equipment)) equipmentList.add(equipment);
//                    }
//                }
//            }
//        }
//
//        Iterator<Unit> k = equipmentList.iterator();
//        while (k.hasNext()) {
//            if (k.next().getSettlementInventory().getAllAmountResourcesStored(false).size() > 0D) result++;
//        }
//
//        return result;
//    }

	/**
	 * Gets the number of people in a job associated with the settlement.
	 * 
	 * @return number of people
	 */
	private int getJobNum(JobType jobType) {
		int result = 0;
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			if (i.next().getMind().getJob() == jobType)
				result++;
		}
		return result;
	}

	/**
	 * Gets the number of equipment for a settlement.
	 * 
	 * @param equipmentClass the equipmentType to check.
	 * @return number of equipment for the settlement.
	 */
	private <T extends Unit> double getNumberOfEquipmentForSettlement(Good good, Class<T> equipmentClass) {
//		if (equipmentClass.equals(Robot.class))
//			return ROBOT_FACTOR;

		double number = 0D;

		// Get number of the equipment in settlement storage.
		number += settlement.getInventory().findNumEmptyUnitsOfClass(equipmentClass, false);

		// Get number of equipment out on mission vehicles.
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					number += vehicle.getInventory().findNumEmptyUnitsOfClass(equipmentClass, false);
			}
		}

		// Get number of equipment carried by people on EVA.
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.isOutside())
				number += person.getInventory().findNumEmptyUnitsOfClass(equipmentClass, false);
		}

		// Get the number of equipment that will be produced by ongoing manufacturing
		// processes.
//		Good equipmentGood = GoodsUtil.getEquipmentGood(equipmentClass);
		number += getManufacturingProcessOutput(good);

		return number;
	}

	/**
	 * Determines the value of a vehicle good.
	 * 
	 * @param vehicleGood the vehicle good.
	 * @param supply      the current supply (# of vehicles) of the good.
	 * @param useCache    use the cache to determine value.
	 * @return the value (value points).
	 */
	private double determineVehicleGoodValue(Good vehicleGood, double supply, boolean useCache) {
		double value = 0D;

		String vehicleType = vehicleGood.getName();

		boolean buy = false;
		double currentNum = getNumberOfVehiclesForSettlement(vehicleType);
		if (supply == currentNum) {
			buy = true;
		}

		if (useCache) {
			if (buy) {
				if (vehicleBuyValueCache.containsKey(vehicleType)) {
					value = vehicleBuyValueCache.get(vehicleType);
				} else {
					value = determineVehicleGoodValue(vehicleGood, supply, false);
				}
			} else {
				if (vehicleSellValueCache.containsKey(vehicleType)) {
					value = vehicleSellValueCache.get(vehicleType);
				} else {
					value = determineVehicleGoodValue(vehicleGood, supply, false);
				}
			}
			
		} else {
			
			double demandValue = determineVehicleDemand(vehicleGood, useCache);
			
			double tradeValue = determineTradeVehicleValue(vehicleGood, useCache);
			if (tradeValue > demandValue) {
				demandValue = tradeValue;
			}

			if (buy) {
				vehicleBuyValueCache.put(vehicleType, demandValue);
			} else {
				vehicleSellValueCache.put(vehicleType, demandValue);
			}
			
			value = demandValue;
			
			// Save the value point
			goodsValues.put(vehicleGood.getID(), value);
		}

		return value;
	}	
	
	
	/**
	 * Determines the vehicle demand
	 * 
	 * @param vehicleGood
	 * @param useCache
	 * @return the vehicle demand
	 */
	private double determineVehicleDemand(Good vehicleGood, boolean useCache) {
		double value = 0D;
		
		String vehicleType = vehicleGood.getName();

		if (useCache) {
			value = vehicleDemandCache.get(vehicleGood.getID());
		}
		else {
			boolean buy = false;

			if (vehicleType.equalsIgnoreCase(LightUtilityVehicle.NAME)) {
				value = determineLUVValue(buy);
			} 
			
			else if (vehicleType.equalsIgnoreCase(Drone.NAME)) {
				double tradeMissionValue = determineMissionVehicleValue(TRADE_MISSION, vehicleType, buy);
				if (tradeMissionValue > value) {
					value = tradeMissionValue;
				}
				value += determineDroneValue(buy);
			} 
			
			else {
				double travelToSettlementMissionValue = determineMissionVehicleValue(TRAVEL_TO_SETTLEMENT_MISSION,
						vehicleType, buy);
				if (travelToSettlementMissionValue > value) {
					value = travelToSettlementMissionValue;
				}

				double explorationMissionValue = determineMissionVehicleValue(EXPLORATION_MISSION, vehicleType, buy);
				if (explorationMissionValue > value) {
					value = explorationMissionValue;
				}

				double collectIceMissionValue = determineMissionVehicleValue(COLLECT_ICE_MISSION, vehicleType, buy);
				if (collectIceMissionValue > value) {
					value = collectIceMissionValue;
				}

				double rescueMissionValue = determineMissionVehicleValue(RESCUE_SALVAGE_MISSION, vehicleType, buy);
				if (rescueMissionValue > value) {
					value = rescueMissionValue;
				}

				double tradeMissionValue = determineMissionVehicleValue(TRADE_MISSION, vehicleType, buy);
				if (tradeMissionValue > value) {
					value = tradeMissionValue;
				}

				double collectRegolithMissionValue = determineMissionVehicleValue(COLLECT_REGOLITH_MISSION, vehicleType,
						buy);
				if (collectRegolithMissionValue > value) {
					value = collectRegolithMissionValue;
				}

				double miningMissionValue = determineMissionVehicleValue(MINING_MISSION, vehicleType, buy);
				if (miningMissionValue > value) {
					value = miningMissionValue;
				}

				double constructionMissionValue = determineMissionVehicleValue(CONSTRUCT_BUILDING_MISSION, vehicleType,
						buy);
				if (constructionMissionValue > value) {
					value = constructionMissionValue;
				}

				double salvageMissionValue = determineMissionVehicleValue(SALVAGE_BUILDING_MISSION, vehicleType, buy);
				if (salvageMissionValue > value) {
					value = salvageMissionValue;
				}

				double areologyFieldMissionValue = determineMissionVehicleValue(AREOLOGY_STUDY_FIELD_MISSION,
						vehicleType, buy);
				if (areologyFieldMissionValue > value) {
					value = areologyFieldMissionValue;
				}

				double biologyFieldMissionValue = determineMissionVehicleValue(BIOLOGY_STUDY_FIELD_MISSION, vehicleType,
						buy);
				if (biologyFieldMissionValue > value) {
					value = biologyFieldMissionValue;
				}

				double emergencySupplyMissionValue = determineMissionVehicleValue(EMERGENCY_SUPPLY_MISSION, vehicleType,
						buy);
				if (emergencySupplyMissionValue > value) {
					value = emergencySupplyMissionValue;
				}
			}

			if (vehicleType.equalsIgnoreCase(VehicleType.CARGO_ROVER.getName()))
				value *= (.5 + transportation_factor) * CARGO_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.TRANSPORT_ROVER.getName()))
				value *= (.5 + transportation_factor) * TRANSPORT_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.EXPLORER_ROVER.getName()))
				value *= (.5 + transportation_factor) * EXPLORER_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.DELIVERY_DRONE.getName()))
				value *= (.5 + transportation_factor) * DRONE_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.LUV.getName()))
				value *= (.5 + transportation_factor) * LUV_VEHICLE_FACTOR;

			vehicleDemandCache.put(vehicleGood.getID(), value);
		}

		return value;
	}

	/**
	 * Determines the trade vehicle value
	 * 
	 * @param vehicleGood
	 * @param useCache
	 * @return the trade vehicle value
	 */
	private double determineTradeVehicleValue(Good vehicleGood, boolean useCache) {
		double tradeDemand = determineTradeDemand(vehicleGood, useCache);
		double supply = getNumberOfVehiclesForSettlement(vehicleGood.getName());
		return tradeDemand / (supply + 1D);
	}

	/**
	 * Determine the value of a drone.
	 * 
	 * @param buy true if vehicles can be bought.
	 * @return value (VP)
	 */
	private double determineDroneValue(boolean buy) {

		double demand = 1D;

		// Add demand for construction missions by architects.
		demand += Math.min(7, getJobNum(JobType.PILOT) * 1.1);

		// Add demand for mining missions by engineers.
		demand += Math.min(8, getJobNum(JobType.TRADER) * 1.2);

		double supply = getNumberOfVehiclesForSettlement(Drone.NAME);
		if (!buy)
			supply--;
		if (supply < 0D)
			supply = 0D;

		return demand / Math.log(supply + 2) * DRONE_FACTOR * Math.log(Math.min(48, settlement.getNumCitizens()));
	}

	/**
	 * Determine the value of a light utility vehicle.
	 * 
	 * @param buy true if vehicles can be bought.
	 * @return value (VP)
	 */
	private double determineLUVValue(boolean buy) {

		double demand = 1;

		// Add demand for mining missions by areologists.
		demand += Math.min(10, getJobNum(JobType.AREOLOGIST) * 1.3);

		// Add demand for construction missions by architects.
		demand += Math.min(8, getJobNum(JobType.ARCHITECT) * 1.2);

		// Add demand for mining missions by engineers.
		demand += Math.min(6, getJobNum(JobType.ENGINEER) * 1.1);

		double supply = getNumberOfVehiclesForSettlement(LightUtilityVehicle.NAME);
		if (!buy)
			supply--;
		if (supply < 0D)
			supply = 0D;

		return demand / Math.log(supply + 2) * LUV_FACTOR * Math.log(Math.min(24, settlement.getNumCitizens()));
	}

	private double determineMissionVehicleValue(String missionType, String vehicleType, boolean buy) {

		double demand = determineMissionVehicleDemand(missionType);

		double currentCapacity = 0D;
		boolean soldFlag = false;
		Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle v  = i.next();
			String type = v.getDescription().toLowerCase();
			if (!buy && !soldFlag && (type.equalsIgnoreCase(vehicleType)))
				soldFlag = true;
			else
				currentCapacity += determineMissionVehicleCapacity(missionType, type);
		}

		double vehicleCapacity = determineMissionVehicleCapacity(missionType, vehicleType);

		double baseValue = (demand / (currentCapacity + 1D)) * vehicleCapacity;

		return baseValue;
	}

	private double determineMissionVehicleDemand(String missionType) {
		double demand = 0D;

		if (TRAVEL_TO_SETTLEMENT_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.PILOT);
			demand *= ((double) settlement.getNumCitizens() / (double) settlement.getPopulationCapacity());
		} else if (EXPLORATION_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.AREOLOGIST);
		} else if (COLLECT_ICE_MISSION.equals(missionType)) {
			demand = getAmountDemandValue(GoodsUtil.getResourceGood(ResourceUtil.iceID));
			if (demand > 100D)
				demand = 100D;
		} else if (RESCUE_SALVAGE_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.PILOT);
		} else if (TRADE_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.TRADER);
		} else if (DELIVERY_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.TRADER);
		} else if (COLLECT_REGOLITH_MISSION.equals(missionType)) {
			demand = getAmountDemandValue(GoodsUtil.getResourceGood(ResourceUtil.regolithID));
			if (demand > 100D)
				demand = 100D;
		} else if (MINING_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.AREOLOGIST);
//		} else if (CONSTRUCT_BUILDING_MISSION.equals(missionType)) {
//			// No demand for rover vehicles.
//		} else if (SALVAGE_BUILDING_MISSION.equals(missionType)) {
//			// No demand for rover vehicles.
		} else if (AREOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.AREOLOGIST);
		} else if (BIOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.BIOLOGIST);
		} else if (METEOROLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
			demand = getJobNum(JobType.METEOROLOGIST);
		} else if (EMERGENCY_SUPPLY_MISSION.equals(missionType)) {
			demand = unitManager.getSettlementNum() - 1D;
			if (demand < 0D) {
				demand = 0D;
			}
		}

		return demand;
	}

	private double determineMissionVehicleCapacity(String missionType, String vehicleType) {
		double capacity = 0D;

		// VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		VehicleDescription v = vehicleConfig.getVehicleDescription(vehicleType);
		int crewCapacity = v.getCrewSize();

		if (TRAVEL_TO_SETTLEMENT_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;
			capacity *= crewCapacity / 8D;

			double range = getVehicleRange(v);
			capacity *= range / 2000D;

		} else if (EXPLORATION_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			double cargoCapacity = v.getTotalCapacity();
			if (cargoCapacity < 500D)
				capacity = 0D;

			boolean hasAreologyLab = false;
			if (v.hasLab() && v.getLabTechSpecialties().contains(ScienceType.AREOLOGY)) {
				hasAreologyLab = true;
			}
			if (!hasAreologyLab)
				capacity /= 2D;

			double range = getVehicleRange(v);
			if (range == 0D)
				capacity = 0D;

		} else if (COLLECT_ICE_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			double cargoCapacity = v.getTotalCapacity();
			if (cargoCapacity < 1250D)
				capacity = 0D;

			double range = getVehicleRange(v);
			if (range == 0D)
				capacity = 0D;

		} else if (RESCUE_SALVAGE_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			double range = getVehicleRange(v);
			capacity *= range / 2000D;

		} else if (TRADE_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			double cargoCapacity = v.getTotalCapacity();
			capacity *= cargoCapacity / 10000D;

			double range = getVehicleRange(v);
			capacity *= range / 2000D;

		} else if (DELIVERY_MISSION.equals(missionType)) {
			capacity = 1D;

			double cargoCapacity = v.getTotalCapacity();
			capacity *= cargoCapacity / 10000D;

			double range = getDroneRange(v);
			capacity *= range / 2000D;

		} else if (COLLECT_REGOLITH_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			double cargoCapacity = v.getTotalCapacity();
			if (cargoCapacity < 1250D)
				capacity = 0D;

			double range = getVehicleRange(v);
			if (range == 0D)
				capacity = 0D;

		} else if (MINING_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			double cargoCapacity = v.getTotalCapacity();
			if (cargoCapacity < 1000D)
				capacity = 0D;

			double range = getVehicleRange(v);
			if (range == 0D)
				capacity = 0D;
//		} else if (CONSTRUCT_BUILDING_MISSION.equals(missionType)) {
//			// No rover vehicles needed.
//		} else if (SALVAGE_BUILDING_MISSION.equals(missionType)) {
//			// No rover vehicles needed.
		} else if (AREOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			if (v.hasLab()) {
				if (v.getLabTechSpecialties().contains(ScienceType.AREOLOGY)) {
					capacity += v.getLabTechLevel();
				} else {
					capacity /= 2D;
				}
			}

			double range = getVehicleRange(v);
			if (range == 0D)
				capacity = 0D;

		} else if (BIOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			if (v.hasLab()) {
				if (v.getLabTechSpecialties().contains(ScienceType.BIOLOGY)) {
					capacity += v.getLabTechLevel();
				} else {
					capacity /= 2D;
				}
			}

			double range = getVehicleRange(v);
			if (range == 0D)
				capacity = 0D;

		} else if (METEOROLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			if (v.hasLab()) {
				if (v.getLabTechSpecialties().contains(ScienceType.METEOROLOGY)) {
					capacity += v.getLabTechLevel();
				} else {
					capacity /= 2D;
				}
			}

			double range = getVehicleRange(v);
			if (range == 0D)
				capacity = 0D;

		} else if (EMERGENCY_SUPPLY_MISSION.equals(missionType)) {
			if (crewCapacity >= 2)
				capacity = 1D;

			double cargoCapacity = v.getTotalCapacity();
			capacity *= cargoCapacity / 10000D;

			double range = getVehicleRange(v);
			capacity *= range / 2000D;
		}

		return capacity;
	}

	/**
	 * Gets the range of the vehicle type.
	 * 
	 * @param v {@link VehicleDescription}.
	 * @return range (km)
	 */
	private double getVehicleRange(VehicleDescription v) {
		double range = 0D;

		double fuelCapacity = v.getCargoCapacity(METHANE);
		double fuelEfficiency = v.getDriveTrainEff();
		range = fuelCapacity * fuelEfficiency * Vehicle.SOFC_CONVERSION_EFFICIENCY;// / 1.5D; ?

		double baseSpeed = v.getBaseSpeed();
		double distancePerSol = baseSpeed / SPEED_TO_DISTANCE;

		int crewSize = v.getCrewSize();

		// Check food capacity as range limit.
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = v.getCargoCapacity(LifeSupportInterface.FOOD);
		double foodSols = foodCapacity / (foodConsumptionRate * crewSize);
		double foodRange = distancePerSol * foodSols / 3D;
		if (foodRange < range)
			range = foodRange;

		// Check water capacity as range limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = v.getCargoCapacity(LifeSupportInterface.WATER);
		double waterSols = waterCapacity / (waterConsumptionRate * crewSize);
		double waterRange = distancePerSol * waterSols / 3D;
		if (waterRange < range)
			range = waterRange;

		// Check oxygen capacity as range limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = v.getCargoCapacity(LifeSupportInterface.OXYGEN);
		double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewSize);
		double oxygenRange = distancePerSol * oxygenSols / 3D;
		if (oxygenRange < range)
			range = oxygenRange;

		return range;
	}

	/**
	 * Gets the range of the drone type.
	 * 
	 * @param v {@link VehicleDescription}.
	 * @return range (km)
	 */
	private double getDroneRange(VehicleDescription v) {
		double range = 0D;

		double fuelCapacity = v.getCargoCapacity(METHANE);
		double fuelEfficiency = v.getDriveTrainEff();
		range = fuelCapacity * fuelEfficiency * Vehicle.SOFC_CONVERSION_EFFICIENCY;// / 1.5D; ?

//		double baseSpeed = v.getBaseSpeed();
//		double distancePerSol = baseSpeed / SPEED_TO_DISTANCE;

		return range;
	}

	/**
	 * Gets the number of the vehicle for the settlement.
	 * 
	 * @param vehicleType the vehicle type.
	 * @return the number of vehicles.
	 */
	private double getNumberOfVehiclesForSettlement(String vehicleType) {
		double number = 0D;
		for (Vehicle vehicle : settlement.getAllAssociatedVehicles()) {
			if (vehicleType.equalsIgnoreCase(vehicle.getDescription()))
				number += 1D;
		}

		// Get the number of vehicles that will be produced by ongoing manufacturing
		// processes.
		Good vehicleGood = GoodsUtil.getVehicleGood(vehicleType);
		number += getManufacturingProcessOutput(vehicleGood);

		return number;
	}

	/**
	 * Determines the trade demand for a good at a settlement.
	 * 
	 * @param good          the good.
	 * @param useTradeCache use the goods trade cache to determine trade demand?
	 * @return the trade demand.
	 */
	private double determineTradeDemand(Good good, boolean useTradeCache) {
		if (useTradeCache) {
			if (goodsTradeCache.containsKey(good.getID()))
				return goodsTradeCache.get(good.getID());
			else
				logger.severe(settlement, " - Good value of " + good + " not valid.");
		} else {
			double bestTradeValue = 0D;

			for (Settlement tempSettlement : unitManager.getSettlements()) {
				if (tempSettlement != settlement) {
					double baseValue = tempSettlement.getGoodsManager().getDemandValue(good);
					double distance = Coordinates.computeDistance(settlement.getCoordinates(),
							tempSettlement.getCoordinates());
					double tradeValue = baseValue / (1D + (distance / 1000D));
					if (tradeValue > bestTradeValue)
						bestTradeValue = tradeValue;
				}
			}
			goodsTradeCache.put(good.getID(), bestTradeValue);
			return bestTradeValue;
		}
		return 0;
	}

	/**
	 * Prepare the goods manager for a vehicle load calculation.
	 */
	public void prepareForLoadCalculation() {
		// Clear vehicle buy and sell value caches.
		vehicleBuyValueCache.clear();
		vehicleSellValueCache.clear();
	}

	/**
	 * Gets the nth power
	 * 
	 * @return
	 */
	public int getNthPower(double num) {
		int power = 0;
		int base = 2;
		int n = (int) num;
		while (n != 1) {
			n = n / base;
			--power;
		}

		return -power;
	}

	public int computeLevel(double ratio) {
		double lvl = 0;
		if (ratio < 1) {
			lvl = 0;
//			double m = getNthPower(1D/ratio);
//			lvl = 10 - m;
		} else if (ratio > 1) {
			double m = getNthPower(ratio);
			lvl = m;
		} else {
			lvl = 1;
		}

		return (int) (Math.round(lvl));
	}

	public int getRepairLevel() {
		return computeLevel(repairMod / BASE_REPAIR_PART);
	}

	public int getMaintenanceLevel() {
		return computeLevel(maintenanceMod / BASE_MAINT_PART);
	}

	public int getEVASuitLevel() {
		return computeLevel(eVASuitMod / BASE_EVA_SUIT);
	}

	public void setRepairPriority(int level) {
		repairMod = computeModifier(BASE_REPAIR_PART, level);
	}

	public void setMaintenancePriority(int level) {
		maintenanceMod = computeModifier(BASE_MAINT_PART, level);

	}

	public void setEVASuitPriority(int level) {
		eVASuitMod = computeModifier(BASE_EVA_SUIT, level);
	}

	public double computeModifier(int baseValue, int level) {
		double mod = 0;
		if (level == 1) {
			mod = baseValue;
		} else if (level < 1) {
//			double m = Math.pow(2, (10 - level));
//			mod = baseValue / m;
			mod = baseValue / 2.0;
		} else if (level > 1) {
			if (level > 5) {
				// Limit the level to the maximum of 5
				level = 5;
			}
			double m = Math.pow(2, level);
			mod = m * baseValue;
		}
		return mod;
	}

	/**
	 * Gets the settlement inventory.
	 * 
	 * @return inventory
	 */
	public Inventory getInventory() {
		return settlement.getInventory();
	}

	public double getWaterValue() {
		return waterValue;
	}

	public void setWaterValue(double value) {
		waterValue = value;
	}

	public static List<Good> getBuyList() {
		if (buyList == null) {
			buyList = new ArrayList<>(GoodsUtil.getGoodsList());
			buyList.removeAll(getExclusionBuyList());
//			for (Good g : buyList) {
//				for (int i : getExclusionBuyList()) {
//					if (g.getID() == i)
//						buyList.remove(g);
//				}
//			}
		}
		return buyList;
	}

//	/**
//	 * Update the goods value from buffers
//	 * 
//	 * @param time
//	 */
//	public void updateGoodsValueBuffers(double time) {
//		// Use buffer to gradually update
//		Map<Integer, Good> map = GoodsUtil.getGoodsMap();
//		for (Good good : map.values()) {
//			// Load the old good value
//			double oldValue = good.getGoodValue(); // goodsValues.get(good); //
//			// Gets the old delta
//			double oldDelta = good.getGoodValueBuffer();
//
//			double newValue = 0;
//
//			double newDelta = 0;
//
//			if (oldDelta > 0) {
//
//				if (oldDelta > time) {
//					newValue = oldValue + time;
//					newDelta = oldDelta - time;
//				} else {
//					newValue = oldValue + oldDelta;
//					newDelta = 0;
//				}
//				// Add the good value of its input good
////				value += good.computeInputValue();
//				// Save the newDelta in the good's buffer
//				good.setGoodValueBuffer(newDelta);
//				// Save the newValue in the good
//				good.setGoodValue(newValue);
//				// Save the newValue in the goodsValues map
//				goodsValues.put(good.getID(), newValue);
//
////				logger.info(good.getName() + " +ve oldDelta : " + Math.round(oldDelta*1000.0)/1000.0
////						+ "   newDelta : " + Math.round(newDelta*1000.0)/1000.0	
////						+ "   oldValue : " + Math.round(oldValue*1000.0)/1000.0
////						+ "   newValue : " + Math.round(newValue*1000.0)/1000.0
////						);
//
//			} else if (oldDelta < 0) {
//
//				if (-oldDelta > time) {
//					newValue = oldValue - time;
//					newDelta = oldDelta + time;
//				} else {
//					newValue = oldValue + oldDelta;
//					newDelta = 0;
//				}
//
//				// Add the good value of its input good
////				value += good.computeInputValue();
//				// Save the newDelta in the good's buffer
//				good.setGoodValueBuffer(newDelta);
//				// Save the newValue in the good
//				good.setGoodValue(newValue);
//				// Save the newValue in the goodsValues map
//				goodsValues.put(good.getID(), newValue);
//
////				logger.info(good.getName() + " -ve oldDelta : " + Math.round(oldDelta*1000.0)/1000.0
////						+ "   newDelta : " + Math.round(newDelta*1000.0)/1000.0	
////						+ "   oldValue : " + Math.round(oldValue*1000.0)/1000.0
////						+ "   newValue : " + Math.round(newValue*1000.0)/1000.0
////						);
//			}
//		}
//	}

//	/**
//	 * Updates the values for all the goods at the settlement.
//	 */
//	private void update() {
////		System.out.println(settlement + " GoodsManager::updateGoodsValuePrice");
//		// Clear parts demand cache.
////		partDemandCache.clear();
//
//		// Clear vehicle caches.
////		vehicleBuyValueCache.clear();
////		vehicleSellValueCache.clear();
//
//		Iterator<Good> i = GoodsUtil.getGoodsList().iterator();
//		while (i.hasNext()) {
//			Good good = i.next();
//			updateGoodValue(good, true);
////			good.computeBaseCost();
//		}
//
//		settlement.fireUnitUpdate(UnitEventType.GOODS_VALUE_EVENT);
//		
////		settlement.fireUnitUpdate(UnitEventType.PRICE_EVENT);
//
//		initialized = true;
//	}

//	/**
//	 * Updates the value of a good at the settlement.
//	 * 
//	 * @param good             the good to update.
//	 * @param collectiveUpdate true if this update is part of a collective good
//	 *                         value update.
//	 */
//	public void updateGoodValue(Good good, boolean collectiveUpdate) {
//		if (good != null) {
//
//			if (initialized) {
//				// Load the old good value
//				double oldValue = good.getGoodValue();
//				// Compute the new good value
//				double newValue = determineGoodValue(good, getNumberOfGoodForSettlement(good), false);
////				// Gets the old delta
////				double oldDelta = good.getGoodValueBuffer();
////				// Compute the new delta
////				double delta = oldDelta + newValue - oldValue;
//				// Compute the new delta
//				double newDelta = newValue - oldValue;
//				// Add the good value of its input good
////				value += good.computeInputValue();
//				// Save the newDelta in the good's buffer
//				good.setGoodValueBuffer(newDelta);
//
////				if (delta > 0) logger.info(good.getName() + " - delta : " + Math.round(delta*1000.0)/1000.0);
//				// Save it in the good
////				good.setGoodValue(newValue);
//				// Save it in the goodsValues map
////				goodsValues.put(good, newValue);
//			} else {
//				// Compute the new good value
//				double newValue = determineGoodValue(good, getNumberOfGoodForSettlement(good), false);
//				// Save it in the good
//				good.setGoodValue(newValue);
//				// Save it in the goodsValues map
//				goodsValues.put(good.getID(), newValue);
//			}
//
//			if (!collectiveUpdate)
//				settlement.fireUnitUpdate(UnitEventType.GOODS_VALUE_EVENT, good);
//		} else
//			logger.severe(settlement, "Good is null.");
//	}

	/**
	 * Gets the price per item for a good
	 * 
	 * @param good
	 * @return
	 */
	public double getPricePerItem(Good good) {
		double price = good.getCostOutput();
//		double price = getGoodValuePerItem(good.getID()) * cost;
//		price = Math.max(minPrice, price);
//		price = Math.min(maxPrice, price);
//		System.out.println(good.getName() 
//				+ "'s cost " + cost
//				+ "   price: " + price);
		return price;
	}

	/**
	 * Gets the price per item for a good
	 * 
	 * @param id the good id
	 * @return
	 */
	public double getPricePerItem(int id) {
		double price = GoodsUtil.getResourceGood(id).getCostOutput();
//		double price = getGoodValuePerItem(id) * cost;
//		price = Math.max(minPrice, price);
//		price = Math.min(maxPrice, price);
		return price;
	}

	/**
	 * Gets the value per item of a good.
	 * 
	 * @param good the good to check.
	 * @return value (VP)
	 */
	public double getGoodValuePerItem(Good good) {
		return getGoodValuePerItem(good.getID());
	}

	/**
	 * Gets the value per item of a good.
	 * 
	 * @param id the good id to check.
	 * @return value (VP)
	 */
	public double getGoodValuePerItem(int id) {
//		try {
		if (goodsValues.containsKey(id))
			return goodsValues.get(id);
		else
			logger.severe(settlement, " - Good Value of " + id + " not valid.");
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, e.getMessage());
//		}
		return 0;
	}

	/**
	 * Gets the demand value of a part
	 * 
	 * @param good the good to check.
	 * @return value (VP)
	 */
	public double getPartDemandValue(Good good) {
		return getPartDemandValue(good.getID());
	}

	/**
	 * Gets the demand value of a part
	 * 
	 * @param good's id.
	 * @return value (VP)
	 */
	public double getPartDemandValue(int id) {
//		try {
		if (partDemandCache.containsKey(id))
			return partDemandCache.get(id);
		else
			logger.severe(settlement, " - Part  " 
					+ ItemResourceUtil.findItemResourceName(id)
					+ "(" + id + ")" + " not valid.");
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, e.getMessage());
//		}
		return 1;
	}

	/**
	 * Gets the demand value of a resource.
	 * 
	 * @param good the good to check.
	 * @return demand value
	 */
	public double getAmountDemandValue(Good good) {
		return getAmountDemandValue(good.getID());
	}

	/**
	 * Gets the demand value of a resource.
	 * 
	 * @param good's id.
	 * @return demand value
	 */
	public double getAmountDemandValue(int id) {
//		try {
		if (amountDemandCache.containsKey(id))
			return amountDemandCache.get(id);
		else
			logger.severe(settlement, " - Amount resource " 
					+ ResourceUtil.findAmountResourceName(id) + "(" + id + ")" + " not valid.");
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, e.getMessage());
//		}
		return 1;
	}

	/**
	 * Gets the demand value of an equipment.
	 * 
	 * @param equipment id.
	 * @return demand value
	 */
	public double getEquipmentDemandValue(int id) {
//		try {
		if (equipmentDemandCache.containsKey(id))
			return equipmentDemandCache.get(id);
		else
			logger.severe(settlement, " - Equipment " 
					+ EquipmentType.convertID2Type(id) + "(" + id + ")" + " not valid.");
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, e.getMessage());
//		}
		return 5;
	}

	public double getVehicleDemandValue(int id) {
//		try {
		if (vehicleDemandCache.containsKey(id))
			return vehicleDemandCache.get(id);
		else
			logger.severe(settlement, " - Vehicle "
					+ VehicleType.convertID2Type(id) + "(" + id + ")" + " not valid.");
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, e.getMessage());
//		}
		return 10;
	}
	
	public double getDemandValue(Good good) {
		int id = good.getID();
		if (id < ResourceUtil.FIRST_AMOUNT_RESOURCE_ID)
			return 0;
		if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID)
			return getAmountDemandValue(id);
		if (id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID)
			return getPartDemandValue(id);
		if (id < ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID)
			return getVehicleDemandValue(id);
		
		return getEquipmentDemandValue(id);
	}
	
	/**
	 * Calculates the good value of a good.
	 * 
	 * @param good's id.
	 * @return value (VP)
	 */
	public double determineGoodValueWithSupply(Good good, double supply) {
		if (goodsValues.containsKey(good.getID()))
			return determineGoodValue(good, supply, true);
		else
			logger.severe(settlement, " - Good Value of : " + good + " not valid.");
		return 0;
	}

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param s  {@link Simulation}
	 * @param c  {@link MarsClock}
	 * @param m  {@link MissionManager}
	 * @param u  {@link UnitManager}
	 * @param pc {@link PersonConfig}
	 */
	public static void initializeInstances(Simulation s, MarsClock c, MissionManager m, UnitManager u,
			PersonConfig pc) {
		sim = s;
		simulationConfig = SimulationConfig.instance();
		unitManager = u;
		missionManager = m;
		marsClock = c;
//		buildingConfig = simulationConfig.getBuildingConfiguration();
//		cropConfig = simulationConfig.getCropConfiguration();
//		mealConfig = simulationConfig.getMealConfiguration();
		personConfig = pc;
		vehicleConfig = simulationConfig.getVehicleConfiguration();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

		settlement = null;
		goodsValues.clear();
		goodsValues = null;
		amountDemandCache.clear();
		amountDemandCache = null;
		goodsTradeCache.clear();
		goodsTradeCache = null;
		equipmentDemandCache.clear();
		equipmentDemandCache = null;
		vehicleDemandCache.clear();
		vehicleDemandCache = null;
		
		if (vehicleBuyValueCache != null) {
			vehicleBuyValueCache.clear();
			vehicleBuyValueCache = null;
		}

		if (vehicleSellValueCache != null) {
			vehicleSellValueCache.clear();
			vehicleSellValueCache = null;
		}

		if (partDemandCache != null) {
			partDemandCache.clear();
			partDemandCache = null;
		}

		// Destroy goods list in GoodsUtil.
		GoodsUtil.destroyGoods();
	}
}
