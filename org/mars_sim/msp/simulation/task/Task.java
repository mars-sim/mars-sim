/**
 * Mars Simulation Project
 * Task.java
 * @version 2.72 2001-05-31
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The Task class is an abstract parent class for tasks that allow people to do various things.
 *  A person's TaskManager keeps track of one current task for the person, but a task may use other 
 *  tasks internally to accomplish things.
 */
abstract class Task {

    // Data members
	String name;            // The name of the task
	Person person;          // The person performing the task.
	VirtualMars mars;       // The virtual Mars
	boolean isDone;         // True if task is finished
	double timeCompleted;   // The current amount of time spent on the task
	String description;     // Description of the task
	Task subTask;           // Sub-task of the current task
	String phase;           // Phase of task completion
	String subPhase;        // Sub-phase of task completion
	double subPhaseCompleted;  // Amount of time completed in current subPhase

    /** Constructs a Task object
     *  @param name the name of the task
     *  @param person the person performing the task
     *  @param mars the virtual Mars
     */
	public Task(String name, Person person, VirtualMars mars) {
		this.name = new String(name);
		this.person = person;
		this.mars = mars;
		
		isDone = false;
		timeCompleted = 0D;
		description = name;
		subTask = null;
		phase = new String("");
		subPhase = new String("");
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
	
	/** Returns a string of the current sub-phase of the task. 
     *  @return the current sub-phase of the task
     */
	public String getSubPhase() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getSubPhase();
		return subPhase; 
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
	public static int getProbability(Person person, VirtualMars mars) { return 50; }
	
	/** Perform the task for the given number of seconds.
	 *  Children should override and implement this.
     *  @param seconds the number of seconds to perform the task
     */
	void doTask(double seconds) {	
		if ((subTask != null) && subTask.isDone()) subTask = null;
		if (subTask != null) subTask.doTask(seconds);
	}
	
	/** Returns true if task is finished, false otherwise. 
     *  @return true if task is finished
     */
	public boolean isDone() { return isDone; }
	
	/** Returns true is subPhase can be completed in given time. 
     *  @param seconds the number of seconds to perform the sub-phase
     *  @param timeRequired the time required to do the sub-phase
     *  @return true if sub-phase can be completed in given time
     */
	boolean doSubPhase(double seconds, double timeRequired) {
		if ((subPhaseCompleted + seconds) < timeRequired) {
			subPhaseCompleted += seconds;
			return false;
		}
		else {
			subPhaseCompleted = 0;
			return true;
		}
	}
}
