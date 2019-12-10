/**
 * Mars Simulation Project
 * HaveConversationMeta.java
 * @version 3.1.0 2017-10-23
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.HaveConversation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for HaveConversation task.
 */
public class HaveConversationMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(HaveConversationMeta.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.haveConversation"); //$NON-NLS-1$
    
    private static final double VALUE = 0.1;
    
    private static MarsClock marsClock;
    
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

        double result = 0;
        // TODO: Probability affected by the person's stress and fatigue.

        if (person.isInSettlement()) {
       
        	Settlement settlement = person.getSettlement();
        
            Set<Person> pool = new HashSet<Person>();

            // Gets a list of people willing to have conversations  
            Collection<Person> p_talking_all = settlement.getChattingPeople(person, false, false, true);         
                 	      	              
            pool.addAll(p_talking_all); 
            
            // pool doesn't include this person
            pool.remove(person);
            
            int num = pool.size();
        	// check if someone is idling somewhere and ready for a chat 
            if (num > 0) {      
            	// Note: having people who are already chatting will increase the probability of having this person to join
                double rand = RandomUtil.getRandomDouble(num) + 1;
                result = rand * VALUE * 2;
            }
            else {
            	result = 0; // if there is no one else in the settlement, set result to 0
//            	return 0;
            }
            // get a list of "idle" people
            Collection<Person> p_idle_all = settlement.getChattingPeople(person, true, false, true);  
            
            pool.clear();
        	pool.addAll(p_idle_all);
        	         
        	// Note: having idling people will somewhat increase the chance of starting conversations,
        	// though at a low probability 
        	if (pool.size() > 0) {
                num = pool.size();

                if (num == 1) {
            		double rand = RandomUtil.getRandomDouble(.3);
                	result = result + rand * VALUE;
                }
                else {
            		double rand = RandomUtil.getRandomDouble(.3*(num+1));
                	result = result + rand * VALUE;
                }
        	}

        	if (result > 0) {
	            // Check if there is a local dining building.
	            Building diningBuilding = EatDrink.getAvailableDiningBuilding(person, true);
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

        else if (person.isInVehicle()) {
        	
        	Settlement s = person.getAssociatedSettlement();
        	Set<Person> pool = new HashSet<Person>();
        	
        	Vehicle v = (Vehicle) person.getContainerUnit();
        	// get the number of people maintaining or repairing this vehicle
        	Collection<Person> affected = v.getAffectedPeople();       	
            //Collection<Person> crew = ((Rover) v).getCrew();     
            Collection<Person> talking = v.getTalkingPeople();
            // Gets a list of people willing to have conversations  
            Collection<Person> p_talking_all = s.getChattingPeople(person, false, false, true);         
            
            pool.addAll(affected);
            pool.addAll(p_talking_all);
            //candidates.addAll(crew);          
            pool.addAll(talking);        
            int num = pool.size();
            //System.out.println("talking folks : " + talking);
            //TODO: get some candidates to switch their tasks to HaveConversation       
            // need to have at least two people to have a social conversation
            if (num == 1) {
        		double rand = RandomUtil.getRandomDouble(1);
            	result = result + rand * VALUE;
            }
            else  if (num > 1) {
        		//result = (num + 1)*result;
        		double rand = RandomUtil.getRandomDouble(num)+ 1;
            	result = result + rand * VALUE;
            }
            
	        // Check if person is in a moving rover.
	        if (Vehicle.inMovingRover(person)) {
	        	result += 10D;
	        }
        }

        // Effort-driven task modifier.
//        result *= person.getPerformanceRating();

        if (marsClock == null)
        	marsClock = Simulation.instance().getMasterClock().getMarsClock();
    	int now = marsClock.getMillisolInt();
        boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);
        if (isOnShiftNow)
        	result = result/100.0;
        
        if (result > 0)
        	result = result + result * person.getPreference().getPreferenceScore(this)/8D;

        if (result < 1) 
        	result = 0;
        
//        if (result > 30)
//        	LogConsolidated.log(Level.INFO, 0, sourceName,
//        			person + " " + Math.round(result*100.0)/100.0);
    
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