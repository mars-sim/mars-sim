/*
 * Mars Simulation Project
 * TabPanelSpecs.java
 * @date 2025-09-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
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
	private JLabel adjFC;
	private JLabel adjFE;
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
	 * Builds the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		center.add(panel, BorderLayout.NORTH);
		
		AttributePanel grid1 = new AttributePanel(3, 2);
		JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
		panel.add(centerPanel);
		centerPanel.add(grid1, BorderLayout.NORTH);
		centerPanel.setBorder(BorderFactory.createTitledBorder("DriveTrain (DT) Performance"));
	
		grid1.addRow( "Base Speed", StyleManager.DECIMAL_KPH.format(v.getVehicleSpec().getBaseSpeed()));
		grid1.addRow( "Base Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getBasePower()));

		roadSpeed = grid1.addRow( "Ave Road Speed", StyleManager.DECIMAL_KPH.format(v.getRoadSpeedHistoryAverage()));
		roadPower = grid1.addRow( "Ave Road Power", StyleManager.DECIMAL_KW.format(v.getRoadPowerHistoryAverage()));
		
		grid1.addRow( "Base Accel", StyleManager.DECIMAL_M_S2.format(v.getVehicleSpec().getBaseAccel()));	
		grid1.addRow( "Peak Power", StyleManager.DECIMAL_KW.format(v.getVehicleSpec().getPeakPower()));

		
		AttributePanel grid = new AttributePanel(7, 2);
		JPanel topPanel = new JPanel(new BorderLayout(0, 10));
		panel.add(topPanel);
		topPanel.add(grid, BorderLayout.NORTH);
		topPanel.setBorder(BorderFactory.createTitledBorder("Fuel Consumption (FC) / Fuel Economy (FE)"));
		
		grid.addRow( "Base FC*FE", StyleManager.DECIMAL_KWH_KG.format(v.getVehicleSpec().getCoeffBaseFCFE()/1000));
		estFCFECoef = grid.addRow( "Est FC*FE", StyleManager.DECIMAL_KWH_KG.format(v.getCoeffEstFCFE()/1000));		
		
		grid.addRow( "Base FC", StyleManager.DECIMAL_KWH_KM.format(v.getVehicleSpec().getBaseFuelConsumption()/1000));
		grid.addRow( "Base FE", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getBaseFuelEconomy()));	
		
		grid.addRow( "Initial FC", StyleManager.DECIMAL_KWH_KM.format(v.getVehicleSpec().getInitialFuelConsumption()/1000));
		grid.addRow( "Initial FE", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getInitialFuelEconomy()));			

		adjFC = grid.addRow( "Adj FC", StyleManager.DECIMAL_KWH_KM.format(v.getVehicleSpec().getAdjustedFuelConsumption()/1000));
		adjFE = grid.addRow( "Adj FE", StyleManager.DECIMAL_KM_KG.format(v.getVehicleSpec().getAdjustedFuelEconomy()));			
	
		cumFC = grid.addRow( "Cum FC", StyleManager.DECIMAL_KWH_KM.format(v.getCumFuelConsumption()/1000));
		cumFE = grid.addRow( "Cum FE", StyleManager.DECIMAL_KM_KG.format(v.getCumFuelEconomy()));	
		
		estFC = grid.addRow( "Estimated FC", StyleManager.DECIMAL_KWH_KM.format(v.getEstimatedFuelConsumption()/1000));
		estFE = grid.addRow( "Estimated FE", StyleManager.DECIMAL_KM_KG.format(v.getEstimatedFuelEconomy()));			
		
		instantFC = grid.addRow( "Instant FC", StyleManager.DECIMAL_KWH_KM.format(v.getIFuelConsumption()/1000));
		instantFE = grid.addRow( "Instant FE", StyleManager.DECIMAL_KM_KG.format(v.getIFuelEconomy()));
		
						
		AttributePanel grid2 = new AttributePanel(5, 2);
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
		panel.add(bottomPanel);
		bottomPanel.add(grid2, BorderLayout.NORTH);
		bottomPanel.setBorder(BorderFactory.createTitledBorder("Range / Fuel / Energy"));
	
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
		estFCFECoef.setText(StyleManager.DECIMAL_KWH_KG.format(v.getCoeffEstFCFE()/1000));		
		
		roadSpeed.setText(StyleManager.DECIMAL_KPH.format(v.getRoadSpeedHistoryAverage()));
		roadPower.setText(StyleManager.DECIMAL_KW.format(v.getRoadPowerHistoryAverage()));
	
		cumEnergyUsage.setText(StyleManager.DECIMAL2_KWH.format(v.getCumEnergyUsage()));	
		cumFuelUsage.setText(StyleManager.DECIMAL_KG.format(v.getCumFuelUsage()));	
		
		cumFE.setText(StyleManager.DECIMAL_KM_KG.format(v.getCumFuelEconomy()));
		cumFC.setText(StyleManager.DECIMAL_KWH_KM.format(v.getCumFuelConsumption()/1000));
		
		adjFE.setText(StyleManager.DECIMAL_KM_KG.format(v.getAdjustedFuelEconomy()));
		adjFC.setText(StyleManager.DECIMAL_KWH_KM.format(v.getAdjustedFuelConsumption()/1000));
		
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
