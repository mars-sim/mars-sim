/**
 * Mars Simulation Project
 * ConstructedBuildingsPanel.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.structure.construction.ConstructedBuildingLogEntry;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

public class ConstructedBuildingsPanel
extends JPanel {

	// Data members
	private JTable constructedTable;
	private ConstructedBuildingTableModel constructedTableModel;

	/**
	 * Constructor.
	 * @param manager the settlement construction manager.
	 */
	public ConstructedBuildingsPanel(ConstructionManager manager) {
		// Use JPanel constructor.
		super();

		setLayout(new BorderLayout(0, 0));
//		setBorder(new MarsPanelBorder());

		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(titlePanel, BorderLayout.NORTH);

		JLabel titleLabel = new JLabel("Constructed Buildings");
		titlePanel.add(titleLabel);

		// Create scroll panel for the outer table panel.
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(200, 75));
		scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPanel, BorderLayout.CENTER);

		// Prepare constructed table model.
		constructedTableModel = new ConstructedBuildingTableModel(manager);

		// Prepare constructed table.
		constructedTable = new ZebraJTable(constructedTableModel);
		scrollPanel.setViewportView(constructedTable);
		constructedTable.setRowSelectionAllowed(true);
		constructedTable.getColumnModel().getColumn(0).setPreferredWidth(105);
		constructedTable.getColumnModel().getColumn(1).setPreferredWidth(105);

		constructedTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		constructedTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		constructedTable.setAutoCreateRowSorter(true);
	
		TableStyle.setTableStyle(constructedTable);

	}

	/**
	 * Update the information on this panel.
	 */
	public void update() {
		TableStyle.setTableStyle(constructedTable);
		constructedTableModel.update();
	}

	/**
	 * Internal class used as model for the constructed table.
	 */
	private static class ConstructedBuildingTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		ConstructionManager manager;

		private ConstructedBuildingTableModel(ConstructionManager manager) {
			this.manager = manager;
		}

		public int getRowCount() {
			return manager.getConstructedBuildingLog().size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Building";
			else if (columnIndex == 1) return "Time Stamp";
			else return null;
		}

		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				ConstructedBuildingLogEntry logEntry = manager.getConstructedBuildingLog().get(row);
				if (column == 0) return logEntry.getBuildingName();
				else if (column == 1) return logEntry.getBuiltTime().toString();
				else return null;
			}
			else return null;
		}

		public void update() {
			fireTableDataChanged();
		}
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		constructedTable = null;
		constructedTableModel = null;
	}
}