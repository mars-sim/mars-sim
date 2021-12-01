/**
 * Mars Simulation Project
 * BuildingPanelComputation.java
 * @date 2021-09-30
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.SpringLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

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
		super(computation.getBuilding(), desktop);

		setLayout(new BorderLayout(0, 0));

		// Create title label.
		WebLabel titleLabel = new WebLabel(Msg.getString("BuildingPanelComputation.title"), WebLabel.CENTER);
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));

		WebPanel titlePanel = new WebPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(titleLabel);

		WebPanel springPanel = new WebPanel(new SpringLayout());
		add(springPanel, BorderLayout.CENTER);

		// Power Demand
		WebLabel label0 = new WebLabel(Msg.getString("BuildingPanelComputation.powerDemand"), WebLabel.RIGHT);
		label0.setToolTipText(Msg.getString("BuildingPanelComputation.powerDemand.tooltip"));
		springPanel.add(label0);

		double powerDemand = building.getComputation().getPowerDemand();
		WebPanel wrapper0 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		WebTextField tf0 = new WebTextField(Math.round(10.0*powerDemand)/10.0 + " kW");
		tf0.setEditable(false);
		tf0.setColumns(10);
		tf0.setPreferredSize(new Dimension(120, 25));
		wrapper0.add(tf0);
		springPanel.add(wrapper0);

		// Cooling Demand
		WebLabel label1 = new WebLabel(Msg.getString("BuildingPanelComputation.coolingDemand"), WebLabel.RIGHT);
		label1.setToolTipText(Msg.getString("BuildingPanelComputation.coolingDemand.tooltip"));
		springPanel.add(label1);

		double coolingDemand = building.getComputation().getCoolingDemand();
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		WebTextField tf1 = new WebTextField(Math.round(10.0*coolingDemand)/10.0 + " kW");
		tf1.setEditable(false);
		tf1.setColumns(10);
		tf1.setPreferredSize(new Dimension(120, 25));
		wrapper1.add(tf1);
		springPanel.add(wrapper1);

		// Capability
		WebLabel label2 = new WebLabel(Msg.getString("BuildingPanelComputation.computingUnit"), WebLabel.RIGHT);
		label2.setToolTipText(Msg.getString("BuildingPanelComputation.computingUnit.tooltip"));
		springPanel.add(label2);

		double computingUnit = building.getComputation().getComputingUnit();
		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		WebTextField tf2 = new WebTextField(Math.round(10.0*computingUnit)/10.0 + " CUs");
		tf2.setEditable(false);
		tf2.setColumns(10);
		tf2.setPreferredSize(new Dimension(120, 25));
		wrapper2.add(tf2);
		springPanel.add(wrapper2);

		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(springPanel,
		                                3, 2, //rows, cols
		                                65, 5,        //initX, initY
		                                3, 1);       //xPad, yPad
	}

	@Override
	public void update() {
		// doesn't change so nothing to update.
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	}
}
