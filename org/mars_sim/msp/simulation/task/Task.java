/**
 * Mars Simulation Project
 * Task.java
 * @version 2.72 2001-08-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;

/** The Task class is an abstract parent class for tasks that allow people to do various things.
 *  A person's TaskManager keeps track of one current task for the person, but a task may use other 
 *  tasks internally to accomplish things.
 */
abstract class Task implements Serializable {

    // Data members
    protected String name;            // The name of the task
    protected Person person;          // The person performing the task.
    protected VirtualMars mars;       // The virtual Mars
    protected boolean done;         // True if task is finished
    protected double timeCompleted;   // The current amount of time spent on the task (in microsols)
    protected String description;     // Description of the task
    protected Task subTask;           // Sub-task of the current task
    protected String phase;           // Phase of task completion
    protected double phaseTimeRequired;  // Amount of time required to complete current phase. (in microsols)
    protected double phaseTimeCompleted; // Amount of time completed on the current phase. (in microsols)

    /** Constructs a Task object
     *  @param name the name of the task
     *  @param person the person performing the task
     *  @param mars the virtual Mars
     */
    public Task(String name, Person person, VirtualMars mars) {
        this.name = name;
        this.person = person;
        this.mars = mars;
		
        done = false;
        timeCompleted = 0D;
        description = name;
        subTask = null;
        phase = "";
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
	
    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and the situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, VirtualMars mars) { return 0; }
	
    /** Perform the task for the given number of seconds.
     *  Children should override and implement this.
     *  @param time amount of time given to perform the task (in microsols)
     *  @return amount of time remaining after performing the task (in microsols)
     */
    double performTask(double time) {	
        double timeLeft = time;
        if ((subTask != null) && subTask.isDone()) subTask = null;
        if (subTask != null) timeLeft = subTask.performTask(timeLeft);

        return timeLeft;
    }
}
