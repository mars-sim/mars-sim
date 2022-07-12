/*
 * Mars Simulation Project
 * BuildingPanelComputation.java
 * @date 2022-07-10
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.panel.WebPanel;

/**
 * The BuildingPanelComputation class is a building function panel representing
 * the computational capability of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelComputation extends BuildingFunctionPanel {

	private static final String SERVER_ICON = Msg.getString("icon.server"); //$NON-NLS-1$

	private JTextField textField0;
	private JTextField textField1;
	private JTextField textField2;
	/**
	 * Constructor.
	 * @param computation the computation building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelComputation(Computation computation, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelComputation.title"), 
			ImageLoader.getNewIcon(SERVER_ICON), 
			computation.getBuilding(), 
			desktop
		);
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		WebPanel springPanel = new WebPanel(new GridLayout(3, 2, 3, 1));
		center.add(springPanel, BorderLayout.NORTH);

		// Power Demand
		double powerDemand = building.getComputation().getPowerDemand();
		textField0 = addTextField(springPanel, Msg.getString("BuildingPanelComputation.powerDemand"),
				     Math.round(10.0*powerDemand)/10.0 + " kW", Msg.getString("BuildingPanelComputation.powerDemand.tooltip"));

		// Cooling Demand
		double coolingDemand = building.getComputation().getCoolingDemand();
		textField1 = addTextField(springPanel, Msg.getString("BuildingPanelComputation.coolingDemand"),
					 Math.round(10.0*coolingDemand)/10.0 + " kW", Msg.getString("BuildingPanelComputation.coolingDemand.tooltip"));

		// Capability
		double computingUnit = building.getComputation().getComputingUnit();
		textField2 = addTextField(springPanel, Msg.getString("BuildingPanelComputation.computingUnit"),
					 Math.round(computingUnit* 100_000.0)/100_000.0 + " CUs", Msg.getString("BuildingPanelComputation.computingUnit.tooltip"));
	}
	
	@Override
	public void update() {

		double power = Math.round(10.0*building.getComputation().getPowerDemand())/10.0;
		if (!textField0.getText().equalsIgnoreCase(power + " kW"))
			textField0.setText(power + " kW");
		
		double cooling = Math.round(10.0*building.getComputation().getCoolingDemand())/10.0;
		if (!textField1.getText().equalsIgnoreCase(cooling + " kW"))
			textField1.setText(cooling + " kW");
		
		double u = Math.round(building.getComputation().getComputingUnit()* 100_000.0)/100_000.0;
		if (!textField1.getText().equalsIgnoreCase(u + " CUs"))
			textField1.setText(u + " CUs");
		
	}
}
