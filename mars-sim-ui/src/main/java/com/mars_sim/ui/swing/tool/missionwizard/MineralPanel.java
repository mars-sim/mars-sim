/*
 * Mars Simulation Project
 * MineralPanel.java
 * @date 2026-07-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.ui.swing.components.PercentageTableCellRenderer;
import com.mars_sim.ui.swing.utils.SwingHelper;
import java.awt.BorderLayout;

/**
 * This JPanel shows the mineral concentration of a MineralSite in a table.
 */
class MineralPanel extends JPanel {
    private static final long serialVersionUID = 1L;

	/**
	 * Show the mineral concentration when a site is selected.
	 */
	private static class MineralModel extends AbstractTableModel {

		private record MineralId(String name, int id) {}

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private MineralSite site;
		private List<MineralId> minerals = Collections.emptyList();
	
		private void update(MineralSite newSite) {
			if (newSite == null) {
				minerals = Collections.emptyList();
			}
			else {
				minerals = newSite.getMinerals().keySet().stream()
						.map(m ->  new MineralId(ResourceUtil.findAmountResourceName(m), m))
						.sorted(Comparator.comparing(MineralId::name))
						.toList();
			}

			site = newSite;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return minerals.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var mineral = minerals.get(rowIndex);
			return switch(columnIndex) {
				case 0 -> mineral.name;
				case 1 -> site.getMinerals().get(mineral.id).concentration();
				case 2 -> site.getMinerals().get(mineral.id).certainty();
				default -> null;
			};
		}
		
		@Override
		public String getColumnName(int column) {
			return switch(column) {
				case 0 -> "Mineral";
				case 1 -> "Estimated Concentration";
				case 2 -> "Degree of Certainty";
				default -> null;
			};
		}
	}

    private MineralModel model;

    public MineralPanel() {
        setLayout(new BorderLayout());
		model = new MineralModel();

		var itemTable = new JTable(model);
		itemTable.setRowSelectionAllowed(false);	
		itemTable.setPreferredScrollableViewportSize(itemTable.getPreferredSize());
		itemTable.getColumnModel().getColumn(1).setCellRenderer(
							new PercentageTableCellRenderer(false));
		itemTable.getColumnModel().getColumn(2).setCellRenderer(
							new PercentageTableCellRenderer(false));
		JScrollPane tableScrollPane = new JScrollPane();
		tableScrollPane.setViewportView(itemTable);
		tableScrollPane.setBorder(SwingHelper.createLabelBorder("Minerals"));

        add(tableScrollPane, BorderLayout.CENTER);
    }

    public void setSelection(MineralSite site) {
        model.update(site);
    }
}
