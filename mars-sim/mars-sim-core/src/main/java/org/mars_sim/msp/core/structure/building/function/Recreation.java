/**
 * Mars Simulation Project
 * Recreation.java
 * @version 3.03 2012-06-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

import java.io.Serializable;
import java.util.Iterator;

/**
 * The Recreation class is a building function for recreation.
 */
public class Recreation extends Function implements Serializable {
        
	public static final String NAME = "Recreation";
	
	// Data members
	private int populationSupport;
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 */
	public Recreation(Building building) {
		// Use Function constructor.
		super(NAME, building);
		
		// Populate data members.
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		populationSupport = config.getRecreationPopulationSupport(building.getName());
	}
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {
        
        // Settlements need enough recreation buildings to support population.
        double demand = settlement.getAllAssociatedPeople().size();
        
        // Supply based on wear condition of buildings.
        double supply = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (i.hasNext()) {
            Building recreationBuilding = i.next();
            Recreation recreation = (Recreation) recreationBuilding.getFunction(NAME);
            double populationSupport = recreation.getPopulationSupport();
            double wearFactor = ((recreationBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
            supply += populationSupport * wearFactor;
        }
        
        if (!newBuilding) {
            BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
            supply -= config.getRecreationPopulationSupport(buildingName);
            if (supply < 0D) supply = 0D;
        }
        
        return demand / (supply + 1D);
    }
    
    /**
     * Gets the number of people this recreation facility can support.
     * @return population that can be supported.
     */
    public int getPopulationSupport() {
        return populationSupport;
    }
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {}
	
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