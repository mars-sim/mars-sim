/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2023-02-25
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.robot;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.SystemCondition;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * This tab shows the general details of the Robot type.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = "info";
	
	private Robot r;

	private JLabel charge;

	/**
	 * Constructor.
	 */
	public TabPanelGeneral(Robot r, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelGeneral.title"),
			ImageLoader.getIconByName(ID_ICON), 
			Msg.getString("BuildingPanelGeneral.title"),
			desktop);
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
		AttributePanel infoPanel = new AttributePanel(5);
		topPanel.add(infoPanel, BorderLayout.CENTER);

		SystemCondition sc = r.getSystemCondition();

		infoPanel.addTextField("Type:", r.getRobotType().getName(), null);
		infoPanel.addTextField("Model:", r.getModel(), null);
		charge = infoPanel.addTextField("Battery Charge:", StyleManager.DECIMAL_PERC.format(sc.getBatteryState()), null);
		infoPanel.addTextField("Battery Capacity:", StyleManager.DECIMAL_KW.format(sc.getBatteryCapacity()), null);

		// Prepare mass label
		infoPanel.addTextField("Base Mass:", StyleManager.DECIMAL_KG.format(r.getBaseMass()), "The base mass of this robot");
	}

	@Override
	public void update() {
		charge.setText(StyleManager.DECIMAL_PERC.format(r.getSystemCondition().getBatteryState()));

	}
}
