/*
 * Mars Simulation Project
 * Converse.java
 * @date 2026-08-07
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The Converse class is the task of having a casual conversation with another person
 */
public class Converse extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(Converse.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.converse"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase CONVERSING = new TaskPhase(Msg.getString(
            "Task.phase.conversing")); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase RESPONDING = new TaskPhase(Msg.getString(
            "Task.phase.responding")); //$NON-NLS-1$

    
    private static final String CHATTING_WITH = 
    		Msg.getString("Task.description.converse.chatting.detail"); //$NON-NLS-1$
    
    private static final String RESPONDING_TO = 
    		Msg.getString("Task.description.converse.responding.detail"); //$NON-NLS-1$
    	
	/** Impact doing this Task */
	private static final ExperienceImpact INSTIGATOR_IMPACT = new ExperienceImpact(50D,
										NaturalAttributeType.CONVERSATION, false, -0.5,
										SkillType.PSYCHOLOGY, SkillType.REPORTING);
    

    private Location inviteeLocation = Location.NONE;

	/** The id of the target person (either the invitee or the initiator). */
	private Integer targetID;

	private Person target;

    private enum Location {
        ANOTHER_BUILDING,
        ANOTHER_SETTLEMENT,
    	EVA,
    	NONE,
        SAME_BUILDING,
    	SAME_DINING,
        SAME_SETTLEMENT,
        SAME_VEHICLE,
    	VEHICLE,
    	SETTLEMENT_VICINITY,
    	VEHICLE_VICINITY;
    	
		@Override
    	public String toString(){
            switch(this) {
                case ANOTHER_BUILDING:
                    return "from another building";
                case ANOTHER_SETTLEMENT:
                    return "in same settlement";
                case EVA:
                    return "while in EVA";
                case NONE:
                    return "";        
                case SAME_BUILDING:
                    return "in same building"; 
                case SAME_DINING:
                    return "in same dining hall";
                case SAME_SETTLEMENT:
                    return "in same settlement";  
                case SAME_VEHICLE:
                    return "in same vehicle";  
                case VEHICLE:
                    return "in a vehicle";  
                case SETTLEMENT_VICINITY:
                    return "in a settlement vicinity"; 
                case VEHICLE_VICINITY:
                    return "in a vehicle vicinity"; 
                default: 
                	return "";
            }
        }
    }

    /**
     * Constructor. This is an effort-driven task.
     * 
     * @param person the person performing the task.
     */
    public Converse(Person person) {
        // Use Task constructor.
        super(NAME, person, true,
        		INSTIGATOR_IMPACT,
        		Math.max(1,
        		 1 + RandomUtil.getRandomDouble(person.getNaturalAttributeManager()
        				 .getAttribute(NaturalAttributeType.CONVERSATION))/20
        		 + RandomUtil.getRandomDouble(person.getPreference()
        				 .getPreferenceScore(MetaTaskUtil.getConverseMeta())/3.0))
        		);
    	
    	findInvitee();
        
        if (getTarget() != null) {
            // Initialize phase
            setPhase(CONVERSING);
    	}
    	else {
            endTask();
        }
    }

    /**
     * Finds an invitee.
     */
    public void findInvitee() {
    	Person p = null;
        if (person.isInSettlement()) {
           	p = selectFromSettlement(0);
        	if (p != null)
        		setTarget(p, true);
        }
        else if (person.isInVehicle()) {
        	p = selectFromVehicle(1);
        	if (p != null)
        		setTarget(p, true);
        }
        else {
        	// Allow a person who are walking on the surface of Mars to have conversation
        	p = selectforEVA(2);
        	if (p != null)
        		setTarget(p, true);
        }
        
        // If no one is available, then end the task
        if (p == null)
        	endTask();
        
    }
    
    /**
     * Constructor 2.
     * 
     * @param invitee the invitee of this conversation
     * @param initiator the initiator of this conversation
     */
    public Converse(Person invitee, Person initiator) {
        // Use Task constructor.
        super(NAME, invitee, true, 
				INSTIGATOR_IMPACT,
        		RandomUtil.getRandomDouble(initiator.getTaskManager().getTask().getTimeLeft())
        		);
    	
    	setTarget(initiator, true);
    	
    	// Initialize phase
        setPhase(RESPONDING);
    }
    
	
	/**
	 * Gets the target person of this task.
	 * 
	 * @return target.
	 */
	public Person getTarget() {
		return target;
	}
	
	/**
	 * Gets the id of target person of this task.
	 * 
	 * @return target id.
	 */
	public Integer getTargetID() {
		return targetID;
	}
	
	/**
	 * Sets the person who's the target of this task.
	 * 
	 * @param newTarget the new target
	 * @param true if id has not been saved
	 */
	public void setTarget(Person newTarget, boolean newID) {
		this.target = newTarget;
		if (newID)
			targetID = target.getIdentifier();
	}
	
    /**
     * Gets a likable person.
     *
     * @param list
     * @return
     */
    public Person getLikablePerson(List<Person> list) {
    	int size = list.size();
    	double bestScore = 0;
    	Person bestFriend = null;

    	// High conversation attribute with psychology skill 
    	// increase the chance of finding more friends to converse
        double variance = (1 + person.getSkillManager()
        		.getSkillLevel(SkillType.PSYCHOLOGY)) * 2.5 
        		+ person.getNaturalAttributeManager()
       				 .getAttribute(NaturalAttributeType.CONVERSATION) / 20D;
	
    	for (int i= 0; i<size; i++) {
    		double score = RelationshipUtil.getOpinionOfPerson(person, list.get(i));
    		score += RandomUtil.getRandomDouble(-variance/2, variance);
    		if (score > bestScore) {
    			bestScore = score;
    			bestFriend = list.get(i);
    		}
    	}
    	return bestFriend;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (CONVERSING.equals(getPhase())) {
            return conversing(time);
        }
        else if (RESPONDING.equals(getPhase())) {
            return responding(time);
        }
        else {
            return time;
        }
    }

    /**
     * Selects a person to chat in settlement.
     * 
     * @return
     */
    public Person selectFromSettlement(int choice) {
    	Person invitee = null;
        Set<Person> pool = new UnitSet<>();
    
        // Gets a list of idle people in the same building
        Collection<Person> candidates = getChattingPeople(person, true, true, true);
        pool.addAll(candidates);
     // remove the one who starts the conversation
        pool.remove(person);
        
    	inviteeLocation = Location.SAME_BUILDING;

        if (pool.isEmpty()) {
        	// Gets a list of busy people in the same building
            candidates = getChattingPeople(person, false, true, true);
        	pool.addAll(candidates);
        }
        
        if (pool.isEmpty()) {
        	// Go to a chatty chow hall
            Building diningBuilding = BuildingManager.getAvailableDiningBuilding(person, true);
            if (diningBuilding != null) {
            	// Walk to that building.
            	walkToActivitySpotInBuilding(diningBuilding, FunctionType.DINING, true);
                // Gets a list of chatty people in the same building
            	candidates = getChattingPeople(person, true, true, true);
            	pool.addAll(candidates);
            	inviteeLocation = Location.SAME_DINING;
            	
                if (pool.isEmpty()) {
                	// Gets a list of busy people in the same dining building
                    candidates = getChattingPeople(person, false, true, true);
                	pool.addAll(candidates);
                }
            }
        }

        if (pool.isEmpty()) {
        	// Gets a list of idle people in different bldg but the same settlement
        	candidates = getChattingPeople(person, true, false, true);
        	pool.addAll(candidates);
        	inviteeLocation = Location.SAME_SETTLEMENT; 
        }
        
        if (pool.isEmpty()) {
        	// Gets a list of busy people in different bldg but the same settlement
            candidates = getChattingPeople(person, false, false, true);
        	pool.addAll(candidates);
        	inviteeLocation = Location.SAME_SETTLEMENT;
        }
                
        if (pool.isEmpty() && choice != 1) {
        	// Gets a list of people from vehicles
        	Person p = selectFromVehicle(0);
        	if (p != null) {
        		candidates.add(p);
        		pool.addAll(candidates);
        	}
        }
        
        if (pool.isEmpty() && choice != 2) {
        	
        	Person p = selectforEVA(0);
        	if (p != null) {
	        	candidates.add(p);
	        	pool.addAll(candidates);
	        	inviteeLocation = Location.EVA;
        	}
        }
        
        if (pool.isEmpty()) {
        	// Gets a list of idle people from other settlements
            candidates = getChattingPeople(person, true, false, false);
        	pool.addAll(candidates);
        	inviteeLocation = Location.ANOTHER_SETTLEMENT;
        }

        if (pool.isEmpty()) {
        	// Gets a list of busy people from other settlements
            candidates = getChattingPeople(person, false, false, false);
        	pool.addAll(candidates);
        	inviteeLocation = Location.ANOTHER_SETTLEMENT;
        }
        
        if (pool.isEmpty()) {
        	inviteeLocation = Location.NONE;
        	logger.info(person, 30_000, "Unable to find anyone to chat with.");
        	
        	endTask();
        	return null;
        }

        int num = pool.size();
        List<Person> list = new ArrayList<>();
        list.addAll(pool);
        if (num == 1) {
        	invitee = list.get(0);
        }
        else if (num > 1) {
        	int rand = RandomUtil.getRandomInt(num-1);

        	// half of the time, talk to the most favorite friend 
        	if (RandomUtil.getRandomInt(1) == 0) {
        		invitee = getLikablePerson(list);
        	}
        	
        	if (invitee == null) {
    			invitee = list.get(RandomUtil.getRandomInt(rand));
    		}
        }

    	return invitee;
    }

    /**
     * Selects a person to chat in vehicle.
     *  
     * @return
     */
    public Person selectFromVehicle(int choice) {
    	Person invitee = null;

        Set<Person> pool = new UnitSet<>();
    
        Collection<Person> candidates = new UnitSet<>();
        if (person.getContainerUnit() instanceof Vehicle cv) {
            candidates = cv.getTalkingPeople();
            pool.addAll(candidates);
            // remove the one who starts the conversation
            pool.remove(person);
        }

        if (!pool.isEmpty()) {
        	inviteeLocation = Location.SAME_VEHICLE;
        }
        else {
            Collection<Vehicle> vv = person.getAssociatedSettlement().getAllAssociatedVehicles();
            if (person.getContainerUnit() instanceof Vehicle v) {
            	vv.remove(v);
            }
            
            for (Vehicle vehicle: vv) {
            	Collection<Person> p = vehicle.getTalkingPeople();
                pool.addAll(p);
                // remove the one who starts the conversation
                pool.remove(person);
            }
            if (!pool.isEmpty()) {
            	inviteeLocation = Location.VEHICLE;
            }
        }

        if (pool.isEmpty() && choice != 0) {
        	
        	Person p = selectFromSettlement(1);
        	if (p != null) {
	        	candidates.add(p);
	        	pool.addAll(candidates);
        	}
        }
        
        if (pool.isEmpty() && choice != 2) {
        	
        	Person p = selectforEVA(1);
        	if (p != null) {
	        	candidates.add(p);
	        	pool.addAll(candidates);
	        	inviteeLocation = Location.EVA;
        	}
        }

        if (pool.isEmpty()) {
        	inviteeLocation = Location.NONE;
        	logger.info(person, 30_000, "Unable to find anyone to chat with in vehicles.");
        	
        	endTask();
        	
        	return null;
        }

        int num = pool.size();
        List<Person> list = new ArrayList<>();
        list.addAll(pool);
        if (num == 1) {
        	invitee = list.get(0);
        }
        else if (num > 1) {
        	int rand = RandomUtil.getRandomInt(num-1);
        	// half of the time, talk to just one person
        	if (RandomUtil.getRandomInt(1) == 0) {
        		invitee = getLikablePerson(list);
        	}
        	
        	if (invitee == null) {
    			invitee = list.get(RandomUtil.getRandomInt(rand));
    		}
        }
        
    	return invitee;
    }

    /**
     * Selects a person to chat during EVA.
     * 
     * @return
     */
    public Person selectforEVA(int choice) {
    	Person invitee = null;

        Set<Person> pool = new UnitSet<>();
        
        Collection<Person> candidates = new UnitSet<>();
    	Settlement settlementVicinity = CollectionUtils.findSettlement(person.getCoordinates());

    	if (settlementVicinity != null) {
    		// Look for citizens only
            candidates = CollectionUtils.getPeopleInSettlementVicinity(person.getAssociatedSettlement(), true);
                  
            if (!candidates.isEmpty()) {
            	inviteeLocation = Location.SETTLEMENT_VICINITY;
            }
            else {
            	// Look for non-citizens only
                candidates = CollectionUtils.getPeopleInSettlementVicinity(person.getAssociatedSettlement(), false);
                
                if (!candidates.isEmpty()) {
                	inviteeLocation = Location.SETTLEMENT_VICINITY;
                }
            }
            
    	}
    	else { // if (vehicleVicinity != null){
        	Vehicle vehicleVicinity = CollectionUtils.findVehicle(person.getCoordinates());      	
    		// Look for citizens only
    		candidates = CollectionUtils.getPeopleInVehicleVicinity(vehicleVicinity, true);
    		
    		if (!candidates.isEmpty()) {
            	inviteeLocation = Location.VEHICLE_VICINITY;
            }
            else {
            	// Look for non-citizens only
            	// Note: for now, it's very rare for two mission vehicles of different settlement to arrive at the same outside location
                candidates = CollectionUtils.getPeopleInVehicleVicinity(vehicleVicinity, false);
                
                if (!candidates.isEmpty()) {
                	inviteeLocation = Location.VEHICLE_VICINITY;
                }
            }
    	}
    	
    	// In future, add other settlements' vicinity and other settlement's vehicles' vicinity  
          
    	pool.addAll(candidates);
        // remove the one who starts the conversation
        pool.remove(person);
        
        if (pool.isEmpty() && choice != 0) {
        	
        	Person p = selectFromSettlement(2);
        	if (p != null) {
	        	candidates.add(p);
	        	pool.addAll(candidates);
        	}
        }
        
        if (pool.isEmpty() && choice != 1) {
        	
        	Person p = selectFromVehicle(2);
        	if (p != null) {
        		candidates.add(p);
        		pool.addAll(candidates);
        	}
        }
        
        if (pool.isEmpty()) {
            endTask();
            return null;
        }
        
        int num = pool.size();
        List<Person> list = new ArrayList<>();
        list.addAll(pool);
        if (num == 1) {
        	invitee = list.get(0);
//        	inviteeLocation = Location.EVA;
        }
        else if (num > 1) {
        	int rand = RandomUtil.getRandomInt(num-1);
        	// half of the time, talk to just one person
        	if (RandomUtil.getRandomInt(1) == 0) {
        		invitee = getLikablePerson(list);
        	}
        	
        	if (invitee == null) {
    			invitee = list.get(RandomUtil.getRandomInt(rand));
    		}
        	
//        	inviteeLocation = Location.EVA;
        }
        else {
        	endTask();
        }

    	return invitee;
    }
    
    /**
     * Performs reading phase.
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double conversing(double time) {
		double remainingTime = 0;
		
        if (isDone()) {
        	endTask();
            return time;
        }
        
		// starting the conversation talking to the invitee
		if (getTarget() != null)
			talkWithInvitee();
		else
			logger.warning(person, "invitee is null.");
 
    	if (inviteeLocation.toString().contains("same"))
    		RelationshipUtil.changeOpinion(person, getTarget(), 
            	RelationshipType.FACE_TO_FACE_COMMUNICATION, RandomUtil.getRandomDouble(-.1, .3));
    	else 
    		RelationshipUtil.changeOpinion(person, getTarget(), 
        		RelationshipType.REMOTE_COMMUNICATION, RandomUtil.getRandomDouble(-.1, .2));

        if (getTimeCompleted() + time >= getDuration()) {
        	endTask();
        }

        return remainingTime;
    }
    
    /**
     * Talks to the invitee.
     */
    public void talkWithInvitee() {
		Task task = getTarget().getMind().getTaskManager().getTask();
		boolean canAdd = false;
		if (!hasConservation(getTarget())) {
			if (task == null)
				canAdd = getTarget().getMind().getTaskManager()
					.checkReplaceTask(new Converse(getTarget(), person));
			else {
				// Add conversation as a subtask to the invitee
				canAdd = task.addSubTask(new Converse(getTarget(), person));
			}
		}
		else {
			canAdd = true;
		}
		
		if (canAdd) {
			String name = getTarget().getName();
	    	String loc = inviteeLocation.toString();
	    	String s = CHATTING_WITH + " " + name + " " + loc;
	    	
	    	setDescription(s);
		}
		else {
			findInvitee();
		}
    }
    
    /**
     * Checks if a person is already conversing with someone.
     * 
     * @param person
     * @return
     */
    private boolean hasConservation(Person person) {
    	for (Task t : person.getTaskManager().getTaskStack()) {
    		if (t.getName().equalsIgnoreCase(Converse.NAME))
    			return true;
    	}
    	
    	return false;
    }
    
    /**
     * Performs the responding conversation phase.
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double responding(double time) {
		double remainingTime = 0;
		
        if (isDone()) {
        	endTask();
            return time;
        }
        	
		// Start the conversation talking to the initiator
		if (getTarget() != null) {
			talkWithInitiator();
		}	
		else
			logger.warning(getTarget(), "initiator is null.");

		// Add experience points
        addExperience(time);
        
        if (getTimeCompleted() + time >= getDuration()) {
        	endTask();
        }

        return remainingTime;
    }
    
    /**
     * Talks with the initiator.
     */
    public void talkWithInitiator() {
    	String name = getTarget().getName();
    	String s = RESPONDING_TO + " " + name;
    	
    	setDescription(s);
    }
    
    /**
	 * Reinitializes instances and reloads the target of the conversation.
	 */
	@Override
	public void reinit() {
		super.reinit();

		if (targetID != null && (targetID.intValue() > 0))
			target = unitManager.getPersonByID(targetID);
	}

	/**
	 * Gets a collection of people who are available for social conversation in the
	 * same/another building in the same/another settlement
	 *
	 * @param initiator      the initiator of this conversation
	 * @param checkIdle      true if the invitee is idling/relaxing (false if the
	 *                       invitee is in a chat)
	 * @param sameBuilding   true if the invitee is at the same building as the
	 *                       initiator (false if it doesn't matter)
	 * @param sameSettlement true if the collection includes all settlements (false
	 *                       if only the initiator's settlement)
	 * @return person a collection of invitee(s)
	 */
	public static Collection<Person> getChattingPeople(Person initiator, 
			boolean checkIdle, boolean sameBuilding, boolean sameSettlement) {
		Collection<Person> people = new ArrayList<>();
		Iterator<Person> i;
		// Set up rules that allows

		if (sameSettlement) {
			i = initiator.getAssociatedSettlement().getAllAssociatedPeople().iterator();
		} 
		
		else {
			i = CollectionUtils.getOtherPeople(initiator.getAssociatedSettlement()).iterator();
		}

		while (i.hasNext()) {
			Person invitee = i.next();
			
			// Skip the initiator
			if (invitee.equals(initiator))
				continue;

			Task task = invitee.getMind().getTaskManager().getTask();

			if (initiator.isInSettlement()) {

				if (sameBuilding) {
					// face-to-face conversation
					if (initiator.getBuildingLocation() != null
						&& invitee.getBuildingLocation() != null
						&& initiator.getBuildingLocation().equals(invitee.getBuildingLocation())) {
						addPerson(checkIdle, task, initiator, people, invitee);
					}
				}

				else {
					// may be radio (non face-to-face) conversation
					addPerson(checkIdle, task, initiator, people, invitee);
				}
			}

			else {
				addPerson(checkIdle, task, initiator, people, invitee);
			}
		}

		return people;
	}
	

	/**
	 * Adds a person to the people list.
	 * 
	 * @param checkIdle
	 * @param task
	 * @param initiator
	 * @param people
	 * @param person
	 */
	private static void addPerson(boolean checkIdle, Task task, Person initiator, Collection<Person> people, Person person) {
		if (checkIdle
			&& TaskUtil.isIdleTask(task)) {
				people.add(person);
	
		} else if ((task == null 
			|| initiator.getMind().getTaskManager().getTask() == null
			|| task.getName().equals(initiator.getMind().getTaskManager().getTask().getName())
			|| task instanceof Converse)) {
				people.add(person);
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		inviteeLocation = null; 
	}
}
