/**
 * Mars Simulation Project
 * Mission.java
 * @version 2.77 2004-09-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.events.HistoricalEvent;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;

/** The Mission class represents a large multi-person task
 *
 *  There is at most one instance of a mission per person.
 *  A Mission may have one or more people associated with it.
 */
public abstract class Mission implements Serializable {

    // Constant string type for events
    private static final String START_MISSION = "Start Mission ";
    private static final String END_MISSION = "End Mission ";

    // Data members
    protected PersonCollection people; // People in mission
    protected String name; // Name of mission
    protected String description; // Description of the mission
    protected MissionManager missionManager; // The simulation's mission manager
    protected boolean done; // True if mission is completed
    protected String phase; // The phase of the mission
    protected int missionCapacity; // The number of people that can be in the mission

    /** Constructs a Mission object
     *  @param name the name of the mission
     *  @param missionManager the simulation's misison manager
     */
    public Mission(String name, MissionManager missionManager, Person startingPerson) {

        // Initialize data members
        this.name = name;
        this.missionManager = missionManager;
        description = name;
        people = new PersonCollection();
        done = false;
        phase = "";
        missionCapacity = Integer.MAX_VALUE;

		// Created mission starting event.
		HistoricalEvent newEvent = new MissionEvent(startingPerson, this, MissionEvent.START);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);

        // Add starting person to mission.
        addPerson(startingPerson);
    }

    /** Adds a person to the mission.
     *  @param person to be added
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

    /** Removes a person from the mission
     *  @param person to be removed
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

    /** Determines if a mission includes the given person
     *  @param person person to be checked
     *  @return true if person is member of mission
     */
    public boolean hasPerson(Person person) {
        return people.contains(person);
    }

    /** Gets the number of people in the mission.
     *  @return number of people
     */
    public int getPeopleNumber() {
        return people.size();
    }

    /**
     * Gets a collection of the people in the mission.
     * @return collection of people
     */
    public PersonCollection getPeople() {
        return new PersonCollection(people);
    }

    /** Returns the mission's manager
     *  @return mission manager
     */
    public MissionManager getMissionManager() {
        return missionManager;
    }

    /** Determines if mission is completed.
     *  @return true if mission is completed
     */
    public boolean isDone() {
        return done;
    }

    /** Gets the name of the mission.
     *  @return name of mission
     */
    public String getName() {
        return name;
    }

    /** Gets the mission's description.
     *  @return mission description
     */
    public String getDescription() {
        return description;
    }

    /** Gets the current phase of the mission.
     *  @return phase
     */
    public String getPhase() {
        return phase;
    }

    /** Performs the mission.
     *  Mission may choose a new task for a person in the mission.
     *  @param person the person performing the mission
     */
    public void performMission(Person person) {

    }

    /** Gets the weighted probability that a given person would start this mission.
     *  @param person the given person
     *  @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {
        return 0D;
    }

    /** Gets the weighted probability that a given person would join this mission.
     *  @param person the given person
     *  @return the weighted probability
     */
    public double getJoiningProbability(Person person) {
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
        for(int i = 0; i < p.length; i++) {
            removePerson((Person)p[i]);
        }
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
     * Gets the home settlement for the mission. 
     * @return home settlement or null if none.
     */
    public abstract Settlement getHomeSettlement();
    
    /**
     * Gets a collection of the vehicles associated with this mission.
     * @return collection of vehicles.
     */
    public abstract VehicleCollection getMissionVehicles();
    
    /**
     * Gets the relationship mission probability modifier.
     * @param person the person to check for
     * @return the modifier
     */
    protected double getRelationshipProbabilityModifier(Person person) {
    	double result = 1D;
    	
    	RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		double totalOpinion = 0D;
    	PersonIterator i = getPeople().iterator();
    	while (i.hasNext()) totalOpinion+= ((relationshipManager.getOpinionOfPerson(person, i.next()) - 50D) / 50D);
    	
    	if (totalOpinion > 0D) result*= (1D + totalOpinion);
    	else if (totalOpinion < 0D) result/= (1D - totalOpinion);
    	
    	return result;
    }
}