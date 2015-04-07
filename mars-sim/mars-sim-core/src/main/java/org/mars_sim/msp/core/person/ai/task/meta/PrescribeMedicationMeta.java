/**
 * Mars Simulation Project
 * PrescribeMedicationMeta.java
 * @version 3.07 2015-02-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PrescribeMedication;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Medicbot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;

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

    public Task constructInstance(Robot robot) {
        return new PrescribeMedication(robot);
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

    public double getProbability(Robot robot) {
        
        double result = 0D;
        
        if (robot.getLocationSituation() != LocationSituation.OUTSIDE) {
        	
	        // Only medicbot or a doctor is allowed to perform this task.
	        if (robot.getBotMind().getRobotJob() instanceof Medicbot) {
	            
	            // Determine patient needing medication.
	            Person patient = PrescribeMedication.determinePatient(robot);
	            if (patient != null) {
	                result = 100D;
	            }
	        }
	        
	        // Effort-driven task modifier.
	        result *= robot.getPerformanceRating();
	
	    }
        
        return result;
    }


}