/*
 * Mars Simulation Project
 * MiningPanel.java
 * @date 2025-07-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission.objectives;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.mission.objectives.MiningObjective;
import com.mars_sim.core.mission.objectives.MiningObjective.MineralStats;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying mining mission information.
 */
@SuppressWarnings("serial")
public class MiningPanel extends JPanel
		implements EntityListener {

	// Data members
	private MineralTableModel excavationTableModel;
	
	/**
	 * Constructor
	 * 
	 * @param objective the mining objective.
	 * @param context the UI context.
	 */
	public MiningPanel(MiningObjective objective, UIContext context) {
		// Use JPanel constructor
		super();

		// Set the layout.
		setLayout(new BorderLayout());
		setName(objective.getName());

		// Create LUV panel.
		var detailsPane = new AttributePanel();
		add(detailsPane, BorderLayout.NORTH);

		// Create LUV label.
		detailsPane.addLabelledItem("Light Utility Vehicle", new EntityLabel(objective.getLUV(), context));
		detailsPane.addRow("Certainty",
						StyleManager.DECIMAL1_PERC.format(objective.getSite().getAverageCertainty()));

		// Create excavation table.
		excavationTableModel = new MineralTableModel(objective.getMineralStats());
		JTable excavationTable = new JTable(excavationTableModel);
		excavationTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
		add(StyleManager.createScrollBorder("Minerals Excavated", excavationTable), BorderLayout.CENTER);
	}


	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getType().equals(Mining.EXCAVATE_MINERALS_EVENT)
				|| event.getType().equals(Mining.COLLECT_MINERALS_EVENT)
				|| EntityEventType.INVENTORY_RESOURCE_EVENT.equals(event.getType())) {
			excavationTableModel.updateTable();
		}
	}

	/**
	 * Mineral table model.
	 */
	private class MineralTableModel extends AbstractTableModel {

		private Map<Integer, MineralStats> details;
		private List<AmountResource> names = Collections.emptyList();

		/**
		 * Constructor
		 */
		private MineralTableModel(Map<Integer,MineralStats> resources) {
			// Use AbstractTableModel constructor.
			super();

			this.details = resources;

			updateTable();
		}

		private void updateTable() {
			if (details.size() != names.size()) {
				// New resources
				names = details.keySet().stream()
					.map(ResourceUtil::findAmountResource)
					.sorted()
					.toList();

				fireTableStructureChanged();
			}
			else {
				fireTableDataChanged();
			}
		}

		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		public int getRowCount() {
			return names.size();
		}

		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> "Mineral";
				case 1 -> "Detected (kg)";
				case 2 -> "Mined (kg)";
				case 3 -> "Collected (kg)";
				default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return String.class;
			return Double.class;
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row    the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			var selected = names.get(row);

			if (column == 0)
				return selected.getName();
			var d = details.get(selected.getID());
			return switch(column) {
				case 1 -> d.getDetected();
				case 2 -> d.getExtracted();
				case 3 -> d.getCollected();
				default -> null;
			};
		}
	}
}
