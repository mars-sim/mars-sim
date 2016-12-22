/**
 * Mars Simulation Project
 * Storage.java
 * @version 3.07 2015-03-07
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

/**
 * The storage class is a building function for storing resources and units.
 */
public class Storage
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    /* default logger.*/
	private static Logger logger = Logger.getLogger(Storage.class.getName());

	private static final BuildingFunction FUNCTION = BuildingFunction.STORAGE;

	private double stockCapacity = 0;
	//private static int count = 0;

	private Map<AmountResource, Double> storageCapacity;

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public Storage(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);

    	//count++;
		//System.out.println("Storage.java : for " + count + " times for " + building );

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		//Inventory inventory = building.getSettlementInventory();
		Inventory inv = building.getBuildingManager().getSettlement().getInventory();

		// 2015-03-07 Added stockCapacity
	    stockCapacity = config.getStockCapacity(building.getBuildingType());
		//System.out.println("Storage.java : stockCapacity is " +stockCapacity);
		// Get building resource capacity.
		storageCapacity = config.getStorageCapacities(building.getBuildingType());
		Iterator<AmountResource> i1 = storageCapacity.keySet().iterator();
		while (i1.hasNext()) {
			AmountResource resource = i1.next();
			//System.out.println("resource : " + resource.getName());
			double currentCapacity = inv.getAmountResourceCapacity(resource, false);
			//System.out.println("currentCapacity is "+currentCapacity);
			double buildingCapacity = storageCapacity.get(resource);
			//System.out.println("buildingCapacity is "+buildingCapacity);
			inv.addAmountResourceTypeCapacity(resource, currentCapacity + buildingCapacity);
		}

		// Get initial resources in building.
		//Inventory inv = building.getBuildingManager().getSettlement().getSettlementInventory();
		Map<AmountResource, Double> initialResources = config.getInitialStorage(building.getBuildingType());
		Iterator<AmountResource> i2 = initialResources.keySet().iterator();
		while (i2.hasNext()) {
			AmountResource resource = i2.next();
			//System.out.println("Storage.java : resource : " + resource.getName());
			double initialResource = initialResources.get(resource);
			//System.out.println("Storage.java : initialResource : " + initialResource);
			double resourceCapacity = inv.getAmountResourceRemainingCapacity(resource, true, false);
			//System.out.println("Storage.java : resourceCapacity is "+resourceCapacity);
			if (initialResource > resourceCapacity) initialResource = resourceCapacity;
			inv.storeAmountResource(resource, initialResource, true);
		}
	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding,
			Settlement settlement) {

		double result = 0D;

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		Map<AmountResource, Double> storageMap = config.getStorageCapacities(buildingName);
		Iterator<AmountResource> i = storageMap.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();

			double existingStorage = 0D;
			Iterator<Building> j = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
			while (j.hasNext()) {
				Building building = j.next();
				Storage storageFunction = (Storage) building.getFunction(FUNCTION);
				double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) *
						.75D + .25D;
				if (storageFunction.storageCapacity.containsKey(resource))
					existingStorage += storageFunction.storageCapacity.get(resource) * wearModifier;
			}

			double storageAmount = storageMap.get(resource);

			if (!newBuilding) {
				existingStorage -= storageAmount;
				if (existingStorage < 0D) existingStorage = 0D;
			}

			Good resourceGood = GoodsUtil.getResourceGood(resource);
			double resourceValue = settlement.getGoodsManager().getGoodValuePerItem(resourceGood);
			double resourceStored = settlement.getInventory().getAmountResourceStored(resource, false);
			double resourceDemand = resourceValue * (resourceStored + 1D);

			double currentStorageDemand = resourceDemand - existingStorage;
			if (currentStorageDemand < 0D) currentStorageDemand = 0D;

			// Determine amount of this building's resource storage is useful to the settlement.
			double buildingStorageNeeded = storageAmount;
			if (currentStorageDemand < storageAmount) {
			    buildingStorageNeeded = currentStorageDemand;
			}
			
			double storageValue = buildingStorageNeeded / 1000D;

			result += storageValue;
		}
		
		return result;
	}

	/**
	 * Gets a map of the resources this building is capable of
	 * storing and their amounts in kg.
	 * @return Map of resource keys and amount Double values.
	 */
	public Map<AmountResource, Double> getResourceStorageCapacity() {
		return storageCapacity;
	}

	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {}

	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0D;
	}

	@Override
	public void removeFromSettlement() {

		// Remove excess amount resources that can no longer be stored.
		Iterator<AmountResource> i = storageCapacity.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			double storageCapacityAmount = storageCapacity.get(resource);
			Inventory inv = getBuilding().getSettlementInventory();
			double totalStorageCapacityAmount = inv.getAmountResourceCapacity(resource, false);
			double remainingStorageCapacityAmount = totalStorageCapacityAmount - storageCapacityAmount;
			double totalStoredAmount = inv.getAmountResourceStored(resource, false);
			if (remainingStorageCapacityAmount < totalStoredAmount) {
				double resourceAmountRemoved = totalStoredAmount - remainingStorageCapacityAmount;
				inv.retrieveAmountResource(resource, resourceAmountRemoved);
			}
		}

		// Remove storage capacity from settlement.
		Iterator<AmountResource> j = storageCapacity.keySet().iterator();
		while (j.hasNext()) {
			AmountResource resource = j.next();
			double storageCapacityAmount = storageCapacity.get(resource);
			Inventory inv = getBuilding().getSettlementInventory();
			inv.removeAmountResourceTypeCapacity(resource, storageCapacityAmount);
		}
	}

	@Override
	public double getMaintenanceTime() {
		return 10D;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

    /**
     * Stores a resource
     * @param amount
     * @param ar
     * @param inv
     */
	// 2015-03-09 Added storeAnResource()
	public static boolean storeAnResource(double amount, AmountResource ar, Inventory inv) {
		boolean result = false;
		try {
			double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, true, false);

			if (remainingCapacity < amount) {
			    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
				amount = remainingCapacity;
				result = false;
			    //logger.info(name + " storage is full!");
			}
			else {
				inv.storeAmountResource(ar, amount, true);
				inv.addAmountSupplyAmount(ar, amount);
				result = true;
			}
		} catch (Exception e) {
    		logger.log(Level.SEVERE, "Issues with storeAnResource(AmountResource ar) on " + ar.getName() + e.getMessage());
		}

		return result;
	}

	
    /**
     * Stores a resource
     * @param name
     * @param Amount
     * @param inv
     */
	// 2015-03-09 Added storeAnResource()
	public static boolean storeAnResource(double amount, String name, Inventory inv) {
		boolean result = false;
		try {
			AmountResource ar = AmountResource.findAmountResource(name);
			double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, true, false);

			if (remainingCapacity < amount) {
			    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
				amount = remainingCapacity;
				result = false; 
			    //logger.info(name + " storage is full!");
			}
			else {
				inv.storeAmountResource(ar, amount, true);
				inv.addAmountSupplyAmount(ar, amount);
				result = true;
			}
		} catch (Exception e) {
    		logger.log(Level.SEVERE, "Issues with storeAnResource(String name) on " + name + e.getMessage());
		}

		return result;
	}


    /**
     * Retrieves a resource or test if a resource is available
     * @param name
     * @param requestedAmount
     * @param inv
     * @param isRetrieving
     * @return true if the full amount can be retrieved.
     */
    //2016-12-03 Added retrieveAnResource()
    public static boolean retrieveAnResource(double requestedAmount, String name, Inventory inv, boolean isRetrieving ) {
    	boolean result = false;
    	try {
	    	AmountResource nameAR = AmountResource.findAmountResource(name);
	        double amountStored = inv.getAmountResourceStored(nameAR, false);
	    	inv.addAmountDemandTotalRequest(nameAR);
	    	
	    	if (Math.round(amountStored * 100000.0 ) / 100000.0 < 0.00001) {
	     		// TODO: how to report it only 3 times and quit the reporting ?
	    		//logger.warning("No more " + name);
	    		result = false;
	    	}
	    	else if (amountStored < requestedAmount) {
	     		//requestedAmount = amountStored;
	     		// TODO: how to report it only 3 times and quit the reporting ?
	    		//logger.warning("Just ran out of " + name);
	    		result = false; // not enough for the requested amount
	    	}
	    	else {
	    		if (isRetrieving) {
		    		inv.retrieveAmountResource(nameAR, requestedAmount);
		    		inv.addAmountDemand(nameAR, requestedAmount);
	    		}
	    		result = true;
	    	}
	    }  catch (Exception e) {
    		logger.log(Level.SEVERE, "Issues with retrieveAnResource(String name) on " + name + e.getMessage());
	    }

    	return result;
    }


    /**
     * Retrieves a resource or test if a resource is available
     * @param requestedAmount
     * @param ar
     * @param inv
     * @param isRetrieving
     * @return true if the full amount can be retrieved.
     */
    //2016-12-03 Added retrieveAnResource()
    public static boolean retrieveAnResource(double requestedAmount, AmountResource ar, Inventory inv, boolean isRetrieving ) {
    	boolean result = false;
    	try {
	        double amountStored = inv.getAmountResourceStored(ar, false);
	    	inv.addAmountDemandTotalRequest(ar);
	    	
	    	if (Math.round(amountStored * 100000.0 ) / 100000.0 < 0.00001) {
	     		// TODO: how to report it only 3 times and quit the reporting ?
	    		//logger.warning("No more " + name);
	    		result = false; // not enough for the requested amount
	    	}
	    	else if (amountStored < requestedAmount) {
	     		//requestedAmount = amountStored;
	     		// TODO: how to report it only 3 times and quit the reporting ?
	    		//logger.warning("Just ran out of " + name);
	    		result = false;
	    	}
	    	else {
	    		if (isRetrieving) {
		    		inv.retrieveAmountResource(ar, requestedAmount);
		    		inv.addAmountDemand(ar, requestedAmount);
	    		}
	    		result = true;
	    	}
	    }  catch (Exception e) {
			e.printStackTrace(System.err);
    		logger.log(Level.SEVERE, "Issues with retrieveAnResource(AmountResource ar) on " + ar.getName() + " : " + e.getMessage());
	    }

    	return result;
    }
    
	@Override
	public void destroy() {
		super.destroy();

		storageCapacity.clear();
		storageCapacity = null;
	}
}