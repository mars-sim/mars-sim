/**
 * Mars Simulation Project
 * HaveConversation.java
 * @version 3.08 2015-09-24
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Preference;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.meta.TaskProbabilityUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Communication;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The HaveConversation class is the task of having a casual conversation with another person
 */
public class HaveConversation
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(HaveConversation.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.haveConversation"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase HAVING_CONVERSATION = new TaskPhase(Msg.getString(
            "Task.phase.havingConversation")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.5D;

    private List<Person> invitees = new ArrayList<Person>();
    
    private Person invitee;
    //private int randomTime;

    /**
     * Constructor. This is an effort-driven task.
     * @param person the person performing the task.
     */
    public HaveConversation(Person person) {
        // Use Task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER - RandomUtil.getRandomDouble(3), true, 2D + RandomUtil.getRandomDouble(5));

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check if there is a local dining building.
            Building diningBuilding = EatMeal.getAvailableDiningBuilding(person);
            if (diningBuilding != null)
            	// Walk to that building.
            	walkToActivitySpotInBuilding(diningBuilding, BuildingFunction.DINING, true);

            Set<Person> pool = new HashSet<Person>();
            Settlement s = person.getSettlement();
            Collection<Person> idle = s.getIdlePeople();           
            Collection<Person> talking = s.getTalkingPeople();
            pool.addAll(idle);   
            pool.addAll(talking);
            pool.remove((Person)person);
            int num = pool.size();
            List<Person> list = new ArrayList<Person>();
            list.addAll(pool);
            // pool includes the one who starts the conversation
            if (num == 1) {
        		invitee = list.get(0);
        		invitees.add(invitee);
        		talkTo(invitee);
            }
            else if (num > 1) {
            	int rand = RandomUtil.getRandomInt(num-1);           	
            	// half of the time, talk to just one person
            	if (RandomUtil.getRandomInt(1) == 0) {
            		invitee = list.get(rand);
            		invitees.add(invitee);
            		talkTo(invitee);
            	}
            	else {	
            	// speak to a group of people
	            	for (int i= 0; i< rand; i++) {
	            		invitee = list.get(i);
	            		invitees.add(invitee);
	            		talkTo(invitee);
	            	}     	
            	}
            }  
        }
        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

            int time = person.getPreference().getPreferenceScore(Preference.convertTask2MetaTask(this));
            super.setDuration(5 + time);

	        // set the boolean to true so that it won't be done again today
        	//person.getPreference().setTaskStatus(this, false);
        	
            Set<Person> pool = new HashSet<Person>();
            Vehicle v = (Vehicle) person.getContainerUnit();
            Collection<Person> crew = ((Rover) v).getCrew();           
            Collection<Person> talking = v.getTalkingPeople();
            pool.addAll(crew);   
            pool.addAll(talking);
            // remove the one who starts the conversation
            pool.remove((Person)person);
            int num = pool.size();
            List<Person> list = new ArrayList<Person>();
            list.addAll(pool);
            // pool includes the one who starts the conversation
            if (num == 1) {
                invitee = list.get(0);
        		invitees.add(invitee);
        		talkTo(invitee);
            }
            else if (num > 1) {
            	int rand = RandomUtil.getRandomInt(num-1);           	
            	// half of the time, talk to just one person
            	if (RandomUtil.getRandomInt(1) == 0) {
            		invitee = list.get(rand);
            		invitees.add(invitee);
            		talkTo(invitee);
            	}
            	else {	
            	// speak to a group of people
	            	for (int i= 0; i< rand; i++) {
	            		invitee = list.get(i);
	            		invitees.add(invitee); 
	            		talkTo(invitee);
	            	}     	
            	}
            }  
            
        }
        else {
            endTask();
        }

        // Initialize phase
        addPhase(HAVING_CONVERSATION);
        setPhase(HAVING_CONVERSATION);
    }

    public void talkTo(Person invitee) {
    	if (invitee.getMind().getTaskManager().getTask() instanceof HaveConversation) {
        	setDescription(Msg.getString("Task.description.havingConversation.detail", 
                invitee.getName())); //$NON-NLS-1$
        	//logger.info(person.getName() + " is chatting with " + invitee.getName());
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

    /**
     * Performs reading phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double havingConversation(double time) {

        if (isDone()) {
            return time;
        }

        // If duration, send invitation.
        if (getDuration() <= (getTimeCompleted() + time)) {

            // TODO: switch the invitee(s) to HaveConversation.           

            // Check if existing relationship between primary researcher and invitee.
            RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
            int size = invitees.size();
                     
            for (int i= 0; i< size; i++) {
            	Person invitee = invitees.get(i);
	            if (!relationshipManager.hasRelationship(person, invitee)) {
	                // Add new communication meeting relationship.
	                relationshipManager.addRelationship(person, invitee, Relationship.COMMUNICATION_MEETING);
	            }
	
	            // Add 1 point to invitee's opinion of the one who starts the conversation
	            Relationship relationship = relationshipManager.getRelationship(invitee, person);
	            double currentOpinion = relationship.getPersonOpinion(invitee);
	            relationship.setPersonOpinion(invitee, currentOpinion + RandomUtil.getRandomDouble(1));
	
            }
        }

        return 0D;
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