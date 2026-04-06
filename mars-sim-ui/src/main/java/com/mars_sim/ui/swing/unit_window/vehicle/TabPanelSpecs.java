/*
 * Mars Simulation Project
 * TabPanelSpecs.java
 * @date 2025-09-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
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
import com.mars_sim.ui.swing.components.JDoubleLabel;
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
	
	private JDoubleLabel estimatedRange;
	private JDoubleLabel currentRange;
	private JDoubleLabel cumEnergyUsage;
	private JDoubleLabel cumFuelUsage;
	private JDoubleLabel roadSpeed;
	private JDoubleLabel roadPower;
	private JDoubleLabel cumFE;
	private JDoubleLabel cumFC;
	private JDoubleLabel estFC;
	private JDoubleLabel estFE;
	private JDoubleLabel adjFC;
	private JDoubleLabel adjFE;
	private JDoubleLabel instantFE;
	private JDoubleLabel instantFC;
	private JDoubleLabel estFCFECoef;


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

		roadSpeed = new JDoubleLabel(StyleManager.DECIMAL_KPH);
		grid1.addLabelledItem("Ave Road Speed", roadSpeed, null);
		roadPower = new JDoubleLabel(StyleManager.DECIMAL_KW);
		grid1.addLabelledItem("Ave Road Power", roadPower, null);
		
		grid1.addRow( "Base Accel", StyleManager.DECIMAL_M_S2.format(vSpec.getBaseAccel()));	
		grid1.addRow( "Peak Power", StyleManager.DECIMAL_KW.format(vSpec.getPeakPower()));

		
		AttributePanel grid = new AttributePanel(7, 2);
		JPanel topPanel = new JPanel(new BorderLayout(0, 10));
		panel.add(topPanel);
		topPanel.add(grid, BorderLayout.NORTH);
		topPanel.setBorder(SwingHelper.createLabelBorder("Fuel Consumption (FC) / Fuel Economy (FE)"));
		
		grid.addRow( "Base FC*FE", StyleManager.DECIMAL_KWH_KG.format(vSpec.getCoeffBaseFCFE()/1000));
		estFCFECoef = new JDoubleLabel(StyleManager.DECIMAL_KWH_KG);
		grid.addLabelledItem("Est FC*FE", estFCFECoef, null);
		
		grid.addRow( "Base FC", StyleManager.DECIMAL_KWH_KM.format(vSpec.getBaseFuelConsumption()/1000));
		grid.addRow( "Base FE", StyleManager.DECIMAL_KM_KG.format(vSpec.getBaseFuelEconomy()));	
		
		grid.addRow( "Initial FC", StyleManager.DECIMAL_KWH_KM.format(vSpec.getInitialFuelConsumption()/1000));
		grid.addRow( "Initial FE", StyleManager.DECIMAL_KM_KG.format(vSpec.getInitialFuelEconomy()));			

		adjFC = new JDoubleLabel(StyleManager.DECIMAL_KWH_KM);
		grid.addLabelledItem("Adj FC", adjFC, null);
		adjFE = new JDoubleLabel(StyleManager.DECIMAL_KM_KG);
		grid.addLabelledItem("Adj FE", adjFE, null);
	
		cumFC = new JDoubleLabel(StyleManager.DECIMAL_KWH_KM);
		grid.addLabelledItem("Cum FC", cumFC, null);
		cumFE = new JDoubleLabel(StyleManager.DECIMAL_KM_KG);
		grid.addLabelledItem("Cum FE", cumFE, null);
		
		estFC = new JDoubleLabel(StyleManager.DECIMAL_KWH_KM);
		grid.addLabelledItem("Estimated FC", estFC, null);
		estFE = new JDoubleLabel(StyleManager.DECIMAL_KM_KG);
		grid.addLabelledItem("Estimated FE", estFE, null);
			
		instantFC = new JDoubleLabel(StyleManager.DECIMAL_KWH_KM);
		grid.addLabelledItem("Instant FC", instantFC, null);
		instantFE = new JDoubleLabel(StyleManager.DECIMAL_KM_KG);
		grid.addLabelledItem("Instant FE", instantFE, null);
		
						
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

		cumEnergyUsage = new JDoubleLabel(StyleManager.DECIMAL2_KWH);
		grid2.addLabelledItem("Cum Energy Used", cumEnergyUsage, null);
		
		currentRange = new JDoubleLabel(StyleManager.DECIMAL_KM);
		grid2.addLabelledItem("Current Range", currentRange, null);
		
		cumFuelUsage = new JDoubleLabel(StyleManager.DECIMAL_KG);
		grid2.addLabelledItem("Cum Fuel Used", cumFuelUsage, null);

		estimatedRange = new JDoubleLabel(StyleManager.DECIMAL_KM);
		grid2.addLabelledItem("Estimated Range", estimatedRange, null);

		// Load the dynamic values
		updateSpecs();
	}
	
    /**
     * Update the specs on this vehicle
     */	
	private void updateSpecs() {
		var v = getEntity();

		estFCFECoef.setValue(v.getCoeffEstFCFE()/1000);
		
		roadSpeed.setValue(v.getRoadSpeedHistoryAverage());
		roadPower.setValue(v.getRoadPowerHistoryAverage());
	
		cumEnergyUsage.setValue(v.getCumEnergyUsage());
		cumFuelUsage.setValue(v.getCumFuelUsage());
		
		cumFE.setValue(v.getCumFuelEconomy());
		cumFC.setValue(v.getCumFuelConsumption()/1000);
		
		adjFE.setValue(v.getAdjustedFuelEconomy());
		adjFC.setValue(v.getAdjustedFuelConsumption()/1000);
		
		estFE.setValue(v.getEstimatedFuelEconomy());
		estFC.setValue(v.getEstimatedFuelConsumption()/1000);
		
		instantFE.setValue(v.getIFuelEconomy());
		instantFC.setValue(v.getIFuelConsumption()/1000);
		
		currentRange.setValue(v.getRange());
		if (v instanceof Rover r) {
			estimatedRange.setValue(r.getEstimatedRange());
		}
		else if (v instanceof Drone d) {
			estimatedRange.setValue(d.getEstimatedRange());
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
