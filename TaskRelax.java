//************************** TaskRelax **************************
// Last Modified: 2/27/00

// The TaskRelax class is a simple task that implements resting and doing nothing for a while.
// The duration of the task is by default chosen randomly, up to one day (approx).
// An alternative constructor allows the duration to be set to a given number of seconds.

// Note: Personal mental stress may be added later, which this task could be used to reduce.

class TaskRelax extends Task {

	// Data members

        private int duration;      // The predetermined duration in seconds of the task.

	// Constructor

	public TaskRelax(Person person, VirtualMars mars) {
		super("Relaxing", person, mars);
		
		duration = (int) Math.round(Math.random() * (25D * 60D * 60D));
	}
	
	// Constructor to relax for a given number of seconds
	
	public TaskRelax(Person person, VirtualMars mars, int seconds) {
		this(person, mars);
		
		duration = seconds;
	}
	
	// Returns the weighted probability that a person might perform this task.
	// It should return a 0 if there is no chance to perform this task given the person and his/her situation.
	
	public static int getProbability(Person person, VirtualMars mars) { return 50; }
	
	// Overriding Task's doTask method.
	// This task simply waits until the set duration of the task is complete, then ends the task.
	
	public void doTask(int seconds) {
	
		timeCompleted += seconds;
		if (timeCompleted > duration) isDone = true;
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