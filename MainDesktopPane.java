//*********************** Main Desktop Pane ***********************
// Last Modified: 4/30/00

// The MainDesktopPane class is the desktop part of the project's UI.
// It contains all tool and unit windows, and is itself contained, along
// with the tool bars, by the main window.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainDesktopPane extends JDesktopPane implements ComponentListener {

	// Data members
	
	private Vector unitWindows;     // List of open or buttoned unit windows.
	private Vector toolWindows;     // List of tool windows.
	private MainWindow mainWindow;  // The main window frame.
	
	private ImageIcon backgroundImageIcon;
	private JLabel backgroundLabel;
	private JLabel logoLabel;
	private boolean firstDisplay;
	
	// Constructor

	public MainDesktopPane(MainWindow mainWindow) {
	
		// Use JDesktopPane constructor
		
		super();
		
		// Initialize data members
		
		this.mainWindow = mainWindow;
		unitWindows = new Vector();
		toolWindows = new Vector();
		
		// Set background color to black
		
		setBackground(Color.black);
		
		// set desktop manager
		
		setDesktopManager(new MainDesktopManager());
		
		// Set component listener
		
		addComponentListener(this);
		
		// Prepare tool windows
		
		prepareToolWindows();
		
		// Create background logo label and make it partially transparent
		
		logoLabel = new JLabel(new ImageIcon("logo2.gif"), JLabel.LEFT);
		add(logoLabel, Integer.MIN_VALUE);
		logoLabel.setOpaque(false);
		
		// Create background label and set it to the back layer
			
		backgroundImageIcon = new ImageIcon();
		backgroundLabel = new JLabel(backgroundImageIcon);
		add(backgroundLabel, Integer.MIN_VALUE);
		backgroundLabel.setLocation(0, 0);
		moveToBack(backgroundLabel);
		
		// Initialize firstDisplay to true
		
		firstDisplay = true;
	}
	
	
	
	public void componentResized(ComponentEvent e) {
		
		// If displayed for the first time, create background image tile.
		// The size of the background tile cannot be determined during construction
		// since it requires the MainDesktopPane be displayed first.
		
		if (firstDisplay) {
			ImageIcon baseImageIcon = new ImageIcon("background.gif");
			Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
			Image backgroundImage = createImage((int) screen_size.getWidth(), (int) screen_size.getHeight());
			Graphics backgroundGraphics = backgroundImage.getGraphics();
		
			for (int x=0; x < backgroundImage.getWidth(this); x += baseImageIcon.getIconWidth()) {
				for (int y=0; y < backgroundImage.getHeight(this); y += baseImageIcon.getIconHeight()) {
					backgroundGraphics.drawImage(baseImageIcon.getImage(), x, y, this);
				}
			}
			
			backgroundImageIcon.setImage(backgroundImage);
			
			backgroundLabel.setSize(getSize());
			logoLabel.setSize(logoLabel.getPreferredSize());
			
			firstDisplay = false;
		}
		
		// Set the backgroundLabel size to the size of the desktop
		
		backgroundLabel.setSize(getSize());
		
		// Recenter the logo on the window
		
		int Xpos = ((mainWindow.getWidth() - logoLabel.getWidth()) / 2) - (int) getLocation().getX();
		int Ypos = ((mainWindow.getHeight() - logoLabel.getHeight()) / 2) - 45;
		logoLabel.setLocation(Xpos, Ypos);
	}
	
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	
	// Creates tool windows
	
	private void prepareToolWindows() {
		
		// Prepare navigator window
		
		NavigatorWindow navWindow = new NavigatorWindow(this);
		try { navWindow.setClosed(true); } 
		catch (java.beans.PropertyVetoException e) {}
		toolWindows.addElement(navWindow);
		
		// Prepare search tool window
		
		SearchWindow searchWindow = new SearchWindow(this);
		try { searchWindow.setClosed(true); }
		catch (java.beans.PropertyVetoException e) {}
		toolWindows.addElement(searchWindow);
	}
	
	// Returns a tool window for a given tool name
	
	private ToolWindow getToolWindow(String toolName) {
		ToolWindow resultWindow = null;
		for (int x=0; x < toolWindows.size(); x++) {
			ToolWindow tempWindow = (ToolWindow) toolWindows.elementAt(x);
			if (tempWindow.getToolName().equals(toolName)) resultWindow = tempWindow;
		}
		return resultWindow;
	}
	
	// Centers the map and the globe on given coordinates
	
	public void centerMapGlobe(Coordinates targetLocation) { ((NavigatorWindow) getToolWindow("Mars Navigator")).updateCoords(targetLocation); }
	
	// Return true if tool window is open
	
	public boolean isToolWindowOpen(String toolName) {
		ToolWindow tempWindow = getToolWindow(toolName);
		if (tempWindow != null) return !tempWindow.isClosed();
		else return false;
	}
	
	// Opens a tool window if necessary
	
	public void openToolWindow(String toolName) {
		ToolWindow tempWindow = getToolWindow(toolName);
		if (tempWindow != null) {
			if (tempWindow.isClosed()) {
				if (tempWindow.hasNotBeenOpened()) {
					tempWindow.setLocation(getRandomLocation(tempWindow));
					tempWindow.setOpened();
				}
				add(tempWindow, 0);
			}
			tempWindow.show();
		}
	}
	
	// Closes a tool window if it is open
	
	public void closeToolWindow(String toolName) {
		ToolWindow tempWindow = getToolWindow(toolName);
		if ((tempWindow != null) && !tempWindow.isClosed()) {
			try { tempWindow.setClosed(true); }
			catch (java.beans.PropertyVetoException e) {}
		}
	}
	
	// Creates and opens a window for a unit if it isn't already in existance and open
	
	public void openUnitWindow(int unitID) {
		
		UnitDialog tempWindow = null;
		
		for (int x=0; x < unitWindows.size(); x++) 
			if (((UnitDialog) unitWindows.elementAt(x)).getUnitID() == unitID) tempWindow = (UnitDialog) unitWindows.elementAt(x);
			
		if (tempWindow != null) { if (tempWindow.isClosed()) add(tempWindow, 0); }
		else {
			tempWindow = mainWindow.getUnitDialog(unitID);
			add(tempWindow, 0);
			try { tempWindow.setSelected(true); } 
			catch (java.beans.PropertyVetoException e) {}
			
			// Set internal frame listener
			
			tempWindow.addInternalFrameListener(new UnitWindowListener(this));
				
			// Put window in random position on desktop
			
			tempWindow.setLocation(getRandomLocation(tempWindow));
			
			// Add unit window to unitWindows vector
			
			unitWindows.addElement(tempWindow);
			
			// Create new unit button in tool bar if necessary
		
			mainWindow.createUnitButton(unitID, tempWindow.getUnitName(), tempWindow.getIcon());
		}
		
		tempWindow.show();
	}
	
	// Disposes a unit window and button
	
	public void disposeUnitWindow(int unitID) {
	
		// Dispose unit window
	
		for (int x=0; x < unitWindows.size(); x++) {
			UnitDialog tempWindow = (UnitDialog) unitWindows.elementAt(x);
			if (tempWindow.getUnitID() == unitID) {
				unitWindows.removeElement(tempWindow);
				tempWindow.dispose();
			}
		}
		
		// Have main window dispose of unit button
		
		mainWindow.disposeUnitButton(unitID);
	}
	
	// Returns an array of unit info for all moving vehicles sorted by name
	
	public UnitInfo[] getMovingVehicleInfo() { return mainWindow.getMovingVehicleInfo(); }
	
	// Returns an array of unit info for all vehicles sorted by name
	
	public UnitInfo[] getVehicleInfo() { return mainWindow.getVehicleInfo(); }
	
	// Returns an array of unit info for all settlements sorted by name
	
	public UnitInfo[] getSettlementInfo() { return mainWindow.getSettlementInfo(); }
	
	// Returns an array of unit info for all people sorted by name
	
	public UnitInfo[] getPeopleInfo() { return mainWindow.getPeopleInfo(); }
	
	// Returns a random location on the desktop for a given JInternalFrame
	
	private Point getRandomLocation(JInternalFrame tempWindow) {
		
		Dimension desktop_size = getSize();
 		Dimension window_size = tempWindow.getSize();
		
		int rX = (int) Math.round(Math.random() * (desktop_size.width - window_size.width));
		int rY = (int) Math.round(Math.random() * (desktop_size.height - window_size.height));
		
		return new Point(rX, rY);
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