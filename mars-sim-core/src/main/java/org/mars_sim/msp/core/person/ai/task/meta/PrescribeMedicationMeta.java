/**
 * Mars Simulation Project
 * PrescribeMedicationMeta.java
 * @version 3.07 2014-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PrescribeMedication;
import org.mars_sim.msp.core.person.ai.task.Task;

/**
 * Meta task for the PrescribeMedication task.
 */
public class PrescribeMedicationMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Prescribing Medication";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PrescribeMedication(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // Only doctor job allowed to perform this task.
        Job job = person.getMind().getJob();
        if (job instanceof Doctor) {
            
            // Determine patient needing medication.
            Person patient = PrescribeMedication.determinePatient(person);
            if (patient != null) {
                result = 100D;
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }
}