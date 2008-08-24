/**
 * Mars Simulation Project
 * WindPowerSource.java
 * @version 2.85 2008-08-23
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.simulation.structure.building.Building;

/**
 * A wind turbine power source.
 */
public class WindPowerSource extends PowerSource implements Serializable {

    private final static String TYPE = "Wind Power Source";
    
    /**
     * Constructor
     * @param maxPower the maximum generated power.
     */
    public WindPowerSource(double maxPower) {
        // Call PowerSource constructor.
        super(TYPE, maxPower);
    }
    
    @Override
    public double getCurrentPower(Building building) {
        // TODO: Make power generated to be based on current wind speed at location.
        return getMaxPower();
    }
}