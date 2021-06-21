/**
 * Mars Simulation Project
 * DeliveryUtil.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
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
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Utility class for static methods for Delivery Mission. TODO externalize strings
 */
public final class DeliveryUtil {

	/** default logger. */
	private static Logger logger = Logger.getLogger(DeliveryUtil.class.getName());

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

	/** Cache for the best delivery settlement. */
	public static Settlement bestDeliverySettlementCache = null;

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
	private DeliveryUtil() {
	};

	/**
	 * Gets the best delivery value for a given settlement.
	 * 
	 * @param startingSettlement the settlement to delivery from.
	 * @param drone              the drone to carry the delivery.
	 * @return the best value (value points) for delivery.
	 * @throws Exception if error while getting best delivery profit.
	 */
	public static double getBestDeliveryProfit(Settlement startingSettlement, Drone drone) {
		double bestProfit = 0D;
		Settlement bestSettlement = null;

		for (Settlement tradingSettlement : unitManager.getSettlements()) {
			if (tradingSettlement != startingSettlement && tradingSettlement.isMissionDisable(Delivery.DEFAULT_DESCRIPTION)) {

				boolean hasCurrentDeliveryMission = hasCurrentDeliveryMission(startingSettlement, tradingSettlement);

				double settlementRange = Coordinates.computeDistance(tradingSettlement.getCoordinates(), startingSettlement.getCoordinates());
				boolean withinRange = (settlementRange <= (drone.getRange(Delivery.missionType) * .8D));

				if (!hasCurrentDeliveryMission && withinRange) {
					// double startTime = System.currentTimeMillis();

					double profit = getEstimatedDeliveryProfit(startingSettlement, drone, tradingSettlement);
					// double endTime = System.currentTimeMillis();
//					 logger.finest("getEstimatedDeliveryProfit " + (endTime - startTime));
					if (profit > bestProfit) {
						bestProfit = profit;
						bestSettlement = tradingSettlement;
					}
				}
			}
		}

		// Set settlement cache.
		bestDeliverySettlementCache = bestSettlement;

		return bestProfit;
	}

	/**
	 * Checks if there is currently a delivery mission between two settlements.
	 * 
	 * @param settlement1 the first settlement.
	 * @param settlement2 the second settlement.
	 * @return true if current delivery mission between settlements.
	 */
	private static boolean hasCurrentDeliveryMission(Settlement settlement1, Settlement settlement2) {
		boolean result = false;

		// MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof Delivery) {
				Delivery deliveryMission = (Delivery) mission;
				Settlement startingSettlement = deliveryMission.getStartingSettlement();
				Settlement tradingSettlement = deliveryMission.getTradingSettlement();
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
	 * Gets the estimated delivery profit from one settlement to another.
	 * 
	 * @param startingSettlement the settlement to delivery from.
	 * @param drone              the drone to carry the delivery goods.
	 * @param tradingSettlement  the settlement to delivery to.
	 * @return the delivery profit (value points)
	 * @throws Exception if error getting the estimated delivery profit.
	 */
	private static double getEstimatedDeliveryProfit(Settlement startingSettlement, Drone drone,
			Settlement tradingSettlement) {

		// Determine estimated delivery revenue.
		double revenue = getEstimatedDeliveryRevenue(startingSettlement, drone, tradingSettlement);

		// Determine estimated mission cost.
		double distance = startingSettlement.getCoordinates().getDistance(tradingSettlement.getCoordinates()) * 2D;
		double cost = getEstimatedMissionCost(startingSettlement, drone, distance);

		return revenue - cost;
	}

	/**
	 * Gets the estimated delivery revenue from one settlement to another.
	 * 
	 * @param startingSettlement the settlement to delivery from.
	 * @param drone              the drone to carry the delivery goods.
	 * @param tradingSettlement  the settlement to delivery to.
	 * @return the delivery revenue (value points).
	 * @throws Exception if error getting the estimated delivery revenue.
	 */
	private static double getEstimatedDeliveryRevenue(Settlement startingSettlement, Drone drone,
			Settlement tradingSettlement) {

		// Get credit between starting settlement and trading settlement.
		double credit = creditManager.getCredit(startingSettlement, tradingSettlement);

		Map<Good, Integer> buyLoad = null;
		if (credit > (DeliveryUtil.SELL_CREDIT_LIMIT * -1D)) {
			// Determine desired buy load,
			buyLoad = DeliveryUtil.getDesiredBuyLoad(startingSettlement, drone, tradingSettlement);
		} else {
			// Cannot buy from settlement due to credit limit.
			buyLoad = new HashMap<Good, Integer>(0);
		}

		Map<Good, Integer> sellLoad = null;
		if (credit < DeliveryUtil.SELL_CREDIT_LIMIT) {
			// Determine sell load.
			sellLoad = DeliveryUtil.determineBestSellLoad(startingSettlement, drone, tradingSettlement);
		} else {
			// Will not sell to settlement due to credit limit.
			sellLoad = new HashMap<Good, Integer>(0);
		}

		double sellingValueHome = DeliveryUtil.determineLoadValue(sellLoad, startingSettlement, false);
		double sellingValueRemote = DeliveryUtil.determineLoadValue(sellLoad, tradingSettlement, true);
		double sellingProfit = sellingValueRemote - sellingValueHome;

		double buyingValueHome = DeliveryUtil.determineLoadValue(buyLoad, startingSettlement, true);
		double buyingValueRemote = DeliveryUtil.determineLoadValue(buyLoad, tradingSettlement, false);
		double buyingProfit = buyingValueHome - buyingValueRemote;

		double totalProfit = sellingProfit + buyingProfit;

		return totalProfit;
	}

	/**
	 * Gets the desired buy load from a trading settlement.
	 * 
	 * @param startingSettlement the settlement that is buying.
	 * @param drone              the drone used for delivery.
	 * @param tradingSettlement  the settlement to buy from.
	 * @return the desired buy load.
	 * @throws Exception if error determining the buy load.
	 */
	public static Map<Good, Integer> getDesiredBuyLoad(Settlement startingSettlement, Drone drone,
			Settlement tradingSettlement) {

		// Determine best buy load.
		Map<Good, Integer> buyLoad = determineLoad(startingSettlement, tradingSettlement, drone,
				Double.POSITIVE_INFINITY);

		return buyLoad;
	}

	/**
	 * Determines the best sell load from a settlement to another.
	 * 
	 * @param startingSettlement the settlement to delivery from.
	 * @param drone              the drone to carry the goods.
	 * @param tradingSettlement  the settlement to delivery to.
	 * @return a map of goods and numbers in the load.
	 * @throws Exception if error determining best sell load.
	 */
	static Map<Good, Integer> determineBestSellLoad(Settlement startingSettlement, Drone drone,
			Settlement tradingSettlement) {

		// Determine best sell load.
		Map<Good, Integer> sellLoad = determineLoad(tradingSettlement, startingSettlement, drone,
				Double.POSITIVE_INFINITY);

		return sellLoad;
	}

	/**
	 * Determines the load between a buying settlement and a selling settlement.
	 * 
	 * @param buyingSettlement  the settlement buying the goods.
	 * @param sellingSettlement the settlement selling the goods.
	 * @param drone             the drone to carry the goods.
	 * @param maxBuyValue       the maximum value the selling settlement will
	 *                          permit.
	 * @return map of goods and their number.
	 * @throws Exception if error determining the load.
	 */
	public static Map<Good, Integer> determineLoad(Settlement buyingSettlement, Settlement sellingSettlement,
			Drone drone, double maxBuyValue) {

		Map<Good, Integer> deliveryList = new HashMap<Good, Integer>();
		boolean hasRover = false;
		GoodsManager buyerGoodsManager = buyingSettlement.getGoodsManager();
		buyerGoodsManager.prepareForLoadCalculation();
		GoodsManager sellerGoodsManager = sellingSettlement.getGoodsManager();
		sellerGoodsManager.prepareForLoadCalculation();

		double massCapacity = drone.getInventory().getGeneralCapacity();

		// Subtract mission base mass (estimated).
		double missionPartsMass = MISSION_BASE_MASS;
		if (massCapacity < missionPartsMass)
			missionPartsMass = massCapacity;
		massCapacity -= missionPartsMass;

		// Determine repair parts for trip.
		Set<Integer> repairParts = drone.getMalfunctionManager().getRepairPartProbabilities().keySet();

		// Determine the load.
		boolean done = false;
		double buyerLoadValue = 0D;
		Good previousGood = null;
		Set<Good> nonDeliveryGoods = Collections.emptySet();
		while (!done) {
			double remainingBuyValue = maxBuyValue - buyerLoadValue;
			Good good = findBestDeliveryGood(sellingSettlement, buyingSettlement, deliveryList, nonDeliveryGoods, massCapacity,
					hasRover, drone, previousGood, false, repairParts, remainingBuyValue);
			if (good != null) {
				try {
					boolean isAmountResource = good.getCategory() == GoodType.AMOUNT_RESOURCE;
					boolean isItemResource = good.getCategory() == GoodType.ITEM_RESOURCE;
					AmountResource resource = null;
					
					// Add resource container if needed.
					if (isAmountResource) {
						resource = ResourceUtil.findAmountResource(good.getID());
						Equipment container = getAvailableContainerForResource(resource,
								sellingSettlement, deliveryList);
						if (container != null) {
							Good containerGood = GoodsUtil.getEquipmentGood(container.getClass());
							massCapacity -= container.getBaseMass();
							int containerNum = 0;
							if (deliveryList.containsKey(containerGood))
								containerNum = deliveryList.get(containerGood);
							double containerSupply = buyerGoodsManager.getNumberOfGoodForSettlement(containerGood);
							double totalContainerNum = containerNum + containerSupply;
							buyerLoadValue += buyerGoodsManager.getGoodValuePerItem(containerGood, totalContainerNum);
							deliveryList.put(containerGood, (containerNum + 1));
						} else
							logger.warning("container for " + resource.getName() + " not available.");
					}

					int itemResourceNum = 0;
					if (isItemResource) {
						itemResourceNum = getNumItemResourcesToDelivery(good, sellingSettlement, buyingSettlement,
								deliveryList, massCapacity, remainingBuyValue);
					}

					// Add good.
					if (good.getCategory() == GoodType.VEHICLE)
						hasRover = true;
					else {
						int number = 1;
						if (isAmountResource)
							number = (int) getResourceDeliveryAmount(ResourceUtil.findAmountResource(good.getID()));
						else if (isItemResource)
							number = itemResourceNum;
						massCapacity -= (GoodsUtil.getGoodMassPerItem(good) * number);
					}
					
					int currentNum = 0;
					if (deliveryList.containsKey(good))
						currentNum = deliveryList.get(good);
					double supply = buyerGoodsManager.getNumberOfGoodForSettlement(good);
					double goodNum = 1D;
					
					if (isAmountResource)
						goodNum = getResourceDeliveryAmount(resource);
					if (isItemResource)
						goodNum = itemResourceNum;
					
					double buyGoodValue = buyerGoodsManager.getGoodValuePerItem(good, (supply + currentNum + goodNum));
					
					if (isAmountResource) {
						double deliveryAmount = getResourceDeliveryAmount(resource);
						buyGoodValue *= deliveryAmount;
					}
					if (isItemResource) {
						buyGoodValue *= itemResourceNum;
					}
					buyerLoadValue += buyGoodValue;
					int newNumber = currentNum + (int) goodNum;
					deliveryList.put(good, newNumber);
				} catch (Exception e) {
					done = true;
				}
			} else
				done = true;

			previousGood = good;
		}

		return deliveryList;
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
				double deliveryAmount = getResourceDeliveryAmount(ResourceUtil.findAmountResource(good.getID()));
				goodNumber /= (int) deliveryAmount;
				multiplier = deliveryAmount;
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
	 * Finds the best delivery good for a delivery.
	 * 
	 * @param sellingSettlement the settlement selling the good.
	 * @param buyingSettlement  the settlement buying the good.
	 * @param deliverydGoods       the map of goods deliveryd so far.
	 * @param nonDeliveryGoods     the set of goods not to delivery.
	 * @param remainingCapacity remaining general capacity (kg) in vehicle
	 *                          inventory.
	 * @param hasVehicle        true if a vehicle is in the delivery goods.
	 * @param missionRover      the drone carrying the goods.
	 * @param previousGood      the previous delivery good used in the delivery.
	 * @param allowNegValue     allow negative value goods.
	 * @param repairParts       set of repair parts possibly needed for the trip.
	 * @param maxBuyValue       the maximum value the item can be.
	 * @return best good to delivery or null if none found.
	 * @throws Exception if error determining best delivery good.
	 */
	private static Good findBestDeliveryGood(Settlement sellingSettlement, Settlement buyingSettlement,
			Map<Good, Integer> deliverydGoods, Set<Good> nonDeliveryGoods, double remainingCapacity, boolean hasVehicle,
			Drone missionRover, Good previousGood, boolean allowNegValue, Set<Integer> repairParts,
			double maxBuyValue) {

		Good result = null;

		// Check previous good first.
		if (previousGood != null) {
			double previousGoodValue = getDeliveryValue(previousGood, sellingSettlement, buyingSettlement, deliverydGoods,
					remainingCapacity, hasVehicle, missionRover, allowNegValue, repairParts);
			if ((previousGoodValue > 0D) && (previousGoodValue < maxBuyValue))
				result = previousGood;
		}

		// Check all goods.
		if (result == null) {
			double bestValue = 0D;
			if (allowNegValue)
				bestValue = Double.NEGATIVE_INFINITY;
			Iterator<Good> i = GoodsUtil.getGoodsList().iterator();
			while (i.hasNext()) {
			Good good = i.next();
				if (!nonDeliveryGoods.contains(good)) {
					double deliveryValue = getDeliveryValue(good, sellingSettlement, buyingSettlement, deliverydGoods,
							remainingCapacity, hasVehicle, missionRover, allowNegValue, repairParts);
					if ((deliveryValue > bestValue) && (deliveryValue < maxBuyValue)) {
						result = good;
						bestValue = deliveryValue;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the number of an item resource good that should be deliveryd.
	 * 
	 * @param itemResourceGood  the item resource good.
	 * @param sellingSettlement the settlement selling the good.
	 * @param buyingSettlement  the settlement buying the good.
	 * @param deliveryList         the map of goods deliveryd so far.
	 * @param remainingCapacity remaining general capacity (kg) in vehicle
	 *                          inventory.
	 * @param maxBuyValue       the maximum buy value.
	 * @return number of goods to delivery.
	 * @throws Exception if error determining number of goods.
	 */
	private static int getNumItemResourcesToDelivery(Good itemResourceGood, Settlement sellingSettlement,
			Settlement buyingSettlement, Map<Good, Integer> deliveryList, double remainingCapacity, double maxBuyValue) {

		int result = 0;

		Part item = ItemResourceUtil.findItemResource(itemResourceGood.getID());

		int sellingInventory = sellingSettlement.getInventory().getItemResourceNum(item);
		int buyingInventory = buyingSettlement.getInventory().getItemResourceNum(item);

		int numberDeliveryd = 0;
		if (deliveryList.containsKey(itemResourceGood))
			numberDeliveryd = deliveryList.get(itemResourceGood);

		int roverLimit = (int) (remainingCapacity / item.getMassPerItem());

		int totalDeliveryd = numberDeliveryd;
		double totalBuyingValue = 0D;
		boolean limitReached = false;
		while (!limitReached) {

			double sellingSupplyAmount = sellingInventory - totalDeliveryd - 1;
			double sellingValue = sellingSettlement.getGoodsManager().getGoodValuePerItem(itemResourceGood,
					sellingSupplyAmount);
			double buyingSupplyAmount = buyingInventory + totalDeliveryd + 1;
			double buyingValue = buyingSettlement.getGoodsManager().getGoodValuePerItem(itemResourceGood,
					buyingSupplyAmount);

			if (buyingValue <= sellingValue)
				limitReached = true;
			if (totalDeliveryd + 1 > sellingInventory)
				limitReached = true;
			if (totalDeliveryd + 1 > roverLimit)
				limitReached = true;
			if ((totalBuyingValue + buyingValue) >= maxBuyValue)
				limitReached = true;

			if (!limitReached) {
				result++;
				totalDeliveryd = numberDeliveryd + result;
				totalBuyingValue += buyingValue;
			}
		}

		// Result shouldn't be zero, but just in case it is.
		if (result == 0)
			result = 1;
		return result;
	}

	/**
	 * Gets the delivery value of a good.
	 * 
	 * @param good              the good
	 * @param sellingSettlement the settlement selling the good.
	 * @param buyingSettlement  the settlement buying the good.
	 * @param deliverydGoods       the map of goods deliveryd so far.
	 * @param remainingCapacity remaining general capacity (kg) in vehicle
	 *                          inventory.
	 * @param hasVehicle        true if a vehicle is in the delivery goods.
	 * @param missionDrone      the drone carrying the goods.
	 * @param allowNegValue     allow negative value goods.
	 * @param repairParts       set of repair parts possibly needed for the trip.
	 * @return delivery value of good.
	 * @throws Exception if error determining delivery value.
	 */
	private static double getDeliveryValue(Good good, Settlement sellingSettlement, Settlement buyingSettlement,
			Map<Good, Integer> deliverydGoods, double remainingCapacity, boolean hasVehicle, Drone missionDrone,
			boolean allowNegValue, Set<Integer> repairParts) {

		double result = Double.NEGATIVE_INFINITY;
		AmountResource resource = null;
		double amountDeliveryd = 0D;
		if (deliverydGoods.containsKey(good))
			amountDeliveryd += deliverydGoods.get(good).doubleValue();

		double sellingInventory = getNumInInventory(good, sellingSettlement.getInventory());
		double sellingSupplyAmount = sellingInventory - amountDeliveryd - 1D;
		if (sellingSupplyAmount < 0D)
			sellingSupplyAmount = 0D;
		double sellingValue = sellingSettlement.getGoodsManager().getGoodValuePerItem(good, sellingSupplyAmount);
		if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
			resource = ResourceUtil.findAmountResource(good.getID());
			sellingValue *= getResourceDeliveryAmount(resource);
		}
		boolean allDeliveryd = (sellingInventory <= amountDeliveryd);

		double buyingInventory = getNumInInventory(good, buyingSettlement.getInventory());
		double buyingSupplyAmount = buyingInventory + amountDeliveryd + 1D;
		if (buyingSupplyAmount < 0D)
			buyingSupplyAmount = 0D;
		double buyingValue = buyingSettlement.getGoodsManager().getGoodValuePerItem(good, buyingSupplyAmount);
		if (good.getCategory() == GoodType.AMOUNT_RESOURCE)
			buyingValue *= getResourceDeliveryAmount(resource);

		boolean profitable = (buyingValue > sellingValue);
		boolean hasBuyValue = buyingValue > 0D;
		if ((allowNegValue || profitable) && hasBuyValue && !allDeliveryd) {
			// Check if drone inventory has capacity for the good.
			boolean isRoverCapacity = hasCapacityInInventory(good, remainingCapacity, hasVehicle);

			boolean isContainerAvailable = true;
			if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
				Equipment container = getAvailableContainerForResource(resource,
						sellingSettlement, deliverydGoods);
				isContainerAvailable = (container != null);
			}

			boolean isMissionRover = false;
			if (good.getCategory() == GoodType.VEHICLE) {
				if (good.getName().toLowerCase() == missionDrone.getDescription().toLowerCase()) {
					if (sellingInventory == 1D)
						isMissionRover = true;
				}
			}

			boolean enoughResourceForContainer = true;
			if (good.getCategory() == GoodType.AMOUNT_RESOURCE) {
				enoughResourceForContainer = (sellingSupplyAmount >= getResourceDeliveryAmount(resource));
			}

			boolean enoughEVASuits = true;
			boolean enoughEquipment = true;
			if (good.getCategory() == GoodType.EQUIPMENT) {	
				if (good.getClassType() == EVASuit.class) {//.getName().equalsIgnoreCase("EVA Suit")) {
					double remainingSuits = sellingInventory - amountDeliveryd;
					int requiredSuits = Delivery.MAX_MEMBERS + 2;
					enoughEVASuits = remainingSuits > requiredSuits;
				}
				else {
					double remaining = sellingInventory - amountDeliveryd;
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
			result = (remainingCapacity >= getResourceDeliveryAmount(ResourceUtil.findAmountResource(good.getID())));
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
	 * @param deliverydGoods the list of goods deliveryd so far.
	 * @return container for the resource or null if none.
	 * @throws Exception if error.
	 */
	private static Equipment getAvailableContainerForResource(AmountResource resource, Settlement settlement,
			Map<Good, Integer> deliverydGoods) {

		Equipment result = null;

		Class<? extends Equipment> containerClass = ContainerUtil.getContainerTypeNeeded(resource.getPhase());

		Inventory settlementInv = settlement.getInventory();

		int containersStored = settlementInv.findNumEmptyContainersOfClass(containerClass, false);

		Good containerGood = GoodsUtil.getEquipmentGood(containerClass);
		int containersDeliveryd = 0;
		if (deliverydGoods.containsKey(containerGood))
			containersDeliveryd = deliverydGoods.get(containerGood);

		if (containersStored > containersDeliveryd)
			result = settlementInv.findAnEmptyEquipment(containerClass);

		return result;
	}

	/**
	 * Gets the estimated delivery mission cost.
	 * 
	 * @param startingSettlement the settlement starting the delivery mission.
	 * @param drone              the delivery drone.
	 * @param distance           the distance (km) of the mission trip.
	 * @return the cost of the mission (value points).
	 * @throws Exception if error getting the estimated mission cost.
	 */
	public static double getEstimatedMissionCost(Settlement startingSettlement, Drone drone, double distance) {
		Map<Good, Integer> neededResources = new HashMap<Good, Integer>(4);

		// Get required fuel.
		Good fuelGood = GoodsUtil.getResourceGood(drone.getFuelType());
		neededResources.put(fuelGood, (int) VehicleMission.getFuelNeededForTrip(distance, drone.getEstimatedAveFuelConsumption(), false));

		// Get estimated trip time.
		double averageSpeed = drone.getBaseSpeed() / 2D;
		double averageSpeedMillisol = averageSpeed / MarsClock.convertSecondsToMillisols(60D * 60D);
		double tripTimeSols = ((distance / averageSpeedMillisol) + 1000D) / 1000D;

		// Get cost of resources.
		return determineLoadValue(neededResources, startingSettlement, false);
	}
	
	/**
	 * Gets the amount of a resource that should be delivered based on its standard
	 * container capacity.
	 * 
	 * @param resource the amount resource.
	 * @return amount (kg) of resource to delivery.
	 * @throws Exception if error determining container.
	 */
	private static double getResourceDeliveryAmount(AmountResource resource) {
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
