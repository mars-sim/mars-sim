//************************** TaskTendGreenhouse **************************
// Last Modified: 7/27/00

// The TaskTendGreenhouse class is a task for tending the greenhouse in a settlement.  It has the phases, "Planting", "Tending" and "Harvesting".
// The duration of the task is chosen randomly at construction.

class TaskTendGreenhouse extends Task {

	// Data members

	private GreenhouseFacility greenhouse;      // The greenhouse the person is tending.
	private Settlement settlement;              // The settlement the greenhouse is in.
	private int duration;                       // The duration (in seconds) the person will perform the task.

	// Default Constructor

	public TaskTendGreenhouse(Person person, VirtualMars mars) {
	
		// Use Task constructor
	
		super("Tending Greenhouse", person, mars);
		
		// Initialize data members
		
		this.settlement = person.getSettlement();
		this.greenhouse = (GreenhouseFacility) settlement.getFacilityManager().getFacility("Greenhouse");
		
		// Randomly determine duration (in seconds) (up to 8 hours)
		
		duration = (int) Math.round(Math.random() * (8D * 60D * 60D));
	}
	
	// Returns the weighted probability that a person might perform this task.
	// Returns a 30 probability if person is at a settlement.
	// Returns a 0 if not.
	
	public static int getProbability(Person person, VirtualMars mars) { 
		if (person.getLocationSituation().equals("In Settlement")) {
			GreenhouseFacility greenhouse = (GreenhouseFacility) person.getSettlement().getFacilityManager().getFacility("Greenhouse");
			if ((greenhouse.getPhase().equals("Growing")) && (greenhouse.getGrowingWork() >= greenhouse.getWorkLoad())) return 0;
			else return 30;
		}
		else return 0;
	}
	
	// Performs the tending greenhouse task for a given number of seconds.
	
	public void doTask(int seconds) {
		
		super.doTask(seconds);
		if (subTask != null) return;
		
		// Get the phase from the greenhouse's phase of operation.
		
		phase = greenhouse.getPhase();
		
		// Determine seconds of effective work based on "Greenhouse Farming" skill.
		
		int workSeconds = seconds;
		int greenhouseSkill = person.getSkillManager().getSkillLevel("Greenhouse Farming");
		if (greenhouseSkill == 0) workSeconds /= 2;
		if (greenhouseSkill > 1) workSeconds += (int) Math.round((double) workSeconds * (.2D * (double) greenhouseSkill));
		
		// Add this work to the greenhouse.
		
		greenhouse.addWorkToGrowthCycle(workSeconds);
		
		// Keep track of the duration of the task.
		
		timeCompleted += seconds;
		if (timeCompleted >= duration) isDone = true;
		
		// Add experience to "Greenhouse Farming" skill
		// (1 base experience point per hour of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		
		double experience = ((double) seconds / 60D) / 60D;
		experience += experience * (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") - 50D) / 100D);
		person.getSkillManager().addExperience("Greenhouse Farming", experience);
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