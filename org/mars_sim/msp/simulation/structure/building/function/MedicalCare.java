/**
 * Mars Simulation Project
 * MedicalCare.java
 * @version 2.75 2003-05-01
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
import org.mars_sim.msp.simulation.person.medical.*;
 
public interface MedicalCare extends Function, MedicalAid {

    /**
     * Get the sick bay of this building.
     * @return sick bay.
     */
    public SickBay getSickBay();
}
