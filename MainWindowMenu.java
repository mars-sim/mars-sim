//*********************** Main Window Menu ***********************
// Last Modified: 4/9/00

// The MainWindowMenu class is the menu for the main window.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainWindowMenu extends JMenuBar implements ActionListener, MenuListener {

	// Data Members
	
	private MainWindow mainWindow;                // The main window frame
	private JMenuItem exitItem;                   // Exit menu item
	private JCheckBoxMenuItem marsNavigatorItem;  // Mars navigator menu item
	private JCheckBoxMenuItem searchToolItem;     // Search tool menu item
	private JMenuItem aboutMspItem;               // About Mars Simulation Project menu item
	
	// Constructor
	
	public MainWindowMenu(MainWindow mainWindow) {
	
		// Use JMenuBar constructor
	
		super();
		
		// Initialize data members
		
		this.mainWindow = mainWindow;
		
		// Create file menu
		
		JMenu fileMenu = new JMenu("File");
		add(fileMenu);
		
		// Create exit menu item
		
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);
		
		// Create tools menu
		
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.addMenuListener(this);
		add(toolsMenu);
		
		// Create Mars navigator menu item
		
		marsNavigatorItem = new JCheckBoxMenuItem("Mars Navigator");
		marsNavigatorItem.addActionListener(this);
		toolsMenu.add(marsNavigatorItem);
		
		// Create search tool menu item
		
		searchToolItem = new JCheckBoxMenuItem("Search Tool");
		searchToolItem.addActionListener(this);
		toolsMenu.add(searchToolItem);
		
		// Create help menu
		
		JMenu helpMenu = new JMenu("Help");
		add(helpMenu);
		
		// Create about Mars Simulation Project menu item
		
		aboutMspItem = new JMenuItem("About The Mars Simulation Project");
		aboutMspItem.addActionListener(this);
		helpMenu.add(aboutMspItem);
	}
	
	// ActionListener method implementation
	
	public void actionPerformed(ActionEvent event) {
		
		JMenuItem selectedItem = (JMenuItem) event.getSource();
		
		if (selectedItem == exitItem) System.exit(0);
		
		if (selectedItem == marsNavigatorItem) {
			if (marsNavigatorItem.isSelected()) mainWindow.openToolWindow("Mars Navigator");
			else mainWindow.closeToolWindow("Mars Navigator");
		}
		
		if (selectedItem == searchToolItem) {
			if (searchToolItem.isSelected()) mainWindow.openToolWindow("Search Tool");
			else mainWindow.closeToolWindow("Search Tool");
		}
		
		if (selectedItem == aboutMspItem) System.out.println("About The Mars Simulation Project");
	}
	
	// MenuListener method implementations
	
	public void menuSelected(MenuEvent event) { 
		marsNavigatorItem.setSelected(mainWindow.isToolWindowOpen("Mars Navigator"));
		searchToolItem.setSelected(mainWindow.isToolWindowOpen("Search Tool"));
	}
	
	public void menuCanceled(MenuEvent event) {}
	public void menuDeselected(MenuEvent event) {}
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