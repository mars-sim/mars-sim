/*
 * Mars Simulation Project
 * TabPanelComputing.java
 * @date 2024-07-07
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * This is a tab panel for settlement's computing capability.
 */
@SuppressWarnings("serial")
public class TabPanelComputing extends TabPanelTable {

	// default logger.

	private static final String COMPUTING_ICON = "computing";
	private static final String CU = " CUs";
	private static final String SLASH = " / ";
	private static final String KW = " kW";
	
	private JLabel powerloadsLabel;
	private JLabel percentUsageLabel;
	private JLabel cULabel;
	private JLabel entropyLabel;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private BuildingManager manager;

	private TableModel tableModel;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelComputing(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelComputing.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(COMPUTING_ICON),
			Msg.getString("TabPanelComputing.title"), //$NON-NLS-1$
			desktop
		);
		settlement = unit;
		manager = settlement.getBuildingManager();
	}
	
	@Override
	protected JPanel createInfoPanel() {
		// Prepare heat info panel.
		AttributePanel springPanel = new AttributePanel(4);

		// Total Power Demand
		double[] powerLoads = manager.getTotalCombinedLoads();
		String twoLoads = Math.round(powerLoads[0] * 10.0)/10.0 + SLASH
				+ Math.round(powerLoads[1] * 10.0)/10.0 + KW;
		
		powerloadsLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.powerload"),
				twoLoads, Msg.getString("BuildingPanelComputation.powerload.tooltip"));

		// Total Usage
		double[] combined = manager.getPeakCurrentPercent();	
		// Get the peak total
		double peak = Math.round(combined[1] * 10.0)/10.0;		
		// Get the total CUs available
		double cUs = Math.round(combined[0] * 10.0)/10.0;
		// Get total usage
		double usage = Math.round((peak - cUs)/peak * 1000.0)/10.0;

		String text = cUs + SLASH + peak + CU;
		
		percentUsageLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.usage"),
					 			StyleManager.DECIMAL_PERC1.format(usage), Msg.getString("BuildingPanelComputation.usage.tooltip"));

		cULabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.computingUnit"),
				text, Msg.getString("BuildingPanelComputation.computingUnit.tooltip"));
	
		// Total Entropy
		double entropy = manager.getTotalEntropy();
		entropyLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.entropy"),
	 			Math.round(entropy * 1_000.0)/1_000.0 + "", Msg.getString("BuildingPanelComputation.entropy.tooltip"));	
		
		return springPanel;
	}
	
	/**
	 * Create a table model that shows the comuting details of the Buildings
	 * in the settlement.
	 * 
	 * @return Table model.
	 */
	protected TableModel createModel() {
		tableModel = new TableModel(settlement);

		return tableModel;
	}

	/**
	 * Sets column widths and renderers.
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columns) {
		columns.getColumn(0).setPreferredWidth(90);
		columns.getColumn(1).setPreferredWidth(27);
		columns.getColumn(2).setPreferredWidth(27);
		columns.getColumn(3).setPreferredWidth(33);
		columns.getColumn(4).setPreferredWidth(30);
		columns.getColumn(5).setPreferredWidth(60);
		columns.getColumn(6).setPreferredWidth(33);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		columns.getColumn(1).setCellRenderer(renderer);
		columns.getColumn(2).setCellRenderer(renderer);
		columns.getColumn(3).setCellRenderer(renderer);
		columns.getColumn(5).setCellRenderer(renderer);
		columns.getColumn(6).setCellRenderer(renderer);
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		columns.getColumn(4).setCellRenderer(renderer);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		// Total Power Demand
		double[] powerLoads = manager.getTotalCombinedLoads();
		String twoLoads = Math.round(powerLoads[0] * 10.0)/10.0 + SLASH
				+ Math.round(powerLoads[1] * 10.0)/10.0 + KW;
		
		if (!powerloadsLabel.getText().equals(twoLoads))
			powerloadsLabel.setText(twoLoads);
		
		// Total Usage
		double[] combined = manager.getPeakCurrentPercent();	
		// Get the peak total
		double peak = Math.round(combined[1] * 10.0)/10.0;		
		// Get the total CUs available
		double cUs = Math.round(combined[0] * 10.0)/10.0;
		// Get total usage
		double usage = Math.round((peak - cUs)/peak * 1000.0)/10.0;
		
		String text = cUs + SLASH + peak + CU;
		
		percentUsageLabel.setText(StyleManager.DECIMAL_PERC1.format(usage));
		
		if (!cULabel.getText().equalsIgnoreCase(text))
			cULabel.setText(text);
		
		String entropy = Math.round(manager.getTotalEntropy() * 1_000.0)/1_000.0 + "";
		
		if (!entropyLabel.getText().equalsIgnoreCase(entropy))
			entropyLabel.setText(entropy);
		
		// Update  table.
		tableModel.update();
	}

	/**
	 * Internal class used as model for the table.
	 */
	private class TableModel extends AbstractTableModel
		implements EntityModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private List<Building> buildings;

		private TableModel(Settlement settlement) {
			buildings = manager.getBuildings(FunctionType.COMPUTATION);
		}

		public int getRowCount() {
			return buildings.size();
		}

		public int getColumnCount() {
			return 7;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = Building.class;
			else if (columnIndex == 1) dataType = Double.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			else if (columnIndex == 4) dataType = Double.class;
			else if (columnIndex == 5) dataType = String.class;
			else if (columnIndex == 6) dataType = Double.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelThermalSystem.column.building"); //$NON-NLS-1$
			else if (columnIndex == 1) return "kWe"; //$NON-NLS-1$
			else if (columnIndex == 2) return "kWt"; //$NON-NLS-1$
			else if (columnIndex == 3) return "Cooling"; //$NON-NLS-1$
			else if (columnIndex == 4) return "% Util"; //$NON-NLS-1$
			else if (columnIndex == 5) return "CUs"; //$NON-NLS-1$
			else if (columnIndex == 6) return "Entropy"; //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {

			if (column == 0) {
				return buildings.get(row);
			}
			if (column == 1) {
				// Power load
				return Math.round(buildings.get(row).getComputation().getCombinedPowerLoad() * 10.0)/10.0;
			}
			if (column == 2) {
				// heat generated
				return Math.round(buildings.get(row).getComputation().getInstantHeatGenerated() * 10.0)/10.0;
			}
			else if (column == 3) {
				// cooling load
				return Math.round(buildings.get(row).getComputation().getInstantCoolingLoad() * 10.0)/10.0;
			}
			else if (column == 4) {
				// Usage
				return Math.round(buildings.get(row).getComputation().getUsagePercent() * 10.0)/10.0;
			}
			else if (column == 5) {
				// Peak
				double peak = Math.round(buildings.get(row).getComputation().getPeakCU() * 100.0)/100.0;
				// Current
				double computingUnit = Math.round(buildings.get(row).getComputation().getCurrentCU() * 100.0)/100.0;
				return computingUnit + SLASH + peak;
			}
			else if (column == 6) {
				// Entropy
				return Math.round(buildings.get(row).getComputation().getEntropy( )* 1_000.0)/1_000.0;
			}

			return null;
		}

		public void update() {
			fireTableDataChanged();
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return buildings.get(row);
		}
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		settlement = null;
		manager = null;
	}
}
