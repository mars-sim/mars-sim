/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2023-06-06
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.svg.SVGGraphicNodeIcon;
import org.mars_sim.msp.ui.swing.tool.svg.SVGMapUtil;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * This tab shows the general details of the Vehicle type.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = "info";
	
	private Vehicle v;

	/**
	 * Constructor.
	 */
	public TabPanelGeneral(Vehicle v, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelGeneral.title"),
			ImageLoader.getIconByName(ID_ICON), 
			Msg.getString("BuildingPanelGeneral.title"),
			v, desktop);
		this.v = v;
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

		// Add SVG Image loading for the building
		GraphicsNode svg = SVGMapUtil.getVehicleSVG(v.getBaseImage());
		SVGGraphicNodeIcon svgIcon = new SVGGraphicNodeIcon(svg, 128, 64, true);
		JLabel svgLabel = new JLabel(svgIcon);
		JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		svgPanel.add(svgLabel);
		topPanel.add(svgPanel, BorderLayout.NORTH);
		
		// Prepare spring layout info panel.
//		AttributePanel infoPanel = new AttributePanel(2);
//		topPanel.add(infoPanel, BorderLayout.CENTER);
		
		AttributePanel labelGrid = new AttributePanel(9, 2);
		topPanel.add(labelGrid, BorderLayout.CENTER);
		
		
		int fuelTypeID = v.getFuelTypeID();
		String fuelTypeStr = "None";
		if (fuelTypeID < 0) {
			fuelTypeStr = v.getFuelTypeStr();
		}
		else {
			fuelTypeStr = ResourceUtil.findAmountResourceName(fuelTypeID);
		}
		
		labelGrid.addTextField( "Type", v.getVehicleType().getName(), null);
		labelGrid.addTextField( "Specification", v.getSpecName(), null);
		labelGrid.addTextField( "# Battery Module", v.getBatteryModule() + "", null);		
		labelGrid.addTextField( "# Fuel Cell Stack", v.getFuellCellStack() + "", null);		
		labelGrid.addTextField( "Fuel Type", fuelTypeStr, null);
		
		labelGrid.addTextField( "Fuel Capacity", StyleManager.DECIMAL_KG.format(v.getFuelCapacity()), null);
		labelGrid.addTextField( "Base Mass", StyleManager.DECIMAL_KG.format(v.getBaseMass()), "The base mass of this vehicle");
		labelGrid.addTextField( "Cargo Capacity", StyleManager.DECIMAL_KG.format(v.getCargoCapacity()), null);	
		labelGrid.addTextField( "Crew Size", v.getVehicleSpec().getCrewSize() + "", null);
//		labelGrid.addTextField( "# Motors", "", null);
		labelGrid.addTextField( "Base Speed", StyleManager.DECIMAL_M_S.format(v.getVehicleSpec().getBaseSpeed()), null);
		
		labelGrid.addTextField( "Base Accel", StyleManager.DECIMAL_M_S2.format(v.getVehicleSpec().getBaseAccel()), null);	
		labelGrid.addTextField( "Average Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getAveragePower()), null);
		labelGrid.addTextField( "Peak Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getPeakPower()), null);
		labelGrid.addTextField( "Drivetrain Eff", StyleManager.DECIMAL_PERC.format(100*v.getVehicleSpec().getDrivetrainEfficiency()), null);	
		labelGrid.addTextField( "Drivetrain Energy", StyleManager.DECIMAL_KWH.format(v.getVehicleSpec().getDrivetrainEnergy()), null);
		
		labelGrid.addTextField( "Fuel2Drive Conversion", StyleManager.DECIMAL_WH_KG.format(v.getVehicleSpec().getFuelConv()), null);
		labelGrid.addTextField( "Base Range", StyleManager.DECIMAL_KM.format(v.getBaseRange()), null);
		labelGrid.addTextField( "Current Range", StyleManager.DECIMAL_KM.format(v.getRange()), null);
		
		// Prepare spring layout info panel.
		AttributePanel fuelPanel = new AttributePanel(10);
		center.add(fuelPanel, BorderLayout.CENTER);

		fuelPanel.addTextField( "Base Fuel Consumption", StyleManager.DECIMAL_WH_KM.format(v.getVehicleSpec().getBaseFuelConsumption()), null);
		fuelPanel.addTextField( "Initial Fuel Consumption", StyleManager.DECIMAL_WH_KM.format(v.getVehicleSpec().getInitialFuelConsumption()), null);
		fuelPanel.addTextField( "Cumulative Fuel Consumption", StyleManager.DECIMAL_WH_KM.format(v.getCumFuelConsumption()), null);
		fuelPanel.addTextField( "Estimated Fuel Consumption", StyleManager.DECIMAL_WH_KM.format(v.getEstimatedFuelConsumption()), null);
		fuelPanel.addTextField( "Instant Fuel Consumption", StyleManager.DECIMAL_WH_KM.format(v.getIFuelConsumption()), null);

		fuelPanel.addTextField( "Base Fuel Economy", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getBaseFuelEconomy()), null);	
		fuelPanel.addTextField( "Initial Fuel Economy", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getInitialFuelEconomy()), null);			
		fuelPanel.addTextField( "Cumulative Fuel Economy", StyleManager.DECIMAL_KM_KG.format(v.getCumFuelEconomy()), null);	
		fuelPanel.addTextField( "Estimated Fuel Economy", StyleManager.DECIMAL_KM_KG.format(v.getEstimatedFuelEconomy()), null);			
		fuelPanel.addTextField( "Instant Fuel Economy", StyleManager.DECIMAL_KM_KG.format(v.getIFuelEconomy()), null);			
	}
}
