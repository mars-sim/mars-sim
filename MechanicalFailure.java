//************************** Vehicle Mechanical Failure **************************
// Last Modified: 8/19/00

// The MechanicalFailure class represents a mechanical failure in a vehicle..

public class MechanicalFailure {

	// Data Members
	
	private String name;               // The name of the failure.
	private float totalWorkHours;      // The total number of work hours required to fix the failure. (1 - 50).
	private float remainingWorkHours;  // The remaining work hours required to fix the failure. (1 - 50).
	private boolean fixed;             // True when mechanical failure is fixed.
	
	// Constructor
	
	public MechanicalFailure(String name) {
		
		// Initialize data members
		
		this.name = name;
		fixed = false;
		
		// workHours random from 1 to 50 hours.
		
		totalWorkHours = ((float) Math.random() * (50F - 1F)) + 1F;
		remainingWorkHours = totalWorkHours;
	}
	
	// Returns the name of the failure.
	
	public String getName() { return name; }
	
	// Returns true if mechanical failure is fixed.
	
	public boolean isFixed() { return fixed; }
	
	// Returns the total work hours required to fix the failure.
	
	public float getTotalWorkHours() { return totalWorkHours; }
	
	// Returns the remaining work hours required to fix the failure.
	
	public float getRemainingWorkHours() { return remainingWorkHours; }
	
	// Adds some work time (in seconds) to the failure.
	
	public void addWorkTime(int seconds) {
		
		// Convert seconds to work hours.
		
		float hours = ((float) seconds / 60) / 60;
		
		remainingWorkHours -= hours;
		
		if (remainingWorkHours <= 0F) fixed = true;
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
