/**
 * Mars Simulation Project
 * EVA.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
 
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
			SimulationConfig simConfig = Simulation.instance().getSimConfig();
			BuildingConfig config = simConfig.getBuildingConfiguration();
			
			// Add a building airlock.
			int airlockCapacity = config.getAirlockCapacity(building.getName());
			airlock = new BuildingAirlock(building, airlockCapacity);
			
			// Add EVA suits.
			int evaSuitNum = config.getEVASuitNumber(building.getName());
			Settlement settlement = building.getBuildingManager().getSettlement();
			for (int x=0; x < evaSuitNum; x++) 
				building.getInventory().addUnit(new EVASuit(settlement.getCoordinates()));
		}
		catch (Exception e) {
			throw new BuildingException("EVA.constructor: " + e.getMessage());
		}
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