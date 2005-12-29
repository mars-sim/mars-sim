/**
 * Mars Simulation Project
 * ResourceProcessing.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * The ResourceProcessing class is a building function indicating 
 * that the building has a set of resource processes.
 */
public class ResourceProcessing extends Function implements Serializable {
        
	public static final String NAME = "Resource Processing";
        
	private double powerDownProcessingLevel;
    private List resourceProcesses;

	/**
	 * Constructor
	 * @param building the building the function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public ResourceProcessing(Building building) throws BuildingException {
		// Use Function constructor
		super(NAME, building);
		
		SimulationConfig simConfig = Simulation.instance().getSimConfig();
		BuildingConfig config = simConfig.getBuildingConfiguration();
			
		try {
			powerDownProcessingLevel = config.getResourceProcessingPowerDown(building.getName());
			resourceProcesses = config.getResourceProcesses(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("ResourceProcessing.constructor: " + e.getMessage());
		}
	}
	
	/**
	 * Gets the resource processes in this building.
	 * @return list of processes.
	 */
    public List getProcesses() {
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
		Iterator i = resourceProcesses.iterator();
		while (i.hasNext()) {
			ResourceProcess process = (ResourceProcess) i.next();
			try {
				process.processResources(time, productionLevel, getBuilding().getInventory());
			}
			catch(Exception e) {
				throw new BuildingException("Error processing resources: " + e.getMessage());
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