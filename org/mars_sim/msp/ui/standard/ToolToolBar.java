/**
 * Mars Simulation Project
 * ToolToolBar.java
 * @version 2.72 2001-04-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/** The ToolToolBar class is a UI toolbar for holding tool buttons.
 *  The should only be one instance and is contained in the MainWindow instance.
 */
public class ToolToolBar extends JToolBar implements ActionListener {

	// Data members
	private Vector toolButtons;          // List of tool buttons
	private MainWindow parentMainWindow; // Main window that contains this toolbar.
	
	/** Constructs a ToolToolBar object 
     *  @param parentMainWindow the main window pane
     */
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
	
	/** Prepares tool buttons */
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

                
		// Add time tool button
		ToolButton timeButton = new ToolButton("Time Tool", "images/SearchIcon.gif");
		timeButton.addActionListener(this);
		add(timeButton);
		toolButtons.addElement(timeButton);
	}
	
	/** ActionListener method overriden */
	public void actionPerformed(ActionEvent event) {
	
		// show tool window on desktop
		parentMainWindow.openToolWindow(((ToolButton) event.getSource()).getToolName()); 
	}
}
