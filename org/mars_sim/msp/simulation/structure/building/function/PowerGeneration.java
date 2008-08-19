/**
 * Mars Simulation Project
 * PowerGeneration.java
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
 * The PowerGeneration class is a building function for generating power.
 */
public class PowerGeneration extends Function implements Serializable {
    
    public static final String NAME = "Power Generation";
    
    private List<PowerSource> powerSources;
    
    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public PowerGeneration(Building building) throws BuildingException {
    	// Call Function constructor.
    	super(NAME, building);
    	
    	// Determine power sources.
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
    		
    	try {
    		powerSources = config.getPowerSources(building.getName());
    	}
    	catch (Exception e) {
    		throw new BuildingException("PowerGeneration.constructor: " + e.getMessage());
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
	public void timePassing(double time) throws BuildingException {
	    for(PowerSource source : powerSources ) {
		if(source instanceof FuelPowerSource) {
		    FuelPowerSource fuelSource = (FuelPowerSource)source;
		    if(fuelSource.isToggleON()) {
			fuelSource.consumeFuel(time,getBuilding().getInventory());
		    }
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