//***************** Unit Button *****************
// Last Modified: 2/23/00

// The UnitButton class is a UI button for a given unit.
// It is displayed in the unit tool bar.

import java.awt.*;
import javax.swing.*;

public class UnitButton extends JButton {

	// Data members
	
	private int unitID;  // ID of unit which button is for.

	// Constructor

	public UnitButton(int unitID, String unitName, ImageIcon unitIcon) {
		
		// Use JButton constructor
		
		super(unitName, unitIcon); 
		
		// Initialize ID
		
		this.unitID = unitID;
		
		// Prepare default unit button values
		
		setFont(new Font("Helvetica", Font.BOLD, 9));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setAlignmentX(.5F);
		setAlignmentY(.5F);
	}
	
	// Returns units ID number
	
	public int getUnitID() { return unitID; }
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