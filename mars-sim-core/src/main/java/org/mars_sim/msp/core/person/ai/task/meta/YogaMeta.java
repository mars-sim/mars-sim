/**
 * Mars Simulation Project
 * YogaMeta.java
 * @version 3.1.0 2017-09-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
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

        if (!person.getPreference().isTaskDue(this) && person.isInSettlement()) {
	
            double pref = person.getPreference().getPreferenceScore(this);
            
         	result = pref * 5D;
         	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double stress = condition.getStress();
            double fatigue = condition.getFatigue();
            
            if (fatigue > 1000)
            	return 0;
            
            if (pref > 0) {
             	if (stress > 45D)
             		result*=1.5;
             	else if (stress > 65D)
             		result*=2D;
             	else if (stress > 85D)
             		result*=3D;
             	else
             		result*=4D;
            }
            else {
            	return 0;
            }

        	// doing yoga is less popular than doing regular workout
            result = condition.getFatigue() / 20D;
            if (result < 0D) {
                result = 0D;
            }
            
            // Effort-driven task modifier.
            result *= person.getPerformanceRating();


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