/*
 * Mars Simulation Project
 * TabPanelSpecs.java
 * @date 2025-09-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This tab shows the specs of the vehicle.
 */
@SuppressWarnings("serial")
class TabPanelSpecs extends EntityTabPanel<Vehicle> 
		implements EntityListener {

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


	/**
	 * Constructor.
	 */
	public TabPanelSpecs(Vehicle v, UIContext context) {
		super(
			Msg.getString("TabPanelSpecs.title"),
			ImageLoader.getIconByName(SPECS_ICON), 
			Msg.getString("TabPanelSpecs.tooltip"),
			context, v);
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
		centerPanel.setBorder(SwingHelper.createLabelBorder("DriveTrain (DT) Performance"));
	
		var v = getEntity();
		var vSpec = v.getVehicleSpec();
		grid1.addRow( "Base Speed", StyleManager.DECIMAL_KPH.format(vSpec.getBaseSpeed()));
		grid1.addRow( "Base Power", StyleManager.DECIMAL_KW.format(vSpec.getBasePower()));

		roadSpeed = grid1.addRow( "Ave Road Speed", null);
		roadPower = grid1.addRow( "Ave Road Power", null);
		
		grid1.addRow( "Base Accel", StyleManager.DECIMAL_M_S2.format(vSpec.getBaseAccel()));	
		grid1.addRow( "Peak Power", StyleManager.DECIMAL_KW.format(vSpec.getPeakPower()));

		
		AttributePanel grid = new AttributePanel(7, 2);
		JPanel topPanel = new JPanel(new BorderLayout(0, 10));
		panel.add(topPanel);
		topPanel.add(grid, BorderLayout.NORTH);
		topPanel.setBorder(SwingHelper.createLabelBorder("Fuel Consumption (FC) / Fuel Economy (FE)"));
		
		grid.addRow( "Base FC*FE", StyleManager.DECIMAL_KWH_KG.format(vSpec.getCoeffBaseFCFE()/1000));
		estFCFECoef = grid.addRow( "Est FC*FE", null);		
		
		grid.addRow( "Base FC", StyleManager.DECIMAL_KWH_KM.format(vSpec.getBaseFuelConsumption()/1000));
		grid.addRow( "Base FE", StyleManager.DECIMAL_KM_KG.format(vSpec.getBaseFuelEconomy()));	
		
		grid.addRow( "Initial FC", StyleManager.DECIMAL_KWH_KM.format(vSpec.getInitialFuelConsumption()/1000));
		grid.addRow( "Initial FE", StyleManager.DECIMAL_KM_KG.format(vSpec.getInitialFuelEconomy()));			

		adjFC = grid.addRow( "Adj FC", null);
		adjFE = grid.addRow( "Adj FE", null);			
	
		cumFC = grid.addRow( "Cum FC", null);
		cumFE = grid.addRow( "Cum FE", null);	
		
		estFC = grid.addRow( "Estimated FC", null);
		estFE = grid.addRow( "Estimated FE", null);			
		
		instantFC = grid.addRow( "Instant FC", null);
		instantFE = grid.addRow( "Instant FE", null);
		
						
		AttributePanel grid2 = new AttributePanel(5, 2);
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
		panel.add(bottomPanel);
		bottomPanel.add(grid2, BorderLayout.NORTH);
		bottomPanel.setBorder(SwingHelper.createLabelBorder("Range / Fuel / Energy"));
	
		grid2.addRow( "Fuel", Conversion.capitalize(v.getFuelTypeStr()));
		grid2.addRow( "Fuel-to-Drive", StyleManager.DECIMAL_KWH_KG.format(vSpec.getFuel2DriveEnergy()/1000));
		
		grid2.addRow( "Full Tank", StyleManager.DECIMAL_KWH.format(v.getFullTankFuelEnergyCapacity()));
		grid2.addRow( "DT Efficiency", StyleManager.DECIMAL_PERC.format(100*vSpec.getDrivetrainEfficiency()));

		grid2.addRow( "DT Fuel Energy", StyleManager.DECIMAL_KWH.format(vSpec.getDrivetrainFuelEnergy()));
		grid2.addRow( "Base Range", StyleManager.DECIMAL_KM.format(v.getBaseRange()));

		cumEnergyUsage = grid2.addRow( "Cum Energy Used", null);	
		
		currentRange = grid2.addRow( "Current Range", null);
		
		cumFuelUsage = grid2.addRow( "Cum Fuel Used", null);	

		estimatedRange = grid2.addRow( "Estimated Range", null);

		// Load the dynamic values
		updateSpecs();
	}
	
    /**
     * Update the specs on this vehicle
     */	
	private void updateSpecs() {
		var v = getEntity();

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

	/**
	 * Listens for change of coordinates on the Vehicle
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.COORDINATE_EVENT.equals(event.getType())) {
			updateSpecs();
		}
	}
}
