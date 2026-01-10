/*
 * Mars Simulation Project
 * TabPanelGeneralVehicle.java
 * @date 2024-07-14
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TabPanelGeneral is a tab panel for general information about a vehicle.
 */
@SuppressWarnings("serial")
class TabPanelGeneralVehicle extends EntityTabPanel<Vehicle> 
		implements TemporalComponent{
	
	private JLabel fuelTankLabel;
	private JLabel batteryPercentLabel;
	private JLabel currentMassLabel;
	private JLabel remainCapLabel;
	
	private int fuelTypeID;
	
	private double fuelCap;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelGeneralVehicle(Vehicle vehicle, UIContext context) {
		// Use the TabPanel constructor
		super(GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),		
			GENERAL_TOOLTIP,
			context, vehicle
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {

		JPanel panel = new JPanel(new BorderLayout());
		content.add(panel, BorderLayout.NORTH);

		var vehicle = getEntity();

		// Add SVG Image loading for the vehicle
		var svgPanel = SVGMapUtil.createVehiclePanel(vehicle.getBaseImage(), 128, 64);
		panel.add(svgPanel, BorderLayout.NORTH);
		
		fuelTypeID = vehicle.getFuelTypeID();
		String fuelTypeStr = "";
		if (fuelTypeID < 0) {
			fuelTypeStr = vehicle.getFuelTypeStr();
		}
		else {
			fuelTypeStr = ResourceUtil.findAmountResourceName(fuelTypeID);
		}	
		
		String text = vehicle.getDescription().replace("\n", " ").replace("\t", "");
		var label = SwingHelper.createTextBlock(Msg.getString("Entity.description"), text);
		panel.add(label, BorderLayout.CENTER);
		
		// Prepare attribute panel.
		AttributePanel infoPanel = new AttributePanel();
		
		panel.add(infoPanel, BorderLayout.SOUTH);
		
		infoPanel.addTextField(Msg.getString("Entity.name"), vehicle.getName(), null);
		infoPanel.addTextField(Msg.getString("Vehicle.type"), vehicle.getSpecName(), null);
		infoPanel.addTextField(Msg.getString("Vehicle.model"), vehicle.getModelName(), null);
		
		// FUTURE: 
		// add date of commission
		// add country
		// add maintainer
		
		infoPanel.addTextField("Max Crew", vehicle.getVehicleSpec().getCrewSize() + "", null);

		currentMassLabel = infoPanel.addTextField("Current Mass", StyleManager.DECIMAL_KG.format(vehicle.getMass()), null);
		infoPanel.addTextField("Base Mass", StyleManager.DECIMAL_KG.format(vehicle.getBaseMass()), null);
		
		remainCapLabel = infoPanel.addTextField("Remaining Capacity", StyleManager.DECIMAL_KG.format(vehicle.getRemainingCargoCapacity()), null);
		infoPanel.addTextField("Cargo Capacity", StyleManager.DECIMAL_KG.format(vehicle.getCargoCapacity()), null);

		fuelCap = vehicle.getFuelCapacity();
		
		infoPanel.addTextField(Msg.getString("Vehicle.fuelType"), fuelTypeStr, null);
		
		double fuel = vehicle.getSpecificAmountResourceStored(fuelTypeID);
		
		fuelTankLabel = infoPanel.addTextField("Fuel Tank", StyleManager.DECIMAL_KG.format(fuel) + " (" + 
				StyleManager.DECIMAL_PERC.format(100 * fuel/fuelCap) + " Filled)", null);
		
		infoPanel.addTextField("Fuel Cap", StyleManager.DECIMAL_KG.format(fuelCap), null);
		
		infoPanel.addTextField("Cell Stack", vehicle.getFuellCellStack() + "", null);	
		infoPanel.addTextField("Battery Module", vehicle.getBatteryModule() + "", null);	
		batteryPercentLabel = infoPanel.addTextField("Battery Percent", 
				StyleManager.DECIMAL_PERC.format(vehicle.getBatteryPercent()), null);
		infoPanel.addTextField("Battery Cap", StyleManager.DECIMAL_KWH.format(vehicle.getBatteryCapacity()), null);	
	}

	/**
     * Update the variable Vehicle
     * @param pulse Incoming pulse.
     */
    @Override
    public void clockUpdate(ClockPulse pulse) {
		var vehicle = getEntity();
		currentMassLabel.setText(StyleManager.DECIMAL_KG.format(vehicle.getMass()));

		remainCapLabel.setText(StyleManager.DECIMAL_KG.format(vehicle.getRemainingCargoCapacity()));
		
		double fuel = vehicle.getSpecificAmountResourceStored(fuelTypeID);
		fuelTankLabel.setText(StyleManager.DECIMAL_KG.format(fuel) + " (" + 
				StyleManager.DECIMAL_PERC.format(100 * fuel/fuelCap) + " Filled Up)");
		
		batteryPercentLabel.setText(StyleManager.DECIMAL_PERC.format(vehicle.getBatteryPercent()));
	}
}
