/**
 * Mars Simulation Project
 * Communication.java
 * @version 2.85 2008-08-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;
 
import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.*;
 
/**
 * The Communication class is a building function for communication.
 */
public class Communication extends Function implements Serializable {
        
	public static final String NAME = "Communication";
    
    /**
     * Constructor
     * @param building the building this function is for.
     */
    public Communication(Building building) {
    	// Use Function constructor.
    	super(NAME, building);
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
        
        // Settlements need one communication building.
        // Note: Might want to update this when we do more with simulating communication.
        double demand = 1D;
        double supply = settlement.getBuildingManager().getBuildings(NAME).size();
        if (!newBuilding) {
            supply -= 1D;
            if (supply < 0D) supply = 0D;
        }
        
        return demand / (supply + 1D);
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