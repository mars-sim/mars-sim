//*********************** Tool Button Toolbar ***********************
// Last Modified: 4/10/00

package org.mars_sim.msp.ui.standard; 

// The ToolToolBar class is a UI toolbar for holding tool buttons.
// The should only be one instance and is contained in the MainWindow instance.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class ToolToolBar extends JToolBar implements ActionListener {

	// Data members

	private Vector toolButtons;          // List of tool buttons
	private MainWindow parentMainWindow; // Main window that contains this toolbar.
	
	// Constructor

	public ToolToolBar(MainWindow parentMainWindow) {
	
		// Use JToolBar constructor
	
		super(JToolBar.VERTICAL);
		
		// Initialize data members
	
		toolButtons = new Vector();
		this.parentMainWindow = parentMainWindow;
		
		// Set name
	
		setName("Tool Toolbar");
		
		// Fix tool bar
		
		setFloatable(false);
	
		// Prepare tool buttons
	
		prepareToolButtons();
		
		// Set border around toolbar
		
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}
	
	// Prepares tool buttons

	private void prepareToolButtons() {
		
		// Add Mars navigator button
		
		ToolButton navButton = new ToolButton("Mars Navigator", "images/NavigatorIcon.gif");
		navButton.addActionListener(this);
		add(navButton);
		toolButtons.addElement(navButton);
		
		// Add search tool button
		
		ToolButton searchButton = new ToolButton("Search Tool", "images/SearchIcon.gif");
		searchButton.addActionListener(this);
		add(searchButton);
		toolButtons.addElement(searchButton);
	}
	
	// ActionListener method overriden
	
	public void actionPerformed(ActionEvent event) {
	
		// show tool window on desktop
			
		parentMainWindow.openToolWindow(((ToolButton) event.getSource()).getToolName()); 
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
