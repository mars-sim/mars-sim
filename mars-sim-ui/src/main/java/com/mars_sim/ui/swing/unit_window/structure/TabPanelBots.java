/*
 * Mars Simulation Project
 * TabPanelBots.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BaseRobotModel;

/**
 * This is a tab panel for robots.
 */
@SuppressWarnings("serial")
class TabPanelBots extends EntityTableTabPanel<Settlement> implements TemporalComponent {

	private static final String ROBOT_ICON = "robot";

	private JIntegerLabel robotNumLabel;
	private BotModel model;


	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelBots(Settlement unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("robot.plural"), // $NON-NLS-1$
			ImageLoader.getIconByName(ROBOT_ICON), null,
			unit, context
		);
	}

	
	/**
	 * Info panel shows the bot count.
	 */
	@Override
	protected JPanel createInfoPanel() {

		// Prepare count spring layout panel.
		AttributePanel countPanel = new AttributePanel(3);

		// Create robot num label
		robotNumLabel = new JIntegerLabel(getEntity().getNumBots());
		countPanel.addLabelledItem(Msg.getString("TabPanelBots.associated"), robotNumLabel, null); // $NON-NLS-1$

		countPanel.addTextField(Msg.getString("TabPanelBots.capacity"),
											Integer.toString(getEntity().getRobotCapacity()), null); // $NON-NLS-1$
		return countPanel;
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Update robot num
		robotNumLabel.setValue(getEntity().getNumBots());
	
		// Update robot list
		model.update();
	}


	@Override
	protected TableModel createModel() {
		if (model == null) {
			model = new BotModel(getEntity());
		}

		return model;
	}

	@Override
	public void destroy() {
		if (model != null) {
			model.release();
		}
		super.destroy();
	}

	private static class BotModel extends BaseRobotModel {
		private final Settlement home;

		public BotModel(Settlement home) {
			super(NAME, TASK);
			this.home = home;
		}

		public void update() {
			SwingUtilities.invokeLater(() -> 
					setEntities(home.getAllAssociatedRobots()));
		}
	}
}
