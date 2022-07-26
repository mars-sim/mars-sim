/**
 * Mars Simulation Project
 * RobotWindow.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.NotesTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The RobotWindow is the window for displaying a robot.
 */
@SuppressWarnings("serial")
public class RobotWindow extends UnitWindow {

	/** Is robot inoperable? */
	private boolean inoperableCache = false;
	
	private Robot robot;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop panel.
	 * @param robot   the robot for this window.
	 */
	public RobotWindow(MainDesktopPane desktop, Robot robot) {
		// Use UnitWindow constructor
		super(desktop, robot, robot.getSettlement().getName() + " - " + robot.getNickName(), false);
		this.robot = robot;

		// Add tab panels
		addTabPanel(new TabPanelActivity(robot, desktop));
		addTabPanel(new TabPanelAttribute(robot, desktop));

		// Add death tab panel if robot is inoperable.
		if (robot.getSystemCondition().isInoperable()) {
			inoperableCache = true;
			addTabPanel(new TabPanelDeath(robot, desktop));
		} else
			inoperableCache = false;

		addTabPanel(new InventoryTabPanel(robot, desktop));
		addTopPanel(new LocationTabPanel(robot, desktop));
		addTabPanel(new NotesTabPanel(robot, desktop));
		addTabPanel(new TabPanelSchedule(robot, desktop));
		addTabPanel(new TabPanelSkill(robot, desktop));

		// Add tab sorting
//		sortTabPanels();
		// Add to tab panels. 
		addTabIconPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();

		if (!inoperableCache) {
			if (robot.getSystemCondition().isInoperable()) {
				inoperableCache = true;
				addTabPanel(new TabPanelDeath(robot, desktop));
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// SwingUtilities.updateComponentTreeUI(this);
//		TabPanel newTab = getSelected();
//
//		if (newTab != oldTab) {
//
//			if (newTab instanceof TabPanelActivity) {
////				if (tabPanelActivity.isUIDone());
////				 	tabPanelActivity.initializeUI();
//			} else if (newTab instanceof TabPanelAttribute) {
//				
//			}
//		}
	}
}
