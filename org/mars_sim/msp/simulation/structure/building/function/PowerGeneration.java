/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 2.75 2004-03-29
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.structure.building.*;
 
/**
 * The PowerGeneration class is a building function for generating power.
 */
public class PowerGeneration extends Function implements Serializable {
    
    public static final String NAME = "Power Generation";
    
    private List powerSources;
    
    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public PowerGeneration(Building building) throws BuildingException {
    	// Call Function constructor.
    	super(NAME, building);
    	
    	// Determine power sources.
    	BuildingConfig config = building.getBuildingManager().getSettlement()
    		.getMars().getSimulationConfiguration().getBuildingConfiguration();
    		
    	try {
    		powerSources = config.getPowerSources(building.getName());
    	}
    	catch (Exception e) {
    		throw new BuildingException("PowerGeneration.constructor: " + e.getMessage());
    	}
    }
    
    /**
     * Gets the amount of electrical power generated.
     * @return power generated in kW
     */
    public double getGeneratedPower() {
    	double result = 0D;
    	Iterator i = powerSources.iterator();
    	while (i.hasNext()) 
    		result += ((PowerSource) i.next()).getCurrentPower(getBuilding());
    	return result;
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