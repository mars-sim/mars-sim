/*
 * Mars Simulation Project
 * TabPanelGeneralVehicle.java
 * @date 2024-07-14
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.svg.SVGGraphicNodeIcon;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.utils.AttributePanel;

import io.github.parubok.text.multiline.MultilineLabel;

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
		
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		addBorder(labelPanel, Msg.getString("Entity.description"));
		var label = new MultilineLabel();
		labelPanel.add(label);
		String text = vehicle.getDescription().replace("\n", " ").replace("\t", "");
		label.setText(text);
		label.setPreferredWidthLimit(430);
		label.setLineSpacing(1.2f);
		label.setMaxLines(30);
		label.setBorder(new EmptyBorder(5, 5, 5, 5));
		label.setSeparators(Set.of(' ', '/', '|', '(', ')'));
		panel.add(labelPanel, BorderLayout.CENTER);
		
		// Prepare attribute panel.
		AttributePanel infoPanel = new AttributePanel();
		
		panel.add(infoPanel, BorderLayout.SOUTH);
		
		infoPanel.addRow(Msg.getString("Entity.name"), vehicle.getName());
		infoPanel.addRow(Msg.getString("Vehicle.type"), vehicle.getSpecName());
		infoPanel.addRow(Msg.getString("Vehicle.model"), vehicle.getModelName());
		
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
		
		infoPanel.addRow(Msg.getString("Vehicle.fuelType"), fuelTypeStr);
		
		double fuel = vehicle.getSpecificAmountResourceStored(fuelTypeID);
		
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
