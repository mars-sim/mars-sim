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
import org.mars_sim.msp.simulation.structure.building.InhabitableBuilding;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
 
/**
 * Standard implementation of the MedicalCare building function.
 */
public class StandardMedicalCare implements MedicalCare, Serializable {
    
    private InhabitableBuilding building;
    private SickBay sickbay; // Sickbay of the infirmary
    
    /**
     * Constructor
     *
     * @param building the building this is implemented for.
     * @param numBeds the number of beds in the sick bay.
     * @param treatmentLevel the treatment level of the sick bay.
     */
    public StandardMedicalCare(InhabitableBuilding building, int numBeds, int treatmentLevel) {
        this.building = building;
        
        // Construct sick bay.
        Mars mars = building.getBuildingManager().getSettlement().getMars();
        SickBay sickbay = new SickBay(building.getName(), numBeds, treatmentLevel, mars, building);
    }
    
    /**
     * A person requests to start the specified treatment using this aid. This
     * aid may elect to record that this treatment is being performed. If the
     * treatment can not be satisfied, then a false return value is provided.
     *
     * @param problem the medical problem.
     * @return true if problem can be treated.
     */
    public boolean requestTreatment(HealthProblem problem) {
        return sickbay.requestTreatment(problem);
    }

    /**
     * Stop a previously started treatment.
     *
     * @param problem the medical problem.
     */
    public void stopTreatment(HealthProblem problem) {
        sickbay.stopTreatment(problem);
    }
    
    /**
     * Get the sick bay of this building.
     * @return sick bay.
     */
    public SickBay getSickBay() {
        return sickbay;
    }
}
