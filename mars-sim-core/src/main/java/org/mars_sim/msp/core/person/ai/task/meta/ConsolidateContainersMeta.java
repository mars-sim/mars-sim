/**
 * Mars Simulation Project
 * ConsolidateContainersMeta.java
 * @version 3.08 2015-05-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;

/**
 * Meta task for the ConsolidateContainers task.
 */
public class ConsolidateContainersMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.consolidateContainers"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ConsolidateContainers(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
        
        if (LocationSituation.IN_SETTLEMENT == person.getLocationSituation() || 
                LocationSituation.IN_VEHICLE == person.getLocationSituation()) {
        
            // Check if there are local containers that need resource consolidation.
            if (ConsolidateContainers.needResourceConsolidation(person)) {
                result = 10D;
            }
        
            // Effort-driven task modifier.
            result *= person.getPerformanceRating();
            
            // Modify if operations is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Operations")) {
                result *= 2D;
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new ConsolidateContainers(robot);
	}

	@Override
	public double getProbability(Robot robot) {
        
        double result = 0D;
        
        if (robot.getBotMind().getRobotJob() instanceof Deliverybot) 
	        if (LocationSituation.IN_SETTLEMENT == robot.getLocationSituation() || 
	                LocationSituation.IN_VEHICLE == robot.getLocationSituation()) {
	        
	            // Check if there are local containers that need resource consolidation.
	            if (ConsolidateContainers.needResourceConsolidation(robot)) {
	                result = 10D;
	            }
	        
	            // Effort-driven task modifier.
	            result *= robot.getPerformanceRating();
	        }

        return result;
	}
}