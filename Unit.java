//************************** Abstract Basic Unit Class **************************
// Last Modified: 2/29/00

// The Unit class is the abstract parent class to all units on the virtual Mars.
// Units include people, vehicles and settlements.
// This class provides data members and methods common to all units.

import java.awt.*;
import javax.swing.*;

public abstract class Unit {

	// Data members

	protected Coordinates location;	   // Unit location coordinates
	protected int unitID;              // Unit ID assigned by UnitManager
	protected String name;             // Unit name
	protected VirtualMars mars;        // The virtual Mars
	protected UnitManager manager;     // Primary unit manager

	// Constructor

	public Unit(int unitID, String name, Coordinates location, VirtualMars mars, UnitManager manager)	{

		// Initialize data members from parameters

		this.unitID = unitID;
		this.name = name;
		this.location = location;
		this.mars = mars;
		this.manager = manager;
	}
	
	// Return a UnitInfo object for the unit
	
	public UnitInfo getUnitInfo() { return new UnitInfo(this, unitID, name); }

	// Returns unit's ID
	
	public int getID() { return unitID; }
	
	// Returns unit's UnitManager
	
	public UnitManager getUnitManager() { return manager; }

	// Returns unit's name

	public String getName() { return new String(name); }

	// Returns unit's location coordinates
	
	public Coordinates getCoordinates() { return new Coordinates(location); }

	// Sets unit's location coordinates
	
	public void setCoordinates(Coordinates newLocation) { location.setCoords(newLocation); }

	// Returns a detail window for the unit
	// Must be overridden

	public abstract UnitDialog getDetailWindow(MainDesktopPane parentDesktop); 
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