/*
 * Mars Simulation Project
 * TabPanelBots.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

import com.alee.laf.panel.WebPanel;

/**
 * This is a tab panel for robots.
 */
@SuppressWarnings("serial")
public class TabPanelBots extends TabPanel {

	private int robotNumCache;
	private int robotCapacityCache;
	private int robotIndoorCache;

	private Settlement settlement;

	private JTextField robotNumLabel;
	private JTextField robotCapLabel;
	private JTextField robotIndoorLabel;
	private UnitListPanel<Robot> robotList;


	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelBots(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelBots.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelBots.tooltip"), //$NON-NLS-1$
				unit, desktop);

		settlement = (Settlement) unit;
	}

	@Override
	protected void buildUI(JPanel content) {

		// Prepare count spring layout panel.
		WebPanel countPanel = new WebPanel(new SpringLayout());
		content.add(countPanel, BorderLayout.NORTH);

		// Create robot num label
		robotNumCache = settlement.getNumBots();
		robotNumLabel = addTextField(countPanel, Msg.getString("TabPanelBots.associated"), robotNumCache, null); // $NON-NLS-1$

		// Create robot indoor label
		robotIndoorCache = settlement.getNumBots();
		robotIndoorLabel = addTextField(countPanel, Msg.getString("TabPanelBots.indoor"), robotIndoorCache, null);

		// Create robot capacity label
		robotCapacityCache = settlement.getRobotCapacity();
		robotCapLabel = addTextField(countPanel, Msg.getString("TabPanelBots.capacity"), robotCapacityCache, null); // $NON-NLS-1$

		// Set up the spring layout.
		SpringUtilities.makeCompactGrid(countPanel, 3, 2, // rows, cols
				INITX_DEFAULT, INITY_DEFAULT, // initX, initY
				XPAD_DEFAULT, YPAD_DEFAULT); // xPad, yPad

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
