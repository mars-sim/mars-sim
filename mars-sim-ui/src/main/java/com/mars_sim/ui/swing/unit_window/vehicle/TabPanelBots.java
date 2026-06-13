/*
 * Mars Simulation Project
 * TabPanelBots.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BaseRobotModel;

/**
 * The TabPanelBots is a tab panel for a vehicle's bots crew information.
 */
@SuppressWarnings("serial")
public class TabPanelBots extends EntityTableTabPanel<Vehicle>
	implements EntityListener {

	private static final String ROBOT_ICON = "robot";
	private BotModel model;

	/**
	 * Constructor.
	 * @param vehicle the vehicle.
	 * @param context the UI context.
	 */
	public TabPanelBots(Vehicle vehicle, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("robot.plural"),
			ImageLoader.getIconByName(ROBOT_ICON),
			null,
			vehicle, context
		);
	}

	/**
	 * Info panel shows the bot crew capacity.
	 */
	@Override
	protected JPanel createInfoPanel() {
		// Create crew count panel
		AttributePanel crewCountPanel = new AttributePanel();

		var crewable = (Crewable)getEntity();

		// Create crew capacity label
		crewCountPanel.addTextField(Msg.getString("TabPanelBots.capacity"),
					Integer.toString(crewable.getRobotCrewCapacity()), null);

		return crewCountPanel;
	}

	/**
	 * Watch for changes in the inventory storing unit.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.INVENTORY_STORING_UNIT_EVENT.equals(event.getType())) {
			model.update();
		}
	}

	@Override
	protected TableModel createModel() {
		if (model == null) {
			model = new BotModel((Crewable)getEntity());
		}

		return model;
	}

	private static class BotModel extends BaseRobotModel {
		private final Crewable crewable;

		public BotModel(Crewable crewable) {
			super(NAME, TASK);
			this.crewable = crewable;
		}

		public void update() {
			SwingUtilities.invokeLater(() -> 
					setEntities(crewable.getRobotCrew()));
		}
	}
}