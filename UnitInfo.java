//************************** Basic Unit Information Class **************************
// Last Modified: 2/29/00

// The UnitInfo class represents basic information about a unit.
// The UI commonly requests this information from the virtual Mars.

public class UnitInfo {

	// Data members

	private Unit unit;                 // Particular Unit referenced by UnitInfo
	private int unitID;                // Unit's ID number
	private String unitName;           // Unit's name
	
	// Constructor
	
	public UnitInfo(Unit unit, int unitID, String unitName) {
	
		// Initialize globals to arguments
	
		this.unit = unit;
		this.unitID = unitID;
		this.unitName = unitName;
	}
	
	// Returns unit's ID number
	
	public int getID() { return unitID; }
	
	// Returns unit's name
	
	public String getName() { return unitName; }
	
	// Returns unit's location coordinates
	
	public Coordinates getCoords() { return unit.getCoordinates(); }
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