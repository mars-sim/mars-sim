/*
 * Mars Simulation Project
 * TabPanelMaintenance.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.PercentageCellRenderer;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;

@SuppressWarnings("serial")
public class TabPanelMaintenance extends TabPanel {

	private static final String SPANNER_ICON = "maintenance";

	private BuildingMaintModel tableModel;
	
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelMaintenance(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(null, ImageLoader.getIconByName(SPANNER_ICON), "Maintenance", unit, desktop);

		tableModel = new BuildingMaintModel((Settlement) unit);
	}

	@Override
	protected void buildUI(JPanel content) {
		
		JScrollPane maintPane = new JScrollPane();
		maintPane.setPreferredSize(new Dimension(160, 80));
		content.add(maintPane, BorderLayout.CENTER);

		// Create the parts table
		JTable table = new JTable(tableModel);
		table.setRowSelectionAllowed(true);
		table.addMouseListener(new UnitTableLauncher(getDesktop()));
		table.setAutoCreateRowSorter(true);

		TableColumnModel tc = table.getColumnModel();
		tc.getColumn(0).setPreferredWidth(120);
		tc.getColumn(1).setPreferredWidth(60);
		tc.getColumn(1).setCellRenderer(new PercentageCellRenderer());
		tc.getColumn(2).setCellRenderer(new NumberCellRenderer(2));
		tc.getColumn(2).setPreferredWidth(60);
		tc.getColumn(3).setCellRenderer(new PercentageCellRenderer());
		tc.getColumn(3).setPreferredWidth(60);
		maintPane.setViewportView(table);
	}

	/**
	 * Updates the tab panel.
	 */
	@Override
	public void update() {
		tableModel.update();
	}

	private static class BuildingMaintModel extends AbstractTableModel
			implements UnitModel {

		List<Building> buildings;

		public BuildingMaintModel(Settlement settlement) {
			this.buildings = new ArrayList<>(settlement.getBuildingManager().getBuildings());
		}

		public void update() {
			fireTableRowsUpdated(0, buildings.size()-1);
		}

		@Override
		public int getRowCount() {
			return buildings.size();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
            case 0:
				return String.class;
			case 1:
				return Integer.class;
			case 2:
				return Double.class;
			case 3:
				return Integer.class;
			default:
                return null;
            }
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
				case 0: 
					return "Building";
				case 1:
					return "Condition";
				case 2:
					return "Last Maint.";
				case 3:
					return "Completed";
				default:
					return "";
			}
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Building building = buildings.get(rowIndex);
			MalfunctionManager manager = building.getMalfunctionManager();
			switch(columnIndex) {
			case 0: 
				return building.getName();
			case 1:
				return (int)manager.getWearCondition();
			case 2:
				return manager.getTimeSinceLastMaintenance()/1000D;
			case 3: {
				double completed = manager.getMaintenanceWorkTimeCompleted();
				double total = manager.getMaintenanceWorkTime();
				return (int)(100.0 * completed / total);
				}
			default:
				return "";
			}
		}

		@Override
		public Unit getAssociatedUnit(int row) {
			return buildings.get(row);
		}
	}
}
