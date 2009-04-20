/**
 * Mars Simulation Project
 * Storage.java
 * @version 2.86 2009-04-20
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;

/**
 * The storage class is a building function for storing resources and units.
 */
public class Storage extends Function implements Serializable {
        
	public static final String NAME = "Storage";
	
	private Map<AmountResource, Double> storageCapacity;
	
	/**
	 * Constructor
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public Storage(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		try {
			BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			Inventory inventory = building.getInventory();	
			
			// Get building resource capacity.
			storageCapacity = config.getStorageCapacities(building.getName());
			Iterator<AmountResource> i1 = storageCapacity.keySet().iterator();
			while (i1.hasNext()) {
                AmountResource resource = i1.next();
				double currentCapacity = inventory.getAmountResourceCapacity(resource);
				double buildingCapacity = ((Double) storageCapacity.get(resource)).doubleValue();
				inventory.addAmountResourceTypeCapacity(resource, currentCapacity + buildingCapacity);
			}
		
			// Get initial resources in building.
			Map<AmountResource, Double> initialResources = config.getInitialStorage(building.getName());
			Iterator<AmountResource> i2 = initialResources.keySet().iterator();
			while (i2.hasNext()) {
                AmountResource resource = i2.next();
				double initialResource = ((Double) initialResources.get(resource)).doubleValue();
				double resourceCapacity = inventory.getAmountResourceRemainingCapacity(resource, true);
				if (initialResource > resourceCapacity) initialResource = resourceCapacity;
				inventory.storeAmountResource(resource, initialResource, true);
			}
		}
		catch (Exception e) {
			throw new BuildingException("Storage.constructor: " + e.getMessage());
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
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) throws Exception {
        
        double result = 0D;
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        
        Map<AmountResource, Double> storageMap = config.getStorageCapacities(buildingName);
        Iterator<AmountResource> i = storageMap.keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            
            double existingStorage = 0D;
            Iterator<Building> j = settlement.getBuildingManager().getBuildings(NAME).iterator();
            while (j.hasNext()) {
                Storage storageFunction = (Storage) j.next().getFunction(NAME);
                if (storageFunction.getResourceStorageCapacity().containsKey(resource))
                    existingStorage += storageFunction.getResourceStorageCapacity().get(resource);
            }
            
            double storageAmount = storageMap.get(resource);
            
            if (!newBuilding) existingStorage -= storageAmount;
            
            Good resourceGood = GoodsUtil.getResourceGood(resource);
            double resourceValue = settlement.getGoodsManager().getGoodValuePerItem(resourceGood);
            double resourceStored = settlement.getInventory().getAmountResourceStored(resource);
            double resourceDemand = resourceValue * (resourceStored + 1D);
            
            double currentStorageDemand = resourceDemand - existingStorage;
            if (currentStorageDemand < 0D) currentStorageDemand = 0D;
            
            double buildingStorageNeeded = currentStorageDemand;
            if (buildingStorageNeeded > storageAmount) buildingStorageNeeded = storageAmount;
            
            result += buildingStorageNeeded / 1000D;
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
	public void timePassing(double time) throws BuildingException {}
	
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
	public double getPowerDownPowerRequired() {
		return 0D;
	}
}