/*
 * Mars Simulation Project
 * TabPanelSpecs.java
 * @date 2024-06-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.batik.gvt.GraphicsNode;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.svg.SVGGraphicNodeIcon;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * This tab shows the specs of the vehicle.
 */
@SuppressWarnings("serial")
public class TabPanelSpecs extends TabPanel {

	private static final String ID_ICON = "info";
	
	private Vehicle v;

	/**
	 * Constructor.
	 */
	public TabPanelSpecs(Vehicle v, MainDesktopPane desktop) {
		super(
			Msg.getString("TabPanelSpecs.title"),
			ImageLoader.getIconByName(ID_ICON), 
			Msg.getString("TabPanelSpecs.tooltip"),
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
		
		int fuelTypeID = v.getFuelTypeID();
		String fuelTypeStr = "None";
		if (fuelTypeID < 0) {
			fuelTypeStr = v.getFuelTypeStr();
		}
		else {
			fuelTypeStr = ResourceUtil.findAmountResourceName(fuelTypeID);
		}
		
		JPanel specsPanel = new JPanel(new BorderLayout());
		center.add(specsPanel, BorderLayout.CENTER);
		
		// Prepare spring layout info panel.
		AttributePanel labelGrid = new AttributePanel(17, 2);
		specsPanel.add(labelGrid, BorderLayout.CENTER);
		
		labelGrid.addTextField( "Type", v.getVehicleType().getName(), null);
		labelGrid.addTextField( "Model", v.getSpecName(), null);
		
		labelGrid.addTextField( "Crew Size", v.getVehicleSpec().getCrewSize() + "", null);
		labelGrid.addTextField( "Cell Stack", v.getFuellCellStack() + "", null);	
		
		labelGrid.addTextField( "Battery Module", v.getBatteryModule() + "", null);	
		labelGrid.addTextField( "Fuel Type", fuelTypeStr, null);
	
		labelGrid.addTextField( "Battery Cap", v.getBatteryCapacity() + "", null);	
		labelGrid.addTextField( "Fuel Cap", StyleManager.DECIMAL_KG.format(v.getFuelCapacity()), null);
		
		labelGrid.addTextField( "Battery %", StyleManager.DECIMAL_PERC1.format(v.getBatteryPercent()), null);
		labelGrid.addTextField( "Base Mass", StyleManager.DECIMAL_KG.format(v.getBaseMass()), "The base mass of this vehicle");
		
		labelGrid.addTextField( "Cargo Cap", StyleManager.DECIMAL_KG.format(v.getCargoCapacity()), null);	
		labelGrid.addTextField( "Base FC2FE", StyleManager.DECIMAL_PLACES3.format(v.getVehicleSpec().getCoeffBaseFC2FE()), null);
		
		labelGrid.addTextField( "Base Accel", StyleManager.DECIMAL_M_S2.format(v.getVehicleSpec().getBaseAccel()), null);	
		labelGrid.addTextField( "Cum FC2FE", StyleManager.DECIMAL_PLACES3.format(v.getCoeffCumFC2FE()), null);		
		
		labelGrid.addTextField( "Base Speed", StyleManager.DECIMAL_M_S.format(v.getVehicleSpec().getBaseSpeed()), null);
		labelGrid.addTextField( "Base Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getBasePower()), null);

		labelGrid.addTextField( "Road Speed", StyleManager.DECIMAL_M_S.format(v.getAverageRoadLoadSpeed()), null);
		labelGrid.addTextField( "Road Power", StyleManager.DECIMAL_KW.format(v.getAverageRoadLoadPower()), null);
		
		labelGrid.addTextField( "Drivetrain Eff", StyleManager.DECIMAL_PERC.format(100*v.getVehicleSpec().getDrivetrainEfficiency()), null);
		labelGrid.addTextField( "Peak Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getPeakPower()), null);
		
		labelGrid.addTextField( "Drivetrain En", StyleManager.DECIMAL_KWH.format(v.getVehicleSpec().getDrivetrainEnergy()), null);
		labelGrid.addTextField( "Base Range", StyleManager.DECIMAL_KM.format(v.getBaseRange()), null);
		
		labelGrid.addTextField( "Drive Energy", StyleManager.DECIMAL_WH_KG.format(v.getVehicleSpec().getFuel2DriveEnergy()), null);
		labelGrid.addTextField( "Current Range", StyleManager.DECIMAL_KM.format(v.getRange()), null);

		labelGrid.addTextField( "Base FC", StyleManager.DECIMAL_WH_KM.format(v.getVehicleSpec().getBaseFuelConsumption()), null);
		labelGrid.addTextField( "Base FE", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getBaseFuelEconomy()), null);	
		
		labelGrid.addTextField( "Initial FC", StyleManager.DECIMAL_WH_KM.format(v.getVehicleSpec().getInitialFuelConsumption()), null);
		labelGrid.addTextField( "Initial FE", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getInitialFuelEconomy()), null);			
		
		labelGrid.addTextField( "Cumulative FC", StyleManager.DECIMAL_WH_KM.format(v.getCumFuelConsumption()), null);
		labelGrid.addTextField( "Cumulative FE", StyleManager.DECIMAL_KM_KG.format(v.getCumFuelEconomy()), null);	
		
		labelGrid.addTextField( "Estimated FC", StyleManager.DECIMAL_WH_KM.format(v.getEstimatedFuelConsumption()), null);
		labelGrid.addTextField( "Estimated FE", StyleManager.DECIMAL_KM_KG.format(v.getEstimatedFuelEconomy()), null);			
		
		labelGrid.addTextField( "Instant FC", StyleManager.DECIMAL_WH_KM.format(v.getIFuelConsumption()), null);
		labelGrid.addTextField( "Instant FE", StyleManager.DECIMAL_KM_KG.format(v.getIFuelEconomy()), null);			
	}
}
