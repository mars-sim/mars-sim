//***************** Rover Unit *****************
// Last Modified: 5/6/00

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
	}

	// Returns a detail window for the unit

	public UnitDialog getDetailWindow(MainDesktopPane parentDesktop) { return new RoverDialog(parentDesktop, this); } 
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