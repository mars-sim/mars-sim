//************************ Virtual Mars ************************
// Last Modified: 2/28/00

// The VirtualMars class represents virtual Mars in the simulation.  It contains
// all the units, a master clock, and access to the topography data.

import java.util.*;

public class VirtualMars {

	// Data members
	
	private MasterClock masterClock;    // Master clock for virtual world
	private UnitManager units;          // Unit controller
	private SurfaceTerrain surface;     // Surface Terrain of Mars
	
	// Constructor

	public VirtualMars() {
		
		// Initialize surface terrain
		
		surface = new SurfaceTerrain();
		
		// Initialize all units
		
		units = new UnitManager(this);
		
		// Initialize and start master clock

		masterClock = new MasterClock(this);
		masterClock.start();
	}
	
	// Clock pulse from master clock
	
	public void clockPulse(int seconds) { units.takeAction(seconds); }
	
	// Returns surface terrain object
	
	public SurfaceTerrain getSurfaceTerrain() { return surface; }
	
//**************************** UI Accessor Methods ****************************
	
	// Returns an array of unit info for all moving vehicles sorted by name
	
	public UnitInfo[] getMovingVehicleInfo() { return units.getMovingVehicleInfo(); }
	
	// Returns an array of unit info for all vehicles sorted by name
	
	public UnitInfo[] getVehicleInfo() { return units.getVehicleInfo(); }
	
	// Returns an array of unit info for all settlements sorted by name
	
	public UnitInfo[] getSettlementInfo() { return units.getSettlementInfo(); }
	
	// Returns an array of unit info for all people sorted by name
	
	public UnitInfo[] getPeopleInfo() { return units.getPeopleInfo(); }
	
	// Returns a unit dialog for a given unit ID
	
	public UnitDialog getDetailWindow(int unitID, MainDesktopPane desktop) { return units.getDetailWindow(unitID, desktop); };
	/*
	// Returns the coordinates of a named unit
	
	public Coordinates getUnitCoords(String unitName) { return units.getUnitCoords(unitName); }
	
	// Returns sorted array of unit names with a given category
	
	public String[] getSortedUnitNames(String unitCategory) { return units.getSortedUnitNames(unitCategory); }
	*/
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