/**
 * Mars Simulation Project
 * ToolToolBar.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.preferences.PreferencesWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * The ToolToolBar class is a UI toolbar for holding tool buttons. The should
 * only be one instance and is contained in the MainWindow instance.
 */
public class ToolToolBar extends JToolBar implements ActionListener {

	// Data members
	private Vector<ToolButton> toolButtons; // List of tool buttons
	private MainWindow parentMainWindow; // Main window that contains this
											// toolbar.

	/**
	 * Constructs a ToolToolBar object
	 * 
	 * @param parentMainWindow
	 *            the main window pane
	 */
	public ToolToolBar(MainWindow parentMainWindow) {

		// Use JToolBar constructor
		super(JToolBar.HORIZONTAL);

		// Initialize data members
		toolButtons = new Vector<ToolButton>();
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
		});
		add(newButton);

		ToolButton openButton = new ToolButton("Open", "Open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.loadSimulation();
			};
		});
		add(openButton);

		ToolButton saveButton = new ToolButton("Save", "Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.saveSimulation(true);
			};
		});
		add(saveButton);

		ToolButton saveAsButton = new ToolButton("Save As", "SaveAs");
		saveAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.saveSimulation(false);
			};
		});
		add(saveAsButton);
		addSeparator();

		// Add Mars navigator button
		ToolButton navButton = new ToolButton(NavigatorWindow.NAME, "Planet");
		navButton.addActionListener(this);
		add(navButton);
		toolButtons.addElement(navButton);

		// Add search tool button
		ToolButton searchButton = new ToolButton(SearchWindow.NAME, "Find");
		searchButton.addActionListener(this);
		add(searchButton);
		toolButtons.addElement(searchButton);

		// Add time tool button
		ToolButton timeButton = new ToolButton(TimeWindow.NAME, "Time");
		timeButton.addActionListener(this);
		add(timeButton);
		toolButtons.addElement(timeButton);

		// Add monitor tool button
		ToolButton monitorButton = new ToolButton(MonitorWindow.NAME, "Monitor");
		monitorButton.addActionListener(this);
		add(monitorButton);
		toolButtons.addElement(monitorButton);

		// Add prefs tool button
		ToolButton prefsButton = new ToolButton(PreferencesWindow.NAME,
				"Preferences");
		prefsButton.addActionListener(this);
		add(prefsButton);
		toolButtons.addElement(prefsButton);

		// Add mission tool button
		ToolButton missionButton = new ToolButton(MissionWindow.NAME, "Mission");
		missionButton.addActionListener(this);
		add(missionButton);
		toolButtons.addElement(missionButton);

		// Add settlement tool button
		ToolButton settlementButton = new ToolButton(SettlementWindow.NAME, "SettlementMapTool");
		settlementButton.addActionListener(this);
		add(settlementButton);
		toolButtons.addElement(settlementButton);

		// Add science tool button
		ToolButton scienceButton = new ToolButton(ScienceWindow.NAME, "Science");
		scienceButton.addActionListener(this);
		add(scienceButton);
		toolButtons.addElement(scienceButton);

		// Add resupply tool button
		ToolButton resupplyButton = new ToolButton(ResupplyWindow.NAME, "Resupply");
		resupplyButton.addActionListener(this);
		add(resupplyButton);
		toolButtons.addElement(resupplyButton);
		
		addSeparator();

		// Add guide button
		ToolButton guideButton = new ToolButton(GuideWindow.NAME, "Guide");
		guideButton.addActionListener(this);
		add(guideButton);
		toolButtons.addElement(guideButton);

	}

	/** ActionListener method overriden */
	public void actionPerformed(ActionEvent event) {

		// show tool window on desktop
		parentMainWindow.getDesktop().openToolWindow(
				((ToolButton) event.getSource()).getToolName());
	}
}