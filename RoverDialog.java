//******************** Rover Detail Window ********************
// Last Modified: 2/23/00

// The RoverDialog class is the detail window for a rover.
// It displays information about a rover and it's current status.

import java.awt.*;
import javax.swing.*;

public class RoverDialog extends GroundVehicleDialog {

	// Data members

	protected Rover rover;  // Rover related to this detail window 

	// Constructor

	public RoverDialog(MainDesktopPane parentDesktop, Rover rover) {
		
		// Use GroundVehicleDialog constructor
		
		super(parentDesktop, rover);
	}
	
	// Load image icon (overridden)
	
	public ImageIcon getIcon() { return new ImageIcon("RoverIcon.gif"); }
	
	// Set window size
	
	protected Dimension setWindowSize() { return new Dimension(300, 430); }
	
	// Prepare components
	
	protected void setupComponents() {
		
		// Initialize rover
		
		rover = (Rover) parentUnit;
		
		super.setupComponents();
	}
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

