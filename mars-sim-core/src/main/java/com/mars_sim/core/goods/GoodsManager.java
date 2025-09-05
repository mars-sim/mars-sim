/*
 * Mars Simulation Project
 * GoodsManager.java
 * @date 2025-08-26
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementConfig.ResourceLimits;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * A manager for computing the values of goods at a settlement.
 */
public class GoodsManager implements Serializable {

	/**
	 * Scheduled event handler for update Goods Values
	 */
	private class GoodsUpdater implements ScheduledEventHandler {
		private static final long serialVersionUID = 1L;
		// For now, update 20 times per day
		// May adjust it according to the time ratio
		private static final int UPDATE_GOODS_PERIOD = (1000/20); 


		@Override
		public String getEventDescription() {
			return "Refresh Goods Values";
		}

		/**
		 * Time to updated Goods
		 * 
		 * @param now Current time not used.
		 */
		@Override
		public int execute(MarsTime now) {
			updatedMetrics();
			return UPDATE_GOODS_PERIOD;
		}	
	}

	/**
	 * Scheduled event handler for refreshing the shopping lists
	 */
	private class TradeListUpdater implements ScheduledEventHandler {
		// Duration that buying & selling list are valid
		private static final int LIST_VALIDITY = 500;
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
		public int execute(MarsTime now) {
			// MUST calculate the buy list before the sell
			calculateBuyList();
			calculateSellList();
			return LIST_VALIDITY;
		}	
	}

	/**
	 * Scheduled event handler for triggering the next review of essential resources
	 */
	private class ResourcesReset implements ScheduledEventHandler {
		// Duration to between reviewing essential resources
		private static final int REVIEW_PERIOD = 2000;
		private static final long serialVersionUID = 1L;

		@Override
		public String getEventDescription() {
			return "Start review period of essential resources";
		}

		/**
		 * Resets the review.
		 * 
		 * @param now Current time not used.
		 */
		@Override
		public int execute(MarsTime now) {
			resetEssentialsReview();
			return REVIEW_PERIOD;
		}	
	}

	/**
	 * Types of commerce factor
	 */
	public enum CommerceType {
		TRANSPORT, TOURISM, CROP, MANUFACTURING, RESEARCH, TRADE, BUILDING
 	}

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	/** Initialized logger. */
	private static final SimLogger logger = SimLogger.getLogger(GoodsManager.class.getName());

	// Number modifiers for outstanding repair and maintenance parts and EVA parts.
	private static final int BASE_REPAIR_PART = 150;
	private static final int BASE_MAINT_PART = 15;
	private static final int BASE_EVA_SUIT = 1;	
	private static final int BASE_BOT = 1;	
	private static final int MAX_SUPPLY = 5_000;
	
	public static final double THROTTLING = .25;
	
	static final double MIN_VP = 0.01;
	private static final int MAX_VP = 10_000;
	public static final double MAX_FINAL_VP = 5_000D;
	
	static final int MAX_DEMAND = 10_000;
	static final double MIN_DEMAND = 0.01;

	private static final double MIN_SUPPLY = 1;
	private static final double PERCENT_110 = 1.1;
	private static final double PERCENT_90 = .9;
	private static final double PERCENT_81 = .81;


	// Fixed weights to apply to updates to commerce factors.
	private static final Map<CommerceType, Double> FACTOR_WEIGHTS = Map.of(CommerceType.RESEARCH, 1.5D);

	private static Map<Integer, ResourceLimits> resLimits;

	/** A standard list of resources to be excluded in buying negotiation. */
	private static Set<Good> unsellableGoods = null;

	private transient Map<MissionType, Deal> deals = new EnumMap<>(MissionType.class);

	// Data members
	private double repairMod = BASE_REPAIR_PART;
	private double maintenanceMod = BASE_MAINT_PART;
	private double eVASuitMod = BASE_EVA_SUIT;
	private double botMod = BASE_BOT;
	
	private Map<CommerceType, Double> factors = new EnumMap<>(CommerceType.class);

	private Map<Integer, Double> goodsValues = new HashMap<>();
	private Map<Integer, Double> tradeCache = new HashMap<>();

	private Map<Integer, Double> demandCache = new HashMap<>();
	private Map<Integer, Double> supplyCache = new HashMap<>();

	private Map<Integer, Integer> deflationIndexMap = new HashMap<>();
	
	/** A standard list of buying resources in buying negotiation. */
	private Map<Good, ShoppingItem> buyList =  Collections.emptyMap();
	private Map<Good, ShoppingItem> sellList = Collections.emptyMap();

	private Set<Integer> reviewedEssentials = new HashSet<>();

	private Settlement settlement;

	private static UnitManager unitManager;
	private static MarketManager marketManager;


	/**
	 * Constructor.
	 *
	 * @param settlement the settlement this manager is for.
	 */
	public GoodsManager(Settlement settlement) {
		this.settlement = settlement;

		int startOfDayOffset = settlement.getTimeOffset();
		
		// Schedule an event to recalculate shopping lists just after start of day
		settlement.getFutureManager().addEvent(startOfDayOffset + 10, new TradeListUpdater());
		
		// Future event to update Goods values; randomise first trigger
		settlement.getFutureManager().addEvent(RandomUtil.getRandomInt(1, 50), new GoodsUpdater());
		
		// Populate the caches
		populateCaches();

		// Schedule reseting the first review cycle during early morning
		settlement.getFutureManager().addEvent(startOfDayOffset + 15, new ResourcesReset());
		
		// Schedule reseting the first review cycle during early morning
		settlement.getFutureManager().addEvent(startOfDayOffset + 15, new ResourcesReset());
	}
    
	/**
     * Gets the flattened demand of a good.
     * 
	 * @param good
	 * @return
	 */
    public double getFlattenDemand(Good good) {
		return good.getFlattenDemand();
	}
    
	/**
     * Gets the projected demand of a good.
     * 
	 * @param good
	 * @return
	 */
    public double getProjectedDemand(Good good) {
		return good.getProjectedDemand();
	}
    
	/**
     * Gets the trade demand of a good.
     * 
	 * @param good
	 * @return
	 */
    public double getTradeDemand(Good good) {
		return good.getTradeDemand();
	}
    
	/**
     * Gets the repair demand of a good.
     * 
	 * @param good
	 * @return
	 */
    public double getRepairDemand(Good good) {
		return good.getRepairDemand();
	}
    
	/**
	 * Populates the cache maps.
	 */
	private void populateCaches() {
		// Preload the good cache
		for(Good good : GoodsUtil.getGoodsList()) {
			int id = good.getID();
			goodsValues.put(id, 1D);
			tradeCache.put(id, 0D);
			deflationIndexMap.put(id, 0);
			demandCache.put(id, good.getDefaultDemandValue());
			supplyCache.put(id, good.getDefaultSupplyValue());
//			marketMap.put(good, new MarketData());
		}
	}

	/**
	 * Gets a list of item to be excluded in a buying negotiation.
	 *
	 * @return
	 */
	static Set<Good> getUnsellableGoods() {
		if (unsellableGoods == null) {
			unsellableGoods = new HashSet<>();
			for (VehicleType type : VehicleType.values()) {
				unsellableGoods.add(GoodsUtil.getVehicleGood(type));
			}
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.REGOLITH_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.ICE_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.CO2_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.CO_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.SAND_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.GREY_WATER_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.BLACK_WATER_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.E_WASTE_ID));
			unsellableGoods.add(GoodsUtil.getGood(ResourceUtil.TOXIC_WASTE_ID));
			// Note: add vehicles to this list ?
		}
		return unsellableGoods;
	}

	/**
	 * Updates the metrics for all good.
	 */
	public void updatedMetrics() {

 		// Update the metrics on all goods 
		for (Good g: GoodsUtil.getGoodsList()) {
			updateOneGood(g);
		}				
	}

	/**
	 * Updates a particular good.
	 * 
	 * @param g
	 */
	public void updateOneGood(Good g) {
		MarketData mv = marketManager.getGlobalMarketBook().get(g);
		
		// Note: calling determineGoodValue would automatically update 
		// local demand/VP with global market data adjustment
		double localValue = determineGoodValue(g);
		
		// Note: No need to update the market demand/VP. 
		// Already done in determineGoodValue()

		double localCost = g.computeAdjustedCost();
		double localPrice = g.calculatePrice(settlement, localValue);

		settlement.fireUnitUpdate(UnitEventType.COST_EVENT, g);
		settlement.fireUnitUpdate(UnitEventType.PRICE_EVENT, g);
		
		mv.updateCost(localCost);
		mv.updatePrice(localPrice);
		
		settlement.fireUnitUpdate(UnitEventType.MARKET_COST_EVENT, g);
		settlement.fireUnitUpdate(UnitEventType.MARKET_PRICE_EVENT, g);
	}
	
	/**
	 * Determines the value of a good. This recalculates the supply & demand.
	 *
	 * @param good     the good to check.
	 * @return value of good.
	 */
	public double determineGoodValue(Good good) {
		if (good != null) {
			// Refresh the Supply and Demand score
			good.refreshSupplyDemandScore(this);

			int id = good.getID();
		
			double totalSupply = supplyCache.get(id);
			double oldDemand = demandCache.get(id);
			
			// Adjust the market demand
			double marketDemand = adjustMarketDemand(good, oldDemand);
			double newDemand = .95 *  oldDemand + .05 * marketDemand;
	
			// Save the demand if it has changed
			if (oldDemand != newDemand) {
				setDemandScore(good, newDemand);
			}

			// Calculate the good value
			double newGoodValue = newDemand / (1 + totalSupply);

			// Check if it surpasses MAX_VP
			if (newGoodValue > MAX_VP) {
				// Update deflationIndexMap for other resources of the same category
				newGoodValue = updateDeflationMap(id, newGoodValue, good.getCategory(), true);
			}
			// Check if it falls below MIN_VP
			else if (newGoodValue < MIN_VP) {
				// Update deflationIndexMap for other resources of the same category
				updateDeflationMap(id, newGoodValue, good.getCategory(), false);
			}

			// Check for inflation and deflation adjustment due to other resources
			newGoodValue = checkDeflation(id, newGoodValue);
			
			// Adjust the market VP
			double marketVP = adjustMarketVP(good, newGoodValue);
			newGoodValue = .95 *  newGoodValue + .05 * marketVP;

			// Save the value point if it has changed
			double oldValue = goodsValues.get(id);
			if (oldValue != newGoodValue) {
				setGoodValue(good, newGoodValue);
			}

			return newGoodValue;
		} else
			logger.severe(settlement, "Good is null.");

		return 0;
	}

	/**
	 * Adjusts the market demand of a good of a settlement.
	 * 
	 * @param good
	 * @param demand
	 * @return the market demand
	 */
	private double adjustMarketDemand(Good good, double demand) {
		// Adjust the market demand
		// Note: Changing data on another thread may not work well
		double marketDemand = getMarketData(good).updateDemand(demand);
		settlement.fireUnitUpdate(UnitEventType.MARKET_DEMAND_EVENT, good);				
		return marketDemand;
	}
	
	/**
	 * Adjusts the market vp of a good of a settlement.
	 * 
	 * @param good
	 * @param vp
	 * @return the market vp
	 */
	private double adjustMarketVP(Good good, double vp) {
		// Adjust the market vp 
		// Note: Changing data on another thread may not work well
		double marketVP = getMarketData(good).updateGoodValue(vp);
		settlement.fireUnitUpdate(UnitEventType.MARKET_VALUE_EVENT, good);				
		return marketVP;
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
		if (!deflationIndexMap.containsKey(id)) {
			return 0;
		}
		if (type == GoodsUtil.getGood(id).getCategory()) {
			// This good is of the same category as the one that cause the
			// inflation/deflation
			int oldIndex = deflationIndexMap.get(id);
			if (exceed) {
				// reduce twice
				deflationIndexMap.put(id, oldIndex + 2);
			}
		}
		else { // This good is of different category
			int oldIndex = deflationIndexMap.get(id);
			if (exceed) {
				// reduce once
				deflationIndexMap.put(id, oldIndex + 1);
			}
		}

		if (exceed)
			return value * PERCENT_81;

		return value;
	}

	/**
	 * Updates a value for a Commerce factor.
	 * 
	 * @param type Commerce type being changed
	 * @param value New value
	 */
	public void setCommerceFactor(CommerceType type, double value) {
		// apply any weighting
		value *= FACTOR_WEIGHTS.getOrDefault(type, 1D);
		factors.put(type, value);
	}

	public double getCommerceFactor(CommerceType type) {
		return factors.getOrDefault(type, 1D);
	}

	/**
	 * Resets all commerce factors back to 1.
	 */
	public void resetCommerceFactors() {
		factors.clear();
	}

	/**
	 * Determines the trade demand for a good at a settlement.
	 *
	 * @param good          the good.
	 * @return the trade demand.
	 */
	double determineTradeDemand(Good good) {

		double selectedTradeValue = 0D;

		for (Settlement tempSettlement : unitManager.getSettlements()) {
			if (tempSettlement != settlement) {
				double baseValue = tempSettlement.getGoodsManager().getDemandScore(good);
				double distance = settlement.getCoordinates().getDistance(
												tempSettlement.getCoordinates());
				double tradeValue = baseValue / (1D + (distance / 1000D));
				if (tradeValue > selectedTradeValue)
					selectedTradeValue = tradeValue;
			}
		}
		tradeCache.put(good.getID(), selectedTradeValue);
		return selectedTradeValue;
	}

	/**
	 * Gets the nth power.
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

	double getBotMod() {
		return botMod;
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

	private static double computeModifier(int baseValue, int level) {
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
		return good.getPrice();
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
	 * Gets the demand score from an resource id.
	 *
	 * @param good's id.
	 * @return demand value
	 */
	public double getDemandScoreWithID(int id) {
		if (demandCache.containsKey(id))
			return demandCache.get(id);
		else
			logger.severe(settlement, "id: " + id + " not valid.");
		return 1;
	}

	/**
	 * Gets the demand score of a good.
	 * 
	 * @param good
	 * @return
	 */
	public double getDemandScore(Good good) {
		return demandCache.get(good.getID());
	}

	/**
	 * Sets the demand score of a good.
	 * 
	 * @param good
	 * @param newScore
	 */
	public void setDemandScore(Good good, double newScore) {
		double clippedValue = MathUtils.between(newScore, MIN_DEMAND, MAX_DEMAND);
		demandCache.put(good.getID(), clippedValue);
		settlement.fireUnitUpdate(UnitEventType.DEMAND_EVENT, good);
	}

	/**
	 * Sets the good value or value point (VP) of a good.
	 * 
	 * @param good
	 * @param newValue
	 */
	public void setGoodValue(Good good, double newValue) {
		double clippedValue = MathUtils.between(newValue, MIN_VP, MAX_VP);
		goodsValues.put(good.getID(), clippedValue);
		settlement.fireUnitUpdate(UnitEventType.VALUE_EVENT, good);
	}
	
	/**
	 * Sets the supply score of a good.
	 * 
	 * @param good
	 * @param newScore
	 */
	void setSupplyScore(Good good, double newScore) {
		setSupplyScore(good.getID(), newScore);
		settlement.fireUnitUpdate(UnitEventType.SUPPLY_EVENT, good);
	}

	/**
	 * Sets the supply score of a good.
	 * 
	 * @param good
	 * @param newScore
	 */
	public void setSupplyScore(int id, double newScore) {
		double clippedValue = MathUtils.between(newScore, MIN_SUPPLY, MAX_SUPPLY);
		supplyCache.put(id, clippedValue);
	}

	/**
	 * Gets the supply score of a good.
	 * 
	 * @param good
	 * @return
	 */
	public double getSupplyScore(Good good) {
		return getSupplyScore(good.getID());
	}
	
	/**
	 * Gets the supply score of a good.
	 * 
	 * @param id
	 * @return
	 */
	public double getSupplyScore(int id) {
		return supplyCache.get(id);
	}

	/**
	 * Reset the reviews essential resources.
	 */
	public void resetEssentialsReview() {
		reviewedEssentials.clear();
	}

	/**
	 * Gets the number of resources that need reviewing.
	 * 
	 * @return
	 */
	public int getResourceReviewDue() {
		return resLimits.size() - reviewedEssentials.size();
	}
	
	private Set<Integer> getResourceForReview() {
		Set<Integer> unreviewed = new HashSet<>(resLimits.keySet());
		unreviewed.removeAll(reviewedEssentials);

		return unreviewed;
	}

	/**
	 * Reserves an essential resource for a review.
	 * 
	 * @return Selected resource
	 */
    public int reserveResourceReview() {
		var unreviewed = getResourceForReview();

		// Everything has been reviewed
		if (unreviewed.isEmpty()) {
			return -1;
		}

		// Pick one an random and add to the reviewed set
		int selected = RandomUtil.getARandSet(unreviewed);
		reviewedEssentials.add(selected);
		return selected;
    }

	/**
	 * Checks if the demand for a life support resource is met.
	 *
	 * @param resourceID
	 * @return the new demand 
	 */
	public double moderateLifeResourceDemand(int resourceID) {
		double lacking = 0;

		var limits = resLimits.get(resourceID);
		if (limits == null) {
			throw new IllegalArgumentException("Resource is not essential " + resourceID);
		}
		int reservePerPop = limits.reserve();
		int optimalPerPop = limits.optimal();
		int pop = settlement.getNumCitizens();
		
		double optimal = optimalPerPop * pop;
		double reserve = reservePerPop * pop;
		double demand = getDemandScoreWithID(resourceID);

		// Compare the available amount of oxygen
//		double supply = getSupplyScore(resourceID);

		double stored = settlement.getAllAmountResourceStored(resourceID);
	
		if (stored > optimal) {
			// Thus no need of demand adjustment
			return 0;
		}
			
		lacking = MathUtils.between(optimal - reserve - stored, 0, optimal - reserve);

		// Note : Make sure stored is not zero by adding 1 so that delta is not infinite	
		
		// Multiply by THROTTLING to limit the speed of increase to avoid an abrupt rise in demand
		double delta = (lacking / (1 + stored) - 1); //* THROTTLING;
 
		
		if (delta > 0) {
			String gasName = ResourceUtil.findAmountResourceName(resourceID);
			logger.info(settlement, 10_000L,
					gasName + " - " 
					+ "Injecting Demand: " + Math.round(demand * 100.0)/100.0 
					+ " -> " + Math.round((demand + delta) * 100.0)/100.0 
					+ "  Optimal: " + Math.round(optimal * 100.0)/100.0 
//					+ "  Supply: " + Math.round(supply * 100.0)/100.0 
					+ "  Stored: " + Math.round(stored * 100.0)/100.0
					+ "  reserve: " + Math.round(reservePerPop * 100.0)/100.0
					+ "  lacking: " + Math.round(lacking * 100.0)/100.0
					+ ".");

			return delta * demand;
		}
		
		return 0;
	}

	/**
	 * Injects the resource demand.
	 * 
	 * @param resourceID
	 * @param newDemand
	 */
	public void injectResourceDemand(int resourceID, double newDemand) {
		// Inject a sudden change of demand
		setDemandScore(GoodsUtil.getGood(resourceID), newDemand);
	}

	/**
	 * Gets the market position for a good.
	 * 
	 * @param good
	 * @return
	 */
	public MarketData getMarketData(Good good) {
		return marketManager.getGlobalMarketBook().get(good);
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
	 * Exclude any Good that is on the Buy list.
	 */
	private void calculateSellList() {
		
		// This logic is a draft and need more refinement
		Map<Good, ShoppingItem> newSell = new HashMap<>();
		Set<Good> excluded = new HashSet<>(GoodsManager.getUnsellableGoods());
		excluded.addAll(buyList.keySet());   // Exclude goods that are already being bought

		for(Entry<Integer, Double> item : supplyCache.entrySet()) {
			Good good = GoodsUtil.getGood(item.getKey());

			if (excluded.contains(good)) {
				continue;
			}

			// Sell goods where there is a good supply value
			double buyPrice = getPrice(good);
			if (buyPrice > 0D) {
				// Look up sell 10%
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
		Map<Good, ShoppingItem> newBuy = new HashMap<>();
		Set<Good> excluded = GoodsManager.getUnsellableGoods();
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
	
	/**
	 * Reloads instances after loading from a saved sim.
	 *
	 * @param s  {@link SimulationConfg}
	 * @param m  {@link MissionManager}
	 * @param u  {@link UnitManager}
	 * @param mm  {@link MarketManager}
	 */
	public static void initializeInstances(SimulationConfig sc, MissionManager m, UnitManager u, MarketManager mm) {
		unitManager = u;
		Good.initializeInstances(sc, m, u);
		CommerceUtil.initializeInstances(m, u);
		marketManager = mm;
		resLimits = sc.getSettlementConfiguration().getEssentialResources();
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {

		settlement = null;
		goodsValues = null;
		demandCache = null;
		tradeCache = null;

		deflationIndexMap = null;

		supplyCache = null;

		buyList = null;
		sellList = null;
	}

}
