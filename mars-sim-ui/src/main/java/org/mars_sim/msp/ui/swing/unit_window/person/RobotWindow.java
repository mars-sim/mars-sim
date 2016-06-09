/**
 * Mars Simulation Project
 * RobotWindow.java
 * @version 3.07 2015-01-21
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

	/** Is robot dead? */
	private boolean dead = false;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 * @param robot the robot for this window.
	 */
	public RobotWindow(MainDesktopPane desktop, Robot robot) {
		// Use UnitWindow constructor
		super(desktop, robot, false);

		// Add tab panels
		addTabPanel(new TabPanelActivity(robot, desktop));
		addTabPanel(new TabPanelAttribute(robot, desktop));

		// Add death tab panel if robot is dead.
		if (robot.getSystemCondition().isInoperable()) {
			dead = true;
			addTabPanel(new TabPanelDeath(robot, desktop));
		}
		else dead = false;

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
		Robot robot = (Robot) unit;
		if (!dead) {
			if (robot.getSystemCondition().isInoperable()) {
				dead = true;
				addTabPanel(new TabPanelDeath(robot, desktop));
			}
		}
	}
}
