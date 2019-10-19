/**
 * Mars Simulation Project
 * ConsolidateContainersMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;

/**
 * Meta task for the ConsolidateContainers task.
 */
public class ConsolidateContainersMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
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

    	// a person can be within a settlement or inside a vehicle
        if (person.isInside()) {
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
            // Check if there are local containers that need resource consolidation.
            if (ConsolidateContainers.needResourceConsolidation(person)) {
                result = 10D;
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Modify if operations is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION) 
                result *= 1.5D;

            // 2015-06-07 Added Preference modifier
            if (result > 0D) {
                result = result + result * person.getPreference().getPreferenceScore(this)/5D;
            }
         
            if (result < 0) result = 0;

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

        if (robot.getBotMind().getRobotJob() instanceof Deliverybot && robot.isInside()) {

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