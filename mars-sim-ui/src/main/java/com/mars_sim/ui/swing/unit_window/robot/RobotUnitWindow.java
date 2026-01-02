/*
 * Mars Simulation Project
 * RobotWindow.java
 * @date 2025-08-11
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.robot;

import java.util.Properties;

import com.mars_sim.core.robot.Robot;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelActivity;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelAttribute;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelSchedule;
import com.mars_sim.ui.swing.entitywindow.worker.TabPanelSkill;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;

/**
 * The RobotWindow is the window for displaying a robot.
 */
@SuppressWarnings("serial")
public class RobotUnitWindow extends EntityContentPanel<Robot> {

	/**
	 * Constructor.
	 * 
	 * @param robot   the robot for this window.
	 * @param context the UI context.
	 * @param props   Saved properties for the window.
	 */
	public RobotUnitWindow(Robot robot, UIContext context, Properties props) {
		// Use UnitWindow constructor
		super(robot, context);
					
		// Add tab panels
		addTabPanel(new TabPanelGeneralRobot(robot, context));

		addTabPanel(new TabPanelActivity(robot, context));
		addTabPanel(new TabPanelAttribute(robot, context));
		addTabPanel(new MaintenanceTabPanel(robot, context));
		addTabPanel(new MalfunctionTabPanel(robot, context));

		addTabPanel(new InventoryTabPanel(robot, context));
		addTabPanel(new LocationTabPanel(robot, context));
		addTabPanel(new NotesTabPanel(robot, context));
		addTabPanel(new TabPanelSchedule(robot, context));
		addTabPanel(new TabPanelSkill(robot, context));
		
		applyProps(props);
	}
}
