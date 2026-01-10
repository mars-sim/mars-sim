/*
 * Mars Simulation Project
 * TabPanelGeneralRobot.java
 * @date 2024-07-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.robot;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This tab shows the general details of the Robot type.
 */
@SuppressWarnings("serial")
class TabPanelGeneralRobot extends EntityTabPanel<Robot> implements EntityListener{

	private JLabel statePercent;
	private JLabel cap;
	private JLabel ampHours;
	private JLabel maxCapNameplate;
	private JLabel kWhStored;
	private JLabel tVolt;
	private JLabel health;
	private JLabel degradPercent;
	private JLabel maxCRating;
	private JLabel cycles;
		
	/**
	 * Constructor.
	 */
	public TabPanelGeneralRobot(Robot r, UIContext context) {
		super(
			GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),
			GENERAL_TOOLTIP,
			context, r);
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout(10, 10));
		center.add(topPanel, BorderLayout.NORTH);

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		topPanel.add(infoPanel, BorderLayout.NORTH);

		var r = getEntity();
		var settlement = new EntityLabel(r.getAssociatedSettlement(), getContext());
		infoPanel.addLabelledItem(Msg.getString("Settlement.singular"), settlement);
		infoPanel.addRow(Msg.getString("Robot.type"), r.getRobotType().getName());
		infoPanel.addRow(Msg.getString("Robot.model"), r.getModel());
		infoPanel.addRow("Base Mass", StyleManager.DECIMAL_KG.format(r.getBaseMass()), "The base mass of this unit");

		String text = r.getDescription().replace("\n", " ").replace("\t", "");
		var desc = SwingHelper.createTextBlock(Msg.getString("Entity.description"), text);
		topPanel.add(desc, BorderLayout.CENTER);
			
		JPanel dataPanel = new JPanel(new BorderLayout(10, 10));
		topPanel.add(dataPanel, BorderLayout.SOUTH);
		
        dataPanel.setBorder(SwingHelper.createLabelBorder("Battery Condition"));
		AttributePanel battPanel = new AttributePanel(10);
		dataPanel.add(battPanel, BorderLayout.NORTH);
    
		var battery = getEntity().getSystemCondition().getBattery();

		statePercent = battPanel.addRow("Battery Level", StyleManager.DECIMAL2_PERC.format(battery.getBatteryPercent()), 
				"The state of the battery is kWh stored / energy storage capacity * 100 percent");
		kWhStored = battPanel.addRow("kWh Stored", StyleManager.DECIMAL_KWH.format(battery.getkWhStored()));
		cap = battPanel.addRow("Energy Storage Capacity", StyleManager.DECIMAL_KWH.format(battery.getEnergyStorageCapacity()));
		maxCapNameplate = battPanel.addRow("Nameplate Capacity", StyleManager.DECIMAL_KWH.format(battery.getMaxCapNameplate()));	
		maxCRating = battPanel.addRow("Max C-Rating Discharging", StyleManager.DECIMAL_PLACES1.format(battery.getMaxCRating()));		
		
		ampHours = battPanel.addRow("Amp Hour", StyleManager.DECIMAL_AH.format(battery.getAmpHourStored()));
		tVolt = battPanel.addRow("Terminal Voltage", StyleManager.DECIMAL_V.format(battery.getTerminalVoltage()));
		health = battPanel.addRow(Msg.getString("Robot.health"), StyleManager.DECIMAL2_PERC.format(battery.getHealth() * 100));
		degradPercent = battPanel.addRow("Degradation", StyleManager.DECIMAL2_PERC.format(battery.getPercentDegrade())
				+ " per sol");
		cycles = battPanel.addRow("Charge Cycles", StyleManager.DECIMAL_PLACES2.format(battery.getNumCycles()) + "");
	}

	/**
	 * Track changes to the battery or system condition.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
	
		if (EntityEventType.BATTERY_EVENT.equals(event.getType())) {
			var battery = getEntity().getSystemCondition().getBattery();

			statePercent.setText(StyleManager.DECIMAL_PERC.format(battery.getBatteryPercent()));
			cap.setText(StyleManager.DECIMAL_KWH.format(battery.getEnergyStorageCapacity()));
			maxCapNameplate.setText(StyleManager.DECIMAL_KWH.format(battery.getMaxCapNameplate()));
			kWhStored.setText(StyleManager.DECIMAL_KWH.format(battery.getkWhStored()));
			ampHours.setText(StyleManager.DECIMAL_AH.format(battery.getAmpHourStored()));
			
			tVolt.setText(StyleManager.DECIMAL_V.format(battery.getTerminalVoltage()));
			health.setText(StyleManager.DECIMAL2_PERC.format(battery.getHealth() * 100));
			degradPercent.setText(StyleManager.DECIMAL2_PERC.format(battery.getPercentDegrade()) + " per sol");
			maxCRating.setText(StyleManager.DECIMAL_PLACES1.format(battery.getMaxCRating()));
			cycles.setText(StyleManager.DECIMAL_PLACES2.format(battery.getNumCycles()));
		}
	}
}
