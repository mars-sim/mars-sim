//************************** Unit Detail Window **************************
// Last Modified: 2/27/00

// The UnitDialog class is an abstract UI detail window for a given unit.
// It displays information about the unit and its current status.
// It is abstract and detail windows for particular types of units need
// to be derived from this class.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class UnitDialog extends JInternalFrame implements Runnable, ActionListener {

	// Data members

	protected MainDesktopPane parentDesktop;  // Parent Main Window
	protected JButton unitButton;             // Button for unit detail window
	protected Unit parentUnit;                // Parent unit for which detail window is about
	protected JPanel mainPane;                // Content pane
	protected Box mainVerticalBox;            // Main layout box
	protected Thread updateThread;            // Dialog update thread
	protected JButton centerMapButton;        // Center map button

	// Constructor

	public UnitDialog(MainDesktopPane parentDesktop, Unit parentUnit) {

		// Use JInternalFrame constructor

		super(parentUnit.getName(), false, true, false, true);
		
		// Initialize data members

		this.parentDesktop = parentDesktop;
		this.parentUnit = parentUnit;
		
		// Initialize cached data members
		
		initCachedData();
		
		// Prepare frame
		
		startGUISetup();
		setupComponents();
		finishGUISetup();
		
		// Do first update
		
		generalUpdate();
		
		// Start update thread
		
		start();
	}
	
	// Starts display update thread, and creates a new one if necessary

	public void start() {
		if ((updateThread == null) || (!updateThread.isAlive())) {
			updateThread = new Thread(this, "unit dialog");
			updateThread.start();
		}
	}
	
	// Update thread runner
	
	public void run() {

		// Endless refresh loop

		while(true) { 
			
			// Pause for 2 seconds between display refreshs

			try { updateThread.sleep(2000); }
			catch (InterruptedException e) {}
			
			// Update display
			try { generalUpdate(); }
			catch(NullPointerException e) { System.out.println("NullPointerException: " + parentUnit.getName()); }
		}
	}
	
	// Returns unit's name
	
	public String getUnitName() { return parentUnit.getName(); }
	
	// Returns unit ID number
	
	public int getUnitID() { return parentUnit.getID(); }
	
	// Load image icon
	// Must be overridden by final dialog
	
	public abstract ImageIcon getIcon();
	
	// ActionListener method overriden
	
	public void actionPerformed(ActionEvent event) {
		Object button = event.getSource();
			
		// If center map button, center map and globe on unit
			
		if (button == centerMapButton) parentDesktop.centerMapGlobe(parentUnit.getCoordinates());
	}
	
	// Initialize cached data members
	
	protected void initCachedData() {}
	
	// Start creating window
	
	protected void startGUISetup() {
	
		// Don't show window until finished
		
		setVisible(false);

		// Set default font

		setFont(new Font("Helvetica", Font.BOLD, 12));
		
		// Prepare content pane
		
		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
	}

	// Finish creating window

	protected void finishGUISetup() {

		// Properly size window

		setSize(setWindowSize());
	}
	
	// Set up proper window size
	// Must be overridden by final dialog
	
	protected abstract Dimension setWindowSize();
	
	// Prepare and add components to window

	protected void setupComponents() {

		// Prepare name panel
		
		JPanel namePanel = new JPanel();
		mainPane.add(namePanel);

		// Prepare name label

		JLabel nameLabel = new JLabel(parentUnit.getName(), getIcon(), JLabel.CENTER);
		nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
		nameLabel.setHorizontalTextPosition(JLabel.CENTER);
		nameLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
		nameLabel.setForeground(Color.black);
		namePanel.add(nameLabel);
		
	}

	// Complete update
	// Must be overridden by final dialog

	protected abstract void generalUpdate();
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
