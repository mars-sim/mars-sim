/*
 * Mars Simulation Project
 * CommenceUtil.java
 * @date 2022-07-19
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Utility class for static methods for Commence Mission. 
 */
public final class CommerceUtil {

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(CommerceUtil.class.getName());

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int FOOD_ID = ResourceUtil.foodID;
	
	/**
	 * Credit limit under which a seller is willing to sell goods to a buyer. Buyer
	 * must pay off credit to under limit to continue buying.
	 */
	private static final double SELL_CREDIT_LIMIT = 10_000_000D;

	private static MissionManager missionManager;
	private static UnitManager unitManager;
			
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
		Deal bestDeal = null;
		for (Settlement tradingSettlement : unitManager.getSettlements()) {
			Deal deal = getPotentialDeal(startingSettlement, commerceType, tradingSettlement, delivery);
			if ((deal != null) 
				&& ((bestDeal == null) || (bestDeal.getProfit() > deal.getProfit()))) {
					bestDeal = deal;
			}
		}

		if (bestDeal == null) {
			logger.info(startingSettlement, 30_000, "No deal for a " + commerceType.name().toLowerCase() + " mission.");
			return null;
		}
		logger.info(startingSettlement, "New best deal for " + commerceType.name() + " to " + bestDeal.getBuyer().getName()
									+ " profit " + bestDeal.getProfit() + ".");
		return bestDeal;
	}

	/**
	 * Gets the available trade deal for combination of settlements.
	 * 
	 * @param startingSettlement the settlement initiating the deal
	 * @param commenceType The type of Commerce mission being evaluated
	 * @param tradingSettlement Settlement potnetially completing the Deal.
	 * @param delivery              the Vehicle to carry the trade.
	 * @return the deal(value points) for trade.
	 */
	public static Deal getPotentialDeal(Settlement startingSettlement, MissionType commerceType, Settlement tradingSettlement,
										Vehicle delivery) {
		double possibleRange = delivery.getRange() * .8D;

		if (!startingSettlement.equals(tradingSettlement) && tradingSettlement.isMissionEnable(commerceType)) {

			boolean hasCurrentCommerce = hasCurrentCommerceMission(startingSettlement, tradingSettlement);

			double settlementRange = tradingSettlement.getCoordinates().getDistance(startingSettlement.getCoordinates());
			boolean withinRange = (settlementRange <= possibleRange);

			if (!hasCurrentCommerce && withinRange) {					
				// Determine desired buy load at the other end. So reverse the buyer & seller 
				// when calling
				Shipment buyLoad = getDesiredBuyLoad(startingSettlement, delivery, tradingSettlement);
				
				// Determine sell load.
				Shipment sellLoad = determineBestSellLoad(startingSettlement, delivery, tradingSettlement);

				if (buyLoad != null && sellLoad != null) {
					return createDeal(startingSettlement, delivery, tradingSettlement, buyLoad, sellLoad);
				}
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
	public static double getEstimatedProfit(Settlement sellingSettlement, Vehicle delivery,
			Settlement buyingSettlement, Map<Good, Integer> buyLoad, Map<Good, Integer> sellLoad) {
		double buyCost = determineLoadCredit(buyLoad, buyingSettlement, false);
		double sellRevenue = determineLoadCredit(sellLoad, sellingSettlement, true);

		return createDeal(sellingSettlement, delivery, buyingSettlement, new Shipment(buyLoad, buyCost),
							new Shipment(sellLoad, sellRevenue)).getProfit();
	}

	/**
	 * Gets the Deal details for a trade between 2 settlements.	 * 
	 * @param startingSettlement the settlement to trade from.
	 * @param delivery              the vehicle to carry the trade goods.
	 * @param tradingSettlement  the settlement to trade to.
	 * @param buyLoad Load being bought
	 * @param sellLoad Load being sold.
	 * @return the trade profit (value points)
	 */
	private static Deal createDeal(Settlement sellingSettlement, Vehicle delivery,
			Settlement buyingSettlement, Shipment buyLoad, Shipment sellLoad) {

		// Determine estimated mission cost.
		double distance = sellingSettlement.getCoordinates().getDistance(buyingSettlement.getCoordinates()) * 2D;
		double cost = getEstimatedMissionCost(sellingSettlement, delivery, distance);

		return new Deal(buyingSettlement, sellLoad, buyLoad, cost);
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
	public static Shipment getDesiredBuyLoad(Settlement buyingSettlement,
													Vehicle delivery, Settlement sellingSettlement) {
		// Get the credit that the starting settlement has with the destination
		// settlement.
		double credit = CreditManager.getCredit(buyingSettlement, sellingSettlement);

		Shipment desiredBuyLoad = null;
		if (credit > (SELL_CREDIT_LIMIT * -1D)) {
			// Determine desired buy load,
			desiredBuyLoad 	= determineLoad(buyingSettlement, sellingSettlement, delivery,
					Double.POSITIVE_INFINITY);
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
	public static Shipment determineBestSellLoad(Settlement sellingSettlement, Vehicle delivery,
			Settlement buyingSettlement) {
		double credit = CreditManager.getCredit(sellingSettlement, buyingSettlement);

		Shipment sellLoad = null;
		if (credit < SELL_CREDIT_LIMIT) {
			// Determine sell load.
			sellLoad = determineLoad(buyingSettlement, sellingSettlement, delivery,
									 Double.POSITIVE_INFINITY);
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
	private static Shipment determineLoad(Settlement buyingSettlement, Settlement sellingSettlement,
			Vehicle delivery, double maxBuyValue) {

		Map<Good, Integer> tradeList = new HashMap<>();
		double costValue = 0;
		Map<Good,ShoppingItem> buyList = buyingSettlement.getGoodsManager().getBuyList();
		Map<Good,ShoppingItem> sellList = sellingSettlement.getGoodsManager().getSellList();

		double massCapacity = delivery.getCargoCapacity() * 0.8D;

		// Find the matchign Goods being sold and bought
		Set<Good> unionGoods = new HashSet<>(buyList.keySet());
		unionGoods.retainAll(sellList.keySet());
		
		for(Good good : unionGoods) {
			ShoppingItem buy = buyList.get(good);
			ShoppingItem sell = sellList.get(good);
			if (buy.getPrice() <= sell.getPrice()) {
				continue;
			}

			int amountToTrade = Math.min(buy.getQuantity(), sell.getQuantity());

			boolean isAmountResource = good.getCategory() == GoodCategory.AMOUNT_RESOURCE;

			double extraMass = 0D;
			// Calculate the Value of this item to the seller
			if (isAmountResource) {
				AmountResource resource = ResourceUtil.findAmountResource(good.getID());
				Container container = getAvailableContainerForResource(resource,
						sellingSettlement, tradeList);
				if (container != null) {
					Good containerGood = GoodsUtil.getEquipmentGood(container.getEquipmentType());

					// Trade 1 containers worth
					amountToTrade = (int)getContainerCapacity(resource);
					extraMass = container.getBaseMass() + (amountToTrade * good.getMassPerItem());
					
					// Add the new container to the load
					int existingContainers = 0;
					if (tradeList.containsKey(containerGood))
						existingContainers = tradeList.get(containerGood);
					tradeList.put(containerGood, (1 + existingContainers));
				}
				else
					logger.warning(sellingSettlement, "Container for " + resource.getName() + " not available.");
			}
		
			// Add good. mass
			if (good.getCategory() == GoodCategory.VEHICLE) {
				// Lets skip vehicles for now
				continue;
			}

			extraMass += (good.getMassPerItem() * amountToTrade);
			if (extraMass < massCapacity) {
				costValue += buy.getPrice() * amountToTrade;
				massCapacity -= extraMass;
				if (tradeList.containsKey(good)) {
					amountToTrade += tradeList.get(good);
				}
				tradeList.put(good, amountToTrade);
						
				// CHeck capacity
				if ((massCapacity <= 0) || (maxBuyValue <= 0)) {
					break;
				}
			}
		}

		if (tradeList.isEmpty()) {
			return null;
		}
		return new Shipment(tradeList, costValue);
	}

	/**
	 * Determines the credit of a load to a settlement.
	 * 
	 * @param load       a map of the goods and their number.
	 * @param settlement the settlement valuing the load.
	 * @param b
	 * @return credit of the load (items  * production cost).
	 * @throws Exception if error determining the load credit.
	 */
	private static double determineLoadCredit(Map<Good, Integer> load, Settlement settlement, boolean useSellingPrice) {
		GoodsManager manager = settlement.getGoodsManager();
		Map<Good,ShoppingItem> prices = null;
		if (useSellingPrice) {
			prices = manager.getSellList();
		}
		else {
			prices = manager.getBuyList();
		}
		
		double loadCost = 0D;

		for(Entry<Good, Integer> item : load.entrySet()) {
			double itemPrice = 0D;
			if (prices != null) {
				ShoppingItem sItem = prices.get(item.getKey());
				if (sItem != null) {
					itemPrice = sItem.getPrice();
				}
			}
			if (itemPrice == 0) {
				// USe base price
				itemPrice = manager.getPrice(item.getKey());
			}
			
			loadCost += itemPrice * item.getValue();
		}
		return loadCost;
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
		switch(good.getCategory()) {
			case AMOUNT_RESOURCE:
				return settlement.getAmountResourceStored(good.getID());
			
			case ITEM_RESOURCE:
				return settlement.getItemResourceStored(good.getID());
			
			case EQUIPMENT:
			case CONTAINER:
				return settlement.findNumEmptyContainersOfType(((EquipmentGood) good).getEquipmentType(), false);
			
			case VEHICLE:
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
			default:
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

		EquipmentType containerType = ContainerUtil.getEquipmentTypeNeeded(resource.getPhase());

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
		int fuelTypeID = delivery.getFuelTypeID();
		Good fuelGood = null;
		if (fuelTypeID != -1) {
			fuelGood = GoodsUtil.getGood(fuelTypeID);
			neededResources.put(fuelGood, (int) delivery.getFuelNeededForTrip(distance, false));
		}

		if (delivery instanceof Crewable) {
			// Needs a crew
			// Get estimated trip time.
			double averageSpeed = delivery.getBaseSpeed(); // Life support supplies are reloaded on the return trip
			double averageSpeedMillisol = averageSpeed / MarsTime.convertSecondsToMillisols(60D * 60D);
			double tripTimeSols = ((distance / averageSpeedMillisol) + 1000D) / 1000D;

			double lifeSupportMargin = Vehicle.getLifeSupportRangeErrorMargin();
			// Get oxygen amount.
			double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* lifeSupportMargin;
			Good oxygenGood = GoodsUtil.getGood(OXYGEN_ID);
			neededResources.put(oxygenGood, (int) oxygenAmount);

			// Get water amount.
			double waterAmount = PhysicalCondition.getWaterConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* lifeSupportMargin;
			Good waterGood = GoodsUtil.getGood(WATER_ID);
			neededResources.put(waterGood, (int) waterAmount);

			// Get food amount.
			double foodAmount = PhysicalCondition.getFoodConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS
					* lifeSupportMargin;
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
	private static double getContainerCapacity(AmountResource resource) {
		EquipmentType containerType = ContainerUtil.getEquipmentTypeNeeded(resource.getPhase());
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

		Map<Good, Integer> buyLoad = null;
		// Check if buying settlement owes the selling settlement too much for them to
		// sell.
		if (credit > (-1D * SELL_CREDIT_LIMIT)) {

			// Determine the initial buy load based on goods that are profitable for the
			// destination settlement to sell.
			Shipment returnLoad = determineLoad(buyingSettlement, sellingSettlement, delivery, Double.POSITIVE_INFINITY);
			double baseBuyLoadValue = returnLoad.getCostValue();
			double buyLoadValue = baseBuyLoadValue / tradeModifier;
			buyLoad = returnLoad.getLoad();

			// Update the credit value between the starting and destination settlements.
			credit -= buyLoadValue;
			CreditManager.setCredit(buyingSettlement, sellingSettlement, credit);
		}

		return buyLoad;
    }

	public static void initializeInstances(MissionManager m, UnitManager u) {
		missionManager = m;
		unitManager = u;
	}
}