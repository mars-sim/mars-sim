//************************** Task **************************
// Last Modified: 7/27/00

// The Task class is an abstract parent class for tasks that allow people to do various things.
// A person's TaskManager keeps track of one current task for the person, but a task may use other 
// tasks internally to accomplish things.

abstract class Task {

	// Data members

	protected String name;            // The name of the task
	protected Person person;          // The person performing the task.
	protected VirtualMars mars;       // The virtual Mars
	protected boolean isDone;         // True if task is finished
	protected int timeCompleted;      // The current amount of time spent on the task
	protected String description;     // Description of the task
	protected Task subTask;           // Sub-task of the current task
	protected String phase;           // Phase of task completion
	protected String subPhase;        // Sub-phase of task completion
	protected int subPhaseCompleted;  // Amount of time completed in current subPhase

	// Constructor

	public Task(String name, Person person, VirtualMars mars) {
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
	
	// Returns the name of the task.
	
	public String getName() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getName();
		else return name; 
	}
	
	// Returns a string that is a current description of what the task is doing.
	// This is mainly for user interface purposes.
	// Derived tasks should extend this if necessary.
	// Defaults to just the name of the task.
	
	public String getDescription() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getDescription();
		else return description; 
	}
	
	// Returns a string of the current phase of the task.
	
	public String getPhase() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getPhase();
		return phase; 
	}
	
	// Returns a string of the current sub-phase of the task.
	
	public String getSubPhase() { 
		if ((subTask != null) && !subTask.isDone()) return subTask.getSubPhase();
		return subPhase; 
	}
	
	// Adds a new sub-task.
	
	public void addSubTask(Task newSubTask) {
		if (subTask != null) subTask.addSubTask(newSubTask);
		else subTask = newSubTask;
	}
	
	// Returns the weighted probability that a person might perform this task.
	// It should return a 0 if there is no chance to perform this task given the person and the situation.
	
	public static int getProbability(Person person, VirtualMars mars) { return 50; }
	
	// Perform the task for the given number of seconds.
	// Children should override and implement this.
	
	public void doTask(int seconds) {
		
		if ((subTask != null) && subTask.isDone()) subTask = null;
		
		if (subTask != null) subTask.doTask(seconds);
	}
	
	// Returns true if task is finished, false otherwise.
	
	public boolean isDone() { return isDone; }
	
	// Returns true is subPhase can be completed in given time 
	
	protected boolean doSubPhase(int seconds, int timeRequired) {
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

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA