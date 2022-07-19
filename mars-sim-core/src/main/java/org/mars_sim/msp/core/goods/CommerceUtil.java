/*
 * Mars Simulation Project
 * CommenceUtil.java
 * @date 2022-07-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.goods.CreditManager;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodCategory;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Utility class for static methods for Commence Mission. 
 */
public final class CommerceUtil {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(CommerceUtil.class.getName());

	/**
	 * Credit limit under which a seller is willing to sell goods to a buyer. Buyer
	 * must pay off credit to under limit to continue buying.
	 */
	public static final double SELL_CREDIT_LIMIT = 10_000_000D;

    private static final int FREQUENCY = 1000;

	/** Estimated mission parts mass. */
	private static final double MISSION_BASE_MASS = 2_000D;

	/** Minimum mass (kg) of life support resources to leave at settlement. */
	private static final int MIN_LIFE_SUPPORT_RESOURCES = 100;

	/** Minimum number of repair parts to leave at settlement. */
	private static final int MIN_REPAIR_PARTS = 20;

	/** Minimum number of repair parts to leave at settlement. */
	private static final int MIN_NUM_EQUIPMENT = 10;

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int FOOD_ID = ResourceUtil.foodID;

	// Bad, need re-init
	private static Simulation sim = Simulation.instance();
	private static MissionManager missionManager = sim.getMissionManager();
	private static UnitManager unitManager = sim.getUnitManager();
	private static MarsClock marsClock = sim.getMasterClock().getMarsClock();

	private static Map<CommerceKey, Deal> deals = new HashMap<>();
			
	/**
	 * Private constructor for utility class.
	 */
	private CommerceUtil() {
	};

	/**
	 * Gets the best trade deal for a given settlement.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param commenceType The type of Commerce mission being evaulated
	 * @param delivery              the Vehicle to carry the trade.
	 * @return the deal(value points) for trade.
	 * @throws Exception if error while getting best trade profit.
	 */
	private static Deal getBestDeal(Settlement startingSettlement, MissionType commerceType, Vehicle delivery) {
		double bestProfit = 0D;
		Settlement bestSettlement = null;

		for (Settlement tradingSettlement : unitManager.getSettlements()) {
			if (tradingSettlement != startingSettlement && tradingSettlement.isMissionEnable(commerceType)) {

				boolean hasCurrentCommerce = hasCurrentCommerceMission(startingSettlement, tradingSettlement);

				double settlementRange = Coordinates.computeDistance(tradingSettlement.getCoordinates(), startingSettlement.getCoordinates());
				boolean withinRange = (settlementRange <= (delivery.getRange(commerceType) * .8D));

				if (!hasCurrentCommerce && withinRange) {
					double profit = getEstimatedProfit(startingSettlement, delivery, tradingSettlement);
					if (profit > bestProfit) {
						bestProfit = profit;
					}
				}
			}
		}

		return new Deal(bestSettlement, bestProfit, (MarsClock) marsClock.clone());
	}

	/**
	 * Checks if there is currently a commerce mission between two settlements.
	 * 
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @return true if current trade mission between settlements.
	 */
	private static boolean hasCurrentCommerceMission(Settlement settlement1, Settlement settlement2) {
		boolean result = false;

		for(Mission mission : missionManager.getMissions()) {
			if (mission instanceof CommerceMission) {
				CommerceMission tradeMission = (CommerceMission) mission;
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
	 * @param delivery              the vehicle to carry the trade goods.
	 * @param tradingSettlement  the settlement to trade to.
	 * @return the trade profit (value points)
	 * @throws Exception if error getting the estimated trade profit.
	 */
	private static double getEstimatedProfit(Settlement startingSettlement, Vehicle delivery,
			Settlement tradingSettlement) {

		// Determine estimated trade revenue.
		double revenue = getEstimatedRevenue(startingSettlement, delivery, tradingSettlement);

		// Determine estimated mission cost.
		double distance = startingSettlement.getCoordinates().getDistance(tradingSettlement.getCoordinates()) * 2D;
		double cost = getEstimatedMissionCost(startingSettlement, delivery, distance);

		return revenue - cost;
	}

	/**
	 * Gets the estimated trade revenue from one settlement to another.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param delivery              the vehicle to carry the trade goods.
	 * @param tradingSettlement  the settlement to trade to.
	 * @return the trade revenue (value points).
	 * @throws Exception if error getting the estimated trade revenue.
	 */
	private static double getEstimatedRevenue(Settlement startingSettlement, Vehicle delivery,
			Settlement tradingSettlement) {

		// Get credit between starting settlement and trading settlement.
		double credit = CreditManager.getCredit(startingSettlement, tradingSettlement);

		Map<Good, Integer> buyLoad = null;
		if (credit > (SELL_CREDIT_LIMIT * -1D)) {
			// Determine desired buy load,
			buyLoad = getDesiredBuyLoad(startingSettlement, delivery, tradingSettlement);
		} else {
			// Cannot buy from settlement due to credit limit.
			buyLoad = new HashMap<>(0);
		}

		Map<Good, Integer> sellLoad = null;
		if (credit < SELL_CREDIT_LIMIT) {
			// Determine sell load.
			sellLoad = determineBestSellLoad(startingSettlement, delivery, tradingSettlement);
		} else {
			// Will not sell to settlement due to credit limit.
			sellLoad = new HashMap<>(0);
		}

		double sellingCreditHome = determineLoadCredit(sellLoad, startingSettlement, false);
		double sellingCreditRemote = determineLoadCredit(sellLoad, tradingSettlement, true);
		double sellingProfit = sellingCreditRemote - sellingCreditHome;

		double buyingCreditHome = determineLoadCredit(buyLoad, startingSettlement, true);
		double buyingCreditRemote = determineLoadCredit(buyLoad, tradingSettlement, false);
		double buyingProfit = buyingCreditHome - buyingCreditRemote;

		double totalProfit = sellingProfit + buyingProfit;

		return totalProfit;
	}

	/**
	 * Gets the desired buy load from a trading settlement.
	 * 
	 * @param startingSettlement the settlement that is buying.
	 * @param delivery              the vehicle used for trade.
	 * @param tradingSettlement  the settlement to buy from.
	 * @return the desired buy load.
	 * @throws Exception if error determining the buy load.
	 */
	public static Map<Good, Integer> getDesiredBuyLoad(Settlement startingSettlement, Vehicle delivery,
			Settlement tradingSettlement) {

		// Determine best buy load.
		Map<Good, Integer> buyLoad = determineLoad(startingSettlement, tradingSettlement, delivery,
				Double.POSITIVE_INFINITY);

		return buyLoad;
	}

	/**
	 * Determines the best sell load from a settlement to another.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param delivery              the vehicle to carry the goods.
	 * @param tradingSettlement  the settlement to trade to.
	 * @return a map of goods and numbers in the load.
	 * @throws Exception if error determining best sell load.
	 */
	static Map<Good, Integer> determineBestSellLoad(Settlement startingSettlement, Vehicle delivery,
			Settlement tradingSettlement) {

		// Determine best sell load.
		Map<Good, Integer> sellLoad = determineLoad(tradingSettlement, startingSettlement, delivery,
				Double.POSITIVE_INFINITY);

		return sellLoad;
	}

	/**
	 * Determines the load between a buying settlement and a selling settlement.
	 * 
	 * @param buyingSettlement  the settlement buying the goods.
	 * @param sellingSettlement the settlement selling the goods.
	 * @param delivery             the Vehicle to carry the goods.
	 * @param maxBuyValue       the maximum value the selling settlement will
	 *                          permit.
	 * @return map of goods and their number.
	 * @throws Exception if error determining the load.
	 */
	public static Map<Good, Integer> determineLoad(Settlement buyingSettlement, Settlement sellingSettlement,
			Vehicle delivery, double maxBuyValue) {

		Map<Good, Integer> tradeList = new HashMap<>();
		boolean hasRover = false;
		GoodsManager buyerGoodsManager = buyingSettlement.getGoodsManager();
		buyerGoodsManager.prepareForLoadCalculation();
		GoodsManager sellerGoodsManager = sellingSettlement.getGoodsManager();
		sellerGoodsManager.prepareForLoadCalculation();

		double massCapacity = delivery.getCargoCapacity();

		// Subtract mission base mass (estimated).
		double missionPartsMass = MISSION_BASE_MASS;
		if (massCapacity < missionPartsMass)
			missionPartsMass = massCapacity;
		massCapacity -= missionPartsMass;

		// Determine repair parts for trip.
		Set<Integer> repairParts = delivery.getMalfunctionManager().getRepairPartProbabilities().keySet();

		// Determine the load.
		boolean done = false;
		double buyerLoadValue = 0D;
		Good previousGood = null;
		Set<Good> nonTradeGoods = Collections.emptySet();
		while (!done) {
			double remainingBuyValue = maxBuyValue - buyerLoadValue;
			Good good = findBestGood(sellingSettlement, buyingSettlement, tradeList, nonTradeGoods, massCapacity,
					hasRover, delivery, previousGood, false, repairParts, remainingBuyValue);
			if (good != null) {
				try {
					boolean isAmountResource = good.getCategory() == GoodCategory.AMOUNT_RESOURCE;
					boolean isItemResource = good.getCategory() == GoodCategory.ITEM_RESOURCE;
					AmountResource resource = null;
					
					// Add resource container if needed.
					if (isAmountResource) {
						resource = ResourceUtil.findAmountResource(good.getID());
						Container container = getAvailableContainerForResource(resource,
								sellingSettlement, tradeList);
						if (container != null) {
							Good containerGood = GoodsUtil.getEquipmentGood(container.getEquipmentType());
							massCapacity -= container.getBaseMass();
							int containerNum = 0;
							if (tradeList.containsKey(containerGood))
								containerNum = tradeList.get(containerGood);
							double containerSupply = containerGood.getNumberForSettlement(buyingSettlement);
							double totalContainerNum = containerNum + containerSupply;
							buyerLoadValue += buyerGoodsManager.determineGoodValueWithSupply(containerGood, totalContainerNum);
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
					if (good.getCategory() == GoodCategory.VEHICLE)
						hasRover = true;
					else {
						int number = 1;
						if (isAmountResource)
							number = (int) getResourceAmount(resource);
						else if (isItemResource)
							number = itemResourceNum;
						massCapacity -= (good.getMassPerItem() * number);
					}
					
					int currentNum = 0;
					if (tradeList.containsKey(good))
						currentNum = tradeList.get(good);
					double supply = good.getNumberForSettlement(buyingSettlement);
					double goodNum = 1D;
					
					if (isAmountResource)
						goodNum = getResourceAmount(resource);
					if (isItemResource)
						goodNum = itemResourceNum;
					
					double buyGoodValue = buyerGoodsManager.determineGoodValueWithSupply(good, (supply + currentNum + goodNum));
					
					if (isAmountResource) {
						double tradeAmount = getResourceAmount(resource);
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
	 * Determines the credit of a load to a settlement.
	 * 
	 * @param load       a map of the goods and their number.
	 * @param settlement the settlement valuing the load.
	 * @param buy        true if settlement is buying the load, false if selling.
	 * @return credit of the load (value points * production cost).
	 * @throws Exception if error determining the load credit.
	 */
	public static double determineLoadCredit(Map<Good, Integer> load, Settlement settlement, boolean buy) {
		double result = 0D;

		GoodsManager manager = settlement.getGoodsManager();

		Iterator<Good> i = load.keySet().iterator();
		while (i.hasNext()) {
			Good good = i.next();
			double cost = good.getCostOutput();
			int goodNumber = load.get(good);
			double supply = good.getNumberForSettlement(settlement);
			double multiplier = 1D;
			if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {	
				double amount = getResourceAmount(ResourceUtil.findAmountResource(good.getID()));
				if (amount < 1) {
					multiplier = 1;
				}
				else {
					goodNumber /= (int) amount;
					multiplier = amount;
				}
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

				double value = (manager.determineGoodValueWithSupply(good, supplyAmount) * multiplier);

				result += value * cost;
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
	 * @param delivery      the vehicle carrying the goods.
	 * @param previousGood      the previous trade good used in the trade.
	 * @param allowNegValue     allow negative value goods.
	 * @param repairParts       set of repair parts possibly needed for the trip.
	 * @param maxBuyValue       the maximum value the item can be.
	 * @return best good to trade or null if none found.
	 * @throws Exception if error determining best trade good.
	 */
	private static Good findBestGood(Settlement sellingSettlement, Settlement buyingSettlement,
			Map<Good, Integer> tradedGoods, Set<Good> nonTradeGoods, double remainingCapacity, boolean hasVehicle,
			Vehicle delivery, Good previousGood, boolean allowNegValue, Set<Integer> repairParts,
			double maxBuyValue) {

		Good result = null;

		// Check previous good first.
		if (previousGood != null) {
			double previousGoodValue = getGoodValue(previousGood, sellingSettlement, buyingSettlement, tradedGoods,
					remainingCapacity, hasVehicle, delivery, allowNegValue, repairParts);
			if ((previousGoodValue > 0D) && (previousGoodValue < maxBuyValue))
				result = previousGood;
		}

		// Check all goods.
		if (result == null) {
			double bestValue = 0D;
			if (allowNegValue)
				bestValue = Double.NEGATIVE_INFINITY;
			Iterator<Good> i = buyingSettlement.getBuyList().iterator(); 
			while (i.hasNext()) {
			Good good = i.next();
				if (!nonTradeGoods.contains(good)) {
					double tradeValue = getGoodValue(good, sellingSettlement, buyingSettlement, tradedGoods,
							remainingCapacity, hasVehicle, delivery, allowNegValue, repairParts);
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

		int sellingInventory = sellingSettlement.getItemResourceStored(item.getID());
		int buyingInventory = buyingSettlement.getItemResourceStored(item.getID());

		int numberTraded = 0;
		if (tradeList.containsKey(itemResourceGood))
			numberTraded = tradeList.get(itemResourceGood);

		int roverLimit = (int) (remainingCapacity / item.getMassPerItem());

		int totalTraded = numberTraded;
		double totalBuyingValue = 0D;
		boolean limitReached = false;
		while (!limitReached) {

			double sellingSupplyAmount = sellingInventory - totalTraded - 1D;
			double sellingValue = sellingSettlement.getGoodsManager().determineGoodValueWithSupply(itemResourceGood,
					sellingSupplyAmount);
			double buyingSupplyAmount = buyingInventory + totalTraded + 1D;
			double buyingValue = buyingSettlement.getGoodsManager().determineGoodValueWithSupply(itemResourceGood,
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
	 * @param delivery      the vehicle carrying the goods.
	 * @param allowNegValue     allow negative value goods.
	 * @param repairParts       set of repair parts possibly needed for the trip.
	 * @return trade value of good.
	 * @throws Exception if error determining trade value.
	 */
	private static double getGoodValue(Good good, Settlement sellingSettlement, Settlement buyingSettlement,
			Map<Good, Integer> tradedGoods, double remainingCapacity, boolean hasVehicle, Vehicle delivery,
			boolean allowNegValue, Set<Integer> repairParts) {

		double result = Double.NEGATIVE_INFINITY;
		AmountResource resource = null;
		double amountTraded = 0D;
		if (tradedGoods.containsKey(good))
			amountTraded += tradedGoods.get(good).doubleValue();

		double sellingInventory = getNumInInventory(good, sellingSettlement);
		double sellingSupplyAmount = sellingInventory - amountTraded - 1D;
		if (sellingSupplyAmount < 0D)
			sellingSupplyAmount = 0D;
		double sellingValue = sellingSettlement.getGoodsManager().determineGoodValueWithSupply(good, sellingSupplyAmount);
		if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
			resource = ResourceUtil.findAmountResource(good.getID());
			sellingValue *= getResourceAmount(resource);
		}
		boolean allTraded = (sellingInventory <= amountTraded);

		double buyingInventory = getNumInInventory(good, buyingSettlement);
		double buyingSupplyAmount = buyingInventory + amountTraded + 1D;
		if (buyingSupplyAmount < 0D)
			buyingSupplyAmount = 0D;
		double buyingValue = buyingSettlement.getGoodsManager().determineGoodValueWithSupply(good, buyingSupplyAmount);
		if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE)
			buyingValue *= getResourceAmount(resource);

		boolean profitable = (buyingValue > sellingValue);
		boolean hasBuyValue = buyingValue > 0D;
		if ((allowNegValue || profitable) && hasBuyValue && !allTraded) {
			// Check if rover inventory has capacity for the good.
			boolean isRoverCapacity = hasCapacityInInventory(good, buyingSettlement, remainingCapacity, hasVehicle);

			boolean isContainerAvailable = true;
			if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
				Container container = getAvailableContainerForResource(resource,
						sellingSettlement, tradedGoods);
				isContainerAvailable = (container != null);
			}

			boolean isMissionRover = false;
			if (good.getCategory() == GoodCategory.VEHICLE) {
				if (good.getName().equalsIgnoreCase(delivery.getDescription())) {
					if (sellingInventory == 1D)
						isMissionRover = true;
				}
			}

			boolean enoughResourceForContainer = true;
			if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
				enoughResourceForContainer = (sellingSupplyAmount >= getResourceAmount(resource));
			}

			boolean enoughEVASuits = true;
			boolean enoughEquipment = true;
			if (good.getCategory() == GoodCategory.EQUIPMENT
					|| good.getCategory() == GoodCategory.CONTAINER) {	
				if (good.getEquipmentType() == EquipmentType.EVA_SUIT) {
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
			if (good.getCategory() == GoodCategory.ITEM_RESOURCE) {
				if (repairParts.contains(good.getID())) {
					if (sellingSupplyAmount < MIN_REPAIR_PARTS)
						enoughRepairParts = false;
				}
			}

			boolean enoughLifeSupportResources = true;
			if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
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
	private static boolean hasCapacityInInventory(Good good, Settlement settlement, double remainingCapacity, boolean hasVehicle) {
		boolean result = false;
		if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
			result = (remainingCapacity >= getResourceAmount(ResourceUtil.findAmountResource(good.getID())));
		} else if (good.getCategory() == GoodCategory.ITEM_RESOURCE)
			result = remainingCapacity >= ItemResourceUtil.findItemResource(good.getID()).getMassPerItem();
		else if (good.getCategory() == GoodCategory.EQUIPMENT
				|| good.getCategory() == GoodCategory.CONTAINER) {
			result = (remainingCapacity >= EquipmentFactory.getEquipmentMass(good.getEquipmentType()));
		} else if (good.getCategory() == GoodCategory.VEHICLE)
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
	public static double getNumInInventory(Good good, Settlement settlement) {
		if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
			return settlement.getAmountResourceStored(good.getID());
		} else if (good.getCategory() == GoodCategory.ITEM_RESOURCE) {
			return settlement.getItemResourceStored(good.getID());
		} else if (good.getCategory() == GoodCategory.EQUIPMENT
				|| good.getCategory() == GoodCategory.CONTAINER) {
			return settlement.findNumEmptyContainersOfType(good.getEquipmentType(), false);
		} else if (good.getCategory() == GoodCategory.VEHICLE) {
			int count = 0;
			VehicleType vehicleType = VehicleType.convertNameToVehicleType(good.getName());
			Iterator<Unit> i = settlement.getVehicleTypeList(vehicleType).iterator();
			while (i.hasNext()) {
				Vehicle vehicle = (Vehicle) i.next();
				boolean isEmpty = vehicle.isEmpty();
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
	private static Container getAvailableContainerForResource(AmountResource resource, Settlement settlement,
			Map<Good, Integer> tradedGoods) {

		Container result = null;

		EquipmentType containerType = ContainerUtil.getContainerTypeNeeded(resource.getPhase());

		int containersStored = settlement.findNumEmptyContainersOfType(containerType, false);

		Good containerGood = GoodsUtil.getEquipmentGood(containerType);
		int containersTraded = 0;
		if (tradedGoods.containsKey(containerGood))
			containersTraded = tradedGoods.get(containerGood);

		if (containersStored > containersTraded)
			result = settlement.findContainer(containerType, true, resource.getID());

		return result;
	}

	/**
	 * Gets the estimated trade mission cost.
	 * 
	 * @param startingSettlement the settlement starting the trade mission.
	 * @param delivery              the mission vehicle.
	 * @param distance           the distance (km) of the mission trip.
	 * @return the cost of the mission (value points).
	 * @throws Exception if error getting the estimated mission cost.
	 */
	public static double getEstimatedMissionCost(Settlement startingSettlement, Vehicle delivery, double distance) {
		Map<Good, Integer> neededResources = new HashMap<>(4);

		// Get required fuel.
		Good fuelGood = GoodsUtil.getGood(delivery.getFuelType());
		neededResources.put(fuelGood, (int) VehicleMission.getFuelNeededForTrip(delivery, distance, 
				(delivery.getCumFuelEconomy() + delivery.getEstimatedFuelEconomy()) / VehicleMission.FE_FACTOR, false));

		if (delivery instanceof Crewable) {
			// Needs a crew
			// Get estimated trip time.
			double averageSpeed = delivery.getBaseSpeed() / 2D;
			double averageSpeedMillisol = averageSpeed / MarsClock.convertSecondsToMillisols(60D * 60D);
			double tripTimeSols = ((distance / averageSpeedMillisol) + 1000D) / 1000D;

			double life_support_margin = Vehicle.getLifeSupportRangeErrorMargin();
			// Get oxygen amount.
			double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* Mission.OXYGEN_MARGIN * life_support_margin;
			Good oxygenGood = GoodsUtil.getGood(OXYGEN_ID);
			neededResources.put(oxygenGood, (int) oxygenAmount);

			// Get water amount.
			double waterAmount = PhysicalCondition.getWaterConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* Mission.WATER_MARGIN * life_support_margin;
			Good waterGood = GoodsUtil.getGood(WATER_ID);
			neededResources.put(waterGood, (int) waterAmount);

			// Get food amount.
			double foodAmount = PhysicalCondition.getFoodConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* Mission.FOOD_MARGIN * life_support_margin;
			Good foodGood = GoodsUtil.getGood(FOOD_ID);
			neededResources.put(foodGood, (int) foodAmount);
		}

		// Get cost of resources.
		return determineLoadCredit(neededResources, startingSettlement, false);
	}
	
	/**
	 * Gets the amount of a resource that should be traded based on its standard
	 * container capacity.
	 * 
	 * @param resource the amount resource.
	 * @return amount (kg) of resource to trade.
	 * @throws Exception if error determining container.
	 */
	private static double getResourceAmount(AmountResource resource) {
		EquipmentType containerType = ContainerUtil.getContainerTypeNeeded(resource.getPhase());
		return ContainerUtil.getContainerCapacity(containerType);
	}

	/**
	 * Get the best settlement to do a Commerce with
	 * @param seller Selling settlement
	 * @param commerce Type of commerce mission
	 */
	public static Settlement getBestSettlement(Settlement seller, MissionType commerce, Vehicle delivery) {
		return getDeal(seller, commerce, delivery).buyer;
	}

	/**
	 * Get the best profit a settlement can achieve via commerce
	 * @param seller Selling settlement
	 * @param commerce Type of commerce mission
	 */
	public static double getBestProfit(Settlement seller, MissionType commerce, Vehicle delivery) {
		return getDeal(seller, commerce, delivery).profit;
	}

	private static Deal getDeal(Settlement seller, MissionType commerce, Vehicle delivery) {
		CommerceKey key = new CommerceKey(seller, commerce);
		Deal deal = deals.get(key);

		if ((deal != null) 
				&& (MarsClock.getTimeDiff(marsClock, deal.created) > FREQUENCY)) {
			return deal;
		}

		// Recalculate
		Deal best = getBestDeal(seller, commerce, delivery);
		deals.put(key, best);

		return best;
	}

	public static void clearBestSettlement(Settlement seller, MissionType commerce) {
		deals.remove(new CommerceKey(seller, commerce));
	}
	
	/**
	 * Represents a potential deal with another Settlement.
	 */
	private static class Deal {
		Settlement buyer;
		double profit;
		MarsClock created;

		Deal(Settlement buyer, double profit, MarsClock created) {
			this.buyer = buyer;
			this.profit = profit;
			this.created = created;
		}
	}

	/**
	 * Composite key class.
	 */
	private static class CommerceKey {
		Settlement seller;
		MissionType commerceType;
		
		public CommerceKey(Settlement seller, MissionType commerceType) {
			this.seller = seller;
			this.commerceType = commerceType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((commerceType == null) ? 0 : commerceType.hashCode());
			result = prime * result + ((seller == null) ? 0 : seller.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CommerceKey other = (CommerceKey) obj;
			if (commerceType != other.commerceType)
				return false;
			if (seller == null) {
				if (other.seller != null)
					return false;
			} else if (!seller.equals(other.seller))
				return false;
			return true;
		}
	}
}
