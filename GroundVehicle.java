//***************** Ground Vehicle Unit *****************
// Last Modified: 6/20/00

// The GroundVehicle class represents a ground-type vehicle.
// It is abstract and should be extended to a particular type of ground vehicle.

public abstract class GroundVehicle extends Vehicle {

	// Data members

	protected double elevation;                 // Current elevation in km
	protected double terrainHandlingCapability; // Ground vehicle's basic terrain handling capability	
	protected double terrainGrade;              // Average angle of terrain over next 7.4km distance in direction vehicle is traveling

	// Constructor

	public GroundVehicle(int unitID, String name, Coordinates location, VirtualMars mars, UnitManager manager) {
		
		// Use Vehicle constructor
		
		super(unitID, name, location, mars, manager); 
		
		// Initialize public variables
		
		terrainHandlingCapability = 0D;  // Default terrain capability
		terrainGrade = 0D;
		elevation = mars.getSurfaceTerrain().getElevation(location);
	}
	
	// Returns the elevation of the vehicle in km.
	
	public double getElevation() { return elevation; }
	
	// Sets the elevation of the vehicle (in km.)
	
	public void setElevation(double elevation) { this.elevation = elevation; }

	// Returns the vehicle's terrain capability

	public double getTerrainHandlingCapability() { return terrainHandlingCapability; }

	// Returns true if ground vehicle is stuck

	public boolean getStuck() { 
		if (status.equals("Stuck - Using Winch")) return true; 
		else return false;
	}
	
	// Sets the ground vehicle's stuck value
	
	public void setStuck(boolean stuck) { 
		if (stuck) status = "Stuck - Using Winch";
		else status = "Moving";
	}

	// Returns terrain steepness as angle

	public double getTerrainGrade() { return terrainGrade; }

	// Sets the terrain grade with an angle
	
	public void setTerrainGrade(double terrainGrade) { this.terrainGrade = terrainGrade; }
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
