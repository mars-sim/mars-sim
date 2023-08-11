/*
 * Mars Simulation Project
 * TabPanelComputing.java
 * @date 2023-08-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;

/**
 * This is a tab panel for settlement's computing capability.
 */
@SuppressWarnings("serial")
public class TabPanelComputing
extends TabPanel {

	// default logger.

	private static final String COMPUTING_ICON = "computing";

	private JLabel powerDemandLabel;
	private JLabel percentUsageLabel;
	private JLabel cULabel;
	private JLabel entropyLabel;
	
	/** The Settlement instance. */
	private Settlement settlement;

	/** Table model for heat info. */
	private TableModel tableModel;
	
	private JTable table;
			
	private JScrollPane scrollPane;
	
	private BuildingManager manager;

	private List<Building> buildings;
	
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
	}
	
	@Override
	protected void buildUI(JPanel content) {
		
		manager = settlement.getBuildingManager();

		buildings = manager.getComputingBuildings();

		JPanel topContentPanel = new JPanel();
		topContentPanel.setLayout(new BoxLayout(topContentPanel, BoxLayout.Y_AXIS));
		content.add(topContentPanel, BorderLayout.NORTH);
		
		// Prepare heat info panel.
		AttributePanel springPanel = new AttributePanel(4);
		content.add(springPanel, BorderLayout.NORTH);

		// Total Power Demand
		double powerDemand = manager.getTotalComputingPowerDemand();
		powerDemandLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.powerDemand"),
				     StyleManager.DECIMAL_KW.format(powerDemand), Msg.getString("BuildingPanelComputation.powerDemand.tooltip"));

		// Total Usage
		double usage = manager.getComputingUsagePercent();
		percentUsageLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.usage"),
					 			StyleManager.DECIMAL_PERC.format(usage), Msg.getString("BuildingPanelComputation.usage.tooltip"));

		// Peak Usage
		double peak = Math.round(manager.getPeakTotalComputing() * 1_000.0)/1_000.0;
		
		// Total CUs Available
		double cUs = Math.round(manager.getTotalCapacityCUsComputing() * 1_000.0)/1_000.0;
		
		String text = cUs + " / " + peak + " CUs";
		cULabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.computingUnit"),
				text, Msg.getString("BuildingPanelComputation.computingUnit.tooltip"));
	
		// Total Entropy
		double entropy = manager.getTotalEntropy();
		entropyLabel = springPanel.addTextField(Msg.getString("BuildingPanelComputation.entropy"),
	 			Math.round(entropy * 1_000.0)/1_000.0 + "", Msg.getString("BuildingPanelComputation.entropy.tooltip"));	
		
		// Create scroll panel for the outer table panel.
		scrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scrollPane,BorderLayout.CENTER);
		
		// Prepare table model.
		tableModel = new TableModel(settlement);
		
		// Prepare table.
		table = new JTable(tableModel);
		// Call up the building window when clicking on a row on the table
		table.addMouseListener(new UnitTableLauncher(getDesktop()));
		
		table.setRowSelectionAllowed(true);
		TableColumnModel columns = table.getColumnModel();
		columns.getColumn(0).setPreferredWidth(150);
		columns.getColumn(1).setPreferredWidth(30);
		columns.getColumn(2).setPreferredWidth(30);
		columns.getColumn(3).setPreferredWidth(50);
		columns.getColumn(4).setPreferredWidth(30);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
//		heatColumns.getColumn(1).setCellRenderer(renderer);
		columns.getColumn(2).setCellRenderer(renderer);
		columns.getColumn(3).setCellRenderer(renderer);
		columns.getColumn(4).setCellRenderer(renderer);
		
		// Resizable automatically when its Panel resizes
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Add sorting
		table.setAutoCreateRowSorter(true);

		scrollPane.setViewportView(table);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {

		// Total Power Demand
		double powerDemand = manager.getTotalComputingPowerDemand();
		String power = StyleManager.DECIMAL_KW.format(powerDemand);
		
		if (!powerDemandLabel.getText().equalsIgnoreCase(power))
			powerDemandLabel.setText(power);
		
		// Total Usage
		double usage = manager.getComputingUsagePercent();
		percentUsageLabel.setText(StyleManager.DECIMAL_PERC.format(usage));
		
		// Peak Usage
		double peak = Math.round(manager.getPeakTotalComputing() * 100.0)/100.0;
		// Total CUs Available
		double cUs = Math.round(manager.getTotalCapacityCUsComputing() * 100.0)/100.0;
		
		String text = cUs + " / " + peak;
		
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
		implements UnitModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private TableModel(Settlement settlement) {
		}

		public int getRowCount() {
			return buildings.size();
		}

		public int getColumnCount() {
			return 5;
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = Building.class;
			else if (columnIndex == 1) dataType = Double.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = String.class;
			else if (columnIndex == 4) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelThermalSystem.column.building"); //$NON-NLS-1$
			else if (columnIndex == 1) return "Power Demand"; //$NON-NLS-1$
			else if (columnIndex == 2) return "% Usage"; //$NON-NLS-1$
			else if (columnIndex == 3) return "CUs"; //$NON-NLS-1$
			else if (columnIndex == 4) return "Entropy"; //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {

			if (column == 0) {
				return buildings.get(row);
			}
			if (column == 1) {
				// Power Demand
				return Math.round(buildings.get(row).getComputation().getFullPowerRequired() * 100.0)/100.0;
			}
			else if (column == 2) {
				// Usage
				return Math.round(buildings.get(row).getComputation().getUsagePercent() * 100.0)/100.0;
			}
			else if (column == 3) {
				// Peak
				double peak = Math.round(buildings.get(row).getComputation().getPeakComputingUnit() * 1_000.0)/1_000.0;
				// Current
				double computingUnit = Math.round(buildings.get(row).getComputation().getComputingUnitCapacity() * 1_000.0)/1_000.0;
				return computingUnit + " / " + peak + " CUs";
			}
			else if (column == 4) {
				// Entropy
				return Math.round(buildings.get(row).getComputation().getEntropy( )* 1_000.0)/1_000.0;
			}

			return null;
		}

		public void update() {
			scrollPane.validate();

			fireTableDataChanged();
		}

		@Override
		public Unit getAssociatedUnit(int row) {
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
