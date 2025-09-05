/*
 * Mars Simulation Project
 * MeetTogether.java
 * @date 2022-09-02
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The MeetTogether class is the task of having a casual conversation with another person
 */
public class MeetTogether extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static final SimLogger logger = SimLogger.getLogger(MeetTogether.class.getName());
	
	/** Simple Task name */
	static final String SIMPLE_NAME = MeetTogether.class.getSimpleName();
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.meetTogether"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase MEET_TOGETHER = new TaskPhase(Msg.getString(
            "Task.phase.meetTogether")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.2D;
    
    private transient Person invitee;
    private transient Person initiator;
    
    /** The id of the person choosen by initiator for having a meeting. */
    private int inviteeId = -1;
    /** The id of the person who initiates the meeting. */
    private int initiatorId = -1;
    
    private Settlement settlement;

    
    /**
     * Constructor. This is an effort-driven task.
     * 
     * @param person the person performing the task.
     */
    public MeetTogether(Person person) {
        // Use Task constructor.
        super(NAME, person, false, false, 
        		STRESS_MODIFIER - RandomUtil.getRandomDouble(.2), 
        		null, 100D, 
        		10D + RandomUtil.getRandomDouble(20));

        this.person = person;
        
		setDescription(Msg.getString("Task.description.meetTogether")); //$NON-NLS-1$

		// Check if this person has an meeting initiator
		initiatorId = person.getMeetingInitiator();
		
		// Check if this person is being invited by a initiator for this meeting
		if (initiatorId != -1) {
			// Case 1: This person is the invitee
			inviteeId = person.getIdentifier();
			// Set this person as invitee
			invitee = person;
			// Get the initiator's person instance
			initiator = Simulation.instance().getUnitManager().getPersonByID(initiatorId);
		}
		
		else {
			// Case 2: This person is the initiator
			initiator = person;
			
	        settlement = initiator.getSettlement();
	        
	        if (settlement == null) { 
	            clearTask();
	            return;
	        }
	        	
            Set<Person> pool = new UnitSet<>();

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
						.filter(p -> p.getMeetingInitiator() == -1 && p.getMeetingInvitee() == -1)
						.findAny().orElse(null);	
                
                if (pool.size() == 0)
                	pool.add(pp);
            }
            
            else if (roleType != null && roleType.isChief()) {
            	pool = getPool(ppl, roleType);
            }
      	
            pool.remove(initiator);
            
            List<Person> list = new ArrayList<>();
            
            list.addAll(pool);

            if (list.size() == 0) {
                clearTask();
            }
            else {
    	    	int size = list.size();
    	        
    	        if (size == 1)
    	        	invitee = list.get(0); 
    	        else
    	        	invitee = list.get(RandomUtil.getRandomInt(0, size-1));
            }
	
	        if (invitee != null) {
		        // Set the invitee's inviter's ID
	        	invitee.setMeetingInitiator(initiator.getIdentifier());
	        	// Set the person's invitee's ID
	        	initiator.setMeetingInvitee(invitee.getIdentifier());
	        }
		}
		
        // Initialize phase
        setPhase(MEET_TOGETHER);
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
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double meetingTogether(double time) {

    	// When loading from a saved sim, invitee will be null
    	if (invitee == null) {
    		if (inviteeId == -1)
    			logger.warning(initiator, "inviteeId is -1.");
    		else
    			invitee = Simulation.instance().getUnitManager().getPersonByID(inviteeId);

    	}
    	else {
    		invitee.setMeetingInitiator(-1);
    		clearTask();
    		return time * .75;
    	}
    		
    	// When loading from a saved sim, initiator will be null
    	if (initiator == null) {
    		if (initiatorId == -1)
    			logger.warning(initiator, "initiatorId is -1.");
    		else
    			initiator = Simulation.instance().getUnitManager().getPersonByID(initiatorId);

    	}
    	else {
    		initiator.setMeetingInitiator(-1);
    		clearTask();
    		return time * .75;
    	}
    	
    	if (invitee != null && !(invitee.getMind().getTaskManager().getTask() instanceof MeetTogether)) {
	    	
	    	boolean willMeet = invitee.getMind().getTaskManager().addPendingTask(MeetTogether.SIMPLE_NAME, false, 1, (int)getDuration());
	    	if (!willMeet) {
	    		clearTask();
	    		return time * .75;
	    	}
	    }
		
    	if (invitee != null && initiator != null) {
    	   	// The person is setting up and inviting the candidate
        	// e.g. Joe is meeting with Mary
        			
    		Building personbuilding = initiator.getBuildingLocation();
			Building inviteebuilding = invitee.getBuildingLocation();
	    			
			if (inviteebuilding.equals(personbuilding)) {
				setDescription(Msg.getString("Task.description.meetTogether.detail", invitee.getName())); //$NON-NLS-1$
				
				logger.info(initiator, Msg.getString("Task.description.meetTogether.detail", invitee.getName()));
				
				RelationshipUtil.changeOpinion(initiator, invitee, RelationshipType.REMOTE_COMMUNICATION, RandomUtil.getRandomDouble(-.1, .15));
				RelationshipUtil.changeOpinion(invitee, initiator, RelationshipType.REMOTE_COMMUNICATION, RandomUtil.getRandomDouble(-.1, .15));
		   
				// No need to walk elsewhere since they are already in the same building
			}
    		else {
    			// Walk to invitee building 
				walkToActivitySpotInBuilding(inviteebuilding, FunctionType.DINING, false);
    		}
			
			// FUTURE: how to ask invitee to walk to a diner ?
//	    	if (!personbuilding.hasFunction(FunctionType.DINING)) {
//	    		Building diner = settlement.getBuildingManager()
//						.getBuildingSet(FunctionType.DINING)
//						.stream()
//						.findAny().orElse(null);	 
//			
//				if (diner != null) {
//					walkToActivitySpotInBuilding(diner, FunctionType.DINING, false);
//				}
//	    	}

    	}
		
        if (getTimeCompleted() + time >= getDuration()) {
        	clearTask();
        }

        return 0D;
    }


    private void clearTask() {
    	if (invitee != null)
    		invitee.setMeetingInitiator(-1);
    	if (initiator != null)
    		initiator.setMeetingInvitee(-1);
    	super.endTask();
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
 
        Set<Person> pool = new UnitSet<>();
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
