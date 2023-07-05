/*
 * Mars Simulation Project
 * Conversation.java
 * @date 2022-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.data.UnitSet;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.social.RelationshipType;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.meta.ConversationMeta;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Conversation class is the task of having a casual conversation with another person
 */
public class Conversation extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(Conversation.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.conversation"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase HAVING_CONVERSATION = new TaskPhase(Msg.getString(
            "Task.phase.havingConversation")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.3D;
    /** The person selected by initiator for having a conversation. */
    private transient Person invitee;
    
    /** The id of the person selected by initiator for having a conversation. */
    private int inviteeId = -1;

    private Location initiatorLocation = null;

    private enum Location
    {
        ALL_SETTLEMENTS,
        ANOTHER_BUILDING,
    	DINING_BUILDING,
    	NONE,
    	EVA,
        SAME_BUILDING,
        SAME_VEHICLE
    }

    /**
     * Constructor. This is an effort-driven task.
     * 
     * @param person the person performing the task.
     */
    public Conversation(Person person) {
        // Use Task constructor.
        super(NAME, person, true, false, 
        		STRESS_MODIFIER - RandomUtil.getRandomDouble(.2),
        		3 + RandomUtil.getRandomDouble(person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION))/20
        		 + RandomUtil.getRandomDouble(person.getPreference().getPreferenceScore(new ConversationMeta())/3.0)
        		);

        if (person.isInSettlement()) {
        	if (invitee == null)
        		invitee = selectFromSettlement();
        }
        else if (person.isInVehicle()) {
        	if (invitee == null)
        		invitee = selectFromVehicle();
        }
        else {
        	// Allow a person who are walking on the surface of Mars to have conversation
        	if (invitee == null)
        		invitee = selectforEVA();
        }
        
        if (invitee != null) {
            // Initialize phase
            addPhase(HAVING_CONVERSATION);
            setPhase(HAVING_CONVERSATION);
    	}
    	else {
            endTask();
        }
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

    	for (int i= 0; i<size; i++) {
    		double score = RelationshipUtil.getOpinionOfPerson(person, list.get(i));
    		if (score > bestScore) {
    			bestScore = score;
    			bestFriend = list.get(i);
    		}
    	}
    	return bestFriend;
    }

    /**
     * Talks to a person. Add conditional checking to append " via radio" in two cases.
     *
     * @param invitee
     */
    public void talkTo(Person invitee) {
    	String detail = invitee.getName();
    	if (initiatorLocation == Location.ANOTHER_BUILDING || initiatorLocation == Location.ALL_SETTLEMENTS)
    		detail = detail + " via radio";

    	if (invitee.getMind().getTaskManager().getTask() instanceof Conversation) {
    		String s = Msg.getString("Task.description.conversation.with.detail",
                    detail); //$NON-NLS-1$
        	setDescription(s);

			logger.log(person, Level.FINE, 30_000, s + ".");
        }
    	else {
    		String s = Msg.getString("Task.description.conversation.to.detail",
                    detail); //$NON-NLS-1$
        	setDescription(s);

			logger.log(person, Level.FINE, 30_000, s + ".");
    	}
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (HAVING_CONVERSATION.equals(getPhase())) {
            return havingConversation(time);
        }
        else {
            return time;
        }
    }

    public Person selectFromSettlement() {
    	Person invitee = null;
        Set<Person> pool = new UnitSet<>();
    
        // Gets a list of idle people in the same building
        Collection<Person> candidates = getChattingPeople(person, true, true, true);
        pool.addAll(candidates);
    	initiatorLocation = Location.SAME_BUILDING;

        if (pool.size() == 0) {
        	// Gets a list of busy people in the same building
            candidates = getChattingPeople(person, false, true, true);
        	pool.addAll(candidates);
        	initiatorLocation = Location.ANOTHER_BUILDING;
        }
        
        if (pool.size() == 0) {
        	// Go to a chatty chow hall
            Building diningBuilding = BuildingManager.getAvailableDiningBuilding(person, true);
            if (diningBuilding != null) {
            	// Walk to that building.
            	walkToActivitySpotInBuilding(diningBuilding, FunctionType.DINING, true);
                // Gets a list of chatty people in the same building
            	candidates = getChattingPeople(person, true, true, true);
            	pool.addAll(candidates);
            	initiatorLocation = Location.DINING_BUILDING;
            	
                if (pool.size() == 0) {
                	// Gets a list of busy people in the same dining building
                    candidates = getChattingPeople(person, false, true, true);
                	pool.addAll(candidates);
                	initiatorLocation = Location.DINING_BUILDING;
                }
            }
        }

        if (pool.size() == 0) {
        	// Gets a list of idle people in different bldg but the same settlement
        	candidates = getChattingPeople(person, true, false, true);
        	pool.addAll(candidates);
        	initiatorLocation = Location.ANOTHER_BUILDING;
        }
        
        if (pool.size() == 0) {
        	// Gets a list of busy people in different bldg but the same settlement
            candidates = getChattingPeople(person, false, false, true);
        	pool.addAll(candidates);
        	initiatorLocation = Location.ANOTHER_BUILDING;
        }
        
        if (pool.size() == 0) {
        	// Gets a list of idle people from other settlements
            candidates = getChattingPeople(person, true, false, false);
        	pool.addAll(candidates);
        	initiatorLocation = Location.ALL_SETTLEMENTS;
        }

        if (pool.size() == 0) {
        	// Gets a list of busy people from other settlements
            candidates = getChattingPeople(person, false, false, false);
        	pool.addAll(candidates);
        	initiatorLocation = Location.ALL_SETTLEMENTS;
        }

        if (pool.size() == 0) {
        	initiatorLocation = Location.NONE;
//        	logger.info(person, 30_000, "can't find anyone to chat with.");
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
        
        if (invitee != null)
        	inviteeId = invitee.getIdentifier();
    	return invitee;
    }

    public Person selectFromVehicle() {
    	Person invitee = null;

        Set<Person> pool = new UnitSet<>();
        Vehicle v = (Vehicle) person.getContainerUnit();
         
        Collection<Person> candidates = v.getTalkingPeople();
        pool.addAll(candidates);
        // remove the one who starts the conversation
        pool.remove(person);
		initiatorLocation = Location.SAME_VEHICLE;

        if (pool.size() == 0) {
 
            Collection<Person> talkingSameSettlement = getChattingPeople(person, false, false, true);
        	pool.addAll(talkingSameSettlement);
        	initiatorLocation = Location.ALL_SETTLEMENTS;
        }


        if (pool.size() == 0) {
        	initiatorLocation = Location.NONE;
//        	logger.info(person, "can't find anyone to chat with.");
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
        
        if (invitee != null)
        	inviteeId = invitee.getIdentifier();
    	return invitee;
    }

    public Person selectforEVA() {
    	Person invitee = null;

        Set<Person> pool = new UnitSet<>();
        
        Collection<Person> talkingSameSettlement = getChattingPeople(person, false, false, true);

        // remove the one who starts the conversation
        pool.remove(person);
        pool.addAll(talkingSameSettlement);

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
        else {
        	endTask();
        }
        
        if (invitee != null)
        	inviteeId = invitee.getIdentifier();
    	initiatorLocation = Location.EVA;
    	
    	return invitee;
    }
    
    /**
     * Performs reading phase.
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double havingConversation(double time) {
		double remainingTime = 0;
		
        if (isDone()) {
        	endTask();
            return time;
        }
        
        // After loading from saved sim, need to reload invitee
    	if (invitee == null) {
    		if (inviteeId == -1) {
    			logger.warning(person, "inviteeId is -1.");
    		}
    		else
    			invitee = Simulation.instance().getUnitManager().getPersonByID(inviteeId);
    		
    		// starting the conversation talking to the invitee
    		if (invitee != null)
    			talkTo(invitee);
    		else
    			logger.warning(person, "invitee is null.");
    		
    		// TODO: check if the invitee can or cannot carry on the conversation 
    		// and switch to another invitee
    	}
    	else
    		talkTo(invitee);
 
        RelationshipUtil.changeOpinion(person, invitee, 
        		RelationshipType.REMOTE_COMMUNICATION, RandomUtil.getRandomDouble(-.1, .15));

        if (getTimeCompleted() + time >= getDuration()) {
        	endTask();
        }

        return remainingTime;
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
	public static Collection<Person> getChattingPeople(Person initiator, boolean checkIdle, boolean sameBuilding,
			boolean sameSettlement) {
		Collection<Person> people = new ArrayList<>();
		Iterator<Person> i;
		// Set up rules that allows

		if (sameSettlement) {
			// could be either radio (non face-to-face) conversation, don't care
			i = unitManager.getPeople().iterator();
			sameBuilding = false;
		} else {
			// the only initiator's settlement
			// may be radio or face-to-face conversation
			i = initiator.getAssociatedSettlement().getIndoorPeople().iterator();
		}

		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			if (person.isInSettlement()
					&& initiator.isInSettlement()) {

				if (sameBuilding) {
					// face-to-face conversation
					if (initiator.getBuildingLocation().equals(person.getBuildingLocation())) {
						addPerson(checkIdle, task, initiator, people, person);
					}
				}

				else {
					// may be radio (non face-to-face) conversation
					addPerson(checkIdle, task, initiator, people, person);
				}
			}
			
			else {
				addPerson(checkIdle, task, initiator, people, person);
			}
		}

		return people;
	}
	

	private static void addPerson(boolean checkIdle, Task task, Person initiator, Collection<Person> people, Person person) {
		if (checkIdle
			&& isIdleTask(task)
				&& !person.equals(initiator)) {
					people.add(person);
	
		} else if ((task == null 
			|| initiator.getMind().getTaskManager().getTask() == null
			|| task.getName().equals(initiator.getMind().getTaskManager().getTask().getName())
			|| task instanceof Conversation)
				&& !person.equals(initiator)) {
				people.add(person);
		}
	}
	

	private static boolean isIdleTask(Task task) {
        return task instanceof Relax
                || task instanceof Read
                || task instanceof DayDream
                || task instanceof Conversation
                || task instanceof EatDrink;
    }
}
