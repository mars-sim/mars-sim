/**
 * Mars Simulation Project
 * EVA.java
 * @version 2.75 2002-11-08
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
import org.mars_sim.msp.simulation.Airlock;
 
public interface EVA extends Function {
        
    /**
     * Gets the building's airlock.
     * @return airlock
     */
    public Airlock getAirlock();
}
