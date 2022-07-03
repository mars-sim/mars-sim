/*
 * Mars Simulation Project
 * GoodsManager.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.goods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A manager for computing the values of goods at a settlement.
 */
public class GoodsManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	/** Initialized logger. */
	private static SimLogger logger = SimLogger.getLogger(GoodsManager.class.getName());

	// Number modifiers for outstanding repair and maintenance parts and EVA parts.
	private static final int BASE_REPAIR_PART = 150;
	private static final int BASE_MAINT_PART = 15;
	private static final int BASE_EVA_SUIT = 1;

	private static final double ATTACHMENT_PARTS_DEMAND = 1.2;

	static final double MANUFACTURING_INPUT_FACTOR = 2D;
	static final double CONSTRUCTING_INPUT_FACTOR = 2D;

	static final double FOOD_PRODUCTION_INPUT_FACTOR = .1;

	private static final double MIN_SUPPLY = 0.01;
	private static final double MIN_DEMAND = 0.01;
	
	private static final int MAX_SUPPLY = 5_000;
	static final int MAX_DEMAND = 10_000;
	
	private static final int MAX_VP = 10_000;
	private static final double MIN_VP = 0.01;
	
	private static final double PERCENT_110 = 1.1;
	private static final double PERCENT_90 = .9;
	private static final double PERCENT_81 = .81;
	
	private static final double MAX_FINAL_VP = 5_000D;

	private static final double CROPFARM_BASE = 1;
	private static final double MANU_BASE = 1;
	private static final double RESEARCH_BASE = 1.5;
	private static final double TRANSPORT_BASE = 1;
	private static final double TRADE_BASE = 1;
	private static final double TOURISM_BASE = 1;
	private static final double BUILDERS_BASE = 1;

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
	private double builders_factor = 1;

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

	private static UnitManager unitManager;

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
		// Preload the good cache
		for(Good good : GoodsUtil.getGoodsList()) {
			int id = good.getID();
			goodsValues.put(id, 1D);
			tradeCache.put(id, 0D);
			deflationIndexMap.put(id, 0);
			demandCache.put(id, good.getDefaultDemandValue());
			supplyCache.put(id, good.getDefaultSupplyValue());

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
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.eWasteID));
			exclusionBuyList.add(GoodsUtil.getGood(ResourceUtil.toxicWasteID));
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
	 * Determines the value of a good. This reclaculated the Supply & Demand in addition.
	 *
	 * @param good     the good to check.
	 * @return value of good.
	 */
	public double determineGoodValue(Good good) {
		if (good != null) {
			// Refresh the Supply and Demand values
			good.refreshSupplyDemandValue(this);

			int id = good.getID();
		
			// Calculate the value point
			double totalSupply = supplyCache.get(id);
			double totalDemand = demandCache.get(id);
			double value = totalDemand / totalSupply;

			// Check if it surpasses MAX_VP
			if (value > MAX_VP) {
				// Update deflationIndexMap for other resources of the same category
				value = updateDeflationMap(id, value, good.getCategory(), true);
			}
			// Check if it falls below MIN_VP
			else if (value < MIN_VP) {
				// Update deflationIndexMap for other resources of the same category
				updateDeflationMap(id, value, good.getCategory(), false);
			}

			// Check for inflation and deflation adjustment due to other resources
			value = checkDeflation(id, value);
			// Adjust the value to the average value
			value = tuneToAverageValue(good, value);

			// Save the value point if it has changed
			double oldValue = goodsValues.get(id);
			if (oldValue != value) {
				goodsValues.put(id, value);

				settlement.fireUnitUpdate(UnitEventType.GOODS_VALUE_EVENT, good);
			}

			return value;
		} else
			logger.severe(settlement, "Good is null.");

		return 0;
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

	public void setBuildersFactor(double value) {
		builders_factor = value * BUILDERS_BASE;
	}

	public double getBuildersFactor() {
		return builders_factor;
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
	// private Map<Integer, Number> getEstimatedOrbitRepairParts(Malfunctionable entity) {

	// 	if (!orbitRepairParts.containsKey(entity)) {

	// 		Map<Integer, Number> result = new HashMap<>();

	// 		MalfunctionManager manager = entity.getMalfunctionManager();

	// 		// Estimate number of malfunctions for entity per orbit.
	// 		double orbitMalfunctions = manager.getEstimatedNumberOfMalfunctionsPerOrbit();

	// 		// Estimate parts needed per malfunction.
	// 		Map<Integer, Double> partsPerMalfunction = manager.getRepairPartProbabilities();

	// 		// Multiply parts needed by malfunctions per orbit.
	// 		Iterator<Integer> i = partsPerMalfunction.keySet().iterator();
	// 		while (i.hasNext()) {
	// 			Integer part = i.next();
	// 			result.put(part, partsPerMalfunction.get(part) * orbitMalfunctions);
	// 		}

	// 		orbitRepairParts.put(entity, result);
	// 		return result;
	// 	}

	// 	else {
	// 		return orbitRepairParts.get(entity);
	// 	}
	// }

	/**
	 * Gets the outstanding repair parts by entity.
	 *
	 * @param entity
	 * @return
	 */
	// private Map<Integer, Number> getOutstandingRepairParts(Malfunctionable entity) {
	// 	Map<Integer, Number> result = new HashMap<>(0);

	// 	Iterator<Malfunction> i = entity.getMalfunctionManager().getMalfunctions().iterator();
	// 	while (i.hasNext()) {
	// 		Malfunction malfunction = i.next();
	// 		Map<Integer, Integer> repairParts = malfunction.getRepairParts();
	// 		Iterator<Integer> j = repairParts.keySet().iterator();
	// 		while (j.hasNext()) {
	// 			Integer part = j.next();
	// 			int number = (int) Math.round(repairParts.get(part) * repairMod);
	// 			if (result.containsKey(part))
	// 				number += result.get(part).intValue();
	// 			result.put(part, number);
	// 		}
	// 	}

	// 	return result;
	// }

	/**
	 * Gets an estimated orbit maintenance parts.
	 * 
	 * @param entity
	 * @return
	 */
	// private Map<Integer, Number> getEstimatedOrbitMaintenanceParts(Malfunctionable entity) {
	// 	Map<Integer, Number> result = new HashMap<>();

	// 	MalfunctionManager manager = entity.getMalfunctionManager();

	// 	// Estimate number of maintenances for entity per orbit.
	// 	double orbitMaintenances = manager.getEstimatedNumberOfMaintenancesPerOrbit();

	// 	// Estimate parts needed per maintenance.
	// 	Map<Integer, Double> partsPerMaintenance = manager.getMaintenancePartProbabilities();

	// 	// Multiply parts needed by maintenances per orbit.
	// 	Iterator<Integer> i = partsPerMaintenance.keySet().iterator();
	// 	while (i.hasNext()) {
	// 		Integer part = i.next();
	// 		result.put(part, partsPerMaintenance.get(part) * orbitMaintenances);
	// 	}

	// 	return result;
	// }

	/**
	 * Gets outstanding maintenance parts.
	 * 
	 * @param entity
	 * @return
	 */
	// private Map<Integer, Number> getOutstandingMaintenanceParts(Malfunctionable entity) {
	// 	Map<Integer, Number> result = new HashMap<>();

	// 	Map<Integer, Integer> maintParts = entity.getMalfunctionManager().getMaintenanceParts();
	// 	Iterator<Integer> i = maintParts.keySet().iterator();
	// 	while (i.hasNext()) {
	// 		Integer part = i.next();
	// 		int number = (int) Math.round(maintParts.get(part) * maintenanceMod);
	// 		result.put(part, number);
	// 	}

	// 	return result;
	// }

	/**
	 * Gets the part demand for vehicle attachments.
	 *
	 * @return map of parts and demand number.
	 */
	// private Map<Integer, Number> getVehicleAttachmentParts() {
	// 	Map<Integer, Number> result = new HashMap<>();

	// 	Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
	// 	while (i.hasNext()) {
	// 		String type = i.next().getDescription().toLowerCase();
	// 		VehicleSpec spec = vehicleConfig.getVehicleSpec(type);
	// 		if (spec.hasPartAttachments()) {
	// 			Iterator<Part> j = spec.getAttachableParts().iterator();
	// 			while (j.hasNext()) {
	// 				Part part = j.next();
	// 				double demand = ATTACHMENT_PARTS_DEMAND;
	// 				if (result.containsKey(part.getID()))
	// 					demand += result.get(part.getID()).intValue();
	// 				result.put(ItemResourceUtil.findIDbyItemResourceName(part.getName()), demand);
	// 			}
	// 		}
	// 	}

	// 	return result;
	// }

	/**
	 * Determines the trade demand for a good at a settlement.
	 *
	 * @param good          the good.
	 * @return the trade demand.
	 */
	double determineTradeDemand(Good good) {

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

	double getEVASuitMod() {
		return eVASuitMod;
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

	void setDemandValue(Good good, double newValue) {
		double oldValue = getDemandValue(good);

		double clippedValue = limitMaxMin(newValue, MIN_DEMAND, MAX_DEMAND);
		demandCache.put(good.getID(), clippedValue);

		// if (oldValue != newValue) {
		// 	logger.info(settlement, "Demand updated for " + good.getName() + " old=" + oldValue + " new=" + clippedValue);
		// }
	}

	void setSupplyValue(Good good, double newValue) {
		double clippedValue = limitMaxMin(newValue, MIN_SUPPLY, MAX_SUPPLY);

		supplyCache.put(good.getID(), clippedValue);
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
		double previousDemand = getDemandValue(good);
		return previousDemand / supply;
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
	 * @param s  {@link SimulationConfg}
	 * @param c  {@link MarsClock}
	 * @param m  {@link MissionManager}
	 * @param u  {@link UnitManager}
	 */
	public static void initializeInstances(SimulationConfig sc, MarsClock c, MissionManager m, UnitManager u) {
		unitManager = u;

		Good.initializeInstances(sc, c, m);
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

	/**
	 * The owning Settlement of this manager
	 */
	Settlement getSettlement() {
		return settlement;
	}
}
