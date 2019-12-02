/**
 * Mars Simulation Project
 * TradeUtil.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodType;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Utility class for static trade methods. TODO externalize strings
 */
public final class TradeUtil {

	/** default logger. */
	private static Logger logger = Logger.getLogger(TradeUtil.class.getName());

	/**
	 * Credit limit under which a seller is willing to sell goods to a buyer. Buyer
	 * must pay off credit to under limit to continue buying.
	 */
	public static final double SELL_CREDIT_LIMIT = 10_000_000D;

	/** Estimated mission parts mass. */
	private static final double MISSION_BASE_MASS = 2_000D;

	/** Minimum mass (kg) of life support resources to leave at settlement. */
	private static final int MIN_LIFE_SUPPORT_RESOURCES = 100;

	/** Minimum number of repair parts to leave at settlement. */
	private static final int MIN_REPAIR_PARTS = 20;

	/** Minimum number of repair parts to leave at settlement. */
	private static final int MIN_NUM_EQUIPMENT = 10;
	
	/** Performance cache for equipment goods. */
	private final static Map<Class<? extends Equipment>, Equipment> equipmentGoodCache = new HashMap<Class<? extends Equipment>, Equipment>(
			5);

	/** Cache for the best trade settlement. */
	public static Settlement bestTradeSettlementCache = null;

	/** Cache for container types. */
	private final static Map<Class<? extends Equipment>, Equipment> containerTypeCache = new HashMap<Class<? extends Equipment>, Equipment>(
			3);

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;

	private static Simulation sim = Simulation.instance();
	private static MissionManager missionManager = sim.getMissionManager();
	private static CreditManager creditManager = sim.getCreditManager();
	private static UnitManager unitManager = sim.getUnitManager();

			
	/**
	 * Private constructor for utility class.
	 */
	private TradeUtil() {
	};

	/**
	 * Gets the best trade value for a given settlement.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param rover              the rover to carry the trade.
	 * @return the best value (value points) for trade.
	 * @throws Exception if error while getting best trade profit.
	 */
	public static double getBestTradeProfit(Settlement startingSettlement, Rover rover) {
		double bestProfit = 0D;
		Settlement bestSettlement = null;

		for (Settlement tradingSettlement : unitManager.getSettlements()) {
			if (tradingSettlement != startingSettlement && tradingSettlement.isMissionDisable(Trade.DEFAULT_DESCRIPTION)) {

				boolean hasCurrentTradeMission = hasCurrentTradeMission(startingSettlement, tradingSettlement);

				double settlementRange = Coordinates.computeDistance(tradingSettlement.getCoordinates(), startingSettlement.getCoordinates());
				boolean withinRange = (settlementRange <= (rover.getRange(Trade.missionType) * .8D));

				if (!hasCurrentTradeMission && withinRange) {
					// double startTime = System.currentTimeMillis();

					double profit = getEstimatedTradeProfit(startingSettlement, rover, tradingSettlement);
					// double endTime = System.currentTimeMillis();
//					 logger.finest("getEstimatedTradeProfit " + (endTime - startTime));
					if (profit > bestProfit) {
						bestProfit = profit;
						bestSettlement = tradingSettlement;
					}
				}
			}
		}

		// Set settlement cache.
		bestTradeSettlementCache = bestSettlement;

		return bestProfit;
	}

	/**
	 * Checks if there is currently a trade mission between two settlements.
	 * 
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @return true if current trade mission between settlements.
	 */
	private static boolean hasCurrentTradeMission(Settlement settlement1, Settlement settlement2) {
		boolean result = false;

		// MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof Trade) {
				Trade tradeMission = (Trade) mission;
				Settlement startingSettlement = tradeMission.getStartingSettlement();
				Settlement tradingSettlement = tradeMission.getTradingSettlement();
				if (startingSettlement.equals(settlement1) && tradingSettlement.equals(settlement2)) {
					result = true;
					break;
				} else if (startingSettlement.equals(settlement2) && tradingSettlement.equals(settlement1)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the estimated trade profit from one settlement to another.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param rover              the rover to carry the trade goods.
	 * @param tradingSettlement  the settlement to trade to.
	 * @return the trade profit (value points)
	 * @throws Exception if error getting the estimated trade profit.
	 */
	private static double getEstimatedTradeProfit(Settlement startingSettlement, Rover rover,
			Settlement tradingSettlement) {

		// Determine estimated trade revenue.
		double revenue = getEstimatedTradeRevenue(startingSettlement, rover, tradingSettlement);

		// Determine estimated mission cost.
		double distance = startingSettlement.getCoordinates().getDistance(tradingSettlement.getCoordinates()) * 2D;
		double cost = getEstimatedMissionCost(startingSettlement, rover, distance);

		return revenue - cost;
	}

	/**
	 * Gets the estimated trade revenue from one settlement to another.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param rover              the rover to carry the trade goods.
	 * @param tradingSettlement  the settlement to trade to.
	 * @return the trade revenue (value points).
	 * @throws Exception if error getting the estimated trade revenue.
	 */
	private static double getEstimatedTradeRevenue(Settlement startingSettlement, Rover rover,
			Settlement tradingSettlement) {

		// Get credit between starting settlement and trading settlement.
		double credit = creditManager.getCredit(startingSettlement, tradingSettlement);

		Map<Good, Integer> buyLoad = null;
		if (credit > (TradeUtil.SELL_CREDIT_LIMIT * -1D)) {
			// Determine desired buy load,
			buyLoad = TradeUtil.getDesiredBuyLoad(startingSettlement, rover, tradingSettlement);
		} else {
			// Cannot buy from settlement due to credit limit.
			buyLoad = new HashMap<Good, Integer>(0);
		}

		Map<Good, Integer> sellLoad = null;
		if (credit < TradeUtil.SELL_CREDIT_LIMIT) {
			// Determine sell load.
			sellLoad = TradeUtil.determineBestSellLoad(startingSettlement, rover, tradingSettlement);
		} else {
			// Will not sell to settlement due to credit limit.
			sellLoad = new HashMap<Good, Integer>(0);
		}

		double sellingValueHome = TradeUtil.determineLoadValue(sellLoad, startingSettlement, false);
		double sellingValueRemote = TradeUtil.determineLoadValue(sellLoad, tradingSettlement, true);
		double sellingProfit = sellingValueRemote - sellingValueHome;

		double buyingValueHome = TradeUtil.determineLoadValue(buyLoad, startingSettlement, true);
		double buyingValueRemote = TradeUtil.determineLoadValue(buyLoad, tradingSettlement, false);
		double buyingProfit = buyingValueHome - buyingValueRemote;

		double totalProfit = sellingProfit + buyingProfit;

		return totalProfit;
	}

	/**
	 * Gets the desired buy load from a trading settlement.
	 * 
	 * @param startingSettlement the settlement that is buying.
	 * @param rover              the rover used for trade.
	 * @param tradingSettlement  the settlement to buy from.
	 * @return the desired buy load.
	 * @throws Exception if error determining the buy load.
	 */
	public static Map<Good, Integer> getDesiredBuyLoad(Settlement startingSettlement, Rover rover,
			Settlement tradingSettlement) {

		// Determine best buy load.
		Map<Good, Integer> buyLoad = determineLoad(startingSettlement, tradingSettlement, rover,
				Double.POSITIVE_INFINITY);

		return buyLoad;
	}

	/**
	 * Determines the best sell load from a settlement to another.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param rover              the rover to carry the goods.
	 * @param tradingSettlement  the settlement to trade to.
	 * @return a map of goods and numbers in the load.
	 * @throws Exception if error determining best sell load.
	 */
	static Map<Good, Integer> determineBestSellLoad(Settlement startingSettlement, Rover rover,
			Settlement tradingSettlement) {

		// Determine best sell load.
		Map<Good, Integer> sellLoad = determineLoad(tradingSettlement, startingSettlement, rover,
				Double.POSITIVE_INFINITY);

		return sellLoad;
	}

	/**
	 * Determines the load between a buying settlement and a selling settlement.
	 * 
	 * @param buyingSettlement  the settlement buying the goods.
	 * @param sellingSettlement the settlement selling the goods.
	 * @param rover             the rover to carry the goods.
	 * @param maxBuyValue       the maximum value the selling settlement will
	 *                          permit.
	 * @return map of goods and their number.
	 * @throws Exception if error determining the load.
	 */
	public static Map<Good, Integer> determineLoad(Settlement buyingSettlement, Settlement sellingSettlement,
			Rover rover, double maxBuyValue) {

		Map<Good, Integer> tradeList = new HashMap<Good, Integer>();
		boolean hasRover = false;
		GoodsManager buyerGoodsManager = buyingSettlement.getGoodsManager();
		buyerGoodsManager.prepareForLoadCalculation();
		GoodsManager sellerGoodsManager = sellingSettlement.getGoodsManager();
		sellerGoodsManager.prepareForLoadCalculation();

		double massCapacity = rover.getInventory().getGeneralCapacity();

		// Subtract mission base mass (estimated).
		double missionPartsMass = MISSION_BASE_MASS;
		if (massCapacity < missionPartsMass)
			missionPartsMass = massCapacity;
		massCapacity -= missionPartsMass;

		// Determine repair parts for trip.
		Set<Integer> repairParts = rover.getMalfunctionManager().getRepairPartProbabilities().keySet();

		// Determine the load.
		boolean done = false;
		double buyerLoadValue = 0D;
		Good previousGood = null;
		Set<Good> nonTradeGoods = Collections.emptySet();
		while (!done) {
			double remainingBuyValue = maxBuyValue - buyerLoadValue;
			Good good = findBestTradeGood(sellingSettlement, buyingSettlement, tradeList, nonTradeGoods, massCapacity,
					hasRover, rover, previousGood, false, repairParts, remainingBuyValue);
			if (good != null) {
				try {
					boolean isAmountResource = good.getCategory() == GoodType.AMOUNT_RESOURCE;
					boolean isItemResource = good.getCategory() == GoodType.ITEM_RESOURCE;
					AmountResource resource = null;
					
					// Add resource container if needed.
					if (isAmountResource) {
						resource = ResourceUtil.findAmountResource(good.getID());
						Equipment container = getAvailableContainerForResource(resource,
								sellingSettlement, tradeList);
						if (container != null) {
							Good containerGood = GoodsUtil.getEquipmentGood(container.getClass());
							massCapacity -= container.getBaseMass();
							int containerNum = 0;
							if (tradeList.containsKey(containerGood))
								containerNum = tradeList.get(containerGood);
							double containerSupply = buyerGoodsManager.getNumberOfGoodForSettlement(containerGood);
							double totalContainerNum = containerNum + containerSupply;
							buyerLoadValue += buyerGoodsManager.getGoodValuePerItem(containerGood, totalContainerNum);
							tradeList.put(containerGood, (containerNum + 1));
						} else
							logger.warning("container for " + resource.getName() + " not available.");
					}

					int itemResourceNum = 0;
					if (isItemResource) {
						itemResourceNum = getNumItemResourcesToTrade(good, sellingSettlement, buyingSettlement,
								tradeList, massCapacity, remainingBuyValue);
					}

					// Add good.
					if (good.getCategory() == GoodType.VEHICLE)
						hasRover = true;
					else {
						int number = 1;
						if (isAmountResource)
							number = (int) getResourceTradeAmount(ResourceUtil.findAmountResource(good.getID()));
						else if (isItemResource)
							number = itemResourceNum;
						massCapacity -= (GoodsUtil.getGoodMassPerItem(good) * number);
					}
					
					int currentNum = 0;
					if (tradeList.containsKey(good))
						currentNum = tradeList.get(good);
					double supply = buyerGoodsManager.getNumberOfGoodForSettlement(good);
					double goodNum = 1D;
					
					if (isAmountResource)
						goodNum = getResourceTradeAmount(resource);
					if (isItemResource)
						goodNum = itemResourceNum;
					
					double buyGoodValue = buyerGoodsManager.getGoodValuePerItem(good, (supply + currentNum + goodNum));
					
					if (isAmountResource) {
						double tradeAmount = getResourceTradeAmount(resource);
						buyGoodValue *= tradeAmount;
					}
					if (isItemResource) {
						buyGoodValue *= itemResourceNum;
					}
					buyerLoadValue += buyGoodValue;
					int newNumber = currentNum + (int) goodNum;
					tradeList.put(good, newNumber);
				} catch (Exception e) {
					done = true;
				}
			} else
				done = true;

			previousGood = good;
		}

		return tradeList;
	}

	/**
	 * Determines the value of a load to a settlement.
	 * 
	 * @param load       a map of the goods and their number.
	 * @param settlement the settlement valuing the load.
	 * @param buy        true if settlement is buying the load, false if selling.
	 * @return value of the load (value points).
	 * @throws Exception if error determining the load value.
	 */
	public static double determineLoadValue(Map<Good, Integer> load, Settlement settlement, boolean buy) {
		double result = 0D;

		GoodsManager manager = settlement.getGoodsManager();

		Iterator<Good> i = load.keySet().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			int goodNumber = load.get(good);
			double supply = manager.getNumberOfGoodForSettlement(good);
			double multiplier = 1D;
			if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {	
				double tradeAmount = getResourceTradeAmount(ResourceUtil.findAmountResource(good.getID()));
				goodNumber /= (int) tradeAmount;
				multiplier = tradeAmount;
			}
			else {	
				multiplier = 1D;
			}
			
			for (int x = 0; x < goodNumber; x++) {

				double supplyAmount = 0D;
				if (buy)
					supplyAmount = supply + x;
				else {
					supplyAmount = supply - x;
					if (supplyAmount < 0D)
						supplyAmount = 0D;
				}

				double value = (manager.getGoodValuePerItem(good, supplyAmount) * multiplier);

				result += value;
			}
		}

		return result;
	}

	/**
	 * Finds the best trade good for a trade.
	 * 
	 * @param sellingSettlement the settlement selling the good.
	 * @param buyingSettlement  the settlement buying the good.
	 * @param tradedGoods       the map of goods traded so far.
	 * @param nonTradeGoods     the set of goods not to trade.
	 * @param remainingCapacity remaining general capacity (kg) in vehicle
	 *                          inventory.
	 * @param hasVehicle        true if a vehicle is in the trade goods.
	 * @param missionRover      the rover carrying the goods.
	 * @param previousGood      the previous trade good used in the trade.
	 * @param allowNegValue     allow negative value goods.
	 * @param repairParts       set of repair parts possibly needed for the trip.
	 * @param maxBuyValue       the maximum value the item can be.
	 * @return best good to trade or null if none found.
	 * @throws Exception if error determining best trade good.
	 */
	private static Good findBestTradeGood(Settlement sellingSettlement, Settlement buyingSettlement,
			Map<Good, Integer> tradedGoods, Set<Good> nonTradeGoods, double remainingCapacity, boolean hasVehicle,
			Rover missionRover, Good previousGood, boolean allowNegValue, Set<Integer> repairParts,
			double maxBuyValue) {

		Good result = null;

		// Check previous good first.
		if (previousGood != null) {
			double previousGoodValue = getTradeValue(previousGood, sellingSettlement, buyingSettlement, tradedGoods,
					remainingCapacity, hasVehicle, missionRover, allowNegValue, repairParts);
			if ((previousGoodValue > 0D) && (previousGoodValue < maxBuyValue))
				result = previousGood;
		}

		// Check all goods.
		if (result == null) {
			double bestValue = 0D;
			if (allowNegValue)
				bestValue = Double.NEGATIVE_INFINITY;
			// Iterator<Good> i = GoodsUtil.getGoodsList().iterator();
			// while (i.hasNext()) {
			for (Good good : GoodsUtil.getGoodsList()) {// = i.next();
				if (!nonTradeGoods.contains(good)) {
					double tradeValue = getTradeValue(good, sellingSettlement, buyingSettlement, tradedGoods,
							remainingCapacity, hasVehicle, missionRover, allowNegValue, repairParts);
					if ((tradeValue > bestValue) && (tradeValue < maxBuyValue)) {
						result = good;
						bestValue = tradeValue;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the number of an item resource good that should be traded.
	 * 
	 * @param itemResourceGood  the item resource good.
	 * @param sellingSettlement the settlement selling the good.
	 * @param buyingSettlement  the settlement buying the good.
	 * @param tradeList         the map of goods traded so far.
	 * @param remainingCapacity remaining general capacity (kg) in vehicle
	 *                          inventory.
	 * @param maxBuyValue       the maximum buy value.
	 * @return number of goods to trade.
	 * @throws Exception if error determining number of goods.
	 */
	private static int getNumItemResourcesToTrade(Good itemResourceGood, Settlement sellingSettlement,
			Settlement buyingSettlement, Map<Good, Integer> tradeList, double remainingCapacity, double maxBuyValue) {

		int result = 0;

		Part item = ItemResourceUtil.findItemResource(itemResourceGood.getID());

		int sellingInventory = sellingSettlement.getInventory().getItemResourceNum(item);
		int buyingInventory = buyingSettlement.getInventory().getItemResourceNum(item);

		int numberTraded = 0;
		if (tradeList.containsKey(itemResourceGood))
			numberTraded = tradeList.get(itemResourceGood);

		int roverLimit = (int) (remainingCapacity / item.getMassPerItem());

		int totalTraded = numberTraded;
		double totalBuyingValue = 0D;
		boolean limitReached = false;
		while (!limitReached) {

			double sellingSupplyAmount = sellingInventory - totalTraded - 1;
			double sellingValue = sellingSettlement.getGoodsManager().getGoodValuePerItem(itemResourceGood,
					sellingSupplyAmount);
			double buyingSupplyAmount = buyingInventory + totalTraded + 1;
			double buyingValue = buyingSettlement.getGoodsManager().getGoodValuePerItem(itemResourceGood,
					buyingSupplyAmount);

			if (buyingValue <= sellingValue)
				limitReached = true;
			if (totalTraded + 1 > sellingInventory)
				limitReached = true;
			if (totalTraded + 1 > roverLimit)
				limitReached = true;
			if ((totalBuyingValue + buyingValue) >= maxBuyValue)
				limitReached = true;

			if (!limitReached) {
				result++;
				totalTraded = numberTraded + result;
				totalBuyingValue += buyingValue;
			}
		}

		// Result shouldn't be zero, but just in case it is.
		if (result == 0)
			result = 1;
		return result;
	}

	/**
	 * Gets the trade value of a good.
	 * 
	 * @param good              the good
	 * @param sellingSettlement the settlement selling the good.
	 * @param buyingSettlement  the settlement buying the good.
	 * @param tradedGoods       the map of goods traded so far.
	 * @param remainingCapacity remaining general capacity (kg) in vehicle
	 *                          inventory.
	 * @param hasVehicle        true if a vehicle is in the trade goods.
	 * @param missionRover      the rover carrying the goods.
	 * @param allowNegValue     allow negative value goods.
	 * @param repairParts       set of repair parts possibly needed for the trip.
	 * @return trade value of good.
	 * @throws Exception if error determining trade value.
	 */
	private static double getTradeValue(Good good, Settlement sellingSettlement, Settlement buyingSettlement,
			Map<Good, Integer> tradedGoods, double remainingCapacity, boolean hasVehicle, Rover missionRover,
			boolean allowNegValue, Set<Integer> repairParts) {

		double result = Double.NEGATIVE_INFINITY;
		AmountResource resource = null;
		double amountTraded = 0D;
		if (tradedGoods.containsKey(good))
			amountTraded += tradedGoods.get(good).doubleValue();

		double sellingInventory = getNumInInventory(good, sellingSettlement.getInventory());
		double sellingSupplyAmount = sellingInventory - amountTraded - 1D;
		if (sellingSupplyAmount < 0D)
			sellingSupplyAmount = 0D;
		double sellingValue = sellingSettlement.getGoodsManager().getGoodValuePerItem(good, sellingSupplyAmount);
		if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
			resource = ResourceUtil.findAmountResource(good.getID());
			sellingValue *= getResourceTradeAmount(resource);
		}
		boolean allTraded = (sellingInventory <= amountTraded);

		double buyingInventory = getNumInInventory(good, buyingSettlement.getInventory());
		double buyingSupplyAmount = buyingInventory + amountTraded + 1D;
		if (buyingSupplyAmount < 0D)
			buyingSupplyAmount = 0D;
		double buyingValue = buyingSettlement.getGoodsManager().getGoodValuePerItem(good, buyingSupplyAmount);
		if (good.getCategory() == GoodType.AMOUNT_RESOURCE)
			buyingValue *= getResourceTradeAmount(resource);

		boolean profitable = (buyingValue > sellingValue);
		boolean hasBuyValue = buyingValue > 0D;
		if ((allowNegValue || profitable) && hasBuyValue && !allTraded) {
			// Check if rover inventory has capacity for the good.
			boolean isRoverCapacity = hasCapacityInInventory(good, remainingCapacity, hasVehicle);

			boolean isContainerAvailable = true;
			if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
				Equipment container = getAvailableContainerForResource(resource,
						sellingSettlement, tradedGoods);
				isContainerAvailable = (container != null);
			}

			boolean isMissionRover = false;
			if (good.getCategory() == GoodType.VEHICLE) {
				if (good.getName().toLowerCase() == missionRover.getDescription().toLowerCase()) {
					if (sellingInventory == 1D)
						isMissionRover = true;
				}
			}

			boolean enoughResourceForContainer = true;
			if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
				enoughResourceForContainer = (sellingSupplyAmount >= getResourceTradeAmount(resource));
			}

			boolean enoughEVASuits = true;
			boolean enoughEquipment = true;
			if (good.getCategory() == GoodType.EQUIPMENT) {	
				if (good.getClassType() == EVASuit.class) {//.getName().equalsIgnoreCase("EVA Suit")) {
					double remainingSuits = sellingInventory - amountTraded;
					int requiredSuits = Trade.MAX_MEMBERS + 2;
					enoughEVASuits = remainingSuits > requiredSuits;
				}
				else {
					double remaining = sellingInventory - amountTraded;
					enoughEquipment = remaining > MIN_NUM_EQUIPMENT;
				}

			}

			boolean enoughRepairParts = true;
			if (good.getCategory() == GoodType.ITEM_RESOURCE) {
				if (repairParts.contains(good.getID())) {
					if (sellingSupplyAmount < MIN_REPAIR_PARTS)
						enoughRepairParts = false;
				}
			}

			boolean enoughLifeSupportResources = true;
			if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
				if (resource.isLifeSupport() && sellingSupplyAmount < MIN_LIFE_SUPPORT_RESOURCES)
					enoughLifeSupportResources = false;
			}

			if (isRoverCapacity && isContainerAvailable && !isMissionRover && enoughResourceForContainer
					&& enoughEVASuits && enoughEquipment && enoughRepairParts && enoughLifeSupportResources) {
				result = buyingValue - sellingValue;
			}
		}

		return result;
	}

	/**
	 * Checks if capacity in inventory for good.
	 * 
	 * @param good              the good to check for.
	 * @param remainingCapacity remaining general capacity (kg) in vehicle
	 *                          inventory.
	 * @param hasVehicle        true if good load already includes a vehicle.
	 * @return true if capacity for good.
	 * @throws Exception if error checking for capacity.
	 */
	private static boolean hasCapacityInInventory(Good good, double remainingCapacity, boolean hasVehicle) {
		boolean result = false;
		if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
			result = (remainingCapacity >= getResourceTradeAmount(ResourceUtil.findAmountResource(good.getID())));
		} else if (good.getCategory() == GoodType.ITEM_RESOURCE)
			result = remainingCapacity >= ItemResourceUtil.findItemResource(good.getID()).getMassPerItem();
		else if (good.getCategory() == GoodType.EQUIPMENT) {
			Class<? extends Equipment> type = good.getClassType();
			if (!equipmentGoodCache.containsKey(type)) {
				equipmentGoodCache.put(type, EquipmentFactory.createEquipment(type, new Coordinates(0D, 0D), true));
			}
			result = (remainingCapacity >= equipmentGoodCache.get(type).getBaseMass());
		} else if (good.getCategory() == GoodType.VEHICLE)
			result = !hasVehicle;
		return result;
	}

	/**
	 * Gets the number of a good currently in the inventory.
	 * 
	 * @param good      the good to check.
	 * @param inventory the inventory to check.
	 * @return number of goods in inventory.
	 * @throws Exception if error getting number of goods in inventory.
	 */
	public static double getNumInInventory(Good good, Inventory inventory) {
		if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
			return inventory.getAmountResourceStored(good.getID(), false);
		} else if (good.getCategory() == GoodType.ITEM_RESOURCE) {
			return inventory.getItemResourceNum(good.getID());
		} else if (good.getCategory() == GoodType.EQUIPMENT) {
			return inventory.findNumEmptyUnitsOfClass(EquipmentFactory.getEquipmentClass(good.getID()), false);
		} else if (good.getCategory() == GoodType.VEHICLE) {
			int count = 0;
			Iterator<Unit> i = inventory.findAllUnitsOfClass(Vehicle.class).iterator();
			while (i.hasNext()) {
				Vehicle vehicle = (Vehicle) i.next();
				boolean isEmpty = vehicle.getInventory().isEmpty(false);
				if (vehicle.getDescription().equalsIgnoreCase(good.getName()) && !vehicle.isReserved() && isEmpty) {
					count++;
				}
			}
			return count;
		} else {
			return 0D;
		}
	}

	/**
	 * Gets an available container for a given resource.
	 * 
	 * @param resource    the resource to check.
	 * @param settlement  the settlement to check for containers.
	 * @param tradedGoods the list of goods traded so far.
	 * @return container for the resource or null if none.
	 * @throws Exception if error.
	 */
	private static Equipment getAvailableContainerForResource(AmountResource resource, Settlement settlement,
			Map<Good, Integer> tradedGoods) {

		Equipment result = null;

		Class<? extends Equipment> containerClass = ContainerUtil.getContainerTypeNeeded(resource.getPhase());

		Inventory settlementInv = settlement.getInventory();

		int containersStored = settlementInv.findNumEmptyContainersOfClass(containerClass, false);

		Good containerGood = GoodsUtil.getEquipmentGood(containerClass);
		int containersTraded = 0;
		if (tradedGoods.containsKey(containerGood))
			containersTraded = tradedGoods.get(containerGood);

		if (containersStored > containersTraded)
			result = settlementInv.findAnEmptyEquipment(containerClass);

		return result;
	}

	/**
	 * Gets the estimated trade mission cost.
	 * 
	 * @param startingSettlement the settlement starting the trade mission.
	 * @param rover              the mission rover.
	 * @param distance           the distance (km) of the mission trip.
	 * @return the cost of the mission (value points).
	 * @throws Exception if error getting the estimated mission cost.
	 */
	public static double getEstimatedMissionCost(Settlement startingSettlement, Rover rover, double distance) {
		Map<Good, Integer> neededResources = new HashMap<Good, Integer>(4);

		// Get required fuel.
		Good fuelGood = GoodsUtil.getResourceGood(rover.getFuelType());
		neededResources.put(fuelGood, (int) VehicleMission.getFuelNeededForTrip(distance, rover.getEstimatedAveFuelConsumption(), false));

		// Get estimated trip time.
		double averageSpeed = rover.getBaseSpeed() / 2D;
		double averageSpeedMillisol = averageSpeed / MarsClock.convertSecondsToMillisols(60D * 60D);
		double tripTimeSols = ((distance / averageSpeedMillisol) + 1000D) / 1000D;

		double life_support_margin = Vehicle.getLifeSupportRangeErrorMargin();
		// Get oxygen amount.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
				* Mission.OXYGEN_MARGIN * life_support_margin;
		Good oxygenGood = GoodsUtil.getResourceGood(oxygenID);
		neededResources.put(oxygenGood, (int) oxygenAmount);

		// Get water amount.
		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
				* Mission.WATER_MARGIN * life_support_margin;
		Good waterGood = GoodsUtil.getResourceGood(waterID);
		neededResources.put(waterGood, (int) waterAmount);

		// Get food amount.
		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
				* Mission.FOOD_MARGIN * life_support_margin;
		Good foodGood = GoodsUtil.getResourceGood(foodID);
		neededResources.put(foodGood, (int) foodAmount);

		// Get dessert amount.
//		double dessertAmount = PhysicalCondition.getDessertConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS *
//				Rover.getErrorMargin();
//		AmountResource dessert = AmountResource.findAmountResource("Soymilk");
//		Good dessertGood = GoodsUtil.getResourceGood(dessert);
//		neededResources.put(dessertGood, (int) dessertAmount);

		// Get cost of resources.
		return determineLoadValue(neededResources, startingSettlement, false);
	}

	/**
	 * Gets the amount of a resource that should be traded based on its standard
	 * container capacity.
	 * 
	 * @param resource the amount resource.
	 * @return amount (kg) of resource to trade.
	 * @throws Exception if error determining container.
	 */
	private static double getResourceTradeAmount(AmountResource resource) {
		double result = 0D;

		Class<? extends Equipment> containerType = ContainerUtil.getContainerTypeNeeded(resource.getPhase());

		Equipment container = null;
		if (containerTypeCache.containsKey(containerType))
			container = containerTypeCache.get(containerType);
		else { 
			container = EquipmentFactory.createEquipment(containerType, new Coordinates(0, 0), true);
			containerTypeCache.put(containerType, container);
		}

		result = container.getInventory().getAmountResourceCapacity(resource, false);

		return result;
	}
	
}