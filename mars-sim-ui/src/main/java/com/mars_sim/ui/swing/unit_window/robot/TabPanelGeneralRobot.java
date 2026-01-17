/*
 * Mars Simulation Project
 * TabPanelGeneralRobot.java
 * @date 2024-07-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.robot;

import java.awt.BorderLayout;

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
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This tab shows the general details of the Robot type.
 */
@SuppressWarnings("serial")
class TabPanelGeneralRobot extends EntityTabPanel<Robot> implements EntityListener{

	private JDoubleLabel statePercent;
	private JDoubleLabel cap;
	private JDoubleLabel ampHours;
	private JDoubleLabel maxCapNameplate;
	private JDoubleLabel kWhStored;
	private JDoubleLabel tVolt;
	private JDoubleLabel health;
	private JDoubleLabel degradPercent;
	private JDoubleLabel maxCRating;
	private JDoubleLabel cycles;
		
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

		statePercent = new JDoubleLabel(StyleManager.DECIMAL2_PERC, battery.getBatteryPercent());
		battPanel.addLabelledItem("Battery Level", statePercent, 
				"The state of the battery is kWh stored / energy storage capacity * 100 percent");
		kWhStored = new JDoubleLabel(StyleManager.DECIMAL_KWH, battery.getkWhStored());
		battPanel.addLabelledItem("kWh Stored", kWhStored);
		cap =  new JDoubleLabel(StyleManager.DECIMAL_KWH, battery.getEnergyStorageCapacity());
		battPanel.addLabelledItem("Energy Storage Capacity", cap);
		maxCapNameplate = new JDoubleLabel(StyleManager.DECIMAL_KWH, battery.getMaxCapNameplate());
		battPanel.addLabelledItem("Nameplate Capacity", maxCapNameplate);
		maxCRating = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, battery.getMaxCRating());
		battPanel.addLabelledItem("Max C-Rating Discharging", maxCRating);
		
		ampHours = new JDoubleLabel(StyleManager.DECIMAL_AH, battery.getAmpHourStored());
		battPanel.addLabelledItem("Amp Hours Stored", ampHours);
		tVolt = new JDoubleLabel(StyleManager.DECIMAL_V, battery.getTerminalVoltage());
		battPanel.addLabelledItem("Terminal Voltage", tVolt);
		health = new JDoubleLabel(StyleManager.DECIMAL2_PERC, battery.getHealth() * 100);
		battPanel.addLabelledItem(Msg.getString("Robot.health"), health);
		degradPercent = new JDoubleLabel(StyleManager.DECIMAL2_PERC, battery.getPercentDegrade());
		battPanel.addLabelledItem("Degradation", degradPercent);
		cycles = new JDoubleLabel(StyleManager.DECIMAL_PLACES2, battery.getNumCycles());
		battPanel.addLabelledItem("Charge Cycles", cycles);
	}

	/**
	 * Track changes to the battery or system condition.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
	
		if (EntityEventType.BATTERY_EVENT.equals(event.getType())) {
			var battery = getEntity().getSystemCondition().getBattery();

			statePercent.setValue(battery.getBatteryPercent());
			cap.setValue(battery.getEnergyStorageCapacity());
			maxCapNameplate.setValue(battery.getMaxCapNameplate());
			kWhStored.setValue(battery.getkWhStored());
			ampHours.setValue(battery.getAmpHourStored());
			
			tVolt.setValue(battery.getTerminalVoltage());
			health.setValue(battery.getHealth() * 100);
			degradPercent.setValue(battery.getPercentDegrade());
			maxCRating.setValue(battery.getMaxCRating());
			cycles.setValue(battery.getNumCycles());
		}
	}
}
