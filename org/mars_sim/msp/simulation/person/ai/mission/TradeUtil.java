/**
 * Mars Simulation Project
 * TradeUtil.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.Barrel;
import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.equipment.EquipmentFactory;
import org.mars_sim.msp.simulation.equipment.GasCanister;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Phase;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.goods.CreditManager;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsManager;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/**
 * Utility class for static trade methods.
 */
public final class TradeUtil {
    
	private static String CLASS_NAME = 
		"org.mars_sim.msp.simulation.person.ai.mission.TradeUtil";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Performance cache for equipment goods.
	private final static Map <Class, Equipment> equipmentGoodCache = new HashMap<Class, Equipment>(5);
	
	// Cache for the best trade settlement.
	static Settlement bestTradeSettlementCache = null;
	
	// Cache for container types.
	private final static Map <Class, Equipment> containerTypeCache = new HashMap<Class, Equipment>(3);
	
	/**
	 * Private constructor for utility class.
	 */
	private TradeUtil() {};
    
	/**
	 * Gets the best trade value for a given settlement.
	 * @param startingSettlement the settlement to trade from.
	 * @param rover the rover to carry the trade.
	 * @return the best value (value points) for trade.
	 * @throws Exception if error while getting best trade profit.
	 */
    static double getBestTradeProfit(Settlement startingSettlement, Rover rover) throws Exception {
    	double bestProfit = 0D;
    	Settlement bestSettlement = null;
    	
    	Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
    	while (i.hasNext()) {
    		Settlement settlement = i.next();
    		if (settlement != startingSettlement) {
    			double settlementRange = settlement.getCoordinates().getDistance(startingSettlement.getCoordinates());
    			if (settlementRange <= rover.getRange()) {
    				// double startTime = System.currentTimeMillis();
    				double profit = getEstimatedTradeProfit(startingSettlement, rover, settlement);
    				// double endTime = System.currentTimeMillis();
    				// logger.info("getEstimatedTradeProfit " + (endTime - startTime));
    				if (profit > bestProfit) {
    					bestProfit = profit;
    					bestSettlement = settlement;
    				}
    			}
    		}
    	}
    	
    	// Set settlement cache.
    	bestTradeSettlementCache = bestSettlement;
    	
    	return bestProfit;
    }
    
    /**
     * Gets the estimated trade profit from one settlement to another.
     * @param startingSettlement the settlement to trade from.
     * @param rover the rover to carry the trade goods.
     * @param tradingSettlement the settlement to trade to.
     * @return the trade profit (value points)
     * @throws Exception if error getting the estimated trade profit.
     */
    private static double getEstimatedTradeProfit(Settlement startingSettlement, Rover rover, 
    		Settlement tradingSettlement) throws Exception {
    	
    	// Determine estimated trade revenue.
    	double revenue = getEstimatedTradeRevenue(startingSettlement, rover, tradingSettlement);
    	
    	// Determine estimated mission cost.
    	double distance = startingSettlement.getCoordinates().getDistance(tradingSettlement.getCoordinates()) * 2D;
    	double cost = getEstimatedMissionCost(startingSettlement, rover, distance);
    	
    	return revenue - cost;
    }
    
    /**
     * Gets the desired buy load from a trading settlement.
     * @param startingSettlement the settlement that is buying.
     * @param rover the rover used for trade.
     * @param tradingSettlement the settlement to buy from.
     * @return the desired buy load.
     * @throws Exception if error determining the buy load.
     */
    static Map<Good, Integer> getDesiredBuyLoad(Settlement startingSettlement, Rover rover, 
    		Settlement tradingSettlement) throws Exception {
    	
    	// Determine best trade loads.
    	Map<Good, Integer> sellLoad = determineLoad(tradingSettlement, startingSettlement, rover, Double.MAX_VALUE);
    	double sellValue = determineLoadValue(sellLoad, tradingSettlement, true);
    	
    	// Add in credit between settlements.
    	CreditManager creditManager = Simulation.instance().getCreditManager();
    	double credit = creditManager.getCredit(startingSettlement, tradingSettlement);
    	double valueLimit = sellValue + credit;
    	
    	// Determine desired buy load.
    	return determineLoad(startingSettlement, tradingSettlement, rover, valueLimit);
    }
    
    /**
     * Gets the estimated trade revenue from one settlement to another.
     * @param startingSettlement the settlement to trade from.
     * @param rover the rover to carry the trade goods.
     * @param tradingSettlement the settlement to trade to.
     * @return the trade revenue (value points).
     * @throws Exception if error getting the estimated trade revenue.
     */
    private static double getEstimatedTradeRevenue(Settlement startingSettlement, Rover rover, 
    		Settlement tradingSettlement) throws Exception {
    	
    	// Determine best trade loads.
    	Map<Good, Integer> sellLoad = determineLoad(tradingSettlement, startingSettlement, rover, Double.MAX_VALUE);
    	double sellValue = determineLoadValue(sellLoad, tradingSettlement, true);
    	
    	// Add in credit between settlements.
    	CreditManager creditManager = Simulation.instance().getCreditManager();
    	double credit = creditManager.getCredit(startingSettlement, tradingSettlement);
    	double valueLimit = sellValue + credit;
    	
    	Map<Good, Integer> buyLoad = determineLoad(startingSettlement, tradingSettlement, rover, valueLimit);
    	double buyValue = determineLoadValue(buyLoad, startingSettlement, true);
    	
    	// Balance loads to least trade value.
    	if (buyValue < valueLimit) { 
    		sellLoad = determineLoad(tradingSettlement, startingSettlement, rover, (buyValue - credit));
    		sellValue = determineLoadValue(sellLoad, tradingSettlement, true);
    	}
    	
    	// Determine value of buy and sell loads to starting settlement.
    	double startingSettlementSellValue = determineLoadValue(sellLoad, startingSettlement, false);
    	double startingSettlementBuyValue = determineLoadValue(buyLoad, startingSettlement, true);
    	
    	// Add credit to buy value if credit is owed.
    	if (credit < 0D) startingSettlementBuyValue -= credit;
    	
    	// Revenue is the value of what is bought minus what is sold.
    	return startingSettlementBuyValue - startingSettlementSellValue;
    }
    
    /**
     * Determines the best sell load from a settlement to another.
     * @param startingSettlement the settlement to trade from.
     * @param rover the rover to carry the goods.
     * @param tradingSettlement the settlement to trade to.
     * @return a map of goods and numbers in the load.
     * @throws Exception if error determining best sell load.
     */
    static Map<Good, Integer> determineBestSellLoad(Settlement startingSettlement, Rover rover, 
    		Settlement tradingSettlement) throws Exception {
    	
    	// Determine best initial sell load.
    	Map<Good, Integer> sellLoad = determineLoad(tradingSettlement, startingSettlement, rover, Double.MAX_VALUE);
    	double sellValue = determineLoadValue(sellLoad, tradingSettlement, true);
    	
    	// Determine best buy load.
    	Map<Good, Integer> buyLoad = determineLoad(startingSettlement, tradingSettlement, rover, sellValue);
    	double buyValue = determineLoadValue(buyLoad, startingSettlement, true);
    	
    	// Balance loads to least trade value.
    	if (buyValue < sellValue) sellLoad = determineLoad(tradingSettlement, startingSettlement, rover, buyValue);
    	
    	return sellLoad;
    }
    
    /**
     * Determines the load between a buying settlement and a selling settlement.
     * @param buyingSettlement the settlement buying the goods.
     * @param sellingSettlement the settlement selling the goods.
     * @param rover the rover to carry the goods.
     * @param valueLimit the value limit for determining the load.
     * @return map of goods and their number.
     * @throws Exception if error determining the load.
     */
    public static Map<Good, Integer> determineLoad(Settlement buyingSettlement, Settlement sellingSettlement,
    		Rover rover, double valueLimit) throws Exception {
    	
    	Map<Good, Integer> tradeList = new HashMap<Good, Integer>();
    	boolean hasRover = false;
    	GoodsManager manager = buyingSettlement.getGoodsManager();
    	
    	// Get rover inventory clone.
    	Inventory inventory = new Inventory(null);
    	double massCapacity = rover.getInventory().getGeneralCapacity();
    	
    	// Subtract mission parts mass (estimated).
    	massCapacity -= 300D;
    	if (massCapacity < 0D) massCapacity = 0D;
    	inventory.addGeneralCapacity(massCapacity);
    	
    	// Determine the load.
    	boolean done = false;
    	double loadValue = 0D;
    	Good previousGood = null;
    	while (!done && (valueLimit > loadValue)) {
    		double goodValueLimit = valueLimit - loadValue;
    		Good good = findBestTradeGood(sellingSettlement, buyingSettlement, tradeList, inventory, hasRover, rover, 
    				goodValueLimit, previousGood);
    		if (good != null) {
    			try {
    				boolean isAmountResource = good.getCategory().equals(Good.AMOUNT_RESOURCE);
    				boolean isItemResource = good.getCategory().equals(Good.ITEM_RESOURCE);
    				
    				// Add resource container if needed.
    				if (isAmountResource) {
    					AmountResource resource = (AmountResource) good.getObject();
    					if (inventory.getAmountResourceRemainingCapacity(resource, true) < getResourceTradeAmount(resource)) {
    						Equipment container = getAvailableContainerForResource(resource, sellingSettlement, tradeList);
    						if (container != null) {
    							Good containerGood = GoodsUtil.getEquipmentGood(container.getClass());
    							addToInventory(containerGood, inventory, 1);
    							int containerNum = 0;
    							if (tradeList.containsKey(containerGood)) containerNum = tradeList.get(containerGood).intValue();
    							double containerSupply = manager.getAmountOfGoodForSettlement(containerGood);
    		    	    		double containerMass = GoodsUtil.getGoodMassPerItem(containerGood);
    		    	    		loadValue+= manager.getGoodValuePerItem(good, (containerSupply + (containerNum * containerMass)));
    		    	    		tradeList.put(containerGood, (containerNum + 1));
    						}
    						else logger.warning("container for " + resource.getName() + " not available.");
    					}
    				}
    				
    				int itemResourceNum = 0;
    				if (isItemResource) {
    					itemResourceNum = getNumItemResourcesToTrade(good, sellingSettlement, buyingSettlement, tradeList, 
    							inventory, goodValueLimit);
    				}
    				
    				// Add good.
    				if (good.getCategory().equals(Good.VEHICLE)) hasRover = true;
    				else {
    					int number = 1;
    					if (isAmountResource) number = (int) getResourceTradeAmount((AmountResource) good.getObject());
    					else if (isItemResource) number = itemResourceNum;
    					addToInventory(good, inventory, number);
    				}
    				int currentNum = 0;
    				if (tradeList.containsKey(good)) currentNum = tradeList.get(good).intValue();
    				double supply = manager.getAmountOfGoodForSettlement(good);
    	    		double goodMass = GoodsUtil.getGoodMassPerItem(good);
    	    		if (isAmountResource) goodMass *= getResourceTradeAmount((AmountResource) good.getObject());
    	    		if (isItemResource) goodMass *= itemResourceNum;
    	    		double goodValue = manager.getGoodValuePerItem(good, (supply + (currentNum * goodMass)));
    	    		if (isAmountResource) goodValue *= getResourceTradeAmount((AmountResource) good.getObject());
    	    		if (isItemResource) goodValue *= itemResourceNum;
    	    		loadValue += goodValue;
    	    		int newNumber = currentNum + 1;
    	    		if (isAmountResource) newNumber = currentNum + (int) getResourceTradeAmount((AmountResource) good.getObject());
    	    		if (isItemResource) newNumber = currentNum + itemResourceNum;
    	    		tradeList.put(good, newNumber);
    			}
    			catch (Exception e) {
    				done = true;
    			}
    		}
    		else done = true;
    		
    		previousGood = good;
    	}
    	
    	return tradeList;
    }
    
    /**
     * Determines the value of a load to a settlement.
     * @param load a map of the goods and their number.
     * @param settlement the settlement valuing the load.
     * @param buy true if settlement is buying the load, false if selling.
     * @return value of the load (value points).
     * @throws Exception if error determining the load value.
     */
    public static double determineLoadValue(Map<Good, Integer> load, Settlement settlement, boolean buy) 
			throws Exception {

    	double result = 0D;

    	GoodsManager manager = settlement.getGoodsManager();

    	Iterator<Good> i = load.keySet().iterator(); 
    	while (i.hasNext()) {
    		Good good = i.next();
    		int goodNumber = load.get(good).intValue();
    		double supply = manager.getAmountOfGoodForSettlement(good);
    		double goodMass = GoodsUtil.getGoodMassPerItem(good);
    		double multiplier = 1D;
    		if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) {
    			double tradeAmount = getResourceTradeAmount((AmountResource) good.getObject());
    			goodNumber/= (int) tradeAmount;
    			goodMass *= tradeAmount;
    			multiplier = tradeAmount;
    		}
    		
    		for (int x = 0; x < goodNumber; x++) {
    			
    			double supplyAmount = 0D;
    			if (buy) supplyAmount = supply + (x * goodMass);
    			else {
    				supplyAmount = supply - (x * goodMass);
    				if (supplyAmount < 0D) supplyAmount = 0D;
    			}
    			
    			double value = (manager.getGoodValuePerItem(good, supplyAmount) * multiplier);
    			
    			result+= value;
    		}
    	}

		return result;
    }
    
    /**
     * Finds the best trade good for a trade.
     * @param sellingSettlement the settlement selling the good.
     * @param buyingSettlement the settlement buying the good.
     * @param tradedGoods the map of goods traded so far.
     * @param roverInventory the inventory of the rover carrying the goods.
     * @param hasVehicle true if a vehicle is in the trade goods.
     * @param missionRover the rover carrying the goods.
     * @param valueLimit the value limit of the trade.
     * @param previousGood the previous trade good used in the trade.
     * @return best good to trade or null if none found.
     * @throws Exception if error determining best trade good.
     */
    private static Good findBestTradeGood(Settlement sellingSettlement, Settlement buyingSettlement, 
    		Map<Good, Integer> tradedGoods, Inventory roverInventory, boolean hasVehicle, Rover missionRover, 
    		double valueLimit, Good previousGood) throws Exception {
    	
    	Good result = null;
    	
    	// Check previous good first.
    	if (previousGood != null) {
    		double previousGoodValue = getTradeValue(previousGood, sellingSettlement, buyingSettlement, tradedGoods, 
    				roverInventory, hasVehicle, missionRover, valueLimit);
    		if (previousGoodValue > 0D) result = previousGood;
    	}
    	
    	// Check all goods.
    	if (result == null) {
    		double bestValue = 0D;
    		Iterator<Good> i = GoodsUtil.getGoodsList().iterator();
    		while (i.hasNext()) {
    			Good good = i.next();
    			if (!tradedGoods.containsKey(good)) {
    				double tradeValue = getTradeValue(good, sellingSettlement, buyingSettlement, tradedGoods, 
    						roverInventory, hasVehicle, missionRover, valueLimit);
    				if (tradeValue > bestValue) {
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
     * @param itemResourceGood the item resource good.
     * @param sellingSettlement the settlement selling the good.
     * @param buyingSettlement the settlement buying the good.
     * @param tradeList the map of goods traded so far.
     * @param roverInventory the inventory of the rover carrying the goods.
     * @param valueLimit the value limit of the trade.
     * @return number of goods to trade.
     * @throws Exception if error determining number of goods.
     */
    private static int getNumItemResourcesToTrade(Good itemResourceGood, Settlement sellingSettlement, 
    		Settlement buyingSettlement, Map<Good, Integer>tradeList, Inventory roverInventory, 
    		double valueLimit) throws Exception {
    	
    	int result = 0;
    	
    	ItemResource item = (ItemResource) itemResourceGood.getObject();
    	
    	int sellingInventory = sellingSettlement.getInventory().getItemResourceNum(item);
    	int buyingInventory = buyingSettlement.getInventory().getItemResourceNum(item);
    	
    	int numberTraded = 0;
    	if (tradeList.containsKey(itemResourceGood)) numberTraded = tradeList.get(itemResourceGood);
    	
    	int roverLimit = (int) (roverInventory.getRemainingGeneralCapacity() / item.getMassPerItem());
    	
    	int totalTraded = numberTraded;
    	double totalBuyingValue = 0D;
    	boolean limitReached = false;
    	while (!limitReached) {
    		
    		double sellingSupplyAmount = (sellingInventory - totalTraded - 1) * item.getMassPerItem();
    		double sellingValue = sellingSettlement.getGoodsManager().getGoodValuePerItem(itemResourceGood, sellingSupplyAmount);
    		double buyingSupplyAmount = (buyingInventory + totalTraded + 1) * item.getMassPerItem();
    		double buyingValue = buyingSettlement.getGoodsManager().getGoodValuePerItem(itemResourceGood, buyingSupplyAmount);
    		
    		if (buyingValue <= sellingValue) limitReached = true;
    		if (totalBuyingValue + buyingValue > valueLimit) limitReached = true;
    		if (totalTraded + 1 > sellingInventory) limitReached = true;
    		if (totalTraded + 1 > roverLimit) limitReached = true;
    		
    		if (!limitReached) {
    			result++;
    			totalTraded = numberTraded + result;
    			totalBuyingValue += buyingValue;
    		}
    	}
    	
    	// Result shouldn't be zero, but just in case it is.
    	if (result == 0) result = 1;
    	return result;
    }
    
    /**
     * Gets the trade value of a good.
     * @param good the good
     * @param sellingSettlement the settlement selling the good.
     * @param buyingSettlement the settlement buying the good.
     * @param tradedGoods the map of goods traded so far.
     * @param roverInventory the inventory of the rover carrying the goods.
     * @param hasVehicle true if a vehicle is in the trade goods.
     * @param missionRover the rover carrying the goods.
     * @param valueLimit the value limit of the trade.
     * @return trade value of good.
     * @throws Exception if error determining trade value.
     */
    private static double getTradeValue(Good good, Settlement sellingSettlement, Settlement buyingSettlement, 
    		Map<Good, Integer> tradedGoods, Inventory roverInventory, boolean hasVehicle, Rover missionRover, 
    		double valueLimit) throws Exception {
    	
    	double result = 0D;
    	
		double amountTraded = 0D;
		if (tradedGoods.containsKey(good)) amountTraded += tradedGoods.get(good).doubleValue();
		
		double sellingInventory = getNumInInventory(good, sellingSettlement.getInventory());
		double sellingSupplyAmount = GoodsUtil.getGoodMassPerItem(good) * (sellingInventory - amountTraded - 1D);
		double sellingValue = sellingSettlement.getGoodsManager().getGoodValuePerItem(good, sellingSupplyAmount);
		if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) 
			sellingValue*= getResourceTradeAmount((AmountResource) good.getObject());
		
		boolean allTraded = (sellingInventory <= amountTraded); 
		
		double buyingInventory = getNumInInventory(good, buyingSettlement.getInventory());
		double buyingSupplyAmount = GoodsUtil.getGoodMassPerItem(good) * (buyingInventory + amountTraded + 1D);
		double buyingValue = buyingSettlement.getGoodsManager().getGoodValuePerItem(good, buyingSupplyAmount);
		if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) 
			buyingValue*= getResourceTradeAmount((AmountResource) good.getObject());
		
		if ((buyingValue > sellingValue) && !allTraded) {
			// Check if rover inventory has capacity for the good.
			boolean isRoverCapacity = hasCapacityInInventory(good, roverInventory, hasVehicle);

			boolean isContainerAvailable = false;
			if (good.getCategory().equals(Good.AMOUNT_RESOURCE) && !isRoverCapacity) {
				Equipment container = getAvailableContainerForResource((AmountResource) good.getObject(), 
						sellingSettlement, tradedGoods);
				isContainerAvailable = (container != null);
			}
			
			boolean isMissionRover = false;
			if (good.getCategory().equals(Good.VEHICLE)) {
				if (good.getName().toLowerCase().equals(missionRover.getDescription().toLowerCase())) {
					if (sellingInventory == 1D) isMissionRover = true;
				}
			}
			
			boolean inValueLimit = buyingValue < valueLimit;
			
			boolean enoughResourceForContainer = true;
			if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) {
				enoughResourceForContainer = 
					(sellingSupplyAmount >= getResourceTradeAmount((AmountResource) good.getObject()));
			}
	    	
			if ((isRoverCapacity || isContainerAvailable) && !isMissionRover && inValueLimit && 
					enoughResourceForContainer) {
				result = buyingValue - sellingValue;
			}
		}
		
		return result;
    }
    
    /**
     * Checks if capacity in inventory for good.
     * @param good the good to check for.
     * @param inventory the inventory to check.
     * @param hasVehicle true if good load already includes a vehicle.
     * @return true if capacity for good.
     * @throws Exception if error checking for capacity.
     */
    private static boolean hasCapacityInInventory(Good good, Inventory inventory, boolean hasVehicle) throws Exception {
    	boolean result = false;
    	if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) {
    		AmountResource resource = (AmountResource) good.getObject();
    		result = (inventory.getAmountResourceRemainingCapacity(resource, true) >= getResourceTradeAmount(resource));
    	}
    	else if (good.getCategory().equals(Good.ITEM_RESOURCE)) 
    		result = inventory.getRemainingGeneralCapacity() >= ((ItemResource) good.getObject()).getMassPerItem();
    	else if (good.getCategory().equals(Good.EQUIPMENT)) {
    		Class type = good.getClassType();
    		if (!equipmentGoodCache.containsKey(type)) 
    			equipmentGoodCache.put(type, EquipmentFactory.getEquipment(type, new Coordinates(0D, 0D), true));
    		result = inventory.canStoreUnit(equipmentGoodCache.get(type));
    	}
    	else if (good.getCategory().equals(Good.VEHICLE)) 
    		result = !hasVehicle;
    	return result;
    }
    
    /**
     * Gets the number of a good currently in the inventory.
     * @param good the good to check.
     * @param inventory the inventory to check.
     * @return number of goods in inventory.
     * @throws Exception if error getting number of goods in inventory.
     */
    public static double getNumInInventory(Good good, Inventory inventory) throws Exception {
    	if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) 
    		return inventory.getAmountResourceStored((AmountResource) good.getObject());
    	else if (good.getCategory().equals(Good.ITEM_RESOURCE)) 
    		return inventory.getItemResourceNum((ItemResource) good.getObject());
    	else if (good.getCategory().equals(Good.EQUIPMENT))
    		return inventory.findNumUnitsOfClass(good.getClassType());
    	else if (good.getCategory().equals(Good.VEHICLE)) {
    		int count = 0;
    		Iterator i = inventory.findAllUnitsOfClass(good.getClassType()).iterator();
    		while (i.hasNext()) {
    			Vehicle vehicle = (Vehicle) i.next();
    			if (vehicle.getDescription().equalsIgnoreCase(good.getName()) && !vehicle.isReserved()) count++;
    		}
    		return count;
    	}
    	else return 0D;
    }
    
    /**
     * Adds a good to a inventory.
     * @param good the good to add.
     * @param inventory the inventory.
     * @param number the number of the good to add.
     * @throws Exception if error adding good to the inventory.
     */
    private static void addToInventory(Good good, Inventory inventory, int number) throws Exception {	
    	if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) {
    		AmountResource resource = (AmountResource) good.getObject();
    		double amount = (double) number;
    		double capacity = inventory.getAmountResourceRemainingCapacity(resource, true);
    		if (amount > capacity) amount = capacity;
    		inventory.storeAmountResource(resource, amount, true);
    	}
    	else if (good.getCategory().equals(Good.ITEM_RESOURCE)) {
    		inventory.storeItemResources((ItemResource) good.getObject(), number);
    	}
    	else if (good.getCategory().equals(Good.EQUIPMENT)) {
    		for (int x = 0; x < number; x++) {
    			inventory.storeUnit(EquipmentFactory.getEquipment(good.getClassType(), new Coordinates(0D, 0D), false));
    		}
    	}
    }
    
    /**
     * Gets an available container for a given resource.
     * @param resource the resource to check.
     * @param settlement the settlement to check for containers.
     * @param tradedGoods the list of goods traded so far.
     * @return container for the resource or null if none.
     * @throws Exception if error.
     */
    private static Equipment getAvailableContainerForResource(AmountResource resource, Settlement settlement, 
    		Map<Good, Integer> tradedGoods) throws Exception {
    	
    	Equipment result = null;
    	
    	Class containerType = null;
    	
    	if (resource.getPhase().equals(Phase.SOLID)) containerType = Bag.class;
    	else if (resource.getPhase().equals(Phase.LIQUID)) containerType = Barrel.class;
    	else if (resource.getPhase().equals(Phase.GAS)) containerType = GasCanister.class;
    	
    	Inventory settlementInv = settlement.getInventory();
    	
    	int containersStored = settlementInv.findNumEmptyUnitsOfClass(containerType);
    	
    	Good containerGood = GoodsUtil.getEquipmentGood(containerType);
    	int containersTraded = 0;
    	if (tradedGoods.containsKey(containerGood)) containersTraded = tradedGoods.get(containerGood).intValue();
    	
    	if (containersStored > containersTraded) result = (Equipment) settlementInv.findUnitOfClass(containerType);
    	
    	return result;
    }
    
    /**
     * Gets the estimated trade mission cost.
     * @param startingSettlement the settlement starting the trade mission.
     * @param rover the mission rover.
     * @param distance the distance (km) of the mission trip.
     * @return the cost of the mission (value points).
     * @throws Exception if error getting the estimated mission cost.
     */
    public static double getEstimatedMissionCost(Settlement startingSettlement, Rover rover, double distance) 
			throws Exception {
    	Map<Good, Integer> neededResources = new HashMap<Good, Integer>(4);

    	// Get required fuel.
    	Good fuelGood = GoodsUtil.getResourceGood(rover.getFuelType());
    	double efficiency = rover.getFuelEfficiency();
    	neededResources.put(fuelGood, 
    			new Integer((int) VehicleMission.getFuelNeededForTrip(distance, efficiency, true)));

    	// Get estimated trip time.
    	double averageSpeed = rover.getBaseSpeed() / 2D;
    	double averageSpeedMillisol = averageSpeed / MarsClock.convertSecondsToMillisols(60D * 60D);
    	double tripTimeSols = ((distance / averageSpeedMillisol) + 1000D) / 1000D;

    	// Get oxygen amount.
    	double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS * 
				Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    	Good oxygenGood = GoodsUtil.getResourceGood(oxygen);
    	neededResources.put(oxygenGood, new Integer((int) oxygenAmount));

    	// Get water amount.
    	double waterAmount = PhysicalCondition.getWaterConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS * 
				Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	AmountResource water = AmountResource.findAmountResource("water");
    	Good waterGood = GoodsUtil.getResourceGood(water);
    	neededResources.put(waterGood, new Integer((int) waterAmount));

    	// Get food amount.
    	double foodAmount = PhysicalCondition.getFoodConsumptionRate() * tripTimeSols * Trade.MAX_MEMBERS * 
				Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	AmountResource food = AmountResource.findAmountResource("food");
    	Good foodGood = GoodsUtil.getResourceGood(food);
    	neededResources.put(foodGood, new Integer((int) foodAmount));

    	// Get cost of resources.
    	return determineLoadValue(neededResources, startingSettlement, false);
    }
    
    /**
     * Gets the amount of a resource that should be traded based on its standard container capacity.
     * @param resource the amount resource.
     * @return amount (kg) of resource to trade.
     * @throws Exception if error determining container.
     */
    private static double getResourceTradeAmount(AmountResource resource) throws Exception {
    	double result = 0D;
    	
    	Class containerType = null;
    	if (resource.getPhase().equals(Phase.SOLID)) containerType = Bag.class;
    	else if (resource.getPhase().equals(Phase.LIQUID)) containerType = Barrel.class;
    	else if (resource.getPhase().equals(Phase.GAS)) containerType = GasCanister.class;
    	
    	Equipment container = null;
    	if (containerTypeCache.containsKey(containerType)) container = containerTypeCache.get(containerType);
    	else {
    		container = EquipmentFactory.getEquipment(containerType, new Coordinates(0, 0), true);
    		containerTypeCache.put(containerType, container);
    	}
    	
    	result = container.getInventory().getAmountResourceCapacity(resource);
    	
    	return result;
    }
}