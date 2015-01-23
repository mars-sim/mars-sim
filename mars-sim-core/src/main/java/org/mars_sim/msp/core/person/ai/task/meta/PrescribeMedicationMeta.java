/**
 * Mars Simulation Project
 * PrescribeMedicationMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PrescribeMedication;
import org.mars_sim.msp.core.person.ai.task.Task;

/**
 * Meta task for the PrescribeMedication task.
 */
public class PrescribeMedicationMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prescribeMedication"); //$NON-NLS-1$
    
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

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}