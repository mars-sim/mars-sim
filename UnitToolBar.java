//*********************** Unit Button Toolbar ***********************
// Last Modified: 4/10/00

// The UnitToolBar class is a UI toolbar for holding unit buttons.
// The should only be one instance and is contained in the MainWindow instance.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class UnitToolBar extends JToolBar implements ActionListener {

	// Data members

	private Vector unitButtons;          // List of unit buttons
	private MainWindow parentMainWindow; // Main window that contains this toolbar.
	
	// Constructor

	public UnitToolBar(MainWindow parentMainWindow) {
	
		// Use JToolBar constructor
	
		super();
	
		// Initialize data members
	
		unitButtons = new Vector();
		this.parentMainWindow = parentMainWindow;
	
		// Set name
	
		setName("Unit Toolbar");
		
		// Fix tool bar
		
		setFloatable(false);
		
		// Set preferred height to 53 pixels.
		
		setPreferredSize(new Dimension(0, 57));
		
		// Set border around toolbar
		
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}
	
	// Create a new unit button in toolbar
	
	public void createUnitButton(int unitID, String unitName, ImageIcon unitIcon) {
		
		// Check if unit button already exists
		
		boolean alreadyExists = false;
		for (int x=0; x < unitButtons.size(); x++) {
			if (((UnitButton) unitButtons.elementAt(x)).getUnitID() == unitID) alreadyExists = true;
		}
		
		if (!alreadyExists) {
			UnitButton tempButton = new UnitButton(unitID, unitName, unitIcon);	
			tempButton.addActionListener(this);
			add(tempButton);
			validate();
			repaint();	
			unitButtons.addElement(tempButton);
		}
	}
	
	// Disposes a unit button in toolbar
	
	public void disposeUnitButton(int unitID) {
		for (int x=0; x < unitButtons.size(); x++) {
			UnitButton tempButton = (UnitButton) unitButtons.elementAt(x);
			if (tempButton.getUnitID() == unitID) {
				unitButtons.removeElement(tempButton);
				remove(tempButton);
				validate();
				repaint();
			}
		}
	}
		
	// ActionListener method overriden
	
	public void actionPerformed(ActionEvent event) {
			
		// show unit window on desktop
			
		parentMainWindow.openUnitWindow(((UnitButton) event.getSource()).getUnitID());
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