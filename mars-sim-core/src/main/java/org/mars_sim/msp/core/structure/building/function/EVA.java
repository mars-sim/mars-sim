/**
 * Mars Simulation Project
 * EVA.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

import java.io.Serializable;
import java.util.Iterator;
 
/**
 * The EVA class is a building function for extra vehicular activity.
 */
public class EVA extends Function implements Serializable {
        
	public static final String NAME = "EVA";
    
    private Airlock airlock;
    
	/**
	 * Constructor
	 * @param building the building this function is for.
	 */
	public EVA(Building building) {
		// Use Function constructor.
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		// Add a building airlock.
		int airlockCapacity = config.getAirlockCapacity(building.getName());
		airlock = new BuildingAirlock(building, airlockCapacity);
	}
	
	/**
	 * Constructor with airlock parameter.
	 * @param building the building this function is for.
	 * @param airlock the building airlock.
	 */
	public EVA(Building building, BuildingAirlock airlock) {
	    // Use Function constructor.
	    super(NAME, building);
	    
	    // Add building airlock
	    this.airlock = airlock;
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
                supply += evaFunction.airlock.getCapacity() * wearModifier;
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
	public void timePassing(double time) {
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
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    airlock = null;
	}
}