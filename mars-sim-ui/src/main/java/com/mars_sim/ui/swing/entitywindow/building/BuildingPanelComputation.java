/*
 * Mars Simulation Project
 * BuildingPanelComputation.java
 * @date 2022-07-10
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.Computation;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The BuildingPanelComputation class is a building function panel representing
 * the computational capability of a building.
 */
@SuppressWarnings("serial")
class BuildingPanelComputation extends EntityTabPanel<Building> 
		implements TemporalComponent{

	private static final String COMPUTING_ICON = "computing";
	private static final String CU = " CUs";
	private static final String SLASH = " / ";
	private static final String KW = " kW";
	
	private JLabel powerLoadsLabel;
	private JLabel percentUsageLabel;
	private JLabel cULabel;
	private JLabel entropyLabel;
	
	/**
	 * Constructor.
	 * 
	 * @param computation the computation building function.
	 * @param context the UI context
	 */
	public BuildingPanelComputation(Computation computation, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelComputation.title"), 
			ImageLoader.getIconByName(COMPUTING_ICON), null,
			context, computation.getBuilding() 
		);
	}
	
	/**
	 * Builds the UI.
	 * 
	 * @param center the center JPanel in BuildingFunctionPanel
	 */
	@Override
	protected void buildUI(JPanel center) {

		var building = getEntity();

		AttributePanel springPanel = new AttributePanel(4);
		center.add(springPanel, BorderLayout.NORTH);

		// Power Loads
		double[] powerLoads = building.getComputation().getSeparatePowerLoadNonLoad();
		String twoLoads = Math.round(powerLoads[0] * 10.0)/10.0 + SLASH
				+ Math.round(powerLoads[1] * 10.0)/10.0 + KW;
		
		powerLoadsLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.powerload"),
				twoLoads, Msg.getString("BuildingPanelComputation.powerload.tooltip"));

		// CU Loads
		double usage = building.getComputation().getUsagePercent();
		percentUsageLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.usage"),
					 			StyleManager.DECIMAL1_PERC.format(usage), Msg.getString("BuildingPanelComputation.usage.tooltip"));

		// Peak CUs
		double peak = Math.round(building.getComputation().getPeakCU() * 10.0)/10.0;
		// Current CUs
		double computingUnit = Math.round(building.getComputation().getCurrentCU() * 10.0)/10.0;
		
		String text = computingUnit + SLASH + peak + CU;
		
		cULabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.computingUnit"),
				text, Msg.getString("BuildingPanelComputation.computingUnit.tooltip"));
	
		// Entropy
		double entropy = building.getComputation().getEntropy();
		entropyLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.entropy"),
	 			Math.round(entropy * 1_000.0)/1_000.0 + "", Msg.getString("BuildingPanelComputation.entropy.tooltip"));
	}
	
	@Override
	public void clockUpdate(ClockPulse pulse) {
		var building = getEntity();
		
		// Power Loads
		double[] powerLoads = building.getComputation().getSeparatePowerLoadNonLoad();
		String twoLoads = Math.round(powerLoads[0] * 10.0)/10.0 + SLASH
				+ Math.round(powerLoads[1] * 10.0)/10.0 + KW;
		
		if (!powerLoadsLabel.getText().equalsIgnoreCase(twoLoads))
			powerLoadsLabel.setText(twoLoads);
		
		// CU Loads
		double usage = building.getComputation().getUsagePercent();
		percentUsageLabel.setText(StyleManager.DECIMAL1_PERC.format(usage));
		
		// Peak CUs
		double peak = Math.round(building.getComputation().getPeakCU()* 10.0)/10.0;
		// Current CUs
		double computingUnit = Math.round(building.getComputation().getCurrentCU()* 10.0)/10.0;
		
		String text = computingUnit + SLASH + peak + CU;
		
		if (!cULabel.getText().equalsIgnoreCase(text))
			cULabel.setText(text);
		
		// Update entropy
		String entropy = Math.round(building.getComputation().getEntropy() * 1_000.0)/1_000.0 + "";
		if (!entropyLabel.getText().equalsIgnoreCase(entropy))
			entropyLabel.setText(entropy);
	}
}
