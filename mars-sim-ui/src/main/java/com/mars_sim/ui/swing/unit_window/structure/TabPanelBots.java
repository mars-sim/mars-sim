/*
 * Mars Simulation Project
 * TabPanelBots.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This is a tab panel for robots.
 */
@SuppressWarnings("serial")
class TabPanelBots extends EntityTabPanel<Settlement> implements TemporalComponent {

	private static final String ROBOT_ICON = "robot";

	private int robotNumCache;
	private int robotIndoorCache;

	private Settlement settlement;

	private JLabel robotNumLabel;
	private JLabel robotIndoorLabel;
	private UnitListPanel<Robot> robotList;


	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelBots(Settlement unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("Robot.plural"), // $NON-NLS-1$
			ImageLoader.getIconByName(ROBOT_ICON), null,
			context, unit
		);

		settlement = unit;
	}

	@Override
	protected void buildUI(JPanel content) {

		// Prepare count spring layout panel.
		AttributePanel countPanel = new AttributePanel(3);
		content.add(countPanel, BorderLayout.NORTH);

		// Create robot num label
		robotNumCache = settlement.getNumBots();
		robotNumLabel = countPanel.addTextField(Msg.getString("TabPanelBots.associated"),
													Integer.toString(robotNumCache), null); // $NON-NLS-1$

		// Create robot indoor label
		robotIndoorCache = settlement.getNumBots();
		robotIndoorLabel = countPanel.addTextField(Msg.getString("TabPanelBots.indoor"),
													Integer.toString(robotIndoorCache), null);

		// Create robot capacity label
		countPanel.addTextField(Msg.getString("TabPanelBots.capacity"),
											Integer.toString(settlement.getRobotCapacity()), null); // $NON-NLS-1$

		// Create spring layout robot display panel
		robotList = new UnitListPanel<>(getContext(), new Dimension(175, 200)) {
			@Override
			protected Collection<Robot> getData() {
				return settlement.getAllAssociatedRobots();
			}			
		};
		robotList.setBorder(SwingHelper.createLabelBorder(Msg.getString("Robot.plural")));
		content.add(robotList, BorderLayout.CENTER);
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Update robot num
		if (robotNumCache != settlement.getNumBots()) {
			robotNumCache = settlement.getNumBots();
			robotNumLabel.setText(Integer.toString(robotNumCache));
			robotIndoorLabel.setText(Integer.toString(robotIndoorCache));
		}

		// Update robot list
		robotList.update();
	}
}
