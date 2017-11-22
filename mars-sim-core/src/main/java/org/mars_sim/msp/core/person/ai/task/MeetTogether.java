/**
 * Mars Simulation Project
 * MeetTogether.java
 * @version 3.1.0 2017-09-13
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The MeetTogether class is the task of having a casual conversation with another person
 */
public class MeetTogether
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(MeetTogether.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.meetTogether"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase MEET_TOGETHER = new TaskPhase(Msg.getString(
            "Task.phase.meetTogether")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.2D;

    private List<Person> list = new ArrayList<Person>();
    
    private Person candidate;  
    private Person inviter;
    
    private Settlement settlement;
    
    private static RelationshipManager relationshipManager;
    
    /**
     * Constructor. This is an effort-driven task.
     * @param person the person performing the task.
     */
    // 2016-03-01 Added 8 situations for having a conversation
    public MeetTogether(Person person) {
        // Use Task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER - RandomUtil.getRandomDouble(.2), true, 5D + RandomUtil.getRandomDouble(10));

        this.person = person;
        
        settlement = person.getSettlement();
        
        if (settlement != null) {
        	
            Set<Person> pool = new HashSet<Person>();

            Collection<Person> ppl = settlement.getAllAssociatedPeople(); 
            RoleType roleType = person.getRole().getType();
            
            if (roleType.equals(RoleType.PRESIDENT)
                	|| roleType.equals(RoleType.MAYOR)
            		|| roleType.equals(RoleType.COMMANDER)
            		|| roleType.equals(RoleType.SUB_COMMANDER)) {
            	
                for (Person p : ppl) {
                    RoleType type = p.getRole().getType();

                    if (type.equals(RoleType.CHIEF_OF_AGRICULTURE)
                    	|| type.equals(RoleType.CHIEF_OF_ENGINEERING)
                    	|| type.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
                    	|| type.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
                    	|| type.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
                    	|| type.equals(RoleType.CHIEF_OF_SCIENCE)
                    	|| type.equals(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) ) {
     
                    	if (p.getBuildingLocation() != null)
                    		// if that person is inside the settlement and within a building
                    		pool.add(p);

                    }
                }    
                
                Person pp = ppl
						.stream()
						.findAny().orElse(null);	
                
                if (pool.size() == 0)
                	pool.add(pp);
                
            }
            
            else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
                	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
                	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
                	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
                	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
                	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
                	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES)) {
    	
            	pool = getPool(ppl, roleType);
            }
      	
            pool.remove(person);
            
            list.addAll(pool);

            if (list.size() == 0) {
                endTask();
            }
            else {
    	    	int size = list.size();
    	        
    	        if (size == 1)
    	        	candidate = list.get(0); 
    	        else
    	        	candidate = list.get(RandomUtil.getRandomInt(0, size-1));
            }
            
        }
        
        else {
            endTask();
        }

        // Initialize phase
        addPhase(MEET_TOGETHER);
        setPhase(MEET_TOGETHER);
    }

    
    public MeetTogether(Person person, Person inviter) {
        // Use Task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER - RandomUtil.getRandomDouble(.2), true, 5D + RandomUtil.getRandomDouble(10));
        
        this.inviter = inviter;
        
        settlement = person.getSettlement();
        
        if (settlement != null) {
        	
        }
    }
    
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (MEET_TOGETHER.equals(getPhase())) {
            return meetingTogether(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs reading phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double meetingTogether(double time) {

    	if (inviter == null) {
    		// The person is setting up and inviting the candidate
       		Building building = settlement.getBuildingManager()
        					.getBuildings(FunctionType.COMMUNICATION)
							.stream()
							.findAny().orElse(null);	        		
	        		
	    	if (building != null) {
	    				
				walkToActivitySpotInBuilding(building, FunctionType.COMMUNICATION, false);

				setDescription(Msg.getString("Task.description.meetTogether.detail", candidate.getName())); //$NON-NLS-1$
			
		        //if (isDone()) {
		        //    return time;
		        //}
		
		        if (getDuration() <= (getTimeCompleted() + time)) {
		
		            // Check if existing relationship between primary researcher and invitee.
		            if (relationshipManager == null)
		            	relationshipManager = Simulation.instance().getRelationshipManager();
		            	
		            if (!relationshipManager.hasRelationship(person, candidate)) {
		                // Add new communication meeting relationship.
		                relationshipManager.addRelationship(person, candidate, Relationship.COMMUNICATION_MEETING);
		            }
		            // Add 1 point to invitee's opinion of the one who starts the conversation
		            Relationship relationship = relationshipManager.getRelationship(candidate, person);
		            double currentOpinion = relationship.getPersonOpinion(candidate);
		            relationship.setPersonOpinion(candidate, currentOpinion + RandomUtil.getRandomDouble(1));
		 
		        }

		    }
    	}
    	
    	else {
    		// The person is invited to a meeting setup by the inviter
    		
    		Building building = inviter.getBuildingLocation();
  			    		
			walkToActivitySpotInBuilding(building, FunctionType.COMMUNICATION, false);

			setDescription(Msg.getString("Task.description.meetTogether.detail.invited", person.getName())); //$NON-NLS-1$

    	}
	    	
        return 0D;
    }


    public Set<Person> getPool(Collection<Person> ppl, RoleType roleType) {
        Set<Person> pool = new HashSet<Person>();
    	
    	if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.AGRICULTURE_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType.equals(RoleType.CHIEF_OF_ENGINEERING)) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.ENGINEERING_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.LOGISTIC_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }		
    	}
    	else if (roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.MISSION_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.SAFETY_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType.equals(RoleType.CHIEF_OF_SCIENCE)) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.SCIENCE_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType.equals(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES)) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.RESOURCE_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}

    	return pool;
    }
    
    
    @Override
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    @Override
    public void endTask() {
        super.endTask();
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

    }
}