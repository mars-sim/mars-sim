/**
 * Mars Simulation Project
 * MeetTogetherMeta.java
 * @version 3.1.0 2016-11-21
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.EatMeal;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.MeetTogether;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Workout;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for MeetTogether task.
 */
public class MeetTogetherMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.meetTogether"); //$NON-NLS-1$
     
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new MeetTogether(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        // TODO: Probability affected by the person's stress and fatigue.

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

        	RoleType roleType = person.getRole().getType();

            if (roleType.equals(RoleType.PRESIDENT)
                	|| roleType.equals(RoleType.MAYOR)
            		|| roleType.equals(RoleType.COMMANDER) )
            	result += 100D;

            else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
            	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
            	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
            	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
            	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
            	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
            	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) )
            	result += 30D;
 
        }

        //else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
        //}


        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

    	int now = (int) Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);
        if (isOnShiftNow)
        	result = result*1.5D;
        
        if (result > 0)
        	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

        if (result < 0) 
        	result = 0;
        
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