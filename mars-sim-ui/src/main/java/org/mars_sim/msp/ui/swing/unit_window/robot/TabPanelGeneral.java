/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2023-02-25
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.robot;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.SystemCondition;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * This tab shows the general details of the Robot type.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = "info";
	
	private Robot r;

	private JTextField charge;

	/**
	 * Constructor.
	 */
	public TabPanelGeneral(Robot r, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelGeneral.title"),
			ImageLoader.getIconByName(ID_ICON), 
			Msg.getString("BuildingPanelGeneral.title"),
			r, desktop);
		this.r = r;
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

	
		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new GridLayout(5, 2, 3, 1));
		topPanel.add(infoPanel, BorderLayout.CENTER);

		SystemCondition sc = r.getSystemCondition();

		addTextField(infoPanel, "Type:", r.getRobotType().getName(), null);
		addTextField(infoPanel, "Model:", r.getModel(), null);
		charge = addTextField(infoPanel, "Battery Charge:", StyleManager.DECIMAL_PERC.format(sc.getBatteryState()), null);
		addTextField(infoPanel, "Battery Capacity:", StyleManager.DECIMAL_KW.format(sc.getBatteryCapacity()), null);

		// Prepare mass label
		addTextField(infoPanel, "Base Mass:", r.getBaseMass() + " kg", "The base mass of this robot");
	}

	@Override
	public void update() {
		charge.setText(StyleManager.DECIMAL_PERC.format(r.getSystemCondition().getBatteryState()));

	}
}
