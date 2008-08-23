/**
 * Mars Simulation Project
 * Storage.java
 * @version 2.85 2008-08-23
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
	
	private Map storageCapacity;
	
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
			Iterator i1 = storageCapacity.keySet().iterator();
			while (i1.hasNext()) {
				String resourceName = (String) i1.next();
				AmountResource resource = AmountResource.findAmountResource(resourceName);
				double currentCapacity = inventory.getAmountResourceCapacity(resource);
				double buildingCapacity = ((Double) storageCapacity.get(resourceName)).doubleValue();
				inventory.addAmountResourceTypeCapacity(resource, currentCapacity + buildingCapacity);
			}
		
			// Get initial resources in building.
			Map initialResources = config.getInitialStorage(building.getName());
			Iterator i2 = initialResources.keySet().iterator();
			while (i2.hasNext()) {
				String resourceName = (String) i2.next();
				AmountResource resource = AmountResource.findAmountResource(resourceName);
				double initialResource = ((Double) initialResources.get(resourceName)).doubleValue();
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
        
        Map<String, Double> storageMap = config.getStorageCapacities(buildingName);
        Iterator<String> i = storageMap.keySet().iterator();
        while (i.hasNext()) {
            String resourceString = i.next();
            double storageAmount = storageMap.get(resourceString);
            AmountResource resource = AmountResource.findAmountResource(resourceString);
            double existingStorage = settlement.getInventory().getAmountResourceRemainingCapacity(resource, false);
            if (!newBuilding) existingStorage -= storageAmount;
            
            if (storageAmount > existingStorage) {
                Good resourceGood = GoodsUtil.getResourceGood(resource);
                double resourceValue = settlement.getGoodsManager().getGoodValuePerMass(resourceGood);
                result += (storageAmount - existingStorage) * resourceValue;
            }
        }
        
        return result;
    }
	
    /** 
     * Gets a map of the resources this building is capable of
     * storing and their amounts in kg.
     * @return Map of resource keys and amount Double values.
     */
    public Map getResourceStorageCapacity() {
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