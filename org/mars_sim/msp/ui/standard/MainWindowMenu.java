/**
 * Mars Simulation Project
 * MainWindowMenu.java
 * @version 2.71 2000-10-07
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/** The MainWindowMenu class is the menu for the main window. 
 */
public class MainWindowMenu extends JMenuBar implements ActionListener, MenuListener {

    // Data members
    private MainWindow mainWindow;                // The main window frame
    private JMenuItem exitItem;                   // Exit menu item
    private JCheckBoxMenuItem marsNavigatorItem;  // Mars navigator menu item
    private JCheckBoxMenuItem searchToolItem;     // Search tool menu item
    private JCheckBoxMenuItem terrainFromWebItem; // origin of terrain image
    private JMenuItem aboutMspItem;               // About Mars Simulation Project menu item
	
    /** Constructs a MainWindowMenu object 
     *  @mainWindow the main window pane
     */
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
		
	// Create options menu
	JMenu optionsMenu = new JMenu("Options");
	add(optionsMenu);
		
	// choose terrain image source from web
	terrainFromWebItem = new JCheckBoxMenuItem("Use USGS Map", false);
	terrainFromWebItem.addActionListener(this);
	optionsMenu.add(terrainFromWebItem);
		
	// Create help menu
	JMenu helpMenu = new JMenu("Help");
	add(helpMenu);
		
	// Create about Mars Simulation Project menu item
	aboutMspItem = new JMenuItem("About The Mars Simulation Project");
	aboutMspItem.addActionListener(this);
	helpMenu.add(aboutMspItem);
    }
	
    // ActionListener method overriding
    public void actionPerformed(ActionEvent event) {
		
	JMenuItem selectedItem = (JMenuItem) event.getSource();
		
	if (selectedItem == exitItem) System.exit(0);
		
	if (selectedItem == marsNavigatorItem) {
	    if (marsNavigatorItem.isSelected()) {
		mainWindow.openToolWindow("Mars Navigator");
	    } else {
		mainWindow.closeToolWindow("Mars Navigator");
	    }
	}
		
	if (selectedItem == searchToolItem) {
	    if (searchToolItem.isSelected()) {
		mainWindow.openToolWindow("Search Tool");
	    } else {
		mainWindow.closeToolWindow("Search Tool");
	    }
	}
		
	if (selectedItem == terrainFromWebItem) {
	    NavigatorWindow nw = (NavigatorWindow)(mainWindow.getToolWindow("Mars Navigator"));
	    nw.setUSGSMap(terrainFromWebItem.isSelected());
	}
		
	if (selectedItem == aboutMspItem) new AboutDialog(mainWindow);
    }
	
    // MenuListener method overriding
    public void menuSelected(MenuEvent event) { 
	marsNavigatorItem.setSelected(mainWindow.isToolWindowOpen("Mars Navigator"));
	searchToolItem.setSelected(mainWindow.isToolWindowOpen("Search Tool"));
    }
	
    public void menuCanceled(MenuEvent event) {}
    public void menuDeselected(MenuEvent event) {}
}
