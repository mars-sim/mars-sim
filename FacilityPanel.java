//************************** Facility Panel **************************
// Last Modified: 5/19/00

// The FacilityPanel class displays information about a settlement's facility in the user interface.
// It is the abstract parent class for the displays of each facility.

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class FacilityPanel extends JPanel {
	
	// Data members
	
	protected String tabName;           // The name for the panel's tab
	protected MainDesktopPane desktop;  // The main desktop

	// Constructor

	public FacilityPanel(MainDesktopPane desktop) {
	
		// Use JPanel's constructor
	
		super();
		
		// Initialize data members
		
		this.desktop = desktop;

		// Set the default font

		setFont(new Font("Helvetica", Font.BOLD, 12));
		
		// Set a border around the panel
		
		setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	}
	
	// Returns the tab name for the facility
	
	public String getTabName() { return tabName; }
	
	// Updates the facility panel's information
	
	public abstract void updateInfo();
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