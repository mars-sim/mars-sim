/**
 * Mars Simulation Project
 * MedicalAssistanceMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.MedicalAssistance;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the MedicalAssistance task.
 */
public class MedicalAssistanceMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.medicalAssistance"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(MedicalAssistanceMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new MedicalAssistance(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // Get the local medical aids to use.
        if (MedicalAssistance.getNeedyMedicalAids(person).size() > 0) {
            result = 150D;
        }

        // Crowding task modifier.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            try {
                Building building = MedicalAssistance.getMedicalAidBuilding(person);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                } 
                else {
                    result = 0D;
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"MedicalAssistance.getProbability(): " + e.getMessage());
            }
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier if there is a nearby doctor.
        if (MedicalAssistance.isThereADoctorInTheHouse(person)) {
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(MedicalAssistance.class);
            }
        }        

        return result;
    }
}