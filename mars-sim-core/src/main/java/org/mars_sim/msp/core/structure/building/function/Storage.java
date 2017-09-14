/**
 * Mars Simulation Project
 * Storage.java
 * @version 3.1.0 2017-09-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
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
	//private static org.apache.log4j.Logger log4j = LogManager.getLogger(Storage.class);
    private static String sourceName = logger.getName();
    
	private static final FunctionType FUNCTION = FunctionType.STORAGE;
	
	//private static int count = 0;
	private double stockCapacity = 0;

	private static BuildingConfig config;
	
	private Map<AmountResource, Double> resourceCapacities;

	/**
	 * Constructor.
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public Storage(Building building) {
		// Use Function constructor.
		super(FUNCTION, building);
		
        sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());
      
		config = SimulationConfig.instance().getBuildingConfiguration();
		
		//Inventory inventory = building.getSettlementInventory();
		Settlement s = building.getBuildingManager().getSettlement();

		Inventory inv = s.getInventory();

	    stockCapacity = config.getStockCapacity(building.getBuildingType());
   
		// Get capacity for each resource.
		resourceCapacities = config.getStorageCapacities(building.getBuildingType());

		// Initialize resource capacities for this building.
		Iterator<AmountResource> i1 = resourceCapacities.keySet().iterator();
		while (i1.hasNext()) {
			AmountResource ar = i1.next();

			double capacity = resourceCapacities.get(ar);
			// Note : A capacity of a resource in a settlement is the sum of the capacity of the same resource 
			// in all buildings of that settlement
			inv.addAmountResourceTypeCapacity(ar, capacity);
		}

		// Fill up initial resources for this building.
		Map<AmountResource, Double> initialResources = config.getInitialResources(building.getBuildingType());
		Iterator<AmountResource> i2 = initialResources.keySet().iterator();
		while (i2.hasNext()) {
			AmountResource ar = i2.next();

			double initialAmount = initialResources.get(ar);

			double remainingCap = inv.getAmountResourceRemainingCapacity(ar, true, false);

			if (initialAmount > remainingCap)
				initialAmount = remainingCap;
			
			inv.storeAmountResource(ar, initialAmount, true);
		}
/*
		// 2017-05-24 initialize inventory of this building for resource storage 
		Collection<AmountResource> resources = ResourceUtil.getInstance().getAmountResources();
		Iterator<AmountResource> i3 = resources.iterator();
		while (i3.hasNext()) {
			AmountResource ar = i3.next();	
			double remainingCap = inv.getAmountResourceRemainingCapacity(ar, false, false);
			if (remainingCap >= 0)
				inv.storeAmountResource(ar, 0, true);
		}
*/		
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
				if (storageFunction.resourceCapacities.containsKey(resource))
					existingStorage += storageFunction.resourceCapacities.get(resource) * wearModifier;
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
		return resourceCapacities;
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
		Iterator<AmountResource> i = resourceCapacities.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			double storageCapacityAmount = resourceCapacities.get(resource);
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
		Iterator<AmountResource> j = resourceCapacities.keySet().iterator();
		while (j.hasNext()) {
			AmountResource resource = j.next();
			double storageCapacityAmount = resourceCapacities.get(resource);
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
     * @return true if all mounts is being stored properly
     */
	// 2015-03-09 Added storeAnResource()
	public static boolean storeAnResource(double amount, AmountResource ar, Inventory inv) {
		//if (ar == ResourceUtil.greyWaterAR) System.out.println("grey water's amount to be stored: " + amount);
		boolean result = false;
		if (amount > 0) {
			try {
				double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, true, true);
	
				if (remainingCapacity < amount) {
				    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
					amount = remainingCapacity;
					result = false;
					double stored = inv.getAmountResourceStored(ar, false);
				    LogConsolidated.log(logger, Level.SEVERE, 3000, sourceName, "(AR) Can't store all " 
				    	+ Math.round(amount*100.0)/100.0 
				    	+ " kg of '" + ar.getName() 
				    	+ "' in " + inv.getOwner() 
				    	+ " (Remaining capacity : " + Math.round(remainingCapacity*100.0)/100.0
				    	+ "  Stored : " + Math.round(stored*100.0)/100.0
				    	+ ")"
				    	//+ ". Need to allocate more storage space for this resource."
				    	, null);
				}
				else {
					inv.storeAmountResource(ar, amount, true);
					inv.addAmountSupplyAmount(ar, amount);
					result = true;
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
	    		logger.log(Level.SEVERE, "Issues with storeAnResource(ar) on " + ar.getName() + " : " + e.getMessage());
			}
		}
		
		else {
			result = false;
		    LogConsolidated.log(logger, Level.SEVERE, 3000, sourceName, 
		    	"(ar) Attempting to store zero amount of " + ar.getName() + " in " + inv.getOwner() 
		    	, null);
		}
		
		return result;
	}

	/**
     * Stores a resource
     * @param amount
     * @param ar
     * @param inv
     * @return true if all mounts is being stored properly
     */
	// 2015-03-09 Added storeAnResource()
	public static boolean storeAnResource(double amount, AmountResource ar, Inventory inv, String method) {
		boolean result = false;
		if (amount > 0) {
			try {
				double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, true, true);
	
				if (remainingCapacity == 0) {
					LogConsolidated.log(logger, Level.SEVERE, 3000, sourceName + " at " + method, "(AR) Capacity for "
							+ ar.getName() + " is zero or isn't initialized.", null);
					result = false;
				}
				
				else if (remainingCapacity < amount) {
				    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
					amount = remainingCapacity;
					result = false;
					double stored = inv.getAmountResourceStored(ar, false);
				    LogConsolidated.log(logger, Level.SEVERE, 3000, sourceName + " at " + method, "(AR) Can't store all "
					    	+ Math.round(amount*100.0)/100.0 
					    	+ " kg of '" + ar.getName() 
					    	+ "' in " + inv.getOwner() 
					    	+ " (Remaining capacity : " + Math.round(remainingCapacity*100.0)/100.0
					    	+ "  Stored : " + Math.round(stored*100.0)/100.0
					    	+ ")"
				    	//+ ". Need to allocate more storage space for this resource."
				    	, null);			
				    }
				
				else {
					inv.storeAmountResource(ar, amount, true);
					inv.addAmountSupplyAmount(ar, amount);
					result = true;
				}
				
			} catch (Exception e) {
				e.printStackTrace(System.err);
	    		logger.log(Level.SEVERE, "Issues with storeAnResource(ar) on " + ar.getName() + " : " + e.getMessage());
			}
		}
		else {
			result = false;
		    LogConsolidated.log(logger, Level.SEVERE, 3000, sourceName, 
		    	"(ar) Attempting to store zero amount of " + ar.getName() + " in " + inv.getOwner() + " at " + method
		    	, null);
		}

		return result;
	}
    /**
     * Stores a resource
     * @param name
     * @param Amount
     * @param inv
     * @return true if all mounts is being stored properly
     */
	// 2015-03-09 Added storeAnResource()
	public static boolean storeAnResource(double amount, String name, Inventory inv) {
		boolean result = false;
		if (amount > 0) {
			try {
				AmountResource ar = ResourceUtil.findAmountResource(name);
				double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, true, true);
	
				if (remainingCapacity < amount) {
				    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
					amount = remainingCapacity;
					result = false;
					double stored = inv.getAmountResourceStored(ar, false);
				    LogConsolidated.log(logger, Level.SEVERE, 3000, sourceName, "(String) Can't store all "
					    	+ Math.round(amount*100.0)/100.0 
					    	+ " kg of '" + ar.getName() 
					    	+ "' in " + inv.getOwner() 
					    	+ " (Remaining capacity : " + Math.round(remainingCapacity*100.0)/100.0
					    	+ "  Stored : " + Math.round(stored*100.0)/100.0
					    	+ ")"
				    	//+ ". Need to allocate more storage space for this resource."
				    	, null);	
				}
				else {
					inv.storeAmountResource(ar, amount, true);
					inv.addAmountSupplyAmount(ar, amount);
					result = true;
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
	    		logger.log(Level.SEVERE, "Issues with storeAnResource(name) on " + name + " : " + e.getMessage());
			}
		}
		
		else {
			result = false;
		    LogConsolidated.log(logger, Level.SEVERE, 3000, sourceName, 
		    	"(str) Attempting to store zero amount of " + ResourceUtil.findAmountResource(name).getName() 
		    	+ " in " + inv.getOwner() 
		    	, null);
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
	    } catch (Exception e) {
			e.printStackTrace(System.err);
    		logger.log(Level.SEVERE, "Issues with retrieveAnResource(name) on " + name + " : " + e.getMessage());
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
    public static boolean retrieveAnResource(double amount, AmountResource ar, Inventory inv, boolean isRetrieving) {
		//if (ar == ResourceUtil.argonAR) System.out.println("argon's amount to be retrieved: " + amount);
    	boolean result = false;
    	try {
	        double amountStored = inv.getAmountResourceStored(ar, false);
	    	inv.addAmountDemandTotalRequest(ar);

	    	if (Math.round(amountStored * 100000.0 ) / 100000.0 < 0.00001) {
	     		// TODO: how to report it only 3 times and quit the reporting ?
	    		//logger.warning("No more " + name);
	    		result = false; // not enough for the requested amount
	    	}
	    	else if (amountStored < amount) {
	     		//requestedAmount = amountStored;
	     		// TODO: how to report it only 3 times and quit the reporting ?
	    		//logger.warning("Just ran out of " + name);
	    		result = false;
	    	}
	    	else {
	    		if (isRetrieving) {
		    		inv.retrieveAmountResource(ar, amount);
		    		inv.addAmountDemand(ar, amount);
	    		}
	    		result = true;
	    	}
	    } catch (Exception e) {
			e.printStackTrace(System.err);
    		logger.log(Level.SEVERE, "Issues with retrieveAnResource(ar) on " + ar.getName() + " : " + e.getMessage());
	    }

    	return result;
    }

	@Override
	public void destroy() {
		super.destroy();
		config = null;
		resourceCapacities = null;
	}
}