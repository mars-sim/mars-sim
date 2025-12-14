/**
 * Mars Simulation Project
 * EmergencySupplyPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.mars_sim.core.mission.objectives.EmergencySupplyObjective;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.mission.GoodsTableModel;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * A panel for displaying emergency supply mission information.
 */
@SuppressWarnings("serial")
public class EmergencySupplyPanel extends JPanel
		implements EntityListener {

	// Data members.
	private EmergencySupplyObjective supplies;
	private GoodsTableModel emergencySuppliesTableModel;

	/**
	 * Constructor.
	 */
	public EmergencySupplyPanel(EmergencySupplyObjective supplies, UIContext context) {
		// Use JPanel constructor
		super();
		setName(supplies.getName());
		this.supplies = supplies;

		// Set the layout.
		setLayout(new BorderLayout());

		// Create the emergency supplies label.
		var attrPanel = new AttributePanel();
		attrPanel.addLabelledItem(Msg.getString("EmergencySupplyMissionCustomInfoPanel.emergencySupplies"),
					new EntityLabel(supplies.getDestination(), context));
		add(attrPanel, BorderLayout.NORTH);

		// Create a scroll pane for the emergency supplies table.
		JScrollPane emergencySuppliesScrollPane = new JScrollPane();
		emergencySuppliesScrollPane.setPreferredSize(new Dimension(-1, -1));
		add(emergencySuppliesScrollPane, BorderLayout.CENTER);

		// Create the emergency supplies table and model.
		emergencySuppliesTableModel = new GoodsTableModel();
		JTable emergencySuppliesTable = new JTable(emergencySuppliesTableModel);
		emergencySuppliesTable.setAutoCreateRowSorter(true);
		emergencySuppliesScrollPane.setViewportView(emergencySuppliesTable);

		emergencySuppliesTableModel.updateTable(supplies.getSupplies());
	}

	@Override
	public void entityUpdate(EntityEvent event) {
		emergencySuppliesTableModel.updateTable(supplies.getSupplies());
	}
}
