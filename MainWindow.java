//*********************** Main UI Window ***********************
// Last Modified: 2/22/00

// The MainWindow class is the primary UI frame for the project.
// It contains the tool bars and main desktop pane.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainWindow extends JFrame implements WindowListener {

	// Data members

	private VirtualMars mars;        // The virtual Mars
	private UnitToolBar toolbar;     // The tool bar
	private MainDesktopPane desktop; // The main desktop

	// Constructor

	public MainWindow(VirtualMars mars) {
		
		// Use JFrame constructor
		
		super("Mars Simulation Project (version 2.6)");

		// Initialize data member to parameter.
		
		this.mars = mars;

		// Prepare frame

		setVisible(false);
		addWindowListener(this);
		
		// Prepare content frame
		
		JPanel mainPane = new JPanel(new BorderLayout());
		setContentPane(mainPane);
		
		// Prepare toolbar
		
		toolbar = new UnitToolBar(this);
		mainPane.add(toolbar, "North");
		
		// Prepare desktop
		
		desktop = new MainDesktopPane(this);
		mainPane.add(desktop, "Center");
		
		// Set frame size
		
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frame_size = new Dimension(screen_size);
		if (screen_size.width > 800) frame_size = new Dimension((int) Math.round(screen_size.getWidth() * .7D), (int) Math.round(screen_size.getHeight() * .7D));
		setSize(frame_size);

		// Center frame on screen
		
 		setLocation(((screen_size.width - frame_size.width) / 2), ((screen_size.height - frame_size.height) / 2));	
		
		// Show frame
		
		setVisible(true);
	}
	
	// Create a new unit button in toolbar
	
	public void createUnitButton(int unitID, String unitName, ImageIcon unitIcon) { toolbar.createUnitButton(unitID, unitName, unitIcon); }
	
	// Opens a tool window if necessary
	
	public void openToolWindow(String toolName) { desktop.openToolWindow(toolName); }
	
	// Opens a window for a unit if it isn't already open
	// Also makes a new unit botton in toolbar if necessary
	
	public void openUnitWindow(int unitID) { desktop.openUnitWindow(unitID); }
	
	// Disposes a unit window and button
	
	public void disposeUnitWindow(int unitID) { desktop.disposeUnitWindow(unitID); }
	
	// Disposes a unit button in toolbar
	
	public void disposeUnitButton(int unitID) { toolbar.disposeUnitButton(unitID); }
	
	// Returns a unit dialog for a given unit ID
	
	public UnitDialog getUnitDialog(int unitID) { return mars.getDetailWindow(unitID, desktop); }
	
	// Returns an array of unit info for all moving vehicles sorted by name
	
	public UnitInfo[] getMovingVehicleInfo() { return mars.getMovingVehicleInfo(); }
	
	// Returns an array of unit info for all vehicles sorted by name
	
	public UnitInfo[] getVehicleInfo() { return mars.getVehicleInfo(); }
	
	// Returns an array of unit info for all settlements sorted by name
	
	public UnitInfo[] getSettlementInfo() { return mars.getSettlementInfo(); }
	
	// Returns an array of unit info for all people sorted by name
	
	public UnitInfo[] getPeopleInfo() { return mars.getPeopleInfo(); }

	/*
	
	// Returns the coordinates of a named unit
	
	public Coordinates getUnitCoords(String unitName) { return mars.getUnitCoords(unitName); }
	
	// Returns sorted array of unit names with a given category
	
	public String[] getSortedUnitNames(String unitCategory) { return mars.getSortedUnitNames(unitCategory); }

	*/

	// WindowListener methods overridden

	public void windowClosing(WindowEvent event) { System.exit(0); }
	public void windowClosed(WindowEvent event) {}
	public void windowDeiconified(WindowEvent event) {}
	public void windowIconified(WindowEvent event) {}
	public void windowActivated(WindowEvent event) {}
	public void windowDeactivated(WindowEvent event) {}
	public void windowOpened(WindowEvent event) {}
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