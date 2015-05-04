/**
 * Mars Simulation Project
 * TabPanelPowerGrid.java
 * @version 3.07 2014-12-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.PowerGrid;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.PowerGeneration;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * This is a tab panel for a settlement's power grid information.
 */
public class TabPanelPowerGrid
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data Members
	/** The total power generated label. */
	private JLabel powerGeneratedLabel;
	/** The total power used label. */
	private JLabel powerUsedLabel;
	/** The total power storage capacity label. */
	private JLabel powerStorageCapacityLabel;
	/** The total power stored label. */
	private JLabel powerStoredLabel;
	/** Table model for power info. */
	private PowerTableModel powerTableModel;
	/** The settlement's power grid. */
	private PowerGrid powerGrid;

	// Data cache
	/** The total power generated cache. */
	private double powerGeneratedCache;
	/** The total power used cache. */
	private double powerUsedCache;
	/** The total power storage capacity cache. */
	private double powerStorageCapacityCache;
	/** The total power stored cache. */
	private double powerStoredCache;

	private DecimalFormat formatter = new DecimalFormat(Msg.getString("TabPanelPowerGrid.decimalFormat")); //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelPowerGrid(Unit unit, MainDesktopPane desktop) {

		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelPowerGrid.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelPowerGrid.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Settlement settlement = (Settlement) unit;
		powerGrid = settlement.getPowerGrid();

		// Prepare power grid label panel.
		JPanel powerGridLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(powerGridLabelPanel);

		// Prepare power grid label.
		JLabel titleLabel = new JLabel(Msg.getString("TabPanelPowerGrid.label"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		powerGridLabelPanel.add(titleLabel);

		// Prepare power info panel.
		JPanel powerInfoPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		powerInfoPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(powerInfoPanel);

		// Prepare power generated label.
		powerGeneratedCache = powerGrid.getGeneratedPower();
		powerGeneratedLabel = new JLabel(Msg.getString("TabPanelPowerGrid.totalPowerGenerated", formatter.format(powerGeneratedCache)), JLabel.CENTER); //$NON-NLS-1$
		powerInfoPanel.add(powerGeneratedLabel);

		// Prepare power used label.
		powerUsedCache = powerGrid.getRequiredPower();
		powerUsedLabel = new JLabel(Msg.getString("TabPanelPowerGrid.totalPowerUsed", formatter.format(powerUsedCache)), JLabel.CENTER); //$NON-NLS-1$
		powerInfoPanel.add(powerUsedLabel);

		// Prepare power storage capacity label.
		powerStorageCapacityCache = powerGrid.getStoredPowerCapacity();
		powerStorageCapacityLabel = new JLabel(Msg.getString("TabPanelPowerGrid.powerStorageCapacity", formatter.format(powerStorageCapacityCache)), JLabel.CENTER); //$NON-NLS-1$
		powerInfoPanel.add(powerStorageCapacityLabel);

		// Prepare power stored label.
		powerStoredCache = powerGrid.getStoredPower();
		powerStoredLabel = new JLabel(Msg.getString("TabPanelPowerGrid.totalPowerStored", formatter.format(powerStoredCache)), JLabel.CENTER); //$NON-NLS-1$
		powerInfoPanel.add(powerStoredLabel);

		// Create scroll panel for the outer table panel.
		JScrollPane powerScrollPane = new JScrollPane();
		//powerScrollPane.setPreferredSize(new Dimension(257, 230));
		// increase vertical mousewheel scrolling speed for this one
		powerScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		centerContentPanel.add(powerScrollPane,BorderLayout.CENTER);

		// Prepare outer table panel.
		//JPanel outerTablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		//outerTablePanel.setBorder(new MarsPanelBorder());
		//powerScrollPane.setViewportView(outerTablePanel);


		// Prepare power table panel.
		//JPanel powerTablePanel = new JPanel(new BorderLayout(0, 0));
		//outerTablePanel.add(powerTablePanel);
		// powerScrollPanel.setViewportView(powerTablePanel);

		// Prepare power table model.
		powerTableModel = new PowerTableModel(settlement);

		// Prepare power table.
		JTable powerTable = new JTable(powerTableModel);
		powerTable.setCellSelectionEnabled(false);
		powerTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		powerTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		powerTable.getColumnModel().getColumn(1).setPreferredWidth(120);
		powerTable.getColumnModel().getColumn(2).setPreferredWidth(40);
		powerTable.getColumnModel().getColumn(3).setPreferredWidth(10);
		// 2014-12-03 Added the two methods below to make all heatTable columns
		//resizable automatically when its Panel resizes
		powerTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		powerTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		//powerTablePanel.add(powerTable.getTableHeader(), BorderLayout.NORTH);
		//powerTablePanel.add(powerTable, BorderLayout.CENTER);
		powerScrollPane.setViewportView(powerTable);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {

		// Update power generated label.
		if (powerGeneratedCache != powerGrid.getGeneratedPower()) {
			powerGeneratedCache = powerGrid.getGeneratedPower();
			powerGeneratedLabel.setText(
				Msg.getString(
					"TabPanelPowerGrid.totalPowerGenerated", //$NON-NLS-1$
					formatter.format(powerGeneratedCache)
				)
			);
		}

		// Update power used label.
		if (powerUsedCache != powerGrid.getRequiredPower()) {
			powerUsedCache = powerGrid.getRequiredPower();
			powerUsedLabel.setText(Msg.getString("TabPanelPowerGrid.totalPowerUsed",formatter.format(powerUsedCache))); //$NON-NLS-1$
		}

		// Update power storage capacity label.
		if (powerStorageCapacityCache != powerGrid.getStoredPowerCapacity()) {
			powerStorageCapacityCache = powerGrid.getStoredPowerCapacity();
			powerStorageCapacityLabel.setText(Msg.getString(
				"TabPanelPowerGrid.powerStorageCapacity", //$NON-NLS-1$
				formatter.format(powerStorageCapacityCache)
			));
		}

		// Update power stored label.
		if (powerStoredCache != powerGrid.getStoredPower()) {
			powerStoredCache = powerGrid.getStoredPower();
			powerStoredLabel.setText(Msg.getString(
				"TabPanelPowerGrid.totalPowerStored", //$NON-NLS-1$
				formatter.format(powerStoredCache)
			));
		}

		// Update power table.
		powerTableModel.update();
	}

	/**
	 * Internal class used as model for the power table.
	 */
	private static class PowerTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Settlement settlement;
		private java.util.List<Building> buildings;
		private ImageIcon dotRed;
		private ImageIcon dotYellow;
		private ImageIcon dotGreen;

		private PowerTableModel(Settlement settlement) {
			this.settlement = settlement;
			buildings = settlement.getBuildingManager().getBuildings();
			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$
			dotYellow = ImageLoader.getIcon(Msg.getString("img.dotYellow")); //$NON-NLS-1$
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen")); //$NON-NLS-1$
		}

		public int getRowCount() {
			return buildings.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelPowerGrid.column.s"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelPowerGrid.column.building"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelPowerGrid.column.generated"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelPowerGrid.column.used"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Building building = buildings.get(row);
			PowerMode powerMode = building.getPowerMode();

			if (column == 0) {
				if (powerMode == PowerMode.FULL_POWER) {
					return dotGreen;
				}
				else if (powerMode == PowerMode.POWER_DOWN) {
					return dotYellow;
				}
				else if (powerMode == PowerMode.NO_POWER) {
					return dotRed;
				}
				else return null;
			}
			else if (column == 1) return buildings.get(row);
			else if (column == 2) {
				double generated = 0D;
				if (building.hasFunction(BuildingFunction.POWER_GENERATION)) {
					try {
						PowerGeneration generator = (PowerGeneration) building.getFunction(BuildingFunction.POWER_GENERATION);
						generated = generator.getGeneratedPower();
					}
					catch (Exception e) {}
				}
				if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {
					try {
						ThermalGeneration heater = (ThermalGeneration) building.getFunction(BuildingFunction.THERMAL_GENERATION);
						generated += heater.calculateGeneratedPower();
					}
					catch (Exception e) {}
				}
				return generated;
			}
			else if (column == 3) {
				double used = 0D;
				if (powerMode == PowerMode.FULL_POWER)
					used = building.getFullPowerRequired();
				else if (powerMode == PowerMode.POWER_DOWN)
					used = building.getPoweredDownPowerRequired();
				return used;
			}
			else return null;
		}

		public void update() {
			if (!buildings.equals(settlement.getBuildingManager().getBuildings()))
				buildings = settlement.getBuildingManager().getBuildings();

			fireTableDataChanged();
		}
	}
}