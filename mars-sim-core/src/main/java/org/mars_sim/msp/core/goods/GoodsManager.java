/*
 * Mars Simulation Project
 * GoodsManager.java
 * @date 2022-07-30
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.ScheduledEventHandler;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A manager for computing the values of goods at a settlement.
 */
public class GoodsManager implements Serializable {

	private class FutureHandler implements ScheduledEventHandler {

		private static final long serialVersionUID = 1L;

		@Override
		public String getEventDescription() {
			return "Refresh Buy/Sell list";
		}

		/**
		 * Time to updated lists.
		 * 
		 * @param now Current time not used.
		 */
		@Override
		public int execute(MarsClock now) {
			calculateBuyList();
			calculateSellList();
			return LIST_VALIDITY;
		}
		
	}

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	/** Initialized logger. */
	private static SimLogger logger = SimLogger.getLogger(GoodsManager.class.getName());

	// Number modifiers for outstanding repair and maintenance parts and EVA parts.
	private static final int BASE_REPAIR_PART = 150;
	private static final int BASE_MAINT_PART = 15;
	private static final int BASE_EVA_SUIT = 1;

	// Duration that buying & selling list are valid
    private static final int LIST_VALIDITY = 500;

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

	private boolean initialized = false;
	// Add modifiers due to Settlement Development Objectives
	private double cropFarmFactor = 1;
	private double manufactureFactor = 1;
	private double researchFactor = 1;
	private double transportFactor = 1;
	private double tradeFactor = 1;
	private double tourismFactor = 1;
	private double buildersFactor = 1;

	private Map<Integer, Double> goodsValues = new HashMap<>();
	private Map<Integer, Double> tradeCache = new HashMap<>();

	private Map<Integer, Double> demandCache = new HashMap<>();
	private Map<Integer, Double> supplyCache = new HashMap<>();

	private Map<Integer, Integer> deflationIndexMap = new HashMap<>();

	/** A standard list of resources to be excluded in buying negotiation. */
	private static List<Good> exclusionBuyList = null;
	/** A standard list of buying resources in buying negotiation. */
	private transient Map<Good, ShoppingItem> buyList =  Collections.emptyMap();
	private transient Map<Good, ShoppingItem> sellList = Collections.emptyMap();

	private Settlement settlement;

	private transient Map<MissionType, Deal> deals = new EnumMap<>(MissionType.class);

	private static UnitManager unitManager;

	/**
	 * Constructor.
	 *
	 * @param settlement the settlement this manager is for.
	 * @param sunRiseOffSet Offset to sunrise
	 */
	public GoodsManager(Settlement settlement, int sunRiseOffSet) {
		this.settlement = settlement;
		// Schedule an event to recalculate shopping lists just after sunrise
		settlement.getFutureManager().addEvent(sunRiseOffSet + 10, new FutureHandler());
		
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
	 * Gets a list of item to be excluded in a buying negotiation.
	 *
	 * @return
	 */
	static List<Good> getExclusionBuyList() {
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
	 * Updates the good values for all good.
	 */
	public void updateGoodValues() {
 		// Update the goods value gradually with the use of buffers
		for(Good g: GoodsUtil.getGoodsList()) {
			determineGoodValue(g);
			
			if (initialized) {
				g.adjustInterMarketGoodValue();
			}
		}
				
		initialized = true;
	}

	
	/**
	 * Determines the value of a good. This recalculates the supply & demand.
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
			double value = totalDemand / (1 + totalSupply);

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
			// Adjust the value according to the inter-market value
			double adjustment = adjustMarketValue(good, value) / 20.0;
			if (value + adjustment > 0)
				value += adjustment;

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
	 * Adjusts the inter-market value of a good based on the local value of a settlement.
	 * 
	 * @param good
	 * @param value
	 * @return the market adjustment
	 */
	private double adjustMarketValue(Good good, double value) {
		// Gets the inter-market value among the settlements
		double currentMarket = good.getInterMarketGoodValue();
		double futureMarket = 0;

		if (currentMarket == -1) {
			// At the startup of the sim
			futureMarket = value;
				
			if (futureMarket > MAX_FINAL_VP)
				futureMarket = MAX_FINAL_VP;			
			else if (futureMarket < MIN_VP)
				futureMarket = MIN_VP;
			
			good.setInterMarketGoodValue(futureMarket);
			
			return 0;
		}

		else {
			// Let the inter-market value affects the value of this good
			// at this settlement 
			futureMarket = .9 * currentMarket + .1 * value;

//			double newValue = 0;
//
//			if (currentMarket > value)
//				newValue = (currentMarket - value) / 10;
//			else if (currentMarket < value)
//				newValue = (value - currentMarket) / 10;
//
//			futureMarket = futureMarket + newValue;

			if (futureMarket > MAX_FINAL_VP)
				futureMarket = MAX_FINAL_VP;
			else if (futureMarket < MIN_VP)
				futureMarket = MIN_VP;
			
			good.setInterMarketGoodValue(futureMarket);
			
			return futureMarket - currentMarket;
		}
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
		cropFarmFactor = value * CROPFARM_BASE;
	}

	public void setManufacturingFactor(double value) {
		manufactureFactor = value * MANU_BASE;
	}

	public void setTransportationFactor(double value) {
		transportFactor = value * TRANSPORT_BASE;
	}

	public void setResearchFactor(double value) {
		researchFactor = value * RESEARCH_BASE;
	}

	public void setTradeFactor(double value) {
		tradeFactor = value * TRADE_BASE;
	}

	public void setTourismFactor(double value) {
		tourismFactor = value * TOURISM_BASE;
	}

	public void setBuildersFactor(double value) {
		buildersFactor = value * BUILDERS_BASE;
	}

	public double getBuildersFactor() {
		return buildersFactor;
	}

	public double getCropFarmFactor() {
		return cropFarmFactor;
	}

	public double getManufacturingFactor() {
		return manufactureFactor;
	}

	public double getTransportationFactor() {
		return transportFactor;
	}

	public double getResearchFactor() {
		return researchFactor;
	}

	public double getTradeFactor() {
		return tradeFactor;
	}

	public double getTourismFactor() {
		return tourismFactor;
	}

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
				double baseValue = tempSettlement.getGoodsManager().getGoodValuePoint(good);
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

	/**
	 * Gets the current list of items on this Settlement wants to buy.
	 * 
	 * @return Mapping from Good to the item
	 */
	public Map<Good, ShoppingItem> getBuyList() {
		return buyList;
	}

	/**
	 * Gets the current list of items on this Settlement is willing to sell.
	 * 
	 * @return Mapping from Good to the item
	 */
	public Map<Good, ShoppingItem> getSellList() {
        return sellList;
    }

	/**
	 * Gets the price per item for a good.
	 *
	 * @param id the good id
	 * @return
	 */
	public double getPricePerItem(int id) {
		return getPrice(GoodsUtil.getGood(id));
	}

	/**
	 * Gets the price for a good.
	 *
	 * @param good the good
	 * @return
	 */
	public double getPrice(Good good) {
		double value = getGoodValuePoint(good);

		return good.getPrice(settlement, value);
	}
	
	/**
	 * Gets the value point of a good.
	 *
	 * @param good the good to check.
	 * @return value (VP)
	 */
	public double getGoodValuePoint(Good good) {
		return getGoodValuePoint(good.getID());
	}


	/**
	 * Gets the value point of a good.
	 *
	 * @param id the good id to check.
	 * @return value (VP)
	 */
	public double getGoodValuePoint(int id) {
		if (goodsValues.containsKey(id))
			return goodsValues.get(id);
		else
			logger.severe(settlement, " - Good Value of " + id + " not valid.");
		return 0;
	}

	/**
	 * Gets the demand value from an resource id.
	 *
	 * @param good's id.
	 * @return demand value
	 */
	public double getDemandValueWithID(int id) {
		if (demandCache.containsKey(id))
			return demandCache.get(id);
		else
			logger.severe(settlement, "id: " + id + " not valid.");
		return 1;
	}

	/**
	 * Gets the demand value of a good.
	 * 
	 * @param good
	 * @return
	 */
	public double getDemandValue(Good good) {
		return demandCache.get(good.getID());
	}

	/**
	 * Sets the demand value of a good.
	 * 
	 * @param good
	 * @param newValue
	 */
	void setDemandValue(Good good, double newValue) {
		double clippedValue = limitMaxMin(newValue, MIN_DEMAND, MAX_DEMAND);
		demandCache.put(good.getID(), clippedValue);
	}

	/**
	 * Sets the supply value of a good.
	 * 
	 * @param good
	 * @param newValue
	 */
	void setSupplyValue(Good good, double newValue) {
		double clippedValue = limitMaxMin(newValue, MIN_SUPPLY, MAX_SUPPLY);
		supplyCache.put(good.getID(), clippedValue);
	}

	/**
	 * Gets the supply value of a good.
	 * 
	 * @param good
	 * @return
	 */
	public double getSupplyValue(Good good) {
		return getSupplyValue(good.getID());
	}
	
	/**
	 * Gets the supply value of a good.
	 * 
	 * @param id
	 * @return
	 */
	public double getSupplyValue(int id) {
		return supplyCache.get(id);
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
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param s  {@link SimulationConfg}
	 * @param m  {@link MissionManager}
	 * @param u  {@link UnitManager}
	 */
	public static void initializeInstances(SimulationConfig sc, MissionManager m, UnitManager u) {
		unitManager = u;
		Good.initializeInstances(sc, m);
		CommerceUtil.initializeInstances(m, u);
	}
	
	/**
	 * Prepares object for garbage collection.
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

		buyList = null;
		sellList = null;
	}

	/**
	 * Returns the owning Settlement of this manager.
	 */
	Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Finds the best trading deal for the parent Settlement using a certain Vehicle 
	 * for a Commerce mission.
	 * 
	 * @param commerce Type of Commerce
	 * @param delivery Vehicle doing the Delivery
	 */
	public Deal getBestDeal(MissionType commerce, Vehicle delivery) {
		Deal deal = deals.get(commerce);

		if (deal != null) {
			return deal;
		}

		// Recalculate
		Deal best = CommerceUtil.getBestDeal(settlement, commerce, delivery);
		if (best != null) {
			deals.put(commerce, best);
		}
		return best;
	}

	public void clearDeal(MissionType commerce) {
		deals.remove(commerce);
	}
	
	/**
	 * Calculates the sell list.
	 */
	private void calculateSellList() {
		
		// This logic is a draft and need more refinement
		Map<Good,ShoppingItem> newSell = new HashMap<>();
		List<Good> excluded = GoodsManager.getExclusionBuyList();
		for(Entry<Integer, Double> item : supplyCache.entrySet()) {
			Good good = GoodsUtil.getGood(item.getKey());

			if (excluded.contains(good)) {
				continue;
			}

			// Sell goods where there is a good supply value
			double buyPrice = getPrice(good);
			if (buyPrice > 0D) {
				/// Look up sell 10%
				int quantity = (int)(good.getNumberForSettlement(settlement) * 0.1D);

				// Take Goods where I have ample in store
				if (quantity > 0) {
					newSell.put(good, new ShoppingItem(quantity, buyPrice));
				}
			}
		}

		sellList = Collections.unmodifiableMap(newSell);

		// Any deal are now invalid
		deals.clear();
	}

	/**
	 * Calculates the current buying list for this Settlement.
	 */
	private void calculateBuyList() {

		// This logic is a draft and need more refinement
		Map<Good,ShoppingItem> newBuy = new HashMap<>();
		List<Good> excluded = GoodsManager.getExclusionBuyList();
		for(Entry<Integer, Double> item : demandCache.entrySet()) {
			Good good = GoodsUtil.getGood(item.getKey());
			if (excluded.contains(good)) {
				continue;
			}

			// Take Goods in demand more than supply
			if (item.getValue() > supplyCache.get(good.getID())) {
				double buyPrice = getPrice(good) * 1.1D;
				int quantity = (int)(good.getNumberForSettlement(settlement) * 0.1D);
				if (quantity == 0) {
					// Don't have any so buy some
					quantity = Math.max((int)(50D / buyPrice), 10);
				}
				newBuy.put(good, new ShoppingItem(quantity, buyPrice));
			}
		}

		buyList = Collections.unmodifiableMap(newBuy);

		// Any deal are now invalid
		deals.clear();
	}
	
	/**
	 * Custom read to re-init deals variable.
	 */
	private void readObject(ObjectInputStream in)
    	throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		deals = new EnumMap<>(MissionType.class);
		buyList = Collections.emptyMap();
		sellList = Collections.emptyMap();
	}
}
