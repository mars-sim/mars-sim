/*
 * Mars Simulation Project
 * TabPanelSpecs.java
 * @date 2024-06-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * This tab shows the specs of the vehicle.
 */
@SuppressWarnings("serial")
public class TabPanelSpecs extends TabPanel {

	private static final String SPECS_ICON = "specs";
	
	private JLabel estimatedRange;
	private JLabel currentRange;
	private JLabel cumEnergyUsage;
	private JLabel cumFuelUsage;
	private JLabel roadSpeed;
	private JLabel roadPower;
	private JLabel cumFE;
	private JLabel cumFC;
	private JLabel estFC;
	private JLabel estFE;
	private JLabel instantFE;
	private JLabel instantFC;
	private JLabel estFCFECoef;
	
	
	private Vehicle v;

	/**
	 * Constructor.
	 */
	public TabPanelSpecs(Vehicle v, MainDesktopPane desktop) {
		super(
			Msg.getString("TabPanelSpecs.title"),
			ImageLoader.getIconByName(SPECS_ICON), 
			Msg.getString("TabPanelSpecs.tooltip"),
			v, desktop);
		this.v = v;
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		AttributePanel grid0 = new AttributePanel(6, 2);
		JPanel topPanel = new JPanel(new BorderLayout(0, 10));
		center.add(topPanel, BorderLayout.NORTH);
		topPanel.add(grid0, BorderLayout.NORTH);
//		addBorder(topPanel, "Fuel Consumption and Economy");
		topPanel.setBorder(BorderFactory.createTitledBorder("Fuel Consumption (FC) and Fuel Economy (FE)"));
		
		grid0.addRow( "Base FC-FC Coef", StyleManager.DECIMAL_PLACES3.format(v.getVehicleSpec().getCoeffBaseFC2FE()));
		estFCFECoef = grid0.addRow( "Est FC-FE Coef", StyleManager.DECIMAL_PLACES3.format(v.getCoeffEstFC2FE()));		
		
		grid0.addRow( "Base FC", StyleManager.DECIMAL_KWH_KM.format(v.getVehicleSpec().getBaseFuelConsumption()/1000));
		grid0.addRow( "Base FE", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getBaseFuelEconomy()));	
		
		grid0.addRow( "Initial FC", StyleManager.DECIMAL_KWH_KM.format(v.getVehicleSpec().getInitialFuelConsumption()/1000));
		grid0.addRow( "Initial FE", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getInitialFuelEconomy()));			
		
		cumFC = grid0.addRow( "Cum FC", StyleManager.DECIMAL_KWH_KM.format(v.getCumFuelConsumption()/1000));
		cumFE = grid0.addRow( "Cum FE", StyleManager.DECIMAL_KM_KG.format(v.getCumFuelEconomy()));	
		
		estFC = grid0.addRow( "Estimated FC", StyleManager.DECIMAL_KWH_KM.format(v.getEstimatedFuelConsumption()/1000));
		estFE = grid0.addRow( "Estimated FE", StyleManager.DECIMAL_KM_KG.format(v.getEstimatedFuelEconomy()));			
		
		instantFC = grid0.addRow( "Instant FC", StyleManager.DECIMAL_KWH_KM.format(v.getIFuelConsumption()/1000));
		instantFE = grid0.addRow( "Instant FE", StyleManager.DECIMAL_KM_KG.format(v.getIFuelEconomy()));
		
		AttributePanel grid1 = new AttributePanel(3, 2);
		JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
		center.add(centerPanel, BorderLayout.CENTER);
		centerPanel.add(grid1, BorderLayout.NORTH);
//		addBorder(centerPanel, "DriveTrain (DT) Performance");	
		centerPanel.setBorder(BorderFactory.createTitledBorder("DriveTrain (DT) Performance"));
	
		grid1.addRow( "Base Speed", StyleManager.DECIMAL_KPH.format(v.getVehicleSpec().getBaseSpeed()));
		grid1.addRow( "Base Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getBasePower()));

		roadSpeed = grid1.addRow( "Ave Road Speed", StyleManager.DECIMAL_KPH.format(v.getAverageRoadLoadSpeed()));
		roadPower = grid1.addRow( "Ave Road Power", StyleManager.DECIMAL_KW.format(v.getAverageRoadLoadPower()));
		
		grid1.addRow( "Base Accel", StyleManager.DECIMAL_M_S2.format(v.getVehicleSpec().getBaseAccel()));	
		grid1.addRow( "Peak Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getPeakPower()));
				
		AttributePanel grid2 = new AttributePanel(5, 2);
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 30));
		centerPanel.add(bottomPanel, BorderLayout.CENTER);
		bottomPanel.add(grid2, BorderLayout.NORTH);
//		addBorder(bottomPanel, "DriveTrain (DT) Range Energy");	
		bottomPanel.setBorder(BorderFactory.createTitledBorder("DriveTrain (DT) Range Energy"));
	
		grid2.addRow( "Fuel", Conversion.capitalize(v.getFuelTypeStr()));
		grid2.addRow( "Fuel-to-Drive", StyleManager.DECIMAL_KWH_KG.format(v.getVehicleSpec().getFuel2DriveEnergy()/1000));
		
		grid2.addRow( "Full Tank", StyleManager.DECIMAL_KWH.format(v.getFullTankFuelEnergyCapacity()));
		grid2.addRow( "DT Efficiency", StyleManager.DECIMAL_PERC.format(100*v.getVehicleSpec().getDrivetrainEfficiency()));

		grid2.addRow( "DT Fuel Energy", StyleManager.DECIMAL_KWH.format(v.getVehicleSpec().getDrivetrainFuelEnergy()));
		grid2.addRow( "Base Range", StyleManager.DECIMAL_KM.format(v.getBaseRange()));

		cumEnergyUsage = grid2.addRow( "Cum Energy Used", StyleManager.DECIMAL2_KWH.format(v.getCumEnergyUsage()));	
		
		currentRange = grid2.addRow( "Current Range", StyleManager.DECIMAL_KM.format(v.getRange()));
		
		cumFuelUsage = grid2.addRow( "Cum Fuel Used", StyleManager.DECIMAL_KG.format(v.getCumFuelUsage()));	
		
		if (v instanceof Rover r) {
			estimatedRange = grid2.addRow( "Estimated Range", StyleManager.DECIMAL_KM.format(r.getEstimatedRange()));
		}
		else if (v instanceof Drone d) {
			estimatedRange = grid2.addRow( "Estimated Range", StyleManager.DECIMAL_KM.format(d.getEstimatedRange()));
		}
		else {
			estimatedRange = grid2.addRow( "Estimated Range", StyleManager.DECIMAL_KM.format(0));
		}
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		estFCFECoef.setText(StyleManager.DECIMAL_PLACES3.format(v.getCoeffEstFC2FE()));		
		
		roadSpeed.setText(StyleManager.DECIMAL_KPH.format(v.getAverageRoadLoadSpeed()));
		roadPower.setText(StyleManager.DECIMAL_KW.format(v.getAverageRoadLoadPower()));
	
		cumEnergyUsage.setText(StyleManager.DECIMAL2_KWH.format(v.getCumEnergyUsage()));	
		cumFuelUsage.setText(StyleManager.DECIMAL_KG.format(v.getCumFuelUsage()));	
		
		cumFE.setText(StyleManager.DECIMAL_KM_KG.format(v.getCumFuelEconomy()));
		cumFC.setText(StyleManager.DECIMAL_KWH_KM.format(v.getCumFuelConsumption()/1000));
		
		estFE.setText(StyleManager.DECIMAL_KM_KG.format(v.getEstimatedFuelEconomy()));
		estFC.setText(StyleManager.DECIMAL_KWH_KM.format(v.getEstimatedFuelConsumption()/1000));
	
		instantFE.setText(StyleManager.DECIMAL_KM_KG.format(v.getIFuelEconomy()));
		instantFC.setText(StyleManager.DECIMAL_KWH_KM.format(v.getIFuelConsumption()/1000));
		
		currentRange.setText(StyleManager.DECIMAL_KM.format(v.getRange()));
		if (v instanceof Rover r) {
			estimatedRange.setText(StyleManager.DECIMAL_KM.format(r.getEstimatedRange()));
		}
		else if (v instanceof Drone d) {
			estimatedRange.setText(StyleManager.DECIMAL_KM.format(d.getEstimatedRange()));
		}
	}
}
