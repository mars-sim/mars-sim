/*
 * Mars Simulation Project
 * ToolWindow.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.toolWindow;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.commander.CommanderWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

/**
 * The ToolWindow class is an abstract UI window for a tool. Particular tool
 * windows should be derived from this.
 */
@SuppressWarnings("serial")
public abstract class ToolWindow extends JInternalFrame {

	// Data members

	/** True if window is open. */
	protected boolean opened;

	/** The name of the tool the window is for. */
	protected String name;

	/** The main desktop. */
	protected MainDesktopPane desktop;
	protected MonitorWindow monitorWindow;
	protected CommanderWindow commanderWindow;

	protected static Simulation sim = Simulation.instance();
	protected static MasterClock masterClock = sim.getMasterClock();
	protected static UnitManager unitManager = sim.getUnitManager();

	/**
	 * Constructor.
	 *
	 * @param name    the name of the tool
	 * @param desktop the main desktop.
	 */
	public ToolWindow(String name, MainDesktopPane desktop) {

		// use JInternalFrame constructor
		super(name, true, // resizable
				true, // closable
				false, // maximizable
				false // iconifiable
		);

		// Initialize data members
		this.name = name;
		this.desktop = desktop;

		if (this instanceof MonitorWindow)
			this.monitorWindow = (MonitorWindow) this;

		else if (this instanceof CommanderWindow)
			this.commanderWindow = (CommanderWindow) this;

		opened = false;

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		// setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Set internal frame listener
		addInternalFrameListener(new ToolFrameListener());

		// Set the icon
		setIconImage();
	}

	/**
	 * Sets the icon image for the main window.
	 */
	public void setIconImage() {
		super.setFrameIcon(MainWindow.getLanderIcon());
	}

	/**
	 * Gets the tool name.
	 *
	 * @return tool name
	 */
	public String getToolName() {
		return name;
	}

	/**
	 * Sets the tool name.
	 *
	 * @param tool name
	 */
	public void setTitleName(String value) {
		setName(value);
	}

	/**
	 * Checks if the tool window has previously been opened.
	 *
	 * @return true if tool window has previously been opened.
	 */
	public boolean wasOpened() {
		return opened;
	}

	/**
	 * Sets if the window has previously been opened.
	 *
	 * @param opened true if previously opened.
	 */
	public void setWasOpened(boolean opened) {
		this.opened = opened;
	}

	public void closeMaps() {
		if (desktop.isToolWindowOpen(SettlementWindow.NAME)) {
//			mainScene.closeMaps();
		}
	}

	/**
	 * Update window.
	 */
	public void update() {
		if (isVisible() || isShowing()) {
			// Note: need to update the table color style after the theme is changed
//			if (getToolName().equals(MonitorWindow.TITLE))
//				monitorWindow.refreshTableStyle();
				// pack(); // create time lag, and draw artifact

			if (getToolName().equals(CommanderWindow.NAME))
				commanderWindow.update();
		}
		else {
			opened = false;
			if (this.getToolName().equals(NavigatorWindow.NAME))
				desktop.closeToolWindow(NavigatorWindow.NAME);
		}
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		desktop = null;
		masterClock = null;
		monitorWindow = null;
		commanderWindow = null;
	}
}
