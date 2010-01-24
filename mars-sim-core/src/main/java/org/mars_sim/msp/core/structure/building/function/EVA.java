/**
 * Mars Simulation Project
 * EVA.java
 * @version 2.90 2010-01-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.*;
 
/**
 * The EVA class is a building function for extra vehicular activity.
 */
public class EVA extends Function implements Serializable {
        
	public static final String NAME = "EVA";
    
    private Airlock airlock;
    
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if function cannot be constructed.
	 */
	public EVA(Building building) throws BuildingException {
		// Use Function constructor.
		super(NAME, building);
		
		try {
			BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			
			// Add a building airlock.
			int airlockCapacity = config.getAirlockCapacity(building.getName());
			airlock = new BuildingAirlock(building, airlockCapacity);
		}
		catch (Exception e) {
			throw new BuildingException("EVA.constructor: " + e.getMessage());
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
        
        // Demand is one airlock capacity for every four inhabitants.
        double demand = settlement.getAllAssociatedPeople().size() / 4D;
        
        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                EVA evaFunction = (EVA) building.getFunction(NAME);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += evaFunction.getAirlock().getCapacity() * wearModifier;
            }
        }
        
        double airlockCapacityValue = demand / (supply + 1D);
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double airlockCapacity = config.getAirlockCapacity(buildingName);
        
        return airlockCapacity * airlockCapacityValue;
    }
        
    /**
     * Gets the building's airlock.
     * @return airlock
     */
    public Airlock getAirlock() {
    	return airlock;
    }
    
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {
		airlock.timePassing(time);
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