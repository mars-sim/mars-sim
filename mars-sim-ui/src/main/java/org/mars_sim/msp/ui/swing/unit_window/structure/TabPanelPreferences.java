/*
 * Mars Simulation Project
 * TabPanelWeights.java
 * @date 2023-06-08
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.reportingAuthority.PreferenceKey;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

@SuppressWarnings("serial")
public class TabPanelPreferences extends TabPanel {

	private static final String ICON = "scales";
	
	private PreferenceTableModel tableModel;

	/**
	 * Constructor.
	 * @param unit {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelPreferences(Unit unit, MainDesktopPane desktop) {
		// Use TabPanel constructor.
		super(
			null,
			ImageLoader.getIconByName(ICON),
			"Preferences", //$NON-NLS-1$
			unit, desktop
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {
		
 		// Create scroll panel for the outer table panel.
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(250, 300));
		// increase vertical mousewheel scrolling speed for this one
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		content.add(scrollPane);

		// Prepare goods table model.
		tableModel = new PreferenceTableModel((Settlement) getUnit());

		// Prepare goods table.
		JTable table = new JTable(tableModel);
		scrollPane.setViewportView(table);
		//table.setRowSelectionAllowed(true);
		
		// Override default cell renderer for formatting double values.
		table.setDefaultRenderer(Double.class, new NumberCellRenderer(1, true));
		
		//goodsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
		//goodsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		
		// Added the two methods below to make all heatTable columns
		// Resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));

		// Added sorting
		table.setAutoCreateRowSorter(true);
	}

	/**
	 * Internal class used as model for preference weights
	 */
	private static class PreferenceTableModel
				extends AbstractTableModel {

		// Data members
		private List<PreferenceKey> items;
		private Settlement target;

		private PreferenceTableModel(Settlement manager) {
			target = manager;
			items = new ArrayList<>(manager.getKnownPreferences());
		}

		@Override
		public int getRowCount() {
			return items.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch(columnIndex) {
				case 0, 1 -> String.class;
				case 2 -> Double.class;
				default -> throw new IllegalArgumentException("Unexpected value: " + columnIndex);
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> "Type";
				case 1 -> "Name";
				case 2 -> "Modifier";
				default -> throw new IllegalArgumentException("Unexpected value: " + columnIndex);
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < getRowCount()) {
				PreferenceKey key = items.get(row);
				// Capitalized good's names
				return switch(column) {
					case 0 -> key.getType();
					case 1 -> key.getName();
					case 2 -> target.getPreferenceModifier(key);
					default -> throw new IllegalArgumentException("Unexpected value: " + column);
				};
			}
			return null;
		}
	}
}
