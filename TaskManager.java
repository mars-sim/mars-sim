//************************** Task Manager **************************
// Last Modified: 7/25/00

// The TaskManager class keeps track of a person's current task and can randomly
// assign a new task to a person based on a list of possible tasks and that person's
// current situation.  
//
// There is one instance of TaskManager per person.

import java.util.*;
import java.lang.reflect.*;

class TaskManager {

	// Data Members

	private Task currentTask;            // The current task the person is doing.
	private Person person;               // The person the task manager is responsible for.
	private VirtualMars mars;            // The virtual Mars
	private Class[] generalTasks;        // A collection of general tasks a person can do.

	// Constructor

	public TaskManager(Person person, VirtualMars mars) {
		
		// Initialize data members
		
		this.person = person;
		this.mars = mars;
		currentTask = null;
		
		// Create an array of general task classes.
		// (Add additional general tasks as they are created)
		
		try { generalTasks = new Class[] { TaskRelax.class, TaskDrive.class, TaskTendGreenhouse.class }; }
		catch(Exception e) { System.out.println("TaskManager.constructor(): " + e.toString()); }
	}
	
	// Returns true if person has a current task.
	
	public boolean hasCurrentTask() { 
		if (currentTask != null) return true;
		else return false;
	}
	
	// Returns a description of current task for UI purposes.
	// Returns null if there is no current task.
	
	public String getCurrentTaskDescription() { 
		if (currentTask != null) return currentTask.getDescription(); 
		else return null;	
	}
	
	// Returns the name of current task phase if there is one.
	// Returns black string if current task has no phase.
	// Returns null if there is no current task.
	
	public String getCurrentPhase() {
		if (currentTask != null) return currentTask.getPhase();
		else return null;
	}
	
	// Returns the name of current task sub-phase if there is one.
	// Returns black string if current task has no sub-phase.
	// Returns null if there is no current task.
	
	public String getCurrentSubPhase() {
		if (currentTask != null) return currentTask.getSubPhase();
		else return null;
	}
	
	// Returns the current task.
	// Return null if there is no current task.
	
	public Task getCurrentTask() { return currentTask; }
	
	// Adds a sub-task to the stack of tasks.
	
	public void addSubTask(Task subTask) {
		if (currentTask != null) currentTask.addSubTask(subTask);
		else currentTask = subTask;
	}
	
	// Perform a task for a given number of seconds.
	// If person has no task or the current task is done, assign a new task to him/her.
	
	public void takeAction(int seconds) {
		
		if ((currentTask == null) || currentTask.isDone()) { getNewTask(); }

		currentTask.doTask(seconds);
	}
	
	// Assigns a new task to a person based on general tasks available.
	// (Add support for role-based tasks later)
	
	private void getNewTask() {
		
		// Initialize variables
		
		Vector probableTasks = new Vector();
		Vector weights = new Vector();
		Class[] parametersForFindingMethod = { Person.class, VirtualMars.class };
		Object[] parametersForInvokingMethod = { person, mars };
		
		// Find the probable weights of each possible general task.
		
		for (int x=0; x < generalTasks.length; x++) {
			
			try {
				Method probability = generalTasks[x].getMethod("getProbability", parametersForFindingMethod);
				int weight = ((Integer) probability.invoke(null, parametersForInvokingMethod)).intValue();
				
				if (weight > 0) {
					probableTasks.addElement(generalTasks[x]);
					weights.addElement(new Integer(weight));
				}
			}
			catch(Exception e) { System.out.println("TaskManager.getNewTask() (1): " + e.toString()); }
		}
		
		// Total up the weights
		
		int totalWeight = 0;
		for (int x=0; x < weights.size(); x++) totalWeight += ((Integer) weights.elementAt(x)).intValue();
			
		// Get a random number from 0 to the total weight	
			
		int r = (int) Math.round(Math.random() * (double) totalWeight);
		  
		// Determine which task is selected  
		  
		int tempWeight = ((Integer) weights.elementAt(0)).intValue();
		int taskNum = 0;
		while (tempWeight < r) {
			taskNum++;
			tempWeight += ((Integer) weights.elementAt(taskNum)).intValue();
		}
		
		// Create an instance of that task and set it as the current task
		
		try {
			Constructor construct = ((Class) probableTasks.elementAt(taskNum)).getConstructor(parametersForFindingMethod);
			currentTask = (Task) construct.newInstance(parametersForInvokingMethod);
		}
		catch(Exception e) { System.out.println("TaskManager.getNewTask() (2): " + e.toString()); }
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