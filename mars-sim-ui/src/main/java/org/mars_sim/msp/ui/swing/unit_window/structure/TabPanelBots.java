/*
 * Mars Simulation Project
 * TabPanelBots.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * This is a tab panel for robots.
 */
@SuppressWarnings("serial")
public class TabPanelBots extends TabPanel {

	private static final String ROBOT_ICON = "robot";

	private int robotNumCache;
	private int robotCapacityCache;
	private int robotIndoorCache;

	private Settlement settlement;

	private JLabel robotNumLabel;
	private JLabel robotCapLabel;
	private JLabel robotIndoorLabel;
	private UnitListPanel<Robot> robotList;


	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelBots(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelBots.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ROBOT_ICON),
			Msg.getString("TabPanelBots.title"), //$NON-NLS-1$
			desktop
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
		robotCapacityCache = settlement.getRobotCapacity();
		robotCapLabel = countPanel.addTextField(Msg.getString("TabPanelBots.capacity"),
													Integer.toString(robotCapacityCache), null); // $NON-NLS-1$

		// Create spring layout robot display panel
		robotList = new UnitListPanel<>(getDesktop(), new Dimension(175, 200)) {
			@Override
			protected Collection<Robot> getData() {
				return settlement.getRobots();
			}			
		};
		addBorder(robotList, "Robots");
		content.add(robotList, BorderLayout.CENTER);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Update robot num
		if (robotNumCache != settlement.getNumBots()) {
			robotNumCache = settlement.getNumBots();
			robotNumLabel.setText("" + robotNumCache);
			robotIndoorLabel.setText("" + robotNumCache);
		}

		// Update robot capacity
		if (robotCapacityCache != settlement.getRobotCapacity()) {
			robotCapacityCache = settlement.getRobotCapacity();
			robotCapLabel.setText("" + robotCapacityCache);
		}

		// Update robot list
		robotList.update();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		robotList = null;
	}
}
