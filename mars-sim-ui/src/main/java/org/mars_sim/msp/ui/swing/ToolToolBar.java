/**
 * Mars Simulation Project
 * ToolToolBar.java
 * @version 3.1.0 2019-02-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
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

import com.alee.extended.button.WebSwitch;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The ToolToolBar class is a UI toolbar for holding tool buttons. There should
 * only be one instance and it is contained in the {@link MainWindow} instance.
 */
public class ToolToolBar extends WebToolBar implements ActionListener, ClockListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static final int ICON_H = 16;
//	private static final int ICON_W = 16;
//	private static final int EMPTY_W = GameManager.mode == GameMode.COMMAND  ? MainWindow.WIDTH - (15 + 4) * 18 - 330 : MainWindow.WIDTH - (15 + 4) * 18 - 300;//735;
//	private static final int EMPTY_H = 32;
	
	// Data members
	/** List of tool buttons. */
	private Vector<ToolButton> toolButtons;
	
	/** WebSwitch for the control of play or pause the simulation*/
	private WebSwitch webSwitch;
	
	/** Main window that contains this toolbar. */
	private MainWindow parentMainWindow;
	
	private MasterClock masterClock;

	/**
	 * Constructs a ToolToolBar object
	 * @param parentMainWindow the main window pane
	 */
	public ToolToolBar(MainWindow parentMainWindow) {

		// Use JToolBar constructor
		super(JToolBar.HORIZONTAL);

		setStyleId(StyleId.toolbarAttachedNorth);
		
		// Initialize data members
		masterClock = Simulation.instance().getMasterClock();
		
		// Initialize data members
		toolButtons = new Vector<ToolButton>();
		this.parentMainWindow = parentMainWindow;
		
		// Set name
		setName(Msg.getString("ToolToolBar.toolbar")); //$NON-NLS-1$
		// Fix tool bar
		setFloatable(false);

		setPreferredSize(new Dimension(0, 32));

		setOpaque(false);
		
		setBackground(new Color(0,0,0,128));
		
		// Prepare tool buttons
		prepareToolButtons();

		// Set border around toolbar
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}
          
	/** Prepares tool buttons */
	private void prepareToolButtons() {

//		ToolButton openButton = new ToolButton(Msg.getString("mainMenu.open"), Msg.getString("img.open")); //$NON-NLS-1$ //$NON-NLS-2$
//		openButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				parentMainWindow.loadSimulation(false);
//			};
//		});
//		add(openButton);
//		
//		ToolButton openAutosaveButton = new ToolButton(Msg.getString("mainMenu.openAutosave"), Msg.getString("img.openAutosave")); //$NON-NLS-1$ //$NON-NLS-2$
//		openAutosaveButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				parentMainWindow.loadSimulation(true);
//			};
//		});
//		add(openAutosaveButton);

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

		// Add the command dashboard button
		if (GameManager.mode == GameMode.COMMAND) {
			// Add commander dashboard button
			ToolButton dashboardButton = new ToolButton(CommanderWindow.NAME, Msg.getString("img.dashboard")); //$NON-NLS-1$
			dashboardButton.addActionListener(this);
			add(dashboardButton);
			toolButtons.addElement(dashboardButton);
		}
		
//		addSeparator();

		webSwitch = new WebSwitch(true);
		webSwitch.setSwitchComponents(
//				new SvgIcon("play16"), new SvgIcon("pause16"));
				ImageLoader.getIcon(Msg.getString("img.speed.play")), 
				ImageLoader.getIcon(Msg.getString("img.speed.pause")));
		TooltipManager.setTooltip(webSwitch, "Pause or Resume the Simulation", TooltipWay.down);
		webSwitch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				masterClock.setPaused(!masterClock.isPaused(), false);
				if (webSwitch.isSelected())
					masterClock.setPaused(false, false);
				else
					masterClock.setPaused(true, false);
			};
		});
			
		addToEnd(webSwitch);
		
		addSeparatorToMiddle();

		// Add guide button
		ToolButton guideButton = new ToolButton(GuideWindow.NAME, Msg.getString("img.guide")); //$NON-NLS-1$
		guideButton.addActionListener(this);
		addToEnd(guideButton);
		toolButtons.addElement(guideButton);

//		addSeparator();

//		JPanel emptyPanel = new JPanel();
//		emptyPanel.setPreferredSize(new Dimension(EMPTY_W, EMPTY_H));
//		add(emptyPanel);
		 
//		add(Box.createGlue());
//		
//		addSeparator();
		
//		ToolButton slowDownButton = new ToolButton("Slow Down", Msg.getString("img.speed.slowDown")); //$NON-NLS-1$ //$NON-NLS-2$
////		slowDownButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		slowDownButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				int ratio = (int)masterClock.getTimeRatio();
//				if (ratio >= 2)
//					masterClock.setTimeRatio(ratio/2.0);
//			};
//		});
//		add(slowDownButton);
//
//		ToolButton pauseButton = new ToolButton("Pause", Msg.getString("img.speed.pause")); //$NON-NLS-1$ //$NON-NLS-2$
////		pauseButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		pauseButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (!masterClock.isPaused())
//					masterClock.setPaused(true, false);
//			};
//		});
//		add(pauseButton);
//		
//		
//		ToolButton resumeButton = new ToolButton("Resume", Msg.getString("img.speed.play")); //$NON-NLS-1$ //$NON-NLS-2$
////		resumeButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		resumeButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (masterClock.isPaused())
//					masterClock.setPaused(false, false);
//			};
//		});
//		add(resumeButton);
//		
//		ToolButton speedUpButton = new ToolButton("Speed Up", Msg.getString("img.speed.speedUp")); //$NON-NLS-1$ //$NON-NLS-2$
////		speedUpButton.setPreferredSize(new Dimension(ICON_W, ICON_H));
//		speedUpButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				int ratio = (int)masterClock.getTimeRatio();
//				if (ratio <= 4096)
//					masterClock.setTimeRatio(ratio*2.0);
//			};
//		});
//		add(speedUpButton);
		
//		addToEnd(new MagnifierToggleTool(parentMainWindow.getFrame()) );
	}

	/** ActionListener method overridden */
	@Override
	public void actionPerformed(ActionEvent event) {
		// show tool window on desktop
		parentMainWindow.getDesktop().openToolWindow(
			((ToolButton) event.getSource()).getToolName()
		);
	}
	
	/**
	 * Change the pause status. Called by Masterclock's firePauseChange() since
	 * TimeWindow is on clocklistener.
	 * 
	 * @param isPaused true if set to pause
	 * @param showPane true if the pane will show up
	 */
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// Update pause/resume webswitch buttons, based on masterclock's pause state.
		if (isPaused) {
			// To pause
			webSwitch.setSelected(false);

		} else {
			// To play or to resume 
			webSwitch.setSelected(false);
		}
	}

	@Override
	public void clockPulse(double time) {
	}

	@Override
	public void uiPulse(double time) {
	}
	
	public void destroy() {
		toolButtons.clear();
		toolButtons = null;
		parentMainWindow = null;
		masterClock = null;
	}
}