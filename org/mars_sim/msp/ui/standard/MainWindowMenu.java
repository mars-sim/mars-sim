/**
 * Mars Simulation Project
 * MainWindowMenu.java
 * @version 2.72 2001-07-14
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.mars_sim.msp.simulation.VirtualMars;

/** The MainWindowMenu class is the menu for the main window. 
 */
public class MainWindowMenu extends JMenuBar implements ActionListener, MenuListener {

    // Data members
    private MainWindow mainWindow;                // The main window frame
    private JMenuItem loadItem;                   // Load menu item
    private JMenuItem saveItem;                   // Save menu item
    private JMenuItem saveAsItem;                 // Save As menu item
    private JMenuItem exitItem;                   // Exit menu item
    private JCheckBoxMenuItem marsNavigatorItem;  // Mars navigator menu item
    private JCheckBoxMenuItem searchToolItem;     // Search tool menu item
    private JCheckBoxMenuItem timeToolItem;       // Time tool menu item
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
		
	// Create load menu item
	loadItem = new JMenuItem("Load");
	loadItem.addActionListener(this);
	fileMenu.add(loadItem);
		
	// Create save menu item
	saveItem = new JMenuItem("Save");
	saveItem.addActionListener(this);
	fileMenu.add(saveItem);
		
	// Create save as menu item
	saveAsItem = new JMenuItem("Save As");
	saveAsItem.addActionListener(this);
	fileMenu.add(saveAsItem);
		
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
		
    // Create time tool menu item
    timeToolItem = new JCheckBoxMenuItem("Time Tool");
    timeToolItem.addActionListener(this);
    toolsMenu.add(timeToolItem);    
        
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
		
	    if (selectedItem == exitItem) {
		    mainWindow.exitSimulation();
		}
		
        try{ 
	        if (selectedItem == saveItem) {
		        mainWindow.getVirtualMars().store(null);
		    }
	        else if (selectedItem == saveAsItem) {
                VirtualMars mars = mainWindow.getVirtualMars(); 
                JFileChooser chooser = new JFileChooser(mars.DEFAULT_DIR);
                chooser.setDialogTitle("Selected storage location");
                int returnVal = chooser.showSaveDialog(mainWindow);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
		            mars.store(chooser.getSelectedFile());
                }
		    }
	        else if (selectedItem == loadItem) {
                JFileChooser chooser = new JFileChooser(VirtualMars.DEFAULT_DIR);
                chooser.setDialogTitle("Selected stored simulation");
                int returnVal = chooser.showOpenDialog(mainWindow);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    VirtualMars mars = VirtualMars.load(chooser.getSelectedFile());
		            if (mars != null) {
                        mainWindow.setVirtualMars(mars);
                        mars.start();
                    }
                }
		    }
		
		}
		catch(Exception e) {
            e.printStackTrace();
		    JOptionPane.showMessageDialog(null, "Error saving state",
                    e.toString(), JOptionPane.ERROR_MESSAGE);
		}
		
	    if (selectedItem == marsNavigatorItem) {
	        if (marsNavigatorItem.isSelected()) {
		        mainWindow.openToolWindow("Mars Navigator");
	        } 
            else {
		        mainWindow.closeToolWindow("Mars Navigator");
	        }
	    }
		
	    if (selectedItem == searchToolItem) {
	        if (searchToolItem.isSelected()) {
		        mainWindow.openToolWindow("Search Tool");
	        } 
            else {
		        mainWindow.closeToolWindow("Search Tool");
	        }
	    }
		
        if (selectedItem == timeToolItem) {
            if (timeToolItem.isSelected()) {
                mainWindow.openToolWindow("Time Tool");
            }
            else {
                mainWindow.closeToolWindow("Time Tool");
            }
        }
        
	    if (selectedItem == aboutMspItem) new AboutDialog(mainWindow);
    }
	
    // MenuListener method overriding
    public void menuSelected(MenuEvent event) { 
	    marsNavigatorItem.setSelected(mainWindow.isToolWindowOpen("Mars Navigator"));
	    searchToolItem.setSelected(mainWindow.isToolWindowOpen("Search Tool"));
        timeToolItem.setSelected(mainWindow.isToolWindowOpen("Time Tool"));
    }
	
    public void menuCanceled(MenuEvent event) {}
    public void menuDeselected(MenuEvent event) {}
}
