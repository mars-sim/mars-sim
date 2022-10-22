/*
 * Mars Simulation Project
 * ConversationMeta.java
 * @date 2022-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Conversation;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for Conversation task.
 */
public class ConversationMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.conversation"); //$NON-NLS-1$
    
    private static final double VALUE = .1;
    private static final int CAP = 1;
    
	public ConversationMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setTrait(TaskTrait.PEOPLE, TaskTrait.RELAXATION);
	}
       
    @Override
    public Task constructInstance(Person person) {
        return new Conversation(person);
    }

    @Override
    public double getProbability(Person person) {
    	double result = 0;
    	
        if (person.isInSettlement()) {
        	 result = checkSettlement(person);
        }
        else if (person.isInVehicle()) {
        	 result = checkVehicle(person);
        }
    	
        if (person.isOutside())
        	result = 0;
        
      	int now = marsClock.getMillisolInt();
        boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);
        if (!isOnShiftNow)
        	result = result * 1.2;
        
        if (result == 0) 
        	return 0;
        
        if (result > 0)
        	result = result + result * person.getPreference().getPreferenceScore(this)/10D;
        
        result *= VALUE;
        
        if (result > CAP) 
        	result = CAP;
        
        return result;
    }
    
    private double checkSettlement(Person person) {

        double result = 0;
        Collection<Person> candidate = null;
   
    	Settlement settlement = person.getAssociatedSettlement();
    
        Set<Person> pool = new HashSet<>();
        
        int num = 0;
        
        // Check if there is a local dining building.
        Building currentBuilding = person.getBuildingLocation();
        
        if (currentBuilding != null && currentBuilding.hasFunction(FunctionType.DINING)) {
        	 // Gets a list of chatty people in the same building
        	candidate = settlement.getChattingPeople(person, true, true, true);
        	
        	pool.addAll(candidate);
            // pool doesn't include this person
            pool.remove(person);
            
            num = pool.size();
            
            if (num > 0) {
                // Note: having people who are already chatting will increase the probability of having this person to join
                double rand = RandomUtil.getRandomDouble(num);
            	// Assume being in a dining hall will increase the base chance of conversation by 5
                result = rand * 2;
            	// modified by his relationship with those already there
            	result *= TaskProbabilityUtil.getRelationshipModifier(person, currentBuilding);
            }
        }

        if (currentBuilding == null || num == 0) {
        	
        	// Check if there is a local dining building.
        	Building diningBuilding = BuildingManager.getAvailableDiningBuilding(person, true);
        	
        	if (diningBuilding != null) {
        		// Gets a list of people 
        		candidate = diningBuilding.getInhabitants();
            		
            	pool.addAll(candidate);
            	 // pool doesn't include this person
                pool.remove(person);
                
                num = pool.size();
                
                if (num > 0) {
            		// Note: having people who are already chatting will increase the probability of having this person to join
	                double rand = RandomUtil.getRandomDouble(num);
	            	// Assume being in a dining hall will increase the base chance of conversation by 5
	                result = rand * 1.5;
            		// modified by his relationship with those already there
            		result *= TaskProbabilityUtil.getRelationshipModifier(person, diningBuilding);
            		
            		return result;
                }
        	}
        	
            // Gets a list of people willing to have conversations  
        	candidate = settlement.getChattingPeople(person, true, true, true);         
                 	      	              
            pool.addAll(candidate); 
            // pool doesn't include this person
            pool.remove(person);
            
            num = pool.size();
        	// check if someone is idling somewhere and ready for a chat 
            if (num > 0) {      
            	// Note: having people who are already chatting will increase the probability of having this person to join
                double rand = RandomUtil.getRandomDouble(num);
                
                result = rand;
            }
            
            if (num == 0) {
	            // get a list of "busy" people
            	candidate = settlement.getChattingPeople(person, false, true, true);  
	            
	        	pool.addAll(candidate);
	            // pool doesn't include this person
	            pool.remove(person);
	            
	            num = pool.size();
	            
	        	// Note: having idling people will somewhat increase the chance of starting conversations,
	        	// though at a low probability 
	        	if (num > 0) {
	
	            	double rand = RandomUtil.getRandomDouble(num);
	            	
	                result = rand;
	        	}
            }
        }
        
        int pop = settlement.getIndoorPeopleCount();
        
        return result / pop * 2;
	}

	private double checkVehicle(Person person) { 
		double result = 0;
		
    	Settlement s = person.getAssociatedSettlement();

    	Vehicle v = (Vehicle) person.getContainerUnit();
    	// get the number of people maintaining or repairing this vehicle
    	Collection<Person> affected = v.getAffectedPeople();       	
        // Collection<Person> crew = ((Rover) v).getCrew();     
        Collection<Person> talking = v.getTalkingPeople();
        // Gets a list of people willing to have conversations  
        Collection<Person> idling = s.getChattingPeople(person, true, false, true);         
        
        List<Person> pool = Stream.of(affected, talking, idling)
            .flatMap(x -> x.stream())
            .collect(Collectors.toList()); 
     
        // pool doesn't include this person
        pool.remove(person);
        
        int num = pool.size();

        //TODO: Get some candidates to switch their tasks to HaveConversation  
        if (num == 0) {
        	return 0;
        }
        // need to have at least two people to have a social conversation
        if (num > 1) {
    		//result = (num + 1)*result;
    		double rand = RandomUtil.getRandomDouble(num);
        	result = rand;
        }
        
        // Check if person is in a moving rover.
        if (Vehicle.inMovingRover(person)) {
        	result += 2D;
        }
        
        return result;
	}
}
