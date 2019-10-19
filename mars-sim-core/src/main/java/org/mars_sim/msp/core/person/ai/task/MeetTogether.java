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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

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

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
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
    public MeetTogether(Person person) {
        // Use Task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER - RandomUtil.getRandomDouble(.2), true, 5D + RandomUtil.getRandomDouble(10));

        this.person = person;
        
        settlement = person.getSettlement();
        
        if (settlement != null) {
        	
            Set<Person> pool = new HashSet<Person>();

            Collection<Person> ppl = settlement.getAllAssociatedPeople(); 
            RoleType roleType = person.getRole().getType();
            
            if (roleType != null && roleType == RoleType.PRESIDENT
                	|| roleType == RoleType.MAYOR
            		|| roleType == RoleType.COMMANDER
            		|| roleType == RoleType.SUB_COMMANDER) {
            	
                for (Person p : ppl) {
                    RoleType type = p.getRole().getType();

                    if (type != null &&  type == RoleType.CHIEF_OF_AGRICULTURE
                    	|| type == RoleType.CHIEF_OF_ENGINEERING
                    	|| type == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
                    	|| type == RoleType.CHIEF_OF_MISSION_PLANNING
                    	|| type == RoleType.CHIEF_OF_SAFETY_N_HEALTH
                    	|| type == RoleType.CHIEF_OF_SCIENCE
                    	|| type == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
     
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
            
            else if (roleType != null && roleType == RoleType.CHIEF_OF_AGRICULTURE
                	|| roleType == RoleType.CHIEF_OF_ENGINEERING
                	|| roleType == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
                	|| roleType == RoleType.CHIEF_OF_MISSION_PLANNING
                	|| roleType == RoleType.CHIEF_OF_SAFETY_N_HEALTH
                	|| roleType == RoleType.CHIEF_OF_SCIENCE
                	|| roleType == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
    	
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

    
    // TODO: how to use this version of MeetTogether
    public MeetTogether(Person candidate, Person inviter) {
        // Use Task constructor.
        super(NAME, candidate, true, false, STRESS_MODIFIER - RandomUtil.getRandomDouble(.2), true, 5D + RandomUtil.getRandomDouble(10));
        
        this.inviter = inviter;
        
        settlement = candidate.getSettlement();
        
        if (settlement != null) {
            // Check if existing relationship between primary researcher and invitee.
            if (relationshipManager == null)
            	relationshipManager = Simulation.instance().getRelationshipManager();
            	
            if (!relationshipManager.hasRelationship(candidate, inviter)) {
                // Add new communication meeting relationship.
                relationshipManager.addRelationship(candidate, inviter, Relationship.COMMUNICATION_MEETING);
            }

            Relationship relationship = relationshipManager.getRelationship(candidate, inviter);
            double currentOpinion = relationship.getPersonOpinion(inviter);
            relationship.setPersonOpinion(inviter, currentOpinion + RandomUtil.getRandomDouble(1));
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

    	// When loading from a saved sim, candidate will be null
    	if (candidate == null)
    		return time;
    	
    	if (inviter == null) {
    		// The person is setting up and inviting the candidate
    		// e.g. Joe is meeting with Mary
    		
       		Building building = settlement.getBuildingManager()
        					.getBuildings(FunctionType.COMMUNICATION)
							.stream()
							.findAny().orElse(null);	        		
	        		
	    	if (building != null) {
	    				
				walkToActivitySpotInBuilding(building, FunctionType.COMMUNICATION, false);

				setDescription(Msg.getString("Task.description.meetTogether.detail", candidate.getName())); //$NON-NLS-1$
			
				LogConsolidated.log(logger, Level.FINER, 5000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " +  Msg.getString("Task.description.meetTogether.detail", candidate.getName()), null);
	
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
    	
    	else if (inviter != null) {
    		// The person is invited to a meeting setup by the inviter
    		// e.g. Joe is invited to meet with Mary
    		
    		Building building = inviter.getBuildingLocation();
  			    		
			walkToActivitySpotInBuilding(building, FunctionType.COMMUNICATION, false);

			setDescription(Msg.getString("Task.description.meetTogether.detail.invited", inviter.getName())); //$NON-NLS-1$
			
			LogConsolidated.log(logger, Level.FINER, 5000, sourceName,
					"[" + inviter.getLocationTag().getLocale() + "] " +  Msg.getString("Task.description.meetTogether.detail.invited", inviter.getName()), null);


    	}
	    	
        return 0D;
    }


    /**
     * Gets a pool of candidates
     * 
     * @param ppl
     * @param roleType
     * @return a set of persons
     */
    public Set<Person> getPool(Collection<Person> ppl, RoleType roleType) {
        Set<Person> pool = new HashSet<Person>();
    	
    	if (roleType == RoleType.CHIEF_OF_AGRICULTURE) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.AGRICULTURE_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType == RoleType.CHIEF_OF_ENGINEERING) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.ENGINEERING_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.LOGISTIC_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }		
    	}
    	else if (roleType == RoleType.CHIEF_OF_MISSION_PLANNING) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.MISSION_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType == RoleType.CHIEF_OF_SAFETY_N_HEALTH) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.SAFETY_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType == RoleType.CHIEF_OF_SCIENCE) {
            for (Person p : ppl) {
            	if (p.getRole().getType() == RoleType.SCIENCE_SPECIALIST) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
            	}
            }	
    	}
    	else if (roleType == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
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