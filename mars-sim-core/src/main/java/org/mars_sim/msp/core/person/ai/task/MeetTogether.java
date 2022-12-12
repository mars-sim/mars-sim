/*
 * Mars Simulation Project
 * MeetTogether.java
 * @date 2022-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

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
import org.mars_sim.msp.core.person.ai.social.RelationshipType;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;

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
        super(NAME, person, true, false, 
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
	        	
            Set<Person> pool = new HashSet<>();

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
        addPhase(MEET_TOGETHER);
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
	    	
	    	boolean willMeet = invitee.getMind().getTaskManager().addAPendingTask(MeetTogether.SIMPLE_NAME, false);
	    	if (!willMeet) {
	    		clearTask();
	    		return time * .75;
	    	}
	    }
    	
    	Building personbuilding = null;
    	
    	if (invitee != null && initiator != null) {
			personbuilding = initiator.getBuildingLocation();
			
	    	if (!personbuilding.hasFunction(FunctionType.DINING)) {
	    		Building building = settlement.getBuildingManager()
						.getBuildings(FunctionType.DINING)
						.stream()
						.findAny().orElse(null);	 
			
				if (building != null) {
					walkToActivitySpotInBuilding(building, FunctionType.DINING, false);
				}
	    	}

			// The person is setting up and inviting the candidate
			// e.g. Joe is meeting with Mary
			
			Building inviteebuilding = invitee.getBuildingLocation();
	
			if (inviteebuilding.equals(personbuilding)) {
				setDescription(Msg.getString("Task.description.meetTogether.detail", invitee.getName())); //$NON-NLS-1$
				
				logger.info(initiator, Msg.getString("Task.description.meetTogether.detail", invitee.getName()));
				
				RelationshipUtil.changeOpinion(initiator, invitee, RelationshipType.COMMUNICATION_MEETING, RandomUtil.getRandomDouble(-.1, .15));
				RelationshipUtil.changeOpinion(invitee, initiator, RelationshipType.COMMUNICATION_MEETING, RandomUtil.getRandomDouble(-.1, .15));
		   
				// The person is invited to a meeting setup by the inviter
				// e.g. Joe is invited to meet with Mary
			}
			else {
				// Walk to invitee building first
				walkToActivitySpotInBuilding(inviteebuilding, FunctionType.DINING, false);
			}
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
 
        Set<Person> pool = new HashSet<>();
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
