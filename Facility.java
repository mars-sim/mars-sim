//************************** Facility **************************
// Last Modified: 5/6/00

// The Facility class is an abstract class that is the parent to 
// all settlement facilities and has data members and methods
// common to all facilities.

public abstract class Facility {

	// Data members
	
	protected String name;             // Name of the facility.
	protected String tabName;          // Name of the facility used for the UI tab.
	protected FacilityManager manager; // The Settlement's FacilityManager.

	// Constructor

	public Facility(FacilityManager manager, String name, String tabName) {
	
		// Initialize data members
		
		this.manager = manager;
		this.name = name;
		this.tabName = tabName;
	}
	
	// Returns the name of the facility.
	
	public String getName() { return name; }
	
	// Returns the UI tab name of the facility.
	
	public String getTabName() { return tabName; }
	
	// Called every clock pulse for time events in facilities.
	// Override in children to use this.
	
	public void timePasses(int seconds) {}
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