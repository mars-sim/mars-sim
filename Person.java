//************************** Person Unit **************************
// Last Modified: 5/8/00

// The Person class represents a person on the virtual Mars.  
// It keeps track of everything related to that person and provides information about him/her.

import java.awt.*;
import javax.swing.*;

public class Person extends Unit {

	// Data members

	private Settlement settlement;     // Person's current settlement
	private Vehicle vehicle;           // Vehicle person is riding in
	private NaturalAttributeManager attributes; // Manager for Person's natural attributes
	private SkillManager skills;      // Manager for Person's skills
	private TaskManager tasks;        // Manager for Person's tasks
	private String locationSituation; // Where person is ("In Settlement", "In Vehicle", "Outside")

	// Constructor

	public Person(int unitID, String name, Coordinates location, VirtualMars mars, UnitManager manager) {
		
		// Use Unit constructor
		
		super(unitID, name, location, mars, manager);
		
		// Initialize data members
		
		settlement = null;
		vehicle = null;
		attributes = new NaturalAttributeManager();
		skills = new SkillManager();
		tasks = new TaskManager(this, mars);
		locationSituation = new String("In Settlement");
	}

	// Returns a string for the person's relative location
	// "In Settlement", "In Vehicle" or "Outside"
	
	public String getLocationSituation() { return locationSituation; }
	
	// Sets the person's relative location
	// "In Settlement", "In Vehicle" or "Outside"
	
	public void setLocationSituation(String newLocation) { locationSituation = newLocation; }

	// Get settlement person is at, null if person is not at settlement

	public Settlement getSettlement() { return settlement; }

	// Get vehicle person is in, null if person is not in vehicle

	public Vehicle getVehicle() { return vehicle; }

	// Makes the person an inhabitant of a given settlement

	public void setSettlement(Settlement settlement) {
		this.settlement = settlement;
		location.setCoords(settlement.getCoordinates());
		settlement.addPerson(this);
		vehicle = null;
	}
	
	// Makes the person a passenger in a vehicle
	
	public void setVehicle(Vehicle vehicle) { 
		this.vehicle = vehicle; 
		settlement = null;	
	}

	// Action taken by person during unit turn

	public void takeAction(int seconds) { tasks.takeAction(seconds); }

	// Returns a detail window for the unit

	public UnitDialog getDetailWindow(MainDesktopPane parentDesktop) { return new PersonDialog(parentDesktop, this); } 
	
	// Returns a reference to the Person's natural attribute manager
	
	public NaturalAttributeManager getNaturalAttributeManager() { return attributes; }
	
	// Returns a reference to the Person's skill manager
	
	public SkillManager getSkillManager() { return skills; }
	
	// Returns a reference to the Person's task manager
	
	public TaskManager getTaskManager() { return tasks; }
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

