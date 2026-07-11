/*
 * Mars Simulation Project
 * TabPanelGeneralRobot.java
 * @date 2024-07-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.robot;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This tab shows the general details of the Robot type.
 */
@SuppressWarnings("serial")
class TabPanelGeneralRobot extends EntityTabPanel<Robot> implements TemporalComponent {

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
	private JDoubleLabel temp;
		
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
		infoPanel.addTextField(Msg.getString("robot.model"), r.getModel(), null);
		infoPanel.addTextField("Base Mass", StyleManager.DECIMAL_KG.format(r.getBaseMass()), "The base mass of this unit");

		String text = r.getDescription().replace("\n", " ").replace("\t", "");
		var desc = SwingHelper.createTextBlock(Msg.getString("entity.description"), text);
		topPanel.add(desc, BorderLayout.CENTER);
			
		JPanel dataPanel = new JPanel(new BorderLayout(10, 10));
		topPanel.add(dataPanel, BorderLayout.SOUTH);
		
        dataPanel.setBorder(SwingHelper.createLabelBorder("Battery Parameters"));
		AttributePanel battPanel = new AttributePanel();
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
		battPanel.addLabelledItem(Msg.getString("robot.health"), health);
		degradPercent = new JDoubleLabel(StyleManager.DECIMAL2_PERC, battery.getPercentDegrade());
		battPanel.addLabelledItem("Degradation", degradPercent);
		cycles = new JDoubleLabel(StyleManager.DECIMAL_PLACES2, battery.getNumCycles());
		battPanel.addLabelledItem("Charge Cycles", cycles);
		temp = new JDoubleLabel(StyleManager.DECIMAL_CELCIUS, battery.getInternalTemperature());
		battPanel.addLabelledItem("Internal Temperature", temp);
	}

	/**
	 * Updates the battery params.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
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
		temp.setValue(battery.getInternalTemperature());
	}
}
