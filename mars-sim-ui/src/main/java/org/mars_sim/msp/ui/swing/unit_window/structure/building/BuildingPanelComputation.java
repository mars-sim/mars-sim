/**
 * Mars Simulation Project
 * BuildingPanelComputation.java
 * @date 2021-09-30
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.panel.WebPanel;

/**
 * The BuildingPanelComputation class is a building function panel representing
 * the computational capability of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelComputation
extends BuildingFunctionPanel {

	/**
	 * Constructor.
	 * @param computation the computation building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelComputation(Computation computation, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelComputation.title"), computation.getBuilding(), desktop);
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
		addTextField(springPanel, Msg.getString("BuildingPanelComputation.powerDemand"),
				     Math.round(10.0*powerDemand)/10.0 + " kW", Msg.getString("BuildingPanelComputation.powerDemand.tooltip"));

		// Cooling Demand
		double coolingDemand = building.getComputation().getCoolingDemand();
		addTextField(springPanel, Msg.getString("BuildingPanelComputation.coolingDemand"),
					 Math.round(10.0*coolingDemand)/10.0 + " kW", Msg.getString("BuildingPanelComputation.coolingDemand.tooltip"));

		// Capability
		double computingUnit = building.getComputation().getComputingUnit();
		addTextField(springPanel, Msg.getString("BuildingPanelComputation.computingUnit"),
					 Math.round(10.0*computingUnit)/10.0 + " CUs", Msg.getString("BuildingPanelComputation.computingUnit.tooltip"));
	}
}
