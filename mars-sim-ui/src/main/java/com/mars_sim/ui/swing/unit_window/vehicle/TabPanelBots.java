/*
 * Mars Simulation Project
 * TabPanelBots.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelBots is a tab panel for a vehicle's bots crew information.
 */
@SuppressWarnings("serial")
public class TabPanelBots extends EntityTabPanel<Vehicle>
	implements EntityListener {

	private static final String ROBOT_ICON = "robot";

	private UnitListPanel<Robot> crewList;

	/**
	 * Constructor.
	 * @param vehicle the vehicle.
	 * @param context the UI context.
	 */
	public TabPanelBots(Vehicle vehicle, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("Robot.plural"),
			ImageLoader.getIconByName(ROBOT_ICON),
			null,
			context, vehicle
		);
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create crew count panel
		AttributePanel crewCountPanel = new AttributePanel();
		content.add(crewCountPanel, BorderLayout.NORTH);

		var crewable = (Crewable)getEntity();

		// Create crew capacity label
		crewCountPanel.addTextField(Msg.getString("TabPanelBots.capacity"),
					Integer.toString(crewable.getRobotCrewCapacity()), null);

		// Create crew display panel
		JPanel crewDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(crewDisplayPanel, BorderLayout.CENTER);

		// Create crew list
		crewList = new UnitListPanel<>(getContext(), new Dimension(175, 100)) {
			@Override
			protected Collection<Robot> getData() {
				return crewable.getRobotCrew();
			}
		};
		crewDisplayPanel.add(crewList);
	}

	/**
	 * What for changes in the inventory storing unit.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.INVENTORY_STORING_UNIT_EVENT.equals(event.getType())) {
			crewList.update();
		}
	}
}