/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 2.75 2003-01-20
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
/**
 * The PowerGeneration class is a building function for generating power.
 */
public interface PowerGeneration extends Function {
        
    /**
     * Gets the amount of electrical power generated.
     * @return power generated in kW
     */
    public double getGeneratedPower();
}
