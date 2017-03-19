/**
 * Mars Simulation Project
 * RobotWindow.java
 * @version 3.1.0 2017-03-19
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The RobotWindow is the window for displaying a robot.
 */
public class RobotWindow
extends UnitWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Is robot inoperable? */
	private boolean inoperableCache = false;

	private Robot robot;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 * @param robot the robot for this window.
	 */
	public RobotWindow(MainDesktopPane desktop, Robot robot) {
		// Use UnitWindow constructor
		super(desktop, robot, false);
		this.robot = robot;

		// Add tab panels
		addTabPanel(new TabPanelActivity(robot, desktop));
		addTabPanel(new TabPanelAttribute(robot, desktop));

		// Add death tab panel if robot is inoperable.
		if (robot.getSystemCondition().isInoperable()) {
			inoperableCache = true;
			addTabPanel(new TabPanelDeath(robot, desktop));
		}
		else
			inoperableCache = false;

		addTabPanel(new InventoryTabPanel(robot, desktop));
		addTopPanel(new LocationTabPanel(robot, desktop));
		// 2015-03-20  Added TabPanelSchedule
		addTabPanel(new TabPanelSchedule(robot, desktop));
		//addTabPanel(new TabPanelSkill(robot, desktop));
		//addTabPanel(new TabPanelHealth(robot, desktop));
		//addTabPanel(new TabPanelGeneral(robot, desktop));

		// 2015-06-20 Added tab sorting
		sortTabPanels();

	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();
		//Robot robot = (Robot) unit;
		if (!inoperableCache) {
			if (robot.getSystemCondition().isInoperable()) {
				inoperableCache = true;
				addTabPanel(new TabPanelDeath(robot, desktop));
			}
		}
	}
}
