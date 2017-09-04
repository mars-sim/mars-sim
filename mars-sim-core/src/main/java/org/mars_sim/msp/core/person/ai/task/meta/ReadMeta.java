/**
 * Mars Simulation Project
 * ReadMeta.java
 * @version 3.1.0 2017-02-20
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

        	if (person.getLocationSituation() == LocationSituation.IN_VEHICLE)
        		result *= RandomUtil.getRandomDouble(2); // more likely than not if on a vehicle


	        // Effort-driven task modifier.
	        //result *= person.getPerformanceRating();

            String fav = person.getFavorite().getFavoriteActivity();
            // The 3 favorite activities drive the person to want to read
            if (fav.equalsIgnoreCase("Research")) {
                result *= 2D;
            }
            else if (fav.equalsIgnoreCase("Tinkering")) {
                result *= 1.5D;
            }
            else if (fav.equalsIgnoreCase("Lab Experimentation")) {
                result *= 1.5D;
            }


            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();

         	if (condition.getFatigue() > 1200D)
         		result/=1.5;
         	else if (condition.getFatigue() > 2000D)
         		result/=2D;
         	else if (condition.getFatigue() > 3000D)
         		result/=3D;

         	if (condition.getStress() > 55D)
         		result/=1.5;
         	else if (condition.getStress() > 75D)
         		result/=2D;
         	else if (condition.getStress() > 90D)
         		result/=3D;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}