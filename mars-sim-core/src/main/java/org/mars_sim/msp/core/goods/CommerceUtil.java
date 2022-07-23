/*
 * Mars Simulation Project
 * CommenceUtil.java
 * @date 2022-07-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Utility class for static methods for Commence Mission. 
 */
public final class CommerceUtil {

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(CommerceUtil.class.getName());

	/**
	 * Credit limit under which a seller is willing to sell goods to a buyer. Buyer
	 * must pay off credit to under limit to continue buying.
	 */
	private static final double SELL_CREDIT_LIMIT = 10_000_000D;

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

	private static MissionManager missionManager;
	private static UnitManager unitManager;
	private static MarsClock marsClock;
			
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
	 */
	public static Deal getBestDeal(Settlement startingSettlement, MissionType commerceType, Vehicle delivery) {
		List<Deal> deals = new ArrayList<>();
		for (Settlement tradingSettlement : unitManager.getSettlements()) {
			Deal deal = getPotentialDeal(startingSettlement, commerceType, tradingSettlement, delivery);
			if (deal != null) {
				deals.add(deal);
			}
		}

		if (deals.isEmpty()) {
			logger.info(startingSettlement, "No trading deal for " + commerceType.name());
			return null;
		}

		// Sort in reverse order (biggest profit first)
		Collections.sort(deals, Collections.reverseOrder());
		Deal bestDeal = deals.get(0);
		logger.info(startingSettlement, "New best deal for " + commerceType.name() + " to " + bestDeal.getBuyer().getName()
									+ " profit " + bestDeal.getProfit());
		return bestDeal;
	}

	/**
	 * Gets the available trade deal for combineation of settlements.
	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param commenceType The type of Commerce mission being evaulated
	 * @param delivery              the Vehicle to carry the trade.
	 * @return the deal(value points) for trade.
	 */
	public static Deal getPotentialDeal(Settlement startingSettlement, MissionType commerceType, Settlement tradingSettlement,
										Vehicle delivery) {
		double possibleRange = delivery.getRange(commerceType) * .8D;

		if (tradingSettlement != startingSettlement && tradingSettlement.isMissionEnable(commerceType)) {

			boolean hasCurrentCommerce = hasCurrentCommerceMission(startingSettlement, tradingSettlement);

			double settlementRange = Coordinates.computeDistance(tradingSettlement.getCoordinates(), startingSettlement.getCoordinates());
			boolean withinRange = (settlementRange <= possibleRange);

			if (!hasCurrentCommerce && withinRange) {					
				// Determine desired buy load,
				Map<Good, Integer> buyLoad = getDesiredBuyLoad(startingSettlement, delivery, tradingSettlement);
				
				// Determine sell load.
				Map<Good, Integer> sellLoad = determineBestSellLoad(startingSettlement, delivery, tradingSettlement);

				double profit = getEstimatedProfit(startingSettlement, delivery, tradingSettlement, buyLoad, sellLoad);
				return new Deal(tradingSettlement, profit, (MarsClock) marsClock.clone());
			}
		}

		return null;
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
				}
				else if (startingSettlement.equals(settlement2) && tradingSettlement.equals(settlement1)) {
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
	 * @param buyLoad Load being bought
	 * @param sellLoad Load being sold.
	 * @return the trade profit (value points)
	 */
	public static double getEstimatedProfit(Settlement startingSettlement, Vehicle delivery,
			Settlement tradingSettlement, Map<Good, Integer> buyLoad, Map<Good, Integer> sellLoad) {
		double sellingCreditHome = determineLoadCredit(sellLoad, startingSettlement, false);
		double sellingCreditRemote = determineLoadCredit(sellLoad, tradingSettlement, true);
		double sellingProfit = sellingCreditRemote - sellingCreditHome;

		double buyingCreditHome = determineLoadCredit(buyLoad, startingSettlement, true);
		double buyingCreditRemote = determineLoadCredit(buyLoad, tradingSettlement, false);
		double buyingProfit = buyingCreditHome - buyingCreditRemote;

		double totalProfit = sellingProfit + buyingProfit;

		// Determine estimated mission cost.
		double distance = startingSettlement.getCoordinates().getDistance(tradingSettlement.getCoordinates()) * 2D;
		double cost = getEstimatedMissionCost(startingSettlement, delivery, distance);

		return totalProfit - cost;
	}

	/**
	 * Gets the desired buy load from a trading settlement. Check there is sufficent Credit in place.
	 * 
	 * @param buyingSettlement the settlement that is buying.
	 * @param delivery              the vehicle used for trade.
	 * @param sellingSettlement  the settlement to buy from.
	 * @return the desired buy load.
	 * @throws Exception if error determining the buy load.
	 */
	public static Map<Good, Integer> getDesiredBuyLoad(Settlement buyingSettlement,
													Vehicle delivery, Settlement sellingSettlement) {
		// Get the credit that the starting settlement has with the destination
		// settlement.
		double credit = CreditManager.getCredit(buyingSettlement, sellingSettlement);

		Map<Good, Integer> desiredBuyLoad;
		if (credit > (SELL_CREDIT_LIMIT * -1D)) {
			// Determine desired buy load,
			desiredBuyLoad 	= determineLoad(buyingSettlement, sellingSettlement, delivery,
					Double.POSITIVE_INFINITY);
		}
		else {
			// Cannot buy from settlement due to credit limit.
			desiredBuyLoad = new HashMap<>(0);
		}

		return desiredBuyLoad;
	}

	/**
	 * Determines the best sell load from a settlement to another. Check there is sufficent Credit in place.
	 * 
	 * @param sellingSettlement the settlement to trade from.
	 * @param delivery              the vehicle to carry the goods.
	 * @param buyingSettlement  the settlement to trade to.
	 * @return a map of goods and numbers in the load.
	 * @throws Exception if error determining best sell load.
	 */
	public static Map<Good, Integer> determineBestSellLoad(Settlement sellingSettlement, Vehicle delivery,
			Settlement buyingSettlement) {
		double credit = CreditManager.getCredit(sellingSettlement, buyingSettlement);

		Map<Good, Integer> sellLoad;
		if (credit < SELL_CREDIT_LIMIT) {
			// Determine sell load.
			sellLoad = determineLoad(buyingSettlement, sellingSettlement, delivery,
									 Double.POSITIVE_INFINITY);
		} else {
			// Will not sell to settlement due to credit limit.
			sellLoad = new HashMap<>(0);
		}

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
		// Not sure if we need this ?
		// double missionPartsMass = MISSION_BASE_MASS;
		// if (massCapacity < missionPartsMass)
		// 	missionPartsMass = massCapacity;
		// massCapacity -= missionPartsMass;

		// Determine repair parts for trip.
		Set<Integer> repairParts = null;
		if (delivery instanceof Rover) {
			repairParts = delivery.getMalfunctionManager().getRepairPartProbabilities().keySet();
		}
		else {
			repairParts = Collections.emptySet();
		}	

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
							logger.warning(sellingSettlement, "Container for " + resource.getName() + " not available.");
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
	private static double determineLoadCredit(Map<Good, Integer> load, Settlement settlement, boolean buy) {
		double result = 0D;

		GoodsManager manager = settlement.getGoodsManager();

		for(Map.Entry<Good,Integer> goodItem : load.entrySet()) {
			Good good = goodItem.getKey();
			double cost = good.getCostOutput();
			int goodNumber = goodItem.getValue();
			double supply = good.getNumberForSettlement(settlement);
			
			// Calculate the new goods in the Settlment if the deal was done
			double newSupply = 0D;
			if (buy)
				newSupply = supply + goodNumber;
			else {
				newSupply = supply - goodNumber;
				if (newSupply < 0D)
					newSupply = 0D;
			}

			// Credit of the new total
			double value = manager.getDemandValue(good);
			result += value * cost * newSupply;

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
			for(Good good : buyingSettlement.getBuyList()) {
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
					// Make sure keep enough number of EVA suits for each citizen with margin 
					int requiredSuits = (int)(sellingSettlement.getNumCitizens() * 1.2);
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
	private static double getEstimatedMissionCost(Settlement startingSettlement, Vehicle delivery, double distance) {
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

			double lifeSupportMargin = Vehicle.getLifeSupportRangeErrorMargin();
			// Get oxygen amount.
			double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* Mission.OXYGEN_MARGIN * lifeSupportMargin;
			Good oxygenGood = GoodsUtil.getGood(OXYGEN_ID);
			neededResources.put(oxygenGood, (int) oxygenAmount);

			// Get water amount.
			double waterAmount = PhysicalCondition.getWaterConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* Mission.WATER_MARGIN * lifeSupportMargin;
			Good waterGood = GoodsUtil.getGood(WATER_ID);
			neededResources.put(waterGood, (int) waterAmount);

			// Get food amount.
			double foodAmount = PhysicalCondition.getFoodConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* Mission.FOOD_MARGIN * lifeSupportMargin;
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
	 * Negotiate a deal between 2 Settlement based on a load to be sold.
	 * @param buyingSettlement Buyer
	 * @param sellingSettlement Seller
	 * @param delivery Means how the goods are being exchanged
	 * @param tradeModifier Discount modifier
	 * @param load The Load being offer for sale.
	 * @return The load bought in return by the seller from the buyer
	*/
    public static Map<Good, Integer>  negotiateDeal(Settlement sellingSettlement, Settlement buyingSettlement, Vehicle delivery,
            double tradeModifier, Map<Good, Integer> load) {
				
		// Get the credit of the load that is being sold to the destination settlement.
		double baseSoldCredit = determineLoadCredit(load, sellingSettlement, true);
		double soldCredit = baseSoldCredit * tradeModifier;

		// Get the credit that the starting settlement has with the destination
		// settlement.
		double credit = CreditManager.getCredit(buyingSettlement, sellingSettlement);
		credit += soldCredit;
		CreditManager.setCredit(buyingSettlement, sellingSettlement, credit);

		Map<Good, Integer> buyLoad;
		// Check if buying settlement owes the selling settlement too much for them to
		// sell.
		if (credit > (-1D * SELL_CREDIT_LIMIT)) {

			// Determine the initial buy load based on goods that are profitable for the
			// destination settlement to sell.
			buyLoad = determineLoad(buyingSettlement, sellingSettlement, delivery, Double.POSITIVE_INFINITY);
			double baseBuyLoadValue = determineLoadCredit(buyLoad, buyingSettlement, true);
			double buyLoadValue = baseBuyLoadValue / tradeModifier;

			// Update the credit value between the starting and destination settlements.
			credit -= buyLoadValue;
			CreditManager.setCredit(buyingSettlement, sellingSettlement, credit);
		}
		else {
			buyLoad = new HashMap<>();
		}

		return buyLoad;
    }

	public static void initializeInstances(MarsClock c, MissionManager m, UnitManager u) {
		missionManager = m;
		unitManager = u;
		marsClock = c;
	}
}
