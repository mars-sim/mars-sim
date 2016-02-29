/**
 * Mars Simulation Project
 * HaveConversationMeta.java
 * @version 3.08 2015-09-24
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

        double result = 3D;

        // TODO: Probability affected by the person's stress and fatigue.

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check if there is a local dining building.
            Building diningBuilding = EatMeal.getAvailableDiningBuilding(person);
            if (diningBuilding != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, diningBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
            }
            
            Set<Person> pool = new HashSet<Person>();
            Settlement s = person.getSettlement();
            Collection<Person> idle = s.getIdlePeople();           
            Collection<Person> talking = s.getTalkingPeople();
            pool.addAll(idle);   
            pool.addAll(talking);
            // pool doesn't include the one who starts the conversation
            pool.remove((Person)person);
            int num = pool.size();
            List<Person> list = new ArrayList<Person>();
            list.addAll(pool);
/*       	
        	Building b = person.getBuildingLocation();     	
            // Check all people.
            Iterator<Person> i = candidates.iterator();
            while (i.hasNext()) {
                Person p = i.next();
                if (p.getLocationState().getName().equals("Inside a building"))
	                if (p.getBuildingLocation().equals(b))
            }                   
    		double rand = RandomUtil.getRandomDouble(finalCandidates.size());
        	result = result + result*rand;
*/   
            if (num == 0) {
            	// no one to talk to, set result to zero
            	result = 0;
            }
            else if (num == 1) {
        		double rand = RandomUtil.getRandomDouble(2);
            	result = (rand + 1)*result;
            }
            else if (num > 1) {
        		//result = (num + 1)*result;
        		double rand = RandomUtil.getRandomDouble(num+1);
            	result = (rand + 1)*result;
            }
  
        }

        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
        	Set<Person> candidates = new HashSet<Person>();
        	
        	Vehicle v = (Vehicle) person.getContainerUnit();
        	// get the number of people maintaining or repairing this vehicle
        	Collection<Person> affected = v.getAffectedPeople();
        	
            Collection<Person> crew = ((Rover) v).getCrew();           
            Collection<Person> talking = v.getTalkingPeople();
                
            candidates.addAll(affected);
            candidates.addAll(crew);   
            candidates.addAll(talking);        
            candidates.remove((Person)person);
            int num = candidates.size();
            //System.out.println("talking folks : " + talking);
            //TODO: get some candidates to switch their tasks to HaveConversation       
            // need to have at least two people to have a social conversation
            if (num == 0) {
            	// no one to talk to, set result to zero
            	result = 0;
            }
            else if (num == 1) {
        		double rand = RandomUtil.getRandomDouble(2);
            	result = (rand + 1)*result;
            }
            else if (num > 1) {
        		//result = (num + 1)*result;
        		double rand = RandomUtil.getRandomDouble(num+1);
            	result = (rand + 1)*result;
            }
        }

    	int now = (int) Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);
        if (isOnShiftNow)
        	result = result/1.5;
        
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