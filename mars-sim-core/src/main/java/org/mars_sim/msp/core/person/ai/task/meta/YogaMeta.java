/**
 * Mars Simulation Project
 * RelaxMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Yoga;

/**
 * Meta task for the Yoga task.
 */
public class YogaMeta implements MetaTask {
    
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

        // Stress modifier
        result += person.getPhysicalCondition().getStress() / 2D;
        
        // No yoga outside.
        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
            result = 0D;
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