/**
 * Mars Simulation Project
 * StandardMedicalCare.java
 * @version 2.75 2003-05-01
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function.impl;
 
import java.io.Serializable;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
 
/**
 * Standard implementation of the MedicalCare building function.
 */
public class StandardMedicalCare extends MedicalStation implements MedicalCare, Serializable {
    
    private InhabitableBuilding building;
    
    /**
     * Constructor
     *
     * @param building the building this is implemented for.
     * @param numBeds the number of beds in the sick bay.
     * @param treatmentLevel the treatment level of the sick bay.
     */
    public StandardMedicalCare(InhabitableBuilding building, int numBeds, int treatmentLevel) {
        // Use MedicalStation constructor
        super(treatmentLevel, numBeds, 
            building.getBuildingManager().getSettlement().getMars().getMedicalManager());
        
        // Initialize data members
        this.building = building;
    }
    
    /**
     * Add a health problem to the queue of problems awaiting treatment at this
     * medical aid.
     *
     * @param problem The health problem to await treatment.
     * @throws Exception if health problem cannot be treated here.
     */
    public void requestTreatment(HealthProblem problem) throws Exception {
        super.requestTreatment(problem);
        
        // Add person to building if necessary.
        try {
            building.addPerson(problem.getSufferer());
        }
        catch (BuildingException e) {}
    }
    
    /**
     * Starts the treatment of a health problem in the waiting queue.
     *
     * @param problem the health problem to start treating.
     * @throws Exception if treatment cannot be started.
     */
    public void startTreatment(HealthProblem problem) throws Exception {
        super.startTreatment(problem);
        
        // Add person to building if necessary.
        try {
            building.addPerson(problem.getSufferer());
        }
        catch (BuildingException e) {}
    }
}
