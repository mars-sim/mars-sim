/*
 * Mars Simulation Project
 * TabPanelBots.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The TabPanelBots is a tab panel for a vehicle's bots crew information.
 */
@SuppressWarnings("serial")
public class TabPanelBots extends TabPanel {

	private static final String ROBOT_ICON = "robot";

	private JLabel crewNumLabel;
	private JLabel crewCapLabel;
	private UnitListPanel<Robot> crewList;

	private int crewNumCache;
	private int crewCapacityCache;

	/** The Crewable instance. */
	private Crewable crewable;

	/**
	 * Constructor.
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelBots(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(ROBOT_ICON),
			Msg.getString("TabPanelBots.title"), //$NON-NLS-1$
			vehicle, desktop
		);

		crewable = (Crewable) vehicle;

	}

	@Override
	protected void buildUI(JPanel content) {

		// Create crew count panel
		AttributePanel crewCountPanel = new AttributePanel(2);
		content.add(crewCountPanel, BorderLayout.NORTH);

		// Create crew num label
		crewNumCache = crewable.getRobotCrewNum();
		crewNumLabel = crewCountPanel.addTextField(Msg.getString("TabPanelBots.crew"), Integer.toString(crewNumCache), null); //$NON-NLS-1$

		// Create crew capacity label
		crewCapacityCache = crewable.getRobotCrewCapacity();
		crewCapLabel = crewCountPanel.addTextField(Msg.getString("TabPanelBots.crewCapacity"), Integer.toString(crewCapacityCache), null); //$NON-NLS-1$

		// Create crew display panel
		JPanel crewDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(crewDisplayPanel, BorderLayout.CENTER);

		// Create crew list
		crewList = new UnitListPanel<>(getDesktop(), new Dimension(175, 100)) {
			@Override
			protected Collection<Robot> getData() {
				return crewable.getRobotCrew();
			}
		};
		crewDisplayPanel.add(crewList);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		// Update crew num
		if (crewNumCache !=  crewable.getRobotCrewNum()) {
			crewNumCache = crewable.getRobotCrewNum();
			crewNumLabel.setText(Msg.getString("TabPanelBots.crew", crewNumCache)); //$NON-NLS-1$
		}

		// Update crew capacity
		if (crewCapacityCache != crewable.getRobotCrewCapacity()) {
			crewCapacityCache =  crewable.getRobotCrewCapacity();
			crewCapLabel.setText(Msg.getString("TabPanelBots.crewCapacity", crewCapacityCache)); //$NON-NLS-1$
		}

		// Update crew list
		crewList.update();
	}

	@Override
	public void destroy() {
		super.destroy();
		
		crewList = null;
		crewable = null;
	}
}