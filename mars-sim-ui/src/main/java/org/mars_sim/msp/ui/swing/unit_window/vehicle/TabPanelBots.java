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
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

/**
 * The TabPanelBots is a tab panel for a vehicle's bots crew information.
 */
@SuppressWarnings("serial")
public class TabPanelBots extends TabPanel {

	private static final String ROBOT_ICON = Msg.getString("icon.robot"); //$NON-NLS-1$

	private JTextField crewNumLabel;
	private JTextField crewCapLabel;
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
			ImageLoader.getNewIcon(ROBOT_ICON),
			Msg.getString("TabPanelBots.title"), //$NON-NLS-1$
			vehicle, desktop
		);

		crewable = (Crewable) vehicle;

	}

	@Override
	protected void buildUI(JPanel content) {

		// Create crew count panel
		JPanel crewCountPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		content.add(crewCountPanel, BorderLayout.NORTH);

		// Create crew num label
		crewNumCache = crewable.getRobotCrewNum();
		crewNumLabel = addTextField(crewCountPanel, Msg.getString("TabPanelBots.crew"), crewNumCache, null); //$NON-NLS-1$

		// Create crew capacity label
		crewCapacityCache = crewable.getRobotCrewCapacity();
		crewCapLabel = addTextField(crewCountPanel, Msg.getString("TabPanelBots.crewCapacity"), crewCapacityCache, null); //$NON-NLS-1$

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