/**
 * Mars Simulation Project
 * ResourceProcessing.java
 * @version 2.85 2008-08-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * The ResourceProcessing class is a building function indicating 
 * that the building has a set of resource processes.
 */
public class ResourceProcessing extends Function implements Serializable {
        
	public static final String NAME = "Resource Processing";
        
	private double powerDownProcessingLevel;
    private List<ResourceProcess> resourceProcesses;

	/**
	 * Constructor
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public ResourceProcessing(Building building) throws BuildingException {
		// Use Function constructor
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			
		try {
			powerDownProcessingLevel = config.getResourceProcessingPowerDown(building.getName());
			resourceProcesses = config.getResourceProcesses(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("ResourceProcessing.constructor: " + e.getMessage());
		}
	}
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) {
        // TODO: Implement later as needed.
        return 0D;
    }
	
	/**
	 * Gets the resource processes in this building.
	 * @return list of processes.
	 */
    public List<ResourceProcess> getProcesses() {
    	return resourceProcesses;
    }
    
    /**
     * Gets the power down mode resource processing level.
     * @return proportion of max processing rate (0D - 1D)
     */
    public double getPowerDownResourceProcessingLevel() {
    	return powerDownProcessingLevel;
    }
    
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {
		
		double productionLevel = 0D;
		if (getBuilding().getPowerMode().equals(Building.FULL_POWER)) productionLevel = 1D;
		else if (getBuilding().getPowerMode().equals(Building.POWER_DOWN)) 
			productionLevel = powerDownProcessingLevel;
	
		// Run each resource process.
		Iterator<ResourceProcess> i = resourceProcesses.iterator();
		while (i.hasNext()) {
			try {
				i.next().processResources(time, productionLevel, getBuilding().getInventory());
			}
			catch(Exception e) {
				throw new BuildingException("Error processing resources: " + e.getMessage(), e);
			}
		}
	}
	
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