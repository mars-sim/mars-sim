//************************** TaskMechanic **************************
// Last Modified: 8/29/00

package org.mars_sim.msp.simulation;

// The TaskMechanic class is a task for repairing or maintaining a vehicle.  
// It can be used for field repairing a vehicle or performing periodic maintenance
// on it in a maintenance garage.

import java.util.*;

class TaskMechanic extends Task {

	// Data members
	
	private Vehicle vehicle;                   // Vehicle that person is performing the task on.
	private MaintenanceGarageFacility garage;  // The maintenance garage at the settlement. (maintenance only)
	private Settlement settlement;             // The settlement the person is at. (maintenance only)
	private MechanicalFailure failure;         // The vehicle's mechanical failure. (repairing only)
	
	// Constructor for periodic vehicle maintenance in a garage.
	
	public TaskMechanic(Person person, VirtualMars mars) {
		super("Performing Maintenance on ", person, mars);
		
		settlement = person.getSettlement();
		garage = (MaintenanceGarageFacility) settlement.getFacilityManager().getFacility("Maintenance Garage");
		
		// Create vector of vehicles needing maintenance.
		
		Vector vehiclesNeedingMaint = new Vector();
		for (int x=0; x < settlement.getVehicleNum(); x++) {
			Vehicle tempVehicle = settlement.getVehicle(x);
			if ((tempVehicle.getDistanceLastMaintenance() > 5000) && !tempVehicle.isReserved()) {
				if (garage.vehicleInGarage(tempVehicle)) vehiclesNeedingMaint.addElement(tempVehicle);
				else {
					if (garage.getMaxVehicleSize() >= tempVehicle.getSize()) {
						if ((garage.getMaxSizeCapacity() - garage.getTotalSize()) >= tempVehicle.getSize()) 
							vehiclesNeedingMaint.addElement(tempVehicle);
					}
				}
			}
		}
		
		// Choose one of the vehicles needing maintenance.
		
		if (vehiclesNeedingMaint.size() > 0) {
			int vehicleNum = RandomUtil.getRandomInteger(vehiclesNeedingMaint.size() - 1);
			vehicle = (Vehicle) vehiclesNeedingMaint.elementAt(vehicleNum);
			if (!garage.vehicleInGarage(vehicle)) garage.addVehicle(vehicle);
			name = "Performing Maintenance on " + vehicle.getName();
			description = name;
			vehicle.setStatus("Periodic Maintenance");
		}
		else isDone = true;
	}
	
	// Constructor for vehicle field repairs.
	
	public TaskMechanic(Person person, VirtualMars mars, MechanicalFailure failure) {
		super("Repairing " + person.getVehicle().getName(), person, mars);
		
		vehicle = person.getVehicle();
		this.failure = failure;
		
		phase = "Repairing " + failure.getName();
	}
	
	// Returns the weighted probability that a person might perform this task.
	// Implement later.
	
	public static int getProbability(Person person, VirtualMars mars) { 
		
		int result = 0;
		
		if (person.getLocationSituation().equals("In Settlement")) {
			Settlement settlement = person.getSettlement();
			for (int x=0; x < settlement.getVehicleNum(); x++) {
				Vehicle vehicle = settlement.getVehicle(x);
				if ((vehicle.getDistanceLastMaintenance() > 5000) && !vehicle.isReserved()) result = 30;
			}
		}
		
		return result;
	}
	
	// Performs the mechanic task for a given number of seconds.
	
	public void doTask(int seconds) {
		
		super.doTask(seconds);
		if (subTask != null) return;
		
		// Determine seconds of effective work based on "Vehicle Mechanic" skill.
			
		int workSeconds = seconds;
		int mechanicSkill = person.getSkillManager().getSkillLevel("Vehicle Mechanic");
		if (mechanicSkill == 0) workSeconds /= 2;
		if (mechanicSkill > 1) workSeconds += (int) Math.round((double) workSeconds * (.2D * (double) mechanicSkill));
		
		if (name.startsWith("Repairing ")) {
			
			// Add work to the mechanical failure.
			
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
		}
		
		if (name.startsWith("Performing Maintenance on ")) {
			
			// Add work to the vehicle maintenance
			
			vehicle.addWorkToMaintenance(workSeconds);
			
			// Add experience to "Vehicle Mechanic" skill.
			// (1 base experience point per hour of work)
			// Experience points adjusted by person's "Experience Aptitude" attribute.
		
			double experience = 1D * (((double) seconds / 60D) / 60D);
			experience += experience * (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") - 50D) / 100D);
			person.getSkillManager().addExperience("Vehicle Mechanic", experience);
			
			// If vehicle maintenance is complete, task is done.
			// Move vehicle out of maintenance garage.
			
			if (vehicle.getDistanceLastMaintenance() == 0) {
				garage.removeVehicle(vehicle);
				vehicle.setStatus("Parked");
				isDone = true;
			}
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
