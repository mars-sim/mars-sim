/**
 * Mars Simulation Project
 * Task.java
 * @version 2.71 2000-09-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The Task class is an abstract parent class for tasks that allow people to do various things.
 *  A person's TaskManager keeps track of one current task for the person, but a task may use other 
 *  tasks internally to accomplish things.
 */
abstract class Task {

	String name;            // The name of the task
	Person person;          // The person performing the task.
	VirtualMars mars;       // The virtual Mars
	boolean isDone;         // True if task is finished
	int timeCompleted;      // The current amount of time spent on the task
	String description;     // Description of the task
	Task subTask;           // Sub-task of the current task
	String phase;           // Phase of task completion
	String subPhase;        // Sub-phase of task completion
	int subPhaseCompleted;  // Amount of time completed in current subPhase

	Task(String name, Person person, VirtualMars mars) {
		this.name = new String(name);
		this.person = person;
		this.mars = mars;
		
		isDone = false;
		timeCompleted = 0;
		description = name;
		subTask = null;
		phase = new String("");
		subPhase = new String("");
	}
	
	/** Returns the name of the task. */
	public String getName() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getName();
		else return name; 
	}
	
	/** Returns a string that is a current description of what the task is doing.
	 *  This is mainly for user interface purposes.
	 *  Derived tasks should extend this if necessary.
	 *  Defaults to just the name of the task.
     */
	public String getDescription() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getDescription();
		else return description; 
	}
	
	/** Returns a string of the current phase of the task. */
	public String getPhase() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getPhase();
		return phase; 
	}
	
	/** Returns a string of the current sub-phase of the task. */
	public String getSubPhase() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getSubPhase();
		return subPhase; 
	}
	
	/** Adds a new sub-task. */
	void addSubTask(Task newSubTask) {
		if (subTask != null) subTask.addSubTask(newSubTask);
		else subTask = newSubTask;
	}
	
	/** Returns the weighted probability that a person might perform this task.
	 *  It should return a 0 if there is no chance to perform this task given the person and the situation.
     */
	static int getProbability(Person person, VirtualMars mars) { return 50; }
	
	/** Perform the task for the given number of seconds.
	 *  Children should override and implement this.
     */
	void doTask(int seconds) {	
		if ((subTask != null) && subTask.isDone()) subTask = null;
		if (subTask != null) subTask.doTask(seconds);
	}
	
	/** Returns true if task is finished, false otherwise. */
	public boolean isDone() { return isDone; }
	
	/** Returns true is subPhase can be completed in given time. */
	boolean doSubPhase(int seconds, int timeRequired) {
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
