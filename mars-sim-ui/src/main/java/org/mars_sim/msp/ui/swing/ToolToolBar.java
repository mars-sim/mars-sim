/**
 * Mars Simulation Project
 * ToolToolBar.java
 * @version 3.1.0 2019-02-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.tool.commander.CommanderWindow;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;

/**
 * The ToolToolBar class is a UI toolbar for holding tool buttons. There should
 * only be one instance and it is contained in the {@link MainWindow} instance.
 */
public class ToolToolBar
extends JToolBar
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** List of tool buttons. */
	private Vector<ToolButton> toolButtons;
	/** Main window that contains this toolbar. */
	private MainWindow parentMainWindow;

	/**
	 * Constructs a ToolToolBar object
	 * @param parentMainWindow the main window pane
	 */
	public ToolToolBar(MainWindow parentMainWindow) {

		// Use JToolBar constructor
		super(JToolBar.HORIZONTAL);

		// Initialize data members
		toolButtons = new Vector<ToolButton>();
		this.parentMainWindow = parentMainWindow;

		// Set name
		setName(Msg.getString("ToolToolBar.toolbar")); //$NON-NLS-1$

		// Fix tool bar
		setFloatable(false);

		setPreferredSize(new Dimension(0, 32));

		// Prepare tool buttons
		prepareToolButtons();

		// Set border around toolbar
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}
          
	/** Prepares tool buttons */
	private void prepareToolButtons() {

		// Add utilise buttons

		ToolButton openButton = new ToolButton(Msg.getString("mainMenu.open"), Msg.getString("img.open")); //$NON-NLS-1$ //$NON-NLS-2$
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.loadSimulation(false);
			};
		});
		add(openButton);
		
		ToolButton openAutosaveButton = new ToolButton(Msg.getString("mainMenu.openAutosave"), Msg.getString("img.openAutosave")); //$NON-NLS-1$ //$NON-NLS-2$
		openAutosaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.loadSimulation(true);
			};
		});
		add(openAutosaveButton);

		ToolButton saveButton = new ToolButton(Msg.getString("mainMenu.save"), Msg.getString("img.save")); //$NON-NLS-1$ //$NON-NLS-2$
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.saveSimulation(true, false);
			};
		});
		add(saveButton);

		ToolButton saveAsButton = new ToolButton(Msg.getString("mainMenu.saveAs"), Msg.getString("img.saveAs")); //$NON-NLS-1$ //$NON-NLS-2$
		saveAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.saveSimulation(false, false);
			};
		});
		add(saveAsButton);

		ToolButton exitButton = new ToolButton(Msg.getString("mainMenu.exit"), Msg.getString("img.exit")); //$NON-NLS-1$ //$NON-NLS-2$
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentMainWindow.exitSimulation();
			};
		});
		add(exitButton);

		addSeparator(new Dimension(20, 20));

		// Add Mars navigator button
		ToolButton navButton = new ToolButton(NavigatorWindow.NAME, Msg.getString("img.planet")); //$NON-NLS-1$
		navButton.addActionListener(this);
		add(navButton);
		toolButtons.addElement(navButton);

		// Add search tool button
		ToolButton searchButton = new ToolButton(SearchWindow.NAME, Msg.getString("img.find")); //$NON-NLS-1$
		searchButton.addActionListener(this);
		add(searchButton);
		toolButtons.addElement(searchButton);

		// Add time tool button
		ToolButton timeButton = new ToolButton(TimeWindow.NAME, Msg.getString("img.time")); //$NON-NLS-1$
		timeButton.addActionListener(this);
		add(timeButton);
		toolButtons.addElement(timeButton);

		// Add monitor tool button
		ToolButton monitorButton = new ToolButton(MonitorWindow.NAME, Msg.getString("img.monitor")); //$NON-NLS-1$
		monitorButton.addActionListener(this);
		add(monitorButton);
		toolButtons.addElement(monitorButton);

		// Add mission tool button
		ToolButton missionButton = new ToolButton(MissionWindow.NAME, Msg.getString("img.mission")); //$NON-NLS-1$
		missionButton.addActionListener(this);
		add(missionButton);
		toolButtons.addElement(missionButton);

		// Add settlement tool button
		ToolButton settlementButton = new ToolButton(SettlementWindow.NAME, Msg.getString("img.settlementMapTool")); //$NON-NLS-1$
		settlementButton.addActionListener(this);
		add(settlementButton);
		toolButtons.addElement(settlementButton);

		// Add science tool button
		ToolButton scienceButton = new ToolButton(ScienceWindow.NAME, Msg.getString("img.science")); //$NON-NLS-1$
		scienceButton.addActionListener(this);
		add(scienceButton);
		toolButtons.addElement(scienceButton);

		// Add resupply tool button
		ToolButton resupplyButton = new ToolButton(ResupplyWindow.NAME, Msg.getString("img.resupply")); //$NON-NLS-1$
		resupplyButton.addActionListener(this);
		add(resupplyButton);
		toolButtons.addElement(resupplyButton);

		// Add commander dashboard button
		ToolButton dashboardButton = new ToolButton(CommanderWindow.NAME, Msg.getString("img.dashboard")); //$NON-NLS-1$
		dashboardButton.addActionListener(this);
		add(dashboardButton);
		toolButtons.addElement(dashboardButton);
		
		addSeparator();

		// Add guide button
		ToolButton guideButton = new ToolButton(GuideWindow.NAME, Msg.getString("img.guide")); //$NON-NLS-1$
		guideButton.addActionListener(this);
		add(guideButton);
		toolButtons.addElement(guideButton);

	}

	/** ActionListener method overridden */
	@Override
	public void actionPerformed(ActionEvent event) {
		// show tool window on desktop
		parentMainWindow.getDesktop().openToolWindow(
			((ToolButton) event.getSource()).getToolName()
		);
	}
}