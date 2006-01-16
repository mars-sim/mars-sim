/**
 * Mars Simulation Project
 * Storage.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.building.*;

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
			SimulationConfig simConfig = Simulation.instance().getSimConfig();
			BuildingConfig config = simConfig.getBuildingConfiguration();
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
				inventory.storeAmountResource(resource, initialResource);
			}
		}
		catch (Exception e) {
			throw new BuildingException("Storage.constructor: " + e.getMessage());
		}
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