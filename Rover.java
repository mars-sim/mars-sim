//***************** Rover Unit *****************
// Last Modified: 8/30/00

// The Rover class represents the rover type of ground vehicle.
// It contains information about the rover.

import java.awt.*;
import javax.swing.*;

class Rover extends GroundVehicle {

	// Constructor

	public Rover(int unitID, String name, Coordinates location, VirtualMars mars, UnitManager manager) {
		
		// Use GroundVehicle constructor
		
		super(unitID, name, location, mars, manager); 
		
		// Set rover terrain modifier
		
		terrainHandlingCapability = 0D;
		
		// Set the vehicle size of the rover.
		
		vehicleSize = 2;
		
		// Set default maximum passengers for a rover.
		
		maxPassengers = 8;
		
		// Set default fuel capacity for a rover.
		
		fuelCapacity = 10D;
	}

	// Returns a detail window for the unit

	public UnitDialog getDetailWindow(MainDesktopPane parentDesktop) { return new RoverDialog(parentDesktop, this); } 
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