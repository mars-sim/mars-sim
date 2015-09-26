/**
 * Mars Simulation Project
 * HaveConversationMeta.java
 * @version 3.08 2015-09-24
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
import org.mars_sim.msp.core.person.ai.task.EatMeal;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Workout;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for HaveConversation task.
 */
public class HaveConversationMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.haveConversation"); //$NON-NLS-1$
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new HaveConversation(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Probability affected by the person's stress and fatigue.

    	result = 10D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Gets a seat in a chow hall
            Building building = EatMeal.getAvailableDiningBuilding(person);
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            }

        }

        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

        	Vehicle v = (Vehicle) person.getContainerUnit();

            int people = v.getAffectedPeople().size();

            // need to have at least two people to have a social conversation
            if (people >= 2) {
        		double rand = RandomUtil.getRandomDouble(people);
            	result = result + result*rand;
            }
        }

        if (result > 0)
        	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
        if (result < 0) result = 0;

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