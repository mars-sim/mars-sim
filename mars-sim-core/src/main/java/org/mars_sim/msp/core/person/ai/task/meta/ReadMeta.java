/**
 * Mars Simulation Project
 * ReadMeta.java
 * @version 3.08 2015-06-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Read task.
 */
public class ReadMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.read"); //$NON-NLS-1$
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Read(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
        	|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

        	result += 5D;

        	if (person.getLocationSituation() == LocationSituation.IN_VEHICLE)  {
        	
		        if (result > 0)
		        	result *= RandomUtil.getRandomDouble(2); // more likely than not if on a vehicle
	        	
        	}
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
			
	        // Effort-driven task modifier.
	        //result *= person.getPerformanceRating();

            // 2015-06-07 Added Preference modifier
            if (result > 0D) {
                result = result + result * person.getPreference().getPreferenceScore(this)/5D;
            }
            
         	if (condition.getFatigue() > 1200D)
         		result = result - 20D;

         	if (condition.getStress() > 75D)
         		result = result - 10D;
         	
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