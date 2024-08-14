/*
 * Mars Simulation Project
 * TabPanelGeneralVehicle.java
 * @date 2024-07-14
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.svg.SVGGraphicNodeIcon;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelGeneral is a tab panel for general information about a vehicle.
 */
@SuppressWarnings("serial")
public class TabPanelGeneralVehicle extends TabPanel {

	private static final String ID_ICON = "info"; //$NON-NLS-1$

	private Vehicle vehicle;
	
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
	public TabPanelGeneralVehicle(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelGeneral.title"),
				ImageLoader.getIconByName(ID_ICON), 
				Msg.getString("TabPanelGeneral.tooltip"),
				vehicle, desktop);
		this.vehicle = vehicle;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		JPanel panel = new JPanel(new BorderLayout());
		content.add(panel, BorderLayout.NORTH);

		// Add SVG Image loading for the building
		GraphicsNode svg = SVGMapUtil.getVehicleSVG(vehicle.getBaseImage());
		SVGGraphicNodeIcon svgIcon = new SVGGraphicNodeIcon(svg, 128, 64, true);
		JLabel svgLabel = new JLabel(svgIcon);
		JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
		svgPanel.add(svgLabel);
		panel.add(svgPanel, BorderLayout.NORTH);
		
		fuelTypeID = vehicle.getFuelTypeID();
		String fuelTypeStr = "";
		if (fuelTypeID < 0) {
			fuelTypeStr = vehicle.getFuelTypeStr();
		}
		else {
			fuelTypeStr = ResourceUtil.findAmountResourceName(fuelTypeID);
		}	
		
		// Prepare attribute panel.
		AttributePanel infoPanel = new AttributePanel(15);
		
		panel.add(infoPanel, BorderLayout.SOUTH);
		
		infoPanel.addRow("Name", vehicle.getName());
		infoPanel.addRow("Type", vehicle.getSpecName());
		infoPanel.addRow("Model", vehicle.getModelName());
		
		// FUTURE: 
		// add date of commission
		// add country
		// add maintainer
		
		infoPanel.addRow("Max Crew", vehicle.getVehicleSpec().getCrewSize() + "");

		currentMassLabel = infoPanel.addRow("Current Mass", StyleManager.DECIMAL_KG.format(vehicle.getMass()));
		infoPanel.addRow("Base Mass", StyleManager.DECIMAL_KG.format(vehicle.getBaseMass()));
		
		remainCapLabel = infoPanel.addRow("Remaining Capacity", StyleManager.DECIMAL_KG.format(vehicle.getRemainingCargoCapacity()));
		infoPanel.addRow("Cargo Capacity", StyleManager.DECIMAL_KG.format(vehicle.getCargoCapacity()));

		fuelCap = vehicle.getFuelCapacity();
		
		infoPanel.addRow("Fuel Type", fuelTypeStr);
		
		double fuel = vehicle.getAmountResourceStored(fuelTypeID);
		
		fuelTankLabel = infoPanel.addRow("Fuel Tank", StyleManager.DECIMAL_KG.format(fuel) + " (" + 
				StyleManager.DECIMAL_PERC.format(100 * fuel/fuelCap) + " Filled)");
		
		infoPanel.addRow("Fuel Cap", StyleManager.DECIMAL_KG.format(fuelCap));
		
		infoPanel.addRow("Cell Stack", vehicle.getFuellCellStack() + "");	
		infoPanel.addRow("Battery Module", vehicle.getBatteryModule() + "");	
		batteryPercentLabel = infoPanel.addRow("Battery Percent", 
				StyleManager.DECIMAL_PERC.format(vehicle.getBatteryPercent()));
		infoPanel.addRow("Battery Cap", StyleManager.DECIMAL_KWH.format(vehicle.getBatteryCapacity()));	
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		currentMassLabel.setText(StyleManager.DECIMAL_KG.format(vehicle.getMass()));

		remainCapLabel.setText(StyleManager.DECIMAL_KG.format(vehicle.getRemainingCargoCapacity()));
		
		double fuel = vehicle.getAmountResourceStored(fuelTypeID);
		fuelTankLabel.setText(StyleManager.DECIMAL_KG.format(fuel) + " (" + 
				StyleManager.DECIMAL_PERC.format(100 * fuel/fuelCap) + " Filled Up)");
		
		batteryPercentLabel.setText(StyleManager.DECIMAL_PERC.format(vehicle.getBatteryPercent()));
	}
}
