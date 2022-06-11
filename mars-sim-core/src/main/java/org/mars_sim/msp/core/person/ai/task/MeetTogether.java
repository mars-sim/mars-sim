/*
 * Mars Simulation Project
 * MeetTogether.java
 * @date 2022-06-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.social.RelationshipType;
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
    private static final SimLogger logger = SimLogger.getLogger(MeetTogether.class.getName());
	
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
        super(NAME, person, true, false, 
        		STRESS_MODIFIER - RandomUtil.getRandomDouble(.2), 
        		null, 100D, 
        		5D + RandomUtil.getRandomDouble(10));

        this.person = person;

		setDescription(Msg.getString("Task.description.meetTogether")); //$NON-NLS-1$

        settlement = person.getSettlement();
        
        if (settlement != null) {
        	
            Set<Person> pool = new HashSet<Person>();

            Collection<Person> ppl = settlement.getAllAssociatedPeople(); 
            RoleType roleType = person.getRole().getType();
            
            if (roleType != null && roleType.isCouncil()) {
            	
                for (Person p : ppl) {
                    RoleType type = p.getRole().getType();

                    if (type != null 
                    	&& roleType.isChief()) {
     
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
            
            else if (roleType != null && roleType.isChief()) {
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

    
    // Note: how to use this version of MeetTogether
    public MeetTogether(Person candidate, Person inviter) {
        // Use Task constructor.
        super(NAME, candidate, true, false, STRESS_MODIFIER - RandomUtil.getRandomDouble(.2), null, 100D,
        	  5D + RandomUtil.getRandomDouble(10));
        
        this.inviter = inviter;
        
		setDescription(Msg.getString("Task.description.meetTogether")); //$NON-NLS-1$

        settlement = candidate.getSettlement();
        
        if (settlement != null) {

            if (relationshipManager == null)
            	relationshipManager = Simulation.instance().getRelationshipManager();
            // Check if existing relationship between candidate and inviter.      
            if (!relationshipManager.hasRelationship(candidate, inviter)) {
                // Create new communication meeting relationship.
            	relationshipManager.createRelationship(candidate, inviter, RelationshipType.COMMUNICATION_MEETING);
            }
            relationshipManager.changeOpinion(candidate, inviter, RandomUtil.getRandomDouble(1));
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
			
				logger.fine(person, Msg.getString("Task.description.meetTogether.detail", candidate.getName()));
	
		        if (getDuration() <= (getTimeCompleted() + time)) {
		
		            if (relationshipManager == null)
		            	relationshipManager = Simulation.instance().getRelationshipManager();
		            // Check if existing relationship between person and candidate.
		            if (!relationshipManager.hasRelationship(person, candidate)) {
		                // Create new communication meeting relationship.
		            	relationshipManager.createRelationship(person, candidate, RelationshipType.COMMUNICATION_MEETING);
		            }
		            relationshipManager.changeOpinion(person, candidate, RandomUtil.getRandomDouble(1));
		        }
		    }
    	}
    	
    	else if (inviter != null) {
    		// The person is invited to a meeting setup by the inviter
    		// e.g. Joe is invited to meet with Mary
    		
    		Building building = inviter.getBuildingLocation();
  			    		
			walkToActivitySpotInBuilding(building, FunctionType.COMMUNICATION, false);

			setDescription(Msg.getString("Task.description.meetTogether.detail.invited", inviter.getName())); //$NON-NLS-1$
			
			logger.fine(person, Msg.getString("Task.description.meetTogether.detail.invited", inviter.getName()));
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
    	RoleType candidateType = RoleUtil.getChiefSpeciality(roleType);
 
        Set<Person> pool = new HashSet<Person>();
        if (candidateType != null) {
	        for (Person p : ppl) {
	        	if (p.getRole().getType() == candidateType) {
	            	if (p.getBuildingLocation() != null)
	            		// if that person is inside the settlement and within a building
	            		pool.add(p);
	        	}
	        }	
        }
    	return pool;
    }
}
