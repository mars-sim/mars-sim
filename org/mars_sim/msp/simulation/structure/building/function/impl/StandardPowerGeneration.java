/**
 * Mars Simulation Project
 * StandardPowerGeneration.java
 * @version 2.75 2003-04-16
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function.impl;
 
import java.io.Serializable;

import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.function.PowerGeneration;
 
/**
 * Standard implementation of the PowerGeneration building function.
 */
public class StandardPowerGeneration implements PowerGeneration, Serializable {
    
    private Building building;
    private double powerGeneratable;
    
    /**
     * Constructor
     *
     * @param building the building this is implemented for.
     * @param powerGeneration baseline power generation.
     */
    public StandardPowerGeneration(Building building, double powerGeneratable) {
        this.building = building;
        this.powerGeneratable = powerGeneratable;
    }
    
    /**
     * Gets the amount of electrical power generated.
     * @return power generated in kW
     */
    public double getGeneratedPower() {
        return powerGeneratable;
    }
}
