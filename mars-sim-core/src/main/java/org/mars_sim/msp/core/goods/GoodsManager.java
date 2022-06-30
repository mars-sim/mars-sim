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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionProcessItem;
import org.mars_sim.msp.core.food.FoodProductionUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
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
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleSpec;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A manager for computing the values of goods at a settlement.
 */
public class GoodsManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	/** Initialized logger. */
	private static SimLogger logger = SimLogger.getLogger(GoodsManager.class.getName());

	private static final String SCRAP = "scrap";
	private static final String INGOT = "ingot";
	private static final String SHEET = "sheet";
	private static final String TRUSS = "steel truss";
	private static final String STEEL = "steel";

	private static final String HEAT_PROBE = "heat probe";
	private static final String BOTTLE = "bottle";
	private static final String FIBERGLASS = "fiberglass";
	private static final String METHANE = "methane";
	private static final String BRICK = "brick";
	private static final String METEORITE = "meteorite";

	private static final int MALFUNCTION_REPAIR_COEF = 50;
	private static final int MAINTENANCE_REPAIR_COEF = 10;

	// Number modifiers for outstanding repair and maintenance parts and EVA parts.
	private static final int BASE_REPAIR_PART = 150;
	private static final int BASE_MAINT_PART = 15;
	private static final int BASE_EVA_SUIT = 1;

	private static final double ATTACHMENT_PARTS_DEMAND = 1.2;

	private static final int PROJECTED_GAS_CANISTERS = 10;

	private static final double WASTE_WATER_VALUE = .05D;
	private static final double WASTE_VALUE = .05D;
	private static final double USEFUL_WASTE_VALUE = 1.05D;

	private static final int EVA_SUIT_VALUE = 60;

	private static final int EVA_PARTS_VALUE = 20;

	private static final double ORE_VALUE = .9;
	private static final double MINERAL_VALUE = .9;
	private static final double ROCK_VALUE = .6;

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
	private static final double TISSUE_CULTURE_FACTOR = .75;
	private static final double LEAVES_FACTOR = .5;
	private static final double CROP_FACTOR = .1;

	private static final double CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR = 100D;
	private static final double CONSTRUCTION_SITE_REQUIRED_PART_FACTOR = 100D;

	private static final double MIN_SUPPLY = 0.01;
	private static final double MIN_DEMAND = 0.01;
	
	private static final int MAX_SUPPLY = 5_000;
	private static final int MAX_DEMAND = 10_000;
	
	private static final int MAX_PROJ_DEMAND = 100_000;
	private static final double MIN_PROJ_DEMAND = 0.001;
	
	private static final int MAX_VP = 10_000;
	private static final double MIN_VP = 0.01;
	
	private static final double PERCENT_110 = 1.1;
	private static final double PERCENT_90 = .9;
	private static final double PERCENT_81 = .81;
	
	private static final double MAX_FINAL_VP = 5_000D;

	private static final double LIFE_SUPPORT_MAX = 100;

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

	private static final double SCRAP_METAL_DEMAND = .01;
	private static final double INGOT_METAL_DEMAND = .01;
	private static final double SHEET_METAL_DEMAND = .1;
	private static final double TRUSS_DEMAND = .05;
	private static final double STEEL_DEMAND = .1;

	private static final double BOTTLE_DEMAND = .02;
	private static final double FIBERGLASS_DEMAND = .1;
	private static final double BRICK_DEMAND = .005;

	private static final double REGOLITH_DEMAND_FACTOR = 30;
	private static final double CHEMICAL_DEMAND_FACTOR = .01;
	private static final double COMPOUND_DEMAND_FACTOR = .01;
	private static final double ELEMENT_DEMAND_FACTOR = .1;

	private static final double ELECTRICAL_DEMAND = .8;
	private static final double INSTRUMENT_DEMAND = 1.2;
	private static final double METALLIC_DEMAND = .7;
	private static final double UTILITY_DEMAND = .7;
	private static final double TOOL_DEMAND = .8;
	private static final double KITCHEN_DEMAND = 1.5;
	private static final double CONSTRUCTION_DEMAND = .8;

	/** VP probability modifier. */
	public static final double ICE_VALUE_MODIFIER = 5D;
	private static final double WATER_VALUE_MODIFIER = 1D;

	public static final double SOIL_VALUE_MODIFIER = .5;
	public static final double REGOLITH_VALUE_MODIFIER = 25D;
	public static final double SAND_VALUE_MODIFIER = 5D;
	public static final double CONCRETE_VALUE_MODIFIER = .5D;
	public static final double ROCK_MODIFIER = 0.99D;
	public static final double METEORITE_MODIFIER = 1.05;
	public static final double SALT_VALUE_MODIFIER = .2;

	public static final double OXYGEN_VALUE_MODIFIER = .02D;
	public static final double METHANE_VALUE_MODIFIER = .5D;

	private static final double LIFE_SUPPORT_FACTOR = .005;
	private static final double FOOD_VALUE_MODIFIER = .1;

	// Data members
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

	private Map<Integer, Double> goodsValues = new HashMap<>();
	private Map<Integer, Double> tradeCache = new HashMap<>();

	private Map<Integer, Double> demandCache = new HashMap<>();
	private Map<Integer, Double> supplyCache = new HashMap<>();

	private Map<Integer, Integer> deflationIndexMap = new HashMap<>();

	private Map<Malfunctionable, Map<Integer, Number>> orbitRepairParts = new HashMap<>();

	/** A standard list of resources to be excluded in buying negotiation. */
	private static List<Good> exclusionBuyList = null;
	/** A standard list of buying resources in buying negotiation. */
	private static List<Good> buyList = null;

	private Settlement settlement;

	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static CropConfig cropConfig = simulationConfig.getCropConfiguration();
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
	 * Populates the cache maps.
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

		// Preload the good cache
		for(Good good : GoodsUtil.getGoodsList()) {
			demandCache.put(good.getID(), good.getDefaultDemandValue());
			supplyCache.put(good.getID(), good.getDefaultSupplyValue());

		}
	}

	/**
	 * Gets a list of item to be excluded in a buying negotiation
	 *
	 * @return
	 */
	private static List<Good> getExclusionBuyList() {
		if (exclusionBuyList == null) {
			exclusionBuyList = new ArrayList<>();
			for (VehicleType type : VehicleType.values()) {
				exclusionBuyList.add(GoodsUtil.getVehicleGood(type));
			}
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.regolithID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.iceID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.co2ID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.coID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.sandID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.greyWaterID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.blackWaterID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.compostID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.eWasteID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.toxicWasteID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.cropWasteID));
			// Note: add vehicles to this list ?
		}
		return exclusionBuyList;
	}

	/**
	 * Update the Good values of all good.
	 */
	public void updateGoodValues() {
 		// Update the goods value gradually with the use of buffers
		for(Good g: GoodsUtil.getGoodsList()) {
			determineGoodValue(g);
			
			if (initialized) {
				g.adjustGoodValue();
			}
		}
				
		initialized = true;
	}

	/**
	 * Determines the value of a good.
	 *
	 * @param good     the good to check.
	 * @return value of good.
	 */
	public double determineGoodValue(Good good) {
		return determineGoodValue(good, 0, false);
	}
	
	/**
	 * Determines the value of a good with a designated value of supply.
	 *
	 * @param good     the good to check.
	 * @param supply   the current supply (# of items) of the good.
	 * @param useCache use demand and trade caches to determine value?
	 * @return value of good.
	 */
	private double determineGoodValue(Good good, double supply, boolean useCache) {
		if (good != null) {

			double previousDemand = getDemandValue(good);

			if (useCache) {
				return previousDemand / supply;
			}

			double value = 0D;

			switch(good.getCategory()) {
			case AMOUNT_RESOURCE:
				value = determineAmountResourceGoodValue(good, supply);
				break;

			case ITEM_RESOURCE:
				value = determineItemResourceGoodValue(good, supply);
				break;

			case EQUIPMENT:
			case CONTAINER:
				value = determineEquipmentGoodValue(good, supply);
				break;

			case VEHICLE:
				value = determineVehicleGoodValue(good, supply);
				break;

			case ROBOT:
				// Why no Robot good value ?
				value = 0D;
				break;
			}

			settlement.fireUnitUpdate(UnitEventType.GOODS_VALUE_EVENT, good);
			
			return value;
		} else
			logger.severe(settlement, "Good is null.");

		return 0;
	}

	/**
	 * Determines the value of an amount resource.
	 *
	 * @param good the amount resource good.
	 * @param supply       the current supply (kg) of the good.
	 * @return value the value point 
	 */
	private double determineAmountResourceGoodValue(Good good, double supply) {	
		int id = good.getID();
		double previousDemand = getDemandValue(good);

		double value = 0;

		double average = 0;
		double trade = 0;
		double totalDemand = 0;
		
		double totalSupply = 0;

		// Needed for loading a saved sim
		int solElapsed = marsClock.getMissionSol();
		// Compact and/or clear supply and demand maps every x days
		int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;

		// Calculate the average demand
		average = getAverageCapAmountDemand(good, numSol);

		// Calculate projected demand
		double projected = limitMaxMin(
			// Tune ice demand.
			(computeIceProjectedDemand(id)
			// Tune regolith projected demand.
			+ computeRegolithProjectedDemand(id)
			// Tune life support demand if applicable.
			+ getLifeSupportDemand(id)
			// Tune potable water usage demand if applicable.
			+ getPotableWaterUsageDemand(id)
			// Tune toiletry usage demand if applicable.
			+ getToiletryUsageDemand(id)
			// Tune vehicle demand if applicable.
			+ getVehicleFuelDemand(id)
			// Tune farming demand.
			+ getFarmingDemand(id)
			// Tune the crop demand
			+ getCropDemand(id)
			// Tune resource processing demand.
			+ getResourceProcessingDemand(id)
			// Tune manufacturing demand.
			+ getResourceManufacturingDemand(id)
			// Tune food production related demand.
			+ getResourceFoodProductionDemand(id)
			// Tune demand for the ingredients in a cooked meal.
			+ getResourceCookedMealIngredientDemand(id)
			// Tune dessert demand.
			+ getResourceDessertDemand(id)
			// Tune construction demand.
			+ getResourceConstructionDemand(id)
			// Tune construction site demand.
			+ getResourceConstructionSiteDemand(id)
			// Adjust the demand on various waste products with the disposal cost.
			+ getWasteDisposalSinkCost(id)
			// Adjust the demand on minerals and ores.
			+ getMineralDemand(id))
			// Flatten certain types of demand.
			* flattenAmountDemand(good), MIN_PROJ_DEMAND, MAX_PROJ_DEMAND);
			
		// Add trade value. Cache is always false if this method is called
		trade = limitMaxMin(determineTradeDemand(good), MIN_DEMAND, MAX_DEMAND);
		
		if (previousDemand == 0) {
			// At the start of the sim
			totalDemand = limitMaxMin(.5 * average + .1 * projected + .01 * trade, MIN_DEMAND, MAX_DEMAND);
		}
		else {
			// Intentionally loses .01% 
			// Allows only very small fluctuations of demand as possible
			totalDemand = limitMaxMin(.9998 * previousDemand + .00005 * projected + .00005 * trade, MIN_DEMAND, MAX_DEMAND);
		}

		// Save the goods demand
		setDemandValue(good, totalDemand);
		
		// Calculate total supply
		totalSupply = limitMaxMin(getAverageAmountSupply(settlement.getAmountResourceStored(id), numSol), MIN_SUPPLY, MAX_SUPPLY);

		// Store the average supply
		setSupplyValue( good, totalSupply);
		
		// Calculate the value point
		value = totalDemand / totalSupply;

		// Check if it surpasses MAX_VP
		if (value > MAX_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), true);
		}
		// Check if it falls below MIN_VP
		else if (value < MIN_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), false);
		}

		// Check for inflation and deflation adjustment due to other resources
		value = checkDeflation(id, value);
		// Adjust the value to the average value
		value = tuneToAverageValue(good, value);
		// Save the value point
		goodsValues.put(id, value);
		
		return value;
	}


	/**
	 * Tunes the value of a good to be closer to the national average.
	 * 
	 * @param good
	 * @param value
	 * @return
	 */
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
			
			if (newAve0 < MIN_VP)
				newAve0 = MIN_VP;
		}

		good.setAverageGoodValue(newAve0);

		return newAve0;
	}


	/**
	 * Checks the deflation of a resource.
	 * 
	 * @param id
	 * @param value
	 * @return
	 */
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

		else if (index < 0) {  // if the index is negative, need to inflate the value
			for (int i = 0; i < -index; i++) {
				double newValue = value * PERCENT_110;
				if (newValue >= 1_000) {
					// if it is larger than 1000, then do not need to further increase it
				}
				else
					value = newValue;
			}
		}

		deflationIndexMap.put(id, 0);
		return value;
	}

	/**
	 * Updates the deflation index Map.
	 *
	 * @param id     the id of the resource that cause the deflation
	 * @param value  the demand value to be adjusted
	 * @param exceed true if it surpasses the upper limit; false if it falls below
	 *               the lower limit
	 * @return the adjusted value
	 */
	private double updateDeflationMap(int id, double value, GoodCategory type, boolean exceed) {

		for (int i : deflationIndexMap.keySet()) {
			if (id != i) {
				if (type == GoodsUtil.getGood(i).getCategory()) {
					// This good is of the same category as the one that cause the
					// inflation/deflation
					int oldIndex = deflationIndexMap.get(i);
					if (exceed) {
						// reduce twice
						deflationIndexMap.put(id, oldIndex + 2);
					}
				}
				else { // This good is of different category
					int oldIndex = deflationIndexMap.get(i);
					if (exceed) {
						// reduce once
						deflationIndexMap.put(id, oldIndex + 1);
					}
				}
			}
		}

		if (exceed)
			return value * PERCENT_81;

		return value;
	}

	/**
	 * Gets the average cap amount demand.
	 * 
	 * @param resource
	 * @return
	 */
	private double getAverageCapAmountDemand(Good resource, int solElapsed) {
		return capLifeSupportAmountDemand(resource.getID(), getAverageAmountDemand(resource, solElapsed));		
	}

	/**
	 * Gets the new demand.
	 *
	 * @param resource
	 * @param projectedDemand
	 * @param solElapsed
	 * @return
	 */
	private double getAverageAmountDemand(Good resource, int solElapsed) {
		return Math.min(10, getDemandValue(resource) / solElapsed);
	}
	
	/**
	 * Gets the total supply for the amount resource.
	 *
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private double getAverageAmountSupply(double supplyStored, int solElapsed) {
		double aveSupply = 0.5 + Math.log((1 + 5 * supplyStored) / solElapsed);

		if (aveSupply < 0.5)
			aveSupply = 0.5;

		return aveSupply;
	}

	/**
	 * Gets the total supply for the item resource.
	 *
	 * @param resource
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private double getAverageItemSupply(double supplyStored, int solElapsed) {
		double aveSupply = 1.0 + Math.log((1 + 2 * supplyStored) / solElapsed);

		if (aveSupply < 1.0)
			aveSupply = 1.0;
		
		return aveSupply;
	}

	/**
	 * Gets the total supply for the equipment.
	 *
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private double getAverageEquipmentSupply(double supplyStored, int solElapsed) {
		double aveSupply = 0.25 + Math.log((1 + supplyStored) / solElapsed);

		if (aveSupply < 0.5)
			aveSupply = 0.5;

		return aveSupply;
	}
	
	/**
	 * Gets the total supply for the vehicle.
	 *
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private double getAverageVehicleSupply(double supplyStored, int solElapsed) {
		double aveSupply = 0.05 + Math.log((1 + 0.5 * supplyStored) / solElapsed);

		if (aveSupply < 0.5)
			aveSupply = 0.5;

		return aveSupply;
	}
	
	/**
	 * Gets the new item demand.
	 *
	 * @param resource
	 * @param solElapsed
	 * @return
	 */
	private double getAverageItemDemand(Good resource, int solElapsed) {
		double aveDemand = getDemandValue(resource) / solElapsed;
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

	/**
	 * Limits the demand of life support resources.
	 * 
	 * @param resource
	 * @param demand
	 * @return
	 */
	private double capLifeSupportAmountDemand(int resource, double demand) {
		if (resource == ResourceUtil.foodID
				|| resource == ResourceUtil.oxygenID || resource == ResourceUtil.waterID 
				|| resource == ResourceUtil.hydrogenID || resource == ResourceUtil.methaneID)
			// Cap the resource at less than LIFE_SUPPORT_MAX
			return Math.min(LIFE_SUPPORT_MAX, demand);
		return demand;
	}

	/**
	 * Gets a particular mineral demand.
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
				return demand * (.3 * regolithVP + .6 * vp) / vp;
			}

			// Checks if this resource is a ROCK type
			GoodType type = ResourceUtil.findAmountResource(resource).getGoodType();
			if (type != null && type == GoodType.ROCK) {
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
			GoodType type = ResourceUtil.findAmountResource(resource).getGoodType();
			if (type != null && type == GoodType.WASTE) {
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
		if (resource == ResourceUtil.regolithID
				|| resource == ResourceUtil.regolithBID
				|| resource == ResourceUtil.regolithCID
				|| resource == ResourceUtil.regolithDID) {
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
			if (ar != null && ar.getGoodType() == GoodType.CROP) {
				String tissueName = it.getName() + Farming.TISSUE;

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
			if (ar != null && ar.getGoodType() == GoodType.CROP) {
				String tissueName = it.getName() + Farming.TISSUE;

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


	/**
	 * Gets an instance of the hot meal.
	 * 
	 * @param dishList
	 * @param dish
	 * @return
	 */
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


	/**
	 * Flattens the amount demand on certain selected resources or types of resources.
	 * 
	 * @param good
	 * @return
	 */
	private double flattenAmountDemand(Good good) {
		double demand = 0;
		String name = good.getName();
		GoodType type = good.getGoodType();
		
		if (name.contains("polyester")
				|| name.contains("styrene")
				|| name.contains("polyethylene"))
			demand = CHEMICAL_DEMAND_FACTOR;
		
		else if (type == GoodType.REGOLITH
				|| type == GoodType.ORE
				|| type == GoodType.MINERAL)
			demand = REGOLITH_DEMAND_FACTOR;

		else if (type == GoodType.ROCK)
			demand = 2;
		
		else if (type == GoodType.CHEMICAL)
			demand = CHEMICAL_DEMAND_FACTOR;

		else if (type == GoodType.ELEMENT)
			demand = ELEMENT_DEMAND_FACTOR;

		else if (type == GoodType.COMPOUND)
			demand = COMPOUND_DEMAND_FACTOR;

		else if (type == GoodType.WASTE)
			demand = WASTE_VALUE;

		else
			return 1;

		return demand;
	}

	/**
	 * Limits the part demand based on types.
	 * 
	 * @param good
	 * @return
	 */
	public double flattenPartDemand(Good good) {
		String name = good.getName();
		GoodType type = good.getGoodType();

		if (name.contains("electrical wire"))
			return 0.1 * ELECTRICAL_DEMAND;

		if (name.contains("wire connector"))
			return 0.5 * ELECTRICAL_DEMAND;
		
		if (name.contains("pipe"))
			return .1;
		
		if (name.contains("valve"))
			return .05;

		if (name.contains("plastic"))
			return 1.1;
		
		// Note that there are 'plastic pipe', 'plastic sheet', 'plastic tubing'
		if (name.contains("tank"))
			return .1;
		
		if (name.contains(HEAT_PROBE))
			return .05;

		if (name.contains(BOTTLE))
			return BOTTLE_DEMAND;

		if (name.contains("duct"))
			return .1;

		if (name.contains("gasket"))
			return .1;
		
		if (type == GoodType.ELECTRICAL
				|| name.contains("light")
				|| name.contains("resistor")
				|| name.contains("capacitor")
				|| name.contains("diode"))
			return ELECTRICAL_DEMAND;

		if (type == GoodType.INSTRUMENT)
			return INSTRUMENT_DEMAND;

		if (type == GoodType.METALLIC)
			return  METALLIC_DEMAND;
		
		if (type == GoodType.UTILITY)
			return UTILITY_DEMAND;
		
		if (type == GoodType.TOOL)
			return TOOL_DEMAND;

		if (type == GoodType.CONSTRUCTION)
			return CONSTRUCTION_DEMAND;

		if (type == GoodType.INSTRUMENT)
			return INSTRUMENT_DEMAND;

		return 1;
	}

	/**
	 * Limits the demand for a particular raw material part.
	 *
	 * @param part   the part.
	 */
	private double flattenRawPartDemand(Part part) {
		String name = part.getName();
		// Reduce the demand on the steel/aluminum scrap metal
		// since they can only be produced by salvaging a vehicle
		// therefore it's not reasonable to have high VP

		if (name.contains(SCRAP))
			return SCRAP_METAL_DEMAND;
		// May recycle the steel/AL scrap back to ingot
		// Note: the VP of a scrap metal could be heavily influence by VP of regolith

		if (name.contains(INGOT))
			return INGOT_METAL_DEMAND;

		if (name.contains(SHEET))
			return SHEET_METAL_DEMAND;

		if (name.contains(TRUSS))
			return TRUSS_DEMAND;

		if (name.contains(STEEL))
			return STEEL_DEMAND;

		if (name.contains(FIBERGLASS))
			return FIBERGLASS_DEMAND;

		if (name.equalsIgnoreCase(BRICK))
			return BRICK_DEMAND;

		if (name.equalsIgnoreCase("glass sheet"))
			return .5;
		
		return 1;
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
			double processDemand = getResourceProcessDemand(i.next(), resource);
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
					outputValue += (getDemandValue(GoodsUtil.getGood(output)) * outputRate);
				}
			}

			double resourceInputRate = process.getMaxInputResourceRate(resource);

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputValue - powerValue) * RESOURCE_PROCESSING_INPUT_FACTOR;

			if (totalInputsValue > 0D) {
				double demandMillisol = resourceInputRate;
				double demandSol = demandMillisol * 1000D;
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

	/**
	 * Computes vehicle parts cost
	 * 
	 * @param good
	 * @return
	 */
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

			double totalItems = 0D;
			Iterator<ManufactureProcessItem> k = process.getInputList().iterator();
			while (k.hasNext()) {
				ManufactureProcessItem item = k.next();
				totalItems += item.getAmount();
			}

			// Determine value of required process power.
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
	 * Gets all resource amounts required to build a stage including all pre-stages.
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
	 * Gets the total amount of a given resource required to build a stage.
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
	 * Gets a map of all parts required to build a stage including all pre-stages.
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

			// Add parts from second pre-stage, if any.
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
	 * pre-stages.
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
	 * Determines the value of an item resource.
	 *
	 * @param good the resource good to check.
	 * @param supply       the current supply (# items) of the good.
	 * @return value (Value Points / item)
	 */
	private double determineItemResourceGoodValue(Good good, double supply) {
		int id = good.getID();
		double previousDemand = getDemandValue(good);
		
		double value = 1;
		double totalDemand = 0;
		double average = 0;
		double totalSupply = 0;

		// Needed for loading a saved sim
		int solElapsed = marsClock.getMissionSol();
		// Compact and/or clear supply and demand maps every x days
		int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;

		Part part = (Part) ItemResourceUtil.findItemResource(id);

		average = getAverageItemDemand(good, numSol);

		// Get demand for a part.
		// NOTE: the following estimates are for each orbit (Martian year) :
		double projected = limitMaxMin(
			// Add manufacturing demand.					
			(getPartManufacturingDemand(part)
			// Add food production demand.
			+ getPartFoodProductionDemand(part)
			// Add construction demand.
			+ getPartConstructionDemand(id)
			// Add construction site demand.
			+ getPartConstructionSiteDemand(id)
			// Calculate individual EVA suit-related part demand.
			+ getEVASuitPartsDemand(good)
			// Calculate individual attachment part demand.
			+ getAttachmentPartsDemand(good)
			// Calculate kitchen part demand.
			+ getKitchenPartDemand(good)
			// Calculate vehicle part demand.
			* getVehiclePartDemand(part)
			// Flatten raw part demand.
			* flattenRawPartDemand(part)
			// Flatten certain part demand.
			* flattenPartDemand(good)), MIN_PROJ_DEMAND, MAX_PROJ_DEMAND);

		// Add trade demand.
		double trade = limitMaxMin(determineTradeDemand(good), MIN_DEMAND, MAX_DEMAND);

		// Recalculate the partsDemandCache
		determineRepairPartsDemand();
		// Gets the repair part demand
		double repair = demandCache.get(id);

		if (previousDemand == 0) {
			// At the start of the sim
			totalDemand = limitMaxMin(.4 * repair 
					+ .1 * average 
					+ .4 * projected 
					+ .1 * trade, 
					MIN_DEMAND, MAX_DEMAND);
		}

		else {
			// Intentionally lose 1% of its value
			totalDemand = limitMaxMin(.986 * previousDemand + .001 * repair 
					+ .001 * average 
					+ .001 * projected 
					+ .001 * trade, 
					MIN_DEMAND, MAX_DEMAND);
		}
		
		// Save the goods demand
		setDemandValue(good, totalDemand);
		
		// Calculate total supply
		totalSupply = limitMaxMin(getAverageItemSupply(settlement.getItemResourceStored(id), numSol), 
							MIN_SUPPLY, MAX_SUPPLY);

		// Save the average supply
		setSupplyValue(good, totalSupply);

		// Calculate item value
		value = totalDemand / totalSupply;

		// Check if it surpass the max VP
		if (value > MAX_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), true);
		}
		// Check if it falls below 1
		else if (value < MIN_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), false);
		}

		// Check for inflation and deflation adjustment due to other resources
		value = checkDeflation(id, value);
		// Adjust the value to the average value
		value = tuneToAverageValue(good, value);
				
		// Save the value point
		goodsValues.put(id, value);

		return value;
	}

	/**
	 * Determines the number demand for all parts at the settlement.
	 *
	 * @return map of parts and their demand.
	 */
	private void determineRepairPartsDemand() {
		Map<Good, Double> partsProbDemand = new HashMap<>();

		// Get all malfunctionables associated with settlement.
		Iterator<Malfunctionable> i = MalfunctionFactory.getAssociatedMalfunctionables(settlement).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();

			// Determine wear condition modifier.
			double wearModifier = (entity.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;

			// Estimate repair parts needed per orbit for entity.
			sumPartsDemand(partsProbDemand, getEstimatedOrbitRepairParts(entity), wearModifier);

			// Add outstanding repair parts required.
			sumPartsDemand(partsProbDemand, getOutstandingRepairParts(entity), MALFUNCTION_REPAIR_COEF);

			// Estimate maintenance parts needed per orbit for entity.
			sumPartsDemand(partsProbDemand, getEstimatedOrbitMaintenanceParts(entity), wearModifier);

			// Add outstanding maintenance parts required.
			sumPartsDemand(partsProbDemand, getOutstandingMaintenanceParts(entity), MAINTENANCE_REPAIR_COEF);
		}
	
		// Add demand for vehicle attachment parts.
		sumPartsDemand(partsProbDemand, getVehicleAttachmentParts(), 1D);

		// Store in parts demand cache.
		for(Entry<Good, Double> entry : partsProbDemand.entrySet()) {
			Good part = entry.getKey();

			if (getDemandValue(part) < 1)
				setDemandValue(part, 1.0);
			else
				setDemandValue(part, entry.getValue());
		}
	}

	/**
	 * Sums the additional parts number map into a total parts number map.
	 *
	 * @param totalPartsDemand      the total parts number; will be updated
	 * @param additionalPartsDemand the additional parts number.
	 * @param multiplier            the multiplier for the additional parts number.
	 */
	private void sumPartsDemand(Map<Good, Double> totalPartsDemand, Map<Integer, Number> additionalPartsDemand,
			double multiplier) {

		for (Entry<Integer, Number> item : additionalPartsDemand.entrySet()) {
			Good part = GoodsUtil.getGood(item.getKey());
			double number = item.getValue().doubleValue() * multiplier;
			if (totalPartsDemand.containsKey(part))
				number += totalPartsDemand.get(part);
			totalPartsDemand.put(part, number);
		}
	}

	/**
	 * Clears the previous calculation on estimated orbit repair parts.
	 */
	public void clearOrbitRepairParts() {
		orbitRepairParts.clear();
	}

	/**
	 * Gets the estimated orbit repair parts by entity.
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
	 * Gets the outstanding repair parts by entity.
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

	/**
	 * Gets an estimated orbit maintenance parts.
	 * 
	 * @param entity
	 * @return
	 */
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

	/**
	 * Gets outstanding maintenance parts.
	 * 
	 * @param entity
	 * @return
	 */
	private Map<Integer, Number> getOutstandingMaintenanceParts(Malfunctionable entity) {
		Map<Integer, Number> result = new HashMap<>();

		Map<Integer, Integer> maintParts = entity.getMalfunctionManager().getMaintenanceParts();
		Iterator<Integer> i = maintParts.keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			int number = (int) Math.round(maintParts.get(part) * maintenanceMod);
			result.put(part, number);
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
	private double getAttachmentPartsDemand(Good part) {
		if (ItemResourceUtil.ATTACHMENTS_ID.contains(part.getID())) {
			return ATTACHMENT_PARTS_DEMAND * getDemandValue(part);
		}
		return 0;
	}

	/**
	 * Gets the eva related demand for a part.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getEVASuitPartsDemand(Good part) {
		if (ItemResourceUtil.EVASUIT_PARTS_ID.contains(part.getID())) {
			return eVASuitMod * EVA_PARTS_VALUE * getDemandValue(part);
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
			demand += getDemandValue(GoodsUtil.getGood(id));
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
		return Math.min(MAX_DEMAND, demand);
	}

	/**
	 * Limit the demand for kitchen parts.
	 *
	 * @param part   the part.
	 * @param demand the original demand.
	 * @return the flattened demand
	 */
	private double getKitchenPartDemand(Good part) {
		if (ItemResourceUtil.KITCHEN_WARE_ID.contains(part.getID())) {
			return getDemandValue(part) * KITCHEN_DEMAND;
		}
		return 0;
	}

	/**
	 * Gets the vehicle part factor for part demand.
	 * 
	 * @param part
	 * @return
	 */
	private double getVehiclePartDemand(Part part) {
		GoodType type = part.getGoodType();
		if (type == GoodType.VEHICLE) {
			return (1 + tourism_factor/30.0) * VEHICLE_PART_DEMAND;
		}
		return 1;
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
				double foodProductionDemand = getPartFoodProductionProcessDemand(part, i.next());
				demand += foodProductionDemand;
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
	 * Determines the value of an equipment.
	 *
	 * @param good the equipment good to check.
	 * @param supply        the current supply (# of items) of the good.
	 * @return the value (value points)
	 */
	private double determineEquipmentGoodValue(Good good, double supply) {
		int id = good.getID();
		double previousDemand = getDemandValue(good);

		double totalDemand = 0;
		
		EquipmentType equipmentType = EquipmentType.convertID2Type(id);		
		// Needed for loading a saved sim
		int solElapsed = marsClock.getMissionSol();
		// Compact and/or clear supply and demand maps every x days
		int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;

		// Determine average demand.
		double average = determineEquipmentDemand(equipmentType);

		double totalSupply = 0;
		
		if (supply > 0)
			// For estimating trade/delivery mission, use specified resources as supply
			totalSupply = getAverageEquipmentSupply(supply, numSol);
		else
			totalSupply = getAverageEquipmentSupply(settlement.findNumContainersOfType(equipmentType), numSol);
		
		totalSupply = limitMaxMin(totalSupply, MIN_SUPPLY, MAX_SUPPLY);
		
		setSupplyValue(good, totalSupply);
		
		// This method is not using cache
		double trade = determineTradeDemand(good);

		trade = limitMaxMin(trade, MIN_DEMAND, MAX_DEMAND);
		
		if (previousDemand == 0) {
			totalDemand = .5 * average + .5 * trade;
		}

		else {
			// Intentionally lose 2% of its value
			totalDemand = .97 * previousDemand + .005 * average + .005 * trade;
		}
		
		totalDemand = limitMaxMin(totalDemand, MIN_DEMAND, MAX_DEMAND);
		
		setDemandValue(good, totalDemand);

		double value = totalDemand / totalSupply;

		// Check if it surpass the max VP
		if (value > MAX_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), true);
		}
		// Check if it falls below 1
		else if (value < MIN_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), false);
		}

		// Check for inflation and deflation adjustment due to other equipment
		value = checkDeflation(id, value);
		// Adjust the value to the average value
		value = tuneToAverageValue(good, value);

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
	private double determineEquipmentDemand(EquipmentType type) {
		double baseDemand = 1;

		double areologistFactor = (1 + JobUtil.numJobs(JobType.AREOLOGIST, settlement)) / 3.0;

		// Determine number of EVA suits that are needed
		if (type == EquipmentType.EVA_SUIT) {
			// Add the whole EVA Suit demand.
			baseDemand += getWholeEVASuitDemand();

			return baseDemand + eVASuitMod * EVA_SUIT_VALUE;
		}

		// Determine the number of containers that are needed.

		double containerCapacity = ContainerUtil.getContainerCapacity(type);
		double totalPhaseOverfill = 0D;

		for(AmountResource resource : ResourceUtil.getAmountResources()) {
			if (ContainerUtil.getContainerClassToHoldResource(resource.getID()) == type) {
				double settlementCapacity = settlement.getAmountResourceCapacity(resource.getID());

				double resourceDemand = demandCache.get(resource.getID());

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

	/**
	 * Computes the usage factor (the used number of container / the total number)
	 * of a type of container.
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
	 * Determines the value of a vehicle good.
	 *
	 * @param good the vehicle good.
	 * @param supply      the current supply (# of vehicles) of the good.
	 * @return the value (value points).
	 */
	private double determineVehicleGoodValue(Good good, double supply) {
		
		int id = good.getID();
		double previousDemand = getDemandValue(good);

		// Needed for loading a saved sim
		int solElapsed = marsClock.getMissionSol();
		// Compact and/or clear supply and demand maps every x days
		int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;

		// Calculate total supply
		double totalSupply = limitMaxMin(getAverageVehicleSupply(good.getNumberForSettlement(settlement), numSol), MIN_SUPPLY, MAX_SUPPLY);
		
		setSupplyValue(good, totalSupply);
			
		// Doesn't use cache value sin this method
		double projected = limitMaxMin(determineVehicleProjectedDemand(good), MIN_PROJ_DEMAND, MAX_PROJ_DEMAND);
		
		double average = computeVehiclePartsCost(good);
		
		double trade = limitMaxMin(determineTradeVehicleValue(good), MIN_DEMAND, MAX_DEMAND);
		
		double totalDemand = 0D;
		if (previousDemand == 0) {
			totalDemand = .5 * average + .25 * projected + .25 * trade;
		}

		else {
			// Intentionally lose 2% of its value
			totalDemand = .97 * previousDemand + .003 * average + .003 * projected + .003 * trade;
		}

		totalDemand = limitMaxMin(totalDemand, MIN_DEMAND, MAX_DEMAND);
				
		setDemandValue(good, totalDemand);

		double value = totalDemand / totalSupply;
		
		// Check if it surpass the max VP
		if (value > MAX_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), true);
		}
		// Check if it falls below 1
		else if (value < MIN_VP) {
			// Update deflationIndexMap for other resources of the same category
			value = updateDeflationMap(id, value, good.getCategory(), false);
		}

		// Check for inflation and deflation adjustment due to other vehicle
		value = checkDeflation(id, value);
		// Adjust the value to the average value
		value = tuneToAverageValue(good, value);
		// Save the value point
		goodsValues.put(id, value);
		
		return value;
	}

	/**
	 * Determines the vehicle projected demand
	 *
	 * @param vehicleGood
	 * @return the vehicle demand
	 */
	private double determineVehicleProjectedDemand(Good vehicleGood) {
		double demand = 0D;

		VehicleType vehicleType = VehicleType.convertNameToVehicleType(vehicleGood.getName());

		boolean buy = false;

		if (vehicleType == VehicleType.LUV) {
			demand = determineLUVValue(buy);
		}

		else if (vehicleType == VehicleType.DELIVERY_DRONE) {
			double tradeMissionValue = determineMissionVehicleDemand(MissionType.TRADE, vehicleType, buy);
			if (tradeMissionValue > demand) {
				demand = tradeMissionValue;
			}
			demand += determineDroneValue(buy);
		}

		else {
			// Check all missions and take highest demand
			for (MissionType missionType : MissionType.values()) {
				double missionDemand = determineMissionVehicleDemand(missionType,
												vehicleType, buy);
				if (missionDemand > demand) {
					demand = missionDemand;
				}
			}
		}

		if (vehicleType == VehicleType.CARGO_ROVER)
			demand *= (.5 + transportation_factor) * CARGO_VEHICLE_FACTOR;
		else if (vehicleType == VehicleType.TRANSPORT_ROVER)
			demand *= (.5 + transportation_factor) * TRANSPORT_VEHICLE_FACTOR;
		else if (vehicleType == VehicleType.EXPLORER_ROVER)
			demand *= (.5 + transportation_factor) * EXPLORER_VEHICLE_FACTOR;
		else if (vehicleType == VehicleType.DELIVERY_DRONE)
			demand *= (.5 + transportation_factor) * DRONE_VEHICLE_FACTOR;
		else if (vehicleType == VehicleType.LUV)
			demand *= (.5 + transportation_factor) * LUV_VEHICLE_FACTOR;

		return demand;
	}

	/**
	 * Determines the trade vehicle value
	 *
	 * @param vehicleGood
	 * @return the trade vehicle value
	 */
	private double determineTradeVehicleValue(Good vehicleGood) {
		double tradeDemand = determineTradeDemand(vehicleGood);
		double supply = vehicleGood.getNumberForSettlement(settlement);
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
		demand += Math.min(7, JobUtil.numJobs(JobType.PILOT, settlement) * 1.1);

		// Add demand for mining missions by engineers.
		demand += Math.min(8, JobUtil.numJobs(JobType.TRADER, settlement) * 1.2);

		Good droneGood = GoodsUtil.getVehicleGood(Drone.NAME);
		double supply = droneGood.getNumberForSettlement(settlement);
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
		demand += Math.min(10, JobUtil.numJobs(JobType.AREOLOGIST, settlement) * 1.3);

		// Add demand for construction missions by architects.
		demand += Math.min(8, JobUtil.numJobs(JobType.ARCHITECT, settlement) * 1.2);

		// Add demand for mining missions by engineers.
		demand += Math.min(6, JobUtil.numJobs(JobType.ENGINEER, settlement) * 1.1);

		Good luvGood = GoodsUtil.getVehicleGood(LightUtilityVehicle.NAME);
		double supply = luvGood.getNumberForSettlement(settlement);
		if (!buy)
			supply--;
		if (supply < 0D)
			supply = 0D;

		return demand / Math.log(supply + 2) * LUV_FACTOR * Math.log(Math.min(24, settlement.getNumCitizens()));
	}

	/**
	 * Determines the mission vehicle demand based on vehicle type and mission type.
	 * 
	 * @param missionType
	 * @param vehicleType
	 * @param buy
	 * @return
	 */
	private double determineMissionVehicleDemand(MissionType missionType, VehicleType vehicleType, boolean buy) {

		double demand = determineMissionJob(missionType);

		double currentCapacity = 0D;
		boolean soldFlag = false;
		Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle v = i.next();
			if (!buy && !soldFlag && (v.getVehicleType() == vehicleType))
				soldFlag = true;
			else
				currentCapacity += determineMissionVehicleCapacity(missionType, v.getVehicleType());
		}

		double vehicleCapacity = determineMissionVehicleCapacity(missionType, vehicleType);

		double baseValue = (demand / (currentCapacity + 1D)) * vehicleCapacity;

		return baseValue;
	}

	/**
	 * Determines the mission vehicle demand based on mission type and job numbers.
	 * 
	 * @param missionType
	 * @return
	 */
	private double determineMissionJob(MissionType missionType) {
		// TODO should come from MissionMeta classes
		switch(missionType) {
			case BUILDING_CONSTRUCTION:
			case BUILDING_SALVAGE:
				return JobUtil.numJobs(JobType.ARCHITECT, settlement);
		
		case TRAVEL_TO_SETTLEMENT:
		case RESCUE_SALVAGE_VEHICLE:
			return JobUtil.numJobs(JobType.PILOT, settlement)
					* ((double) settlement.getNumCitizens() 
					/ (double) settlement.getPopulationCapacity());

		case COLLECT_ICE:
			return Math.min(getDemandValue(GoodsUtil.getGood(ResourceUtil.iceID)), 100);
		
		case TRADE:
		case DELIVERY:
			return JobUtil.numJobs(JobType.TRADER, settlement);
		
		case COLLECT_REGOLITH:
			return Math.min(getDemandValue(GoodsUtil.getGood(ResourceUtil.regolithID)), 100);
		
		case MINING:
		case AREOLOGY:
		case EXPLORATION:
			return JobUtil.numJobs(JobType.AREOLOGIST, settlement);
		
		case BIOLOGY:
			return JobUtil.numJobs(JobType.BIOLOGIST, settlement);
		
		case METEOROLOGY:
			return JobUtil.numJobs(JobType.METEOROLOGIST, settlement);
		
		case EMERGENCY_SUPPLY:
			return Math.max(unitManager.getSettlementNum() - 1D, 0);
		}

		return 0;
	}

	/**
	 * Determines the mission vehicle capacity.
	 * 
	 * @param missionType
	 * @param vehicleType
	 * @return
	 */
	private double determineMissionVehicleCapacity(MissionType missionType, VehicleType vehicleType) {
		double capacity = 0D;

		VehicleSpec v = vehicleConfig.getVehicleSpec(vehicleType.getName());
		int crewCapacity = v.getCrewSize();

		// TODO This logic should be pushed into the MissionMeta to remove knowledge of different Mission types.
		switch (missionType) {
		case TRAVEL_TO_SETTLEMENT: {
				if (crewCapacity >= 2)
					capacity = 1D;
				capacity *= crewCapacity / 8D;

				double range = getVehicleRange(v);
				capacity *= range / 2000D;
			} break;

		case EXPLORATION: {
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
			} break;

		case COLLECT_ICE:
		case COLLECT_REGOLITH: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				if (cargoCapacity < 1250D)
					capacity = 0D;

				double range = getVehicleRange(v);
				if (range == 0D)
					capacity = 0D;
			} break;

		case RESCUE_SALVAGE_VEHICLE: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double range = getVehicleRange(v);
				capacity *= range / 2000D;
			}
			break;

		case TRADE:
		case EMERGENCY_SUPPLY: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				capacity *= cargoCapacity / 10000D;

				double range = getVehicleRange(v);
				capacity *= range / 2000D;
			} break;

		case DELIVERY: {
				capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				capacity *= cargoCapacity / 10000D;

				double range = getDroneRange(v);
				capacity *= range / 2000D;			
			} break;

		case MINING: {
				if (crewCapacity >= 2)
					capacity = 1D;

				double cargoCapacity = v.getTotalCapacity();
				if (cargoCapacity < 1000D)
					capacity = 0D;

				double range = getVehicleRange(v);
				if (range == 0D)
					capacity = 0D;
			} break;

		case AREOLOGY: {
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
			} break;

		case BIOLOGY: {
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
			} break;

		case METEOROLOGY: {
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
			} break;
		
			default:
				capacity = 1D;
				break;
		}

		return capacity;
	}

	/**
	 * Gets the range of the vehicle type.
	 *
	 * @param v {@link VehicleSpec}.
	 * @return range (km)
	 */
	private double getVehicleRange(VehicleSpec v) {
		double range = 0D;

		double fuelCapacity = v.getCargoCapacity(METHANE);
		double fuelEfficiency = v.getDriveTrainEff();
		range = fuelCapacity * fuelEfficiency * Vehicle.SOFC_CONVERSION_EFFICIENCY;

		double baseSpeed = v.getBaseSpeed();
		double distancePerSol = baseSpeed / SPEED_TO_DISTANCE;

		int crewSize = v.getCrewSize();

		// Check food capacity as range limit.
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = v.getCargoCapacity(ResourceUtil.FOOD);
		double foodSols = foodCapacity / (foodConsumptionRate * crewSize);
		double foodRange = distancePerSol * foodSols / 3D;
		if (foodRange < range)
			range = foodRange;

		// Check water capacity as range limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = v.getCargoCapacity(ResourceUtil.WATER);
		double waterSols = waterCapacity / (waterConsumptionRate * crewSize);
		double waterRange = distancePerSol * waterSols / 3D;
		if (waterRange < range)
			range = waterRange;

		// Check oxygen capacity as range limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = v.getCargoCapacity(ResourceUtil.OXYGEN);
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

		double fuelCapacity = v.getCargoCapacity(METHANE);
		double fuelEfficiency = v.getDriveTrainEff();
		return fuelCapacity * fuelEfficiency * Vehicle.SOFC_CONVERSION_EFFICIENCY;
	}


	/**
	 * Determines the trade demand for a good at a settlement.
	 *
	 * @param good          the good.
	 * @return the trade demand.
	 */
	private double determineTradeDemand(Good good) {

		double bestTradeValue = 0D;

		for (Settlement tempSettlement : unitManager.getSettlements()) {
			if (tempSettlement != settlement) {
				double baseValue = tempSettlement.getGoodsManager().getGoodValuePerItem(good);
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

	/**
	 * Prepare the goods manager for a vehicle load calculation.
	 */
	public void prepareForLoadCalculation() {
		// Clear vehicle buy and sell value caches.
//		vehicleBuyValueCache.clear();
//		vehicleSellValueCache.clear();
	}

	/**
	 * Gets the nth power
	 *
	 * @return
	 */
	private int getNthPower(double num) {
		int power = 0;
		int base = 2;
		int n = (int) num;
		while (n != 1) {
			n = n / base;
			--power;
		}

		return -power;
	}

	private int computeLevel(double ratio) {
		double lvl = 0;
		if (ratio < 1) {
			lvl = 0;
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

	private double computeModifier(int baseValue, int level) {
		double mod = 0;
		if (level == 1) {
			mod = baseValue;
		} else if (level < 1) {
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

	public static List<Good> getBuyList() {
		if (buyList == null) {
			buyList = new ArrayList<>(GoodsUtil.getGoodsList());
			buyList.removeAll(getExclusionBuyList());
		}
		return buyList;
	}

	/**
	 * Gets the price per item for a good
	 *
	 * @param id the good id
	 * @return
	 */
	public double getPricePerItem(int id) {
		return getPrice(GoodsUtil.getGood(id));
	}

	/**
	 * Gets the price for a good
	 *
	 * @param good the good
	 * @return
	 */
	public double getPrice(Good good) {
		double value = getGoodValuePerItem(good);

		return good.getPrice(settlement, value);
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
	 * Gets the demand value of a resource.
	 *
	 * @param good's id.
	 * @return demand value
	 */
	public double getAmountDemandValue(int id) {
		if (demandCache.containsKey(id))
			return demandCache.get(id);
		else
			logger.severe(settlement,
					" - Amount resource " + ResourceUtil.findAmountResourceName(id) + "(" + id + ")" + " not valid.");
		return 1;
	}

	public double getDemandValue(Good good) {
		return demandCache.get(good.getID());
	}

	private void setDemandValue(Good good, double newValue) {
		//double oldValue = getDemandValue(good);
		demandCache.put(good.getID(), newValue);

		//logger.info(settlement, "Demand cache update for " + good.getName() + " old=" + oldValue + " new=" + newValue
		//			+ " size=" + demandCache.size());
	}

	private void setSupplyValue(Good good, double newValue) {
		supplyCache.put(good.getID(), newValue);
	}

	public double getSupplyValue(Good good) {
		return supplyCache.get(good.getID());
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
	 * Bounds a prescribed parameter with upper and lower allowable limit.
	 * 
	 * @param param
	 * @param min
	 * @param max
	 * @return
	 */
	private static double limitMaxMin(double param, double min, double max) {
		return Math.max(min, Math.min(max, param));
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
		demandCache.clear();
		demandCache = null;
		tradeCache.clear();
		tradeCache = null;

		deflationIndexMap = null;

		supplyCache = null;
			
		orbitRepairParts = null;

		exclusionBuyList = null;
		buyList = null;
		
		GoodsUtil.destroyGoods();
	}
}
