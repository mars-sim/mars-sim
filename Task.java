//************************** Task **************************
// Last Modified: 3/2/00

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
		phase = new String("");
		subPhase = new String("");
	}
	
	// Returns the name of the task.
	
	public String getName() { return new String(name); }
	
	// Returns a string that is a current description of what the task is doing.
	// This is mainly for user interface purposes.
	// Derived tasks should extend this if necessary.
	// Defaults to just the name of the task.
	
	public String getDescription() { return description; }
	
	// Returns a string of the current phase of the task.
	
	public String getPhase() { return phase; }
	
	// Returns a string of the current sub-phase of the task.
	
	public String getSubPhase() { return subPhase; }
	
	// Returns the weighted probability that a person might perform this task.
	// It should return a 0 if there is no chance to perform this task given the person and the situation.
	
	public static int getProbability(Person person, VirtualMars mars) { return 50; }
	
	// Perform the task for the given number of seconds.
	// Children should override and implement this.
	
	public void doTask(int seconds) {}
	
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
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
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