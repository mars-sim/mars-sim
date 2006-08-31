/**
 * Mars Simulation Project
 * Mission.java
 * @version 2.78 2005-08-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.events.HistoricalEvent;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The Mission class represents a large multi-person task
 * There is at most one instance of a mission per person.
 * A Mission may have one or more people associated with it.
 */
public abstract class Mission implements Serializable {

    // Constant string type for events
    // private static final String START_MISSION = "Start Mission ";
    // private static final String END_MISSION = "End Mission ";

    // Data members
    private PersonCollection people; // People in mission
    private String name; // Name of mission
    private String description; // Description of the mission
    private int minPeople; // The minimum number of people for mission.
    private boolean done; // True if mission is completed
    private Collection phases; // A collection of the mission's phases.
    private String phase; // The current phase of the mission
    private String phaseDescription; // The discription of the current phase of operation.
    private boolean phaseEnded; // Has the current phase ended?
    private int missionCapacity; // The number of people that can be in the mission

    /** 
     * Constructs a Mission object
     * @param name the name of the mission
     * @param startingPerson the person starting the mission.
     * @param minPeople the minimum number of people required for mission.
     * @throws MissionException if error constructing mission.
     */
    public Mission(String name, Person startingPerson, int minPeople) throws MissionException {

        // Initialize data members
        this.name = name;
        description = name;
        people = new PersonCollection();
        done = false;
        phase = null;
        phaseDescription = null;
        phases = new ArrayList();
        phaseEnded = false;
        this.minPeople = minPeople;
        missionCapacity = Integer.MAX_VALUE;

		// Created mission starting event.
		HistoricalEvent newEvent = new MissionEvent(startingPerson, this, MissionEvent.START);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);

        // Add starting person to mission.
		startingPerson.getMind().setMission(this);
    }

    /** 
     * Adds a person to the mission.
     * @param person to be added
     */
    public void addPerson(Person person) {
        if (!people.contains(person)) {
            people.add(person);

			// Creating mission joining event.
            HistoricalEvent newEvent = new MissionEvent(person, this, MissionEvent.JOINING);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);
            // System.out.println(person.getName() + " added to mission: " + name);
        }
    }

    /** 
     * Removes a person from the mission
     * @param person to be removed
     */
    public void removePerson(Person person) {
        if (people.contains(person)) {
            people.remove(person);

			// Creating missing finishing event.
			HistoricalEvent newEvent = new MissionEvent(person, this, MissionEvent.FINISH);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

            if (people.size() == 0) done = true;
            // System.out.println(person.getName() + " removed from mission: " + name);
        }
    }

    /** 
     * Determines if a mission includes the given person
     * @param person person to be checked
     * @return true if person is member of mission
     */
    public boolean hasPerson(Person person) {
        return people.contains(person);
    }

    /** 
     * Gets the number of people in the mission.
     * @return number of people
     */
    public int getPeopleNumber() {
        return people.size();
    }
    
    /**
     * Gets the minimum number of people required for mission.
     * @return minimum number of people
     */
    public int getMinPeople() {
    	return minPeople;
    }
    
    /**
     * Sets the minimum number of people required for a mission.
     * @param minPeople minimum number of people
     */
    protected void setMinPeople(int minPeople) {
    	this.minPeople = minPeople;
    }

    /**
     * Gets a collection of the people in the mission.
     * @return collection of people
     */
    public PersonCollection getPeople() {
        return new PersonCollection(people);
    }

    /** 
     * Determines if mission is completed.
     * @return true if mission is completed
     */
    public boolean isDone() {
        return done;
    }

    /** 
     * Gets the name of the mission.
     * @return name of mission
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the mission.
     * @param name the new mission name
     */
    protected void setName(String name) {
    	this.name = name;
    }

    /** 
     * Gets the mission's description.
     * @return mission description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the mission's description.
     * @param description the new description.
     */
    public void setDescription(String description) {
    	this.description = description;
    }

    /** 
     * Gets the current phase of the mission.
     * @return phase
     */
    public String getPhase() {
        return phase;
    }
    
    /**
     * Sets the mission phase.
     * @param newPhase the new mission phase.
     * @throws MissionException if newPhase is not in the mission's collection of phases.
     */
    protected void setPhase(String newPhase) throws MissionException {
    	if (newPhase == null) throw new IllegalArgumentException("newPhase is null");
    	else if (phases.contains(newPhase)) {
    		phase = newPhase;
    		setPhaseEnded(false);
    		phaseDescription = null;
    	}
    	else throw new MissionException(getPhase(), "newPhase: " + newPhase + " is not a valid phase for this mission.");
    }
    
    /**
     * Adds a phase to the mission's collection of phases.
     * @param newPhase the new phase to add.
     */
    public void addPhase(String newPhase) {
    	if (newPhase == null) throw new IllegalArgumentException("newPhase is null");
    	else if (!phases.contains(newPhase)) phases.add(newPhase);
    }
    
    /**
     * Gets the description of the current phase.
     * @return phase description.
     */
    public String getPhaseDescription() {
    	if (phaseDescription != null) return phaseDescription;
    	else return phase;
    }
    
    /**
     * Sets the description of the current phase.
     * @param description the phase description.
     */
    public void setPhaseDescription(String description) {
    	phaseDescription = description;
    }

    /** 
     * Performs the mission. 
     * @param person the person performing the mission.
     * @throws MissionException if problem performing the mission.
     */
    public void performMission(Person person) throws MissionException {

    	// If current phase is over, decide what to do next.
    	if (getPhaseEnded()) determineNewPhase();
    	
    	// Perform phase.
    	if (!isDone()) performPhase(person);
    }
    
    /**
     * Determines a new phase for the mission when the current phase has ended.
     * @throws MissionException if problem setting a new phase.
     */
    protected abstract void determineNewPhase() throws MissionException;
    
    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     * @throws MissionException if problem performing the phase.
     */
    protected abstract void performPhase(Person person) throws MissionException;

    /** Gets the weighted probability that a given person would start this mission.
     *  @param person the given person
     *  @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {
        return 0D;
    }

    /** Gets the mission capacity for participating people.
     *  @return mission capacity
     */
    public int getMissionCapacity() {
        return missionCapacity;
    }

    /** Sets the mission capacity to a given value.
     *  @param newCapacity the new mission capacity
     */
    protected void setMissionCapacity(int newCapacity) {
        missionCapacity = newCapacity;
    }

    /** Finalizes the mission.
     *  Mission can override this to perform necessary finalizing operations.
     */
    protected void endMission() {
        done = true;
        Object p[] = people.toArray();
        for(int i = 0; i < p.length; i++) removePerson((Person) p[i]);
    }

    /**
     * Adds a new task for a person in the mission.
     * Task may be not assigned if it is effort-driven and person is too ill
     * to perform it.
     * @param person the person to assign to the task
     * @param task the new task to be assigned
     */
    protected void assignTask(Person person, Task task) {
        boolean canPerformTask = true;

        // If task is effort-driven and person too ill, do not assign task.
        if (task.isEffortDriven() && (person.getPerformanceRating() < .5D))
            canPerformTask = false;

        if (canPerformTask) person.getMind().getTaskManager().addTask(task);
    }
    
	/**
	 * Checks to see if any of the people in the mission have any dangerous medical 
	 * problems that require treatment at a settlement. 
	 * Also any environmental problems, such as suffocation.
	 * @return true if dangerous medical problems
	 */
	protected boolean hasDangerousMedicalProblems() {
		boolean result = false;
		PersonIterator i = people.iterator();
		while (i.hasNext()) {
			if (i.next().getPhysicalCondition().hasSeriousMedicalProblems()) result = true;
		}
		return result;
	}
	
	/**
	 * Checks if the mission has an emergency situation.
	 * @return true if emergency.
	 */
	protected boolean hasEmergency() {
		return hasDangerousMedicalProblems();
	}
	
	/**
	 * Recruits new people into the mission.
	 * @param startingPerson the person starting the mission.
	 */
	protected void recruitPeopleForMission(Person startingPerson) {
		
		// Get all people qualified for the mission.
		PersonCollection qualifiedPeople = new PersonCollection();
		PersonIterator i = Simulation.instance().getUnitManager().getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (isCapableOfMission(person)) qualifiedPeople.add(person);
		}
		
		// Recruit the most qualified and most liked people first.
		try {
			while (qualifiedPeople.size() > 0) {
				double bestPersonValue = 0D;
				Person bestPerson = null;
				PersonIterator j = qualifiedPeople.iterator();
				while (j.hasNext() && (getPeopleNumber() < getMissionCapacity())) {
					Person person = j.next();
					// Determine the person's mission qualification.
					double qualification = getMissionQualification(person) * 100D;
					
					// Determine how much the recruiter likes the person.
					RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
					double likability = relationshipManager.getOpinionOfPerson(startingPerson, person);
					
					// Check if person is the best recruit.
					double personValue = (qualification + likability) / 2D;
					if (personValue > bestPersonValue) {
						bestPerson = person;
						bestPersonValue = personValue;
					}
				}
		
				// Try to recruit best person available to the mission.
				if (bestPerson != null) {
					recruitPerson(startingPerson, bestPerson);
					qualifiedPeople.remove(bestPerson);
				}
				else break;
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
		
		if (getPeopleNumber() < getMinPeople()) endMission();
	}
	
	/**
	 * Attempt to recruit a new person into the mission.
	 * @param recruiter the person doing the recruiting.
	 * @param recruitee the person being recruited.
	 * @throws MissionException if problem recruiting person.
	 */
	private void recruitPerson(Person recruiter, Person recruitee) throws MissionException {
		if (isCapableOfMission(recruitee)) {
			// Get mission qualification modifier.
			double qualification = getMissionQualification(recruitee) * 100D;
			
			RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
			
			// Get the recruitee's social opinion of the recruiter.
			double recruiterLikability = relationshipManager.getOpinionOfPerson(recruitee, recruiter);
			
			// Get the recruitee's average opinion of all the current mission members.
			double groupLikability = relationshipManager.getAverageOpinionOfPeople(recruitee, people);
			
			double recruitmentChance = (qualification + recruiterLikability + groupLikability) / 3D;
			if (recruitmentChance > 100D) recruitmentChance = 100D;
			else if (recruitmentChance < 0D) recruitmentChance = 0D;
			
			if (RandomUtil.lessThanRandPercent(recruitmentChance)) recruitee.getMind().setMission(this);
		}
	}
	
	/**
	 * Checks to see if a person is capable of joining a mission.
	 * @param person the person to check.
	 * @return true if person could join mission.
	 */
	protected boolean isCapableOfMission(Person person) {
		if (person == null) throw new IllegalArgumentException("person is null");
		
		// Make sure person isn't already on a mission.
		if (person.getMind().getMission() == null) {
			// Make sure person doesn't have any serious health problems.
			if (!person.getPhysicalCondition().hasSeriousMedicalProblems()) return true;
		}
		
		return false;
	}
	
	/**
	 * Gets the mission qualification value for the person.
	 * Person is qualified and interested in joining the mission if the value is larger than 0.
	 * The larger the qualification value, the more likely the person will be picked for the mission.
	 * @param person the person to check.
	 * @return mission qualification value.
	 * @throws MissionException if error determining mission qualification.
	 */
	protected double getMissionQualification(Person person) throws MissionException {
		
		double result = 0D;
		
		if (isCapableOfMission(person)) {
			// Get base result for job modifier.
			Job job = person.getMind().getJob();
			if (job != null) result = job.getJoinMissionProbabilityModifier(this.getClass());
		}
		
		return result;
	}
	
	/**
	 * Checks if the current phase has ended or not.
	 * @return true if phase has ended
	 */
	protected boolean getPhaseEnded() {
		return phaseEnded;
	}
	
	/**
	 * Sets if the current phase has ended or not.
	 * @param phaseEnded true if phase has ended
	 */
	protected void setPhaseEnded(boolean phaseEnded) {
		this.phaseEnded = phaseEnded;
	}
	
	/**
	 * Gets the settlement associated with the mission.
	 * @return settlement or null if none.
	 */
	public abstract Settlement getAssociatedSettlement();
	
	/**
	 * Gets the number and amounts of resources needed for the mission.
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of amount and item resources and their Double amount or Integer number.
	 * @throws Exception if error determining needed resources.
	 */
    public abstract Map getResourcesNeededForRemainingMission(boolean useBuffer) throws Exception ;
    
    /**
     * Gets the number and types of equipment needed for the mission.
     * @param useBuffer use time buffers in estimation if true.
     * @return map of equipment class and Integer number.
     * @throws Exception if error determining needed equipment.
     */
    public abstract Map getEquipmentNeededForRemainingMission(boolean useBuffer) throws Exception;
}