/**
 * Mars Simulation Project
 * Mission.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import java.util.Vector;
import java.io.Serializable;

/** The Mission class represents a large multi-person task
 *
 *  There is at most one instance of a mission per person.
 *  A Mission may have one or more people associated with it.
 */
public abstract class Mission implements Serializable {

    // Data members
    protected Mars mars; // Virtual Mars
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
        mars = missionManager.getMars();
        phase = "";
        missionCapacity = Integer.MAX_VALUE;

        // Add starting person to mission.
        people.add(startingPerson);
    }

    /** Adds a person to the mission. 
     *  @param person to be added 
     */
    void addPerson(Person person) {
        if (!people.contains(person)) {
            people.add(person);
            // System.out.println(person.getName() + " added to mission: " + name);
        }
    }

    /** Removes a person from the mission 
     *  @param person to be removed
     */
    protected void removePerson(Person person) {
        if (people.contains(person)) {
            people.remove(person);
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
     *  @param mars the virtual Mars
     *  @return the weighted probability
     */
    public static double getNewMissionProbability(Person person, Mars mars) {
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
	people.clear();
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
} 
