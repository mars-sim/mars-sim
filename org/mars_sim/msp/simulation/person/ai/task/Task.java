/**
 * Mars Simulation Project
 * Task.java
 * @version 2.76 2004-08-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;

/** 
 * The Task class is an abstract parent class for tasks that allow people to do various things.
 * A person's TaskManager keeps track of one current task for the person, but a task may use other
 * tasks internally to accomplish things.
 */
public abstract class Task implements Serializable, Comparable {

    // Data members
    protected String name;            // The name of the task
    protected Person person;          // The person performing the task.
    private boolean done;             // True if task is finished
    protected double timeCompleted;   // The current amount of time spent on the task (in microsols)
    protected String description;     // Description of the task
    protected Task subTask;           // Sub-task of the current task
    protected String phase;           // Phase of task completion
    protected double phaseTimeRequired;  // Amount of time required to complete current phase. (in microsols)
    protected double phaseTimeCompleted; // Amount of time completed on the current phase. (in microsols)
    protected boolean effortDriven;     // Is this task effort driven
    private boolean createEvents;       // Task should create Historical events
    protected double stressModifier;  // Stress modified by person performing task per millisol.

    /** 
     * Constructs a Task object.
     * @param name the name of the task
     * @param person the person performing the task
     * @param effort Does this task require physical effort
     * @param createEvents Does this task create events?
     * @param stressModifier stress modified by person performing task per millisol.
     */
    public Task(String name, Person person, boolean effort, boolean createEvents, 
    		double stressModifier) {
        this.name = name;
        this.person = person;
		this.createEvents = createEvents;
		this.stressModifier = stressModifier;

        done = false;
        
        timeCompleted = 0D;
        description = name;
        subTask = null;
        phase = "";
        effortDriven = effort;
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        done = true;
        
        // Create ending task event if needed.
        if (getCreateEvents()) {
        	TaskEvent endingEvent = new TaskEvent(person, this, TaskEvent.FINISH, "");
			Simulation.instance().getEventManager().registerNewEvent(endingEvent);
        }
    }

    /**
     * Return the value of the effort driven flag.
     * @return Effort driven.
     */
    public boolean isEffortDriven() {
        return effortDriven;
    }

    /** Returns the name of the task.
     *  @return the task's name
     */
    public String getName() {
        if ((subTask != null) && !subTask.isDone()) return subTask.getName();
        else return name;
    }

    /** Returns a string that is a description of what the task is currently doing.
     *  This is mainly for user interface purposes.
     *  Derived tasks should extend this if necessary.
     *  Defaults to just the name of the task.
     *  @return the description of what the task is currently doing
     */
    public String getDescription() {
        if ((subTask != null) && !subTask.isDone()) return subTask.getDescription();
        else return description;
    }

    /** Returns a boolean whether this task should generate events
     *  @return boolean flag.
     */
    public boolean getCreateEvents() {
        return createEvents;
    }

    /** Returns a string of the current phase of the task.
     *  @return the current phase of the task
     */
    public String getPhase() {
        if ((subTask != null) && !subTask.isDone()) return subTask.getPhase();
        return phase;
    }

    /** Determines if task is still active.
     *  @return true if task is completed
     */
    public boolean isDone() {
        return done;
    }

    /** Adds a new sub-task.
     *  @param newSubTask the new sub-task to be added
     */
    void addSubTask(Task newSubTask) {
        if (subTask != null) subTask.addSubTask(newSubTask);
        else subTask = newSubTask;
    }

    /**
     * Gets the task's subtask.
     * Returns null if none
     * @return subtask
     */
    public Task getSubTask() {
        return subTask;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and the situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) { return 0D; }

    /** 
     * Perform the task for the given number of seconds.
     * Children should override and implement this.
     * @param time amount of time given to perform the task (in microsols)
     * @return amount of time remaining after performing the task (in microsols)
     * @throws Exception if error peforming task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = time;
        if (subTask != null) {
            if (subTask.isDone()) subTask = null;
            else timeLeft = subTask.performTask(timeLeft);
        }
        
        // Modify stress performing task.
        modifyStress(timeLeft);
        
        return timeLeft;
    }

    /**
     * SHould the start of this task create an historical event.
     * @param create New flag value.
     */
    protected void setCreateEvents(boolean create) {
        createEvents = create;
    }

    /**
     * Get a string representation of this Task. It's content will consist
     * of the description.
     *
     * @return Description of the task.
     */
    public String toString() {
        return description;
    }

    /**
     * Compare this object to another for an ordering. THe ordering is based
     * on the alphabetic ordering of the Name attribute.
     *
     * @param other Object to compare against.
     * @return integer comparasion of the two objects.
     * @throws ClassCastException if the object in not of a Task.
     */
    public int compareTo(Object other) {
        return name.compareTo(((Task)other).name);
    }
    
    /**
     * Modify stress from performing task for given time.
     * @param time the time performing the task.
     */
    private void modifyStress(double time) {
    	PhysicalCondition condition = person.getPhysicalCondition();
    	condition.setStress(condition.getStress() + stressModifier);
    }
    
    /**
     * Set the task's stress modifier.
     * Stress modifier can be positive (increase in stress) or negative (decrease in stress).
     * @param newStressModifier stress modification per millisol.
     */
    protected void setStressModifier(double newStressModifier) {
    	this.stressModifier = newStressModifier;
    }
    
    /**
     * Gets the probability modifier for a task if person needs to go to a new building.
     * @param person the person to perform the task.
     * @param newBuilding the building the person is to go to.
     * @return probability modifier
     * @throws BuildingException if current or new building doesn't have life support function.
     */
	protected static double getCrowdingProbabilityModifier(Person person, Building newBuilding) 
			throws BuildingException {
		double modifier = 1D;
		
		Building currentBuilding = BuildingManager.getBuilding(person);
		if ((currentBuilding != null) && (newBuilding != null) && (currentBuilding != newBuilding)) {
			
			// Increase probability if current building is overcrowded.
			LifeSupport currentLS = (LifeSupport) currentBuilding.getFunction(LifeSupport.NAME);
			int currentOverCrowding = currentLS.getOccupantNumber() - currentLS.getOccupantCapacity();
			if (currentOverCrowding > 0) modifier *= ((double) currentOverCrowding + 1);
			
			// Decrease probability if new building is overcrowded.
			LifeSupport newLS = (LifeSupport) newBuilding.getFunction(LifeSupport.NAME);
			int newOverCrowding = newLS.getOccupantNumber() - newLS.getOccupantCapacity();
			if (newOverCrowding > 0) modifier /= ((double) newOverCrowding + 1);
		}
		
		return modifier;
	}
}