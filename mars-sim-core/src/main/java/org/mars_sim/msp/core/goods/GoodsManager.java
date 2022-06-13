/*
 * Mars Simulation Project
 * GoodsManager.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.goods;

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
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.food.FoodProductionProcess;
import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionProcessItem;
import org.mars_sim.msp.core.food.FoodProductionUtil;
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
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
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
import org.mars_sim.msp.core.vehicle.VehicleSpec;
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
	private static final String STEEL = "steel";
//	private static final String STEEL_CAN = "steel canister";
//	private static final String AL_WIRE = "aluminum wire";

	private static final String BOTTLE = "bottle";
	private static final String FIBERGLASS = "fiberglass";
	private static final String FIBERGLASS_CLOTH = "fiberglass cloth";
	private static final String METHANE = "methane";
	private static final String BRICK = "brick";
	private static final String METEORITE = "meteorite";
	private static final String ROCK = "rock";
	private static final String VEHICLE = "vehicle";

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

	private static final double ATTACHMENT_PARTS_DEMAND = 1.2;

	private static final int PROJECTED_GAS_CANISTERS = 10;

//	private static final double DAMPING_RATIO = .5;
	private static final double MIN = .000_001;

	private static final double INITIAL_AMOUNT_DEMAND = 0;
	private static final double INITIAL_PART_DEMAND = 0;
	private static final double INITIAL_EQUIPMENT_DEMAND = 0;
	private static final double INITIAL_VEHICLE_DEMAND = 0;

	private static final double WASTE_WATER_VALUE = .05D;
	private static final double WASTE_VALUE = .05D;
	private static final double USEFUL_WASTE_VALUE = 1.05D;

	private static final double EVA_SUIT_VALUE = 3D;

	private static final double EVA_PARTS_VALUE = .2;

	private static final double ORE_VALUE = .9;
	private static final double MINERAL_VALUE = .9;
	private static final double ROCK_VALUE = .6;

	private static final double ROBOT_FACTOR = 1;

	private static final double TRANSPORT_VEHICLE_FACTOR = 5;
	private static final double CARGO_VEHICLE_FACTOR = 4;
	private static final double EXPLORER_VEHICLE_FACTOR = 3;
	private static final double LUV_VEHICLE_FACTOR = 4;
	private static final double DRONE_VEHICLE_FACTOR = 5;

	private static final double VEHICLE_PART_DEMAND = .05;

	private static final double LUV_FACTOR = 2;
	private static final double DRONE_FACTOR = 2;

	private static final double VEHICLE_FUEL_FACTOR = .5;

	private static final double RESOURCE_PROCESSING_INPUT_FACTOR = .5;
	private static final double MANUFACTURING_INPUT_FACTOR = 2D;
	private static final double CONSTRUCTING_INPUT_FACTOR = 2D;

	private static final double COOKED_MEAL_INPUT_FACTOR = .5;
	private static final double DESSERT_FACTOR = .1;
	private static final double FOOD_PRODUCTION_INPUT_FACTOR = .1;
	private static final double FARMING_FACTOR = .1;
	private static final double TISSUE_CULTURE_FACTOR = .25;
	private static final double LEAVES_FACTOR = .5;
	private static final double CROP_FACTOR = .1;

	private static final double CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR = 100D;
	private static final double CONSTRUCTION_SITE_REQUIRED_PART_FACTOR = 100D;

	private static final double MIN_SUPPLY = 0.1;
	private static final double MIN_DEMAND = 0.1;
	private static final double MAX_SUPPLY = 5_000D;
	private static final double MAX_DEMAND = 5_000D;
	private static final double MAX_PROJ_DEMAND = 5_000D;
	private static final double MAX_VP = 5_000D;
	private static final double MIN_VP = .1;
	private static final double PERCENT_90 = .9;
	private static final double PERCENT_81 = .81;
	private static final double MAX_FINAL_VP = 5_000D;

	private static final double LIFE_SUPPORT_MIN = 100;

	private static final double SPEED_TO_DISTANCE = 2D / 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;

	private static final double CROPFARM_BASE = 1;
	private static final double MANU_BASE = 1;
	private static final double RESEARCH_BASE = 1.5;
	private static final double TRANSPORT_BASE = 1;
	private static final double TRADE_BASE = 1;
	private static final double TOURISM_BASE = 1;

	private static final double GAS_CANISTER_DEMAND = 1;
	private static final double SPECIMEN_BOX_DEMAND = 1.2;
	private static final double LARGE_BAG_DEMAND = .5;
	private static final double BAG_DEMAND = .1;
	private static final double BARREL_DEMAND = .2;

	private static final double SCRAP_METAL_DEMAND = .05;
	private static final double INGOT_METAL_DEMAND = .1;
	private static final double SHEET_METAL_DEMAND = .5;
	private static final double STEEL_DEMAND = .1;

	private static final double BOTTLE_DEMAND = .002;
	private static final double FIBERGLASS_DEMAND = .05;
	private static final double BRICK_DEMAND = .005;

	private static final double REGOLITH_DEMAND = .5;
	private static final double CHEMICAL_DEMAND = .1;
	private static final double COMPOUND_DEMAND = .01;
	private static final double ELEMENT_DEMAND = .1;

	private static final double ELECTRICAL_DEMAND = .09;
	private static final double INSTRUMENT_DEMAND = .999;
	private static final double METALLIC_DEMAND = .999;
	private static final double UTILITY_DEMAND = .999;
	private static final double KITCHEN_DEMAND = .999;
	private static final double CONSTRUCTION_DEMAND = .999;

	/** VP probability modifier. */
	public static final double ICE_VALUE_MODIFIER = .005D;
	private static final double WATER_VALUE_MODIFIER = 1D;

	public static final double SOIL_VALUE_MODIFIER = .5;
	public static final double REGOLITH_VALUE_MODIFIER = .25D;
	public static final double SAND_VALUE_MODIFIER = .5D;
	public static final double CONCRETE_VALUE_MODIFIER = .5D;
	public static final double ROCK_MODIFIER = 0.99D;
	public static final double METEORITE_MODIFIER = 1.05;
	public static final double SALT_VALUE_MODIFIER = .2;

	public static final double OXYGEN_VALUE_MODIFIER = .02D;
	public static final double METHANE_VALUE_MODIFIER = .5D;

	private static final double LIFE_SUPPORT_FACTOR = .005;
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
	private Map<Integer, Double> tradeCache = new HashMap<>();

	private Map<Integer, Double> amountDemandCache = new HashMap<>();
	private Map<Integer, Double> partDemandCache = new HashMap<>();
	private Map<Integer, Double> vehicleDemandCache = new HashMap<>();
	private Map<Integer, Double> equipmentDemandCache = new HashMap<>();

	private Map<Integer, Integer> deflationIndexMap = new HashMap<>();

//	 private Map<Good, Double> goodsSupplyCache;

	private Map<String, Double> vehicleBuyValueCache;
	private Map<String, Double> vehicleSellValueCache;

	private Map<Malfunctionable, Map<Integer, Number>> orbitRepairParts = new HashMap<>();

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

	private static final int METEORITE_ID = ResourceUtil.findIDbyAmountResourceName(METEORITE);

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
			tradeCache.put(id, 0D);
			deflationIndexMap.put(id, 0);
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

		// Create equipment demand cache.
		for(EquipmentType eType : EquipmentType.values()) {
			int id = EquipmentType.getResourceID(eType);
			equipmentDemandCache.put(id, INITIAL_EQUIPMENT_DEMAND);
		}

		// Create equipment demand cache.
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
			for (VehicleType type : VehicleType.values()) {
				exclusionBuyList.add(GoodsUtil.getVehicleGood(type));
			}
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.regolithID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.iceID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.co2ID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.coID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.sandID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.greyWaterID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.blackWaterID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.compostID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.eWasteID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.toxicWasteID));
			exclusionBuyList.add(GoodsUtil.getResourceGood(ResourceUtil.cropWasteID));
			// Note: add vehicles to this list ?
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
			if (GoodCategory.AMOUNT_RESOURCE == good.getCategory())
				value = determineAmountResourceGoodValue(good, supply, useCache);

			// Determine all item resource values.
			if (GoodCategory.ITEM_RESOURCE == good.getCategory())
				value = determineItemResourceGoodValue(good, supply, useCache);

			// Determine all equipment values.
			if (GoodCategory.EQUIPMENT == good.getCategory()
					|| GoodCategory.CONTAINER == good.getCategory())
				value = determineEquipmentGoodValue(good, supply, useCache);

			// Determine all vehicle values.
			if (GoodCategory.VEHICLE == good.getCategory())
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
			return value;// goodsValues.get(id);
		}

		else {
			double value = 0;
			double previous = 0;
			double average = 0;
			double projected = 0;
			double trade = 0;
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

//			if (id == ResourceUtil.regolithID)
//				System.out.println("1. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Tune fuel demand if applicable.
//			projected += getFuelDemand(id); // cause the sim to freeze

			// Tune potable water usage demand if applicable.
			projected += getPotableWaterUsageDemand(id);

			// Tune toiletry usage demand if applicable.
			projected += getToiletryUsageDemand(id);

			// Tune vehicle demand if applicable.
			projected += getVehicleFuelDemand(id);

//			if (id == ResourceUtil.regolithID)
//				System.out.println("2. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Tune farming demand.
			projected += getFarmingDemand(id);

//			if (id == ResourceUtil.regolithID)
//				System.out.println("2.1. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Tune the crop demand
			projected += getCropDemand(id);


//			if (id == ResourceUtil.regolithID)
//				System.out.println("2.2. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Tune resource processing demand.
			projected += getResourceProcessingDemand(id);

//			if (id == ResourceUtil.regolithID)
//				System.out.println("3. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Tune manufacturing demand.
			projected += getResourceManufacturingDemand(id);

//			if (id == ResourceUtil.regolithID)
//				System.out.println("4. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Tune food production related demand.
			projected += getResourceFoodProductionDemand(id);

			// Tune demand for the ingredients in a cooked meal.
			projected += getResourceCookedMealIngredientDemand(id);

			// Tune dessert demand.
			projected += getResourceDessertDemand(id);

			// Tune construction demand.
			projected += getResourceConstructionDemand(id);

//			if (id == ResourceUtil.regolithID)
//				System.out.println("5. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Tune construction site demand.
			projected += getResourceConstructionSiteDemand(id);

			// Adjust the demand on various waste products with the disposal cost.
			projected += getWasteDisposalSinkCost(id);

//			if (id == ResourceUtil.regolithID)
//				System.out.println("6. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Adjust the demand on minerals and ores.
			projected += getMineralDemand(id);

			// Flatten demand.
			projected = flattenAmountDemand(resourceGood, projected);

//			if (id == ResourceUtil.regolithID)
//				System.out.println("7. " + id
//					+ "  " + ResourceUtil.findAmountResourceName(id)
//					+ "  projected: " + Math.round(projected*10.0)/10.0);

			// Adjust the demand on life support consumables with the disposal cost.
//			projected = AdjustLifeSupport(id, projected);

//			if (projected > MAX_PROJ_DEMAND);
//				projected = MAX_PROJ_DEMAND;
//			projected = Math.min(MAX_PROJ_DEMAND, projected);

			if (projected < MIN_DEMAND)
				projected = MIN_DEMAND;

			// Add trade value.
			trade = determineTradeDemand(resourceGood, useCache);

			if (previous == 0) {
				// At the start of the sim
				totalDemand = .1 * average + .8 * projected + .1 * trade;
			}
			else {
				// Intentionally lose 10% of its value
				totalDemand = .75 * previous + .05 * average + .05 * projected + .05 * trade;
			}

//			if (id == ResourceUtil.regolithID)
//				System.out.println("td. " + id
//						+ "  " + ResourceUtil.findAmountResourceName(id)
//						+ "  previous: " + Math.round(previous*10.0)/10.0
//						+ "  average: " + Math.round(average*10.0)/10.0
//						+ "  projected: " + Math.round(projected*10.0)/10.0
//						+ "  trade: " + Math.round(trade*10.0)/10.0);
//						+ "  supply: " + Math.round(supply*10.0)/10.0
//						+ "  totalDemand: " + Math.round(totalDemand*10.0)/10.0
//						+ "  totalSupply: " + Math.round(totalSupply*10.0)/10.0
//						+ "  amountValue: " + Math.round(amountValue*10.0)/10.0
//						);

			if (totalDemand < MIN_DEMAND)
				totalDemand = MIN_DEMAND;
//			totalDemand = Math.max(MIN_DEMAND, totalDemand);
			if (totalDemand > MAX_DEMAND)
				totalDemand = MAX_DEMAND;
//			totalDemand = Math.max(MAX_DEMAND, totalDemand);

			// Save the goods demand
			amountDemandCache.put(id, totalDemand);

			if (supply == 0)
				supply = settlement.getAmountResourceStored(id);

			// Calculate total supply
			totalSupply = getAverageAmountSupply(id, supply, solElapsed);// lowerLifeSupportSupply(id, supply * .1);

//			if (totalSupply < MIN_SUPPLY)
//				totalSupply = MIN_SUPPLY;
			totalSupply = Math.max(MIN_SUPPLY, totalSupply);

//			if (totalSupply > MAX_SUPPLY)
//				totalSupply = MAX_SUPPLY;
			totalSupply = Math.min(MAX_SUPPLY, totalSupply);

			// Calculate the value point
			value = totalDemand / totalSupply;

			// Check if it surpass the max VP
			if (value > MAX_VP) {
//				System.out.println("deflation: " + id + " " + ResourceUtil.findAmountResourceName(id) + " " + amountValue);
				// Update deflationIndexMap for other resources of the same category
				value = updateDeflationMap(id, value, resourceGood.getCategory(), true);
			}
			// Check if it falls below 1
			else if (value < MIN_VP) {
				// Update deflationIndexMap for other resources of the same category
				value = updateDeflationMap(id, value, resourceGood.getCategory(), false);
			}

			// Check for inflation and deflation adjustment due to other resources
			value = checkDeflation(id, value);
			// Adjust the value to the average value
			value = tuneToAverageValue(resourceGood, value);

			// Save the value point
			goodsValues.put(id, value);

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
			return value;
		}
	}


	private double tuneToAverageValue(Good good, double value) {
		// Gets the inter-market value among the settlements
		double average = good.getAverageGoodValue();
		double newAve0 = 0;

		if (average == 0) {
			newAve0 = value;
		}

		else {
			newAve0 = .1 * average + .9 * value;

			double newAve1 = 0;

			if (average > value)
				newAve1 = 1.1 * value;
			else
				newAve1 = 1.1 * average;

			newAve0 = Math.min(newAve0, newAve1);

			if (newAve0 > MAX_FINAL_VP)
				newAve0 = MAX_FINAL_VP;
		}

		good.setAverageGoodValue(newAve0);

		return newAve0;
	}


	private double checkDeflation(int id, double value) {
		// Check for inflation and deflation adjustment
		int index = deflationIndexMap.get(id);

		if (index > 0) { // if the index is positive, need to deflate the value
			for (int i = 0; i < index; i++) {
				double newValue = value * PERCENT_90;
				if (newValue <= 10) {
					// if it will become less than 10, then do not need to further reduce it
				}
				else
					value = newValue;
			}
		}

//		else if (index < 0) {  // if the index is negative, need to inflate the value
//			for (int i = 0; i < -index; i++) {
//				double newValue = value * PERCENT_110;
//				if (newValue >= 1_000) {
//					// if it is larger than 1000, then do not need to further increase it
//				}
//				else
//					value = newValue;
//			}
//		}

		deflationIndexMap.put(id, 0);
		return value;
	}

	/**
	 * Updates the deflation index Map
	 *
	 * @param id     the id of the resource that cause the deflation
	 * @param value  the demand value to be adjusted
	 * @param exceed true if it surpasses the upper limit; false if it falls below
	 *               the lower limit
	 * @return the adjusted value
	 */
	public double updateDeflationMap(int id, double value, GoodCategory type, boolean exceed) {

		for (int i : deflationIndexMap.keySet()) {
			if (id != i) {
				if (type == GoodsUtil.getResourceGood(i).getCategory()) {
					// This good is of the same category as the one that cause the
					// inflation/deflation
					int oldIndex = deflationIndexMap.get(i);
					if (exceed) {
						// reduce twice
						deflationIndexMap.put(id, oldIndex + 2);
					} else {
//						deflationIndexMap.put(id, oldIndex - 2);
					}
				}
				else { // This good is of different category
					int oldIndex = deflationIndexMap.get(i);
					if (exceed) {
						// reduce once
						deflationIndexMap.put(id, oldIndex + 1);
					} else {
//						deflationIndexMap.put(id, oldIndex - 1);
					}
				}
			}
		}

		if (exceed)
			return value * PERCENT_81;

		return value;
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
		double supply = 0; //getInventory().getAmountSupply(resource);
		// Gets # of requests
		int requests = 0; //getInventory().getAmountSupplyRequest(resource);

		double aveSupply = 1 + Math.log((1 + supply * requests + supplyStored) / solElapsed);

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
		double supply = 0; //getInventory().getItemSupply(resource);
		// Gets # of successful requests
		int requests = 0; //getInventory().getAmountSupplyRequest(resource);

		double aveSupply = 1 + Math.log((1 + supply * requests + supplyStored) / solElapsed);

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

//			Inventory inv = getInventory();
			// Gets the total demand on record
			double demand = 0; //inv.getAmountDemand(resource);
			// Gets the estimated demand on record
			double estDemand = 0; //inv.getAmountDemandEstimated(resource);
			// Gets # of successful requests
			int metRequests = 0; //inv.getAmountDemandMetRequest(resource);
			// Gets the total # of requests
			int totRequests = 0; //inv.getAmountDemandTotalRequest(resource);

			double demandPerMetRequest = 0;

			if (demand > MIN && metRequests > 0) {
				demandPerMetRequest = demand / (metRequests + 1);
				// Figure out the total potential demand based on good demand statistics
				aveDemand = demandPerMetRequest / solElapsed;
			} else {
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

		return Math.min(500, aveDemand);
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

//			Inventory inv = getInventory();
			// Gets the total demand record
			double demand = 0; //inv.getItemDemand(resource);
			// Gets # of successful requests
			int metRequests = 0; //inv.getItemDemandMetRequest(resource);
			// Gets the total # of requests
			int totRequests = 0; //inv.getItemDemandTotalRequest(resource);
			// Gets the estimated demand on record
			double estDemand = 0; //inv.getItemDemandEstimated(resource);

			double demandPerMetRequest = 0;

			if (demand > MIN && metRequests > 0) {
				demandPerMetRequest = demand / (metRequests + 1);
				// Figure out the total potential demand based on good demand statistics
				aveDemand = demandPerMetRequest / solElapsed;
			} else {
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

		return Math.min(500, aveDemand);
	}

	/**
	 * Gets the life support demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getLifeSupportDemand(int resource) {
		if (ResourceUtil.isLifeSupport(resource)) {
			double amountNeededSol = 0;
			int numPeople = settlement.getNumCitizens();

			if (resource == ResourceUtil.oxygenID) {
				amountNeededSol = personConfig.getNominalO2ConsumptionRate() * OXYGEN_VALUE_MODIFIER;
			} else if (resource == ResourceUtil.waterID) {
				amountNeededSol = personConfig.getWaterConsumptionRate() * waterValue;
			} else if (resource == ResourceUtil.foodID) {
				amountNeededSol = personConfig.getFoodConsumptionRate() * FOOD_VALUE_MODIFIER;
			}

			return numPeople * amountNeededSol * trade_factor * LIFE_SUPPORT_FACTOR;

		} else
			return 0;
	}

	private double capLifeSupportAmountDemand(int resource, double demand) {
		if (resource == ResourceUtil.oxygenID || resource == ResourceUtil.waterID || resource == ResourceUtil.hydrogenID
				|| resource == ResourceUtil.methaneID)
			return Math.min(LIFE_SUPPORT_MIN, demand);
		return demand;
	}

	/**
	 * Gets a particular mineral demand
	 *
	 * @param resource
	 * @param demand
	 * @return
	 */
	private double getMineralDemand(int resource) {
		double demand = 1;

		if (resource == ResourceUtil.rockSaltID) {
			return demand * SALT_VALUE_MODIFIER;
		}

		else if (resource == ResourceUtil.epsomSaltID) {
			return demand * SALT_VALUE_MODIFIER;
		}

		else if (resource == ResourceUtil.soilID) {
			return demand * settlement.getCropsNeedingTending() * SAND_VALUE_MODIFIER;
		}

		else if (resource == ResourceUtil.concreteID) {
			double regolithVP = 1 + goodsValues.get(ResourceUtil.regolithID);
			double sandVP = 1 + goodsValues.get(ResourceUtil.sandID);
			// the demand for sand is dragged up or down by that of regolith
			// loses 5% by default
			return demand * (.75 * regolithVP + .2 * sandVP) / sandVP * CONCRETE_VALUE_MODIFIER;
		}

		else if (resource == ResourceUtil.sandID) {
			double regolithVP = 1 + goodsValues.get(ResourceUtil.regolithID);
			double sandVP = 1 + goodsValues.get(ResourceUtil.sandID);
			// the demand for sand is dragged up or down by that of regolith
			// loses 10% by default
			return demand * (.2 * regolithVP + .7 * sandVP) / sandVP * SAND_VALUE_MODIFIER;
		}

		else {
			double regolithVP = goodsValues.get(ResourceUtil.regolithID);
			double sandVP = 1 + goodsValues.get(ResourceUtil.sandID);

			for (int id : ResourceUtil.rockIDs) {
				if (resource == id) {
					double vp = goodsValues.get(id);
					return demand * (.2 * regolithVP + .9 * vp) / vp * ROCK_VALUE;
				}
			}

			for (int id : ResourceUtil.mineralConcIDs) {
				if (resource == id) {
					double vp = goodsValues.get(id);
					return demand * (.2 * regolithVP + .9 * vp) / vp * MINERAL_VALUE;
				}
			}

			for (int id : ResourceUtil.oreDepositIDs) {
				if (resource == id) {
					double vp = goodsValues.get(id);
					// loses 10% by default
					return demand * (.3 * regolithVP + .6 * vp) / vp * ORE_VALUE;
				}
			}

			if (resource == ResourceUtil.regolithBID || resource == ResourceUtil.regolithCID
					|| resource == ResourceUtil.regolithDID) {
				double vp = goodsValues.get(resource);
				return demand * (.3 * regolithVP + .6 * vp) / vp;// * REGOLITH_VALUE_MODIFIER;
			}

			// Checks if this resource is a ROCK type
			String type = ResourceUtil.findAmountResource(resource).getType();
			if (type != null && type.equalsIgnoreCase(ROCK)) {
				double vp = goodsValues.get(resource);

				if (resource == METEORITE_ID)
					return demand * (.4 * regolithVP + .5 * vp) / vp * METEORITE_MODIFIER;
				else
					return demand * (.2 * sandVP + .7 * vp) / vp * ROCK_MODIFIER;
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
	private double getWasteDisposalSinkCost(int resource) {
		double demand = 0;
		if (resource == ResourceUtil.greyWaterID || resource == ResourceUtil.blackWaterID) {
			return demand * WASTE_WATER_VALUE;
		}

		if (resource == ResourceUtil.leavesID) {
			return demand * LEAVES_FACTOR;
		}

		if (resource == ResourceUtil.soilID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.coID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.foodWasteID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.cropWasteID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.compostID) {
			return demand / 2D * USEFUL_WASTE_VALUE;
		}

		if (resource == ResourceUtil.co2ID) {
			return demand / 2D;
		}

		else {
			String type = ResourceUtil.findAmountResource(resource).getType();
			if (type != null && type.equalsIgnoreCase("waste")) {
				return WASTE_VALUE;
			}

		}

		return demand;
	}

	/**
	 * Gets the potable water usage demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getPotableWaterUsageDemand(int resource) {
		double demand = 0;
		if (resource == ResourceUtil.waterID) {
			// Add the awareness of the water ration level in adjusting the water demand
			double waterRationLevel = settlement.getWaterRationLevel();
			double amountNeededSol = personConfig.getWaterUsageRate();
			int numPeople = settlement.getNumCitizens();
			demand = numPeople * amountNeededSol * waterValue * trade_factor * (1 + waterRationLevel);
		}

		return demand;
	}

	/**
	 * Computes the ice demand.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double computeIceProjectedDemand(int resource) {
		if (resource == ResourceUtil.iceID) {
			double ice = 1 + goodsValues.get(resource);
			double water = 1 + goodsValues.get(ResourceUtil.waterID);
			// Use the water's VP and existing iceSupply to compute the ice demand
			return Math.max(1, settlement.getNumCitizens()) * (.8 * water + .2 * ice) / ice
					* ICE_VALUE_MODIFIER * settlement.getWaterRationLevel();
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
		double demand = 0;
		if (resource == ResourceUtil.regolithID) {
			double sand = 1 + goodsValues.get(ResourceUtil.sandID);
			double concrete = 1 + goodsValues.get(ResourceUtil.concreteID);
			double cement = 1 + goodsValues.get(ResourceUtil.cementID);
			double regolith = 1 + goodsValues.get(resource);
			// Limit the minimum value of regolith projected demand
			demand = 1 + Math.min(48, settlement.getNumCitizens()) *
					(.1 * cement + .2 * concrete + .5 * regolith + .2 * sand) / regolith
					* REGOLITH_VALUE_MODIFIER;
		}

		return demand;
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
			int numPeople = settlement.getIndoorPeopleCount();
			return numPeople * amountNeededSol;
		}

		return 0;
	}

	/**
	 * Gets vehicle fuel demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg) for the resource.
	 */
	private double getVehicleFuelDemand(int resource) {
		double demand = 0D;
		if (resource == ResourceUtil.methaneID) {
			Iterator<Vehicle> i = getAssociatedVehicles().iterator();
			while (i.hasNext()) {
				double fuelDemand = i.next().getAmountResourceCapacity(resource);
				demand += fuelDemand * transportation_factor * VEHICLE_FUEL_FACTOR;
			}
		}

		return demand;
	}

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
			demand += getFarmingResourceDemand(resource, farm) * cropFarm_factor * FARMING_FACTOR;
		}

		return demand;
	}

	/**
	 * Gets the farming demand for the resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg) for the resource.
	 */
	private double getCropDemand(int resource) {
		double demand = 0D;

		if (settlement.getNumCitizens() == 0)
			return demand;

		HotMeal mainMeal = null;
		HotMeal sideMeal = null;
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person p = i.next();
			String mainDish = p.getFavorite().getFavoriteMainDish();
			String sideDish = p.getFavorite().getFavoriteSideDish();
			mainMeal = getHotMeal(MealConfig.getMainDishList(), mainDish);
			sideMeal = getHotMeal(MealConfig.getSideDishList(), sideDish);
		}

		Iterator<Ingredient> ii = mainMeal.getIngredientList().iterator();
		while (ii.hasNext()) {
			Ingredient it = ii.next();

			AmountResource ar = ResourceUtil.findAmountResource(it.getAmountResourceID());
			if (ar.getType() != null && ar.getType().equalsIgnoreCase("crop")) {
				String tissueName = it.getName() + Farming.TISSUE_CULTURE;

				if (it.getAmountResourceID() == resource) {
					// Tune demand with various factors
					demand += CROP_FACTOR * cropFarm_factor;
				}

				else if (ResourceUtil.findIDbyAmountResourceName(tissueName.toLowerCase()) == resource) {
					// Tune demand with various factors
					demand += TISSUE_CULTURE_FACTOR * cropFarm_factor;
				}
			}
		}

		Iterator<Ingredient> iii = sideMeal.getIngredientList().iterator();
		while (iii.hasNext()) {
			Ingredient it = iii.next();

			AmountResource ar = ResourceUtil.findAmountResource(it.getAmountResourceID());
			if (ar.getType() != null && ar.getType().equalsIgnoreCase("crop")) {
				String tissueName = it.getName() + Farming.TISSUE_CULTURE;

				if (it.getAmountResourceID() == resource) {
					// Tune demand with various factors
					demand += CROP_FACTOR * cropFarm_factor;
				}

				else if (ResourceUtil.findIDbyAmountResourceName(tissueName.toLowerCase()) == resource) {
					// Tune demand with various factors
					demand += TISSUE_CULTURE_FACTOR * cropFarm_factor;
				}
			}
		}

		return demand;
	}


	public HotMeal getHotMeal(List<HotMeal> dishList, String dish) {
		Iterator<HotMeal> i = dishList.iterator();
		while (i.hasNext()) {
			HotMeal hm = i.next();
			if (hm.getMealName().equals(dish))
				return hm;
		}
		return null;
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

	public double getTourismFactor() {
		return tourism_factor;
	}

	/**
	 * Gets the individual greenhouse resource demand
	 *
	 * @param resource
	 * @param farm
	 * @return
	 */
	private double getFarmingResourceDemand(int resource, Farming farm) {
		double demand = 0;

		double averageGrowingCyclesPerOrbit = farm.getAverageGrowingCyclesPerOrbit();
		double totalCropArea = farm.getGrowingArea();
		int solsInOrbit = MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
		double factor = totalCropArea * averageGrowingCyclesPerOrbit / solsInOrbit;
//		CropConfig cropConfig = simulationConfig.getCropConfiguration();

		if (resource == ResourceUtil.waterID) {
			// Average water consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getWaterConsumptionRate() * factor;
		} else if (resource == ResourceUtil.co2ID) {
			// Average co2 consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getCarbonDioxideConsumptionRate() * factor;
		} else if (resource == ResourceUtil.oxygenID) {
			// Average oxygen consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getOxygenConsumptionRate() * factor;
		} else if (resource == ResourceUtil.soilID) {
			// Estimate soil needed for average number of crop plantings for total growing
			// area.
			demand = Crop.NEW_SOIL_NEEDED_PER_SQM * factor;
		} else if (resource == ResourceUtil.fertilizerID) {
			// Estimate fertilizer needed for average number of crop plantings for total
			// growing area.
			// Estimate fertilizer needed when grey water not available.
			demand = (Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM + Crop.FERTILIZER_NEEDED_WATERING) * factor;
		} else if (resource == ResourceUtil.greyWaterID) {
			// NOTE: how to properly get rid of grey water? it should NOT be considered an
			// economically vital resource
			// Average grey water consumption rate of crops per orbit using total growing
			// area.
			// demand = cropConfig.getWaterConsumptionRate() * totalCropArea * solsInOrbit;
			demand = WASTE_WATER_VALUE;
		}

		return demand;
	}


	public double flattenAmountDemand(Good good, double oldDemand) {
		double demand = 0;
		String name = good.getName();
		String type = GoodsUtil.getGoodType(good);

		if (name.contains("polyester"))
			demand = CHEMICAL_DEMAND * .5;

		else if (name.contains("styrene"))
			demand = CHEMICAL_DEMAND * .5;

		else if (name.contains("polyethylene"))
			demand = CHEMICAL_DEMAND * .5;

		else if (type.equalsIgnoreCase("regolith")
				|| type.equalsIgnoreCase("ore")
				|| type.equalsIgnoreCase("mineral")
				|| type.equalsIgnoreCase("rock")
				)
			demand = REGOLITH_DEMAND;

		else if (type.equalsIgnoreCase(GoodsUtil.CHEMICAL))
			demand = CHEMICAL_DEMAND;

		else if (type.equalsIgnoreCase(GoodsUtil.ELEMENT))
			demand = ELEMENT_DEMAND;

		else if (type.equalsIgnoreCase(GoodsUtil.COMPOUND))
			demand = COMPOUND_DEMAND;

		else if (type.equalsIgnoreCase("waste"))
			demand = WASTE_VALUE;

		if (demand == 0)
			return oldDemand;

		return oldDemand * demand;
	}

	public double flattenPartDemand(Good good, double oldDemand) {
		double demand = 0;
		String name = good.getName();
		String type = GoodsUtil.getGoodType(good);

		if (type.equalsIgnoreCase(GoodsUtil.ELECTRICAL)) {
			demand = ELECTRICAL_DEMAND;

			if (name.contains("light"))
				demand *= ELECTRICAL_DEMAND;

			else if (name.contains("resistor"))
				demand *= ELECTRICAL_DEMAND;

			else if (name.contains("capacitor"))
				demand *= ELECTRICAL_DEMAND;

			else if (name.contains("diode"))
				demand *= ELECTRICAL_DEMAND;

		}

//		else if (type.equalsIgnoreCase(GoodsUtil.VEHICLE_PART))
//			demand = (1 + tourism_factor) * VEHICLE_PART_DEMAND;

		else if (type.equalsIgnoreCase(GoodsUtil.INSTRUMENT))
			demand = INSTRUMENT_DEMAND;

		else if (type.equalsIgnoreCase(GoodsUtil.METALLIC)) {
			demand = METALLIC_DEMAND;

			if (name.contains("electrical wire"))
				demand *= ELECTRICAL_DEMAND;

			else if (name.contains("wire connector"))
				demand *= ELECTRICAL_DEMAND;
		}

		else if (type.equalsIgnoreCase(GoodsUtil.UTILITY)) {
			demand = UTILITY_DEMAND;

			if (name.contains("valve"))
				demand *= .05;

			else if (name.contains("pipe"))
				demand *= .05;

			else if (name.contains("tank"))
				demand *= .1;

			else if (name.contains("bottle"))
				demand *= .05;

			else if (name.contains("duct"))
				demand *= .1;

			else if (name.contains("gasket"))
				demand *= .1;
		}

//		else if (type.equalsIgnoreCase(GoodsUtil.KITCHEN))
//			demand = KITCHEN_DEMAND;

		else if (type.equalsIgnoreCase(GoodsUtil.CONSTRUCTION))
			demand = CONSTRUCTION_DEMAND;

		else if (type.equalsIgnoreCase(GoodsUtil.RAW))
			demand = INSTRUMENT_DEMAND;

		if (demand == 0)
			return oldDemand;

		return oldDemand * demand;
	}

	/**
	 * Limit the demand for a particular part.
	 *
	 * @param part   the part.
	 * @param demand the original demand.
	 * @return the flattened demand
	 */
	private double flattenRawPartDemand(Part part, double oldDemand) {
		double demand = 0;
		String name = part.getName();
		// Reduce the demand on the steel/aluminum scrap metal
		// since they can only be produced by salvaging a vehicle
		// therefore it's not reasonable to have high VP

		if (name.contains(SCRAP))
			demand = SCRAP_METAL_DEMAND;
		// May recycle the steel/AL scrap back to ingot
		// Note: the VP of a scrap metal could be heavily influence by VP of regolith

		else if (name.contains(INGOT))
			demand = INGOT_METAL_DEMAND;

		else if (name.contains(SHEET))
			demand = SHEET_METAL_DEMAND;

		else if (name.equalsIgnoreCase(TRUSS))
			demand = SHEET_METAL_DEMAND;

//		if (name.equalsIgnoreCase(STEEL_WIRE))
//			demand = STEEL_WIRE_DEMAND;
//
//		if (name.equalsIgnoreCase(AL_WIRE))
//			demand = AL_WIRE_DEMAND;
//
//		else if (name.contains("wire"))
//			demand = WIRE_DEMAND;
//
//		else if (name.contains("pipe"))
//			demand = PIPE_DEMAND;
//
//		else if (name.contains("valve"))
//			demand = PIPE_DEMAND;
//
//		if (name.equalsIgnoreCase(STEEL_CAN))
//			demand = STEEL_CAN_DEMAND;

		else if (name.contains(STEEL))
			demand = STEEL_DEMAND;

		else if (name.equalsIgnoreCase(BOTTLE))
			demand = BOTTLE_DEMAND;

		else if (name.equalsIgnoreCase(FIBERGLASS_CLOTH))
			demand = FIBERGLASS_DEMAND;

		else if (name.equalsIgnoreCase(FIBERGLASS))
			demand = FIBERGLASS_DEMAND;

		else if (name.equalsIgnoreCase(BRICK))
			demand = BRICK_DEMAND;

		if (demand == 0)
			return oldDemand;

		return demand * oldDemand;

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

		return Math.min(1000, demand);
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
//				double demandOrbit = demandSol; // * MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;

				demand = demandSol;
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

	private double computeVehiclePartsCost(Good good) {
		double result = 0;

		String name = good.getName();

		List<ManufactureProcessInfo> manufactureProcessInfos = new ArrayList<ManufactureProcessInfo>();
		Iterator<ManufactureProcessInfo> i = ManufactureUtil.getAllManufactureProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			for (String n : process.getOutputNames()) {
				if (name.equalsIgnoreCase(n)) {
					manufactureProcessInfos.add(process);
				}
			}
		}

		Iterator<ManufactureProcessInfo> ii = manufactureProcessInfos.iterator();
		while (ii.hasNext()) {
			ManufactureProcessInfo process = ii.next();

			List<ManufactureProcessItem> itemList = process.getInputList();

			for (ManufactureProcessItem pi : itemList) {
				String iName = pi.getName();
//				double iQuantity = pi.getAmount();
				int id = GoodsUtil.getGoodID(iName);
				double value = getGoodValuePerItem(id);
				result += value;
			}
		}

		return result;
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
				demand += manufacturingDemand / 100D;
			}
		}

		return Math.min(1000, demand);
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


		return Math.min(1000, demand);
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

			if (totalItems > 0) {
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

			if (totalItems > 0D) {
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
				return MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_SALT_PER_MEAL;
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
				int numMeals = MealConfig.getDishList().size();
				Iterator<HotMeal> i = MealConfig.getDishList().iterator();
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
//				double amountNeededOrbit = amountNeededSol * MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;
				int numPeople = settlement.getNumCitizens();
				demand = 5 * Math.log(1 + numPeople) * amountNeededSol * DESSERT_FACTOR;
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

		if (buildingStage.isConstructable()) {
			ConstructionStageInfo frameStage = ConstructionUtil.getPrerequisiteStage(buildingStage);
			if (frameStage != null) {
				ConstructionStageInfo foundationStage = ConstructionUtil.getPrerequisiteStage(frameStage);
				if (foundationStage != null) {
					if (frameStage.isConstructable() && foundationStage.isConstructable()) {
						return true;
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
									return true;
								}
							}
						}
					}
				}
			}
		}

		return false;
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

			if (totalItems > 0 ) {
				double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;
				demand = (1D / totalItems) * totalInputsValue;
			}
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
	 * Gets the total amount of a given resource required to build a stage
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
			if (GoodCategory.AMOUNT_RESOURCE == good.getCategory())
				return getAmountOfResourceForSettlement(ResourceUtil.findAmountResource(good.getID()));
			if (GoodCategory.ITEM_RESOURCE == good.getCategory())
				return getNumItemResourceForSettlement(ItemResourceUtil.findItemResource(good.getID()));
			if (GoodCategory.EQUIPMENT == good.getCategory()
					|| GoodCategory.CONTAINER == good.getCategory())
				return getNumberOfEquipmentForSettlement(good, good.getEquipmentType());
			if (GoodCategory.VEHICLE == good.getCategory())
				return getNumberOfVehiclesForSettlement(good.getName());

			return 0;
		}

		else
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
		amount += settlement.getAmountResourceStored(resource.getID());

		// Get amount of resource out on mission vehicles.
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					amount += vehicle.getAmountResourceStored(resource.getID());
			}
		}

		// Get amount of resource carried by people on EVA.
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.isOutside())
				amount += person.getAmountResourceStored(resource.getID());
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

		double value = 1;
		double totalDemand = 0;
		double previous = 0;
		double projected = 0;
		double average = 0;
		double totalSupply = 0;

		// Needed for loading a saved sim
		int solElapsed = marsClock.getMissionSol();
		// Compact and/or clear supply and demand maps every x days
		int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;

		int id = resourceGood.getID();

		if (id >= ResourceUtil.FIRST_ITEM_RESOURCE_ID && id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {

			Part part = (Part) ItemResourceUtil.findItemResource(id);

			if (useCache) {
				return goodsValues.get(id);
			}

			else {

				if (partDemandCache.containsKey(id)) {
					// Get previous demand
					previous = partDemandCache.get(id);
				}

				average = getAverageItemDemand(id, numSol);

				// Get demand for a part.

				// NOTE: the following estimates are for each orbit (Martian year) :

				// Add manufacturing demand.
				projected = getPartManufacturingDemand(part);

				// Add food production demand.
				projected += getPartFoodProductionDemand(part);

				// Add construction demand.
				projected += getPartConstructionDemand(id);

				// Add construction site demand.
				projected += getPartConstructionSiteDemand(id);

				// Calculate individual EVA suit-related part demand.
				projected += getEVASuitPartsDemand(part);

				// Calculate individual attachment part demand.
				projected += getAttachmentPartsDemand(part);

				// Calculate vehicle part demand.
				projected = getVehiclePartDemand(part, projected);

				// Calculate kitchen part demand.
				projected += getKitchenPartDemand(part);

				// Flatten raw part demand.
				projected = flattenRawPartDemand(part, projected);

				// Flatten part demand.
				projected = flattenPartDemand(resourceGood, projected);

//				projected /= MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR;

				if (projected < MIN_DEMAND)
					projected = MIN_DEMAND;

				if (projected > MAX_PROJ_DEMAND)
					projected = MAX_PROJ_DEMAND;

				// Add trade demand.
				double trade = determineTradeDemand(resourceGood, useCache);

//				if (itemDemand < tradeDemand) {
//					itemDemand = tradeDemand;
//				}

//				// Apply the universal damping ratio
//				if (totalDemand / previousDemand > 1)
//					// Reduce the increase
//					totalDemand = previousDemand + (totalDemand - previousDemand) * DAMPING_RATIO;
				//
//				else // if (totalItemDemand / previousItemDemand < 1)
//						// Reduce the decrease
//					totalDemand = previousDemand - (previousDemand - totalDemand) * DAMPING_RATIO;

				// Recalculate the partsDemandCache
				determineRepairPartsDemand();
				// Gets the repair part demand
				double repair = partDemandCache.get(id);

				if (previous == 0) {
					// At the start of the sim
					totalDemand = .4 * repair + .1 * average + .4 * projected + .1 * trade;
				}

				else {
					// Intentionally lose 10% of its value
					totalDemand = .7 * previous + .05 * repair + .05 * average + .05 * projected + .05 * trade;
				}

				if (totalDemand < MIN_DEMAND)
					totalDemand = MIN_DEMAND;

				if (totalDemand > MAX_DEMAND)
					totalDemand = MAX_DEMAND;

				// Save the goods demand
				partDemandCache.put(id, totalDemand);

				// Calculate total item supply

				if (supply == 0)
					supply = settlement.getItemResourceStored(id);

				totalSupply = getAverageItemSupply(id, supply, solElapsed);

				if (totalSupply < MIN_SUPPLY)
					totalSupply = MIN_SUPPLY;

				if (totalSupply > MAX_SUPPLY)
					totalSupply = MAX_SUPPLY;

				// Calculate item value
				value = totalDemand / totalSupply;

				// Check if it surpass the max VP
				if (value > MAX_VP) {
//					System.out.println("deflation: " + id + " " + ItemResourceUtil.findItemResourceName(id) + " " + itemValue);
					// Update deflationIndexMap for other resources of the same category
					value = updateDeflationMap(id, value, resourceGood.getCategory(), true);
				}
				// Check if it falls below 1
				else if (value < MIN_VP) {
					// Update deflationIndexMap for other resources of the same category
					value = updateDeflationMap(id, value, resourceGood.getCategory(), false);
				}

				// Check for inflation and deflation adjustment due to other resources
				value = checkDeflation(id, value);
				// Adjust the value to the average value
				value = tuneToAverageValue(resourceGood, value);
				// Save the value point
				goodsValues.put(id, value);
			}

		}

		return value;
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

	/**
	 * Clears the previous calculation on estimated orbit repair parts
	 */
	public void clearOrbitRepairParts() {
		orbitRepairParts.clear();
	}

	/**
	 * Gets the estimated orbit repair parts by entity
	 *
	 * @param entity
	 * @return
	 */
	private Map<Integer, Number> getEstimatedOrbitRepairParts(Malfunctionable entity) {

		if (!orbitRepairParts.containsKey(entity)) {

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

			orbitRepairParts.put(entity, result);
			return result;
		}

		else {
			return orbitRepairParts.get(entity);
		}
	}

	/**
	 * Gets the outstanding repair parts by entity
	 *
	 * @param entity
	 * @return
	 */
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
//			settlement.getInventory().addItemDemandTotalRequest(part, number);
//			settlement.getInventory().addItemDemand(part, number);
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
			VehicleSpec spec = vehicleConfig.getVehicleSpec(type);
			if (spec.hasPartAttachments()) {
				Iterator<Part> j = spec.getAttachableParts().iterator();
				while (j.hasNext()) {
					Part part = j.next();
					double demand = ATTACHMENT_PARTS_DEMAND;
					if (result.containsKey(part.getID()))
						demand += result.get(part.getID()).intValue();
					result.put(ItemResourceUtil.findIDbyItemResourceName(part.getName()), demand);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the attachment part demand.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getAttachmentPartsDemand(Part part) {
		for (int id : ItemResourceUtil.ATTACHMENTS_ID) {
			if (part.getID() == id) {
				return ATTACHMENT_PARTS_DEMAND * getPartDemandValue(id);
			}
		}
		return 0;
	}

	/**
	 * Gets the eva related demand for a part.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getEVASuitPartsDemand(Part part) {
		for (int id : ItemResourceUtil.EVASUIT_PARTS_ID) {
			if (part.getID() == id) {
				return eVASuitMod * EVA_PARTS_VALUE * getPartDemandValue(id);
			}
		}
		return 0;
	}

	/**
	 * Gets the EVA suit demand from its part.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getWholeEVASuitDemand() {
		double demand = 0;
		for (int id : ItemResourceUtil.EVASUIT_PARTS_ID) {
			demand += getPartDemandValue(id);
		}
		return demand;
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
				demand += manufacturingDemand * (1 + techLevel);
			}
		}
		return Math.min(1000, demand);
	}

	/**
	 * Limit the demand for kitchen parts.
	 *
	 * @param part   the part.
	 * @param demand the original demand.
	 * @return the flattened demand
	 */
	private double getKitchenPartDemand(Part part) {
		for (int id : ItemResourceUtil.KITCHEN_WARE_ID) {
			if (part.getID() == id)
				return getPartDemandValue(id) * KITCHEN_DEMAND;
		}
		return 0;
	}

	private double getVehiclePartDemand(Part part, double projected) {
		String type = part.getType();
		if (type.equalsIgnoreCase(VEHICLE)) {
			return projected * (1 + tourism_factor/30.0) * VEHICLE_PART_DEMAND;
		}
		return 0;
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

			if (totalNumber > 0) {
				double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;
				demand = totalInputsValue * (partNumber / totalNumber);
			}
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
		number += settlement.getItemResourceStored(resource.getID());

		// Get number of resources out on mission vehicles.
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					number += vehicle.getItemResourceStored(resource.getID());
			}
		}

		// Get number of resources carried by people on EVA.
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.isOutside())
				number += person.getItemResourceStored(resource.getID());
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
		double value = 0D;
		double average = 0D;
		double previous = 0D;
		double totalDemand = 0;

		int id = equipmentGood.getID();

		if (useCache) {
			return goodsValues.get(id);
		}

		else {
			previous = getEquipmentDemandValue(id);

			// Determine average demand.
			average = determineEquipmentDemand(EquipmentType.convertID2Type(id));
//			logger.info(settlement, equipmentGood.getName() + " average demand: " + average);
			// Add trade demand.
//            demand += determineTradeDemand(equipmentGood, useCache);
			double trade = determineTradeDemand(equipmentGood, useCache);
//			if (tradeDemand > demand) {
//				demand = tradeDemand;
//			}

			if (previous == 0) {
				totalDemand = .5 * average + .5 * trade;
			}

			else {
				// Intentionally lose 10% of its value
				totalDemand = .75 * previous + .1 * average + .05 * trade;
			}

			equipmentDemandCache.put(id, totalDemand);

			if (supply == 0)
				supply = settlement.findNumContainersOfType(EquipmentType.convertID2Type(id));

			value = totalDemand / (1 + Math.log(supply + 1D));

			// Check if it surpass the max VP
			if (value > MAX_VP) {
				// Update deflationIndexMap for other resources of the same category
				value = updateDeflationMap(id, value, equipmentGood.getCategory(), true);
			}
			// Check if it falls below 1
			else if (value < MIN_VP) {
				// Update deflationIndexMap for other resources of the same category
				value = updateDeflationMap(id, value, equipmentGood.getCategory(), false);
			}

			// Check for inflation and deflation adjustment due to other equipment
			value = checkDeflation(id, value);
			// Adjust the value to the average value
			value = tuneToAverageValue(equipmentGood, value);
			// Save the value point
			goodsValues.put(id, value);

			return value;
		}
	}

	/**
	 * Determines the demand for a type of equipment.
	 *
	 * @param equipmentClass the equipment class.
	 * @return demand (# of equipment).
	 */
	private double determineEquipmentDemand(EquipmentType type) {
		double baseDemand = 1;

		double areologistFactor = (1 + getJobNum(JobType.AREOLOGIST)) / 3.0;

		if (type == EquipmentType.ROBOT)
			return ROBOT_FACTOR ;

		// Determine number of EVA suits that are needed
		else if (type == EquipmentType.EVA_SUIT) {
			// Add the whole EVA Suit demand.
			baseDemand += getWholeEVASuitDemand();

			return baseDemand += eVASuitMod * EVA_SUIT_VALUE;
		}

		else {
			// Determine the number of containers that are needed.

			double containerCapacity = ContainerUtil.getContainerCapacity(type);
			double totalPhaseOverfill = 0D;

			Iterator<AmountResource> i = ResourceUtil.getAmountResources().iterator();
			while (i.hasNext()) {
				AmountResource resource = i.next();

				if (ContainerUtil.getContainerClassToHoldResource(resource.getID()) == type) {
					double settlementCapacity = settlement.getAmountResourceCapacity(resource.getID());

					double resourceDemand = getAmountDemandValue(resource.getID());

					if (resourceDemand > settlementCapacity) {
						double resourceOverfill = resourceDemand - settlementCapacity;
						totalPhaseOverfill += resourceOverfill;
					}
				}
			}

			baseDemand += totalPhaseOverfill * containerCapacity;

			double ratio = computeUsageFactor(type);

			switch (type) {
				case BAG:
					return Math.max(baseDemand * ratio *settlement.getRegolithCollectionRate() / 1_000, 1000) * areologistFactor * BAG_DEMAND;

				case LARGE_BAG:
					return Math.max(baseDemand * ratio * CollectRegolith.REQUIRED_LARGE_BAGS, 1000) * LARGE_BAG_DEMAND;

				case BARREL:
					return Math.max(baseDemand * ratio * CollectIce.REQUIRED_BARRELS, 1000) * areologistFactor * BARREL_DEMAND;

				case SPECIMEN_BOX:
					return Math.max(baseDemand * ratio * Exploration.REQUIRED_SPECIMEN_CONTAINERS, 1000) * areologistFactor * SPECIMEN_BOX_DEMAND;

				case GAS_CANISTER:
					return Math.max(baseDemand * ratio * PROJECTED_GAS_CANISTERS, 1000) * areologistFactor * GAS_CANISTER_DEMAND;

				default:
					throw new IllegalArgumentException("Do not know how to calculate demand for " + type);
			}
		}
	}

	/**
	 * Computes the usage factor (the used number of container / the total number)
	 * of a type of container
	 *
	 * @param containerType
	 * @return the usage factor
	 */
	private double computeUsageFactor(EquipmentType containerType) {
		int numUsed = 0;

		Collection<Container> equipmentList = settlement.findContainersOfType(containerType);

		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && (vehicle.getSettlement() == null)) {
					equipmentList.addAll(vehicle.findContainersOfType(containerType));
				}
			}
		}

		double total = equipmentList.size();
		for(Container c: equipmentList) {
			if (c.getStoredMass() > 0)
				numUsed++;
		}

		return  (1 + numUsed) / (1 + total);
	}


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
	private double getNumberOfEquipmentForSettlement(Good good, EquipmentType equipmentType) {
		double number = 0D;

		// Get number of the equipment in settlement storage.
		number += settlement.findNumEmptyContainersOfType(equipmentType, false);

		// Get number of equipment out on mission vehicles.
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					number += vehicle.findNumEmptyContainersOfType(equipmentType, false);
			}
		}

		// Get number of equipment carried by people on EVA.
		Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
		while (j.hasNext()) {
			Person person = j.next();
			if (person.isOutside())
				number += person.findNumEmptyContainersOfType(equipmentType, false);
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
		double totalDemand = 0;

		String vehicleType = vehicleGood.getName();
		int id = vehicleGood.getID();

		boolean buy = false;

		double currentNum = getNumberOfVehiclesForSettlement(vehicleType);

		if (supply == 0) {
			supply = currentNum;
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

			double projected = determineVehicleProjectedDemand(vehicleGood, useCache);

			double average = computeVehiclePartsCost(vehicleGood);
//			System.out.println(vehicleGood.getName() + "  average: " + average);
			double previous = getVehicleDemandValue(id);

			double tradeValue = determineTradeVehicleValue(vehicleGood, useCache);
//			if (tradeValue > demandValue) {
//				demandValue = tradeValue;
//			}

			if (previous == 0) {
				totalDemand = .5 * average + .25 * projected + .25 * tradeValue;
			}

			else {
				// Intentionally lose 10% of its value
				totalDemand = .75 * previous + .05 * average + .05 * projected + .05 * tradeValue;
			}

			vehicleDemandCache.put(id, totalDemand);

			if (buy) {
				vehicleBuyValueCache.put(vehicleType, totalDemand);
			} else {
				vehicleSellValueCache.put(vehicleType, totalDemand);
			}

			value = (totalDemand) / (supply + 1);

			// Check if it surpass the max VP
			if (value > MAX_VP) {
				// Update deflationIndexMap for other resources of the same category
				value = updateDeflationMap(id, value, vehicleGood.getCategory(), true);
			}
			// Check if it falls below 1
			else if (value < MIN_VP) {
				// Update deflationIndexMap for other resources of the same category
				value = updateDeflationMap(id, value, vehicleGood.getCategory(), false);
			}

			// Check for inflation and deflation adjustment due to other vehicle
			value = checkDeflation(id, value);
			// Adjust the value to the average value
			value = tuneToAverageValue(vehicleGood, value);
			// Save the value point
			goodsValues.put(id, value);
		}

		return value;
	}

	/**
	 * Determines the vehicle projected demand
	 *
	 * @param vehicleGood
	 * @param useCache
	 * @return the vehicle demand
	 */
	private double determineVehicleProjectedDemand(Good vehicleGood, boolean useCache) {
		double demand = 0D;

		String vehicleType = vehicleGood.getName();

		if (useCache) {
			demand = vehicleDemandCache.get(vehicleGood.getID());
		}

		else {
			boolean buy = false;

			if (vehicleType.equalsIgnoreCase(LightUtilityVehicle.NAME)) {
				demand = determineLUVValue(buy);
			}

			else if (vehicleType.equalsIgnoreCase(Drone.NAME)) {
				double tradeMissionValue = determineMissionVehicleDemand(TRADE_MISSION, vehicleType, buy);
				if (tradeMissionValue > demand) {
					demand = tradeMissionValue;
				}
				demand += determineDroneValue(buy);
			}

			else {
				double travelToSettlementMissionValue = determineMissionVehicleDemand(TRAVEL_TO_SETTLEMENT_MISSION,
						vehicleType, buy);
				if (travelToSettlementMissionValue > demand) {
					demand = travelToSettlementMissionValue;
				}

				double explorationMissionValue = determineMissionVehicleDemand(EXPLORATION_MISSION, vehicleType, buy);
				if (explorationMissionValue > demand) {
					demand = explorationMissionValue;
				}

				double collectIceMissionValue = determineMissionVehicleDemand(COLLECT_ICE_MISSION, vehicleType, buy);
				if (collectIceMissionValue > demand) {
					demand = collectIceMissionValue;
				}

				double rescueMissionValue = determineMissionVehicleDemand(RESCUE_SALVAGE_MISSION, vehicleType, buy);
				if (rescueMissionValue > demand) {
					demand = rescueMissionValue;
				}

				double tradeMissionValue = determineMissionVehicleDemand(TRADE_MISSION, vehicleType, buy);
				if (tradeMissionValue > demand) {
					demand = tradeMissionValue;
				}

				double collectRegolithMissionValue = determineMissionVehicleDemand(COLLECT_REGOLITH_MISSION, vehicleType,
						buy);
				if (collectRegolithMissionValue > demand) {
					demand = collectRegolithMissionValue;
				}

				double miningMissionValue = determineMissionVehicleDemand(MINING_MISSION, vehicleType, buy);
				if (miningMissionValue > demand) {
					demand = miningMissionValue;
				}

				double constructionMissionValue = determineMissionVehicleDemand(CONSTRUCT_BUILDING_MISSION, vehicleType,
						buy);
				if (constructionMissionValue > demand) {
					demand = constructionMissionValue;
				}

				double salvageMissionValue = determineMissionVehicleDemand(SALVAGE_BUILDING_MISSION, vehicleType, buy);
				if (salvageMissionValue > demand) {
					demand = salvageMissionValue;
				}

				double areologyFieldMissionValue = determineMissionVehicleDemand(AREOLOGY_STUDY_FIELD_MISSION,
						vehicleType, buy);
				if (areologyFieldMissionValue > demand) {
					demand = areologyFieldMissionValue;
				}

				double biologyFieldMissionValue = determineMissionVehicleDemand(BIOLOGY_STUDY_FIELD_MISSION, vehicleType,
						buy);
				if (biologyFieldMissionValue > demand) {
					demand = biologyFieldMissionValue;
				}

				double emergencySupplyMissionValue = determineMissionVehicleDemand(EMERGENCY_SUPPLY_MISSION, vehicleType,
						buy);
				if (emergencySupplyMissionValue > demand) {
					demand = emergencySupplyMissionValue;
				}
			}

			if (vehicleType.equalsIgnoreCase(VehicleType.CARGO_ROVER.getName()))
				demand *= (.5 + transportation_factor) * CARGO_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.TRANSPORT_ROVER.getName()))
				demand *= (.5 + transportation_factor) * TRANSPORT_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.EXPLORER_ROVER.getName()))
				demand *= (.5 + transportation_factor) * EXPLORER_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.DELIVERY_DRONE.getName()))
				demand *= (.5 + transportation_factor) * DRONE_VEHICLE_FACTOR;
			else if (vehicleType.equalsIgnoreCase(VehicleType.LUV.getName()))
				demand *= (.5 + transportation_factor) * LUV_VEHICLE_FACTOR;

//			vehicleDemandCache.put(vehicleGood.getID(), demand);
		}

		return demand;
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

	private double determineMissionVehicleDemand(String missionType, String vehicleType, boolean buy) {

		double demand = determineMissionVehicleDemand(missionType);

		double currentCapacity = 0D;
		boolean soldFlag = false;
		Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle v = i.next();
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
		VehicleSpec v = vehicleConfig.getVehicleSpec(vehicleType);
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

			boolean hasAreologyLab = v.hasLab() && v.getLabTechSpecialties().contains(ScienceType.AREOLOGY);
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
	private double getVehicleRange(VehicleSpec v) {
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
	private double getDroneRange(VehicleSpec v) {
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
			if (tradeCache.containsKey(good.getID()))
				return tradeCache.get(good.getID());
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
			tradeCache.put(good.getID(), bestTradeValue);
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

//	public double getWaterValue() {
//		return waterValue;
//	}
//
//	public void setWaterValue(double value) {
//		waterValue = value;
//	}

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
		if (goodsValues.containsKey(id))
			return goodsValues.get(id);
		else
			logger.severe(settlement, " - Good Value of " + id + " not valid.");
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
		if (partDemandCache.containsKey(id))
			return partDemandCache.get(id);
		else
			logger.severe(settlement,
					" - Part  " + ItemResourceUtil.findItemResourceName(id) + "(" + id + ")" + " not valid.");
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
		if (amountDemandCache.containsKey(id))
			return amountDemandCache.get(id);
		else
			logger.severe(settlement,
					" - Amount resource " + ResourceUtil.findAmountResourceName(id) + "(" + id + ")" + " not valid.");
		return 1;
	}

	/**
	 * Gets the demand value of an equipment.
	 *
	 * @param equipment id.
	 * @return demand value
	 */
	public double getEquipmentDemandValue(int id) {
		if (equipmentDemandCache.containsKey(id))
			return equipmentDemandCache.get(id);
		else
			logger.severe(settlement,
					" - Equipment " + EquipmentType.convertID2Type(id) + "(" + id + ")" + " not valid.");
		return 5;
	}

	public double getVehicleDemandValue(int id) {
		if (vehicleDemandCache.containsKey(id))
			return vehicleDemandCache.get(id);
		else
			logger.severe(settlement, " - Vehicle " + VehicleType.convertID2Type(id) + "(" + id + ")" + " not valid.");
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
		tradeCache.clear();
		tradeCache = null;
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
