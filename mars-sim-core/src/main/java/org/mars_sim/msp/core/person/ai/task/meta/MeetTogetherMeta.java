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

        double result = 1D;
        // TODO: Probability affected by the person's stress and fatigue.

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            Set<Person> pool = new HashSet<Person>();
            Settlement s = person.getSettlement();

            // Person initiator, boolean checkIdle, boolean sameBuilding, boolean allSettlements      
            Collection<Person> attendees = null; 
                 	      	              
            pool.addAll(attendees); 
            
            // pool doesn't include this person
            pool.remove((Person)person);
            
            int num = pool.size();
 
        	if (result > 0) {
	            // Check if there is a building big enough to hold the meeting.
        		// Use Lounge by default (max : 33)
	            Building diningBuilding = EatMeal.getAvailableDiningBuilding(person, true);
	            if (diningBuilding != null) {
	            	// Walk to that building.
	            	//pool.addAll(p_same_bldg_talking);
	            	// having a dining hall will increase the base chance of conversation by 1.2 times 
	            	result = result * 1.2;
	            	// modified by his relationship with those already there
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
	            }
        	}
        }

        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
        	Settlement s = person.getAssociatedSettlement();
        	Set<Person> pool = new HashSet<Person>();
        	
        	Vehicle v = (Vehicle) person.getContainerUnit();
        	// get the number of people maintaining or repairing this vehicle
        	Collection<Person> affected = v.getAffectedPeople();       	
             
            pool.addAll(affected);
             int num = pool.size();
            //System.out.println("talking folks : " + talking);
            //TODO: get some candidates to switch their tasks to HaveConversation       
            // need to have at least two people to have a social conversation
            //if (num == 0) {
            	// no one is chatting to yet
            	// but he could the first person to start the chat 
            //}
            if (num == 1) {
        		double rand = RandomUtil.getRandomDouble(1);
            	result = result + rand*result;
            }
            else {
        		//result = (num + 1)*result;
        		double rand = RandomUtil.getRandomDouble(num)+ 1;
            	result = result + rand*result;
            }
        }


        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

    	int now = (int) Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);
        if (isOnShiftNow)
        	result = result/2.0;
        
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