/*
 * Mars Simulation Project
 * TabPanelMaintenance.java
 * @date 2024-07-22
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.components.PercentageTableCellRenderer;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * The TabPanelMaintenance is a tab panel for settlement's building maintenance.
 */
@SuppressWarnings("serial")
public class TabPanelMaintenance extends TabPanelTable {

	private static final String SPANNER_ICON = "maintenance";

	private BuildingMaintModel tableModel;
	
		
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit (currently for settlements only)
	 * @param desktop the main desktop.
	 */
	public TabPanelMaintenance(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
				Msg.getString("TabPanelMaintenance.title"),
				ImageLoader.getIconByName(SPANNER_ICON),
				Msg.getString("TabPanelMaintenance.title"), //$NON-NLS-1$
				settlement, desktop
			);

		
		tableModel = new BuildingMaintModel(settlement);
	}

	@Override
	protected TableModel createModel() {
		return tableModel;
	}
	

	@Override
	protected void setColumnDetails(TableColumnModel tc) {
		tc.getColumn(0).setPreferredWidth(130);
		tc.getColumn(1).setPreferredWidth(35);
		tc.getColumn(1).setCellRenderer(new PercentageTableCellRenderer(false));
		tc.getColumn(2).setCellRenderer(new NumberCellRenderer(2));
		tc.getColumn(2).setPreferredWidth(55);
		tc.getColumn(3).setCellRenderer(new NumberCellRenderer(2));
		tc.getColumn(3).setPreferredWidth(55);
		tc.getColumn(4).setCellRenderer(new NumberCellRenderer(3));
		tc.getColumn(4).setPreferredWidth(55);
		tc.getColumn(5).setCellRenderer(new PercentageTableCellRenderer(false));
		tc.getColumn(5).setPreferredWidth(45);
	}

	/**
	 * Updates the tab panel.
	 */
	@Override
	public void update() {
		tableModel.update();
	}

	private static class BuildingMaintModel extends AbstractTableModel
			implements EntityModel {

		List<Building> buildings;

		public BuildingMaintModel(Settlement settlement) {
			this.buildings = new ArrayList<>(settlement.getBuildingManager().getBuildingSet());
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
				return Double.class;
			case 4:
				return Double.class;
			case 5:
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
					return "Health";
				case 2:
					return "Last Inspect";
				case 3:
					return "Inspect window";
				case 4:
					return "Maint time";
				case 5:
					return "% Done";
				default:
					return "";
			}
		}

		@Override
		public int getColumnCount() {
			return 6;
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
				return manager.getEffectiveTimeSinceLastMaintenance()/1000D;
			case 3: 
				return manager.getStandardInspectionWindow()/1000D;
			case 4:
				return manager.getBaseMaintenanceWorkTime()/1000D;	
			case 5: {
				double completed = manager.getInspectionWorkTimeCompleted();
				double total = manager.getBaseMaintenanceWorkTime();
				return (int)(Math.round(1000.0 * completed / total)/10.0);
				}
			default:
				return "";
			}
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return buildings.get(row);
		}
	}
}
