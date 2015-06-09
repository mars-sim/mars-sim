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
            result += person.getPhysicalCondition().getStress() / 2D;

	        // Modify if working out is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Workout")) {
	            result *= 2D;
	        }

	        // 2015-06-07 Added Preference modifier
	        result += person.getPreference().getPreferenceScore(this);
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