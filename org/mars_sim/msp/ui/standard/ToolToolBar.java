/**
 * Mars Simulation Project
 * ToolToolBar.java
 * @version 2.72 2001-04-07
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
		super(JToolBar.HORIZONTAL);

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

        // Add utilise buttons
 		ToolButton newButton = new ToolButton("New", "New");
		newButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        parentMainWindow.newSimulation();
                    };
                } );
		add(newButton);

        ToolButton openButton = new ToolButton("Open", "Open");
		openButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        parentMainWindow.loadSimulation();
                    };
                } );
		add(openButton);

 		ToolButton saveButton = new ToolButton("Save", "Save");
		saveButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        parentMainWindow.saveSimulation(true);
                    };
                } );
		add(saveButton);

        ToolButton saveAsButton = new ToolButton("Save As", "SaveAs");
		saveAsButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        parentMainWindow.saveSimulation(false);
                    };
                } );
		add(saveAsButton);
        addSeparator();

		// Add Mars navigator button
		ToolButton navButton = new ToolButton("Mars Navigator", "Planet");
		navButton.addActionListener(this);
		add(navButton);
		toolButtons.addElement(navButton);

		// Add search tool button
		ToolButton searchButton = new ToolButton("Search Tool", "Find");
		searchButton.addActionListener(this);
		add(searchButton);
		toolButtons.addElement(searchButton);


		// Add time tool button
		ToolButton timeButton = new ToolButton("Time Tool", "Time");
		timeButton.addActionListener(this);
		add(timeButton);
		toolButtons.addElement(timeButton);

		// Add monitor tool button
		ToolButton monitorButton = new ToolButton("Monitor Tool", "Monitor");
		monitorButton.addActionListener(this);
		add(monitorButton);
		toolButtons.addElement(monitorButton);
	}

	/** ActionListener method overriden */
	public void actionPerformed(ActionEvent event) {

		// show tool window on desktop
		parentMainWindow.openToolWindow(((ToolButton) event.getSource()).getToolName());
	}
}
