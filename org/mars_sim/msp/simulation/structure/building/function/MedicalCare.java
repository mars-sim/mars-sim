/**
 * Mars Simulation Project
 * MedicalCare.java
 * @version 2.75 2003-05-07
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function;
 
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.medical.*;
 
/**
 * Interface MedicalCare represents a building that can provide medical care.
 */
public interface MedicalCare extends Function, MedicalAid {

    /**
     * Gets the number of sick beds.
     *
     * @return Sick bed count.
     */
    public int getSickBedNum();
    
    /**
     * Gets the current number of people being treated here.
     *
     * @return Patient count.
     */
    public int getPatientNum();
    
    /**
     * Gets the patients at this medical station.
     * @return Collection of People.
     */
    public PersonCollection getPatients();
}
