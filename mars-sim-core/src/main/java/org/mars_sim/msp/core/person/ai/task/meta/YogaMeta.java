/**
 * Mars Simulation Project
 * YogaMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Yoga;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Meta task for the Yoga task.
 */
public class YogaMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.yoga"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Yoga(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // No yoga outside.
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Stress modifier
        	PhysicalCondition condition = person.getPhysicalCondition();
        	// doing yoga is less popular than doing regular workout
            result = condition.getStress() * 1.5D + (condition.getFatigue() / 20D);
            if (result < 0D) {
                result = 0D;
            }
            
            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

	        // 2015-06-07 Added Preference modifier
         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	        if (result < 0) result = 0;

        }

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