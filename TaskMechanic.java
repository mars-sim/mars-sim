//************************** TaskMechanic **************************
// Last Modified: 7/27/00

// The TaskMechanic class is a task for repairing or maintaining a vehicle.  
// It can be used for field repairing a vehicle or performing periodic maintenance
// on it in a maintenance garage.

class TaskMechanic extends Task {

	// Data members
	
	private Vehicle vehicle;                   // Vehicle that person is performing the task on.
	private MaintenanceGarageFacility garage;  // The maintenance garage at the settlement. (maintenance only)
	private Settlement settlement;             // The settlement the person is at. (maintenance only)
	private MechanicalFailure failure;         // The vehicle's mechanical failure. (repairing only)
	
	// Constructor for periodic vehicle maintenance in a garage.
	
	public TaskMechanic(Person person, VirtualMars mars) {
		super("Performing Maintainance on " + person.getVehicle().getName(), person, mars);
		
		settlement = person.getSettlement();
		garage = (MaintenanceGarageFacility) settlement.getFacilityManager().getFacility("Maintenance Garage");
		// vehicle = garage.getVehicle();
	}
	
	// Constructor for vehicle field repairs.
	
	public TaskMechanic(Person person, VirtualMars mars, MechanicalFailure failure) {
		super("Repair " + person.getVehicle().getName(), person, mars);
		
		vehicle = person.getVehicle();
		this.failure = failure;
		
		phase = "Repairing " + failure.getName();
		
		System.out.println(person.getName() + " starts repairing " + failure.getName() + " on " + vehicle.getName());
	}
	
	// Returns the weighted probability that a person might perform this task.
	// Implement later.
	
	public static int getProbability(Person person, VirtualMars mars) { 
		
		int result = 0;
		
		return result;
	}
	
	// Performs the mechanic task for a given number of seconds.
	
	public void doTask(int seconds) {
		
		super.doTask(seconds);
		if (subTask != null) return;
		
		if (name.startsWith("Repair ")) {
			
			// Determine seconds of effective work based on "Vehicle Mechanic" skill.
			
			int workSeconds = seconds;
			int mechanicSkill = person.getSkillManager().getSkillLevel("Vehicle Mechanic");
			if (mechanicSkill == 0) workSeconds /= 2;
			if (mechanicSkill > 1) workSeconds += (int) Math.round((double) workSeconds * (.2D * (double) mechanicSkill));
			
			// Add this work to the mechanical failure.
			
			failure.addWorkTime(workSeconds);
			
			// Add experience to "Vehicle Mechanic" skill.
			// (3 base experience points per hour of work)
			// Experience points adjusted by person's "Experience Aptitude" attribute.
		
			double experience = 3D * (((double) seconds / 60D) / 60D);
			experience += experience * (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") - 50D) / 100D);
			person.getSkillManager().addExperience("Vehicle Mechanic", experience);
			
			// If failure is fixed, task is done.
			
			isDone = failure.isFixed();
			
			// If task is done, return vehicle to moving status.
			
			if (isDone) vehicle.setStatus("Moving");
			if (isDone) System.out.println(person.getName() + " is done repairing " + failure.getName() + " on " + vehicle.getName());
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